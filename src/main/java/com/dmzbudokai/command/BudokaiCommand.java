package com.dmzbudokai.command;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.angel.AngelPath;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.compat.NoeaCompat;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dmzbudokai.data.TrainingDataProvider;
import com.dmzbudokai.forms.FormEvents;
import com.dmzbudokai.forms.FormUnlockConfig;
import com.dmzbudokai.forms.FormUnlocks;
import com.dmzbudokai.training.TrainingManager;
import com.dragonminez.common.stats.StatsData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

/**
 * /budokai                       – your training summary
 * /budokai angel join|leave|info – the Angel Path
 * /budokai forms                 – every transformation for your race and the BP it needs
 * /budokai reload                – reload form_unlocks.json (op level 2)
 * /budokai admin angelrank <player> <rank>  (op level 2)
 */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class BudokaiCommand {
	private static final String[] STATS = {"str", "skp", "res", "vit", "pwr", "ene"};

	private BudokaiCommand() {
	}

	@SubscribeEvent
	public static void register(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(Commands.literal("budokai")
				.executes(BudokaiCommand::info)
				.then(Commands.literal("angel")
						.executes(BudokaiCommand::angelInfo)
						.then(Commands.literal("info").executes(BudokaiCommand::angelInfo))
						.then(Commands.literal("join").executes(ctx -> {
							AngelPath.join(ctx.getSource().getPlayerOrException());
							return 1;
						}))
						.then(Commands.literal("leave").executes(ctx -> {
							AngelPath.leave(ctx.getSource().getPlayerOrException(), false);
							return 1;
						})))
				.then(Commands.literal("forms").executes(BudokaiCommand::forms))
				.then(Commands.literal("reload")
						.requires(source -> source.hasPermission(2))
						.executes(BudokaiCommand::reload))
				.then(Commands.literal("admin")
						.requires(source -> source.hasPermission(2))
						.then(Commands.literal("angelrank")
								.then(Commands.argument("player", EntityArgument.player())
										.then(Commands.argument("rank", IntegerArgumentType.integer(0, 99))
												.executes(BudokaiCommand::setAngelRank))))));
	}

	private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		Optional<StatsData> statsOpt = DmzBridge.stats(player);
		Optional<TrainingData> dataOpt = TrainingDataProvider.get(player);
		if (statsOpt.isEmpty() || dataOpt.isEmpty() || !DmzBridge.hasCharacter(statsOpt.get())) {
			ctx.getSource().sendFailure(Component.translatable("dmzbudokai.message.no_character"));
			return 0;
		}
		StatsData stats = statsOpt.get();
		TrainingData data = dataOpt.get();

		send(ctx, Component.translatable("dmzbudokai.info.header").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		send(ctx, Component.translatable("dmzbudokai.info.bp",
				AngelPath.formatBp(DmzBridge.currentBattlePower(stats)),
				AngelPath.formatBp(DmzBridge.baseBattlePower(stats))).withStyle(ChatFormatting.YELLOW));

		for (String stat : STATS) {
			int value = DmzBridge.statValue(stats, stat);
			double banked = data.getStatXp(stat);
			double needed = TrainingManager.costForNextPoint(value);
			int percent = (int) Math.min(100, Math.floor(banked / needed * 100.0));
			send(ctx, Component.translatable("dmzbudokai.info.stat",
					Component.translatable("dmzbudokai.stat." + stat), value, percent).withStyle(ChatFormatting.GRAY));
		}

		if (BudokaiConfig.DISABLE_TRAINING_POINTS.get()) {
			send(ctx, Component.translatable("dmzbudokai.info.tp_disabled").withStyle(ChatFormatting.DARK_GRAY));
		}
		if (data.isAngel()) {
			send(ctx, Component.translatable("dmzbudokai.info.angel", Component.translatable(AngelPath.rankKey(data.getAngelRank())))
					.withStyle(ChatFormatting.AQUA));
		}
		return 1;
	}

	private static int angelInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		Optional<StatsData> statsOpt = DmzBridge.stats(player);
		Optional<TrainingData> dataOpt = TrainingDataProvider.get(player);
		if (statsOpt.isEmpty() || dataOpt.isEmpty()) return 0;
		StatsData stats = statsOpt.get();
		TrainingData data = dataOpt.get();

		send(ctx, Component.translatable("dmzbudokai.angel.info.header").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
		send(ctx, Component.translatable("dmzbudokai.angel.info.lore").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		if (NoeaCompat.isNoeaLoaded()) {
			send(ctx, Component.translatable("dmzbudokai.angel.info.noea").withStyle(ChatFormatting.DARK_AQUA));
		}

		if (!data.isAngel()) {
			Component blocker = AngelPath.joinBlocker(player, stats, data);
			if (blocker == null) {
				send(ctx, Component.translatable("dmzbudokai.angel.info.can_join").withStyle(ChatFormatting.GREEN));
			} else {
				send(ctx, blocker.copy().withStyle(ChatFormatting.RED));
			}
			return 1;
		}

		int rank = data.getAngelRank();
		send(ctx, Component.translatable("dmzbudokai.angel.info.rank", Component.translatable(AngelPath.rankKey(rank)), rank)
				.withStyle(ChatFormatting.AQUA));
		send(ctx, Component.translatable("dmzbudokai.angel.info.alignment", DmzBridge.alignment(stats),
				BudokaiConfig.ANGEL_FALL_ALIGNMENT.get()).withStyle(ChatFormatting.GRAY));

		int next = rank + 1;
		if (next <= AngelPath.maxRank()) {
			send(ctx, Component.translatable("dmzbudokai.angel.info.next",
					Component.translatable(AngelPath.rankKey(next)),
					(int) data.getDivineXp(), (int) AngelPath.divineXpForRank(next),
					AngelPath.formatBp(DmzBridge.baseBattlePower(stats)), AngelPath.formatBp(AngelPath.bpForRank(next)))
					.withStyle(ChatFormatting.GRAY));
		} else {
			send(ctx, Component.translatable("dmzbudokai.angel.info.max").withStyle(ChatFormatting.GOLD));
		}
		return 1;
	}

	private static int setAngelRank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
		int rank = Math.min(IntegerArgumentType.getInteger(ctx, "rank"), AngelPath.maxRank());
		if (rank == 0) {
			AngelPath.leave(target, false);
			TrainingDataProvider.get(target).ifPresent(data -> data.setAngelRejoinAtMs(0L));
		} else {
			TrainingDataProvider.get(target).ifPresent(data -> data.setAngelRank(rank));
			TrainingDataProvider.get(target).ifPresent(data -> AngelPath.tick(target, data));
		}
		ctx.getSource().sendSuccess(() -> Component.translatable("dmzbudokai.admin.angelrank",
				target.getDisplayName(), rank), true);
		return 1;
	}

	private static int forms(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		Optional<StatsData> statsOpt = DmzBridge.stats(player);
		Optional<TrainingData> dataOpt = TrainingDataProvider.get(player);
		if (statsOpt.isEmpty() || dataOpt.isEmpty() || !DmzBridge.hasCharacter(statsOpt.get())) {
			ctx.getSource().sendFailure(Component.translatable("dmzbudokai.message.no_character"));
			return 0;
		}
		StatsData stats = statsOpt.get();
		TrainingData data = dataOpt.get();
		double bp = DmzBridge.baseBattlePower(stats);

		send(ctx, Component.translatable("dmzbudokai.forms.header", DmzBridge.race(stats), FormUnlocks.formatBp(bp))
				.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		if (!BudokaiConfig.BP_FORM_UNLOCKS.get()) {
			send(ctx, Component.translatable("dmzbudokai.forms.disabled").withStyle(ChatFormatting.GRAY));
		}

		List<FormUnlocks.FormEntry> entries = FormUnlocks.entriesFor(stats);
		if (entries.isEmpty()) {
			send(ctx, Component.translatable("dmzbudokai.forms.none").withStyle(ChatFormatting.GRAY));
		}
		for (FormUnlocks.FormEntry entry : entries) {
			String id = entry.group() + "." + entry.form();
			if (entry.excluded()) {
				send(ctx, Component.translatable("dmzbudokai.forms.line.excluded", id, entry.skill())
						.withStyle(ChatFormatting.DARK_GRAY));
				continue;
			}
			boolean owned = DmzBridge.skillLevel(stats, entry.skill()) >= entry.level();
			boolean met = FormUnlocks.meets(entry, bp, data);
			ChatFormatting color = owned ? ChatFormatting.GREEN : met ? ChatFormatting.YELLOW : ChatFormatting.RED;
			Component line = Component.translatable(owned ? "dmzbudokai.forms.line.owned" : "dmzbudokai.forms.line.locked",
					id, FormUnlocks.formatBp(entry.bp()));
			if (entry.angelRank() > 0) {
				line = line.copy().append(Component.translatable("dmzbudokai.forms.line.angel",
						Component.translatable(AngelPath.rankKey(entry.angelRank()))));
			}
			if (!entry.master().isEmpty()) {
				boolean metMaster = data.hasMetMaster(entry.master());
				line = line.copy().append(Component.translatable(metMaster ? "dmzbudokai.forms.line.master_met" : "dmzbudokai.forms.line.master",
						FormEvents.masterName(entry.master())));
			}
			send(ctx, line.copy().withStyle(color));
		}

		if (FormEvents.isStatCapTooLow()) {
			send(ctx, Component.translatable("dmzbudokai.forms.cap_warning", DmzBridge.dmzMaxValue(),
					BudokaiConfig.RECOMMENDED_DMZ_MAX_VALUE.get()).withStyle(ChatFormatting.DARK_RED));
		}
		return 1;
	}

	private static int reload(CommandContext<CommandSourceStack> ctx) {
		String error = FormUnlockConfig.load();
		if (error != null) {
			ctx.getSource().sendFailure(Component.translatable("dmzbudokai.reload.failed", error));
			return 0;
		}
		ctx.getSource().sendSuccess(() -> Component.translatable("dmzbudokai.reload.done"), true);
		return 1;
	}

	private static void send(CommandContext<CommandSourceStack> ctx, Component message) {
		ctx.getSource().sendSuccess(() -> message, false);
	}
}
