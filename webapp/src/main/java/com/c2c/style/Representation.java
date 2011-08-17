package com.c2c.style;

import org.geotools.util.Range;

import java.awt.*;

/**
 * User: jeichar
 * Date: Sep 6, 2010
 * Time: 3:17:49 PM
 */
public class Representation implements Comparable<Representation> {
    private final Range range;
    private final Color fill;
    private final Color outline;

    public Representation(Range range, Color fill, Color outline) {
        this.range = range;
        this.fill = fill;
        this.outline = outline;
    }

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(Representation r) {
		return this.range.getMinValue().compareTo(r.range.getMinValue());
	}
	
	public Range getRange() {
		return range;
	}
	
	public Color getFill() {
		return fill;
	}
	
	public Color getOutline() {
		return outline;
	}
}
