package com.nuix.nx.controls.models;

import javax.swing.*;

public class DoubleBoundedRangeModel extends DefaultBoundedRangeModel {
    private double min, max, extent, value;
    private int digitsToMaintain;

    public DoubleBoundedRangeModel(double value, double extent, double min, double max, int digitsToMaintain) {

    }

    private static int convertToInt(double toConvert, int digitsToMaintain) {
        double multiplier = Math.pow(10.0, digitsToMaintain);
        double results = toConvert * multiplier;
        return (int)Math.round(results);
    }

    private static double convertToDouble(int toConvert, int digitsToMaintain) {
        double divider = Math.pow(10.0, digitsToMaintain);
        return ((double)toConvert)/divider;
    }
}
