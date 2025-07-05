package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.infotoast.petcontrol.PetControl;

public class HealPetCommand implements CommandExecutor {
    private final PetControl plugin;

    public HealPetCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("petcontrol.heal")) {
                Player player = (Player) sender;
                Entity playerFacing = PetControl.getPlayerFacingEntity(player);
                if (playerFacing != null) {
                    CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                    net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                    if (animalFacing instanceof TamableAnimal) {
                        TamableAnimal tamableAnimalFacing = (TamableAnimal) animalFacing;
                        tamableAnimalFacing.setHealth(tamableAnimalFacing.getMaxHealth());
                        sender.sendMessage("§bAnimal healed!");
                        return true;
                    }
                }
                sender.sendMessage("§4You must face tamable animal.");
                return false;
            }
            sender.sendMessage("§4Access denied.");
            return true;
        }
        sender.sendMessage("§4You must be a player to use this command!");
        return false;
    }
}
