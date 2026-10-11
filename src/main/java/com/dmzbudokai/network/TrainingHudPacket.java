package com.dmzbudokai.network;

import com.dmzbudokai.client.ClientTrainingState;
import com.dmzbudokai.training.TrainingActivity;
import com.dmzbudokai.training.TrainingBonus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: what the training HUD should show after a rep. */
public record TrainingHudPacket(String activity, int partners, float partnerMult, float leechMult, String leechFrom,
								float mentorMult, float angelMult, float totalMult, int pointsGained, int angelRank) {

	public static TrainingHudPacket of(TrainingActivity activity, TrainingBonus bonus, int pointsGained, int angelRank) {
		String leechFrom = bonus.isLeeching() && bonus.leechSource() != null
				? bonus.leechSource().getGameProfile().getName() : "";
		return new TrainingHudPacket(activity.id(), bonus.partners(), (float) bonus.partnerMult(), (float) bonus.leechMult(),
				leechFrom, (float) bonus.mentorMult(), (float) bonus.angelMult(), (float) bonus.total(), pointsGained, angelRank);
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(activity, 64);
		buf.writeVarInt(partners);
		buf.writeFloat(partnerMult);
		buf.writeFloat(leechMult);
		buf.writeUtf(leechFrom, 64);
		buf.writeFloat(mentorMult);
		buf.writeFloat(angelMult);
		buf.writeFloat(totalMult);
		buf.writeVarInt(pointsGained);
		buf.writeVarInt(angelRank);
	}

	public static TrainingHudPacket decode(FriendlyByteBuf buf) {
		return new TrainingHudPacket(buf.readUtf(64), buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readUtf(64),
				buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readVarInt());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTrainingState.update(this));
		ctx.get().setPacketHandled(true);
	}
}
