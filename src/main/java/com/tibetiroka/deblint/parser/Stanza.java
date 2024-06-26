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

import com.tibetiroka.deblint.Configuration;
import com.tibetiroka.deblint.Main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Stanzas are collections of data fields, separated by blank lines.
 */
public class Stanza {
	/**
	 * The list of data fields in this stanza.
	 */
	public final List<DataField> dataFields = new ArrayList<>();
	/**
	 * The line number of the first line in this stanza.
	 */
	private final int firstLine;

	/**
	 * Creates a new stanza beginning at the specified line.
	 *
	 * @param firstLine The number of the first line
	 */
	private Stanza(int firstLine) {
		this.firstLine = firstLine;
	}

	/**
	 * Parses the next stanza, removing it from the list of lines. The lines must begin with a stanza.
	 *
	 * @param lines  The list of text lines
	 * @param config The configuration
	 * @return The stanza or null if it could not be parsed
	 */
	public static Stanza parseNext(List<Line> lines, Configuration config) {
		Stanza s = new Stanza(lines.getFirst().lineNumber());
		Pattern empty = Pattern.compile("^[ \\t]*$");
		HashSet<String> fieldNames = new HashSet<>();
		while(!lines.isEmpty() && !empty.matcher(lines.getFirst().text()).matches()) {
			int index = lines.getFirst().lineNumber();
			DataField field = DataField.parseNext(lines, config);
			if(field != null) {
				if(fieldNames.contains(field.name().toUpperCase())) {
					if(config.duplicateField) {
						Main.error("Duplicate data field in stanza: " + field.name(), null, "https://www.debian.org/doc/debian-policy/ch-controlfields#syntax-of-control-files", index);
					}
				} else {
					if(config.emptyFields) {
						if(field.data().isEmpty()) {
							Main.error("Empty data field: " + field.name(), null, "https://www.debian.org/doc/debian-policy/ch-controlfields#syntax-of-control-files", index);
						}
					}
					s.dataFields.add(field);
					fieldNames.add(field.name().toLowerCase());
				}
			} else {
				break;
			}
		}
		while(!lines.isEmpty() && empty.matcher(lines.getFirst().text()).matches()) {
			lines.removeFirst();
		}
		if(s.dataFields.isEmpty()) {
			return null;
		}
		return s;
	}

	/**
	 * Gets the field with the specified name. Field names are case-insensitive.
	 *
	 * @param name The name of the field
	 * @return The field or null if not present
	 */
	public DataField getField(String name) {
		return dataFields.stream().filter(field -> field.name().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	/**
	 * Gets the line number of the first line of the stanza.
	 *
	 * @return The line number
	 */
	public int getFirstLine() {
		return firstLine;
	}

	@Override
	public String toString() {
		return "Stanza{" +
		       "dataFields=" + dataFields +
		       '}';
	}
}