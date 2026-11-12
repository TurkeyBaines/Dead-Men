package io.dm.network.incoming.handlers;

import io.dm.api.buffer.InBuffer;
import io.dm.model.entity.player.Player;
import io.dm.social.clan.ClanChat;
import io.dm.network.incoming.Incoming;
import io.dm.utility.IdHolder;

@IdHolder(ids = {53, 22})
public class ClanHandler implements Incoming {

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        String username = in.readString();
        if(opcode == 53) {
            /**
             * Join / Leave
             */
            joinLeave(player, username);
            return;
        }
        if(opcode == 22) {
            /**
             * Kick
             */
            kick(player, username);
            return;
        }
    }

    /** Join / leave (empty name = leave). Shared with the RSProt {@code FriendChatJoinLeave} bridge. */
    public static void joinLeave(Player player, String username) {
        if (username == null || username.isEmpty()) {
            player.getClanChat().leave(player, false);
        } else {
            player.getClanChat().join(player, username);
        }
    }

    /** Kick. Shared with the RSProt {@code FriendChatKick} bridge. */
    public static void kick(Player player, String username) {
        ClanChat active = player.getActiveClanChat();
        if (active != null) {
            active.kick(player, username);
        }
    }

}
