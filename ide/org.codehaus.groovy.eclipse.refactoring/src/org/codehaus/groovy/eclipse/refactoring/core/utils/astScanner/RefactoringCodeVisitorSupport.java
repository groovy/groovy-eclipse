/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

public abstract class RefactoringCodeVisitorSupport extends AbstractRefactoringCodeVisitor {

	protected ModuleNode rootNode;

	public RefactoringCodeVisitorSupport(ModuleNode rootNode) {
		this.rootNode = rootNode;
	}

	@Override
    public void visitClassImport(ClassImport classImport) {
	}

	protected void analyzeNode(ASTNode node) {
	}

	protected void clear(ASTNode node) {
	}

	@Override
    public void visitStaticClassImport(StaticClassImport staticClassImport) {
	}

	@Override
    public void visitStaticFieldImport(StaticFieldImport staticAliasImport) {
	}

	@Override
    public void scanAST() {
		analyseClassImports();
		analyseStaticClassImport();
		analyseStaticFieldImport();

		if (!rootNode.getStatementBlock().isEmpty()) {
			for (Statement statement : (Iterable<Statement>) rootNode.getStatementBlock().getStatements()) {
				statement.visit(this);
			}
		}
		List<ClassNode> classes = rootNode.getClasses();
		for (ClassNode classNode : classes) {
			if (!classNode.isScript()) {
				visitClass(classNode);
			} else {
				List<MethodNode> methods = rootNode.getMethods();
				for ( MethodNode method : methods) {
					visitMethod(method);
				}
			}
		}
	}

	private void analyseStaticFieldImport() {
		//visit static imports like import java.lang.Math.cos

        for (Entry<String, ImportNode> aliasOrField : rootNode.getStaticImports().entrySet()) {
            StaticFieldImport staticAliasImport = new StaticFieldImport(aliasOrField.getValue().getType(), aliasOrField.getKey(),
                    aliasOrField.getValue().getFieldName());
            staticAliasImport.setSourcePosition(aliasOrField.getValue());
			visitStaticFieldImport(staticAliasImport);
		}
	}

	private void analyseStaticClassImport() {
		//visit static imports like import java.lang.Math.*
        Collection<ImportNode> staticImportClasses = rootNode.getStaticImports().values();

		for (ImportNode staticImp : staticImportClasses) {
            ClassNode type = staticImp.getType();
            StaticClassImport staticClassImport = new StaticClassImport(type);
            staticClassImport.setSourcePosition(type);
			visitStaticClassImport(staticClassImport);
		}
	}

	private void analyseClassImports() {
		//visit imports like import java.io.File and import java.io.File as MyFile

		List<ImportNode> imports = rootNode.getImports();
		for(ImportNode importNode : imports) {
			ClassImport classImport = new ClassImport(importNode);
			classImport.setSourcePosition(importNode.getType());
			visitClassImport(classImport);
		}
	}

	protected void analyzeNodes(ASTNode[] nodes) {
        if (nodes != null) {
        	for(int i = 0; i < nodes.length; i++){
        		analyzeNode(nodes[i]);
        		clear(nodes[i]);
        	}
        }
	}

	protected void analyzeTypes(ClassNode[] classNodes) {
        if (classNodes != null) {
        	for(int i = 0; i < classNodes.length; i++){
        	    if (classNodes[i] != null) {
            		analyzeType(classNodes[i]);
            		clear(classNodes[i]);
        	    }
        	}
        }
	}

	private void analyzeTypeInternal(ClassNode classNode) {
	    ClassNode node = classNode;
		//visit all classNodes for arrays
    	while (node.isArray()) {
    		node = node.getComponentType();
    	}
    	analyzeNode(node);
    	analyzeGenerics(node);
	}

	@Override
    public void analyzeType(ClassNode node) {
		analyzeTypeInternal(node);
	}

	@Override
    public void analyzeParameter(Parameter parameter) {
		analyzeNode(parameter);
	}

