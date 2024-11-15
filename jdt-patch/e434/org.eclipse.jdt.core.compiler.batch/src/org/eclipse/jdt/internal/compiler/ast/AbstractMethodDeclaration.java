/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 367203 - [compiler][null] detect assigning null to nonnull argument
 *								bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
 *								bug 382353 - [1.8][compiler] Implementation property modifiers should be accepted on default methods.
 *								bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								bug 388281 - [compiler][null] inheritance of null annotations as an option
 *								bug 401030 - [1.8][null] Null analysis support for lambda methods.
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 403216 - [1.8][null] TypeReference#captureTypeAnnotations treats type annotations as type argument annotations
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 435570 - [1.8][null] @NonNullByDefault illegally tries to affect "throws E"
 *								Bug 435805 - [1.8][compiler][null] Java 8 compiler does not recognize declaration style null annotations
 *								Bug 466713 - Null Annotations: NullPointerException using <int @Nullable []> as Type Param
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.lookup.MethodBinding.PARAM_NONNULL;
import static org.eclipse.jdt.internal.compiler.lookup.MethodBinding.PARAM_NOTOWNING;
import static org.eclipse.jdt.internal.compiler.lookup.MethodBinding.PARAM_NULLABLE;
import static org.eclipse.jdt.internal.compiler.lookup.MethodBinding.PARAM_NULLITY;
import static org.eclipse.jdt.internal.compiler.lookup.MethodBinding.PARAM_OWNING;

