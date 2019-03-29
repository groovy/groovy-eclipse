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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.codeassist.relevance.IRelevanceRule;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IType;

public class CompositeRule implements IRelevanceRule {

    private final Map<IRelevanceRule, Double> rules;

    public CompositeRule(Map<IRelevanceRule, Double> rules) {
        Assert.isLegal(rules != null && !rules.isEmpty());
        this.rules = rules;
    }

    @Override
    public int getRelevance(IType type, IType[] contextTypes) {
        double relevance = 0;

        for (Map.Entry<IRelevanceRule, Double> entry : rules.entrySet()) {
            relevance += (entry.getValue() * entry.getKey().getRelevance(type, contextTypes));
        }

        return Math.max(1, (int) Math.ceil(relevance));
    }

    @Override
    public int getRelevance(char[] fullyQualifiedName, IType[] contextTypes, int accessibility, int modifiers) {
        double relevance = 0;

        for (Map.Entry<IRelevanceRule, Double> entry : rules.entrySet()) {
            relevance += (entry.getValue() * entry.getKey().getRelevance(fullyQualifiedName, contextTypes, accessibility, modifiers));
        }

        return Math.max(1, (int) Math.ceil(relevance));
    }

    //--------------------------------------------------------------------------

    public static IRelevanceRule of(double weight, IRelevanceRule rule) {
        return new CompositeRule(Collections.singletonMap(rule, weight));
    }

    public static IRelevanceRule of(double weight1, IRelevanceRule rule1, double weight2, IRelevanceRule rule2) {
        Map<IRelevanceRule, Double> rules = new IdentityHashMap<>();
        rules.put(rule1, weight1);
        rules.put(rule2, weight2);

        return new CompositeRule(Collections.unmodifiableMap(rules));
    }

    public static IRelevanceRule of(double weight1, IRelevanceRule rule1, double weight2, IRelevanceRule rule2, double weight3, IRelevanceRule rule3) {
        Map<IRelevanceRule, Double> rules = new IdentityHashMap<>();
        rules.put(rule1, weight1);
        rules.put(rule2, weight2);
        rules.put(rule3, weight3);

        return new CompositeRule(Collections.unmodifiableMap(rules));
    }
}
