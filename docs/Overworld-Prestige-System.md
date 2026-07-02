# The Overworld — Prestige System

> **Status:** Living document. Companion to [`Game-Design-Bible.md`](./Game-Design-Bible.md)
> and [`Tournament-Design.md`](./Tournament-Design.md). Where this document is silent, the
> Bible's pillars govern. Where it conflicts with the Bible, the Bible wins and this document
> is wrong until fixed.
>
> **Scope:** This document covers *only* The Overworld's Prestige System. It does not touch
> tournament balance, loot tables, or mutators — those live in the companion docs.

---

## 0. The one decision that shapes everything else

**Prestige is 100% Overworld-only. It grants zero tournament power, ever.**

This needed to be said explicitly because the current in-game NPC dialogue (the Overworld
guide, `OverworldNPCs.java`) promises the opposite: *"Prestige is a way to have your levels
scale above 99 in tournaments."* That line was written before this document existed, and it
directly violates **Pillar 1 — Fair Resets**: *"power never persists; only prestige does."*
Tournament stats reset to a clean slate every game. A permanent +1-per-prestige level bump,
even a small one, is exactly the "time buys power" failure mode Pillar 1 exists to prevent —
new players would start tournaments *mechanically* behind veterans, not just less practiced.

There's also a concrete integrity hole backing this decision up, not just a philosophical one.
The account **bank is shared** between your Overworld profile and your tournament profile —
it isn't part of the swap (`Citadel.swapOverworld()` swaps stats, inventory, and equipment,
but never touches `p.getBank()`). Tournament resets normally clear that shared bank
(`Deadman.resetPlayer()` calls `p.getBank().clear()`), but only when the reset fires while
the player is *outside* the Overworld — the reset branch that runs while a player is standing
in the Overworld skips the bank clear entirely. That means a player who times a tournament
reset while parked in the Overworld currently keeps their bank intact when everyone else's
wipes. That's a pre-existing bug, not something this system introduces — but it means *any*
prestige reward that becomes a bankable item is one exploit away from crossing into
tournaments undetected. The fix isn't "patch the bug and hope" — it's "don't give prestige
rewards a form that can survive in the bank." Every reward below is a **flag, a stat, a
cosmetic override, or a currency balance on the player object** — never a physical item.

One reward *does* deliberately show up in tournaments: the account-wide capstone for
completing every skill's Prestige 10 in a single season (§4). That's allowed, on purpose —
it's a pure cosmetic/title flex with no mechanical effect, which is exactly what Pillar 1
carves out as safe to persist. Power stays in the Overworld. Reputation is allowed to travel.

---

## 1. What The Overworld actually is (grounding)

The Overworld is a second, persistent character slot living on its own island (Miscellanea),
reached through the portal object at the Citadel (Falador Castle home). Stepping through swaps
your live stats, inventory, and equipment out for a second, independently-progressing set held
in `skillHolder` / `inventoryHolder` / `equipmentHolder` — stepping back swaps them back. It
is **not PvP** (`Overworld.allowAttack()` always returns false) and it is **not reset by
tournaments** — it has its own clock, running out at the end of each **season**, independent
of how many tournaments have run in between.

The Overworld has its own economy: **Overworld Points**, earned by depositing gathered
resources into the Hopper or completing Wise Old Man combat tasks in the Task Cave, currently
spent on a persistent tool-tier ladder (`OverworldTools.Tier`: Bronze → Iron → Steel → Mithril
→ Adamant → Rune → Dragon → Infernal, per tool). This is the account's *season-long grind*, and
Prestige is this document's answer to "what do I do once I've already hit 99 in a skill and the
season isn't over yet?"

---

## 2. How prestiging works

The mechanic is deliberately the Call of Duty model, because it's a model players already
understand instinctively:

1. Take any of the 23 skills to **level 99** in the Overworld.
2. Right-click the skill in your Overworld skill tab → **Prestige**. (This confirm action
   doesn't exist yet — see §5 implementation notes. The design intent, per the existing NPC
   dialogue, is exactly this interaction; only the "+1 level in tournaments" promise is cut.)
3. The skill's XP and level reset to **level 1 / 0 XP**. Nothing else about your account changes.
4. Your **Prestige Tier** for that skill increases by one (`player.prestigeLevel[skill]++` —
   this field already exists on `Player`, has existed unused since before this document, and
   was clearly built for exactly this purpose).
5. The reward for the new tier is **granted immediately and automatically** — no currency
   cost, no claim step. You keep grinding.
6. Repeat up to **Prestige Tier 10** per skill, the hard cap.

Levels never exceed 99 at any point — prestiging *is* the reset, not a level-cap raise. This
sidesteps a real technical constraint too: the level clamp in `Stat.java` is hardcoded to 99
and shared by both profiles. Not raising it means zero risk of that clamp ever needing to be
special-cased per-profile, which is one less place this system could leak into tournaments.

---

## 3. The two reward layers

Every skill's ten prestige tiers are **season-scoped** — they're perks, cosmetics, and economy
bonuses that exist because you're actively playing that season, and they wipe along with
everything else when the season resets. That's intentional: Prestige is meant to be a
season-long carrot, not a permanent power fantasy.

