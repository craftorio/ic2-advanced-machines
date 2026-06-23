package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.Recipes;

public class TileEntitySingularityCompressor extends TileEntityHeatingMachine {
   private static final byte OUTPUTS = 1;

   public TileEntitySingularityCompressor() {
      super((byte)1, Recipes.singularityCompressor);
   }

   @Override
   public int getHeat() {
      return this.heat * 9;
   }

   @Override
   public String getSound() {
      return "Machines/CompressorOp.ogg";
   }
}
