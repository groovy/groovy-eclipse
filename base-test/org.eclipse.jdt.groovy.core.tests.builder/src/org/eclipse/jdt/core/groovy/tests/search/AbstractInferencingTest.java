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

package org.eclipse.jdt.core.groovy.tests.search;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * @author Andrew Eisenberg
 * @created Nov 13, 2009
 */
public abstract class AbstractInferencingTest extends AbstractGroovySearchTest {

    public AbstractInferencingTest(String name) {
        super(name);
    }


    protected void assertType(String contents, String expectedType) {
        assertType(contents, 0, contents.length(), expectedType, false);
    }

    protected void assertType(String contents, int exprStart, int exprEnd,
            String expectedType) {
        assertType(contents, exprStart, exprEnd, expectedType, false);
    }
    protected void assertType(String contents, String expectedType, boolean forceWorkingCopy) {
        assertType(contents, 0, contents.length(), expectedType, forceWorkingCopy);
    }

    protected void assertType(String contents, int exprStart, int exprEnd,
            String expectedType, boolean forceWorkingCopy) {
        assertType(contents, exprStart, exprEnd, expectedType, null, forceWorkingCopy);
    }
    
    protected void assertType(String contents, int exprStart, int exprEnd,
            String expectedType, String extraDocSnippet, boolean forceWorkingCopy) {
        GroovyCompilationUnit unit = createUnit("Search", contents);
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, forceWorkingCopy);
        
