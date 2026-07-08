package io.dm.net

import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.login.GameLoginResponseHandler
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.loginprot.outgoing.util.AuthenticatorResponse

/**
 * Helpers `:DMS`'s [DmLoginService] uses to complete the RSProt login handshake once it has
 * authenticated the account and allocated a world index. This is the "write the response"
 * half of the legacy `io.dm.network.LoginDecoder`.
 */
object DmLogin {

    /**
     * Accept a login: sends `LoginResponse.Ok`, obtains the [Session], binds it to [player],
     * and (optionally) registers a disconnect hook (logout/save). Returns the session so the
     * caller can immediately push initial game state (region rebuild, tabs, stats, ...).
     *
     * Field order matches RSProt's `LoginResponse.Ok(authResponse, staffModLevel, playerMod,
     * index, member, accountHash, userId, userHash)`.
     */
    fun accept(
        responseHandler: GameLoginResponseHandler<DmNetworkPlayer>,
        loginBlock: LoginBlock<*>,
        player: DmNetworkPlayer,
        index: Int,
        staffModLevel: Int = 0,
        playerMod: Boolean = false,
        member: Boolean = true,
        accountHash: Long = -1L,
        userId: Long = -1L,
        userHash: Long = -1L,
        onDisconnect: Runnable? = null,
    ): Session<DmNetworkPlayer> {
        val ok = LoginResponse.Ok(
            AuthenticatorResponse.NoAuthenticator,
            staffModLevel,
            playerMod,
            index,
            member,
            accountHash,
            userId,
            userHash,
        )
        val session = responseHandler.writeSuccessfulResponse(ok, loginBlock)
        player.session = session
        if (onDisconnect != null) session.setDisconnectionHook(onDisconnect)
        return session
    }

    /** Reject a login with any [LoginResponse] (e.g. `LoginResponse.InvalidUsernameOrPassword`). */
    fun reject(
        responseHandler: GameLoginResponseHandler<DmNetworkPlayer>,
        response: LoginResponse,
    ) {
        responseHandler.writeFailedResponse(response)
    }
}
