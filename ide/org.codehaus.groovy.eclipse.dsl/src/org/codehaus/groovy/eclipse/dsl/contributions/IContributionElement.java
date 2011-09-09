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

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;

/**
 * Represents a single contributed element
 * @author andrew
 * @created Nov 17, 2010
 */
public interface IContributionElement {
    String GROOVY_DSL_PROVIDER = "DSL Descriptor";
    String NO_DOC = "Provided by ";

    /**
     * Produces the main proposal for this element
     * @param declaringType
     * @param resolver
     * @return
     */
    IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolver);
    /**
     * Produces any subsidiary proposals for this element (eg- named parameter proposals)
     * @param declaringType
     * @param resolver
     * @param enclosingExpression
     * @return
     */
    List<IGroovyProposal> extraProposals(ClassNode declaringType, ResolverCache resolver, Expression enclosingExpression);
    TypeAndDeclaration lookupType(String name, ClassNode declaringType, ResolverCache resolver);
    String contributionName();
    String description();
    String getDeclaringTypeName();
}
