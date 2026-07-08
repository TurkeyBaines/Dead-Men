package io.dm.content.areas.ver_sinhaza

import io.dm.api.chat
import io.dm.api.event
import io.dm.api.random
import io.dm.api.whenNpcClick
import io.dm.model.entity.npc.NPC
import io.dm.model.entity.player.Player

/**
 * The interaction script for Meiyerditch citizens.
 * @author Heaven
 */
object MeiyerditchCitizen {

    init {
        for (id in intArrayOf(8212, 8328, 8329, 8330)) {
            whenNpcClick(id, 1) { player, npc ->
                player.talkTo(npc)
            }
        }
    }

    private fun Player.talkTo(npc: NPC) = event {
        when(random(6)) {
            1 -> npc.chat("I guess this will be my last day of blood tithes, one way or another.")
            2 -> npc.chat("I'm starting to think I made a mistake coming here.")
            3 -> npc.chat("It can't be that hard can it?")
            4 -> npc.chat("Soon I will be free!")
            5 -> npc.chat("I will survive the Theatre! Just like Serafina did!")
            6 -> npc.chat("Can't talk. I need to prepare myself.")
        }
    }
}