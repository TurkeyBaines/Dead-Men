package io.dm.deadman.tournament.events.gold_cart;

import io.dm.deadman.tournament.events.reward.EventRewards;
import io.dm.deadman.tournament.events.reward.RewardTier;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.HitListener;

import java.util.*;

public class DMM_Goblin extends NPCCombat {

    private HashMap<Player, Record> damageMap;

    @Override
    public void init() {
        this.damageMap = new HashMap<>();

        info.hitpoints = 1000;
        info.attack_ticks = 6;
        info.max_damage = 1;

        npc.hitListener = new HitListener().postDamage(hit -> {
            if (hit.damage > 0) {
                addDamage(hit.attacker.player, hit.damage);

                int hpPercent = npc.getHp() * 100 / npc.getMaxHp();
                if (info.attack_ticks != 1 && hpPercent < 30) {
                    info.attack_ticks = 1;
                    info.max_damage = 3;
                    npc.forceText("Now you will see my terror");
                } else if (info.attack_ticks != 3 && hpPercent < 60) {
                    info.attack_ticks = 3;
                    info.max_damage = 2;
                    npc.forceText("You're a bit tougher than expected!");
                } else {
                    info.attack_ticks = 6;
                }
            }
        });

        npc.deathEndListener = (entity, killer, killHit) -> {
            System.out.println("GOBLIN HAS DIED");

            EventRewards.giveReward(killer.player, RewardTier.GOD);

            this.npc.remove();
        };
    }

    @Override
    public void follow() {

    }

    @Override
    public boolean attack() {
        basicAttack();
        return true;
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

}
