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

import com.tibetiroka.deblint.parser.ControlType;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration options specify what checks are enabled and how they behave. An option with a {@code true} value means the check is enabled, not that the described behaviour is allowed.
 * <p>
 * A short description for each check can be found in the fields of {@link ConfigOptionDetails}.
 */
public class Configuration implements Cloneable {
	/**
	 * Enables all checks, even if they are not mandated by the specification.
	 */
	public static final Configuration PRESET_EXACT;
	/**
	 * Accepts control files that follow the specifications, even if they don't follow the best practices.
	 */
	public static final Configuration PRESET_NORMAL;
	/**
	 * Accepts control files that are generally supported by parsers, even though they don't follow the specification.
	 */
	public static final Configuration PRESET_QUIRKS;
	/**
	 * Only accepts control files that follow all best practices.
	 */
	public static final Configuration PRESET_STRICT;

	static {
		PRESET_QUIRKS = new Configuration("The quirks preset disables all non-essential checks.");
		PRESET_NORMAL = new Configuration("The normal preset is designed for files following the letter of the specification (unless ambiguous), but not necessarily following all best practices.");
		PRESET_STRICT = new Configuration("The strict preset is for files following the specification, including any best practices or conventions.");
		PRESET_EXACT = new Configuration("The exact preset enables all checks, even ones not mentioned or mandated by the specification, including unstable checks. Not for production use.");
		//
		//
		//
		PRESET_STRICT.emptyStanzaSeparators = true;
		PRESET_EXACT.emptyStanzaSeparators = true;
		//
		PRESET_NORMAL.emptyFields = true;
		PRESET_STRICT.emptyFields = true;
		PRESET_EXACT.emptyFields = true;
		//
		PRESET_NORMAL.comments = true;
		PRESET_STRICT.comments = true;
		PRESET_EXACT.comments = true;
		//
		PRESET_STRICT.spaceAfterColon = true;
		PRESET_EXACT.spaceAfterColon = true;
		//
		PRESET_EXACT.fieldNameCapitalization = true;
		//
		PRESET_STRICT.customFields = true;
		PRESET_EXACT.customFields = true;
		//
		PRESET_EXACT.maintainerNameFullStop = true;
		//
		PRESET_STRICT.recommendedFields = true;
		PRESET_EXACT.recommendedFields = true;
		//
		PRESET_NORMAL.debianInstallerSection = true;
		PRESET_STRICT.debianInstallerSection = true;
		PRESET_EXACT.debianInstallerSection = true;
		//
		PRESET_NORMAL.extraPriority = true;
		PRESET_STRICT.extraPriority = true;
		PRESET_EXACT.extraPriority = true;
		//
		PRESET_STRICT.strictArch = true;
		PRESET_EXACT.strictArch = true;
		//
		PRESET_EXACT.strictStandardsVersion = true;
		//
		PRESET_EXACT.urlExists = true;
		//
		PRESET_EXACT.urlForceHttps = true;
		//
		PRESET_EXACT.vcsBranch = true;
		//
		PRESET_STRICT.duplicateArchitecture = true;
		PRESET_EXACT.duplicateArchitecture = true;
		//
		PRESET_EXACT.unknownPackageType = true;
		//
		PRESET_STRICT.redundantPackageType = true;
		PRESET_EXACT.redundantPackageType = true;
		//
		PRESET_EXACT.exactFormatVersion = true;
		//
		PRESET_STRICT.dgitExtraData = true;
		PRESET_EXACT.dgitExtraData = true;
		//
		PRESET_NORMAL.missingSectionOrPriority = true;
		PRESET_STRICT.missingSectionOrPriority = true;
		PRESET_EXACT.missingSectionOrPriority = true;
		//
		PRESET_STRICT.duplicatePackages = true;
		PRESET_EXACT.duplicatePackages = true;
		//
		PRESET_STRICT.duplicateFiles = true;
		PRESET_EXACT.duplicateFiles = true;
		//
		PRESET_EXACT.multipleDistributions = true;
		//
		PRESET_NORMAL.customUrgencies = true;
		PRESET_STRICT.customUrgencies = true;
		PRESET_EXACT.customUrgencies = true;
		//
		PRESET_STRICT.urgencyDescriptionParentheses = true;
		PRESET_EXACT.urgencyDescriptionParentheses = true;
		//
		PRESET_STRICT.duplicateIssueNumbers = true;
		PRESET_EXACT.duplicateIssueNumbers = true;
		//
		PRESET_STRICT.strictCopyrightFormatVersion = true;
		PRESET_EXACT.strictCopyrightFormatVersion = true;
		//
		PRESET_EXACT.upstreamContactStyle = true;
		//
		PRESET_EXACT.trailingSpace = true;
		//
		PRESET_EXACT.sourceRedundantVersion = true;
		//
		PRESET_EXACT.customLicenseException = true;
		//
		PRESET_EXACT.redundantFilePattern = true;
		//
		PRESET_NORMAL.copyrightFilePatternGenerality = true;
		PRESET_STRICT.copyrightFilePatternGenerality = true;
		PRESET_EXACT.copyrightFilePatternGenerality = true;
		//
		PRESET_NORMAL.licenseName = true;
		PRESET_STRICT.licenseName = true;
		PRESET_EXACT.licenseName = true;
		//
		PRESET_NORMAL.licenseDeclarations = true;
		PRESET_STRICT.licenseDeclarations = true;
		PRESET_EXACT.licenseDeclarations = true;
		//
		PRESET_EXACT.copyrightSourceStyle = true;
		//
		PRESET_NORMAL.unknownPriority = true;
		PRESET_STRICT.unknownPriority = true;
		PRESET_EXACT.unknownPriority = true;
		//
		PRESET_STRICT.customFieldNames = true;
		PRESET_EXACT.customFieldNames = true;
		//
		PRESET_EXACT.licenseDeclaredAfterExplanation = true;
		//
		PRESET_STRICT.duplicateFilePattern = true;
		PRESET_EXACT.duplicateFilePattern = true;
		//
		PRESET_NORMAL.fieldName = true;
		PRESET_STRICT.fieldName = true;
		PRESET_EXACT.fieldName = true;
		//
		PRESET_NORMAL.duplicateField = true;
		PRESET_STRICT.duplicateField = true;
		PRESET_EXACT.duplicateField = true;
		//
		PRESET_NORMAL.fieldType = true;
		PRESET_STRICT.fieldType = true;
		PRESET_EXACT.fieldType = true;
		//
		PRESET_NORMAL.archInversion = true;
		PRESET_STRICT.archInversion = true;
		PRESET_EXACT.archInversion = true;
		//
		PRESET_NORMAL.futureDate = true;
		PRESET_STRICT.futureDate = true;
		PRESET_EXACT.futureDate = true;
		//
		PRESET_NORMAL.descriptionReservedSyntax = true;
		PRESET_STRICT.descriptionReservedSyntax = true;
		PRESET_EXACT.descriptionReservedSyntax = true;
		//
		PRESET_NORMAL.fileListIndent = true;
		PRESET_STRICT.fileListIndent = true;
		PRESET_EXACT.fileListIndent = true;
		//
		PRESET_NORMAL.duplicateVcs = true;
		PRESET_STRICT.duplicateVcs = true;
		PRESET_EXACT.duplicateVcs = true;
		//
		PRESET_NORMAL.upstreamVersionStyle = true;
		PRESET_STRICT.upstreamVersionStyle = true;
		PRESET_EXACT.upstreamVersionStyle = true;
		//
		PRESET_NORMAL.url = true;
		PRESET_STRICT.url = true;
		PRESET_EXACT.url = true;
		//
		PRESET_NORMAL.versionStyle = true;
		PRESET_STRICT.versionStyle = true;
		PRESET_EXACT.versionStyle = true;
		//
		PRESET_STRICT.strictSection = true;
		PRESET_EXACT.strictSection = true;
		//
		PRESET_NORMAL.leadingEmptyLine = true;
		PRESET_STRICT.leadingEmptyLine = true;
		PRESET_EXACT.leadingEmptyLine = true;
		//
		PRESET_NORMAL.email = true;
		PRESET_STRICT.email = true;
		PRESET_EXACT.email = true;
		//
		PRESET_NORMAL.addressStyle = true;
		PRESET_STRICT.addressStyle = true;
		PRESET_EXACT.addressStyle = true;
	}

