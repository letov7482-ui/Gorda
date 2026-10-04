package com.kovak.aura.ui.widget;

import com.kovak.aura.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SliderWidget extends SettingWidget {
    private final NumberSetting numSetting;
    private boolean dragging;

    public SliderWidget(NumberSetting s, ModuleButton parent) {
        super(s, parent);
        this.numSetting = s;
    }

    @Override
    public int getHeight() { return 15; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;

        double min = numSetting.getMin();
        double max = numSetting.getMax();
        double val = numSetting.getValue();
        double pct = (val - min) / (max - min);

        int trackX = x + 6;
        int trackY = y + 10;
        int trackW = CategoryPanel.WIDTH - 12;
        int trackH = 2;

        ctx.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF222230);
        ctx.fill(trackX, trackY, trackX + (int)(trackW * pct), trackY + trackH, 0xFF00E5B0);

        int knobX = trackX + (int)(trackW * pct);
        ctx.fill(knobX - 2, trackY - 3, knobX + 2, trackY + 5, 0xFF00E5B0);

        String display = numSetting.getName() + ": " + trim(val);
        ctx.drawTextWithShadow(tr, display, x + 8, y - 1, 0xFFDDDDDD);
    }

    private String trim(double v) {
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.format("%.2f", v);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && my >= y && my <= y + getHeight()) {
            dragging = true;
            updateFromMouse(mx);
            return true;
        }
        return false;
    }

    @Override
    public void onRelease(double mx, double my, int button) {
        dragging = false;
    }

    @Override
    public void mouseDragged(double mx, double my, int button) {
        if (dragging) updateFromMouse(mx);
    }

    private void updateFromMouse(double mx) {
        int trackX = x + 6;
        int trackW = CategoryPanel.WIDTH - 12;
        double pct = (mx - trackX) / trackW;
        pct = Math.max(0, Math.min(1, pct));
        double value = numSetting.getMin() + pct * (numSetting.getMax() - numSetting.getMin());
        numSetting.setValue(value);
    }
}
