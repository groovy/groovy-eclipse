/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryModule;
import org.eclipse.jdt.internal.compiler.env.IModule;

public class ModuleInfo extends ClassFileStruct implements IBinaryModule {
	protected int flags;
	protected int requiresCount;
	protected int exportsCount;
	protected int usesCount;
	protected int providesCount;
	protected int opensCount;
	protected char[] name;
	protected char[] version;
	protected ModuleReferenceInfo[] requires;
	protected PackageExportInfo[] exports;
	protected PackageExportInfo[] opens;
	char[][] uses;
	IModule.IService[] provides;

	@Override
	public boolean isOpen() {
		return (this.flags & ClassFileConstants.ACC_OPEN) != 0;
	}
	public int requiresCount() {
		return this.requiresCount;
	}
	public int exportsCount() {
		return this.exportsCount;
	}
	public int usesCount() {
		return this.usesCount;
	}
	public int providesCount() {
		return this.providesCount;
	}
	@Override
	public char[] name() {
		return this.name;
	}
	public void setName(char[] name) {
		this.name = name;
	}
	@Override
	public IModule.IModuleReference[] requires() {
		return this.requires;
	}
	@Override
	public IModule.IPackageExport[] exports() {
		return this.exports;
	}
	@Override
	public char[][] uses() {
		return this.uses;
	}
	@Override
	public IService[] provides() {
		return this.provides;
	}
	@Override
	public IModule.IPackageExport[] opens() {
		return this.opens;
	}
	@Override
	public void addReads(char[] modName) {
		Predicate<char[]> shouldAdd = m -> {
			return Stream.of(this.requires).map(ref -> ref.name()).noneMatch(n -> CharOperation.equals(modName, n));
		};
		if (shouldAdd.test(modName)) {
			int len = this.requires.length;
			this.requires = Arrays.copyOf(this.requires, len);
			ModuleReferenceInfo info = this.requires[len] = new ModuleReferenceInfo();
			info.refName = modName;
		}		
	}
	@Override
	public void addExports(IPackageExport[] toAdd) {
		Predicate<char[]> shouldAdd = m -> {
			return Stream.of(this.exports).map(ref -> ref.packageName).noneMatch(n -> CharOperation.equals(m, n));
		};
		Collection<PackageExportInfo> merged = Stream.concat(Stream.of(this.exports), Stream.of(toAdd)
				.filter(e -> shouldAdd.test(e.name()))
				.map(e -> {
					PackageExportInfo exp = new PackageExportInfo();
					exp.packageName = e.name();
					exp.exportedTo = e.targets();
					return exp;
				}))
			.collect(
				ArrayList::new,
				ArrayList::add,
				ArrayList::addAll);
		this.exports = merged.toArray(new PackageExportInfo[merged.size()]);
	}
	/**
	 * @param classFileBytes byte[]
	 * @param offsets int[]
	 * @param offset int
	 */
	protected ModuleInfo (byte classFileBytes[], int offsets[], int offset) {
		super(classFileBytes, offsets, offset);
	}

