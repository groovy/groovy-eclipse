/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.compiler;

import static org.apache.commons.lang.StringUtils.join;
import static org.codehaus.groovy.eclipse.core.GroovyCore.logException;
import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;
import static org.codehaus.groovy.eclipse.core.util.MapUtil.newMap;
import static org.codehaus.groovy.eclipse.core.util.SetUtil.hashSet;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.codehaus.groovy.antlr.CSTParserPluginFactory;
import org.codehaus.groovy.antlr.ErrorRecoveredCSTParserPluginFactory;
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.ICSTReporter;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.CompilationUnit.ProgressCallback;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.SyntaxException;

import antlr.TokenStreamIOException;

/**
 * High level compiler API suitable for IDE tools. The API compile methods
 * report on all generated artifacts and errors. Specific artifacts can be
 * generated, such as CST and/or AST and/or class files.
 * 
 * The notifications sent to the {@link IGroovyCompilationReporter} can be used
 * by the IDE tools to maintain internal caches, error markers, and so on.
 * 
 * @author empovazan
 */
public class GroovyCompiler implements IGroovyCompiler {
    /**
     * Reporter that will collect CST errors when error recovery is active.
     */
    @SuppressWarnings("unchecked")
    static class CSTReporter implements ICSTReporter {
        final Map<String, GroovySourceAST> mapFileNameToCST = newMap();

        final Map<String, List<?>> mapFileNameToErrors = newMap();

        public void generatedCST(final String fileName,
                final GroovySourceAST ast) {
            mapFileNameToCST.put(fileName, ast);
        }

        public void reportErrors(final String fileName, final List errors) {
            mapFileNameToErrors.put(fileName, errors);
        }
    }

    public void compile(final String[] fileNames,
            final IGroovyCompilerConfiguration configuration,
            final IGroovyCompilationReporter reporter) {
        final CSTReporter cstReporter = new CSTReporter();
        final CompilationUnit compilationUnit = initCompile(configuration,
                cstReporter);
        compilationUnit.addSources(fileNames);
        compile(compilationUnit, configuration, reporter, cstReporter,
                fileNames);
    }

    public void compile(final String fileName,
            final IGroovyCompilerConfiguration configuration,
            final IGroovyCompilationReporter reporter) {
        compile(new String[] { fileName }, configuration, reporter);
    }

    public void compile(final String fileName, final InputStream inputStream,
            final IGroovyCompilerConfiguration configuration,
            final IGroovyCompilationReporter reporter) {
        final CSTReporter cstReporter = new CSTReporter();
        final CompilationUnit compilationUnit = initCompile(configuration,
                cstReporter);
        compilationUnit.addSource(fileName, inputStream);
        compile(compilationUnit, configuration, reporter, cstReporter, fileName);
    }

    // Copied from org.codehaus.groovy.ant.Groovyc
    protected File createTempDir() {
        try {
            final File tempFile = File.createTempFile("groovy-generated-",
                    "-java-source");
            tempFile.delete();
            tempFile.mkdirs();
            return tempFile;
        } catch (final IOException e) {
            throw new BuildException(e);
        }
    }

    // private CompilationUnit initCompile( final IGroovyCompilerConfiguration
    // configuration,
    // final CSTReporter cstReporter )
    // {
    // final ClassLoader classLoader = configuration.getClassLoader();
    // final String classPath = configuration.getClassPath();
    // final CompilerConfiguration config = createCompilerConfiguration(
    // configuration, cstReporter );
    // config.setClasspath( classPath );
    // config.setTargetDirectory( configuration.getOutputPath() );
    // final Map jointCompilationOptions = new HashMap();
    // jointCompilationOptions.put( "stubDir", createTempDir() );
    // config.setJointCompilationOptions( jointCompilationOptions );
    // final GroovyClassLoader loader;
    // if( classLoader != null )
    // loader = new GroovyClassLoader( classLoader, config, false );
    // else
    // loader = new GroovyClassLoader( null, config, true );
    //
    // return new JavaAwareCompilationUnit( config, loader );
    // }

    private CompilationUnit initCompile(
            final IGroovyCompilerConfiguration configuration,
            final CSTReporter cstReporter) {
        final ClassLoader classLoader = configuration.getClassLoader();
        final String classPath = configuration.getClassPath();
        final CompilerConfiguration config = createCompilerConfiguration(
                configuration, cstReporter);
        config.setClasspath(classPath);
        config.setTargetDirectory(configuration.getOutputPath());

        final GroovyClassLoader loader;
        if (classLoader != null)
            loader = new GroovyClassLoader(classLoader, config, false);
        else
            loader = new GroovyClassLoader(null, config, true);

        final CompilationUnit compilationUnit = new CompilationUnit(config,
                null, loader);
        return compilationUnit;
    }

