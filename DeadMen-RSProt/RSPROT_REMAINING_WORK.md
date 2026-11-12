# Dead-Men RSProt port — remaining work (verified 2026-11-12)

Repo: C:\Users\Turk\Documents\GitHub\Dead-Men\DeadMen-RSProt  (git root is the parent dir; prefix paths with DeadMen-RSProt/ for git show)
Verified by a second agent (grep-only pass, 2026-11-12): all claims below confirmed; see section notes for the two wording corrections it made (D7 IP is "", A21 word-match grep).
Target: RSProt net.rsprot osrs-228 1.0.0-ALPHA-20260630 (dm-net/build.gradle rsprotRev='228'), stock Cache228.
Build: `./gradlew :DMS:compileJava :dm-net:compileKotlin --offline` -> EXIT 0 (2026-11-12).
Key files: DMS/src/main/java/io/dm/network/PacketSender.java (PS), DMS/src/main/java/io/dm/network/rsprot/RsProt.java (RP),
dm-net/src/main/kotlin/io/dm/net/DmIncomingPackets.kt (ALL_INCOMING list), DMS/src/main/java/io/dm/model/entity/player/Player.java (PL).

Legend: callers = call sites outside PacketSender.java.

## A. Outgoing packets with NO RSProt branch (silently dropped for every real player)
| # | Method (PS) | Legacy opcode | Callers | RSProt target | Impact |
|---|---|---|---|---|---|
| A1 | setAlignment | 26 | 27 | IfSetPosition | interface component positioning — DONE 2026-11-12 (pending live-test) |
| A2 | moveInterface | 73 | 1 | IfMoveSub | — DONE 2026-11-12 (pending live-test) |
| A3 | sendModel | 63 | 0 | IfSetModel | dead unless content added — DONE 2026-11-12 (pending live-test) |
| A4 | sendModelInformation | 13 | 0 | IfSetAngle | dead — DONE 2026-11-12 (pending live-test) |
| A5 | unlinkItems | 57 | 0 | UpdateInvStopTransmit | dead — DONE 2026-11-12 (pending live-test) |
| A6 | sendShopItems | 49 | 1 (shop open) | UpdateInvFull (+display-id mapping) | shops show no stock — DONE 2026-11-12 (pending live-test) |
| A7 | updateItems(ShopItem[]) | 1 | 1 (ShopItemContainer) | UpdateInvPartial | shop stock changes invisible — DONE 2026-11-12 (pending live-test) |
| A8 | resetMapFlag | 25 | 1 | SetMapFlag(255,255) | — DONE 2026-11-12 (pending live-test) |
| A9 | clearChunk / clearChunks | 65 | 1 + 1 | UpdateZoneFullFollows | zone resets (instances/object cleanup) — DONE 2026-11-12 (pending live-test) |
| A10 | sendMapState | 55 | 12 | MinimapToggle | minimap lock in minigames/cutscenes — DONE 2026-11-12 (pending live-test) |
| A11 | turnCameraToLocation | 68 | 10 | CamLookAt | cutscenes/bosses — DONE 2026-11-12 (pending live-test) |
| A12 | moveCameraToLocation | 0 | 9 | CamMoveTo | — DONE 2026-11-12 (pending live-test) |
| A13 | shakeCamera | 51 | 8 | CamShake | — DONE 2026-11-12 (pending live-test) |
| A14 | resetCamera | 76 | 13 | CamReset | — DONE 2026-11-12 (pending live-test) |
| A15 | sendUrl | 87 | 3 | UrlOpen | — DONE 2026-11-12 (pending live-test) |
| A16 | sendWidget | 91 (custom 184) | 12 | no 228 equivalent — rebuild as interface/cs2 | custom popup widgets — DONE 2026-11-12 (decision: chat messages only; pending live-test) |
| A17 | sendTeleports | 89 (custom 184) | 3 | no 228 equivalent — rebuild teleport UI | teleport interface — DONE 2026-11-12 (decision: custom teleport interface 583 removed entirely; data + teleports.previous/teleport kept) |
| A18 | sendSkillinterface | 95 (custom, body commented) | 2 | none | custom skill guide — DECIDED 2026-11-12: use the stock XP lamp interface (240, 28 components in Cache228). BLOCKED on ids: SkillLamp.java still opens custom Interface.SKILL_LAMP=134 with child ids 3-26 from the Kronos cache; stock 240's skill-button/confirm component ids and the selected-skill mechanism cannot be confirmed from RSProt, the cache dump (component counts only) or AlterRSPS (no lamp code). Needs a component listing of 240 (e.g. from a RuneLite cache dump) |
| A19 | worldHop | 76 (custom) | 0 | none/LogoutTransfer | dead |
| A20 | refreshGameFrame | 58 | 0 | IfOpenTop | dead |
| A21 | sendRegion dynamic branch (opcode 71) | 66/71 | PlayerUpdater only (legacy) | RebuildRegion | RP.maybeRebuildRegion only ever sends RebuildNormal; `grep -w RebuildRegion` = 0 hits. Every DynamicMap user (26 files: Inferno, FightCaves, CoX, ToB, Zulrah, Vorkath, Hydra, Skotizo, PestControl, PVMInstance, PVPInstance, House, Versus finals, Karuulm, CanoeStation...) is broken for RSProt players — DONE 2026-11-12 (pending live-test) |
| A22 | sendItems/updateItems item-attribute side channel | 49/1 | all containers | n/a — needs ObjCustomise/IfSetText/cs2 approach | Kronos item-upgrade display lost (PS comment line ~503) — DONE 2026-11-12 (decision: remove the whole item-upgrade feature; see HANDOFF) |
| A23 | sendLogin | 2 | legacy only | n/a | dead code, delete |
| A24 | sendVarp id>=20000 custom-varp filter | 34/42 | Config has 0 ids>=20000 | n/a | dead code |

