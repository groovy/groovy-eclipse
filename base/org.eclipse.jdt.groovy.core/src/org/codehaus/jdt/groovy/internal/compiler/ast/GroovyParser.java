/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilationUnit.ProgressListener;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.control.EclipseSourceUnit;
import org.codehaus.jdt.groovy.integration.internal.GroovyLanguageSupport;
import org.codehaus.jdt.groovy.internal.compiler.GroovyClassLoaderFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.CompilerUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.builder.BatchImageBuilder;
import org.eclipse.jdt.internal.core.builder.BuildNotifier;

/**
 * The mapping layer between the groovy parser and the JDT. This class communicates
 * with the groovy parser and translates results back for JDT to consume.
 */
public class GroovyParser {

    public Object requestor;
    private JDTResolver resolver;
    public final ProblemReporter problemReporter;
    public static IGroovyDebugRequestor debugRequestor;
    private final GroovyClassLoaderFactory loaderFactory;

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
        // FIXASC review callers who pass null for options
        // FIXASC set parent of the loader to system or context class loader?

        // record any paths we use for a project so that when the project is cleared,
        // the paths (which point to cached classloaders) can be cleared

        this.requestor = requestor;
        this.compilerOptions = compilerOptions;
        this.problemReporter = problemReporter;
        this.loaderFactory = new GroovyClassLoaderFactory(compilerOptions, requestor);

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

        compilationUnit = newCompilationUnit(isReconcile, allowTransforms);
    }

    public void reset() {
        compilationUnit = newCompilationUnit(compilationUnit.isReconcile, compilationUnit.allowTransforms);
    }

    private CompilationUnit newCompilationUnit(boolean isReconcile, boolean allowTransforms) {
        CompilerConfiguration compilerConfiguration = GroovyLanguageSupport.newCompilerConfiguration(compilerOptions, problemReporter);
        GroovyClassLoader[] classLoaders = loaderFactory.getGroovyClassLoaders(compilerConfiguration);
        CompilationUnit cu = new CompilationUnit(
            compilerConfiguration,
            null, // CodeSource
            classLoaders[0],
            classLoaders[1],
            allowTransforms,
            compilerOptions.groovyExcludeGlobalASTScan);
        this.resolver = new JDTResolver(cu);
        cu.removeOutputPhaseOperation();
        cu.setResolveVisitor(resolver);
        cu.tweak(isReconcile);

        // GRAILS add
        if (allowTransforms && compilerOptions != null && (compilerOptions.groovyFlags & CompilerUtils.IsGrails) != 0) {
            cu.addPhaseOperation(new GrailsInjector(classLoaders[1]), Phases.CANONICALIZATION);
            new Grails20TestSupport(compilerOptions, classLoaders[1]).addGrailsTestCompilerCustomizers(cu);
            cu.addPhaseOperation(new GrailsGlobalPluginAwareEntityInjector(classLoaders[1]), Phases.CANONICALIZATION);
        }
        // GRAILS end

        return cu;
    }

    public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
        char[] sourceCode = sourceUnit.getContents();
        if (sourceCode == null) {
            sourceCode = CharOperation.NO_CHAR; // pretend empty from thereon
        }

        ErrorCollector errorCollector = new GroovyErrorCollectorForJDT(compilationUnit.getConfiguration());
        String filepath = null;

        // This check is necessary because the filename is short (as in the last part, eg. Foo.groovy) for types coming in
        // from the hierarchy resolver. If there is the same type in two different packages then the compilation process
        // is going to go wrong because the filename is used as a key in some groovy data structures. This can lead to false
        // complaints about the same file defining duplicate types.
        char[] fileName = sourceUnit.getFileName();
        if (sourceUnit instanceof org.eclipse.jdt.internal.compiler.batch.CompilationUnit) {
            filepath = String.valueOf(((org.eclipse.jdt.internal.compiler.batch.CompilationUnit) sourceUnit).fileName);
        } else {
            filepath = String.valueOf(fileName);
        }

        IPath path = new Path(filepath);
        // Try to turn this into a 'real' absolute file system reference (this is because Grails 1.5 expects it).
        IFile eclipseFile = null;
        // GRECLIPSE-1269 ensure get plugin is not null to ensure the workspace is open (ie- not in batch mode)
        // Needs 2 segments: a project and file name or eclipse throws assertion failed here
        if (ResourcesPlugin.getPlugin() != null && path.segmentCount() >= 2) {
            eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            final IPath location = eclipseFile.getLocation();
            if (location != null) {
                filepath = location.toFile().getAbsolutePath();
            }
        }

        SourceUnit groovySourceUnit = new EclipseSourceUnit(eclipseFile, filepath, String.valueOf(sourceCode),
            compilationUnit.getConfiguration(), compilationUnit.getClassLoader(), errorCollector, this.resolver);
        groovySourceUnit.isReconcile = compilationUnit.isReconcile;
        GroovyCompilationUnitDeclaration gcuDeclaration = new GroovyCompilationUnitDeclaration(
            problemReporter, compilationResult, sourceCode.length, compilationUnit, groovySourceUnit, compilerOptions);
        // FIXASC get this from the Antlr parser
        compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(sourceCode);
        compilationUnit.addSource(groovySourceUnit);

        // Check if it is worth plugging in a callback listener for parse/generation
        if (requestor instanceof Compiler) {
            if (((Compiler) requestor).requestor instanceof BatchImageBuilder) {
                BuildNotifier notifier = ((BatchImageBuilder) ((Compiler) requestor).requestor).notifier;
                if (notifier != null) {
                    compilationUnit.setProgressListener(new ProgressListenerImpl(notifier));
                }
            }
        }
        gcuDeclaration.processToPhase(Phases.CONVERSION);

        // ModuleNode is null when there is a fatal error
        if (gcuDeclaration.getModuleNode() != null) {
            gcuDeclaration.populateCompilationUnitDeclaration();
            for (TypeDeclaration decl : gcuDeclaration.types) {
                resolver.record((GroovyTypeDeclaration) decl);
            }
        }
        String projectName = compilerOptions.groovyProjectName;
        // Is this a script? If allowTransforms is TRUE then this is a 'full build' and we should remember which are scripts so that .class file output can be suppressed
        if (projectName != null && eclipseFile != null) {
            ScriptFolderSelector scriptFolderSelector = scriptFolderSelectorCache.get(projectName);
            if (scriptFolderSelector == null) {
                scriptFolderSelector = new ScriptFolderSelector(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
                scriptFolderSelectorCache.put(projectName, scriptFolderSelector);
            }
            if (scriptFolderSelector.isScript(eclipseFile)) {
                gcuDeclaration.tagAsScript();
            }
        }
        if (debugRequestor != null) {
            debugRequestor.acceptCompilationUnitDeclaration(gcuDeclaration);
        }
        return gcuDeclaration;
    }

    /**
     * ProgressListener is called back when parsing of a file or generation of a classfile completes. By calling back to the build
     * notifier we ignore those long pauses where it look likes it has hung!
     *
     * Note: this does not move the progress bar, it merely updates the text
     */
    private static class ProgressListenerImpl implements ProgressListener {

        private BuildNotifier notifier;

        public ProgressListenerImpl(BuildNotifier notifier) {
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
}
