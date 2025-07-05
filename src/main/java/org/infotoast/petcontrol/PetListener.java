package org.infotoast.petcontrol;

import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.infotoast.petcontrol.cachefile.RoamingAnimal;
import org.infotoast.petcontrol.cachefile.RoamingAnimalEntry;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;

public class PetListener implements Listener {
    public static boolean entityAddLock = false;

    @EventHandler(priority=EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!entityAddLock) {
            for (org.bukkit.entity.Entity entity : event.getChunk().getEntities()) {
                //if (entity instanceof CraftCat || entity instanceof CraftWolf) {
                RoamingAnimalEntry ent = PetControl.cacheManager.checkIfRoamingAnimalFromUUID(entity.getUniqueId());
                if (ent != null) {
                    if (ent.getAnimal().equals(RoamingAnimal.CAT)) {
                        RoamingCat rcat = RoamingCat.convertFromCat(((CraftCat) entity).getHandle(),
                                ent.getCenterX(), ent.getCenterZ(), ent.getRadius(), ent.isGuarded());
                    } else {
                        RoamingDog rdog = RoamingDog.convertFromWolf(((CraftWolf) entity).getHandle(),
                                ent.getCenterX(), ent.getCenterZ(), ent.getRadius(), ent.isGuarded());
                    }
                }
                //}
            }
        }
    }
}
