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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.framework.Bundle;

public class LanguageSupportFactory {

	private static LanguageSupport languageSupport;
	
	public static final int CommentRecorderParserVariant = 2;
	
	public static Parser getParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean parseLiteralExpressionsAsConstants,int variant) {
		return getLanguageSupport().getParser(requestor, compilerOptions,problemReporter,parseLiteralExpressionsAsConstants, variant);
	}
	
	public static IndexingParser getIndexingParser(ISourceElementRequestor requestor, IProblemFactory problemFactory, CompilerOptions options, boolean reportLocalDeclarations, 
			boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
		return getLanguageSupport().getIndexingParser(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, useSourceJavadocParser);
	}
	
	public static SourceElementParser getSourceElementParser(ISourceElementRequestor requestor, IProblemFactory problemFactory, CompilerOptions options, boolean reportLocalDeclarations, 
			boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
		return getLanguageSupport().getSourceElementParser(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, useSourceJavadocParser);
	}
	
	public static MatchLocatorParser getMatchLocatorParser(ProblemReporter problemReporter, MatchLocator locator) {
		return getLanguageSupport().getMatchLocatorParserParser(problemReporter, locator);
	}
	
	public static ImportMatchLocatorParser getImportMatchLocatorParser(ProblemReporter problemReporter, MatchLocator locator) {
		return getLanguageSupport().getImportMatchLocatorParserParser(problemReporter, locator);
	}
	
	public static CompilationUnit newCompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner) {
        return getLanguageSupport().newCompilationUnit(parent, name, owner);
    }
	public static CompilationUnitDeclaration newCompilationUnitDeclaration(ICompilationUnit unit, ProblemReporter problemReporter, CompilationResult compilationResult, int sourceLength) {
        return getLanguageSupport().newCompilationUnitDeclaration(unit, problemReporter, compilationResult, sourceLength);
    }
	
	public static boolean isInterestingProject(IProject project) {
	    return getLanguageSupport().isInterestingProject(project);
	}
	
	public static boolean isSourceFile(String fileName, boolean isInterestingProject) {
	    return getLanguageSupport().isSourceFile(fileName, isInterestingProject);
	}
	
	/**
	 * Does this file name require special language support?
	 * This method does not look at project natures and will return true or false
	 * independent of any natures attached to the project that contains this source file
	 * @param fileName the file name to look at.
	 * @return true iff the file name is one that requires special language support.
	 */
	public static boolean isInterestingSourceFile(String fileName) {
		return getLanguageSupport().isInterestingSourceFile(fileName);
	}
	
	public static boolean maybePerformDelegatedSearch(PossibleMatch possibleMatch, SearchPattern pattern, SearchRequestor requestor) {
		return getLanguageSupport().maybePerformDelegatedSearch(possibleMatch, pattern, requestor);
	}
	
	/**
	 * Removes members from this binary type that are not mapped to locations in the 
	 * source code (ie- their source location is invalid).  This ensures that 
	 * generated groovy methods (eg- getters and setters) and fields are not shown in
	 * the outline view.
	 * @param binaryType
	 */
	public static void filterNonSourceMembers(BinaryType binaryType) {
		getLanguageSupport().filterNonSourceMembers(binaryType);
	}
	
	
	//FIXASC static state issues?
	private static LanguageSupport getLanguageSupport() {
		if (languageSupport==null) {
			languageSupport = /*new GroovyLanguageSupport();*/tryInstantiate("org.codehaus.jdt.groovy.integration.internal.GroovyLanguageSupport"); //$NON-NLS-1$
			if (languageSupport==null) {
				languageSupport = new DefaultLanguageSupport();
			}
		}
		return languageSupport;
	}
	
	private static LanguageSupport tryInstantiate(String className) {
		LanguageSupport instance= null;
		if (className != null && className.length() > 0) {
			try {
				int separator= className.indexOf(':');
				Bundle bundle= null;
				if (separator == -1) {
					JavaCore javaCore = JavaCore.getJavaCore();
					if (javaCore==null) {
						Class clazz = Class.forName(className);
						return (LanguageSupport)clazz.newInstance();
					} else {
						bundle= javaCore.getBundle();
					}
				} else {
					String bundleName = className.substring(0, separator);
					className = className.substring(separator + 1);
					bundle = Platform.getBundle(bundleName);
				}
				Class c= bundle.loadClass(className);
				instance= (LanguageSupport) c.newInstance();
			} catch (ClassNotFoundException e) {
		        log(e);
			} catch (InstantiationException e) {
		        log(e);
			} catch (IllegalAccessException e) {
		        log(e);
			} catch (ClassCastException e) {
		        log(e);
			}
		}
		return instance;
	}

	private static void log(Exception e) {
		if (JavaCore.getPlugin()==null || JavaCore.getPlugin().getLog()==null) {
			System.err.println("Error creating Groovy language support:"); //$NON-NLS-1$
			e.printStackTrace(System.err);
		} else {
			Util.log(e, "Error creating Groovy language support"); //$NON-NLS-1$
		}
	}

	public static EventHandler getEventHandler() {
		return getLanguageSupport().getEventHandler();
	}


	public static IJavaSearchScope expandSearchScope(IJavaSearchScope scope, SearchPattern pattern, SearchRequestor requestor) {
		return getLanguageSupport().expandSearchScope(scope, pattern, requestor);
	}
	
	public static boolean isGroovyLanguageSupportInstalled() {
		return getLanguageSupport().getClass().getName().endsWith("GroovyLanguageSupport"); //$NON-NLS-1$
	}
	/**
	 * @param type a binary type that may or may not come from Groovy
	 * @return true iff the binary type was compiled from groovy sources
	 */
	public static boolean isInterestingBinary(BinaryType type, IBinaryType typeInfo) {
		return getLanguageSupport().isInterestingBinary(type, typeInfo);
	}

	/**
	 * Performs code select on the given {@link ClassFile}
	 * @param classFile the class file to use, must be a classFile known to be of groovy origin
	 * @param offset the start of the selection
	 * @param length the length of the selection
	 * @param owner the {@link WorkingCopyOwner} for this operation
	 * @return {@link IJavaElement}s corresponding to the given selection.
	 * @throws JavaModelException 
	 */
	public static IJavaElement[] binaryCodeSelect(ClassFile classFile, int offset, int length, WorkingCopyOwner owner) throws JavaModelException {
		return getLanguageSupport().binaryCodeSelect(classFile, offset, length, owner);
	}

	/**
	 * @return an object that can provide supplemental indexing on a class file
	 */
	public static ISupplementalIndexer getSupplementalIndexer() {
		return getLanguageSupport().getSupplementalIndexer();
	}
}
