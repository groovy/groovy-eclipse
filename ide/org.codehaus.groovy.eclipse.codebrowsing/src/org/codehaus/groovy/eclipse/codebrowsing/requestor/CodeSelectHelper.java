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

package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ICodeSelectHelper;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jface.text.IRegion;

/**
 * @author Andrew Eisenberg
 * @created Nov 4, 2009
 *
 */
public class CodeSelectHelper implements ICodeSelectHelper {
    
    private class ASTNodeFinder extends ClassCodeVisitorSupport {
        
        ASTNode nodeFound;
        private IRegion r;
        
        public ASTNodeFinder(IRegion r) {
            this.r = r;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
        
        
        @Override
        public void visitVariableExpression(VariableExpression expression) {
            check(expression);
            super.visitVariableExpression(expression);
        }
        
        @Override
        public void visitFieldExpression(FieldExpression expression) {
            check(expression);
            super.visitFieldExpression(expression);
        }
        
        @Override
        public void visitClassExpression(ClassExpression expression) {
            check(expression);
            super.visitClassExpression(expression);
        }
        
        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            checkParameters(expression.getParameters());
            super.visitClosureExpression(expression);
        }

        @Override
        protected void visitConstructorOrMethod(MethodNode node,
                boolean isConstructor) {
            ClassNode expression = node.getReturnType();
            if (expression != null) {
                check(expression);
            }
            if (node.getExceptions() != null) {
                for (ClassNode e : node.getExceptions()) {
                    check(e);
                }
            }
            checkParameters(node.getParameters());
            super.visitConstructorOrMethod(node, isConstructor);
        }

        /**
         * @param node
         */
        private void checkParameters(Parameter[] params) {
            if (params != null) {
               for (Parameter p : params) {
                   check(p.getType());
                   if (p.getInitialExpression() != null) {
                       p.getInitialExpression().visit(this);
                   }
               }
            }
        }
        
        @Override
        public void visitField(FieldNode node) {
            check(node.getType());
            super.visitField(node);
        }
        
        @Override
        public void visitCastExpression(CastExpression node) {
            check(node.getType());
            super.visitCastExpression(node);
        }
        
        @Override
        public void visitConstantExpression(ConstantExpression expression) {
            check(expression);
            super.visitConstantExpression(expression);
        }
        
        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
            check(expression.getLeftExpression().getType());
            super.visitDeclarationExpression(expression);
        }
        
        @Override
        public void visitConstructorCallExpression(
                ConstructorCallExpression call) {
            check(call.getType());
            super.visitConstructorCallExpression(call);
        }
        
        @Override
        public void visitClass(ClassNode node) {
            if (node.getUnresolvedSuperClass() != null) {
                check(node.getUnresolvedSuperClass());  // use unresolved to maintain source locations 
            }
            if (node.getInterfaces() != null) {
                for (ClassNode inter : node.getInterfaces()) {
                    check(inter);
                }
            }
            if (node.getObjectInitializerStatements() != null) {
                for (Statement s : node.getObjectInitializerStatements()) {
                    super.visitStatement(s);
                }
            }
            super.visitClass(node);
        }

        /**
         * @param node
         */
        private void check(ASTNode node) {
            if (doTest(node)) {
                nodeFound = node;
                throw new VisitCompleteException();
            }
        }

        /**
         * @param node
         * @return
         */
        private boolean doTest(ASTNode node) {
            return node.getStart() <= r.getOffset() && node.getEnd() >= r.getOffset()+r.getLength();
        }

        /**
         * @param module
         * @return
         */
        public ASTNode doVisit(ModuleNode module) {
            try {
                for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
                    this.visitClass(clazz);
                }
            } catch (VisitCompleteException e) {
            }
            return nodeFound;
        }
        
    }
    
    public IJavaElement[] select(GroovyCompilationUnit unit, IRegion r) {
        ModuleNode module = unit.getModuleNode();
        if (module != null) {
            ASTNode nodeToLookFor = findASTNodeAt(module, r);
            if (nodeToLookFor != null) {
                CodeSelectRequestor requestor = new CodeSelectRequestor(nodeToLookFor, unit);
                TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                visitor.visitCompilationUnit(requestor);
                return requestor.getRequestedElement() != null ? new IJavaElement[] { requestor.getRequestedElement() } : new IJavaElement[0];
            }
        }
        return new IJavaElement[0];
    }

    /**
     * @param r
     * @return
     */
    private ASTNode findASTNodeAt(ModuleNode module, IRegion r) {
        ASTNodeFinder finder = new ASTNodeFinder(r);
        return finder.doVisit(module);
    }
}
