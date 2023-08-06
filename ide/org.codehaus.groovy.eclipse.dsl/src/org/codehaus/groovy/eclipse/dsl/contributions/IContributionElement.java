/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.contributions;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Represents a single contributed element.
 */
public interface IContributionElement {

    String GROOVY_DSL_PROVIDER = "DSL Descriptor", NO_DOC = "Provided by ";

    /**
     * Produces the main proposal for this element.
     */
    IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolverCache);

    /**
     * Produces any secondary proposals for this element (e.g. named-parameter proposals).
     */
    List<IGroovyProposal> extraProposals(ClassNode declaringType, ResolverCache resolverCache, Expression enclosingExpression);

    /**
     * @return The type and declaration information or {@code null} if no suitable declaration exists.
     */
    TypeAndDeclaration resolve(String name, ClassNode declaringType, ResolverCache resolverCache, VariableScope variableScope);

    String description();

    String getContributionName();

    String getDeclaringTypeName();
}
