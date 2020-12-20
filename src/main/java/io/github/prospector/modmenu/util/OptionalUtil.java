package io.github.prospector.modmenu.util;

import java.util.Optional;

public class OptionalUtil {
	public static boolean isPresentAndTrue(Optional<Boolean> optional) {
		return optional.isPresent() && optional.get();
	}
}