	protected void analyzeGenerics(ClassNode node) {
		if (node.getGenericsTypes() != null) {
			GenericsType[] generics = node.getGenericsTypes();
			for (int i = 0; i < generics.length; i++) {
				GenericsType genericType = generics[i];

				// bottoms out recursion when a type parameter refers to itself, eg- java.lang.Enum
				if (! node.getName().equals(genericType.getType().getName())) {
    				analyzeType(genericType.getType());
    				clear(genericType.getType());
    				if (genericType.getLowerBound() != null) {
    					analyzeType(genericType.getLowerBound());
    					clear(genericType.getLowerBound());
    				}
    				if (genericType.getUpperBounds() != null) {
        				ClassNode[] upperBounds = genericType.getUpperBounds().clone();
        				// prevent recursion by nulling out duplicates
        				for (int j = 0; j < upperBounds.length; j++) {
                            if (upperBounds[i].getName().equals(node.getName())) {
                                upperBounds[i] = null;
                            }
                        }
                        analyzeTypes(upperBounds);
    				}
				}
			}
		}
	}

	protected void analyseParameters(Parameter[] parameters) {
		if(parameters != null) {
			for(Parameter parameter : parameters) {
	        	analyzeParameter(parameter);
	        	analyzeType(parameter.getOriginType());
	        	clear(parameter.getOriginType());
	            if (parameter.hasInitialExpression()) {
	            	parameter.getInitialExpression().visit(this);
	            }
	            clear(parameter);

			}
		}
	}

    public void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotionMap = node.getAnnotations();
        if (annotionMap.isEmpty()) return;

        Iterator<AnnotationNode> it = annotionMap.iterator();
        while (it.hasNext()) {
            AnnotationNode an = (AnnotationNode) it.next();
            //skip builtin properties
            if (an.isBuiltIn()) continue;
            for (Entry<String, Expression> element : (Iterable<Entry<String, Expression>>) an.getMembers().entrySet()) {
                element.getValue().visit(this);
            }
        }
    }

	public void visitClass(ClassNode node) {
		analyzeTypeInternal(node);
		analyzeType(node.getUnresolvedSuperClass());
		clear(node.getUnresolvedSuperClass());
		analyzeTypes(node.getInterfaces());
		node.visitContents(this);
		clear(node);
	}

	public void visitField(FieldNode node) {
		analyzeNode(node);
		analyzeType(node.getOriginType());
        clear(node.getOriginType());
        Expression initExp = node.getInitialValueExpression();
        if (initExp != null) {
            analyzeNode(initExp);
            initExp.visit(this);
            clear(initExp);
        }
		clear(node);
	}

    protected void visitClassCodeContainer(Statement code) {
        if (code != null) code.visit(this);
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        visitAnnotations(node);
        analyseMethodHead(node);
        Statement code = node.getCode();

        visitClassCodeContainer(code);
    }

	public void visitConstructor(ConstructorNode node) {
		analyzeNode(node);
		visitConstructorOrMethod(node, true);
		clear(node);
	}

	public void visitMethod(MethodNode node) {
		analyzeNode(node);
		visitConstructorOrMethod(node, false);
	    clear(node);
	}

	private void analyseMethodHead(MethodNode node) {
		analyzeType(node.getReturnType());
		clear(node.getReturnType());
        analyseParameters(node.getParameters());
        analyzeTypes(node.getExceptions());
	}

	public void visitProperty(PropertyNode node) {

	}

	@Override
	public void visitVariableExpression(VariableExpression expression) {
		analyzeNode(expression);
		analyzeType(expression.getOriginType());
		clear(expression.getOriginType());
		super.visitVariableExpression(expression);
		clear(expression);
	}

	@Override
	public void visitClosureExpression(ClosureExpression expression) {
		analyzeNode(expression);
		analyseParameters(expression.getParameters());
		super.visitClosureExpression(expression);
		clear(expression);
	}

	@Override
	public void visitArgumentlistExpression(ArgumentListExpression ale) {
		analyzeNode(ale);
		super.visitArgumentlistExpression(ale);
		clear(ale);
	}

	@Override
	public void visitArrayExpression(ArrayExpression expression) {
		analyzeNode(expression);
		analyzeType(expression.getElementType());
		clear(expression.getElementType());
		super.visitArrayExpression(expression);
		clear(expression);
	}

	@Override
	public void visitAssertStatement(AssertStatement statement) {
		analyzeNode(statement);
		super.visitAssertStatement(statement);
		clear(statement);
	}

	@Override
	public void visitAttributeExpression(AttributeExpression expression) {
		analyzeNode(expression);
		super.visitAttributeExpression(expression);
		clear(expression);
	}

	@Override
	public void visitBinaryExpression(BinaryExpression expression) {
		analyzeNode(expression);
		super.visitBinaryExpression(expression);
		clear(expression);
	}

	@Override
	public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
		analyzeNode(expression);
		super.visitBitwiseNegationExpression(expression);
		clear(expression);
	}

	@Override
	public void visitBlockStatement(BlockStatement block) {
		analyzeNode(block);
		super.visitBlockStatement(block);
		clear(block);
	}

	@Override
	public void visitBooleanExpression(BooleanExpression expression) {
		analyzeNode(expression);
		super.visitBooleanExpression(expression);
		clear(expression);
	}

	@Override
	public void visitBreakStatement(BreakStatement statement) {
		analyzeNode(statement);
		super.visitBreakStatement(statement);
		clear(statement);
	}

