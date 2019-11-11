/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.ISourceBuffer;

/**
 * Implementation of ISourceBuffer for String instances.
 */
public class StringSourceBuffer implements ISourceBuffer {
    private final char[] sourceCode;

    /** List of Integer to line offsets. */
    private final List<Integer> lineOffsets;

    public StringSourceBuffer(String sourceCode) {
        this.sourceCode = new char[sourceCode.length()];
        sourceCode.getChars(0, sourceCode.length(), this.sourceCode, 0);
        this.lineOffsets = createLineLookup(this.sourceCode);
    }

    private List<Integer> createLineLookup(char[] sourceCode) {
        List<Integer> offsets = new ArrayList<>();
        if (sourceCode.length == 0) {
            return offsets;
        }
        offsets.add(Integer.valueOf(0));

        int ch;
        for (int i = 0; i < sourceCode.length; i += 1) {
            ch = sourceCode[i];
            if (ch == '\r') {
                if (i + 1 < sourceCode.length) {
                    ch = sourceCode[i + 1];
                    if (ch == '\n') {
                        offsets.add(Integer.valueOf(++i + 1));
                    } else {
                        offsets.add(Integer.valueOf(i + 1));
                    }
                } else {
                    offsets.add(Integer.valueOf(i + 1));
                }
            } else if (ch == '\n') {
                offsets.add(Integer.valueOf(i + 1));
            }
        }

        return offsets;
    }

    @Override
    public char charAt(int offset) {
        try {
            return sourceCode[offset];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Offset: " + offset + ", Range: [0.." + (sourceCode.length - 1) + "]");
        }
    }

    @Override
    public int length() {
        return sourceCode.length;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new String(sourceCode, start, end - start);
    }

    @Override
    public int[] toLineColumn(int offset) {
        try {
            for (int i = 0; i < lineOffsets.size(); ++i) {
                int lineOffset = lineOffsets.get(i).intValue();
                if (offset < lineOffset) {
                    lineOffset = lineOffsets.get(i - 1).intValue();
                    return new int[] { i, offset - lineOffset + 1 };
                }
            }
            int line = lineOffsets.size();
            int lineOffset = lineOffsets.get(line - 1).intValue();
            return new int[] { line, offset - lineOffset + 1 };
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Offset: " + offset + ", Range: [0.." + (sourceCode.length - 1) + "]");
        }
    }

    @Override
    public int toOffset(int line, int column) {
        int offset = lineOffsets.get(line - 1).intValue();
        return offset + column - 1;
    }
}
