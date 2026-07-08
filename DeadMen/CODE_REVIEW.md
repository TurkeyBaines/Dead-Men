# Dead-Men RSPS — Code Review

**Reviewer:** Claude (Opus 4.8)
**Date:** 2026-07-04
**Scope:** Server modules — `DMS` (game server), `kronos-api` (engine lib), `DMFS` (JS5 file server), and the in-process `io.dm.panel` Server Panel. The vendored `runelite/` client fork is **out of scope**.
**Method:** Static analysis + targeted reads. Metrics from `grep`/`find`/`wc` over `DMS/src/main/java`.

> You asked me not to be shy, and then asked for more. So this is blunt and it is long. The project *works* and has real strengths (§2) — but you asked for the critique, so the critique dominates. Severity ratings are my judgement; treat 🔴 as "fix before the next tournament," 🟠 as "schedule it," 🟡 as "tidy when you're in there."

---

## 1. At a glance

| Metric | Value | Note |
|---|---:|---|
| Java files (DMS) | 1,359 | |
| Lines of code (DMS) | ~191,000 | ~151k excluding generated ID files |
| **Command classes with server-side rights checks** | **1 / 14** | see C1 — the headline finding |
| Automated test files | **4** | for 1,359 source files → effectively **0% coverage** |
| Player save writes | **non-atomic** | see C3 |
| `System.out/err.print*` calls | 163 | ad-hoc logging |
| `printStackTrace()` calls | 38 (23 in DMS) | swallowed traces |
| Empty `catch {}` blocks | ~10 | silent failure |
| `TODO`/`FIXME`/`HACK`/`XXX` | ~96 | accumulated debt |
| Outdated deps w/ known CVEs | gson 2.8.2, guava 28.1, mysql-conn 8.0.28 | see M1 |
| Largest hand-written classes | `Player.java` 1,782 · `CommandHandler.java` 814 | God classes |
| `World` mutable static fields | 23 | global mutable state |

**Overall verdict:** *A large, feature-rich, working server with a serious authorization hole, a data-loss risk, and effectively no test safety net.* The engine core (`kronos-api`) is well-built; the content layer (`DMS`) is where the risk concentrates. The four themes that matter most: **(1) commands aren't authorized server-side, (2) saves can corrupt, (3) nothing is tested, (4) failures are silent.**

---

## 2. What's genuinely good (credit where due)

- **`kronos-api/.../database/Database.java`** — HikariCP pooling, prepared-statement caching, leak detection, async execution on a dedicated executor, connection-validity checks. Professional-grade; well above the surrounding code.
- **The tick loop is defensively wrapped.** `ProcessWorker.process()` (`kronos-api`) catches around the whole tick (line 36–38) *and* around inner sections (67–69, 84–87), so one player's exception won't tear down the whole game loop. Good instinct. (Caveat in M-series: it catches `Exception`, not `Throwable`.)
- **Clean module separation** (`DMS` / `kronos-api` / `DMFS`) with real boundaries.
- **The Deadman tournament domain** (`Stage`/`Tournament`/`TournamentConfig`/`Mutator`/`DMMEvent`) is a clean, extensible, polymorphic state machine — the best-modelled game logic in the base.
- **No string-identity bugs.** Zero `== "literal"` comparisons across DMS — a common RSPS footgun you've avoided.
- **Consistent Lombok use**, cohesive content packaging, and a sensible `DatabaseStatement.execute(Connection)` callback that guarantees connection close.

---

## 3. Findings by severity

### 🔴 Critical

**C1 — Admin commands have no server-side authorization (privilege escalation).**
`DMS/.../network/incoming/handlers/CommandHandler.java:53–85`
The packet handler reads the command string and calls `handleAdmin(player, ...)` for **every player**. The only `isStaff()`/`isAdmin()` references in the method gate *logging* (line 57), a *lock check* (74), and an *error message* (82) — **never execution**. `handleAdmin` (line 88) drops straight into its `switch` with no rights gate, and **13 of 14 command classes** (`StatCommands`, `ItemCommands`, `MapCommands`, `TournamentCommands`, …) perform no rights check either (only `PlayerCommands` does).

