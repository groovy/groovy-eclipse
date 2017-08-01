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
package org.codehaus.groovy.eclipse.editor.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceField;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.core.JavaElement;

public class GroovyScriptOutlineExtender implements IOutlineExtender {

    public GroovyOutlinePage getGroovyOutlinePageForEditor(String contextMenuID, GroovyEditor editor) {
        return new GroovyOutlinePage(contextMenuID, editor, new GroovyScriptOCompilationUnit(editor.getGroovyCompilationUnit()));
    }

    public boolean appliesTo(GroovyCompilationUnit unit) {
        ModuleNode moduleNode = unit.getModuleNode();
        return moduleNode != null && !moduleNode.getClasses().isEmpty() && moduleNode.getClasses().get(0).isScript();
    }

    //--------------------------------------------------------------------------

    private static class GroovyScriptOCompilationUnit extends OCompilationUnit {

        public GroovyScriptOCompilationUnit(GroovyCompilationUnit unit) {
            super(unit);
        }

        @Override
        public IJavaElement[] refreshChildren() {
            ModuleNode node = (ModuleNode) getNode();
            ClassNode scriptClassDummy;
            String scriptName;
            if (node != null) {
                scriptClassDummy = node.getScriptClassDummy();
                if (scriptClassDummy == null) {
                    if (!node.getClasses().isEmpty()) {
                        scriptClassDummy = node.getClasses().get(0);
                    }
                }
                if (scriptClassDummy == null) {
                    scriptName = "Problem";
                } else {
                    scriptName = scriptClassDummy.getNameWithoutPackage();
                }
            } else {
                scriptName = null;
                scriptClassDummy = null;
            }
            if (node == null || node.encounteredUnrecoverableError() || scriptClassDummy == null) {
                return new IJavaElement[] {new OType(getUnit(), node, scriptName + " -- No structure found")};
            }

            // otherwise, add all children directly except for the script class
            try {
                IJavaElement[] children = getUnit().getChildren();
                final IType scriptType;
                final List<IJavaElement> fakeChildren = new ArrayList<IJavaElement>();
                IType candidate = null;
                for (IJavaElement elt : children) {
                    if (elt.getElementName().equals(scriptName)) {
                        candidate = (IType) elt;
                    } else {
                        fakeChildren.add(elt);
                    }
                }
                scriptType = candidate;

                if (scriptType != null) {
                    // do not add the script type directly. Rather, add all of the children
                    // Additionally, do not add the run or main methods
                    for (IJavaElement scriptElt : scriptType.getChildren()) {
                        if (scriptElt instanceof IMember && !(isRunMethod(scriptElt) || isMainMethod(scriptElt) || isConstructor(scriptElt))) {
                            fakeChildren.add(scriptElt);
                        }
                    }

                    // add all of the field declarations
                    for (FieldNode field : scriptClassDummy.getFields()) {
                        if (!field.isSynthetic()) {
                            fakeChildren.add(new GroovyResolvedSourceField((JavaElement) scriptType, field.getName(), null, null, field));
                        }
                    }

                    // add all of the variable declarations
                    ClassCodeVisitorSupport visitor = new ClassCodeVisitorSupport() {
                        @Override
                        public void visitClosureExpression(ClosureExpression expression) {
                        }

                        @Override
                        public void visitDeclarationExpression(DeclarationExpression expression) {
                            fakeChildren.add(new GroovyScriptVariable((JavaElement) scriptType, expression));
                            super.visitDeclarationExpression(expression);
                        }
                    };
                    visitor.visitBlockStatement(node.getStatementBlock());
                }

                // finally, sort all the elements by source location
                IJavaElement[] fakeChildrenArr = fakeChildren.toArray(new IJavaElement[fakeChildren.size()]);
                return sort(fakeChildrenArr);

            } catch (JavaModelException e) {
                GroovyCore.logException("Encountered exception when calculating children", e);
                return new IJavaElement[] {new OType(getUnit(), node, scriptName + " -- Encountered exception.  See log.")};
            }
        }

        private static boolean isConstructor(IJavaElement scriptElt) throws JavaModelException {
            if (scriptElt.getElementType() != IJavaElement.METHOD) {
                return false;
            }
            return ((IMethod) scriptElt).isConstructor();
        }

        private static boolean isMainMethod(IJavaElement scriptElt) throws JavaModelException {
            if (scriptElt.getElementType() != IJavaElement.METHOD) {
                return false;
            }
            return ((IMethod) scriptElt).isMainMethod();
        }

        private static boolean isRunMethod(IJavaElement scriptElt) throws JavaModelException {
            if (scriptElt.getElementType() != IJavaElement.METHOD) {
                return false;
            }
            if (!scriptElt.getElementName().equals("run")) {
                return false;
            }
            String[] parammeterTypes = ((IMethod) scriptElt).getParameterTypes();
            return parammeterTypes == null || parammeterTypes.length == 0;
        }

        /**
         * Sorts array of IJavaElements by their start position.
         */
        private static IJavaElement[] sort(IJavaElement[] elts) {
            Arrays.sort(elts, new Comparator<IJavaElement>() {
                public int compare(IJavaElement e1, IJavaElement e2) {
                    try {
                        // really we should only be getting source refs elements here
                        Assert.isTrue(e1 instanceof ISourceReference, "Expecting a ISourceReference, but found " + e1);
                        Assert.isTrue(e2 instanceof ISourceReference, "Expecting a ISourceReference, but found " + e2);
                        return ((ISourceReference) e1).getSourceRange().getOffset() - ((ISourceReference) e2).getSourceRange().getOffset();
                    } catch (JavaModelException e) {
                        GroovyCore.logException("Exception when comparing " + e1 + " and " + e2, e);
                        return 0;
                    }
                }
            });
            return elts;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * A variable declaration in a script.
     */
    private static class GroovyScriptVariable extends OField {

        private String typeSignature;

        public GroovyScriptVariable(JavaElement parent, DeclarationExpression node) {
            super(parent, node, extractName(node));
            ClassNode fieldType = node.getLeftExpression().getType();
            if (ClassHelper.DYNAMIC_TYPE == fieldType) {
                typeSignature = "Qdef;";
            } else {
                typeSignature = fieldType.toString(false);
                // don't need to convert array signatures
                if (!typeSignature.startsWith("[")) {
                    typeSignature = Signature.createTypeSignature(typeSignature, false);
                }
            }
        }

        private static String extractName(DeclarationExpression node) {
            Expression leftExpression = node.getLeftExpression();
            if (leftExpression instanceof VariableExpression) {
                return ((VariableExpression) leftExpression).getName();
            } else {
                // multi-variable expression
                if (leftExpression instanceof TupleExpression) {
                    List<Expression> exprs = ((TupleExpression) leftExpression).getExpressions();
                    StringBuilder sb = new StringBuilder();
                    for (Iterator<Expression> exprIter = exprs.iterator(); exprIter.hasNext();) {
                        Expression expr = exprIter.next();
                        sb.append(expr.getText());
                        if (exprIter.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    return sb.toString();
                }
            }
            return "no name";
        }

        @Override
        public ASTNode getElementNameNode() {
            DeclarationExpression decl = (DeclarationExpression) getNode();
            return decl.getLeftExpression();
        }

        @Override
        public ISourceRange getSourceRange() throws JavaModelException {
            ISourceRange range = super.getSourceRange();
            if (range.getLength() < 1) {
                ASTNode lhs = getElementNameNode();
                range = new SourceRange(lhs.getStart(), lhs.getLength());
            }
            return range;
        }

        @Override
        public String getTypeSignature() {
            return typeSignature;
        }
    }
}
