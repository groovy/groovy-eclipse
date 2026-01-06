/*******************************************************************************
 * Copyright (c) 2015, 2025 GK Software SE and others.
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

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants.DangerousMethod;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/* @NonNullByDefault */
public class UnlikelyArgumentCheck {
	public final DangerousMethod dangerousMethod;
	public final TypeBinding typeToCheck;
	public final TypeBinding expectedType;
	public final TypeBinding typeToReport;

	// don't call, use the factory.
	private UnlikelyArgumentCheck(DangerousMethod dangerousMethod, TypeBinding typeToCheck, TypeBinding expectedType, TypeBinding typeToReport) {
		this.dangerousMethod = dangerousMethod;
		this.typeToCheck = typeToCheck;
		this.expectedType = expectedType;
		this.typeToReport = typeToReport;
	}

	public static UnlikelyArgumentCheck createUnlikelyArgumentCheck(DangerousMethod dangerousMethod, TypeBinding typeToCheck, TypeBinding expectedType, TypeBinding typeToReport) {
		return typeToCheck == null ? null : new UnlikelyArgumentCheck(dangerousMethod, typeToCheck, expectedType, typeToReport);
	}
	/**
	 * Check if the invocation is likely a bug.
	 * @return false, if the typeToCheck does not seem to related to the expectedType
	 */
	public boolean isDangerous(BlockScope currentScope) {
		TypeBinding typeToCheck2 = this.typeToCheck;
		// take autoboxing into account
		if (typeToCheck2.isBaseType()) {
			typeToCheck2 = currentScope.boxing(typeToCheck2);
		}
		TypeBinding expectedType2 = this.expectedType;
		if (expectedType2.isBaseType()) { // can happen for the first parameter of java.util.Object.equals
			expectedType2 = currentScope.boxing(expectedType2);
		}
		if(this.dangerousMethod != DangerousMethod.Equals && currentScope.compilerOptions().reportUnlikelyCollectionMethodArgumentTypeStrict) {
			return !typeToCheck2.isCompatibleWith(expectedType2, currentScope);
		}
		// unless both types are true type variables (not captures), take the erasure of both.
		if (typeToCheck2.isCapture() || !typeToCheck2.isTypeVariable() || expectedType2.isCapture()
				|| !expectedType2.isTypeVariable()) {
			typeToCheck2 = typeToCheck2.erasure();
			expectedType2 = expectedType2.erasure();
		}
		boolean potentiallyDangerous = !typeToCheck2.isCompatibleWith(expectedType2, currentScope)
				&& !expectedType2.isCompatibleWith(typeToCheck2, currentScope);
		if (potentiallyDangerous
				&& this.dangerousMethod == DangerousMethod.Equals
				&& this.expectedType.kind() != Binding.ARRAY_TYPE
				&& !this.expectedType.isBaseType()
				&& hasEqualsMethodInCommonSuperType(this.expectedType, this.typeToCheck, new HashSet<>())) {
			return false;
		}
		return potentiallyDangerous;
	}

	private boolean hasEqualsMethodInCommonSuperType(TypeBinding type1, TypeBinding type2, Set<TypeBinding> visited) {
		if (type1.id == TypeIds.T_JavaLangObject || type2.id == TypeIds.T_JavaLangObject)
			return false;
		if (!visited.add(type1))
			return false;
		if (type2.isCompatibleWith(type1)) {
			// found a common super type
			for (MethodBinding equalsMethod : type1.getMethods(TypeConstants.EQUALS))
				if (equalsMethod.parameters.length == 1 && equalsMethod.parameters[0].id == TypeIds.T_JavaLangObject)
					return true;
		}
		if (hasEqualsMethodInCommonSuperType(type1.superclass(), type2, visited))
			return true;
		for (ReferenceBinding ifc : type1.superInterfaces()) {
			if (hasEqualsMethodInCommonSuperType(ifc, type2, visited))
				return true;
		}
		return false;
	}

