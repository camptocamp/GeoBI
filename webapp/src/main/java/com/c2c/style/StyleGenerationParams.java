package com.c2c.style;

import com.c2c.controller.GetOverlayIcon;
import com.c2c.controller.Util;

import org.geotools.brewer.color.StyleGenerator;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.visitor.*;
import org.geotools.filter.Expression;
import org.geotools.filter.function.*;
import org.geotools.styling.*;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.geotools.util.NullProgressListener;
import org.omg.CORBA.portable.IndirectionException;
import org.opengis.feature.Feature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static com.c2c.controller.Util.createDefaultStyle;
import static com.c2c.controller.Util.defaultStroke;
import static java.lang.Integer.parseInt;
import static org.geotools.brewer.color.StyleGenerator.ELSEMODE_IGNORE;
import static org.geotools.brewer.color.StyleGenerator.createFeatureTypeStyle;
import static org.geotools.factory.CommonFactoryFinder.*;
import static org.geotools.styling.SLD.featureTypeStyle;

public class StyleGenerationParams {
	
    private final String choroplethsIndicator;
	private final String classificationMethod;

	private final int nbClasses;
    private final Color startColor;
    private final Color endColor;
    private double choroplethsOpacity;
    private final String interpolation;
    private final String overlayType;
    private final String[] overlayIndicators;
    private String sizes;
    private double overlayOpacity;
    private org.geotools.styling.Style gtStyle;
    
	private String queryId ;
	private Util.SYMBOL_TYPE symbolType ;

	private int minOverlaySize ;
	private int maxOverlaySize ;
		
    public String getClassificationMethod() {
		return classificationMethod;
	}

    public String getChoroplethsIndicator() {
		return choroplethsIndicator;
	}
    
	public Util.SYMBOL_TYPE getSymbolType() {
		return symbolType;
	}

	public int getMaxOverlaySize() {		
		return maxOverlaySize;
	}
	
	public int getMinOverlaySize() {
		return minOverlaySize;
	}
	
	public org.geotools.styling.Style getGtStyle(SimpleFeatureCollection results) throws IOException {

        FeatureTypeStyle choropleths = getChoropleths(results);
        Style  style = null ;

    	StyleFactory2 factory = new StyleFactoryImpl();

    	style = factory.createStyle();
    	if (choropleths != null) {    		
    		style.featureTypeStyles().add(choropleths);
    	} else {
    		StyleBuilder styleBuilder = new StyleBuilder();
            Symbolizer symb = styleBuilder.createLineSymbolizer(Color.BLACK, 2);
            style.featureTypeStyles().add(styleBuilder.createFeatureTypeStyle(symb));
    	}
    	
    	if (getOverlayType() != null) {

    		// pie / bar charts
    		if (! getOverlayType().equalsIgnoreCase("symbol")) {
    			
    			FeatureTypeStyle overlays = getOverlays(results);    			
    			if (overlays != null) {    				
    				style.featureTypeStyles().add(overlays);
    			}
    			
    		} else {
    			
    			// proportional symbols
    			FeatureTypeStyle propSymbols = null;
    			try {
    				propSymbols = getProportionalSymbols(results);
    			} catch (Exception e) {
    				throw new RuntimeException(e);
    			}
    			
    			if (propSymbols != null) {
    				style.featureTypeStyles().add(propSymbols);
    			}
    		}
    	}
		
		return style; 
	}
	
	public int getIndicatorsCount() {
		return overlayIndicators.length;
	}
	
	public String[] getOverlayIndicators() {
		return overlayIndicators;
	}
	
	public boolean isChoroplethsEnabled() {
		return ! (choroplethsIndicator == null);
	}
	
	public boolean isOverlaySymbolsEnabled() {
		return ! (symbolType == Util.SYMBOL_TYPE.NONE);	
	}
	
	public int getNbClasses() {
			return nbClasses;
	}

