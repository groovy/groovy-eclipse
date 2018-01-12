/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.eclipse.codeassist.relevance.internal.AccessibilityRule;
import org.codehaus.groovy.eclipse.codeassist.relevance.internal.CompositeRule;
import org.codehaus.groovy.eclipse.codeassist.relevance.internal.PackageLocalityRule;
import org.codehaus.groovy.eclipse.codeassist.relevance.internal.PackagePriorityRule;
import org.eclipse.jdt.core.IType;

/**
 * Computes type relevance. Useful for content assist or import selection.
 */
@FunctionalInterface
public interface IRelevanceRule {

    IRelevanceRule DEFAULT = CompositeRule.of(
        5.0, new AccessibilityRule(), // base value for accessible types
        1.2, new PackageLocalityRule(), // boost types in nearby package
        1.0, new PackagePriorityRule("groovy", "java", "spock", "javax")
    );

    /**
     * @param type Non-null type reference whose relevance must be computed.
     * @param contextTypes Types found nearby to relevance calculation location.
     *     For example the top level type where the relevance type needs to be
     *     resolved or all be types in the same compilation unit.
     *
     * @return Positive value, with a higher value indicating higher relevance;
     *     or zero if relevance cannot be computed.
     */
    default int getRelevance(IType type, IType[] contextTypes) {
        if (type == null) {
            return 0;
        }
        return getRelevance(type.getFullyQualifiedName().toCharArray(), contextTypes, 0, 0);
    }

    /**
     * @param fullyQualifiedName Non-null type name whose relevance must be computed.
     * @param contextTypes Types found nearby to relevance calculation location.
     *     For example the top level type where the relevance type needs to be
     *     resolved or all be types in the same compilation unit.
     *
     * @return Positive value, with a higher value indicating higher relevance;
     *     or zero if relevance cannot be computed.
     */
    int getRelevance(char[] fullyQualifiedName, IType[] contextTypes, int accessibility, int modifiers);
}
