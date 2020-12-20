package io.github.prospector.modmenu.util;

import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public interface Mod {
	@NotNull
	String getId();

	@NotNull
	String getName();

	@NotNull
	NativeImageBackedTexture getIcon(ModIconHandler iconHandler, int i);

	@NotNull
	String getSummary();

	@NotNull
	String getDescription();

	@NotNull
	String getVersion();

	@NotNull
	List<String> getAuthors();

	@NotNull
	Set<Badge> getBadges();

	@Nullable
	String getWebsite();

	@Nullable
	String getIssueTracker();

	@Nullable
	String getParent();

	enum Badge {
		LIBRARY("modmenu.library", 0xff107454, 0xff093929, "library"),
		CLIENT("modmenu.clientsideOnly", 0xff2b4b7c, 0xff0e2a55, "client"),
		DEPRECATED("modmenu.deprecated", 0xffff3333, 0xffb30000, "deprecated"),
		PATCHWORK_FORGE("modmenu.forge", 0xff1f2d42, 0xff101721, null),
		MINECRAFT("modmenu.minecraft", 0xff6f6c6a, 0xff31302f, null);

		private final Text text;
		private final int outlineColor, fillColor;
		private final String key;
		private static final Map<String, Badge> KEY_MAP = new HashMap<>();

		Badge(String translationKey, int outlineColor, int fillColor, String key) {
			this.text = new TranslatableText(translationKey);
			this.outlineColor = outlineColor;
			this.fillColor = fillColor;
			this.key = key;
		}

		public Text getText() {
			return this.text;
		}

		public int getOutlineColor() {
			return this.outlineColor;
		}

		public int getFillColor() {
			return this.fillColor;
		}

		public static Set<Badge> convert(Set<String> badgeKeys) {
			return badgeKeys.stream().map(KEY_MAP::get).collect(Collectors.toSet());
		}

		static {
			Arrays.stream(values()).forEach(badge -> KEY_MAP.put(badge.key, badge));
		}
	}
}
