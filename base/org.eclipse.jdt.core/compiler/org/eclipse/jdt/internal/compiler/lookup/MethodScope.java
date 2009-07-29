/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Specific block scope used for methods, constructors or clinits, representing
 * its outermost blockscope. Note also that such a scope will be provided to enclose
 * field initializers subscopes as well.
 */
public class MethodScope extends BlockScope {

	public ReferenceContext referenceContext;
	public boolean isStatic; // method modifier or initializer one

	//fields used during name resolution
	public boolean isConstructorCall = false; 
	public FieldBinding initializedField; // the field being initialized
	public int lastVisibleFieldID = -1; // the ID of the last field which got declared 
	// note that #initializedField can be null AND lastVisibleFieldID >= 0, when processing instance field initializers.

	// flow analysis
	public int analysisIndex; // for setting flow-analysis id
	public boolean isPropagatingInnerClassEmulation;

	// for local variables table attributes
	public int lastIndex = 0;
	public long[] definiteInits = new long[4];
	public long[][] extraDefiniteInits = new long[4][];

	// annotation support
	public boolean insideTypeAnnotation = false;
	
	// inner-emulation
	public SyntheticArgumentBinding[] extraSyntheticArguments;
	
public MethodScope(ClassScope parent, ReferenceContext context, boolean isStatic) {
	super(METHOD_SCOPE, parent);
	locals = new LocalVariableBinding[5];
	this.referenceContext = context;
	this.isStatic = isStatic;
	this.startIndex = 0;
}

String basicToString(int tab) {
	String newLine = "\n"; //$NON-NLS-1$
	for (int i = tab; --i >= 0;)
		newLine += "\t"; //$NON-NLS-1$

	String s = newLine + "--- Method Scope ---"; //$NON-NLS-1$
	newLine += "\t"; //$NON-NLS-1$
	s += newLine + "locals:"; //$NON-NLS-1$
	for (int i = 0; i < localIndex; i++)
		s += newLine + "\t" + locals[i].toString(); //$NON-NLS-1$
	s += newLine + "startIndex = " + startIndex; //$NON-NLS-1$
	s += newLine + "isConstructorCall = " + isConstructorCall; //$NON-NLS-1$
	s += newLine + "initializedField = " + initializedField; //$NON-NLS-1$
	s += newLine + "lastVisibleFieldID = " + lastVisibleFieldID; //$NON-NLS-1$
	s += newLine + "referenceContext = " + referenceContext; //$NON-NLS-1$
	return s;
}

/**
 * Spec : 8.4.3 & 9.4
 */
private void checkAndSetModifiersForConstructor(MethodBinding methodBinding) {
	int modifiers = methodBinding.modifiers;
	final ReferenceBinding declaringClass = methodBinding.declaringClass;
	if ((modifiers & ExtraCompilerModifiers.AccAlternateModifierProblem) != 0)
		problemReporter().duplicateModifierForMethod(declaringClass, (AbstractMethodDeclaration) referenceContext);

	if ((((ConstructorDeclaration) referenceContext).bits & ASTNode.IsDefaultConstructor) != 0) {
		// certain flags are propagated from declaring class onto constructor
		final int DECLARING_FLAGS = ClassFileConstants.AccEnum|ClassFileConstants.AccPublic|ClassFileConstants.AccProtected;
		final int VISIBILITY_FLAGS = ClassFileConstants.AccPrivate|ClassFileConstants.AccPublic|ClassFileConstants.AccProtected;
		int flags;
		if ((flags = declaringClass.modifiers & DECLARING_FLAGS) != 0) {
			if ((flags & ClassFileConstants.AccEnum) != 0) {
				modifiers &= ~VISIBILITY_FLAGS;
				modifiers |= ClassFileConstants.AccPrivate; // default constructor is implicitly private in enum
			} else {
				modifiers &= ~VISIBILITY_FLAGS;
				modifiers |= flags; // propagate public/protected
			}
		}
	}

	// after this point, tests on the 16 bits reserved.
	int realModifiers = modifiers & ExtraCompilerModifiers.AccJustFlag;

	// check for abnormal modifiers
	final int UNEXPECTED_MODIFIERS = ~(ClassFileConstants.AccPublic | ClassFileConstants.AccPrivate | ClassFileConstants.AccProtected | ClassFileConstants.AccStrictfp);
	if (declaringClass.isEnum() && (((ConstructorDeclaration) referenceContext).bits & ASTNode.IsDefaultConstructor) == 0) {
		final int UNEXPECTED_ENUM_CONSTR_MODIFIERS = ~(ClassFileConstants.AccPrivate | ClassFileConstants.AccStrictfp);
		if ((realModifiers & UNEXPECTED_ENUM_CONSTR_MODIFIERS) != 0) {
			problemReporter().illegalModifierForEnumConstructor((AbstractMethodDeclaration) referenceContext);
			modifiers &= ~ExtraCompilerModifiers.AccJustFlag | ~UNEXPECTED_ENUM_CONSTR_MODIFIERS;
		} else if ((((AbstractMethodDeclaration) referenceContext).modifiers & ClassFileConstants.AccStrictfp) != 0) {
			// must check the parse node explicitly
			problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) referenceContext);
		}
		modifiers |= ClassFileConstants.AccPrivate; // enum constructor is implicitly private
	} else if ((realModifiers & UNEXPECTED_MODIFIERS) != 0) {
		problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) referenceContext);
		modifiers &= ~ExtraCompilerModifiers.AccJustFlag | ~UNEXPECTED_MODIFIERS;
	} else if ((((AbstractMethodDeclaration) referenceContext).modifiers & ClassFileConstants.AccStrictfp) != 0) {
		// must check the parse node explicitly
		problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) referenceContext);
	}

	// check for incompatible modifiers in the visibility bits, isolate the visibility bits
	int accessorBits = realModifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate);
	if ((accessorBits & (accessorBits - 1)) != 0) {
		problemReporter().illegalVisibilityModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration) referenceContext);

		// need to keep the less restrictive so disable Protected/Private as necessary
		if ((accessorBits & ClassFileConstants.AccPublic) != 0) {
			if ((accessorBits & ClassFileConstants.AccProtected) != 0)
				modifiers &= ~ClassFileConstants.AccProtected;
			if ((accessorBits & ClassFileConstants.AccPrivate) != 0)
				modifiers &= ~ClassFileConstants.AccPrivate;
		} else if ((accessorBits & ClassFileConstants.AccProtected) != 0 && (accessorBits & ClassFileConstants.AccPrivate) != 0) {
			modifiers &= ~ClassFileConstants.AccPrivate;
		}
	}

