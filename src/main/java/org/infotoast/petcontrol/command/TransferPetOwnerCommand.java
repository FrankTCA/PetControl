package org.infotoast.petcontrol.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.bukkit.entity.Tameable;
import org.infotoast.petcontrol.PetControl;

import java.util.Objects;
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
                    if (playerFacing instanceof Tameable) {
                        Tameable tamableAnimal = (Tameable) playerFacing;
                        if (tamableAnimal.isTamed()) {
                            UUID lastOwnerUUID = Objects.requireNonNull(tamableAnimal.getOwnerUniqueId());
                            String lastOwnerName = Bukkit.getOfflinePlayer(lastOwnerUUID).getName();
                            if (args.length == 1) {
                                String nextOwnerName = args[0];
                                OfflinePlayer newOwner = Bukkit.getOfflinePlayer(nextOwnerName);
                                tamableAnimal.setOwner(newOwner);
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
