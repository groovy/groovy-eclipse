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

package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.editor.GroovyTagScanner;
import org.eclipse.jface.text.Position;

class GatherSemanticReferences extends ClassCodeVisitorSupport {
    
        List<Position> unknownReferences = new LinkedList<Position>();  // underlined
        List<Position> staticReferences = new LinkedList<Position>();   // italicized
        
        private ClassNode thisClass;
        
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    
        /**
         * @param module
         */
        public void visitModule(ModuleNode module) {
            for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
                thisClass = clazz;
                this.visitClass(clazz);
            }
        }
        
        @Override
        public void visitStaticMethodCallExpression(
                StaticMethodCallExpression call) {
            // no static refs for now
//            if (call.getStart() > 0 || call.getEnd() > 0) {
//                staticReferences.add(call);
//            }
            super.visitStaticMethodCallExpression(call);
        }
        
        @Override
        public void visitFieldExpression(FieldExpression expression) {
            // no static refs for now
//            if (expression.getField().isStatic()) {
//                if (expression.getStart() > 0 || expression.getEnd() > 0) {
//                    staticReferences.add(expression);
//                }
//            }
            super.visitFieldExpression(expression);
        }
        
        @Override
        public void visitVariableExpression(VariableExpression expression) {
            if (expression.getAccessedVariable() == null || expression.getAccessedVariable() instanceof DynamicVariable) {
                String name = expression.getName();
                if (!name.equals("this") && !name.equals("super")) {
                    if (expression.getStart() > 0 || expression.getEnd() > 0) {
                        unknownReferences.add(fullPosition(expression));
                    }
                }
            }
            super.visitVariableExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (call.getObjectExpression() instanceof VariableExpression) {
                String objName = ((VariableExpression) call.getObjectExpression()).getName();
                if (objName.equals("this") || objName.equals("super")) {
                    if (call.getMethod() instanceof ConstantExpression) {
                        String methodName= call.getMethodAsString();
                        if (!isGjdkMethod(methodName) && thisClass.getMethods(methodName).size() == 0) {
                            unknownReferences.add(extractNameOnly(call.getMethod(), methodName));
                        }
                    }
                }
            }
            super.visitMethodCallExpression(call);
        }

        /**
         * @param methodName
         * @return
         */
        private boolean isGjdkMethod(String methodName) {
            return GroovyTagScanner.gjdkSet.contains(methodName);
        }

        /**
         * @param call
         * @return
         */
        private Position extractNameOnly(Expression expr, String name) {
            return new Position(expr.getStart(), name.length());
        }
        
        @Override
        public void visitMethodPointerExpression(
                MethodPointerExpression expression) {
            super.visitMethodPointerExpression(expression);
        }

        /**
         * @param expression
         * @return
         */
        private Position fullPosition(VariableExpression expression) {
            return new Position(expression.getStart(), expression.getEnd()-expression.getStart());
        }
        
    }