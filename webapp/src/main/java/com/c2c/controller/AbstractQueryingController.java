package com.c2c.controller;

import com.c2c.cache.BiCache;
import com.c2c.cache.CacheMissException;
import com.c2c.query.QueryFactory;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Abstract class for all controllers that to make queries to the backing OLAP
 * server.
 * <p/>
 * This class handles making the request, caching the results and obtaining the
 * cached results if necessary.
 * <p/>
 * Only one query is cached per user (session).
 *
 * @author jeichar
 */
public abstract class AbstractQueryingController {

	private BiCache cache; 
	
    /**
     * The object that makes the queries
     */
    private QueryFactory queryFactory;

    public QueryFactory getQueryFactory() {
        return queryFactory;
    }

    /* setter method used by spring dependency injection */

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    public BiCache getCache() {
		return cache;
	}
    
    public void setCache(BiCache cache) {
		this.cache = cache;
	}

    @ExceptionHandler(CacheMissException.class)
    public String handleException(CacheMissException e, HttpServletResponse response) throws IOException {
        JSONObject object = new JSONObject();
        object.put("id", e.id);
        object.put("type",e.type);
        String msg = object.toJSONString();
        response.sendError(409,msg);
        return msg;
    }
}
