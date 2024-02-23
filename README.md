# debian-control-linter

A flexible debian control file linter supporting `debian/control`, `DEBIAN/control`, `debian/copyright`, `.changes` and `.dsc` files.

**This software is in an alpha state. While it works well for `debian/copyright` files, some of the other formats are largely untested.**

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

Errors can be check in:

- Individual fields
- Across multiple fields in a stanza (e.g. a `Source` field declaring the same version as the `Version` field)
- Across multiple stanzas in the same control file (e.g. checking if more generic file patters come first in a copyright file)

There are a large number of individual checks that can be enabled or disabled. They can be listed using `debian-control-linter --list checks`. There are also check presets available; `normal` and `strict` are the most useful ones for regular users.

Some errors cannot be disabled, not even with the `quirks` preset. Generally, these are parsing errors, or errors that might break other people's scripts.

## Limitations

**Variable substitutions and OpenPGP signatures are not supported.**

As this linter only processes the control file, it is not possible to check for errors across files, such as files without copyright notice or missing entries from a file list.

Error messages are also sometimes ambiguous.

```
License: Unsplash License
```

in a `debian/copyright` file will yield the error `Invalid license exception: Unsplash License`, which is not the most intuitive. (The solution is to use a custom, single-word short name for the license, since license names are not allowed to contain spaces.)

Similarly, if a required field is missing from a stanza, the error message will say

```
Cannot match stanza; possibly missing fields or incorrect stanza order: no. <number>
```

but doesn't say which field is missing, since there may be an arbitrary number of stanza specifications that may match the given text if a new field was added.

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