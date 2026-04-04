package org.infotoast.petcontrol.listener;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.*;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.cachefile.AnimalType;
import org.infotoast.petcontrol.cachefile.EntryType;
import org.infotoast.petcontrol.cachefile.RoamingAnimalEntry;
import org.infotoast.petcontrol.cachefile.TamedAnimalEntry;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;
import org.infotoast.petcontrol.exception.EntityNotCatOrDogException;
import org.infotoast.petcontrol.exception.EntityNotTamableException;

import java.util.UUID;

public class PetListener implements Listener {

    @EventHandler(priority=EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (org.bukkit.entity.Entity entity : event.getChunk().getEntities()) {
            if (entity.getType() != EntityType.CAT && entity.getType() != EntityType.WOLF && entity.getType() != EntityType.PARROT) continue;
            // Roaming logic
            boolean entityIsRoaming = false;
            if (PetControl.roamingTeam != null) {
                if (PetControl.roamingTeam.hasEntity(entity)) {
                    try {
                        RoamingAnimalEntry ent = PetControl.cacheManager.getRoamingAnimalFromUUID(entity.getUniqueId());
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

                            TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(entity.getUniqueId());
                            if (tae == null) {
                                PetControl.logger.warning("Tamed animal entry not found for UUID: " + entity.getUniqueId());
                                PetControl.logger.warning("Ignore this error if you just updated.");
                                return;
                            } else {
                                // TODO: Find way not to perform this operation twice
                                PetControl.cacheManager.getTamedAnimalFromUUID(entity.getUniqueId()).setSitting(true);
                            }
                            entityIsRoaming = true;
                        } else {
                            TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(entity.getUniqueId());
                            PetControl.cacheManager.removeByUUID(entity.getUniqueId(), EntryType.TAMED);
                            if (tae == null) {
                                if (((CraftEntity)entity).getHandleRaw() instanceof TamableAnimal tamableAnimal) {
                                    tae = new TamedAnimalEntry(PetControl.cacheManager.getAnimalTypeFromEntity(entity), entity.getUniqueId(), tamableAnimal.getOwner().getUUID(), entity.getName(), Bukkit.getOfflinePlayer(tamableAnimal.getOwnerReference().getUUID()).getName(), tamableAnimal.isOrderedToSit(), ent.isGuarded(), true);
                                }
                            }
                            Entity newEntity = createRoamingAnimalFromCacheEntry(entity, ent);
                            tae.setUUID(newEntity.getUUID());
                            tae.setGuarded(tae.isGuarded());
                            tae.setRoaming(true);
                            PetControl.cacheManager.removeByUUID(entity.getUniqueId(), EntryType.ROAMING);
                            PetControl.cacheManager.removeByUUID(entity.getUniqueId(), EntryType.TAMED);
                            PetControl.cacheManager.addTamedAnimalEntry(tae);
                            ent.setUUID(newEntity.getUUID());
                        }
                    } catch (EntityNotCatOrDogException e) {
                        PetControl.logger.warning("Error while loading roaming animal " + entity.getUniqueId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                // Is the animal's UUID in the cache?
                RoamingAnimalEntry ent = PetControl.cacheManager.getRoamingAnimalFromUUID(entity.getUniqueId());
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

            if (!entityIsRoaming && entity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame()) {
                if (PetControl.cacheManager.getTamedAnimalFromUUID(entity.getUniqueId()) == null) {
                    PetControl.logger.warning("Entity " + entity.getUniqueId() + " is tame but not in cache. Creating new entry.");
                    PetControl.logger.warning("Ignore this error if you have not loaded the animal since updating PetControl.");
                    AnimalType animalType;
                    if (entity instanceof CraftCat) {
                        animalType = AnimalType.CAT;
                    } else if (entity instanceof CraftWolf) {
                        animalType = AnimalType.DOG;
                    } else if (entity instanceof CraftParrot) {
                        animalType = AnimalType.PARROT;
                    } else if (entity instanceof CraftNautilus) {
                        animalType = AnimalType.NAUTILUS;
                    } else if (entity instanceof CraftZombieNautilus) {
                        animalType = AnimalType.ZOMBIE_NAUTILUS;
                    } else {
                        throw new EntityNotTamableException("Entity is not a tamable animal while chunk loading. Please report this error to the developers.");
                    }
                    UUID ownerUUID = tamableAnimal.getOwnerReference().getUUID();
                    TamedAnimalEntry tae = new TamedAnimalEntry(animalType, entity.getUniqueId(), ownerUUID, entity.getName(), Bukkit.getOfflinePlayer(ownerUUID).getName(), tamableAnimal.isOrderedToSit(), false, false);
                }
            }
        }
    }

    /*@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent evt) {
        org.bukkit.entity.Entity entity = evt.getRightClicked();
        if (evt.getRightClicked() instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame()) {
            TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(tamableAnimal.getUUID());
            PetControl.cacheManager.removeByUUID(tamableAnimal.getUUID(), EntryType.TAMED);
            if (tae == null) {
                PetControl.logger.warning("Tamed animal entry on right click not found for UUID: " + tamableAnimal.getUUID());
                PetControl.logger.warning("Ignore this error if you just updated.");
                UUID ownerUUID = tamableAnimal.getOwnerReference().getUUID();
                AnimalType animalType = PetControl.cacheManager.getAnimalTypeFromEntity(entity);
                tae = new TamedAnimalEntry(animalType, entity.getUniqueId(), ownerUUID, entity.getName(), Bukkit.getOfflinePlayer(ownerUUID).getName(), tamableAnimal.isOrderedToSit(), false, false);
            }
            tae.setSitting(tamableAnimal.isOrderedToSit());
            PetControl.cacheManager.addTamedAnimalEntry(tae);
        }
    }*/

    /*@EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPoseChange(EntityPoseChangeEvent evt) {
        System.out.println("Entity pose change event triggered for entity: " + evt.getEntity().getUniqueId());
        if (((CraftEntity)evt.getEntity()).getHandleRaw() instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame()) {
            TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(tamableAnimal.getUUID());
            boolean isSitting = PetControl.isAnimalSitting(evt.getEntity());
            System.out.println("Entity pose change event triggered for tamed animal: " + tamableAnimal.getUUID() + " isSitting: " + isSitting);
            if (tae != null) {
                tae.setSitting(isSitting);
                PetControl.cacheManager.removeByUUID(tamableAnimal.getUUID(), EntryType.TAMED);
                PetControl.cacheManager.addTamedAnimalEntry(tae);
            } else {
                RoamingAnimalEntry rae = PetControl.cacheManager.getRoamingAnimalFromUUID(evt.getEntity().getUniqueId());
                boolean isRoaming = (rae != null);
                tae = new TamedAnimalEntry(PetControl.cacheManager.getAnimalTypeFromEntity(evt.getEntity()), evt.getEntity().getUniqueId(), tamableAnimal.getOwnerReference().getUUID(), evt.getEntity().getName(), Bukkit.getOfflinePlayer(tamableAnimal.getOwnerReference().getUUID()).getName(), isSitting, false, isRoaming);
                PetControl.cacheManager.addTamedAnimalEntry(tae);
            }
        }
    }*/

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPetSitOrStandEvent(PetSitOrStandEvent evt) {
        TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(evt.getEntity().getUniqueId());
        if (tae != null) {
            tae.setSitting(evt.isSitting());
            PetControl.cacheManager.removeByUUID(evt.getEntity().getUniqueId(), EntryType.TAMED);
            PetControl.cacheManager.addTamedAnimalEntry(tae);
        } else {
            tae = new TamedAnimalEntry(PetControl.cacheManager.getAnimalTypeFromEntity(evt.getEntity()), evt.getEntity().getUniqueId(), evt.getOwner(), evt.getEntity().getName(), Bukkit.getOfflinePlayer(evt.getOwner()).getName(), evt.isSitting(), false, false);
            PetControl.cacheManager.addTamedAnimalEntry(tae);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityTameEvent(EntityTameEvent evt) {
        TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(evt.getEntity().getUniqueId());
        if (tae != null) {
            // Why is the animal being retamed?
            PetControl.logger.warning("Animal with UUID " + evt.getEntity().getUniqueId() + " is being retamed. This warning should not appear.");
            return;
        }
        tae = new TamedAnimalEntry(PetControl.cacheManager.getAnimalTypeFromEntity(evt.getEntity()), evt.getEntity().getUniqueId(), evt.getOwner().getUniqueId(), evt.getEntity().getName(), evt.getOwner().getName(), PetControl.isAnimalSitting(evt.getEntity()), false, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent evt) {
        System.out.println("Entity damage event triggered for entity: " + evt.getEntity().getUniqueId());
        if (((CraftEntity)evt.getEntity()).getHandleRaw() instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame()) {
            System.out.println("Entity damage event triggered for tamed animal: " + tamableAnimal.getUUID());
            TamedAnimalEntry tae = PetControl.cacheManager.getTamedAnimalFromUUID(tamableAnimal.getUUID());
            if (tae != null) {
                System.out.println("Tamed animal entry found for UUID: " + tamableAnimal.getUUID() + "Guarded: " + tae.isGuarded());
                if (tae.isGuarded()) {
                    evt.setCancelled(true);
                }
            }
        }
    }

    private Entity createRoamingAnimalFromCacheEntry(org.bukkit.entity.Entity entity, RoamingAnimalEntry entry) {
        if (entity instanceof CraftCat && entry.getAnimalType().equals(AnimalType.CAT)) {
            RoamingCat rcat = RoamingCat.convertFromCat(((CraftCat) entity).getHandle(),
                    entry.getCenterX(), entry.getCenterZ(), entry.getRadius(), false);
            return rcat;
        } else if (entity instanceof CraftWolf && entry.getAnimalType().equals(AnimalType.DOG)) {
            RoamingDog rdog = RoamingDog.convertFromWolf(((CraftWolf) entity).getHandle(),
                    entry.getCenterX(), entry.getCenterZ(), entry.getRadius(), false);
            return rdog;
        } else {
            throw new EntityNotCatOrDogException("Entity is not a cat or dog. Please report this error to the developers.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent evt) {
        PetControl.cacheManager.onAutoSave();
    }
}
