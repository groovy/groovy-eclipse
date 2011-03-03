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
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Checks to make sure that a rename request for a local variable is not shadowing an existing name
 * @author Andrew Eisenberg
 * @created Apr 1, 2010
 */
public class LocalVariableNameCheckerRequestor implements ITypeRequestor {

    private final Variable variable;

    private final int start;

    private final int end;

    private final String newName;
    private boolean shadowing = false;
    private boolean variableFound = false;
    private IJavaElement foundEnclosingElement = null;
    public LocalVariableNameCheckerRequestor(Variable variable, String newName) {
        this.variable = variable;
        this.newName = newName;

        if (variable instanceof ASTNode) {
            ASTNode v = (ASTNode) variable;
            start = v.getStart();
            end = v.getEnd();
        } else {
            start = end = -1;
        }
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
            IJavaElement enclosingElement) {

        // check to see if the enclosing element does not enclose the
        // nodeToLookFor
        if (!interestingElement(enclosingElement)) {
            return VisitStatus.CANCEL_MEMBER;
        }

        // only start looking for shadows when we've hit the variable that we are renaming
        if (!variableFound) {
            if (node == variable) {
                variableFound = true;
                foundEnclosingElement = enclosingElement;
            }
            return VisitStatus.CONTINUE;
        }
        // ensure that we don't keep looking for shadows after we've left the enclosing element.
        if (enclosingElement != foundEnclosingElement) {
            return VisitStatus.STOP_VISIT;
        }
        if (node instanceof Variable) {
            Variable other = (Variable) node;
            if (other.getName().equals(newName)) {
                if (! (other instanceof VariableExpression) || ((VariableExpression) other).getAccessedVariable() != variable) {
                    shadowing = true;
                    return VisitStatus.STOP_VISIT;
                }
            }
        }

        if (node instanceof ConstantExpression) {
            ConstantExpression con = (ConstantExpression) node;
            if (con.getText().equals(variable.getName()) && result.declaration != variable) {
                shadowing = true;
                return VisitStatus.STOP_VISIT;
            }
        }

        return VisitStatus.CONTINUE;
    }

    public boolean isShadowing() {
        return shadowing;
    }

    /**
     * @param enclosingElement
     * @return true iff enclosingElement's source location contains the source
     *         location of {@link #variable}
     */
    private boolean interestingElement(IJavaElement enclosingElement) {
        // the clinit is always interesting since the clinit contains static
        // initializers
        if (enclosingElement.getElementName().equals("<clinit>")) {
            return true;
        }

        if (start >= 0 && end >= 0 && enclosingElement instanceof ISourceReference) {
            try {
                ISourceRange range = ((ISourceReference) enclosingElement).getSourceRange();
                return range.getOffset() <= start && range.getOffset() + range.getLength() >= end;
            } catch (JavaModelException e) {
                Util.log(e);
            }
        }
        return false;
    }

}
