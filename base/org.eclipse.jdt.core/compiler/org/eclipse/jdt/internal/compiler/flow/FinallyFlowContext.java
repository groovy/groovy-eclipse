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
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class FinallyFlowContext extends FlowContext {
	
	Reference[] finalAssignments;
	VariableBinding[] finalVariables;
	int assignCount;

	LocalVariableBinding[] nullLocals;	
	Expression[] nullReferences;
	int[] nullCheckTypes;
	int nullCount;
	
	public FinallyFlowContext(FlowContext parent, ASTNode associatedNode) {
		super(parent, associatedNode);
	}

/**
 * Given some contextual initialization info (derived from a try block or a catch block), this 
 * code will check that the subroutine context does not also initialize a final variable potentially set
 * redundantly.
 */
public void complainOnDeferredChecks(FlowInfo flowInfo, BlockScope scope) {
	
	// check redundant final assignments
	for (int i = 0; i < this.assignCount; i++) {
		VariableBinding variable = this.finalVariables[i];
		if (variable == null) continue;
		
		boolean complained = false; // remember if have complained on this final assignment
		if (variable instanceof FieldBinding) {
			// final field
			if (flowInfo.isPotentiallyAssigned((FieldBinding)variable)) {
				complained = true;
				scope.problemReporter().duplicateInitializationOfBlankFinalField((FieldBinding)variable, finalAssignments[i]);
			}
		} else {
			// final local variable
			if (flowInfo.isPotentiallyAssigned((LocalVariableBinding) variable)) {
				complained = true;
				scope.problemReporter().duplicateInitializationOfFinalLocal(
					(LocalVariableBinding) variable,
					this.finalAssignments[i]);
			}
		}
		// any reference reported at this level is removed from the parent context 
		// where it could also be reported again
		if (complained) {
			FlowContext currentContext = this.parent;
			while (currentContext != null) {
				//if (currentContext.isSubRoutine()) {
				currentContext.removeFinalAssignmentIfAny(this.finalAssignments[i]);
				//}
				currentContext = currentContext.parent;
			}
		}
	}
	
	// check inconsistent null checks
	if (this.deferNullDiagnostic) { // within an enclosing loop, be conservative
		for (int i = 0; i < this.nullCount; i++) {
			this.parent.recordUsingNullReference(scope, this.nullLocals[i], 
					this.nullReferences[i],	this.nullCheckTypes[i], flowInfo);
		}
	}
	else { // no enclosing loop, be as precise as possible right now
		for (int i = 0; i < this.nullCount; i++) {
			Expression expression = this.nullReferences[i];
			// final local variable
			LocalVariableBinding local = this.nullLocals[i];
			switch (this.nullCheckTypes[i]) {
				case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NULL:
				case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL:
					if (flowInfo.isDefinitelyNonNull(local)) {
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
						scope.problemReporter().localVariableNullReference(local, expression);
						continue;
					}
					if (flowInfo.isPotentiallyNull(local)) {
						scope.problemReporter().localVariablePotentialNullReference(local, expression);
					}
					break;
				default:
					// should not happen
			}
		}
	}
}
	
	public String individualToString() {
		
		StringBuffer buffer = new StringBuffer("Finally flow context"); //$NON-NLS-1$
		buffer.append("[finalAssignments count - ").append(assignCount).append(']'); //$NON-NLS-1$
		buffer.append("[nullReferences count - ").append(nullCount).append(']'); //$NON-NLS-1$
		return buffer.toString();
	}
	
	public boolean isSubRoutine() {
		return true;
	}
	
	protected boolean recordFinalAssignment(
		VariableBinding binding,
		Reference finalAssignment) {
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

	public void recordUsingNullReference(Scope scope, LocalVariableBinding local, 
			Expression reference, int checkType, FlowInfo flowInfo) {
		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0 && !flowInfo.isDefinitelyUnknown(local))	{
			if (deferNullDiagnostic) { // within an enclosing loop, be conservative
				switch (checkType) {
					case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NULL:
					case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL:
					case CAN_ONLY_NULL | IN_COMPARISON_NULL:
					case CAN_ONLY_NULL | IN_COMPARISON_NON_NULL:
					case CAN_ONLY_NULL | IN_ASSIGNMENT:
					case CAN_ONLY_NULL | IN_INSTANCEOF:
						if (flowInfo.cannotBeNull(local)) {
							if (checkType == (CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL)) {
								scope.problemReporter().localVariableRedundantCheckOnNonNull(local, reference);
							} else {
								scope.problemReporter().localVariableNonNullComparedToNull(local, reference);
							}
							return;
						}
						if (flowInfo.canOnlyBeNull(local)) {
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
						break;
					case MAY_NULL :
						if (flowInfo.cannotBeNull(local)) {
							return;
						}
						if (flowInfo.canOnlyBeNull(local)) {
							scope.problemReporter().localVariableNullReference(local, reference);
							return;
						}
						break;
					default:
						// never happens
				}
			}
			else { // no enclosing loop, be as precise as possible right now
				switch (checkType) {
					case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NULL:
					case CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL:
						if (flowInfo.isDefinitelyNonNull(local)) {
							if (checkType == (CAN_ONLY_NULL_NON_NULL | IN_COMPARISON_NON_NULL)) {
								scope.problemReporter().localVariableRedundantCheckOnNonNull(local, reference);
							} else {
								scope.problemReporter().localVariableNonNullComparedToNull(local, reference);
							}
							return;
						}
					case CAN_ONLY_NULL | IN_COMPARISON_NULL:
					case CAN_ONLY_NULL | IN_COMPARISON_NON_NULL:
					case CAN_ONLY_NULL | IN_ASSIGNMENT:
					case CAN_ONLY_NULL | IN_INSTANCEOF:
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
						break;
					case MAY_NULL :
						if (flowInfo.isDefinitelyNull(local)) {
							scope.problemReporter().localVariableNullReference(local, reference);
							return;
						}
						if (flowInfo.isPotentiallyNull(local)) {
							scope.problemReporter().localVariablePotentialNullReference(local, reference);
							return;
						}
						if (flowInfo.isDefinitelyNonNull(local)) {
							return; // shortcut: cannot be null
						}
						break;
					default:
						// never happens
				}
			}
			recordNullReference(local, reference, checkType); 
			// prepare to re-check with try/catch flow info
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

protected void recordNullReference(LocalVariableBinding local, 
	Expression expression, int status) {
	if (this.nullCount == 0) {
		this.nullLocals = new LocalVariableBinding[5];
		this.nullReferences = new Expression[5];
		this.nullCheckTypes = new int[5];
	} 
	else if (this.nullCount == this.nullLocals.length) {
		int newLength = this.nullCount * 2;
		System.arraycopy(this.nullLocals, 0, 
			this.nullLocals = new LocalVariableBinding[newLength], 0, 
			this.nullCount);
		System.arraycopy(this.nullReferences, 0, 
			this.nullReferences = new Expression[newLength], 0,
			this.nullCount);
		System.arraycopy(this.nullCheckTypes, 0, 
			this.nullCheckTypes = new int[newLength], 0, 
			this.nullCount);
	}
	this.nullLocals[this.nullCount] = local;
	this.nullReferences[this.nullCount] = expression;
	this.nullCheckTypes[this.nullCount++] = status;
}
}
