package com.kovak.aura.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class FriendManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("aura-friends.json");

    private final Set<String> friends = new HashSet<>();

    public FriendManager() {
        load();
    }

    public boolean isFriend(PlayerEntity player) {
        return friends.contains(player.getGameProfile().getName().toLowerCase());
    }

    public boolean isFriend(String name) {
        return friends.contains(name.toLowerCase());
    }

    public void addFriend(String name) {
        friends.add(name.toLowerCase());
        save();
    }

    public void removeFriend(String name) {
        friends.remove(name.toLowerCase());
        save();
    }

    public void toggle(String name) {
        if (isFriend(name)) removeFriend(name);
        else addFriend(name);
    }

    public Set<String> getFriends() { return friends; }

    public void load() {
        if (!Files.exists(PATH)) return;
        try {
            String json = Files.readString(PATH);
            Type type = new TypeToken<Set<String>>(){}.getType();
            Set<String> loaded = GSON.fromJson(json, type);
            if (loaded != null) friends.addAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            Files.writeString(PATH, GSON.toJson(friends));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
