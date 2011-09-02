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
package org.codehaus.groovy.eclipse.dsl.checker;

import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.editor.highlighting.SemanticReferenceRequestor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * The {@link StaticTypeCheckerRequestor} walks through a single ModuleNode and reports all unknown types.
 * It further handles type assertion statements of the form:
 * <pre>
 * expr // TYPE:java.lang.String
 * </pre>
 * 
 * Where:
 * <ul>
 * <li>expr is the expression whose type should be asserted
 * <li>java.lang.String is the expected type of the expression 
 * </ul>
 * 
 * Note that the <code>|| true</code> segment is required in order to ensure that the assertions do not fail at runtime.
 * 
 * @author andrew
 * @created Aug 28, 2011
 */
public class StaticTypeCheckerRequestor extends SemanticReferenceRequestor implements ITypeRequestor {

    private final IStaticCheckerHandler handler;
    
    private final Map<Integer, String> commentsMap;

    private final boolean onlyAssertions;

    StaticTypeCheckerRequestor(IStaticCheckerHandler handler, Map<Integer, String> commentsMap, boolean onlyAssertions) {
        this.handler = handler;
        this.commentsMap = commentsMap;
        this.onlyAssertions = onlyAssertions;
    }


    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (node instanceof BlockStatement) {
            if (((BlockStatement) node).getStatements() == null) {
                return VisitStatus.CANCEL_BRANCH;
            }
        }
        
        // ignore statements and declarations
        if (!(node instanceof AnnotatedNode)) {
            return VisitStatus.CONTINUE;
        }

        // ignore nodes with invalid slocs
        if (node.getEnd() <= 0 || (node.getStart() == 0 && node.getEnd() == 1)) {
            return VisitStatus.CONTINUE;
        }
        
        if (!onlyAssertions && result.confidence == TypeConfidence.UNKNOWN && node.getEnd() > 0) {
            handler.handleUnknownReference(node, getPosition(node), node.getLineNumber());
            return VisitStatus.CONTINUE;
        }
        
        String expectedType = commentsMap.remove(node.getLineNumber());
        if (expectedType != null && !typeMatches(result.type, expectedType)) {
            handler.handleTypeAssertionFailed(node, expectedType, printTypeName(result.type), getPosition(node), node.getLineNumber());
        }
        
        return VisitStatus.CONTINUE;
    }
    
    /**
     * @param type
     * @param expectedType
     * @return
     */
    private boolean typeMatches(ClassNode type, String expectedType) {
        String actualType = printTypeName(type);
        return expectedType.equals(actualType);
    }


    protected String printTypeName(ClassNode type) {
        return type != null ? type.getName() + printGenerics(type) : "null";
    }

    private String printGenerics(ClassNode type) {
        if (type.getGenericsTypes() == null || type.getGenericsTypes().length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < type.getGenericsTypes().length; i++) {
            GenericsType gt = type.getGenericsTypes()[i];
            sb.append(printTypeName(gt.getType()));
            if (i < type.getGenericsTypes().length-1) {
                sb.append(',');
            }
        }
        sb.append('>');
        return sb.toString();
    }
}
