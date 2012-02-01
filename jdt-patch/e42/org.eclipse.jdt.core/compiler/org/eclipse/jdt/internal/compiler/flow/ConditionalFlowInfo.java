/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for bug 332637 - Dead Code detection removing code that isn't dead
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Record conditional initialization status during definite assignment analysis
 *
 */
public class ConditionalFlowInfo extends FlowInfo {

	public FlowInfo initsWhenTrue;
	public FlowInfo initsWhenFalse;

ConditionalFlowInfo(FlowInfo initsWhenTrue, FlowInfo initsWhenFalse){

	this.initsWhenTrue = initsWhenTrue;
	this.initsWhenFalse = initsWhenFalse;
}

public FlowInfo addInitializationsFrom(FlowInfo otherInits) {

	this.initsWhenTrue.addInitializationsFrom(otherInits);
	this.initsWhenFalse.addInitializationsFrom(otherInits);
	return this;
}

public FlowInfo addNullInfoFrom(FlowInfo otherInits) {

	this.initsWhenTrue.addNullInfoFrom(otherInits);
	this.initsWhenFalse.addNullInfoFrom(otherInits);
	return this;
}

public FlowInfo addPotentialInitializationsFrom(FlowInfo otherInits) {

	this.initsWhenTrue.addPotentialInitializationsFrom(otherInits);
	this.initsWhenFalse.addPotentialInitializationsFrom(otherInits);
	return this;
}

public FlowInfo asNegatedCondition() {

	FlowInfo extra = this.initsWhenTrue;
	this.initsWhenTrue = this.initsWhenFalse;
	this.initsWhenFalse = extra;
	return this;
}

public FlowInfo copy() {

	return new ConditionalFlowInfo(this.initsWhenTrue.copy(), this.initsWhenFalse.copy());
}

public FlowInfo initsWhenFalse() {

	return this.initsWhenFalse;
}

public FlowInfo initsWhenTrue() {

	return this.initsWhenTrue;
}

public boolean isDefinitelyAssigned(VariableBinding var) {

	return this.initsWhenTrue.isDefinitelyAssigned(var)
			&& this.initsWhenFalse.isDefinitelyAssigned(var);
}

public boolean isDefinitelyNonNull(VariableBinding var) {
	return this.initsWhenTrue.isDefinitelyNonNull(var)
			&& this.initsWhenFalse.isDefinitelyNonNull(var);
}

public boolean isDefinitelyNull(VariableBinding var) {
	return this.initsWhenTrue.isDefinitelyNull(var)
			&& this.initsWhenFalse.isDefinitelyNull(var);
}

public boolean isDefinitelyUnknown(VariableBinding var) {
	return this.initsWhenTrue.isDefinitelyUnknown(var)
			&& this.initsWhenFalse.isDefinitelyUnknown(var);
}

public boolean isPotentiallyAssigned(VariableBinding var) {
	return this.initsWhenTrue.isPotentiallyAssigned(var)
			|| this.initsWhenFalse.isPotentiallyAssigned(var);
}

public boolean isPotentiallyNonNull(VariableBinding var) {
	return this.initsWhenTrue.isPotentiallyNonNull(var)
		|| this.initsWhenFalse.isPotentiallyNonNull(var);
}

public boolean isPotentiallyNull(VariableBinding var) {
	return this.initsWhenTrue.isPotentiallyNull(var)
		|| this.initsWhenFalse.isPotentiallyNull(var);
}

public boolean isPotentiallyUnknown(VariableBinding var) {
	return this.initsWhenTrue.isPotentiallyUnknown(var)
		|| this.initsWhenFalse.isPotentiallyUnknown(var);
}

public boolean isProtectedNonNull(VariableBinding var) {
	return this.initsWhenTrue.isProtectedNonNull(var)
		&& this.initsWhenFalse.isProtectedNonNull(var);
}

public boolean isProtectedNull(VariableBinding var) {
	return this.initsWhenTrue.isProtectedNull(var)
		&& this.initsWhenFalse.isProtectedNull(var);
}

public void markAsComparedEqualToNonNull(VariableBinding var) {
	this.initsWhenTrue.markAsComparedEqualToNonNull(var);
	this.initsWhenFalse.markAsComparedEqualToNonNull(var);
}

public void markAsComparedEqualToNull(VariableBinding var) {
	this.initsWhenTrue.markAsComparedEqualToNull(var);
    this.initsWhenFalse.markAsComparedEqualToNull(var);
}

public void markAsDefinitelyAssigned(VariableBinding var) {
	this.initsWhenTrue.markAsDefinitelyAssigned(var);
	this.initsWhenFalse.markAsDefinitelyAssigned(var);
}

public void markAsDefinitelyNonNull(VariableBinding var) {
	this.initsWhenTrue.markAsDefinitelyNonNull(var);
	this.initsWhenFalse.markAsDefinitelyNonNull(var);
}

public void markAsDefinitelyNull(VariableBinding var) {
	this.initsWhenTrue.markAsDefinitelyNull(var);
	this.initsWhenFalse.markAsDefinitelyNull(var);
}

public void resetNullInfo(VariableBinding var) {
	this.initsWhenTrue.resetNullInfo(var);
	this.initsWhenFalse.resetNullInfo(var);
}

public void resetNullInfoForFields() {
	this.initsWhenTrue.resetNullInfoForFields();
	this.initsWhenFalse.resetNullInfoForFields();
}

public void updateConstantFieldsMask(FieldBinding field) {
	this.initsWhenTrue.updateConstantFieldsMask(field);
	this.initsWhenFalse.updateConstantFieldsMask(field);
}

public void addConstantFieldsMask(UnconditionalFlowInfo other) {
	this.initsWhenTrue.addConstantFieldsMask(other);
	this.initsWhenFalse.addConstantFieldsMask(other);
}

public void markPotentiallyNullBit(VariableBinding var) {
	this.initsWhenTrue.markPotentiallyNullBit(var);
	this.initsWhenFalse.markPotentiallyNullBit(var);
}

public void markPotentiallyNonNullBit(VariableBinding var) {
	this.initsWhenTrue.markPotentiallyNonNullBit(var);
	this.initsWhenFalse.markPotentiallyNonNullBit(var);
}

public void markAsDefinitelyUnknown(VariableBinding var) {
	this.initsWhenTrue.markAsDefinitelyUnknown(var);
	this.initsWhenFalse.markAsDefinitelyUnknown(var);
}

public void markPotentiallyUnknownBit(VariableBinding var) {
	this.initsWhenTrue.markPotentiallyUnknownBit(var);
	this.initsWhenFalse.markPotentiallyUnknownBit(var);
}

public FlowInfo setReachMode(int reachMode) {
	if (reachMode == REACHABLE) {
		this.tagBits &= ~UNREACHABLE;
	}
	else {
		this.tagBits |= reachMode;
	}
	this.initsWhenTrue.setReachMode(reachMode);
	this.initsWhenFalse.setReachMode(reachMode);
	return this;
}

public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits) {
	return unconditionalInits().mergedWith(otherInits);
}

