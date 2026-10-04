package com.kovak.aura.module;

import com.kovak.aura.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int keyBind = -1;

    public final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) return;
        this.enabled = state;
        if (state) onEnable();
        else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public abstract void onTick();

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int key) { this.keyBind = key; }
}
