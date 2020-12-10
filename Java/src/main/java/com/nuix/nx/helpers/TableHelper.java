package com.nuix.nx.helpers;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

public class TableHelper {
	private static Logger logger = Logger.getLogger(TableHelper.class);
	
	public static void autofitTableColumns(JTable table, JScrollPane scrollPane){
		JViewport viewport = scrollPane.getViewport();
		Point p = viewport.getViewPosition();
		int minRow = table.rowAtPoint(p);
		if(minRow < 0){ minRow = 0; }
		int maxRow = minRow + 100;
		
		for (int column = 0; column < table.getColumnCount(); column++)
		{
		    TableColumn tableColumn = table.getColumnModel().getColumn(column);
		    int preferredWidth = tableColumn.getMinWidth();
		    int maxWidth = tableColumn.getMaxWidth();
		 
		    for (int row = minRow; row < table.getRowCount() && row < maxRow; row++)
		    {
		        TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
		        Component c = table.prepareRenderer(cellRenderer, row, column);
		        int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
		        preferredWidth = Math.max(preferredWidth, width);
		 
		        //  We've exceeded the maximum width, no need to check other rows
		 
		        if (preferredWidth >= maxWidth){
		            preferredWidth = maxWidth;
		            break;
		        }
		    }
		    if(column != 0 && preferredWidth < 100){ preferredWidth = 100; }
		    tableColumn.setPreferredWidth( preferredWidth );
		}
	}
	
	public static void autofitTableColumns(JTable table, JScrollPane scrollPane, int pad){
		JViewport viewport = scrollPane.getViewport();
		Point p = viewport.getViewPosition();
		int minRow = table.rowAtPoint(p);
		if(minRow < 0){ minRow = 0; }
		int maxRow = minRow + 100;
		
		int intercellSpacing = table.getIntercellSpacing().width;
		
		for (int column = 0; column < table.getColumnCount(); column++)
		{
		    TableColumn tableColumn = table.getColumnModel().getColumn(column);
		    int preferredWidth = tableColumn.getMinWidth();
		    int maxWidth = tableColumn.getMaxWidth();
		 
		    for (int row = minRow; row < table.getRowCount() && row < maxRow; row++)
		    {
		        try {
					TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
					Component c = table.prepareRenderer(cellRenderer, row, column);
					int width = c.getPreferredSize().width + intercellSpacing;
					preferredWidth = Math.max(preferredWidth, width);
 
					//  We've exceeded the maximum width, no need to check other rows
					if (preferredWidth >= maxWidth){
					    preferredWidth = maxWidth;
					    break;
					}
				} catch (Exception e) {
					logger.error(e);
				}
		    }
		    
		    if(preferredWidth < 100){ preferredWidth = 100; }
		    tableColumn.setPreferredWidth(preferredWidth+pad);
		}
	}
}