Concretely: any client that sends command opcode 17 with `stat master`, `item <id> <amt>`, `tele <x> <y>`, or `punish …` gets max stats, spawned items, teleports, and moderation powers. The only thing stopping a normal player is the *stock client not showing the input* — i.e. **client-side-only protection, trivially bypassed** by a modified client or a packet bot. On a competitive tournament server, this is economy-and-integrity-ending.

**Fix:** classify every command by required privilege and enforce it in one place, server-side, before dispatch — e.g. a `RequiredRank` on each command and a single check in `handleAdmin`. Keep genuinely-public commands (`::players`, etc.) explicitly whitelisted. Never trust that the client didn't send the packet.

**C2 — SQL injection via player name.**
`DMS/.../utility/PlayerRestore.java:53`
```java
connection.prepareStatement("SELECT * FROM highscores WHERE username='" + player.getName() + "'");
```
Player-controlled string concatenated into SQL, then wrapped in a `PreparedStatement` (which does nothing here). Combined with C1's "don't trust the client," names are attacker-influenced. **Fix:** bind parameters (`WHERE username=?`). Then audit the other 6 concatenated statements (`Projectile.java:341`, `StaffBounty.java:149`, `LatestUpdate.java`) — lower-risk (ints/internal) but the pattern must go.

**C3 — Player saves are not atomic → account corruption / total progress loss.**
`DMS/.../model/entity/player/PlayerFile.java:115`
```java
Files.write(new File(CHARACTERS_FOLDER, player.getName().toLowerCase() + ".json").toPath(),
            json.getBytes(), ...);
```
This writes JSON **directly over the live save file** (default `TRUNCATE_EXISTING`). A crash, power loss, OOM, or full disk *between truncate and full write* leaves a truncated/corrupt `.json` — the player loses everything. For a server where progress *is* the game (season points, stats), that's the worst kind of bug: silent, rare, unrecoverable. Also `json.getBytes()` uses the platform-default charset (not explicit UTF-8). **Fix:** write to `name.json.tmp`, then `Files.move(tmp, target, ATOMIC_MOVE, REPLACE_EXISTING)`; use `getBytes(StandardCharsets.UTF_8)`. Consider a rolling backup (`name.json.bak`) on each save.

### 🟠 High

**H1 — Effectively zero automated tests.** 4 test files for ~191k LOC / 1,359 files. Every change is a live gamble; refactoring is dangerous; the CI (`azure-pipelines.yml`) can only prove it compiles. This is the biggest *long-term* risk. **Fix:** start with characterization tests on the highest-value isolated logic — `TournamentConfig`, `Currency`, `Stat`/XP math, `Punishment` durations, save/load round-trip — then grow.

**H2 — Fragile boot: one bad content class degrades the server silently.** `Server.main()` → `PackageLoader.load("io.dm")` eagerly `Class.forName`-**initializes every class**; failures are caught, logged, and boot *continues with that content silently missing*. This happens today: `MovementFixes.<clinit>` NPEs (`ObjectAction.register` → `obj is null`) and the server starts anyway. **Fix:** classify load failures fatal vs. optional; fail loud on fatal so corruption doesn't masquerade as a clean boot.

**H3 — Unbounded retry spin in the DB executor.** `kronos-api/.../database/Database.java:68` loops `while(true)` with `sleep(100)` on pool exhaustion — forever, pinning a worker thread. 100 workers → 100-connection pool; a DB stall can spin every worker indefinitely with no cap or circuit breaker. **Fix:** bound retries by attempts/deadline; surface failure via `statement.failed(...)`.

### 🟠 Medium

