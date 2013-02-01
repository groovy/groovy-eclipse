/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - contribution for Bug 300576 - NPE Computing type hierarchy when compliance doesn't match libraries
 *******************************************************************************/
package org.eclipse.jdt.internal.core.hierarchy;
// GROOVY PATCHED

/**
 * This is the public entry point to resolve type hierarchies.
 *
 * When requesting additional types from the name environment, the resolver
 * accepts all forms (binary, source & compilation unit) for additional types.
 *
 * Side notes: Binary types already know their resolved supertypes so this
 * only makes sense for source types. Even though the compiler finds all binary
 * types to complete the hierarchy of a given source type, is there any reason
 * why the requestor should be informed that binary type X subclasses Y &
 * implements I & J?
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.util.ASTNodeFinder;
import org.eclipse.jdt.internal.core.util.HandleFactory;

public class HierarchyResolver implements ITypeRequestor {

	private ReferenceBinding focusType;
	private boolean superTypesOnly;
	private boolean hasMissingSuperClass;
	LookupEnvironment lookupEnvironment;
	private CompilerOptions options;
	HierarchyBuilder builder;
	private ReferenceBinding[] typeBindings;

	private int typeIndex;
	private IGenericType[] typeModels;

	private static final CompilationUnitDeclaration FakeUnit;
	static {
		IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.exitAfterAllProblems();
		ProblemReporter problemReporter = new ProblemReporter(policy, new CompilerOptions(), new DefaultProblemFactory());
		CompilationResult result = new CompilationResult(CharOperation.NO_CHAR, 0, 0, 0);
		FakeUnit = new CompilationUnitDeclaration(problemReporter, result, 0);
	}

public HierarchyResolver(INameEnvironment nameEnvironment, Map settings, HierarchyBuilder builder, IProblemFactory problemFactory) {
	// create a problem handler with the 'exit after all problems' handling policy
	this.options = new CompilerOptions(settings);
	IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.exitAfterAllProblems();
	ProblemReporter problemReporter = new ProblemReporter(policy, this.options, problemFactory);

	LookupEnvironment environment = new LookupEnvironment(this, this.options, problemReporter, nameEnvironment);
	environment.mayTolerateMissingType = true;
	setEnvironment(environment, builder);
}
public HierarchyResolver(LookupEnvironment lookupEnvironment, HierarchyBuilder builder) {
	setEnvironment(lookupEnvironment, builder);
}

/**
 * Add an additional binary type
 * @param binaryType
 * @param packageBinding
 */
public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
	IProgressMonitor progressMonitor = this.builder.hierarchy.progressMonitor;
	if (progressMonitor != null && progressMonitor.isCanceled())
		throw new OperationCanceledException();

	BinaryTypeBinding typeBinding = this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
	try {
		this.remember(binaryType, typeBinding);
	} catch (AbortCompilation e) {
		// ignore
	}
}

/**
 * Add an additional compilation unit.
 * @param sourceUnit
 */
public void accept(ICompilationUnit sourceUnit, AccessRestriction accessRestriction) {
	//System.out.println("Cannot accept compilation units inside the HierarchyResolver.");
	this.lookupEnvironment.problemReporter.abortDueToInternalError(
		new StringBuffer(Messages.accept_cannot)
			.append(sourceUnit.getFileName())
			.toString());
}

/**
 * Add additional source types
 * @param sourceTypes
 * @param packageBinding
 */
public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
	IProgressMonitor progressMonitor = this.builder.hierarchy.progressMonitor;
	if (progressMonitor != null && progressMonitor.isCanceled())
		throw new OperationCanceledException();

	// find most enclosing type first (needed when explicit askForType(...) is done
	// with a member type (e.g. p.A$B))
	ISourceType sourceType = sourceTypes[0];
	while (sourceType.getEnclosingType() != null)
		sourceType = sourceType.getEnclosingType();

	// build corresponding compilation unit
	CompilationResult result = new CompilationResult(sourceType.getFileName(), 1, 1, this.options.maxProblemsPerUnit);
	CompilationUnitDeclaration unit =
		SourceTypeConverter.buildCompilationUnit(
			new ISourceType[] {sourceType}, // ignore secondary types, to improve laziness
			SourceTypeConverter.MEMBER_TYPE, // need member types
			// no need for field initialization
			this.lookupEnvironment.problemReporter,
			result);

	// build bindings
	if (unit != null) {
		try {
			this.lookupEnvironment.buildTypeBindings(unit, accessRestriction);

			org.eclipse.jdt.core.ICompilationUnit cu = ((SourceTypeElementInfo)sourceType).getHandle().getCompilationUnit();
			rememberAllTypes(unit, cu, false);

			this.lookupEnvironment.completeTypeBindings(unit, true/*build constructor only*/);
		} catch (AbortCompilation e) {
			// missing 'java.lang' package: ignore
		}
	}
}
/*
 * Creates the super class handle of the given type.
 * Returns null if the type has no super class.
 * Adds the simple name to the hierarchy missing types if the class is not found and returns null.
 */
