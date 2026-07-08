# STAGES — Dead-Men RSProt migration, drip-feed work plan
Created 2026-07-08. Parent status doc: ../THE_PLAN.txt (read that for the big picture; each stage
file below is self-contained on purpose so an agent does NOT need THE_PLAN.txt to do its stage).

## How to drip-feed a stage to an agent
Paste this (swap the filename):

    Read C:\Users\Turk\Documents\GitHub\Dead-Men\DeadMen-RSProt\stages\STAGE-02-ground-items.txt
    and complete that stage. Follow its RULES and WRAP-UP sections exactly.

One stage per chat session. Stages are sized to fit comfortably in one context window.

## Stage index & recommended order
| Stage | File                                  | Fixes / delivers                                   | Depends on |
|-------|---------------------------------------|----------------------------------------------------|------------|
| 00    | STAGE-00-git-checkpoint.txt           | Commit all work to git (NOTHING is committed!)     | —          |
| 01    | STAGE-01-baseline-capture.txt         | Boot + scripted click-through; triage data files   | 00         |
| 02    | STAGE-02-ground-items.txt             | Ground items spawn/render/despawn                  | 01 (soft)  |
| 03    | STAGE-03-npc-spawning.txt             | NPCs visible in the world                          | 01 (soft)  |
| 04    | STAGE-04-main-interfaces.txt          | Bank + all modal/main interfaces render            | 01 (soft)  |
| 05    | STAGE-05-inventory-actions.txt        | Wield/drop/eat etc. actually execute               | 01         |
| 06    | STAGE-06-unequip.txt                  | Unequip from worn-equipment tab                    | 05         |
| 07    | STAGE-07-object-interactions.txt      | Object clicks execute on arrival                   | 01         |
| 08    | STAGE-08-item-on-x-and-drag.txt       | Item-on-item/npc/object/player + inventory drag    | 05         |
| 09    | STAGE-09-npc-extended-info.txt        | NPC animations/hitsplats/overheads                 | 03         |
| 10    | STAGE-10-player-fx-verification.txt   | Player anims/hitsplats/projectiles/gfx verified    | 01         |
| 11    | STAGE-11-polish-and-diag-cleanup.txt  | Choose-option background; strip temp diagnostics   | 04,05,07   |
| 12    | STAGE-12-legacy-cleanup.txt           | Delete legacy 184 transport (Milestone 5)          | ALL        |

"(soft)" = the stage can run without it, but Stage 01's dump files make it easier.
Stages 02, 03, 04 are independent of each other — they can go to different agents in parallel.
Stages 05→06→08 form a chain (same subsystem, findings carry forward).

## RULES for every agent (each stage file also says "follow the RULES in README.txt")
1. Working dir: C:\Users\Turk\Documents\GitHub\Dead-Men\DeadMen-RSProt (all paths in stage files
   are relative to this).
2. Build check before handing back: `./gradlew :DMS:compileJava` must print BUILD SUCCESSFUL.
3. You cannot run the game client. Live verification is done by the human (server + RSProx +
   rev-228 client). Structure your work as: investigate → fix → compile → hand the human the
   exact TEST SCRIPT from your stage file (adjusted if your fix changed what to look for). If the
   human reports failure, iterate using what they report plus PROTOCOL_DUMP.txt.
4. Ground truth priority when facts conflict: (1) RSProt's own jars/sources in the Gradle cache
   (`net.rsprot:osrs-228-*:1.0.0-ALPHA-20260630` — osrs-228-model has a full sources jar),
   (2) Cache228 data via CACHE_INTERFACE_DUMP.txt or the repo's own FileStore/IndexFile readers,
   (3) code comments, (4) HANDOFF.txt claims. The vendored runelite/ checkout is rev 184 — NEVER
   use it as a 228 reference.
5. Do NOT read HANDOFF.txt end-to-end (690 lines of session logs). If your stage file cites a
   HANDOFF section name, grep for that heading only.
6. Scope discipline: fix only what your stage covers. If you find an out-of-scope bug, note it in
   your wrap-up — do not fix it.
7. WRAP-UP (mandatory, in this order):
   a. Flip the STATUS line at the top of your stage file (DONE yyyy-mm-dd / BLOCKED: reason).
   b. Append a short dated summary (what changed, files touched, what the human confirmed) to
      HANDOFF.txt under a heading `## STAGE NN — <title> (yyyy-mm-dd)`.
   c. If you discovered facts that change other stages, add a line to their stage file under a
      `NOTES FROM OTHER STAGES` heading (create it if absent).
8. Two diagnostic files land in the project root when the server runs — use them before guessing:
   - PROTOCOL_DUMP.txt: one line per incoming interaction packet (accumulates across runs).
   - CACHE_INTERFACE_DUMP.txt: every interface's real Cache228 component count + scripts
     (regenerated each boot).

## How to boot for a live test (human does this; agents just reference it)
1. `./gradlew :DMS:compileJava` then run main class `io.dm.Server` (`./gradlew :DMS:run` or IDE).
   Server config comes from server.properties (template: DMS/server.example.properties).
2. Launch RSProx with the "Dead-Men (Local)" target (import rsprox-proxy-targets.yaml if missing),
   log in with any username (dev mode skips auth; new name = fresh character at Lumbridge).
