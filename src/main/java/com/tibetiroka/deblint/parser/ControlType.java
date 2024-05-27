/*
 * Copyright (c) 2024 by tibetiroka.
 *
 * debian-control-linter is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * debian-control-linter is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.tibetiroka.deblint.parser;

import com.tibetiroka.deblint.linter.FileLinter;
import com.tibetiroka.deblint.linter.Linters;

import java.util.List;

/**
 * The types of supported control files. THey each have a description for use with {@code --type-info}, and their own linter configurations.
 */
public enum ControlType {
	SOURCE_PACKAGE_CONTROL("debian/control", "control", "source package control file", Linters.PACKAGE_SOURCE_CONTROL_STANZAS, null, false), BINARY_PACKAGE_CONTROL("DEBIAN/control", "control", "binary package control file", Linters.PACKAGE_BINARY_CONTROL_STANZAS, null, false), COPYRIGHT("debian/copyright", "copyright", "copyright file", Linters.COPYRIGHT_STANZAS, Linters.TYPE_COPYRIGHT_LINTER, false), SOURCE_CONTROL(".dsc", ".dsc", "source control file", Linters.SOURCE_CONTROL_STANZAS, null, true), CHANGES(".changes", ".changes", "upload control file", Linters.CHANGES_STANZAS, null, true);
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
	private final FileLinter linter;
	/**
	 * The list of stanzas that can appear in this type, in their expected order.
	 */
	private final List<StanzaSpec> stanzas;
	/**
	 * Whether the file supports OpenPGP signatures.
	 */
	private final boolean supportsPgp;
	/**
	 * The debian standard name for this control file type.
	 */
	private final String typeName;

	private ControlType(String typeName, String defaultFile, String description, List<StanzaSpec> stanzas, FileLinter linter, boolean supportsPgp) {
		this.typeName = typeName;
		this.defaultFile = defaultFile;
		this.description = description;
		this.stanzas = stanzas;
		this.linter = linter == null ? (a, b) -> {
		} : linter;
		this.supportsPgp = supportsPgp;
	}

	/**
	 * The default location/name of control files in this type.
	 *
	 * @return {@link #defaultFile}
	 */
	public String getDefaultFile() {
		return defaultFile;
	}

	/**
	 * The short description of this type.
	 *
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * The file-wide linter for this type.
	 *
	 * @return {@link #linter}
	 */
	public FileLinter getLinter() {
		return linter;
	}

	/**
	 * The list of stanzas that can appear in this type, in their expected order.
	 *
	 * @return {@link #stanzas}
	 */
	public List<StanzaSpec> getStanzas() {
		return stanzas;
	}

	/**
	 * The debian standard name for this control file type.
	 *
	 * @return {@link #typeName}
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Whether the file supports OpenPGP signatures.
	 *
	 * @return {@link #supportsPgp}
	 */
	public boolean isSupportsPgp() {
		return supportsPgp;
	}
}