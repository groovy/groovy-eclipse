// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Erling Ellingsen -  patch for bug 125570
 *     Stephan Herrmann - Contribution for
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 434570 - Generic type mismatch for parametrized class annotation attribute with inner class
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.*;

public class CompilationUnitScope extends Scope {

	public LookupEnvironment environment;
	public CompilationUnitDeclaration referenceContext;
	public char[][] currentPackageName;
	public PackageBinding fPackage;
	public ImportBinding[] imports;
	public int importPtr;
	public HashtableOfObject typeOrPackageCache; // used in Scope.getTypeOrPackage()

	public SourceTypeBinding[] topLevelTypes;

	private CompoundNameVector qualifiedReferences;
	private SimpleNameVector simpleNameReferences;
	private SimpleNameVector rootReferences;
	private ObjectVector referencedTypes;
	private ObjectVector referencedSuperTypes;

	HashtableOfType constantPoolNameUsage;
	private int captureID = 1;
	
	private ImportBinding[] tempImports;	// to keep a record of resolved imports while traversing all in faultInImports()
	
	/**
	 * Flag that should be set during annotation traversal or similar runs
	 * to prevent caching of failures regarding imports of yet to be generated classes.
	 */
	public boolean suppressImportErrors;
	
	/**
	 * Skips import caching if unresolved imports were
	 * found last time.
	 */
	private boolean skipCachingImports;

	boolean connectingHierarchy;
	private ArrayList<Invocation> inferredInvocations;
	/** Cache of interned inference variables. Access only via {@link InferenceVariable#get(TypeBinding, int, InvocationSite, Scope, ReferenceBinding)}. */
	Map<InferenceVariable.InferenceVarKey, InferenceVariable> uniqueInferenceVariables = new HashMap<>();

public CompilationUnitScope(CompilationUnitDeclaration unit, LookupEnvironment environment) {
	this(unit, environment.globalOptions);
	this.environment = environment;
}

public CompilationUnitScope(CompilationUnitDeclaration unit, CompilerOptions compilerOptions) {
	super(COMPILATION_UNIT_SCOPE, null);
	this.referenceContext = unit;
	unit.scope = this;
	this.currentPackageName = unit.currentPackage == null ? CharOperation.NO_CHAR_CHAR : unit.currentPackage.tokens;

	if (compilerOptions.produceReferenceInfo) {
		this.qualifiedReferences = new CompoundNameVector();
		this.simpleNameReferences = new SimpleNameVector();
		this.rootReferences = new SimpleNameVector();
		this.referencedTypes = new ObjectVector();
		this.referencedSuperTypes = new ObjectVector();
	} else {
		this.qualifiedReferences = null; // used to test if dependencies should be recorded
		this.simpleNameReferences = null;
		this.rootReferences = null;
		this.referencedTypes = null;
		this.referencedSuperTypes = null;
	}
	// client still needs to assign #environment
}
void buildFieldsAndMethods() {
	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].scope.buildFieldsAndMethods();
}
// GROOVY add
/**
 * @return {@code true} if error gets reported
 */
