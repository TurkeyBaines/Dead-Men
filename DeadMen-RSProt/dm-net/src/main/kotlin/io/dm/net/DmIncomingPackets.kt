package io.dm.net

import net.rsprot.protocol.game.incoming.buttons.If3Button
import net.rsprot.protocol.game.incoming.misc.user.ClientCheat
import net.rsprot.protocol.game.incoming.misc.user.MoveGameClick
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepositoryBuilder
import net.rsprot.protocol.message.codec.incoming.MessageConsumer

/**
 * Seam implemented by `:DMS`: the game-side reactions to decoded client packets. This is where
 * the bodies of the legacy `io.dm.network.incoming.handlers.*` move to. `:dm-net` decodes and
 * dispatches; `:DMS` decides what happens.
 */
interface DmPacketHandlers {
    /** Map-click movement. [keyCombination] != 0 indicates a ctrl-click (run) request. */
    fun onWalk(player: DmNetworkPlayer, x: Int, z: Int, keyCombination: Int)

    /** A `::command` typed by the player (without the leading `::`). */
    fun onCommand(player: DmNetworkPlayer, command: String)

    /** A modern (if3) interface button/component action. [sub] is the child slot, [obj] the item id (or -1). */
    fun onButton(player: DmNetworkPlayer, interfaceId: Int, sub: Int, obj: Int, op: Int)
}

/**
 * Registers RSProt incoming-message listeners against a [DmPacketHandlers]. Pass this to
 * [NetworkBootstrap.start]'s `registerConsumers` argument:
 * ```kotlin
 * NetworkBootstrap.start(...) { builder -> DmIncomingPackets.register(builder, dmsHandlers) }
 * ```
 *
 * Only three representative packets are wired here to lock the pattern; the remaining ~40
 * legacy handlers (OpNpc/OpLoc/OpObj/OpPlayer, Resume* dialogs, appearance, etc.) register
 * identically — see MIGRATION.md step 4.
 */
object DmIncomingPackets {

    fun register(
        builder: GameMessageConsumerRepositoryBuilder<DmNetworkPlayer>,
        handlers: DmPacketHandlers,
    ) {
        builder.addListener(
            MoveGameClick::class.java,
            MessageConsumer<DmNetworkPlayer, MoveGameClick> { player, msg ->
                handlers.onWalk(player, msg.x, msg.z, msg.keyCombination)
            },
        )
        builder.addListener(
            ClientCheat::class.java,
            MessageConsumer<DmNetworkPlayer, ClientCheat> { player, msg ->
                handlers.onCommand(player, msg.command)
            },
        )
        builder.addListener(
            If3Button::class.java,
            MessageConsumer<DmNetworkPlayer, If3Button> { player, msg ->
                handlers.onButton(player, msg.interfaceId, msg.sub, msg.obj, msg.op)
            },
        )
    }

    /**
     * Registers a no-op consumer for **every** rev-228 incoming game message. RSProt's
     * `Session.processIncomingPackets` throws `IllegalStateException` for any decoded message whose
     * class has no registered consumer, so during Stage 2 (world push, no gameplay handlers yet) we
     * must accept-and-drop every client packet or the game tick loop crashes the moment the player
     * moves the mouse. Stage 3 replaces these with real handlers one packet at a time.
     */
    fun registerNoOps(builder: GameMessageConsumerRepositoryBuilder<DmNetworkPlayer>) {
        val noOp = MessageConsumer<DmNetworkPlayer, IncomingGameMessage> { _, _ -> }
        for (cls in ALL_INCOMING) {
            builder.addListener(cls, noOp)
        }
    }

    /** Diagnostic variant: logs the FIRST time each distinct incoming packet type is received. */
    fun registerNoOpsLogging(builder: GameMessageConsumerRepositoryBuilder<DmNetworkPlayer>) {
        registerNoOpsExcept(builder, emptySet())
    }

    /**
     * Registers the logging no-op consumer for every incoming packet type EXCEPT [handled] (which
     * the caller wires to real handlers). Lets `:DMS` claim a few packets (e.g. movement) while all
     * others are still accepted-and-dropped so [net.rsprot.protocol.api.Session.processIncomingPackets]
     * never throws on an unregistered type.
     */
    fun registerNoOpsExcept(
        builder: GameMessageConsumerRepositoryBuilder<DmNetworkPlayer>,
        handled: Set<Class<*>>,
    ) {
        val seen = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
        val noOp = MessageConsumer<DmNetworkPlayer, IncomingGameMessage> { _, msg ->
            val name = msg.javaClass.simpleName
            if (seen.add(name)) println("[IN] first $name")
        }
        for (cls in ALL_INCOMING) {
            if (cls !in handled) builder.addListener(cls, noOp)
        }
    }

