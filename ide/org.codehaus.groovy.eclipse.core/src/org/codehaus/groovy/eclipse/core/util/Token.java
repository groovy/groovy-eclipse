/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.util;

import java.io.Serializable;

/**
 * Tokens recognized for an expression.
 */
public class Token implements Serializable {

    private static final long serialVersionUID = 384520235046067298L;

    public enum Type {
        EOF,

        /** [0-9a-zA-Z_'"]* Note, can start with [0-9] */
        IDENT,

        DOT,

        SEMI,

        /** Any quoted string, single and triple */
        QUOTED_STRING,

        /** '(' anything ')' */
        PAREN_BLOCK,

        /** '{' anything '}' */
        BRACE_BLOCK,

        /** '[' anything ']' */
        BRACK_BLOCK,

        /** '//' */
        LINE_COMMENT,

        /** '/star ... star/' */
        BLOCK_COMMENT,

        /** '\n', '\r', '\n\r', '\r\n' */
        LINE_BREAK,

        /** range definition 10..23, 10..<23 */
        DOUBLE_DOT,

        /** safe navigation operator ?. */
        SAFE_DEREF,

        /** spread operator *. */
        SPREAD,

        /** direct field access .@ */
        FIELD_ACCESS,

        /** method pointer .& */
        METHOD_POINTER,
    }

    private final int type;
    public final int startOffset;
    public final int endOffset;
    public final String text;

    public Token(Type type, int startOffset, int endOffset, String text) {
        this.type = type.ordinal();
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.text = text;
    }

    public Type getType() {
        return Type.values()[type];
    }

    public boolean isType(Type t) {
        return getType() == t;
    }

    public boolean isDotAccess() {
        switch (getType()) {
        case DOT:
        case SAFE_DEREF:
        case SPREAD:
        case FIELD_ACCESS:
        case METHOD_POINTER:
            return true;
        default:
            return false;
        }
    }

    public boolean isValidBeforeDot() {
        switch (getType()) {
        case IDENT:
        case QUOTED_STRING:
        case BRACE_BLOCK:
        case BRACK_BLOCK:
        case PAREN_BLOCK:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean equals(Object that) {
        if (that == this) return true;
        if (!(that instanceof Token)) return false;
        return ((Token) that).type == this.type;
    }

    @Override
    public int hashCode() {
        return getType().hashCode() + type;
    }

    @Override
    public String toString() {
        return getType() + "[" + startOffset + ":" + endOffset + "] - " + text;
    }
}
