package com.chocohead.AdvMachines;

import java.util.Collection;

import com.chocohead.AdvMachines.api.DualRecipeManager;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.ref.Ic2Items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Advanced Machines specific machine recipes. Most machines simply reuse their base
 * IC2 recipe manager; only the Compacting Recycler adds an extra recipe (compacting
 * 9 scrap into a scrap box), so it runs through a {@link DualRecipeManager}.
 */
public final class AdvRecipes {
	/** Extra recipes layered on top of the vanilla recycler for the Compacting Recycler. */
	public static final BasicMachineRecipeManager RECYCLER_EXTRA = new BasicMachineRecipeManager();

	private static volatile IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recyclerDual;

	/** Recipe manager getter handed to the Compacting Recycler's input slot. */
	public static final Recipes.IGetter<IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>> COMPACTING_RECYCLER = level -> {
		IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> dual = recyclerDual;
		if (dual == null) {
			dual = recyclerDual = new DualRecipeManager(Recipes.recycler, RECYCLER_EXTRA);
		}

		return dual;
	};

	private AdvRecipes() {
	}

	/** Register the programmatic machine recipes. Call during common setup. */
	public static void registerExtraRecipes() {
		// 9 scrap -> 1 scrap box, always (chance 1).
		// Use forIngredient (not forStack): IC2's RecipeInputItemStack.listStacks() returns an
		// immutable List.of(...), and BasicMachineRecipeManager.addRecipe calls getInputs().replaceAll(),
		// which throws UnsupportedOperationException. forIngredient is backed by a mutable-enough list.
		RECYCLER_EXTRA.addRecipe(Recipes.inputFactory.forIngredient(Ingredient.of(Ic2Items.SCRAP), 9), chance(1), false, new ItemStack(Ic2Items.SCRAP_BOX));
	}

	private static CompoundTag chance(int value) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("chance", value);
		return tag;
	}
}
