 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;

public class SingleHyperlink implements IHyperlink {
	private IJavaElement proposal;
	private IRegion hyperlinkRegion;

	public SingleHyperlink(IJavaElement proposal, IRegion hyperlinkRegion) {
		this.proposal = proposal;
		this.hyperlinkRegion = hyperlinkRegion;
	}

	public IRegion getHyperlinkRegion() {
		return hyperlinkRegion;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return proposal.getElementName();
	}

	public void open() {
	    try {
            EditorUtility.openInEditor(proposal, true);
        } catch (PartInitException e) {
            GroovyCore.logException("Error opening editor for " + proposal, e);
        }
	}
}