	public StyleGenerationParams(String choroplethsIndicator, 
			String classificationMethod, 
			Integer nbClasses, 
			String colors, 
			Double choroplethsOpacity, 
			String interpolation, 
			String overlayType, 
			String overlayIndicators, 
			String sizes, 
			Double overlayOpacity,
			String queryId,
			Util.SYMBOL_TYPE symtype) {

		this.choroplethsIndicator = choroplethsIndicator;
		this.classificationMethod = classificationMethod;

		this.queryId = queryId ;
		this.symbolType = symtype ;

		this.nbClasses = nbClasses == null ? -1 : nbClasses;

		String[] splitColors = colors.split(",");
		startColor = toColor(splitColors[0]);
		endColor = toColor(splitColors[1]);

		this.choroplethsOpacity = choroplethsOpacity == null ? 1.0 : Math.min(1.0, Math.max(0.0,choroplethsOpacity.doubleValue()));
		this.overlayOpacity = overlayOpacity == null ? 1.0 : Math.min(1.0, Math.max(0.0,overlayOpacity.doubleValue()));
		this.interpolation = interpolation;
		this.overlayType = overlayType;
		this.overlayIndicators = overlayIndicators == null ? null : overlayIndicators.split(",");
		if (sizes != null)
		{
			this.sizes = sizes;
			//
			try
			{

				String[] tmpS = sizes.split(",");
				this.minOverlaySize = new Integer(tmpS[0]).intValue();
				this.maxOverlaySize = new Integer(tmpS[1]).intValue();

			} catch (Exception e) 
			{
				// number format exception
				// getting back to default
				this.sizes = "5,20";
				this.minOverlaySize = 5;
				this.maxOverlaySize = 20;
			}
		}
		else
		{
			this.sizes = "5,20";
			this.minOverlaySize = 5;
			this.maxOverlaySize = 20;
		}
	}

	private Color toColor(String splitColor) {
		String[] parts = splitColor.trim().replaceAll("\\s\\s"," ").split(" ");
		final int r = parseInt(parts[0]);
		final int g = parseInt(parts[1]);
		final int b = parseInt(parts[2]);
		return new Color(r, g, b);
	}

	public FeatureTypeStyle getChoropleths(SimpleFeatureCollection results) throws IOException {
		
		if(choroplethsIndicator == null) {
			return featureTypeStyle(createDefaultStyle(), results.getSchema());
		}

		FilterFactory2 filterFactory = getFilterFactory2(GeoTools.getDefaultHints());
		PropertyName expression = filterFactory.property(choroplethsIndicator);

		ClassificationFunction classifierFn;
		// decimalPlaces function must be overridden due to Geotools bug
		if("StandardDeviation".equalsIgnoreCase(classificationMethod)) {
			classifierFn = new StandardDeviationFunction(){
				@Override
				protected int decimalPlaces(double slotWidth) {
					return -1;
				}
			};
		} else if("EqualInterval".equalsIgnoreCase(classificationMethod)) {
			classifierFn = new EqualIntervalFunction(){ 
				@Override
				protected int decimalPlaces(double slotWidth) {
					return -1;
				}
			};
		} else if("UniqueInterval".equalsIgnoreCase(classificationMethod)) {
			classifierFn = new UniqueIntervalFunction(){
				@Override
				protected int decimalPlaces(double slotWidth) {
					return -1;
				}
			};
		} else if("Quantile".equalsIgnoreCase(classificationMethod)) {
			classifierFn = new QuantileFunction(){
				@Override
				protected int decimalPlaces(double slotWidth) {
					return -1;
				}
			};
		} else {
			classifierFn = new StandardDeviationFunction(){
				@Override
				protected int decimalPlaces(double slotWidth) {
					return -1;
				}
			};
		}

		classifierFn.setClasses(nbClasses == -1 ? results.size() : nbClasses);
		classifierFn.setExpression((Expression) expression);
		classifierFn.setName("choropleths");

		Classifier classifier;
		try {
			classifier = (Classifier) classifierFn.evaluate(results);
		} catch (IllegalStateException e) {
			return featureTypeStyle(createDefaultStyle(), results.getSchema());
		}
			
		final GeometryDescriptor geomAtt = results.getSchema().getGeometryDescriptor();
		FeatureTypeStyle fts = createFeatureTypeStyle(classifier, expression, getColors(classifier), "choropleth style", geomAtt,
				ELSEMODE_IGNORE, choroplethsOpacity, defaultStroke());

		// HACK because of a Geotools bug.
		final DuplicatingStyleVisitor setSymbolizerGeomAtt = new DuplicatingStyleVisitor() {
			@Override
			protected Symbolizer copy(Symbolizer symbolizer) {
				final Symbolizer copy = super.copy(symbolizer);
				copy.setGeometryPropertyName(geomAtt.getLocalName());
				return copy;
			}
		};
		fts.accept(setSymbolizerGeomAtt);
		return (FeatureTypeStyle) setSymbolizerGeomAtt.getCopy();
	}

