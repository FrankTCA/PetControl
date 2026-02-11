package org.infotoast.petcontrol.cachefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class CacheFileLoader {
    private File cacheFile;
    public CacheFileLoader(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public CacheFile getFromFile() {
        CacheFile data = new CacheFile();
        try {
            FileInputStream fis = new FileInputStream(this.cacheFile);
            byte[] file = fis.readAllBytes();
            fis.close();

            ArrayList<Byte> temp = new ArrayList<>();
            // Cache version 2 - TamedAnimalEntry and sitting bit
            if (file[0] == 0x2) {
                for (int i = 1; i < file.length; i++) {
                    temp.add(file[i]);
                    // Add entry when delimiter encountered
                    if (file[i] == 0xf) {
                        Byte[] B = new Byte[temp.size()];
                        temp.toArray(B);
                        byte[] b = new byte[temp.size()];
                        for (int j = 0; j < B.length; i++) {
                            b[j] = B[j];
                        }

                        temp.clear();

                        if (b[0] == 0x0) {
                            CacheFileEntry ent = RoamingAnimalEntry.readBytes(b);
                            data.addAnimal(ent);
                        } else if (b[0] == 0x1) {
                            CacheFileEntry ent = TamedAnimalEntry.readBytes(b);
                            data.addAnimal(ent);
                        }
                    }
                }
            } else {
                for (int i = 0; i < file.length; i++) {
                    temp.add(file[i]);
                    if (file[i] == 0xf) {
                        Byte[] B = new Byte[temp.size()];
                        temp.toArray(B);
                        byte[] b = new byte[temp.size()];
                        for (int j = 0; j < B.length; j++) {
                            b[j] = B[j];
                        }
                        temp.clear();

                        CacheFileEntry ent = (RoamingAnimalEntry)RoamingAnimalEntry.readBytes(b);
                        data.addAnimal(ent);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
