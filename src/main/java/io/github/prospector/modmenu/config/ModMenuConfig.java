package io.github.prospector.modmenu.config;

import com.google.gson.annotations.SerializedName;
import io.github.prospector.modmenu.util.Mod;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ModMenuConfig {
	private boolean showLibraries = false;
	private boolean hideConfigButtons = false;
	private boolean hideBadges = false;
	private Sorting sorting = Sorting.ASCENDING;
	private Set<String> hiddenMods = new HashSet<>();

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = Sorting.values()[(getSorting().ordinal() + 1) % Sorting.values().length];
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public boolean isHidingConfigurationButtons() {
		return hideConfigButtons;
	}

	public Sorting getSorting() {
		return sorting == null ? Sorting.ASCENDING : sorting;
	}

	public void hideMod(String id) {
		hiddenMods.add(id);
	}

	public void unhideMod(String id) {
		hiddenMods.remove(id);
	}

	public boolean isModHidden(String id) {
		return hiddenMods.contains(id);
	}

	public Set<String> getHiddenMods() {
		return hiddenMods;
	}

	public boolean areBadgesHidden() {
		return hideBadges;
	}

	public enum Sorting {
		@SerializedName("ascending")
		ASCENDING(Comparator.comparing(mod -> mod.getName().toLowerCase()), "modmenu.sorting.ascending"),
		@SerializedName("descending")
		DESCENDING(ASCENDING.getComparator().reversed(), "modmenu.sorting.decending");

		Comparator<Mod> comparator;
		String translationKey;

		Sorting(Comparator<Mod> comparator, String translationKey) {
			this.comparator = comparator;
			this.translationKey = translationKey;
		}

		public Comparator<Mod> getComparator() {
			return comparator;
		}

		public String getTranslationKey() {
			return translationKey;
		}
	}
}
