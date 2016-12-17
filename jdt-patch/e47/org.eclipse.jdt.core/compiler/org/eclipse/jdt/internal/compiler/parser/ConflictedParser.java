/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

public interface ConflictedParser {
	
	/* Return true if at the configuration the parser finds itself in, token would need to be disambiguated.
	   At Java SE 8 time, we have three tokens that need to clarified: the use of '( and that of '<' and finally
	   whether an @ begins a SE8 style type annotation or a SE5 declaration annotation. Where they can co-exist,
	   we treat the type annotation as a declarative annotation.
	*/
	boolean atConflictScenario(int token);
}
