package com.kovak.aura.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {

    private static float silentYaw;
    private static float silentPitch;
    private static boolean silentActive;

    private static float jitterYaw;
    private static float jitterPitch;

    public static float[] calculate(Entity from, Entity target, boolean predict) {
        Vec3d eyes = from.getEyePos();
        Vec3d point = TargetUtils.getHitboxCenter(target);

        if (predict && target instanceof LivingEntity le) {
            Vec3d vel = new Vec3d(le.getVelocity().x, 0, le.getVelocity().z);
            point = point.add(vel.multiply(0.7));
        }

        double dx = point.x - eyes.x;
        double dy = point.y - eyes.y;
        double dz = point.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));

        yaw += jitterYaw;
        pitch += jitterPitch;

        return new float[]{yaw, MathHelper.clamp(pitch, -90f, 90f)};
    }

    /**
     * Rotate from current to target by at most `speed` degrees per tick.
     * When difference is small — snap exactly to eliminate oscillation.
     */
    public static float step(float current, float target, float speed) {
        float delta = MathHelper.wrapDegrees(target - current);
        float absDelta = Math.abs(delta);

        // Snap when very close — prevents oscillation and "missing" the target
        if (absDelta < 0.5f) return snapToGcd(target);

        float move = Math.min(absDelta, speed);
        float result = current + (delta > 0 ? move : -move);
        return snapToGcd(result);
    }

    /**
     * Set target rotation for silent aim.
     * Now takes a speed parameter (deg per tick).
     */
    public static void setSilent(float currentYaw, float currentPitch, float targetYaw, float targetPitch, float speed) {
        silentYaw = step(currentYaw, targetYaw, speed);
        silentPitch = step(currentPitch, targetPitch, speed);
        silentPitch = MathHelper.clamp(silentPitch, -90f, 90f);
        silentActive = true;
    }

    public static void applyPostAttackJitter() {
        jitterYaw = (float)((Math.random() - 0.5) * 0.6);
        jitterPitch = (float)((Math.random() - 0.5) * 0.4);
    }

    public static void clearJitter() {
        jitterYaw = 0;
        jitterPitch = 0;
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
