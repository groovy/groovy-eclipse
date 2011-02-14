/*******************************************************************************
 * Copyright (c) 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg     - Initial API and implementation
 *     Carlos Fernandez     - fix for nowarn
 *******************************************************************************/
package org.codehaus.groovy.eclipse.compiler;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.codehaus.plexus.compiler.AbstractCompiler;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerError;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerOutputStyle;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.batch.Main;

/**
 * @plexus.component role="org.codehaus.plexus.compiler.Compiler"
 *                   role-hint="groovy-eclipse"
 * 
 * 
 * @author <a href="mailto:andrew@eisenberg.as">Andrew Eisenberg</a>
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 */
public class GroovyEclipseCompiler extends AbstractCompiler {

    boolean verbose;

    /*
    Eclipse Compiler for Java(TM) 0.A58, 3.6.0
    Copyright IBM Corp 2000, 2010. All rights reserved.
     
     Usage: <options> <source files | directories>
     If directories are specified, then their source contents are compiled.
     Possible options are listed below. Options enabled by default are prefixed
     with '+'.
     
     Classpath options:
        -cp -classpath <directories and ZIP archives separated by :>
                           specify location for application classes and sources.
                           Each directory or file can specify access rules for
                           types between '[' and ']' (e.g. [-X] to forbid
                           access to type X, [~X] to discourage access to type X,
                           [+p/X:-p/*] to forbid access to all types in package p
                           but allow access to p/X)
        -bootclasspath <directories and ZIP archives separated by :>
                           specify location for system classes. Each directory or
                           file can specify access rules for types between '['
                           and ']'
        -sourcepath <directories and ZIP archives separated by :>
                           specify location for application sources. Each directory
                           or file can specify access rules for types between '['
                           and ']'. Each directory can further specify a specific
                           destination directory using a '-d' option between '['
                           and ']'; this overrides the general '-d' option.
                           .class files created from source files contained in a
                           jar file are put in the user.dir folder in case no
                           general '-d' option is specified. ZIP archives cannot
                           override the general '-d' option
        -extdirs <directories separated by :>
                           specify location for extension ZIP archives
        -endorseddirs <directories separated by :>
                           specify location for endorsed ZIP archives
        -d <dir>           destination directory (if omitted, no directory is
                           created); this option can be overridden per source
                           directory
        -d none            generate no .class files
        -encoding <enc>    specify default encoding for all source files. Each
                           file/directory can override it when suffixed with
                           '['<enc>']' (e.g. X.java[utf8]).
                           If multiple default encodings are specified, the last
                           one will be used.
     
     Compliance options:
        -1.3               use 1.3 compliance (-source 1.3 -target 1.1)
        -1.4             + use 1.4 compliance (-source 1.3 -target 1.2)
        -1.5 -5 -5.0       use 1.5 compliance (-source 1.5 -target 1.5)
        -1.6 -6 -6.0       use 1.6 compliance (-source 1.6 -target 1.6)
        -1.7 -7 -7.0       use 1.7 compliance (-source 1.7 -target 1.7)
        -source <version>  set source level: 1.3 to 1.7 (or 5, 5.0, etc)
        -target <version>  set classfile target: 1.1 to 1.7 (or 5, 5.0, etc)
                           cldc1.1 can also be used to generate the StackMap
                           attribute
     
     Warning options:
        -deprecation     + deprecation outside deprecated code (equivalent to
                           -warn:+deprecation)
        -nowarn -warn:none disable all warnings
        -?:warn -help:warn display advanced warning options
     
     Error options:
        -err:<warnings separated by ,>    convert exactly the listed warnings
                                          to be reported as errors
        -err:+<warnings separated by ,>   enable additional warnings to be
                                          reported as errors
        -err:-<warnings separated by ,>   disable specific warnings to be
                                          reported as errors
     
     Setting warning or error options using properties file:
        -properties: <file>   set warnings/errors option based on the properties
                              file contents. This option can be used with -nowarn,
                              -err:.. or -warn:.. options, but the last one on the
                              command line sets the options to be used.
     
     Debug options:
        -g[:lines,vars,source] custom debug info
        -g:lines,source  + both lines table and source debug info
        -g                 all debug info
        -g:none            no debug info
        -preserveAllLocals preserve unused local vars for debug purpose
     
     Annotation processing options:
       These options are meaningful only in a 1.6 environment.
        -Akey[=value]        options that are passed to annotation processors
        -processorpath <directories and ZIP archives separated by :>
                             specify locations where to find annotation processors.
                             If this option is not used, the classpath will be
                             searched for processors
        -processor <class1[,class2,...]>
                             qualified names of the annotation processors to run.
                             This bypasses the default annotation discovery process
        -proc:only           run annotation processors, but do not compile
        -proc:none           perform compilation but do not run annotation
                             processors
        -s <dir>             destination directory for generated source files
        -XprintProcessorInfo print information about which annotations and elements
                             a processor is asked to process
        -XprintRounds        print information about annotation processing rounds
        -classNames <className1[,className2,...]>
                             qualified names of binary classes to process
     
     Advanced options:
        @<file>            read command line arguments from file
        -maxProblems <n>   max number of problems per compilation unit (100 by
                           default)
        -log <file>        log to a file. If the file extension is '.xml', then
                           the log will be a xml file.
        -proceedOnError[:Fatal]
                           do not stop at first error, dumping class files with
                           problem methods
                           With ":Fatal", all optional errors are treated as fatal
        -verbose           enable verbose output
        -referenceInfo     compute reference info
        -progress          show progress (only in -log mode)
        -time              display speed information 
        -noExit            do not call System.exit(n) at end of compilation (n==0
                           if no error)
        -repeat <n>        repeat compilation process <n> times for perf analysis
        -inlineJSR         inline JSR bytecode (implicit if target >= 1.5)
        -enableJavadoc     consider references in javadoc
        -Xemacs            used to enable emacs-style output in the console.
                           It does not affect the xml log output
     
        -? -help           print this help message
        -v -version        print compiler version
        -showversion       print compiler version and continue
     
     Ignored options:
        -J<option>         pass option to virtual machine (ignored)
        -X<option>         specify non-standard option (ignored
                           except for listed -X options)
        -X                 print non-standard options and exit (ignored)
        -O                 optimize for execution time (ignored)
     */
    public GroovyEclipseCompiler() {
        super(CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, ".groovy",
                ".class", null);
    }

