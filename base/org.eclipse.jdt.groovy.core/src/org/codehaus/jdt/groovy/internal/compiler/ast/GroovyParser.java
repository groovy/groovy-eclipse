/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.env.IDependent.JAR_FILE_ENTRY_SEPARATOR;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilationUnit.ProgressListener;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.control.EclipseSourceUnit;
import org.codehaus.jdt.groovy.integration.internal.GroovyLanguageSupport;
import org.codehaus.jdt.groovy.internal.compiler.GroovyClassLoaderFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.CharArraySequence;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ReadManager;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.BatchCompilerRequestor;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;
import org.eclipse.jdt.internal.core.builder.BuildNotifier;
import org.eclipse.jdt.internal.core.builder.SourceFile;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * The mapping layer between the groovy parser and the JDT. This class communicates
 * with the groovy parser and translates results back for JDT to consume.
 */
public class GroovyParser {

    public Object requestor;
    private JDTResolver resolver;
    public final ProblemReporter problemReporter;
    public static IGroovyDebugRequestor debugRequestor;
    private final Supplier<CompilationUnit> unitFactory;

    private CompilationUnit compilationUnit;
    private CompilerOptions compilerOptions;

    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    private static Map<String, ScriptFolderSelector> scriptFolderSelectorCache = new ConcurrentHashMap<>();

    public static void clearCache(String projectName) {
        scriptFolderSelectorCache.remove(projectName);
        GroovyClassLoaderFactory.clearCache(projectName);
    }

    public static char[] getContents(ICompilationUnit compilationUnit, /*@Nullable*/ ReadManager readManager) {
        return Optional.ofNullable(readManager != null ? readManager.getContents(compilationUnit) : compilationUnit.getContents()).orElse(CharOperation.NO_CHAR);
    }

    public static boolean isGroovyParserEligible(ICompilationUnit compilationUnit, /*@Nullable*/ ReadManager readManager) {
        if (compilationUnit instanceof BasicCompilationUnit) {
            if (ContentTypeUtils.isGroovyLikeFileName(
                    ((BasicCompilationUnit) compilationUnit).sourceName)) {
                return true;
            }
        } else if (compilationUnit instanceof PossibleMatch) {
            return ((PossibleMatch) compilationUnit).isInterestingSourceFile();
        }

        String fileName = CharOperation.charToString(compilationUnit.getFileName());
        if (ContentTypeUtils.isGroovyLikeFileName(fileName)) {
            return true;
        }

        if (!ContentTypeUtils.isJavaLikeButNotGroovyLikeFileName(fileName)) {
            final char[] contents = getContents(compilationUnit, readManager);
            if (GROOVY_SOURCE_DISCRIMINATOR.matcher(new CharArraySequence(contents)).find()) {
                return true;
            }
        }

        return false;
    }

    private static final Pattern GROOVY_SOURCE_DISCRIMINATOR = Pattern.compile("\\A(/\\*.*?\\*/\\s*)?package\\s+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:\\s*\\.\\s*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*\\s++(?!;)", Pattern.DOTALL);

    private static final Pattern STC_EXTENSION_DISCRIMINATOR = Pattern.compile("^(?:unresolvedVariable|unresolvedProperty|unresolvedAttribute|methodNotFound|ambiguousMethods|onMethodSelection|incompatibleAssignment|incompatibleReturnType|(?:before|after)(?:MethodCall|VisitMethod|VisitClass))\\b", Pattern.MULTILINE);

    //--------------------------------------------------------------------------

    public GroovyParser(CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean allowTransforms, boolean isReconcile) {
        this(null, compilerOptions, problemReporter, allowTransforms, isReconcile);
    }

