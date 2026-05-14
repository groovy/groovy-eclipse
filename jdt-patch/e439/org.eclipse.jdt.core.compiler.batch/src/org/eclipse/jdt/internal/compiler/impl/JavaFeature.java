/*******************************************************************************
 * Copyright (c) 2020, 2025 IBM Corporation and others.
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

import org.eclipse.jdt.core.compiler.CharOperation;
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

	SWITCH_EXPRESSIONS(ClassFileConstants.JDK14,
			Messages.bind(Messages.switch_expression),
			new char[][] {TypeConstants.YIELD},
			false),

	TEXT_BLOCKS(ClassFileConstants.JDK15,
			Messages.bind(Messages.text_block),
			new char[][] {},
			false),

	PATTERN_MATCHING_IN_INSTANCEOF(ClassFileConstants.JDK16,
			Messages.bind(Messages.pattern_matching_instanceof),
			new char[][] {},
			false),

	RECORDS(ClassFileConstants.JDK16,
			Messages.bind(Messages.records),
			new char[][] {TypeConstants.RECORD_RESTRICTED_IDENTIFIER},
			false),

	SEALED_CLASSES(ClassFileConstants.JDK17,
			Messages.bind(Messages.sealed_types),
			new char[][] {TypeConstants.SEALED, TypeConstants.NON_SEALED, TypeConstants.PERMITS},
			false),
	PATTERN_MATCHING_IN_SWITCH(ClassFileConstants.JDK21,
			Messages.bind(Messages.pattern_matching_switch),
			new char[][] {},
			false),
	RECORD_PATTERNS(ClassFileConstants.JDK21,
			Messages.bind(Messages.record_patterns),
			new char[][] {},
			false),
	UNNAMMED_PATTERNS_AND_VARS(ClassFileConstants.JDK22,
			Messages.bind(Messages.unnamed_patterns_and_vars),
			new char[][] {},
			false),
	COMPACT_SOURCE_AND_INSTANCE_MAIN_METHODS(ClassFileConstants.JDK25,
			Messages.bind(Messages.compact_source_and_instance_main_methods),
			new char[][] {},
			false),
	/**
	 * JEP 513.
	 * As this feature graduated to a standard feature, we optimistically perform analysis using the
	 * rules of this JEP even below 25. This is done so we can report when certain code will become
	 * legal when adopting version 25.
	 * <dl>
	 * <dt>Initial check in ConstructorDeclaration.resolveStatements();
	 * <dd>we always call enterEarlyConstructionContext() to enable many downstream analyses.<br>
	 * Check actual support only when the feature is actually used.<br>
	 * Similar for analyseCode() and generateCode().
	 * <dt>Differentiate error messages based on enablement:
	 * <dd><ul>
	 * 	<li>AllocationExpression.checkEarlyConstructionContext()
	 * 	<li>ExplicitConstructorCall.resolve(BlockScope)
	 * 	<li>ThisReference.checkAccess(BlockScope, ReferenceBinding)
	 * 	</ul>
	 * <dt>Main checks during resolve: Reference.checkFieldAccessInEarlyConstructionContext()
	 * <dd>applies all strategy variants from above
	 * <dt>Individual exceptions from old rules
	 * <dd><ul><li>MethodScope.findField()<li>Scope.getBinding(char[], int, InvocationSite, boolean)</ul>
	 * <dt>Main code gen addition in TypeDeclaration.addSyntheticArgumentsBeyondEarlyConstructionContext()
	 * <dd>Only if feature is actually supported, we will generate special synthetic args and fields<br>
	 * Uses some feature-specific help from BlockScope.getEmulationPath()
	 * </dl>
	 */
	FLEXIBLE_CONSTRUCTOR_BODIES(ClassFileConstants.JDK25,
			Messages.bind(Messages.flexible_constructor_bodies),
			new char[][] {},
			false),
	PRIMITIVES_IN_PATTERNS(ClassFileConstants.JDK25,
			Messages.bind(Messages.primitives_in_patterns),
			new char[][] {},
			true),
	MODULE_IMPORTS(ClassFileConstants.JDK25,
			Messages.bind(Messages.module_imports),
			CharOperation.NO_CHAR_CHAR,
			false),
	MARKDOWN_COMMENTS(ClassFileConstants.JDK23,
			Messages.bind(Messages.markdown_comments),
			CharOperation.NO_CHAR_CHAR,
			false),
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
	public boolean isSupported(String comp, boolean preview) {
		if (this.isPreview)
			return preview;
		return this.getCompliance() <= CompilerOptions.versionToJdkLevel(comp);
	}
	public boolean matchesCompliance(CompilerOptions options) {
		return this.compliance == options.complianceLevel;
	}
	JavaFeature(long compliance, String name, char[][] restrictedKeywords, boolean isPreview) {
        this.compliance = compliance;
        this.name = name;
        this.isPreview = isPreview;
        this.restrictedKeywords = restrictedKeywords;
	}
}