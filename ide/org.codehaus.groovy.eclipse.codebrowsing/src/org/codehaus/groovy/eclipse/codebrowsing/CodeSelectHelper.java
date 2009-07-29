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

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ICodeSelectHelper;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.IRegion;

public class CodeSelectHelper implements ICodeSelectHelper {
    
    public IJavaElement[] select(GroovyCompilationUnit unit, IRegion r) {
        List<IJavaElement> proposals = DeclarationSearchAssistant
                .getInstance().getProposals(unit, r);
        if (proposals != null) {
            IJavaElement[] elements = new IJavaElement[proposals.size()];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = proposals.get(i);
            }
            return elements;
        } else {
            return null;
        }
    }

}
