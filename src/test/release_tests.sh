#!/usr/bin/env bash

# Runs tests before a release. This must be run from the top-level directory of the project.
# These tests are time sensitive and require internet connectivity.
# Must be run in a GNU/Linux environment.

release_version=`echo "$1" | sed 's/^v//'`
error_count='0'

# Verify that the manpage has the correct release date.
release_date=`head -n1 'debian-control-linter.1' | cut -f 4-6 -d ' ' | sed 's/"//g'`
current_date=`LANG=en_US date -u '+%-d %b %Y'`

if [ "$release_date" != "$current_date" ]; then
	echo "Invalid release date in manpage: current date is ${current_date}, found ${release_date}"
	((error_count++))
fi

# Verify that there is no release with the current version.
# Versions are stored without a 'v' prefix, and with all trailing .0 removed.
version=`head -n1 debian-control-linter.1 | cut -f 8 -d ' ' | sed 's/"//g' | sed 's/(.0)+$//'`
remote_tags=`git ls-remote --tags https://github.com/tibetiroka/debian-control-linter.git | cut -d '	' -f 2 | cut -d '/' -f 3  | sed 's/^v//' | sed 's/(.0)+$//'`
releases_matching=`echo "${remote_tags}" | grep "^${version}$" | wc -l`
if [ "$releases_matching" -gt '0' ]; then
	echo "Invalid release version: this release already exists"
	((error_count++))
fi
if [ "$release_version" != "$version" ]; then
	echo "Invalid release version: doesn't match the specified version"
	((error_count++))
fi

# Verify that the version specified is the latest
latest=`echo -e "${version}\\n${remote_tags}" | sort -t '.' -k1,1n -k2,2n -k3,3n | tail -n1`
if [ "$latest" != "$version" ]; then
	echo "Invalid release version: there is a newer release"
	((error_count++))
fi

# Fail if there are errors
if [ "$error_count" -gt 0 ]; then
	echo "$error_count errors found."
	exit 1
fi
exit 0