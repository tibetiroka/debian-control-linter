# debian-control-linter

A flexible debian control file linter supporting `debian/control`, `DEBIAN/control`, `debian/copyright`, `.changes` and `.dsc` files.

## Usage

```bash
debian-control-linter --type <type> path/to/file
```

You can also specify various presets:

```bash
debian-control-linter --preset strict --type <type> path/to/file
```

Or toggle checks manually:

```bash
debian-control-linter --enable recommendedFields,spaceAfterColon,urlForceHttps path/to/file
```

There are more options and checks available; for an up-to-date list, please check

```bash
debian-control-linter --help
```

or the provided man page.

## Features

Any file following the debian control file format can be parsed into a series of stanzas containing data fields. For the supported formats, these parsed stanzas are matched with the specification and, if successful, are then checked for errors.

Errors can be checked in:

- Individual fields
- Across multiple fields in a stanza (e.g. a `Source` field declaring the same version as the `Version` field)
- Across multiple stanzas in the same control file (e.g. checking if more generic file patters come first in a copyright file)

There are a large number of individual checks that can be enabled or disabled. They can be listed using `debian-control-linter --list checks`. There are also check presets available; `normal` and `strict` are the most useful ones for regular users.

Some errors cannot be disabled, not even with the `quirks` preset. Generally, these are parsing errors, or errors that might break other people's scripts.

## Limitations

**Variable substitutions are not supported.**

As this linter only processes the control file, it is not possible to check for errors across files, such as files without copyright notice or missing entries from a file list.

URLs using a scheme other than HTTP/HTTPS may be misreported as invalid.

## System Requirements and Building

This project is written in Java 21, which is required for building the project (and for running the jar package). It is not required when using the native executable.

To build this project with Maven, do

```bash
mvn package
```

This will produce the `sources`, `javadoc` and the library jar files. For a runnable jar (with all dependencies included), use the `portable` profile. The native executable can be compiled via the `native` profile, which requires GraalVM 21 to be set up on your system. To generate all project files, use

```bash
mvn package -P native,portable
```

For development, you can also enable debug mode to greatly cut compile times for the native image. (Using the native image for development is not necessary, but is made a lot easier with this option.)

```bash
mvn package -Ddebug=true -Pnative
```