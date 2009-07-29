/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

/**
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 * Modeled after JavaTextTools
 * Shared text tools for GroovyEditors
 */
public class GroovyTextTools {

    private final IColorManager colorManager = new GroovyColorManager();

    private IPartitionTokenScanner partitionScanner;
    
    public IColorManager getColorManager() {
        return colorManager;
    }
    
    public void dispose() {
        colorManager.dispose();
    }
    
    /**
     * @return
     */
    public IPartitionTokenScanner getGroovyPartitionScanner() {
        if (partitionScanner == null) {
            partitionScanner = new GroovyPartitionScanner();
        }
        return partitionScanner;
    }

    
}
