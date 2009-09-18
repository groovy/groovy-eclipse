/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement     - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.builder.AbortIncrementalBuildException;

/**
 * A subtype of CompilationUnitScope that allows us to override some methods and prevent JDT doing some checks that groovy will be
 * doing anyway (or that JDT should be prevented from doing on groovy type declarations)
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class GroovyCompilationUnitScope extends CompilationUnitScope {

	// FIXASC (M2) determine if these caches are worth the effort?
	// Caches of typenames (eg. java.foo.Bar) to ClassNodes. This is a cache of types that can be correctly seen from this scope
	// private Map<String, JDTClassNode> jdtSourceBindingCache = new HashMap<String, JDTClassNode>();
	// private Map<String, JDTClassNode> jdtBinaryBindingCache = new HashMap<String, JDTClassNode>();

	// Matches ResolveVisitor - these are the additional automatic imports for groovy files
	private static final char[][] javaIo;
	private static final char[][] javaNet;
	private static final char[][] javaUtil;
	private static final char[][] groovyLang;
	private static final char[][] groovyUtil;

	private static final char[][] javaMathBigDecimal;
	private static final char[][] javaMathBigInteger;

	static {
		javaIo = CharOperation.splitOn('.', "java.io".toCharArray());
		javaNet = CharOperation.splitOn('.', "java.net".toCharArray());
		javaUtil = CharOperation.splitOn('.', "java.util".toCharArray());
		groovyLang = CharOperation.splitOn('.', "groovy.lang".toCharArray());
		groovyUtil = CharOperation.splitOn('.', "groovy.util".toCharArray());

		javaMathBigDecimal = CharOperation.splitOn('.', "java.math.BigDecimal".toCharArray());
		javaMathBigInteger = CharOperation.splitOn('.', "java.math.BigInteger".toCharArray());
	}

	private final static char[] GROOVY = "groovy".toCharArray(); //$NON-NLS-1$
	private final static char[] LANG = "lang".toCharArray(); //$NON-NLS-1$
	private final static char[][] GROOVY_LANG_GROOVYOBJECT = { GROOVY, LANG, "GroovyObject".toCharArray() }; //$NON-NLS-1$

	public GroovyCompilationUnitScope(GroovyCompilationUnitDeclaration compilationUnitDeclaration,
			LookupEnvironment lookupEnvironment) {
		super(compilationUnitDeclaration, lookupEnvironment);
	}

	@Override
	protected ImportBinding[] getDefaultImports() {
		ImportBinding[] defaultImports = super.getDefaultImports(); // picks up 'java.lang'

		List<ImportBinding> importBindings = new ArrayList<ImportBinding>();

		importBindings.add(defaultImports[0]);

		// augment with the groovy additional automatic imports
		Binding importBinding = environment.createPackage(javaIo);
		importBindings.add(new ImportBinding(javaIo, true, importBinding, null));

		importBinding = environment.createPackage(javaNet);
		importBindings.add(new ImportBinding(javaNet, true, importBinding, null));

		importBinding = environment.createPackage(javaUtil);
		importBindings.add(new ImportBinding(javaUtil, true, importBinding, null));

		importBinding = environment.createPackage(groovyLang);
		importBindings.add(new ImportBinding(groovyLang, true, importBinding, null));

		importBinding = environment.createPackage(groovyUtil);
		importBindings.add(new ImportBinding(groovyUtil, true, importBinding, null));

		// And specific imports for BigDecimal and BigInteger
		Binding jmBigDecimal = environment.getType(javaMathBigDecimal);
		importBindings.add(new ImportBinding(javaMathBigDecimal, false, jmBigDecimal, null));

		Binding jmBigInteger = environment.getType(javaMathBigInteger);
		importBindings.add(new ImportBinding(javaMathBigInteger, false, jmBigInteger, null));
		return importBindings.toArray(new ImportBinding[importBindings.size()]);
	}

	@Override
	protected ClassScope buildClassScope(Scope parent, TypeDeclaration typeDecl) {
		return new GroovyClassScope(parent, typeDecl);
	}

	/**
	 * Called after the type hierarchy is built for all types involved - allows glObject to be inserted
	 */
	@Override
	public void augmentTypeHierarchy() {
		for (int i = 0, length = topLevelTypes.length; i < length; i++) {
			augmentTypeHierarchy(topLevelTypes[i]);
		}
	}

	// FIXASC (M2) move this into GroovyClassScope
	/**
	 * Ensure Groovy types extend groovy.lang.GroovyObject
	 */
	private void augmentTypeHierarchy(SourceTypeBinding typeBinding) {
		// FIXASC (M2) others to exclude here?
		if (typeBinding.isAnnotationType() || typeBinding.isInterface()) {
			return;
		}
		ReferenceBinding groovyLangObjectBinding = getGroovyLangObjectBinding();
		if (!typeBinding.implementsInterface(groovyLangObjectBinding, true)) {
			ReferenceBinding[] superInterfaceBindings = typeBinding.superInterfaces;
			int count = superInterfaceBindings.length;
			System.arraycopy(superInterfaceBindings, 0, superInterfaceBindings = new ReferenceBinding[count + 1], 0, count);
			superInterfaceBindings[count] = groovyLangObjectBinding;
			typeBinding.superInterfaces = superInterfaceBindings;
		}
	}

	private final ReferenceBinding getGroovyLangObjectBinding() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(GROOVY_LANG_GROOVYOBJECT);
		return unitScope.environment.getResolvedType(GROOVY_LANG_GROOVYOBJECT, this);
	}

	/**
	 * Look in the local cache, if we don't find it then ask JDT. If JDT responds with a SourceTypeBinding then it has been found.
	 * If JDT responds with some other kind of binding, we consider that 'not found as source' and return null.
	 * 
	 */
	// FIXASC (M2:optimization) cache any non SourceTypeBinding found and use that information in the lookupClassNodeForBinary
	public ClassNode lookupClassNodeForSource(String typename, JDTResolver jdtResolver) {
		// ClassNode node = jdtSourceBindingCache.get(typename);
		// if (node != null) {
		// return node;
		// }
		char[][] compoundName = CharOperation.splitOn('.', typename.toCharArray());
		TypeBinding jdtBinding = null;
		try {
			jdtBinding = getType(compoundName, compoundName.length);
		} catch (AbortCompilation t) {
			if (t.silentException instanceof AbortIncrementalBuildException) {
				jdtBinding = null;
			} else {
				throw t;
			}
		}

		if (jdtBinding != null && (jdtBinding instanceof SourceTypeBinding)) {
			// log("GCUScope.lookupClassNodeForSource: JDTBinding for '" + typename + "' found to be "
			// + jdtBinding.getClass().getSimpleName());
			ClassNode newNode = jdtResolver.convertToClassNode(jdtBinding);
			// if (newNode != null) {
			// jdtSourceBindingCache.put(typename, newNode);
			// }
			return newNode;
		}
		// FIXASC (RC1) better to look it up properly as a member type rather than catch the problem and unwrap!
		// FIXASC (RC1) make sure enough thinking has gone into verifying this is reasonable.
		// FIXASC (RC1) think about renaming the method due to it now using BTBs
		// on an incremental build we see BTBs for what we previously saw STBs
		if (jdtBinding != null && (jdtBinding instanceof BinaryTypeBinding)) {
			ClassNode newNode = jdtResolver.convertToClassNode(jdtBinding);
			return newNode;
		}

		if (jdtBinding != null && (jdtBinding instanceof ProblemReferenceBinding)) {
			ProblemReferenceBinding prBinding = (ProblemReferenceBinding) jdtBinding;
			if (prBinding.problemId() == ProblemReasons.InternalNameProvided) {
				jdtBinding = prBinding.closestMatch();
				// FIXASC (M2) caching for this too
				if (jdtBinding != null && (jdtBinding instanceof SourceTypeBinding)) {
					return jdtResolver.convertToClassNode(jdtBinding);
				}
				if (jdtBinding != null && (jdtBinding instanceof BinaryTypeBinding)) {
					return jdtResolver.convertToClassNode(jdtBinding);
				}
			}
		}
		return null;
	}

	public ClassNode lookupClassNodeForBinary(String typename, JDTResolver jdtResolver) {
		// ClassNode node = jdtBinaryBindingCache.get(typename);
		// if (node != null) {
		// return node;
		// }
		char[][] compoundName = CharOperation.splitOn('.', typename.toCharArray());
		TypeBinding jdtBinding = getType(compoundName, compoundName.length);

		if (jdtBinding != null && (jdtBinding instanceof BinaryTypeBinding)) {
			// log("GCUScope.lookupClassNodeForBinary(): JDTBinding for '" + typename + "' found to be "
			// + jdtBinding.getClass().getSimpleName());
			ClassNode newNode = jdtResolver.convertToClassNode(jdtBinding);
			// if (newNode != null) {
			// jdtBinaryBindingCache.put(typename, newNode);
			// }
			return newNode;
		}

		if (jdtBinding != null && (jdtBinding instanceof ProblemReferenceBinding)) {
			ProblemReferenceBinding prBinding = (ProblemReferenceBinding) jdtBinding;
			if (prBinding.problemId() == ProblemReasons.InternalNameProvided) {
				jdtBinding = prBinding.closestMatch();
				// FIXASC (M2) caching for this too
				if (jdtBinding != null && (jdtBinding instanceof BinaryTypeBinding)) {
					return jdtResolver.convertToClassNode(jdtBinding);
				}
			}
		}
		return null;
	}

    // let it run to create synthetic methods
	// @Override
	// public void verifyMethods(MethodVerifier verifier) {
	// }

	// FIXASC (M2) Policing this might avoid the problem of JUnit not finding tests because the typename differs from the filename
	@Override
	protected void checkPublicTypeNameMatchesFilename(TypeDeclaration typeDecl) {
	}

	@Override
	protected void recordImportProblem(ImportReference importReference, Binding importBinding) {
	}

	// FIXASC (M2) is this true just for scripts or for anything at all?
	@Override
	protected void reportPackageIsNotExpectedPackage(CompilationUnitDeclaration referenceContext) {
	}

	@Override
	protected void checkParameterizedTypes() {
	}

	@Override
	public boolean reportInvalidType() {
		return false;
	}

	@Override
	public boolean canSeeEverything() {
		return true;
	}

	@Override
	public boolean checkTargetCompatibility() {
		return false;
	}

	// FIXASC (M2) verify that groovy sees all
	@Override
	protected boolean canBeSeenBy(ReferenceBinding type, PackageBinding fPackage) {
		return true;
	}

	@Override
	public boolean scannerAvailable() {
		return false;
	}

	// public ClassNode lookupInner(String typename, JDTResolver jdtResolver) {
	// ClassNode node = null;// jdtBindingCache.get(typename);
	// if (node != null) {
	// return node;
	// }
	// char[][] innerref = CharOperation.splitOn('$', typename.toCharArray());
	// Binding jdtBinding = getTypeOrPackage(innerref);
	// if (jdtBinding != null && (jdtBinding instanceof BinaryTypeBinding)) {
	// // log("GCUScope.lookupClassNodeForBinary(): JDTBinding for '" + typename + "' found to be "
	// // + jdtBinding.getClass().getSimpleName());
	// ClassNode newNode = jdtResolver.createJDTClassNode(jdtBinding);
	// return newNode;
	// }
	// return null;
	// }

	// FIXASC (M2) Does not appear to be necessary since the imports are now setup correctly
	// @Override
	// protected void faultInImports() {
	// if (this.typeOrPackageCache != null) {
	// // can be called when a field constant is resolved before static imports
	// return;
	// }
	//
	// // Special for groovy - want to insert BigDecimal/BigInteger
	// if (referenceContext.imports == null) {
	// this.typeOrPackageCache = new HashtableOfObject(3);
	// Binding jmBigDecimal = environment.getType(CharOperation.splitOn('.', "java.math.BigDecimal".toCharArray()));
	// typeOrPackageCache.put("BigDecimal".toCharArray(), jmBigDecimal);
	// Binding jmBigInteger = environment.getType(CharOperation.splitOn('.', "java.math.BigInteger".toCharArray()));
	// typeOrPackageCache.put("BigInteger".toCharArray(), jmBigInteger);
	// return;
	// } else {
	// super.faultInImports();
	// // Add entries for aliased imports
	// ImportReference[] importReferences = referenceContext.imports;
	// for (int i = 0; i < referenceContext.imports.length; i++) {
	// if (importReferences[i] instanceof AliasImportReference) {
	// AliasImportReference aliasImportReference = (AliasImportReference) importReferences[i];
	// char[][] importName = aliasImportReference.getImportName();
	// Binding importBinding = findSingleImport(importName, Binding.TYPE | Binding.FIELD | Binding.METHOD, false);//
	// importReference.isStatic());
	// typeOrPackageCache.put(aliasImportReference.getSimpleName(), importBinding);
	// }
	// }
	// }
	// }

}
