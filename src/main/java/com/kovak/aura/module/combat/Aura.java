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

    // === Mode ===
    public final ModeSetting mode = new ModeSetting("Mode", "Rage", "Legit", "Rage", "Custom");

    // === Target ===
    public final ModeSetting priority = new ModeSetting("Priority", "Distance",
            "Distance", "Health", "Angle", "Armor");
    public final ModeSetting targetMode = new ModeSetting("Target Mode", "Always",
            "Always", "Only Jump", "Only Ground", "Only Sprint", "Only Crit");
    public final BooleanSetting multiTarget = new BooleanSetting("Multi Target", true);
    public final NumberSetting switchDelay = new NumberSetting("Switch Delay", 6, 0, 40, 1);

    // === Range ===
    public final NumberSetting range = new NumberSetting("Range", 3.0, 3.0, 6.0, 0.1);
    public final BooleanSetting throughWalls = new BooleanSetting("Through Walls", false);
    public final NumberSetting wallsRange = new NumberSetting("Walls Range", 3.0, 0.0, 6.0, 0.1);

    // === Rotation ===
    public final BooleanSetting silentRotation = new BooleanSetting("Silent Rotation", true);
    public final NumberSetting rotationSpeed = new NumberSetting("Rot Speed", 12.0, 1.0, 30.0, 0.5);
    public final BooleanSetting predict = new BooleanSetting("Predict", true);

    // === Attack ===
    public final BooleanSetting autoAttack = new BooleanSetting("Auto Attack", true);
    public final BooleanSetting perfectCooldown = new BooleanSetting("Perfect Cooldown", true);
    public final NumberSetting cpsMin = new NumberSetting("CPS Min", 8, 1, 20, 1);
    public final NumberSetting cpsMax = new NumberSetting("CPS Max", 14, 1, 20, 1);
    public final BooleanSetting swing = new BooleanSetting("Swing Hand", true);

    // === Filters ===
    public final BooleanSetting onlyPlayers = new BooleanSetting("Only Players", true);
    public final BooleanSetting keepSprint = new BooleanSetting("Keep Sprint", true);
    public final BooleanSetting noAttackWhileEating = new BooleanSetting("No Attack Eat", true);
    public final BooleanSetting hurtTimeCheck = new BooleanSetting("HurtTime Check", true);

    // === Bypass ===
    public final BooleanSetting rotationJitter = new BooleanSetting("Rotation Jitter", true);
    public final BooleanSetting gcdFix = new BooleanSetting("GCD Fix", true);

    private LivingEntity target;
    private int attackTimer;
    private int switchTimer;
    private int targetAge;

    public Aura() {
        super("Aura", "Auto attacks nearby enemies with silent rotations.", Category.COMBAT);

        settings.add(mode);
        settings.add(priority);
        settings.add(targetMode);
        settings.add(multiTarget);
        settings.add(switchDelay);
        settings.add(range);
        settings.add(throughWalls);
        settings.add(wallsRange);
        settings.add(silentRotation);
        settings.add(rotationSpeed);
        settings.add(predict);
        settings.add(autoAttack);
        settings.add(perfectCooldown);
        settings.add(cpsMin);
        settings.add(cpsMax);
        settings.add(swing);
        settings.add(onlyPlayers);
        settings.add(keepSprint);
        settings.add(noAttackWhileEating);
        settings.add(hurtTimeCheck);
        settings.add(rotationJitter);
        settings.add(gcdFix);

        mode.onChange(this::applyPreset);
        applyPreset("Rage");
    }

    private void applyPreset(String m) {
        switch (m) {
            case "Legit" -> {
                range.setValue(3.0);
                wallsRange.setValue(3.0);
                cpsMin.setValue(6.0);
                cpsMax.setValue(10.0);
                rotationSpeed.setValue(6.0);
                silentRotation.setValue(true);
                throughWalls.setValue(false);
                perfectCooldown.setValue(false);
                predict.setValue(true);
                rotationJitter.setValue(true);
                multiTarget.setValue(true);
                switchDelay.setValue(10.0);
            }
            case "Rage" -> {
                range.setValue(4.5);
                wallsRange.setValue(4.5);
                cpsMin.setValue(16.0);
                cpsMax.setValue(20.0);
                rotationSpeed.setValue(25.0);
                silentRotation.setValue(true);
                throughWalls.setValue(true);
                perfectCooldown.setValue(true);
                predict.setValue(true);
                rotationJitter.setValue(false);
                multiTarget.setValue(true);
                switchDelay.setValue(3.0);
            }
        }
    }

    @Override
    public void onDisable() {
        target = null;
        attackTimer = 0;
        switchTimer = 0;
        targetAge = 0;
        RotationUtils.clearJitter();
        RotationUtils.clearSilent();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // === Target acquisition ===
        if (target == null || !isStillValid(target)) {
            LivingEntity found = TargetUtils.findBest(mc, this);
            if (found != null) {
                target = found;
                targetAge = 0;
                switchTimer = 0;
            } else {
                target = null;
                return;
            }
        } else {
            targetAge++;

            // Check if we should switch to a better target
            if (multiTarget.getValue() && switchTimer >= switchDelay.getInt()) {
                LivingEntity better = TargetUtils.findBest(mc, this);
                if (better != null && better != target && TargetUtils.isBetter(mc, target, better, this)) {
                    target = better;
                    targetAge = 0;
                    switchTimer = 0;
                }
            }
            switchTimer++;
        }

        if (target == null) return;

        // Wall check
        boolean canSee = mc.player.canSee(target);
        if (!canSee && !throughWalls.getValue()) {
            target = null;
            return;
        }

        double distance = mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
        double effectiveRange = canSee ? range.getValue() : wallsRange.getValue();
        if (distance > effectiveRange + 0.5) {
            target = null;
            return;
        }

        // === Rotation ===
        float[] rot = RotationUtils.calculate(mc.player, target, predict.getValue());
        float speed = rotationSpeed.getFloat();

        // Rage mode: instant snap. Legit: smooth but fast enough to actually converge.
        if (mode.is("Rage")) speed = 180f; // basically instant
        if (mode.is("Legit")) speed = Math.min(speed, 20f);

        RotationUtils.setSilent(mc.player.getYaw(), mc.player.getPitch(), rot[0], rot[1], speed);

        // In Rage mode, we also snap the local camera for visual feedback
        if (mode.is("Rage") && !silentRotation.getValue()) {
            mc.player.setYaw(rot[0]);
            mc.player.setPitch(MathHelper.clamp(rot[1], -90f, 90f));
        }

        // === Attack ===
        if (!autoAttack.getValue()) return;

        // Don't attack while eating/drinking (if enabled)
        if (noAttackWhileEating.getValue() && mc.player.isUsingItem()) return;

        // Attack cooldown gate
        float cooldown = mc.player.getAttackCooldownProgress(0f);
        boolean ready = cooldown >= 0.98f;

        if (!perfectCooldown.getValue() && ready) {
            // Limit by CPS instead of perfect cooldown
            if (attackTimer > 0) {
                attackTimer--;
                return;
            }
            ready = true;
        }

        if (!ready) return;

        doAttack(mc);

        if (perfectCooldown.getValue()) {
            attackTimer = 0;
        } else {
            attackTimer = calcDelayFromCps();
        }
    }

    private void doAttack(MinecraftClient mc) {
        if (swing.getValue()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        if (rotationJitter.getValue()) {
            RotationUtils.applyPostAttackJitter();
        }
    }

    private int calcDelayFromCps() {
        int min = cpsMin.getInt();
        int max = Math.max(min, cpsMax.getInt());
        int cps = min + (int)(Math.random() * (max - min + 1));
        return Math.max(1, 20 / Math.max(1, cps));
    }

    private boolean isStillValid(LivingEntity e) {
        if (e == null || !e.isAlive() || e.isDead()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;
        if (!mc.world.getEntities().contains(e) && !mc.world.getPlayers().contains(e)) return false;

        double distance = mc.player.getEyePos().distanceTo(e.getBoundingBox().getCenter());
        double maxRange = Math.max(range.getValue(), wallsRange.getValue());
        return distance <= maxRange + 2.0;
    }

    public LivingEntity getTarget() { return target; }
                     }