    public List compile(CompilerConfiguration config) throws CompilerException {
        File destinationDir = new File(config.getOutputLocation());

        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }
        
        // force the resetting of the source files so that java files are included
        config.setSourceFiles(null);
        config.addInclude("**/*.java");
        config.addInclude("**/*.groovy");
        String[] sourceFiles = getSourceFiles(config);

        if (sourceFiles.length == 0) {
            getLogger().warn("No sources added to compile; skipping");
            return Collections.EMPTY_LIST;
        }

        getLogger().info("Using Groovy-Eclipse compiler to compile both Java and Groovy files");
        getLogger().info("Compiling " + sourceFiles.length + " "
                + "source file" + (sourceFiles.length == 1 ? "" : "s") + " to "
                + destinationDir.getAbsolutePath());
        

        List args = new ArrayList();
        String cp = super.getPathString(config.getClasspathEntries());
        verbose = config.isVerbose();
        if (verbose) {
            getLogger().info("Classpath: " + cp);
        }
        if (cp.length() > 0) {
            args.add("-cp");
            args.add(cp);
        }
        
        if (config.getOutputLocation()!= null && config.getOutputLocation().length() > 0) {
            args.add("-d");
            args.add(config.getOutputLocation());
        }
        
        args.add("-g");
        
        // change default to 1.5...why?  because I say so.
        String source = config.getSourceVersion();
        args.add("-source");
        if (source != null && source.length() > 0) {
            args.add(source);
        } else {
            args.add("1.5");
        }
        String target = config.getTargetVersion();
        args.add("-target");
        if (target != null && target.length() > 0) {
            args.add(target);
        } else {
            args.add("1.5");
        }

        // trigger nowarn based on the CompilerConfiguration
        if (config.isShowWarnings() ) {
            args.add("-nowarn");
        }

        //TODO review CompilerConfiguration - make sure all options are taken into account


        for (Iterator argIter = config.getCustomCompilerArguments().entrySet().iterator(); argIter.hasNext();) {
            Entry entry = (Entry) argIter.next();

            Object key = entry.getKey();
            if (doesStartWithHyphen(key)) { // don't add a "-" if the arg already has one
                args.add(key);
            } else {
                /*
                 * Not sure what the possible range of usage looks like but
                 * i don't think this should allow for null keys?
                 * "-null" probably isn't going to play nicely with any compiler?
                 */
                args.add("-" + key);
            }

            if (null != entry.getValue()) { // don't allow a null value
                args.add("\"" + entry.getValue() + "\"");
            }

        }
        
        args.addAll(composeSourceFiles(sourceFiles));
        
        if (verbose) {
            args.add("-verbose");
        }
        
        if (verbose) {
            getLogger().info("All args: " + args);
        }
        Progress progress = new Progress();
        Main main = new Main(new PrintWriter(System.out), new PrintWriter(System.err), false/*systemExit*/, null/*options*/, progress);
        boolean result = main.compile((String[]) args.toArray(new String[args.size()]));

        return formatResult(main, result);
    }

    private boolean doesStartWithHyphen(Object key) {
        return null != key
                && String.class.isInstance(key)
                && ((String)key).startsWith("-");
    }

    /**
     * @param main
     * @param result
     * @return
     */
    private List formatResult(Main main, boolean result) {
        if (result) {
            return Collections.EMPTY_LIST;
        } else {
            String error = main.globalErrorsCount == 1 ? "error" : "errors";
            String warning = main.globalWarningsCount == 1 ? "warning" : "warnings";
            return Collections.singletonList(new CompilerError("Found " + main.globalErrorsCount + " " + error + " and " + main.globalWarningsCount + " " + warning  + ".", true));
        }
    }

    private List composeSourceFiles(String[] sourceFiles) {
        List sources = new ArrayList(sourceFiles.length);
        for (int i = 0; i < sourceFiles.length; i++) {
            sources.add(sourceFiles[i]);
        }
        return sources;
    }

    public String[] createCommandLine(CompilerConfiguration config)
            throws CompilerException {
        return null;
    }
    
    /**
     * Simple progress monitor to keep track of number of files compiled
     * 
     * @author Andrew Eisenberg
     * @created Aug 13, 2010
     */
    private class Progress extends CompilationProgress {
        
        int numCompiled = 0;

        public void begin(int arg0) { }

        public void done() { }

        public boolean isCanceled() {
            return false;
        }

        public void setTaskName(String newTaskName) { }

        public void worked(int workIncrement, int remainingWork) {
            if (verbose) {
                String file = remainingWork == 1 ? "file" : "files";
                getLogger().info(remainingWork + " " + file + " left.");
            }
            numCompiled++;
        }
        
    }

}