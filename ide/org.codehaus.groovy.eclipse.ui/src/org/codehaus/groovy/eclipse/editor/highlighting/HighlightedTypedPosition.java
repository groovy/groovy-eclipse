/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.highlighting;

import org.eclipse.jface.text.Position;

/**
 *
 * @author andrew
 * @created Jun 10, 2010
 */
public class HighlightedTypedPosition extends Position implements Comparable<HighlightedTypedPosition> {
    public static enum HighlightKind {
        DEPRECATED, FIELD, METHOD, STATIC_FIELD, STATIC_METHOD, REGEX, NUMBER, UNKNOWN
    }

    public final HighlightKind kind;

    public HighlightedTypedPosition(Position p, HighlightKind kind) {
        super(p.getOffset(), p.getLength());
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        HighlightedTypedPosition other = (HighlightedTypedPosition) obj;
        if (kind != other.kind)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((kind == null) ? 0 : kind.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "HighlightedTypedPosition [kind=" + kind + ", offset=" + offset + ", length=" + length + ", isDeleted=" + isDeleted
                + "]";
    }

    public int compareTo(HighlightedTypedPosition o) {
        if (o == null) {
            return 1;
        }
        return this.offset - o.offset;
    }
}
