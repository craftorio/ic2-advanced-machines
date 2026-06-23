package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.Recipes;

public class TileEntityCentrifugeExtractor extends TileEntityHeatingMachine {
   private static final byte OUTPUTS = 3;

   public TileEntityCentrifugeExtractor() {
      super((byte)3, Recipes.centrifugeExtractor);
   }
}
