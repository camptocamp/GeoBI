package com.c2c.controller;

import com.c2c.query.DimensionsQuery;
import com.c2c.query.LevelsQuery;
import com.c2c.query.MeasuresQuery;
import com.c2c.query.MembersQuery;

import org.hsqldb.lib.Iterator;
import org.json.simple.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Hashtable;

@Controller
@RequestMapping("/getcubeproperties")
public class GetCubeProperties extends AbstractQueryingController {


    // default handling
    @RequestMapping(method = RequestMethod.GET, params = {"!requestType"})
    public void getcubeproperties(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        PrintWriter writer = response.getWriter();

        String datasourceHelp = "<li><strong>datasource</strong> - the datasource, a datasource is a logical collection of catalogs</li>";
        String requesttypeHelp = "<li><strong>requestType</strong> - The request type to send to the cube (dimensions, levels, measures or members)</li>";
        String cubeHelp = "<li><strong>cubeName</strong> - the cube name</li>";
        String dimensionNameHelp = "<li><strong>dimensionUniqueName</strong> - the dimension name ; ignored if request is not members</li>";
        String formatHelp = "<li><strong>format</strong> - the expected output format</li>";
        writer.write("<html><body>");
        writer.write("Parameters: <ul>" + datasourceHelp + requesttypeHelp +
                cubeHelp + dimensionNameHelp + formatHelp
                + "</ul><p>some example queries : </p>"
                + "<p><a href=\"" + "getcubeproperties?requestType=members&cubeName=pg_CLC90_00&dimensionUniqueName=[BIOGEOGRAPHIC REGIONS]" + "\">here (members)</a></p>");

        writer.write("<p><a href=\"" + "getcubeproperties?requestType=measures&cubeName=pg_CLC90_00" + "\">here (measures)</a></p>");

        writer.write("<p><a href=\"" + "getcubeproperties?requestType=levels&cubeName=pg_CLC90_00" + "\">here (levels)</a></p>");

        writer.write("<p><a href=\"" + "getcubeproperties?requestType=dimensions&cubeName=pg_CLC90_00" + "\">here (dimensions)</a></p>");

        writer.write("</body></html>");

        writer.close();
    }

    @RequestMapping(method = RequestMethod.GET)
    public void getcubeproperties(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam("REQUESTTYPE") String requestType,
                                  @RequestParam(value = "CUBENAME", required = false) String cubeName,
                                  @RequestParam(value = "DIMENSIONUNIQUENAME", required = false) String dimensionName,
                                  @RequestParam(value = "FORMAT", required = false) String format) throws IOException {

        response.setContentType("application/json; charset=UTF-8");

        PrintWriter writer = response.getWriter();
        
        if (requestType.equalsIgnoreCase("members")) {
            JSONArray jsRet = executeGetMembers(cubeName, dimensionName);
            writer.write(jsRet.toString());
        } else if (requestType.equalsIgnoreCase("measures")) {
            JSONArray jsRet = executeGetMeasures(cubeName);
            writer.write(jsRet.toString());
        } else if (requestType.equalsIgnoreCase("levels")) {
            JSONArray jsRet = executeGetLevels(cubeName);
            writer.write(jsRet.toString());
        } else if (requestType.equalsIgnoreCase("dimensions")) {
            JSONArray jsRet = executeGetDimensions(cubeName);
            writer.write(jsRet.toString());
        } else {
            throw new IllegalArgumentException("Illegal request type");
        }
    }


