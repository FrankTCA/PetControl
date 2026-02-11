package org.infotoast.petcontrol.cachefile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class RoamingAnimalEntry extends CacheFileEntry {
    private final int radius;
    private final int centerX;
    private final int centerZ;
    private final boolean guarded;
    private boolean sitting;
    public RoamingAnimalEntry(AnimalType animal, int radius, int centerX, int centerZ, UUID uuid, boolean guarded, boolean sitting) {
        super(animal, uuid);
        this.radius = radius;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.guarded = guarded;
        this.sitting = sitting;
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

    public boolean isGuarded() {
        return guarded;
    }

    public boolean isSitting() {
        return sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public byte[] getAsBytes() {
        byte entryType = 0x0;
        byte animal = (byte)animalId;
        byte[] radiusB = CacheFileManager.intToBytes(radius);
        byte[] centerXB = CacheFileManager.intToBytes(centerX);
        byte[] centerZB = CacheFileManager.intToBytes(centerZ);
        byte[] uuidB = this.uuid.toString().getBytes();
        byte uuidLen = (byte)(uuidB.length);
        int byteCount = 7;
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
        result[result.length-3] = (byte)((guarded) ? 0x1 : 0x0);
        result[result.length-2] = (byte)((sitting) ? 0x1 : 0x0);
        result[result.length-1] = 0xf;
        return result;
    }

    public static CacheFileEntry readBytes(byte[] bytes) {
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
        UUID uuid = UUID.fromString(uuidStr);
        boolean guarded = bytes[bytes.length-3] == 0x1;
        boolean sitting = bytes[bytes.length-2] == 0x1;
        AnimalType animal = convertIdToAnimal(animalId);
        return new RoamingAnimalEntry(animal, radius, centerX, centerZ, uuid, guarded, sitting);
    }

    // Reads legacy cache files that do not include whether the animal is sitting
    public static CacheFileEntry readBytesLegacy(byte[] bytes) {
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
        UUID uuid = UUID.fromString(uuidStr);
        boolean guarded = bytes[bytes.length-2] == 0x1;
        AnimalType animal = convertIdToAnimal(animalId);
        return new RoamingAnimalEntry(animal, radius, centerX, centerZ, uuid, guarded, true);
    }

    @Override
    public String toString() {
        return "AnimalType " + convertIdToAnimal(animalId) + " center: " + centerX + ", " + centerZ + " radius: " + radius + " uuid: " + uuid + " guarded: " + guarded;
    }
}
