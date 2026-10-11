package com.dmzbudokai.content;

import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.training.TrainingActivity;
import com.dmzbudokai.training.TrainingManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Hold right-click to lift. Every few seconds of lifting is one rep of Resistance + Vitality training.
 */
public class TrainingWeightsItem extends Item {
	private static final int USE_DURATION = 72000;

	public TrainingWeightsItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(player.getItemInHand(hand));
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return USE_DURATION;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		if (level.isClientSide || !(entity instanceof ServerPlayer player)) return;

		int ticksUsed = USE_DURATION - remainingUseDuration;
		if (ticksUsed <= 0 || ticksUsed % BudokaiConfig.WEIGHTS_REP_TICKS.get() != 0) return;

		if (TrainingManager.train(player, TrainingActivity.WEIGHTS)) {
			level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 0.6F, 0.8F);
		} else {
			player.stopUsingItem();
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.dmzbudokai.training_weights.tooltip"));
	}
}