protected boolean reportPackageIsNotExpectedPackage(CompilationUnitDeclaration unitDecl) {
	problemReporter().packageIsNotExpectedPackage(unitDecl);	
	return true;
}
protected
// GROOVY end
void buildTypeBindings(AccessRestriction accessRestriction) {
	this.topLevelTypes = new SourceTypeBinding[0]; // want it initialized if the package cannot be resolved
	boolean firstIsSynthetic = false;
	if (this.referenceContext.compilationResult.compilationUnit != null) {
		char[][] expectedPackageName = this.referenceContext.compilationResult.compilationUnit.getPackageName();
		if (expectedPackageName != null && !this.referenceContext.isModuleInfo()
				&& !CharOperation.equals(this.currentPackageName, expectedPackageName)) {
			// GROOVY add
			boolean errorReported = true;
			// GROOVY end
			// only report if the unit isn't structurally empty
			if (this.referenceContext.currentPackage != null
					|| this.referenceContext.types != null
					|| this.referenceContext.imports != null) {
				// GROOVY edit
				//problemReporter().packageIsNotExpectedPackage(this.referenceContext);
				errorReported = reportPackageIsNotExpectedPackage(this.referenceContext);
				// GROOVY end
			}
			// GROOVY add
			if (errorReported)
			// GROOVY end
			this.currentPackageName = expectedPackageName.length == 0 ? CharOperation.NO_CHAR_CHAR : expectedPackageName;
		}
	}
	if (this.currentPackageName == CharOperation.NO_CHAR_CHAR) {
		// environment default package is never null
		this.fPackage = this.environment.defaultPackage;
		if (this.referenceContext.isModuleInfo()) { // cannot have a package as per grammar
			ModuleDeclaration moduleDecl = this.referenceContext.moduleDeclaration;
			if (moduleDecl != null) {
				moduleDecl.createScope(this);
				moduleDecl.checkAndSetModifiers();
			}
		} else if (this.environment.module != this.environment.UnNamedModule) {
			problemReporter().unnamedPackageInNamedModule(this.environment.module);
		}
	} else {
		if ((this.fPackage = this.environment.createPackage(this.currentPackageName)) == null) {
			if (this.referenceContext.currentPackage != null) {
				problemReporter().packageCollidesWithType(this.referenceContext); // only report when the unit has a package statement
			}
			// ensure fPackage is not null
			this.fPackage = this.environment.defaultPackage;
			return;
		} else if (this.referenceContext.isPackageInfo()) {
			// resolve package annotations now if this is "package-info.java".
			if (this.referenceContext.types == null || this.referenceContext.types.length == 0) {
				this.referenceContext.types = new TypeDeclaration[1];
				this.referenceContext.createPackageInfoType();
				firstIsSynthetic = true;
			}
			// ensure the package annotations are copied over before resolution
			if (this.referenceContext.currentPackage != null && this.referenceContext.currentPackage.annotations != null) {
				this.referenceContext.types[0].annotations = this.referenceContext.currentPackage.annotations;
			}
		}
		recordQualifiedReference(this.currentPackageName); // always dependent on your own package
	}

	// Skip typeDeclarations which know of previously reported errors
	TypeDeclaration[] types = this.referenceContext.types;
	int typeLength = (types == null) ? 0 : types.length;
	this.topLevelTypes = new SourceTypeBinding[typeLength];
	int count = 0;
	nextType: for (int i = 0; i < typeLength; i++) {
		TypeDeclaration typeDecl = types[i];
		if (this.environment.isProcessingAnnotations && this.environment.isMissingType(typeDecl.name))
			throw new SourceTypeCollisionException(); // resolved a type ref before APT generated the type
		ReferenceBinding typeBinding = this.fPackage.getType0(typeDecl.name);
		if (Binding.isValid(typeBinding) && this.fPackage instanceof SplitPackageBinding && !this.environment.module.canAccess(typeBinding.fPackage))
			typeBinding = null;
		recordSimpleReference(typeDecl.name); // needed to detect collision cases
		if (Binding.isValid(typeBinding) && !(typeBinding instanceof UnresolvedReferenceBinding)) {
			// if its an unresolved binding - its fixed up whenever its needed, see UnresolvedReferenceBinding.resolve()
			if (this.environment.isProcessingAnnotations)
				throw new SourceTypeCollisionException(); // resolved a type ref before APT generated the type
			// if a type exists, check that its a valid type
			// it can be a NotFound problem type if its a secondary type referenced before its primary type found in additional units
			// and it can be an unresolved type which is now being defined
			problemReporter().duplicateTypes(this.referenceContext, typeDecl);
			continue nextType;
		}

		// GROOVY edit
		/*if ((typeDecl.modifiers & ClassFileConstants.AccPublic) != 0) {
			char[] mainTypeName;
			if ((mainTypeName = this.referenceContext.getMainTypeName()) != null // mainTypeName == null means that implementor of ICompilationUnit decided to return null
					&& !CharOperation.equals(mainTypeName, typeDecl.name)) {
				problemReporter().publicClassMustMatchFileName(this.referenceContext, typeDecl);
				// tolerate faulty main type name (91091), allow to proceed into type construction
			}
		}*/
		checkPublicTypeNameMatchesFilename(typeDecl);
		// GROOVY end

		// GROOVY edit
		//ClassScope child = new ClassScope(this, typeDecl);
		ClassScope child = buildClassScope(this, typeDecl);
		// GROOVY end
		SourceTypeBinding type = child.buildType(null, this.fPackage, accessRestriction);
		if (firstIsSynthetic && i == 0)
			type.modifiers |= ClassFileConstants.AccSynthetic;
		if (type != null)
			this.topLevelTypes[count++] = type;
	}

	// shrink topLevelTypes... only happens if an error was reported
	if (count != this.topLevelTypes.length)
		System.arraycopy(this.topLevelTypes, 0, this.topLevelTypes = new SourceTypeBinding[count], 0, count);
}
// GROOVY add
protected void checkPublicTypeNameMatchesFilename(TypeDeclaration typeDecl) {
	if ((typeDecl.modifiers & ClassFileConstants.AccPublic) != 0) {
		char[] mainTypeName;
		if ((mainTypeName = this.referenceContext.getMainTypeName()) != null && // mainTypeName == null means that implementor of ICompilationUnit decided to return null
				!CharOperation.equals(mainTypeName, typeDecl.name)) {
			problemReporter().publicClassMustMatchFileName(this.referenceContext, typeDecl);
			// tolerate faulty main type name (91091), allow to proceed into type construction
		}
	}
}
protected ClassScope buildClassScope(Scope parentScope, TypeDeclaration typeDecl) {
	return new ClassScope(parentScope, typeDecl);
}
// GROOVY end
void checkAndSetImports() {
	// TODO(SHMOD): verify: this block moved here from buildTypeBindings.
	// package resolving may require all modules to be known
	TypeDeclaration[] types = this.referenceContext.types;
	if (types != null) {
		for (int i = 0; i < types.length; i++) {
			TypeDeclaration typeDecl = types[i];
			if (this.fPackage != this.environment.defaultPackage && this.fPackage.getPackage(typeDecl.name, module()) != null) {
				// if a package exists, it must be a valid package - cannot be a NotFound problem package
				// this is now a warning since a package does not really 'exist' until it contains a type, see JLS v2, 7.4.3
				problemReporter().typeCollidesWithPackage(this.referenceContext, typeDecl);
			}
		}
	}

	if (this.referenceContext.moduleDeclaration != null) {
		this.referenceContext.moduleDeclaration.resolveModuleDirectives(this);
		this.referenceContext.moduleDeclaration.resolvePackageDirectives(this);
	}

	if (this.referenceContext.imports == null) {
		this.imports = getDefaultImports();
		return;
	}

	// allocate the import array, add java.lang.* by default
	int numberOfStatements = this.referenceContext.imports.length;
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = this.referenceContext.imports[i];
		if (((importReference.bits & ASTNode.OnDemand) != 0) && CharOperation.equals(TypeConstants.JAVA_LANG, importReference.tokens) && !importReference.isStatic()) {
			numberOfImports--;
			break;
		}
	}
	// GROOVY edit
	//ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
	//resolvedImports[0] = getDefaultImports()[0];
	//int index = 1;
	ImportBinding[] defaultImports = getDefaultImports();
	int index = defaultImports.length;
	ImportBinding[] resolvedImports = new ImportBinding[numberOfImports + defaultImports.length-1];
	System.arraycopy(defaultImports, 0, resolvedImports, 0, index);
	// GROOVY end

	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = this.referenceContext.imports[i];
		// GROOVY edit
		//char[][] compoundName = importReference.tokens;
		char[][] compoundName = importReference.getImportName();
		// GROOVY end

		// skip duplicates or imports of the current package
		for (int j = 0; j < index; j++) {
			ImportBinding resolved = resolvedImports[j];
			if (resolved.onDemand == ((importReference.bits & ASTNode.OnDemand) != 0) && resolved.isStatic() == importReference.isStatic())
				// GROOVY edit
				//if (CharOperation.equals(compoundName, resolvedImports[j].compoundName))
				if (CharOperation.equals(compoundName, resolvedImports[j].compoundName) && CharOperation.equals(importReference.getSimpleName(), getSimpleName(resolvedImports[j])))
				// GROOVY end
					continue nextImport;
		}

		if ((importReference.bits & ASTNode.OnDemand) != 0) {
			if (CharOperation.equals(compoundName, this.currentPackageName))
				continue nextImport;

			Binding importBinding = findImport(compoundName, compoundName.length);
			if (!importBinding.isValidBinding() || (importReference.isStatic() && importBinding instanceof PackageBinding))
				continue nextImport;	// we report all problems in faultInImports()
			resolvedImports[index++] = new ImportBinding(compoundName, true, importBinding, importReference);
		} else {
			// resolve single imports only when the last name matches
			resolvedImports[index++] = new ImportBinding(compoundName, false, null, importReference);
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (resolvedImports.length > index)
		System.arraycopy(resolvedImports, 0, resolvedImports = new ImportBinding[index], 0, index);
	this.imports = resolvedImports;
}

