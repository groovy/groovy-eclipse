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
 *     Travis Schneeberger  - ensure that all options are supported
 *******************************************************************************/
package org.codehaus.groovy.eclipse.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.codehaus.plexus.compiler.AbstractCompiler;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerError;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerOutputStyle;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.batch.Main;

/**
 * Allows the use of the Groovy-Eclipse compiler through maven.
 * 
 * @plexus.component role="org.codehaus.plexus.compiler.Compiler"
 *                   role-hint="groovy-eclipse-compiler"
 * 
 * 
 * @author <a href="mailto:andrew@eisenberg.as">Andrew Eisenberg</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:matthew.pocock@ncl.ac.uk">Matthew Pocock</a>
 */
public class GroovyEclipseCompiler extends AbstractCompiler {

    // see compiler.warn.warning in compiler.properties of javac sources
    private static final String[] WARNING_PREFIXES = { "warning: ", "\u8b66\u544a: ", "\u8b66\u544a\uff1a " };

    // see compiler.note.note in compiler.properties of javac sources
    private static final String[] NOTE_PREFIXES = { "Note: ", "\u6ce8: ", "\u6ce8\u610f\uff1a " };

    private static final String JAVA_AGENT_CLASS_PARAM_NAME = "-javaAgentClass";

    private String javaAgentClass = "";

    /**
     * Simple progress monitor to keep track of number of files compiled
     * 
     * @author Andrew Eisenberg
     * @created Aug 13, 2010
     */
    private class Progress extends CompilationProgress {

        private int count = 0;

        public void begin(int remainingWork) {}

        public void done() {
            if (verbose) {
                getLogger().info("Compilation complete.  Compiled " + count + " files.");
            }
        }

        public boolean isCanceled() {
            return false;
        }

        public void setTaskName(String newTaskName) {}

        public void worked(int workIncrement, int remainingWork) {
            if (verbose) {
                String file = remainingWork == 1 ? "file" : "files";
                getLogger().info(remainingWork + " " + file + " left.");
                count++;
            }
        }
    }

    boolean verbose;

    public GroovyEclipseCompiler() {
        // here is a bit of a hack. maven only wants a single file extension
        // for sources, so we pass it "". Later, we must recalculate for real.
        super(CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, "", ".class", null);
    }

    @SuppressWarnings("rawtypes")
    public List compile(CompilerConfiguration config) throws CompilerException {

        String[] args = createCommandLine(config);
        if (args.length == 0) {
            getLogger().info("Nothing to compile - all classes are up to date");
            return Collections.emptyList();
        }

        List<CompilerError> messages;
        if (config.isFork()) {
            String executable = config.getExecutable();

            if (StringUtils.isEmpty(executable)) {
                try {
                    executable = getJavaExecutable();
                } catch (IOException e) {
                    getLogger().warn("Unable to autodetect 'java' path, using 'java' from the environment.");
                    executable = "java";
                }
            }

            String groovyEclipseLocation = getGroovyEclipseBatchLocation();
            messages = compileOutOfProcess(config, executable, groovyEclipseLocation, args);
        } else {
            Progress progress = new Progress();
            Main main = new Main(new PrintWriter(System.out), new PrintWriter(System.err), false/* systemExit */,
                    null/* options */, progress);
            boolean result = main.compile(args);

            messages = formatResult(main, result);
        }
        return messages;
    }

    private File[] recalculateStaleFiles(CompilerConfiguration config) throws CompilerException {
        config.setSourceFiles(null);
        long staleMillis = 0; // can we do better than using 0?
        Set<String> includes = config.getIncludes();
        if (includes == null || includes.isEmpty()) {
            includes = Collections.singleton("**/*");
        }
        StaleSourceScanner scanner = new StaleSourceScanner(staleMillis, includes, config.getExcludes());
        Set<File> staleSources = computeStaleSources(config, scanner);
        config.setSourceFiles(staleSources);

        File[] sourceFiles = staleSources.toArray(new File[0]);
        return sourceFiles;
    }

    private boolean startsWithHyphen(Object key) {
        return null != key && String.class.isInstance(key) && ((String) key).startsWith("-");
    }

    private List<CompilerError> formatResult(Main main, boolean result) {
        if (result) {
            return Collections.EMPTY_LIST;
        } else {
            String error = main.globalErrorsCount == 1 ? "error" : "errors";
            String warning = main.globalWarningsCount == 1 ? "warning" : "warnings";
            return Collections.singletonList(new CompilerError("Found " + main.globalErrorsCount + " " + error + " and "
                    + main.globalWarningsCount + " " + warning + ".", true));
        }
    }

