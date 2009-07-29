package org.codehaus.groovy.antlr;

import java.io.Reader;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Reduction;

/**
 * AST parser plugin which hooks into the CST parsing and reports on CSTs built.
 * 
 * @author empovazan
 */
public class CSTParserPlugin extends AntlrParserPlugin {
	private ICSTReporter cstReporter;

	CSTParserPlugin(ICSTReporter cstReporter) {
		this.cstReporter = cstReporter;
	}
	
    public Reduction parseCST(final SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
    	Reduction reduction = super.parseCST(sourceUnit, reader);
    	GroovySourceAST cst = (GroovySourceAST) super.ast;
    	if (cst != null) {
    		cstReporter.generatedCST(sourceUnit.getName(), cst);
    	}
		return reduction;
    }
}