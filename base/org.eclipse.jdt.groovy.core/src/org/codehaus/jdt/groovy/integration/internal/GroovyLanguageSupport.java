/*******************************************************************************
 * Copyright (c) 2009, 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.integration.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
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
 * 
 * @author Andy Clement
 * 
 */
@SuppressWarnings("restriction")
public class GroovyLanguageSupport implements LanguageSupport {

	public Parser getParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter,
			boolean parseLiteralExpressionsAsConstants, int variant) {
		if (variant == 1) {
			return new MultiplexingParser(requestor, compilerOptions, problemReporter, parseLiteralExpressionsAsConstants);
		} else if (variant == 2) {
			return new MultiplexingCommentRecorderParser(requestor, compilerOptions, problemReporter,
					parseLiteralExpressionsAsConstants);
		} else { // (variant == 3) { similar to '2' but does not allow transforms
			return new MultiplexingCommentRecorderParser(requestor, compilerOptions, problemReporter,
					parseLiteralExpressionsAsConstants, false);
		}
	}

	public IndexingParser getIndexingParser(ISourceElementRequestor requestor, IProblemFactory problemFactory,
			CompilerOptions options, boolean reportLocalDeclarations, boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
		return new MultiplexingIndexingParser(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals,
				useSourceJavadocParser);
	}

	public MatchLocatorParser getMatchLocatorParserParser(ProblemReporter problemReporter, MatchLocator locator) {
		return new MultiplexingMatchLocatorParser(problemReporter, locator);
	}

	public ImportMatchLocatorParser getImportMatchLocatorParserParser(ProblemReporter problemReporter, MatchLocator locator) {
		return new MultiplexingImportMatchLocatorParser(problemReporter, locator);
	}

	public SourceElementParser getSourceElementParser(ISourceElementRequestor requestor, IProblemFactory problemFactory,
			CompilerOptions options, boolean reportLocalDeclarations, boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
		ProblemReporter problemReporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), options,
				new DefaultProblemFactory());
		return new MultiplexingSourceElementRequestorParser(problemReporter, requestor, problemFactory, options,
				reportLocalDeclarations, optimizeStringLiterals);
	}

	public CompilationUnit newCompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner) {

		// should use a content type here
		if (ContentTypeUtils.isGroovyLikeFileName(name)) {
			return new GroovyCompilationUnit(parent, name, owner);
		} else {
			return new CompilationUnit(parent, name, owner);
		}
	}

	public CompilationUnitDeclaration newCompilationUnitDeclaration(ICompilationUnit unit, ProblemReporter problemReporter,
			CompilationResult compilationResult, int sourceLength) {
		if (ContentTypeUtils.isGroovyLikeFileName(compilationResult.getFileName())) {
			CompilerConfiguration groovyCompilerConfig = new CompilerConfiguration();
			// groovyCompilerConfig.setPluginFactory(new ErrorRecoveredCSTParserPluginFactory(null));
			ErrorCollector errorCollector = new GroovyErrorCollectorForJDT(groovyCompilerConfig);
			SourceUnit groovySourceUnit = new SourceUnit(new String(compilationResult.getFileName()),
					new String(unit.getContents()), groovyCompilerConfig, null, errorCollector);

			// FIXASC missing the classloader configuration (eg. to include transformers)
			org.codehaus.groovy.control.CompilationUnit groovyCU = new org.codehaus.groovy.control.CompilationUnit(
					groovyCompilerConfig);
			// groovyCU.removeOutputPhaseOperation();
			JDTResolver resolver = new JDTResolver(groovyCU);
			groovyCU.setResolveVisitor(resolver);

			// TODO groovy get this from the Antlr parser
			compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(unit.getContents());

			groovyCU.addSource(groovySourceUnit);
			GroovyCompilationUnitDeclaration gcuDeclaration = new GroovyCompilationUnitDeclaration(problemReporter,
					compilationResult, sourceLength, groovyCU, groovySourceUnit, null);

			// boolean success =
			gcuDeclaration.processToPhase(Phases.CONVERSION);

			if (gcuDeclaration.getModuleNode() != null) {
				// Regardless of a successful outcome, build what is possible in the face of any errors
				gcuDeclaration.populateCompilationUnitDeclaration();
				for (TypeDeclaration decl : gcuDeclaration.types) {
					GroovyTypeDeclaration gtDeclaration = (GroovyTypeDeclaration) decl;
					resolver.record(gtDeclaration);
				}
			}

			return gcuDeclaration;
		} else {
			return new CompilationUnitDeclaration(problemReporter, compilationResult, sourceLength);
		}
	}

	public boolean isInterestingProject(IProject project) {
		return GroovyNature.hasGroovyNature(project);
	}

	public boolean isSourceFile(String fileName, boolean isInterestingProject) {
		if (isInterestingProject) {
			return Util.isJavaLikeFileName(fileName);
		} else {
			return ContentTypeUtils.isJavaLikeButNotGroovyLikeExtension(fileName);
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
		return isInterestingProject(type.getJavaProject().getProject())
				&& ContentTypeUtils.isGroovyLikeFileName(type.sourceFileName(typeInfo));
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
