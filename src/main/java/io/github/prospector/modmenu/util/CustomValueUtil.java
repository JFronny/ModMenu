package io.github.prospector.modmenu.util;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CustomValueUtil {
	public static Optional<Boolean> getBoolean(String key, ModMetadata metadata) {
		if (metadata.containsCustomValue(key)) {
			return Optional.of(metadata.getCustomValue(key).getAsBoolean());
		}
		return Optional.empty();
	}

	public static Optional<String> getString(String key, ModMetadata metadata) {
		if (metadata.containsCustomValue(key)) {
			return Optional.of(metadata.getCustomValue(key).getAsString());
		}
		return Optional.empty();
	}

	public static Optional<Boolean> getBoolean(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			return Optional.of(object.get(key).getAsBoolean());
		}
		return Optional.empty();
	}

	public static Optional<String> getString(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			return Optional.of(object.get(key).getAsString());
		}
		return Optional.empty();
	}

	public static Optional<String[]> getStringArray(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			CustomValue.CvArray cvArray = object.get(key).getAsArray();
			String[] strings = new String[cvArray.size()];
			for (int i = 0; i < cvArray.size(); i++) {
				strings[i] = cvArray.get(i).getAsString();
			}
			return Optional.of(strings);
		}
		return Optional.empty();
	}

	public static Optional<Set<String>> getStringSet(String key, CustomValue.CvObject object) {
		if (object.containsKey(key)) {
			Set<String> strings = new HashSet<>();
			for (CustomValue value : object.get(key).getAsArray()) {
				strings.add(value.getAsString());
			}
			return Optional.of(strings);
		}
		return Optional.empty();
	}
}
