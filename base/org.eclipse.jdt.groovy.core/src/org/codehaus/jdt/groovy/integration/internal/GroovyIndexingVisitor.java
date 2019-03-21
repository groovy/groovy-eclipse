/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.jdt.groovy.integration.internal;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;

/**
 * Visits a {@link ModuleNode} and passes it to an indexing element requestor,
 * thus adding this class to the Java indexes.
 */
public class GroovyIndexingVisitor extends DepthFirstVisitor {

    private final ISourceElementRequestor requestor;

    public GroovyIndexingVisitor(ISourceElementRequestor requestor) {
        this.requestor = requestor;
    }

    // NOTE: Expected entry point is visitModule(ModuleNode).

    @Override
    public void visitImport(ImportNode node) {
        if (node.getType() != null) {
            visitTypeReference(node.getType(), false, true);
        }
        String fieldName = node.getFieldName();
        if (fieldName != null) {
            requestor.acceptUnknownReference(fieldName.toCharArray(), node.getFieldNameExpr().getStart());
        }
        super.visitImport(node);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (!node.isSynthetic()) {
            visitTypeReference(node, false, false);
            visitTypeReference(node.getSuperClass(), false, true);
            for (ClassNode face : node.getInterfaces()) {
                visitTypeReference(face, false, true);
            }
        }
        super.visitClass(node);
    }

    @Override
    public void visitField(FieldNode node) {
        if (node.getType() != node.getDeclaringClass() && node.getEnd() > 0) {
            visitTypeReference(node.getType(), false, true);
        }
        super.visitField(node);
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (node != runMethod && node.getEnd() > 0) {
            if (isNotEmpty(node.getGenericsTypes())) {
                visitTypeParameters(node.getGenericsTypes(), null);
            }
            visitTypeReference(node.getReturnType(), false, true);

            if (isNotEmpty(node.getExceptions())) {
                for (ClassNode type : node.getExceptions()) {
                    visitTypeReference(type, false, true);
                }
            }
        }
        super.visitMethod(node);
    }

    @Override
    protected void visitAnnotation(AnnotationNode node) {
        visitTypeReference(node.getClassNode(), true, true);
        super.visitAnnotation(node);
    }

    @Override
    protected void visitParameter(Parameter parameter) {
        visitTypeReference(parameter.getType(), false, true);
        super.visitParameter(parameter);
    }

