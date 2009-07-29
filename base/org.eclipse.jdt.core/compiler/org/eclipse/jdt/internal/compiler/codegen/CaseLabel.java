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
package org.eclipse.jdt.internal.compiler.codegen;

public class CaseLabel extends BranchLabel {
	
	public int instructionPosition = POS_NOT_SET;
	
/**
 * CaseLabel constructor comment.
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public CaseLabel(CodeStream codeStream) {
	super(codeStream);
}

/*
* Put down  a reference to the array at the location in the codestream.
* #placeInstruction() must be performed prior to any #branch()
*/
void branch() {
	if (position == POS_NOT_SET) {
		addForwardReference(codeStream.position);
		// Leave 4 bytes free to generate the jump offset afterwards
		codeStream.position += 4;
		codeStream.classFileOffset += 4;
	} else { //Position is set. Write it!
		/*
		 * Position is set. Write it if it is not a wide branch.
		 */
		this.codeStream.writeSignedWord(this.position - this.instructionPosition);
	}
}

/*
* No support for wide branches yet
*/
void branchWide() {
	this.branch(); // case label branch is already wide
}

public boolean isCaseLabel() {
	return true;
}
public boolean isStandardLabel(){
	return false;
}
/*
* Put down  a reference to the array at the location in the codestream.
*/
public void place() {
	if ((this.tagBits & USED) != 0) {
		position = codeStream.getPosition();
	} else {
		position = codeStream.position;
	}
	if (instructionPosition != POS_NOT_SET) {
		int offset = position - instructionPosition;
		int[] forwardRefs = forwardReferences();
		for (int i = 0, length = forwardReferenceCount(); i < length; i++) {
			codeStream.writeSignedWord(forwardRefs[i], offset);
		}
		// add the label int the codeStream labels collection
		codeStream.addLabel(this);
	}
}

/*
* Put down  a reference to the array at the location in the codestream.
*/
void placeInstruction() {
	if (instructionPosition == POS_NOT_SET) {
		instructionPosition = codeStream.position;
	}
}
}
