package io.dm.model.map.object.actions.impl.dungeons;

import io.dm.model.activities.motherlodemine.MotherlodeMine;
import io.dm.model.entity.shared.listeners.SpawnListener;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.map.object.actions.ObjectAction;
import io.dm.model.skills.slayer.Slayer;

public class KalphiteCave {

    static {
        ObjectAction.register(30180, 1, (player, obj) -> {
            MotherlodeMine.tunnel(player, 3305, 9497);
        });

        ObjectAction.register(26712, 1, (player, obj) -> player.getMovement().teleport(3321, 3122, 0));

        SpawnListener.register(new String[]{"kalphite guardian", "kalphite soldier", "kalphite worker"}, npc -> {
            if (npc.getPosition().getRegion().id == 13204 || npc.getPosition().getRegion().id == 13205) {
                npc.attackNpcListener = (player, npc1, message) -> {
                  if (!Slayer.isTask(player, npc)) {
                      if (message) {
                          player.dialogue(new NPCDialogue(491, "I didn't... create this cave for... you to do that... sorry, it's not for you."));
                          player.sendMessage("Buggie wants you to stick to your Slayer assignments.");
                      }
                      return false;
                  }
                  return true;
                };
            }
        });
    }

}
