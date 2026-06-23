package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.Recipes;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityRotaryMacerator extends TileEntityHeatingMachine {
   private static final byte OUTPUTS = 2;

   public TileEntityRotaryMacerator() {
      super((byte)2, Recipes.rotaryMacerator);
   }

   @SideOnly(Side.CLIENT)
   protected void updateEntityClient() {
      super.updateEntityClient();
      if (this.getActive() && this.world.rand.nextInt(8) == 0) {
         for (int i = 0; i < 4; i++) {
            this.world
               .spawnParticle(
                  EnumParticleTypes.SMOKE_NORMAL,
                  (double)this.pos.getX() + 0.5 + (double)this.world.rand.nextFloat() * 0.6 - 0.3,
                  (double)(this.pos.getY() + 1) + (double)this.world.rand.nextFloat() * 0.2 - 0.1,
                  (double)this.pos.getZ() + 0.5 + (double)this.world.rand.nextFloat() * 0.6 - 0.3,
                  0.0,
                  0.0,
                  0.0,
                  new int[0]
               );
         }
      }
   }

   @Override
   public String getSound() {
      return "Machines/MaceratorOp.ogg";
   }
}
