/*
 * Copyright 2003-2010 the original author or authors.
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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.proposals.AbstractGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyCategoryMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyPropertyProposal;
import org.eclipse.jdt.groovy.search.VariableScope;


/**
 * This class defines relative relevance classes of proposals.
 *
 * There can be gradations of relevance within these relevance classes. Each
 * relevance class is a factor of 10 greater than the previous class So, there
 * are 10 gradations within each class for more fine grained control.
 *
 * Note that the reason we are using factors of 10 is that JDT will make small
 * changes to the relevancy deep inside the Completion computer. By making the
 * differences between the gradations so large, we ensure that these small
 * changes have no effect on the final outcome.
 *
 * @author andrew
 * @created Aug 10, 2010
 */
public enum Relevance {
    /**
     * Types
     */
    LOWEST(1),
    /**
     * DGM, DGSM, Object, GroovyObject and other fields/methods available in all
     * types
     */
    VERY_LOW(10),
    /**
     * Static fields
     */
    LOW(100),
    /**
     * Methods
     */
    MEDIUM(1000),
    /**
     * Fields and properties
     */
    MEDIUM_HIGH(10000),
    /**
     * Local variables, parameters
     */
    HIGH(100000),
    /**
     * New method or field proposals, most recently used, favorites, or other
     * special cases
     */
    VERY_HIGH(1000000);

    private Relevance(int value) {
        this.value = value;

    }

    private int value;

    /**
     * There are 10 gradations in each relevance class
     *
     * @param multiplier
     *            how many times the actual value multiplier is a float so that
     *            it is possible to reduce the relative relavance by passing in
     *            a value < 1
     * @return the actual relavance of the associated proposal
     */
    public int getRelevance(float multiplier) {
        return Math.max(1, (int) ((float) value * multiplier));
    }

    public int getRelavance() {
        return value;
    }

    /**
     * Calculates the relevance of an AST node based on a number of heuristics
     *
     * @param groovyProposal
     * @return
     */
    public static int calculateRelevance(AbstractGroovyProposal groovyProposal,
            float multiplier) {
        return findRelevanceClass(groovyProposal).getRelevance(multiplier);
    }

    public static Relevance findRelevanceClass(
            AbstractGroovyProposal groovyProposal) {
        // dispatch on the kind of groovyProposal

        if (groovyProposal instanceof GroovyFieldProposal || groovyProposal instanceof GroovyPropertyProposal) {
            AnnotatedNode node = groovyProposal.getAssociatedNode();
            if (node instanceof FieldNode
                    && IGNORED_FIELD_NAMES.contains(((FieldNode) node)
                            .getName())) {
                return VERY_LOW;
            } else {
                return MEDIUM_HIGH;
            }
        } else if (groovyProposal instanceof GroovyCategoryMethodProposal) {
            AnnotatedNode node = groovyProposal.getAssociatedNode();
            if (!(node instanceof MethodNode)) {
                return MEDIUM_HIGH;
            }

            MethodNode method = (MethodNode) node;
            if (VariableScope.ALL_DEFAULT_CATEGORIES.contains(method.getDeclaringClass())) {
                return VERY_LOW;
            } else {
                // should be higher relevance than regular methods
                return MEDIUM_HIGH;
            }
        } else if (groovyProposal instanceof GroovyMethodProposal) {
            AnnotatedNode node = groovyProposal.getAssociatedNode();
            if (!(node instanceof MethodNode)) {
                return MEDIUM;
            }

            MethodNode method = (MethodNode) node;
            if (node instanceof MethodNode
                    && IGNORED_METHOD_NAMES.contains(method.getName())
                    || VariableScope.OBJECT_CLASS_NODE.equals(method
                            .getDeclaringClass())) {
                return VERY_LOW;
            } else {
                return MEDIUM;
            }
        } else {
            // don't really know
            return MEDIUM;
        }
    }

    // these are fields that we don't really want to see
    private static final Set<String> IGNORED_FIELD_NAMES = new HashSet<String>();
    static {
        IGNORED_FIELD_NAMES.add("metaClass");
        IGNORED_FIELD_NAMES.add("property");
        IGNORED_FIELD_NAMES.add("class");
    }

    // these are methods that we don't really want to see
    private static final Set<String> IGNORED_METHOD_NAMES = new HashSet<String>();
    static {
        IGNORED_METHOD_NAMES.add("getMetaClass");
        IGNORED_METHOD_NAMES.add("setMetaClass");
        IGNORED_METHOD_NAMES.add("getProperty");
        IGNORED_METHOD_NAMES.add("setProperty");
        IGNORED_METHOD_NAMES.add("invokeMethod");
    }

}