private IType findSuperClass(IGenericType type, ReferenceBinding typeBinding) {
	ReferenceBinding superBinding = typeBinding.superclass();

	if (superBinding != null) {
		superBinding = (ReferenceBinding) superBinding.erasure();
		if (typeBinding.isHierarchyInconsistent()) {
			if (superBinding.problemId() == ProblemReasons.NotFound) {
				this.hasMissingSuperClass = true;
				this.builder.hierarchy.missingTypes.add(new String(superBinding.sourceName)); // note: this could be Map$Entry
				return null;
			} else if ((superBinding.id == TypeIds.T_JavaLangObject)) {
				char[] superclassName;
				char separator;
				if (type instanceof IBinaryType) {
					superclassName = ((IBinaryType)type).getSuperclassName();
					separator = '/';
				} else if (type instanceof ISourceType) {
					superclassName = ((ISourceType)type).getSuperclassName();
					separator = '.';
				} else if (type instanceof HierarchyType) {
					superclassName = ((HierarchyType)type).superclassName;
					separator = '.';
				} else {
					return null;
				}

				if (superclassName != null) { // check whether subclass of Object due to broken hierarchy (as opposed to explicitly extending it)
					int lastSeparator = CharOperation.lastIndexOf(separator, superclassName);
					char[] simpleName = lastSeparator == -1 ? superclassName : CharOperation.subarray(superclassName, lastSeparator+1, superclassName.length);
					if (!CharOperation.equals(simpleName, TypeConstants.OBJECT)) {
						this.hasMissingSuperClass = true;
						this.builder.hierarchy.missingTypes.add(new String(simpleName));
						return null;
					}
				}
			}
		}
		for (int t = this.typeIndex; t >= 0; t--) {
			if (this.typeBindings[t] == superBinding) {
				return this.builder.getHandle(this.typeModels[t], superBinding);
			}
		}
	}
	return null;
}
/*
 * Returns the handles of the super interfaces of the given type.
 * Adds the simple name to the hierarchy missing types if an interface is not found (but don't put null in the returned array)
 */
