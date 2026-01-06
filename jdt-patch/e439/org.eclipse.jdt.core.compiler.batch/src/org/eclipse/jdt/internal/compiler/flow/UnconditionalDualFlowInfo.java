/*******************************************************************************
 * Copyright (c) 2025 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

/**
 * A wrapper for two flow infos being co-maintained side-by-side.
 *
 * Dispatch all modifications to both variant infos.
 * Where copying is involved wrap the result in a new DualFlowInfo or
 * UnconditionalDualFlowInfo.
 */
public class UnconditionalDualFlowInfo extends UnconditionalFlowInfo {
/* TODO: may need to implement also the following.
 * Some cannot be simply dispatched because the only exist in UnconditionalFlowInfo.
 *
 * Definite assignment:
 * discardInitializationInfo()
 * discardNonFieldInitializations()
 *
 * Null analysis:
 * acceptAllIncomingNullness()
 * acceptIncomingNullnessFrom(UnconditionalFlowInfo)
 * addPotentialNullInfoFrom(UnconditionalFlowInfo(UnconditionalFlowInfo)
 * cannotBeDefinitelyNullOrNonNull(LocalVariableBinding)
 * cannotBeNull(LocalVariableBinding)
 * canOnlyBeNull(LocalVariableBinding)
 * isDefinitelyNonNull(LocalVariableBinding)
 * isDefinitelyNull(LocalVariableBinding)
 * isDefinitelyUnknown(LocalVariableBinding)
 * hasNullInfoFor(LocalVariableBinding)
 * isPotentiallyNonNull(LocalVariableBinding)
 * isPotentiallyNull(LocalVariableBinding)
 * isPotentiallyUnknown(LocalVariableBinding)
 * isProtectedNonNull(LocalVariableBinding)
 * isProtectedNull(LocalVariableBinding)
 */

	private FlowInfo companionInits;

	public UnconditionalDualFlowInfo(FlowInfo mainInits, FlowInfo companionInits) {
		super.addInitializationsFrom(mainInits);
		super.addNullInfoFrom(mainInits);
		this.tagBits = mainInits.tagBits & UNREACHABLE;
		while (!(mainInits instanceof UnconditionalFlowInfo ufi)) {
			mainInits = mainInits.initsWhenTrue();
		}
		this.maxFieldCount = ufi.maxFieldCount;
		this.companionInits = companionInits;
	}

	public FlowInfo getMainInits() {
		return super.copy();
	}

	@Override
	public FlowInfo addInitializationsFrom(FlowInfo otherInits) {
		super.addInitializationsFrom(otherInits);
		this.companionInits.addInitializationsFrom(otherInits);
		return this;
	}

	@Override
	public FlowInfo addNullInfoFrom(FlowInfo otherInits) {
		super.addNullInfoFrom(otherInits);
		this.companionInits.addNullInfoFrom(otherInits);
		return this;
	}

	@Override
	public FlowInfo addPotentialInitializationsFrom(FlowInfo otherInits) {
		super.addPotentialInitializationsFrom(otherInits);
		this.companionInits.addPotentialInitializationsFrom(otherInits);
		return this;
	}


	@Override
	public FlowInfo asNegatedCondition() {
		this.companionInits.asNegatedCondition();
		return super.asNegatedCondition();
	}

	@Override
	public FlowInfo copy() {
		return new UnconditionalDualFlowInfo(super.copy(), this.companionInits.copy());
	}

	@Override
	public boolean isDefinitelyAssigned(FieldBinding field) {
		return super.isDefinitelyAssigned(field) && this.companionInits.isDefinitelyAssigned(field);
	}

	@Override
	public boolean isDefinitelyAssigned(LocalVariableBinding local) {
		return super.isDefinitelyAssigned(local) && this.companionInits.isDefinitelyAssigned(local);
	}

