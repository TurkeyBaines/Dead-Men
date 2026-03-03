package io.dm.content.activities.tournament

import io.dm.api.utils.Tuple
import io.dm.model.entity.player.Player
import io.dm.model.item.Item
import io.dm.model.skills.magic.SpellBook
import io.dm.model.skills.prayer.Prayer
import io.dm.model.stat.StatType

/**
 * Represents a set of attributes to be associated to a specific [TournamentPlaylist] type.
 * @author Heaven
 */
interface TournamentAttributes {

    /**
     * The playlist key associated with these attributes.
     */
    fun playlist(): TournamentPlaylist

    /**
     * Defines a specific spellbook to be used while in the tournament. The return value is
     * the varp value used.
     */
    fun spellbookLoadout(): SpellBook {
        return SpellBook.MODERN
    }

    /**
     * Defines a set of equipment items to be used while in the tournament.
     */
    fun equipmentLoadout(): Array<Tuple<Int, Item>>

    /**
     * Defines a set of inventory items to be given while in the tournament.
     */
    fun inventoryLoadout(): Array<Tuple<Int, Item>>

    /**
     * Defines any skill modifications needing to be made while in the tournament.
     */
    fun skillLoadout(): Array<Tuple<StatType, Int>>

    /**
     * Applies any remaining custom logic to the tournament.
     */
    fun applyExtras(player: Player) {

    }

    /**
     * Defines any prayers that are unable to be used while in the tournament.
     */
    fun restrictedPrayers(): Array<Prayer> {
        return arrayOf(Prayer.PROTECT_ITEM)
    }
}