## B. Legacy packet writers OUTSIDE PacketSender (bypass queueRsprot entirely)
| # | Location | What | RSProt target | Callers |
|---|---|---|---|---|
| B1 | PL.sendFriends (line ~349, opcode 61) | friends list | UpdateFriendList + FriendListLoaded | sendSocial 7 — DONE 2026-11-12 (pending live-test) |
| B2 | PL.sendIgnores (~369, opcode 59) | ignore list | UpdateIgnoreList | — DONE 2026-11-12 (pending live-test) |
| B3 | PL.sendPrivacy (~383, opcode 17) | privacy settings | ChatFilterSettings / ChatFilterSettingsPrivateChat | 1 — DONE 2026-11-12 (pending live-test) |
| B4 | PL.sendPM (Protocol.outgoingPm) | PM echo to sender | MessagePrivateEcho | 2 — DONE 2026-11-12 (pending live-test) |
| B5 | PL.sendReceivePM (~392, opcode 45) | incoming PM | MessagePrivate | 1 — DONE 2026-11-12 (pending live-test) |
| B6 | social/clan/ClanChat.java (opcodes 86 settings, 48 channel list, 22 message, leave buffer) | clan/friend chat | UpdateFriendChatChannelFull(V2)/SingleUser, MessageFriendChannel | 7 files — DONE 2026-11-12 (pending live-test) |
| B7 | PlayerUpdater.java / PlayerNPCUpdater.java (opcodes 4, 36/53) | legacy GPI/NPI | replaced by PlayerInfo/NpcInfo — dead for RSProt | CoreWorker guards session==null |
| B8 | masks/*.java send() + Appearance.data OutBuffer + ChatUpdate encode | legacy mask encoders | dead for RSProt (bridged in RP.pushMaskUpdates / pushNpcMaskUpdates) | delete in Stage 12 |

## C. Incoming packets still on the accept-and-drop no-op path (RP.start registerNoOpsExcept)
Wired today (21): MoveGameClick, MoveMinimapClick, ClientCheat, MessagePublic, OpLoc, OpNpc, OpObj, OpPlayer, If3Button, OpLocT, OpObjT, OpNpcT, OpPlayerT, IfButtonT, IfButtonD, ResumePauseButton, ResumePCountDialog, ResumePStringDialog, ResumePNameDialog, ResumePObjDialog, CloseModal.
| # | Packet | Legacy handler it should reach | Impact |
|---|---|---|---|
| C1 | MessagePrivate | FriendsHandler (opcode 48 PM) | PMs — DONE 2026-11-12 (pending live-test) |
| C2 | FriendListAdd / FriendListDel / IgnoreListAdd / IgnoreListDel | FriendsHandler | friends/ignore mgmt — DONE 2026-11-12 (pending live-test) |
| C3 | FriendChatJoinLeave / FriendChatKick / FriendChatSetRank | ClanHandler (53/22) | clan chat — DONE 2026-11-12 (pending live-test) |
| C4 | SetChatFilterSettings | PrivacyHandler (21) | privacy toggles — DONE 2026-11-12 (pending live-test) |
| C5 | WindowStatus | DisplayHandler (72) | resizable/fixed switching; login hardcodes fixed 548 (RP.openFrame) — DONE 2026-11-12 (pending live-test; 161 classic-resizable only, 164 stacked not selected) |
| C6 | OpLoc6 / OpNpc6 / OpObj6 | examine (legacy option-6 branches are debug-only) | no examine text for objects/NPCs/ground items — DONE 2026-11-12 (pending live-test; npc/obj text is generic 'It's a <name>.' since Cache228 has no examine text for them) |
| C7 | Idle | idle logout (PL idleTicks logic ~1696 exists) | AFK logout — LEFT NO-OP 2026-11-12: legacy idleTicks is movement-based (Player ~1728) and never logs out; the Idle packet is informational |
| C8 | Teleport | TeleportHandler (7) — admin world-map teleport | — DONE 2026-11-12 (pending live-test) |
| C9 | ClickWorldMap | (no legacy) | optional |
| C10 | HiscoreRequest | HiscoreReply | optional |
| C11 | BugReport / DetectModifiedClient / ReflectionCheckReply / SendPingReply / ConnectionTelemetry | anti-cheat/telemetry | optional |
| C12 | MapBuildComplete | RegionUpdateHandler (13, empty body) | low — LEFT NO-OP 2026-11-12 (legacy handler body was empty) |
| C13 | If1Button / IfSubOp | n/a (IF1 obsolete) | confirm unused, leave no-op |
| C14 | Appearance/character design (legacy opcode 44 AppearanceHandler, Interface.APPEARANCE_CUSTOMIZATION=269, MakeoverMage) | no RSProt packet exists; must be rebuilt as If3Button/varbit flow | makeover mage / new-player design |
| C15 | ClanChannel*/AffinedClanSettings* | OSRS clans — not a 184 feature | skip |
| C16 | onPublicChat parity (RP ~458) bypasses ChatHandler: no mute/IP-mute/shadow-mute, no bad-word filter, no chat logging, no `!` admin force-text, no clan-chat (type 2) routing | ChatHandler | moderation gaps — DONE 2026-11-12 (pending live-test) |
| C17 | onCommand parity: legacy ChatHandler also routed `::`/`;;` typed in chat | fine via ClientCheat | ok |

