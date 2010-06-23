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
public class HighlightedTypedPosition extends Position {
    public static enum HighlightKind {
        UNKNOWN, REGEX
    }

    public HighlightedTypedPosition(int offset, HighlightKind kind) {
        super(offset);
        this.kind = kind;
    }

    public HighlightedTypedPosition(int offset, int length, HighlightKind kind) {
        super(offset, length);
        this.kind = kind;
    }

    public final HighlightKind kind;

}
