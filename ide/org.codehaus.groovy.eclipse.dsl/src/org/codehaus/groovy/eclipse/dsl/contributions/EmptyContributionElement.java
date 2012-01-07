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
package org.codehaus.groovy.eclipse.dsl.contributions;

import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;

/**
 * 
 * @author andrew
 * @created Jan 6, 2012
 */
public class EmptyContributionElement implements IContributionElement {

    private ClassNode declaringType;

    public EmptyContributionElement(ClassNode declaringType) {
        this.declaringType = declaringType;
    }

    public IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolver) {
        return null;
    }

    public List<IGroovyProposal> extraProposals(ClassNode declaringType, ResolverCache resolver, Expression enclosingExpression) {
        return Collections.emptyList();
    }

    public TypeAndDeclaration lookupType(String name, ClassNode declaringType, ResolverCache resolver) {
        return null;
    }

    public String contributionName() {
        return "";
    }

    public String description() {
        return "Empty contribution";
    }

    public String getDeclaringTypeName() {
        return declaringType.getName();
    }

}
