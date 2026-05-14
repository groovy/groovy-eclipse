/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.jdt.groovy.model;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.JavaElementDelta;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.ReconcileWorkingCopyOperation;

/**
 * Overrides super type with a custom {@link #makeConsistent(org.eclipse.jdt.internal.core.CompilationUnit)} method.
 *
 * Need to ensure that the {@link ModuleNode} is cached in the {@link ModuleNodeMapper} after a call to make consistent.
 */
public class GroovyReconcileWorkingCopyOperation extends ReconcileWorkingCopyOperation {

    WorkingCopyOwner workingCopyOwner;

    public GroovyReconcileWorkingCopyOperation(IJavaElement workingCopy, int astLevel, int reconcileFlags, WorkingCopyOwner workingCopyOwner) {
        super(workingCopy, astLevel, reconcileFlags, workingCopyOwner);
        this.workingCopyOwner = workingCopyOwner;
    }

    /**
     * Makes the given working copy consistent, computes the delta and computes an AST if needed.
     * Returns the AST.
     */
    @Override
    public org.eclipse.jdt.core.dom.CompilationUnit makeConsistent(CompilationUnit workingCopy) throws JavaModelException {
        if (!workingCopy.isConsistent()) {
            // make working copy consistent
            if (this.problems == null)
                this.problems = new HashMap<>();
            this.resolveBindings = this.requestorIsActive;
            this.ast = workingCopy.makeConsistent(this.astLevel, this.resolveBindings, this.reconcileFlags, this.problems, this.progressMonitor);
            this.deltaBuilder.buildDeltas();
            if (this.ast != null && this.deltaBuilder.delta != null)
                this.deltaBuilder.delta.changedAST(this.ast);
            return this.ast;
        }
        if (this.ast != null)
            return this.ast; // no need to recompute AST if known already

        CompilationUnitDeclaration unit = null;
        try {
            JavaModelManager.getJavaModelManager().abortOnMissingSource.set(Boolean.TRUE);
            CompilationUnit source = workingCopy.cloneCachingContents();
            // find problems if needed
            if (JavaProject.hasJavaNature(workingCopy.getJavaProject().getProject())
                    && (this.reconcileFlags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0) {
                this.resolveBindings = this.requestorIsActive;
                if (this.problems == null)
                    this.problems = new HashMap<>();
                unit =
                    CompilationUnitProblemFinder.process(
                        source,
                        this.workingCopyOwner,
                        this.problems,
                        this.astLevel != ICompilationUnit.NO_AST, // creating AST if level is not NO_AST
                        this.reconcileFlags,
                        this.progressMonitor);
                // GROOVY add -- cache the ModuleNode in the ModuleNodeMapper
                if (unit instanceof GroovyCompilationUnitDeclaration && !(workingCopy instanceof GroovyClassFileWorkingCopy)) {
                    ModuleNodeMapper.getInstance().maybeCacheModuleNode(workingCopy.getPerWorkingCopyInfo(), (GroovyCompilationUnitDeclaration) unit);
                }
                // GROOVY end
                if (this.progressMonitor != null) this.progressMonitor.worked(1);
            }

            // create AST if needed
            if (this.astLevel != ICompilationUnit.NO_AST
                    && unit != null /*unit is null if working copy is consistent && (problem detection not forced || non-Java project) -> don't create AST as per API*/) {
                Map<String, String> options = workingCopy.getJavaProject().getOptions(true);
                // convert AST
                this.ast =
                    AST.convertCompilationUnit(
                        this.astLevel,
                        unit,
                        options,
                        this.resolveBindings,
                        source,
                        this.reconcileFlags,
                        this.progressMonitor);
                if (this.ast != null) {
                    if (this.deltaBuilder.delta == null) {
                        this.deltaBuilder.delta = new JavaElementDelta(workingCopy);
                    }
                    this.deltaBuilder.delta.changedAST(this.ast);
                }
                if (this.progressMonitor != null) this.progressMonitor.worked(1);
            }
        } catch (JavaModelException e) {
            if (JavaProject.hasJavaNature(workingCopy.getJavaProject().getProject()))
                throw e;
            // else JavaProject has lost its nature (or most likely was closed/deleted) while reconciling -> ignore
            // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=100919)
        } finally {
            JavaModelManager.getJavaModelManager().abortOnMissingSource.set(null);
            if (unit != null) {
                unit.cleanUp();
            }
        }
        return this.ast;
    }
}
