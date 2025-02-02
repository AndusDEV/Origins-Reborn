package com.starshootercity;

import com.starshootercity.abilities.Ability;
import com.starshootercity.abilities.AbilityRegister;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public abstract class OriginsAddon extends JavaPlugin {
    private static OriginsAddon instance;

    public static OriginsAddon getInstance() {
        return instance;
    }

    @Override
    public final void onEnable() {
        instance = this;
        onRegister();
        OriginLoader.register(this);
        for (Ability ability : getAbilities()) {
            AbilityRegister.registerAbility(ability, this);
        }
    }

    @Override
    public @NotNull File getFile() {
        return super.getFile();
    }

    public void onRegister() {}

    public @NotNull List<Ability> getAbilities() {
        return List.of();
    }
}