## D. Login / session (RP.doLogin, PlayerFile.loadForRsProt)
| # | Gap | Evidence |
|---|---|---|
| D1 | No ALREADY_LOGGED_IN check — second login of same name creates a second Player | deleted PlayerLogin.java had it (git show HEAD:./DMS/.../PlayerLogin.java line 46); RP.doLogin has no isOnline/getPlayer lookup — DONE 2026-11-12 (pending live-test) |
| D2 | No WORLD_FULL handling (World.players.add result unchecked) | PlayerLogin line 52 had it — DONE 2026-11-12 (pending live-test; capped at avatar index 2047) |
| D3 | Bans/locks not enforced (IPBans, MACBan, UUIDBan, Punishment banned/locked) | legacy LoginDecoder/PlayerLogin; doLogin only does bcrypt — PARTIAL 2026-11-12: IP ban, UUID ban and BANNED group enforced; MAC ban + Punishment locks NOT (no MAC in the 228 LoginBlock) |
| D4 | login_set allow-list unenforced | HANDOFF Stage 12 increment 1 note |
| D5 | 2FA dropped — AuthenticatorResponse.NoAuthenticator hardcoded (DmLogin.kt) | legacy LoginDecoder tfa logic lines 121-223 |
| D6 | Reconnect = full doLogin (new Player object) instead of rebinding to the online player | RP.loginService().reconnect -> doLogin |
| D7 | Player IP/UUID/MAC not captured — LoginInfo built with null channel -> ipAddress "" and ipAddressInt 0 (LoginInfo.java 48-50); everything using getIp() (mutes, bans, Loggers) gets an empty string | RP.doLogin `new LoginInfo(null, ...)` — DONE 2026-11-12 (pending live-test; IP from login ctx remoteAddress, UUID hex from LoginBlock) |
| D8 | Temporary starter kit + Lumbridge override hardcoded in doLogin | RP ~783-795 |
| D9 | UPDATING/system-update login block (Response.UPDATING) not reproduced | PlayerLogin line 34 — DONE 2026-11-12 (pending live-test) |
| D10 | Logout: forceLogout on AIPlayer NPEs (ArtificialChannel.writeAndFlush null) | HANDOFF Stage 12 |

