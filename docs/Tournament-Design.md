# Dead Men — Tournament Design Bible

> A practical, numbers-filled balance guide for the tournament-style Deadman server.
> Every number here is a **starting point you can tune**, not gospel. The goal is to get
> you off a blank page with values that are internally consistent, so you can nudge one
> dial at a time instead of guessing at everything at once.
>
> **Companion documents:** [`Game-Design-Bible.md`](./Game-Design-Bible.md) holds the *why*
> behind every number here — read its §3a before touching §3C below, since that's the section
> that defines exactly how far Overworld Points are allowed to reach into a tournament.
> [`Overworld-Prestige-System.md`](./Overworld-Prestige-System.md) and
> [`Overworld-Points-Citadel-Shops.md`](./Overworld-Points-Citadel-Shops.md) cover the Overworld
> side of the economy this document now bridges into.

**Working assumptions** (tell me if any are wrong and the numbers shift):
- Population: **Small–Medium (5–80 online)**. Scaling notes included for larger.
- Carryover: **Cosmetics + leaderboard rank persist; gear/stats always wipe** (true Deadman) —
  **with one deliberate exception:** Overworld Points persist and buy bounded, sub-BiS gear and
  Sigils at the Citadel (§3C). Everything else about the fresh start is untouched.
- Gearing: **Balanced** — supplies & mid-gear from drops/shops/Citadel, BiS from events & PvP
  only, never from Overworld Points.

---

## 0. The one mental model that prevents "unbalancing everything"

A fresh-wipe tournament has **three acts**. Tune everything against where a player sits in
these acts, and balance stops feeling like a black box:

| Act | % of runtime | What players do | What the server should feed them |
|-----|-------------|-----------------|----------------------------------|
| **The Rush** | first ~33% | Skill up, grab starter→mid gear, avoid fights | Fast XP, abundant *supplies*, cheap mid-gear |
| **The War** | middle ~40% | PvP, contest events, bank loot with keys | Events, Blood Money sinks, mid→high gear |
| **The Squeeze** | final ~27% | Finals approach, gas closes, BiS matters | Rare BiS from events, escalating danger |

**Golden rule:** an *average* player should reach **combat viability** (~70–80 combat
stats + a full mid-tier setup) right around the **33% mark**. If they hit it sooner, the
War act drags and snowballs; later, and the tournament ends before the fun starts.
**XP_RATE is the single dial that controls this.** Everything else is secondary — and that
includes Overworld Points: they're allowed to get an OP-rich player to the Citadel with a
better shopping list, never to skip the 33% mark outright. If OP spending starts moving that
mark meaningfully, the fix is Citadel pricing (`Overworld-Points-Citadel-Shops.md §3`), not
this dial.

---

## 1. Tournament runtimes & rates

Your `TournamentConfig.Timespan` enum already offers 1h / 3h / 5h / 8h / 10h / 12h. You do
**not** want one rate for all of them — short games need blistering XP so people can fight;
long games need slow XP so progression *is* the content. Rates scale **inversely** with length.

### Recommended format menu

| Format | Timespan | XP_RATE | DROP_RATE | PET_RATE | Team size | When to run |
|--------|----------|--------:|----------:|---------:|-----------|-------------|
| **Blitz** | `ONE_HOUR` | 50 | 5 | 5 | Solo/Duo | Filler, low pop, "one more game" |
| **Standard** | `THREE_HOURS` | 25 | 3 | 3 | Solo/Trio | **Your bread & butter** |
| **Marathon** | `FIVE_HOURS` | 15 | 2 | 2 | Duo/Trio | Prime-time weekend |
| **Endurance** | `EIGHT_HOURS` | 10 | 2 | 2 | Trio | Special / seasonal |
| **Grand** | `TWELVE_HOURS` | 7 | 1 | 1 | Trio | Season finale only |

Rationale: at 50x a player reaches ~80 combat in ~20–30 min (≈33% of a 1h game). At 7x it
takes a couple of hours (≈33% of a 12h game). The "time to viability" stays ~1/3 of runtime
across the board — that's the property you're protecting.

> **Default config to ship:** make **Standard (3h)** the random default, not the current
> `QuickSolo`. The current default (`QuickSolo.java`) runs **50x XP / 5x drop** which is
> Blitz rates on an unbounded team size — that's why it feels swingy. See §8 fixes.

