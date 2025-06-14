package org.infotoast.petcontrol;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.infotoast.petcontrol.cachefile.RoamingAnimal;
import org.infotoast.petcontrol.cachefile.RoamingAnimalEntry;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;

public class PetListener implements Listener {
    public static boolean entityAddLock = false;

    @EventHandler
    public void onEntityAddToWorld(EntityAddToWorldEvent event) {
        if (!entityAddLock) {
            if (event.getEntityType().equals(EntityType.CAT) || event.getEntityType().equals(EntityType.WOLF)) {
                RoamingAnimalEntry ent = PetControl.cacheManager.checkIfRoamingAnimalFromUUID(event.getEntity().getUniqueId());
                if (ent != null) {
                    BukkitScheduler scheduler = PetControl.plugin.getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(PetControl.plugin, () -> {
                        if (ent.getAnimal().equals(RoamingAnimal.CAT)) {
                            RoamingCat rcat = RoamingCat.convertFromCat(((CraftCat)event.getEntity()).getHandle(),
                                    ent.getCenterX(), ent.getCenterZ(), ent.getRadius(), ent.isGuarded());
                        } else {
                            RoamingDog rdog = RoamingDog.convertFromWolf(((CraftWolf)event.getEntity()).getHandle(),
                                    ent.getCenterX(), ent.getCenterZ(), ent.getRadius(), ent.isGuarded());
                        }
                    }, 5L);
                }
            }
        }
    }
}