    private List<String> composeSourceFiles(File[] sourceFiles) {
        List<String> sources = new ArrayList<String>(sourceFiles.length);
        for (int i = 0; i < sourceFiles.length; i++) {
            sources.add(sourceFiles[i].getPath());
        }
        return sources;
    }

    public String[] createCommandLine(CompilerConfiguration config) throws CompilerException {
        File destinationDir = new File(config.getOutputLocation());

        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // adds src/main/groovy and src/test/groovy if exksts not already added
        File workingDirectory = config.getWorkingDirectory();
        // assume dest dir for main is in target/classes and for test is in
        // target/test-classes
        // There must be a more robust way of doing this.
        if (destinationDir.getName().equals("classes")) {
            File srcMainGroovy = new File(workingDirectory, "src/main/groovy");
            if (srcMainGroovy.exists() && !config.getSourceLocations().contains(srcMainGroovy.getAbsolutePath())) {
                config.addSourceLocation(srcMainGroovy.getAbsolutePath());
            }
        }

        if (destinationDir.getName().equals("test-classes")) {
            File srcTestGroovy = new File(workingDirectory, "src/test/groovy");
            if (srcTestGroovy.exists() && !config.getSourceLocations().contains(srcTestGroovy.getAbsolutePath())) {
                config.addSourceLocation(srcTestGroovy.getAbsolutePath());
            }
        }
        // recalculate stale files since they were not properly calculated in
        // super
        File[] sourceFiles = recalculateStaleFiles(config);

        if (sourceFiles.length == 0) {
            return new String[0];
        }

        getLogger().info("Using Groovy-Eclipse compiler to compile both Java and Groovy files");
        getLogger().debug(
                "Compiling " + sourceFiles.length + " " + "source file" + (sourceFiles.length == 1 ? "" : "s") + " to "
                        + destinationDir.getAbsolutePath());

        List<String> args = new ArrayList<String>();
        String cp = super.getPathString(config.getClasspathEntries());
        verbose = config.isVerbose();
        if (verbose) {
            getLogger().info("Classpath: " + cp);
        }
        if (cp.length() > 0) {
            args.add("-cp");
            args.add(cp);
        }

        if (config.getOutputLocation() != null && config.getOutputLocation().length() > 0) {
            args.add("-d");
            args.add(config.getOutputLocation());
        }

        if (config.isDebug()) {
            if (config.getDebugLevel() != null && config.getDebugLevel().trim().length() > 0) {
                args.add("-g:" + config.getDebugLevel());
            } else {
                args.add("-g");
            }
        }

        if ("none".equals(config.getProc())) {
            args.add("-proc:none");
        } else if ("only".equals(config.getProc())) {
            args.add("-proc:only");
        }

        if (config.getGeneratedSourcesDirectory() != null) {
            args.add("-s");
            args.add(config.getGeneratedSourcesDirectory().getAbsolutePath());
        }

        // change default to 1.5
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

        if (config.isShowDeprecation()) {
            args.add("-deprecation");
        }
        if (!config.isShowWarnings()) {
            args.add("-nowarn");
        }

        if (config.getAnnotationProcessors() != null) {
            StringBuilder procArg = new StringBuilder();
            for (String proc : config.getAnnotationProcessors()) {
                if (proc != null && proc.trim().length() > 0) {
                    procArg.append(proc);
                    procArg.append(",");
                }
            }
            if (procArg.length() > 0) {
                args.add("-processor ");
                procArg.replace(procArg.length() - 1, procArg.length(), "");
                args.add("\"" + procArg.toString() + "\"");
            }
        }

        if (verbose) {
            args.add("-verbose");
        }

        if (config.getSourceEncoding() != null) {
            args.add("-encoding");
            args.add(config.getSourceEncoding());
        }

        for (Entry<Object, Object> entry : (Iterable<Entry<Object, Object>>) config.getCustomCompilerArguments().entrySet()) {

            Object key = entry.getKey();
            if (startsWithHyphen(key)) {
                if (JAVA_AGENT_CLASS_PARAM_NAME.equals(key)) {
                    setJavaAgentClass((String) entry.getValue());
                    // do not add the custom java agent arg because it is not
                    // expected by groovy-eclipse compiler
                } else {
                    // don't add a "-" if the arg
                    // already has one
                    args.add((String) key);
                }
            } else if (key != null && !key.equals("org.osgi.framework.system.packages")) {
                // See https://jira.codehaus.org/browse/GRECLIPSE-1418 ignore
                // the system packages option
                /*
                 * Not sure what the possible range of usage looks like but I
                 * don't think this should allow for null keys? "-null" probably
                 * isn't going to play nicely with any compiler?
                 */
                args.add("-" + key);
                if (null != entry.getValue()) { // don't allow a null value
                    args.add("\"" + entry.getValue() + "\"");
                }
            }

        }

        args.addAll(composeSourceFiles(sourceFiles));

        if (verbose) {
            getLogger().info("All args: " + args);
        }

        return args.toArray(new String[args.size()]);
    }

