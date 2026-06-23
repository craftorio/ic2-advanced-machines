# Porting notes: Advanced Machines 1.12.2 → 1.20.1 (IC2: Refactored)

Concrete gotchas hit while porting the classic IC2 addon to Minecraft 1.20.1 / Forge against
[IC2: Refactored](https://github.com/HalfCooler/ic2). Roughly in the order they bite you.

---

## 1. IC2 is no longer an addon platform

The whole extension layer the 1.12.2 addon hooked into is **gone** in Refactored:

| 1.12.2 (ex112) | 1.20.1 (Refactored) |
|---|---|
| `TeBlockRegistry` + `TeBlockFinalCallEvent` (register your TE into IC2's block) | **removed** — register your own block via Forge `DeferredRegister` |
| `ProfileManager` / `TextureStyle` (the "Classic" skin) | **removed** — no profile system |
| `.ini` recipe files + `Rezepte.getConfigFile` / `Config` | **removed** — define recipes in code |
| `ic2.core.audio.AudioSource` | `ic2.core.sound.Sound` + `Ic2SoundEvents` |

What **survives** and is reused directly: `TileEntityElectricMachine` / `TileEntityStandardMachine`,
the `InvSlot*` slots, the dynamic GUI (`DynamicContainer` + `guidef/*.xml`), the
`Ic2TileEntityBlock.create(...)` machine-block factory, the `ic2.api.recipe.Recipes.*` managers,
and `ConfigUtil` / `StackUtil` / `ReflectionUtil`.

So a "port" is really: keep the processing TEs, rebuild registration + assets from scratch.

---

## 2. Depend on a **dev jar**, not `fg.deobf` on the release jar

IC2's compile-time internals (`ic2.core.*`) are needed. The obvious
`implementation fg.deobf(files("ic2-release.jar"))` **fails to compile**:

> `<MachineTE> is not abstract and does not override abstract method stillValid(Player) in Container`

The release jar is *reobfuscated*, so IC2's overrides of Minecraft methods (`Container.stillValid` →
`m_6542_`, etc.) are stored under SRG names, and `fg.deobf` does **not** reliably remap those
inherited-override names. Your subclass then sees the MC abstract as unimplemented.

**Fix:** vendor a *dev jar* assembled from IC2's compiled, official-named dev output, and consume it
with a plain `files(...)` dependency (no `fg.deobf`):

```sh
cd ../ic2 && ./gradlew build
jar cf ../advanced-machines/libs/ic2-forge-<ver>-dev.jar -C build/classes/java/main . -C build/resources/main .
```

```gradle
implementation files("libs/ic2-forge-${ic2_version}-dev.jar")
```

(Verify with `javap`: the dev jar shows `stillValid(Player)`; the release jar shows `m_6542_`.)

---

## 3. Registration mirrors `TeBlock` via `Ic2TileEntityBlock`

`Ic2TileEntityBlock.create(props, teClass, canActive, DefaultDrop, supportedFacings, allowWrenchRotating)`
is the modern equivalent of the old `TeBlock` — it handles facing/`ACTIVE` state, ticking, GUI-open,
and drops. Register the returned block in your own `DeferredRegister<Block>`.

Two non-obvious details:
- The block creates its BE by **reflection** on the TE class constructor `(BlockPos, BlockState)` —
  but you still must register a `BlockEntityType` separately (the TE passes it to `super(...)` and
  needs a valid `type()`). `BlockEntityType.Builder.of(TE::new, block).build(null)`.
- Reuse the shared menu `Ic2ScreenHandlers.DYNAMIC_BE`; don't register your own menu type.

The heat/spin-up base (`TileEntityHeatingMachine`) is rebuilt on `TileEntityElectricMachine`:
`load`/`saveAdditional(CompoundTag)`, `@GuiSynced` fields, `activate(false)`/`shutdown(interrupted)`
for active-state + sound, and `getLoopingSoundEvent()` / `getInterruptSoundEvent()` overrides.

---

## 4. Recipe input: use `forIngredient`, not `forStack`

Registering a machine recipe programmatically:

```java
manager.addRecipe(Recipes.inputFactory.forStack(new ItemStack(SCRAP), 9), meta, false, out); // CRASHES
```

> `UnsupportedOperationException` at `RecipeInputBase.getInputs` → `List.replaceAll`

`RecipeInputItemStack.listStacks()` returns an immutable `List.of(...)`, and IC2's `addRecipe` calls
`getInputs().replaceAll(...)` on it. `forStack`/`forItem` only work on the datapack-serialization path.

**Fix:** use `forIngredient`, whose backing list (`Arrays.asList`) supports `replaceAll`:

```java
manager.addRecipe(Recipes.inputFactory.forIngredient(Ingredient.of(SCRAP), 9), meta, false, out);
```

Also note the manager getters changed shape: `Recipes.macerator` is now
`Recipes.IGetter<IBasicMachineRecipeManager>`, resolved lazily with a `Level` by the input slot.

---

## 5. Dynamic GUI `guidef` isn't found in the addon jar (JPMS)

Opening any machine threw:

> `FileNotFoundException: Could not load /assets/advanced_machines/guidef/<machine>.xml from the classpath`

IC2's `GuiParser` loads the XML via `GuiParser.class.getResourceAsStream(...)`. Under the Forge 1.20.1
**module system** that only resolves resources inside IC2's own jar — not the addon's. (On 1.12.2 all
mods shared one classloader, so it "just worked".)

**Fix:** parse the guidef from the **addon's own** classloader and hand the node to the
`DynamicContainer.create(type, syncId, inv, base, guiNode)` overload, in both the server and client
screen handlers (the client `DynamicGui` renders from `container.guiNode`, so no second lookup):

```java
Method parse = GuiParser.class.getDeclaredMethod("parse", InputStream.class, Class.class); // private
parse.setAccessible(true);
GuiParser.GuiNode node;
try (InputStream is = getClass().getResourceAsStream("/assets/<ns>/guidef/<path>.xml")) {
    node = (GuiParser.GuiNode) parse.invoke(null, new BufferedInputStream(is), getClass());
}
return DynamicContainer.create(Ic2ScreenHandlers.DYNAMIC_BE, syncId, inv, (TileEntityInventory) this, node);
```

---

## 6. `guidef` gauge styles renamed

1.12.2 style strings are invalid in 1.20.1:

> `SAXException: invalid gauge style: progress_extruder`

Valid names are the `Gauge.GaugeStyle` enum constants lowercased: `progresscrush`, `progressdrop`,
`progressrecycler`, `progresstriangle`, `progressarrow`, `progressorewasher`, **`progressmetalformer`**
(the replacement for `progress_extruder`), etc. Slot styles `normal`/`large`/`plain` are unchanged.

---

## 7. GUI title key is `container.*`, not `block.*`

The managed-BE screen title comes from `IHasGui.getBeName`, which builds
`container.<namespace>.<path>` — **not** the block's `block.<namespace>.<path>` key. Add both to your
lang files or the title renders as the raw key.

---

## 8. Texture atlas directory: `block/`, not `blocks/`

The #1 cause of every machine rendering as the missing-texture checkerboard. 1.12.2 textures live in
`textures/blocks/` and `textures/items/` (plural). The 1.20.1 block/item atlas only stitches
**`textures/block/`** and **`textures/item/`** (singular). The PNGs exist but never enter the atlas:

> `Missing textures in model advanced_machines:rotary_macerator#...`

**Fix:** move `textures/blocks → textures/block`, `textures/items → textures/item`, and repoint every
model reference (`advanced_machines:blocks/...` → `advanced_machines:block/...`).

GUI textures (`textures/gui/...`) are loaded directly by IC2's GUI, *not* via the atlas, so they stay.

---

## 9. Resource locations must be lowercase

The centrifuge/washer active models referenced `..._active_A` / `_active_B`:

> `JsonParseException: ...extractor_side_active_B is not valid resource location`

Uppercase is illegal in 1.20.1 resource locations — the model fails to load entirely. Rename to
lowercase (`_a` / `_b`). Also watch for undefined `#texvar` references inherited from 1.12.2 models.

---

## 10. Blockstates & models

- One blockstate JSON per block, keyed on the real properties: `facing` (horizontal) + `active`
  (`Ic2TileEntityBlock` adds `ACTIVE` when `canActive`). Cover all 8 combos.
- IC2's own machines use a custom `"loader": "ic2:be"` model (`DynamicBeModel`) to swap active/inactive
  dynamically. An addon doesn't need it: `Ic2TileEntityBlock` doesn't override `getRenderShape` (default
  `MODEL`), so vanilla `active=true/false` blockstate variants pointing at `_active` models work fine.

---

## What was intentionally dropped / simplified

- The switchable **"Classic" texture profile** (no profile system in Refactored).
- Right-click **bucket fill** on the water machines — fill via fluid pipes/cells instead.
- Animated active models collapse to a single `_active` model.
- **JEI**: machines are registered as catalysts for IC2's categories; the Compacting Recycler has no
  category to attach to (IC2 1.20.1 ships no recycler JEI category) so its scrap→scrap-box extra recipe
  isn't shown in JEI.
