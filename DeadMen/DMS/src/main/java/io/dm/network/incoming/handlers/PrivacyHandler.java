package io.dm.network.incoming.handlers;

import io.dm.api.buffer.InBuffer;
import io.dm.model.entity.player.Player;
import io.dm.network.incoming.Incoming;
import io.dm.social.SocialList;
import io.dm.utility.IdHolder;

@IdHolder(ids = {21})
public class PrivacyHandler implements Incoming {

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        int publicSetting = in.readByte();
        int privateSetting = in.readByte();
        int tradeSetting = in.readByte();
        int oldPrivacy = player.socialList.privacy;
        player.socialList.privacy = privateSetting;
        if (oldPrivacy != privateSetting) {
            SocialList.notifyFriends(player.getName(), true);
        }
    }

}
