/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Element represents any defined Java language element - a package,
 * a method, a class or interface.  Contrast with DeclaredType.
 */
public abstract class ElementImpl
	implements javax.lang.model.element.Element, IElementInfo
{
	public final BaseProcessingEnvImpl _env;
	public final Binding _binding;

	protected ElementImpl(BaseProcessingEnvImpl env, Binding binding) {
		this._env = env;
		this._binding = binding;
	}

	@Override
	public TypeMirror asType() {
		return this._env.getFactory().newTypeMirror(this._binding);
	}

	/**
	 * @return the set of compiler annotation bindings on this element
	 */
	protected abstract AnnotationBinding[] getAnnotationBindings();

	/* Package any repeating annotations into containers, return others as is.
	   In the compiler bindings repeating annotations are left in as is, hence
	   this step. The return value would match what one would expect to see in
	   a class file.
	*/
	public final AnnotationBinding [] getPackedAnnotationBindings() {
		return Factory.getPackedAnnotationBindings(getAnnotationBindings());
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		A annotation = this._env.getFactory().getAnnotation(getPackedAnnotationBindings(), annotationClass);
		if (annotation != null || this.getKind() != ElementKind.CLASS || annotationClass.getAnnotation(Inherited.class) == null)
			return annotation;

		ElementImpl superClass = (ElementImpl) this._env.getFactory().newElement(((ReferenceBinding) this._binding).superclass());
		return superClass == null ? null : superClass.getAnnotation(annotationClass);
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return this._env.getFactory().getAnnotationMirrors(getPackedAnnotationBindings());
	}

	@Override
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		A [] annotations = this._env.getFactory().getAnnotationsByType(Factory.getUnpackedAnnotationBindings(getPackedAnnotationBindings()), annotationType);
		if (annotations.length != 0 || this.getKind() != ElementKind.CLASS || annotationType.getAnnotation(Inherited.class) == null)
			return annotations;

		ElementImpl superClass =  (ElementImpl) this._env.getFactory().newElement(((ReferenceBinding) this._binding).superclass());
		return superClass == null ? annotations : superClass.getAnnotationsByType(annotationType);
	}

	@Override
	public Set<Modifier> getModifiers() {
		// Most subclasses implement this; this default is appropriate for
		// PackageElement and TypeParameterElement.
		return Collections.emptySet();
	}

	@Override
	public Name getSimpleName() {
		return new NameImpl(this._binding.shortReadableName());
	}

	@Override
	public int hashCode() {
		return this._binding.hashCode();
	}

	// TODO: equals() implemented as == of JDT bindings.  Valid within
	// a single Compiler instance; breaks in IDE if processors cache values.
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ElementImpl other = (ElementImpl) obj;
		if (this._binding == null) {
			if (other._binding != null)
				return false;
		} else if (this._binding != other._binding)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this._binding.toString();
	}

	@Override
	public String getFileName() {
		// Subclasses should override and return something of value
		return null;
	}

	/**
	 * @return the package containing this element.  The package of a PackageElement is itself.
	 */
	PackageElement getPackage() {
		return null;
	}

	/**
	 * Subclassed by VariableElementImpl, TypeElementImpl, and ExecutableElementImpl.
	 * This base implementation suffices for other types.
	 * @see javax.lang.model.util.Elements#hides
	 * @return true if this element hides {@code hidden}
	 */
	public boolean hides(Element hidden)
	{
		return false;
	}
}
