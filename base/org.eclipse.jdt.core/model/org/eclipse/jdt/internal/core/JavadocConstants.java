/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

public interface JavadocConstants {

	String ANCHOR_PREFIX_END = "\""; //$NON-NLS-1$
	String ANCHOR_PREFIX_START = "<A NAME=\""; //$NON-NLS-1$
	int ANCHOR_PREFIX_START_LENGHT = ANCHOR_PREFIX_START.length();
	String ANCHOR_SUFFIX = "</A>"; //$NON-NLS-1$
	int ANCHOR_SUFFIX_LENGTH = JavadocConstants.ANCHOR_SUFFIX.length();
	String CONSTRUCTOR_DETAIL = "<!-- ========= CONSTRUCTOR DETAIL ======== -->"; //$NON-NLS-1$
	String CONSTRUCTOR_SUMMARY = "<!-- ======== CONSTRUCTOR SUMMARY ======== -->"; //$NON-NLS-1$
	String FIELD_DETAIL= "<!-- ============ FIELD DETAIL =========== -->"; //$NON-NLS-1$
	String FIELD_SUMMARY = "<!-- =========== FIELD SUMMARY =========== -->"; //$NON-NLS-1$
	String ENUM_CONSTANT_SUMMARY = "<!-- =========== ENUM CONSTANT SUMMARY =========== -->"; //$NON-NLS-1$
	String ANNOTATION_TYPE_REQUIRED_MEMBER_SUMMARY = "<!-- =========== ANNOTATION TYPE REQUIRED MEMBER SUMMARY =========== -->"; //$NON-NLS-1$
	String ANNOTATION_TYPE_OPTIONAL_MEMBER_SUMMARY = "<!-- =========== ANNOTATION TYPE OPTIONAL MEMBER SUMMARY =========== -->"; //$NON-NLS-1$
	String END_OF_CLASS_DATA = "<!-- ========= END OF CLASS DATA ========= -->"; //$NON-NLS-1$
	String HTML_EXTENSION = ".html"; //$NON-NLS-1$
	String INDEX_FILE_NAME = "index.html"; //$NON-NLS-1$
	String METHOD_DETAIL = "<!-- ============ METHOD DETAIL ========== -->"; //$NON-NLS-1$
	String METHOD_SUMMARY = "<!-- ========== METHOD SUMMARY =========== -->"; //$NON-NLS-1$
	String NESTED_CLASS_SUMMARY = "<!-- ======== NESTED CLASS SUMMARY ======== -->"; //$NON-NLS-1$
	String PACKAGE_FILE_NAME = "package-summary.html"; //$NON-NLS-1$
	String SEPARATOR_START = "<!-- ="; //$NON-NLS-1$
	String START_OF_CLASS_DATA = "<!-- ======== START OF CLASS DATA ======== -->"; //$NON-NLS-1$
	int START_OF_CLASS_DATA_LENGTH = JavadocConstants.START_OF_CLASS_DATA.length();
}
