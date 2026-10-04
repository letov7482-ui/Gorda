package com.kovak.aura.util;

import com.kovak.aura.AuraClient;
import com.kovak.aura.module.combat.Aura;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class TargetUtils {

    public static LivingEntity findBest(MinecraftClient mc, Aura aura) {
        if (mc.player == null || mc.world == null) return null;

        float range = aura.range.getFloat();
        Box searchBox = mc.player.getBoundingBox().expand(range + 1.0);

        List<LivingEntity> candidates = mc.world.getEntitiesByClass(
                LivingEntity.class, searchBox, e -> isValid(mc, e, aura));

        return candidates.stream()
                .min(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e)))
                .orElse(null);
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
        if (entity.hurtTime > 0 && aura.hurtTimeCheck.getValue()) return false;

        if (entity instanceof PlayerEntity player) {
            if (player.isCreative() || player.isSpectator()) return false;
            if (AuraClient.friendManager != null && AuraClient.friendManager.isFriend(player)) return false;
            if (AuraClient.moduleManager.getModule(com.kovak.aura.module.misc.AntiBot.class).isEnabled()
                    && com.kovak.aura.module.misc.AntiBot.isBot(player)) return false;
        } else if (aura.onlyPlayers.getValue()) {
            return false;
        }

        // FOV check for legit mode
        if (aura.mode.is("Legit")) {
            float fov = aura.fov.getFloat();
            if (fov < 360f) {
                double angle = getAngleTo(mc.player, entity);
                if (angle > fov / 2f) return false;
            }
        }

        // Reach check
        double dist = mc.player.getEyePos().distanceTo(entity.getBoundingBox().getCenter());
        if (dist > aura.range.getFloat()) return false;

        return true;
    }

    private static double getAngleTo(PlayerEntity from, Entity target) {
        Vec3d eyes = from.getEyePos();
        Vec3d targetPos = target.getBoundingBox().getCenter();
        double dx = targetPos.x - eyes.x;
        double dz = targetPos.z - eyes.z;
        float yawToTarget = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        return Math.abs(net.minecraft.util.math.MathHelper.wrapDegrees(yawToTarget - from.getYaw()));
    }
}