private IType[] findSuperInterfaces(IGenericType type, ReferenceBinding typeBinding) {
	char[][] superInterfaceNames;
	char separator;
	if (type instanceof IBinaryType) {
		superInterfaceNames = ((IBinaryType)type).getInterfaceNames();
		separator = '/';
	} else if (type instanceof ISourceType) {
		ISourceType sourceType = (ISourceType)type;
		if (sourceType.getName().length == 0) { // if anonymous type
			if (typeBinding.superInterfaces() != null && typeBinding.superInterfaces().length > 0) {
				superInterfaceNames = new char[][] {sourceType.getSuperclassName()};
			} else {
				superInterfaceNames = sourceType.getInterfaceNames();
			}
		} else {
			if (TypeDeclaration.kind(sourceType.getModifiers()) == TypeDeclaration.ANNOTATION_TYPE_DECL)
				superInterfaceNames = new char[][] {TypeConstants.CharArray_JAVA_LANG_ANNOTATION_ANNOTATION};
			else
				superInterfaceNames = sourceType.getInterfaceNames();
		}
		separator = '.';
	} else if (type instanceof HierarchyType) {
		HierarchyType hierarchyType = (HierarchyType)type;
		if (hierarchyType.name.length == 0) { // if anonymous type
			if (typeBinding.superInterfaces() != null && typeBinding.superInterfaces().length > 0) {
				superInterfaceNames = new char[][] {hierarchyType.superclassName};
			} else {
				superInterfaceNames = hierarchyType.superInterfaceNames;
			}
		} else {
			superInterfaceNames = hierarchyType.superInterfaceNames;
		}
		separator = '.';
	} else{
		return null;
	}

	ReferenceBinding[] interfaceBindings = typeBinding.superInterfaces();
	int bindingIndex = 0;
	int bindingLength = interfaceBindings == null ? 0 : interfaceBindings.length;
	int length = superInterfaceNames == null ? 0 : superInterfaceNames.length;
	IType[] superinterfaces = new IType[length];
	int index = 0;
	next : for (int i = 0; i < length; i++) {
		char[] superInterfaceName = superInterfaceNames[i];
		int end = superInterfaceName.length;

		// find the end of simple name
		int genericStart = CharOperation.indexOf(Signature.C_GENERIC_START, superInterfaceName);
		if (genericStart != -1) end = genericStart;

		// find the start of simple name
		int lastSeparator = CharOperation.lastIndexOf(separator, superInterfaceName, 0, end);
		int start = lastSeparator + 1;

		// case of binary inner type -> take the last part
		int lastDollar = CharOperation.lastIndexOf('$', superInterfaceName, start);
		if (lastDollar != -1) start = lastDollar + 1;

		char[] simpleName = CharOperation.subarray(superInterfaceName, start, end);

		if (bindingIndex < bindingLength) {
			ReferenceBinding interfaceBinding = (ReferenceBinding) interfaceBindings[bindingIndex].erasure();

			// ensure that the binding corresponds to the interface defined by the user
			if (CharOperation.equals(simpleName, interfaceBinding.sourceName)) {
				bindingIndex++;
				for (int t = this.typeIndex; t >= 0; t--) {
					if (this.typeBindings[t] == interfaceBinding) {
						IType handle = this.builder.getHandle(this.typeModels[t], interfaceBinding);
						if (handle != null) {
							superinterfaces[index++] = handle;
							continue next;
						}
					}
				}
			}
		}
		this.builder.hierarchy.missingTypes.add(new String(simpleName));
	}
	if (index != length)
		System.arraycopy(superinterfaces, 0, superinterfaces = new IType[index], 0, index);
	return superinterfaces;
}
/*
 * For all type bindings that have hierarchy problems, artificially fix their superclass/superInterfaces so that the connection
 * can be made.
 */