But hitting **Prestige 10** in a skill, and especially hitting **Prestige 10 in all 23
skills**, is a genuine achievement that deserves to outlive the season. So there's a second,
much smaller layer that survives the wipe:

| Layer | Scope | Resets? | Examples |
|---|---|---|---|
| **Tier rewards (1–10, per skill)** | The 230 rewards in §6 | Yes, every season | Titles, cosmetics, XP/economy perks, tool tiers, QoL |
| **Hall of Fame flex** | Per skill, awarded at that skill's Tier 10 | **No — permanent** | A permanent chat/skill-tab marker proving you *once* hit max prestige in that skill |
| **Grand Prestige** | Account-wide, awarded once per season for maxing all 23 skills to Tier 10 in that season | **No — permanent**, one per season achieved | A unique, dated title + cosmetic aura, visible in the Overworld *and* in tournaments, plus a server-wide broadcast |

The Grand Prestige reward is the one deliberate exception to "Overworld-only." It's cosmetic
only — a title, a name-color aura, nothing mechanical — which is precisely what Pillar 1
permits to carry over. It should feel *enormous* to earn (230 individual prestige tiers, one
season) and be unmistakable when someone has it. See §4 for the full design.

---

## 4. Permanent rewards (survive season reset)

### 4.1 Per-skill Hall of Fame (23 of these, one per skill)

Awarded the moment a skill hits Prestige 10. Persists forever, independent of that skill's
prestige tier resetting next season (you keep the flex even after the season wipes your
levels back to 1). Design:

- A **permanent chat icon** next to your name, specific to that skill (e.g. a small crossed-
  pickaxes icon for Mining), visible **only in the Overworld** — small, tasteful, a badge for
  regulars, not a tournament flex.
- The skill's entry in your **Overworld skill tab permanently displays a gold border**, even
  after the skill resets to level 1 next season. It's a record of "I have done this," not a
  reflection of current standing.
- Counts toward a `/prestigelog` style lifetime stat other players can check — total number of
  Hall of Fame skills across all seasons played, for players who want to flex history.

### 4.2 Grand Prestige — the season's ultimate goal

Awarded once per player per season, the moment all 23 skills simultaneously hold Prestige 10
*within that same season* (i.e., achieved without waiting for a skill to un-prestige — since
nothing un-prestiges, this really just means "reach it 23 times before the season ends").

- **Title:** `Prestige Master` (unique, cannot be earned any other way), permanently unlocked
  and equippable in the title system (`Title.java` — same predicate-gated pattern already used
  for e.g. tournament win titles).
- **Cosmetic aura:** a subtle gold particle/name-glow effect, visible **everywhere**, including
  tournaments and the Citadel lobby. Cosmetic only — no stat, no gear, no XP effect. This is
  the single exception carved out in §0, and it's carved out on purpose: Pillar 1 explicitly
  protects cosmetic/reputational carryover, and "this player did something enormous" is exactly
  the kind of story Pillar 4 wants told.
- **Server-wide broadcast** the moment it happens (matches the existing pattern used for e.g.
  Staff Bounty kills) — `"<name> has achieved GRAND PRESTIGE — every skill maxed to Prestige
  10 this season. All hail the Prestige Master."` This is a rare, loud, generate-conversation
  moment, not a quiet flag flip.
- **Dated**, and stackable across seasons — a player who achieves it in three separate seasons
  should be able to show that (e.g. `Prestige Master III`), since doing it again next season is
  real, repeatable effort, not a one-time unlock.

---

## 5. Reward design framework

To keep 230 individual rewards internally fair — no skill's prestige track quietly worth more
than another's — every skill follows the same **shape**, even though every reward within that
shape is unique to the skill (per your call: 23 fully bespoke tracks, not a shared ladder).

| Tiers | Category | Power level |
|---|---|---|
| 1–2 | **Flavor** — title prefix, skill-tab icon recolor | Cosmetic only, no mechanical effect |
| 3 | **Economy I** | +3% Overworld Points from that skill's primary earn method |
| 4 | **Flavor** — unique emote | Cosmetic only |
| 5 | **QoL I** | A meaningful but bounded convenience perk, unique per skill |
| 6 | **Economy II** | +5% (cumulative +8%) Overworld Points |
| 7 | **Flavor** — unique cosmetic (outfit piece, tool skin, or aura stage) | Cosmetic only |
| 8 | **QoL II** | A stronger convenience perk, unique per skill |
| 9 | **Economy III** | +7% (cumulative **+15% cap**) Overworld Points — this is the ceiling, no skill exceeds it |
| 10 | **Capstone** | Full title + full cosmetic set + triggers the Hall of Fame flex (§4.1) |

**Why the +15% cap:** Overworld Points already has a spending sink (tool tiers) that's meant to
feel like a real grind. Uncapped prestige stacking across multiple maxed skills would let a
dedicated player trivialize that sink, which just shifts the "time buys power" problem from
tournaments (banned) into the Overworld's own economy (still bad pacing, even if power stays
contained). +15% per skill, non-stacking-into-infinity, keeps prestige a *nice accelerant*, not
a replacement for the grind it's supposed to reward you for finishing.

