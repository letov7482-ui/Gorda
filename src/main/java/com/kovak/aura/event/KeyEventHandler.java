package com.kovak.aura.event;

import com.kovak.aura.AuraClient;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyEventHandler {

    public static void register() {
        KeyBinding guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auraclient.gui",
                InputUtil.Type.KEYSYM,
                AuraClient.config.guiKey,
                "category.auraclient"
        ));
        AuraClient.moduleManager.setGuiKey(guiKey);

        // Module toggle binds are processed each tick
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            for (var module : AuraClient.moduleManager.getModules()) {
                if (module.getKeyBind() == -1) continue;
                while (InputUtil.isKeyPressed(client.getWindow().getHandle(), module.getKeyBind())) {
                    module.toggle();
                }
            }
        });
    }

    public static String keyName(int key) {
        if (key == -1) return "NONE";
        String name = GLFW.glfwGetKeyName(key, 0);
        return name != null ? name.toUpperCase() : "KEY_" + key;
    }
}
