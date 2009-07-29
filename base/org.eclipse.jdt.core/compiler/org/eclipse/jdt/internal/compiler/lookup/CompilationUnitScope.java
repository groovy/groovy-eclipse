/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Erling Ellingsen -  patch for bug 125570
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.*;

public class CompilationUnitScope extends Scope {
	
	public LookupEnvironment environment;
	public CompilationUnitDeclaration referenceContext;
	public char[][] currentPackageName;
	public PackageBinding fPackage;
	public ImportBinding[] imports;
	public HashtableOfObject typeOrPackageCache; // used in Scope.getTypeOrPackage()
	
	public SourceTypeBinding[] topLevelTypes;
	
	private CompoundNameVector qualifiedReferences;
	private SimpleNameVector simpleNameReferences;
	private ObjectVector referencedTypes;
	private ObjectVector referencedSuperTypes;
	
	HashtableOfType constantPoolNameUsage;
	private int captureID = 1;

public CompilationUnitScope(CompilationUnitDeclaration unit, LookupEnvironment environment) {
	super(COMPILATION_UNIT_SCOPE, null);
	this.environment = environment;
	this.referenceContext = unit;
	unit.scope = this;
	this.currentPackageName = unit.currentPackage == null ? CharOperation.NO_CHAR_CHAR : unit.currentPackage.tokens;

	if (compilerOptions().produceReferenceInfo) {
		this.qualifiedReferences = new CompoundNameVector();
		this.simpleNameReferences = new SimpleNameVector();
		this.referencedTypes = new ObjectVector();
		this.referencedSuperTypes = new ObjectVector();
	} else {
		this.qualifiedReferences = null; // used to test if dependencies should be recorded
		this.simpleNameReferences = null;
		this.referencedTypes = null;
		this.referencedSuperTypes = null;
	}
}
void buildFieldsAndMethods() {
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].scope.buildFieldsAndMethods();
}