**Two skills get a variant shape:** Mining, Fishing, and Woodcutting each already have a
persistent tool-tier ladder (`OverworldTools.Tier`, 8 rungs: Bronze is the free starting tier,
then Iron→Infernal cost Overworld Points to buy). For these three skills, prestige tiers 1–7
**grant the next tool tier for free** instead of a generic QoL slot — reaching Prestige 7 in
Mining means you've been handed a free Infernal pickaxe upgrade rather than having to save
for it. Tiers 8–10 follow the normal Economy/Flavor/Capstone shape. This reuses an existing,
already-balanced system instead of inventing a parallel one, and it's a *very* tangible reward
that ties directly into the leveling you just did.

**Reward types used, and what each needs to be buildable** (see §7 for the full build list):
- **Titles** — existing system (`Title.java`), zero new infrastructure.
- **Economy % bonuses** — new field needed (a small per-skill bonus multiplier feeding into
  `Resources`/task point payout), simple.
- **Tool tier grants** — existing system (`OverworldTools.Tier`), zero new infrastructure.
- **Cosmetics (icons, auras, outfit pieces, chat icons)** — **no cosmetic-override system
  currently exists** in the codebase. This is the single biggest build dependency in the whole
  document; see §7.
- **QoL perks** — mostly small, targeted logic changes near existing systems (Task Cave, Hopper,
  patches); flagged individually where they touch code that doesn't exist yet (e.g. the
  Runecrafting altar).

---

## 6. Per-skill reward tables

### Combat skills (7)

Combat skills train in the Overworld's **Task Cave**, assigned by the Wise Old Man
(`CombatTask` system). Economy bonuses for this group apply to **Overworld Points earned from
completed Task Cave assignments**, and the escrow mechanic (`overworldPointsVault`, forfeited
if you die mid-task) is the natural hook for this group's QoL rewards.

#### Attack — *"The Keen Eye"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Keen-Eyed" | Cosmetic title prefix. |
| 2 | Bronze Precision Aura | Skill-tab icon gains a subtle glow. |
| 3 | Focused Strikes I | +3% Overworld Points from Task Cave kills. |
| 4 | "Feint" emote | Unique taunt/feint animation, usable anywhere. |
| 5 | Weapon Familiarity | Weapon-switch delay while task-caving reduced by 1 tick. |
| 6 | Focused Strikes II | +5% (cumulative +8%) Overworld Points from Task Cave kills. |
| 7 | Duelist's Wrappings | Cosmetic hand-wrap override with a faint weapon-trail effect. |
| 8 | Practiced Guard | 10% reduced chance of losing your escrowed task points if you die mid-task. |
| 9 | Focused Strikes III | +7% (cumulative **+15% cap**) Overworld Points from Task Cave kills. |
| 10 | **"Blademaster" (Capstone)** | Gold weapon-trail cosmetic, title suffix "the Blademaster", triggers Attack Hall of Fame. |

#### Strength — *"The Iron Grip"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Sturdy" | Cosmetic title prefix. |
| 2 | Bronze Might Aura | Skill-tab icon gains a subtle glow. |
| 3 | Heavy Hands I | +3% Overworld Points from Task Cave kills. |
| 4 | "Flex" emote | Unique celebratory animation. |
| 5 | Reinforced Grip | Task Cave weapon special-attack energy regenerates 10% faster. |
| 6 | Heavy Hands II | +5% (cumulative +8%) Overworld Points from Task Cave kills. |
| 7 | Strongman's Wraps | Cosmetic forearm-wrap override. |
| 8 | Iron Resolve | 10% reduced escrowed task-point loss on death. |
| 9 | Heavy Hands III | +7% (cumulative **+15% cap**) Overworld Points from Task Cave kills. |
| 10 | **"Ironback" (Capstone)** | Molten-effect weapon trail, title suffix "the Ironback", triggers Strength Hall of Fame. |

#### Defence — *"The Unbroken Wall"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Steadfast" | Cosmetic title prefix. |
| 2 | Bronze Ward Aura | Skill-tab icon gains a subtle glow. |
| 3 | Braced Stance I | +3% Overworld Points from Task Cave kills. |
| 4 | "Shield Bash" emote | Unique defensive taunt animation. |
| 5 | Steady Footing | Immune to the first knockback/stun effect per Task Cave assignment. |
| 6 | Braced Stance II | +5% (cumulative +8%) Overworld Points from Task Cave kills. |
| 7 | Bulwark's Cloak | Cosmetic cape-back override, stone-textured. |
| 8 | Unshaken | 15% reduced escrowed task-point loss on death (Defence's QoL runs slightly
higher than other combat skills — thematically appropriate, still inside the shared power band since it trades off against no separate emote-tier bonus). |
| 9 | Braced Stance III | +7% (cumulative **+15% cap**) Overworld Points from Task Cave kills. |
| 10 | **"The Unbroken" (Capstone)** | Stone-plate cosmetic aura, title suffix "the Unbroken", triggers Defence Hall of Fame. |

