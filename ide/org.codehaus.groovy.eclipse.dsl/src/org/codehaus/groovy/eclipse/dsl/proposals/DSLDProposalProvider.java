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
package org.codehaus.groovy.eclipse.dsl.proposals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
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
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;

public class DSLDProposalProvider implements IProposalProvider {

    @Override
    public List<String> getNewFieldProposals(final ContentAssistContext context) {
        return null;
    }

    @Override
    public List<MethodNode> getNewMethodProposals(final ContentAssistContext context) {
        return null;
    }

    @Override
    public List<IGroovyProposal> getStatementAndExpressionProposals(final ContentAssistContext context,
            final ClassNode completionType, final boolean isStatic, final Set<ClassNode> categoryTypes) {
        String event = null;
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "Getting DSL proposals for " + context.fullCompletionExpression);
            event = "DSL proposals";
            GroovyLogManager.manager.logStart(event);
        }
        List<IGroovyProposal> proposals = new ArrayList<>();
        try {
            DSLDStore store = GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(context.unit.getJavaProject().getProject());
            ModuleNodeInfo info = context.unit.getModuleInfo(true);
            if (info == null) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST, "Null module node for " + context.unit.getElementName());
                }
            } else {
                GroovyDSLDContext dsldContext = new GroovyDSLDContext(context.unit, info.module, info.resolver);
                dsldContext.setCurrentScope(context.currentScope);
                dsldContext.setPrimaryNode(
                    (context.location == ContentAssistLocation.SCRIPT) ||
                    (context.location == ContentAssistLocation.STATEMENT) ||
                    (context.location == ContentAssistLocation.METHOD_CONTEXT && context.currentScope.isPrimaryNode()));
                dsldContext.setStatic(isStatic);
                dsldContext.setTargetType(completionType);

                List<IContributionElement> contributions = store.findContributions(dsldContext, DSLPreferences.getDisabledScriptsAsSet());

                String completionString = context.getPerceivedCompletionExpression();
                boolean isMethodContext = context instanceof MethodInfoContentAssistContext;
                AssistOptions   options = new AssistOptions(context.unit.getJavaProject().getOptions(true));

                for (IContributionElement element : contributions) {
                    if (ProposalUtils.matches(completionString, element.getContributionName(), options.camelCaseMatch, options.subwordMatch)) {
                        IGroovyProposal proposal = element.toProposal(completionType, dsldContext.getResolverCache());
                        if (proposal != null) {
                            proposals.add(proposal);
                        }
                        if (isMethodContext) {
                            // also add any related proposals, like those for method parameters
                            proposals.addAll(element.extraProposals(completionType, dsldContext.getResolverCache(), (Expression) context.completionNode));
                        }
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
}
