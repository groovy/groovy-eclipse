/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class LoopingFlowContext extends SwitchFlowContext {
	
	public BranchLabel continueLabel;
	public UnconditionalFlowInfo initsOnContinue = FlowInfo.DEAD_END;
	private UnconditionalFlowInfo upstreamNullFlowInfo;
	private LoopingFlowContext innerFlowContexts[] = null;
	private UnconditionalFlowInfo innerFlowInfos[] = null;
	private int innerFlowContextsCount = 0;
	private LabelFlowContext breakTargetContexts[] = null;
	private int breakTargetsCount = 0;
	
	Reference finalAssignments[];
	VariableBinding finalVariables[];
	int assignCount = 0;
	
	LocalVariableBinding[] nullLocals;
	Expression[] nullReferences;
	int[] nullCheckTypes;
	int nullCount;
	
	Scope associatedScope;
	
	public LoopingFlowContext(
		FlowContext parent,
		FlowInfo upstreamNullFlowInfo,
		ASTNode associatedNode,
		BranchLabel breakLabel,
		BranchLabel continueLabel,
		Scope associatedScope) {
		super(parent, associatedNode, breakLabel);
		preemptNullDiagnostic = true; 
			// children will defer to this, which may defer to its own parent 
		this.continueLabel = continueLabel;
		this.associatedScope = associatedScope;
		this.upstreamNullFlowInfo = upstreamNullFlowInfo.unconditionalCopy();
	}

/**
 * Perform deferred checks relative to final variables duplicate initialization 
 * of lack of initialization.
 * @param scope the scope to which this context is associated
 * @param flowInfo the flow info against which checks must be performed
 */
public void complainOnDeferredFinalChecks(BlockScope scope, FlowInfo flowInfo) {
	// complain on final assignments in loops
	for (int i = 0; i < assignCount; i++) {
		VariableBinding variable = finalVariables[i];
		if (variable == null) continue;
		boolean complained = false; // remember if have complained on this final assignment
		if (variable instanceof FieldBinding) {
			if (flowInfo.isPotentiallyAssigned((FieldBinding) variable)) {
				complained = true;
				scope.problemReporter().duplicateInitializationOfBlankFinalField(
					(FieldBinding) variable,
					finalAssignments[i]);
			}
		} else {
			if (flowInfo.isPotentiallyAssigned((LocalVariableBinding) variable)) {
				complained = true;
				scope.problemReporter().duplicateInitializationOfFinalLocal(
					(LocalVariableBinding) variable,
					finalAssignments[i]);
			}
		}
		// any reference reported at this level is removed from the parent context where it 
		// could also be reported again
		if (complained) {
			FlowContext context = parent;
			while (context != null) {
				context.removeFinalAssignmentIfAny(finalAssignments[i]);
				context = context.parent;
			}
		}
	}
}

/**
 * Perform deferred checks relative to the null status of local variables.
 * @param scope the scope to which this context is associated
 * @param callerFlowInfo the flow info against which checks must be performed
 */
public void complainOnDeferredNullChecks(BlockScope scope, FlowInfo callerFlowInfo) {
	for (int i = 0 ; i < this.innerFlowContextsCount ; i++) {
		this.upstreamNullFlowInfo.
			addPotentialNullInfoFrom(
				this.innerFlowContexts[i].upstreamNullFlowInfo).
			addPotentialNullInfoFrom(this.innerFlowInfos[i]);
	}
	this.innerFlowContextsCount = 0;
	UnconditionalFlowInfo flowInfo = this.upstreamNullFlowInfo.
		addPotentialNullInfoFrom(callerFlowInfo.unconditionalInitsWithoutSideEffect());
	if (this.deferNullDiagnostic) {
		// check only immutable null checks on innermost looping context
		for (int i = 0; i < this.nullCount; i++) {
			LocalVariableBinding local = this.nullLocals[i];
			Expression expression = this.nullReferences[i];
			// final local variable
			switch (this.nullCheckTypes[i]) {
				case CAN_ONLY_NON_NULL | IN_COMPARISON_NULL:
				case CAN_ONLY_NON_NULL | IN_COMPARISON_NON_NULL:
					if (flowInfo.isDefinitelyNonNull(local)) {
						this.nullReferences[i] = null;
						if (this.nullCheckTypes[i] == (CAN_ONLY_NON_NULL | IN_COMPARISON_NON_NULL)) {
							scope.problemReporter().localVariableRedundantCheckOnNonNull(local, expression);
						} else {
							scope.problemReporter().localVariableNonNullComparedToNull(local, expression);
						}
						continue;
					}
					break;
				case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NULL:
				case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL:
					if (flowInfo.isDefinitelyNonNull(local)) {
						this.nullReferences[i] = null;
						if (this.nullCheckTypes[i] == (CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL)) {
							scope.problemReporter().localVariableRedundantCheckOnNonNull(local, expression);
						} else {
							scope.problemReporter().localVariableNonNullComparedToNull(local, expression);
						}
						continue;
					}
					if (flowInfo.isDefinitelyNull(local)) {
						this.nullReferences[i] = null;
						if (this.nullCheckTypes[i] == (CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NULL)) {
							scope.problemReporter().localVariableRedundantCheckOnNull(local, expression);
						} else {
							scope.problemReporter().localVariableNullComparedToNonNull(local, expression);
						}
						continue;
					}
					break;
				case CAN_ONLY_NULL | IN_COMPARISON_NULL:
				case CAN_ONLY_NULL | IN_COMPARISON_NON_NULL:
				case CAN_ONLY_NULL | IN_ASSIGNMENT:
				case CAN_ONLY_NULL | IN_INSTANCEOF:
					if (flowInfo.isDefinitelyNull(local)) {
						this.nullReferences[i] = null;
						switch(this.nullCheckTypes[i] & CONTEXT_MASK) {
							case FlowContext.IN_COMPARISON_NULL:
								scope.problemReporter().localVariableRedundantCheckOnNull(local, expression);
								continue;
							case FlowContext.IN_COMPARISON_NON_NULL:
								scope.problemReporter().localVariableNullComparedToNonNull(local, expression);
								continue;
							case FlowContext.IN_ASSIGNMENT:
								scope.problemReporter().localVariableRedundantNullAssignment(local, expression);
								continue;
							case FlowContext.IN_INSTANCEOF:
								scope.problemReporter().localVariableNullInstanceof(local, expression);
								continue;
						}
					}
					break;
				case MAY_NULL:
					if (flowInfo.isDefinitelyNull(local)) {
						this.nullReferences[i] = null;
						scope.problemReporter().localVariableNullReference(local, expression);
						continue;
					}
					break;
				default:
					// never happens	
			}
			this.parent.recordUsingNullReference(scope, local, expression, 
					this.nullCheckTypes[i], flowInfo);
		}
	}
	else {
		// check inconsistent null checks on outermost looping context
		for (int i = 0; i < this.nullCount; i++) {
			Expression expression = this.nullReferences[i];
			// final local variable
			LocalVariableBinding local = this.nullLocals[i];
			switch (this.nullCheckTypes[i]) {
				case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NULL:
				case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL:
					if (flowInfo.isDefinitelyNonNull(local)) {
						this.nullReferences[i] = null;
						if (this.nullCheckTypes[i] == (CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL)) {
							scope.problemReporter().localVariableRedundantCheckOnNonNull(local, expression);
						} else {
							scope.problemReporter().localVariableNonNullComparedToNull(local, expression);
						}
						continue;
					}
				case CAN_ONLY_NULL | IN_COMPARISON_NULL:
				case CAN_ONLY_NULL | IN_COMPARISON_NON_NULL:
				case CAN_ONLY_NULL | IN_ASSIGNMENT:
				case CAN_ONLY_NULL | IN_INSTANCEOF:
					if (flowInfo.isDefinitelyNull(local)) {
						this.nullReferences[i] = null;
						switch(this.nullCheckTypes[i] & CONTEXT_MASK) {
							case FlowContext.IN_COMPARISON_NULL:
								scope.problemReporter().localVariableRedundantCheckOnNull(local, expression);
								continue;
							case FlowContext.IN_COMPARISON_NON_NULL:
								scope.problemReporter().localVariableNullComparedToNonNull(local, expression);
								continue;
							case FlowContext.IN_ASSIGNMENT:
								scope.problemReporter().localVariableRedundantNullAssignment(local, expression);
								continue;
							case FlowContext.IN_INSTANCEOF:
								scope.problemReporter().localVariableNullInstanceof(local, expression);
								continue;
						}
					}
					break;
				case MAY_NULL:
					if (flowInfo.isDefinitelyNull(local)) {
						this.nullReferences[i] = null;
						scope.problemReporter().localVariableNullReference(local, expression);
						continue;
					}
					if (flowInfo.isPotentiallyNull(local)) {
						this.nullReferences[i] = null;
						scope.problemReporter().localVariablePotentialNullReference(local, expression);
						continue;
					}
					break;
				default:
					// never happens	
			}
		}
	}
	// propagate breaks
	this.initsOnBreak.addPotentialNullInfoFrom(flowInfo);
	for (int i = 0; i < this.breakTargetsCount; i++) {
		this.breakTargetContexts[i].initsOnBreak.addPotentialNullInfoFrom(flowInfo);
	}
}
	
	public BranchLabel continueLabel() {
		return continueLabel;
	}

	public String individualToString() {
		StringBuffer buffer = new StringBuffer("Looping flow context"); //$NON-NLS-1$
		buffer.append("[initsOnBreak - ").append(initsOnBreak.toString()).append(']'); //$NON-NLS-1$
		buffer.append("[initsOnContinue - ").append(initsOnContinue.toString()).append(']'); //$NON-NLS-1$
		buffer.append("[finalAssignments count - ").append(assignCount).append(']'); //$NON-NLS-1$
		buffer.append("[nullReferences count - ").append(nullCount).append(']'); //$NON-NLS-1$
		return buffer.toString();
	}

	public boolean isContinuable() {
		return true;
	}

	public boolean isContinuedTo() {
		return initsOnContinue != FlowInfo.DEAD_END;
	}

public void recordBreakTo(FlowContext targetContext) {
	if (targetContext instanceof LabelFlowContext) {
		int current;
		if ((current = this.breakTargetsCount++) == 0) {
			this.breakTargetContexts = new LabelFlowContext[2];
		} else if (current == this.breakTargetContexts.length) {
			System.arraycopy(this.breakTargetContexts, 0, this.breakTargetContexts = new LabelFlowContext[current + 2], 0, current);
		}
		this.breakTargetContexts[current] = (LabelFlowContext) targetContext;
	}
}

public void recordContinueFrom(FlowContext innerFlowContext, FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
	if ((initsOnContinue.tagBits & FlowInfo.UNREACHABLE) == 0) {
		initsOnContinue = initsOnContinue.
			mergedWith(flowInfo.unconditionalInitsWithoutSideEffect());
	} 
	else {
		initsOnContinue = flowInfo.unconditionalCopy();
	}
	FlowContext inner = innerFlowContext;
	while (inner != this && !(inner instanceof LoopingFlowContext)) {
		inner = inner.parent;
	}
	if (inner == this) {
		this.upstreamNullFlowInfo.
			addPotentialNullInfoFrom(
				flowInfo.unconditionalInitsWithoutSideEffect());
	}
	else {
		int length = 0;
		if (this.innerFlowContexts == null) {
			this.innerFlowContexts = new LoopingFlowContext[5];
			this.innerFlowInfos = new UnconditionalFlowInfo[5];
		}
		else if (this.innerFlowContextsCount == 
				(length = this.innerFlowContexts.length) - 1) {
			System.arraycopy(this.innerFlowContexts, 0, 
				(this.innerFlowContexts = new LoopingFlowContext[length + 5]), 
				0, length);
			System.arraycopy(this.innerFlowInfos, 0, 
				(this.innerFlowInfos= new UnconditionalFlowInfo[length + 5]), 
				0, length);
		}
		this.innerFlowContexts[this.innerFlowContextsCount] = (LoopingFlowContext) inner;
		this.innerFlowInfos[this.innerFlowContextsCount++] = 
			flowInfo.unconditionalInitsWithoutSideEffect();
	}
	}
}

	protected boolean recordFinalAssignment(
		VariableBinding binding,
		Reference finalAssignment) {

		// do not consider variables which are defined inside this loop
		if (binding instanceof LocalVariableBinding) {
			Scope scope = ((LocalVariableBinding) binding).declaringScope;
			while ((scope = scope.parent) != null) {
				if (scope == associatedScope)
					return false;
			}
		}
		if (assignCount == 0) {
			finalAssignments = new Reference[5];
			finalVariables = new VariableBinding[5];
		} else {
			if (assignCount == finalAssignments.length)
				System.arraycopy(
					finalAssignments,
					0,
					(finalAssignments = new Reference[assignCount * 2]),
					0,
					assignCount);
			System.arraycopy(
				finalVariables,
				0,
				(finalVariables = new VariableBinding[assignCount * 2]),
				0,
				assignCount);
		}
		finalAssignments[assignCount] = finalAssignment;
		finalVariables[assignCount++] = binding;
		return true;
	}

