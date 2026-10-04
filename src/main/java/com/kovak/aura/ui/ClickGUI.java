package com.kovak.aura.ui;

import com.kovak.aura.AuraClient;
import com.kovak.aura.module.Category;
import com.kovak.aura.ui.widget.CategoryPanel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {

    private final List<CategoryPanel> panels = new ArrayList<>();
    private CategoryPanel draggingPanel;
    private int dragOffX, dragOffY;

    public ClickGUI() {
        super(Text.literal("Aura Client"));
    }

    @Override
    protected void init() {
        panels.clear();
        int x = 20;
        int y = 40;
        int spacing = 175;

        for (Category cat : Category.values()) {
            var mods = AuraClient.moduleManager.getModulesByCategory(cat);
            if (mods.isEmpty()) continue;

            CategoryPanel panel = new CategoryPanel(cat.getDisplayName(), x, y);
            mods.forEach(panel::addModule);
            panels.add(panel);

            x += spacing;
            if (x + 170 > width - 20) {
                x = 20;
                y += 200;
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim background
        ctx.fill(0, 0, width, height, 0xAA0A0A12);

        // Header
        ctx.drawCenteredTextWithShadow(textRenderer,
                "§b§lAURA CLIENT §7v" + AuraClient.VERSION,
                width / 2, 12, 0xFFFFFFFF);

        // Panels
        for (CategoryPanel p : panels) {
            p.render(ctx, mouseX, mouseY, delta);
        }

        // Tooltip
        for (CategoryPanel p : panels) {
            String tip = p.getHoveredTooltip(mouseX, mouseY);
            if (tip != null) {
                ctx.drawTooltip(textRenderer, Text.literal(tip), mouseX + 8, mouseY + 8);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (CategoryPanel p : panels) {
            if (p.isHeaderHovered(mx, my) && button == 0) {
                draggingPanel = p;
                dragOffX = (int)(mx - p.getX());
                dragOffY = (int)(my - p.getY());
                panels.remove(p);
                panels.add(p);
                return true;
            }
            if (p.mouseClicked(mx, my, button)) return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingPanel != null) {
            draggingPanel.onRelease(mx, my, button);
            draggingPanel = null;
        }
        for (CategoryPanel p : panels) p.onRelease(mx, my, button);
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingPanel != null) {
            draggingPanel.setX((int)(mx - dragOffX));
            draggingPanel.setY((int)(my - dragOffY));
            return true;
        }
        for (CategoryPanel p : panels) p.mouseDragged(mx, my, button);
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hz, double vt) {
        for (CategoryPanel p : panels) {
            if (p.mouseScrolled(mx, my, vt)) return true;
        }
        return super.mouseScrolled(mx, my, hz, vt);
    }

    public boolean onKeyPressed(int key) {
        for (CategoryPanel p : panels) {
            if (p.onKeyPressed(key)) return true;
        }
        if (key == 256) { close(); return true; }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (onKeyPressed(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
                               }
