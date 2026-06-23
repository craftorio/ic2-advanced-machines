package com.chocohead.AdvMachines;

import com.chocohead.AdvMachines.api.IDualMachineRecipeManager;
import com.chocohead.AdvMachines.te.AdvancedMachineTEs;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.ref.TeBlock;
import ic2.core.util.ReflectionUtil;
import ic2.jeiIntegration.recipe.machine.DynamicCategory;
import ic2.jeiIntegration.recipe.machine.IORecipeCategory;
import ic2.jeiIntegration.recipe.machine.IORecipeWrapper;
import ic2.jeiIntegration.recipe.machine.IRecipeWrapperGenerator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

@JEIPlugin
public final class JEICompat implements IModPlugin {
   public IRecipeWrapperGenerator<IDualMachineRecipeManager> recipeWrapperGenerator = new IRecipeWrapperGenerator<IDualMachineRecipeManager>() {
      private final Field recipeManager = ReflectionUtil.getField(IORecipeCategory.class, new String[]{"recipeManager"});

      public List<IRecipeWrapper> getRecipeList(IORecipeCategory<IDualMachineRecipeManager> category) {
         List<IRecipeWrapper> recipes = new ArrayList<>();

         for (MachineRecipe<IRecipeInput, Collection<ItemStack>> container : ((IDualMachineRecipeManager)ReflectionUtil.getFieldValue(
               this.recipeManager, category
            ))
            .getExtraRecipes()) {
            recipes.add(new IORecipeWrapper((IRecipeInput)container.getInput(), (Collection)container.getOutput(), category) {
            });
         }

         return recipes;
      }
   };

   public void register(IModRegistry registry) {
      registry.addRecipeCatalyst(AdvancedMachines.machines.getItemStack(AdvancedMachineTEs.rotary_macerator), new String[]{TeBlock.macerator.getName()});
      registry.addRecipeCatalyst(AdvancedMachines.machines.getItemStack(AdvancedMachineTEs.singularity_compressor), new String[]{TeBlock.compressor.getName()});
      registry.addRecipeCatalyst(AdvancedMachines.machines.getItemStack(AdvancedMachineTEs.centrifuge_extractor), new String[]{TeBlock.extractor.getName()});
      registry.addRecipeCatalyst(AdvancedMachines.machines.getItemStack(AdvancedMachineTEs.compacting_recycler), new String[]{TeBlock.recycler.getName()});
      registry.addRecipeCatalyst(
         AdvancedMachines.machines.getItemStack(AdvancedMachineTEs.liquescent_extruder), new String[]{TeBlock.metal_former.getName() + '0'}
      );
      registry.addRecipeCatalyst(
         AdvancedMachines.machines.getItemStack(AdvancedMachineTEs.impellerized_roller), new String[]{TeBlock.metal_former.getName() + '1'}
      );
      registry.addRecipeCatalyst(
         AdvancedMachines.machines.getItemStack(AdvancedMachineTEs.water_jet_cutter), new String[]{TeBlock.metal_former.getName() + '2'}
      );
      registry.addRecipeCatalyst(AdvancedMachines.machines.getItemStack(AdvancedMachineTEs.thermal_washer), new String[]{TeBlock.ore_washing_plant.getName()});
      IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

      for (AdvancedMachineTEs te : AdvancedMachineTEs.values()) {
         for (String type : te.getRecipeCategories()) {
            this.addMachineRecipes(registry, new DynamicCategory(te, te.getManager(type), guiHelper), this.recipeWrapperGenerator);
         }
      }
   }

   private <T> void addMachineRecipes(IModRegistry registry, IORecipeCategory<T> category, IRecipeWrapperGenerator<T> wrappergen) {
      List<IRecipeWrapper> recipes = wrappergen.getRecipeList(category);
      if (!recipes.isEmpty()) {
         registry.addRecipeCategories(new IRecipeCategory[]{category});
         registry.addRecipes(recipes, category.getUid());
         registry.addRecipeCatalyst(category.getBlockStack(), new String[]{category.getUid()});
      }
   }
}
