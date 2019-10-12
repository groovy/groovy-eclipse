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
package org.eclipse.jdt.groovy.core.util;

import org.eclipse.jdt.core.dom.AST;

public abstract class JavaConstants {

    /** Highest supported Java Language Specification (JLS) level. */
    public static int AST_LEVEL = AST.JLS10;
    static {
        try {
            AST.class.getDeclaredField("JLS13");
            AST_LEVEL = 13;
        } catch (NoSuchFieldException ignore13) {
            try {
                AST.class.getDeclaredField("JLS12");
                AST_LEVEL = 12;
            } catch (NoSuchFieldException ignore12) {
                try {
                    AST.class.getDeclaredField("JLS11");
                    AST_LEVEL = 11;
                } catch (NoSuchFieldException ignore11) {
                }
            }
        }
    }
}
