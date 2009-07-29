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

package org.codehaus.groovy.antlr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;

/**
 * @author Andrew Eisenberg
 * @created Jun 4, 2009
 * 
 * Maps Line/Columns to offsets in a text file.  Assumes that '\n' is the newline delimiter.
 * The newline character is included as the last char on the current line.
 * Both columns and lines are 1 based
 *
 * <ul>
 * <li> "" -> [0,0]
 * <li> "a" -> [0,1]
 * <li> "\n" -> [0,1], [1,0]
 * <li> "a\n" -> [0,2], [2,0]
 * <li> "a\nb" -> [0,2], [2,1]
 * <li> "a\nbc\n" -> [0,2], [2,3], [5,0]
 * </ul>
 */
public class LocationSupport {
	
	private static final int[] NO_LINE_ENDINGS = new int[0];

	public static final LocationSupport NO_LOCATIONS = new LocationSupport();
	
    private final int[] lineEndings;
    
    // not used
    public LocationSupport(char[] contents) {
    	if (contents != null) {
    		lineEndings = processLineEndings(contents);
    	} else {
    		lineEndings = NO_LINE_ENDINGS;
    	}
    }
    
    public LocationSupport(List<StringBuffer> lines) {
    	if (lines != null) {
    		lineEndings = processLineEndings(lines);
    	} else {
    		lineEndings = NO_LINE_ENDINGS;
    	}
    }

    public LocationSupport(int[] lineEndings) {
    	this.lineEndings = lineEndings;
    }
    
    public LocationSupport() {
        lineEndings = NO_LINE_ENDINGS;
    }
    
    private int[] processLineEndings(List<StringBuffer> lines) {
        int[] newLineEndings = new int[lines.size()+1];  // last index stores end of file
        int total = 0;
        int current = 1;
        for (StringBuffer line : lines) {
        	newLineEndings[current++] = total += (line.length());
        }
        return newLineEndings;
    }
    
    
    private int[] processLineEndings(char[] contents) {
        List<Integer> l = new ArrayList<Integer>();
        for (int i = 0; i < contents.length; i++) {
        	
            if (contents[i] == '\n') {
                l.add(i);
            } else if (contents[i] == '\r') {
            	l.add(i);
            	if (i < contents.length && contents[i] == '\n') {
            		i++;
            	}
            }
        }
        
        int[] newLineEndings = new int[l.size()];
        int i = 0;
        for (Integer integer : l) {
        	newLineEndings[i] = integer.intValue();
        }
        return newLineEndings;
    }

    // TODO maybe should throw exception if out of bounds?
    public int findOffset(int row, int col) {
        return row <= lineEndings.length && row > 0 ? lineEndings[row-1] + col-1 : 0;
    }
    public int getEnd() {
        return lineEndings.length > 0 ? 
                lineEndings[lineEndings.length-1] :
                0;
    }
    public int getEndColumn() {
        if (lineEndings.length > 1) {
            return lineEndings[lineEndings.length-1] - lineEndings[lineEndings.length-2];
        } else if (lineEndings.length > 0) {
            return lineEndings[0];
        } else {
            return 0;
        }
    }
    public int getEndLine() {
        return lineEndings.length > 0 ?
                lineEndings.length-1 : 0;  // last index contains length of document
    }
    
    public int[] getRowCol(int offset) {
        for (int i = 1; i < lineEndings.length; i++) {
            if (lineEndings[i] > offset) {
                return new int[] { i, offset - lineEndings[i-1] +1};
            }
        }
        // after end of document
        throw new RuntimeException("Location is after end of document.  Offset : " + offset);
    }
    
    public boolean isPopulated() {
        return lineEndings.length > 0;
    }
}