### Lobby / finals timing
- **Lobby: 10 min** at small pop, **15 min** at medium+ (gives latecomers time to log in & pick teams). Your code currently sets `900000` (15 min) but the chat messages say "30 minutes" — pick one and make them match (see §8). The Lobby (and Final) windows are also exactly when the game already points idle players at the Overworld ("visit the Overworld while you wait") — that messaging is doing real retention work, don't cut it during a copy pass.
- **Finals: use the `Timespan.finals` value already in the enum** (15–60 min scaling with length). That window is the gas-arena endgame.

---

## 2. The global loot table (the heart of the economy)

This is `GlobalDropManager.java`. Right now it's the single biggest balance lever **and** the
most under-built (2 items per tier, and most kills return `null`). In a survival mode where
players start with nothing, "you killed something and got nothing" 66% of the time feels
punishing and starves them of the *supplies* that actually keep them alive.

### Fix #1 — Split drops into two independent rolls

Every NPC kill should roll **two separate tables**:

1. **Supply roll** (high frequency) — food, runes, ammo, coins/Blood Money, low-level pots.
   This is what keeps players in the fight. Aim for **~70% of kills give *something* useful.**
2. **Gear roll** (low frequency) — the tiered weapon/armour table you already have.

This single change is the difference between "Deadman feels brutal and dead" and "the map
feels alive." Players should rarely feel a kill was wasted.

### Fix #2 — Reserve BiS for events, not trash mobs

