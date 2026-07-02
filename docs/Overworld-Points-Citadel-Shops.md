# Overworld Points — Citadel Shops

> **Status:** Living document. Companion to [`Game-Design-Bible.md`](./Game-Design-Bible.md),
> [`Tournament-Design.md`](./Tournament-Design.md), and
> [`Overworld-Prestige-System.md`](./Overworld-Prestige-System.md).
>
> **This document makes a deliberate, explicit exception to Pillar 1.** Overworld Points
> persist across tournaments and buy real, usable tournament power (Sigils and gear) at the
> Citadel. That's a direct policy call from ownership, not something I'm proposing — see §0.
> Where the Prestige doc went out of its way to keep power inside the Overworld, this one is
> the opposite: the entire point is for Overworld grinding to matter in tournaments.

---

## 0. The policy this document implements

Three explicit decisions, given directly, that shape everything below:

1. **Overworld Points do not reset with tournaments.** They're a genuinely persistent,
   cross-tournament currency — the only one that behaves this way (Blood Money and gear wipe
   every tournament per `Tournament-Design.md §3A`; Tournament Points are persistent but
   cosmetic-only per `§3B`). This is new: OP is the first currency allowed to convert directly
   into tournament-usable power.
2. **A player who spends time in the Overworld between/during tournaments should walk into
   the *next* tournament with something to show for it** — spendable at the Citadel, day one.
3. **Sigils reset every tournament** (confirmed in code: `Sigil.reset(player)` fires in
   `Lobby.onLoad()`, wiping `unlockedSigils[]` and `toggleSigils[]`) **and must be repurchased
   with Overworld Points each time.** This makes Sigils Overworld Points' primary, recurring
   sink — every single tournament, a returning player has a reason to spend.

**Why this is safe enough to ship despite Pillar 1:** Pillar 1 exists to stop *time or money*
from buying *permanent, unfair* power — specifically, gear/stats that persist and put new
players permanently behind. Overworld Points threading into tournaments is time-gated (you
have to actually play the Overworld to earn it) but it is **not permanent power** in the sense
Pillar 1 is worried about — a rich-in-OP veteran still starts every tournament at level 3 with
an empty bank, same as everyone else. What OP buys is a **head start on day one**, not a
ceiling no one else can reach. To keep that head start from becoming the whole game, this
document holds two lines throughout, and flags anywhere it's tempted to cross them:

