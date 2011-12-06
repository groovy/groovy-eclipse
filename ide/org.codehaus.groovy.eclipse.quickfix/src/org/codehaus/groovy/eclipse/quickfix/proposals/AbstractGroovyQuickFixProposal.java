/*
 * Copyright 2010-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Base class for a Groovy quick fix proposal where an image is specified by a
 * path, and the proposal can handle a Groovy quick fix problem representation.
 * 
 * @author Nieraj Singh
 * 
 */
public abstract class AbstractGroovyQuickFixProposal implements IJavaCompletionProposal {

	private QuickFixProblemContext problemContext;
	private int relevance;

	public AbstractGroovyQuickFixProposal(QuickFixProblemContext problem) {
		this.problemContext = problem;
	}
	
	public AbstractGroovyQuickFixProposal(QuickFixProblemContext problem, int relevance) {
		this.problemContext = problem;
		this.relevance = relevance;
	}

	
	public int getRelevance() {
		return relevance;
	}

	/**
	 * 
	 * @return the invocation context and problem this proposal should fix.
	 */
	protected QuickFixProblemContext getQuickFixProblemContext() {
		return problemContext;
	}

	public Point getSelection(IDocument document) {
		return problemContext != null ? new Point(problemContext.getOffset(), problemContext.getLength()) : null;
	}

	public String getAdditionalProposalInfo() {
		return null;
	}

	public Image getImage() {
		String imageLocation = getImageBundleLocation();
		if (imageLocation != null) {
			return JavaPluginImages.get(imageLocation);
		}
		return null;
	}

	abstract protected String getImageBundleLocation();

	public IContextInformation getContextInformation() {
		return null;
	}

}
