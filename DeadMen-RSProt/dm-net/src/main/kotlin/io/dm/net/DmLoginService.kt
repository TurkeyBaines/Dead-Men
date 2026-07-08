package io.dm.net

import net.rsprot.protocol.api.login.GameLoginResponseHandler
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.crypto.xtea.XteaKey

/**
 * Seam implemented by `:DMS`: turns a decrypted RSProt login block into an authenticated,
 * world-registered game player and writes the login response.
 *
 * This is where the legacy `io.dm.network.LoginDecoder` account logic (bcrypt check,
 * ban/lock checks, save-file load, world capacity) moves to. `:dm-net` owns the transport;
 * `:DMS` owns the account + world.
 */
interface DmLoginService {

    /** Fresh login (auth block carries username/password/OTP). */
    fun connect(
        responseHandler: GameLoginResponseHandler<DmNetworkPlayer>,
        loginBlock: LoginBlock<AuthenticationType>,
        reconnect: Boolean,
    )

    /** Reconnect (client already had a session; auth block is the XTEA key set). */
    fun reconnect(
        responseHandler: GameLoginResponseHandler<DmNetworkPlayer>,
        loginBlock: LoginBlock<XteaKey>,
    )
}
