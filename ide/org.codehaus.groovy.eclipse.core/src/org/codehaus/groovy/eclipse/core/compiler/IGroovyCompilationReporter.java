 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.compiler;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.ast.ModuleNode;

/**
 * A IGroovyCompilationReporter is implemented by the user of the GroovyCompiler. It receieves notifications for the
 * various artifacts generated during compilation, as well as compilation errors.
 * 
 * Note that the order in which the generated artifacts are reported is guaranteed to be CST->AST->Classes
 * 
 * @author empovazan
 */
public interface IGroovyCompilationReporter {
	/**
	 * Reporting for the current compile begins.
	 */
	void beginReporting();

	/**
	 * Reporting for the current compile ends.
	 */
	void endReporting();
	
	/**
	 * Reporting for the given file begins. This is a chance to initialize reporting for this file, for example, remove
	 * current error markers. All other methods are called between the beginReporting/endReporting methods.
	 * 
	 * @param fileName
	 */
	void beginReporting(String fileName);

	/**
	 * Reporting for the given file is complete.
	 * 
	 * @param fileName
	 */
	void endReporting(String fileName);

	/** Not implemented yet */
	void generatedCST(String fileName, GroovySourceAST cst);

	/**
	 * An AST was generated. This method is only called if the compilation flag GENERATE_AST was set.
	 * 
	 * @param fileName
	 * @param moduleNode
	 */
	void generatedAST(String fileName, ModuleNode moduleNode);

	/**
	 * Classes were generated. This method is only called if the compilation flag GENERATE_CLASS_FILE was set.
	 * 
	 * @param file
	 * @param classNames
	 *            An array of fully qualified class names. Note that these are only top level classes defined by 'class'
	 *            statements, not the classes generated from closures etc. To get these other classes, one needs to
	 *            search for /classname$.+\.class/ in the output directory.
	 * @param classFilePaths
	 *            An array of canonical paths to the class files that were generated.
	 */
	void generatedClasses(String fileName, String[] classNames, String[] classFilePaths);

	/**
	 * A compilation error occured at the following coordinates.
	 * 
	 * @param fileName
	 * @param line
	 * @param startCol
	 * @param endCol
	 * @param message
	 * @param stackTrace
	 */
	void compilationError(String fileName, int line, int startCol, int endCol, String message, String stackTrace);
}
