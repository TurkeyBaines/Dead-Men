package io.dm.model.map.object.actions;

import io.dm.cache.ObjectDef;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Tile;
import io.dm.model.map.object.GameObject;

import java.util.Arrays;
import java.util.function.Consumer;

public interface ObjectAction {

    void handle(Player player, GameObject obj);

    /**
     * Default - (Registers the given action for every object in the game with the given objectId)
     */

    static void register(int objectId, int option, ObjectAction action) {
        ObjectDef def = ObjectDef.get(objectId);
        if(def.defaultActions == null)
            def.defaultActions = new ObjectAction[5];
        def.defaultActions[option - 1] = action;
    }

    static boolean register(int objectId, String optionName, ObjectAction action) {
        int option = ObjectDef.get(objectId).getOption(optionName);
        if(option == -1)
            return false;
        register(objectId, option, action);
        return true;
    }

    static void register(int objectId, Consumer<ObjectAction[]> actionsConsumer) {
        ObjectAction[] actions = new ObjectAction[6];
        actionsConsumer.accept(actions);
        ObjectDef.get(objectId).defaultActions = Arrays.copyOfRange(actions, 1, actions.length);
    }

    /**
     * Global - (Registers the given action for several objects in the game with the given name)
     */

    static void register(String objectName, int option, ObjectAction action) {
        for (ObjectDef def : ObjectDef.getByName(objectName))
            register(def.id, option, action);
    }

    static void register(String objectName, String optionName, ObjectAction action) {
        for (ObjectDef def : ObjectDef.getByName(objectName))
            register(def.id, optionName, action);
    }

    static void register(String objectName, Consumer<ObjectAction[]> actionsConsumer) {
        for (ObjectDef def : ObjectDef.getByName(objectName))
            register(def.id, actionsConsumer);
    }

    /**
     * Specific - (Registers the given action for an object in the game with the given objectId, x, y, and z)
     */

    static void register(int objectId, int x, int y, int z, int option, ObjectAction action) {
        register(Tile.getObject(objectId, x, y, z), option, action);
    }

    static boolean register(int objectId, int x, int y, int z, String optionName, ObjectAction action) {
        return register(Tile.getObject(objectId, x, y, z), optionName, action);
    }

    static void register(int objectId, int x, int y, int z, Consumer<ObjectAction[]> actionsConsumer) {
        register(Tile.getObject(objectId, x, y, z), actionsConsumer);
    }

    /**
     * Specific - (Registers the given action for the given object)
     */

    static void register(GameObject obj, int option, ObjectAction action) {
        // FIX (2026-07-14, Rev Caves crash investigation): a hardcoded (objectId, x, y, z) that
        // doesn't resolve to a real placed object in this stock cache (stale/wrong coordinate,
        // content ported from a different reference) used to NPE here and take the ENTIRE calling
        // class's static initializer down with it - losing every OTHER registration in that class
        // too, not just the bad one. Same class of bug already fixed elsewhere in this codebase for
        // NPC.setCombat()/Tile.occupy() (see THE_PLAN.txt's 2026-07-12 update) - degrade gracefully,
        // don't crash the loader. Concretely: RevCaves.java's south-exit door (objectId 31557 @
        // 3194,10055,0) doesn't exist in Cache228, which previously crashed RevCaves's <clinit>
        // before its 5 jump-pillar registrations (all independently confirmed correct against real
        // cache data) ever ran.
        if(obj == null) {
            System.err.println("[ObjectAction] register(option) called with no object (stale coordinate?) - skipped");
            return;
        }
        if(obj.actions == null)
            obj.actions = new ObjectAction[5];
        obj.actions[option - 1] = action;
    }

    static boolean register(GameObject obj, String optionName, ObjectAction action) {
        if(obj == null) {
            System.err.println("[ObjectAction] register(\"" + optionName + "\") called with no object (stale coordinate?) - skipped");
            return false;
        }
        int option = obj.getDef().getOption(optionName);
        if(option == -1)
            return false;
        register(obj, option, action);
        return true;
    }

    static void register(GameObject obj, Consumer<ObjectAction[]> actionsConsumer) {
        if(obj == null) {
            System.err.println("[ObjectAction] register(actionsConsumer) called with no object (stale coordinate?) - skipped");
            return;
        }
        ObjectAction[] actions = new ObjectAction[5 + 1];
        actionsConsumer.accept(actions);
        obj.actions = Arrays.copyOfRange(actions, 1, actions.length);
    }

}