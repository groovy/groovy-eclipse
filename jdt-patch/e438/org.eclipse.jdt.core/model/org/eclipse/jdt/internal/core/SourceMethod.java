/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Arrays;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see IMethod
 */

public class SourceMethod extends NamedMember implements IMethod {

	/**
	 * The parameter type signatures of the method - stored locally
	 * to perform equality test. <code>null</code> indicates no
	 * parameters.
	 */
	protected String[] parameterTypes;

protected SourceMethod(JavaElement parent, String name, String[] parameterTypes) {
	this(parent, name, parameterTypes, 1);
}

protected SourceMethod(JavaElement parent, String name, String[] parameterTypes, int occurrenceCount) {
	super(parent, name, occurrenceCount);
	// Assertion disabled since bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=179011
	// Assert.isTrue(name.indexOf('.') == -1);
	if (parameterTypes == null) {
		this.parameterTypes= CharOperation.NO_STRINGS;
	} else {
		this.parameterTypes= parameterTypes;
	}
}
@Override
protected void closing(Object info) throws JavaModelException {
	super.closing(info);
	SourceMethodElementInfo elementInfo = (SourceMethodElementInfo) info;
	ITypeParameter[] typeParameters = elementInfo.typeParameters;
	for (ITypeParameter typeParameter : typeParameters) {
		((TypeParameter) typeParameter).close();
	}
}
@Override
public boolean equals(Object o) {
	if (!(o instanceof SourceMethod other)) return false;
	return super.equals(o) && Util.equalArraysOrNull(this.parameterTypes, other.parameterTypes);
}

@Override
protected int calculateHashCode() {
	return Util.combineHashCodes(super.calculateHashCode(), Arrays.hashCode(this.parameterTypes));
}

@Override
public IMemberValuePair getDefaultValue() throws JavaModelException {
	SourceMethodElementInfo sourceMethodInfo = (SourceMethodElementInfo) getElementInfo();
	if (sourceMethodInfo.isAnnotationMethod()) {
		return ((SourceAnnotationMethodInfo) sourceMethodInfo).defaultValue;
	}
	return null;
}
/**
 * @see IJavaElement
 */
@Override
public int getElementType() {
	return METHOD;
}
/**
 * @see IMethod
 */
@Override
public String[] getExceptionTypes() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	char[][] exs= info.getExceptionTypeNames();
	return CompilationUnitStructureRequestor.convertTypeNamesToSigs(exs);
}
/**
 * @see JavaElement#getHandleMemento(StringBuilder)
 */
@Override
protected void getHandleMemento(StringBuilder buff) {
	getParent().getHandleMemento(buff);
	char delimiter = getHandleMementoDelimiter();
	buff.append(delimiter);
	escapeMementoName(buff, getElementName());
	for (String parameterType : this.parameterTypes) {
		buff.append(delimiter);
		escapeMementoName(buff, parameterType);
	}
	if (this.getOccurrenceCount() > 1) {
		buff.append(JEM_COUNT);
		buff.append(this.getOccurrenceCount());
	}
}
/**
 * @see JavaElement#getHandleMemento()
 */
@Override
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_METHOD;
}

@Override
public String getKey() {
	try {
		return getKey(this, false/*don't open*/);
	} catch (JavaModelException e) {
		// happen only if force open is true
		return null;
	}
}
/**
 * @see IMethod
 */
@Override
public int getNumberOfParameters() {
	return this.parameterTypes == null ? 0 : this.parameterTypes.length;
}
/**
 * @see IMethod
 */
@Override
public String[] getParameterNames() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	char[][] names= info.getArgumentNames();
	return CharOperation.toStrings(names);
}
/**
 * @see IMethod
 */
@Override
public String[] getParameterTypes() {
	return this.parameterTypes;
}

@Override
public ITypeParameter getTypeParameter(String typeParameterName) {
	return new TypeParameter(this, typeParameterName);
}

@Override
public ITypeParameter[] getTypeParameters() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	return info.typeParameters;
}
@Override
public ILocalVariable[] getParameters() throws JavaModelException {
	ILocalVariable[] arguments = ((SourceMethodElementInfo) getElementInfo()).arguments;
	if (arguments == null)
		return LocalVariable.NO_LOCAL_VARIABLES;
	return arguments;
}
/**
 * @see IMethod#getTypeParameterSignatures()
 * @since 3.0
 * @deprecated
 */
@Override
public String[] getTypeParameterSignatures() throws JavaModelException {
	ITypeParameter[] typeParameters = getTypeParameters();
	int length = typeParameters.length;
	String[] typeParameterSignatures = new String[length];
	for (int i = 0; i < length; i++) {
		TypeParameter typeParameter = (TypeParameter) typeParameters[i];
		TypeParameterElementInfo info = (TypeParameterElementInfo) typeParameter.getElementInfo();
		char[][] bounds = info.bounds;
		if (bounds == null) {
			typeParameterSignatures[i] = Signature.createTypeParameterSignature(typeParameter.getElementName(), CharOperation.NO_STRINGS);
		} else {
			int boundsLength = bounds.length;
			char[][] boundSignatures = new char[boundsLength][];
			for (int j = 0; j < boundsLength; j++) {
				boundSignatures[j] = Signature.createCharArrayTypeSignature(bounds[j], false);
			}
			typeParameterSignatures[i] = new String(Signature.createTypeParameterSignature(typeParameter.getElementName().toCharArray(), boundSignatures));
		}
	}
	return typeParameterSignatures;
}

