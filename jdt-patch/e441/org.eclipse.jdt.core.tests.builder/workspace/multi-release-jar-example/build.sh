#!/usr/bin/env bash

# ============================================================================
# Build script for multi-release-example.jar
# This script compiles Java sources for multiple versions and packages them
# into a multi-release JAR file.
# ============================================================================

set -u

echo "============================================================================"
echo "Building Multi-Release JAR Example"
echo "============================================================================"
echo

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASE_DIR" || exit 1

if ! command -v javac >/dev/null 2>&1; then
    echo "ERROR: javac not found in PATH"
    echo "Please ensure JDK is installed and JAVA_HOME is set"
    exit 1
fi

echo "Using Java compiler:"
javac -version
echo

echo "Cleaning previous build outputs..."
rm -rf bin8 bin9 bin11 bin17 bin21 bin25 temp_jar
rm -f multi-release-example.jar

mkdir -p bin8 bin9 bin11 bin17 bin21 bin25 temp_jar

echo "============================================================================"
echo "Compiling Java 8 base version..."
echo "============================================================================"
if ! javac --release 8 -d bin8 src/java8/com/example/*.java; then
    echo "ERROR: Failed to compile Java 8 sources"
    exit 1
fi
echo "Java 8 compilation successful"
echo

echo "============================================================================"
echo "Compiling Java 9 module-info..."
echo "============================================================================"
if ! javac --release 9 -d bin9 src/java9/module-info.java; then
    echo "ERROR: Failed to compile Java 9 module-info"
    exit 1
fi
echo "Java 9 compilation successful"
echo

echo "============================================================================"
echo "Compiling Java 11 version..."
echo "============================================================================"
if ! javac --release 11 -d bin11 src/java11/com/example/*.java; then
    echo "ERROR: Failed to compile Java 11 sources"
    exit 1
fi
echo "Java 11 compilation successful"
echo

echo "============================================================================"
echo "Compiling Java 17 version..."
echo "============================================================================"
if ! javac --release 17 -d bin17 src/java17/com/example/*.java; then
    echo "ERROR: Failed to compile Java 17 sources"
    exit 1
fi
echo "Java 17 compilation successful"
echo

echo "============================================================================"
echo "Compiling Java 21 version..."
echo "============================================================================"
if ! javac --release 21 -d bin21 src/java21/com/example/*.java; then
    echo "ERROR: Failed to compile Java 21 sources"
    exit 1
fi
echo "Java 21 compilation successful"
echo

SKIP_25=0

echo "============================================================================"
echo "Compiling Java 25 version..."
echo "============================================================================"
if ! javac --release 25 -d bin25 src/java25/com/example/*.java; then
    echo "WARNING: Failed to compile Java 25 sources"
    echo "This is expected if your JDK doesn't support Java 25"
    echo "Continuing without Java 25 support..."
    rm -rf bin25
    SKIP_25=1
else
    echo "Java 25 compilation successful"
fi
echo

echo "============================================================================"
echo "Preparing JAR structure..."
echo "============================================================================"

if ! cp -R bin8/. temp_jar/; then
    echo "ERROR: Failed to copy Java 8 classes"
    exit 1
fi

if ! cp -R bin9/. temp_jar/; then
    echo "ERROR: Failed to copy Java 9 module-info"
    exit 1
fi

mkdir -p temp_jar/META-INF/versions/11
if ! cp -R bin11/. temp_jar/META-INF/versions/11/; then
    echo "ERROR: Failed to copy Java 11 classes"
    exit 1
fi

mkdir -p temp_jar/META-INF/versions/17
if ! cp -R bin17/. temp_jar/META-INF/versions/17/; then
    echo "ERROR: Failed to copy Java 17 classes"
    exit 1
fi

mkdir -p temp_jar/META-INF/versions/21
if ! cp -R bin21/. temp_jar/META-INF/versions/21/; then
    echo "ERROR: Failed to copy Java 21 classes"
    exit 1
fi

if [ "$SKIP_25" -eq 0 ]; then
    mkdir -p temp_jar/META-INF/versions/25
    if ! cp -R bin25/. temp_jar/META-INF/versions/25/; then
        echo "ERROR: Failed to copy Java 25 classes"
        exit 1
    fi
fi

mkdir -p temp_jar/META-INF
if ! cp META-INF/MANIFEST.MF temp_jar/META-INF/MANIFEST.MF; then
    echo "ERROR: Failed to copy manifest"
    exit 1
fi

echo "Preparation complete"
echo

echo "============================================================================"
echo "Creating multi-release JAR..."
echo "============================================================================"

if ! (
    cd temp_jar && jar cfm ../multi-release-example.jar META-INF/MANIFEST.MF .
); then
    echo "ERROR: Failed to create JAR file"
    exit 1
fi

echo
echo "============================================================================"
echo "Build successful!"
echo "============================================================================"
echo
echo "Created: multi-release-example.jar"
echo

echo "JAR contents:"
jar tf multi-release-example.jar
echo

echo
echo "Cleaning up temporary files..."
rm -rf temp_jar

echo
echo "============================================================================"
echo "Test the JAR with:"
echo "  java -cp multi-release-example.jar com.example.TestMultiRelease"

echo
java -cp multi-release-example.jar com.example.TestMultiRelease

echo "============================================================================"
echo
