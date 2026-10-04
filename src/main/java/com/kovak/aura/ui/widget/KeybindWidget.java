package com.kovak.aura.ui.widget;

import com.kovak.aura.event.KeyEventHandler;
import com.kovak.aura.setting.KeybindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class KeybindWidget extends SettingWidget {
    private final KeybindSetting keySetting;

    public KeybindWidget(KeybindSetting s, ModuleButton parent) {
        super(s, parent);
        this.keySetting = s;
    }

    @Override
    public int getHeight() { return 14; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;

        boolean hovered = mouseX >= x + 4 && mouseX <= x + CategoryPanel.WIDTH - 4
                && mouseY >= y && mouseY <= y + getHeight();

        int bg = listening ? 0xFF3D2A0A : (hovered ? 0xFF20202C : 0xFF181822);
        ctx.fill(x + 4, y, x + CategoryPanel.WIDTH - 4, y + getHeight() - 1, bg);

        String keyName = listening ? "[...]" : KeyEventHandler.keyName(keySetting.getValue());
        String display = keySetting.getName() + ": §b" + keyName;
        ctx.drawTextWithShadow(tr, display, x + 8, y + 2, 0xFFDDDDDD);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx >= x + 4 && mx <= x + CategoryPanel.WIDTH - 4
                && my >= y && my <= y + getHeight()) {
            if (button == 0) { listening = true; return true; }
            if (button == 1) { keySetting.setValue(-1); return true; }
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(int key) {
        if (listening) {
            if (key == 256) keySetting.setValue(-1);
            else keySetting.setValue(key);
            listening = false;
            return true;
        }
        return false;
    }
}
