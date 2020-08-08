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
package org.codehaus.groovy.transform.trait;

import groovy.lang.MetaProperty;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.tools.ClosureUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

import java.util.List;
import java.util.function.Function;

import static org.codehaus.groovy.ast.tools.GeneralUtils.thisPropX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static groovyjarjarasm.asm.Opcodes.ACC_ABSTRACT;
import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC;
import static groovyjarjarasm.asm.Opcodes.ACC_STATIC;
import static groovyjarjarasm.asm.Opcodes.ACC_SYNTHETIC;

/**
 * This transformer is used to transform calls to <code>SomeTrait.super.foo()</code> into the appropriate trait call.
 *
 * @since 2.3.0
 */
class SuperCallTraitTransformer extends ClassCodeExpressionTransformer {
    static final String UNRESOLVED_HELPER_CLASS = "UNRESOLVED_HELPER_CLASS";
    private final SourceUnit unit;

    SuperCallTraitTransformer(final SourceUnit unit) {
        this.unit = unit;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    @Override
    public Expression transform(final Expression exp) {
        if (exp instanceof BinaryExpression) {
            return transformBinaryExpression((BinaryExpression) exp);
        }
        // GRECLIPSE add -- GROOVY-9256
        if (exp instanceof ClosureExpression) {
            return transformClosureExpression((ClosureExpression) exp);
        }
        // GRECLIPSE add -- GROOVY-9255
        if (exp instanceof PropertyExpression) {
            return transformPropertyExpression((PropertyExpression) exp);
        }
        // GRECLIPSE end
        if (exp instanceof MethodCallExpression) {
            return transformMethodCallExpression((MethodCallExpression) exp);
        }
        return super.transform(exp);
    }

    private Expression transformBinaryExpression(final BinaryExpression exp) {
        // GRECLIPSE add -- GROOVY-9255
        if (exp.getOperation().isA(Types.ASSIGNMENT_OPERATOR))
            exp.getLeftExpression().putNodeMetaData("assign.target", exp.getOperation());
        // GRECLIPSE end
        Expression trn = super.transform(exp);
        if (trn instanceof BinaryExpression) {
            BinaryExpression bin = (BinaryExpression) trn;
            Expression leftExpression = bin.getLeftExpression();
            if (bin.getOperation().getType() == Types.EQUAL && leftExpression instanceof PropertyExpression) {
                ClassNode traitReceiver = null;
                PropertyExpression leftPropertyExpression = (PropertyExpression) leftExpression;
                if (isTraitSuperPropertyExpression(leftPropertyExpression.getObjectExpression())) {
                    PropertyExpression pexp = (PropertyExpression) leftPropertyExpression.getObjectExpression();
                    traitReceiver = pexp.getObjectExpression().getType();
                }
                if (traitReceiver!=null) {
                    // A.super.foo = ...
                    /* GRECLIPSE edit
                    TraitHelpersTuple helpers = Traits.findHelpers(traitReceiver);
                    ClassNode helper = helpers.getHelper();
                    */
                    ClassNode helper = getHelper(traitReceiver);
                    // GRECLIPSE end
                    String setterName = MetaProperty.getSetterName(leftPropertyExpression.getPropertyAsString());
                    List<MethodNode> methods = helper.getMethods(setterName);
                    for (MethodNode method : methods) {
                        Parameter[] parameters = method.getParameters();
                        // GRECLIPSE edit -- GROOVY-9672
                        //if (parameters.length==2 && parameters[0].getType().equals(traitReceiver)) {
                        if (parameters.length == 2 && isSelfType(parameters[0], traitReceiver)) {
                        // GRECLIPSE end
                            ArgumentListExpression args = new ArgumentListExpression(
                                    /* GRECLIPSE edit -- GROOVY-9672
                                    new VariableExpression("this"),
                                    transform(exp.getRightExpression())
                                    */
                                    parameters[0].getType().equals(ClassHelper.CLASS_Type)
                                        ? thisPropX(false, "class") : varX("this"),
                                    bin.getRightExpression()
                                    // GRECLIPSE end
                            );
                            MethodCallExpression setterCall = new MethodCallExpression(
                                    new ClassExpression(helper),
                                    setterName,
                                    args
                            );
                            // GRECLIPSE add
                            setterCall.getMethod().setSourcePosition(leftPropertyExpression.getProperty());
                            setterCall.getObjectExpression().setSourcePosition(traitReceiver);
                            // GRECLIPSE end
                            /* GRECLIPSE edit -- GROOVY-9673
                            setterCall.setMethodTarget(method);
                            */
                            setterCall.setImplicitThis(false);
                            return setterCall;
                        }
                    }
                    return bin;
                }
            }
        }
        return trn;
    }

    private Expression transformClosureExpression(final ClosureExpression exp) {
        for (Parameter prm : ClosureUtils.getParametersSafe(exp)) {
            Expression ini = transform(prm.getInitialExpression());
            prm.setInitialExpression(ini);
        }
        visitClassCodeContainer(exp.getCode());
        return super.transform(exp);
    }

    private Expression transformPropertyExpression(final PropertyExpression exp) {
        if (exp.getNodeMetaData("assign.target") == null && isTraitSuperPropertyExpression(exp.getObjectExpression())) {
            Expression classExpression = ((PropertyExpression) exp.getObjectExpression()).getObjectExpression();
            ClassNode traitType = classExpression.getType();
            if (traitType != null) {
                ClassNode helperType = getHelper(traitType);
                // TraitType.super.foo -> TraitType$Trait$Helper.getFoo(this)

                Function<MethodNode, MethodCallExpression> makeCall = (methodNode) -> {
                    MethodCallExpression methodCall = new MethodCallExpression(
                            new ClassExpression(helperType),
                            methodNode.getName(),
                            new ArgumentListExpression(
                                    methodNode.getParameters()[0].getType().equals(ClassHelper.CLASS_Type)
                                        ? thisPropX(false, "class") : varX("this")
                            )
                    );
                    methodCall.getObjectExpression().setSourcePosition(classExpression);
                    methodCall.getMethod().setSourcePosition(exp.getProperty());
                    methodCall.setMethodTarget(methodNode);
                    methodCall.setImplicitThis(false);
                    return methodCall;
                };

                String getterName = MetaProperty.getGetterName(exp.getPropertyAsString(), null);
                for (MethodNode method : helperType.getMethods(getterName)) {
                    if (method.isStatic() && method.getParameters().length == 1
                            && isSelfType(method.getParameters()[0], traitType)
                            && !method.getReturnType().equals(ClassHelper.VOID_TYPE)) {
                        return makeCall.apply(method);
                    }
                }

                String isserName = "is" + getterName.substring(3);
                for (MethodNode method : helperType.getMethods(isserName)) {
                    if (method.isStatic() && method.getParameters().length == 1
                            && isSelfType(method.getParameters()[0], traitType)
                            && method.getReturnType().equals(ClassHelper.boolean_TYPE)) {
                        return makeCall.apply(method);
                    }
                }
            }
        }
        exp.removeNodeMetaData("assign.target");
        return super.transform(exp);
    }

    private Expression transformMethodCallExpression(final MethodCallExpression exp) {
        if (isTraitSuperPropertyExpression(exp.getObjectExpression())) {
            Expression objectExpression = exp.getObjectExpression();
            ClassNode traitReceiver = ((PropertyExpression) objectExpression).getObjectExpression().getType();

            if (traitReceiver != null) {
                // (SomeTrait.super).foo() --> SomeTrait$Helper.foo(this)
                ClassExpression receiver = new ClassExpression(
                        getHelper(traitReceiver)
                );
                // GRECLIPSE add
                receiver.setSourcePosition(traitReceiver);
                // GRECLIPSE end
                /* GRECLIPSE edit -- GROOVY-9672
                ArgumentListExpression newArgs = new ArgumentListExpression();
                newArgs.addExpression(new VariableExpression("this"));
                */
                List<MethodNode> targets = receiver.getType().getMethods(exp.getMethodAsString());
                boolean isStatic = !targets.isEmpty() && targets.stream().map(MethodNode::getParameters)
                    .allMatch(params -> params.length > 0 && params[0].getType().equals(ClassHelper.CLASS_Type));
                ArgumentListExpression newArgs = new ArgumentListExpression(
                        isStatic ? thisPropX(false, "class") : varX("this"));
                // GRECLIPSE end
                Expression arguments = exp.getArguments();
                if (arguments instanceof TupleExpression) {
                    List<Expression> expressions = ((TupleExpression) arguments).getExpressions();
                    for (Expression expression : expressions) {
                        newArgs.addExpression(transform(expression));
                    }
                } else {
                    newArgs.addExpression(transform(arguments));
                }
                MethodCallExpression result = new MethodCallExpression(
                        receiver,
                        transform(exp.getMethod()),
                        newArgs
                );
                result.setImplicitThis(false);
                result.setSpreadSafe(exp.isSpreadSafe());
                result.setSafe(exp.isSafe());
                /* GRECLIPSE edit
                result.setSourcePosition(exp);
                */
                return result;
            }
        }
        return super.transform(exp);
    }

    private ClassNode getHelper(final ClassNode traitReceiver) {
        if (helperClassNotCreatedYet(traitReceiver)) {
            // GROOVY-7909 A Helper class in same compilation unit may have not been created when referenced
            // Here create a symbol as a "placeholder" and it will be resolved later.
            ClassNode ret = new InnerClassNode(
                    traitReceiver,
                    Traits.helperClassName(traitReceiver),
                    ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_SYNTHETIC,
                    ClassHelper.OBJECT_TYPE,
                    ClassNode.EMPTY_ARRAY,
                    null
            ).getPlainNodeReference();

            ret.setRedirect(null);
            traitReceiver.redirect().setNodeMetaData(UNRESOLVED_HELPER_CLASS, ret);
            return ret;
        } else {
            /* GRECLIPSE edit
            TraitHelpersTuple helpers = Traits.findHelpers(traitReceiver);
            return helpers.getHelper();
            */
            return Traits.findHelper(traitReceiver).getPlainNodeReference();
            // GRECLIPSE end
        }
    }

    private boolean helperClassNotCreatedYet(final ClassNode traitReceiver) {
        return !traitReceiver.redirect().getInnerClasses().hasNext()
                && this.unit.getAST().getClasses().contains(traitReceiver.redirect());
    }

    private boolean isTraitSuperPropertyExpression(final Expression exp) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pexp = (PropertyExpression) exp;
            Expression objectExpression = pexp.getObjectExpression();
            if (objectExpression instanceof ClassExpression) {
                ClassNode type = objectExpression.getType();
                if (Traits.isTrait(type) && "super".equals(pexp.getPropertyAsString())) {
                    return true;
                }
            }
        }
        return false;
    }

    // GRECLIPSE add
    private static boolean isSelfType(final Parameter parameter, final ClassNode traitType) {
        ClassNode paramType = parameter.getType();
        if (paramType.equals(traitType)) return true;
        return paramType.equals(ClassHelper.CLASS_Type)
                && paramType.getGenericsTypes() != null
                && paramType.getGenericsTypes()[0].getType().equals(traitType);
    }
    // GRECLIPSE end
}
