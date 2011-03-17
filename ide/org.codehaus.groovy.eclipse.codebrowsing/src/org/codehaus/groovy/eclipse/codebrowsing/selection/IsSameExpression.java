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
package org.codehaus.groovy.eclipse.codebrowsing.selection;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

/**
 * Recursively checks to see if two expressions are the same.
 *
 * Note that we ignore annotations and generics. Also, see the
 * caveat in {@link FindAllOccurrencesVisitor}.
 *
 * @author andrew
 * @created May 12, 2010
 */
public class IsSameExpression {

    /**
     * true iff the two expressions match
     */
    public boolean isSame(Expression left, Expression right) {

        if (left == null && right == null) {
            return true;
        }

        if (left == null || right == null) {
            return false;
        }

        if (handleSpecialCases(left, right)) {
            return true;
        }

        if (left.getClass() != right.getClass()) {
            return false;
        }

        // Damn you Java for not having dynamic dispatch
        if (left instanceof ArgumentListExpression) {
            return visit((ArgumentListExpression) left, (ArgumentListExpression) right);
        } else if (left instanceof ArrayExpression) {
            return visit((ArrayExpression) left, (ArrayExpression) right);
        } else if (left instanceof BinaryExpression) {
            return visit((BinaryExpression) left, (BinaryExpression) right);
        } else if (left instanceof BitwiseNegationExpression) {
            return visit((BitwiseNegationExpression) left, (BitwiseNegationExpression) right);
        } else if (left instanceof BooleanExpression) {
            return visit((BooleanExpression) left, (BooleanExpression) right);
        } else if (left instanceof CastExpression) {
            return visit((CastExpression) left, (CastExpression) right);
        } else if (left instanceof ClassExpression) {
            return visit((ClassExpression) left, (ClassExpression) right);
        } else if (left instanceof ClosureExpression) {
            return visit((ClosureExpression) left, (ClosureExpression) right);
        } else if (left instanceof ConstantExpression) {
            return visit((ConstantExpression) left, (ConstantExpression) right);
        } else if (left instanceof ConstructorCallExpression) {
            return visit((ConstructorCallExpression) left, (ConstructorCallExpression) right);
        } else if (left instanceof EmptyExpression) {
            return visit((EmptyExpression) left, (EmptyExpression) right);
        } else if (left instanceof FieldExpression) {
            return visit((FieldExpression) left, (FieldExpression) right);
        } else if (left instanceof GStringExpression) {
            return visit((GStringExpression) left, (GStringExpression) right);
        } else if (left instanceof ListExpression) {
            // also ClosureListExpression
            return visit((ListExpression) left, (ListExpression) right);
        } else if (left instanceof MapExpression) {
            // also NamedArgumentListExpression
            return visit((MapExpression) left, (MapExpression) right);
        } else if (left instanceof MethodCallExpression) {
            return visit((MethodCallExpression) left, (MethodCallExpression) right);
        } else if (left instanceof MethodPointerExpression) {
            return visit((MethodPointerExpression) left, (MethodPointerExpression) right);
        } else if (left instanceof PostfixExpression) {
            return visit((PostfixExpression) left, (PostfixExpression) right);
        } else if (left instanceof PrefixExpression) {
            return visit((PrefixExpression) left, (PrefixExpression) right);
        } else if (left instanceof PropertyExpression) {
            return visit((PropertyExpression) left, (PropertyExpression) right);
        } else if (left instanceof RangeExpression) {
            return visit((RangeExpression) left, (RangeExpression) right);
        } else if (left instanceof SpreadExpression) {
            return visit((SpreadExpression) left, (SpreadExpression) right);
        } else if (left instanceof SpreadMapExpression) {
            return visit((SpreadMapExpression) left, (SpreadMapExpression) right);
        } else if (left instanceof StaticMethodCallExpression) {
            return visit((StaticMethodCallExpression) left, (StaticMethodCallExpression) right);
        } else if (left instanceof TernaryExpression) {
            // also ElvisOperatorExpression
            return visit((TernaryExpression) left, (TernaryExpression) right);
        } else if (left instanceof TupleExpression) {
            // ArgumentListExpression
            return visit((TupleExpression) left, (TupleExpression) right);
        } else if (left instanceof UnaryMinusExpression) {
            return visit((UnaryMinusExpression) left, (UnaryMinusExpression) right);
        } else if (left instanceof UnaryPlusExpression) {
            return visit((UnaryPlusExpression) left, (UnaryPlusExpression) right);
        } else if (left instanceof VariableExpression) {
            return visit((VariableExpression) left, (VariableExpression) right);
        } else {
            return false;
        }
    }

    /**
     * There are some special cases where the left and right expressions may not
     * be of the same type, but they still match
     */
    private boolean handleSpecialCases(Expression left, Expression right) {
        if (left instanceof ConstantExpression && right instanceof ClassExpression) {
            return right.getType().getName().equals(((ConstantExpression) left).getValue());
        } else if (left instanceof ClassExpression && right instanceof ConstantExpression) {
            return left.getType().getName().equals(((ConstantExpression) right).getValue());
        }
        return false;
    }

    private boolean visit(VariableExpression left, VariableExpression right) {
        return left.getName().equals(right.getName());
    }

    private boolean visit(UnaryPlusExpression left, UnaryPlusExpression right) {
        return isSame(left.getExpression(), right.getExpression());
    }

