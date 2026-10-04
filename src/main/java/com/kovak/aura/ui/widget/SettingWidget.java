package com.kovak.aura.ui.widget;

import com.kovak.aura.setting.*;

public abstract class SettingWidget {
    protected final Setting<?> setting;
    protected final ModuleButton parent;
    protected int x, y;
    protected boolean listening = false;

    protected SettingWidget(Setting<?> setting, ModuleButton parent) {
        this.setting = setting;
        this.parent = parent;
    }

    public static SettingWidget create(Setting<?> setting, ModuleButton parent) {
        return switch (setting.getType()) {
            case BOOLEAN -> new CheckboxWidget((BooleanSetting) setting, parent);
            case NUMBER -> new SliderWidget((NumberSetting) setting, parent);
            case MODE -> new DropdownWidget((ModeSetting) setting, parent);
            case KEYBIND -> new KeybindWidget((KeybindSetting) setting, parent);
        };
    }

    public abstract int getHeight();
    public abstract void render(net.minecraft.client.gui.DrawContext ctx,
                                 int mouseX, int mouseY, float delta);
    public abstract boolean mouseClicked(double mx, double my, int button);
    public void onRelease(double mx, double my, int button) {}
    public void mouseDragged(double mx, double my, int button) {}
    public boolean onKeyPressed(int key) { return false; }

    public boolean isListening() { return listening; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
