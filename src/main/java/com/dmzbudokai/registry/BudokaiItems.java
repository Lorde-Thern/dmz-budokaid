package com.dmzbudokai.registry;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.content.TrainingWeightsItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BudokaiItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DMZBudokai.MOD_ID);

	public static final RegistryObject<Item> PUNCHING_BAG = ITEMS.register("punching_bag",
			() -> new BlockItem(BudokaiBlocks.PUNCHING_BAG.get(), new Item.Properties()));

	public static final RegistryObject<Item> TRAINING_WEIGHTS = ITEMS.register("training_weights",
			() -> new TrainingWeightsItem(new Item.Properties().stacksTo(1)));

	private BudokaiItems() {
	}
}
