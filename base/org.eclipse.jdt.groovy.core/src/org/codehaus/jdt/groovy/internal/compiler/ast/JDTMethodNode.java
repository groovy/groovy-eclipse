/*******************************************************************************
 * Copyright (c) 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.List;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;

/**
 * Wraps a JDT MethodBinding, representing it to groovy as a MethodNode. Translates annotations only when required.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class JDTMethodNode extends MethodNode implements JDTNode {

	private MethodBinding methodBinding;
	private JDTResolver resolver;
	private int bits = 0;

	public JDTMethodNode(MethodBinding methodBinding, JDTResolver resolver, String name, int modifiers, ClassNode returnType,
			Parameter[] gParameters, ClassNode[] thrownExceptions, Statement object) {
		super(name, modifiers, returnType, gParameters, thrownExceptions, object);
		this.resolver = resolver;
		this.methodBinding = methodBinding;
	}

	@Override
	public void addAnnotation(AnnotationNode value) {
		// The grails TestMixinTransformation.autoAnnotateSetupTeardown likes to add annotations to immutable objects... (it gets
		// the setup method from a junit
		// class sometimes)
		System.err.println("Unexpected: Trying to add an annotation " + value.getClassNode().getName()
				+ " to an immutable method node " + toString());
		// throwImmutableException();
	}

	@Override
	public void addAnnotations(List annotations) {
		throwImmutableException();
	}

	@Override
	public List<AnnotationNode> getAnnotations() {
		ensureAnnotationsInitialized();
		return super.getAnnotations();
	}

	@Override
	public List<AnnotationNode> getAnnotations(ClassNode type) {
		ensureAnnotationsInitialized();
		return super.getAnnotations(type);
	}

	private void ensureAnnotationsInitialized() {
		if ((bits & ANNOTATIONS_INITIALIZED) == 0) {
			// If the backing declaring entity for the member is not a SourceTypeBinding then the
			// annotations will have already been discarded/lost
			AnnotationBinding[] annotationBindings = methodBinding.getAnnotations();
			for (AnnotationBinding annotationBinding : annotationBindings) {
				super.addAnnotation(new JDTAnnotationNode(annotationBinding, this.resolver));
			}
			bits |= ANNOTATIONS_INITIALIZED;
		}
	}

	private void throwImmutableException() {
		throw new IllegalStateException("JDTMethodNode is immutable");
	}

	public JDTResolver getResolver() {
		return resolver;
	}

	public MethodBinding getMethodBinding() {
		return methodBinding;
	}

	public Binding getJdtBinding() {
		return methodBinding;
	}

	public boolean isDeprecated() {
		return methodBinding.isDeprecated();
	}
}
