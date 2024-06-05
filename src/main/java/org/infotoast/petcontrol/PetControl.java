package org.infotoast.petcontrol;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

public final class PetControl extends JavaPlugin {

    public static Logger logger;

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = getLogger();
        getServer().getConsoleSender().sendMessage("§l§bPetControl has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getConsoleSender().sendMessage("§l§bPetControl disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("petinfo")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("petcontrol.petinfo")) {
                    Player player = (Player) sender;
                    Entity playerFacing = getPlayerFacingEntity(player);
                    if (playerFacing != null) {
                        CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                        net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                        if (animalFacing instanceof TamableAnimal) {
                            TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                            sender.sendMessage("§6§l------------< PET INFO >------------");
                            sender.sendMessage("§6§l| §r§1Entity Type: §r§9" + playerFacing.getType());
                            sender.sendMessage("§6§l| §r§1Entity UUID: §r§9" + playerFacing.getUniqueId());
                            String isTame = (tamableAnimal.isTame()) ? "Yes" : "No";
                            sender.sendMessage("§6§l| §r§1Tamed: §r§9" + isTame);
                            if (tamableAnimal.isTame()) {
                                UUID ownerUUID = tamableAnimal.getOwnerUUID();
                                String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                                sender.sendMessage("§6§l| §r§1Owner: §r§9" + ownerName);
                                String isSitting = (tamableAnimal.isInSittingPose()) ? "Yes" : "No";
                                sender.sendMessage("§6§l| §r§1Sitting: §r§9" + isSitting);
                            }
                            sender.sendMessage("§6§l------------------------------------");
                            return true;
                        }
                        sender.sendMessage("§4This is not a tamable animal!");
                        return false;
                    }
                    sender.sendMessage("§4Please face an animal.");
                    return false;
                }
                sender.sendMessage("§4Access denied.");
                return false;
            }
            sender.sendMessage("§4This command must be sent by a player!");
            return false;
        } else if (label.equalsIgnoreCase("transferpetowner")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("petcontrol.transferpetowner")) {
                    Player player = (Player) sender;
                    Entity playerFacing = getPlayerFacingEntity(player);
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
        } else if (label.equalsIgnoreCase("tamepet")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("petcontrol.tamepet")) {
                    Player player = (Player) sender;
                    Entity playerFacing = getPlayerFacingEntity(player);
                    if (playerFacing != null) {
                        CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                        net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                        if (animalFacing instanceof TamableAnimal) {
                            TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                            if (!tamableAnimal.isTame()) {
                                net.minecraft.world.entity.player.Player newOwner;
                                if (args.length == 1) {
                                    String nextOwnerName = args[0];
                                    CraftPlayer craftPlayer = (CraftPlayer)Bukkit.getPlayer(nextOwnerName);
                                    if (craftPlayer != null) {
                                        net.minecraft.world.entity.Entity thePlayerEntity = craftPlayer.getHandleRaw();
                                        if (thePlayerEntity instanceof net.minecraft.world.entity.player.Player) {
                                            newOwner = (net.minecraft.world.entity.player.Player) thePlayerEntity;
                                        } else {
                                            logger.log(Level.SEVERE, "Assert net.minecraft.world.entity.player.Player");
                                            sender.sendMessage("§4Something went wrong! Check console for more info.");
                                            return false;
                                        }
                                    } else {
                                        logger.log(Level.SEVERE, "Assert craftPlayer is not null");
                                        sender.sendMessage("§4Something went wrong! Check console for more info.");
                                        return false;
                                    }
                                } else if (args.length == 0) {
                                    net.minecraft.world.entity.Entity thePlayerEntity = ((CraftPlayer)player).getHandleRaw();
                                    if (thePlayerEntity instanceof net.minecraft.world.entity.player.Player) {
                                        newOwner = (net.minecraft.world.entity.player.Player) thePlayerEntity;
                                    } else {
                                        logger.log(Level.SEVERE, "Assert playerEntity instance of net.minecraft.world.entity.player.Player");
                                        sender.sendMessage("§4Something went wrong! Check console for more info.");
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
        } else if (label.equalsIgnoreCase("togglesit")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("petcontrol.togglesit")) {
                    Player player = (Player) sender;
                    Entity playerFacing = getPlayerFacingEntity(player);
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
        return false;
    }

    private Entity getPlayerFacingEntity(Player player) {
        List<Entity> nearbyEntities = player.getNearbyEntities(10, 10, 10);
        ArrayList<LivingEntity> entitiesAlive = new ArrayList<LivingEntity>();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                entitiesAlive.add((LivingEntity) entity);
            }
        }

        Entity target = null;
        BlockIterator iterator = new BlockIterator(player, 10);
        Block block;
        Location loc;
        int blockX, blockY, blockZ;
        double entityX, entityY, entityZ;
        while (iterator.hasNext()) {
            block = iterator.next();
            blockX = block.getX();
            blockY = block.getY();
            blockZ = block.getZ();
            for (LivingEntity entity : entitiesAlive) {
                loc = entity.getLocation();
                entityX = loc.getX();
                entityY = loc.getY();
                entityZ = loc.getZ();
                if ((blockX-.75 <= entityX && entityX <= blockX+1.75) && (blockZ-.75 <= entityZ && entityZ <= blockZ+1.75) && (blockY - 1 <= entityY && entityY <= blockY + 2.5)) {
                    target = entity;
                    break;
                }
            }
        }

        return target;
    }
}