    // expressions:

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        if (expression.getEnd() > 0) {
            visitTypeReference(expression.getType(), false, true);
        }
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        // NOTE: expression.getType() may refer to ClassNode behind "this" or "super"
        if (expression.getEnd() > 0 && (/*cast:*/expression.getStart() == expression.getType().getStart() ||
                                        /*coerce:*/expression.getEnd() == expression.getType().getEnd())) {
            visitTypeReference(expression.getType(), false, true);
        }
        super.visitCastExpression(expression);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        visitTypeReference(expression.getType(), false, true);
        super.visitClassExpression(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        if (!(expression.isNullExpression() || expression.isTrueExpression() || expression.isFalseExpression() || expression.isEmptyStringExpression())) {
            if (expression instanceof AnnotationConstantExpression) {
                // ex: @interface X { Y default @Y(...) } -- expression is "@Y(...)"
                visitTypeReference(expression.getType(), true, true);
            }

            char[] constName = expression.getValue().toString().toCharArray();
            requestor.acceptFieldReference(constName, expression.getStart());
            // also could be a method reference
            // we don't know how many arguments the method has, so go up to 7
            for (int i = 0; i <= 7; i += 1) {
                requestor.acceptMethodReference(constName, i, expression.getStart());
            }
        }
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
        char[] typeName = expression.getType().getName().toCharArray();
        // we don't know how many arguments the ctor has, so go up to 9
        for (int i = 0; i <= 9; i += 1) {
            requestor.acceptConstructorReference(typeName, i, expression.getStart());
        }

        // handle idiomatic constructor call like "new Foo(bar:..., 'baz':...)" -- index references to "setBar", etc.
        ((TupleExpression) expression.getArguments()).getExpressions()
            .stream().filter(e -> e instanceof NamedArgumentListExpression)
            .flatMap(e -> ((NamedArgumentListExpression) e).getMapEntryExpressions().stream())
            .forEach(kv -> {
                if (kv.getKeyExpression() instanceof ConstantExpression) {
                    visitNameReference(AccessorSupport.SETTER, kv.getKeyExpression().getText(), kv.getKeyExpression().getStart());
                }
            });

        super.visitConstructorCallExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitTypeReference(expression.getLeftExpression().getType(), false, true);
        visitAnnotations(expression.getAnnotations());
        expression.getRightExpression().visit(this);
        //super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        requestor.acceptFieldReference(expression.getFieldName().toCharArray(), expression.getStart());
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression expression) {
        String methodStr = expression.getMethodAsString();
        if (methodStr != null) {
            char[] methodName = methodStr.toCharArray();
            // also could be a field reference
            requestor.acceptFieldReference(methodName, expression.getStart());
            // we don't know how many arguments the method has, so go up to 7
            for (int i = 0; i <= 7; i += 1) {
                requestor.acceptMethodReference(methodName, i, expression.getStart());
            }
        }
        if (expression.isUsingGenerics() && isNotEmpty(expression.getGenericsTypes())) {
            visitTypeParameters(expression.getGenericsTypes(), null);
        }
        super.visitMethodCallExpression(expression);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        if (!(expression instanceof AttributeExpression) &&
                expression.getProperty() instanceof ConstantExpression) {
            int offset = expression.getProperty().getStart();
            String name = expression.getProperty().getText();
            visitNameReference(AccessorSupport.ISSER , name, offset);
            visitNameReference(AccessorSupport.GETTER, name, offset);
            visitNameReference(AccessorSupport.SETTER, name, offset);
        }
        super.visitPropertyExpression(expression);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
        ClassNode ownerType = expression.getOwnerType();
        if (ownerType != ownerType.redirect()) {
            visitTypeReference(ownerType, false, true);
        }
        super.visitStaticMethodCallExpression(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        requestor.acceptUnknownReference(expression.getName().toCharArray(), expression.getStart());
        //super.visitVariableExpression(expression);
    }

    //--------------------------------------------------------------------------

    private void visitNameReference(AccessorSupport kind, String name, int offset) {
        String methodName = kind.createAccessorName(name);
        if (methodName != null) {
            int params = (kind == AccessorSupport.SETTER ? 1 : 0);
            requestor.acceptMethodReference(methodName.toCharArray(), params, offset);
        }
    }

    private void visitTypeReference(ClassNode type, boolean isAnnotation, boolean useQualifiedName) {
        if (isAnnotation) {
            requestor.acceptAnnotationTypeReference(splitName(type, useQualifiedName), type.getStart(), type.getEnd());
        } else {
            requestor.acceptTypeReference(splitName(GroovyUtils.getBaseType(type), useQualifiedName), type.getStart(), type.getEnd());
        }
        visitTypeParameters(type);
    }

    private void visitTypeParameters(ClassNode type) {
        if (type.isUsingGenerics() && isNotEmpty(type.getGenericsTypes())) {
            visitTypeParameters(type.getGenericsTypes(), type.getName());
        }
    }

    private void visitTypeParameters(GenericsType[] generics, String typeName) {
        for (GenericsType generic : generics) {
            if (generic.getType() != null && generic.getType().getName().charAt(0) != '?') {
                visitTypeReference(generic.getType(), generic.getType().isAnnotationDefinition(), true);
            }
            if (generic.getLowerBound() != null) {
                visitTypeReference(generic.getLowerBound(), generic.getLowerBound().isAnnotationDefinition(), true);
            }
            if (generic.getUpperBounds() != null) {
                for (ClassNode bound : generic.getUpperBounds()) {
                    // handle enums where the upper bound is the same as the type
                    if (!bound.getName().equals(typeName)) {
                        visitTypeReference(bound, bound.isAnnotationDefinition(), true);
                    }
                }
            }
        }
    }

    private static char[][] splitName(ClassNode type, boolean useQualifiedName) {
        String name = useQualifiedName ? type.getName() : type.getNameWithoutPackage();
        String[] nameArr = name.split("\\.");
        char[][] nameCharArr = new char[nameArr.length][];
        for (int i = 0; i < nameArr.length; i += 1) {
            nameCharArr[i] = nameArr[i].toCharArray();
        }
        return nameCharArr;
    }
}