#### Hitpoints — *"The Survivor"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Resilient" | Cosmetic title prefix. |
| 2 | Bronze Vitality Aura | Skill-tab icon gains a subtle glow. |
| 3 | Vital Spark I | +3% Overworld Points from Task Cave kills. |
| 4 | "Second Wind" emote | Unique recovery-themed animation. |
| 5 | Deep Reserves | Task Cave food/potion eat-delay reduced by 1 tick. |
| 6 | Vital Spark II | +5% (cumulative +8%) Overworld Points from Task Cave kills. |
| 7 | Survivor's Bandana | Cosmetic head-slot override. |
| 8 | Grim Determination | 10% reduced escrowed task-point loss on death. |
| 9 | Vital Spark III | +7% (cumulative **+15% cap**) Overworld Points from Task Cave kills. |
| 10 | **"Undying" (Capstone)** | Crimson vitality aura, title suffix "the Undying", triggers Hitpoints Hall of Fame. |

#### Ranged — *"The Dead Shot"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Sharp-Eyed" | Cosmetic title prefix. |
| 2 | Bronze Marksman Aura | Skill-tab icon gains a subtle glow. |
| 3 | Steady Aim I | +3% Overworld Points from Task Cave kills. |
| 4 | "Trick Shot" emote | Unique bow-flourish animation. |
| 5 | Quick Quiver | Ammo-strain/reload delay in the Task Cave reduced by 1 tick. |
| 6 | Steady Aim II | +5% (cumulative +8%) Overworld Points from Task Cave kills. |
| 7 | Marksman's Cloak | Cosmetic cape override with a faint fletching-feather trim. |
| 8 | Practiced Retreat | 10% reduced escrowed task-point loss on death. |
| 9 | Steady Aim III | +7% (cumulative **+15% cap**) Overworld Points from Task Cave kills. |
| 10 | **"Deadshot" (Capstone)** | Arrow-trail cosmetic effect, title suffix "the Deadshot", triggers Ranged Hall of Fame. |

#### Prayer — *"The Devout"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Devout" | Cosmetic title prefix. |
| 2 | Bronze Halo Aura | Skill-tab icon gains a subtle glow. |
| 3 | Sacred Favor I | +3% Overworld Points from Task Cave kills. |
| 4 | "Blessing" emote | Unique prayer-gesture animation. |
| 5 | Steady Faith | Prayer-point drain rate in the Task Cave reduced by 5%. |
| 6 | Sacred Favor II | +5% (cumulative +8%) Overworld Points from Task Cave kills. |
| 7 | Devout Vestments | Cosmetic robe-trim override. |
| 8 | Unwavering | 10% reduced escrowed task-point loss on death. |
| 9 | Sacred Favor III | +7% (cumulative **+15% cap**) Overworld Points from Task Cave kills. |
| 10 | **"The Anointed" (Capstone)** | Golden halo cosmetic aura, title suffix "the Anointed", triggers Prayer Hall of Fame. |

#### Magic — *"The Archmage"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Studious" | Cosmetic title prefix. |
| 2 | Bronze Arcane Aura | Skill-tab icon gains a subtle glow. |
| 3 | Arcane Insight I | +3% Overworld Points from Task Cave kills. |
| 4 | "Spellweave" emote | Unique casting-flourish animation. |
| 5 | Efficient Casting | Task Cave rune-pouch/rune-strain checks relaxed by 5%. |
| 6 | Arcane Insight II | +5% (cumulative +8%) Overworld Points from Task Cave kills. |
| 7 | Archmage's Trim | Cosmetic robe/staff override with a faint spark effect. |
| 8 | Mana Shield | 10% reduced escrowed task-point loss on death. |
| 9 | Arcane Insight III | +7% (cumulative **+15% cap**) Overworld Points from Task Cave kills. |
| 10 | **"The Archmage" (Capstone)** | Arcane particle aura, title suffix "the Archmage", triggers Magic Hall of Fame. |

---

### Gathering skills (5)

Gathering skills earn Overworld Points by depositing raw materials in the **Hopper**
(`Resources.java`). Mining, Fishing, and Woodcutting use the tool-tier variant shape from §5
(free tool upgrades at tiers 1–7); Farming and Hunter follow the standard shape.

#### Mining — *"The Deep Delver"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Free Iron Pickaxe | `overworldToolTier[Pickaxe]` set to Iron at no Overworld Points cost. |
| 2 | Free Steel Pickaxe | Tool tier advanced to Steel, free. |
| 3 | Free Mithril Pickaxe | Tool tier advanced to Mithril, free. |
| 4 | Free Adamant Pickaxe | Tool tier advanced to Adamant, free. |
| 5 | Free Rune Pickaxe | Tool tier advanced to Rune, free. |
| 6 | Free Dragon Pickaxe | Tool tier advanced to Dragon, free. |
| 7 | Free Infernal Pickaxe | Tool tier advanced to Infernal — the ladder's top rung, free. |
| 8 | Deep Vein Sense I | +5% Overworld Points from Hopper ore deposits. |
| 9 | Deep Vein Sense II | +10% (cumulative **+15% cap**) Overworld Points from Hopper ore deposits. |
| 10 | **"The Deep Delver" (Capstone)** | Glowing pickaxe-swing cosmetic trail, title suffix "the Deep Delver", triggers Mining Hall of Fame. |

