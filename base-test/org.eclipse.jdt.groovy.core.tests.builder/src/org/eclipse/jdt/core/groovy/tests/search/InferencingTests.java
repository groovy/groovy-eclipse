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

import junit.framework.Test;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;

/**
 * Lots of tests to see that expressions have the proper type associated with them
 * @author Andrew Eisenberg
 * @created Nov 4, 2009
 *
 */
public class InferencingTests extends AbstractGroovySearchTest {
 
    public static Test suite() {
        return buildTestSuite(InferencingTests.class);
    }

    public class SearchRequestor implements ITypeRequestor {

        private final int start;
        private final int end;
        
        TypeLookupResult result;
        ASTNode node;
        
        public SearchRequestor(int start, int end) {
            super();
            this.start = start;
            this.end = end;
        }



        public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
                IJavaElement enclosingElement) {
            
            if (node.getStart() == start && node.getEnd() == end && !(node instanceof MethodNode /* ignore the run() method*/)) {
                this.result = result;
                this.node = node;
                return VisitStatus.STOP_VISIT;
            }
            return VisitStatus.CONTINUE;
        }
        
        String getTypeName() {
            return result.type.getName();
        }
    }

    public InferencingTests(String name) {
        super(name);
    }

    public void testInferNumber1() throws Exception {
        assertType("10", "java.lang.Integer");
    }

    public void testInferNumber2() throws Exception {
        assertType("1+2", "java.lang.Integer");
    }

    public void testInferNumber3() throws Exception {
        assertType("10L", "java.lang.Long");
    }
    
    public void testInferNumber4() throws Exception {
        assertType("10++", "java.lang.Number");
    }
    
    public void testInferNumber5() throws Exception {
        assertType("++10", "java.lang.Number");
    }
    
    public void testInferString1() throws Exception {
        assertType("\"10\"", "java.lang.String");
    }
    
    public void testInferString2() throws Exception {
        assertType("'10'", "java.lang.String");
    }
    
    public void testInferString3() throws Exception {
        String contents = "def x = '10'";
        assertType(contents, contents.indexOf('\''), contents.lastIndexOf('\'')+1, "java.lang.String");
    }
    
    public void testInferList1() throws Exception {
        assertType("[]", "java.util.List");
    }
    
    public void testInferList2() throws Exception {
        assertType("[] << \"\"", "java.util.List");
    }
    
    public void testInferMap1() throws Exception {
        assertType("[:]", "java.util.Map");
    }
    
    public void testInferBoolean1() throws Exception {
        assertType("!x", "java.lang.Boolean");
    }
    
    private void assertType(String contents, String expectedType) {
        assertType(contents, 0, contents.length(), expectedType);
    }
    
    private void assertType(String contents, int exprStart, int exprEnd, String expectedType) {
        GroovyCompilationUnit unit = createUnit("Search", contents);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
        SearchRequestor requestor = new SearchRequestor(exprStart, exprEnd);
        visitor.visitCompilationUnit(requestor);
        
        assertNotNull("Did not find expected ASTNode", requestor.node);
        if (! expectedType.equals(requestor.getTypeName())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected type not found.\n");
            sb.append("Expected: " + expectedType + "\n");
            sb.append("Found: " + requestor.getTypeName() + "\n");
            sb.append("Declaring type: " + requestor.result.declaringType.getName() + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            fail(sb.toString());
            
        }
    }
}
