package com.dmzbudokai.registry;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.content.PunchingBagBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BudokaiBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DMZBudokai.MOD_ID);

	public static final RegistryObject<Block> PUNCHING_BAG = BLOCKS.register("punching_bag",
			() -> new PunchingBagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.strength(1.5F, 6.0F)
					.sound(SoundType.WOOL)
					.noOcclusion()));

	private BudokaiBlocks() {
	}
}
