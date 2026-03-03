package io.dm.content.npcs

import io.dm.api.chat
import io.dm.api.event
import io.dm.api.options
import io.dm.api.whenNpcClick
import io.dm.model.entity.npc.NPC
import io.dm.model.entity.player.Player
import io.dm.model.shop.ShopManager

/**
 * @author Leviticus
 */
object Runa {

    private const val RUNA = 1078

    init {
        whenNpcClick(RUNA, 1) { player, npc ->
            player.talk(npc)
        }
    }

    private fun Player.talk(woman: NPC) = event {
        woman.chat("Hello there, Are you interested in looking at the Tournament Cosmetic Shop?")
        if (options("Yes", "No") == 1) {
           ShopManager.openIfExists(player, "30c58469-c4b6-4035-a98b-100ff0b8407a")
        } else {
            woman.chat("Very well.")
        }
    }
}