package com.kovak.aura.ui.widget;

import com.kovak.aura.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class CheckboxWidget extends SettingWidget {
    private final BooleanSetting boolSetting;

    public CheckboxWidget(BooleanSetting s, ModuleButton parent) {
        super(s, parent);
        this.boolSetting = s;
    }

    @Override
    public int getHeight() { return 13; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;

        boolean hovered = mouseX >= x && mouseX <= x + CategoryPanel.WIDTH
                && mouseY >= y && mouseY <= y + getHeight();

        int bg = hovered ? 0xFF20202C : 0xFF181822;
        ctx.fill(x + 4, y, x + CategoryPanel.WIDTH - 4, y + getHeight() - 1, bg);

        int boxX = x + 8;
        int boxY = y + 3;
        ctx.fill(boxX, boxY, boxX + 7, boxY + 7, 0xFF0A0A0A);

        if (boolSetting.getValue()) {
            ctx.fill(boxX + 1, boxY + 1, boxX + 6, boxY + 6, 0xFF00E5B0);
        }

        ctx.drawTextWithShadow(tr, boolSetting.getName(), boxX + 11, y + 2, 0xFFDDDDDD);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx >= x + 4 && mx <= x + CategoryPanel.WIDTH - 4
                && my >= y && my <= y + getHeight()) {
            if (button == 0) {
                boolSetting.toggle();
                return true;
            }
        }
        return false;
    }
}
