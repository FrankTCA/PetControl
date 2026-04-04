package org.infotoast.petcontrol;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BlockIterator;

import org.infotoast.petcontrol.cachefile.CacheFileManager;
import org.infotoast.petcontrol.command.*;
import org.infotoast.petcontrol.listener.PetListener;
import org.infotoast.petcontrol.listener.PetSitOrStandTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class PetControl extends JavaPlugin {
    public static Logger logger;
    public static CacheFileManager cacheManager;
    public static PetControl plugin;
    public static ScoreboardManager scoreboardManager;
    public static Team roamingTeam;
    private PetSitOrStandTracker petSitOrStandTracker;
    public static HashMap<UUID, UUID> currentlySelectedPets = new HashMap<>();
    public static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final List<String> SUPPORTED_ANIMAL_TYPES =
            List.of("CAT", "WOLF", "HORSE", "FOX", "PARROT", "RABBIT", "COPPER_GOLEM", "IRON_GOLEM", "SNOW_GOLEM",
                    "ALLAY", "AXOLOTL", "CAMEL", "DONKEY", "HAPPY_GHAST", "MULE", "STRIDER", "LLAMA", "NAUTILUS");

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
        getCommand("guard").setExecutor(new GuardCommand(this));
        getCommand("pselect").setExecutor(new PSelectCommand(this));
        this.cacheManager = new CacheFileManager(this);
        this.cacheManager.onStartup();
        // Start tracking pets sitting/standing
        petSitOrStandTracker = new PetSitOrStandTracker(this);
        petSitOrStandTracker.start();
        getServer().getPluginManager().registerEvents(new PetListener(), this);
        // Create scoreboard team for roaming animals
        getServer().getScheduler().runTask(this, () -> {
            scoreboardManager = getServer().getScoreboardManager();
            if (scoreboardManager.getMainScoreboard().getTeam("roaming") == null) {
                roamingTeam = scoreboardManager.getMainScoreboard().registerNewTeam("roaming");
            } else {
                roamingTeam = scoreboardManager.getMainScoreboard().getTeam("roaming");
            }
        });
        getServer().getConsoleSender().sendMessage("§l§bPetControl has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        cacheManager.onShutdown();
        petSitOrStandTracker.stop();
        getServer().getConsoleSender().sendMessage("§l§bPetControl disabled.");
    }

    public static void setSelection(Player player, Entity entity) {
        setSelectionByUUID(player.getUniqueId(), entity.getUniqueId());
    }

    public static void setSelectionByUUID(UUID player, UUID entity) {
        currentlySelectedPets.put(player, entity);
    }

    public static void clearSelection(UUID player) {
        currentlySelectedPets.remove(player);
    }

    public static Entity getPlayerFacingEntity(CommandSender sender) {
        UUID senderUUID;
        if (!(sender instanceof Player)) {
            senderUUID = NULL_UUID;
        } else {
            senderUUID = ((Player) sender).getUniqueId();
        }
        UUID current = currentlySelectedPets.get(senderUUID);
        if (current != null) {
            return plugin.getServer().getEntity(current);
        } else {
            if (sender instanceof Player player) {
                return getPlayerFacingEntityRaw(player);
            } else {
                sender.sendMessage("§4You are not a player and therefore cannot face an animal. Please use §a/pselect --uuid <uuid> §4to select an entity.");
                return null;
            }
        }
    }

    public static Entity getFacingEntityForSelection(Player player) {
        return getPlayerFacingEntityRaw(player);
    }

    private static Entity getPlayerFacingEntityRaw(Player player) {
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

    public static boolean isAnimalSitting(Entity entity) {
        if (((CraftEntity)entity).getHandleRaw() instanceof TamableAnimal tamableAnimal) {
            return tamableAnimal.isOrderedToSit();
        }
        logger.warning("isAnimalSitting called with non-TamableAnimal entity: " + entity.getType() + "with UUID " + entity.getUniqueId());
        return false;
    }
}
