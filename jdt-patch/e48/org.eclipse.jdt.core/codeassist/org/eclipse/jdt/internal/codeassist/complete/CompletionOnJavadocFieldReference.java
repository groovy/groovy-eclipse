/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnJavadocFieldReference extends JavadocFieldReference implements CompletionOnJavadoc {
//	public boolean completionInText;
	public int completionFlags = JAVADOC;
	public int separatorPosition;

	public CompletionOnJavadocFieldReference(Expression receiver, int tag, int position, int separatorPos, char[] name) {
		super(null, (((long)position)<<32)+position-1);
		this.receiver = receiver;
		this.tagSourceStart = position;
		this.tagSourceEnd = position;
		this.tagValue = tag;
		this.separatorPosition = separatorPos;
	}

	public CompletionOnJavadocFieldReference(JavadocFieldReference fieldRef, int position, char[] name) {
		super(fieldRef.token, fieldRef.nameSourcePosition);
		this.receiver = fieldRef.receiver;
		this.separatorPosition = position;
		this.tagSourceStart = fieldRef.tagSourceStart;
		this.tagSourceEnd = fieldRef.tagSourceEnd;
		this.tagValue = fieldRef.tagValue;
	}

	public CompletionOnJavadocFieldReference(JavadocMessageSend msgSend, int position) {
		super(msgSend.selector, ((msgSend.nameSourcePosition>>32)<<32)+msgSend.sourceEnd);
		this.receiver = msgSend.receiver;
		this.separatorPosition = position;
		this.tagSourceStart = msgSend.tagSourceStart;
		this.tagSourceEnd = msgSend.tagSourceEnd;
		this.tagValue = msgSend.tagValue;
	}

	/**
	 * @param flags The completionFlags to set.
	 */
	public void addCompletionFlags(int flags) {
		this.completionFlags |= flags;
	}

	public boolean completeAnException() {
		return (this.completionFlags & EXCEPTION) != 0;
	}

	public boolean completeInText() {
		return (this.completionFlags & TEXT) != 0;
	}

	public boolean completeBaseTypes() {
		return (this.completionFlags & BASE_TYPES) != 0;
	}

	public boolean completeFormalReference() {
		return (this.completionFlags & FORMAL_REFERENCE) != 0;
	}

	/**
	 * Get completion node flags.
	 *
	 * @return int Flags of the javadoc completion node.
	 */
	public int getCompletionFlags() {
		return this.completionFlags;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference#internalResolveType(org.eclipse.jdt.internal.compiler.lookup.Scope)
	 */
	protected TypeBinding internalResolveType(Scope scope) {

		if (this.token != null) {
			return super.internalResolveType(scope);
		}

		// Resolve only receiver
		if (this.receiver == null) {
			this.actualReceiverType = scope.enclosingSourceType();
		} else if (scope.kind == Scope.CLASS_SCOPE) {
			this.actualReceiverType = this.receiver.resolveType((ClassScope) scope);
		} else {
			this.actualReceiverType = this.receiver.resolveType((BlockScope)scope);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference#printExpression(int, java.lang.StringBuffer)
	 */
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("<CompleteOnJavadocFieldReference:"); //$NON-NLS-1$
		super.printExpression(indent, output);
		indent++;
		if (this.completionFlags > 0) {
			output.append('\n');
			for (int i=0; i<indent; i++) output.append('\t');
			output.append("infos:"); //$NON-NLS-1$
			char separator = 0;
			if (completeAnException()) {
				output.append("exception"); //$NON-NLS-1$
				separator = ',';
			}
			if (completeInText()) {
				if (separator != 0) output.append(separator);
				output.append("text"); //$NON-NLS-1$
				separator = ',';
			}
			if (completeBaseTypes()) {
				if (separator != 0) output.append(separator);
				output.append("base types"); //$NON-NLS-1$
				separator = ',';
			}
			if (completeFormalReference()) {
				if (separator != 0) output.append(separator);
				output.append("formal reference"); //$NON-NLS-1$
				separator = ',';
			}
			output.append('\n');
		}
		indent--;
		for (int i=0; i<indent; i++) output.append('\t');
		return output.append('>');
	}
}
