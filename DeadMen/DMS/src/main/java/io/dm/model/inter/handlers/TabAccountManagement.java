package io.dm.model.inter.handlers;

import io.dm.cache.Color;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerGroup;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.inter.dialogue.ItemDialogue;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.services.XenUsername;

public class TabAccountManagement {

    private static final int OSPVP_CREDITS = 13190;
    private static final String FORUM_INBOX_URL = "https://community.kronos.rip/index.php?conversations/";
    private static final String VOTE_URL = World.type.getWebsiteUrl() + "/voting";
    private static final String HISCORES = World.type.getWebsiteUrl() + "/highscores";

    static {
        /**
         * Send the interface when our player logs in
         */
        //LoginListener.register(player -> player.getPacketSender().sendAccountManagement(getDonatorRank(player), getUsername(player), player.getUnreadPMs()));

        /**
         * Interface buttons
         */
        InterfaceHandler.register(Interface.ACCOUNT_MANAGEMENT, h -> {
            h.actions[3] = (SimpleAction) p -> p.dialogue( new MessageDialogue("We may bring a cosmetic store in the future, but all p2w has been removed."));
            h.actions[8] = (SimpleAction) p -> p.dialogue( new MessageDialogue("We may bring a cosmetic store in the future, but all p2w has been removed."));
            h.actions[15] = (SimpleAction) p -> p.dialogue( new MessageDialogue("We can show new update information here maybe?"));
            h.actions[22] = (SimpleAction) p -> p.dialogue(  new MessageDialogue("We may bring a cosmetic store in the future, but all p2w has been removed.") );
            h.actions[29] = (SimpleAction) p -> p.dialogue(new OptionsDialogue("Would you like to vote now?",
                    new Option("Yes", () -> p.openUrl(World.type.getWorldName() + " Vote", VOTE_URL)),
                    new Option("No", p::closeDialogue)
                    ));
            h.actions[32] = (SimpleAction) p -> p.dialogue( new MessageDialogue("We need to hook this up to a server highscores list."));
        });
    }

    public static String getUsername(Player player) {
        PlayerGroup clientGroup = player.getClientGroup();
        return (clientGroup.clientImgId != -1 ? clientGroup.tag() + " " : "") + player.getName();
    }

    public static String getDonatorRank(Player player) {
        if (player.isGroup(PlayerGroup.ZENYTE)) {
            return PlayerGroup.ZENYTE.tag() + " Godlike";
        } else if (player.isGroup(PlayerGroup.ONYX)) {
            return PlayerGroup.ONYX.tag() + " Master";
        } else if (player.isGroup(PlayerGroup.DRAGONSTONE)) {
            return PlayerGroup.DRAGONSTONE.tag() + " Ultimate";
        } else if (player.isGroup(PlayerGroup.DIAMOND)) {
            return PlayerGroup.DIAMOND.tag() + " Extreme";
        } else if (player.isGroup(PlayerGroup.RUBY)) {
            return PlayerGroup.RUBY.tag() + " Super";
        } else if (player.isGroup(PlayerGroup.SAPPHIRE)) {
            return PlayerGroup.SAPPHIRE.tag() + " Regular";
        } else {
            return "Unranked";
        }
    }

}
