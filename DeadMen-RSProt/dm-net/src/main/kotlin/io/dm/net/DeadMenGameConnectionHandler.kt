package io.dm.net

import net.rsprot.protocol.api.GameConnectionHandler
import net.rsprot.protocol.api.login.GameLoginResponseHandler
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.crypto.xtea.XteaKey
import org.slf4j.LoggerFactory

/**
 * Entry point RSProt calls once a client has passed the login handshake (RSA block
 * decrypted, username/password/auth available). This replaces the legacy
 * `io.dm.network.LoginDecoder`.
 *
 * Responsibilities (see MIGRATION.md "Login flow"):
 *  - resolve/authenticate the account (bcrypt via kronos-api),
 *  - construct the game `Player`, register it in the world,
 *  - call [GameLoginResponseHandler.writeSuccessfulResponse] with a
 *    `LoginResponse.Ok` to obtain the [net.rsprot.protocol.api.Session], then
 *  - flush the initial game state (rebuild region, interfaces, etc).
 *
 * The body below is the wiring skeleton; the account/world hookup is the seam that
 * `:DMS` fills in via [loginService].
 */
class DeadMenGameConnectionHandler(
    private val loginService: DmLoginService,
) : GameConnectionHandler<DmNetworkPlayer> {

    private val logger = LoggerFactory.getLogger(DeadMenGameConnectionHandler::class.java)

    override fun onLogin(
        responseHandler: GameLoginResponseHandler<DmNetworkPlayer>,
        loginBlock: LoginBlock<AuthenticationType>,
    ) {
        logger.info("Login request for '{}'", loginBlock.username)
        loginService.connect(responseHandler, loginBlock, reconnect = false)
    }

    override fun onReconnect(
        responseHandler: GameLoginResponseHandler<DmNetworkPlayer>,
        loginBlock: LoginBlock<XteaKey>,
    ) {
        logger.info("Reconnect request for '{}'", loginBlock.username)
        loginService.reconnect(responseHandler, loginBlock)
    }
}
