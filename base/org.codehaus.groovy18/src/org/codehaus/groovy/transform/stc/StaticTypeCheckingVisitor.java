package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Actually Groovy 1.8 does not support Static Type Checking, but Type Inference Engine does use it in some cases.
 * This class is created just to prevent some messy problems with very, very old versions of Groovy. 
 *
 * @author denis_murashev
 *
 */
public class StaticTypeCheckingVisitor {

	public StaticTypeCheckingVisitor(SourceUnit sourceUnit,
			ClassNode declaringClass) {
	}

	public ClassNode inferReturnTypeGenerics(ClassNode receiver, MethodNode method, Expression arguments) {
		return method.getReturnType();
	}
}
