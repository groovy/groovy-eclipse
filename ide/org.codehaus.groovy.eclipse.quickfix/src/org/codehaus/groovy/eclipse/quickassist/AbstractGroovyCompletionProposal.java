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
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * All {@link AbstractGroovyCompletionProposal}s must have a constructor that takes
 * an {@link IInvocationContext} and nothing else.
 * 
 * @author Andrew Eisenberg
 * @created Oct 28, 2011
 */
public abstract class AbstractGroovyCompletionProposal implements IJavaCompletionProposal {
    
    private final IInvocationContext context;

    public AbstractGroovyCompletionProposal(IInvocationContext context) {
        this.context = context;
    }

    protected IInvocationContext getContext() {
        return context;
    }
    
	public Image getImage() {
		String imageLocation = getImageBundleLocation();
		if (imageLocation != null) {
			return JavaPluginImages.get(imageLocation);
		}
		return null;
	}

	abstract protected String getImageBundleLocation();
	
	/**
	 * @return true iff this completion proposal is valid in the current context
	 */
	abstract public boolean hasProposals();
	
	   /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection
     * (org.eclipse.jface.text.IDocument)
     */
    public Point getSelection(IDocument document) {
        return new Point(context.getSelectionOffset(), context.getSelectionLength());
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
}
