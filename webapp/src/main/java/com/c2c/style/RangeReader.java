package com.c2c.style;

import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.util.Range;
import org.opengis.filter.*;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 * User: jeichar
 * Date: Sep 6, 2010
 * Time: 3:21:53 PM
 */
class RangeReader extends DefaultFilterVisitor {
	private Double minValue = null;
	private Double maxValue = null;

    @Override
    public Object visit(Literal expression, Object data) {
    	if (minValue == null) {
    		minValue = Double.valueOf(expression.getValue().toString());
    	} else {
    		maxValue = Double.valueOf(expression.getValue().toString());
    	}
    	return super.visit(expression, data);
    }

    public Range getRange() {
        return new Range<Double>(Double.class, minValue, maxValue);
    }
}