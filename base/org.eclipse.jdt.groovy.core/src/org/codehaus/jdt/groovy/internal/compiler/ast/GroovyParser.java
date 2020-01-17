/*
 * Copyright 2009-2020 the original author or authors.
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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

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
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
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
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;
import org.eclipse.jdt.internal.core.builder.BuildNotifier;
import org.eclipse.jdt.internal.core.builder.SourceFile;

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

    //--------------------------------------------------------------------------

    public GroovyParser(CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean allowTransforms, boolean isReconcile) {
        this(null, compilerOptions, problemReporter, allowTransforms, isReconcile);
    }

    public GroovyParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean allowTransforms, boolean isReconcile) {
        this.requestor = requestor;
        this.compilerOptions = compilerOptions;
        this.problemReporter = problemReporter;

        // 2011-10-18: Status of transforms and reconciling
        // Prior to 2.6.0 all transforms were turned OFF for reconciling, and by turned off that meant no phase
        // processing for them was done at all. With 2.6.0 this phase processing is now active during reconciling
        // but it is currently limited to only allowing the Grab (global) transform to run. (Not sure why Grab
        // is a global transform... isn't it always annotation driven). Non-global transforms are all off.
        // This means the transformLoader is setup for the compilation unit but the cu is also told the
        // allowTransforms setting so it can decide what should be allowed through.
        // ---
        // Basic grab support: the design here is that a special classloader is created that will be augmented
        // with URLs when grab processing is running. This classloader is used as a last resort when resolving
        // types and is *only* called if a grab has occurred somewhere during compilation.
        // Currently it is not cached but created each time - we'll have to decide if there is a need to cache

        GroovyClassLoaderFactory loaderFactory = new GroovyClassLoaderFactory(compilerOptions, requestor);

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
        compilationUnit = null;
        resolver = null;
    }

    public GroovyCompilationUnitDeclaration dietParse(ICompilationUnit iCompilationUnit, CompilationResult compilationResult) {
        String fileName = String.valueOf(iCompilationUnit.getFileName());
        final IPath filePath = new Path(fileName);
        final IFile eclipseFile;
        final boolean isScript;
        // try to turn this into a 'real' absolute file system reference (this is because Grails 1.5 expects it)
        // GRECLIPSE-1269 ensure get plugin is not null to ensure the workspace is open (ie- not in batch mode)
        // needs 2 segments: a project and file name or eclipse throws assertion failed here
        if (filePath.segmentCount() > 1 && ResourcesPlugin.getPlugin() != null) {
            eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
            IPath location = eclipseFile.getLocation();
            if (location != null) {
                fileName = location.toFile().getAbsolutePath();
            }
            isScript = isScript(eclipseFile, compilerOptions.groovyProjectName);
        } else {
            eclipseFile = null;
            isScript = false;
        }

        if (problemReporter.referenceContext == null) {
            problemReporter.referenceContext = new ReferenceContextImpl(compilationResult);
        }

        if (compilationUnit == null) {
            if (isScript || (eclipseFile != null && eclipseFile.getProject().isAccessible() &&
                    !JavaCore.create(eclipseFile.getProject()).isOnClasspath(eclipseFile))) {
                compilerOptions.groovyCompilerConfigScript = null;
            }
            compilationUnit = unitFactory.get();
        }

        char[] sourceCode = Optional.ofNullable(iCompilationUnit.getContents()).orElse(CharOperation.NO_CHAR);
        SourceUnit sourceUnit = new EclipseSourceUnit(eclipseFile, fileName, sourceCode,
            compilationUnit.getConfiguration(), compilationUnit.getClassLoader(), new GroovyErrorCollectorForJDT(compilationUnit.getConfiguration()), resolver);

        compilationUnit.addSource(sourceUnit);

        if (requestor instanceof Compiler) {
            Compiler compiler = (Compiler) requestor;
            if (compiler.requestor instanceof AbstractImageBuilder) {
                AbstractImageBuilder builder = (AbstractImageBuilder) compiler.requestor;
                if (builder.notifier != null) {
                    compilationUnit.setProgressListener(new ProgressListenerImpl(builder.notifier));
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

        compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(sourceCode); // TODO: Get from Antlr

        GroovyCompilationUnitDeclaration gcuDeclaration = new GroovyCompilationUnitDeclaration(
            problemReporter, compilationResult, sourceCode.length, compilationUnit, sourceUnit, compilerOptions);

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

    /**
     * Determines if file matches any groovy script filter in the project.
     */
    private static boolean isScript(IFile sourceFile, String projectName) {
        if (projectName != null) {
            assert projectName == sourceFile.getProject().getName();
            ScriptFolderSelector scriptFolderSelector = scriptFolderSelectorCache
                .computeIfAbsent(projectName, key -> new ScriptFolderSelector(sourceFile.getProject()));
            return scriptFolderSelector.isScript(sourceFile);
        }
        return false;
    }

    /**
     * ProgressListener is called back when parsing of a file or generation of a classfile completes. By calling back to the build
     * notifier we ignore those long pauses where it look likes it has hung!
     *
     * Note: this does not move the progress bar, it merely updates the text
     */
    private static class ProgressListenerImpl implements ProgressListener {

        private BuildNotifier notifier;

        ProgressListenerImpl(BuildNotifier notifier) {
            this.notifier = notifier;
        }

        @Override
        public void parseComplete(int phase, String sourceUnitName) {
            try {
                // Chop it down to the containing package folder
                int lastSlash = sourceUnitName.lastIndexOf("/");
                if (lastSlash == -1) {
                    lastSlash = sourceUnitName.lastIndexOf("\\");
                }
                if (lastSlash != -1) {
                    StringBuffer msg = new StringBuffer();
                    msg.append("Parsing groovy source in ");
                    msg.append(sourceUnitName, 0, lastSlash);
                    notifier.subTask(msg.toString());
                }
            } catch (Exception e) {
                // doesn't matter
            }
            notifier.checkCancel();
        }

        @Override
        public void generateComplete(int phase, ClassNode classNode) {
            try {
                String pkgName = classNode.getPackageName();
                if (pkgName != null && pkgName.length() > 0) {
                    StringBuffer msg = new StringBuffer();
                    msg.append("Generating groovy classes in ");
                    msg.append(pkgName);
                    notifier.subTask(msg.toString());
                }
            } catch (Exception e) {
                // doesn't matter
            }
            notifier.checkCancel();
        }
    }

    private static class ReferenceContextImpl implements ReferenceContext {

        private boolean hasErrors;
        private final CompilationResult compilationResult;

        ReferenceContextImpl(CompilationResult compilationResult) {
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
        public void tagAsHavingIgnoredMandatoryErrors(int problemId) {
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
        public void abort(int abortLevel, CategorizedProblem problem) {
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
