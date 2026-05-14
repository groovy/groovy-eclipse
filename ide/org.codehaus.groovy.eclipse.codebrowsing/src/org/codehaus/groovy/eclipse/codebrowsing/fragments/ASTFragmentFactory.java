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
package org.codehaus.groovy.eclipse.codebrowsing.fragments;

import java.util.ArrayList;
import java.util.List;

import groovy.lang.Reference;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.syntax.Token;
import org.eclipse.core.runtime.Assert;

/**
 * Factory class to create an AST Fragment for a given expression. Only works on
 * expressions now, but consider expanding to statements in the future.
 */
public class ASTFragmentFactory {

    public IASTFragment createFragment(final ASTNode node) {
        if (node instanceof Expression) {
            return createFragment((Expression) node, node.getStart(), node.getEnd());
        } else {
            return new EnclosingASTNodeFragment(node);
        }
    }

    public IASTFragment createFragment(final Expression expression) {
        return createFragment(expression, expression.getStart(), expression.getEnd());
    }

    public IASTFragment createFragment(Expression expression, final int start, final int end) {
        Assert.isLegal(expression.getStart() <= start, "Invalid start position: " + start);
        Assert.isLegal(expression.getEnd() >= end, "Invalid end position: " + end);

        // do some unwrapping
        if (expression instanceof BooleanExpression) {
            Expression inner = ((BooleanExpression) expression).getExpression();
            if (inner.getStart() == expression.getStart() && inner.getEnd() == expression.getEnd()) {
                expression = inner;
            }
        } else if (expression instanceof TupleExpression && ((TupleExpression) expression).getExpressions().size() == 1) {
            Expression inner = ((TupleExpression) expression).getExpression(0);
            if (inner.getStart() == expression.getStart() && inner.getEnd() == expression.getEnd()) {
                expression = inner;
            }
        }

        if (expression instanceof BinaryExpression) {
            IASTFragment fragment = createBinaryFragment((BinaryExpression) expression, start, end);
            if (expression instanceof DeclarationExpression) {
                // for declaration, include the type in the start position
                if (fragment.kind() == ASTFragmentKind.BINARY) {
                    ((BinaryExpressionFragment) fragment).setActualStartPosition(((DeclarationExpression) expression).getStart());
                } else if (fragment.kind() == ASTFragmentKind.SIMPLE_EXPRESSION) { // declaration without an assignment
                    ((SimpleExpressionASTFragment) fragment).setActualStartPosition(((DeclarationExpression) expression).getStart());
                }
            }
            return fragment;
        } else if (expression instanceof PropertyExpression) {
            return createPropertyFragment(expression, start, end);
        } else if (expression instanceof MethodCallExpression) {
            return createPropertyFragment(expression, start, end);
        } else if (expression instanceof MethodPointerExpression) {
            return createPropertyFragment(expression, start, end);
        } else if (expression.getStart() == start && expression.getEnd() == end) {
            return new SimpleExpressionASTFragment(expression);
        } else {
            return new EmptyASTFragment();
        }
    }

    private IASTFragment createBinaryFragment(final BinaryExpression expression, final int start, final int end) {
        // tokens list will be one shorter than the exprs list
        List<Expression> expressions = new ArrayList<>();
        List<Token> tokens = new ArrayList<>();

        Reference<java.util.function.Consumer<BinaryExpression>> walker = new Reference<>();
        walker.set((BinaryExpression bin) -> {
            if (bin.getLeftExpression() instanceof BinaryExpression) {
                walker.get().accept((BinaryExpression) bin.getLeftExpression());
            } else {
                expressions.add(bin.getLeftExpression());
            }

            tokens.add(bin.getOperation());

            if (bin.getRightExpression() instanceof BinaryExpression) {
                walker.get().accept((BinaryExpression) bin.getRightExpression());
            } else {
                expressions.add(bin.getRightExpression());
            }
        });
        walker.get().accept(expression);

        IASTFragment fragment = new EmptyASTFragment();
        Expression lastExpr = expressions.get(expressions.size() - 1);
        if (lastExpr.getStart() >= start && lastExpr.getEnd() <= end) {
            fragment = new SimpleExpressionASTFragment(lastExpr);
        }
        for (int i = tokens.size() - 1; i >= 0; i -= 1) {
            Expression nextExpr = expressions.get(i);
            if (nextExpr.getStart() >= start && nextExpr.getEnd() <= end) {
                if (fragment.kind() == ASTFragmentKind.EMPTY) {
                    fragment = new SimpleExpressionASTFragment(nextExpr);
                } else {
                    fragment = new BinaryExpressionFragment(tokens.get(i), nextExpr, fragment);
                }
            }
        }
        return fragment;
    }

