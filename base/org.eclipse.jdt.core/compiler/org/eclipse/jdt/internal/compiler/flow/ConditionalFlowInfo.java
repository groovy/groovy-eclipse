/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

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

public FlowInfo addPotentialInitializationsFrom(FlowInfo otherInits) {
	
	this.initsWhenTrue.addPotentialInitializationsFrom(otherInits);
	this.initsWhenFalse.addPotentialInitializationsFrom(otherInits);
	return this;
}

public FlowInfo asNegatedCondition() {
	
	FlowInfo extra = initsWhenTrue;
	initsWhenTrue = initsWhenFalse;
	initsWhenFalse = extra;
	return this;
}

public FlowInfo copy() {
	
	return new ConditionalFlowInfo(initsWhenTrue.copy(), initsWhenFalse.copy());
}

public FlowInfo initsWhenFalse() {
	
	return initsWhenFalse;
}

public FlowInfo initsWhenTrue() {
	
	return initsWhenTrue;
}
	
public boolean isDefinitelyAssigned(FieldBinding field) {
	
	return initsWhenTrue.isDefinitelyAssigned(field) 
			&& initsWhenFalse.isDefinitelyAssigned(field);
}

public boolean isDefinitelyAssigned(LocalVariableBinding local) {
	
	return initsWhenTrue.isDefinitelyAssigned(local) 
			&& initsWhenFalse.isDefinitelyAssigned(local);
}
	
public boolean isDefinitelyNonNull(LocalVariableBinding local) {
	return initsWhenTrue.isDefinitelyNonNull(local) 
			&& initsWhenFalse.isDefinitelyNonNull(local);
}
	
public boolean isDefinitelyNull(LocalVariableBinding local) {
	return initsWhenTrue.isDefinitelyNull(local) 
			&& initsWhenFalse.isDefinitelyNull(local);
}

public boolean isDefinitelyUnknown(LocalVariableBinding local) {
	return initsWhenTrue.isDefinitelyUnknown(local) 
			&& initsWhenFalse.isDefinitelyUnknown(local);
}
	
public boolean isPotentiallyAssigned(FieldBinding field) {
	return initsWhenTrue.isPotentiallyAssigned(field) 
			|| initsWhenFalse.isPotentiallyAssigned(field);
}

public boolean isPotentiallyAssigned(LocalVariableBinding local) {
	return initsWhenTrue.isPotentiallyAssigned(local) 
			|| initsWhenFalse.isPotentiallyAssigned(local);
}
	
public boolean isPotentiallyNonNull(LocalVariableBinding local) {
	return initsWhenTrue.isPotentiallyNonNull(local) 
		|| initsWhenFalse.isPotentiallyNonNull(local);
}	
	
public boolean isPotentiallyNull(LocalVariableBinding local) {
	return initsWhenTrue.isPotentiallyNull(local) 
		|| initsWhenFalse.isPotentiallyNull(local);
}	

public boolean isPotentiallyUnknown(LocalVariableBinding local) {
	return initsWhenTrue.isPotentiallyUnknown(local) 
		|| initsWhenFalse.isPotentiallyUnknown(local);
}	

public boolean isProtectedNonNull(LocalVariableBinding local) {
	return initsWhenTrue.isProtectedNonNull(local) 
		&& initsWhenFalse.isProtectedNonNull(local);
}		
	
public boolean isProtectedNull(LocalVariableBinding local) {
	return initsWhenTrue.isProtectedNull(local) 
		&& initsWhenFalse.isProtectedNull(local);
}		
	
public void markAsComparedEqualToNonNull(LocalVariableBinding local) {
	initsWhenTrue.markAsComparedEqualToNonNull(local);
	initsWhenFalse.markAsComparedEqualToNonNull(local);
}

public void markAsComparedEqualToNull(LocalVariableBinding local) {
	initsWhenTrue.markAsComparedEqualToNull(local);
    initsWhenFalse.markAsComparedEqualToNull(local);
}
	
public void markAsDefinitelyAssigned(FieldBinding field) {
	initsWhenTrue.markAsDefinitelyAssigned(field);
	initsWhenFalse.markAsDefinitelyAssigned(field);	
}

public void markAsDefinitelyAssigned(LocalVariableBinding local) {
	initsWhenTrue.markAsDefinitelyAssigned(local);
	initsWhenFalse.markAsDefinitelyAssigned(local);	
}

public void markAsDefinitelyNonNull(LocalVariableBinding local) {
	initsWhenTrue.markAsDefinitelyNonNull(local);
	initsWhenFalse.markAsDefinitelyNonNull(local);	
}

public void markAsDefinitelyNull(LocalVariableBinding local) {
	initsWhenTrue.markAsDefinitelyNull(local);
	initsWhenFalse.markAsDefinitelyNull(local);	
}

public void markAsDefinitelyUnknown(LocalVariableBinding local) {
	initsWhenTrue.markAsDefinitelyUnknown(local);
	initsWhenFalse.markAsDefinitelyUnknown(local);	
}

public FlowInfo setReachMode(int reachMode) {
	if (reachMode == REACHABLE) {
		this.tagBits &= ~UNREACHABLE;
	}
	else {
		this.tagBits |= UNREACHABLE;
	}
	initsWhenTrue.setReachMode(reachMode);
	initsWhenFalse.setReachMode(reachMode);
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
	
	return "FlowInfo<true: " + initsWhenTrue.toString() + ", false: " + initsWhenFalse.toString() + ">"; //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$
}

public FlowInfo safeInitsWhenTrue() {
	return initsWhenTrue;
}

public UnconditionalFlowInfo unconditionalCopy() {
	return initsWhenTrue.unconditionalCopy().
			mergedWith(initsWhenFalse.unconditionalInits());
}

public UnconditionalFlowInfo unconditionalFieldLessCopy() {
	return initsWhenTrue.unconditionalFieldLessCopy().
		mergedWith(initsWhenFalse.unconditionalFieldLessCopy()); 
	// should never happen, hence suboptimal does not hurt
}

public UnconditionalFlowInfo unconditionalInits() {
	return initsWhenTrue.unconditionalInits().
			mergedWith(initsWhenFalse.unconditionalInits());
}

public UnconditionalFlowInfo unconditionalInitsWithoutSideEffect() {
	// cannot do better here than unconditionalCopy - but still a different 
	// operation for UnconditionalFlowInfo
	return initsWhenTrue.unconditionalCopy().
			mergedWith(initsWhenFalse.unconditionalInits());
}
}
