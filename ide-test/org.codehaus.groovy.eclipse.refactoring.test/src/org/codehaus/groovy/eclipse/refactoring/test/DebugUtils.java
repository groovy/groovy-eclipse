/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public final class DebugUtils {

	private DebugUtils(){
	}

	public static void dumpCharCharArray(String msg, char[][] o){
		dump("DUMPING char[][]:" + msg); //$NON-NLS-1$
		for (int i= 0; i < o.length; i++){
			dump(new String(o[i]));
		}
	}

	public static void dumpArray(String msg, Object[] refs){
		System.out.println("DUMPING array: "+  msg); //$NON-NLS-1$
		if (refs == null){
			System.out.println("null"); //$NON-NLS-1$
			return;
		}
		for (int i= 0; i < refs.length; i++)
			System.out.println(refs[i].toString());
	}

	public static void dumpCollectionCollection(String msg, Collection c){
		for (Iterator iter= c.iterator(); iter.hasNext(); ){
			dumpCollection("", (List)iter.next()); //$NON-NLS-1$
		}
	}

	public static void dumpCollection(String msg, Collection c){
		System.out.println("DUMPING collection: "+  msg); //$NON-NLS-1$
		if (c == null){
			System.out.println("null"); //$NON-NLS-1$
			return;
		}
		for (Iterator iter= c.iterator(); iter.hasNext(); ){
			System.out.println(iter.next().toString());
		}
	}

	public static void dumpIMethod(IMethod method){
		try{
			if (method == null){
				System.out.println("DUMPING method: null"); //$NON-NLS-1$
				return;
			}
			System.out.println("DUMPING method:" +  method.getElementName() + "\n " + method.getSignature() + "\n declared in " + method.getDeclaringType().getFullyQualifiedName('.') //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			+ "\nreturnType:" + method.getReturnType() ); //$NON-NLS-1$
			dumpArray("paramTypes:", method.getParameterTypes()); //$NON-NLS-1$
			dumpArray("exceptions:", method.getExceptionTypes()); //$NON-NLS-1$
		}catch (JavaModelException e){
			System.out.println("JavaModelException: "+ e.getMessage()); //$NON-NLS-1$
		}
	}

	public static void dumpIMethodList(String msg, List l){
		System.out.println("DUMPING IMethodList: "+  msg); //$NON-NLS-1$
		if (l == null){
			System.out.println("null"); //$NON-NLS-1$
			return;
		}
		Iterator iter= l.iterator();
		while(iter.hasNext()){
			dumpIMethod((IMethod)iter.next());
		}
	}

	public static void dumpIType(String msg, IType type){
		System.out.println("DUMPING IType:"+ msg); //$NON-NLS-1$
		System.out.println("exists:" + type.exists()); //$NON-NLS-1$
		try{
			System.out.println("correspondingResource:" + type.getCorrespondingResource()); //$NON-NLS-1$
			System.out.println("underResource:" + type.getUnderlyingResource()); //$NON-NLS-1$
			System.out.println("source:\n" + type.getSource()); //$NON-NLS-1$

			//System.out.println("cu.orig.under" + type.getCompilationUnit().getOriginalElement().getUnderlyingResource());
			System.out.println("cu:" + type.getCompilationUnit().getSource());		 //$NON-NLS-1$
		}catch (JavaModelException e){
			System.out.println("JavaModelException: "+ e.getMessage()); //$NON-NLS-1$
		}

	}

	public static void dumpIResource(String msg, IResource res){
		System.out.println("DUMPING IResource:"+ msg); //$NON-NLS-1$
		System.out.println("name:" + res.getFullPath().toString()); //$NON-NLS-1$
		System.out.println("exists" + res.exists()); //$NON-NLS-1$
	}

	public static void dump(Object o){
		if (o == null)
			dump("null");		 //$NON-NLS-1$
		else
			dump(o.toString());
	}
	public static void dump(String msg){
		System.out.println("DUMP:" + msg); //$NON-NLS-1$
	}

	public static void dumpImports(ICompilationUnit cu) throws JavaModelException{
		IImportDeclaration[] imports= cu.getImports();
		if (imports == null)
			return;
		DebugUtils.dump("Compilation Unit: " + cu.getElementName());	 //$NON-NLS-1$
		for (int k= 0; k < imports.length; k ++){
			DebugUtils.dump("import " + imports[k].getElementName() + " on demand: " + imports[k].isOnDemand()); //$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	public static void dumpImports(IPackageFragment pack) throws JavaModelException{
		ICompilationUnit[] cus= pack.getCompilationUnits();
		if (cus == null)
			return;
		//DebugUtils.dump("Package " + pack.getElementName());
		for (int j= 0; j < cus.length; j++){
			dumpImports(cus[j]);
		}
	}

	public static void dumpImports(IJavaProject project) throws JavaModelException{
		IPackageFragment[] packages= project.getPackageFragments();
		if (packages == null)
			return;
		//DebugUtils.dump("Project " + project.getElementName());
		for (int i= 0; i < packages.length; i++){
			dumpImports(packages[i]);
		}
	}
}