import java.util.List;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationPosition;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings({"rawtypes"})
public abstract class AbstractMethodDeclaration
	extends ASTNode
	implements ProblemSeverities, ReferenceContext {

	public MethodScope scope;
	//it is not relevent for constructor but it helps to have the name of the constructor here
	//which is always the name of the class.....parsing do extra work to fill it up while it do not have to....
	public char[] selector;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiers;
	public int modifiersSourceStart;
	public Annotation[] annotations;
	// jsr 308
	public Receiver receiver;
	public Argument[] arguments;
	public TypeReference[] thrownExceptions;
	public Statement[] statements;
	public int explicitDeclarations;
	public MethodBinding binding;
	public boolean ignoreFurtherInvestigation = false;

	public Javadoc javadoc;

	public int bodyStart;
	public int bodyEnd = -1;
	public CompilationResult compilationResult;

	AbstractMethodDeclaration(CompilationResult compilationResult){
		this.compilationResult = compilationResult;
	}

	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	@Override
	public void abort(int abortLevel, CategorizedProblem problem) {

		switch (abortLevel) {
			case AbortCompilation :
				throw new AbortCompilation(this.compilationResult, problem);
			case AbortCompilationUnit :
				throw new AbortCompilationUnit(this.compilationResult, problem);
			case AbortType :
				throw new AbortType(this.compilationResult, problem);
			default :
				throw new AbortMethod(this.compilationResult, problem);
		}
	}

	/**
	 * When a method is accessed via SourceTypeBinding.resolveTypesFor(MethodBinding)
	 * we create the argument binding and resolve annotations in order to compute null annotation tagbits.
	 */
	public void createArgumentBindings() {
		createArgumentBindings(this.arguments, this.binding, this.scope);
	}
	// version for invocation from LambdaExpression:
	static void createArgumentBindings(Argument[] arguments, MethodBinding binding, MethodScope scope) {
		boolean useTypeAnnotations = scope.environment().usesNullTypeAnnotations();
		if (arguments != null && binding != null) {
			for (int i = 0, length = arguments.length; i < length; i++) {
				Argument argument = arguments[i];
				binding.parameters[i] = argument.createBinding(scope, binding.parameters[i]);
				long argumentTagBits = argument.binding.tagBits;
				if ((argumentTagBits & TagBits.AnnotationOwning) != 0) {
					if (binding.parameterFlowBits == null) {
						binding.parameterFlowBits = new byte[arguments.length];
					}
					binding.parameterFlowBits[i] |= PARAM_OWNING;
				} else if ((argumentTagBits & TagBits.AnnotationNotOwning) != 0) {
					if (binding.parameterFlowBits == null) {
						binding.parameterFlowBits = new byte[arguments.length];
					}
					binding.parameterFlowBits[i] |= PARAM_NOTOWNING;
				}
				if (useTypeAnnotations)
					continue; // no business with SE7 null annotations in the 1.8 case.
				// createBinding() has resolved annotations, now transfer nullness info from the argument to the method:
				long argTypeTagBits = (argumentTagBits & TagBits.AnnotationNullMASK);
				if (argTypeTagBits != 0) {
					if (binding.parameterFlowBits == null) {
						binding.parameterFlowBits = new byte[arguments.length];
						binding.tagBits |= TagBits.IsNullnessKnown;
					}
					binding.parameterFlowBits[i] = MethodBinding.flowBitFromAnnotationTagBit(argTypeTagBits);
				}
			}
		}
	}

	/**
	 * Bind and add argument's binding into the scope of the method
	 */
	public void bindArguments() {

		if (this.arguments != null) {
			// by default arguments in abstract/native methods are considered to be used (no complaint is expected)
			if (this.binding == null) {
				for (Argument argument : this.arguments) {
					argument.bind(this.scope, null, true);
				}
				return;
			}
			boolean used = this.binding.isAbstract() || this.binding.isNative();
			AnnotationBinding[][] paramAnnotations = null;
			for (int i = 0, length = this.arguments.length; i < length; i++) {
				Argument argument = this.arguments[i];
				this.binding.parameters[i] = argument.bind(this.scope, this.binding.parameters[i], used);
				if (argument.annotations != null) {
					if (paramAnnotations == null) {
						paramAnnotations = new AnnotationBinding[length][];
						for (int j=0; j<i; j++) {
							paramAnnotations[j] = Binding.NO_ANNOTATIONS;
						}
					}
					paramAnnotations[i] = argument.binding.getAnnotations();
				} else if (paramAnnotations != null) {
					paramAnnotations[i] = Binding.NO_ANNOTATIONS;
				}
			}
			if (paramAnnotations == null) {
				paramAnnotations = getPropagatedRecordComponentAnnotations();
			}

			if (paramAnnotations != null)
				this.binding.setParameterAnnotations(paramAnnotations);
		}
	}

	protected AnnotationBinding[][] getPropagatedRecordComponentAnnotations() {
		return null;
	}

	/**
	 * Record the thrown exception type bindings in the corresponding type references.
	 */
	public void bindThrownExceptions() {

		if (this.thrownExceptions != null
			&& this.binding != null
			&& this.binding.thrownExceptions != null) {
			int thrownExceptionLength = this.thrownExceptions.length;
			int length = this.binding.thrownExceptions.length;
			if (length == thrownExceptionLength) {
				for (int i = 0; i < length; i++) {
					this.thrownExceptions[i].resolvedType = this.binding.thrownExceptions[i];
				}
			} else {
				int bindingIndex = 0;
				for (int i = 0; i < thrownExceptionLength && bindingIndex < length; i++) {
					TypeReference thrownException = this.thrownExceptions[i];
					ReferenceBinding thrownExceptionBinding = this.binding.thrownExceptions[bindingIndex];
					char[][] bindingCompoundName = thrownExceptionBinding.compoundName;
					if (bindingCompoundName == null) continue; // skip problem case
					if (thrownException instanceof SingleTypeReference) {
						// single type reference
						int lengthName = bindingCompoundName.length;
						char[] thrownExceptionTypeName = thrownException.getTypeName()[0];
						if (CharOperation.equals(thrownExceptionTypeName, bindingCompoundName[lengthName - 1])) {
							thrownException.resolvedType = thrownExceptionBinding;
							bindingIndex++;
						}
					} else {
						// qualified type reference
						if (CharOperation.equals(thrownException.getTypeName(), bindingCompoundName)) {
							thrownException.resolvedType = thrownExceptionBinding;
							bindingIndex++;
						}
					}
				}
			}
		}
	}

	/**
	 * Feed information from certain argument annotations into the analysis and mark arguments as assigned.
	 * Annotations evaluated here are (if enabled):
	 * <ul>
	 * <li>NonNull - for null analysis
	 * <li>Nullable - for null analysis
	 * <li>Owning - for resource leak analysis
	 * <li>NotOwning - for resource leak analysis
	 * </ul>
	 */
	static void analyseArguments(LookupEnvironment environment, FlowInfo flowInfo, FlowContext flowContext, Argument[] methodArguments, MethodBinding methodBinding) {
		if (methodArguments != null) {
			boolean usesNullTypeAnnotations = environment.usesNullTypeAnnotations();
			boolean usesOwningAnnotations = environment.usesOwningAnnotations();

			int length = Math.min(methodBinding.parameters.length, methodArguments.length);
			for (int i = 0; i < length; i++) {
				TypeBinding parameterBinding = methodBinding.parameters[i];
				LocalVariableBinding local = methodArguments[i].binding;
				if (usesNullTypeAnnotations) {
					// leverage null type annotations:
					long tagBits = parameterBinding.tagBits & TagBits.AnnotationNullMASK;
					if (tagBits == TagBits.AnnotationNonNull)
						flowInfo.markAsDefinitelyNonNull(local);
					else if (tagBits == TagBits.AnnotationNullable)
						flowInfo.markPotentiallyNullBit(local);
					else if (parameterBinding.isFreeTypeVariable())
						flowInfo.markNullStatus(local, FlowInfo.FREE_TYPEVARIABLE);
				} else {
					if (methodBinding.parameterFlowBits != null) {
						// leverage null-info from parameter annotations:
						int nullity = methodBinding.parameterFlowBits[i] & PARAM_NULLITY;
						if (nullity == PARAM_NONNULL)
							flowInfo.markAsDefinitelyNonNull(local);
						else if (nullity == PARAM_NULLABLE)
							flowInfo.markPotentiallyNullBit(local);
					}
				}
				if (!flowInfo.hasNullInfoFor(local))
					flowInfo.markNullStatus(local, FlowInfo.UNKNOWN); // ensure nullstatus is initialized
				// tag parameters as being set:
				flowInfo.markAsDefinitelyAssigned(local);

				if (usesOwningAnnotations && local.type.hasTypeBit(TypeIds.BitAutoCloseable|TypeIds.BitCloseable)) {
					long owningTagBits = local.tagBits & TagBits.AnnotationOwningMASK;
					int initialNullStatus = (local.tagBits & TagBits.AnnotationOwning) !=0 ? FlowInfo.NULL : FlowInfo.NON_NULL; // defaulting to not-owning
					local.closeTracker = new FakedTrackingVariable(local, methodArguments[i], flowInfo, flowContext, initialNullStatus, usesOwningAnnotations);
					local.closeTracker.owningState = FakedTrackingVariable.owningStateFromTagBits(owningTagBits, FakedTrackingVariable.NOT_OWNED_PER_DEFAULT);
				}
			}
		}
	}

	@Override
	public CompilationResult compilationResult() {

		return this.compilationResult;
	}

	/**
	 * Bytecode generation for a method
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {

		classFile.codeStream.wideMode = false; // reset wideMode to false
		if (this.ignoreFurtherInvestigation) {
			// method is known to have errors, dump a problem method
			if (this.binding == null)
				return; // handle methods with invalid signature or duplicates
			int problemsLength;
			CategorizedProblem[] problems =
				this.scope.referenceCompilationUnit().compilationResult.getProblems();
			CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemMethod(this, this.binding, problemsCopy);
			return;
		}
		int problemResetPC = 0;
		CompilationResult unitResult = null;
		int problemCount = 0;
		if (classScope != null) {
			TypeDeclaration referenceContext = classScope.referenceContext;
			if (referenceContext != null) {
				unitResult = referenceContext.compilationResult();
				problemCount = unitResult.problemCount;
			}
		}
		boolean restart = false;
		boolean abort = false;
		// regular code generation
		do {
			try {
				problemResetPC = classFile.contentsOffset;
				this.generateCode(classFile);
				restart = false;
			} catch (AbortMethod e) {
				// a fatal error was detected during code generation, need to restart code gen if possible
				if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
					// a branch target required a goto_w, restart code gen in wide mode.
					classFile.contentsOffset = problemResetPC;
					classFile.methodCount--;
					classFile.codeStream.resetInWideMode(); // request wide mode
					// reset the problem count to prevent reporting the same warning twice
					if (unitResult != null) {
						unitResult.problemCount = problemCount;
					}
					restart = true;
				} else if (e.compilationResult == CodeStream.RESTART_CODE_GEN_FOR_UNUSED_LOCALS_MODE) {
					classFile.contentsOffset = problemResetPC;
					classFile.methodCount--;
					classFile.codeStream.resetForCodeGenUnusedLocals();
					// reset the problem count to prevent reporting the same warning twice
					if (unitResult != null) {
						unitResult.problemCount = problemCount;
					}
					restart = true;
				} else {
					restart = false;
					abort = true;
				}
			}
		} while (restart);
		// produce a problem method accounting for this fatal error
		if (abort) {
			int problemsLength;
			CategorizedProblem[] problems =
				this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
			CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemMethod(this, this.binding, problemsCopy, problemResetPC);
		}
	}

	public void generateCode(ClassFile classFile) {

		classFile.generateMethodInfoHeader(this.binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttributes(this.binding);
		if ((!this.binding.isNative()) && (!this.binding.isAbstract())) {
			int codeAttributeOffset = classFile.contentsOffset;
			classFile.generateCodeAttributeHeader();
			CodeStream codeStream = classFile.codeStream;
			codeStream.reset(this, classFile);
			// initialize local positions
			this.scope.computeLocalVariablePositions(this.binding.isStatic() ? 0 : 1, codeStream);

			// arguments initialization for local variable debug attributes
			if (this.arguments != null) {
				for (Argument argument : this.arguments) {
					LocalVariableBinding argBinding;
					codeStream.addVisibleLocalVariable(argBinding = argument.binding);
					argBinding.recordInitializationStartPC(0);
				}
			}
			codeStream.pushPatternAccessTrapScope(this.scope);
			if (this.statements != null) {
				for (Statement stmt : this.statements) {
					stmt.generateCode(this.scope, codeStream);
					if (!this.compilationResult.hasErrors() && (codeStream.stackDepth != 0 || codeStream.operandStack.size() != 0)) {
						this.scope.problemReporter().operandStackSizeInappropriate(this);
					}
				}
			}
			// if a problem got reported during code gen, then trigger problem method creation
			if (this.ignoreFurtherInvestigation) {
				throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
			}
			if ((this.bits & ASTNode.NeedFreeReturn) != 0) {
				codeStream.return_();
			}
			// See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796#issuecomment-1933458054
			codeStream.exitUserScope(this.scope, lvb -> !lvb.isParameter());
			codeStream.handleRecordAccessorExceptions(this.scope);
			// local variable attributes
			codeStream.exitUserScope(this.scope);
			codeStream.recordPositionsFrom(0, this.declarationSourceEnd);
			try {
				classFile.completeCodeAttribute(codeAttributeOffset,this.scope);
			} catch(NegativeArraySizeException e) {
				throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
			}
			attributeNumber++;
		} else {
			checkArgumentsSize();
		}
		classFile.completeMethodInfo(this.binding, methodAttributeOffset, attributeNumber);
	}

	public void getAllAnnotationContexts(int targetType, List allAnnotationContexts) {
		// do nothing
	}

	private void checkArgumentsSize() {
		TypeBinding[] parameters = this.binding.parameters;
		int size = 1; // an abstract method or a native method cannot be static
		for (int i = 0, max = parameters.length; i < max; i++) {
			switch(parameters[i].id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					size += 2;
					break;
				default :
					size++;
					break;
			}
			if (size > 0xFF) {
				this.scope.problemReporter().noMoreAvailableSpaceForArgument(this.scope.locals[i], this.scope.locals[i].declaration);
			}
		}
	}

	@Override
	public CompilationUnitDeclaration getCompilationUnitDeclaration() {
		if (this.scope != null) {
			return this.scope.compilationUnitScope().referenceContext;
		}
		return null;
	}

	@Override
	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public boolean isAbstract() {

		if (this.binding != null)
			return this.binding.isAbstract();
		return (this.modifiers & ClassFileConstants.AccAbstract) != 0;
	}

	public boolean isAnnotationMethod() {

		return false;
	}

	public boolean isClinit() {

		return false;
	}

	public boolean isConstructor() {

		return false;
	}

	public boolean isCanonicalConstructor() {

		return false;
	}
	public boolean isDefaultConstructor() {

		return false;
	}

	public boolean isDefaultMethod() {
		return false;
	}

	public boolean isInitializationMethod() {

		return false;
	}

	public boolean isMethod() {

		return false;
	}

	public boolean isNative() {

		if (this.binding != null)
			return this.binding.isNative();
		return (this.modifiers & ClassFileConstants.AccNative) != 0;
	}

	public RecordComponent getRecordComponent() {
		return null;
	}

	public boolean isStatic() {

		if (this.binding != null)
			return this.binding.isStatic();
		return (this.modifiers & ClassFileConstants.AccStatic) != 0;
	}

	public boolean isCandidateMain() {
		return false;
	}

	/**
	 * Fill up the method body with statement
	 */
	public abstract void parseStatements(Parser parser, CompilationUnitDeclaration unit);

	@Override
	public StringBuilder print(int tab, StringBuilder output) {

		if (this.javadoc != null) {
			this.javadoc.print(tab, output);
		}
		printIndent(tab, output);
		printModifiers(this.modifiers, output);
		if (this.annotations != null) {
			printAnnotations(this.annotations, output);
			output.append(' ');
		}

		TypeParameter[] typeParams = typeParameters();
		if (typeParams != null) {
			output.append('<');
			int max = typeParams.length - 1;
			for (int j = 0; j < max; j++) {
				typeParams[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			typeParams[max].print(0, output);
			output.append('>');
		}

		printReturnType(0, output).append(this.selector).append('(');
		if (this.receiver != null) {
			this.receiver.print(0, output);
		}
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) {
				if (i > 0 || this.receiver != null) output.append(", "); //$NON-NLS-1$
				this.arguments[i].print(0, output);
			}
		}
		output.append(')');
		if (this.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < this.thrownExceptions.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.thrownExceptions[i].print(0, output);
			}
		}
		printBody(tab + 1, output);
		return output;
	}

	public StringBuilder printBody(int indent, StringBuilder output) {

		if (isAbstract() || (this.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0)
			return output.append(';');

		output.append(" {"); //$NON-NLS-1$
		if (this.statements != null) {
			for (Statement statement : this.statements) {
				output.append('\n');
				statement.printStatement(indent, output);
			}
		}
		output.append('\n');
		printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
		return output;
	}

	public StringBuilder printReturnType(int indent, StringBuilder output) {

		return output;
	}

	public void resolve(ClassScope upperScope) {

		if (this.binding == null) {
			this.ignoreFurtherInvestigation = true;
		}

		try {
			bindArguments();
			resolveReceiver();
			bindThrownExceptions();
			resolveAnnotations(this.scope, this.annotations, this.binding, this.isConstructor());

			long sourceLevel = this.scope.compilerOptions().sourceLevel;
			if (sourceLevel < ClassFileConstants.JDK1_8) // otherwise already checked via Argument.createBinding
				validateNullAnnotations(this.scope.environment().usesNullTypeAnnotations());

			resolveStatements();
			// check @Deprecated annotation presence
			if (this.binding != null
					&& (this.binding.getAnnotationTagBits() & TagBits.AnnotationDeprecated) == 0
					&& (this.binding.modifiers & ClassFileConstants.AccDeprecated) != 0
					&& sourceLevel >= ClassFileConstants.JDK1_5) {
				this.scope.problemReporter().missingDeprecatedAnnotationForMethod(this);
			}
		} catch (AbortMethod e) {
			// ========= abort on fatal error =============
			this.ignoreFurtherInvestigation = true;
		}
	}

	public void resolveReceiver() {
		if (this.receiver == null) return;

		if (this.receiver.modifiers != 0) {
			this.scope.problemReporter().illegalModifiers(this.receiver.declarationSourceStart, this.receiver.declarationSourceEnd);
		}

		TypeBinding resolvedReceiverType = this.receiver.type.resolvedType;
		if (this.binding == null || resolvedReceiverType == null || !resolvedReceiverType.isValidBinding()) {
			return;
		}

		ReferenceBinding declaringClass = this.binding.declaringClass;
		/* neither static methods nor methods in anonymous types can have explicit 'this' */
		if (this.isStatic() || declaringClass.isAnonymousType()) {
			this.scope.problemReporter().disallowedThisParameter(this.receiver);
			return; // No need to do further validation
		}

		ReferenceBinding enclosingReceiver = this.scope.enclosingReceiverType();
		if (this.isConstructor()) {
			/* Only non static member types or local types can declare explicit 'this' params in constructors */
			if (declaringClass.isStatic()
					|| (declaringClass.tagBits & (TagBits.IsLocalType | TagBits.IsMemberType)) == 0) { /* neither member nor local type */
				this.scope.problemReporter().disallowedThisParameter(this.receiver);
				return; // No need to do further validation
			}
			enclosingReceiver = enclosingReceiver.enclosingType();
		}

		char[][] tokens = (this.receiver.qualifyingName == null) ? null : this.receiver.qualifyingName.getName();
		if (this.isConstructor()) {
			if (tokens == null || tokens.length > 1 || !CharOperation.equals(enclosingReceiver.sourceName(), tokens[0])) {
				this.scope.problemReporter().illegalQualifierForExplicitThis(this.receiver, enclosingReceiver);
				this.receiver.qualifyingName = null;
			}
		} else if (tokens != null && tokens.length > 0) {
			this.scope.problemReporter().illegalQualifierForExplicitThis2(this.receiver);
			this.receiver.qualifyingName = null;
		}

		if (TypeBinding.notEquals(enclosingReceiver, resolvedReceiverType)) {
			this.scope.problemReporter().illegalTypeForExplicitThis(this.receiver, enclosingReceiver);
		}

		if (this.receiver.type.hasNullTypeAnnotation(AnnotationPosition.ANY)) {
			this.scope.problemReporter().nullAnnotationUnsupportedLocation(this.receiver.type);
		}
		if (this.scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled && this.receiver.type.resolvedType != null) {
			for (AnnotationBinding annotationBinding : this.receiver.type.resolvedType.getTypeAnnotations()) {
				ReferenceBinding annotationType = annotationBinding.getAnnotationType();
				if (annotationType != null && annotationType.hasTypeBit(TypeIds.BitOwningAnnotation)) {
					this.binding.extendedTagBits = ExtendedTagBits.IsClosingMethod;
					break;
				}
			}
		}
	}
	public void resolveJavadoc() {

		if (this.binding == null) return;
		if (this.javadoc != null) {
			this.javadoc.resolve(this.scope);
			return;
		}
		if (this.binding.declaringClass != null && !this.binding.declaringClass.isLocalType()) {
			// Set javadoc visibility
			int javadocVisibility = this.binding.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
			ClassScope classScope = this.scope.classScope();
			ProblemReporter reporter = this.scope.problemReporter();
			try {
				int severity = reporter.computeSeverity(IProblem.JavadocMissing);
				if (severity != ProblemSeverities.Ignore) {
					if (classScope != null) {
						javadocVisibility = Util.computeOuterMostVisibility(classScope.referenceType(), javadocVisibility);
					}
					int javadocModifiers = (this.binding.modifiers & ~ExtraCompilerModifiers.AccVisibilityMASK) | javadocVisibility;
					reporter.javadocMissing(this.sourceStart, this.sourceEnd, severity, javadocModifiers);
				}
			} finally {
				reporter.close();
			}
		}
	}

	public void resolveStatements() {

		if (this.statements != null) {
			resolveStatements(this.statements, this.scope);
		} else if ((this.bits & UndocumentedEmptyBlock) != 0) {
			if (!this.isConstructor() || this.arguments != null) { // https://bugs.eclipse.org/bugs/show_bug.cgi?id=319626
				this.scope.problemReporter().undocumentedEmptyBlock(this.bodyStart-1, this.bodyEnd+1);
			}
		}
	}

	@Override
	public void tagAsHavingErrors() {
		this.ignoreFurtherInvestigation = true;
	}

	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {
		// default implementation: subclass will define it
	}

	public TypeParameter[] typeParameters() {
	    return null;
	}

	void validateNullAnnotations(boolean useTypeAnnotations) {
		if (this.binding == null) return;
		// null annotations on parameters?
		if (!useTypeAnnotations) {
			if (this.binding.parameterFlowBits != null) {
				int length = this.binding.parameters.length;
				for (int i=0; i<length; i++) {
					byte nullity = this.binding.parameterFlowBits[i];
					if (nullity != 0) {
						long nullAnnotationTagBit =  nullity == PARAM_NONNULL
								? TagBits.AnnotationNonNull : TagBits.AnnotationNullable;
						if (!this.scope.validateNullAnnotation(nullAnnotationTagBit, this.arguments[i].type, this.arguments[i].annotations))
							this.binding.parameterFlowBits[i] &= ~PARAM_NULLITY;
					}
				}
			}
		} else {
			int length = this.binding.parameters.length;
			for (int i=0; i<length; i++) {
				this.scope.validateNullAnnotation(this.binding.parameters[i].tagBits, this.arguments[i].type, this.arguments[i].annotations);
// TODO(stephan) remove once we're sure:
//					this.binding.parameters[i] = this.binding.parameters[i].unannotated();
			}
		}
	}
}
