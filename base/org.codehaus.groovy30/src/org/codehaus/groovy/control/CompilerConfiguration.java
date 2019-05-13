/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.control;

import org.apache.groovy.util.Maps;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.io.NullWriter;
import org.codehaus.groovy.control.messages.WarningMessage;
import groovyjarjarasm.asm.Opcodes;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import static org.apache.groovy.util.SystemUtil.getSystemPropertySafe;

/**
 * Compilation control flags and coordination stuff.
 */
public class CompilerConfiguration {

    /** This (<code>"indy"</code>) is the Optimization Option value for enabling <code>invokedynamic</code> compilation. */
    public static final String INVOKEDYNAMIC = "indy";

    /** This (<code>"groovydoc"</code>) is the Optimization Option value for enabling attaching groovydoc as AST node metadata. */
    public static final String GROOVYDOC = "groovydoc";

    /** This (<code>"runtimeGroovydoc"</code>) is the Optimization Option value for enabling attaching {@link groovy.lang.Groovydoc} annotation. */
    public static final String RUNTIME_GROOVYDOC = "runtimeGroovydoc";

    /** This (<code>"memStub"</code>) is the Joint Compilation Option value for enabling generating stubs in memory. */
    public static final String MEM_STUB = "memStub";

    /** This (<code>"1.4"</code>) is the value for targetBytecode to compile for a JDK 1.4. **/
    public static final String JDK4 = "1.4";
    /** This (<code>"1.5"</code>) is the value for targetBytecode to compile for a JDK 1.5. **/
    public static final String JDK5 = "1.5";
    /** This (<code>"1.6"</code>) is the value for targetBytecode to compile for a JDK 1.6. **/
    public static final String JDK6 = "1.6";
    /** This (<code>"1.7"</code>) is the value for targetBytecode to compile for a JDK 1.7. **/
    public static final String JDK7 = "1.7";
    /** This (<code>"1.8"</code>) is the value for targetBytecode to compile for a JDK 1.8. **/
    public static final String JDK8 = "1.8";
    /** This (<code>"9"</code>) is the value for targetBytecode to compile for a JDK 9. **/
    public static final String JDK9 = "9";
    /** This (<code>"10"</code>) is the value for targetBytecode to compile for a JDK 10. **/
    public static final String JDK10 = "10";
    /** This (<code>"11"</code>) is the value for targetBytecode to compile for a JDK 11. **/
    public static final String JDK11 = "11";
    /** This (<code>"12"</code>) is the value for targetBytecode to compile for a JDK 12. **/
    public static final String JDK12 = "12";
    /** This (<code>"13"</code>) is the value for targetBytecode to compile for a JDK 13. **/
    public static final String JDK13 = "13";

    /**
     * This constant is for comparing targetBytecode to ensure it is set to JDK 1.5 or later.
     * @deprecated
     */
    @Deprecated
    public static final String POST_JDK5 = JDK5;

    /**
     * This constant is for comparing targetBytecode to ensure it is set to an earlier value than JDK 1.5.
     * @deprecated
     */
    @Deprecated
    public static final String PRE_JDK5 = JDK4;

    /**
     * JDK version to bytecode version mapping
     */
    public static final Map<String, Integer> JDK_TO_BYTECODE_VERSION_MAP = Maps.of(
            JDK4, Opcodes.V1_4,
            JDK5, Opcodes.V1_5,
            JDK6, Opcodes.V1_6,
            JDK7, Opcodes.V1_7,
            JDK8, Opcodes.V1_8,
            JDK9, Opcodes.V9,
            JDK10, Opcodes.V10,
            JDK11, Opcodes.V11,
            JDK12, Opcodes.V12,
            JDK13, Opcodes.V13
    );

    /* GRECLIPSE edit
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /** An array of the valid targetBytecode values ** /
    public static final String[] ALLOWED_JDKS = JDK_TO_BYTECODE_VERSION_MAP.keySet().toArray(EMPTY_STRING_ARRAY);
    */
    /** The valid targetBytecode values. */
    public static final String[] ALLOWED_JDKS = JDK_TO_BYTECODE_VERSION_MAP.keySet().toArray(new String[JDK_TO_BYTECODE_VERSION_MAP.size()]);
    // GRECLIPSE end

    /* GRECLIPSE edit
    private static final String GROOVY_ANTLR4_OPT = "groovy.antlr4";
    */

    /**
     * The default source encoding
     */
    public static final String DEFAULT_SOURCE_ENCODING = "UTF-8";

