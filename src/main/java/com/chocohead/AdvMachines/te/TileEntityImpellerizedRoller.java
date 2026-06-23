package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.Recipes;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityImpellerizedRoller extends TileEntityHeatingMachine {
   private static final byte OUTPUTS = 1;

   public TileEntityImpellerizedRoller() {
      super((byte)1, Recipes.impellerizedRoller, 1, 24);
   }
}