/**
 * Perform deferred check specific to parameterized types: bound checks, supertype collisions
 */
// GROOVY package->protected
protected void checkParameterizedTypes() {
	if (compilerOptions().sourceLevel < ClassFileConstants.JDK1_5) return;

	for (int i = 0, length = this.topLevelTypes.length; i < length; i++) {
		ClassScope scope = this.topLevelTypes[i].scope;
		scope.checkParameterizedTypeBounds();
		scope.checkParameterizedSuperTypeCollisions();
	}
}
/*
 * INTERNAL USE-ONLY
 * Innerclasses get their name computed as they are generated, since some may not
 * be actually outputed if sitting inside unreachable code.
 */
public char[] computeConstantPoolName(LocalTypeBinding localType) {
	if (localType.constantPoolName != null) {
		return localType.constantPoolName;
	}
	// delegates to the outermost enclosing classfile, since it is the only one with a global vision of its innertypes.

	if (this.constantPoolNameUsage == null)
		this.constantPoolNameUsage = new HashtableOfType();

	ReferenceBinding outerMostEnclosingType = localType.scope.outerMostClassScope().enclosingSourceType();

	// ensure there is not already such a local type name defined by the user
	int index = 0;
	char[] candidateName;
	boolean isCompliant15 = compilerOptions().complianceLevel >= ClassFileConstants.JDK1_5;
	while(true) {
		if (localType.isMemberType()){
			if (index == 0){
				candidateName = CharOperation.concat(
					localType.enclosingType().constantPoolName(),
					localType.sourceName,
					'$');
			} else {
				// in case of collision, then member name gets extra $1 inserted
				// e.g. class X { { class L{} new X(){ class L{} } } }
				candidateName = CharOperation.concat(
					localType.enclosingType().constantPoolName(),
					'$',
					String.valueOf(index).toCharArray(),
					'$',
					localType.sourceName);
			}
		} else if (localType.isAnonymousType()){
			if (isCompliant15) {
				// from 1.5 on, use immediately enclosing type name
				candidateName = CharOperation.concat(
					localType.enclosingType.constantPoolName(),
					String.valueOf(index+1).toCharArray(),
					'$');
			} else {
				candidateName = CharOperation.concat(
					outerMostEnclosingType.constantPoolName(),
					String.valueOf(index+1).toCharArray(),
					'$');
			}
		} else {
			// local type
			if (isCompliant15) {
				candidateName = CharOperation.concat(
					CharOperation.concat(
						localType.enclosingType().constantPoolName(),
						String.valueOf(index+1).toCharArray(),
						'$'),
					localType.sourceName);
			} else {
				candidateName = CharOperation.concat(
					outerMostEnclosingType.constantPoolName(),
					'$',
					String.valueOf(index+1).toCharArray(),
					'$',
					localType.sourceName);
			}
		}
		if (this.constantPoolNameUsage.get(candidateName) != null) {
			index ++;
		} else {
			this.constantPoolNameUsage.put(candidateName, localType);
			break;
		}
	}
	return candidateName;
}

