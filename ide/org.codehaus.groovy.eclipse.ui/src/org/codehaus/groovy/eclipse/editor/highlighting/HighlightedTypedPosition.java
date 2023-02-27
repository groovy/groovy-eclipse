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
package org.codehaus.groovy.eclipse.editor.highlighting;

import org.eclipse.jface.text.Position;

public class HighlightedTypedPosition extends Position implements Comparable<Position> {

    public enum HighlightKind {
        COMMENT, DEFAULT, KEYWORD, RESERVED, NUMBER, STRING, REGEXP, MAP_KEY, TAG_KEY,
        CTOR, METHOD, STATIC_METHOD, CTOR_CALL, GROOVY_CALL, METHOD_CALL, STATIC_CALL,
        CLASS, ABSTRACT_CLASS, INTERFACE, TRAIT, ANNOTATION, ENUMERATION, PLACEHOLDER,
        FIELD, STATIC_FIELD, STATIC_VALUE, PARAMETER, VARIABLE, DEPRECATED, UNKNOWN
    }

    public final HighlightKind kind;

    public HighlightedTypedPosition(Position pos, HighlightKind kind) {
        super(pos.getOffset(), pos.getLength());
        this.kind = kind;
    }

    public HighlightedTypedPosition(int offset, HighlightKind kind) {
        super(offset);
        this.kind = kind;
    }

    public HighlightedTypedPosition(int offset, int length, HighlightKind kind) {
        super(offset, length);
        this.kind = kind;
    }

    @Override
    public int compareTo(Position that) {
        return (that == null ? 1 : this.offset - that.offset);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (!super.equals(that) || !(that instanceof HighlightedTypedPosition))
            return false;
        return (kind == ((HighlightedTypedPosition) that).kind);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (kind == null ? 0 : kind.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "HighlightedTypedPosition[kind=" + kind + ", offset=" + offset + ", length=" + length + "]";
    }
}
