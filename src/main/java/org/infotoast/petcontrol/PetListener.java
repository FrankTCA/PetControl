package org.infotoast.petcontrol;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.infotoast.petcontrol.cachefile.RoamingAnimal;
import org.infotoast.petcontrol.cachefile.RoamingAnimalEntry;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;
import org.infotoast.petcontrol.exception.EntityNotCatOrDogException;

public class PetListener implements Listener {

    @EventHandler(priority=EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (org.bukkit.entity.Entity entity : event.getChunk().getEntities()) {
            if (entity.getType() != EntityType.CAT && entity.getType() != EntityType.WOLF) continue;
            if (PetControl.roamingTeam != null) {
                if (PetControl.roamingTeam.hasEntity(entity)) {
                    try {
                        RoamingAnimalEntry ent = PetControl.cacheManager.checkIfRoamingAnimalFromUUID(entity.getUniqueId());
                        if (ent == null) {
                            PetControl.logger.warning("Pet named " + entity.getName() + " was in roaming mode but the range info was lost from a bad restart.");
                            PetControl.logger.warning("The animal will be sitting. Please let the owner know to put the animal back on roaming.");
                            if (entity instanceof CraftCat) {
                                ((CraftCat) entity).setSitting(true);
                            } else if (entity instanceof CraftWolf) {
                                ((CraftWolf) entity).setSitting(true);
                            } else {
                                throw new EntityNotCatOrDogException("Somehow the entity is not a cat or dog? Please report this error to the developers.");
                            }
                        } else {
                            createRoamingAnimalFromCacheEntry(entity, ent);
                        }
                    } catch (EntityNotCatOrDogException e) {
                        PetControl.logger.warning("Error while loading roaming animal " + entity.getUniqueId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                // Is the animal's UUID in the cache?
                RoamingAnimalEntry ent = PetControl.cacheManager.checkIfRoamingAnimalFromUUID(entity.getUniqueId());
                if (ent != null) {
                    // This needs to wait for the scoreboard teams to be created
                    PetControl.plugin.getServer().getScheduler().runTask(PetControl.plugin, () -> createRoamingAnimalFromCacheEntry(entity, ent));
                } else {
                    // Check if animal is tamed and standing
                    if (entity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame() && !tamableAnimal.isOrderedToSit()) {
                        PetControl.logger.warning("Animal name " + tamableAnimal.getName() + " may have been in roaming mode but the range info was lost from a bad restart.");
                        PetControl.logger.warning("Please let the owner, " + tamableAnimal.getOwner().getName() + ", know to put the animal back on roaming.");
                        PetControl.logger.warning("Animal will now be sitting.");
                        tamableAnimal.setOrderedToSit(true);
                    }
                }
            }
        }
    }

    private void createRoamingAnimalFromCacheEntry(org.bukkit.entity.Entity entity, RoamingAnimalEntry entry) {
        if (entity instanceof CraftCat && entry.getAnimal().equals(RoamingAnimal.CAT)) {
            RoamingCat rcat = RoamingCat.convertFromCat(((CraftCat) entity).getHandle(),
                    entry.getCenterX(), entry.getCenterZ(), entry.getRadius(), entry.isGuarded());
        } else if (entity instanceof CraftWolf && entry.getAnimal().equals(RoamingAnimal.DOG)) {
            RoamingDog rdog = RoamingDog.convertFromWolf(((CraftWolf) entity).getHandle(),
                    entry.getCenterX(), entry.getCenterZ(), entry.getRadius(), entry.isGuarded());
        } else {
            throw new EntityNotCatOrDogException("Entity is not a cat or dog. Please report this error to the developers.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent evt) {
        PetControl.cacheManager.onAutoSave();
    }
}
