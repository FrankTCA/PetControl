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
                        sender.sendMessage("§6§l| §r§1Entity UUID: §r§9" + playerFacing.getUniqueId());
                        sender.sendMessage("§6§l| §r§1Health: §r§9" + tamableAnimal.getHealth() + "/" + tamableAnimal.getMaxHealth());
                        String isTame = (tamableAnimal.isTame()) ? "Yes" : "No";
                        sender.sendMessage("§6§l| §r§1Tamed: §r§9" + isTame);
                        if (tamableAnimal.isTame()) {
                            UUID ownerUUID = Objects.requireNonNull(tamableAnimal.getOwnerReference()).getUUID();
                            if (ownerUUID != null) {
                                String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                                sender.sendMessage("§6§l| §r§1Owner: §r§9" + ownerName);
                                String isSitting = (tamableAnimal.isInSittingPose()) ? "Yes" : "No";
                                sender.sendMessage("§6§l| §r§1Sitting: §r§9" + isSitting);
                                if (tamableAnimal instanceof RoamingCat || tamableAnimal instanceof RoamingDog) {
                                    sender.sendMessage("§6§l| §r§1Roaming: §r§9Yes");
                                } else {
                                    sender.sendMessage("§6§l| §r§1Roaming: §r§9No");
                                }
                            }
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
            return true;
        }
        sender.sendMessage("§4This command must be sent by a player!");
        return false;
    }
}
