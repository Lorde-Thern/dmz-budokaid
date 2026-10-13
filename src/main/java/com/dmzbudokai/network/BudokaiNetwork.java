package com.dmzbudokai.network;

import com.dmzbudokai.DMZBudokai;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BudokaiNetwork {
	private static final String PROTOCOL = "2";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(DMZBudokai.MOD_ID, "main"),
			() -> PROTOCOL,
			PROTOCOL::equals,
			PROTOCOL::equals);

	private static boolean registered;

	private BudokaiNetwork() {
	}

	public static void register() {
		if (registered) return;
		registered = true;

		// Packet ids follow registration order: only ever append new packets.
		int id = 0;
		CHANNEL.messageBuilder(TrainingHudPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(TrainingHudPacket::encode)
				.decoder(TrainingHudPacket::decode)
				.consumerMainThread(TrainingHudPacket::handle)
				.add();
	}

	public static <MSG> void sendTo(ServerPlayer player, MSG message) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
	}
}
