# DMZ Budokai

An addon for **[Dragon Mine Z](https://github.com/DragonMineZ/dragonminez)** (Forge, Minecraft 1.20.1) that replaces Training Points with
training like the Roblox Dragon Ball "Xeno" games. You don't earn TP and spend it in a menu. You **train**, your stats go up directly,
and your **Battle Power** rises with them.

- **Train to get stronger.** Punching bags, weights, meditation, sparring and running each raise specific DMZ stats.
- **Partner training.** Training near other players who are also training gives bonus gains.
- **Leeching.** Training near someone much stronger than you gives a large leech bonus. The stronger player gets a small mentor bonus for helping.
- **Transformations by Battle Power.** Reach a BP threshold and the form unlocks: Super Saiyan at **1M** base BP, SSJ2 at 15M, and so on, on a realistic scale for every race.
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
XP for next point = statCostBase + currentStat * statCostPerPoint                  (defaults: 10 + stat * 0.05)
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

Leeching compares **base** Battle Power (DMZ's BP with the power-release % removed), so powering up doesn't change who leeches from whom.
Examples: a partner with 4x your BP gives x2.0, and 16x or more gives the x3.0 cap.

A HUD panel on the left of the screen shows your current activity, Battle Power, partners, leech source and total multiplier while you train.

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
| Any race (stack forms) | Kaioken x2 50K · x3 150K · x4 400K · x10 2M · x20 6M · x100 75M · Ultimate 20M · Ultra Ego 500M / 2B · **Ultra Instinct 500M / 2B + Attendant Angel rank** |

Legendary forms and android forms are excluded by default (`excludedSkills`). They're lineage and cybernetics, not training, so
they keep DMZ's normal unlocks. Ultra Instinct also needs the Angel Path (`angelRankRequired`).

Rough solo training time with default settings: SSJ about 2-3 hours of mixed training, SSJ2 about a day. Partners and leeching cut that a lot.
Tune `statXpGrowthPerPoint`, `globalGainMultiplier` or the thresholds to taste, then run `/budokai reload`.

### Raise Dragon Mine Z's stat cap

DMZ caps total stats at `maxValue * 6` (in `config/dragonminez/general-server.json`, default 10000). That caps Battle Power:

| DMZ `maxValue` | Max base BP (about) | Reaches |
|---|---|---|
| 10,000 (default) | 1.6M | SSJ only |
| 250,000 | 78M | SSJ3 |
| 1,000,000 | 414M | SSJ4 |
| 4,000,000 | 2.2B | everything incl. Ultra Instinct |

Set it with DMZ's `/dmzconfig` command or by editing the file. The server log and `/budokai forms` warn while it's below
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
