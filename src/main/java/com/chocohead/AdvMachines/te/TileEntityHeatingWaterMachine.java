package com.chocohead.AdvMachines.te;

import java.util.Collection;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.block.comp.Fluids;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.network.GuiSynced;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Heating machine variant that additionally consumes water from an internal tank
 * (the Water Jet Cutter and Thermal Washer). The tank only accepts water and is
 * insert-only; fill it with fluid pipes/cells.
 */
public abstract class TileEntityHeatingWaterMachine extends TileEntityHeatingMachine {
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	@GuiSynced
	protected final Fluids.InternalFluidTank tank;
	protected final int activeWaterUse;

	public TileEntityHeatingWaterMachine(BlockEntityType<? extends TileEntityHeatingWaterMachine> type, BlockPos pos, BlockState state,
			int numberOfOutputs, Recipes.IGetter<? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>> recipeManager,
			int idleEU, int activeEU, int activeWaterUse) {
		super(type, pos, state, numberOfOutputs, recipeManager, idleEU, activeEU);
		this.tank = this.fluids.addTankInsert("tank", 8000, Fluids.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER));
		this.activeWaterUse = activeWaterUse;
	}

	public int getWaterAmount() {
		return this.tank.getFluidAmount();
	}

	@Override
	protected boolean canRun() {
		int waterNeeded = this.getIdleWaterUse();
		if (waterNeeded <= 0) {
			return true;
		}

		Ic2FluidStack available = this.tank.drainMb(waterNeeded, true);
		if (available != null && available.getAmountMb() == waterNeeded) {
			this.tank.drainMb(waterNeeded, false);
			return true;
		}

		return false;
	}

	protected abstract int getIdleWaterUse();

	@Override
	public boolean canOperate() {
		if (!super.canOperate()) {
			return false;
		}

		Ic2FluidStack available = this.tank.drainMb(this.activeWaterUse, true);
		return available != null && available.getAmountMb() == this.activeWaterUse;
	}

	@Override
	public void operate() {
		super.operate();
		this.tank.drainMb(this.activeWaterUse, false);
	}
}