## E. Avatar / appearance / FX gaps (RP.syncAppearance, pushMaskUpdates)
| # | Gap | Evidence |
|---|---|---|
| E1 | Weapon render animations: setBaseAnimationSet(808,823,...) hardcoded; legacy Appearance uses WeaponType.renderAnimations / setCustomRenders | RP 1364, Appearance.java 182-184 — DONE 2026-11-12 (pending live-test) |
| E2 | Overhead prayer icon never bridged: setOverheadIcon(-1) hardcoded; Appearance.prayerIcon has 16 content setters | RP 1372 — DONE 2026-11-12 (pending live-test) |
| E3 | Player->NPC transform never bridged: transformToNpc(-1) hardcoded; Appearance.npcId/setNpcId used by content | RP 1374 — DONE 2026-11-12 (pending live-test) |
| E4 | Player hidden flag never bridged: setHidden(false) hardcoded; Entity.hidden / PL.setHidden(true) at 1435 exist | RP 1375 — DONE 2026-11-12 (pending live-test) |
| E5 | Hit-source tinting (sourceIndex -1) — documented accepted gap | RP pushMaskUpdates |
| E6 | NPC head icons (NpcHeadIconSpecific / avatar headicon) not bridged — 7 NPC masks only | RP.pushNpcMaskUpdates — NO-OP 2026-11-12: no legacy runtime NPC head-icon state exists (only NPCDef.headIcon, which the client reads from the cache itself) |
| E7 | World-entity info allocated per player but never used — harmless | RP driveUpdate step 2 |

## F. Revision drift 184 -> 228 (content tables, not transport) — required for "everything works"
| # | Area | Scale | State |
|---|---|---|---|
| F1 | Interface.java component/interface ids | 157 constants | only BANK family, INVENTORY, EQUIPMENT, dialogue children audited; InterfaceType has 6 overlay layers marked TODO (InterfaceType.java 68-74) |
| F2 | Config.java varp/varbit ids | 378 entries | mostly unaudited; Stage 11 already found 2 wrong (5983 vs 10670) |
| F3 | sendClientScript ids/signatures | 108 call sites | partially audited (917, 814 oiiss) |
| F4 | Enum/struct ids | e.g. enum 1174 slayer names wrong type | THE_PLAN §9 |
| F5 | NPC spawn ids | 66 spawn files; audit: 4266 auto-remapped, 6571 unfixable, 2494 "no usable cache NPC" | NPC_SPAWN_AUDIT.txt |
| F6 | Object id/coordinate drift | 11 stock objects + RevCaves doors + MovementFixes boot crash; ~84 instance-template regions fail object parse | THE_PLAN §9, BASELINE-FINDINGS |
| F7 | Custom Kronos cache content (items >=30000, custom NPCs/objects, noticeboard iface 720, custom head icons, custom map edits) | hidden by ItemDef.isValid guards / commented registrations (TabQuest, Citadel/Overworld areas) | needs a 228 cache repack or removal decision |
| F8 | Player saves containing custom item ids | existing characters | isValid blanks them client-side; data migration needed |
| F9 | Double item reader (ItemDef legacy loader + Cache228ItemDefs) now both read stock cache | consolidate | |
| F10 | DisplayHandler.sendDisplay uses 184 frame 165 layout | whole resizable layout table is 184-era | with C5 — DONE 2026-11-12 with C5 (resizable slot table now from AlterRSPS; legacy DisplayHandler.sendDisplay is unreachable for RSProt players) |

