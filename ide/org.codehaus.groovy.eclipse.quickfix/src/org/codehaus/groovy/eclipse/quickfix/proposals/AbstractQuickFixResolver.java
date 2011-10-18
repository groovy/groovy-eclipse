/*
 * Copyright 2010-2011 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Base Groovy quick fix resolver that makes it more convenient for concrete
 * resolvers to define which problem types this resolver can handle.
 * <p>
 * This resolver is passed an actual quick fix problem context representing the
 * problem encountered in the resource which the quick fix proposals can then
 * reference when resolving the problem
 * </p>
 * 
 * @author Nieraj Singh
 * 
 */
public abstract class AbstractQuickFixResolver implements IQuickFixResolver {
	private List<ProblemType> problemTypes;
	private QuickFixProblemContext problem;

	/**
	 * 
	 * @param problem
	 *            the actual quick fix problem that this resolver should
	 *            resolve.
	 */
	protected AbstractQuickFixResolver(QuickFixProblemContext problem) {
		this.problem = problem;
	}

	/**
	 * 
	 * @return non-null quick fix problem representing the actual Groovy or Java
	 *         problem that has been encountered in the resource
	 */
	protected QuickFixProblemContext getQuickFixProblem() {
		return problem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver#
	 * getProblemTypes()
	 */
	public List<ProblemType> getProblemTypes() {
		if (problemTypes == null) {
			problemTypes = new ArrayList<ProblemType>();
			ProblemType[] types = getTypes();
			if (types != null) {
				for (ProblemType type : types) {
					if (type != null && !problemTypes.contains(type)) {
						problemTypes.add(type);
					}
				}
			}
		}
		return problemTypes;
	}

	protected IType[] getContextTypes() {
		QuickFixProblemContext context = getQuickFixProblem();
		if (context != null) {
			ICompilationUnit unit = context.getCompilationUnit();
			if (unit != null) {
				try {
					return unit.getAllTypes();
				} catch (JavaModelException e) {
					// do nothing
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @return non null, non empty array of problem types that this resolver can
	 *         handle
	 */
	protected abstract ProblemType[] getTypes();

}
