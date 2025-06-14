package org.infotoast.petcontrol.cachefile;

import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.customanimals.RoamingCat;
import org.infotoast.petcontrol.customanimals.RoamingDog;

import java.io.*;
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
        try (FileOutputStream fos = new FileOutputStream(this.cacheFile)) {
            fos.write(data.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addRoamingCat(RoamingCat cat) {
        RoamingAnimalEntry entry = new RoamingAnimalEntry(RoamingAnimal.CAT,
                cat.getRoamingRadius(), cat.getRoamingCenterX(), cat.getRoamingCenterZ(), cat.getUUID(), cat.isGuarded());

        data.addAnimal(entry);
    }

    public void addRoamingDog(RoamingDog dog) {
        RoamingAnimalEntry entry = new RoamingAnimalEntry(RoamingAnimal.DOG,
                dog.getRoamingRadius(), dog.getRoamingCenterX(), dog.getRoamingCenterZ(), dog.getUUID(), dog.isGuarded());

        data.addAnimal(entry);
    }

    public void removeByUUID(UUID uuid) {
        for (int i = 0; i < data.animals.size(); i++) {
            RoamingAnimalEntry entry = data.animals.get(i);
            if (entry.getUuid().equals(uuid)) {
                data.animals.remove(i);
            }
        }
    }

    public RoamingAnimalEntry checkIfRoamingAnimalFromUUID(UUID uuid) {
        for (RoamingAnimalEntry ent : data.animals) {
            if (ent.getUuid().equals(uuid)) {
                return ent;
            }
        }
        return null;
    }

    public void onShutdown() {
        writeFile();
    }
}
