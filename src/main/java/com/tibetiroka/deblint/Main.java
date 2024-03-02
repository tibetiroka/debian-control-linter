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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiPredicate;

public class Main {
	/**
	 * The current version of the project
	 */
	public static final String VERSION = "1.0.0";
	/**
	 * Stores whether the app is running inside a native image.
	 */
	public static boolean IS_NATIVE_IMAGE = isNativeImage();
	/**
	 * Stores whether the app is in testing mode.
	 */
	public static boolean IS_TEST = isJUnitTest();
	/**
	 * Whether to display the check that generated an error.
	 */
	private static boolean DISPLAY_CHECK = false;
	/**
	 * Whether to display a reference to the standard that describes the error.
	 */
	private static boolean DISPLAY_REFERENCE = false;
	/**
	 * The number of errors generated during linting.
	 */
	private static int ERROR_COUNT = 0;

	/**
	 * Displays an error message, incrementing {@link #ERROR_COUNT}. During testing, throws an {@link IllegalArgumentException}.
	 *
	 * @param error The error message
	 * @throws IllegalArgumentException If testing
	 */
	public static void error(String error) {
		error(error, null, null);
	}

	/**
	 * Displays an error message, incrementing {@link #ERROR_COUNT}. During testing, throws an {@link IllegalArgumentException}.
	 *
	 * @param error The error message
	 * @param check The name of the check that generated the error
	 * @throws IllegalArgumentException If testing
	 */
	public static void error(String error, String check) {
		error(error, check, null);
	}

	/**
	 * Displays an error message, incrementing {@link #ERROR_COUNT}. During testing, throws an {@link IllegalArgumentException}.
	 *
	 * @param error     The error message
	 * @param check     The name of the check that generated the error
	 * @param reference The error's description in the standard
	 * @throws IllegalArgumentException If testing
	 */
	public static void error(String error, String check, String reference) {
		ERROR_COUNT++;
		if(IS_TEST) {
			if(check != null) {
				if(Configuration.getChecks().stream().noneMatch(f -> f.getName().equals(check))) {
					throw new RuntimeException("Invalid check: " + check);
				}
			}
			throw new IllegalArgumentException(error);
		} else {
			StringBuilder sb = new StringBuilder("Error: ");
			if(DISPLAY_CHECK && check != null) {
				sb.append('[').append(check).append("] ");
			}
			sb.append(error);
			if(DISPLAY_REFERENCE && reference != null) {
				sb.append(" <").append(reference).append('>');
			}
			System.out.println(sb);
		}
	}

	/**
	 * Gets the number of errors generated.
	 *
	 * @return {@link #ERROR_COUNT}
	 */
	public static int getErrorCount() {
		return ERROR_COUNT;
	}

	/**
	 * Prints an informative message. Shows no output during testing.
	 *
	 * @param information The message to print
	 */
	public static void info(String information) {
		if(!IS_TEST) {
			System.out.println(information);
		}
	}