void connectTypeHierarchy() {
	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].scope.connectTypeHierarchy();
}
void faultInImports() {
	boolean unresolvedFound = false;
	// should report unresolved only if we are not suppressing caching of failed resolutions
	boolean reportUnresolved = !this.suppressImportErrors;

	if (this.typeOrPackageCache != null && !this.skipCachingImports)
		return; // can be called when a field constant is resolved before static imports
	if (this.referenceContext.imports == null) {
		this.typeOrPackageCache = new HashtableOfObject(1);
		return;
	}

	// collect the top level type names if a single type import exists
	int numberOfStatements = this.referenceContext.imports.length;
	HashtableOfType typesBySimpleNames = null;
	for (int i = 0; i < numberOfStatements; i++) {
		if ((this.referenceContext.imports[i].bits & ASTNode.OnDemand) == 0) {
			typesBySimpleNames = new HashtableOfType(this.topLevelTypes.length + numberOfStatements);
			for (int j = 0, length = this.topLevelTypes.length; j < length; j++)
				typesBySimpleNames.put(this.topLevelTypes[j].sourceName, this.topLevelTypes[j]);
			break;
		}
	}

	// allocate the import array, add java.lang.* by default
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = this.referenceContext.imports[i];
		if (((importReference.bits & ASTNode.OnDemand) != 0) && CharOperation.equals(TypeConstants.JAVA_LANG, importReference.tokens) && !importReference.isStatic()) {
			numberOfImports--;
			break;
		}
	}
	// GROOVY edit
	//this.tempImports = new ImportBinding[numberOfImports];
	//this.tempImports[0] = getDefaultImports()[0];
	//this.importPtr = 1;
	ImportBinding[] defaultImports = getDefaultImports();
	this.importPtr = defaultImports.length;
	this.tempImports = new ImportBinding[numberOfImports + this.importPtr-1];
	System.arraycopy(defaultImports, 0, this.tempImports, 0, this.importPtr);
	// GROOVY end
	
	// keep static imports with normal imports until there is a reason to split them up
	// on demand imports continue to be packages & types. need to check on demand type imports for fields/methods
	// single imports change from being just types to types or fields
	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = this.referenceContext.imports[i];
		// GROOVY edit
		//char[][] compoundName = importReference.tokens;
		char[][] compoundName = importReference.getImportName();
		// GROOVY end

		// skip duplicates or imports of the current package
		for (int j = 0; j < this.importPtr; j++) {
			ImportBinding resolved = this.tempImports[j];
			if (resolved.onDemand == ((importReference.bits & ASTNode.OnDemand) != 0) && resolved.isStatic() == importReference.isStatic()) {
				// GROOVY edit
				//if (CharOperation.equals(compoundName, resolved.compoundName)) {
				if (CharOperation.equals(compoundName, resolved.compoundName) && CharOperation.equals(importReference.getSimpleName(), getSimpleName(resolved))) {
				// GROOVY end
					problemReporter().unusedImport(importReference); // since skipped, must be reported now
					continue nextImport;
				}
			}
		}
		if ((importReference.bits & ASTNode.OnDemand) != 0) {
			Binding importBinding = findImport(compoundName, compoundName.length);
			if (!importBinding.isValidBinding()) {
				// GROOVY edit
				//problemReporter().importProblem(importReference, importBinding);
				reportImportProblem(importReference, importBinding);
				// GROOVY end
				continue nextImport;
			}
			if (importBinding instanceof PackageBinding) {
				PackageBinding uniquePackage = ((PackageBinding)importBinding).getVisibleFor(module());
				if (uniquePackage instanceof SplitPackageBinding) {
					SplitPackageBinding splitPackage = (SplitPackageBinding) uniquePackage;
					problemReporter().conflictingPackagesFromModules(splitPackage, importReference.sourceStart, importReference.sourceEnd);
					continue nextImport;
				}
			}
			if (importReference.isStatic() && importBinding instanceof PackageBinding) {
				problemReporter().cannotImportPackage(importReference);
				continue nextImport;
			}
			recordImportBinding(new ImportBinding(compoundName, true, importBinding, importReference));
		} else {
			Binding importBinding = findSingleImport(compoundName, Binding.TYPE | Binding.FIELD | Binding.METHOD, importReference.isStatic());
			if (!importBinding.isValidBinding()) {
				if (importBinding.problemId() == ProblemReasons.Ambiguous) {
					// keep it unless a duplicate can be found below
				} else {
					unresolvedFound = true;
					if (reportUnresolved) {
						// GROOVY edit
						//problemReporter().importProblem(importReference, importBinding);
						reportImportProblem(importReference, importBinding);
						// GROOVY end
					}
					continue nextImport;
				}
			}
			if (importBinding instanceof PackageBinding) {
				problemReporter().cannotImportPackage(importReference);
				continue nextImport;
			} else if (this.environment.useModuleSystem && importBinding instanceof ReferenceBinding) {
				PackageBinding importedPackage = ((ReferenceBinding) importBinding).fPackage;
				if (importedPackage != null) {
					if (!importedPackage.isValidBinding()) {
						problemReporter().importProblem(importReference, importedPackage);
						continue nextImport;
					}
					// re-get to find a possible split package:
					importedPackage = (PackageBinding) findImport(importedPackage.compoundName, false, true);
					if (importedPackage != null)
						importedPackage = importedPackage.getVisibleFor(module());
					if (importedPackage instanceof SplitPackageBinding) {
						SplitPackageBinding splitPackage = (SplitPackageBinding) importedPackage;
						int sourceEnd = (int) importReference.sourcePositions[splitPackage.compoundName.length-1];
						problemReporter().conflictingPackagesFromModules(splitPackage, importReference.sourceStart, sourceEnd);
						continue nextImport;
					}
				}
			}
			// all the code here which checks for valid bindings have been moved to the method 
			// checkAndRecordImportBinding() since bug 361327
			if(checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName) == -1)
				continue nextImport;
			if (importReference.isStatic()) {
				// look for more static bindings being imported by single static import(bug 361327).
				// findSingleImport() finds fields first, followed by method and then type
				// So if a type is found, no fields and methods are available anyway
				// similarly when method is found, type may be available but no field available for sure
				if (importBinding.kind() == Binding.FIELD) {
					checkMoreStaticBindings(compoundName, typesBySimpleNames, Binding.TYPE | Binding.METHOD, importReference);		
				} else if (importBinding.kind() == Binding.METHOD) {
					checkMoreStaticBindings(compoundName, typesBySimpleNames, Binding.TYPE, importReference);
				}
			}
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (this.tempImports.length > this.importPtr)
		System.arraycopy(this.tempImports, 0, this.tempImports = new ImportBinding[this.importPtr], 0, this.importPtr);
	this.imports = this.tempImports;
	int length = this.imports.length;
	this.typeOrPackageCache = new HashtableOfObject(length);
	for (int i = 0; i < length; i++) {
		ImportBinding binding = this.imports[i];
		if (!binding.onDemand && binding.resolvedImport instanceof ReferenceBinding || binding instanceof ImportConflictBinding)
			// GROOVY edit
			//this.typeOrPackageCache.put(binding.compoundName[binding.compoundName.length - 1], binding);
			this.typeOrPackageCache.put(getSimpleName(binding), binding);
			// GROOVY end
	}
	this.skipCachingImports = this.suppressImportErrors && unresolvedFound;
}
public void faultInTypes() {
	faultInImports();
	if (this.referenceContext.moduleDeclaration != null) {
		this.referenceContext.moduleDeclaration.resolveTypeDirectives(this);
	} else if (this.referenceContext.currentPackage != null) {
		this.referenceContext.currentPackage.checkPackageConflict(this);
	}

	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].faultInTypesForFieldsAndMethods();
}
// this API is for code assist purpose
public Binding findImport(char[][] compoundName, boolean findStaticImports, boolean onDemand) {
	if(onDemand) {
		return findImport(compoundName, compoundName.length);
	} else {
		return findSingleImport(compoundName, Binding.TYPE | Binding.FIELD | Binding.METHOD, findStaticImports);
	}
}
private Binding findImport(char[][] compoundName, int length) {
	recordQualifiedReference(compoundName);
	ModuleBinding module = module();
	Binding binding = this.environment.getTopLevelPackage(compoundName[0]);
	int i = 1;
	foundNothingOrType: if (binding != null) {
		PackageBinding packageBinding = (PackageBinding) binding;
		while (i < length) {
			binding = packageBinding.getTypeOrPackage(compoundName[i++], module);
			if (binding instanceof ReferenceBinding && binding.problemId() == ProblemReasons.NotAccessible) {
				return this.environment.convertToRawType((TypeBinding) binding, false /*do not force conversion of enclosing types*/);
			}
			if (binding == null || !binding.isValidBinding()) {
				binding = null;
				break foundNothingOrType;
			}
			if (!(binding instanceof PackageBinding))
				break foundNothingOrType;

			packageBinding = (PackageBinding) binding;
		}
		if (packageBinding.isValidBinding() && !module.canAccess(packageBinding))
			return new ProblemPackageBinding(compoundName, ProblemReasons.NotAccessible);
		return packageBinding;
	}

	ReferenceBinding type;
	if (binding == null) {
		if (compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
		type = findType(compoundName[0], this.environment.defaultPackage, this.environment.defaultPackage);
		if (type == null || !type.isValidBinding())
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
		i = 1; // reset to look for member types inside the default package type
	} else {
		type = (ReferenceBinding) binding;
	}

	while (i < length) {
		type = (ReferenceBinding)this.environment.convertToRawType(type, false /*do not force conversion of enclosing types*/); // type imports are necessarily raw for all except last
		if (!type.canBeSeenBy(this.fPackage))
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), type, ProblemReasons.NotVisible);

		char[] name = compoundName[i++];
		// does not look for inherited member types on purpose, only immediate members
		type = type.getMemberType(name);
		if (type == null)
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
	}
	// GROOVY edit
	//if (!type.canBeSeenBy(this.fPackage))
	if (!canBeSeenBy(type, this.fPackage))
	// GROOVY end
		return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotVisible);
	return type;
}
// GROOVY add -- new method for determining visibility; rules are relaxed for groovy
protected boolean canBeSeenBy(ReferenceBinding type, PackageBinding pkg) {
	return type.canBeSeenBy(pkg);
}
// GROOVY end
// GROOVY private->protected
protected Binding findSingleImport(char[][] compoundName, int mask, boolean findStaticImports) {
	if (compoundName.length == 1) {
		// findType records the reference
		// the name cannot be a package
		if (compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4 && !this.referenceContext.isModuleInfo())
			return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
		ReferenceBinding typeBinding = findType(compoundName[0], this.environment.defaultPackage, this.fPackage);
		if (typeBinding == null)
			return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
		return typeBinding;
	}

	if (findStaticImports)
		return findSingleStaticImport(compoundName, mask);
	return findImport(compoundName, compoundName.length);
}
private Binding findSingleStaticImport(char[][] compoundName, int mask) {
	Binding binding = findImport(compoundName, compoundName.length - 1);
	if (!binding.isValidBinding()) return binding;

	char[] name = compoundName[compoundName.length - 1];
	if (binding instanceof PackageBinding) {
		Binding temp = ((PackageBinding) binding).getTypeOrPackage(name, module());
		if (temp != null && temp instanceof ReferenceBinding) // must resolve to a member type or field, not a top level type
			return new ProblemReferenceBinding(compoundName, (ReferenceBinding) temp, ProblemReasons.InvalidTypeForStaticImport);
		return binding; // cannot be a package, error is caught in sender
	}

	// look to see if its a static field first
	ReferenceBinding type = (ReferenceBinding) binding;
	FieldBinding field = (mask & Binding.FIELD) != 0 ? findField(type, name, null, true) : null;
	if (field != null) {
		if (field.problemId() == ProblemReasons.Ambiguous && ((ProblemFieldBinding) field).closestMatch.isStatic())
			return field; // keep the ambiguous field instead of a possible method match
		if (field.isValidBinding() && field.isStatic() && field.canBeSeenBy(type, null, this))
			return field;
	}

	// look to see if there is a static method with the same selector
	MethodBinding method = (mask & Binding.METHOD) != 0 ? findStaticMethod(type, name) : null;
	if (method != null) return method;

	type = findMemberType(name, type);
	if (type == null || !type.isStatic()) {
		if (field != null && !field.isValidBinding() && field.problemId() != ProblemReasons.NotFound)
			return field;
		return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotFound);
	}
	if (type.isValidBinding() && !type.canBeSeenBy(this.fPackage))
		return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotVisible);
	if (type.problemId() == ProblemReasons.NotVisible) // ensure compoundName is correct
		return new ProblemReferenceBinding(compoundName, ((ProblemReferenceBinding) type).closestMatch, ProblemReasons.NotVisible);
	return type;
}
// helper method for findSingleStaticImport()
private MethodBinding findStaticMethod(ReferenceBinding currentType, char[] selector) {
	if (!currentType.canBeSeenBy(this))
		return null;

	do {
		currentType.initializeForStaticImports();
		MethodBinding[] methods = currentType.getMethods(selector);
		if (methods != Binding.NO_METHODS) {
			for (int i = methods.length; --i >= 0;) {
				MethodBinding method = methods[i];
				if (method.isStatic() && method.canBeSeenBy(this.fPackage))
					return method;
			}
		}
	} while ((currentType = currentType.superclass()) != null);
	return null;
}
// GROOVY package->protected
protected ImportBinding[] getDefaultImports() {
	// initialize the default imports if necessary... share the default java.lang.* import
	if (this.environment.root.defaultImports != null) return this.environment.root.defaultImports;

	Binding importBinding = this.environment.getTopLevelPackage(TypeConstants.JAVA);
	if (importBinding != null)
		importBinding = ((PackageBinding) importBinding).getTypeOrPackage(TypeConstants.JAVA_LANG[1], module());

	if (importBinding == null || !importBinding.isValidBinding()) {
		// create a proxy for the missing BinaryType
		problemReporter().isClassPathCorrect(
				TypeConstants.JAVA_LANG_OBJECT,
			this.referenceContext,
			this.environment.missingClassFileLocation);
		BinaryTypeBinding missingObject = this.environment.createMissingType(null, TypeConstants.JAVA_LANG_OBJECT);
		importBinding = missingObject.fPackage;
	}

	return this.environment.root.defaultImports = new ImportBinding[] {new ImportBinding(TypeConstants.JAVA_LANG, true, importBinding, null)};
}
// NOT Public API
public final Binding getImport(char[][] compoundName, boolean onDemand, boolean isStaticImport) {
	if (onDemand)
		return findImport(compoundName, compoundName.length);
	return findSingleImport(compoundName, Binding.TYPE | Binding.FIELD | Binding.METHOD, isStaticImport);
}

