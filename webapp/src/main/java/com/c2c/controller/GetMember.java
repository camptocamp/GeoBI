package com.c2c.controller;

import static java.lang.Double.parseDouble;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.Intersects;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.c2c.data.DataQueryDimension;
import com.c2c.data.DataQueryFeatureSource;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * The spring controller for obtaining the feature data of all the features
 * within a pixel of a returned map. A simplified geometry should be returned if
 * the geometry data is to be returned. This method is very related to GetData
 * and shares much of the same code.
 *
 * @author jeichar, pmauduit
 */
@Controller
@RequestMapping("/getmember")
public class GetMember extends AbstractQueryingController {
    
    @RequestMapping(method = RequestMethod.GET)
    public void getmember(HttpServletRequest request,
                          HttpServletResponse response,
                          @RequestParam(value = "QUERYID") String queryId,
                          @RequestParam(value = "DIMENSION") String dimension,
                          @RequestParam(value = "BBOX") String bbox) throws Exception {

        response.setContentType("application/json; charset=UTF-8");

        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        
        final int finalMaxFeatures = Integer.MAX_VALUE;
        
        String ret = "";
        	
        DataQueryFeatureSource rs = getCache().getResults(queryId);
        SimpleFeatureSource results = (SimpleFeatureSource) rs.getFeatureSource();
        
        
        ArrayList<String> dimKeys = new ArrayList<String>();
        
        for (DataQueryDimension d : rs.getRows()) {
        	dimKeys.add(d.getUniqueName());
        }
          
        CoordinateReferenceSystem targetCrs = null;

        String[] attrs = new String[1];
        attrs[0] = dimension;
        Query datastoreQuery = makeDatastoreQuery(bbox, results, finalMaxFeatures, attrs, crs, targetCrs);

        FeatureCollection<SimpleFeatureType, SimpleFeature> results2 = results.getFeatures(datastoreQuery);
   
        FeatureIterator<SimpleFeature> it = results2.features();

        try {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();

                if (feature.getAttribute(dimension) instanceof String)
                {
                	ret = (String) feature.getAttribute(dimension);
                	break;
                }
            }
        } finally {
            it.close();
        }
        	
        PrintWriter writer = response.getWriter();

        try {
            response.setContentType("text/plain");
            writer.write(ret);
        } finally {
            writer.close();
        }
    }

    private Query makeDatastoreQuery(String _bbox, SimpleFeatureSource results,
                                     int finalMaxFeatures, String[] attributes, CoordinateReferenceSystem crs, CoordinateReferenceSystem targetCrs) throws FactoryException, TransformException {
        GeometryFactory fac = new GeometryFactory();

        Geometry referenceGeom;
        String[] bbox = _bbox.split(",");
        double minx = parseDouble(bbox[0]);
        double miny = parseDouble(bbox[1]);
        double maxx = parseDouble(bbox[2]);
        double maxy = parseDouble(bbox[3]);

        Envelope env = new Envelope(minx, maxx, miny, maxy);
        referenceGeom = fac.toGeometry(env);

        final CoordinateReferenceSystem nativeCrs = results.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();

        if (nativeCrs != null) {
            JTS.transform(referenceGeom, CRS.findMathTransform(crs, nativeCrs, true));
        }

        final SimpleFeatureType schema = results.getSchema();
        String geometryAttributeName = schema.getGeometryDescriptor().getLocalName();

        FilterFactory2 filterFactory2 = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

        final PropertyName geomAttribute = filterFactory2.property(geometryAttributeName);
        Intersects filter = filterFactory2.intersects(geomAttribute, filterFactory2.literal(referenceGeom));

        if (attributes != null) {
            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = attributes[i].trim();
            }
        }

        final Query query = new Query(schema.getTypeName(), filter, attributes);
        query.setCoordinateSystemReproject(targetCrs);
        query.setMaxFeatures(finalMaxFeatures);
        return query;
    }

}
