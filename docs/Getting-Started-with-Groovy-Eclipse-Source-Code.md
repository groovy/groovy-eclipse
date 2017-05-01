Getting Started with Groovy-Eclipse Source Code
===============================================

1. [Communication](#communication)
1. [Development Environment Setup](#development-environment-setup)
1. [Get Source Code](#get-source-code)
1. [Eclipse Project Descriptions](#eclipse-project-descriptions)
1. [Build with Eclipse](#build-with-eclipse)
1. [Test with Eclipse](#test-with-eclipse)
1. [Release Build with Maven](#release-build-with-maven)
1. [Contribute](#contribute)
1. [Debugging 'groovyc' Command](#debugging-groovyc-command)

## Communication

There is currently no mailing list or official forum dedicated to Groovy-Eclipse development. Bug reports, feature enhancement requests, and development questions can be filed in [the Issues section of the Groovy-Eclipse Github repo](https://github.com/groovy/groovy-eclipse/issues).

Additional questions and answers might be found by searching for [the groovy-eclipse tag on stack overflow](http://stackoverflow.com/questions/tagged/groovy-eclipse) and in the [Groovy Users group](https://plus.google.com/communities/105160926044623621768).

## Development Environment Setup

### Get the Java Development Kit

Download JDK 7 from http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html, and install it. This is the minimum version Groovy-Eclipse supports.

Download JDK 8 from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html, and install it. This is the version required to run Eclipse Neon, and should be set as your default system Java run-time.

### Get the Base Eclipse Package

Groovy-Eclipse is intended to be developed using Eclipse. Download and install [the "Eclipse IDE for Eclipse Committers" package (4.6 Neon)](http://www.eclipse.org/downloads/packages/eclipse-ide-eclipse-committers/neonr) for your operating system.

### Install Groovy-Eclipse

On the Eclipse menu bar, select *Help -> Install New Software...*.  In the *Install* window, paste the Groovy-Eclipse update site `http://dist.springsource.org/snapshot/GRECLIPSE/e4.6/` into the *Work with:* field and press enter. Check the boxes next to "Groovy-Eclipse" and "Extra Groovy compilers", then click the *Next* button a couple of times, accept the license agreement, and let Eclipse restart.

### Setup Groovy-Eclipse Compiler

On the Eclipse menu bar, select *Window -> Preferences*.  In the *Preferences* window, go to *Groovy -> Compiler*. Click the *Switch to 2.1.9* button, and let Eclipse restart.

### Install Execution Environment Descriptions

On the Eclipse menu bar, select *Help -> Install New Software...*.  In the *Install* window, select `Neon - http://download.eclipse.org/releases/neon` from the *Work with:* field and enter "exeuction" in the filter. Check the boxes next to "API Tools Execution Environment Descriptions", and click the *Next* button a couple of times, accept the license agreement, and let Eclipse restart.

![API Baseline](images/eclipse-api-tools-execution-environment.png)

### Setup Execution Environment Descriptions

On the Eclipse menu bar, select *Window -> Preferences*.  In the *Preferences* window, go to *Java -> Installed JREs* and add JDK 7. Then go to *Java -> Installed JREs -> Execution Environments*, select JavaSE-1.7, and check the box next to JDK 7 in the list of compatible JREs.

![API Baseline](images/eclipse-execution-environment.png)

### Disable Missing API Baseline Errors

On the Eclipse menu bar, select *Window -> Preferences*.  In the *Preferences* window, go to *Plug-in Development -> API Baselines*. Select either *Ignore* or *Warning* instead of *Error* in the *Missing API Baseline:* drop-down list.

![API Baseline](images/eclipse-api-baseline.png)

## Get Source Code

The source code can be obtained by cloning the repository:

```
https://github.com/groovy/groovy-eclipse.git
```

Import the projects that you want to work with into your workspace.  If you do not know how to clone a repository or import projects from a repository, refer to the [EGit User Guide](https://wiki.eclipse.org/EGit/User_Guide).

Typically not all projects need to be imported as some of them are optional parts of Groovy support for Eclipse.  See the [Build with Eclipse section](#build-with-eclipse) for a list of projects to start with.

## Eclipse Project Descriptions

Groovy-Eclipse is a Groovy language tooling (similar to JDT, CDT eclipse components) integrated into Eclipse. GroovyEclipse source code is a set of Eclipse plug-in projects, where each project is contributing Groovy tooling logic into Eclipse via various Eclipse extension points. For example, Groovy-specific launch configurations, compiler, debugger, editor for .groovy files, etc... Each plug-in project is responsible for some specific feature of the Groovy language tooling support in Eclipse.

### org.eclipse.jdt.core

This project is a patched version of the Eclipse JDT plug-in that hooks core low-level support for the Groovy language into JDT, since JDT does not provide extension support for this kind of integration. See the LanguageSupportFactory class references (and other classes from 'groovy' folder under this project) in the project to locate integration points. Issues noticed in Java tooling may sometimes be caused by Groovy-Eclipse because of this project.

### org.eclipse.jdt.groovy.core

This project contains Groovy core logic for parser, compiler, type inferencing, type look-ups, etc... Logic here is being hooked to JDT via the org.eclipse.jdt.core plug-in.

### org.codehaus.groovyXX

Implementations of Groovy language, where XX stands for Groovy language version. These projects are the source code for Groovy language and are needed in the workspace to make Groovy-Eclipse compile. One can test Groovy-Eclipse for a specific version of Groovy by keeping the desired Groovy language version project opened and the rest closed. If anyone is tempted to change anything in any of these projects, either talk to the Groovy team folks or [raise a defect against Groovy language](http://www.groovy-lang.org/contribute.html#reporting-issues).

### org.codehaus.groovy.eclipse.compilerResolver

Supports the ability to detect and switch between the available version of Groovy available within the Eclipse. Contains core logic only--no UI.

### org.codehaus.groovy.eclipse.junit.test

Integrates JUnits written in Groovy into Eclipse's JUnit framework.

### org.codehaus.groovy.eclipse.ant

Ant integration for building classes from Groovy code.

### org.codehaus.groovy.eclipse.core

Integration of Groovy core components into Eclipse: compiler, launcher, search, type look up, preferences, etc... No UI contributions, just back-end logic.

### org.codehaus.groovy.eclipse.ui

Various contributions to Eclipse UI components: Preference pages, launch configuration wizard tabs, UI actions, creation wizard for Groovy project, creation wizards for Groovy language artifacts, type browsing, search, etc...

### org.codehaus.groovy.eclipse.refactoring

Feature of Groovy source editor (.groovy files editor). Integration of code refactorings for Groovy language into Eclipse. (Right-click on a Groovy statement and select *Source -> Refactor*.)

### org.codehaus.groovy.eclipse.codeassist.completion

Feature of Groovy source editor (.groovy files editor). Integration of code completions for Groovy code. (<Ctrl> + <Space> behavior for uncompleted Groovy statements.)

### org.codehaus.groovy.eclipse.quickfix

Feature of Groovy source editor (.groovy files editor). Integration of quick fix suggestions for Groovy language. Can either be activated by selecting a statements and pressing <Ctrl> + 1 or by clicking on the error annotation in the .groovy file editor's overview ruler.

### org.codehaus.groovy.eclipse.dsl

Support for Groovy based Domain Specific Language. Provides contents assist, type inferencing, etc... for a DSL defined by a DSL descriptor.

### org.eclipse.jdt.groovy.core.tests.compiler

Groovy compiler tests.

If a snippet of code is not building cleanly, it should be captured as a test case in [GroovySimpleTest.java](https://github.com/groovy/groovy-eclipse/blob/master/base-test/org.eclipse.jdt.groovy.core.tests.compiler/src/org/eclipse/jdt/groovy/core/tests/basic/GroovySimpleTest.java)  For each test, there is either a call to 'runConformTest()' if the snippet should successfully compile and run, or 'runNegativeTest()' if it should not compile (in which case it polices the error messages that will come out). Each test is simply the test code captured as a string and then expected output or expected errors. It is possible to pass multiple source files to these run methods.

### org.eclipse.jdt.groovy.core.tests.builder

Java/Groovy builder tests.

If a project is not building cleanly, it should be captured as a test case in [BasicGroovyBuildTests.java](https://github.com/groovy/groovy-eclipse/blob/master/base-test/org.eclipse.jdt.groovy.core.tests.builder/src/org/eclipse/jdt/core/groovy/tests/builder/BasicGroovyBuildTests.java).  These tests represent a typical build flow running against a project.
These tests let you create a project, then add files to it (groovy or java) and then call
fullBuild/incrementalBuild to simulate what would happen in a real eclipse and then either police
the expected errors or expected output. If you have an issue that says it is failing on an
incremental build (but not a full build) you might be creating a BasicGroovyBuildTest.

### org.codehaus.groovy.alltests

High-level test suites.  (These suites group tests that are located in other test projects.)

## Build with Eclipse

This minimal project set should be open in your workspace:

* org.codehaus.groovy.alltests
* org.codehaus.groovy.eclipse
* org.codehaus.groovy.eclipse.ant
* org.codehaus.groovy.eclipse.astviews
* org.codehaus.groovy.eclipse.codeassist.completion
* org.codehaus.groovy.eclipse.codeassist.completion.test
* org.codehaus.groovy.eclipse.codebrowsing
* org.codehaus.groovy.eclipse.codebrowsing.test
* org.codehaus.groovy.eclipse.compilerResolver
* org.codehaus.groovy.eclipse.core
* org.codehaus.groovy.eclipse.core.test
* org.codehaus.groovy.eclipse.dsl
* org.codehaus.groovy.eclipse.dsl.tests
* org.codehaus.groovy.eclipse.junit.test
* org.codehaus.groovy.eclipse.quickfix
* org.codehaus.groovy.eclipse.quickfix.test
* org.codehaus.groovy.eclipse.refactoring
* org.codehaus.groovy.eclipse.refactoring.test
* org.codehaus.groovy.eclipse.tests
* org.codehaus.groovy.eclipse.ui
* org.codehaus.groovyXX (where XX is the Groovy version you are working with; you should only have one of these open in your workspace at a time)
* org.eclipse.jdt.core (There are multiple projects with this name, so they cannot all be imported into the workspace at once. Import only the one in the folder corresponding to the Eclipse version you are working with; for example, the patch in the /e46 folder is for Eclipse 4.6 Neon)
* org.eclipse.jdt.groovy.core
* org.eclipse.jdt.groovy.core.tests.builder
* org.eclipse.jdt.groovy.core.tests.compiler

When the projects are imported into Eclipse, they should automatically build.  To rebuild, go to the Eclipse menu bar and select *Project -> Clean...* and clean all projects.

## Test with Eclipse

### Automated

Right-click on a test class and select Run As -> JUnit Plug-in Test.

### Manual

For manual testing and debugging, right-click on the org.codehaus.groovy.eclipse.ui project and--depending on what you are trying to do--select *Run As -> Eclipse Application* or *Debug As -> Eclipse Application*.  This will launch another instance of Eclipse loaded with the plug-ins in the workspace.

## Release Build with Maven

[Download and install Maven](https://maven.apache.org/).

From the root directory of the repository, execute the following command to build Groovy-Eclipse for Eclipse 4.6 (Neon).

```
mvn -Pe46  clean install
```

Replace e46 with a different option to build it for an older Eclipse version:

* e45
* e44
* e43j8
* e43
* e42
* e37

Tests will be executed as part of the build.  To skip them, append this option to the command: `-Dmaven.test.skip=true`.

## Contribute

### Track Work

Before making changes, a Github issue should be created to make others aware of what you plan to work on.

### Coding Standards

There are currently no coding standards, but ensure your changes match the style of the code that is already present in the project.

### Submit Changes

Fork the Groovy-Eclipse repository, push your changes to it, and submit a pull request.  If you do not know how to do this, see [the Github forking guide](https://guides.github.com/activities/forking/).

## Debugging 'groovyc' Command

Often defects are addressing something that doesn't compile in Groovy Eclipse and yet compiles fine when compiled with a 'groovyc' command executed from command line interface. Such defects would require one to investigate differences between 'groovyc' command compilation (pure Groovy) and Groovy-Eclipse compilation. This involves debugging 'groovyc' command execution. There are 2 ways debugging 'groovyc' command:

- Attach Eclipse debugger to 'groovyc' java process
- Launch org.codehaus.groovy.tools.FileSystemCompiler as a Java Application

It is recommended to have Groovy source from https://github.com/groovy/groovy-core in your workspace as code in Groovy Eclipse org.codehaus.groovy doesn't exactly match the original groovy code and has a few Groovy Eclipse specific fixes.

### Attach Eclipse Debugger to 'groovyc' java process

1. Execute the following in the console:
	```
	export JAVA_OPTS="--Xdebug --Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=5000,server=y,suspend=y"
	```
1. Start 'groovyc' command from the console, e.g. '<path to groovyc>/groovyc <groovy files to compile>. Process should be suspended until debugger is attached.
1. Create a new 'Remote Java Application' launch configuration in Eclipse. Specify project from the workspace corresponding the version of Groovy for the ran 'groovyc' command, leave host as localhost, and specify the port 5000.
1. Launch the created 'Remote Java Application'.

### Launch org.codehaus.groovy.tools.FileSystemCompiler as Java Application

1. Create a new 'Java Application' launch configuration in Eclipse.
1. Specify the project from the workspace corresponding the required Groovy version.
1. Specify org.codehaus.groovy.tools.FileSystemCompiler as the Main class to launch.
1. Specify groovy files to compile (absolute path) on the 'Arguments' tab of the launch configuration dialog in the 'Program Arguments' text box.
1. Run the newly created launch configuration.
