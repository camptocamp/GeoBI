package com.c2c.style;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.visitor.CalcResult;
import org.geotools.feature.visitor.FeatureCalc;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import java.io.IOException;
import java.util.*;

/**
 * Visitor for performing multiple calculations in a single pass
 */
public class CombinatorialFeatureVisitor implements FeatureVisitor {
    private final List<FeatureCalc> calculations;

    public CombinatorialFeatureVisitor(List<FeatureCalc> calculations) {
        this.calculations = new ArrayList<FeatureCalc>(calculations);
    }

    @Override
    public void visit(Feature feature) {
        for (FeatureCalc calculation : calculations) {
            calculation.visit(feature);
        }
    }

    public static void visit(SimpleFeatureCollection features, FeatureCalc... calculations) throws IOException {
        CombinatorialFeatureVisitor visitor = new CombinatorialFeatureVisitor(Arrays.asList(calculations));
        features.accepts(visitor, new NullProgressListener());
    }
}
