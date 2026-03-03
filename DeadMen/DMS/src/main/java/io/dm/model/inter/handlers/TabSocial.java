package io.dm.model.inter.handlers;

import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.inter.utils.Config;

public class TabSocial {

    static {
        InterfaceHandler.register(Interface.FRIENDS_LIST, h -> h.actions[1]  = (SimpleAction) p -> {
            p.openInterface(InterfaceType.SOCIAL_TAB, Interface.IGNORE_LIST);
            Config.FRIENDS_AND_IGNORE_TOGGLE.set(p, 1);
        });

        InterfaceHandler.register(Interface.IGNORE_LIST, h -> h.actions[1]  = (SimpleAction) p -> {
            p.openInterface(InterfaceType.SOCIAL_TAB, Interface.FRIENDS_LIST);
            Config.FRIENDS_AND_IGNORE_TOGGLE.set(p, 0);
        });
    }

}