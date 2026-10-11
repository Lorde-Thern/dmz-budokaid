package com.dmzbudokai.content;

import com.dmzbudokai.training.TrainingActivity;
import com.dmzbudokai.training.TrainingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Punch it (left-click) to train Strength and Strike Power.
 * It can't be broken by punching; sneak while mining it to pick it back up.
 */
public class PunchingBagBlock extends Block {
	private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);

	public PunchingBagBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		return player.isShiftKeyDown() ? super.getDestroyProgress(state, player, level, pos) : 0.0F;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void attack(BlockState state, Level level, BlockPos pos, Player player) {
		if (player.isShiftKeyDown() || !(player instanceof ServerPlayer serverPlayer)) return;

		if (TrainingManager.train(serverPlayer, TrainingActivity.PUNCHING_BAG)) {
			level.playSound(null, pos, SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.8F,
					0.9F + level.random.nextFloat() * 0.2F);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
						6, 0.25, 0.25, 0.25, 0.1);
			}
		}
	}
}