    private JSONArray executeGetMembers(String cube, String dimension)
            throws MalformedURLException, IOException {
        MembersQuery qGm = getQueryFactory().createMembersQuery(cube, dimension);

        ResultSet membersSet = qGm.execute();
        try {

            JSONArray jsRet = new JSONArray();

            ResultSetMetaData mdSet = membersSet.getMetaData();
            
            while (membersSet.next()) {
                Hashtable<String, String> membersMap = new Hashtable<String, String>();
                // hardcoded columns :
                // - 9  is MEMBER_NAME
                // - 10 is MEMBER_UNIQUE_NAME
                // - 7  is LEVEL_NAME
                // - 16 is PARENT_UNIQUE_NAME
                membersMap.put(mdSet.getColumnName(9), membersSet.getString(9));
                membersMap.put(mdSet.getColumnName(10), membersSet.getString(10));
                membersMap.put(mdSet.getColumnName(7), membersSet.getString(7));
                membersMap.put(mdSet.getColumnName(16), membersSet.getString(16));
                jsRet.add(membersMap);
            }

            return jsRet;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }

    private JSONArray executeGetMeasures(String cube) {
        MeasuresQuery qGm = getQueryFactory().createMeasuresQuery(cube);

        ResultSet membersSet = qGm.execute();
        try {

            JSONArray jsRet = new JSONArray();

            ResultSetMetaData mdSet = membersSet.getMetaData();

            while (membersSet.next()) {
                Hashtable<String, String> membersMap = new Hashtable<String, String>();
                // hardcoded columns :
                // - 4 is measure_name
                // - 5 is measure_unique_name
                membersMap.put(mdSet.getColumnName(4), membersSet.getString(4));
                membersMap.put(mdSet.getColumnName(5), membersSet.getString(5));
                jsRet.add(membersMap);
            }

            return jsRet;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private JSONArray executeGetLevels(String cube) {
        LevelsQuery qGm = getQueryFactory().createLevelsQuery(cube);

        ResultSet membersSet = qGm.execute();
        try {

            JSONArray jsRet = new JSONArray();

            ResultSetMetaData mdSet = membersSet.getMetaData();

            while (membersSet.next()) {
                Hashtable<String, String> membersMap = new Hashtable<String, String>();
                // hardcoded columns :
                // - 4 is dimension_unique_name
                // - 6 is level_name
                // - 7 is level_unique_name
                // - 10 is level_number
                membersMap.put(mdSet.getColumnName(4), membersSet.getString(4));
                membersMap.put(mdSet.getColumnName(6), membersSet.getString(6));
                membersMap.put(mdSet.getColumnName(7), membersSet.getString(7));
                membersMap.put(mdSet.getColumnName(10), membersSet.getString(10));

                jsRet.add(membersMap);

            }

            return jsRet;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private JSONArray executeGetDimensions(String cube) {
        DimensionsQuery qGm = getQueryFactory().createDimensionsQuery(cube);

        ArrayList<String[]>  membersSet = qGm.execute();
        try {

            JSONArray jsRet = new JSONArray();

//            ResultSetMetaData mdSet = membersSet.getMetaData();
//            int i = 0;
//            while (membersSet.next()) {
//            	if (i == 0)
//            	{
//            		// first result is the measures (same hack as in pentaho's code)
//            		// we skip it (since we have a specific call to get the measures)
//            		i++ ; 
//            		continue ;
//            	}

            	for (java.util.Iterator<String[]> i =  membersSet.iterator() ; i.hasNext(); )
            	{
            	  String[]  curDim = i.next();	
                  Hashtable<String, String> membersMap = new Hashtable<String, String>();
                  membersMap.put("DIMENSION_NAME", curDim[0]);
                  membersMap.put("DIMENSION_UNIQUE_NAME", curDim[1]);
                  membersMap.put("type", curDim[2]);
                  jsRet.add(membersMap);
            		
            	}
            
//                Hashtable<String, String> membersMap = new Hashtable<String, String>();
//                // hardcoded columns :
//                // - 4 is dimension_name
//                // - 5 is dimension_unique_name
//                membersMap.put(mdSet.getColumnName(4), membersSet.getString(4));
//                membersMap.put(mdSet.getColumnName(5), membersSet.getString(5));
//                jsRet.add(membersMap);
//            }

            return jsRet;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
