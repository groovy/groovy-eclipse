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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

import java.util.Map;

/**
 * Represents a constant expression such as null, true, false.
 */
public class ConstantExpression extends Expression {
    // The following fields are only used internally; every occurrence of a user-defined expression of the same kind
    // has its own instance so as to preserve line information. Consequently, to test for such an expression, don't
    // compare against the field but call isXXXExpression() instead.
    public static final ConstantExpression NULL = new StaticConstantExpression(null);
    public static final ConstantExpression TRUE = new StaticConstantExpression(Boolean.TRUE);
    public static final ConstantExpression FALSE = new StaticConstantExpression(Boolean.FALSE);
    public static final ConstantExpression EMPTY_STRING = new StaticConstantExpression("");
    public static final ConstantExpression PRIM_TRUE = new StaticConstantExpression(Boolean.TRUE, true);
    public static final ConstantExpression PRIM_FALSE = new StaticConstantExpression(Boolean.FALSE, true);

    // the following fields are only used internally; there are no user-defined expressions of the same kind
    public static final ConstantExpression VOID = new StaticConstantExpression(Void.class);
    public static final ConstantExpression EMPTY_EXPRESSION = new StaticConstantExpression(null);

    private final Object value;
    private String constantName;

    public ConstantExpression(Object value) {
        this(value, false);
    }

    public ConstantExpression(Object value, boolean keepPrimitive) {
        this.value = value;
        if (value != null) {
            if (keepPrimitive) {
                if (value instanceof Integer) {
                    setType(ClassHelper.int_TYPE);
                } else if (value instanceof Long) {
                    setType(ClassHelper.long_TYPE);
                } else if (value instanceof Boolean) {
                    setType(ClassHelper.boolean_TYPE);
                } else if (value instanceof Double) {
                    setType(ClassHelper.double_TYPE);
                } else if (value instanceof Float) {
                    setType(ClassHelper.float_TYPE);
                } else if (value instanceof Character) {
                    setType(ClassHelper.char_TYPE);
                } else {
                    setType(ClassHelper.make(value.getClass()));
                }
                //TODO: more cases here
            } else {
                setType(ClassHelper.make(value.getClass()));
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[" + value + "]";
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitConstantExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    /**
     * @return the value of this constant expression
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String getText() {
        return (value == null ? "null" : value.toString());
    }

    public String getConstantName() {
        return constantName;
    }

    public void setConstantName(String constantName) {
        this.constantName = constantName;
    }

    public boolean isNullExpression() {
        return value == null;
    }

    public boolean isTrueExpression() {
        return Boolean.TRUE.equals(value);
    }

    public boolean isFalseExpression() {
        return Boolean.FALSE.equals(value);
    }

    public boolean isEmptyStringExpression() {
        return "".equals(value);
    }
}

// GRECLIPSE add
class StaticConstantExpression extends ConstantExpression {

    public StaticConstantExpression(Object value) {
        super(value);
    }

    public StaticConstantExpression(Object value, boolean keepPrimitive) {
        super(value, keepPrimitive);
    }

    // ASTNode overrides:

    @Override
    public void setColumnNumber(int n) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setLastColumnNumber(int n) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setLastLineNumber(int n) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setLineNumber(int n) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setMetaDataMap(Map meta) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setSourcePosition(ASTNode node) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setStart(int i) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setEnd(int i) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    // AnnotatedNode overrides:

    @Override
    public void addAnnotation(AnnotationNode node) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setDeclaringClass(ClassNode node) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setHasNoRealSourcePosition(boolean b) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setNameStart(int i) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setNameEnd(int i) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setSynthetic(boolean b) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    // ConstantExpression overrides:

    @Override
    public void setConstantName(String name) {
        throw new GroovyBugError("Attempt to change static constant expression: " + getText());
    }

    @Override
    public void setType(ClassNode type) {
        if (!isNullExpression() && getType() == ClassHelper.DYNAMIC_TYPE) {
            super.setType(type); // allow one-time initialization
        } else {
            throw new GroovyBugError("Attempt to change static constant expression: " + getText());
        }
    }
}
// GRECLIPSE end