    public GroovyParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean allowTransforms, boolean isReconcile) {
        this.requestor = requestor;
        this.compilerOptions = compilerOptions;
        this.problemReporter = problemReporter;

        GroovyClassLoaderFactory loaderFactory = new GroovyClassLoaderFactory(compilerOptions, requestor instanceof Compiler ? ((Compiler) requestor).lookupEnvironment : null);

        this.unitFactory = () -> {
            CompilerConfiguration compilerConfiguration = GroovyLanguageSupport.newCompilerConfiguration(compilerOptions, problemReporter);
            GroovyClassLoader[] classLoaders = loaderFactory.getGroovyClassLoaders(compilerConfiguration);
            // https://github.com/groovy/groovy-eclipse/issues/814
            if (allowTransforms && isReconcile) {
                // disable Spock transform until inferencing supports it
                Set<String> xforms = new HashSet<>();
                xforms.add("org.spockframework.compiler.SpockTransform");
                Optional.ofNullable(
                    compilerConfiguration.getDisabledGlobalASTTransformations()
                ).ifPresent(xforms::addAll);
                compilerConfiguration.setDisabledGlobalASTTransformations(xforms);
            }

            CompilationUnit unit = new CompilationUnit(
                compilerConfiguration,
                null, // CodeSource
                classLoaders[0],
                classLoaders[1],
                allowTransforms,
                null);
            this.resolver = new JDTResolver(unit);
            unit.setResolveVisitor(resolver);
            unit.tweak(isReconcile);
            return unit;
        };
    }

    public void reset() {
        try {
            if (compilationUnit != null && compilerOptions.groovyProjectName == null) {
                compilationUnit.getTransformLoader().close();
                compilationUnit.getClassLoader().close();
            }
        } catch (Exception ignore) {
            // e.printStackTrace();
        } finally {
            compilationUnit = null;
            resolver = null;
        }
    }

    //--------------------------------------------------------------------------

    public GroovyCompilationUnitDeclaration dietParse(final char[] contents, String fileName, final CompilationResult compilationResult) {
        boolean isInJar = fileName.indexOf(JAR_FILE_ENTRY_SEPARATOR) > 0;
        boolean isScript = false;
        IFile eclipseFile = null;
        if (!isInJar) {
            // try to convert fileName into an absolute filesystem reference
            IPath filePath = new Path(fileName);
            // needs 2 segments (project and file names) or eclipse throws assertion failed
            // GRECLIPSE-1269: ensure the workspace is available (i.e. not in batch mode)
            if (filePath.segmentCount() >= 2 && ResourcesPlugin.getPlugin() != null) {
                eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
                IPath location = eclipseFile.getLocation();
                if (location != null) {
                    fileName = location.toFile().getAbsolutePath();
                    org.eclipse.core.resources.IProject project = eclipseFile.getProject();
                    isScript = scriptFolderSelectorCache.computeIfAbsent(project.getName(), key -> new ScriptFolderSelector(project)).isScript(eclipseFile);
                }
            }
        }

        if (problemReporter.referenceContext == null) {
            problemReporter.referenceContext = new ReferenceContextImpl(compilationResult);
        }

        if (compilationUnit == null) {
            if (isInJar || isScript || (eclipseFile != null && eclipseFile.getProject().isAccessible() &&
                                    !JavaCore.create(eclipseFile.getProject()).isOnClasspath(eclipseFile))){
                if (isScript && STC_EXTENSION_DISCRIMINATOR.matcher(new CharArraySequence(contents)).find())
                    compilerOptions.buildGroovyFiles |= 4; // need type-checking script config
                compilerOptions.groovyCompilerConfigScript = null;
            }
            compilationUnit = unitFactory.get();
        }

        SourceUnit sourceUnit = new EclipseSourceUnit(eclipseFile, fileName, contents,
            compilationUnit.getConfiguration(), compilationUnit.getClassLoader(), new GroovyErrorCollectorForJDT(compilationUnit.getConfiguration()), resolver);

        compilationUnit.addSource(sourceUnit);

        if (requestor instanceof Compiler) {
            Compiler compiler = (Compiler) requestor;
            if (compiler.requestor instanceof AbstractImageBuilder) {
                AbstractImageBuilder builder = (AbstractImageBuilder) compiler.requestor;
                if (builder.notifier != null) {
                    compilationUnit.setProgressListener(newProgressListener(builder.notifier));
                }
                if (eclipseFile != null) {
                    SourceFile sourceFile = (SourceFile) builder.fromIFile(eclipseFile);
                    if (sourceFile != null) {
                        compilationUnit.getConfiguration().setTargetDirectory(sourceFile.getOutputLocation().toFile());
                    }
                }
            } else if (compiler.requestor instanceof BatchCompilerRequestor) {
                Main main = ReflectionUtils.getPrivateField(BatchCompilerRequestor.class, "compiler", compiler.requestor);
                if (main != null && main.destinationPath != null && main.destinationPath != Main.NONE) {
                    compilationUnit.getConfiguration().setTargetDirectory(main.destinationPath);
                }
            }
        }

        compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(contents); // TODO: Get from Antlr

        GroovyCompilationUnitDeclaration gcuDeclaration = new GroovyCompilationUnitDeclaration(
            problemReporter, compilationResult, contents.length, compilationUnit, sourceUnit, compilerOptions);

        gcuDeclaration.processToPhase(Phases.CONVERSION);

        // ModuleNode is null when there is a fatal error
        if (gcuDeclaration.getModuleNode() != null) {
            gcuDeclaration.populateCompilationUnitDeclaration();
            for (TypeDeclaration decl : gcuDeclaration.types) {
                resolver.record((GroovyTypeDeclaration) decl);
            }
        }
        // remember scripts so that class file output can be suppressed
        if (isScript) {
            gcuDeclaration.tagAsScript();
        }
        if (debugRequestor != null) {
            debugRequestor.acceptCompilationUnitDeclaration(gcuDeclaration);
        }
        return gcuDeclaration;
    }

    private static IFile getWorkspaceFile(final String filePath) {
        return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filePath));
    }

    /**
     * ProgressListener is called when parsing of a source unit or generation of
     * a class file completes.  By calling back to the build notifier we prevent
     * those long pauses where it looks like compilation has stalled.
     * <p>
     * Note: this does not move the progress bar, it merely updates the text
     */
    private static ProgressListener newProgressListener(final BuildNotifier notifier) {
        return new ProgressListener() {
            @Override
            public void parseComplete(final int phase, final String sourceUnitName) {
                try {
                    IFile sourceUnitFile = getWorkspaceFile(sourceUnitName);
                    notifier.subTask("Parsing groovy sources in " + sourceUnitFile.getParent().getFullPath());
                } catch (Exception ignore) {
                }
                notifier.checkCancel();
            }

            @Override
            public void generateComplete(final int phase, final ClassNode classNode) {
                try {
                    IFile sourceUnitFile = getWorkspaceFile(classNode.getModule().getContext().getName());
                    notifier.subTask("Writing groovy classes for " + sourceUnitFile.getParent().getFullPath());
                } catch (Exception ignore) {
                }
                notifier.checkCancel();
            }
        };
    }

    //--------------------------------------------------------------------------

    private static class ReferenceContextImpl implements ReferenceContext {

        private boolean hasErrors;
        private final CompilationResult compilationResult;

        ReferenceContextImpl(final CompilationResult compilationResult) {
            this.compilationResult = compilationResult;
        }

        @Override
        public boolean hasErrors() {
            return hasErrors;
        }

        @Override
        public void tagAsHavingErrors() {
            hasErrors = true;
        }

        @Override
        public void tagAsHavingIgnoredMandatoryErrors(final int problemId) {
            // no-op
        }

        @Override
        public CompilationResult compilationResult() {
            return compilationResult;
        }

        @Override
        public CompilationUnitDeclaration getCompilationUnitDeclaration() {
            return null;
        }

        @Override
        public void abort(final int abortLevel, final CategorizedProblem problem) {
            switch (abortLevel) {
            case ProblemSeverities.AbortType:
                throw new AbortType(compilationResult, problem);
            case ProblemSeverities.AbortMethod:
                throw new AbortMethod(compilationResult, problem);
            default:
                throw new AbortCompilationUnit(compilationResult, problem);
            }
        }
    }
}
