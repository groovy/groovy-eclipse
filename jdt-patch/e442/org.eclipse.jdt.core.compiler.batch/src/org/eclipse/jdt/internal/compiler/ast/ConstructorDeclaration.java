/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     							bug 343713 - [compiler] bogus line number in constructor of inner class in 1.5 compliance
 *     							bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 361407 - Resource leak warning when resource is assigned to a field outside of constructor
 *								bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
 *								bug 383690 - [compiler] location of error re uninitialized final field should be aligned
 *								bug 331649 - [compiler][null] consider null annotations for fields
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								bug 400421 - [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 416176 - [1.8][compiler][null] null type annotations cause grief on type variables
 *								Bug 435805 - [1.8][compiler][null] Java 8 compiler does not recognize declaration style null annotations
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 415399 - [1.8][compiler] Type annotations on constructor results dropped by the code generator
 *     Ulrich Grave <ulrich.grave@gmx.de> - Contributions for
 *                              bug 386692 - Missing "unused" warning on "autowired" fields
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration.ConstructorFlowAnalysisMode.FULL_ANALYSIS;
import static org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration.ConstructorFlowAnalysisMode.PROLOGUE_ANALYSIS;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationCollector;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.codegen.StackMapFrameCodeStream;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ConstructorDeclaration extends AbstractMethodDeclaration {

	public ExplicitConstructorCall constructorCall;

	public TypeParameter[] typeParameters;

	private ExceptionHandlingFlowContext constructorContext;

	public AbstractVariableDeclaration [] protoArguments; // for compact constructors; we don't have a back pointer to declaring class.

	static class PrologueInfo {
		private FlowInfo prologueFlowInfo;
		private int eccIndex;

		PrologueInfo(FlowInfo prologueFlowInfo, int eccIndex) {
			this.prologueFlowInfo = prologueFlowInfo;
			this.eccIndex = eccIndex;
		}
	}

	private PrologueInfo prologueInfo;

public ConstructorDeclaration(CompilationResult compilationResult){
	super(compilationResult);
}

private int prologueLocalSlotSize; // extra slots to offset initializer scope by to prevent aliasing with constructor locals.

FlowInfo getPrologueFlowInfo() {
	if (this.prologueInfo == null) // may be null when `this.ignoreFurtherInvestigation` is true; epilogue analysis will be skipped too.
		return null;
	/* Field initialization analysis should not see constructor locals! Compute the index of the
	   first constructor parameter in flow data and discard that and all subsequent locals.
	*/
	TypeDeclaration typeDeclaration = this.scope.enclosingClassScope().referenceContext;
	int limit = typeDeclaration.maxFieldCount + this.scope.firstLocalIndex;
	return this.prologueInfo.prologueFlowInfo.unconditionalCopy().discardNonFieldInitializations(limit);
}

private void complainOnUnusedPrivateConstructor() {
	MethodBinding constructorBinding = this.binding;
	if (constructorBinding == null || constructorBinding.isUsed())
		return;
	if ((this.bits & ASTNode.IsDefaultConstructor) != 0)
		return;
	if (constructorBinding.isPrivate()) {
		if ((this.binding.declaringClass.tagBits & TagBits.HasNonPrivateConstructor) == 0)
			return; // tolerate as known pattern to block instantiation
	} else if (!constructorBinding.isOrEnclosedByPrivateType()) {
		return;
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=270446, When the AST built is an abridged version
	// we don't have all tree nodes we would otherwise expect. (see ASTParser.setFocalPosition)
	if (this.constructorCall == null)
		return;
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=264991, Don't complain about this
	// constructor being unused if the base class doesn't have a no-arg constructor.
	// See that a seemingly unused constructor that chains to another constructor with a
	// this(...) can be flagged as being unused without hesitation.
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142
	if (this.constructorCall.accessMode != ExplicitConstructorCall.This) {
		ReferenceBinding superClass = constructorBinding.declaringClass.superclass();
		if (superClass == null)
			return;
		// see if there is a no-arg super constructor
		MethodBinding methodBinding = superClass.getExactConstructor(Binding.NO_PARAMETERS);
		if (methodBinding == null)
			return;
		if (!methodBinding.canBeSeenBy(SuperReference.implicitSuperConstructorCall(), this.scope))
			return;
		ReferenceBinding declaringClass = constructorBinding.declaringClass;
		if (constructorBinding.isPublic() && constructorBinding.parameters.length == 0 && declaringClass.isStatic()
				&& declaringClass.findSuperTypeOriginatingFrom(TypeIds.T_JavaIoExternalizable, false) != null)
			return;
		// otherwise default super constructor exists, so go ahead and complain unused.
	}
	this.scope.problemReporter().unusedPrivateConstructor(this);
}
private void complainOnUnusedTypeVariables() {
	if (this.typeParameters != null  && !this.scope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
		for (TypeParameter typeParameter : this.typeParameters) {
			if ((typeParameter.binding.modifiers & ExtraCompilerModifiers.AccLocallyUsed) == 0) {
				this.scope.problemReporter().unusedTypeParameter(typeParameter);
			}
		}
	}
}

enum ConstructorFlowAnalysisMode {

	/** Analyse entire body in one go: used for Java 24- as the ONLY phase. Or the ONLY phase of a Java 25+ constructor that chains to an alternate via this(...)
	 *  In this mode, instance fields have ALREADY been flow analyzed and therefore `flowInfo` passed into `analyzeCode()` holds instance fields initialization info.
	 */
	FULL_ANALYSIS,

	/** Analyse up to chaining constructor invocation (although JEP 513 defines prologue to not include the call itself)
	 *  Evaluation of the arguments of the chaining constructor invocation is in early construction context/prologue.
	 *  Phase 1 of 2 for Java 25+ for a super(...) chaining constructor. Mode invoked BEFORE fields are analyzed.
	 */
	PROLOGUE_ANALYSIS,

	/** Skip the prologue and analyse only the statements after the explicit constructor call
	 *  Phase 2 of 2 for Java 25+ for a super(...) chaining constructor
	 *  In this mode, instance fields have ALREADY been flow analyzed and therefore `flowInfo` passed into `analyzeCode()` holds instance fields initialization info.
	 */
	EPILOGUE_ANALYSIS
}

public void analyseCode(ClassScope classScope, InitializationFlowContext initializerFlowContext, FlowInfo flowInfo, int classReachMode, ConstructorFlowAnalysisMode mode) {

	// FlowInfo produced during PROLOGUE_ANALYSIS will be held in the field prologueInfo for use during EPILOGUE_ANALYSIS
	// prologueInfo is gathered *before* any field initializers, see its use in TypeDeclaration.internalAnalyseCode()

	if (this.ignoreFurtherInvestigation || this.statements == null)
		return;

	try {
		CompilerOptions compilerOptions = this.scope.compilerOptions();
		boolean enableSyntacticNullAnalysisForFields = compilerOptions.enableSyntacticNullAnalysisForFields;
		int epilogReachMode, complaintLevel = Statement.NOT_COMPLAINED; // Clean start even if we barked inside field initializer that being a lexically disjoint region
		int cursor = 0;

		if (mode == PROLOGUE_ANALYSIS || mode == FULL_ANALYSIS) {
			this.constructorContext =
				new ExceptionHandlingFlowContext(
					initializerFlowContext.parent,
					this,
					this.binding.thrownExceptions,
					initializerFlowContext,
					this.scope,
					FlowInfo.DEAD_END);

			if (mode == PROLOGUE_ANALYSIS)              // flowInfo corresponds to TypeDeclaration
				epilogReachMode = FlowInfo.REACHABLE;   // Just to silence the compiler. We are going to halt analysis with prologue.
			else                                        // flowInfo corresponds to instance fields flow analysis output.
				epilogReachMode = flowInfo.reachMode(); // epilogue is reachable, if last instance field was reachable.

			this.scope.enterEarlyConstructionContext();
			// nullity, owning and mark as assigned
			analyseArguments(this.scope, flowInfo, initializerFlowContext, arguments(true), this.binding);
			complaintLevel = (classReachMode & FlowInfo.UNREACHABLE) == 0 ? Statement.NOT_COMPLAINED : Statement.COMPLAINED_FAKE_REACHABLE;
			flowInfo.setReachMode(classReachMode);
		} else {
			/* Passed in flowInfo corresponds to instance fields flow analysis output.
			   Fuse it with prologue flow info to arrive at flow info for epilogue analysis.
			*/
			if ((this.prologueInfo.prologueFlowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0) {
				epilogReachMode = this.prologueInfo.prologueFlowInfo.reachMode(); // epilogue is unreachable if prologue completes abruptly!
				complaintLevel = Statement.COMPLAINED_FAKE_REACHABLE; // Having already complained about ECC being unreachable, also don't complain about epilogue.
			} else {
				epilogReachMode = flowInfo.reachMode(); // epilogue is reachable, if last instance field was reachable.
			}

			flowInfo = this.prologueInfo.prologueFlowInfo.addInitializationsFrom(flowInfo); // compose flow info input for epilogue analysis.
			flowInfo.setReachMode(epilogReachMode);
			cursor = ++this.prologueInfo.eccIndex;
		}

		for (int length = this.statements.length; cursor < length; cursor++) { // cursor positioned at epilogue or at this.statements[0] depending on analysis mode.
			Statement statement = this.statements[cursor];
			if ((complaintLevel = statement.complainIfUnreachable(flowInfo, this.scope, complaintLevel, true)) < Statement.COMPLAINED_UNREACHABLE) {
				flowInfo = statement.analyseCode(this.scope, this.constructorContext, flowInfo);
			}
			if (enableSyntacticNullAnalysisForFields) {
				this.constructorContext.expireNullCheckedFieldInfo();
			}
			if (compilerOptions.analyseResourceLeaks) {
				FakedTrackingVariable.cleanUpUnassigned(this.scope, statement, flowInfo, false);
			}
			if (statement == this.constructorCall) {
				if (mode == PROLOGUE_ANALYSIS) {
					this.prologueInfo = new PrologueInfo(flowInfo.copy(), cursor);
					return;
				}
				if (this.constructorCall.accessMode == ExplicitConstructorCall.This)
					markFieldsAsInitializedAfterThisCall(this.constructorCall, flowInfo);
				flowInfo.setReachMode(epilogReachMode);
			}
		}


		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0) { // don't fall through the constructor!
			this.bits |= ASTNode.NeedFreeReturn;
		}

		if (this.isCompactConstructor()) {
			for (FieldBinding field : this.binding.declaringClass.fields()) {
				if (!field.isStatic()) {
					flowInfo.markAsDefinitelyAssigned(field);
				}
			}
		}

		// check missing blank final field initializations (plus @NonNull)
		if (this.constructorCall != null && this.constructorCall.accessMode != ExplicitConstructorCall.This) {
			flowInfo = flowInfo.mergedWith(this.constructorContext.initsOnReturn);
			doFieldReachAnalysis(flowInfo, this.binding.declaringClass.fields());
		}

		initializerFlowContext.checkInitializerExceptions(
				this.scope,
				this.constructorContext,
				flowInfo);

		// anonymous constructor can gain extra thrown exceptions from unhandled ones
		if (this.binding.declaringClass.isAnonymousType()) {
			List computedExceptions = this.constructorContext.extendedExceptions;
			if (computedExceptions != null) {
				int size;
				if ((size = computedExceptions.size()) > 0) {
					ReferenceBinding[] actuallyThrownExceptions;
					computedExceptions.toArray(actuallyThrownExceptions = new ReferenceBinding[size]);
					this.binding.thrownExceptions = actuallyThrownExceptions;
				}
			}
		}

		// Complain about unused { constructors, type variables, parameters, catch blocks } etc
		complainOnUnusedPrivateConstructor();
		if (isRecursive(null /*lazy initialized visited list*/)) { // check constructor recursion, now that all constructors got resolved
			this.scope.problemReporter().recursiveConstructorInvocation(this.constructorCall);
		}
		complainOnUnusedTypeVariables();
		this.constructorContext.complainIfUnusedExceptionHandlers(this);
		this.scope.checkUnusedParameters(this.binding);
		this.scope.checkUnclosedCloseables(flowInfo, null, null/*don't report against a specific location*/, null);
		this.constructorContext = null;
	} catch (AbortMethod e) {
		this.ignoreFurtherInvestigation = true;
	}
}

private void markFieldsAsInitializedAfterThisCall(ExplicitConstructorCall call, FlowInfo flowInfo) {

	/* We are chaining to `this(...)': Flag all non-static fields as definitely assigned
       since they are supposed to be set inside the alternate constructor. Which also means
       any final fields that are already assigned in the current prologue will result in
       duplicate initialization.
	*/
	FieldBinding[] fields = this.binding.declaringClass.fields();
	for (FieldBinding field : fields) {
		if (!field.isStatic()) {
			if (field.isBlankFinal() && flowInfo.isPotentiallyAssigned(field))
				this.scope.problemReporter().duplicateInitializationOfBlankFinalField(field, call);
			flowInfo.markAsDefinitelyAssigned(field);
		}
	}
}

@Override
public AbstractVariableDeclaration[] arguments(boolean includedElided) {
	return includedElided && this.isCompactConstructor() ? this.protoArguments : super.arguments(includedElided);
}

protected void doFieldReachAnalysis(FlowInfo flowInfo, FieldBinding[] fields) {
	for (FieldBinding field : fields) {
		if (!field.isStatic() && !flowInfo.isDefinitelyAssigned(field)) {
			if (field.isFinal()) {
				this.scope.problemReporter().uninitializedBlankFinalField(
						field,
						((this.bits & ASTNode.IsDefaultConstructor) != 0)
							? (ASTNode) this.scope.referenceType().declarationOf(field.original())
							: this);
			} else if (field.isNonNull() || field.type.isFreeTypeVariable()) {
				FieldDeclaration fieldDecl = this.scope.referenceType().declarationOf(field.original());
				if (!isValueProvidedUsingAnnotation(fieldDecl))
					this.scope.problemReporter().uninitializedNonNullField(
						field,
						((this.bits & ASTNode.IsDefaultConstructor) != 0)
							? (ASTNode) fieldDecl
							: this);
			}
		}
	}
}
boolean isValueProvidedUsingAnnotation(FieldDeclaration fieldDecl) {
	// a member field annotated with @Inject is considered to be initialized by the injector
	if (fieldDecl.annotations != null) {
		int length = fieldDecl.annotations.length;
		for (int i = 0; i < length; i++) {
			Annotation annotation = fieldDecl.annotations[i];
			int annotId = annotation.resolvedType.id;
			if (annotId == TypeIds.T_JavaxInjectInject || annotId == TypeIds.T_JakartaInjectInject) {
				return true; // no concept of "optional"
			} else if (annotId == TypeIds.T_ComGoogleInjectInject) {
				MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
				if (memberValuePairs == Annotation.NoValuePairs)
					return true;
				for (MemberValuePair memberValuePair : memberValuePairs) {
					// if "optional=false" is specified, don't rely on initialization by the injector:
					if (CharOperation.equals(memberValuePair.name, TypeConstants.OPTIONAL))
						return memberValuePair.value instanceof FalseLiteral;
				}
			} else if (annotId == TypeIds.T_OrgSpringframeworkBeansFactoryAnnotationAutowired) {
				MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
				if (memberValuePairs == Annotation.NoValuePairs)
					return true;
				for (MemberValuePair memberValuePair : memberValuePairs) {
					if (CharOperation.equals(memberValuePair.name, TypeConstants.REQUIRED))
						return memberValuePair.value instanceof TrueLiteral;
				}
			}
		}
	}
	return false;
}

@Override
public void generateCode(ClassScope classScope, ClassFile classFile) {
	int problemResetPC = 0;
	if (this.ignoreFurtherInvestigation) {
		if (this.binding == null)
			return; // Handle methods with invalid signature or duplicates
		int problemsLength;
		CategorizedProblem[] problems =
			this.scope.referenceCompilationUnit().compilationResult.getProblems();
		CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
		System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
		classFile.addProblemConstructor(this, this.binding, problemsCopy);
		return;
	}
	boolean restart = false;
	boolean abort = false;
	CompilationResult unitResult = null;
	int problemCount = 0;
	if (classScope != null) {
		TypeDeclaration referenceContext = classScope.referenceContext;
		if (referenceContext != null) {
			unitResult = referenceContext.compilationResult();
			problemCount = unitResult.problemCount;
		}
	}
	do {
		try {
			problemResetPC = classFile.contentsOffset;
			internalGenerateCode(classScope, classFile);
			restart = false;
		} catch (AbortMethod e) {
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
	if (abort) {
		int problemsLength;
		CategorizedProblem[] problems =
				this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
		CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
		System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
		classFile.addProblemConstructor(this, this.binding, problemsCopy, problemResetPC);
	}
}

public void generateSyntheticFieldInitializationsIfNecessary(MethodScope methodScope, CodeStream codeStream, ReferenceBinding declaringClass) {
	if (declaringClass instanceof NestedTypeBinding nestedType) {
		SyntheticArgumentBinding[] syntheticArgs = nestedType.syntheticEnclosingInstances();
		if (syntheticArgs != null) {
			for (SyntheticArgumentBinding syntheticArg : syntheticArgs) {
				if (syntheticArg.matchingField != null) {
					codeStream.aload_0();
					codeStream.load(syntheticArg);
					codeStream.fieldAccess(Opcodes.OPC_putfield, syntheticArg.matchingField, null /* default declaringClass */);
				}
			}
		}
		syntheticArgs = nestedType.syntheticOuterLocalVariables();
		if (syntheticArgs != null) {
			for (SyntheticArgumentBinding syntheticArg : syntheticArgs) {
				if (syntheticArg.matchingField != null) {
					codeStream.aload_0();
					codeStream.load(syntheticArg);
					codeStream.fieldAccess(Opcodes.OPC_putfield, syntheticArg.matchingField, null /* default declaringClass */);
				}
			}
		}
	}
}

private void internalGenerateCode(ClassScope classScope, ClassFile classFile) {
	classFile.generateMethodInfoHeader(this.binding);
	int methodAttributeOffset = classFile.contentsOffset;
	int attributeNumber = classFile.generateMethodInfoAttributes(this.binding);
	if ((!this.binding.isNative()) && (!this.binding.isAbstract())) {

		TypeDeclaration declaringType = classScope.referenceContext;
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);

		// initialize local positions - including initializer scope.
		ReferenceBinding declaringClass = this.binding.declaringClass;

		int enumOffset = declaringClass.isEnum() ? 2 : 0; // String name, int ordinal
		int argSlotSize = 1 + enumOffset; // this==aload0

		if (declaringClass.isNestedType()){
			this.scope.extraSyntheticArguments = declaringClass.syntheticOuterLocalVariables();
			this.scope.computeLocalVariablePositions(// consider synthetic arguments if any
					declaringClass.getEnclosingInstancesSlotSize() + 1 + enumOffset,
				codeStream);
			argSlotSize += declaringClass.getEnclosingInstancesSlotSize();
			argSlotSize += declaringClass.getOuterLocalVariablesSlotSize();
		} else {
			this.scope.computeLocalVariablePositions(1 + enumOffset,  codeStream);
		}

		for (LocalVariableBinding local : this.scope.locals) {
			if (local != null && local.isParameter()) {
				codeStream.addVisibleLocalVariable(local);
				local.recordInitializationStartPC(0);
				switch(local.type.id) {
					case TypeIds.T_long :
					case TypeIds.T_double :
						argSlotSize += 2;
						break;
					default :
						argSlotSize++;
						break;
				}
			}
		}

		MethodScope initializerScope = declaringType.initializerScope;
		initializerScope.computeLocalVariablePositions(argSlotSize + this.prologueLocalSlotSize, codeStream); // offset by the argument size (since not linked to method scope)

		codeStream.pushPatternAccessTrapScope(this.scope);
		boolean needFieldInitializations = this.constructorCall == null || this.constructorCall.accessMode != ExplicitConstructorCall.This;

		// Synthetic initializations occur prior to explicit constructor call
		if (needFieldInitializations){
			generateSyntheticFieldInitializationsIfNecessary(this.scope, codeStream, declaringClass);
			codeStream.recordPositionsFrom(0, this.bodyStart > 0 ? this.bodyStart : this.sourceStart);
		}

		this.scope.enterEarlyConstructionContext();

		// generate statements
		if (this.statements != null) {
			for (Statement statement : this.statements) {
				statement.generateCode(this.scope, codeStream);
				if (!this.compilationResult.hasErrors() && (codeStream.stackDepth != 0 || codeStream.operandStack.size() != 0)) {
					this.scope.problemReporter().operandStackSizeInappropriate(this);
				}
				if (this.constructorCall == statement && this.constructorCall.accessMode != ExplicitConstructorCall.This) {
					if ((this.constructorCall.bits & IsReachable) != 0)
						generateFieldInitializations(declaringType, codeStream, initializerScope); // The single bit in the field can only say it is reachable in *some* universe
				}
			}
		}
		// if a problem got reported during code gen, then trigger problem method creation
		if (this.ignoreFurtherInvestigation) {
			throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
		}
		if ((this.bits & ASTNode.NeedFreeReturn) != 0) {
			if (this.isCompactConstructor()) {
				// Note: the body of a compact constructor may not contain a return statement and so will need an injected return
				for (RecordComponent rc : classScope.referenceContext.recordComponents) {
					LocalVariableBinding parameter = this.scope.findVariable(rc.name);
					FieldBinding field = classScope.referenceContext.binding.getField(rc.name, true).original();
					codeStream.aload_0();
					codeStream.load(parameter);
					codeStream.fieldAccess(Opcodes.OPC_putfield, field, classScope.referenceContext.binding);
				}
			}
			codeStream.return_();
		}
		// See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796#issuecomment-1933458054
		codeStream.exitUserScope(this.scope, lvb -> !lvb.isParameter());
		codeStream.handleRecordAccessorExceptions(this.scope);
		// local variable attributes
		codeStream.exitUserScope(this.scope);
		codeStream.recordPositionsFrom(0, this.bodyEnd > 0 ? this.bodyEnd : this.sourceStart);
		try {
			classFile.completeCodeAttribute(codeAttributeOffset, this.scope);
		} catch(NegativeArraySizeException e) {
			throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
		}
		attributeNumber++;
		if ((codeStream instanceof StackMapFrameCodeStream)
				&& needFieldInitializations
				&& declaringType.fields != null) {
			((StackMapFrameCodeStream) codeStream).resetSecretLocals();
		}
	}
	classFile.completeMethodInfo(this.binding, methodAttributeOffset, attributeNumber);
}
private void generateFieldInitializations(TypeDeclaration declaringType, CodeStream codeStream, MethodScope initializerScope) {
	if (declaringType.fields != null) {
		for (FieldDeclaration field : declaringType.fields) {
			if (!field.isStatic())
				field.generateCode(initializerScope, codeStream);
		}
	}
}

@Override
public void getAllAnnotationContexts(int targetType, List allAnnotationContexts) {
	TypeReference fakeReturnType = new SingleTypeReference(this.selector, 0);
	fakeReturnType.resolvedType = this.binding.declaringClass;
	AnnotationCollector collector = new AnnotationCollector(fakeReturnType, targetType, allAnnotationContexts);
	for (Annotation annotation : this.annotations) {
		annotation.traverse(collector, (BlockScope) null);
	}
}

@Override
public boolean isConstructor() {
	return true;
}

public boolean invokesSuper() {
	return this.constructorCall != null && this.constructorCall.accessMode != ExplicitConstructorCall.This;
}

@Override
public boolean isCanonicalConstructor() {
	return (this.bits & ASTNode.IsCanonicalConstructor) != 0;
}

@Override
public boolean isCompactConstructor() {
	return (this.modifiers & ExtraCompilerModifiers.AccCompactConstructor) != 0;
}

@Override
public boolean isDefaultConstructor() {
	return (this.bits & ASTNode.IsDefaultConstructor) != 0;
}

@Override
public boolean isInitializationMethod() {
	return true;
}

/*
 * Returns true if the constructor is directly involved in a cycle.
 * Given most constructors aren't, we only allocate the visited list
 * lazily.
 */
public boolean isRecursive(ArrayList visited) {
	if (this.binding == null
			|| this.constructorCall == null
			|| this.constructorCall.binding == null
			|| this.constructorCall.isSuperAccess()
			|| !this.constructorCall.binding.isValidBinding()) {
		return false;
	}

	ConstructorDeclaration targetConstructor =
		((ConstructorDeclaration)this.scope.referenceType().declarationOf(this.constructorCall.binding.original()));
	if (targetConstructor == null) return false; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=358762
	if (this == targetConstructor) return true; // direct case

	if (visited == null) { // lazy allocation
		visited = new ArrayList(1);
	} else {
		int index = visited.indexOf(this);
		if (index >= 0) return index == 0; // only blame if directly part of the cycle
	}
	visited.add(this);

	return targetConstructor.isRecursive(visited);
}

@Override
public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
	// fill up the constructor body with its statements
    if (((this.bits & ASTNode.IsDefaultConstructor) != 0) && this.statements == null) {
    	chainUpwards();
        return;
    }
	parser.parse(this, unit, false);
}

@Override
public StringBuilder printBody(int indent, StringBuilder output) {
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

@Override
public void resolveJavadoc() {
	if (this.binding == null || this.javadoc != null) {
		super.resolveJavadoc();
	} else if ((this.bits & ASTNode.IsDefaultConstructor) == 0 ) {
		if (this.binding.declaringClass != null && !this.binding.declaringClass.isLocalType()) {
			// Set javadoc visibility
			int javadocVisibility = this.binding.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
			ClassScope classScope = this.scope.classScope();
			ProblemReporter reporter = this.scope.problemReporter();
			int severity = reporter.computeSeverity(IProblem.JavadocMissing);
			if (severity != ProblemSeverities.Ignore) {
				if (classScope != null) {
					javadocVisibility = Util.computeOuterMostVisibility(classScope.referenceType(), javadocVisibility);
				}
				int javadocModifiers = (this.binding.modifiers & ~ExtraCompilerModifiers.AccVisibilityMASK) | javadocVisibility;
				reporter.javadocMissing(this.sourceStart, this.sourceEnd, severity, javadocModifiers);
			}
		}
	}
}

@Override
public void resolve(ClassScope upperScope) {

	if (this.binding != null && this.binding.isCanonicalConstructor()) {
		RecordComponentBinding[] rcbs = upperScope.referenceContext.binding.components();
		boolean lastComponentVarargs = rcbs.length > 0 && rcbs[rcbs.length - 1].sourceRecordComponent().isVarArgs();
		if (this.binding.isVarargs() != lastComponentVarargs)
			upperScope.problemReporter().erasureIncompatibilityInCanonicalConstructor(this.arguments[this.arguments.length - 1].type);
		for (int i = 0; i < rcbs.length; ++i) {
			TypeBinding mpt = this.binding.parameters[i];
			TypeBinding rct = rcbs[i].type;
			if (TypeBinding.notEquals(mpt, rct))
				upperScope.problemReporter().erasureIncompatibilityInCanonicalConstructor(this.arguments[i].type);
		}

		if (!this.binding.isAsVisible(this.binding.declaringClass))
			this.scope.problemReporter().canonicalConstructorVisibilityReduced(this);
		if (this.typeParameters != null && this.typeParameters.length > 0)
			this.scope.problemReporter().canonicalConstructorShouldNotBeGeneric(this);
		if (this.binding.thrownExceptions != null && this.binding.thrownExceptions.length > 0)
			this.scope.problemReporter().canonicalConstructorHasThrowsClause(this);
		if (!this.isCompactConstructor()) {
			for (int i = 0; i < rcbs.length; i++)
				if (!CharOperation.equals(this.arguments[i].name, rcbs[i].name))
					this.scope.problemReporter().mismatchedParameterNameInCanonicalConstructor(rcbs[i], this.arguments[i]);
		}
	}
	super.resolve(upperScope);
}

private void normalizeConstructor() {
	// We know `this.constructorCall != null && (this.statements == null || this.statements.length == 0 || this.constructorCall != this.statements[0])`
	for (int i = 1, length = this.statements != null ? this.statements.length : 0; i < length; i++) {
		if (this.constructorCall == this.statements[i])
			return; // Already normalized. We have a constructor with a prologue with the chaining constructor invocation past the prologue.
	}
	// We are likely here with a constructor that is synthesized by a tool such as Lombok that still uses bifurcated body (see https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5349)
	Statement [] oldBody = this.statements;
	int oldLength = oldBody != null ? oldBody.length : 0;
	this.statements = new Statement[oldLength + 1];
	this.statements[0] = this.constructorCall;
	if (oldLength > 0)
		System.arraycopy(oldBody, 0, this.statements, 1, oldLength);
}
/*
 * Type checking for constructor, just another method, except for special check
 * for recursive constructor invocations.
 */
@Override
public void resolveStatements() {
	SourceTypeBinding sourceType = this.scope.enclosingSourceType();
	if (!CharOperation.equals(sourceType.sourceName, this.selector)){
		this.scope.problemReporter().missingReturnType(this);
	}
	// typeParameters are already resolved from Scope#connectTypeVariables()
	if (this.binding != null && !this.binding.isPrivate()) {
		sourceType.tagBits |= TagBits.HasNonPrivateConstructor;
	}
	if ((this.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0) {
		this.scope.problemReporter().methodNeedBody(this);
	}
	if (this.constructorCall != null)
		if (this.statements == null || this.statements.length == 0 || this.constructorCall != this.statements[0])
			normalizeConstructor(); // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5489
	this.scope.enterEarlyConstructionContext();
	super.resolveStatements();
	this.scope.leaveEarlyConstructionContext(); // code completion may work with diet mode constructors! These don't have ecc to issue leave!
	if (sourceType.id == TypeIds.T_JavaLangObject) {
		if (this.constructorCall != null && this.constructorCall.accessMode != ExplicitConstructorCall.This) {
			if (this.constructorCall.accessMode == ExplicitConstructorCall.Super)
				this.scope.problemReporter().cannotUseSuperInJavaLangObject(this.constructorCall);
			for (int i = 0, length = this.statements != null ? this.statements.length : 0; i < length; i++) { // we can come here with purged body.
				if (this.statements[i] == this.constructorCall) {
					this.statements[i] = new EmptyStatement(this.constructorCall.sourceStart, this.constructorCall.sourceEnd);
					break;
				}
			}
			this.constructorCall = null;
		}
	}
}

public final void chainUpwards() {
	buildBody(ASTNode.NO_STATEMENTS, 0, 0, null);
}

public final void buildBody(ASTNode [] astStack, int astPtr, int length, /* @Nullable */CompilerOptions options) {

	for (int i = 0; i < length; i++) {
		Statement statement = (Statement) astStack[astPtr + i];
	    if (statement instanceof ExplicitConstructorCall ecc) {
	    	if (i == 0 || (options != null && JavaFeature.FLEXIBLE_CONSTRUCTOR_BODIES.isSupported(options))) {
	    		System.arraycopy(astStack, astPtr, this.statements = new Statement[length], 0, length);
	    		this.constructorCall = ecc;
	    		return;
	    	}
	    }
	}

	boolean superCallPrecedes = true; // for JEP 401, super call is added to the tail end of the constructor

	this.statements = new Statement[length + 1];

	this.constructorCall = SuperReference.implicitSuperConstructorCall();
	this.constructorCall.sourceEnd = this.sourceEnd;
	this.constructorCall.sourceStart = this.sourceStart;

	if (superCallPrecedes) {
		this.statements[0] = this.constructorCall;
		if (length > 0)
			System.arraycopy(astStack, astPtr, this.statements, 1, length);
	} else {
		this.statements[length] = this.constructorCall;
		if (length > 0)
			System.arraycopy(astStack, astPtr, this.statements, 0, length);
	}
}

@Override
public void traverse(ASTVisitor visitor, ClassScope classScope) {
	if (visitor.visit(this, classScope)) {
		if (this.javadoc != null) {
			this.javadoc.traverse(visitor, this.scope);
		}
		if (this.annotations != null) {
			int annotationsLength = this.annotations.length;
			for (int i = 0; i < annotationsLength; i++)
				this.annotations[i].traverse(visitor, this.scope);
		}
		if (this.typeParameters != null) {
			int typeParametersLength = this.typeParameters.length;
			for (int i = 0; i < typeParametersLength; i++) {
				this.typeParameters[i].traverse(visitor, this.scope);
			}
		}
		if (this.arguments != null) {
			int argumentLength = this.arguments.length;
			for (int i = 0; i < argumentLength; i++)
				this.arguments[i].traverse(visitor, this.scope);
		}
		if (this.thrownExceptions != null) {
			int thrownExceptionsLength = this.thrownExceptions.length;
			for (int i = 0; i < thrownExceptionsLength; i++)
				this.thrownExceptions[i].traverse(visitor, this.scope);
		}
		if (this.statements != null) {
			int statementsLength = this.statements.length;
			for (int i = 0; i < statementsLength; i++)
				this.statements[i].traverse(visitor, this.scope);
		}
	}
	visitor.endVisit(this, classScope);
}
@Override
public TypeParameter[] typeParameters() {
    return this.typeParameters;
}

public void computePrologueLocalsSize() {
   this.prologueLocalSlotSize = 0;
   for (LocalVariableBinding local : this.scope.locals) {
       if (local == null || local.isParameter()) // accounted for elsewhere in argSlotSize
           continue;
       switch(local.type.id) {
           case TypeIds.T_long :
           case TypeIds.T_double :
               this.prologueLocalSlotSize += 2;
               break;
           default :
               this.prologueLocalSlotSize++;
               break;
       }
   }
   // we can ignore subscopes because super()/this() has to be in constructor's main scope
}
}
