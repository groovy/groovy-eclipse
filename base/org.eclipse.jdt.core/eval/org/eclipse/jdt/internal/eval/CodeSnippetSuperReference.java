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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A super reference inside a code snippet denotes a reference to the super type of
 * the remote receiver object (that is, the receiver of the context in the stack frame). This is
 * used to report an error through JavaModelException according to the fact that super
 * reference are not supported in code snippet.
 */
public class CodeSnippetSuperReference extends SuperReference implements EvaluationConstants, InvocationSite {

public CodeSnippetSuperReference(int pos, int sourceEnd) {
	super(pos, sourceEnd);
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return null;
}

public TypeBinding resolveType(BlockScope scope) {
	scope.problemReporter().cannotUseSuperInCodeSnippet(this.sourceStart, this.sourceEnd);
	return null;
}
public boolean isSuperAccess(){
	return false;
}
public boolean isTypeAccess(){
	return false;
}
public void setActualReceiverType(ReferenceBinding receiverType) {
	// ignored
}
public void setDepth(int depth){
	// ignored
}
public void setFieldIndex(int index){
	// ignored
}
}

