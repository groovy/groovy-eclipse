/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.util.Util;

public class LambdaMethod extends SourceMethod {

	private int sourceStart; // cached for ease of use in hashcode/equals.
	private String [] parameterNameStrings;
	private String returnTypeString;
	SourceMethodElementInfo elementInfo;
	private String key;
	
	LambdaMethod(JavaElement parent, String name, String key, int sourceStart, String [] parameterTypes, String [] parameterNames, String returnType, SourceMethodElementInfo elementInfo) {
		super(parent, name, parameterTypes);
		this.sourceStart = sourceStart;
		this.parameterNameStrings = parameterNames;
		this.returnTypeString = returnType;
		this.elementInfo = elementInfo;
		this.key = key;
	}
	
	/**
	 * @see IMethod
	 */
	public String getReturnType() throws JavaModelException {
		return this.returnTypeString;
	}
	/**
	 * @see IMethod
	 */
	public String getSignature() throws JavaModelException {
		return Signature.createMethodSignature(this.parameterTypes, this.returnTypeString);
	}
	/**
	 * @see IMethod#isLambdaMethod()
	 */
	public boolean isLambdaMethod() {
		return true;
	}
	
	protected void closing(Object info) {
		// nothing to do.
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof LambdaMethod)) return false;
		LambdaMethod that = (LambdaMethod) o;
		return super.equals(o) && this.sourceStart == that.sourceStart;
	}

	public Object getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.elementInfo;
	}
	
	public void getHandleMemento(StringBuffer buff, boolean serializeParent) {
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
		for (int i = 0, length = arguments.length; i < length; i++) {
			LocalVariable local = (LocalVariable) arguments[i];
			local.getHandleMemento(buff, false);
		}
	}
	public void getHandleMemento(StringBuffer buff) {
		getHandleMemento(buff, true);
		// lambda method and lambda expression cannot share the same memento - add a trailing discriminator.
		appendEscapedDelimiter(buff, getHandleMementoDelimiter());
	}
	
	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_LAMBDA_METHOD;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public int hashCode() {
	   return Util.combineHashCodes(super.hashCode(), this.sourceStart);
	}
	
	public boolean isResolved() {
		return true;  // we maintain enough information so as not to need another layer of abstraction.
	}
	
	public JavaElement resolved(Binding binding) {
		return this;
	}
}
