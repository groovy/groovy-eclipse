/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Reconcile a working copy and signal the changes through a delta.
 * <p>
 * High level summmary of what a reconcile does:
 * <ul>
 * <li>populates the model with the new working copy contents</li>
 * <li>fires a fine grained delta (flag F_FINE_GRAINED) describing the difference between the previous content
 *      and the new content (which method was added/removed, which field was changed, etc.)</li>
 * <li>computes problems and reports them to the IProblemRequestor {@code (begingReporting(), n x acceptProblem(...), endReporting()) iff
 *     	(working copy is not consistent with its buffer || forceProblemDetection is set)
 * 		&& problem} requestor is active
 * </li>
 * <li>produces a DOM AST (either JLS_2, JLS_3 or NO_AST) that is resolved if flag is set</li>
 * <li>notifies compilation participants of the reconcile allowing them to participate in this operation and report problems</li>
 * </ul>
 */
public class ReconcileWorkingCopyOperation extends JavaModelOperation {
	public static boolean PERF = false;

	public int astLevel;
	public boolean resolveBindings;
	public Map<String, CategorizedProblem[]>  problems;
	public int reconcileFlags;
	WorkingCopyOwner workingCopyOwner;
	public org.eclipse.jdt.core.dom.CompilationUnit ast;
	public JavaElementDeltaBuilder deltaBuilder;
	public boolean requestorIsActive;

	public ReconcileWorkingCopyOperation(IJavaElement workingCopy, int astLevel, int reconcileFlags, WorkingCopyOwner workingCopyOwner) {
		super(new IJavaElement[] {workingCopy});
		this.astLevel = astLevel;
		this.reconcileFlags = reconcileFlags;
		this.workingCopyOwner = workingCopyOwner;
	}

	/**
	 * @exception JavaModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	@Override
	protected void executeOperation() throws JavaModelException {
		checkCanceled();
		try {
			beginTask(Messages.element_reconciling, 2);

			CompilationUnit workingCopy = getWorkingCopy();
			boolean wasConsistent = workingCopy.isConsistent();

			// check is problem requestor is active
			IProblemRequestor problemRequestor = workingCopy.getPerWorkingCopyInfo();
			if (problemRequestor != null)
				problemRequestor =  ((JavaModelManager.PerWorkingCopyInfo)problemRequestor).getProblemRequestor();
			boolean defaultRequestorIsActive = problemRequestor != null && problemRequestor.isActive();
			IProblemRequestor ownerProblemRequestor = this.workingCopyOwner.getProblemRequestor(workingCopy);
			boolean ownerRequestorIsActive = ownerProblemRequestor != null && ownerProblemRequestor != problemRequestor && ownerProblemRequestor.isActive();
			this.requestorIsActive = defaultRequestorIsActive || ownerRequestorIsActive;

			// create the delta builder (this remembers the current content of the cu)
			this.deltaBuilder = new JavaElementDeltaBuilder(workingCopy);

			// make working copy consistent if needed and compute AST if needed
			makeConsistent(workingCopy);

			// notify reconcile participants only if working copy was not consistent or if forcing problem detection
			// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=177319)
			if (!wasConsistent || ((this.reconcileFlags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0)) {
				notifyParticipants(workingCopy);

				// recreate ast if one participant reset it
				if (this.ast == null)
					makeConsistent(workingCopy);
			}

			// report problems
			if (this.problems != null && (((this.reconcileFlags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0) || !wasConsistent)) {
				if (defaultRequestorIsActive) {
					reportProblems(workingCopy, problemRequestor);
				}
				if (ownerRequestorIsActive) {
					reportProblems(workingCopy, ownerProblemRequestor);
				}
			}

			// report delta
			JavaElementDelta delta = this.deltaBuilder.delta;
			if (delta != null) {
				addReconcileDelta(workingCopy, delta);
			}
		} finally {
			done();
		}
	}

	/**
	 * Report working copy problems to a given requestor.
	 */
	private void reportProblems(CompilationUnit workingCopy, IProblemRequestor problemRequestor) {
		try {
			problemRequestor.beginReporting();
			for (CategorizedProblem[] categorizedProblems : this.problems.values()) {
				if (categorizedProblems == null) continue;
				for (CategorizedProblem problem : categorizedProblems) {
					if (JavaModelManager.VERBOSE){
						JavaModelManager.trace("PROBLEM FOUND while reconciling : " + problem.getMessage());//$NON-NLS-1$
					}
					if (this.progressMonitor != null && this.progressMonitor.isCanceled()) break;
					problemRequestor.acceptProblem(problem);
				}
			}
		} finally {
			problemRequestor.endReporting();
		}
	}

