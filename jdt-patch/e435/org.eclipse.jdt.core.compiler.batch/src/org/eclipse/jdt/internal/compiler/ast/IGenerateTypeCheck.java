/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.Pattern.PrimitiveConversionRoute;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A mixin for nodes that may generate a runtime type check based on a {@link PrimitiveConversionRoute}
 */
interface IGenerateTypeCheck {

	default void generateTypeCheck(TypeBinding providedType, TypeReference expectedTypeRef, BlockScope scope, CodeStream codeStream, PrimitiveConversionRoute route) {
		switch (route) {
			case IDENTITY_CONVERSION -> {
				consumeProvidedValue(providedType, codeStream);
				codeStream.iconst_1();
				setPatternIsTotalType();
			}
			case WIDENING_PRIMITIVE_CONVERSION,
			NARROWING_PRIMITVE_CONVERSION,
			WIDENING_AND_NARROWING_PRIMITIVE_CONVERSION -> {
				generateExactConversions(providedType, expectedTypeRef.resolvedType, scope, codeStream);
				setPatternIsTotalType();
			}
			case BOXING_CONVERSION,
			BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION -> {
				consumeProvidedValue(providedType, codeStream);
				codeStream.iconst_1();
				setPatternIsTotalType();
			}
			case WIDENING_REFERENCE_AND_UNBOXING_COVERSION,
			WIDENING_REFERENCE_AND_UNBOXING_COVERSION_AND_WIDENING_PRIMITIVE_CONVERSION -> {
				codeStream.instance_of(scope.getJavaLangObject());
				setPatternIsTotalType();
			}
			case NARROWING_AND_UNBOXING_CONVERSION -> {
				TypeBinding boxType = scope.environment().computeBoxingType(expectedTypeRef.resolvedType);
				codeStream.instance_of(expectedTypeRef, boxType);
			}
			case UNBOXING_CONVERSION,
			UNBOXING_AND_WIDENING_PRIMITIVE_CONVERSION -> {
				codeStream.instance_of(scope.getJavaLangObject());
				setPatternIsTotalType();
			}
			case NO_CONVERSION_ROUTE -> {
				codeStream.instance_of(expectedTypeRef, expectedTypeRef.resolvedType);
				break;
			}
			default -> {
				throw new IllegalArgumentException("Unexpected conversion route "+route); //$NON-NLS-1$
			}
		}
	}

	/* Overridden in InstanceOfExpression */
	default void consumeProvidedValue(TypeBinding provided, CodeStream codeStream) {
		codeStream.pop(provided);
	}

	void setPatternIsTotalType();

	default void generateExactConversions(TypeBinding provided, TypeBinding expected, BlockScope scope, CodeStream codeStream) {
		if (BaseTypeBinding.isExactWidening(expected.id, provided.id)) {
			consumeProvidedValue(provided, codeStream);
			codeStream.iconst_1();
		} else {
			codeStream.invokeExactConversionsSupport(BaseTypeBinding.getRightToLeft(expected.id, provided.id));
		}
	}
}