## G. RSProt-side bugs still open / unverified (from stages)
| # | Item | Status |
|---|---|---|
| G1 | Stage 07 object interaction on arrival | IN PROGRESS, root cause unconfirmed, [OBJACTION] diags in tree |
| G2 | Stage 10 player FX / projectiles (Player.localPlayers fix 07-19) | code fixed, never live-tested |
| G3 | Stage 09 NPC extended info | code-complete, never live-tested |
| G4 | Stage 05: left-click default option, dropOptionIndex latent risk, item-on-item mask 3407870 (2nd guess) | unconfirmed |
| G5 | Stage 11: options dialogue "not firing" un-root-caused; magic tab components 4/12 unknown | open |
| G6 | Equipment IfSetEvents start/end=-1 semantics | uncertain |
| G7 | Rev caves client crash | open |
| G8 | Follow/Trade Bug C (TargetRoute) | speculative, TEMP diag |
| G9 | Stage 15/16 player options + Follow/Trade | code fixed, not re-tested |
| G10 | Milestone 1 zone payload coordinate fix (build-area-relative) | live-confirmed for ground items; projectiles/area sound unconfirmed |

## H. Legacy cleanup (Stage 12 remaining increments) + hygiene
| # | Item |
|---|---|
| H1 | Incoming.java / IncomingDecoder.java opcode table + Incoming.load() at boot (Server.java 178) + per-handler handle(Player, InBuffer, int) entry points (keep handleAction) |
| H2 | PacketSender.write(), ISAACCipher, all OutBuffer fallback bodies, sendMapPacket |
| H3 | PlayerUpdater / PlayerNPCUpdater / mask send() encoders / Appearance.data |
| H4 | kronos-api netty remnants: Message, MessageDecoder, ExceptionHandler, Protocol, PacketSizeTool, LoginInfo channel ctor |
| H5 | DMFS module (legacy JS5 update server) still in settings.gradle; root build.gradle references DMFS + nonexistent kronos-server-live |
| H6 | dm-net unused scaffolding: GamePackets.kt, DmPacketHandlers, DmIncomingPackets.register (0 callers) |
| H7 | TEMP diagnostics: [ANIM] (RP), [OBJACTION] (ObjectActionHandler, RouteFinder), [TARGETROUTE], [MLM_RAW] (Region), SERVER-OUT (OptionsDialogue), [LOGIN]/[TICK] printlns |
| H8 | Boot dumpers run unconditionally: CacheInterfaceDumper, ItemOptionDumper, ObjectDef opcode audit, NpcSpawnAudit class |
| H9 | Revision labels say 236/239 in Server.java 211, DMS/build.gradle 24, dm-net/build.gradle 4, DeadMenNetworkServiceFactory.kt 22, server.properties — actual is 228; js5_cache_path duplicates cache_path |
| H10 | AI bot path (AIPlayer/PlayerBot/ArtificialChannel) has no RSProt-free design; 0 spawners today |
| H11 | Git: 80 modified + 47 untracked files (sigils tier1-3, NpcSpawnAudit, STAGE-13) uncommitted; hs_err_pid20728.log (native OOM) in DMS/ |
| H12 | Docs: MIGRATION.txt/THE_PLAN.txt/HANDOFF.txt say Milestone 5 "not started" vs Stage 12 increment 1 done; runelite-184 references |
