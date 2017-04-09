/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for bug 215139
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.HandleFactory;
import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;

/**
 * Wrapper used to link {@link IRestrictedAccessTypeRequestor} with {@link TypeNameRequestor}.
 * This wrapper specifically allows usage of internal method {@link BasicSearchEngine#searchAllTypeNames(
 * 	char[] packageName,
 * 	int packageMatchRule,
 * 	char[] typeName,
 * 	int typeMatchRule,
 * 	int searchFor,
 * 	org.eclipse.jdt.core.search.IJavaSearchScope scope,
 * 	IRestrictedAccessTypeRequestor nameRequestor,
 * 	int waitingPolicy,
 * 	org.eclipse.core.runtime.IProgressMonitor monitor) }.
 * from  API method {@link org.eclipse.jdt.core.search.SearchEngine#searchAllTypeNames(
 * 	char[] packageName,
 * 	int packageMatchRule,
 * 	char[] typeName,
 * 	int matchRule,
 * 	int searchFor,
 * 	org.eclipse.jdt.core.search.IJavaSearchScope scope,
 * 	TypeNameRequestor nameRequestor,
 * 	int waitingPolicy,
 * 	org.eclipse.core.runtime.IProgressMonitor monitor) }.
 */
public class TypeNameMatchRequestorWrapper implements IRestrictedAccessTypeRequestor {
	TypeNameMatchRequestor requestor;
	private IJavaSearchScope scope; // scope is needed to retrieve project path for external resource
	private HandleFactory handleFactory; // in case of IJavaSearchScope defined by clients, use an HandleFactory instead

	/**
	 * Cache package fragment root information to optimize speed performance.
	 */
	private String lastPkgFragmentRootPath;
	private IPackageFragmentRoot lastPkgFragmentRoot;

