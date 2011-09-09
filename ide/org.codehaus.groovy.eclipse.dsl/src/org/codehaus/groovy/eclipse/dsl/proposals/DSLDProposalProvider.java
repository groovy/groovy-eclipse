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
package org.codehaus.groovy.eclipse.dsl.proposals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.processors.IProposalProvider;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLPreferences;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.runtime.CoreException;

public class DSLDProposalProvider implements IProposalProvider {


    public List<IGroovyProposal> getStatementAndExpressionProposals(
            ContentAssistContext context, ClassNode completionType,
            boolean isStatic, Set<ClassNode> categories) {
                String event = null;
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Getting DSL proposals for " + context.fullCompletionExpression);
            event = "DSL proposals";
            GroovyLogManager.manager.logStart(event);
        }        
        List<IContributionElement> contributions;
        List<IGroovyProposal> proposals = new ArrayList<IGroovyProposal>();
        try {
            DSLDStore store = GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(context.unit.getJavaProject());
            GroovyDSLDContext pattern = new GroovyDSLDContext(context.unit);
            pattern.setCurrentScope(context.currentScope);
            pattern.setTargetType(completionType);
            contributions = store.findContributions(pattern, DSLPreferences.getDisabledScriptsAsSet());
        
            ResolverCache resolver = new ResolverCache(context.unit.getResolver(), context.unit.getModuleNode());
            for (IContributionElement element : contributions) {
                if (element.contributionName().startsWith(context.getPerceivedCompletionExpression())) {
                    proposals.add(element.toProposal(completionType, resolver));
                    if (context instanceof MethodInfoContentAssistContext) { 
                        // also add any related proposals, like those for method paraetersfuin
                        proposals.addAll(element.extraProposals(completionType, resolver, (Expression) ((MethodInfoContentAssistContext) context).completionNode));
                    }
                }
            }
        } catch (CoreException e) {
            GroovyDSLCoreActivator.logException(e);
        }
        if (event != null) {
            GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
        }
        
        return proposals;
    }

    public List<MethodNode> getNewMethodProposals(ContentAssistContext context) {
        return null;
    }

    public List<String> getNewFieldProposals(ContentAssistContext context) {
        return null;
    }

}