#### Fishing — *"The Tidecaller"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Free Iron Fishing Tools | `overworldToolTier[Fishing]` set to Iron at no Overworld Points cost. |
| 2 | Free Steel Fishing Tools | Tool tier advanced to Steel, free. |
| 3 | Free Mithril Fishing Tools | Tool tier advanced to Mithril, free. |
| 4 | Free Adamant Fishing Tools | Tool tier advanced to Adamant, free. |
| 5 | Free Rune Fishing Tools | Tool tier advanced to Rune, free. |
| 6 | Free Dragon Fishing Tools | Tool tier advanced to Dragon, free. |
| 7 | Free Infernal Fishing Tools | Tool tier advanced to Infernal — the ladder's top rung, free. |
| 8 | Deep Water Instinct I | +5% Overworld Points from Hopper fish/seaweed deposits. |
| 9 | Deep Water Instinct II | +10% (cumulative **+15% cap**) Overworld Points from Hopper fish/seaweed deposits. |
| 10 | **"The Tidecaller" (Capstone)** | Rippling-water cosmetic aura, title suffix "the Tidecaller", triggers Fishing Hall of Fame. |

#### Woodcutting — *"The Timberlord"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Free Iron Axe | `overworldToolTier[Axe]` set to Iron at no Overworld Points cost. |
| 2 | Free Steel Axe | Tool tier advanced to Steel, free. |
| 3 | Free Mithril Axe | Tool tier advanced to Mithril, free. |
| 4 | Free Adamant Axe | Tool tier advanced to Adamant, free. |
| 5 | Free Rune Axe | Tool tier advanced to Rune, free. |
| 6 | Free Dragon Axe | Tool tier advanced to Dragon, free. |
| 7 | Free Infernal Axe | Tool tier advanced to Infernal — the ladder's top rung, free. |
| 8 | Timber Sense I | +5% Overworld Points from Hopper log deposits. |
| 9 | Timber Sense II | +10% (cumulative **+15% cap**) Overworld Points from Hopper log deposits. |
| 10 | **"The Timberlord" (Capstone)** | Leaf-particle cosmetic aura, title suffix "the Timberlord", triggers Woodcutting Hall of Fame. |

#### Farming — *"The Green Thumb"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Green-Fingered" | Cosmetic title prefix. |
| 2 | Bronze Bloom Aura | Skill-tab icon gains a subtle glow. |
| 3 | Fertile Ground I | +3% Overworld Points from Hopper crop/produce deposits. |
| 4 | "Green Thumb" emote | Unique planting-gesture animation. |
| 5 | Patient Sower | 5% chance a patch doesn't consume its seed on planting. |
| 6 | Fertile Ground II | +5% (cumulative +8%) Overworld Points from Hopper crop/produce deposits. |
| 7 | Harvester's Apron | Cosmetic apron/glove override. |
| 8 | Green Fortune | 5% chance of a bonus harvest yield per patch cycle. |
| 9 | Fertile Ground III | +7% (cumulative **+15% cap**) Overworld Points from Hopper crop/produce deposits. |
| 10 | **"The Green Thumb" (Capstone)** | Blooming-flower cosmetic aura, title suffix "the Green Thumb", triggers Farming Hall of Fame. |

#### Hunter — *"The Silent Tracker"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Watchful" | Cosmetic title prefix. |
| 2 | Bronze Tracker's Aura | Skill-tab icon gains a subtle glow. |
| 3 | Keen Trail I | +3% Overworld Points from Hunter-related Hopper deposits. |
| 4 | "Vanish" emote | Unique stealth-crouch animation. |
| 5 | Practiced Patience | 5% reduced chance of a trap/snare failing on first attempt. |
| 6 | Keen Trail II | +5% (cumulative +8%) Overworld Points from Hunter-related Hopper deposits. |
| 7 | Tracker's Cloak | Cosmetic cloak override, muted earth tones. |
| 8 | Silent Step | 5% chance of an extra catch per successful trap check. |
| 9 | Keen Trail III | +7% (cumulative **+15% cap**) Overworld Points from Hunter-related Hopper deposits. |
| 10 | **"The Silent Tracker" (Capstone)** | Feather/fur particle aura, title suffix "the Silent Tracker", triggers Hunter Hall of Fame. |

---

### Artisan skills (7)

Production skills convert raw materials into finished goods. Economy bonuses for this group
apply to **Overworld Points from Hopper deposits of that skill's finished products** (not raw
materials — this rewards actually using the skill, not just gathering its inputs).

#### Smithing — *"The Forgemaster"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Hammerhand" | Cosmetic title prefix. |
| 2 | Bronze Ember Aura | Skill-tab icon gains a subtle glow. |
| 3 | Tempered Edge I | +3% Overworld Points from Hopper smithed-good deposits. |
| 4 | "Hammer Flourish" emote | Unique anvil-strike animation. |
| 5 | Efficient Forge | 5% chance a smithing action produces a bonus item at no extra material cost. |
| 6 | Tempered Edge II | +5% (cumulative +8%) Overworld Points from Hopper smithed-good deposits. |
| 7 | Forgemaster's Apron | Cosmetic apron override, ember-scorched texture. |
| 8 | Master's Precision | 5% reduced bar cost on smelting actions in the Overworld. |
| 9 | Tempered Edge III | +7% (cumulative **+15% cap**) Overworld Points from Hopper smithed-good deposits. |
| 10 | **"The Forgemaster" (Capstone)** | Molten-glow cosmetic aura, title suffix "the Forgemaster", triggers Smithing Hall of Fame. |

