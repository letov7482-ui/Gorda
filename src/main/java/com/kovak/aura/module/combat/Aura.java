package com.kovak.aura.module.combat;

import com.kovak.aura.module.Category;
import com.kovak.aura.module.Module;
import com.kovak.aura.setting.BooleanSetting;
import com.kovak.aura.setting.ModeSetting;
import com.kovak.aura.setting.NumberSetting;
import com.kovak.aura.util.RotationUtils;
import com.kovak.aura.util.TargetUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

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
    public final NumberSetting wallsRange = new NumberSetting("Walls Range", 0.0, 0.0, 6.0, 0.1);
    public final BooleanSetting reachOptimize = new BooleanSetting("Reach Optimize", true);
    public final NumberSetting reachTolerance = new NumberSetting("Reach Tol", 0.15, 0.0, 0.5, 0.01);

    // === Rotation ===
    public final ModeSetting rotationMode = new ModeSetting("Rotation", "Smooth",
            "Smooth", "Instant", "Bezier", "Snap");
    public final BooleanSetting silentRotation = new BooleanSetting("Silent Rotation", true);
    public final NumberSetting rotationSpeed = new NumberSetting("Rot Speed", 12.0, 1.0, 30.0, 0.5);
    public final BooleanSetting predict = new BooleanSetting("Predict", true);
    public final NumberSetting predictAmount = new NumberSetting("Predict Amt", 1.5, 0.5, 4.0, 0.1);
    public final NumberSetting pingCompensation = new NumberSetting("Ping Comp", 50, 0, 300, 10);

    // === Attack ===
    public final ModeSetting attackPattern = new ModeSetting("Pattern", "Perfect",
            "Perfect", "Random", "Burst");
    public final BooleanSetting autoAttack = new BooleanSetting("Auto Attack", true);
    public final NumberSetting cpsMin = new NumberSetting("CPS Min", 8, 1, 20, 1);
    public final NumberSetting cpsMax = new NumberSetting("CPS Max", 14, 1, 20, 1);
    public final NumberSetting burstSize = new NumberSetting("Burst Size", 3, 2, 6, 1);
    public final BooleanSetting swing = new BooleanSetting("Swing Hand", true);

    // === Crit / WTap ===
    public final BooleanSetting critOptimize = new BooleanSetting("Crit Optimize", true);
    public final BooleanSetting autoJump = new BooleanSetting("Auto Jump", false);
    public final BooleanSetting autoWTap = new BooleanSetting("Auto W-Tap", false);

    // === Filters ===
    public final BooleanSetting onlyPlayers = new BooleanSetting("Only Players", true);
    public final BooleanSetting keepSprint = new BooleanSetting("Keep Sprint", true);
    public final BooleanSetting noAttackWhileEating = new BooleanSetting("No Attack Eat", true);
    public final BooleanSetting hurtTimeCheck = new BooleanSetting("HurtTime Check", true);

    // === Bypass ===
    public final BooleanSetting rotationJitter = new BooleanSetting("Rotation Jitter", true);
    public final BooleanSetting lagCheck = new BooleanSetting("Lag Check", true);
    public final NumberSetting maxTargetSpeed = new NumberSetting("Max Speed", 20.0, 5.0, 100.0, 1.0);

    private LivingEntity target;
    private int attackTimer;
    private int switchTimer;
    private int targetAge;
    private int burstCounter;
    private Vec3d lastTargetPos;
    private boolean wtapActive;
    private int wtapTimer;

    public Aura() {
        super("Aura", "Smart combat automation with silent rotations.", Category.COMBAT);

        settings.add(mode);
        settings.add(priority);
        settings.add(targetMode);
        settings.add(multiTarget);
        settings.add(switchDelay);
        settings.add(range);
        settings.add(throughWalls);
        settings.add(wallsRange);
        settings.add(reachOptimize);
        settings.add(reachTolerance);
        settings.add(rotationMode);
        settings.add(silentRotation);
        settings.add(rotationSpeed);
        settings.add(predict);
        settings.add(predictAmount);
        settings.add(pingCompensation);
        settings.add(attackPattern);
        settings.add(autoAttack);
        settings.add(cpsMin);
        settings.add(cpsMax);
        settings.add(burstSize);
        settings.add(swing);
        settings.add(critOptimize);
        settings.add(autoJump);
        settings.add(autoWTap);
        settings.add(onlyPlayers);
        settings.add(keepSprint);
        settings.add(noAttackWhileEating);
        settings.add(hurtTimeCheck);
        settings.add(rotationJitter);
        settings.add(lagCheck);
        settings.add(maxTargetSpeed);

        mode.onChange(this::applyPreset);
        applyPreset("Rage");
    }

    private void applyPreset(String m) {
        switch (m) {
            case "Legit" -> {
                range.setValue(3.0);
                wallsRange.setValue(0.0);
                rotationSpeed.setValue(8.0);
                silentRotation.setValue(true);
                throughWalls.setValue(false);
                rotationMode.setValue("Smooth");
                attackPattern.setValue("Random");
                predict.setValue(true);
                predictAmount.setValue(1.0);
                rotationJitter.setValue(true);
                critOptimize.setValue(true);
                autoJump.setValue(true);
                autoWTap.setValue(false);
                multiTarget.setValue(true);
                switchDelay.setValue(10.0);
            }
            case "Rage" -> {
                range.setValue(4.5);
                wallsRange.setValue(0.0);
                rotationSpeed.setValue(25.0);
                silentRotation.setValue(true);
                throughWalls.setValue(false);
                rotationMode.setValue("Instant");
                attackPattern.setValue("Perfect");
                predict.setValue(true);
                predictAmount.setValue(2.0);
                rotationJitter.setValue(false);
                critOptimize.setValue(true);
                autoJump.setValue(true);
                autoWTap.setValue(true);
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
        burstCounter = 0;
        lastTargetPos = null;
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
                lastTargetPos = null;
                burstCounter = 0;
            } else {
                target = null;
                return;
            }
        } else {
            targetAge++;

            if (multiTarget.getValue() && switchTimer >= switchDelay.getInt()) {
                LivingEntity better = TargetUtils.findBest(mc, this);
                if (better != null && better != target
                        && TargetUtils.isBetter(mc, target, better, this)) {
                    target = better;
                    targetAge = 0;
                    switchTimer = 0;
                    lastTargetPos = null;
                    burstCounter = 0;
                }
            }
            switchTimer++;
        }

        if (target == null) return;

        // === Wall check — hard gate ===
        boolean canSee = mc.player.canSee(target);
        boolean hasLineOfSight = canSee && raycastHasLineOfSight(mc, target);

        if (!hasLineOfSight && !throughWalls.getValue()) {
            target = null;
            return;
        }

        double distance = mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
        double effectiveRange = hasLineOfSight ? range.getValue() : wallsRange.getValue();
        if (distance > effectiveRange + 0.3) {
            target = null;
            return;
        }

        // === Rotation ===
        float[] rot = RotationUtils.calculate(mc.player, target,
                predict.getValue(), predictAmount.getFloat());

        float speed = rotationSpeed.getFloat();
        if (mode.is("Rage")) speed = 180f;
        if (mode.is("Legit")) speed = Math.min(speed, 20f);

        RotationUtils.setSilentWithMode(
                rotationMode.getValue(),
                mc.player.getYaw(), mc.player.getPitch(),
                rot[0], rot[1],
                speed
        );

        if (mode.is("Rage") && !silentRotation.getValue()) {
            mc.player.setYaw(rot[0]);
            mc.player.setPitch(MathHelper.clamp(rot[1], -90f, 90f));
        }

        // === W-Tap: release sprint right before hit, re-press after ===
        if (autoWTap.getValue() && mc.player.isSprinting()) {
            if (!wtapActive) {
                mc.options.sprintKey.setPressed(false);
                wtapActive = true;
                wtapTimer = 2;
            }
        }
        if (wtapActive) {
            wtapTimer--;
            if (wtapTimer <= 0) {
                wtapActive = false;
                if (keepSprint.getValue()) {
                    mc.options.sprintKey.setPressed(true);
                }
            }
        }

        // === Attack ===
        if (!autoAttack.getValue()) return;

        if (!hasLineOfSight && !throughWalls.getValue()) return;

        if (noAttackWhileEating.getValue() && mc.player.isUsingItem()) return;

        float cooldown = mc.player.getAttackCooldownProgress(0f);

        // Auto-jump for crit
        if (critOptimize.getValue() && autoJump.getValue()
                && mc.player.isOnGround()
                && cooldown >= 0.85f
                && !mc.player.isTouchingWater()) {
            mc.player.jump();
        }

        // Crit gate — if critOptimize, wait until falling to hit
        boolean canCrit = isCritPossible(mc);
        boolean critReady = !critOptimize.getValue() || canCrit || !mc.player.isOnGround();

        boolean ready = false;

        switch (attackPattern.getValue()) {
            case "Perfect" -> ready = cooldown >= 0.98f && critReady;
            case "Random" -> {
                if (attackTimer <= 0) ready = cooldown >= 0.90f;
                else attackTimer--;
            }
            case "Burst" -> {
                if (burstCounter < burstSize.getInt()) {
                    ready = cooldown >= 0.90f;
                } else {
                    if (attackTimer <= 0) {
                        burstCounter = 0;
                        ready = cooldown >= 0.98f;
                    } else attackTimer--;
                }
            }
        }

        if (!ready) return;

        doAttack(mc);

        if (attackPattern.is("Random")) {
            attackTimer = calcDelayFromCps();
        } else if (attackPattern.is("Burst")) {
            burstCounter++;
            if (burstCounter >= burstSize.getInt()) {
                attackTimer = calcDelayFromCps();
            }
        }
    }

    private boolean isCritPossible(MinecraftClient mc) {
        return mc.player.fallDistance > 0.0f
                && !mc.player.isOnGround()
                && !mc.player.isTouchingWater()
                && !mc.player.isClimbing()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !mc.player.hasVehicle()
                && !mc.player.isSprinting();
    }

    private boolean raycastHasLineOfSight(MinecraftClient mc, LivingEntity target) {
        if (mc.player == null || mc.world == null) return false;

        Vec3d from = mc.player.getEyePos();
        Vec3d to = TargetUtils.getHitboxCenter(target);

        HitResult hit = mc.world.raycast(new RaycastContext(
                from, to,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        return hit == null || hit.getType() == HitResult.Type.MISS;
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

        if (e.getWorld() != mc.world) return false;

        // Lag check — skip teleporting / lagging targets
        if (lagCheck.getValue()) {
            Vec3d current = e.getEntityPos();
            if (lastTargetPos != null) {
                double moved = current.distanceTo(lastTargetPos);
                if (moved > maxTargetSpeed.getValue() / 20.0) {
                    lastTargetPos = current;
                    return false;
                }
            }
            lastTargetPos = current;
        }

        double distance = mc.player.getEyePos().distanceTo(e.getBoundingBox().getCenter());
        double maxRange = Math.max(range.getValue(), wallsRange.getValue());
        return distance <= maxRange + 2.0;
    }

    public LivingEntity getTarget() { return target; }
            }
