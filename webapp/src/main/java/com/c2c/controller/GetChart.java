package com.c2c.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.util.TableOrder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.c2c.data.DataQueryFeatureSource;


/**
 *
 * @author pmauduit, stolen from tbonfort's code from cdc_geodecisionnel project
 */
@Controller
@RequestMapping("/getchart")
public class GetChart  extends AbstractQueryingController {

    // TODO : use colors from a registered style ?
    public static Color[] DEFAULT_COLORS = {
        new Color(141,211,199),
        new Color(255,255,179),
        new Color(190,186,218),
        new Color(251,128,114),
        new Color(128,177,211),
        new Color(253,180,98),
        new Color(179,222,105),
        new Color(252,205, 229)
    };

    private class ChartDataElem {
    	 
    	 private String geographicRegion ;
    	 private ArrayList<String> indicators;
    	 private ArrayList<Double> datas ;
    	 
    	 public ChartDataElem(String reg, Double value)
    	 {
    		 this.geographicRegion = reg;
    		 datas = new ArrayList<Double>();
    		 this.addDatas(value);
    	 }
    	 public ChartDataElem(String reg, String indicator, Double value)
    	 {
    		 this.geographicRegion = reg;
    		 datas = new ArrayList<Double>();
    		 indicators = new ArrayList<String>();
    		 indicators.add(indicator);
    		 this.addDatas(value);
    	 }
    	 public void addDatas(Double value)
    	 {
    		datas.add(value); 
    	 }
    	 public void addDatas(Double value, String indicator)
    	 {
    		datas.add(value); 
    		indicators.add(indicator);
    	 }
    	 public Double[] getDatas()
    	 {
    		 Double[] ret = new Double[datas.size()];
    		 return  datas.toArray(ret);
    	 }
    	 
    	 public String getGeographicRegion() { return geographicRegion; }
    	 public String[] getIndicators() {return indicators.toArray(new String[indicators.size()]);}
     }
    
    private class ChartDatas
    {
    	private ArrayList<ChartDataElem> elements ;
    	
    	public ChartDatas() { elements = new ArrayList<ChartDataElem>() ; }
    	
     	public void addElement(String key, String indicator, Double value)
    	{
    		for (Iterator<ChartDataElem> i = elements.iterator() ; i.hasNext() ; )
    		{
    				ChartDataElem cur = i.next();
    				if (cur.getGeographicRegion().equals(key))
    				{
    					cur.addDatas(value, indicator);
    					return;
    				}
    		}
    		/* else */
    		elements.add(new ChartDataElem(key, indicator, value));
    	}
    	
    	
    	
    	public Double[][] getDatas()
    	{
    		ArrayList<Double[]> ret = new ArrayList<Double[]> ();
    		
    		for (ChartDataElem cur : elements)
    		{
    				ret.add((Double[]) cur.getDatas());
    		}
    		Double[][] ret2 = new Double[elements.size()][];
    		return (Double[][]) ret.toArray(ret2);
    	}
    	
    	public double[][] getdoubleDatas()
    	{
    		Double[][] tmp = getDatas();
    		double[][] ret = new double[tmp.length][];
    		for (int i = 0; i <  tmp.length ; i++)
    		{
				ret[i] = new double[tmp[i].length];

    			for (int j =0 ; j < tmp[i].length; j++)
    			{
    				ret[i][j] = tmp[i][j].doubleValue();
    			}
    			
    		}
    		return ret;
    	}
    	public double[][] getSwappedDoubleDatas()
    	{
    		Double[][] tmp = getDatas();
    		double[][] ret = new double[tmp[0].length][];
    		
    		for (int i = 0; i <  tmp[0].length ; i++) // from 0 to b
    		{
				ret[i] = new double[tmp.length]; // new array of dimension a

    			for (int j =0 ; j < tmp.length; j++) // from 0 to a
    			{
    				ret[i][j] = tmp[j][i].doubleValue();
    			}
    		}
    		return ret;		
    	}
    	public String[] getRowKeys()
    	{
    		String[] ret = new String[elements.size()];
    		for (int i = 0 ; i < ret.length ; i++)
    		{
    			ret[i] = elements.get(i).getGeographicRegion();
    		}
    		return ret;
    	}
    	public String[] getColumnKeys()
    	{
    		return elements.get(0).getIndicators();
    	}
    	public String[] getTranslatedIndicators(DataQueryFeatureSource dqfs)
    	{
    		HashMap<String, String> lkp = Util.lookupIndicators(dqfs);
    		String[] ret = getColumnKeys();
    		for (int i = 0 ; i < ret.length ;  i++)
    		{
    			ret[i] = lkp.get(ret[i]) == null ? ret[i] : lkp.get(ret[i]);
    		}	
    		return ret;
    	}

    }
    
