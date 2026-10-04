package com.kovak.aura.ui.widget;

import com.kovak.aura.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class DropdownWidget extends SettingWidget {
    private final ModeSetting modeSetting;
    private boolean open;

    public DropdownWidget(ModeSetting s, ModuleButton parent) {
        super(s, parent);
        this.modeSetting = s;
    }

    @Override
    public int getHeight() {
        int h = 15;
        if (open) h += modeSetting.getModes().size() * 12;
        return h;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;

        ctx.fill(x + 4, y, x + CategoryPanel.WIDTH - 4, y + 14, 0xFF181822);

        String label = modeSetting.getName() + ": " + modeSetting.getValue();
        ctx.drawTextWithShadow(tr, label, x + 8, y + 3, 0xFFDDDDDD);
        ctx.drawTextWithShadow(tr, open ? "▲" : "▼", x + CategoryPanel.WIDTH - 14, y + 3, 0xFF00E5B0);

        if (open) {
            int cy = y + 15;
            for (String mode : modeSetting.getModes()) {
                boolean hovered = mouseX >= x + 4 && mouseX <= x + CategoryPanel.WIDTH - 4
                        && mouseY >= cy && mouseY <= cy + 12;
                int bg = hovered ? 0xFF28283A : 0xFF1E1E2C;
                if (mode.equals(modeSetting.getValue())) bg = 0xFF0A3D2E;
                ctx.fill(x + 4, cy, x + CategoryPanel.WIDTH - 4, cy + 12, bg);
                ctx.drawTextWithShadow(tr, mode, x + 12, cy + 2, 0xFFCCCCCC);
                cy += 12;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx < x + 4 || mx > x + CategoryPanel.WIDTH - 4) return false;

        if (my >= y && my <= y + 14) {
            if (button == 0) { open = !open; return true; }
            if (button == 1) { modeSetting.cycleBack(); return true; }
            return false;
        }

        if (open) {
            int cy = y + 15;
            for (String mode : modeSetting.getModes()) {
                if (my >= cy && my <= cy + 12) {
                    modeSetting.setValue(mode);
                    open = false;
                    return true;
                }
                cy += 12;
            }
            open = false;
        }
        return false;
    }
}
