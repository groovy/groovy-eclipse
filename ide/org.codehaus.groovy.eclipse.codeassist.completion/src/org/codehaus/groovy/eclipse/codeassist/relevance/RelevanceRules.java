/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.codeassist.relevance;

import java.util.Arrays;

import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.core.SourceType;

/**
 * Computes the relevance of a type based on a set of relevance rules. The
 * caller has the option of specifying a subset of available rules to use, or if
 * none is provided, by default all the rules will be used. <br>
 * New rules can be added and require a corresponding type definition </br>
 *
 *
 * @author Nieraj Singh
 * @created 2011-02-17
 */
public class RelevanceRules implements IRelevanceRule {

	public static int DEFAULT_STARTING_RELEVANCE_VALUE = 1;

	public static enum RelevanceRuleType {
		SOURCE_TYPE, ACCESSIBILITY, MODIFIERS, LIBRARY_TYPE, SIMILAR_PACKAGE
	}

	private RelevanceRuleType[] ruleTypes;

	public static RelevanceRules ALL_RULES = new RelevanceRules(
			RelevanceRuleType.LIBRARY_TYPE, RelevanceRuleType.SOURCE_TYPE,
			// FIXNS: Enable only after it has been tested
			// RelevanceRuleType.ACCESSIBILITY,
			RelevanceRuleType.MODIFIERS, RelevanceRuleType.SIMILAR_PACKAGE);

	public static IRelevanceRule getRule(RelevanceRuleType type) {

		if (type == null) {
			return null;
		}

		IRelevanceRule rule = null;
		switch (type) {
		case SOURCE_TYPE:
			rule = new SourceRule();
			break;
		// FIXNS: Enable only after it has been tested
		// case ACCESSIBILITY:
		// rule = new AccessibilityRule();
		// break;
		case MODIFIERS:
			rule = new ModifierRule();
			break;
		case LIBRARY_TYPE:
			rule = new LibraryTypeRule();
			break;
		case SIMILAR_PACKAGE:
			rule = new SimilarPackagesRule();
			break;
		}
		return rule;
	}

	/**
	 * Argument is a list of relevance rule types that should be used when
	 * computing the relevance of a type. If null or empty is passed, all the
	 * available rule types will be used. This constructor allows a caller to
	 * use only a subset of rules.
	 */
	public RelevanceRules(RelevanceRuleType... ruleTypes) {
		this.ruleTypes = ruleTypes;
	}

	/**
	 * Computes the integer relevance value of a given type based on registered
	 * relevance rule types
	 */
	public int getRelevance(char[] fullyQualifiedName, IType[] contextTypes,
			int accessibility, int modifiers) {
		if (fullyQualifiedName == null) {
			return 0;
		}

		// use all the rule types if none were specified by the invoker.
		RelevanceRuleType[] rTypes = ruleTypes == null || ruleTypes.length == 0 ? RelevanceRuleType
				.values() : ruleTypes;

		int relevance = getStartingRelevanceValue();
		for (RelevanceRuleType ruleType : rTypes) {
			IRelevanceRule rule = getRule(ruleType);
			if (rule != null) {
				relevance += rule.getRelevance(fullyQualifiedName,
						contextTypes, accessibility, modifiers);
			}
		}
		// Use lowest Relevance category as Types have lowest relevance
		// category
		return Relevance.LOWEST.getRelevance(relevance);
	}

	/**
	 * Computes the integer relevance value of a given type based on registered
	 * relevance rule types
	 */
	public int getRelevance(IType type, IType[] contextTypes) {
		if (type == null) {
			return 0;
		}

		// use all the rule types if none were specified by the invoker.
		RelevanceRuleType[] rTypes = ruleTypes == null || ruleTypes.length == 0 ? RelevanceRuleType
				.values() : ruleTypes;

		int relevance = getStartingRelevanceValue();
		for (RelevanceRuleType ruleType : rTypes) {
			IRelevanceRule rule = getRule(ruleType);
			if (rule != null) {
				relevance += rule.getRelevance(type, contextTypes);
			}
		}
		// User lowest Relevance category as Types have lowest relevance
		// category
		return Relevance.LOWEST.getRelevance(relevance);
	}

