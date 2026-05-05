/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.antlr;

import java.util.List;
import java.util.Objects;

/**
 * Maps lines/columns to offsets in a text file.  Assumes '\n' is the newline
 * delimiter.  The newline character is included as the last char on the line.
 * Lines and columns are both 1 based
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

    private final int[] lineEndings;

    private static final int[] NO_LINE_ENDINGS = new int[0];

    public static final LocationSupport NO_LOCATIONS = new LocationSupport();

    public LocationSupport() {
        this(NO_LINE_ENDINGS);
    }

    public LocationSupport(int[] lineEndings) {
        this.lineEndings = Objects.requireNonNull(lineEndings);
    }

    public LocationSupport(List<? extends CharSequence> lines) {
        this(lines != null ? processLineEndings(lines) : NO_LINE_ENDINGS);
    }

    private static int[] processLineEndings(List<? extends CharSequence> lines) {
        int[] lineEndings = new int[lines.size() + 1]; // last index stores end of file
        int total = 0;
        int current = 1;
        for (CharSequence line : lines) {
            total += line.length();
            lineEndings[current++] = total;
        }
        return lineEndings;
    }

    public int findOffset(int row, int col) {
        if (row > 0 && row <= lineEndings.length) {
            return lineEndings[row - 1] + col - 1;
        }
        return 0;
    }

    public int getEnd() {
        if (lineEndings.length > 0) {
            return lineEndings[lineEndings.length - 1];
        }
        return 0;
    }

    public int getEndColumn() {
        if (lineEndings.length > 1) {
            return lineEndings[lineEndings.length - 1] - lineEndings[lineEndings.length - 2];
        } else if (lineEndings.length > 0) {
            return lineEndings[0];
        }
        return 0;
    }

    public int getEndLine() {
        if (lineEndings.length > 0) {
            return lineEndings.length - 1;
        }
        return 0;
    }

    public int[] getRowCol(int offset) {
        for (int i = 1, n = lineEndings.length; i < n; i += 1) {
            if (lineEndings[i] > offset || (i + 1 == n && lineEndings[i] == offset)) {
                return new int[] {i, offset - lineEndings[i - 1] + 1};
            }
        }
        throw new RuntimeException("Location is after end of document.  Offset: " + offset);
    }

    public boolean isPopulated() {
        return (lineEndings.length > 0);
    }
}
