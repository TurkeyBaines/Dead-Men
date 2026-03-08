package io.dm.network.incoming.handlers.commands;

import io.dm.model.entity.player.Player;

public class GoToCommands {

    public void process(Player p, String... args) {
        switch (args[0].toLowerCase()) {
            case "overowrld": case "ow":
                overworld(p, args);
                break;
        }
    }

    public void overworld(Player p, String... args) {
        switch (args[1]) {
            case "portal":
                p.getMovement().teleport(2539, 3872);
                p.sendMessage("You have moved to CITADEL > PORTAL");
                break;

            case "desk":
                p.getMovement().teleport(2686, 3895);
                p.sendMessage("You have moved to CITADEL > EAST CASTLE");
                break;

            case "fish":
                p.getMovement().teleport(2574, 3853);
                p.sendMessage("You have moved to CITADEL > FISHING/COOKING");
                break;

            case "wc":
                p.getMovement().teleport(2550, 3868);
                p.sendMessage("You have moved to CITADEL > WOODCUTTING");
                break;

            case "ess":
                p.getMovement().teleport(2548, 3853);
                p.sendMessage("You have moved to CITADEL > ESSENCE");
                break;

            case "rc":
                p.getMovement().teleport(2519, 3879);
                p.sendMessage("You have moved to CITADEL > ZMI ALTAR");
                break;

            case "herb":
                p.getMovement().teleport(2529, 3853);
                p.sendMessage("You have moved to CITADEL > HERB PATCHES");
                break;

            case "mine":
                p.getMovement().teleport(2537, 3889);
                p.sendMessage("You have moved to CITADEL > MINE/SMITH");
                break;

            case "flax":
                p.getMovement().teleport(2585, 3862);
                p.sendMessage("You have moved to CITADEL > FLAX FIELDS");
                break;

            case "task":
                p.getMovement().teleport(2613, 3855);
                p.sendMessage("You have moved to CITADEL > TASK NOTICE BOARD");
                break;

            case "east":
                p.getMovement().teleport(2598, 3874);
                p.sendMessage("You have moved to CITADEL > EAST CASTLE");
                break;

            case "west":
                p.getMovement().teleport(2524, 3860);
                p.sendMessage("You have moved to CITADEL > WEST CASTLE");
                break;

            case "cave":
                p.getMovement().teleport(2770, 10070);
                p.sendMessage("You have moved to CITADEL > CAVE");
                break;
        }
    }



}
