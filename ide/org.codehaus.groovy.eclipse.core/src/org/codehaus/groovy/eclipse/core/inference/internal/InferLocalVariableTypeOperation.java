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
					.typeEvaluationContext(evalContext).project(evalContext.getProject())
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
