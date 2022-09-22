/******************************************
 Copyright 2022 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/
package com.nuix.nx.controls.models;

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Data model for use with the {@link com.nuix.nx.controls.ReportDisplayPanel}.
 *
 * The report is represented by a group of sections, each section being a group of data labels and their values.
 * A report might be displayed as:
 *
 *  SECTION 1
 *  --------
 *  Data Field 1                  Value 1
 *  Data Field 2                  Value 2
 *
 *  SECTION 2
 *  --------
 *  Data Field 3                  Value 3
 *
 *  ...
 *
 *  The sections names should be strings, the data field names should be strings, and the data values can be any
 *  object that has a reasonable toString() representation.
 *
 *  No effort is made by this class to make building the list of sections and data labels threadsafe.  As such, the
 *  sections and data should be built prior to displaying the data in any UI.  Updating data values will be
 *  run from the Swing thread so updating these values during display is safe.
 */
public class ReportDataModel {

    Map<String, Map<String, Object>> report = new LinkedHashMap<>();

    List<PropertyChangeListener> listeners = new ArrayList<>();

    /**
     * Add a PropertyChangeListener to this object.  The listener will be updated when new sections are added and when
     * data field values are updated.  When a section is added, the Property Name will be the name of the section.  When
     * a data field value is updated the property name will be a string containing the Section Name and the Data Field
     * Name connected with the SECTION_FIELD_DELIM (for example, "Section 1::Data Field 1".)
     * @param listener A property change listener to be informed when section and data changes are made.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        SwingUtilities.invokeLater( () -> { if (!listeners.contains(listener)) listeners.add(listener); } );
    }

    /**
     * Remove the provided listener from this object.
     * @param listener The PropertyChangeListener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        SwingUtilities.invokeLater( () -> { if (!listeners.contains(listener)) listeners.remove(listener); } );
    }

    /**
     * Add a section to the report, providing data as the section is created.
     *
     * This method is not guaranteed to be threadsafe.  It will inform listeners of the new section.
     *
     * If the section already exists it will be replaced with the provided data.
     * @param sectionName The name of the section.  This is for both identification and display purposes so make it
     *                    meaningful for display and also unique.
     * @param dataInSection The data to display in the form of a Map.  The keys to the map are the data field names and
     *                      are used both for display and as ids, so make them meaningful and unique.
     */
    public void addSection(String sectionName, Map<String, Object> dataInSection) {
        Map<String, Object> copied = new LinkedHashMap<>();

        for (String dataField : dataInSection.keySet()) {
           Object value = dataInSection.get(dataField);
           if (null == value) {
               throw new IllegalArgumentException(String .format("The value for the data field named %s is null, which is not allowed.", dataField));
           } else {
               copied.put(dataField, value);
           }
        }

        Map<String, Object> oldSection = report.getOrDefault(sectionName, null);
        report.put(sectionName, copied);
        notifyOfChange(sectionName, null, oldSection, copied);
    }

    /**
     * Add a new, empty section to the report.
     *
     * This method is not guaranteed to be thread safe.
     *
     * @param sectionName The name of the new section.  The name is both an id and displayed, so make it meaningful and
     *                    unique. If the section already exists, it will be replaced and the data in it will be lost.
     */
    public void addSection(String sectionName) {
        addSection(sectionName, new LinkedHashMap<String, Object>());
    }

    /**
     * Add data to the given section.  If the section doesn't already exist it will be added with the new data.  If
     * the data field already exists in the section, it will be replaced with the new value.
     *
     * This method is not guaranteed to be thread safe.  It will not notify the UI of a new data field, though if
     * it results in a new section, that section will be added to the UI with the new data field.
     *
     * @param sectionName The name of the section to add the data to.
     * @param dataField The name of the data to add - this will be used both for id and display so make it unique in the
     *                  section and also meaningful to the user.
     * @param value The value of the data field - any object with a meaningful toString() method.
     */
    public void addData(String sectionName, String dataField, Object value) {
        if (report.containsKey(sectionName)) {
            Map<String, Object> section = report.get(sectionName);

            if (null == value) {
                throw new IllegalArgumentException("The value being added must not be null.");
            } else {
                section.put(dataField, value);
            }
        } else {
            this.addSection(sectionName, Map.of(dataField, value));
        }
    }

    /**
     * Update an existing data field in the given section with a new value.  If the section doesn't exist an exception
     * will be thrown.  If the data field doesn't exist in the section then an exception will be thrown.  Otherwise,
     * the existing value for the data field will be replaced with the one provided here.
     *
     * This method will update listeners with new values and the changes should be reflected in the UI.
     *
     * @param sectionName The name of the section with the data field to update.  It must already exist in the report.
     * @param dataField The name of the data field to update.  It must exist in the section provided.
     * @param newValue The value to replace any value currently in the data field.  Any object with a meaningful
     *                 toString() method
     */
    public void updateData(String sectionName, String dataField, Object newValue) {
        if (report.containsKey(sectionName)) {
            Map<String, Object> section = report.get(sectionName);
            if (section.containsKey(dataField)) {
                if (null == newValue) {
                    throw new IllegalArgumentException("The value being set must not be null.");
                } else {
                    Object oldValue = section.get(dataField);

                    if (!newValue.equals(oldValue)) {
                        section.put(dataField, newValue);
                        notifyOfChange(sectionName, dataField, oldValue, newValue);
                    }

                }
            } else {
                throw new IllegalArgumentException(String.format("The section %s does not have a value for %s", sectionName, dataField));
            }
        } else {
            throw new IllegalArgumentException(String.format("This report has no section named %s", sectionName));
        }
    }

    /**
     * Get a String representation of the value of a data field.
     *
     * If either the section doesn't exist in the report or the data field is not found in the section provided then
     * this will return an empty string.
     *
     * @param sectionName The name of the section where the data can be found
     * @param dataField The name of the field whose data is to be retrieved
     * @return A string representation of the data field value, or an empty string if the data field can't be located.
     */
    public String getDataFieldValue(String sectionName, String dataField) {
        if (report.containsKey(sectionName)) {
            Map<String, Object> section = report.get(sectionName);
            if (section.containsKey(dataField)) {
                return section.get(dataField).toString();
            }
        }

        // Either section or data field don't exist, return empty value.
        return "";
    }

    /**
     * Get the list of data fields in the provided section.
     *
     * If the section doesn't exist in this report an empty set will be provided.
     *
     * @param sectionName The name of the section whose data fields are to be retrieved.
     * @return A Set of strings with the data field names, or an empty Set if the section doesn't exist.
     */
    public Set<String> getDataFieldsInSection(String sectionName) {
        if (report.containsKey(sectionName)) {
            return new LinkedHashSet<>(report.get(sectionName).keySet());
        }

        // Section not in report, return an empty Set
        return Set.of();
    }

    /**
     * Get the list of sections in this report.
     *
     * @return a Set of Strings with the names of all the sections in this report.
     */
    public Set<String> getSections() {
        return new HashSet<>(report.keySet());
    }

    public static final String SECTION_FIELD_DELIM = "::";

    private void notifyOfChange(String sectionName, String dataField, Object oldValue, Object newValue) {
        SwingUtilities.invokeLater(() -> {
            for (PropertyChangeListener listener : listeners) {
                String propertyName = dataField == null ? sectionName : sectionName + SECTION_FIELD_DELIM + dataField;
                listener.propertyChange(new PropertyChangeEvent(
                        this,
                        propertyName,
                        oldValue, newValue));
            }
        });
    }

}
