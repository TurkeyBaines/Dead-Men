package io.dm.model.inter.dialogue;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SlotAction;
import io.dm.model.inter.utils.Config;
import io.dm.model.inter.utils.Option;
import io.dm.model.inter.utils.Unlock;

import java.util.List;

public class OptionsDialogue implements Dialogue {

    private final Object[] params;

    private final Option[] options;

    private boolean resend;

    public OptionsDialogue(List<Option> options) {
        this(options.toArray(new Option[options.size()]));
    }

    public OptionsDialogue(Option... options) {
        this("Select an Option", options);
    }

    public OptionsDialogue(String title, List<Option> options) {
        this(title, options.toArray(new Option[options.size()]));
    }

    public OptionsDialogue(String title, Option... options) {
        params = new Object[]{title, ""};
        for(int i = 0; i < options.length; i++) {
            params[1] += options[i].name;
            if(i != options.length - 1)
                params[1] += "|";
        }
        this.options = options;
    }

    @Override
    public void open(Player player) {
        player.optionsDialogue = this;
        // Rev-228 recipe per AlterRSPS's options() (QueueTaskExt.kt), verified against the deobbed
        // cs2: script 2379 (toplevel_chatbox_resetbackground) zeroes the varc that suppresses the
        // chatbox parchment, and varbit 10670 makes the dialogue child full-chatbox-size — script 58
        // (chatbox_multi_init) only draws the title/options/sword decor, never the background, so
        // without these two the options float on a bare chat area.
        player.getPacketSender().sendClientScript(2379, "");
        player.openInterface(InterfaceType.CHATBOX, Interface.OPTIONS_DIALOGUE);
        // Set AFTER openInterface: opening fires the REPLACED chatbox handler's closedAction, and
        // several of those (219 itself on a chained/resent options, 270, 284) reset this varbit to 0.
        Config.CHATBOX_DIALOGUE_FULL_SIZE.set(player, 1);
        player.getPacketSender().setAlignment(Interface.OPTIONS_DIALOGUE, 1, 0, -10);
        player.getPacketSender().sendClientScript(58, "ss", params);
        new Unlock(Interface.OPTIONS_DIALOGUE, 1).children(1, 5).unlockSingle(player);
        // TEMP diagnostic (Stage 11 Round 2): live test reported options "not firing at all" with no
        // 219 lines in PROTOCOL_DUMP.txt — this server-side line settles whether open() even ran.
        // Remove once an options dialogue is confirmed working live.
        io.dm.network.logging.NetworkLogger.log(player, "SERVER-OUT OptionsDialogue.open",
                Interface.OPTIONS_DIALOGUE, 1, -1, -1, -1, "title=" + params[0] + " options=" + params[1]);
    }

    public void resend() {
        resend = true;
    }

    private void handle(Player player, int slot) {
        int index = slot - 1;
        if(index < 0 || index >= options.length) {
            player.closeDialogue();
            return;
        }
        options[index].select(player);
        if(resend) {
            resend = false;
            open(player);
        } else if(player.lastDialogue == this) {
            /* dialogue was not continued */
            player.closeDialogue();
        }
    }

    static {
        InterfaceHandler.register(Interface.OPTIONS_DIALOGUE, h -> {
            h.actions[1] = (SlotAction) (p, slot) -> {
                if (p.optionsDialogue != null) {
                    p.optionsDialogue.handle(p, slot);
                }
            };
            // Mirror of SkillDialogue's closedAction for its own full-frame varbit: put the dialogue
            // child back to the small pane once the options close, or the next plain chat dialogue
            // opens full-size.
            h.closedAction = (p, i) -> Config.CHATBOX_DIALOGUE_FULL_SIZE.set(p, 0);
        });
    }

}