package io.dm.network.incoming.handlers.commands;

import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.item.containers.bank.Bank;
import io.dm.model.item.containers.bank.BankItem;

public class BankCommands {

    public void process(Player player, String... args) {
        if (args == null)
            open(player);

        switch (args[0]) {

            case "check":
                check(player, args);
                break;

            case "open":
                open(player);
                break;

            case "copy":
                copy(player, args);
                break;

        }
    }


    private void check(Player player, String... args) {
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) return;
    }

    private void copy(Player player, String... args) {
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) return;
        Bank p2Bank = p2.getBank();
        if (p2Bank == null) return;
        player.getBank().clear();

        for (BankItem item : p2Bank.getItems()) {
            player.getBank().add(item);
        }

        player.sendMessage("Cleared your Bank & Copied bank from " + p2.getName());
    }

    private void open(Player player) {
        player.getBank().open();
    }
}
