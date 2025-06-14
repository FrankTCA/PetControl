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

                    RoamingAnimalEntry ent = RoamingAnimalEntry.readBytes(b);
                    System.out.println(ent);
                    data.addAnimal(ent);
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
