package com.chocohead.AdvMachines;

import com.chocohead.AdvMachines.item.ItemSharpPlate;
import com.chocohead.AdvMachines.te.TileEntityCentrifugeExtractor;
import com.chocohead.AdvMachines.te.TileEntityCompactingRecycler;
import com.chocohead.AdvMachines.te.TileEntityImpellerizedRoller;
import com.chocohead.AdvMachines.te.TileEntityLiquescentExtruder;
import com.chocohead.AdvMachines.te.TileEntityRotaryMacerator;
import com.chocohead.AdvMachines.te.TileEntitySingularityCompressor;
import com.chocohead.AdvMachines.te.TileEntityThermalWasher;
import com.chocohead.AdvMachines.te.TileEntityWaterJetCutter;

import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.Ic2TileEntityBlock.DefaultDrop;
import ic2.core.util.Util;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Forge registration for all Advanced Machines content: one {@link Ic2TileEntityBlock}
 * per machine (mirroring IC2's own processing machines), a matching {@link BlockEntityType},
 * a {@link BlockItem}, plus the Sharp Plate item and a creative tab.
 */
public final class AdvMachinesBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AdvancedMachines.MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AdvancedMachines.MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
			DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AdvancedMachines.MODID);
	public static final DeferredRegister<CreativeModeTab> TABS =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdvancedMachines.MODID);

	// --- Rotary Macerator ---
	public static final RegistryObject<Block> ROTARY_MACERATOR = block("rotary_macerator", TileEntityRotaryMacerator.class);
	public static final RegistryObject<BlockEntityType<TileEntityRotaryMacerator>> BE_ROTARY_MACERATOR =
			be("rotary_macerator", ROTARY_MACERATOR, TileEntityRotaryMacerator::new);
	public static final RegistryObject<Item> ITEM_ROTARY_MACERATOR = item("rotary_macerator", ROTARY_MACERATOR);

	// --- Singularity Compressor ---
	public static final RegistryObject<Block> SINGULARITY_COMPRESSOR = block("singularity_compressor", TileEntitySingularityCompressor.class);
	public static final RegistryObject<BlockEntityType<TileEntitySingularityCompressor>> BE_SINGULARITY_COMPRESSOR =
			be("singularity_compressor", SINGULARITY_COMPRESSOR, TileEntitySingularityCompressor::new);
	public static final RegistryObject<Item> ITEM_SINGULARITY_COMPRESSOR = item("singularity_compressor", SINGULARITY_COMPRESSOR);

	// --- Centrifuge Extractor ---
	public static final RegistryObject<Block> CENTRIFUGE_EXTRACTOR = block("centrifuge_extractor", TileEntityCentrifugeExtractor.class);
	public static final RegistryObject<BlockEntityType<TileEntityCentrifugeExtractor>> BE_CENTRIFUGE_EXTRACTOR =
			be("centrifuge_extractor", CENTRIFUGE_EXTRACTOR, TileEntityCentrifugeExtractor::new);
	public static final RegistryObject<Item> ITEM_CENTRIFUGE_EXTRACTOR = item("centrifuge_extractor", CENTRIFUGE_EXTRACTOR);

	// --- Compacting Recycler ---
	public static final RegistryObject<Block> COMPACTING_RECYCLER = block("compacting_recycler", TileEntityCompactingRecycler.class);
	public static final RegistryObject<BlockEntityType<TileEntityCompactingRecycler>> BE_COMPACTING_RECYCLER =
			be("compacting_recycler", COMPACTING_RECYCLER, TileEntityCompactingRecycler::new);
	public static final RegistryObject<Item> ITEM_COMPACTING_RECYCLER = item("compacting_recycler", COMPACTING_RECYCLER);

	// --- Liquescent Extruder ---
	public static final RegistryObject<Block> LIQUESCENT_EXTRUDER = block("liquescent_extruder", TileEntityLiquescentExtruder.class);
	public static final RegistryObject<BlockEntityType<TileEntityLiquescentExtruder>> BE_LIQUESCENT_EXTRUDER =
			be("liquescent_extruder", LIQUESCENT_EXTRUDER, TileEntityLiquescentExtruder::new);
	public static final RegistryObject<Item> ITEM_LIQUESCENT_EXTRUDER = item("liquescent_extruder", LIQUESCENT_EXTRUDER);

	// --- Impellerized Roller ---
	public static final RegistryObject<Block> IMPELLERIZED_ROLLER = block("impellerized_roller", TileEntityImpellerizedRoller.class);
	public static final RegistryObject<BlockEntityType<TileEntityImpellerizedRoller>> BE_IMPELLERIZED_ROLLER =
			be("impellerized_roller", IMPELLERIZED_ROLLER, TileEntityImpellerizedRoller::new);
	public static final RegistryObject<Item> ITEM_IMPELLERIZED_ROLLER = item("impellerized_roller", IMPELLERIZED_ROLLER);

	// --- Water Jet Cutter ---
	public static final RegistryObject<Block> WATER_JET_CUTTER = block("water_jet_cutter", TileEntityWaterJetCutter.class);
	public static final RegistryObject<BlockEntityType<TileEntityWaterJetCutter>> BE_WATER_JET_CUTTER =
			be("water_jet_cutter", WATER_JET_CUTTER, TileEntityWaterJetCutter::new);
	public static final RegistryObject<Item> ITEM_WATER_JET_CUTTER = item("water_jet_cutter", WATER_JET_CUTTER);

	// --- Thermal Washer ---
	public static final RegistryObject<Block> THERMAL_WASHER = block("thermal_washer", TileEntityThermalWasher.class);
	public static final RegistryObject<BlockEntityType<TileEntityThermalWasher>> BE_THERMAL_WASHER =
			be("thermal_washer", THERMAL_WASHER, TileEntityThermalWasher::new);
	public static final RegistryObject<Item> ITEM_THERMAL_WASHER = item("thermal_washer", THERMAL_WASHER);

	// --- Sharp Plate ---
	public static final RegistryObject<Item> SHARP_PLATE = ITEMS.register("sharp_plate", ItemSharpPlate::new);

	// --- Creative tab ---
	public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.advanced_machines"))
			.icon(() -> new net.minecraft.world.item.ItemStack(ROTARY_MACERATOR.get()))
			.displayItems((params, output) -> {
				output.accept(ROTARY_MACERATOR.get());
				output.accept(SINGULARITY_COMPRESSOR.get());
				output.accept(CENTRIFUGE_EXTRACTOR.get());
				output.accept(COMPACTING_RECYCLER.get());
				output.accept(LIQUESCENT_EXTRUDER.get());
				output.accept(IMPELLERIZED_ROLLER.get());
				output.accept(WATER_JET_CUTTER.get());
				output.accept(THERMAL_WASHER.get());
				output.accept(SHARP_PLATE.get());
			})
			.build());

	private AdvMachinesBlocks() {
	}

	private static BlockBehaviour.Properties machineProps() {
		return BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_LIGHT_GRAY)
				.strength(5.0F, 10.0F)
				.requiresCorrectToolForDrops()
				.sound(SoundType.METAL);
	}

	private static RegistryObject<Block> block(String name, Class<? extends Ic2TileEntity> teClass) {
		return BLOCKS.register(name, () -> Ic2TileEntityBlock.create(machineProps(), teClass, true, DefaultDrop.AdvMachine, Util.horizontalFacings, false));
	}

	private static <T extends Ic2TileEntity> RegistryObject<BlockEntityType<T>> be(String name, RegistryObject<Block> block, BlockEntitySupplier<T> factory) {
		return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
	}

	private static RegistryObject<Item> item(String name, RegistryObject<Block> block) {
		return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
	}

	public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
		BLOCKS.register(bus);
		ITEMS.register(bus);
		BLOCK_ENTITIES.register(bus);
		TABS.register(bus);
	}
}
