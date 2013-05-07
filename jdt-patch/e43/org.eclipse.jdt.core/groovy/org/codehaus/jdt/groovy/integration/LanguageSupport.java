/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.integration;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
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
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.search.indexing.IndexingParser;
import org.eclipse.jdt.internal.core.search.matching.ImportMatchLocatorParser;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MatchLocatorParser;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

public interface LanguageSupport {

	Parser getParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean parseLiteralExpressionsAsConstants, int variant);

	IndexingParser getIndexingParser(ISourceElementRequestor requestor, IProblemFactory problemFactory, CompilerOptions options, boolean reportLocalDeclarations, 
			boolean optimizeStringLiterals, boolean useSourceJavadocParser);
	
	MatchLocatorParser getMatchLocatorParserParser(ProblemReporter problemReporter, MatchLocator locator);

	SourceElementParser getSourceElementParser(ISourceElementRequestor requestor, IProblemFactory problemFactory,
			CompilerOptions options, boolean reportLocalDeclarations, boolean optimizeStringLiterals,
			boolean useSourceJavadocParser);

	ImportMatchLocatorParser getImportMatchLocatorParserParser(ProblemReporter problemReporter, MatchLocator locator);
	
    CompilationUnit newCompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner);
    
    CompilationUnitDeclaration newCompilationUnitDeclaration(ICompilationUnit unit, ProblemReporter problemReporter, CompilationResult compilationResult, int sourceLength);
    
    /**
     * Determines if the project requires special languages support.
     * 
     * @param project the project to analyze
     * 
     * @return true iff the project passed in has a nature that is considered interesting 
     * for this language support
     */
    boolean isInterestingProject(IProject project);
    
    /**
     * Determines if fileName is a source file, and takes into account
     * whether or not this project requires special language support.
     * 
     * @param fileName the name of the file to analyze
     * @param isInterestingProject true if this project requires special
     * language support, or if it should be treated like a regular 
     * Java project
     * 
     *  @return true iff fileName is considered a Java-like file and should 
     *  be passed to the compiler to produce byte code
     */
    boolean isSourceFile(String fileName, boolean isInterestingProject);

	/**
	 * Determines if the file name requires special language support.  This method does not
	 * examine the project nature and so will return true if the file name is interesting 
	 * regardless of whether or not the containing project itself is interesting.
	 * 
	 * @param fileName
	 * @return true iff the file name is one that requires special language support.
	 */
	boolean isInterestingSourceFile(String fileName);

	/**
	 * Maybe perform a search for the possible match using special language support.
	 * Returns true if the search was completed by the special language support. 
	 * Even if the search document requires special language support
	 * (i.e., {@link LanguageSupport#isInterestingSourceFile(String)}  returns true),
	 * this method may not perform the search.  The kind of search pattern will determine
	 * if a special search is required.
	 * The results of the search are sent to the SearchRequestor that is passed in.
	 * @param possibleMatch the possible match to look for
	 * @param pattern
	 * @param requestor the requestor to send any completed search results to 
	 * @return true iff the search was performed
	 */
	boolean maybePerformDelegatedSearch(PossibleMatch possibleMatch, SearchPattern pattern, SearchRequestor requestor);

	EventHandler getEventHandler();

	void filterNonSourceMembers(BinaryType binaryType);

	/**
	 * Creates an expanded search scope for the given search pattern if required
	 * @param scope the initial scope
	 * @param pattern the target pattern
	 * @param requestor the {@link SearchRequestor} for the given search
	 * @return an expanded scope if required, or the initial scope if there is no change
	 */
	IJavaSearchScope expandSearchScope(IJavaSearchScope scope, SearchPattern pattern, SearchRequestor requestor);

	/**
	 * @param type a binary type that may or may not come from Groovy
	 * @param typeInfo type info for the given type
	 * @return true iff the binary type was compiled from groovy sources
	 */
	boolean isInterestingBinary(BinaryType type, IBinaryType typeInfo);

	/**
	 * Performs code select on the given {@link IClassFile}
	 * @param classFile the class file to use, must be a classFile known to be of groovy origin
	 * @param offset the start of the selection
	 * @param length the length of the selection
	 * @param owner the {@link WorkingCopyOwner} for this operation
	 * @return {@link IJavaElement}s corresponding to the given selection.
	 */
	IJavaElement[] binaryCodeSelect(ClassFile classFile, int offset, int length, WorkingCopyOwner owner) throws JavaModelException;

	/**
	 * @return the supplemental indexer that provides extra indexing for interesting binary files
	 */
	ISupplementalIndexer getSupplementalIndexer();
}