public int nextCaptureID() {
	return this.captureID++;
}

/* Answer the problem reporter to use for raising new problems.
*
* Note that as a side-effect, this updates the current reference context
* (unit, type or method) in case the problem handler decides it is necessary
* to abort.
*/
public ProblemReporter problemReporter() {
	ProblemReporter problemReporter = this.referenceContext.problemReporter;
	problemReporter.referenceContext = this.referenceContext;
	return problemReporter;
}

/*
What do we hold onto:

1. when we resolve 'a.b.c', say we keep only 'a.b.c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b.c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b.c'
-> This approach fails because every type is resolved in every onDemand import to
 detect collision cases... so the references could be 10 times bigger than necessary.

2. when we resolve 'a.b.c', lets keep 'a.b' & 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
-> This approach does not have a space problem but fails to handle collision cases.
 What happens if a type is added named 'a.b'? We would search for 'a' & 'b' but
 would not find a match.

3. when we resolve 'a.b.c', lets keep 'a', 'a.b' & 'a', 'b', 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a', 'a.b' & 'a', 'b', 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
OR 'a.b' -> 'a' & 'b'
OR 'a' -> '' & 'a'
-> As long as each single char[] is interned, we should not have a space problem
 and can handle collision cases.

4. when we resolve 'a.b.c', lets keep 'a.b' & 'a', 'b', 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'a', 'b', 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
OR 'a.b' -> 'a' & 'b' in the simple name collection
OR 'a' -> 'a' in the simple name collection
-> As long as each single char[] is interned, we should not have a space problem
 and can handle collision cases.
*/
// GROOVY package->public
public void recordQualifiedReference(char[][] qualifiedName) {
	if (this.qualifiedReferences == null) return; // not recording dependencies

	int length = qualifiedName.length;
	if (length > 1) {
		recordRootReference(qualifiedName[0]);
		while (!this.qualifiedReferences.contains(qualifiedName)) {
			this.qualifiedReferences.add(qualifiedName);
			if (length == 2) {
				recordSimpleReference(qualifiedName[0]);
				recordSimpleReference(qualifiedName[1]);
				return;
			}
			length--;
			recordSimpleReference(qualifiedName[length]);
			System.arraycopy(qualifiedName, 0, qualifiedName = new char[length][], 0, length);
		}
	} else if (length == 1) {
		recordRootReference(qualifiedName[0]);
		recordSimpleReference(qualifiedName[0]);
	}
}
void recordReference(char[][] qualifiedEnclosingName, char[] simpleName) {
	recordQualifiedReference(qualifiedEnclosingName);
	if (qualifiedEnclosingName.length == 0)
		recordRootReference(simpleName);
	recordSimpleReference(simpleName);
}
void recordReference(ReferenceBinding type, char[] simpleName) {
	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null)
		recordReference(actualType.compoundName, simpleName);
}
void recordRootReference(char[] simpleName) {
	if (this.rootReferences == null) return; // not recording dependencies

	if (!this.rootReferences.contains(simpleName))
		this.rootReferences.add(simpleName);
}
// GROOVY package->public
public void recordSimpleReference(char[] simpleName) {
	if (this.simpleNameReferences == null) return; // not recording dependencies

	if (!this.simpleNameReferences.contains(simpleName))
		this.simpleNameReferences.add(simpleName);
}
void recordSuperTypeReference(TypeBinding type) {
	if (this.referencedSuperTypes == null) return; // not recording dependencies

	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null && !this.referencedSuperTypes.containsIdentical(actualType))
		this.referencedSuperTypes.add(actualType);
}
public void recordTypeConversion(TypeBinding superType, TypeBinding subType) {
	recordSuperTypeReference(subType); // must record the hierarchy of the subType that is converted to the superType
}
void recordTypeReference(TypeBinding type) {
	if (this.referencedTypes == null) return; // not recording dependencies

	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null && !this.referencedTypes.containsIdentical(actualType))
		this.referencedTypes.add(actualType);
}
void recordTypeReferences(TypeBinding[] types) {
	if (this.referencedTypes == null) return; // not recording dependencies
	if (types == null || types.length == 0) return;

	for (int i = 0, max = types.length; i < max; i++) {
		// No need to record supertypes of method arguments & thrown exceptions, just the compoundName
		// If a field/method is retrieved from such a type then a separate call does the job
		ReferenceBinding actualType = typeToRecord(types[i]);
		if (actualType != null && !this.referencedTypes.containsIdentical(actualType))
			this.referencedTypes.add(actualType);
	}
}
Binding resolveSingleImport(ImportBinding importBinding, int mask) {
	if (importBinding.resolvedImport == null) {
		importBinding.resolvedImport = findSingleImport(importBinding.compoundName, mask, importBinding.isStatic());
		if (!importBinding.resolvedImport.isValidBinding() || importBinding.resolvedImport instanceof PackageBinding) {
			if (importBinding.resolvedImport.problemId() == ProblemReasons.Ambiguous)
				return importBinding.resolvedImport;
			if (this.imports != null) {
				ImportBinding[] newImports = new ImportBinding[this.imports.length - 1];
				for (int i = 0, n = 0, max = this.imports.length; i < max; i++)
					if (this.imports[i] != importBinding)
						newImports[n++] = this.imports[i];
				this.imports = newImports;
			}
			return null;
		}
	}
	return importBinding.resolvedImport;
}
public void storeDependencyInfo() {
	// add the type hierarchy of each referenced supertype
	// cannot do early since the hierarchy may not be fully resolved
	for (int i = 0; i < this.referencedSuperTypes.size; i++) { // grows as more types are added
		ReferenceBinding type = (ReferenceBinding) this.referencedSuperTypes.elementAt(i);
		if (!this.referencedTypes.containsIdentical(type))
			this.referencedTypes.add(type);

		if (!type.isLocalType()) {
			ReferenceBinding enclosing = type.enclosingType();
			if (enclosing != null)
				recordSuperTypeReference(enclosing);
		}
		ReferenceBinding superclass = type.superclass();
		if (superclass != null)
			recordSuperTypeReference(superclass);
		ReferenceBinding[] interfaces = type.superInterfaces();
		if (interfaces != null)
			for (int j = 0, length = interfaces.length; j < length; j++)
				recordSuperTypeReference(interfaces[j]);
	}

	for (int i = 0, l = this.referencedTypes.size; i < l; i++) {
		ReferenceBinding type = (ReferenceBinding) this.referencedTypes.elementAt(i);
		if (!type.isLocalType())
			recordQualifiedReference(type.isMemberType()
				? CharOperation.splitOn('.', type.readableName())
				: type.compoundName);
	}

	int size = this.qualifiedReferences.size;
	char[][][] qualifiedRefs = new char[size][][];
	for (int i = 0; i < size; i++)
		qualifiedRefs[i] = this.qualifiedReferences.elementAt(i);
	this.referenceContext.compilationResult.qualifiedReferences = qualifiedRefs;

	size = this.simpleNameReferences.size;
	char[][] simpleRefs = new char[size][];
	for (int i = 0; i < size; i++)
		simpleRefs[i] = this.simpleNameReferences.elementAt(i);
	this.referenceContext.compilationResult.simpleNameReferences = simpleRefs;

	size = this.rootReferences.size;
	char[][] rootRefs = new char[size][];
	for (int i = 0; i < size; i++)
		rootRefs[i] = this.rootReferences.elementAt(i);
	this.referenceContext.compilationResult.rootReferences = rootRefs;
}
public String toString() {
	return "--- CompilationUnit Scope : " + new String(this.referenceContext.getFileName()); //$NON-NLS-1$
}
private ReferenceBinding typeToRecord(TypeBinding type) {
	if (type == null)
		return null;
	while (type.isArrayType())
		type = ((ArrayBinding) type).leafComponentType();

	switch (type.kind()) {
		case Binding.BASE_TYPE :
		case Binding.TYPE_PARAMETER :
		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE :
		case Binding.INTERSECTION_TYPE18: // constituents would have been recorded.
		case Binding.POLY_TYPE: // not a real type, will mutate into one, hopefully soon.
			return null;
		case Binding.PARAMETERIZED_TYPE :
		case Binding.RAW_TYPE :
			type = type.erasure();
	}
	ReferenceBinding refType = (ReferenceBinding) type;
	if (refType.isLocalType()) return null;
	return refType;
}
public void verifyMethods(MethodVerifier verifier) {
	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].verifyMethods(verifier);
}
private void recordImportBinding(ImportBinding bindingToAdd) {
	if (this.tempImports.length == this.importPtr) {
		System.arraycopy(this.tempImports, 0, (this.tempImports = new ImportBinding[this.importPtr + 1]), 0, this.importPtr);
	}
	this.tempImports[this.importPtr++] = bindingToAdd;
}
/**
 * Checks additional bindings (methods or types) imported from a single static import. 
 * Method is tried first, followed by type. If found, records them.
 * If in the process, import is flagged as duplicate, -1 is returned.
 * @param compoundName
 * @param typesBySimpleNames
 * @param mask
 * @param importReference
 */
