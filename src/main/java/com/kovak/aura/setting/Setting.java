package com.kovak.aura.setting;

public abstract class Setting<T> {
    private final String name;
    protected T value;
    private final T defaultValue;
    private java.util.function.Consumer<T> onChange;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public String getName() { return name; }
    public T getValue() { return value; }
    public void setValue(T value) {
        this.value = value;
        if (onChange != null) onChange.accept(value);
    }
    public T getDefaultValue() { return defaultValue; }
    public void reset() { setValue(defaultValue); }

    public Setting<T> onChange(java.util.function.Consumer<T> cb) {
        this.onChange = cb;
        return this;
    }

    public abstract SettingType getType();

    public enum SettingType { NUMBER, BOOLEAN, MODE, KEYBIND }
}
