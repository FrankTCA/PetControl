package org.infotoast.petcontrol.cachefile;

import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;
import org.infotoast.petcontrol.exception.EntityNotTamableException;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CacheFileManager {
    private PetControl plugin;
    private File cacheFile;
    private CacheFile data;
    public CacheFileManager(PetControl plugin) {
        this.plugin = plugin;
    }

    public static byte[] intToBytes(int i) {
        return new byte[] {
                (byte)(i >> 24),
                (byte)(i >> 16),
                (byte)(i >> 8),
                (byte)(i),
        };
        /*byte[] bytes = new byte[Integer.BYTES+1];
        bytes[0] = (byte)((i < 0) ? 0x1 : 0x2);
        for (int j = 1; j < bytes.length; j++) {
            bytes[bytes.length-j-1] = (byte) (i & 0xFF);
            i >>= 8;
        }
        return bytes;*/
    }

    public static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                ((bytes[3] & 0xFF));
        /*int i = 0;
        for (int j = 1; j < bytes.length; j++) {
            i = (i << 8) + (bytes[j] & 0xFF);
        }
        if (bytes[0] == 0x1) {
            return -i;
        } else {
            return i;
        }*/
    }

    public void onStartup() {
        // First, see if cache file exists
        try {
            String fileName = plugin.getDataFolder().getPath() + File.separator + "cache.dat";
            this.cacheFile = new File(fileName);
            if (this.cacheFile.exists() && !(this.cacheFile.length() == 0)) {
                CacheFileLoader loader = new CacheFileLoader(this.cacheFile);
                this.data = loader.getFromFile();
            } else {
                plugin.getDataFolder().mkdir();
                this.cacheFile.createNewFile();
                this.data = new CacheFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFile() {
        try (FileOutputStream fos = new FileOutputStream(this.cacheFile, false)) {
            fos.write(data.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addRoamingCat(RoamingCat cat) {
        RoamingAnimalEntry entry = new RoamingAnimalEntry(AnimalType.CAT,
                cat.getRoamingRadius(), cat.getRoamingCenterX(), cat.getRoamingCenterZ(), cat.getUUID(), cat.isGuarded(), cat.isOrderedToSit());

        data.addAnimal(entry);
    }

    public void addRoamingDog(RoamingDog dog) {
        RoamingAnimalEntry entry = new RoamingAnimalEntry(AnimalType.DOG,
                dog.getRoamingRadius(), dog.getRoamingCenterX(), dog.getRoamingCenterZ(), dog.getUUID(), dog.isGuarded(), dog.isOrderedToSit());

        data.addAnimal(entry);
    }

    public void removeByUUID(UUID uuid, EntryType entType) {
        for (int i = 0; i < data.animals.size(); i++) {
            CacheFileEntry entry = data.animals.get(i);
            if (entry.getUUID().equals(uuid) && entry.getEntryType() == entType) {
                data.animals.remove(i);
            }
        }
    }

    public AnimalType getAnimalTypeFromEntity(org.bukkit.entity.Entity entity) {
        if (entity instanceof org.bukkit.entity.Cat) {
            return AnimalType.CAT;
        } else if (entity instanceof org.bukkit.entity.Wolf) {
            return AnimalType.DOG;
        } else if (entity instanceof org.bukkit.entity.Parrot) {
            return AnimalType.PARROT;
        } else if (entity instanceof org.bukkit.entity.Nautilus) {
            return AnimalType.NAUTILUS;
        } else if (entity instanceof org.bukkit.entity.ZombieNautilus) {
            return AnimalType.ZOMBIE_NAUTILUS;
        } else {
            throw new EntityNotTamableException("Entity is not a tamable animal. Please report this error to the developers.");
        }
    }

    public RoamingAnimalEntry getRoamingAnimalFromUUID(UUID uuid) {
        for (CacheFileEntry ent : data.animals) {
            if (ent.getUUID().equals(uuid)) {
                if (ent.getEntryType() == EntryType.ROAMING) {
                    return (RoamingAnimalEntry) ent;
                }
            }
        }
        return null;
    }

    public TamedAnimalEntry getTamedAnimalFromUUID(UUID uuid) {
        for (CacheFileEntry ent : data.animals) {
            if (ent.getUUID().equals(uuid)) {
                if (ent.getEntryType() == EntryType.TAMED) {
                    return (TamedAnimalEntry) ent;
                }
            }
        }
        return null;
    }

    public TamedAnimalEntry[] getTamedAnimalEntriesByAnimalNameAndOwnerUUID(UUID owner, String name) {
        List<TamedAnimalEntry> matchingEntries = new ArrayList<>();
        for (CacheFileEntry ent : data.animals) {
            if (ent.getEntryType() == EntryType.TAMED) {
                TamedAnimalEntry tamedAnimalEntry = (TamedAnimalEntry) ent;
                if (tamedAnimalEntry.getName().equals(name) && tamedAnimalEntry.getOwnerUUID().equals(owner)) {
                    matchingEntries.add(tamedAnimalEntry);
                }
            }
        }
        return matchingEntries.toArray(new TamedAnimalEntry[matchingEntries.size()]);
    }

    public void addTamedAnimalEntry(TamedAnimalEntry entry) {
        data.addAnimal(entry);
    }

    public void onShutdown() {
        saveCache();
    }

    public void saveCache() {
        writeFile();
    }

    public void onAutoSave() {
        saveCache();
    }
}
