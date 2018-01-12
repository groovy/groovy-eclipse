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
package org.codehaus.groovy.eclipse.codeassist.relevance.internal;

import org.codehaus.groovy.eclipse.codeassist.relevance.IRelevanceRule;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;

public class PackageLocalityRule implements IRelevanceRule {

    @Override
    public int getRelevance(char[] fullyQualifiedName, IType[] contextTypes, int accessibility, int modifiers) {
        int relevance = 0;

        String qualifiedName = String.valueOf(fullyQualifiedName);
        if (isInSameFile(qualifiedName, contextTypes)) {
            relevance = 10;
        } else {
            IPackageFragment contextFragment = getContextPackageFragment(contextTypes);
            if (contextFragment != null) {
                if (contextFragment.isDefaultPackage()) {
                    if (!CharOperation.contains('.', fullyQualifiedName)) {
                        relevance = 1;
                    }
                } else {
                    String[] testingSegments = qualifiedName.split("\\.");
                    String[] contextSegments = contextFragment.getElementName().split("\\.");

                    for (int i = 0; i < testingSegments.length && i < contextSegments.length; i += 1) {
                        if (testingSegments[i].equals(contextSegments[i])) {
                            relevance += 1;
                        } else {
                            // stop counting once different segment is encountered
                            break;
                        }
                    }

                    // pull back one for the most common top-level domains
                    if (relevance > 0 && contextSegments[0].matches("com|edu|net|org")) {
                        relevance -= 1;
                    }
                }
            }
        }

        return relevance;
    }

    private static boolean isInSameFile(String fullyQualifiedName, IType[] contextTypes) {
        for (IType contextType : contextTypes) {
            // prevent building fully-qualified name string too many times
            if (fullyQualifiedName.endsWith(contextType.getElementName())) {
                if (fullyQualifiedName.equals(contextType.getFullyQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static IPackageFragment getContextPackageFragment(IType[] contextTypes) {
        if (contextTypes == null) {
            return null;
        }
        IPackageFragment frag = null;
        for (IType type : contextTypes) {
            IPackageFragment next = type.getPackageFragment();
            if (frag != null && !frag.equals(next)) {
                return null;
            }
            frag = next;
        }
        return frag;
    }
}
