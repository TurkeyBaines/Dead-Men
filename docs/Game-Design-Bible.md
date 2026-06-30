# Dead Men — Game Design Bible

> **Status:** Living document. This is the constitution for *Dead Men*. Every future
> decision — a new item, an event, a rate change, a UI tweak — should be checkable against
> this document. If a proposed change can't be justified by the Vision and Pillars below,
> it doesn't go in, no matter how cool it sounds.
>
> **Companion document:** [`Tournament-Design.md`](./Tournament-Design.md) holds the tuned
> *numbers* (loot weights, rates, prices, runtimes). This Bible holds the *why*. When the two
> ever disagree, the Bible wins on direction and the companion wins on exact values.
>
> Authored as lead designer for the Dead Men project. Treat it as opinionated on purpose —
> a design with no point of view is a design no one remembers.

---

## Part I — The North Star

### 1. The Fantasy (what game are we actually making?)

> **Dead Men is a high-stakes survival arena where every life is temporary, every fight
> matters, and a few hours from now everyone starts over as equals.**

Strip away the RuneScape skin and the genre is a **battle-royale roguelike with an MMO's
depth**. Players are dropped in with nothing, claw their way to power against the clock and
each other, and the tension comes from one truth: *you can lose everything, and soon you'll
lose it anyway.* The reset isn't a punishment — it's the **promise**. It means:

- The new player who joins today is never permanently behind the veteran.
- A bad tournament is forgotten in hours; a great one is legend.
- Every single tournament is a complete story with a beginning, middle, and end.

That promise — **"a fresh, fair, complete experience every time you log in"** — is the most
valuable thing this server owns. **Protect it above all else.** The moment power persists
between tournaments, or money buys an edge, or veterans get permanent advantages, the
fantasy dies and Dead Men becomes just another grindy PvP server.

### 2. The Player (who are we building for, and what do they want?)

Players come to a PvP-survival server for four overlapping reasons. Design must feed all
four, because a server that only serves one type empties out fast:

| Player type | What they crave | What we give them |
|-------------|-----------------|-------------------|
| **The Killer** | The duel, the clutch, the kill feed | Fair fights, visible bounties, finals, rivalries |
| **The Achiever** | Progression, mastery, "best in the lobby" | Leaderboards, ranks, seasonal titles, build mastery |
| **The Explorer** | Variety, secrets, "what does this do?" | Rotating mutators, events, build experimentation |
| **The Socializer** | Belonging, teams, banter | Teams (Duo/Trio), clans, chat, shared victories |

**The single most underserved type on most PvP servers is the Socializer** — and they're the
glue that keeps a community alive. Design for teams, rivalries, and shared stories, not just
solo combat. A player who has *friends* on the server has a reason to log in even when they
don't feel like fighting.

### 3. Design Pillars (the five tests every decision must pass)

These are non-negotiable. When in doubt, run the idea through all five.

#### Pillar 1 — **Fair Resets.** *Power never persists; only prestige does.*
Gear, stats, and currency wipe every tournament. The only things that survive are cosmetic
and reputational: titles, ranks, leaderboard history, cosmetic unlocks. A returning veteran
should be *recognizable* (cosmetics, rank) but never *stronger* (gear, stats). This is the
pillar that makes Dead Men worth playing. **If a feature lets time or money buy power, it
fails this pillar and does not ship.**

#### Pillar 2 — **Skill Over Stuff.** *Outplaying beats out-gearing.*
Gear should *tilt* fights, never *decide* them. A skilled player in mid-gear should be able
to beat an unskilled player in BiS often enough that mechanical skill is the dominant
variable. This is why BiS is rare and earned (events/PvP), never bought or trash-mob common.
Gear is a *thumb on the scale*, not the whole scale.

#### Pillar 3 — **Always Something at Stake.** *Tension is the product.*
Deadman's soul is risk. A player carrying loot they could lose, a bank key they need to use,
a bounty on their head, a gas wall closing in — there must *always* be a reason the next
five minutes matter. Safe, frictionless play is the enemy. We don't remove danger; we make
danger *interesting and fair*.

#### Pillar 4 — **Every Tournament Tells a Story.** *Variety is retention.*
Two tournaments should never feel identical. Mutators, event rotations, formats, and the
emergent drama of *who's winning* make each one a fresh story. A player should be able to
say "remember the Berserk Finale where three trios collided on the Gold Cart" — that's the
content. We manufacture the conditions for stories; players write them.