        assertNotNull("Did not find expected ASTNode", requestor.node);
        if (! expectedType.equals(printTypeName(requestor.result.type))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected type not found.\n");
            sb.append("Expected: " + expectedType + "\n");
            sb.append("Found: " + printTypeName(requestor.result.type) + "\n");
            sb.append("Declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            fail(sb.toString());
        }
        
        if (extraDocSnippet != null && ! requestor.result.extraDoc.contains(extraDocSnippet)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Incorrect Doc found.\n");
            sb.append("Expected doc should contain: " + extraDocSnippet + "\n");
            sb.append("Found: " + requestor.result.extraDoc + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            fail(sb.toString());
        }
        
        // this is from https://issuetracker.springsource.com/browse/STS-1854
        // make sure that the Type parameterization of Object has not been messed up
        assertNull("Problem!!! Object type has type parameters now.  See STS-1854", VariableScope.OBJECT_CLASS_NODE.getGenericsTypes());
    }
    
    protected SearchRequestor doVisit(int exprStart, int exprEnd, GroovyCompilationUnit unit, boolean forceWorkingCopy) {
        try {
            if (forceWorkingCopy) {
                unit.becomeWorkingCopy(null);
            }
            try {
                TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
                SearchRequestor requestor = new SearchRequestor(exprStart, exprEnd);
                visitor.visitCompilationUnit(requestor);
                return requestor;
            } finally {
                if (forceWorkingCopy) {
                    unit.discardWorkingCopy();
                }
            }
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    protected void assertDeclaringType(String contents, int exprStart, int exprEnd,
            String expectedDeclaringType) {
        assertDeclaringType(contents, exprStart, exprEnd, expectedDeclaringType, false);
    }
    
    protected enum DeclarationKind { FIELD, METHOD, PROPERTY, CLASS }
    protected void assertDeclaration(String contents, int exprStart, int exprEnd,
            String expectedDeclaringType, String declarationName, DeclarationKind kind) {
        assertDeclaringType(contents, exprStart, exprEnd, expectedDeclaringType, false, false);
        GroovyCompilationUnit unit = createUnit("Search", contents);
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, false);
        
        switch (kind) {
            case FIELD:
                assertTrue("Expecting field, but was " + requestor.result.declaration, 
                        requestor.result.declaration instanceof FieldNode);
                assertEquals("Wrong field name", declarationName, ((FieldNode) requestor.result.declaration).getName());
                break;
            case METHOD:
                assertTrue("Expecting method, but was " + requestor.result.declaration, 
                        requestor.result.declaration instanceof MethodNode);
                assertEquals("Wrong method name", declarationName, ((MethodNode) requestor.result.declaration).getName());
                break;
            case PROPERTY:
                assertTrue("Expecting property, but was " + requestor.result.declaration, 
                        requestor.result.declaration instanceof PropertyNode);
                assertEquals("Wrong property name", declarationName, ((PropertyNode) requestor.result.declaration).getName());
                break;
            case CLASS:
                assertTrue("Expecting class, but was " + requestor.result.declaration, 
                        requestor.result.declaration instanceof ClassNode);
                assertEquals("Wrong class name", declarationName, ((ClassNode) requestor.result.declaration).getName());
                
        }
        
    }
    
    protected void assertDeclaringType(String contents, int exprStart, int exprEnd,
            String expectedDeclaringType, boolean forceWorkingCopy) {
        
        assertDeclaringType(contents, exprStart, exprEnd, expectedDeclaringType, forceWorkingCopy, false);
    }
    protected void assertDeclaringType(String contents, int exprStart, int exprEnd,
            String expectedDeclaringType, boolean forceWorkingCopy, boolean expectingUnknown) {
        GroovyCompilationUnit unit = createUnit("Search", contents);
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, forceWorkingCopy);
        
        assertNotNull("Did not find expected ASTNode", requestor.node);
        if (! expectedDeclaringType.equals(requestor.getDeclaringTypeName())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected declaring type not found.\n");
            sb.append("Expected: " + expectedDeclaringType + "\n");
            sb.append("Found type: " + printTypeName(requestor.result.type) + "\n");
            sb.append("Found declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            fail(sb.toString());
        }
        if (expectingUnknown) {
            if (requestor.result.confidence != TypeConfidence.UNKNOWN) {
                StringBuilder sb = new StringBuilder();
                sb.append("Confidence: " + requestor.result.confidence + " (but expecting UNKNOWN)\n");
                sb.append("Expected: " + expectedDeclaringType + "\n");
                sb.append("Found: " + printTypeName(requestor.result.type) + "\n");
                sb.append("Declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
                sb.append("ASTNode: " + requestor.node + "\n");
                fail(sb.toString());
            }
        } else {
            if (requestor.result.confidence == TypeConfidence.UNKNOWN) {
                StringBuilder sb = new StringBuilder();
                sb.append("Expected Confidence should not have been UNKNOWN, but it was.\n");
                sb.append("Expected declaring type: " + expectedDeclaringType + "\n");
                sb.append("Found type: " + printTypeName(requestor.result.type) + "\n");
                sb.append("Found declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
                sb.append("ASTNode: " + requestor.node + "\n");
                fail(sb.toString());
            }
        }
    }
    
    protected void assertUnknownConfidence(String contents, int exprStart, int exprEnd,
            String expectedDeclaringType, boolean forceWorkingCopy) {
        GroovyCompilationUnit unit = createUnit("Search", contents);
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, forceWorkingCopy);
        
        assertNotNull("Did not find expected ASTNode", requestor.node);
        if (requestor.result.confidence != TypeConfidence.UNKNOWN) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expecting unknown confidentce, but was " + requestor.result.confidence + ".\n");
            sb.append("Expected: " + expectedDeclaringType + "\n");
            sb.append("Found: " + printTypeName(requestor.result.type) + "\n");
            sb.append("Declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            fail(sb.toString());
        }
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

    public class SearchRequestor implements ITypeRequestor {

        private final int start;
        private final int end;
        
        public TypeLookupResult result;
        public ASTNode node;
        
        public SearchRequestor(int start, int end) {
            super();
            this.start = start;
            this.end = end;
        }

        public VisitStatus acceptASTNode(ASTNode visitorNode, TypeLookupResult visitorResult,
                IJavaElement enclosingElement) {
            
            if (visitorNode.getStart() == start && visitorNode.getEnd() == end && 
                    !(visitorNode instanceof MethodNode /* ignore the run() method*/) &&
                    !(visitorNode instanceof Statement /* ignore all statements */) &&
                    !(visitorNode instanceof ClassNode && ((ClassNode) visitorNode).isScript() /* ignore the script */ )) {
                this.result = visitorResult;
                this.node = visitorNode;
                return VisitStatus.STOP_VISIT;
            }
            return VisitStatus.CONTINUE;
        }
        
        public String getDeclaringTypeName() {
            return printTypeName(result.declaringType);
        }
        
        public String getTypeName() {
            return result.type.getName();
        }
    }
}