private void fixSupertypeBindings() {
	for (int current = this.typeIndex; current >= 0; current--) {
		ReferenceBinding typeBinding = this.typeBindings[current];
		if ((typeBinding.tagBits & TagBits.HierarchyHasProblems) == 0)
			continue;

		if (typeBinding instanceof SourceTypeBinding) {
			if (typeBinding instanceof LocalTypeBinding) {
				LocalTypeBinding localTypeBinding = (LocalTypeBinding) typeBinding;
				QualifiedAllocationExpression allocationExpression = localTypeBinding.scope.referenceContext.allocation;
				TypeReference type;
				if (allocationExpression != null && (type = allocationExpression.type) != null && type.resolvedType != null) {
					localTypeBinding.superclass = (ReferenceBinding) type.resolvedType;
					continue;
				}
			}
			ClassScope scope = ((SourceTypeBinding) typeBinding).scope;
			if (scope != null) {
				TypeDeclaration typeDeclaration = scope.referenceContext;
				TypeReference superclassRef = typeDeclaration == null ? null : typeDeclaration.superclass;
				TypeBinding superclass = superclassRef == null ? null : superclassRef.resolvedType;
				if (superclass != null) {
					superclass = superclass.closestMatch();
				}
				if (superclass instanceof ReferenceBinding) {
					// ensure we are not creating a cycle (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=215681 )
					if (!(subTypeOfType((ReferenceBinding) superclass, typeBinding))) {
						((SourceTypeBinding) typeBinding).superclass = (ReferenceBinding) superclass;
					}
				}

				TypeReference[] superInterfaces = typeDeclaration == null ? null : typeDeclaration.superInterfaces;
				int length;
				ReferenceBinding[] interfaceBindings = typeBinding.superInterfaces();
				if (superInterfaces != null && (length = superInterfaces.length) > (interfaceBindings == null ? 0 : interfaceBindings.length)) { // check for interfaceBindings being null (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=139689)
					interfaceBindings = new ReferenceBinding[length];
					int index = 0;
					for (int i = 0; i < length; i++) {
						TypeBinding superInterface = superInterfaces[i].resolvedType;
						if (superInterface != null) {
							superInterface = superInterface.closestMatch();
						}
						if (superInterface instanceof ReferenceBinding) {
							// ensure we are not creating a cycle (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=215681 )
							if (!(subTypeOfType((ReferenceBinding) superInterface, typeBinding))) {
								interfaceBindings[index++] = (ReferenceBinding) superInterface;
							}
						}
					}
					if (index < length)
						System.arraycopy(interfaceBindings, 0, interfaceBindings = new ReferenceBinding[index], 0 , index);
					((SourceTypeBinding) typeBinding).superInterfaces = interfaceBindings;
				}
			}
		} else if (typeBinding instanceof BinaryTypeBinding) {
			try {
				typeBinding.superclass();
			} catch (AbortCompilation e) {
				// allow subsequent call to superclass() to succeed so that we don't have to catch AbortCompilation everywhere
				((BinaryTypeBinding) typeBinding).tagBits &= ~TagBits.HasUnresolvedSuperclass;
				this.builder.hierarchy.missingTypes.add(new String(typeBinding.superclass().sourceName()));
				this.hasMissingSuperClass = true;
			}
			try {
				typeBinding.superInterfaces();
			} catch (AbortCompilation e) {
				// allow subsequent call to superInterfaces() to succeed so that we don't have to catch AbortCompilation everywhere
				((BinaryTypeBinding) typeBinding).tagBits &= ~TagBits.HasUnresolvedSuperinterfaces;
			}
		}
	}
}
private void remember(IGenericType suppliedType, ReferenceBinding typeBinding) {
	if (typeBinding == null) return;

	if (++this.typeIndex == this.typeModels.length) {
		System.arraycopy(this.typeModels, 0, this.typeModels = new IGenericType[this.typeIndex * 2], 0, this.typeIndex);
		System.arraycopy(this.typeBindings, 0, this.typeBindings = new ReferenceBinding[this.typeIndex * 2], 0, this.typeIndex);
	}
	this.typeModels[this.typeIndex] = suppliedType;
	this.typeBindings[this.typeIndex] = typeBinding;
}
private void remember(IType type, ReferenceBinding typeBinding) {
	if (((CompilationUnit)type.getCompilationUnit()).isOpen()) {
		try {
			IGenericType genericType = (IGenericType)((JavaElement)type).getElementInfo();
			remember(genericType, typeBinding);
		} catch (JavaModelException e) {
			// cannot happen since element is open
			return;
		}
	} else {
		if (typeBinding == null) return;

		TypeDeclaration typeDeclaration = ((SourceTypeBinding)typeBinding).scope.referenceType();

		// simple super class name
		char[] superclassName = null;
		TypeReference superclass;
		if ((typeDeclaration.bits & ASTNode.IsAnonymousType) != 0) {
			superclass = typeDeclaration.allocation.type;
		} else {
			superclass = typeDeclaration.superclass;
		}
		if (superclass != null) {
			char[][] typeName = superclass.getTypeName();
			superclassName = typeName == null ? null : typeName[typeName.length-1];
		}

		// simple super interface names
		char[][] superInterfaceNames = null;
		TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			int length = superInterfaces.length;
			superInterfaceNames = new char[length][];
			for (int i = 0; i < length; i++) {
				TypeReference superInterface = superInterfaces[i];
				char[][] typeName = superInterface.getTypeName();
				superInterfaceNames[i] = typeName[typeName.length-1];
			}
		}

		HierarchyType hierarchyType = new HierarchyType(
			type,
			typeDeclaration.name,
			typeDeclaration.binding.modifiers,
			superclassName,
			superInterfaceNames);
		remember(hierarchyType, typeDeclaration.binding);
	}

}
/*
 * Remembers all type bindings defined in the given parsed unit, adding local/anonymous types if specified.
 */
private void rememberAllTypes(CompilationUnitDeclaration parsedUnit, org.eclipse.jdt.core.ICompilationUnit cu, boolean includeLocalTypes) {
	TypeDeclaration[] types = parsedUnit.types;
	if (types != null) {
		for (int i = 0, length = types.length; i < length; i++) {
			TypeDeclaration type = types[i];
			rememberWithMemberTypes(type, cu.getType(new String(type.name)));
		}
	}
	if (includeLocalTypes && parsedUnit.localTypes != null) {
		HandleFactory factory = new HandleFactory();
		HashSet existingElements = new HashSet(parsedUnit.localTypeCount);
		HashMap knownScopes = new HashMap(parsedUnit.localTypeCount);
		for (int i = 0; i < parsedUnit.localTypeCount; i++) {
			LocalTypeBinding localType = parsedUnit.localTypes[i];
			ClassScope classScope = localType.scope;
			TypeDeclaration typeDecl = classScope.referenceType();
			IType typeHandle = (IType)factory.createElement(classScope, cu, existingElements, knownScopes);
			rememberWithMemberTypes(typeDecl, typeHandle);
		}
	}
}
private void rememberWithMemberTypes(TypeDeclaration typeDecl, IType typeHandle) {
	remember(typeHandle, typeDecl.binding);

	TypeDeclaration[] memberTypes = typeDecl.memberTypes;
	if (memberTypes != null) {
		for (int i = 0, length = memberTypes.length; i < length; i++) {
			TypeDeclaration memberType = memberTypes[i];
			rememberWithMemberTypes(memberType, typeHandle.getType(new String(memberType.name)));
		}
	}
}
/*
 * Reports the hierarchy from the remembered bindings.
 * Note that 'binaryTypeBinding' is null if focus type is a source type.
 */
