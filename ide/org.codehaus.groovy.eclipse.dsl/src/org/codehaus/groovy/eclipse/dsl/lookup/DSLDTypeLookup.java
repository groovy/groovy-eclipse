/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.dsl.lookup;

import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLDStoreManager;
import org.codehaus.groovy.eclipse.dsl.DSLPreferences;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Uses the current set of DSLDs for this project to look up types.
 */
public class DSLDTypeLookup extends AbstractSimplifiedTypeLookup implements ITypeLookup, ITypeResolver {

    private static final String GORM_SIGNATURE = "Provided by Grails ORM DSL";

    DSLDStoreManager contextStoreManager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();

    private DSLDStore store;
    private GroovyDSLDContext pattern;
    private Set<String> disabledScriptsAsSet;
    private ModuleNode module;
    private JDTResolver resolver;

    @Override
    public void setResolverInformation(ModuleNode module, JDTResolver resolver) {
        this.module = module;
        this.resolver = resolver;
    }

    @Override
    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        if (!GroovyDSLCoreActivator.getDefault().isDSLDDisabled()) {
            // run referesh dependencies synchronously if DSLD store doesn't exist yet
            contextStoreManager.ensureInitialized(unit.getJavaProject().getProject(), true);
        }
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

    // FIXADE: Should shortcut if we find a solution earlier.
    @Override
    protected TypeAndDeclaration lookupTypeAndDeclaration(ClassNode declaringType, String name, VariableScope scope) {
        pattern.setStatic(isStatic());
        pattern.setCurrentScope(scope);
        pattern.setTargetType(declaringType);
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
        pattern.setPrimaryNode(true);
        pattern.setStatic(isStatic());
        pattern.setCurrentScope(scope);
        ClassNode delegateOrThis = scope.getDelegateOrThis();
        if (delegateOrThis != null) {
            pattern.setTargetType(delegateOrThis);
            store.findContributions(pattern, disabledScriptsAsSet);
        }
        // no need to return anything; setDelegateType is called and evaluated implicitly
    }

    @Override
    protected TypeConfidence confidence() {
        return TypeConfidence.INFERRED;
    }

    /*
     * Checks explicitly if the confidence decision should be made later.
     */
    @Override
    protected TypeConfidence checkConfidence(Expression node, TypeConfidence originalConfidence, ASTNode declaration, String extraDoc) {
        TypeConfidence confidence = originalConfidence == null ? confidence() : originalConfidence;
        if (declaration instanceof MethodNode && extraDoc != null && extraDoc.contains(GORM_SIGNATURE)) {
            // Give a chance for TypeLookups called later
            confidence = TypeConfidence.LOOSELY_INFERRED;
        }
        return confidence;
    }
}
