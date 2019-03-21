/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								bug 388281 - [compiler][null] inheritance of null annotations as an option
 *								bug 388795 - [compiler] detection of name clash depends on order of super interfaces
 *								bug 388739 - [1.8][compiler] consider default methods when detecting whether a class needs to be declared abstract
 *								bug 390883 - [1.8][compiler] Unable to override default method
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								bug 401246 - [1.8][compiler] abstract class method should now trump conflicting default methods
 *								bug 401796 - [1.8][compiler] don't treat default methods as overriding an independent inherited abstract method
 *								bug 403867 - [1.8][compiler] Suspect error about duplicate default methods
 *								bug 391376 - [1.8] check interaction of default methods with bridge methods and generics
 *								bug 395681 - [compiler] Improve simulation of javac6 behavior from bug 317719 after fixing bug 388795
 *								bug 409473 - [compiler] JDT cannot compile against JRE 1.8
 *								Bug 420080 - [1.8] Overridden Default method is reported as duplicated
 *								Bug 404690 - [1.8][compiler] revisit bridge generation after VM bug is fixed
 *								Bug 410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 390889 - [1.8][compiler] Evaluate options to support 1.7- projects against 1.8 JRE.
 *								Bug 440773 - [1.8][null]DefaultLocation.RETURN_TYPE erroneously affects method parameters in @NonNullByDefault
 *								Bug 435805 - [1.8][compiler][null] Java 8 compiler does not recognize declaration style null annotations
 *								Bug 446442 - [1.8] merge null annotations from super methods
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;


import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.Sorting;

class MethodVerifier15 extends MethodVerifier {

MethodVerifier15(LookupEnvironment environment) {
	super(environment);
}
// Given `overridingMethod' which overrides `inheritedMethod' answer whether some subclass method that
// differs in erasure from overridingMethod could override `inheritedMethod'
protected boolean canOverridingMethodDifferInErasure(MethodBinding overridingMethod, MethodBinding inheritedMethod) {
	if (overridingMethod.areParameterErasuresEqual(inheritedMethod))
		return false;  // no further change in signature is possible due to parameterization.
	if (overridingMethod.declaringClass.isRawType())
		return false;  // no parameterization is happening anyways.
	return true;
}
boolean canSkipInheritedMethods() {
	if (this.type.superclass() != null)
		if (this.type.superclass().isAbstract() || this.type.superclass().isParameterizedType())
			return false;
	return this.type.superInterfaces() == Binding.NO_SUPERINTERFACES;
}
boolean canSkipInheritedMethods(MethodBinding one, MethodBinding two) {
	return two == null // already know one is not null
		|| (TypeBinding.equalsEquals(one.declaringClass, two.declaringClass) && !one.declaringClass.isParameterizedType());
}
void checkConcreteInheritedMethod(MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	super.checkConcreteInheritedMethod(concreteMethod, abstractMethods);
	boolean analyseNullAnnotations = this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled;
	// TODO (stephan): unclear if this srcMethod is actually needed
	AbstractMethodDeclaration srcMethod = null;
	if (analyseNullAnnotations && this.type.equals(concreteMethod.declaringClass)) // is currentMethod from the current type?
		srcMethod = concreteMethod.sourceMethod();
	boolean useTypeAnnotations = this.environment.usesNullTypeAnnotations();
	boolean hasReturnNonNullDefault = analyseNullAnnotations && concreteMethod.hasNonNullDefaultFor(Binding.DefaultLocationReturnType, useTypeAnnotations, srcMethod);
	boolean hasParameterNonNullDefault = analyseNullAnnotations && concreteMethod.hasNonNullDefaultFor(Binding.DefaultLocationParameter, useTypeAnnotations, srcMethod);

	for (int i = 0, l = abstractMethods.length; i < l; i++) {
		MethodBinding abstractMethod = abstractMethods[i];
		if (concreteMethod.isVarargs() != abstractMethod.isVarargs())
			problemReporter().varargsConflict(concreteMethod, abstractMethod, this.type);

		// so the parameters are equal and the return type is compatible b/w the currentMethod & the substituted inheritedMethod
		MethodBinding originalInherited = abstractMethod.original();
		if (TypeBinding.notEquals(originalInherited.returnType, concreteMethod.returnType))
			if (!isAcceptableReturnTypeOverride(concreteMethod, abstractMethod))
				problemReporter().unsafeReturnTypeOverride(concreteMethod, originalInherited, this.type);

		// check whether bridge method is already defined above for interface methods
		// skip generation of bridge method for current class & method if an equivalent
		// bridge will be/would have been generated in the context of the super class since
		// the bridge itself will be inherited. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=298362
		if (originalInherited.declaringClass.isInterface()) {
			if ((TypeBinding.equalsEquals(concreteMethod.declaringClass, this.type.superclass) && this.type.superclass.isParameterizedType() && !areMethodsCompatible(concreteMethod, originalInherited))
				|| this.type.superclass.erasure().findSuperTypeOriginatingFrom(originalInherited.declaringClass) == null)
					this.type.addSyntheticBridgeMethod(originalInherited, concreteMethod.original());
		}
		if (analyseNullAnnotations && !concreteMethod.isStatic() && !abstractMethod.isStatic()) {
			checkNullSpecInheritance(concreteMethod, srcMethod, hasReturnNonNullDefault, hasParameterNonNullDefault, true, abstractMethod, abstractMethods, this.type.scope, null);
		}
	}
}
void checkForBridgeMethod(MethodBinding currentMethod, MethodBinding inheritedMethod, MethodBinding[] allInheritedMethods) {
	if (currentMethod.isVarargs() != inheritedMethod.isVarargs())
		problemReporter(currentMethod).varargsConflict(currentMethod, inheritedMethod, this.type);

	// so the parameters are equal and the return type is compatible b/w the currentMethod & the substituted inheritedMethod
	MethodBinding originalInherited = inheritedMethod.original();
	if (TypeBinding.notEquals(originalInherited.returnType, currentMethod.returnType))
		if (!isAcceptableReturnTypeOverride(currentMethod, inheritedMethod))
			problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, originalInherited, this.type);

