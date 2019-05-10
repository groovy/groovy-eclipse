/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.antlr;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple buffer that provides line/col access to chunks of source code
 * held within itself.
 */
public class SourceBuffer {
    private final List<StringBuilder> lines;
    private StringBuilder current;
    // GRECLIPSE add
    private final List<Integer> lineEndings = new ArrayList<Integer>();
    // GRECLIPSE-805: Support for unicode escape sequences
    private UnicodeEscapingReader unescaper = new NoEscaper();
    // GRECLIPSE end

    public SourceBuffer() {
        lines = new ArrayList<StringBuilder>();
        //lines.add(new StringBuilder()); // dummy row for position [0] in the List

        current = new StringBuilder();
        lines.add(current);
        // GRECLIPSE add
        lineEndings.add(0);
        // GRECLIPSE end
    }

    /**
     * Obtains a snippet of the source code within the bounds specified
     * @param start (inclusive line/ inclusive column)
     * @param end (inclusive line / exclusive column)
     * @return specified snippet of source code as a String, or null if no source available
     */
    public String getSnippet(LineColumn start, LineColumn end) {
        // preconditions
        if (start == null || end == null) { return null; } // no text to return
        if (start.equals(end)) { return null; } // no text to return
        if (lines.size() == 1 && current.length() == 0) { return null; } // buffer hasn't been filled yet

        // working variables
        int startLine = start.getLine();
        int startColumn = start.getColumn();
        int endLine = end.getLine();
        int endColumn = end.getColumn();

        // reset any out of bounds requests
        if (startLine < 1) { startLine = 1;}
        if (endLine < 1) { endLine = 1;}
        if (startColumn < 1) { startColumn = 1;}
        if (endColumn < 1) { endColumn = 1;}
        if (startLine > lines.size()) { startLine = lines.size(); }
        if (endLine > lines.size()) { endLine = lines.size(); }

        // obtain the snippet from the buffer within specified bounds
        StringBuilder snippet = new StringBuilder();
        for (int i = startLine - 1; i < endLine;i++) {
            String line = ((StringBuilder)lines.get(i)).toString();
            if (startLine == endLine) {
                // reset any out of bounds requests (again)
                if (startColumn > line.length()) { startColumn = line.length();}
                if (startColumn < 1) { startColumn = 1;}
                if (endColumn > line.length()) { endColumn = line.length() + 1;}
                if (endColumn < 1) { endColumn = 1;}
                if (endColumn < startColumn) { endColumn = startColumn;}

                line = line.substring(startColumn - 1, endColumn - 1);
            } else {
                if (i == startLine - 1) {
                    if (startColumn - 1 < line.length()) {
                        line = line.substring(startColumn - 1);
                    }
                }
                if (i == endLine - 1) {
                    if (endColumn - 1 < line.length()) {
                        line = line.substring(0,endColumn - 1);
                    }
                }
            }
            snippet.append(line);
        }
        return snippet.toString();
    }

    /**
     * Writes the specified character into the buffer
     * @param c
     */
    public void write(int c) {
        if (c != -1) {
            // GRECLIPSE add
            col += 1;
            // GRECLIPSE end
            current.append((char)c);
        }
        if (c == '\n') {
            current = new StringBuilder();
            // GRECLIPSE add
            if (!prevWasCarriageReturn) {
            // GRECLIPSE end
            lines.add(current);
            // GRECLIPSE add
            } else { // \r\n was found
                // back out previous line and add a \n to the line
                lines.get(lines.size() - 1).append('\n');
                lineEndings.remove(lineEndings.size() - 1);
            }
            lineEndings.add(col + unescaper.getUnescapedUnicodeOffsetCount());
            // GRECLIPSE end
        }
        // GRECLIPSE add
        // handle carriage returns as well as newlines
        if (c == '\r') {
            current = new StringBuilder();
            lines.add(current);
            lineEndings.add(col + unescaper.getUnescapedUnicodeOffsetCount());
            // this may be a \r\n, but may not be
            prevWasCarriageReturn = true;
        } else {
            prevWasCarriageReturn = false;
        }
        // GRECLIPSE end
    }
    private int col;
    private boolean prevWasCarriageReturn;

    public LocationSupport getLocationSupport() {
        lineEndings.add(col + unescaper.getUnescapedUnicodeOffsetCount()); // last line ends where the data runs out
        int[] lineEndingsArray = new int[lineEndings.size()];
        for (int i = 0, max = lineEndings.size(); i < max; i += 1) {
            lineEndingsArray[i] = lineEndings.get(i).intValue();
        }
        return new LocationSupport(lineEndingsArray);
    }

    public void setUnescaper(UnicodeEscapingReader unicodeEscapingReader) {
        this.unescaper = unicodeEscapingReader;
    }
    // GRECLIPSE end
}

/**
 * GRECLIPSE-805: Support for unicode escape sequences
 */
class NoEscaper extends UnicodeEscapingReader {
    public NoEscaper() {
        super(null, null);
    }
    public int getUnescapedUnicodeColumnCount() {
        return 0;
    }
    public int getUnescapedUnicodeOffsetCount() {
        return 0;
    }
}
