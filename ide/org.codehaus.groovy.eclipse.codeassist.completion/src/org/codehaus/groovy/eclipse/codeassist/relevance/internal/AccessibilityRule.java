/*
 * Copyright 2009-2017 the original author or authors.
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

import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.relevance.IRelevanceRule;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;

public class AccessibilityRule implements IRelevanceRule {

    @Override
    public int getRelevance(IType type, IType[] contextTypes) {
        int accessibility = IAccessRule.K_ACCESSIBLE;

        AccessRestriction accessRestriction = ProposalUtils.getTypeAccessibility(type);
        if (accessRestriction != null) {
            switch (accessRestriction.getProblemId()) {
            case IProblem.DiscouragedReference:
                accessibility = IAccessRule.K_DISCOURAGED;
                break;
            case IProblem.ForbiddenReference:
                accessibility = IAccessRule.K_NON_ACCESSIBLE;
                break;
            }
        }

        return getRelevance(null, null, accessibility, 0);
    }

    @Override
    public int getRelevance(char[] fullyQualifiedName, IType[] contextTypes, int accessibility, int modifiers) {
        switch (accessibility & ~IAccessRule.IGNORE_IF_BETTER) {
        case IAccessRule.K_ACCESSIBLE:
            return +1;
        case IAccessRule.K_DISCOURAGED:
            break;
        case IAccessRule.K_NON_ACCESSIBLE:
            return -1;
        }
        return 0;
    }
}
