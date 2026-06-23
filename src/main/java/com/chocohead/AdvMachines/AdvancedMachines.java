package com.chocohead.AdvMachines;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Advanced Machines — faster, hotter IC2 machines. Ported from the classic 1.12.2
 * addon to Minecraft 1.20.1 / IC2: Refactored.
 */
@Mod(AdvancedMachines.MODID)
public final class AdvancedMachines {
	public static final String MODID = "advanced_machines";
	public static final Logger LOGGER = LogUtils.getLogger();

	public AdvancedMachines() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		AdvMachinesBlocks.register(modBus);
		modBus.addListener(this::commonSetup);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(AdvRecipes::registerExtraRecipes);
		LOGGER.info("Advanced Machines {} loaded.", MODID);
	}
}
