/*******************************************************************************
 * Copyright (c) 2025 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

/**
 * Common properties of type declarations and lambda expressions.
 * <p>
 * In terms of access to enclosing instances both type declarations and lambda expressions
 * are barriers which need to be bridged with synthetic arguments (and fields in the case of types).
 * </p>
 */
public interface TypeOrLambda {

	/**
	 * If the current type or lambda is within some early construction context, then next enclosing
	 * instance may need to be managed via a synthetic argument (and field in the case of types).
	 * @param earlySeen are we already looking from an early construction context?
	 * @param outerScope where to search for enclosing types to be managed
	 */
	default void addSyntheticArgumentsBeyondEarlyConstructionContext(boolean earlySeen, Scope outerScope) {
		if (outerScope != null && JavaFeature.FLEXIBLE_CONSTRUCTOR_BODIES.isSupported(outerScope.compilerOptions())) {
			// JEP 513:
			// This is the central location for organizing synthetic arguments and fields
			// to serve far outer instances even in inner early construction context.
			// Locations MethodBinding.computeSignature() and BlockScope.getEmulationPath() will faithfully
			// use the information generated here, to decide about signature and call sequence.
			while (outerScope != null) {
				if (outerScope instanceof ClassScope cs) {
					if (earlySeen && !cs.insideEarlyConstructionContext) {
						// a direct outer beyond an early construction context disrupts
						// the chain of fields, supply a local copy instead (arg & field):
						ensureSyntheticOuterAccess(cs.referenceContext.binding);
					}
					earlySeen = cs.insideEarlyConstructionContext;
					if (cs.referenceContext.binding != null && cs.referenceContext.binding.isStatic())
						break;
				}
				outerScope = outerScope.parent;
				if (outerScope instanceof MethodScope ms && ms.isStatic)
					break;
			}
		}
	}

	void ensureSyntheticOuterAccess(SourceTypeBinding targetEnclosing);
}