#### Crafting — *"The Artisan"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Meticulous" | Cosmetic title prefix. |
| 2 | Bronze Craftsman's Aura | Skill-tab icon gains a subtle glow. |
| 3 | Deft Hands I | +3% Overworld Points from Hopper crafted-good deposits. |
| 4 | "Fine Detailing" emote | Unique needle/chisel-flourish animation. |
| 5 | Steady Hands | 5% chance to avoid a failed crafting attempt entirely. |
| 6 | Deft Hands II | +5% (cumulative +8%) Overworld Points from Hopper crafted-good deposits. |
| 7 | Artisan's Gloves | Cosmetic glove override, fine-stitched detail. |
| 8 | Practiced Economy | 5% reduced raw-material cost on crafting actions in the Overworld. |
| 9 | Deft Hands III | +7% (cumulative **+15% cap**) Overworld Points from Hopper crafted-good deposits. |
| 10 | **"The Artisan" (Capstone)** | Shimmering-thread cosmetic aura, title suffix "the Artisan", triggers Crafting Hall of Fame. |

#### Fletching — *"The Bowyer"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Precise" | Cosmetic title prefix. |
| 2 | Bronze Fletcher's Aura | Skill-tab icon gains a subtle glow. |
| 3 | Steady Carve I | +3% Overworld Points from Hopper fletched-good deposits. |
| 4 | "Knife Spin" emote | Unique carving-flourish animation. |
| 5 | Efficient Carving | 5% chance a fletching action yields a bonus unit at no extra material cost. |
| 6 | Steady Carve II | +5% (cumulative +8%) Overworld Points from Hopper fletched-good deposits. |
| 7 | Bowyer's Wraps | Cosmetic forearm-wrap override. |
| 8 | Practiced Grip | Fletching action speed increased slightly (bounded QoL, not a training-rate change). |
| 9 | Steady Carve III | +7% (cumulative **+15% cap**) Overworld Points from Hopper fletched-good deposits. |
| 10 | **"The Bowyer" (Capstone)** | Feathered-arrow cosmetic aura, title suffix "the Bowyer", triggers Fletching Hall of Fame. |

#### Cooking — *"The Head Chef"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Seasoned" | Cosmetic title prefix. |
| 2 | Bronze Chef's Aura | Skill-tab icon gains a subtle glow. |
| 3 | Practiced Palate I | +3% Overworld Points from Hopper cooked-good deposits. |
| 4 | "Flambé" emote | Unique flourish-cooking animation. |
| 5 | Steady Hands | 10% reduced chance of burning food while cooking in the Overworld. |
| 6 | Practiced Palate II | +5% (cumulative +8%) Overworld Points from Hopper cooked-good deposits. |
| 7 | Head Chef's Apron | Cosmetic apron override. |
| 8 | Efficient Kitchen | 5% chance to cook two portions from one raw item. |
| 9 | Practiced Palate III | +7% (cumulative **+15% cap**) Overworld Points from Hopper cooked-good deposits. |
| 10 | **"The Head Chef" (Capstone)** | Steam/spark cosmetic aura, title suffix "the Head Chef", triggers Cooking Hall of Fame. |

#### Herblore — *"The Alchemist"*

Trains at the Overworld's herb patch. Economy bonus applies to Hopper deposits of potions and
unfinished potions.

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Herbalist" | Cosmetic title prefix. |
| 2 | Bronze Alchemical Aura | Skill-tab icon gains a subtle glow. |
| 3 | Potent Brew I | +3% Overworld Points from Hopper potion deposits. |
| 4 | "Mix & Swirl" emote | Unique potion-mixing flourish animation. |
| 5 | Herbal Efficiency | 5% chance a herb isn't consumed when cleaning. |
| 6 | Potent Brew II | +5% (cumulative +8%) Overworld Points from Hopper potion deposits. |
| 7 | Alchemist's Apron | Cosmetic apron override, vial-pouch detail. |
| 8 | Concentrated Dose | 5% chance to produce an extra dose per potion made. |
| 9 | Potent Brew III | +7% (cumulative **+15% cap**) Overworld Points from Hopper potion deposits. |
| 10 | **"The Alchemist" (Capstone)** | Bubbling-vial cosmetic aura, title suffix "the Alchemist", triggers Herblore Hall of Fame. |

#### Runecrafting — *"The Rune Warden"*

