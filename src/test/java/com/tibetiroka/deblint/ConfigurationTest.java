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
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {
	@Test
	void checkExact() {
		Configuration c = Configuration.PRESET_EXACT;
		Configuration.getChecks().forEach(f -> {
			try {
				assertTrue((Boolean) f.get(c), f.getName());
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	void checkPresetSeverity() {
		List<Configuration> severity = Configuration.getPrecedenceList();
		for(int i = 0; i < severity.size(); i++) {
			final int k = i;
			for(int j = i + 1; j < severity.size(); j++) {
				final int k1 = j;
				Configuration.getChecks().forEach(f -> {
					try {
						if((Boolean) f.get(severity.get(k))) {
							assertTrue((Boolean) f.get(severity.get(k1)), f.getName());
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
		Configuration.getChecks().forEach(f -> {
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
		HashSet<String> configs = Configuration.getChecks().stream()
		                                       .map(Field::getName)
		                                       .collect(Collectors.toCollection(HashSet::new));
		assertEquals(docsFields, configs);
	}

	@Test
	void presetSeverityList() {
		Assertions.assertEquals(Configuration.getPresets().size(), Configuration.getPrecedenceList().size());
		Assertions.assertEquals(Configuration.getPresets().stream().map(f -> {
			try {
				return f.get(null);
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toSet()), new HashSet<>(Configuration.getPrecedenceList()));
	}
}