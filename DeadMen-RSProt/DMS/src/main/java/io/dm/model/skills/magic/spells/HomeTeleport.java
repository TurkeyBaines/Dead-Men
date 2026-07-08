package io.dm.model.skills.magic.spells;

import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Position;
import io.dm.model.skills.magic.Spell;
import io.dm.model.skills.magic.spells.modern.ModernTeleport;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HomeTeleport extends Spell {

    public static final HomeTeleport MODERN = new HomeTeleport(p -> ModernTeleport.teleport(p, World.HOME));

    public static final HomeTeleport ANCIENT = new HomeTeleport(p -> ModernTeleport.teleport(p, World.HOME));

    public static final HomeTeleport LUNAR = new HomeTeleport(p -> ModernTeleport.teleport(p, World.HOME));

    public static final HomeTeleport ARCEUUS = new HomeTeleport(p -> ModernTeleport.teleport(p, World.HOME));

    private static final List<HomeTeleportOverride> OVERRIDES = new LinkedList<>();

    private HomeTeleport(Consumer<Player> consumer) {
        clickAction = (p, i) -> {
            if(p.wildernessLevel > 0) {
                if(p.getCombat().isDefending(16)) {
                    p.sendMessage("You can't cast this spell while in combat in the wilderness.");
                    return;
                }
            } else if(p.pvpAttackZone) {
                if(p.getCombat().isDefending(16)) {
                    p.sendMessage("You can't cast this spell while in combat in a pvp zone.");
                    return;
                }
            }
            consumer.accept(p);
        };
    }

    private static class HomeTeleportOverride {
        Predicate<Player> condition;
        Position destination;

        public HomeTeleportOverride(Predicate<Player> condition, Position destination) {
            this.condition = condition;
            this.destination = destination;
        }
    }

    public static void registerHomeTeleportOverride(Predicate<Player> condition, Position destination) {
        OVERRIDES.add(new HomeTeleportOverride(condition, destination));
    }

    public static Position getHomeTeleportOverride(Player player) {
        for (HomeTeleportOverride teleportOverride : OVERRIDES) {
            if (teleportOverride.condition.test(player))
                return teleportOverride.destination;
        }
        return null;
    }
}