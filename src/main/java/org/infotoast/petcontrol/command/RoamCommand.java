package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.metadata.EntityMetadataStore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.PetListener;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;

import java.util.Objects;
import java.util.UUID;

public class RoamCommand implements CommandExecutor {
    private final PetControl plugin;

    public RoamCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("petcontrol.roam")) {
                Player player = (Player) sender;
                Entity playerFacing = PetControl.getPlayerFacingEntity(player);
                if (playerFacing != null) {
                    CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                    net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                    if (animalFacing instanceof TamableAnimal) {
                        TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                        if (tamableAnimal.isTame()) {
                            UUID ownerUUID = Objects.requireNonNull(tamableAnimal.getOwnerReference()).getUUID();
                            if (args.length >= 1) {
                                // Get coordinates
                                int x;
                                int z;
                                int radius = Integer.parseInt(args[0]);
                                if (args.length >= 3) {
                                    x = Integer.parseInt(args[1]);
                                    z = Integer.parseInt(args[2]);
                                } else {
                                    x = tamableAnimal.getBlockX();
                                    z = tamableAnimal.getBlockZ();
                                }

                                boolean guarded = false;
                                // Get guarded
                                if (args.length == 4) {
                                    if (args[3].equalsIgnoreCase("guarded")) {
                                        if (sender.hasPermission("petcontrol.roam.guarded")) {
                                            guarded = true;
                                        } else {
                                            sender.sendMessage("§4You do not have permission to set mobs as guarded. Defaulting to unguarded.");
                                        }
                                    }
                                }

                                if (args.length == 2) {
                                    if (args[1].equalsIgnoreCase("guarded")) {
                                        if  (sender.hasPermission("petcontrol.roam.guarded")) {
                                            guarded = true;
                                        } else {
                                            sender.sendMessage("§4You do not have permission to set mobs as guarded. Defaulting to unguarded.");
                                        }
                                    }
                                }

                                if (ownerUUID.equals(player.getUniqueId()) || sender.hasPermission("petcontrol.roam.others")) {
                                    if (tamableAnimal instanceof Cat) {
                                        playerFacing.remove();
                                        RoamingCat rcat = RoamingCat.convertFromCat((Cat) tamableAnimal, x, z, radius, guarded);
                                        sender.sendMessage("§bCat is now roaming. UUID: " + rcat.getUUID());
                                        return true;
                                    } else if (tamableAnimal instanceof Wolf) {
                                        playerFacing.remove();
                                        RoamingDog rdog = RoamingDog.convertFromWolf((Wolf) tamableAnimal, x, z, radius, guarded);
                                        sender.sendMessage("§bDog is now roaming. UUID: " + rdog.getUUID());
                                        return true;
                                    }
                                } else {
                                    sender.sendMessage("§4You do not have permission to make other player's animals roam.");
                                    return false;
                                }
                            } else {
                                sender.sendMessage("§cIncorrect usage:");
                                sender.sendMessage("Usage: /roam <radius> [center x] [center z] [guarded]");
                                return false;
                            }
                        } else {
                            sender.sendMessage("§cCommand must be run on a tamed animal");
                            return false;
                        }
                    } else {
                        sender.sendMessage("§cAnimal must be tamable!");
                        return false;
                    }
                } else {
                    sender.sendMessage("§4You must face tamable animal.");
                    return false;
                }
            } else {
                sender.sendMessage("§4You do not have permission to use this command.");
                return true;
            }
        } else {
            sender.sendMessage("Command must be run as a player.");
            return false;
        }
        sender.sendMessage("§4Unknown error. Please report this on GitHub issues.");
        return false;
    }
}