	@Override
	public boolean isPotentiallyAssigned(FieldBinding field) {
		return super.isPotentiallyAssigned(field) || this.companionInits.isPotentiallyAssigned(field);
	}

	@Override
	public void markAsComparedEqualToNonNull(LocalVariableBinding local) {
		super.markAsComparedEqualToNonNull(local);
		this.companionInits.markAsComparedEqualToNonNull(local);
	}

	@Override
	public void markAsComparedEqualToNull(LocalVariableBinding local) {
		super.markAsComparedEqualToNull(local);
		this.companionInits.markAsComparedEqualToNull(local);
	}

	@Override
	public void markAsDefinitelyAssigned(FieldBinding field) {
		super.markAsDefinitelyAssigned(field);
		this.companionInits.markAsDefinitelyAssigned(field);
	}

	@Override
	public void markAsDefinitelyAssigned(LocalVariableBinding local) {
		super.markAsDefinitelyAssigned(local);
		this.companionInits.markAsDefinitelyAssigned(local);
	}

	@Override
	public void markAsDefinitelyNonNull(LocalVariableBinding local) {
		super.markAsDefinitelyNonNull(local);
		this.companionInits.markAsDefinitelyNonNull(local);
	}

	@Override
	public void markAsDefinitelyNull(LocalVariableBinding local) {
		super.markAsDefinitelyNull(local);
		this.companionInits.markAsDefinitelyNull(local);
	}

	@Override
	public void markAsDefinitelyUnknown(LocalVariableBinding local) {
		super.markAsDefinitelyUnknown(local);
		this.companionInits.markAsDefinitelyUnknown(local);
	}

	@Override
	public void markPotentiallyUnknownBit(LocalVariableBinding local) {
		super.markPotentiallyUnknownBit(local);
		this.companionInits.markPotentiallyUnknownBit(local);
	}

	@Override
	public void markPotentiallyNullBit(LocalVariableBinding local) {
		super.markPotentiallyNullBit(local);
		this.companionInits.markPotentiallyNullBit(local);
	}

	@Override
	public void markPotentiallyNonNullBit(LocalVariableBinding local) {
		super.markPotentiallyNonNullBit(local);
		this.companionInits.markPotentiallyNonNullBit(local);
	}

	@Override
	public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits) {
		return new UnconditionalDualFlowInfo(super.mergedWith(otherInits), this.companionInits.mergedWith(otherInits));
	}

	@Override
	public UnconditionalFlowInfo mergeDefiniteInitsWith(UnconditionalFlowInfo otherInits) {
		return new UnconditionalDualFlowInfo(super.mergeDefiniteInitsWith(otherInits), this.companionInits.mergeDefiniteInitsWith(otherInits));
	}

	@Override
	public UnconditionalFlowInfo nullInfoLessUnconditionalCopy() {
		return new UnconditionalDualFlowInfo(super.nullInfoLessUnconditionalCopy(), this.companionInits.nullInfoLessUnconditionalCopy());
	}

	@Override
	public void resetNullInfo(LocalVariableBinding local) {
		super.resetNullInfo(local);
		this.companionInits.resetNullInfo(local);
	}

	@Override
	public void resetAssignmentInfo(LocalVariableBinding local) {
		super.resetAssignmentInfo(local);
		this.companionInits.resetAssignmentInfo(local);
	}

	@Override
	public FlowInfo setReachMode(int reachMode) {
		return new DualFlowInfo(super.setReachMode(reachMode), this.companionInits.setReachMode(reachMode));
	}

	@Override
	public UnconditionalFlowInfo unconditionalCopy() {
		return new UnconditionalDualFlowInfo(super.unconditionalCopy(), this.companionInits.unconditionalCopy());
	}

	@Override
	public UnconditionalFlowInfo unconditionalFieldLessCopy() {
		return new UnconditionalDualFlowInfo(super.unconditionalFieldLessCopy(), this.companionInits.unconditionalFieldLessCopy());
	}
}
