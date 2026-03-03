package io.dm.services.discord.impl;

import io.dm.content.activities.event.impl.eventboss.EventBossType;
import io.dm.services.discord.DiscordConnection;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 6/13/2020
 */
public class EventBossEmbedMessage {
    public static void sendDiscordMessage(EventBossType boss, String location) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("An Event Boss Has Spawned!");
        eb.setDescription(location);
        eb.setImage(boss.getEmbedUrl());
        eb.setColor(new Color(0xB00D03));
        DiscordConnection.post(0, eb.build());
        System.out.println("Embed sent!");
    }
}
