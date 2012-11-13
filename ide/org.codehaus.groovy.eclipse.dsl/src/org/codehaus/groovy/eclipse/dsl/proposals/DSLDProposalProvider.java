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
import java.util.Collections;
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
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLPreferences;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
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
            ModuleNodeInfo info = context.unit.getModuleInfo(true);
            if (info == null) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST,
                            "Null module node for " + context.unit.getElementName());
                }
                return Collections.EMPTY_LIST;
            }

            GroovyDSLDContext pattern = new GroovyDSLDContext(context.unit, info.module, info.resolver);
            pattern.setCurrentScope(context.currentScope);
            pattern.setTargetType(completionType);
            pattern.setStatic(isStatic);
            pattern.setPrimaryNode(context.location == ContentAssistLocation.STATEMENT ||
            		(context.location == ContentAssistLocation.METHOD_CONTEXT && context.currentScope.isPrimaryNode()));
            contributions = store.findContributions(pattern, DSLPreferences.getDisabledScriptsAsSet());
        
            boolean isMethodContext = context instanceof MethodInfoContentAssistContext;
            for (IContributionElement element : contributions) {
                if (element.contributionName().startsWith(context.getPerceivedCompletionExpression())) {
                    IGroovyProposal proposal = element.toProposal(completionType, pattern.getResolverCache());
                    if (proposal != null) {
                        proposals.add(proposal);
                    }
                    if (isMethodContext) { 
                        // also add any related proposals, like those for method paraetersfuin
                        proposals.addAll(element.extraProposals(completionType, pattern.getResolverCache(), (Expression) ((MethodInfoContentAssistContext) context).completionNode));
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
