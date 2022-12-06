/******************************************
 Copyright 2022 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/
package com.nuix.nx.controls;

import com.nuix.nx.controls.models.ReportDataModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is designed to display a ReportDataModel
 */
public class ReportDisplayPanel extends JPanel {
    private static final int LABEL_COLUMN_WIDTH = 50;
    private static final int CONTROL_COLUMN_WIDTH = 50;
    private int activeRow = 0;
    private static final Insets sectionInsets = new Insets(5, 2, 2, 2);
    private static final Insets separatorInsets = new Insets(0, 0, 0, 0);
    private static final Insets dataRowInsets = new Insets(2,2,2,2);
    private GridBagLayout rootLayout;

    private JPanel filler;

    Map<String, JLabel> sectionLabels = new LinkedHashMap<>();
    Map<String, JSeparator> separators = new HashMap<>();
    Map<String, Map<String, JLabel[]>> dataValues = new LinkedHashMap<>();

    public ReportDisplayPanel() {
        super();
    }
    public ReportDisplayPanel(ReportDataModel dataModel) {
        super();

        setReportDataModel(dataModel);
    }

    public void setReportDataModel(ReportDataModel dataModel) {
        ReportDataChangeListener listener = new ReportDataChangeListener();
        dataModel.addPropertyChangeListener(listener);

        buildDisplay(dataModel);
    }

    private void buildDisplay(ReportDataModel dataModel) {
        setBorder(new EmptyBorder(5,5,5,5));
        rootLayout = new GridBagLayout();
        rootLayout.columnWidths = new int[]{LABEL_COLUMN_WIDTH,CONTROL_COLUMN_WIDTH};
        setLayout(rootLayout);

        for (String sectionName : dataModel.getSections()) {
            buildSection(sectionName, dataModel);
        }

        //filler = addVerticalFiller();
    }

    private void buildSection(String sectionName, ReportDataModel data) {
        Map<String, JLabel[]> section = makeSection(sectionName);


        for (String dataField : data.getDataFieldsInSection(sectionName)) {
            String dataValue = data.getDataFieldValue(sectionName, dataField);
            JLabel[] dataValueDisplay = buildDataRow(dataField, dataValue);
            section.put(dataField, dataValueDisplay);
        }
    }

    private Map<String, JLabel[]> makeSection(String sectionName) {
        JLabel label = addSectionHeader(sectionName);
        JSeparator separator = addHorizontalSeparator();

        sectionLabels.put(sectionName, label);
        separators.put(sectionName, separator);

        Map<String, JLabel[]> section = new LinkedHashMap<>();
        dataValues.put(sectionName, section);

        return section;
    }

    private JLabel[] buildDataRow(String label, String value) {
        return addDataFieldDisplay(label, value);
    }

    public class ReportDataChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            String changedProperty = event.getPropertyName();

            String[] fieldNames = changedProperty.split(ReportDataModel.SECTION_FIELD_DELIM);
            String sectionName = fieldNames[0];
            String newValue = event.getNewValue().toString();

            if (fieldNames.length > 1) {
                // Section and Data Field present
                String dataField = fieldNames[1];

                if (!dataValues.containsKey(sectionName)) {
                    //The section does not exist, make it
                    makeSection(sectionName);
                }

                Map<String, JLabel[]> section = dataValues.get(sectionName);

                if (section.containsKey(dataField)) {
                    // field exists, update it
                    JLabel valueField = section.get(dataField)[1];
                    valueField.setText(newValue);
                } else {
                    // field does not exist, make a new one
                    JLabel[] dataFieldDisplays = addDataFieldDisplay(dataField, newValue);
                    section.put(dataField, dataFieldDisplays);
                }
            } else {
                // Section only
                if (dataValues.containsKey(sectionName)) {
                    // Section already exists, remove it first

                    Map<String, JLabel[]> section = dataValues.get(sectionName);
                    for (JLabel[] fields : section.values()) {
                        for (JLabel field : fields) {
                            ReportDisplayPanel.this.remove(field);
                        }
                    }

                    dataValues.remove(sectionName);
                    sectionLabels.remove(sectionName);
                    separators.remove(sectionName);
                }

                buildSection(sectionName, (ReportDataModel)event.getSource());
            }

        }
    }

    private void addComponent(Component component, GridBagConstraints c){
        rootLayout.setConstraints(component,c);
        add(component);
    }

    private JLabel[] addDataFieldDisplay(String label, String initialValue){
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = activeRow;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = dataRowInsets;
        c.anchor = GridBagConstraints.WEST;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setHorizontalAlignment(SwingConstants.LEFT);
        labelComponent.setHorizontalTextPosition(SwingConstants.LEFT);
        labelComponent.setVerticalAlignment(SwingConstants.CENTER);
        addComponent(labelComponent,c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = activeRow;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = dataRowInsets;
        c.anchor = GridBagConstraints.EAST;
        JLabel valueComponent = new JLabel(initialValue);
        valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
        valueComponent.setHorizontalTextPosition(SwingConstants.RIGHT);
        valueComponent.setVerticalAlignment(SwingConstants.CENTER);
        addComponent(valueComponent,c);

        activeRow++;


        return new JLabel[] { labelComponent, valueComponent};
    }

    private JLabel addSectionHeader(String label) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = activeRow;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = sectionInsets;
        c.anchor = GridBagConstraints.EAST;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setHorizontalAlignment(SwingConstants.LEFT);
        labelComponent.setHorizontalTextPosition(SwingConstants.LEFT);
        labelComponent.setVerticalAlignment(SwingConstants.CENTER);
        Font font = labelComponent.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        labelComponent.setFont(boldFont);
        addComponent(labelComponent,c);

        activeRow++;

        return labelComponent;
    }

    private JSeparator addHorizontalSeparator() {
        GridBagConstraints c = new GridBagConstraints();
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = activeRow;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = separatorInsets;
        c.anchor = GridBagConstraints.EAST;
        JSeparator separator = new JSeparator();
        addComponent(separator, c);

        activeRow++;

        return separator;
    }
    private JPanel addVerticalFiller(){
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = activeRow;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = dataRowInsets;
        c.anchor = GridBagConstraints.NORTHWEST;
        JPanel filler = new JPanel();
        addComponent(filler,c);

        activeRow++;

        return filler;
    }

}
