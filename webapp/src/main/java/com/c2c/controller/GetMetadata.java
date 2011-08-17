package com.c2c.controller;

import com.c2c.data.*;
import com.c2c.query.DataQuery;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * The spring controller for obtaining the result data in json
 *
 * @author pmauduit
 */
@Controller
@RequestMapping("/getmetadata")
public class GetMetadata extends AbstractQueryingController {
    private FeatureJSON JSON_ENCODER = new FeatureJSON();
    {
        JSON_ENCODER.setEncodeFeatureCollectionBounds(true);
    }

    private List<IndicatorMetadata> getIndicators(List<DataQueryDimension> columns) {
    	
    	List<IndicatorMetadata> indicators = new ArrayList<IndicatorMetadata>();
    	for (DataQueryDimension c : columns) {
    		List<IndicatorMetadata> newIndicators = new ArrayList<IndicatorMetadata>();
    		
    		if (indicators.isEmpty()) {
    			for (DataQueryLevel l : c.getLevels()) {
	    			for (DataQueryMember m : l.getMembers()) {
	    				newIndicators.add(new IndicatorMetadata(m.getName(),
	    						DataQuery.getUniqueId(m.getUniqueName())));
	    			}
    			}
    		} else {
    			for (IndicatorMetadata i : indicators) {
    				for (DataQueryLevel l : c.getLevels()) {
    					for (DataQueryMember m : l.getMembers()) {
    						newIndicators.add(new IndicatorMetadata(i.getName() + " / " + m.getName(),
    								i.getDataIndex() + "_" + DataQuery.getUniqueId(m.getUniqueName())));
    					}
    				}
    			}    			
    		}
    		indicators = newIndicators;
    	}
    	return indicators;
    }
    
    private JSONArray buildDimensionsArray(List<DataQueryDimension> dimensions) throws Exception {
    	
    	JSONArray array = new JSONArray();
        for (DataQueryDimension d : dimensions) {
        	JSONObject dim = new JSONObject();
        	dim.put("dimension_name", d.getUniqueName().replace("[", "").replace("]", ""));
        	dim.put("dimension_unique_name", d.getUniqueName());
        	
        	JSONArray levels = new JSONArray();
        	for (DataQueryLevel l : d.getLevels()) {
        		JSONObject lev = new JSONObject();
        		lev.put("level_unique_name", l.getUniqueName());
        		
            	JSONArray members = new JSONArray();
            	for (DataQueryMember m : l.getMembers()) {
            		JSONObject memb = new JSONObject();
            		memb.put("member_name", m.getName());
            		memb.put("member_unique_name", m.getUniqueName());
            		memb.put("data_index", DataQuery.getUniqueId(m.getUniqueName()));
            		members.put(memb);
            	}
            	lev.put("members", members);
        		
        		levels.put(lev);
        	}
        	dim.put("levels", levels);
        	                	
        	array.put(dim);
        }
    	return array;
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
    public void getmetadata(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "QUERYID") String queryId,
            @RequestParam(value = "FORMAT", required = false) String format
    ) throws Exception {

        response.setContentType("application/json; charset=UTF-8");

    	DataQueryFeatureSource results = getCache().getResults(queryId);
    	SimpleFeatureSource featureSource = (SimpleFeatureSource) results.getFeatureSource();

        PrintWriter writer = response.getWriter();
        try {
            JSONObject ret = new JSONObject();
            
        	JSONArray indicators = new JSONArray();
        	for (IndicatorMetadata i : getIndicators(results.getColumns())) {
            	JSONObject indic = new JSONObject();
            	indic.put("name", i.getName());
            	indic.put("data_index", i.getDataIndex());
            	indicators.put(indic);
        	}        	
        	ret.put("choropleths_indicators", indicators);           
        	
            JSONArray rows = buildDimensionsArray(results.getRows());
            ret.put("rows", rows);
            JSONArray columns = buildDimensionsArray(results.getColumns());
            ret.put("columns", columns);
            ret.put("bbox", results.getBoundingBox());
            writer.write(ret.toString());
            
        } finally {
            writer.close();
        }
    }
}

class IndicatorMetadata {
	
	private final String name;
	private final String dataIndex;
	
	public IndicatorMetadata(String name, String dataIndex) {
		this.name = name;
		this.dataIndex = dataIndex;
	}
	
	public String getName() {
		return name;
	}

	public String getDataIndex() {
		return dataIndex;
	}
}
