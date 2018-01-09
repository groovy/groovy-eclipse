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
package org.codehaus.groovy.eclipse.dsl.contributions;

import static org.codehaus.groovy.eclipse.dsl.contributions.ContributionElems.removeJavadocMarkup;

import java.util.List;

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyPropertyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;

public class PropertyContributionElement implements IContributionElement {

    private final String propName;
    private final String propType;
    private final String declaringType;
    private final boolean isStatic;

    private ClassNode cachedDeclaringType;
    private ClassNode cachedType;

    private final String provider;
    private final String doc;

    private final int relevanceMultiplier;

    private final boolean isDeprecated;

    public PropertyContributionElement(String propName, String propType, String declaringType, boolean isStatic, String provider, String doc, boolean isDeprecated, int relevanceMultiplier) {
        this.propName = propName;
        this.propType = propType;
        this.isStatic = isStatic;
        this.isDeprecated = isDeprecated;
        this.declaringType = declaringType;
        this.relevanceMultiplier = relevanceMultiplier;
        this.provider = (provider != null ? removeJavadocMarkup(provider) : GROOVY_DSL_PROVIDER);
        this.doc = (doc != null ? doc : NO_DOC + (provider != null ? provider : GROOVY_DSL_PROVIDER));
    }

    @Override
    public TypeAndDeclaration lookupType(String name, ClassNode declaringType, ResolverCache resolver) {
        if (name.equals(propName)) {
            return new TypeAndDeclaration(returnType(resolver), toProperty(declaringType, resolver), declaringType(declaringType, resolver), doc);
        }
        return null;
    }

    @Override
    public IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolver) {
        GroovyPropertyProposal proposal = new GroovyPropertyProposal(toProperty(declaringType, resolver), provider);
        proposal.setRelevanceMultiplier(relevanceMultiplier);
        return proposal;
    }

    @Override
    public List<IGroovyProposal> extraProposals(ClassNode declaringType, ResolverCache resolver, Expression enclosingExpression) {
        return ProposalUtils.NO_PROPOSALS;
    }

    private PropertyNode toProperty(ClassNode declaringType, ResolverCache resolver) {
        ClassNode resolvedDeclaringType = declaringType(declaringType, resolver);

        PropertyNode prop = new PropertyNode(new FieldNode(propName, modifiers(), returnType(resolver), resolvedDeclaringType, null), modifiers(), null, null);
        prop.getField().setDeclaringClass(resolvedDeclaringType);
        prop.setDeclaringClass(resolvedDeclaringType);
        return prop;
    }

    protected int modifiers() {
        int modifiers = Opcodes.ACC_PUBLIC;
        if (isStatic) modifiers |= Opcodes.ACC_STATIC;
        if (isDeprecated) modifiers |= Opcodes.ACC_DEPRECATED;
        return modifiers;
    }

    protected ClassNode returnType(ResolverCache resolver) {
        if (cachedType == null) {
            cachedType = resolver.resolve(propType);
        }
        return cachedType == null ? ClassHelper.DYNAMIC_TYPE : cachedType;
    }

    protected ClassNode declaringType(ClassNode lexicalDeclaringType, ResolverCache resolver) {
        if (declaringType != null && cachedDeclaringType == null) {
            cachedDeclaringType = resolver.resolve(declaringType);
        }
        return cachedDeclaringType == null ? lexicalDeclaringType : cachedDeclaringType;
    }

    @Override
    public String contributionName() {
        return propName;
    }

    @Override
    public String description() {
        return "Property: " + declaringType + "." + propName;
    }

    @Override
    public String getDeclaringTypeName() {
        return declaringType;
    }

    @Override
    public String toString() {
        return "public " + (isStatic ? "static " : "") + (isDeprecated ? "deprecated " : "") + propType + " " + declaringType + "." + propName + " (" + provider + ")";
    }
}
