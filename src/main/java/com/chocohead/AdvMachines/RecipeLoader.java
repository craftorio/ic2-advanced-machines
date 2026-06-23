package com.chocohead.AdvMachines;

import com.chocohead.AdvMachines.api.IRecipeLoadingTeBlock;
import com.chocohead.AdvMachines.api.Recipes;
import com.chocohead.AdvMachines.te.AdvancedMachineTEs;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.core.init.Rezepte;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.Config.Value;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class RecipeLoader {
   private static final Queue<RecipeLoader.PendingRecipe> PENDING_RECIPES = new ArrayDeque<>();
   private static final Method WHITESPACE = ReflectionUtil.getMethod(Rezepte.class, new String[]{"splitWhitespace"}, new Class[]{String.class});
   private static final Marker RECIPE = MarkerManager.getMarker("recipe");

   public static void loadRecipes() {
      for (IRecipeLoadingTeBlock machine : AdvancedMachineTEs.values()) {
         for (String category : machine.getRecipeCategories()) {
            Config config = new Config(category.replace('_', ' ') + " recipes");

            try {
               config.load(Rezepte.getConfigFile(category));
            } catch (Exception var10) {
               AdvancedMachines.logger.warn(RECIPE, "Recipe loading for " + category + " failed.", var10);
               continue;
            }

            loadRecipes(config, machine.getManager(category), machine.getType(category));
         }
      }

      AdvancedMachines.logger.debug(RECIPE, PENDING_RECIPES.size() + " recipes failed to load in the first pass.");
   }

   public static void loadFailedRecipes() {
      RecipeLoader.PendingRecipe recipe;
      while ((recipe = PENDING_RECIPES.poll()) != null) {
         loadMachineRecipe(recipe.value, recipe.manager, recipe.machineType, true);
      }
   }

   private static void loadRecipes(
      Config config, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> machine, IRecipeLoadingTeBlock.MachineType type
   ) {
      int amount = 0;
      int successful = 0;

      for (Iterator<Value> it = config.valueIterator(); it.hasNext(); amount++) {
         if (loadMachineRecipe(it.next(), machine, type, false)) {
            successful++;
         }
      }

      AdvancedMachines.logger.info(RECIPE, "Successfully loaded " + successful + " out of " + amount + " recipes for " + config.name);
   }

   private static boolean loadMachineRecipe(
      Value value, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> machine, IRecipeLoadingTeBlock.MachineType type, boolean lastAttempt
   ) {
      IRecipeInput input;
      try {
         input = ConfigUtil.asRecipeInputWithAmount(value.name);
      } catch (ParseException var11) {
         throw new ic2.core.util.Config.ParseException("invalid key", value, var11);
      }

      if (input == null) {
         if (lastAttempt) {
            AdvancedMachines.logger
               .warn(
                  RECIPE,
                  "Skipping recipe due to unresolvable input " + value.name + '.',
                  new ic2.core.util.Config.ParseException("invalid input specified: " + value.name, value)
               );
         } else {
            PENDING_RECIPES.add(new RecipeLoader.PendingRecipe(value, machine, type));
         }

         return false;
      } else {
         List<ItemStack> outputs = new ArrayList<>();
         NBTTagCompound metadata = new NBTTagCompound();
         boolean squishOutputs = false;

         try {
            for (String part : splitWhitespace(value.getString())) {
               if (part.startsWith("@")) {
                  if (part.startsWith("@ignoreSameInputOutput")) {
                     metadata.setBoolean("ignoreSameInputOutput", true);
                  } else if (!part.startsWith("@chance:") || type != IRecipeLoadingTeBlock.MachineType.RECYCLER) {
                     if (!part.startsWith("@heat:") || type != IRecipeLoadingTeBlock.MachineType.THERMAL_CENTRIFUGE) {
                        throw new ic2.core.util.Config.ParseException("Invalid attribute: " + part, value);
                     }

                     metadata.setInteger("minHeat", Integer.parseInt(part.substring(6)));
                  } else {
                     int chance = Integer.parseInt(part.substring(8));
                     if (chance < 1) {
                        throw new ic2.core.util.Config.ParseException("Invalid chance: " + chance, value);
                     }

                     metadata.setInteger("chance", chance);
                  }
               } else if ("<NULL>".equals(part)) {
                  squishOutputs = true;
               } else {
                  ItemStack cOutput = ConfigUtil.asStackWithAmount(part);
                  if (cOutput == null) {
                     if (lastAttempt) {
                        AdvancedMachines.logger
                           .warn(
                              RECIPE,
                              String.format("Skipping recipe using %s due to unresolvable output %s.", value.name, part),
                              new ic2.core.util.Config.ParseException("invalid output specified: " + value.name, value)
                           );
                     } else {
                        PENDING_RECIPES.add(new RecipeLoader.PendingRecipe(value, machine, type));
                     }

                     return false;
                  }

                  outputs.add(cOutput);
               }
            }

            if (squishOutputs) {
               if (machine.addRecipe(input, Recipes.SKIP, metadata, true)) {
                  return true;
               } else {
                  AdvancedMachines.logger.warn(RECIPE, "Couldn't remove " + input + " recipe.");
                  return false;
               }
            } else if (type.needsTags() && (metadata.isEmpty() || !type.hasRequiredTags(metadata))) {
               AdvancedMachines.logger.warn(RECIPE, "Could not add machine recipe: " + value.name + " missing tag.");
               return false;
            } else if (machine.addRecipe(input, outputs, metadata.isEmpty() ? null : metadata, false)) {
               return true;
            } else {
               throw new Exception("Conflicting recipe");
            }
         } catch (ic2.core.util.Config.ParseException var12) {
            throw var12;
         } catch (Exception var13) {
            throw new ic2.core.util.Config.ParseException("generic parse error", value, var13);
         }
      }
   }

   private static List<String> splitWhitespace(String text) {
      if (WHITESPACE == null) {
         throw new IllegalStateException("Could not reflect whitespace method!");
      } else {
         try {
            return (List<String>)WHITESPACE.invoke(null, text);
         } catch (Exception var2) {
            throw new RuntimeException("Error reflecting whitespace", var2);
         }
      }
   }

   private static class PendingRecipe {
      final Value value;
      final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> manager;
      final IRecipeLoadingTeBlock.MachineType machineType;

      PendingRecipe(Value value, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> manager, IRecipeLoadingTeBlock.MachineType machineType) {
         this.value = value;
         this.manager = manager;
         this.machineType = machineType;
      }
   }
}
