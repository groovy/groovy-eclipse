/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
import java.util.function.IntPredicate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement.ResolvedCase;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CaseLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
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

@SuppressWarnings("rawtypes")
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
	public BranchLabel breakLabel;
	public CaseStatement[] cases;
	public CaseStatement defaultCase;
	public CaseStatement nullCase; // convenience pointer for pattern switches
	public int blockStart;
	public int caseCount;
	int[] constants;
	int[] constMapping;
	// Any non int constants
	public ResolvedCase[] otherConstants;
	public int nConstants;
	public int switchBits;

	public boolean containsPatterns;
	public boolean containsRecordPatterns;
	public boolean containsNull;
	boolean nullProcessed = false;
	BranchLabel switchPatternRestartTarget;
	/* package */ public Pattern totalPattern;

	// fallthrough
	public final static int CASE = 0;
	public final static int FALLTHROUGH = 1;
	public final static int ESCAPING = 2;
	public final static int BREAKING  = 3;

	// Other bits
	public final static int LabeledRules = ASTNode.Bit1;
	public final static int NullCase = ASTNode.Bit2;
	public final static int TotalPattern = ASTNode.Bit3;
	public final static int Exhaustive = ASTNode.Bit4;
	public final static int QualifiedEnum = ASTNode.Bit5;
	public final static int LabeledBlockStatementGroup = ASTNode.Bit6;

	// for switch on strings
	private static final char[] SecretStringVariableName = " switchDispatchString".toCharArray(); //$NON-NLS-1$

	// for patterns in switch
	/* package */ static final char[] SecretPatternVariableName = " switchDispatchPattern".toCharArray(); //$NON-NLS-1$
	private static final char[] SecretPatternRestartIndexName = " switchPatternRestartIndex".toCharArray(); //$NON-NLS-1$

	public SyntheticMethodBinding synthetic; // use for switch on enums types

	// for local variables table attributes
	int preSwitchInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	Statement[] duplicateCases = null;
	int duplicateCaseCounter = 0;
	private LocalVariableBinding dispatchStringCopy = null;
	LocalVariableBinding dispatchPatternCopy = null;
	LocalVariableBinding restartIndexLocal = null;

	/* package */ boolean isNonTraditional = false;
	/* package */ boolean isPrimitiveSwitch = false;
	/* package */ List<Pattern> caseLabelElements = new ArrayList<>(0);//TODO: can we remove this?
	public List<TypeBinding> caseLabelElementTypes = new ArrayList<>(0);
	int constantIndex = 0;

	class Node {
		TypeBinding type;
		boolean hasError = false;
		public void traverse(NodeVisitor visitor) {
			visitor.visit(this);
			visitor.endVisit(this);
		}
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
			if (p instanceof RecordPattern)
				addPattern((RecordPattern)p);
		}
		void addPattern(RecordPattern rp) {
			if (!TypeBinding.equalsEquals(this.type, rp.type.resolvedType))
				return;
			if (this.firstComponent == null)
				return;
			this.firstComponent.addPattern(rp, 0);
		}
		@Override
		public String toString() {
	        StringBuilder sb = new StringBuilder();
	        sb.append("[RNode] {\n"); //$NON-NLS-1$
	        sb.append("    type:"); //$NON-NLS-1$
	        sb.append(this.type != null ? this.type.toString() : "null"); //$NON-NLS-1$
	        sb.append("    firstComponent:"); //$NON-NLS-1$
	        sb.append(this.firstComponent != null ? this.firstComponent.toString() : "null"); //$NON-NLS-1$
	        sb.append("\n}\n"); //$NON-NLS-1$
	        return sb.toString();
		}
		@Override
		public void traverse(NodeVisitor visitor) {
			if (this.firstComponent != null) {
				visitor.visit(this.firstComponent);
			}
			visitor.endVisit(this);
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
				child = childType.isRecord() ?
					new RecordPatternNode(childType) : new PatternNode(childType);
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
	        StringBuilder sb = new StringBuilder();
	        sb.append("[TNode] {\n"); //$NON-NLS-1$
	        sb.append("    type:"); //$NON-NLS-1$
	        sb.append(this.type != null ? this.type.toString() : "null"); //$NON-NLS-1$
	        sb.append("    children:"); //$NON-NLS-1$
	        if (this.children == null) {
	        	sb.append("null"); //$NON-NLS-1$
	        } else {
	        	for (Node child : this.children) {
	        		sb.append(child.toString());
	        	}
	        }
	        sb.append("\n}\n"); //$NON-NLS-1$
	        return sb.toString();
		}
		@Override
		public void traverse(NodeVisitor visitor) {
			if (visitor.visit(this)) {
				if (this.children != null) {
					for (PatternNode child : this.children) {
						if (!visitor.visit(child)) {
							break;
						}
					}
				}
			}
			visitor.endVisit(this);
		}
	}
	class PatternNode extends Node {
		TNode next; // next component

		PatternNode(TypeBinding type) {
			this.type = type;
		}

		public void addPattern(RecordPattern rp, int i) {
			TypeBinding ref = SwitchStatement.this.expression.resolvedType;
			if (!(ref instanceof ReferenceBinding))
				return;
			RecordComponentBinding[] comps = ref.components();
			if (comps == null || comps.length <= i) // safety-net for incorrect code.
				return;
			if (this.next == null)
				this.next = new TNode(comps[i].type);
			this.next.addPattern(rp, i);
		}
		@Override
		public String toString() {
	        StringBuilder sb = new StringBuilder();
	        sb.append("[Pattern node] {\n"); //$NON-NLS-1$
	        sb.append("    type:"); //$NON-NLS-1$
	        sb.append(this.type != null ? this.type.toString() : "null"); //$NON-NLS-1$
	        sb.append("    next:"); //$NON-NLS-1$
	        sb.append(this.next != null ? this.next.toString() : "null"); //$NON-NLS-1$
	        sb.append("\n}\n"); //$NON-NLS-1$
	        return sb.toString();
		}
		@Override
		public void traverse(NodeVisitor visitor) {
			if (visitor.visit(this)) {
				if (this.next != null) {
					visitor.visit(this.next);
				}
			}
			visitor.endVisit(this);
		}
	}
	class RecordPatternNode extends PatternNode {
		RNode rNode;
		RecordPatternNode(TypeBinding type) {
			super(type);
		}
		@Override
		public String toString() {
	        StringBuilder sb = new StringBuilder();
	        sb.append("[RecordPattern node] {\n"); //$NON-NLS-1$
	        sb.append("    type:"); //$NON-NLS-1$
	        sb.append(this.type != null ? this.type.toString() : "null"); //$NON-NLS-1$
	        sb.append("    next:"); //$NON-NLS-1$
	        sb.append(this.next != null ? this.next.toString() : "null"); //$NON-NLS-1$
	        sb.append("    rNode:"); //$NON-NLS-1$
	        sb.append(this.rNode != null ? this.rNode.toString() : "null"); //$NON-NLS-1$
	        sb.append("\n}\n"); //$NON-NLS-1$
	        return sb.toString();
		}
		@Override
		public void traverse(NodeVisitor visitor) {
			if (visitor.visit(this)) {
				if (visitor.visit(this.rNode)) {
					if (this.next != null) {
						visitor.visit(this.next);
					}
				}
			}
			visitor.endVisit(this);
		}
	}

	abstract class NodeVisitor {
		public void endVisit(Node node) {
			// do nothing by default
		}
		public void endVisit(PatternNode node) {
			// do nothing by default
		}
		public void endVisit(RecordPatternNode node) {
			// do nothing by default
		}
		public void endVisit(RNode node) {
			// do nothing by default
		}
		public void endVisit(TNode node) {
			// do nothing by default
		}
		public boolean visit(Node node) {
			return true;
		}
		public boolean visit(PatternNode node) {
			return true;
		}
		public boolean visit(RecordPatternNode node) {
			return true;
		}
		public boolean visit(RNode node) {
			return true;
		}
		public boolean visit(TNode node) {
			return true;
		}
	}
	class CoverageCheckerVisitor extends NodeVisitor {
		public boolean covers = true;
		@Override
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
			this.covers = false;
			return false; // no need to visit further.
		}
	}
	protected int getFallThroughState(Statement stmt, BlockScope blockScope) {
		if ((this.switchBits & LabeledRules) != 0) {
			if (stmt.isTrulyExpression() || stmt instanceof ThrowStatement)
				return BREAKING;
			if (!stmt.canCompleteNormally())
				return BREAKING;
			if (stmt instanceof Block block) {
				BreakStatement breakStatement = new BreakStatement(null, block.sourceEnd -1, block.sourceEnd);
				breakStatement.isSynthetic = true; // suppress dead code flagging - codegen will not generate dead code anyway
				int length = block.statements == null ? 0 : block.statements.length;
				if (length == 0) {
					block.statements = new Statement[] {breakStatement};
					block.scope = this.scope; // (upper scope) see Block.resolve() for similar
				} else {
					Statement[] newArray = new Statement[length + 1];
					System.arraycopy(block.statements, 0, newArray, 0, length);
					newArray[length] = breakStatement;
					block.statements = newArray;
				}
				return BREAKING;
			}
		}
		return FALLTHROUGH;
	}
	protected boolean needToCheckFlowInAbsenceOfDefaultBranch() {
		return !this.isExhaustive();
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		try {
			flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo);
			if (isNullHostile()) {
				this.expression.checkNPE(currentScope, flowContext, flowInfo, 1);
			}
			SwitchFlowContext switchContext =
				new SwitchFlowContext(flowContext, this, (this.breakLabel = new BranchLabel()), true, true);

			CompilerOptions compilerOptions = currentScope.compilerOptions();

			// analyse the block by considering specially the case/default statements (need to bind them
			// to the entry point)
			FlowInfo caseInits = FlowInfo.DEAD_END;
			// in case of statements before the first case
			this.preSwitchInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
			int caseIndex = 0;
			if (this.statements != null) {
				int initialComplaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0 ? Statement.COMPLAINED_FAKE_REACHABLE : Statement.NOT_COMPLAINED;
				int complaintLevel = initialComplaintLevel;
				int fallThroughState = CASE;
				int prevCaseStmtIndex = -100;
				for (int i = 0, max = this.statements.length; i < max; i++) {
					Statement statement = this.statements[i];
					if ((caseIndex < this.caseCount) && (statement == this.cases[caseIndex])) { // statement is a case
						this.scope.enclosingCase = this.cases[caseIndex]; // record entering in a switch case block
						caseIndex++;
						if (prevCaseStmtIndex == i - 1) {
							if (this.statements[prevCaseStmtIndex].containsPatternVariable())
								this.scope.problemReporter().illegalFallthroughFromAPattern(this.statements[prevCaseStmtIndex]);
						}
						prevCaseStmtIndex = i;
						if (fallThroughState == FALLTHROUGH && complaintLevel <= NOT_COMPLAINED) {
							if (statement.containsPatternVariable())
								this.scope.problemReporter().IllegalFallThroughToPattern(this.scope.enclosingCase);
							else if ((statement.bits & ASTNode.DocumentedFallthrough) == 0) { // the case is not fall-through protected by a line comment
								this.scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
							}
						}
						caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
						complaintLevel = initialComplaintLevel; // reset complaint
						fallThroughState = this.containsPatterns ? FALLTHROUGH : CASE;
					} else if (statement == this.defaultCase) { // statement is the default case
						this.scope.enclosingCase = this.defaultCase; // record entering in a switch case block
						if (fallThroughState == FALLTHROUGH
								&& complaintLevel <= NOT_COMPLAINED
								&& (statement.bits & ASTNode.DocumentedFallthrough) == 0) {
							this.scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
						}
						caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
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
						complaintLevel = initialComplaintLevel; // reset complaint
						fallThroughState = this.containsPatterns ? FALLTHROUGH : CASE;
					} else {
						fallThroughState = getFallThroughState(statement, currentScope); // reset below if needed
					}
					if ((complaintLevel = statement.complainIfUnreachable(caseInits, this.scope, complaintLevel, true)) < Statement.COMPLAINED_UNREACHABLE) {
						caseInits = statement.analyseCode(this.scope, switchContext, caseInits);
						if (caseInits == FlowInfo.DEAD_END) {
							fallThroughState = ESCAPING;
						}
						if (compilerOptions.enableSyntacticNullAnalysisForFields) {
							switchContext.expireNullCheckedFieldInfo();
						}
						if (compilerOptions.analyseResourceLeaks) {
							FakedTrackingVariable.cleanUpUnassigned(this.scope, statement, caseInits, false);
						}
					}
				}
			}

			final TypeBinding resolvedTypeBinding = this.expression.resolvedType;
			if (resolvedTypeBinding.isEnum()) {
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

			// merge all branches inits
			FlowInfo mergedInfo = caseInits.mergedWith(switchContext.initsOnBreak);
			this.mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}
	private boolean isNullHostile() {
		if (this.containsNull)
			return false;
		if ((this.expression.implicitConversion & TypeIds.UNBOXING) != 0) {
			return true;
		} else if (this.expression.resolvedType != null
						&& (this.expression.resolvedType.id == T_JavaLangString || this.expression.resolvedType.isEnum())) {
			return true;
		} else if ((this.switchBits & (LabeledRules|NullCase)) == LabeledRules && this.totalPattern == null) {
			return true;
		}
		return false;
	}

	/**
	 * Switch on String code generation
	 * This assumes that hashCode() specification for java.lang.String is API
	 * and is stable.
	 *
	 * @see "http://download.oracle.com/javase/6/docs/api/java/lang/String.html"
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCodeForStringSwitch(BlockScope currentScope, CodeStream codeStream) {

		try {
			if ((this.bits & IsReachable) == 0) {
				return;
			}
			int pc = codeStream.position;

			class StringSwitchCase implements Comparable {
				int hashCode;
				String string;
				BranchLabel label;
				public StringSwitchCase(int hashCode, String string, BranchLabel label) {
					this.hashCode = hashCode;
					this.string = string;
					this.label = label;
				}
				@Override
				public int compareTo(Object o) {
					StringSwitchCase that = (StringSwitchCase) o;
					if (this.hashCode == that.hashCode) {
						return 0;
					}
					if (this.hashCode > that.hashCode) {
						return 1;
					}
					return -1;
				}
				@Override
				public String toString() {
					return "StringSwitchCase :\n" + //$NON-NLS-1$
					       "case " + this.hashCode + ":(" + this.string + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			/*
			 * With multi constant case statements, the number of case statements (hence branch labels)
			 * and number of constants (hence hashcode labels) could be different. For e.g:

			  switch(s) {
			  	case "FB", "c":
			  		System.out.println("A/C");
			 		break;
			  	case "Ea":
					System.out.println("B");
					break;

				With the above code, we will have
				2 branch labels for FB and c
				3 stringCases for FB, c and Ea
				2 hashCodeCaseLabels one for FB, Ea and one for c

				Should produce something like this:
				lookupswitch  { // 2
                      99: 32
                    2236: 44
                 default: 87

				"FB" and "Ea" producing the same hashcode values, but still belonging in different case statements.
				First, produce the two branch labels pertaining to the case statements
				And the three string cases and use the this.constMapping to get the correct branch label.
			 */
			final boolean hasCases = this.caseCount != 0;
			int constSize = hasCases ? this.otherConstants.length : 0;
			BranchLabel[] sourceCaseLabels = this.<BranchLabel>gatherLabels(codeStream, new BranchLabel[this.nConstants], BranchLabel::new);
			StringSwitchCase [] stringCases = new StringSwitchCase[constSize]; // may have to shrink later if multiple strings hash to same code.
			CaseLabel [] hashCodeCaseLabels = new CaseLabel[constSize];
			this.constants = new int[constSize];  // hashCode() values.
			for (int i = 0; i < constSize; i++) {
				String literal = this.otherConstants[i].c.stringValue();
				stringCases[i] = new StringSwitchCase(literal.hashCode(), literal, sourceCaseLabels[this.constMapping[i]]);
				hashCodeCaseLabels[i] = new CaseLabel(codeStream);
				hashCodeCaseLabels[i].tagBits |= BranchLabel.USED;
			}
			Arrays.sort(stringCases);

			int uniqHashCount = 0;
			int lastHashCode = 0;
			for (int i = 0, length = constSize; i < length; ++i) {
				int hashCode = stringCases[i].hashCode;
				if (i == 0 || hashCode != lastHashCode) {
					lastHashCode = this.constants[uniqHashCount++] = hashCode;
				}
			}

			if (uniqHashCount != constSize) { // multiple keys hashed to the same value.
				System.arraycopy(this.constants, 0, this.constants = new int[uniqHashCount], 0, uniqHashCount);
				System.arraycopy(hashCodeCaseLabels, 0, hashCodeCaseLabels = new CaseLabel[uniqHashCount], 0, uniqHashCount);
			}
			int[] sortedIndexes = new int[uniqHashCount]; // hash code are sorted already anyways.
			for (int i = 0; i < uniqHashCount; i++) {
				sortedIndexes[i] = i;
			}

			CaseLabel defaultCaseLabel = new CaseLabel(codeStream);
			defaultCaseLabel.tagBits |= BranchLabel.USED;

			// prepare the labels and constants
			this.breakLabel.initialize(codeStream);

			BranchLabel defaultBranchLabel = new BranchLabel(codeStream);
			if (hasCases) defaultBranchLabel.tagBits |= BranchLabel.USED;
			if (this.defaultCase != null) {
				this.defaultCase.targetLabel = defaultBranchLabel;
			}
			// generate expression
			this.expression.generateCode(currentScope, codeStream, true);
			codeStream.store(this.dispatchStringCopy, true);  // leaves string on operand stack
			codeStream.addVariable(this.dispatchStringCopy);
			codeStream.invokeStringHashCode();
			if (hasCases) {
				codeStream.lookupswitch(defaultCaseLabel, this.constants, sortedIndexes, hashCodeCaseLabels);
				for (int i = 0, j = 0, max = constSize; i < max; i++) {
					int hashCode = stringCases[i].hashCode;
					if (i == 0 || hashCode != lastHashCode) {
						lastHashCode = hashCode;
						if (i != 0) {
							codeStream.goto_(defaultBranchLabel);
						}
						hashCodeCaseLabels[j++].place();
					}
					codeStream.load(this.dispatchStringCopy);
					codeStream.ldc(stringCases[i].string);
					codeStream.invokeStringEquals();
					codeStream.ifne(stringCases[i].label);
				}
				codeStream.goto_(defaultBranchLabel);
			} else {
				codeStream.pop();
			}

			// generate the switch block statements
			int caseIndex = 0;
			if (this.statements != null) {
				for (Statement statement : this.statements) {
					if ((caseIndex < this.caseCount) && (statement == this.cases[caseIndex])) { // statements[i] is a case
						this.scope.enclosingCase = this.cases[caseIndex]; // record entering in a switch case block
						if (this.preSwitchInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
						}
						if (statement == this.defaultCase) { // statements[i] is a case or a default case
							defaultCaseLabel.place(); // branch label gets placed by generateCode below.
						}
						caseIndex++;
					} else {
						if (statement == this.defaultCase) { // statements[i] is a case or a default case
							defaultCaseLabel.place(); // branch label gets placed by generateCode below.
							this.scope.enclosingCase = this.defaultCase; // record entering in a switch case block
							if (this.preSwitchInitStateIndex != -1) {
								codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
							}
						}
					}
					statementGenerateCode(currentScope, codeStream, statement);
				}
			}

			// May loose some local variable initializations : affecting the local variable attributes
			if (this.mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
			}
			codeStream.removeVariable(this.dispatchStringCopy);
			if (this.scope != currentScope) {
				codeStream.exitUserScope(this.scope);
			}
			// place the trailing labels (for break and default case)
			this.breakLabel.place();
			if (this.defaultCase == null) {
				// we want to force an line number entry to get an end position after the switch statement
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd, true);
				defaultCaseLabel.place();
				defaultBranchLabel.place();
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}
	private <T extends BranchLabel>T[] gatherLabels(CodeStream codeStream, T[] caseLabels,
			Function<CodeStream, T> newLabel)
	{
		for (int i = 0, j = 0, max = this.caseCount; i < max; i++) {
			CaseStatement stmt = this.cases[i];
			final Expression[] peeledLabelExpressions = stmt.peeledLabelExpressions();
			int l = peeledLabelExpressions.length;
			BranchLabel[] targetLabels = new BranchLabel[l];
			int count = 0;
			for (int k = 0; k < l; ++k) {
				Expression e = peeledLabelExpressions[k];
				if (e instanceof FakeDefaultLiteral) continue;
				targetLabels[count++] = (caseLabels[j] = newLabel.apply(codeStream));
				if (e == this.totalPattern)
					this.defaultCase = stmt;
				caseLabels[j++].tagBits |= BranchLabel.USED;
			}
			System.arraycopy(targetLabels, 0, stmt.targetLabels = new BranchLabel[count], 0, count);
		}
		return caseLabels;
	}
	/**
	 * Switch code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if (this.expression.resolvedType.id == TypeIds.T_JavaLangString && !this.isNonTraditional) {
			generateCodeForStringSwitch(currentScope, codeStream);
			return;
		}
		try {
			if ((this.bits & IsReachable) == 0) {
				return;
			}
			int pc = codeStream.position;

			// prepare the labels and constants
			this.breakLabel.initialize(codeStream);
			int constantCount = this.otherConstants == null ? 0 : this.otherConstants.length;
			CaseLabel[] caseLabels = this.<CaseLabel>gatherLabels(codeStream, new CaseLabel[this.nConstants], CaseLabel::new);

			CaseLabel defaultLabel = new CaseLabel(codeStream);
			final boolean hasCases = this.caseCount != 0;
			if (hasCases) defaultLabel.tagBits |= BranchLabel.USED;
			if (this.defaultCase != null) {
				this.defaultCase.targetLabel = defaultLabel;
			}

			final TypeBinding resolvedType1 = this.expression.resolvedType;
			boolean valueRequired = false;
			if (needPatternDispatchCopy()) {
				generateCodeSwitchPatternPrologue(currentScope, codeStream);
				valueRequired = true;
				transformConstants();
			} else if (resolvedType1.isEnum()) {
				// go through the translation table
				codeStream.invoke(Opcodes.OPC_invokestatic, this.synthetic, null /* default declaringClass */);
				this.expression.generateCode(currentScope, codeStream, true);
				// get enum constant ordinal()
				codeStream.invokeEnumOrdinal(resolvedType1.constantPoolName());
				codeStream.iaload();
				if (!hasCases) {
					// we can get rid of the generated ordinal value
					codeStream.pop();
				}
				valueRequired = hasCases;
			} else {
				valueRequired = this.expression.constant == Constant.NotAConstant || hasCases;
				// generate expression
				this.expression.generateCode(currentScope, codeStream, valueRequired);
				if (resolvedType1.id == TypeIds.T_JavaLangBoolean) {
					codeStream.generateUnboxingConversion(TypeIds.T_boolean); // optimize by avoiding indy typeSwitch
				}
			}
			// generate the appropriate switch table/lookup bytecode
			if (hasCases) {
				int[] sortedIndexes = new int[constantCount];
				// we sort the keys to be able to generate the code for tableswitch or lookupswitch
				for (int i = 0; i < constantCount; i++) {
					sortedIndexes[i] = i;
				}
				int[] localKeysCopy;
				System.arraycopy(this.constants, 0, (localKeysCopy = new int[constantCount]), 0, constantCount);
				CodeStream.sort(localKeysCopy, 0, constantCount - 1, sortedIndexes);

				int max = localKeysCopy[constantCount - 1];
				int min = localKeysCopy[0];
				if ((long) (constantCount * 2.5) > ((long) max - (long) min)) {
						codeStream.tableswitch(
							defaultLabel,
							min,
							max,
							this.constants,
							sortedIndexes,
							this.constMapping,
							caseLabels);
				} else {
					codeStream.lookupswitch(defaultLabel, this.constants, sortedIndexes, caseLabels);
				}
				codeStream.recordPositionsFrom(codeStream.position, this.expression.sourceEnd);
			} else if (valueRequired) {
				codeStream.pop();
			}

			// generate the switch block statements
			int caseIndex = 0;
			int typeSwitchIndex = 0;
			if (this.statements != null) {
				for (Statement statement : this.statements) {
					CaseStatement caseStatement = null;
					if ((caseIndex < constantCount) && (statement == this.cases[caseIndex])) { // statements[i] is a case
						this.scope.enclosingCase = this.cases[caseIndex]; // record entering in a switch case block
						if (this.preSwitchInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
						}
						caseStatement = (CaseStatement) statement;
						caseIndex++;
						caseStatement.typeSwitchIndex = typeSwitchIndex;
						typeSwitchIndex += caseStatement.constantExpressions.length;
					} else {
						if (statement == this.defaultCase) { // statements[i] is a case or a default case
							this.scope.enclosingCase = this.defaultCase; // record entering in a switch case block
							if (this.preSwitchInitStateIndex != -1) {
								codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
							}
						}
					}
					statementGenerateCode(currentScope, codeStream, statement);
				}
			}
			boolean needsThrowingDefault = false;
			if (this.defaultCase == null) {
				// enum:
				needsThrowingDefault = resolvedType1.isEnum() && (this instanceof SwitchExpression || this.containsNull);
				// pattern switches:
				needsThrowingDefault |= isExhaustive();
			}
			if (needsThrowingDefault) {
				// we want to force an line number entry to get an end position after the switch statement
				if (this.preSwitchInitStateIndex != -1) {
					codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
				}
				/* a default case is not needed for an exhaustive switch expression
				 * we need to handle the default case to throw an error in order to make the stack map consistent.
				 * All cases will return a value on the stack except the missing default case.
				 * There is no returned value for the default case so we handle it with an exception thrown.
				 */
				CompilerOptions compilerOptions = this.scope != null ? this.scope.compilerOptions() : null;
				if (compilerOptions.complianceLevel >= ClassFileConstants.JDK19) {
					// since 19 we have MatchException for this
					if (this.statements.length > 0 && this.statements[this.statements.length - 1].canCompleteNormally()) {
						codeStream.goto_(this.breakLabel); // hop, skip and jump over match exception throw.
					}
					defaultLabel.place();
					codeStream.newJavaLangMatchException();
					codeStream.dup();
					codeStream.aconst_null();
					codeStream.aconst_null();
					codeStream.invokeJavaLangMatchExceptionConstructor();
					codeStream.athrow();
				} else {
					// old style using IncompatibleClassChangeError:
					defaultLabel.place();
					codeStream.newJavaLangIncompatibleClassChangeError();
					codeStream.dup();
					codeStream.invokeJavaLangIncompatibleClassChangeErrorDefaultConstructor();
					codeStream.athrow();
				}
			}
			// May loose some local variable initializations : affecting the local variable attributes
			if (this.mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
			}
			generateCodeSwitchPatternEpilogue(codeStream);
			if (this.scope != currentScope) {
				codeStream.exitUserScope(this.scope);
			}
			// place the trailing labels (for break and default case)
			this.breakLabel.place();
			if (this.defaultCase == null && !needsThrowingDefault) {
				// we want to force an line number entry to get an end position after the switch statement
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd, true);
				defaultLabel.place();
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}
	private void transformConstants() {
		if (this.nullCase == null) {
			for (int i = 0,l = this.otherConstants.length; i < l; ++i) {
				if (this.otherConstants[i].e == this.totalPattern) {
					this.otherConstants[i].index = -1;
					break;
				}
			}
		}
		for (int i = 0; i < this.constants.length; i++) {
			this.constants[i] = this.otherConstants[i].index;
		}
	}
	private void generateCodeSwitchPatternEpilogue(CodeStream codeStream) {
		if (needPatternDispatchCopy()) {
			codeStream.removeVariable(this.dispatchPatternCopy);
			codeStream.removeVariable(this.restartIndexLocal);
		}
	}

	private void generateCodeSwitchPatternPrologue(BlockScope currentScope, CodeStream codeStream) {
		this.expression.generateCode(currentScope, codeStream, true);
		if ((this.switchBits & NullCase) == 0 && !this.expression.resolvedType.isPrimitiveType()) {
			codeStream.dup();
			codeStream.invokeJavaUtilObjectsrequireNonNull();
			codeStream.pop();
		}

		codeStream.store(this.dispatchPatternCopy, false);
		codeStream.addVariable(this.dispatchPatternCopy);

		codeStream.loadInt(0); // restartIndex
		codeStream.store(this.restartIndexLocal, false);
		codeStream.addVariable(this.restartIndexLocal);

		this.switchPatternRestartTarget = new BranchLabel(codeStream);
		this.switchPatternRestartTarget.place();

		codeStream.load(this.dispatchPatternCopy);
		codeStream.load(this.restartIndexLocal);
		int invokeDynamicNumber = codeStream.classFile.recordBootstrapMethod(this);
		if (this.expression.resolvedType.isEnum()) {
			generateEnumSwitchPatternPrologue(codeStream, invokeDynamicNumber);
		} else {
			generateTypeSwitchPatternPrologue(codeStream, invokeDynamicNumber);
		}
		boolean hasQualifiedEnums = (this.switchBits & QualifiedEnum) != 0;
		for (int i = 0; i < this.otherConstants.length; i++) {
			ResolvedCase c = this.otherConstants[i];
			if (hasQualifiedEnums) {
				c.index = i;
			}
			if (c.t.isPrimitiveType()) {
				SingletonBootstrap descriptor = null;
				if (c.isPattern()) {
					descriptor = PRIMITIVE_CLASS__BOOTSTRAP;
				} else if (c.t.id == TypeIds.T_boolean) {
					descriptor = GET_STATIC_FINAL__BOOTSTRAP;
				}
				if (descriptor != null) {
					c.primitivesBootstrapIdx = codeStream.classFile.recordSingletonBootstrapMethod(descriptor);
				}
				continue;
			}
			if (!c.isQualifiedEnum())
				continue;
			int classdescIdx = codeStream.classFile.recordBootstrapMethod(c.t);
			invokeDynamicNumber = codeStream.classFile.recordBootstrapMethod(c);
			c.enumDescIdx = invokeDynamicNumber;
			c.classDescIdx = classdescIdx;
		}
	}
	private void generateTypeSwitchPatternPrologue(CodeStream codeStream, int invokeDynamicNumber) {
		TypeBinding exprType = this.expression.resolvedType;
		char[] signature = typeSwitchSignature(exprType);
		int argsSize = TypeIds.getCategory(exprType.id) + 1; // Object | PRIM, restartIndex (PRIM = Z|S|I..)
		codeStream.invokeDynamic(invokeDynamicNumber,
				argsSize,
				1, // int
				ConstantPool.TYPESWITCH,
				signature,
				TypeBinding.INT);
	}
	char[] typeSwitchSignature(TypeBinding exprType) {
		char[] arg1 = switch (exprType.id) {
			case TypeIds.T_JavaLangLong, TypeIds.T_JavaLangFloat, TypeIds.T_JavaLangDouble, TypeIds.T_JavaLangBoolean,
				TypeIds.T_JavaLangByte, TypeIds.T_JavaLangShort, TypeIds.T_JavaLangInteger, TypeIds.T_JavaLangCharacter->
				this.isPrimitiveSwitch
				? exprType.signature()
				: "Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
			default -> {
				if (exprType.id > TypeIds.T_LastWellKnownTypeId && exprType.erasure().isBoxedPrimitiveType())
					yield exprType.erasure().signature(); // <T extends Integer> / <? extends Short> ...
				else
					yield exprType.isPrimitiveType()
						? exprType.signature()
						: "Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
			}
		};
		return CharOperation.concat("(".toCharArray(), arg1, "I)I".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	private void generateEnumSwitchPatternPrologue(CodeStream codeStream, int invokeDynamicNumber) {
		String genericTypeSignature = new String(this.expression.resolvedType.genericTypeSignature());
		String callingParams = "(" + genericTypeSignature + "I)I"; //$NON-NLS-1$ //$NON-NLS-2$
		codeStream.invokeDynamic(invokeDynamicNumber,
				2, // Object, restartIndex
				1, // int
				"enumSwitch".toCharArray(), //$NON-NLS-1$
				callingParams.toCharArray(),
				TypeBinding.INT);
	}
	protected void statementGenerateCode(BlockScope currentScope, CodeStream codeStream, Statement statement) {
		statement.generateCode(this.scope, codeStream);
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		generateCode(currentScope, codeStream); // redirecting to statement part
	}
	@Override
	public StringBuilder printStatement(int indent, StringBuilder output) {

		printIndent(indent, output).append("switch ("); //$NON-NLS-1$
		this.expression.printExpression(0, output).append(") {"); //$NON-NLS-1$
		if (this.statements != null) {
			for (Statement statement : this.statements) {
				output.append('\n');
				if (statement instanceof CaseStatement) {
					statement.printStatement(indent, output);
				} else {
					statement.printStatement(indent+2, output);
				}
			}
		}
		output.append("\n"); //$NON-NLS-1$
		return printIndent(indent, output).append('}');
	}

	private int getNConstants() {
		int n = 0;
		for (final Statement statement : this.statements) {
			if (statement instanceof CaseStatement)  {
				Expression[] exprs = ((CaseStatement) statement).peeledLabelExpressions();
				int count = 0;
				if (exprs != null) {
					for (Expression e : exprs) {
						if (e instanceof FakeDefaultLiteral) continue;
						++count;
					}
				}
				n += count;
			}
		}
		return n;
	}

	boolean isAllowedType(TypeBinding type) {
		if (type == null)
			return false;
		switch (type.id) {
			case TypeIds.T_char:
			case TypeIds.T_byte:
			case TypeIds.T_short:
			case TypeIds.T_int:
			case TypeIds.T_JavaLangCharacter :
			case TypeIds.T_JavaLangByte :
			case TypeIds.T_JavaLangShort :
			case TypeIds.T_JavaLangInteger :
				return true;
			default: break;
		}
		return false;
	}
	@Override
	public void resolve(BlockScope upperScope) {
		try {
			TypeBinding expressionType = this.expression.resolveType(upperScope);
			CompilerOptions compilerOptions = upperScope.compilerOptions();
			if (expressionType != null) {
				this.expression.computeConversion(upperScope, expressionType, expressionType);
				checkType: {

					if (!expressionType.isValidBinding()) {
						expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
						break checkType;
					}

					if (expressionType.isBaseType()) {
						if (JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(compilerOptions))
							this.isPrimitiveSwitch = true;
						if (this.expression.isConstantValueOfTypeAssignableToType(expressionType, TypeBinding.INT))
							break checkType;
						if (expressionType.isCompatibleWith(TypeBinding.INT))
							break checkType;
					}

					if (expressionType.id == TypeIds.T_JavaLangString || expressionType.isEnum() || upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT))
						break checkType;

					if (!JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions) || (expressionType.isBaseType() && expressionType.id != T_null && expressionType.id != T_void)) {
						if (!this.isPrimitiveSwitch) { // when isPrimitiveSwitch is set it is approved above
							upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType);
							expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
						}
					} else {
						this.isNonTraditional = true;
					}
				}
			}

			if (expressionType != null)
				reserveSecretVariablesSlots(upperScope);

			if (this.statements != null) {
				if (this.scope == null)
					this.scope = new BlockScope(upperScope);
				int length;
				// collection of cases is too big but we will only iterate until caseCount
				this.cases = new CaseStatement[length = this.statements.length];
				this.nConstants = getNConstants();
				this.constants = new int[this.nConstants];
				this.otherConstants = new ResolvedCase[this.nConstants];
				this.constMapping = new int[this.nConstants];
				int counter = 0;
				int caseCounter = 0;
				Pattern[] patterns = new Pattern[this.nConstants];
				int[] caseIndex = new int[this.nConstants];
				LocalVariableBinding[] patternVariables = NO_VARIABLES;
				boolean caseNullDefaultFound = false;
				boolean defaultFound = false;
				Pattern aTotalPattern = null;
				for (int i = 0; i < length; i++) {
					ResolvedCase[] constantsList;
					final Statement statement = this.statements[i];
					if (statement instanceof CaseStatement caseStmt) {
						caseNullDefaultFound |= isCaseStmtNullDefault(caseStmt);
						defaultFound |= caseStmt.constantExpressions == Expression.NO_EXPRESSIONS;
						constantsList = caseStmt.resolveCase(this.scope, expressionType, this);
						patternVariables = statement.bindingsWhenTrue();
						for (ResolvedCase c : constantsList) {
							if (c.e instanceof Pattern p && p.isTotalTypeNode && p.isUnguarded)
								aTotalPattern = p;
							Constant con = c.c;
							if (con == Constant.NotAConstant)
								continue;
							this.otherConstants[counter] = c;
							final int c1 = this.containsPatterns ? (c.intValue() == -1 ? -1 : counter) : c.intValue();
							this.constants[counter] = c1;
							if (counter == 0 && defaultFound) {
								if (c.isPattern() || isCaseStmtNullOnly(caseStmt))
									this.scope.problemReporter().patternDominatedByAnother(c.e);
							}
							for (int j = 0; j < counter; j++) {
								IntPredicate check = idx -> {
									ResolvedCase otherResolvedCase = this.otherConstants[idx];
									Constant c2 = otherResolvedCase.c;
									if (con.typeID() == TypeIds.T_JavaLangString) {
										return c2.stringValue().equals(con.stringValue());
									} else {
										if (c2.typeID() == TypeIds.T_JavaLangString)
											return false;
										int id = c.t.id, otherId = otherResolvedCase.t.id;
										if (id == TypeIds.T_null || otherId == TypeIds.T_null)
											return id == otherId; // 'null' shares IntConstant(-1)
										if (con.equals(c2))
											return true;
										if (id == TypeIds.T_boolean)
											this.switchBits |= Exhaustive; // 2 different boolean constants => exhaustive :)
										return this.constants[idx] == c1;
									}
								};
								TypeBinding type = c.e.resolvedType;
								if (!type.isValidBinding())
									continue;
								if ((caseNullDefaultFound || defaultFound) && (c.isPattern() || isCaseStmtNullOnly(caseStmt))) {
									this.scope.problemReporter().patternDominatedByAnother(c.e);
									break;
								}
								Pattern p1 = patterns[j];
								if (p1 != null) {
									if (c.isPattern()) {
										if (p1.dominates((Pattern) c.e)) {
											this.scope.problemReporter().patternDominatedByAnother(c.e);
										}
									} else {
										if (type.id != TypeIds.T_null) {
											if (type.isBaseType()) {
												type = this.scope.environment().computeBoxingType(type);
											}
											if (p1.coversType(type, this.scope))
												this.scope.problemReporter().patternDominatedByAnother(c.e);
										}
									}
								} else {
									if (!c.isPattern() && check.test(j)) {
										if (this.isNonTraditional) {
											reportDuplicateCase(c.e, this.otherConstants[j].e, length);
										} else {
											reportDuplicateCase(caseStmt, this.cases[caseIndex[j]], length);
										}
									}
								}
							}
							this.constMapping[counter] = counter;
							caseIndex[counter] = caseCounter;
							// Only the pattern expressions count for dominance check
							if (c.e instanceof Pattern) {
								patterns[counter] = (Pattern) c.e;
							}
							counter++;
						}
						caseCounter++;
					} else {
						statement.resolveWithBindings(patternVariables, this.scope);
						patternVariables = LocalVariableBinding.merge(patternVariables, statement.bindingsWhenComplete());
					}
				}
				if (!defaultFound && aTotalPattern != null) {
					this.totalPattern = aTotalPattern;
				}
				if (expressionType != null
						&& (expressionType.id == TypeIds.T_boolean || expressionType.id == TypeIds.T_JavaLangBoolean)
						&& defaultFound && isExhaustive()) {
					upperScope.problemReporter().caseDefaultPlusTrueAndFalse(this);
				}
				if (length != counter) { // resize constants array
					System.arraycopy(this.otherConstants, 0, this.otherConstants = new ResolvedCase[counter], 0, counter);
					System.arraycopy(this.constants, 0, this.constants = new int[counter], 0, counter);
					System.arraycopy(this.constMapping, 0, this.constMapping = new int[counter], 0, counter);
				}
			} else {
				if ((this.bits & UndocumentedEmptyBlock) != 0) {
					upperScope.problemReporter().undocumentedEmptyBlock(this.blockStart, this.sourceEnd);
				}
			}

			if (expressionType != null) {
				if (!expressionType.isBaseType() && upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT)) {
					if (this.containsPatterns || this.containsNull) {
						if (!JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions) || (expressionType.isBaseType() && expressionType.id != T_null && expressionType.id != T_void)) {
							if (!this.isPrimitiveSwitch) { // when isPrimitiveSwitch is set it is approved above
								upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType);
								expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
							}
						}
					} else
						this.expression.computeConversion(upperScope, TypeBinding.INT, expressionType);
				}
				releaseUnusedSecretVariables(upperScope);
				complainIfNotExhaustiveSwitch(upperScope, expressionType, compilerOptions);
			}

		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}
	private void complainIfNotExhaustiveSwitch(BlockScope upperScope, TypeBinding selectorType, CompilerOptions compilerOptions) {

		boolean isEnhanced = isEnhancedSwitch(upperScope, selectorType);
		if (selectorType != null && selectorType.isEnum()) {
			if (isEnhanced)
				this.switchBits |= SwitchStatement.Exhaustive; // negated below if found otherwise
			if (this.defaultCase != null && !compilerOptions.reportMissingEnumCaseDespiteDefault)
				return;

			int constantCount = this.otherConstants == null ? 0 : this.otherConstants.length;
			if (!((this.switchBits & TotalPattern) != 0) &&
					((this.containsPatterns || this.containsNull) ||
					(constantCount >= this.caseCount &&
					constantCount != ((ReferenceBinding)selectorType).enumConstantCount()))) {
				Set<FieldBinding> unenumeratedConstants = unenumeratedConstants((ReferenceBinding) selectorType, constantCount);
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
			    if (this instanceof SwitchExpression // complained about elsewhere, don't also bark here
			    				|| compilerOptions.getSeverity(CompilerOptions.MissingDefaultCase) == ProblemSeverities.Ignore) {
					upperScope.methodScope().hasMissingSwitchDefault = true;
				} else {
					upperScope.problemReporter().missingDefaultCase(this, true, selectorType);
				}
			}
			return;
		}

		if (isExhaustive() || this.defaultCase != null || selectorType == null) {
			if (isEnhanced)
				this.switchBits |= SwitchStatement.Exhaustive;
			return;
		}

		if (JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions) && selectorType.isSealed() && caseElementsCoverSealedType((ReferenceBinding) selectorType, this.caseLabelElementTypes)) {
			this.switchBits |= SwitchStatement.Exhaustive;
			return;
		}

		if (selectorType.isRecordWithComponents() && this.containsRecordPatterns && caseElementsCoverRecordType(upperScope, compilerOptions, (ReferenceBinding) selectorType)) {
			this.switchBits |= SwitchStatement.Exhaustive;
			return;
		}

		if (!isExhaustive()) {
			if (isEnhanced)
				upperScope.problemReporter().enhancedSwitchMissingDefaultCase(this.expression);
			else
				upperScope.problemReporter().missingDefaultCase(this, false, selectorType);
		}
	}

	// Return the set of enumerations belonging to the selector enum type that are NOT listed in case statements.
	private Set<FieldBinding> unenumeratedConstants(ReferenceBinding enumType, int constantCount) {
		FieldBinding[] enumFields = ((ReferenceBinding) enumType.erasure()).fields();
		Set<FieldBinding> unenumerated = new HashSet<>(Arrays.asList(enumFields));
		for (int i = 0, max = enumFields.length; i < max; i++) {
			FieldBinding enumConstant = enumFields[i];
			if ((enumConstant.modifiers & ClassFileConstants.AccEnum) == 0) {
				unenumerated.remove(enumConstant);
				continue;
			}
			for (int j = 0; j < constantCount; j++) {
				if (TypeBinding.equalsEquals(this.otherConstants[j].e.resolvedType, enumType)) {
					if (this.otherConstants[j].e instanceof NameReference reference) {
						FieldBinding field = reference.fieldBinding();
						int intValue = field.original().id + 1;
						if ((enumConstant.id + 1) == intValue) { // zero should not be returned see bug 141810
							unenumerated.remove(enumConstant);
							break;
						}
					}
				}
			}
		}
		return unenumerated;
	}
	private boolean isCaseStmtNullDefault(CaseStatement caseStmt) {
		return caseStmt != null
				&& caseStmt.constantExpressions.length == 2
				&& caseStmt.constantExpressions[0] instanceof NullLiteral
				&& caseStmt.constantExpressions[1] instanceof FakeDefaultLiteral;
	}
	private boolean isCaseStmtNullOnly(CaseStatement caseStmt) {
		return caseStmt != null
				&& caseStmt.constantExpressions.length == 1
				&& caseStmt.constantExpressions[0] instanceof NullLiteral;
	}
	private boolean isExhaustive() {
		return (this.switchBits & SwitchStatement.Exhaustive) != 0;
	}

	private boolean isEnhancedSwitch(BlockScope upperScope, TypeBinding expressionType) {
		if (JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(upperScope.compilerOptions())
				&& expressionType != null && !(this instanceof SwitchExpression)) {

			boolean acceptableType = !expressionType.isEnum();
			switch (expressionType.id) {
				case TypeIds.T_char:
				case TypeIds.T_byte:
				case TypeIds.T_short:
				case TypeIds.T_int:
				case TypeIds.T_long:
				case TypeIds.T_double:
				case TypeIds.T_boolean:
				case TypeIds.T_float:
				case TypeIds.T_void:
				case TypeIds.T_JavaLangCharacter:
				case TypeIds.T_JavaLangByte:
				case TypeIds.T_JavaLangShort:
				case TypeIds.T_JavaLangInteger:
				case TypeIds.T_JavaLangString:
					acceptableType = false;
			}
			if (acceptableType || this.containsPatterns || this.containsNull) {
				return true;
			}
		}
		if (expressionType != null && !(this instanceof SwitchExpression) && JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(upperScope.compilerOptions())) {
			switch (expressionType.id) {
				case TypeIds.T_float:
				case TypeIds.T_double:
				case TypeIds.T_long:
				case TypeIds.T_boolean:
				case TypeIds.T_JavaLangFloat:
				case TypeIds.T_JavaLangDouble:
				case TypeIds.T_JavaLangLong:
				case TypeIds.T_JavaLangBoolean:
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
				int constantCount = this.otherConstants == null ? 0 : this.otherConstants.length;
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
	private boolean needPatternDispatchCopy() {
		if (this.containsPatterns || (this.switchBits & QualifiedEnum) != 0)
			return true;
		if (this.containsNull)
			return true;
		TypeBinding eType = this.expression != null ? this.expression.resolvedType : null;
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

	private void reserveSecretVariablesSlots(BlockScope upperScope) { // may be released later if unused.

		if (this.expression.resolvedType.id == T_JavaLangString) {
			this.dispatchStringCopy  = new LocalVariableBinding(SecretStringVariableName, upperScope.getJavaLangString(), ClassFileConstants.AccDefault, false);
			upperScope.addLocalVariable(this.dispatchStringCopy);
			this.dispatchStringCopy.setConstant(Constant.NotAConstant);
		}

		this.scope = new BlockScope(upperScope);

		this.dispatchPatternCopy  = new LocalVariableBinding(SecretPatternVariableName, this.expression.resolvedType, ClassFileConstants.AccDefault, false);
		this.scope.addLocalVariable(this.dispatchPatternCopy);
		this.dispatchPatternCopy.setConstant(Constant.NotAConstant);

		this.restartIndexLocal  = new LocalVariableBinding(SecretPatternRestartIndexName, TypeBinding.INT, ClassFileConstants.AccDefault, false);
		this.scope.addLocalVariable(this.restartIndexLocal);
		this.restartIndexLocal.setConstant(Constant.NotAConstant);
	}

	private void releaseUnusedSecretVariables(BlockScope upperScope) {
		if (this.expression.resolvedType.id == T_JavaLangString && !this.isNonTraditional)
			this.dispatchStringCopy.useFlag = LocalVariableBinding.USED;

		if (needPatternDispatchCopy()) {
			this.dispatchPatternCopy.useFlag = LocalVariableBinding.USED;
			this.restartIndexLocal.useFlag = LocalVariableBinding.USED;
		}
	}
	protected void reportMissingEnumConstantCase(BlockScope upperScope, FieldBinding enumConstant) {
		upperScope.problemReporter().missingEnumConstantCase(this, enumConstant);
	}
	@Override
	public boolean isTrulyExpression() {
		return false;
	}

	private void reportDuplicateCase(final Statement duplicate,
			final Statement original,
			int length) {
		if (this.duplicateCases == null) {
			this.scope.problemReporter().duplicateCase(original);
			if (duplicate != original)
				this.scope.problemReporter().duplicateCase(duplicate);
			this.duplicateCases = new Statement[length];
			this.duplicateCases[this.duplicateCaseCounter++] = original;
			if (duplicate != original)
				this.duplicateCases[this.duplicateCaseCounter++] = duplicate;
		} else {
			boolean found = false;
			searchReportedDuplicate: for (int k = 2; k < this.duplicateCaseCounter; k++) {
				if (this.duplicateCases[k] == duplicate) {
					found = true;
					break searchReportedDuplicate;
				}
			}
			if (!found) {
				this.scope.problemReporter().duplicateCase(duplicate);
				this.duplicateCases[this.duplicateCaseCounter++] = duplicate;
			}
		}
	}

	@Override
	public void traverse(
			ASTVisitor visitor,
			BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.expression.traverse(visitor, blockScope);
			if (this.statements != null) {
				int statementsLength = this.statements.length;
				for (int i = 0; i < statementsLength; i++)
					this.statements[i].traverse(visitor, this.scope);
			}
		}
		visitor.endVisit(this, blockScope);
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

	@Override
	public boolean doesNotCompleteNormally() {
		if (this.statements == null || this.statements.length == 0)
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
	public boolean canCompleteNormally() {
		if (this.statements == null || this.statements.length == 0)
			return true;
		if ((this.switchBits & LabeledRules) == 0) { // switch labeled statement group
			if (this.statements[this.statements.length - 1].canCompleteNormally())
				return true; // last statement as well as last switch label after blocks if exists.
			if (this.totalPattern == null && this.defaultCase == null)
				return true;
			for (Statement statement : this.statements) {
				if (statement.breaksOut(null))
					return true;
			}
		} else {
			// switch block consists of switch rules
			for (Statement stmt : this.statements) {
				if (stmt instanceof CaseStatement)
					continue; // skip case
				if (this.totalPattern == null && this.defaultCase == null)
					return true;
				if (stmt instanceof Expression)
					return true;
				if (stmt.canCompleteNormally())
					return true;
				if (stmt instanceof YieldStatement && ((YieldStatement) stmt).isImplicit) // note: artificially introduced
					return true;
				if (stmt instanceof Block) {
					Block block = (Block) stmt;
					if (block.canCompleteNormally())
						return true;
					if (block.breaksOut(null))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean continueCompletes() {
		if (this.statements == null || this.statements.length == 0)
			return false;
		for (Statement statement : this.statements) {
			if (statement.continueCompletes())
				return true;
		}
		return false;
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		return printStatement(indent, output);
	}
}
