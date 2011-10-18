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

import java.util.List;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 * Groovy problems are handled by implementing a Quick Fix resolver, which is
 * associated with a group of problem types, meaning that a resolver can
 * potentially handle different Groovy problems, as well as provide a list of Java
 * completion proposals, in case the resolver is able to present different
 * options on how to resolve the same problem. There is no assumed or expected
 * mapping between each different type and each Java completion proposal.
 * <p>
 * In fact, the resolver should be defined against a set of problem types for
 * which the solution is the same. Therefore, for each problem type, the
 * list of Java completion proposals should be the same. If they are different,
 * then different resolvers should be implemented.
 * 
 * </p>
 * 
 * @author Nieraj Singh
 * 
 */
public interface IQuickFixResolver {

	/**
	 * 
	 * @return non null list of problem type that this resolver can handle
	 */
	public List<ProblemType> getProblemTypes();

	/**
	 * 
	 * @return non null list of Java completion proposals that can handle each
	 *         of the problem descriptors associated with this resolver
	 */
	public List<IJavaCompletionProposal> getQuickFixProposals();

}