	/**
	 * Returns the working copy this operation is working on.
	 */
	protected CompilationUnit getWorkingCopy() {
		return (CompilationUnit)getElementToProcess();
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	/*
	 * Makes the given working copy consistent, computes the delta and computes an AST if needed.
	 * Returns the AST.
	 */
	public org.eclipse.jdt.core.dom.CompilationUnit makeConsistent(CompilationUnit workingCopy) throws JavaModelException {
		if (!workingCopy.isConsistent()) {
			// make working copy consistent
			if (this.problems == null) this.problems = new HashMap<>();
			this.resolveBindings = this.requestorIsActive;
			this.ast = workingCopy.makeConsistent(this.astLevel, this.resolveBindings, this.reconcileFlags, this.problems, this.progressMonitor);
			this.deltaBuilder.buildDeltas();
			if (this.ast != null && this.deltaBuilder.delta != null)
				this.deltaBuilder.delta.changedAST(this.ast);
			return this.ast;
		}
		if (this.ast != null)
			return this.ast; // no need to recompute AST if known already

		try {
			JavaModelManager.getJavaModelManager().abortOnMissingSource.set(Boolean.TRUE);
			CompilationUnit source = workingCopy.cloneCachingContents();
			// find problems if needed
			if (JavaProject.hasJavaNature(workingCopy.getJavaProject().getProject())
					&& (this.reconcileFlags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0) {
				this.resolveBindings = this.requestorIsActive;
				if (this.problems == null) {
					this.problems = new HashMap<>();
				}
				Map<String, String> options = workingCopy.getJavaProject().getOptions(true);
				if (CompilationUnit.DOM_BASED_OPERATIONS) {
					try {
						ASTParser parser = ASTParser.newParser(this.astLevel > 0 ? this.astLevel : AST.getJLSLatest());
						parser.setResolveBindings(this.resolveBindings || (this.reconcileFlags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0);
						parser.setCompilerOptions(options);
						parser.setSource(source);
						org.eclipse.jdt.core.dom.CompilationUnit newAST = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(this.progressMonitor);
						if ((this.reconcileFlags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0 && newAST != null) {
							newAST.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()); //trigger resolution and analysis
						}
						Map<String, List<CategorizedProblem>> groupedProblems = new HashMap<>();
						for (IProblem problem : newAST.getProblems()) {
							if (problem instanceof CategorizedProblem categorizedProblem) {
								groupedProblems.computeIfAbsent(categorizedProblem.getMarkerType(), key -> new ArrayList<>()).add(categorizedProblem);
							}
						}
						for (Entry<String, List<CategorizedProblem>> entry : groupedProblems.entrySet()) {
							this.problems.put(entry.getKey(), entry.getValue().toArray(CategorizedProblem[]::new));
						}
						if (this.astLevel != ICompilationUnit.NO_AST) {
							this.ast = newAST;
						}
					} catch (AbortCompilationUnit ex) {
						var problem = ex.problem;
						if (problem == null && ex.exception instanceof IOException ioEx) {
							String path = source.getPath().toString();
							String exceptionTrace = ioEx.getClass().getName() + ':' + ioEx.getMessage();
							problem = new DefaultProblemFactory().createProblem(
									path.toCharArray(),
									IProblem.CannotReadSource,
									new String[] { path, exceptionTrace },
									new String[] { path, exceptionTrace },
									ProblemSeverities.AbortCompilation | ProblemSeverities.Error | ProblemSeverities.Fatal,
									0, 0, 1, 0);
						}
						this.problems.put(Integer.toString(CategorizedProblem.CAT_BUILDPATH),
							new CategorizedProblem[] { problem });
					}
				} else {
					CompilationUnitDeclaration unit = null;
					try {
						unit = CompilationUnitProblemFinder.process(
								source,
								this.workingCopyOwner,
								this.problems,
								this.astLevel != ICompilationUnit.NO_AST/*creating AST if level is not NO_AST */,
								this.reconcileFlags,
								this.progressMonitor);
						if (this.progressMonitor != null) this.progressMonitor.worked(1);

						// create AST if needed
						if (this.astLevel != ICompilationUnit.NO_AST
								&& unit !=null/*unit is null if working copy is consistent && (problem detection not forced || non-Java project) -> don't create AST as per API*/) {
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
						}
					} finally {
						if (unit != null) {
							unit.cleanUp();
						}
					}
				}

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
			JavaModelManager.getJavaModelManager().abortOnMissingSource.remove();
	    }
		return this.ast;
	}

	private void notifyParticipants(final CompilationUnit workingCopy) {
		IJavaProject javaProject = getWorkingCopy().getJavaProject();
		CompilationParticipant[] participants = JavaModelManager.getJavaModelManager().compilationParticipants.getCompilationParticipants(javaProject);
		if (participants == null) return;

		final ReconcileContext context = new ReconcileContext(this, workingCopy);
		for (final CompilationParticipant participant : participants) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					if (exception instanceof Error) {
						throw (Error) exception; // errors are not supposed to be caught
					} else if (exception instanceof OperationCanceledException)
						throw (OperationCanceledException) exception;
					else if (exception instanceof UnsupportedOperationException) {
						// might want to disable participant as it tried to modify the buffer of the working copy being reconciled
						Util.log(exception, "Reconcile participant attempted to modify the buffer of the working copy being reconciled"); //$NON-NLS-1$
					} else
						Util.log(exception, "Exception occurred in reconcile participant"); //$NON-NLS-1$
				}
				@Override
				public void run() throws Exception {
					participant.reconcile(context);
				}
			});
		}
	}

	@Override
	protected IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		CompilationUnit workingCopy = getWorkingCopy();
		if (!workingCopy.isWorkingCopy()) {
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, workingCopy); //was destroyed
		}
		return status;
	}

}
