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
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.runtime.CoreException;

public class DSLDProposalProvider implements IProposalProvider {


    public List<IGroovyProposal> getStatementAndExpressionProposals(
            ContentAssistContext context, ClassNode completionType,
            boolean isStatic, Set<ClassNode> categories) {
        
        GroovyLogManager.manager.log(TraceCategory.DSL, "Getting DSL proposals for " + context.fullCompletionExpression);
        String event = "DSL proposals";
        GroovyLogManager.manager.logStart(event);
        
        List<IContributionElement> contributions;
        List<IGroovyProposal> proposals = new ArrayList<IGroovyProposal>();
        try {
            // FIXADE better error handling
            DSLDStore store = GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(context.unit.getJavaProject());
            GroovyDSLDContext pattern = new GroovyDSLDContext(context.unit);
            pattern.setCurrentScope(context.currentScope);
            pattern.setTargetType(completionType);
            contributions = store.findContributions(pattern);
        
            for (IContributionElement element : contributions) {
                if (element.contributionName().startsWith(context.completionExpression)) {
                    proposals.add(element.toProposal(completionType, new ResolverCache(context.unit.getResolver(), context.unit.getModuleNode())));
                }
            }
        } catch (CoreException e) {
            GroovyDSLCoreActivator.logException(e);
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
