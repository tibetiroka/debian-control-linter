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

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class SelfTest {
	@Test
	public void copyrightTest() {
		Configuration config = Configuration.PRESET_EXACT.clone();
		config.urlExists = false;
		config.apply();
		Main.lint(config);
	}

	@Test
	public void versionTest() throws IOException {
		if(!Main.IS_NATIVE_IMAGE) {
			{
				// check version in man page
				BufferedReader reader = new BufferedReader(new FileReader("debian-control-linter.1"));
				String line = reader.readLine();
				String end = line.split(" (?=[^ ]*$)")[1].replace("\"", "");
				assertEquals(Main.VERSION, end);
				reader.close();
			}
			{
				// check version in pom
				BufferedReader reader = new BufferedReader(new FileReader("pom.xml"));
				Pattern version = Pattern.compile(".*<version>.*</version>.*");
				for(String line = reader.readLine(); line != null; line = reader.readLine()) {
					if(version.matcher(line).matches()) {
						Pattern extractor = Pattern.compile("(?<=<version>).*(?=</version>)");
						Matcher matcher = extractor.matcher(line);
						matcher.find();
						String ver = matcher.group();
						assertEquals(Main.VERSION, ver);
						break;
					}
				}
				reader.close();
			}
		}
	}
}