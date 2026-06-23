package com.chocohead.AdvMachines.api;

import com.google.common.collect.Iterables;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.recipe.BasicMachineRecipeManager;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public enum Recipes implements IDualMachineRecipeManager {
   rotaryMacerator(ic2.api.recipe.Recipes.macerator),
   singularityCompressor(ic2.api.recipe.Recipes.compressor),
   centrifugeExtractor(ic2.api.recipe.Recipes.extractor),
   compactingRecycler(ic2.api.recipe.Recipes.recycler),
   liquescentExtruder(ic2.api.recipe.Recipes.metalformerExtruding),
   impellerizedRoller(ic2.api.recipe.Recipes.metalformerRolling),
   waterJetCutter(ic2.api.recipe.Recipes.metalformerCutting),
   thermalWasher(ic2.api.recipe.Recipes.oreWashing);

   public static final Collection<ItemStack> SKIP = Collections.emptyList();
   private final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> base;
   private final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> extra;

   private Recipes(IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> base) {
      this(base, new BasicMachineRecipeManager());
   }

   private Recipes(
      IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> base, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> extra
   ) {
      this.base = base;
      this.extra = extra;
   }

   public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
      return this.extra.addRecipe(input, output, metadata, replace);
   }

   public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> out = this.extra.apply(input, acceptTest);
      if (out == null) {
         out = this.base.apply(input, acceptTest);
      } else if (((Collection)out.getOutput()).isEmpty()) {
         return null;
      }

      return out;
   }

   public boolean isIterable() {
      return true;
   }

   public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
      return this.base.isIterable() ? Iterables.concat(this.base.getRecipes(), this.getExtraRecipes()) : this.getExtraRecipes();
   }

   @Override
   public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getExtraRecipes() {
      return this.extra.getRecipes();
   }

   @Override
   public IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> getDefault() {
      return this.base;
   }
}
