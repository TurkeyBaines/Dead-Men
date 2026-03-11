package io.dm.deadman.tournament.events.breach;

import io.dm.deadman.tournament.events.reward.EventRewards;
import io.dm.deadman.tournament.events.reward.RewardTier;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.HitListener;

import java.util.*;

public abstract class BreachNPC extends NPCCombat {

    private HashMap<Player, Record> damageMap;

    @Override
    public void init() {
        this.damageMap = new HashMap<>();
        npc.hitListener = new HitListener().postDamage(hit -> {
            System.out.println("HIT: " + hit.damage + " by " + hit.attacker.player.getName());
            if (hit.damage > 0) {
                addDamage(hit.attacker.player, hit.damage);
            }
        });

        npc.deathEndListener = (entity, killer, killHit) -> {
            System.out.println("JAD HAS DIED");

            EventRewards.giveReward(killer.player, RewardTier.GOD);

            this.npc.remove();
        };
    }

    @Override
    public void follow() {

    }

    @Override
    public boolean attack() {
        return false;
    }

    private class Record {
        private int damage;
        private Player player;

        Record(int damage) {
            this.damage = damage;
        }

        void addDamage(int damage) {
            this.damage += damage;
        }

        Player getPlayer() {
            return player;
        }
    }

    public void addDamage(Player player, int damage) {
        if (damageMap.containsKey(player))
            damageMap.get(player).addDamage(damage);
        else
            damageMap.put(player, new Record(damage));

    }

    private void sortDamageDealers() {
        List<Map.Entry<Player, Record>> sorted =
                new ArrayList<>(damageMap.entrySet());

        sorted.sort((a, b) -> Integer.compare(b.getValue().damage, a.getValue().damage));

        Map<Player, Record> ordered = new LinkedHashMap<>();
        for (Map.Entry<Player, Record> entry : sorted) {
            ordered.put(entry.getKey(), entry.getValue());
        }

        damageMap = new HashMap<>(ordered);
    }

}