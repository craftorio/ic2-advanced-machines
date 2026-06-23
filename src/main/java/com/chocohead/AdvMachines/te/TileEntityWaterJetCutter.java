package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.Recipes;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityWaterJetCutter extends TileEntityHeatingWaterMachine {
   private static final byte OUTPUTS = 1;
   protected static final short IDLE_WATER_USE = 2;
   protected static final short ACTIVE_WATER_USE = 500;

   public TileEntityWaterJetCutter() {
      super((byte)1, Recipes.waterJetCutter, 1, 24, (short)500);
   }

   @Override
   protected int getIdleWaterUse() {
      return (int)((double)this.heat / 10000.0 * 2.0 + this.world.rand.nextDouble());
   }
}
