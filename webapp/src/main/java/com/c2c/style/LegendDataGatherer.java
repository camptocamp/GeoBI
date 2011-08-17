package com.c2c.style;

import org.geotools.styling.Rule;
import org.geotools.util.Range;
import org.opengis.filter.Filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * User: jeichar
 * Date: Sep 6, 2010
 * Time: 3:23:04 PM
 */
public class LegendDataGatherer extends AbstractStyleVisitor {
    SortedSet<Representation> representations = new TreeSet<Representation>();

    @Override
    public void visit(Rule rule) {
        RangeReader rangeReader = new RangeReader();
        Filter filter = rule.getFilter();
        if (filter != null) {
        	rule.getFilter().accept(rangeReader, null);
            Range range = rangeReader.getRange();

            RepresentationReader representationReader = new RepresentationReader(range);
            rule.accept(representationReader);
            Representation r = representationReader.getRepresentation();
            if (representations.contains(r)) {
            	representations.remove(r);
            }
            representations.add(r);
        }
    }

    public Collection<Representation> getRepresentations() {
        return Collections.unmodifiableCollection(representations);
    }
}
