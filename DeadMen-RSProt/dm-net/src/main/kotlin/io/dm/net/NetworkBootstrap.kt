package io.dm.net

import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepositoryBuilder
import org.slf4j.LoggerFactory

/**
 * Stands up the RSProt [NetworkService] — the single call that replaces
 * `io.dm.api.netty.NettyServer.start(...)` in `io.dm.Server.main`.
 *
 * Usage from `:DMS`:
 * ```
 * NetworkBootstrap.start(
 *     gamePort = 43594,
 *     rsaKeyPair = RsaKeys.loadOrGenerate(File("server-data/rsa.properties")),
 *     cache = DmsCacheProvider(filestore),      // implements DmCacheProvider
 *     loginService = DmsLoginService(world),    // implements DmLoginService
 * ) { builder -> DmsIncomingPackets.register(builder) }
 * ```
 */
object NetworkBootstrap {

    private val logger = LoggerFactory.getLogger(NetworkBootstrap::class.java)

    @OptIn(ExperimentalStdlibApi::class)
    fun start(
        gamePort: Int,
        rsaKeyPair: RsaKeyPair,
        cache: DmCacheProvider,
        loginService: DmLoginService,
        registerConsumers: (GameMessageConsumerRepositoryBuilder<DmNetworkPlayer>) -> Unit = {},
    ): NetworkService<DmNetworkPlayer> {
        val factory = DeadMenNetworkServiceFactory(
            gamePort = gamePort,
            rsaKeyPairInput = rsaKeyPair,
            cache = cache,
            loginService = loginService,
            messageConsumers = registerConsumers,
        )
        val service = factory.build()
        service.start()
        logger.info("RSProt NetworkService listening on port {}", gamePort)
        return service
    }
}
