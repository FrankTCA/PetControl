package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.infotoast.petcontrol.PetControl;

import java.util.UUID;

public class TransferPetOwnerCommand implements CommandExecutor {
    private final PetControl plugin;

    public TransferPetOwnerCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("petcontrol.transferpetowner")) {
                Player player = (Player) sender;
                Entity playerFacing = PetControl.getPlayerFacingEntity(player);
                if (playerFacing != null) {
                    CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                    net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                    if (animalFacing instanceof TamableAnimal) {
                        TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                        if (tamableAnimal.isTame()) {
                            UUID lastOwnerUUID = tamableAnimal.getOwnerUUID();
                            String lastOwnerName = Bukkit.getOfflinePlayer(lastOwnerUUID).getName();
                            if (args.length == 1) {
                                String nextOwnerName = args[0];
                                UUID newOwnerUUID = Bukkit.getOfflinePlayer(nextOwnerName).getUniqueId();
                                tamableAnimal.setOwnerUUID(newOwnerUUID);
                                sender.sendMessage("§bLast Owner: " + lastOwnerName);
                                sender.sendMessage("§bNew Owner: " + nextOwnerName);
                                sender.sendMessage("§5Ownership successfully transferred!");
                                return true;
                            }
                            sender.sendMessage("§4Please provide a playername");
                            return false;
                        }
                    }
                    sender.sendMessage("§4Animal must be tamed!");
                    return false;
                }
                sender.sendMessage("§4You are not facing an animal!");
                return false;
            }
            sender.sendMessage("§4Access denied.");
            return false;
        }
        sender.sendMessage("You must be a player to use this command!");
        return false;
    }
}
