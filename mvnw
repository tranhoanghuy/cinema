#!/bin/sh
# Licensed to the Apache Software Foundation (ASF)
set -e

# Determine the script directory
if [ -n "$BASH_VERSION" ] ; then
  # If running under bash, use BASH_SOURCE[0]
  THIS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
else
  THIS_DIR="$(cd "$(dirname "$0")" && pwd)"
fi

MAVEN_PROJECTBASEDIR="$THIS_DIR"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
MAVEN_HOME="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/apache-maven"

# Extract distributionUrl from properties
DISTRIBUTION_URL=$(grep distributionUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d= -f2)

# If maven-wrapper.jar does not exist, download it
if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  WRAPPER_URL=$(grep wrapperUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d= -f2)
  wget -q -O "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null \
    || curl -q -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null
fi

exec java \
  -cp "$MAVEN_WRAPPER_JAR" \
  "-Dmaven.home=$MAVEN_HOME" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
