package com.kovak.aura.util;

import com.kovak.aura.AuraClient;
import com.kovak.aura.module.combat.Aura;
import com.kovak.aura.module.misc.AntiBot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class TargetUtils {

    public static LivingEntity findBest(MinecraftClient mc, Aura aura) {
        if (mc.player == null || mc.world == null) return null;

        float range = aura.range.getFloat();
        Box searchBox = mc.player.getBoundingBox().expand(range + 2.0);

        List<LivingEntity> candidates = mc.world.getEntitiesByClass(
                LivingEntity.class, searchBox, e -> isValid(mc, e, aura));

        if (candidates.isEmpty()) return null;

        return candidates.stream().min(buildComparator(mc, aura)).orElse(null);
    }

    public static boolean isBetter(MinecraftClient mc, LivingEntity current, LivingEntity candidate, Aura aura) {
        Comparator<LivingEntity> cmp = buildComparator(mc, aura);
        return cmp.compare(candidate, current) < 0;
    }

    private static Comparator<LivingEntity> buildComparator(MinecraftClient mc, Aura aura) {
        return switch (aura.priority.getValue()) {
            case "Health" -> Comparator.comparingDouble(e -> e.getHealth() + e.getAbsorptionAmount());
            case "Angle" -> Comparator.comparingDouble(e -> getAngleTo(mc.player, e));
            case "Armor" -> Comparator.comparingDouble(e -> -getArmorValue((PlayerEntity) e instanceof PlayerEntity p ? p : null));
            default -> Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e));
        };
    }

    public static Vec3d getHitboxCenter(Entity entity) {
        Box box = entity.getBoundingBox();
        return new Vec3d(
                (box.minX + box.maxX) * 0.5,
                (box.minY + box.maxY) * 0.85,
                (box.minZ + box.maxZ) * 0.5
        );
    }

    private static boolean isValid(MinecraftClient mc, LivingEntity entity, Aura aura) {
        if (entity == mc.player) return false;
        if (!entity.isAlive() || entity.isDead()) return false;
        if (entity.isInvulnerable()) return false;
        if (entity instanceof ArmorStandEntity) return false;

        // Target mode checks
        if (entity instanceof PlayerEntity player) {
            if (player.isCreative() || player.isSpectator()) return false;
            if (AuraClient.friendManager != null && AuraClient.friendManager.isFriend(player)) return false;

            AntiBot antibot = AuraClient.moduleManager.getModule(AntiBot.class);
            if (antibot != null && antibot.isEnabled() && AntiBot.isBot(player)) return false;
        } else if (aura.onlyPlayers.getValue()) {
            return false;
        }

        // Target mode gates
        switch (aura.targetMode.getValue()) {
            case "Only Jump" -> {
                if (entity.isOnGround()) return false;
            }
            case "Only Ground" -> {
                if (!entity.isOnGround()) return false;
            }
            case "Only Sprint" -> {
                if (!entity.isSprinting()) return false;
            }
            case "Only Crit" -> {
                if (mc.player.isOnGround() || mc.player.isTouchingWater()
                        || mc.player.isClimbing() || mc.player.hasStatusEffect(
                        net.minecraft.entity.effect.StatusEffects.BLINDNESS)) return false;
            }
        }

        // Wall check — allow through walls only if aura allows
        boolean canSee = mc.player.canSee(entity);
        double effectiveRange = canSee ? aura.range.getValue() : aura.wallsRange.getValue();
        if (!canSee && !aura.throughWalls.getValue()) return false;

        double distance = mc.player.getEyePos().distanceTo(entity.getBoundingBox().getCenter());
        if (distance > effectiveRange + 0.3) return false;

        // HurtTime check
        if (aura.hurtTimeCheck.getValue() && entity.hurtTime > 8) return false;

        return true;
    }

    private static double getAngleTo(PlayerEntity from, Entity target) {
        Vec3d eyes = from.getEyePos();
        Vec3d targetPos = target.getBoundingBox().getCenter();
        double dx = targetPos.x - eyes.x;
        double dz = targetPos.z - eyes.z;
        float yawToTarget = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        return Math.abs(MathHelper.wrapDegrees(yawToTarget - from.getYaw()));
    }

    private static int getArmorValue(PlayerEntity player) {
        if (player == null) return 0;
        int total = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty()) total += 1;
        }
        return total;
    }
                }
