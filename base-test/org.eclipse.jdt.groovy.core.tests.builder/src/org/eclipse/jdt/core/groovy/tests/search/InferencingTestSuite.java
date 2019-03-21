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
package org.eclipse.jdt.core.groovy.tests.search;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.junit.Assert;

public abstract class InferencingTestSuite extends SearchTestSuite {

    protected void assertType(String contents, String expectedType) {
        assertType(contents, 0, contents.length(), expectedType, false);
    }

    protected void assertType(String contents, int exprStart, int exprEnd, String expectedType) {
        assertType(contents, exprStart, exprEnd, expectedType, false);
    }

    protected void assertTypeOneOf(String contents, int start, int end, String... expectedTypes) throws Throwable {
        boolean ok = false;
        Throwable error = null;
        for (int i = 0; !ok && i < expectedTypes.length; i++) {
            try {
                assertType(contents, start, end, expectedTypes[i]);
                ok = true;
            } catch (Throwable e) {
                error = e;
            }
        }
        if (!ok) {
            if (error!=null) {
                throw error;
            } else {
                Assert.fail("assertTypeOneOf must be called with at least one expectedType");
            }
        }
    }

    public static void assertType(GroovyCompilationUnit contents, int start, int end, String expectedType) {
        assertType(contents, start, end, expectedType, false);
    }

    protected void assertType(String contents, String expectedType, boolean forceWorkingCopy) {
        assertType(contents, 0, contents.length(), expectedType, forceWorkingCopy);
    }

    protected void assertType(String contents, int exprStart, int exprEnd, String expectedType, boolean forceWorkingCopy) {
        assertType(contents, exprStart, exprEnd, expectedType, null, forceWorkingCopy);
    }

    protected void assertType(String contents, int exprStart, int exprEnd, String expectedType, String extraDocSnippet, boolean forceWorkingCopy) {
        GroovyCompilationUnit unit = createUnit("Search", contents);
        assertType(unit, exprStart, exprEnd, expectedType, extraDocSnippet, forceWorkingCopy);
    }

    public static void assertType(GroovyCompilationUnit contents, int exprStart, int exprEnd, String expectedType, boolean forceWorkingCopy) {
        assertType(contents, exprStart, exprEnd, expectedType, null, forceWorkingCopy);
    }

    public static void assertType(GroovyCompilationUnit unit, int exprStart, int exprEnd, String expectedType, String extraDocSnippet, boolean forceWorkingCopy) {
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, forceWorkingCopy);

