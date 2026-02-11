package org.infotoast.petcontrol.cachefile;

import java.util.UUID;

public abstract class CacheFileEntry implements ICacheFileEntry {
    protected final int animalId;
    protected final UUID uuid;

    public CacheFileEntry(AnimalType animal, UUID uuid) {
        this.animalId = convertAnimalToId(animal);
        this.uuid = uuid;
    }

    protected static int convertAnimalToId(AnimalType animal) {
        return switch (animal) {
            case DOG -> 0;
            case CAT -> 1;
            case PARROT -> 2;
        };
    }

    protected static AnimalType convertIdToAnimal(int id) {
        return switch (id) {
            case 0 -> AnimalType.DOG;
            case 1 -> AnimalType.CAT;
            case 2 -> AnimalType.PARROT;
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };
    }

    @Override
    public AnimalType getAnimalType() {
        return convertIdToAnimal(animalId);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public abstract byte[] getAsBytes();

    public abstract CacheFileEntry readBytes(byte[] bytes);
}