    /**
     *  A convenience for getting a default configuration.  Do not modify it!
     *  See {@link #CompilerConfiguration(Properties)} for an example on how to
     *  make a suitable copy to modify.  But if you're really starting from a
     *  default context, then you probably just want <code>new CompilerConfiguration()</code>. 
     */
    public static final CompilerConfiguration DEFAULT = new CompilerConfiguration() {
        @Override
        public List<String> getClasspath() {
            return Collections.unmodifiableList(super.getClasspath());
        }

        @Override
        public List<CompilationCustomizer> getCompilationCustomizers() {
            return Collections.unmodifiableList(super.getCompilationCustomizers());
        }

        @Override
        public Set<String> getDisabledGlobalASTTransformations() {
            return Collections.emptySet();
        }

        @Override
        public Map<String, Object> getJointCompilationOptions() {
            return Collections.unmodifiableMap(super.getJointCompilationOptions());
        }

        @Override
        public Map<String, Boolean> getOptimizationOptions() {
            return Collections.unmodifiableMap(super.getOptimizationOptions());
        }

        @Override
        public Set<String> getScriptExtensions() {
            return Collections.unmodifiableSet(super.getScriptExtensions());
        }

        @Override
        public void setBytecodePostprocessor(BytecodeProcessor bytecodePostprocessor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setClasspath(String classpath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setClasspathList(List<String> parts) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompilerConfiguration addCompilationCustomizers(CompilationCustomizer... customizers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDebug(boolean debug) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDefaultScriptExtension(String defaultScriptExtension) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisabledGlobalASTTransformations(Set<String> disabledGlobalASTTransformations) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setJointCompilationOptions(Map<String, Object> options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMinimumRecompilationInterval(int time) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOptimizationOptions(Map<String, Boolean> options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOutput(PrintWriter output) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setParameters(boolean parameters) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPluginFactory(ParserPluginFactory pluginFactory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRecompileGroovySource(boolean recompile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setScriptBaseClass(String scriptBaseClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setScriptExtensions(Set<String> scriptExtensions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSourceEncoding(String encoding) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTargetBytecode(String version) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTargetDirectory(File directory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTargetDirectory(String directory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTolerance(int tolerance) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setVerbose(boolean verbose) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setWarningLevel(int level) {
            throw new UnsupportedOperationException();
        }
    };


    /**
     * See {@link WarningMessage} for levels.
     */
    private int warningLevel;

    /**
     * Encoding for source files
     */
    private String sourceEncoding;

    /**
     * The <code>PrintWriter</code> does nothing.
     */
    private PrintWriter output;

    /**
     * Directory into which to write classes
     */
    private File targetDirectory;

    /**
     * Classpath for use during compilation
     */
    private LinkedList<String> classpath;

    /**
     * If true, the compiler should produce action information
     */
    private boolean verbose;

    /**
     * If true, debugging code should be activated
     */
    private boolean debug;

    /**
     * If true, generates metadata for reflection on method parameters
     */
    private boolean parameters;

    /**
     * The number of non-fatal errors to allow before bailing
     */
    private int tolerance;

    /**
     * Base class name for scripts (must derive from Script)
     */
    private String scriptBaseClass;

    private ParserPluginFactory pluginFactory;

    /**
     * extension used to find a groovy file
     */
    private String defaultScriptExtension;

    /**
     * extensions used to find a groovy files
     */
    private Set<String> scriptExtensions = new LinkedHashSet<String>();

    /**
     * if set to true recompilation is enabled
     */
    private boolean recompileGroovySource;

    /**
     * sets the minimum of time after a script can be recompiled.
     */
    private int minimumRecompilationInterval;

    /**
     * sets the bytecode version target
     */
    private String targetBytecode;

    /**
     * Whether the bytecode version has preview features enabled (JEP 12)
     */
    private boolean previewFeatures;

    /**
     * options for joint compilation (null by default == no joint compilation)
     */
    private Map<String, Object> jointCompilationOptions;

    /**
     * options for optimizations (empty map by default)
     */
    private Map<String, Boolean> optimizationOptions;

    private final List<CompilationCustomizer> compilationCustomizers = new LinkedList<CompilationCustomizer>();

    /**
     * Global AST transformations which should not be loaded even if they are
     * defined in META-INF/services/org.codehaus.groovy.transform.ASTTransformation files.
     * By default, none are disabled.
     */
    private Set<String> disabledGlobalASTTransformations;

    private BytecodeProcessor bytecodePostprocessor;

    /**
     * defines if antlr2 parser should be used or the antlr4 one if
     * no factory is set yet
     *
     * The antlr4 parser Parrot is enabled by default
     *
     */
    /* GRECLIPSE edit
    private ParserVersion parserVersion = ParserVersion.V_4;

    public static final int ASM_API_VERSION = Opcodes.ASM7;
    */

    /**
     * Sets the compiler flags/settings to default values.
     *
     * The following system properties are referenced when setting the configuration:
     *
     * <blockquote>
     * <table summary="Groovy Compiler Configuration Properties">
     *   <tr><th>Property Key</th><th>Related Property Getter</th></tr>
     *   <tr><td><code>groovy.antlr4</code></td><td>{@link #getParserVersion}</td></tr>
     *   <tr><td><code>groovy.source.encoding</code> (defaulting to <code>file.encoding</code>)</td><td>{@link #getSourceEncoding}</td></tr>
     *   <tr><td><code>groovy.target.bytecode</code></td><td>{@link #getTargetBytecode}</td></tr>
     *   <tr><td><code>groovy.target.directory</code></td><td>{@link #getTargetDirectory}</td></tr>
     *   <tr><td><code>groovy.parameters</code></td><td>{@link #getParameters()}</td></tr>
     *   <tr><td><code>groovy.preview.features</code></td><td>{@link #isPreviewFeatures}</td></tr>
     *   <tr><td><code>groovy.script.base</code></td><td>{@link #getScriptBaseClass}</td></tr>
     *   <tr><td><code>groovy.default.scriptExtension</code></td><td>{@link #getDefaultScriptExtension}</td></tr>
     * </table>
     * </blockquote>
     *
     * The following system properties are referenced when setting the configuration optimization options:
     *
     * <blockquote>
     * <table summary="Groovy Compiler Optimization Options Configuration Properties">
     *   <tr><th>Property Key</th><th>Related Property Getter</th></tr>
     *   <tr><td><code>groovy.target.indy</code></td><td>{@link #getOptimizationOptions}</td></tr>
     *   <tr><td><code>groovy.attach.groovydoc</code></td><td>{@link #getOptimizationOptions}</td></tr>
     *   <tr><td><code>groovy.attach.runtime.groovydoc</code></td><td>{@link #getOptimizationOptions}</td></tr>
     * </table>
     * </blockquote>
     */
    public CompilerConfiguration() {
        // Set in safe defaults
        warningLevel = WarningMessage.LIKELY_ERRORS;
        classpath = new LinkedList<String>();
        parameters = getSystemPropertySafe("groovy.parameters") != null;
        tolerance = 10;
        minimumRecompilationInterval = 100;

        setTargetBytecodeIfValid(getSystemPropertySafe("groovy.target.bytecode", getMinBytecodeVersion()));

        previewFeatures = getSystemPropertySafe("groovy.preview.features") != null;
        defaultScriptExtension = getSystemPropertySafe("groovy.default.scriptExtension", ".groovy");

        // Source file encoding
        String encoding = getSystemPropertySafe("file.encoding", DEFAULT_SOURCE_ENCODING);
        encoding = getSystemPropertySafe("groovy.source.encoding", encoding);
        setSourceEncodingOrDefault(encoding);

        setTargetDirectorySafe(getSystemPropertySafe("groovy.target.directory"));

        optimizationOptions = new HashMap<>(4);
        handleOptimizationOption(optimizationOptions, INVOKEDYNAMIC, "groovy.target.indy");
        handleOptimizationOption(optimizationOptions, GROOVYDOC, "groovy.attach.groovydoc");
        handleOptimizationOption(optimizationOptions, RUNTIME_GROOVYDOC, "groovy.attach.runtime.groovydoc");

        jointCompilationOptions = new HashMap<>(4);
        handleJointCompilationOption(jointCompilationOptions, MEM_STUB, "groovy.generate.stub.in.memory");

        /* GRECLIPSE edit
        try {
            String groovyAntlr4Opt = getSystemPropertySafe(GROOVY_ANTLR4_OPT);

            this.parserVersion =
                    null == groovyAntlr4Opt || Boolean.valueOf(groovyAntlr4Opt)
                            ? ParserVersion.V_4
                            : ParserVersion.V_2;
        } catch (Exception e) {
            // IGNORE
        }
        */
    }

    private void handleOptimizationOption(Map<String, Boolean> options, String optionName, String sysOptionName) {
        String propValue = getSystemPropertySafe(sysOptionName);
        boolean optionEnabled = propValue == null
                ? (DEFAULT == null ? false : Boolean.TRUE.equals(DEFAULT.getOptimizationOptions().get(optionName)))
                : Boolean.valueOf(propValue);

        if (optionEnabled) {
            options.put(optionName, Boolean.TRUE);
        }
    }

    private void handleJointCompilationOption(Map<String, Object> options, String optionName, String sysOptionName) {
        String propValue = getSystemPropertySafe(sysOptionName);
        boolean optionEnabled = propValue == null
                ? (DEFAULT == null ? false : Boolean.TRUE.equals(DEFAULT.getJointCompilationOptions().get(optionName)))
                : Boolean.valueOf(propValue);

        if (optionEnabled) {
            options.put(optionName, Boolean.TRUE);
        }
    }

    /**
     * Copy constructor. Use this if you have a mostly correct configuration
     * for your compilation but you want to make a some changes programmatically.
     * An important reason to prefer this approach is that your code will most
     * likely be forward compatible with future changes to this configuration API.
     * <p>
     * An example of this copy constructor at work:
     * <blockquote><pre>
     * // In all likelihood there is already a configuration in your code's context
     * // for you to copy, but for the sake of this example we'll use the global default.
     * CompilerConfiguration myConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
     * myConfiguration.setDebug(true);
     * </pre></blockquote>
     *
     * @param configuration The configuration to copy.
     */
    public CompilerConfiguration(CompilerConfiguration configuration) {
        setWarningLevel(configuration.getWarningLevel());
        setTargetDirectory(configuration.getTargetDirectory());
        setClasspathList(new LinkedList<String>(configuration.getClasspath()));
        setVerbose(configuration.getVerbose());
        setDebug(configuration.getDebug());
        setParameters(configuration.getParameters());
        setTolerance(configuration.getTolerance());
        setScriptBaseClass(configuration.getScriptBaseClass());
        setRecompileGroovySource(configuration.getRecompileGroovySource());
        setMinimumRecompilationInterval(configuration.getMinimumRecompilationInterval());
        setTargetBytecode(configuration.getTargetBytecode());
        setPreviewFeatures(configuration.isPreviewFeatures());
        setDefaultScriptExtension(configuration.getDefaultScriptExtension());
        setSourceEncoding(configuration.getSourceEncoding());
        Map<String, Object> jointCompilationOptions = configuration.getJointCompilationOptions();
        if (jointCompilationOptions != null) {
            jointCompilationOptions = new HashMap<String, Object>(jointCompilationOptions);
        }
        setJointCompilationOptions(jointCompilationOptions);
        setPluginFactory(configuration.getPluginFactory());
        /* GRECLIPSE edit
        setParserVersion(configuration.getParserVersion());
        */
        setDisabledGlobalASTTransformations(configuration.getDisabledGlobalASTTransformations());
        setScriptExtensions(new LinkedHashSet<String>(configuration.getScriptExtensions()));
        setOptimizationOptions(new HashMap<String, Boolean>(configuration.getOptimizationOptions()));
        setBytecodePostprocessor(configuration.getBytecodePostprocessor());
    }

    /**
     * Sets the configuration flags/settings according to values from the supplied {@code Properties} instance
     * or if not found, supplying a default value.
     *
     * Note that unlike {@link #CompilerConfiguration()}, the "defaults" here do <em>not</em> in general
     * include checking the settings in {@link System#getProperties()}.
     * If you want to set a few flags but keep Groovy's default
     * configuration behavior then be sure to make your settings in
     * a {@code Properties} object that is backed by <code>System.getProperties()</code> (which
     * is done using this constructor). That might be done like this:
     * <blockquote><pre>
     * Properties myProperties = new Properties(System.getProperties());
     * myProperties.setProperty("groovy.output.debug", "true");
     * myConfiguration = new CompilerConfiguration(myProperties);
     * </pre></blockquote>
     * And you also have to contend with a possible {@code SecurityException} when
     * getting the system properties (See {@link System#getProperties()}).
     * A safer approach would be to copy a default
     * {@code CompilerConfiguration} and make your changes there using the setter:
     * <blockquote><pre>
     * // In all likelihood there is already a configuration for you to copy,
     * // but for the sake of this example we'll use the global default.
     * CompilerConfiguration myConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
     * myConfiguration.setDebug(true);
     * </pre></blockquote>
     *
     * The following properties are referenced when setting the configuration:
     *
     * <blockquote>
     * <table summary="Groovy Compiler Configuration Properties">
     *   <tr><th>Property Key</th><th>Related Property Getter</th></tr>
     *   <tr><td><code>groovy.warnings</code></td><td>{@link #getWarningLevel}</td></tr>
     *   <tr><td><code>groovy.source.encoding</code> (defaulting to <code>file.encoding</code>)</td><td>{@link #getSourceEncoding}</td></tr>
     *   <tr><td><code>groovy.target.directory</code></td><td>{@link #getTargetDirectory}</td></tr>
     *   <tr><td><code>groovy.target.bytecode</code></td><td>{@link #getTargetBytecode}</td></tr>
     *   <tr><td><code>groovy.parameters</code></td><td>{@link #getParameters()}</td></tr>
     *   <tr><td><code>groovy.preview.features</code></td><td>{@link #isPreviewFeatures}</td></tr>
     *   <tr><td><code>groovy.classpath</code></td><td>{@link #getClasspath}</td></tr>
     *   <tr><td><code>groovy.output.verbose</code></td><td>{@link #getVerbose}</td></tr>
     *   <tr><td><code>groovy.output.debug</code></td><td>{@link #getDebug}</td></tr>
     *   <tr><td><code>groovy.errors.tolerance</code></td><td>{@link #getTolerance}</td></tr>
     *   <tr><td><code>groovy.default.scriptExtension</code></td><td>{@link #getDefaultScriptExtension}</td></tr>
     *   <tr><td><code>groovy.script.base</code></td><td>{@link #getScriptBaseClass}</td></tr>
     *   <tr><td><code>groovy.recompile</code></td><td>{@link #getRecompileGroovySource}</td></tr>
     *   <tr><td><code>groovy.recompile.minimumInterval</code></td><td>{@link #getMinimumRecompilationInterval}</td></tr>
     *   <tr><td><code>groovy.disabled.global.ast.transformations</code></td><td>{@link #getDisabledGlobalASTTransformations}</td></tr>
     * </table>
     * </blockquote>
     *
     * @param configuration The properties to get flag values from.
     */
    public CompilerConfiguration(Properties configuration) throws ConfigurationException {
        this();
        configure(configuration);
    }

    /**
     * Checks if the specified bytecode version string represents a JDK 1.5+ compatible
     * bytecode version.
     * @param bytecodeVersion The parameter can take one of the values in {@link #ALLOWED_JDKS}.
     * @return true if the bytecode version is JDK 1.5+
     */
    public static boolean isPostJDK5(String bytecodeVersion) {
        return new BigDecimal(bytecodeVersion).compareTo(new BigDecimal(JDK5)) >= 0;
    }

    /**
     * Checks if the specified bytecode version string represents a JDK 1.7+ compatible
     * bytecode version.
     * @param bytecodeVersion The parameter can take one of the values in {@link #ALLOWED_JDKS}.
     * @return true if the bytecode version is JDK 1.7+
     */
    public static boolean isPostJDK7(String bytecodeVersion) {
        return new BigDecimal(bytecodeVersion).compareTo(new BigDecimal(JDK7)) >= 0;
    }

    /**
     * Checks if the specified bytecode version string represents a JDK 1.8+ compatible
     * bytecode version.
     * @param bytecodeVersion The parameter can take one of the values in {@link #ALLOWED_JDKS}.
     * @return true if the bytecode version is JDK 1.8+
     */
    public static boolean isPostJDK8(String bytecodeVersion) {
        return new BigDecimal(bytecodeVersion).compareTo(new BigDecimal(JDK8)) >= 0;
    }

    /**
     * Checks if the specified bytecode version string represents a JDK 1.8+ compatible
     * bytecode version.
     * @param bytecodeVersion The parameter can take one of the values in {@link #ALLOWED_JDKS}.
     * @return true if the bytecode version is JDK 9.0+
     */
    public static boolean isPostJDK9(String bytecodeVersion) {
        return new BigDecimal(bytecodeVersion).compareTo(new BigDecimal(JDK9)) >= 0;
    }

    /**
     * Method to configure a CompilerConfiguration by using Properties.
     * For a list of available properties look at {@link #CompilerConfiguration(Properties)}.
     * @param configuration The properties to get flag values from.
     */
    public void configure(Properties configuration) throws ConfigurationException {
        String text;
        int numeric;

        numeric = getWarningLevel();
        text = configuration.getProperty("groovy.warnings", "likely errors");
        try {
            numeric = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            text = text.toLowerCase();
            if (text.equals("none")) {
                numeric = WarningMessage.NONE;
            } else if (text.startsWith("likely")) {
                numeric = WarningMessage.LIKELY_ERRORS;
            } else if (text.startsWith("possible")) {
                numeric = WarningMessage.POSSIBLE_ERRORS;
            } else if (text.startsWith("paranoia")) {
                numeric = WarningMessage.PARANOIA;
            } else {
                throw new ConfigurationException("unrecognized groovy.warnings: " + text);
            }
        }
        setWarningLevel(numeric);

        text = configuration.getProperty("groovy.source.encoding");
        if (text == null) {
            text = configuration.getProperty("file.encoding", DEFAULT_SOURCE_ENCODING);
        }
        setSourceEncoding(text);

        text = configuration.getProperty("groovy.target.directory");
        if (text != null) setTargetDirectory(text);

        text = configuration.getProperty("groovy.target.bytecode");
        if (text != null) setTargetBytecode(text);

        text = configuration.getProperty("groovy.parameters");
        if (text != null) setParameters(text.equalsIgnoreCase("true"));

        text = configuration.getProperty("groovy.preview.features");
        if (text != null) setPreviewFeatures(text.equalsIgnoreCase("true"));

        text = configuration.getProperty("groovy.classpath");
        if (text != null) setClasspath(text);

        text = configuration.getProperty("groovy.output.verbose");
        if (text != null) setVerbose(text.equalsIgnoreCase("true"));

        text = configuration.getProperty("groovy.output.debug");
        if (text != null) setDebug(text.equalsIgnoreCase("true"));

        numeric = 10;
        text = configuration.getProperty("groovy.errors.tolerance", "10");
        try {
            numeric = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(e);
        }
        setTolerance(numeric);

        text = configuration.getProperty("groovy.default.scriptExtension");
        if (text != null) setDefaultScriptExtension(text);

        text = configuration.getProperty("groovy.script.base");
        if (text != null) setScriptBaseClass(text);

        text = configuration.getProperty("groovy.recompile");
        if (text != null) setRecompileGroovySource(text.equalsIgnoreCase("true"));

        numeric = 100;
        text = configuration.getProperty("groovy.recompile.minimumIntervall"); // legacy misspelling
        try {
            if (text == null) text = configuration.getProperty("groovy.recompile.minimumInterval");
            if (text != null) {
                numeric = Integer.parseInt(text);
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException(e);
        }
        setMinimumRecompilationInterval(numeric);

        text = configuration.getProperty("groovy.disabled.global.ast.transformations");
        if (text != null) {
            String[] classNames = text.split(",\\s*}");
            Set<String> blacklist = new HashSet<String>(Arrays.asList(classNames));
            setDisabledGlobalASTTransformations(blacklist);
        }
    }

    /**
     * Gets the currently configured warning level. See {@link WarningMessage}
     * for level details.
     */
    public int getWarningLevel() {
        return this.warningLevel;
    }

    /**
     * Sets the warning level. See {@link WarningMessage} for level details.
     */
    public void setWarningLevel(int level) {
        if (level < WarningMessage.NONE || level > WarningMessage.PARANOIA) {
            this.warningLevel = WarningMessage.LIKELY_ERRORS;
        }
        else {
            this.warningLevel = level;
        }
    }

    /**
     * Gets the currently configured source file encoding.
     */
    public String getSourceEncoding() {
        return this.sourceEncoding;
    }

    /**
     * Sets the encoding to be used when reading source files.
     */
    public void setSourceEncoding(String encoding) {
        setSourceEncodingOrDefault(encoding);
    }

    private void setSourceEncodingOrDefault(String encoding) {
        if (encoding == null) encoding = DEFAULT_SOURCE_ENCODING;
        this.sourceEncoding = encoding;
    }

    /**
     * Gets the currently configured output writer.
     * @deprecated not used anymore
     */
    @Deprecated
    public PrintWriter getOutput() {
        return this.output;
    }

    /**
     * Sets the output writer.
     * @deprecated not used anymore, has no effect
     */
    @Deprecated
    public void setOutput(PrintWriter output) {
        if (output == null) {
            this.output = new PrintWriter(NullWriter.DEFAULT);
        } else {
            this.output = output;
        }
    }

    /**
     * Gets the target directory for writing classes.
     */
    public File getTargetDirectory() {
        return this.targetDirectory;
    }

    /**
     * Sets the target directory.
     */
    public void setTargetDirectory(String directory) {
        setTargetDirectorySafe(directory);
    }

    private void setTargetDirectorySafe(String directory) {
        if (directory != null && directory.length() > 0) {
            this.targetDirectory = new File(directory);
        } else {
            this.targetDirectory = null;
        }
    }

    /**
     * Sets the target directory.
     */
    public void setTargetDirectory(File directory) {
        this.targetDirectory = directory;
    }

    /**
     * @return the classpath
     */
    public List<String> getClasspath() {
        return this.classpath;
    }

    /**
     * Sets the classpath.
     */
    public void setClasspath(String classpath) {
        this.classpath = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            this.classpath.add(tokenizer.nextToken());
        }
    }

    /**
     * sets the classpath using a list of Strings
     * @param parts list of strings containing the classpath parts
     */
    public void setClasspathList(List<String> parts) {
        this.classpath = new LinkedList<String>(parts);
    }

    /**
     * Returns true if verbose operation has been requested.
     */
    public boolean getVerbose() {
        return this.verbose;
    }

    /**
     * Turns verbose operation on or off.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Returns true if debugging operation has been requested.
     */
    public boolean getDebug() {
        return this.debug;
    }

    /**
     * Turns debugging operation on or off.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Returns true if parameter metadata generation has been enabled.
     */
    public boolean getParameters() {
        return this.parameters;
    }

    /**
     * Turns parameter metadata generation on or off.
     */
    public void setParameters(boolean parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the requested error tolerance.
     */
    public int getTolerance() {
        return this.tolerance;
    }

    /**
     * Sets the error tolerance, which is the number of
     * non-fatal errors (per unit) that should be tolerated before
     * compilation is aborted.
     */
    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Gets the name of the base class for scripts.  It must be a subclass
     * of Script.
     */
    public String getScriptBaseClass() {
        return this.scriptBaseClass;
    }

    /**
     * Sets the name of the base class for scripts.  It must be a subclass
     * of Script.
     */
    public void setScriptBaseClass(String scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass;
    }

    public ParserPluginFactory getPluginFactory() {
        if (pluginFactory == null) {
            /* GRECLIPSE edit
            pluginFactory = ParserVersion.V_2 == parserVersion
                                ? ParserPluginFactory.antlr2()
                                : ParserPluginFactory.antlr4(this);
            */
            pluginFactory = new ParserPluginFactory() {
                @Override
                public ParserPlugin createParserPlugin() {
                    return !Boolean.getBoolean("groovy.antlr4")
                        ? ParserPluginFactory.antlr2().createParserPlugin()
                        : ParserPluginFactory.antlr4(CompilerConfiguration.this).createParserPlugin();
                }
            };
            // GRECLIPSE end
        }
        return pluginFactory;
    }

    public void setPluginFactory(ParserPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    public void setScriptExtensions(Set<String> scriptExtensions) {
        if(scriptExtensions == null) scriptExtensions = new LinkedHashSet<String>();
        this.scriptExtensions = scriptExtensions;
    }

    public Set<String> getScriptExtensions() {
        if(scriptExtensions == null || scriptExtensions.isEmpty()) {
            /*
             *  this happens
             *  *    when groovyc calls FileSystemCompiler in forked mode, or
             *  *    when FileSystemCompiler is run from the command line directly, or
             *  *    when groovy was not started using groovyc or FileSystemCompiler either
             */
            scriptExtensions = SourceExtensionHandler.getRegisteredExtensions(
                    this.getClass().getClassLoader());
        }
        return scriptExtensions;
    }

    public String getDefaultScriptExtension() {
        return defaultScriptExtension;
    }

    public void setDefaultScriptExtension(String defaultScriptExtension) {
        this.defaultScriptExtension = defaultScriptExtension;
    }

    public void setRecompileGroovySource(boolean recompile) {
        recompileGroovySource = recompile;
    }

    public boolean getRecompileGroovySource(){
        return recompileGroovySource;
    }

    public void setMinimumRecompilationInterval(int time) {
        minimumRecompilationInterval = Math.max(0,time);
    }

    public int getMinimumRecompilationInterval() {
        return minimumRecompilationInterval;
    }

    /**
     * Sets the bytecode compatibility level. The parameter can take one of the values
     * in {@link #ALLOWED_JDKS}.
     *
     * @param version the bytecode compatibility level
     */
    public void setTargetBytecode(String version) {
        setTargetBytecodeIfValid(version);
    }

    private void setTargetBytecodeIfValid(String version) {
        if (JDK_TO_BYTECODE_VERSION_MAP.containsKey(version)) {
            this.targetBytecode = version;
        }
    }

    /**
     * Retrieves the compiler bytecode compatibility level. Defaults to the minimum
     * officially supported bytecode version for any particular Groovy version.
     *
     * @return bytecode compatibility level
     */
    public String getTargetBytecode() {
        return this.targetBytecode;
    }

    /**
     * Whether the bytecode version has preview features enabled (JEP 12)
     *
     * @return preview features
     */
    public boolean isPreviewFeatures() {
        return previewFeatures;
    }

    /**
     * Sets whether the bytecode version has preview features enabled (JEP 12).
     *
     * @param previewFeatures whether to support preview features
     */
    public void setPreviewFeatures(boolean previewFeatures) {
        this.previewFeatures = previewFeatures;
    }

    private static String getMinBytecodeVersion() {
        return JDK8;
    }

    /**
     * Gets the joint compilation options for this configuration.
     * @return the options
     */
    public Map<String, Object> getJointCompilationOptions() {
        return jointCompilationOptions;
    }

    /**
     * Sets the joint compilation options for this configuration.
     * Using null will disable joint compilation.
     * @param options the options
     */
    public void setJointCompilationOptions(Map<String, Object> options) {
        jointCompilationOptions = options;
    }

    /**
     * Gets the optimization options for this configuration.
     * @return the options (always not null)
     */
    public Map<String, Boolean> getOptimizationOptions() {
        return optimizationOptions;
    }

    /**
     * Sets the optimization options for this configuration.
     * No entry or a true for that entry means to enable that optimization,
     * a false means the optimization is disabled.
     * Valid keys are "all" and "int".
     * @param options the options.
     * @throws IllegalArgumentException if the options are null
     */
    public void setOptimizationOptions(Map<String, Boolean> options) {
        if (options == null) throw new IllegalArgumentException("provided option map must not be null");
        optimizationOptions = options;
    }

    /**
     * Adds compilation customizers to the compilation process. A compilation customizer is a class node
     * operation which performs various operations going from adding imports to access control.
     * @param customizers the list of customizers to be added
     * @return this configuration instance
     */
    public CompilerConfiguration addCompilationCustomizers(CompilationCustomizer... customizers) {
        if (customizers == null) throw new IllegalArgumentException("provided customizers list must not be null");
        compilationCustomizers.addAll(Arrays.asList(customizers));
        return this;
    }

    /**
     * Returns the list of compilation customizers.
     * @return the customizers (always not null)
     */
    public List<CompilationCustomizer> getCompilationCustomizers() {
        return compilationCustomizers;
    }

    /**
     * Returns the list of disabled global AST transformation class names.
     * @return a list of global AST transformation fully qualified class names
     */
    public Set<String> getDisabledGlobalASTTransformations() {
        return disabledGlobalASTTransformations;
    }

    /**
     * Disables the specified global AST transformations. In order to avoid class loading side effects,
     * it is not recommended to use MyASTTransformation.class.getName() but instead directly use the class
     * name as a string. Disabled AST transformations only apply to automatically loaded global AST
     * transformations, that is to say transformations defined in a
     * META-INF/services/org.codehaus.groovy.transform.ASTTransformation file.
     * If you explicitly add a global AST transformation in your compilation process,
     * for example using the {@link org.codehaus.groovy.control.customizers.ASTTransformationCustomizer} or
     * using a {@link org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation},
     * then nothing will prevent the transformation from being loaded.
     *
     * @param disabledGlobalASTTransformations a set of fully qualified class names of global AST transformations
     * which should not be loaded.
     */
    public void setDisabledGlobalASTTransformations(final Set<String> disabledGlobalASTTransformations) {
        this.disabledGlobalASTTransformations = disabledGlobalASTTransformations;
    }

    public BytecodeProcessor getBytecodePostprocessor() {
        return bytecodePostprocessor;
    }

    public void setBytecodePostprocessor(final BytecodeProcessor bytecodePostprocessor) {
        this.bytecodePostprocessor = bytecodePostprocessor;
    }

    /* GRECLIPSE edit
    public ParserVersion getParserVersion() {
        return this.parserVersion;
    }

    public void setParserVersion(ParserVersion parserVersion) {
        this.parserVersion = parserVersion;
    }
    */

    /**
     * Checks if invoke dynamic is enabled.
     */
    public boolean isIndyEnabled() {
        Boolean indyEnabled = getOptimizationOptions().get(INVOKEDYNAMIC);
        return Optional.ofNullable(indyEnabled).orElse(Boolean.FALSE).booleanValue();
    }

    /**
     * Check if groovydoc is enabled.
     */
    public boolean isGroovydocEnabled() {
        Boolean groovydocEnabled = getOptimizationOptions().get(GROOVYDOC);
        return Optional.ofNullable(groovydocEnabled).orElse(Boolean.FALSE).booleanValue();
    }

    /**
     * Check if runtime groovydoc is enabled.
     */
    public boolean isRuntimeGroovydocEnabled() {
        Boolean runtimeGroovydocEnabled = getOptimizationOptions().get(RUNTIME_GROOVYDOC);
        return Optional.ofNullable(runtimeGroovydocEnabled).orElse(Boolean.FALSE).booleanValue();
    }

    /**
     * Check if in-memory stub creation is enabled.
     */
    public boolean isMemStubEnabled() {
        Object memStubEnabled = getJointCompilationOptions().get(MEM_STUB);
        return Optional.ofNullable(memStubEnabled).map(value -> "true".equals(value.toString())).orElse(Boolean.FALSE).booleanValue();
    }
}
