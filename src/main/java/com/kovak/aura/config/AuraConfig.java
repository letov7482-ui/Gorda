package com.kovak.aura.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AuraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("aura-client.json");

    public int guiKey = 344; // Right Shift

    public static AuraConfig load() {
        if (Files.exists(PATH)) {
            try {
                return GSON.fromJson(Files.readString(PATH), AuraConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AuraConfig c = new AuraConfig();
        c.save();
        return c;
    }

    public void save() {
        try {
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
