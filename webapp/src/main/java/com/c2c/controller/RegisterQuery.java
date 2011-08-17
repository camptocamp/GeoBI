package com.c2c.controller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.c2c.data.DataQueryFeatureSource;
import com.c2c.query.DataQuery;

/**
 * Performs the query and constructs the style
 * <p/>
 * User: jeichar
 * Date: Jul 2, 2010
 * Time: 3:59:48 PM
 */
@Controller
@RequestMapping("/registerquery")
public class RegisterQuery extends AbstractQueryingController {

    @RequestMapping(method = RequestMethod.POST)
    public void registerquery(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "QUERY", required = false) String query)
            throws Exception {

        if (query == null) {
        	throw new IllegalArgumentException("Parameters must be posted");
        }

        DataQuery dataQuery = getQueryFactory().createDataQuery(query);
        DataQueryFeatureSource results = (DataQueryFeatureSource) dataQuery.execute();
        String id = getCache().putResults(results);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.append("{");
        out.append("\"id\": \"");
        out.append(id);
        out.append("\"}");
    }
}
