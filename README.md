# Groovy-Eclipse

This is the [Groovy-Eclipse](http://groovy.codehaus.org/Eclipse+Plugin) project, the Eclipse tooling support for the [Groovy programming language](http://groovy.codehaus.org).  More information, including how to use and install is available on the [Groovy-Eclipse](http://groovy.codehaus.org/Eclipse+Plugin) home page.

## Bugs and Questions

Report bugs on the [codehaus jira](http://jira.codehaus.org/browse/GRECLIPSE).

Send questions to the [mailing list](http://xircles.codehaus.org/lists/eclipse-plugin-user@groovy.codehaus.org).

## Getting Started

For a complete guide on getting started with Groovy-Eclipse source code, see the [developer documentation](http://groovy.codehaus.org/Getting+Started+With+Groovy-Eclipse+Source+Code).

A quick approach to getting started:

1. Download Eclipse SDK (must have the RCP plugins installed)
2. Install Groovy-Eclipse
3. Clone this repository
4. Import projects from the cloned Git repository. You do not need to import all projects, and some projects conflict with each other.  Here are some suggestions on what to import:
	- **IMPORTANT:** there are multiple org.eclipse.jdt.core projects.  These projects contain the patched Eclipse Java compiler and are specific to a single version of Eclipse.  Only import the single project that matches your Eclipse level (e.g., E36, E37, or E42).  To determine which project is appropriate for your eclipse level, look at the path to each of these projects, and you will see a segment like: jdt-patch/e42.  Match your eclipse level to the path.  If you do not follow this step, you will see compile errors in the imported projects.
	- The projects org.codehaus.groovy16, org.codehaus.groovy18, and org.codehaus.groovy20 projects contain the groovy 1.6, 1.8, and 2.0 compilers respectively.  The org.codehaus.groovy project contains the groovy 1.7 compiler.  It is not necessary to import all of these projects.  If you import more than one compiler project, then the latest one will take precedence over the earlier ones.
	- The org.codehaus.groovy.m2eclipse project requires m2eclipse to be installed in your target Eclipse.  If m2eclipse is not available, there will be compile errors.  This project provides maven integration when running in an Eclipse workbench.  You do not need to import this project unless you intend to work on maven integration.
	- Any project that contains the word test contains unit tests.  It is recommended to import these projects.
	- Any project prefixed with Feature as well as the Site Groovy does not contain code and can be ignored unless you know that you need it.
	- Any project prefixed with groovy-eclipse- is part of the maven compiler plugin for groovy-eclipse.  You only need to import these projects if you plan on working on the maven support.
	- The org.codehaus.groovy.eclipse.pluginbuilder project contains the releng code to build Groovy-Eclipse and publish the update site.
5. If any of the imported projects has an error about Groovy Compiler version then open Eclipse Preferences, navigate to Groovy -> Compiler and set the required Groovy Compiler version, apply the changes and restart Eclipse.
