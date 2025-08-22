#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Attempt to find JAVACMD from JAVA_HOME, PATH, or standard locations
if [ -n "${JAVA_HOME}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
    JAVACMD="${JAVA_HOME}/bin/java"
elif [ -n "$(command -v java)" ]; then
    JAVACMD="java"
else
    echo "ERROR: Java is not installed or not in your PATH." >&2
    exit 1
fi

# Standard Gradle wrapper script
APP_HOME="$(cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)"
APP_NAME="Gradle"
APP_BASE_NAME="$(basename "$0")"

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if [ -n "${CYGWIN}" ]; then
    [ -n "${APP_HOME}" ] && APP_HOME="$(cygpath --unix "${APP_HOME}")"
    [ -n "${JAVA_HOME}" ] && JAVA_HOME="$(cygpath --unix "${JAVA_HOME}")"
fi

# Determine the Java command to use to start the JVM.
if [ -n "${JAVACMD}" ] ; then
    RUN_JAVA="${JAVACMD}"
else
    RUN_JAVA="${JAVA_HOME}/bin/java"
fi

# Set script variables
GRADLE_OPTS="${GRADLE_OPTS:-}"
CLASSPATH="${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"

# Execute Gradle
exec "${RUN_JAVA}" ${DEFAULT_JVM_OPTS} ${JAVA_OPTS} ${GRADLE_OPTS} -classpath "${CLASSPATH}" org.gradle.wrapper.GradleWrapperMain "$@"
