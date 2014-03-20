/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Util;

public class LambdaExpression extends SourceType {

	SourceTypeElementInfo elementInfo;
	LambdaMethod lambdaMethod;
	
	// These fields could be materialized from elementInfo, but for ease of use stashed here 
	protected int sourceStart;
	protected int sourceEnd;
	protected int arrowPosition;
	protected String interphase;
	
	
	// Construction from AST node
	public LambdaExpression(JavaElement parent, org.eclipse.jdt.internal.compiler.ast.LambdaExpression lambdaExpression) {
		super(parent, new String("Lambda(") + new String(lambdaExpression.resolvedType.sourceName()) + ')'); //$NON-NLS-1$
		this.sourceStart = lambdaExpression.sourceStart;
		this.sourceEnd = lambdaExpression.sourceEnd;
		this.arrowPosition = lambdaExpression.arrowPosition;
		this.interphase = new String(CharOperation.replaceOnCopy(lambdaExpression.resolvedType.genericTypeSignature(), '/', '.'));
		this.elementInfo = makeTypeElementInfo(this, this.interphase, this.sourceStart, this.sourceEnd, this.arrowPosition); 
		this.lambdaMethod = LambdaMethod.make(this, lambdaExpression);
		this.elementInfo.children = new IJavaElement[] { this.lambdaMethod };
	}
	
	// Construction from memento
	public LambdaExpression(JavaElement parent, String name, String interphase, int sourceStart, int sourceEnd, int arrowPosition) {
		super(parent, name);
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.arrowPosition = arrowPosition;
		this.interphase = interphase;
		this.elementInfo = makeTypeElementInfo(this, interphase, this.sourceStart = sourceStart, sourceEnd, arrowPosition);
		// Method is in the process of being fabricated, will be attached shortly.
	}
	
	// Construction from subtypes.
	public LambdaExpression(JavaElement parent, String name, String interphase, int sourceStart, int sourceEnd, int arrowPosition, LambdaMethod lambdaMethod) {
		super(parent, name);
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.arrowPosition = arrowPosition;
		this.interphase = interphase;
		this.elementInfo = makeTypeElementInfo(this, interphase, this.sourceStart = sourceStart, sourceEnd, arrowPosition);
		this.elementInfo.children = new IJavaElement[] { this.lambdaMethod = lambdaMethod };
	}
	
	// Lambda expression is not backed by model, fabricate element information structure and stash it.
	static private SourceTypeElementInfo makeTypeElementInfo (LambdaExpression handle, String interphase, int sourceStart, int sourceEnd, int arrowPosition) {
		
		SourceTypeElementInfo elementInfo = new SourceTypeElementInfo();
		
		elementInfo.setFlags(0);
		elementInfo.setHandle(handle);
		elementInfo.setSourceRangeStart(sourceStart);
		elementInfo.setSourceRangeEnd(sourceEnd);
		
		elementInfo.setNameSourceStart(sourceStart);
		elementInfo.setNameSourceEnd(arrowPosition);
		elementInfo.setSuperclassName(null);
		elementInfo.addCategories(handle, null);
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		char[][] superinterfaces = new char [][] { manager.intern(Signature.toString(interphase).toCharArray()) }; // drops marker interfaces - to fix.
		elementInfo.setSuperInterfaceNames(superinterfaces);
		return elementInfo;
	}
	
	protected void closing(Object info) throws JavaModelException {
		// nothing to do, not backed by model ATM.
	}
	
	public boolean equals(Object o) {
		if (this == o)
			return true;
		/* I see cases where equal lambdas are dismissed as unequal on account of working copy owner.
		   This results in spurious failures. See JavaSearchBugs8Tests.testBug400905_0021()
		   For now exclude the working copy owner and compare
		*/
		if (o instanceof LambdaExpression) {
			LambdaExpression that = (LambdaExpression) o;
			if (this.sourceStart != that.sourceStart)
				return false;
			CompilationUnit thisCU = (CompilationUnit) this.getCompilationUnit();
			CompilationUnit thatCU = (CompilationUnit) that.getCompilationUnit();
			return thisCU.getElementName().equals(thatCU.getElementName()) && thisCU.parent.equals(thatCU.parent);
		}
		return false;
	}
	
	public int hashCode() {
		return Util.combineHashCodes(super.hashCode(), this.sourceStart);
	}
	
