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

    // Bezier state
    private static float bezierStartYaw;
    private static float bezierStartPitch;
    private static float bezierEndYaw;
    private static float bezierEndPitch;
    private static int bezierTick;
    private static int bezierDuration;
    private static boolean bezierActive;

    public static float[] calculate(Entity from, Entity target, boolean predict, float predictAmount) {
        Vec3d eyes = from.getEyePos();
        Vec3d point = TargetUtils.getHitboxCenter(target);

        if (predict && target instanceof LivingEntity le) {
            Vec3d vel = le.getVelocity();
            // Predict horizontal movement more aggressively than vertical
            Vec3d predicted = new Vec3d(vel.x, vel.y * 0.5, vel.z).multiply(predictAmount);
            point = point.add(predicted);
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

    public static float step(float current, float target, float speed) {
        float delta = MathHelper.wrapDegrees(target - current);
        float absDelta = Math.abs(delta);
        if (absDelta < 0.5f) return snapToGcd(target);
        float move = Math.min(absDelta, speed);
        float result = current + (delta > 0 ? move : -move);
        return snapToGcd(result);
    }

    /**
     * Set silent rotation with a specific mode.
     * Modes:
     *   Smooth  — linear human-like step
     *   Instant — snap immediately to target
     *   Bezier  — smooth curve over N ticks
     *   Snap    — instant snap with GCD preservation
     */
    public static void setSilentWithMode(String mode,
                                         float currentYaw, float currentPitch,
                                         float targetYaw, float targetPitch,
                                         float speed) {
        switch (mode) {
            case "Instant", "Snap" -> {
                silentYaw = snapToGcd(targetYaw);
                silentPitch = snapToGcd(MathHelper.clamp(targetPitch, -90f, 90f));
            }
            case "Bezier" -> {
                if (!bezierActive
                        || Math.abs(MathHelper.wrapDegrees(bezierEndYaw - targetYaw)) > 5f
                        || Math.abs(bezierEndPitch - targetPitch) > 5f) {
                    bezierStartYaw = currentYaw;
                    bezierStartPitch = currentPitch;
                    bezierEndYaw = targetYaw;
                    bezierEndPitch = targetPitch;
                    bezierTick = 0;
                    bezierDuration = Math.max(2, (int)(distance3D(currentYaw, currentPitch, targetYaw, targetPitch) / Math.max(1f, speed)));
                    bezierActive = true;
                }
                bezierTick++;
                float t = Math.min(1f, (float) bezierTick / bezierDuration);
                // Ease-in-out cubic
                float eased = t < 0.5f
                        ? 4f * t * t * t
                        : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;

                silentYaw = snapToGcd(lerpAngle(bezierStartYaw, bezierEndYaw, eased));
                silentPitch = snapToGcd(bezierStartPitch + (bezierEndPitch - bezierStartPitch) * eased);

                if (t >= 1f) bezierActive = false;
            }
            default -> {
                silentYaw = step(currentYaw, targetYaw, speed);
                silentPitch = step(currentPitch, targetPitch, speed);
            }
        }
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
    public static void clearSilent() {
        silentActive = false;
        bezierActive = false;
    }

    private static float lerpAngle(float a, float b, float t) {
        float delta = MathHelper.wrapDegrees(b - a);
        return a + delta * t;
    }

    private static float distance3D(float y1, float p1, float y2, float p2) {
        float dy = MathHelper.wrapDegrees(y2 - y1);
        float dp = p2 - p1;
        return (float) Math.sqrt(dy * dy + dp * dp);
    }

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
