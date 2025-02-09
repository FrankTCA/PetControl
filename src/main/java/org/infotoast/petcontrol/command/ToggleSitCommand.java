package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.infotoast.petcontrol.PetControl;

public class ToggleSitCommand implements CommandExecutor {
    private final PetControl plugin;

    public ToggleSitCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("petcontrol.togglesit")) {
                Player player = (Player) sender;
                Entity playerFacing = PetControl.getPlayerFacingEntity(player);
                if (playerFacing != null) {
                    CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                    net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                    if (animalFacing instanceof TamableAnimal) {
                        TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                        if (tamableAnimal.isTame()) {
                            tamableAnimal.setOrderedToSit(!tamableAnimal.isOrderedToSit());
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
            return false;
        }
        sender.sendMessage("§4This command must be sent by a player!");
        return false;
    }
}
