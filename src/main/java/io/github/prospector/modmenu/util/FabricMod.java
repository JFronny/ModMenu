package io.github.prospector.modmenu.util;

import com.google.common.collect.Lists;
import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FabricMod implements Mod {
	private static final Logger LOGGER = LogManager.getLogger("Mod Menu");

	private final ModContainer container;
	private final ModMetadata metadata;

	private final ModMenuData modMenuData;

	private final Set<Badge> badges;

	public FabricMod(ModContainer modContainer) {
		this.container = modContainer;
		this.metadata = modContainer.getMetadata();
		Optional<String> parentId = Optional.empty();
		ModMenuData.DummyParentData parentData = null;
		Set<String> badgeNames = new HashSet<>();
		CustomValue modMenuValue = metadata.getCustomValue("modmenu");
		if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
			CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();
			CustomValue parentCv = modMenuObject.get("parent");
			if (parentCv.getType() == CustomValue.CvType.STRING) {
				parentId = Optional.of(parentCv.getAsString());
			} else if (parentCv.getType() == CustomValue.CvType.OBJECT) {
				try {
					CustomValue.CvObject parentObj = parentCv.getAsObject();
					parentId = CustomValueUtil.getString("id", parentObj);
					parentData = new ModMenuData.DummyParentData(
							parentId.orElseThrow(() -> new RuntimeException("Parent object lacks an id")),
							CustomValueUtil.getString("name", parentObj),
							CustomValueUtil.getString("description", parentObj),
							CustomValueUtil.getString("icon", parentObj),
							CustomValueUtil.getStringSet("badges", parentObj).orElse(new HashSet<>())
					);
				} catch (Throwable t) {
					LOGGER.error("Error loading parent data from mod: " + metadata.getId(), t);
				}
			}
			badgeNames.addAll(CustomValueUtil.getStringSet("badges", modMenuObject).orElse(new HashSet<>()));
		}
		this.modMenuData = new ModMenuData(
				badgeNames,
				parentId,
				parentData
		);
		CustomValueUtil.getString("modmenu:parent", metadata).ifPresent(parent -> {
			modMenuData.parent = Optional.of(parent);
			LOGGER.warn("WARNING! Mod " + metadata.getId() + " is using deprecated 'modmenu:parent' custom value! This will be removed in 1.18 snapshots, so ask the author of this mod to update to the new API.");
		});
		CustomValueUtil.getBoolean("modmenu:clientsideOnly", metadata).ifPresent(client -> {
			if (client) {
				modMenuData.badges.add(Badge.CLIENT);
			}
			LOGGER.warn("WARNING! Mod " + metadata.getId() + " is using deprecated 'modmenu:clientsideOnly' custom value! This will be removed in 1.18 snapshots, so ask the author of this mod to update to the new API.");
		});
		CustomValueUtil.getBoolean("modmenu:api", metadata).ifPresent(library -> {
			if (library) {
				modMenuData.badges.add(Badge.LIBRARY);
			}
			LOGGER.warn("WARNING! Mod " + metadata.getId() + " is using deprecated 'modmenu:api' custom value! This will be removed in 1.18 snapshots, so ask the author of this mod to update to the new API.");
		});
		CustomValueUtil.getBoolean("modmenu:deprecated", metadata).ifPresent(deprecated -> {
			if (deprecated) {
				modMenuData.badges.add(Badge.DEPRECATED);
			}
			LOGGER.warn("WARNING! Mod " + metadata.getId() + " is using deprecated 'modmenu:deprecated' custom value! This will be removed in 1.18 snapshots, so ask the author of this mod to update to the new API.");
		});
		HardcodedUtil.hardcodeModMenuData(metadata, modMenuData);
		this.badges = modMenuData.badges;
		if (this.metadata.getEnvironment() == ModEnvironment.CLIENT) {
			badges.add(Badge.CLIENT);
		}
		if (OptionalUtil.isPresentAndTrue(CustomValueUtil.getBoolean("fabric-loom:generated", metadata))) {
			badges.add(Badge.LIBRARY);
		}
		if (metadata.containsCustomValue("patchwork:patcherMeta")) {
			badges.add(Badge.PATCHWORK_FORGE);
		}
		if ("minecraft".equals(getId())) {
			badges.add(Badge.MINECRAFT);
		}
	}

	@Override
	public @NotNull String getId() {
		return metadata.getId();
	}

	@Override
	public @NotNull String getName() {
		return metadata.getName();
	}

	@Override
	public @NotNull NativeImageBackedTexture getIcon(ModIconHandler iconHandler, int i) {
		String iconSourceId = getId();
		String iconPath = metadata.getIconPath(i).orElse(null);
		if ("minecraft".equals(getId())) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/mc_icon.png";
		} else if (iconPath == null) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/unknown_icon.png";
		}
		final String finalIconSourceId = iconSourceId;
		ModContainer iconSource = FabricLoader.getInstance().getModContainer(iconSourceId).orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + finalIconSourceId));
		NativeImageBackedTexture icon = iconHandler.createIcon(iconSource, iconPath);
		if (icon == null) {
			LOGGER.warn("Warning! Mod {} has a broken icon, loading default icon", metadata.getId());
			return iconHandler.createIcon(FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + ModMenu.MOD_ID)), "assets/" + ModMenu.MOD_ID + "/unknown_icon.png");
		}
		return icon;
	}

	@Override
	public @NotNull String getSummary() {
		return getDescription();
	}

	@Override
	public @NotNull String getDescription() {
		String description = metadata.getDescription();
		if ("minecraft".equals(getId()) && description.isEmpty()) {
			return "The base game.";
		}
		return description;
	}

	@Override
	public @NotNull String getVersion() {
		return metadata.getVersion().getFriendlyString();
	}

	@Override
	public @NotNull List<String> getAuthors() {
		List<String> authors = metadata.getAuthors().stream().map(Person::getName).collect(Collectors.toList());
		if ("minecraft".equals(getId()) && authors.isEmpty()) {
			return Lists.newArrayList("Mojang Studios");
		}
		return authors;
	}

	@Override
	public @NotNull Set<Badge> getBadges() {
		return badges;
	}

	@Override
	public @Nullable String getWebsite() {
		return metadata.getContact().get("homepage").orElse(null);
	}

	@Override
	public @Nullable String getIssueTracker() {
		return metadata.getContact().get("issues").orElse(null);
	}

	@Override
	public @Nullable String getParent() {
		return modMenuData.parent.orElse(null);
	}

	public ModMenuData getModMenuData() {
		return modMenuData;
	}

	static class ModMenuData {
		private Set<Badge> badges;
		private Optional<String> parent;
		private @Nullable DummyParentData dummyParentData;

		public ModMenuData(Set<String> badges, Optional<String> parent, DummyParentData dummyParentData) {
			this.badges = Badge.convert(badges);
			this.parent = parent;
			this.dummyParentData = dummyParentData;
		}

		public Set<Badge> getBadges() {
			return badges;
		}

		public Optional<String> getParent() {
			return parent;
		}

		public @Nullable DummyParentData getDummyParentData() {
			return dummyParentData;
		}

		public void addClientBadge(boolean add) {
			if (add) {
				badges.add(Badge.CLIENT);
			}
		}

		public void addLibraryBadge(boolean add) {
			if (add) {
				badges.add(Badge.LIBRARY);
			}
		}

		public void fillParentIfEmpty(String parent) {
			if (!this.parent.isPresent()) {
				this.parent = Optional.of(parent);
			}
		}

		public static class DummyParentData {
			private String id;
			private Optional<String> name, description, icon;
			private Set<Badge> badges;

			public DummyParentData(String id, Optional<String> name, Optional<String> description, Optional<String> icon, Set<String> badges) {
				this.id = id;
				this.name = name;
				this.description = description;
				this.icon = icon;
				this.badges = Badge.convert(badges);
			}

			public String getId() {
				return id;
			}

			public Optional<String> getName() {
				return name;
			}

			public Optional<String> getDescription() {
				return description;
			}

			public Optional<String> getIcon() {
				return icon;
			}

			public Set<Badge> getBadges() {
				return badges;
			}
		}
	}
}
