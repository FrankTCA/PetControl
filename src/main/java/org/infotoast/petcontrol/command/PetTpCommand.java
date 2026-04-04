package org.infotoast.petcontrol.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.cachefile.TamedAnimalEntry;

public class PetTpCommand implements CommandExecutor {
    private final PetControl plugin;

    public PetTpCommand(PetControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Entity playerFacing = PetControl.getPlayerFacingEntity(player);
            Entity target;
            String name;
            if (args.length > 0) {
                name = args[0];
                if (name.equalsIgnoreCase("SELECTED")) {
                    target = playerFacing;
                } else {
                    TamedAnimalEntry[] entriesWithName = PetControl.cacheManager.getTamedAnimalEntriesByAnimalNameAndOwnerUUID(player.getUniqueId(), name);
                    if (entriesWithName.length == 0) {
                        player.sendMessage("§4No pets found with that name.");
                        return true;
                    } else if (entriesWithName.length > 1) {
                        player.sendMessage("§4Multiple pets found with that name. Please use §c/pselect --uuid §4and then run §c/pettp SELECTED [to|from]");
                        return true;
                    } else {
                        target = Bukkit.getEntity(entriesWithName[0].getUUID());
                    }
                }

                if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("to"))) {
                    player.teleport(target);
                    sender.sendMessage("§aTeleported to §b" + target.getName());
                    return true;
                } else if (args.length == 2 && args[1].equalsIgnoreCase("from")) {
                    target.teleport(player);
                    sender.sendMessage("§aTeleported §b" + target.getName() + " §ato you.");
                    return true;
                }
            }
            sender.sendMessage("§4Invalid command usage.");
            return false;
        }
        sender.sendMessage("§4This command must be run by a player.");
        return false;
    }
}
