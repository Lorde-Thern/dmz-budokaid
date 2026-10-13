package com.dmzbudokai.content;

import com.dmzbudokai.angel.AngelAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Given to angels when they reach Attendant Angel. Right-click another player to bless their training.
 */
public class AngelStaffItem extends Item {
	public AngelStaffItem(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.EPIC));
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof Player)) return InteractionResult.PASS;
		if (player instanceof ServerPlayer angel && target instanceof ServerPlayer targetPlayer) {
			AngelAbilities.bless(angel, targetPlayer);
		}
		return InteractionResult.sidedSuccess(player.level().isClientSide);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.dmzbudokai.angel_staff.tooltip"));
	}
}
