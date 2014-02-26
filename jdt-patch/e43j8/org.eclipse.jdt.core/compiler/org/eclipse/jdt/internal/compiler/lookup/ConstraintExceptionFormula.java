/*******************************************************************************
 * Copyright (c) 2013, 2014 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;

/**
 * Constraint formula expressing that a given expression must have an exception type.
 * <ul>
 * <li>Expression contains<sub>throws</sub> T</li>
 * </ul>
 */
public class ConstraintExceptionFormula extends ConstraintFormula {

	FunctionalExpression left;
	
	public ConstraintExceptionFormula(FunctionalExpression left, TypeBinding type) {
		this.left = left;
		this.right = type;
		this.relation = EXCEPTIONS_CONTAINED;
	}
	
	public Object reduce(InferenceContext18 inferenceContext) {
		// JLS 18.2.5
		Scope scope = inferenceContext.scope;
		if (!this.right.isFunctionalInterface(scope))
			return FALSE;
		MethodBinding sam = this.right.getSingleAbstractMethod(scope, true);
		if (sam == null)
			return FALSE;
		if (this.left instanceof LambdaExpression) {
			if (((LambdaExpression)this.left).argumentsTypeElided()) {
				int nParam = sam.parameters.length;
				for (int i = 0; i < nParam; i++)
					if (!sam.parameters[i].isProperType(true))
						return FALSE;
			}
			if (sam.returnType != TypeBinding.VOID && !sam.returnType.isProperType(true))
				return FALSE;
		} else { // reference expression
			if (!((ReferenceExpression)this.left).isExactMethodReference()) {					
				int nParam = sam.parameters.length;
				for (int i = 0; i < nParam; i++)
					if (!sam.parameters[i].isProperType(true))
						return FALSE;
				if (sam.returnType != TypeBinding.VOID && !sam.returnType.isProperType(true))
					return FALSE;
			}
		}
		TypeBinding[] thrown = sam.thrownExceptions;
		TypeBinding[] e = new TypeBinding[thrown.length];
		int n = 0;
		for (int i = 0; i < thrown.length; i++)
			if (!thrown[i].isProperType(true))
				e[n++] = thrown[i];
		TypeBinding[] ePrime = null;
		if (this.left instanceof LambdaExpression) {
// TODO find exceptions thrown by the lambda's body, see 18.2.5 bullet 5
//				((LambdaExpression)this.left).
//				InferenceContext18.missingImplementation("NYI");
		} else {
			ReferenceExpression referenceExpression = (ReferenceExpression)this.left;
			MethodBinding method = referenceExpression.findCompileTimeMethodTargeting(this.right, scope);
			if (method != null)
				ePrime = method.thrownExceptions;
		}
		if (ePrime == null)
			return TRUE; // TODO is it a bug if we actually get here?
		int m = ePrime.length;
		if (n == 0) {
			actual: for (int i = 0; i < m; i++) {
				for (int j = 0; j < thrown.length; j++)
					if (ePrime[i].isCompatibleWith(thrown[j]))
						continue actual;
				return FALSE;
			}
			return TRUE;
		} else {
			List<ConstraintFormula> result = new ArrayList<ConstraintFormula>();
			actual: for (int i = 0; i < m; i++) {
				for (int j = 0; j < thrown.length; j++)
					if (thrown[j].isProperType(true) && ePrime[i].isCompatibleWith(thrown[j]))
						continue actual;
				for (int j = 0; j < n; j++)
					result.add(new ConstraintTypeFormula(ePrime[i], e[j], SUBTYPE));
			}				
			for (int j = 0; j < n; j++)
				result.add(new ConstraintExceptionFormula(this.left, e[j]));
			return result.toArray(new ConstraintFormula[result.size()]);
		}
	}

	Collection<InferenceVariable> inputVariables(final InferenceContext18 context) {
		// from 18.5.2.
		if (this.left instanceof LambdaExpression) {
			if (this.right instanceof InferenceVariable) {
				return Collections.singletonList((InferenceVariable)this.right);
			}
			if (this.right.isFunctionalInterface(context.scope)) {
				LambdaExpression lambda = (LambdaExpression) this.left;
				MethodBinding sam = this.right.getSingleAbstractMethod(context.scope, true); // TODO derive with target type?
				final Set<InferenceVariable> variables = new HashSet<InferenceVariable>();
				if (lambda.argumentsTypeElided()) {
					// i)
					int len = sam.parameters.length;
					for (int i = 0; i < len; i++) {
						sam.parameters[i].collectInferenceVariables(variables);
					}
				} 
				if (sam.returnType != TypeBinding.VOID) {
					// ii)
					sam.returnType.collectInferenceVariables(variables);
				}
				return variables;
			}
		} else if (this.left instanceof ReferenceExpression) {
			if (this.right instanceof InferenceVariable) {
				return Collections.singletonList((InferenceVariable)this.right);
			}
			if (this.right.isFunctionalInterface(context.scope)) { // TODO: && this.left is inexact
				MethodBinding sam = this.right.getSingleAbstractMethod(context.scope, true); // TODO derive with target type?
				final Set<InferenceVariable> variables = new HashSet<InferenceVariable>();
				int len = sam.parameters.length;
				for (int i = 0; i < len; i++) {
					sam.parameters[i].collectInferenceVariables(variables);
				}
				sam.returnType.collectInferenceVariables(variables);
				return variables;
			}
		}
		return EMPTY_VARIABLE_LIST;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer().append(LEFT_ANGLE_BRACKET);
		this.left.printExpression(4, buf);
		buf.append(" \u2286throws "); //$NON-NLS-1$
		appendTypeName(buf, this.right);
		buf.append(RIGHT_ANGLE_BRACKET);
		return buf.toString();
	}
}
