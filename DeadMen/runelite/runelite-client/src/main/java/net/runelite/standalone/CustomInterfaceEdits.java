package net.runelite.standalone;

public class CustomInterfaceEdits {

    public static void childLoaded(Widget inter) {
        int parentId = inter.id >> 16;
        int childId = inter.id & 0xffff;
        inter.text = inter.text.replace("RuneScape", CustomMain.worldType.getName()).replace("Dead Men", CustomMain.worldType.getName());
        inter.text = inter.text.replace("Kronos", CustomMain.worldType.getName()).replace("Dead Men", CustomMain.worldType.getName());


        if(parentId == 24) { //barrows overlay
            if(childId == 9 || childId == 10) {
                inter.rawX = inter.rawX = 0;
                inter.width = inter.height = 0;
                inter.isHidden = true;
            }
            return;
        }
        if(parentId == 275) { //quest scroll
            if(childId >= 4 && childId <= 133)
                inter.text = "";
            return;
        }
        /*if(parentId == 320) { //skills
            if(childId == 27) {
                inter.setAction(0, "Shout total level");
                inter.setAction(1, "Shout total XP");
                unlock(inter.id, -1, -1, 0, 1);
            }
            return;
        }*/
        if(parentId == 596) { //target overlay
            if(childId == 2)
                inter.width += 70;
            return;
        }
        if(parentId == 541) { //prayer interface
            if(childId == 30 || childId == 31) {
                inter.setAction(1, "Refund Scroll");
                unlock(inter.id, -1, -1, 0, 1, 1);
            }
        }

        /*
        *
        * Interface IDs: [720/8]
            Text: [component: 8]
            Button Text: [Ok]
            Data Text: []
            Spell Text: []
            Text2: []
            Button Type: [0]
            Click Mask: [0]
            Has Listener: [true]
	*
        Interface IDs: [720/9]
            Text: [component: 9]
            Action(0): [<col=ff981f>View</col>]
            Button Text: [Ok]
            Data Text: [Online staff]
            Spell Text: []
            Text2: []
            Button Type: [0]
            Click Mask: [2]
            Has Listener: [true]

         */

        if (parentId == 720) {
            switch (childId) {
                case 8:
                    inter.actions = new String[] {
                            "<col=ff981f>View</col>"
                    };
                    inter.buttonText = "Ok";
                    inter.dataText = "Online players";
                    inter.clickMask = 2;
                    break;

                case 9:
                    inter.buttonText = "Ok";
                    inter.dataText = "Online staff";
                    inter.clickMask = 2;
                    break;

                case 10:
                    inter.actions = new String[]{ "View" };
                    inter.buttonText = "Staff Online";
                    unlock(701, 10, 10);
                    break;


                case 11:
                case 12:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 49:

                    if (inter.getActions() == null) {
                        System.out.println("FUCK OFF NEW!");
                        inter.actions = new String[] { "Fuck off new" };
                    } else {
                        optionSet(inter, 0, "Fuck off old");
                    }
                    System.out.println("We changed the default Actions for [" + parentId + "/" + childId + "]");
            }

        }
    }


    public static void optionSet(Widget inter, int index, String option) {
        int parentId = inter.id >> 16;
        int childId = inter.id & 0xffff;



/*        if(parentId == 320) {
            if(childId >= 1 && childId <= 23) {
                if(childId >= 1 && childId <= 6 || childId == 9) {
                    if (CustomMain.worldType.isPVP())
                        inter.setAction(0, option.replace("View", "Set").replace("guide", "level"));
                    else
                        inter.setAction(0, option.replace("Set", "View").replace("level", "guide"));
                } else {
                    inter.setAction(0, option.replace("Set", "View").replace("level", "guide"));
                }
                inter.setAction(1, option.replace("View", "Shout").replace("guide", "level"));
                inter.setAction(2, option.replace("View", "Shout").replace("guide", "XP"));
                unlock(inter.id, -1, -1, 0, 1, 2);
            }
        }*/
    }

    public static void unlock(int id, int minChildId, int maxChildId, int... optionIndexes) {
        int mask = 0;
        for(int i : optionIndexes)
            mask |= 2 << i;
        for(int slot = minChildId; slot <= maxChildId; slot++) {
            long l = (((long) id << 32) + (long) slot);
            final Node node_0 = (Node) Client.widgetClickMasks.get(l);
            if (node_0 != null)
                node_0.unlink();
            Client.widgetClickMasks.put(new IntegerNode(mask), l);
        }
    }

    public static boolean allowUnload(int interfaceId) {
        return interfaceId != 94;
    }

}