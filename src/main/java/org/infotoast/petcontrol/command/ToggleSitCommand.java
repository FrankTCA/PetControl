package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.cachefile.TamedAnimalEntry;

public class ToggleSitCommand implements CommandExecutor {
    private final PetControl plugin;

    public ToggleSitCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("petcontrol.togglesit")) {
            Entity playerFacing = PetControl.getPlayerFacingEntity(sender);
            if (playerFacing != null) {
                CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                if (animalFacing instanceof TamableAnimal) {
                    TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                    if (tamableAnimal.isTame()) {
                        tamableAnimal.setOrderedToSit(!tamableAnimal.isOrderedToSit());
                        PetControl.cacheManager.getTamedAnimalFromUUID(tamableAnimal.getUUID()).setSitting(tamableAnimal.isOrderedToSit());
                        sender.sendMessage("§bAnimal sitting toggled.");
                        return true;
                    }
                    sender.sendMessage("§4Animal must be tamed!");
                    return false;
                }
            }
            sender.sendMessage("§4You must face a tamed animal!");
            return false;
        }
        sender.sendMessage("§4Access denied.");
        return true;
    }
}
