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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.sc.StaticPropertyAccessHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.sc.TemporaryVariableExpression;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

public class BinaryExpressionTransformer {
    private static final MethodNode COMPARE_TO_METHOD = ClassHelper.COMPARABLE_TYPE.getMethods("compareTo").get(0);
    private static final ConstantExpression CONSTANT_MINUS_ONE = constX(-1, true);
    private static final ConstantExpression CONSTANT_ZERO = constX(0, true);
    private static final ConstantExpression CONSTANT_ONE = constX(1, true);

    private int tmpVarCounter;

    private final StaticCompilationTransformer staticCompilationTransformer;

    public BinaryExpressionTransformer(final StaticCompilationTransformer staticCompilationTransformer) {
        this.staticCompilationTransformer = staticCompilationTransformer;
    }

    public Expression transformBinaryExpression(final BinaryExpression bin) {
        if (bin instanceof DeclarationExpression) {
            Expression optimized = transformDeclarationExpression(bin);
            if (optimized != null) {
                return optimized;
            }
        }
        Object[] list = bin.getNodeMetaData(StaticCompilationMetadataKeys.BINARY_EXP_TARGET);
        Token operation = bin.getOperation();
        int operationType = operation.getType();
        Expression rightExpression = bin.getRightExpression();
        Expression leftExpression = bin.getLeftExpression();
        if (bin instanceof DeclarationExpression && leftExpression instanceof VariableExpression) {
            ClassNode declarationType = ((VariableExpression) leftExpression).getOriginType();
            if (rightExpression instanceof ConstantExpression) {
                ClassNode unwrapper = ClassHelper.getUnwrapper(declarationType);
                ClassNode wrapper = ClassHelper.getWrapper(declarationType);
                if (!rightExpression.getType().equals(declarationType)
                        && wrapper.isDerivedFrom(ClassHelper.Number_TYPE)
                        && WideningCategories.isDoubleCategory(unwrapper)) {
                    ConstantExpression constant = (ConstantExpression) rightExpression;
                    if (!constant.isNullExpression()) {
                        return optimizeConstantInitialization(bin, operation, constant, leftExpression, declarationType);
                    }
                }
            }
        }
        if (operationType == Types.ASSIGN) {
            MethodNode directMCT = leftExpression.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
            if (directMCT != null) {
                Expression left = staticCompilationTransformer.transform(leftExpression);
                Expression right = staticCompilationTransformer.transform(rightExpression);
                if (left instanceof PropertyExpression) {
                    // transform "a.x = val" into "def tmp = val; a.setX(tmp); tmp"
                    PropertyExpression pe = (PropertyExpression) left;
                    return transformAssignmentToSetterCall(
                            pe.getObjectExpression(), // "a"
                            directMCT, // "setX"
                            right, // "val"
                            pe.isImplicitThis(),
                            pe.isSafe(),
                            pe.getProperty(), // "x"
                            bin // "a.x = val"
                    );
                } else if (left instanceof VariableExpression) {
                    // transform "x = val" into "def tmp = val; this.setX(tmp); tmp"
                    return transformAssignmentToSetterCall(
                            varX("this"),
                            directMCT, // "setX"
                            right, // "val"
                            true,
                            false,
                            left, // "x"
                            bin // "x = val"
                    );
                }
            }
        } else if (operationType == Types.COMPARE_EQUAL || operationType == Types.COMPARE_NOT_EQUAL) {
            // let's check if one of the operands is the null constant
            CompareToNullExpression compareToNullExpression = null;
            if (isNullConstant(leftExpression)) {
                compareToNullExpression = new CompareToNullExpression(staticCompilationTransformer.transform(rightExpression), operationType == Types.COMPARE_EQUAL);
            } else if (isNullConstant(rightExpression)) {
                compareToNullExpression = new CompareToNullExpression(staticCompilationTransformer.transform(leftExpression), operationType == Types.COMPARE_EQUAL);
            }
            if (compareToNullExpression != null) {
                compareToNullExpression.setSourcePosition(bin);
                return compareToNullExpression;
            }
        // GRECLIPSE add -- GROOVY-10377, GROOVY-10395
        } else if (operationType == Types.COMPARE_IDENTICAL || operationType == Types.COMPARE_NOT_IDENTICAL) {
            if (isNullConstant(rightExpression)) {
                CompareToNullExpression ctn = new CompareToNullExpression(staticCompilationTransformer.transform(leftExpression), operationType == Types.COMPARE_IDENTICAL);
                ctn.setSourcePosition(bin);
                return ctn;
            }
            if (isNullConstant(leftExpression)) {
                CompareToNullExpression ctn = new CompareToNullExpression(staticCompilationTransformer.transform(rightExpression), operationType == Types.COMPARE_IDENTICAL);
                ctn.setSourcePosition(bin);
                return ctn;
            }
            if (!ClassHelper.isPrimitiveType(findType(rightExpression))
                    && !ClassHelper.isPrimitiveType(findType(leftExpression))) {
                Expression cid = new CompareIdentityExpression(staticCompilationTransformer.transform(leftExpression), operationType == Types.COMPARE_IDENTICAL, staticCompilationTransformer.transform(rightExpression));
                cid.setSourcePosition(bin);
                return cid;
            }
        } else if (operationType == Types.COMPARE_TO) {
            ClassNode leftType = findType(leftExpression), rightType = findType(rightExpression);
            // same-type primitive compare
            if (leftType.equals(rightType)
                    && ClassHelper.isPrimitiveType(leftType)
                    || ClassHelper.isPrimitiveType(rightType)) {
                ClassNode wrapperType = ClassHelper.getWrapper(leftType);
                Expression leftAndRight = args(
                    staticCompilationTransformer.transform(leftExpression),
                    staticCompilationTransformer.transform(rightExpression)
                );
                // transform "a <=> b" into "[Integer|Long|Short|Byte|Double|Float|...].compare(a,b)"
                MethodCallExpression call = callX(classX(wrapperType), "compare", leftAndRight);
                call.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);
                call.setMethodTarget(wrapperType.getMethods("compare").get(0));
                call.setImplicitThis(false);
                call.setSourcePosition(bin);
                return call;
            }
        // GRECLIPSE end
        } else if (operationType == Types.KEYWORD_IN) {
            return staticCompilationTransformer.transform(convertInOperatorToTernary(bin, rightExpression, leftExpression));
        }
        if (list != null) {
            MethodCallExpression call;
            Expression left = staticCompilationTransformer.transform(leftExpression);
            Expression right = staticCompilationTransformer.transform(rightExpression);

            if (operationType == Types.COMPARE_TO
                    && findType(leftExpression).implementsInterface(ClassHelper.COMPARABLE_TYPE)
                    && findType(rightExpression).implementsInterface(ClassHelper.COMPARABLE_TYPE)) {
                // GRECLIPSE add -- GROOVY-10394
                left = transformRepeatedReference(left);
                right = transformRepeatedReference(right);
                // GRECLIPSE end
                call = callX(left, "compareTo", args(right));
                call.setImplicitThis(false);
                call.setMethodTarget(COMPARE_TO_METHOD);
                call.setSourcePosition(bin);

                // right == null ? 1 : left.compareTo(right)
                Expression expr = ternaryX(
                        boolX(new CompareToNullExpression(right, true)),
                        CONSTANT_ONE,
                        call
                );
                expr.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);

                // left == null ? -1 : (right == null ? 1 : left.compareTo(right))
                expr = ternaryX(
                        boolX(new CompareToNullExpression(left, true)),
                        CONSTANT_MINUS_ONE,
                        expr
                );
                expr.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);

