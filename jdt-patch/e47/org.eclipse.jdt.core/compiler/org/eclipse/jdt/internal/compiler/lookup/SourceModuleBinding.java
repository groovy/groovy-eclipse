/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class SourceModuleBinding extends ModuleBinding {

	final public CompilationUnitScope scope; // TODO(SHMOD): consider cleanup at end of compile
	private SimpleLookupTable storedAnnotations = null;

	/**
	 * Construct a named module from source.
	 * <p><strong>Side effects:</strong> adds the new module to root.knownModules,
	 * creates a new LookupEnvironment and links that into the scope.</p>
	 */
	public SourceModuleBinding(char[] moduleName, CompilationUnitScope scope, LookupEnvironment rootEnv) {
		super(moduleName);
		rootEnv.knownModules.put(moduleName, this);
		this.environment = new LookupEnvironment(rootEnv, this);
		this.scope = scope;
		scope.environment = this.environment;
	}

	public void setRequires(ModuleBinding[] requires, ModuleBinding[] requiresTransitive) {
		// TODO(SHMOD): it's a bit awkward that we may get called after applyModuleUpdates() has already worked.
		ModuleBinding javaBase = this.environment.javaBaseModule();
		if (javaBase.isUnnamed()) // happens when no java.base can be found in the name environment.
			javaBase = null;
		this.requires = merge(this.requires, requires, javaBase, ModuleBinding[]::new);
		this.requiresTransitive = merge(this.requiresTransitive, requiresTransitive, null, ModuleBinding[]::new);
	}
	
	public void setUses(TypeBinding[] uses) {
		this.uses = merge(this.uses, uses, null, TypeBinding[]::new);
	}

	public void setServices(TypeBinding[] services) {
		this.services = merge(this.services, services, null, TypeBinding[]::new);
	}

	public void setImplementations(TypeBinding infBinding, Collection<TypeBinding> resolvedImplementations) {
		if (this.implementations == null)
			this.implementations = new HashMap<>();
		this.implementations.put(infBinding, resolvedImplementations.toArray(new TypeBinding[resolvedImplementations.size()]));
	}

	private <T> T[] merge(T[] one, T[] two, T extra, IntFunction<T[]> supplier) {
		if (one.length == 0 && extra == null) {
			if (two.length > 0)
				return two;
			return one;
		}
		int len0 = extra == null ? 0 : 1;
		int len1 = one.length;
		int len2 = two.length;
		T[] result = supplier.apply(len0+len1+len2);
		if (extra != null)
			result[0] = extra;
		System.arraycopy(one, 0, result, len0, len1);
		System.arraycopy(two, 0, result, len0+len1, len2);
		return result;
	}
	
	@Override
	Stream<ModuleBinding> getRequiredModules(boolean transitiveOnly) {
		if (this.requires == NO_MODULES) {
			this.scope.referenceContext.moduleDeclaration.resolveModuleDirectives(this.scope);
		}
		return super.getRequiredModules(transitiveOnly);
	}

	@Override
	public ModuleBinding[] getAllRequiredModules() {
		if (this.scope != null)
			this.scope.referenceContext.moduleDeclaration.resolveModuleDirectives(this.scope);
		return super.getAllRequiredModules();
	}
	public long getAnnotationTagBits() {
		//TODO: This code is untested as we don't yet get a scope in ModuleBinding
		if ((this.tagBits & TagBits.AnnotationResolved) == 0 && this.scope != null) {
			ModuleDeclaration module = this.scope.referenceContext.moduleDeclaration;
			ASTNode.resolveAnnotations(module.scope, module.annotations, this);
			if ((this.tagBits & TagBits.AnnotationDeprecated) != 0) {
				this.modifiers |= ClassFileConstants.AccDeprecated;
				this.tagBits |= TagBits.DeprecatedAnnotationResolved;
			}
			this.tagBits |= TagBits.AnnotationResolved;
		}
		return this.tagBits;
	}
	public AnnotationBinding[] getAnnotations() {
		return retrieveAnnotations(this);
	}
	public AnnotationHolder retrieveAnnotationHolder(Binding binding, boolean forceInitialization) {
		SimpleLookupTable store = storedAnnotations(forceInitialization, false);
		return store == null ? null : (AnnotationHolder) store.get(binding);
	}

	AnnotationBinding[] retrieveAnnotations(Binding binding) {
		AnnotationHolder holder = retrieveAnnotationHolder(binding, true);
		return holder == null ? Binding.NO_ANNOTATIONS : holder.getAnnotations();
	}

	public void setAnnotations(AnnotationBinding[] annotations, boolean forceStore) {
		storeAnnotations(this, annotations, forceStore);
	}
	void storeAnnotationHolder(Binding binding, AnnotationHolder holder) {
		if (holder == null) {
			SimpleLookupTable store = storedAnnotations(false, false);
			if (store != null)
				store.removeKey(binding);
		} else {
			SimpleLookupTable store = storedAnnotations(true, false);
			if (store != null)
				store.put(binding, holder);
		}
	}

	void storeAnnotations(Binding binding, AnnotationBinding[] annotations, boolean forceStore) {
		AnnotationHolder holder = null;
		if (annotations == null || annotations.length == 0) {
			SimpleLookupTable store = storedAnnotations(false, forceStore);
			if (store != null)
				holder = (AnnotationHolder) store.get(binding);
			if (holder == null) return; // nothing to delete
		} else {
			SimpleLookupTable store = storedAnnotations(true, forceStore);
			if (store == null) return; // not supported
			holder = (AnnotationHolder) store.get(binding);
			if (holder == null)
				holder = new AnnotationHolder();
		}
		storeAnnotationHolder(binding, holder.setAnnotations(annotations));
	}

	SimpleLookupTable storedAnnotations(boolean forceInitialize, boolean forceStore) {
		if (forceInitialize && this.storedAnnotations == null && this.scope != null) { // scope null when no annotation cached, and type got processed fully (159631)
			this.scope.referenceCompilationUnit().compilationResult.hasAnnotations = true;
			final CompilerOptions globalOptions = this.scope.environment().globalOptions;
			if (!globalOptions.storeAnnotations && !forceStore)
				return null; // not supported during this compile
			this.storedAnnotations = new SimpleLookupTable(3);
		}
		return this.storedAnnotations;
	}

}