	/**
	 * The description of the preset, used in {@code --preset-info}.
	 */
	public final String presetDescription;
	public boolean addressStyle;
	public boolean archInversion;
	/**
	 * The type of control file this object is configured for.
	 */
	public ControlType checkedType = ControlType.COPYRIGHT;
	public boolean comments;
	public boolean copyrightFilePatternGenerality;
	public boolean copyrightSourceStyle;
	public boolean customFieldNames;
	public boolean customFields;
	public boolean customLicenseException;
	public boolean customUrgencies;
	public boolean debianInstallerSection;
	public boolean descriptionReservedSyntax;
	public boolean dgitExtraData;
	public boolean duplicateArchitecture;
	public boolean duplicateField;
	public boolean duplicateFilePattern;
	public boolean duplicateFiles;
	public boolean duplicateIssueNumbers;
	public boolean duplicatePackages;
	public boolean duplicateVcs;
	public boolean email;
	public boolean emptyFields;
	public boolean emptyStanzaSeparators;
	public boolean exactFormatVersion;
	public boolean extraPriority;
	public boolean fieldName;
	public boolean fieldNameCapitalization;
	public boolean fieldType;
	public boolean fileListIndent;
	public boolean futureDate;
	public boolean leadingEmptyLine;
	public boolean licenseDeclarations;
	public boolean licenseDeclaredAfterExplanation;
	public boolean licenseName;
	public boolean maintainerNameFullStop;
	public boolean missingSectionOrPriority;
	public boolean multipleDistributions;
	public boolean recommendedFields;
	public boolean redundantFilePattern;
	public boolean redundantPackageType;
	public boolean sourceRedundantVersion;
	public boolean spaceAfterColon;
	public boolean strictArch;
	public boolean strictCopyrightFormatVersion;
	public boolean strictSection;
	public boolean strictStandardsVersion;
	/**
	 * The file checked by this configuration.
	 */
	public File targetFile;
	public boolean trailingSpace;
	public boolean unknownPackageType;
	public boolean unknownPriority;
	public boolean upstreamContactStyle;
	public boolean upstreamVersionStyle;
	public boolean urgencyDescriptionParentheses;
	public boolean url;
	public boolean urlExists;
	public boolean urlForceHttps;
	public boolean vcsBranch;
	public boolean versionStyle;