    private Set<File> computeStaleSources(CompilerConfiguration compilerConfiguration, SourceInclusionScanner scanner)
            throws CompilerException {
        SourceMapping mappingGroovy = new SuffixMapping(".groovy", ".class");

        SourceMapping mappingJava = new SuffixMapping(".java", ".class");
        scanner.addSourceMapping(mappingGroovy);
        scanner.addSourceMapping(mappingJava);

        File outputDirectory = new File(compilerConfiguration.getOutputLocation());
        Set<File> staleSources = new HashSet<File>();

        for (String sourceRoot : (List<String>) compilerConfiguration.getSourceLocations()) {
            if (verbose) {
                getLogger().info("Looking for sources in source root: " + sourceRoot);
            }
            File rootFile = new File(sourceRoot);

            if (!rootFile.isDirectory()) {
                continue;
            }

            try {
                staleSources.addAll(scanner.getIncludedSources(rootFile, outputDirectory));
            } catch (InclusionScanException e) {
                throw new CompilerException(
                        "Error scanning source root: \'" + sourceRoot + "\' " + "for stale files to recompile.", e);
            }
        }

        return staleSources;
    }

    /**
     * Compile the java sources in a external process, calling an external
     * executable, like javac.
     * 
     * @param config
     *            compiler configuration
     * @param executable
     *            name of the executable to launch
     * @param args
     *            arguments for the executable launched
     * @return List of CompilerError objects with the errors encountered.
     * @throws CompilerException
     */
    private List<CompilerError> compileOutOfProcess(CompilerConfiguration config, String executable, String groovyEclipseLocation,
            String[] args) throws CompilerException {

        Commandline cli = new Commandline();
        cli.setWorkingDirectory(config.getWorkingDirectory().getAbsolutePath());
        cli.setExecutable(executable);

        try {
            // we need to setup any javaagent before the -jar flag
            if (!StringUtils.isEmpty(javaAgentClass)) {
                cli.addArguments(new String[] { "-javaagent:" + getAdditionnalJavaAgentLocation() });
            } else {
                getLogger().info("no javaAgentClass seems to be set");
            }

            cli.addArguments(new String[] { "-jar", groovyEclipseLocation });

            File argumentsFile = createFileWithArguments(args, config.getOutputLocation());
            cli.addArguments(new String[] { "@" + argumentsFile.getCanonicalPath().replace(File.separatorChar, '/') });

            if (!StringUtils.isEmpty(config.getMaxmem())) {
                cli.addArguments(new String[] { "-J-Xmx" + config.getMaxmem() });
            }

            if (!StringUtils.isEmpty(config.getMeminitial())) {
                cli.addArguments(new String[] { "-J-Xms" + config.getMeminitial() });
            }

        } catch (IOException e) {
            throw new CompilerException("Error creating file with javac arguments", e);
        }

        CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();

        int returnCode;
        List<CompilerError> messages;

        if ((getLogger() != null) && getLogger().isDebugEnabled()) {
            File commandLineFile = new File(config.getOutputLocation(), "greclipse."
                    + (Os.isFamily(Os.FAMILY_WINDOWS) ? "bat" : "sh"));
            try {
                FileUtils.fileWrite(commandLineFile.getAbsolutePath(), cli.toString().replaceAll("'", ""));

                if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
                    Runtime.getRuntime().exec(new String[] { "chmod", "a+x", commandLineFile.getAbsolutePath() });
                }
            } catch (IOException e) {
                if ((getLogger() != null) && getLogger().isWarnEnabled()) {
                    getLogger().warn("Unable to write '" + commandLineFile.getName() + "' debug script file", e);
                }
            }
        }

        try {
            getLogger().info("Compiling in a forked process using " + groovyEclipseLocation);
            returnCode = CommandLineUtils.executeCommandLine(cli, out, err);
            messages = parseModernStream(returnCode, new BufferedReader(new StringReader(err.getOutput())));
        } catch (CommandLineException e) {
            throw new CompilerException("Error while executing the external compiler.", e);
        } catch (IOException e) {
            throw new CompilerException("Error while executing the external compiler.", e);
        }

