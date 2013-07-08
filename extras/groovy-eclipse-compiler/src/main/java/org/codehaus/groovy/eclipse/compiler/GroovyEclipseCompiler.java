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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import java.util.Properties;
import java.util.Set;

import org.codehaus.groovy.eclipse.compiler.InternalCompiler.Result;
import org.codehaus.plexus.compiler.AbstractCompiler;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerMessage;
import org.codehaus.plexus.compiler.CompilerResult;
import org.codehaus.plexus.compiler.CompilerMessage.Kind;
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
    // IMPORTANT!!! this class must not reference any JDT classes directly.  Must be loadable even if batch compiler not around

    private static final String PROB_SEPARATOR = "----------\n";

    private static final String JAVA_AGENT_CLASS_PARAM_NAME = "-javaAgentClass";

    private String javaAgentClass = "";

    boolean verbose;

    public GroovyEclipseCompiler() {
        // here is a bit of a hack. maven only wants a single file extension
        // for sources, so we pass it "". Later, we must recalculate for real.
        super(CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, "", ".class", null);
    }
    
    @Override
    public CompilerResult performCompile(CompilerConfiguration configuration) throws CompilerException {
        checkForGroovyEclipseBatch();
        
        List<CompilerMessage> messages = new ArrayList<CompilerMessage>();
        boolean result = internalCompile(configuration, messages);
        return new CompilerResult(result, messages);
    }

    /**
     * groovy-eclipse-batch must be depended upon explicitly.if it is not there, then raise a nice, readable error
     * @throws CompilerException 
     */
    private void checkForGroovyEclipseBatch() throws CompilerException {
        try {
            Class.forName("org.eclipse.jdt.core.compiler.CompilationProgress");
        } catch (Exception e) {
            throw new CompilerException("Could not find groovy-eclipse-batch artifact. "
                    + "Must add this artifact as an explicit dependency the pom.");
        }
    }

    private boolean internalCompile(CompilerConfiguration config, List<CompilerMessage> messages) throws CompilerException {

        String[] args = createCommandLine(config);
        if (args.length == 0) {
            getLogger().info("Nothing to compile - all classes are up to date");
            return true;
        }

        boolean success;
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
            success = compileOutOfProcess(config, executable, groovyEclipseLocation, args, messages);
        } else {
            StringWriter out = new StringWriter();
            Result result = InternalCompiler.doCompile(args, out, getLogger(), verbose);
            success = result.success;
            try {
                messages.addAll(parseMessages(success ? 0 : 1, out.getBuffer().toString(), config.isShowWarnings()));
            } catch (IOException e) {
                messages = new ArrayList<CompilerMessage>(1);
            }

            if (!success) {
                messages.add(formatResult(success, result.globalErrorsCount, result.globalWarningsCount));
            }
        }
        return success;
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

    private CompilerMessage formatResult(boolean result, int globalErrorsCount, int globalWarningsCount) {
        if (result) {
            return new CompilerMessage("Success!", Kind.NOTE);
        } else {
            Kind kind;
            if (globalErrorsCount > 0) {
                kind = Kind.ERROR;
            } else if (globalWarningsCount > 0) {
                kind = Kind.WARNING;
            } else {
                kind = Kind.NOTE;
            }
            
            String error = globalErrorsCount == 1 ? "error" : "errors";
            String warning = globalWarningsCount == 1 ? "warning" : "warnings";
            return new CompilerMessage("Found " + globalErrorsCount + " " + error + " and "
                    + globalWarningsCount + " " + warning + ".", kind);
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

        for (Entry<String, String> entry : config.getCustomCompilerArgumentsAsMap().entrySet()) {

            String key = entry.getKey();
            if (startsWithHyphen(key)) {
                if (JAVA_AGENT_CLASS_PARAM_NAME.equals(key)) {
                    setJavaAgentClass(entry.getValue());
                    // do not add the custom java agent arg because it is not
                    // expected by groovy-eclipse compiler
                    continue;
                } else {
                    // don't add a "-" if the arg
                    // already has one
                    args.add(key);
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
            }
            if (null != entry.getValue()) { // don't allow a null value
                args.add("\"" + entry.getValue() + "\"");
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

        for (String sourceRoot : compilerConfiguration.getSourceLocations()) {
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
     * @param messages2 
     * @return List of CompilerError objects with the errors encountered.
     * @throws CompilerException
     */
    private boolean compileOutOfProcess(CompilerConfiguration config, String executable, String groovyEclipseLocation,
            String[] args, List<CompilerMessage> messages) throws CompilerException {

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
            messages.addAll(parseMessages(returnCode, out.getOutput(), config.isShowWarnings()));
        } catch (CommandLineException e) {
            throw new CompilerException("Error while executing the external compiler.", e);
        } catch (IOException e) {
            throw new CompilerException("Error while executing the external compiler.", e);
        }

        if ((returnCode != 0) && messages.isEmpty()) {
            if (err.getOutput().length() == 0) {
                throw new CompilerException("Unknown error trying to execute the external compiler: " + EOL + cli.toString());
            } else {
                messages.add(new CompilerMessage("Failure executing groovy-eclipse compiler:" + EOL + err.getOutput(), Kind.ERROR));
            }
        }

        return returnCode != 0;
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
   private List<CompilerMessage> parseMessages(int exitCode, String input, boolean showWarnings) throws IOException {
        List<CompilerMessage> parsedMessages = new ArrayList<CompilerMessage>();

        String[] msgs = input.split(PROB_SEPARATOR);
        for (String msg : msgs) {
            if (msg.length() > 1) {
                // add the error bean
                CompilerMessage message = parseMessage(msg, showWarnings, false);
                if (message != null) {
                    if (showWarnings || message.getKind() == Kind.ERROR) {
                        parsedMessages.add(message);
                    }
                } else {
                    // assume that there are one or more non-normal messages here
                    // All messages start with <num>. ERROR or <num>. WARNING
                    String[] extraMsgs = msg.split("\n");
                    StringBuilder sb = new StringBuilder();
                    for (String extraMsg : extraMsgs) {
                        if (extraMsg.indexOf(". WARNING") > 0 || extraMsg.indexOf(". ERROR") > 0) {
                            if (sb.length() > 0) {
                                message = parseMessage(sb.toString(), showWarnings, true);
                                if (showWarnings || message.getKind() == Kind.ERROR) {
                                    parsedMessages.add(message);
                                }
                            }
                            sb = new StringBuilder(extraMsg);
                        } else {
                            if (!PROB_SEPARATOR.equals(extraMsg)) {
                                sb.append(extraMsg);
                            }
                        }
                    }
                }
            }
        }
        return parsedMessages;
    }

    /**
     * Construct a CompilerError object from a line of the compiler output
     * 
     * @param msgText
     *            output line from the compiler
     * @return the CompilerError object
     */
   private CompilerMessage parseMessage(String msgText, boolean showWarning, boolean force) {
        // message should look like this:
//        1. WARNING in /Users/andrew/git-repos/foo/src/main/java/packAction.java (at line 47)
//            public abstract class AbstractScmTagAction extends TaskAction implements BuildBadgeAction {
//                                  ^^^^^^^^^^^^^^^^^^^^
        
        // But there will also be messages contributed from annotation processors that will look non-normal
        int dotIndex = msgText.indexOf('.');
        Kind kind;
        boolean isNormal = false;
        if (dotIndex > 0) {
            if (msgText.substring(dotIndex, dotIndex + ". WARNING".length()).equals(". WARNING")) {
                kind = Kind.WARNING;
                isNormal = true;
                dotIndex += ". WARNING in ".length();
            } else if (msgText.substring(dotIndex, dotIndex + ". ERROR".length()).equals(". ERROR")) {
                kind = Kind.ERROR;
                isNormal = true;
                dotIndex += ". ERROR in ".length();
            } else {
                kind = Kind.NOTE;
            }
        } else {
            kind = Kind.NOTE;
        }
        
        int firstNewline = msgText.indexOf('\n');
        String firstLine = firstNewline > 0 ? msgText.substring(0, firstNewline) : msgText;
        String rest = firstNewline > 0 ? msgText.substring(firstNewline+1) : "";
        
        if (isNormal) {
            try {
                int parenIndex = firstLine.indexOf(" (");
                String file = firstLine.substring(dotIndex, parenIndex);
                int line = Integer.parseInt(firstLine.substring(parenIndex + " (at line ".length(), firstLine.indexOf(')')));
                int lastLineIndex = rest.lastIndexOf("\n\t");
                int startColumn = rest.indexOf('^', lastLineIndex) -1 - lastLineIndex; // -1 because starts with tab
                int endColumn = rest.lastIndexOf('^') -1 - lastLineIndex;
                return new CompilerMessage(file, kind, line, startColumn, line, endColumn, msgText);
            } catch (RuntimeException e) {
                // lots of things could go wrong
                if (force) {
                    return new CompilerMessage(msgText, kind);
                } else {
                    return null;
                }
            }
        } else {
            if (force) {
                return new CompilerMessage(msgText, kind);
            } else {
                return null;
            }
        }
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
        // can't reference JDT directly in this class
        return getClassLocation("org.eclipse.jdt.internal.compiler.batch.Main");
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
