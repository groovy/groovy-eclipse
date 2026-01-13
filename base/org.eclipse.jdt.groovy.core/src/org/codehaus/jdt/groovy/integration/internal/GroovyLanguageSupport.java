/*
 * Copyright 2009-2026 the original author or authors.
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
package org.codehaus.jdt.groovy.integration.internal;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import org.apache.xbean.classloader.MultiParentClassLoader;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.jdt.groovy.control.CharArrayReaderSource;
import org.codehaus.jdt.groovy.integration.EventHandler;
import org.codehaus.jdt.groovy.integration.ISupplementalIndexer;
import org.codehaus.jdt.groovy.integration.LanguageSupport;
import org.codehaus.jdt.groovy.internal.compiler.GroovyClassLoaderFactory;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyErrorCollectorForJDT;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyTypeDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeRequestorFactory;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.BinaryMember;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.search.indexing.IndexingParser;
import org.eclipse.jdt.internal.core.search.matching.ImportMatchLocatorParser;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MatchLocatorParser;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.framework.Version;

/**
 * Groovy implementation of LanguageSupport. This class is dynamically loaded by
 * jdt.core (so referenced by name from jdt.core) and then invoked to get parser
 * that can handle either Groovy or Java.
 */
public class GroovyLanguageSupport implements LanguageSupport {

    private static final CompilerConfiguration CONFIG_SCRIPT_CONFIG = new CompilerConfiguration();
    static {
        CONFIG_SCRIPT_CONFIG.addCompilationCustomizers(new ImportCustomizer().addStaticStars(
            "org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder"));
        CONFIG_SCRIPT_CONFIG.setDisabledGlobalASTTransformations(new HashSet<>(Arrays.asList(
            "org.codehaus.groovy.ast.builder.AstBuilderTransformation", "groovy.grape.GrabAnnotationTransformation")));
    }

    @Override
    public Parser getParser(final Object requestor, final CompilerOptions compilerOptions, final ProblemReporter problemReporter, final boolean parseLiteralExpressionsAsConstants, final int variant) {
        if (variant == 1) {
            return new MultiplexingParser(requestor, compilerOptions, problemReporter, parseLiteralExpressionsAsConstants);
        } else if (variant == 2) {
            return new MultiplexingCommentRecorderParser(requestor, compilerOptions, problemReporter, parseLiteralExpressionsAsConstants);
        } else { // (variant == 3) { similar to '2' but does not allow transforms
            return new MultiplexingCommentRecorderParser(requestor, compilerOptions, problemReporter, parseLiteralExpressionsAsConstants, false);
        }
    }

    @Override
    public CompletionParser getCompletionParser(final CompilerOptions compilerOptions, final ProblemReporter problemReposrter, final boolean storeExtraSourceEnds, final IProgressMonitor monitor) {
        return new MultiplexingCompletionParser(compilerOptions, problemReposrter, storeExtraSourceEnds, monitor);
    }

    @Override
    public IndexingParser getIndexingParser(final ISourceElementRequestor requestor, final IProblemFactory problemFactory, final CompilerOptions options, final boolean reportLocalDeclarations, final boolean optimizeStringLiterals, final boolean useSourceJavadocParser) {
        return new MultiplexingIndexingParser(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, useSourceJavadocParser);
    }

    @Override
    public MatchLocatorParser getMatchLocatorParserParser(final ProblemReporter problemReporter, final MatchLocator locator) {
        return new MultiplexingMatchLocatorParser(problemReporter, locator);
    }

    @Override
    public ImportMatchLocatorParser getImportMatchLocatorParserParser(final ProblemReporter problemReporter, final MatchLocator locator) {
        return new MultiplexingImportMatchLocatorParser(problemReporter, locator);
    }

