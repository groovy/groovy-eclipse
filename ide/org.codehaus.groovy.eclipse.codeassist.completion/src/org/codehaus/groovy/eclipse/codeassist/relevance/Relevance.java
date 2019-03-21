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
package org.codehaus.groovy.eclipse.codeassist.relevance;

/**
 * Defines relative relevance classes of completion proposals.
 *
 * Note that the reason we are using factors of 10 is that JDT will make small
 * changes to the relevancy deep inside the Completion computer. By making the
 * differences between the gradations so large, we ensure that these small
 * changes have no effect on the final outcome.
 */
public enum Relevance {
    /**
     * Intended for keywords, packages, and types.
     */
    LOWEST,
    /**
     * Intended for Class, Object, and GroovyObject fields/methods/properties.
     */
    VERY_LOW,
    /**
     * Intended for ?.
     */
    LOW,
    /**
     * Intended for DGMs, DGSMs, and all others (default).
     */
    MEDIUM,
    /**
     * Intended for instance fields/methods/properties.
     */
    MEDIUM_HIGH,
    /**
     * Intended for local variables, method parameters, new get/set method proposals, and receiver type matches (good assignment candidates).
     */
    HIGH,
    /**
     * Named parameters, new field or method proposals, most recently used, or other special cases.
     */
    VERY_HIGH;

    @Deprecated
    public int getRelavance() {
        return getRelevance();
    }

    public int getRelevance() {
        return (int) Math.pow(10, ordinal());
    }

    /**
     * @param multiplier
     *            how many times the actual value multiplier is a float so that
     *            it is possible to reduce the relative relavance by passing in
     *            a value < 1
     * @return the actual relavance of the associated proposal
     */
    public int getRelevance(float multiplier) {
        return Math.max(1, (int) (getRelavance() * multiplier));
    }
}
