/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompilationReporter;

/**
 * Reporter to get the AST from.
 */
public class GroovyCompilationReporter implements IGroovyCompilationReporter {
	
	public ModuleNode moduleNode;

	public void beginReporting() {
	}

	public void beginReporting(String fileName) {
	}

	public void compilationError(String fileName, int line, int startCol,
			int endCol, String message, String stackTrace) {
		throw new IllegalStateException(message + "\n" + stackTrace);
	}

	public void endReporting() {
	}

	public void endReporting(String fileName) {
	}

	public void generatedAST(String fileName, ModuleNode moduleNode) {
		this.moduleNode = moduleNode;
	}

	public void generatedCST(String fileName, GroovySourceAST cst) {
	}

	public void generatedClasses(String fileName, String[] classNames,
			String[] classFilePaths) {
	}
}
