package com.nuix.nx.controls.models;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class StringListTableModel extends AbstractTableModel {

	private List<String> values = new ArrayList<String>();
	private boolean editable = false;
	
	@Override
	public int getRowCount() { return values.size(); }

	@Override
	public int getColumnCount() { return 1; }

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return values.get(rowIndex);
	}

	@Override
	public String getColumnName(int column) {
		return "Values";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return editable;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(editable) {
			values.set(rowIndex, (String)aValue);
		}
	}
	
	public int getValueCount() {
		return values.size();
	}
	
	public String getValueAt(int index) {
		return values.get(index);
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
		this.fireTableDataChanged();
	}
	
	public void addValue(String value) {
		values.add(value);
		this.fireTableDataChanged();
	}
	
	public void removeValueAt(int rowIndex) {
		values.remove(rowIndex);
		this.fireTableRowsDeleted(rowIndex, rowIndex);
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}
