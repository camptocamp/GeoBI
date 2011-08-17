package com.c2c.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: yves
 */
public class DataQueryResults implements Iterable<Iterable<DataAttribute>> {

	private List<Iterable<DataAttribute>> data;
	private List<DataAttributeDef> spec;
    private String defaultGeomAttName;

    public DataQueryResults() {
		data = new ArrayList<Iterable<DataAttribute>>();
		spec = new ArrayList<DataAttributeDef>();
	}

	public void addData(Iterable<DataAttribute> data) {
		this.data.add(data);
	}

	public void addSpec(DataAttributeDef spec) {
		this.spec.add(spec);
	}

    public Iterator<Iterable<DataAttribute>> iterator() {
        return this.data.iterator();
    }

    public Iterable<DataAttributeDef> getFeatureTypeSpec() {
    	return this.spec;
    }

    public String defaultGeom() {
        return this.defaultGeomAttName;
    }

    public void setDefaultGeomAttName(String defaultGeomAttName) {
        this.defaultGeomAttName = defaultGeomAttName;
    }
}

class DataAttribute {
    private String _name;
    private Object _value;

    public DataAttribute(String name, Object value) {
        _name = name;
        _value = value;
    }

    public String name() {
        return _name;
    }

    public Object value() {
        return _value;
    }
}

class DataAttributeDef {
    private String _name;
    private Class<?> _value;

    public DataAttributeDef(String name, Class<?> value) {
        _name = name;
        _value = value;
    }

    public String name() {
        return _name;
    }

    public Class<?> value() {
        return _value;
    }

}