    private CompilerConfiguration createCompilerConfiguration(
            final IGroovyCompilerConfiguration configuration,
            final CSTReporter cstReporter) {
        CompilerConfiguration config;
        if (configuration.isErrorRecovery()
                && (configuration.getBuildCST() || configuration.getBuildAST() || configuration
                        .isForceBuild())) {
            config = new CompilerConfiguration();
            config.setPluginFactory(new ErrorRecoveredCSTParserPluginFactory(
                    cstReporter));
        } else if (configuration.getBuildCST()) {
            config = new CompilerConfiguration();
            config.setPluginFactory(new CSTParserPluginFactory(cstReporter));
        } else {
            config = new CompilerConfiguration();
        }
        config.setOutput(new PrintWriter(System.err));
        config.setWarningLevel(WarningMessage.PARANOIA);
        return config;
    }

    private void compile(final CompilationUnit compilationUnit,
            final IGroovyCompilerConfiguration configuration,
            final IGroovyCompilationReporter reporter,
            final CSTReporter cstReporter, final String... fileNames) {
        setProgressCallback(compilationUnit);

        // CompilerConfiguration cc = new CompilerConfiguration();
        // cc.setDebug(true);
        // JavaStubCompilationUnit compilation = new JavaStubCompilationUnit(cc,
        // compilationUnit.getClassLoader(),
        // compilationUnit.getConfiguration().getTargetDirectory());
        //
        // for (int i=0;i<fileNames.length;i++){
        // compilation.addSourceFile(new File(fileNames[i]));
        // }
        // compilation.compile(Phases.CONVERSION);

        int phases = 0;

        if (configuration.getBuildCST()) {
            phases = Phases.PARSING;
        }

        if (configuration.getBuildAST()) {
            phases = Phases.SEMANTIC_ANALYSIS;
            if (configuration.getResolveAST()) {
                phases = Phases.CANONICALIZATION;
            }
            if (configuration.getUnResolvedAST()) {
                phases = Phases.CONVERSION;
            }
        }

        if (configuration.getBuildClasses()
                && (!configuration.isErrorRecovery() || configuration
                        .isForceBuild())) {
            phases = Phases.ALL;
        }

        if (phases == 0) {
            return;
        }
        Exception compileException = null;
        try {
            compilationUnit.compile(phases);
        } catch (final Exception e) {
            compileException = e;
        }

        // Now report
        if (compileException == null) {
            // There may be errors which were reported, but were recovered. They
            // are still reported.
            reportBuild(compilationUnit, configuration, fileNames, reporter,
                    cstReporter);
        } else {
            // These are fatal compile errors.
            reportErrors(reporter, configuration, fileNames, compileException);
        }
    }

    private void reportBuild(final CompilationUnit compilationUnit,
            final IGroovyCompilerConfiguration configuration,
            final String[] fileNames,
            final IGroovyCompilationReporter reporter,
            final CSTReporter cstReporter) {
        final CompileUnit ast = compilationUnit.getAST();
        final List<?> moduleList = newList(ast.getModules());
        // FUTURE: possible control of prune/no prune in the configuration.
        pruneModuleList(moduleList, fileNames);

        if (moduleList.size() > 0) {
            reportAll(compilationUnit, configuration, moduleList, fileNames,
                    reporter, cstReporter);
        } else {
            reportCSTs(configuration, reporter, cstReporter);
        }
    }

    private void reportAll(final CompilationUnit compilationUnit,
            final IGroovyCompilerConfiguration configuration,
            final List moduleList, final String[] fileNames,
            final IGroovyCompilationReporter reporter,
            final CSTReporter cstReporter) {
        final File targetDirectory = compilationUnit.getConfiguration()
                .getTargetDirectory();
        reporter.beginReporting();
        try {
            for (final Iterator iter = moduleList.iterator(); iter.hasNext();) {
                final ModuleNode moduleNode = (ModuleNode) iter.next();
                final String fileName = moduleNode.getDescription();
                final String reportFileName = fileName.replaceAll("\\\\\\\\",
                        "\\\\");

                reporter.beginReporting(reportFileName);
                try {
                    if (configuration.getBuildCST()
                            || configuration.isErrorRecovery()) {
                        // First the errors.
                        final List errors = cstReporter.mapFileNameToErrors
                                .get(fileName);
                        if (errors != null) {
                            reportCSTErrors(reportFileName, errors, reporter);
                        }

                        // Now the CST.
                        final GroovySourceAST cst = cstReporter.mapFileNameToCST
                                .get(fileName);
                        reporter.generatedCST(reportFileName, cst);
                    }

                    if (configuration.getBuildAST()) {
                        reporter.generatedAST(reportFileName, moduleNode);
                    }

                    if (configuration.getBuildClasses()) {
                        reporter.generatedClasses(reportFileName,
                                makeClassNameArray(moduleNode),
                                makeClassFilePathArray(moduleNode,
                                        targetDirectory));
                    }
                } finally {
                    reporter.endReporting(fileName);
                }
            }
        } finally {
            reporter.endReporting();
        }
    }

