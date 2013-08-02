/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import groovy.lang.IntRange;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Reads parts of a file *
 * 
 * @author reto kleeb
 * @author Andrew Eisenberg
 */
public class FilePartReader {

    private static List<String> getWords(String line, IntRange range) {
		String relevantPieceOfLine = StringGroovyMethods.getAt(line, range);
		relevantPieceOfLine = relevantPieceOfLine.replaceAll("\\(", " ( ");
		relevantPieceOfLine = relevantPieceOfLine.replaceAll("\\)", " ) ");
		return StringGroovyMethods.tokenize(relevantPieceOfLine);
	}

    public static String readBackwardsFromCoordinate(IDocument doc, LineColumn coord) throws BadLocationException {
		//make sure that no one reads from a file with impossible coordinates
		if(doc != null && coord.getLine() > 0 && coord.getColumn() > 0) {
			int lineLength = doc.getLineLength(coord.getLine()-1);
			int offset = doc.getLineOffset(coord.getLine()-1);
			String line = doc.get(offset, lineLength);
			List<String> words = getWords(line, new IntRange(0, coord.getColumn()-2));
			return words.get(words.size()-1);
		} else {
			return " ";
		}
	}

    public static String readForwardFromCoordinate(IDocument doc, LineColumn coord) throws BadLocationException {
		//make sure that no one reads from a file with impossible coordinates
		if(doc != null && coord.getLine() > 0 && coord.getColumn() > 0) {
			int lineLength = doc.getLineLength(coord.getLine()-1);
			int offset = doc.getLineOffset(coord.getLine()-1);
			String line = doc.get(offset, lineLength);
            List<String> words = getWords(line, new IntRange(coord.getColumn()-1, line.length()));
            return words.get(0);
		} else {
			return " ";
		}

	}

	public static final String DEFAULT_LINE_DELIMITER = System.getProperty("line.separator");

    public static String getLineDelimiter(FileReader file) throws IOException {

		char[] content = IOGroovyMethods.getText(file).toCharArray();
		String lineDelimiter = DEFAULT_LINE_DELIMITER;
		int index = 0;
		for (char currentChar : content) {
			if(currentChar == '\r') {
				if(content[index+1] == '\n') {
                    lineDelimiter = "\r\n";
					break;
				}
                lineDelimiter = "\r"; // mac os 9
				break;
			}
			else if(currentChar == '\n'){
                lineDelimiter = "\n";
				break;
			}
			index++;
		}
		return lineDelimiter;
	}
}