**M1 — Outdated dependencies with known CVEs.** `build.gradle`: `gson 2.8.2` (2017 — deserialization DoS CVE-2022-25647), `guava 28.1-jre` (2019 — CVE-2020-8908, CVE-2023-2976 temp-dir), `mysql-connector-java 8.0.28` (deserialization CVE-2023-22102). Also `netty-all` is the discouraged fat artifact (pull only the modules you use). `jackson-dataformat-yaml 2.11.0` is old (the databind line it pulls has had many CVEs). **Fix:** bump gson→2.10.1+, guava→32+, `mysql:mysql-connector-java`→`com.mysql:mysql-connector-j:8.4+`, and run `./gradlew dependencies` + a vuln scanner (OWASP dependency-check / `gradle-versions-plugin`).

**M2 — Unstructured, partly-dead logging.** 163 `System.out/err.print*`, 38 `printStackTrace()`. The SLF4J binding is `slf4j-simple` (→ stderr) while `DMS/src/main/resources/logback.xml` (referencing `co.elastic.logging.logback.EcsEncoder`) is **inert** — logback isn't on the classpath. So a tuned log config does nothing while real output scatters across raw prints. Bonus: `CommandHandler.java:65` does `System.out.println(query)` — **every command from every player echoed to stdout** (noise + minor info leak). **Fix:** one binding, delete/activate the dead config, convert prints to logged calls, drop the query echo.

**M3 — Silent failure.** ~10 empty `catch {}` plus many `printStackTrace()`-and-continue. Combined with H2, the base's dominant failure mode is *silent degradation*. **Fix:** log with context or rethrow; never swallow.

**M4 — Buggy RNG helper.** `kronos-api/.../utils/Random.java:32`
```java
public static <T> T get(List<T> list, Object exclude) {
    T result;
    while ((result = get(list)) == exclude) {}   // reference equality + unbounded loop
    return result;
}
```
Uses `==` (identity, not `.equals`) so it excludes by reference only — usually "works" by accident, silently fails when it should exclude by value. And if the list contains only the excluded element, it **spins forever** on the game thread. **Fix:** `Objects.equals`, and bail after N attempts.

**M5 — Copy-paste currency label.** `DMS/.../model/shop/Currency.java:146` — `PEST_CONTROL_POINTS(new CurrencyHandler("pvm points"){...})`. Copy-pasted from `PVM_POINTS` (line 122); pest-point messages say the wrong currency. 10-second fix, symptomatic of the enum's copy-paste style.

**M6 — God classes.** `Player.java` (1,782 LOC) and `CommandHandler.java` (814 LOC, one mega-`switch`). `Player` is the classic "everything hangs off the player" merge-magnet — untestable, unreasonable. **Fix (incremental):** extract components (combat/skills/containers/social) the player *has* rather than *is*; finish migrating `CommandHandler` to the command-class pattern you already started.

**M7 — Pervasive global mutable static state.** `World` has 23 mutable `public static` fields; `Deadman`, `Server.worker`, `World.players` are static singletons. This is *why* player fields get read off arbitrary threads with no synchronization, and *why* the Panel had to be built defensively. **Fix:** new subsystems depend on injected instances, not statics.

**M8 — Secrets in a plaintext properties file.** `DMS/server.properties` holds `database_host/user/password` (placeholders locally, but the pattern implies real creds in prod) with no env-var/secret indirection. **Fix:** read DB creds from environment; confirm any file with real values is git-ignored.

### 🟡 Low / Style

- **L1 — ~96 `TODO`/`FIXME`/`HACK`/`XXX`.** Convert the real ones to issues; delete the stale.
- **L2 — ~40k LOC of generated constants committed** (`ObjectID` 970 KB, `ItemID` 576 KB, `NpcID` 399 KB) — bloats the repo and grep. Generate into `build/generated` instead.
- **L3 — `@SneakyThrows`** in `Database.execute` hides the checked-exception contract; a smell if it spreads.
- **L4 — Gradle deprecation.** `kronos-api/build.gradle` uses the deprecated `JavaPluginConvention` (removed in Gradle 9). Will break on the next major bump.
- **L5 — Inconsistent null-handling.** e.g. `TournamentConfig.printConfig` dereferences `MUTATOR.name()` with no guard → NPE if a config ever ships a null mutator.
- **L6 — ~8 files open streams without try-with-resources** — leak risk on exception paths.
- **L7 — Default-charset `getBytes()`/IO** in save/load — non-portable for non-ASCII data.