    private boolean visit(UnaryMinusExpression left, UnaryMinusExpression right) {
        return isSame(left.getExpression(), right.getExpression());
    }

    private boolean visit(TupleExpression left, TupleExpression right) {
        return checkExpressionList(left.getExpressions(), right.getExpressions());
    }

    private boolean visit(TernaryExpression left, TernaryExpression right) {
        return isSame(left.getBooleanExpression(), right.getBooleanExpression()) &&
               isSame(left.getTrueExpression(), right.getTrueExpression()) &&
               isSame(left.getFalseExpression(), right.getFalseExpression());
    }


    private boolean visit(StaticMethodCallExpression left, StaticMethodCallExpression right) {
        return visit(left.getType(), right.getType()) && left.getMethod().equals(right.getMethod()) &&
              isSame(left.getArguments(), right.getArguments()) ;
    }


    private boolean visit(SpreadExpression left, SpreadExpression right) {
        return isSame(left.getExpression(), right.getExpression());
    }


    private boolean visit(SpreadMapExpression left, SpreadMapExpression right) {
        return isSame(left.getExpression(), right.getExpression());
    }


    private boolean visit(RangeExpression left, RangeExpression right) {
        return isSame(left.getFrom(), right.getFrom()) && isSame(left.getTo(), right.getTo());
    }


    private boolean visit(PropertyExpression left, PropertyExpression right) {
        return isSame(left.getObjectExpression(), right.getObjectExpression()) && isSame(left.getProperty(), right.getProperty());
    }


    private boolean visit(PrefixExpression left, PrefixExpression right) {
        return nullEquals(left.getOperation(), (right.getOperation())) && isSame(left.getExpression(), right.getExpression());
    }


    private boolean visit(PostfixExpression left, PostfixExpression right) {
        return nullEquals(left.getOperation(), (right.getOperation())) && isSame(left.getExpression(), right.getExpression());
    }


    private boolean visit(MethodPointerExpression left, MethodPointerExpression right) {
        return isSame(left.getExpression(), right.getExpression()) &&
               isSame(left.getMethodName(), right.getMethodName());
    }


    private boolean visit(MethodCallExpression left, MethodCallExpression right) {
        return isSame(left.getObjectExpression(), right.getObjectExpression()) &&
               isSame(left.getMethod(), right.getMethod()) &&
               isSame(left.getArguments(), right.getArguments());
    }


    private boolean visit(MapExpression left, MapExpression right) {
        return checkExpressionList(left.getMapEntryExpressions(), right.getMapEntryExpressions());
    }


    private boolean visit(ListExpression left, ListExpression right) {
        return checkExpressionList(left.getExpressions(), right.getExpressions());
    }


    private boolean visit(GStringExpression left, GStringExpression right) {
        return checkExpressionList(left.getStrings(), right.getStrings()) &&
               checkExpressionList(left.getValues(), right.getValues());
    }


    private boolean visit(FieldExpression left, FieldExpression right) {
        return visit(left.getField().getDeclaringClass(), right.getField().getDeclaringClass())
                && nullEquals(left.getFieldName(), right.getFieldName());
    }


    private boolean visit(EmptyExpression left, EmptyExpression right) {
        return true;
    }


    private boolean visit(ConstructorCallExpression left, ConstructorCallExpression right) {
        return visit(left.getType(), right.getType()) && isSame(left.getArguments(), right.getArguments());
    }


    private boolean visit(ConstantExpression left, ConstantExpression right) {
        return nullEquals(left.getText(), right.getText());
    }


    private boolean visit(ClosureExpression left, ClosureExpression right) {
        return false;  // not implemented yet because we can't compare statements
    }


    private boolean visit(ClassExpression left, ClassExpression right) {
        return visit(left.getType(), right.getType());
    }


    private boolean visit(CastExpression left, CastExpression right) {
        return visit(left.getType(), right.getType()) && isSame(left.getExpression(), right.getExpression());
    }


    private boolean visit(BooleanExpression left, BooleanExpression right) {
        return isSame(left.getExpression(), right.getExpression());
    }


    private boolean visit(BitwiseNegationExpression left, BitwiseNegationExpression right) {
        return isSame(left.getExpression(), right.getExpression());
    }


    private boolean visit(BinaryExpression left, BinaryExpression right) {
        return left.getOperation().getType() == right.getOperation().getType()
                && isSame(left.getLeftExpression(), right.getLeftExpression())
                && isSame(left.getRightExpression(), right.getRightExpression());
    }

    private boolean visit(ArrayExpression left, ArrayExpression right) {
        return checkExpressionList(left.getExpressions(), right.getExpressions()) &&
               checkExpressionList(left.getSizeExpression(), right.getSizeExpression());
    }

    private boolean visit(ClassNode left, ClassNode right) {
        return nullEquals(left, right);
    }

    private boolean nullEquals(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null || right == null) {
            return false;
        } else {
            return left.equals(right);
        }
    }

    private <T extends Expression> boolean checkExpressionList(List<T> left, List<T> right) {
        if (right == null && left == null) {
            return true;
        } else if (right == null || left == null) {
            return false;
        } else if (left.size() != right.size()) {
            return false;
        }

        Iterator<T> leftIter = left.iterator();
        Iterator<T> rightIter = right.iterator();
        boolean success = true;
        while (leftIter.hasNext() && rightIter.hasNext()) {
            success &= isSame(leftIter.next(), rightIter.next());
        }
        return success;
    }
}