#### Pillar 5 — **Respect the Player's Time.** *Frustration is not difficulty.*
Hard is good. Tedious, unclear, or unfair is not. A death should feel earned. A loot drought
should never starve a player out of the game. The clock should always be visible, the rules
always knowable, the next goal always obvious. We make players *think*, never make them
*guess at our intent*.

> **The 30-second gut check for any new idea:** Does it keep resets fair (P1)? Does it reward
> skill over gear (P2)? Does it raise the stakes (P3)? Does it add variety (P4)? Does it
> respect the player's time (P5)? If it scores poorly on two or more, cut it or rework it.

---

## Part II — The Experience

### 4. The Nested Gameplay Loops

Great games are loops inside loops, each one rewarding on its own timescale. Dead Men has four:

```
SECONDS  ── Combat loop:   position → attack → eat/pot → react → kill or die
MINUTES  ── Session loop:  skill up → gear up → fight → bank loot → repeat
HOURS    ── Tournament loop: Rush (gear) → War (fight/events) → Squeeze (finals) → reset
WEEKS    ── Season loop:    place → earn points → climb rank → unlock cosmetics → new season
```

**Every one of these loops must independently feel good and have a clear reward**, because
different players live at different timescales. The Killer lives in the seconds loop; the
Achiever lives in the weeks loop. If any loop is unrewarding, you lose that player segment.

- **Seconds (combat):** must feel responsive and readable. See §13 (Feel).
- **Minutes (session):** every few minutes a player should get *something* — a level, a drop,
  a kill, Blood Money. This is why the global loot table splits into a frequent supply roll
  (see companion doc §2). Dead air kills sessions.
- **Hours (tournament):** the three-act structure (§5) gives shape. There's always a "now"
  goal and an "endgame" goal.
- **Weeks (season):** the persistent meta (ranks, cosmetics, leaderboards) gives the reason
  to log in on a Tuesday. Without this loop, players churn after the novelty fades.

### 5. The Three Acts (the shape of every tournament)

Every tournament is a three-act play. This is the master structure all pacing flows from.

| Act | Runtime | Player mindset | Server's job |
|-----|---------|----------------|--------------|
| **I — The Rush** | first ~33% | "Gear up, avoid death, get viable" | Fast XP, abundant supplies, cheap mid-gear, low-stakes events |
| **II — The War** | middle ~40% | "Fight, farm events, build a lead" | Frequent events, Blood Money sinks, escalating loot, rivalries form |
| **III — The Squeeze** | final ~27% | "Survive, contest BiS, reach finals" | Rare BiS events, shrinking safe zones, gas, the finals build-up |

The **golden constant**: an average player reaches **combat viability (~70–80 combat stats +
a full mid setup) at the ~33% mark**, no matter the tournament length. `XP_RATE` is the dial
that protects this. Hit it early → Act II drags and snowballs. Hit it late → the tournament
ends before the fun. (Exact rates per length: companion doc §1.)

**Escalation is the secret sauce.** Events, danger, and rewards should all ramp with the
runtime percentage (your code already computes `Main.getRuntimePercentage()`). Act I events
drop supplies; Act III events drop godswords in forced-PvP zones. The tournament should feel
like it's *building to something*, pulling everyone toward a climactic finals.

### 6. First Session — the most important 10 minutes you'll ever design

**A new player decides whether to stay in their first session.** Most PvP servers fail here:
they drop a confused new player into a meat grinder and never see them again. We will not.

The first-session experience must answer three questions *fast and without a wiki*:

1. **"What do I do right now?"** — On first login (and after every reset), the player should
   get a single, clear next step. Not a wall of text. One objective: *"Head to the overworld,
   train on these NPCs, come back geared."* Your `StarterKit` and sigil-pick are the opening
   beats — make them feel like a *choice*, not a chore.
2. **"Am I making progress?"** — Constant small wins in the Rush act. Levels, supply drops,
   Blood Money. The new player must *feel* themselves getting stronger within minutes.
3. **"Was that death fair?"** — When they die (they will), the death screen / message should
   make it clear *why*, and that *they'll be back in the next tournament as an equal.* Reframe
   death from "I lost everything" to "round over, next one starts soon." The reset is our
   gift — make sure new players understand that, or they'll read it as the cruelty of other
   servers.

> **Onboarding principle:** A brand-new player and a 500-hour veteran start the *exact same
> way* every tournament. That's our superpower — lean into it. The new player is *never*
> permanently behind. Make sure they *know* that within their first ten minutes.

A short, optional, skippable **"first tournament" tutorial overlay** (where to train, how
bank keys work, what the gas does, how to read the event timer) pays for itself many times
over in retention. Veterans skip it; newbies get rescued.

### 7. Progression & Identity (builds, mastery, expression)