	public static void main(String[] args) {
		Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
			e.printStackTrace();
			System.exit(3);
		});
		//
		List<String> argList = Arrays.asList(args);
		if(argList.contains("--help") || argList.contains("-h")) {
			printHelp();
		} else if(argList.contains("--version") || argList.contains("-v")) {
			printVersion();
		}
		HashMap<String, List<String>> parameters = new HashMap<>();
		for(int i = 0; i < args.length - 1; i += 2) {
			parameters.computeIfAbsent(args[i], s -> new ArrayList<>());
			parameters.get(args[i]).add(args[i + 1]);
		}
		if(args.length % 2 == 1) {
			parameters.computeIfAbsent("--file", s -> new ArrayList<>());
			parameters.get("--file").add(args[args.length - 1]);
		}
		Configuration config = processParameters(parameters);
		//
		lint(config);
		if(ERROR_COUNT != 0) {
			System.exit(1);
		}
	}

	/**
	 * Prints a warning message. During testing, throws an {@link IllegalStateException} with the same message.
	 *
	 * @param warning The warning to display
	 * @throws IllegalStateException If testing
	 */
	public static void warn(String warning) {
		if(IS_TEST) {
			throw new IllegalStateException(warning);
		} else {
			System.out.println("Warning: " + warning);
		}
	}

	/**
	 * Runs the linter with the specific configuration.
	 *
	 * @param config The linter configuration
	 */
	protected static void lint(Configuration config) {
		try {
			ControlFile file = new ControlFile(config);
			file.parse();
			file.matchStanzas();
			file.lintStanzas();
		} catch(Exception e) {
			if(IS_TEST) {
				throw new RuntimeException(e);
			}
			error("Error during linting: " + e.getMessage());
			System.exit(2);
		}
	}

	/**
	 * Checks whether the application is being run from a JUnit test.
	 *
	 * @return True if called from JUnit
	 */
	private static boolean isJUnitTest() {
		for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
			if(element.getClassName().startsWith("org.junit.")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the application is running inside a GraalVM native image.
	 *
	 * @return True if native image
	 */
	private static boolean isNativeImage() {
		return "executable".equals(System.getProperty("org.graalvm.nativeimage.kind"));
	}

	/**
	 * Prints a help message, then exits.
	 */
	private static void printHelp() {
		// don't forget to edit the man page
		info("""
				     Debian control file linter by tibetiroka
				     Usage: debian-control-linter [OPTION] [FILE]
				     		
				     Arguments:
				      [FILE]
				     		The file to lint; can also be supplied via a '--file' option. Each control file type also has a default file associated with it that is used if no file is specified. If the file is '-' and it doesn't exist, read from standard input.
				     					
				     Options:
				      -h, --help
				     		Show this help message and exit.
				      -v, --version
				     		Print version information and exit.
				      -l, --list <name>
				     		Lists all options belonging to a specific list. The supported values can be queried via '--list lists'.
				      -p, --preset <name>
				     		Applies all configurations in a specific preset. Presets can be listed with '--list presets'. The list of configuration options enabled by each preset can be acquired via '--preset-info <preset>'. The default preset is 'normal'. Preset names are case-insensitive.
				      -t, --type <name>
				     		Specifies the type of the control file to lint. The list of supported types can be queried via '--list types'. Information on these types can be acquired via '--type-info <type>'. The default type is 'debian/copyright'. Type names are case-sensitive.
				      --file <path>
				     		Specifies the file to lint. This file can also be specified by an argument after all of the options. All types also have a default file. If the file is '-' and it doesn't exist, read from standard input.
				      --enable <check[,check]...>
				     		Enables a specific check. The list of checks can be queried via '--list checks'. Regardless of the order they are specified in, presets are always processed before individual check toggles. Information on these checks can be acquired via '--check-info <check>'. Check names are case-insensitive.
				      --disable <check[,check]...>
				     		Disables a specific check. For more details, see '--enable'.
				      --preset-info <preset>
				     		Prints information about a specific preset, including a short description and the list of checks it enables.
				      --type-info <type>
				     		Prints information about a specific control file type, including a short description and the default file name.
				      --check-info <check>
				     		Prints information about a specific check.
				      --display <check|reference|both|neither>
				     		Configures how error messages are displayed. Check names are displayed in square brackets before the error text, and references to the standard are displayed in angle brackets after the error body. The default value is 'neither'. Display values are case-insensitive.
				      """);
		System.exit(0);
	}

	/**
	 * Prints a version message, then exits.
	 */
	private static void printVersion() {
		info("Debian control file linter by tibetiroka, version " + VERSION);
		System.exit(0);
	}

	/**
	 * Processes the map of command-line parameters given to the program.
	 *
	 * @param parameters The map of parameters; the key is the parameter name, the values are the data given for each occurrence of the parameter
	 * @return A {@link Configuration} with the requested settings
	 */
	private static Configuration processParameters(Map<String, List<String>> parameters) {
		final Configuration[] config = {Configuration.PRESET_NORMAL};
		ArrayList<BiPredicate<String, String>> processors = new ArrayList<>();
		{
			processors.add((param, value) -> {
				if(param.equals("--list") || param.equals("-l")) {
					switch(value) {
						case "presets" -> Configuration.getPresets().forEach(f -> info(f.getName().substring("PRESET_".length()).toLowerCase()));
						case "checks" -> Configuration.getChecks().forEach(f -> info(f.getName()));
						case "types" -> {
							for(ControlType type : ControlType.values()) {
								info(type.getTypeName());
							}
						}
						case "lists" -> {
							String[] values = {"presets", "checks", "types", "lists"};
							for(String s : values) {
								info(s);
							}
						}
						default -> warn("Invalid item to list: " + value);
					}
					System.exit(0);
					return true;
				}
				return false;
			});
			processors.add((param, value) -> {
				if(param.equals("--preset-info")) {
					String presetName = "PRESET_" + value.toUpperCase();
					try {
						Field f = Configuration.getPresets().stream().filter(p -> p.getName().equals(presetName)).findAny().get();
						Configuration preset = (Configuration) f.get(null);
						Configuration lesserPreset = preset == Configuration.getPrecedenceList().getFirst() ? null : Configuration.getPrecedenceList().get(Configuration.getPrecedenceList().indexOf(preset) - 1);
						info(preset.presetDescription);
						info("Configuration values enabled for preset:");
						for(Field check : Configuration.getChecks()) {
							if((Boolean) check.get(preset)) {
								if(lesserPreset == null || !(Boolean) check.get(lesserPreset)) {
									info(check.getName() + " [!]");
								} else {
									info(check.getName());
								}
							}
						}
					} catch(ReflectiveOperationException | NoSuchElementException ex) {
						warn("Unknown preset: " + value);
					}
					System.exit(0);
					return true;
				}
				return false;
			});
			processors.add((param, value) -> {
				if(param.equals("--type-info")) {
					boolean found = false;
					for(ControlType type : ControlType.values()) {
						if(type.getTypeName().equals(value)) {
							found = true;
							info(type.getTypeName());
							info(type.getDescription());
							info("Default file: " + type.getDefaultFile());
							break;
						}
					}
					if(!found) {
						warn("Invalid type: " + value);
					}
					System.exit(0);
					return true;
				}
				return false;
			});
			processors.add((param, value) -> {
				if(param.equals("--check-info")) {
					try {
						Field fi = Arrays.stream(Configuration.ConfigOptionDetails.class.getDeclaredFields())
						                 .filter(f -> f.getType() == String.class)
						                 .filter(f -> f.getName().equalsIgnoreCase(value))
						                 .findAny()
						                 .get();
						info((String) fi.get(null));
						info("Presets enabling this check by default:");
						for(Field field : Configuration.class.getDeclaredFields()) {
							if(Modifier.isStatic((field.getModifiers())) && field.getName().startsWith("PRESET_")) {
								Configuration preset = (Configuration) field.get(null);
								Field check = Arrays.stream(Configuration.class.getDeclaredFields()).filter(f -> f.getName().equalsIgnoreCase(value)).findAny().get();
								if((Boolean) check.get(preset)) {
									info(field.getName().substring("PRESET_".length()).toLowerCase());
								}
							}
						}
					} catch(NoSuchElementException | IllegalAccessException ex) {
						warn("Unknown check: " + value);
					}
					System.exit(0);
					return true;
				}
				return false;
			});
			processors.add((param, value) -> {
				if(param.equals("--preset") || param.equals("-p")) {
					String preset = "PRESET_" + value.toUpperCase();
					try {
						Field f = Configuration.class.getDeclaredField(preset);
						config[0] = (Configuration) f.get(null);
					} catch(ReflectiveOperationException ex) {
						warn("Unknown preset: " + value);
					}
					return true;
				}
				return false;
			});
			processors.add((param, value) -> {
				if(param.equals("--enable") || param.equals("--disable")) {
					boolean enable = param.equals("--enable");
					String[] checks = value.split(",");
					for(String check : checks) {
						String trimmedOption = check.trim();
						if(!check.isEmpty()) {
							try {
								Field field = Arrays.stream(Configuration.class.getDeclaredFields())
								                    .filter(f -> f.getType() == boolean.class)
								                    .filter(f -> f.getName().equalsIgnoreCase(trimmedOption))
								                    .findAny()
								                    .get();
								field.set(config[0], enable);
							} catch(NoSuchElementException | IllegalAccessException ex) {
								warn("Cannot set unknown check: " + check);
							}
						}
					}
					return true;
				}
				return false;
			});
			processors.add((param, value) -> {
				if(param.equals("--type")) {
					boolean found = false;
					for(ControlType controlType : ControlType.values()) {
						if(controlType.getTypeName().equals(value)) {
							found = true;
							config[0].checkedType = controlType;
							break;
						}
					}
					if(!found) {
						warn("Invalid type: " + value);
					}
					return true;
				}
				return false;
			});
			processors.add((param, value) -> {
				if(param.equals("--file")) {
					config[0].targetFile = new File(value);
					return true;
				} else {
					return false;
				}
			});
			processors.add((param, value) -> {
				if(param.equals("--display")) {
					switch(value.toLowerCase()) {
						case "check" -> {
							DISPLAY_CHECK = true;
							DISPLAY_REFERENCE = false;
						}
						case "reference" -> {
							DISPLAY_CHECK = false;
							DISPLAY_REFERENCE = true;
						}
						case "neither" -> {
							DISPLAY_CHECK = false;
							DISPLAY_REFERENCE = false;
						}
						case "both" -> {
							DISPLAY_CHECK = true;
							DISPLAY_REFERENCE = true;
						}
						default -> Main.warn("Unknown value for --display: " + value);
					}
					return true;
				}
				return false;
			});
		}
		for(BiPredicate<String, String> processor : processors) {
			parameters.entrySet().removeIf(entry -> {
				entry.getValue().removeIf(v -> processor.test(entry.getKey(), v));
				return entry.getValue().isEmpty();
			});
		}
		parameters.forEach((key, value) -> warn("Unknown option: " + key));
		config[0].apply();
		return config[0];
	}
}