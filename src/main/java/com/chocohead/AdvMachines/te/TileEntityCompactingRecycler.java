package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.Recipes;
import ic2.api.item.IC2Items;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.item.ItemStack;

public class TileEntityCompactingRecycler extends TileEntityHeatingMachine {
   private static final byte OUTPUTS = 1;
   protected static final ItemStack SCRAP = IC2Items.getItem("crafting", "scrap");
   protected static final ItemStack SCRAP_BOX = IC2Items.getItem("crafting", "scrap_box");

   public TileEntityCompactingRecycler() {
      super((byte)1, Recipes.compactingRecycler);
   }

   @Override
   protected int getSpeedFactor() {
      return 12;
   }

   @Override
   public void operate() {
      if (this.canOperate()) {
         MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe = this.inputSlot.process();
         int chance = recipe.getRecipe().getMetaData() == null ? TileEntityRecycler.recycleChance() : recipe.getRecipe().getMetaData().getInteger("chance");
         Collection<ItemStack> output = new ArrayList<>((Collection<? extends ItemStack>)recipe.getOutput());
         if (chance > 1) {
            Iterator<ItemStack> it = output.iterator();

            while (it.hasNext()) {
               it.next();
               if (IC2.random.nextInt(chance) != 0) {
                  it.remove();
               }
            }
         }

         this.processUpgrades(output);
         this.outputSlot.add(output);
         this.inputSlot.consume(recipe);
      }
   }

   @Override
   public int getHeat() {
      return this.heat * 9;
   }

   @Override
   public String getSound() {
      return "Machines/RecyclerOp.ogg";
   }
}
