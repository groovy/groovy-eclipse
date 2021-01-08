/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.Messages;

/**
 * An internal enumeration of all Java language features that were introduced as
 * standard feature or preview feature from Java 15. The idea is to have one
 * location where the applicability of a feature, such as version supported in,
 * whether or not a preview, what are the restricted keywords introduced by a
 * feature etc. This is expected to be updated every time there's a new Java
 * version and the change is expected to be one of the following kinds:
 * <ul>
 * <li>The preview feature continues to be a preview in the next version</li>
 * <li>The preview feature is upgraded to a standard feature</li>
 * <li>The preview feature is removed</li>
 * </ul>
 *
 * @author jay
 */
public enum JavaFeature {

	TEXT_BLOCKS(ClassFileConstants.JDK15,
			Messages.bind(Messages.text_block),
			new char[][] {},
			false),

	PATTERN_MATCHING_IN_INSTANCEOF(ClassFileConstants.JDK15,
			Messages.bind(Messages.pattern_matching_instanceof),
			new char[][] {},
			true),

	RECORDS(ClassFileConstants.JDK15,
			Messages.bind(Messages.records),
			new char[][] {TypeConstants.RECORD_RESTRICTED_IDENTIFIER},
			true),

	SEALED_CLASSES(ClassFileConstants.JDK15,
			Messages.bind(Messages.sealed_types),
			new char[][] {TypeConstants.SEALED, TypeConstants.PERMITS},
			true),
    ;

	final long compliance;
	final String name;
	final boolean isPreview;
	char[][] restrictedKeywords;

	public boolean isPreview() {
		return this.isPreview;
	}
	public String getName() {
		return this.name;
	}
	public long getCompliance() {
		return this.compliance;
	}
	public char[][] getRestrictedKeywords() {
		return this.restrictedKeywords;
	}
	public boolean isSupported(CompilerOptions options) {
		if (this.isPreview)
			return options.enablePreviewFeatures;
		return this.getCompliance() <= options.sourceLevel;
	}
	public boolean isSupported(long comp, boolean preview) {
		if (this.isPreview)
			return preview;
		return this.getCompliance() <= comp;
	}

	JavaFeature(long compliance, String name, char[][] restrictedKeywords, boolean isPreview) {
        this.compliance = compliance;
        this.name = name;
        this.isPreview = isPreview;
        this.restrictedKeywords = restrictedKeywords;
	}
}