The global table should top out at **mid/high gear** (rune, dragon, mystic, basic d'hide).
**Best-in-slot lives on event & boss tables only** (§4), and — as of §3C — is explicitly never
purchasable with Overworld Points either. If a max-level trash mob (or a rich shopping trip)
can hand out a godsword, PvP becomes a gear-lottery instead of a skill contest either way.

### Recommended tier contents

Keep your 4-tier structure (`LOW / MED / HIGH / ELITE`), but flesh each out and re-scope:

```
LOW    (starter→early): leather/iron/steel armour, iron/steel/black weapons,
                        air/mind/water runes, bronze/iron arrows, lobster
MED    (mid):           rune armour pieces, rune weapons, adamant gear,
                        mystic robes, chaos/death runes, adamant arrows, swordfish
HIGH   (high, capped):  dragon weapons (dds, scimitar, mace), green/blue d'hide,
                        basic battlestaves, rune/dragon arrows, shark
ELITE  (rare top of global, NOT BiS): dragon platelegs/chain, berserker/archer ring
                        (un-imbued), helm of neitiznot, fighter torso — "almost-BiS"
```

> BiS (whip, godswords, claws, ancient/imbued items, void) → **event tables only** (§4). The
> Citadel's Overworld Points shop ceiling (`Overworld-Points-Citadel-Shops.md §3`) sits *below*
> even this ELITE band on its top tier — OP gear should read as "very solid," never "almost-BiS."

### Recommended weight model

Your current weight math is sound in shape; here's a tuned version. Weights are *relative*,
so only ratios matter:

```
nothing  = 250          (was 500 — halve it; pair with the supply roll so kills feel rewarding)
low      = 120
med      = (npcLvl > 50)  ? (npcLvl - 45) : 0
high     = (npcLvl > 90)  ? (npcLvl - 85) : 0
elite    = (npcLvl > 120) ? (npcLvl - 115) : 0   # only the toughest mobs, and even then rare
```

Worked example — a **level 130** NPC: nothing 250, low 120, med 85, high 45, elite 15
→ total 515. ~49% nothing on the *gear* roll (the supply roll covers the rest). When gear
does drop: low 45%, med 32%, high 17%, **elite only ~6%**. That keeps near-BiS scarce while
low/mid gear flows freely. Drop the `elite` divisor threshold higher if elite still feels
too common at your pop.

> **Multiply the final gear-roll chance by `config.DROP_RATE`** so the per-tournament drop dial
> actually does something to global loot (right now `GlobalDropManager` ignores it).

---

## 3. Currencies & the cost economy

You now run **three** currencies, three purposes — two reset every tournament, one doesn't:

### A) Blood Money — the in-tournament sink (resets each wipe)

Earned by playing, spent on supplies & mid-gear. This is your *soft economy* — its whole job
is to give players something to chase mid-game and a reason to take fights. As of §3C below,
Blood Money is also the **anchor value** every Overworld Points tournament reward is calculated
from — every BM source in the table below now has a matching, smaller OP payout riding along
with it.

**Income (per tournament, ballpark):**
| Source | Blood Money |
|--------|------------:|
| Player kill | 1,000–3,000 (scale with victim's risk/Blood Money carried) |
| Combat task (TaskBoard) | 500–2,000 |
| Gold Cart event | 5,000–15,000 to participants |
| Breach boss | 8,000–20,000 split by damage |
| Static Chest | 3,000–10,000 to the looter |

**Costs (Blood Money shop — supplies & mid gear, NOT BiS):**
| Item | Price (BM) | Why |
|------|-----------:|-----|
| Shark | 150 | Keep food flowing |
| Prayer potion (4) | 1,000 | The real bottleneck in PvP |
| Super combat (4) | 2,500 | Premium, not free |
| Ranging / Magic potion (4) | 1,500 | |
| Death runes (each) | 40 | Feed casters |
| Rune set (per piece) | 1,500–4,000 | Mid gear is buyable… |
| Dragon weapon | 25,000–40,000 | …high gear is *expensive* |
| **Whip / BiS** | **not sold** | Earn it from events/PvP |

Design intent: a player who fights and farms events can comfortably buy **supplies + mid
gear**, but BiS is gated behind risk and events. The numbers above assume Standard (3h) income;
scale prices ~1.5× for Marathon+ where players earn more total.

### B) Tournament Points — the persistent meta (survives wipes, cosmetic-only)

This is your **retention hook** and the answer to "why grind if it all resets?" Awarded by
**final placement**, spent on **cosmetics, titles, and leaderboard rank** that carry over.
Use the existing `Tournament_Cosmetics` shop pattern (it already takes a currency).

**Placement payout (Standard tournament, scale by population):**
| Placement | Tournament Points |
|-----------|------------------:|
| 1st (winner) | 1,000 + unique seasonal title |
| 2nd–3rd | 500 |
| Top 10 | 250 |
| Reached finals | 100 |
| Participation (played the Main act) | 25 |

**Cosmetic pricing:** 2,000–10,000 points each → several tournaments per cosmetic. That
pacing is deliberate: it should take a *dedicated season* to collect a set, so the leaderboard
and cosmetics stay aspirational. Never let meta-points buy power (per your "cosmetics + rank
only" choice) — the moment they do, you've broken Deadman's fairness. Tournament Points and
Overworld Points are deliberately kept separate and non-interchangeable: one buys status, the
other buys a bounded head start. Don't let a future feature quietly merge them.

### C) Overworld Points — the tournament-to-Overworld bridge (persists, buys bounded power)

**This is new.** Overworld Points already flow from Overworld training (`Resources.java`
Hopper deposits, Task Cave combat) and spend at the Citadel on Sigils and sub-BiS gear
(`Overworld-Points-Citadel-Shops.md`). As of this revision, **tournament actions feed the same
currency**, so time spent fighting and completing events *inside* a tournament also pays into
a player's persistent Overworld economy — not just time spent in the Overworld itself.

**The mechanic:** every Blood Money reward in §3A also pays out Overworld Points, calculated as
a **percentage of that same Blood Money reward**. This is deliberate — it means the OP bridge
automatically scales with format, population, and event tier exactly the way Blood Money
already does, with no separate tuning table to maintain. One dial (the percentage), not five.

| Action | OP conversion rate | Reasoning |
|--------|---:|---|
| **Player kill** | **5%** of BM earned | PvP is the highest-skill, highest-risk action — rewards the Killer archetype directly for engaging, not just surviving |
| **Combat task** (TaskBoard) | **4%** of BM earned | More PvE/passive than a PvP kill, so it sits slightly below it |
| **Event completion** (Gold Cart, Breach, Static Chest, King of the Hill, etc.) | **6%** of BM earned | Events take coordination and higher effort/risk — the best rate in the table, and it makes chasing events pay into *two* economies at once (Pillar 4 spectacle + the bridge) |

Round down to the nearest whole point; floor of **1 OP per qualifying action** so a tiny reward
never rounds to zero and feels like it didn't count.

**Placement bonus (paid once, at tournament end, on top of the above):** mirrors the
Tournament Points table in §3B exactly in structure, at a fraction of the value — this is the
"even a rough combat tournament still pays into your Overworld economy if you finished well"
safety net:

| Placement | Overworld Points bonus |
|-----------|------------------------:|
| 1st (winner) | 500 |
| 2nd–3rd | 250 |
| Top 10 | 100 |
| Reached finals | 50 |
| Participation (played the Main act) | 10 |

**Worked example (Standard, 3h, an unusually dominant run):** ~10 kills (≈15,000 BM → 750 OP),
2 combat tasks (≈2,500 BM → 100 OP), one Gold Cart (≈10,000 BM → 600 OP), one Breach
(≈14,000 BM → 840 OP), 1st place finish (+500 OP flat) → **roughly 2,800 OP for an exceptional
3-hour tournament.** Compare that to `Overworld-Points-Citadel-Shops.md §1`'s own estimate of
"low thousands of points per hour" from dedicated endgame Overworld grinding — a dominant
3-hour tournament run earns *less* than one hour of focused Overworld play. That gap is the
whole point (see the Bridge Ratio, `Game-Design-Bible.md §3a`) — tournaments should always feel
like a nice bonus stream on top of Overworld grinding, never a faster way to farm the same
currency. **If actual playtesting shows tournaments out-earning the Overworld itself, cut these
three percentages, don't touch the Overworld's own rates.**

**Explicit non-stacking rule:** Prestige's Overworld-earn-rate bonuses (the capped +15% "more
OP from Hopper deposits / Task Cave kills" rewards in `Overworld-Prestige-System.md §6`) apply
**only to OP earned inside the Overworld itself** — never to the tournament-action conversions
in this section. Keeping these two income streams independently balanced is what keeps the
Bridge Ratio meaningful; letting Prestige bonuses silently compound the tournament-side rate
would blur a boundary that was deliberately kept sharp on both sides.

**Non-negotiable, restated from the Bible:** none of this OP income ever converts into anything
above the Citadel's sub-BiS ceiling, and none of it changes what a player starts a tournament
with. It only changes what they can afford to buy once they're back at the Citadel next time.

---

## 4. Event reward tiers (fix the broken ladder)

`EventRewards.java` currently has **MED and HIGH tiers that are byte-for-byte identical**
(both = void sets + a mislabeled rune defender). HIGH must be a strict upgrade or the ladder
is meaningless. Suggested re-scope so each tier clearly beats the last:

| Tier | Theme | Example contents |
|------|-------|------------------|
| **LOW** | Full mid setup | rune armour set, rune weapons, mystic robes *(your current LOW is fine)* |
| **MED** | Power spikes | void sets, fighter torso, berserker/archer ring (un-imbued), dragon defender, dragon boots |
| **HIGH** | Near-BiS | whip, dragon crossbow, trident (uncharged), occult, imbued rings, dragon warhammer |
| **GOD** | True BiS / chase | godswords, dragon claws, ancient (Vesta/Statius/Morrigan/Zuriel) sets, kodai, sang, ACB, ghrazi rapier *(your current GOD list is great here)* |

Which tier an event rolls should depend on **how contested / late it is**: early Gold Carts
roll LOW–MED; a late-game Breach in the Squeeze act rolls HIGH–GOD. Tie the tier to
`Main.getRuntimePercentage()` (you already compute it) — the same signal the Citadel's own
runtime-gated catalog tiers use (`Overworld-Points-Citadel-Shops.md §2`), so both systems
escalate on the same curve.

Every event on this table also pays the **6% Overworld Points event rate** from §3C on top of
its Blood Money reward — a GOD-tier Breach kill is now valuable in three currencies at once
(the drop itself, Blood Money, and Overworld Points), which is exactly the kind of moment
worth making loud (`Game-Design-Bible.md §13`).

---

## 5. Fun mechanics to fill gameplay

Your engine already supports the two best tools for variety — **Mutators** (per-tournament
rule changes) and **Events** (timed world hotspots). Lean on these instead of inventing new
systems; they're cheap content multipliers.

### Mutators (one active per tournament — `config.MUTATOR`)
You have `VampiricRites` and `StaticGas`. A rotation of ~6–8 keeps every tournament feeling
different even with identical maps:

| Mutator | Effect | Feeling |
|---------|--------|---------|
| **Vampiric Rites** *(have it)* | Hits heal a % of damage | Aggressive, melee-favoured |
| **Static Gas** *(have it)* | Shrinking safe area / creeping gas | Forces fights, prevents camping |
| **Double Drops** | DROP_RATE ×2 | Loot rush, faster gearing |
| **Glass Cannon** | +25% damage dealt *and taken* | High-stakes, fast fights |
| **Bounty Hunter** | Every player shows a visible kill bounty | Hunt the leaders |
| **Famine** | No food on global drops; must cook your own | Survival/skilling emphasis |
| **Rune Rain** | Free elemental runes flow; mage-favoured | Spellcaster meta |
| **Berserk Finale** | Last 25% of runtime: all rates ×2 | Explosive endgame |

Rule of thumb: a mutator should change *how you play*, never *who can win* — avoid anything
that hard-counters a single combat style for the whole game. **None of these should touch the
§3C Overworld Points rates** — a mutator that doubles BM income (e.g. a hypothetical "Gold
Rush") would silently double OP income too via the percentage formula, which is exactly the
kind of drift the Bridge Ratio is meant to catch. If a future mutator changes BM income, decide
explicitly whether the OP percentage should scale with it or stay fixed — don't let it happen
by accident.

### Events (timed, every ~15 min — `Main.nextEvent`)
You have **Breach** (Jad boss), **Gold Cart** (mobile loot piñata), **Static Chest**. Add a few
zone-control / mass-loot beats so the map has rhythm:

| Event | Mechanic | Why it's fun |
|-------|----------|--------------|
| **Breach** *(have it)* | PvE boss spawns in a zone; PvP allowed | Risk/reward, draws a crowd |
| **Gold Cart** *(have it)* | Slow cart of loot crosses the map; escort/raid it | Moving hotspot, no camping |
| **Static Chest** *(have it)* | High-value chest spawns at a fixed contested spot | Predictable brawl |
| **King of the Hill** | Hold a zone for N seconds → reward; contested | Team objective, great for Duo/Trio |
| **Blood Moon** | 5-min forced-PvP window, safe zones disabled | Spikes action on demand |
| **Treasure Drop** | Items rain over an area for ~60s | Chaotic free-for-all |

**Escalation:** make events richer and more dangerous as `getRuntimePercentage()` climbs.
Early events = supplies & LOW gear & small Blood Money; late events = HIGH/GOD tier & big
Blood Money, with tighter/forced PvP. This naturally pulls the lobby toward a climactic finals.
Overworld Points scale right along with this via the flat 6% event rate — late-tournament
events are already the biggest Blood Money payouts, so they're automatically the biggest OP
payouts too, with no separate escalation curve needed.

### The Final (Squeeze act)
You already have `FFA` and `Versus` finals + gas areas. Recommendation by population:
- **Small (≤25):** FFA in a gas arena. Last team standing wins.
- **Medium (25–80):** FFA, but seed the arena from the **Top N by Blood Money / kills** so the finals reward the whole tournament's play, not just survival.
- **Large (80+):** bracket/`Versus` rounds feeding a final FFA.

---

## 6. The starter kit & sigils (the opening 5 minutes)

`StarterKit.java` gives a sword/bow/staff, 12 tuna, Deadman's cape, oddskull. Good. Two notes:
- Keep the starter **deliberately weak** — it's a "get you to the first NPC," not a loadout.
  Power should come from the Rush act, not the box. This holds even for OP-rich veterans — the
  starter kit is never enhanced by Overworld Points; only the Citadel *shop* is.
- `Deadman.java` / `Main.java` make players pick a **starting combat sigil** (Ranger / Fighter
  / Mage). That's a great identity choice — make sure the three are **balanced against each
  other** (equal power budget) since it's the player's first commitment and effectively picks
  their combat style for the game. This choice stays **free, always** — it's never sold at the
  Citadel, precisely so the opening commitment every player makes is identical regardless of
  Overworld Points balance. The skilling/utility Sigils are then either earned in-tournament or
  **repurchased every tournament with Overworld Points** at the Citadel's Sigil Exchange
  (`Overworld-Points-Citadel-Shops.md §4`) — treat those as the "build crafting" layer and keep
  each roughly equal in value so there's no single must-buy that becomes a de facto toll.

---

## 7. A concrete "first season" plan (so you can just start)

If you want a turnkey starting point, run this for season 1 and adjust from data:

1. **Default format:** Standard (3h, 25x XP, 3x drop), Solo + Trio allowed.
2. **Lobby:** 15 min, message text fixed to match.
3. **Global loot:** implement the supply-roll + gear-roll split (§2), nothing-weight 250.
4. **Events:** every 15 min, tier scales with runtime %, your 3 existing events in rotation.
5. **Mutator:** rotate Vampiric Rites / Static Gas / Double Drops / Berserk Finale.
6. **Currencies:** Blood Money (sink, §3A) + Tournament Points (meta, §3B) + Overworld Points
   bridge live at launch (§3C — kill 5% / task 4% / event 6%, plus the placement bonus table).
7. **Citadel:** the runtime-gated shop tiers and Sigil Exchange
   (`Overworld-Points-Citadel-Shops.md §2–4`) should ship *alongside* the OP bridge, not before
   it — there's no point generating Overworld Points from tournament play if there's nowhere
   to spend them yet.
8. **Finals:** FFA gas arena, seeded by top Blood Money earners at medium pop.
9. **Fix the bugs in §8 first** — they're actively skewing balance right now.

Then watch **two metrics**: *when does the average player hit combat viability?* (nudge
`XP_RATE` if it's off the 33% mark) and *is the Bridge Ratio holding* — is a strong tournament
run earning less OP/hour than dedicated Overworld grinding? (nudge the §3C percentages down if
not, per `Game-Design-Bible.md §3a`). Leave everything else alone for a few seasons.

---

## 8. Bugs & inconsistencies found in the current code

These are live issues skewing balance today — worth fixing before tuning anything:

1. **`EventRewards.java` — MED == HIGH tier.** `addMedTier()` and `addHighTier()` add the
   identical 4 entries (void sets + a "Rune Defender" that actually uses void id `11665`).
   HIGH gives no upgrade. Re-scope per §4.
2. **`EventRewards.java` — duplicate/mislabeled entries.** Seers ring `(I)` and Void Melee
   Helm are each added twice in GOD; the "Rune Defender" comment doesn't match its id.
3. **`QuickSolo.java` — misleading default.** Named "Solo" but `TEAM_SIZE_MAX = TRIO`, and
   runs **50x/5x** (Blitz rates) as the *random default*. Either rename it Blitz or make
   Standard the default (§1).
4. **Lobby duration mismatch.** `Lobby.onLoad()` sets `duration = 900000` (15 min) but the
   player messages say **"30 minutes."** Pick one; make code and text agree.
5. **`GlobalDropManager` ignores `DROP_RATE`.** The per-tournament drop multiplier never
   reaches global loot. Multiply the gear-roll chance by `config.DROP_RATE`.
6. **Most kills return `null`** (nothing-weight 500 with tiny tables). Pair the §2 supply
   roll with a lower nothing-weight so the map feels alive.
7. **Roughly half the sellable Sigils have no implemented `effect()`.** Flagged in full in
   `Overworld-Points-Citadel-Shops.md §4` — don't let the Sigil Exchange sell a Stub sigil at
   full Overworld Points price; either finish its code first or hold it back from sale.
8. **The Overworld's ZMI-style rune altar has a dead "Craft-rune" action** (commented out) —
   Runecrafting can't currently be trained there at all, which blocks that skill's Prestige
   track (`Overworld-Prestige-System.md §6`) independent of anything in this document.

---

## 9. Tuning cheat-sheet (which dial does what)

| You want… | Turn this | Direction |
|-----------|-----------|-----------|
| Players gear up faster | `XP_RATE` ↑ (primary), `DROP_RATE` ↑ | up |
| More/longer gearing before fights | `XP_RATE` ↓ | down |
| More loot on the ground | `DROP_RATE` ↑, nothing-weight ↓ | — |
| BiS feels too common | event tier thresholds ↑, elite weight ↓ | — |
| Mid-game feels dead | event frequency ↑, Blood Money income ↑ | — |
| Economy inflating | Blood Money shop prices ↑, income ↓ | — |
| Tournaments feel samey | rotate **Mutators** | — |
| Finals are anticlimactic | seed arena by performance, escalate late events | — |
| Tournaments out-earn the Overworld itself (Bridge Ratio inverted) | §3C's kill/task/event **percentages** ↓ | down |
| Overworld feels disconnected from tournaments / bridge feels pointless | §3C's percentages ↑, *or* Citadel Tier 0 pricing ↓ | up |
| OP-funded players reach viability before the 33% mark | Citadel Tier 0/1 prices ↑ (`Overworld-Points-Citadel-Shops.md §3`) | up |

> **Change one dial per season and observe.** The fastest way to "unbalance everything" is to
> move five numbers at once and not know which one did what. This now includes the §3C
> percentages — they're a single dial each, treat them with the same discipline as `XP_RATE`.
