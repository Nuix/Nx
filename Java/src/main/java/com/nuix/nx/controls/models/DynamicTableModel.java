/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

/***
 * Table model used to store data for a {@link com.nuix.nx.controls.DynamicTableControl}
 * @author JWells01
 *
 */
@SuppressWarnings("serial")
public class DynamicTableModel extends AbstractTableModel {
	private List<String> headers;
	private List<Object> records;
	private List<Boolean> recordSelection;
	private DynamicTableValueCallback valueCallback;
	private String filter = "";
	private ChoiceTableModelChangeListener changeListener;
	private Map<Integer,Integer> filterMap;
	private Set<Integer> additionalEditableColumns = new HashSet<Integer>();
	private boolean defaultCheckState = false;
	
	/***
	 * Create a new instance
	 * @param headers The headers for each column
	 * @param records A collection of records to be displayed
	 * @param valueCallback Callback that yields a value for a cell given a particular record and column number
	 * @param defaultCheckState Determines whether by default are records checked
	 */
	public DynamicTableModel(List<String> headers, List<Object> records, DynamicTableValueCallback valueCallback, boolean defaultCheckState){
		this.headers = headers;
		this.records = records;
		this.valueCallback = valueCallback;
		this.defaultCheckState = defaultCheckState;
		recordSelection = new ArrayList<Boolean>();
		for (int i = 0; i < records.size(); i++) {
			recordSelection.add(defaultCheckState);
		}
		filterMap = new HashMap<Integer,Integer>();
		applyFiltering();
	}
	
	/***
	 * Set a listener which will be notified when changes are made
	 * @param listener The listener to be notified of changes
	 */
	public void setChangeListener(ChoiceTableModelChangeListener listener){
		changeListener = listener;
	}
	
	@Override
	public int getColumnCount() {
		return headers.size()+1;
	}

