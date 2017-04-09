/*******************************************************************************
 * Copyright (c) 2007, 2009 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * Used to convert an IFile into an ICompilationUnit,
 * for clients outside of this package.
 * @since 3.3
 */
public interface ICompilationUnitLocator {
	public ICompilationUnit fromIFile(IFile file);
}
