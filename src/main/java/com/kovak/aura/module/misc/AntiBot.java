package com.kovak.aura.module.misc;

import com.kovak.aura.module.Category;
import com.kovak.aura.module.Module;
import com.kovak.aura.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiBot extends Module {

    private static final Map<UUID, Integer> playerTicks = new HashMap<>();
    private static final Map<UUID, Boolean> flagged = new HashMap<>();

    public final BooleanSetting checkHover = new BooleanSetting("Check Hover", true);
    public final BooleanSetting checkAge = new BooleanSetting("Check Age", true);
    public final BooleanSetting checkTabList = new BooleanSetting("Check Tab", true);
    public final BooleanSetting checkArmor = new BooleanSetting("Check Armor", true);

    public AntiBot() {
        super("AntiBot", "Filters out fake players / NPCs.", Category.MISC);
        settings.add(checkHover);
        settings.add(checkAge);
        settings.add(checkTabList);
        settings.add(checkArmor);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            UUID id = player.getUuid();
            if (player == mc.player) continue;

            playerTicks.merge(id, 1, Integer::sum);

            boolean isBot = false;

            // Check 1: name doesn't appear in tab list
            if (checkTabList.getValue()) {
                boolean inTab = mc.getNetworkHandler() != null
                        && mc.getNetworkHandler().getPlayerList().stream()
                        .anyMatch(entry -> {
                            var profile = entry.getProfile();
                            return profile.id().equals(id);
                        });
                if (!inTab) isBot = true;
            }

            // Check 2: player is too young (only seen for <10 ticks)
            if (checkAge.getValue()) {
                Integer ticks = playerTicks.get(id);
                if (ticks != null && ticks < 10) isBot = true;
            }

            // Check 3: invalid name
            String name = player.getName().getString();
            if (name == null || name.isEmpty() || name.length() > 16) isBot = true;

            // Check 4: suspicious armor setup
            if (checkArmor.getValue()) {
                ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
                ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
                if (helmet.isEmpty() && !chest.isEmpty() && chest.getItem() == helmet.getItem()) {
                    isBot = true;
                }
            }

            flagged.put(id, isBot);
        }
    }

    public static boolean isBot(PlayerEntity player) {
        if (player == null) return false;
        return flagged.getOrDefault(player.getUuid(), false);
    }

    @Override
    public void onDisable() {
        playerTicks.clear();
        flagged.clear();
    }
}
