/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.compiler.AbstractCompiler;
import org.codehaus.plexus.compiler.Compiler;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerMessage;
import org.codehaus.plexus.compiler.CompilerMessage.Kind;
import org.codehaus.plexus.compiler.CompilerOutputStyle;
import org.codehaus.plexus.compiler.CompilerResult;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Allows the use of the Groovy-Eclipse compiler through Maven.
 *
 * @threadSafe
 */
@Component(role = Compiler.class, hint = "groovy-eclipse-compiler")
public class GroovyEclipseCompiler extends AbstractCompiler {

    // IMPORTANT!!! This class must not reference any JDT classes directly.  It must be loadable even if batch compiler is absent.

    private static final String PROBLEM_SEPARATOR = "----------\r?\n";

    public GroovyEclipseCompiler() {
        // Here is a bit of a hack. Maven only wants a single file extension
        // for sources, so we pass it "". Later, we must recalculate for real.
        super(CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, "", ".class", null);
    }

    private final List<String> vmArgs = new ArrayList<>();

    private boolean verbose = false;

    private String javaAgentClass = "";

    public String getJavaAgentClass() {
        return javaAgentClass;
    }

    public void setJavaAgentClass(String javaAgentClass) {
        this.javaAgentClass = javaAgentClass;
    }

    @Override
    public CompilerResult performCompile(CompilerConfiguration config) throws CompilerException {
        // groovy-eclipse-batch must be depended upon explicitly; if it is not there, then raise a nice, readable error
        try {
            Class.forName("org.eclipse.jdt.core.compiler.CompilationProgress");
        } catch (Exception e) {
            throw new CompilerException("Could not find groovy-eclipse-batch artifact. Must add this artifact as an explicit dependency in the pom.");
        }

        String[] args = createCommandLine(config);
        if (args.length == 0) {
            getLogger().info("Nothing to compile - all classes are up to date");

            return new CompilerResult(true, Collections.EMPTY_LIST);
        }
        if (config.isFork()) {
            String executable = config.getExecutable();
            if (isBlank(executable)) {
                try {
                    executable = getJavaExecutable();
                } catch (IOException e) {
                    getLogger().warn("Unable to autodetect 'java' path, using 'java' from the environment.");
                    executable = "java";
                }
            }

            String groovyEclipseLocation = getGroovyEclipseBatchLocation();
            return compileOutOfProcess(config, executable, groovyEclipseLocation, args);

        } else {
            StringWriter out = new StringWriter();
            if (verbose) getLogger().info("Compiler arguments: " + Arrays.toString(args));
            InternalCompiler.Result result = InternalCompiler.doCompile(args, out, getLogger(), verbose);

            List<CompilerMessage> messages = parseMessages(result.success ? 0 : 1, out.getBuffer().toString(), config.isShowWarnings() || config.isVerbose());
            if (!result.success) {
                messages.add(formatResult(result.success, result.globalErrorsCount, result.globalWarningsCount));
            }

            return new CompilerResult(result.success, messages);
        }
    }

    private File[] recalculateStaleFiles(CompilerConfiguration config) throws CompilerException {
        config.setSourceFiles(null);

        long staleMillis = 0; // Can we do better than using 0?

        Set<String> includes = config.getIncludes();
        if (includes == null || includes.isEmpty()) {
            includes = Collections.singleton("**/*");
        }

        Set<File> staleSources = computeStaleSources(config, new StaleSourceScanner(staleMillis, includes, config.getExcludes()));

        config.setSourceFiles(staleSources);

        return staleSources.toArray(new File[staleSources.size()]);
    }

