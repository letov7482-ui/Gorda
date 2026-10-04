package com.kovak.aura.module;

import com.kovak.aura.module.combat.Aura;
import com.kovak.aura.module.combat.AutoBlock;
import com.kovak.aura.module.misc.AntiBot;
import com.kovak.aura.module.movement.TargetStrafe;
import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private KeyBinding guiKey;

    public void init() {
        // Combat
        modules.add(new Aura());
        modules.add(new AutoBlock());
        // Movement
        modules.add(new TargetStrafe());
        // Misc
        modules.add(new AntiBot());
    }

    public void onTick() {
        for (Module m : modules) {
            if (m.isEnabled()) m.onTick();
        }
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (clazz.isInstance(m)) return clazz.cast(m);
        }
        return null;
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public void setGuiKey(KeyBinding key) { this.guiKey = key; }
    public KeyBinding getGuiKey() { return guiKey; }
}
