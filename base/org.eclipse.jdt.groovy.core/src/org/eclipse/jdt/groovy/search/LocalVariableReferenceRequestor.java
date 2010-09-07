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
package org.eclipse.jdt.groovy.search;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.LocalVariableReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * 
 * @author Andrew Eisenberg
 * @created Apr 1, 2010
 */
public class LocalVariableReferenceRequestor implements ITypeRequestor {

	private List<IRegion> references;
	private SearchRequestor requestor;
	private IJavaElement enclosingElement = null;
	private boolean foundEnclosingElement = false;
	private String variableName;
	private int declStart;
	private SearchParticipant participant;

	public LocalVariableReferenceRequestor(Variable variable, IJavaElement enclosingElement) {
		this(variable.getName(), enclosingElement, null, null, -1);
	}

	public LocalVariableReferenceRequestor(String name, IJavaElement enclosingElement, SearchRequestor requestor,
			SearchParticipant participant, int declStart) {
		references = new ArrayList<IRegion>();
		this.enclosingElement = enclosingElement;
		variableName = name;

		this.declStart = declStart;
		this.requestor = requestor;
		this.participant = participant;
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		if (enclosingElement.equals(this.enclosingElement)) {
			foundEnclosingElement = true;
			if (node instanceof Variable && ((Variable) node).getName().equals(variableName)) {
				IRegion realSourceLocation = getRealSourceLocation(node);
				references.add(realSourceLocation);
				if (requestor != null && realSourceLocation.getOffset() >= declStart) {
					try {
						requestor.acceptSearchMatch(new LocalVariableReferenceMatch(enclosingElement, SearchMatch.A_ACCURATE,
								realSourceLocation.getOffset(), realSourceLocation.getLength(), true /* isReadAccess */,
								true /* isWriteAccess */, false, participant, enclosingElement.getResource()));
					} catch (CoreException e) {
						Util.log(e);
					}
				}
			}
		} else {
			if (foundEnclosingElement) {
				// end the visit once we have visited the element we are looking for.
				return VisitStatus.STOP_VISIT;
			}
		}
		return VisitStatus.CONTINUE;
	}

	/**
	 * Different behavior if selecting a parameter definition
	 * 
	 * @param node
	 * @return
	 */
	private IRegion getRealSourceLocation(ASTNode node) {
		if (node instanceof Parameter) {
			Parameter parameter = (Parameter) node;
			return new Region(parameter.getNameStart(), parameter.getNameEnd() - parameter.getNameStart());
		}
		return new Region(node.getStart(), variableName.length());
	}

	public List<IRegion> getReferences() {
		return references;
	}
}
