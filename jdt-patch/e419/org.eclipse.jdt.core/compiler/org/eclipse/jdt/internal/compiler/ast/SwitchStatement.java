/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CaseLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.SwitchFlowContext;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
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
	public int blockStart;
	public int caseCount;
	int[] constants;
	int[] constMapping;
	String[] stringConstants;
	public boolean switchLabeledRules = false; // true if case ->, false if case :
	public int nConstants;

	// fallthrough
	public final static int CASE = 0;
	public final static int FALLTHROUGH = 1;
	public final static int ESCAPING = 2;
	public final static int BREAKING  = 3;

	// for switch on strings
	private static final char[] SecretStringVariableName = " switchDispatchString".toCharArray(); //$NON-NLS-1$


	public SyntheticMethodBinding synthetic; // use for switch on enums types

	// for local variables table attributes
	int preSwitchInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	CaseStatement[] duplicateCaseStatements = null;
	int duplicateCaseStatementsCounter = 0;
	private LocalVariableBinding dispatchStringCopy = null;

	protected int getFallThroughState(Statement stmt, BlockScope blockScope) {
		if (this.switchLabeledRules) {
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
		return true;
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		try {
			flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo);
			if ((this.expression.implicitConversion & TypeIds.UNBOXING) != 0
					|| (this.expression.resolvedType != null
							&& (this.expression.resolvedType.id == T_JavaLangString || this.expression.resolvedType.isEnum()))) {
				this.expression.checkNPE(currentScope, flowContext, flowInfo, 1);
			}
			SwitchFlowContext switchContext =
				new SwitchFlowContext(flowContext, this, (this.breakLabel = new BranchLabel()), true, true);
			switchContext.isExpression = this instanceof SwitchExpression;

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
				for (int i = 0, max = this.statements.length; i < max; i++) {
					Statement statement = this.statements[i];
					if ((caseIndex < this.caseCount) && (statement == this.cases[caseIndex])) { // statement is a case
						this.scope.enclosingCase = this.cases[caseIndex]; // record entering in a switch case block
						caseIndex++;
						if (fallThroughState == FALLTHROUGH
								&& (statement.bits & ASTNode.DocumentedFallthrough) == 0) { // the case is not fall-through protected by a line comment
							this.scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
						}
						caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
						complaintLevel = initialComplaintLevel; // reset complaint
						fallThroughState = CASE;
					} else if (statement == this.defaultCase) { // statement is the default case
						this.scope.enclosingCase = this.defaultCase; // record entering in a switch case block
						if (fallThroughState == FALLTHROUGH
								&& (statement.bits & ASTNode.DocumentedFallthrough) == 0) {
							this.scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
						}
						caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
						complaintLevel = initialComplaintLevel; // reset complaint
						fallThroughState = CASE;
					} else {
						if (!(this instanceof SwitchExpression) &&
							currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK14 &&
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
						switchContext.expireNullCheckedFieldInfo();
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
			int constSize = hasCases ? this.stringConstants.length : 0;
			BranchLabel[] sourceCaseLabels;
			if (currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK12) {
				for (int i = 0, max = this.caseCount; i < max; i++) {
					int l = this.cases[i].constantExpressions.length;
					this.cases[i].targetLabels = new BranchLabel[l];
				}
				sourceCaseLabels = new BranchLabel[this.nConstants];
				int j = 0;
				for (int i = 0, max = this.caseCount; i < max; i++) {
					CaseStatement stmt = this.cases[i];
					for (int k = 0, l = stmt.constantExpressions.length; k < l; ++k) {
						stmt.targetLabels[k] = (sourceCaseLabels[j] = new BranchLabel(codeStream));
						sourceCaseLabels[j++].tagBits |= BranchLabel.USED;
					}
				}
			} else {
				sourceCaseLabels = new BranchLabel[this.caseCount];
				for (int i = 0, max = this.caseCount; i < max; i++) {
					this.cases[i].targetLabel = (sourceCaseLabels[i] = new BranchLabel(codeStream));  // A branch label, not a case label.
					sourceCaseLabels[i].tagBits |= BranchLabel.USED;
				}
			}
			StringSwitchCase [] stringCases = new StringSwitchCase[constSize]; // may have to shrink later if multiple strings hash to same code.
			CaseLabel [] hashCodeCaseLabels = new CaseLabel[constSize];
			this.constants = new int[constSize];  // hashCode() values.
			for (int i = 0; i < constSize; i++) {
				stringCases[i] = new StringSwitchCase(this.stringConstants[i].hashCode(), this.stringConstants[i], sourceCaseLabels[this.constMapping[i]]);
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
				for (int i = 0, maxCases = this.statements.length; i < maxCases; i++) {
					Statement statement = this.statements[i];
					if ((caseIndex < this.caseCount) && (statement == this.cases[caseIndex])) { // statements[i] is a case
						this.scope.enclosingCase = this.cases[caseIndex]; // record entering in a switch case block
						if (this.preSwitchInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
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
			if (this.expectedType() != null) {
				TypeBinding expectedType = this.expectedType().erasure();
				boolean optimizedGoto = codeStream.lastAbruptCompletion == -1;
				// if the last bytecode was an optimized goto (value is already on the stack) or an enum switch without default case, then we need to adjust the
				// stack depth to reflect the fact that there is an value on the stack (return type of the switch expression)
				codeStream.recordExpressionType(expectedType, optimizedGoto ? 0 : 1, optimizedGoto);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}


	/**
	 * Switch code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if (this.expression.resolvedType.id == TypeIds.T_JavaLangString) {
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
			int constantCount = this.constants == null ? 0 : this.constants.length;
			int nCaseLabels = 0;
			CaseLabel[] caseLabels;
			if (currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK12) {
				for (int i = 0, max = this.caseCount; i < max; i++) {
					int l = this.cases[i].constantExpressions.length;
					nCaseLabels += l;
					this.cases[i].targetLabels = new BranchLabel[l];
				}
				caseLabels = new CaseLabel[nCaseLabels];
				int j = 0;
				for (int i = 0, max = this.caseCount; i < max; i++) {
					CaseStatement stmt = this.cases[i];
					for (int k = 0, l = stmt.constantExpressions.length; k < l; ++k) {
						stmt.targetLabels[k] = (caseLabels[j] = new CaseLabel(codeStream));
						caseLabels[j++].tagBits |= BranchLabel.USED;
					}
				}
			} else {
				caseLabels = new CaseLabel[this.caseCount];
				for (int i = 0, max = this.caseCount; i < max; i++) {
					this.cases[i].targetLabel = (caseLabels[i] = new CaseLabel(codeStream));
					caseLabels[i].tagBits |= BranchLabel.USED;
				}
			}

			CaseLabel defaultLabel = new CaseLabel(codeStream);
			final boolean hasCases = this.caseCount != 0;
			if (hasCases) defaultLabel.tagBits |= BranchLabel.USED;
			if (this.defaultCase != null) {
				this.defaultCase.targetLabel = defaultLabel;
			}

			final TypeBinding resolvedType1 = this.expression.resolvedType;
			boolean valueRequired = false;
			if (resolvedType1.isEnum()) {
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
			if (this.statements != null) {
				for (int i = 0, maxCases = this.statements.length; i < maxCases; i++) {
					Statement statement = this.statements[i];
					if ((caseIndex < constantCount) && (statement == this.cases[caseIndex])) { // statements[i] is a case
						this.scope.enclosingCase = this.cases[caseIndex]; // record entering in a switch case block
						if (this.preSwitchInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
						}
						caseIndex++;
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
			boolean enumInSwitchExpression =  resolvedType1.isEnum() && this instanceof SwitchExpression;
			boolean isEnumSwitchWithoutDefaultCase = this.defaultCase == null && enumInSwitchExpression;
			if (isEnumSwitchWithoutDefaultCase) {
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
				codeStream.newJavaLangIncompatibleClassChangeError();
				codeStream.dup();
				codeStream.invokeJavaLangIncompatibleClassChangeErrorDefaultConstructor();
				codeStream.athrow();
			}
			// May loose some local variable initializations : affecting the local variable attributes
			if (this.mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
			}
			if (this.scope != currentScope) {
				codeStream.exitUserScope(this.scope);
			}
			// place the trailing labels (for break and default case)
			this.breakLabel.place();
			if (this.defaultCase == null && !enumInSwitchExpression) {
				// we want to force an line number entry to get an end position after the switch statement
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd, true);
				defaultLabel.place();
			}
			if (this instanceof SwitchExpression) {
				TypeBinding switchResolveType = this.resolvedType;
				if (this.expectedType() != null) {
					switchResolveType = this.expectedType().erasure();
				}
				boolean optimizedGoto = codeStream.lastAbruptCompletion == -1;
				// if the last bytecode was an optimized goto (value is already on the stack) or an enum switch without default case, then we need to adjust the
				// stack depth to reflect the fact that there is an value on the stack (return type of the switch expression)
				codeStream.recordExpressionType(switchResolveType, optimizedGoto ? 0 : 1, optimizedGoto || isEnumSwitchWithoutDefaultCase);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}
	protected void statementGenerateCode(BlockScope currentScope, CodeStream codeStream, Statement statement) {
		statement.generateCode(this.scope, codeStream);
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		generateCode(currentScope, codeStream); // redirecting to statement part
	}
	@Override
	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output).append("switch ("); //$NON-NLS-1$
		this.expression.printExpression(0, output).append(") {"); //$NON-NLS-1$
		if (this.statements != null) {
			for (int i = 0; i < this.statements.length; i++) {
				output.append('\n');
				if (this.statements[i] instanceof CaseStatement) {
					this.statements[i].printStatement(indent, output);
				} else {
					this.statements[i].printStatement(indent+2, output);
				}
			}
		}
		output.append("\n"); //$NON-NLS-1$
		return printIndent(indent, output).append('}');
	}

	private int getNConstants() {
		int n = 0;
		for (int i = 0, l = this.statements.length; i < l; ++i) {
			final Statement statement = this.statements[i];
			if (!(statement instanceof CaseStatement))  {
				continue;
			}
			CaseStatement caseStmt = (CaseStatement) statement;
			n += caseStmt.constantExpressions != null ? caseStmt.constantExpressions.length :
				caseStmt.constantExpression != null ? 1 : 0;
		}
		return n;
	}
	protected void addSecretTryResultVariable() {
		// do nothing
	}
	@Override
	public void resolve(BlockScope upperScope) {
		try {
			boolean isEnumSwitch = false;
			boolean isStringSwitch = false;
			TypeBinding expressionType = this.expression.resolveType(upperScope);
			CompilerOptions compilerOptions = upperScope.compilerOptions();
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
					} else if (upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT)) {
						this.expression.computeConversion(upperScope, TypeBinding.INT, expressionType);
						break checkType;
					} else if (compilerOptions.complianceLevel >= ClassFileConstants.JDK1_7 && expressionType.id == TypeIds.T_JavaLangString) {
						isStringSwitch = true;
						break checkType;
					}
					upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType);
					expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
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
			if (this.statements != null) {
				this.scope = new BlockScope(upperScope);
//				addSecretTryResultVariable();
				int length;
				// collection of cases is too big but we will only iterate until caseCount
				this.cases = new CaseStatement[length = this.statements.length];
				this.nConstants = getNConstants();
				if (!isStringSwitch) {
					this.constants = new int[this.nConstants];
					this.constMapping = new int[this.nConstants];
				} else {
					this.stringConstants = new String[this.nConstants];
					this.constMapping = new int[this.nConstants];
				}
				int counter = 0;
				int caseCounter = 0;
				for (int i = 0; i < length; i++) {
					Constant[] constantsList;
					int[] caseIndex = new int[this.nConstants];
					final Statement statement = this.statements[i];
					if (!(statement instanceof CaseStatement))  {
						statement.resolve(this.scope);
						continue;
					}
					if ((constantsList = statement.resolveCase(this.scope, expressionType, this)) != Constant.NotAConstantList) {
						for (Constant con : constantsList) {
							if (con == Constant.NotAConstant)
								continue;
							if (!isStringSwitch) {
								int key = con.intValue();
								//----check for duplicate case statement------------
								for (int j = 0; j < counter; j++) {
									if (this.constants[j] == key) {
										reportDuplicateCase((CaseStatement) statement, this.cases[caseIndex[j]], length);
									}
								}
								this.constants[counter] = key;
							} else {
								String key = con.stringValue();
								//----check for duplicate case statement------------
								for (int j = 0; j < counter; j++) {
									if (this.stringConstants[j].equals(key)) {
										reportDuplicateCase((CaseStatement) statement, this.cases[caseIndex[j]], length);
									}
								}
								this.stringConstants[counter] = key;
							}
							this.constMapping[counter] = counter;
							caseIndex[counter] = caseCounter;
							counter++;
						}
					}
					caseCounter++;
				}
				if (length != counter) { // resize constants array
					if (!isStringSwitch) {
						System.arraycopy(this.constants, 0, this.constants = new int[counter], 0, counter);
					} else {
						System.arraycopy(this.stringConstants, 0, this.stringConstants = new String[counter], 0, counter);
					}
					System.arraycopy(this.constMapping, 0, this.constMapping = new int[counter], 0, counter);
				}
			} else {
				if ((this.bits & UndocumentedEmptyBlock) != 0) {
					upperScope.problemReporter().undocumentedEmptyBlock(this.blockStart, this.sourceEnd);
				}
			}
			reportMixingCaseTypes();
			// check default case for all kinds of switch:
			if (this.defaultCase == null) {
				if (ignoreMissingDefaultCase(compilerOptions, isEnumSwitch)) {
					if (isEnumSwitch) {
						upperScope.methodScope().hasMissingSwitchDefault = true;
					}
				} else {
					upperScope.problemReporter().missingDefaultCase(this, isEnumSwitch, expressionType);
				}
			}
			// for enum switch, check if all constants are accounted for (perhaps depending on existence of a default case)
			if (isEnumSwitch && compilerOptions.complianceLevel >= ClassFileConstants.JDK1_5) {
				if (this.defaultCase == null || compilerOptions.reportMissingEnumCaseDespiteDefault) {
					int constantCount = this.constants == null ? 0 : this.constants.length; // could be null if no case statement
					if (constantCount >= this.caseCount
							&& constantCount != ((ReferenceBinding)expressionType).enumConstantCount()) {
						FieldBinding[] enumFields = ((ReferenceBinding)expressionType.erasure()).fields();
						for (int i = 0, max = enumFields.length; i <max; i++) {
							FieldBinding enumConstant = enumFields[i];
							if ((enumConstant.modifiers & ClassFileConstants.AccEnum) == 0) continue;
							findConstant : {
								for (int j = 0; j < constantCount; j++) {
									if ((enumConstant.id + 1) == this.constants[j]) // zero should not be returned see bug 141810
										break findConstant;
								}
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
			this.switchLabeledRules = this.defaultCase != null ? this.defaultCase.isExpr : this.switchLabeledRules;
			return;
		}
		boolean isExpr = this.switchLabeledRules = this.cases[0].isExpr;
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
	private void reportDuplicateCase(final CaseStatement duplicate, final CaseStatement original, int length) {
		if (this.duplicateCaseStatements == null) {
			this.scope.problemReporter().duplicateCase(original);
			if (duplicate != original)
				this.scope.problemReporter().duplicateCase(duplicate);
			this.duplicateCaseStatements = new CaseStatement[length];
			this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = original;
			if (duplicate != original)
				this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = duplicate;
		} else {
			boolean found = false;
			searchReportedDuplicate: for (int k = 2; k < this.duplicateCaseStatementsCounter; k++) {
				if (this.duplicateCaseStatements[k] == duplicate) {
					found = true;
					break searchReportedDuplicate;
				}
			}
			if (!found) {
				this.scope.problemReporter().duplicateCase(duplicate);
				this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = duplicate;
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
		for (int i = 0, length = this.statements.length; i < length; i++) {
			if (this.statements[i].breaksOut(null))
				return false;
		}
		return this.statements[this.statements.length - 1].doesNotCompleteNormally();
	}

	@Override
	public boolean completesByContinue() {
		if (this.statements == null || this.statements.length == 0)
			return false;
		for (int i = 0, length = this.statements.length; i < length; i++) {
			if (this.statements[i].completesByContinue())
				return true;
		}
		return false;
	}

	@Override
	public boolean canCompleteNormally() {
		if (this.statements == null || this.statements.length == 0)
			return true;
		if (!this.switchLabeledRules) { // switch labeled statement group
			if (this.statements[this.statements.length - 1].canCompleteNormally())
				return true; // last statement as well as last switch label after blocks if exists.
			if (this.defaultCase == null)
				return true;
			for (int i = 0, length = this.statements.length; i < length; i++) {
				if (this.statements[i].breaksOut(null))
					return true;
			}
		} else {
			// switch block consists of switch rules
			for (Statement stmt : this.statements) {
				if (stmt instanceof CaseStatement)
					continue; // skip case
				if (this.defaultCase == null)
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
		for (int i = 0, length = this.statements.length; i < length; i++) {
			if (this.statements[i].continueCompletes())
				return true;
		}
		return false;
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return printStatement(indent, output);
	}
}
