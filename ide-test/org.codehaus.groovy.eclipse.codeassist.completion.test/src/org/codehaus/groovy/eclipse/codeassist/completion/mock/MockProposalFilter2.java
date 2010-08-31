package org.codehaus.groovy.eclipse.codeassist.completion.mock;

import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.processors.IProposalFilter;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;

public class MockProposalFilter2 implements IProposalFilter {

    private static boolean filterCalled = false;
    
    public List<IGroovyProposal> filterProposals(List<IGroovyProposal> proposals) {
        filterCalled = true;
        return proposals;
    }
    
    public static boolean wasFilterCalled() {
        return filterCalled;
    }

    public static void reset() {
        filterCalled = false;
    }
}
