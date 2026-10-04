package com.kovak.aura.ui.widget;

import com.kovak.aura.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {
    private final String name;
    private int x, y;
    private boolean expanded = true;
    private boolean scrollable = false;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private final List<ModuleButton> buttons = new ArrayList<>();

    public static final int WIDTH = 170;
    public static final int HEADER_HEIGHT = 18;
    public static final int MAX_CONTENT_HEIGHT = 260;

    public CategoryPanel(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public void addModule(Module module) {
        buttons.add(new ModuleButton(module, this));
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;

        // Header with accent
        ctx.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, 0xFF12121C);
        ctx.fill(x, y, x + 3, y + HEADER_HEIGHT, 0xFF00E5B0);

        String label = (expanded ? "▼ " : "▶ ") + name;
        ctx.drawTextWithShadow(tr, label, x + 8, y + 5, 0xFF00E5B0);

        if (!expanded) return;

        int contentHeight = 0;
        for (ModuleButton b : buttons) contentHeight += b.getHeight();

        scrollable = contentHeight > MAX_CONTENT_HEIGHT;
        maxScroll = Math.max(0, contentHeight - MAX_CONTENT_HEIGHT);

        // Clip region — Minecraft doesn't support scissor natively in DrawContext easily,
        // so we render every button but skip those outside visible window.
        int visibleTop = y + HEADER_HEIGHT;
        int visibleBottom = visibleTop + MAX_CONTENT_HEIGHT;

        int cy = visibleTop - scrollOffset;
        for (ModuleButton b : buttons) {
            b.setX(x);
            b.setY(cy);
            if (cy + b.getHeight() > visibleTop && cy < visibleBottom) {
                b.render(ctx, mouseX, mouseY, delta);
            }
            cy += b.getHeight();
        }

        // Background frame for the panel body
        ctx.fill(x - 1, visibleTop, x + WIDTH + 1, visibleBottom, 0x22000000);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (mx < x || mx > x + WIDTH) return false;

        // Header toggle
        if (my >= y && my <= y + HEADER_HEIGHT) {
            if (button == 1) { expanded = !expanded; return true; }
        }

        if (!expanded) return false;

        int visibleTop = y + HEADER_HEIGHT;
        int visibleBottom = visibleTop + MAX_CONTENT_HEIGHT;
        if (my < visibleTop || my > visibleBottom) return false;

        for (ModuleButton b : buttons) {
            if (b.mouseClicked(mx, my, button)) return true;
        }
        return false;
    }

    public void onRelease(double mx, double my, int button) {
        for (ModuleButton b : buttons) b.onRelease(mx, my, button);
    }

    public void mouseDragged(double mx, double my, int button) {
        for (ModuleButton b : buttons) b.mouseDragged(mx, my, button);
    }

    public boolean mouseScrolled(double mx, double my, double amount) {
        if (mx < x || mx > x + WIDTH) return false;
        if (!expanded || !scrollable) return false;
        int visibleTop = y + HEADER_HEIGHT;
        int visibleBottom = visibleTop + MAX_CONTENT_HEIGHT;
        if (my < visibleTop || my > visibleBottom) return false;

        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - amount * 12));
        return true;
    }

    public boolean onKeyPressed(int key) {
        for (ModuleButton b : buttons) {
            if (b.onKeyPressed(key)) return true;
        }
        return false;
    }

    public String getHoveredTooltip(int mx, int my) {
        for (ModuleButton b : buttons) {
            String tip = b.getTooltip(mx, my);
            if (tip != null) return tip;
        }
        return null;
    }

    public boolean isHeaderHovered(double mx, double my) {
        return mx >= x && mx <= x + WIDTH && my >= y && my <= y + HEADER_HEIGHT;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
            }
