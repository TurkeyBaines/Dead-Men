package io.dm.services.discord.impl;

import io.dm.api.utils.ServerWrapper;
import io.dm.model.World;
import io.dm.services.discord.Webhook;
import io.dm.services.discord.util.Embed;
import io.dm.services.discord.util.Footer;
import io.dm.services.discord.util.Message;

public class TournamentEmbedMessage {

    public static void sendDiscordMessage(String presetName, String minutes) {
        if (!World.isLive()){
            return;
        }
        try {
            Webhook webhook = new Webhook("https://discordapp.com/api/webhooks/724832730944110612/XsPyRFkx-EiNNle3UFzrmV1Nmahn7J6PUEne89RU7cRrHwY2smjTw5fs2uLzTpv26OpP");
            Message message = new Message();

            Embed embedMessage = new Embed();
            embedMessage.setTitle("Tournament System");
            embedMessage.setDescription("The " + presetName + " tournament will begin in **" + minutes + " minutes**. Login and type ::tournament to join!");
            embedMessage.setColor(8917522);

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
