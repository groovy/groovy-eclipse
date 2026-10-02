/*******************************************************************************
 * Copyright (c) 2000, 2026 Advantest Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Advantest Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;

public class InstanceFieldsFlowInfo extends UnconditionalFlowInfo {

	UnconditionalFlowInfo prologueInfo;

	private InstanceFieldsFlowInfo() {
		// for copy operations.
	}

	@Override
	public InstanceFieldsFlowInfo newInstance() {
		return new InstanceFieldsFlowInfo();
	}

	public InstanceFieldsFlowInfo(UnconditionalFlowInfo fieldsInfo, FlowInfo prologueInfo) {
		super.copy(fieldsInfo);
		this.prologueInfo = prologueInfo.unconditionalInits();
		if ((prologueInfo.reachMode() & FlowInfo.UNREACHABLE_OR_DEAD) != 0) {
			setReachMode(FlowInfo.UNREACHABLE_OR_DEAD);
		} else {
			for (int i = 0; i < this.prologueInfo.maxFieldCount; i++) { // A field that is DAs in EVERY constructor prologue is DA for fields analysis.
				if (this.prologueInfo.isDefinitelyAssigned(i))
					markAsDefinitelyAssigned(i);
			}
		}
	}

	@Override
	public boolean isPotentiallyAssigned(FieldBinding field) {
		// Raison d'etre for this class's existence! All else is support system
		return super.isPotentiallyAssigned(field) || this.prologueInfo.isPotentiallyAssigned(field);
	}

	@Override
	public InstanceFieldsFlowInfo copy() {
		InstanceFieldsFlowInfo copy = (InstanceFieldsFlowInfo) super.copy();
		copy.prologueInfo = this.prologueInfo;
		return copy;
	}

	@Override
	public InstanceFieldsFlowInfo nullInfoLessUnconditionalCopy() {
		InstanceFieldsFlowInfo copy = (InstanceFieldsFlowInfo) super.nullInfoLessUnconditionalCopy();
		copy.prologueInfo = this.prologueInfo;
		return copy;
	}

	@Override
	public InstanceFieldsFlowInfo unconditionalFieldLessCopy() {
		InstanceFieldsFlowInfo copy = (InstanceFieldsFlowInfo) super.unconditionalFieldLessCopy();
		copy.prologueInfo = FlowInfo.initial(copy.maxFieldCount); // any local classes should not see prologue initializations!
		return copy;
	}

	@Override
	public UnconditionalFlowInfo discardInitializationInfo() {
		super.discardInitializationInfo();
		this.prologueInfo = FlowInfo.initial(this.maxFieldCount); // SAST.testDiscardInitializationInfo() establishes that obeying the contract results in no harm, no foul.
		return this;
	}

	public UnconditionalFlowInfo withoutPrologues() {
		return new UnconditionalFlowInfo().copy(this);
	}

	@Override
	public String toString() {
		return "Instance Fields Flow Info  { " + super.toString() + //$NON-NLS-1$
			"}\nPrologues Merged Flow Info { " + this.prologueInfo.toString() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}