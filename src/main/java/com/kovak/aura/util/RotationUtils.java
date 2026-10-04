package com.kovak.aura.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {

    private static float silentYaw;
    private static float silentPitch;
    private static boolean silentActive;

    // Post-attack jitter — prevents "rotation spike then attack" pattern
    private static float postAttackJitterYaw;
    private static float postAttackJitterPitch;

    public static float[] calculate(Entity from, Entity target, boolean predict) {
        Vec3d eyes = from.getEyePos();
        Vec3d point = TargetUtils.getHitboxCenter(target);

        if (predict && target instanceof net.minecraft.entity.LivingEntity le) {
            // Predict target movement by one tick
            Vec3d vel = new Vec3d(le.getVelocity().x, 0, le.getVelocity().z);
            point = point.add(vel.multiply(0.5));
        }

        double dx = point.x - eyes.x;
        double dy = point.y - eyes.y;
        double dz = point.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));

        // Post-attack jitter — small random deviation
        yaw += postAttackJitterYaw;
        pitch += postAttackJitterPitch;

        return new float[]{yaw, pitch};
    }

    /**
     * Human-like rotation smoothing with GCD snapping.
     * GrimAC derives sensitivity from GCD of rotation deltas across packets.
     * By snapping every value to the sensitivity grid, we make every delta
     * a multiple of the smallest human-possible increment.
     */
    public static float smooth(float current, float target, float maxStep) {
        float delta = MathHelper.wrapDegrees(target - current);
        float step = MathHelper.clamp(delta, -maxStep, maxStep);
        float result = current + step;
        return snapToGcd(result);
    }

    public static void setSilent(float yaw, float pitch, float speed) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        silentYaw = smooth(mc.player.getYaw(), yaw, speed);
        silentPitch = smooth(mc.player.getPitch(), pitch, speed);
        silentPitch = MathHelper.clamp(silentPitch, -90f, 90f);
        silentActive = true;
    }

    public static void applyPostAttackJitter() {
        postAttackJitterYaw = (float)((Math.random() - 0.5) * 0.8);
        postAttackJitterPitch = (float)((Math.random() - 0.5) * 0.5);
    }

    public static void clearJitter() {
        postAttackJitterYaw = 0;
        postAttackJitterPitch = 0;
    }

    public static float getSilentYaw() { return silentYaw; }
    public static float getSilentPitch() { return silentPitch; }
    public static boolean hasSilent() { return silentActive; }
    public static void clearSilent() { silentActive = false; }

    private static float snapToGcd(float value) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return value;
        try {
            float sens = mc.options.getMouseSensitivity().getValue().floatValue();
            float f = sens * 0.6f + 0.2f;
            float gcd = f * f * f * 8.0f * 0.15f;
            if (gcd < 0.0001f) return value;
            return Math.round(value / gcd) * gcd;
        } catch (Exception e) {
            return value;
        }
    }
}
