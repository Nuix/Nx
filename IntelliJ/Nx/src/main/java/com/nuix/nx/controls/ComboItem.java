/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

/***
 * Data class for representing combo box entries
 * @author Jason Wells
 */
public class ComboItem {
    private String value;
    private String label;

    /***
     * Create instance with the provided label and value
     * @param label The label to associated
     * @param value The value to associate
     */
    public ComboItem(String label, String value) {
        this.value = value;
        this.label = label;
    }

    /***
     * Gets the value associated to this instance
     * @return The associated value
     */
    public String getValue() {
        return this.value;
    }

    /***
     * Gets the label associate to this instance
     * @return The associated label
     */
    public String getLabel() {
        return this.label;
    }

    @Override
    public String toString() {
        return label;
    }
}