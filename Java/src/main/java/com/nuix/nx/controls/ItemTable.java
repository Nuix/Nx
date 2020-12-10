package com.nuix.nx.controls;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.nuix.nx.controls.models.ItemTableModel;
import com.nuix.nx.helpers.TableHelper;

import nuix.Item;
import nuix.MetadataProfile;

@SuppressWarnings("serial")
public class ItemTable extends JPanel {
	private static Logger logger = Logger.getLogger(ItemTable.class);
	
	private JTable table;
	private ItemTableModel model = new ItemTableModel();
	private JScrollPane scrollPane;
	
	public ItemTable() {
		setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane);
		
		table = new JTable(model);
		table.setFont(new Font("Dialog", Font.PLAIN, 12));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
	}

	public void setItems(List<Item> items) {
		model.setItems(items);
		SwingUtilities.invokeLater(()->{
			logger.info("Selecting row 0...");
			setSelectedRow(0);
			logger.info("Autofitting table columns...");
			TableHelper.autofitTableColumns(table, scrollPane, 10);
		});
	}
	
	public void setSelectedRow(int rowIndex) {
		if(model.getRowCount() < 1) { return; }
		if(rowIndex < 0) { rowIndex = model.getRowCount() - 1; }
		if(rowIndex > model.getRowCount() - 1) { rowIndex = 0; }
		table.setRowSelectionInterval(rowIndex, rowIndex);
		Rectangle cellRect = table.getCellRect(rowIndex, 0, true);
		table.scrollRectToVisible(cellRect);
	}
	
	public int getSelectedRow() {
		return table.getSelectedRow();
	}

	public void setProfile(MetadataProfile profile) {
		model.setProfile(profile);
	}
	
	public void selectNextItem() {
		setSelectedRow(table.getSelectedRow() + 1);
	}
	
	public void selectPreviousItem() {
		setSelectedRow(table.getSelectedRow() - 1);
	}
	
	public void whenSelectionChanges(BiConsumer<Integer,Item> callback) {
		addSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selectedIndex = table.getSelectedRow();
				if(model.getRowCount() > 0 && selectedIndex >= 0 && selectedIndex < model.getRowCount()) {
					Item item = model.getItemAt(selectedIndex);
					callback.accept(selectedIndex,item);	
				}
			}
		});
	}
	
	public void addSelectionListener(ListSelectionListener listener) {
		table.getSelectionModel().addListSelectionListener(listener);
	}
	
	public void removeSelectionListener(ListSelectionListener listener) {
		table.getSelectionModel().removeListSelectionListener(listener);
	}
}
