package com.chocohead.AdvMachines.te;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.chocohead.AdvMachines.AdvMachinesBlocks;
import com.chocohead.AdvMachines.AdvRecipes;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCompactingRecycler extends TileEntityHeatingMachine {
	public TileEntityCompactingRecycler(BlockPos pos, BlockState state) {
		super(AdvMachinesBlocks.BE_COMPACTING_RECYCLER.get(), pos, state, 1, AdvRecipes.COMPACTING_RECYCLER);
	}

	@Override
	protected int getSpeedFactor() {
		return 12;
	}

	@Override
	public void operate() {
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> recipe = this.inputSlot.process();
		CompoundTag meta = recipe.getRecipe().getMetaData();
		int chance = meta == null ? TileEntityRecycler.recycleChance() : meta.getInt("chance");
		Collection<ItemStack> output = new ArrayList<>(StackUtil.copy(recipe.getOutput()));
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

	@Override
	public int getHeat() {
		return this.heat * 9;
	}

	@Override
	public SoundEvent getLoopingSoundEvent() {
		return Ic2SoundEvents.MACHINE_RECYCLER_OPERATE;
	}
}
