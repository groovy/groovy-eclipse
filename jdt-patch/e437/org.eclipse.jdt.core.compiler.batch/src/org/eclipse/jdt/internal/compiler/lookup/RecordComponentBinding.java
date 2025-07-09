/*******************************************************************************
 * Copyright (c) 2020, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.RecordComponent;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public class RecordComponentBinding extends VariableBinding {

	public ReferenceBinding declaringRecord;

	public RecordComponentBinding(ReferenceBinding declaringRecord, RecordComponent declaration, TypeBinding type, int modifiers) {
		super(declaration.name, type, modifiers,  null);
		this.declaringRecord = declaringRecord;
		declaration.binding = this;
	}
	public RecordComponentBinding(char[] name, TypeBinding type, int modifiers, ReferenceBinding declaringClass) {
		super(name, type, modifiers, null);
		this.declaringRecord = declaringClass;
	}

	/* API
	* Answer the receiver's binding type from Binding.BindingID.
	*/
	@Override
	public final int kind() {
		return RECORD_COMPONENT;
	}

	/*
	 * declaringUniqueKey # recordComponentName
	 *    p.X (int first, int second) { }  --> Lp/X;#first
	 */
	@Override
	public char[] computeUniqueKey(boolean isLeaf) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(this.declaringRecord.computeUniqueKey(false/*not a leaf*/));
		// variable name
		buffer.append('#');
		buffer.append(this.name);

		int length = buffer.length();
		char[] uniqueKey = new char[length];
		buffer.getChars(0, length, uniqueKey, 0);
		return uniqueKey;
	}

	/**<pre>{@code
	 * X<T> t   -->  LX<TT;>;
	 *}</pre>
	 */
	public char[] genericSignature() {
	    if ((this.modifiers & ExtraCompilerModifiers.AccGenericSignature) == 0) return null;
	    return this.type.genericTypeSignature();
	}

	@Override
	public AnnotationBinding[] getAnnotations() {
		RecordComponentBinding originalRecordComponentBinding = original();
		ReferenceBinding declaringRecordBinding = originalRecordComponentBinding.declaringRecord;
		if (declaringRecordBinding == null) {
			return Binding.NO_ANNOTATIONS;
		}
		return declaringRecordBinding.retrieveAnnotations(originalRecordComponentBinding);
	}

	@Override
	public long getAnnotationTagBits() {
		RecordComponentBinding originalRecordComponentBinding = original();
		if ((originalRecordComponentBinding.extendedTagBits & ExtendedTagBits.AnnotationResolved) == 0 &&
				originalRecordComponentBinding.declaringRecord instanceof SourceTypeBinding) {
			ClassScope scope = ((SourceTypeBinding) originalRecordComponentBinding.declaringRecord).scope;
			if (scope == null) {// should not be true - but safety net
				this.extendedTagBits |= ExtendedTagBits.AllAnnotationsResolved;
				return 0;
			}
			TypeDeclaration typeDecl = scope.referenceContext;
			RecordComponent recordComponent = typeDecl.declarationOf(originalRecordComponentBinding);
			if (recordComponent != null) {
				ASTNode.resolveAnnotations(typeDecl.initializerScope, recordComponent.annotations, originalRecordComponentBinding);
			}
		}
		return originalRecordComponentBinding.tagBits;
	}

	@Override
	public ReferenceBinding getDeclaringClass() {
		return this.declaringRecord;
	}

	/**
	 * Returns the original RecordComponent (as opposed to parameterized instances)
	 */
	public RecordComponentBinding original() {
		return this;
	}

	@Override
	public void setAnnotations(AnnotationBinding[] annotations, boolean forceStore) {
		this.declaringRecord.storeAnnotations(this, annotations, forceStore);
	}

	public RecordComponent sourceRecordComponent() {
		if (this.declaringRecord instanceof SourceTypeBinding sourceType) {
			for (RecordComponent component : sourceType.scope.referenceContext.recordComponents)
				if (this == component.binding)
					return component;
		}
		return null;
	}
}
