package com.c2c.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.BasicPolygonStyle;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.c2c.geoutils.ReferenceEnvelopeFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
/**
 * The Controller for generating base layer maps, given a list of dimensions (levels).
 * <p/>
 * This class is registered as a bean in ws-servlet.xml.
 *
 * @author pmauduit
 */
@Controller
@RequestMapping("/getbaselayer")
public class GetBaseLayer extends AbstractQueryingController {

	private Style baseStyle;
	
	private String dbServer ;
	private String dbName ;
	private String dbUser ;
	private String dbPassword ;
	
    
	
    @RequestMapping(method = RequestMethod.GET)
    public void getbaselayer(HttpServletRequest request,
                       		 HttpServletResponse response,
                       		 @RequestParam("BBOX") String bbox,
                       		 @RequestParam("WIDTH") int width,
                       		 @RequestParam("HEIGHT") int height,
                       		 @RequestParam(value = "SRS", required = false) String srs,
                       		 @RequestParam(value = "FORMAT", required = false) String format)
            throws Exception {

        if (format == null) {
            format = "image/png";
        }
        
        response.setContentType(format);

        StyleBuilder sb = new StyleBuilder();

        baseStyle =  new BasicPolygonStyle(sb.createFill(new Color(0xdedede)), sb.createStroke());

        String pgRequest = "";

        Class.forName("org.postgresql.Driver");

        Connection c = null;
        c = DriverManager.getConnection("jdbc:postgresql://"+ dbServer + "/" + dbName,
        		dbUser, dbPassword);

        try {
        	ResultSet rs = null ;
 
        	pgRequest = "SELECT ST_AsBinary(geom) FROM spatial_dimensions WHERE dimension_name = ?";

    		PreparedStatement prepStmt = c.prepareStatement(pgRequest);

    		prepStmt.setString(1, "[NUTS].[NUTS LEVEL 0]");

    		rs = prepStmt.executeQuery();


    		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection();

    		while (rs.next())
    		{
    			WKBReader wkbr = new WKBReader() ;

    			
    			Geometry geom = wkbr.read(rs.getBytes(1));
    			
    			SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    			builder.add("the_geom", Geometry.class);
    			builder.add("id",String.class);
    			builder.add("data", String.class);
    			builder.add("search", String.class);
    			builder.setName("baselayer");
    			
    			SimpleFeatureType bld = builder.buildFeatureType();

    			Object[] values = {geom};
    			
    			SimpleFeature sf = SimpleFeatureBuilder.build(bld, values, "featureId");
    			collection.add(sf);
    			// previously executed query should return only one column
    			break;
    		}
        	ReferencedEnvelope bounds = ReferenceEnvelopeFactory.toReferencedEnvelope(bbox,
        			srs == null ? "EPSG:4326" : srs);

        	Rectangle imageSize = new Rectangle(width, height);

        	renderMap(response, collection, bounds, imageSize,
        			parseFormat(format));
        } finally
        {
        	c.close();
        }
        
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
    					   FeatureCollection feat,
                           ReferencedEnvelope bounds, Rectangle imageSize,
                           String format) throws IOException {

    	
        MapLayer[] layers = {new MapLayer(feat,baseStyle)};
        MapContext map = new DefaultMapContext(layers, bounds
                .getCoordinateReferenceSystem());

        try {
            GTRenderer renderer = new StreamingRenderer();

            renderer.setContext(map);
            BufferedImage image = new BufferedImage(imageSize.width,
                    imageSize.height, BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D graphics = image.createGraphics();
            graphics.setBackground(new Color(0xd6e4f1));
            graphics.clearRect(0, 0, imageSize.width, imageSize.height);
            
            
            try {
                if (feat.getSchema().getGeometryDescriptor() == null) {
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
    
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }
    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }
    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }
}
