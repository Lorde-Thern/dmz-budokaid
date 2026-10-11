package com.dmzbudokai.client;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.angel.AngelPath;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.network.TrainingHudPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/** Small panel on the left of the screen that shows your training bonuses while you train. */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TrainingHudOverlay {
	private static final int WHITE = 0xFFFFFF;
	private static final int GRAY = 0xAAAAAA;
	private static final int GREEN = 0x55FF55;
	private static final int GOLD = 0xFFAA00;
	private static final int AQUA = 0x55FFFF;

	private TrainingHudOverlay() {
	}

	@SubscribeEvent
	public static void register(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "training", OVERLAY);
	}

	private static final IGuiOverlay OVERLAY = (ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) -> {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.player == null || !ClientTrainingState.isVisible()) return;

		TrainingHudPacket hud = ClientTrainingState.latest();
		List<Line> lines = new ArrayList<>();

		Component title = Component.translatable("dmzbudokai.hud.training",
				Component.translatable("dmzbudokai.activity." + hud.activity()));
		int gain = ClientTrainingState.recentGain();
		if (gain > 0) title = title.copy().append(Component.literal("  +" + gain).withStyle(s -> s.withColor(GREEN)));
		lines.add(new Line(title, WHITE));

		DmzBridge.stats(mc.player).ifPresent(stats -> lines.add(new Line(
				Component.translatable("dmzbudokai.hud.bp", AngelPath.formatBp(DmzBridge.currentBattlePower(stats))), GOLD)));

		if (hud.partners() > 0) {
			lines.add(new Line(Component.translatable("dmzbudokai.hud.partners", hud.partners(), fmt(hud.partnerMult())), GREEN));
		} else {
			lines.add(new Line(Component.translatable("dmzbudokai.hud.solo"), GRAY));
		}
		if (hud.leechMult() > 1.0F) {
			lines.add(new Line(Component.translatable("dmzbudokai.hud.leech", hud.leechFrom(), fmt(hud.leechMult())), GOLD));
		}
		if (hud.mentorMult() > 1.0F) {
			lines.add(new Line(Component.translatable("dmzbudokai.hud.mentor", fmt(hud.mentorMult())), GREEN));
		}
		if (hud.angelMult() > 1.0F) {
			lines.add(new Line(Component.translatable("dmzbudokai.hud.angel_bonus", fmt(hud.angelMult())), AQUA));
		}
		if (hud.angelRank() > 0) {
			lines.add(new Line(Component.translatable(AngelPath.rankKey(hud.angelRank())), AQUA));
		}
		lines.add(new Line(Component.translatable("dmzbudokai.hud.total", fmt(hud.totalMult())), WHITE));

		Font font = mc.font;
		long sinceUpdate = ClientTrainingState.msSinceUpdate();
		long fadeStart = ClientTrainingState.VISIBLE_MS - 1000L;
		float alpha = sinceUpdate < fadeStart ? 1.0F : 1.0F - (sinceUpdate - fadeStart) / 1000.0F;
		int alphaBits = Mth.clamp((int) (alpha * 255.0F), 8, 255) << 24;

		int x = 6;
		int y = height / 2 - lines.size() * (font.lineHeight + 1) / 2;
		int boxWidth = 0;
		for (Line line : lines) boxWidth = Math.max(boxWidth, font.width(line.text()));
		int boxHeight = lines.size() * (font.lineHeight + 1);
		graphics.fill(x - 3, y - 3, x + boxWidth + 3, y + boxHeight + 2, ((int) (alpha * 0x80) << 24));

		for (Line line : lines) {
			graphics.drawString(font, line.text(), x, y, line.color() | alphaBits, true);
			y += font.lineHeight + 1;
		}
	};

	private static String fmt(float mult) {
		return String.format("x%.2f", mult);
	}

	private record Line(Component text, int color) {
	}
}
