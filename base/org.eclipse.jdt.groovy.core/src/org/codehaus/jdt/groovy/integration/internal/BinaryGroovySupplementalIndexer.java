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
package org.codehaus.jdt.groovy.integration.internal;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jdt.groovy.integration.ISupplementalIndexer;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;

class BinaryGroovySupplementalIndexer implements ISupplementalIndexer {

    @Override
    public List<char[]> extractNamedReferences(final byte[] contents, final ClassFileReader reader) {
        int[] constantPoolOffsets = reader.getConstantPoolOffsets();
        int constantPoolCount = constantPoolOffsets.length;
        List<char[]> refs = new ArrayList<>();
        for (int i = 1; i < constantPoolCount; i += 1) {
            int tag = reader.u1At(constantPoolOffsets[i]);
            switch (tag) {
            case ClassFileConstants.Utf8Tag:
                char[] strConst = extractStringConstant(constantPoolOffsets, reader, i);
                if (isValidId(strConst)) {
                    char[][] splits = CharOperation.splitOn('.', strConst);
                    for (char[] split : splits) {
                        refs.add(split);
                    }
                }
            }
        }
        return refs;
    }

    private boolean isValidId(final char[] strConst) {
        if (strConst == null || strConst.length == 0) {
            return false;
        }
        if (!(Character.isJavaIdentifierStart(strConst[0]) || strConst[0] == '.') || strConst[0] == '$') {
            return false;
        }
        for (int i = 1; i < strConst.length; i += 1) {
            if (!(Character.isJavaIdentifierPart(strConst[i]) || strConst[i] == '.') || strConst[i] == '$') {
                return false;
            }
        }
        return true;
    }

    private char[] extractStringConstant(final int[] constantPoolOffsets, final ClassFileReader reader, final int index) {
        int strlen = reader.u2At(constantPoolOffsets[index] + 1); // / +1 for the tag type
        int strstart = constantPoolOffsets[index] + 3; // +1 for the tag type and +2 for the strlen
        return reader.utf8At(strstart, strlen);
    }
}
