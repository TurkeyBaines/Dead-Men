# Dead Men — Game Design Bible

> **Status:** Living document. This is the constitution for *Dead Men*. Every future
> decision — a new item, an event, a rate change, a UI tweak — should be checkable against
> this document. If a proposed change can't be justified by the Vision and Pillars below,
> it doesn't go in, no matter how cool it sounds.
>
> **Companion documents:** [`Tournament-Design.md`](./Tournament-Design.md) holds the tuned
> *numbers* for the tournament itself (loot weights, rates, prices, runtimes).
> [`Overworld-Prestige-System.md`](./Overworld-Prestige-System.md) and
> [`Overworld-Points-Citadel-Shops.md`](./Overworld-Points-Citadel-Shops.md) hold the tuned
> numbers for The Overworld — the persistent downtime location this revision formally folds
> into the Bible (see §3a). This Bible holds the *why*. When any companion doc disagrees with
> this one, the Bible wins on direction and the companion wins on exact values.
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
valuable thing this server owns. **Protect it above all else.** The moment starting power
persists between tournaments, or money buys an edge, or veterans start a game mechanically
ahead of new players, the fantasy dies and Dead Men becomes just another grindy PvP server.

**Second location, same promise, different job.** Dead Men now has a second space —
**The Overworld**, reached through the Citadel portal — that runs on its own clock and *does
not* reset with the tournament. It exists to give players something to *do* between and during
tournaments, and (by deliberate design, see §3a) to let dedicated play there earn a bounded,
non-BiS head start spendable back at the Citadel. That is a real, intentional exception to the
"nothing persists" half of the promise above — the "everyone starts equal" half never bends.
Every player still opens every tournament at the same level, the same empty bank, the same
starter kit. What Overworld time buys is better supplies to *start with*, never a higher
ceiling than the tournament itself can hand out.

### 2. The Player (who are we building for, and what do they want?)

Players come to a PvP-survival server for four overlapping reasons. Design must feed all
four, because a server that only serves one type empties out fast:

| Player type | What they crave | What we give them | The Overworld's role |
|-------------|-----------------|-------------------|----------------------|
| **The Killer** | The duel, the clutch, the kill feed | Fair fights, visible bounties, finals, rivalries | A place to wait out downtime (Lobby/Final) without going idle |
| **The Achiever** | Progression, mastery, "best in the lobby" | Leaderboards, ranks, seasonal titles, build mastery | Its **primary home** — Prestige (23 tracks × 10 tiers), tool tiers, Hall of Fame flexes |
| **The Explorer** | Variety, secrets, "what does this do?" | Rotating mutators, events, build experimentation | A whole second economy and skilling loop to master |
| **The Socializer** | Belonging, teams, banter | Teams (Duo/Trio), clans, chat, shared victories | A low-stakes, non-PvP hangout — no tournament pressure, just company |

**The single most underserved type on most PvP servers is the Socializer** — and they're the
glue that keeps a community alive. Design for teams, rivalries, and shared stories, not just
solo combat. A player who has *friends* on the server has a reason to log in even when they
don't feel like fighting — and now they have a *place* to do that: the Overworld is explicitly
non-PvP, so it's the server's one guaranteed-safe social space.

**The Overworld also solves a real structural problem:** the Lobby and Final tournament stages
are, by nature, downtime — waiting for a game to start, or watching finals you're not in. Before
The Overworld, that downtime was dead air. Now it's productive dead air: the game already
directs players there during exactly those windows ("visit the Overworld while you wait").
That's not a side feature, it's load-bearing retention infrastructure.

### 3. Design Pillars (the five tests every decision must pass)

These are non-negotiable. When in doubt, run the idea through all five.

