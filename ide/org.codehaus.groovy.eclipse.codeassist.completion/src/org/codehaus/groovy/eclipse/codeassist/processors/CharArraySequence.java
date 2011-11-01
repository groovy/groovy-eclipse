/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.processors;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * A char sequence from an array
 */
public class CharArraySequence implements CharSequence {

    private final char[] chars;

    public CharArraySequence(char[] chars) {
        this.chars = chars;
    }

    public CharArraySequence(String contents) {
        this.chars = contents.toCharArray();
    }

    public char[] chars() {
        return chars;
    }

    public int length() {
        return chars.length;
    }

    /**
     * may throw {@link IndexOutOfBoundsException}
     */
    public char charAt(int index) {
        return chars[index];
    }

    public CharArraySequence subSequence(int start, int end) {
        return new CharArraySequence(CharOperation.subarray(chars, start,
                end));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i]);
        }
        return sb.toString();
    }
}