package io.dm.model.entity.npc;

import io.dm.model.entity.Entity;
import io.dm.model.skills.fishing.FishingArea;

import java.util.function.Predicate;

public abstract class NPCAttributes extends Entity {

    public FishingArea fishingArea;

    public boolean minnowsFish;

    public Predicate<Entity> aggressionImmunity;

    public int ownerId = -1; //for pets

}