	public Object getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.elementInfo;
	}

	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_LAMBDA_EXPRESSION;
	}
	
	/*
	 * @see JavaElement#getHandleMemento(StringBuffer)
	 */
	protected void getHandleMemento(StringBuffer buff) {
		getHandleMemento(buff, true, true);
		// lambda method and lambda expression cannot share the same memento - add a trailing discriminator.
		appendEscapedDelimiter(buff, getHandleMementoDelimiter());
	}
	
	protected void getHandleMemento(StringBuffer buff, boolean serializeParent, boolean serializeChild) {
		if (serializeParent) 
			((JavaElement)getParent()).getHandleMemento(buff);
		appendEscapedDelimiter(buff, getHandleMementoDelimiter());
		escapeMementoName(buff, this.name);
		appendEscapedDelimiter(buff, JEM_STRING);
		escapeMementoName(buff, this.interphase);
		buff.append(JEM_COUNT);
		buff.append(this.sourceStart);
		buff.append(JEM_COUNT);
		buff.append(this.sourceEnd);
		buff.append(JEM_COUNT);
		buff.append(this.arrowPosition);
		if (serializeChild)
			this.lambdaMethod.getHandleMemento(buff, false);
	}
	
	public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {

		if (token.charAt(0) != JEM_LAMBDA_METHOD)
			return null;
		
		// ----
		if (!memento.hasMoreTokens()) return this;
		String selector = memento.nextToken();
		if (!memento.hasMoreTokens() || memento.nextToken().charAt(0) != JEM_COUNT) return this;
		if (!memento.hasMoreTokens()) return this;
		int length = Integer.parseInt(memento.nextToken());
		String [] parameterTypes = new String[length];
		String [] parameterNames = new String[length];
		for (int i = 0; i < length; i++) {
			if (!memento.hasMoreTokens() || memento.nextToken().charAt(0) != JEM_STRING) return this;
			parameterTypes[i] = memento.nextToken();
			if (!memento.hasMoreTokens() || memento.nextToken().charAt(0) != JEM_STRING) return this;
			parameterNames[i] = memento.nextToken();
		}
		if (!memento.hasMoreTokens() || memento.nextToken().charAt(0) != JEM_STRING) return this;
		String returnType = memento.nextToken();
		if (!memento.hasMoreTokens() || memento.nextToken().charAt(0) != JEM_STRING) return this;
		String key = memento.nextToken();
		this.lambdaMethod = LambdaMethod.make(this, selector, key, this.sourceStart, this.sourceEnd, this.arrowPosition, parameterTypes, parameterNames, returnType);
		ILocalVariable [] parameters = new ILocalVariable[length];
		for (int i = 0; i < length; i++) {
			parameters[i] = (ILocalVariable) this.lambdaMethod.getHandleFromMemento(memento, workingCopyOwner);
		}
		this.lambdaMethod.elementInfo.arguments  = parameters;
		this.elementInfo.children = new IJavaElement[] { this.lambdaMethod };
		if (!memento.hasMoreTokens())
			return this.lambdaMethod;
		switch (memento.nextToken().charAt(0)) {
			case JEM_LAMBDA_METHOD:
				if (!memento.hasMoreTokens())
					return this.lambdaMethod;
				return this.lambdaMethod.getHandleFromMemento(memento, workingCopyOwner);
			case JEM_LAMBDA_EXPRESSION:
			default:
				return this;	
		}
	}

	public IJavaElement[] getChildren() throws JavaModelException {
		return new IJavaElement[] { this.lambdaMethod };
	}

	public boolean isLocal() {
		return true;
	}
	
	public JavaElement resolved(Binding binding) {
		ResolvedLambdaExpression resolvedHandle = new ResolvedLambdaExpression(this.parent, this, new String(binding.computeUniqueKey()));
		return resolvedHandle;
	}

	public IMethod getMethod() {
		return this.lambdaMethod;
	}
	
	@Override
	public IJavaElement getPrimaryElement(boolean checkOwner) {
		if (checkOwner) {
			CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
			if (cu == null || cu.isPrimary()) return this;
		}
		IJavaElement primaryParent = this.parent.getPrimaryElement(false);
		if (primaryParent instanceof JavaElement) {
			JavaElement ancestor = (JavaElement) primaryParent;
			StringBuffer buffer = new StringBuffer(32);
			getHandleMemento(buffer, false, true);
			String memento = buffer.toString();
			return ancestor.getHandleFromMemento(new MementoTokenizer(memento), DefaultWorkingCopyOwner.PRIMARY).getParent();
		}
		return this;
	}

	public String[] getSuperInterfaceTypeSignatures() throws JavaModelException {
		return new String[] { this.interphase };
	}
}
