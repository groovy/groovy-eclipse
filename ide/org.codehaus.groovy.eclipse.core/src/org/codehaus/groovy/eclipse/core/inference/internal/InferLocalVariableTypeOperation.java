/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.inference.internal;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.core.types.ITypeEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluationContextBuilder;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator;
import org.codehaus.groovy.eclipse.core.types.internal.InferringEvaluationContext;
import org.eclipse.jface.text.Region;

/**
 * Performs inference on local variable expressions. The closest local variable assigment to the current source line/col
 * is used to determine the type.
 * 
 * @author empovazan
 */
public class InferLocalVariableTypeOperation {
	private LocalVariable variable;

	private InferringEvaluationContext evalContext;

	public InferLocalVariableTypeOperation(LocalVariable variable, InferringEvaluationContext evalContext) {
		this.variable = variable;
		this.evalContext = evalContext;
	}

	public LocalVariable getLocalVariable() {
		Expression[] expressions = findAllLocalAssignExpressions(variable, evalContext);
		if (expressions.length != 0) {
			ITypeEvaluationContext newEvalContext = new TypeEvaluationContextBuilder()
					.typeEvaluationContext(evalContext)
					.location(new Region(expressions[0].getStart(), expressions[0].getLength()))
					.done();
			TypeEvaluator eval = new TypeEvaluator(newEvalContext);
			return new LocalVariable(eval.evaluate(expressions[0]).getName(), variable.getName(), true);
		} else {
			return new LocalVariable(variable.getSignature(), variable.getName(), true);
		}
	}

	/**
	 * Finds all assignments to the given variable in the current method, which are before the current context location.
	 * The list is sorted from the closest expression to the first expression. For example: def a = 10; a = 20; a_,
	 * finds [20, 10]
	 * 
	 * @param variable
	 * @param evalContext
	 * @return
	 */
	private Expression[] findAllLocalAssignExpressions(LocalVariable variable, InferringEvaluationContext evalContext) {
		AssignmentExpressionCollector collector = new AssignmentExpressionCollector(evalContext)
				.localName(variable.getName())
				.inCurrentScope()
				.closestToContext()
				.reverseResults();
		return collector.getExpressions();
	}
}