    private IASTFragment createPropertyFragment(final Expression expression, final int start, final int end) {
        // kinds list will be same size as exprs list
        // args list will contain nulls for items that are not method calls
        List<ASTFragmentKind> kinds = new ArrayList<>();

        // all of the target expressions
        List<Expression> exprs = new ArrayList<>();

        // ensure that method calls include their arguments
        List<Expression> args = new ArrayList<>();

        // ensure that method calls include the closing paren
        List<Integer> ends = new ArrayList<>();

        Reference<java.util.function.Consumer<Expression>> walker = new Reference<>();
        walker.set(nextExpression -> {
            ASTFragmentKind kind = ASTFragmentKind.SIMPLE_EXPRESSION;
            Expression last = nextExpression, aExpr = null;
            Integer until = null;

            if (nextExpression instanceof PropertyExpression) {
                PropertyExpression propExpression = (PropertyExpression) nextExpression;
                walker.get().accept(propExpression.getObjectExpression());
                last = propExpression.getProperty();
                int lastIndex = kinds.size() - 1;
                if (kinds.get(lastIndex) == ASTFragmentKind.SIMPLE_EXPRESSION) {
                    kinds.set(lastIndex, ASTFragmentKind.toPropertyKind(propExpression));
                    args.set(lastIndex, null);
                }
            } else if (nextExpression instanceof MethodCallExpression) {
                MethodCallExpression callExpression = (MethodCallExpression) nextExpression;
                walker.get().accept(callExpression.getObjectExpression());
                int lastIndex = kinds.size() - 1;
                if (kinds.get(lastIndex) == ASTFragmentKind.SIMPLE_EXPRESSION) {
                    kinds.set(lastIndex, callExpression.isSpreadSafe() ? ASTFragmentKind.SPREAD_SAFE_PROPERTY
                        : callExpression.isSafe() ? ASTFragmentKind.SAFE_PROPERTY : ASTFragmentKind.PROPERTY);
                    args.set(lastIndex, null);
                }
                kind = ASTFragmentKind.METHOD_CALL;
                last = callExpression.getMethod();
                aExpr = callExpression.getArguments();
                until = callExpression.getEnd();
                //
            } else if (nextExpression instanceof MethodPointerExpression) {
                walker.get().accept(((MethodPointerExpression) nextExpression).getExpression());
                last = ((MethodPointerExpression) nextExpression).getMethodName();
                int lastIndex = kinds.size() - 1;
                if (kinds.get(lastIndex) == ASTFragmentKind.SIMPLE_EXPRESSION) {
                    kinds.set(lastIndex, ASTFragmentKind.METHOD_POINTER);
                    args.set(lastIndex, null);
                }
            }

            if (until == null)
                until = last.getEnd();

            kinds.add(kind);
            exprs.add(last);
            args.add(aExpr);
            ends.add(until);
        });
        walker.get().accept(expression);

        // closure calls are implicitly converted into method calls with 'call' as the method name
        // we need to handle this case specially and ignore this and make the next fragment a method call instead
        boolean wasImplicitCall = false;

        IASTFragment fragment = new EmptyASTFragment();
        Expression lastExpr = exprs.get(exprs.size() - 1);
        if (lastExpr.getStart() >= start && lastExpr.getEnd() <= end) {
            if (kinds.get(kinds.size() - 1) == ASTFragmentKind.METHOD_CALL) {
                MethodCallFragment frag = new MethodCallFragment(lastExpr, args.get(exprs.size() - 1), ends.get(exprs.size() - 1));
                frag.callExpression = (MethodCallExpression) expression;
                fragment = frag;
            } else {
                fragment = new SimpleExpressionASTFragment(lastExpr);
            }
        } else if (isCallImplicit(lastExpr)) {
            wasImplicitCall = true;
        }
        for (int i = exprs.size() - 2; i >= 0; i -= 1) {
            Expression expr = exprs.get(i);
            if (expr.getStart() >= start && expr.getEnd() <= end) {
                if (kinds.get(i) == ASTFragmentKind.METHOD_CALL) {
                    fragment = new MethodCallFragment(expr, args.get(i), fragment.kind() == ASTFragmentKind.EMPTY ? null : fragment, ends.get(i));
                } else {
                    if (fragment.kind() == ASTFragmentKind.EMPTY) {
                        if (!wasImplicitCall) {
                            fragment = new SimpleExpressionASTFragment(expr);
                        } else {
                            fragment = new MethodCallFragment(expr, args.get(i + 1), null, ends.get(i));
                        }
                    } else {
                        if (!wasImplicitCall) {
                            fragment = new PropertyExpressionFragment(kinds.get(i), expr, fragment);
                        } else {
                            fragment = new MethodCallFragment(expr, args.get(i + 1), fragment, ends.get(i));
                        }
                    }
                }
                wasImplicitCall = false;
            } else if (isCallImplicit(expr)) {
                wasImplicitCall = true;
            }
        }
        return fragment;
    }

    private static boolean isCallImplicit(final Expression e) {
        return e.getEnd() == 0 && e instanceof ConstantExpression && ((ConstantExpression) e).getText().equals("call");
    }

    public static String spaces(int num) {
        StringBuilder sb = new StringBuilder();
        while (num-- > 0) {
            sb.append("  ");
        }
        return sb.toString();
    }
}
