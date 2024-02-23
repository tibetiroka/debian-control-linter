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

import com.tibetiroka.deblint.FieldSpec.RequirementStatus;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * A stanza spec is a description of what a stanza should or could contain.
 *
 * @param name       The name of the stanza, used for error messages
 * @param mandatory  Whether the stanza is mandatory
 * @param repeatable Whether the stanza can occur multiple times in a row
 * @param fields     The fields this stanza can contain
 * @param linter     The linter used for this stanza
 */
public record StanzaSpec(String name, boolean mandatory, boolean repeatable, HashMap<String, FieldSpec> fields, BiConsumer<Stanza, Configuration> linter) {
	/**
	 * Checks whether this spec matches the given stanza. A spec matches a stanza if all required fields are present. The types of the fields are not checked, so it is possible that this method returns true for a match that does not strictly follow the specification. These are reported in {@link #match(Stanza, Configuration)}.
	 *
	 * @param s      The stanza to match
	 * @param config The configuration
	 * @return True if matches
	 */
	public boolean canMatch(Stanza s, Configuration config) {
		for(Entry<String, FieldSpec> entry : this.fields.entrySet()) {
			if(entry.getValue().required() == RequirementStatus.MANDATORY) {
				if(s.getField(entry.getKey()) == null) {
					return false; // missing required field
				}
			}
		}
		return true;
	}

	/**
	 * Matches this specification onto the stanza, changing the parsed types of fields if required.
	 *
	 * @param s      The stanza to match
	 * @param config The configuration
	 */
	public void match(Stanza s, Configuration config) {
		for(Entry<String, FieldSpec> entry : this.fields.entrySet()) {
			String name = entry.getKey().toLowerCase();
			FieldSpec spec = entry.getValue();
			DataField field = s.getField(name);
			if(field != null) {
				DataField proper = field.changeType(spec.type());
				s.dataFields.remove(field);
				if(proper == null) {
					Main.error("Invalid field type for field " + name + ": expected " + spec.type() + ", found " + field.type(), null, "https://www.debian.org/doc/debian-policy/ch-controlfields#syntax-of-control-files");
				} else {
					s.dataFields.add(proper);
				}
			} else if(config.recommendedFields && spec.required() == RequirementStatus.RECOMMENDED) {
				Main.error("Missing recommended field: " + name, "recommendedFields");
			}
		}
		if(config.customFields) {
			s.dataFields.forEach(field -> {
				boolean found = false;
				for(Entry<String, FieldSpec> entry : this.fields.entrySet()) {
					if(field.name().equalsIgnoreCase(entry.getKey())) {
						found = true;
						break;
					}
				}
				if(!found) {
					Main.error("Custom field: " + field.name());
					if(config.customFieldNames) {
						if(!field.name().matches("X[BCS]{1,3}-.+")) {
							Main.error("Invalid custom field name: " + field.name(), "customFieldNames", "https://www.debian.org/doc/debian-policy/ch-controlfields#user-defined-fields");
						} else {
							String prefix = field.name().split("-", 2)[0];
							char[] chars = {'B', 'C', 'S'};
							for(char c : chars) {
								if(prefix.indexOf(c) != prefix.lastIndexOf(c)) {
									Main.error("Duplicate marker in custom field name : " + c + " " + field.name(), "customFieldNames", "https://www.debian.org/doc/debian-policy/ch-controlfields#user-defined-fields");
								}
							}
						}
					}
				}
			});
		}
	}
}