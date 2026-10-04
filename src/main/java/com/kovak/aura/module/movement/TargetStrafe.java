package com.kovak.aura.module.movement;

import com.kovak.aura.AuraClient;
import com.kovak.aura.module.Category;
import com.kovak.aura.module.Module;
import com.kovak.aura.module.combat.Aura;
import com.kovak.aura.setting.BooleanSetting;
import com.kovak.aura.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class TargetStrafe extends Module {

    public final NumberSetting radius = new NumberSetting("Radius", 1.5, 0.5, 4.0, 0.1);
    public final NumberSetting speed = new NumberSetting("Speed", 0.25, 0.05, 1.0, 0.05);
    public final BooleanSetting onlyWhileAttacking = new BooleanSetting("Only Attacking", true);

    private boolean direction = true;
    private int switchTimer = 0;

    public TargetStrafe() {
        super("TargetStrafe", "Strafe around your target in a circle.", Category.MOVEMENT);
        settings.add(radius);
        settings.add(speed);
        settings.add(onlyWhileAttacking);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Aura aura = AuraClient.moduleManager.getModule(Aura.class);
        if (aura == null || !aura.isEnabled()) return;

        LivingEntity target = aura.getTarget();
        if (target == null) return;

        if (onlyWhileAttacking.getValue() && mc.player.getAttackCooldownProgress(0f) < 0.9f) return;

        Vec3d targetPos = target.getEntityPos();
        Vec3d playerPos = mc.player.getEntityPos();
        Vec3d diff = targetPos.subtract(playerPos);
        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        if (dist < 0.001) return;

        // Switch direction periodically
        switchTimer++;
        if (switchTimer > 40) {
            direction = !direction;
            switchTimer = 0;
        }

        // Radial direction (toward/away from target)
        Vec3d radial = new Vec3d(diff.x, 0, diff.z).normalize();
        // Tangential direction (perpendicular)
        Vec3d tangent = new Vec3d(-radial.z, 0, radial.x).multiply(direction ? 1 : -1);

        double radiusError = dist - radius.getValue();
        Vec3d motion = tangent.multiply(speed.getValue())
                .add(radial.multiply(-Math.signum(radiusError)
                        * Math.min(Math.abs(radiusError) * 0.3, 0.15)));

        mc.player.addVelocity(motion.x, 0, motion.z);
    }
}
