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
 *     							bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *     							bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 265744 - Enum switch should warn about missing default
 *								bug 374605 - Unreasonable warning for enum-based switch statements
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *     Advantest R & D - Enhanced Switch v2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ClassFile.CONSTANT_BOOTSTRAP__GET_STATIC_FINAL;
import static org.eclipse.jdt.internal.compiler.ClassFile.CONSTANT_BOOTSTRAP__PRIMITIVE_CLASS;

import java.lang.invoke.ConstantBootstraps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement.LabelExpression;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CaseLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.codegen.Label;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.SwitchFlowContext;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class SwitchStatement extends Expression {

	/** Descriptor for a bootstrap method that is created only once but can be used more than once. */
	public static record SingletonBootstrap(String id, char[] selector, char[] signature) { }
	/** represents {@link ConstantBootstraps#primitiveClass(java.lang.invoke.MethodHandles.Lookup, String, Class)}*/
	public static final SingletonBootstrap PRIMITIVE_CLASS__BOOTSTRAP = new SingletonBootstrap(
			CONSTANT_BOOTSTRAP__PRIMITIVE_CLASS, PRIMITIVE_CLASS, PRIMITIVE_CLASS__SIGNATURE);
	/** represents {@link ConstantBootstraps#getStaticFinal(java.lang.invoke.MethodHandles.Lookup, String, Class)}*/
	public static final SingletonBootstrap GET_STATIC_FINAL__BOOTSTRAP = new SingletonBootstrap(
			CONSTANT_BOOTSTRAP__GET_STATIC_FINAL, GET_STATIC_FINAL, GET_STATIC_FINAL__SIGNATURE);

	public Expression expression;
	public Statement[] statements;
	public BlockScope scope;
	public int explicitDeclarations;
	public int blockStart;
	public BranchLabel breakLabel;
	private CaseLabel defaultLabel;


	public int caseCount; // count of all cases *including* default
	public CaseStatement[] cases; // all cases *including* default
	public CaseStatement defaultCase;
	public CaseStatement nullCase; // convenience pointer for pattern switches
	public CaseStatement unconditionalPatternCase;

	public static final LabelExpression[] NO_LABEL_EXPRESSIONS = new LabelExpression[0];
	public LabelExpression[] labelExpressions = NO_LABEL_EXPRESSIONS;
	public int labelExpressionIndex = 0;

	public int nConstants;
	public int switchBits;

	public boolean containsPatterns;
	public boolean containsRecordPatterns;
	public boolean containsNull;
	boolean nullProcessed = false;
	BranchLabel switchPatternRestartTarget;

	// fallthrough
	public final static int CASE = 0;
	public final static int FALLTHROUGH = 1;
	public final static int BREAKING  = 2;

	public final static int LabeledRules = ASTNode.Bit1;
	public final static int InvalidSelector = ASTNode.Bit2;
	public final static int Exhaustive = ASTNode.Bit3;
	public final static int QualifiedEnum = ASTNode.Bit4;
	public final static int LabeledBlockStatementGroup = ASTNode.Bit5;
	public final static int BarricadeInjectedDefault = ASTNode.Bit6;
	public final static int HasNondefaultCase = ASTNode.Bit7;

	// for switch on strings
	private static final char[] SecretSelectorVariableName = " selector".toCharArray(); //$NON-NLS-1$

	public SyntheticMethodBinding synthetic; // use for switch on enums types

	// for local variables table attributes
	int preSwitchInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	LocalVariableBinding selector = null;

	/* package */ boolean isNonTraditional = false;
	/* package */ boolean isPrimitiveSwitch = false;
	/* package */ List<Pattern> caseLabelElements = new ArrayList<>(0);//TODO: can we remove this?
	public List<TypeBinding> caseLabelElementTypes = new ArrayList<>(0);

	abstract class Node {
		TypeBinding type;
		boolean hasError = false;
		public abstract void traverse(CoverageCheckerVisitor visitor);
	}

	class RNode extends Node {

		TNode firstComponent;

		RNode(TypeBinding rec) {
			this.type = rec;
			RecordComponentBinding[] comps = rec.components();
			int len = comps != null ? comps.length : 0;
			if (len > 0) {
				RecordComponentBinding comp = comps[0];
				if (comp != null && comp.type != null)
					this.firstComponent = new TNode(comp.type);
			}
		}

		void addPattern(Pattern p) {
			if (p instanceof RecordPattern rp && TypeBinding.equalsEquals(this.type, rp.type.resolvedType) && this.firstComponent != null)
				this.firstComponent.addPattern(rp, 0);
		}

		@Override
		public String toString() {
			return "[RNode] {\n    type:" + this.type + "     firstComponent:" + this.firstComponent + "\n}\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		@Override
		public void traverse(CoverageCheckerVisitor visitor) {
			if (this.firstComponent != null) {
				visitor.visit(this.firstComponent);
			}
		}
	}

	class TNode extends Node {

		List<PatternNode> children;

		TNode(TypeBinding type) {
			this.type = type;
			this.children = new ArrayList<>();
		}

		public void addPattern(RecordPattern rp, int i) {
			if (rp.patterns.length <= i) {
				this.hasError = true;
				return;
			}
			TypeBinding childType = rp.patterns[i].resolvedType;
			PatternNode child = null;
			for (PatternNode c : this.children) {
				if (TypeBinding.equalsEquals(childType, c.type)) {
					child = c;
					break;
				}
			}
			if (child == null) {
				child = new PatternNode(childType);
				if (this.type.isSubtypeOf(childType, false))
					this.children.add(0, child);
				else
					this.children.add(child);
			}
			if ((i+1) < rp.patterns.length) {
				child.addPattern(rp, i + 1);
			}
		}

		@Override
		public String toString() {
	        StringBuilder sb = new StringBuilder("[TNode] {\n    type:" + this.type + "    children:"); //$NON-NLS-1$ //$NON-NLS-2$
	        if (this.children == null) {
	        	sb.append("null"); //$NON-NLS-1$
	        } else {
	        	for (Node child : this.children) {
	        		sb.append(child);
	        	}
	        }
	        return sb.append("\n}\n").toString(); //$NON-NLS-1$
		}

		@Override
		public void traverse(CoverageCheckerVisitor visitor) {
			visitor.visit(this);
		}
	}

	class PatternNode extends Node {

		TNode next; // next component

		PatternNode(TypeBinding type) {
			this.type = type;
		}

		public void addPattern(RecordPattern rp, int i) {
			TypeBinding ref = SwitchStatement.this.expression.resolvedType;
			RecordComponentBinding[] comps = ref.components();
			if (comps == null || comps.length <= i) // safety-net for incorrect code.
				return;
			if (this.next == null)
				this.next = new TNode(comps[i].type);
			this.next.addPattern(rp, i);
		}

		@Override
		public String toString() {
	        return "[" + (this.type.isRecord() ? "Record" : "") + "Pattern node] {\n    type:" + this.type + "    next:" + this.next + "\n}\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}

		@Override
		public void traverse(CoverageCheckerVisitor visitor) {
			if (this.next != null) {
				visitor.visit(this.next);
			}
		}
	}

	class CoverageCheckerVisitor {

		public boolean covers = true;

		public boolean visit(TNode node) {
			if (node.hasError)
				return false;

			List<TypeBinding> availableTypes = new ArrayList<>();
			if (node.children != null) {
				for (Node child : node.children) {
					if (node.type.isSubtypeOf(child.type, false))
						this.covers = true;
					child.traverse(this);
					if (node.type.isSubtypeOf(child.type, false) && this.covers)
						return false; // no further visit required - covering!
					availableTypes.add(child.type);
				}
			}
			if (node.type instanceof ReferenceBinding ref && ref.isSealed()) {
				this.covers &= caseElementsCoverSealedType(ref, availableTypes);
				return this.covers;
			}
			return this.covers = false; // no need to visit further.
		}
	}

	private void preprocess() { // make a pass over the switch block and allocate vectors.
		int n = 0;
		for (final Statement statement : this.statements) {
			if (statement instanceof CaseStatement caseStatement) {
				n++;
				int count = 0;
				for (Expression e : caseStatement.peeledLabelExpressions()) {
					if (e instanceof FakeDefaultLiteral)
						continue;
					++count;
				}
				this.nConstants += count;
			}
		}
		this.labelExpressions = new LabelExpression[this.nConstants];
		this.cases = new CaseStatement[n];
	}

	boolean integralType(TypeBinding type) {
		return switch (type.id) {
			case TypeIds.T_char, TypeIds.T_byte, TypeIds.T_short, TypeIds.T_int,
			     TypeIds.T_JavaLangCharacter, TypeIds.T_JavaLangByte, TypeIds.T_JavaLangShort, TypeIds.T_JavaLangInteger -> true;
			     default -> false;
		};
	}

	private boolean duplicateConstant(LabelExpression current, LabelExpression prior) {
		if (current.expression instanceof Pattern || prior.expression instanceof Pattern)
			return false; // apples and oranges
		if (current.expression instanceof NullLiteral ^ prior.expression instanceof NullLiteral) // I actually got to use XOR! :)
			return false;
		if (current.constant.equals(prior.constant))
			return true;
		if (current.type.id == TypeIds.T_boolean)
			this.switchBits |= Exhaustive; // 2 different boolean constants => exhaustive :)
		return false;
	}

	void gatherLabelExpression(LabelExpression labelExpression) {
		// domination check
		if (labelExpression.expression instanceof Pattern pattern) {
			if (this.defaultCase != null) {
				this.scope.problemReporter().patternDominatedByAnother(pattern);
			} else {
				for (int i = 0; i < this.labelExpressionIndex; i++) {
					if (this.labelExpressions[i].expression instanceof Pattern priorPattern && priorPattern.dominates(pattern)) {
						this.scope.problemReporter().patternDominatedByAnother(pattern);
						break;
					}
				}
			}
		} else {
			if (labelExpression.expression instanceof NullLiteral) {
				if (this.defaultCase != null)
					this.scope.problemReporter().patternDominatedByAnother(labelExpression.expression);
			} else {
				TypeBinding boxedType = labelExpression.type.isBaseType() ? this.scope.environment().computeBoxingType(labelExpression.type) : labelExpression.type;
				for (int i = 0; i < this.labelExpressionIndex; i++) {
					if (this.labelExpressions[i].expression instanceof Pattern priorPattern && priorPattern.coversType(boxedType, this.scope)) {
						this.scope.problemReporter().patternDominatedByAnother(labelExpression.expression);
						break;
					}
				}
			}
			// duplicate constant check
			for (int i = 0; i < this.labelExpressionIndex; i++) {
				if (duplicateConstant(labelExpression, this.labelExpressions[i])) {
					this.scope.problemReporter().duplicateCase(labelExpression.expression);
					break;
				}
			}
		}
		this.labelExpressions[this.labelExpressionIndex++] = labelExpression;
	}

	private void complainIfNotExhaustiveSwitch(BlockScope upperScope, TypeBinding selectorType, CompilerOptions compilerOptions) {

		boolean isEnhanced = isEnhancedSwitch(upperScope, selectorType);
		if (selectorType != null && selectorType.isEnum()) {
			if (isEnhanced)
				this.switchBits |= SwitchStatement.Exhaustive; // negated below if found otherwise
			if (this.defaultCase != null && !compilerOptions.reportMissingEnumCaseDespiteDefault)
				return;

			int casesCount =  this.caseCount;
			if (this.defaultCase != null && this.defaultCase.constantExpressions == NO_EXPRESSIONS)
				casesCount--; // discount the default

			int constantCount = this.labelExpressions.length;
			if (this.unconditionalPatternCase == null && (this.containsPatterns || this.containsNull || (constantCount >= casesCount && constantCount != selectorType.enumConstantCount()))) {
				Set<FieldBinding> unenumeratedConstants = unenumeratedConstants(selectorType, constantCount);
				if (unenumeratedConstants.size() != 0) {
					this.switchBits &= ~SwitchStatement.Exhaustive;
					if (!(this.defaultCase != null && (this.defaultCase.bits & DocumentedCasesOmitted) != 0)) {
						if (isEnhanced)
							upperScope.problemReporter().enhancedSwitchMissingDefaultCase(this.expression);
						else {
							for (FieldBinding enumConstant : unenumeratedConstants)
								reportMissingEnumConstantCase(upperScope, enumConstant);
						}
					}
				}
			}

			if (this.defaultCase == null) {
			    if (this instanceof SwitchExpression || compilerOptions.getSeverity(CompilerOptions.MissingDefaultCase) == ProblemSeverities.Ignore) // complained about elsewhere, don't also bark here
					upperScope.methodScope().hasMissingSwitchDefault = true;
				else
					upperScope.problemReporter().missingDefaultCase(this, true, selectorType);
			}
			return;
		}

		if (isExhaustive() || this.defaultCase != null || selectorType == null) {
			if (isEnhanced)
				this.switchBits |= SwitchStatement.Exhaustive;
			return;
		}

		if (JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions) && selectorType.isSealed() && caseElementsCoverSealedType((ReferenceBinding) selectorType, this.caseLabelElementTypes))
			this.switchBits |= SwitchStatement.Exhaustive;
		else if (selectorType.isRecordWithComponents() && this.containsRecordPatterns && caseElementsCoverRecordType(upperScope, compilerOptions, (ReferenceBinding) selectorType))
			this.switchBits |= SwitchStatement.Exhaustive;

		if (!isExhaustive()) {
			if (isEnhanced)
				upperScope.problemReporter().enhancedSwitchMissingDefaultCase(this.expression);
			else
				upperScope.problemReporter().missingDefaultCase(this, false, selectorType);
		}
	}

	// Return the set of enumerations belonging to the selector enum type that are NOT listed in case statements.
	private Set<FieldBinding> unenumeratedConstants(TypeBinding enumType, int constantCount) {
		FieldBinding[] enumFields = enumType.erasure().fields();
		Set<FieldBinding> unenumerated = new HashSet<>(Arrays.asList(enumFields));
		for (int i = 0, max = enumFields.length; i < max; i++) {
			FieldBinding enumConstant = enumFields[i];
			if ((enumConstant.modifiers & ClassFileConstants.AccEnum) == 0) {
				unenumerated.remove(enumConstant);
				continue;
			}
			for (int j = 0; j < constantCount; j++) {
				if (TypeBinding.equalsEquals(this.labelExpressions[j].expression.resolvedType, enumType)) {
					if (this.labelExpressions[j].expression instanceof NameReference reference) {
						if (enumConstant.id == reference.fieldBinding().original().id) {
							unenumerated.remove(enumConstant);
							break;
						}
					}
				}
			}
		}
		return unenumerated;
	}

	private boolean isExhaustive() {
		return (this.switchBits & SwitchStatement.Exhaustive) != 0;
	}

	private boolean isEnhancedSwitch(BlockScope upperScope, TypeBinding expressionType) {
		if (expressionType == null || this instanceof SwitchExpression)
			return false;
		if (JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(upperScope.compilerOptions())) {
			boolean nonTraditionalSelector = !expressionType.isEnum();
			switch (expressionType.id) {
				case TypeIds.T_char, TypeIds.T_byte, TypeIds.T_short, TypeIds.T_int,
				     TypeIds.T_long, TypeIds.T_double, TypeIds.T_boolean, TypeIds.T_float,
					 TypeIds.T_void, TypeIds.T_JavaLangCharacter, TypeIds.T_JavaLangByte,
					 TypeIds.T_JavaLangShort, TypeIds.T_JavaLangInteger, TypeIds.T_JavaLangString:
						 nonTraditionalSelector = false;
			}
			if (nonTraditionalSelector || this.containsPatterns || this.containsNull) {
				return true;
			}
		}
		if (JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(upperScope.compilerOptions())) {
			switch (expressionType.id) {
				case TypeIds.T_float, TypeIds.T_double, TypeIds.T_long, TypeIds.T_boolean, TypeIds.T_JavaLangFloat,
				     TypeIds.T_JavaLangDouble, TypeIds.T_JavaLangLong, TypeIds.T_JavaLangBoolean:
				    	 return true;
			}
		}
		return false;
	}

	private boolean caseElementsCoverRecordType(BlockScope skope, CompilerOptions compilerOptions, ReferenceBinding recordType) {
		RNode head = new RNode(recordType);
		for (Pattern pattern : this.caseLabelElements) {
			head.addPattern(pattern);
		}
		CoverageCheckerVisitor ccv = new CoverageCheckerVisitor();
		head.traverse(ccv);
		return ccv.covers;
	}

	private boolean caseElementsCoverSealedType(ReferenceBinding sealedType,  List<TypeBinding> listedTypes) {
		List<ReferenceBinding> allAllowedTypes = sealedType.getAllEnumerableAvatars();
		Iterator<ReferenceBinding> iterator = allAllowedTypes.iterator();
		while (iterator.hasNext()) {
			ReferenceBinding next = iterator.next();
			if (next.isAbstract() && next.isSealed()) {
				/* Per JLS 14.11.1.1: A type T that names an abstract sealed class or sealed interface is covered
				   if every permitted direct subclass or subinterface of it is covered. These subtypes are already
				   added to allAllowedTypes and subject to cover test.
				*/
				iterator.remove();
				continue;
			}
			if (next.isEnum()) {
				int constantCount = this.labelExpressions.length;
				Set<FieldBinding> unenumeratedConstants = unenumeratedConstants(next, constantCount);
				if (unenumeratedConstants.size() == 0) {
					iterator.remove();
					continue;
				}
			}
			for (TypeBinding type : listedTypes) {
				// permits specifies classes, not parameterizations
				if (next.erasure().isCompatibleWith(type.erasure())) {
					iterator.remove();
					break;
				}
			}
		}
		return allAllowedTypes.size() == 0;
	}

	private void reserveSecretVariablesSlot() { // may be released later if unused.
		this.selector  = new LocalVariableBinding(SecretSelectorVariableName, this.scope.getJavaLangObject(), ClassFileConstants.AccDefault, false);
		this.scope.addLocalVariable(this.selector);
		this.selector.setConstant(Constant.NotAConstant);
	}

	private void releaseUnusedSecretVariable() {
		if (this.selector != null) {
			if (this.expression.resolvedType.id == T_JavaLangString && !this.isNonTraditional) {
				this.selector.useFlag = LocalVariableBinding.USED;
				this.selector.type = this.scope.getJavaLangString();
			} else if (indySwitch()) {
				this.selector.useFlag = LocalVariableBinding.USED;
			    this.selector.type = this.expression.resolvedType;
			} else {
				this.selector = null;
			}
		}
	}

	private boolean indySwitch() {
		if (this.containsPatterns || this.containsNull || (this.switchBits & QualifiedEnum) != 0)
			return true;
		TypeBinding eType = this.expression.resolvedType;
		if (eType == null)
			return false;
		switch (eType.id) {
			case TypeIds.T_JavaLangLong, TypeIds.T_JavaLangFloat, TypeIds.T_JavaLangDouble:
				return true;
			case TypeIds.T_long, TypeIds.T_double, TypeIds.T_float :
				if (this.isPrimitiveSwitch)
					return true;
			// note: if no patterns are present we optimize Boolean to use unboxing rather than indy typeSwitch
		}
		return !(eType.isPrimitiveOrBoxedPrimitiveType() || eType.isEnum() || eType.id == TypeIds.T_JavaLangString); // classic selectors
	}

	@Override
	public void resolve(BlockScope upperScope) {
		try {
			TypeBinding expressionType = this.expression.resolveType(upperScope);
			if (expressionType != null && !expressionType.isValidBinding())
				expressionType = null; // fault-tolerance: ignore further type mismatch from label expressions
			CompilerOptions compilerOptions = upperScope.compilerOptions();
			if (expressionType != null) {
				this.expression.computeConversion(upperScope, expressionType, expressionType);
				checkType: {
					if (expressionType.isBaseType()) {
						if (expressionType.id == TypeIds.T_void) {
							upperScope.problemReporter().illegalVoidExpression(this.expression);
							break checkType;
						}
						if (JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(compilerOptions)) {
							upperScope.referenceContext().compilationResult().usesPreview = true;
							this.isPrimitiveSwitch = true;
						}
						if (this.expression.isConstantValueOfTypeAssignableToType(expressionType, TypeBinding.INT))
							break checkType;
						if (expressionType.isCompatibleWith(TypeBinding.INT))
							break checkType;
					}
					if (expressionType.id == TypeIds.T_JavaLangString || expressionType.isEnum() || upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT))
						break checkType;
					if (JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions) && (!expressionType.isBaseType() || expressionType.id == T_null || expressionType.id == T_void)) {
						this.isNonTraditional = true;
					} else {
						if (!this.isPrimitiveSwitch) { // when isPrimitiveSwitch is set it is approved above
							upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType);
							expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
						}
					}
				}
			}

			this.scope = new BlockScope(upperScope);
			if (expressionType != null)
				reserveSecretVariablesSlot();
			else
				this.switchBits |= InvalidSelector;

			if (this.statements != null) {
				preprocess(); // make a pass over the switch block and allocate vectors.
				LocalVariableBinding[] patternVariables = NO_VARIABLES;
				boolean trueSeen = false, falseSeen = false;
				for (final Statement statement : this.statements) {
					if (statement instanceof CaseStatement caseStatement) {
						caseStatement.swich = this;
						caseStatement.resolve(this.scope);
						patternVariables = caseStatement.bindingsWhenTrue();
						Boolean booleanConstant = caseStatement.getBooleanConstantValue();
						if (booleanConstant == Boolean.TRUE)
							trueSeen = true;
						else if (booleanConstant == Boolean.FALSE)
							falseSeen = true;
					} else {
						statement.resolveWithBindings(patternVariables, this.scope);
						patternVariables = LocalVariableBinding.merge(patternVariables, statement.bindingsWhenComplete());
					}
				}
				if (expressionType != null
						&& (expressionType.id == TypeIds.T_boolean || expressionType.id == TypeIds.T_JavaLangBoolean)
						&& this.defaultCase != null  && trueSeen && falseSeen) {
					upperScope.problemReporter().caseDefaultPlusTrueAndFalse(this);
				}
				if (this.labelExpressions.length != this.labelExpressionIndex)
					System.arraycopy(this.labelExpressions, 0, this.labelExpressions = new LabelExpression[this.labelExpressionIndex], 0, this.labelExpressionIndex);
			} else {
				if ((this.bits & UndocumentedEmptyBlock) != 0)
					upperScope.problemReporter().undocumentedEmptyBlock(this.blockStart, this.sourceEnd);
			}

			if (expressionType != null) {
				if (!expressionType.isBaseType() && upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT)) {
					if (!this.containsPatterns && !this.containsNull)
						this.expression.computeConversion(upperScope, TypeBinding.INT, expressionType);
				}
				releaseUnusedSecretVariable();
				complainIfNotExhaustiveSwitch(upperScope, expressionType, compilerOptions);
			}

		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}

	protected void reportMissingEnumConstantCase(BlockScope upperScope, FieldBinding enumConstant) {
		upperScope.problemReporter().missingEnumConstantCase(this, enumConstant);
	}

	/**
	 * Dispatch the call on its last statement.
	 */
	@Override
	public void branchChainTo(BranchLabel label) {

		// in order to improve debug attributes for stepping (11431)
		// we want to inline the jumps to #breakLabel which already got
		// generated (if any), and have them directly branch to a better
		// location (the argument label).
		// we know at this point that the breakLabel already got placed
		if (this.breakLabel.forwardReferenceCount() > 0) {
			label.becomeDelegateFor(this.breakLabel);
		}
	}

	protected boolean needToCheckFlowInAbsenceOfDefaultBranch() {
		return !this.isExhaustive();
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		try {
			flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo);
			if (!this.containsNull && this.expression.resolvedType instanceof ReferenceBinding)
				this.expression.checkNPE(currentScope, flowContext, flowInfo, 1);

			SwitchFlowContext switchContext = new SwitchFlowContext(flowContext, this, (this.breakLabel = new BranchLabel()), true, true);

			CompilerOptions compilerOptions = currentScope.compilerOptions();

			// analyse the block by considering specially the case/default statements (need to bind them to the entry point)
			FlowInfo caseInits = FlowInfo.DEAD_END;
			// in case of statements before the first case
			this.preSwitchInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
			if (this.statements != null) {
				int initialComplaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0 ? Statement.COMPLAINED_FAKE_REACHABLE : Statement.NOT_COMPLAINED;
				int complaintLevel = initialComplaintLevel;
				int fallThroughState = CASE;
				int prevCaseStmtIndex = -100;
				for (int i = 0, max = this.statements.length; i < max; i++) {
					Statement statement = this.statements[i];
					if (statement instanceof CaseStatement caseStatement) {
						this.scope.enclosingCase = caseStatement; // record entering in a switch case block
						if (prevCaseStmtIndex == i - 1 && this.statements[prevCaseStmtIndex].containsPatternVariable())
							this.scope.problemReporter().illegalFallthroughFromAPattern(this.statements[prevCaseStmtIndex]);
						prevCaseStmtIndex = i;
						if (fallThroughState == FALLTHROUGH && complaintLevel <= NOT_COMPLAINED) {
							if (statement.containsPatternVariable())
								this.scope.problemReporter().IllegalFallThroughToPattern(this.scope.enclosingCase);
							else if ((statement.bits & ASTNode.DocumentedFallthrough) == 0) // the case is not fall-through protected by a line comment
								this.scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
						}
						caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
						if (caseStatement.constantExpressions == NO_EXPRESSIONS) {
							if ((this.switchBits & LabeledRules) != 0 && this.expression.resolvedType instanceof ReferenceBinding) {
								if (this.expression instanceof NameReference) {
									// default case does not apply to null => mark the variable being switched over as nonnull:
									NameReference reference = (NameReference) this.expression;
									if (reference.localVariableBinding() != null) {
										caseInits.markAsDefinitelyNonNull(reference.localVariableBinding());
									} else if (reference.lastFieldBinding() != null) {
										if (this.scope.compilerOptions().enableSyntacticNullAnalysisForFields)
											switchContext.recordNullCheckedFieldReference(reference, 2); // survive this case statement and into the next
									}
								} else if (this.expression instanceof FieldReference) {
									if (this.scope.compilerOptions().enableSyntacticNullAnalysisForFields)
										switchContext.recordNullCheckedFieldReference((FieldReference) this.expression, 2); // survive this case statement and into the next
								}
							}
						}
						complaintLevel = initialComplaintLevel; // reset complaint
						fallThroughState = this.containsPatterns ? FALLTHROUGH : CASE;
					} else {
						fallThroughState = (this.switchBits & LabeledRules) != 0 || statement.doesNotCompleteNormally() ? BREAKING : FALLTHROUGH;  // reset below if needed
					}
					if ((complaintLevel = statement.complainIfUnreachable(caseInits, this.scope, complaintLevel, true)) < Statement.COMPLAINED_UNREACHABLE) {
						caseInits = statement.analyseCode(this.scope, switchContext, caseInits);
						if (compilerOptions.enableSyntacticNullAnalysisForFields)
							switchContext.expireNullCheckedFieldInfo();
						if (compilerOptions.analyseResourceLeaks)
							FakedTrackingVariable.cleanUpUnassigned(this.scope, statement, caseInits, false);
					}
				}
			}
			if (caseInits != FlowInfo.DEAD_END) {
				if (isTrulyExpression())
					currentScope.problemReporter().switchExpressionBlockCompletesNormally(this.statements[this.statements.length - 1]);
				if (this.defaultCase == null)
					this.switchBits |= BarricadeInjectedDefault;
			}

			final TypeBinding resolvedTypeBinding = this.expression.resolvedType;
			if (resolvedTypeBinding.isEnum() && !indySwitch()) {
				final SourceTypeBinding sourceTypeBinding = currentScope.classScope().referenceContext.binding;
				this.synthetic = sourceTypeBinding.addSyntheticMethodForSwitchEnum(resolvedTypeBinding, this);
			}
			// if no default case, then record it may jump over the block directly to the end
			if (this.defaultCase == null && needToCheckFlowInAbsenceOfDefaultBranch()) {
				// only retain the potential initializations
				flowInfo.addPotentialInitializationsFrom(caseInits.mergedWith(switchContext.initsOnBreak));
				this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
				return flowInfo;
			}

			FlowInfo mergedInfo = caseInits.mergedWith(switchContext.initsOnBreak); // merge all branches inits
			this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}

	abstract static sealed class SwitchTranslator {

		protected SwitchStatement swich;
		int [] constants; // case constants or proxies;
		int constantCount;
		BranchLabel[] caseLabels;

		public void setSwitch(SwitchStatement swich) {
			this.swich = swich;
		}

		protected <T extends BranchLabel> void gatherLabels(CodeStream codeStream, Function<CodeStream, T> newLabel) {
			for (int i = 0, j = 0, max = this.swich.caseCount; i < max; i++) {
				CaseStatement stmt = this.swich.cases[i];
				T label;
				stmt.targetLabel = label = newLabel.apply(codeStream);
				for (Expression e : stmt.peeledLabelExpressions()) {
					if (e instanceof FakeDefaultLiteral) continue;
					this.caseLabels[j++] = label;
				}
			}
		}

		protected void initializeLabels(CodeStream codeStream) {
			this.swich.breakLabel.initialize(codeStream);
			this.caseLabels = new CaseLabel[this.swich.nConstants];
			gatherLabels(codeStream, CaseLabel::new);
			this.swich.defaultLabel = this.swich.defaultCase != null ? (CaseLabel) this.swich.defaultCase.targetLabel :
											this.swich.unconditionalPatternCase != null ? (CaseLabel) this.swich.unconditionalPatternCase.targetLabel : new CaseLabel(codeStream);
		}

		protected void gatherCaseConstantsOrProxies(CodeStream codeStream) {
			this.constantCount = this.swich.labelExpressions.length;
			this.constants = new int [this.constantCount];
			for (int i = 0, length = this.swich.labelExpressions.length; i < length; ++i)
				this.constants[i] = this.swich.labelExpressions[i].intValue();
		}

		protected void finalizeLabels(CodeStream codeStream) {
			// nothing to do - initial labels are good enough to be final labels.
		}

		protected void generateSelectorExpression(BlockScope currentScope, CodeStream codeStream) {
			this.swich.expression.generateCode(currentScope, codeStream, true);
			if (this.swich.expression.resolvedType.id == TypeIds.T_JavaLangBoolean)
				codeStream.generateUnboxingConversion(TypeIds.T_boolean); // optimize by avoiding indy typeSwitch
		}

		protected void generateSwitchByteCode(BlockScope currentScope, CodeStream codeStream) {
			if ((this.swich.switchBits & HasNondefaultCase) != 0) { // generate the appropriate switch table/lookup bytecode
				int[] sortedIndexes = new int[this.constantCount];
				for (int i = 0; i < this.constantCount; i++)
					sortedIndexes[i] = i;
				int[] localKeysCopy;
				System.arraycopy(this.constants, 0, (localKeysCopy = new int[this.constantCount]), 0, this.constantCount);
				CodeStream.sort(localKeysCopy, 0, this.constantCount - 1, sortedIndexes);

				int max = localKeysCopy[this.constantCount - 1];
				int min = localKeysCopy[0];
				if ((long) (this.constantCount * 2.5) > ((long) max - (long) min))
					codeStream.tableswitch(this.swich.defaultLabel, min, max, this.constants, sortedIndexes, (CaseLabel[]) this.caseLabels);
				else
					codeStream.lookupswitch(this.swich.defaultLabel, this.constants, sortedIndexes, (CaseLabel[]) this.caseLabels);
				codeStream.recordPositionsFrom(codeStream.position, this.swich.expression.sourceEnd);
			} else {
				codeStream.pop();
			}
		}

		protected final void generateSwitchBlock (BlockScope currentScope, CodeStream codeStream) {
			if (this.swich.statements != null) {
				for (Statement statement : this.swich.statements) {
					if (statement instanceof CaseStatement caseStatement) {
						this.swich.scope.enclosingCase = caseStatement; // record entering in a switch case block
						if (this.swich.preSwitchInitStateIndex != -1)
							codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.swich.preSwitchInitStateIndex);
					}
					statement.generateCode(this.swich.scope, codeStream);
					if (statement instanceof Block block && (block.bits & BlockShouldEndDead) != 0)
						codeStream.goto_(this.swich.breakLabel);
				}
			}
		}

		protected final void generateDefaultCase(BlockScope currentScope, CodeStream codeStream) {
			if (this.swich.defaultCase == null && this.swich.unconditionalPatternCase == null) {
				boolean needsThrowingDefault = this.swich.expression.resolvedType.isEnum() && (this.swich instanceof SwitchExpression || this.swich.containsNull);
				needsThrowingDefault |= this.swich.isExhaustive(); // pattern switches:
				if (needsThrowingDefault) {
					if (this.swich.preSwitchInitStateIndex != -1)
						codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.swich.preSwitchInitStateIndex);
					if (this.swich.scope.compilerOptions().complianceLevel >= ClassFileConstants.JDK19) { // since 19 we have MatchException for this
						if ((this.swich.switchBits & BarricadeInjectedDefault) != 0)
							codeStream.goto_(this.swich.breakLabel); // hop, skip and jump over match exception throw.
						this.swich.defaultLabel.place();
						codeStream.newJavaLangMatchException();
						codeStream.dup();
						codeStream.aconst_null();
						codeStream.aconst_null();
						codeStream.invokeJavaLangMatchExceptionConstructor();
						codeStream.athrow();
					} else { // old style using IncompatibleClassChangeError:
						this.swich.defaultLabel.place();
						codeStream.newJavaLangIncompatibleClassChangeError();
						codeStream.dup();
						codeStream.invokeJavaLangIncompatibleClassChangeErrorDefaultConstructor();
						codeStream.athrow();
					}
				}
			}
		}

		protected final void generateEpilogue(BlockScope currentScope, CodeStream codeStream, int pc) {
			this.swich.breakLabel.place();
			if (this.swich.defaultLabel.position == Label.POS_NOT_SET) {
				codeStream.recordPositionsFrom(codeStream.position, this.swich.sourceEnd, true); // force a line number entry to get an end position after the switch
				this.swich.defaultLabel.place();
			}
			// May loose some local variable initializations : affecting the local variable attributes
			if (this.swich.mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.swich.mergedInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, this.swich.mergedInitStateIndex);
			}
			codeStream.removeVariable(this.swich.selector);
			if (this.swich.scope != currentScope)
				codeStream.exitUserScope(this.swich.scope);
			codeStream.recordPositionsFrom(pc, this.swich.sourceStart);
		}

		public final void generateCode(BlockScope currentScope, CodeStream codeStream) { // common main code generator driver for all switches - hence final

			if ((this.swich.bits & IsReachable) == 0)
				return;

			try {
				// Prepare labels & case constants or their proxies.
				int pc = codeStream.position;
				initializeLabels(codeStream); // assemble case labels, break label and default label
				gatherCaseConstantsOrProxies(codeStream);
				finalizeLabels(codeStream); // actual cases may differ from what is seen in source code - e.g., String switch.
				generateSelectorExpression(currentScope, codeStream);
				generateSwitchByteCode(currentScope, codeStream);
				generateSwitchBlock(currentScope, codeStream); // And also case label placement
				generateDefaultCase(currentScope, codeStream); // inject a MatchException throw for binary incompatibility signaling
				generateEpilogue(currentScope, codeStream, pc); // Epilogue: Place the trailing labels (for break and default case)
			} finally {
				if (this.swich.scope != null)
					this.swich.scope.enclosingCase = null; // no longer inside switch case block
			}
		}

		final static class ClassicSwitchTranslator extends SwitchTranslator {
			// default behavior is fine enough.
		}

		final static class StringSwitchTranslator extends SwitchTranslator {

			private record StringCaseConstant(int hashKode, String string, BranchLabel label) implements Comparable<StringCaseConstant> {
				@Override
				public int compareTo(StringCaseConstant that) {
					return this.hashKode == that.hashKode ? 0 : this.hashKode > that.hashKode ? 1 : -1; // can't use just '-' due to potential overflow/underflow
				}
			}

			private StringCaseConstant [] stringCaseConstants;

			@Override
			protected void initializeLabels(CodeStream codeStream) {
				// prepare the labels and constants
				this.swich.breakLabel.initialize(codeStream);
				this.caseLabels = new BranchLabel[this.swich.nConstants];
				gatherLabels(codeStream, BranchLabel::new);
				this.swich.defaultLabel = new CaseLabel(codeStream, true /* allow narrow branch to */);
				if (this.swich.defaultCase != null)
					this.swich.defaultCase.targetLabel = this.swich.defaultLabel; // Replace the vanilla branch label with a case label that doubles as a branch label.
			}

			@Override
			protected void gatherCaseConstantsOrProxies(CodeStream codeStream) { // proxies in this case - unique hash code values.
				this.stringCaseConstants = new StringCaseConstant[this.swich.nConstants];

				int [] hashCodes = new int[this.swich.nConstants];
				for (int i = 0; i < this.swich.nConstants; i++) {
					String literal = this.swich.labelExpressions[i].constant.stringValue();
					this.stringCaseConstants[i] = new StringCaseConstant(literal.hashCode(), literal, this.caseLabels[i]);
				}
				Arrays.sort(this.stringCaseConstants);

				int uniqHashCount = 0, lastHashCode = 0;
				for (int i = 0; i < this.swich.nConstants; ++i) {
					int hashCode = this.stringCaseConstants[i].hashKode;
					if (i == 0 || hashCode != lastHashCode)
						lastHashCode = hashCodes[uniqHashCount++] = hashCode;
				}
				if (uniqHashCount != this.swich.nConstants) // multiple keys hashed to the same value.
					System.arraycopy(hashCodes, 0, hashCodes = new int[uniqHashCount], 0, uniqHashCount);
				this.constants = hashCodes;
				this.constantCount = uniqHashCount;
			}

			@Override
			protected void finalizeLabels(CodeStream codeStream) { // case labels in String switch don't correspond to source code cases ...
				this.caseLabels = new CaseLabel[this.constantCount];
				for (int i = 0; i < this.constantCount; i++)
					this.caseLabels[i] = new CaseLabel(codeStream);
			}

			@Override
			protected void generateSelectorExpression(BlockScope currentScope, CodeStream codeStream) {
				this.swich.expression.generateCode(currentScope, codeStream, true);
				codeStream.store(this.swich.selector, true);  // leaves string on operand stack
				codeStream.addVariable(this.swich.selector);
				codeStream.invokeStringHashCode();
			}

			@Override
			protected void generateSwitchByteCode(BlockScope currentScope, CodeStream codeStream) {
				int[] sortedIndexes = new int[this.constantCount]; // hash code are sorted already anyways.
				for (int i = 0; i < this.constantCount; i++)
					sortedIndexes[i] = i;
				int lastHashCode = 0;
				codeStream.lookupswitch(this.swich.defaultLabel, this.constants, sortedIndexes, (CaseLabel[]) this.caseLabels);
				for (int i = 0, j = 0; i < this.swich.nConstants; i++) {
					int hashCode = this.stringCaseConstants[i].hashKode;
					if (i == 0 || hashCode != lastHashCode) {
						lastHashCode = hashCode;
						if (i != 0)
							codeStream.goto_(this.swich.defaultLabel);
						this.caseLabels[j++].place();
					}
					codeStream.load(this.swich.selector);
					codeStream.ldc(this.stringCaseConstants[i].string);
					codeStream.invokeStringEquals();
					codeStream.ifne(this.stringCaseConstants[i].label);
				}
				codeStream.goto_(this.swich.defaultLabel);
			}
		}

		final static class ClassicEnumSwitchTranslator extends SwitchTranslator { // 1.5 enum switch, PatternSwitchTranslator covers enums with patterns

			@Override
			protected void generateSelectorExpression(BlockScope currentScope, CodeStream codeStream) {
				// go through the translation table in order to guarantee binary compatibility promises of "13.4.26 Evolution of Enum Classes"
				codeStream.invoke(Opcodes.OPC_invokestatic, this.swich.synthetic, null /* default declaringClass */);
				this.swich.expression.generateCode(currentScope, codeStream, true);
				codeStream.invokeEnumOrdinal(this.swich.expression.resolvedType.constantPoolName());
				codeStream.iaload();
			}
		}

		final static class PatternSwitchTranslator extends SwitchTranslator {

			@Override
			protected void gatherCaseConstantsOrProxies(CodeStream codeStream) {
				this.constantCount = this.swich.labelExpressions.length;
				this.constants = new int [this.constantCount];
				for (int i = 0, j = 0, length = this.swich.labelExpressions.length; i < length; ++i) {
					final LabelExpression labelExpression = this.swich.labelExpressions[i];
					this.constants[i] = labelExpression.index - j;
					if (labelExpression.type.isPrimitiveType()) {
						SingletonBootstrap descriptor = labelExpression.isPattern() ? PRIMITIVE_CLASS__BOOTSTRAP : labelExpression.type.id == TypeIds.T_boolean ? GET_STATIC_FINAL__BOOTSTRAP : null;
						if (descriptor != null)
							labelExpression.primitivesBootstrapIdx = codeStream.classFile.recordSingletonBootstrapMethod(descriptor);
					} else if (labelExpression.isQualifiedEnum()) {
						labelExpression.enumDescIdx = codeStream.classFile.recordBootstrapMethod(labelExpression);
						labelExpression.classDescIdx = codeStream.classFile.recordBootstrapMethod(labelExpression.type);
					} else if (labelExpression.expression instanceof NullLiteral) {
						j = 1;  // since we yank null out to -1, shift down everything beyond.
					}
				}
			}

			private char[] typeSwitchSignature(TypeBinding exprType) {
				char[] arg1 = switch (exprType.id) {
					case TypeIds.T_JavaLangLong, TypeIds.T_JavaLangFloat, TypeIds.T_JavaLangDouble, TypeIds.T_JavaLangBoolean,
						TypeIds.T_JavaLangByte, TypeIds.T_JavaLangShort, TypeIds.T_JavaLangInteger, TypeIds.T_JavaLangCharacter->
						this.swich.isPrimitiveSwitch
						? exprType.signature()
						: "Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
					default -> {
						if (exprType.id > TypeIds.T_LastWellKnownTypeId && exprType.erasure().isBoxedPrimitiveType())
							yield exprType.erasure().signature(); // <T extends Integer> / <? extends Short> ...
						else
							yield exprType.isPrimitiveType() || exprType.isEnum()
								? exprType.signature()
								: "Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
					}
				};
				return CharOperation.concat("(".toCharArray(), arg1, "I)I".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			@Override
			protected void generateSelectorExpression(BlockScope currentScope, CodeStream codeStream) {
				this.swich.expression.generateCode(currentScope, codeStream, true);
				if (!this.swich.containsNull && !this.swich.expression.resolvedType.isPrimitiveType()) {
					codeStream.dup();
					codeStream.invokeJavaUtilObjectsrequireNonNull();
					codeStream.pop();
				}

				codeStream.store(this.swich.selector, false);
				codeStream.addVariable(this.swich.selector);

				int invokeDynamicNumber = codeStream.classFile.recordBootstrapMethod(this.swich);

				codeStream.load(this.swich.selector);
				codeStream.loadInt(0); // restartIndex
				this.swich.switchPatternRestartTarget = new BranchLabel(codeStream);
				this.swich.switchPatternRestartTarget.place();

				TypeBinding selectorType = this.swich.expression.resolvedType;
				char[] signature = typeSwitchSignature(selectorType);
				int argsSize = TypeIds.getCategory(selectorType.id) + 1; // ReferenceType | PRIM, restartIndex (PRIM = Z|S|I..)
				char [] bootstrap = selectorType.isEnum() ? ConstantPool.ENUMSWITCH : ConstantPool.TYPESWITCH;
				codeStream.invokeDynamic(invokeDynamicNumber, argsSize, 1 /* int case constant/proxy */, bootstrap, signature, TypeBinding.INT);
			}
		}
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		SwitchTranslator translator = indySwitch() ? new SwitchTranslator.PatternSwitchTranslator() :
			this.expression.resolvedType.id == TypeIds.T_JavaLangString && !this.isNonTraditional ? new SwitchTranslator.StringSwitchTranslator() :
					this.expression.resolvedType.isEnum() ? new SwitchTranslator.ClassicEnumSwitchTranslator() : new SwitchTranslator.ClassicSwitchTranslator();
		translator.setSwitch(this);
		translator.generateCode(currentScope, codeStream);
	}

	@Override
	public boolean isTrulyExpression() {
		return false;
	}

	@Override
	public boolean doesNotCompleteNormally() {
		if (this.statements == null || this.statements.length == 0)
			return false;
		if (!isExhaustive() && this.defaultCase == null) // selector not covered by cases - will escape.
			return false;
		for (Statement statement : this.statements) {
			if (statement.breaksOut(null))
				return false;
		}
		return this.statements[this.statements.length - 1].doesNotCompleteNormally();
	}

	@Override
	public boolean completesByContinue() {
		if (this.statements == null || this.statements.length == 0)
			return false;
		for (Statement statement : this.statements) {
			if (statement.completesByContinue())
				return true;
		}
		return false;
	}

	@Override
	public StringBuilder printStatement(int indent, StringBuilder output) {
		printIndent(indent, output).append("switch ("); //$NON-NLS-1$
		this.expression.printExpression(0, output).append(") {"); //$NON-NLS-1$
		if (this.statements != null) {
			for (Statement statement : this.statements) {
				output.append('\n');
				if (statement instanceof CaseStatement)
					statement.printStatement(indent, output);
				else
					statement.printStatement(indent+2, output);
			}
		}
		output.append("\n"); //$NON-NLS-1$
		return printIndent(indent, output).append('}');
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		return printStatement(indent, output);
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope)) {
			this.expression.traverse(visitor, blockScope);
			if (this.statements != null)
				for (Statement statement : this.statements)
					statement.traverse(visitor, this.scope);
		}
		visitor.endVisit(this, blockScope);
	}
}