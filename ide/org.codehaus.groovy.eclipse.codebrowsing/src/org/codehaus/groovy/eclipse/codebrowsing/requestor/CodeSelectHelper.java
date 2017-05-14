/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.ModuleNode;
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

    public IJavaElement[] select(GroovyCompilationUnit unit, int start, int length) {
        // GRECLIPSE-1330: check for possible reference in GString
        char[] contents = unit.getContents();
        if (length > 1 && start + length < contents.length && contents[start] == '$' && contents[start + 1] != '{') {
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
                if (node != null && !isKeywordSelection(node, contents, start, length)) {
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

    protected static boolean isKeywordSelection(ASTNode node, char[] contents, int start, int length) {
        boolean keyword = false;
        // "this." something
        if (node instanceof VariableExpression && ((VariableExpression) node).isThisExpression()) {
            keyword = true;
        }
        // "def " something
        else if (node == ClassHelper.DYNAMIC_TYPE && length == 3) {
            keyword = String.valueOf(contents, start, length).equals("def");
        }
        // "import " or "import static " something
        else if (node instanceof ImportNode && length == 6 && (start == node.getStart() ||
                ((ImportNode) node).isStatic() && start < node.getStart() + 14/*"import static ".length()*/)) {
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

    protected static boolean isTypeDeclaration(ASTNode node, ModuleNode module) {
        // don't use inner class nodes since they really should resolve to the super type
        if (node instanceof ClassNode &&  !(node instanceof InnerClassNode)) {
            for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
                if (clazz == node) {
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
