/*
 * Copyright 2012 SpringSource, a division of VMware, Inc
 * 
 * Daniel and Stephanie - Initial API and implementation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.quickassist;


import org.codehaus.groovy.eclipse.refactoring.core.convert.AssignStatementToNewLocalRefactoring;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;

/**
 * Assigns a statement to a new local variable. eg. "new Point(2,3)" becomes "def temp = new Point(2,3)"
 * 
 * @author Stephanie Van Dyk sevandyk@gmail.com
 * @created April 12, 2012
 */
public class AssignStatementToNewLocalProposal extends
		AbstractGroovyCompletionProposal {

	private final GroovyCompilationUnit unit;
	private final int length;
	private final int offset;

	
	private AssignStatementToNewLocalRefactoring assignStatementRefactoring;

	public AssignStatementToNewLocalProposal(IInvocationContext context) {
		super(context);
		ICompilationUnit compUnit = context.getCompilationUnit();
		if (compUnit instanceof GroovyCompilationUnit) {
			this.unit = (GroovyCompilationUnit) compUnit;
		} else {
			this.unit = null;
		}
		length = context.getSelectionLength();
		offset = context.getSelectionOffset();
	}

	public int getRelevance() {
		return 0;
	}

	public void apply(IDocument document) {
		assignStatementRefactoring.applyRefactoring(document);
	}

	public Point getSelection(IDocument document) {
		return assignStatementRefactoring.getNewSelection();
	}

	public String getAdditionalProposalInfo() {
		return getDisplayString();
	}

	public String getDisplayString() {
		return "Assign statement to new local variable.";
	}

	public IContextInformation getContextInformation() {
		return new ContextInformation(getImage(), getDisplayString(),
				getDisplayString());
	}

	@Override
	protected String getImageBundleLocation() {
		return JavaPluginImages.IMG_CORRECTION_CHANGE;
	}

	@Override
	public boolean hasProposals() {
		if (unit == null) {
            return false;
        }
		assignStatementRefactoring = new AssignStatementToNewLocalRefactoring(unit, offset);
        return assignStatementRefactoring.isApplicable();
	}
}