private void checkMoreStaticBindings(
		char[][] compoundName, 
		HashtableOfType typesBySimpleNames, 
		int mask,
		ImportReference importReference) {
	Binding importBinding = findSingleStaticImport(compoundName, mask);
	if (!importBinding.isValidBinding()) {
		// only continue if the same kind's ambiguous binding is returned
		// may have found an ambiguous type when looking for field or method. Don't continue in that case
		if (importBinding.problemId() == ProblemReasons.Ambiguous) {
			// keep it unless a duplicate can be found below
			checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName);
		}
	} else {
		checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName);
	}
	if (((mask & Binding.METHOD) != 0) && (importBinding.kind() == Binding.METHOD)) {
		// found method
		// type is left to be looked for
		// reset METHOD bit to enable lookup for only type
		mask &= ~Binding.METHOD;
		// now search for a type binding
		checkMoreStaticBindings(compoundName, typesBySimpleNames, mask, importReference);
	}
}
/**
 * Checks for duplicates. If all ok, records the importBinding
 * returns -1 when this import is flagged as duplicate.
 * @param importBinding
 * @param typesBySimpleNames
 * @param importReference
 * @param compoundName
 * @return -1 when this import is flagged as duplicate, importPtr otherwise.
 */
private int checkAndRecordImportBinding(
		Binding importBinding, 
		HashtableOfType typesBySimpleNames, 
		ImportReference importReference,
		char[][] compoundName) {
	ReferenceBinding conflictingType = null;
	if (importBinding instanceof MethodBinding) {
		conflictingType = (ReferenceBinding) getType(compoundName, compoundName.length);
		if (!conflictingType.isValidBinding() || (importReference.isStatic() && !conflictingType.isStatic()))
			conflictingType = null;
	}
	// collisions between an imported static field & a type should be checked according to spec... but currently not by javac
	final char[] name = compoundName[compoundName.length - 1];
	if (importBinding instanceof ReferenceBinding || conflictingType != null) {
		ReferenceBinding referenceBinding = conflictingType == null ? (ReferenceBinding) importBinding : conflictingType;
		ReferenceBinding typeToCheck = referenceBinding.problemId() == ProblemReasons.Ambiguous
			? ((ProblemReferenceBinding) referenceBinding).closestMatch
			: referenceBinding;
		if (importReference.isTypeUseDeprecated(typeToCheck, this))
			problemReporter().deprecatedType(typeToCheck, importReference);

		// GROOVY edit
		//ReferenceBinding existingType = typesBySimpleNames.get(name);
		ReferenceBinding existingType = typesBySimpleNames.get(importReference.getSimpleName());
		// GROOVY end
		if (existingType != null) {
			// duplicate test above should have caught this case, but make sure
			if (TypeBinding.equalsEquals(existingType, referenceBinding)) {
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302865
				// Check all resolved imports to see if this import qualifies as a duplicate
				for (int j = 0; j < this.importPtr; j++) {
					ImportBinding resolved = this.tempImports[j];
					if (resolved instanceof ImportConflictBinding) {
						ImportConflictBinding importConflictBinding = (ImportConflictBinding) resolved;
						if (TypeBinding.equalsEquals(importConflictBinding.conflictingTypeBinding, referenceBinding)) {
							if (!importReference.isStatic()) {
								// resolved is implicitly static
								problemReporter().duplicateImport(importReference);
								recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
							}
						}
					} else if (resolved.resolvedImport == referenceBinding) {
						if (importReference.isStatic() != resolved.isStatic()) {
							recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
						}
					}
				}
				return -1;
			}
			// either the type collides with a top level type or another imported type
			for (int j = 0, length = this.topLevelTypes.length; j < length; j++) {
				if (CharOperation.equals(this.topLevelTypes[j].sourceName, existingType.sourceName)) {
					problemReporter().conflictingImport(importReference);
					return -1;
				}
			}
			if (importReference.isStatic() && importBinding instanceof ReferenceBinding && compilerOptions().sourceLevel >= ClassFileConstants.JDK1_8) {
				// 7.5.3 says nothing about collision of single static imports and JDK8 tolerates them, though use is flagged.
				for (int j = 0; j < this.importPtr; j++) {
					ImportBinding resolved = this.tempImports[j];
					if (resolved.isStatic() && resolved.resolvedImport instanceof ReferenceBinding && importBinding != resolved.resolvedImport) {
						if (CharOperation.equals(name, resolved.compoundName[resolved.compoundName.length - 1])) {
							ReferenceBinding type = (ReferenceBinding) resolved.resolvedImport;
							resolved.resolvedImport = new ProblemReferenceBinding(new char[][] { name }, type, ProblemReasons.Ambiguous);
							return -1;
						}
					}
				}
			}
			problemReporter().duplicateImport(importReference);
			return -1;
		}
		// GROOVY edit
		//typesBySimpleNames.put(name, referenceBinding);
		typesBySimpleNames.put(importReference.getSimpleName(), referenceBinding);
		// GROOVY end
	} else if (importBinding instanceof FieldBinding) {
		for (int j = 0; j < this.importPtr; j++) {
			ImportBinding resolved = this.tempImports[j];
			// find other static fields with the same name
			if (resolved.isStatic() && resolved.resolvedImport instanceof FieldBinding && importBinding != resolved.resolvedImport) {
				if (CharOperation.equals(name, resolved.compoundName[resolved.compoundName.length - 1])) {
					if (compilerOptions().sourceLevel >= ClassFileConstants.JDK1_8) {
						// 7.5.3 says nothing about collision of single static imports and JDK8 tolerates them, though use is flagged.
						FieldBinding field = (FieldBinding) resolved.resolvedImport;
						resolved.resolvedImport = new ProblemFieldBinding(field, field.declaringClass, name, ProblemReasons.Ambiguous);
						return -1;
					} else {
						problemReporter().duplicateImport(importReference);
						return -1;
					}
				}
			}
		}
	}
	if (conflictingType == null) {
		recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
	} else {
		recordImportBinding(new ImportConflictBinding(compoundName, importBinding, conflictingType, importReference));
	}
	return this.importPtr;
}
@Override
public boolean hasDefaultNullnessFor(int location, int sourceStart) {
	int nonNullByDefaultValue = localNonNullByDefaultValue(sourceStart);
	if (nonNullByDefaultValue != 0) {
		return (nonNullByDefaultValue & location) != 0;
	}
	if (this.fPackage != null)
		return (this.fPackage.defaultNullness & location) != 0;
	return false;
}

