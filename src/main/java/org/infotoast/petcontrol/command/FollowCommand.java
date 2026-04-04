package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.cachefile.EntryType;
import org.infotoast.petcontrol.cachefile.TamedAnimalEntry;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;

import java.util.Objects;
import java.util.UUID;

public class FollowCommand implements CommandExecutor {
    private final PetControl plugin;
    public FollowCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("petcontrol.follow")) {
            Entity playerFacing = PetControl.getPlayerFacingEntity(sender);
            if (playerFacing != null) {
                CraftEntity craftPlayerFacing = (CraftEntity)playerFacing;
                net.minecraft.world.entity.Entity animalFacing = craftPlayerFacing.getHandle();
                if (animalFacing instanceof TamableAnimal) {
                    TamableAnimal tamableAnimal = (TamableAnimal) animalFacing;
                    if (tamableAnimal.isTame()) {
                        UUID ownerUUID = Objects.requireNonNull(tamableAnimal.getOwnerReference()).getUUID();
                        if ((sender instanceof Player player && ownerUUID.equals(player.getUniqueId())) || sender.hasPermission("petcontrol.follow.others")) {
                            if (tamableAnimal instanceof RoamingCat) {
                                Cat newCat = ((RoamingCat) tamableAnimal).convertToCat();
                                sender.sendMessage("§bCat is now reset to normal AI and will follow and teleport.");
                                sender.sendMessage("§bUUID: " + newCat.getUUID());
                                disableRoamingInCache(tamableAnimal, newCat);
                                return true;
                            } else if (tamableAnimal instanceof RoamingDog) {
                                Wolf newWolf = ((RoamingDog) tamableAnimal).convertToWolf();
                                sender.sendMessage("§bDog is now reset to normal AI and will follow and teleport.");
                                sender.sendMessage("§bUUID: " + newWolf.getUUID());
                                disableRoamingInCache(tamableAnimal, newWolf);
                                return true;
                            } else {
                                sender.sendMessage("§4You must face a cat or dog that is roaming.");
                                return false;
                            }
                        } else {
                            sender.sendMessage("§4You do not have permission to make set other's animals to follow mode.");
                            return false;
                        }
                    } else {
                        sender.sendMessage("§4This command only works for tamed animals.");
                        return false;
                    }
                } else {
                    sender.sendMessage("§4You must face a tamable animal.");
                    return false;
                }
            } else {
                sender.sendMessage("§4You must face an animal.");
                return false;
            }
        } else {
            sender.sendMessage("§4You do not have permission to use this command!");
            return true;
        }
    }

    private void disableRoamingInCache(TamableAnimal oldAnimal, TamableAnimal newAnimal) {
        TamedAnimalEntry ent = PetControl.cacheManager.getTamedAnimalFromUUID(oldAnimal.getUUID());
        PetControl.cacheManager.removeByUUID(oldAnimal.getUUID(), EntryType.TAMED);
        ent.setUUID(newAnimal.getUUID());
        ent.setRoaming(false);
        PetControl.cacheManager.addTamedAnimalEntry(ent);
    }
}
