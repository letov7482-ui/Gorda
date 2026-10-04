package com.kovak.aura.ui.widget;

import com.kovak.aura.AuraClient;
import com.kovak.aura.module.Module;
import com.kovak.aura.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    private final Module module;
    private final CategoryPanel parent;
    private int x, y;
    private boolean expanded = false;

    private final List<SettingWidget> widgets = new ArrayList<>();
    private SettingWidget listeningWidget;

    public static final int BASE_HEIGHT = 15;

    public ModuleButton(Module module, CategoryPanel parent) {
        this.module = module;
        this.parent = parent;
        for (Setting<?> s : module.settings) {
            widgets.add(SettingWidget.create(s, this));
        }
    }

    public int getHeight() {
        int h = BASE_HEIGHT;
        if (expanded) {
            for (SettingWidget w : widgets) h += w.getHeight();
        }
        return h;
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;

        boolean hovered = isHovered(mouseX, mouseY);
        boolean enabled = module.isEnabled();

        int bg;
        if (enabled) bg = 0xFF0A3D2E;
        else if (hovered) bg = 0xFF1E1E2C;
        else bg = 0xFF141420;

        ctx.fill(x, y, x + CategoryPanel.WIDTH, y + BASE_HEIGHT, bg);

        if (enabled) {
            ctx.fill(x, y, x + 2, y + BASE_HEIGHT, 0xFF00E5B0);
        }

        int textColor = enabled ? 0xFF00E5B0 : 0xFFDDDDDD;
        ctx.drawTextWithShadow(tr, module.getName(), x + 8, y + 4, textColor);

        // expand indicator
        String indicator = expanded ? "-" : "+";
        ctx.drawTextWithShadow(tr, indicator, x + CategoryPanel.WIDTH - 12, y + 4, 0xFF888888);

        if (expanded) {
            int cy = y + BASE_HEIGHT;
            for (SettingWidget w : widgets) {
                w.setX(x);
                w.setY(cy);
                w.render(ctx, mouseX, mouseY, delta);
                cy += w.getHeight();
            }
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (mx < x || mx > x + CategoryPanel.WIDTH) return false;

        if (my >= y && my <= y + BASE_HEIGHT) {
            if (button == 0) {
                module.toggle();
                return true;
            }
            if (button == 1) {
                expanded = !expanded;
                return true;
            }
        }

        if (expanded) {
            for (SettingWidget w : widgets) {
                if (w.mouseClicked(mx, my, button)) {
                    if (w.isListening()) listeningWidget = w;
                    return true;
                }
            }
        }
        return false;
    }

    public void onRelease(double mx, double my, int button) {
        for (SettingWidget w : widgets) w.onRelease(mx, my, button);
    }

    public void mouseDragged(double mx, double my, int button) {
        for (SettingWidget w : widgets) w.mouseDragged(mx, my, button);
    }

    public boolean onKeyPressed(int key) {
        if (listeningWidget != null) {
            boolean handled = listeningWidget.onKeyPressed(key);
            if (handled) listeningWidget = null;
            return true;
        }
        return false;
    }

    public String getTooltip(int mx, int my) {
        if (isHovered(mx, my) && !expanded) return module.getDescription();
        return null;
    }

    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + CategoryPanel.WIDTH
                && my >= y && my <= y + BASE_HEIGHT;
    }

    public Module getModule() { return module; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
