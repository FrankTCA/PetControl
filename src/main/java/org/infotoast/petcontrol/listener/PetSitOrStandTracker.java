package org.infotoast.petcontrol.listener;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.infotoast.petcontrol.PetControl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetSitOrStandTracker {
    private PetControl plugin;
    private final Map<UUID, Boolean> lastState = new HashMap<>();
    public PetSitOrStandTracker(PetControl plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1, 3);
    }

    public void stop() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    public void tick() {
        for (Entity entity : plugin.getServer().getWorlds().stream().flatMap(w -> w.getEntities().stream()).toList()) {
            if (!(((CraftEntity)entity).getHandleRaw() instanceof TamableAnimal tameable)) continue;
            if (!tameable.isTame()) continue;

            UUID uuid = tameable.getUUID();
            boolean current = tameable.isOrderedToSit();

            boolean previous = lastState.getOrDefault(uuid, false);
            if (previous != current) {
                UUID ownerUUID = tameable.getOwnerReference().getUUID();
                if (ownerUUID == null) {
                    plugin.getLogger().warning("Failed to find owner for tamed animal with UUID: " + uuid);
                }

                PetSitOrStandEvent event = new PetSitOrStandEvent(entity, ownerUUID, current);
                plugin.getServer().getPluginManager().callEvent(event);

                lastState.put(uuid, current);
            }
            lastState.put(uuid, current);

        }
    }
}
