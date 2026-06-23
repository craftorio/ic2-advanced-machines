package com.chocohead.AdvMachines.item;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class ItemSharpPlate extends Item implements IItemModelProvider {
   public ItemSharpPlate() {
      this.setCreativeTab(IC2.tabIC2);
      BlocksItems.registerItem(this, new ResourceLocation("advanced_machines", "sharp_plate"));
   }

   public void registerModels(ItemName name) {
      ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), null));
   }

   public String getTranslationKey() {
      return this.getRegistryName().toString().replace(':', '.');
   }

   public String getTranslationKey(ItemStack stack) {
      return this.getTranslationKey();
   }

   public String getItemStackDisplayName(ItemStack stack) {
      return Localization.translate(this.getTranslationKey(stack));
   }

   public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
      if (this.isInCreativeTab(tab) && !IC2.version.isClassic()) {
         items.add(new ItemStack(this));
      }
   }
}
