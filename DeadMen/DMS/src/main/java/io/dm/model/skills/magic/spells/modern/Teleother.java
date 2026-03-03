package io.dm.model.skills.magic.spells.modern;

import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.inter.utils.Config;
import io.dm.model.item.Item;
import io.dm.model.skills.magic.Spell;
import io.dm.model.skills.magic.rune.Rune;
import io.dm.model.stat.StatType;

public enum Teleother {

    LUMBRIDGE(74, 74, 3222, 3218, "Lumbridge", Rune.SOUL.toItem(1), Rune.LAW.toItem(1), Rune.EARTH.toItem(1)),
    FALADOR(82, 82, 2965, 3379, "Falador", Rune.SOUL.toItem(1), Rune.LAW.toItem(1), Rune.WATER.toItem(1)),
    CAMELOT(90, 100, 2757, 3478, "Camelot", Rune.SOUL.toItem(2), Rune.LAW.toItem(1));

    public final int levelReq, x, y;
    public final double exp;
    public final String location;
    public Item[] runes;

    Teleother(int levelReq, double exp, int x, int y, String location, Item... runes) {
        this.levelReq = levelReq;
        this.exp = exp;
        this.x = x;
        this.y = y;
        this.location = location;
        this.runes = runes;
    }

    private boolean teleport(Player player, Entity target) {
        if(target.player == null) {
            player.sendMessage("You cannot teleport a NPC.."); //todo - get actual message lmfao
            return false;
        }
        if(Config.ACCEPT_AID.get(target.player) == 0) {
            player.sendMessage("That player won't let you teleport them."); //todo - get actual message lmfao
            return false;
        }
        if(target.player.isVisibleInterface(Interface.TELEOTHER)) {
            player.sendMessage("This player is busy."); //todo - get actual message lmfao
            return false;
        }

        player.animate(1818);
        player.graphics(343);
        player.getStats().addXp(StatType.Magic, exp, true);

        target.player.openInterface(InterfaceType.MAIN, Interface.TELEOTHER);
        target.player.getPacketSender().sendString(Interface.TELEOTHER, 89, player.getName());
        target.player.getPacketSender().sendString(Interface.TELEOTHER, 91, location);
        target.player.teleotherActive = this;
        return true;
    }

    public Spell toSpell() {
        Spell spell = new Spell();
        spell.registerEntity(levelReq, runes, this::teleport);
        return spell;
    }

    static {
        InterfaceHandler.register(Interface.TELEOTHER, h -> {
            h.actions[97] = (SimpleAction) player -> {
                if(player.teleotherActive == null)
                    return;
                player.getMovement().startTeleport(event -> {
                    player.animate(1816);
                    player.graphics(342);
                    event.delay(3);
                    player.getMovement().teleport(player.teleotherActive.x, player.teleotherActive.y, 0);
                });
            };
            h.actions[99] = (SimpleAction) player -> player.closeInterface(InterfaceType.MAIN);
        });
    }

}