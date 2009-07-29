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


import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;

/**
 * The interface to get IDeclarationProposals with.
 * 
 * This interface is not intended to be implemented, use
 * DeclarationSearchAssistant.getInstance() to get the singleton.
 * 
 * @author emp
 */
public interface IDeclarationSearchAssistant {
    List<IJavaElement> getProposals(IEditorPart editor, IRegion region);
	List<IJavaElement> getProposals(ICompilationUnit unit, IRegion region);
}