#### Pillar 1 — **Fair Resets.** *Starting power never persists; only prestige — and one bounded bridge — does.*
Tournament gear, stats, and in-tournament currency wipe every tournament. The only things that
survive by default are cosmetic and reputational: titles, ranks, leaderboard history, cosmetic
unlocks. A returning veteran should be *recognizable* (cosmetics, rank) but never structurally
*ahead* in a way a new player can't close by playing well *this* tournament. This is the pillar
that makes Dead Men worth playing. **If a feature lets time or money buy uncapped, unbounded
power, it fails this pillar and does not ship.** §3a below defines the one deliberate,
explicitly bounded exception — read it before assuming anything here is absolute.

#### Pillar 2 — **Skill Over Stuff.** *Outplaying beats out-gearing.*
Gear should *tilt* fights, never *decide* them. A skilled player in mid-gear should be able
to beat an unskilled player in BiS often enough that mechanical skill is the dominant
variable. This is why BiS is rare and earned (events/PvP), never bought or trash-mob common —
and it's why the one thing Overworld Points are *never* allowed to buy is BiS. Gear is a
*thumb on the scale*, not the whole scale, no matter which currency paid for it.

#### Pillar 3 — **Always Something at Stake.** *Tension is the product.*
Deadman's soul is risk. A player carrying loot they could lose, a bank key they need to use,
a bounty on their head, a gas wall closing in — there must *always* be a reason the next
five minutes matter. Safe, frictionless play is the enemy. We don't remove danger; we make
danger *interesting and fair*. This applies just as hard to gear bought with Overworld
Points as to anything found on the ground — the moment it's equipped in a tournament, it can
be lost like anything else. Persistent currency buys a *head start*, never *safety*.

#### Pillar 4 — **Every Tournament Tells a Story.** *Variety is retention.*
Two tournaments should never feel identical. Mutators, event rotations, formats, and the
emergent drama of *who's winning* make each one a fresh story. A player should be able to
say "remember the Berserk Finale where three trios collided on the Gold Cart" — that's the
content. We manufacture the conditions for stories; players write them.

#### Pillar 5 — **Respect the Player's Time.** *Frustration is not difficulty.*
Hard is good. Tedious, unclear, or unfair is not. A death should feel earned. A loot drought
should never starve a player out of the game. The clock should always be visible, the rules
always knowable, the next goal always obvious. We make players *think*, never make them
*guess at our intent*. This now also governs downtime: a player between tournaments should
always have something worthwhile to do (§3a), not just a lobby timer to stare at.

> **The 30-second gut check for any new idea:** Does it keep resets fair (P1)? Does it reward
> skill over gear (P2)? Does it raise the stakes (P3)? Does it add variety (P4)? Does it
> respect the player's time (P5)? If it scores poorly on two or more, cut it or rework it.

### 3a. The One Sanctioned Exception — The Overworld Bridge

This section exists because the closing line of this Bible says "when you change a Pillar,
you're changing what Dead Men *is*. Do it on purpose." This is that deliberate change, written
down instead of left to drift in through feature creep.

**What changed:** Overworld Points — earned by training and fighting in The Overworld, and (as
of this revision) by *acting* inside tournaments — persist across every tournament reset and
can be spent at the Citadel on gear and Sigils that are usable in the very next tournament.
This is a real exception to "power never persists." It is allowed, on purpose, within hard
boundaries:

1. **It never touches the starting line.** Every player still begins every tournament at the
   same level, the same empty inventory, the same empty bank, the same starter kit choice. The
   exception buys *supplies and gear you can purchase after the tournament starts*, not a
   higher floor than anyone else gets. A rich-in-OP veteran and a brand-new player still open
   the tournament identically — the veteran just has a faster, better-funded shopping trip
   available once the gates open.
2. **It has a hard ceiling: strong mid-tier, never BiS.** Per `Overworld-Points-Citadel-Shops.md
   §0`, the Citadel's gear ceiling stays below the tournament's own event/PvP-earned GOD tier.
   Pillar 2 still has to be true *inside* this exception — a maxed-out OP spender should be
   dangerous, never unbeatable, and an unlucky new player in starter gear should still be able
   to win the fight often enough that skill remains the deciding variable.
