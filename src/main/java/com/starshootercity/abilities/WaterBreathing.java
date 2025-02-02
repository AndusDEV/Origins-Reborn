package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.DamageApplier;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WaterBreathing implements Listener, VisibleAbility {
    @EventHandler
    public void onEntityAirChange(EntityAirChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            AbilityRegister.runForAbility(player, getKey(), () -> {
                if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, PersistentDataType.BOOLEAN)))
                    return;
                if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(dehydrationKey, PersistentDataType.BOOLEAN)))
                    return;
                if (player.getRemainingAir() - event.getAmount() > 0) {
                    if (!player.isUnderWater() && !hasWaterBreathing(player)) return;
                } else if (player.isUnderWater() || hasWaterBreathing(player)) return;
                event.setCancelled(true);
            });
        }
    }

    public boolean hasWaterBreathing(Player player) {
        return player.hasPotionEffect(PotionEffectType.CONDUIT_POWER) || player.hasPotionEffect(PotionEffectType.WATER_BREATHING);
    }

    NamespacedKey airKey = new NamespacedKey(OriginsReborn.getInstance(), "fullair");
    NamespacedKey dehydrationKey = new NamespacedKey(OriginsReborn.getInstance(), "dehydrating");

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AbilityRegister.runForAbility(player, getKey(), () -> {
                if (player.isUnderWater() || hasWaterBreathing(player) || player.isInRain()) {
                    if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, PersistentDataType.BOOLEAN))) {
                        player.setRemainingAir(-50);
                        return;
                    }
                    player.setRemainingAir(Math.min(Math.max(player.getRemainingAir() + 4, 4), player.getMaximumAir()));
                    if (player.getRemainingAir() == player.getMaximumAir()) {
                        player.setRemainingAir(-50);
                        player.getPersistentDataContainer().set(airKey, PersistentDataType.BOOLEAN, true);
                    }
                } else {
                    if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, PersistentDataType.BOOLEAN))) {
                        player.setRemainingAir(player.getMaximumAir());
                        player.getPersistentDataContainer().set(airKey, PersistentDataType.BOOLEAN, false);
                    }
                    player.setRemainingAir(player.getRemainingAir() - 1);
                    if (player.getRemainingAir() < -25) {
                        player.getPersistentDataContainer().set(dehydrationKey, PersistentDataType.BOOLEAN, true);
                        player.setRemainingAir(-5);
                        player.getPersistentDataContainer().set(dehydrationKey, PersistentDataType.BOOLEAN, false);
                        DamageApplier.damage(player, DamageApplier.DamageSource.DRY_OUT, 2);
                    }
                }
            }, () -> {
                if (player.getPersistentDataContainer().has(airKey)) {
                    player.setRemainingAir(player.getMaximumAir());
                    player.getPersistentDataContainer().remove(airKey);
                }
            });
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:water_breathing");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You can breathe underwater, but not on land.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Gills", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
