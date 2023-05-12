/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.ui.config;

import java.util.regex.Pattern;

public class CssColorUtils {

	private static final Pattern HEX_RGB_PATTERN = Pattern.compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$");

	public String hexToRgb(String color) {
		if (!HEX_RGB_PATTERN.matcher(color).matches()) {
			throw new IllegalArgumentException(String.format("Invalid hex rgb format %s", color));
		}
		return String.format("%s, %s, %s", Integer.valueOf(color.substring(1, 3), 16),
				Integer.valueOf(color.substring(3, 5), 16), Integer.valueOf(color.substring(5, 7), 16));
	}

}
