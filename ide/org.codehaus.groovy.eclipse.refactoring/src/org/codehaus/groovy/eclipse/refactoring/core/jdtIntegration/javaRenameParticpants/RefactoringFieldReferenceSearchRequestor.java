/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * @author andrew
 *
 */
public class RefactoringFieldReferenceSearchRequestor implements
        IRefactoringChangeRequestor {
    
    private IField target;
    private List<ITextSelection> locations = new ArrayList<ITextSelection>();

    public RefactoringFieldReferenceSearchRequestor(IField target) {
        this.target = target;
    }

    public List<ITextSelection> getMatchLocations() {
        return locations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.groovy.search.ITypeRequestor#acceptASTNode(org.codehaus.groovy.ast.ASTNode, org.eclipse.jdt.groovy.search.TypeLookupResult, org.eclipse.jdt.core.IJavaElement)
     */
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
            IJavaElement enclosingElement) {
        if (checkDeclaringType(result)) {
            if (node instanceof FieldExpression) {
                if (((FieldExpression) node).getFieldName().equals(target.getElementName())) {
                    locations.add(new TextSelection(node.getStart(), node.getEnd()-node.getStart()));
                }
            } else if (node instanceof VariableExpression) {
                if (((VariableExpression) node).getName().equals(target.getElementName())) {
                    locations.add(new TextSelection(node.getStart(), ((VariableExpression) node).getName().length()));
                }
            } else if (node instanceof ConstantExpression) {
                if (((ConstantExpression) node).getText().equals(target.getElementName())) {
                    locations.add(new TextSelection(node.getStart(), node.getEnd()-node.getStart()));
                }
            }
        }
        
        return VisitStatus.CONTINUE;
    }

    /**
     * @param result
     * @return
     */
    private boolean checkDeclaringType(TypeLookupResult result) {
        return result.declaringType != null && result.declaringType.getName().equals(target.getDeclaringType().getFullyQualifiedName());
    }

    
}
