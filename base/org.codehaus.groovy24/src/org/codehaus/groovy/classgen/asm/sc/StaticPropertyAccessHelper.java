/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.sc.TemporaryVariableExpression;

import java.util.Arrays;

import static java.beans.Introspector.decapitalize;

/**
 * Facilitates the generation of statically-compiled bytecode for property access.
 *
 * @since 2.4.0
 */
public abstract class StaticPropertyAccessHelper {

    public static Expression transformToSetterCall(
            final Expression receiver,
            final MethodNode setterMethod,
            final Expression argument,
            final boolean implicitThis,
            final boolean safe,
            final boolean spreadSafe,
            final boolean requiresReturnValue,
            final Expression propertyExpression) {
        if (requiresReturnValue) {
            TemporaryVariableExpression tmp = new TemporaryVariableExpression(argument);
            PoppingMethodCallExpression call = new PoppingMethodCallExpression(receiver, setterMethod, tmp);
            call.setSafe(safe);
            call.setSpreadSafe(spreadSafe);
            call.setImplicitThis(implicitThis);
            call.setSourcePosition(propertyExpression);
            PoppingListOfExpressionsExpression list = new PoppingListOfExpressionsExpression(tmp, call);
            list.setSourcePosition(propertyExpression);
            return list;
        } else {
            MethodCallExpression call = new MethodCallExpression(receiver, setterMethod.getName(), argument);
            call.setSafe(safe);
            call.setSpreadSafe(spreadSafe);
            call.setImplicitThis(implicitThis);
            call.setMethodTarget(setterMethod);
            call.setSourcePosition(propertyExpression);
            return call;
        }
    }

    private static class PoppingListOfExpressionsExpression extends ListOfExpressionsExpression {

        private final TemporaryVariableExpression tmp;
        private final PoppingMethodCallExpression call;

        public PoppingListOfExpressionsExpression(final TemporaryVariableExpression tmp, final PoppingMethodCallExpression call) {
            super(Arrays.asList(tmp, call));
            this.tmp = tmp;
            this.call = call;
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            PoppingMethodCallExpression call = (PoppingMethodCallExpression) this.call.transformExpression(transformer);
            return new PoppingListOfExpressionsExpression(call.tmp, call);
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            super.visit(visitor);
            if (visitor instanceof AsmClassGenerator) {
                tmp.remove(((AsmClassGenerator) visitor).getController());
            }
        }
    }

    private static class PoppingMethodCallExpression extends MethodCallExpression {

        private final TemporaryVariableExpression tmp;

        public PoppingMethodCallExpression(final Expression receiver, final MethodNode setterMethod, final TemporaryVariableExpression tmp) {
            /* GRECLIPSE edit
            super(receiver, setterMethod.getName(), tmp);
            */
            super(receiver, new ConstantExpression(setterMethod.getName()) {
                @Override
                public String getText() {
                    // retaian property semantic for method expression
                    return decapitalize(super.getText().substring(3));
                }
            }, tmp);
            // GRECLIPSE end
            setMethodTarget(setterMethod);
            this.tmp = tmp;
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            PoppingMethodCallExpression call = new PoppingMethodCallExpression(getObjectExpression().transformExpression(transformer), getMethodTarget(), (TemporaryVariableExpression) tmp.transformExpression(transformer));
            call.copyNodeMetaData(this);
            call.setSourcePosition(this);
            call.setSafe(isSafe());
            call.setSpreadSafe(isSpreadSafe());
            call.setImplicitThis(isImplicitThis());
            return call;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            super.visit(visitor);
            if (visitor instanceof AsmClassGenerator) {
                // ignore the return of the call
                ((AsmClassGenerator) visitor).getController().getOperandStack().pop();
            }
        }
    }
}