        Assert.assertNotNull("Did not find expected ASTNode", requestor.node);
        if (!expectedType.equals(printTypeName(requestor.result.type))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected type not found.\n");
            sb.append("Expected: " + expectedType + "\n");
            sb.append("Found: " + printTypeName(requestor.result.type) + "\n");
            sb.append("Declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            sb.append("Confidence: " + requestor.result.confidence + "\n");
            Assert.fail(sb.toString());
        }

        if (extraDocSnippet != null && (requestor.result.extraDoc == null || !requestor.result.extraDoc.replace("}", "").contains(extraDocSnippet))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Incorrect Doc found.\n");
            sb.append("Expected doc should contain: " + extraDocSnippet + "\n");
            sb.append("Found: " + requestor.result.extraDoc + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            sb.append("Confidence: " + requestor.result.confidence + "\n");
            Assert.fail(sb.toString());
        }
    }

    /**
     * Checks the compilation unit for the expected type and declaring type.
     * @param assumeNoUnknowns
     * @return null if all is OK, or else returns an error message specifying the problem
     */
    public static String checkType(GroovyCompilationUnit unit, int exprStart, int exprEnd, String expectedType, String expectedDeclaringType, boolean assumeNoUnknowns, boolean forceWorkingCopy) {
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, forceWorkingCopy);
        if (requestor.node == null) {
            return "Did not find expected ASTNode.  (Start:" + exprStart + ", End:" + exprEnd + ")\n" +
                    "text:" +  String.valueOf(CharOperation.subarray(unit.getContents(), exprStart, exprEnd)) + "\n";
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

    /**
     * Asserts that the declaration returned at the selection is deprecated
     * Checks only for the deprecated flag, (and so will only succeed for deprecated
     * DSLDs).  Could change this in the future
     */
    protected void assertDeprecated(String contents, int exprStart, int exprEnd) {
        GroovyCompilationUnit unit = createUnit("Search", contents);
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, false);
        Assert.assertNotNull("Did not find expected ASTNode", requestor.node);
        Assert.assertTrue("Declaration should be deprecated: " + requestor.result.declaration, GroovyUtils.isDeprecated(requestor.result.declaration));
    }

    public static SearchRequestor doVisit(int exprStart, int exprEnd, GroovyCompilationUnit unit, boolean forceWorkingCopy) {
        try {
            if (forceWorkingCopy) {
                unit.becomeWorkingCopy(null);
            }
            try {
                TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
                visitor.DEBUG = true;
                SearchRequestor requestor = new SearchRequestor(exprStart, exprEnd);
                visitor.visitCompilationUnit(requestor);
                return requestor;
            } finally {
                if (forceWorkingCopy) {
                    unit.discardWorkingCopy();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertDeclaringType(String contents, int exprStart, int exprEnd, String expectedDeclaringType) {
        assertDeclaringType(contents, exprStart, exprEnd, expectedDeclaringType, false);
    }

    protected enum DeclarationKind { CLASS, FIELD, METHOD, PROPERTY, VARIABLE }

    protected <N extends ASTNode> N assertDeclaration(String contents, int exprStart, int exprEnd, String expectedDeclaringType, String declarationName, DeclarationKind kind) {
        assertDeclaringType(contents, exprStart, exprEnd, expectedDeclaringType, false, false);
        GroovyCompilationUnit unit = createUnit("Search", contents);
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, false);

        switch (kind) {
        case CLASS:
            Assert.assertTrue("Expecting class, but was " + requestor.result.declaration, requestor.result.declaration instanceof ClassNode);
            Assert.assertEquals("Wrong class name", declarationName, ((ClassNode) requestor.result.declaration).getName());
            break;
        case FIELD:
            Assert.assertTrue("Expecting field, but was " + requestor.result.declaration, requestor.result.declaration instanceof FieldNode);
            Assert.assertEquals("Wrong field name", declarationName, ((FieldNode) requestor.result.declaration).getName());
            break;
        case METHOD:
            Assert.assertTrue("Expecting method, but was " + requestor.result.declaration, requestor.result.declaration instanceof MethodNode);
            Assert.assertEquals("Wrong method name", declarationName, ((MethodNode) requestor.result.declaration).getName());
            break;
        case PROPERTY:
            Assert.assertTrue("Expecting property, but was " + requestor.result.declaration, requestor.result.declaration instanceof PropertyNode);
            Assert.assertEquals("Wrong property name", declarationName, ((PropertyNode) requestor.result.declaration).getName());
            break;
        case VARIABLE:
            Assert.assertTrue("Expecting variable, but was " + requestor.result.declaration, requestor.result.declaration instanceof Variable &&
                !(requestor.result.declaration instanceof FieldNode || requestor.result.declaration instanceof PropertyNode));
            Assert.assertEquals("Wrong variable name", declarationName, ((Variable) requestor.result.declaration).getName());
            break;
        }

        @SuppressWarnings("unchecked")
        N decl = (N) requestor.result.declaration;

        return decl;
    }

    protected void assertDeclaringType(String contents, int exprStart, int exprEnd, String expectedDeclaringType, boolean forceWorkingCopy) {
        assertDeclaringType(contents, exprStart, exprEnd, expectedDeclaringType, forceWorkingCopy, false);
    }

    protected void assertDeclaringType(String contents, int exprStart, int exprEnd, String expectedDeclaringType, boolean forceWorkingCopy, boolean expectingUnknown) {
        GroovyCompilationUnit unit = createUnit("Search", contents);
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, forceWorkingCopy);

        Assert.assertNotNull("Did not find expected ASTNode", requestor.node);
        if (!expectedDeclaringType.equals(requestor.getDeclaringTypeName())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected declaring type not found.\n");
            sb.append("\tExpected: ").append(expectedDeclaringType).append("\n");
            sb.append("\tFound type: ").append(printTypeName(requestor.result.type)).append("\n");
            sb.append("\tFound declaring type: ").append(printTypeName(requestor.result.declaringType)).append("\n");
            sb.append("\tASTNode: ").append(requestor.node);
            Assert.fail(sb.toString());
        }
        if (expectingUnknown) {
            if (requestor.result.confidence != TypeConfidence.UNKNOWN) {
                StringBuilder sb = new StringBuilder();
                sb.append("Confidence: ").append(requestor.result.confidence).append(" (but expecting UNKNOWN)\n");
                sb.append("\tExpected: ").append(expectedDeclaringType).append("\n");
                sb.append("\tFound: ").append(printTypeName(requestor.result.type)).append("\n");
                sb.append("\tDeclaring type: ").append(printTypeName(requestor.result.declaringType)).append("\n");
                sb.append("\tASTNode: ").append(requestor.node);
                Assert.fail(sb.toString());
            }
        } else {
            if (requestor.result.confidence == TypeConfidence.UNKNOWN) {
                StringBuilder sb = new StringBuilder();
                sb.append("Expected Confidence should not have been UNKNOWN, but it was.\n");
                sb.append("\tExpected declaring type: ").append(expectedDeclaringType).append("\n");
                sb.append("\tFound type: ").append(printTypeName(requestor.result.type)).append("\n");
                sb.append("\tFound declaring type: ").append(printTypeName(requestor.result.declaringType)).append("\n");
                sb.append("\tASTNode: ").append(requestor.node);
                Assert.fail(sb.toString());
            }
        }
    }

    protected void assertUnknownConfidence(String contents, int exprStart, int exprEnd, String expectedDeclaringType, boolean forceWorkingCopy) {
        GroovyCompilationUnit unit = createUnit("Unknown", contents);
        SearchRequestor requestor = doVisit(exprStart, exprEnd, unit, forceWorkingCopy);

        Assert.assertNotNull("Did not find expected ASTNode", requestor.node);
        if (requestor.result.confidence != TypeConfidence.UNKNOWN) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expecting unknown confidentce, but was " + requestor.result.confidence + ".\n");
            sb.append("Expected: " + expectedDeclaringType + "\n");
            sb.append("Found: " + printTypeName(requestor.result.type) + "\n");
            sb.append("Declaring type: " + printTypeName(requestor.result.declaringType) + "\n");
            sb.append("ASTNode: " + requestor.node + "\n");
            Assert.fail(sb.toString());
        }
    }

    protected void assertNoUnknowns(String contents) {
        List<ASTNode> unknownNodes = new ArrayList<>();
        GroovyCompilationUnit unit = createUnit("Known", contents);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
        visitor.DEBUG = true;
        visitor.visitCompilationUnit((node, result, element) -> {
            if (result.confidence == TypeConfidence.UNKNOWN && node.getEnd() > 0) {
                unknownNodes.add(node);
            }
            return VisitStatus.CONTINUE;
        });
        Assert.assertTrue("Should not have found any AST nodes with unknown confidence, but found:\n" + unknownNodes, unknownNodes.isEmpty());
    }

    public static String printTypeName(ClassNode type) {
        if (type == null) {
            return "null";
        }
        String arraySuffix = "";
        while (type.getComponentType() != null) {
            type = type.getComponentType();
            arraySuffix += "[]";
        }
        return type.getName() + printGenerics(type) + arraySuffix;
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
                    visitorNode.getStart() == start && (visitorNode.getEnd() == end || (visitorNode instanceof AnnotatedNode && ((AnnotatedNode) visitorNode).getNameEnd() + 1 == end)) &&
                    !(visitorNode instanceof Statement /* ignore any statement */) &&
                    !(visitorNode instanceof TupleExpression /* ignore wrapper */) &&
                    !(visitorNode instanceof MethodNode /* ignore the run() method */) &&
                    !(visitorNode instanceof ClassNode && ((ClassNode) visitorNode).isScript() /* ignore the script */)) {
                if (ClassHelper.isPrimitiveType(visitorResult.type)) {
                    this.result = new TypeLookupResult(ClassHelper.getWrapper(visitorResult.type), visitorResult.declaringType, visitorResult.declaration, visitorResult.confidence, visitorResult.scope, visitorResult.extraDoc);
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
            return result.type.getName();
        }
    }
}
