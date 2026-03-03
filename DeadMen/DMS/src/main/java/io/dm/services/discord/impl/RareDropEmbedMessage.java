package io.dm.services.discord.impl;

import io.dm.api.utils.ServerWrapper;
import io.dm.model.World;
import io.dm.services.discord.Webhook;
import io.dm.services.discord.util.Embed;
import io.dm.services.discord.util.Footer;
import io.dm.services.discord.util.Message;
import io.dm.services.discord.util.Thumbnail;

public class RareDropEmbedMessage {

    public static void sendDiscordMessage(String discordMessage, String npcDescriptiveName, int itemId) {
        if (!World.isLive()){
            return;
        }
        try {
            Webhook webhook = new Webhook("https://discordapp.com/api/webhooks/723479940929945640/QTeEH1MFFp7nR-WrSoMr3mt1gxaNUAiCzU8CM96j4sssDycDvbk69DCSD9dOtBf7Kz8S");
            Message message = new Message();

            Embed embedMessage = new Embed();
            embedMessage.setTitle("New Loot! Rare drop received!");
            embedMessage.setDescription(discordMessage + " from " + npcDescriptiveName + "!");
            embedMessage.setColor(8917522);

            /*
             * Thumbnail
             */
            Thumbnail thumbnail = new Thumbnail();
            thumbnail.setUrl("https://static.runelite.net/cache/item/icon/" + itemId + ".png");
            embedMessage.setThumbnail(thumbnail);

            /*
             * Footer
             */
            Footer footer = new Footer();
            footer.setText(World.type.getWorldName() + " - The Final Challenge!");
            embedMessage.setFooter(footer);

            /*
             * Fire the message
             */
            message.setEmbeds(embedMessage);
            webhook.sendMessage(message.toJson());
        } catch (Exception e) {
            ServerWrapper.logError("Failed to send discord embed", e);
        }
    }

}
