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
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.syntax.Token;
import groovyjarjarasm.asm.Label;
import groovyjarjarasm.asm.MethodVisitor;
import groovyjarjarasm.asm.Opcodes;

/**
 * Compares two objects using identity comparison.
 * This expression will generate bytecode using the IF_ACMPNE instruction, instead of
 * using the "equals" method that is currently mapped to "==" in Groovy.
 *
 * This expression should only be used to compare to objects, not primitives, and only
 * in the context of reference equality check.
 */
public class CompareIdentityExpression extends BinaryExpression implements Opcodes {

    public CompareIdentityExpression(final Expression leftExpression, final Expression rightExpression) {
        /* GRECLIPSE edit
        super(leftExpression, new Token(Types.COMPARE_TO, "==", -1, -1), rightExpression);
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        */
        this(leftExpression, true, rightExpression);
        // GRECLIPSE end
    }

    // GRECLIPSE add
    public CompareIdentityExpression(final Expression leftExpression, final boolean eq, final Expression rightExpression) {
        super(leftExpression, Token.newSymbol(eq ? "===" : "!==", -1, -1), rightExpression);
        super.setType(ClassHelper.boolean_TYPE);
    }

    public boolean isEq() {
        return getOperation().getText().charAt(0) == '=';
    }

    @Override
    public void setType(final org.codehaus.groovy.ast.ClassNode type) {
        throw new UnsupportedOperationException();
    }
    // GRECLIPSE end

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new CompareIdentityExpression(transformer.transform(getLeftExpression()), isEq(), transformer.transform(getRightExpression()));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        if (visitor instanceof AsmClassGenerator) {
            WriterController controller = ((AsmClassGenerator) visitor).getController();
            MethodVisitor mv = controller.getMethodVisitor();
            getLeftExpression().visit(visitor);
            controller.getOperandStack().box();
            getRightExpression().visit(visitor);
            controller.getOperandStack().box();
            Label l1 = new Label();
            // GRECLIPSE add -- GROOVY-10377
            if (!isEq())
                mv.visitJumpInsn(IF_ACMPEQ, l1);
            else
            // GRECLIPSE end
            mv.visitJumpInsn(IF_ACMPNE, l1);
            mv.visitInsn(ICONST_1);
            Label l2 = new Label();
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l1);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l2);
            controller.getOperandStack().replace(ClassHelper.boolean_TYPE, 2);
        } else {
            super.visit(visitor);
        }
    }
}
