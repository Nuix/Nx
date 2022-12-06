/******************************************
 Copyright 2022 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/
package com.nuix.nx;

import com.nuix.nx.controls.ReportDisplayPanel;
import com.nuix.nx.controls.models.ReportDataModel;
import com.nuix.nx.dialogs.ProgressDialog;

import java.util.LinkedHashMap;
import java.util.Map;

public class DialogTester {

    private void runTest() {
        LookAndFeelHelper.setWindowsIfMetal();
        ProgressDialog.forBlock((ProgressDialog pd) -> {
            ReportDataModel rdm = new ReportDataModel();

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

            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) { /* Don't Care */ }

                rdm.updateData("Summary Statistics", "Count", i+1);

                pd.setMainProgress(i+1, 100);
            }
        });

    }

    public static void main(String[] arg) {
        DialogTester dt = new DialogTester();
        dt.runTest();
    }
}
