package com.kovak.aura.setting;

public class KeybindSetting extends Setting<Integer> {
    public KeybindSetting(String name, int defaultKey) {
        super(name, defaultKey);
    }

    public String getKeyName() {
        int key = getValue();
        if (key == -1) return "NONE";
        try {
            return org.lwjgl.glfw.GLFW.glfwGetKeyName(key, 0) != null
                    ? org.lwjgl.glfw.GLFW.glfwGetKeyName(key, 0).toUpperCase()
                    : "KEY_" + key;
        } catch (Exception e) {
            return "KEY_" + key;
        }
    }

    @Override
    public SettingType getType() { return SettingType.KEYBIND; }
}
