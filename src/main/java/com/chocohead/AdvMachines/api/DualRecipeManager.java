package com.chocohead.AdvMachines.api;

import java.util.Collection;

import com.google.common.collect.Iterables;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;

import net.minecraft.world.item.ItemStack;

/**
 * Combines a base IC2 recipe manager (e.g. the vanilla recycler recipes) with an
 * {@code extra} manager holding Advanced Machines specific recipes. The extra manager
 * is consulted first so it can add to or override the base set.
 */
public class DualRecipeManager implements IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> {
	private final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> base;
	private final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> extra;

	public DualRecipeManager(IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> base,
			IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> extra) {
		this.base = base;
		this.extra = extra;
	}

	@Override
	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> out = this.extra.apply(input, acceptTest);
		return out != null ? out : this.base.apply(input, acceptTest);
	}

	@Override
	public boolean isIterable() {
		return true;
	}

	@Override
	public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
		if (this.base.isIterable()) {
			return Iterables.concat(this.base.getRecipes(), this.extra.getRecipes());
		}

		return this.extra.getRecipes();
	}
}