Because gear resets, the *durable* progression is **knowledge and skill**, and the *in-
tournament* progression is **your build**. Both are identity.

- **The Sigil system is your build-crafting layer.** The opening combat-sigil choice (Ranger
  / Fighter / Mage) sets a player's combat identity for the tournament; the skilling/utility
  sigils earned in-run let them specialize. **Design rule:** the three opening sigils must be
  **balanced to an equal power budget** (it's the player's first commitment — no trap picks),
  and no single later sigil should be a mandatory pick. Variety of viable builds = replay value.
- **Mastery is the real meta-progression.** A veteran's edge is *knowing* the optimal Rush
  route, the event timings, the gas patterns, the PvP mechanics — not having better gear.
  This is the healthiest possible progression for a fair PvP game: it's earned, it can't be
  bought, and it transfers to every future tournament. **Lean into rewarding knowledge.**
- **Identity should be *visible*.** Cosmetics, titles, and rank (the persistent meta) let a
  player wear their history. The Killer wants their kill-count title seen; the Achiever wants
  their #1 rank seen. Make prestige loud and public — it's free retention.

### 8. Risk & Reward (the soul of Deadman)

This is the system that separates Dead Men from a normal server. Get it right and everything
else has weight; get it wrong and it's a themepark.

- **Banking should be a meaningful decision, not a free save.** The bank-key mechanic (your
  `BankKey` / `DMMConst` keys) is gold: to protect loot you must *do something risky or
  costly*. Never let banking be frictionless. The tension of "do I bank now or push for one
  more kill?" is the product.
- **Carrying risk should be visible and rewarded.** A player hauling loot/Blood Money is a
  walking treasure — others should be able to *see* the opportunity (bounties, skull
  mechanics) and the carrier should earn more for the danger. Risk you can't see isn't
  exciting; risk everyone can see creates hunts, rivalries, and stories.
- **Death must transfer value, not just delete it.** When a player dies, their risked loot
  should (mostly) go to the killer. This is the economic engine of the War act — it's how
  Blood Money and gear *flow* toward skilled players. A death that just vaporizes loot
  feels bad for everyone; a death that *funds the killer* drives the whole loop.
- **Safe zones are a pacing tool, not a hiding spot.** The Citadel / safe zones let new and
  resetting players breathe. But the gas/`StaticGas` mutator and shrinking zones in Act III
  must make *permanent safety impossible*. You can rest, but you can't camp to victory.

### 9. Social Systems (the glue)

The cheapest, highest-leverage retention you can build. People stay for people.

- **Teams (Duo/Trio)** are already in your config. Make teaming *frictionless* — easy to form
  in the lobby, clear shared UI, shared event rewards. A player with a regular duo partner
  is a player who logs in to *see their friend*, not just to play.
- **Rivalries are content.** Surface "Player X killed you" / "You've died to X three times"
  prompts. Let grudges form. A nemesis is a reason to come back.
- **Clans / persistent groups** (longer term) give the Socializer a home and the Achiever a
  team leaderboard to climb. Even a lightweight clan tag is powerful.
- **Shared spectacle.** The finals should be *spectatable* (you already have a viewing orb).
  Dead players watching the finals, cheering their team, is community-building for free. Make
  the finals a show.

---

## Part III — Keeping It Alive

### 10. Content & Variety (manufacturing stories)

We don't hand-author stories; we build the *conditions* for them and let players write the
drama. Three engines do this:

- **Mutators** (one per tournament) change *how you play*. Rotate ~6–8 so no two tournaments
  feel the same. (List & rules: companion doc §5.) **Rule:** a mutator may change the *style*
  of optimal play, never *who is allowed to win.* Avoid anything that hard-locks a combat
  style out of the whole game.
- **Events** (timed world hotspots) change *where the action is*. Your Breach / Gold Cart /
  Static Chest plus zone-control and mass-loot beats give the map a rhythm and force players
  to collide. Escalate richness/danger with runtime %.
- **Formats** (length + team size + rates) change the *texture* of a session. A 1h Blitz solo
  and an 8h Endurance trio are different games. A varied weekly schedule serves different
  player moods and timezones.

**The variety mandate:** a returning player should be able to ask "what's the tournament
*right now*?" and get a different answer than yesterday. Sameness is the slow death of any
recurring-session game.

### 11. The Season — the meta-game that beats churn

The tournament loop is hours; the **season loop is weeks**, and it's what turns a fun
weekend toy into a server people return to for months.

- **A season is a series of tournaments with a persistent leaderboard.** Players earn
  **Tournament Points** (placement-based, persistent — companion doc §3B) that buy
  **cosmetics, titles, and rank.** None of it buys power (Pillar 1).