> **Build note:** the Overworld's ZMI-style rune altar exists as a spawned object, but its
> "Craft-rune" action is currently commented out (dead code) — Runecrafting cannot actually be
> trained in the Overworld yet. This track is designed assuming that gets fixed first; it's a
> prerequisite for this skill's prestige, not part of this document's scope.

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Attuned" | Cosmetic title prefix. |
| 2 | Bronze Runic Aura | Skill-tab icon gains a subtle glow. |
| 3 | Essence Efficiency I | +3% Overworld Points from Hopper rune deposits. |
| 4 | "Rune Weave" emote | Unique altar-channeling animation. |
| 5 | Steady Channel | 5% chance to craft one extra rune per essence used. |
| 6 | Essence Efficiency II | +5% (cumulative +8%) Overworld Points from Hopper rune deposits. |
| 7 | Warden's Robes | Cosmetic robe-trim override, rune-glyph detail. |
| 8 | Pure Essence | 5% chance an essence isn't consumed when crafting. |
| 9 | Essence Efficiency III | +7% (cumulative **+15% cap**) Overworld Points from Hopper rune deposits. |
| 10 | **"The Rune Warden" (Capstone)** | Orbiting-rune cosmetic aura, title suffix "the Rune Warden", triggers Runecrafting Hall of Fame. |

#### Construction — *"The Architect"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Handy" | Cosmetic title prefix. |
| 2 | Bronze Builder's Aura | Skill-tab icon gains a subtle glow. |
| 3 | Sturdy Frame I | +3% Overworld Points from Hopper plank/material deposits. |
| 4 | "Blueprint" emote | Unique measuring/planning animation. |
| 5 | Efficient Build | 5% chance a build action doesn't consume its materials. |
| 6 | Sturdy Frame II | +5% (cumulative +8%) Overworld Points from Hopper plank/material deposits. |
| 7 | Architect's Toolbelt | Cosmetic belt/tool-pouch override. |
| 8 | Master Planner | 5% reduced plank/nail cost on Overworld construction actions. |
| 9 | Sturdy Frame III | +7% (cumulative **+15% cap**) Overworld Points from Hopper plank/material deposits. |
| 10 | **"The Architect" (Capstone)** | Blueprint-particle cosmetic aura, title suffix "the Architect", triggers Construction Hall of Fame. |

---

### Support skills (4)

#### Agility — *"The Acrobat"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Nimble" | Cosmetic title prefix. |
| 2 | Bronze Swift Aura | Skill-tab icon gains a subtle glow. |
| 3 | Sure Footing I | +3% Overworld Points from Hopper deposits made within 30 seconds of a course lap. |
| 4 | "Flip" emote | Unique acrobatic-flip animation. |
| 5 | Second Wind | Run energy drains 5% slower while training Agility in the Overworld. |
| 6 | Sure Footing II | +5% (cumulative +8%) Overworld Points from the same window as tier 3. |
| 7 | Acrobat's Wraps | Cosmetic wrist/ankle-wrap override. |
| 8 | Perfect Balance | 5% chance to avoid a fail-and-fall obstacle entirely. |
| 9 | Sure Footing III | +7% (cumulative **+15% cap**) Overworld Points, same window as tier 3. |
| 10 | **"The Acrobat" (Capstone)** | Wind-trail cosmetic aura, title suffix "the Acrobat", triggers Agility Hall of Fame. |

#### Thieving — *"The Shadow"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Light-Fingered" | Cosmetic title prefix. |
| 2 | Bronze Shadow Aura | Skill-tab icon gains a subtle glow. |
| 3 | Quiet Hands I | +3% Overworld Points from Hopper deposits of stolen/pickpocketed goods. |
| 4 | "Vanish" emote | Unique stealth-fade animation. |
| 5 | Practiced Fingers | 5% reduced stun duration on a failed pickpocket attempt. |
| 6 | Quiet Hands II | +5% (cumulative +8%) Overworld Points from the same source as tier 3. |
| 7 | Shadow's Hood | Cosmetic hood override. |
| 8 | Second Chance | 5% chance a failed pickpocket doesn't trigger a stun at all. |
| 9 | Quiet Hands III | +7% (cumulative **+15% cap**) Overworld Points, same source as tier 3. |
| 10 | **"The Shadow" (Capstone)** | Smoke-fade cosmetic aura, title suffix "the Shadow", triggers Thieving Hall of Fame. |

#### Slayer — *"The Head Hunter"*

Slayer already lives directly in the Task Cave/Wise Old Man system, so its economy bonus is
framed around task completion itself rather than a Hopper deposit.

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Task-Ready" | Cosmetic title prefix. |
| 2 | Bronze Hunter's Aura | Skill-tab icon gains a subtle glow. |
| 3 | Efficient Assignment I | +3% Overworld Points from completed Wise Old Man tasks. |
| 4 | "Mark the Target" emote | Unique target-callout animation. |
| 5 | Task Insight | Wise Old Man reveals the task's kill-count remaining up front (a small QoL clarity perk). |
| 6 | Efficient Assignment II | +5% (cumulative +8%) Overworld Points from completed tasks. |
| 7 | Head Hunter's Cloak | Cosmetic cloak override, trophy-tally stitching. |
| 8 | Vault Insurance | 15% reduced escrowed task-point loss on death mid-task. |
| 9 | Efficient Assignment III | +7% (cumulative **+15% cap**) Overworld Points from completed tasks. |
| 10 | **"The Head Hunter" (Capstone)** | Trophy-rack cosmetic aura, title suffix "the Head Hunter", triggers Slayer Hall of Fame. |

