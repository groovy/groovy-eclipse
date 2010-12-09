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
public interface IProblemDescriptor {

	/**
	 * A problem type represents a particular kind of problem, for example, an
	 * unresolved Java type, or a syntax error like a missing ";" at the end of
	 * a statement.
	 * 
	 * @return the problem type associated with this descriptor.
	 */
	public IProblemType getType();

	/**
	 * Returns the original arguments recorded into the problem.
	 * 
	 * @return String[] Returns the problem arguments.
	 */
	public String[] getMarkerMessages();

}