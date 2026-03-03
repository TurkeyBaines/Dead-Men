package io.dm.model.stat;

import io.dm.model.entity.player.Player;
import lombok.Data;


@Data
public class StatRequirement {
    public final StatType statType;
    public final int requiredLevel;
    private boolean canBoost;

    public boolean hasRequirement(Player player){
       Stat currentLevel = player.getStats().get(statType);

       if(canBoost)
           return currentLevel.currentLevel >= requiredLevel;
       else
           return currentLevel.fixedLevel >= requiredLevel;
    }
}
