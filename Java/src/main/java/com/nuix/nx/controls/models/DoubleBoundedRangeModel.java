package com.nuix.nx.controls.models;

import javax.swing.*;

public class DoubleBoundedRangeModel extends DefaultBoundedRangeModel {
    private int digitsToMaintain;

    public DoubleBoundedRangeModel(double value, double extent, double min, double max, int digitsToMaintain) {
        super(convertToInt(value, digitsToMaintain),
                convertToInt(extent, digitsToMaintain),
                convertToInt(min, digitsToMaintain),
                convertToInt(max, digitsToMaintain));
        this.digitsToMaintain = digitsToMaintain;
    }

    public void setExtent(double extent) {
        super.setExtent(convertToInt(extent, digitsToMaintain));
    }

    public void setMaximum(double max) {
        super.setMinimum(convertToInt(max, digitsToMaintain));
    }

    public void setMinimum(double min) {
        super.setMinimum(convertToInt(min, digitsToMaintain));
    }

    public void setValue(double value) {
        super.setValue(convertToInt(value, digitsToMaintain));
    }

    public double getExtentAsDouble() {
        return convertToDouble(getExtent(), digitsToMaintain);
    }

    public double getMaximumAsDouble() {
        return convertToDouble(getMaximum(), digitsToMaintain);
    }

    public double getMinimumAsDouble() {
        return convertToDouble(getMinimum(), digitsToMaintain);
    }

    public double getValueAsDouble() {
        return convertToDouble(getValue(), digitsToMaintain);
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
