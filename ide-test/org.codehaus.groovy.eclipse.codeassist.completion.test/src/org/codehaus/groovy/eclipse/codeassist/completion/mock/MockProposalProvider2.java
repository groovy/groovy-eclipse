package org.codehaus.groovy.eclipse.codeassist.completion.mock;

import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.processors.IProposalProvider;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;

public class MockProposalProvider2 implements IProposalProvider {

    private static boolean providerCalled = false;
    
    public static boolean wasProviderCalled() {
        return providerCalled;
    }

    public static void reset() {
        providerCalled = false;
    }


    public List<IGroovyProposal> getStatementAndExpressionProposals(
            ContentAssistContext context, ClassNode completionType,
            boolean isStatic, Set<ClassNode> categories) {
        providerCalled = true;
        return null;
    }

    public List<MethodNode> getNewMethodProposals(ContentAssistContext context) {
        providerCalled = true;
        return null;
    }

    public List<String> getNewFieldProposals(ContentAssistContext context) {
        providerCalled = true;
        return null;
    }

}