3. **It's time-gated, not money-gated.** Overworld Points are earned by playing — grinding,
   fighting, completing tournament actions — never purchased with real money. This is the line
   that keeps the exception from becoming Pay-to-Win (§15). If that line is ever crossed, it's
   not a tuning tweak, it's abandoning this pillar — treat it as a five-alarm decision, not a
   sprint ticket.
4. **It's non-tradeable between players.** Overworld Points must never be giftable, tradeable,
   or transferable account-to-account. The moment they are, a real-money marketplace for them
   becomes possible, which reopens the exact RWT risk Pillar 1 was designed to close (§12). A
   currency that can only be earned by the account that spends it is a currency real money can't
   buy a shortcut into.
5. **It's still fully at risk once it's in a tournament (Pillar 3).** OP-bought gear dies like
   any other gear. Buying in doesn't buy safety — it buys a better starting hand, which you can
   still lose to the first player who out-plays you.
6. **Sigils are the recurring proof this isn't a one-time unlock.** Sigils wipe every single
   tournament (`Sigil.reset()` in `Lobby.onLoad()`) and must be *repurchased* with Overworld
   Points every time. This keeps the bridge a living, ongoing cost of staying competitive, not
   a permanent unlock a veteran buys once and never thinks about again.

**Why this is worth the risk:** without it, The Overworld is just a skilling minigame with no
reason to visit once its own cosmetics are collected. With it, every hour spent there has a
second payoff — it *matters* the next time a tournament starts — which is precisely what turns
a side activity into a second reason to keep the server open. The boundaries above exist so
that reason never grows into "why bother playing the tournament fresh, just grind the
Overworld" — if that sentence ever becomes true for a meaningful slice of the playerbase, this
exception has grown past its bounds and needs to be pulled back in, not defended.

**The metric that keeps this honest — "the Bridge Ratio":** watch how much Overworld Points a
strong, dedicated tournament performance earns (via the §3a mechanic tuned in
`Tournament-Design.md §3C`) versus one dedicated hour of endgame Overworld grinding. **Tournament
play should always earn noticeably less OP per hour than the Overworld itself.** If tournaments
ever become the *efficient* way to farm Overworld Points, the bridge has inverted — people will
stop visiting the Overworld to grind it and instead treat tournaments as a grind-optimal chore,
which kills both the "fresh start" fantasy (§1) and the reason the Overworld exists at all. This
is a one-dial problem: if the ratio inverts, turn the tournament-side conversion percentage down
(§3C of the companion doc), don't touch the Overworld's own rates.

---

## Part II — The Experience

### 4. The Nested Gameplay Loops

Great games are loops inside loops, each one rewarding on its own timescale. Dead Men has four
nested loops, plus one **parallel** loop that runs alongside all of them without ever being
reset by the tournament clock:

```
SECONDS  ── Combat loop:     position → attack → eat/pot → react → kill or die
MINUTES  ── Session loop:    skill up → gear up → fight → bank loot → repeat
HOURS    ── Tournament loop: Rush (gear) → War (fight/events) → Squeeze (finals) → reset
WEEKS    ── Season loop:     place → earn Tournament Points → climb rank → new season

PARALLEL ── Overworld loop:  train/fight in the Overworld → earn Overworld Points
                              → spend at the Citadel (gear + Sigils) → repeat
                              (also fed directly by tournament actions — see §3a/§3C)
```

**Every one of these loops must independently feel good and have a clear reward**, because
different players live at different timescales. The Killer lives in the seconds loop; the
Achiever lives in the weeks loop *and* the Overworld loop. If any loop is unrewarding, you lose
that player segment.

- **Seconds (combat):** must feel responsive and readable. See §13 (Feel).
- **Minutes (session):** every few minutes a player should get *something* — a level, a drop,
  a kill, Blood Money, and now a trickle of Overworld Points from that same action (§3C of the
  companion doc). This is why the global loot table splits into a frequent supply roll (see
  companion doc §2). Dead air kills sessions.
- **Hours (tournament):** the three-act structure (§5) gives shape. There's always a "now"
  goal and an "endgame" goal.
