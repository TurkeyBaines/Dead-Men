package io.dm.deadman.keys;

import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.ItemContainer;
import io.dm.services.Loggers;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BankKey {

    Player owner;
    ItemContainer items;
    @Getter
    int value;

    public BankKey(Player owner, Player victim) {
        this.owner = owner;
        items = new ItemContainer();

        Item[] victimBankItems = getItems(victim);
        TempItem[] itemsSortedByValue = sortItemsByValue(victimBankItems);

        int totalValue = 0;
        for (TempItem i : itemsSortedByValue) {
            totalValue += i.totalPrice;
            items.add(i.item);
        }
        value = totalValue;
        //Loggers.logBankKeyCreation(owner.getName(), victim.getName(), totalValue);
    }

    public Item[] getItems(Player victim) {
        if (victim == null || !victim.isOnline()) return null;

        return victim.getBank().getItems();
    }

    public TempItem[] sortItemsByValue(Item[] rawList) {
        List<Item> itemList = Arrays.asList(rawList);
        ArrayList<TempItem> sortedList = new ArrayList<>();

        for (Item item : itemList) {
            sortedList.add(new TempItem(item));
        }

        itemList.clear();
        sortedList.sort((a, b) -> Integer.compare(
                a.totalPrice,
                b.totalPrice
        ));

        for (int i = 0; i < 10; i++) {
            itemList.add(sortedList.get(i).item);
        }

        return sortedList.toArray(new TempItem[10]);
    }

    private class TempItem {
        Item item;
        int totalPrice;
        public TempItem(Item item) {
            this.item = item;
            //this.totalPrice = (item.getAmount() * Integer.parseInt(item.getDef().gePrice));
            this.totalPrice = 0;
        }
    }
}