//		// if the receiver's declaring class is a private nested type, then make sure the receiver is not private (causes problems for inner type emulation)
//		if (declaringClass.isPrivate() && (modifiers & ClassFileConstants.AccPrivate) != 0)
//			modifiers &= ~ClassFileConstants.AccPrivate;

	methodBinding.modifiers = modifiers;
}

/**
 * Spec : 8.4.3 & 9.4
 */
private void checkAndSetModifiersForMethod(MethodBinding methodBinding) {
	int modifiers = methodBinding.modifiers;
	final ReferenceBinding declaringClass = methodBinding.declaringClass;
	if ((modifiers & ExtraCompilerModifiers.AccAlternateModifierProblem) != 0)
		problemReporter().duplicateModifierForMethod(declaringClass, (AbstractMethodDeclaration) referenceContext);

	// after this point, tests on the 16 bits reserved.
	int realModifiers = modifiers & ExtraCompilerModifiers.AccJustFlag;

	// set the requested modifiers for a method in an interface/annotation
	if (declaringClass.isInterface()) {
		if ((realModifiers & ~(ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract)) != 0) {
			if ((declaringClass.modifiers & ClassFileConstants.AccAnnotation) != 0)
				problemReporter().illegalModifierForAnnotationMember((AbstractMethodDeclaration) referenceContext);
			else
				problemReporter().illegalModifierForInterfaceMethod((AbstractMethodDeclaration) referenceContext);
		}
		return;
	}

	// check for abnormal modifiers
	final int UNEXPECTED_MODIFIERS = ~(ClassFileConstants.AccPublic | ClassFileConstants.AccPrivate | ClassFileConstants.AccProtected
		| ClassFileConstants.AccAbstract | ClassFileConstants.AccStatic | ClassFileConstants.AccFinal | ClassFileConstants.AccSynchronized | ClassFileConstants.AccNative | ClassFileConstants.AccStrictfp);
	if ((realModifiers & UNEXPECTED_MODIFIERS) != 0) {
		problemReporter().illegalModifierForMethod((AbstractMethodDeclaration) referenceContext);
		modifiers &= ~ExtraCompilerModifiers.AccJustFlag | ~UNEXPECTED_MODIFIERS;
	}

	// check for incompatible modifiers in the visibility bits, isolate the visibility bits
	int accessorBits = realModifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate);
	if ((accessorBits & (accessorBits - 1)) != 0) {
		problemReporter().illegalVisibilityModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration) referenceContext);

		// need to keep the less restrictive so disable Protected/Private as necessary
		if ((accessorBits & ClassFileConstants.AccPublic) != 0) {
			if ((accessorBits & ClassFileConstants.AccProtected) != 0)
				modifiers &= ~ClassFileConstants.AccProtected;
			if ((accessorBits & ClassFileConstants.AccPrivate) != 0)
				modifiers &= ~ClassFileConstants.AccPrivate;
		} else if ((accessorBits & ClassFileConstants.AccProtected) != 0 && (accessorBits & ClassFileConstants.AccPrivate) != 0) {
			modifiers &= ~ClassFileConstants.AccPrivate;
		}
	}

	// check for modifiers incompatible with abstract modifier
	if ((modifiers & ClassFileConstants.AccAbstract) != 0) {
		int incompatibleWithAbstract = ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccFinal | ClassFileConstants.AccSynchronized | ClassFileConstants.AccNative | ClassFileConstants.AccStrictfp;
		if ((modifiers & incompatibleWithAbstract) != 0)
			problemReporter().illegalAbstractModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration) referenceContext);
		if (!methodBinding.declaringClass.isAbstract())
			problemReporter().abstractMethodInAbstractClass((SourceTypeBinding) declaringClass, (AbstractMethodDeclaration) referenceContext);
	}

	/* DISABLED for backward compatibility with javac (if enabled should also mark private methods as final)
	// methods from a final class are final : 8.4.3.3 
	if (methodBinding.declaringClass.isFinal())
		modifiers |= AccFinal;
	*/
	// native methods cannot also be tagged as strictfp
	if ((modifiers & ClassFileConstants.AccNative) != 0 && (modifiers & ClassFileConstants.AccStrictfp) != 0)
		problemReporter().nativeMethodsCannotBeStrictfp(declaringClass, (AbstractMethodDeclaration) referenceContext);

	// static members are only authorized in a static member or top level type
	if (((realModifiers & ClassFileConstants.AccStatic) != 0) && declaringClass.isNestedType() && !declaringClass.isStatic())
		problemReporter().unexpectedStaticModifierForMethod(declaringClass, (AbstractMethodDeclaration) referenceContext);

	methodBinding.modifiers = modifiers;
}

