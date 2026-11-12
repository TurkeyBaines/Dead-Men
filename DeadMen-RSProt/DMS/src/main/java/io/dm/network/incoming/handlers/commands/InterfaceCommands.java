package io.dm.network.incoming.handlers.commands;

import io.dm.cache.InterfaceDef;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.Widget;
import io.dm.model.inter.utils.Config;
import io.dm.utility.CS2Script;

public class InterfaceCommands {

    public void process(Player p, String... args) {
        switch (args[0]) {
            case "show":
                show(p, args);
                break;

            case "debug":
                debug(p);
                break;

            case "print":
                print(p, args);
                break;

            case "sendstring": case "sendstr":
                sendString(p, args);
                break;

            case "sprite":
                sprite(p, args);
                break;
        }
    }

    private static final int QUEST_LIST_INTERFACE = 629;

    /** ::inter sprite <childId> <spriteId> - sets the sprite of a graphic child of the quest list (629). */
    private void sprite(Player p, String... args) {
        if (args.length != 3) {
            p.sendMessage("Usage: ::inter sprite <childId> <spriteId>");
            return;
        }
        try {
            int child = Integer.parseInt(args[1]);
            int sprite = Integer.parseInt(args[2]);
            p.getPacketSender().sendSprite(QUEST_LIST_INTERFACE, child, sprite);
            p.sendMessage("Set sprite " + sprite + " on " + QUEST_LIST_INTERFACE + ":" + child);
        } catch (NumberFormatException e) {
            p.sendMessage("Usage: ::inter sprite <childId> <spriteId>");
        }
    }

    private void show(Player p, String... args) {
        if (args.length == 2)
            p.openInterface(InterfaceType.MAIN, Integer.parseInt(args[1]));
        else if (args.length == 3)
            if (args[1].equalsIgnoreCase("cont"))
                p.startEvent(e -> {
                    int starter = Integer.parseInt(args[2]);
                    int max = 100;
                    int counter = 0;
                    while ((starter + max) > (starter + counter)) {
                        p.sendMessage("Interface ID: " + (starter + counter));
                        p.openInterface(InterfaceType.MAIN, starter+counter);
                        counter++;

                        e.delay(2);
                        p.closeInterfaces();
                        e.delay(1);
                    }
                });

    }

    private void debug(Player p) {
        p.openInterface(InterfaceType.WILDERNESS_OVERLAY, 90);
        p.setVisibleInterface(90, 90, 63);
    }

    private void print(Player p, String... args) {
        switch (args[1]) {
            case "config":
                int interfaceId = Integer.valueOf(args[2]);
                boolean recursiveSearch = args.length >= 4 && Integer.valueOf(args[3]) == 1;
                InterfaceDef.printConfigs(interfaceId, recursiveSearch);
                break;
        }
    }

    private void sendString(Player p, String... args) {
        int inter = Integer.parseInt(args[1]);
        int comp = Integer.parseInt(args[2]);
        String text;
        if (args.length == 3)
            text = "";
        else {
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                sb.append(args[i] + " ");
            }
            text = sb.toString();
        }

        System.out.println("interface: " + inter);
        System.out.println("component: " + comp);
        System.out.println("text: " + text);
        p.getPacketSender().sendString(inter, comp, text);
    }

}
