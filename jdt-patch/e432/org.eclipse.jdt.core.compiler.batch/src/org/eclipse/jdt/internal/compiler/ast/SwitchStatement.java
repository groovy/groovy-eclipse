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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntPredicate;
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
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

@SuppressWarnings("rawtypes")
public class SwitchStatement extends Expression {

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
			RecordComponentBinding[] comps = ((ReferenceBinding)ref).components();
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
			if (node.type instanceof ReferenceBinding && ((ReferenceBinding)node.type).isSealed()) {
				List<ReferenceBinding> allAllowedTypes = getAllPermittedTypes((ReferenceBinding) node.type);
				this.covers &= isExhaustiveWithCaseTypes(allAllowedTypes, availableTypes);
				return this.covers;
			}
			this.covers = false;
			return false; // no need to visit further.
		}
	}
	protected int getFallThroughState(Statement stmt, BlockScope blockScope) {
		if ((this.switchBits & LabeledRules) != 0) {
			if ((stmt instanceof Expression && ((Expression) stmt).isTrulyExpression()) || stmt instanceof ThrowStatement)
				return BREAKING;
			if (!stmt.canCompleteNormally())
				return BREAKING;

			if (stmt instanceof Block) {
				Block block = (Block) stmt;
				// Note implicit break anyway - Let the flow analysis do the dead code analysis
				BreakStatement breakStatement = new BreakStatement(null, block.sourceEnd -1, block.sourceEnd);
				breakStatement.isSynthetic = true; // suppress dead code flagging - codegen will not generate dead code anyway

				int l = block.statements == null ? 0 : block.statements.length;
				if (l == 0) {
					block.statements = new Statement[] {breakStatement};
					block.scope = this.scope; // (upper scope) see Block.resolve() for similar
				} else {
					Statement[] newArray = new Statement[l + 1];
					System.arraycopy(block.statements, 0, newArray, 0, l);
					newArray[l] = breakStatement;
					block.statements = newArray;
				}
				return BREAKING;
			}
		}
		return FALLTHROUGH;
	}
	protected void completeNormallyCheck(BlockScope blockScope) {
		// do nothing
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
			switchContext.isExpression = this instanceof SwitchExpression;

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
							if (((CaseStatement) this.statements[prevCaseStmtIndex]).containsPatternVariable())
								this.scope.problemReporter().illegalFallthroughFromAPattern(this.statements[prevCaseStmtIndex]);
						}
						prevCaseStmtIndex = i;
						if (fallThroughState == FALLTHROUGH && complaintLevel <= NOT_COMPLAINED) {
							if (((CaseStatement) statement).containsPatternVariable())
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
						if (!isTrulyExpression() &&
							compilerOptions.complianceLevel >= ClassFileConstants.JDK14 &&
							statement instanceof YieldStatement &&
							((YieldStatement) statement).isImplicit) {
							YieldStatement y = (YieldStatement) statement;
							Expression e = ((YieldStatement) statement).expression;
							/* JLS 13 14.11.2
									Switch labeled rules in switch statements differ from those in switch expressions (15.28).
									In switch statements they must be switch labeled statement expressions, ... */
							if (!y.expression.statementExpression()) {
								this.scope.problemReporter().invalidExpressionAsStatement(e);
							}
						}
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
				completeNormallyCheck(currentScope);
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

					// work-around 1.3 VM bug, if max>0x7FFF0000, must use lookup bytecode
					// see http://dev.eclipse.org/bugs/show_bug.cgi?id=21557
					if (max > 0x7FFF0000 && currentScope.compilerOptions().complianceLevel < ClassFileConstants.JDK1_4) {
						codeStream.lookupswitch(defaultLabel, this.constants, sortedIndexes, caseLabels);

					} else {
						codeStream.tableswitch(
							defaultLabel,
							min,
							max,
							this.constants,
							sortedIndexes,
							this.constMapping,
							caseLabels);
					}
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
						} else if (statement instanceof CaseStatement) {
							caseStatement = (CaseStatement) statement;
							caseStatement.typeSwitchIndex = typeSwitchIndex;
							typeSwitchIndex += caseStatement.constantExpressions.length;
						}
					}
					statementGenerateCode(currentScope, codeStream, statement);
				}
			}

			boolean isEnumSwitchWithoutDefaultCase = this.defaultCase == null && resolvedType1.isEnum() && (this instanceof SwitchExpression || this.containsNull);
			CompilerOptions compilerOptions = this.scope != null ? this.scope.compilerOptions() : null;
			boolean isPatternSwitchSealedWithoutDefaultCase = this.defaultCase == null
							&& compilerOptions != null
							&& this.containsPatterns
							&& JavaFeature.SEALED_CLASSES.isSupported(compilerOptions)
							&& JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions)
							&& this.expression.resolvedType instanceof ReferenceBinding
							&& ((ReferenceBinding) this.expression.resolvedType).isSealed();

			boolean isRecordPatternSwitchWithoutDefault = this.defaultCase == null
					&& compilerOptions != null
					&& this.containsPatterns
					&& JavaFeature.RECORD_PATTERNS.isSupported(compilerOptions)
					&& JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions)
					&& this.expression.resolvedType instanceof ReferenceBinding
					&& this.expression.resolvedType.isRecord();
			if (isEnumSwitchWithoutDefaultCase
					|| isPatternSwitchSealedWithoutDefaultCase
					|| isRecordPatternSwitchWithoutDefault) {
				// we want to force an line number entry to get an end position after the switch statement
				if (this.preSwitchInitStateIndex != -1) {
					codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
				}
				defaultLabel.place();
				/* a default case is not needed for enum if all enum values are used in the switch expression
				 * we need to handle the default case to throw an error (IncompatibleClassChangeError) in order
				 * to make the stack map consistent. All cases will return a value on the stack except the missing default
				 * case.
				 * There is no returned value for the default case so we handle it with an exception thrown. An
				 * IllegalClassChangeError seems legitimate as this would mean the enum type has been recompiled with more
				 * enum constants and the class that is using the switch on the enum has not been recompiled
				 */
				if (compilerOptions.complianceLevel >= ClassFileConstants.JDK19) {
					codeStream.newJavaLangMatchException();
					codeStream.dup();
					codeStream.aconst_null();
					codeStream.aconst_null();
					codeStream.invokeJavaLangMatchExceptionConstructor();
					codeStream.athrow();
				} else {
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
			if (this.defaultCase == null && !(isEnumSwitchWithoutDefaultCase
					|| isPatternSwitchSealedWithoutDefaultCase
					|| isRecordPatternSwitchWithoutDefault)) {
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
		if ((this.switchBits & NullCase) == 0) {
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
			if (!c.isQualifiedEnum())
				continue;
			int classdescIdx = codeStream.classFile.recordBootstrapMethod(c.t);
			invokeDynamicNumber = codeStream.classFile.recordBootstrapMethod(c);
			c.enumDescIdx = invokeDynamicNumber;
			c.classDescIdx = classdescIdx;
		}
	}
	private void generateTypeSwitchPatternPrologue(CodeStream codeStream, int invokeDynamicNumber) {
		codeStream.invokeDynamic(invokeDynamicNumber,
				2, // Object, restartIndex
				1, // int
				ConstantPool.TYPESWITCH,
				"(Ljava/lang/Object;I)I".toCharArray(), //$NON-NLS-1$
				TypeIds.T_int,
				TypeBinding.INT);
	}
	private void generateEnumSwitchPatternPrologue(CodeStream codeStream, int invokeDynamicNumber) {
		String genericTypeSignature = new String(this.expression.resolvedType.genericTypeSignature());
		String callingParams = "(" + genericTypeSignature + "I)I"; //$NON-NLS-1$ //$NON-NLS-2$
		codeStream.invokeDynamic(invokeDynamicNumber,
				2, // Object, restartIndex
				1, // int
				"enumSwitch".toCharArray(), //$NON-NLS-1$
				callingParams.toCharArray(),
				TypeIds.T_int,
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
			boolean isEnumSwitch = false;
			boolean isStringSwitch = false;
			TypeBinding expressionType = this.expression.resolveType(upperScope);
			CompilerOptions compilerOptions = upperScope.compilerOptions();
			boolean isEnhanced = checkAndSetEnhanced(upperScope, expressionType);
			if (expressionType != null) {
				this.expression.computeConversion(upperScope, expressionType, expressionType);
				checkType: {
					if (!expressionType.isValidBinding()) {
						expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
						break checkType;
					} else if (expressionType.isBaseType()) {
						if (this.expression.isConstantValueOfTypeAssignableToType(expressionType, TypeBinding.INT))
							break checkType;
						if (expressionType.isCompatibleWith(TypeBinding.INT))
							break checkType;
					} else if (expressionType.isEnum()) {
						isEnumSwitch = true;
						if (compilerOptions.complianceLevel < ClassFileConstants.JDK1_5) {
							upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType); // https://bugs.eclipse.org/bugs/show_bug.cgi?id=360317
						}
						break checkType;
					} else if (!this.containsPatterns && !this.containsNull && upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT)) {
						this.expression.computeConversion(upperScope, TypeBinding.INT, expressionType);
						break checkType;
					} else if (compilerOptions.complianceLevel >= ClassFileConstants.JDK1_7 && expressionType.id == TypeIds.T_JavaLangString) {
						if (this.containsPatterns || this.containsNull) {
							isStringSwitch = !JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions);
							this.isNonTraditional = true;
							break checkType;
						}
						isStringSwitch = true;
						break checkType;
					}
					if (!JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions) || (expressionType.isBaseType() && expressionType.id != T_null && expressionType.id != T_void)) {
						upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType);
						expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
					} else {
						this.isNonTraditional = true;
					}
				}
			}
 			if (isStringSwitch) {
				// the secret variable should be created before iterating over the switch's statements that could
				// create more locals. This must be done to prevent overlapping of locals
				// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=356002
				this.dispatchStringCopy  = new LocalVariableBinding(SecretStringVariableName, upperScope.getJavaLangString(), ClassFileConstants.AccDefault, false);
				upperScope.addLocalVariable(this.dispatchStringCopy);
				this.dispatchStringCopy.setConstant(Constant.NotAConstant);
				this.dispatchStringCopy.useFlag = LocalVariableBinding.USED;
			}
			addSecretPatternSwitchVariables(upperScope);
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
				for (int i = 0; i < length; i++) {
					ResolvedCase[] constantsList;
					final Statement statement = this.statements[i];
					if (statement instanceof CaseStatement caseStmt) {
						caseNullDefaultFound = caseNullDefaultFound ? caseNullDefaultFound
								: isCaseStmtNullDefault(caseStmt);
						defaultFound |= caseStmt.constantExpressions == Expression.NO_EXPRESSIONS;
						constantsList = caseStmt.resolveCase(this.scope, expressionType, this);
						patternVariables = statement.bindingsWhenTrue();
						for (ResolvedCase c : constantsList) {
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
									Constant c2 = this.otherConstants[idx].c;
									if (con.typeID() == TypeIds.T_JavaLangString) {
										return c2.stringValue().equals(con.stringValue());
									} else {
										if (c2.typeID() == TypeIds.T_JavaLangString)
											return false;
										if (con.intValue() == c2.intValue())
											return true;
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
											if (p1.coversType(type))
												this.scope.problemReporter().patternDominatedByAnother(c.e);
										}
									}
								} else {
									if (!c.isPattern() && check.test(j)) {
										if (this.isNonTraditional) {
										if (c.e instanceof NullLiteral && this.otherConstants[j].e instanceof NullLiteral) {
												reportDuplicateCase(c.e, this.otherConstants[j].e, length);
											}
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
			// Try it again in case we found any qualified enums.
			if (this.dispatchPatternCopy == null) {
				addSecretPatternSwitchVariables(upperScope);
			}
			reportMixingCaseTypes();

			// check default case for all kinds of switch:
			boolean flagged = checkAndFlagDefaultSealed(upperScope, compilerOptions);
			if (!flagged && this.defaultCase == null) {
				if (ignoreMissingDefaultCase(compilerOptions, isEnumSwitch) && isEnumSwitch) {
						upperScope.methodScope().hasMissingSwitchDefault = true;
				} else {
					if (!isExhaustive()) {
						if (isEnhanced)
							upperScope.problemReporter().enhancedSwitchMissingDefaultCase(this.expression);
						else
							upperScope.problemReporter().missingDefaultCase(this, isEnumSwitch, expressionType);
					}
				}
			}
			// for enum switch, check if all constants are accounted for (perhaps depending on existence of a default case)
			if (isEnumSwitch && compilerOptions.complianceLevel >= ClassFileConstants.JDK1_5) {
				if (this.defaultCase == null || compilerOptions.reportMissingEnumCaseDespiteDefault) {
					int constantCount = this.otherConstants == null ? 0 : this.otherConstants.length; // could be null if no case statement
					// The previous computation of exhaustiveness by comparing the size of cases to the enum fields
					// no longer holds true when we throw in a pattern expression to the mix.
					// And if there is a total pattern, then we don't have to check further.
					if (!((this.switchBits & TotalPattern) != 0) &&
							(this.containsPatterns ||
							(constantCount >= this.caseCount &&
							constantCount != ((ReferenceBinding)expressionType).enumConstantCount()))) {
						FieldBinding[] enumFields = ((ReferenceBinding)expressionType.erasure()).fields();
						for (int i = 0, max = enumFields.length; i <max; i++) {
							FieldBinding enumConstant = enumFields[i];
							if ((enumConstant.modifiers & ClassFileConstants.AccEnum) == 0) continue;
							findConstant : {
								for (int j = 0; j < constantCount; j++) {
									if ((enumConstant.id + 1) == this.otherConstants[j].c.intValue()) // zero should not be returned see bug 141810
										break findConstant;
								}
								this.switchBits &= ~(1 << SwitchStatement.Exhaustive);
								// enum constant did not get referenced from switch
								boolean suppress = (this.defaultCase != null && (this.defaultCase.bits & DocumentedCasesOmitted) != 0);
								if (!suppress) {
									reportMissingEnumConstantCase(upperScope, enumConstant);
								}
							}
						}
					}
				}
			}
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
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

	private boolean checkAndSetEnhanced(BlockScope upperScope, TypeBinding expressionType) {
		if (JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(upperScope.compilerOptions())
				&& expressionType != null && !(this instanceof SwitchExpression )) {

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
		return false;
	}
	private boolean checkAndFlagDefaultSealed(BlockScope skope, CompilerOptions compilerOptions) {
		if (this.defaultCase != null) { // mark covered as a side effect (since covers is intro in 406)
			this.switchBits |= SwitchStatement.Exhaustive;
			return false;
		}
		boolean checkSealed = this.containsPatterns
				&& JavaFeature.SEALED_CLASSES.isSupported(compilerOptions)
				&& JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(compilerOptions)
				&& this.expression.resolvedType instanceof ReferenceBinding;
		if (!checkSealed) return false;
		ReferenceBinding ref = (ReferenceBinding) this.expression.resolvedType;
		if (!(ref.isClass() || ref.isInterface() || ref.isTypeVariable() || ref.isIntersectionType()))
			return false;
		if (ref instanceof TypeVariableBinding) {
			TypeVariableBinding tvb = (TypeVariableBinding) ref;
			ref = tvb.firstBound instanceof ReferenceBinding ? (ReferenceBinding) tvb.firstBound : ref;
		}
		if (ref.isRecord()) {
			boolean isRecordPattern = false;
			for (Pattern pattern : this.caseLabelElements) {
				if (pattern instanceof RecordPattern) {
					isRecordPattern = true;
					break;
				}
			}
			if (isRecordPattern)
				return checkAndFlagDefaultRecord(skope, compilerOptions, ref);
		}
		if (!ref.isSealed()) return false;
		if (!isExhaustiveWithCaseTypes(getAllPermittedTypes(ref), this.caseLabelElementTypes)) {
			if (this instanceof SwitchExpression) // non-exhaustive switch expressions will be flagged later.
				return false;
			skope.problemReporter().enhancedSwitchMissingDefaultCase(this.expression);
			return true;
		}
		this.switchBits |= SwitchStatement.Exhaustive;
		return false;
	}
	List<ReferenceBinding> getAllPermittedTypes(ReferenceBinding ref) {
		if (!ref.isSealed())
			return new ArrayList<>(0);

		Set<ReferenceBinding> permSet = new HashSet<>(Arrays.asList(ref.permittedTypes()));
		if (ref.isClass() && (!ref.isAbstract()))
			permSet.add(ref);
		Set<ReferenceBinding> oldSet = new HashSet<>(permSet);
		do {
			for (ReferenceBinding type : permSet) {
				oldSet.addAll(Arrays.asList(type.permittedTypes()));
			}
			Set<ReferenceBinding> tmp = oldSet;
			oldSet = permSet;
			permSet = tmp;
		} while (oldSet.size() != permSet.size());
		return Arrays.asList(permSet.toArray(new ReferenceBinding[0]));
	}

	private boolean checkAndFlagDefaultRecord(BlockScope skope, CompilerOptions compilerOptions, ReferenceBinding ref) {
		RecordComponentBinding[] comps = ref.components();
		List<ReferenceBinding> allallowedTypes = new ArrayList<>();
		allallowedTypes.add(ref);
		if (comps == null || comps.length == 0) {
			if (!isExhaustiveWithCaseTypes(allallowedTypes, this.caseLabelElementTypes)) {
				skope.problemReporter().enhancedSwitchMissingDefaultCase(this.expression);
				return true;
			}
			return false;
		}
		// non-zero components
		RNode head = new RNode(ref);
		for (Pattern pattern : this.caseLabelElements) {
			head.addPattern(pattern);
		}
		CoverageCheckerVisitor ccv = new CoverageCheckerVisitor();
		head.traverse(ccv);
		if (!ccv.covers) {
			skope.problemReporter().enhancedSwitchMissingDefaultCase(this.expression);
			return true; // not exhaustive, error flagged
		}
		this.switchBits |= SwitchStatement.Exhaustive;
		return false;
	}
	private boolean isExhaustiveWithCaseTypes(List<ReferenceBinding> allAllowedTypes,  List<TypeBinding> listedTypes) {
		// first KISS (Keep It Simple Stupid)
		int pendingTypes = allAllowedTypes.size();
		for (ReferenceBinding pt : allAllowedTypes) {
			/* Per JLS 14.11.1.1: A type T that names an abstract sealed class or sealed interface is covered
			   if every permitted direct subclass or subinterface of it is covered. These subtypes are already
			   added to allAllowedTypes and subject to cover test.
			*/
			if (pt.isAbstract() && pt.isSealed()) {
				--pendingTypes;
				continue;
			}
			for (TypeBinding type : listedTypes) {
				// permits specifies classes, not parameterizations
				if (pt.erasure().isCompatibleWith(type.erasure())) {
					--pendingTypes;
					break;
				}
			}
		}
		if (pendingTypes == 0)
			return true;
		// else - #KICKME (Keep It Complicated Keep Me Employed)"
		List<TypeBinding> coveredTypes = new ArrayList<>(listedTypes);
		List<ReferenceBinding> remainingTypes = new ArrayList<>(allAllowedTypes);
		remainingTypes.removeAll(coveredTypes);

		Map<TypeBinding, List<TypeBinding>> impliedTypes = new HashMap<>();

		for (ReferenceBinding type : remainingTypes) {
			impliedTypes.put(type, new ArrayList<>());
			List<ReferenceBinding> typesToAdd = new ArrayList<>();
			for (ReferenceBinding impliedType : allAllowedTypes) {
				if (impliedType.equals(type)) continue;
				if (type.isClass()) {
					if (impliedType.isAbstract() && type.superclass().equals(impliedType)) {
						typesToAdd.add(impliedType);
					}
					if (Arrays.asList(type.superInterfaces()).contains(impliedType))
						typesToAdd.add(impliedType);
				} else if (type.isInterface()) {
					if (Arrays.asList(impliedType.superInterfaces()).contains(type))
						typesToAdd.add(impliedType);
				}
			}
			if (!typesToAdd.isEmpty()) {
				impliedTypes.get(type).addAll(typesToAdd);
			}
		}
		boolean delta = true;
		while (delta) {
			delta = false;
			List<ReferenceBinding> typesToAdd = new ArrayList<>();
			for (ReferenceBinding type : remainingTypes) {
				boolean add = false;
				if (type.isClass()) {
					for (TypeBinding tb : impliedTypes.get(type)) {
						if (coveredTypes.contains(tb)) {
							add = true;
							break;
						}
					}
				} else if (type.isInterface()) {
					add = coveredTypes.containsAll(impliedTypes.get(type));
				}
				if (add) {
					typesToAdd.add(type);
				}
			}
			if (!typesToAdd.isEmpty()) {
				remainingTypes.removeAll(typesToAdd);
				coveredTypes.addAll(typesToAdd);
				typesToAdd.clear();
				delta = true;
			}
		}
		return remainingTypes.isEmpty();
	}
	private boolean needPatternDispatchCopy() {
		if (this.containsPatterns || (this.switchBits & QualifiedEnum) != 0)
			return true;
		if (this.containsNull)
			return true;
		TypeBinding eType = this.expression != null ? this.expression.resolvedType : null;
		if (eType == null)
			return false;
		return !(eType.isPrimitiveOrBoxedPrimitiveType() || eType.isEnum() || eType.id == TypeIds.T_JavaLangString); // classic selectors
	}
	private void addSecretPatternSwitchVariables(BlockScope upperScope) {
		if (needPatternDispatchCopy()) {
			this.scope = new BlockScope(upperScope);
			this.dispatchPatternCopy  = new LocalVariableBinding(SecretPatternVariableName, this.expression.resolvedType, ClassFileConstants.AccDefault, false);
			this.scope.addLocalVariable(this.dispatchPatternCopy);
			this.dispatchPatternCopy.setConstant(Constant.NotAConstant);
			this.dispatchPatternCopy.useFlag = LocalVariableBinding.USED;
			this.restartIndexLocal  = new LocalVariableBinding(SecretPatternRestartIndexName, TypeBinding.INT, ClassFileConstants.AccDefault, false);
			this.scope.addLocalVariable(this.restartIndexLocal);
			this.restartIndexLocal.setConstant(Constant.NotAConstant);
			this.restartIndexLocal.useFlag = LocalVariableBinding.USED;
		}
	}
	protected void reportMissingEnumConstantCase(BlockScope upperScope, FieldBinding enumConstant) {
		upperScope.problemReporter().missingEnumConstantCase(this, enumConstant);
	}
	protected boolean ignoreMissingDefaultCase(CompilerOptions compilerOptions, boolean isEnumSwitch) {
		return compilerOptions.getSeverity(CompilerOptions.MissingDefaultCase) == ProblemSeverities.Ignore;
	}
	@Override
	public boolean isTrulyExpression() {
		return false;
	}
	private void reportMixingCaseTypes() {
		if (this.caseCount == 0) {
			if (this.defaultCase != null && this.defaultCase.isExpr)
				this.switchBits |= LabeledRules;
			return;
		}
		if (this.cases[0] == null)
			return;
		boolean isExpr = this.cases[0].isExpr;
		if (isExpr) this.switchBits |= LabeledRules;
		for (int i = 1, l = this.caseCount; i < l; ++i) {
			if (this.cases[i].isExpr != isExpr) {
				this.scope.problemReporter().switchExpressionMixedCase(this.cases[i]);
				return;
			}
		}
		if (this.defaultCase != null && this.defaultCase.isExpr != isExpr) {
			this.scope.problemReporter().switchExpressionMixedCase(this.defaultCase);
		}
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
