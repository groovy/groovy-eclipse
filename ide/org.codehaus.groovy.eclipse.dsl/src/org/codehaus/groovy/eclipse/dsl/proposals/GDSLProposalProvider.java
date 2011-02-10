package org.codehaus.groovy.eclipse.dsl.proposals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.processors.IProposalProvider;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLActivator;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextPattern;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextStore;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;

public class GDSLProposalProvider implements IProposalProvider {


    public List<IGroovyProposal> getStatementAndExpressionProposals(
            ContentAssistContext context, ClassNode completionType,
            boolean isStatic, Set<ClassNode> categories) {
        
        GroovyLogManager.manager.log(TraceCategory.DSL, "Getting DSL proposals for " + context.fullCompletionExpression);
        String event = "DSL proposals";
        GroovyLogManager.manager.logStart(event);
        
        ContextStore store = GroovyDSLActivator.getDefault().getContextStoreManager().getTransitiveContextStore(context.unit.getJavaProject());
        ContextPattern pattern = new ContextPattern(context.unit.getElementName(), completionType, context.currentScope);
        List<IContributionElement> contributions = store.findContributions(pattern, completionType, context.currentScope);
        
        List<IGroovyProposal> proposals = new ArrayList<IGroovyProposal>();
        for (IContributionElement element : contributions) {
            if (element.contributionName().startsWith(context.completionExpression)) {
                proposals.add(element.toProposal(completionType, new ResolverCache(context.unit.getResolver())));
            }
        }
        GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
        
        return proposals;
    }

    public List<MethodNode> getNewMethodProposals(ContentAssistContext context) {
        return null;
    }

    public List<String> getNewFieldProposals(ContentAssistContext context) {
        return null;
    }

}
