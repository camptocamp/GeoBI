package com.c2c.query;

import mondrian.olap.Cube;

import org.hsqldb.lib.Iterator;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;




public class DimensionsQuery extends AbstractQuery<ArrayList<String[]> > {
    private final String cube;
    
    public DimensionsQuery(String jdbcConnection, String catalogDefFile, String cube) {
        super(jdbcConnection, catalogDefFile);
        this.cube = cube;
    }

//   
    
    private static enum DIMENSION_TYPE {TYPE_MEASURE, TYPE_SPATIAL,TYPE_THEMATIC } ;
    private static final int DIMENSION_NAME_COLUMN = 4;
    private static final int DIMENSION_UNIQUE_NAME_COLUMN = 5;
    
    private DIMENSION_TYPE getDimensionType(OlapConnection olapConnection, String dimName)
    {

    	try {
    		org.olap4j.metadata.Cube c = olapConnection.getSchema().getCubes().get(cube);
    		List<Dimension> lDims = c.getDimensions();

    		for (java.util.Iterator<Dimension> i = lDims.iterator() ; i.hasNext() ; )
    		{
    			Dimension cDim = i.next();
    			
    			

    			// not the good dimension
    			// we skip this iteration
    			
    			if (! dimName.equals(cDim.getName()))
    			{
    				continue;
    			}
    			if (cDim.getDimensionType() ==  Dimension.Type.MEASURE)
    			{
    				return DIMENSION_TYPE.TYPE_MEASURE;
    			}
    			// first hierarchy
    			List <Hierarchy> lHies = cDim.getHierarchies();

    			for (java.util.Iterator<Hierarchy> j = lHies.iterator() ; j.hasNext() ; )
    			{
    				Hierarchy lHie = j.next();
    				
    				List<Level> lLvls = lHie.getLevels();
    				// we just need to iterate on at most 2 levels
    				int nbLvls = 0;
    				
    				for (java.util.Iterator<Level> k = lLvls.iterator() ; k.hasNext() ; )
    				{
    					Level cLvl = k.next();
    					nbLvls++;
    					
    					List<Member> lMbrs;
    					lMbrs = cLvl.getMembers();
    					// iterating on members
    					for (java.util.Iterator<Member> l = lMbrs.iterator() ; l.hasNext() ; )
    					{
    						Member cMbr = l.next();

    						List<Property> lPrps = cMbr.getProperties();
    						// iterating on properties
    						for  (java.util.Iterator<Property> m = lPrps.iterator() ; m.hasNext() ; )
    						{
    							Property cPrp = m.next();

    							if (cMbr.getPropertyValue(cPrp) != null)
    							{
    								if ((cMbr.getPropertyValue(cPrp) instanceof com.vividsolutions.jts.geom.MultiPolygon) ||
    										(cMbr.getPropertyValue(cPrp) instanceof com.vividsolutions.jts.geom.Point))
    								{
    									return DIMENSION_TYPE.TYPE_SPATIAL;
    								}
    							}
    						} // end iterating on the properties
    						// let's break after the first member
    						break;
    					} // end iterating on members
    					
    					// we need to iterate at least 2 times on levels
    					// since the dummy level "All lvlName" could potentially
    					// pollute our properties lookup
    					
    					if (nbLvls == 2)
    					{
    						break;
    					}
    					
    				} // end iterating on levels
    				break;
    				
    			} // end iterating on hierarchies
    			break;
    			
    		} // end iterating on dimensions


    	} catch (OlapException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	return DIMENSION_TYPE.TYPE_THEMATIC;
    }
    
    
    
    @Override
    protected synchronized ArrayList<String[]> doExecute(OlapConnection olapConnection) throws OlapException {
    	ArrayList<String[]> ret = new ArrayList<String[]>();

        try {
        	int i = 0;
        	OlapDatabaseMetaData md = olapConnection.getMetaData();
        	ResultSet propMd = md.getDimensions(null, null, cube, null);
            
		
        	while (propMd.next()) {
        		 
        		DIMENSION_TYPE type = getDimensionType(olapConnection, propMd.getString(DIMENSION_NAME_COLUMN));
                String[] curCol = new String[3];
                
                curCol[0] = new String(propMd.getString(DIMENSION_NAME_COLUMN));
                curCol[1] = new String(propMd.getString(DIMENSION_UNIQUE_NAME_COLUMN));
                if (type == DIMENSION_TYPE.TYPE_SPATIAL) {
                    curCol[2] = "spatial";
                } else if (type == DIMENSION_TYPE.TYPE_THEMATIC) {
                    curCol[2] = "thematic";
                } else if (type == DIMENSION_TYPE.TYPE_MEASURE) {
                    curCol[2] = "measure";
                }
                ret.add(curCol);
        	}

        } catch (SQLException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
        return ret;
       // return md.getDimensions(null, null, cube, null);
    }


}
