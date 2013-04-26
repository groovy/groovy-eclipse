/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.contributions;

import groovyjarjarasm.asm.Opcodes;

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
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;

/**
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class PropertyContributionElement implements IContributionElement {

    private static final ClassNode UNKNOWN_TYPE = ClassHelper.DYNAMIC_TYPE;

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
        super();
        this.propName = propName;
        this.propType = propType;
        this.isStatic = isStatic;
        this.declaringType = declaringType;
        this.isDeprecated = isDeprecated;
        this.relevanceMultiplier = relevanceMultiplier;
        
        this.provider = provider == null ? GROOVY_DSL_PROVIDER : provider;
        this.doc = doc == null ? NO_DOC + this.provider : doc;
    }

    public IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolver) {
        GroovyPropertyProposal groovyPropertyProposal = new GroovyPropertyProposal(toProperty(declaringType, resolver), provider);
        groovyPropertyProposal.setRelevanceMultiplier(relevanceMultiplier);
        return groovyPropertyProposal;
    }

    public TypeAndDeclaration lookupType(String name, ClassNode declaringType, ResolverCache resolver) {
        return name.equals(propName) ? new TypeAndDeclaration(ensureReturnType(resolver), toProperty(declaringType, resolver),
                ensureDeclaringType(declaringType, resolver), doc) : null;
    }

    private PropertyNode toProperty(ClassNode declaringType, ResolverCache resolver) {
        ClassNode realDeclaringType = ensureDeclaringType(declaringType, resolver);
        PropertyNode prop = new PropertyNode(new FieldNode(propName, opcode(), ensureReturnType(resolver), 
                realDeclaringType, null), opcode(), null, null);
        prop.setDeclaringClass(realDeclaringType);
        prop.getField().setDeclaringClass(realDeclaringType);
        return prop;
    }

    protected int opcode() {
        int modifiers = isStatic ? Opcodes.ACC_STATIC : Opcodes.ACC_PUBLIC;
        modifiers |= isDeprecated ? Opcodes.ACC_DEPRECATED : 0;
        return modifiers;
    }

    protected ClassNode ensureReturnType(ResolverCache resolver) {
        if (cachedType == null) {
            cachedType = resolver.resolve(propType);
        }
        return cachedType == null ? UNKNOWN_TYPE : cachedType;
    }

    protected ClassNode ensureDeclaringType(ClassNode lexicalDeclaringType, ResolverCache resolver) {
        if (declaringType != null && cachedDeclaringType == null) {
            cachedDeclaringType = resolver.resolve(declaringType);
        }
        return cachedDeclaringType == null ? lexicalDeclaringType : cachedDeclaringType;
    }
    
    public String contributionName() {
        return propName;
    }
    
    public String description() {
        return "Property: " + declaringType + "." + propName;
    }
    
    public String getDeclaringTypeName() {
        return declaringType;
    }
    
    @Override
    public String toString() {
        return "public " + (isStatic ? "static " : "") + (isDeprecated ? "deprecated " : "")
                + propType + " " + declaringType + "." + propName + " (" + provider + ")";
    }

    public List<IGroovyProposal> extraProposals(ClassNode declaringType, ResolverCache resolver, Expression enclosingExpression) {
        return ProposalUtils.NO_PROPOSALS;
    }

}
