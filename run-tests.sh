#!/usr/bin/env sh
# Compiles the app and runs every test with Maven. Needs JDK 21+ and Maven 3.9+.
set -e
cd "$(dirname "$0")"
mvn -B -q test "$@"
echo "All tests passed. Reports: target/surefire-reports"
