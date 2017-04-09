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
package org.eclipse.jdt.internal.core.builder;

/**
 * Exception thrown when the build should be aborted because a source file is missing/empty.
 */
public class MissingSourceFileException extends RuntimeException {

	protected String missingSourceFile;
	private static final long serialVersionUID = -1416609004971115719L; // backward compatible

public MissingSourceFileException(String missingSourceFile) {
	this.missingSourceFile = missingSourceFile;
}
}