private void reportHierarchy(IType focus, TypeDeclaration focusLocalType, ReferenceBinding binaryTypeBinding) {

	// set focus type binding
	if (focus != null) {
		if (binaryTypeBinding != null) {
			// binary type
			this.focusType = binaryTypeBinding;
		} else {
			// source type
			if (focusLocalType != null) {
				// anonymous or local type
				this.focusType = focusLocalType.binding;
			} else {
				// top level or member type
				char[] fullyQualifiedName = focus.getFullyQualifiedName().toCharArray();
				setFocusType(CharOperation.splitOn('.', fullyQualifiedName));
			}
		}
	}

	// be resilient and fix super type bindings
	fixSupertypeBindings();

	int objectIndex = -1;
	IProgressMonitor progressMonitor = this.builder.hierarchy.progressMonitor;
	for (int current = this.typeIndex; current >= 0; current--) {
		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();
		
		ReferenceBinding typeBinding = this.typeBindings[current];

		// java.lang.Object treated at the end
		if (typeBinding.id == TypeIds.T_JavaLangObject) {
			objectIndex = current;
			continue;
		}

		IGenericType suppliedType = this.typeModels[current];

		if (!subOrSuperOfFocus(typeBinding)) {
			continue; // ignore types outside of hierarchy
		}

		IType superclass;
		if (typeBinding.isInterface()){ // do not connect interfaces to Object
			superclass = null;
		} else {
			superclass = findSuperClass(suppliedType, typeBinding);
		}
		IType[] superinterfaces = findSuperInterfaces(suppliedType, typeBinding);

		this.builder.connect(suppliedType, this.builder.getHandle(suppliedType, typeBinding), superclass, superinterfaces);
	}
	// add java.lang.Object only if the super class is not missing
	if (objectIndex > -1 && (!this.hasMissingSuperClass || this.focusType == null)) {
		IGenericType objectType = this.typeModels[objectIndex];
		this.builder.connect(objectType, this.builder.getHandle(objectType, this.typeBindings[objectIndex]), null, null);
	}
}
private void reset(){
	this.lookupEnvironment.reset();

	this.focusType = null;
	this.superTypesOnly = false;
	this.typeIndex = -1;
	this.typeModels = new IGenericType[5];
	this.typeBindings = new ReferenceBinding[5];
}

/**
 * Resolve the supertypes for the supplied source type.
 * Inform the requestor of the resolved supertypes using:
 *    connect(ISourceType suppliedType, IGenericType superclass, IGenericType[] superinterfaces)
 * @param suppliedType
 */
public void resolve(IGenericType suppliedType) {
	try {
		if (suppliedType.isBinaryType()) {
			BinaryTypeBinding binaryTypeBinding = this.lookupEnvironment.cacheBinaryType((IBinaryType) suppliedType, false/*don't need field and method (bug 125067)*/, null /*no access restriction*/);
			remember(suppliedType, binaryTypeBinding);
			// We still need to add superclasses and superinterfaces bindings (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=53095)
			int startIndex = this.typeIndex;
			for (int i = startIndex; i <= this.typeIndex; i++) {
				IGenericType igType = this.typeModels[i];
				if (igType != null && igType.isBinaryType()) {
					CompilationUnitDeclaration previousUnitBeingCompleted = this.lookupEnvironment.unitBeingCompleted;
					// fault in its hierarchy...
					try {
						// ensure that unitBeingCompleted is set so that we don't get an AbortCompilation for a missing type
						// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=213249 )
						if (previousUnitBeingCompleted == null) {
							this.lookupEnvironment.unitBeingCompleted = FakeUnit;
						}
						ReferenceBinding typeBinding = this.typeBindings[i];
						typeBinding.superclass();
						typeBinding.superInterfaces();
					} catch (AbortCompilation e) {
						// classpath problem for this type: ignore
					} finally {
						this.lookupEnvironment.unitBeingCompleted = previousUnitBeingCompleted;
					}
				}
			}
			this.superTypesOnly = true;
			reportHierarchy(this.builder.getType(), null, binaryTypeBinding);
		} else {
			org.eclipse.jdt.core.ICompilationUnit cu = ((SourceTypeElementInfo)suppliedType).getHandle().getCompilationUnit();
			HashSet localTypes = new HashSet();
			localTypes.add(cu.getPath().toString());
			this.superTypesOnly = true;
			resolve(new Openable[] {(Openable)cu}, localTypes, null);
		}
	} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
	} finally {
		reset();
	}
}

