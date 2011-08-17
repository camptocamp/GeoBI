package com.c2c.query;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.c2c.data.DataQueryFeatureSource;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.FeatureSource;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Makes the OLAP request and builds a {@link FeatureSource} from the
 * results using the {@link FeatureSourceBuilder}
 *
 * @author jeichar
 */
public class DataQuery extends AbstractQuery<DataQueryFeatureSource> {

    private final String mdx;

    public DataQuery(String jdbcConnection, String catalogDefFile, String mdx) {
        super(jdbcConnection, catalogDefFile);
        this.mdx = mdx;
    }

    public static String getUniqueId(String longName) {
    	MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	digest.update(longName.getBytes());
    	StringBuffer uniqueId = new StringBuffer();
    	for (byte b : digest.digest()) {
    		int i1 = (0xff & b);
    		int i2 = (int) i1 / 16;
    		uniqueId.append((char) (i2 + 65));
    		uniqueId.append((char) (i1 - (i2 * 16) + 65));
    	}
      	return uniqueId.toString();
    }
    
    private String constructAttributeName(List<Member> members) {		
		StringBuilder dataIndex = new StringBuilder();
    	for (Member m : members) {
    		if (dataIndex.length() > 0) {
    			dataIndex.append("_");
    		}
    		dataIndex.append(getUniqueId((String) m.getPropertyValue(Property.StandardMemberProperty.MEMBER_UNIQUE_NAME)));
    	}
    	return dataIndex.toString();
    }
    
    private void addMembers(DataQueryFeatureSource dataQueryFeatures, String type, List<Member> members) {
    	for (Member m : members) {
        	String dimensionUniqueName = (String) m.getPropertyValue(Property.StandardMemberProperty.DIMENSION_UNIQUE_NAME);
        	String levelUniqueName = (String) m.getPropertyValue(Property.StandardMemberProperty.LEVEL_UNIQUE_NAME);
        	String memberName = (String) m.getPropertyValue(Property.StandardMemberProperty.MEMBER_NAME);
        	String memberUniqueName = (String) m.getPropertyValue(Property.StandardMemberProperty.MEMBER_UNIQUE_NAME);
    		dataQueryFeatures.addMember(type, dimensionUniqueName, levelUniqueName, memberName, memberUniqueName);
    	}
    }
    
    @Override
    protected DataQueryFeatureSource doExecute(OlapConnection connection) throws Exception {

    	OlapStatement statement = connection.createStatement();
        CellSet cellSet = statement.executeOlapQuery(mdx);
        
        DataQueryFeatureSource dataQueryFeatures = new DataQueryFeatureSource(mdx);
        DataQueryResults results = new DataQueryResults();
        
        // FIXME: Member names should be returned by GetMetadata only
        // results.addSpec(new DataAttributeDef("row_data_index", String.class));
        for (Position row : cellSet.getAxes().get(1)) {
        	for (Member m : row.getMembers()) {
        		String dimensionName = (String) m.getPropertyValue(Property.StandardMemberProperty.DIMENSION_UNIQUE_NAME);
        		results.addSpec(new DataAttributeDef(dimensionName, String.class));
        	}
        	break;
        }
        for (Position column : cellSet.getAxes().get(0)) {
        	addMembers(dataQueryFeatures, "columns", column.getMembers());
        	results.addSpec(new DataAttributeDef(constructAttributeName(column.getMembers()), Double.class));
        }

        results.addSpec(new DataAttributeDef("geom", MultiPolygon.class));
        results.setDefaultGeomAttName("geom");
        results.addSpec(new DataAttributeDef("point", Point.class));

        List<DataAttribute> feature;
        
        for (Position row : cellSet.getAxes().get(1)) {
        	feature = new ArrayList<DataAttribute>();
            Geometry geom = null;
            Geometry point = null;
        	for (Member m : row.getMembers()) {
            	for (Property p : m.getProperties()) {
                    final String name = p.getName();
                    if (name.equals("geom")) {
                        Geometry g = (Geometry) m.getPropertyValue(p);
                        if (geom == null) {
                            geom = g;
                        } else {
                            geom = geom.intersection(g);
                        }
                    }
                    if (name.equals("pointgeom")) {
                        if (point == null) {
                            point = (Point) m.getPropertyValue(p);
                        }
                    }
            	}
            	String dimensionName = (String) m.getPropertyValue(Property.StandardMemberProperty.DIMENSION_UNIQUE_NAME);
            	String memberName = (String) m.getPropertyValue(Property.StandardMemberProperty.MEMBER_NAME);
            	feature.add(new DataAttribute(dimensionName, memberName));        	
        	}
            for (Position column : cellSet.getAxes().get(0)) {
                final Cell cell = cellSet.getCell(column, row);
              	feature.add(new DataAttribute(constructAttributeName(column.getMembers()),
              			(Double) cell.getValue()));            	
            }
            feature.add(new DataAttribute("geom", geom));
            feature.add(new DataAttribute("point", point));
            results.addData(feature);
            
        	addMembers(dataQueryFeatures, "rows", row.getMembers());
        }
        
        dataQueryFeatures.buildFeatureSource(results);
        return dataQueryFeatures;
    }

}