    private Set<File> computeStaleSources(CompilerConfiguration compilerConfiguration, SourceInclusionScanner scanner) throws CompilerException {
        scanner.addSourceMapping(new SuffixMapping(".groovy", ".class"));
        scanner.addSourceMapping(new SuffixMapping(".java", ".class"));

        File outputDirectory = new File(compilerConfiguration.getOutputLocation());
        Set<File> staleSources = new TreeSet<>();

        for (String sourceRoot : compilerConfiguration.getSourceLocations()) {
            File sourcePath = new File(sourceRoot);
            if (!sourcePath.isDirectory() || sourcePath.equals(compilerConfiguration.getGeneratedSourcesDirectory())) {
                continue;
            }

            if (verbose) {
                getLogger().info("Looking for sources in source root: " + sourceRoot);
            }
            try {
                staleSources.addAll(scanner.getIncludedSources(sourcePath, outputDirectory));
            } catch (InclusionScanException e) {
                throw new CompilerException("Error scanning source root: \'" + sourceRoot + "\' for stale files to recompile.", e);
            }
        }

        return staleSources;
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
            return new CompilerMessage("Found " + globalErrorsCount + " " + error + " and " + globalWarningsCount + " " + warning + ".", kind);
        }
    }

    private Map<String,String> composeSourceFiles(File[] sourceFiles) {
        Map<String, String> sources = new DeduplicatingHashMap<>(getLogger(), sourceFiles.length);
        for (File sourceFile : sourceFiles) {
            sources.put(sourceFile.getPath(), null);
        }
        return sources;
    }

    @Override
    public String[] createCommandLine(CompilerConfiguration config) throws CompilerException {
        File destinationDir = new File(config.getOutputLocation());
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // adds src/main/groovy and src/test/groovy if exksts not already added
        File workingDirectory = config.getWorkingDirectory();
        // assume dest dir for main is in target/classes and for test is in target/test-classes
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

        // recalculate stale files since they were not properly calculated in super
        File[] sourceFiles = recalculateStaleFiles(config);
        if (sourceFiles.length == 0) {
            return new String[0];
        }

        getLogger().info("Using Groovy-Eclipse compiler to compile both Java and Groovy files");
        getLogger().debug(String.format("Compiling %d source file%s to %s", sourceFiles.length, (sourceFiles.length == 1 ? "" : "s"), destinationDir.getAbsolutePath()));


        Map<String, String> args = new DeduplicatingHashMap<>(getLogger());

        verbose = config.isVerbose();
        if (verbose) {
            args.put("-verbose", null);
        }

        String cp = getPathString(config.getClasspathEntries());
        if (verbose) {
            getLogger().info("Classpath: " + cp);
        }
        if (isNotBlank(cp)) {
            args.put("-cp", cp.trim());
        }

        String mp = getPathString(config.getModulepathEntries());
        if (verbose) {
            getLogger().info("Modulepath: " + mp);
        }
        if (isNotBlank(mp)) {
            args.put("-p", mp.trim());
        }

        if (isNotBlank(config.getOutputLocation())) {
            args.put("-d", config.getOutputLocation().trim());
        }

        if (config.getGeneratedSourcesDirectory() != null) {
            args.put("-s", config.getGeneratedSourcesDirectory().getAbsolutePath());
        }

        if (config.isDebug()) {
            if (isNotBlank(config.getDebugLevel())) {
                args.put("-g:" + config.getDebugLevel().trim(), null);
            } else {
                args.put("-g", null);
            }
        }

        if (isNotBlank(config.getSourceEncoding())) {
            args.put("-encoding", config.getSourceEncoding().trim());
        }

        String release = config.getReleaseVersion();
        if (isNotBlank(release)) {
            args.put("--release", release.trim());
        } else {
            String source = config.getSourceVersion();
            if (isNotBlank(source)) {
                args.put("-source", source.trim());
            }
            String target = config.getTargetVersion();
            if (isNotBlank(target)) {
                args.put("-target", target.trim());
            }
        }
        // TODO: Maven 3.7.1: <multiReleaseOutput>

        if (config.isShowDeprecation()) {
            args.put("-deprecation", null);
        }
        if (config.isFailOnWarning()) {
            args.put("-proceedOnError:Fatal", null);
        }
        if (!config.isShowWarnings()) {
            args.put("-nowarn", null);
        }
        if (config.isParameters()) {
            args.put("-parameters", null);
        }

        if ("none".equals(config.getProc())) {
            args.put("-proc:none", null);
        } else if ("only".equals(config.getProc())) {
            args.put("-proc:only", null);
        }

        if (config.getAnnotationProcessors() != null) {
            StringBuilder processor = new StringBuilder();
            for (String item : config.getAnnotationProcessors()) {
                if (isNotBlank(item)) {
                    processor.append(item.trim()).append(',');
                }
            }
            if (processor.length() > 0) {
                // remove the trailing comma
                processor.setLength(processor.length() - 1);
                args.put("-processor", processor.toString());
            }
        }

        if (config.getProcessorPathEntries() != null) {
            StringBuilder processorpath = new StringBuilder();
            for (String item : config.getProcessorPathEntries()) {
                if (isNotBlank(item)) {
                    processorpath.append(item.trim()).append(';');
                }
            }
            if (processorpath.length() > 0) {
                // remove the trailing semicolon
                processorpath.setLength(processorpath.length() - 1);
                args.put("-processorpath", processorpath.toString());
            }
        }

        for (Map.Entry<String, String> entry : config.getCustomCompilerArgumentsAsMap().entrySet()) {
            String key = entry.getKey();
            if (startsWithHyphen(key)) {
                if ("-javaAgentClass".equals(key)) {
                    setJavaAgentClass(entry.getValue());
                } else if (!key.startsWith("-J")) {
                    args.put(key, entry.getValue());
                } else {
                    vmArgs.add(key.substring(2));
                }
            } else if (!"org.osgi.framework.system.packages".equals(key)) { // GRECLIPSE-1418: ignore the system packages option
                args.put("-" + key, entry.getValue());
            }
        }

        args.putAll(composeSourceFiles(sourceFiles));

        return flattenArgumentsMap(args);
    }

    private CompilerResult compileOutOfProcess(CompilerConfiguration config, String executable, String groovyEclipseLocation, String[] args) throws CompilerException {
        Commandline cli = new Commandline();
        cli.setWorkingDirectory(config.getWorkingDirectory().getAbsolutePath());
        cli.setExecutable(executable);

        try {
            if (isNotBlank(javaAgentClass)) {
                cli.addArguments(new String[] {"-javaagent:" + getAdditionnalJavaAgentLocation()});
            }

            if (isNotBlank(config.getMeminitial())) {
                cli.addArguments(new String[] {"-Xms" + config.getMeminitial()});
            }

            if (isNotBlank(config.getMaxmem())) {
                cli.addArguments(new String[] {"-Xmx" + config.getMaxmem()});
            }

            if (!vmArgs.isEmpty()) {
                cli.addArguments(vmArgs.toArray(new String[vmArgs.size()]));
            }

            cli.addArguments(new String[] {"-jar", groovyEclipseLocation});

            if (verbose) getLogger().info("Compiler arguments: " + Arrays.toString(args));
            File argumentsFile = createFileWithArguments(args, config.getOutputLocation());
            cli.addArguments(new String[] {"@" + argumentsFile.getCanonicalPath()});
        } catch (IOException e) {
            throw new CompilerException("Error creating file with javac arguments", e);
        }

        CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();

        if (getLogger().isDebugEnabled()) {
            File commandLineFile = new File(config.getOutputLocation(), "greclipse." + (Os.isFamily(Os.FAMILY_WINDOWS) ? "bat" : "sh"));
            try {
                FileUtils.fileWrite(commandLineFile.getAbsolutePath(), cli.toString().replaceAll("'", ""));
                if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
                    Runtime.getRuntime().exec(new String[] {"chmod", "a+x", commandLineFile.getAbsolutePath()});
                }
            } catch (IOException e) {
                getLogger().warn("Unable to write '" + commandLineFile.getName() + "' debug script file", e);
            }
        }

        getLogger().info(verbose ? cli.toString() : "Compiling in a forked process using " + groovyEclipseLocation);

        int returnCode;
        try {
            returnCode = CommandLineUtils.executeCommandLine(cli, out, err);
        } catch (Exception e) {
            throw new CompilerException("Error while executing the external compiler.", e);
        }

        List<CompilerMessage> messages = parseMessages(returnCode, out.getOutput(), config.isShowWarnings() || config.isVerbose());
        if (returnCode != 0 && messages.isEmpty()) {
            if (isBlank(err.getOutput())) {
                throw new CompilerException("Unknown error trying to execute the external compiler: " + EOL + cli.toString());
            } else {
                messages.add(new CompilerMessage("Failure executing groovy-eclipse compiler:" + EOL + err.getOutput(), Kind.ERROR));
            }
        } else if (isNotBlank(err.getOutput())) {
            getLogger().warn("Error output from groovy-eclipse compiler:" + EOL + err.getOutput());
        }

        return new CompilerResult(returnCode == 0, messages);
    }

    private List<CompilerMessage> parseMessages(int exitCode, String input, boolean showWarnings) {
        List<CompilerMessage> parsedMessages = new ArrayList<>();

        for (String msg : input.split(PROBLEM_SEPARATOR)) {
            if (isBlank(msg)) continue;

            CompilerMessage message = parseMessage(msg, showWarnings, false);
            if (message != null) {
                if (showWarnings || message.getKind() == Kind.ERROR) {
                    parsedMessages.add(message);
                }
            } else {
                StringBuilder unrecognized = new StringBuilder();
                // assume that there are one or more non-normal messages here
                // typical messages start with <num>. ERROR or <num>. WARNING
                for (String line : msg.split("\n")) {
                    if (line.indexOf(". WARNING") > 0 || line.indexOf(". ERROR") > 0) {
                        message = parseMessage(line, showWarnings, true);
                        if (showWarnings || message.getKind() == Kind.ERROR) {
                            parsedMessages.add(message);
                        }
                    } else if (!PROBLEM_SEPARATOR.equals(line)) {
                        unrecognized.append(line).append("\n");
                    }
                }
                if (unrecognized.length() > 0) {
                    message = parseMessage(unrecognized.toString(), showWarnings, true);
                    if (showWarnings || message.getKind() != Kind.WARNING) {
                        parsedMessages.add(message);
                    }
                }
            }
        }

        return parsedMessages;
    }

    /**
     * Constructs a CompilerError object from a line of the compiler output.
     *
     * Eclipse compiler messages should look like this: <pre>
     * 1. WARNING in /Users/andrew/git-repos/foo/src/main/java/packAction.java (at line 47)
     * \tpublic abstract class AbstractScmTagAction extends TaskAction implements BuildBadgeAction {
     * \t                      ^^^^^^^^^^^^^^^^^^^^</pre>
     * But there will also be messages contributed from annotation processors that will look non-normal.
     *
     * @param msgText eclipse compiler message (see above)
     * @param showWarning unused parameter
     * @param force produce {@link CompilerMessage} even if parsing fails
     * @return parsed message and severity
     */
    private CompilerMessage parseMessage(String msgText, boolean showWarning, boolean force) {
        Matcher m = Pattern.compile("^\\d+\\. (ERROR|WARNING|INFO) in (.+?) \\(at line (\\d+)\\)").matcher(msgText);
        boolean isNormal = m.find();
        Kind kind = Kind.NOTE;
        if (isNormal) {
            if (m.group(1).equals("ERROR")) {
                kind = Kind.ERROR;
            } else if (m.group(1).equals("WARNING")) {
                kind = Kind.WARNING;
            }
        }

        if (isNormal) {
            String[] parts = msgText.split("\r?\n", 2);
            try {
                String file = m.group(2);
                int line = Integer.parseInt(m.group(3));
                int lastLineIndex = parts[1].lastIndexOf("\n\t");
                int startColumn = parts[1].indexOf('^', lastLineIndex) - 1 - lastLineIndex; // -1 because starts with tab
                int endColumn = parts[1].lastIndexOf('^') - 1 - lastLineIndex;

                return new CompilerMessage(file, kind, line, startColumn, line, endColumn, EOL + msgText.trim());

            } catch (RuntimeException e) { // lots of things could go wrong
                if (force) {
                    return new CompilerMessage(msgText.trim(), kind);
                } else {
                    return null;
                }
            }
        } else if (force) {
            return new CompilerMessage(msgText.trim(), kind);
        } else {
            return null;
        }
    }

    /**
     * put args into a temp file to be referenced using the @ option in javac
     * command line
     *
     * @param args command file lines (slashes will be normalized)
     * @param outputDirectory parent directory of command file
     * @return the temporary file wth the arguments
     * @throws IOException if create fails
     */
    private File createFileWithArguments(String[] args, String outputDirectory) throws IOException {
        File tempFile;
        if (getLogger().isDebugEnabled()) {
            tempFile = File.createTempFile(GroovyEclipseCompiler.class.getName(), ".txt", new File(outputDirectory));
        } else {
            tempFile = File.createTempFile(GroovyEclipseCompiler.class.getName(), ".txt");
            tempFile.deleteOnExit();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (String arg : args) {
                String argValue = arg.replace(File.separatorChar, '/');
                writer.write("\"" + argValue + "\"");
                writer.println();
            }
            writer.flush();
        }

        return tempFile;
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
            if (verbose) {
                getLogger().info("Found location <" + file.getPath() + "> for className <" + className + ">");
            }
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
     * @throws IOException if not found
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
            if (isBlank(javaHome)) {
                throw new IOException("The environment variable JAVA_HOME is not correctly set.");
            }
            if (!new File(javaHome).isDirectory()) {
                throw new IOException("The environment variable JAVA_HOME=" + javaHome + " doesn't exist or is not a valid directory.");
            }

            javaExe = new File(env.getProperty("JAVA_HOME") + File.separator + "bin", javaCommand);
        }

        if (!javaExe.isFile()) {
            throw new IOException("The javadoc executable '" + javaExe + "' doesn't exist or is not a file. Verify the JAVA_HOME environment variable.");
        }

        return javaExe.getAbsolutePath();
    }

    /**
     * Returns content of the Map as an array of Strings. Ignores {@code null} and empty Strings.
     * Implementation note {@link LinkedHashMap} is preferred Map implementation as it preserves order
     * @param args Map to be converted
     * @return Array with {@code args} converted to an array
     */
    private static String[] flattenArgumentsMap(Map<String, String> args) {
        List<String> argsList = new ArrayList<>(args.size() * 2);

        for (Map.Entry<String, String> entry : args.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key != null && key.length() > 0) {
                argsList.add(key);
                // adds value only if key is actually defined
                if (isNotBlank(value)) {
                    argsList.add(value);
                }
            }
        }

        return argsList.toArray(new String[0]);
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    private static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    private static boolean startsWithHyphen(Object key) {
        return (key instanceof CharSequence && ((CharSequence) key).charAt(0) == '-');
    }

    /**
     * Linked Hash Map implementation that logs replaced entries.
     */
    private static class DeduplicatingHashMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = -589299605523895999L;

        private Logger logger;

        public DeduplicatingHashMap(Logger logger) {
            super();
            this.logger = logger;
        }

        public DeduplicatingHashMap(Logger logger, int initialCapacity) {
            super(initialCapacity);
            this.logger = logger;
        }

        @Override
        public V put(K k, V v) {
            if (this.containsKey(k) && logger.isDebugEnabled()) {
                logger.debug("Replacing compiler argument \"" + k + "\" old value: " + get(k) + " with: " + v);
            }
            return super.put(k, v);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }
    }
}
