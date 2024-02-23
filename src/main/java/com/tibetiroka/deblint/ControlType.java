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

import java.util.List;
import java.util.function.BiConsumer;

/**
 * The types of supported control files. THey each have a description for use with {@code --type-info}, and their own linter configurations.
 */
public enum ControlType {
	SOURCE_PACKAGE_CONTROL("debian/control", "control", "source package control file", Linters.PACKAGE_SOURCE_CONTROL_STANZAS, null), BINARY_PACKAGE_CONTROL("DEBIAN/control", "control", "binary package control file", Linters.PACKAGE_BINARY_CONTROL_STANZAS, null), COPYRIGHT("debian/copyright", "copyright", "copyright file", Linters.COPYRIGHT_STANZAS, Linters.TYPE_COPYRIGHT_LINTER), SOURCE_CONTROL(".dsc", ".dsc", "source control file", Linters.SOURCE_CONTROL_STANZAS, null), CHANGES(".changes", ".changes", "upload control file", Linters.CHANGES_STANZAS, null);
	/**
	 * The default location/name of control files in this type.
	 */
	private final String defaultFile;
	/**
	 * The short description of this type.
	 */
	private final String description;
	/**
	 * The file-wide linter for this type.
	 */
	private final BiConsumer<ControlFile, Configuration> linter;
	/**
	 * The list of stanzas that can appear in this type, in their expected order.
	 */
	private final List<StanzaSpec> stanzas;
	/**
	 * The debian standard name for this control file type.
	 */
	private final String typeName;

	private ControlType(String typeName, String defaultFile, String description, List<StanzaSpec> stanzas, BiConsumer<ControlFile, Configuration> linter) {
		this.typeName = typeName;
		this.defaultFile = defaultFile;
		this.description = description;
		this.stanzas = stanzas;
		this.linter = linter == null ? (a, b) -> {
		} : linter;
	}

	/**
	 * The default location/name of control files in this type.
	 */
	public String getDefaultFile() {
		return defaultFile;
	}

	/**
	 * The short description of this type.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * The file-wide linter for this type.
	 */
	public BiConsumer<ControlFile, Configuration> getLinter() {
		return linter;
	}

	/**
	 * The list of stanzas that can appear in this type, in their expected order.
	 */
	public List<StanzaSpec> getStanzas() {
		return stanzas;
	}

	/**
	 * The debian standard name for this control file type.
	 */
	public String getTypeName() {
		return typeName;
	}
}