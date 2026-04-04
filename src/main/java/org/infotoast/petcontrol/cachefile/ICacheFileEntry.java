package org.infotoast.petcontrol.cachefile;

import java.util.UUID;

public interface ICacheFileEntry {
    public AnimalType getAnimalType();
    public UUID getUUID();
    public byte[] getAsBytes();
    public EntryType getEntryType();
    public void setUUID(UUID uuid);
}
