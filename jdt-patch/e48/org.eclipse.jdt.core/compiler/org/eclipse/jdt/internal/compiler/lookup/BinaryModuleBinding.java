/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software SE, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.HashMap;
import java.util.stream.Stream;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryModule;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.compiler.env.IModule.IService;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;

public class BinaryModuleBinding extends ModuleBinding {
	
	private static class AutomaticModuleBinding extends ModuleBinding {

		boolean autoNameFromManifest;

		public AutomaticModuleBinding(IModule module, LookupEnvironment existingEnvironment) {
			super(module.name(), existingEnvironment);
			existingEnvironment.root.knownModules.put(this.moduleName, this);
			this.isAuto = true;
			this.autoNameFromManifest = module.isAutoNameFromManifest();
			this.requires = Binding.NO_MODULES;
			this.requiresTransitive = Binding.NO_MODULES;
			this.exportedPackages = Binding.NO_PACKAGES;
		}
		@Override
		public boolean hasUnstableAutoName() {
			return !this.autoNameFromManifest;
		}
		@Override
		public ModuleBinding[] getRequiresTransitive() {
			if (this.requiresTransitive == NO_MODULES) {
				char[][] autoModules = ((IModuleAwareNameEnvironment)this.environment.nameEnvironment).getAllAutomaticModules();
				this.requiresTransitive = Stream.of(autoModules)
					.filter(name -> !CharOperation.equals(name, this.moduleName))
					.map(name -> this.environment.getModule(name)).filter(m -> m != null)
					.toArray(ModuleBinding[]::new);
			}
			return this.requiresTransitive;
		}
		@Override
		public char[] nameForLookup() {
			return ANY_NAMED;
		}
	}
	
	private IPackageExport[] unresolvedExports;
	private IPackageExport[] unresolvedOpens;
	private char[][] unresolvedUses;
	private IService[] unresolvedProvides;
	
	/**
	 * Construct a named module from binary, could be an auto module - or from an info from Java Model.
	 * <p>
	 * <strong>Side effects:</strong> adds the new module to root.knownModules and resolves its directives.
	 * </p>
	 */
	public static ModuleBinding create(IModule module, LookupEnvironment existingEnvironment) {
		if (module.isAutomatic())
			return new AutomaticModuleBinding(module, existingEnvironment);
		return new BinaryModuleBinding(module, existingEnvironment);
	}

	private BinaryModuleBinding(IModule module, LookupEnvironment existingEnvironment) {
		super(module.name(), existingEnvironment);
		existingEnvironment.root.knownModules.put(this.moduleName, this);
		cachePartsFrom(module);
	}
	
