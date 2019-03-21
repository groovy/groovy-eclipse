/*
 * Copyright 2009-2016 the original author or authors.
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

import org.eclipse.jdt.core.dom.AST;

public abstract class JavaConstants {

    /** Highest supported Java Language Specification (JLS) level. */
    public static final int AST_LEVEL;
    static {
        @SuppressWarnings("deprecation")
        int astLevel = AST.JLS3;
        try {
            AST.class.getDeclaredField("JLS8");
            astLevel = 8;
        } catch (NoSuchFieldException nsfe) {
            // pre-java8
        }
        AST_LEVEL = astLevel;
    }
}
