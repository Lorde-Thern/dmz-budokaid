# DMZ Budokai

An addon for **[Dragon Mine Z](https://github.com/DragonMineZ/dragonminez)** (Forge, Minecraft 1.20.1) that replaces Training Points with
training like the Roblox Dragon Ball "Xeno" games. You don't earn TP and spend it in a menu. You **train**, your stats go up directly,
and your **Battle Power** rises with them.

- **Train to get stronger.** Punching bags, weights, meditation, sparring and running each raise specific DMZ stats.
- **Partner training.** Training near other players who are also training gives bonus gains.
- **Leeching.** Training near someone much stronger than you gives a large leech bonus. The stronger player gets a small mentor bonus for helping.
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
XP for next point = statCostBase + currentStat * statCostPerPoint     (defaults: 10 + stat * 0.05)
```

Higher stats need more training, the same diminishing-returns feel as the Xeno games. Gains respect Dragon Mine Z's per-stat and total
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
| `/budokai admin angelrank <player> <rank>` | Set someone's Angel rank (op level 2; 0 removes them) |

## Config

`<world>/serverconfig/dmzbudokai-server.toml`. Every gain, cost, radius, multiplier and Angel Path threshold is in there, with comments.

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
  compat/DmzBridge           every Dragon Mine Z API call, in one place
  compat/NoeaCompat          Dragon Block Noea race rules
  data/                      per-player saved training data (Forge capability)
  content/                   Punching Bag block, Training Weights item
  network/, client/          training HUD
  command/BudokaiCommand     /budokai
```

If a Dragon Mine Z update changes its API, `compat/DmzBridge.java` is the only file that should need changes.
