package com.c2c.controller;

import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.c2c.data.DataQueryFeatureSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static java.lang.String.format;

/**
 * The spring controller for obtaining the result data in json
 *
 * @author jeichar
 */
@Controller
@RequestMapping("/getdata")
public class GetData extends AbstractQueryingController {
    private FeatureJSON JSON_ENCODER = new FeatureJSON();
    {
        JSON_ENCODER.setEncodeFeatureCollectionBounds(true);
    }

    /**
     * Return the results of the query in JSON format
     *
     * @param attr   Comma separated list of attributes to return. Default is all
     *               non-geometry attributes
     * @param format the format of the features. Currently only text/json is supported
     * @see AbstractQueryingController for datasource, query and catalog
     *      parameter description
     */
    @RequestMapping(method = RequestMethod.GET)
    public void getdata(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "QUERYID") String queryId,
            @RequestParam(value = "ATTR", required = false) String attr,
            @RequestParam(value = "FORMAT", required = false) String format,
            @RequestParam(value = "TARGETSRS", required = false) String targetSrs
    ) throws Exception {

        response.setContentType("application/json; charset=UTF-8");

    	DataQueryFeatureSource results = getCache().getResults(queryId);
    	SimpleFeatureSource featureSource = (SimpleFeatureSource) results.getFeatureSource();
    	
        PrintWriter writer = response.getWriter();
        try {

            //cellFormat(results,writer);
            rowFormat(results, writer);
            
        } finally {
            writer.close();
        }
    }
             /*
    private void cellFormat(DataQueryFeatureSource results, PrintWriter writer) {
        results
        
    }          */

    private void rowFormat(DataQueryFeatureSource results, PrintWriter writer) throws JSONException, IOException {
        SimpleFeatureSource featureSource = results.getFeatureSource();
        JSONObject ret = new JSONObject();
        JSONObject metaData = new JSONObject();
        metaData.put("root", "rows");

        JSONArray fields = new JSONArray();
        for (AttributeDescriptor d : featureSource.getSchema().getAttributeDescriptors()) {
            String name = d.getName().toString().replace("[", "").replace("]", "");
            if (!name.equals("geom") && !name.equals("point")) {
                fields.put(new JSONObject().put("name", name));
            }
        }
        metaData.put("fields", fields);
        ret.putOpt("metaData", metaData);

        JSONArray rows = new JSONArray();
        boolean first = true;
        for (SimpleFeatureIterator it = featureSource.getFeatures().features(); it.hasNext() ; ) {

            JSONObject feature = new JSONObject();
            SimpleFeature current = it.next();

            for (AttributeDescriptor d : featureSource.getSchema().getAttributeDescriptors()) {
                if(!(d instanceof GeometryDescriptor)) {
                	String name = d.getName().toString();
                	feature.put(name.replace("[", "").replace("]", ""), current.getAttribute(name));
                }
            }
            rows.put(feature);
        }
        ret.put("rows", rows);

        writer.write(ret.toString());
    }
}
