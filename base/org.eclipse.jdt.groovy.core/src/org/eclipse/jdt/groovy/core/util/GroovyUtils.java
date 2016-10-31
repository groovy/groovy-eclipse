/*
 * Copyright 2009-2016 the original author or authors.
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
