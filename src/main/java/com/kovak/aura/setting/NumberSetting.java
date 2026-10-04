package com.kovak.aura.setting;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    public int getInt() { return getValue().intValue(); }
    public float getFloat() { return getValue().floatValue(); }

    @Override
    public void setValue(Double value) {
        double clamped = Math.max(min, Math.min(max, value));
        double rounded = Math.round(clamped / step) * step;
        super.setValue(rounded);
    }

    @Override
    public SettingType getType() { return SettingType.NUMBER; }
}