- **Seasons reset the leaderboard** (e.g. monthly or 6-weekly), giving everyone a fresh
  climb and a recurring "new season hype" beat. Old cosmetics/titles stay earned; the *race*
  restarts. This is the proven model (every successful competitive game runs seasons) because
  it manufactures a fresh start *and* preserves earned prestige simultaneously.
- **Seasonal exclusives** (a unique cosmetic / title only available this season) give urgency
  without ever touching power. "Play this season or miss the Vampire Lord cape forever."
- **The leaderboard is the carrot.** Top-N ranks, visible titles, maybe a seasonal champion
  crown. The Achiever's entire reason to exist lives here. Make it prominent and prestigious.

> **Why this matters:** Without a season loop, Dead Men is a fun thing people try and drift
> from. With it, there's always a reason to log in *this week* — a rank to defend, a cosmetic
> to chase, a season ending soon. This is the difference between a 3-week server and a 3-year
> server.

### 12. Fairness & Integrity (the invisible foundation)

A PvP server lives or dies on *trust*. If players believe the game is fair, they forgive a
lot. If they suspect cheating, RWT, or favoritism, the community evaporates overnight. This
is not optional infrastructure — it's a core feature.

- **Anti-cheat is a Pillar-1 issue.** Botting/cheating is "buying power" by another name.
  Invest in detection. A single tournament won by a bot, publicly, can poison the well.
- **No staff power in tournaments.** Admins/mods must compete on equal footing or not at all.
  Visible staff advantage is the fastest way to kill a community's trust. Spawning gear,
  god-mode, or seeing positions = death. Staff play in a separate flagged capacity or as
  equals, full stop.
- **Anti-RWT by design.** Because power resets and can't be bought with real money (Pillar 1),
  the *incentive* to buy gold/accounts is already low — that's a structural gift of the
  Deadman model. Protect it: don't add any persistent tradeable power that RWT could target.
- **Anti-toxicity.** PvP communities skew hostile. A clear, enforced code of conduct, easy
  reporting, and swift action on harassment keep the Socializers (your glue) from fleeing.
  Toxicity is a retention bug, treat it like one.
- **Transparency builds trust.** Announce rule changes, explain balance decisions, publish
  patch notes. A community that understands *why* you changed something forgives the change.
  A community blindsided by a silent nerf revolts.

### 13. Feel & Juice (why it's fun second-to-second)

All the systems above are scaffolding for the moment a player lands a kill. That moment has
to *feel* incredible, or none of the rest matters.

- **Combat must be readable and responsive.** Clear hit feedback, obvious prayer/protection
  states, no input ambiguity. A player who loses must understand *what* killed them within
  the second it happened. Unreadable death = "this game is unfair" = churn.
- **Feedback for everything.** Levels, drops, kills, Blood Money, bounty changes — every
  reward needs visible/audible punch. The supply-roll loot split (companion §2) exists partly
  for *feel*: frequent small dings keep the session alive. Dead air is the enemy of fun.
- **The clock is always present.** Players should always know what act they're in and how long
  until the next phase/event. Tension requires a visible timer. You already surface event/
  finals countdowns — make them prominent.
- **Spectacle at the peaks.** Event spawns, the gas closing, the finals beginning — these
  should be *loud*: server-wide announcements, visual drama, a sense of occasion. The peaks
  are what players remember and screenshot.

---

## Part IV — Governing the Project

### 14. The Decision Framework (how to evaluate any future feature)

When you're considering *anything* new, run this checklist. It's the whole Bible compressed
into a tool you can use in 60 seconds.

```
1. WHICH PILLAR does this serve?     (If none → cut it.)
2. WHICH PLAYER TYPE does it feed?   (Killer / Achiever / Explorer / Socializer)
3. WHICH LOOP does it improve?       (Seconds / Minutes / Hours / Weeks)
4. DOES IT BREAK PILLAR 1?           (Persistent or purchasable power → hard NO.)
5. DOES IT ADD CLARITY OR CONFUSION? (Respect the player's time — Pillar 5.)
6. CAN WE TUNE IT WITH ONE DIAL?     (If it needs five knobs to balance, simplify it.)
7. WHAT STORY does it let players tell? (Variety/spectacle — does it create moments?)
```

A strong feature serves a clear pillar, feeds a player type, improves a loop, respects
Pillar 1 absolutely, adds clarity, tunes simply, and creates stories. A weak one is "cool"
but can't answer these. **Cut the cool ones. Ship the clear ones.**

### 15. Anti-Goals (what Dead Men will deliberately NOT be)

