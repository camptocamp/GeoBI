package com.c2c.style;

import org.geotools.factory.Hints;
import org.geotools.filter.expression.PropertyAccessor;
import org.geotools.filter.expression.PropertyAccessorFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * User: jeichar
 * Date: Jul 9, 2010
 * Time: 1:36:21 PM
 */
public class OlapPropertyAccessorFactory implements
        PropertyAccessorFactory {
    private static final OlapPropertyAccessor ACCESSOR = new OlapPropertyAccessor();

    @Override
    public PropertyAccessor createPropertyAccessor(Class type, String xpath,
            Class target, Hints hints) {
        if(canHandle(type,xpath)) {
            return ACCESSOR;
        }
        return null; // not a olap property
    }

    static boolean canHandle(Class type, String xpath) {
        return xpath != null && (SimpleFeature.class.isAssignableFrom(type) || SimpleFeatureType.class.isAssignableFrom(type)) &&
                xpath.startsWith("{[") && xpath.endsWith("]}");
    }

    private static class OlapPropertyAccessor implements PropertyAccessor {
        @Override
        public boolean canHandle(Object obj, String xpath, Class target) {
            return OlapPropertyAccessorFactory.canHandle(obj.getClass(), xpath);  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object get(Object obj, String xpath, Class target) throws IllegalArgumentException {
            return ((SimpleFeature)obj).getAttribute(xpath);
        }

        @Override
        public void set(Object object, String xpath, Object value, Class target) throws IllegalArgumentException {
            ((SimpleFeature)object).setAttribute(xpath,value);
        }
    }
}
