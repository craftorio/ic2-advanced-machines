package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.Recipes;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.ExplosionIC2.Type;
import ic2.core.profile.NotClassic;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import org.apache.logging.log4j.Level;

@NotClassic
public class TileEntityThermalWasher extends TileEntityHeatingWaterMachine {
   private static final byte OUTPUTS = 3;
   private static final short IDLE_WATER_USE = 5;
   private static final short ACTIVE_WATER_USE = 500;

   public TileEntityThermalWasher() {
      super((byte)3, Recipes.thermalWasher, 6, 48, (short)500);
   }

   @Override
   public int getHeat() {
      return this.heat * 100 / 10000;
   }

   @Override
   protected int getIdleWaterUse() {
      return (int)((double)this.heat / 10000.0 * 5.0);
   }

   @Override
   protected void onFluidFill(boolean didFill, int amount) {
      super.onFluidFill(didFill, amount);
      if (didFill && this.tank.getFluidAmount() - amount <= 0 && this.heat >= 5000) {
         IC2.log
            .log(
               LogCategory.PlayerActivity,
               Level.INFO,
               "Thermal Washer at %s exploded (%d water inserted at %d%% heat)",
               new Object[]{Util.formatPosition(this), amount, this.heat * 100 / 10000}
            );
         ExplosionIC2 explosion = new ExplosionIC2(this.world, null, this.pos, 12.0F, 0.0F, Type.Heat);
         explosion.destroy(this.pos.getX(), this.pos.getY(), this.pos.getZ(), true);
         explosion.doExplosion();
      }
   }
}
