package com.kovak.aura.mixin;

import com.kovak.aura.util.RotationUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    private float savedYaw;
    private float savedPitch;

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void auraclient$beforeSend(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;
        if (RotationUtils.hasSilent()) {
            savedYaw = self.getYaw();
            savedPitch = self.getPitch();
            self.setYaw(RotationUtils.getSilentYaw());
            self.setPitch(RotationUtils.getSilentPitch());
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"))
    private void auraclient$afterSend(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;
        if (RotationUtils.hasSilent()) {
            self.setYaw(savedYaw);
            self.setPitch(savedPitch);
            RotationUtils.clearSilent();
        }
    }
}
