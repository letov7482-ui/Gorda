package com.kovak.aura.ui;

import com.kovak.aura.AuraClient;
import com.kovak.aura.module.Category;
import com.kovak.aura.ui.widget.CategoryPanel;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
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
                y += 220;
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xAA0A0A12);

        ctx.drawCenteredTextWithShadow(textRenderer,
                "§b§lAURA CLIENT §7v" + AuraClient.VERSION,
                width / 2, 12, 0xFFFFFFFF);

        for (CategoryPanel p : panels) {
            p.render(ctx, mouseX, mouseY, delta);
        }

        for (CategoryPanel p : panels) {
            String tip = p.getHoveredTooltip(mouseX, mouseY);
            if (tip != null) {
                ctx.drawTooltip(textRenderer, Text.literal(tip), mouseX + 8, mouseY + 8);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

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
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        if (draggingPanel != null) {
            draggingPanel.onRelease(mx, my, button);
            draggingPanel = null;
        }
        for (CategoryPanel p : panels) p.onRelease(mx, my, button);
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        if (draggingPanel != null) {
            draggingPanel.setX((int)(mx - dragOffX));
            draggingPanel.setY((int)(my - dragOffY));
            return true;
        }
        for (CategoryPanel p : panels) p.mouseDragged(mx, my, button);
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hz, double vt) {
        for (CategoryPanel p : panels) {
            if (p.mouseScrolled(mx, my, vt)) return true;
        }
        return super.mouseScrolled(mx, my, hz, vt);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        for (CategoryPanel p : panels) {
            if (p.onKeyPressed(key)) return true;
        }
        if (key == 256) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
