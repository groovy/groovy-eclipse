/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.eclipse.jdt.groovy.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods - can be made more eclipse friendly or replaced if the groovy infrastructure provides the information (eg.
 * getSourceLineSeparatorsIn())
 * 
 * @author Andy Clement
 */
public class GroovyUtils {

	// FIXASC don't use this any more?
	public static int[] getSourceLineSeparatorsIn(char[] code) {
		List<Integer> lineSeparatorsCollection = new ArrayList<Integer>();
		for (int i = 0, max = code.length; i < max; i++) {
			if (code[i] == '\r') {
				if ((i + 1) < max && code[i + 1] == '\n') {// \r\n
					lineSeparatorsCollection.add(i + 1); // add the position of the \n
					i++;
				} else {
					lineSeparatorsCollection.add(i); // add the position of the \r
				}
			} else if (code[i] == '\n') {
				lineSeparatorsCollection.add(i);
			}
		}
		int[] lineSepPositions = new int[lineSeparatorsCollection.size()];
		for (int i = 0; i < lineSeparatorsCollection.size(); i++) {
			lineSepPositions[i] = lineSeparatorsCollection.get(i);
		}
		return lineSepPositions;
	}
}
