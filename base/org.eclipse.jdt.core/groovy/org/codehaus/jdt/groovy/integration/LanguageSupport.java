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
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;

public interface LanguageSupport {

	Parser getParser(LookupEnvironment lookupEnvironment, ProblemReporter problemReporter, boolean parseLiteralExpressionsAsConstants, int variant);

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

}
