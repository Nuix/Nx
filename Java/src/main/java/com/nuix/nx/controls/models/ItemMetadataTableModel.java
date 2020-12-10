package com.nuix.nx.controls.models;

import javax.swing.table.DefaultTableModel;

import nuix.Item;
import nuix.MetadataProfile;

@SuppressWarnings("serial")
public class ItemMetadataTableModel extends DefaultTableModel {

	private String[] headers = new String[] {"Name","Value"};
	private MetadataProfile profile = null;
	private Item item = null;
	
	@Override
	public int getRowCount() {
		if(profile == null) {
			return 0;
		} else {
			return profile.getMetadata().size();
		}
	}

	@Override
	public int getColumnCount() {
		return headers.length;
	}

	@Override
	public String getColumnName(int column) {
		return headers[column];
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if(profile == null || item == null) {
			return "";
		} else {
			if(column == 0) {
				return profile.getMetadata().get(row).getLocalisedName();
			} else {
				try {
					return profile.getMetadata().get(row).evaluate(item);
				} catch (Exception e) {
					return e.getMessage();
				}	
			}
			
		}
	}

	public MetadataProfile getProfile() {
		return profile;
	}

	public void setProfile(MetadataProfile profile) {
		this.profile = profile;
		this.fireTableDataChanged();
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
		this.fireTableDataChanged();
	}
}
