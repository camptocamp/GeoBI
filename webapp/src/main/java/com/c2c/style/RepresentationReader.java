package com.c2c.style;

import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.util.Range;

import java.awt.*;

/**
 * User: jeichar
 * Date: Sep 6, 2010
 * Time: 3:21:14 PM
 */

class RepresentationReader extends AbstractStyleVisitor {
    private final Range range;
    private Color fill;
    private Color outline;

    public RepresentationReader(Range range) {
        this.range = range;
    }

    @Override
    public void visit(Fill fill) {
        this.fill = getRBGColor(fill.getColor().toString());
    }

    @Override
    public void visit(org.geotools.styling.Stroke stroke) {
    	this.outline = getRBGColor(stroke.getColor().toString());
    }

    public Representation getRepresentation() {
        return new Representation(range, fill, outline);
    }
    
    public static Color getRBGColor(String color) {
    	int r = Integer.parseInt(color.substring(1, 3), 16);
    	int g = Integer.parseInt(color.substring(3, 5), 16);
    	int b = Integer.parseInt(color.substring(5, 7), 16);
    	return new Color(r, g, b);
    }
}