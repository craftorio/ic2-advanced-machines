package com.chocohead.AdvMachines.api;

import com.google.common.collect.ImmutableSet;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.core.block.ITeBlock;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.ArrayUtils;

public interface IRecipeLoadingTeBlock extends ITeBlock {
   String[] getRecipeCategories();

   IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> getManager(String var1);

   IRecipeLoadingTeBlock.MachineType getType(String var1);

   public static enum MachineType {
      NORMAL,
      RECYCLER("chance"),
      THERMAL_CENTRIFUGE("minHeat");

      private final Set<String> requiredTags;

      private MachineType() {
         this.requiredTags = Collections.emptySet();
      }

      private MachineType(String... neededTags) {
         assert !ArrayUtils.contains(neededTags, null);

         assert neededTags.length > 0;

         this.requiredTags = ImmutableSet.copyOf(neededTags);
      }

      public boolean needsTags() {
         return !this.requiredTags.isEmpty();
      }

      public boolean hasRequiredTags(NBTTagCompound metadata) {
         for (String key : this.requiredTags) {
            if (!metadata.hasKey(key)) {
               return false;
            }
         }

         return true;
      }
   }
}