//	@Override
//	public void visitBytecodeExpression(BytecodeExpression cle) {
//		analyzeNode(cle);
//		super.visitBytecodeExpression(cle);
//		clear(cle);
//	}

	@Override
	public void visitCaseStatement(CaseStatement statement) {
		analyzeNode(statement);
		super.visitCaseStatement(statement);
		clear(statement);
	}

	@Override
	public void visitCastExpression(CastExpression expression) {
		analyzeNode(expression);
		analyzeType(expression.getType());
		clear(expression.getType());
		super.visitCastExpression(expression);
		clear(expression);
	}

	@Override
	public void visitCatchStatement(CatchStatement statement) {
		analyzeNode(statement);
		analyzeType(statement.getExceptionType());
		clear(statement.getExceptionType());
		super.visitCatchStatement(statement);
		clear(statement);
	}

	@Override
	public void visitClassExpression(ClassExpression expression) {
		analyzeNode(expression);
		super.visitClassExpression(expression);
		clear(expression);
	}

	@Override
	public void visitClosureListExpression(ClosureListExpression cle) {
		analyzeNode(cle);
		super.visitClosureListExpression(cle);
		clear(cle);
	}

	@Override
	public void visitConstantExpression(ConstantExpression expression) {
		analyzeNode(expression);
		super.visitConstantExpression(expression);
		clear(expression);
	}

	@Override
	public void visitConstructorCallExpression(ConstructorCallExpression call) {
		analyzeNode(call);
		analyzeType(call.getType());
		clear(call.getType());
		super.visitConstructorCallExpression(call);
		clear(call);
	}

	@Override
	public void visitContinueStatement(ContinueStatement statement) {
		analyzeNode(statement);
		super.visitContinueStatement(statement);
		clear(statement);
	}

	@Override
	public void visitDeclarationExpression(DeclarationExpression expression) {
		//Do not analyse, since it is analysed in visitBinaryExpression
		//analyzeNode(expression);
		super.visitDeclarationExpression(expression);
		//clear(expression);
	}

	@Override
	public void visitDoWhileLoop(DoWhileStatement loop) {
		analyzeNode(loop);
		super.visitDoWhileLoop(loop);
		clear(loop);
	}

	@Override
	public void visitExpressionStatement(ExpressionStatement statement) {
		analyzeNode(statement);
		super.visitExpressionStatement(statement);
		clear(statement);
	}

	@Override
	public void visitFieldExpression(FieldExpression expression) {
		if (!expression.getType().getNameWithoutPackage().equals("MetaClass")) {
			analyzeNode(expression);
			super.visitFieldExpression(expression);
			clear(expression);
		}
	}

	@Override
	public void visitForLoop(ForStatement forLoop) {
		analyzeNode(forLoop);
		super.visitForLoop(forLoop);
		clear(forLoop);
	}

	@Override
	public void visitGStringExpression(GStringExpression expression) {
		analyzeNode(expression);
		super.visitGStringExpression(expression);
		clear(expression);
	}

	@Override
	public void visitIfElse(IfStatement ifElse) {
		analyzeNode(ifElse);
		super.visitIfElse(ifElse);
		clear(ifElse);
	}

	@Override
	public void visitListExpression(ListExpression expression) {
		analyzeNode(expression);
		super.visitListExpression(expression);
		clear(expression);
	}

	@Override
	public void visitMapEntryExpression(MapEntryExpression expression) {
		analyzeNode(expression);
		super.visitMapEntryExpression(expression);
		clear(expression);
	}

	@Override
	public void visitMapExpression(MapExpression expression) {
		analyzeNode(expression);
		super.visitMapExpression(expression);
		clear(expression);
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression call) {
		analyzeNode(call);
		super.visitMethodCallExpression(call);
		clear(call);
	}

	@Override
	public void visitMethodPointerExpression(MethodPointerExpression expression) {
		analyzeNode(expression);
		super.visitMethodPointerExpression(expression);
		clear(expression);
	}

	@Override
	public void visitNotExpression(NotExpression expression) {
		analyzeNode(expression);
		super.visitNotExpression(expression);
		clear(expression);
	}

	@Override
	public void visitPostfixExpression(PostfixExpression expression) {
		analyzeNode(expression);
		super.visitPostfixExpression(expression);
		clear(expression);
	}

	@Override
	public void visitPrefixExpression(PrefixExpression expression) {
		analyzeNode(expression);
		super.visitPrefixExpression(expression);
		clear(expression);
	}

	@Override
	public void visitPropertyExpression(PropertyExpression expression) {
		analyzeNode(expression);
		super.visitPropertyExpression(expression);
		clear(expression);
	}

	@Override
	public void visitRangeExpression(RangeExpression expression) {
		analyzeNode(expression);
		super.visitRangeExpression(expression);
		clear(expression);
	}

	@Override
	public void visitReturnStatement(ReturnStatement statement) {
		analyzeNode(statement);
		super.visitReturnStatement(statement);
		clear(statement);
	}

	@Override
	public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
		analyzeNode(expression);
		super.visitShortTernaryExpression(expression);
		clear(expression);
	}

	@Override
	public void visitSpreadExpression(SpreadExpression expression) {
		analyzeNode(expression);
		super.visitSpreadExpression(expression);
		clear(expression);
	}

	@Override
	public void visitSpreadMapExpression(SpreadMapExpression expression) {
		analyzeNode(expression);
		super.visitSpreadMapExpression(expression);
		clear(expression);
	}

	@Override
	public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
		analyzeNode(call);
		super.visitStaticMethodCallExpression(call);
		clear(call);
	}

	@Override
	public void visitSwitch(SwitchStatement statement) {
		analyzeNode(statement);
		super.visitSwitch(statement);
		clear(statement);
	}

	@Override
	public void visitSynchronizedStatement(SynchronizedStatement statement) {
		analyzeNode(statement);
		super.visitSynchronizedStatement(statement);
		clear(statement);
	}

	@Override
	public void visitTernaryExpression(TernaryExpression expression) {
		analyzeNode(expression);
		super.visitTernaryExpression(expression);
		clear(expression);
	}

	@Override
	public void visitThrowStatement(ThrowStatement statement) {
		analyzeNode(statement);
		super.visitThrowStatement(statement);
		clear(statement);
	}

	@Override
	public void visitTryCatchFinally(TryCatchStatement statement) {
		analyzeNode(statement);
		super.visitTryCatchFinally(statement);
		clear(statement);
	}

	@Override
	public void visitTupleExpression(TupleExpression expression) {
//		do nothing here, TupleExpression is visited in ArgumentListExpression

//		analyzeNode(expression);
		super.visitTupleExpression(expression);
//		clear(expression);
	}

	@Override
	public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
		analyzeNode(expression);
		super.visitUnaryMinusExpression(expression);
		clear(expression);
	}

	@Override
	public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
		analyzeNode(expression);
		super.visitUnaryPlusExpression(expression);
		clear(expression);
	}

	@Override
	public void visitWhileLoop(WhileStatement loop) {
		analyzeNode(loop);
		super.visitWhileLoop(loop);
		clear(loop);
	}
}
