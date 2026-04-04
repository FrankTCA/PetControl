package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.cachefile.TamedAnimalEntry;

import java.util.UUID;

public class GuardCommand implements CommandExecutor {
    private final PetControl plugin;
    public GuardCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender.hasPermission("petcontrol.guard")) {
            Entity playerFacing = PetControl.getPlayerFacingEntity(sender);
            if (playerFacing != null && playerFacing instanceof Tameable) {
                Tameable tameable = (Tameable) playerFacing;
                if (tameable.isTamed()) {
                    CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                    net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                    TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                    TamedAnimalEntry entry = PetControl.cacheManager.getTamedAnimalFromUUID(tameable.getUniqueId());
                    if (entry == null) {
                        boolean isGuarded = false;
                        PetControl.cacheManager.addTamedAnimalEntry(new TamedAnimalEntry(PetControl.cacheManager.getAnimalTypeFromEntity(tameable), tameable.getUniqueId(), UUID.randomUUID(), tameable.getName(), Bukkit.getOfflinePlayer(tamableAnimal.getOwnerReference().getUUID()).getName(), tamableAnimal.isOrderedToSit(), true, false));
                        sender.sendMessage("§bGuarding is now §a§lon§r§b for §l§a" + tameable.getName());
                        return true;
                    } else {
                        boolean isGuarded = PetControl.cacheManager.getTamedAnimalFromUUID(tameable.getUniqueId()).isGuarded();
                        PetControl.cacheManager.getTamedAnimalFromUUID(tameable.getUniqueId()).setGuarded(!isGuarded);
                        String guardStatus = (!isGuarded) ? "on" : "off";
                        sender.sendMessage("§bGuarding toggled §a§l" + guardStatus + " §r§bfor §l§a" + tameable.getName());
                        return true;
                    }
                } else {
                    sender.sendMessage("§cThis animal is not tamed.");
                    return false;
                }
            } else {
                sender.sendMessage("§cYou are not facing an animal.");
                return false;
            }
        } else {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return false;
        }
    }
}
