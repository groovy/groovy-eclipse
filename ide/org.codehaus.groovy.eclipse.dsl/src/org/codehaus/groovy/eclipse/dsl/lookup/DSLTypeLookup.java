package org.codehaus.groovy.eclipse.dsl.lookup;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLActivator;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextPattern;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextStore;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Uses the current set of DSLs for this project to look up types
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLTypeLookup extends AbstractSimplifiedTypeLookup implements ITypeLookup {

    private ContextStore store;
    private ContextPattern initialPattern;
    private ResolverCache resolver;
    
    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        GroovyLogManager.manager.log(TraceCategory.DSL, "DSL Type lookup created for " + unit.getElementName());
        initialPattern = new ContextPattern(unit.getElementName(), null, topLevelScope);
        store = GroovyDSLActivator.getDefault().getContextStoreManager().getTransitiveContextStore(unit.getJavaProject());
        store = store.createSubStore(initialPattern);
        resolver = new ResolverCache(unit.getResolver());
    }

    @Override
    protected TypeAndDeclaration lookupTypeAndDeclaration(ClassNode declaringType, String name, VariableScope scope) {
        ContextPattern copy = initialPattern.copy(scope);
        copy.setTargetType(declaringType);
        // don't know about closure or annotated scopes
        // would be nice to do a succeed-fast approach here rather than trolling through all
        List<IContributionElement> elts = store.findContributions(copy, declaringType, scope);
        for (IContributionElement elt : elts) {
            TypeAndDeclaration td = elt.lookupType(name, declaringType, resolver);
            if (td != null) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "DSL match found for " + name + " in " + elt.contributionName());
                return td;
            }
        }
        return null;
    }

}
