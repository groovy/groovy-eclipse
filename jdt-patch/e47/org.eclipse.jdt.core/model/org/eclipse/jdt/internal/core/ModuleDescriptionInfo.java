/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ExportsStatement;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.ast.OpensStatement;
import org.eclipse.jdt.internal.compiler.ast.ProvidesStatement;
import org.eclipse.jdt.internal.compiler.ast.RequiresStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UsesStatement;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class ModuleDescriptionInfo extends AnnotatableInfo implements IModule {

	protected static final char[][] NO_USES = new char[0][0];
	protected static final ModuleReferenceInfo[] NO_REQUIRES = new ModuleReferenceInfo[0];
	protected static final PackageExportInfo[] NO_EXPORTS = new PackageExportInfo[0];
	protected static final ServiceInfo[] NO_PROVIDES = new ServiceInfo[0];
	protected static final PackageExportInfo[] NO_OPENS = new PackageExportInfo[0];

	protected IJavaElement[] children = JavaElement.NO_ELEMENTS;

	ModuleReferenceInfo[] requires;
	PackageExportInfo[] exports;
	ServiceInfo[] services;
	PackageExportInfo[] opens;
	char[][] usedServices;
	IModuleDescription handle;
	char[] name;
	boolean isOpen = false;

	static class ModuleReferenceInfo extends MemberElementInfo implements IModule.IModuleReference {
		char[] name;
		int modifiers;
		public char[] name() {
			return this.name;
		}
		public int getModifiers() {
			return this.modifiers;
		}
	}
	static class PackageExportInfo extends MemberElementInfo implements IModule.IPackageExport {
		char[] pack;
		char[][] target;
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(this.pack);
			if (this.target != null) {
				buffer.append(" to "); //$NON-NLS-1$
				for (char[] mod : this.target) {
					buffer.append(mod);
				}
			}
			buffer.append(';');
			return buffer.toString();
		}

		@Override
		public char[] name() {
			return this.pack;
		}

		@Override
		public char[][] targets() {
			return this.target;
		}
	}

	static class ServiceInfo extends MemberElementInfo implements IModule.IService {
		char[] serviceName;
		char[][] implNames;
		@Override
		public char[] name() {
			return this.serviceName;
		}
		@Override
		public char[][] with() {
			return this.implNames;
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(this.serviceName);
			buffer.append(" with "); //$NON-NLS-1$
			for (int i = 0; i < this.implNames.length; i++) {
				buffer.append(this.implNames[i]);
				if (i < this.implNames.length - 1)
					buffer.append(", "); //$NON-NLS-1$
			}
			buffer.append(';');
			return buffer.toString();
		}
	}
	
	public static ModuleDescriptionInfo createModule(IModule module) {
		ModuleDescriptionInfo mod = new ModuleDescriptionInfo();
		mod.children = JavaElement.NO_ELEMENTS;
		mod.name = module.name();
		mod.isOpen = module.isOpen();
		if (module.requires().length > 0) {
			IModuleReference[] refs = module.requires();
			mod.requires = new ModuleReferenceInfo[refs.length];
			for (int i = 0; i < refs.length; i++) {
				mod.requires[i] = new ModuleReferenceInfo();
				mod.requires[i].name = refs[i].name(); // Check why ModuleReference#tokens must be a char[][] and not a char[] or String;
				mod.requires[i].modifiers = refs[i].getModifiers();
			}
		} else {
			mod.requires = NO_REQUIRES;
		}
		if (module.exports().length > 0) {
			IPackageExport[] refs = module.exports();
			mod.exports = new PackageExportInfo[refs.length];
			for (int i = 0; i < refs.length; i++) {
				PackageExportInfo exp = createPackageExport(refs[i]);
				mod.exports[i] = exp;
			}
		} else {
			mod.exports = NO_EXPORTS;
		}
		if (module.uses().length > 0) {
			char[][] uses = module.uses();
			mod.usedServices = new char[uses.length][];
			for (int i = 0; i < uses.length; i++) {
				mod.usedServices[i] = uses[i];
			}
		} else {
			mod.usedServices = NO_USES;
		}
		if (module.provides().length > 0) {
			IService[] provides = module.provides();
			mod.services = new ServiceInfo[provides.length];
			for (int i = 0; i < provides.length; i++) {
				mod.services[i] = createService(provides[i]);
			}
		} else {
			mod.services = NO_PROVIDES;
		}
		if (module.opens().length > 0) {
			IPackageExport[] opens = module.opens();
			mod.opens = new PackageExportInfo[opens.length];
			for (int i = 0; i < opens.length; i++) {
				PackageExportInfo op = createOpensInfo(opens[i]);
				mod.opens[i] = op;
			}
		} else {
			mod.opens = NO_OPENS;
		}
		return mod;
	}
	private static PackageExportInfo createPackageExport(IModule.IPackageExport ref) {
		PackageExportInfo exp = new PackageExportInfo();
		exp.pack = ref.name();
		char[][] targets = ref.targets();
		if (targets != null) {
			exp.target = new char[targets.length][];
			for(int j = 0; j < targets.length; j++) {
				exp.target[j] = targets[j];
			}
		}
		return exp;
	}
	private static PackageExportInfo createOpensInfo(IModule.IPackageExport opens) {
		PackageExportInfo open = new PackageExportInfo();
		open.pack = opens.name();
		char[][] targets = opens.targets();
		if (targets != null) {
			open.target = new char[targets.length][];
			for(int j = 0; j < targets.length; j++) {
				open.target[j] = targets[j];
			}
		}
		return open;
	}

	private static ServiceInfo createService(IModule.IService provides) {
		ServiceInfo info = new ServiceInfo();
		info.serviceName = provides.name();
		char[][] implementations = provides.with();
		info.implNames = new char[implementations.length][];
		for(int i = 0; i < implementations.length; i++) {
			info.implNames[i] = implementations[i];
		}
		return info;
	}

	public static ModuleDescriptionInfo createModule(ModuleDeclaration module) {
		ModuleDescriptionInfo mod = new ModuleDescriptionInfo();
		mod.name = module.moduleName;
		mod.isOpen = module.isOpen();
		if (module.requiresCount > 0) {
			RequiresStatement[] refs = module.requires;
			mod.requires = new ModuleReferenceInfo[refs.length+1];
			mod.requires[0] = getJavaBaseReference();
			for (int i = 0; i < refs.length; i++) {
				mod.requires[i+1] = new ModuleReferenceInfo();
				mod.requires[i+1].name = CharOperation.concatWith(refs[i].module.tokens, '.'); // Check why ModuleReference#tokens must be a char[][] and not a char[] or String;
				mod.requires[i+1].modifiers = refs[i].modifiers;
			}
		} else {
			mod.requires = CharOperation.equals(module.moduleName, TypeConstants.JAVA_BASE) 
					? NO_REQUIRES 
					: new ModuleReferenceInfo[] { getJavaBaseReference() };
		}
		if (module.exportsCount > 0) {
			ExportsStatement[] refs = module.exports;
			mod.exports = new PackageExportInfo[refs.length];
			for (int i = 0; i < refs.length; i++) {
				PackageExportInfo exp = createPackageExport(refs[i]);
				mod.exports[i] = exp;
			}
		} else {
			mod.exports = NO_EXPORTS;
		}
		if (module.usesCount > 0) {
			UsesStatement[] uses = module.uses;
			mod.usedServices = new char[uses.length][];
			for (int i = 0; i < uses.length; i++) {
				mod.usedServices[i] = CharOperation.concatWith(uses[i].serviceInterface.getTypeName(), '.');
			}
		} else {
			mod.usedServices = NO_USES;
		}
		if (module.servicesCount > 0) {
			ProvidesStatement[] provides = module.services;
			mod.services = new ServiceInfo[provides.length];
			for (int i = 0; i < provides.length; i++) {
				mod.services[i] = createService(provides[i]);
			}
		} else {
			mod.services = NO_PROVIDES;
		}
		if (module.opensCount > 0) {
			OpensStatement[] opens = module.opens;
			mod.opens = new PackageExportInfo[opens.length];
			for (int i = 0; i < opens.length; i++) {
				PackageExportInfo op = createOpensInfo(opens[i]);
				mod.opens[i] = op;
			}
		} else {
			mod.opens = NO_OPENS;
		}
		return mod;
	}

	private static ModuleReferenceInfo getJavaBaseReference() {
		ModuleReferenceInfo ref = new ModuleReferenceInfo();
		ref.name = TypeConstants.JAVA_BASE;
		return ref;
	}

	private static PackageExportInfo createPackageExport(ExportsStatement ref) {
		PackageExportInfo exp = new PackageExportInfo();
		exp.pack = ref.pkgName;
		ModuleReference[] imp = ref.targets;
		if (imp != null) {
			exp.target = new char[imp.length][];
			for(int j = 0; j < imp.length; j++) {
				exp.target[j] = imp[j].moduleName;
			}
		}
		return exp;
	}
	private static PackageExportInfo createOpensInfo(OpensStatement opens) {
		PackageExportInfo open = new PackageExportInfo();
		open.pack = opens.pkgName;
		ModuleReference[] imp = opens.targets;
		if (imp != null) {
			open.target = new char[imp.length][];
			for(int j = 0; j < imp.length; j++) {
				open.target[j] = imp[j].moduleName;
			}
		}
		return open;
	}

	private static ServiceInfo createService(ProvidesStatement provides) {
		ServiceInfo info = new ServiceInfo();
		info.serviceName = CharOperation.concatWith(provides.serviceInterface.getTypeName(), '.');
		TypeReference[] implementations = provides.implementations;
		info.implNames = new char[implementations.length][];
		for(int i = 0; i < implementations.length; i++) {
			info.implNames[i] = CharOperation.concatWith(implementations[i].getTypeName(), '.');
		}
		return info;
	}

	protected void setHandle(IModuleDescription handle) {
		this.handle = handle;
	}

	public IJavaElement[] getChildren() {
		return this.children;
	}

	@Override
	public boolean isOpen() {
		return this.isOpen;
	}

	@Override
	public char[] name() {
		return this.name;
	}

	@Override
	public IModule.IModuleReference[] requires() {
		return this.requires;
	}
	@Override
	public IPackageExport[] exports() {
		return this.exports;
	}

	@Override
	public char[][] uses() {
		return this.usedServices;
	}

	@Override
	public IService[] provides() {
		return this.services;
	}

	@Override
	public IPackageExport[] opens() {
		return this.opens;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(getClass().getName());
		toStringContent(buffer);
		return buffer.toString();
	}
	protected void toStringContent(StringBuffer buffer) {
		buffer.append("\n"); //$NON-NLS-1$
		if (this.isOpen())
			buffer.append("open "); //$NON-NLS-1$
		buffer.append("module "); //$NON-NLS-1$
		buffer.append(this.name).append(' ');
		buffer.append('{').append('\n');
		if (this.requires != null && this.requires.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.requires.length; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (this.requires[i].isTransitive()) {
					buffer.append("transitive "); //$NON-NLS-1$
				}
				if (this.requires[i].isStatic()) {
					buffer.append("static "); //$NON-NLS-1$
				}
				buffer.append(this.requires[i].name);
				buffer.append(';').append('\n');
			}
		}
		if (this.exports != null && this.exports.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.exports.length; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(this.exports[i].toString()).append('\n');
			}
		}
		if (this.usedServices != null && this.usedServices.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.usedServices.length; i++) {
				buffer.append("\tuses "); //$NON-NLS-1$
				buffer.append(this.usedServices[i]).append('\n');
			}
		}
		if (this.services != null && this.services.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.services.length; i++) {
				buffer.append("\tprovides "); //$NON-NLS-1$
				buffer.append(this.services[i].toString()).append('\n');
			}
		}
		if (this.opens != null && this.opens.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.opens.length; i++) {
				buffer.append("\topens "); //$NON-NLS-1$
				buffer.append(this.opens[i].toString()).append('\n');
			}
		}
		buffer.append('\n').append('}').toString();
	}
}