    protected JFreeChart createPieChart(HttpServletRequest request, HttpServletResponse response, 
    								String queryId, CategoryDataset newSet, int width, int height, String format) throws Exception 
    {
        
        final JFreeChart multiChart = ChartFactory.createMultiplePieChart(null,  // chart title
        		newSet,               // dataset
        		TableOrder.BY_ROW,
        		true,                  // include legend
        		true,  // include tooltip
        		false);
        
            final MultiplePiePlot mPlot = (MultiplePiePlot) multiChart.getPlot();
            final JFreeChart subchart = mPlot.getPieChart();
            final PiePlot p = (PiePlot) subchart.getPlot();
            
            p.setSectionOutlinesVisible(false);
            p.setLabelGenerator(null);
            p.setShadowXOffset(0);
            p.setShadowYOffset(0);
            
            subchart.setBorderVisible(false);
            multiChart.setBorderVisible(false);

            p.setBackgroundPaint(new Color(0xd6e4f1));
            subchart.getTitle().setFont(new Font("SansSerif", Font.PLAIN, 12));


            List l = newSet.getColumnKeys();
            for (int i = 0; i < Math.min(newSet.getColumnCount(), 8); i++) {
                p.setSectionPaint((Comparable) l.get(i), GetChart.DEFAULT_COLORS[i]);
            }


            return multiChart;
    }

	protected JFreeChart createBarChart(HttpServletRequest request, HttpServletResponse response, 
    		String queryId, CategoryDataset datas, int width, int height, String format, PlotOrientation orientation) throws Exception {

			JFreeChart chart = ChartFactory.createBarChart(null, null, null, datas, orientation,
					true, true, false);

			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			java.util.List l = datas.getColumnKeys();
			BarRenderer r = (BarRenderer) plot.getRenderer();

			for (int i = 0; i < Math.min(datas.getColumnCount(), 8); i++) {
				r.setSeriesPaint(i, GetChart.DEFAULT_COLORS[i]);
			}
			r.setBarPainter(new StandardBarPainter());
			r.setDefaultShadowsVisible(false);
			r.setDrawBarOutline(false);
			r.setShadowVisible(false);
			chart.setBorderVisible(false);
			plot.setOutlineVisible(false);
			plot.setBackgroundPaint(new Color(0xd6e4f1));


			return chart;
	}

