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

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public abstract class AbstractGroovyCompletionProposal implements
		IGroovyCompletionProposal {

	public Image getImage() {
		String imageLocation = getImageBundleLocation();
		if (imageLocation != null) {
			return JavaPluginImages.get(imageLocation);
		}
		return null;
	}

	abstract protected String getImageBundleLocation();
	
	/**
	 * 
	 * @param proposal
	 *            Eclipse quick fix proposal
	 * @return JDT quick fix representation
	 */
	public IJavaCompletionProposal convertToJavaCompletionProposal() {

		final ICompletionProposal proposalToConvert = this;
		final int relevance = getRelevance();

		return new IJavaCompletionProposal() {

			public Point getSelection(IDocument document) {
				return proposalToConvert.getSelection(document);
			}

			public Image getImage() {
				return proposalToConvert.getImage();
			}

			public String getDisplayString() {
				return proposalToConvert.getDisplayString();
			}

			public IContextInformation getContextInformation() {
				return proposalToConvert.getContextInformation();
			}

			public String getAdditionalProposalInfo() {
				return proposalToConvert.getAdditionalProposalInfo();
			}

			public void apply(IDocument document) {
				proposalToConvert.apply(document);
			}

			public int getRelevance() {
				return relevance;
			}
		};
	}

}
