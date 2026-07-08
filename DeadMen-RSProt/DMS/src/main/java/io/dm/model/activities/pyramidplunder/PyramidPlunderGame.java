package io.dm.model.activities.pyramidplunder;

import io.dm.model.entity.player.Player;

/**
 * @author ReverendDread on 2/29/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project ZarosOSRS
 */
public class PyramidPlunderGame {

    //The minigame overlay
    private static final int OVERLAY = 428;

    //The player assosiated with this game.
    private Player player;

    //Duration in ticks until time is up.
    private int duration = 500;

    public static void start(Player player) {

    }

    public void pulse() {
        duration--;
    }

//    public void updateOverlay() {
//        player.getPacketSender().sendString(OVERLAY, );
//    }

}