	private Color[] getColors(Classifier classifier) {

		Color[] colors = new Color[classifier.getSize()];

		if (interpolation == null || interpolation.equalsIgnoreCase("RGB"))
		{
			for(float i=0.0f; i<classifier.getSize(); i++) {
				final float r = interpolate(0, i / classifier.getSize());
				final float g = interpolate(1, i / classifier.getSize());
				final float b = interpolate(2, i / classifier.getSize());
				colors[((int) i)] = new Color(r, g, b);
			}
		}
		else if (interpolation.equalsIgnoreCase("HSV"))
		{
			for(float i=0.0f; i<classifier.getSize(); i++) {
				final float r = interpolateHSV(0, i / classifier.getSize());
				final float g = interpolateHSV(1, i / classifier.getSize());
				final float b = interpolateHSV(2, i / classifier.getSize());
				colors[((int) i)] = new Color(r, g, b);
			}           	
		}	

		return colors;
	}

	private float interpolate(int i, float t) {
		float start, end;

		switch (i) {
		case 0 :
			start = startColor.getRed();
			end = endColor.getRed();
			break;
		case 1 :
			start = startColor.getGreen();
			end = endColor.getGreen();
			break;
		default :
			start = startColor.getBlue();
			end = endColor.getBlue();
			break;
		}

		end = end / 255;
		start = start / 255;
		final float interpolatedValue = ((end - start) * t) + start;
		return interpolatedValue;
	}

	private float interpolateHSV(int i, float t) {
		float rStart, rEnd, bStart, bEnd,gStart, gEnd;

		rStart = startColor.getRed();
		rEnd = endColor.getRed();

		gStart = startColor.getGreen();
		gEnd = endColor.getGreen();

		bStart = startColor.getBlue();
		bEnd = endColor.getBlue();

		//    	final float[] toHsv = RgbHsv.RGBtoHSV(R, G, B, HSV)
		//        final float interpolatedValue = ((end - start) * t) + start;

		return 0;
	}


	/* this class has been stolen from http://www.imagingbook.com/index.php?id=59 and adapted 
	 * for the needs of the project
	 * 
	 * Its current code is licensed under LGPL
	 * 
	 * */
	private static class RgbHsv {

