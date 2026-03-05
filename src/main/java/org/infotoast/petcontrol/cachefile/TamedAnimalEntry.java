package org.infotoast.petcontrol.cachefile;

import java.util.UUID;

public class TamedAnimalEntry extends CacheFileEntry {
    private UUID ownerUuid;
    private String ownerName;
    private boolean sitting;
    private boolean guarded;
    private boolean roaming;
    private final String name;
    public TamedAnimalEntry(AnimalType animal, UUID uuid, UUID ownerUuid, String name, String ownerName, boolean sitting, boolean guarded, boolean roaming) {
        super(animal, uuid);
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.sitting = sitting;
        this.guarded = guarded;
        this.roaming = roaming;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerUUID() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setOwnerUUID(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public boolean isSitting() {
        return sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public boolean isGuarded() {
        return guarded;
    }

    public void setGuarded(boolean guarded) {
        this.guarded = guarded;
    }

    public boolean isRoaming() {
        return roaming;
    }

    public void setRoaming(boolean roaming) {
        this.roaming = roaming;
    }

    @Override
    public byte[] getAsBytes() {
        byte entryType = 0x1;
        byte animal = (byte)animalId;
        byte[] uuidB = this.uuid.toString().getBytes();
        byte uuidLen = (byte)(uuidB.length);
        byte[] ownerUuidB = this.ownerUuid.toString().getBytes();
        byte ownerUuidLen = (byte)(ownerUuidB.length);
        byte[] ownerNameB = this.ownerName.getBytes();
        byte ownerNameLen = (byte)(ownerNameB.length);
        byte[] nameB = this.name.getBytes();
        byte nameLen = (byte)(nameB.length);

        int byteCount = 9;
        byteCount += uuidLen;
        byteCount += ownerUuidLen;
        byteCount += ownerNameLen;
        byteCount += nameLen;

        byte[] result = new byte[byteCount];
        result[0] = entryType;
        result[1] = animal;
        result[2] = uuidLen;
        for (int i = 0; i < uuidLen; i++) {
            result[i+3] = uuidB[i];
        }
        result[3+uuidLen] = ownerUuidLen;
        for (int i = 0; i < ownerUuidLen; i++) {
            result[i+4+uuidLen] = ownerUuidB[i];
        }
        result[4+uuidLen+ownerUuidLen] = ownerNameLen;
        for (int i = 0; i < ownerNameLen; i++) {
            result[i+5+uuidLen+ownerUuidLen] = ownerNameB[i];
        }
        result[5+uuidLen+ownerUuidLen+ownerNameLen] = nameLen;
        for (int i = 0; i < nameLen; i++) {
            result[i+6+uuidLen+ownerUuidLen+ownerNameLen] = nameB[i];
        }
        result[result.length-4] = (byte)(guarded ? 0x1 : 0x0);
        result[result.length-3] = (byte)(sitting ? 0x1 : 0x0);
        result[result.length-2] = (byte)(roaming ? 0x1 : 0x0);
        result[result.length-1] = 0xf;
        return result;
    }

    public static CacheFileEntry readBytes(byte[] bytes) {
        int animalId = bytes[1];
        AnimalType animalType = CacheFileEntry.convertIdToAnimal(animalId);
        int uuidLength = bytes[2];
        byte[] uuidB = new byte[uuidLength];
        for (int i = 0; i < uuidLength; i++) {
            uuidB[i] = bytes[i+3];
        }
        String uuidStr = new String(uuidB);
        UUID uuid = UUID.fromString(uuidStr);
        int ownerUuidLength = bytes[3+uuidLength];
        byte[] ownerUuidB = new byte[ownerUuidLength];
        for (int i = 0; i < ownerUuidLength; i++) {
            ownerUuidB[i] = bytes[i+4+uuidLength];
        }
        String ownerUuidStr = new String(ownerUuidB);
        UUID ownerUuid = UUID.fromString(ownerUuidStr);
        int ownerNameLength = bytes[4+uuidLength+ownerUuidLength];
        byte[] ownerNameB = new byte[ownerNameLength];
        for (int i = 0; i < ownerNameLength; i++) {
            ownerNameB[i] = bytes[i+5+uuidLength+ownerUuidLength];
        }
        String ownerName = new String(ownerNameB);
        int nameLength = bytes[5+uuidLength+ownerUuidLength+ownerNameLength];
        byte[] nameB = new byte[nameLength];
        for (int i = 0; i < nameLength; i++) {
            nameB[i] = bytes[i+6+uuidLength+ownerUuidLength+ownerNameLength];
        }
        String name = new String(nameB);
        boolean guarded = bytes[bytes.length-4] == 0x1;
        boolean sitting = bytes[bytes.length-3] == 0x1;
        boolean roaming = bytes[bytes.length-2] == 0x1;
        return new TamedAnimalEntry(animalType, uuid, ownerUuid, name, ownerName, sitting, guarded, roaming);
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.TAMED;
    }
}
