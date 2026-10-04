package com.kovak.aura;

import com.kovak.aura.config.AuraConfig;
import com.kovak.aura.event.KeyEventHandler;
import com.kovak.aura.module.ModuleManager;
import com.kovak.aura.util.FriendManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AuraClient implements ClientModInitializer {
    public static final String MOD_ID = "auraclient";
    public static final String NAME = "Aura Client";
    public static final String VERSION = "1.1.0";

    public static ModuleManager moduleManager;
    public static AuraConfig config;
    public static FriendManager friendManager;

    @Override
    public void onInitializeClient() {
        config = AuraConfig.load();
        friendManager = new FriendManager();
        moduleManager = new ModuleManager();
        moduleManager.init();

        KeyEventHandler.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            moduleManager.onTick();
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            config.save();
            friendManager.save();
        }));
    }
}
