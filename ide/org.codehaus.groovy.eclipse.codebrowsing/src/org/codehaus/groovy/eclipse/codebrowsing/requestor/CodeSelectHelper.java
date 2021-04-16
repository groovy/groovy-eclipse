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
package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ICodeSelectHelper;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.groovy.core.util.GroovyCodeVisitorAdapter;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;

public class CodeSelectHelper implements ICodeSelectHelper {

    @Override
    public IJavaElement[] select(final GroovyCompilationUnit unit, int start, int length) {
        char[] contents = unit.getContents();
        // expand zero-length selection to include adjacent identifier characters
        if (length == 0) {
            // TODO: Use the ExpressionFinder or TokenStream to do this?
            while (start > 0 && Character.isJavaIdentifierPart(contents[start - 1])) {
                start -= 1;
                length += 1;
            }
            while (contents.length > (start + length) && Character.isJavaIdentifierPart(contents[start + length])) {
                length += 1;
            }
        }
        // GRECLIPSE-1330: check for possible reference in GString
        if (length > 1 && contents.length > (start + 1) && contents[start] == '$' && contents[start + 1] != '{') {
            start += 1;
            length -= 1;
        }

        ModuleNode module = unit.getModuleNode();
        if (module != null) {
            String event = null;
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.CODE_SELECT, unit.getElementName() + " at [" + start + "," + length + "]");
                event = "Code select: " + unit.getElementName();
                GroovyLogManager.manager.logStart(event);
            }
            try {
                Region select = new Region(start, length);
                Object[] result = findNodeForRegion(module, select);
                ASTNode node = (ASTNode) result[0];
                Region region = (Region) result[1];
                // check for name that begins with '$' and revert GRECLIPSE-1330
                if (node != null && start > 0 && node.getStart() == start - 1 && contents[start - 1] == '$') {
                    start -= 1;
                    length += 1;
                }
                if (node instanceof AnnotatedNode && !isKeyword(node, contents, start, length) && !isStringLiteral(node, contents, start, length)) {
                    // shortcut: check to see if we are looking for this type itself
                    if (isTypeDeclaration(node, module)) {
                        return returnThisNode(node, unit);
                    }

                    CodeSelectRequestor requestor = createRequestor(node, region, select, unit);
                    TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                    visitor.visitCompilationUnit(requestor);
                    return requestor.getRequestedElements();
                }
            } catch (RuntimeException e) {
                if (event != null) {
                    GroovyLogManager.manager.logException(TraceCategory.CODE_SELECT, e);
                }
                throw e;
            } finally {
                if (event != null) {
                    GroovyLogManager.manager.logEnd(event, TraceCategory.CODE_SELECT);
                }
            }
        }
        return new IJavaElement[0];
    }

    public ASTNode selectASTNode(final GroovyCompilationUnit unit, final int start, final int length) {
        ModuleNode module = unit.getModuleNode();
        if (module != null) {
            String event = null;
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.CODE_SELECT, "Code select on " + unit.getElementName() + " at [ " + start + "," + length + " ]");
                event = "Code select: " + unit.getElementName();
                GroovyLogManager.manager.logStart(event);
            }
            try {
                Region select = new Region(start, length);
                Object[] result = findNodeForRegion(module, select);
                ASTNode node = (ASTNode) result[0];
                Region region = (Region) result[1];
                if (node != null) {
                    // shortcut: check to see if we are looking for this type itself
                    if (isTypeDeclaration(node, module)) {
                        return ((ClassNode) node).redirect();
                    }

                    CodeSelectRequestor requestor = createRequestor(node, region, select, unit);
                    TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                    visitor.visitCompilationUnit(requestor);
                    return requestor.getRequestedNode();
                }
            } finally {
                if (event != null) {
                    GroovyLogManager.manager.logEnd(event, TraceCategory.CODE_SELECT);
                }
            }
        }
        return null;
    }

    /**
     * Allows sub-classes to provide their own requestor.
     *
     * @param node the selected AST node
     * @param nodeRegion the source range for {@code node}
     * @param selectRegion the source range for the selection
     */
    protected CodeSelectRequestor createRequestor(final ASTNode node, final Region nodeRegion, final Region selectRegion, final GroovyCompilationUnit unit) {
        return new CodeSelectRequestor(node, nodeRegion, selectRegion, unit);
    }

    /**
     * @return array of {@link ASTNode} and {@link Region}
     */
    protected Object[] findNodeForRegion(final ModuleNode module, final Region r) {
        ASTNodeFinder finder = new ASTNodeFinder(r);
        finder.doVisit(module);

        return new Object[] {finder.result, finder.sloc};
    }

    protected static boolean isKeyword(final ASTNode node, final char[] contents, final int start, final int length) {
        if (node instanceof Expression) {
            boolean[] keyword = new boolean[1];

            node.visit(new GroovyCodeVisitorAdapter() {

                @Override public void visitCastExpression(final CastExpression expr) {
                    // something " as " something
                    keyword[0] = (expr.isCoerce() && length == 2 && String.valueOf(contents, start, length).equals("as"));
                }

                @Override public void visitClassExpression(final ClassExpression expr) {
                    // something ".class"
                    keyword[0] = (length == 5 && String.valueOf(contents, start, length).equals("class"));
                }

                @Override public void visitConstantExpression(final ConstantExpression expr) {
                    // "null" or "true" or "false"
                    keyword[0] = (expr.isNullExpression() || expr.isTrueExpression() || expr.isFalseExpression());
                }

                @Override public void visitConstructorCallExpression(final ConstructorCallExpression expr) {
                    // "new " something
                    keyword[0] = (length == 3 && start < expr.getNameStart() && String.valueOf(contents, start, length).equals("new"));
                }

                @Override public void visitMethodCallExpression(final MethodCallExpression expr) {
                    // something " in " something
                    keyword[0] = (expr.getMethodAsString().equals("isCase") && length == 2 && String.valueOf(contents, start, length).equals("in"));
                }

                @Override public void visitMethodPointerExpression(final MethodPointerExpression expr) {
                    // something "::" something
                    keyword[0] = (length == 2 /*&& String.valueOf(contents, start, length).equals("::")*/);
                }

                @Override public void visitVariableExpression(final VariableExpression expr) {
                    // "this." something
                    keyword[0] = (expr.isThisExpression());
                }
            });

            return keyword[0];
        }

        if (node instanceof ImportNode) {
            ImportNode imp = (ImportNode) node;
            switch (length) {
            case 1:
                // import something ".*"
                if (imp.isStar() && start == node.getEnd() - 1)
                    return true;
                break;
            case 2:
                // import something " as " something
                if (imp.getAliasExpr() != null && String.valueOf(contents, start, length).equals("as"))
                    return true;
                break;
            case 6:
                // "import " or "import static " something
                if (start == node.getStart() || imp.isStatic() && start < node.getStart() + 14 /*"import static ".length()*/)
                    return true;
                break;
            }
        }

        // "def " or "var " or "final " something
        if (node == ClassHelper.DYNAMIC_TYPE && length >= 3 && String.valueOf(contents, start, length).matches("def|var|final")) {
            return true;
        }

        return false;
    }

    protected static boolean isStringLiteral(final ASTNode node, final char[] contents, final int start, final int length) {
        if (node instanceof ConstantExpression && ClassHelper.STRING_TYPE.equals(((ConstantExpression) node).getType())) {
            return (start > node.getStart() && length < node.getLength());
        } else if (node instanceof MethodNode) {
            return (start > ((MethodNode) node).getNameStart() && start + length <= ((MethodNode) node).getNameEnd());
        }
        return false;
    }

    protected static boolean isTypeDeclaration(final ASTNode node, final ModuleNode module) {
        // don't use inner class nodes since they really should resolve to the super type
        if (node instanceof ClassNode && ((ClassNode) node).getOuterClass() == null) {
            for (ClassNode clazz : module.getClasses()) {
                if (clazz.equals(node)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static IJavaElement[] returnThisNode(final ASTNode node, final GroovyCompilationUnit unit) {
        // GRECLIPSE-803: ensure inner classes are handled correctly
        String rawName = ((ClassNode) node).getNameWithoutPackage();
        String[] enclosingTypes = rawName.split("\\$");
        IType candidate = null;
        for (int i = 0, n = enclosingTypes.length; i < n; i += 1) {
            if (i == 0) {
                candidate = unit.getType(enclosingTypes[i]);
            } else {
                candidate = candidate.getType(enclosingTypes[i]);
            }
        }
        IJavaElement result;
        if (unit instanceof GroovyClassFileWorkingCopy) {
            result = ((GroovyClassFileWorkingCopy) unit).convertToBinary(candidate);
        } else {
            result = candidate;
        }
        return new IJavaElement[] {result};
    }
}
