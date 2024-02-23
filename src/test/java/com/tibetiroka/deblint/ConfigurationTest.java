/*
 * Copyright (c) 2024 by tibetiroka.
 *
 * debian-control-linter is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * debian-control-linter is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.tibetiroka.deblint;

import com.tibetiroka.deblint.Configuration.ConfigOptionDetails;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {
	@Test
	void checkExact() {
		Configuration c = Configuration.PRESET_EXACT;
		getCheckFields().forEach(f -> {
			try {
				assertTrue((Boolean) f.get(c), f.getName());
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	void checkPresetSeverity() {
		Configuration[] severity = {Configuration.PRESET_QUIRKS, Configuration.PRESET_NORMAL, Configuration.PRESET_STRICT, Configuration.PRESET_EXACT};
		for(int i = 0; i < severity.length; i++) {
			final int k = i;
			for(int j = i + 1; j < severity.length; j++) {
				final int k1 = j;
				getCheckFields().forEach(f -> {
					try {
						if((Boolean) f.get(severity[k])) {
							assertTrue((Boolean) f.get(severity[k1]), f.getName());
						}
					} catch(IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				});
			}
		}
	}

	@Test
	void checkQuirks() {
		Configuration c = Configuration.PRESET_QUIRKS;
		getCheckFields().forEach(f -> {
			try {
				assertFalse((Boolean) f.get(c), f.getName());
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	void docsTest() {
		HashSet<String> docsFields = Arrays.stream(ConfigOptionDetails.class.getDeclaredFields())
		                                   .filter(f -> Modifier.isStatic(f.getModifiers()) && f.getType() == String.class && Modifier.isFinal(f.getModifiers()))
		                                   .filter(f -> {
			                                   try {
				                                   return f.get(null) != null;
			                                   } catch(IllegalAccessException e) {
				                                   throw new RuntimeException(e);
			                                   }
		                                   })
		                                   .map(Field::getName)
		                                   .collect(Collectors.toCollection(HashSet::new));
		HashSet<String> configs = getCheckFields()
				.map(f -> f.getName())
				.collect(Collectors.toCollection(HashSet::new));
		assertEquals(docsFields, configs);
	}

	Stream<Field> getCheckFields() {
		return Arrays.stream(Configuration.class.getDeclaredFields())
		             .filter(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers()) && f.getType() == boolean.class);
	}
}