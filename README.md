# Advanced Machines — 1.20.1 port

A Minecraft **1.20.1 / Forge** port of the classic **Advanced Machines** IndustrialCraft 2
addon, running against the modern **IC2: Refactored** fork.

- Original mod (1.2.5 … 1.12.2), by immibis and later Chocohead:
  <https://www.curseforge.com/minecraft/mc-mods/advanced-machines>
- Target IC2 for 1.20.1+ (the fork this port depends on):
  <https://github.com/HalfCooler/ic2>

Advanced Machines adds "heated" variants of the standard IC2 processing machines. Unlike a
normal IC2 machine — which runs at a fixed speed scaled only by overclockers — these build up
**heat** while they run and turn that heat into progress each tick. They start slow and spin
up to full speed, trading a warm-up period for a high top throughput.

## Machines

| Machine | Based on | Notes |
|---|---|---|
| Rotary Macerator | Macerator | |
| Singularity Compressor | Compressor | |
| Centrifuge Extractor | Extractor | |
| Compacting Recycler | Recycler | also compacts 9 scrap → 1 scrap box |
| Liquescent Extruder | Metal Former (extruding) | |
| Impellerized Roller | Metal Former (rolling) | |
| Water-Jet Cutter | Metal Former (cutting) | consumes water |
| Thermal Washer | Ore Washing Plant | consumes water; flash-explodes if filled while hot |

Plus the **Sharpened Iron Plate** crafting component.

## Branches

| Branch | Contents |
|---|---|
| `forge/1.20.1` | **default** — the live 1.20.1 port (this) |
| `forge/1.12.2` | the original mod, decompiled & deobfuscated, kept as a study reference |

## Requirements

| | |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.20 |
| IC2 | [IC2: Refactored](https://github.com/HalfCooler/ic2) `2.10.26-ex120` or newer |

## Building

This mod compiles against IC2's internal classes, so it needs a **dev jar** of IC2 — a jar of
IC2's compiled, official-named (dev-mappings) classes and resources. It is **not** committed
here; build it from a checkout of the IC2 fork next to this repo:

```sh
# 1. Build IC2 (produces build/classes + build/resources under official mappings)
cd ../ic2
./gradlew build

# 2. Assemble the dev jar and drop it into this project's libs/
jar cf ../advanced-machines/libs/ic2-forge-2.10.26-ex120-dev.jar \
    -C build/classes/java/main . \
    -C build/resources/main .

# 3. Build the addon
cd ../advanced-machines
./gradlew build
```

Output: `build/libs/advanced_machines-61.0.2-1.20.1.jar`. To test, install it into a 1.20.1 Forge
profile alongside IC2, or run `./gradlew runClient` with IC2 on the mod classpath.

> **Why a dev jar and not `fg.deobf` on the release jar?** IC2's release jar is reobfuscated,
> so its overrides of Minecraft methods (e.g. `Container.stillValid`) are stored under SRG
> names, and `fg.deobf` does not reliably remap those inherited-override names — compilation
> breaks. The dev classes are already official-named, so they are consumed with a plain
> `files(...)` dependency. Re-vendor the jar whenever IC2 changes.

## How the port works

IC2: Refactored is no longer an addon platform — the `TeBlock` registry, the texture-profile
system, `TeBlockFinalCallEvent` and the config-driven (`.ini`) recipe loader the original
hooked into are all gone. The processing core survives, so this port:

- **Reuses** `TileEntityElectricMachine`, the `InvSlot*` slots, the dynamic GUI system
  (`DynamicContainer` + `guidef/*.xml`, the same format as the original), the
  `Ic2TileEntityBlock.create(...)` machine-block factory, and the IC2 recipe managers.
- **Reimplements** registration with a Forge `DeferredRegister` (`AdvMachinesBlocks`), the
  heat/spin-up base (`te/TileEntityHeatingMachine`, `te/TileEntityHeatingWaterMachine`),
  sounds via `Ic2SoundEvents`, and the extra recycler recipe in code (`AdvRecipes`).

### Texture styles

The mod ships with both texture sets from the original. The **modern** (IC2 Experimental) look is the
**default** via a bundled resource pack; set `modernTextures = false` in `config/advanced_machines-client.toml`
for the **classic** (IC2-Classic) look:

```toml
modernTextures = false  # true = modern (default), false = classic
```

Changing it requires a restart (it swaps a resource pack). IC2's old profile-driven runtime switch
has no equivalent, so this config toggle replaces it.

### Known differences from the original
- The water machines are filled via **fluid pipes/cells**; right-click-with-bucket filling is
  not reimplemented.
- Animated active models (centrifuge / thermal washer) collapse to a single active model.
- JEI: each machine is registered as a catalyst for its matching IC2 recipe category, so its
  recipes are browsable. The **Compacting Recycler** is the exception — IC2 1.20.1 has no JEI
  category for the recycler, so it has nothing to attach to and its "9 scrap → scrap box" extra
  recipe is not shown in JEI (it still works in-world).

## Credits & licensing

Advanced Machines was created by **immibis** and maintained by **Chocohead**; see the
[CurseForge page](https://www.curseforge.com/minecraft/mc-mods/advanced-machines) for the
original. This port targets [HalfCooler's IC2: Refactored](https://github.com/HalfCooler/ic2).
IndustrialCraft 2 and Advanced Machines are the property of their respective authors; this
repository is a compatibility port and is bound by their licensing terms.
