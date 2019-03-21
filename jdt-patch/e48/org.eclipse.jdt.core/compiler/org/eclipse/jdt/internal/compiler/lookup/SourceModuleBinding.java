/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class SourceModuleBinding extends ModuleBinding {

	final public CompilationUnitScope scope; // TODO(SHMOD): consider cleanup at end of compile

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

	@Override
	public TypeBinding[] getUses() {
		resolveTypes();
		return super.getUses();
	}
	
	@Override
	public TypeBinding[] getServices() {
		resolveTypes();
		return super.getServices();
	}
	
	@Override
	public TypeBinding[] getImplementations(TypeBinding binding) {
		resolveTypes();
		return super.getImplementations(binding);
	}

	private void resolveTypes() {
		if (this.scope != null) {
			ModuleDeclaration ast = this.scope.referenceCompilationUnit().moduleDeclaration;
			if (ast != null)
				ast.resolveTypeDirectives(this.scope);
		}
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
	@Override
	public long getAnnotationTagBits() {
		ensureAnnotationsResolved();
		return this.tagBits;
	}
	protected void ensureAnnotationsResolved() {
		if ((this.tagBits & TagBits.AnnotationResolved) == 0 && this.scope != null) {
			ModuleDeclaration module = this.scope.referenceContext.moduleDeclaration;
			ASTNode.resolveAnnotations(module.scope, module.annotations, this);
			if ((this.tagBits & TagBits.AnnotationDeprecated) != 0) {
				this.modifiers |= ClassFileConstants.AccDeprecated;
				this.tagBits |= TagBits.DeprecatedAnnotationResolved;
			}
			this.tagBits |= TagBits.AnnotationResolved;
		}
	}
	@Override
	public AnnotationBinding[] getAnnotations() {
		ensureAnnotationsResolved();
		return retrieveAnnotations(this);
	}

	@Override
	SimpleLookupTable storedAnnotations(boolean forceInitialize, boolean forceStore) {
		if (this.scope != null) { // scope null when no annotation cached, and module got processed fully (159631)
			SimpleLookupTable annotationTable = super.storedAnnotations(forceInitialize, forceStore); 
			if (annotationTable != null)
				this.scope.referenceCompilationUnit().compilationResult.hasAnnotations = true;
			return annotationTable;
		}
		return null;
	}
}
