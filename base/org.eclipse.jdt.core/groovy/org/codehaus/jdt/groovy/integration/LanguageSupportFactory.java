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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.framework.Bundle;

public class LanguageSupportFactory {

	private static LanguageSupport languageSupport;
	
	public static final int CommentRecorderParserVariant = 2;
	
	public static Parser getParser(CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean parseLiteralExpressionsAsConstants,int variant) {
		return getLanguageSupport().getParser(compilerOptions,problemReporter,parseLiteralExpressionsAsConstants, variant);
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
	
	//FIXASC (M2) static state issues?
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
					bundle= JavaCore.getJavaCore().getBundle();
				} else {
					String bundleName= className.substring(0, separator);
					className= className.substring(separator + 1);
					bundle= Platform.getBundle(bundleName);
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
	    Util.log(e, "Error creating Groovy language support"); //$NON-NLS-1$
	}


}
