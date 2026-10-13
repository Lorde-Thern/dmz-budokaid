package com.dmzbudokai.angel;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Angel Path progress beyond rank and Divine XP: Whis's acceptance, Divine Favor, trial progress,
 * ability cooldowns and blessings. Saved with the rest of the player's training data.
 */
public class AngelState {
	public static final double MAX_FAVOR = 100.0;

	// Saved
	private boolean mentorAccepted;
	private double favor = MAX_FAVOR;
	private int fallCount;
	private int bestMeditationTicks;
	private int mentorReps;
	private int instinctDodges;
	private long rewindReadyAtMs;
	private long blessCooldownUntilMs;
	private long blessedUntilMs;
	private double blessingMultiplier = 1.0;
	private String blessedBy = "";
	private boolean staffGiven;

	// Runtime
	private int meditationStreakTicks;
	private boolean disfavorWarned;
	private final Deque<Snapshot> snapshots = new ArrayDeque<>();

	/** Where the player was and how healthy they were, for Temporal Do-Over. */
	public record Snapshot(long gameTime, ResourceKey<Level> dimension, Vec3 pos, float health) {
	}

	public boolean isMentorAccepted() { return mentorAccepted; }
	public void setMentorAccepted(boolean mentorAccepted) { this.mentorAccepted = mentorAccepted; }

	public double getFavor() { return favor; }
	public void setFavor(double favor) { this.favor = Math.max(0.0, Math.min(MAX_FAVOR, favor)); }
	public void addFavor(double amount) { setFavor(favor + amount); }

	public int getFallCount() { return fallCount; }
	public void setFallCount(int fallCount) { this.fallCount = Math.max(0, fallCount); }

	public int getBestMeditationTicks() { return bestMeditationTicks; }
	public void setBestMeditationTicks(int ticks) { this.bestMeditationTicks = Math.max(0, ticks); }

	public int getMentorReps() { return mentorReps; }
	public void addMentorRep() { mentorReps++; }

	public int getInstinctDodges() { return instinctDodges; }
	public void addInstinctDodge() { instinctDodges++; }

	public long getRewindReadyAtMs() { return rewindReadyAtMs; }
	public void setRewindReadyAtMs(long ms) { this.rewindReadyAtMs = ms; }

	public long getBlessCooldownUntilMs() { return blessCooldownUntilMs; }
	public void setBlessCooldownUntilMs(long ms) { this.blessCooldownUntilMs = ms; }

	public boolean isBlessed() { return System.currentTimeMillis() < blessedUntilMs; }
	public long getBlessedUntilMs() { return blessedUntilMs; }
	public double getBlessingMultiplier() { return isBlessed() ? blessingMultiplier : 1.0; }
	public String getBlessedBy() { return blessedBy; }

	public void bless(long untilMs, double multiplier, String byName) {
		this.blessedUntilMs = untilMs;
		this.blessingMultiplier = Math.max(1.0, multiplier);
		this.blessedBy = byName == null ? "" : byName;
	}

	public boolean isStaffGiven() { return staffGiven; }
	public void setStaffGiven(boolean staffGiven) { this.staffGiven = staffGiven; }

	public int getMeditationStreakTicks() { return meditationStreakTicks; }
	public void setMeditationStreakTicks(int ticks) { this.meditationStreakTicks = Math.max(0, ticks); }

	public boolean isDisfavorWarned() { return disfavorWarned; }
	public void setDisfavorWarned(boolean warned) { this.disfavorWarned = warned; }

	public Deque<Snapshot> snapshots() { return snapshots; }

	/** Clears everything a fallen or departing angel loses. Whis's acceptance and the fall count stay. */
	public void resetProgress() {
		favor = MAX_FAVOR;
		bestMeditationTicks = 0;
		mentorReps = 0;
		instinctDodges = 0;
		rewindReadyAtMs = 0L;
		blessCooldownUntilMs = 0L;
		disfavorWarned = false;
		snapshots.clear();
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("MentorAccepted", mentorAccepted);
		tag.putDouble("Favor", favor);
		tag.putInt("FallCount", fallCount);
		tag.putInt("BestMeditation", bestMeditationTicks);
		tag.putInt("MentorReps", mentorReps);
		tag.putInt("InstinctDodges", instinctDodges);
		tag.putLong("RewindReadyAt", rewindReadyAtMs);
		tag.putLong("BlessCooldownUntil", blessCooldownUntilMs);
		tag.putLong("BlessedUntil", blessedUntilMs);
		tag.putDouble("BlessingMultiplier", blessingMultiplier);
		tag.putString("BlessedBy", blessedBy);
		tag.putBoolean("StaffGiven", staffGiven);
		return tag;
	}

	public void load(CompoundTag tag) {
		mentorAccepted = tag.getBoolean("MentorAccepted");
		favor = tag.contains("Favor") ? tag.getDouble("Favor") : MAX_FAVOR;
		fallCount = tag.getInt("FallCount");
		bestMeditationTicks = tag.getInt("BestMeditation");
		mentorReps = tag.getInt("MentorReps");
		instinctDodges = tag.getInt("InstinctDodges");
		rewindReadyAtMs = tag.getLong("RewindReadyAt");
		blessCooldownUntilMs = tag.getLong("BlessCooldownUntil");
		blessedUntilMs = tag.getLong("BlessedUntil");
		blessingMultiplier = tag.contains("BlessingMultiplier") ? tag.getDouble("BlessingMultiplier") : 1.0;
		blessedBy = tag.getString("BlessedBy");
		staffGiven = tag.getBoolean("StaffGiven");
	}
}
