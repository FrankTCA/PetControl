package org.infotoast.petcontrol.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.cachefile.TamedAnimalEntry;

import java.util.UUID;

public class PSelectCommand implements CommandExecutor {
    private final PetControl plugin;
    public PSelectCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender.hasPermission("petcontrol.pselect")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Entity playerFacing = PetControl.getPlayerFacingEntity((Player) sender);
                    if (playerFacing == null) {
                        sender.sendMessage("§4You are not facing a valid entity to select.");
                        return true;
                    }
                    PetControl.setSelection((Player)sender, playerFacing);
                    setTaskToExpire(((Player)sender).getUniqueId());
                    sender.sendMessage("§bSelected entity: §l§a" + playerFacing.getName());
                    return true;
                }
                sender.sendMessage("You must be a player to use this command in this way. Please see usage below for console/command block use.");
                return false;
            } else if (args.length == 1) {
                if (sender instanceof Player) {
                    UUID ownerUUID = ((Player) sender).getUniqueId();
                    String name = args[0];
                    TamedAnimalEntry[] matchingEntries = PetControl.cacheManager.getTamedAnimalEntriesByAnimalNameAndOwnerUUID(ownerUUID, name);
                    if (matchingEntries.length == 0) {
                        sender.sendMessage("§4No tamed animals owned by you found with the name §c" + name);
                        return false;
                    }
                    if (matchingEntries.length > 1) {
                        sender.sendMessage("§4You have multiple tamed animals with that name. Please search with --uuid.");
                        return false;
                    }
                    PetControl.setSelectionByUUID(((Player)sender).getUniqueId(), matchingEntries[0].getUUID());
                    setTaskToExpire(ownerUUID);
                    sender.sendMessage("§bSelected entity: §l§a" + matchingEntries[0].getName());
                    return true;
                }
                sender.sendMessage("You must be a player to use this command searching by name. Please see usage below for console/command block use.");
                return false;
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("--name")) {
                    return onCommand(sender, command, label, new String[] {args[1]});
                } else if (args[0].equalsIgnoreCase("--uuid")) {
                    TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(UUID.fromString(args[1]));
                    if (tae == null) {
                        sender.sendMessage("§4No tamed animals that belong to you found with the UUID §c" + args[1]);
                        return false;
                    }
                    UUID senderUUID;
                    if (sender instanceof Player) {
                        senderUUID = ((Player) sender).getUniqueId();
                    } else {
                        // Command block and console
                        senderUUID = PetControl.NULL_UUID;
                    }
                    PetControl.setSelectionByUUID(senderUUID, tae.getUUID());
                    setTaskToExpire(senderUUID);
                    sender.sendMessage("§bSelected entity: §l§a" + tae.getName());
                    return true;
                }
            }
            sender.sendMessage("§4Invalid command usage.");
            return false;
        }
        sender.sendMessage("§4You do not have permission to use this command.");
        return true;
    }

    private void setTaskToExpire(UUID playerUUID) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> PetControl.clearSelection(playerUUID), 20 * 60 * 10);
    }
}
