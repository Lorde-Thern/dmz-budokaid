# DMZ Budokai

An addon for **[Dragon Mine Z](https://github.com/DragonMineZ/dragonminez)** (Forge, Minecraft 1.20.1) that replaces Training Points with
training like the Roblox Dragon Ball "Xeno" games. You don't earn TP and spend it in a menu. You **train**, your stats go up directly,
and your **Battle Power** rises with them.

- **Train to get stronger.** Punching bags, weights, meditation, sparring and running each raise specific DMZ stats.
- **Partner training.** Training near other players who are also training gives bonus gains.
- **Leeching.** Training near someone much stronger than you gives a large leech bonus. The stronger player gets a small mentor bonus for helping.
- **Transformations by Battle Power.** Reach a BP threshold and the form unlocks: Super Saiyan at **1M** base BP, SSJ2 at 15M, and so on, on a realistic scale for every race.
- **Realistic Battle Power.** Transforming multiplies your BP like in the show: Super Saiyan ×50, SSJ2 ×100, SSJ3 ×400, Kaioken ×N.
- **Limitless mode (optional).** Removes every ceiling so you can keep getting stronger forever, without boosting gains.
- **Custom races just work.** Any race Dragon Mine Z loads, including hand-made ones and races made with **DMZ Ragnarok**'s editor, trains and unlocks forms with no extra setup.
- **The Angel Path.** An optional progression path with ranks, perks and a purity requirement. Built to work alongside **Dragon Block Noea**.

Requires Dragon Mine Z **2.1.x** (built against 2.1.3) and its own dependencies (GeckoLib, TerraBlender, Curios).

---

## Training

| Activity | How | Trains | Uses stamina |
|---|---|---|---|
| Punching Bag | Place it, then punch it (left-click). Sneak + mine to pick it up | Strength, Strike Power | yes |
| Training Weights | Hold right-click | Resistance, Vitality | yes |
| Meditation | Sneak and stand still | Ki Power, Energy | no |
| Sparring (offense) | Hit a mob or player | Strength, Strike Power | yes |
| Sparring (defense) | Get hit by a mob or player | Resistance, Vitality | no |
| Roadwork | Sprint on the ground | Vitality, Energy | no |

Every rep adds **training XP** to each stat it trains. When a stat's XP bank is full, you get a stat point:

```
XP per rep        = activityXp * (1 + currentStat * statXpGrowthPerPoint) * bonuses   (growth default 0.0125)
XP for next point = statCostBase + currentStat * statCostPerPoint                  (defaults: 12.5 + stat * 0.0625)
```

Early on, a point takes a few reps. Later it settles at about one point per rep per stat, so millions of BP are reachable
with real grinding. Gains respect Dragon Mine Z's per-stat and total
stat caps. Training uses DMZ **stamina**: when you're out, you're too exhausted to train until you recover.

When `disableTrainingPoints = true` (the default), every DMZ Training Point gain is cancelled, so training is the only way to grow.
Set it to `false` if you want both systems at once.

### Partner and leech bonuses

Anyone within `partnerRadius` (12 blocks) who did a rep in the last `partnerActiveTicks` (5 s) counts as a partner.

```
partner multiplier = 1 + 0.25 * partners          (up to 4 partners = x2.0)
leech multiplier   = 1 + 0.5 * log2(theirBP / yourBP)   if their BP >= 2x yours, capped at x3.0
mentor multiplier  = x1.1 for the stronger player while someone leeches from them
total              = partner * leech * mentor * angel * globalGainMultiplier
```

Leeching compares **trained base** Battle Power (no transformations, buffs or power release), so powering up or transforming doesn't change who leeches from whom.
Examples: a partner with 4x your BP gives x2.0, and 16x or more gives the x3.0 cap.

A HUD panel on the left of the screen shows your current activity, Battle Power, partners, leech source and total multiplier while you train.

## Realistic Battle Power

With `realisticBattlePower = true` (the default), Battle Power works like Dragon Ball power levels:

```
shown BP   = trained base BP  x  form multiplier  x  stack form multiplier  x  power release %
trained BP = DMZ's BP curve on your stats (race scaling and permanent bonuses included), nothing else
```

- **Training** raises your trained base BP. A fresh character starts in the hundreds, about where Goku was at the start of Z.
- **Transforming** multiplies it, using canon-style numbers. A 1M-base Saiyan going Super Saiyan reads **50M**, like Goku's 3M to 150M on Namek.

| Form | BP multiplier |
|---|---|
| Super Saiyan / Mastered | ×50 |
| SSJ Grade 2 / Grade 3 | ×60 / ×75 |
| SSJ2 / SSJ3 | ×100 / ×400 |
| Oozaru / Golden Oozaru / SSJ4 | ×10 / ×500 / ×4000 |
| Kaioken | ×2, ×3, ×4, ×10, ×20, ×100 |
| Ultimate | ×100 |
| Ultra Instinct / Ultra Ego | ×1000 (Sign), ×5000 (Mastered) |
| Other races | Full table in `formBpMultipliers`; e.g. Frieza's final form ×4 then Full Power ×10, Perfect Cell ×6, Super Namekian ×50 |

- **Forms with no number** (custom races, addon forms, god forms) use DMZ's own stat multiplier for that form raised to
  `formBpFallbackExponent` (5.5). DMZ's ×1.5 becomes about ×9, ×2.25 about ×90, and ×3 about ×400, so custom forms still scale sensibly.
- **Kaioken stacks** on top of a form, so SSJ + Kaioken x20 is ×1000.
- **Everywhere DMZ shows BP** (scouter, stats screen, ki sense) shows the realistic number, via a small mixin. In DMZ, BP is only used for display and
  comparisons, so **damage and combat balance are unchanged**: they still come from DMZ's stats and form multipliers.
- **Form unlocks, leeching and the Angel Path** always use your **trained base BP**, so transforming or powering up never counts toward them.

Turn it off with `realisticBattlePower = false` to go back to DMZ's own BP formula.

## Limitless mode

`limitlessMode = true` (off by default) removes the ceilings on growth:

- Training ignores DMZ's total stat cap (`maxValue * 6`).
- DMZ's per-stat cap is lifted too (including when player data reloads), via a small mixin.
- The "raise DMZ's maxValue" warning is turned off, since the cap no longer applies.

It does **not** boost anything: gains, costs, partner and leech bonuses all stay exactly the same. You just never hit a wall.
The only remaining limit is that DMZ stores each stat as a whole number, so one stat tops out at 2,147,483,647. That's far beyond
anything reachable by training, and a trained base BP in the trillions.

## Transformations by Battle Power

With `bpFormUnlocks = true` (the default), DMZ forms unlock when your **base Battle Power** (BP without power release) reaches the
form's threshold. You get a chat message when your power surges. `/budokai forms` lists every form for your race, its BP
requirement, and whether you have it.

How it works: DMZ unlocks forms through form skills (`superforms`, `kaioken`, ...). Each form has an unlock level. This addon reads your
race's forms straight from DMZ's loaded config and raises the form skill when your BP is high enough. It only ever raises skills, so
forms earned from quests or wishes are kept. DMZ's own rules still apply on top, such as needing mastery of the previous form or a tail for Oozaru.

### Realistic defaults (`config/dmzbudokai/form_unlocks.json`)

| Race | Form (BP) |
|---|---|
| Saiyan | Super Saiyan **1M** · Grade 2 1.5M · Grade 3 2.5M · SSJ Mastered 5M · SSJ2 15M · SSJ3 50M · Golden Oozaru 100M · SSJ4 250M |
| Human | Buffed 100K · Full Power 500K · Overdrive 3M · Solaris 20M |
| Namekian | Giant 150K · Full Power 750K · Super Namekian 5M |
| Frost Demon | Second 100K · Third 300K · Final 800K · Full Power 10M · Fifth 100M |
| Majin | Kid 250K · Evil 1M · Super 8M · Ultra 50M |
| Bio-Android | Semi-Perfect 300K · Perfect 1.2M · Super Perfect 10M · Ultra Perfect 60M |
| Any race (stack forms) | **Kaioken (needs King Kai)** x2 50K · x3 150K · x4 400K · x10 2M · x20 6M · x100 75M · Ultimate 20M · **Ultra Ego (needs Beerus)** 500M / 2B · **Ultra Instinct (needs Whis) 500M / 2B + Attendant Angel rank** |

**God forms:** DMZ doesn't ship any; they come from custom race files or addons (DMZ files them under the `godforms` skill).
Any god form without its own number in `form_unlocks.json` needs at least **150M** base BP (`skillMinimumBp`), plus Whis.

Legendary forms and android forms are excluded by default (`excludedSkills`). They're lineage and cybernetics, not training, so
they keep DMZ's normal unlocks. Ultra Instinct also needs the Angel Path (`angelRankRequired`).

**Some forms need a master.** Kaioken needs **King Kai** (`master_kaiosama`), Ultra Instinct and **god forms** need **Whis**
(`master_whis`), and Ultra Ego needs **Beerus** (`master_beerus`), all DMZ NPCs. A form only unlocks by BP after you've found its master and right-clicked
them once. They tell you they'll teach you, and from then on that form's levels unlock as your BP reaches them. `/budokai forms` shows
"+ talk to <master>" until you do. This is set by `masterRequired` in `form_unlocks.json` (skill → master entity id), so you can give
other forms a master too, or change who teaches what. Forms bought directly from a master through DMZ still work as normal.

Rough solo training time with default settings: SSJ about 3-4 hours of mixed training, SSJ2 roughly 25-30 hours. Partners and leeching cut that a lot.
Tune `statXpGrowthPerPoint`, `globalGainMultiplier` or the thresholds to taste, then run `/budokai reload`.

### Raise Dragon Mine Z's stat cap (or use limitless mode)

DMZ caps total stats at `maxValue * 6` (in `config/dragonminez/general-server.json`, default 10000). That caps Battle Power:

| DMZ `maxValue` | Max trained base BP (about) | Reaches |
|---|---|---|
| 10,000 (default) | 1.6M | SSJ only |
| 250,000 | 78M | SSJ3 |
| 1,000,000 | 414M | SSJ4 |
| 4,000,000 | 2.2B | everything incl. Ultra Instinct |

Set it with DMZ's `/dmzconfig` command or by editing the file, or turn on `limitlessMode` to remove the cap entirely. The server log and `/budokai forms` warn while it's below
`recommendedDmzMaxValue` (1,000,000).

### Custom races (hand-made, DMZ Ragnarok, Dragon Block Noea)

Dragon Mine Z loads custom races from `config/dragonminez/races/<race>/` (including `forms/*.json`). Everything in this addon reads
races from DMZ at runtime, so a custom race needs no extra setup:

- **Training and BP** use DMZ's own stats and Battle Power formula, which already include the race's stat scaling.
- **Form unlocks**: every form in the race's form files gets a BP requirement. If it isn't listed in `form_unlocks.json`, it uses
  `defaultFirstFormBp * defaultBpMultiplierPerLevel^(unlockOnSkillLevel - 1)`, which is 1M, 3M, 9M, 27M... by default.
  If a custom race defines more form levels than DMZ's TP-cost table allows, the cap is raised so the form can still unlock.
- To set exact numbers, add the race using its **folder name**. Run `/budokai forms` as that race to see the exact group and form names:

```json
"races": {
  "saibaman": {
    "supersaibaman": { "supersaibaman": 2000000, "ultrasaibaman": 20000000 }
  }
}
```

Race `"*"` applies to every race, which is handy when several custom races share a group name.

**DMZ Ragnarok note:** this was written without access to Ragnarok's code. Ragnarok's race editor builds races for DMZ, so they should
show up like any other custom race. Check with `/budokai forms` while playing a Ragnarok race. If forms Ragnarok adds don't appear
in that list, Ragnarok is storing them outside DMZ's form config, and they'll need their own integration.

## The Angel Path

Angels are the greatest teachers in the multiverse (think Whis and Vados). The path is opt-in:

```
/budokai angel info    – requirements, your rank and progress
/budokai angel join    – start the path
/budokai angel leave   – leave it (you can't rejoin for a while)
```

**Requirements to join (configurable):** base BP of 5,000 or more, DMZ alignment of 75 or more, and a race that isn't forbidden (see Noea below).

**Ranks:** Angel Trainee → Attendant Angel → Guardian Angel → Grand Angel. To rank up you need **Divine XP** (1 per training rep,
2 per meditation rep) and base Battle Power. The defaults are 500 / 2,500 / 10,000 Divine XP and 50K / 500K / 5M BP.

**Perks (per rank):**
- +25% meditation gains
- **Angelic guidance:** anyone training near you gets +10% gains
- **Angelic Instinct:** 5% chance to completely dodge an attack (3 s cooldown)
- +5% movement speed
- The DMZ halo

**Purity:** if your DMZ alignment drops below 40, you **fall** from the path and lose your rank and Divine XP.

### Dragon Block Noea

Dragon Block Noea builds on Dragon Mine Z's race system, so this addon reads the player's DMZ race name instead of depending on Noea's code.
By default the races `demon` and `glind` (Noea's demon race) **cannot** walk the Angel Path. If Noea uses different internal race names in
your version, edit `angelForbiddenRaces` in the config to match. Run `/budokai angel info` while playing a Noea race to see the name being read.
`noeaModIds` only affects whether `/budokai angel info` mentions Noea.

## Commands

| Command | |
|---|---|
| `/budokai` | Your BP, stats and progress toward the next point in each stat |
| `/budokai angel [info\|join\|leave]` | Angel Path |
| `/budokai forms` | Every form for your race, its BP requirement, and whether you have it |
| `/budokai reload` | Reload `form_unlocks.json` (op level 2) |
| `/budokai admin angelrank <player> <rank>` | Set someone's Angel rank (op level 2; 0 removes them) |

## Config

- `<world>/serverconfig/dmzbudokai-server.toml`: every gain, cost, radius, multiplier and Angel Path threshold, with comments.
- `config/dmzbudokai/form_unlocks.json`: BP thresholds for transformations (created on first server start).

## Recipes

- **Punching Bag:** chain on top, then leather / wool / leather twice.
- **Training Weights:** two iron ingots on top, then iron block / stick / iron block.

---

## Building

1. Install **JDK 17**.
2. Download the Dragon Mine Z 1.20.1 jar (CurseForge or Modrinth) and put it in `libs/` named **`dragonminez-2.1.3.jar`**.
   If you use another version, change `dmz_version` in `gradle.properties` to match the filename.
3. Run `./gradlew build` (on Windows: `gradlew.bat build`). The mod jar is written to `build/libs/`.
4. `./gradlew runClient` starts a dev game with DMZ, GeckoLib, TerraBlender and Curios loaded.

## Project layout

```
src/main/java/com/dmzbudokai/
  DMZBudokai.java            mod entry point
  config/BudokaiConfig       all server config values
  training/                  TrainingManager (XP to stats), PartnerTraining (partner/leech/mentor),
                             TrainingEvents (combat, meditation, sprint, TP cancel), TrainingActivity
  compat/BattlePower         trained BP and realistic BP (form multipliers)
  mixin/                     StatsDataMixin (DMZ reports realistic BP), StatsMixin (limitless per-stat cap)
  angel/AngelPath            Angel Path ranks, perks, purity check, Angelic Instinct
  forms/                     BP transformation unlocks (FormUnlocks) and form_unlocks.json (FormUnlockConfig)
  compat/DmzBridge           every Dragon Mine Z API call, in one place
  compat/NoeaCompat          Dragon Block Noea race rules
  data/                      per-player saved training data (Forge capability)
  content/                   Punching Bag block, Training Weights item
  network/, client/          training HUD
  command/BudokaiCommand     /budokai
```

If a Dragon Mine Z update changes its API, `compat/DmzBridge.java` is the only file that should need changes.
