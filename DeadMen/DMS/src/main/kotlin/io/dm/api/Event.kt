package io.dm.api

import io.dm.event.GameEvent
import io.dm.model.entity.npc.NPC
import io.dm.model.entity.npc.NPCAction
import io.dm.model.entity.player.Player
import io.dm.model.inter.InterfaceHandler
import io.dm.model.inter.actions.DefaultAction
import io.dm.model.inter.actions.SimpleAction
import io.dm.model.inter.actions.SlotAction
import io.dm.model.item.Item
import io.dm.model.item.actions.ItemAction
import io.dm.model.item.actions.ItemItemAction
import io.dm.model.item.actions.ItemNPCAction
import io.dm.model.map.MapListener
import io.dm.model.map.`object`.GameObject
import io.dm.model.map.`object`.actions.ObjectAction
import io.dm.model.map.dynamic.DynamicMap

typealias PlayerObjectEvent = (Player, GameObject) -> Unit
typealias PlayerNpcEvent = (Player, NPC) -> Unit
typealias PlayerItemEvent = (Player, Item) -> Unit
typealias ItemOnNpcEvent = (Player, Item, NPC) -> Unit

fun whenObjClick(id: Int, option: Int, action: PlayerObjectEvent) = ObjectAction.register(id, option, action)

fun whenObjClick(id: Int, option: String, action: PlayerObjectEvent) = ObjectAction.register(id, option, action)

fun whenObjClick(id: Int, x: Int, y: Int, height: Int, option: String, action: PlayerObjectEvent) = ObjectAction.register(id, x, y, height, option, action)

fun whenNpcClick(id: Int, option: Int, action: PlayerNpcEvent) = NPCAction.register(id, option, action)

fun whenButtonClick(parentId: Int, childId: Int, action: SimpleAction) {
    InterfaceHandler.register(parentId) {
        it.actions[childId] = action
    }
}

fun whenButtonClick(parentId: Int, childId: Int, action: DefaultAction) {
    InterfaceHandler.register(parentId) {
        it.actions[childId] = action
    }
}

fun whenButtonClick(parentId: Int, childId: Int, action: SlotAction) {
    InterfaceHandler.register(parentId) {
        it.actions[childId] = action
    }
}

fun whenRegionEntered(regionId: Int, action: (Player) -> Unit) = MapListener.registerRegion(regionId).onEnter(action)

fun whenRegionExit(regionId: Int, action: (Player, Boolean) -> Unit) = MapListener.registerRegion(regionId).onExit(action)

fun whenMapExit(map: DynamicMap, action: (Player, Boolean) -> Unit) = MapListener.registerMap(map).onExit(action)

fun globalEvent(fn: suspend GameEvent.() -> Unit) {
    val event = GameEvent(null, fn)
    event.start()
}

fun Player.event(fn: suspend GameEvent.() -> Unit) {
    resetActions(true, true, true)
    val event = GameEvent(this, fn)
    event.start()
}

fun NPC.event(fn: suspend GameEvent.() -> Unit) {
    val event = GameEvent(this, fn)
    event.start()
}

fun whenItemOnItem(id: Int, id2: Int, fn: ItemItemAction) = ItemItemAction.register(id, id2, fn)

fun whenItemOnNpc(id: Int, id2: Int, fn: ItemOnNpcEvent) = ItemNPCAction.register(id, id2, fn)

fun whenInvClick(id: Int, option: Int, fn: PlayerItemEvent) = ItemAction.registerInventory(id, option, fn)