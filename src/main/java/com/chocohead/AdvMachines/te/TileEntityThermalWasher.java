package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.AdvMachinesBlocks;

import ic2.api.recipe.Recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityThermalWasher extends TileEntityHeatingWaterMachine {
	/** Heat at/above which inserting water into an empty tank flash-explodes the machine. */
	private static final int EXPLODE_HEAT = 5000;
	private int lastWaterAmount = 0;

	public TileEntityThermalWasher(BlockPos pos, BlockState state) {
		super(AdvMachinesBlocks.BE_THERMAL_WASHER.get(), pos, state, 3, Recipes.oreWashing, 6, 48, 500);
	}

	@Override
	protected void updateEntityServer() {
		super.updateEntityServer();
		int water = this.getWaterAmount();
		if (water > 0 && this.lastWaterAmount <= 0 && this.heat >= EXPLODE_HEAT) {
			Level world = this.getLevel();
			world.explode(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
					12.0F, Level.ExplosionInteraction.BLOCK);
		}
		this.lastWaterAmount = water;
	}

	@Override
	public int getHeat() {
		return this.heat * 100 / 10000;
	}

	@Override
	protected int getIdleWaterUse() {
		return (int) (this.heat / 10000.0 * 5.0);
	}
}
