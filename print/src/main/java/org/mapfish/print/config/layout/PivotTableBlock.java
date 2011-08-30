/*
 * Copyright (C) 2010  Camptocamp
 *
 * This file is part of MapFish Server
 *
 * MapFish Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MapFish Server.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print.config.layout;

import java.util.ArrayList;

import org.mapfish.print.PDFCustomBlocks;
import org.mapfish.print.PDFUtils;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonArray;
import org.mapfish.print.utils.PJsonObject;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Bean to configure a !pivottable
 */
public class PivotTableBlock extends Block {

    private int[] widths = null;
    private int absoluteX = Integer.MIN_VALUE;
    private int absoluteY = Integer.MIN_VALUE;
    private int width = Integer.MIN_VALUE;
    private TableConfig tableConfig = null;

    public void render(final PJsonObject params, PdfElement target, final RenderingContext context) throws DocumentException {

        if (isAbsolute()) {
            context.getCustomBlocks().addAbsoluteDrawer(new PDFCustomBlocks.AbsoluteDrawer() {
                public void render(PdfContentByte dc) throws DocumentException {
                    final PdfPTable table = buildPivotTable(params, context, tableConfig);
                    if (table != null) {
                        table.setTotalWidth(width);
                        table.setLockedWidth(true);

                        if (widths != null) {
                            table.setWidths(widths);
                        }

                        table.writeSelectedRows(0, -1, absoluteX, absoluteY, dc);
                    }
                }
            });
        } else {
            final PdfPTable table = buildPivotTable(params, context, tableConfig);
            if (table != null) {
                if (widths != null) {
                    table.setWidths(widths);
                }

                table.setSpacingAfter((float) spacingAfter);
                target.add(table);
            }
        }
    }

    
    public void validate() {
        super.validate();
    }
    
    
    /**
     * Creates a PDF pivot table. Returns null if the table is empty
     * @throws DocumentException
     */
	private PdfPTable buildPivotTable(PJsonObject params, 
    								  RenderingContext context, 
    								  TableConfig tableConfig) throws DocumentException {
		int nbColumns = 0, nbRows = 0 ;
		ArrayList<PivotTableCell> tableBlocks = new ArrayList<PivotTableCell>();

		ArrayList<String> dataIndexes = new ArrayList<String>();

		PJsonArray groupHeaderObj = params.getJSONArray("groupHeaders");

		if (groupHeaderObj == null)
		{
			return null;
		}
		for (int i =0 ; i < groupHeaderObj.size(); i++)
		{
			int lastnbRows = 0;
			int colspan = 1 ;

			PJsonArray CurgroupHeader = groupHeaderObj.getJSONArray(i);
			
			for (int j = 0 ; j < CurgroupHeader.size() ; j ++)
			{
				PJsonObject cell = CurgroupHeader.getJSONObject(j);
				colspan = cell.getInt("colspan");
				lastnbRows += colspan;
				PivotTableCell newTb = new PivotTableCell(i, j, colspan);
				newTb.setText(cell.getString("header"));
				newTb.setAlign(HorizontalAlign.CENTER);
				newTb.setSpacingAfter(10);
				newTb.setFontSize(8.0);
				tableBlocks.add(newTb);
			}

			// check if the number of columns matches
			if (nbColumns == 0)
			{
				nbColumns = lastnbRows;
			}
			else
			{
				if (lastnbRows != nbColumns)
				{			
					throw new DocumentException("Malformed JSON : number of columns does not match");
				}
			}


			nbRows++;

		} 
		
		// iterating on columns JSON object
		PJsonArray columnsObj = params.getJSONArray("columns");

		if (nbColumns != columnsObj.size())
		{    				
			throw new DocumentException("Malformed JSON : number of columns does not match");
		}


		for (int i = 0 ; i < columnsObj.size(); i++)
		{
			PJsonObject column = columnsObj.getJSONObject(i);

			PivotTableCell newTb = new PivotTableCell(nbRows, i, 1);
			newTb.setText(column.getString("header"));
			newTb.setAlign(HorizontalAlign.CENTER);
			newTb.setSpacingAfter(10);
			tableBlocks.add(newTb);
			newTb.setFontSize(8.0);

			dataIndexes.add(column.getString("dataIndex"));
		}
		// we increment nbRows (we need to count the one coming with the columns JSON array
		nbRows++;

		// iterating on datas rows
		PJsonArray datasObj = params.getJSONArray("data");

		for (int i = 0 ; i < datasObj.size(); i++)
		{
			PJsonObject column = datasObj.getJSONObject(i);
			//System.out.println(column.getString("header"));
			for (int j = 0 ; j < dataIndexes.size(); j++)
			{
				String curValue ;

				try {
					Float cellFloat = column.getFloat(dataIndexes.get(j));
					// one decimal by default
					// TODO : this should be parameterized into the YAML
					// configuration file.
					curValue = String.format("%.1f", cellFloat);
				} catch (Exception e) {
					try {
						curValue = column.getString(dataIndexes.get(j));
					} catch (Exception e2) {
						curValue = ""; // not found ? falling back to empty
										// string value
					}
				}
				
				PivotTableCell newTb = new PivotTableCell(nbRows, j, 1);
				newTb.setText(curValue);
				newTb.setFontSize(5.0);
				newTb.setSpacingAfter(7);
				newTb.setAlign(HorizontalAlign.RIGHT);
				tableBlocks.add(newTb);
			}
			nbRows++;
		}

        
      final PdfPTable table = new PdfPTable(nbColumns);
      
      table.setWidthPercentage(100f);
       
      for (int i = 0; i < tableBlocks.size(); i++) {
    	  final PivotTableCell block = tableBlocks.get(i);
    	  if (block.isVisible(context, params) && !block.isAbsolute()) {
    		  final PdfPCell cell = createCell(params, context, block, 
    				  block.getRowIndex(), block.getColumnIndex(), nbRows, 
    				  nbColumns, tableConfig, block.getColSpan());
    		  table.addCell(cell);
    	  }
      }
      table.setSplitRows(false);
      table.setComplete(true);
      return table;
      
    }
	
    private PdfPCell createCell(final PJsonObject params, final RenderingContext context, final Block block, final int row,
    		final int col, final int nbRows, final int nbCols, final TableConfig tableConfig, final int colSpan) throws DocumentException {

    	final PdfPCell cell = PDFUtils.createCell(params, context, block, row, col, nbRows,  nbCols, tableConfig);
    	if (colSpan > 1)
    	{
    		cell.setColspan(colSpan);
    	}
    	return cell;
    } 
    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }
}