	public static ModuleInfo createModule(char[] className, byte classFileBytes[], int offsets[], int offset) {

		int readOffset = offset;
//		module.name = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1)); // returns 'Module' 
		int moduleOffset = readOffset + 6;
		int utf8Offset;
		ModuleInfo module = new ModuleInfo(classFileBytes, offsets, 0);
		int name_index = module.constantPoolOffsets[module.u2At(moduleOffset)];
		utf8Offset = module.constantPoolOffsets[module.u2At(name_index + 1)];
		module.name = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
		CharOperation.replace(module.name, '/', '.');
		moduleOffset += 2;
		module.flags = module.u2At(moduleOffset);
		moduleOffset += 2;
		int version_index = module.u2At(moduleOffset);
		if (version_index > 0) {
			utf8Offset = module.constantPoolOffsets[version_index];
			module.version = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
		}
		moduleOffset += 2;

		utf8Offset = module.constantPoolOffsets[module.u2At(readOffset)];
		int count = module.u2At(moduleOffset);
		module.requiresCount = count;
		module.requires = new ModuleReferenceInfo[count];
		moduleOffset += 2;
		for (int i = 0; i < count; i++) {
			name_index = module.constantPoolOffsets[module.u2At(moduleOffset)];
			utf8Offset = module.constantPoolOffsets[module.u2At(name_index + 1)];
			char[] requiresNames = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
			module.requires[i] = module.new ModuleReferenceInfo();
			CharOperation.replace(requiresNames, '/', '.');
			module.requires[i].refName = requiresNames;
			moduleOffset += 2;
			int modifiers = module.u2At(moduleOffset);
			module.requires[i].modifiers = modifiers;
			module.requires[i].isTransitive = (ClassFileConstants.ACC_TRANSITIVE & modifiers) != 0; // Access modifier
			moduleOffset += 2;
			version_index = module.u2At(moduleOffset);
			if (version_index > 0) {
				utf8Offset = module.constantPoolOffsets[version_index];
				module.requires[i].required_version = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
			}
			moduleOffset += 2;
		}
		count = module.u2At(moduleOffset);
		moduleOffset += 2;
		module.exportsCount = count;
		module.exports = new PackageExportInfo[count];
		for (int i = 0; i < count; i++) {
			name_index = module.constantPoolOffsets[module.u2At(moduleOffset)];
			utf8Offset = module.constantPoolOffsets[module.u2At(name_index + 1)];
			char[] exported = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
			CharOperation.replace(exported, '/', '.');
			PackageExportInfo pack = module.new PackageExportInfo();
			module.exports[i] = pack;
			pack.packageName = exported;
			moduleOffset += 2;
			pack.modifiers = module.u2At(moduleOffset);
			moduleOffset += 2;
			int exportedtoCount = module.u2At(moduleOffset);
			moduleOffset += 2;
			if (exportedtoCount > 0) {
				pack.exportedTo = new char[exportedtoCount][];
				pack.exportedToCount = exportedtoCount;
				for(int k = 0; k < exportedtoCount; k++) {
					name_index = module.constantPoolOffsets[module.u2At(moduleOffset)];
					utf8Offset = module.constantPoolOffsets[module.u2At(name_index + 1)];
					char[] exportedToName = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
					CharOperation.replace(exportedToName, '/', '.');
					pack.exportedTo[k] = exportedToName;
					moduleOffset += 2;
				}
			}
		}
		count = module.u2At(moduleOffset);
		moduleOffset += 2;
		module.opensCount = count;
		module.opens = new PackageExportInfo[count];
		for (int i = 0; i < count; i++) {
			name_index = module.constantPoolOffsets[module.u2At(moduleOffset)];
			utf8Offset = module.constantPoolOffsets[module.u2At(name_index + 1)];
			char[] exported = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
			CharOperation.replace(exported, '/', '.');
			PackageExportInfo pack = module.new PackageExportInfo();
			module.opens[i] = pack;
			pack.packageName = exported;
			moduleOffset += 2;
			pack.modifiers = module.u2At(moduleOffset);
			moduleOffset += 2;
			int exportedtoCount = module.u2At(moduleOffset);
			moduleOffset += 2;
			if (exportedtoCount > 0) {
				pack.exportedTo = new char[exportedtoCount][];
				pack.exportedToCount = exportedtoCount;
				for(int k = 0; k < exportedtoCount; k++) {
					name_index = module.constantPoolOffsets[module.u2At(moduleOffset)];
					utf8Offset = module.constantPoolOffsets[module.u2At(name_index + 1)];
					char[] exportedToName = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
					CharOperation.replace(exportedToName, '/', '.');
					pack.exportedTo[k] = exportedToName;
					moduleOffset += 2;
				}
			}
		}
		count = module.u2At(moduleOffset);
		moduleOffset += 2;
		module.usesCount = count;
		module.uses = new char[count][];
		for (int i = 0; i < count; i++) {
			int classIndex = module.constantPoolOffsets[module.u2At(moduleOffset)];
			utf8Offset = module.constantPoolOffsets[module.u2At(classIndex + 1)];
			char[] inf = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
			CharOperation.replace(inf, '/', '.');
			module.uses[i] = inf;
			moduleOffset += 2;
		}
		count = module.u2At(moduleOffset);
		moduleOffset += 2;
		module.providesCount = count;
		module.provides = new ServiceInfo[count];
		for (int i = 0; i < count; i++) {
			int classIndex = module.constantPoolOffsets[module.u2At(moduleOffset)];
			utf8Offset = module.constantPoolOffsets[module.u2At(classIndex + 1)];
			char[] inf = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
			CharOperation.replace(inf, '/', '.');
			ServiceInfo service = module.new ServiceInfo();
			module.provides[i] = service;
			service.serviceName = inf;
			moduleOffset += 2;
			int implCount = module.u2At(moduleOffset);
			moduleOffset += 2;
			service.with = new char[implCount][];
			if (implCount > 0) {
				service.with = new char[implCount][];
				for(int k = 0; k < implCount; k++) {
					classIndex = module.constantPoolOffsets[module.u2At(moduleOffset)];
					utf8Offset = module.constantPoolOffsets[module.u2At(classIndex + 1)];
					char[] implName = module.utf8At(utf8Offset + 3, module.u2At(utf8Offset + 1));
					CharOperation.replace(implName, '/', '.');
					service.with[k] = implName;
					moduleOffset += 2;
				}
			}
		}
		return module;
	}
	class ModuleReferenceInfo implements IModule.IModuleReference {
		char[] refName;
		boolean isTransitive = false;
		int modifiers;
		char[] required_version;
		@Override
		public char[] name() {
			return this.refName;
		}
		@Override
		public boolean isTransitive() {
			return this.isTransitive;
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) 
				return true;
			if (!(o instanceof IModule.IModuleReference))
				return false;
			IModule.IModuleReference mod = (IModule.IModuleReference) o;
			if (this.modifiers != mod.getModifiers())
				return false;
			return CharOperation.equals(this.refName, mod.name(), false);
		}
		@Override
		public int hashCode() {
			return CharOperation.hashCode(this.refName);
		}
		@Override
		public int getModifiers() {
			return this.modifiers;
		}
	}
	class PackageExportInfo implements IModule.IPackageExport {
		char[] packageName;
		char[][] exportedTo;
		int exportedToCount;
		int modifiers;
		@Override
		public char[] name() {
			return this.packageName;
		}

		@Override
		public char[][] targets() {
			return this.exportedTo;
		}
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			toStringContent(buffer);
			return buffer.toString();
		}
		protected void toStringContent(StringBuffer buffer) {
			buffer.append(this.packageName);
			if (this.exportedToCount > 0) {
				buffer.append(" to "); //$NON-NLS-1$
				for(int i = 0; i < this.exportedToCount; i++) {
					buffer.append(this.exportedTo[i]);
					buffer.append(',').append(' ');
				}
			}
			buffer.append(';').append('\n');
		}
	}
	class ServiceInfo implements IModule.IService {
		char[] serviceName;
		char[][] with;
		@Override
		public char[] name() {
			return this.serviceName;
		}

		@Override
		public char[][] with() {
			return this.with;
		}
	}
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof IModule))
			return false;
		IModule mod = (IModule) o;
		if (!CharOperation.equals(this.name, mod.name()))
			return false;
		return Arrays.equals(this.requires, mod.requires());
	}
	@Override
	public int hashCode() {
		int result = 17;
		int c = CharOperation.hashCode(this.name);
		result = 31 * result + c;
		c =  Arrays.hashCode(this.requires);
		result = 31 * result + c;
		return result;
	}
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(getClass().getName());
		toStringContent(buffer);
		return buffer.toString();
	}
	protected void toStringContent(StringBuffer buffer) {
		buffer.append("\nmodule "); //$NON-NLS-1$
		buffer.append(this.name).append(' ');
		buffer.append('{').append('\n');
		if (this.requiresCount > 0) {
			for(int i = 0; i < this.requiresCount; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (this.requires[i].isTransitive) {
					buffer.append(" public "); //$NON-NLS-1$
				}
				buffer.append(this.requires[i].refName);
				buffer.append(';').append('\n');
			}
		}
		if (this.exportsCount > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.exportsCount; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(this.exports[i].toString());
			}
		}
		buffer.append('\n').append('}').toString();
	}
}