    private void reportCSTErrors(final String fileName, final List errors,
            final IGroovyCompilationReporter reporter) {
        for (final Iterator iter = errors.iterator(); iter.hasNext();) {
            final Map map = (Map) iter.next();
            final String error = (String) map.get("error");
            final int line = ((Integer) map.get("line")).intValue();
            final int column = ((Integer) map.get("column")).intValue();
            reporter.compilationError(fileName, line, column, column, error,
                    null);
        }
    }

    private void reportCSTs(final IGroovyCompilerConfiguration configuration,
            final IGroovyCompilationReporter reporter,
            final CSTReporter cstReporter) {
        reporter.beginReporting();
        try {
            if (configuration.getBuildCST()) {
                final Set setOfFileNames = cstReporter.mapFileNameToCST
                        .keySet();
                for (final Iterator iter = setOfFileNames.iterator(); iter
                        .hasNext();) {
                    final String fileName = (String) iter.next();
                    final String reportFileName = fileName.replaceAll(
                            "\\\\\\\\", "\\\\");

                    reporter.beginReporting(reportFileName);
                    try {
                        // First the errors.
                        final List errors = cstReporter.mapFileNameToErrors
                                .get(fileName);
                        if (errors != null) {
                            reportCSTErrors(reportFileName, errors, reporter);
                        }

                        // Now the CST.
                        final GroovySourceAST cst = cstReporter.mapFileNameToCST
                                .get(fileName);
                        if (cst != null) {
                            reporter.generatedCST(reportFileName, cst);
                        }
                    } finally {
                        reporter.endReporting(reportFileName);
                    }
                }
            }
        } finally {
            reporter.endReporting();
        }
    }

    /**
     * Remove ModuleNodes not in the list of files that were built. Given a list
     * of ModuleNodes, some of them will not be in the list of file names that
     * were built. This is because the Groovy compiler creates ModuleNodes
     * during building our files in the list. So prune the list.
     * 
     * @param moduleList
     * @param builtFileNames
     */
    private void pruneModuleList(final List moduleList,
            final String[] builtFileNames) {
        // Set fileNamesLookup = new HashSet(Arrays.asList(builtFileNames));
        final Set<File> fileNamesLookup = hashSet();
        for (final String fileName : builtFileNames)
            fileNamesLookup.add(new File(fileName));
        for (final Iterator iter = moduleList.iterator(); iter.hasNext();) {
            final ModuleNode moduleNode = (ModuleNode) iter.next();
            if (!fileNamesLookup
                    .contains(new File(moduleNode.getDescription())))
                iter.remove();
        }
    }

    /**
     * @param moduleNode
     * @return An array of fully qualified class names.
     */
    private String[] makeClassNameArray(final ModuleNode moduleNode) {
        final List classes = moduleNode.getClasses();
        final String[] ret = new String[classes.size()];

        for (int i = 0; i < ret.length; ++i) {
            ret[i] = ((ClassNode) classes.get(i)).getName();
        }

        return ret;
    }

    /**
     * Returns a list of class files generated for a specific module node.
     * 
     * @param moduleNode
     * @param targetDirectory
     *            The target directory in which the class files were generated.
     * @return Array of canonical paths to the class files.
     * @throws IOException
     */
    private String[] makeClassFilePathArray(final ModuleNode moduleNode,
            final File targetDirectory) {
        try {
            final List classes = moduleNode.getClasses();
            final List<String> ret = newList();
            final String outputPath = targetDirectory.getCanonicalPath();

            for (final Iterator iter = classes.iterator(); iter.hasNext();) {
                final ClassNode classNode = (ClassNode) iter.next();
                final String partialPath = classNode.getName().replace('.',
                        File.separatorChar);
                final String classFilePath = outputPath + File.separatorChar
                        + partialPath + ".class";
                ret.add(classFilePath);

                final String searchDirectory = classFilePath.substring(0,
                        classFilePath.lastIndexOf(File.separatorChar));
                final String classNamePrefix = classFilePath.substring(
                        classFilePath.lastIndexOf(File.separatorChar) + 1,
                        classFilePath.lastIndexOf('.')) + '$';
                final File[] files = new File(searchDirectory)
                        .listFiles(new FilenameFilter() {
                            public boolean accept(final File dir,
                                    final String name) {
                                return name.startsWith(classNamePrefix);
                            }
                        });
                for (int i = 0; i < files.length; ++i) {
                    ret.add(files[i].getCanonicalPath());
                }
            }

            return ret.toArray(new String[ret.size()]);
        } catch (final IOException e) {
            throw new RuntimeException("Error getting canonical paths.");
        }
    }

