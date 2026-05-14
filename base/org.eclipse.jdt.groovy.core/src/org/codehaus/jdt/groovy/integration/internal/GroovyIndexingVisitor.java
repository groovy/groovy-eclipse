/*
 * Copyright 2009-2024 the original author or authors.
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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
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
import org.codehaus.groovy.syntax.Types;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

/**
 * Visits a {@link ModuleNode} and passes it to an indexing element requestor,
 * thus adding this class to the Java indexes.
 */
class GroovyIndexingVisitor extends DepthFirstVisitor {

    private boolean newify; // for MCEs
    private MethodNode enclosingMethod; // for CCEs
    private final ISourceElementRequestor requestor;

    GroovyIndexingVisitor(final ISourceElementRequestor requestor) {
        this.requestor = requestor;
    }

    // NOTE: Expected entry point is visitModule(ModuleNode).

    @Override
    public void visitPackage(final PackageNode node) {
        char[][] tokens = CharOperation.splitOn('.', node.getName().toCharArray(), 0, node.getName().length() - 1);
        requestor.acceptPackage(new ImportReference(tokens, new long[tokens.length], false, 0));
        super.visitPackage(node);
    }

    @Override
    public void visitImport(final ImportNode node) {
        if (node.getEnd() > 0) {
            if (node.getType() == null) { String name = node.getPackageName(); // includes trailing '.'
                char[][] tokens = CharOperation.splitOn('.', name.toCharArray(), 0, name.length() - 1);
                requestor.acceptUnknownReference(tokens, node.getNameStart(), node.getNameEnd());
            } else {
                visitTypeReference(node.getType(), false, true);
            }
        }
        super.visitImport(node);
    }

    @Override
    public void visitClass(final ClassNode node) {
        if (!node.isSynthetic()) {
            visitTypeReference(node, false, false);
            visitTypeReference(node.getSuperClass(), false, true);
            for (ClassNode face : node.getInterfaces()) {
                visitTypeReference(face, false, true);
            }
        }
        boolean oldify = newify;
        super.visitClass(node);
        newify = oldify;
    }

    @Override
    public void visitField(final FieldNode node) {
        if (node.getType() != node.getDeclaringClass() && node.getEnd() > 0) {
            visitTypeReference(node.getType(), false, true);
        }
        boolean oldify = newify;
        super.visitField(node);
        newify = oldify;
    }

    @Override
    public void visitMethod(final MethodNode node) {
        MethodNode meth = enclosingMethod;
        enclosingMethod = node;
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
        boolean oldify = newify;
        super.visitMethod(node);
        enclosingMethod = meth;
        newify = oldify;
    }

    @Override
    protected void visitAnnotation(final AnnotationNode node) {
        visitTypeReference(node.getClassNode(), true, true);
        newify = (newify || "Newify".equals(
            node.getClassNode().getNameWithoutPackage()));
        super.visitAnnotation(node);
    }

    @Override
    protected void visitParameter(final Parameter parameter) {
        visitTypeReference(parameter.getType(), false, true);
        super.visitParameter(parameter);
    }

