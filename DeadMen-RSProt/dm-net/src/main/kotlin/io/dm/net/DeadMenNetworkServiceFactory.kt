package io.dm.net

import io.netty.buffer.ByteBuf
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.api.AbstractNetworkServiceFactory
import net.rsprot.protocol.api.ChannelExceptionHandler
import net.rsprot.protocol.api.GameConnectionHandler
import net.rsprot.protocol.api.IncomingGameMessageConsumerExceptionHandler
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepository
import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepositoryBuilder
import net.rsprot.protocol.message.codec.incoming.provider.GameMessageConsumerRepositoryProvider
import org.slf4j.LoggerFactory

/**
 * Dead-Men's concrete [AbstractNetworkServiceFactory]. Building this and calling
 * [build] yields a fully-wired RSProt [net.rsprot.protocol.api.NetworkService] for
 * OSRS revision 239 — the replacement for `io.dm.api.netty.NettyServer`.
 *
 * All game-specific inputs are injected so this module stays decoupled from `:DMS`:
 *  - [rsaKeyPairInput]  : the server's private RSA key (modulus + private exponent),
 *  - [cache]            : JS5 group + Huffman access ([DmCacheProvider]),
 *  - [loginService]     : account/world hookup ([DmLoginService]),
 *  - [messageConsumers] : registration of the incoming-packet handlers.
 *
 * Note: `ports` and `supportedClientTypes` are `val` in the base class, while the
 * provider hooks below are abstract *functions* (`getX()`); we match each accordingly.
 */
class DeadMenNetworkServiceFactory(
    private val gamePort: Int,
    private val rsaKeyPairInput: RsaKeyPair,
    private val cache: DmCacheProvider,
    private val loginService: DmLoginService,
    private val messageConsumers: (GameMessageConsumerRepositoryBuilder<DmNetworkPlayer>) -> Unit = {},
) : AbstractNetworkServiceFactory<DmNetworkPlayer>() {

    override val ports: List<Int>
        get() = listOf(gamePort)

    // Desktop only for now; ANDROID / IOS can be added once their caches are served.
    override val supportedClientTypes: List<OldSchoolClientType>
        get() = listOf(OldSchoolClientType.DESKTOP)

    private val huffmanProvider: HuffmanCodecProvider by lazy {
        DefaultHuffmanCodecProvider(cache.huffmanCodec())
    }

    override fun getRsaKeyPair(): RsaKeyPair = rsaKeyPairInput

    override fun getHuffmanCodecProvider(): HuffmanCodecProvider = huffmanProvider

    override fun getJs5GroupProvider(): Js5GroupProvider =
        Js5GroupProvider { archive, group -> cache.js5Group(archive, group) }

    override fun getGameMessageConsumerRepositoryProvider():
        GameMessageConsumerRepositoryProvider<DmNetworkPlayer> =
        GameMessageConsumerRepositoryProvider {
            val builder = GameMessageConsumerRepositoryBuilder<DmNetworkPlayer>()
            // :DMS registers all incoming-packet listeners here (Walk, ObjAction,
            // NpcAction, Chat, ButtonClick, ...) — see MIGRATION.md "Incoming packets".
            messageConsumers(builder)
            builder.build()
        }

    override fun getGameConnectionHandler(): GameConnectionHandler<DmNetworkPlayer> =
        DeadMenGameConnectionHandler(loginService)

    /**
     * RSProt's default [ChannelExceptionHandler] is a **no-op** that silently swallows every
     * channel/encoder exception and just closes the connection — which makes a malformed outgoing
     * packet look like a mysterious instant disconnect. Override it to actually log the cause (both
     * the Netty channel pipeline and the incoming-consumer path), so encode/decode failures surface.
     */
    override fun getExceptionHandlers(): ExceptionHandlers<DmNetworkPlayer> =
        ExceptionHandlers(
            ChannelExceptionHandler { ctx, cause ->
                logger.error("RSProt channel exception on {}", ctx.channel(), cause)
            },
            IncomingGameMessageConsumerExceptionHandler<DmNetworkPlayer> { _, message, cause ->
                logger.error("RSProt incoming-consumer exception on {}", message.javaClass.simpleName, cause)
            },
        )

    private companion object {
        private val logger = LoggerFactory.getLogger(DeadMenNetworkServiceFactory::class.java)
    }
}

/** Backing type for [DeadMenNetworkServiceFactory.getJs5GroupProvider]'s SAM lambda. */
private inline fun Js5GroupProvider(crossinline block: (Int, Int) -> ByteBuf): Js5GroupProvider =
    object : Js5GroupProvider {
        override fun provide(archive: Int, group: Int): ByteBuf = block(archive, group)
    }

/** Backing type for the consumer-repository provider SAM. */
private inline fun GameMessageConsumerRepositoryProvider(
    crossinline block: () -> GameMessageConsumerRepository<DmNetworkPlayer>,
): GameMessageConsumerRepositoryProvider<DmNetworkPlayer> =
    object : GameMessageConsumerRepositoryProvider<DmNetworkPlayer> {
        override fun provide(): GameMessageConsumerRepository<DmNetworkPlayer> = block()
    }
