/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.types;

public interface Modifiers {
	public static final int ACC_PUBLIC = 0x1;
	
	public static final int ACC_PACKAGE = 0x2;

	public static final int ACC_PROTECTED = 0x4;

	public static final int ACC_PRIVATE = 0x8;
	
	public static final int ACC_STATIC = 0x10;
	
	public static final int ACC_FINAL = 0x20;
	
	public static final int ACC_ABSTRACT = 0x40;
}