Knowing what you *won't* build is as important as knowing what you will. These are the
temptations that have killed a hundred PvP servers:

- ❌ **A gear-treadmill.** No persistent gear progression. Resets are sacred (Pillar 1).
- ❌ **Pay-to-win, ever.** No purchasable power, no donor stats, no $ → advantage. Cosmetics
  and convenience-that-isn't-power only.
- ❌ **A spreadsheet sim.** Depth comes from *decisions under pressure*, not from 47 stats and
  a crafting tree only a wiki-reader can parse. Respect the player's time (Pillar 5).
- ❌ **A gear-check arena.** If BiS becomes common, skill stops mattering (Pillar 2 violation).
  Keep BiS rare and earned.
- ❌ **A lonely game.** If solo play is always optimal and teaming is friction, the Socializers
  leave and the community dies. Invest in social systems even when solo is simpler to build.
- ❌ **A black box.** No silent nerfs, no unexplained changes, no "trust me." Transparency is
  a feature (§12).
- ❌ **A staff playground.** No in-game staff advantage. Integrity over ego (§12).

### 16. Roadmap Philosophy (build order, not a feature list)

Build in this order, because each phase makes the next one meaningful. Don't build the
penthouse before the foundation.

1. **Phase 1 — Make one tournament *fair and fun.*** The core loop (Rush/War/Squeeze), a
   working global economy (companion §2–3), readable combat, and the first-session experience
   (§6). *Fix the live bugs in companion §8 first.* If a single tournament isn't fun, nothing
   else matters.
2. **Phase 2 — Make it *varied.*** The mutator rotation, the full event roster, the format
   menu. Now no two tournaments feel the same (Pillar 4).
3. **Phase 3 — Make it *sticky.*** The season loop — persistent points, leaderboards, ranks,
   cosmetics (§11). Now there's a reason to come back next week.
4. **Phase 4 — Make it *social.*** Clans, rivalries, richer team systems, spectator polish
   (§9). Now the community holds itself together.
5. **Phase 5 — Make it *trustworthy at scale.*** Anti-cheat, moderation tooling, transparency
   cadence (§12). Now it survives growth.

**Resist building Phase 3+ features before Phase 1 is genuinely fun.** A beautiful season
system wrapped around a boring tournament is a beautiful tomb.

### 17. Success Metrics (how we know it's working)

Watch these, in priority order. Don't optimize for vanity numbers.

1. **First-session retention** — do new players come back for a *second* tournament? (The
   single most important number. If this is low, fix §6 before anything else.)
2. **Time-to-viability** — does the average player hit combat-ready at the ~33% mark?
   (The master pacing health-check. Tune `XP_RATE` alone against this — companion §0.)
3. **Tournament completion** — do players stay through to the finals, or drift in Act II?
   (Low = the War act is dead; add event frequency / Blood Money flow.)
4. **Week-2 retention** — does the season loop bring people back after the novelty? (Tests §11.)
5. **Team adoption** — what % play in Duo/Trio? (Tests whether the social glue is forming.)

**Change one dial per measurement cycle.** The fastest way to lose control of balance is to
move five numbers at once and never learn which one mattered.

---

## Part V — Quick Reference

### The Bible in one page

- **Fantasy:** A fair, complete, high-stakes survival arena where everyone starts equal every
  few hours.
- **Pillars:** (1) Fair Resets — power never persists. (2) Skill Over Stuff — outplay beats
  out-gear. (3) Always Something at Stake — tension is the product. (4) Every Tournament Tells
  a Story — variety is retention. (5) Respect the Player's Time — frustration isn't difficulty.
- **Players:** Killer, Achiever, Explorer, Socializer — feed all four.
- **Loops:** Seconds (combat), Minutes (session), Hours (tournament), Weeks (season) — each
  must reward independently.
- **Acts:** Rush (gear) → War (fight) → Squeeze (finals). Viability at ~33%.
- **Meta:** Only cosmetics + rank persist. Seasons reset the race, keep the prestige.
- **Anti-goals:** No persistent power, no pay-to-win, no gear-check, no black box, no staff
  edge, no lonely grind.
- **When unsure:** run the §14 checklist. Serve a pillar, feed a player, improve a loop,
  never break Pillar 1.

### Companion documents
- [`Tournament-Design.md`](./Tournament-Design.md) — tuned numbers: loot tables, rates, prices,
  runtimes, event tiers, and the list of live bugs to fix first.

---

*This Bible is meant to be argued with and revised — but only deliberately, and only in
writing. When you change a Pillar, you're changing what Dead Men* is. *Do it on purpose.*
