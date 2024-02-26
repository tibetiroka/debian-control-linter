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

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import java.io.IOException;
import java.net.*;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.tibetiroka.deblint.FieldSpec.RequirementStatus.MANDATORY;
import static com.tibetiroka.deblint.FieldSpec.RequirementStatus.OPTIONAL;
import static com.tibetiroka.deblint.FieldSpec.RequirementStatus.RECOMMENDED;
import static com.tibetiroka.deblint.FieldType.MULTILINE;
import static com.tibetiroka.deblint.FieldType.SIMPLE;

/**
 * A collection of linters and presets for various control file types.
 */
class Linters {
	/**
	 * Specification preset for {@link ControlType#CHANGES}.
	 */
	public static final List<StanzaSpec> CHANGES_STANZAS = new ArrayList<>();
	/**
	 * Specification preset for {@link ControlType#COPYRIGHT}.
	 */
	public static final List<StanzaSpec> COPYRIGHT_STANZAS = new ArrayList<>();
	/**
	 * Specification preset for {@link ControlType#BINARY_PACKAGE_CONTROL}.
	 */
	public static final List<StanzaSpec> PACKAGE_BINARY_CONTROL_STANZAS = new ArrayList<>();
	/**
	 * Specification preset for {@link ControlType#SOURCE_PACKAGE_CONTROL}.
	 */
	public static final List<StanzaSpec> PACKAGE_SOURCE_CONTROL_STANZAS = new ArrayList<>();
	/**
	 * Specification preset for {@link ControlType#SOURCE_CONTROL}.
	 */
	public static final List<StanzaSpec> SOURCE_CONTROL_STANZAS = new ArrayList<>();
	/**
	 * The list of supported architectures, according to dpkg-architecteure 1.21.1
	 */
	public static final String[] architectures = {"uclibc-linux-armel", "uclibc-linux-i386", "uclibc-linux-ia64", "uclibc-linux-alpha", "uclibc-linux-amd64", "uclibc-linux-arc", "uclibc-linux-armeb", "uclibc-linux-arm", "uclibc-linux-arm64", "uclibc-linux-avr32", "uclibc-linux-hppa", "uclibc-linux-m32r", "uclibc-linux-m68k", "uclibc-linux-mips", "uclibc-linux-mipsel", "uclibc-linux-mipsr6", "uclibc-linux-mipsr6el", "uclibc-linux-mips64", "uclibc-linux-mips64el", "uclibc-linux-mips64r6", "uclibc-linux-mips64r6el", "uclibc-linux-nios2", "uclibc-linux-or1k", "uclibc-linux-powerpc", "uclibc-linux-powerpcel", "uclibc-linux-ppc64", "uclibc-linux-ppc64el", "uclibc-linux-riscv64", "uclibc-linux-s390", "uclibc-linux-s390x", "uclibc-linux-sh3", "uclibc-linux-sh3eb", "uclibc-linux-sh4", "uclibc-linux-sh4eb", "uclibc-linux-sparc", "uclibc-linux-sparc64", "uclibc-linux-tilegx", "musl-linux-armhf", "musl-linux-i386", "musl-linux-ia64", "musl-linux-alpha", "musl-linux-amd64", "musl-linux-arc", "musl-linux-armeb", "musl-linux-arm", "musl-linux-arm64", "musl-linux-avr32", "musl-linux-hppa", "musl-linux-m32r", "musl-linux-m68k", "musl-linux-mips", "musl-linux-mipsel", "musl-linux-mipsr6", "musl-linux-mipsr6el", "musl-linux-mips64", "musl-linux-mips64el", "musl-linux-mips64r6", "musl-linux-mips64r6el", "musl-linux-nios2", "musl-linux-or1k", "musl-linux-powerpc", "musl-linux-powerpcel", "musl-linux-ppc64", "musl-linux-ppc64el", "musl-linux-riscv64", "musl-linux-s390", "musl-linux-s390x", "musl-linux-sh3", "musl-linux-sh3eb", "musl-linux-sh4", "musl-linux-sh4eb", "musl-linux-sparc", "musl-linux-sparc64", "musl-linux-tilegx", "armhf", "armel", "mipsn32", "mipsn32el", "mipsn32r6", "mipsn32r6el", "mips64", "mips64el", "mips64r6", "mips64r6el", "powerpcspe", "x32", "arm64ilp32", "i386", "ia64", "alpha", "amd64", "arc", "armeb", "arm", "arm64", "avr32", "hppa", "m32r", "m68k", "mips", "mipsel", "mipsr6", "mipsr6el", "nios2", "or1k", "powerpc", "powerpcel", "ppc64", "ppc64el", "riscv64", "s390", "s390x", "sh3", "sh3eb", "sh4", "sh4eb", "sparc", "sparc64", "tilegx", "kfreebsd-armhf", "kfreebsd-i386", "kfreebsd-ia64", "kfreebsd-alpha", "kfreebsd-amd64", "kfreebsd-arc", "kfreebsd-armeb", "kfreebsd-arm", "kfreebsd-arm64", "kfreebsd-avr32", "kfreebsd-hppa", "kfreebsd-m32r", "kfreebsd-m68k", "kfreebsd-mips", "kfreebsd-mipsel", "kfreebsd-mipsr6", "kfreebsd-mipsr6el", "kfreebsd-mips64", "kfreebsd-mips64el", "kfreebsd-mips64r6", "kfreebsd-mips64r6el", "kfreebsd-nios2", "kfreebsd-or1k", "kfreebsd-powerpc", "kfreebsd-powerpcel", "kfreebsd-ppc64", "kfreebsd-ppc64el", "kfreebsd-riscv64", "kfreebsd-s390", "kfreebsd-s390x", "kfreebsd-sh3", "kfreebsd-sh3eb", "kfreebsd-sh4", "kfreebsd-sh4eb", "kfreebsd-sparc", "kfreebsd-sparc64", "kfreebsd-tilegx", "knetbsd-i386", "knetbsd-ia64", "knetbsd-alpha", "knetbsd-amd64", "knetbsd-arc", "knetbsd-armeb", "knetbsd-arm", "knetbsd-arm64", "knetbsd-avr32", "knetbsd-hppa", "knetbsd-m32r", "knetbsd-m68k", "knetbsd-mips", "knetbsd-mipsel", "knetbsd-mipsr6", "knetbsd-mipsr6el", "knetbsd-mips64", "knetbsd-mips64el", "knetbsd-mips64r6", "knetbsd-mips64r6el", "knetbsd-nios2", "knetbsd-or1k", "knetbsd-powerpc", "knetbsd-powerpcel", "knetbsd-ppc64", "knetbsd-ppc64el", "knetbsd-riscv64", "knetbsd-s390", "knetbsd-s390x", "knetbsd-sh3", "knetbsd-sh3eb", "knetbsd-sh4", "knetbsd-sh4eb", "knetbsd-sparc", "knetbsd-sparc64", "knetbsd-tilegx", "kopensolaris-i386", "kopensolaris-ia64", "kopensolaris-alpha", "kopensolaris-amd64", "kopensolaris-arc", "kopensolaris-armeb", "kopensolaris-arm", "kopensolaris-arm64", "kopensolaris-avr32", "kopensolaris-hppa", "kopensolaris-m32r", "kopensolaris-m68k", "kopensolaris-mips", "kopensolaris-mipsel", "kopensolaris-mipsr6", "kopensolaris-mipsr6el", "kopensolaris-mips64", "kopensolaris-mips64el", "kopensolaris-mips64r6", "kopensolaris-mips64r6el", "kopensolaris-nios2", "kopensolaris-or1k", "kopensolaris-powerpc", "kopensolaris-powerpcel", "kopensolaris-ppc64", "kopensolaris-ppc64el", "kopensolaris-riscv64", "kopensolaris-s390", "kopensolaris-s390x", "kopensolaris-sh3", "kopensolaris-sh3eb", "kopensolaris-sh4", "kopensolaris-sh4eb", "kopensolaris-sparc", "kopensolaris-sparc64", "kopensolaris-tilegx", "hurd-i386", "hurd-ia64", "hurd-alpha", "hurd-amd64", "hurd-arc", "hurd-armeb", "hurd-arm", "hurd-arm64", "hurd-avr32", "hurd-hppa", "hurd-m32r", "hurd-m68k", "hurd-mips", "hurd-mipsel", "hurd-mipsr6", "hurd-mipsr6el", "hurd-mips64", "hurd-mips64el", "hurd-mips64r6", "hurd-mips64r6el", "hurd-nios2", "hurd-or1k", "hurd-powerpc", "hurd-powerpcel", "hurd-ppc64", "hurd-ppc64el", "hurd-riscv64", "hurd-s390", "hurd-s390x", "hurd-sh3", "hurd-sh3eb", "hurd-sh4", "hurd-sh4eb", "hurd-sparc", "hurd-sparc64", "hurd-tilegx", "darwin-i386", "darwin-ia64", "darwin-alpha", "darwin-amd64", "darwin-arc", "darwin-armeb", "darwin-arm", "darwin-arm64", "darwin-avr32", "darwin-hppa", "darwin-m32r", "darwin-m68k", "darwin-mips", "darwin-mipsel", "darwin-mipsr6", "darwin-mipsr6el", "darwin-mips64", "darwin-mips64el", "darwin-mips64r6", "darwin-mips64r6el", "darwin-nios2", "darwin-or1k", "darwin-powerpc", "darwin-powerpcel", "darwin-ppc64", "darwin-ppc64el", "darwin-riscv64", "darwin-s390", "darwin-s390x", "darwin-sh3", "darwin-sh3eb", "darwin-sh4", "darwin-sh4eb", "darwin-sparc", "darwin-sparc64", "darwin-tilegx", "dragonflybsd-i386", "dragonflybsd-ia64", "dragonflybsd-alpha", "dragonflybsd-amd64", "dragonflybsd-arc", "dragonflybsd-armeb", "dragonflybsd-arm", "dragonflybsd-arm64", "dragonflybsd-avr32", "dragonflybsd-hppa", "dragonflybsd-m32r", "dragonflybsd-m68k", "dragonflybsd-mips", "dragonflybsd-mipsel", "dragonflybsd-mipsr6", "dragonflybsd-mipsr6el", "dragonflybsd-mips64", "dragonflybsd-mips64el", "dragonflybsd-mips64r6", "dragonflybsd-mips64r6el", "dragonflybsd-nios2", "dragonflybsd-or1k", "dragonflybsd-powerpc", "dragonflybsd-powerpcel", "dragonflybsd-ppc64", "dragonflybsd-ppc64el", "dragonflybsd-riscv64", "dragonflybsd-s390", "dragonflybsd-s390x", "dragonflybsd-sh3", "dragonflybsd-sh3eb", "dragonflybsd-sh4", "dragonflybsd-sh4eb", "dragonflybsd-sparc", "dragonflybsd-sparc64", "dragonflybsd-tilegx", "freebsd-i386", "freebsd-ia64", "freebsd-alpha", "freebsd-amd64", "freebsd-arc", "freebsd-armeb", "freebsd-arm", "freebsd-arm64", "freebsd-avr32", "freebsd-hppa", "freebsd-m32r", "freebsd-m68k", "freebsd-mips", "freebsd-mipsel", "freebsd-mipsr6", "freebsd-mipsr6el", "freebsd-mips64", "freebsd-mips64el", "freebsd-mips64r6", "freebsd-mips64r6el", "freebsd-nios2", "freebsd-or1k", "freebsd-powerpc", "freebsd-powerpcel", "freebsd-ppc64", "freebsd-ppc64el", "freebsd-riscv64", "freebsd-s390", "freebsd-s390x", "freebsd-sh3", "freebsd-sh3eb", "freebsd-sh4", "freebsd-sh4eb", "freebsd-sparc", "freebsd-sparc64", "freebsd-tilegx", "netbsd-i386", "netbsd-ia64", "netbsd-alpha", "netbsd-amd64", "netbsd-arc", "netbsd-armeb", "netbsd-arm", "netbsd-arm64", "netbsd-avr32", "netbsd-hppa", "netbsd-m32r", "netbsd-m68k", "netbsd-mips", "netbsd-mipsel", "netbsd-mipsr6", "netbsd-mipsr6el", "netbsd-mips64", "netbsd-mips64el", "netbsd-mips64r6", "netbsd-mips64r6el", "netbsd-nios2", "netbsd-or1k", "netbsd-powerpc", "netbsd-powerpcel", "netbsd-ppc64", "netbsd-ppc64el", "netbsd-riscv64", "netbsd-s390", "netbsd-s390x", "netbsd-sh3", "netbsd-sh3eb", "netbsd-sh4", "netbsd-sh4eb", "netbsd-sparc", "netbsd-sparc64", "netbsd-tilegx", "openbsd-i386", "openbsd-ia64", "openbsd-alpha", "openbsd-amd64", "openbsd-arc", "openbsd-armeb", "openbsd-arm", "openbsd-arm64", "openbsd-avr32", "openbsd-hppa", "openbsd-m32r", "openbsd-m68k", "openbsd-mips", "openbsd-mipsel", "openbsd-mipsr6", "openbsd-mipsr6el", "openbsd-mips64", "openbsd-mips64el", "openbsd-mips64r6", "openbsd-mips64r6el", "openbsd-nios2", "openbsd-or1k", "openbsd-powerpc", "openbsd-powerpcel", "openbsd-ppc64", "openbsd-ppc64el", "openbsd-riscv64", "openbsd-s390", "openbsd-s390x", "openbsd-sh3", "openbsd-sh3eb", "openbsd-sh4", "openbsd-sh4eb", "openbsd-sparc", "openbsd-sparc64", "openbsd-tilegx", "aix-i386", "aix-ia64", "aix-alpha", "aix-amd64", "aix-arc", "aix-armeb", "aix-arm", "aix-arm64", "aix-avr32", "aix-hppa", "aix-m32r", "aix-m68k", "aix-mips", "aix-mipsel", "aix-mipsr6", "aix-mipsr6el", "aix-mips64", "aix-mips64el", "aix-mips64r6", "aix-mips64r6el", "aix-nios2", "aix-or1k", "aix-powerpc", "aix-powerpcel", "aix-ppc64", "aix-ppc64el", "aix-riscv64", "aix-s390", "aix-s390x", "aix-sh3", "aix-sh3eb", "aix-sh4", "aix-sh4eb", "aix-sparc", "aix-sparc64", "aix-tilegx", "solaris-i386", "solaris-ia64", "solaris-alpha", "solaris-amd64", "solaris-arc", "solaris-armeb", "solaris-arm", "solaris-arm64", "solaris-avr32", "solaris-hppa", "solaris-m32r", "solaris-m68k", "solaris-mips", "solaris-mipsel", "solaris-mipsr6", "solaris-mipsr6el", "solaris-mips64", "solaris-mips64el", "solaris-mips64r6", "solaris-mips64r6el", "solaris-nios2", "solaris-or1k", "solaris-powerpc", "solaris-powerpcel", "solaris-ppc64", "solaris-ppc64el", "solaris-riscv64", "solaris-s390", "solaris-s390x", "solaris-sh3", "solaris-sh3eb", "solaris-sh4", "solaris-sh4eb", "solaris-sparc", "solaris-sparc64", "solaris-tilegx", "uclinux-armel", "uclinux-i386", "uclinux-ia64", "uclinux-alpha", "uclinux-amd64", "uclinux-arc", "uclinux-armeb", "uclinux-arm", "uclinux-arm64", "uclinux-avr32", "uclinux-hppa", "uclinux-m32r", "uclinux-m68k", "uclinux-mips", "uclinux-mipsel", "uclinux-mipsr6", "uclinux-mipsr6el", "uclinux-mips64", "uclinux-mips64el", "uclinux-mips64r6", "uclinux-mips64r6el", "uclinux-nios2", "uclinux-or1k", "uclinux-powerpc", "uclinux-powerpcel", "uclinux-ppc64", "uclinux-ppc64el", "uclinux-riscv64", "uclinux-s390", "uclinux-s390x", "uclinux-sh3", "uclinux-sh3eb", "uclinux-sh4", "uclinux-sh4eb", "uclinux-sparc", "uclinux-sparc64", "uclinux-tilegx", "mint-m68k"};
	/**
	 * The list of cpu architectures, extracted from {@link #architectures}.
	 */
	public static final String[] cpus = Arrays.stream(architectures).map(a -> a.contains("-") ? Arrays.asList(a.split("-")).getLast() : null).filter(Objects::nonNull).toArray(String[]::new);
	/**
	 * The list of operating systems, extracted from {@link #architectures}.
	 */
	public static final String[] systems = Arrays.stream(architectures).map(a -> a.indexOf('-') == a.lastIndexOf('-') ? a.split("-")[0] : a.split("-")[1]).toArray(String[]::new);
	protected static final BiConsumer<String, Configuration> ARCHITECTURE_LINTER = (s, config) -> {
		boolean inverted = s.contains("!");
		String[] declared = s.split(" ");
		for(String arch : declared) {
			if(inverted && !arch.startsWith("!")) {
				Main.error("Architecture names must all be prepended with exclamation marks, or not at all: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#architecture");
			}
		}
		if(inverted) {
			declared = Arrays.stream(declared).map(a -> a.replace("!", "")).toArray(String[]::new);
		}
		Set<String> supArch = new HashSet<>(Arrays.asList(architectures));
		Set<String> supOs = new HashSet<>(Arrays.asList(systems));
		Set<String> supCpu = new HashSet<>(Arrays.asList(cpus));
		for(String arch : declared) {
			if(arch.equals("any")) {
				continue;
			}
			if(config.strictArch) {
				if(arch.endsWith("-any")) {
					String os = arch.substring(0, arch.length() - "-any".length());
					if(!supOs.contains(os)) {
						Main.error("Wildcard does not match any architecture: " + arch, "strictArch", "https://www.debian.org/doc/debian-policy/ch-customized-programs.html#s-arch-wildcard-spec");
					}
				} else if(arch.startsWith("any-")) {
					String cpu = arch.substring("any-".length());
					if(!supCpu.contains(cpu)) {
						Main.error("Wildcard does not match any architecture: " + arch, "strictArch", "https://www.debian.org/doc/debian-policy/ch-customized-programs.html#s-arch-wildcard-spec");
					}
				} else {
					if(!supArch.contains(arch)) {
						Main.error("Unknown architecture: " + arch, "strictArch");
					}
				}
			} else {
				if(!Pattern.matches("^[a-zA-Z0-9\\-]+$", arch)) {
					Main.error("Invalid architecture: " + arch);
				}
			}
		}
	};
	protected static final BiConsumer<String, Configuration> SHA1_LINTER = (s, config) -> {
		String[] lines = s.split("\\n");
		if(!lines[0].isBlank()) {
			Main.error("The first line of checksums should be empty");
		}
		Arrays.stream(lines).filter(s1 -> !s1.isBlank()).map(String::strip).forEachOrdered(l -> {
			String[] parts = l.split(" ", 3);
			if(parts.length < 3) {
				Main.error("Missing parameter; 3 values required: " + l);
				return;
			}
			if(!Pattern.matches("^[a-fA-F0-9]{40}$", parts[0])) {
				Main.error("Invalid SHA hash: " + parts[0]);
			}
			SIZE_LINTER.accept(parts[1], config);
		});
	};
	protected static final BiConsumer<String, Configuration> BINARY_LIST_LINTER = (s, config) -> {
		HashSet<String> files = new HashSet<>();
		if(config.checkedType == ControlType.SOURCE_CONTROL) {
			for(String string : s.split(",")) {
				string = string.strip();
				if(string.isEmpty()) {
					Main.error("Empty file name: " + s);
				} else if(config.duplicateFiles && files.contains(string)) {
					Main.error("Duplicated file in list: " + s, "duplicateFiles");
				} else {
					files.add(string);
				}
			}
		} else if(config.checkedType == ControlType.CHANGES) {
			for(String string : s.split(" ")) {
				string = string.strip();
				if(string.isEmpty()) {
					Main.error("Empty file name: " + s);
				} else if(config.duplicateFiles && files.contains(string)) {
					Main.error("Duplicated file in list: " + s, "duplicateFiles");
				} else {
					files.add(string);
				}
			}
		}
	};
	protected static final BiConsumer<String, Configuration> BOOLEAN_LINTER = (s, config) -> {
		if(!s.equals("yes") && !s.equals("no")) {
			Main.error("Invalid boolean value; should be 'yes' or 'no': " + s, null, "https://www.debian.org/doc/debian-policy/ch-customized-programs.html#s-arch-wildcard-spec");
		}
	};
	protected static final BiConsumer<String, Configuration> CHANGE_LIST_LINTER = (s, config) -> {
		String[] lines = s.split("\\n", -1);
		if(!lines[0].isBlank()) {
			Main.error("The first line of changes should be empty", null, "https://www.debian.org/doc/debian-policy/ch-controlfields#changes");
		}
		//todo: check all title requirements from https://www.debian.org/doc/debian-policy/ch-controlfields#changes
	};
	protected static final BiConsumer<String, Configuration> COPYRIGHT_FILE_LIST_LINTER = (s, config) -> {
		String[] patterns = s.split("\\n", -1);
		for(String pattern : patterns) {
			pattern = pattern.strip();
			if(Pattern.matches(".*\\\\[^\\\\*?].*", pattern)) {
				Main.error("Illegal escape sequence: " + pattern, null, "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#files-field");
			}
			if(Pattern.matches(".*\\s.*", pattern)) {
				Main.error("Illegal whitespace in pattern: " + pattern, null, "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#files-field");
			}
		}
	};
	protected static final BiConsumer<String, Configuration> DATE_LINTER = (s, config) -> {
		// day-of-week, dd month yyyy hh:mm:ss +zzzz
		if(!Pattern.matches("^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \\d\\d? (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{4} \\d{2}:\\d{2}:\\d{2} [+-]\\d{4}$", s)) {
			Main.error("Invalid date: " + s);
		} else {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy HH:mm:ss Z");
				ZonedDateTime parsed = ZonedDateTime.parse(s, formatter);
				if(ZonedDateTime.now().isBefore(parsed)) {
					Main.error("Future date specified: " + s);
				}
			} catch(DateTimeException e) {
				Main.error("Invalid date: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#date");
			}
		}
	};
	/**
	 * A linter that accepts any input.
	 */
	protected static final BiConsumer<String, Configuration> DEFAULT_LINTER = (s, config) -> {
	};
	protected static final BiConsumer<String, Configuration> DESCRIPTION_LINTER = (s, config) -> {
		String[] lines = s.split("\n");
		if(lines[0].isBlank()) {
			Main.error("Missing synopsys: ", null, "https://www.debian.org/doc/debian-policy/ch-controlfields#description");
		}
		for(int i = 1; i < lines.length; i++) {
			if(lines[i].startsWith(" .") && !" .".equals(lines[i])) {
				Main.error("Use of reserved syntax: " + lines[i], null, "https://www.debian.org/doc/debian-policy/ch-controlfields#description");
			}
		}
	};
	protected static final BiConsumer<String, Configuration> DGIT_LINTER = (s, config) -> {
		String[] parts = s.split(" ");
		if(config.dgitExtraData && parts.length > 1) {
			Main.error("Extra data after the commit hash is reserved for future expansion; do not use: " + s, "dgitExtraData", "https://www.debian.org/doc/debian-policy/ch-controlfields#dgit");
		}
		if(!Pattern.matches("^[a-f0-9]{40}$", parts[0])) {
			Main.error("Invalid git hash: " + parts[0]);
		}
	};
	protected static final BiConsumer<String, Configuration> DISTRIBUTION_LINTER = (s, config) -> {
		if(config.multipleDistributions && s.contains(" ")) {
			Main.error("Please only use a single distribution: " + s, "multipleDistributions", "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-distribution");
		}
	};
	protected static final BiConsumer<String, Configuration> LICENSE_LINTER = (s, config) -> {
		String[] parts = s.split("\\n");
		if(s.split("\\n")[0].isBlank()) {
			Main.error("License must have a short name in the first line: ", null, "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#license-field");
		} else {
			if(config.licenseName) {
				String shortNames = parts[0].strip().replace(",", "");
				for(String name : shortNames.split(" (or|and) ")) {
					if(name.contains(" ")) {
						String[] nameParts = name.split(" ");
						if(nameParts.length != 4) {
							Main.error("Invalid license exception: " + name, "licenseName", "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#license-short-name");
						} else {
							if(!nameParts[1].equals("with") || !nameParts[3].equals("exception")) {
								Main.error("Invalid license exception: " + name, "licenseName", "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#license-short-name");
							}
							if(config.customLicenseException) {
								String[] exceptions = {"Font", "OpenSSL"};
								if(Arrays.stream(exceptions).noneMatch(e -> e.equals(nameParts[2]))) {
									Main.error("Unknown license exception: " + parts[2], "customLicenseException");
								}
							}
						}
					} else if(name.equals("public-domain") && parts.length == 1) {
						Main.error("Licensing to public domain must be followed by an explanation", "licenseName", "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#license-short-name");
					}
				}
			}
		}
	};
	protected static final BiConsumer<String, Configuration> MD5_LINTER = (s, config) -> {
		if(!Pattern.matches("^[a-fA-F0-9]{32}$", s)) {
			Main.error("Invalid MD5 hash: " + s);
		}
	};
	protected static final BiConsumer<String, Configuration> NUMBER_LIST_LINTER = (s, config) -> {
		String[] parts = s.split(" ");
		HashSet<Integer> numbers = new HashSet<>();
		for(String part : parts) {
			try {
				int i = Integer.parseInt(part);
				if(part.startsWith("+")) {
					Main.error("Numbers should be unsigned: " + part);
				} else if(i <= 0) {
					Main.error("Numbers should not be negative: " + part);
				} else {
					if(config.duplicateIssueNumbers && numbers.contains(i)) {
						Main.error("Duplicate number: " + part, "duplicateIssueNumbers");
					} else {
						numbers.add(i);
					}
				}
			} catch(NumberFormatException e) {
				Main.error("Invalid number: " + part);
			}
		}
	};
	protected static final BiConsumer<String, Configuration> PACKAGE_NAME_LINTER = (s, config) -> {
		if(!Pattern.matches("^[a-z0-9][a-z0-9+.\\-]+$", s)) {
			Main.error("Invalid package name: " + s);
		}
	};
	protected static final BiConsumer<String, Configuration> PACKAGE_TYPE_LINTER = (s, config) -> {
		if(config.unknownPackageType && !s.equals("deb") && !s.equals("udeb")) {
			Main.error("Unknown package type: " + s, "unknownPackageType");
		}
		if(config.redundantPackageType && s.equals("deb") && config.checkedType == ControlType.SOURCE_PACKAGE_CONTROL) {
			Main.error("Package-Type should be omitted when using the default value: " + s, "redundantPackageType", "https://www.debian.org/doc/debian-policy/ch-controlfields#package-type");
		}
	};
	protected static final BiConsumer<String, Configuration> PRIORITY_LINTER = (s, config) -> {
		if(config.unknownPriority) {
			String[] priorities = {"required", "important", "standard", "optional", "extra"};
			if(Arrays.stream(priorities).noneMatch(p -> p.equals(s))) {
				Main.error("Unknown priority: " + s, "unknownPriority", "https://www.debian.org/doc/debian-policy/ch-archive.html#s-priorities");
			}
		}
		if(config.extraPriority && s.equals("extra")) {
			Main.error("The 'extra' priority is deprecated, use 'optional' instead", "extraPriority", "https://www.debian.org/doc/debian-policy/ch-archive.html#s-priorities");
		}
	};
	protected static final BiConsumer<String, Configuration> REQUIRES_ROOT_LINTER = (s, config) -> {
		if(s.equals("no") || s.equals("binary-targets")) {
			return;
		}
		String[] keywords = s.split(" ");
		String ascii_printable = "!-.0-~";
		Pattern pattern = Pattern.compile("^[" + ascii_printable + "]{2,}/[" + ascii_printable + "/]{2,}$");
		for(String keyword : keywords) {
			if(!pattern.matcher(keyword).matches()) {
				Main.error("Invalid keyword for Rules-Requires-Root: " + keyword, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-rules-requires-root");
			}
		}
	};
	protected static final BiConsumer<String, Configuration> RFC_822_LINTER = (s, config) -> {
		try {
			InternetAddress emailAddr = new InternetAddress(s);
			emailAddr.validate();
		} catch(AddressException e) {
			Main.error("Invalid email address: " + s, null, "https://www.w3.org/Protocols/rfc822/");
		}
	};
	protected static final BiConsumer<String, Configuration> ADDRESS_LINTER = (s, config) -> {
		int begin = s.indexOf('<');
		int end = s.lastIndexOf('>');
		if(begin == -1 || end == -1 || end < begin) {
			Main.error("Missing email address: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#maintainer");
		} else {
			RFC_822_LINTER.accept(s.substring(begin + 1, end), config);
		}
		if(end != s.length() - 1) {
			Main.error("Extra content after email address: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#maintainer");
		}
		if(config.maintainerNameFullStop && s.substring(0, begin).contains(".")) {
			Main.error("Name contains full stop: " + s.substring(0, begin), "maintainerNameFullStop", "https://www.debian.org/doc/debian-policy/ch-controlfields#maintainer");
		}
		if(begin == 0) {
			Main.error("Missing name: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#maintainer");
		}
	};
	protected static final BiConsumer<String, Configuration> MULTI_ADDRESS_LINTER = (s, config) -> {
		for(String address : s.split(",", -1)) {
			ADDRESS_LINTER.accept(address, config);
		}
	};
	protected static final BiConsumer<String, Configuration> UPSTREAM_CONTACT_LINTER = (s, config) -> {
		if(config.upstreamContactStyle) {
			try {
				URL u = new URI(s).toURL();
				checkUrl(u, config);
			} catch(URISyntaxException | MalformedURLException | IllegalArgumentException e) {
				ADDRESS_LINTER.accept(s, config);
			}
		}
	};
	protected static final BiConsumer<String, Configuration> SECTION_LINTER = (s, config) -> {
		String[] areas = {"contrib", "non-free"};
		String[] sections = {"admin", "cli-mono", "comm", "database", "debian-installer", "debug", "devel", "doc", "editors", "education", "electronics", "embedded", "fonts", "games", "gnome", "gnu-r", "gnustep", "graphics", "hamradio", "haskell", "httpd", "interpreters", "introspection", "java", "javascript", "kde", "kernel", "libdevel", "libs", "lisp", "localization", "mail", "math", "metapackages", "misc", "net", "news", "ocaml", "oldlibs", "otherosfs", "perl", "php", "python", "ruby", "rust", "science", "shells", "sound", "tasks", "tex", "text", "utils", "vcs", "video", "web", "x11", "xfce", "zope"};
		String section;
		if(s.contains("/")) {
			String[] parts = s.split("/", 2);
			if(Arrays.stream(areas).noneMatch(a -> a.equals(parts[0]))) {
				Main.error("Unknown area: " + s, null, "https://www.debian.org/doc/debian-policy/ch-archive.html#s-subsections");
			}
			section = parts[1];
		} else {
			section = s;
		}
		if(Arrays.stream(sections).noneMatch(a -> a.equals(section))) {
			Main.error("Unknown section: " + s, null, "https://www.debian.org/doc/debian-policy/ch-archive.html#s-subsections");
		}
		if(config.debianInstallerSection && section.equals("debian-installer")) {
			Main.error("debian-installer section should not be used here: " + s, "debianInstallerSection", "https://www.debian.org/doc/debian-policy/ch-archive.html#s-subsections");
		}
	};
	protected static final BiConsumer<String, Configuration> PACKAGE_LIST_LINTER = (s, config) -> {
		String[] lines = s.split("\\n", -1);
		if(!lines[0].isBlank()) {
			Main.error("Package-List must begin with an empty line: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-package-list");
		}
		HashSet<String> binaries = new HashSet<>();
		for(int i = 1; i < lines.length; i++) {
			String[] parts = lines[i].strip().split(" ");
			// name, type, section, priority
			if(parts.length < 4) {
				Main.error("Missing values from package; 4 values are required: " + lines[i].strip(), null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-package-list");
			} else {
				PACKAGE_NAME_LINTER.accept(parts[0], config);
				PACKAGE_TYPE_LINTER.accept(parts[1], config);
				SECTION_LINTER.accept(parts[2], config);
				PRIORITY_LINTER.accept(parts[3], config);
				if(config.duplicatePackages && binaries.contains(parts[0])) {
					Main.error("Duplicate package in list: " + parts[0], "duplicatePackages");
				} else {
					binaries.add(parts[0]);
				}
			}
		}
	};
	protected static final BiConsumer<String, Configuration> SIZE_LINTER = (s, config) -> {
		try {
			Long l = Long.parseLong(s);
			if(l < 0) {
				Main.error("Size cannot be negative: " + s);
			} else if(s.startsWith("+")) {
				Main.error("Size must be unsigned: " + s);
			}
		} catch(NumberFormatException e) {
			Main.error("Invalid size: " + s);
		}
	};
	protected static final BiConsumer<String, Configuration> SHA256_LINTER = (s, config) -> {
		String[] lines = s.split("\n");
		if(!lines[0].isBlank()) {
			Main.error("The first line of checksums should be empty");
		}
		Arrays.stream(lines).filter(s1 -> !s1.isBlank()).map(String::strip).forEachOrdered(l -> {
			String[] parts = l.split(" ", 3);
			if(parts.length < 3) {
				Main.error("Missing parameter; 3 values required: " + l);
				return;
			}
			if(!Pattern.matches("^[a-fA-F0-9]{64}$", parts[0])) {
				Main.error("Invalid SHA hash: " + parts[0]);
			}
			SIZE_LINTER.accept(parts[1], config);
		});
	};
	protected static final BiConsumer<String, Configuration> SINGLE_ARCHITECTURE_LINTER = (s, config) -> {
		ArrayList<String> arches = new ArrayList<>(List.of(s.split(" ")));
		if(config.duplicateArchitecture && arches.size() > new HashSet<>(arches).size()) {
			Main.error("Duplicated architecture: " + s, "duplicateArchitecture");
		}
		if(config.checkedType == ControlType.SOURCE_PACKAGE_CONTROL) {
			if(!s.equals("all") && !s.equals("any")) {
				if(arches.stream().anyMatch(a -> a.equals("all") || a.equals("any"))) {
					Main.error("'all' or 'any' must be the only entries, if present: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#architecture");
				} else {
					ARCHITECTURE_LINTER.accept(String.join(" ", arches.stream().filter(a -> !a.equals("all")).toList()), config);
				}
			}
		} else if(config.checkedType == ControlType.SOURCE_CONTROL) {
			if(arches.contains("any")) {
				if(!arches.stream().allMatch(a -> a.equals("any") || a.equals("all"))) {
					Main.error("When 'any' is present in a list, the only other value allowed is 'all': " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#architecture");
				}
			} else {
				ARCHITECTURE_LINTER.accept(String.join(" ", arches.stream().filter(a -> !a.equals("all")).toList()), config);
			}
		} else if(config.checkedType == ControlType.CHANGES) {
			HashSet<String> archSet = new HashSet<>(arches);
			archSet.remove("source");
			if(archSet.contains("any") || archSet.stream().anyMatch(a -> a.startsWith("any-") || a.endsWith("-any"))) {
				Main.error("Architecture wildcards are not allowed in .changes files: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#architecture");
			} else {
				if(!archSet.isEmpty()) {
					ARCHITECTURE_LINTER.accept(String.join(" ", archSet), config);
				}
			}
		} else {
			ARCHITECTURE_LINTER.accept(s, config);
		}
	};
	protected static final BiConsumer<String, Configuration> FILE_LIST_LINTER = (s, config) -> {
		String[] lines = s.split("\\n");
		if(!lines[0].isBlank()) {
			Main.error("The first line of 'Files' should be empty: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-files");
		}
		Pattern indent = Pattern.compile("^ \\w.*");
		for(int i = 1; i < lines.length; i++) {
			if(!indent.matcher(lines[i]).matches()) {
				Main.error("Lines should be indented with only one space: " + lines[i], null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-files");
			}
		}
		String[][] words = Arrays.stream(lines).map(a -> a.strip().split(" ", config.checkedType == ControlType.SOURCE_CONTROL ? 3 : 5)).toArray(String[][]::new);
		HashSet<String> filenames = new HashSet<>();
		for(int i = 1; i < words.length; i++) {
			String[] stuffs = words[i];
			if(config.checkedType == ControlType.SOURCE_CONTROL) {
				if(stuffs.length < 3) {
					Main.error("Missing parameter: 3 values required: " + lines[i], null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-files");
				} else {
					MD5_LINTER.accept(stuffs[0], config);
					SIZE_LINTER.accept(stuffs[1], config);
					if(config.duplicateFiles && filenames.contains(stuffs[2])) {
						Main.error("Duplicated file in list: " + stuffs[2]);
					} else {
						filenames.add(stuffs[2]);
					}
				}
			} else if(config.checkedType == ControlType.CHANGES) {
				if(stuffs.length < 3) {
					Main.error("Missing parameter: 5 values required: " + lines[i], null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-files");
				} else {
					MD5_LINTER.accept(stuffs[0], config);
					SIZE_LINTER.accept(stuffs[1], config);
					if(stuffs[2].equals("-")) {
						if(config.missingSectionOrPriority) {
							Main.error("Section must be defined: " + stuffs[2], "missingSectionOrPriority", "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-files");
						}
					} else if(stuffs[2].equals("byhand")) {
						if(!stuffs[3].equals("-")) {
							Main.error("Priority must be '-' if section is 'byhand': " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-files");
						}
					} else {
						SECTION_LINTER.accept(stuffs[2], config);
						PRIORITY_LINTER.accept(stuffs[3], config);
						if(config.missingSectionOrPriority && stuffs[3].equals("-")) {
							Main.error("Priority must be defined: " + stuffs[2], "missingPriority", "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-files");
						}
					}
					if(config.duplicateFiles && filenames.contains(stuffs[4])) {
						Main.error("Duplicated file in list: " + stuffs[4], "duplicateFiles");
					} else {
						filenames.add(stuffs[4]);
					}
				}
			}
		}
	};
	protected static final BiConsumer<String, Configuration> STANDARDS_VERSION_LINTER = (s, config) -> {
		String[] parts = s.split("\\.");
		int[] latest = {4, 6, 2, 1};
		if(parts.length < 3 || parts.length > 4) {
			Main.error("Invalid standards version: " + s);
		} else {
			try {
				int[] values = Arrays.stream(parts).mapToInt(Integer::parseInt).toArray();
				if(config.strictStandardsVersion) {
					for(int i = 0; i < values.length; i++) {
						if(values[i] > latest[i]) {
							Main.error("Invalid standards version: " + s, "strictStandardsVersion");
						} else if(values[i] < latest[i]) {
							break;
						}
					}
				}
			} catch(NumberFormatException e) {
				Main.error("Invalid standards version: " + s);
			}
		}
	};
	protected static final BiConsumer<Stanza, Configuration> STANZA_CHECKSUM_LINTER = (s, config) -> {
		String[] hashes = {"Checksums-Sha1", "Checksums-Sha256"};
		HashSet<String> files = new HashSet<>();
		DataField fileField = s.getField("Files");
		if(fileField == null) {
			return;
		}
		Arrays.stream(fileField.data().split("\\n")).map(String::strip).filter(f -> !f.isEmpty()).map(f -> List.of(f.split(" ")).getLast()).forEach(files::add);
		//
		for(String hashType : hashes) {
			DataField field = s.getField(hashType);
			if(field != null) {
				HashSet<String> localHashes = new HashSet<>(files);
				Arrays.stream(field.data().split("\\n")).map(String::strip).filter(f -> !f.isEmpty()).map(f -> List.of(f.split(" ")).getLast()).forEachOrdered(f -> {
					if(!localHashes.contains(f)) {
						Main.error("Checksummed file is not in file list, or is already checksummed: " + f, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#checksums-sha1-and-checksums-sha256");
					}
					localHashes.remove(f);
				});
				if(!localHashes.isEmpty()) {
					Main.error("File is not in checksum list: " + String.join(", ", localHashes));
				}
			}
		}
	};
	protected static final BiConsumer<Stanza, Configuration> STANZA_DEFAULT_LINTER = (s, config) -> {
	};
	protected static final BiConsumer<Stanza, Configuration> STANZA_SOURCE_LINTER = (s, config) -> {
		if(config.sourceRedundantVersion) {
			DataField source = s.getField("source");
			DataField version = s.getField("Version");
			if(source != null && version != null) {
				String[] parts = source.data().split("\\(");
				if(parts.length == 2) {
					if(parts[1].contains(")")) {
						String sourceVersion = parts[1].split("\\)", 2)[0];
						if(sourceVersion.equals(version.data())) {
							Main.error("Please omit the source version when the Version field is used with the same value: " + source.data(), "sourceRedundantVersion", "https://www.debian.org/doc/debian-policy/ch-controlfields#source");
						}
					}
				}
			}
		}
	};
	protected static final BiConsumer<Stanza, Configuration> STANZA_SOURCE_AND_CHECKSUM_LINTER = (s, config) -> {
		STANZA_SOURCE_LINTER.accept(s, config);
		STANZA_CHECKSUM_LINTER.accept(s, config);
	};
	protected static final BiConsumer<Stanza, Configuration> STANZA_VCS_LINTER = (s, config) -> {
		String[] vcsFields = {"Vcs-Arch", "Vcs-Bzr", "Vcs-Cvs", "Vcs-Darcs", "Vcs-Git", "Vcs-Hg", "Vcs-Mtn", "Vcs-Svn"};
		boolean found = false;
		for(String vcsField : vcsFields) {
			if(s.getField(vcsField) != null) {
				if(found) {
					Main.error("Multiple VCS fields are declared: " + vcsField, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
				} else {
					found = true;
				}
			}
		}
	};
	protected static final BiConsumer<Stanza, Configuration> STANZA_SOURCE_AND_VCS_LINTER = (s, config) -> {
		STANZA_VCS_LINTER.accept(s, config);
		STANZA_SOURCE_LINTER.accept(s, config);
	};
	protected static final BiConsumer<Stanza, Configuration> STANZA_SOURCE_CONTROL_LINTER = (s, config) -> {
		STANZA_VCS_LINTER.accept(s, config);
		STANZA_SOURCE_LINTER.accept(s, config);
		STANZA_CHECKSUM_LINTER.accept(s, config);
	};
	protected static final BiConsumer<ControlFile, Configuration> TYPE_COPYRIGHT_LINTER = new TypeCopyrightLinter();
	protected static final BiConsumer<String, Configuration> UPSTREAM_VERSION_LINTER = (s, config) -> {
		Pattern upstream = Pattern.compile("^[0-9][A-Za-z0-9.+~\\-]*$");
		if(!upstream.matcher(s).matches()) {
			Main.error("Upstream version uses an invalid format: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#version");
		}
	};
	protected static final BiConsumer<String, Configuration> FORMAT_VERSION_LINTER = (s, config) -> {
		if(config.checkedType == ControlType.CHANGES) {
			UPSTREAM_VERSION_LINTER.accept(s, config);
			if(config.exactFormatVersion && !s.equals("1.8")) {
				Main.error("Please use format version 1.8: " + s, "exactFormatVersion", "https://www.debian.org/doc/debian-policy/ch-controlfields#format");
			}
		} else if(config.checkedType == ControlType.SOURCE_CONTROL) {
			String[] supported = {"1.0", "3.0 (native)", "3.0 (quilt)"};
			if(!Pattern.matches("^\\d+\\.\\d+( \\([a-zA-Z0-9]+\\))?$", s)) {
				Main.error("Invalid format version: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#format");
			}
			if(config.exactFormatVersion) {
				if(Arrays.stream(supported).noneMatch(a -> a.equals(s))) {
					Main.error("Unsupported format version: " + s, "exactFormatVersion", "https://www.debian.org/doc/debian-policy/ch-controlfields#format");
				}
			}
		}
	};
	protected static final BiConsumer<String, Configuration> URGENCY_LINTER = (s, config) -> {
		String[] urgencies = {"low", "medium", "high", "emergency", "critical"};
		String[] parts = s.split(" ", 2);
		if(config.customUrgencies && Arrays.stream(urgencies).noneMatch(a -> a.equalsIgnoreCase(parts[0]))) {
			Main.error("Unknown urgency level: " + parts[0], "customUrgencies", "https://www.debian.org/doc/debian-policy/ch-controlfields#urgency");
		}
		if(parts.length > 1) {
			if(config.urgencyDescriptionParentheses && (!parts[1].startsWith("(") || !parts[1].endsWith(")"))) {
				Main.error("Urgency commentary should be wrapped in parentheses: " + s, "urgencyDescriptionParentheses", "https://www.debian.org/doc/debian-policy/ch-controlfields#urgency");
			}
		}
	};
	protected static final BiConsumer<String, Configuration> URL_LINTER = (s, config) -> {
		try {
			URL u = new URI(s).toURL();
			checkUrl(u, config);
		} catch(URISyntaxException | MalformedURLException | IllegalArgumentException e) {
			Main.error("Invalid URL: " + s);
		}
	};
	protected static final BiConsumer<String, Configuration> GIT_VCS_LINTER = (s, config) -> {
		String[] parts = s.split(" ", 2);
		String url = parts[0];
		URL_LINTER.accept(url, config);
		if(parts.length == 2) {
			// there is a branch or path defined
			if(parts[1].startsWith("-b")) {
				String[] params = parts[1].split(" ", 3);
				if(params.length < 2) {
					Main.error("Incomplete branch definition for Git: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
				} else {
					// params[0] == -b
					// params[1] == <branch-name>
					// params[2] == [path] | missing
					if(params[1].isEmpty()) {
						Main.error("Empty branch name: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
					}
					if(params.length == 3) {
						String path = params[2].strip();
						if(!path.startsWith("[") || !path.endsWith("]")) {
							Main.error("Invalid path definition for Git: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
						}
					}
				}
			} else if(parts[1].startsWith("[")) {
				// otherwise, check for path
				if(!parts[1].endsWith("]")) {
					Main.error("Invalid path definition for Git: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
				}
			} else {
				Main.error("Invalid git data: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
			}
		} else if(config.vcsBranch) {
			Main.error("Missing branch definition for Git: " + s, "vcsBranch", "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
		}
	};
	protected static final BiConsumer<String, Configuration> MERCURIAL_VCS_LINTER = (s, config) -> {
		String[] parts = s.split(" ", 2);
		String url = parts[0];
		URL_LINTER.accept(url, config);
		if(parts.length == 2) {
			// there is a branch defined
			if(parts[1].startsWith("-b")) {
				String[] params = parts[1].split(" ");
				if(params.length != 2) {
					Main.error("Incomplete branch definition for Mercurial: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
				}
			}
		} else if(config.vcsBranch) {
			Main.error("Missing branch definition for Mercurial: " + s, "vcsBranch", "https://www.debian.org/doc/debian-policy/ch-controlfields#s-f-vcs-fields");
		}
	};
	protected static final BiConsumer<String, Configuration> COPYRIGHT_FORMAT_LINTER = (s, config) -> {
		URL_LINTER.accept(s, config);
		if(config.strictCopyrightFormatVersion) {
			if(!s.equals("https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/")) {
				Main.error("Unknown copyright format: " + s, "strictCopyrightFormatVersion", "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#format-field");
			}
		}
	};
	protected static final BiConsumer<String, Configuration> COPYRIGHT_SOURCE_LINTER = (s, config) -> {
		if(config.copyrightSourceStyle) {
			try {
				URL_LINTER.accept(s, config);
			} catch(IllegalArgumentException e) {
				Main.error("Invalid copyright source URL: " + s, "copyrightSourceStyle", "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#source-field");
			}
		}
	};
	protected static final BiConsumer<String, Configuration> VERSION_LINTER = (s, config) -> {
		String[] epochSplit = s.split(":", 2);
		String epoch = epochSplit.length == 2 ? epochSplit[0] : "0";
		String remaining = epochSplit.length == 2 ? epochSplit[1] : epochSplit[0];
		String[] revisionSplit = remaining.split("-(?=[^-]+$)", 2);
		String upstreamVersion = revisionSplit[0];
		String debianRevision = revisionSplit.length == 2 ? revisionSplit[1] : "0";
		if(epoch != null) {
			if(epoch.startsWith("+")) {
				Main.error("Epoch must not have a sign: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#version");
			} else {
				try {
					Integer.parseUnsignedInt(epoch);
				} catch(NumberFormatException e) {
					Main.error("Epoch must be an unsigned integer: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#version");
				}
			}
		}
		UPSTREAM_VERSION_LINTER.accept(upstreamVersion, config);
		Pattern debian = Pattern.compile("^[A-Za-z0-9.+~]+$");
		if(!debian.matcher(debianRevision).matches()) {
			Main.error("Debian version uses an invalid format: " + debianRevision, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#version");
		}
	};
	protected static final BiConsumer<String, Configuration> DEPENDENCY_LINTER = (s, config) -> {
		String[] packages = s.split("[|,]");
		String[] operators = {"<<", "<=", "=", ">=", ">>"};
		for(String aPackage : packages) {
			aPackage = aPackage.strip();
			// version config
			int begin = aPackage.indexOf('(');
			int end = aPackage.lastIndexOf(')');
			if(begin != -1 && end != -1 && begin + 1 < end) {
				String version = aPackage.substring(begin + 1, end).strip();
				if(version.isEmpty()) {
					Main.error("Empty package version string: " + s, null, "https://www.debian.org/doc/debian-policy/ch-relationships.html");
				} else {
					boolean found = false;
					for(String operator : operators) {
						if(version.startsWith(operator)) {
							VERSION_LINTER.accept(version.substring(operator.length()).strip(), config);
							found = true;
							break;
						}
					}
					if(!found) {
						Main.error("Invalid relation for package version: " + version, null, "https://www.debian.org/doc/debian-policy/ch-relationships.html");
					}
				}
			} else if(begin != end) {
				Main.error("Incomplete package version string: " + s, null, "https://www.debian.org/doc/debian-policy/ch-relationships.html");
			} else {
				// arch config
				begin = aPackage.indexOf('[');
				end = aPackage.lastIndexOf(']');
				if(begin != -1 && end != -1 && begin + 1 < end) {
					ARCHITECTURE_LINTER.accept(aPackage.substring(begin + 1, end).strip(), config);
				} else if(begin != end) {
					Main.error("Incomplete architecture specification string: " + s, null, "https://www.debian.org/doc/debian-policy/ch-relationships.html");
				}
				// package name
				String name = aPackage.split("[(\\[\\s]", 2)[0].strip();
				PACKAGE_NAME_LINTER.accept(name, config);
			}
		}
	};
	protected static final BiConsumer<String, Configuration> EXACT_DEPENDENCY_LINTER = (s, config) -> {
		String[] forbidden_operators = {"<<", "<=", ">=", ">>"};
		for(String op : forbidden_operators) {
			if(s.contains(op)) {
				Main.error("Only exact package versions can be provided: " + s);
			}
		}
		DEPENDENCY_LINTER.accept(s, config);
	};
	protected static final BiConsumer<String, Configuration> SOURCE_LINTER = (s, config) -> {
		String[] parts = s.split("\\(", 2);
		PACKAGE_NAME_LINTER.accept(parts[0].stripTrailing(), config);
		if(parts.length == 2) {
			if(config.checkedType == ControlType.SOURCE_PACKAGE_CONTROL || config.checkedType == ControlType.SOURCE_CONTROL) {
				Main.error("debian/control and .dsc files cannot have a version in their source: " + s, null, "https://www.debian.org/doc/debian-policy/ch-controlfields#source");
			}
			String version = parts[1].substring(0, parts.length - 1);
			VERSION_LINTER.accept(version, config);
		}
	};
	private static final BiConsumer<Stanza, Configuration> STANZA_COPYRIGHT_HEADER_LINTER = (s, config) -> {
		if(s.getField("Copyright") != null && s.getField("License") == null) {
			Main.error("A Copyright field alone is not sufficient; please include a License field as well when an explanation is needed: Copyright", null, "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#header-stanza");
		}
	};

	static {
		{
			HashMap<String, FieldSpec> general = new HashMap<>();
			general.put("Source", new FieldSpec(MANDATORY, SIMPLE, SOURCE_LINTER));
			general.put("Maintainer", new FieldSpec(MANDATORY, SIMPLE, ADDRESS_LINTER));
			general.put("Uploaders", new FieldSpec(OPTIONAL, FieldType.FOLDED, MULTI_ADDRESS_LINTER));
			general.put("Section", new FieldSpec(RECOMMENDED, SIMPLE, SECTION_LINTER));
			general.put("Priority", new FieldSpec(RECOMMENDED, SIMPLE, PRIORITY_LINTER));
			general.put("Build-Depends", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			general.put("Build-Depends-Indep", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			general.put("Build-Depends-Arch", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			general.put("Build-Conflicts", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			general.put("Build-Conflicts-Indep", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			general.put("Build-Conflicts-Arch", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			general.put("Standards-Version", new FieldSpec(MANDATORY, SIMPLE, STANDARDS_VERSION_LINTER));
			general.put("Homepage", new FieldSpec(OPTIONAL, SIMPLE, URL_LINTER));
			general.put("Vcs-Browser", new FieldSpec(OPTIONAL, SIMPLE, URL_LINTER));
			general.put("Vcs-Arch", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER)); // todo: vcs-specific linters
			general.put("Vcs-Bzr", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			general.put("Vcs-Cvs", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			general.put("Vcs-Darcs", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			general.put("Vcs-Git", new FieldSpec(OPTIONAL, SIMPLE, GIT_VCS_LINTER));
			general.put("Vcs-Hg", new FieldSpec(OPTIONAL, SIMPLE, MERCURIAL_VCS_LINTER));
			general.put("Vcs-Mtn", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			general.put("Vcs-Svn", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			general.put("Testsuite", new FieldSpec(OPTIONAL, SIMPLE, DEPENDENCY_LINTER));
			general.put("Rules-Requires-Root", new FieldSpec(OPTIONAL, SIMPLE, REQUIRES_ROOT_LINTER));
			StanzaSpec sourceGeneral = new StanzaSpec("general stanza", true, false, general, STANZA_SOURCE_AND_VCS_LINTER);
			//
			HashMap<String, FieldSpec> binary = new HashMap<>();
			binary.put("Package", new FieldSpec(MANDATORY, SIMPLE, PACKAGE_NAME_LINTER));
			binary.put("Architecture", new FieldSpec(MANDATORY, SIMPLE, SINGLE_ARCHITECTURE_LINTER));
			binary.put("Section", new FieldSpec(RECOMMENDED, SIMPLE, SECTION_LINTER));
			binary.put("Priority", new FieldSpec(RECOMMENDED, SIMPLE, PRIORITY_LINTER));
			binary.put("Essential", new FieldSpec(OPTIONAL, SIMPLE, BOOLEAN_LINTER));
			binary.put("Depends", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Pre-Depends", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Recommends", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Suggests", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Enhances", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Breaks", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Conflicts", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Description", new FieldSpec(RECOMMENDED, FieldType.MULTILINE, DESCRIPTION_LINTER));
			binary.put("Homepage", new FieldSpec(OPTIONAL, SIMPLE, URL_LINTER));
			binary.put("Built-Using", new FieldSpec(OPTIONAL, SIMPLE, EXACT_DEPENDENCY_LINTER));
			binary.put("Package-Type", new FieldSpec(OPTIONAL, SIMPLE, PACKAGE_TYPE_LINTER));
			StanzaSpec sourceBinary = new StanzaSpec("binary package stanza", true, true, binary, STANZA_DEFAULT_LINTER);
			//
			PACKAGE_SOURCE_CONTROL_STANZAS.add(sourceGeneral);
			PACKAGE_SOURCE_CONTROL_STANZAS.add(sourceBinary);
		}
		{
			HashMap<String, FieldSpec> binary = new HashMap<>();
			binary.put("Package", new FieldSpec(MANDATORY, SIMPLE, PACKAGE_NAME_LINTER));
			binary.put("Source", new FieldSpec(OPTIONAL, SIMPLE, SOURCE_LINTER));
			binary.put("Version", new FieldSpec(MANDATORY, SIMPLE, VERSION_LINTER));
			binary.put("Section", new FieldSpec(RECOMMENDED, SIMPLE, SECTION_LINTER));
			binary.put("Priority", new FieldSpec(RECOMMENDED, SIMPLE, PRIORITY_LINTER));
			binary.put("Architecture", new FieldSpec(MANDATORY, SIMPLE, SINGLE_ARCHITECTURE_LINTER));
			binary.put("Essential", new FieldSpec(OPTIONAL, SIMPLE, BOOLEAN_LINTER));
			binary.put("Depends", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Pre-Depends", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Recommends", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Suggests", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Enhances", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Breaks", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Conflicts", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			binary.put("Installed-Size", new FieldSpec(OPTIONAL, SIMPLE, SIZE_LINTER));
			binary.put("Maintainer", new FieldSpec(MANDATORY, SIMPLE, ADDRESS_LINTER));
			binary.put("Description", new FieldSpec(RECOMMENDED, FieldType.MULTILINE, DESCRIPTION_LINTER));
			binary.put("Homepage", new FieldSpec(OPTIONAL, SIMPLE, URL_LINTER));
			binary.put("Built-Using", new FieldSpec(OPTIONAL, SIMPLE, EXACT_DEPENDENCY_LINTER));
			StanzaSpec binaryControl = new StanzaSpec("binary package control stanza", true, false, binary, STANZA_SOURCE_LINTER);
			PACKAGE_BINARY_CONTROL_STANZAS.add(binaryControl);
		}
		{
			HashMap<String, FieldSpec> map = new HashMap<>();
			map.put("Format", new FieldSpec(MANDATORY, SIMPLE, FORMAT_VERSION_LINTER));
			map.put("Source", new FieldSpec(MANDATORY, SIMPLE, SOURCE_LINTER));
			map.put("Binary", new FieldSpec(OPTIONAL, FieldType.FOLDED, BINARY_LIST_LINTER));
			map.put("Architecture", new FieldSpec(OPTIONAL, SIMPLE, SINGLE_ARCHITECTURE_LINTER));
			map.put("Version", new FieldSpec(MANDATORY, SIMPLE, VERSION_LINTER));
			map.put("Maintainer", new FieldSpec(MANDATORY, SIMPLE, ADDRESS_LINTER));
			map.put("Uploaders", new FieldSpec(OPTIONAL, FieldType.FOLDED, MULTI_ADDRESS_LINTER));
			map.put("Homepage", new FieldSpec(OPTIONAL, SIMPLE, URL_LINTER));
			map.put("Vcs-Browser", new FieldSpec(OPTIONAL, SIMPLE, URL_LINTER));
			map.put("Vcs-Arch", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			map.put("Vcs-Bzr", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			map.put("Vcs-Cvs", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			map.put("Vcs-Darcs", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			map.put("Vcs-Git", new FieldSpec(OPTIONAL, SIMPLE, GIT_VCS_LINTER));
			map.put("Vcs-Hg", new FieldSpec(OPTIONAL, SIMPLE, MERCURIAL_VCS_LINTER));
			map.put("Vcs-Mtn", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			map.put("Vcs-Svn", new FieldSpec(OPTIONAL, SIMPLE, DEFAULT_LINTER));
			map.put("Testsuite", new FieldSpec(OPTIONAL, SIMPLE, DEPENDENCY_LINTER));
			map.put("Dgit", new FieldSpec(OPTIONAL, FieldType.FOLDED, DGIT_LINTER));
			map.put("Standards-Version", new FieldSpec(MANDATORY, SIMPLE, STANDARDS_VERSION_LINTER));
			map.put("Build-Depends", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			map.put("Build-Depends-Indep", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			map.put("Build-Depends-Arch", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			map.put("Build-Conflicts", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			map.put("Build-Conflicts-Indep", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			map.put("Build-Conflicts-Arch", new FieldSpec(OPTIONAL, FieldType.FOLDED, DEPENDENCY_LINTER));
			map.put("Package-List", new FieldSpec(RECOMMENDED, FieldType.MULTILINE, PACKAGE_LIST_LINTER));
			map.put("Checksums-Sha1", new FieldSpec(MANDATORY, FieldType.MULTILINE, SHA1_LINTER));
			map.put("Checksums-Sha256", new FieldSpec(MANDATORY, FieldType.MULTILINE, SHA256_LINTER));
			map.put("Files", new FieldSpec(MANDATORY, FieldType.MULTILINE, FILE_LIST_LINTER));
			StanzaSpec sourceControl = new StanzaSpec("source stanza", true, false, map, STANZA_SOURCE_CONTROL_LINTER);
			SOURCE_CONTROL_STANZAS.add(sourceControl);
		}
		{
			HashMap<String, FieldSpec> map = new HashMap<>();
			map.put("Format", new FieldSpec(MANDATORY, SIMPLE, FORMAT_VERSION_LINTER));
			map.put("Date", new FieldSpec(MANDATORY, SIMPLE, DATE_LINTER));
			map.put("Source", new FieldSpec(MANDATORY, SIMPLE, SOURCE_LINTER));
			map.put("Binary", new FieldSpec(MANDATORY, FieldType.FOLDED, BINARY_LIST_LINTER));
			map.put("Architecture", new FieldSpec(OPTIONAL, SIMPLE, SINGLE_ARCHITECTURE_LINTER));
			map.put("Version", new FieldSpec(MANDATORY, SIMPLE, VERSION_LINTER));
			map.put("Distribution", new FieldSpec(MANDATORY, SIMPLE, DISTRIBUTION_LINTER));
			map.put("Urgency", new FieldSpec(RECOMMENDED, SIMPLE, URGENCY_LINTER));
			map.put("Maintainer", new FieldSpec(MANDATORY, SIMPLE, ADDRESS_LINTER));
			map.put("Changed-By", new FieldSpec(OPTIONAL, SIMPLE, ADDRESS_LINTER));
			map.put("Description", new FieldSpec(RECOMMENDED, FieldType.MULTILINE, DESCRIPTION_LINTER));
			map.put("Closes", new FieldSpec(OPTIONAL, SIMPLE, NUMBER_LIST_LINTER));
			map.put("Changes", new FieldSpec(MANDATORY, FieldType.MULTILINE, CHANGE_LIST_LINTER));
			map.put("Checksums-Sha1", new FieldSpec(MANDATORY, FieldType.MULTILINE, SHA1_LINTER));
			map.put("Checksums-Sha256", new FieldSpec(MANDATORY, FieldType.MULTILINE, SHA256_LINTER));
			map.put("Files", new FieldSpec(MANDATORY, FieldType.MULTILINE, FILE_LIST_LINTER));
			StanzaSpec change = new StanzaSpec("changes stanza", true, false, map, STANZA_SOURCE_AND_CHECKSUM_LINTER);
			CHANGES_STANZAS.add(change);
		}
		{
			HashMap<String, FieldSpec> map = new HashMap<>();
			map.put("Format", new FieldSpec(MANDATORY, SIMPLE, COPYRIGHT_FORMAT_LINTER));
			map.put("Upstream-Name", new FieldSpec(OPTIONAL, SIMPLE, PACKAGE_NAME_LINTER));
			map.put("Upstream-Contact", new FieldSpec(OPTIONAL, SIMPLE, UPSTREAM_CONTACT_LINTER));
			map.put("Source", new FieldSpec(OPTIONAL, MULTILINE, COPYRIGHT_SOURCE_LINTER));
			map.put("Disclaimer", new FieldSpec(OPTIONAL, MULTILINE, DEFAULT_LINTER));
			map.put("Comment", new FieldSpec(OPTIONAL, MULTILINE, DEFAULT_LINTER));
			map.put("License", new FieldSpec(OPTIONAL, MULTILINE, LICENSE_LINTER));
			map.put("Copyright", new FieldSpec(OPTIONAL, MULTILINE, DEFAULT_LINTER));
			StanzaSpec header = new StanzaSpec("header stanza", true, false, map, STANZA_COPYRIGHT_HEADER_LINTER);
			COPYRIGHT_STANZAS.add(header);
			map = new HashMap<>();
			map.put("Files", new FieldSpec(MANDATORY, MULTILINE, COPYRIGHT_FILE_LIST_LINTER));
			map.put("License", new FieldSpec(MANDATORY, MULTILINE, LICENSE_LINTER));
			map.put("Copyright", new FieldSpec(MANDATORY, MULTILINE, DEFAULT_LINTER));
			map.put("Comment", new FieldSpec(OPTIONAL, MULTILINE, DEFAULT_LINTER));
			StanzaSpec files = new StanzaSpec("file stanza", true, true, map, STANZA_DEFAULT_LINTER);
			COPYRIGHT_STANZAS.add(files);
			map = new HashMap<>();
			map.put("License", new FieldSpec(MANDATORY, MULTILINE, LICENSE_LINTER));
			map.put("Comment", new FieldSpec(OPTIONAL, MULTILINE, DEFAULT_LINTER));
			StanzaSpec license = new StanzaSpec("stand-alone license stanza", false, true, map, STANZA_DEFAULT_LINTER);
			COPYRIGHT_STANZAS.add(license);
		}
	}

	/**
	 * Checks that a URL is valid and whether it is reachable.
	 *
	 * @param u      The URL to check
	 * @param config The configuration options
	 */
	private static void checkUrl(URL u, Configuration config) {
		if(config.urlForceHttps) {
			if(!"https".equals(u.getProtocol())) {
				Main.error("URL does not use HTTPS: " + u, "urlForceHttps");
			}
		}
		if(config.urlExists) {
			try {
				URLConnection conn = u.openConnection();
				conn.setConnectTimeout(30000);
				if(conn instanceof HttpURLConnection http) {
					http.setRequestMethod("HEAD");
					http.setInstanceFollowRedirects(true);
					http.connect();
					if(http.getResponseCode() < 200 || http.getResponseCode() >= 300) {
						Main.error("URL returned invalid response code (HTTP " + http.getResponseCode() + "): " + u);
					}
				} else {
					conn.connect();
				}
			} catch(IOException e) {
				Main.error("URL not found: " + u);
			}
		}
	}

	/**
	 * A linter fpr {@link ControlType#COPYRIGHT} files.
	 */
	public static class TypeCopyrightLinter implements BiConsumer<ControlFile, Configuration> {
		/**
		 * A cache for compiled patters, used in {@link #toRegex(String)}.
		 */
		private static final HashMap<String, Pattern> PARSED_PATTERNS = new HashMap<>();

		@Override
		public void accept(ControlFile file, Configuration config) {
			lintFileStanzas(file, config);
			checkCopyrightNames(file, config);
		}

		/**
		 * Checks that every used licenses are present in the file, and that all declared licenses are used.
		 *
		 * @param file   The file to check
		 * @param config The configuration
		 */
		public void checkCopyrightNames(ControlFile file, Configuration config) {
			if(config.licenseDeclarations) {
				HashSet<String> names = new HashSet<>();
				HashSet<String> optionalNames = new HashSet<>();
				List<StanzaSpec> specs = file.getSpecs();
				for(int i = 0; i < specs.size(); i++) {
					StanzaSpec spec = specs.get(i);
					if(spec.name().equals("file stanza")) {
						Stanza s = file.getStanzas().get(i);
						DataField license = s.getField("License");
						if(license != null) {
							String shortNames = license.data().split("\\n")[0].strip();
							String[] declarations = shortNames.replace(",", "").split(" (and|or) "); // todo: handle duplicate licenses on this line with proper logic expression processing
							if(!shortNames.equals(license.data().strip())) {
								// licenses are declared in this field -> no need for standalone stanzas
								for(String declaration : declarations) {
									optionalNames.add(simpleLicenseName(declaration));
								}
							} else {
								for(String declaration : declarations) {
									names.add(simpleLicenseName(declaration));
								}
							}
						}
					}
				}
				optionalNames.removeAll(names);
				optionalNames.remove("public-domain");
				for(int i = 0; i < specs.size(); i++) {
					StanzaSpec spec = specs.get(i);
					if(spec.name().equals("stand-alone license stanza")) {
						Stanza s = file.getStanzas().get(i);
						DataField license = s.getField("License");
						if(license != null) {
							String shortName = license.data().split("\\n")[0].strip();
							shortName = simpleLicenseName(shortName);
							if(!names.remove(shortName)) {
								boolean optional = optionalNames.remove(shortName);
								if(config.licenseDeclaredAfterExplanation && optional) {
									Main.error("Stand-alone license stanza is not required; this license has an explanation: " + shortName, "licenseDeclaredAfterExplanation");
								} else if(!optional) {
									Main.error("Stand-alone license stanza is not required; maybe the license was already defined: " + shortName, "licenseDeclarations");
								}
							}
						}
					}
				}
				if(!names.isEmpty()) {
					names.stream().filter(f -> !f.equals("public-domain")).forEach(s -> Main.error("License text is missing: " + s));
				}
			}
		}

		/**
		 * Checks whether the first pattern is at least as generic as the second one.
		 *
		 * @param first  The first pattern
		 * @param second The second pattern
		 * @return True if the first one matches any text the second one matches
		 */
		public boolean isMoreGeneric(String first, String second) {
			Pattern p1 = toRegex(first);
			char[] testers = {'a', 'b', 'c'};
			boolean firstBetter = true;
			for(char tester : testers) {
				String filler = new StringBuilder().repeat(tester, first.length() + second.length() + 1).toString();
				String secondTest = second.replaceAll("(^\\*|[^\\\\]\\*)", filler).replaceAll("(^\\?|[^\\\\]\\?)", tester + "");
				firstBetter &= p1.matcher(secondTest).matches();
			}
			return firstBetter;
		}

		/**
		 * Lints file stanzas, checking whether they contain duplicate patterns and whether more generic patterns are listed first.
		 *
		 * @param file   The file to check
		 * @param config The configuration
		 */
		public void lintFileStanzas(ControlFile file, Configuration config) {
			if(config.copyrightFilePatternGenerality) {
				HashSet<String> previousPatterns = new HashSet<>();
				for(int i = 0; i < file.getSpecs().size(); i++) {
					StanzaSpec spec = file.getSpecs().get(i);
					if(spec.name().equals("file stanza")) {
						ArrayList<String> currentPatterns = new ArrayList<>();
						Stanza s = file.getStanzas().get(i);
						DataField field = s.getField("Files");
						Arrays.stream(field.data().split("\\n")).map(String::trim).filter(d -> !d.isEmpty()).forEachOrdered(currentPatterns::add);
						if(config.redundantFilePattern) {
							for(int i1 = 0; i1 < currentPatterns.size(); i1++) {
								String pat1 = currentPatterns.get(i1);
								if(previousPatterns.contains(pat1)) {
									Main.error("Duplicate file pattern: " + pat1, "redundantFilePattern");
								}
								for(int i2 = i1 + 1; i2 < currentPatterns.size(); i2++) {
									String pat2 = currentPatterns.get(i2);
									if(isMoreGeneric(pat1, pat2) || isMoreGeneric(pat2, pat1)) {
										Main.error("File stanza includes redundant pattern: " + pat1 + " and " + pat2 + " cannot both be needed", "redundantFilePattern");
									}
								}
							}
						}
						for(String current : currentPatterns) {
							for(String previous : previousPatterns) {
								if(isMoreGeneric(current, previous)) {
									Main.error("More generic patterns should precede specific ones: " + previous + " and " + current, "copyrightFilePatternGenerality", "https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/#files-field");
								}
							}
						}
						previousPatterns.addAll(currentPatterns);
					}
				}
			}
		}

		/**
		 * Converts a file pattern to a regular expression. Repeated queries are cached via {@link #PARSED_PATTERNS}.
		 *
		 * @param pattern The pattern to convert
		 * @return The regex pattern
		 */
		public Pattern toRegex(String pattern) {
			while(pattern.startsWith("./")) {
				pattern = pattern.substring("./".length());
			}
			pattern = pattern.replace("/./", "/");
			if(PARSED_PATTERNS.containsKey(pattern)) {
				return PARSED_PATTERNS.get(pattern);
			}
			HashSet<Character> escaped = new HashSet<>(List.of('(', ')', '[', '{', '*', '+', '.', '$', '^', '\\', '|', '?'));
			StringBuilder sb = new StringBuilder("^(\\./)?");
			for(int i = 0; i < pattern.toCharArray().length; i++) {
				switch(pattern.charAt(i)) {
					case '\\' -> {
						sb.append('\\');
						if(pattern.charAt(i + 1) == '\\') {
							sb.append("\\\\");
						} else {
							sb.append(pattern.charAt(i + 1));
						}
						i++;
					}
					case '*' -> sb.append(".*");
					case '?' -> sb.append(".");
					default -> {
						if(escaped.contains(pattern.charAt(i))) {
							sb.append("\\");
						}
						sb.append(pattern.charAt(i));
					}
				}
			}
			sb.append("$");
			Pattern p = Pattern.compile(sb.toString());
			PARSED_PATTERNS.put(pattern, p);
			return p;
		}

		/**
		 * Gets the simple name of a license.
		 *
		 * @param name The license name
		 * @return The simple name
		 */
		protected String simpleLicenseName(String name) {
			String[] parts = name.split(" ", 2);
			String base = parts[0];
			while(base.endsWith(".0")) {
				base = base.substring(0, base.length() - 2);
			}
			if(base.endsWith("-1")) {
				base = base.substring(0, base.length() - 2);
			}
			parts[0] = base;
			return String.join(" ", parts);
		}
	}
}