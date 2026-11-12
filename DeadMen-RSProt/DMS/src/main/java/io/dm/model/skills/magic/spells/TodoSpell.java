package io.dm.model.skills.magic.spells;

import io.dm.model.skills.magic.Spell;

public class TodoSpell extends Spell {

    private final String name;

    public TodoSpell(String name) {
        this.name = name;
        clickAction = (p, i) -> p.sendMessage("Todo click action spell: " + name);
        itemAction = (p, i) -> p.sendMessage("Todo item action spell: " + name);
        entityAction = (p, e) -> p.sendMessage("Todo entity action spell: " + name);
        objectAction = (p, o) -> p.sendMessage("Todo object action spell: " + name);
    }

    @Override
    public String toString() {
        return "TodoSpell(" + name + ")";
    }

}