package com.c2c.controller;

import java.io.PrintWriter;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The spring controller for obtaining the a registered SLD style
 *
 * @author jeichar
 */
@Controller
@RequestMapping("/getmapstyle")
public class GetMapStyle extends AbstractQueryingController {


	
    @RequestMapping(method = RequestMethod.GET)
    public void getstyle(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "QUERYID") String queryId,
            @RequestParam(value = "STYLEID") String styleId
    )
            throws Exception {

        SimpleFeatureSource results = (SimpleFeatureSource) getCache().getResults(queryId).getFeatureSource();

        Style style = getCache().getStyle(styleId).getGtStyle(results.getFeatures());
        if(style == null) {
            throw new IllegalArgumentException("There is no style cached with id: "+ styleId);
        }

        response.setContentType("application/xml");
        PrintWriter writer = response.getWriter();

        try {
            SLDTransformer styleTransform = new SLDTransformer();
            styleTransform.setEncoding(Charset.forName("UTF-8"));
            styleTransform.setIndentation(4);     

            String xml = styleTransform.transform(style);
            writer.write(xml);
        } finally {
            writer.close();
        }
        
    }
    
}