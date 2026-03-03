package io.dm.services.discord.impl;

import io.dm.api.utils.ServerWrapper;
import io.dm.model.World;
import io.dm.services.discord.Webhook;
import io.dm.services.discord.util.Embed;
import io.dm.services.discord.util.Footer;
import io.dm.services.discord.util.Message;
import io.dm.services.discord.util.Thumbnail;

public class ShutdownEmbedMessage {

    public static void sendDiscordMessage(String shutdownMessage) {
        if (!World.isLive()){
            return;
        }
        try {
            Webhook webhook = new Webhook("https://discordapp.com/api/webhooks/639305351564623882/5y_4r4uy6emG1VptJ6lf7fHO77nE8UYC7R3EhzwOyjkB2sHM_77E_ydgo9lBoTBIWT0z");
            Message message = new Message();

            Embed embedMessage = new Embed();
            embedMessage.setTitle("Shutdown Announcement");
            embedMessage.setDescription(shutdownMessage);
            embedMessage.setColor(8917522);

            /*
             * Thumbnail
             */
            Thumbnail thumbnail = new Thumbnail();
            thumbnail.setUrl("https://static.runelite.net/cache/item/icon/" + 13307 + ".png");
            embedMessage.setThumbnail(thumbnail);

            /*
             * Footer
             */
            Footer footer = new Footer();
            footer.setText(World.type.getWorldName() + " - Waste Your Life One Skill At A Time!");
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
