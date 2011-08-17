package com.c2c.data;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import com.c2c.query.DataQueryResults;
import com.c2c.query.FeatureSourceBuilder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class DataQueryFeatureSource {
	
	private String mdx;
	private List<DataQueryDimension> rows;
	private List<DataQueryDimension> columns;
	private SimpleFeatureSource featureSource;
	
    private final FeatureSourceBuilder featureSourceBuilder = new FeatureSourceBuilder();
	
	public DataQueryFeatureSource(String mdx) {
		this.mdx = mdx;
		this.rows = new ArrayList<DataQueryDimension>();
		this.columns = new ArrayList<DataQueryDimension>();
	}
	
	public Double[] getBoundingBox()
	{
		Double[] ret = new Double[4];
		ret[0] = -175.0; 
		ret[1] = -88.0;
		ret[2] = 175.0; 
		ret[3] = 88.0;
		try {
			ReferencedEnvelope ref =featureSource.getBounds();
			ret[0] = ref.getMinX(); 
			ret[1] = ref.getMinY();
			ret[2] = ref.getMaxX(); 
			ret[3] = ref.getMaxY();
		} catch (IOException e) {
			e.printStackTrace();

			return ret;
		}
		return ret;
	}
	
	public void addMember(String type,
					      String dimensionUniqueName,
					      String levelUniqueName,
					      String memberName,
					      String memberUniqueName) {
		List<DataQueryDimension> dimensionsList = null;
		if (type.equalsIgnoreCase("rows")) {
			dimensionsList = rows;
		} else {
			dimensionsList = columns;			
		}
		for (DataQueryDimension d : dimensionsList) {
			if (d.getUniqueName().equals(dimensionUniqueName)) {
				d.addMember(levelUniqueName, memberName, memberUniqueName);
				return;
			}
		}
		// Not found, new dimension
		DataQueryDimension newDimension = new DataQueryDimension(dimensionUniqueName);
		newDimension.addMember(levelUniqueName, memberName, memberUniqueName);
		dimensionsList.add(newDimension);
	}	
	
	public void buildFeatureSource(DataQueryResults results) throws IOException {
		featureSource = featureSourceBuilder.createFeatureStore(results);
	}
	
	public List<DataQueryDimension> getRows() {
		return rows;
	}

	public List<DataQueryDimension> getColumns() {
		return columns;
	}
	
	public SimpleFeatureSource getFeatureSource() {
		return featureSource;
	}

	public String getMdx() {
		return mdx;
	}
}
