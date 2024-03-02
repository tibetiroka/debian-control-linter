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
import java.util.ListIterator;

/**
 * A data field is a single key-value mapping inside a stanza.
 *
 * @param name The name of this field
 * @param data The data stored in this field, with surrounding whitespaces trimmed
 * @param type The type of this field, as parsed
 */
public record DataField(String name, String data, FieldType type) {
	public DataField(String name, String data, FieldType type) {
		this.name = name;
		this.data = data.stripTrailing().replaceAll("^[ \t]*", "");
		this.type = type;
	}

	/**
	 * Parses the next data field, removing its lines from the input list. Please note that this method handles {@link FieldType#MULTILINE multiline} and {@link FieldType#FOLDED folded} fields the same; multiline fields can be converted to folded fields later using {@link #changeType(FieldType, boolean)}.
	 *
	 * @param lines The data field and any content following it in the control file
	 * @return The data field, or null if it could not be parsed
	 */
	public static DataField parseNext(List<String> lines, Configuration config) {
		if(lines.isEmpty()) {
			Main.error("Missing expected data field: no lines left");
			return null;
		}
		String first = lines.getFirst();
		String[] parts = first.split(":", 2);
		String fieldName = parts[0];
		if(parts.length == 1) {
			Main.error("Data field declaration is missing colon: " + first);
			return null;
		}
		if(config.fieldName && !fieldName.matches("[!-\"$-,.-9;-~][!-9;-~]*")) {
			Main.error("Invalid field name: " + fieldName, "fieldName", "https://www.debian.org/doc/debian-policy/ch-controlfields#syntax-of-control-files");
		}
		if(config.spaceAfterColon && (!parts[1].startsWith(" ") && !parts[1].isEmpty())) {
			Main.error("Missing space after colon: " + fieldName, "spaceAfterColon", "https://www.debian.org/doc/debian-policy/ch-controlfields#syntax-of-control-files");
		}
		StringBuilder contents = new StringBuilder(parts[1]);
		lines.removeFirst();
		ListIterator<String> it = lines.listIterator();
		boolean multiline = false;
		while(it.hasNext()) {
			String s = it.next();
			if(s.startsWith(" ") || s.startsWith("\t")) {
				contents.append("\n");
				contents.append(s);
				it.remove();
				multiline = true;
			} else {
				break;
			}
		}
		return new DataField(fieldName, contents.toString(), multiline ? FieldType.MULTILINE : FieldType.SIMPLE);
	}

	/**
	 * Changes the type of this field. If the new type is compatible with the old type, a data field is returned with the correct type. Its data may be different if the type conversion mandates changes.
	 * <p>
	 * If the field cannot be converted to the new type, {@code null} is returned.
	 *
	 * @param type  The new type of the field
	 * @param force If true, conversion is always done, even if it is invalid
	 * @return The converted field or null
	 */
	public DataField changeType(FieldType type, boolean force) {
		if(type == this.type) {
			return this;
		} else if(this.type == FieldType.MULTILINE && type == FieldType.FOLDED) {
			return new DataField(name, data.replaceAll("\\s*\\n\\s*", ""), type);
		} else if(this.type == FieldType.SIMPLE) {
			return new DataField(name, data, type);
		} else if(force) {
			if(type == FieldType.SIMPLE) {
				return new DataField(name, data.split("\\n", 2)[0], type);
			} else {
				return new DataField(name, data, type);
			}
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "DataField{" + "name='" + name + '\'' + ", data='" + data + '\'' + ", type=" + type + '}';
	}
}