package com.c2c.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.visitor.MaxVisitor;
import org.geotools.feature.visitor.MinVisitor;
import org.geotools.styling.Style;
import org.geotools.util.NullProgressListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.c2c.controller.Util.SYMBOL_TYPE;
import com.c2c.data.DataQueryFeatureSource;
import com.c2c.style.LegendDataGatherer;
import com.c2c.style.Representation;
import com.c2c.style.StyleGenerationParams;

/**
 * The Controller for handling compute requests.
 * <p/>
 * This class is registered as a bean in ws-servlet.xml.
 *
 * @author yves, pmauduit
 */
@Controller
@RequestMapping("/getlegend")
public class GetLegend extends AbstractQueryingController {

	/**
	 * padding percentage factor at both sides of the legend.
	 */
	public static final float hpaddingFactor = 0.15f;
	/**
	 * top & bottom padding percentage factor for the legend
	 */
	public static final float vpaddingFactor = 0.15f;   

	private static final Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 10);
	private static final Font FONT_BOLD   = new Font("SansSerif", Font.BOLD, 10);
	private static final Font FONT_ITALIC = new Font("SansSerif", Font.ITALIC, 10);

	private static final int N_PROP_CLASSES = 4;

	private class LegendSettings
	{
		public int getWidth() {
			return width;
		}


		public int getHeight() {
			return height;
		}

		public BufferedImage getChartSymbol() {
			return chartSymbol;
		}

		public BufferedImage getOverlayLegend() {
			return overlayLegend;
		}

		public String getChoropletIndicator() {
			return choropletIndicator;
		}

		public String getSymbolIndicator() {
			return symbolIndicator;
		}

		private final int width ;
		private final int height ;
		private final BufferedImage chartSymbol;
		private final BufferedImage overlayLegend;
		private final String choropletIndicator;
		private final String symbolIndicator;

		public LegendSettings(int _width, int _height,
				BufferedImage _chartSym, BufferedImage _overlayLgd,
				String _choropletIndicator, String _symbolIndicator)
		{
			width = _width;
			height = _height;
			chartSymbol = _chartSym;
			overlayLegend = _overlayLgd;
			choropletIndicator = _choropletIndicator;
			symbolIndicator = _symbolIndicator;
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public void getlegend(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("QUERYID") String queryId,
			@RequestParam("STYLEID") String styleId,
			@RequestParam(value = "FORMAT", required = false) String format,
			@RequestParam(value = "FORMAT_OPTIONS", required = false) String format_options)
	throws Exception {

		if (format == null) {
			format = "image/png";
		}
		DataQueryFeatureSource rs = getCache().getResults(queryId);
		SimpleFeatureSource results = (SimpleFeatureSource) rs.getFeatureSource();

		response.setContentType(format);
		response.addHeader("Cache-Control", "no-cache");
		response.addHeader("Expires", "-1");

		StyleGenerationParams sld = getCache().getStyle(styleId);

		int dpi = 90;
		if (format_options != null) {
			dpi = Util.getDpiFromFormat(format_options);
		}

		LegendSettings lgds = prepareLegendSize(rs, sld, dpi);

		int width = lgds.getWidth();
		int height = lgds.getHeight();        
		Rectangle imageSize = new Rectangle(width, height);

		renderLegend(response, results, imageSize, sld, lgds, parseFormat(format), dpi);
	}

	private String parseFormat(String format) {
		if (format == null) {
			return "png";
		}
		String[] parts = format.split("/");
		if (parts.length == 1) {
			return format;
		}
		return parts[1];
	}

	private void renderLegend(HttpServletResponse response,
			SimpleFeatureSource results,
			Rectangle imageSize, StyleGenerationParams sld,
			LegendSettings lgds,
			String format,
			int dpi) throws Exception {

		double ratio = (double) dpi/90;
		BufferedImage image = new BufferedImage((int) (imageSize.width * ratio),
				(int) (imageSize.height * ratio), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();        
		graphics.scale(ratio, ratio);

		Style sld2 = sld.getGtStyle(results.getFeatures());
		LegendDataGatherer legendDataGatherer = new LegendDataGatherer();
		sld2.accept(legendDataGatherer);
		paint(graphics, legendDataGatherer.getRepresentations(), sld, results, lgds);

		ServletOutputStream output = response.getOutputStream();
		try {
			ImageIO.write(image, format, output);
		} finally {
			output.close();
			graphics.dispose();
		}
	}

	private LegendSettings prepareLegendSize(DataQueryFeatureSource rs, StyleGenerationParams sld, int dpi) {
		int height = 1;
		int width = 250;
		BufferedImage chartSymbol = null;
		BufferedImage overlayLegend = null;
		String choropletIndicator = null;
		String symbolIndicator = null;

		//  choropleth
		if (sld.isChoroplethsEnabled())
		{
			if (sld.getNbClasses() > 0) {
				//height += (48 + (30 + 5) * sld.getNbClasses());
				// 25 px by nbclasses / number of results in the resultset
				height += (25 * sld.getNbClasses());
			} else

			{
				/* count each lines then 10 * nblines */
				try{
					int nbRows = rs.getFeatureSource().getFeatures().size();
					height += (25 * nbRows);

				} catch (IOException e) {
					// Unable to get the number of rows of 
					// our resultset. Considering we got 5 lines.
					height += (25 * 5);
				}

			}
			String [] indicators = {sld.getChoroplethsIndicator()};
			choropletIndicator = Util.getHumanReadableIndicators(rs, indicators)[0];
		}
		if (sld.isOverlaySymbolsEnabled())
		{
			// case 1 : prop. symbols ; no idea on how much
			// it would take ; let's say 10px margin top / bottom = max symbol size
			if (sld.getSymbolType() == Util.SYMBOL_TYPE.PROPORTIONAL_SYMBOLS)
			{
				height += (42 + sld.getMaxOverlaySize() + N_PROP_CLASSES * (sld.getMaxOverlaySize() + 10));

				String [] indicators = sld.getOverlayIndicators();
				symbolIndicator = Util.getHumanReadableIndicators(rs, indicators)[0];
			}

			// case 2 : pies or bars
			// we are then prefetching the images we are going to use

			if ((sld.getSymbolType() == SYMBOL_TYPE.BARS) || (sld.getSymbolType() == SYMBOL_TYPE.PIES))
			{
				symbolIndicator = Util.getSymbolsDimensions(rs);
				String type = sld.getSymbolType() == SYMBOL_TYPE.BARS ? "bar" : "pie";

				String data = "";
				// Create a URL for the image's location
				for (int i = 0 ; i < sld.getIndicatorsCount(); i++)
				{
					if (i==0)
					{
						data += String.format("%d", i+1);
					}
					else
					{
						data += String.format(",%d", i+1);
					}
				}
				try {

					int size = 55;
					URL url = new URL("http://localhost:8080/webbi/getoverlayicon?type=" + type +
							"&width=" + size + "&height=" + size + "&data="+data+"&legend=false"); 
					URLConnection urlConn = url.openConnection();

					// first "symbol"
					chartSymbol = ImageIO.read(urlConn.getInputStream());

					int img_w = chartSymbol.getWidth();
					int img_h = chartSymbol.getHeight();

					height += (12 + img_h);
					if (width < img_w) {
						width = img_w;
					}
					// second one (actually the legend, generated by JFreeCharts)
					String [] lbls = sld.getOverlayIndicators();
					lbls = Util.getHumanReadableIndicators(rs, sld.getOverlayIndicators());
					String labels = "";
					for (int i	=0 ; i < lbls.length; i++) {
						if (i == 0)
							labels += lbls[i];
						else
							labels += "," + lbls[i];						
					}
					labels = URLEncoder.encode(labels, "UTF-8");
					url = new URL("http://localhost:8080/webbi/getoverlayicon?type="+type+"&width="+width+"&height=300&data="+data+"&labels="+labels+"&legend=true"); 
					urlConn = url.openConnection();
					// draw it on the legend
					overlayLegend = ImageIO.read(urlConn.getInputStream());

					img_w = overlayLegend.getWidth();
					img_h = overlayLegend.getHeight();

					height += (12 + img_h);

					if (width < img_w)
					{
						width = img_w;
					}
					// we are losing 12 + 24 px somehow (by reading the paint() method)
					// adding a "security" margin of 12px.
					height += 48 ;


				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else{
			/* 250px ought to be enough */
			width = 250;
		}

		return new LegendSettings(width, height, chartSymbol, overlayLegend,
				choropletIndicator, symbolIndicator);
	}

	private void paint(Graphics2D graphics, Collection<Representation> representations, StyleGenerationParams sld,
			SimpleFeatureSource results, LegendSettings lgds) throws IOException {

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		graphics.setColor(Color.WHITE);
		graphics.setFont(FONT_NORMAL);
		graphics.setColor(Color.BLACK);

		int cur_v = 0, cur_h = 10; //position of current item in legend 
		int box_w = 30, box_h = 20; //size of choropleth legend box in pixels

		int width = lgds.getWidth();
		BufferedImage chartSymbol = lgds.getChartSymbol();
		BufferedImage overlayLegend = lgds.getOverlayLegend();

		if (sld.isChoroplethsEnabled())
		{

			graphics.setFont(FONT_BOLD);
			graphics.drawString("Choroplets: ", cur_h, cur_v + 15);
			graphics.drawString(lgds.getChoropletIndicator(), cur_h, cur_v + 27);
			graphics.setFont(FONT_NORMAL);
			cur_v += 37;

			for (Representation r : representations) {

				graphics.setPaint(r.getFill());
				graphics.fill(new RoundRectangle2D.Double(cur_h, cur_v, box_w,
						box_h, 5, 5));
				graphics.setColor(r.getOutline());
				graphics.draw(new RoundRectangle2D.Double(cur_h, cur_v, box_w,
						box_h, 5, 5));
				String text;
				if ("UniqueInterval".equals(sld.getClassificationMethod()))
					text = String.format("%.1f", r.getRange().getMinValue());
				else
					text = String.format("%.1f %s %.1f", r.getRange().getMinValue(), " to ", r.getRange().getMaxValue());

				graphics.setColor(Color.BLACK);	
				graphics.drawString(text, cur_h + box_w + 5, cur_v + box_h - 6);
				cur_v += (box_h + 5);

			}
		}
		if (sld.isOverlaySymbolsEnabled())
		{
			if (sld.getSymbolType() == SYMBOL_TYPE.PROPORTIONAL_SYMBOLS)
			{
				graphics.setFont(FONT_BOLD);
				graphics.drawString("Proportional symbols: ", cur_h, cur_v + box_h + 6);
				graphics.drawString(lgds.getSymbolIndicator(), cur_h, cur_v + box_h + 18);
				graphics.setFont(FONT_NORMAL);
				cur_v += 48;
				Color fillColor = new Color(0x5858d5);
				Color drawColor = new Color(0xccc);

				int minSize = sld.getMinOverlaySize();
				int maxSize = sld.getMaxOverlaySize();

				double minValue, maxValue;
				MaxVisitor maxVisitor = new MaxVisitor(sld.getOverlayIndicators()[0]);
				results.getFeatures().accepts(maxVisitor, new NullProgressListener());
				MinVisitor minVisitor = new MinVisitor(sld.getOverlayIndicators()[0]);
				results.getFeatures().accepts(minVisitor, new NullProgressListener());
				try {
					maxValue = ((Number)maxVisitor.getMax()).doubleValue();
					minValue = ((Number)minVisitor.getMin()).doubleValue();
				} catch (IllegalStateException e) {				
					return;
				} 

				double incSize = (maxSize - minSize) / (N_PROP_CLASSES - 1);
				double incValue = (maxValue - minValue) / (N_PROP_CLASSES - 1);
				for (int i = 0; i < N_PROP_CLASSES; i++) {
					int size = (int) (maxSize - incSize * i);
					double value = maxValue - incValue * i;
					int offset = (maxSize - size) / 2;
					graphics.setColor(fillColor);
					graphics.fillArc(cur_h + offset, cur_v + i * 25 + offset, size, size, 0, 360);  		
					graphics.setColor(drawColor);
					graphics.drawArc(cur_h + offset, cur_v + i * 25 + offset, size, size, 0, 360);
					graphics.setColor(Color.BLACK);					
					graphics.drawString(String.format("%.1f", value), cur_h + maxSize + 6, cur_v + i * 25 + maxSize / 2 + 4);
				}			    	
			}

			else if ((sld.getSymbolType() == SYMBOL_TYPE.PIES) || (sld.getSymbolType() == SYMBOL_TYPE.BARS))
			{
				graphics.setFont(FONT_BOLD);
				cur_v += (box_h + 6);

				graphics.drawString("Symbols legend: ", cur_h, cur_v);
				graphics.drawString(lgds.getSymbolIndicator(), cur_h, cur_v + 12);
				graphics.setFont(FONT_NORMAL);

				cur_v +=  24;

				if (chartSymbol != null)
				{
					graphics.drawImage(chartSymbol, cur_h, cur_v, chartSymbol.getWidth(), chartSymbol.getHeight(), null);

					cur_v += (chartSymbol.getHeight() + 12);
				}
				if (overlayLegend != null)
				{
					graphics.drawImage(overlayLegend, cur_h, cur_v, overlayLegend.getWidth(), overlayLegend.getHeight(), null);
				}
			}
		}
	}


}
