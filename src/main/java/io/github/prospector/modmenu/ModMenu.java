package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.FabricDummyParentMod;
import io.github.prospector.modmenu.util.FabricMod;
import io.github.prospector.modmenu.util.Mod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screen.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	private static ImmutableMap<String, ConfigScreenFactory<?>> configScreenFactories = ImmutableMap.of();

	private static int cachedDisplayedModCount = -1;

	public static boolean hasConfigScreenFactory(String modid) {
		return configScreenFactories.containsKey(modid);
	}

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
		return factory != null ? factory.create(menuScreen) : null;
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
		FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
			ModMenuApi api = entrypoint.getEntrypoint();
			factories.put(entrypoint.getProvider().getMetadata().getId(), api.getModConfigScreenFactory());
			api.getProvidedConfigScreenFactories().forEach(factories::putIfAbsent);
		});
		configScreenFactories = new ImmutableMap.Builder<String, ConfigScreenFactory<?>>().putAll(factories).build();


		// Fill mods map
		ModMenuConfig config = ModMenuConfigManager.getConfig();
		for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
			if (!config.isModHidden(modContainer.getMetadata().getId())) {
				FabricMod mod = new FabricMod(modContainer);
				MODS.put(mod.getId(), mod);
			}
		}

		Map<String, Mod> dummyParents = new HashMap<>();

		// Initialize parent map
		for (Mod mod : MODS.values()) {
			String parentId = mod.getParent();
			if (parentId != null) {
				Mod parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
				if (parent == null) {
					if (mod instanceof FabricMod) {
						parent = new FabricDummyParentMod((FabricMod) mod, parentId);
						dummyParents.put(parentId, parent);
					}
				}
				PARENT_MAP.put(parent, mod);
			} else {
				ROOT_MODS.put(mod.getId(), mod);
			}
		}
		MODS.putAll(dummyParents);
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			ModMenuConfig config = ModMenuConfigManager.getConfig();
			// listen, if you have >= 2^32 mods then that's on you
			cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter(mod ->
					mod.getParent() == null &&
							!mod.getBadges().contains(Mod.Badge.LIBRARY) &&
							!config.isModHidden(mod.getId())
			).count());
		}
		return NumberFormat.getInstance().format(cachedDisplayedModCount);
	}
}
