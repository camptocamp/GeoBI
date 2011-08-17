package com.c2c.style;

import org.geotools.styling.*;

/**
 * User: jeichar
 * Date: Sep 6, 2010
 * Time: 3:11:02 PM
 */
public abstract class AbstractStyleVisitor implements StyleVisitor {

	@Override
    public void visit(StyledLayerDescriptor sld) {
		// Not implemented
	}

    @Override
    public void visit(NamedLayer layer) {
		// Not implemented
    }

    @Override
    public void visit(UserLayer layer) {
		// Not implemented
    }

    @Override
    public void visit(FeatureTypeConstraint ftc) {
		// Not implemented
    }

    @Override
    public void visit(Style style) {
    	for (FeatureTypeStyle fts : style.featureTypeStyles()) {
    		fts.accept(this);
    	}
    }

    @Override
    public void visit(Rule rule) {
    	for (Symbolizer symbolizer : rule.symbolizers()) {
    		symbolizer.accept(this);
    	}
    }

    @Override
    public void visit(FeatureTypeStyle fts) {
    	for (Rule rule : fts.rules()) {
    		rule.accept(this);
    	}
    }

    @Override
    public void visit(Fill fill) {
		// Not implemented
    }

    @Override
    public void visit(Stroke stroke) {
		// Not implemented
    }

    @Override
    public void visit(Symbolizer sym) {
		// Not implemented
    }

    @Override
    public void visit(PointSymbolizer ps) {
		// Not implemented
    }

    @Override
    public void visit(LineSymbolizer line) {
		// Not implemented
    }

    @Override
    public void visit(PolygonSymbolizer poly) {
		poly.getFill().accept(this);
    }

    @Override
    public void visit(TextSymbolizer text) {
		// Not implemented
    }

    @Override
    public void visit(RasterSymbolizer raster) {
		// Not implemented
    }

    @Override
    public void visit(Graphic gr) {
		// Not implemented
    }

    @Override
    public void visit(Mark mark) {
		// Not implemented
    }

    @Override
    public void visit(ExternalGraphic exgr) {
		// Not implemented
    }

    @Override
    public void visit(PointPlacement pp) {
		// Not implemented
    }

    @Override
    public void visit(AnchorPoint ap) {
		// Not implemented
    }

    @Override
    public void visit(Displacement dis) {
		// Not implemented
    }

    @Override
    public void visit(LinePlacement lp) {
		// Not implemented
    }

    @Override
    public void visit(Halo halo) {
		// Not implemented
    }

    @Override
    public void visit(ColorMap colorMap) {
		// Not implemented
    }

    @Override
    public void visit(ColorMapEntry colorMapEntry) {
		// Not implemented
    }

    @Override
    public void visit(ContrastEnhancement contrastEnhancement) {
		// Not implemented
    }

    @Override
    public void visit(ImageOutline outline) {
		// Not implemented
    }

    @Override
    public void visit(ChannelSelection cs) {
		// Not implemented
    }

    @Override
    public void visit(OverlapBehavior ob) {
		// Not implemented
    }

    @Override
    public void visit(SelectedChannelType sct) {
		// Not implemented
    }

    @Override
    public void visit(ShadedRelief sr) {
		// Not implemented
    }
}
