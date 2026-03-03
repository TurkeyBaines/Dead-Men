package io.dm.model.activities.raids.tob.dungeon.boss;

import com.google.common.collect.Lists;
import io.dm.model.activities.raids.tob.dungeon.RoomType;
import io.dm.model.activities.raids.tob.dungeon.TheatreBoss;
import io.dm.model.activities.raids.tob.dungeon.room.TheatreRoom;
import io.dm.model.activities.raids.tob.party.TheatreParty;
import io.dm.model.combat.Hit;
import io.dm.model.combat.HitType;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.shared.listeners.HitListener;
import io.dm.model.map.Position;

import java.util.List;

import static io.dm.cache.NpcID.*;

/**
 * @author ReverendDread on 7/28/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project Kronos
 */
public class TheMaidenOfSugadinti extends TheatreBoss {

    private int stage = 0;

    public TheMaidenOfSugadinti(int id, TheatreParty party) {
        super(id, party);
        npc.hitListener = new HitListener().postDamage(hit -> {
            double ratio = ((double) npc.getHp() / npc.getMaxHp());
            if (ratio >= .70 && ratio < .5 && stage == 0) {
                npc.transform(THE_MAIDEN_OF_SUGADINTI_8361);
                nylocas();
            } else if (ratio <= .5 && ratio >= .3 && stage == 1) {
                npc.transform(THE_MAIDEN_OF_SUGADINTI_8362);
                nylocas();
            } else if (ratio <= .3 && stage == 2) {
                npc.transform(THE_MAIDEN_OF_SUGADINTI_8363);
                nylocas();
            } else if (ratio == 0) {
                npc.transform(THE_MAIDEN_OF_SUGADINTI_8364);
            }
        });
    }

    private void nylocas() {
        for (int i = 0; i < party.getUsers().size() * 2; i++) {
            NPC nylocas = new NPC(NYLOCAS_MATOMENOS).spawn(getNylocasSpawns().get(i));
            nylocas.getCombat().setAllowRetaliate(false);
            nylocas.getCombat().setAllowRespawn(false);
            nylocas.startEvent(e -> {
                nylocas.getRouteFinder().routeEntity(npc);
                e.waitForMovement(nylocas);
                nylocas.hit(new Hit().fixedDamage(nylocas.getHp()));
                npc.hit(new Hit(HitType.HEAL).fixedDamage(nylocas.getHp()));
            }).setCancelCondition(nylocas::dead);
        }
        stage++;
    }

    private List<Position> getNylocasSpawns() {
        TheatreRoom room = party.getDungeon().getRooms().get(RoomType.MAIDEN);
        return Lists.newArrayList(
            Position.of(room.convertX(3174), room.convertY(4457)),
            Position.of(room.convertX(3174), room.convertY(4435)),
            Position.of(room.convertX(3179), room.convertY(4436)),
            Position.of(room.convertX(3178), room.convertY(4457)),
            Position.of(room.convertX(3182), room.convertY(4457)),
            Position.of(room.convertX(3182), room.convertY(4435)),
            Position.of(room.convertX(3187), room.convertY(4436)),
            Position.of(room.convertX(3186), room.convertY(4457)),
            Position.of(room.convertX(3186), room.convertY(4455)),
            Position.of(room.convertX(3186), room.convertY(4437))
        );
    }

}
