/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * Internal implementation of package bindings.
 */
@SuppressWarnings("rawtypes")
class PackageBinding implements IPackageBinding {

	private static final String[] NO_NAME_COMPONENTS = CharOperation.NO_STRINGS;
	private static final String UNNAMED = Util.EMPTY_STRING;
	private static final char PACKAGE_NAME_SEPARATOR = '.';

	private org.eclipse.jdt.internal.compiler.lookup.PackageBinding binding;
	private String name;
	private BindingResolver resolver;
	private String[] components;

	PackageBinding(org.eclipse.jdt.internal.compiler.lookup.PackageBinding binding, BindingResolver resolver) {
		this.binding = binding;
		this.resolver = resolver;
	}

	public IAnnotationBinding[] getAnnotations() {
		try {
			INameEnvironment nameEnvironment = this.binding.environment.nameEnvironment;
			if (!(nameEnvironment instanceof SearchableEnvironment))
				return AnnotationBinding.NoAnnotations;
			NameLookup nameLookup = ((SearchableEnvironment) nameEnvironment).nameLookup;
			if (nameLookup == null)
				return AnnotationBinding.NoAnnotations;
			final String pkgName = getName();
			IPackageFragment[] pkgs = nameLookup.findPackageFragments(pkgName, false/*exact match*/);
			if (pkgs == null)
				return AnnotationBinding.NoAnnotations;

			for (int i = 0, len = pkgs.length; i < len; i++) {
				int fragType = pkgs[i].getKind();
				switch(fragType) {
					case IPackageFragmentRoot.K_SOURCE:
						String unitName = "package-info.java"; //$NON-NLS-1$
						ICompilationUnit unit = pkgs[i].getCompilationUnit(unitName);
						if (unit != null && unit.exists()) {
							ASTParser p = ASTParser.newParser(AST.JLS3_INTERNAL);
							p.setSource(unit);
							p.setResolveBindings(true);
							p.setUnitName(unitName);
							p.setFocalPosition(0);
							p.setKind(ASTParser.K_COMPILATION_UNIT);
							CompilationUnit domUnit = (CompilationUnit) p.createAST(null);
							PackageDeclaration pkgDecl = domUnit.getPackage();
							if (pkgDecl != null) {
								List annos = pkgDecl.annotations();
								if (annos == null || annos.isEmpty())
									return AnnotationBinding.NoAnnotations;
								IAnnotationBinding[] result = new IAnnotationBinding[annos.size()];
								int index=0;
		 						for (Iterator it = annos.iterator(); it.hasNext(); index++) {
									result[index] = ((Annotation) it.next()).resolveAnnotationBinding();
									// not resolving bindings
									if (result[index] == null)
										return AnnotationBinding.NoAnnotations;
								}
								return result;
							}
						}
						break;
					case IPackageFragmentRoot.K_BINARY:
						NameEnvironmentAnswer answer =
							nameEnvironment.findType(TypeConstants.PACKAGE_INFO_NAME, this.binding.compoundName);
						if (answer != null && answer.isBinaryType()) {
							IBinaryType type = answer.getBinaryType();
							char[][][] missingTypeNames = type.getMissingTypeNames();
							IBinaryAnnotation[] binaryAnnotations = type.getAnnotations();
							org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding[] binaryInstances =
								BinaryTypeBinding.createAnnotations(binaryAnnotations, this.binding.environment, missingTypeNames);
							org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding[] allInstances =
								org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding.addStandardAnnotations(binaryInstances, type.getTagBits(), this.binding.environment);
							int total = allInstances.length;
							IAnnotationBinding[] domInstances = new AnnotationBinding[total];
							for (int a = 0; a < total; a++) {
								final IAnnotationBinding annotationInstance = this.resolver.getAnnotationInstance(allInstances[a]);
								if (annotationInstance == null) {// not resolving binding
									return AnnotationBinding.NoAnnotations;
								}
								domInstances[a] = annotationInstance;
							}
							return domInstances;
						}
				}
			}
		} catch(JavaModelException e) {
			return AnnotationBinding.NoAnnotations;
		}
		return AnnotationBinding.NoAnnotations;
	}

	/*
	 * @see IBinding#getName()
	 */
	public String getName() {
		if (this.name == null) {
			computeNameAndComponents();
		}
		return this.name;
	}

	/*
	 * @see IPackageBinding#isUnnamed()
	 */
	public boolean isUnnamed() {
		return getName().equals(UNNAMED);
	}

	/*
	 * @see IPackageBinding#getNameComponents()
	 */
	public String[] getNameComponents() {
		if (this.components == null) {
			computeNameAndComponents();
		}
		return this.components;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.PACKAGE;
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		return Modifier.NONE;
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		return false;
	}

	/**
	 * @see IBinding#isRecovered()
	 */
	public boolean isRecovered() {
		return false;
	}

	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		return false;
	}

	/*
	 * @see IBinding#getJavaElement()
	 */
	public IJavaElement getJavaElement() {
		INameEnvironment nameEnvironment = this.binding.environment.nameEnvironment; // a package binding always has a LooupEnvironment set
		if (!(nameEnvironment instanceof SearchableEnvironment)) return null;
		// this is not true in standalone DOM/AST
		NameLookup nameLookup = ((SearchableEnvironment) nameEnvironment).nameLookup;
		if (nameLookup == null) return null;
		IJavaElement[] pkgs = nameLookup.findPackageFragments(getName(), false/*exact match*/);
		if (pkgs == null) return null;
		if (pkgs.length == 0) {
			// add additional tracing as this should not happen
			org.eclipse.jdt.internal.core.util.Util.log(
				new Status(
						IStatus.WARNING,
						JavaCore.PLUGIN_ID,
						"Searching for package " + getName() + " returns an empty array")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		return pkgs[0];
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		return new String(this.binding.computeUniqueKey());
	}

	/*
	 * @see IBinding#isEqualTo(Binding)
	 * @since 3.1
	 */
	public boolean isEqualTo(IBinding other) {
		if (other == this) {
			// identical binding - equal (key or no key)
			return true;
		}
		if (other == null) {
			// other binding missing
			return false;
		}
		if (!(other instanceof PackageBinding)) {
			return false;
		}
		org.eclipse.jdt.internal.compiler.lookup.PackageBinding packageBinding2 = ((PackageBinding) other).binding;
		return CharOperation.equals(this.binding.compoundName, packageBinding2.compoundName);
	}

	private void computeNameAndComponents() {
		char[][] compoundName = this.binding.compoundName;
		if (compoundName == CharOperation.NO_CHAR_CHAR || compoundName == null) {
			this.name = UNNAMED;
			this.components = NO_NAME_COMPONENTS;
		} else {
			int length = compoundName.length;
			this.components = new String[length];
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < length - 1; i++) {
				this.components[i] = new String(compoundName[i]);
				buffer.append(compoundName[i]).append(PACKAGE_NAME_SEPARATOR);
			}
			this.components[length - 1] = new String(compoundName[length - 1]);
			buffer.append(compoundName[length - 1]);
			this.name = buffer.toString();
		}
	}

	/*
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
}