	MethodBinding bridge = this.type.addSyntheticBridgeMethod(originalInherited, currentMethod.original());
	if (bridge != null) {
		for (int i = 0, l = allInheritedMethods == null ? 0 : allInheritedMethods.length; i < l; i++) {
			if (allInheritedMethods[i] != null && detectInheritedNameClash(originalInherited, allInheritedMethods[i].original()))
				return;
		}
		// See if the new bridge clashes with any of the user methods of the class. For this check
		// we should check for "method descriptor clash" and not just "method signature clash". Really
		// what we are checking is whether there is a contention for the method dispatch table slot.
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=293615.
		MethodBinding[] current = (MethodBinding[]) this.currentMethods.get(bridge.selector);
		for (int i = current.length - 1; i >= 0; --i) {
			final MethodBinding thisMethod = current[i];
			if (thisMethod.areParameterErasuresEqual(bridge) && TypeBinding.equalsEquals(thisMethod.returnType.erasure(), bridge.returnType.erasure())) {
				// use inherited method for problem reporting.
				problemReporter(thisMethod).methodNameClash(thisMethod, inheritedMethod.declaringClass.isRawType() ? inheritedMethod : inheritedMethod.original(), ProblemSeverities.Error);
				return;	
			}
		}
	}
}
void checkForNameClash(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	// sent from checkMethods() to compare a current method and an inherited method that are not 'equal'

	// error cases:
	//		abstract class AA<E extends Comparable> { abstract void test(E element); }
	//		class A extends AA<Integer> { public void test(Integer i) {} }
	//		public class B extends A { public void test(Comparable i) {} }
	//		interface I<E extends Comparable> { void test(E element); }
	//		class A implements I<Integer> { public void test(Integer i) {} }
	//		public class B extends A { public void test(Comparable i) {} }

	//		abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {
	//			public boolean equalTo(Integer other) { return true; }
	//		}
	//		interface Equivalent<T> { boolean equalTo(T other); }
	//		interface EqualityComparable<T> { boolean equalTo(T other); }

	//		class Y implements EqualityComparable, Equivalent<String>{
	//			public boolean equalTo(String other) { return true; }
	//			public boolean equalTo(Object other) { return true; }
	//		}
	//		interface Equivalent<T> { boolean equalTo(T other); }
	//		interface EqualityComparable { boolean equalTo(Object other); }

	//		class A<T extends Number> { void m(T t) {} }
	//		class B<S extends Integer> extends A<S> { void m(S t) {}}
	//		class D extends B<Integer> { void m(Number t) {}    void m(Integer t) {} }

	//		inheritedMethods does not include I.test since A has a valid implementation
	//		interface I<E extends Comparable<E>> { void test(E element); }
	//		class A implements I<Integer> { public void test(Integer i) {} }
	//		class B extends A { public void test(Comparable i) {} }

	if (inheritedMethod.isStatic() || currentMethod.isStatic()) {
		MethodBinding original = inheritedMethod.original(); // can be the same as inherited
		if (this.type.scope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_7 && currentMethod.areParameterErasuresEqual(original)) {
			problemReporter(currentMethod).methodNameClashHidden(currentMethod, inheritedMethod.declaringClass.isRawType() ? inheritedMethod : original);
		}
		return; // no chance of bridge method's clashing
	}

	if (!detectNameClash(currentMethod, inheritedMethod, false)) { // check up the hierarchy for skipped inherited methods
		TypeBinding[] currentParams = currentMethod.parameters;
		TypeBinding[] inheritedParams = inheritedMethod.parameters;
		int length = currentParams.length;
		if (length != inheritedParams.length) return; // no match

		for (int i = 0; i < length; i++)
			if (TypeBinding.notEquals(currentParams[i], inheritedParams[i]))
				if (currentParams[i].isBaseType() != inheritedParams[i].isBaseType() || !inheritedParams[i].isCompatibleWith(currentParams[i]))
					return; // no chance that another inherited method's bridge method can collide

		ReferenceBinding[] interfacesToVisit = null;
		int nextPosition = 0;
		ReferenceBinding superType = inheritedMethod.declaringClass;
		ReferenceBinding[] itsInterfaces = superType.superInterfaces();
		if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
			nextPosition = itsInterfaces.length;
			interfacesToVisit = itsInterfaces;
		}
		superType = superType.superclass(); // now start with its superclass
		while (superType != null && superType.isValidBinding()) {
			MethodBinding[] methods = superType.getMethods(currentMethod.selector);
			for (int m = 0, n = methods.length; m < n; m++) {
				MethodBinding substitute = computeSubstituteMethod(methods[m], currentMethod);
				if (substitute != null && !isSubstituteParameterSubsignature(currentMethod, substitute) && detectNameClash(currentMethod, substitute, true))
					return;
			}
			if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
				if (interfacesToVisit == null) {
					interfacesToVisit = itsInterfaces;
					nextPosition = interfacesToVisit.length;
				} else {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
			superType = superType.superclass();
		}

		for (int i = 0; i < nextPosition; i++) {
			superType = interfacesToVisit[i];
			if (superType.isValidBinding()) {
				MethodBinding[] methods = superType.getMethods(currentMethod.selector);
				for (int m = 0, n = methods.length; m < n; m++){
					MethodBinding substitute = computeSubstituteMethod(methods[m], currentMethod);
					if (substitute != null && !isSubstituteParameterSubsignature(currentMethod, substitute) && detectNameClash(currentMethod, substitute, true))
						return;
				}
				if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
		}
	}
}
void checkInheritedMethods(MethodBinding inheritedMethod, MethodBinding otherInheritedMethod) {

	// the 2 inherited methods clash because of a parameterized type overrides a raw type
	//		interface I { void foo(A a); }
	//		class Y { void foo(A<String> a) {} }
	//		abstract class X extends Y implements I { }
	//		class A<T> {}
	// in this case the 2 inherited methods clash because of type variables
	//		interface I { <T, S> void foo(T t); }
	//		class Y { <T> void foo(T t) {} }
	//		abstract class X extends Y implements I {}

	if (inheritedMethod.isStatic()) return;
	if (this.environment.globalOptions.complianceLevel < ClassFileConstants.JDK1_7 && inheritedMethod.declaringClass.isInterface())
		return;  // JDK7 checks for name clashes in interface inheritance, while JDK6 and below don't. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229

	detectInheritedNameClash(inheritedMethod.original(), otherInheritedMethod.original());
}
// 8.4.8.4
void checkInheritedMethods(MethodBinding[] methods, int length, boolean[] isOverridden, boolean[] isInherited) {
	boolean continueInvestigation = true;
	MethodBinding concreteMethod = null;
	MethodBinding abstractSuperClassMethod = null;
	boolean playingTrump = false; // invariant: playingTrump => (concreteMethod == null)
	for (int i = 0; i < length; i++) {
		if (!methods[i].declaringClass.isInterface()
				&& TypeBinding.notEquals(methods[i].declaringClass, this.type)
				&& methods[i].isAbstract())
		{
			abstractSuperClassMethod = methods[i];
			break;
		}
	}
	for (int i = 0; i < length; i++) {
		// methods not inherited as of 8.4.8 cannot create a name clash,
		// but could still cause errors against return types etc. (below)
		if (isInherited[i] && !methods[i].isAbstract()) {
			// 8.4.8.4 defines an exception for default methods if
			// (a) there exists an abstract method declared in a superclass of C and inherited by C
			// (b) that is override-equivalent with the two methods.
			if (methods[i].isDefaultMethod()
					&& abstractSuperClassMethod != null							// condition (a)
					&& areParametersEqual(abstractSuperClassMethod, methods[i]) // condition (b)...
					&& concreteMethod == null) {
				// skip, class method trumps this default method (concreteMethod remains null)
				playingTrump = true;
			} else {
				playingTrump = false;
				if (concreteMethod != null) {
					// re-checking compatibility is needed for https://bugs.eclipse.org/346029
					if (isOverridden[i] && areMethodsCompatible(concreteMethod, methods[i])) {
						continue;
					}
					// https://bugs.eclipse.org/195802 with https://bugs.eclipse.org/410325
					// If a replace method (from findReplacedMethod()) is the rawified version of another
					// don't count this as duplicates:
					//   (Not asking ParameterizedGenericMethodBinding.isRawMethod(),
					//    because that is true only for methods of a RawTypeBinding,
					//    but here we look for rawness regarding the method's type variables).
					if (TypeBinding.equalsEquals(concreteMethod.declaringClass, methods[i].declaringClass) 
							&& concreteMethod.typeVariables.length != methods[i].typeVariables.length) 
					{
						if (concreteMethod.typeVariables == Binding.NO_TYPE_VARIABLES
								&& concreteMethod.original() == methods[i])
							continue;
						if (methods[i].typeVariables == Binding.NO_TYPE_VARIABLES
								&& methods[i].original() == concreteMethod)
							continue;
					}

					problemReporter().duplicateInheritedMethods(this.type, concreteMethod, methods[i],
											this.environment.globalOptions.sourceLevel >= ClassFileConstants.JDK1_8);
					continueInvestigation = false;
				}
				concreteMethod = methods[i];
			}
		}
	}
	if (continueInvestigation) {
		if (playingTrump) {
			// multiple abstract & default methods are OK on this branch, but then the class must be declared abstract:
			if (!this.type.isAbstract()) {
				problemReporter().abstractMethodMustBeImplemented(this.type, abstractSuperClassMethod);
				return;
			}
		} else {
			if (concreteMethod != null && concreteMethod.isDefaultMethod()) {
				if (this.environment.globalOptions.complianceLevel >= ClassFileConstants.JDK1_8) {
					if (!checkInheritedDefaultMethods(methods, isOverridden, length))
						return;
				}
			}
		}
		super.checkInheritedMethods(methods, length, isOverridden, isInherited);
	}
}
boolean checkInheritedDefaultMethods(MethodBinding[] methods, boolean[] isOverridden, int length) {
	// JLS8  9.4.1.3 (interface) and  8.4.8.4 (class):
	// default method clashes with other inherited method which is override-equivalent 
	if (length < 2) return true;
	boolean ok = true;
	findDefaultMethod: for (int i=0; i<length; i++) {
		if (methods[i].isDefaultMethod() && !isOverridden[i]) {
			findEquivalent: for (int j=0; j<length; j++) {
				if (j == i || isOverridden[j]) continue findEquivalent;
				if (isMethodSubsignature(methods[i], methods[j])) {
					if (!doesMethodOverride(methods[i], methods[j]) && !doesMethodOverride(methods[j], methods[i])) { 
						problemReporter().inheritedDefaultMethodConflictsWithOtherInherited(this.type, methods[i], methods[j]);
						ok = false;
						continue findDefaultMethod;
					}
				}
			}
		}
	}
	return ok;
}
boolean checkInheritedReturnTypes(MethodBinding method, MethodBinding otherMethod) {
	if (areReturnTypesCompatible(method, otherMethod)) return true;

	/* We used to have some checks here to see if we would have already blamed the super type and if so avoid blaming
	   the current type again. I have gotten rid of them as they in fact short circuit error reporting in cases where
	   they should not. This means that occasionally we would report the error twice - the diagnostics is valid however,
	   albeit arguably redundant. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=334313. For an example of a test
	   where we do this extra reporting see org.eclipse.jdt.core.tests.compiler.regression.MethodVerifyTest.test159()
	 */
	// check to see if this is just a warning, if so report it & skip to next method
	if (isUnsafeReturnTypeOverride(method, otherMethod)) {
		if (!method.declaringClass.implementsInterface(otherMethod.declaringClass, false))
			problemReporter(method).unsafeReturnTypeOverride(method, otherMethod, this.type);
		return true;
	}

	return false;
}
void checkAgainstInheritedMethods(MethodBinding currentMethod, MethodBinding[] methods, int length, MethodBinding[] allInheritedMethods)
{
	super.checkAgainstInheritedMethods(currentMethod, methods, length, allInheritedMethods);
	CompilerOptions options = this.environment.globalOptions;
	if (options.isAnnotationBasedNullAnalysisEnabled 
			&& (currentMethod.tagBits & TagBits.IsNullnessKnown) == 0)
	{
		// if annotations are inherited these have been checked during STB.resolveTypesFor() (for methods explicit in this.type)
		AbstractMethodDeclaration srcMethod = null;
		if (this.type.equals(currentMethod.declaringClass)) // is currentMethod from the current type?
			srcMethod = currentMethod.sourceMethod();
		boolean useTypeAnnotations = this.environment.usesNullTypeAnnotations();
		boolean hasReturnNonNullDefault = currentMethod.hasNonNullDefaultFor(Binding.DefaultLocationReturnType, useTypeAnnotations, srcMethod);
		boolean hasParameterNonNullDefault = currentMethod.hasNonNullDefaultFor(Binding.DefaultLocationParameter, useTypeAnnotations, srcMethod);
		for (int i = length; --i >= 0;)
			if (!currentMethod.isStatic() && !methods[i].isStatic())
				checkNullSpecInheritance(currentMethod, srcMethod, hasReturnNonNullDefault, hasParameterNonNullDefault, true, methods[i], methods, this.type.scope, null);
	}
}

void checkNullSpecInheritance(MethodBinding currentMethod, AbstractMethodDeclaration srcMethod, 
		boolean hasReturnNonNullDefault, boolean hasParameterNonNullDefault, boolean complain, MethodBinding inheritedMethod, MethodBinding[] allInherited, Scope scope, InheritedNonNullnessInfo[] inheritedNonNullnessInfos)
{
	complain &= !currentMethod.isConstructor();
	if (!hasReturnNonNullDefault && !hasParameterNonNullDefault && !complain && !this.environment.globalOptions.inheritNullAnnotations) {
		// nothing to be done, take the shortcut
		currentMethod.tagBits |= TagBits.IsNullnessKnown;
		return;
	}
	// in this context currentMethod can be inherited, too. Recurse if needed.
	if (TypeBinding.notEquals(currentMethod.declaringClass, this.type) 
			&& (currentMethod.tagBits & TagBits.IsNullnessKnown) == 0) 
	{
		this.buddyImplicitNullAnnotationsVerifier.checkImplicitNullAnnotations(currentMethod, srcMethod, complain, scope);
	}
	super.checkNullSpecInheritance(currentMethod, srcMethod, hasReturnNonNullDefault, hasParameterNonNullDefault, complain, inheritedMethod, allInherited, scope, inheritedNonNullnessInfos);
}

void reportRawReferences() {
	CompilerOptions compilerOptions = this.type.scope.compilerOptions();
	if (compilerOptions.sourceLevel < ClassFileConstants.JDK1_5 // shouldn't whine at all
			|| compilerOptions.reportUnavoidableGenericTypeProblems) { // must have already whined 
		return;
	}
	/* Code below is only for a method that does not override/implement a super type method. If it were to,
	   it would have been handled in checkAgainstInheritedMethods.
	*/
	Object [] methodArray = this.currentMethods.valueTable;
	for (int s = methodArray.length; --s >= 0;) {
		if (methodArray[s] == null) continue;
		MethodBinding[] current = (MethodBinding[]) methodArray[s];
		for (int i = 0, length = current.length; i < length; i++) {
			MethodBinding currentMethod = current[i];
			if ((currentMethod.modifiers & (ExtraCompilerModifiers.AccImplementing | ExtraCompilerModifiers.AccOverriding)) == 0) {
				AbstractMethodDeclaration methodDecl = currentMethod.sourceMethod();
				if (methodDecl == null) return;
				TypeBinding [] parameterTypes = currentMethod.parameters;
				Argument[] arguments = methodDecl.arguments;
				for (int j = 0, size = currentMethod.parameters.length; j < size; j++) {
					TypeBinding parameterType = parameterTypes[j];
					Argument arg = arguments[j];
					if (parameterType.leafComponentType().isRawType()
						&& compilerOptions.getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore
			      		&& (arg.type.bits & ASTNode.IgnoreRawTypeCheck) == 0) {
						methodDecl.scope.problemReporter().rawTypeReference(arg.type, parameterType);
			    	}
				}
				if (!methodDecl.isConstructor() && methodDecl instanceof MethodDeclaration) {
					TypeReference returnType = ((MethodDeclaration) methodDecl).returnType;
					TypeBinding methodType = currentMethod.returnType;
					if (returnType != null) {
						if (methodType.leafComponentType().isRawType()
								&& compilerOptions.getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore
								&& (returnType.bits & ASTNode.IgnoreRawTypeCheck) == 0) {
							methodDecl.scope.problemReporter().rawTypeReference(returnType, methodType);
						}
					}
				}
			}
		}
	}
}
public void reportRawReferences(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	CompilerOptions compilerOptions = this.type.scope.compilerOptions();
	if (compilerOptions.sourceLevel < ClassFileConstants.JDK1_5 // shouldn't whine at all
			|| compilerOptions.reportUnavoidableGenericTypeProblems) { // must have already whined 
		return;
	}
	AbstractMethodDeclaration methodDecl = currentMethod.sourceMethod();
	if (methodDecl == null) return;
	TypeBinding [] parameterTypes = currentMethod.parameters;
	TypeBinding [] inheritedParameterTypes = inheritedMethod.parameters;
	Argument[] arguments = methodDecl.arguments;
	for (int j = 0, size = currentMethod.parameters.length; j < size; j++) {
		TypeBinding parameterType = parameterTypes[j];
		TypeBinding inheritedParameterType = inheritedParameterTypes[j];
		Argument arg = arguments[j];
		if (parameterType.leafComponentType().isRawType()) {
			if (inheritedParameterType.leafComponentType().isRawType()) {
				arg.binding.tagBits |= TagBits.ForcedToBeRawType;
			} else {
				if (compilerOptions.getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore
						&& (arg.type.bits & ASTNode.IgnoreRawTypeCheck) == 0) {
					methodDecl.scope.problemReporter().rawTypeReference(arg.type, parameterType);
				}
			}
    	}
    }
	TypeReference returnType = null;
	if (!methodDecl.isConstructor() && methodDecl instanceof MethodDeclaration && (returnType = ((MethodDeclaration) methodDecl).returnType) != null) {
		final TypeBinding inheritedMethodType = inheritedMethod.returnType;
		final TypeBinding methodType = currentMethod.returnType;
		if (methodType.leafComponentType().isRawType()) {
			if (inheritedMethodType.leafComponentType().isRawType()) {
				// 
			} else {
				if ((returnType.bits & ASTNode.IgnoreRawTypeCheck) == 0
						&& compilerOptions.getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
					methodDecl.scope.problemReporter().rawTypeReference(returnType, methodType);
				}
			}
		}
	}
 }

void checkMethods() {
	boolean mustImplementAbstractMethods = mustImplementAbstractMethods();
	boolean skipInheritedMethods = mustImplementAbstractMethods && canSkipInheritedMethods(); // have a single concrete superclass so only check overridden methods
	boolean isOrEnclosedByPrivateType = this.type.isOrEnclosedByPrivateType();
	char[][] methodSelectors = this.inheritedMethods.keyTable;
	nextSelector : for (int s = methodSelectors.length; --s >= 0;) {
		if (methodSelectors[s] == null) continue nextSelector;
		MethodBinding[] current = (MethodBinding[]) this.currentMethods.get(methodSelectors[s]);
		MethodBinding[] inherited = (MethodBinding[]) this.inheritedMethods.valueTable[s];
		// ensure that if we have a concrete method this shows up at position [0]:
		inherited = Sorting.concreteFirst(inherited, inherited.length);
		
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660, if current type is exposed,
		// inherited methods of super classes are too. current != null case handled below.
		if (current == null && !isOrEnclosedByPrivateType) {
			int length = inherited.length;
			for (int i = 0; i < length; i++){
				inherited[i].original().modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
			}
		}
		if (current == null && this.type.isPublic()) {
			int length = inherited.length;
			for (int i = 0; i < length; i++) {
				MethodBinding inheritedMethod = inherited[i];
				if (inheritedMethod.isPublic() && (!inheritedMethod.declaringClass.isInterface() && !inheritedMethod.declaringClass.isPublic()))
					this.type.addSyntheticBridgeMethod(inheritedMethod.original());
			}
		}

		if (current == null && skipInheritedMethods)
			continue nextSelector;

		if (inherited.length == 1 && current == null) { // handle the common case
			if (mustImplementAbstractMethods && inherited[0].isAbstract())
				checkAbstractMethod(inherited[0]);
			continue nextSelector;
		}

		int index = -1;
		int inheritedLength = inherited.length;
		MethodBinding[] matchingInherited = new MethodBinding[inheritedLength];
		MethodBinding[] foundMatch = new MethodBinding[inheritedLength]; // null is no match, otherwise value is matching currentMethod

		// skip tracks inherited methods which can be safely ignored for one of these reasons:
		// - methods that have matched other inherited methods
		// 		either because they match the same currentMethod or match each other
		// - methods that are overridden by a current method
		boolean[] skip = new boolean[inheritedLength];
		boolean[] isOverridden = new boolean[inheritedLength];
		boolean[] isInherited = new boolean[inheritedLength];
		Arrays.fill(isInherited, true);
		if (current != null) {
			for (int i = 0, length1 = current.length; i < length1; i++) {
				MethodBinding currentMethod = current[i];
				MethodBinding[] nonMatchingInherited = null;
				for (int j = 0; j < inheritedLength; j++) {
					MethodBinding inheritedMethod = computeSubstituteMethod(inherited[j], currentMethod);
					if (inheritedMethod != null) {
						if (foundMatch[j] == null && isSubstituteParameterSubsignature(currentMethod, inheritedMethod)) {
							// already checked compatibility, do visibility etc. also indicate overriding? If so ignore inheritedMethod further downstream
							isOverridden[j] = skip[j] = couldMethodOverride(currentMethod, inheritedMethod);
							matchingInherited[++index] = inheritedMethod;
							foundMatch[j] = currentMethod;
						} else {
							// best place to check each currentMethod against each non-matching inheritedMethod
							checkForNameClash(currentMethod, inheritedMethod);
							if (inheritedLength > 1) {
								if (nonMatchingInherited == null)
									nonMatchingInherited = new MethodBinding[inheritedLength];
								nonMatchingInherited[j] = inheritedMethod;
							}
						}
					}
				}
				if (index >= 0) {
					// see addtional comments in https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
					// if (index > 0 && currentMethod.declaringClass.isInterface()) // only check when inherited methods are from interfaces
					//	checkInheritedReturnTypes(matchingInherited, index + 1);
					checkAgainstInheritedMethods(currentMethod, matchingInherited, index + 1, nonMatchingInherited); // pass in the length of matching
					while (index >= 0) matchingInherited[index--] = null; // clear the contents of the matching methods
				}
			}
		}
		// first round: collect information into skip and isOverridden by comparing all pairs:
		// (and perform some side effects : bridge methods & use flags)
		for (int i = 0; i < inheritedLength; i++) {
			MethodBinding matchMethod = foundMatch[i];
			
			if (matchMethod == null && current != null && this.type.isPublic()) { // current == null case handled already.
				MethodBinding inheritedMethod = inherited[i];
				if (inheritedMethod.isPublic() && (!inheritedMethod.declaringClass.isInterface() && !inheritedMethod.declaringClass.isPublic())) {
					this.type.addSyntheticBridgeMethod(inheritedMethod.original());
				}
			}
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660, if current type is exposed,
			// inherited methods of super classes are too. current == null case handled already.
			if (!isOrEnclosedByPrivateType && matchMethod == null && current != null) {
				inherited[i].original().modifiers |= ExtraCompilerModifiers.AccLocallyUsed;	
			}
			MethodBinding inheritedMethod = inherited[i];
			for (int j = i + 1; j < inheritedLength; j++) {
				MethodBinding otherInheritedMethod = inherited[j];
				if (matchMethod == foundMatch[j] && matchMethod != null)
					continue; // both inherited methods matched the same currentMethod
				if (canSkipInheritedMethods(inheritedMethod, otherInheritedMethod))
					continue;
				// Skip the otherInheritedMethod if it is completely replaced by inheritedMethod
				// This elimination used to happen rather eagerly in computeInheritedMethods step
				// itself earlier. (https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358)
				if (TypeBinding.notEquals(inheritedMethod.declaringClass, otherInheritedMethod.declaringClass)) {
					// these method calls produce their effect as side-effects into skip and isOverridden:
					if (isSkippableOrOverridden(inheritedMethod, otherInheritedMethod, skip, isOverridden, isInherited, j))
						continue;
					if (isSkippableOrOverridden(otherInheritedMethod, inheritedMethod, skip, isOverridden, isInherited, i))
						continue;
				}
			}
		}
		// second round: collect and check matchingInherited, directly check methods with no replacing etc.
		for (int i = 0; i < inheritedLength; i++) {
			MethodBinding matchMethod = foundMatch[i];
			if (skip[i]) continue;
			MethodBinding inheritedMethod = inherited[i];
			if (matchMethod == null)
				matchingInherited[++index] = inheritedMethod;
			for (int j = i + 1; j < inheritedLength; j++) {
				if (foundMatch[j] == null) {
					MethodBinding otherInheritedMethod = inherited[j];
					if (matchMethod == foundMatch[j] && matchMethod != null)
						continue; // both inherited methods matched the same currentMethod
					if (canSkipInheritedMethods(inheritedMethod, otherInheritedMethod))
						continue;

					MethodBinding replaceMatch;
					if ((replaceMatch = findReplacedMethod(inheritedMethod, otherInheritedMethod)) != null) {
						matchingInherited[++index] = replaceMatch;
						skip[j] = true;
					} else if ((replaceMatch = findReplacedMethod(otherInheritedMethod, inheritedMethod)) != null) {
						matchingInherited[++index] = replaceMatch;
						skip[j] = true;						
					} else if (matchMethod == null) {
						// none replaced by the other, check these methods against each other now:
						checkInheritedMethods(inheritedMethod, otherInheritedMethod);
					}
				}
			}
			if (index == -1) continue;

			if (index > 0) {
				int length = index + 1;
				boolean[] matchingIsOverridden;
				boolean[] matchingIsInherited;
				if (length != inheritedLength) { // transfer inherited & overridden status to align with subset of methods.
					matchingIsOverridden = new boolean[length];
					matchingIsInherited = new boolean[length];
					for (int j = 0; j < length; j++) {
						for (int k = 0; k < inheritedLength; k++) {
							if (matchingInherited[j] == inherited[k]) {
								matchingIsOverridden[j] = isOverridden[k];
								matchingIsInherited[j] = isInherited[k];
								break;
							}
						}
					}
				} else {
					matchingIsOverridden = isOverridden;
					matchingIsInherited = isInherited;
				}
				
				checkInheritedMethods(matchingInherited, length, matchingIsOverridden, matchingIsInherited); // pass in the length of matching
			}
			else if (mustImplementAbstractMethods && matchingInherited[0].isAbstract() && matchMethod == null)
				checkAbstractMethod(matchingInherited[0]);
			while (index >= 0) matchingInherited[index--] = null; // clear the previous contents of the matching methods
		}
	}
}
/* mark as skippable
 * - any interface method implemented by a class method
 * - an x method (x in {class, interface}), for which another x method with a subsignature was found
 * mark as isOverridden
 * - any skippable method as defined above iff it is actually overridden by the specific method (disregarding visibility etc.)
 * Note, that 'idx' corresponds to the position of 'general' in the arrays 'skip' and 'isOverridden'
 * TODO(stephan) currently (as of Bug 410325), the boarder between skip and isOverridden is blurred,
 *                should reassess after more experience with this patch.
 */
boolean isSkippableOrOverridden(MethodBinding specific, MethodBinding general, boolean[] skip, boolean[] isOverridden, boolean[] isInherited, int idx) {
	boolean specificIsInterface = specific.declaringClass.isInterface();
	boolean generalIsInterface = general.declaringClass.isInterface();
	if (!specificIsInterface && generalIsInterface) {
		if (!specific.isAbstract() && isParameterSubsignature(specific, general)) {
			// 8.4.8: abstract and default methods are not inherited if a concrete method with a subsignature is defined or inherited in C
			isInherited[idx] = false;
			return true;
		} else if (isInterfaceMethodImplemented(general, specific, general.declaringClass)) {
			skip[idx] = true;
			isOverridden[idx] = true;
			return true;
		}
	} else if (specificIsInterface == generalIsInterface) { 
		if (specific.declaringClass.isCompatibleWith(general.declaringClass) && isMethodSubsignature(specific, general)) {
			skip[idx] = true;
			isOverridden[idx] = true;
			return true;
		}
	}
	return false;
}
/* 'general' is considered as replaced by 'specific' if
 * - 'specific' is "at least as concrete as" 'general'
 * - 'specific' has a signature that is a subsignature of the substituted signature of 'general' (as seen from specific's declaring class)  
 * - default methods should also be considered replaced by class methods that meet the signature that is a subsignature criteria.
 */
MethodBinding findReplacedMethod(MethodBinding specific, MethodBinding general) {
	MethodBinding generalSubstitute = computeSubstituteMethod(general, specific);
	if (generalSubstitute != null 
			&& (!specific.isAbstract() || general.isAbstract() || (general.isDefaultMethod() && specific.declaringClass.isClass()))	// if (abstract(specific) => abstract(general)) check if 'specific' overrides 'general' 
			&& isSubstituteParameterSubsignature(specific, generalSubstitute)) 
	{
		return generalSubstitute;
	} 
	return null;
}
void checkTypeVariableMethods(TypeParameter typeParameter) {
	char[][] methodSelectors = this.inheritedMethods.keyTable;
	nextSelector : for (int s = methodSelectors.length; --s >= 0;) {
		if (methodSelectors[s] == null) continue nextSelector;
		MethodBinding[] inherited = (MethodBinding[]) this.inheritedMethods.valueTable[s];
		if (inherited.length == 1) continue nextSelector;

		int index = -1;
		MethodBinding[] matchingInherited = new MethodBinding[inherited.length];
		for (int i = 0, length = inherited.length; i < length; i++) {
			while (index >= 0) matchingInherited[index--] = null; // clear the previous contents of the matching methods
			MethodBinding inheritedMethod = inherited[i];
			if (inheritedMethod != null) {
				matchingInherited[++index] = inheritedMethod;
				for (int j = i + 1; j < length; j++) {
					MethodBinding otherInheritedMethod = inherited[j];
					if (canSkipInheritedMethods(inheritedMethod, otherInheritedMethod))
						continue;
					otherInheritedMethod = computeSubstituteMethod(otherInheritedMethod, inheritedMethod);
					if (otherInheritedMethod != null && isSubstituteParameterSubsignature(inheritedMethod, otherInheritedMethod)) {
						matchingInherited[++index] = otherInheritedMethod;
						inherited[j] = null; // do not want to find it again
					}
				}
			}
			if (index > 0) {
				MethodBinding first = matchingInherited[0];
				int count = index + 1;
				while (--count > 0) {
					MethodBinding match = matchingInherited[count];
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=314556
					MethodBinding interfaceMethod = null, implementation = null;
					if (first.declaringClass.isInterface()) {
						interfaceMethod = first;
					} else if (first.declaringClass.isClass()) {
						implementation = first;
					}
					if (match.declaringClass.isInterface()) {
						interfaceMethod = match;
					} else if (match.declaringClass.isClass()) {
						implementation = match;
					}
					if (interfaceMethod != null && implementation != null && !isAsVisible(implementation, interfaceMethod))
						problemReporter().inheritedMethodReducesVisibility(typeParameter, implementation, new MethodBinding [] {interfaceMethod});
					
					if (areReturnTypesCompatible(first, match)) continue;
					// unrelated interfaces - check to see if return types are compatible
					if (first.declaringClass.isInterface() && match.declaringClass.isInterface() && areReturnTypesCompatible(match, first))
						continue;
					break;
				}
				if (count > 0) {  // All inherited methods do NOT have the same vmSignature
					problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(typeParameter, matchingInherited, index + 1);
					continue nextSelector;
				}
			}
		}
	}
}
boolean detectInheritedNameClash(MethodBinding inherited, MethodBinding otherInherited) {
	if (!inherited.areParameterErasuresEqual(otherInherited))
		return false;
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322001
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=323693
	// When reporting a name clash between two inherited methods, we should not look for a
	// signature clash, but instead should be looking for method descriptor clash. 
	if (TypeBinding.notEquals(inherited.returnType.erasure(), otherInherited.returnType.erasure()))
		return false;
	// skip it if otherInherited is defined by a subtype of inherited's declaringClass or vice versa.
	// avoid being order sensitive and check with the roles reversed also.
	if (TypeBinding.notEquals(inherited.declaringClass.erasure(), otherInherited.declaringClass.erasure())) {
		if (inherited.declaringClass.findSuperTypeOriginatingFrom(otherInherited.declaringClass) != null)
			return false;
		if (otherInherited.declaringClass.findSuperTypeOriginatingFrom(inherited.declaringClass) != null)
			return false;
	}

	problemReporter().inheritedMethodsHaveNameClash(this.type, inherited, otherInherited);
	return true;
}
boolean detectNameClash(MethodBinding current, MethodBinding inherited, boolean treatAsSynthetic) {
	MethodBinding methodToCheck = inherited;
	MethodBinding original = methodToCheck.original(); // can be the same as inherited
	if (!current.areParameterErasuresEqual(original))
		return false;
	int severity = ProblemSeverities.Error;
	if (this.environment.globalOptions.complianceLevel == ClassFileConstants.JDK1_6) {
		// for 1.6 return types also need to be checked
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
		if (TypeBinding.notEquals(current.returnType.erasure(), original.returnType.erasure()))
			severity = ProblemSeverities.Warning;
	}
	if (!treatAsSynthetic) {
		// For a user method, see if current class overrides the inherited method. If it does,
		// then any grievance we may have ought to be against the current class's method and
		// NOT against any super implementations. https://bugs.eclipse.org/bugs/show_bug.cgi?id=293615
		
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=315978 : we now defer this rather expensive
		// check to just before reporting (the incorrect) name clash. In the event there is no name
		// clash to report to begin with (the common case), no penalty needs to be paid.  
		MethodBinding[] currentNamesakes = (MethodBinding[]) this.currentMethods.get(inherited.selector);
		if (currentNamesakes.length > 1) { // we know it ought to at least one and that current is NOT the override
			for (int i = 0, length = currentNamesakes.length; i < length; i++) {
				MethodBinding currentMethod = currentNamesakes[i];
				if (currentMethod != current && doesMethodOverride(currentMethod, inherited)) {
					methodToCheck = currentMethod;
					break;
				}
			}
		}
	}
	original = methodToCheck.original(); // can be the same as inherited
	if (!current.areParameterErasuresEqual(original))
		return false;
	original = inherited.original();  // For error reporting use, inherited.original()
	problemReporter(current).methodNameClash(current, inherited.declaringClass.isRawType() ? inherited : original, severity);
	if (severity == ProblemSeverities.Warning) return false;
	return true;
}
boolean doTypeVariablesClash(MethodBinding one, MethodBinding substituteTwo) {
	// one has type variables and substituteTwo did not pass bounds check in computeSubstituteMethod()
	return one.typeVariables != Binding.NO_TYPE_VARIABLES && !(substituteTwo instanceof ParameterizedGenericMethodBinding);
}
SimpleSet findSuperinterfaceCollisions(ReferenceBinding superclass, ReferenceBinding[] superInterfaces) {
	ReferenceBinding[] interfacesToVisit = null;
	int nextPosition = 0;
	ReferenceBinding[] itsInterfaces = superInterfaces;
	if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
		nextPosition = itsInterfaces.length;
		interfacesToVisit = itsInterfaces;
	}

	boolean isInconsistent = this.type.isHierarchyInconsistent();
	ReferenceBinding superType = superclass;
	while (superType != null && superType.isValidBinding()) {
		isInconsistent |= superType.isHierarchyInconsistent();
		if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
			if (interfacesToVisit == null) {
				interfacesToVisit = itsInterfaces;
				nextPosition = interfacesToVisit.length;
			} else {
				int itsLength = itsInterfaces.length;
				if (nextPosition + itsLength >= interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
				nextInterface : for (int a = 0; a < itsLength; a++) {
					ReferenceBinding next = itsInterfaces[a];
					for (int b = 0; b < nextPosition; b++)
						if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
					interfacesToVisit[nextPosition++] = next;
				}
			}
		}
		superType = superType.superclass();
	}

	for (int i = 0; i < nextPosition; i++) {
		superType = interfacesToVisit[i];
		if (superType.isValidBinding()) {
			isInconsistent |= superType.isHierarchyInconsistent();
			if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
				int itsLength = itsInterfaces.length;
				if (nextPosition + itsLength >= interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
				nextInterface : for (int a = 0; a < itsLength; a++) {
					ReferenceBinding next = itsInterfaces[a];
					for (int b = 0; b < nextPosition; b++)
						if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
					interfacesToVisit[nextPosition++] = next;
				}
			}
		}
	}

	if (!isInconsistent) return null; // hierarchy is consistent so no collisions are possible
	SimpleSet copy = null;
	for (int i = 0; i < nextPosition; i++) {
		ReferenceBinding current = interfacesToVisit[i];
		if (current.isValidBinding()) {
			TypeBinding erasure = current.erasure();
			for (int j = i + 1; j < nextPosition; j++) {
				ReferenceBinding next = interfacesToVisit[j];
				if (next.isValidBinding() && TypeBinding.equalsEquals(next.erasure(), erasure)) {
					if (copy == null)
						copy = new SimpleSet(nextPosition);
					copy.add(interfacesToVisit[i]);
					copy.add(interfacesToVisit[j]);
				}
			}
		}
	}
	return copy;
}
boolean isAcceptableReturnTypeOverride(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	// called when currentMethod's return type is compatible with inheritedMethod's return type

	if (inheritedMethod.declaringClass.isRawType())
		return true; // since the inheritedMethod comes from a raw type, the return type is always acceptable

	MethodBinding originalInherited = inheritedMethod.original();
	TypeBinding originalInheritedReturnType = originalInherited.returnType.leafComponentType();
	if (originalInheritedReturnType.isParameterizedTypeWithActualArguments())
		return !currentMethod.returnType.leafComponentType().isRawType(); // raw types issue a warning if inherited is parameterized

	TypeBinding currentReturnType = currentMethod.returnType.leafComponentType();
	switch (currentReturnType.kind()) {
	   	case Binding.TYPE_PARAMETER :
	   		if (TypeBinding.equalsEquals(currentReturnType, inheritedMethod.returnType.leafComponentType()))
	   			return true;
	   		//$FALL-THROUGH$
		default :
			if (originalInheritedReturnType.isTypeVariable())
				if (((TypeVariableBinding) originalInheritedReturnType).declaringElement == originalInherited)
					return false;
			return true;
	}
}
// caveat: returns false if a method is implemented, but with a return type that is incompatible with that of the interface method
boolean isInterfaceMethodImplemented(MethodBinding inheritedMethod, MethodBinding existingMethod, ReferenceBinding superType) {
	if (inheritedMethod.original() != inheritedMethod && existingMethod.declaringClass.isInterface())
		return false; // must hold onto ParameterizedMethod to see if a bridge method is necessary

	inheritedMethod = computeSubstituteMethod(inheritedMethod, existingMethod);
	if (inheritedMethod == null	|| !doesMethodOverride(existingMethod, inheritedMethod))
		return false;
	return TypeBinding.equalsEquals(inheritedMethod.returnType, existingMethod.returnType)
			|| (TypeBinding.notEquals(this.type, existingMethod.declaringClass) // ... not if inheriting the bridge situation from a superclass
				&& !existingMethod.declaringClass.isInterface()
				&& areReturnTypesCompatible(existingMethod, inheritedMethod)); // may have to report incompatible return type
}
public boolean isMethodSubsignature(MethodBinding method, MethodBinding inheritedMethod) {
	if (!org.eclipse.jdt.core.compiler.CharOperation.equals(method.selector, inheritedMethod.selector))
		return false;

	// need to switch back to the original if the method is from a ParameterizedType
	if (method.declaringClass.isParameterizedType())
		method = method.original();

	MethodBinding inheritedOriginal = method.findOriginalInheritedMethod(inheritedMethod);
	return isParameterSubsignature(method, inheritedOriginal == null ? inheritedMethod : inheritedOriginal);
}
boolean isUnsafeReturnTypeOverride(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	// called when currentMethod's return type is NOT compatible with inheritedMethod's return type

	// JLS 3 §8.4.5: more are accepted, with an unchecked conversion
	if (TypeBinding.equalsEquals(currentMethod.returnType, inheritedMethod.returnType.erasure())) {
		TypeBinding[] currentParams = currentMethod.parameters;
		TypeBinding[] inheritedParams = inheritedMethod.parameters;
		for (int i = 0, l = currentParams.length; i < l; i++)
			if (!areTypesEqual(currentParams[i], inheritedParams[i]))
				return true;
	}
	if (currentMethod.typeVariables == Binding.NO_TYPE_VARIABLES
		&& inheritedMethod.original().typeVariables != Binding.NO_TYPE_VARIABLES
		&& currentMethod.returnType.erasure().findSuperTypeOriginatingFrom(inheritedMethod.returnType.erasure()) != null) {
			return true;
	}
	return false;
}
boolean reportIncompatibleReturnTypeError(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	if (isUnsafeReturnTypeOverride(currentMethod, inheritedMethod)) {
		problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, inheritedMethod, this.type);
		return false;
	}
	return super.reportIncompatibleReturnTypeError(currentMethod, inheritedMethod);
}
void verify() {
	if (this.type.isAnnotationType())
		this.type.detectAnnotationCycle();

	super.verify();
	
	reportRawReferences();

	for (int i = this.type.typeVariables.length; --i >= 0;) {
		TypeVariableBinding var = this.type.typeVariables[i];
		// must verify bounds if the variable has more than 1
		if (var.superInterfaces == Binding.NO_SUPERINTERFACES) continue;
		if (var.superInterfaces.length == 1 && var.superclass.id == TypeIds.T_JavaLangObject) continue;

		this.currentMethods = new HashtableOfObject(0);
		ReferenceBinding superclass = var.superclass();
		if (superclass.kind() == Binding.TYPE_PARAMETER)
			superclass = (ReferenceBinding) superclass.erasure();
		ReferenceBinding[] itsInterfaces = var.superInterfaces();
		ReferenceBinding[] superInterfaces = new ReferenceBinding[itsInterfaces.length];
		for (int j = itsInterfaces.length; --j >= 0;) {
			superInterfaces[j] = itsInterfaces[j].kind() == Binding.TYPE_PARAMETER
				? (ReferenceBinding) itsInterfaces[j].erasure()
				: itsInterfaces[j];
		}
		computeInheritedMethods(superclass, superInterfaces);
		checkTypeVariableMethods(this.type.scope.referenceContext.typeParameters[i]);
	}
}
}