	protected int getStartingRelevanceValue() {
		return DEFAULT_STARTING_RELEVANCE_VALUE;
	}

	/*
	 * RULE DEFINITIONS
	 */

	/**
	 * Simple rule that places higher priority on Source types. Binary types
	 * have lower priority.
	 */
	public static class SourceRule extends AbstractRule {

		public int getRelevance(IType relevanceType, IType[] contextTypes) {
			// Source have higher relevance than Binary
			return relevanceType instanceof SourceType ? TypeRelevanceCategory.MEDIUM_TYPE
					.applyCategory(1) : TypeRelevanceCategory.LOW_TYPE
					.applyCategory(1);
		}

		public int getRelevance(char[] fullyQualifiedName,
				IType[] contextTypes, int accessibility, int modifiers) {
			// don't know
			return 0;
		}

	}

	/**
	 * FIXNS: Not fully tested. Not recommended for use.
	 *
	 * Accessible types have higher priority than restricted types.
	 *
	 */
	public static class AccessibilityRule extends AbstractRule {

		public int getRelevance(IType relevanceType, IType[] contextTypes) {
			if (relevanceType == null) {
				return 0;
			}

			// determine associated access restriction
			AccessRestriction accessRestriction = ProposalUtils
					.getTypeAccessibility(relevanceType);

			// If no access restriction found, assume accessible?
			int accessibility = IAccessRule.K_ACCESSIBLE;

			if (accessRestriction != null) {
				switch (accessRestriction.getProblemId()) {
				case IProblem.ForbiddenReference:
					accessibility = IAccessRule.K_NON_ACCESSIBLE;
					break;
				case IProblem.DiscouragedReference:
					// discouraged references have a lower priority
					accessibility = IAccessRule.K_DISCOURAGED;
					break;
				}
			}
			return getRelevance(null, null, accessibility, 0);
		}

		public int getRelevance(char[] fullyQualifiedName,
				IType[] contextTypes, int accessibility, int modifiers) {
			return accessibility == IAccessRule.K_ACCESSIBLE ? TypeRelevanceCategory.MEDIUM_TYPE
					.applyCategory(1) : 0;
		}
	}

	/**
	 * Types in the same project as the context types (the context compilation
	 * unit where a type is being imported or referenced) have higher priority
	 * that types in other projects. Furthermore, private types have the highest
	 * priority, followed by package private, followed by public
	 */
	public static class ModifierRule extends AbstractRule {

		protected TypeRelevanceCategory getTypeCategory(IType relevanceType,
				IType[] contextTypes) {

			TypeRelevanceCategory category = null;
			if (areTypesInSameCompilationUnit(relevanceType, contextTypes)) {
				category = TypeRelevanceCategory.HIGH_TYPE;
			} else if (areTypesInSamePackage(relevanceType, contextTypes)) {
				category = TypeRelevanceCategory.MEDIUM_HIGH_TYPE;
			} else {
				// ignore this rule if not in same package or unit
				category = null;
			}
			return category;

		}

		public int getRelevance(IType relevanceType, IType[] contextTypes) {

			int relevance = 0;
			TypeRelevanceCategory category = null;
			try {
				int modifiers = relevanceType.getFlags();
				category = getTypeCategory(relevanceType, contextTypes);
				if (category != null) {
					relevance += (modifiers & Flags.AccDefault) != 0 ? 0 : 1;
					relevance += (modifiers & Flags.AccPrivate) != 0 ? 0 : 1;
					return category.applyCategory(relevance);
				}
			} catch (JavaModelException e) {
				GroovyCore.logException("Exception calculating relevance", e);
			}
			return 0;
		}

		// don't do for relevance calculation involving content assist
		public int getRelevance(char[] fullyQualifiedName,
				IType[] contextTypes, int accessibility, int modifiers) {
			return 0;
		}

	}

	/**
	 *
	 * Types from certain libraries have higher priority. In particular, types
	 * from java, groovy, groovyx, and javax have higher priority than types
	 * from other packages. The order is shown in the following example <br>
	 * Example: </br> <li>
	 * java.lang.SomeType</li> <li>
	 * groovy.lang.SomeType</li> <li>
	 * groovyx.lang.SomeType</li> <li>
	 * javax.lang.SomeType</li> <li>
	 * com.lang.SomeType</li>
	 *
	 */
	public static class LibraryTypeRule extends AbstractRule {

