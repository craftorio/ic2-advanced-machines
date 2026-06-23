package com.chocohead.AdvMachines.client;

import java.nio.file.Path;

import com.chocohead.AdvMachines.AdvConfig;
import com.chocohead.AdvMachines.AdvancedMachines;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * When {@code modernTextures} is enabled, force-loads the bundled "modern_overlay" resource pack
 * (top priority, required) so its modern machine models/textures override the classic defaults.
 * When disabled, the pack is never registered and the classic base assets show.
 */
@Mod.EventBusSubscriber(modid = AdvancedMachines.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AdvMachinesClientPacks {
	private AdvMachinesClientPacks() {
	}

	@SubscribeEvent
	public static void addPackFinders(AddPackFindersEvent event) {
		if (event.getPackType() != PackType.CLIENT_RESOURCES) {
			return;
		}

		boolean modern = AdvConfig.useModernTextures();
		AdvancedMachines.LOGGER.info("Advanced Machines texture style: {}", modern ? "modern (overlay pack enabled)" : "classic (default)");
		if (!modern) {
			return;
		}

		Path overlay = ModList.get().getModFileById(AdvancedMachines.MODID).getFile().findResource("modern_overlay");
		Pack pack = Pack.readMetaAndCreate(
				"builtin/advanced_machines_modern",
				Component.literal("Advanced Machines: Modern"),
				true, // required → always enabled while present
				id -> new PathPackResources(id, overlay, false),
				PackType.CLIENT_RESOURCES,
				Pack.Position.TOP,
				PackSource.BUILT_IN);
		event.addRepositorySource(consumer -> consumer.accept(pack));
	}
}
