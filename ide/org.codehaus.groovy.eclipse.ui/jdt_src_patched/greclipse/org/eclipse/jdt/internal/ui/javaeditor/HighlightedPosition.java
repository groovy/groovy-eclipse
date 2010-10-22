//Modified copy from: org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingManager#HighlightedPosition
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

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

/**
 * Highlighted Positions.
 */
// GROOVY make public not static
public class HighlightedPosition extends Position {

    /** Highlighting of the position */
    private HighlightingStyle fStyle;

    /** Lock object */
    private Object fLock;

    /**
     * Initialize the styled positions with the given offset, length and
     * foreground color.
     *
     * @param offset The position offset
     * @param length The position length
     * @param highlighting The position's highlighting
     * @param lock The lock object
     */
    public HighlightedPosition(int offset, int length, HighlightingStyle highlighting, Object lock) {
        super(offset, length);
        fStyle = highlighting;
        fLock = lock;
    }

    /**
     * @return Returns a corresponding style range.
     */
    public StyleRange createStyleRange() {
        int len = 0;
        if (fStyle.isEnabled())
            len = getLength();

        TextAttribute textAttribute = fStyle.getTextAttribute();
        int style = textAttribute.getStyle();
        int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
        StyleRange styleRange = new StyleRange(getOffset(), len, textAttribute.getForeground(), textAttribute.getBackground(),
                fontStyle);
        styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
        styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;

        return styleRange;
    }

    /**
     * Uses reference equality for the highlighting.
     *
     * @param off The offset
     * @param len The length
     * @param highlighting The highlighting
     * @return <code>true</code> iff the given offset, length and highlighting
     *         are equal to the internal ones.
     */
    public boolean isEqual(int off, int len, HighlightingStyle highlighting) {
        synchronized (fLock) {
            return !isDeleted() && getOffset() == off && getLength() == len && fStyle == highlighting;
        }
    }

    /**
     * Is this position contained in the given range (inclusive)? Synchronizes
     * on position updater.
     *
     * @param off The range offset
     * @param len The range length
     * @return <code>true</code> iff this position is not delete and contained
     *         in the given range.
     */
    public boolean isContained(int off, int len) {
        synchronized (fLock) {
            return !isDeleted() && off <= getOffset() && off + len >= getOffset() + getLength();
        }
    }

    public void update(int off, int len) {
        synchronized (fLock) {
            super.setOffset(off);
            super.setLength(len);
        }
    }

    /*
     * @see org.eclipse.jface.text.Position#setLength(int)
     */
    @Override
    public void setLength(int length) {
        synchronized (fLock) {
            super.setLength(length);
        }
    }

    /*
     * @see org.eclipse.jface.text.Position#setOffset(int)
     */
    @Override
    public void setOffset(int offset) {
        synchronized (fLock) {
            super.setOffset(offset);
        }
    }

    /*
     * @see org.eclipse.jface.text.Position#delete()
     */
    @Override
    public void delete() {
        synchronized (fLock) {
            super.delete();
        }
    }

    /*
     * @see org.eclipse.jface.text.Position#undelete()
     */
    @Override
    public void undelete() {
        synchronized (fLock) {
            super.undelete();
        }
    }

    /**
     * @return Returns the highlighting.
     */
    public HighlightingStyle getHighlighting() {
        return fStyle;
    }
}
