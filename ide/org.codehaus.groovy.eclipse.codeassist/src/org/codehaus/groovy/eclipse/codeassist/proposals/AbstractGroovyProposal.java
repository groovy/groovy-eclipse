/*
 * Copyright 2009-2024 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

public abstract class AbstractGroovyProposal implements IGroovyProposal {

    /**
     * @return AST node associated with this proposal, or {@code null} if there is none
     */
    public AnnotatedNode getAssociatedNode() {
        return null;
    }

    public int getAssociatedNodeFlags() {
        AnnotatedNode node = getAssociatedNode();
        if (node instanceof ClassNode) {
            return ((ClassNode) node).getModifiers();
        }
        if (node instanceof FieldNode) {
            return ((FieldNode) node).getModifiers();
        }
        if (node instanceof MethodNode) {
            return ((MethodNode) node).getModifiers();
        }
        if (node instanceof PropertyNode) {
            return ((PropertyNode) node).getModifiers();
        }
        return 0;
    }

    /** TODO */
    private float relevanceMultiplier = 1;

    public final float getRelevanceMultiplier() {
        return relevanceMultiplier;
    }

    public final void setRelevanceMultiplier(float relevanceMultiplier) {
        // TODO: Ensure multiplier is between 0.1 and 10 (exclusive)?
        this.relevanceMultiplier = relevanceMultiplier;
    }

    /** TODO */
    private String requiredQualifier;

    public final String getRequiredQualifier() {
        return requiredQualifier;
    }

    public final void setRequiredQualifier(String requiredQualifier) {
        this.requiredQualifier = requiredQualifier;
    }

    /** TODO */
    private String requiredStaticImport;

    public final String getRequiredStaticImport() {
        return requiredStaticImport;
    }

    public final void setRequiredStaticImport(String requiredStaticImport) {
        this.requiredStaticImport = requiredStaticImport;
    }

    //--------------------------------------------------------------------------

    protected int computeRelevance(ContentAssistContext context) {
        int relevance = getRelevanceClass().getRelevance(getRelevanceMultiplier());

        AnnotatedNode node = getAssociatedNode();
        if (node != null) {
            if (context.lhsType != null) {
                ClassNode type = null;
                if (node instanceof FieldNode) {
                    type = ((FieldNode) node).getType();
                } else if (node instanceof MethodNode) {
                    type = ((MethodNode) node).getReturnType();
                } else if (node instanceof PropertyNode) {
                    type = ((PropertyNode) node).getType();
                }
                if (type != null && GroovyUtils.isAssignable(type, context.lhsType)) {
                    relevance = Math.max(relevance, Relevance.HIGH.getRelevance());
                }
            }

            if (StringGroovyMethods.asBoolean(context.completionExpression)) {
                String name = null;
                if (node instanceof FieldNode) {
                    name = ((FieldNode) node).getName();
                } else if (node instanceof MethodNode) {
                    name = ((MethodNode) node).getName();
                } else if (node instanceof PropertyNode) {
                    name = ((PropertyNode) node).getName();
                }
                if (StringGroovyMethods.asBoolean(name)) {
                    String expr = context.getPerceivedCompletionExpression();
                    if (name.equals(expr)) {
                        relevance += RelevanceConstants.R_EXACT_NAME + RelevanceConstants.R_CASE;
                    } else if (name.equalsIgnoreCase(expr)) {
                        relevance += RelevanceConstants.R_EXACT_NAME;
                    } else if (/*name.startsWithIgnoreCase(expr)*/CharOperation.prefixEquals(expr.toCharArray(), name.toCharArray(), false)) {
                        if (name.startsWith(expr)) relevance += RelevanceConstants.R_CASE;
                    } else if (/*options.camelCaseMatch &&*/ SearchPattern.camelCaseMatch(expr, name)) {
                        relevance += RelevanceConstants.R_CAMEL_CASE;
                    } else if (/*options.substringMatch &&*/ CharOperation.substringMatch(expr, name)) {
                        relevance += RelevanceConstants.R_SUBSTRING;
                    }/* else if (options.subwordMatch && CharOperation.getSubWordMatchingRegions(expr, name) != null) {
                        relevance += RelevanceConstants.R_SUBWORD;
                    }*/
                }
            }
        }

        if (relevance > 1 && this instanceof GroovyMethodProposal) {
            relevance *= 0.99f; // promote fields
        }

        return Math.max(relevance, RelevanceConstants.R_INTERESTING);
    }

    private Relevance getRelevanceClass() {
        AnnotatedNode node = getAssociatedNode();
        if (node != null && EXTREMELY_COMMON_TYPES.contains(node.getDeclaringClass())) {
            return Relevance.VERY_LOW;
        }
        if (node instanceof FieldNode || node instanceof MethodNode || node instanceof PropertyNode) {
            return Relevance.MEDIUM_HIGH;
        }
        return Relevance.MEDIUM;
    }

    private static final Set<ClassNode> EXTREMELY_COMMON_TYPES;
    static {
        Set<ClassNode> types = new HashSet<>();
        types.add(VariableScope.CLASS_CLASS_NODE);
        types.add(VariableScope.OBJECT_CLASS_NODE);
        types.add(VariableScope.CLOSURE_CLASS_NODE);
        types.add(VariableScope.GROOVY_OBJECT_CLASS_NODE);
        types.add(VariableScope.GROOVY_SUPPORT_CLASS_NODE);
        EXTREMELY_COMMON_TYPES = Collections.unmodifiableSet(types);
    }
}
