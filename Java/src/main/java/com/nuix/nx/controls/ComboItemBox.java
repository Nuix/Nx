/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.util.List;

import javax.swing.JComboBox;

/***
 * Slightly customized combo box control for displaying {@link ComboItem} objects
 * @author JWells01
 */
@SuppressWarnings("serial")
public class ComboItemBox extends JComboBox<ComboItem> {
	/***
	 * Sets the values displayed in the combo box, then selects the first value
	 * @param values List of {@link ComboItem} representing choices in the combo box
	 */
	public void setValues(List<ComboItem> values){
		removeAllItems();
		for(ComboItem value : values){
			addItem(value);
		}
		setSelectedIndex(0);
	}
	
	/***
	 * Adds a single {@link ComboItem} item as a value to the combo box
	 * @param value A single {@link ComboItem} value to add to the combo box
	 */
	public void addValue(ComboItem value){
		addItem(value);
	}
	
	/***
	 * Convenience method for adding 
	 * @param label
	 * @param value
	 */
	public void addValue(String label, String value){
		addValue(new ComboItem(label,value));
	}
	
	public ComboItem getSelectedComboItem(){
		return (ComboItem)getSelectedItem();
	}
	
	public String getSelectedLabel(){
		return getSelectedComboItem().getLabel();
	}
	
	public String getSelectedValue(){
		return getSelectedComboItem().getValue();
	}
	
	public void setSelectedComboItem(ComboItem value){
		setSelectedItem(value);
	}
	
	public void setSelectedLabel(String label){
		for (int i = 0; i < this.getModel().getSize(); i++) {
			if(this.getModel().getElementAt(i).getLabel().equalsIgnoreCase(label)){
				this.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public void setSelectedValue(String value){
		for (int i = 0; i < this.getModel().getSize(); i++) {
			if(this.getModel().getElementAt(i).getValue().equalsIgnoreCase(value)){
				this.setSelectedIndex(i);
				break;
			}
		}
	}
}
