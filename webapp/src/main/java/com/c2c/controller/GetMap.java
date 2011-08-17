package com.c2c.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.c2c.geoutils.ReferenceEnvelopeFactory;

/**
 * The Controller for handling compute requests.
 * <p/>
 * This class is registered as a bean in ws-servlet.xml.
 *
 * @author jeichar
 */
@Controller
@RequestMapping("/getmap")
public class GetMap extends AbstractQueryingController {

    @RequestMapping(method = RequestMethod.GET)
    public void getmap(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestParam("QUERYID") String queryId,
                       @RequestParam("BBOX") String bbox,
                       @RequestParam("WIDTH") int width,
                       @RequestParam("HEIGHT") int height,
                       @RequestParam(value = "STYLEID", required = false) String styleId,
                       @RequestParam(value = "SRS", required = false) String srs,
                       @RequestParam(value = "FORMAT", required = false) String format,
                       @RequestParam(value = "FORMAT_OPTIONS", required = false) String format_options)
            throws Exception {

        if (format == null) {
            format = "image/png";
        }
        int dpi = 90;
        if (format_options != null) {
        	dpi = Util.getDpiFromFormat(format_options);
        }
        SimpleFeatureSource results = (SimpleFeatureSource) getCache().getResults(queryId).getFeatureSource();

        response.setContentType(format);
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Expires", "-1");

        ReferencedEnvelope bounds = ReferenceEnvelopeFactory.toReferencedEnvelope(bbox,
                srs == null ? "EPSG:4326" : srs);
        Rectangle imageSize = new Rectangle(width, height);
        Style sld = getCache().getStyle(styleId).getGtStyle(results.getFeatures());

        //if(results.getSchema().getGeometryDescriptor() == null)
        renderMap(response, results, bounds, imageSize, sld,
                parseFormat(format), dpi);
    }

    private String parseFormat(String format) {
        if (format == null) {
            return "png";
        }
        String[] parts = format.split("/");
        if (parts.length == 1) {
            return format;
        }
        return parts[1];
    }

    private void renderMap(HttpServletResponse response,
                           SimpleFeatureSource results,
                           ReferencedEnvelope bounds, Rectangle imageSize, Style sld,
                           String format, int dpi) throws IOException {
        MapLayer[] layers = {new MapLayer(results, sld)};
        MapContext map = new DefaultMapContext(layers, bounds
                .getCoordinateReferenceSystem());

        try {
            GTRenderer renderer = new StreamingRenderer();
            Map hints = new HashMap();
            hints.put(StreamingRenderer.DPI_KEY, dpi);
            renderer.setRendererHints(hints);
            
            renderer.setContext(map);
            BufferedImage image = new BufferedImage(imageSize.width,
                    imageSize.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();

            try {
                if (results.getSchema().getGeometryDescriptor() == null) {
                    graphics.setColor(Color.BLACK);
                    int y = imageSize.height / 2;
                    Font font = new Font(Font.SERIF, Font.BOLD, 14);
                    graphics.setFont(font);
                    graphics.drawString("Results have no geometries", 10, y - 14);
                } else {
                    renderer.paint(graphics, imageSize, bounds);
                }
                ServletOutputStream output = response.getOutputStream();
                try {
                    ImageIO.write(image, format, output);
                } finally {
                    output.close();
                }
            } finally {
                graphics.dispose();
            }
        } finally {
            map.dispose();
        }
    }

}
