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
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * An annotation node that is backed by a JDT reference binding, members are created lazily
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class JDTAnnotationNode extends AnnotationNode {

	private static final char[] jlString = "Ljava/lang/String;".toCharArray();
	private static final char[] baseInt = "I".toCharArray();

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
	public ClassNode getClassNode() {
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
		return (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationClassRetention) == TagBits.AnnotationClassRetention;
	}

	@Override
	public boolean hasRuntimeRetention() {
		return (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationRuntimeRetention) == TagBits.AnnotationRuntimeRetention;
	}

	@Override
	public boolean hasSourceRetention() {
		return !hasRuntimeRetention()
				&& !hasClassRetention()
				&& (annotationBinding.getAnnotationType().tagBits & TagBits.AnnotationSourceRetention) == TagBits.AnnotationSourceRetention;
	}

	@Override
	public boolean isBuiltIn() {
		return super.isBuiltIn();
	}

	@Override
	public boolean isTargetAllowed(int target) {
		// FIXASC Auto-generated method stub
		return super.isTargetAllowed(target);
	}

	// @Override
	// public void setAllowedTargets(int bitmap) {
	// throw new ImmutableException();
	// }

	// @Override
	// public void setRuntimeRetention(boolean flag) {
	// throw new ImmutableException();
	// }

	// @Override
	// public void setSourceRetention(boolean flag) {
	// throw new ImmutableException();
	// }

	// @Override
	// public void setClassRetention(boolean flag) {
	// throw new ImmutableException();
	// }

	@Override
	public void setMember(String name, Expression value) {
		throw new ImmutableException();
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

	// FIXASC does not cope with all variants of value types, see AnnotationVisitor.visitExpression() for the code to utilise
	private Expression createExpressionFor(TypeBinding b, Object value) {
		if (b.isArrayType()) {
			ListExpression listExpression = new ListExpression();
			// FIXASC is it a groovy optimization that if the value is expected to be an array you don't have to
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
		} else if (b.isBaseType()) {
			char[] sig = b.signature();
			if (CharOperation.equals(sig, baseInt)) {
				return new ConstantExpression(((IntConstant) value).intValue());
			} else {
				throw new GroovyEclipseBug("NYI for signature " + new String(sig));
			}
		} else if (b.isClass()) {
			ClassExpression classExpression = new ClassExpression(resolver.convertToClassNode((TypeBinding) value));
			return classExpression;
		}
		throw new GroovyEclipseBug("Problem in JDTAnnotatioNode.createExpressionFor(binding=" + b + " value=" + value + ")");
	}

	public JDTResolver getResolver() {
		return resolver;
	}
}
