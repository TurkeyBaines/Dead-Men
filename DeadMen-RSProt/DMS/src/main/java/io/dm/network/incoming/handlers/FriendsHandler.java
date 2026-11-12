package io.dm.network.incoming.handlers;

import io.dm.api.buffer.InBuffer;
import io.dm.api.filestore.utility.Huffman;
import io.dm.model.entity.player.Player;
import io.dm.social.SocialList;
import io.dm.social.SocialMember;
import io.dm.social.SocialRank;
import io.dm.social.clan.ClanChat;
import io.dm.network.incoming.Incoming;
import io.dm.services.Loggers;
import io.dm.services.Punishment;
import io.dm.utility.IdHolder;

@IdHolder(ids = {84, 80, 48, 56, 93, 67})
public class FriendsHandler implements Incoming {

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        String name;
        if(opcode == 67) {
            /**
             * Rank friend
             */
            int rank = in.readByte();
            name = in.readString();
            rankFriend(player, name, rank);
            return;
        }
        name = in.readString();
        if(opcode == 80) {
            /**
             * Add friend
             */
            SocialList.handle(player, name, 1);
            return;
        }
        if(opcode == 48) {
            /**
             * Delete friend
             */
            SocialList.handle(player, name, 2);
            return;
        }
        if(opcode == 84) {
            /**
             * Add ignore
             */
            SocialList.handle(player, name, 3);
            return;
        }
        if(opcode == 56) {
            /**
             * Delete ignore
             */
            SocialList.handle(player, name, 4);
            return;
        }
        if(opcode == 93) {
            /**
             * Private message
             */
            privateMessage(player, name, Huffman.decrypt(in, 100));
            return;
        }
    }

    /**
     * Rank friend (legacy opcode 67; RSProt {@code FriendChatSetRank}).
     */
    public static void rankFriend(Player player, String name, int rank) {
        SocialRank socialRank = SocialRank.get(rank, null);
        if (socialRank == null) {
            return;
        }
        SocialMember friend = player.socialList.getFriend(name);
        if (friend == null || friend.rank == socialRank) {
            return;
        }
        friend.rank = socialRank;
        friend.resend();
        ClanChat cc = player.getClanChat();
        if (cc.inClan(friend.playerName)) {
            cc.update(false);
        }
    }

    /**
     * Private message (legacy opcode 93; RSProt incoming {@code MessagePrivate}).
     */
    public static void privateMessage(Player player, String name, String message) {
        if(Punishment.isMuted(player)) {
            if(player.shadowMute)
                player.sendPM(name, message);
            else
                player.sendMessage("You're muted and can't talk.");
            return;
        }
        SocialList.sendPrivateMessage(player, player.getClientGroupId(), name, message);
        Loggers.logPrivateChat(player.getName(), player.getIp(), name, message);
    }

}
