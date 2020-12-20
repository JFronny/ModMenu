package io.github.prospector.modmenu.util;

import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardcodedUtil {
	public static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");

	public static void hardcodeModMenuData(ModMetadata metadata, FabricMod.ModMenuData modMenuData) {
		String id = metadata.getId();
		Matcher matcher = FABRIC_PATTERN.matcher(id);
		if (matcher.matches() || id.equals("fabric-api-base") || id.equals("fabric-renderer-indigo")) {
			modMenuData.fillParentIfEmpty("fabric");
			modMenuData.addLibraryBadge(true);
		}
		modMenuData.addLibraryBadge(id.equals("fabricloader") || id.equals("fabric") || metadata.getName().endsWith(" API"));
	}
}