	/**
	 * When targeting a well-known dangerous method, returns an UnlikelyArgumentCheck object that contains the types to
	 * check against each other and to report
	 */
	public static /* @Nullable */ UnlikelyArgumentCheck determineCheckForNonStaticSingleArgumentMethod(
			TypeBinding argumentType, Scope scope, char[] selector, TypeBinding actualReceiverType,
			TypeBinding[] parameters) {
		// detecting only methods with a single argument, typed either as Object or as Collection:
		if (parameters.length != 1)
			return null;
		int paramTypeId = parameters[0].original().id;
		if (paramTypeId != TypeIds.T_JavaLangObject && paramTypeId != TypeIds.T_JavaUtilCollection)
			return null;

		// check selectors before typeBits as to avoid unnecessary super-traversals for the receiver type
		DangerousMethod suspect = DangerousMethod.detectSelector(selector);
		if (suspect == null)
			return null;

		if (actualReceiverType.hasTypeBit(TypeIds.BitMap)) {
			if (paramTypeId == TypeIds.T_JavaLangObject) {
				switch (suspect) {
					case ContainsKey:
					case Get:
					case Remove:
						// map operations taking a key
						ReferenceBinding mapType = actualReceiverType
								.findSuperTypeOriginatingFrom(TypeIds.T_JavaUtilMap, false);
						if (mapType != null && mapType.isParameterizedType())
							return createUnlikelyArgumentCheck(suspect, argumentType,
									((ParameterizedTypeBinding) mapType).typeArguments()[0], mapType);
						break;
					case ContainsValue:
						// map operation taking a value
						mapType = actualReceiverType.findSuperTypeOriginatingFrom(TypeIds.T_JavaUtilMap, false);
						if (mapType != null && mapType.isParameterizedType())
							return createUnlikelyArgumentCheck(suspect, argumentType,
									((ParameterizedTypeBinding) mapType).typeArguments()[1], mapType);
						break;
					default: // no other suspects are detected in java.util.Map
				}
			}
		}
		if (actualReceiverType.hasTypeBit(TypeIds.BitCollection)) {
			if (paramTypeId == TypeIds.T_JavaLangObject) {
				switch (suspect) {
					case Remove:
					case Contains:
						// collection operations taking a single element
						ReferenceBinding collectionType = actualReceiverType
								.findSuperTypeOriginatingFrom(TypeIds.T_JavaUtilCollection, false);
						if (collectionType != null && collectionType.isParameterizedType())
							return createUnlikelyArgumentCheck(suspect, argumentType,
									((ParameterizedTypeBinding) collectionType).typeArguments()[0], collectionType);
						break;
					default: // no other suspects with Object-parameter are detected in java.util.Collection
				}
			} else if (paramTypeId == TypeIds.T_JavaUtilCollection) {
				switch (suspect) {
					case RemoveAll:
					case ContainsAll:
					case RetainAll:
						// collection operations taking another collection
						ReferenceBinding collectionType = actualReceiverType
								.findSuperTypeOriginatingFrom(TypeIds.T_JavaUtilCollection, false);
						ReferenceBinding argumentCollectionType = argumentType
								.findSuperTypeOriginatingFrom(TypeIds.T_JavaUtilCollection, false);
						if (collectionType != null && argumentCollectionType != null
								&& argumentCollectionType.isParameterizedTypeWithActualArguments()
								&& collectionType.isParameterizedTypeWithActualArguments()) {
							return createUnlikelyArgumentCheck(suspect,
									((ParameterizedTypeBinding) argumentCollectionType).typeArguments()[0],
									((ParameterizedTypeBinding) collectionType).typeArguments()[0], collectionType);
						}
						break;
					default: // no other suspects with Collection-parameter are detected in java.util.Collection
				}
			}
			if (actualReceiverType.hasTypeBit(TypeIds.BitList)) {
				if (paramTypeId == TypeIds.T_JavaLangObject) {
					switch (suspect) {
						case IndexOf:
						case LastIndexOf:
							// list operations taking a single element
							ReferenceBinding listType = actualReceiverType
									.findSuperTypeOriginatingFrom(TypeIds.T_JavaUtilList, false);
							if (listType != null && listType.isParameterizedType())
								return createUnlikelyArgumentCheck(suspect, argumentType,
										((ParameterizedTypeBinding) listType).typeArguments()[0], listType);
							break;
						default: // no other suspects are detected in java.util.List
					}
				}
			}
		}
		if (paramTypeId == TypeIds.T_JavaLangObject && suspect == DangerousMethod.Equals) {
			return createUnlikelyArgumentCheck(suspect, argumentType, actualReceiverType, actualReceiverType);
		}
		return null; // not replacing
	}
	public static /* @Nullable */ UnlikelyArgumentCheck determineCheckForStaticTwoArgumentMethod(
			TypeBinding secondParameter, Scope scope, char[] selector, TypeBinding firstParameter,
			TypeBinding[] parameters, TypeBinding actualReceiverType) {
		// detecting only methods with two arguments, both typed as Object:
		if (parameters.length != 2)
			return null;
		int paramTypeId1 = parameters[0].original().id;
		int paramTypeId2 = parameters[1].original().id;

		if (paramTypeId1 != TypeIds.T_JavaLangObject || paramTypeId2 != TypeIds.T_JavaLangObject)
			return null;

		// check selectors before typeBits as to avoid unnecessary super-traversals for the receiver type
		DangerousMethod suspect = DangerousMethod.detectSelector(selector);
		if (suspect == null)
			return null;

		if (actualReceiverType.id == TypeIds.T_JavaUtilObjects && suspect == DangerousMethod.Equals) {
			return createUnlikelyArgumentCheck(suspect, secondParameter, firstParameter, firstParameter);
		}
		return null;
	}
}