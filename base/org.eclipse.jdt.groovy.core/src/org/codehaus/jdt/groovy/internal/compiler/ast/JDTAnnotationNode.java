/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.Map;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
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

    private static final char[] jlString = "Ljava/lang/String;".toCharArray();
    private static final char[] baseInt = "I".toCharArray();
    private static final char[] baseBoolean = "Z".toCharArray();

    private boolean membersInitialized = false;
    private AnnotationBinding annotationBinding;
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
        return (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationClassRetention) == TagBits.AnnotationClassRetention;
    }

    @Override
    public boolean hasRuntimeRetention() {
        return (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationRuntimeRetention) == TagBits.AnnotationRuntimeRetention;
    }

    @Override
    public boolean hasSourceRetention() {
        return !hasRuntimeRetention() && !hasClassRetention() &&
            (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationSourceRetention) == TagBits.AnnotationSourceRetention;
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

        if (CharOperation.equals(b.signature(), jlString)) {
            return new ConstantExpression(((StringConstant) value).stringValue());
        }

        if (b.isClass()) {
            return new ClassExpression(resolver.convertToClassNode((TypeBinding) value));
        }

        if (b.isAnnotationType()) {
            return new AnnotationConstantExpression(new JDTAnnotationNode((AnnotationBinding) value, resolver));
        }

        if (b.isEnum()) {
            ClassNode enumType = resolver.convertToClassNode(b);
            String fieldName = String.valueOf(((FieldBinding) value).name);
            return new PropertyExpression(new ClassExpression(enumType), fieldName);
        }

        if (b.isBaseType()) {
            char[] sig = b.signature();
            if (CharOperation.equals(sig, baseInt)) {
                return new ConstantExpression(((IntConstant) value).intValue());
            } else if (CharOperation.equals(sig, baseBoolean)) {
                return new ConstantExpression(((BooleanConstant) value).booleanValue());
            } else {
                throw new GroovyEclipseBug("NYI for signature " + String.valueOf(sig));
            }
        }

        throw new GroovyEclipseBug("Problem in JDTAnnotatioNode.createExpressionFor(binding=" + b + " value=" + value + ")");
    }
}
