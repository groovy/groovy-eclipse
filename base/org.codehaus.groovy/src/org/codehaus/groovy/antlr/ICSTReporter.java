package org.codehaus.groovy.antlr;

import java.util.List;

import org.codehaus.groovy.antlr.GroovySourceAST;

/**
 * Reports on CST builds.
 * 
 * @author empovazan
 */
public interface ICSTReporter {
	/**
	 * Report that an AST has been generated.
	 * @param ast
	 */
	public void generatedCST(String fileName, GroovySourceAST ast);

	/**
	 * Report parse errors while attempting to generate a CST. Not that the CST may in fact be generated if the parser
	 * is error recovering.
	 * 
	 * @param fileName
	 * @param errors
	 */
	public void reportErrors(String fileName, List errors);
}
