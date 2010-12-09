/*******************************************************************************
 * Copyright (c) 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.model;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.PackageFragment;

/**
 * Don't think I need thisce
 * 
 * @author Andrew Eisenberg
 * @created Oct 22, 2010
 */
public class GroovyClassFile extends ClassFile {

	protected GroovyClassFile(PackageFragment parent, String nameWithoutExtension) {
		super(parent, nameWithoutExtension);
	}

	@Override
	protected IJavaElement[] codeSelect(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu, int offset, int length,
			WorkingCopyOwner o) throws JavaModelException {

		if (CodeSelectHelperFactory.selectHelper != null) {
			// return CodeSelectHelperFactory.selectHelper.select(this, offset, length);
		}
		return new IJavaElement[0];
	}
}
