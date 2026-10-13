package com.dmzbudokai.data;

import com.dmzbudokai.angel.AngelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Per-player training state. Saved fields are the stat XP banks and Angel Path progress;
 * everything else is short-lived runtime state that resets on relog.
 */
public class TrainingData {
	// Saved
	private final Map<String, Double> statXp = new HashMap<>();
	private int angelRank;
	private double divineXp;
	private long angelRejoinAtMs;
	private boolean haloForcedByAngel;
	private final Map<String, Integer> announcedFormLevels = new HashMap<>();
	private final Set<String> metMasters = new HashSet<>();
	private final AngelState angel = new AngelState();

	// Runtime
	private long lastRepTick = Long.MIN_VALUE / 2;
	private long lastPunchTick = Long.MIN_VALUE / 2;
	private long lastCombatTick = Long.MIN_VALUE / 2;
	private long lastHurtTick = Long.MIN_VALUE / 2;
	private long mentoringUntilTick = Long.MIN_VALUE / 2;
	private long dodgeReadyTick;
	private long lastHudSyncTick = Long.MIN_VALUE / 2;
	private int meditationTicks;
	private Vec3 meditationAnchor;
	private double sprintDistance;
	private Vec3 lastSprintPos;

	public double getStatXp(String stat) {
		return statXp.getOrDefault(stat, 0.0);
	}

	public void setStatXp(String stat, double xp) {
		statXp.put(stat, Math.max(0.0, xp));
	}

	/** Angel Path details: Whis's acceptance, Divine Favor, trials, abilities. */
	public AngelState angel() { return angel; }

	public int getAngelRank() { return angelRank; }
	public void setAngelRank(int angelRank) { this.angelRank = Math.max(0, angelRank); }
	public boolean isAngel() { return angelRank > 0; }

	public double getDivineXp() { return divineXp; }
	public void setDivineXp(double divineXp) { this.divineXp = Math.max(0.0, divineXp); }

	public long getAngelRejoinAtMs() { return angelRejoinAtMs; }
	public void setAngelRejoinAtMs(long angelRejoinAtMs) { this.angelRejoinAtMs = angelRejoinAtMs; }

	public boolean isHaloForcedByAngel() { return haloForcedByAngel; }
	public void setHaloForcedByAngel(boolean haloForcedByAngel) { this.haloForcedByAngel = haloForcedByAngel; }

	/** Highest form skill level we've already told the player about (so re-unlocks after a DMZ reload stay quiet). */
	public int getAnnouncedFormLevel(String skill) { return announcedFormLevels.getOrDefault(skill, 0); }
	public void setAnnouncedFormLevel(String skill, int level) { announcedFormLevels.put(skill, level); }

	/** Master NPCs (entity ids) this player has talked to. */
	public boolean hasMetMaster(String entityId) { return metMasters.contains(entityId); }
	public boolean addMetMaster(String entityId) { return metMasters.add(entityId); }

	public long getLastRepTick() { return lastRepTick; }
	public void setLastRepTick(long lastRepTick) { this.lastRepTick = lastRepTick; }

	public long getLastPunchTick() { return lastPunchTick; }
	public void setLastPunchTick(long lastPunchTick) { this.lastPunchTick = lastPunchTick; }

	public long getLastCombatTick() { return lastCombatTick; }
	public void setLastCombatTick(long lastCombatTick) { this.lastCombatTick = lastCombatTick; }

	public long getLastHurtTick() { return lastHurtTick; }
	public void setLastHurtTick(long lastHurtTick) { this.lastHurtTick = lastHurtTick; }

	public long getMentoringUntilTick() { return mentoringUntilTick; }
	public void setMentoringUntilTick(long mentoringUntilTick) { this.mentoringUntilTick = mentoringUntilTick; }

	public long getDodgeReadyTick() { return dodgeReadyTick; }
	public void setDodgeReadyTick(long dodgeReadyTick) { this.dodgeReadyTick = dodgeReadyTick; }

	public long getLastHudSyncTick() { return lastHudSyncTick; }
	public void setLastHudSyncTick(long lastHudSyncTick) { this.lastHudSyncTick = lastHudSyncTick; }

	public int getMeditationTicks() { return meditationTicks; }
	public void setMeditationTicks(int meditationTicks) { this.meditationTicks = meditationTicks; }

	public Vec3 getMeditationAnchor() { return meditationAnchor; }
	public void setMeditationAnchor(Vec3 meditationAnchor) { this.meditationAnchor = meditationAnchor; }

	public double getSprintDistance() { return sprintDistance; }
	public void setSprintDistance(double sprintDistance) { this.sprintDistance = sprintDistance; }

	public Vec3 getLastSprintPos() { return lastSprintPos; }
	public void setLastSprintPos(Vec3 lastSprintPos) { this.lastSprintPos = lastSprintPos; }

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		CompoundTag xp = new CompoundTag();
		statXp.forEach(xp::putDouble);
		tag.put("StatXp", xp);
		tag.putInt("AngelRank", angelRank);
		tag.putDouble("DivineXp", divineXp);
		tag.putLong("AngelRejoinAt", angelRejoinAtMs);
		tag.putBoolean("HaloForcedByAngel", haloForcedByAngel);
		CompoundTag forms = new CompoundTag();
		announcedFormLevels.forEach(forms::putInt);
		tag.put("AnnouncedForms", forms);
		ListTag masters = new ListTag();
		metMasters.forEach(id -> masters.add(StringTag.valueOf(id)));
		tag.put("MetMasters", masters);
		tag.put("Angel", angel.save());
		return tag;
	}

	public void load(CompoundTag tag) {
		statXp.clear();
		CompoundTag xp = tag.getCompound("StatXp");
		for (String key : xp.getAllKeys()) {
			statXp.put(key, xp.getDouble(key));
		}
		angelRank = tag.getInt("AngelRank");
		divineXp = tag.getDouble("DivineXp");
		angelRejoinAtMs = tag.getLong("AngelRejoinAt");
		haloForcedByAngel = tag.getBoolean("HaloForcedByAngel");
		announcedFormLevels.clear();
		CompoundTag forms = tag.getCompound("AnnouncedForms");
		for (String key : forms.getAllKeys()) {
			announcedFormLevels.put(key, forms.getInt(key));
		}
		metMasters.clear();
		ListTag masters = tag.getList("MetMasters", Tag.TAG_STRING);
		for (int i = 0; i < masters.size(); i++) {
			metMasters.add(masters.getString(i));
		}
		angel.load(tag.getCompound("Angel"));
	}

	public void copyFrom(TrainingData other) {
		load(other.save());
	}
}
