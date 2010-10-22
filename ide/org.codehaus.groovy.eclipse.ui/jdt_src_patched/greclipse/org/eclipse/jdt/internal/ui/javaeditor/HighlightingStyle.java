//Modified copy from: org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingManager#Highlighting
//All changes in this file are "immaterial" and would disappear if we got a patch
//into JDT.
// This class was copied from an inner class
//Changes: search for // GROOVY
/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package greclipse.org.eclipse.jdt.internal.ui.javaeditor;

import org.eclipse.jface.text.TextAttribute;

/**
 * Highlighting.
 */
// GROOVY make public not static
public class HighlightingStyle {

    /** Text attribute */
    private TextAttribute fTextAttribute;

    /** Enabled state */
    private boolean fIsEnabled;

    /**
     * Initialize with the given text attribute.
     *
     * @param textAttribute The text attribute
     * @param isEnabled the enabled state
     */
    public HighlightingStyle(TextAttribute textAttribute, boolean isEnabled) {
        setTextAttribute(textAttribute);
        setEnabled(isEnabled);
    }

    /**
     * @return Returns the text attribute.
     */
    public TextAttribute getTextAttribute() {
        return fTextAttribute;
    }

    /**
     * @param textAttribute The background to set.
     */
    public void setTextAttribute(TextAttribute textAttribute) {
        fTextAttribute = textAttribute;
    }

    /**
     * @return the enabled state
     */
    public boolean isEnabled() {
        return fIsEnabled;
    }

    /**
     * @param isEnabled the new enabled state
     */
    public void setEnabled(boolean isEnabled) {
        fIsEnabled = isEnabled;
    }
}
