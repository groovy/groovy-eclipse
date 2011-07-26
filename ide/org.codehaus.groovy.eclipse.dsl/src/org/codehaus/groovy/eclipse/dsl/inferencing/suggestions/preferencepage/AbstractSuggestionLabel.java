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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.preferencepage;

/**
 * 
 * @author Nieraj Singh
 * @created Apr 21, 2011
 */
public abstract class AbstractSuggestionLabel implements ISuggestionLabel {

	protected static final String EMPTY_SPACE = " ";
	protected static final String COLON = ":";
	protected static final String OPEN_PAR = "(";
	protected static final String CLOSE_PAR = ")";
	protected static final String COMMA = ",";

	private String displayName;

	public String getLabel() {
		if (displayName == null) {
			displayName = constructName();
			if (displayName == null) {
				displayName = "";
			}
		}
		return displayName;
	}

	protected abstract String constructName();
}
