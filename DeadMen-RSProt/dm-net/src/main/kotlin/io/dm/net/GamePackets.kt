package io.dm.net

import net.rsprot.protocol.api.Session
import net.rsprot.protocol.game.outgoing.interfaces.IfCloseSub
import net.rsprot.protocol.game.outgoing.interfaces.IfOpenTop
import net.rsprot.protocol.game.outgoing.interfaces.IfSetText
import net.rsprot.protocol.game.outgoing.misc.player.MessageGame
import net.rsprot.protocol.game.outgoing.misc.player.UpdateRunEnergy
import net.rsprot.protocol.game.outgoing.varp.VarpLarge
import net.rsprot.protocol.game.outgoing.varp.VarpSmall
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Thin outgoing-packet API over an RSProt [Session] — the replacement for
 * `io.dm.network.PacketSender`. Instead of hand-building `OutBuffer`s, each method queues a
 * strongly-typed RSProt [OutgoingGameMessage]; RSProt encodes and frames it.
 *
 * This intentionally implements only the unambiguous, high-frequency packets to establish the
 * pattern. The remaining ~450 outgoing message types under `net.rsprot.protocol.game.outgoing.*`
 * are mapped the same way — construct the message and pass it to [queue]. See MIGRATION.md
 * step 3 for the full `PacketSender` → RSProt correspondence.
 */
class GamePackets(private val session: Session<DmNetworkPlayer>) {

    /** Escape hatch for any message not yet given a named helper. */
    fun queue(message: OutgoingGameMessage) = session.queue(message)

    /** Flush all queued messages to the client (call once per game tick after updates). */
    fun flush() = session.flush()

    // --- chat / messages ---
    fun message(text: String, type: Int = 0, sender: String? = null) =
        queue(if (sender == null) MessageGame(type, text) else MessageGame(type, sender, text))

    // --- interfaces ---
    fun openTop(interfaceId: Int) = queue(IfOpenTop(interfaceId))

    /** [combinedId] = (interfaceId shl 16) or componentId. */
    fun closeSub(combinedId: Int) = queue(IfCloseSub(combinedId))

    fun setText(combinedId: Int, text: String) = queue(IfSetText(combinedId, text))

    // --- misc player state ---
    fun runEnergy(energy: Int) = queue(UpdateRunEnergy(energy))

    /** Sends the compact or wide varp form automatically based on [value]'s range. */
    fun varp(id: Int, value: Int) =
        queue(if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) VarpSmall(id, value) else VarpLarge(id, value))
}