public void checkUnusedParameters(MethodBinding method) {
	if (method.isAbstract()
			|| (method.isImplementing() && !compilerOptions().reportUnusedParameterWhenImplementingAbstract) 
			|| (method.isOverriding() && !method.isImplementing() && !compilerOptions().reportUnusedParameterWhenOverridingConcrete)
			|| method.isMain()) {
		// do not want to check
		return;
	}
	for (int i = 0, maxLocals = this.localIndex; i < maxLocals; i++) {
		LocalVariableBinding local = locals[i];
		if (local == null || ((local.tagBits & TagBits.IsArgument) == 0)) {
			break; // done with arguments
		}
		if (local.useFlag == LocalVariableBinding.UNUSED && 
				// do not report fake used variable
				((local.declaration.bits & ASTNode.IsLocalDeclarationReachable) != 0)) { // declaration is reachable
			this.problemReporter().unusedArgument(local.declaration);
		}
	}
}

/**
 * Compute variable positions in scopes given an initial position offset
 * ignoring unused local variables.
 * 
 * Deal with arguments here, locals and subscopes are processed in BlockScope method
 */
public void computeLocalVariablePositions(int initOffset, CodeStream codeStream) {
	this.offset = initOffset;
	this.maxOffset = initOffset;

	// manage arguments	
	int ilocal = 0, maxLocals = this.localIndex;
	while (ilocal < maxLocals) {
		LocalVariableBinding local = locals[ilocal];
		if (local == null || ((local.tagBits & TagBits.IsArgument) == 0)) break; // done with arguments

		// record user-defined argument for attribute generation
		codeStream.record(local); 

		// assign variable position
		local.resolvedPosition = this.offset;

		if ((local.type == TypeBinding.LONG) || (local.type == TypeBinding.DOUBLE)) {
			this.offset += 2;
		} else {
			this.offset++;
		}
		// check for too many arguments/local variables
		if (this.offset > 0xFF) { // no more than 255 words of arguments
			this.problemReporter().noMoreAvailableSpaceForArgument(local, local.declaration);
		}
		ilocal++;
	}
	
	// sneak in extra argument before other local variables
	if (extraSyntheticArguments != null) {
		for (int iarg = 0, maxArguments = extraSyntheticArguments.length; iarg < maxArguments; iarg++){
			SyntheticArgumentBinding argument = extraSyntheticArguments[iarg];
			argument.resolvedPosition = this.offset;
			if ((argument.type == TypeBinding.LONG) || (argument.type == TypeBinding.DOUBLE)){
				this.offset += 2;
			} else {
				this.offset++;
			}
			if (this.offset > 0xFF) { // no more than 255 words of arguments
				this.problemReporter().noMoreAvailableSpaceForArgument(argument, (ASTNode)this.referenceContext); 
			}
		}
	}
	this.computeLocalVariablePositions(ilocal, this.offset, codeStream);
}

