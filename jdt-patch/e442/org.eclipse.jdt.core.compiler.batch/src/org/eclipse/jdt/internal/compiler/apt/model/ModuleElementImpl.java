/*******************************************************************************
 * Copyright (c) 2018, 2020 IBM Corporation and others.
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
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.PlainPackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ModuleElementImpl extends ElementImpl implements ModuleElement {

	ModuleBinding binding;
	private List<Directive> directives;
	private static List<Directive> EMPTY_DIRECTIVES = Collections.emptyList();

	/**
	 * In general, clients should call
	 * {@link Factory#newElement(org.eclipse.jdt.internal.compiler.lookup.Binding)}
	 * to create new instances.
	 */
	ModuleElementImpl(BaseProcessingEnvImpl env, ModuleBinding binding) {
		super(env, binding);
		this.binding = binding;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.MODULE;
	}

	@Override
	public Set<Modifier> getModifiers() {
		int modifiers = this.binding.modifiers;
		return Factory.getModifiers(modifiers, getKind(), false);
	}

	@Override
	public Name getQualifiedName() {
		return new NameImpl(this.binding.moduleName);
	}

	@Override
	public Name getSimpleName() {
		char[] simpleName = this.binding.moduleName;
		for(int i = simpleName.length-1;i>=0;i--) {
			if(simpleName[i] == '.') {
				simpleName = Arrays.copyOfRange(simpleName, i+1, simpleName.length);
				break;
			}
		}
		return new NameImpl(simpleName);
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		ModuleBinding module = this.binding;
		Set<PlainPackageBinding> unique = new HashSet<>();
		for (PlainPackageBinding p : module.declaredPackages.values()) {
			if (!p.hasCompilationUnit(true))
				continue;
			unique.add(p);
		}
		if (module.isUnnamed()) {
			PlainPackageBinding def = module.environment.defaultPackage;
			// FIXME: Does it have any impact for unnamed modules - default package combo?
			if (def != null && def.hasCompilationUnit(true)) {
				unique.add(def);
			}
		} else {
			for (PlainPackageBinding pBinding : this.binding.getExports()) {
				unique.add(pBinding);
			}
			for (PlainPackageBinding pBinding : this.binding.getOpens()) {
				unique.add(pBinding);
			}
		}
		List<Element> enclosed = new ArrayList<>(unique.size());
		for (PlainPackageBinding p : unique) {
			PackageElement pElement = (PackageElement) this._env.getFactory().newElement(p);
			enclosed.add(pElement);
		}
		return Collections.unmodifiableList(enclosed);
	}

	@Override
	public boolean isOpen() {
		return (this.binding.modifiers & ClassFileConstants.ACC_OPEN) != 0;
	}

	@Override
	public boolean isUnnamed() {
		return this.binding.moduleName.length == 0;
	}

	@Override
	public Element getEnclosingElement() {
		// As of today, modules have no enclosing element
		return null;
	}

	@Override
	public List<? extends Directive> getDirectives() {
		if (isUnnamed()) {
			return EMPTY_DIRECTIVES;
		}
		if (this.directives == null)
			this.directives = new ArrayList<>();

		PlainPackageBinding[] packs = this.binding.getExports();
		for (PlainPackageBinding exp : packs) {
			this.directives.add(new ExportsDirectiveImpl(exp));
		}
		Set<ModuleBinding> transitive = new HashSet<>();
		for (ModuleBinding mBinding : this.binding.getRequiresTransitive()) {
			transitive.add(mBinding);
		}
		ModuleBinding[] required = this.binding.getRequires();
		for (ModuleBinding mBinding : required) {
			if (transitive.contains(mBinding)) {
				this.directives.add(new RequiresDirectiveImpl(mBinding, true));
			} else {
				this.directives.add(new RequiresDirectiveImpl(mBinding, false));
			}
		}

		TypeBinding[] tBindings = this.binding.getUses();
		for (TypeBinding tBinding : tBindings) {
			this.directives.add(new UsesDirectiveImpl(tBinding));
		}
		tBindings = this.binding.getServices();
		for (TypeBinding tBinding : tBindings) {
			this.directives.add(new ProvidesDirectiveImpl(tBinding));
		}
		packs = this.binding.getOpens();
		for (PlainPackageBinding exp : packs) {
			this.directives.add(new OpensDirectiveImpl(exp));
		}
		return this.directives;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> visitor, P param) {
		return visitor.visitModule(this, param);
	}
	@Override
	protected AnnotationBinding[] getAnnotationBindings() {
		return this._binding.getAnnotations();
	}

	abstract class PackageDirectiveImpl {
		final PackageBinding binding1;
		List<ModuleElement> targets;

		PackageDirectiveImpl(PackageBinding pBinding) {
			this.binding1 = pBinding;
		}

		public PackageElement getPackage() {
			return ModuleElementImpl.this._env.getFactory().newPackageElement(this.binding1);
		}

		public List<? extends ModuleElement> getTargetModules(String[] restrictions) {
			if(this.targets != null) {
				return this.targets;
			}
			if (restrictions.length == 0) {
				return (this.targets = null);
			}
			List<ModuleElement> targets1 = new ArrayList<>(restrictions.length);
			for (String string : restrictions) {
				ModuleBinding target = ModuleElementImpl.this.binding.environment.getModule(string.toCharArray());
				if (target != null) {
					ModuleElement element = ((ModuleElement) ModuleElementImpl.this._env.getFactory().newElement(target));
					targets1.add(element);
				}
			}
			return (this.targets = Collections.unmodifiableList(targets1));
		}
	}

	class ExportsDirectiveImpl extends PackageDirectiveImpl implements ModuleElement.ExportsDirective {

		ExportsDirectiveImpl(PackageBinding pBinding) {
			super(pBinding);
		}

		@Override
		public <R, P> R accept(DirectiveVisitor<R, P> visitor, P param) {
			return visitor.visitExports(this, param);
		}

		@Override
		public javax.lang.model.element.ModuleElement.DirectiveKind getKind() {
			return DirectiveKind.EXPORTS;
		}

		@Override
		public PackageElement getPackage() {
			return ModuleElementImpl.this._env.getFactory().newPackageElement(this.binding1);
		}
		@Override
		public List<? extends ModuleElement> getTargetModules() {
			if(this.targets != null) {
				return this.targets;
			}
			return getTargetModules(ModuleElementImpl.this.binding.getExportRestrictions(this.binding1));
		}

	}

	class RequiresDirectiveImpl implements ModuleElement.RequiresDirective {
		ModuleBinding dependency;
		boolean transitive;

		RequiresDirectiveImpl(ModuleBinding dependency, boolean transitive) {
			this.dependency = dependency;
			this.transitive = transitive;
		}

		@Override
		public <R, P> R accept(DirectiveVisitor<R, P> visitor, P param) {
			return visitor.visitRequires(this, param);
		}

		@Override
		public javax.lang.model.element.ModuleElement.DirectiveKind getKind() {
			return DirectiveKind.REQUIRES;
		}

		@Override
		public ModuleElement getDependency() {
			return (ModuleElement) ModuleElementImpl.this._env.getFactory().newElement(this.dependency, ElementKind.MODULE);
		}

		@Override
		public boolean isStatic() {
			// TODO: Yet to see this in ModuleBinding. Check again.
			return false;
		}

		@Override
		public boolean isTransitive() {
			return this.transitive;
		}
	}

	class OpensDirectiveImpl extends PackageDirectiveImpl implements ModuleElement.OpensDirective {

		OpensDirectiveImpl(PackageBinding pBinding) {
			super(pBinding);
		}

		@Override
		public <R, P> R accept(DirectiveVisitor<R, P> visitor, P param) {
			return visitor.visitOpens(this, param);
		}

		@Override
		public javax.lang.model.element.ModuleElement.DirectiveKind getKind() {
			return DirectiveKind.OPENS;
		}
		@Override
		public List<? extends ModuleElement> getTargetModules() {
			if(this.targets != null) {
				return this.targets;
			}
			return getTargetModules(ModuleElementImpl.this.binding.getOpenRestrictions(this.binding1));
		}
	}

	class UsesDirectiveImpl implements ModuleElement.UsesDirective {
		final TypeBinding binding1;

		UsesDirectiveImpl(TypeBinding binding) {
			this.binding1 = binding;
		}

		@Override
		public <R, P> R accept(DirectiveVisitor<R, P> visitor, P param) {
			return visitor.visitUses(this, param);
		}

		@Override
		public DirectiveKind getKind() {
			return DirectiveKind.USES;
		}

		@Override
		public TypeElement getService() {
			return (TypeElement) ModuleElementImpl.this._env.getFactory().newElement(this.binding1);
		}

	}

	class ProvidesDirectiveImpl implements ModuleElement.ProvidesDirective {

		TypeBinding service;
		public List<? extends TypeElement> implementations;

		ProvidesDirectiveImpl(TypeBinding service) {
			this.service = service;
		}

		@Override
		public <R, P> R accept(DirectiveVisitor<R, P> visitor, P param) {
			return visitor.visitProvides(this, param);
		}

		@Override
		public DirectiveKind getKind() {
			return DirectiveKind.PROVIDES;
		}

		@Override
		public List<? extends TypeElement> getImplementations() {
			if (this.implementations != null)
				return this.implementations;

			TypeBinding[] implementations2 = ModuleElementImpl.this.binding.getImplementations(this.service);
			if (implementations2.length == 0) {
				return (this.implementations = Collections.emptyList());
			}

			List<TypeElement> list = new ArrayList<>(implementations2.length);
			Factory factory = ModuleElementImpl.this._env.getFactory();
			for (TypeBinding type: implementations2) {
				TypeElement element = (TypeElement) factory.newElement(type);
				list.add(element);
			}
			return Collections.unmodifiableList(list);
		}

		@Override
		public TypeElement getService() {
			return (TypeElement) ModuleElementImpl.this._env.getFactory().newElement(this.service);
		}
	}
}
