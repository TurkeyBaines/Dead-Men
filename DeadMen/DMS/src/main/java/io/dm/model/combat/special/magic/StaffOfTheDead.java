package io.dm.model.combat.special.magic;

import io.dm.cache.Color;
import io.dm.cache.ItemDef;
import io.dm.model.activities.duelarena.DuelRule;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.item.actions.ItemItemAction;
import io.dm.model.item.attributes.AttributeExtensions;
import io.dm.model.item.attributes.AttributeTypes;
import io.dm.model.item.attributes.AugmentType;
import io.dm.model.item.containers.Equipment;

//Power of Death: Reduce all melee damage you receive by 50% for the next
//minute while the staff remains equipped. Stacks with Protect from Melee. (100%)
public class StaffOfTheDead implements Special {

    @Override
    public boolean accept(ItemDef def, String name) {
        return def.id == 11791 || def.id == 12904 || def.id == 12902 || def.id == 22296;
    }

    @Override
    public boolean handleActivation(Player player) {
        if(DuelRule.NO_MAGIC.isToggled(player)) {
            player.sendMessage("Magic attacks have been disabled for this duel!");
            return true;
        }
        if(player.sotdDelay.isDelayed()) {
            player.sendMessage("<col=3d5d2b>You're already protected by the spirits of deceased evildoers.");
            return false;
        }
        if(!player.getCombat().useSpecialEnergy(100))
            return false;
        player.sotdDelay.delay(100);
        int staff = player.getEquipment().getId(Equipment.SLOT_WEAPON);
        player.animate(staff == 11791 ? 7083 : (staff == 12902 ? 1719 : 1720), 0);
        player.graphics(1228, 300, 0);
        player.sendMessage("<col=3d5d2b>Spirits of deceased evildoers offer you their protection.");
        return true;
    }

    static {
        ItemItemAction.register(30129, 11921, (player, primary, secondary) -> {
            if (!AttributeExtensions.hasAttribute(secondary, AttributeTypes.AUGMENTED)) {
                player.dialogue(new YesNoDialogue(Color.RED.wrap("WARNING!"), "This will consume the augment and make your staff untradeable.", primary, () -> {
                    player.getInventory().remove(primary.getId(), 1);
                    secondary.putAttribute(AttributeTypes.AUGMENTED, AugmentType.CORRUPT);
                }));
            } else {
                player.sendMessage("Your staff is already augmented.");
            }
        });
        ItemItemAction.register(30129, 12904, (player, primary, secondary) -> {
            if (!AttributeExtensions.hasAttribute(secondary, AttributeTypes.AUGMENTED)) {
                player.dialogue(new YesNoDialogue(Color.RED.wrap("WARNING!"), "This will consume the augment and make your staff untradeable.", primary, () -> {
                    player.getInventory().remove(primary.getId(), 1);
                    secondary.putAttribute(AttributeTypes.AUGMENTED, AugmentType.CORRUPT);
                }));
            } else {
                player.sendMessage("Your staff is already augmented.");
            }
        });
    }

}