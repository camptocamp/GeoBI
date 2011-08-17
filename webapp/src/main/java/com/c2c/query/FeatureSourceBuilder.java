package com.c2c.query;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;

/**
 * Takes the simplified {@link DataAttribute} and {@link DataAttributeDef} data
 * contained in a {@link DataQueryResults} object and converts them into a
 * FeatureSource which is used to render the features and perform the other
 * actions required by the controllers
 *
 * @author jeichar
 */
public class FeatureSourceBuilder {

    /**
     * Entry point method. Takes the simplified {@link DataAttribute} and
     * {@link DataAttributeDef} data contained in a {@link DataQueryResults} object and
     * converts them into a FeatureSource which is used to render the features
     * and perform the other actions required by the controllers
     */
    public SimpleFeatureSource createFeatureStore(
            final DataQueryResults parser) throws IOException {
        SimpleFeatureStore featureSource = createFeatureStore(parser
                .getFeatureTypeSpec(), parser.defaultGeom());

        // TODO A new FeatureCollection should not be created there should
        // be a wrapper class that returns features as they are asked for
        // That will increase performance and reduce memory

        SimpleFeatureCollection coll = FeatureCollections
                .newCollection();
        int i = 1;
        for (Iterable<DataAttribute> spec : parser) {
            coll.add(toFeature(i, featureSource.getSchema(), spec));
            i++;
        }
        featureSource.addFeatures(coll);

        return featureSource;
    }

    private SimpleFeature toFeature(int i, SimpleFeatureType simpleFeatureType,
                                    Iterable<DataAttribute> spec) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(
                simpleFeatureType);
        for (DataAttribute e : spec) {
            builder.set(e.name(), e.value());
        }
        return builder.buildFeature("result-" + i);
    }

    private SimpleFeatureStore createFeatureStore(Iterable<DataAttributeDef> featureTypeSpec, String defaultGeom) throws IOException {
        SimpleFeatureTypeBuilder ftBuilder = new SimpleFeatureTypeBuilder();
        String ftName = "results";
        ftBuilder.setName(ftName);
        for (DataAttributeDef entry : featureTypeSpec) {
            ftBuilder.add(entry.name(), entry.value());
        }

        ftBuilder.setDefaultGeometry(defaultGeom);

        MemoryDataStore ds = new MemoryDataStore(ftBuilder.buildFeatureType());

        return (SimpleFeatureStore) ds
                .getFeatureSource(ftName);
    }

}
    