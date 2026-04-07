/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.util.Util;

public class LambdaMethod extends SourceMethod {

	private final int sourceStart; // cached for ease of use in hashcode/equals.
	private final String [] parameterNameStrings;
	private final String returnTypeString;
	SourceMethodElementInfo elementInfo;
	private final String key;

	LambdaMethod(JavaElement parent, String name, String key, int sourceStart, String [] parameterTypes, String [] parameterNames, String returnType, SourceMethodElementInfo elementInfo) {
		super(parent, name, parameterTypes);
		this.sourceStart = sourceStart;
		this.parameterNameStrings = (parameterNames == null || parameterNames.length == 0) ? CharOperation.NO_STRINGS
				: parameterNames;
		this.returnTypeString = returnType;
		this.elementInfo = elementInfo;
		this.key = key;
	}

	/**
	 * @see IMethod
	 */
	@Override
	public String getReturnType() throws JavaModelException {
		return this.returnTypeString;
	}
	/**
	 * @see IMethod
	 */
	@Override
	public String getSignature() throws JavaModelException {
		return Signature.createMethodSignature(this.parameterTypes, this.returnTypeString);
	}
	/**
	 * @see IMethod#isLambdaMethod()
	 */
	@Override
	public boolean isLambdaMethod() {
		return true;
	}

	@Override
	protected void closing(Object info) {
		// nothing to do.
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LambdaMethod that)) return false;
		return super.equals(o) && this.sourceStart == that.sourceStart;
	}

	@Override
	protected int calculateHashCode() {
	   return Util.combineHashCodes(super.calculateHashCode(), this.sourceStart);
	}

	@Override
	public SourceMethodElementInfo getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.elementInfo;
	}

	public void getHandleMemento(StringBuilder buff, boolean serializeParent) {
		if (serializeParent) {
			((LambdaExpression) getParent()).getHandleMemento(buff, true, false);
		}
		appendEscapedDelimiter(buff, getHandleMementoDelimiter());
		escapeMementoName(buff, getElementName());
		buff.append(JEM_COUNT);
		buff.append(this.parameterTypes.length);
		for (int i = 0, length = this.parameterTypes.length; i < length; i++) {
			appendEscapedDelimiter(buff, JEM_STRING);
			escapeMementoName(buff, this.parameterTypes[i]);
			appendEscapedDelimiter(buff, JEM_STRING);
			escapeMementoName(buff, this.parameterNameStrings[i]);
		}
		appendEscapedDelimiter(buff, JEM_STRING);
		escapeMementoName(buff, this.returnTypeString);
		appendEscapedDelimiter(buff, JEM_STRING);
		escapeMementoName(buff, this.key);
		ILocalVariable[] arguments = this.elementInfo.arguments;
		for (ILocalVariable argument : arguments) {
			LocalVariable local = (LocalVariable) argument;
			local.getHandleMemento(buff, false);
		}
	}
	@Override
	public void getHandleMemento(StringBuilder buff) {
		getHandleMemento(buff, true);
		// lambda method and lambda expression cannot share the same memento - add a trailing discriminator.
		appendEscapedDelimiter(buff, getHandleMementoDelimiter());
	}

	@Override
	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_LAMBDA_METHOD;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public boolean isResolved() {
		return true;  // we maintain enough information so as not to need another layer of abstraction.
	}

	@Override
	public LambdaMethod resolved(Binding binding) {
		return this;
	}
}
