/******************************************
 Copyright 2022 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/
package com.nuix.nx;

import com.nuix.nx.controls.DynamicTableControl;
import com.nuix.nx.controls.models.ChoiceTableModelChangeListener;
import com.nuix.nx.controls.models.DynamicTableModel;
import com.nuix.nx.controls.models.DynamicTableValueCallback;
import com.nuix.nx.dialogs.CustomTabPanel;
import com.nuix.nx.dialogs.TabbedCustomDialog;
import com.nuix.nx.controls.models.ReportDataModel;
import com.nuix.nx.dialogs.ProgressDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DialogTester {

    static class MyTableModel extends DynamicTableModel {
        private static class Callback implements DynamicTableValueCallback {

            @Override
            public Object interact(Object row, int column, boolean setValue, Object aValue) {
                Object[] rowArray = (Object[]) row;

                if ( 2 == column ) {
                    ImageIcon icon = new ImageIcon((URL)rowArray[2]);
                    return icon;
                }
                else return ((Object[])row)[column];
            }
        }
        public MyTableModel() {
            super(getHeaders(), getData(), new Callback(), false);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (0 == column) return Boolean.class;
            if (1 == column) return String[].class;
            if (2 == column) return Double[].class;
            if (3 == column) return Icon.class;
            return String.class;
        }



        public static List<String> getHeaders() { return List.of("Text", "Number", "Image"); }
        public static List<Object> getData() {
            return List.of(
                new Object[]{new String[]{"Accept", "OK", "Proceed"}, new Double[]{1.0, 1.1, 1.2}, DialogTester.class.getResource("/com/nuix/nx/accept.png")},
                new Object[]{new String[]{"Add", "Append"}, new Double[]{2.0, 2.3}, DialogTester.class.getResource("/com/nuix/nx/add.png")},
                new Object[]{new String[]{"Cancel"}, new Double[]{3.0}, DialogTester.class.getResource("/com/nuix/nx/cancel.png")}
            );
        }
    }

    private void runTest() throws Exception {
        LookAndFeelHelper.setWindowsIfMetal();

        TabbedCustomDialog tcd = new TabbedCustomDialog("Test Dialog");

        CustomTabPanel ctp = tcd.addTab("a", "TEST");
        MyTableModel mtm = new MyTableModel();
        ctp.appendDynamicTable("sample", "Display Table",
                MyTableModel.getHeaders(), MyTableModel.getData(), null);
        final DynamicTableControl dtc = (DynamicTableControl) ctp.getControl("sample");
        dtc.setTableModel(mtm);

        final JButton okButton = (JButton)(((Container)tcd.getContentPane().getComponent(1)).getComponent(0));
        okButton.setEnabled(false);
        final ChoiceTableModelChangeListener changeListener = mtm.getChangeListener();
        mtm.setChangeListener(new ChoiceTableModelChangeListener() {
            @Override
            public void dataChanged() {
                if (null != changeListener) changeListener.dataChanged();

                if (dtc.getCheckedRecords().size() > 0) {
                    okButton.setEnabled(true);
                } else {
                    okButton.setEnabled(false);
                }
            }

            @Override
            public void structureChanged() {
                if (null != changeListener) changeListener.structureChanged();
            }
        });

        dtc.setTableCellRenderer(String[].class, new AbsListTableRenderer() {
            @Override
            protected String[] generateDisplayableText(Object source) {
                return (String[])source;
            }
        });

        dtc.setTableCellRenderer(Double[].class, new AbsListTableRenderer(){
            final NumberFormat displayFormat = NumberFormat.getInstance();

            {
                displayFormat.setMaximumFractionDigits(4);
                displayFormat.setMaximumIntegerDigits(2);
                displayFormat.setMinimumFractionDigits(4);
                displayFormat.setMinimumIntegerDigits(1);
            }

            @Override
            protected String[] generateDisplayableText(Object source) {
                Double[] similarities = (Double[]) source;

                List<String> formattedSimilarities = new LinkedList<>();
                for(Double similarity : similarities) {
                    formattedSimilarities.add(displayFormat.format(similarity));
                }

                return formattedSimilarities.toArray(new String[0]);
            }
        });

        dtc.setTableCellRenderer(Icon.class, new TableCellRenderer() {
            JLabel viewPort = new JLabel();

            {
                viewPort.setBackground(Color.WHITE);
                viewPort.setForeground(Color.BLACK);
                viewPort.setMinimumSize(new Dimension(100, 100));
                viewPort.setPreferredSize(new Dimension(100, 100));
                viewPort.setSize(new Dimension(100, 100));
                viewPort.setOpaque(true);
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Icon icon = (Icon)value;
                viewPort.setIcon(icon);

                if (isSelected) {
                    viewPort.setBackground(new Color(0, 120, 215));
                    viewPort.setForeground(Color.WHITE);
                } else {
                    viewPort.setBackground(Color.WHITE);
                    viewPort.setForeground(Color.BLACK);
                }

                int currentHeight = table.getRowHeight();
                int iconHeight = icon.getIconHeight();

                if(currentHeight < 5* iconHeight) table.setRowHeight(5 * iconHeight);

                return viewPort;
            }
        });

        tcd.display();
    }

    public static void main(String[] arg) throws Exception {
        DialogTester dt = new DialogTester();
        dt.runTest();
    }

    static abstract class AbsListTableRenderer extends JPanel implements TableCellRenderer {

        LinkedList<JLabel> labelCache = new LinkedList<>();
        LinkedList<JLabel> labelsInUse = new LinkedList<>();

        LinkedList<GridBagConstraints> constraintsCache = new LinkedList<>();
        LinkedList<GridBagConstraints> constraintsInUse = new LinkedList<>();

        Border labelBorder = new LineBorder(Color.LIGHT_GRAY, 1);

        public AbsListTableRenderer() {
            super(new GridBagLayout());

            // Start with one item in each of the caches
            labelCache.add(createJLabel());
            constraintsCache.add(createConstraints());
        }

        protected abstract String[] generateDisplayableText(Object source);


        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object source,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {

            clearLabels(labelsInUse);
            clearConstraints(constraintsInUse);

            String[] displayableText = generateDisplayableText(source);
            LinkedList<JLabel> labels = getLabelsForText(displayableText);
            labelsInUse = labels;

            LinkedList<GridBagConstraints> constraints = getConstraintsForText(displayableText);
            constraintsInUse = constraints;

            Iterator<GridBagConstraints> constraintsIterator = constraints.iterator();
            for(JLabel label : labels) {
                GridBagConstraints constraint = constraintsIterator.next();
                if (isSelected) {
                    label.setBackground(new Color(0, 120, 215));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.BLACK);
                }
                this.add(label, constraint);
            }

            return this;
        }

        private LinkedList<JLabel> getLabelsForText(String[] texts) {
            LinkedList<JLabel> inUse = new LinkedList<>();

            for(String text : texts) {
                if (labelCache.isEmpty()) {
                    labelCache.add(createJLabel());
                }

                JLabel label = labelCache.remove();
                label.setText(text);
                inUse.add(label);
            }

            return  inUse;
        }

        private LinkedList<GridBagConstraints> getConstraintsForText(String[] texts) {
            LinkedList<GridBagConstraints> inUse = new LinkedList<>();

            int row = 0;
            for(int i = 0; i < texts.length; i++) {
                if (constraintsCache.isEmpty()) {
                    constraintsCache.add(createConstraints());
                }

                GridBagConstraints constraints = constraintsCache.remove();
                constraints.gridy = row++;
                inUse.add(constraints);
            }

            return inUse;
        }

        private void clearLabels(LinkedList<JLabel> activeLabels) {
            while(!activeLabels.isEmpty()) {
                JLabel label = activeLabels.remove();
                this.remove(label);
                label.setText("");
                labelCache.add(label);
            }
        }

        private void clearConstraints(LinkedList<GridBagConstraints> activeConstraints) {
            while(!activeConstraints.isEmpty()) {
                GridBagConstraints constraints = activeConstraints.remove();
                constraintsCache.add(constraints);
            }
        }

        private JLabel createJLabel() {
            JLabel label = new JLabel();

            label.setBorder(labelBorder);
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
            label.setOpaque(true);

            return label;
        }

        private GridBagConstraints createConstraints() {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 0.5;
            constraints.weighty = 0.5;
            constraints.gridx = 0;
            constraints.insets = new Insets(-1,-1, -1,-1);

            return constraints;
        }

    }

    static class DelegatingChoiceChangeListener implements ChoiceTableModelChangeListener {
        private final ChoiceTableModelChangeListener toSendTo;

        public DelegatingChoiceChangeListener(ChoiceTableModelChangeListener alsoCall) {
            System.out.println("FUCK YOU");
            this.toSendTo = alsoCall;
        }

        @Override
        public void dataChanged() {
            if (null != toSendTo) {
                toSendTo.dataChanged();
            }

            System.out.println("Table Model Changed");
        }

        @Override
        public void structureChanged() {
            if(null != toSendTo) {
                toSendTo.structureChanged();
            }

            System.out.println("Table Model Structure Changed");
        }
    }
}
