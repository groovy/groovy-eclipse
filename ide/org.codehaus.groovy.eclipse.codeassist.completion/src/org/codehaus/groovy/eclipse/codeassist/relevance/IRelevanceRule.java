/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.relevance;

import org.eclipse.jdt.core.IType;

/**
 * Computes the relevance of a type. The higher the value the higher the
 * relevance. If no relevance can be determined, zero MUST be returned.
 *
 * @author Nieraj Singh
 * @created 2011-02-17
 */
public interface IRelevanceRule {

	/**
	 * Relevance value of a type. Higher relevance should be indicated by a
	 * higher integer value. If no relevance can be computed, a value of zero
	 * MUST be returned.
	 *
	 * @param relevanceType
	 *            whose relevance must be computed. Must not be null
	 * @param contextTypes
	 *            context types where a relevance calculation is requested, for
	 *            example the top level type where the relevance type needs to
	 *            be resolved. Context types should all be types in the same
	 *            compilation unit.
	 * @return positive value, with a higher value indicating higher relevance,
	 *         or zero if relevance cannot be computed
	 */
	public int getRelevance(IType relevanceType, IType[] contextTypes);

    /**
     * Relevance value of a type. Higher relevance should be indicated by a higher integer value. If
     * no relevance can be computed, a value of zero MUST be returned.
     * 
     * @param fullyQualifiedName type name whose relevance must be computed. Must not be null
     * @param contextTypes context types where a relevance calculation is requested, for example the
     *            top level type where the relevance type needs to be resolved. Context types should
     *            all be types in the same compilation unit.
     * @param accessibility TODO
     * @param modifiers TODO
     * @return positive value, with a higher value indicating higher relevance, or zero if relevance
     *         cannot be computed
     */
    public int getRelevance(char[] fullyQualifiedName, IType[] contextTypes, int accessibility, int modifiers);

}