package io.dm.net

import net.rsprot.protocol.api.Session

/**
 * The server-side connection type that RSProt is parameterised over (`R`).
 *
 * RSProt is generic in the "player/connection" object: every message consumer,
 * the [net.rsprot.protocol.api.GameConnectionHandler] and the login flow all hand
 * you an instance of this type. In the legacy stack this role was played by the
 * concrete `io.dm.model.entity.player.Player`.
 *
 * We deliberately keep it as a thin interface here so `:dm-net` stays independent
 * of the giant `:DMS` model. `:DMS` will make its real `Player` implement this
 * (or provide an adapter) during the migration — see MIGRATION.md, step "Bind Player".
 */
interface DmNetworkPlayer {

    /** Display name, used for logging / reconnection bookkeeping. */
    val username: String

    /**
     * The active RSProt session once login has completed, or `null` while the
     * player is still in the login handshake. Set by the connection handler via
     * [net.rsprot.protocol.api.login.GameLoginResponseHandler.writeSuccessfulResponse].
     */
    var session: Session<DmNetworkPlayer>?
}
