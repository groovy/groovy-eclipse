/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
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