- **The shop's gear ceiling stays at strong mid-tier, never GOD-tier.** Event- and PvP-earned
  BiS (per `Tournament-Design.md`'s reward tiers) stays strictly better than anything OP can
  buy. Pillar 2 ("skill over stuff") still has to mean something even inside this exception.
- **Nothing sold here is a supply-loop skip.** No raw materials, no "buy your way past the
  Rush act." OP buys finished consumables, gear, and Sigils — not a shortcut around the
  tournament's own skilling/gearing loop.

If either line ever gets crossed on purpose, that's a real, separate decision — flag it
explicitly rather than let it drift in through a "just one more item" catalog update.

---

## 1. The math this is priced against

You asked me to look at the existing point-value basis — `Resources.java`'s per-item point
table, used when depositing gathered materials at the Overworld Hopper. It's not a flat rate;
it scales steeply by tier, and (deliberately) rewards *processing* over raw deposit — a cooked
shark (1150 pts) is worth more than a raw one (850 pts), a rune platebody (7334 pts) dwarfs a
single rune bar (1447 pts). Rough bands, read directly from the table:

| Material tier | Point range (raw ore/log → finished good) |
|---|---|
| Bronze/Normal | ~2 – 150 |
| Iron | ~6 – 350 |
| Steel | ~20 – 1,450 |
| Mithril/Gold | ~50 – 1,500 |
| Adamant | ~75 – 5,400 |
| Rune | ~100 – **7,334** (rune platebody — the single highest entry in the table) |
| Fish (shrimp → shark) | 10 – 1,150 |
| Runes (air → wrath) | 6 – 1,200 |

**What this tells us about pricing:** a single high-value finished good (a rune platebody, a
few sharks) is worth low-to-mid thousands of points. A realistic hour of focused Overworld
grinding at endgame tiers — smithing and depositing rune bars, cooking and depositing sharks —
lands somewhere in the **low thousands of points per hour**, less at lower skill tiers. Every
price below is anchored to that reality: cheap items should cost about what a single Overworld
session produces; expensive items should cost what *dedicated, repeated* play produces, so the
top of the shop feels like a genuine season-long goal, not an afternoon's errand.

---

## 2. Runtime-gated catalog structure

The catalog is **cumulative and tiered by tournament runtime percentage**, using the
`Main.getRuntimePercentage()` value that already exists (`Main.java`). Nothing is ever removed
as runtime advances — each tier is a strict superset of the one before it, so a player who
checks the shop at 90% sees everything from 0% *plus* everything newly unlocked. The tier
breakpoints deliberately track the tournament's existing three-act structure
(`Tournament-Design.md §0` — Rush/War/Squeeze), with a little lead time before each act peaks
so players can prep instead of buying reactively:

| Tier | Unlocks at | Tournament act it serves | Theme |
|---|---|---|---|
| **0 — Foundations** | 0% (always) | Pre-Rush | Cheap essentials, so day-one spending always feels worth it |
| **1 — Rush Supplies** | ≥ 15% | Rush | Mid consumables, budget gear, staple ammo/runes |
| **2 — War Chest** | ≥ 40% | War | Strong mid-tier weapons/armor, better potions |
| **3 — Squeeze Reserve** | ≥ 70% | Squeeze | The shop's ceiling — best-in-shop, still sub-BiS |

**Why cumulative, not swapped:** a player who logs in late (or missed the early window) should
never be *locked out* of Tier 0/1 essentials just because the clock moved on — the gate is
additive, not a rotating door. This also means the shop visibly *grows* over the course of a
tournament, which is itself a small piece of content (Pillar 4 — "watch the shop fill in" is a
minor but real story beat, especially in longer formats like Marathon/Endurance/Grand).

**Implementation shape:** four YAML shops (`Overworld_Exchange_Tier0.yaml` … `_Tier3.yaml`),
each a cumulative superset of the previous, opened via one NPC action that picks the highest
tier the player currently qualifies for (`Main.getRuntimePercentage()` read at open-time).
This reuses the exact shop system rebuilt in the earlier name-based shop overhaul — no new
shop infrastructure needed beyond the currency handler in §5.

---

## 3. The Quartermaster — general gear & consumables

Located at the Citadel alongside the existing Sir Sell A Bit's Emporium and portal. Sells
finished consumables and gear only — never raw materials (see §0's second guardrail).

### Tier 0 — Foundations (available from 0%)

| Item | Price (OP) | Notes |
|---|---|---|
| Teleport runes/tabs (home + 2 common hubs) | 15 – 40 each | Immediate mobility from minute one |
| Basic food (trout/salmon-tier) | 8 each | Cheap sustain, matches Rush-act food scarcity |
| Basic potions (attack/strength/defence) | 60 each | Entry-level combat prep |
| Standard ammo (iron/steel arrows, bolts) | 2 – 5 each | Trivial cost, keeps ranged viable day one |
| Prayer potion (dose) | 90 | Slightly pricier — Prayer matters early for some Sigil builds |

### Tier 1 — Rush Supplies (≥ 15% runtime)

| Item | Price (OP) | Notes |
|---|---|---|
| Mithril/Adamant weapon (one-hand, tier-appropriate) | 400 – 700 | Matches the "supplies + cheap mid-gear" Rush-act philosophy from `Tournament-Design.md` |
| Adamant armor piece | 250 – 500 | Sold per-slot, not full sets — keeps individual purchases affordable |
| Super attack/strength potion | 180 each | |
| Runite bolts/arrows (bulk, 250) | 350 | |
| Staple combat runes (bulk, 500) | 200 – 450 depending on rune | Fire/water/earth/air cheap; chaos/nature pricier |

### Tier 2 — War Chest (≥ 40% runtime)

| Item | Price (OP) | Notes |
|---|---|---|
| Rune weapon (one-hand) | 1,400 – 2,200 | Matches rune-tier resource pricing directly |
| Rune armor piece / equivalent mid-high armor | 900 – 1,600 | Per-slot |
| Super combat potion | 320 each | |
| Saradomin brew / super restore | 260 each | |
| Death/blood rune (bulk, 250) | 900 – 1,800 | Priced to match their standalone resource value |
| Ranged/magic ammo upgrade (rune-tier bolts, better runes) | 600 – 1,200 | |

### Tier 3 — Squeeze Reserve (≥ 70% runtime)

| Item | Price (OP) | Notes |
|---|---|---|
| Dragon-tier weapon (one-hand) | 5,000 – 9,000 | The shop's ceiling — strong, but explicitly **not** raids/GOD-tier |
| Barrows-adjacent armor piece | 3,500 – 6,000 | Per-slot; still below event/PvP-earned BiS |
| Overload-tier potion (or best available combat potion) | 700 each | |
| High-value ammo bulk (rune arrows/bolts, 250) | 2,500 – 3,500 | |
| Wrath rune (bulk, 100) | 4,000 | Priced against the single highest per-unit entry in `Resources.java` |

**Guardrail check:** nothing in Tier 3 should out-perform an actual event/PvP GOD-tier drop.
If a specific item on this list would, cut it or downgrade its stats before shipping — the
list above is a shape and a price band, not a final locked catalog; sanity-check every
concrete item against the current GOD-tier loot table before committing IDs.

---

## 4. The Sigil Exchange

Sells every Sigil **except** the three free starters (Formidable Fighter, Ruthless Ranger,
Menacing Mage — still chosen for free via the existing tournament-start dialogue in
`Main.onLoad()`). Available from **Tier 0** — Sigils are the primary sink and shouldn't wait
for a runtime gate; a player should be able to fully re-equip their loadout the moment a new
tournament starts, if they've earned the points for it.

**Important, before pricing:** I read every Sigil's actual implementation, not just its name.
**About half of the non-starter roster has no implemented effect yet** — the `effect()` method
either isn't overridden at all, or (in Titanium's case) the trigger chance is hardcoded to 0,
meaning the sigil can never fire even if it had a body. Selling something that currently does
nothing for real, persistent currency is a trust problem waiting to happen. Every row below is
flagged **Live** or **Stub** — recommend either finishing the Stub sigils' code before they go
on sale, or shipping them at a clearly-labeled placeholder price with in-game "Effect coming
soon" messaging. Don't silently sell a Stub at full price.

### Combat Sigils

| Sigil | Status | What it actually does | Price (OP) |
|---|---|---|---|
| Restoration | **Live** | Heals `damage ÷ 10` (min 1) on every hit landed — a small guaranteed lifesteal. | 900 |
| Conclusion | **Live** | Instantly restores up to 10 special attack energy on trigger, 100% chance. | 1,100 |
| Deft Strikes | Stub | No effect body — `Chance()` 100 but nothing fires. | *hold from sale* |
| Meticulousness | Stub | Comment marks it "Upgrade from Deft Strikes" — clearly intended as the combat track's capstone. No effect body yet. | *hold from sale* |
| Onslaught | Stub | No effect body. | *hold from sale* |
| Resistance | Stub | Paired with Titanium (cross-referencing comments); `RestrictedWith()` returns `false` for both, so the intended "pick one of this pair" exclusivity isn't enforced either. | *hold from sale* |
| Titanium | Stub, and **currently unfireable** | `Chance()` returns 0 — even with an effect body added, this sigil can never trigger as written. | *hold from sale, flag as broken* |

### Skilling Sigils

| Sigil | Status | What it actually does | Price (OP) |
|---|---|---|---|
| Efficiency | **Live** | 50% chance to duplicate a gathered resource on pickup. | 1,600 — priced high on purpose, see caution below |
| Internal Chef | **Live** | 50% chance to auto-cook a raw food item you pick up. | 500 |
| Hoarding | Stub | No effect body. | *hold from sale* |
| Agile Fortune | Stub | No effect body. | *hold from sale* |
| Deception | Stub | Comment describes "auto re-pickpocket until you can't" — a real, specific design intent, just not built. | *hold from sale* |
| Litheness | Stub | No effect body. | *hold from sale* |

> **Caution on Efficiency specifically:** a 50%-chance resource *duplication* is a genuinely
> strong economic multiplier, and it's Live today. Because this shop's whole premise is
> "Overworld grinding should matter," a returning player buying Efficiency effectively
> compounds their own future OP income — the richer you are, the faster you get richer. That's
> a real snowball risk worth a second look before shipping at any price; consider capping it to
> a chance-to-not-consume instead of a duplicate, or gating it to Tier 2+ of the runtime ladder
> so it can't be bought turn-one of a fresh season.

### Utility Sigils

| Sigil | Status | What it actually does | Price (OP) |
|---|---|---|---|
| Faith | **Live** | Boosts Prayer restoration from prayer potions / super restores beyond normal. | 700 |
| Food Master | **Live** | 5% chance to not consume a food item on eat. | 450 |
| Potion Master | **Live** | 5% chance to not consume a potion dose on drink. | 450 |
| Revoked Limitation | Stub | No effect body. | *hold from sale* |
| Eternal Belief | Stub | No effect body. | *hold from sale* |
| Well Fed | Stub-adjacent | Has a real effect body (heals on a 5% roll), but nothing currently calls `trigger()`/`effect()` for it from the eating flow — needs wiring, not just an effect. | *hold from sale, needs wiring* |

### Toggleable Sigils

These are a separate enum (`ToggleSigils`) with their own unlock flow
(`Sigil.unlock(player, ToggleSigils)`), also wiped by `Sigil.reset()` — same sink logic
applies.

| Sigil | Status | What it actually does | Price (OP) |
|---|---|---|---|
| Devotion | **Live** | Doubles Prayer XP gained from a triggering action. | 800 |
| Enhanced Harvest | **Live**, flag it | Guaranteed **triple** yield on a gathering action (100% chance, no roll). | *see caution below* |
| Remote Storage | **Live**, flag it | Sends a gathered resource straight to the bank instead of the inventory. | *see caution below* |
| Slaughter | Stub | No effect body — the name suggests a combat-kill trigger, but nothing is implemented. | *hold from sale* |

> **Caution on Enhanced Harvest:** a guaranteed 3x yield, if it applies to tournament gathering
> (not just Overworld gathering), directly undercuts the Rush act's "skill up, gear up" pacing
> — a player with this active could gear up roughly three times faster than intended for that
> act. Confirm this sigil is scoped correctly (Overworld-only training speed, not a tournament
> supply-loop multiplier) before pricing it into this shop at all. If it *does* apply in
> tournaments, this is the one sigil most likely to break Pillar 5's "time to viability" pacing
> from `Tournament-Design.md §0` — recommend nerfing to a percentage chance rather than
> guaranteed, or restricting it to Tier 3 (70%+ runtime, past the point where Rush-act pacing
> still matters).
>
> **Caution on Remote Storage:** auto-banking loot the moment you gather it meaningfully
> reduces "carrying something you could lose" — that's Pillar 3's entire reason for being
> (`Game-Design-Bible.md` — "Always Something at Stake"). This doesn't have to be cut, but it
> should probably be priced as the single most expensive item in the whole Sigil Exchange, or
> restricted to non-combat/skilling contexts only, so it isn't quietly neutering wilderness risk
> for a price tag.

**Live sigils only, cost to fully re-equip a returning player each tournament:** roughly
5,500 OP across all three trees (Restoration + Conclusion + Efficiency + Internal Chef + Faith
+ Food Master + Potion Master + Devotion + Enhanced Harvest + Remote Storage), before the two
flagged items above are potentially repriced or restricted. That's the real number to build
the "hours of Overworld grinding per tournament re-equip" pacing target around, once the Stub
sigils are finished and folded in properly.

---

## 5. Implementation notes

1. **New currency handler, small.** `CurrencyHandler` is already a clean abstract interface
   (`getCurrencyCount` / `removeCurrency` / `addCurrency`) and every existing `Currency` enum
   entry wraps an `ItemCurrencyHandler` — i.e., today, every shop currency is backed by a
   physical inventory item. Overworld Points is a plain `int` field (`player.overworldPoints`),
   not an item, so it needs one new small class — a `PointsCurrencyHandler` (or similar) that
   reads/writes `player.overworldPoints` directly instead of touching the inventory — plus one
   new `Currency.OVERWORLD_POINTS` enum entry wrapping it. This is the only new piece of shop
   *infrastructure* this document requires; everything else reuses the shop system as-is.

2. **Runtime-tier shop selection.** The NPC action that opens the Quartermaster should read
   `((Main) Deadman.getStage()).getRuntimePercentage()` (guarding for the case the current
   stage isn't `Main` — e.g. Lobby/Final) and open the highest qualifying tier's shop. Cheapest
   correct implementation: four cumulative YAML shops as described in §2, rather than trying to
   dynamically mutate one shop's live stock — simpler, and avoids any edge case around the
   restock loop fighting with a runtime-driven stock injection.

3. **Sigil Exchange wiring.** Not a standard `Shop`/`ShopItem` purchase flow — Sigils aren't
   items, they're a boolean flag (`player.unlockedSigils[id]`) toggled via `Sigil.unlock()`.
   This needs a small dedicated interface (or an `OptionsDialogue` menu, matching the pattern
   `TournamentTicket.java` already uses) that checks `player.overworldPoints >= price`, deducts
   via the new currency handler, then calls `Sigil.unlock(player, sigil)` — not a reuse of the
   generic `Shop` buy flow, which assumes a physical `ShopItem`.

4. **Finish or gate the Stub sigils before sale.** Ten of the twenty-three sellable sigils
   currently have no gameplay effect. Selling them for real, non-refundable, persistent
   currency without fixing this first is a player-trust problem, not just an imbalance one —
   recommend either finishing their `effect()` implementations first, or shipping the Sigil
   Exchange with only the thirteen Live entries and adding the rest as their code lands.

5. **Resolve the two flagged Live sigils (Enhanced Harvest, Remote Storage) before pricing
   them for real.** Both interact directly with a Bible pillar (§4's cautions) — confirm scope
   and, if needed, nerf before they're sellable rather than pricing around an unreviewed effect.

6. **Sanity-pass the Quartermaster's concrete item IDs against the current GOD-tier loot
   table** before finalizing — §3's guardrail (shop ceiling stays below event/PvP BiS) is a
   constraint on the *design*, not something enforced by the shop system itself. It's only
   real if someone checks it item-by-item at implementation time.
