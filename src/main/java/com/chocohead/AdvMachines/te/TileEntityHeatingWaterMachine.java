package com.chocohead.AdvMachines.te;

import com.google.common.base.Predicate;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Fluids.InternalFluidTank;
import ic2.core.network.GuiSynced;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Collection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public abstract class TileEntityHeatingWaterMachine extends TileEntityHeatingMachine {
   @GuiSynced
   protected final InternalFluidTank tank = new InternalFluidTank(
      "tank", Util.allFacings, Util.noFacings, Fluids.fluidPredicate(new Fluid[]{FluidRegistry.WATER}), 8000
   ) {
      public int fillInternal(FluidStack resource, boolean doFill) {
         int out = super.fillInternal(resource, doFill);
         if (out > 0) {
            TileEntityHeatingWaterMachine.this.onFluidFill(doFill, out);
         }

         return out;
      }

      public boolean canDrain() {
         return false;
      }
   };
   protected final short activeWaterUse;

   public TileEntityHeatingWaterMachine(
      byte numberOfOutputs, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeSet, short activeWaterUse
   ) {
      super(numberOfOutputs, recipeSet);
      ((Fluids)this.addComponent(new Fluids(this))).addTank(this.tank);
      this.activeWaterUse = activeWaterUse;
   }

   public TileEntityHeatingWaterMachine(
      byte tier, byte numberOfOutputs, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeSet, short activeWaterUse
   ) {
      super(tier, numberOfOutputs, recipeSet);
      ((Fluids)this.addComponent(new Fluids(this))).addTank(this.tank);
      this.activeWaterUse = activeWaterUse;
   }

   public TileEntityHeatingWaterMachine(
      byte numberOfOutputs, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeSet, int idleEU, int activeEU, short activeWaterUse
   ) {
      super(numberOfOutputs, recipeSet, idleEU, activeEU);
      ((Fluids)this.addComponent(new Fluids(this))).addTank(this.tank);
      this.activeWaterUse = activeWaterUse;
   }

   public TileEntityHeatingWaterMachine(
      byte tier,
      byte numberOfOutputs,
      IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeSet,
      int idleEU,
      int activeEU,
      short activeWaterUse
   ) {
      super(tier, numberOfOutputs, recipeSet, idleEU, activeEU);
      ((Fluids)this.addComponent(new Fluids(this))).addTank(this.tank);
      this.activeWaterUse = activeWaterUse;
   }

   protected void onFluidFill(boolean didFill, int amount) {
   }

   @Override
   public boolean canRun() {
      int waterNeeded = this.getIdleWaterUse();
      if (waterNeeded == 0) {
         return true;
      } else {
         FluidStack stack = this.tank.drainInternal(waterNeeded, false);
         if (stack != null && stack.amount == waterNeeded) {
            this.tank.drainInternal(waterNeeded, true);
            return true;
         } else {
            return false;
         }
      }
   }

   protected abstract int getIdleWaterUse();

   @Override
   public boolean canOperate() {
      FluidStack stack;
      return super.canOperate() && (stack = this.tank.drainInternal(this.activeWaterUse, false)) != null && stack.amount == this.activeWaterUse;
   }

   @Override
   public void operate() {
      super.operate();
      this.tank.drainInternal(this.activeWaterUse, true);
   }

   protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      ItemStack heldItem = StackUtil.get(player, hand);
      int space;
      if (!StackUtil.isEmpty(heldItem)
         && FluidUtil.tryEmptyContainer(heldItem, this.tank, space = this.tank.getCapacity() - this.tank.getFluidAmount(), player, false).success) {
         FluidActionResult stack = FluidUtil.tryEmptyContainer(heldItem, this.tank, space, player, true);

         assert stack.success;

         if (StackUtil.getSize(heldItem) > 1) {
            StackUtil.consumeOrError(player, hand, 1);
            StackUtil.storeInventoryItem(stack.result, player, false);
         } else {
            StackUtil.set(player, hand, stack.result);
         }

         return true;
      } else {
         return super.onActivated(player, hand, side, hitX, hitY, hitZ);
      }
   }
}
