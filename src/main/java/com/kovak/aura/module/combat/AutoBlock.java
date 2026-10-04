package com.kovak.aura.module.combat;

import com.kovak.aura.AuraClient;
import com.kovak.aura.module.Category;
import com.kovak.aura.module.Module;
import com.kovak.aura.setting.BooleanSetting;
import com.kovak.aura.setting.NumberSetting;
import com.kovak.aura.util.RotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

public class AutoBlock extends Module {

    public final NumberSetting range = new NumberSetting("Range", 3.0, 1.0, 6.0, 0.5);
    public final BooleanSetting requireShield = new BooleanSetting("Require Shield", true);
    public final BooleanSetting visualOnly = new BooleanSetting("Visual Only", false);

    private boolean blocking;

    public AutoBlock() {
        super("AutoBlock", "Automatically blocks with shield when enemy is nearby.", Category.COMBAT);
        settings.add(range);
        settings.add(requireShield);
        settings.add(visualOnly);
    }

    @Override
    public void onDisable() {
        blocking = false;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Aura aura = AuraClient.moduleManager.getModule(Aura.class);
        LivingEntity target = aura != null && aura.isEnabled() ? aura.getTarget() : null;
        boolean threat = target != null
                && mc.player.distanceTo(target) <= range.getFloat();

        if (requireShield.getValue() && !(mc.player.getOffHandStack().getItem() instanceof ShieldItem)) {
            return;
        }

        if (threat && !blocking) {
            blocking = true;
            mc.options.useKey.setPressed(true);
            if (!visualOnly.getValue()) {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            }
        } else if (!threat && blocking) {
            blocking = false;
            mc.options.useKey.setPressed(false);
            if (!visualOnly.getValue()) {
                mc.interactionManager.stopUsingItem(mc.player);
            }
        }
    }
}
