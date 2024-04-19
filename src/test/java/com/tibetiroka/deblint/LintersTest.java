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

import static org.junit.jupiter.api.Assertions.*;

class LintersTest {
	@Test
	public void addressLinter() {
		var linter = Linters.ADDRESS_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Very Smart Person", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Very Smart Person <>", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("<smart.person@very.com>", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Very Smart. Person <smart.person@very.com>", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Very Smart Person <smart.person@very.com> extra stuff", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("Very Smart Person <smart.person@very.com>", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void architectureLinterTest() {
		var linter = Linters.ARCHITECTURE_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("hi !there", 0), Configuration.PRESET_NORMAL));
		assertDoesNotThrow(() -> linter.accept(new Line("hi there", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("őúúőő", 0), Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept(new Line("linux-any", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("nonexistentos-any", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("nonexistentos-any", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("any-x85", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("any-x85", 0), Configuration.PRESET_QUIRKS));
	}

	@Test
	public void binaryListLinter() {
		var linter = Linters.BINARY_LIST_LINTER;
		Configuration config = Configuration.PRESET_EXACT.clone();
		config.checkedType = ControlType.SOURCE_CONTROL;
		assertDoesNotThrow(() -> linter.accept(new Line("hello,there", 0), config));
		config.checkedType = ControlType.CHANGES;
		assertDoesNotThrow(() -> linter.accept(new Line("hello there", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("hello hello", 0), config));
	}

	@Test
	public void booleanLinter() {
		var linter = Linters.BOOLEAN_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("yes", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("no", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("true", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void changeListLinter() {
		var linter = Linters.CHANGE_LIST_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("\nhello", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("hello\n", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void copyrightFileListLinter() {
		var linter = Linters.COPYRIGHT_FILE_LIST_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\\a", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("hello there", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("hello?there*", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void copyrightFormatLinter() {
		var linter = Linters.COPYRIGHT_FORMAT_LINTER;
		Configuration config = Configuration.PRESET_QUIRKS.clone();
		config.strictCopyrightFormatVersion = true;
		assertDoesNotThrow(() -> linter.accept(new Line("https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("1.0", 0), config));
	}

	@Test
	public void copyrightSourceLinter() {
		var linter = Linters.COPYRIGHT_SOURCE_LINTER;
		Configuration config = Configuration.PRESET_NORMAL.clone();
		config.copyrightSourceStyle = true;
		assertDoesNotThrow(() -> linter.accept(new Line("https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("package name", 0), config));
	}

	@Test
	public void dateLinter() {
		var linter = Linters.DATE_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("Wed, 11 Apr 2001 20:18:20 +0100", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("wed, 11 Apr 2001 20:18:20 +0100", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Wed, 81 Apr 2001 20:18:20 +0100", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Wed, 11 apr 2001 20:18:20 +0100", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Wed, 11 Apr 2101 20:18:20 +0100", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Wed, 11 Apr 2001 25:18:20 +0100", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Wed, 11 Apr 2001 20:95:20 +0100", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Wed, 11 Apr 2001 20:18:91 +0100", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Wed, 11 Apr 2001 20:18:20 +2500", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("Wed, 11 Apr 2001 20:18:20 +0199", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void dependencyLinter() {
		var linter = Linters.DEPENDENCY_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("libc6 (>= 2.2.1), default-mta | mail-transport-agent", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("kernel-headers-2.2.10 [!hurd-i386],  hurd-dev [hurd-i386], gnumach-dev [hurd-i386]", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("libluajit5.1-dev [i386 amd64 kfreebsd-i386 armel armhf powerpc mips],  liblua5.1-dev [hurd-i386 ia64 kfreebsd-amd64 s390x sparc]", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("libluajit5.1-dev [!i386 amd64 kfreebsd-i386 armel armhf powerpc mips],  liblua5.1-dev [hurd-i386 ia64 kfreebsd-amd64 s390x sparc]", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("foo [i386], bar [amd64]", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("foo [!i386] | bar [!amd64]", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("foo [linux-any], bar [any-i386], baz [!linux-any]", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void descriptionLinter() {
		var linter = Linters.DESCRIPTION_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("hello", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\nhello", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void dgitLinter() {
		var linter = Linters.DGIT_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().repeat('ű', 40).toString(), 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().repeat('a', 41).toString(), 0), Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept(new Line(new StringBuilder().repeat("a", 40).toString(), 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().repeat('a', 40).append(" hello").toString(), 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void distributionLinter() {
		var linter = Linters.DISTRIBUTION_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("hello", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("hello there", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void exactDependencyLinter() {
		var linter = Linters.EXACT_DEPENDENCY_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("libc6 (>= 2.2.1), default-mta | mail-transport-agent", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("libc6 (= 2.2.1), default-mta | mail-transport-agent", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void fileListLinter() {
		var linter = Linters.FILE_LIST_LINTER;
		Configuration config = Configuration.PRESET_EXACT.clone();
		config.checkedType = ControlType.SOURCE_CONTROL;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("hello", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\n\tc6f698f19f2a2aa07dbb9bbda90a2754 571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\n  c6f698f19f2a2aa07dbb9bbda90a2754 571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\n c6f698f19f2a2aa07dbb9bbda90a54 571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\n  c6f698f19f2a2aa07dbb9bbda90a2754 +571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\n c6f698f19f2a2aa07dbb9bbda90a2754 -1 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", 0), config));
		assertDoesNotThrow(() -> linter.accept(new Line("\n c6f698f19f2a2aa07dbb9bbda90a2754 571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", 0), config));
		//
		config.checkedType = ControlType.CHANGES;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\n 4c31ab7bfc40d3cf49d7811987390357 1428 text extra example_1.2-1.dsc\n" +
		                                                                 " c6f698f19f2a2aa07dbb9bbda90a2754 571925 text extra example_1.2.orig.tar.gz\n" +
		                                                                 " 938512f08422f3509ff36f125f5873ba 6220 text extra example_1.2-1.diff.gz\n" +
		                                                                          " 7c98fe853b3bbb47a00e5cd129b6cb 703542 text extra example_1.2-1_i386.deb", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\n 4c31ab7bfc40d3cf49d7811987390357 1428 text extra example_1.2-1.dsc\n" +
		                                                                 " c6f698f19f2a2aa07dbb9bbda90a2754 571925 text extra\n" +
		                                                                 " 938512f08422f3509ff36f125f5873ba 6220 text extra example_1.2-1.diff.gz\n" +
		                                                                          " 7c98fe853b3bbb47a00e5cd129b6cb 703542 text extra example_1.2-1_i386.deb", 0), config));
		assertDoesNotThrow(() -> linter.accept(new Line("\n 4c31ab7bfc40d3cf49d7811987390357 1428 text optional example_1.2-1.dsc\n" +
		                                       " c6f698f19f2a2aa07dbb9bbda90a2754 571925 text optional example_1.2.orig.tar.gz\n" +
		                                       " 938512f08422f3509ff36f125f5873ba 6220 text optional example_1.2-1.diff.gz\n" +
		                                                " 7c98fe853b3bbb47a00e5cd129b6cb56 703542 text optional example_1.2-1_i386.deb", 0), config));
	}

	@Test
	public void formatVersionLinter() {
		var linter = Linters.FORMAT_VERSION_LINTER;
		Configuration config = Configuration.PRESET_NORMAL.clone();
		config.checkedType = ControlType.CHANGES;
		assertDoesNotThrow(() -> linter.accept(new Line("1.0", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("alpha", 0), config));
		//
		config.checkedType = ControlType.SOURCE_CONTROL;
		assertDoesNotThrow(() -> linter.accept(new Line("1.0", 0), config));
		assertDoesNotThrow(() -> linter.accept(new Line("3.0 (quilt)", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("alpha", 0), config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("1.0 alpha", 0), config));
	}

	@Test
	public void gitVcsLinter() {
		var linter = Linters.GIT_VCS_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("https://example.org/repo -b debian [p/package]", 0), Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept(new Line("https://example.org/repo [p/package]", 0), Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept(new Line("https://example.org/repo", 0), Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept(new Line("https://example.org/repo -b debian", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("example.org/repo -b debian [p/package]", 0), Configuration.PRESET_NORMAL));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("https://example.org/repo debian [p/package]", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("https://example.org/repo -b debian p/package", 0), Configuration.PRESET_QUIRKS));
	}

	@Test
	public void licenseLinter() {
		var linter = Linters.LICENSE_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\nhi", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("hi", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("a, or b and c", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("a with OpenSSL exception and C", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("license name", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void md5Linter() {
		var linter = Linters.MD5_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().repeat('ő', 32).toString(), 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().repeat('a', 33).toString(), 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line(new StringBuilder().repeat('a', 32).toString(), 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void mercurialVcsLinter() {
		var linter = Linters.MERCURIAL_VCS_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("https://example.org/repo -b debian", 0), Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept(new Line("https://example.org/repo", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("https://example.org/repo -b debian [p/package]", 0), Configuration.PRESET_QUIRKS));
	}

	@Test
	public void multiAddressLinter() {
		var linter = Linters.MULTI_ADDRESS_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(",", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("invalid address", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("Very Smart <smart@person>, Smart Very <person@smart>", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void numberListLinter() {
		var linter = Linters.NUMBER_LIST_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("a", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("#8", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("3 3", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("+3", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("-3", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("0", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("3 4", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void packageListLinter() {
		var linter = Linters.PACKAGE_LIST_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("text", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\nname deb admin", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("\nname thing admin standard", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("\nname udeb admin standard", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void packageNameLinter() {
		var linter = Linters.PACKAGE_NAME_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("a", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("helló", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("-21", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(".hi", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("package-32.2", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void packageTypeLinter() {
		var linter = Linters.PACKAGE_TYPE_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("random", 0), Configuration.PRESET_EXACT));
		Configuration config = Configuration.PRESET_EXACT.clone();
		config.checkedType = ControlType.SOURCE_PACKAGE_CONTROL;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("deb", 0), config));
		assertDoesNotThrow(() -> linter.accept(new Line("udeb", 0), config));
		assertDoesNotThrow(() -> linter.accept(new Line("deb", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void priorityLinter() {
		var linter = Linters.PRIORITY_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("something", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("something", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("extra", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("important", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void requiresRootLinter() {
		var linter = Linters.REQUIRES_ROOT_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("no", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("binary-targets", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("hello/there", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("dpkg/target/foo", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("/hello/", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("hi /there", 0), Configuration.PRESET_EXACT));
	}

	// todo: stanza and type linter tests

	@Test
	public void rfc822Linter() {
		var linter = Linters.RFC_822_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("well hello there", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("user@localhost", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void sectionLinter() {
		var linter = Linters.SECTION_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("admin", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("contrib/database", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("non-free/debian-installer", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("admin/debian-installer", 0), Configuration.PRESET_NORMAL));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("missing-section", 0), Configuration.PRESET_STRICT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("missing-area/database", 0), Configuration.PRESET_STRICT));
	}

	@Test
	public void sha1Linter() {
		var linter = Linters.SHA1_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().repeat('a', 40).toString(), 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().append('\n').repeat('a', 40) + " 20", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().append('\n').repeat('a', 40) + " hi hello", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line(new StringBuilder().append('\n').repeat('a', 40) + " 100 hello", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void sha256Linter() {
		var linter = Linters.SHA256_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().repeat('a', 64).toString(), 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().append('\n').repeat('a', 64) + " 20", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line(new StringBuilder().append('\n').repeat('a', 64) + " hi hello", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line(new StringBuilder().append('\n').repeat('a', 64) + " 100 hello", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void singleArchitectureLinter() {
		var linter = Linters.SINGLE_ARCHITECTURE_LINTER;
		//todo
	}

	@Test
	public void sizeLinter() {
		var linter = Linters.SIZE_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("+20", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("-1", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("a", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("20.1", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("0", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("1", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void sourceLinter() {
		var linter = Linters.SOURCE_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("package-name (0.10.0)", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("package-name", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("package-name 0.10.0", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void standardsVersionLinter() {
		var linter = Linters.STANDARDS_VERSION_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("a.b.c.d", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("1.2.3.4.5", 0), Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept(new Line("1.2.3.4", 0), Configuration.PRESET_QUIRKS));
	}

	@Test
	public void upstreamContactLinter() {
		var linter = Linters.UPSTREAM_CONTACT_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("upstream contact", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("https://example.com/", 0), Configuration.PRESET_STRICT));
		assertDoesNotThrow(() -> linter.accept(new Line("Smart Person <smart@person>", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void upstreamVersionLinter() {
		var linter = Linters.UPSTREAM_VERSION_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("0.1-alpha", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("0.1~alpha", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, (() -> linter.accept(new Line("~alpha", 0), Configuration.PRESET_EXACT)));
		assertThrows(IllegalArgumentException.class, (() -> linter.accept(new Line("alpha-0.1", 0), Configuration.PRESET_EXACT)));
	}

	@Test
	public void urgencyLinter() {
		var linter = Linters.URGENCY_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("low (HIGH for users of diversions)", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("high", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("random", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("medium comment", 0), Configuration.PRESET_EXACT));
	}

	@Test
	public void urlLinter() {
		var linter = Linters.URL_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("https://example.com/", 0), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("example.com", 0), Configuration.PRESET_NORMAL));
		Configuration config = Configuration.PRESET_NORMAL.clone();
		config.urlForceHttps = true;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("http://example.com", 0), config));
	}

	@Test
	public void versionLinter() {
		var linter = Linters.VERSION_LINTER;
		assertDoesNotThrow(() -> linter.accept(new Line("0:1.0str-ing-alp~ha", 0), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new Line("0.1", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("-1:1.0str-ing-alp~ha", 0), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new Line("1.0str-ing-alp#ha", 0), Configuration.PRESET_EXACT));
	}
}