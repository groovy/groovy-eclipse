package org.codehaus.groovy.eclipse.codebrowsing.astfinders;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;

// TODO: this doesn't take care of nested closures.

/**
 * Given some lineNumber, columnNumber, find the closure surrounding the
 * lineNumber, columnNumber.
 * 
 * @author emp
 */
public class FindSurroundingClosure extends ClassCodeVisitorSupport {
	private ModuleNode moduleNode;

	private ClassNode classNode;

	private int lineNumber;

	@Override
    protected SourceUnit getSourceUnit() {
		// Nothing to do.
		return null;
	}

	/**
	 * Find a ClosureExpression surrounding the given lineNumber, columnNumber.
	 * If a ClosureExpression is found, the identifier field is set to
	 * 'closure', and the lineNumber, columnNumber is set to the found closure
	 * AST lineNumber, columnNumber.
	 * 
	 * @param moduleNode
	 * @param lineNumber
	 * @param columnNumber
	 * @throws ASTNodeFoundException
	 */
	public void doFind(ModuleNode moduleNode, int lineNumber, int columnNumber) {
		this.moduleNode = moduleNode;
		this.lineNumber = lineNumber;
//		this.columnNumber = columnNumber;

		List<ClassNode> classes = moduleNode.getClasses();
		for (Iterator<ClassNode> iter = classes.iterator(); iter.hasNext();) {
			classNode = iter.next();
			classNode.visitContents(this);
		}
	}

	@Override
    public void visitClosureExpression(ClosureExpression expression) {
		/*
		 * If closure bodies were included in the lineNumber, lastLineNumber, this
		 * would be as easy as FindSurroundingMethod. For some reason only the first
		 * line of the closure is included.
		 */
		if (expression.getCode() instanceof BlockStatement) {
			BlockStatement block = (BlockStatement)expression.getCode();
			int line0 = expression.getLineNumber();
			int line1 = 0;
			for (Statement statement : (Iterable<Statement>) block.getStatements()) {
				line1 = statement.getLastLineNumber();
			}
			
			if (line0 <= lineNumber && lineNumber <= line1) {
				throw new ASTNodeFoundException(moduleNode, classNode, expression,
						"closure");
			}
		}
		super.visitClosureExpression(expression);
	}
}