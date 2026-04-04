package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.cachefile.EntryType;
import org.infotoast.petcontrol.cachefile.RoamingAnimalEntry;
import org.infotoast.petcontrol.cachefile.TamedAnimalEntry;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;

import java.util.Objects;
import java.util.UUID;

public class PetInfoCommand implements CommandExecutor {
    private final PetControl plugin;

    public PetInfoCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("petcontrol.petinfo")) {
                Player player = (Player) sender;
                Entity playerFacing = PetControl.getPlayerFacingEntity(player);
                if (playerFacing != null) {
                    CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                    net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                    if (animalFacing instanceof TamableAnimal) {
                        TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                        sender.sendMessage("§6§l------------< PET INFO >------------");
                        sender.sendMessage("§6§l| §r§1Entity Type: §r§9" + playerFacing.getType());
                        if (playerFacing.getName() != null) {
                            sender.sendMessage("§6§l| §r§1Entity Name: §r§9" + playerFacing.getName());
                        }
                        sender.sendMessage("§6§l| §r§1Entity UUID: §r§9" + playerFacing.getUniqueId());
                        sender.sendMessage("§6§l| §r§1Health: §r§9" + tamableAnimal.getHealth() + "/" + tamableAnimal.getMaxHealth());
                        String isTame = (tamableAnimal.isTame()) ? "Yes" : "No";
                        sender.sendMessage("§6§l| §r§1Tamed: §r§9" + isTame);
                        if (tamableAnimal.isTame()) {
                            UUID ownerUUID = Objects.requireNonNull(tamableAnimal.getOwnerReference()).getUUID();
                            if (ownerUUID != null) {
                                TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(playerFacing.getUniqueId());
                                RoamingAnimalEntry rae = PetControl.cacheManager.getRoamingAnimalFromUUID(playerFacing.getUniqueId());
                                boolean roaming = false;
                                int roamingRadius;
                                int roamingCenterX;
                                int roamingCenterZ;
                                if (rae != null)
                                    roaming = true;
                                if (tae == null) {
                                    tae = new TamedAnimalEntry(PetControl.cacheManager.getAnimalTypeFromEntity(playerFacing), playerFacing.getUniqueId(), ownerUUID, playerFacing.getName(), player.getName(), tamableAnimal.isOrderedToSit(), false, roaming);
                                    PetControl.cacheManager.addTamedAnimalEntry(tae);
                                } else {
                                    tae = new TamedAnimalEntry(PetControl.cacheManager.getAnimalTypeFromEntity(playerFacing), playerFacing.getUniqueId(), ownerUUID, playerFacing.getName(), player.getName(), tamableAnimal.isOrderedToSit(), tae.isGuarded(), roaming);
                                    PetControl.cacheManager.removeByUUID(tae.getUUID(), EntryType.TAMED);
                                    PetControl.cacheManager.addTamedAnimalEntry(tae);
                                }
                                String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                                sender.sendMessage("§6§l| §r§1Owner: §r§9" + ownerName);
                                String isSitting = (tamableAnimal.isInSittingPose()) ? "Yes" : "No";
                                sender.sendMessage("§6§l| §r§1Sitting: §r§9" + isSitting);
                                if (roaming) {
                                    roamingRadius = rae.getRadius();
                                    roamingCenterX = rae.getCenterX();
                                    roamingCenterZ = rae.getCenterZ();
                                    sender.sendMessage("§6§l| §r§1Roaming: §r§9Yes");
                                    sender.sendMessage("§6§l| §r§1Roaming Radius: §r§9" + roamingRadius);
                                    sender.sendMessage("§6§l| §r§1Roaming Center: §r§9" + roamingCenterX + ", " + roamingCenterZ);
                                } else {
                                    sender.sendMessage("§6§l| §r§1Roaming: §r§9No");
                                }
                                if (tae != null) {
                                    sender.sendMessage("§6§l| §r§1Guarded: §r§9" + (tae.isGuarded() ? "Yes" : "No"));
                                }
                            }
                        }
                        sender.sendMessage("§6§l------------------------------------");
                        return true;
                    } else if (PetControl.SUPPORTED_ANIMAL_TYPES.contains(playerFacing.getType().toString()) && playerFacing instanceof Mob) {
                        // Support for other types of animals that are near-tamable
                        Mob playerFacingMob = (Mob) playerFacing;
                        sender.sendMessage("§6§l------------< ANIMAL INFO >------------");
                        sender.sendMessage("§6§l| §r§1Entity Type: §r§9" + playerFacing.getType());
                        if (playerFacing.getName() != null) {
                            sender.sendMessage("§6§l| §r§1Entity Name: §r§9" + playerFacing.getName());
                        }
                        sender.sendMessage("§6§l| §r§1Entity UUID: §r§9" + playerFacing.getUniqueId());
                        sender.sendMessage("§6§l| §r§1Health: §r§9" + playerFacingMob.getHealth() + "/" + playerFacingMob.getMaxHealth());
                        sender.sendMessage("§6§l---------------------------------------");
                    }
                    sender.sendMessage("§4This is not an animal we support getting info on!");
                    return false;
                }
                sender.sendMessage("§4Please face an animal.");
                return false;
            }
            sender.sendMessage("§4Access denied.");
            return true;
        }
        sender.sendMessage("§4This command must be sent by a player!");
        return false;
    }
}
