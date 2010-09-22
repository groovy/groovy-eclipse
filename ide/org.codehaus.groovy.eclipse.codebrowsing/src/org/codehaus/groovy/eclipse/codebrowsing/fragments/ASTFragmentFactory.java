/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.fragments;

import java.util.ArrayList;
import java.util.List;

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
 * Factory class to create an AST Fragment for a given expression.
 * Only works on expressions now, but consider expanding to statements in the
 * future if required.
 *
 * @author andrew
 * @created Jun 4, 2010
 */
public class ASTFragmentFactory {

    public IASTFragment createFragment(ASTNode node) {
        if (node instanceof Expression) {
            return createFragment((Expression) node);
        } else {
            return new EnclosingASTNodeFragment(node);
        }
    }

    public IASTFragment createFragment(Expression expression, int start, int end) {
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
                // we have a declaration, must include the type in the start
                // position
                if (fragment.kind() == ASTFragmentKind.BINARY) {
                    ((BinaryExpressionFragment) fragment).setActualStartPosition(((DeclarationExpression) expression).getStart());
                } else if (fragment.kind() == ASTFragmentKind.SIMPLE_EXPRESSION) {
                    // declaration without an assignment
                    ((SimpleExpressionASTFragment) fragment)
                            .setActualStartPosition(((DeclarationExpression) expression).getStart());
                }
            }
            return fragment;
        } else if (expression instanceof PropertyExpression) {
            return createPropertyBasedFragment(expression, start, end);
        } else if (expression instanceof MethodCallExpression) {
            return createPropertyBasedFragment(expression, start, end);
        } else if (expression instanceof MethodPointerExpression) {
            return createPropertyBasedFragment(expression, start, end);
        } else if (expression.getStart() == start && expression.getEnd() == end) {
            return new SimpleExpressionASTFragment(expression);
        } else {
            return new EmptyASTFragment();
        }
    }

    public IASTFragment createFragment(Expression expression) {
        return createFragment(expression, expression.getStart(), expression.getEnd());
    }

    private IASTFragment createBinaryFragment(BinaryExpression expression, int start, int end) {

        // tokens list will be one shorter than the exprs list
        List<Expression> exprs = new ArrayList<Expression>();
        List<Token> tokens = new ArrayList<Token>();
        walkBinaryExpr(expression, exprs, tokens);
        Expression firstExpr = exprs.get(exprs.size() - 1);
        IASTFragment next = new EmptyASTFragment();
        if (firstExpr.getStart() >= start && firstExpr.getEnd() <= end) {
            next = new SimpleExpressionASTFragment(firstExpr);
        }
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Expression nextExpr = exprs.get(i);
            if (nextExpr.getStart() >= start && nextExpr.getEnd() <= end) {
                if (next.kind() == ASTFragmentKind.EMPTY) {
                    next = new SimpleExpressionASTFragment(nextExpr);
                } else {
                    next = new BinaryExpressionFragment(tokens.get(i), nextExpr, next);
                }
            }
        }
        return next;
    }

    /**
     * Flattens a binary expression into components
     */
    private void walkBinaryExpr(BinaryExpression expression, List<Expression> exprs, List<Token> tokens) {
        if (expression.getLeftExpression() instanceof BinaryExpression) {
            walkBinaryExpr((BinaryExpression) expression.getLeftExpression(), exprs, tokens);
        } else {
            exprs.add(expression.getLeftExpression());
        }
        tokens.add(expression.getOperation());
        if (expression.getRightExpression() instanceof BinaryExpression) {
            walkBinaryExpr((BinaryExpression) expression.getRightExpression(), exprs, tokens);
        } else {
            exprs.add(expression.getRightExpression());
        }
    }


    private IASTFragment createPropertyBasedFragment(Expression expression, int start, int end) {
        // kinds list will be same size as exprs list
        // args list will contain nulls for items that are not method calls
        List<ASTFragmentKind> kinds = new ArrayList<ASTFragmentKind>();

        // all of the target expressions
        List<Expression> exprs = new ArrayList<Expression>();

        // ensure that method calls include their arguments
        List<Expression> args = new ArrayList<Expression>();

        // ensure that method calls include the closing paren
        List<Integer> ends = new ArrayList<Integer>();

        walkPropertyExpr(expression, exprs, args, kinds, ends);

        // return an empty fragment if there are no complete matches
        IASTFragment prev = new EmptyASTFragment();

		// closure calls are implicitly converted into method calls with 'call'
		// s the method name
		// we need to handle this case specially and ignore this and make the
		// next fragment a method call instead
		boolean wasImplicitCall = false;
        Expression firstExpr = exprs.get(exprs.size() - 1);
        if (firstExpr.getStart() >= start && firstExpr.getEnd() <= end) {
            if (kinds.get(kinds.size() - 1) == ASTFragmentKind.METHOD_CALL) {
                prev = new MethodCallFragment(firstExpr, args.get(exprs.size() - 1), ends.get(exprs.size() - 1));
            } else {
                prev = new SimpleExpressionASTFragment(firstExpr);
            }
		} else if (checkWasImplicitCall(firstExpr)) {
			wasImplicitCall = true;
        }
        for (int i = exprs.size() - 2; i >= 0; i--) {
            Expression expr = exprs.get(i);
            if (expr.getStart() >= start && expr.getEnd() <= end) {
                if (kinds.get(i) == ASTFragmentKind.METHOD_CALL) {
                    prev = new MethodCallFragment(expr, args.get(i), prev.kind() == ASTFragmentKind.EMPTY ? null : prev, ends
                            .get(i));
                } else {
                    if (prev.kind() == ASTFragmentKind.EMPTY) {
						if (!wasImplicitCall) {
							prev = new SimpleExpressionASTFragment(expr);
						} else {
							prev = new MethodCallFragment(expr, args.get(i + 1), null, ends.get(i));
						}
                    } else {
						if (!wasImplicitCall) {
							prev = new PropertyExpressionFragment(kinds.get(i), expr, prev);
						} else {
							prev = new MethodCallFragment(expr, args.get(i + 1), prev, ends.get(i));
						}
                    }
                }
				wasImplicitCall = false;
			} else if (checkWasImplicitCall(expr)) {
				wasImplicitCall = true;
            }
        }
        return prev;
    }

	/**
	 * @param firstExpr
	 * @return
	 */
	private boolean checkWasImplicitCall(Expression firstExpr) {
		return firstExpr.getEnd() == 0 && firstExpr instanceof ConstantExpression
				&& ((ConstantExpression) firstExpr).getText().equals("call");
	}

    private void walkPropertyExpr(Expression startExpression, List<Expression> targetExprs, List<Expression> targetArgs,
            List<ASTFragmentKind> targetKinds, List<Integer> targetEnds) {
        if (startExpression instanceof PropertyExpression) {
            PropertyExpression propertyExpression = (PropertyExpression) startExpression;
            ASTFragmentKind kind = ASTFragmentKind.toPropertyKind(propertyExpression);
            walkPropertyExpr(propertyExpression.getObjectExpression(), targetExprs, targetArgs, targetKinds, targetEnds);
            int toRemove = targetKinds.size() - 1;
            if (targetKinds.get(toRemove) == ASTFragmentKind.SIMPLE_EXPRESSION) {
                // need to replace with a property
                targetKinds.set(toRemove, kind);
                targetArgs.set(toRemove, null);
            }
            walkPropertyExpr(propertyExpression.getProperty(), targetExprs, targetArgs, targetKinds, targetEnds);
        } else if (startExpression instanceof MethodCallExpression) {
            MethodCallExpression methodCallExpression = (MethodCallExpression) startExpression;
            walkPropertyExpr(methodCallExpression.getObjectExpression(), targetExprs, targetArgs, targetKinds, targetEnds);
            int toRemove = targetKinds.size() - 1;
            if (targetKinds.get(toRemove) == ASTFragmentKind.SIMPLE_EXPRESSION) {
                // need to replace with a property
                targetKinds.set(toRemove, ASTFragmentKind.PROPERTY);
                targetArgs.set(toRemove, null);
            }
            walkPropertyExpr(methodCallExpression.getMethod(), targetExprs, targetArgs, targetKinds, targetEnds);
            // replace the last with method call since we need to add arguments
            // here
            toRemove = targetKinds.size() - 1;
            targetKinds.set(toRemove, ASTFragmentKind.METHOD_CALL);
            targetArgs.set(toRemove, methodCallExpression.getArguments());
            targetEnds.set(toRemove, methodCallExpression.getEnd());
        } else if (startExpression instanceof MethodPointerExpression) {
            MethodPointerExpression methodPointerExpression = (MethodPointerExpression) startExpression;
            walkPropertyExpr(methodPointerExpression.getExpression(), targetExprs, targetArgs, targetKinds, targetEnds);
            int toRemove = targetKinds.size() - 1;
            if (targetKinds.get(toRemove) == ASTFragmentKind.SIMPLE_EXPRESSION) {
                // need to replace with a method pointer
                targetKinds.set(toRemove, ASTFragmentKind.METHOD_POINTER);
                targetArgs.set(toRemove, null);
            }
            walkPropertyExpr(methodPointerExpression.getMethodName(), targetExprs, targetArgs, targetKinds, targetEnds);
        } else {
            // we should only have this kind of fragment as the last one,
            targetKinds.add(ASTFragmentKind.SIMPLE_EXPRESSION);
            targetArgs.add(null);
            targetExprs.add(startExpression);
            targetEnds.add(startExpression.getEnd());
        }
    }

    public static String spaces(int num) {
        StringBuilder sb = new StringBuilder();
        while (num-- > 0) {
            sb.append("  ");
        }
        return sb.toString();
    }
}
