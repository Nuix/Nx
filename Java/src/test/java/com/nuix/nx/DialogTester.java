/******************************************
 Copyright 2022 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/
package com.nuix.nx;

import com.nuix.nx.dialogs.CustomTabPanel;
import com.nuix.nx.dialogs.TabbedCustomDialog;
import com.nuix.nx.controls.models.ReportDataModel;
import com.nuix.nx.dialogs.ProgressDialog;

import java.util.LinkedHashMap;
import java.util.Map;

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

            Map<String, Object> someData = new LinkedHashMap<>();
            someData.put("Average", 0.0);
            someData.put("Count", 0);
            someData.put("Minimum", 0.0);
            someData.put("Maximum", 0.0);

            rdm.addSection("Summary Statistics", someData);

            someData = new LinkedHashMap<>();
            someData.put("Height", 12.3);
            someData.put("Width", 3);
            someData.put("Fill", false);

            rdm.addSection("Sizes", someData);


            pd.addReport(rdm);

    }

    public static void main(String[] arg) throws Exception {
        DialogTester dt = new DialogTester();
        dt.runTest();
    }

}
