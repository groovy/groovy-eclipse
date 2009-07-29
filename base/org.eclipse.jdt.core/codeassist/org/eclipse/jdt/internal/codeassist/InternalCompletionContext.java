/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.Scope;


/**
 * Internal completion context
 * @since 3.1
 */
public class InternalCompletionContext {
	protected char[][] expectedTypesSignatures;
	protected char[][] expectedTypesKeys;
	protected int javadoc;
	
	protected int offset = -1;
	protected int tokenStart = -1;
	protected int tokenEnd = -1;
	protected char[] token = null;
	protected int tokenKind;
	protected int tokenLocation;
	
	protected boolean isExtended;
	protected InternalExtendedCompletionContext extendedContext;
	
	protected void setExpectedTypesKeys(char[][] expectedTypesKeys) {
		this.expectedTypesKeys = expectedTypesKeys;
	}
	
	protected void setExpectedTypesSignatures(char[][] expectedTypesSignatures) {
		this.expectedTypesSignatures = expectedTypesSignatures;
	}
	
	protected void setExtended() {
		this.isExtended = true;
	}
	
	protected void setExtendedData(
			ITypeRoot typeRoot,
			CompilationUnitDeclaration compilationUnitDeclaration,
			LookupEnvironment lookupEnvironment,
			Scope scope,
			ASTNode astNode,
			WorkingCopyOwner owner,
			CompletionParser parser) {
		this.isExtended = true;
		this.extendedContext =
			new InternalExtendedCompletionContext(
					this,
					typeRoot,
					compilationUnitDeclaration,
					lookupEnvironment,
					scope,
					astNode,
					owner,
					parser);
	}

	protected void setJavadoc(int javadoc) {
		this.javadoc = javadoc;
	}
	
	protected void setOffset(int offset) {
		this.offset = offset;
	}

	protected void setToken(char[] token) {
		this.token = token;
	}

	protected void setTokenKind(int tokenKind) {
		this.tokenKind = tokenKind;
	}
	
	protected void setTokenLocation(int tokenLocation) {
		this.tokenLocation = tokenLocation;
	}

	protected void setTokenRange(int start, int end) {
		this.setTokenRange(start, end, -1);
	}

	protected void setTokenRange(int start, int end, int endOfEmptyToken) {
		this.tokenStart = start;
		this.tokenEnd = endOfEmptyToken > end ? endOfEmptyToken : end;
		
		// Work around for bug 132558 (https://bugs.eclipse.org/bugs/show_bug.cgi?id=132558).
		// completionLocation can be -1 if the completion occur at the start of a file or
		// the start of a code snippet but this API isn't design to support negative position.
		if(this.tokenEnd == -1) {
			this.tokenEnd = 0;
		}
	}
}
