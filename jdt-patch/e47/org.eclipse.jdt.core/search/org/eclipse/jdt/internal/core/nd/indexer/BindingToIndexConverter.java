/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.indexer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdTreeNode;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeId;
import org.eclipse.jdt.internal.core.nd.java.NdTypeInterface;

public class BindingToIndexConverter {
	private static final boolean ENABLE_LOGGING = false;
	private JavaIndex index;
	private NdResourceFile resource;

	public BindingToIndexConverter(NdResourceFile resource) {
		this.resource = resource;
		this.index = JavaIndex.getIndex(resource.getNd());
	}

	public void addBinding(NdTreeNode parent, IBinding binding, IProgressMonitor monitor) {
		switch (binding.getKind()) {
			case IBinding.TYPE:
				addType((ITypeBinding) binding, monitor);
				break;
			case IBinding.ANNOTATION:
				addAnnotation(parent, (IAnnotationBinding) binding, monitor);
				break;
			case IBinding.METHOD:
				addMethod(parent, (IMethodBinding) binding, monitor);
				break;
			case IBinding.VARIABLE:
				addVariable(parent, (IVariableBinding) binding, monitor);
				break;
			case IBinding.PACKAGE:
				addPackage(parent, (IPackageBinding) binding, monitor);
				break;
			case IBinding.MEMBER_VALUE_PAIR:
				addMemberValuePair(parent, (IMemberValuePairBinding) binding, monitor);
				break;
			default:
				Package.log("Encountered unknown binding type: " + binding.getKind(), null); //$NON-NLS-1$
		}
	}

	public void addMemberValuePair(NdTreeNode parent, IMemberValuePairBinding binding, IProgressMonitor monitor) {
		logInfo("Adding member value pair: " + binding.getName()); //$NON-NLS-1$
	}

	public void addPackage(NdTreeNode parent, IPackageBinding binding, IProgressMonitor monitor) {
		logInfo("Adding package: " + binding.getName()); //$NON-NLS-1$
	}

	public void addVariable(NdTreeNode parent, IVariableBinding binding, IProgressMonitor monitor) {
		logInfo("Adding variable: " + binding.getName()); //$NON-NLS-1$
	}

	public void addMethod(NdTreeNode parent, IMethodBinding binding, IProgressMonitor monitor) {
		logInfo("Adding method: " + binding.getName()); //$NON-NLS-1$
	}

	public void addAnnotation(NdTreeNode parent, IAnnotationBinding binding, IProgressMonitor monitor) {
		logInfo("Adding annotation: " + binding.getName()); //$NON-NLS-1$
	}

	public NdType addType(ITypeBinding binding, IProgressMonitor monitor) {
		logInfo("Adding type: " + binding.getBinaryName()); //$NON-NLS-1$

		NdTypeId name = makeTypeId(binding);
		NdType type = name.findTypeByResourceAddress(this.resource.address);

		if (type == null) {
			type = new NdType(getNd(), this.resource);
		}

		type.setTypeId(name);

		ITypeBinding superclass = binding.getSuperclass();

		if (superclass != null) {
			type.setSuperclass(makeTypeId(superclass));
		}

		for (ITypeBinding next : binding.getInterfaces()) {
			new NdTypeInterface(getNd(), type, makeTypeId(next));
		}

		return type;
	}

	private void logInfo(String string) {
		if (ENABLE_LOGGING) {
			Package.logInfo(string);
		}
	}

	private NdTypeId makeTypeId(ITypeBinding forBinding) {
		return this.index.createTypeId(JavaNames.binaryNameToFieldDescriptor(forBinding.getBinaryName().toCharArray()));
	}

	private Nd getNd() {
		return this.resource.getNd();
	}
}