// GROOVY start: new method, can be overridden
protected void reportPackageIsNotExpectedPackage(CompilationUnitDeclaration referenceContext) {
	problemReporter().packageIsNotExpectedPackage(referenceContext);	
}
// GROOVY end
void buildTypeBindings(AccessRestriction accessRestriction) {
	topLevelTypes = new SourceTypeBinding[0]; // want it initialized if the package cannot be resolved
	boolean firstIsSynthetic = false;
	if (referenceContext.compilationResult.compilationUnit != null) {
		char[][] expectedPackageName = referenceContext.compilationResult.compilationUnit.getPackageName();
		if (expectedPackageName != null 
				&& !CharOperation.equals(currentPackageName, expectedPackageName)) {

			// only report if the unit isn't structurally empty
			if (referenceContext.currentPackage != null 
					|| referenceContext.types != null 
					|| referenceContext.imports != null) {
				// GROOVY start:
				// old code:
//				problemReporter().packageIsNotExpectedPackage(referenceContext);
				// new code:
				reportPackageIsNotExpectedPackage(referenceContext);
				// GROOVY end
			}
			currentPackageName = expectedPackageName.length == 0 ? CharOperation.NO_CHAR_CHAR : expectedPackageName;
		}
	}
	if (currentPackageName == CharOperation.NO_CHAR_CHAR) {
		if ((fPackage = environment.defaultPackage) == null) {
			problemReporter().mustSpecifyPackage(referenceContext);
			return;
		}
	} else {
		if ((fPackage = environment.createPackage(currentPackageName)) == null) {
			problemReporter().packageCollidesWithType(referenceContext);
			return;
		} else if (referenceContext.isPackageInfo()) {
			// resolve package annotations now if this is "package-info.java".				
			if (referenceContext.types == null || referenceContext.types.length == 0) {
				referenceContext.types = new TypeDeclaration[1];
				TypeDeclaration declaration = new TypeDeclaration(referenceContext.compilationResult);
				referenceContext.types[0] = declaration;
				declaration.name = TypeConstants.PACKAGE_INFO_NAME;
				declaration.modifiers = ClassFileConstants.AccDefault | ClassFileConstants.AccInterface;
				firstIsSynthetic = true;
			}
		}
		recordQualifiedReference(currentPackageName); // always dependent on your own package
	}

	// Skip typeDeclarations which know of previously reported errors
	TypeDeclaration[] types = referenceContext.types;
	int typeLength = (types == null) ? 0 : types.length;
	topLevelTypes = new SourceTypeBinding[typeLength];
	int count = 0;
	nextType: for (int i = 0; i < typeLength; i++) {
		TypeDeclaration typeDecl = types[i];
		ReferenceBinding typeBinding = fPackage.getType0(typeDecl.name);
		recordSimpleReference(typeDecl.name); // needed to detect collision cases
		if (typeBinding != null && typeBinding.isValidBinding() && !(typeBinding instanceof UnresolvedReferenceBinding)) {
			// if a type exists, check that its a valid type
			// it can be a NotFound problem type if its a secondary type referenced before its primary type found in additional units
			// and it can be an unresolved type which is now being defined
			problemReporter().duplicateTypes(referenceContext, typeDecl);
			continue nextType;
		}
		if (fPackage != environment.defaultPackage && fPackage.getPackage(typeDecl.name) != null) {
			// if a package exists, it must be a valid package - cannot be a NotFound problem package
			// this is now a warning since a package does not really 'exist' until it contains a type, see JLS v2, 7.4.3
			problemReporter().typeCollidesWithPackage(referenceContext, typeDecl);
		}

		//GROOVY start: make this decision only for java types, so extract it to a new method so it can be overridden
		// old code:
//		if ((typeDecl.modifiers & ClassFileConstants.AccPublic) != 0) {
//			char[] mainTypeName;
//			if ((mainTypeName = referenceContext.getMainTypeName()) != null // mainTypeName == null means that implementor of ICompilationUnit decided to return null
//					&& !CharOperation.equals(mainTypeName, typeDecl.name)) {
//				problemReporter().publicClassMustMatchFileName(referenceContext, typeDecl);
//				// tolerate faulty main type name (91091), allow to proceed into type construction
//			}
//		}
		// new code:
		checkPublicTypeNameMatchesFilename(typeDecl);
		// GROOVY end
		
		// GROOVY start: make the ClassScope creation overridable
		// old code:
		// ClassScope child = new ClassScope(this, typeDecl);
		// new code:
		ClassScope child = buildClassScope(this, typeDecl);
		// GROOVY end
		SourceTypeBinding type = child.buildType(null, fPackage, accessRestriction);
		if (firstIsSynthetic && i == 0)
			type.modifiers |= ClassFileConstants.AccSynthetic;
		if (type != null)
			topLevelTypes[count++] = type;
	}

	// shrink topLevelTypes... only happens if an error was reported
	if (count != topLevelTypes.length)
		System.arraycopy(topLevelTypes, 0, topLevelTypes = new SourceTypeBinding[count], 0, count);
}
//GROOVY start: new methods, overridden in our compilation unit scope
protected void checkPublicTypeNameMatchesFilename(TypeDeclaration typeDecl) {
	if ((typeDecl.modifiers & ClassFileConstants.AccPublic) != 0) {
		char[] mainTypeName;
		if ((mainTypeName = referenceContext.getMainTypeName()) != null // mainTypeName == null means that implementor of ICompilationUnit decided to return null
				&& !CharOperation.equals(mainTypeName, typeDecl.name)) {
			problemReporter().publicClassMustMatchFileName(referenceContext, typeDecl);
			// tolerate faulty main type name (91091), allow to proceed into type construction
		}
	}
}
protected ClassScope buildClassScope(Scope parent, TypeDeclaration typeDecl) {
	return new ClassScope(parent, typeDecl);
}
// GROOVY end
void checkAndSetImports() {
	if (referenceContext.imports == null) {
		imports = getDefaultImports();
		return;
	}

	// allocate the import array, add java.lang.* by default
	int numberOfStatements = referenceContext.imports.length;
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		if (((importReference.bits & ASTNode.OnDemand) != 0) && CharOperation.equals(JAVA_LANG, importReference.tokens) && !importReference.isStatic()) {
			numberOfImports--;
			break;
		}
	}
	// GROOVY start
	// old code:
//	ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
//	resolvedImports[0] = getDefaultImports()[0];
//	int index = 1;
	// new code:
	ImportBinding[] resolvedImports = null;
	int index = -1;
	ImportBinding[] defaultImportBindings = getDefaultImports();
	if (defaultImportBindings.length==1) {
		resolvedImports = new ImportBinding[numberOfImports];
		resolvedImports[0] = getDefaultImports()[0];
		index = 1;
	} else {
		resolvedImports = new ImportBinding[numberOfImports+defaultImportBindings.length-1];
		index = 0;
		for (int i=0;i<defaultImportBindings.length;i++) {
			resolvedImports[index++] = defaultImportBindings[i];
		}
	}
	// GROOVY end
	

	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		char[][] compoundName = importReference.tokens;

		// skip duplicates or imports of the current package
		for (int j = 0; j < index; j++) {
			ImportBinding resolved = resolvedImports[j];
			if (resolved.onDemand == ((importReference.bits & ASTNode.OnDemand) != 0) && resolved.isStatic() == importReference.isStatic())
				if (CharOperation.equals(compoundName, resolvedImports[j].compoundName))
					continue nextImport;
		}

		if ((importReference.bits & ASTNode.OnDemand) != 0) {
			if (CharOperation.equals(compoundName, currentPackageName))
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
	imports = resolvedImports;
}