#### Firemaking — *"The Flamekeeper"*

| Tier | Reward | Effect |
|---|---|---|
| 1 | Title: "the Kindler" | Cosmetic title prefix. |
| 2 | Bronze Ember Aura | Skill-tab icon gains a subtle glow. |
| 3 | Steady Flame I | +3% Overworld Points from Hopper log deposits burned for points (shared pool with
Woodcutting's raw-log deposits, but tracked separately so both skills can prestige independently
without double-counting the same physical deposit). |
| 4 | "Spark" emote | Unique tinderbox-flourish animation. |
| 5 | Practiced Kindling | 5% chance a log isn't consumed when lighting a fire. |
| 6 | Steady Flame II | +5% (cumulative +8%) Overworld Points, same source as tier 3. |
| 7 | Flamekeeper's Gloves | Cosmetic glove override, scorch-mark detail. |
| 8 | Lasting Ember | Fires you light in the Overworld burn 20% longer before going out. |
| 9 | Steady Flame III | +7% (cumulative **+15% cap**) Overworld Points, same source as tier 3. |
| 10 | **"The Flamekeeper" (Capstone)** | Ember-particle cosmetic aura, title suffix "the Flamekeeper", triggers Firemaking Hall of Fame. |

---

## 7. Implementation notes (what this actually costs to build)

Roughly in the order I'd tackle them:

1. **The Prestige action itself.** Doesn't exist yet. Needs a right-click "Prestige" option
   on a skill in the Overworld skill tab, gated to `fixedLevel == 99` for that skill and
   `prestigeLevel[skill] < 10`. On confirm: reset that skill's level/XP, increment
   `prestigeLevel[skill]`, grant that tier's reward, fire the Hall of Fame flag if tier hit 10.

2. **Two new permanent (non-season-resetting) fields.** The existing `prestigeLevel` int[23]
   is season-scoped by design — whatever process wipes the Overworld at season end should
   reset it to all-zero, same as skills/inventory/points. Two *new* fields need to live outside
   that reset, the same way e.g. unlocked titles already persist across a fresh account state:
   - `boolean[] prestigeHallOfFame` (23) — permanent, one flag per skill.
   - `int grandPrestigeCount` — permanent, incremented each season the player achieves it
     (supports the "Prestige Master III" style stacking from §4.2).

3. **Cosmetic-override system.** This is the real dependency. Nothing in the codebase
   currently supports "override how an equipped item/aura renders without changing the actual
   item" — the closest existing precedent is the boolean pet-skin unlock flags
   (`unlockedGreenSkin` etc.), which aren't a general system. Every cosmetic reward in §6 (auras,
   emotes, outfit-piece overrides, weapon trails) depends on building this, at minimum a simple
   per-slot override table plus a particle/aura attachment point. This is the single largest
   piece of new infrastructure this document requires — everything else is small, additive
   logic near systems that already exist.

4. **Economy bonus plumbing.** A per-skill bonus percentage feeding into the Hopper deposit
   payout (`Resources.java`) and the Task Cave payout (`CombatTask`/task completion). Small:
   look up `prestigeLevel[skill]`, map to the tier table's cumulative bonus, apply as a
   multiplier at the point Overworld Points are actually awarded.

5. **Tool-tier auto-grant.** Zero new infrastructure — Mining/Fishing/Woodcutting tiers 1–7
   just set `overworldToolTier[i]` directly instead of requiring a purchase, reusing
   `OverworldTools.Tier` exactly as it exists today.

6. **Runecrafting prerequisite.** The Overworld's rune altar "Craft-rune" action is dead code
   (commented out). Runecrafting can't train in the Overworld at all right now, prestige or
   not — flagged in §6 as a blocker independent of this document.

7. **Rewrite the Overworld guide's dialogue.** `OverworldNPCs.java`'s current text promises
   tournament-facing level scaling. Once this document's model ships, that copy needs to change
   to match §2 — happy to draft the replacement lines when this is ready to build.

---

## 8. Bible compliance check

Running this design through the Bible's five-pillar gut check (§3 of the Game Design Bible):

- **P1 (Fair Resets):** Pass by construction — §0 exists specifically to guarantee this. The
  one cosmetic exception (Grand Prestige, §4.2) is explicitly sanctioned by the Bible's own
  carve-out for reputational carryover.
- **P2 (Skill Over Stuff):** N/A — this system doesn't touch tournament gear or combat at all.
- **P3 (Always Something at Stake):** N/A by design — the Overworld is explicitly non-PvP, low-
  stakes content. Not every system needs to serve this pillar; forcing risk into a chill
  skilling game mode would work against what that mode is for.
- **P4 (Every Tournament Tells a Story):** Indirect pass — Grand Prestige broadcasts and Hall
  of Fame flexes are exactly the kind of "remember when" server moments Pillar 4 wants, just
  sourced from the Overworld's season loop instead of a single tournament.
- **P5 (Respect the Player's Time):** Pass — the +15% economy cap and free-automatic-unlock
  model mean prestiging always feels like a reward for grinding, never a second grind bolted on
  top of the first one.
