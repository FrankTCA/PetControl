package org.infotoast.petcontrol;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

import org.infotoast.petcontrol.cachefile.CacheFileManager;
import org.infotoast.petcontrol.command.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class PetControl extends JavaPlugin {
    public static Logger logger;
    public static CacheFileManager cacheManager;
    public static PetControl plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.plugin = this;
        logger = getLogger();
        getCommand("petinfo").setExecutor(new PetInfoCommand(this));
        getCommand("tamepet").setExecutor(new TamePetCommand(this));
        getCommand("togglesit").setExecutor(new ToggleSitCommand(this));
        getCommand("transferpetowner").setExecutor(new TransferPetOwnerCommand(this));
        getCommand("healpet").setExecutor(new HealPetCommand(this));
        getCommand("roam").setExecutor(new RoamCommand(this));
        getCommand("follow").setExecutor(new FollowCommand(this));
        this.cacheManager = new CacheFileManager(this);
        this.cacheManager.onStartup();
        getServer().getPluginManager().registerEvents(new PetListener(), this);
        getServer().getConsoleSender().sendMessage("§l§bPetControl has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        cacheManager.onShutdown();
        getServer().getConsoleSender().sendMessage("§l§bPetControl disabled.");
    }

    public static Entity getPlayerFacingEntity(Player player) {
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