public UnconditionalFlowInfo nullInfoLessUnconditionalCopy() {
	return unconditionalInitsWithoutSideEffect().
		nullInfoLessUnconditionalCopy();
}

public String toString() {

	return "FlowInfo<true: " + this.initsWhenTrue.toString() + ", false: " + this.initsWhenFalse.toString() + ">"; //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$
}

public FlowInfo safeInitsWhenTrue() {
	return this.initsWhenTrue;
}

public UnconditionalFlowInfo unconditionalCopy() {
	return this.initsWhenTrue.unconditionalCopy().
			mergedWith(this.initsWhenFalse.unconditionalInits());
}

public UnconditionalFlowInfo unconditionalFieldLessCopy() {
	return this.initsWhenTrue.unconditionalFieldLessCopy().
		mergedWith(this.initsWhenFalse.unconditionalFieldLessCopy());
	// should never happen, hence suboptimal does not hurt
}

public UnconditionalFlowInfo unconditionalInits() {
	return this.initsWhenTrue.unconditionalInits().
			mergedWith(this.initsWhenFalse.unconditionalInits());
}

public UnconditionalFlowInfo unconditionalInitsWithoutSideEffect() {
	// cannot do better here than unconditionalCopy - but still a different
	// operation for UnconditionalFlowInfo
	return this.initsWhenTrue.unconditionalCopy().
			mergedWith(this.initsWhenFalse.unconditionalInits());
}

public void markedAsNullOrNonNullInAssertExpression(VariableBinding var) {
	this.initsWhenTrue.markedAsNullOrNonNullInAssertExpression(var);
	this.initsWhenFalse.markedAsNullOrNonNullInAssertExpression(var);
}

public boolean isMarkedAsNullOrNonNullInAssertExpression(VariableBinding var) {
	return (this.initsWhenTrue.isMarkedAsNullOrNonNullInAssertExpression(var)
		|| this.initsWhenFalse.isMarkedAsNullOrNonNullInAssertExpression(var));
}

public void resetAssignmentInfo(LocalVariableBinding var) {
	this.initsWhenTrue.resetAssignmentInfo(var);
	this.initsWhenFalse.resetAssignmentInfo(var);
}
}
