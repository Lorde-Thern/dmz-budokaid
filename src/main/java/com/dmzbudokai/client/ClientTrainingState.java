package com.dmzbudokai.client;

import com.dmzbudokai.network.TrainingHudPacket;
import net.minecraft.Util;

/** Latest training info from the server, for the HUD. Client-only. */
public final class ClientTrainingState {
	/** How long the HUD stays up after the last rep. */
	public static final long VISIBLE_MS = 6000L;

	private static TrainingHudPacket latest;
	private static long receivedAtMs;
	private static long lastGainAtMs;
	private static int lastGain;

	private ClientTrainingState() {
	}

	public static void update(TrainingHudPacket packet) {
		latest = packet;
		receivedAtMs = Util.getMillis();
		if (packet.pointsGained() > 0) {
			lastGain = packet.pointsGained();
			lastGainAtMs = receivedAtMs;
		}
	}

	public static TrainingHudPacket latest() {
		return latest;
	}

	public static boolean isVisible() {
		return latest != null && Util.getMillis() - receivedAtMs < VISIBLE_MS;
	}

	public static long msSinceUpdate() {
		return Util.getMillis() - receivedAtMs;
	}

	/** Points from the most recent rep that gave any, while it is still worth flashing on screen. */
	public static int recentGain() {
		return Util.getMillis() - lastGainAtMs < 2000L ? lastGain : 0;
	}
}
