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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLDStoreManager;
import org.codehaus.groovy.eclipse.dsl.DSLPreferences;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.internal.Workbench;

/**
 * Uses the current set of DSLs for this project to look up types
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLDTypeLookup extends AbstractSimplifiedTypeLookup implements ITypeLookup, ITypeResolver {

    DSLDStoreManager contextStoreManager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();

    private DSLDStore store;
    private GroovyDSLDContext pattern;
    private Set<String> disabledScriptsAsSet;
    private ModuleNode module;
    private JDTResolver resolver;
    
    public void setResolverInformation(ModuleNode module, JDTResolver resolver) {
        this.module = module;
        this.resolver = resolver;
    }

    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        
        // run referesh dependencies synchronously if DSLD store doesn't exist yet
        final IProject project = unit.getJavaProject().getProject();
        contextStoreManager.ensureInitialized(project, true);
        
        disabledScriptsAsSet = DSLPreferences.getDisabledScriptsAsSet();
        try {
            pattern = new GroovyDSLDContext(unit, module, resolver);
            pattern.setCurrentScope(topLevelScope);
        } catch (CoreException e) {
            GroovyDSLCoreActivator.logException(e);
        }
        store = contextStoreManager.getDSLDStore(unit.getJavaProject());
        store = store.createSubStore(pattern);
    }

    // FIXADE Should shortcut if we find a solution earlier.
    @Override
    protected TypeAndDeclaration lookupTypeAndDeclaration(ClassNode declaringType, String name, VariableScope scope) {
        pattern.setCurrentScope(scope);
        pattern.setTargetType(declaringType);
        pattern.setStatic(isStatic());
        List<IContributionElement> elts = store.findContributions(pattern, disabledScriptsAsSet);
        declaringType = pattern.getCurrentType(); // may have changed via a setDelegateType
        for (IContributionElement elt : elts) {
            TypeAndDeclaration td = elt.lookupType(name, declaringType, pattern.getResolverCache());
            if (td != null) {
                return td;
            }
        }
        return null;
    }
    
    /**
     * setDelegateType must be called even for empty block statements
     */
    @Override
    public void lookupInBlock(BlockStatement node, VariableScope scope) {
        pattern.setCurrentScope(scope);
        ClassNode delegateOrThis = scope.getDelegateOrThis();
        if (delegateOrThis != null) {
            pattern.setTargetType(delegateOrThis);
            pattern.setStatic(isStatic());
            store.findContributions(pattern, disabledScriptsAsSet);
        }
        // no need to return anything.  setDelegateType is called and evaluated implicitly
    }
    
    
    @Override
    protected TypeConfidence confidence() {
        return TypeConfidence.INFERRED;
    }

}
