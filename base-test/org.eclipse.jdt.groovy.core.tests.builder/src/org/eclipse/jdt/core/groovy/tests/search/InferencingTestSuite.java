/*
 * Copyright 2009-2021 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;

public abstract class InferencingTestSuite extends SearchTestSuite {

    protected static final String DEFAULT_UNIT_NAME = "Search";

    protected void assertType(String script, String expectedType) {
        assertType(script, 0, script.length(), expectedType);
    }

    protected void assertType(String source, String target, String expectedType) {
        final int offset = source.lastIndexOf(target);
        assertType(source, offset, offset + target.length(), expectedType);
    }

    protected void assertType(String contents, int exprStart, int exprUntil, String expectedType) {
        GroovyCompilationUnit unit = createUnit(DEFAULT_UNIT_NAME, contents);
        assertType(unit, exprStart, exprUntil, expectedType, null);
    }

    public static void assertType(GroovyCompilationUnit unit, int exprStart, int exprUntil, String expectedType) {
        assertType(unit, exprStart, exprUntil, expectedType, null);
    }

    public static void assertType(GroovyCompilationUnit unit, int exprStart, int exprUntil, String expectedType, String extraDocSnippet) {
        SearchRequestor requestor = doVisit(exprStart, exprUntil, unit);

        assertNotNull("Did not find expected ASTNode", requestor.node);
        if (!expectedType.equals(printTypeName(requestor.result.type))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected type not found.\n");
            sb.append("Expected: " + expectedType + "\n");
            sb.append("Found: " + printTypeName(requestor.result.type) + "\n");
            sb.append("Declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            sb.append("Confidence: " + requestor.result.confidence + "\n");
            fail(sb.toString());
        }

        if (extraDocSnippet != null && (requestor.result.extraDoc == null || !requestor.result.extraDoc.replace("}", "").contains(extraDocSnippet))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Incorrect Doc found.\n");
            sb.append("Expected doc should contain: " + extraDocSnippet + "\n");
            sb.append("Found: " + requestor.result.extraDoc + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            sb.append("Confidence: " + requestor.result.confidence + "\n");
            fail(sb.toString());
        }
    }

    //

    protected void assertDeclaringType(String source, String target, String expectedType) {
        final int offset = source.lastIndexOf(target);
        assertDeclaringType(source, offset, offset + target.length(), expectedType);
    }

    protected void assertDeclaringType(String contents, int exprStart, int exprUntil, String expectedDeclType) {
        assertDeclaringType(contents, exprStart, exprUntil, expectedDeclType, false);
    }

    protected void assertDeclaringType(String contents, int exprStart, int exprUntil, String expectedDeclType, boolean expectUnknown) {
        assertDeclaringType(createUnit(DEFAULT_UNIT_NAME, contents), exprStart, exprUntil, expectedDeclType, expectUnknown);
    }

    private SearchRequestor assertDeclaringType(GroovyCompilationUnit unit, int exprStart, int exprUntil, String expectedDeclType, boolean expectUnknown) {
        SearchRequestor requestor = doVisit(exprStart, exprUntil, unit);
        assertNotNull("Did not find expected ASTNode", requestor.node);
        if (!expectedDeclType.equals(requestor.getDeclaringTypeName())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected declaring type not found.\n");
            sb.append("\tExpect: ").append(expectedDeclType).append("\n");
            sb.append("\tActual: ").append(printTypeName(requestor.result.declaringType)).append("\n");
            sb.append("\tASTNode: ").append(requestor.node);
            fail(sb.toString());
        }
        if (expectUnknown) {
            if (requestor.result.confidence != TypeConfidence.UNKNOWN) {
                StringBuilder sb = new StringBuilder();
                sb.append("Confidence: ").append(requestor.result.confidence).append(" (but expecting UNKNOWN)\n");
                sb.append("\tDeclaring type: ").append(printTypeName(requestor.result.declaringType)).append("\n");
                sb.append("\tASTNode: ").append(requestor.node);
                fail(sb.toString());
            }
        } else {
            if (requestor.result.confidence == TypeConfidence.UNKNOWN) {
                StringBuilder sb = new StringBuilder();
                sb.append("Confidence should *not* have been UNKNOWN.\n");
                sb.append("\tExpect: ").append(expectedDeclType).append("\n");
                sb.append("\tActual: ").append(printTypeName(requestor.result.declaringType)).append("\n");
                sb.append("\tASTNode: ").append(requestor.node);
                fail(sb.toString());
            }
        }
        return requestor;
    }

    protected <N extends ASTNode> N assertDeclaration(String contents, int exprStart, int exprUntil, String expectedType, String name, DeclarationKind kind) {
        SearchRequestor requestor = assertDeclaringType(createUnit(DEFAULT_UNIT_NAME, contents), exprStart, exprUntil, expectedType, false);

        switch (kind) {
        case CLASS:
            assertTrue("Expecting class, but was " + requestor.result.declaration, requestor.result.declaration instanceof ClassNode);
            assertEquals("Wrong class name", name, ((ClassNode) requestor.result.declaration).getName());
            break;
        case FIELD:
            assertTrue("Expecting field, but was " + requestor.result.declaration, requestor.result.declaration instanceof FieldNode);
            assertEquals("Wrong field name", name, ((FieldNode) requestor.result.declaration).getName());
            break;
        case METHOD:
            assertTrue("Expecting method, but was " + requestor.result.declaration, requestor.result.declaration instanceof MethodNode);
            assertEquals("Wrong method name", name, ((MethodNode) requestor.result.declaration).getName());
            break;
        case PROPERTY:
            assertTrue("Expecting property, but was " + requestor.result.declaration, requestor.result.declaration instanceof PropertyNode);
            assertEquals("Wrong property name", name, ((PropertyNode) requestor.result.declaration).getName());
            break;
        case VARIABLE:
            assertTrue("Expecting variable, but was " + requestor.result.declaration, requestor.result.declaration instanceof Variable &&
                !(requestor.result.declaration instanceof FieldNode || requestor.result.declaration instanceof PropertyNode));
            assertEquals("Wrong variable name", name, ((Variable) requestor.result.declaration).getName());
            break;
        }

        @SuppressWarnings("unchecked")
        N decl = (N) requestor.result.declaration;

        return decl;
    }

    /**
     * Asserts that the declaration returned at the selection is deprecated
     * Checks only for the deprecated flag, (and so will only succeed for deprecated
     * DSLDs).  Could change this in the future
     */
    protected void assertDeprecated(String contents, int exprStart, int exprUntil) {
        GroovyCompilationUnit unit = createUnit(DEFAULT_UNIT_NAME, contents);
        SearchRequestor requestor = doVisit(exprStart, exprUntil, unit);

        assertNotNull("Did not find expected ASTNode", requestor.node);
        assertTrue("Declaration should be deprecated: " + requestor.result.declaration, GroovyUtils.isDeprecated(requestor.result.declaration));
    }

    protected void assertUnknownConfidence(String contents, int exprStart, int exprUntil) {
        GroovyCompilationUnit unit = createUnit(DEFAULT_UNIT_NAME, contents);
        SearchRequestor requestor = doVisit(exprStart, exprUntil, unit);

        assertNotNull("Did not find expected ASTNode", requestor.node);
        if (requestor.result.confidence != TypeConfidence.UNKNOWN) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expecting unknown confidence, but was " + requestor.result.confidence + ".\n");
            sb.append("Found: " + printTypeName(requestor.result.type) + "\n");
            sb.append("Declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            fail(sb.toString());
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Checks the compilation unit for the expected type and declaring type.
     *
     * @return null if all is OK, or else returns an error message specifying the problem
     */
    public static String checkType(final GroovyCompilationUnit unit, final int exprStart, final int exprUntil,
                final String expectedType, final String expectedDeclaringType, final boolean assumeNoUnknowns) {
        SearchRequestor requestor = doVisit(exprStart, exprUntil, unit);
        if (requestor.node == null) {
            return "Did not find expected ASTNode.  (Start:" + exprStart + ", End:" + exprUntil + ")\n" +
                    "text:" +  String.valueOf(CharOperation.subarray(unit.getContents(), exprStart, exprUntil)) + "\n";
        }
        if (expectedType != null && !expectedType.equals(printTypeName(requestor.result.type))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected type not found.\n");
            sb.append("Expected: " + expectedType + "\n");
            sb.append("Found: " + printTypeName(requestor.result.type) + "\n");
            sb.append("Declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            sb.append("Confidence: " + requestor.result.confidence + "\n");
            sb.append("Line, column: " + requestor.node.getLineNumber() + ", " + requestor.node.getColumnNumber());
            return sb.toString();
        }
        if (expectedDeclaringType != null && !expectedDeclaringType.equals(printTypeName(requestor.result.declaringType))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected declaring type not found.\n");
            sb.append("Expected: " + expectedDeclaringType + "\n");
            sb.append("Found: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("Type: " + printTypeName(requestor.result.type) + "\n");
            sb.append("ASTNode: " + requestor.node + " : " +  requestor.node.getText() + "\n");
            sb.append("Confidence: " + requestor.result.confidence + "\n");
            sb.append("Line, column: " + requestor.node.getLineNumber() + ", " + requestor.node.getColumnNumber() + "\n");
            return sb.toString();
        }

        if (assumeNoUnknowns && !requestor.unknowns.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following Unknown nodes were found (line:column):\n");
            for (ASTNode unknown : requestor.unknowns) {
                sb.append("(" + unknown.getLineNumber() + ":" + unknown.getColumnNumber() + ") ");
                sb.append(unknown + "\n");
            }
            return sb.toString();
        }

        if (VariableScope.OBJECT_CLASS_NODE.getGenericsTypes() != null) {
            return "Problem!!! Object type has type parameters now.  See STS-1854\n";
        }

        return null;
    }

    public static SearchRequestor doVisit(final int exprStart, final int exprUntil, final GroovyCompilationUnit unit) { waitUntilReady(unit);
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
        visitor.debug = true; // enable console output and post-visit assertions
        SearchRequestor requestor = new SearchRequestor(exprStart, exprUntil);
        visitor.visitCompilationUnit(requestor);
        return requestor;
    }

    public static String printTypeName(ClassNode type) {
        if (type == null) {
            return "null";
        }
        String arraySuffix = "";
        while (type.isArray()) {
            arraySuffix += "[]";
            type = type.getComponentType();
        }
        if (type.isGenericsPlaceHolder()) {
            return type.getUnresolvedName() + arraySuffix;
        }
        return type.getText() + printGenerics(type) + arraySuffix;
    }

    public static String printGenerics(ClassNode type) {
        GenericsType[] generics = GroovyUtils.getGenericsTypes(type);
        int n = generics.length;
        if (n == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < n; i += 1) {
            GenericsType gt = generics[i];
            if (gt.isWildcard()) {
                sb.append('?');
            } else if (gt.isPlaceholder()) {
                sb.append(gt.getName());
            } else {
                sb.append(printTypeName(gt.getType()));
            }
            if (gt.getLowerBound() != null) {
                sb.append(" super ");
                sb.append(printTypeName(gt.getLowerBound()));
            } else if (gt.getUpperBounds() != null) {
                sb.append(" extends ");
                for (ClassNode ub : gt.getUpperBounds()) {
                    sb.append(printTypeName(ub)).append(" & ");
                }
                sb.setLength(sb.length() - 3); // remove trailer
            }
            if (i < n - 1) {
                sb.append(',');
            }
        }
        sb.append('>');
        return sb.toString();
    }

    //--------------------------------------------------------------------------

    protected enum DeclarationKind {
        CLASS, FIELD, METHOD, PROPERTY, VARIABLE
    }

    public static class SearchRequestor implements ITypeRequestor {

        private final int start;
        private final int end;

        public TypeLookupResult result;
        public ASTNode node;

        public final List<ASTNode> unknowns = new ArrayList<>();

        public SearchRequestor(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public VisitStatus acceptASTNode(ASTNode visitorNode, TypeLookupResult visitorResult, IJavaElement enclosingElement) {
            // might have AST nodes with overlapping locations, so result may not be null
            if (this.result == null &&
                    (
                        (visitorNode.getStart() == start && visitorNode.getEnd() == end) ||
                        (visitorNode instanceof AnnotatedNode && ((AnnotatedNode) visitorNode).getNameStart() == start && ((AnnotatedNode) visitorNode).getNameEnd() + 1 == end)
                    ) &&
                    !(visitorNode instanceof MethodNode /* ignore run() method */) &&
                    !(visitorNode instanceof ClassNode && ((ClassNode) visitorNode).isScript() /* ignore the script */) &&
                    !(visitorNode instanceof Statement || visitorNode instanceof ImportNode /* ignore any statement */) &&
                    !(visitorNode instanceof ArrayExpression || visitorNode instanceof TupleExpression /* ignore wrapper */)) {
                if (visitorResult.type != null && ClassHelper.isPrimitiveType(visitorResult.type)) {
                    this.result = new TypeLookupResult(ClassHelper.getWrapper(visitorResult.type), visitorResult.declaringType, visitorResult.declaration, visitorResult);
                } else {
                    this.result = visitorResult;
                }
                this.node = visitorNode;
            }

            if (visitorResult.confidence == TypeConfidence.UNKNOWN && visitorNode.getEnd() > 0) {
                unknowns.add(visitorNode);
            }
            // always continue since we need to visit to the end to check consistency of inferencing engine stacks
            return VisitStatus.CONTINUE;
        }

        public String getDeclaringTypeName() {
            return printTypeName(result.declaringType);
        }

        public String getTypeName() {
            return printTypeName(result.type);
        }
    }
}
