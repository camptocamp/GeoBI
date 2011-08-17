package com.c2c.controller;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactory2;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.styling.StyledLayerDescriptorImpl;
import org.geotools.styling.Symbolizer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.c2c.controller.Util.SYMBOL_TYPE;
import com.c2c.style.StyleGenerationParams;

/**
 * User: jeichar
 * Date: Jul 4, 2010
 * Time: 2:22:24 PM
 */
@Controller
@RequestMapping("/registermapstyle")
public class RegisterMapStyle extends AbstractQueryingController {

    StyleFactory styleFactory = new StyleFactoryImpl();

    @RequestMapping(method = RequestMethod.GET)
    public void registerqueryGET(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "QUERYID", required = false) String queryId,
            @RequestParam(value = "CHOROPLETHS_INDICATOR", required = false) String choroplethsIndicator,
            @RequestParam(value = "CLASSIFICATION_METHOD", required = false) String classificationMethod,
            @RequestParam(value = "NB_CLASSES", required = false) Integer nbClasses,
            @RequestParam(value = "COLORS", required = false) String colors,
            @RequestParam(value = "CHOROPLETHS_OPACITY", required = false) Double choroplethsOpacity,
            @RequestParam(value = "INTERPOLATION", required = false) String interpolation,
            @RequestParam(value = "OVERLAY_TYPE", required = false) String overlayType,
            @RequestParam(value = "OVERLAY_INDICATORS", required = false) String overlayIndicators,
            @RequestParam(value = "OVERLAY_OPACITY", required = false) Double overlayOpacity,
            @RequestParam(value = "SIZES", required = false) String sizes,
            @RequestParam(value = "SLD", required = false) String sld
    ) throws Exception {
        registerqueryPOST(request, response, queryId, choroplethsIndicator, classificationMethod, nbClasses, colors,
                choroplethsOpacity, interpolation, overlayType, overlayIndicators, overlayOpacity, sizes, sld);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void registerqueryPOST(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "QUERYID", required = false) String queryId,
            @RequestParam(value = "CHOROPLETHS_INDICATOR", required = false) String choroplethsIndicator,
            @RequestParam(value = "CLASSIFICATION_METHOD", required = false) String classificationMethod,
            @RequestParam(value = "NB_CLASSES", required = false) Integer nbClasses,
            @RequestParam(value = "COLORS", required = false) String colors,
            @RequestParam(value = "CHOROPLETHS_OPACITY", required = false) Double choroplethsOpacity,
            @RequestParam(value = "INTERPOLATION", required = false) String interpolation,
            @RequestParam(value = "OVERLAY_TYPE", required = false) String overlayType,
            @RequestParam(value = "OVERLAY_INDICATORS", required = false) String overlayIndicators,
            @RequestParam(value = "OVERLAY_OPACITY", required = false) Double overlayOpacity,
            @RequestParam(value = "SIZES", required = false) String sizes,
            @RequestParam(value = "SLD", required = false) String sld
    )
            throws Exception {

        String id;
        // TODO : necessary ?
//        if (sld != null) {
//            id = cacheStyle(request, parseSLD(sld));
//        } else {
            SimpleFeatureSource results = (SimpleFeatureSource) getCache().getResults(queryId).getFeatureSource();
            Util.SYMBOL_TYPE symtype ;
          
            if ("bar".equalsIgnoreCase(overlayType))
            		symtype = SYMBOL_TYPE.BARS;
            else if ("pie".equalsIgnoreCase(overlayType))
        		symtype = SYMBOL_TYPE.PIES;
            else if ("symbol".equalsIgnoreCase(overlayType))
            	symtype =  SYMBOL_TYPE.PROPORTIONAL_SYMBOLS;
            else
            	symtype =  SYMBOL_TYPE.NONE;
            
            final StyleGenerationParams params = new StyleGenerationParams(choroplethsIndicator, classificationMethod, nbClasses,
                    colors, choroplethsOpacity, interpolation, overlayType, overlayIndicators, sizes, overlayOpacity, queryId, symtype);
            id = getCache().putStyle(params);
//        }

        response.setContentType("application/json");

        PrintWriter writer = response.getWriter();
        try {
            writer.append("{\"id\": \"");
            writer.append(id);
            writer.append("\"}");
        } finally {
            writer.close();
        }

    }

}
