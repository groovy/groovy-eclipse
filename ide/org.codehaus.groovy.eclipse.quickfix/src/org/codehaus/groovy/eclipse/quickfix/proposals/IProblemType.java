/*
 * Copyright 2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

/**
 * A descriptor that represents a Java or Eclipse resource problem (compilation
 * problem, etc..) which the Groovy quick fix framework can understand.
 * 
 * @author Nieraj Singh
 * 
 */
public interface IProblemType {
	/**
	 * Returns the marker type of this problem.
	 * 
	 * @return The marker type of the problem.
	 */
	public String getMarkerType();

	/**
	 * Returns the id of problem. Note that problem ids are defined per problem
	 * marker type. See {@link org.eclipse.jdt.core.compiler.IProblem} for id
	 * definitions for problems of type
	 * <code>org.eclipse.jdt.core.problem</code> and
	 * <code>org.eclipse.jdt.core.task</code>.
	 * 
	 * @return The id of the problem.
	 */
	public int getProblemID();
}
