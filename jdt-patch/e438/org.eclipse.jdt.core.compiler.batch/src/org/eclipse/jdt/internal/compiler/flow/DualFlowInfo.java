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

/**
 * A wrapper for two flow infos being co-maintained side-by-side.
 *
 * All inherited methods that dispatch to the individual flow infos {@link #initsWhenTrue} and {@link #initsWhenFalse}
 * are re-used as-is. Any operations involving some kind of copying need to be overridden.
 */
public class DualFlowInfo extends ConditionalFlowInfo {

	public DualFlowInfo(FlowInfo mainInfo, FlowInfo companionInfo) {
		super(mainInfo, companionInfo);
	}

	@Override
	public FlowInfo asNegatedCondition() {
		this.initsWhenTrue = this.initsWhenTrue.asNegatedCondition();
		this.initsWhenFalse = this.initsWhenFalse.asNegatedCondition();
		return this;
	}

	@Override
	public FlowInfo copy() {
		return new DualFlowInfo(this.initsWhenTrue.copy(), this.initsWhenFalse.copy());
	}

	@Override
	public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits) {
		return new UnconditionalDualFlowInfo(this.initsWhenTrue.mergedWith(otherInits), this.initsWhenFalse.mergedWith(otherInits));
	}

	@Override
	public UnconditionalFlowInfo mergeDefiniteInitsWith(UnconditionalFlowInfo otherInits) {
		return new UnconditionalDualFlowInfo(this.initsWhenTrue.mergeDefiniteInitsWith(otherInits), this.initsWhenFalse.mergeDefiniteInitsWith(otherInits));
	}

	@Override
	public UnconditionalFlowInfo unconditionalCopy() {
		return new UnconditionalDualFlowInfo(this.initsWhenTrue.unconditionalCopy(), this.initsWhenFalse.unconditionalCopy());
	}

	@Override
	public UnconditionalFlowInfo unconditionalFieldLessCopy() {
		return new UnconditionalDualFlowInfo(this.initsWhenTrue.unconditionalFieldLessCopy(), this.initsWhenFalse.unconditionalFieldLessCopy());
	}

	@Override
	public UnconditionalFlowInfo unconditionalInits() {
		return new UnconditionalDualFlowInfo(this.initsWhenTrue.unconditionalInits(), this.initsWhenFalse.unconditionalInits());
	}

	@Override
	public UnconditionalFlowInfo unconditionalInitsWithoutSideEffect() {
		return new UnconditionalDualFlowInfo(this.initsWhenTrue.unconditionalInitsWithoutSideEffect(), this.initsWhenFalse.unconditionalInitsWithoutSideEffect());
	}

	@Override
	public String toString() {

		return "FlowInfo<main: " + this.initsWhenTrue.toString() + ", companion: " + this.initsWhenFalse.toString() + ">"; //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$
	}

}