/**
 * Error management:
 * 		keep null for all the errors that prevent the method to be created
 * 		otherwise return a correct method binding (but without the element
 *		that caused the problem) : ie : Incorrect thrown exception
 */
MethodBinding createMethod(AbstractMethodDeclaration method) {
	// is necessary to ensure error reporting
	this.referenceContext = method;
	method.scope = this;
	SourceTypeBinding declaringClass = referenceType().binding;
	int modifiers = method.modifiers | ExtraCompilerModifiers.AccUnresolved;
	if (method.isConstructor()) {
		if (method.isDefaultConstructor())
			modifiers |= ExtraCompilerModifiers.AccIsDefaultConstructor;
		method.binding = new MethodBinding(modifiers, null, null, declaringClass);
		checkAndSetModifiersForConstructor(method.binding);
	} else {
		if (declaringClass.isInterface()) // interface or annotation type
			modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract;
		method.binding =
			new MethodBinding(modifiers, method.selector, null, null, null, declaringClass);
		checkAndSetModifiersForMethod(method.binding);
	}
	this.isStatic = method.binding.isStatic();

	Argument[] argTypes = method.arguments;
	int argLength = argTypes == null ? 0 : argTypes.length;
	if (argLength > 0 && compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5) {
		if (argTypes[--argLength].isVarArgs())
			method.binding.modifiers |= ClassFileConstants.AccVarargs;
		while (--argLength >= 0) {
			if (argTypes[argLength].isVarArgs())
				problemReporter().illegalVararg(argTypes[argLength], method);
		}
	}
	
	TypeParameter[] typeParameters = method.typeParameters();
    // do not construct type variables if source < 1.5
	if (typeParameters == null || compilerOptions().sourceLevel < ClassFileConstants.JDK1_5) {
	    method.binding.typeVariables = Binding.NO_TYPE_VARIABLES;
	} else {
		method.binding.typeVariables = createTypeVariables(typeParameters, method.binding);
		method.binding.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
	}
	return method.binding;
}

/**
 * Overridden to detect the error case inside an explicit constructor call:
class X {
	int i;
	X myX;
	X(X x) {
		this(i, myX.i, x.i); // same for super calls... only the first 2 field accesses are errors
	}
}
 */