		static float[] RGBtoHSV (int R, int G, int B, float[] HSV) {
			// R,G,B in [0,255]
			float H = 0, S = 0, V = 0;
			float cMax = 255.0f;
			int cHi = Math.max(R,Math.max(G,B));	// highest color value
			int cLo = Math.min(R,Math.min(G,B));	// lowest color value
			int cRng = cHi - cLo;				    // color range

			// compute value V
			V = cHi / cMax;

			// compute saturation S
			if (cHi > 0)
				S = (float) cRng / cHi;

			// compute hue H
			if (cRng > 0) {	// hue is defined only for color pixels
				float rr = (float)(cHi - R) / cRng;
				float gg = (float)(cHi - G) / cRng;
				float bb = (float)(cHi - B) / cRng;
				float hh;
				if (R == cHi)                      // r is highest color value
					hh = bb - gg;
				else if (G == cHi)                 // g is highest color value
					hh = rr - bb + 2.0f;
				else                               // b is highest color value
					hh = gg - rr + 4.0f;
				if (hh < 0)
					hh= hh + 6;
				H = hh / 6;
			}

			if (HSV == null)	// create a new HSV array if needed
				HSV = new float[3];
			HSV[0] = H; HSV[1] = S; HSV[2] = V;
			return HSV;
		}

		static int HSVtoRGB (float h, float s, float v) {
			// h,s,v in [0,1]
			float rr = 0, gg = 0, bb = 0;
			float hh = (6 * h) % 6;                 
			int   c1 = (int) hh;                     
			float c2 = hh - c1;
			float x = (1 - s) * v;
			float y = (1 - (s * c2)) * v;
			float z = (1 - (s * (1 - c2))) * v;	
			switch (c1) {
			case 0: rr=v; gg=z; bb=x; break;
			case 1: rr=y; gg=v; bb=x; break;
			case 2: rr=x; gg=v; bb=z; break;
			case 3: rr=x; gg=y; bb=v; break;
			case 4: rr=z; gg=x; bb=v; break;
			case 5: rr=v; gg=x; bb=y; break;
			}
			int N = 256;
			int r = Math.min(Math.round(rr*N),N-1);
			int g = Math.min(Math.round(gg*N),N-1);
			int b = Math.min(Math.round(bb*N),N-1);
			// create int-packed RGB-color:
				int rgb = ((r&0xff)<<16) | ((g&0xff)<<8) | b&0xff; 
				return rgb;
		}

	}

	public FeatureTypeStyle getOverlays(SimpleFeatureCollection results) throws IOException {

		try {
			GetOverlayIcon.ChartType.valueOf(overlayType.toUpperCase());
		} catch (IllegalArgumentException e) {
			// not a recognized type so no overlay
			return null;
		}

		StyleBuilder builder = new StyleBuilder();

		String data = "";
		String dataLabels = "";

		Comparable maxGlobalValue = null;

		for (int i = 0 ; i < overlayIndicators.length ; i++)
		{
			String overlayIndicator = overlayIndicators[i];
            MaxVisitor maxVisitor = new MaxVisitor(overlayIndicator);
            MinVisitor minVisitor = new MinVisitor(overlayIndicator);
            FeatureCalc customCalc = new FeatureCalc() {
                double value = 0;
                CalcResult calc = new AbstractCalcResult(){
                    @Override
                    public Object getValue() {
                        return value;
                    }
                };

                @Override
                public CalcResult getResult() {
                    return calc;
                }

                @Override
                public void visit(Feature feature) {
                    // do some calculation here
                }
            };

            CombinatorialFeatureVisitor.visit(results, minVisitor, maxVisitor, customCalc);

            Comparable maxValue;
            try {
            	maxValue = maxVisitor.getMax();
            } catch (IllegalStateException e) {
            	return null;
            }

			if ((maxGlobalValue == null) || (maxValue.compareTo(maxGlobalValue) > 0))
				maxGlobalValue = maxValue ;

			dataLabels += overlayIndicator;
			data += "${"+overlayIndicator+"}";
			if (i != overlayIndicators.length - 1)
			{  
				data += ",";
				dataLabels += ",";
			}
		}
		//        data += maxGlobalValue;

		// TODO : this value could evolve in the future
		int size = 55; 

		String dynamicGraphicURL = "http://localhost:8080/webbi/getoverlayicon?type="+overlayType+"&width="+size+"&height="+size+"&data="+data+"&labels="+dataLabels;

		ExternalGraphic graphic = builder.createExternalGraphic(dynamicGraphicURL, "image/png");

		PointSymbolizer sym = builder.createPointSymbolizer(builder.createGraphic(graphic, null, null, overlayOpacity, size, 0), "point");
		final FilterFactory ff = builder.getFilterFactory();
		String geomAtt = results.getSchema().getGeometryDescriptor().getLocalName();
		org.opengis.filter.expression.Expression geomExp = ff.function("interiorPoint", ff.property(geomAtt));
		sym.setGeometry(geomExp);
		return builder.createFeatureTypeStyle(sym);
	}