	/**
	 * Creates a new configuration with the specified description. The description cannot be changed later.
	 *
	 * @param description The description of this configuration.
	 */
	private Configuration(String description) {
		this.presetDescription = description;
	}

	/**
	 * Gets the available checks in a configuration.
	 *
	 * @return The fields of the checks
	 */
	public static Set<Field> getChecks() {
		HashSet<Field> fields = new HashSet<>();
		for(Field field : Configuration.class.getDeclaredFields()) {
			if(!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && field.getType() == boolean.class) {
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * The list of presets from least strict to the most strict.
	 */
	public static List<Configuration> getPrecedenceList() {
		return new ArrayList<>(List.of(PRESET_QUIRKS, PRESET_NORMAL, PRESET_STRICT, PRESET_EXACT));
	}

	/**
	 * Gets the available configuration presets.
	 *
	 * @return The fields containing the presets
	 */
	public static Set<Field> getPresets() {
		HashSet<Field> fields = new HashSet<>();
		for(Field field : Configuration.class.getDeclaredFields()) {
			if(Modifier.isStatic((field.getModifiers())) && Modifier.isFinal(field.getModifiers()) && field.getName().startsWith("PRESET_") && field.getType() == Configuration.class) {
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * Finalizes the changes made to this configuration, bringing it to a valid state. Editing values after this call might cause unexpected behaviour. This method should be called after this object is configured.
	 */
	public void apply() {
		if(targetFile == null) {
			targetFile = new File(checkedType.getDefaultFile());
		}
	}

	@Override
	public Configuration clone() {
		try {
			return (Configuration) super.clone();
		} catch(CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

	/**
	 * Contains details about various configuration options, for use via {@code --check-info}.
	 */
	public static final class ConfigOptionDetails {
		public static final String addressStyle = "An address (name and email) not using the proper format.";
		public static final String archInversion = "Mixed inverted and non-inverted architectures.";
		public static final String comments = "Comments outside of debian/control files.";
		public static final String copyrightFilePatternGenerality = "Whether more generic file patterns are declared first in copyright files. When disabled, debian/copyright file lists are not checked for for 'redundantFilePattern' and 'duplicateFilePattern'.";
		public static final String copyrightSourceStyle = "A Source field in a debian/copyright file that is not a single URL address.";
		public static final String customFieldNames = "A user-defined field not following the naming scheme.";
		public static final String customFields = "Fields that are not listed in the specification. When disabled, fields are not checked for 'customFieldNames'.";
		public static final String customLicenseException = "License exception not listed in the specification.";
		public static final String customUrgencies = "An Urgency field value not listed in the specification.";
		public static final String debianInstallerSection = "A Section field value of 'debian-installer'.";
		public static final String descriptionReservedSyntax = "Reserved syntax used in descriptions.";
		public static final String dgitExtraData = "Extra data specified in a Dgit field, reserved for future expansion.";
		public static final String duplicateArchitecture = "An Architecture field declaring the same architecture more than once.";
		public static final String duplicateField = "A field declared twice in the same stanza.";
		public static final String duplicateFilePattern = "A file pattern repeated within the same field.";
		public static final String duplicateFiles = "Duplicate entry in a file list.";
		public static final String duplicateIssueNumbers = "A Closes field with repeated issue numbers.";
		public static final String duplicatePackages = "Duplicate entry in a package list.";
		public static final String duplicateVcs = "More than one version control fields declared.";
		public static final String email = "An email address with an invalid format.";
		public static final String emptyFields = "Fields with no value specified.";
		public static final String emptyStanzaSeparators = "Stanza separators that contain whitespaces.";
		public static final String exactFormatVersion = "An unrecognized format version.";
		public static final String extraPriority = "The use of the deprecated Priority value 'extra'.";
		public static final String fieldName = "A field name using invalid characters or formatting.";
		public static final String fieldNameCapitalization = "Field name that is not capitalized according to the established conventions.";
		public static final String fieldType = "A field with an invalid type.";
		public static final String fileListIndent = "A file list not using a single space as indentation.";
		public static final String futureDate = "A future date specified in a Date field.";
		public static final String leadingEmptyLine = "A field that should begin with an empty line but doesn't.";
		public static final String licenseDeclarations = "Declared licenses that are not used, or used licenses that are not declared.";
		public static final String licenseDeclaredAfterExplanation = "A license that had an explanation every time it was used, and still has a stand-alone license stanza.";
		public static final String licenseName = "Short license name(s) not properly formatted. When disabled, debian/copyright licenses are also not checked for 'customLicenseException'.";
		public static final String maintainerNameFullStop = "A maintainer name that contains a full stop.";
		public static final String missingSectionOrPriority = "A missing section or priority value in a .changes file's file list.";
		public static final String multipleDistributions = "A Distribution field with more than one distribution specified.";
		public static final String recommendedFields = "A recommended field that is not present in the stanza.";
		public static final String redundantFilePattern = "A file pattern that is not necessary, because there is a more generic pattern in the same field.";
		public static final String redundantPackageType = "A Package-Type field with a value of 'deb' in a debian/control file.";
		public static final String sourceRedundantVersion = "A version specified in a Source field that matches the value of the Version field.";
		public static final String spaceAfterColon = "A colon that has no space after it, and doesn't end the line.";
		public static final String strictArch = "An architecture not recognized.";
		public static final String strictCopyrightFormatVersion = "A copyright format version not recognized.";
		public static final String strictSection = "A section or area name not recognized.";
		public static final String strictStandardsVersion = "A standards version not recognized.";
		public static final String trailingSpace = "Line that ends with a trailing whitespace.";
		public static final String unknownPackageType = "An unrecognized type is used in a Package-Type field. Currently, the recognized types are 'deb' and 'udeb'. Used in debian/control files.";
		public static final String unknownPriority = "A priority name not recognized.";
		public static final String upstreamContactStyle = "An Upstream-Contact field that is not a single URL address or a Maintainer-style contact. Used in debian/copyright files.";
		public static final String upstreamVersionStyle = "An upstream version using invalid syntax.";
		public static final String urgencyDescriptionParentheses = "Commentary in an Urgency field that is not wrapped in parentheses. Used in .changes files.";
		public static final String url = "A URL using an invalid format or unknown schema.";
		public static final String urlExists = "A URL address that is not reachable.";
		public static final String urlForceHttps = "A URL address not using the HTTPS protocol.";
		public static final String vcsBranch = "A VCS field that does not declare a branch when it should.";
		public static final String versionStyle = "A debian-compatible version not using the proper format.";
	}
}