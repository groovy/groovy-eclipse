/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse;

import java.util.Arrays;

/**
 * 
 * @author Andrew Eisenberg
 * @created Nov 24, 2010
 */
@SuppressWarnings("nls")
public enum TraceCategory {

    DEFAULT("_"), CLASSPATH("Classpath"),
    REFACTORING("Refactoring"), COMPILER("Compiler"), DSL("DSL"), CODESELECT("Code select"), CONTENT_ASSIST("Content assist"),AST_TRANSFORM("Ast Transforms");
    
    TraceCategory(String label) {
        this.label = label;
    }
    
    public final String label;
    
    private String paddedLabel;
    
    public String getPaddedLabel() {
        if (paddedLabel == null) {
            synchronized(TraceCategory.class) {
                if (longestLabel == -1) {
                    calculateLongest();
                }
            }
            int extraSpace = longestLabel - label.length();
            paddedLabel = spaces(extraSpace) + label; 
        }
        return paddedLabel;
    }
    
    private String spaces(int extraSpace) {
        char[] a = new char[extraSpace];
        Arrays.fill(a, ' ');
        return new String(a);
    }

    private static void calculateLongest() {
        int maybeLongest = longestLabel;
        for (TraceCategory category : values()) {
            maybeLongest = Math.max(category.label.length(), maybeLongest);
        }
        longestLabel = maybeLongest;
    }

    private static String[] stringValues;
    public static String[] stringValues() {
        if (stringValues == null) {
            TraceCategory[] values = values();
            stringValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                stringValues[i] = values[i].label;
            }
        }
        return stringValues;
    }
    
    private static int longestLabel = -1;
}