---

## 4. Cross-cutting themes

**Security.** C1 + C2 together are the story: the server trusts the client for authorization *and* interpolates player strings into SQL. Treat every player-derived value (name, chat, command args) as hostile at every sink — SQL, file paths, format strings, the command dispatcher. A single "authorize then validate" pass on the packet/command layer would close most of it.

**Data integrity.** C3 is the sharp edge, but the theme is broader: saves aren't atomic, there's no backup-on-save, and DEV mode's `DummyDatabase` means a lot of persistence paths are exercised only in prod. Round-trip save/load tests (H1) would guard this.

**Concurrency.** The tick model *mostly* serializes mutation (good), but it's an informal invariant — `Player`/`World` state is shared across game/Netty/DB threads with no synchronization, and `World.players` iteration is exposed to `ConcurrentModificationException` under the wrong interleaving. Document the model; route all mutations through the tick (the Panel's `ActionDispatcher` is a working template).

**Observability.** Dominant failure mode = *silent degradation*: swallowed exceptions, partial boots, dead log config, an unread `System.out` firehose. For a live tournament ("did the season roll correctly?"), you're flying blind. Structured logging + fail-loud boot + a few health assertions pay for themselves the first incident.

---

## 5. Prioritized recommendations

**Do before the next tournament (hours–1 day):**
1. **C1** — add server-side rights enforcement to the command dispatcher. *Single most important change in this document.*
2. **C3** — make player saves atomic (temp + `ATOMIC_MOVE`), add a `.bak`.
3. **C2** — parameterize `PlayerRestore.java:53`; audit the other concatenated SQL.
4. **H3** — bound the DB retry loop. **M5** — fix the currency label. Drop the `System.out.println(query)` echo.

**Near-term (days):**
5. **H2** — classify `PackageLoader` failures fatal vs. optional; fail loud on fatal.
6. **M1** — bump gson/guava/mysql-connector; add a dependency vuln scan to CI.
7. **H1** — first characterization tests: save/load round-trip, `Currency`, `TournamentConfig`, XP math.
8. **M8** — DB creds from env. **M4** — fix `Random.get(list, exclude)`.

**Strategic (ongoing):**
9. **M2/M3** — one logging framework, structured, no swallowed errors.
10. **M6** — keep decomposing `CommandHandler`/`Player`.
11. **M7** — inject instances over new statics; document the threading model.
12. **L2** — move generated constants to a generated-sources set.

---

## 6. A note on the code I added (`io.dm.panel`)

Not exempt:
- `ConsoleTeeStream` line-buffers under a lock on every `System.out` write — fine only because your log volume is modest.
- `ItemGridView`/`ItemPickerDialog` refresh via a `PauseTransition(250ms)` after an async edit — a timing hack, not a real completion signal; `ActionDispatcher` should call back when the tick actually applies.
- Item icons fall back to an external CDN when no local folder exists — a networked dependency in an otherwise offline tool.
- The Panel reads global statics (`World`, `Deadman`, `Server.worker`) I criticized in M7 — because that's the only API the base exposes. It inherits the coupling.
- And per C1: the Panel's "run command as staff" is only as safe as the command layer — which, until C1 is fixed, is *not* safe. Fixing C1 hardens the Panel for free.

---

*End of review. The five-minute fixes (C2 parameterization, M5 label, dropping the query echo) I can patch right now. C1 (authorization) and C3 (atomic saves) are the two I'd most want to pair on — say the word and I'll implement either.*
