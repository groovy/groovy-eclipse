/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.codeassist.relevance.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.codeassist.relevance.IRelevanceRule;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;

public class PackagePriorityRule implements IRelevanceRule {

    private final Map<String, Integer> priorities = new HashMap<>();

    public PackagePriorityRule(String... packages) {
        Assert.isLegal(packages != null && packages.length > 0);
        for (int i = 0, p = packages.length; p > 0; i += 1, p -= 1) {
            priorities.put(packages[i], p);
        }
    }

    @Override
    public int getRelevance(char[] fullyQualifiedName, IType[] contextTypes, int accessibility, int modifiers) {
        int relevance = 0;

        if (CharOperation.contains('.', fullyQualifiedName)) {
            String basePackage = String.valueOf(CharOperation.splitOn('.', fullyQualifiedName)[0]);
            Integer p = priorities.get(basePackage);
            if (p != null) relevance = p.intValue();
        }

        return relevance;
    }
}