    /** All concrete rev-228 [IncomingGameMessage] classes (enumerated from the osrs-228 jars). */
    private val ALL_INCOMING: List<Class<out IncomingGameMessage>> = listOf(
        net.rsprot.protocol.game.incoming.buttons.If1Button::class.java,
        net.rsprot.protocol.game.incoming.buttons.If3Button::class.java,
        net.rsprot.protocol.game.incoming.buttons.IfButtonD::class.java,
        net.rsprot.protocol.game.incoming.buttons.IfButtonT::class.java,
        net.rsprot.protocol.game.incoming.buttons.IfSubOp::class.java,
        net.rsprot.protocol.game.incoming.clan.AffinedClanSettingsAddBannedFromChannel::class.java,
        net.rsprot.protocol.game.incoming.clan.AffinedClanSettingsSetMutedFromChannel::class.java,
        net.rsprot.protocol.game.incoming.clan.ClanChannelFullRequest::class.java,
        net.rsprot.protocol.game.incoming.clan.ClanChannelKickUser::class.java,
        net.rsprot.protocol.game.incoming.clan.ClanSettingsFullRequest::class.java,
        net.rsprot.protocol.game.incoming.events.EventAppletFocus::class.java,
        net.rsprot.protocol.game.incoming.events.EventCameraPosition::class.java,
        net.rsprot.protocol.game.incoming.events.EventKeyboard::class.java,
        net.rsprot.protocol.game.incoming.events.EventMouseClick::class.java,
        net.rsprot.protocol.game.incoming.events.EventMouseMove::class.java,
        net.rsprot.protocol.game.incoming.events.EventMouseScroll::class.java,
        net.rsprot.protocol.game.incoming.events.EventNativeMouseClick::class.java,
        net.rsprot.protocol.game.incoming.events.EventNativeMouseMove::class.java,
        net.rsprot.protocol.game.incoming.friendchat.FriendChatJoinLeave::class.java,
        net.rsprot.protocol.game.incoming.friendchat.FriendChatKick::class.java,
        net.rsprot.protocol.game.incoming.friendchat.FriendChatSetRank::class.java,
        net.rsprot.protocol.game.incoming.locs.OpLoc::class.java,
        net.rsprot.protocol.game.incoming.locs.OpLoc6::class.java,
        net.rsprot.protocol.game.incoming.locs.OpLocT::class.java,
        net.rsprot.protocol.game.incoming.messaging.MessagePrivate::class.java,
        net.rsprot.protocol.game.incoming.messaging.MessagePublic::class.java,
        net.rsprot.protocol.game.incoming.misc.client.ConnectionTelemetry::class.java,
        net.rsprot.protocol.game.incoming.misc.client.DetectModifiedClient::class.java,
        net.rsprot.protocol.game.incoming.misc.client.Idle::class.java,
        net.rsprot.protocol.game.incoming.misc.client.MapBuildComplete::class.java,
        net.rsprot.protocol.game.incoming.misc.client.MembershipPromotionEligibility::class.java,
        net.rsprot.protocol.game.incoming.misc.client.NoTimeout::class.java,
        net.rsprot.protocol.game.incoming.misc.client.RSevenStatus::class.java,
        net.rsprot.protocol.game.incoming.misc.client.ReflectionCheckReply::class.java,
        net.rsprot.protocol.game.incoming.misc.client.SendPingReply::class.java,
        net.rsprot.protocol.game.incoming.misc.client.SoundJingleEnd::class.java,
        net.rsprot.protocol.game.incoming.misc.client.WindowStatus::class.java,
        net.rsprot.protocol.game.incoming.misc.user.BugReport::class.java,
        net.rsprot.protocol.game.incoming.misc.user.ClickWorldMap::class.java,
        net.rsprot.protocol.game.incoming.misc.user.ClientCheat::class.java,
        net.rsprot.protocol.game.incoming.misc.user.CloseModal::class.java,
        net.rsprot.protocol.game.incoming.misc.user.ExitFreecam::class.java,
        net.rsprot.protocol.game.incoming.misc.user.HiscoreRequest::class.java,
        net.rsprot.protocol.game.incoming.misc.user.IfCrmViewOp::class.java,
        net.rsprot.protocol.game.incoming.misc.user.MoveGameClick::class.java,
        net.rsprot.protocol.game.incoming.misc.user.MoveMinimapClick::class.java,
        net.rsprot.protocol.game.incoming.misc.user.SendSnapshot::class.java,
        net.rsprot.protocol.game.incoming.misc.user.SetChatFilterSettings::class.java,
        net.rsprot.protocol.game.incoming.misc.user.SetHeading::class.java,
        net.rsprot.protocol.game.incoming.misc.user.Teleport::class.java,
        net.rsprot.protocol.game.incoming.npcs.OpNpc::class.java,
        net.rsprot.protocol.game.incoming.npcs.OpNpc6::class.java,
        net.rsprot.protocol.game.incoming.npcs.OpNpcT::class.java,
        net.rsprot.protocol.game.incoming.objs.OpObj::class.java,
        net.rsprot.protocol.game.incoming.objs.OpObj6::class.java,
        net.rsprot.protocol.game.incoming.objs.OpObjT::class.java,
        net.rsprot.protocol.game.incoming.players.OpPlayer::class.java,
        net.rsprot.protocol.game.incoming.players.OpPlayerT::class.java,
        net.rsprot.protocol.game.incoming.resumed.ResumePCountDialog::class.java,
        net.rsprot.protocol.game.incoming.resumed.ResumePNameDialog::class.java,
        net.rsprot.protocol.game.incoming.resumed.ResumePObjDialog::class.java,
        net.rsprot.protocol.game.incoming.resumed.ResumePStringDialog::class.java,
        net.rsprot.protocol.game.incoming.resumed.ResumePauseButton::class.java,
        net.rsprot.protocol.game.incoming.social.FriendListAdd::class.java,
        net.rsprot.protocol.game.incoming.social.FriendListDel::class.java,
        net.rsprot.protocol.game.incoming.social.IgnoreListAdd::class.java,
        net.rsprot.protocol.game.incoming.social.IgnoreListDel::class.java,
    )
}
