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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * 
 * @author Andrew Eisenberg
 * @created Apr 1, 2010
 */
public class LocalVariableReferenceRequestor implements ITypeRequestor {

    private List<IRegion> references;
    private IJavaElement enclosingElement = null;
    private char[] contents;
    private boolean foundEnclosingElement = false;
    private String variableName;
    public LocalVariableReferenceRequestor(Variable variable, IJavaElement enclosingElement) {
        references = new ArrayList<IRegion>();
        this.enclosingElement = enclosingElement;
        GroovyCompilationUnit enclosingUnit = (GroovyCompilationUnit) enclosingElement.getAncestor(IJavaElement.COMPILATION_UNIT);
        contents = enclosingUnit.getContents();
        variableName = variable.getName();
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
            IJavaElement enclosingElement) {
        if (enclosingElement.equals(this.enclosingElement)) {
            foundEnclosingElement = true;
            if (node instanceof Variable && ((Variable) node).getName().equals(variableName)) {
                references.add(getRealSourceLocation(node));
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
     * @param node
     * @return
     */
    private IRegion getRealSourceLocation(ASTNode node) {
        int start = node.getStart();
        // the start location for elements in GStrings includes the opening bracket
        if (contents[node.getStart()] == '{') {
            start++;
        }
        return new Region(start, variableName.length());
    }

    public List<IRegion> getReferences() {
        return references;
    }
}
