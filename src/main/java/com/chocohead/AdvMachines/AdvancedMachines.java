package com.chocohead.AdvMachines;

import com.chocohead.AdvMachines.gui.ProgressBars;
import com.chocohead.AdvMachines.item.ItemSharpPlate;
import com.chocohead.AdvMachines.te.AdvancedMachineTEs;
import ic2.api.event.TeBlockFinalCallEvent;
import ic2.api.event.ProfileEvent.Load;
import ic2.api.item.IC2Items;
import ic2.api.recipe.Recipes;
import ic2.api.recipe.ICraftingRecipeManager.AttributeContainer;
import ic2.core.IC2;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TeBlockRegistry;
import ic2.core.profile.Profile;
import ic2.core.profile.ProfileManager;
import ic2.core.profile.ProfileTarget;
import ic2.core.profile.TextureStyle;
import ic2.core.ref.IItemModelProvider;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@Mod(
   modid = "advanced_machines",
   name = "Advanced Machines",
   version = "61.0.1",
   acceptedMinecraftVersions = "[1.12,1.12.2]",
   dependencies = "required-after:ic2"
)
public final class AdvancedMachines {
   public static final String MODID = "advanced_machines";
   public static Logger logger;
   public static BlockTileEntity machines;
   public static Item sharpPlate;

   @EventHandler
   public void start(FMLConstructionEvent event) {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void load(Load event) {
      Profile profile = ProfileManager.getOrError("Classic");

      assert "advanced_machines".equals(((ModContainer)Loader.instance().getReversedModObjectList().get(this)).getModId());

      profile.textures
         .add(
            new TextureStyle(
               "advanced_machines",
               new ProfileTarget(((ModContainer)Loader.instance().getReversedModObjectList().get(this)).getSource(), "assets/advanced_machines/classic")
            )
         );
   }

   @SubscribeEvent
   public void register(TeBlockFinalCallEvent event) {
      TeBlockRegistry.addAll(AdvancedMachineTEs.class, AdvancedMachineTEs.IDENTITY);
      TeBlockRegistry.addCreativeRegisterer(AdvancedMachineTEs.water_jet_cutter);
   }

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      logger = event.getModLog();
      machines = TeBlockRegistry.get(AdvancedMachineTEs.IDENTITY);
      sharpPlate = new ItemSharpPlate();
      ProgressBars.addStyles();
      if (event.getSide().isClient()) {
         ((IItemModelProvider)sharpPlate).registerModels(null);
      }
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      AdvancedMachineTEs.buildDummies();
      ItemStack maceratorSurround;
      if (IC2.version.isExperimental()) {
         Recipes.advRecipes
            .addRecipe(maceratorSurround = new ItemStack(sharpPlate), new Object[]{"^^^", "^#^", "^^^", '^', Items.FLINT, '#', "plateIron"});
         addRecipe("metal_former", IC2Items.getItem("fence", "iron"), AdvancedMachineTEs.liquescent_extruder);
         addRecipe("metal_former", IC2Items.getItem("forge_hammer"), AdvancedMachineTEs.impellerized_roller, true);
         addRecipe("metal_former", IC2Items.getItem("cutter"), AdvancedMachineTEs.water_jet_cutter, true);
         addRecipe("ore_washing_plant", IC2Items.getItem("dust", "sulfur"), AdvancedMachineTEs.thermal_washer);
      } else {
         maceratorSurround = IC2Items.getItem("ingot", "refined_iron");
      }

      addRecipe("macerator", maceratorSurround, AdvancedMachineTEs.rotary_macerator);
      addRecipe("compressor", new ItemStack(Blocks.OBSIDIAN), AdvancedMachineTEs.singularity_compressor);
      addRecipe("extractor", IC2Items.getItem("treetap"), AdvancedMachineTEs.centrifuge_extractor);
      addRecipe("recycler", new ItemStack(Blocks.PISTON), AdvancedMachineTEs.compacting_recycler);
      RecipeLoader.loadRecipes();
   }

   private static void addRecipe(String baseMachine, ItemStack surroundItem, AdvancedMachineTEs output) {
      addRecipe(baseMachine, surroundItem, output, false);
   }

   private static void addRecipe(String baseMachine, ItemStack surroundItem, AdvancedMachineTEs output, boolean consuming) {
      Recipes.advRecipes
         .addRecipe(
            machines.getItemStack(output),
            new Object[]{
               "###",
               "#M#",
               "#X#",
               '#',
               surroundItem,
               'M',
               IC2Items.getItem("te", baseMachine),
               'X',
               IC2Items.getItem("resource", "advanced_machine"),
               new AttributeContainer(false, consuming)
            }
         );
   }

   @EventHandler
   public void postInit(FMLPostInitializationEvent event) {
      RecipeLoader.loadFailedRecipes();
      logger.info("Advanced Machines 61.0.1 loaded.");
   }
}
