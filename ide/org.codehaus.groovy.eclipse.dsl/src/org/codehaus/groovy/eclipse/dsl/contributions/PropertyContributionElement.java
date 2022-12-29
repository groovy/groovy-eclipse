/*
 * Copyright 2009-2022 the original author or authors.
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

import static org.codehaus.groovy.eclipse.dsl.contributions.ContributionElems.removeJavadocMarkup;

import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyPropertyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;
import org.eclipse.jdt.groovy.search.VariableScope;

public class PropertyContributionElement implements IContributionElement {

    private final String propName;
    private final String propType;
    private final String declaringType;

    private final int modifiers;
    private final int relevanceMultiplier;

    private ClassNode cachedDeclaringType;
    private ClassNode cachedType;

    private final String provider;
    private final String doc;

    public PropertyContributionElement(final String propName, final String propType, final String declaringType, final int modifiers,
                                        final String provider, final String doc, final boolean isDeprecated, final int relevanceMultiplier) {
        this.propName = propName;
        this.propType = propType;
        this.declaringType = declaringType;
        this.modifiers = (modifiers & 0xF8) | (isDeprecated ? Flags.AccDeprecated : 0);

        this.relevanceMultiplier = relevanceMultiplier;
        this.provider = (provider != null ? removeJavadocMarkup(provider) : GROOVY_DSL_PROVIDER);
        this.doc = (doc != null ? doc : NO_DOC + (provider != null ? provider : GROOVY_DSL_PROVIDER));
    }

    @Override
    public TypeAndDeclaration resolve(final String name, final ClassNode declaringType, final ResolverCache resolver, final VariableScope scope) {
        if (name.equals(propName)) {
            return new TypeAndDeclaration(returnType(resolver), toProperty(declaringType, resolver), declaringType(declaringType, resolver), doc);
        }
        return null;
    }

    @Override
    public IGroovyProposal toProposal(final ClassNode declaringType, final ResolverCache resolver) {
        GroovyPropertyProposal proposal = new GroovyPropertyProposal(toProperty(declaringType, resolver), provider);
        proposal.setRelevanceMultiplier(relevanceMultiplier);
        return proposal;
    }

    @Override
    public List<IGroovyProposal> extraProposals(final ClassNode declaringType, final ResolverCache resolver, final Expression enclosingExpression) {
        return ProposalUtils.NO_PROPOSALS;
    }

    private PropertyNode toProperty(final ClassNode declaringType, final ResolverCache resolver) {
        ClassNode resolvedDeclaringType = declaringType(declaringType, resolver);

        FieldNode backingField = new FieldNode(propName, Flags.AccPrivate | modifiers, returnType(resolver), resolvedDeclaringType, null);
        backingField.setDeclaringClass(resolvedDeclaringType);

        PropertyNode property = new PropertyNode(backingField, Flags.AccPublic | modifiers, null, null);
        property.setDeclaringClass(resolvedDeclaringType);
        return property;
    }

    protected ClassNode returnType(final ResolverCache resolver) {
        if (cachedType == null) {
            cachedType = resolver.resolve(propType);
        }
        return cachedType == null ? ClassHelper.DYNAMIC_TYPE : cachedType;
    }

    protected ClassNode declaringType(final ClassNode lexicalDeclaringType, final ResolverCache resolver) {
        if (declaringType != null && cachedDeclaringType == null) {
            cachedDeclaringType = resolver.resolve(declaringType);
        }
        return cachedDeclaringType == null ? lexicalDeclaringType : cachedDeclaringType;
    }

    @Override
    public String getContributionName() {
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
        StringBuilder sb = new StringBuilder("public ");
        if ((modifiers & Flags.AccStatic) != 0) sb.append("static ");
        if ((modifiers & Flags.AccFinal) != 0) sb.append("final ");
        sb.append(propType);
        sb.append(' ');
        sb.append(declaringType);
        sb.append('.');
        sb.append(propName);
        sb.append(" (");
        sb.append(provider);
        sb.append(')');

        return sb.toString();
    }
}
