/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

public class CompareToNullExpression extends BinaryExpression {
    @SuppressWarnings("unused")
    private final boolean equalsNull;
    private final Expression objectExpression;

    public CompareToNullExpression(Expression objectExpression, boolean compareToNull) {
        super(objectExpression, new Token(Types.COMPARE_TO, compareToNull ? "==" : "!=", -1, -1), ConstantExpression.NULL);
        this.objectExpression = objectExpression;
        this.equalsNull = compareToNull;
    }

    public Expression getObjectExpression() {
        return objectExpression;
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
    }
}