- **Weeks (season):** the persistent meta (ranks, cosmetics, leaderboards) gives the reason
  to log in on a Tuesday. Without this loop, players churn after the novelty fades.
- **Parallel (Overworld):** unlike the four nested loops, this one isn't reset by a tournament
  ending — it's reset only when the *season* rolls over, exactly like the Season loop above,
  because it *is* part of the season's persistent meta (see §11). A player can dip into it
  between tournaments, during a Lobby/Final downtime window, or dedicate whole sessions to it —
  it's the one loop that doesn't care what stage the tournament is in.

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

**Where Overworld Points fit into the acts:** a player who arrives with OP-bought supplies
gets a *head start on Act I*, not a skip of it — they still have to walk into the tournament,
still start at level 3, still have to physically reach the Citadel shop and gear up before the
clock's already-running Act I ends. The bridge compresses "time to viability" for OP-rich
players slightly, which is exactly the effect it's supposed to have (§3a) — just make sure it
compresses it, never eliminates it. If OP-funded players are reaching viability meaningfully
before the 33% mark while everyone else still lands on time, that's the tell that the Citadel
shop's Tier 0/1 pricing (companion doc) is too generous — tune the *price*, not the pillar.

**Escalation is the secret sauce.** Events, danger, and rewards should all ramp with the
runtime percentage (your code already computes `Main.getRuntimePercentage()`). Act I events
drop supplies; Act III events drop godswords in forced-PvP zones. The tournament should feel
like it's *building to something*, pulling everyone toward a climactic finals. The Citadel's
own runtime-gated catalog tiers (companion `Overworld-Points-Citadel-Shops.md §2`) deliberately
mirror this same escalation curve, so the two systems always feel like one coherent world
instead of two unrelated economies bolted together.

### 6. First Session — the most important 10 minutes you'll ever design

**A new player decides whether to stay in their first session.** Most PvP servers fail here:
they drop a confused new player into a meat grinder and never see them again. We will not.

The first-session experience must answer three questions *fast and without a wiki*:

1. **"What do I do right now?"** — On first login (and after every reset), the player should
   get a single, clear next step. Not a wall of text. One objective: *"Head to the overworld,
   train on these NPCs, come back geared."* Your `StarterKit` and sigil-pick are the opening
   beats — make them feel like a *choice*, not a chore.
2. **"Am I making progress?"** — Constant small wins in the Rush act. Levels, supply drops,
   Blood Money, and now Overworld Points ticking up in the background from the same actions.
   The new player must *feel* themselves getting stronger within minutes.
3. **"Was that death fair?"** — When they die (they will), the death screen / message should
   make it clear *why*, and that *they'll be back in the next tournament as an equal.* Reframe
   death from "I lost everything" to "round over, next one starts soon." The reset is our
   gift — make sure new players understand that, or they'll read it as the cruelty of other
   servers.

> **Onboarding principle:** A brand-new player and a 500-hour veteran start the *exact same
> way* every tournament — same level, same empty bank, same starter choice. That's our
> superpower — lean into it. The new player is *never* mechanically locked out of anything.
> A veteran's Overworld Points buy them a head start on *shopping speed*, not a different
> starting line. Make sure new players *know* that within their first ten minutes, especially
> once the Citadel shop exists — the first time a new player sees a veteran walk out of the
> Citadel in better gear, they need to already understand why that's still a fair fight.

