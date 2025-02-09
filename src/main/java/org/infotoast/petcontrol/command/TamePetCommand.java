package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.infotoast.petcontrol.PetControl;

import java.util.logging.Level;

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
                    CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                    net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                    if (animalFacing instanceof TamableAnimal) {
                        TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                        if (!tamableAnimal.isTame()) {
                            net.minecraft.world.entity.player.Player newOwner;
                            if (args.length == 1) {
                                if (sender.hasPermission("petcontrol.tamepet.others")) {
                                    String nextOwnerName = args[0];
                                    CraftPlayer craftPlayer = (CraftPlayer) Bukkit.getPlayer(nextOwnerName);
                                    if (craftPlayer != null) {
                                        net.minecraft.world.entity.Entity thePlayerEntity = craftPlayer.getHandleRaw();
                                        if (thePlayerEntity instanceof net.minecraft.world.entity.player.Player) {
                                            newOwner = (net.minecraft.world.entity.player.Player) thePlayerEntity;
                                        } else {
                                            plugin.logger.log(Level.SEVERE, "Assert net.minecraft.world.entity.player.Player");
                                            sender.sendMessage("§4Something went wrong! Check console for more info.");
                                            return false;
                                        }
                                    } else {
                                        plugin.logger.log(Level.SEVERE, "Assert craftPlayer is not null");
                                        sender.sendMessage("§4Something went wrong! Check console for more info.");
                                        return false;
                                    }
                                } else {
                                    sender.sendMessage("§4You do not have permission to tame pets for other people.");
                                    return false;
                                }
                            } else if (args.length == 0) {
                                if (sender.hasPermission("petcontrol.tamepet.self")) {
                                    net.minecraft.world.entity.Entity thePlayerEntity = ((CraftPlayer)player).getHandleRaw();
                                    if (thePlayerEntity instanceof net.minecraft.world.entity.player.Player) {
                                        newOwner = (net.minecraft.world.entity.player.Player) thePlayerEntity;
                                    } else {
                                        plugin.logger.log(Level.SEVERE, "Assert playerEntity instance of net.minecraft.world.entity.player.Player");
                                        sender.sendMessage("§4Something went wrong! Check console for more info.");
                                        return false;
                                    }
                                } else {
                                    sender.sendMessage("§4You do not have permission to tame pets for yourself.");
                                    return false;
                                }
                            } else {
                                sender.sendMessage("§4Too many arguments. Command usage: tamepet <Player>.");
                                return false;
                            }
                            tamableAnimal.tame(newOwner);
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
