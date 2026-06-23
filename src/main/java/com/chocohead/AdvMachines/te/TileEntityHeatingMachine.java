package com.chocohead.AdvMachines.te;

import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.network.NetworkManager;
import ic2.core.util.StackUtil;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityHeatingMachine
   extends TileEntityElectricMachine
   implements IHasGui,
   IGuiValueProvider,
   IUpgradableBlock,
   IExplosionPowerOverride,
   INetworkTileEntityEventListener {
   protected static final byte DEFAULT_TIER = 2;
   protected static final byte DEFAULT_IDLE_EU = 1;
   protected static final byte DEFAULT_ACTIVE_EU = 15;
   protected static final byte SPIN_UP = 1;
   protected static final byte SPIN_DOWN = 2;
   protected static final short MAX_STORAGE = 10000;
   protected static final short MAX_HEAT = 10000;
   protected static final byte START = 0;
   protected static final byte INTERRUPT = 1;
   protected static final byte END = 2;
   public final InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack> inputSlot;
   public final InvSlotOutput outputSlot;
   public final InvSlotUpgrade upgradeSlot;
   protected final Redstone redstone;
   protected final int idleEU;
   protected final int activeEU;
   protected final int maxProgress = 120000 / this.getSpeedFactor();
   @GuiSynced
   protected short heat = 0;
   @GuiSynced
   public int progress = 0;
   protected AudioSource sound;
   private int lastSoundEvent = 1;

   public TileEntityHeatingMachine(byte numberOfOutputs, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeSet) {
      this((byte)2, numberOfOutputs, recipeSet);
   }

   public TileEntityHeatingMachine(byte tier, byte numberOfOutputs, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeSet) {
      this(tier, numberOfOutputs, recipeSet, 1, 15);
   }

   public TileEntityHeatingMachine(
      byte numberOfOutputs, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeSet, int idleEU, int activeEU
   ) {
      this((byte)2, numberOfOutputs, recipeSet, idleEU, activeEU);
   }

   public TileEntityHeatingMachine(
      byte tier, byte numberOfOutputs, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeSet, int idleEU, int activeEU
   ) {
      super(10000, tier);

      assert numberOfOutputs > 0 : "Must have at least one output slot";

      this.idleEU = idleEU;
      this.activeEU = activeEU;
      this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, recipeSet);
      this.outputSlot = new InvSlotOutput(this, "output", numberOfOutputs);
      this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
      this.redstone = (Redstone)this.addComponent(new Redstone(this));
      this.comparator.setUpdate(() -> this.heat * 15 / 10000);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      this.heat = nbt.getShort("heat");
      this.progress = nbt.getInteger("progress");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      nbt.setShort("heat", this.heat);
      nbt.setInteger("progress", this.progress);
      return nbt;
   }

   protected void onUnloaded() {
      super.onUnloaded();
      if (this.sound != null) {
         IC2.audioManager.removeSources(this);
         this.sound = null;
      }
   }

   protected int getSpeedFactor() {
      return 1;
   }

   protected void updateEntityServer() {
      super.updateEntityServer();
      boolean needsInvUpdate = false;
      boolean canOperate = this.canOperate();
      if (this.progress >= this.maxProgress) {
         while (this.progress >= this.maxProgress && canOperate) {
            this.operate();
            this.progress = this.progress - this.maxProgress;
            canOperate = this.canOperate();
         }

         needsInvUpdate = true;
      }

      boolean spinUp;
      if (this.canRun()) {
         if (canOperate && this.energy.useEnergy((double)this.activeEU)) {
            spinUp = true;
            this.progress = this.progress + this.heat;
            this.updateSound(0);
         } else {
            spinUp = this.redstone.hasRedstoneInput();
            this.progress = 0;
            if (spinUp && !this.energy.useEnergy((double)this.idleEU)) {
               spinUp = false;
            } else {
               this.updateSound(2);
            }
         }
      } else {
         spinUp = false;
      }

      if (spinUp) {
         this.heat = (short)Math.min(10000, this.heat + 1);
      } else {
         this.heat = (short)Math.max(0, this.heat - 2);
         if (this.heat <= 0) {
            if (this.getActive()) {
               this.updateSound(1);
            }
         } else {
            this.updateSound(2);
         }
      }

      for (ItemStack stack : this.upgradeSlot) {
         if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IUpgradeItem) {
            needsInvUpdate |= ((IUpgradeItem)stack.getItem()).onTick(stack, this);
         }
      }

      this.setActive(this.heat > 0);
      if (needsInvUpdate) {
         this.markDirty();
      }
   }

   protected boolean canRun() {
      return true;
   }

   public boolean canOperate() {
      if (this.inputSlot.isEmpty()) {
         return false;
      } else {
         MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = this.inputSlot.process();
         return output != null && this.outputSlot.canAdd((Collection)output.getOutput());
      }
   }

   public void operate() {
      assert this.canOperate();

      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = this.inputSlot.process();
      this.processUpgrades((Collection<ItemStack>)output.getOutput());
      this.outputSlot.add((Collection)output.getOutput());
      this.inputSlot.consume(output);
   }

   protected void processUpgrades(Collection<ItemStack> output) {
      for (ItemStack stack : this.upgradeSlot) {
         if (stack != null && stack.getItem() instanceof IUpgradeItem) {
            ((IUpgradeItem)stack.getItem()).onProcessEnd(stack, this, output);
         }
      }
   }

   protected void updateSound(int event) {
      if (this.lastSoundEvent != event) {
         ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent(this, this.lastSoundEvent = event, true);
      }
   }

   protected String getSound() {
      return null;
   }

   public void onNetworkEvent(int event) {
      if (this.sound == null && this.getSound() != null) {
         this.sound = IC2.audioManager.createSource(this, PositionSpec.Center, this.getSound(), true, false, IC2.audioManager.getDefaultVolume());
      }

      if (this.sound != null && this.lastSoundEvent != event) {
         this.lastSoundEvent = event;
         switch (event) {
            case 0:
               this.sound.play();
               break;
            case 1:
               this.sound.stop();
               IC2.audioManager.playOnce(this, "Machines/InterruptOne.ogg");
               break;
            case 2:
               this.sound.stop();
         }
      }
   }

   public ContainerBase<? extends TileEntityHeatingMachine> getGuiContainer(EntityPlayer player) {
      return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
   }

   @SideOnly(Side.CLIENT)
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
   }

   public int getHeat() {
      return this.heat;
   }

   public double getGuiValue(String name) {
      if ("progress".equals(name)) {
         return (double)(1000 * this.progress / this.maxProgress) / 1000.0;
      } else {
         throw new IllegalArgumentException("Unknown GUI value: " + name);
      }
   }

   public void onGuiClosed(EntityPlayer player) {
   }

   public boolean shouldExplode() {
      return true;
   }

   public float getExplosionPower(int tier, float defaultPower) {
      return tier >= 5 ? 16.0F : (float)tier + 1.0F;
   }

   public double getEnergy() {
      return this.energy.getEnergy();
   }

   public boolean useEnergy(double amount) {
      return this.energy.useEnergy(amount);
   }

   public Set<UpgradableProperty> getUpgradableProperties() {
      return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
   }
}
