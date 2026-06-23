package com.chocohead.AdvMachines.te;

import com.chocohead.AdvMachines.api.IRecipeLoadingTeBlock;
import com.chocohead.AdvMachines.api.Recipes;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.ITeBlock.ITeBlockCreativeRegisterer;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.profile.Version;
import ic2.core.ref.TeBlock.DefaultDrop;
import ic2.core.ref.TeBlock.HarvestTool;
import ic2.core.util.Util;
import java.util.Collection;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

public enum AdvancedMachineTEs implements IRecipeLoadingTeBlock, ITeBlockCreativeRegisterer {
   rotary_macerator(TileEntityRotaryMacerator.class, 0),
   singularity_compressor(TileEntitySingularityCompressor.class, 1),
   centrifuge_extractor(TileEntityCentrifugeExtractor.class, 2),
   compacting_recycler(TileEntityCompactingRecycler.class, 3),
   liquescent_extruder(TileEntityLiquescentExtruder.class, 4),
   impellerized_roller(TileEntityImpellerizedRoller.class, 5),
   water_jet_cutter(TileEntityWaterJetCutter.class, 6),
   thermal_washer(TileEntityThermalWasher.class, 7);

   private final Class<? extends TileEntityBlock> teClass;
   private final int itemMeta;
   private TileEntityBlock dummyTe;
   public static final ResourceLocation IDENTITY = new ResourceLocation("advanced_machines", "machines");

   private AdvancedMachineTEs(Class<? extends TileEntityBlock> teClass, int itemMeta) {
      this.teClass = teClass;
      this.itemMeta = itemMeta;
      GameRegistry.registerTileEntity(teClass, "advanced_machines:" + this.getName());
   }

   public boolean hasItem() {
      return true;
   }

   public String getName() {
      return this.name();
   }

   public int getId() {
      return this.itemMeta;
   }

   public ResourceLocation getIdentifier() {
      return IDENTITY;
   }

   public Class<? extends TileEntityBlock> getTeClass() {
      return this.teClass;
   }

   public boolean hasActive() {
      return true;
   }

   public float getHardness() {
      return 5.0F;
   }

   public float getExplosionResistance() {
      return 10.0F;
   }

   public HarvestTool getHarvestTool() {
      return HarvestTool.Pickaxe;
   }

   public DefaultDrop getDefaultDrop() {
      return DefaultDrop.AdvMachine;
   }

   public boolean allowWrenchRotating() {
      return false;
   }

   public Set<EnumFacing> getSupportedFacings() {
      return Util.horizontalFacings;
   }

   public EnumRarity getRarity() {
      return EnumRarity.UNCOMMON;
   }

   public Material getMaterial() {
      return Material.IRON;
   }

   public void addSubBlocks(NonNullList<ItemStack> list, BlockTileEntity block, ItemBlockTileEntity item, CreativeTabs tab) {
      if (tab == IC2.tabIC2 || tab == CreativeTabs.SEARCH) {
         for (AdvancedMachineTEs type : values()) {
            if (type.hasItem() && Version.shouldEnable(type.teClass)) {
               list.add(block.getItemStack(type));
            }
         }
      }
   }

   @Override
   public String[] getRecipeCategories() {
      return new String[]{this.getName()};
   }

   @Override
   public IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> getManager(String category) {
      switch (this) {
         case rotary_macerator:
            return Recipes.rotaryMacerator;
         case singularity_compressor:
            return Recipes.singularityCompressor;
         case centrifuge_extractor:
            return Recipes.centrifugeExtractor;
         case compacting_recycler:
            return Recipes.compactingRecycler;
         case liquescent_extruder:
            return Recipes.liquescentExtruder;
         case impellerized_roller:
            return Recipes.impellerizedRoller;
         case water_jet_cutter:
            return Recipes.waterJetCutter;
         case thermal_washer:
            return Recipes.thermalWasher;
         default:
            throw new IllegalStateException("Gee, we've no idea for " + this);
      }
   }

   @Override
   public IRecipeLoadingTeBlock.MachineType getType(String category) {
      switch (this) {
         case compacting_recycler:
            return IRecipeLoadingTeBlock.MachineType.RECYCLER;
         default:
            return IRecipeLoadingTeBlock.MachineType.NORMAL;
      }
   }

   public static void buildDummies() {
      ModContainer mc = Loader.instance().activeModContainer();
      if (mc != null && "advanced_machines".equals(mc.getModId())) {
         for (AdvancedMachineTEs block : values()) {
            if (block.teClass != null) {
               try {
                  block.dummyTe = block.teClass.newInstance();
               } catch (Exception var6) {
                  if (Util.inDev()) {
                     var6.printStackTrace();
                  }
               }
            }
         }
      } else {
         throw new IllegalAccessError("Don't mess with this please.");
      }
   }

   public TileEntityBlock getDummyTe() {
      return this.dummyTe;
   }
}
