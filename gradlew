#!/bin/sh

##############################################################################
#
#   Gradle start up script for POSIX
#
##############################################################################

APP_HOME=$( cd "${0%/*}" && pwd -P ) || exit
APP_NAME="Gradle"
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD=$JAVA_HOME/bin/java
else
    JAVACMD=java
fi

exec "$JAVACMD" -Xmx64m -Xms64m \
        -Dorg.gradle.appname=$APP_NAME \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"