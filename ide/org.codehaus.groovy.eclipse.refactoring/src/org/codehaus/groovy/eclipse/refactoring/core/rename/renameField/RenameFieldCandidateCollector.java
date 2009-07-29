/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameField;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;

/**
 * collects possible candidates for a rename field refactoring
 * @author reto kleeb
 *
 */
public class RenameFieldCandidateCollector extends RefactoringCodeVisitorSupport {

	private final FieldPattern patternOfField;
	private final List<ASTNode> candidates = new ArrayList<ASTNode>();
//	private List<ConstantExpression> visitedConstantExpressions = new ArrayList<ConstantExpression>();

	public RenameFieldCandidateCollector(ModuleNode rootNode, FieldPattern patternOfField) {
		super(rootNode);
		this.patternOfField = patternOfField;
	}

	@Override
    public void visitField(FieldNode node) {
		super.visitField(node);
		FieldPattern candidatePattern = new FieldPattern(node);
		if (patternOfField.equalsName(candidatePattern)) {
			candidates.add(node);
		}
	}

	@Override
    public void visitVariableExpression(VariableExpression expression) {
		super.visitVariableExpression(expression);
		Variable accessedVariable = expression.getAccessedVariable();
		FieldPattern candidatePattern;
		if (accessedVariable instanceof FieldNode) {
			candidatePattern = new FieldPattern((FieldNode) accessedVariable);
			if (patternOfField.equalsName(candidatePattern)) {
				candidates.add(expression);
			}
		} else if (accessedVariable instanceof PropertyNode) {
			candidatePattern = new FieldPattern(((PropertyNode) accessedVariable).getField());
			if (patternOfField.equalsName(candidatePattern)) {
				candidates.add(expression);
			}
		}
	}

	@Override
    public void visitExpressionStatement(ExpressionStatement statement) {
		super.visitExpressionStatement(statement);
		ExpressionStatement exprStatement = statement;
		Expression expression = exprStatement.getExpression();
		if (expression instanceof PropertyExpression) {
			PropertyExpression propExpr = (PropertyExpression) expression;
			handlePropertyExpression(propExpr);
		}
	}
	
	@Override
    public void visitFieldExpression(FieldExpression expression) {
		super.visitFieldExpression(expression);
		if (patternOfField.equalsName(new FieldPattern(expression.getField()))) {
			candidates.add(expression);
		}
	}
	
/*	public void visitConstantExpression(ConstantExpression expression) {
		super.visitConstantExpression(expression);
//		boolean notVisitedYet = !visitedConstantExpressions.contains(expression);
		boolean sameNameAsPattern = patternOfField.getName().equals(expression.getText());
		if(sameNameAsPattern && notVisitedYet){
			candidates.add(expression);
//			visitedConstantExpressions.add(expression);
		}
	}
*/
	
	@Override
    public void visitAttributeExpression(AttributeExpression expression) {
		super.visitAttributeExpression(expression);
		Expression objExpression = expression.getObjectExpression();
		if(objExpression instanceof VariableExpression){
			VariableExpression variExp = (VariableExpression) objExpression;
			FieldPattern pattern = new FieldPattern(variExp.getType(),expression.getType(),expression.getPropertyAsString());
			if (patternOfField.equalsName(pattern)) {
				candidates.add(expression);
			}
		}
	}

	@Override
    public void visitPropertyExpression(PropertyExpression expression) {
		super.visitPropertyExpression(expression);
		if (expression.getProperty() instanceof ConstantExpression){
//			if(!visitedConstantExpressions.contains((ConstantExpression)expression.getProperty())){
				handlePropertyExpression(expression);
				
//			}
		}
	}

	private void handlePropertyExpression(PropertyExpression expression) {
		FieldPattern pattern = new FieldPattern(expression.getObjectExpression().getType(),
				expression.getType(), expression.getPropertyAsString());
		if (patternOfField.equalsName(pattern) /*&& !visitedConstantExpressions.contains((ConstantExpression)expression.getProperty())*/) {
//			visitedConstantExpressions.add((ConstantExpression)expression.getProperty());
			candidates.add(expression);
		}
	}

	public List<ASTNode> getCandidates() {
		return candidates;
	}

}
