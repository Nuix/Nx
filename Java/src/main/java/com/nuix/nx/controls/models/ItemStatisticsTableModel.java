/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import com.nuix.nx.NuixConnection;
import com.nuix.nx.helpers.FormatHelpers;

import nuix.ItemType;
import nuix.ProcessedItem;

/***
 * Table model used by {@link com.nuix.nx.controls.ProcessingStatusControl}.  Used in table to display processing numbers.
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class ItemStatisticsTableModel extends AbstractTableModel {

	class Stat{
		public String mimeType;
		public int totalProcessed;
		public int totalCorrupted;
		public int totalEncrypted;
		public int totalDeleted;
		
		public Stat(String mimeType){
			this.mimeType = mimeType;
			totalProcessed = 0;
			totalCorrupted = 0;
			totalEncrypted = 0;
			totalDeleted = 0;
		}
	}
	
	private String[] headers = new String[]{
		"Kind",
		"Type",
		"Mime Type",
		"Processed",
		"Corrupted",
		"Encrypted",
		"Deleted",
	};
	
	private Map<String,String[]> typeLookup = new HashMap<String,String[]>();
	private Map<String,Stat> stats = new HashMap<String,Stat>();
	private List<Stat> sortedStats = new ArrayList<Stat>();
	private long lastUpdated = System.currentTimeMillis();
	
	public ItemStatisticsTableModel() {
		super();
		if(NuixConnection.getUtilities() != null){
			Set<ItemType> allTypes = NuixConnection.getUtilities().getItemTypeUtility().getAllTypes();
			for(ItemType type : allTypes){
				typeLookup.put(type.getName(),new String[]{type.getKind().getName(),type.getLocalisedName()});
			}
		}
	}

	@Override
	public int getColumnCount() {
		return headers.length;
	}

	@Override
	public int getRowCount() {
		return stats.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Stat stat = sortedStats.get(row);
		switch (col) {
			case 0:
				if(typeLookup.containsKey(stat.mimeType)) {
					return typeLookup.get(stat.mimeType)[0];
				} else {
					return "Unknown";
				}
			case 1:
				if(typeLookup.containsKey(stat.mimeType)) {
					return typeLookup.get(stat.mimeType)[1];
				} else {
					return "Unknown";
				}
			case 2: return stat.mimeType;
			case 3: return FormatHelpers.formatNumber(stat.totalProcessed);
			case 4: return FormatHelpers.formatNumber(stat.totalCorrupted);
			case 5: return FormatHelpers.formatNumber(stat.totalEncrypted);
			case 6: return FormatHelpers.formatNumber(stat.totalDeleted);
			default:
				return "???";
		}
	}

	@Override
	public String getColumnName(int column) {
		return headers[column];
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
			case 0:
			case 1:
			case 2: return String.class;
			default:
				return Integer.class;
		}
	}

	public void record(ProcessedItem item){
		Stat stat = stats.computeIfAbsent(item.getMimeType(),(k) -> {
			Stat result = new Stat(item.getMimeType());
			sortedStats.add(result);
			return result;
		});
		stat.totalProcessed++;
		if(item.isCorrupted()){stat.totalCorrupted++;}
		if(item.isEncrypted()){stat.totalEncrypted++;}
		if(item.isDeleted()){stat.totalDeleted++;}
		
		// Time based rate limit the update frequency
		if (System.currentTimeMillis() - lastUpdated > 250){
			refresh();
			lastUpdated = System.currentTimeMillis();
		}
	}

	public void refresh() {
		sortedStats.sort((a,b)->{
			return Integer.compare(a.totalProcessed * -1, b.totalProcessed * -1);
		});
		fireTableDataChanged();
	}
}
