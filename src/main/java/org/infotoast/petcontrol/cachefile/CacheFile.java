package org.infotoast.petcontrol.cachefile;

import java.util.ArrayList;

public class CacheFile {
    public ArrayList<CacheFileEntry> animals = new ArrayList<>();

    public void addAnimal(CacheFileEntry animal) {
        animals.add(animal);
    }

    public byte[] getBytes() {
        ArrayList<byte[]> bytes = new ArrayList<>();
        int length = 0;
        for (RoamingAnimalEntry animal : roamingAnimals) {
            byte[] b = animal.getAsBytes();
            length += b.length;
            bytes.add(b);
        }

        byte[] resultArray = new byte[length];
        int count = 0;
        for (byte[] b : bytes) {
            for (int i = 0; i < b.length; i++) {
                resultArray[count++] = b[i];
            }
        }

        return resultArray;
    }
}
