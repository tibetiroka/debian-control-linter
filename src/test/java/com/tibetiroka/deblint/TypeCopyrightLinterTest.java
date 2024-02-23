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
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public final class TypeCopyrightLinterTest {
	@ParameterizedTest
	@CsvSource({"*,*,true", "a,*,false", "a,?,false", "*,a,true", "*,?,true", "?,*,false", "??????????,*,false", "*,??????????,true", "?,a,true", "a*a,aa?a,true", "aa?a,a*a,false"})
	public void isMoreGeneric(String a, String b, boolean result) {
		assertEquals(result, new TypeCopyrightLinter().isMoreGeneric(a, b));
	}

	@ParameterizedTest
	@CsvSource({"a,^(\\./)?a$", "hello?there.txt,^(\\./)?hello.there\\.txt$", "file(name)*,^(\\./)?file\\(name\\).*$"})
	public void toRegex(String pattern, String regex) {
		Pattern p = new TypeCopyrightLinter().toRegex(pattern);
		assertEquals(regex, p.pattern());
	}
}