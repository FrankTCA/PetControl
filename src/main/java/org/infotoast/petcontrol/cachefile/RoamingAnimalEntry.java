package org.infotoast.petcontrol.cachefile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class RoamingAnimalEntry {
    private final int animalId;
    private final int radius;
    private final int centerX;
    private final int centerZ;
    private final UUID uuid;
    private final boolean guarded;
    public RoamingAnimalEntry(RoamingAnimal animal, int radius, int centerX, int centerZ, UUID uuid, boolean guarded) {
        this.animalId = convertAnimalToId(animal);
        this.radius = radius;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.uuid = uuid;
        this.guarded = guarded;
    }

    private static int convertAnimalToId(RoamingAnimal animal) {
        return switch (animal) {
            case DOG -> 0;
            case CAT -> 1;
        };
    }

    private static RoamingAnimal convertIdToAnimal(int id) {
        return switch (id) {
            case 0 -> RoamingAnimal.DOG;
            case 1 -> RoamingAnimal.CAT;
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };
    }

    public RoamingAnimal getAnimal() {
        return convertIdToAnimal(this.animalId);
    }

    public int getRadius() {
        return radius;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isGuarded() {
        return guarded;
    }

    public byte[] getAsBytes() {
        byte entryType = 0x0;
        byte animal = (byte)animalId;
        byte[] radiusB = CacheFileManager.intToBytes(radius);
        byte[] centerXB = CacheFileManager.intToBytes(centerX);
        byte[] centerZB = CacheFileManager.intToBytes(centerZ);
        byte[] uuidB = this.uuid.toString().getBytes();
        byte uuidLen = (byte)(uuidB.length);
        int byteCount = 6;
        byteCount += radiusB.length;
        byteCount += centerXB.length;
        byteCount += centerZB.length;
        byteCount += uuidB.length;

        byte[] result = new byte[byteCount];
        result[0] = entryType;
        result[1] = animal;
        for (int i = 0; i < radiusB.length; i++) {
            result[i + 2] = radiusB[i];
        }
        for (int i = 0; i < centerXB.length; i++) {
            result[i + 6] = centerXB[i];
        }
        for (int i = 0; i < centerZB.length; i++) {
            result[i + 10] = centerZB[i];
        }
        result[14] = uuidLen;
        for (int i = 0; i < uuidB.length; i++) {
            result[i + 15] = uuidB[i];
        }
        result[result.length-2] = (byte)((guarded) ? 0x1 : 0x0);
        result[result.length-1] = 0xf;
        return result;
    }

    public static RoamingAnimalEntry readBytes(byte[] bytes) {
        int animalId = bytes[1];
        byte[] radiusBytes = Arrays.copyOfRange(bytes, 2, 6);
        int radius = CacheFileManager.bytesToInt(radiusBytes);
        byte[] centerXBytes = Arrays.copyOfRange(bytes, 6, 10);
        int centerX = CacheFileManager.bytesToInt(centerXBytes);
        byte[] centerZBytes = Arrays.copyOfRange(bytes, 10, 14);
        int centerZ = CacheFileManager.bytesToInt(centerZBytes);
        int uuidLength = bytes[14];
        byte[] uuidB = Arrays.copyOfRange(bytes, 15, 15+uuidLength);
        String uuidStr = new String(uuidB, StandardCharsets.UTF_8);
        System.out.println(uuidStr + " Length " + uuidLength);
        UUID uuid = UUID.fromString(uuidStr);
        boolean guarded = bytes[bytes.length-2] == 0x1;
        RoamingAnimal animal = convertIdToAnimal(animalId);
        return new RoamingAnimalEntry(animal, radius, centerX, centerZ, uuid, guarded);
    }

    @Override
    public String toString() {
        return "RoamingAnimal " + convertIdToAnimal(animalId) + " center: " + centerX + ", " + centerZ + " radius: " + radius;
    }
}
