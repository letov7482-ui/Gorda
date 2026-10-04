package com.kovak.aura.setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() { setValue(!getValue()); }

    @Override
    public SettingType getType() { return SettingType.BOOLEAN; }
}