@Override
public /* @Nullable */ Binding checkRedundantDefaultNullness(int nullBits, int sourceStart) {
	Binding target = localCheckRedundantDefaultNullness(nullBits, sourceStart);
	if (target != null) {
		return target;
	}
	if (this.fPackage != null) {
		return (this.fPackage.defaultNullness == nullBits) ? this.fPackage : null;
	}

	return null;
}

public void registerInferredInvocation(Invocation invocation) {
	if (this.inferredInvocations == null)
		this.inferredInvocations = new ArrayList<>();
	this.inferredInvocations.add(invocation);
}
public void cleanUpInferenceContexts() {
	if (this.inferredInvocations == null)
		return;
	for (Invocation invocation : this.inferredInvocations)
		invocation.cleanUpInferenceContexts();
	this.inferredInvocations = null;
}
// GROOVY add
public void augmentTypeHierarchy() {
	// nothing to do for Java
}
public boolean checkTargetCompatibility() {
	return true;
}
public boolean scannerAvailable() {
	return true;
}
public boolean reportInvalidType(TypeReference typeReference, TypeBinding resolvedType) {
	return true;
}
protected void reportImportProblem(ImportReference importReference, Binding importBinding) {
	problemReporter().importProblem(importReference, importBinding);
}
public boolean canSeeEverything() {
	return false;
}
public ReferenceBinding selectBinding(ReferenceBinding temp, ReferenceBinding type, boolean isDeclaredImport) {
	return null;
}
// GROOVY end
}