    private void setProgressCallback(final CompilationUnit unit) {
        unit.setProgressCallback(new ProgressCallback() {
            @Override
            public void call(final ProcessingUnit context, final int phase)
                    throws CompilationFailedException {
                switch (phase) {
                    case Phases.PARSING:
                        break;
                    case Phases.CANONICALIZATION:
                        break;
                    case Phases.CLASS_GENERATION:
                        break;
                }
            }
        });
    }

    /**
     * Handle compilation errors. This are the errors collected while compiling
     * the source files in the file list. Occasionally the exception is an
     * internal compiler error.
     * 
     * @param reporter
     * @param configuration
     * 
     * @param fileNames
     * @param e
     */
    private void reportErrors(final IGroovyCompilationReporter reporter,
            final IGroovyCompilerConfiguration configuration,
            final String[] fileNames, final Exception e) {
        // TODO: emp - some files may have been built before errors. These
        // should be reported.
        // How to do this? Perhaps collect info in the ProgressCallback?
        if (e instanceof MultipleCompilationErrorsException) {
            final Map mapFileNamesToExceptions = mapFileNamesToExceptions(
                    fileNames, (MultipleCompilationErrorsException) e);
            reportErrors(reporter, mapFileNamesToExceptions);
            return;
        }
        // TODO: emp - possible to get name of the actual file that was
        // being compiled?
        // Perhaps by catching the current file being built in the progress
        // call back, but
        // how to link it to the exception?
        logException("Error compiling " + join(fileNames, ", "), e);
    }

    /**
     * @param fileNames
     * @param e
     * @return A mapping from a file name to a list of exceptions.
     */
    @SuppressWarnings("unchecked")
    private static Map mapFileNamesToExceptions(final String[] fileNames,
            final MultipleCompilationErrorsException e) {
        final Map<String, List<Exception>> mapFileNamesToExceptions = newMap();
        final ErrorCollector collector = e.getErrorCollector();
        for (int i = 0; i < collector.getErrorCount(); i++) {
            final Exception exception = collector.getException(i);
            final String fileName = getFilenameFromException(exception);
            if (StringUtils.isNotBlank(fileName)) {
                addFileToExceptionMap(fileName, exception,
                        mapFileNamesToExceptions);
                continue;
            }
            // file is null if the file with errors is an external file, and not
            // one in the change set.
            // TODO: emp - it the above statement true? AFAIK, it is because of
            // an internal compiler error. And if the above
            // is true, there should be some sort of notification.
            final SourceUnit[] units = getOwnersFromMessage(collector
                    .getError(i));
            for (int j = 0; j < units.length; j++) {
                final SourceUnit unit = units[j];
                addFileToExceptionMap(unit.getName().replaceAll("\\\\\\\\",
                        "\\\\"), exception, mapFileNamesToExceptions);
            }
        }
        return mapFileNamesToExceptions;
    }

    private static void addFileToExceptionMap(final String fileName,
            final Exception exception,
            final Map<String, List<Exception>> mapFileNamesToExceptions) {
        if (StringUtils.isBlank(fileName))
            return;
        if (!mapFileNamesToExceptions.containsKey(fileName))
            mapFileNamesToExceptions.put(fileName, newList(new Exception[0]));
        mapFileNamesToExceptions.get(fileName).add(exception);
    }

