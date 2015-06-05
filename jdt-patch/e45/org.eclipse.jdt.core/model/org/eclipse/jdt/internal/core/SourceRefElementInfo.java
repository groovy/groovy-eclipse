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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.SourceRange;

/**
 * Element info for ISourceReference elements.
 */
/* package */ class SourceRefElementInfo extends JavaElementInfo {
	protected int sourceRangeStart, sourceRangeEnd;
/**
 * @see org.eclipse.jdt.internal.compiler.env.ISourceType#getDeclarationSourceEnd()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceMethod#getDeclarationSourceEnd()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceField#getDeclarationSourceEnd()
 */
public int getDeclarationSourceEnd() {
	return this.sourceRangeEnd;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ISourceType#getDeclarationSourceStart()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceMethod#getDeclarationSourceStart()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceField#getDeclarationSourceStart()
 */
public int getDeclarationSourceStart() {
	return this.sourceRangeStart;
}
protected ISourceRange getSourceRange() {
	return new SourceRange(this.sourceRangeStart, this.sourceRangeEnd - this.sourceRangeStart + 1);
}
protected void setSourceRangeEnd(int end) {
	this.sourceRangeEnd = end;
}
protected void setSourceRangeStart(int start) {
	this.sourceRangeStart = start;
}
}