        if ((returnCode != 0) && messages.isEmpty()) {
            if (err.getOutput().length() == 0) {
                throw new CompilerException("Unknown error trying to execute the external compiler: " + EOL + cli.toString());
            } else {
                messages.add(new CompilerError("Failure executing groovy-eclipse compiler:" + EOL + err.getOutput(), true));
            }
        }

        return messages;
    }

    /**
     * Parse the output from the compiler into a list of CompilerError objects
     * 
     * @param exitCode
     *            The exit code of javac.
     * @param input
     *            The output of the compiler
     * @return List of CompilerError objects
     * @throws IOException
     */
    List<CompilerError> parseModernStream(int exitCode, BufferedReader input) throws IOException {
        List<CompilerError> errors = new ArrayList<CompilerError>();

        String line;

        StringBuffer buffer;

        while (true) {
            // cleanup the buffer
            buffer = new StringBuffer(); // this is quicker than clearing it

            // most errors terminate with the '^' char
            do {
                line = input.readLine();

                if (line == null) {
                    return errors;
                }

                if ((buffer.length() == 0) && line.startsWith("error: ")) {
                    errors.add(new CompilerError(line, true));
                } else if ((buffer.length() == 0) && isNote(line)) {
                    // skip, JDK 1.5 telling us deprecated APIs are used but
                    // -Xlint:deprecation isn't set
                } else {
                    buffer.append(line);

                    buffer.append(EOL);
                }
            } while (!line.endsWith("^"));

            // add the error bean
            errors.add(parseModernError(exitCode, buffer.toString()));
        }
    }

    private static boolean isNote(String line) {
        for (int i = 0; i < NOTE_PREFIXES.length; i++) {
            if (line.startsWith(NOTE_PREFIXES[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Construct a CompilerError object from a line of the compiler output
     * 
     * @param exitCode
     *            The exit code from javac.
     * @param error
     *            output line from the compiler
     * @return the CompilerError object
     */
    static CompilerError parseModernError(int exitCode, String error) {
        StringTokenizer tokens = new StringTokenizer(error, ":");

        boolean isError = exitCode != 0;

        StringBuffer msgBuffer;

        try {
            // With Java 6 error output lines from the compiler got longer. For
            // backward compatibility
            // .. and the time being, we eat up all (if any) tokens up to the
            // erroneous file and source
            // .. line indicator tokens.

            boolean tokenIsAnInteger;

            String previousToken = null;

            String currentToken = null;

            do {
                previousToken = currentToken;

                currentToken = tokens.nextToken();

                // Probably the only backward compatible means of checking if a
                // string is an integer.

                tokenIsAnInteger = true;

                try {
                    Integer.parseInt(currentToken);
                } catch (NumberFormatException e) {
                    tokenIsAnInteger = false;
                }
            } while (!tokenIsAnInteger);

            String file = previousToken;

            String lineIndicator = currentToken;

            int startOfFileName = previousToken.lastIndexOf("]");

            if (startOfFileName > -1) {
                file = file.substring(startOfFileName + 2);
            }

            // When will this happen?
            if (file.length() == 1) {
                file = new StringBuffer(file).append(":").append(tokens.nextToken()).toString();
            }

            int line = Integer.parseInt(lineIndicator);

            msgBuffer = new StringBuffer();

            String msg = tokens.nextToken(EOL).substring(2);

            isError = exitCode != 0;

            // Remove the 'warning: ' prefix
            String warnPrefix = getWarnPrefix(msg);
            if (warnPrefix != null) {
                isError = false;
                msg = msg.substring(warnPrefix.length());
            }

            msgBuffer.append(msg);

            msgBuffer.append(EOL);

            String context = tokens.nextToken(EOL);

            String pointer = tokens.nextToken(EOL);

            if (tokens.hasMoreTokens()) {
                msgBuffer.append(context); // 'symbol' line

                msgBuffer.append(EOL);

                msgBuffer.append(pointer); // 'location' line

                msgBuffer.append(EOL);

                context = tokens.nextToken(EOL);

                try {
                    pointer = tokens.nextToken(EOL);
                } catch (NoSuchElementException e) {
                    pointer = context;

                    context = null;
                }

            }

            String message = msgBuffer.toString();

            int startcolumn = pointer.indexOf("^");

            int endcolumn = context == null ? startcolumn : context.indexOf(" ", startcolumn);

            if (endcolumn == -1) {
                endcolumn = context.length();
            }

            return new CompilerError(file, isError, line, startcolumn, line, endcolumn, message.trim());
        } catch (NoSuchElementException e) {
            return new CompilerError("no more tokens - could not parse error message: " + error, isError);
        } catch (NumberFormatException e) {
            return new CompilerError("could not parse error message: " + error, isError);
        } catch (Exception e) {
            return new CompilerError("could not parse error message: " + error, isError);
        }
    }

    private static String getWarnPrefix(String msg) {
        for (int i = 0; i < WARNING_PREFIXES.length; i++) {
            if (msg.startsWith(WARNING_PREFIXES[i])) {
                return WARNING_PREFIXES[i];
            }
        }
        return null;
    }

    /**
     * put args into a temp file to be referenced using the @ option in javac
     * command line
     * 
     * @param args
     * @return the temporary file wth the arguments
     * @throws IOException
     */
    private File createFileWithArguments(String[] args, String outputDirectory) throws IOException {
        PrintWriter writer = null;
        try {
            File tempFile;
            if ((getLogger() != null) && getLogger().isDebugEnabled()) {
                tempFile = File.createTempFile(GroovyEclipseCompiler.class.getName(), "arguments", new File(outputDirectory));
            } else {
                tempFile = File.createTempFile(GroovyEclipseCompiler.class.getName(), "arguments");
                tempFile.deleteOnExit();
            }

            writer = new PrintWriter(new FileWriter(tempFile));

            for (int i = 0; i < args.length; i++) {
                String argValue = args[i].replace(File.separatorChar, '/');

                writer.write("\"" + argValue + "\"");

                writer.println();
            }

            writer.flush();

            return tempFile;

        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private String getAdditionnalJavaAgentLocation() throws CompilerException {
        return getClassLocation(getJavaAgentClass());
    }

    private String getGroovyEclipseBatchLocation() throws CompilerException {
        return getClassLocation(Main.class.getName());
    }

    private String getClassLocation(String className) throws CompilerException {
        Class<?> cls;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new CompilerException("Cannot find the requested className <" + className + "> in classpath");
        }
        ProtectionDomain pDomain = cls.getProtectionDomain();
        CodeSource cSource = pDomain.getCodeSource();
        if (cSource != null) {
            URL loc = cSource.getLocation();
            File file;
            try {
                file = new File(URLDecoder.decode(loc.getPath(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                getLogger().warn("Unsupported Encoding for URL: " + loc, e);
                file = new File(loc.getPath());
            }
            getLogger().info("Found location <" + file.getPath() + "> for className <" + className + ">");
            return file.getPath();
        } else {
            throw new CompilerException("Cannot find the location of the requested className <" + className + "> in classpath");
        }
    }
    
    /**
     * Get the path of the javac tool executable: try to find it depending the
     * OS or the <code>java.home</code> system property or the
     * <code>JAVA_HOME</code> environment variable.
     * 
     * @return the path of the Javadoc tool
     * @throws IOException
     *             if not found
     */
    private String getJavaExecutable() throws IOException {
        String javaCommand = "java" + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".exe" : "");

        String javaHome = System.getProperty("java.home");
        File javaExe;
        if (Os.isName("AIX")) {
            javaExe = new File(javaHome + File.separator + ".." + File.separator + "sh", javaCommand);
        } else if (Os.isName("Mac OS X")) {
            javaExe = new File(javaHome + File.separator + "bin", javaCommand);
        } else {
            javaExe = new File(javaHome + File.separator + ".." + File.separator + "bin", javaCommand);
        }

        // ----------------------------------------------------------------------
        // Try to find javacExe from JAVA_HOME environment variable
        // ----------------------------------------------------------------------
        if (!javaExe.isFile()) {
            Properties env = CommandLineUtils.getSystemEnvVars();
            javaHome = env.getProperty("JAVA_HOME");
            if (StringUtils.isEmpty(javaHome)) {
                throw new IOException("The environment variable JAVA_HOME is not correctly set.");
            }
            if (!new File(javaHome).isDirectory()) {
                throw new IOException("The environment variable JAVA_HOME=" + javaHome
                        + " doesn't exist or is not a valid directory.");
            }

            javaExe = new File(env.getProperty("JAVA_HOME") + File.separator + "bin", javaCommand);
        }

        if (!javaExe.isFile()) {
            throw new IOException("The javadoc executable '" + javaExe
                    + "' doesn't exist or is not a file. Verify the JAVA_HOME environment variable.");
        }

        return javaExe.getAbsolutePath();
    }

    public String getJavaAgentClass() {
        return javaAgentClass;
    }

    public void setJavaAgentClass(String className) {
        this.javaAgentClass = className;
    }
}
