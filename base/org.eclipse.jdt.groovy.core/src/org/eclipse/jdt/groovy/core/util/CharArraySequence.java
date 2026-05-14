/*
 * Copyright 2009-2023 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.compiler.CharOperation;

public class CharArraySequence implements CharSequence {

    private final char[] chars;

    public CharArraySequence(char[] chars) {
        Assert.isNotNull(chars);
        this.chars = chars;
    }

    @Override
    public char charAt(int offset) {
        return chars[offset];
    }

    @Override
    public int length() {
        return chars.length;
    }

    @Override
    public CharSequence subSequence(int start, int until) {
        return new CharArraySequence(CharOperation.subarray(chars, start, until));
    }

    @Override
    public String toString() {
        return String.valueOf(chars);
    }
}
