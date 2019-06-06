/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

/***
 * Table model used by the {@link com.nuix.nx.controls.ChoiceTableControl}
 * @author Jason Wells
 *
 * @param <T> The data type of the of the {@link com.nuix.nx.controls.models.Choice} instances which will be held by this model.
 */
@SuppressWarnings("serial")
public class ChoiceTableModel<T> extends AbstractTableModel {
	private List<Choice<T>> choices;
	private List<Choice<T>> displayedChoices;
	private String filter = "";
	private ChoiceTableModelChangeListener changeListener;
	
	/***
	 * Create a new instance
	 */
	public ChoiceTableModel(){
		choices = new ArrayList<Choice<T>>();
		setFilter("");
	}
	
	/***
	 * Set a listener callback which will be notified of changes 
	 * @param listener The listener to be notified of changes
	 */
	public void setChangeListener(ChoiceTableModelChangeListener listener){
		changeListener = listener;
	}
	
	/***
	 * Notify listeners that a change occurred
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
	 * Attempt to find a choice in the model with a matching label
	 * @param label The label to look for
	 * @return A corresponding {@link Choice} object if a match was found
	 */
	public Choice<T> getFirstChoiceByLabel(String label){
		Choice<T> result = null;
		for(Choice<T> choice : choices){
			if(choice.getLabel().equals(label)){
				result = choice;
				break;
			}
		}
		return result;
	}
	
	/***
	 * Attempt to find a choice in the model with a matching value
	 * @param value The value to look for
	 * @return A corresponding {@link Choice} object if a match was found
	 */
	public Choice<T> getFirstChoiceByValue(T value){
		Choice<T> result = null;
		for(Choice<T> choice : choices){
			if(choice.getValue().equals(value)){
				result = choice;
				break;
			}
		}
		return result;
	}
	
	/***
	 * Gets the list of choices associated
	 * @return The choices
	 */
	public List<Choice<T>> getChoices() {
		return choices;
	}

	/***
	 * Sets the list of choices associated
	 * @param choices The choices to associate
	 */
	public void setChoices(List<Choice<T>> choices) {
		this.choices = choices;
		applyFiltering();
		notifyChanged();
	}

