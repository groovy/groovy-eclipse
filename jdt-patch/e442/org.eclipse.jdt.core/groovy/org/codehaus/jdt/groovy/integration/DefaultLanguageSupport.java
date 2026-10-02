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
package org.codehaus.jdt.groovy.integration;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.search.indexing.IndexingParser;
import org.eclipse.jdt.internal.core.search.matching.ImportMatchLocatorParser;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MatchLocatorParser;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * The default implementation just does what JDT would do.
 */
class DefaultLanguageSupport implements LanguageSupport {

	@Override
	public Parser getParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean parseLiteralExpressionsAsConstants,int variant) {
		if (variant==1) {
			return new Parser(problemReporter, parseLiteralExpressionsAsConstants);
		} else { // if (variant==2) {
			return new CommentRecorderParser(problemReporter, parseLiteralExpressionsAsConstants);
		}
	}

	@Override
	public CompletionParser getCompletionParser(CompilerOptions compilerOptions, ProblemReporter problemReposrter,
			boolean storeExtraSourceEnds, IProgressMonitor monitor) {
		return new CompletionParser(problemReposrter, storeExtraSourceEnds, monitor);
	}

	@Override
	public IndexingParser getIndexingParser(ISourceElementRequestor requestor, IProblemFactory problemFactory,
			CompilerOptions options, boolean reportLocalDeclarations, boolean optimizeStringLiterals,
			boolean useSourceJavadocParser) {
		return new IndexingParser(requestor, problemFactory, options, reportLocalDeclarations,
				optimizeStringLiterals, useSourceJavadocParser);
	}

	@Override
	public ImportMatchLocatorParser getImportMatchLocatorParserParser(ProblemReporter problemReporter,
			MatchLocator locator) {
		return new ImportMatchLocatorParser(problemReporter, locator);
	}

	@Override
	public SourceElementParser getSourceElementParser(ISourceElementRequestor requestor,
			IProblemFactory problemFactory, CompilerOptions options, boolean reportLocalDeclarations,
			boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
		return new SourceElementParser(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, useSourceJavadocParser);
	}

	@Override
	public MatchLocatorParser getMatchLocatorParserParser(ProblemReporter problemReporter, MatchLocator locator) {
		return new MatchLocatorParser(problemReporter, locator);
	}

	@Override
	public CompilationUnit newCompilationUnit(PackageFragment parent,
			String name, WorkingCopyOwner owner) {
		return new CompilationUnit(parent, name, owner);
	}

	@Override
	public CompilationUnitDeclaration newCompilationUnitDeclaration(
			ICompilationUnit unit,
			ProblemReporter problemReporter, CompilationResult compilationResult, int sourceLength) {
		return new CompilationUnitDeclaration(problemReporter, compilationResult, sourceLength);
	}

	@Override
	public boolean isInterestingProject(IProject project) {
		// assume that if this method is called, them this is a Java project
		return true;
	}

	@Override
	public boolean isSourceFile(String fileName, boolean isInterestingProject) {
		return Util.isJavaLikeFileName(fileName);
	}

	@Override
	public boolean isInterestingSourceFile(String fileName) {
		return false;
	}

	@Override
	public boolean maybePerformDelegatedSearch(PossibleMatch possibleMatch, SearchPattern pattern,
			SearchRequestor requestor) {
		return false;
	}

	@Override
	public EventHandler getEventHandler() {
		return DefaultEventHandler.instance;
	}

	static class DefaultEventHandler implements EventHandler {
		static DefaultEventHandler instance = new DefaultEventHandler();
		private DefaultEventHandler() {
			// nop
		}
		@Override
		public void handle(JavaProject javaProject, String string) {
			// nop
		}
	}

	@Override
	public void filterNonSourceMembers(BinaryType binaryType) {
		// nop
	}

	@Override
	public IJavaSearchScope expandSearchScope(IJavaSearchScope scope, SearchPattern pattern, SearchRequestor requestor) {
		// never expand
		return scope;
	}

	@Override
	public boolean isInterestingBinary(BinaryType type, IBinaryType typeInfo) {
		return false;
	}

	@Override
	public IJavaElement[] binaryCodeSelect(ClassFile classFile, int offset, int length, WorkingCopyOwner owner) throws JavaModelException {
		return new IJavaElement[0];
	}

	@Override
	public ISupplementalIndexer getSupplementalIndexer() {
		return new NoopIndexer();
	}
}
