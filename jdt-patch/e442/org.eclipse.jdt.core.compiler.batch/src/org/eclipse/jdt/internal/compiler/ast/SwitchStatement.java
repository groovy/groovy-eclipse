/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
import java.util.function.Predicate;
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
	/* package */ boolean caseElementsHaveErrors = false; // a case pattern failed applicability/cast checks
	/* package */ List<Pattern> caseLabelElements = new ArrayList<>(0);//TODO: can we remove this?
	public List<TypeBinding> caseLabelElementTypes = new ArrayList<>(0);
	private List<TNode> tNodeList = new ArrayList<>(0);

	/*
	 * The RNode / TNode / PatternNode tree together implement the
	 * "P rewrites to a set Q and Q covers T" clause of JLS 14.11.1.1 for record selector types.
	 *
	 * Recap of the spec machinery this models:
	 *   - P rewrites to Q: a subset of P "reduces" to a single pattern p, and Q is the remaining
	 *     elements of P together with p. Coverage is then decided on Q instead of P.
	 *   - A non-empty set of record patterns RP "reduces" to a single record pattern rp if their
	 *     types all erase to the same record class R (with k>=1 components) and there is one
	 *     distinguished component cr that itself "reduces" to a single pattern q, while every other
	 *     component ci is "equivalent" to a single pattern qi. rp is then R(q1,..,q,..,qk).
	 *   - A non-empty set EP is "equivalent" to a single pattern ep when the patterns share the same
	 *     erasure (type patterns) or erase to the same record R and each component set is itself
	 *     equivalent to a single pattern (record patterns).
	 *
	 * How the tree mirrors that definition:
	 *   - RNode(R)        : the record class R under test; its firstComponent starts the component walk.
	 *   - TNode(type)     : one record component (a "column"). Its children are the distinct alternatives
	 *                       seen for that component across all case record patterns. Grouping the
	 *                       incoming component patterns by erasure into a single child is exactly the
	 *                       "equivalent to a single pattern" merge (type patterns / record patterns of
	 *                       the same erased type collapse into one alternative).
	 *   - PatternNode     : one such alternative for a component. Its 'next' TNode is the following
	 *                       component of the same record pattern, so a full record pattern is threaded
	 *                       column-by-column; 'nested' captures a nested record pattern that must have
	 *                       its own components covered; 'unconditional' marks a total (type) pattern that
	 *                       covers the whole slot.
	 */
	abstract class Node {
		static List<PatternNode> emptyPatternNodeList = new ArrayList<>(0);
		TypeBinding type;
		boolean hasError = false;

		public abstract StringBuilder printNode(int indent, StringBuilder output);
		public abstract void traverse(NodeVisitor visitor);

	}

	class NodeVisitor {
		protected Predicate<Node> nextPredicate = node -> true;

		public void beginVisit(TNode node) { /* do nothing */ }
		public void beginVisit(RNode node) { /* do nothing */ }
		public void beginVisit(PatternNode node) { /* do nothing */ }

		public boolean visit(TNode node) { return true; }
		public boolean visit(RNode node) { return true; }
		public boolean visit(PatternNode node) { return true; }

		public void endVisit(TNode node) { /* do nothing */ }
		public void endVisit(RNode node) { /* do nothing */ }
		public void endVisit(PatternNode node) { /* do nothing */ }

	}

	class RNode extends Node {

		TNode firstComponent;

		RNode(TypeBinding rec) {
			this.type = rec;
			RecordComponentBinding[] comps = rec.components();
			int len = comps != null ? comps.length : 0;
			if (len > 0) {
				RecordComponentBinding comp = comps[0];
				if (comp != null && comp.type != null) {
					this.firstComponent = new TNode(comp.type, rec);
					SwitchStatement.this.tNodeList.add(this.firstComponent);
				}
			}
		}

		List<PatternNode> addPattern(Pattern p) {
			if (p instanceof RecordPattern rp && rp.type.resolvedType != null
					&& TypeBinding.equalsEquals(this.type.erasure(), rp.type.resolvedType.erasure())
					&& this.firstComponent != null)
				return this.firstComponent.addPattern(rp, 0);
			return Node.emptyPatternNodeList;
		}

		@Override
		public String toString() {
			return this.printNode(0, new StringBuilder()).toString();
		}

		@Override
		public void traverse(NodeVisitor visitor) {
			visitor.beginVisit(this);

			if (this.firstComponent != null) {
				this.firstComponent.traverse(visitor);
			}

			visitor.endVisit(this);
		}

		@Override
		public StringBuilder printNode(int indent, StringBuilder output) {
			printIndent(indent, output).append("{ RNode:").append(new String(this.type.shortReadableName())).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.firstComponent != null) {
				printIndent(indent+2, output).append("firstComponent:\n"); //$NON-NLS-1$
				this.firstComponent.printNode(indent+2, output).append("\n"); //$NON-NLS-1$
			}
			return printIndent(indent, output).append(" }\n"); //$NON-NLS-1$
		}
	}

	class TNode extends Node {

		List<PatternNode> children;
		/** The record type whose component this column represents; used to resolve the next component type. */
		TypeBinding enclosingRecord;
		List<TypeBinding> remainingTypes = null;
		boolean unnamed = false;

		TNode(TypeBinding type, TypeBinding enclosingRecord){
			this.type = type;
			this.enclosingRecord = enclosingRecord;
			this.children = new ArrayList<>();
		}

		public List<PatternNode> addPattern(RecordPattern rp, int i) {
			List<PatternNode> relevantPatternNodes = new ArrayList<>();

			if (rp.patterns.length <= i) {
				this.hasError = true;
				return Node.emptyPatternNodeList;
			}
			Pattern componentPattern = rp.patterns[i];
			TypeBinding childType = componentPattern.resolvedType;

			if (childType == null) {
				this.hasError = true;
				return Node.emptyPatternNodeList;
			}
			PatternNode child = null;
			for (PatternNode c : this.children) {
				// Group by erasure per JLS 14.11.1.1 ("... covers a type U where T and U have the
				// same erasure"), consistent with RNode.addPattern and caseElementsCoverSealedType.
				if (TypeBinding.equalsEquals(childType.erasure(), c.type.erasure())) {
					child = c;
					break;
				}
			}
			if (child == null) {
				boolean expandPermittedTypes = childType instanceof ReferenceBinding ref && ref.isSealed() && (ref.isAbstract() || ref.isInterface());
		 		if (expandPermittedTypes) {
					TypeBinding[] permittedTypes = childType.permittedTypes();
					for (TypeBinding permitted : permittedTypes) {
						child = null;
						if (permitted != null && permitted.isValidBinding()) {
							for (PatternNode c : this.children) {
								// Group by erasure per JLS 14.11.1.1 ("... covers a type U where T and U have the
								// same erasure"), consistent with RNode.addPattern and caseElementsCoverSealedType.
								if (TypeBinding.equalsEquals(permitted.erasure(), c.type.erasure())) {
									child = c;
									break;
								}
							}
							if (child == null) {
								child = new PatternNode(permitted, this.enclosingRecord);
								if (this.type.isSubtypeOf(permitted, false))
									this.children.add(0, child);
								else
									this.children.add(child);
							}
							relevantPatternNodes.add(child);
						}
					}
				} else {
					child = new PatternNode(childType, this.enclosingRecord);
					if (this.type.isSubtypeOf(childType, false))
						this.children.add(0, child);
					else
						this.children.add(child);
					relevantPatternNodes.add(child);
				}
			} else {
				relevantPatternNodes.add(child);
			}

			// Record how this component slot is matched: a nested record pattern must have its own
			// components covered, whereas a (type) pattern covers the whole slot unconditionally.
			if (componentPattern instanceof RecordPattern nestedRp && nestedRp.resolvedType.isRecordWithComponents()) {
				if (child.nested == null)
					child.nested = new RNode(childType);
				List<PatternNode> nestedRelevantPatterns = child.nested.addPattern(nestedRp);

				for (int j = relevantPatternNodes.size() - 1; j >= 0; j--) {
					if (relevantPatternNodes.get(j) == child) {
						relevantPatternNodes.remove(j);
						break;
					}
				}
				relevantPatternNodes.addAll(nestedRelevantPatterns);
			} else {
				child.unconditional = true;
			}

			List<PatternNode> result = new ArrayList<>(0);
			if ((i+1) < rp.patterns.length) {
				for (PatternNode newChild : relevantPatternNodes) {
					List<PatternNode> pNodes = newChild.addPattern(rp, i + 1);
					result.addAll(pNodes);
				}
			} else {
				return relevantPatternNodes;
			}
			return result;
		}

		@Override
		public String toString() {
			return this.printNode(0, new StringBuilder()).toString();
		}

		@Override
		public void traverse(NodeVisitor visitor) {

			visitor.beginVisit(this);

			if (visitor.visit(this)) {
				if (this.children != null) {
					for (PatternNode child : this.children) {
						child.traverse(visitor);
					}
				}
			}
			visitor.endVisit(this);
		}

		@Override
		public StringBuilder printNode(int indent, StringBuilder output) {
			printIndent(indent, output).append("{ TNode:").append(new String(this.type.readableName())).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			printIndent(indent+2, output).append("unnamed:").append(this.unnamed).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.children != null) {
				printIndent(indent+2, output).append("children:\n"); //$NON-NLS-1$
				int i = 0;
				for (PatternNode child : this.children) {
					printIndent(indent+2, output).append("child[").append(i++).append("]:\n"); //$NON-NLS-1$ //$NON-NLS-2$
					child.printNode(indent+2, output).append("\n"); //$NON-NLS-1$
				}
			}
			return printIndent(indent, output).append(" }\n"); //$NON-NLS-1$
		}
	}

	class PatternNode extends Node {

		TNode next; // next component
		/** Nested record coverage when this component is matched by a record pattern (e.g. Pair(..) inside Box(..)). */
		RNode nested;
		/** True when this component slot is matched by a (total) type pattern, covering the whole slot type. */
		boolean unconditional;
		/** The record type whose component this node represents; used to resolve the next component type. */
		TypeBinding enclosingRecord;

		PatternNode(TypeBinding type, TypeBinding enclosingRecord) {
			this.type = type;
			this.enclosingRecord = enclosingRecord;
		}

		public List<PatternNode> addPattern(RecordPattern rp, int i) {
			RecordComponentBinding[] comps = rp.resolvedType.components();
			if (comps == null || comps.length <= i) // safety-net for incorrect code.
				return Node.emptyPatternNodeList;

			if (comps[i].type == null) {
				this.hasError = true;
				return Node.emptyPatternNodeList;
			}

			// Thread this record pattern into the next component column (component i+... of the same R),
			if (this.next == null) {
				this.next = new TNode(comps[i].type, this.enclosingRecord);
				SwitchStatement.this.tNodeList.add(this.next);
			}
			if (rp.patterns[i].isUnnamed()) {
				if (rp.patterns[i].resolvedType instanceof ReferenceBinding rec && rec.isRecordWithComponents()) {
					// unnamed pattern in a record component means the whole column is considered unnamed
					NodeVisitor unnamedSetterVisitor = new NodeVisitor() {
						@Override
						public boolean visit(TNode node) {
							if (node.unnamed)
								return false; // already visited - no need to go down further.
							node.unnamed = true;
							return true;
						}
					};
					this.next.traverse(unnamedSetterVisitor);
				}
			}
			return this.next.addPattern(rp, i);
		}

		@Override
		public String toString() {
			return this.printNode(0, new StringBuilder()).toString();
		}

		@Override
		public void traverse(NodeVisitor visitor) {

			visitor.beginVisit(this);

			if (visitor.visit(this)) {
				if (this.nested != null) {
					this.nested.traverse(visitor);
				}

				if (this.next != null && visitor.nextPredicate.test(this.next)) {
					this.next.traverse(visitor);
				}
			}

			visitor.endVisit(this);
		}

		@Override
		public StringBuilder printNode(int indent, StringBuilder output) {
			printIndent(indent, output).append("{ PatternNode:").append(new String(this.type.shortReadableName())).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			printIndent(indent+2, output).append("unconditional:").append(this.unconditional).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.nested != null) {
				printIndent(indent+2, output).append("nested:\n"); //$NON-NLS-1$
				this.nested.printNode(indent+2, output).append("\n"); //$NON-NLS-1$
			}
			if (this.next != null) {
				printIndent(indent+2, output).append("next:=>\n"); //$NON-NLS-1$
				this.next.printNode(indent+2, output).append("\n"); //$NON-NLS-1$
			}
			return printIndent(indent, output).append(" }\n"); //$NON-NLS-1$
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
		if (current.constant.compareAfterPromoting(prior.constant))
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
					if (this.labelExpressions[i].expression instanceof Pattern priorPattern && priorPattern.dominates(pattern, this.scope)) {
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
				boolean dominatedByUnconditional = false;
				if (JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(this.scope.compilerOptions())
						&& this.unconditionalPatternCase != null) {
					Constant cst = labelExpression.expression.constant;
					if (cst != null && cst != Constant.NotAConstant) {
						this.scope.problemReporter().patternDominatedByAnother(labelExpression.expression);
						dominatedByUnconditional = true;
					}
				}
				if (!dominatedByUnconditional) {
					TypeBinding boxedType = labelExpression.type.isBaseType() ? this.scope.environment().computeBoxingType(labelExpression.type) : labelExpression.type;
					for (int i = 0; i < this.labelExpressionIndex; i++) {
						if (this.labelExpressions[i].expression instanceof Pattern priorPattern) {
							if (priorPattern.coversType(boxedType, this.scope)) {
								this.scope.problemReporter().patternDominatedByAnother(labelExpression.expression);
								break;
							}
							Constant cst = labelExpression.expression.constant;
							if (cst != null && cst != Constant.NotAConstant && priorPattern.coversValue(cst, this.scope)) {
								this.scope.problemReporter().patternDominatedByAnother(labelExpression.expression);
								break;
							}
						}
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

		// When a case pattern failed applicability/cast checks, the switch is already in error; don't
		// pile on a redundant missing-default/exhaustiveness diagnostic (matches javac's behavior).
		if (this.caseElementsHaveErrors) {
			this.switchBits |= SwitchStatement.Exhaustive;
			return;
		}

		boolean isEnhanced = isEnhancedSwitch(upperScope, selectorType);
		// JLS 14.11.1.1: T is an enum class E and P covers T when P contains all names of E's enum
		// constants (a default label is then permitted but not required).
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

		if (JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions) && selectorType.isSealed()
				&& caseElementsCoverSealedType((ReferenceBinding) selectorType, this.caseLabelElementTypes, true))
			this.switchBits |= SwitchStatement.Exhaustive; // JLS 14.11.1.1: T names an abstract sealed class/interface and every permitted subtype is covered
		else if (selectorType.isRecordWithComponents() && this.containsRecordPatterns && caseElementsCoverRecordType(upperScope, compilerOptions, (ReferenceBinding) selectorType))
			this.switchBits |= SwitchStatement.Exhaustive; // JLS 14.11.1.1: T names a record R and a record pattern covers every component

		if (!isExhaustive()) {
			if (isEnhanced)
				upperScope.problemReporter().enhancedSwitchMissingDefaultCase(this.expression);
			else
				upperScope.problemReporter().missingDefaultCase(this, false, selectorType);
		}
	}

	// Return the set of enumerations belonging to the selector enum type that are NOT listed in case statements.
	// JLS 14.11.1.1: P covers enum type E only when this set is empty (all constant names of E appear).
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
		// JLS 14.11.1.1: T names a record R and P contains a record pattern naming R whose component
		// patterns cover each record component. Model the components as an RNode/TNode tree and check coverage.
		RNode head = new RNode(recordType);
		for (Pattern pattern : this.caseLabelElements) {
			head.addPattern(pattern);
		}
		return checkRecordCoverage(SwitchStatement.this.tNodeList);
	}

	public boolean checkRecordCoverage(List<TNode> tNodeList2) {
		if (tNodeList2.isEmpty())
			return true;
		boolean allCovered = true;
		for (TNode tNode : tNodeList2) {

			allCovered &= checkRecordCoverage(tNode);
//			if (!allCovered) // we need to check all TNodes to report all uncovered types, not just the first one
//				break;
		}
		return allCovered;
	}
	public boolean checkRecordCoverage(TNode node) {
		if (node.hasError)
			return false;

		if (node.unnamed) {
			return true; // unnamed pattern covers the whole slot type
		}

		List<TypeBinding> availableTypes = new ArrayList<>();
		if (node.children != null) {
			for (PatternNode child : node.children) {
				availableTypes.add(child.type);
			}
		}
		List<TypeBinding> allTypes = new ArrayList<>();
		if (node.type instanceof ReferenceBinding ref && ref.isSealed()) {
				allTypes.addAll(ref.getAllEnumerableAvatars());
		} else {
			allTypes.add(node.type);
		}
		if (node.remainingTypes == null) {
			node.remainingTypes = new ArrayList<>();
			for (TypeBinding type : allTypes) {
				boolean found = availableTypes.stream().filter(findCompatibleType(type)).findAny().isPresent();
				if (!found) {
					if (type instanceof ReferenceBinding ref && ref.isSealed()) {
						for (ReferenceBinding permitted : ref.getAllEnumerableAvatars()) { // check all permitted types for coverage
							found = availableTypes.stream().filter(findCompatibleType(permitted)).findAny().isPresent();
							if (!found)
								break;
						}
					}
					if (!found)
						node.remainingTypes.add(type);
				}
			}
		}
		return node.remainingTypes.isEmpty();
	}

	private Predicate<? super TypeBinding> findCompatibleType(TypeBinding type) {
		return candidate -> type.erasure().isCompatibleWith(candidate.erasure());
	}


	/**
	 * @param checkRecordPatterns when true (top-level sealed selector), a permitted record
	 *        is covered only if case patterns fully cover it — including nested components.
	 */
	private boolean caseElementsCoverSealedType(ReferenceBinding sealedType, List<TypeBinding> listedTypes,
			boolean checkRecordPatterns) {
		// JLS 14.11.1.1: T names an abstract sealed class/interface C and, for every permitted direct
		// subclass/subinterface D, either no subtype of T names D, or a type U naming D (subtype of T) is covered.
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
 			if (checkRecordPatterns && this.containsRecordPatterns && next.isRecord()) {
				if (isRecordTypeFullyCoveredByCasePatterns(next))
					iterator.remove();
				continue;
			}
			for (TypeBinding type : listedTypes) {
				if (next.erasure().isCompatibleWith(type.erasure())) {
					iterator.remove();
					break;
				}
			}
		}
		return allAllowedTypes.size() == 0;
	}

	/** Type pattern covers the record, or record patterns collectively cover nested components. */
	private boolean isRecordTypeFullyCoveredByCasePatterns(ReferenceBinding recordType) {
		ReferenceBinding patternRecordType = null;
		for (Pattern pattern : this.caseLabelElements) {
			if (pattern instanceof RecordPattern rp) {
				if (rp.resolvedType instanceof ReferenceBinding ref
						&& TypeBinding.equalsEquals(recordType.erasure(), ref.erasure())) {
					patternRecordType = ref; // use pattern's parameterization for RNode matching
				}
			} else if (pattern.resolvedType != null
					&& recordType.erasure().isCompatibleWith(pattern.resolvedType.erasure())
					&& pattern.coversType(pattern.resolvedType, this.scope)) {
				return true;
			}
		}
		if (patternRecordType == null)
			return false;
		return caseElementsCoverRecordType(this.scope, null, patternRecordType);
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
			case TypeIds.T_boolean, TypeIds.T_long, TypeIds.T_double, TypeIds.T_float :
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
						if (this.expression.isConstantValueOfTypeAssignableToType(expressionType, TypeBinding.INT))
							break checkType;
						if (expressionType.isCompatibleWith(TypeBinding.INT))
							break checkType;
						if (JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(compilerOptions)) {
							upperScope.problemReporter().previewFeatureUsed(this.expression.sourceStart, this.expression.sourceEnd);
							this.isPrimitiveSwitch = true;
						}
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
											switchContext.recordNullCheckedFieldReference(reference, 2, FlowInfo.NON_NULL); // survive this case statement and into the next
									}
								} else if (this.expression instanceof FieldReference) {
									if (this.scope.compilerOptions().enableSyntacticNullAnalysisForFields)
										switchContext.recordNullCheckedFieldReference((FieldReference) this.expression, 2, FlowInfo.NON_NULL); // survive this case statement and into the next
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
				this.swich.defaultLabel = new CaseLabel(codeStream, true /* reachable also via goto[_w] */);
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
							codeStream.goto_(this.swich.defaultLabel.branchLabel);
						this.caseLabels[j++].place();
					}
					codeStream.load(this.swich.selector);
					codeStream.ldc(this.stringCaseConstants[i].string);
					codeStream.invokeStringEquals();
					codeStream.ifne(this.stringCaseConstants[i].label);
				}
				codeStream.goto_(this.swich.defaultLabel.branchLabel);
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