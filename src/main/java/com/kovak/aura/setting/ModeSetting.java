package com.kovak.aura.setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = Arrays.asList(modes);
    }

    public List<String> getModes() { return modes; }

    public void cycle() {
        int idx = modes.indexOf(getValue());
        setValue(modes.get((idx + 1) % modes.size()));
    }

    public void cycleBack() {
        int idx = modes.indexOf(getValue());
        int next = (idx - 1 + modes.size()) % modes.size();
        setValue(modes.get(next));
    }

    public boolean is(String mode) {
        return getValue().equalsIgnoreCase(mode);
    }

    @Override
    public SettingType getType() { return SettingType.MODE; }
}
