package com.chocohead.AdvMachines.te;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of the classic Advanced Machines heating machine base. Unlike vanilla IC2
 * processing machines (which run at a fixed speed scaled by overclockers), these
 * machines build up {@code heat} while running and convert that heat into progress
 * each tick, so they start slow and spin up to full speed. Rebuilt on top of
 * {@link TileEntityElectricMachine} for 1.20.1 / IC2: Refactored.
 */
public abstract class TileEntityHeatingMachine extends TileEntityElectricMachine
		implements IHasGui, IGuiValueProvider, IUpgradableBlock {
	/** Tracks last synced active state on the client for multiplayer sound start/stop. */
	private boolean clientLastActive;

	protected static final int DEFAULT_TIER = 2;
	protected static final int DEFAULT_IDLE_EU = 1;
	protected static final int DEFAULT_ACTIVE_EU = 15;
	protected static final int MAX_STORAGE = 10000;
	protected static final int MAX_HEAT = 10000;

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

	public TileEntityHeatingMachine(BlockEntityType<? extends TileEntityHeatingMachine> type, BlockPos pos, BlockState state,
			int numberOfOutputs, Recipes.IGetter<? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>> recipeManager) {
		this(type, pos, state, DEFAULT_TIER, numberOfOutputs, recipeManager, DEFAULT_IDLE_EU, DEFAULT_ACTIVE_EU);
	}

	public TileEntityHeatingMachine(BlockEntityType<? extends TileEntityHeatingMachine> type, BlockPos pos, BlockState state,
			int numberOfOutputs, Recipes.IGetter<? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>> recipeManager,
			int idleEU, int activeEU) {
		this(type, pos, state, DEFAULT_TIER, numberOfOutputs, recipeManager, idleEU, activeEU);
	}

	public TileEntityHeatingMachine(BlockEntityType<? extends TileEntityHeatingMachine> type, BlockPos pos, BlockState state,
			int tier, int numberOfOutputs, Recipes.IGetter<? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>> recipeManager,
			int idleEU, int activeEU) {
		super(type, pos, state, MAX_STORAGE, tier);

		if (numberOfOutputs <= 0) {
			throw new IllegalArgumentException("Must have at least one output slot");
		}

		this.idleEU = idleEU;
		this.activeEU = activeEU;
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, recipeManager);
		this.outputSlot = new InvSlotOutput(this, "output", numberOfOutputs);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
		this.redstone = this.addComponent(new Redstone(this));
		this.comparator.setUpdate(() -> this.heat * 15 / MAX_HEAT);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.heat = nbt.getShort("heat");
		this.progress = nbt.getInt("progress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putShort("heat", this.heat);
		nbt.putInt("progress", this.progress);
	}

	protected int getSpeedFactor() {
		return 1;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient() {
		super.updateEntityClient();
		// IC2 2.10.29 tries to restart looping sounds in updateEntityServer(), but on a dedicated
		// server IC2.soundManager is a no-op — sounds must be driven from the client instead.
		if (this.loopingSound != null) {
			if (this.getActive() && !this.loopingSound.isPlaying()) {
				this.loopingSound.play();
			} else if (!this.getActive() && this.loopingSound.isPlaying()) {
				this.loopingSound.stop();
			}
		}
	}

	@Override
	public void onNetworkUpdate(String field) {
		super.onNetworkUpdate(field);
		Level level = this.getLevel();
		if ("active".equals(field) && level != null && level.isClientSide) {
			boolean nowActive = this.getActive();
			if (nowActive != this.clientLastActive) {
				this.clientLastActive = nowActive;
				if (nowActive) {
					this.startPlaySound(false);
				} else {
					this.stopStartSound();
					this.stopLoopingSound();
					this.playStopSound();
				}
			}
		}
	}

	@Override
	protected void updateEntityServer() {
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		boolean canOperate = this.canOperate();
		if (this.progress >= this.maxProgress) {
			while (this.progress >= this.maxProgress && canOperate) {
				this.operate();
				this.progress -= this.maxProgress;
				canOperate = this.canOperate();
			}

			needsInvUpdate = true;
		}

		boolean spinUp;
		if (this.canRun()) {
			if (canOperate && this.energy.useEnergy(this.activeEU)) {
				spinUp = true;
				this.progress += this.heat;
			} else {
				spinUp = this.redstone.hasRedstoneInput();
				this.progress = 0;
				if (spinUp && !this.energy.useEnergy(this.idleEU)) {
					spinUp = false;
				}
			}
		} else {
			spinUp = false;
		}

		if (spinUp) {
			this.heat = (short) Math.min(MAX_HEAT, this.heat + 1);
		} else {
			this.heat = (short) Math.max(0, this.heat - 2);
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();

		boolean shouldBeActive = this.heat > 0;
		if (shouldBeActive) {
			if (!this.getActive()) {
				this.activate(false);
			}
		} else if (this.getActive()) {
			this.shutdown(true);
		}

		if (needsInvUpdate) {
			super.setChanged();
		}
	}

	protected boolean canRun() {
		return true;
	}

	public boolean canOperate() {
		if (this.inputSlot.isEmpty()) {
			return false;
		}

		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = this.inputSlot.process();
		return output != null && this.outputSlot.canAdd(output.getOutput());
	}

	public void operate() {
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = this.inputSlot.process();
		Collection<ItemStack> result = StackUtil.copy(output.getOutput());
		this.processUpgrades(result);
		this.outputSlot.add(result);
		this.inputSlot.consume(output);
	}

	protected void processUpgrades(Collection<ItemStack> output) {
		for (int i = 0; i < this.upgradeSlot.size(); i++) {
			ItemStack stack = this.upgradeSlot.get(i);
			if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IUpgradeItem upgrade) {
				output = upgrade.onProcessEnd(stack, this, output);
			}
		}
	}

	// IC2's GuiParser loads guidef XML via GuiParser.class.getResourceAsStream(), which under the
	// Forge 1.20.1 module system only sees IC2's own jar — not this addon's. So we parse the guidef
	// from this class's own classloader and hand the resulting node to DynamicContainer directly.
	private static final Method GUI_PARSE_STREAM;
	static {
		try {
			Method m = GuiParser.class.getDeclaredMethod("parse", InputStream.class, Class.class);
			m.setAccessible(true);
			GUI_PARSE_STREAM = m;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not access IC2 GuiParser.parse(InputStream, Class)", e);
		}
	}

	private GuiParser.GuiNode parseGui() {
		ResourceLocation id = Util.getName(this.getBlockType());
		String path = String.format("/assets/%s/guidef/%s.xml", id.getNamespace(), id.getPath());
		try (InputStream raw = this.getClass().getResourceAsStream(path)) {
			if (raw == null) {
				throw new FileNotFoundException("Could not load " + path + " from the addon classpath.");
			}

			return (GuiParser.GuiNode) GUI_PARSE_STREAM.invoke(null, new BufferedInputStream(raw), this.getClass());
		} catch (Exception e) {
			throw new RuntimeException("Error reading/parsing GUI definition " + id + " from " + path, e);
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player) {
		return DynamicContainer.create(Ic2ScreenHandlers.DYNAMIC_BE, syncId, player.getInventory(), (TileEntityInventory) this, parseGui());
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data) {
		return DynamicContainer.create(Ic2ScreenHandlers.DYNAMIC_BE, syncId, inventory, (TileEntityInventory) this, parseGui());
	}

	@Override
	public SoundEvent getInterruptSoundEvent() {
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
	}

	public int getHeat() {
		return this.heat;
	}

	@Override
	public double getGuiValue(String name) {
		if ("progress".equals(name)) {
			return (double) this.progress / this.maxProgress;
		}

		throw new IllegalArgumentException("Unknown GUI value: " + name);
	}

	@Override
	public double getEnergy() {
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount) {
		return this.energy.useEnergy(amount);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties() {
		return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
	}
}
