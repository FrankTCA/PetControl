package org.infotoast.petcontrol.command;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.infotoast.petcontrol.PetControl;

public class GuardCommand implements CommandExecutor {
    private final PetControl plugin;
    public GuardCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("petcontrol.guard")) {
                Player player = (Player) sender;
                Entity playerFacing = PetControl.getPlayerFacingEntity(player);
                if (playerFacing != null) {
                    TamableAnimal tamableAnimal = (TamableAnimal) playerFacing;
                    if (tamableAnimal.isTame()) {
                        boolean isGuarded = PetControl.cacheManager.getTamedAnimalFromUUID(tamableAnimal.getUUID()).isGuarded();
                        PetControl.cacheManager.getTamedAnimalFromUUID(tamableAnimal.getUUID()).setGuarded(!isGuarded);
                        String guardStatus = (!isGuarded) ? "on" : "off";
                        sender.sendMessage("§bGuard status toggled §a§l" + guardStatus + " §r§bfor §l§a" + tamableAnimal.getName());
                        return true;
                    } else {
                        sender.sendMessage("§cThis animal is not tamed.");
                        return false;
                    }
                } else {
                    sender.sendMessage("§cYou are not facing an animal.");
                    return false;
                }
            } else {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return false;
            }
        } else {
            sender.sendMessage("§cOnly players can use this command.");
            return false;
        }
    }
}
