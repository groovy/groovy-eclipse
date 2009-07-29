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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ForStatement extends Statement {
	
	public Statement[] initializations;
	public Expression condition;
	public Statement[] increments;
	public Statement action;

	//when there is no local declaration, there is no need of a new scope
	//scope is positioned either to a new scope, or to the "upper"scope (see resolveType)
	public BlockScope scope;

	private BranchLabel breakLabel, continueLabel;

	// for local variables table attributes
	int preCondInitStateIndex = -1;
	int preIncrementsInitStateIndex = -1;
	int condIfTrueInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public ForStatement(
		Statement[] initializations,
		Expression condition,
		Statement[] increments,
		Statement action,
		boolean neededScope,
		int s,
		int e) {

		this.sourceStart = s;
		this.sourceEnd = e;
		this.initializations = initializations;
		this.condition = condition;
		this.increments = increments;
		this.action = action;
		// remember useful empty statement
		if (action instanceof EmptyStatement) action.bits |= ASTNode.IsUsefulEmptyStatement;
		if (neededScope) {
			this.bits |= ASTNode.NeededScope;
		}
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
			
		breakLabel = new BranchLabel();
		continueLabel = new BranchLabel();

		// process the initializations
		if (initializations != null) {
			for (int i = 0, count = initializations.length; i < count; i++) {
				flowInfo = initializations[i].analyseCode(scope, flowContext, flowInfo);
			}
		}
		preCondInitStateIndex =
			currentScope.methodScope().recordInitializationStates(flowInfo);

		Constant cst = this.condition == null ? null : this.condition.constant;
		boolean isConditionTrue = cst == null || (cst != Constant.NotAConstant && cst.booleanValue() == true);
		boolean isConditionFalse = cst != null && (cst != Constant.NotAConstant && cst.booleanValue() == false);

		cst = this.condition == null ? null : this.condition.optimizedBooleanConstant();
		boolean isConditionOptimizedTrue = cst == null ||  (cst != Constant.NotAConstant && cst.booleanValue() == true);
		boolean isConditionOptimizedFalse = cst != null && (cst != Constant.NotAConstant && cst.booleanValue() == false);
		
		// process the condition
		LoopingFlowContext condLoopContext = null;
		FlowInfo condInfo = flowInfo.nullInfoLessUnconditionalCopy();
		if (condition != null) {
			if (!isConditionTrue) {
				condInfo =
					condition.analyseCode(
						scope,
						(condLoopContext =
							new LoopingFlowContext(flowContext, flowInfo, this, null, 
								null, scope)),
						condInfo);
			}
		}

		// process the action
		LoopingFlowContext loopingContext;
		UnconditionalFlowInfo actionInfo;
		if (action == null 
			|| (action.isEmptyBlock() && currentScope.compilerOptions().complianceLevel <= ClassFileConstants.JDK1_3)) {
			if (condLoopContext != null)
				condLoopContext.complainOnDeferredFinalChecks(scope, condInfo);
			if (isConditionTrue) {
				if (condLoopContext != null) {
					condLoopContext.complainOnDeferredNullChecks(currentScope,
						condInfo);
				}
				return FlowInfo.DEAD_END;
			} else {
				if (isConditionFalse){
					continueLabel = null; // for(;false;p());
				}
				actionInfo = condInfo.initsWhenTrue().unconditionalCopy();
				loopingContext =
					new LoopingFlowContext(flowContext, flowInfo, this, 
						breakLabel, continueLabel, scope);
			}
		} 
		else {
			loopingContext =
				new LoopingFlowContext(flowContext, flowInfo, this, breakLabel, 
					continueLabel, scope);
			FlowInfo initsWhenTrue = condInfo.initsWhenTrue();
			condIfTrueInitStateIndex =
				currentScope.methodScope().recordInitializationStates(initsWhenTrue);

				if (isConditionFalse) {
					actionInfo = FlowInfo.DEAD_END;
				} else {
					actionInfo = initsWhenTrue.unconditionalCopy();
					if (isConditionOptimizedFalse){
						actionInfo.setReachMode(FlowInfo.UNREACHABLE);
					}
				}
			if (!this.action.complainIfUnreachable(actionInfo, scope, false)) {
				actionInfo = action.analyseCode(scope, loopingContext, actionInfo).
					unconditionalInits();
			}

			// code generation can be optimized when no need to continue in the loop
			if ((actionInfo.tagBits & 
					loopingContext.initsOnContinue.tagBits &
					FlowInfo.UNREACHABLE) != 0) {
				continueLabel = null;
			} 
			else {
				if (condLoopContext != null) {
					condLoopContext.complainOnDeferredFinalChecks(scope, 
							condInfo);
				}
				actionInfo = actionInfo.mergedWith(loopingContext.initsOnContinue);
				loopingContext.complainOnDeferredFinalChecks(scope, 
						actionInfo);
			}
		}
		// for increments
		FlowInfo exitBranch = flowInfo.copy();
		// recover null inits from before condition analysis
		LoopingFlowContext incrementContext = null;
		if (continueLabel != null) {
			if (increments != null) {
				incrementContext =
					new LoopingFlowContext(flowContext, flowInfo, this, null, 
						null, scope);
				FlowInfo incrementInfo = actionInfo;
				this.preIncrementsInitStateIndex =
					currentScope.methodScope().recordInitializationStates(incrementInfo);
				for (int i = 0, count = increments.length; i < count; i++) {
					incrementInfo = increments[i].
						analyseCode(scope, incrementContext, incrementInfo);
				}
				incrementContext.complainOnDeferredFinalChecks(scope,
						actionInfo = incrementInfo.unconditionalInits());
			}
			exitBranch.addPotentialInitializationsFrom(actionInfo).
				addInitializationsFrom(condInfo.initsWhenFalse());
		}
		else {
			exitBranch.addInitializationsFrom(condInfo.initsWhenFalse());
		}
		// nulls checks
		if (condLoopContext != null) {
			condLoopContext.complainOnDeferredNullChecks(currentScope, 
				actionInfo);
		}
		loopingContext.complainOnDeferredNullChecks(currentScope, 
			actionInfo);
		if (incrementContext != null) {
			incrementContext.complainOnDeferredNullChecks(currentScope, 
				actionInfo);
		}

		//end of loop
		FlowInfo mergedInfo = FlowInfo.mergedOptimizedBranches(
				(loopingContext.initsOnBreak.tagBits &
					FlowInfo.UNREACHABLE) != 0 ?
					loopingContext.initsOnBreak :
					flowInfo.addInitializationsFrom(loopingContext.initsOnBreak), // recover upstream null info
				isConditionOptimizedTrue, 
				exitBranch, 
				isConditionOptimizedFalse, 
				!isConditionTrue /*for(;;){}while(true); unreachable(); */);
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * For statement code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachable) == 0) {
			return;
		}
		int pc = codeStream.position;

		// generate the initializations
		if (initializations != null) {
			for (int i = 0, max = initializations.length; i < max; i++) {
				initializations[i].generateCode(scope, codeStream);
			}
		}
		Constant cst = this.condition == null ? null : this.condition.optimizedBooleanConstant();
		boolean isConditionOptimizedFalse = cst != null && (cst != Constant.NotAConstant && cst.booleanValue() == false);
		if (isConditionOptimizedFalse) {
			condition.generateCode(scope, codeStream, false);
			// May loose some local variable initializations : affecting the local variable attributes
			if ((this.bits & ASTNode.NeededScope) != 0) {
				codeStream.exitUserScope(scope);
			}
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		
		// label management
		BranchLabel actionLabel = new BranchLabel(codeStream);
		actionLabel.tagBits |= BranchLabel.USED;
		BranchLabel conditionLabel = new BranchLabel(codeStream);
		breakLabel.initialize(codeStream);
		if (this.continueLabel == null) {
			conditionLabel.place();
			if ((condition != null) && (condition.constant == Constant.NotAConstant)) {
				condition.generateOptimizedBoolean(scope, codeStream, null, breakLabel, true);
			}
		} else {
			this.continueLabel.initialize(codeStream);
			// jump over the actionBlock
			if ((condition != null)
				&& (condition.constant == Constant.NotAConstant)
				&& !((action == null || action.isEmptyBlock()) && (increments == null))) {
				conditionLabel.tagBits |= BranchLabel.USED;
				int jumpPC = codeStream.position;
				codeStream.goto_(conditionLabel);
				codeStream.recordPositionsFrom(jumpPC, condition.sourceStart);
			}
		}

		// generate the loop action
		if (action != null) {
			// Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
			if (condIfTrueInitStateIndex != -1) {
				// insert all locals initialized inside the condition into the action generated prior to the condition
				codeStream.addDefinitelyAssignedVariables(
					currentScope,
					condIfTrueInitStateIndex);
			}
			actionLabel.place();
			action.generateCode(scope, codeStream);
		} else {
			actionLabel.place();
		}
		if (this.preIncrementsInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preIncrementsInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.preIncrementsInitStateIndex);
		}
		// continuation point
		if (continueLabel != null) {
			continueLabel.place();
			// generate the increments for next iteration
			if (increments != null) {
				for (int i = 0, max = increments.length; i < max; i++) {
					increments[i].generateCode(scope, codeStream);
				}
			}
			// May loose some local variable initializations : affecting the local variable attributes
			if (preCondInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, preCondInitStateIndex);
			}		
			// generate the condition
			conditionLabel.place();
			if ((condition != null) && (condition.constant == Constant.NotAConstant)) {
				condition.generateOptimizedBoolean(scope, codeStream, actionLabel, null, true);
			} else {
				codeStream.goto_(actionLabel);
			}
			
		} else {
			// May loose some local variable initializations : affecting the local variable attributes
			if (preCondInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, preCondInitStateIndex);
			}
		}


		// May loose some local variable initializations : affecting the local variable attributes
		if ((this.bits & ASTNode.NeededScope) != 0) {
			codeStream.exitUserScope(scope);
		}
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		breakLabel.place();
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("for ("); //$NON-NLS-1$
		//inits
		if (initializations != null) {
			for (int i = 0; i < initializations.length; i++) {
				//nice only with expressions
				if (i > 0) output.append(", "); //$NON-NLS-1$
				initializations[i].print(0, output);
			}
		}
		output.append("; "); //$NON-NLS-1$
		//cond
		if (condition != null) condition.printExpression(0, output);
		output.append("; "); //$NON-NLS-1$
		//updates
		if (increments != null) {
			for (int i = 0; i < increments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				increments[i].print(0, output);
			}
		}
		output.append(") "); //$NON-NLS-1$
		//block
		if (action == null)
			output.append(';');
		else {
			output.append('\n');
			action.printStatement(tab + 1, output);
		}
		return output;
	}

	public void resolve(BlockScope upperScope) {

		// use the scope that will hold the init declarations
		scope = (this.bits & ASTNode.NeededScope) != 0 ? new BlockScope(upperScope) : upperScope;
		if (initializations != null)
			for (int i = 0, length = initializations.length; i < length; i++)
				initializations[i].resolve(scope);
		if (condition != null) {
			TypeBinding type = condition.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
			condition.computeConversion(scope, type, type);
		}
		if (increments != null)
			for (int i = 0, length = increments.length; i < length; i++)
				increments[i].resolve(scope);
		if (action != null)
			action.resolve(scope);
	}
	
	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			if (initializations != null) {
				int initializationsLength = initializations.length;
				for (int i = 0; i < initializationsLength; i++)
					initializations[i].traverse(visitor, scope);
			}

			if (condition != null)
				condition.traverse(visitor, scope);

			if (increments != null) {
				int incrementsLength = increments.length;
				for (int i = 0; i < incrementsLength; i++)
					increments[i].traverse(visitor, scope);
			}

			if (action != null)
				action.traverse(visitor, scope);
		}
		visitor.endVisit(this, blockScope);
	}
}
