/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/***
 * Table model for {@link com.nuix.nx.controls.CsvTable}.
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class CsvTableModel extends AbstractTableModel {

	private List<String> headers = null;
	private List<Map<String,String>> records = new ArrayList<Map<String,String>>();
	
	public CsvTableModel(List<String> headers){
		this.headers = headers;
	}
	
	@Override
	public int getColumnCount() {
		return headers.size()+1;
	}

	@Override
	public int getRowCount() {
		return records.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(col == 0){
			return row + 1;
		} else {
			Map<String,String> record = records.get(row);
			String header = headers.get(col-1);
			if (record.containsKey(header)){
				return record.get(header);	
			}
			else {
				return "";
			}
		}
	}

	@Override
	public void setValueAt(Object aValue, int row, int col) {
		if(col > 0){
			Map<String,String> record = records.get(row);
			record.put(headers.get(col-1),(String)aValue);
		}
	}

	@Override
	public String getColumnName(int col) {
		if(col == 0){
			return "#";
		} else {
			return headers.get(col-1);
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col != 0;
	}

	public void addRecord(Map<String,String> record){
		System.out.println("Adding Record:");
		for (Map.Entry<String, String> entry : record.entrySet()) {
			System.out.println(entry.getKey()+" => "+entry.getValue());
		}
		records.add(record);
		fireTableRowsInserted(records.size()-1, records.size()-1);
	}
	
	public void removeRecordAt(int row){
		records.remove(row);
		fireTableRowsDeleted(row, row);
	}

	public List<String> getHeaders() {
		return headers;
	}

	public List<Map<String, String>> getRecords() {
		return records;
	}
}