		enum LibraryType {
			JAVA("java"), JAVAX("javax"), GROOVY("groovy"), GROOVYX("groovyx");

			private LibraryType(String value) {
				this.value = value.toCharArray();
			}

			private char[] value;

			public char[] getValue() {
				return value;
			}

		}

		/**
		 * The library type is the first segment in the package name.
		 *
		 * @param relevanceType
		 * @return first segment in the package name containing the type
		 */
		protected LibraryType getLibraryType(char[] qualifiedName) {
			char[][] segments = CharOperation.splitOn('.', qualifiedName);
			if (segments != null && segments.length > 0) {
				char[] firstPackSegment = segments[0];
				for (LibraryType type : LibraryType.values()) {
					if (Arrays.equals(type.getValue(), firstPackSegment)) {
						return type;
					}
				}
			}
			return null;

		}

		public int getRelevance(IType relevanceType, IType[] contextTypes) {
			if (relevanceType == null) {
				return 0;
			}
			return getRelevance(relevanceType.getFullyQualifiedName()
					.toCharArray(), contextTypes, 0, 0);
		}

		public int getRelevance(char[] fullyQualifiedName,
				IType[] contextTypes, int accessibility, int modifiers) {
			// Default is zero, meaning relevance for types in any other library
			// is governed by other rules. Only types in the following libraries
			// get higher priority
			int relevance = 0;
			LibraryType packType = getLibraryType(fullyQualifiedName);
			if (packType != null) {
				switch (packType) {
				case JAVA:
					relevance += 4;
					break;
				case GROOVY:
					relevance += 3;
					break;
				case GROOVYX:
					relevance += 2;
					break;
				case JAVAX:
					relevance += 1;
					break;
				}
			}

			return TypeRelevanceCategory.LOW_TYPE.applyCategory(relevance);
		}

	}

	/**
	 *
	 * Types in packages with common segments have higher priority. As this rule
	 * may clash with LibraryTypeRule, a higher relevance category is assigned
	 * to this rule. This example shows the effect of the higher category. <br>
	 * With just LibraryTypeRule, the following order would be expected:</br>
	 * <li>
	 * java.lang.SomeType</li> <li>
	 * groovy.lang.SomeType</li> <li>
	 * groovyx.lang.SomeType</li> <li>
	 * javax.lang.SomeType</li> <li>
	 * com.lang.SomeType</li>
	 * <p>
	 * However, if both LibraryTypeRule and SimilarPackagesRule are enabled, and
	 * if SomeType is being imported into a compilation unit in
	 * com.lang.AnotherType, the order would be:
	 * </p>
	 * <li>
	 * com.lang.SomeType</li> <li>
	 * java.lang.SomeType</li> <li>
	 * groovy.lang.SomeType</li> <li>
	 * groovyx.lang.SomeType</li> <li>
	 * javax.lang.SomeType</li>
	 *
	 */
	public static class SimilarPackagesRule extends AbstractRule {

		protected String convertToDot(String name) {
			return name != null ? name.replace('$', '.') : name;
		}

		public int getRelevance(IType relevanceType, IType[] contextTypes) {
			return getRelevance(relevanceType.getFullyQualifiedName('.')
					.toCharArray(), contextTypes, 0, 0);
		}

		public int getRelevance(char[] fullyQualifiedName,
				IType[] contextTypes, int accessibility, int modifiers) {
			int relevance = 0;
			IPackageFragment contextFragment = getContextPackageFragment(contextTypes);
			if (contextFragment != null && fullyQualifiedName != null) {
				String relQualified = String.valueOf(fullyQualifiedName);
				String contextQualified = convertToDot(contextFragment
						.getElementName());

				String[] relSegments = relQualified.split("\\.");
				String[] contextSegments = contextQualified.split("\\.");

				for (int i = 0; i < relSegments.length
						&& i < contextSegments.length; i++) {
					if (relSegments[i].equals(contextSegments[i])) {
						relevance++;
					} else {
						// Stop relevance counting once different segments are
						// encountered
						break;
					}
				}

			}

			return TypeRelevanceCategory.HIGH_TYPE.applyCategory(relevance);
		}

	}
}