/**
 * Resolve the supertypes for the types contained in the given openables (ICompilationUnits and/or IClassFiles).
 * Inform the requestor of the resolved supertypes for each
 * supplied source type using:
 *    connect(ISourceType suppliedType, IGenericType superclass, IGenericType[] superinterfaces)
 *
 * Also inform the requestor of the supertypes of each
 * additional requested super type which is also a source type
 * instead of a binary type.
 * @param openables
 * @param localTypes
 * @param monitor
 */
public void resolve(Openable[] openables, HashSet localTypes, IProgressMonitor monitor) {
	try {
		int openablesLength = openables.length;
		CompilationUnitDeclaration[] parsedUnits = new CompilationUnitDeclaration[openablesLength];
		boolean[] hasLocalType = new boolean[openablesLength];
		org.eclipse.jdt.core.ICompilationUnit[] cus = new org.eclipse.jdt.core.ICompilationUnit[openablesLength];
		int unitsIndex = 0;

		CompilationUnitDeclaration focusUnit = null;
		ReferenceBinding focusBinaryBinding = null;
		IType focus = this.builder.getType();
		Openable focusOpenable = null;
		if (focus != null) {
			if (focus.isBinary()) {
				focusOpenable = (Openable)focus.getClassFile();
			} else {
				focusOpenable = (Openable)focus.getCompilationUnit();
			}
		}

		// build type bindings
		
		// GROOVY start: ensure downstream groovy parses share the same compilationunit
		/* old {
		Parser parser = new Parser(this.lookupEnvironment.problemReporter, true);
		} new */
		Parser parser = LanguageSupportFactory.getParser(this, this.lookupEnvironment.globalOptions, this.lookupEnvironment.problemReporter, true, 1);
		// GROOVY end
		for (int i = 0; i < openablesLength; i++) {
			Openable openable = openables[i];
			if (openable instanceof org.eclipse.jdt.core.ICompilationUnit) {
				org.eclipse.jdt.core.ICompilationUnit cu = (org.eclipse.jdt.core.ICompilationUnit)openable;

				// contains a potential subtype as a local or anonymous type?
				boolean containsLocalType = false;
				if (localTypes == null) { // case of hierarchy on region
					containsLocalType = true;
				} else {
					IPath path = cu.getPath();
					containsLocalType = localTypes.contains(path.toString());
				}

				// build parsed unit
				CompilationUnitDeclaration parsedUnit = null;
				if (cu.isOpen()) {
					// create parsed unit from source element infos
					CompilationResult result = new CompilationResult((ICompilationUnit)cu, i, openablesLength, this.options.maxProblemsPerUnit);
					SourceTypeElementInfo[] typeInfos = null;
					try {
						IType[] topLevelTypes = cu.getTypes();
						int topLevelLength = topLevelTypes.length;
						if (topLevelLength == 0) continue; // empty cu: no need to parse (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=65677)
						typeInfos = new SourceTypeElementInfo[topLevelLength];
						for (int j = 0; j < topLevelLength; j++) {
							IType topLevelType = topLevelTypes[j];
							typeInfos[j] = (SourceTypeElementInfo)((JavaElement)topLevelType).getElementInfo();
						}
					} catch (JavaModelException e) {
						// types/cu exist since cu is opened
					}
					int flags = !containsLocalType
						? SourceTypeConverter.MEMBER_TYPE
						: SourceTypeConverter.FIELD_AND_METHOD | SourceTypeConverter.MEMBER_TYPE | SourceTypeConverter.LOCAL_TYPE;
					parsedUnit =
						SourceTypeConverter.buildCompilationUnit(
							typeInfos,
							flags,
							this.lookupEnvironment.problemReporter,
							result);
					
					// We would have got all the necessary local types by now and hence there is no further need 
					// to parse the method bodies. Parser.getMethodBodies, which is called latter in this function, 
					// will not parse the method statements if ASTNode.HasAllMethodBodies is set. 
					if (containsLocalType) 	parsedUnit.bits |= ASTNode.HasAllMethodBodies;
				} else {
					// create parsed unit from file
					IFile file = (IFile) cu.getResource();
					ICompilationUnit sourceUnit = this.builder.createCompilationUnitFromPath(openable, file);

					CompilationResult unitResult = new CompilationResult(sourceUnit, i, openablesLength, this.options.maxProblemsPerUnit);
					parsedUnit = parser.dietParse(sourceUnit, unitResult);
				}

				if (parsedUnit != null) {
					hasLocalType[unitsIndex] = containsLocalType;
					cus[unitsIndex] = cu;
					parsedUnits[unitsIndex++] = parsedUnit;
					try {
						this.lookupEnvironment.buildTypeBindings(parsedUnit, null /*no access restriction*/);
						if (openable.equals(focusOpenable)) {
							focusUnit = parsedUnit;
						}
					} catch (AbortCompilation e) {
						// classpath problem for this type: ignore
					}
				}
			} else {
				// cache binary type binding
				ClassFile classFile = (ClassFile)openable;
				IBinaryType binaryType = (IBinaryType) JavaModelManager.getJavaModelManager().getInfo(classFile.getType());
				if (binaryType == null) {
					// create binary type from file
					if (classFile.getPackageFragmentRoot().isArchive()) {
						binaryType = this.builder.createInfoFromClassFileInJar(classFile);
					} else {
						IResource file = classFile.resource();
						binaryType = this.builder.createInfoFromClassFile(classFile, file);
					}
				}
				if (binaryType != null) {
					try {
						BinaryTypeBinding binaryTypeBinding = this.lookupEnvironment.cacheBinaryType(binaryType, false/*don't need field and method (bug 125067)*/, null /*no access restriction*/);
						remember(binaryType, binaryTypeBinding);
						if (openable.equals(focusOpenable)) {
							focusBinaryBinding = binaryTypeBinding;
						}
					} catch (AbortCompilation e) {
						// classpath problem for this type: ignore
					}
				}
			}
		}

		// remember type declaration of focus if local/anonymous early (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=210498)
		TypeDeclaration focusLocalType = null;
		if (focus != null && focusBinaryBinding == null && focusUnit != null && ((Member)focus).getOuterMostLocalContext() != null) {
			focusLocalType = new ASTNodeFinder(focusUnit).findType(focus);
		}


		for (int i = 0; i <= this.typeIndex; i++) {
			IGenericType suppliedType = this.typeModels[i];
			if (suppliedType != null && suppliedType.isBinaryType()) {
				CompilationUnitDeclaration previousUnitBeingCompleted = this.lookupEnvironment.unitBeingCompleted;
				// fault in its hierarchy...
				try {
					// ensure that unitBeingCompleted is set so that we don't get an AbortCompilation for a missing type
					// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=213249 )
					if (previousUnitBeingCompleted == null) {
						this.lookupEnvironment.unitBeingCompleted = FakeUnit;
					}
					ReferenceBinding typeBinding = this.typeBindings[i];
					typeBinding.superclass();
					typeBinding.superInterfaces();
				} catch (AbortCompilation e) {
					// classpath problem for this type: ignore
				} finally {
					this.lookupEnvironment.unitBeingCompleted = previousUnitBeingCompleted;
				}
			}
		}

		// complete type bindings (i.e. connect super types)
		for (int i = 0; i < unitsIndex; i++) {
			CompilationUnitDeclaration parsedUnit = parsedUnits[i];
			if (parsedUnit != null) {
				try {
					if (hasLocalType[i]) { // NB: no-op if method bodies have been already parsed
						if (monitor != null && monitor.isCanceled())
							throw new OperationCanceledException();
						parser.getMethodBodies(parsedUnit);
					}
				} catch (AbortCompilation e) {
					// classpath problem for this type: don't try to resolve (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=49809)
					hasLocalType[i] = false;
				}
			}
		}
		// complete type bindings and build fields and methods only for local types
		// (in this case the constructor is needed when resolving local types)
		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=145333)
		try {
			this.lookupEnvironment.completeTypeBindings(parsedUnits, hasLocalType, unitsIndex);
		// remember type bindings
		for (int i = 0; i < unitsIndex; i++) {
			CompilationUnitDeclaration parsedUnit = parsedUnits[i];
				if (parsedUnit != null && !parsedUnit.hasErrors()) {
				boolean containsLocalType = hasLocalType[i];
				if (containsLocalType) {
					if (monitor != null && monitor.isCanceled())
						throw new OperationCanceledException();
					parsedUnit.scope.faultInTypes();
					parsedUnit.resolve();
				}

				rememberAllTypes(parsedUnit, cus[i], containsLocalType);
			}
		}
		} catch (AbortCompilation e) {
			// skip it silently
		}
		worked(monitor, 1);


		// if no potential subtype was a real subtype of the binary focus type, no need to go further
		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=54043)
		if (focusBinaryBinding == null && focus != null && focus.isBinary()) {
			char[] fullyQualifiedName = focus.getFullyQualifiedName().toCharArray();
			focusBinaryBinding = this.lookupEnvironment.getCachedType(CharOperation.splitOn('.', fullyQualifiedName));
			if (focusBinaryBinding == null)
				return;
		}

		reportHierarchy(focus, focusLocalType, focusBinaryBinding);

	} catch (ClassCastException e){ // work-around for 1GF5W1S - can happen in case duplicates are fed to the hierarchy with binaries hiding sources
	} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
		if (TypeHierarchy.DEBUG)
			e.printStackTrace();
	} finally {
		reset();
	}
}
private void setEnvironment(LookupEnvironment lookupEnvironment, HierarchyBuilder builder) {
	this.lookupEnvironment = lookupEnvironment;
	this.builder = builder;

	this.typeIndex = -1;
	this.typeModels = new IGenericType[5];
	this.typeBindings = new ReferenceBinding[5];
}