    @RequestMapping(method = RequestMethod.GET)
	public void getchart(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("QUERYID") String queryId,
			@RequestParam("TYPE") String typeChart,
			@RequestParam("INDICATORS") String indicators,
			@RequestParam(value = "WIDTH", required = false) Integer width,
			@RequestParam(value = "HEIGHT", required = false) Integer height,
			@RequestParam(value = "IMAGEMAPID", required = false) String imagemapId,
			@RequestParam(value = "FORMAT", required = false) String format) throws Exception 
			{
		List<String> _indicators = Arrays.asList(indicators.split(","));
		width = width == null ? 1200 : width;
		height = height == null ? 800 : height;

		boolean imagemap = (imagemapId != null);

		
		DataQueryFeatureSource dqfs = getCache().getResults(queryId);
		SimpleFeatureSource results = (SimpleFeatureSource) dqfs.getFeatureSource();

		ChartDatas mpd = new ChartDatas();

		SimpleFeatureIterator it = results.getFeatures().features();
		try 
		{
			while (it.hasNext()) // iteration on the features
			{
				SimpleFeature current = it.next();
				String columnName = "";
				// iteration on the attributes
				for (AttributeDescriptor d : results.getSchema().getAttributeDescriptors())
				{
					String name = d.getName().toString();
					if (current.getAttribute(name) instanceof String)
					{
						columnName += (current.getAttribute(name) + " ");
					}
					// name is an asked indicator
					if (indicators.contains(name)) {
						if (current.getAttribute(name) instanceof Double)
						{
							mpd.addElement(columnName, name, (Double)current.getAttribute(name));
						}
						else if (current.getAttribute(name) == null)
						{
							mpd.addElement(columnName, name, (Double) 0.0);
						}
					}
				}
			}
		} 
		finally{
			it.close();
		}

		final CategoryDataset newSet; 
		
		
		if (! typeChart.equalsIgnoreCase("piebyrow"))
		{
			newSet = DatasetUtilities.createCategoryDataset(mpd.getTranslatedIndicators(dqfs),
					mpd.getRowKeys(), 
					mpd.getSwappedDoubleDatas());
		}
		else
		{
			newSet = DatasetUtilities.createCategoryDataset(mpd.getRowKeys(), 
					mpd.getTranslatedIndicators(dqfs), 
					mpd.getdoubleDatas());

		}
		
		JFreeChart chart = null;

		if (typeChart.equalsIgnoreCase("pie") || typeChart.equalsIgnoreCase("piebyrow"))
		{
			chart = createPieChart(request, response, queryId, newSet, width, height, format);
		}
		else if (typeChart.equalsIgnoreCase("bar"))
		{
			chart = createBarChart(request, response, queryId, newSet, width, height, format, PlotOrientation.VERTICAL);
			
		}
		else if (typeChart.equalsIgnoreCase("horizontalbar"))
		{
			chart = createBarChart(request, response, queryId, newSet, width, height, format, PlotOrientation.HORIZONTAL);
		}
		else
		{
			throw new Exception(typeChart + " is not a legal value");
		}

		// change number format			
		NumberFormat df = new DecimalFormat("##0.0");
		NumberFormat pdf = new DecimalFormat("##0.0%");
		Plot plot = chart.getPlot();
		if (plot instanceof CategoryPlot) {
			Axis axis = ((CategoryPlot) plot).getRangeAxis();
			if (axis instanceof NumberAxis) {
				((NumberAxis) axis).setNumberFormatOverride(df);
			}
			axis = ((CategoryPlot) plot).getDomainAxis();
			if (axis instanceof NumberAxis) {
				((NumberAxis) axis).setNumberFormatOverride(df);
			}			
			CategoryItemRenderer renderer = ((CategoryPlot) plot).getRenderer();
			StandardCategoryToolTipGenerator generator = 
				new StandardCategoryToolTipGenerator(StandardCategoryToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT_STRING, df);
			renderer.setBaseToolTipGenerator(generator);
		} else {
			PiePlot pplot = (PiePlot)((MultiplePiePlot) plot).getPieChart().getPlot();
			StandardPieToolTipGenerator generator = 
				new StandardPieToolTipGenerator(StandardPieToolTipGenerator.DEFAULT_TOOLTIP_FORMAT, df, pdf);
			pplot.setToolTipGenerator(generator);
		}

		if (imagemap)
		{
			
			// create RenderingInfo object
			StandardEntityCollection col = new StandardEntityCollection();
			ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
			BufferedImage chartImage = chart.createBufferedImage(width, height, info);
			BufferedImage image = new BufferedImage(width,
					height, BufferedImage.TYPE_INT_ARGB);

			Graphics2D graphics = image.createGraphics();

			PrintWriter pw = new PrintWriter(response.getOutputStream());
			chart.draw(graphics, new Rectangle(0,0,width,height), info);

			ChartUtilities.writeImageMap(pw, imagemapId, info, new StandardToolTipTagFragmentGenerator(),
					new StandardURLTagFragmentGenerator());

			pw.close();
		}
		else 
		{
			try 
			{
				if (format == null)
				{
					response.setContentType("image/png");
				}
				else 
				{
					response.setContentType(format);    		
				}
				ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width,height);			
			} catch (Exception e) 
			{
				System.out.println("Problem occurred while creating chart.");
				e.printStackTrace();
			}finally {
				response.getOutputStream().close();
			}
		}


	}
}
