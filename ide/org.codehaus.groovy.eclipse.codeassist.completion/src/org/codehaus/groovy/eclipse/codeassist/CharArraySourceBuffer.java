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
package org.codehaus.groovy.eclipse.codeassist;

import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.eclipse.jdt.core.compiler.CharOperation;

/**
 *
 * @author andrew
 * @created Apr 29, 2011
 */
public class CharArraySourceBuffer implements ISourceBuffer {

    private final char[] chars;

    public CharArraySourceBuffer(char[] chars) {
        this.chars = chars;
    }

    public char charAt(int offset) {
        return chars[offset];
    }

    public int length() {
        return chars.length;
    }

    public CharSequence subSequence(int start, int end) {
        return String.valueOf(CharOperation.subarray(chars, start, end));
    }


    public int[] toLineColumn(int offset) {
        // not implemented
        return null;
    }

    public int toOffset(int line, int column) {
        // not implemented
        return 0;
    }

}
