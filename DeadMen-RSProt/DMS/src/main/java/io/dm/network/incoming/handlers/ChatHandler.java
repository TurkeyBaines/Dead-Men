package io.dm.network.incoming.handlers;

import io.dm.api.buffer.InBuffer;
import io.dm.api.filestore.utility.Huffman;
import io.dm.api.utils.IPMute;
import io.dm.model.entity.player.Player;
import io.dm.social.clan.ClanChat;
import io.dm.network.incoming.Incoming;
import io.dm.services.Loggers;
import io.dm.services.Punishment;
import io.dm.utility.IdHolder;

import java.util.Arrays;
import java.util.List;

@IdHolder(ids = {32})
public class ChatHandler implements Incoming {

    /** The message was fully handled (dropped, command, clan chat, force text); nothing to display publicly. */
    public static final int HANDLED = 0;
    /** Display the message as normal public chat. */
    public static final int SHOW = 1;
    /** Shadow-muted: display the message to the sender only. */
    public static final int SHADOW = 2;

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        int type = in.readByte();
        int effects = in.readShort();
        String message = Huffman.decrypt(in, 100);
        int result = process(player, type == 2, type, effects, message);
        if(result != HANDLED) {
            player.getChatUpdate().set(result == SHADOW, effects, player.getClientGroupId(), type, message);
        }
    }

    /**
     * Shared public/clan chat pipeline for both the legacy opcode-32 handler and the RSProt
     * {@code MessagePublic} bridge: mute / IP-mute / shadow-mute, bad-word filter, clan routing,
     * {@code ::}/{@code ;;} commands, {@code !} admin force-text and chat logging.
     *
     * @param clan true when the message is addressed to the clan/friends chat channel
     * @return {@link #HANDLED}, {@link #SHOW} or {@link #SHADOW}
     */
    public static int process(Player player, boolean clan, int type, int effects, String message) {
        boolean shadow = false;
        if(IPMute.isIPMuted(player.getIp()) || Punishment.isMuted(player)) {
            if(!player.shadowMute) {
                player.sendMessage("You're muted and can't talk.");
                return HANDLED;
            }
            shadow = true;
        }
        if(message == null || message.isEmpty()) {
            /* how does this even happen? */
            return HANDLED;
        }

        /*
        * If player mentions any of these words don't send it.
        */
        List<String> badWords = Arrays.asList("runite", "ikov", "zenyte", "roatz", "alora", "nigger");
        for(String word : badWords) {
            if (message.toLowerCase().contains(word)) {
                return HANDLED;
            }
        }
        if(clan) {
            if(message.startsWith("/")) {
                message = message.substring(1);
            }
            ClanChat active = player.getActiveClanChat();
            if (active != null) {
                active.message(player, player.getClientGroupId(), message);
            }
            Loggers.logClanChat(player.getUserId(), player.getName(), player.getIp(), message);
            return HANDLED;
        }
        if(message.length() >= 3) {
            char c1 = message.charAt(0);
            char c2 = message.charAt(1);
            if((c1 == ':' && c2 == ':') || (c1 == ';' && c2 == ';')) {
                CommandHandler.handle(player, message.substring(2));
                return HANDLED;
            }
            if (c1 == '!' && player.isAdmin()) {
                player.forceText(message.substring(1));
                return HANDLED;
            }
        }
        Loggers.logPublicChat(player.getUserId(), player.getName(), player.getIp(), message, type, effects);
        return shadow ? SHADOW : SHOW;
    }

}
