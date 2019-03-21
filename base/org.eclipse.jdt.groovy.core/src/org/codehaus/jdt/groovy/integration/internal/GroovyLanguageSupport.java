/*
 * Copyright 2009-2018 the original author or authors.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.jdt.groovy.integration.EventHandler;
import org.codehaus.jdt.groovy.integration.ISupplementalIndexer;
import org.codehaus.jdt.groovy.integration.LanguageSupport;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyErrorCollectorForJDT;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyTypeDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.util.CompilerUtils;
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

/**
 * The groovy implementation of LanguageSupport. This class is dynamically loaded by jdt.core (so referenced by name from jdt.core)
 * - and then invoked to get a parser that can handle either groovy or java.
 */
public class GroovyLanguageSupport implements LanguageSupport {

    public Parser getParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean parseLiteralExpressionsAsConstants, int variant) {
        if (variant == 1) {
            return new MultiplexingParser(requestor, compilerOptions, problemReporter, parseLiteralExpressionsAsConstants);
        } else if (variant == 2) {
            return new MultiplexingCommentRecorderParser(requestor, compilerOptions, problemReporter, parseLiteralExpressionsAsConstants);
        } else { // (variant == 3) { similar to '2' but does not allow transforms
            return new MultiplexingCommentRecorderParser(requestor, compilerOptions, problemReporter, parseLiteralExpressionsAsConstants, false);
        }
    }

    public CompletionParser getCompletionParser(CompilerOptions compilerOptions, ProblemReporter problemReposrter, boolean storeExtraSourceEnds, IProgressMonitor monitor) {
        return new MultiplexingCompletionParser(compilerOptions, problemReposrter, storeExtraSourceEnds, monitor);
    }

    public IndexingParser getIndexingParser(ISourceElementRequestor requestor, IProblemFactory problemFactory, CompilerOptions options, boolean reportLocalDeclarations, boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
        return new MultiplexingIndexingParser(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, useSourceJavadocParser);
    }

    public MatchLocatorParser getMatchLocatorParserParser(ProblemReporter problemReporter, MatchLocator locator) {
        return new MultiplexingMatchLocatorParser(problemReporter, locator);
    }

    public ImportMatchLocatorParser getImportMatchLocatorParserParser(ProblemReporter problemReporter, MatchLocator locator) {
        return new MultiplexingImportMatchLocatorParser(problemReporter, locator);
    }

    public SourceElementParser getSourceElementParser(ISourceElementRequestor requestor, IProblemFactory problemFactory, CompilerOptions options, boolean reportLocalDeclarations, boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
        ProblemReporter problemReporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), options, new DefaultProblemFactory());
        return new MultiplexingSourceElementRequestorParser(problemReporter, requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals);
    }

    public CompilationUnit newCompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner) {
        if (ContentTypeUtils.isGroovyLikeFileName(name)) {
            return new GroovyCompilationUnit(parent, name, owner);
        } else {
            return new CompilationUnit(parent, name, owner);
        }
    }

    public CompilationUnitDeclaration newCompilationUnitDeclaration(ICompilationUnit icu, ProblemReporter problemReporter, CompilationResult compilationResult, int sourceLength) {
        if (ContentTypeUtils.isGroovyLikeFileName(compilationResult.getFileName())) {

            String unitName = String.valueOf(compilationResult.getFileName());

            if (problemReporter.options.groovyCompilerConfigScript != null) {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                if (workspace != null && workspace.getRoot() != null) {
                    IFile eclipseFile = workspace.getRoot().getFile(new Path(unitName));
                    if (eclipseFile != null && eclipseFile.getProject().isAccessible() &&
                            !JavaCore.create(eclipseFile.getProject()).isOnClasspath(eclipseFile)) {
                        problemReporter.options.groovyCompilerConfigScript = null;
                    }
                }
            }

            CompilerConfiguration compilerConfig = newCompilerConfiguration(problemReporter.options, problemReporter);
            GroovyClassLoader classLoader = null; // TODO: missing the GroovyClassLoader configuration
            ErrorCollector errorCollector = new GroovyErrorCollectorForJDT(compilerConfig);
            SourceUnit groovySourceUnit = new SourceUnit(unitName, String.valueOf(icu.getContents()), compilerConfig, classLoader, errorCollector);

            org.codehaus.groovy.control.CompilationUnit gcu = new org.codehaus.groovy.control.CompilationUnit(compilerConfig);
            JDTResolver resolver = new JDTResolver(gcu);
            gcu.setResolveVisitor(resolver);

            // TODO groovy get this from the Antlr parser
            compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(icu.getContents());

            gcu.addSource(groovySourceUnit);
            GroovyCompilationUnitDeclaration decl = new GroovyCompilationUnitDeclaration(problemReporter, compilationResult, sourceLength, gcu, groovySourceUnit, null);

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

    public static CompilerConfiguration newCompilerConfiguration(CompilerOptions compilerOptions, ProblemReporter problemReporter) {
        CompilerConfiguration config = new CompilerConfiguration();

        if (compilerOptions.buildGroovyFiles > 1 && compilerOptions.groovyCompilerConfigScript != null) {
            Binding binding = new Binding();
            binding.setVariable("configuration", config);

            CompilerConfiguration configuratorConfig = new CompilerConfiguration();
            org.osgi.framework.Version v = GroovyUtils.getGroovyVersion();
            if ((v.getMajor() == 2 && v.getMinor() >= 1) || v.getMajor() > 2) {
                ImportCustomizer customizer = new ImportCustomizer();
                customizer.addStaticStars("org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder");
                configuratorConfig.addCompilationCustomizers(customizer);
            }

            GroovyShell shell = new GroovyShell(binding, configuratorConfig);
            try {
                File configScript = new File(compilerOptions.groovyCompilerConfigScript);
                if (!configScript.isAbsolute() && compilerOptions.groovyProjectName != null) {
                    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(compilerOptions.groovyProjectName);
                    configScript = new File(project.getLocation().append(configScript.getPath()).toOSString());
                }
                shell.evaluate(configScript);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process Groovy config script: " + compilerOptions.groovyCompilerConfigScript, e);
            }
        }

        if ((compilerOptions.groovyFlags & CompilerUtils.InvokeDynamic) != 0) {
            config.getOptimizationOptions().put(/*CompilerConfiguration.INVOKEDYNAMIC*/"indy", Boolean.TRUE);
        }

        return config;
    }

    public boolean isInterestingProject(IProject project) {
        return GroovyNature.hasGroovyNature(project);
    }

    public boolean isSourceFile(String fileName, boolean isInterestingProject) {
        if (isInterestingProject) {
            return Util.isJavaLikeFileName(fileName);
        } else {
            return ContentTypeUtils.isJavaLikeButNotGroovyLikeFileName(fileName);
        }
    }

    public boolean isInterestingSourceFile(String fileName) {
        return ContentTypeUtils.isGroovyLikeFileName(fileName);
    }

    public boolean maybePerformDelegatedSearch(PossibleMatch possibleMatch, SearchPattern pattern, SearchRequestor requestor) {
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

    public EventHandler getEventHandler() {
        // FIXASC could be une singleton?
        return new GroovyEventHandler();
    }

    /**
     * Go through the bunary children and remove all children that do not have a real source location
     */
    public void filterNonSourceMembers(BinaryType binaryType) {
        try {
            IJavaElement[] childrenArr = binaryType.getChildren();
            List<IJavaElement> children = new ArrayList<IJavaElement>(Arrays.asList(childrenArr));
            List<JavaElement> removedChildren = new LinkedList<JavaElement>();
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
                    manager.removeInfoAndChildren((JavaElement) removedChild.getParent());
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
     * Expand the search scope iff the focus is a private member inside of a {@link GroovyCompilationUnit}. And the search requestor
     * is CollectingSearchRequestor.
     */
    public IJavaSearchScope expandSearchScope(IJavaSearchScope scope, SearchPattern pattern, SearchRequestor requestor) {
        // delegate to something that can see the org.eclise.jdt.coreext classes
        if (searchScopeExpander != null) {
            return searchScopeExpander.expandSearchScope(scope, pattern, requestor);
        }
        return scope;
    }

    public boolean isInterestingBinary(BinaryType type, IBinaryType typeInfo) {
        return isInterestingProject(type.getJavaProject().getProject()) && ContentTypeUtils.isGroovyLikeFileName(type.sourceFileName(typeInfo));
    }

    public IJavaElement[] binaryCodeSelect(ClassFile classFile, int offset, int length, WorkingCopyOwner owner)
            throws JavaModelException {
        GroovyCompilationUnit binaryUnit = new GroovyClassFileWorkingCopy(classFile, owner);
        return binaryUnit.codeSelect(offset, length, owner);
    }

    public ISupplementalIndexer getSupplementalIndexer() {
        return new BinaryGroovySupplementalIndexer();
    }
}
