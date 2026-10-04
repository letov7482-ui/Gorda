package com.kovak.aura.module.combat;

import com.kovak.aura.AuraClient;
import com.kovak.aura.module.Category;
import com.kovak.aura.module.Module;
import com.kovak.aura.setting.BooleanSetting;
import com.kovak.aura.setting.ModeSetting;
import com.kovak.aura.setting.NumberSetting;
import com.kovak.aura.util.RotationUtils;
import com.kovak.aura.util.TargetUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class Aura extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Legit", "Legit", "Rage", "Custom");
    public final NumberSetting range = new NumberSetting("Range", 3.0, 3.0, 6.0, 0.1);
    public final NumberSetting cpsMin = new NumberSetting("CPS Min", 8, 1, 20, 1);
    public final NumberSetting cpsMax = new NumberSetting("CPS Max", 12, 1, 20, 1);
    public final NumberSetting rotationSpeed = new NumberSetting("Rot Speed", 8.0, 1.0, 30.0, 0.5);
    public final NumberSetting fov = new NumberSetting("FOV", 360.0, 30.0, 360.0, 5.0);
    public final BooleanSetting silentRotation = new BooleanSetting("Silent Rotation", true);
    public final BooleanSetting onlyPlayers = new BooleanSetting("Only Players", true);
    public final BooleanSetting throughWalls = new BooleanSetting("Through Walls", false);
    public final BooleanSetting keepSprint = new BooleanSetting("Keep Sprint", true);
    public final BooleanSetting predict = new BooleanSetting("Predict", true);
    public final BooleanSetting hurtTimeCheck = new BooleanSetting("HurtTime Check", true);
    public final BooleanSetting multiTarget = new BooleanSetting("Multi Target", false);
    public final BooleanSetting swing = new BooleanSetting("Swing Hand", true);

    private LivingEntity target;
    private int attackTimer;
    private int switchTimer;

    public Aura() {
        super("Aura", "Auto attacks nearby enemies with silent rotations.", Category.COMBAT);
        settings.add(mode);
        settings.add(range);
        settings.add(cpsMin);
        settings.add(cpsMax);
        settings.add(rotationSpeed);
        settings.add(fov);
        settings.add(silentRotation);
        settings.add(onlyPlayers);
        settings.add(throughWalls);
        settings.add(keepSprint);
        settings.add(predict);
        settings.add(hurtTimeCheck);
        settings.add(multiTarget);
        settings.add(swing);

        mode.onChange(v -> applyPreset(v));
    }

    private void applyPreset(String mode) {
        switch (mode) {
            case "Legit" -> {
                range.setValue(3.0);
                cpsMin.setValue(7.0);
                cpsMax.setValue(10.0);
                rotationSpeed.setValue(4.0);
                silentRotation.setValue(true);
                throughWalls.setValue(false);
                predict.setValue(true);
                keepSprint.setValue(true);
            }
            case "Rage" -> {
                range.setValue(4.5);
                cpsMin.setValue(16.0);
                cpsMax.setValue(20.0);
                rotationSpeed.setValue(20.0);
                silentRotation.setValue(false);
                throughWalls.setValue(true);
                predict.setValue(true);
                keepSprint.setValue(true);
            }
        }
    }

    @Override
    public void onDisable() {
        target = null;
        attackTimer = 0;
        RotationUtils.clearJitter();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (target == null || !isTargetValid(target)) {
            target = TargetUtils.findBest(mc, this);
            switchTimer = 0;
        } else {
            switchTimer++;
            if (multiTarget.getValue() && switchTimer > 20) {
                LivingEntity newTarget = TargetUtils.findBest(mc, this);
                if (newTarget != null && newTarget != target) {
                    target = newTarget;
                    switchTimer = 0;
                }
            }
        }

        if (target == null) return;

        // Wall check
        if (!throughWalls.getValue() && !mc.player.canSee(target)) {
            target = null;
            return;
        }

        // Rotation
        if (silentRotation.getValue()) {
            float[] rot = RotationUtils.calculate(mc.player, target, predict.getValue());
            RotationUtils.setSilent(rot[0], rot[1], rotationSpeed.getFloat());
        } else {
            float[] rot = RotationUtils.calculate(mc.player, target, predict.getValue());
            mc.player.setYaw(rot[0]);
            mc.player.setPitch(MathHelper.clamp(rot[1], -90f, 90f));
        }

        // Attack
        if (attackTimer <= 0) {
            if (mc.player.getAttackCooldownProgress(0f) >= 0.9f) {
                doAttack(mc);
                attackTimer = calcDelay();
            } else {
                attackTimer = 1;
            }
        } else {
            attackTimer--;
        }
    }

    private void doAttack(MinecraftClient mc) {
        if (swing.getValue()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        RotationUtils.applyPostAttackJitter();
    }

    private int calcDelay() {
        int min = cpsMin.getInt();
        int max = Math.max(min, cpsMax.getInt());
        int cps = min + (int)(Math.random() * (max - min + 1));
        return Math.max(1, 20 / Math.max(1, cps));
    }

    private boolean isTargetValid(LivingEntity e) {
        if (e == null || !e.isAlive() || e.isDead()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        double dist = mc.player.getEyePos().distanceTo(e.getBoundingBox().getCenter());
        return dist <= range.getFloat() + 0.5;
    }

    public LivingEntity getTarget() { return target; }
}