	private String[] columns = new String[]{
			"",
			""
	};
	
	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public int getRowCount() {
		return displayedChoices.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex){
			case 0:
				return displayedChoices.get(rowIndex).isSelected();
			case 1:
				return displayedChoices.get(rowIndex).getLabel();
			default:
				return "";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex){
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		default:
			return null;
	}
	}

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0) return true;
		else return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			displayedChoices.get(rowIndex).setSelected((boolean)aValue);
			notifyChanged();
		}
	}
	
	private void applyFiltering(){
		if(filter.isEmpty()){
			displayedChoices = choices;
		} else if (filter.equalsIgnoreCase(":checked:")) {
			displayedChoices = choices.stream().filter(c -> c.isSelected()).collect(Collectors.toList());
		}else if (filter.equalsIgnoreCase(":unchecked:")) {
			displayedChoices = choices.stream().filter(c -> !c.isSelected()).collect(Collectors.toList());
		} else {
			try {
				Pattern p = Pattern.compile(filter,Pattern.CASE_INSENSITIVE);
				displayedChoices = choices.stream().filter(c -> p.matcher(c.getLabel().toLowerCase()).find()).collect(Collectors.toList());	
			}
			catch(Exception exc) {
				displayedChoices = new ArrayList<Choice<T>>();
			}
		}
		this.fireTableDataChanged();
	}
	
	public void refreshTable() {
		this.fireTableDataChanged();
	}
	
	public void setFilter(String filter){
		this.filter = filter;
		applyFiltering();
		notifyChanged();
	}
	
	public Choice<T> getChoice(int rowIndex){
		return choices.get(rowIndex);
	}
	
	public Choice<T> getDisplayedChoice(int rowIndex){
		return displayedChoices.get(rowIndex);
	}

	public void addChoice(Choice<T> choice){
		choices.add(choice);
		int lastIndex = choices.size()-1;
		this.fireTableRowsInserted(lastIndex, lastIndex);
		notifyChanged();
	}
	
	public void setChoiceSelection(Choice<T> choice, boolean value){
		int index = choices.indexOf(choice);
		choices.get(index).setSelected(value);
		this.fireTableCellUpdated(index, 0);
		notifyChanged();
	}
	
	public void checkDisplayedChoices(){
		for(Choice<T> choice : displayedChoices){
			choice.setSelected(true);
			this.fireTableCellUpdated(displayedChoices.indexOf(choice), 0);
		}
		notifyChanged();
	}
	
	public void uncheckDisplayedChoices(){
		for(Choice<T> choice : displayedChoices){
			choice.setSelected(false);
			this.fireTableCellUpdated(displayedChoices.indexOf(choice), 0);
		}
		notifyChanged();
	}
	
	public void uncheckAllChoices(){
		for(Choice<T> choice : choices){
			choice.setSelected(false);
			this.fireTableCellUpdated(displayedChoices.indexOf(choice), 0);
		}
		notifyChanged();
	}
	
	public List<Choice<T>> getCheckedChoices(){
		return choices.stream().filter(c -> c.isSelected()).collect(Collectors.toList());
	}
	
	public List<Choice<T>> getUncheckedChoices(){
		return choices.stream().filter(c -> !c.isSelected()).collect(Collectors.toList());
	}
	
	public List<T> getCheckedValues(){
		List<T> result = new ArrayList<T>();
		for(Choice<T> choice : getCheckedChoices()){
			result.add(choice.getValue());
		}
		notifyChanged();
		return result;
	}
	
	public List<String> getCheckedLabels(){
		List<String> result = new ArrayList<String>();
		for(Choice<T> choice : getCheckedChoices()){
			result.add(choice.getLabel());
		}
		return result;
	}
	
	public int getCheckedValueCount(){
		return getCheckedChoices().size();
	}
	
	public int getVisibleValueCount(){
		return displayedChoices.size();
	}
	
	public int getTotalValueCount(){
		return choices.size();
	}
	
	public void setChoiceTypeName(String name){
		columns[1] = name;
		this.fireTableStructureChanged();
	}
	
	public int[] shiftRowsUp(int[] positions){
		return shiftRows(positions,-1);
	}
	
	public int[] shiftRowsDown(int[] positions){
		return shiftRows(positions,1);
	}
	
	public int[] shiftRows(int[] positions, int offset){
		List<Integer> selection = new ArrayList<Integer>();
		for (int i = 0; i < positions.length; i++) {
			if(positions[i] + offset < 0 || positions[i] + offset > choices.size() - 1)
				return positions;
			else
				selection.add(positions[i]);
		}
		Collections.sort(selection);
		int minPos = selection.get(0);
		Collections.reverse(selection);
		List<Choice<T>> selectedObjects = new ArrayList<Choice<T>>();
		for(int i : selection){
			selectedObjects.add(choices.remove(i));	
		}
		Collections.reverse(selectedObjects);
		choices.addAll(minPos+offset, selectedObjects);
		this.fireTableDataChanged();
		return new int[]{minPos+offset,minPos+offset+positions.length-1};
	}
	
	public int[] shiftRows(List<Choice<T>> list, int offset){
		if(offset > 0){
			Collections.reverse(list);
		}
		for(Choice<T> entry : list){
			int previousIndex = choices.indexOf(entry);
			int newIndex = previousIndex + offset;
			if(newIndex < 0){
				newIndex = choices.size() - 1;
			} else if (newIndex > choices.size() - 1){
				newIndex = 0;
			}
			choices.remove(entry);
			choices.add(newIndex,entry);
		}
		fireTableDataChanged();
		int[] newIndices = new int[list.size()];
		for(int i=0;i<list.size();i++){
			newIndices[i] = choices.indexOf(list.get(i));
		}
		return newIndices;
	}
	
	public void sortCheckedToTop(){
		List<Choice<T>> checked = getCheckedChoices();
		List<Choice<T>> unchecked = getUncheckedChoices();
		choices.clear();
		choices.addAll(checked);
		choices.addAll(unchecked);
		fireTableDataChanged();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void sortChoicesToTop(List<Choice> choicesToSort){
		List<Choice> reversed = new ArrayList<Choice>();
		reversed.addAll(choicesToSort);
		Collections.reverse(reversed);
		for(Choice choice : reversed){
			choices.remove(choice);
			choices.add(0, choice);
		}
	}
}
