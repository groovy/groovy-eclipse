/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.codegen;

public class CaseLabel extends BranchLabel {

	public int instructionPosition = POS_NOT_SET;
	private BranchLabel branchLabel; // doppelganger

public CaseLabel(CodeStream codeStream) {
	super(codeStream);
}

public CaseLabel(CodeStream codeStream, boolean allowNarrowBranch) {
	super(codeStream);
	if (allowNarrowBranch)
		this.branchLabel = new BranchLabel(codeStream);
}

@Override
void branch() {
	this.branchLabel.branch();
}

/*
* Put down  a reference to the array at the location in the codestream.
* #placeInstruction() must be performed prior to any #branch()
*/
@Override
void branchWide() {
	if (this.position == POS_NOT_SET) {
		addForwardReference(this.codeStream.position);
		// Leave 4 bytes free to generate the jump offset afterwards
		this.codeStream.position += 4;
		this.codeStream.classFileOffset += 4;
	} else { //Position is set. Write it!
		/*
		 * Position is set. Write it if it is not a wide branch.
		 */
		this.codeStream.writeSignedWord(this.position - this.instructionPosition);
	}
	trackStackDepth(true);
}

@Override
public boolean isCaseLabel() {
	return true;
}
@Override
public boolean isStandardLabel(){
	return false;
}
/*
* Put down  a reference to the array at the location in the codestream.
*/
@Override
public void place() {
	this.position = this.codeStream.position;
	if (this.instructionPosition != POS_NOT_SET) {
		int offset = this.position - this.instructionPosition;
		int[] forwardRefs = forwardReferences();
		for (int i = 0, length = forwardReferenceCount(); i < length; i++) {
			this.codeStream.writeSignedWord(forwardRefs[i], offset);
		}
		// add the label in the codeStream labels collection
		this.codeStream.addLabel(this);
	}
	trackStackDepth(false);
	if (this.branchLabel != null)
		this.branchLabel.place();
}

/*
* Put down  a reference to the array at the location in the codestream.
*/
void placeInstruction() {
	if (this.instructionPosition == POS_NOT_SET) {
		this.instructionPosition = this.codeStream.position;
	}
}
}
