package com.c2c.cache;

public class CacheMissException extends RuntimeException {

	private static final long serialVersionUID = 6484651370036886728L;
    public final String id;
    public final String type;


    public CacheMissException(String id,String type) {
		super(id+" is no in "+type);
        this.id = id;
        this.type = type;
	}
}