@Override
public JavaElement getPrimaryElement(boolean checkOwner) {
	if (checkOwner) {
		CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
		if (cu.isPrimary()) return this;
	}
	IJavaElement primaryParent = this.getParent().getPrimaryElement(false);
	return (JavaElement) ((IType)primaryParent).getMethod(this.name, this.parameterTypes);
}
@Override
public String[] getRawParameterNames() throws JavaModelException {
	return getParameterNames();
}
/**
 * @see IMethod
 */
@Override
public String getReturnType() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	return Signature.createTypeSignature(info.getReturnTypeName(), false);
}
/**
 * @see IMethod
 */
@Override
public String getSignature() throws JavaModelException {
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	return Signature.createMethodSignature(this.parameterTypes, Signature.createTypeSignature(info.getReturnTypeName(), false));
}
/**
 * @see IMethod
 */
@Override
public boolean isConstructor() throws JavaModelException {
	if (!getElementName().equals(this.getParent().getElementName())) {
		// faster than reaching the info
		return false;
	}
	SourceMethodElementInfo info = (SourceMethodElementInfo) getElementInfo();
	return info.isConstructor();
}
/**
 * @see IMethod#isMainMethod()
 */
@Override
public boolean isMainMethod() throws JavaModelException {
	return this.isMainMethod(this);
}

@Override
public boolean isMainMethodCandidate() throws JavaModelException {
	return this.isMainMethodCandidate(this);
}

/**
 * @see IMethod#isLambdaMethod()
 */
@Override
public boolean isLambdaMethod() {
	return false;
}

@Override
public boolean isResolved() {
	return false;
}
/**
 * @see IMethod#isSimilar(IMethod)
 */
@Override
public boolean isSimilar(IMethod method) {
	return
		areSimilarMethods(
			getElementName(), getParameterTypes(),
			method.getElementName(), method.getParameterTypes(),
			null);
}

@Override
public String readableName() {

	StringBuilder buffer = new StringBuilder(super.readableName());
	buffer.append('(');
	int length;
	if (this.parameterTypes != null && (length = this.parameterTypes.length) > 0) {
		for (int i = 0; i < length; i++) {
			buffer.append(Signature.toString(this.parameterTypes[i]));
			if (i < length - 1) {
				buffer.append(", "); //$NON-NLS-1$
			}
		}
	}
	buffer.append(')');
	return buffer.toString();
}
@Override
public JavaElement resolved(Binding binding) {
	return new ResolvedSourceMethod(this.getParent(), this.name, this.parameterTypes, new String(binding.computeUniqueKey()), this.getOccurrenceCount());
}
/**
 * for debugging only
 */
@Override
protected void toStringInfo(int tab, StringBuilder buffer, Object info, boolean showResolvedInfo) {
	buffer.append(tabString(tab));
	if (info == null) {
		toStringName(buffer);
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		toStringName(buffer);
	} else {
		SourceMethodElementInfo methodInfo = (SourceMethodElementInfo) info;
		int flags = methodInfo.getModifiers();
		if (Flags.isStatic(flags)) {
			buffer.append("static "); //$NON-NLS-1$
		}
		if (!methodInfo.isConstructor()) {
			buffer.append(methodInfo.getReturnTypeName());
			buffer.append(' ');
		}
		toStringName(buffer, flags);
	}
}
@Override
protected void toStringName(StringBuilder buffer) {
	toStringName(buffer, 0);
}
protected void toStringName(StringBuilder buffer, int flags) {
	buffer.append(getElementName());
	buffer.append('(');
	String[] parameters = getParameterTypes();
	int length;
	if (parameters != null && (length = parameters.length) > 0) {
		boolean isVarargs = Flags.isVarargs(flags);
		for (int i = 0; i < length; i++) {
			try {
				if (i < length - 1) {
					buffer.append(Signature.toString(parameters[i]));
					buffer.append(", "); //$NON-NLS-1$
				} else if (isVarargs) {
					// remove array from signature
					String parameter = parameters[i].substring(1);
					buffer.append(Signature.toString(parameter));
					buffer.append(" ..."); //$NON-NLS-1$
				} else {
					buffer.append(Signature.toString(parameters[i]));
				}
			} catch (IllegalArgumentException e) {
				// parameter signature is malformed
				buffer.append("*** invalid signature: "); //$NON-NLS-1$
				buffer.append(parameters[i]);
			}
		}
	}
	buffer.append(')');
	if (this.getOccurrenceCount() > 1) {
		buffer.append("#"); //$NON-NLS-1$
		buffer.append(this.getOccurrenceCount());
	}
}
}