	public String getOverlayType() { return overlayType; }

	public FeatureTypeStyle getProportionalSymbols(SimpleFeatureCollection results) throws Exception {

		// there must be only one overlay indicator
		// in case of proportional symbols
		if (overlayIndicators.length != 1)
		{
			return null;
		}
		StyleBuilder builder = new StyleBuilder();

		String overlayIndicator = overlayIndicators[0];


		MaxVisitor maxVisitor = new MaxVisitor(overlayIndicator);
		results.accepts(maxVisitor, new NullProgressListener());

		MinVisitor minVisitor = new MinVisitor(overlayIndicator);
		results.accepts(minVisitor, new NullProgressListener());

		Comparable maxValue, minValue;
		try {
			maxValue = maxVisitor.getMax();
			minValue = minVisitor.getMin();
		} catch (IllegalStateException e) {
			return null;
		}

		Mark mark= builder.createMark("circle", new Color(0x5858d5), new Color(0xccc), 1);

		final FilterFactory ff = builder.getFilterFactory();

		String[] sizesSplit = sizes.split(",");
		double minPxSize = Double.parseDouble(sizesSplit[0]);
		double maxPxSize = Double.parseDouble(sizesSplit[1]);

		// nonsense, we are swapping the 2 variables
		if (minPxSize > maxPxSize)
		{
			double tmp = minPxSize ;
			minPxSize = maxPxSize ;
			maxPxSize = tmp;
		}

		Double maxDblVal = 20.0; 
		Double minDblVal = 5.0;

		if (maxValue instanceof Number) {
			maxDblVal = ((Number) maxValue).doubleValue();
		}

		if (minValue instanceof Number) {
			minDblVal = ((Number) minValue).doubleValue();
		}
		
		Double a,b;
		
		if(minDblVal == maxDblVal) {
			throw new Exception("Division by zero");
		}
		
		a = (minPxSize - maxPxSize) / (minDblVal - maxDblVal);
		b = maxPxSize - maxDblVal * a ;
	
		// y = a * x + b	(y = size in px, x size in the GT store)
				
		org.opengis.filter.expression.Expression sizeExpression;

		sizeExpression = ff.add(ff.multiply(ff.literal(a), ff.property(overlayIndicator)), ff.literal(b));
		
		Graphic graphic = builder.createGraphic(null, mark, null);
		graphic.setSize(sizeExpression);
		PointSymbolizer sym = builder.createPointSymbolizer(graphic);
		String geomAtt = results.getSchema().getGeometryDescriptor().getLocalName();
		org.opengis.filter.expression.Expression geomExp = ff.function("interiorPoint", ff.property(geomAtt));
		sym.setGeometry(geomExp);
		return builder.createFeatureTypeStyle(sym);
        
    }

	public String getIndicators() {
		String ret = "";
		ArrayList<String> tmp = new ArrayList<String>();
		if (choroplethsIndicator != null)
			tmp.add(choroplethsIndicator);
		
		for (int i = 0 ; i < overlayIndicators.length ; i++)
		{
			if (tmp.contains(overlayIndicators[i]))
				continue;
			else
				tmp.add(overlayIndicators[i]);
		}
		// removing brackets
		ret = tmp.toString().substring(1, tmp.toString().length() - 2).replace(" ", "");
		return ret;
	}
}
