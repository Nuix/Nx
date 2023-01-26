/******************************************
 Copyright 2022 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/
package com.nuix.nx;

import com.nuix.nx.controls.filters.DynamicTableFilterProvider;
import com.nuix.nx.controls.models.DoubleBoundedRangeModel;
import com.nuix.nx.controls.models.ReportDataModel;
import com.nuix.nx.dialogs.CustomTabPanel;
import com.nuix.nx.dialogs.ProgressDialog;
import com.nuix.nx.dialogs.TabbedCustomDialog;

import javax.swing.*;
import java.util.*;

import static org.junit.Assert.*;

public class DialogTester {

  private void runProgressTest() throws Exception {
        LookAndFeelHelper.setWindowsIfMetal();
        ProgressDialog.forBlock((pd) -> {
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

    public void runCustomDialogTest() {
        TabbedCustomDialog dialog = new TabbedCustomDialog("This is a test");
        CustomTabPanel panel = dialog.addTab("a_tab", "This is a tab");

        try {
            panel.appendSlider("a_slider", "A Slider", 0.2, 0.2, 1.0);
            panel.appendSlider("b_slider", "B Slider");
            panel.appendSlider("c_slider", "C Slider", 0.2, 0.2, 100.0);
        } catch (Exception e) {
            fail();
        }

        JSlider slider = (JSlider)panel.getControl("a_slider");
        DoubleBoundedRangeModel model = (DoubleBoundedRangeModel) slider.getModel();
        assertEquals(0.2, model.getValueAsDouble(), .00001);

        model.setValue(.55);
        dialog.display();

        model.setValue(0.24);
        assertEquals(0.24, ((Number)dialog.toMap().get("a_slider")).doubleValue(), .00001);

    }

    public void runFilterTests() {
      List<DynamicTableFilterProvider> filters = new ArrayList<>();

      filters.add(new DynamicTableFilterProvider() {
            @Override
            public boolean handlesExpression(String filterExpression) {
                return true;
            }

            @Override
            public boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression, Object record, Map<String, Object> rowValues) {
                boolean result = (sourceIndex % 3) > 0;
                System.out.printf("`(%2d %% 3) > 0`: %b%n", sourceIndex, result);
                return result;
            }
        });

        filters.add(new DynamicTableFilterProvider() {
            @Override
            public boolean handlesExpression(String filterExpression) {
                return true;
            }

            @Override
            public boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression, Object record, Map<String, Object> rowValues) {
                boolean result = (sourceIndex % 2) == 1;
                System.out.printf("`(%2d %% 2) == 1`: %b%n", sourceIndex, result);
                return result;
            }
        });

        filters.add(new DynamicTableFilterProvider() {
            @Override
            public boolean handlesExpression(String filterExpression) {
                return true;
            }

            @Override
            public boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression, Object record, Map<String, Object> rowValues) {
                boolean result = sourceIndex > 1 && sourceIndex < 8;
                System.out.printf("`%2d > 1 && sourceIndex < 8`: %b%n", sourceIndex, result);
                return result;
            }
        });

        int[] id = {0};
        List<Integer> keepThese = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            id[0] = i;
            Optional results = filters.stream().filter(filter -> !filter.keepRecord(id[0], true, "", null, null)).findFirst();
            if(results.isEmpty()) {
                keepThese.add(i);
            }
        }
        keepThese.forEach(value -> System.out.printf("Keep [%2d]%n", value));
    }

    public static void main(String[] arg) throws Exception {
        DialogTester dt = new DialogTester();
        //dt.runProgressTest();
        //dt.runCustomDialogTest();
        dt.runFilterTests();
    }

}
