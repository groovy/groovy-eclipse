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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Abstraction for invocation AST nodes that can trigger 
 * <ul>
 * <li>Invocation Applicability Inferences (18.5.1), and</li> 
 * <li>Invocation Type Inference (18.5.2).</li>
 * </ul>
 */
public interface Invocation extends InvocationSite {

	Expression[] arguments();

	/**
	 * Answer the resolved method binding of this invocation.
	 * If a target type is given, the invocation gets a chance to do repeated method lookup.
	 * @param targetType the target type of this invocation or null if not yet known
	 * @param reportErrors if true then this is the last call, if no valid binding can be answered we should report an error
	 * @param scope if reportErrors is true then this scope can be used for error reporting
	 * 
	 */
	MethodBinding binding(TypeBinding targetType, boolean reportErrors, Scope scope);

	/**
	 * Register the given inference context, which produced the given method as its intermediate result.
	 * Later when the same method is selected as the most specific method, the inference context
	 * for this pair (Invocation x MethodBinding) can be looked up using {@link #getExpressionContext()}
	 * to continue the type inference.
	 */
	void registerInferenceContext(ParameterizedGenericMethodBinding method, InferenceContext18 infCtx18);
	
	/**
	 * Retrieve an inference context for the given method which must have been registered
	 * using {@link #registerInferenceContext(ParameterizedGenericMethodBinding, InferenceContext18)}.
	 * @param method an intermediate resolved candidate for this invocation
	 * return a suspended inference context or null if none was registered for this method.
	 */
	InferenceContext18 getInferenceContext(ParameterizedMethodBinding method);

	/**
	 * Answer true if this invocation has determined its binding using inference.
	 */
	boolean usesInference();
	
	/**
	 * Where the AST node may hold references to the results of Invocation Applicability Inference,
	 * this method allows to update those references to the result of Invocation Type Inference.
	 * Note that potentially more than just the method binding is updated.
	 * @param updatedBinding the final method binding after full inference
	 * @param targetType the target type used during Invocation Type Inference
	 * @return true if an update has happened
	 */
	boolean updateBindings(MethodBinding updatedBinding, TypeBinding targetType);
	
	/**
	 * Answer whether the current invocation has inner expressions that still need updating after inference.
	 */
	boolean innersNeedUpdate();

	/**
	 * Mark that updating (the need for which is signaled via {@link #innersNeedUpdate()}) has been done.
	 */
	void innerUpdateDone();

	/**
	 * If this invocation has any poly expressions as arguments, this method answers an inference helper 
	 * that mediates during overload resolution, even if no actual inference happens for this invocation.
	 */
	InnerInferenceHelper innerInferenceHelper();
	
}