                // left === right ? 0 : (left == null ? -1 : (right == null ? 1 : left.compareTo(right)))
                expr = ternaryX(
                        boolX(new CompareIdentityExpression(left, right)),
                        CONSTANT_ZERO,
                        expr
                );
                expr.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);
                // GRECLIPSE add -- GROOVY-10394
                expr.putNodeMetaData("classgen.callback", classgenCallback(right).andThen(classgenCallback(left)));
                // GRECLIPSE end
                return expr;
            }

            BinaryExpression optimized = tryOptimizeCharComparison(left, right, bin);
            if (optimized != null) {
                optimized.removeNodeMetaData(StaticCompilationMetadataKeys.BINARY_EXP_TARGET);
                optimized.removeNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                return optimized;
            }

            String name = (String) list[1];
            MethodNode node = (MethodNode) list[0];
            boolean isAssignment = Types.isAssignment(operationType);
            Expression expr = left; // TODO: if (isAssignment) scrub source offsets from new copy of left?
            MethodNode adapter = StaticCompilationTransformer.BYTECODE_BINARY_ADAPTERS.get(operationType);
            if (adapter != null) {
                Expression sba = classX(StaticCompilationTransformer.BYTECODE_ADAPTER_CLASS);
                call = callX(sba, adapter.getName(), args(expr, right));
                call.setMethodTarget(adapter);
            } else {
                call = callX(expr, name, args(right));
                call.setMethodTarget(node);
            }
            call.setImplicitThis(false);
            if (!isAssignment) {
                call.setSourcePosition(bin);
                return call;
            }
            // case of +=, -=, /=, ...
            // the method represents the operation type only, and we must add an assignment
            expr = binX(left, Token.newSymbol(Types.EQUAL, operation.getStartLine(), operation.getStartColumn()), call);
            // GRECLIPSE add -- GROOVY-5746, et al.
            if (left instanceof BinaryExpression) {
                BinaryExpression be = (BinaryExpression) left;
                if (be.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
                    be.setLeftExpression(transformRepeatedReference(be.getLeftExpression()));
                    be.setRightExpression(transformRepeatedReference(be.getRightExpression()));
                    expr.putNodeMetaData("classgen.callback", classgenCallback(be.getRightExpression())
                                                     .andThen(classgenCallback(be.getLeftExpression()))
                    );
                }
            }
            expr.putNodeMetaData("original.operator", operation);
            // clone repeat occurrence of left sans source offsets
            call.setObjectExpression(new ExpressionTransformer() {
                @Override
                public Expression transform(final Expression expression) {
                    if (expression == null) return null;
                    Expression transformed = expression.transformExpression(this);
                    if (transformed.getEnd() > 0 || transformed.getLineNumber() > 0) {
                        if (transformed != expression) {
                            transformed.setStart(0);
                            transformed.setEnd( -1);
                            transformed.setNameStart(0);
                            transformed.setNameEnd( -1);
                            transformed.setLineNumber(-1);
                            transformed.setColumnNumber(-1);
                            transformed.setLastLineNumber(-1);
                            transformed.setLastColumnNumber(-1);
                        } else if (expression instanceof ConstantExpression) { ConstantExpression ce = (ConstantExpression) expression;
                            ConstantExpression copy = new ConstantExpression(ce.getValue());
                            copy.setConstantName(ce.getConstantName());
                            copy.setDeclaringClass(ce.getDeclaringClass());
                            copy.setSynthetic(ce.isSynthetic());
                            copy.setType(ce.getType());
                            copy.copyNodeMetaData(ce);
                            transformed = copy;
                        } else if (expression instanceof VariableExpression) { VariableExpression ve = (VariableExpression) expression;
                            VariableExpression copy = new VariableExpression(ve.getName(), ve.getOriginType());
                            copy.setAccessedVariable(ve.getAccessedVariable());
                            copy.setClosureSharedVariable(ve.isClosureSharedVariable());
                            copy.setDeclaringClass(ve.getDeclaringClass());
                            if (ve.isDynamicTyped()) copy.setType(ClassHelper.DYNAMIC_TYPE);
                            copy.setInStaticContext(ve.isInStaticContext());
                            copy.setModifiers(ve.getModifiers());
                            copy.setSynthetic(ve.isSynthetic());
                            copy.setType(ve.getType());
                            copy.setUseReferenceDirectly(ve.isUseReferenceDirectly());
                            copy.copyNodeMetaData(ve);
                            transformed = copy;
                        }
                    }
                    return transformed;
                }
            }.transform(left));
            // GRECLIPSE end
            expr.setSourcePosition(bin);
            return expr;
        }
        if (operationType == Types.ASSIGN && leftExpression instanceof TupleExpression && rightExpression instanceof ListExpression) {
            // multiple assignment
            ListOfExpressionsExpression cle = new ListOfExpressionsExpression();
            boolean isDeclaration = (bin instanceof DeclarationExpression);
            List<Expression> leftExpressions = ((TupleExpression) leftExpression).getExpressions();
            List<Expression> rightExpressions = ((ListExpression) rightExpression).getExpressions();
            Iterator<Expression> leftIt = leftExpressions.iterator();
            Iterator<Expression> rightIt = rightExpressions.iterator();
            if (isDeclaration) {
                while (leftIt.hasNext()) {
                    Expression left = leftIt.next();
                    if (rightIt.hasNext()) {
                        Expression right = rightIt.next();
                        BinaryExpression bexp = new DeclarationExpression(left, operation, right);
                        bexp.setSourcePosition(right);
                        cle.addExpression(bexp);
                    }
                }
            } else {
                // (next, result) = [ result, next+result ]
                // -->
                // def tmp1 = result
                // def tmp2 = next+result
                // next = tmp1
                // result = tmp2
                int size = rightExpressions.size();
                List<Expression> tmpAssignments = new ArrayList<>(size);
                List<Expression> finalAssignments = new ArrayList<>(size);
                for (int i = 0, n = Math.min(size, leftExpressions.size()); i < n; i += 1) {
                    Expression left = leftIt.next();
                    Expression right = rightIt.next();
                    VariableExpression tmpVar = varX("$tmpVar$" + tmpVarCounter++);
                    BinaryExpression bexp = new DeclarationExpression(tmpVar, operation, right);
                    bexp.setSourcePosition(right);
                    tmpAssignments.add(bexp);
                    bexp = binX(left, operation, varX(tmpVar));
                    bexp.setSourcePosition(left);
                    finalAssignments.add(bexp);
                }
                for (Expression tmpAssignment : tmpAssignments) {
                    cle.addExpression(tmpAssignment);
                }
                for (Expression finalAssignment : finalAssignments) {
                    cle.addExpression(finalAssignment);
                }
            }
            return staticCompilationTransformer.transform(cle);
        }
        return staticCompilationTransformer.superTransform(bin);
    }

    private ClassNode findType(final Expression expression) {
        ClassNode classNode = staticCompilationTransformer.getClassNode();
        return staticCompilationTransformer.getTypeChooser().resolveType(expression, classNode);
    }

    private static BinaryExpression tryOptimizeCharComparison(final Expression left, final Expression right, final BinaryExpression bin) {
        int op = bin.getOperation().getType();
        if (StaticTypeCheckingSupport.isCompareToBoolean(op) || op == Types.COMPARE_EQUAL || op == Types.COMPARE_NOT_EQUAL) {
            Character cLeft = tryCharConstant(left);
            Character cRight = tryCharConstant(right);
            if (cLeft != null || cRight != null) {
                Expression oLeft = (cLeft == null ? left : constX(cLeft, true));
                if (oLeft instanceof PropertyExpression && !hasCharType((PropertyExpression)oLeft)) return null;
                oLeft.setSourcePosition(left);
                Expression oRight = (cRight == null ? right : constX(cRight, true));
                if (oRight instanceof PropertyExpression && !hasCharType((PropertyExpression)oRight)) return null;
                oRight.setSourcePosition(right);
                bin.setLeftExpression(oLeft);
                bin.setRightExpression(oRight);
                return bin;
            }
        }
        return null;
    }

    private static boolean hasCharType(PropertyExpression pe) {
        ClassNode inferredType = pe.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
        return inferredType != null && ClassHelper.Character_TYPE.equals(ClassHelper.getWrapper(inferredType));
    }

    private static Character tryCharConstant(final Expression expr) {
        if (expr instanceof ConstantExpression && ClassHelper.STRING_TYPE.equals(expr.getType())) {
            String value = (String) ((ConstantExpression) expr).getValue();
            if (value != null && value.length() == 1) {
                return value.charAt(0);
            }
        }
        return null;
    }

    private static Expression transformDeclarationExpression(final BinaryExpression bin) {
        Expression leftExpression = bin.getLeftExpression();
        if (leftExpression instanceof VariableExpression) {
            if (ClassHelper.char_TYPE.equals(((VariableExpression) leftExpression).getOriginType())) {
                Expression rightExpression = bin.getRightExpression();
                Character c = tryCharConstant(rightExpression);
                if (c != null) {
                    Expression ce = constX(c, true);
                    ce.setSourcePosition(rightExpression);
                    bin.setRightExpression(ce);
                    return bin;
                }
            }
        }
        return null;
    }

    private static Expression convertInOperatorToTernary(final BinaryExpression bin, Expression rightExpression, final Expression leftExpression) {
        MethodCallExpression call = callX(rightExpression, "isCase", leftExpression);
        call.setMethodTarget(bin.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET));
        call.setSourcePosition(bin);
        call.copyNodeMetaData(bin);
        // GRECLIPSE add -- GROOVY-7473
        call.setImplicitThis(false);
        if (rightExpression instanceof ListExpression
                || rightExpression instanceof MapExpression
                || rightExpression instanceof RangeExpression
                || rightExpression instanceof ClassExpression
                || rightExpression instanceof ConstantExpression
                            && !isNullConstant(rightExpression))
            return call;
        call.setObjectExpression(rightExpression = transformRepeatedReference(rightExpression));
        // GRECLIPSE end
        Expression tExp = ternaryX(
                boolX(binX(rightExpression, Token.newSymbol("==", -1, -1), nullX())),
                binX(leftExpression, Token.newSymbol("==", -1, -1), nullX()),
                call
        );
        // GRECLIPSE add
        tExp.putNodeMetaData("classgen.callback", classgenCallback(call.getObjectExpression()));
        // GRECLIPSE end
        return tExp;
    }

    // GRECLIPSE add
    private static Expression transformRepeatedReference(final Expression exp) {
        if (exp instanceof ConstantExpression || exp instanceof VariableExpression
                && ((VariableExpression) exp).getAccessedVariable() instanceof Parameter) {
            return exp;
        }
        return new TemporaryVariableExpression(exp);
    }

    private static Consumer<WriterController> classgenCallback(final Expression source) {
        return (source instanceof TemporaryVariableExpression ? ((TemporaryVariableExpression) source)::remove : wc -> {});
    }
    // GRECLIPSE end

    private static DeclarationExpression optimizeConstantInitialization(final BinaryExpression originalDeclaration, final Token operation, final ConstantExpression constant, final Expression leftExpression, final ClassNode declarationType) {
        Expression cexp = constX(convertConstant((Number) constant.getValue(), ClassHelper.getWrapper(declarationType)), true);
        cexp.setType(declarationType);
        cexp.setSourcePosition(constant);
        DeclarationExpression result = new DeclarationExpression(
                leftExpression,
                operation,
                cexp
        );
        result.setSourcePosition(originalDeclaration);
        result.copyNodeMetaData(originalDeclaration);
        return result;
    }

    private static Object convertConstant(final Number source, final ClassNode target) {
        if (ClassHelper.Byte_TYPE.equals(target)) {
            return source.byteValue();
        }
        if (ClassHelper.Short_TYPE.equals(target)) {
            return source.shortValue();
        }
        if (ClassHelper.Integer_TYPE.equals(target)) {
            return source.intValue();
        }
        if (ClassHelper.Long_TYPE.equals(target)) {
            return source.longValue();
        }
        if (ClassHelper.Float_TYPE.equals(target)) {
            return source.floatValue();
        }
        if (ClassHelper.Double_TYPE.equals(target)) {
            return source.doubleValue();
        }
        if (ClassHelper.BigInteger_TYPE.equals(target)) {
            return DefaultGroovyMethods.asType(source, BigInteger.class);
        }
        if (ClassHelper.BigDecimal_TYPE.equals(target)) {
            return DefaultGroovyMethods.asType(source, BigDecimal.class);
        }
        throw new IllegalArgumentException("Unsupported conversion");
    }

    /**
     * Adapter for {@link StaticPropertyAccessHelper#transformToSetterCall}.
     */
    private static Expression transformAssignmentToSetterCall(
            final Expression receiver,
            final MethodNode setterMethod,
            final Expression valueExpression,
            final boolean implicitThis,
            final boolean safeNavigation,
            final Expression nameExpression,
            final Expression binaryExpression) {
        // expression that will transfer assignment and name positions
        Expression pos = new PropertyExpression(null, nameExpression);
        pos.setSourcePosition(binaryExpression);

        return StaticPropertyAccessHelper.transformToSetterCall(
                receiver,
                setterMethod,
                valueExpression,
                implicitThis,
                safeNavigation,
                false, // spreadSafe
                true, // TODO: replace with a proper test whether a return value is required or not
                pos
        );
    }
}
