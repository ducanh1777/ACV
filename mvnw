#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Maven2 Start Up Shell script
#
# Required ENV vars:
# JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# MAVEN_OPTS - parameters passed to the Java VM when running Maven
#     e.g. to debug Maven itself, use
# export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"
# ----------------------------------------------------------------------------

die() {
    echo
    echo "$*"
    echo
    exit 1
}

# Check for JAVA_HOME
if [ -n "$JAVA_HOME" ] ; then
    JAVA_EXE="$JAVA_HOME/bin/java"
else
    JAVA_EXE="java"
fi

# Check if java is available
"$JAVA_EXE" -version >/dev/null 2>&1 || die "Error: Java not found. Please ensure Java is installed and in your PATH."

# Determine the Maven version from the distribution URL.
MAVEN_PROJECTBASEDIR=$(pwd)
DISTRIBUTION_URL=$(grep "distributionUrl" "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | cut -d'=' -f2)
MAVEN_VERSION="3.9.6"

MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-$MAVEN_VERSION-bin/apache-maven-$MAVEN_VERSION"

if [ ! -d "$MAVEN_HOME" ]; then
    echo "Maven Wrapper: Maven not found at $MAVEN_HOME."
    echo "I will try to download it for you."
    
    # Simple download using curl or wget
    mkdir -p "$(dirname "$MAVEN_HOME")"
    TEMP_ZIP=$(mktemp).zip
    
    if command -v curl >/dev/null 2>&1; then
        curl -sL "$DISTRIBUTION_URL" -o "$TEMP_ZIP" || die "Error: Failed to download Maven."
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$DISTRIBUTION_URL" -O "$TEMP_ZIP" || die "Error: Failed to download Maven."
    else
        die "Error: curl or wget not found. Please install one of them to download Maven."
    fi
    
    unzip -q "$TEMP_ZIP" -d "$(dirname "$MAVEN_HOME")" || die "Error: Failed to unzip Maven."
    rm "$TEMP_ZIP"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
