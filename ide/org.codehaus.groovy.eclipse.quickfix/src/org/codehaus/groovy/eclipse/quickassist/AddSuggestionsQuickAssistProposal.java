/*
 * Copyright 2011 the original author or authors.
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

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionCompilationUnitHelper;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;

public class AddSuggestionsQuickAssistProposal extends
		AbstractGroovyCompletionProposal {

	private GroovyCompilationUnit unit;
	private int length;
	private int offset;

	static final String LABEL = "Add inferencing suggestion";

	/**
	 * @param unit
	 * @param node
	 */
	public AddSuggestionsQuickAssistProposal(IInvocationContext context) {
		super();
		ICompilationUnit compUnit = context.getCompilationUnit();
		if (compUnit instanceof GroovyCompilationUnit) {
			this.unit = (GroovyCompilationUnit) compUnit;

		}
		length = context.getSelectionLength();
		offset = context.getSelectionOffset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse
	 * .jface.text.IDocument)
	 */
	public void apply(IDocument document) {
		IProject project = getProject();
		if (project != null) {
			new SuggestionCompilationUnitHelper(length, offset, unit, project).addSuggestion();
		}
	}

	protected IProject getProject() {
		if (unit != null) {
			IResource resource = unit.getResource();
			if (resource != null) {
				return resource.getProject();

			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection
	 * (org.eclipse.jface.text.IDocument)
	 */
	public Point getSelection(IDocument document) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#
	 * getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString
	 * ()
	 */
	public String getDisplayString() {
		return LABEL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#
	 * getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IGroovyCompletionProposal
	 * #getRelevance()
	 */
	public int getRelevance() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.codehaus.groovy.eclipse.quickfix.proposals.
	 * AbstractGroovyCompletionProposal#getImageBundleLocation()
	 */
	protected String getImageBundleLocation() {
		return null;
	}

}