/**
 * Perform deferred check specific to parameterized types: bound checks, supertype collisions
 */
// GROOVY start: made protected
protected
// GROOVY end
void checkParameterizedTypes() {
	if (compilerOptions().sourceLevel < ClassFileConstants.JDK1_5) return;

	for (int i = 0, length = topLevelTypes.length; i < length; i++) {
		ClassScope scope = topLevelTypes[i].scope;
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
	if (localType.constantPoolName() != null) {
		return localType.constantPoolName();
	}
	// delegates to the outermost enclosing classfile, since it is the only one with a global vision of its innertypes.

	if (constantPoolNameUsage == null)
		constantPoolNameUsage = new HashtableOfType();

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
		if (constantPoolNameUsage.get(candidateName) != null) {
			index ++;
		} else {
			constantPoolNameUsage.put(candidateName, localType);
			break;
		}
	}
	return candidateName;
}

void connectTypeHierarchy() {
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].scope.connectTypeHierarchy();
}
// GROOVY start: made protected
protected
// GROOVY end
void faultInImports() {
	if (this.typeOrPackageCache != null)
		return; // can be called when a field constant is resolved before static imports
	if (referenceContext.imports == null) {
		this.typeOrPackageCache = new HashtableOfObject(1);
		return;
	}

	// collect the top level type names if a single type import exists
	int numberOfStatements = referenceContext.imports.length;
	HashtableOfType typesBySimpleNames = null;
	for (int i = 0; i < numberOfStatements; i++) {
		if ((referenceContext.imports[i].bits & ASTNode.OnDemand) == 0) {
			typesBySimpleNames = new HashtableOfType(topLevelTypes.length + numberOfStatements);
			for (int j = 0, length = topLevelTypes.length; j < length; j++)
				typesBySimpleNames.put(topLevelTypes[j].sourceName, topLevelTypes[j]);
			break;
		}
	}

	// allocate the import array, add java.lang.* by default
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		if (((importReference.bits & ASTNode.OnDemand) != 0) && CharOperation.equals(JAVA_LANG, importReference.tokens) && !importReference.isStatic()) {
			numberOfImports--;
			break;
		}
	}

	// FIXASC (M2) revisit this code and the other piece that does the same job - there must be a neater way
	// GROOVY start
	// old code:
