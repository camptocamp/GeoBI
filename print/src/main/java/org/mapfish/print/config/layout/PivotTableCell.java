package org.mapfish.print.config.layout;

public class PivotTableCell extends TextBlock {


	private int columnIndex ;
	private int rowIndex ;
	private int colSpan ;
	
	public PivotTableCell(int rI, int cI,  int cS)
	{
		columnIndex = cI ;
		rowIndex    = rI ;
		colSpan     = cS ;
	}
	public int getColumnIndex() {
		return columnIndex;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public int getColSpan() {
		return colSpan;
	}
}
