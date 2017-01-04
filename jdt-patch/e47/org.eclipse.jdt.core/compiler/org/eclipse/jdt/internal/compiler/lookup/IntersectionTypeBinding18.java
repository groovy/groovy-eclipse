/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *							Bug 423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference"
 *							Bug 426676 - [1.8][compiler] Wrong generic method type inferred from lambda expression
 *							Bug 426542 - [1.8] Most specific method not picked when one method has intersection type as type parameter
 *							Bug 428019 - [1.8][compiler] Type inference failure with nested generic invocation.
 *     Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 405104 - [1.8][compiler][codegen] Implement support for serializeable lambdas
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Set;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class IntersectionTypeBinding18 extends ReferenceBinding { // abstraction used for intersection casts in Java 8 + type inference at 1.8+

	public ReferenceBinding [] intersectingTypes;
	private ReferenceBinding javaLangObject;
	int length;
	
	public IntersectionTypeBinding18(ReferenceBinding[] intersectingTypes, LookupEnvironment environment) {
		this.intersectingTypes = intersectingTypes;
		this.length = intersectingTypes.length;
		if (!intersectingTypes[0].isClass()) {
			this.javaLangObject = environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null);
			this.modifiers |= ClassFileConstants.AccInterface;
		}
	}
	
	private IntersectionTypeBinding18(IntersectionTypeBinding18 prototype) {
		this.intersectingTypes = prototype.intersectingTypes;
		this.length = prototype.length;
		if (!this.intersectingTypes[0].isClass()) {
			this.javaLangObject = prototype.javaLangObject;
			this.modifiers |= ClassFileConstants.AccInterface;
		}
	}
	
	@Override
	public TypeBinding clone(TypeBinding enclosingType) {
		return new IntersectionTypeBinding18(this);
	}

	@Override
	protected MethodBinding[] getInterfaceAbstractContracts(Scope scope, boolean replaceWildcards) throws InvalidInputException {
		int typesLength = this.intersectingTypes.length;
		MethodBinding[][] methods = new MethodBinding[typesLength][];
		int contractsLength = 0;
		for (int i = 0; i < typesLength; i++) {
			methods[i] = this.intersectingTypes[i].getInterfaceAbstractContracts(scope, replaceWildcards);
			contractsLength += methods[i].length;
		}
		MethodBinding[] contracts = new MethodBinding[contractsLength];
		int idx = 0;
		for (int i = 0; i < typesLength; i++) {
			int len = methods[i].length;
			System.arraycopy(methods[i], 0, contracts, idx, len);
			idx += len;
		}
		return contracts;
	}

	public boolean hasTypeBit(int bit) { // Stephan ??
		for (int i = 0; i < this.length; i++) {		
			if (this.intersectingTypes[i].hasTypeBit(bit))
				return true;
		}
		return false;
	}

	public boolean canBeInstantiated() {
		return false;
	}
	
	public boolean canBeSeenBy(PackageBinding invocationPackage) {
		for (int i = 0; i < this.length; i++) {
			if (!this.intersectingTypes[i].canBeSeenBy(invocationPackage))
				return false;
		}
		return true;
	}
	
	public boolean canBeSeenBy(Scope scope) {
		for (int i = 0; i < this.length; i++) {
			if (!this.intersectingTypes[i].canBeSeenBy(scope))
				return false;
		}
		return true;
	}
	
	public boolean canBeSeenBy(ReferenceBinding receiverType, ReferenceBinding invocationType) {
		for (int i = 0; i < this.length; i++) {
			if (!this.intersectingTypes[i].canBeSeenBy(receiverType, invocationType))
				return false;
		}
		return true;
	}
	
	
	public char[] constantPoolName() {
		return this.intersectingTypes[0].constantPoolName();
	}

	public PackageBinding getPackage() {
		throw new UnsupportedOperationException(); // cannot be referred to
	}
	
	public ReferenceBinding[] getIntersectingTypes() {
		return this.intersectingTypes;
	}

	public ReferenceBinding superclass() {
		return this.intersectingTypes[0].isClass() ? this.intersectingTypes[0] : this.javaLangObject; 
	}
	
	public ReferenceBinding [] superInterfaces() {
		if (this.intersectingTypes[0].isClass()) {
			ReferenceBinding [] superInterfaces = new ReferenceBinding[this.length - 1];
			System.arraycopy(this.intersectingTypes, 1, superInterfaces, 0, this.length - 1);
			return superInterfaces;
		}
		return this.intersectingTypes;
	}
	
	@Override
	public boolean isBoxedPrimitiveType() {
		return this.intersectingTypes[0].isBoxedPrimitiveType();
	}
	
	/* Answer true if the receiver type can be assigned to the argument type (right)
	 */
	public boolean isCompatibleWith(TypeBinding right, Scope scope) {

		// easy way out?
		if (TypeBinding.equalsEquals(this, right))
			return true;

		// need to compare two intersection types?
		int rightKind = right.kind();
		TypeBinding[] rightIntersectingTypes = null;
		if (rightKind == INTERSECTION_TYPE && right.boundKind() == Wildcard.EXTENDS) {
			TypeBinding allRightBounds = ((WildcardBinding) right).allBounds();
			if (allRightBounds instanceof IntersectionTypeBinding18)
				rightIntersectingTypes = ((IntersectionTypeBinding18) allRightBounds).intersectingTypes;
		} else if (rightKind == INTERSECTION_TYPE18) {
			rightIntersectingTypes = ((IntersectionTypeBinding18) right).intersectingTypes;
		}
		if (rightIntersectingTypes != null) {
			int numRequired = rightIntersectingTypes.length;
			TypeBinding[] required = new TypeBinding[numRequired];
			System.arraycopy(rightIntersectingTypes, 0, required, 0, numRequired);
			for (int i = 0; i < this.length; i++) {
				TypeBinding provided = this.intersectingTypes[i];
				for (int j = 0; j < required.length; j++) {
					if (required[j] == null) continue;
					if (provided.isCompatibleWith(required[j], scope)) {
						required[j] = null;
						if (--numRequired == 0)
							return true;
						break;
					}
				}
			}
			return false;
		}

		// normal case:
		for (int i = 0; i < this.length; i++) {		
			if (this.intersectingTypes[i].isCompatibleWith(right, scope))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isSubtypeOf(TypeBinding other) {
		if (TypeBinding.equalsEquals(this, other))
			return true;
		for (int i = 0; i < this.intersectingTypes.length; i++) {
			if (this.intersectingTypes[i].isSubtypeOf(other))
				return true;
		}
		return false;
	}

	public char[] qualifiedSourceName() {
		StringBuffer qualifiedSourceName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				qualifiedSourceName.append(this.intersectingTypes[i].qualifiedSourceName());
				if (i != this.length - 1)
					qualifiedSourceName.append(" & "); //$NON-NLS-1$
		}
		return qualifiedSourceName.toString().toCharArray();
	}

	public char[] sourceName() {
		StringBuffer srcName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				srcName.append(this.intersectingTypes[i].sourceName());
				if (i != this.length - 1)
					srcName.append(" & "); //$NON-NLS-1$
		}
		return srcName.toString().toCharArray();
	}

	public char[] readableName() {
		StringBuffer readableName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				readableName.append(this.intersectingTypes[i].readableName());
				if (i != this.length - 1)
					readableName.append(" & "); //$NON-NLS-1$
		}
		return readableName.toString().toCharArray();
	}
	public char[] shortReadableName() {
		StringBuffer shortReadableName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				shortReadableName.append(this.intersectingTypes[i].shortReadableName());
				if (i != this.length - 1)
					shortReadableName.append(" & "); //$NON-NLS-1$
		}
		return shortReadableName.toString().toCharArray();
	}
	public boolean isIntersectionType18() {
		return true;
	}
	public int kind() {
		return Binding.INTERSECTION_TYPE18;
	}
	public String debugName() {
		StringBuffer debugName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				debugName.append(this.intersectingTypes[i].debugName());
				if (i != this.length - 1)
					debugName.append(" & "); //$NON-NLS-1$
		}
		return debugName.toString();
	}
	public String toString() {
	    return debugName();
	}

	public TypeBinding getSAMType(Scope scope) {
		for (int i = 0, max = this.intersectingTypes.length; i < max; i++) {
			TypeBinding typeBinding = this.intersectingTypes[i];
			MethodBinding methodBinding = typeBinding.getSingleAbstractMethod(scope, true);
			// Why doesn't getSingleAbstractMethod do as the javadoc says, and return null
			// when it is not a SAM type
			if (methodBinding != null && methodBinding.problemId() != ProblemReasons.NoSuchSingleAbstractMethod) {
				return typeBinding; // answer the first SAM we find
			}
		}
		return null;
	}

	@Override
	void collectInferenceVariables(Set<InferenceVariable> variables) {
		for (int i = 0; i < this.intersectingTypes.length; i++)
			this.intersectingTypes[i].collectInferenceVariables(variables);
	}
	
	@Override
	public boolean mentionsAny(TypeBinding[] parameters, int idx) {
		if (super.mentionsAny(parameters, idx))
			return true;
		for (int i = 0; i < this.intersectingTypes.length; i++) {
			if (this.intersectingTypes[i].mentionsAny(parameters, -1))
				return true;
		}
		return false;
	}
	@Override
	public long updateTagBits() {
		for (TypeBinding intersectingType : this.intersectingTypes)
			this.tagBits |= intersectingType.updateTagBits();
		return super.updateTagBits();
	}
}
