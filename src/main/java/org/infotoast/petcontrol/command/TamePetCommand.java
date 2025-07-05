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

public class TamePetCommand implements CommandExecutor {
    private final PetControl plugin;

    public TamePetCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("petcontrol.tamepet.self") || sender.hasPermission("petcontrol.tamepet.others")) {
                Player player = (Player) sender;
                Entity playerFacing = PetControl.getPlayerFacingEntity(player);
                if (playerFacing != null) {
                    if (playerFacing instanceof Tameable) {
                        Tameable tamableAnimal = (Tameable) playerFacing;
                        if (!tamableAnimal.isTamed()) {
                            OfflinePlayer newOwner;
                            if (args.length == 1) {
                                if (sender.hasPermission("petcontrol.tamepet.others")) {
                                    String nextOwnerName = args[0];
                                    newOwner = Bukkit.getOfflinePlayer(nextOwnerName);
                                    if (!newOwner.hasPlayedBefore()) {
                                        sender.sendMessage("§4Player either does not exist or has not played before!");
                                        return false;
                                    }
                                } else {
                                    sender.sendMessage("§4You do not have permission to tame pets for other people.");
                                    return false;
                                }
                            } else if (args.length == 0) {
                                if (sender.hasPermission("petcontrol.tamepet.self")) {
                                    newOwner = (Player)sender;
                                } else {
                                    sender.sendMessage("§4You do not have permission to tame pets for yourself.");
                                    return false;
                                }
                            } else {
                                sender.sendMessage("§4Too many arguments. Command usage: tamepet <Player>.");
                                return false;
                            }
                            tamableAnimal.setOwner(newOwner);
                            sender.sendMessage("§bAnimal has been tamed!");
                            return true;
                        }
                        sender.sendMessage("§4Animal is already tamed! Use /transferpetowner instead.");
                        return false;
                    }
                    sender.sendMessage("§4That animal cannot be tamed.");
                    return false;
                }
                sender.sendMessage("§4Please face an animal before sending this command.");
                return false;
            }
            sender.sendMessage("§4Access denied.");
            return false;
        }
        sender.sendMessage("§4This command must be sent by a player!");
        return false;
    }
}