    @Override
    public SourceElementParser getSourceElementParser(final ISourceElementRequestor requestor, final IProblemFactory problemFactory, final CompilerOptions options, final boolean reportLocalDeclarations, final boolean optimizeStringLiterals, final boolean useSourceJavadocParser) {
        ProblemReporter problemReporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), options, new DefaultProblemFactory());
        return new MultiplexingSourceElementRequestorParser(problemReporter, requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals);
    }

    @Override
    public Collection<String> getImplicitImportContainers(final org.eclipse.jdt.core.ICompilationUnit compilationUnit) {
        Collection<String> implicitImportContainerNames = Arrays.stream(ResolveVisitor.DEFAULT_IMPORTS)
                .map(p -> p.substring(0, p.length() - 1)).collect(java.util.stream.Collectors.toList());

        ModuleNode module = ((GroovyCompilationUnit) compilationUnit).getModuleNode();
        if (module != null) {
            for (ImportNode starImport : module.getStarImports()) {
                if (starImport.getEnd() < 1) { // is it implicit?
                    String packageName = starImport.getPackageName();
                    // strip trailing "." from on-demand import's package name
                    packageName = packageName.substring(0, packageName.length() - 1);

                    implicitImportContainerNames.add(packageName);
                }
            }
        }

        return implicitImportContainerNames;
    }

    @Override
    public CompilationUnit newCompilationUnit(final PackageFragment parent, final String name, final WorkingCopyOwner owner) {
        if (ContentTypeUtils.isGroovyLikeFileName(name)) {
            return new GroovyCompilationUnit(parent, name, owner);
        } else {
            return new CompilationUnit(parent, name, owner);
        }
    }

    @Override
    public CompilationUnitDeclaration newCompilationUnitDeclaration(final ICompilationUnit icu, final ProblemReporter problemReporter, final CompilationResult compilationResult, final int sourceLength) {
        if (ContentTypeUtils.isGroovyLikeFileName(icu.getFileName())) {
            char[] unitText = icu.getContents();
            String unitName = String.valueOf(icu.getFileName());
            ReaderSource unitSource = new CharArrayReaderSource(unitText) {
                @Override public URI getURI() {
                    return URI.create("platform:/resource" + unitName);
                }
            };

            if (problemReporter.options.groovyCompilerConfigScript != null && ResourcesPlugin.getPlugin() != null) {
                IFile eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(unitName));
                if (eclipseFile != null && eclipseFile.getProject().isAccessible() &&
                        !JavaCore.create(eclipseFile.getProject()).isOnClasspath(eclipseFile)) {
                    problemReporter.options.groovyCompilerConfigScript = null;
                }
            }

            CompilerConfiguration compilerConfig = newCompilerConfiguration(problemReporter.options, problemReporter);
            GroovyClassLoader[] classLoaders = new GroovyClassLoaderFactory(problemReporter.options, null).getGroovyClassLoaders(compilerConfig);
            SourceUnit sourceUnit = new SourceUnit(unitName, unitSource, compilerConfig, null, new GroovyErrorCollectorForJDT(compilerConfig));

            org.codehaus.groovy.control.CompilationUnit gcu = new org.codehaus.groovy.control.CompilationUnit(compilerConfig, null, classLoaders[0], classLoaders[1], false, null);
            JDTResolver resolver = new JDTResolver(gcu);
            gcu.setResolveVisitor(resolver);
            gcu.addSource(sourceUnit);

            compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(unitText); // TODO: get from parser

            GroovyCompilationUnitDeclaration decl = new GroovyCompilationUnitDeclaration(problemReporter, compilationResult, sourceLength, gcu, sourceUnit, problemReporter.options);

            decl.processToPhase(Phases.CONVERSION);

            // regardless of a successful outcome, build what is possible in the face of any errors
            if (decl.getModuleNode() != null) {
                decl.populateCompilationUnitDeclaration();
                for (TypeDeclaration type : decl.types) {
                    resolver.record((GroovyTypeDeclaration) type);
                }
            }

            return decl;
        } else {
            return new CompilationUnitDeclaration(problemReporter, compilationResult, sourceLength);
        }
    }

    public static CompilerConfiguration newCompilerConfiguration(final CompilerOptions compilerOptions, final ProblemReporter problemReporter) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setParameters(compilerOptions.produceMethodParameters);
        config.setPreviewFeatures(compilerOptions.enablePreviewFeatures);
        config.setTargetBytecode(CompilerOptions.versionFromJdkLevel(compilerOptions.targetJDK));
        config.setWarningLevel(org.codehaus.groovy.control.messages.WarningMessage.POSSIBLE_ERRORS);

        if (compilerOptions.defaultEncoding != null && !compilerOptions.defaultEncoding.isEmpty()) {
            config.setSourceEncoding(compilerOptions.defaultEncoding);
        }

        if (compilerOptions.buildGroovyFiles > 2) {
            // create type-checking script configuration
            ImportCustomizer ic = new ImportCustomizer();
            ic.addStarImports("org.codehaus.groovy.ast.expr");
            if (GroovyUtils.getGroovyVersion().compareTo(new Version(4, 0, 6)) >= 0) ic.addStarImports("org.codehaus.groovy.ast");
            ic.addStaticStars("org.codehaus.groovy.ast.ClassHelper","org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport");
            config.addCompilationCustomizers(ic).setScriptBaseClass("org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport$TypeCheckingDSL");

            return config;
        }

        if (compilerOptions.buildGroovyFiles > 1 && compilerOptions.groovyCompilerConfigScript != null) {
            Binding binding = new Binding();
            binding.setVariable("configuration", config);
            GroovyShell shell = new GroovyShell(binding, CONFIG_SCRIPT_CONFIG);
            try {
                File configScript = new File(compilerOptions.groovyCompilerConfigScript);
                if (!configScript.isAbsolute() && compilerOptions.groovyProjectName != null) {
                    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(compilerOptions.groovyProjectName);
                    configScript = new File(project.getLocation().append(configScript.getPath()).toOSString());
                }
                shell.evaluate(configScript);
            } catch (Exception | LinkageError e) {
                int severity = ProblemSeverities.Error;
                CompilationResult compilationResult = null;
                if (problemReporter.referenceContext != null) {
                    severity = ProblemSeverities.Warning;
                    compilationResult = problemReporter.referenceContext.compilationResult();
                }
                // TODO: Can the problem be associated with project instead of source unit?
                String[] arguments = {compilerOptions.groovyCompilerConfigScript, e.toString()};
                problemReporter.handle(IProblem.CannotReadSource, arguments, 0, arguments, severity, 0, 0, problemReporter.referenceContext, compilationResult);
            }
        }

        if ((compilerOptions.groovyFlags & CompilerOptions.InvokeDynamic) != 0) {
            config.getOptimizationOptions().putIfAbsent(CompilerConfiguration.INVOKEDYNAMIC, Boolean.TRUE);
        }
        if (Boolean.TRUE.equals(config.getOptimizationOptions().get(CompilerConfiguration.INVOKEDYNAMIC))) {
            if (config.getTargetBytecode().compareTo(CompilerConfiguration.JDK7) < 0) {
                config.setTargetBytecode(CompilerConfiguration.JDK7);
            }
        }

        if (CompilerOptions.versionToJdkLevel(config.getTargetBytecode()) > compilerOptions.targetJDK) {
            Map<String, String> javaOptions = JavaCore.getOptions(); // ensure marker of incompatibility
            if (JavaCore.IGNORE.equals(javaOptions.get(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL))) {
                javaOptions.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.ERROR);
                JavaCore.setOptions((java.util.Hashtable<String,String>) javaOptions);
            }
        }

        return config;
    }

    public static GroovyClassLoader newGroovyClassLoader(final IJavaProject project, final ClassLoader parentLoader) {
        Map<String, String> options = project.getOptions(true);
        options.put(CompilerOptions.OPTIONG_GroovyProjectName, project.getElementName());
        GroovyClassLoaderFactory factory = new GroovyClassLoaderFactory(new CompilerOptions(options), null);
        CompilerConfiguration config = CompilerConfiguration.DEFAULT; // TODO: Use newCompilerConfiguration?
        ClassLoader projectLoader = factory.getGroovyClassLoaders(config)[1]; // the Groovy transform loader

        MultiParentClassLoader multiParentLoader = new MultiParentClassLoader(
            project.getElementName(), new URL[0], new ClassLoader[] {projectLoader, parentLoader});

        return new GroovyClassLoader(multiParentLoader);
    }

    @Override
    public boolean isInterestingProject(final IProject project) {
        return GroovyNature.hasGroovyNature(project);
    }

    @Override
    public boolean isSourceFile(final String fileName, final boolean isInterestingProject) {
        if (isInterestingProject) {
            return Util.isJavaLikeFileName(fileName);
        } else {
            return ContentTypeUtils.isJavaLikeButNotGroovyLikeFileName(fileName);
        }
    }

    @Override
    public boolean isInterestingSourceFile(final String fileName) {
        return ContentTypeUtils.isGroovyLikeFileName(fileName);
    }

    @Override
    public boolean maybePerformDelegatedSearch(final PossibleMatch possibleMatch, final SearchPattern pattern, final SearchRequestor requestor) {
        if (possibleMatch.openable != null && possibleMatch.openable.exists()) {
            ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(possibleMatch, pattern, requestor);
            if (typeRequestor != null) {
                TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(possibleMatch);
                visitor.visitCompilationUnit(typeRequestor);
                return true;
            }
        }
        return false;
    }

    @Override
    public EventHandler getEventHandler() {
        // FIXASC could be une singleton?
        return new GroovyEventHandler();
    }

    /**
     * Removes all children that do not have a real source location.
     */
    @Override
    public void filterNonSourceMembers(final BinaryType binaryType) {
        try {
            IJavaElement[] childrenArr = binaryType.getChildren();
            List<IJavaElement> children = new ArrayList<>(Arrays.asList(childrenArr));
            List<JavaElement> removedChildren = new LinkedList<>();
            for (Iterator<IJavaElement> childIter = children.iterator(); childIter.hasNext();) {
                IJavaElement child = childIter.next();
                if (child instanceof BinaryMember) {
                    BinaryMember binaryChild = (BinaryMember) child;
                    ISourceRange range = binaryChild.getSourceRange();
                    if (range == null || range.getOffset() == -1) {
                        removedChildren.add(binaryChild);
                        childIter.remove();
                    }
                }
            }
            JavaElement[] newChildrenArr = children.toArray(new JavaElement[children.size()]);

            // now comes the icky part.

            // we need to set the children of the ClassFileInfo to the new children
            // but this class is package protected, so can't access it directly.
            Object /* ClassFileInfo */classFileInfo = ((ClassFile) binaryType.getParent()).getElementInfo();
            ReflectionUtils.setPrivateField(classFileInfo.getClass(), "binaryChildren", classFileInfo, newChildrenArr);

            // also need to remove these children from the JavaModelManager
            JavaModelManager manager = JavaModelManager.getJavaModelManager();
            for (JavaElement removedChild : removedChildren) {
                if (removedChild instanceof BinaryType) {
                    manager.removeInfoAndChildren(removedChild.getParent());
                } else {
                    manager.removeInfoAndChildren(removedChild);
                }
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
    }

    // Singleton that determines if a search scope should be expanded
    public static ISearchScopeExpander searchScopeExpander;

    /**
     * Expands the search scope iff the focus is a private member inside of a
     * {@link GroovyCompilationUnit} and the search requestor is CollectingSearchRequestor.
     */
    @Override
    public IJavaSearchScope expandSearchScope(final IJavaSearchScope scope, final SearchPattern pattern, final SearchRequestor requestor) {
        // delegate to something that can see the org.eclise.jdt.coreext classes
        if (searchScopeExpander != null) {
            return searchScopeExpander.expandSearchScope(scope, pattern, requestor);
        }
        return scope;
    }

    @Override
    public boolean isInterestingBinary(final BinaryType type, final IBinaryType typeInfo) {
        return isInterestingProject(type.getJavaProject().getProject()) && ContentTypeUtils.isGroovyLikeFileName(type.sourceFileName(typeInfo));
    }

    @Override
    public IJavaElement[] binaryCodeSelect(final ClassFile classFile, final int offset, final int length, final WorkingCopyOwner owner)
            throws JavaModelException {
        GroovyCompilationUnit binaryUnit = new GroovyClassFileWorkingCopy(classFile, owner);
        return binaryUnit.codeSelect(offset, length, owner);
    }

    @Override
    public ISupplementalIndexer getSupplementalIndexer() {
        return new BinaryGroovySupplementalIndexer();
    }
}
