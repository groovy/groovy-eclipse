@echo off
REM ============================================================================
REM Build script for multi-release-example.jar
REM This script compiles Java sources for multiple versions and packages them
REM into a multi-release JAR file.
REM ============================================================================

setlocal EnableDelayedExpansion

echo ============================================================================
echo Building Multi-Release JAR Example
echo ============================================================================
echo.

REM Set base directory
set BASE_DIR=%~dp0
cd /d "%BASE_DIR%"

REM Check if javac is available
where javac >nul 2>&1
if errorlevel 1 (
    echo ERROR: javac not found in PATH
    echo Please ensure JDK is installed and JAVA_HOME is set
    exit /b 1
)

REM Display Java version
echo Using Java compiler:
javac -version
echo.

REM Clean previous build outputs
echo Cleaning previous build outputs...
if exist bin8 rmdir /s /q bin8
if exist bin11 rmdir /s /q bin11
if exist bin17 rmdir /s /q bin17
if exist bin21 rmdir /s /q bin21
if exist bin25 rmdir /s /q bin25
if exist temp_jar rmdir /s /q temp_jar
if exist multi-release-example.jar del /q multi-release-example.jar

REM Create output directories
mkdir bin8
mkdir bin11
mkdir bin17
mkdir bin21
mkdir bin25
mkdir temp_jar

echo ============================================================================
echo Compiling Java 8 base version...
echo ============================================================================
javac --release 8 -d bin8 src\java8\com\example\*.java
if errorlevel 1 (
    echo ERROR: Failed to compile Java 8 sources
    exit /b 1
)
echo Java 8 compilation successful
echo.

echo ============================================================================
echo Compiling Java 11 version...
echo ============================================================================
javac --release 11 -d bin11 src\java11\com\example\*.java
if errorlevel 1 (
    echo ERROR: Failed to compile Java 11 sources
    exit /b 1
)
echo Java 11 compilation successful
echo.

echo ============================================================================
echo Compiling Java 17 version...
echo ============================================================================
javac --release 17 -d bin17 src\java17\com\example\*.java
if errorlevel 1 (
    echo ERROR: Failed to compile Java 17 sources
    exit /b 1
)
echo Java 17 compilation successful
echo.

echo ============================================================================
echo Compiling Java 21 version...
echo ============================================================================
javac --release 21 -d bin21 src\java21\com\example\*.java
if errorlevel 1 (
    echo ERROR: Failed to compile Java 21 sources
    exit /b 1
)
echo Java 21 compilation successful
echo.

echo ============================================================================
echo Compiling Java 25 version...
echo ============================================================================
javac --release 25 -d bin25 src\java25\com\example\*.java
if errorlevel 1 (
    echo WARNING: Failed to compile Java 25 sources
    echo This is expected if your JDK doesn't support Java 25
    echo Continuing without Java 25 support...
    rmdir /s /q bin25
    set SKIP_25=1
) else (
    echo Java 25 compilation successful
)
echo.

echo ============================================================================
echo Preparing JAR structure...
echo ============================================================================

REM Copy Java 8 base classes
xcopy /e /i /y bin8\* temp_jar\
if errorlevel 1 (
    echo ERROR: Failed to copy Java 8 classes
    exit /b 1
)

REM Create versioned directories and copy classes
mkdir temp_jar\META-INF\versions\11
xcopy /e /i /y bin11\* temp_jar\META-INF\versions\11\
if errorlevel 1 (
    echo ERROR: Failed to copy Java 11 classes
    exit /b 1
)

mkdir temp_jar\META-INF\versions\17
xcopy /e /i /y bin17\* temp_jar\META-INF\versions\17\
if errorlevel 1 (
    echo ERROR: Failed to copy Java 17 classes
    exit /b 1
)

mkdir temp_jar\META-INF\versions\21
xcopy /e /i /y bin21\* temp_jar\META-INF\versions\21\
if errorlevel 1 (
    echo ERROR: Failed to copy Java 21 classes
    exit /b 1
)

if not defined SKIP_25 (
    mkdir temp_jar\META-INF\versions\25
    xcopy /e /i /y bin25\* temp_jar\META-INF\versions\25\
    if errorlevel 1 (
        echo ERROR: Failed to copy Java 25 classes
        exit /b 1
    )
)

REM Copy manifest
copy /y META-INF\MANIFEST.MF temp_jar\META-INF\MANIFEST.MF
if errorlevel 1 (
    echo ERROR: Failed to copy manifest
    exit /b 1
)

echo Preparation complete
echo.

echo ============================================================================
echo Creating multi-release JAR...
echo ============================================================================

cd temp_jar
jar cfm ..\multi-release-example.jar META-INF\MANIFEST.MF .
if errorlevel 1 (
    cd ..
    echo ERROR: Failed to create JAR file
    exit /b 1
)
cd ..

echo.
echo ============================================================================
echo Build successful!
echo ============================================================================
echo.
echo Created: multi-release-example.jar
echo.

REM Display JAR contents
echo JAR contents:
jar tf multi-release-example.jar
echo.

echo.
echo Cleaning up temporary files...
rmdir /s /q temp_jar

echo.
echo ============================================================================
echo Test the JAR with:
echo   java -cp multi-release-example.jar com.example.TestMultiRelease

java -cp multi-release-example.jar com.example.TestMultiRelease

echo ============================================================================
echo.

endlocal
