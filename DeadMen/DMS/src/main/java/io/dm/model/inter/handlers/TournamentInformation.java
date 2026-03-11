package io.dm.model.inter.handlers;

import io.dm.cache.Color;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.Tournament;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.InterfaceType;

import static io.dm.cache.Color.*;
import static io.dm.cache.Color.BLUE;

public class TournamentInformation {

    public static void send(Player p) {
        int interId = 116;
        p.openInterface(InterfaceType.MAIN, interId);
        p.getPacketSender().sendString(interId, 4, "Tournament Information Board");
        p.getPacketSender().sendString(interId, 6, DARK_RED.wrap("-- Server Info --"));
        p.getPacketSender().sendString(interId, 7, ORANGE.wrap("\t Current Tournament Stage: ") + BLUE.wrap(Deadman.getStage().stageName().name()));
        p.getPacketSender().sendString(interId, 8, ORANGE.wrap("\t Time Until Next Stage: ") + BLUE.wrap(Deadman.timeUntilChange()));

        int xOffset = 0;
        switch (Deadman.getStage().stageName()) {
            case LOBBY:
                xOffset = 0;
                p.getPacketSender().sendString(interId, 10, DARK_RED.wrap("--- Lobby Info ---"));
                p.getPacketSender().sendString(interId, 11, DARK_RED.wrap("- Next Tournament -"));
                p.getPacketSender().sendString(interId, 12, ORANGE.wrap("\t # Length: ") + BLUE.wrap(Deadman.getConfig().GAME_LENGTH.text));
                p.getPacketSender().sendString(interId, 13, ORANGE.wrap("\t # XP Rate: ") + BLUE.wrap("" + Deadman.getConfig().XP_RATE));
                p.getPacketSender().sendString(interId, 14, ORANGE.wrap("\t # Drop Rate: ") + BLUE.wrap("" + Deadman.getConfig().DROP_RATE));
                p.getPacketSender().sendString(interId, 15, ORANGE.wrap("\t # Pet Rate: ") + BLUE.wrap("" + Deadman.getConfig().PET_RATE));
                p.getPacketSender().sendString(interId, 16, ORANGE.wrap("\t # Team Size: ") + BLUE.wrap("" + Deadman.getConfig().TEAM_SIZE_MAX.asInt));



                p.getPacketSender().sendString(interId, 18,     DARK_RED.wrap("- Mutator Info -"));
                p.getPacketSender().sendString(interId, 19, DARK_GREEN.wrap(  "----------------------------------------------"));
                p.getPacketSender().sendString(interId, 20, getCenteredMenuString(Deadman.getConfig().MUTATOR.name(), DARK_RED));
                for (String s : Deadman.getConfig().MUTATOR.description()) {
                    p.getPacketSender().sendString(interId, 21 + xOffset, getCenteredMenuString(s, ORANGE));
                    xOffset++;
                }
                p.getPacketSender().sendString(interId, 21 + xOffset, DARK_GREEN.wrap(  "----------------------------------------------"));

                p.getPacketSender().sendString(interId, 23 + xOffset, "Beginner Sigils are now available for purchase");
                p.getPacketSender().sendString(interId, 24 + xOffset, "in the Citadel, and in the Overworld!");
                break;

            case MAIN:
                xOffset = 0;
                p.getPacketSender().sendString(interId, 10,     DARK_RED.wrap("--- Tournament Info ---"));
                p.getPacketSender().sendString(interId, 11,     DARK_RED.wrap("- Current Tournament -"));
                p.getPacketSender().sendString(interId, 12, ORANGE.wrap("\t # Length: ") + BLUE.wrap(Deadman.getConfig().GAME_LENGTH.text));
                p.getPacketSender().sendString(interId, 13, ORANGE.wrap("\t # XP Rate: ") + BLUE.wrap("" + Deadman.getConfig().XP_RATE));
                p.getPacketSender().sendString(interId, 14, ORANGE.wrap("\t # Drop Rate: ") + BLUE.wrap("" + Deadman.getConfig().DROP_RATE));
                p.getPacketSender().sendString(interId, 15, ORANGE.wrap("\t # Pet Rate: ") + BLUE.wrap("" + Deadman.getConfig().PET_RATE));
                p.getPacketSender().sendString(interId, 16, ORANGE.wrap("\t # Team Size: ") + BLUE.wrap("" + Deadman.getConfig().TEAM_SIZE_MAX.asInt));

                p.getPacketSender().sendString(interId, 18,     DARK_RED.wrap("- Event Info -"));
                p.getPacketSender().sendString(interId, 19, ORANGE.wrap("\t # Last Event Type: ") + BLUE.wrap("/TODO"));
                p.getPacketSender().sendString(interId, 20, ORANGE.wrap("\t # Time Until Next Event: ") + BLUE.wrap(Deadman.timeUntilEvent()));
                p.getPacketSender().sendString(interId, 21, ORANGE.wrap("\t # Next Event Tier: ") + BLUE.wrap("/TODO"));

                p.getPacketSender().sendString(interId, 23,     DARK_RED.wrap("- Mutator Info -"));
                p.getPacketSender().sendString(interId, 24, DARK_GREEN.wrap(  "----------------------------------------------"));
                p.getPacketSender().sendString(interId, 25, getCenteredMenuString(Deadman.getConfig().MUTATOR.name(), DARK_RED));
                for (String s : Deadman.getConfig().MUTATOR.description()) {
                    p.getPacketSender().sendString(interId, 26+xOffset, getCenteredMenuString(s, ORANGE));
                    xOffset++;
                }
                p.getPacketSender().sendString(interId, 26+xOffset, DARK_GREEN.wrap(  "----------------------------------------------"));

                break;

            case FINAL:
                break;
        }

        p.startEvent(e -> {
            while(true) {
                p.getPacketSender().sendString(116, 7, ORANGE.wrap("\t Current Tournament Stage: ") + BLUE.wrap(Deadman.getStage().stageName().name()));
                p.getPacketSender().sendString(116, 8, ORANGE.wrap("\t Time Until Next Stage: ") + BLUE.wrap(Deadman.timeUntilChange()));
                if (Deadman.getStage().stageName() == Tournament.StageName.MAIN)
                    p.getPacketSender().sendString(116, 20, ORANGE.wrap("\t # Time Until Next Event: ") + BLUE.wrap(Deadman.timeUntilEvent()));
                e.delay(1);
            }
        });
    }

    public static String getCenteredMenuString(String text, Color middleColour) {
        int contentWidth = 36 - 2; // Subtracting 2 for the "|" characters
        int spacesNeeded = contentWidth - text.length();

        // Ensure we don't have negative spaces if the text is too long
        int leftPadding = Math.max(0, spacesNeeded / 2);
        int rightPadding = Math.max(0, spacesNeeded - leftPadding);

        String leftSpaces = " ".repeat(leftPadding);
        String rightSpaces = " ".repeat(rightPadding);

        return DARK_GREEN.wrap("|" + leftSpaces) +
                middleColour.wrap(text) +
                DARK_GREEN.wrap(rightSpaces + "|");
    }

}
