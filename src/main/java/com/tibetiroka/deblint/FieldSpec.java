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

import java.util.function.BiConsumer;

/**
 * Specification for a field.
 *
 * @param required Whether the field is required, recommended or is completely optional.
 * @param type     The type of this field, as the stanza requires it
 * @param linter   The linter used for the value of this field
 */
public record FieldSpec(RequirementStatus required, FieldType type, BiConsumer<String, Configuration> linter) {
	/**
	 * The importance of a data field. {@link #MANDATORY mandatory} fields are required for a {@link StanzaSpec} to apply to a parsed {@link Stanza},
	 */
	public static enum RequirementStatus {
		MANDATORY, RECOMMENDED, OPTIONAL
	}
}