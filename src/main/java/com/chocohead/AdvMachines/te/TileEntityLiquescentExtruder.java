package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.Recipes;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityLiquescentExtruder extends TileEntityHeatingMachine {
   private static final byte OUTPUTS = 3;

   public TileEntityLiquescentExtruder() {
      super((byte)3, Recipes.liquescentExtruder, 1, 24);
   }

   @Override
   public int getHeat() {
      return (this.heat * 100 + 5000) / 10000;
   }

   @Override
   public String getSound() {
      return "Machines/ExtractorOp.ogg";
   }
}
