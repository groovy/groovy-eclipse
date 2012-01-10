/*******************************************************************************
 * Copyright (c) 2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

/**
 * An extension to the {@link ITypeLookup} interface that specifies that a particular expression visit occurs in the context of a
 * static object expression. We use an extension interface because we do not want to break backwards compatibility with existing
 * third party implementors of {@link ITypeLookup}.
 * 
 * @author Andrew Eisenberg
 * @created Jun 9, 2011
 */
public interface ITypeLookupExtension extends ITypeLookup {
	/**
	 * Determine the type for an expression node.
	 * 
	 * @param node the AST Node to determine the type for
	 * @param scope the variable scope at this location
	 * @param objectExpressionType if the parent of node is a {@link PropertyExpression}, then this value contains the type of
	 *        {@link PropertyExpression#getObjectExpression()}, otherwise null
	 * @param isStaticObjectExpression true iff the objectExpressionType is not null and it is referring to the static instance of
	 *        the class declaration
	 * @return the type for the node and confidence in that type, or null if cannot determine
	 */
	TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType,
			boolean isStaticObjectExpression);

	/**
	 * Determines the type inside of a BlockStatement
	 */
	void lookupInBlock(BlockStatement node, VariableScope scope);
}
