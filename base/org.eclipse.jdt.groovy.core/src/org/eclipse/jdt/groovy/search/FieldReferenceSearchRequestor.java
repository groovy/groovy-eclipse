/*
 * Copyright 2009-2018 the original author or authors.
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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.core.search.matching.FieldPattern;
import org.eclipse.jdt.internal.core.search.matching.VariablePattern;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Position;

public class FieldReferenceSearchRequestor implements ITypeRequestor {

    protected final SearchRequestor requestor;
    protected final SearchParticipant participant;

    protected final String fieldName, declaringQualifiedName;
    protected final Set<Position> acceptedPositions = new HashSet<>();
    protected final boolean readAccess, writeAccess, findReferences, findDeclarations;

    public FieldReferenceSearchRequestor(FieldPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
        this.requestor = requestor;
        this.participant = participant;

        char[] arr = ReflectionUtils.getPrivateField(VariablePattern.class, "name", pattern);
        fieldName = String.valueOf(arr);
        arr = ReflectionUtils.getPrivateField(FieldPattern.class, "declaringSimpleName", pattern);
        String declaringSimpleName = ((arr == null || arr.length == 0) ? "" : String.valueOf(arr));
        arr = ReflectionUtils.getPrivateField(FieldPattern.class, "declaringQualification", pattern);
        String declaringQualification = ((arr == null || arr.length == 0) ? "" : (String.valueOf(arr) + "."));
        declaringQualifiedName = declaringQualification + declaringSimpleName;

        readAccess = (Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "readAccess", pattern);
        writeAccess = (Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "writeAccess", pattern);
        findReferences = (Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "findReferences", pattern);
        findDeclarations = (Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "findDeclarations", pattern);
    }

    @Override
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        boolean doCheck = false;
        boolean isAssignment = false;
        boolean isDeclaration = false;
        int start = 0;
        int end = 0;

        if (node instanceof ConstantExpression) {
            // check for "foo.bar" where "bar" refers to "getBar()" or "setBar(...)" with backing field or property
            if (fieldName.equals(((ConstantExpression) node).getText()) && (result.confidence == TypeConfidence.UNKNOWN ||
                    result.declaringType.getField(fieldName) != null || result.declaringType.getProperty(fieldName) != null)) {
                doCheck = true;
                isAssignment = EqualityVisitor.checkForAssignment(node, result.enclosingAssignment);
                start = node.getStart();
                end = node.getEnd();
            }
        } else if (node instanceof FieldExpression) {
            if (fieldName.equals(((FieldExpression) node).getFieldName())) {
                doCheck = true;
                isAssignment = EqualityVisitor.checkForAssignment(node, result.enclosingAssignment);
                // fully-qualified field expressions in static contexts will have an sloc of the entire qualified name
                start = node.getEnd() - fieldName.length();
                end = node.getEnd();
            }
        } else if (node instanceof FieldNode) {
            FieldNode fnode = (FieldNode) node;
            if (fieldName.equals(fnode.getName())) {
                doCheck = true;
                // assume all FieldNodes are assignments -- not true if there is no initializer, but we
                // can't know this at this point since initializer has already been moved to the <init>
                isAssignment = true;
                isDeclaration = true;
                start = fnode.getNameStart();
                end = fnode.getNameEnd() + 1; // arrrgh...why +1?
            }
        } else if (node instanceof VariableExpression) {
            if (fieldName.equals(((VariableExpression) node).getName()) &&
                    (result.declaration instanceof FieldNode || result.declaration instanceof PropertyNode)) {
                doCheck = true;
                isAssignment = EqualityVisitor.checkForAssignment(node, result.enclosingAssignment);
                start = node.getStart();
                end = node.getEnd();
            }
        }

        if (doCheck && end > 0 && result.declaringType != null) {
            // don't want to double accept nodes; this could happen with field and object initializers can get pushed into multiple constructors
            Position position = new Position(start, end - start);
            if (!acceptedPositions.contains(position)) {
                boolean isCompleteMatch = qualifiedNameMatches(GroovyUtils.getBaseType(result.declaringType));
                // GRECLIPSE-540: Still unresolved is that all field and variable references are considered reads. We don't know about writes.
                if (isCompleteMatch && ((isAssignment && writeAccess) || (!isAssignment && readAccess) || (isDeclaration && findDeclarations))) {
                    SearchMatch match = null;
                    // must translate from synthetic source to binary if necessary
                    if (enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy)
                        enclosingElement = ((GroovyClassFileWorkingCopy) enclosingElement.getOpenable()).convertToBinary(enclosingElement);
                    if (isDeclaration && findDeclarations) {
                        match = new FieldDeclarationMatch(enclosingElement, getAccuracy(result.confidence, isCompleteMatch), start, end - start, participant, enclosingElement.getResource());
                    } else if (!isDeclaration && findReferences) {
                        match = new FieldReferenceMatch(enclosingElement, getAccuracy(result.confidence, isCompleteMatch), start, end - start, !isAssignment, isAssignment, false, participant, enclosingElement.getResource());
                    }
                    if (match != null) {
                        try {
                            requestor.acceptSearchMatch(match);
                            acceptedPositions.add(position);
                        } catch (CoreException e) {
                            Util.log(e, "Error reporting search match inside of " + enclosingElement + " in resource " + enclosingElement.getResource());
                        }
                    }
                }
            }
        }
        return VisitStatus.CONTINUE;
    }

    // recursively check the hierarchy
    private boolean qualifiedNameMatches(ClassNode declaringType) {
        if (declaringType == null) {
            // no declaring type; probably a variable declaration
            return false;
        } else if (declaringQualifiedName.isEmpty()) {
            // no type specified, accept all
            return true;
        } else if (declaringType.getName().equals(declaringQualifiedName)) {
            return true;
        } else {
            return false;
        }
    }

    private int getAccuracy(TypeConfidence confidence, boolean isCompleteMatch) {
        if (shouldAlwaysBeAccurate()) {
            return SearchMatch.A_ACCURATE;
        }
        if (!isCompleteMatch) {
            return SearchMatch.A_INACCURATE;
        }
        switch (confidence) {
        case EXACT:
            return SearchMatch.A_ACCURATE;
        default:
            return SearchMatch.A_INACCURATE;
        }
    }

    /**
     * Checks to see if this requestor has something to do with refactoring.
     * If so, we always want an accurate match otherwise we get complaints
     * in the refactoring wizard of "possible matches".
     */
    private boolean shouldAlwaysBeAccurate() {
        return requestor.getClass().getPackage().getName().indexOf("refactoring") != -1;
    }
}
