# Groovy for Eclipse Plug-in

This Eclipse plug-in adds Groovy tooling support to Eclipse, for the Groovy programming language.

## Users

Information on __[how to use and install](https://github.com/groovy/groovy-eclipse/wiki)__ this Groovy for Eclipse Plug-in.

## Questions and Answers

Check out [groovy-eclipse](http://stackoverflow.com/search?q=groovy-eclipse) on stackoverflow.

## Issues

Please report [improvement idea's, possible bugs, etc as github issues](https://github.com/groovy/groovy-eclipse/issues?q=is%3Aissue+is%3Aopen).

## For Developers who want to Contribute

A quick approach to getting started:

1. Download Eclipse SDK (must have the RCP plugins installed)
2. Install Groovy-Eclipse
3. Clone this repository
4. Import projects from the cloned Git repository. You do not need to import all projects, and some projects conflict with each other.  Here are some suggestions on what to import:
	- **IMPORTANT:** there are multiple `org.eclipse.jdt.core` projects.  These projects contain the patched Eclipse Java compiler and are specific to a single version of Eclipse.  Only import the single project that matches your Eclipse level (e.g., E36, E37, or E42).  To determine which project is appropriate for your eclipse level, look at the path to each of these projects, and you will see a segment like: `jdt-patch/e42`.  Match your eclipse level to the path.  If you do not follow this step, you will see compile errors in the imported projects.
	- The projects `org.codehaus.groovy16`, `org.codehaus.groovy18`, and `org.codehaus.groovy20` projects contain the groovy 1.6, 1.8, and 2.0 compilers respectively.  The `org.codehaus.groovy` project contains the groovy 1.7 compiler.  It is not necessary to import all of these projects.  If you import more than one compiler project, then the latest one will take precedence over the earlier ones.
	- The `org.codehaus.groovy.m2eclipse` project _requires m2eclipse_ to be installed in your target Eclipse.  If m2eclipse is not available, there will be compile errors.  This project provides maven integration when running in an Eclipse workbench.  You do not need to import this project unless you intend to work on maven integration.
	- Any project that contains the word test contains unit tests.  It is recommended to import these projects.
	- Any project prefixed with Feature as well as the Site Groovy does not contain code and can be ignored unless you know that you need it.
	- Any project prefixed with groovy-eclipse- is part of the maven compiler plugin for groovy-eclipse.  You only need to import these projects if you plan on working on the maven support.
	- The org.codehaus.groovy.eclipse.pluginbuilder project contains the releng code to build Groovy-Eclipse and publish the update site.
5. If any of the imported projects has an error about Groovy Compiler version then open Eclipse Preferences, navigate to _Groovy -> Compiler_ and set the required Groovy Compiler version, apply the changes and restart Eclipse.
