/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.Map;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

// FIXASC (M2) immutable from outside
/**
 * An annotation node that is backed by a JDT reference binding, members are created lazily
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class JDTAnnotationNode extends AnnotationNode {

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
		// FIXASC (M2) Auto-generated method stub
		super.addMember(name, value);
	}

	@Override
	public ClassNode getClassNode() {
		// FIXASC (M2) Auto-generated method stub
		return super.getClassNode();
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
		return super.hasClassRetention();
		// return (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationClassRetention) ==
		// TagBits.AnnotationClassRetention;
	}

	@Override
	public boolean hasRuntimeRetention() {
		// FIXASC (M2) check it is resolved?
		// return (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationRuntimeRetention) ==
		// TagBits.AnnotationRuntimeRetention;
		return super.hasRuntimeRetention();
	}

	@Override
	public boolean hasSourceRetention() {
		// return (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationSourceRetention) ==
		// TagBits.AnnotationSourceRetention;
		return super.hasSourceRetention();
	}

	@Override
	public boolean isBuiltIn() {
		// FIXASC (M2) Auto-generated method stub
		return super.isBuiltIn();
	}

	@Override
	public boolean isTargetAllowed(int target) {
		// FIXASC (M2) Auto-generated method stub
		return super.isTargetAllowed(target);
	}

	@Override
	public void setAllowedTargets(int bitmap) {
		// FIXASC (M2) Auto-generated method stub
		super.setAllowedTargets(bitmap);
	}

	@Override
	public void setClassRetention(boolean flag) {
		// FIXASC (M2) Auto-generated method stub
		super.setClassRetention(flag);
	}

	@Override
	public void setMember(String name, Expression value) {
		// FIXASC (M2) Auto-generated method stub
		super.setMember(name, value);
	}

	@Override
	public void setRuntimeRetention(boolean flag) {
		// FIXASC (M2) Auto-generated method stub
		super.setRuntimeRetention(flag);
	}

	@Override
	public void setSourceRetention(boolean flag) {
		// FIXASC (M2) Auto-generated method stub
		super.setSourceRetention(flag);
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
			Expression valueExpression = createExpressionFor(mb.returnType, evpair.value);
			super.addMember(new String(name), valueExpression);
		}
	}

	private static final char[] jlString = "Ljava/lang/String;".toCharArray();

	// FIXASC (M2) does not cope with all variants of value types, see AnnotationVisitor.visitExpression() for the code to utilise
	private Expression createExpressionFor(TypeBinding b, Object value) {
		if (b.isArrayType()) {
			ListExpression listExpression = new ListExpression();
			// FIXASC (M2) is it a groovy optimization that if the value is expected to be an array you don't have to
			// write it as such
			if (value.getClass().isArray()) {
				Object[] values = (Object[]) value;
				for (Object v : values) {
					listExpression.addExpression(createExpressionFor(((ArrayBinding) b).leafComponentType, v));
				}
			} else {
				listExpression.addExpression(createExpressionFor(((ArrayBinding) b).leafComponentType, value));
			}
			return listExpression;
		} else if (b.isEnum()) {
			ClassExpression classExpression = new ClassExpression(resolver.convertToClassNode(b));
			Expression valueExpression = new PropertyExpression(classExpression, new String(((FieldBinding) value).name));
			return valueExpression;
		} else if (CharOperation.equals(b.signature(), jlString)) {
			String v = ((StringConstant) value).stringValue();
			return new ConstantExpression(v);
		} else {

		}
		throw new GroovyEclipseBug("Problem in JDTAnnotatioNode.createExpressionFor(binding=" + b + " value=" + value + ")");
	}
}
