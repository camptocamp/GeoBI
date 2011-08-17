package com.c2c.controller;

import static java.lang.Double.parseDouble;
import static org.geotools.feature.simple.SimpleFeatureBuilder.build;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
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
import com.c2c.style.StyleGenerationParams;
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
@RequestMapping("/getinfo")
public class GetInfo extends AbstractQueryingController {

    private FeatureJSON JSON_ENCODER = new FeatureJSON();

    private HashMap<String, String> indicatorsTable = null;
    
    @RequestMapping(method = RequestMethod.GET)
    public void getinfo(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam(value = "QUERYID") String queryId,
                        @RequestParam(value = "STYLEID") String styleId,
                        @RequestParam(value = "BBOX") String bbox,
                        @RequestParam(value = "ATTRS", required = false) String attributes,
                        @RequestParam(value = "SRS", required = false) String srs,
                        @RequestParam(value = "TARGETSRS", required = false) String targetSrs,
                        @RequestParam(value = "MAXFEATURES", required = false) Integer maxfeatures,
                        @RequestParam(value = "FORMAT", required = false) String format) throws Exception {

        response.setContentType("application/json; charset=UTF-8");

        final CoordinateReferenceSystem crs;
        if (srs == null) {
            crs = DefaultGeographicCRS.WGS84;
        } else {
            crs = CRS.decode(srs, true);
        }

        final int finalMaxFeatures;
        if (maxfeatures == null) {
            finalMaxFeatures = Integer.MAX_VALUE;
        } else {
            finalMaxFeatures = maxfeatures.intValue();
        }
        DataQueryFeatureSource rs = getCache().getResults(queryId);
        SimpleFeatureSource results = (SimpleFeatureSource) rs.getFeatureSource();
        
        
        ArrayList<String> dimKeys = new ArrayList<String>();
        
        for (DataQueryDimension d : rs.getRows()) {
        	dimKeys.add(d.getUniqueName());
        }
        indicatorsTable = Util.lookupIndicators(rs);
        
            
        CoordinateReferenceSystem targetCrs = null;
        if (targetSrs != null) {
            targetCrs = CRS.decode(targetSrs, true);
        }

        /* lookup style */           
        StyleGenerationParams sld = getCache().getStyle(styleId);
        int numIndicators = 0;
        
        String[] attrs = null;

        String choroplethsIndicators = sld.getChoroplethsIndicator();            
        String[] overlayIndicators = sld.getOverlayIndicators();
        
		if (overlayIndicators != null) {
			numIndicators += overlayIndicators.length + dimKeys.size();
		} else {
			numIndicators += dimKeys.size();
		}
		int currentIndex = 0;

		if (choroplethsIndicators != null) {
			numIndicators++;
		}

		attrs = new String[numIndicators];
		if (overlayIndicators != null) {
			for (int i = 0; i < overlayIndicators.length; i++) {
				attrs[i] = overlayIndicators[i];
				currentIndex++;
			}
		}
		if (choroplethsIndicators != null) {
			attrs[currentIndex] = choroplethsIndicators;
			currentIndex++;
		}
		for (int i = 0; i < dimKeys.size(); i++) {
			attrs[currentIndex + i] = dimKeys.get(i);
		}
                    
        Query datastoreQuery = makeDatastoreQuery(bbox, results, finalMaxFeatures, attrs, crs, targetCrs);

        FeatureCollection<SimpleFeatureType, SimpleFeature> results2 = results.getFeatures(datastoreQuery);
        FeatureCollection<SimpleFeatureType, SimpleFeature> results3 = FeatureCollections.newCollection();

        FeatureIterator<SimpleFeature> it = results2.features();

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(results2.getSchema().getName());
        Map<String,String> attNameMapping = new HashMap<String,String>();

        AttributeTypeBuilder attBuilder = new AttributeTypeBuilder();

        ArrayList<String> keptColumns = new ArrayList<String>();
        
        for(AttributeDescriptor att : results2.getSchema().getAttributeDescriptors()) {
        	
            String originalName = att.getLocalName();
            
            if (dimKeys.contains(originalName))
            	continue;
            
            String humanReadable = readableName(originalName);
            attNameMapping.put(originalName, humanReadable);

            attBuilder.init(att);
            typeBuilder.add(attBuilder.buildDescriptor(humanReadable));
            
            keptColumns.add(originalName);
        }
        
        /* adding specific column members */
        typeBuilder.add("member", String.class);
        
        SimpleFeatureType renamedType = typeBuilder.buildFeatureType();              

        try {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                String membersColumnValue = "";
                for (String curKey : dimKeys)
                {
                	membersColumnValue += " " + feature.getAttribute(curKey);
                }
                membersColumnValue =  membersColumnValue.trim();

                ArrayList<Object> finalCols = new ArrayList<Object>();
                
                for (String curKept : keptColumns)
                {
                	finalCols.add(feature.getAttribute(curKept));	
                }

                SimpleFeature newFeature = build(renamedType, finalCols, feature.getID());
                newFeature.setAttribute("member", membersColumnValue);

                results3.add(newFeature);
            }
        } finally {
            it.close();
        }
        	
        PrintWriter writer = response.getWriter();

        try {
            response.setContentType("application/json");
            JSON_ENCODER.writeFeatureCollection(results3, writer);
        } finally {
            writer.close();
        }
    }

    private String readableName(String originalName) {
    	String ret = null ;
        if (indicatorsTable != null)
        {
        	ret =  indicatorsTable.get(originalName);
        }
        // dimension_unique_name ?
        if (ret == null)
        {
        	return originalName;
        }
        /* else */
        return ret;
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
