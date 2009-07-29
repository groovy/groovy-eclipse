/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.jdt.groovy.model;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.ReconcileWorkingCopyOperation;

/**
 * Overrides super type with a custom {@link #makeConsistent(org.eclipse.jdt.internal.core.CompilationUnit)} method
 * 
 * @author Andrew Eisenberg
 * @created Jun 29, 2009
 */
public class GroovyReconcileWorkingCopyOperation extends ReconcileWorkingCopyOperation {

	public GroovyReconcileWorkingCopyOperation(IJavaElement workingCopy, int astLevel, int reconcileFlags,
			WorkingCopyOwner workingCopyOwner) {
		super(workingCopy, astLevel, reconcileFlags, workingCopyOwner);
	}

	// @Override
	// public org.eclipse.jdt.core.dom.CompilationUnit makeConsistent(CompilationUnit workingCopy) throws JavaModelException {
	// if (!workingCopy.isConsistent()) {
	// // make working copy consistent
	// if (this.problems == null)
	// this.problems = new HashMap();
	// this.resolveBindings = this.requestorIsActive;
	// this.ast = workingCopy.makeConsistent(this.astLevel, this.resolveBindings, reconcileFlags, this.problems,
	// this.progressMonitor);
	// this.deltaBuilder.buildDeltas();
	// if (this.ast != null && this.deltaBuilder.delta != null)
	// this.deltaBuilder.delta.changedAST(this.ast);
	// return this.ast;
	// }
	//
	// CompilationUnitDeclaration unit = null;
	// try {
	// JavaModelManager.getJavaModelManager().abortOnMissingSource.set(Boolean.TRUE);
	// CompilationUnit source = workingCopy.cloneCachingContents();
	// // find problems if needed
	// if (GroovyNature.hasGroovyNature(workingCopy.getJavaProject().getProject())
	// && (this.reconcileFlags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0) {
	// this.resolveBindings = this.requestorIsActive;
	// if (this.problems == null) {
	// this.problems = new HashMap();
	// }
	//
	// ///////////////////////////
	// // do problem finding here!
	// ///////////////////////////
	//				
	//				
	// // unit = CompilationUnitProblemFinder.process(source, this.workingCopyOwner, this.problems,
	// // this.astLevel != ICompilationUnit.NO_AST/* creating AST if level is not NO_AST */, reconcileFlags,
	// // this.progressMonitor);
	// if (this.progressMonitor != null)
	// this.progressMonitor.worked(1);
	// }
	//
	// // create AST if needed
	// if (this.astLevel != ICompilationUnit.NO_AST && unit != null/*
	// * unit is null if working copy is consistent && (problem
	// * detection not forced || non-Java project) -> don't create
	// * AST as per API
	// */) {
	// Map options = workingCopy.getJavaProject().getOptions(true);
	// // convert AST
	// this.ast = AST.convertCompilationUnit(this.astLevel, unit, options, this.resolveBindings, source, reconcileFlags,
	// this.progressMonitor);
	// if (this.ast != null) {
	// if (this.deltaBuilder.delta == null) {
	// this.deltaBuilder.delta = new JavaElementDelta(workingCopy);
	// }
	// this.deltaBuilder.delta.changedAST(this.ast);
	// }
	// if (this.progressMonitor != null)
	// this.progressMonitor.worked(1);
	// }
	// } catch (JavaModelException e) {
	// if (JavaProject.hasJavaNature(workingCopy.getJavaProject().getProject()))
	// throw e;
	// // else JavaProject has lost its nature (or most likely was closed/deleted) while reconciling -> ignore
	// // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=100919)
	// } finally {
	// JavaModelManager.getJavaModelManager().abortOnMissingSource.set(null);
	// if (unit != null) {
	// unit.cleanUp();
	// }
	// }
	//
	// return this.ast;
	// }

	// @Override
	// public CompilationUnit makeConsistent(org.eclipse.jdt.internal.core.CompilationUnit workingCopy) throws JavaModelException {
	// if (this.problems == null) {
	// this.problems = new HashMap();
	// }
	// this.ast = workingCopy.makeConsistent(AST.JLS3, true, reconcileFlags, this.problems, this.progressMonitor);
	// this.deltaBuilder.buildDeltas();
	// if (this.ast != null && this.deltaBuilder.delta != null) {
	// this.deltaBuilder.delta.changedAST(this.ast);
	// }
	// return this.ast;
	// }
}
