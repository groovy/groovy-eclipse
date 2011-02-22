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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

/**
 *
 * Abstract class containing helper methods to obtain information like enclosing
 * package or project for the context types, as well as uses an optional
 * relevance category to further prioritise rules. A relevance category is
 * applied to the relevance value of this rule. Categories may be useful to
 * avoid rule clashes, in case one rule has a higher priority than another one.
 *
 * @author Nieraj Singh
 * @created 2011-02-18
 */
public abstract class AbstractRule implements IRelevanceRule {

	/**
	 * Additional granularity for Types only. Allows rules to have different
	 * priorities, as some rules may carry more weight than others.
	 *
	 */
	public enum TypeRelevanceCategory {

		LOWEST_TYPE(1), LOW_TYPE(4), MEDIUM_TYPE(8), MEDIUM_HIGH_TYPE(12), HIGH_TYPE(
				16);

		private double multiplier;

		private TypeRelevanceCategory(double multiplier) {
			this.multiplier = multiplier;
		}

		public double getMultiplier() {
			return multiplier;
		}

		public int applyCategory(int value) {
			return (int) getMultiplier() * value;
		}

	}

	/**
	 * Returns true if an only if the relevance type is contained in the same
	 * project as ALL other context types. If the context types includes types
	 * from different folders, or the relevance type is in a different project
	 * than the context types, false is returned.
	 *
	 * @param relevanceType
	 *            type whose relevance needs to be determined
	 * @param contextTypes
	 *            context types where this rule is being invoked, like the
	 *            compilation unit where the relevance type is being imported
	 * @return true if and only if the relevance type is contained in the same
	 *         project as ALL other context types
	 */
    public boolean areTypesInSameProject(IType relevanceType,
			IType[] contextTypes) {
		if (relevanceType == null || contextTypes == null
				|| contextTypes.length == 0) {
			return false;
		}

		IJavaProject relevanceProject = relevanceType.getJavaProject();

		if (relevanceProject == null) {
			return false;
		}
		for (IType cType : contextTypes) {

			if (!relevanceProject.equals(cType.getJavaProject())) {
				return false;
			}
		}
		return true;

	}

    /**
     * Returns true if an only if the relevance type is contained in the same package as ALL other
     * context types. If the context types includes types from different folders, or the relevance
     * type is in a different package than the context types, false is returned.
     * 
     * @param relevanceType type whose relevance needs to be determined
     * @param contextTypes context types where this rule is being invoked, like the compilation unit
     *            where the relevance type is being imported
     * @return true if and only if the relevance type is contained in the same package as ALL other
     *         context types
     */
    public boolean areTypesInSamePackage(IType relevanceType,
            IType[] contextTypes) {
        if (relevanceType == null || contextTypes == null
                || contextTypes.length == 0) {
            return false;
        }

        IPackageFragment relevancePackage = relevanceType.getPackageFragment();

        if (relevancePackage == null) {
            return false;
        }
        for (IType cType : contextTypes) {

            if (!relevancePackage.equals(cType.getPackageFragment())) {
                return false;
            }
        }
        return true;

    }

    /**
     * Return true if and only if the relevance type and ALL the context types are in the same
     * compilation unit. If at least one of the context types is in a different compilation unit, or
     * the compilation unit of either cannot be resolved, false is returned.
     * 
     * @param relevanceType
     * @param contextTypes
     * @return true if and only if the relevance type and all context types are in the same
     *         compilation unit. False otherwise
     */
	public boolean areTypesInSameCompilationUnit(IType relevanceType,
			IType[] contextTypes) {

		if (relevanceType == null || contextTypes == null
				|| contextTypes.length == 0) {
			return false;
		}

		ICompilationUnit relevanceCompilationUnit = relevanceType
				.getCompilationUnit();
		if (relevanceCompilationUnit != null) {
			for (IType contextType : contextTypes) {
				ICompilationUnit contextCu = contextType.getCompilationUnit();
				if (!relevanceCompilationUnit.equals(contextCu)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * This returns the package fragment containing ALL the context types. If
	 * the list of context types includes types from different packages, null is
	 * returned.
	 *
	 * @param contextTypes
	 *            . Should all be part of the same compilation unit. therefore
	 *            contained in the same package
	 * @return package fragment containing ALL the context types, or null if a
	 *         single package fragment cannot be resolved from the context types
	 */
	public IPackageFragment getContextPackageFragment(IType[] contextTypes) {
		if (contextTypes == null) {
			return null;
		}
		IPackageFragment frag = null;
		for (IType type : contextTypes) {
			IPackageFragment fragToCheck = type.getPackageFragment();
			if (frag != null && !frag.equals(fragToCheck)) {
				return null;
			}
			frag = fragToCheck;

		}
		return frag;
	}

	public IType getFirstContextType(IType[] contextTypes) {
		return contextTypes != null && contextTypes.length > 0 ? contextTypes[0]
				: null;
	}

}