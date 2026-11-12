package io.dm.deadman.content.sigils;

import io.dm.cache.Color;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;

public class SigilSelectionScreen {

    private static final int PARENT = 737;
    private static final int FIGHTER = 6;
    private static final int RANGER = 11;
    private static final int MAGE = 16;

    static {
        InterfaceHandler.register(PARENT, (handler) -> {
            handler.actions[FIGHTER] = (SimpleAction) (p) -> {
                p.sendMessage("You picked: Sigil of the Formidable Fighter");
                p.dialogue(
                        new OptionsDialogue("You picked: " + Color.RED.wrap("Sigil of the Formidable Fighter"),
                                new Option("Confirm", () -> {
                                    p.closeInterfaces();
                                }),
                                new Option("Back", () -> p.openInterface(InterfaceType.MAIN, PARENT)))
                );
            };

            handler.actions[RANGER] = (SimpleAction) (p) -> {
                p.sendMessage("You picked: Sigil of the Rigorous Ranger");
                p.dialogue(
                        new OptionsDialogue("You picked: " + Color.RED.wrap("Sigil of the Rigorous Ranger"),
                                new Option("Confirm", () -> {
                                    p.closeInterfaces();
                                }),
                                new Option("Back", () -> p.openInterface(InterfaceType.MAIN, PARENT)))
                );
            };

            handler.actions[MAGE] = (SimpleAction) (p) -> {
                p.sendMessage("You picked: Sigil of the Meticulous Mage");
                p.dialogue(
                        new OptionsDialogue("You picked: " + Color.RED.wrap("Sigil of the Meticulous Mage"),
                                new Option("Confirm", () -> {
                                    p.closeInterfaces();
                                }),
                                new Option("Back", () -> p.openInterface(InterfaceType.MAIN, PARENT)))
                );
            };
        });
    }

}
