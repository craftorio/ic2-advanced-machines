package com.chocohead.AdvMachines;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Client config. The machines ship with the modern IC2 Experimental style textures by default
 * via a bundled resource pack; disabling {@code modernTextures} falls back to the classic
 * IC2-Classic look. Changing it requires a restart (or a resource reload) because it swaps
 * a resource pack.
 */
public final class AdvConfig {
	public static final ForgeConfigSpec CLIENT_SPEC;
	public static final ForgeConfigSpec.BooleanValue MODERN_TEXTURES;

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		MODERN_TEXTURES = builder
				.comment("Machine texture style.",
						"true = modern IC2 Experimental look (default), false = classic IC2-Classic look.",
						"Requires a restart to take effect (it toggles a bundled resource pack).")
				.define("modernTextures", true);
		CLIENT_SPEC = builder.build();
	}

	private AdvConfig() {
	}

	/**
	 * Whether the modern texture overlay should be enabled. Read directly from the config TOML —
	 * {@code AddPackFindersEvent} fires before the {@link ForgeConfigSpec} is loaded, so
	 * {@code MODERN_TEXTURES.get()} would throw there. Defaults to {@code true} (modern), including
	 * on the very first launch before the file exists.
	 */
	public static boolean useModernTextures() {
		try {
			Path cfg = FMLPaths.CONFIGDIR.get().resolve("advanced_machines-client.toml");
			if (Files.exists(cfg)) {
				for (String line : Files.readAllLines(cfg)) {
					String s = line.trim();
					if (s.isEmpty() || s.startsWith("#")) {
						continue;
					}

					String compact = s.replace(" ", "").toLowerCase(Locale.ROOT);
					if (compact.startsWith("moderntextures=")) {
						return compact.equals("moderntextures=true");
					}
				}
			}
		} catch (Exception ignored) {
			// fall through to modern default
		}

		return true;
	}
}
