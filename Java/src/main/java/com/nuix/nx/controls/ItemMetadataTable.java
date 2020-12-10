package com.nuix.nx.controls;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.nuix.nx.controls.models.ItemMetadataTableModel;
import com.nuix.nx.helpers.TableHelper;

import nuix.Item;
import nuix.MetadataProfile;

import javax.swing.JTable;

@SuppressWarnings("serial")
public class ItemMetadataTable extends JPanel {
	private JTable table;
	private ItemMetadataTableModel model = new ItemMetadataTableModel();
	private JScrollPane scrollPane;
	
	public ItemMetadataTable() {
		setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
	}

	public void setProfile(MetadataProfile profile) {
		model.setProfile(profile);
		SwingUtilities.invokeLater(()->{
			TableHelper.autofitTableColumns(table, scrollPane, 10);
		});
	}

	public void setItem(Item item) {
		model.setItem(item);
		SwingUtilities.invokeLater(()->{
			TableHelper.autofitTableColumns(table, scrollPane, 10);
		});
	}
}