	void cachePartsFrom(IModule module) {
		if (module.isOpen())
			this.modifiers |= ClassFileConstants.ACC_OPEN;

		IModuleReference[] requiresReferences = module.requires();
		this.requires = new ModuleBinding[requiresReferences.length];
		this.requiresTransitive = new ModuleBinding[requiresReferences.length];
		int count = 0;
		int transitiveCount = 0;
		for (int i = 0; i < requiresReferences.length; i++) {
			ModuleBinding requiredModule = this.environment.getModule(requiresReferences[i].name());
			if (requiredModule != null) {
				this.requires[count++] = requiredModule;
				if (requiresReferences[i].isTransitive())
					this.requiresTransitive[transitiveCount++] = requiredModule;
			}
			// TODO(SHMOD): handle null case
		}
		if (count < this.requiresTransitive.length)
			System.arraycopy(this.requires, 0, this.requires = new ModuleBinding[count], 0, count);
		if (transitiveCount < this.requiresTransitive.length)
			System.arraycopy(this.requiresTransitive, 0, this.requiresTransitive = new ModuleBinding[transitiveCount], 0, transitiveCount);

		this.unresolvedExports = module.exports();
		this.unresolvedOpens = module.opens();
		this.unresolvedUses = module.uses();
		this.unresolvedProvides = module.provides();
		if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
			if (module instanceof IBinaryModule)
				scanForNullDefaultAnnotation((IBinaryModule) module);
//			this.setAnnotations(BinaryTypeBinding.createAnnotations(((IBinaryModule) module).getAnnotations(), this.environment, null));
		}
	}

	private void scanForNullDefaultAnnotation(IBinaryModule binaryModule) {
		// trimmed-down version of BinaryTypeBinding.scanForNullDefaultAnnotation()
		char[][] nonNullByDefaultAnnotationName = this.environment.getNonNullByDefaultAnnotationName();
		if (nonNullByDefaultAnnotationName == null)
			return; // not well-configured to use null annotations

		IBinaryAnnotation[] annotations = binaryModule.getAnnotations();
		if (annotations != null) {
			int nullness = NO_NULL_DEFAULT;
			int length = annotations.length;
			for (int i = 0; i < length; i++) {
				char[] annotationTypeName = annotations[i].getTypeName();
				if (annotationTypeName[0] != Util.C_RESOLVED)
					continue;
				int typeBit = this.environment.getNullAnnotationBit(BinaryTypeBinding.signature2qualifiedTypeName(annotationTypeName));
				if (typeBit == TypeIds.BitNonNullByDefaultAnnotation) {
					// using NonNullByDefault we need to inspect the details of the value() attribute:
					nullness |= BinaryTypeBinding.getNonNullByDefaultValue(annotations[i], this.environment);
				}
			}
			this.defaultNullness = nullness;
		}
	}

	@Override
	public PackageBinding[] getExports() {
		if (this.exportedPackages == null && this.unresolvedExports != null)
			resolvePackages();
		return super.getExports();
	}
	
	@Override
	public PackageBinding[] getOpens() {
		if (this.openedPackages == null && this.unresolvedOpens != null)
			resolvePackages();
		return super.getOpens();
	}

	private void resolvePackages() {
		this.exportedPackages = new PackageBinding[this.unresolvedExports.length];
		int count = 0;
		for (int i = 0; i < this.unresolvedExports.length; i++) {
			IPackageExport export = this.unresolvedExports[i];
			PackageBinding declaredPackage = getVisiblePackage(CharOperation.splitOn('.', export.name()));
			if (declaredPackage != null) {
				this.exportedPackages[count++] = declaredPackage;
				declaredPackage.isExported = Boolean.TRUE;
				recordExportRestrictions(declaredPackage, export.targets());
			} else {
				// TODO(SHMOD): report incomplete module path?
			}
		}
		if (count < this.exportedPackages.length)
			System.arraycopy(this.exportedPackages, 0, this.exportedPackages = new PackageBinding[count], 0, count);
		
		this.openedPackages = new PackageBinding[this.unresolvedOpens.length];
		count = 0;
		for (int i = 0; i < this.unresolvedOpens.length; i++) {
			IPackageExport opens = this.unresolvedOpens[i];
			PackageBinding declaredPackage = getVisiblePackage(CharOperation.splitOn('.', opens.name()));
			if (declaredPackage != null) {
				this.openedPackages[count++] = declaredPackage;
				recordOpensRestrictions(declaredPackage, opens.targets());
			} else {
				// TODO(SHMOD): report incomplete module path?
			}
		}
		if (count < this.openedPackages.length)
			System.arraycopy(this.openedPackages, 0, this.openedPackages = new PackageBinding[count], 0, count);
	}
	
	@Override
	public TypeBinding[] getUses() {
		if (this.uses == null) {
			this.uses = new TypeBinding[this.unresolvedUses.length];
			for (int i = 0; i < this.unresolvedUses.length; i++)
				this.uses[i] = this.environment.getType(CharOperation.splitOn('.', this.unresolvedUses[i]), this);
		}
		return super.getUses();
	}
	
	@Override
	public TypeBinding[] getServices() {
		if (this.services == null)
			resolveServices();
		return super.getServices();
	}

	@Override
	public TypeBinding[] getImplementations(TypeBinding binding) {
		if (this.implementations == null)
			resolveServices();
		return super.getImplementations(binding);
	}
	private void resolveServices() {
		this.services = new TypeBinding[this.unresolvedProvides.length];
		this.implementations = new HashMap<>();
		for (int i = 0; i < this.unresolvedProvides.length; i++) {
			this.services[i] = this.environment.getType(CharOperation.splitOn('.', this.unresolvedProvides[i].name()), this);
			char[][] implNames = this.unresolvedProvides[i].with();
			TypeBinding[] impls = new TypeBinding[implNames.length];
			for (int j = 0; j < implNames.length; j++)
				impls[j] = this.environment.getType(CharOperation.splitOn('.', implNames[j]), this);
			this.implementations.put(this.services[i], impls);
		}
	}
}
