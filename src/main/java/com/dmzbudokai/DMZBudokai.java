package com.dmzbudokai;

import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.network.BudokaiNetwork;
import com.dmzbudokai.registry.BudokaiBlocks;
import com.dmzbudokai.registry.BudokaiItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DMZ Budokai: a Dragon Mine Z addon that replaces Training Points with Xeno-style training.
 * <p>
 * Training activities raise DMZ stats directly (and with them Battle Power). Training near other
 * players gives a partner bonus, and training near someone far stronger gives a leech bonus.
 * The Angel Path is an optional progression path built on top of the training system.
 */
@Mod(DMZBudokai.MOD_ID)
public class DMZBudokai {
	public static final String MOD_ID = "dmzbudokai";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public DMZBudokai() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		BudokaiBlocks.BLOCKS.register(modBus);
		BudokaiItems.ITEMS.register(modBus);

		modBus.addListener(this::commonSetup);
		modBus.addListener(this::addCreativeTabContents);

		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, BudokaiConfig.SPEC);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(BudokaiNetwork::register);
	}

	private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			event.accept(BudokaiItems.PUNCHING_BAG);
		}
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(BudokaiItems.TRAINING_WEIGHTS);
			event.accept(BudokaiItems.ANGEL_STAFF);
		}
	}
}