    @SuppressWarnings("unchecked")
    private static SourceUnit[] getOwnersFromMessage(final Message message) {
        try {
            if (message instanceof SyntaxErrorMessage) {
                final Field field = message.getClass().getDeclaredField(
                        "source");
                field.setAccessible(true);
                final SourceUnit sourceUnit = (SourceUnit) field.get(message);
                if (sourceUnit == null)
                    return new SourceUnit[0];
                return new SourceUnit[] { sourceUnit };
            }
            final Field field = message.getClass().getDeclaredField("owner");
            field.setAccessible(true);
            final ProcessingUnit processingUnit = (ProcessingUnit) field
                    .get(message);
            if (processingUnit == null)
                return new SourceUnit[0];
            if (processingUnit instanceof SourceUnit)
                return new SourceUnit[] { (SourceUnit) processingUnit };
            // If not a SourceUnit, it should be a CompilationUnit
            final CompilationUnit compilationUnit = (CompilationUnit) processingUnit;
            final List<SourceUnit> sources = new ArrayList<SourceUnit>();
            final Iterator<SourceUnit> iterator = compilationUnit.iterator();
            while (iterator.hasNext())
                sources.add(iterator.next());
            return sources.toArray(new SourceUnit[0]);
        } catch (final SecurityException e) {
            GroovyCore.logException(e.getMessage(), e);
        } catch (final NoSuchFieldException e) {
            GroovyCore.logException(e.getMessage(), e);
        } catch (final IllegalArgumentException e) {
            GroovyCore.logException(e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            GroovyCore.logException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param reporter
     * @param mapFileNamesToExceptions
     *            Map from a file name to a list of compilation error exceptions
     *            for the file name.
     */
    private void reportErrors(final IGroovyCompilationReporter reporter,
            final Map mapFileNamesToExceptions) {
        for (final Iterator iter = mapFileNamesToExceptions.keySet().iterator(); iter
                .hasNext();) {
            final String fileName = (String) iter.next();
            final List exceptions = (List) mapFileNamesToExceptions
                    .get(fileName);
            reporter.beginReporting(fileName);
            try {
                for (final Iterator iterException = exceptions.iterator(); iterException
                        .hasNext();) {
                    final Exception exception = (Exception) iterException
                            .next();
                    if (exception == null)
                        continue;
                    reportError(reporter, fileName, exception);
                }
            } finally {
                reporter.endReporting(fileName);
            }
        }
    }

    private static String getFilenameFromException(final Exception exception) {
        String filename = "";
        if (exception instanceof SyntaxException)
            filename = ((SyntaxException) exception).getSourceLocator();
        else if (exception instanceof RuntimeParserException)
            filename = ((RuntimeParserException) exception).getModule()
                    .getDescription();

        if (StringUtils.isNotBlank(filename)) {
            // Replaces double backslashes with single ones.
            // Backslash regex: \\, quoted "\\\\", two of them "\\\\\\\\".
            return filename.replaceAll("\\\\\\\\", "\\\\");
        }

        return filename;
    }

    private void reportError(final IGroovyCompilationReporter reporter,
            final String fileName, final Exception exception) {
        int line, startCol, endCol;
        String message;
        StringWriter traceWriter = new StringWriter();

        if (exception instanceof SyntaxException) {
            final SyntaxException se = (SyntaxException) exception;
            line = se.getLine();
            startCol = se.getStartColumn();
            endCol = se.getEndColumn();
            message = exception.getMessage();
            se.printStackTrace(new PrintWriter(traceWriter));
        } else if (exception instanceof RuntimeParserException
                && ((RuntimeParserException) exception).getNode() != null) {
            final RuntimeParserException rex = (RuntimeParserException) exception;

            // Need to extract info from the node.
            final ASTNode node = rex.getNode();
            line = node.getLineNumber();
            startCol = node.getColumnNumber();
            endCol = node.getLastColumnNumber();
            message = rex.getMessageWithoutLocationText();

            // After newline is the AST node where the error was found.
            final int ix = message.indexOf("\n");
            if (ix != -1)
                message = message.substring(0, ix);

            traceWriter = new StringWriter();
            rex.printStackTrace(new PrintWriter(traceWriter));
        } else if (exception instanceof TokenStreamIOException) {
            // Handle an internal compiler exception.
            message = exception.getMessage();
            line = startCol = endCol = -1;
            final Pattern pattern = Pattern
                    .compile("\\w+:\\s*(\\d+)\\s*\\w+:\\s*(\\d+)\\s*");
            final Matcher matcher = pattern.matcher(message);
            if (matcher.find() && matcher.groupCount() == 2) {
                line = Integer.parseInt(matcher.group(1));
                startCol = Integer.parseInt(matcher.group(2));
                endCol = startCol + 1;
            }
            exception.printStackTrace(new PrintWriter(traceWriter));
        } else {
            line = startCol = endCol = -1;
            message = exception.getMessage();
            exception.printStackTrace(new PrintWriter(traceWriter));
        }

        reporter.compilationError(fileName, line, startCol, endCol, message,
                traceWriter.toString());
    }
}