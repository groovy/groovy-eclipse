/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.extractMethod;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

/**
 * Scans a Method if it contains a constructor call (super or this)
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class ExtractConstructorTest extends CodeVisitorSupport {

	private boolean constructorCall = false;
	
	public boolean containsConstructorCall(MethodNode method) {
		constructorCall = false;
		visitBlockStatement((BlockStatement) method.getCode());
		return constructorCall;
	}
	
	@Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
		constructorCall = true;
		super.visitConstructorCallExpression(call);
	}

}
