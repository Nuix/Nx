package com.nuix.nx.controls.models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import nuix.Item;
import nuix.MetadataProfile;

@SuppressWarnings("serial")
public class ItemTableModel extends DefaultTableModel {
	private static Logger logger = Logger.getLogger(ItemTableModel.class);
	
	class RowColKey {
		int row = 0;
		int col = 0;
		
		public RowColKey(int row, int col) {
			this.row = row;
			this.col = col;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + col;
			result = prime * result + row;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RowColKey other = (RowColKey) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (col != other.col)
				return false;
			if (row != other.row)
				return false;
			return true;
		}
		private ItemTableModel getEnclosingInstance() {
			return ItemTableModel.this;
		}
	}
	
	private String[] builtInFields = new String[] {
		"Kind","Name"	
	};
	
	private CacheLoader<RowColKey,String> cellCacheLoader = null;
	private LoadingCache<RowColKey,String> cellCache = null;
	
	private List<Item> items = new ArrayList<Item>();
	private MetadataProfile profile = null;
	
	public void setItems(List<Item> items) {
		this.items = items;
		cellCache.invalidateAll();
		logger.info("Rebuilding item table...");
		this.fireTableDataChanged();
	}
	
	public Item getItemAt(int row) {
		return items.get(row);
	}
	
	public void setProfile(MetadataProfile profile) {
		this.profile = profile;
		this.fireTableStructureChanged();
	}
	
	public ItemTableModel() {
		cellCacheLoader = new CacheLoader<ItemTableModel.RowColKey, String>(){
			@Override
			public String load(RowColKey rcKey) throws Exception {
				Item item = items.get(rcKey.row);
				if(profile == null) {
					switch (rcKey.col) {
					case 0: return item.getKind().getLocalisedName();
					case 1: return item.getLocalisedName();
					default: return "Invalid column index "+rcKey.col;
					}
				} else {
					try {
						return profile.getMetadata().get(rcKey.col).evaluate(item);
					} catch (Exception e) {
						return "ERROR: "+e.getMessage();
					}
				}
			}
		};
		
		cellCache = CacheBuilder.newBuilder()
				.maximumSize(1000)
				.build(cellCacheLoader);
	}
	
	@Override
	public int getRowCount() {
		int result = 0;
		if(items != null) {
			result = items.size();
		}
		return result;
	}
	
	@Override
	public int getColumnCount() {
		int result = 0;
		if(profile == null) {
			result = builtInFields.length;
		} else {
			result = profile.getMetadata().size();
		}
		return result;
	}
	
	@Override
	public String getColumnName(int column) {
		String result = "????";
		if(profile == null) {
			result = builtInFields[column];
		} else {
			result = profile.getMetadata().get(column).getName();
		}
		return result;
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		try {
			return cellCache.get(new RowColKey(row,column));
		} catch (ExecutionException e) {
			logger.error(e);
			return e.getMessage();
		}
	}
	
	
}
