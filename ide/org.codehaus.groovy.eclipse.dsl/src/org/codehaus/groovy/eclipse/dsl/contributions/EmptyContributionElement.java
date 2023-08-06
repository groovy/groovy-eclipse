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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;
import org.eclipse.jdt.groovy.search.VariableScope;

public class EmptyContributionElement implements IContributionElement {

    private ClassNode declaringType;

    public EmptyContributionElement(final ClassNode declaringType) {
        this.declaringType = Objects.requireNonNull(declaringType);
    }

    @Override
    public IGroovyProposal toProposal(final ClassNode declaringType, final ResolverCache resolver) {
        return null;
    }

    @Override
    public List<IGroovyProposal> extraProposals(final ClassNode declaringType, final ResolverCache resolver, final Expression enclosing) {
        return Collections.emptyList();
    }

    @Override
    public TypeAndDeclaration resolve(final String name, final ClassNode declaringType, final ResolverCache resolver, final VariableScope scope) {
        return null;
    }

    @Override
    public String description() {
        return "Empty contribution";
    }

    @Override
    public String getContributionName() {
        return "";
    }

    @Override
    public String getDeclaringTypeName() {
        return declaringType.getName();
    }
}
