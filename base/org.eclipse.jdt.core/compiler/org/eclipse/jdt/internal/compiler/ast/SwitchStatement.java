/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class SwitchStatement extends Statement {

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
	
	// fallthrough
	public final static int CASE = 0;
	public final static int FALLTHROUGH = 1;
	public final static int ESCAPING = 2;
	
	
	public SyntheticMethodBinding synthetic; // use for switch on enums types
	
	// for local variables table attributes
	int preSwitchInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public FlowInfo analyseCode(
			BlockScope currentScope,
			FlowContext flowContext,
			FlowInfo flowInfo) {

	    try {
			flowInfo = expression.analyseCode(currentScope, flowContext, flowInfo);
			SwitchFlowContext switchContext =
				new SwitchFlowContext(flowContext, this, (breakLabel = new BranchLabel()));
	
			// analyse the block by considering specially the case/default statements (need to bind them 
			// to the entry point)
			FlowInfo caseInits = FlowInfo.DEAD_END;
			// in case of statements before the first case
			preSwitchInitStateIndex =
				currentScope.methodScope().recordInitializationStates(flowInfo);
			int caseIndex = 0;
			if (statements != null) {
				boolean didAlreadyComplain = false;
				int fallThroughState = CASE;
				for (int i = 0, max = statements.length; i < max; i++) {
					Statement statement = statements[i];
					if ((caseIndex < caseCount) && (statement == cases[caseIndex])) { // statement is a case
						this.scope.enclosingCase = cases[caseIndex]; // record entering in a switch case block
						caseIndex++;
						if (fallThroughState == FALLTHROUGH
								&& (statement.bits & ASTNode.DocumentedFallthrough) == 0) { // the case is not fall-through protected by a line comment
							scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
						}
						caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
						didAlreadyComplain = false; // reset complaint
						fallThroughState = CASE;
					} else if (statement == defaultCase) { // statement is the default case
						this.scope.enclosingCase = defaultCase; // record entering in a switch case block
						if (fallThroughState == FALLTHROUGH 
								&& (statement.bits & ASTNode.DocumentedFallthrough) == 0) {
							scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
						}
						caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
						didAlreadyComplain = false; // reset complaint
						fallThroughState = CASE;
					} else {
						fallThroughState = FALLTHROUGH; // reset below if needed
					}
					if (!statement.complainIfUnreachable(caseInits, scope, didAlreadyComplain)) {
						caseInits = statement.analyseCode(scope, switchContext, caseInits);
						if (caseInits == FlowInfo.DEAD_END) {
							fallThroughState = ESCAPING;
						}
					} else {
						didAlreadyComplain = true;
					}
				}
			}
	
			final TypeBinding resolvedTypeBinding = this.expression.resolvedType;
			if (caseCount > 0 && resolvedTypeBinding.isEnum()) {
				final SourceTypeBinding sourceTypeBinding = this.scope.classScope().referenceContext.binding;
				this.synthetic = sourceTypeBinding.addSyntheticMethodForSwitchEnum(resolvedTypeBinding);
			}
			// if no default case, then record it may jump over the block directly to the end
			if (defaultCase == null) {
				// only retain the potential initializations
				flowInfo.addPotentialInitializationsFrom(
					caseInits.mergedWith(switchContext.initsOnBreak));
				mergedInitStateIndex =
					currentScope.methodScope().recordInitializationStates(flowInfo);
				return flowInfo;
			}
	
			// merge all branches inits
			FlowInfo mergedInfo = caseInits.mergedWith(switchContext.initsOnBreak);
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;
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
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

	    try {
			if ((bits & IsReachable) == 0) {
				return;
			}
			int pc = codeStream.position;
	
			// prepare the labels and constants
			this.breakLabel.initialize(codeStream);
			CaseLabel[] caseLabels = new CaseLabel[this.caseCount];
			boolean needSwitch = this.caseCount != 0;
			for (int i = 0; i < caseCount; i++) {
				cases[i].targetLabel = (caseLabels[i] = new CaseLabel(codeStream));
				caseLabels[i].tagBits |= BranchLabel.USED;
			}
			CaseLabel defaultLabel = new CaseLabel(codeStream);
			if (needSwitch) defaultLabel.tagBits |= BranchLabel.USED;
			if (defaultCase != null) {
				defaultCase.targetLabel = defaultLabel;
			}

			final TypeBinding resolvedType = this.expression.resolvedType;
			if (resolvedType.isEnum()) {
				if (needSwitch) {
					// go through the translation table
					codeStream.invokestatic(this.synthetic);
					expression.generateCode(currentScope, codeStream, true);
					// get enum constant ordinal()
					codeStream.invokeEnumOrdinal(resolvedType.constantPoolName());
					codeStream.iaload();
				} else {
					// no need to go through the translation table
					expression.generateCode(currentScope, codeStream, false);
				}
			} else {
				// generate expression
				expression.generateCode(currentScope, codeStream, needSwitch); // value required (switch without cases)
			}
			// generate the appropriate switch table/lookup bytecode
			if (needSwitch) {
				int[] sortedIndexes = new int[this.caseCount];
				// we sort the keys to be able to generate the code for tableswitch or lookupswitch
				for (int i = 0; i < caseCount; i++) {
					sortedIndexes[i] = i;
				}
				int[] localKeysCopy;
				System.arraycopy(this.constants, 0, (localKeysCopy = new int[this.caseCount]), 0, this.caseCount);
				CodeStream.sort(localKeysCopy, 0, this.caseCount - 1, sortedIndexes);

				int max = localKeysCopy[this.caseCount - 1];
				int min = localKeysCopy[0];
				if ((long) (caseCount * 2.5) > ((long) max - (long) min)) {
					
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
							caseLabels);
					}
				} else {
					codeStream.lookupswitch(defaultLabel, this.constants, sortedIndexes, caseLabels);
				}
				codeStream.updateLastRecordedEndPC(this.scope, codeStream.position);
			}
			
			// generate the switch block statements
			int caseIndex = 0;
			if (this.statements != null) {
				for (int i = 0, maxCases = this.statements.length; i < maxCases; i++) {
					Statement statement = this.statements[i];
					if ((caseIndex < this.caseCount) && (statement == this.cases[caseIndex])) { // statements[i] is a case
						this.scope.enclosingCase = this.cases[caseIndex]; // record entering in a switch case block
						if (preSwitchInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(currentScope, preSwitchInitStateIndex);
						}
						caseIndex++;
					} else {
						if (statement == this.defaultCase) { // statements[i] is a case or a default case
							this.scope.enclosingCase = this.defaultCase; // record entering in a switch case block
							if (preSwitchInitStateIndex != -1) {
								codeStream.removeNotDefinitelyAssignedVariables(currentScope, preSwitchInitStateIndex);
							}
						}
					}
					statement.generateCode(scope, codeStream);
				}
			}
			// May loose some local variable initializations : affecting the local variable attributes
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
			}
			if (scope != currentScope) {
				codeStream.exitUserScope(this.scope);
			}
			// place the trailing labels (for break and default case)
			this.breakLabel.place();
			if (defaultCase == null) {
				// we want to force an line number entry to get an end position after the switch statement
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd, true);
				defaultLabel.place();
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
	    } finally {
	        if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
	    }		
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output).append("switch ("); //$NON-NLS-1$
		expression.printExpression(0, output).append(") {"); //$NON-NLS-1$
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				output.append('\n');
				if (statements[i] instanceof CaseStatement) {
					statements[i].printStatement(indent, output);
				} else {
					statements[i].printStatement(indent+2, output);
				}
			}
		}
		output.append("\n"); //$NON-NLS-1$
		return printIndent(indent, output).append('}');
	}

	public void resolve(BlockScope upperScope) {
	
	    try {
			boolean isEnumSwitch = false;
			TypeBinding expressionType = expression.resolveType(upperScope);
			if (expressionType != null) {
				expression.computeConversion(upperScope, expressionType, expressionType);
				checkType: {
					if (!expressionType.isValidBinding()) {
						expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
						break checkType;
					} else if (expressionType.isBaseType()) {
						if (expression.isConstantValueOfTypeAssignableToType(expressionType, TypeBinding.INT))
							break checkType;
						if (expressionType.isCompatibleWith(TypeBinding.INT))
							break checkType;
					} else if (expressionType.isEnum()) {
						isEnumSwitch = true;
						break checkType;
					} else if (upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT)) {
						expression.computeConversion(upperScope, TypeBinding.INT, expressionType);
						break checkType;
					}
					upperScope.problemReporter().incorrectSwitchType(expression, expressionType);
					expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
				}
			}
			if (statements != null) {
				scope = /*explicitDeclarations == 0 ? upperScope : */new BlockScope(upperScope);
				int length;
				// collection of cases is too big but we will only iterate until caseCount
				cases = new CaseStatement[length = statements.length];
				this.constants = new int[length];
				CaseStatement[] duplicateCaseStatements = null;
				int duplicateCaseStatementsCounter = 0;
				int counter = 0;
				for (int i = 0; i < length; i++) {
					Constant constant;
					final Statement statement = statements[i];
					if ((constant = statement.resolveCase(scope, expressionType, this)) != Constant.NotAConstant) {
						int key = constant.intValue();
						//----check for duplicate case statement------------
						for (int j = 0; j < counter; j++) {
							if (this.constants[j] == key) {
								final CaseStatement currentCaseStatement = (CaseStatement) statement;
								if (duplicateCaseStatements == null) {
									scope.problemReporter().duplicateCase(cases[j]);
									scope.problemReporter().duplicateCase(currentCaseStatement);
									duplicateCaseStatements = new CaseStatement[length];
									duplicateCaseStatements[duplicateCaseStatementsCounter++] = cases[j];
									duplicateCaseStatements[duplicateCaseStatementsCounter++] = currentCaseStatement;
								} else {
									boolean found = false;
									searchReportedDuplicate: for (int k = 2; k < duplicateCaseStatementsCounter; k++) {
										if (duplicateCaseStatements[k] == statement) {
											found = true;
											break searchReportedDuplicate;
										}
									}
									if (!found) {
										scope.problemReporter().duplicateCase(currentCaseStatement);
										duplicateCaseStatements[duplicateCaseStatementsCounter++] = currentCaseStatement;
									}
								}
							}
						}
						this.constants[counter++] = key;
					}
				}
				if (length != counter) { // resize constants array
					System.arraycopy(this.constants, 0, this.constants = new int[counter], 0, counter);
				}
			} else {
				if ((this.bits & UndocumentedEmptyBlock) != 0) {
					upperScope.problemReporter().undocumentedEmptyBlock(this.blockStart, this.sourceEnd);
				}
			}
			// for enum switch, check if all constants are accounted for (if no default) 
			if (isEnumSwitch && defaultCase == null 
					&& upperScope.compilerOptions().getSeverity(CompilerOptions.IncompleteEnumSwitch) != ProblemSeverities.Ignore) {
				int constantCount = this.constants == null ? 0 : this.constants.length; // could be null if no case statement
				if (constantCount == caseCount // ignore diagnosis if unresolved constants
						&& caseCount != ((ReferenceBinding)expressionType).enumConstantCount()) {
					FieldBinding[] enumFields = ((ReferenceBinding)expressionType.erasure()).fields();
					for (int i = 0, max = enumFields.length; i <max; i++) {
						FieldBinding enumConstant = enumFields[i];
						if ((enumConstant.modifiers & ClassFileConstants.AccEnum) == 0) continue;
						findConstant : {
							for (int j = 0; j < caseCount; j++) {
								if ((enumConstant.id + 1) == this.constants[j]) // zero should not be returned see bug 141810
									break findConstant;
							}
							// enum constant did not get referenced from switch
							upperScope.problemReporter().missingEnumConstantCase(this, enumConstant);
						}
					}
				}
			}
	    } finally {
	        if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
	    }
	}

	public void traverse(
			ASTVisitor visitor,
			BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			expression.traverse(visitor, scope);
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
	
	/**
	 * Dispatch the call on its last statement.
	 */
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
}
