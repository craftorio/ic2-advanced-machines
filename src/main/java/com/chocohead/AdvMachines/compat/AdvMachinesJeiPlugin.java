package com.chocohead.AdvMachines.compat;

import com.chocohead.AdvMachines.AdvMachinesBlocks;
import com.chocohead.AdvMachines.AdvancedMachines;

import ic2.integration.jei.recipe.machine.IORecipeWrapper;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * JEI integration. The Advanced Machines reuse IC2's recipe managers, so their recipes already
 * appear under IC2's own JEI categories — this plugin additionally registers each machine as a
 * <em>catalyst</em> for the matching category, so looking a machine up in JEI shows its recipes
 * and the machine is listed as a valid crafter in each recipe view.
 *
 * <p>Loaded only when JEI is present (JEI scans for {@link JeiPlugin}); referencing the JEI API
 * here is safe at runtime without JEI because the class simply is not loaded.
 *
 * <p>The Compacting Recycler is intentionally absent: IC2 1.20.1 has no JEI category for the
 * recycler (it turns arbitrary items into scrap), so there is nothing to attach it to, and its
 * "9 scrap → scrap box" extra recipe is therefore not shown in JEI.
 */
@JeiPlugin
public class AdvMachinesJeiPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(AdvancedMachines.MODID, "jei");
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		catalyst(registration, AdvMachinesBlocks.ROTARY_MACERATOR.get(), "macerator");
		catalyst(registration, AdvMachinesBlocks.SINGULARITY_COMPRESSOR.get(), "compressor");
		catalyst(registration, AdvMachinesBlocks.CENTRIFUGE_EXTRACTOR.get(), "extractor");
		catalyst(registration, AdvMachinesBlocks.LIQUESCENT_EXTRUDER.get(), "metal_former_extruding");
		catalyst(registration, AdvMachinesBlocks.IMPELLERIZED_ROLLER.get(), "metal_former_rolling");
		catalyst(registration, AdvMachinesBlocks.WATER_JET_CUTTER.get(), "metal_former_cutting");
		catalyst(registration, AdvMachinesBlocks.THERMAL_WASHER.get(), "ore_washer");
	}

	private static void catalyst(IRecipeCatalystRegistration registration, Block machine, String ic2Category) {
		registration.addRecipeCatalyst(new ItemStack(machine), RecipeType.create("ic2", ic2Category, IORecipeWrapper.class));
	}
}