	/**
	 * Cache package handles to optimize memory.
	 */
	private HashtableOfArrayToObject packageHandles;
	private Object lastProject;
	private long complianceValue;

public TypeNameMatchRequestorWrapper(TypeNameMatchRequestor requestor, IJavaSearchScope scope) {
	this.requestor = requestor;
	this.scope = scope;
	if (!(scope instanceof AbstractJavaSearchScope)) {
		this.handleFactory = new HandleFactory();
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.core.search.IRestrictedAccessTypeRequestor#acceptType(int, char[], char[], char[][], java.lang.String, org.eclipse.jdt.internal.compiler.env.AccessRestriction)
 */
public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path, AccessRestriction access) {

	// Get type
	try {
		IType type = null;
		if (this.handleFactory != null) {
			Openable openable = this.handleFactory.createOpenable(path, this.scope);
			if (openable == null) return;
			switch (openable.getElementType()) {
				case IJavaElement.COMPILATION_UNIT:
					ICompilationUnit cu = (ICompilationUnit) openable;
					if (enclosingTypeNames != null && enclosingTypeNames.length > 0) {
						type = cu.getType(new String(enclosingTypeNames[0]));
						for (int j=1, l=enclosingTypeNames.length; j<l; j++) {
							type = type.getType(new String(enclosingTypeNames[j]));
						}
						type = type.getType(new String(simpleTypeName));
					} else {
						type = cu.getType(new String(simpleTypeName));
					}
					break;
				case IJavaElement.CLASS_FILE:
					type = ((IClassFile)openable).getType();
					break;
			}
		} else {
			int separatorIndex= path.indexOf(IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR);
			type = separatorIndex == -1
				? createTypeFromPath(path, new String(simpleTypeName), enclosingTypeNames)
				: createTypeFromJar(path, separatorIndex);
		}

		// Accept match if the type has been found
		if (type != null) {
			// hierarchy scopes require one more check:
			if (!(this.scope instanceof HierarchyScope) || ((HierarchyScope)this.scope).enclosesFineGrained(type)) {

				// Create the match
				final JavaSearchTypeNameMatch match = new JavaSearchTypeNameMatch(type, modifiers);

				// Update match accessibility
				if(access != null) {
					switch (access.getProblemId()) {
						case IProblem.ForbiddenReference:
							match.setAccessibility(IAccessRule.K_NON_ACCESSIBLE);
							break;
						case IProblem.DiscouragedReference:
							match.setAccessibility(IAccessRule.K_DISCOURAGED);
							break;
					}
				}

				// Accept match
				this.requestor.acceptTypeNameMatch(match);
			}
		}
	} catch (JavaModelException e) {
		// skip
	}
}
private IType createTypeFromJar(String resourcePath, int separatorIndex) throws JavaModelException {
	// path to a class file inside a jar
	// Optimization: cache package fragment root handle and package handles
	if (this.lastPkgFragmentRootPath == null
			|| this.lastPkgFragmentRootPath.length() > resourcePath.length()
			|| !resourcePath.startsWith(this.lastPkgFragmentRootPath)) {
		String jarPath= resourcePath.substring(0, separatorIndex);
		IPackageFragmentRoot root= ((AbstractJavaSearchScope)this.scope).packageFragmentRoot(resourcePath, separatorIndex, jarPath);
		if (root == null) return null;
		this.lastPkgFragmentRootPath= jarPath;
		this.lastPkgFragmentRoot= root;
		this.packageHandles= new HashtableOfArrayToObject(5);
	}
	// create handle
	String classFilePath= resourcePath.substring(separatorIndex + 1);
	String[] simpleNames = new Path(classFilePath).segments();
	String[] pkgName;
	int length = simpleNames.length-1;
	if (length > 0) {
		pkgName = new String[length];
		System.arraycopy(simpleNames, 0, pkgName, 0, length);
	} else {
		pkgName = CharOperation.NO_STRINGS;
	}
	IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(pkgName);
	if (pkgFragment == null) {
		pkgFragment= ((PackageFragmentRoot) this.lastPkgFragmentRoot).getPackageFragment(pkgName);
		// filter org.apache.commons.lang.enum package for projects above 1.5 
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=317264
		if (length == 5 && pkgName[4].equals("enum")) { //$NON-NLS-1$
			IJavaProject proj = (IJavaProject)pkgFragment.getAncestor(IJavaElement.JAVA_PROJECT);
			if (!proj.equals(this.lastProject)) {
				String complianceStr = proj.getOption(CompilerOptions.OPTION_Source, true);
				this.complianceValue = CompilerOptions.versionToJdkLevel(complianceStr);
				this.lastProject = proj;
			}
			if (this.complianceValue >= ClassFileConstants.JDK1_5)
				return null;
		} 
		this.packageHandles.put(pkgName, pkgFragment);
	}
	return pkgFragment.getClassFile(simpleNames[length]).getType();
}
private IType createTypeFromPath(String resourcePath, String simpleTypeName, char[][] enclosingTypeNames) throws JavaModelException {
	// path to a file in a directory
	// Optimization: cache package fragment root handle and package handles
	int rootPathLength = -1;
	if (this.lastPkgFragmentRootPath == null
		|| !(resourcePath.startsWith(this.lastPkgFragmentRootPath)
			&& (rootPathLength = this.lastPkgFragmentRootPath.length()) > 0
			&& resourcePath.charAt(rootPathLength) == '/')) {
		PackageFragmentRoot root = (PackageFragmentRoot) ((AbstractJavaSearchScope)this.scope).packageFragmentRoot(resourcePath, -1/*not a jar*/, null/*no jar path*/);
		if (root == null) return null;
		this.lastPkgFragmentRoot = root;
		this.lastPkgFragmentRootPath = root.internalPath().toString();
		this.packageHandles = new HashtableOfArrayToObject(5);
	}
	// create handle
	resourcePath = resourcePath.substring(this.lastPkgFragmentRootPath.length() + 1);
	String[] simpleNames = new Path(resourcePath).segments();
	String[] pkgName;
	int length = simpleNames.length-1;
	if (length > 0) {
		pkgName = new String[length];
		System.arraycopy(simpleNames, 0, pkgName, 0, length);
	} else {
		pkgName = CharOperation.NO_STRINGS;
	}
	IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(pkgName);
	if (pkgFragment == null) {
		pkgFragment= ((PackageFragmentRoot) this.lastPkgFragmentRoot).getPackageFragment(pkgName);
		this.packageHandles.put(pkgName, pkgFragment);
	}
	String simpleName= simpleNames[length];
	if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(simpleName)) {
		ICompilationUnit unit= pkgFragment.getCompilationUnit(simpleName);
		int etnLength = enclosingTypeNames == null ? 0 : enclosingTypeNames.length;
		IType type = (etnLength == 0) ? unit.getType(simpleTypeName) : unit.getType(new String(enclosingTypeNames[0]));
		if (etnLength > 0) {
			for (int i=1; i<etnLength; i++) {
				type = type.getType(new String(enclosingTypeNames[i]));
			}
			type = type.getType(simpleTypeName);
		}
		return type;
	} else if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(simpleName)){
		IClassFile classFile= pkgFragment.getClassFile(simpleName);
		return classFile.getType();
	}
	return null;
}
}
