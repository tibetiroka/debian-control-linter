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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A single control file, the largest unit the linter can handle.
 * <p>
 * Control files are linted individually. This class is responsible for handling the parsing of control files in general and organizing how stanzas are matched to the specification within it.
 */
public class ControlFile {
	/**
	 * The configuration used for linting this control file.
	 */
	private final Configuration config;
	/**
	 * The list of stanza specifications after they were matched to the parsed stanzas of this file.
	 */
	private final List<StanzaSpec> specs = new ArrayList<>();
	/**
	 * The list of stanzas, after they were parsed from the control file.
	 */
	private final List<Stanza> stanzas = new ArrayList<>();

	/**
	 * Creates a control file with the specified linter configuration.
	 *
	 * @param config The config file
	 */
	public ControlFile(Configuration config) {
		this.config = config.clone();
	}

	/**
	 * Gets the list of stanza specifications that were matched to the stanzas of this file.
	 *
	 * @return An unmodifiable view of the list of specifications
	 */
	public List<StanzaSpec> getSpecs() {
		return Collections.unmodifiableList(specs);
	}

	/**
	 * Gets the list of stanzas parsed from the control file.
	 *
	 * @return An unmodifiable view of the list of stanzas
	 */
	public List<Stanza> getStanzas() {
		return Collections.unmodifiableList(stanzas);
	}

	/**
	 * Lints all matched stanzas in this file. This method must be called after {@link #matchStanzas()}.
	 */
	public void lintStanzas() {
		for(int i = 0; i < specs.size(); i++) {
			Stanza stanza = stanzas.get(i);
			StanzaSpec spec = specs.get(i);
			spec.fields().forEach((name, fieldSpec) -> {
				DataField field = stanza.getField(name);
				if(field != null) {
					if(config.fieldNameCapitalization && !name.equals(field.name())) {
						Main.error("Field name is not properly capitalized: " + field.name(), "fieldNameCapitalization");
					}
					fieldSpec.linter().accept(field.data(), config);
				}
			});
			spec.linter().accept(stanza, config);
		}
		config.checkedType.getLinter().accept(this, config);
	}

	/**
	 * Matches the stanzas of this control file to the expected stanzas, removing erroneous stanzas in the process. Some data fields may have their types changed to fit the standard types. This method must be called after {@link #parse()}.
	 */
	public void matchStanzas() {
		if(!this.specs.isEmpty()) {
			throw new IllegalStateException("Cannot match stanzas: already matched");
		}
		List<StanzaSpec> specs = new ArrayList<>(config.checkedType.getStanzas());
		List<StanzaSpec> usedSpecs = new ArrayList<>();
		for(Stanza stanza : stanzas) {
			ArrayList<StanzaSpec> matching = new ArrayList<>();
			for(StanzaSpec spec : specs) {
				if(spec.mandatory()) {
					if(spec.canMatch(stanza, config)) {
						matching.add(spec);
						break;
					}
					if(usedSpecs.isEmpty() || !usedSpecs.getLast().equals(spec)) {
						break;
					}
				} else {
					if(spec.canMatch(stanza, config)) {
						matching.add(spec);
					}
				}
			}
			if(matching.isEmpty()) {
				Main.error("Cannot match stanza; possibly missing fields or incorrect stanza order: no. " + (usedSpecs.size() + 1));
				StanzaSpec spec = new StanzaSpec("blank stanza", false, false, new HashMap<>(), (a, b) -> {
				});
				usedSpecs.add(spec);
				spec.match(stanza, config);
			} else {
				usedSpecs.add(matching.getLast());
				matching.getLast().match(stanza, config);
				int last = specs.indexOf(matching.getLast());
				for(int i = 0; i < last; i++) {
					specs.removeFirst();
				}
				if(!matching.getLast().repeatable()) {
					specs.removeFirst();
				}
			}
		}
		this.specs.addAll(usedSpecs);
		for(StanzaSpec spec : config.checkedType.getStanzas()) {
			if(spec.mandatory() && !usedSpecs.contains(spec)) {
				Main.error("Missing mandatory stanza: " + spec.name());
			}
		}
	}

	/**
	 * Parses data from the {@link #config stored configuration}'s {@link Configuration#targetFile target file}. Must not be called if the control file was already loaded.
	 */
	public void parse() {
		if(config.targetFile == null || (!config.targetFile.equals(new File("-")) && (!config.targetFile.exists() || !config.targetFile.isFile()))) {
			Main.error("Invalid or missing target file");
		} else {
			try {
				ArrayList<String> lines = new ArrayList<>();
				if(config.targetFile.equals(new File("-")) && !config.targetFile.exists()) {
					try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
						for(String line = reader.readLine(); line != null; line = reader.readLine()) {
							lines.add(line);
						}
					}
				} else {
					lines.addAll(Files.readAllLines(config.targetFile.toPath(), StandardCharsets.UTF_8));
				}
				parse(lines);
			} catch(IOException e) {
				Main.error("Could not read input file: " + e.getMessage());
			}
		}
	}

	@Override
	public String toString() {
		return "ControlFile{" + "stanzas=" + stanzas + ", config=" + config + '}';
	}

	/**
	 * Parses the data from the lines of the target file. Most users should use {@link #parse()} instead.
	 *
	 * @param lines The lines to parse
	 */
	protected void parse(List<String> lines) {
		if(!stanzas.isEmpty()) {
			throw new IllegalStateException("Cannot parse control file: there is already content parsed");
		}
		lines = new ArrayList<>(lines);
		if(lines.removeIf(line -> line.startsWith("#"))) {
			if(config.comments && config.checkedType != ControlType.SOURCE_PACKAGE_CONTROL) {
				Main.error("Comments are only allowed in debian/control files", "comments", "https://www.debian.org/doc/debian-policy/ch-controlfields#syntax-of-control-files");
			}
		}
		if(config.trailingSpace) {
			Pattern trailingSpace = Pattern.compile("[ \\t]$");
			lines.forEach(s -> {
				if(trailingSpace.matcher(s).matches()) {
					Main.error("Line has trailing whitespace: " + s.strip());
				}
			});
		}
		Pattern empty = Pattern.compile("^[ \\t]+$");
		while(!lines.isEmpty()) {
			while(!lines.isEmpty() && empty.matcher(lines.getFirst()).matches()) {
				if(!lines.removeFirst().isEmpty() && config.emptyStanzaSeparators) {
					Main.error("Stanza separator contains whitespaces: should be empty", "emptyStanzaSeparators", "https://www.debian.org/doc/debian-policy/ch-controlfields#syntax-of-control-files");
				}
			}
			if(!lines.isEmpty()) {
				Stanza s = Stanza.parseNext(lines, config);
				if(s != null) {
					stanzas.add(s);
				} else {
					break;
				}
			}
		}
	}
}