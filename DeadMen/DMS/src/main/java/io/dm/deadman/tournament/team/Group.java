package io.dm.deadman.tournament.team;

import io.dm.deadman.Deadman;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Group {

    @Getter
    String ID;
    String owner;
    String player1 = null, player2 = null;

    @Getter
    String name;

    @Getter
    int kills, deaths;

    public Group(String ID, String name, String owner) {
        this.ID = ID;
        this.name = name;
        this.owner = owner;
        kills = 0;
        deaths = 0;
    }

    public List<Player> getMembers() {
        List<Player> temp = new ArrayList<>();
        temp.add(World.getPlayer(owner));
        if (player1 != null) temp.add(World.getPlayer(player1));
        if (player2 != null) temp.add(World.getPlayer(player2));

        return temp;
    }

    public List<String> getMemberStrings() {
        return Arrays.asList(
                owner,
                player1 != null ? player1 : null,
                player2 != null ? player2 : null
        );
    }

    public void incKills(Player p) {
        if (p.getName().equalsIgnoreCase(owner) ||
                (player1 != null && p.getName().equalsIgnoreCase(player1)) ||
                (player2 != null && p.getName().equalsIgnoreCase(player2))) {
            kills++;
        }
    }

    public void incDeaths(Player p) {
        if (p.getName().equalsIgnoreCase(owner) ||
                (player1 != null && p.getName().equalsIgnoreCase(player1)) ||
                (player2 != null && p.getName().equalsIgnoreCase(player2))) {
            deaths++;
        }
    }

    public boolean isFull() {
        return owner != null && (Deadman.getConfig().TEAM_SIZE_MAX.asInt > 1 && player1 != null) && (Deadman.getConfig().TEAM_SIZE_MAX.asInt > 2 && player2 != null);
    }

    public boolean leave(Player p) {
        String name = p.getName();
        if (owner.equalsIgnoreCase(name)) {
            if (player1 == null) {
                //TODO - cull the team
            } else {
                owner = player1;
                if (player2 != null) {
                    player1 = player2;
                    player2 = null;
                } else {
                    player1 = null;
                }
            }
            p.groupID = null;
            getMembers().forEach(p1 -> p1.sendMessage(p.getName() + " has left the Group!"));
            return true;
        } else if (player1.equalsIgnoreCase(name)) {
            if (player2 != null) {
                player1 = player2;
                player2 = null;
            } else {
                player1 = null;
            }
            p.groupID = null;
            getMembers().forEach(p1 -> p1.sendMessage(p.getName() + " has left the Group!"));
            return true;
        } else if (player2.equalsIgnoreCase(name)) {
            player2 = null;
            p.groupID = null;

            getMembers().forEach(p1 -> p1.sendMessage(p.getName() + " has left the Group!"));
            return true;
        }
        return false;
    }

    public boolean join(Player p) {
        if (isFull())
            return false;

        String name = p.getName();
        if (player1 == null)
            player1 = name;
        else
            player2 = name;

        getMembers().forEach(p1 -> p1.sendMessage(p.getName() + " has joined the Group!"));

        p.groupID = ID;
        return true;
    }

    public boolean isOwner(Player p) {
        String name = p.getName();
        return owner.equalsIgnoreCase(name);
    }

    public int size() {
        int count = 0;
        if (owner != null)
            count++;
        if (player1 != null)
            count++;
        if (player2 != null)
            count++;
        return count;
    }
}
