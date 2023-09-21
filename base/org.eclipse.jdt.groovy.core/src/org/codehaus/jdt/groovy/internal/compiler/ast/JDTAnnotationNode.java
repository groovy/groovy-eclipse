/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.codegen.ConstantPool.JavaLangStringSignature;

import java.util.Map;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * An annotation node that is backed by a JDT reference binding, members are created lazily.
 */
public class JDTAnnotationNode extends AnnotationNode {

    private AnnotationBinding annotationBinding;
    private volatile boolean membersInitialized;
    private JDTResolver resolver;

    public JDTAnnotationNode(AnnotationBinding annotationBinding, JDTResolver resolver) {
        super(new JDTClassNode(annotationBinding.getAnnotationType(), resolver));
        this.annotationBinding = annotationBinding;
        this.resolver = resolver;
    }

    @Override
    public void addMember(String name, Expression value) {
        throw new ImmutableException();
    }

    @Override
    public void setMember(String name, Expression value) {
        throw new ImmutableException();
    }

    @Override
    public Expression getMember(String name) {
        ensureMembersInitialized();
        return super.getMember(name);
    }

    @Override
    public Map<String, Expression> getMembers() {
        ensureMembersInitialized();
        return super.getMembers();
    }

    @Override
    public boolean hasClassRetention() {
        return 0 != (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationClassRetention) || !(hasRuntimeRetention() || hasSourceRetention());
    }

    @Override
    public boolean hasRuntimeRetention() {
        return 0 != (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationRuntimeRetention);
    }

    @Override
    public boolean hasSourceRetention() {
        return 0 != (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationSourceRetention);
    }

    private void ensureMembersInitialized() {
        if (membersInitialized) {
            return;
        }
        membersInitialized = true;
        ElementValuePair[] evpairs = annotationBinding.getElementValuePairs();
        for (ElementValuePair evpair : evpairs) {
            char[] name = evpair.getName();
            MethodBinding mb = evpair.binding;
            Expression valueExpression = null;
            // FIXASC needs more cases considering
            if (mb == null) {
                if (evpair.value instanceof StringConstant) {
                    String v = ((StringConstant) evpair.value).stringValue();
                    valueExpression = new ConstantExpression(v);
                } else {
                    // GRECLIPSE-1587 fill in something here to avoid an NPE
                    valueExpression = ConstantExpression.NULL;
                }
            } else {
                valueExpression = createExpressionFor(mb.returnType, evpair.value);
            }
            super.addMember(new String(name), valueExpression);
        }
    }

    private Expression createExpressionFor(TypeBinding b, Object value) {
        if (b.isArrayType()) {
            ListExpression listExpression = new ListExpression();
            if (value.getClass().isArray()) {
                for (Object v : (Object[]) value) {
                    if (v != null) // TODO: Why did null values start appearing in Java 9?
                        listExpression.addExpression(createExpressionFor(b.leafComponentType(), v));
                }
            } else {
                listExpression.addExpression(createExpressionFor(b.leafComponentType(), value));
            }
            return listExpression;
        }

        char[] sig = b.signature();

        if (CharOperation.equals(sig, JavaLangStringSignature)) {
            return new ConstantExpression(((Constant) value).stringValue());
        }

        if (b.isClass()) {
            return new ClassExpression(resolver.convertToClassNode((TypeBinding) value));
        }

        if (b.isAnnotationType()) {
            return new AnnotationConstantExpression(resolver.convertToAnnotationNode((AnnotationBinding) value));
        }

        if (b.isEnum()) {
            ClassNode enumType = resolver.convertToClassNode(b);
            String fieldName = String.valueOf(((FieldBinding) value).name);
            return new PropertyExpression(new ClassExpression(enumType), fieldName);
        }

        if (b.isPrimitiveType()) {
            assert sig.length == 1;
            switch (sig[0]) {
            case Signature.C_BOOLEAN:
                return new ConstantExpression(((Constant) value).booleanValue(), true);
            case Signature.C_BYTE:
                return new ConstantExpression(((Constant) value).byteValue(), true);
            case Signature.C_CHAR:
                return new ConstantExpression(((Constant) value).charValue(), true);
            case Signature.C_DOUBLE:
                return new ConstantExpression(((Constant) value).doubleValue(), true);
            case Signature.C_FLOAT:
                return new ConstantExpression(((Constant) value).floatValue(), true);
            case Signature.C_INT:
                return new ConstantExpression(((Constant) value).intValue(), true);
            case Signature.C_LONG:
                return new ConstantExpression(((Constant) value).longValue(), true);
            case Signature.C_SHORT:
                return new ConstantExpression(((Constant) value).shortValue(), true);
            }
        }

        throw new GroovyEclipseBug("Problem in JDTAnnotatioNode.createExpressionFor(binding=" + b + " value=" + value + ")");
    }
}
