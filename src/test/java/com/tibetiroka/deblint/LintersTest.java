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
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Very Smart Person", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Very Smart Person <>", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("<smart.person@very.com>", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Very Smart. Person <smart.person@very.com>", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Very Smart Person <smart.person@very.com> extra stuff", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("Very Smart Person <smart.person@very.com>", Configuration.PRESET_EXACT));
	}

	@Test
	public void architectureLinterTest() {
		var linter = Linters.ARCHITECTURE_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("hi !there", Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept("hi there", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("őúúőő", Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept("linux-any", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("nonexistentos-any", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("nonexistentos-any", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("any-x85", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("any-x85", Configuration.PRESET_QUIRKS));
	}

	@Test
	public void binaryListLinter() {
		var linter = Linters.BINARY_LIST_LINTER;
		Configuration config = Configuration.PRESET_EXACT.clone();
		config.checkedType = ControlType.SOURCE_CONTROL;
		assertDoesNotThrow(() -> linter.accept("hello,there", config));
		config.checkedType = ControlType.CHANGES;
		assertDoesNotThrow(() -> linter.accept("hello there", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("hello hello", config));
	}

	@Test
	public void booleanLinter() {
		var linter = Linters.BOOLEAN_LINTER;
		assertDoesNotThrow(() -> linter.accept("yes", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("no", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("true", Configuration.PRESET_EXACT));
	}

	@Test
	public void changeListLinter() {
		var linter = Linters.CHANGE_LIST_LINTER;
		assertDoesNotThrow(() -> linter.accept("\nhello", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("hello\n", Configuration.PRESET_EXACT));
	}

	@Test
	public void copyrightFileListLinter() {
		var linter = Linters.COPYRIGHT_FILE_LIST_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\\a", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("hello there", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("hello?there*", Configuration.PRESET_EXACT));
	}

	@Test
	public void copyrightFormatLinter() {
		var linter = Linters.COPYRIGHT_FORMAT_LINTER;
		Configuration config = Configuration.PRESET_QUIRKS.clone();
		config.strictCopyrightFormatVersion = true;
		assertDoesNotThrow(() -> linter.accept("https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("1.0", config));
	}

	@Test
	public void copyrightSourceLinter() {
		var linter = Linters.COPYRIGHT_SOURCE_LINTER;
		Configuration config = Configuration.PRESET_QUIRKS.clone();
		config.copyrightSourceStyle = true;
		assertDoesNotThrow(() -> linter.accept("https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("package name", config));
	}

	@Test
	public void dateLinter() {
		var linter = Linters.DATE_LINTER;
		assertDoesNotThrow(() -> linter.accept("Wed, 11 Apr 2001 20:18:20 +0100", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("wed, 11 Apr 2001 20:18:20 +0100", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Wed, 81 Apr 2001 20:18:20 +0100", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Wed, 11 apr 2001 20:18:20 +0100", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Wed, 11 Apr 2101 20:18:20 +0100", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Wed, 11 Apr 2001 25:18:20 +0100", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Wed, 11 Apr 2001 20:95:20 +0100", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Wed, 11 Apr 2001 20:18:91 +0100", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Wed, 11 Apr 2001 20:18:20 +2500", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("Wed, 11 Apr 2001 20:18:20 +0199", Configuration.PRESET_EXACT));
	}

	@Test
	public void dependencyLinter() {
		var linter = Linters.DEPENDENCY_LINTER;
		assertDoesNotThrow(() -> linter.accept("libc6 (>= 2.2.1), default-mta | mail-transport-agent", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("kernel-headers-2.2.10 [!hurd-i386],  hurd-dev [hurd-i386], gnumach-dev [hurd-i386]", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("libluajit5.1-dev [i386 amd64 kfreebsd-i386 armel armhf powerpc mips],  liblua5.1-dev [hurd-i386 ia64 kfreebsd-amd64 s390x sparc]", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("libluajit5.1-dev [!i386 amd64 kfreebsd-i386 armel armhf powerpc mips],  liblua5.1-dev [hurd-i386 ia64 kfreebsd-amd64 s390x sparc]", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("foo [i386], bar [amd64]", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("foo [!i386] | bar [!amd64]", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("foo [linux-any], bar [any-i386], baz [!linux-any]", Configuration.PRESET_EXACT));
	}

	@Test
	public void descriptionLinter() {
		var linter = Linters.DESCRIPTION_LINTER;
		assertDoesNotThrow(() -> linter.accept("hello", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\nhello", Configuration.PRESET_EXACT));
	}

	@Test
	public void dgitLinter() {
		var linter = Linters.DGIT_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().repeat('ű', 40).toString(), Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().repeat('a', 41).toString(), Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept(new StringBuilder().repeat("a", 40).toString(), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().repeat('a', 40).append(" hello").toString(), Configuration.PRESET_EXACT));
	}

	@Test
	public void distributionLinter() {
		var linter = Linters.DISTRIBUTION_LINTER;
		assertDoesNotThrow(() -> linter.accept("hello", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("hello there", Configuration.PRESET_EXACT));
	}

	@Test
	public void exactDependencyLinter() {
		var linter = Linters.EXACT_DEPENDENCY_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("libc6 (>= 2.2.1), default-mta | mail-transport-agent", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("libc6 (= 2.2.1), default-mta | mail-transport-agent", Configuration.PRESET_EXACT));
	}

	@Test
	public void fileListLinter() {
		var linter = Linters.FILE_LIST_LINTER;
		Configuration config = Configuration.PRESET_EXACT.clone();
		config.checkedType = ControlType.SOURCE_CONTROL;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("hello", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\n\tc6f698f19f2a2aa07dbb9bbda90a2754 571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\n  c6f698f19f2a2aa07dbb9bbda90a2754 571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\n c6f698f19f2a2aa07dbb9bbda90a54 571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\n  c6f698f19f2a2aa07dbb9bbda90a2754 +571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\n c6f698f19f2a2aa07dbb9bbda90a2754 -1 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", config));
		assertDoesNotThrow(() -> linter.accept("\n c6f698f19f2a2aa07dbb9bbda90a2754 571925 example_1.2.orig.tar.gz\n 938512f08422f3509ff36f125f5873ba 6220 example_1.2-1.diff.gz", config));
		//
		config.checkedType = ControlType.CHANGES;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\n 4c31ab7bfc40d3cf49d7811987390357 1428 text extra example_1.2-1.dsc\n" +
		                                                                 " c6f698f19f2a2aa07dbb9bbda90a2754 571925 text extra example_1.2.orig.tar.gz\n" +
		                                                                 " 938512f08422f3509ff36f125f5873ba 6220 text extra example_1.2-1.diff.gz\n" +
		                                                                 " 7c98fe853b3bbb47a00e5cd129b6cb 703542 text extra example_1.2-1_i386.deb", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\n 4c31ab7bfc40d3cf49d7811987390357 1428 text extra example_1.2-1.dsc\n" +
		                                                                 " c6f698f19f2a2aa07dbb9bbda90a2754 571925 text extra\n" +
		                                                                 " 938512f08422f3509ff36f125f5873ba 6220 text extra example_1.2-1.diff.gz\n" +
		                                                                 " 7c98fe853b3bbb47a00e5cd129b6cb 703542 text extra example_1.2-1_i386.deb", config));
		assertDoesNotThrow(() -> linter.accept("\n 4c31ab7bfc40d3cf49d7811987390357 1428 text optional example_1.2-1.dsc\n" +
		                                       " c6f698f19f2a2aa07dbb9bbda90a2754 571925 text optional example_1.2.orig.tar.gz\n" +
		                                       " 938512f08422f3509ff36f125f5873ba 6220 text optional example_1.2-1.diff.gz\n" +
		                                       " 7c98fe853b3bbb47a00e5cd129b6cb56 703542 text optional example_1.2-1_i386.deb", config));
	}

	@Test
	public void formatVersionLinter() {
		var linter = Linters.FORMAT_VERSION_LINTER;
		Configuration config = Configuration.PRESET_QUIRKS.clone();
		config.checkedType = ControlType.CHANGES;
		assertDoesNotThrow(() -> linter.accept("1.0", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("alpha", config));
		//
		config.checkedType = ControlType.SOURCE_CONTROL;
		assertDoesNotThrow(() -> linter.accept("1.0", config));
		assertDoesNotThrow(() -> linter.accept("3.0 (quilt)", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("alpha", config));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("1.0 alpha", config));
	}

	@Test
	public void gitVcsLinter() {
		var linter = Linters.GIT_VCS_LINTER;
		assertDoesNotThrow(() -> linter.accept("https://example.org/repo -b debian [p/package]", Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept("https://example.org/repo [p/package]", Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept("https://example.org/repo", Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept("https://example.org/repo -b debian", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("example.org/repo -b debian [p/package]", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("https://example.org/repo debian [p/package]", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("https://example.org/repo -b debian p/package", Configuration.PRESET_QUIRKS));
	}

	@Test
	public void licenseLinter() {
		var linter = Linters.LICENSE_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\nhi", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("hi", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("a, or b and c", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("a with OpenSSL exception and C", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("license name", Configuration.PRESET_EXACT));
	}

	@Test
	public void md5Linter() {
		var linter = Linters.MD5_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().repeat('ő', 32).toString(), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().repeat('a', 33).toString(), Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new StringBuilder().repeat('a', 32).toString(), Configuration.PRESET_EXACT));
	}

	@Test
	public void mercurialVcsLinter() {
		var linter = Linters.MERCURIAL_VCS_LINTER;
		assertDoesNotThrow(() -> linter.accept("https://example.org/repo -b debian", Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept("https://example.org/repo", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("https://example.org/repo -b debian [p/package]", Configuration.PRESET_QUIRKS));
	}

	@Test
	public void multiAddressLinter() {
		var linter = Linters.MULTI_ADDRESS_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(",", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("invalid address", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("Very Smart <smart@person>, Smart Very <person@smart>", Configuration.PRESET_EXACT));
	}

	@Test
	public void numberListLinter() {
		var linter = Linters.NUMBER_LIST_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("a", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("#8", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("3 3", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("+3", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("-3", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("0", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("3 4", Configuration.PRESET_EXACT));
	}

	@Test
	public void packageListLinter() {
		var linter = Linters.PACKAGE_LIST_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("text", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\nname deb admin", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("\nname thing admin standard", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("\nname udeb admin standard", Configuration.PRESET_EXACT));
	}

	@Test
	public void packageNameLinter() {
		var linter = Linters.PACKAGE_NAME_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("a", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("helló", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("-21", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(".hi", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("package-32.2", Configuration.PRESET_EXACT));
	}

	@Test
	public void packageTypeLinter() {
		var linter = Linters.PACKAGE_TYPE_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("random", Configuration.PRESET_EXACT));
		Configuration config = Configuration.PRESET_EXACT.clone();
		config.checkedType = ControlType.SOURCE_PACKAGE_CONTROL;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("deb", config));
		assertDoesNotThrow(() -> linter.accept("udeb", config));
		assertDoesNotThrow(() -> linter.accept("deb", Configuration.PRESET_EXACT));
	}

	@Test
	public void priorityLinter() {
		var linter = Linters.PRIORITY_LINTER;
		assertDoesNotThrow(() -> linter.accept("something", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("something", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("extra", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("important", Configuration.PRESET_EXACT));
	}

	@Test
	public void requiresRootLinter() {
		var linter = Linters.REQUIRES_ROOT_LINTER;
		assertDoesNotThrow(() -> linter.accept("no", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("binary-targets", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("hello/there", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("dpkg/target/foo", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("/hello/", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("hi /there", Configuration.PRESET_EXACT));
	}

	// todo: stanza and type linter tests

	@Test
	public void rfc822Linter() {
		var linter = Linters.RFC_822_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("well hello there", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("user@localhost", Configuration.PRESET_EXACT));
	}

	@Test
	public void sectionLinter() {
		var linter = Linters.SECTION_LINTER;
		assertDoesNotThrow(() -> linter.accept("admin", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("contrib/database", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("non-free/debian-installer", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("admin/debian-installer", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("missing-section", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("missing-area/database", Configuration.PRESET_QUIRKS));
	}

	@Test
	public void sha1Linter() {
		var linter = Linters.SHA1_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().repeat('a', 40).toString(), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().append('\n').repeat('a', 40) + " 20", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().append('\n').repeat('a', 40) + " hi hello", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new StringBuilder().append('\n').repeat('a', 40) + " 100 hello", Configuration.PRESET_EXACT));
	}

	@Test
	public void sha256Linter() {
		var linter = Linters.SHA256_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().repeat('a', 64).toString(), Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().append('\n').repeat('a', 64) + " 20", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept(new StringBuilder().append('\n').repeat('a', 64) + " hi hello", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept(new StringBuilder().append('\n').repeat('a', 64) + " 100 hello", Configuration.PRESET_EXACT));
	}

	@Test
	public void singleArchitectureLinter() {
		var linter = Linters.SINGLE_ARCHITECTURE_LINTER;
		//todo
	}

	@Test
	public void sizeLinter() {
		var linter = Linters.SIZE_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("+20", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("-1", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("a", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("20.1", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("0", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("1", Configuration.PRESET_EXACT));
	}

	@Test
	public void sourceLinter() {
		var linter = Linters.SOURCE_LINTER;
		assertDoesNotThrow(() -> linter.accept("package-name (0.10.0)", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("package-name", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("package-name 0.10.0", Configuration.PRESET_EXACT));
	}

	@Test
	public void standardsVersionLinter() {
		var linter = Linters.STANDARDS_VERSION_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("a.b.c.d", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("1.2.3.4.5", Configuration.PRESET_QUIRKS));
		assertDoesNotThrow(() -> linter.accept("1.2.3.4", Configuration.PRESET_QUIRKS));
	}

	@Test
	public void upstreamContactLinter() {
		var linter = Linters.UPSTREAM_CONTACT_LINTER;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("upstream contact", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("https://example.com/", Configuration.PRESET_STRICT));
		assertDoesNotThrow(() -> linter.accept("Smart Person <smart@person>", Configuration.PRESET_EXACT));
	}

	@Test
	public void upstreamVersionLinter() {
		var linter = Linters.UPSTREAM_VERSION_LINTER;
		assertDoesNotThrow(() -> linter.accept("0.1-alpha", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("0.1~alpha", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, (() -> linter.accept("~alpha", Configuration.PRESET_EXACT)));
		assertThrows(IllegalArgumentException.class, (() -> linter.accept("alpha-0.1", Configuration.PRESET_EXACT)));
	}

	@Test
	public void urgencyLinter() {
		var linter = Linters.URGENCY_LINTER;
		assertDoesNotThrow(() -> linter.accept("low (HIGH for users of diversions)", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("high", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("random", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("medium comment", Configuration.PRESET_EXACT));
	}

	@Test
	public void urlLinter() {
		var linter = Linters.URL_LINTER;
		assertDoesNotThrow(() -> linter.accept("https://example.com/", Configuration.PRESET_QUIRKS));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("example.com", Configuration.PRESET_QUIRKS));
		Configuration config = Configuration.PRESET_QUIRKS.clone();
		config.urlForceHttps = true;
		assertThrows(IllegalArgumentException.class, () -> linter.accept("http://example.com", config));
	}

	@Test
	public void versionLinter() {
		var linter = Linters.VERSION_LINTER;
		assertDoesNotThrow(() -> linter.accept("0:1.0str-ing-alp~ha", Configuration.PRESET_EXACT));
		assertDoesNotThrow(() -> linter.accept("0.1", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("-1:1.0str-ing-alp~ha", Configuration.PRESET_EXACT));
		assertThrows(IllegalArgumentException.class, () -> linter.accept("1.0str-ing-alp#ha", Configuration.PRESET_EXACT));
	}
}