    // expressions:

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        if (expression.getEnd() > 0) {
            visitTypeReference(expression.getType(), false, true);
        }
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        if (expression.getLeftExpression() instanceof VariableExpression &&
                expression.getOperation().isA(Types.ASSIGNMENT_OPERATOR)) {
            String name = expression.getLeftExpression().getText();
            int offset = expression.getLeftExpression().getStart();
            // index "name"
            requestor.acceptFieldReference(name.toCharArray(), offset);
            // index "name(x)" for SyntheticAccessorsRenameParticipant
            requestor.acceptMethodReference(name.toCharArray(), 1, offset);
            // index "setName(x)"
            visitNameReference(AccessorSupport.SETTER, name, offset);

            expression.getRightExpression().visit(this);
        } else {
            super.visitBinaryExpression(expression);
        }
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        // NOTE: expression.getType() may refer to ClassNode behind "this" or "super"
        if (expression.getEnd() > 0 && (/*cast:*/expression.getStart() == expression.getType().getStart() ||
                                        /*coerce:*/expression.getEnd() == expression.getType().getEnd())) {
            visitTypeReference(expression.getType(), false, true);
        }
        super.visitCastExpression(expression);
    }

    @Override
    public void visitClassExpression(final ClassExpression expression) {
        visitTypeReference(expression.getType(), false, true);
        super.visitClassExpression(expression);
    }

    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        if (!(expression.isNullExpression() || expression.isTrueExpression() || expression.isFalseExpression() || expression.isEmptyStringExpression())) {
            if (expression instanceof AnnotationConstantExpression) {
                // ex: @interface X { Y default @Y(...) } -- expression is "@Y(...)"
                visitTypeReference(expression.getType(), true, true);
            }
            if (expression.getEnd() > 0 && ClassHelper.STRING_TYPE.equals(expression.getType()) && !"class".equals(expression.getText())) {
                char[] name = expression.getText().toCharArray();
                if (Character.isJavaIdentifierStart(name[0]) && expression.getLength() == name.length) {
                    int offset = expression.getStart();
                    requestor.acceptFieldReference(name, offset);
                    if (Character.isUpperCase(name[0]))
                        requestor.acceptTypeReference(name, offset);
                    // we don't know how many arguments the method has, so go up to 7
                    for (int i = 0; i <= 7; i += 1) {
                        requestor.acceptMethodReference(name, i, offset);
                    }
                }
            }
        }
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression expression) {
        ClassNode type = expression.getType();
        if (expression.isSpecialCall()) {
            type = enclosingMethod.getDeclaringClass();
            if (expression.isSuperCall()) {
                type = type.getUnresolvedSuperClass(false);
            }
        } else if (expression.isUsingAnonymousInnerClass()) {
            type = expression.getType().getUnresolvedSuperClass(false);
            if (type == ClassHelper.OBJECT_TYPE) {
                type = expression.getType().getUnresolvedInterfaces(false)[0];
            }
        }

        visitTypeReference(type, false, true);
        char[] typeName = type.getName().toCharArray();
        for (int i = 0; i <= 9; i += 1) { // argument count can be dynamic, so go up to 9
            requestor.acceptConstructorReference(typeName, i, expression.getNameStart());
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
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        visitTypeReference(expression.getLeftExpression().getType(), false, true);
        visitAnnotations(expression.getAnnotations());
        expression.getRightExpression().visit(this);
        //super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        requestor.acceptFieldReference(expression.getFieldName().toCharArray(), expression.getStart());
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression expression) {
        String name = expression.getMethodAsString();
        if (name != null) {
            if (!"new".equals(name)) {
                char[] methName = name.toCharArray();
                // could be a field reference followed by a call operator
                requestor.acceptFieldReference(methName, expression.getStart());
                // we don't know how many arguments the method has, so go up to 9
                for (int i = 0; i <= 9; i += 1) {
                    requestor.acceptMethodReference(methName, i, expression.getStart());
                }
                // check for potential @Newify(Type) expression like "Type(...)"
                if (newify && expression.isImplicitThis() && Character.isUpperCase(methName[0])) {
                    // we don't know how many arguments the constructor has, so go up to 9
                    for (int i = 0; i <= 9; i += 1) {
                        requestor.acceptConstructorReference(methName, i, expression.getNameStart());
                    }
                }
            } else {
                assert newify;
                // assume it's a well-formed @Newify expression like "Type.new(...)"
                char[] typeName = expression.getObjectExpression().getText().toCharArray();
                // we don't know how many arguments the constructor has, so go up to 9
                for (int i = 0; i <= 9; i += 1) {
                    requestor.acceptConstructorReference(typeName, i, expression.getNameStart());
                }
            }
        }
        if (expression.isUsingGenerics() && isNotEmpty(expression.getGenericsTypes())) {
            visitTypeParameters(expression.getGenericsTypes(), null);
        }
        super.visitMethodCallExpression(expression);
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        if (!(expression instanceof AttributeExpression)) {
            String name = expression.getPropertyAsString();
            if (name != null) {
                int offset = expression.getProperty().getStart();
                // index "isName()", "getName()" and "setName(x)"
                visitNameReference(AccessorSupport.ISSER,  name, offset);
                visitNameReference(AccessorSupport.GETTER, name, offset);
                visitNameReference(AccessorSupport.SETTER, name, offset);
            }
        }
        super.visitPropertyExpression(expression);
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression expression) {
        ClassNode ownerType = expression.getOwnerType();
        if (ownerType != ownerType.redirect()) {
            visitTypeReference(ownerType, false, true);
        }
        super.visitStaticMethodCallExpression(expression);
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        if (expression.getEnd() > 0 && !expression.isThisExpression() && !expression.isSuperExpression()) {
            String name = expression.getName();
            char[] carray = name.toCharArray();
            int offset = expression.getStart();
            // index "name"
            requestor.acceptFieldReference(carray, offset);
            if (Character.isUpperCase(carray[0]))
                requestor.acceptTypeReference(carray, offset);
            // index "name()" for SyntheticAccessorsRenameParticipant
            requestor.acceptMethodReference(carray, 0, offset);
            // index "getName()" and "isName()"
            visitNameReference(AccessorSupport.GETTER, name, offset);
            visitNameReference(AccessorSupport.ISSER,  name, offset);
        }
    }

    //--------------------------------------------------------------------------

    private void visitNameReference(final AccessorSupport kind, final String name, final int offset) {
        String methodName = kind.createAccessorName(name);
        if (methodName != null) {
            int params = (kind == AccessorSupport.SETTER ? 1 : 0);
            // index "isName()", "getName()" or "setName(x)"
            requestor.acceptMethodReference(methodName.toCharArray(), params, offset);
            // index "isName(x)", "getName(x)" or "setName(x,x)" for groovy method search
            requestor.acceptMethodReference(methodName.toCharArray(), params + 1, offset);
        }
    }

    private void visitTypeReference(final ClassNode type, final boolean isAnnotation, final boolean useQualifiedName) {
        char[][] tokens;
        if (isAnnotation) {
            tokens = splitName(type, useQualifiedName);
            requestor.acceptAnnotationTypeReference(tokens, type.getStart(), type.getEnd());
        } else {
            if (useQualifiedName) visitAnnotations(type.getTypeAnnotations());
            tokens = splitName(GroovyUtils.getBaseType(type), useQualifiedName);
            requestor.acceptTypeReference(tokens, type.getStart(), type.getEnd());
            visitTypeParameters(type);
        }
        for (int i = tokens.length - 1; i > 0;) {
            if (Character.isUpperCase(tokens[--i][0])) {
                requestor.acceptTypeReference(tokens[i], 0);
            } else {
                break;
            }
        }
    }

    private void visitTypeParameters(final ClassNode type) {
        if (type.isUsingGenerics() && isNotEmpty(type.getGenericsTypes())) {
            visitTypeParameters(type.getGenericsTypes(), type.getName());
        }
    }

    private void visitTypeParameters(final GenericsType[] generics, final String typeName) {
        for (GenericsType generic : generics) {
            if (!generic.isPlaceholder() && !generic.isWildcard()) {
                visitTypeReference(generic.getType(), generic.getType().isAnnotationDefinition(), true);
            } else {
                visitAnnotations(generic.getType().getTypeAnnotations());
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

    private static char[][] splitName(final ClassNode type, final boolean useQualifiedName) {
        String name = useQualifiedName ? type.getName() : type.getNameWithoutPackage();
        return CharOperation.splitOn('.', name.toCharArray());
    }
}
