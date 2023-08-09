/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls.models;

/***
 * This class represents a choice in some controls and has an associated label, tool tip, value and whether the choice is checked.
 * @author Jason Wells
 *
 * @param <T> The data type of the value help by this instance
 */
public class Choice<T> {
	private String label = "";
	private String toolTip;
	private boolean isSelected = false;
	private T value;
	
	public Choice(){}
	
	/***
	 * Creates a choice object.  Label and tool tip will be based on calling toString on the value provided. 
	 * @param value The value this choice object represents.
	 */
	public Choice(T value){
		this.value = value;
		this.toolTip = this.label = value.toString();
	}
	
	/***
	 * Creates a choice object.  Tool tip will be based on the provided label string.
	 * @param value The value this choice object represents.
	 * @param label The displayed string for this choice.
	 */
	public Choice(T value,String label){
		this.value = value;
		this.toolTip = this.label = label;
	}
	
	/***
	 * Creates a choice object.
	 * @param value The value this choice object represents.
	 * @param label The displayed string for this choice.
	 * @param toolTip The tool tip associated to this choice.
	 */
	public Choice(T value,String label,String toolTip){
		this.value = value;
		this.label = label;
		this.toolTip = toolTip;
	}
	
	/***
	 * Creates a choice object.
	 * @param value The value this choice object represents.
	 * @param label The displayed string for this choice.
	 * @param toolTip The tool tip associated to this choice.
	 * @param isSelected Whether this choice is checked by default.
	 */
	public Choice(T value,String label,String toolTip, boolean isSelected){
		this.value = value;
		this.label = label;
		this.toolTip = toolTip;
		this.isSelected = isSelected;
	}
	
	/***
	 * Whether this choice is currently selected
	 * @return True if this choice is selected.
	 */
	public boolean isSelected() {
		return isSelected;
	}
	
	/***
	 * Sets whether this choice is currently selected.
	 * @param isSelected Provide true to select this choice.
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	/***
	 * The tool tip which will be associated to this choice.
	 * @return The tool tip.
	 */
	public String getToolTip() {
		return toolTip;
	}
	
	/***
	 * Sets the tool tip associated to this choice.
	 * @param toolTip The tool tip.
	 */
	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}
	
	/***
	 * Gets the label used to display this choice.
	 * @return The label string.
	 */
	public String getLabel() {
		return label;
	}
	
	/***
	 * Sets the label used to display this choice.
	 * @param label The label string to use.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/***
	 * Gets the value associated to this choice.
	 * @return The value associated.
	 */
	public T getValue() {
		return value;
	}
	
	/***
	 * Sets the value associated to this choice.
	 * @param value The value to be associated.
	 */
	public void setValue(T value) {
		this.value = value;
	}
}