protected void recordNullReference(LocalVariableBinding local, 
	Expression expression, int status) {
	if (nullCount == 0) {
		nullLocals = new LocalVariableBinding[5];
		nullReferences = new Expression[5];
		nullCheckTypes = new int[5];
	} 
	else if (nullCount == nullLocals.length) {
		System.arraycopy(nullLocals, 0, 
			nullLocals = new LocalVariableBinding[nullCount * 2], 0, nullCount);
		System.arraycopy(nullReferences, 0, 
			nullReferences = new Expression[nullCount * 2], 0, nullCount);
		System.arraycopy(nullCheckTypes, 0, 
			nullCheckTypes = new int[nullCount * 2], 0, nullCount);
	}
	nullLocals[nullCount] = local;
	nullReferences[nullCount] = expression;
	nullCheckTypes[nullCount++] = status;
}
	
public void recordUsingNullReference(Scope scope, LocalVariableBinding local,
		Expression reference, int checkType, FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0 || 
			flowInfo.isDefinitelyUnknown(local)) {
		return;
	}
	switch (checkType) {
		case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NULL:
		case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL:
			if (flowInfo.isDefinitelyNonNull(local)) {
				if (checkType == (CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL)) {
					scope.problemReporter().localVariableRedundantCheckOnNonNull(local, reference);
				} else {
					scope.problemReporter().localVariableNonNullComparedToNull(local, reference);
				}
			} else if (flowInfo.isDefinitelyNull(local)) {
				if (checkType == (CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NULL)) {
					scope.problemReporter().localVariableRedundantCheckOnNull(local, reference);
				} else {
					scope.problemReporter().localVariableNullComparedToNonNull(local, reference);
				}
			} else if (! flowInfo.cannotBeDefinitelyNullOrNonNull(local)) {
				if (flowInfo.isPotentiallyNonNull(local)) {
					recordNullReference(local, reference, CAN_ONLY_NON_NULL | checkType & CONTEXT_MASK);
				} else {
					recordNullReference(local, reference, checkType);
				}
			}
			return;
		case CAN_ONLY_NULL | IN_COMPARISON_NULL:
		case CAN_ONLY_NULL | IN_COMPARISON_NON_NULL:
		case CAN_ONLY_NULL | IN_ASSIGNMENT:
		case CAN_ONLY_NULL | IN_INSTANCEOF:
			if (flowInfo.isPotentiallyNonNull(local)
					|| flowInfo.isPotentiallyUnknown(local)) {
				return;
			}
			if (flowInfo.isDefinitelyNull(local)) {
				switch(checkType & CONTEXT_MASK) {
					case FlowContext.IN_COMPARISON_NULL:
						scope.problemReporter().localVariableRedundantCheckOnNull(local, reference);
						return;
					case FlowContext.IN_COMPARISON_NON_NULL:
						scope.problemReporter().localVariableNullComparedToNonNull(local, reference);
						return;
					case FlowContext.IN_ASSIGNMENT:
						scope.problemReporter().localVariableRedundantNullAssignment(local, reference);
						return;
					case FlowContext.IN_INSTANCEOF:
						scope.problemReporter().localVariableNullInstanceof(local, reference);
						return;
				}
			}
			recordNullReference(local, reference, checkType);
			return;
		case MAY_NULL :
			if (flowInfo.isDefinitelyNonNull(local)) {
				return;
			}
			if (flowInfo.isDefinitelyNull(local)) {
				scope.problemReporter().localVariableNullReference(local, reference);
				return;
			}
			if (flowInfo.isPotentiallyNull(local)) {
				scope.problemReporter().localVariablePotentialNullReference(local, reference);
				return;
			}
			recordNullReference(local, reference, checkType);
			return;
		default:
			// never happens
	}
}
	
	void removeFinalAssignmentIfAny(Reference reference) {
		for (int i = 0; i < assignCount; i++) {
			if (finalAssignments[i] == reference) {
				finalAssignments[i] = null;
				finalVariables[i] = null;
				return;
			}
		}
	}
}