A short, optional, skippable **"first tournament" tutorial overlay** (where to train, how
bank keys work, what the gas does, how to read the event timer, and — once built — what the
Citadel shop is and isn't) pays for itself many times over in retention. Veterans skip it;
newbies get rescued.

### 7. Progression & Identity (builds, mastery, expression)

Because gear resets, the *durable* progression is **knowledge, skill, and — now — the
Overworld**, and the *in-tournament* progression is **your build**. All three are identity.

- **The Sigil system is your build-crafting layer.** The opening combat-sigil choice (Ranger
  / Fighter / Mage) sets a player's combat identity for the tournament and stays **free** —
  it's never sold, so the very first commitment every player makes is on equal footing
  regardless of Overworld Points. The skilling/utility Sigils are then either earned in-run or
  **repurchased each tournament with Overworld Points** at the Citadel's Sigil Exchange
  (companion doc §4) — treat those as the "build crafting" layer, and keep each roughly equal
  in value so there's no single must-buy that turns into a de facto pay-wall.
- **Mastery is the real meta-progression.** A veteran's edge is *knowing* the optimal Rush
  route, the event timings, the gas patterns, the PvP mechanics — not having better gear.
  This is the healthiest possible progression for a fair PvP game: it's earned, it can't be
  bought, and it transfers to every future tournament. **Lean into rewarding knowledge.**
- **The Overworld is now a durable progression track in its own right.** Prestige (23 skills ×
  10 tiers, fully detailed in `Overworld-Prestige-System.md`) gives the Achiever archetype a
  season-long goal that exists entirely outside the tournament clock, with its own permanent
  Hall of Fame and Grand Prestige flexes — genuine identity that, per §3a, never leaks power
  into a tournament, only reputation.
- **Identity should be *visible*.** Cosmetics, titles, and rank (the persistent meta) let a
  player wear their history. The Killer wants their kill-count title seen; the Achiever wants
  their #1 rank *and* their Grand Prestige aura seen. Make prestige loud and public — it's free
  retention.

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
  must make *permanent safety impossible*. You can rest, but you can't camp to victory. The
  Overworld itself is a deliberate, total exception to this — it's meant to be a genuinely
  safe, non-PvP hangout — but that exemption stops the instant a player steps back through the
  Citadel portal into the tournament proper.
- **Gear bought with Overworld Points is not exempt from any of the above.** A whip bought at
  the Citadel dies to a well-timed PJ the same as a whip found on the ground. The currency
  used to acquire something never changes whether the tournament is willing to take it away.

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
- **The Overworld is the social space with no clock.** Unlike the tournament, there's no
  Rush/War/Squeeze pressure and no PvP risk — it's the one place players can genuinely just
  hang out together between games. Lean into this deliberately: it's the server's "third
  place," not just a skilling minigame.

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
  to collide. Escalate richness/danger with runtime %. As of this revision, completing an
  event also feeds the Overworld Points bridge (companion doc §3C) — one more reason events
  should feel worth chasing beyond their immediate Blood Money payout.
- **Formats** (length + team size + rates) change the *texture* of a session. A 1h Blitz solo
  and an 8h Endurance trio are different games. A varied weekly schedule serves different
  player moods and timezones.

**The variety mandate:** a returning player should be able to ask "what's the tournament
*right now*?" and get a different answer than yesterday. Sameness is the slow death of any
recurring-session game.

### 11. The Season — the meta-game that beats churn

The tournament loop is hours; the **season loop is weeks**, and it's what turns a fun
weekend toy into a server people return to for months. As of this revision, **the Overworld
runs on this exact same clock** — it does not reset with any individual tournament, only when
the season itself rolls over. Season, not tournament, is the top-level "everything persistent
lives here, and only here" boundary now.

- **A season is a series of tournaments with a persistent leaderboard, running alongside one
  continuously-open Overworld.** Players earn **Tournament Points** (placement-based,
  persistent — companion doc §3B) that buy **cosmetics, titles, and rank**, and separately earn
  **Overworld Points** (persistent, companion `Overworld-Points-Citadel-Shops.md`) that buy
  **Sigils and gear usable in the next tournament** — the one deliberate exception carved out
  in §3a. Neither currency buys power *outside* its own defined lane.
- **Seasons reset the leaderboard, the Overworld, and Prestige together** (e.g. monthly or
  6-weekly), giving everyone a genuinely fresh climb on every axis at once — tournament rank,
  Overworld skill levels, Prestige tiers, and Overworld Points balance all zero out together.
  Old cosmetics, titles, and the *permanent* Prestige flexes (Hall of Fame, Grand Prestige —
  `Overworld-Prestige-System.md §4`) stay earned; the *race* restarts. This is the proven model
  (every successful competitive game runs seasons) because it manufactures a fresh start *and*
  preserves earned prestige simultaneously — now on two parallel tracks instead of one.
- **Seasonal exclusives** (a unique cosmetic / title only available this season) give urgency
  without ever touching power. "Play this season or miss the Vampire Lord cape forever" now
  has an Overworld equivalent too: "Hit Grand Prestige this season or wait for the next one."
- **The leaderboard is the carrot — now there are two.** Top-N tournament ranks and visible
  titles are the Killer/Achiever's classic reason to climb. Grand Prestige and per-skill Hall
  of Fame flexes are a second, parallel Achiever carrot that doesn't require winning a single
  tournament to chase. Make both prominent and prestigious.

> **Why this matters:** Without a season loop, Dead Men is a fun thing people try and drift
> from. With it, there's always a reason to log in *this week* — a rank to defend, a cosmetic
> to chase, a Prestige tier to push, a season ending soon. This is the difference between a
> 3-week server and a 3-year server.

### 12. Fairness & Integrity (the invisible foundation)

A PvP server lives or dies on *trust*. If players believe the game is fair, they forgive a
lot. If they suspect cheating, RWT, or favoritism, the community evaporates overnight. This
is not optional infrastructure — it's a core feature.

- **Anti-cheat is a Pillar-1 issue.** Botting/cheating is "buying power" by another name.
  Invest in detection. A single tournament won by a bot, publicly, can poison the well. This
  now extends to Overworld Points farming — an autotyper grinding the Overworld unattended is
  the same integrity violation as a combat bot, just aimed at a slower currency.
- **No staff power in tournaments.** Admins/mods must compete on equal footing or not at all.
  Visible staff advantage is the fastest way to kill a community's trust. Spawning gear,
  god-mode, or seeing positions = death. Staff play in a separate flagged capacity or as
  equals, full stop.
- **Anti-RWT by design — with one carefully-guarded exception.** Power resetting and never
  being purchasable with real money is a structural gift of the Deadman model; protect it. The
  one thing that now genuinely persists and buys real power — Overworld Points — is *only*
  RWT-safe because of a hard requirement: **it must never be tradeable or giftable between
  accounts.** If that constraint is ever relaxed for convenience (account merges, "gift a
  friend some points," anything), a real-money OP marketplace becomes possible overnight, and
  this pillar's protection is gone. Treat "OP is non-transferable" as load-bearing, not a
  nice-to-have.
- **Anti-toxicity.** PvP communities skew hostile. A clear, enforced code of conduct, easy
  reporting, and swift action on harassment keep the Socializers (your glue) from fleeing.
  Toxicity is a retention bug, treat it like one. The Overworld, being non-PvP, should be an
  especially low-friction space — moderate it with the same seriousness as the main game.
- **Transparency builds trust.** Announce rule changes, explain balance decisions, publish
  patch notes. A community that understands *why* you changed something forgives the change.
  A community blindsided by a silent nerf revolts — this applies doubly to any future change
  to the Bridge Ratio (§3a) or Citadel shop pricing, since those changes directly touch how
  much of a head start money-can't-buy-but-time-can gets a returning player.

### 13. Feel & Juice (why it's fun second-to-second)

All the systems above are scaffolding for the moment a player lands a kill. That moment has
to *feel* incredible, or none of the rest matters.

- **Combat must be readable and responsive.** Clear hit feedback, obvious prayer/protection
  states, no input ambiguity. A player who loses must understand *what* killed them within
  the second it happened. Unreadable death = "this game is unfair" = churn.
- **Feedback for everything.** Levels, drops, kills, Blood Money, bounty changes, and now a
  visible Overworld Points tick — every reward needs visible/audible punch. The supply-roll
  loot split (companion §2) exists partly for *feel*: frequent small dings keep the session
  alive. Dead air is the enemy of fun.
- **The clock is always present.** Players should always know what act they're in and how long
  until the next phase/event. Tension requires a visible timer. You already surface event/
  finals countdowns — make them prominent.
- **Spectacle at the peaks.** Event spawns, the gas closing, the finals beginning, a Grand
  Prestige broadcast — these should be *loud*: server-wide announcements, visual drama, a sense
  of occasion. The peaks are what players remember and screenshot.

---

## Part IV — Governing the Project

### 14. The Decision Framework (how to evaluate any future feature)

When you're considering *anything* new, run this checklist. It's the whole Bible compressed
into a tool you can use in 60 seconds.

```
1. WHICH PILLAR does this serve?     (If none → cut it.)
2. WHICH PLAYER TYPE does it feed?   (Killer / Achiever / Explorer / Socializer)
3. WHICH LOOP does it improve?       (Seconds / Minutes / Hours / Weeks / Overworld)
4. DOES IT BREAK PILLAR 1?           (Check against §3a's six boundaries, not a blanket ban —
                                       but if it doesn't clearly satisfy all six, treat it as
                                       a hard NO. Only one exception exists on purpose.)
5. DOES IT ADD CLARITY OR CONFUSION? (Respect the player's time — Pillar 5.)
6. CAN WE TUNE IT WITH ONE DIAL?     (If it needs five knobs to balance, simplify it.)
7. WHAT STORY does it let players tell? (Variety/spectacle — does it create moments?)
```

A strong feature serves a clear pillar, feeds a player type, improves a loop, respects
Pillar 1 absolutely (or clears every §3a boundary explicitly), adds clarity, tunes simply, and
creates stories. A weak one is "cool" but can't answer these. **Cut the cool ones. Ship the
clear ones.**

### 15. Anti-Goals (what Dead Men will deliberately NOT be)

Knowing what you *won't* build is as important as knowing what you will. These are the
temptations that have killed a hundred PvP servers:

- ❌ **A gear-treadmill in the tournament itself.** No persistent gear progression *inside* a
  tournament run. Resets are sacred (Pillar 1) — the one bounded exception (§3a) only ever
  affects what a player can access *before* a tournament starts, never what they can earn
  *during* one beyond the tournament's own systems.
- ❌ **Pay-to-win, ever.** No purchasable-with-real-money power, no donor stats, no $ →
  advantage. Overworld Points are earned by *playing*, never bought — the instant that changes,
  this anti-goal has been violated regardless of what the Bridge Ratio says.
- ❌ **A spreadsheet sim.** Depth comes from *decisions under pressure*, not from 47 stats and
  a crafting tree only a wiki-reader can parse. Respect the player's time (Pillar 5). This
  applies to the Overworld too — 23 Prestige tracks should each be legible at a glance, not a
  min-maxing homework assignment.
- ❌ **A gear-check arena.** If BiS becomes common, skill stops mattering (Pillar 2 violation).
  Keep BiS rare and earned — and keep the Citadel shop's ceiling permanently below it (§3a #2).
- ❌ **A lonely game.** If solo play is always optimal and teaming is friction, the Socializers
  leave and the community dies. Invest in social systems even when solo is simpler to build.
  The Overworld's non-PvP social space is part of this investment, not a separate concern.
- ❌ **A black box.** No silent nerfs, no unexplained changes, no "trust me." Transparency is
  a feature (§12).
- ❌ **A staff playground.** No in-game staff advantage. Integrity over ego (§12).
- ❌ **A second grindy game bolted onto the first.** The Overworld exists to make downtime
  matter and to make the Achiever's season-long ambitions feel real — it must never become
  *mandatory* homework a player has to grind before every tournament just to be competitive.
  If the Bridge Ratio (§3a) drifts toward "you can't compete without hours of prior Overworld
  grinding," that's this anti-goal being violated in slow motion — catch it early.

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
4. **Phase 4 — Make it *deep.*** The Overworld as a full second loop — Prestige, the Citadel
   shops, the tournament-action OP bridge (§3a, companion `Overworld-Prestige-System.md` and
   `Overworld-Points-Citadel-Shops.md`). Only build this once Phase 1–3 are genuinely solid —
   a rich downtime loop wrapped around a boring tournament is a beautiful distraction from the
   real problem.
5. **Phase 5 — Make it *social.*** Clans, rivalries, richer team systems, spectator polish
   (§9). Now the community holds itself together.
6. **Phase 6 — Make it *trustworthy at scale.*** Anti-cheat, moderation tooling, transparency
   cadence (§12), and rigorous enforcement of the Overworld's non-tradeability constraint.
   Now it survives growth.

**Resist building Phase 3+ features before Phase 1 is genuinely fun.** A beautiful season
system — or a beautiful Overworld — wrapped around a boring tournament is a beautiful tomb.

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
6. **The Bridge Ratio** — is a strong tournament performance earning noticeably *less* Overworld
   Points per hour than dedicated Overworld grinding? (Tests §3a. If this inverts, the
   Overworld will empty out and tournaments will start feeling like a second grind.)
7. **Overworld return rate** — do players who visit the Overworld come back to it across
   multiple tournaments, not just once for the novelty? (Tests whether Prestige/Citadel
   actually gives it staying power, or whether it was a one-time curiosity.)

**Change one dial per measurement cycle.** The fastest way to lose control of balance is to
move five numbers at once and never learn which one mattered.

---

## Part V — Quick Reference

### The Bible in one page

- **Fantasy:** A fair, complete, high-stakes survival arena where everyone starts equal every
  few hours — plus a persistent, non-PvP Overworld that bridges a bounded head start between
  tournaments without ever changing the starting line itself.
- **Pillars:** (1) Fair Resets — starting power never persists, with one bounded, explicitly
  documented exception (§3a). (2) Skill Over Stuff — outplay beats out-gear, even gear bought
  with persistent currency. (3) Always Something at Stake — tension is the product, and
  OP-bought gear is just as losable as anything else. (4) Every Tournament Tells a Story —
  variety is retention. (5) Respect the Player's Time — frustration isn't difficulty.
- **Players:** Killer, Achiever, Explorer, Socializer — feed all four. The Overworld is
  especially the Achiever's and Socializer's home.
- **Loops:** Seconds (combat), Minutes (session), Hours (tournament), Weeks (season), and one
  Parallel loop (Overworld) that only resets with the season, not the tournament.
- **Acts:** Rush (gear) → War (fight) → Squeeze (finals). Viability at ~33%, only ever
  *compressed*, never *skipped*, by Overworld Points.
- **Meta:** Cosmetics + rank persist freely. Overworld Points persist and buy real, bounded,
  sub-BiS tournament power — the one deliberate exception, guarded by six hard rules (§3a).
- **Anti-goals:** No mid-tournament persistent power, no pay-to-win, no gear-check, no black
  box, no staff edge, no lonely grind, and no mandatory Overworld homework to be competitive.
- **When unsure:** run the §14 checklist. Serve a pillar, feed a player, improve a loop,
  never break Pillar 1 — or clear every one of §3a's six boundaries explicitly.

### Companion documents
- [`Tournament-Design.md`](./Tournament-Design.md) — tuned numbers: loot tables, rates, prices,
  runtimes, event tiers, the Overworld Points tournament bridge (§3C), and the list of live
  bugs to fix first.
- [`Overworld-Prestige-System.md`](./Overworld-Prestige-System.md) — the 23-skill, 10-tier
  Prestige system: fully Overworld-only power, with permanent Hall of Fame and Grand Prestige
  cosmetic rewards that are allowed to be seen in tournaments because they carry no power.
- [`Overworld-Points-Citadel-Shops.md`](./Overworld-Points-Citadel-Shops.md) — the Citadel's
  runtime-gated shop catalog and Sigil Exchange: where Overworld Points actually get spent, and
  the concrete price ceiling that keeps §3a's boundaries real rather than theoretical.

---

*This Bible is meant to be argued with and revised — but only deliberately, and only in
writing. When you change a Pillar, you're changing what Dead Men* is. *Do it on purpose.*
