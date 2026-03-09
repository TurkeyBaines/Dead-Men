package io.dm.network.incoming.handlers;

import io.dm.api.buffer.InBuffer;
import io.dm.api.utils.*;
import io.dm.cache.*;
import io.dm.data.DataFile;
import io.dm.data.impl.Help;
import io.dm.data.impl.npcs.npc_combat;
import io.dm.data.impl.npcs.npc_drops;
import io.dm.data.impl.teleports;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.*;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Config;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.containers.Equipment;
import io.dm.model.item.containers.bank.BankItem;
import io.dm.model.map.*;
import io.dm.model.skills.magic.rune.Rune;
import io.dm.model.stat.Stat;
import io.dm.model.stat.StatType;
import io.dm.network.incoming.Incoming;
import io.dm.network.incoming.handlers.commands.*;
import io.dm.services.Loggers;
import io.dm.services.Punishment;
import io.dm.utility.IdHolder;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@IdHolder(ids = {17})
public class CommandHandler implements Incoming {

    private static final Bounds MAGE_BANK = new Bounds(2527, 4708, 2551, 4727, 0);

    private static Position relativeBase;

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        String query = in.readString();
        handle(player, query);
    }

    public static void handle(Player player, String query) {
        if((query = query.trim()).isEmpty())
            return;
        Loggers.logCommand(player.getUserId(), player.getName(), player.getIp(), query);
        if (player.isStaff()) {
            String format = String.format("Command:[Player:[%s] Position:%s IPAddress:[%s] Query:[%s]]", player.getName(), player.getPosition(), player.getIp(), query);
            ServerWrapper.log(format);
        }
        String command;
        String[] args;
        try {

            System.out.println(query);
            int spaceIndex = query.indexOf(' ');
            if(spaceIndex == -1) {
                command = query;
                args = null;
            } else {
                command = query.substring(0, spaceIndex);
                args = query.substring(spaceIndex + 1).split(" ");
            }
            if(player.isLocked() && !player.isAdmin()) {
                player.sendMessage("Please finish what you're doing first.");
                return;
            }

            handleAdmin(player, query, command, args);
        } catch(Throwable t) {
            t.printStackTrace();
            if (player.isAdmin())
                player.sendMessage("Error handling command '" + query + "': " + t.getMessage());
        }
    }
    private static TestCommands testCommands;

    private static boolean handleAdmin(Player player, String query, String command, String[] args) {
        switch(command) {

            case "item": case "inventory":
                new ItemCommands().process(player, args);
                return true;

            case "sigil":
                new SigilCommands().process(player, args);
                return true;

            case "object": case "obj":
                new ObjectCommands().process(player, args);
                return true;

            case "position": case "pos": case "map":
            case "loc": case "location":
                new MapCommands().process(player, args);
                return true;

            case "npc":
                new NpcCommands().process(player, args);
                return true;

            case "punish":
                new PunishCommands().process(player, args);
                return true;

            case "stat":
                new StatCommands().process(player, args);
                return true;

            case "bank":
                new BankCommands().process(player, args);
                return true;

            case "tourn": case "tourny": case "tournament":
                new TournamentCommands().process(player, args);
                return true;

            case "test": case "tst":
                if (testCommands == null) testCommands = new TestCommands();
                testCommands.process(player, args);
                return true;

            case "player": case "p":
                new PlayerCommands().process(player, args);
                return true;

            case "goto": case "gt":
                new GoToCommands().process(player, args);
                return true;




            case "test_command":
                player.sendMessage("QUERY: " + query);
                player.sendMessage("COMMAND: " + command);
                if (args != null)
                    for (String s : args) {
                        player.sendMessage("ARGS: " + s);
                    }
                return true;

            case "reloadmutes": {
                IPMute.refreshMutes();
                return true;
            }

            case "altcheck": {
                forPlayer(player, query, "::altcheck target", p2 -> {
                    List<Player> players = World.getPlayerStream().filter(plyr ->
                            plyr.getUUID().equalsIgnoreCase(p2.getUUID())
                    ).collect(Collectors.toList());
                    player.sendMessage("Found " + players.size() + " alt accounts...");
                    players.forEach(plyr -> player.sendMessage(plyr.getName()));
                });
                return true;
            }

            case "checkequipment": {
                forPlayer(player, query, "::checkequipment name", (target) -> {
                    player.dialogue(new OptionsDialogue("Viewing a players equipment will clear yours.",
                            new Option("View " + target.getName() + " equipment.", () -> {
                                Equipment equipment = target.getEquipment();
                                player.getEquipment().clear();
                                for (Item item : equipment.getItems()) {
                                    if (item == null)
                                        continue;
                                    player.getEquipment().equip(item.copy());
                                }
                            }),
                            new Option("No, thanks."))
                    );
                });
                return true;
            }


            case "testinter":
                player.openInterface(InterfaceType.MAIN, 718);
                player.getPacketSender().sendModel(718, 87, 38615);
                player.getPacketSender().sendModelInformation(718, 87, 1000, 0, 0);
                return true;


            case "itemdef": {
                int itemId = Integer.parseInt(args[0]);
                ItemDef def = ItemDef.get(itemId);
                if (def == null) {
                    player.sendMessage("Invalid item definition for fileId "+ itemId +".");
                } else {
                    player.sendMessage(String.format("[ItemDef] id=%d, tradeable=%b", itemId, def.tradeable));
                }
                return true;
            }

            case "controlnpc": {
                if (args == null || args.length == 0) {
                    player.remove("CONTROLLING_NPC");
                    player.sendMessage("NPC control cleared.");
                    return true;
                } else {
                    int indexa = Integer.parseInt(args[0]);
                    NPC npc = World.getNpc(indexa);
                    if (npc == null) {
                        player.sendMessage("Invalid NPC. Use index");
                        return true;
                    } else {
                        player.set("CONTROLLING_NPC", npc);
                        player.sendMessage("You're now controlling " + npc.getDef().name + ".");
                    }
                    return true;
                }
            }

            case "ipban": {
                forPlayer(player, query, "::ipban playerName", p2 -> Punishment.ipBan(player, p2));
                return true;
            }

            case "ipmute": {
                forPlayer(player, query, "::ipmute playerName", p2 -> Punishment.ipMute(player, p2));
                return true;
            }

            case "macban": {
                forPlayer(player, query, "::macban playerName", p2 -> Punishment.macBan(player, p2));
                return true;
            }

            case "shout":
            case "broadcast":
            case "bc": {
                String message = String.join(" ", args);
                World.players.forEach(p -> p.getPacketSender().sendMessage(message, "", 14));
                return true;
            }

            case "hit":
            case "hitme": {
                player.hit(new Hit().fixedDamage(Integer.parseInt(args[0])).delay(0));
                return true;
            }

            case "tutorial": {
                player.newPlayer = true;
                return true;
            }

            case "debug": {
                player.sendMessage("Debug: " + ((player.debug = !player.debug) ? "ON" : "OFF"));
                return true;
            }

            case "update": {
                World.update(Integer.valueOf(args[0]));
                return true;
            }

            case "animateobj": {
                Tile.getObject(-1, player.getAbsX(), player.getAbsY(), player.getHeight(), 10, -1).animate(Integer.parseInt(args[0]));
                return true;
            }

            case "kill": {
                player.hit(new Hit().fixedDamage(255));
                return true;
            }

            case "killnpcs": {
                for (NPC npc : player.localNpcs()) {
                    if (npc.getCombat() == null)
                        continue;
                    if (player.getCombat().canAttack(npc, true)) {
                        npc.startEvent(e -> {
                            npc.hit(new Hit().fixedDamage(50));
                            e.delay(1);
                        });
                    }
                }
                return true;
            }


            case "rune": {
                Rune r = Rune.valueOf(args[0].toUpperCase());
                player.getInventory().add(r.getId(), Integer.MAX_VALUE);
                return true;
            }

            case "removeplayers": {
                World.players.forEach(Player::forceLogout);
                return true;
            }

            case "save": {
                player.sendMessage("Saving online players...");
                for(Player p : World.players)
                    PlayerFile.save(p, -1);
                player.sendMessage("DONE!");
                return true;
            }

            /**
             * Interface commands
             */
            case "interface":
            case "inter": {
                int interfaceId = Integer.valueOf(args[0]);
                InterfaceType type = InterfaceType.MAIN;
                if(args.length == 2)
                    type = InterfaceType.valueOf(args[1].toUpperCase());
                player.temp.put("last_inter_cmd", interfaceId);
                player.openInterface(type, interfaceId);
                return true;
            }

            case "inters": {
                InterfaceType type = InterfaceType.MAIN;
                if(args != null && args.length == 1)
                    type = InterfaceType.valueOf(args[0].toUpperCase());
                int interfaceId = (int) player.temp.getOrDefault("last_inter_cmd", 0);
                if(interfaceId == 548 || interfaceId == 161 || interfaceId == 164) //main screen
                    interfaceId++;
                if(interfaceId == Interface.CHAT_BAR) //chat box
                    interfaceId++;
                if(interfaceId == 156) //annoying
                    interfaceId++;
                player.temp.put("last_inter_cmd", interfaceId + 1);
                player.openInterface(type, interfaceId);
                player.sendFilteredMessage("Interface: " + interfaceId);
                return true;
            }

            case "ic":
            case "iconf": {
                int interfaceId = Integer.valueOf(args[0]);
                boolean recursiveSearch = args.length >= 2 && Integer.valueOf(args[1]) == 1;
                InterfaceDef.printConfigs(interfaceId, recursiveSearch);
                return true;
            }

            case "findinterscript": {
                int scriptId = Integer.valueOf(args[0]);
                boolean recursiveSearch = args.length >= 2 && Integer.valueOf(args[1]) == 1;
                for (int interId = 0; interId < InterfaceDef.COUNTS.length; interId++) {
                    Set<ScriptDef> s = InterfaceDef.getScripts(interId, recursiveSearch);
                    if (s != null && s.stream().anyMatch(def -> def.id == scriptId)) {
                        player.sendMessage("Inter " + interId + " uses script " + scriptId +"!");
                    }
                }
                return true;
            }

            case "bits": {
                int id = Integer.valueOf(args[0]);
                Varp varp = Varp.get(id);
                if(varp == null) {
                    player.sendFilteredMessage("Varp " + id + " has no bits!");
                    return true;
                }
                System.out.println("Varp: " + id);
                for(Varpbit bit : varp.bits)
                    System.out.println("    bit: " + bit.id + "  shift: " + bit.leastSigBit);
                return true;
            }

            case "v":
            case "varp": {
                int id = Integer.valueOf(args[0]);
                int value = Integer.valueOf(args[1]);
                if(id < 0 || id >= 2000) {
                    player.sendFilteredMessage("Varp " + id + " does not exist.");
                    return true;
                }
                Config.create(id, null, false, false).set(player, value);
                player.sendFilteredMessage("Updated varp " + id + "!");
                return true;
            }

            case "vb":
            case "varpbit": {
                int id = Integer.valueOf(args[0]);
                int value = Integer.valueOf(args[1]);
                Varpbit bit = Varpbit.get(id);
                if(bit == null) {
                    player.sendFilteredMessage("Varpbit " + id + " does not exist.");
                    return true;
                }
                Config.create(id, bit, false, false).set(player, value);
                player.sendFilteredMessage("Updated varp " + bit.varpId + " with varpbit " + id + "!");
                return true;
            }

            case "varbitdef": {
                int varpbit = Integer.parseInt(args[0]);
                Varpbit def = Varpbit.get(varpbit);
                if (def != null) {
                    player.sendMessage("[Varpbit Def] varp="+ def.varpId +", start="+ def.leastSigBit +", end="+ def.mostSigBit +", maxValue="+ Math.pow(2, (def.mostSigBit - def.leastSigBit)));
                } else {
                    player.sendMessage("No definition entry found for varpbit "+ varpbit +".");
                }
                return false;
            }

            case "vbs":
            case "varpbits": {
                int minId = Integer.valueOf(args[0]);
                int maxId = Integer.valueOf(args[1]);
                int value = Integer.valueOf(args[2]);
                if(minId < 0 || minId >= Varpbit.LOADED.length || maxId < 0 || maxId >= Varpbit.LOADED.length) {
                    player.sendFilteredMessage("Invalid values! Please use values between 0 and " + (Varpbit.LOADED.length - 1) + "!");
                    return true;
                }
                for(int i = minId; i <= maxId; i++) {
                    Varpbit bit = Varpbit.get(i);
                    if(bit == null)
                        continue;
                    Config.create(i, bit, false, false).set(player, value);
                }
                return true;
            }

            case "dumpscripts": {
                for(int i = 0; i < 65535; i++) {
                    ScriptDef def = ScriptDef.get(i);
                    if(def == null)
                        continue;
                    try(PrintStream ps = new PrintStream(System.getProperty("user.home") + "/Desktop/script_instructions/" + i + ".txt")) {
                        def.print(ps);
                        ps.flush();
                    } catch(Exception e) {
                        ServerWrapper.logError("Failed to dump script: " + i, e);
                    }
                }
                return true;
            }

            case "findintinscript": {
                int search = Integer.parseInt(args[0]);
                for (ScriptDef def : ScriptDef.LOADED) {
                    if (def == null)
                        continue;
                    if (def.intArgs == null)
                        continue;
                    for (int i : def.intArgs)
                        if (i == search)
                            System.out.println("Found in " + def.id);
                }
                return true;
            }

            case "findstringinscript": {
                String search = String.join(" ", args).toLowerCase();
                for (ScriptDef def : ScriptDef.LOADED) {
                    if (def == null)
                        continue;
                    if (def.stringArgs == null)
                        continue;
                    for (String s : def.stringArgs) {
                        if (s == null)
                            continue;
                        if (s.toLowerCase().contains(search))
                            System.out.println("Found " + s + " in " + def.id);
                    }
                }
                return true;
            }

            case "findvarcinscript": {
                int id = Integer.parseInt(args[0]);
                for (ScriptDef def : ScriptDef.LOADED) {
                    if (def == null)
                        continue;
                    if (def.intArgs == null)
                        continue;
                    for (int i = 0; i < def.instructions.length; i++) {
                        if (def.instructions[i] == 43 && def.intArgs[i] == id) {
                            player.sendMessage("Script " + def.id + " sets varc " + id);
                        }
                    }
                }
                return true;
            }

            case "wipe": {
                String name = query.substring(command.length() + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null) {
                    player.sendMessage("Could not find player: " + name);
                    return true;
                }
                player.dialogue(
                        new MessageDialogue("Are you sure you want to wipe player: " + p2.getName() + "?"),
                        new OptionsDialogue(
                                new Option("Yes", () -> { //todo log this
                                    if(!p2.isOnline()) {
                                        player.sendMessage(p2.getName() + " is no longer online!");
                                        return;
                                    }
                                    p2.getInventory().clear();
                                    p2.getEquipment().clear();
                                    p2.getBank().clear();
                                }),
                                new Option("No", player::closeDialogue)
                        )
                );
                return true;
            }

            /**
             * Object commands
             */

            /*
             * Stat commands
             */
            case "resetme": {
                player.sendMessage("RESET!");
                for (StatType st : StatType.values()) {
                    if (st == StatType.Hitpoints)
                        player.getStats().set(st, 10, 1154);
                    else
                        player.getStats().set(st, 1, 0);
                }
                player.getCombat().updateLevel();
                player.getAppearance().update();
            }
            case "master": {
                int xp = Stat.xpForLevel(99);
                for (int i = 0; i < StatType.values().length; i ++) {
                    Stat stat = player.getStats().get(i);
                    stat.currentLevel = stat.fixedLevel = 99;
                    stat.experience = xp;
                    stat.updated = true;
                }

                player.getCombat().updateLevel();
                player.getAppearance().update();
                return true;
            }

            case "lvl": {
                StatType type = StatType.get(args[0]);
                int id = type == null ? Integer.valueOf(args[0]) : type.ordinal();
                int level = Integer.valueOf(args[1]);
                if(level < 1 || level > 255 || (id == 3 && level < 10)) {
                    player.sendMessage("Invalid level!");
                    return true;
                }
                Stat stat = player.getStats().get(id);
                stat.currentLevel = level;
                stat.fixedLevel = Math.min(99, level);
                stat.experience = Stat.xpForLevel(Math.min(99, level));
                stat.updated = true;
                if(id == 5)
                    player.getPrayer().deactivateAll();
                player.getCombat().updateLevel();
                return true;
            }

            case "iteminfo": {

                return true;
            }

            case "gfxan": {
                GfxDef def = GfxDef.get(Integer.valueOf(args[0]));
                if(def == null) {
                    player.sendMessage("Invalid id.");
                    return true;
                }
                player.sendMessage("Gfx " + def.id + " uses animation " + def.animationId);
                return true;
            }

            case "gfxmodel": {
                GfxDef def = GfxDef.get(Integer.valueOf(args[0]));
                if(def == null) {
                    player.sendMessage("Invalid id.");
                    return true;
                }
                player.sendMessage("Gfx " + def.id + " uses model " + def.modelId);
                return true;
            }

            case "findgfxa": {
                int animId = Integer.valueOf(args[0]);
                player.sendMessage("Finding gfx using anim " + animId + "...");
                Arrays.stream(GfxDef.LOADED)
                        .filter(Objects::nonNull)
                        .filter(def -> def.animationId == animId)
                        .forEachOrdered(def -> player.sendMessage("Found: " + def.id));
                return true;
            }

            case "findgfxm": {
                int model = Integer.valueOf(args[0]);
                player.sendMessage("Finding gfx using model " + model + "...");
                Arrays.stream(GfxDef.LOADED)
                        .filter(Objects::nonNull)
                        .filter(def -> def.modelId == model)
                        .forEachOrdered(def -> player.sendMessage("Found: " + def.id));
                return true;
            }

            case "findobj": {
                ObjectDef.LOADED.values().stream()
                        .filter(Objects::nonNull)
                        .filter(def -> !def.name.isEmpty())
                        .filter(def -> query.toLowerCase().contains(def.name.toLowerCase()))
                        .forEachOrdered(def -> player.sendMessage(def.id +" - "+ def.name));
                return true;
            }

            case "ag": {
                int animation = Integer.valueOf(args[0]);
                //if(animation != -1 && AnimationDefinition.get(animation) == null) {
                //    player.sendMessage("Invalid Animation: " + animation);
                //    return true;
                //}
                int gfx = Integer.valueOf(args[1]);
                //if(gfx != -1 && GfxDefinition.get(gfx) == null) {
                //    player.sendMessage("Invalid Graphics: " + gfx);
                //    return true;
                //}
                player.animate(animation);
                player.graphics(gfx, 0, 0);
                return true;
            }

            case "proj":
            case "projectile":
            case "printprojectile": {
                Projectile.print(Integer.valueOf(args[0]));
                return true;
            }

            case "sicon": {
                player.getAppearance().setSkullIcon(Integer.valueOf(args[0]));
                return true;
            }

            case "copy": {
                String name = query.substring(query.indexOf(" ") + 1);
                Player p2 = World.getPlayer(name);
                if(p2 == null) {
                    player.sendMessage(name + " could not be found.");
                    return true;
                }
                for(int slot = 0; slot < player.getInventory().getItems().length; slot++) {
                    Item item = p2.getInventory().get(slot);
                    if(item == null)
                        player.getInventory().set(slot, null);
                    else
                        player.getInventory().set(slot, item.copy());
                }
                player.sendMessage("You have copied " + name + "'s inventory.");
                for(int statId = 0; statId < StatType.values().length; statId++) {
                    Stat stat = player.getStats().get(statId);
                    Stat stat2 = p2.getStats().get(statId);
                    stat.currentLevel = stat2.currentLevel;
                    stat.fixedLevel = stat2.fixedLevel;
                    stat.experience = stat2.experience;
                    stat.updated = true;
                }
                player.getCombat().updateLevel();
                player.getAppearance().update();
                player.sendMessage("You have copied " + name + "'s stats.");
                for(int slot = 0; slot < player.getEquipment().getItems().length; slot++) {
                    Item item = p2.getEquipment().get(slot);
                    if(item == null)
                        player.getEquipment().set(slot, null);
                    else
                        player.getEquipment().set(slot, item.copy());
                }
                player.getAppearance().update();
                player.sendMessage("You have copied " + name + "'s armor.");
                for(int slot = 0; slot < player.getBank().getItems().length; slot++) {
                    BankItem item = p2.getBank().getItems()[slot];
                    if(item == null)
                        player.getBank().set(slot, null);
                    else
                        player.getBank().set(slot, item.copy());
                }
                player.sendMessage("You have copied " + name + "'s bank.");
                return true;
            }

            /**
             * Misc commands
             */
            case "reloadteles":
            case "reloadteleports": {
                DataFile.reload(player, teleports.class);
                return true;
            }

            case "reloadhelp": {
                DataFile.reload(player, Help.class);
                return true;
            }
            case "reloadcombat": {
                DataFile.reload(player, npc_combat.class);
                return true;
            }

            case "smute": {
                forPlayerTime(player, query, "::smute playerName #d/#h/perm", (p2, time) -> Punishment.mute(player, p2, time, true));
                return true;
            }

            case "resetbankpin": {
                forPlayer(player, query, "::resetbankpin playerName", p2 -> {
                    p2.getBankPin().setPin(-1);
                    player.sendMessage("Reset bankpin for " + p2.getName() + ".");
                });
                return true;
            }



            case "dumpdrops": {
                npc_drops.dump(String.join("_", args));
                return true;
            }





        }

        return false;
    }

    /**
     * Utils
     */

    private static void forName(Player player, String cmdQuery, String exampleUsage, Consumer<String> consumer) {
        try {
            String name = cmdQuery.substring(cmdQuery.indexOf(" ") + 1).trim();
            consumer.accept(name);
        } catch(Exception e) {
            player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
        }
    }

    private static void forNameString(Player player, String cmdQuery, String exampleUsage, BiConsumer<String, String> consumer) {
        try {
            String s = cmdQuery.substring(cmdQuery.indexOf(" ") + 1).trim();
            int i = s.lastIndexOf(" ");
            String name = s.substring(0, i).trim();
            String string = s.substring(i).trim();
            consumer.accept(name, string);
        } catch(Exception e) {
            player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
        }
    }

    private static void forNameTime(Player player, String cmdQuery, String exampleUsage, BiConsumer<String, Long> consumer) {
        forNameString(player, cmdQuery, exampleUsage, (name, string) -> {
            try {
                if(string.equalsIgnoreCase("perm")) {
                    consumer.accept(name, -1L);
                    return;
                }
                long time = Long.valueOf(string.substring(0, string.length() - 1));
                String unit = string.substring(string.length() - 1).toLowerCase();
                if(unit.equals("h"))
                    time = TimeUtils.getHoursToMillis(time);
                else if(unit.equals("d"))
                    time = TimeUtils.getDaysToMillis(time);
                else
                    throw new IOException("Invalid time unit: " + unit);
                consumer.accept(name, System.currentTimeMillis() + time);
            } catch(Exception e) {
                ServerWrapper.logError("Invalid command usage. Example: [" + exampleUsage + "]", e);
            }
        });
    }

    private static void forPlayer(Player player, String cmdQuery, String exampleUsage, Consumer<Player> consumer) {
        forName(player, cmdQuery, exampleUsage, name -> {
            try {
                Player p = getOnlinePlayer(player, name);
                if(p != null)
                    consumer.accept(p);
            } catch(Exception e) {
                player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
                e.printStackTrace();
            }
        });
    }

    private static void forPlayerTime(Player player, String cmdQuery, String exampleUsage, BiConsumer<Player, Long> consumer) {
        forNameTime(player, cmdQuery, exampleUsage, (name, time) -> {
            try {
                Player p = getOnlinePlayer(player, name);
                if(p != null)
                    consumer.accept(p, time);
            } catch(Exception e) {
                player.sendMessage("Invalid command usage. Example: [" + exampleUsage + "]");
            }
        });
    }

    private static Player getOnlinePlayer(Player player, String name) {
        Player p = World.getPlayer(name);
        if(p == null)
            player.sendMessage("User '" + name + "' is not online.");
        return p;
    }

    private static void teleport(Player player, int x, int y, int z) {
        if (player.wildernessLevel != 0 || player.pvpAttackZone) {
            player.sendMessage("You can't use this command from where you are standing.");
            return;
        }
        player.getMovement().startTeleport(event -> {
            player.animate(3864);
            player.graphics(1039);
            player.privateSound(200, 0, 10);
            event.delay(2);
            player.getMovement().teleport(x, y, z);
        });
    }

}