//	ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
//	resolvedImports[0] = getDefaultImports()[0];
//	int index = 1;
	// new code:
	ImportBinding[] resolvedImports = null;
	int index = -1;
	ImportBinding[] defaultImportBindings = getDefaultImports();
	if (defaultImportBindings.length==1) {
		resolvedImports = new ImportBinding[numberOfImports];
		resolvedImports[0] = getDefaultImports()[0];
		index = 1;
	} else {
		resolvedImports = new ImportBinding[numberOfImports+defaultImportBindings.length-1];
		index = 0;
		for (int i=0;i<defaultImportBindings.length;i++) {
			resolvedImports[index++] = defaultImportBindings[i];
		}
	}
	// GROOVY end

	// keep static imports with normal imports until there is a reason to split them up
	// on demand imports continue to be packages & types. need to check on demand type imports for fields/methods
	// single imports change from being just types to types or fields
	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		char[][] compoundName = importReference.tokens;

		// skip duplicates or imports of the current package
		for (int j = 0; j < index; j++) {
			ImportBinding resolved = resolvedImports[j];
			if (resolved.onDemand == ((importReference.bits & ASTNode.OnDemand) != 0) && resolved.isStatic() == importReference.isStatic()) {
				if (CharOperation.equals(compoundName, resolved.compoundName)) {
					problemReporter().unusedImport(importReference); // since skipped, must be reported now
					continue nextImport;
				}
			}
		}
		if ((importReference.bits & ASTNode.OnDemand) != 0) {
			if (CharOperation.equals(compoundName, currentPackageName)) {
				problemReporter().unusedImport(importReference); // since skipped, must be reported now
				continue nextImport;
			}

			Binding importBinding = findImport(compoundName, compoundName.length);
			if (!importBinding.isValidBinding()) {
				problemReporter().importProblem(importReference, importBinding);
				continue nextImport;
			}
			if (importReference.isStatic() && importBinding instanceof PackageBinding) {
				problemReporter().cannotImportPackage(importReference);
				continue nextImport;
			}
			resolvedImports[index++] = new ImportBinding(compoundName, true, importBinding, importReference);
		} else {
			Binding importBinding = findSingleImport(compoundName, Binding.TYPE | Binding.FIELD | Binding.METHOD, importReference.isStatic());
			if (!importBinding.isValidBinding()) {
				if (importBinding.problemId() == ProblemReasons.Ambiguous) {
					// keep it unless a duplicate can be found below
				} else {
					// GROOVY start: delegate to overridable helper
					// old code:
					// problemReporter().importProblem(importReference, importBinding);
					// new code:
					recordImportProblem(importReference, importBinding);
					// GROOVY end
					continue nextImport;
				}
			}
			if (importBinding instanceof PackageBinding) {
				problemReporter().cannotImportPackage(importReference);
				continue nextImport;
			}
			ReferenceBinding conflictingType = null;
			if (importBinding instanceof MethodBinding) {
				conflictingType = (ReferenceBinding) getType(compoundName, compoundName.length);
				if (!conflictingType.isValidBinding())
					conflictingType = null;
			}
			// collisions between an imported static field & a type should be checked according to spec... but currently not by javac
			if (importBinding instanceof ReferenceBinding || conflictingType != null) {
				ReferenceBinding referenceBinding = conflictingType == null ? (ReferenceBinding) importBinding : conflictingType;
				ReferenceBinding typeToCheck = referenceBinding.problemId() == ProblemReasons.Ambiguous
					? ((ProblemReferenceBinding) referenceBinding).closestMatch
					: referenceBinding;
				if (importReference.isTypeUseDeprecated(typeToCheck, this))
					problemReporter().deprecatedType(typeToCheck, importReference);

				ReferenceBinding existingType = typesBySimpleNames.get(compoundName[compoundName.length - 1]);
				if (existingType != null) {
					// duplicate test above should have caught this case, but make sure
					if (existingType == referenceBinding)
						continue nextImport;
					// either the type collides with a top level type or another imported type
					for (int j = 0, length = topLevelTypes.length; j < length; j++) {
						if (CharOperation.equals(topLevelTypes[j].sourceName, existingType.sourceName)) {
							problemReporter().conflictingImport(importReference);
							continue nextImport;
						}
					}
					problemReporter().duplicateImport(importReference);
					continue nextImport;
				}
				// GROOVY start: delegate to a method to ask for the shortname to use
				// old code:
				// typesBySimpleNames.put(compoundName[compoundName.length - 1], referenceBinding);
				// new code:
				typesBySimpleNames.put(importReference.getSimpleName(),referenceBinding);
				// GROOVY end
			} else if (importBinding instanceof FieldBinding) {
				for (int j = 0; j < index; j++) {
					ImportBinding resolved = resolvedImports[j];
					// find other static fields with the same name
					if (resolved.isStatic() && resolved.resolvedImport instanceof FieldBinding && importBinding != resolved.resolvedImport) {
						if (CharOperation.equals(compoundName[compoundName.length - 1], resolved.compoundName[resolved.compoundName.length - 1])) {
							problemReporter().duplicateImport(importReference);
							continue nextImport;
						}
					}
				}
			}
			resolvedImports[index++] = conflictingType == null
				? new ImportBinding(compoundName, false, importBinding, importReference)
				: new ImportConflictBinding(compoundName, importBinding, conflictingType, importReference);
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (resolvedImports.length > index)
		System.arraycopy(resolvedImports, 0, resolvedImports = new ImportBinding[index], 0, index);
	imports = resolvedImports;

	int length = imports.length;
	this.typeOrPackageCache = new HashtableOfObject(length);
	for (int i = 0; i < length; i++) {
		ImportBinding binding = imports[i];
		if (!binding.onDemand && binding.resolvedImport instanceof ReferenceBinding || binding instanceof ImportConflictBinding)
			// GROOVY start
			// oldcode
			// this.typeOrPackageCache.put(binding.compoundName[binding.compoundName.length - 1], binding);
			// new code
			this.typeOrPackageCache.put(getSimpleName(binding), binding);
			// GROOVY end
	}
}
public void faultInTypes() {
	faultInImports();

	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].faultInTypesForFieldsAndMethods();
}
// GROOVY start
protected void recordImportProblem(ImportReference importReference, Binding importBinding) {
	problemReporter().importProblem(importReference, importBinding);	
}
// GROOVY end

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

	Binding binding = environment.getTopLevelPackage(compoundName[0]);
	int i = 1;
	foundNothingOrType: if (binding != null) {
		PackageBinding packageBinding = (PackageBinding) binding;
		while (i < length) {
			binding = packageBinding.getTypeOrPackage(compoundName[i++]);
			if (binding == null || !binding.isValidBinding()) {
				binding = null;
				break foundNothingOrType;
			}
			if (!(binding instanceof PackageBinding))
				break foundNothingOrType;

			packageBinding = (PackageBinding) binding;
		}
		return packageBinding;
	}

	ReferenceBinding type;
	if (binding == null) {
		if (environment.defaultPackage == null || compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
		type = findType(compoundName[0], environment.defaultPackage, environment.defaultPackage);
		if (type == null || !type.isValidBinding())
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
		i = 1; // reset to look for member types inside the default package type
	} else {
		type = (ReferenceBinding) binding;
	}

	while (i < length) {
		type = (ReferenceBinding)environment.convertToRawType(type, false /*do not force conversion of enclosing types*/); // type imports are necessarily raw for all except last
		if (!type.canBeSeenBy(fPackage))
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), type, ProblemReasons.NotVisible);

		char[] name = compoundName[i++];
		// does not look for inherited member types on purpose, only immediate members
		type = type.getMemberType(name);
		if (type == null)
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
	}
	// GROOVY start:
	// old code:
    //	if (!type.canBeSeenBy(fPackage))
	// new code:
	if (!canBeSeenBy(type,fPackage))
	// GROOVY end
		return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotVisible);
	return type;
}
// GROOVY start: new method for determining visibility - rules are relaxed for groovy
protected boolean canBeSeenBy(ReferenceBinding type, PackageBinding fPackage) {
	return type.canBeSeenBy(fPackage);
}
// GROOVY end
// GROOVY start: made protected
protected 
// GROOVY end
Binding findSingleImport(char[][] compoundName, int mask, boolean findStaticImports) {
	if (compoundName.length == 1) {
		// findType records the reference
		// the name cannot be a package
		if (environment.defaultPackage == null || compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
			return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
		ReferenceBinding typeBinding = findType(compoundName[0], environment.defaultPackage, fPackage);
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
		Binding temp = ((PackageBinding) binding).getTypeOrPackage(name);
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
	if (type.isValidBinding() && !type.canBeSeenBy(fPackage))
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
				if (method.isStatic() && method.canBeSeenBy(fPackage))
					return method;
			}
		}
	} while ((currentType = currentType.superclass()) != null);
	return null;
}
// GROOVY start: made protected so groovy compilation units can have more default imports
protected
// GROOVY end
ImportBinding[] getDefaultImports() {
	// initialize the default imports if necessary... share the default java.lang.* import
	if (environment.defaultImports != null) return environment.defaultImports;

	Binding importBinding = environment.getTopLevelPackage(JAVA);
	if (importBinding != null)
		importBinding = ((PackageBinding) importBinding).getTypeOrPackage(JAVA_LANG[1]);

	if (importBinding == null || !importBinding.isValidBinding()) {
		// create a proxy for the missing BinaryType
		problemReporter().isClassPathCorrect(
			JAVA_LANG_OBJECT, 
			this.referenceContext, 
			environment.missingClassFileLocation);		
		BinaryTypeBinding missingObject = environment.createMissingType(null, JAVA_LANG_OBJECT);
		importBinding = missingObject.fPackage;
	}

	return environment.defaultImports = new ImportBinding[] {new ImportBinding(JAVA_LANG, true, importBinding, null)};
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
	ProblemReporter problemReporter = referenceContext.problemReporter;
	problemReporter.referenceContext = referenceContext;
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
// GROOVY start: made visible to subtypes
public
// GROOVY end
void recordQualifiedReference(char[][] qualifiedName) { 
	if (qualifiedReferences == null) return; // not recording dependencies

	int length = qualifiedName.length;
	if (length > 1) {
		while (!qualifiedReferences.contains(qualifiedName)) {
			qualifiedReferences.add(qualifiedName);
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
		recordSimpleReference(qualifiedName[0]);
	}
}
void recordReference(char[][] qualifiedEnclosingName, char[] simpleName) {
	recordQualifiedReference(qualifiedEnclosingName);
	recordSimpleReference(simpleName);
}
void recordReference(ReferenceBinding type, char[] simpleName) {
	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null)
		recordReference(actualType.compoundName, simpleName);
}
// GROOVY start: visible
public
// GROOVY end
void recordSimpleReference(char[] simpleName) {
	if (simpleNameReferences == null) return; // not recording dependencies

	if (!simpleNameReferences.contains(simpleName))
		simpleNameReferences.add(simpleName);
}
void recordSuperTypeReference(TypeBinding type) {
	if (referencedSuperTypes == null) return; // not recording dependencies

	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null && !referencedSuperTypes.containsIdentical(actualType))
		referencedSuperTypes.add(actualType);
}
public void recordTypeConversion(TypeBinding superType, TypeBinding subType) {
	recordSuperTypeReference(subType); // must record the hierarchy of the subType that is converted to the superType
}
void recordTypeReference(TypeBinding type) {
	if (referencedTypes == null) return; // not recording dependencies

	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null && !referencedTypes.containsIdentical(actualType))
		referencedTypes.add(actualType);
}
void recordTypeReferences(TypeBinding[] types) {
	if (referencedTypes == null) return; // not recording dependencies
	if (types == null || types.length == 0) return;

	for (int i = 0, max = types.length; i < max; i++) {
		// No need to record supertypes of method arguments & thrown exceptions, just the compoundName
		// If a field/method is retrieved from such a type then a separate call does the job
		ReferenceBinding actualType = typeToRecord(types[i]);
		if (actualType != null && !referencedTypes.containsIdentical(actualType))
			referencedTypes.add(actualType);
	}
}
Binding resolveSingleImport(ImportBinding importBinding, int mask) {
	if (importBinding.resolvedImport == null) {
		importBinding.resolvedImport = findSingleImport(importBinding.compoundName, mask, importBinding.isStatic());
		if (!importBinding.resolvedImport.isValidBinding() || importBinding.resolvedImport instanceof PackageBinding) {
			if (importBinding.resolvedImport.problemId() == ProblemReasons.Ambiguous)
				return importBinding.resolvedImport;
			if (this.imports != null) {
				ImportBinding[] newImports = new ImportBinding[imports.length - 1];
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
	for (int i = 0; i < referencedSuperTypes.size; i++) { // grows as more types are added
		ReferenceBinding type = (ReferenceBinding) referencedSuperTypes.elementAt(i);
		if (!referencedTypes.containsIdentical(type))
			referencedTypes.add(type);

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

	for (int i = 0, l = referencedTypes.size; i < l; i++) {
		ReferenceBinding type = (ReferenceBinding) referencedTypes.elementAt(i);
		if (!type.isLocalType())
			recordQualifiedReference(type.isMemberType()
				? CharOperation.splitOn('.', type.readableName())
				: type.compoundName);
	}

	int size = qualifiedReferences.size;
	char[][][] qualifiedRefs = new char[size][][];
	for (int i = 0; i < size; i++)
		qualifiedRefs[i] = qualifiedReferences.elementAt(i);
	referenceContext.compilationResult.qualifiedReferences = qualifiedRefs;

	size = simpleNameReferences.size;
	char[][] simpleRefs = new char[size][];
	for (int i = 0; i < size; i++)
		simpleRefs[i] = simpleNameReferences.elementAt(i);
	referenceContext.compilationResult.simpleNameReferences = simpleRefs;
}
public String toString() {
	return "--- CompilationUnit Scope : " + new String(referenceContext.getFileName()); //$NON-NLS-1$
}
private ReferenceBinding typeToRecord(TypeBinding type) {
	if (type.isArrayType())
		type = ((ArrayBinding) type).leafComponentType;

	switch (type.kind()) {
		case Binding.BASE_TYPE :
		case Binding.TYPE_PARAMETER :
		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE :
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
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].verifyMethods(verifier);
}
// GROOVY start: new method so more interfaces can be introduced
public void augmentTypeHierarchy() {
	// nothing to do for standard Java
}
public boolean checkTargetCompatibility() {
	return true;
}
public boolean scannerAvailable() {
	return true;
}
public boolean reportInvalidType() {
	return true;
}
public boolean canSeeEverything() {
	return false;
}
//GROOVY end
}
