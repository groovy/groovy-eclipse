/*
 * Copyright 2009-2019 the original author or authors.
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
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ICodeSelectHelper;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;

public class CodeSelectHelper implements ICodeSelectHelper {

    @Override
    public IJavaElement[] select(GroovyCompilationUnit unit, int start, int length) {
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
                if (node instanceof AnnotatedNode && !isKeyword(node, contents, start, length) && !isStringLiteral(node, contents, start, length)) {
                    // shortcut: check to see if we are looking for this type itself
                    if (isTypeDeclaration(node, module)) {
                        return returnThisNode(node, unit);
                    }

                    CodeSelectRequestor requestor = createRequestor(node, region, select, unit);
                    TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                    visitor.visitCompilationUnit(requestor);

                    IJavaElement element = requestor.getRequestedElement();
                    if (element != null) {
                        return new IJavaElement[] {element};
                    }
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

    public ASTNode selectASTNode(GroovyCompilationUnit unit, int start, int length) {
        ModuleNode module = unit.getModuleNode();
        if (module != null) {
            String event = null;
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.CODE_SELECT, "Code select starting on " + unit.getElementName() + " at [ " + start + "," + length + " ]");
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
    protected CodeSelectRequestor createRequestor(ASTNode node, Region nodeRegion, Region selectRegion, GroovyCompilationUnit unit) {
        return new CodeSelectRequestor(node, nodeRegion, selectRegion, unit);
    }

    /**
     * @return array of {@link ASTNode} and {@link Region}
     */
    protected Object[] findNodeForRegion(ModuleNode module, Region r) {
        ASTNodeFinder finder = new ASTNodeFinder(r);
        finder.doVisit(module);

        return new Object[] {finder.result, finder.sloc};
    }

    protected static boolean isKeyword(ASTNode node, char[] contents, int start, int length) {
        boolean keyword = false;
        // "null"
        if (node instanceof ConstantExpression && ((ConstantExpression) node).isNullExpression()) {
            keyword = true;
        }
        // "true" or "false"
        else if (node instanceof ConstantExpression && ClassHelper.boolean_TYPE.equals(((ConstantExpression) node).getType())) {
            keyword = true;
        }
        // "this." something
        else if (node instanceof VariableExpression && ((VariableExpression) node).isThisExpression()) {
            keyword = true;
        }
        // something ".class"
        else if (node instanceof ClassExpression && length == 5) {
            keyword = String.valueOf(contents, start, length).equals("class");
        }
        // "def " or "var " or "final " something
        else if (node == ClassHelper.DYNAMIC_TYPE && length >= 3) {
            keyword = String.valueOf(contents, start, length).matches("def|var|final");
        }
        // "new " something
        else if (node instanceof ConstructorCallExpression && length == 3 && start < ((ConstructorCallExpression) node).getNameStart()) {
            keyword = String.valueOf(contents, start, length).equals("new");
        }
        // something " as " something
        else if (node instanceof CastExpression && ((CastExpression) node).isCoerce() && length == 2) {
            keyword = String.valueOf(contents, start, length).equals("as");
        }
        // something " in " something
        else if (node instanceof MethodCallExpression && ((MethodCallExpression) node).getMethodAsString().equals("isCase") && length == 2) {
            keyword = String.valueOf(contents, start, length).equals("in");
        }
        // "import " or "import static " something
        else if (node instanceof ImportNode && length == 6 && (start == node.getStart() ||
                ((ImportNode) node).isStatic() && start < node.getStart() + 14 /*"import static ".length()*/)) {
            keyword = true;
        }
        else if (node instanceof ImportNode && ((ImportNode) node).getAliasExpr() != null && length == 2) {
            keyword = String.valueOf(contents, start, length).equals("as");
        }
        else if (node instanceof ImportNode && ((ImportNode) node).isStar() && length == 1 && start == node.getEnd() - 1) {
            keyword = true;
        }
        return keyword;
    }

    protected static boolean isStringLiteral(ASTNode node, char[] contents, int start, int length) {
        if (node instanceof ConstantExpression && ClassHelper.STRING_TYPE.equals(((ConstantExpression) node).getType())) {
            return (start > node.getStart() && length < node.getLength());
        } else if (node instanceof MethodNode) {
            return (start > ((MethodNode) node).getNameStart() &&
                start + length <= ((MethodNode) node).getNameEnd());
        }
        return false;
    }

    protected static boolean isTypeDeclaration(ASTNode node, ModuleNode module) {
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

    protected static IJavaElement[] returnThisNode(ASTNode node, GroovyCompilationUnit unit) {
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
