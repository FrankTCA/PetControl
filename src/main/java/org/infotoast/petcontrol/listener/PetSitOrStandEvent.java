package org.infotoast.petcontrol.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PetSitOrStandEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Entity entity;
    private final UUID owner;
    private final boolean sitting;

    public PetSitOrStandEvent(Entity entity, UUID owner, boolean sitting) {
        this.entity = entity;
        this.owner = owner;
        this.sitting = sitting;
    }

    public Entity getEntity() {
        return entity;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isSitting() {
        return sitting;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
