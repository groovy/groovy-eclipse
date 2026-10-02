/*******************************************************************************
 * Copyright (c) 2024 Lorenzo Bettini and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lorenzo Bettini - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;

/**
 * Only to keep track of internal API used by Xtext.
 */
public class XtextDependencies {

	public ClassFileReader ClassFileReader_read_InputStream() throws ClassFormatException, IOException {
		return ClassFileReader.read((InputStream) null, (String) null);
	}
}