/*
 * Set the focus type (i.e. the type that this resolver is computing the hierarch for.
 * Returns the binding of this focus type or null if it could not be found.
 */
public ReferenceBinding setFocusType(char[][] compoundName) {
	if (compoundName == null || this.lookupEnvironment == null) return null;
	this.focusType = this.lookupEnvironment.getCachedType(compoundName);
	if (this.focusType == null) {
		this.focusType = this.lookupEnvironment.askForType(compoundName);
		if (this.focusType == null) {
			int length = compoundName.length;
			char[] typeName = compoundName[length-1];
			int firstDollar = CharOperation.indexOf('$', typeName);
			if (firstDollar != -1) {
				compoundName[length-1] = CharOperation.subarray(typeName, 0, firstDollar);
				this.focusType = this.lookupEnvironment.askForType(compoundName);
				if (this.focusType != null) {
					char[][] memberTypeNames = CharOperation.splitOn('$', typeName, firstDollar+1, typeName.length);
					for (int i = 0; i < memberTypeNames.length; i++) {
						this.focusType = this.focusType.getMemberType(memberTypeNames[i]);
						if (this.focusType == null)
							return null;
					}
				}
			}
		}
	}
	return this.focusType;
}
public boolean subOrSuperOfFocus(ReferenceBinding typeBinding) {
	if (this.focusType == null) return true; // accept all types (case of hierarchy in a region)
	try {
		if (subTypeOfType(this.focusType, typeBinding)) return true;
		if (!this.superTypesOnly && subTypeOfType(typeBinding, this.focusType)) return true;
	} catch (AbortCompilation e) {
		// unresolved superclass/superinterface -> ignore
	}
	return false;
}
private boolean subTypeOfType(ReferenceBinding subType, ReferenceBinding typeBinding) {
	if (typeBinding == null || subType == null) return false;
	if (subType == typeBinding) return true;
	ReferenceBinding superclass = subType.superclass();
	if (superclass != null) superclass = (ReferenceBinding) superclass.erasure();
//	if (superclass != null && superclass.id == TypeIds.T_JavaLangObject && subType.isHierarchyInconsistent()) return false;
	if (subTypeOfType(superclass, typeBinding)) return true;
	ReferenceBinding[] superInterfaces = subType.superInterfaces();
	if (superInterfaces != null) {
		for (int i = 0, length = superInterfaces.length; i < length; i++) {
			ReferenceBinding superInterface = (ReferenceBinding) superInterfaces[i].erasure();
			if (subTypeOfType(superInterface, typeBinding)) return true;
		}
	}
	return false;
}
protected void worked(IProgressMonitor monitor, int work) {
	if (monitor != null) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		} else {
			monitor.worked(work);
		}
	}
}
}
