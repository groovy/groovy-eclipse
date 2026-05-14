## Notes for maintainers of the JDTCompilerAdapter (ant support) in ECJ.

The org.eclipse.jdt.core.JDTCompilerAdapter class can be used by PDE/ant 
via jdtCompilerAdapter.jar if running ant tasks from inside Eclipse or by ant
directly (by adding org.eclipse.jdt.core.compiler.batch jar to ant classpath) 
if running in a separated VM without OSGI.

### Standalone ant use

Using JDTCompilerAdapter class from standalone ant is straightforward - the 
org.eclipse.jdt.core.compiler.batch jar should be added to the ant javac classpath
in some way, like for example here:

```
<javac destdir="${temp.folder}/@dot.bin" failonerror="${javacFailOnError}" verbose="${javacVerbose}" 
	debug="${javacDebugInfo}" includeAntRuntime="no" source="${javacSource}" target="${javacTarget}" encoding="UTF-8">
	<compilerarg line="${compilerArg}" compiler="${build.compiler}"/>
	<classpath refid="@dot.classpath" />
	<src path="src/" />
	<compilerarg line="-properties &apos;.settings/org.eclipse.jdt.core.prefs&apos;" compiler="org.eclipse.jdt.core.JDTCompilerAdapter" />
	<compilerarg line="-log &apos;${temp.folder}/@dot.binnull&apos;" compiler="org.eclipse.jdt.core.JDTCompilerAdapter" />
	<compilerarg line="-g -showversion -encoding UTF-8 -preserveAllLocals 
		-enableJavadoc -nowarn:[src-gen] -nowarn:[.src-gen] -time" compiler="org.eclipse.jdt.core.JDTCompilerAdapter" />
	<compilerclasspath>
		<pathelement path="${ecjBatchCompilerJarLocation}"/>
	</compilerclasspath>
</javac>
```

### Eclipse (OSGI) embedded ant use

Using JDTCompilerAdapter class from ant started from OSGI container (Eclipse) is 
not trivial and has few pitfalls.

First of all, org.eclipse.jdt.core.JDTCompilerAdapter class is packaged into a dedicated
jdtCompilerAdapter.jar that is **not** part of org.eclipse.jdt.core.compiler.batch bundle!

This jdtCompilerAdapter.jar is packaged **inside** org.eclipse.jdt.core bundle for some obscure reasons
and is extracted at runtime in org.eclipse.ant.internal.ui.datatransfer.BuildFileCreator.addInitEclipseCompiler().

Second most surprising point is: if the JDTCompilerAdapter code runs in OSGI environment, in order 
to be properly loaded by ant which is started by PDE/ant.core bundle via
org.eclipse.ant.core.AntRunner.run(Object), the org.eclipse.jdt.core.JDTCompilerAdapter class
should **NOT** be found by classloaders of org.eclipse.jdt.core or org.eclipse.jdt.core.compiler.batch bundles!

The reason is, that the JDTCompilerAdapter class itself needs ant classes for class initialization,
but that must be **same** ant classes loaded by org.eclipse.ant.internal.core.AntClassLoader.
If the ant classes needed by JDTCompilerAdapter class are loaded by OSGI bundle loaders,
ant engine can't use them (even if they are coming from same jar)! From Java
 runtime point of view they are different because loaded by a different classloader. 

Therefore the code in org.eclipse.ant.internal.core.AntClassLoader.loadClassPlugins(String) 
that goes over possible **bundle** classloaders that contribute ant tasks for Eclipse,
**must** fail to load org.eclipse.jdt.core.JDTCompilerAdapter class and return *null*!

This is surprising at least, and has some non trivial consequences.

The main one is: org.eclipse.jdt.core.JDTCompilerAdapter should be "not visible" 
for default OSGI class loading from org.eclipse.jdt.core bundle that contributes 
extra ant classpath entry jdtCompilerAdapter.jar, 
and therefore org.eclipse.jdt.core package should be **not exported** in MANIFEST.MF 
by org.eclipse.jdt.core.compiler.batch!

After failing to load JDTCompilerAdapter class in AntClassLoader.loadClassPlugins(String)
the AntClassLoader code goes to the parent classloader (which is URLClassLoader) 
and the parent walks over **extra** ant classpath entries contributed by plugins 
via "org.eclipse.ant.core.extraClasspathEntries" extension point (see 
org.eclipse.ant.core.AntCorePreferences.computeDefaultExtraClasspathEntries(List)).

The URLClassLoader finally finds and loads JDTCompilerAdapter in jdtCompilerAdapter.jar 
contributed by org.eclipse.jdt.core as an extra ant classpath entry
(with all the required ant classes found by ant own classloader)! 
The URLClassLoader (AntClassLoader) should be initialized with core ant libraries at the beginning, so
it has no issues to satisfy all JDTCompilerAdapter ant dependencies.