package com.kovak.aura.mixin;

import com.kovak.aura.AuraClient;
import com.kovak.aura.ui.ClickGUI;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void auraclient$handleGuiKey(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (AuraClient.moduleManager == null || AuraClient.moduleManager.getGuiKey() == null) return;

        while (AuraClient.moduleManager.getGuiKey().wasPressed()) {
            mc.setScreen(new ClickGUI());
        }
    }
}
