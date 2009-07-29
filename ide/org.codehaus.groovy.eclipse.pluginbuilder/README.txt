======================================================
README FILE:
BUILD AUTOMATION OF THE GROOVY ECLIPSE PROJECT

AUTOR: Reto Kleeb (reto at techbase REMOVE THIS dot ch)
DATE: 6. 5. 2008
======================================================

This File describes the required steps to automatically build the Groovy-Eclipse plugin.
The description can be used to integrate the build process in any common automated build solution
such as CruiseControl.

The script 'runscript.sh' requires XVFB to be installed on the system!

1. Checkout org.codehaus.groovy.eclipse.pluginbuilder project from the
official Repository (only this project!)

2. Create a build directory ('buildDirectory') this directory will be used to download
the sources, compile them and run the tests.

3. Edit the Variables: 'buildHome', 'buildDirectory' and 'eclipseDir' in the file: 'build_local.props'
	* 'buildHome' should point to the directory of the pluginbuilder project
	* 'buildDirectory' should point to the directory we created in step 2
	* 'eclipseDir' should point to a directory that contains an unpacked PDE Eclipse

4. Edit the Variable: 'test.eclipse.zip' in the file 'build-files/automatedTests/run-tests.properties'
	* 'test.eclipse.zip' should point to a zipped / tarred version of eclipse
	* You might also have to adjust the variables 'os', 'ws' and 'arch'
This Eclipse will be unpacked before every test-run.

5. Edit the file 'runscript.sh' so that it matches your environment.

6. Run the script

More information about this build configuration can be found on:
http://sifsstud4.hsr.ch/trac/GroovyRefactoring/wiki/CI_AutoTest


