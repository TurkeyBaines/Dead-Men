package io.dm.content.areas.edgeville

import io.dm.api.message
import io.dm.api.whenObjClick
import io.dm.cache.ObjectID
import io.dm.data.impl.Help
import io.dm.model.entity.player.Player
import io.dm.model.item.actions.impl.tradepost.TradePost

/**
 * @author Leviticus
 */
object TradingPostHandle {

    init {
        whenObjClick(ObjectID.GRAND_EXCHANGE_BOOTH, "open") { player, _ ->
            openTradingPost(player)
        }
        whenObjClick(ObjectID.GRAND_EXCHANGE_BOOTH, "coffer") { player, _ ->
            TradePost.openCoffer(player)
        }
        whenObjClick(ObjectID.GRAND_EXCHANGE_BOOTH, "guide") { player, _ ->
            Help.open(player, "trading_post")
        }
    }

    private fun openTradingPost(player: Player) {
        if (player.gameMode.isIronMan) {
            player.message("Your gamemode prevents you from accessing the trading post!")
            return
        }
        player.tradePost.openViewOffers()
    }
}