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

import com.tibetiroka.deblint.Linters.TypeCopyrightLinter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public final class TypeCopyrightLinterTest {
	@ParameterizedTest
	@CsvSource({"*,*,true", "a,*,false", "a,?,false", "*,a,true", "*,?,true", "?,*,false", "??????????,*,false", "*,??????????,true", "?,a,true", "a*a,aa?a,true", "aa?a,a*a,false", "images/ship/pointedstick?vanguard*,images/ship/pointedstick?vanguard*,true"})
	public void isMoreGeneric(String a, String b, boolean result) {
		assertEquals(result, new TypeCopyrightLinter().isMoreGeneric(a, b));
	}

	@ParameterizedTest
	@CsvSource({"a,^(\\./)?a$", "hello?there.txt,^(\\./)?hello.there\\.txt$", "file(name)*,^(\\./)?file\\(name\\).*$"})
	public void toRegex(String pattern, String regex) {
		Pattern p = new TypeCopyrightLinter().toRegex(pattern);
		assertEquals(regex, p.pattern());
	}

	@Test
	public void checkCopyrightNames() {
		Configuration config = Configuration.PRESET_EXACT.clone();
		config.urlExists = false;
		// working config
		assertDoesNotThrow(() -> lint(config, """
				Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
				Upstream-Name: test
				Upstream-Contact: test <test@test.org>
				Source: https://salsa.debian.org/debian/debmake-doc
								
				Files: *
				Copyright: copyright text
				License: test
				 description
				"""));
		// license with duplicated description
		assertThrows(IllegalArgumentException.class, () -> lint(config, """
				Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
				Upstream-Name: test
				Upstream-Contact: test <test@test.org>
				Source: https://salsa.debian.org/debian/debmake-doc
								
				Files: *
				Copyright: copyright text
				License: test
				 description
								
				License: test
				 license body
				"""));
		// same text, but now allowed
		config.licenseDeclaredAfterExplanation = false;
		assertDoesNotThrow(() -> lint(config, """
				Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
				Upstream-Name: test
				Upstream-Contact: test <test@test.org>
				Source: https://salsa.debian.org/debian/debmake-doc
								
				Files: *
				Copyright: copyright text
				License: test
				 description
								
				License: test
				 license body
				"""));
		// missing license stanza for 'test'
		assertThrows(IllegalArgumentException.class, () -> lint(config, """
				Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
				Upstream-Name: test
				Upstream-Contact: test <test@test.org>
				Source: https://salsa.debian.org/debian/debmake-doc
								
				Files: *
				Copyright: copyright text
				License: test
				"""));
		// public domain without explanation
		assertThrows(IllegalArgumentException.class, () -> lint(config, """
				Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
				Upstream-Name: test
				Upstream-Contact: test <test@test.org>
				Source: https://salsa.debian.org/debian/debmake-doc
								
				Files: *
				Copyright: copyright text
				License: public-domain
				"""));
		// public domain declared independently
		assertThrows(IllegalArgumentException.class, () -> lint(config, """
				Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
				Upstream-Name: test
				Upstream-Contact: test <test@test.org>
				Source: https://salsa.debian.org/debian/debmake-doc
								
				Files: *
				Copyright: copyright text
				License: public-domain
				 body
								
				License: public-domain
				 stuff
				"""));
		// valid public domain
		assertDoesNotThrow(() -> lint(config, """
				Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
				Upstream-Name: test
				Upstream-Contact: test <test@test.org>
				Source: https://salsa.debian.org/debian/debmake-doc
								
				Files: *
				Copyright: copyright text
				License: public-domain
				 description
				"""));
	}

	private void lint(Configuration config, String text) throws Exception {
		ControlFile file = new ControlFile(config);
		file.parse(Arrays.asList(text.split("\\n")));
		file.matchStanzas();
		file.lintStanzas();
	}
}