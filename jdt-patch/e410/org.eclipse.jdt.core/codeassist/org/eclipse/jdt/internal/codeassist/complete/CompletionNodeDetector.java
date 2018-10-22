/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Detect the presence of a node in expression
 */
public class CompletionNodeDetector extends ASTVisitor {
	private ASTNode searchedNode;
	private ASTNode parent;
	private boolean result;

	public CompletionNodeDetector(ASTNode searchedNode, ASTNode visitedAst){
		this.searchedNode = searchedNode;
		this.result = false;

		if(searchedNode != null && visitedAst != null) {
			visitedAst.traverse(this, null);
		}
	}

	public boolean containsCompletionNode() {
		return this.result;
	}

	public ASTNode getCompletionNodeParent() {
		return this.parent;
	}
	@Override
	public void endVisit(AllocationExpression allocationExpression, BlockScope scope) {
		endVisit(allocationExpression);
	}
	@Override
	public void endVisit(AND_AND_Expression and_and_Expression, BlockScope scope) {
		endVisit(and_and_Expression);
	}
	@Override
	public void endVisit(ArrayAllocationExpression arrayAllocationExpression, BlockScope scope) {
		endVisit(arrayAllocationExpression);
	}
	@Override
	public void endVisit(ArrayInitializer arrayInitializer, BlockScope scope) {
		endVisit(arrayInitializer);
	}
	@Override
	public void endVisit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, BlockScope scope) {
		endVisit(arrayQualifiedTypeReference);
	}
	@Override
	public void endVisit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, ClassScope scope) {
		endVisit(arrayQualifiedTypeReference);
	}
	@Override
	public void endVisit(ArrayReference arrayReference, BlockScope scope) {
		endVisit(arrayReference);
	}
	@Override
	public void endVisit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		endVisit(arrayTypeReference);
	}
	@Override
	public void endVisit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		endVisit(arrayTypeReference);
	}
	@Override
	public void endVisit(Assignment assignment, BlockScope scope) {
		endVisit(assignment);
	}
	@Override
	public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
		endVisit(binaryExpression);
	}
	@Override
	public void endVisit(CastExpression castExpression, BlockScope scope) {
		endVisit(castExpression);
	}
	@Override
	public void endVisit(CompoundAssignment compoundAssignment, BlockScope scope) {
		endVisit(compoundAssignment);
	}
	@Override
	public void endVisit(ConditionalExpression conditionalExpression, BlockScope scope) {
		endVisit(conditionalExpression);
	}
	@Override
	public void endVisit(EqualExpression equalExpression, BlockScope scope) {
		endVisit(equalExpression);
	}
	@Override
	public void endVisit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
		endVisit(explicitConstructor);
	}
	@Override
	public void endVisit(FieldReference fieldReference, BlockScope scope) {
		endVisit(fieldReference);
	}
	@Override
	public void endVisit(InstanceOfExpression instanceOfExpression, BlockScope scope) {
		endVisit(instanceOfExpression);
	}
	@Override
	public void endVisit(MessageSend messageSend, BlockScope scope) {
		endVisit(messageSend);
	}
	@Override
	public void endVisit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		endVisit(or_or_Expression);
	}
	@Override
	public void endVisit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, BlockScope scope) {
		endVisit(parameterizedQualifiedTypeReference);
	}
	@Override
	public void endVisit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, ClassScope scope) {
		endVisit(parameterizedQualifiedTypeReference);
	}
	@Override
	public void endVisit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, BlockScope scope) {
		endVisit(parameterizedSingleTypeReference);
	}
	@Override
	public void endVisit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, ClassScope scope) {
		endVisit(parameterizedSingleTypeReference);
	}
	@Override
	public void endVisit(PostfixExpression postfixExpression, BlockScope scope) {
		endVisit(postfixExpression);
	}
	@Override
	public void endVisit(PrefixExpression prefixExpression, BlockScope scope) {
		endVisit(prefixExpression);
	}
	@Override
	public void endVisit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope) {
		endVisit(qualifiedAllocationExpression);
	}
	@Override
	public void endVisit(QualifiedNameReference qualifiedNameReference, BlockScope scope) {
		endVisit(qualifiedNameReference);
	}
	@Override
	public void endVisit(QualifiedSuperReference qualifiedSuperReference, BlockScope scope) {
		endVisit(qualifiedSuperReference);
	}
	@Override
	public void endVisit(QualifiedThisReference qualifiedThisReference, BlockScope scope) {
		endVisit(qualifiedThisReference);
	}
	@Override
	public void endVisit(QualifiedTypeReference qualifiedTypeReference, BlockScope scope) {
		endVisit(qualifiedTypeReference);
	}
	@Override
	public void endVisit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope) {
		endVisit(qualifiedTypeReference);
	}
	@Override
	public void endVisit(ReferenceExpression referenceExpression, BlockScope blockScope) {
		endVisit(referenceExpression);	
	}
	@Override
	public void endVisit(SingleNameReference singleNameReference, BlockScope scope) {
		endVisit(singleNameReference);
	}
	@Override
	public void endVisit(SingleTypeReference singleTypeReference, BlockScope scope) {
		endVisit(singleTypeReference);
	}
	@Override
	public void endVisit(SingleTypeReference singleTypeReference, ClassScope scope) {
		endVisit(singleTypeReference);
	}
	@Override
	public void endVisit(SuperReference superReference, BlockScope scope) {
		endVisit(superReference);
	}
	@Override
	public void endVisit(ThisReference thisReference, BlockScope scope) {
		endVisit(thisReference);
	}
	@Override
	public void endVisit(UnaryExpression unaryExpression, BlockScope scope) {
		endVisit(unaryExpression);
	}
	@Override
	public void endVisit(MemberValuePair pair, BlockScope scope) {
		endVisit(pair);
	}
	public void endVisit(MemberValuePair pair, CompilationUnitScope scope) {
		endVisit(pair);
	}
	@Override
	public void endVisit(LambdaExpression lambda, BlockScope scope) {
		endVisit(lambda);
	}
	@Override
	public boolean visit(AllocationExpression allocationExpression, BlockScope scope) {
		return this.visit(allocationExpression);
	}
	@Override
	public boolean visit(AND_AND_Expression and_and_Expression, BlockScope scope) {
		return this.visit(and_and_Expression);
	}
	@Override
	public boolean visit(ArrayAllocationExpression arrayAllocationExpression, BlockScope scope) {
		return this.visit(arrayAllocationExpression);
	}
	@Override
	public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
		return this.visit(arrayInitializer);
	}
	@Override
	public boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, BlockScope scope) {
		return this.visit(arrayQualifiedTypeReference);
	}
	@Override
	public boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, ClassScope scope) {
		return this.visit(arrayQualifiedTypeReference);
	}
	@Override
	public boolean visit(ArrayReference arrayReference, BlockScope scope) {
		return this.visit(arrayReference);
	}
	@Override
	public boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		return this.visit(arrayTypeReference);
	}
	@Override
	public boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		return this.visit(arrayTypeReference);
	}
	@Override
	public boolean visit(Assignment assignment, BlockScope scope) {
		return this.visit(assignment);
	}
	@Override
	public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
		return this.visit(binaryExpression);
	}
	@Override
	public boolean visit(CastExpression castExpression, BlockScope scope) {
		return this.visit(castExpression);
	}
	@Override
	public boolean visit(CompoundAssignment compoundAssignment, BlockScope scope) {
		return this.visit(compoundAssignment);
	}
	@Override
	public boolean visit(ConditionalExpression conditionalExpression, BlockScope scope) {
		return this.visit(conditionalExpression);
	}
	@Override
	public boolean visit(EqualExpression equalExpression, BlockScope scope) {
		return this.visit(equalExpression);
	}
	@Override
	public boolean visit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
		return this.visit(explicitConstructor);
	}
	@Override
	public boolean visit(FieldReference fieldReference, BlockScope scope) {
		return this.visit(fieldReference);
	}
	@Override
	public boolean visit(InstanceOfExpression instanceOfExpression, BlockScope scope) {
		return this.visit(instanceOfExpression);
	}
	@Override
	public boolean visit(MessageSend messageSend, BlockScope scope) {
		return this.visit(messageSend);
	}
	@Override
	public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		return this.visit(or_or_Expression);
	}
	@Override
	public boolean visit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, BlockScope scope) {
		return this.visit(parameterizedQualifiedTypeReference);
	}
	@Override
	public boolean visit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, ClassScope scope) {
		return this.visit(parameterizedQualifiedTypeReference);
	}
	@Override
	public boolean visit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, BlockScope scope) {
		return this.visit(parameterizedSingleTypeReference);
	}
	@Override
	public boolean visit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, ClassScope scope) {
		return this.visit(parameterizedSingleTypeReference);
	}
	@Override
	public boolean visit(PostfixExpression postfixExpression, BlockScope scope) {
		return this.visit(postfixExpression);
	}
	@Override
	public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
		return this.visit(prefixExpression);
	}
	@Override
	public boolean visit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope) {
		return this.visit(qualifiedAllocationExpression);
	}
	@Override
	public boolean visit(QualifiedNameReference qualifiedNameReference, BlockScope scope) {
		return this.visit(qualifiedNameReference);
	}
	@Override
	public boolean visit(QualifiedSuperReference qualifiedSuperReference, BlockScope scope) {
		return this.visit(qualifiedSuperReference);
	}
	@Override
	public boolean visit(QualifiedThisReference qualifiedThisReference, BlockScope scope) {
		return this.visit(qualifiedThisReference);
	}
	@Override
	public boolean visit(QualifiedTypeReference qualifiedTypeReference, BlockScope scope) {
		return this.visit(qualifiedTypeReference);
	}
	@Override
	public boolean visit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope) {
		return this.visit(qualifiedTypeReference);
	}
	@Override
	public boolean visit(ReferenceExpression referenceExpression, BlockScope blockScope) {
		return this.visit(referenceExpression);
	}
	@Override
	public boolean visit(SingleNameReference singleNameReference, BlockScope scope) {
		return this.visit(singleNameReference);
	}
	@Override
	public boolean visit(SingleTypeReference singleTypeReference, BlockScope scope) {
		return this.visit(singleTypeReference);
	}
	@Override
	public boolean visit(SingleTypeReference singleTypeReference, ClassScope scope) {
		return this.visit(singleTypeReference);
	}
	@Override
	public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
		return this.visit(stringLiteral);
	}
	@Override
	public boolean visit(SuperReference superReference, BlockScope scope) {
		return this.visit(superReference);
	}
	@Override
	public boolean visit(ThisReference thisReference, BlockScope scope) {
		return this.visit(thisReference);
	}
	@Override
	public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {
		return this.visit(unaryExpression);
	}
	@Override
	public boolean visit(MemberValuePair pair, BlockScope scope) {
		return this.visit(pair);
	}
	public boolean visit(MemberValuePair pair, CompilationUnitScope scope) {
		return this.visit(pair);
	}
	private void endVisit(ASTNode astNode) {
		if(this.result && this.parent == null && astNode != this.searchedNode) {
			if(!(astNode instanceof AllocationExpression && ((AllocationExpression) astNode).type == this.searchedNode)
				&& !(astNode instanceof ConditionalExpression && ((ConditionalExpression) astNode).valueIfTrue == this.searchedNode)
				&& !(astNode instanceof ConditionalExpression && ((ConditionalExpression) astNode).valueIfFalse == this.searchedNode)) {
				this.parent = astNode;
			}
		}
	}
	private boolean visit(ASTNode astNode) {
		if(astNode == this.searchedNode) {
			this.result = true;
		}
		return !this.result;
	}
}
