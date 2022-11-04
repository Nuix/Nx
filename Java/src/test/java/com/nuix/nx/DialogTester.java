/******************************************
 Copyright 2022 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/
package com.nuix.nx;

import com.nuix.nx.controls.DynamicTableControl;
import com.nuix.nx.controls.ReportDisplayPanel;
import com.nuix.nx.controls.models.DynamicTableModel;
import com.nuix.nx.controls.models.DynamicTableValueCallback;
import com.nuix.nx.controls.models.ReportDataModel;
import com.nuix.nx.dialogs.CustomTabPanel;
import com.nuix.nx.dialogs.ProgressDialog;
import com.nuix.nx.dialogs.TabbedCustomDialog;

import javax.swing.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DialogTester {

    static class MyTableModel extends DynamicTableModel {
        private static class Callback implements DynamicTableValueCallback {

            @Override
            public Object interact(Object row, int column, boolean setValue, Object aValue) {
                Object[] rowArray = (Object[]) row;

                if ( 1 == column ) {
                    return ((Number)rowArray[1]).intValue();
                } else if ( 2 == column ) {
                    ImageIcon icon = new ImageIcon((URL)rowArray[2]);
                    return icon;
                }
                else return ((Object[])row)[column].toString();
            }
        }
        public MyTableModel() {
            super(getHeaders(), getData(), new Callback(), false);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (0 == column) return Boolean.class;
            if (2 == column) return Number.class;
            if (3 == column) return Icon.class;
            return String.class;
        }



        public static List<String> getHeaders() { return List.of("Text", "Number", "Image"); }
        public static List<Object> getData() {
            return List.of(
                new Object[]{"Accept", 1, DialogTester.class.getResource("/com/nuix/nx/accept.png")},
                new Object[]{"Add", 2, DialogTester.class.getResource("/com/nuix/nx/add.png")},
                new Object[]{"Cancel", 3, DialogTester.class.getResource("/com/nuix/nx/cancel.png")}
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
        DynamicTableControl dtc = (DynamicTableControl) ctp.getControl("sample");
        dtc.setTableModel(mtm);

        tcd.display();
    }

    public static void main(String[] arg) throws Exception {
        DialogTester dt = new DialogTester();
        dt.runTest();
    }
}