public FieldBinding findField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite, boolean needResolve) {

	FieldBinding field = super.findField(receiverType, fieldName, invocationSite, needResolve);
	if (field == null)
		return null;
	if (!field.isValidBinding())
		return field; // answer the error field
	if (field.isStatic())
		return field; // static fields are always accessible

	if (!isConstructorCall || receiverType != enclosingSourceType())
		return field;

	if (invocationSite instanceof SingleNameReference)
		return new ProblemFieldBinding(
			field, // closest match
			field.declaringClass,
			fieldName,
			ProblemReasons.NonStaticReferenceInConstructorInvocation);
	if (invocationSite instanceof QualifiedNameReference) {
		// look to see if the field is the first binding
		QualifiedNameReference name = (QualifiedNameReference) invocationSite;
		if (name.binding == null)
			// only true when the field is the fieldbinding at the beginning of name's tokens
			return new ProblemFieldBinding(
				field, // closest match
				field.declaringClass,
				fieldName,
				ProblemReasons.NonStaticReferenceInConstructorInvocation);
	}
	return field;
}

public boolean isInsideConstructor() {
	return (referenceContext instanceof ConstructorDeclaration);
}

public boolean isInsideInitializer() {
	return (referenceContext instanceof TypeDeclaration);
}

public boolean isInsideInitializerOrConstructor() {
	return (referenceContext instanceof TypeDeclaration)
		|| (referenceContext instanceof ConstructorDeclaration);
}

/**
 * Answer the problem reporter to use for raising new problems.
 *
 * Note that as a side-effect, this updates the current reference context
 * (unit, type or method) in case the problem handler decides it is necessary
 * to abort.
 */
public ProblemReporter problemReporter() {
	MethodScope outerMethodScope;
	if ((outerMethodScope = outerMostMethodScope()) == this) {
		ProblemReporter problemReporter = referenceCompilationUnit().problemReporter;
		problemReporter.referenceContext = referenceContext;
		return problemReporter;
	}
	return outerMethodScope.problemReporter();
}

public final int recordInitializationStates(FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) return -1;
	UnconditionalFlowInfo unconditionalFlowInfo = flowInfo.unconditionalInitsWithoutSideEffect();
	long[] extraInits = unconditionalFlowInfo.extra == null ?
			null : unconditionalFlowInfo.extra[0];
	long inits = unconditionalFlowInfo.definiteInits;
	checkNextEntry : for (int i = lastIndex; --i >= 0;) {
		if (definiteInits[i] == inits) {
			long[] otherInits = extraDefiniteInits[i];
			if ((extraInits != null) && (otherInits != null)) {
				if (extraInits.length == otherInits.length) {
					int j, max;
					for (j = 0, max = extraInits.length; j < max; j++) {
						if (extraInits[j] != otherInits[j]) {
							continue checkNextEntry;
						}
					}
					return i;
				}
			} else {
				if ((extraInits == null) && (otherInits == null)) {
					return i;
				}
			}
		}
	}

	// add a new entry
	if (definiteInits.length == lastIndex) {
		// need a resize
		System.arraycopy(
			definiteInits,
			0,
			(definiteInits = new long[lastIndex + 20]),
			0,
			lastIndex);
		System.arraycopy(
			extraDefiniteInits,
			0,
			(extraDefiniteInits = new long[lastIndex + 20][]),
			0,
			lastIndex);
	}
	definiteInits[lastIndex] = inits;
	if (extraInits != null) {
		extraDefiniteInits[lastIndex] = new long[extraInits.length];
		System.arraycopy(
			extraInits,
			0,
			extraDefiniteInits[lastIndex],
			0,
			extraInits.length);
	}
	return lastIndex++;
}

/**
 *  Answer the reference method of this scope, or null if initialization scoope.
 */
public AbstractMethodDeclaration referenceMethod() {
	if (referenceContext instanceof AbstractMethodDeclaration) return (AbstractMethodDeclaration) referenceContext;
	return null;
}

/**
 *  Answer the reference type of this scope.
 * It is the nearest enclosing type of this scope.
 */
public TypeDeclaration referenceType() {
	return ((ClassScope) parent).referenceContext;
}
}
