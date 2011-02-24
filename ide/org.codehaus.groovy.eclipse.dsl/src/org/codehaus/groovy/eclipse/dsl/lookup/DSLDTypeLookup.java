package org.codehaus.groovy.eclipse.dsl.lookup;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Uses the current set of DSLs for this project to look up types
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLDTypeLookup extends AbstractSimplifiedTypeLookup implements ITypeLookup {

    private DSLDStore store;
    private GroovyDSLDContext initialPattern;
    
    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "DSL Type lookup created for " + unit.getElementName());
        }
        try {
            // FIXADE better error handling
            initialPattern = new GroovyDSLDContext(unit);
        } catch (CoreException e) {
            GroovyDSLCoreActivator.logException(e);
        }
        store = GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(unit.getJavaProject());
        store = store.createSubStore(initialPattern);
    }

    @Override
    protected TypeAndDeclaration lookupTypeAndDeclaration(ClassNode declaringType, String name, VariableScope scope) {
        initialPattern.setCurrentScope(scope);
        initialPattern.setTargetType(declaringType);
        // don't know about closure or annotated scopes
        // would be nice to do a succeed-fast approach here rather than trolling through all
        List<IContributionElement> elts = store.findContributions(initialPattern);
        for (IContributionElement elt : elts) {
            TypeAndDeclaration td = elt.lookupType(name, declaringType, initialPattern.resolver);
            if (td != null) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.DSL, "DSL match found for " + name + " in " + elt.contributionName());
                }
                return td;
            }
        }
        return null;
    }

}
