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
package org.codehaus.groovy.eclipse.dsl.lookup;

import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLPreferences;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Uses the current set of DSLs for this project to look up types
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLDTypeLookup extends AbstractSimplifiedTypeLookup implements ITypeLookup {

    private DSLDStore store;
    private GroovyDSLDContext pattern;
    private Set<String> disabledScriptsAsSet;
    
    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {

        disabledScriptsAsSet = DSLPreferences.getDisabledScriptsAsSet();
        try {
            pattern = new GroovyDSLDContext(unit);
        } catch (CoreException e) {
            GroovyDSLCoreActivator.logException(e);
        }
        store = GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(unit.getJavaProject());
        store = store.createSubStore(pattern);
    }

    // FIXADE Should shortcut if we find a solution earlier.
    @Override
    protected TypeAndDeclaration lookupTypeAndDeclaration(ClassNode declaringType, String name, VariableScope scope) {
        pattern.setCurrentScope(scope);
        pattern.setTargetType(declaringType);
        List<IContributionElement> elts = store.findContributions(pattern, disabledScriptsAsSet);
        for (IContributionElement elt : elts) {
            TypeAndDeclaration td = elt.lookupType(name, declaringType, pattern.resolver);
            if (td != null) {
                return td;
            }
        }
        return null;
    }
    
    
    @Override
    protected TypeConfidence confidence() {
        return TypeConfidence.INFERRED;
    }

}