	@Override
	public int getRowCount() {
		return filterMap.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
			return recordSelection.get(resolveFilterIndex(rowIndex));
		else{
			try {
				Object record = records.get(resolveFilterIndex(rowIndex));
				Object value = valueCallback.interact(record,columnIndex-1,false,null);
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				return "Error Occurred: "+e.getMessage();
			}
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0 || additionalEditableColumns.contains(columnIndex-1);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		//System.out.println("Setting "+rowIndex+"("+resolveFilterIndex(rowIndex)+"),"+columnIndex+" to "+aValue);
		try {
			if(columnIndex == 0){
				recordSelection.set(resolveFilterIndex(rowIndex), (Boolean)aValue);
			}
			else if(additionalEditableColumns.contains(columnIndex-1)){
				Object record = records.get(resolveFilterIndex(rowIndex));
				valueCallback.interact(record,columnIndex-1,true,aValue);
			}
			notifyChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getColumnName(int columnIndex) {
		if(columnIndex == 0){
			return "";
		}
		else {
			return headers.get(columnIndex-1);
		}
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex){
		case 0:
			return Boolean.class;
		default:
			return String.class;
		}
	}
	
	/***
	 * Notify listeners that changes were made
	 */
	private void notifyChanged(){
		if(changeListener != null){
			changeListener.dataChanged();
		}
		
		if(filter.equalsIgnoreCase(":checked:") || filter.equalsIgnoreCase(":unchecked:")){
			applyFiltering();
		}
	}
	
	/***
	 * Filter the displayed records
	 */
	private void applyFiltering(){
		Map<Integer,Integer> tempFilterMap = new HashMap<Integer,Integer>();
		
		if(filter == null || filter.isEmpty()){
			for (int i = 0; i < records.size(); i++) {
				tempFilterMap.put(i,i);
			}
			filterMap = tempFilterMap;
			//System.out.println("Filtered Count: "+filterMap.size());
			this.fireTableDataChanged();
		}else{
			int filterIndex = 0;
			if(filter.equalsIgnoreCase(":checked:")){
				for (int i = 0; i < records.size(); i++) {
					if(recordSelection.get(i) == true){
						tempFilterMap.put(filterIndex,i);
						filterIndex++;
					}
				}
				filterMap = tempFilterMap;
				//System.out.println("Filtered Count: "+filterMap.size());
				this.fireTableDataChanged();
			} else if(filter.equalsIgnoreCase(":unchecked:")){
				for (int i = 0; i < records.size(); i++) {
					if(recordSelection.get(i) == false){
						tempFilterMap.put(filterIndex,i);
						filterIndex++;
					}
				}
				filterMap = tempFilterMap;
				//System.out.println("Filtered Count: "+filterMap.size());
				this.fireTableDataChanged();
			} else {
				Pattern filterPattern = Pattern.compile(Pattern.quote(filter), Pattern.CASE_INSENSITIVE);
				//System.out.println("Filter: "+filter);
				int columnCount = headers.size();
				for (int i = 0; i < records.size(); i++) {
					Object record = records.get(i);
					for (int c = 0; c < columnCount; c++) {
						String value = "";
						try {
							value = valueCallback.interact(record, c, false, null).toString();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (filterPattern.matcher(value).find()) {
							//System.out.println("Matched: "+value);
							tempFilterMap.put(filterIndex, i);
							filterIndex++;
							break;
						}
					}
				}
				filterMap = tempFilterMap;
				//System.out.println("Filtered Count: "+filterMap.size());
				DynamicTableModel.this.fireTableDataChanged();
			}
		}
	}
	
	/***
	 * Needed to translate record indices between entire collection and
	 * the currently displayed filter subset of records
	 * @param filteredIndex
	 * @return
	 */
	private int resolveFilterIndex(int filteredIndex){
		if(filterMap.size() < 1){
			return filteredIndex;
		}
		else{
			return filterMap.get(filteredIndex);
		}
	}
	
	/***
	 * Set the current filter string
	 * @param filter The filter string to use
	 */
	public void setFilter(String filter){
		this.filter = filter;
		applyFiltering();
		notifyChanged();
	}
	
	/***
	 * Used to determine whether a given record is checked in the table
	 * @param record The record to check for
	 * @return True if the record is present and found to be checked
	 */
	public boolean isSelected(Object record){
		int recordIndex = records.lastIndexOf(record);
		return recordSelection.get(recordIndex);
	}
	
	/***
	 * Gets the records associated
	 * @return The current full set of records
	 */
	public List<Object> getRecords(){
		return records;
	}
	
	/***
	 * Sets the records associated
	 * @param records The records to associate
	 */
	public void setRecords(List<Object> records){
		this.records = records;
		for (int i = 0; i < records.size(); i++) {
			recordSelection.add(defaultCheckState);
		}
		filterMap = new HashMap<Integer,Integer>();
		setFilter("");
	}
	
	/***
	 * Adds a single record
	 * @param record The record to add
	 */
	public void addRecord(Object record){
		this.records.add(record);
		recordSelection.add(defaultCheckState);
		setFilter("");
	}
	
	/***
	 * Remove a record at a specified index
	 * @param rowIndex The index of the row containing the record to remove
	 */
	public void remove(int rowIndex){
		this.records.remove(rowIndex);
		recordSelection.remove(rowIndex);
		setFilter("");
	}
	
	/***
	 * Set the checked state of a record at a given index
	 * @param index The index to set the checked state of
	 * @param value The checked state to set
	 */
	public void setCheckedAtIndex(int index, boolean value){
		if(index > 0 && index < recordSelection.size()){
			recordSelection.set(index, value);
		}
	}
	
	/***
	 * Sets the checked state of the currently displayed records to checked.  If no filtering
	 * is currently applied this is all records, otherwise it will be just the filtered subset.
	 */
	public void checkDisplayedRecords(){
		for(Map.Entry<Integer,Integer> entry : filterMap.entrySet()){
			recordSelection.set(entry.getValue(), true);
			this.fireTableCellUpdated(entry.getKey(), 0);
		}
		notifyChanged();
	}
	
	/***
	 * Sets the checked state of the currently displayed records to unchecked.  If no filtering
	 * is currently applied this is all records, otherwise it will be just the filtered subset.
	 */
	public void uncheckDisplayedRecords(){
		for(Map.Entry<Integer,Integer> entry : filterMap.entrySet()){
			recordSelection.set(entry.getValue(), false);
			this.fireTableCellUpdated(entry.getKey(), 0);
		}
		notifyChanged();
	}
	
	/***
	 * Gets a list of records which are checked, regardless of whether they are currently displayed
	 * @return A list of checked records
	 */
	public List<Object> getCheckedRecords(){
		List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < recordSelection.size(); i++) {
			if(recordSelection.get(i) == true)
				result.add(records.get(i));
		}
		return result;
	}
	
	/***
	 * Hashes a record by the values in its columns.  Used to store settings to JSON
	 * @param record The record to hash
	 * @return An MD5 string based on a concatenation of the column values
	 */
	protected String hashRecord(Object record){
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		StringBuffer hashBuffer = new StringBuffer();
		StringBuffer recordContent = new StringBuffer();
		for (int i = 0; i < headers.size(); i++) {
			recordContent.append(valueCallback.interact(record, i,false,null).toString());
		}
		md.update(recordContent.toString().getBytes());
		byte[] digest = md.digest();
		for (byte b : digest) {
			hashBuffer.append(String.format("%02x", b & 0xff));
		}
		return hashBuffer.toString();
	}
	
	/***
	 * Generates a series of hashes representing each record by calling {@link #hashRecord(Object)}
	 * on each record.
	 * @param records The record to generate hashes for.
	 * @return A Set of distinct MD5 hash strings
	 */
	protected Set<String> getRecordHashes(List<Object> records){
		Set<String> hashes = new HashSet<String>();
		for(Object record : records){
			hashes.add(hashRecord(record));
		}
		
		return hashes;
	}
	
	/***
	 * Gets the MD5 hashes for all the currently checked records using {@link #getRecordHashes}
	 * @return MD5 hashes for all the currently checked records
	 */
	public Set<String> getCheckedRecordHashes(){
		return getRecordHashes(getCheckedRecords());
	}
	
	/***
	 * Sets the checked state of loaded records to checked for records with MD5 hash values
	 * matching those in the provided list.  Used to restore a selection of items which has
	 * previously been saved.
	 * @param hashStrings The MD5 hashes to match to records to be checked
	 */
	public void setCheckedRecordsFromHashes(List<String> hashStrings){
		Set<String> hashes = new HashSet<String>();
		for(String hash : hashStrings){
			hashes.add(hash);
		}
		for (int i = 0; i < records.size(); i++) {
			if(hashes.contains(hashRecord(records.get(i)))){
				recordSelection.set(i, true);
				this.fireTableCellUpdated(i, 0);
			} else {
				recordSelection.set(i, false);
				this.fireTableCellUpdated(i, 0);
			}
		}
	}
	
	/***
	 * Gets a count of how many records are currently checked
	 * @return The checked record count
	 */
	public int getCheckedValueCount(){
		int result = 0;
		for (int i = 0; i < recordSelection.size(); i++) {
			if(recordSelection.get(i) == true)
				result++;
		}
		return result;
	}
	
	/***
	 * Gets the count of records which are currently displayed.  If no filtering is currently applied
	 * this will be to total record count.  If filtering is currently applied this will be the number
	 * of displayed records.
	 * @return A count of currently visible records
	 */
	public int getVisibleValueCount(){
		return filterMap.size();
	}
	
	/***
	 * Gets the total count of records, regardless of check state or display state
	 * @return The total count of records
	 */
	public int getTotalValueCount(){
		return records.size();
	}
	
	/***
	 * Shift a series of rows up 1
	 * @param positions Position indices of the rows to be shifted
	 * @return The resulting new positions
	 */
	public int[] shiftRowsUp(int[] positions){
		return shiftRows(positions,-1);
	}
	
	/***
	 * Shift a series of rows down 1
	 * @param positions Position indices of the rows to be shifted
	 * @return The resulting new positions
	 */
	public int[] shiftRowsDown(int[] positions){
		return shiftRows(positions,1);
	}
	
	/***
	 * Shifts a given set of rows (based on row index) a given offset.  A value of -1 for the offset is up (earlier in the list)
	 * while a value of 1 is down (later in the list).
	 * @param positions Position indices of the rows to be shifted
	 * @param offset The offset to shift the rows.
	 * @return The resulting new positions
	 */
	public int[] shiftRows(int[] positions, int offset){
		List<Integer> selection = new ArrayList<Integer>();
		for (int i = 0; i < positions.length; i++) {
			if(positions[i] + offset < 0 || positions[i] + offset > records.size() - 1)
				return positions;
			else
				selection.add(positions[i]);
		}
		Collections.sort(selection);
		int minPos = selection.get(0);
		Collections.reverse(selection);
		List<Object> selectedObjects = new ArrayList<Object>();
		for(int i : selection){
			selectedObjects.add(records.remove(i));	
		}
		Collections.reverse(selectedObjects);
		records.addAll(minPos+offset, selectedObjects);
		this.fireTableDataChanged();
		return new int[]{minPos+offset,minPos+offset+positions.length-1};
	}
	
	/***
	 * Allows caller to define whether a given column is allowed to be editable.  The column index
	 * provided is relative to user data and therefore index 0 is the first record column.
	 * @param column The column index to set as editable
	 */
	public void setColumnEditable(int column){
		additionalEditableColumns.add(column);
	}

	/***
	 * Sets the default checked state of records
	 * @param defaultCheckState The default check state to use
	 */
	public void setDefaultCheckState(boolean defaultCheckState) {
		this.defaultCheckState = defaultCheckState;
	}
}
