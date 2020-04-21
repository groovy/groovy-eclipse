/*
 * Copyright 2009-2020 the original author or authors.
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
package org.eclipse.jdt.groovy.search;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.LocalVariableReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class LocalVariableReferenceRequestor implements ITypeRequestor {

    private final List<IRegion> references = new ArrayList<>();

    private Variable variable;
    private String variableName;
    private IJavaElement enclosingElement;
    private boolean foundEnclosingElement;

    private int declStart;
    private SearchRequestor requestor;
    private SearchParticipant participant;

    public LocalVariableReferenceRequestor(final Variable variable, final IJavaElement enclosingElement) {
        this(variable.getName(), enclosingElement, null, null, -1);
        this.variable = variable;
    }

    public LocalVariableReferenceRequestor(final String variableName, final IJavaElement enclosingElement,
                final SearchRequestor requestor, final SearchParticipant participant, final int declStart) {
        this.variableName = variableName;
        this.enclosingElement = enclosingElement;

        this.declStart = declStart;
        this.requestor = requestor;
        this.participant = participant;
    }

    public List<IRegion> getReferences() {
        return references;
    }

    @Override
    public VisitStatus acceptASTNode(final ASTNode node, final TypeLookupResult result, final IJavaElement enclosingElement) {
        if (enclosingElement.equals(this.enclosingElement)) {
            foundEnclosingElement = true;
            if (node instanceof Variable && isMatchForVariable((Variable) node)) {
                IRegion region = new Region(node instanceof Parameter ? ((Parameter) node).getNameStart() : node.getStart(), variableName.length());
                references.add(region);
                if (requestor != null && region.getOffset() >= declStart) {
                    try {
                        requestor.acceptSearchMatch(new LocalVariableReferenceMatch(
                            enclosingElement, SearchMatch.A_ACCURATE, region.getOffset(), region.getLength(),
                            /*isReadAccess:*/true, /*isWriteAccess:*/true, /*insideDocComment:*/false, participant, enclosingElement.getResource()));
                    } catch (CoreException e) {
                        Util.log(e);
                    }
                }
            }
        } else if (foundEnclosingElement) {
            // end the visit once we have visited the element we are looking for
            return VisitStatus.STOP_VISIT;
        }
        return VisitStatus.CONTINUE;
    }

    private boolean isMatchForVariable(final Variable var) {
        if (variable != null) {
            if (var instanceof VariableExpression) {
                return ((VariableExpression) var).getAccessedVariable() == variable;
            }
            return var == variable;
        }
        return var.getName().equals(variableName);
    }
}
