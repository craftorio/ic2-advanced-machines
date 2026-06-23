package com.chocohead.AdvMachines.api;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import java.util.Collection;
import net.minecraft.item.ItemStack;

public interface IDualMachineRecipeManager extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> {
   IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> getDefault();

   Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getExtraRecipes();
}
