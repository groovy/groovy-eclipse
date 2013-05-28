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
package org.codehaus.groovy.eclipse.refactoring.core.rewriter;

import groovyjarjarasm.asm.Opcodes;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
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
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
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
import org.codehaus.groovy.ast.stmt.EmptyStatement;
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
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.utils.FilePartReader;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ImportResolver;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * @author martin kempf
 * @author reto kleeb
 */
public class ASTWriter extends CodeVisitorSupport implements
		GroovyClassVisitor, Opcodes {

	protected StringBuilder groovyCode;
	//protected PrintStream groovyCode;
	private String lineDelimiter;
	//private ByteArrayOutputStream buffer;
	protected Stack<ASTNode> nodeStack = new Stack<ASTNode>();
	private final ModuleNode root;
	private int lineOfPreviousNode = 1;
	private int lineOfCurrentNode = 1;
	private int columnOffset = 0;
	private int caseCount = 0;  //to know which case is the first one in switch
	private boolean inElseBlock = false;
	private final IDocument currentDocument;  // might be null
	private int lineOffset = 0;
	private boolean explizitModifier = false;
	private String modifier = "";
	private int linesSinceFirstAnnotation = 0; //to know the startline of a method without the annotations
	private DeclarationExpression previousDeclaration = null;

	/**
	 *
	 * @param lineOffset set a startposition for the rewritten Code
	 * @param currentDocument SourceCode, the AST is generated from
	 */
	public ASTWriter(ModuleNode root, int lineOffset, IDocument currentDocument) {
		this(root,currentDocument);
		setLineOffset(lineOffset);
	}

	/**
	 *
	 * @param currentDocument SourceCode, the AST is generated from
	 */
	public ASTWriter(ModuleNode root, IDocument currentDocument) {
		groovyCode = new StringBuilder();
		try {
		    if (currentDocument != null) {
		        lineDelimiter = currentDocument.getLineDelimiter(0);
		    }
		} catch (BadLocationException e) {
		    GroovyCore.logException("Error writing AST", e);
		}
		if (lineDelimiter == null) {
		    lineDelimiter = System.getProperty("line.separator");
		}
		this.root = root;
		this.currentDocument = currentDocument;
	}

	public ASTWriter(Expression e) {
	    this(createModuleNode(e), null);
	}



	/**
     * @param e
     * @return
     */
    private static ModuleNode createModuleNode(Expression e) {
        ModuleNode module = new ModuleNode((CompileUnit) null);
        module.addStatement(new ExpressionStatement(e));
        module.setDescription("DummyModule.groovy");
        return module;
    }

    /**
	 * Sets the start offset (horizontal) of the new written code
	 *
	 * @param tabs start offset of new written code
	 */
	public void setStartOffset(int startOffset) {
		this.columnOffset = startOffset;
		printColumnOffset();
	}

	/**
	 * Sets the line offset to avoid renumber of line information
	 * @param offset
	 */
	public void setLineOffset(int offset) {
		this.lineOffset = offset;
	}

	/**
	 * Sets the modifier that, should be used when writing back a MethodNode
	 * @param mod
	 */
	public void setModifierToUse(String mod) {
		this.modifier = mod;
		explizitModifier = true;
	}

	public void insertLineFeed() {
		groovyCode.append(lineDelimiter);
	}

	public String getGroovyCode() {
		return groovyCode.toString();
	}

	public void visitRoot() {
		preVisitStatement(root);
		//write package
		if (root.getPackageName() != null) {
			groovyCode.append("package ");
			String packageName = root.getPackageName();
			//packageName ends with ".", chop it
			groovyCode.append(packageName.substring(0, packageName.length() - 1));
		}
		//write importPackage like import test.*
        printImports(root.getStarImports());

		//write imports like import test.TestClass
        printImports(root.getImports());

		//write imports like import static java.lang.Math.PI
        printImports(root.getStaticImports().values());

		//write imports like import static java.lang.Math.*
        printImports(root.getStaticStarImports().values());

		//write Statements that are not inside a class
		if (!root.getStatementBlock().isEmpty()) {
			visitBlockStatement(root.getStatementBlock());
		}
		//write the classes
		List<ClassNode> classes = root.getClasses();
		for (ClassNode classNode : classes) {
			if (!classNode.isScript()) {
				visitClass(classNode);
			} else {
				List<MethodNode> methods = root.getMethods();
				for ( MethodNode method : methods) {
					visitMethod(method);
				}
			}
		}
		postVisitStatement(root);
	}

    private void printImports(Collection<ImportNode> imports) {
        for (Iterator<ImportNode> impIter = imports.iterator(); impIter.hasNext();) {
            ImportNode imp = impIter.next();
            groovyCode.append(imp.getText());
            if (impIter.hasNext()) {
                insertLineFeed();
                lineOfPreviousNode++;
            }
        }
	}

    public void visitClass(ClassNode node) {
    	visitAnnotations(node);
    	preVisitStatement(node);
    	linesSinceFirstAnnotation = 0;

        if(node.getSuperClass().getName().equals("java.lang.Enum")){
        	writeEnum(node);
            return;
        }

        //TODO::::::::::::::::::reto
       /*
        * all modifiers in a run of the AST writer
        * are written back the same. example:
        * class that is explicitly public leads to methods
        * that are explicitly public
        */
        if (node.isInterface()) {
        	groovyCode.append("interface ");
        } else {
        	groovyCode.append(ASTWriterHelper.getAccModifier(node.getModifiers(),
        						ASTWriterHelper.MOD_CLASS));
        	groovyCode.append("class ");
        }
		printType(node);
		if (!node.getSuperClass().getNameWithoutPackage().equals("Object")) {
			groovyCode.append(" extends ");
			groovyCode.append(node.getSuperClass().getNameWithoutPackage());
		}
		if (node.getInterfaces().length > 0) {
			groovyCode.append(" implements ");
			ClassNode[] theInterfaces = node.getInterfaces();
			for (int i = 0; i < theInterfaces.length; i++ ) {
				groovyCode.append(theInterfaces[i].getNameWithoutPackage());
				if (i < theInterfaces.length - 1) {
					groovyCode.append(", ");
				}
			}
		}
		preVisitStatementOpenBlock(node);
        node.visitContents(this);
        postVisitStatementCloseBlock(node);
        postVisitStatement(node);

        // Object initializers don't exist at this point
        /*List list = node.getObjectInitializerStatements();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Statement element = (Statement) iter.next();
            element.visit(this);
        }*/
    }

    /*
     * Enums are written back on ONE line and a lot of
     * the original parts will get lost.
     */
	private void writeEnum(ClassNode node) {
		groovyCode.append("enum ");
		groovyCode.append(node.getName() + " ");
		groovyCode.append('{');
		for(int i = 0; i < node.getFields().size(); i++){
			FieldNode fn = (FieldNode) node.getFields().get(i);
			if(i == 0)
				groovyCode.append(fn.getName());
			else{
				//ast contains additional variables that start with a '$'
				//don't write these back
				if(!fn.getName().startsWith("$")){
					groovyCode.append(", " + fn.getName());
				}
			}
		}
		groovyCode.append('}');
		postVisitStatement(node);
	}

    public void visitAnnotations(AnnotatedNode node) {
    	boolean first = true;
        List<AnnotationNode> annotionMap = node.getAnnotations();
        if (annotionMap.isEmpty()) return;
        Iterator<AnnotationNode> it = annotionMap.iterator();
        while (it.hasNext()) {
            AnnotationNode an = (AnnotationNode) it.next();

            // annotations with no member-value pairs are having
            // an invalid lastLineNumber.  It is 1 greater than it should be.
            // ASC no longer an issue when annotation position is fixed...
            int extra = 1;//an.getMembers().size() == 0 ? 0 : 1;

            linesSinceFirstAnnotation += (an.getLastLineNumber() + extra) - an.getLineNumber();
            preVisitStatement(an);
            groovyCode.append("@");
            groovyCode.append(an.getClassNode().getNameWithoutPackage());
            //skip builtin properties
            if (an.isBuiltIn()) continue;
            for (Entry<String, Expression> member : an.getMembers().entrySet()) {
                Expression memberValue = member.getValue();
                preVisitExpression(memberValue);
            	if (first) {
            		first = false;
            		groovyCode.append("(value = ");
            	} else {
            		groovyCode.append(", value = ");
            	}
                memberValue.visit(this);
                if (!first) {
                	groovyCode.append(")");
                }
                postVisitExpression(memberValue);
            }
            postVisitStatement(an);
        }
    }

    protected void visitClassCodeContainer(Statement code) {
        if (code != null) {
        	code.visit(this);
        }
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        visitAnnotations(node);
        preVisitStatement(node);
        linesSinceFirstAnnotation = 0;
        printMethodHead(node);
        Statement code = node.getCode();
        if (code != null) {
	        code.setSourcePosition(node);
	        visitClassCodeContainer(code);
    	}
        postVisitStatement(node);
    }

	/**
	 * Prints the Head of the Method
	 * @param node MethodNode from which the head shall be printed
	 */
	public void printMethodHead(MethodNode node) {
		if (node.isVoidMethod()) {
			if (explizitModifier) {
        		groovyCode.append(modifier);
        		groovyCode.append(" ");
        	}
			groovyCode.append("void ");
        } else {
        	if (explizitModifier) {
        		groovyCode.append(modifier);
        		groovyCode.append(" ");
        	} else {
        		groovyCode.append(ASTWriterHelper.getAccModifier(node.getModifiers(),
        						ASTWriterHelper.MOD_METHOD));
        	}
        	if (!node.isDynamicReturnType()) {
	        	printType(node.getReturnType());
	        	groovyCode.append(" ");
        	}
    	}
        groovyCode.append(node.getName());

        groovyCode.append("(");
        Parameter[] parameters = node.getParameters();
        printParameters(parameters);
        groovyCode.append(")");

        if(node.getExceptions() != null){
	        if(node.getExceptions().length > 0){
	        	groovyCode.append(" throws ");
	        	for(int i = 0; i < node.getExceptions().length; i++){
	        		if(i==0)groovyCode.append(node.getExceptions()[i].getNameWithoutPackage());
	        		else groovyCode.append(", " + node.getExceptions()[i].getNameWithoutPackage());
	        	}
	        }
        }
	}

	private void printParameters(Parameter[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
        	Parameter parameter = parameters[i];
        	visitAnnotations(parameter);
        	if (!parameter.getAnnotations().isEmpty()) {
        		groovyCode.append(" ");
        	}
        	linesSinceFirstAnnotation = 0;
        	if (!parameter.isDynamicTyped()) {
        		printType(parameter.getOriginType());
        		groovyCode.append(" ");
        	}
        	groovyCode.append(parameter.getName());
            if (parameter.hasInitialExpression()) {
            	groovyCode.append("=");
            	parameter.getInitialExpression().visit(this);
            }
            if (i < parameters.length - 1) {
            	groovyCode.append(", ");
            }
        }
	}

    public void visitConstructor(ConstructorNode node) {
        visitConstructorOrMethod(node,true);
    }

    public void visitMethod(MethodNode node) {
        visitConstructorOrMethod(node,false);
    }

    public void visitField(FieldNode node) {
    	// Do not write fields back which are manually added due to optimization
    	if (!node.getName().startsWith("$")) {
	    	visitAnnotations(node);
	    	preVisitStatement(node);
	    	//properties are stored twice in ClassNode, visitOnlyOnce
	    	//visitProperty does nothing
	    	if (ASTWriterHelper.isProperty(node)) {
	    		if (!node.isDynamicTyped()) {
	    			if (node.isStatic()) {
	    				groovyCode.append("static ");
	    			}
	            	printType(node.getOriginType());
	    		} else {
	    			groovyCode.append("def");
	    			if (node.isStatic()) {
	    				groovyCode.append(" static");
	    			}
	    		}
	    		groovyCode.append(" ");
	    	} else {
		        groovyCode.append(ASTWriterHelper.getAccModifier(node.getModifiers(),
		        				ASTWriterHelper.MOD_FIELD));
		        if (!node.isDynamicTyped()) {
		        	printType(node.getOriginType());
		        	groovyCode.append(" ");
		        }
	    	}
	    	groovyCode.append(node.getName());
	        Expression init = node.getInitialExpression();
	        if (init != null) {
	        	/*
	        	 * if lineNumber of init is -1, the initializer
	        	 *  has not been written explicitly
	        	 */
	        	if (init.getLineNumber() != -1) {
		        	groovyCode.append(" = ");
		        	init.visit(this);
	        	}
	        }
	        postVisitStatement(node);
    	}
    }

	public void visitProperty(PropertyNode node) {
		//do nothing, also visited as FieldNode
    }

    @Override
    public void visitAssertStatement(AssertStatement statement) {
        preVisitStatement(statement);
        groovyCode.append("assert ");
        super.visitAssertStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
    	//write CaseStatement and defaultStatment without curly brackets
    	//also finally Statement (BlockStatement in BlockStatement) needs to be adapted maybe
    	//TODO:look in file to decide whether write curly brakets or not
    	if (	getTop() instanceof CaseStatement ||
    			getTop() instanceof SwitchStatement ||
    			getTop() instanceof BlockStatement ||
    			getTop() instanceof ModuleNode) {
    		preVisitStatement(block);
    		super.visitBlockStatement(block);
    		postVisitStatement(block);
    	} else {
    		preVisitStatementOpenBlock(block);
    		if (getParent() instanceof ClosureExpression) {

    			ClosureExpression closure = (ClosureExpression)getParent();
    			Parameter[] parameters = closure.getParameters();
    			if (parameters.length > 0) {
	    			printParameters(parameters);
	    			groovyCode.append(" -> ");
    			}
    		}
    		super.visitBlockStatement(block);
    		postVisitStatementCloseBlock(block);
    	}
    }

    @Override
    public void visitBreakStatement(BreakStatement statement) {
        preVisitStatement(statement);
        groovyCode.append("break");
        if(statement.getLabel() != null){
        	groovyCode.append(" " + statement.getLabel());
        }
        super.visitBreakStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitCaseStatement(CaseStatement statement) {
        preVisitStatement(statement);
        groovyCode.append("case ");
        super.visitCaseStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
    	preVisitStatement(statement);
    	groovyCode.append(" catch (");
    	ClassNode typOfException = statement.getExceptionType();
    	Parameter eVariable = statement.getVariable();
    	if (!eVariable.isDynamicTyped()) {
    		printType(typOfException);
    		groovyCode.append(" ");
    	}
    	groovyCode.append(eVariable.getName());
    	groovyCode.append(")");
        super.visitCatchStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitContinueStatement(ContinueStatement statement) {
        preVisitStatement(statement);
        groovyCode.append("continue");
        super.visitContinueStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement loop) {
        preVisitStatementOpenBlock(loop);
        super.visitDoWhileLoop(loop);
        postVisitStatementCloseBlock(loop);
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        preVisitStatement(statement);
        if(statement.getStatementLabel() != null){
        	groovyCode.append(statement.getStatementLabel() + ": ");
        }
        //super.visitExpressionStatement(statement);
        if (statement.getExpression() instanceof MethodCallExpression){
        	MethodCallExpression methCallExpr = (MethodCallExpression) statement.getExpression();

        	if (!methCallExpr.isImplicitThis()) {
        		methCallExpr.getObjectExpression().visit(this);
        		ArgumentListExpression ale = ((ArgumentListExpression)methCallExpr.getArguments());
        		printArgumentsOfaMethodCall(methCallExpr, ale);
        	}else{
        		super.visitExpressionStatement(statement);
        	}
        }else{
        	super.visitExpressionStatement(statement);
        }
        postVisitStatement(statement);
    }

	private void printArgumentsOfaMethodCall(MethodCallExpression methCallExpr,
			ArgumentListExpression ale) {
		if(ale != null){
			groovyCode.append('.');
			groovyCode.append(methCallExpr.getMethod().getText());

			/*
			 * Methodcall has a certain number of
			 * arguments
			 */
			if(ale.getExpressions().size() >=1){

				List<Expression> listOfAllExpressions = ale.getExpressions();
				ArgumentListExpression listOfMethodArguments = new ArgumentListExpression();
				ClosureExpression closure = null;

				/*
				 * Loop assigns closure, if there's a closure
				 * (There's no difference in the AST whether it is a normal
				 * parameter or a closure)
				 */
				for(Expression expr : listOfAllExpressions){
					if(expr instanceof ClosureExpression){
						closure = (ClosureExpression) expr;
					}else{
						listOfMethodArguments.addExpression(expr);
					}
				}
				/*
				 * Visit the "normal" arguments
				 */
				if(listOfMethodArguments.getExpressions().size() >= 1){
					printArgumentsOfaMethod(listOfMethodArguments);
				}

				if(closure != null) {
					closure.visit(this);
				}

			}else{
				groovyCode.append("()");
			}
		}
	}

	private void printArgumentsOfaMethod(ArgumentListExpression methCallExpr) {
		groovyCode.append('(');
		methCallExpr.visit(this);
		groovyCode.append(')');
	}

    @Override
    public void visitForLoop(ForStatement forLoop) {
        preVisitStatement(forLoop);
        groovyCode.append("for ");
        //if its groovy for-loop
        if (!forLoop.getVariable().getName().equals("forLoopDummyParameter")) {
        	groovyCode.append("(");
        	groovyCode.append(forLoop.getVariable().getName());
        	groovyCode.append(" in ");
        	forLoop.getCollectionExpression().visit(this);
            groovyCode.append(")");
            forLoop.getLoopBlock().visit(this);
        } else {
	        forLoop.getCollectionExpression().visit(this);
	        if (forLoop.getLoopBlock() instanceof BlockStatement) {
	        	forLoop.getLoopBlock().visit(this);
	        } else {
	        	columnOffset++;
	        	forLoop.getLoopBlock().visit(this);
	        	columnOffset--;
	        }
        }
        postVisitStatement(forLoop);
    }

    @Override
    public void visitIfElse(IfStatement ifElse) {
    	preVisitStatement(ifElse);
        groovyCode.append("if (");
        ifElse.getBooleanExpression().visit(this);
        groovyCode.append(")");
        //if without curly braces
        if (!(ifElse.getIfBlock() instanceof BlockStatement)) {
        	columnOffset++; //format
        	ifElse.getIfBlock().visit(this);
        	columnOffset--;
        } else {
        	ifElse.getIfBlock().visit(this);
        }
        //If ElseBlock exists
        if (!(ifElse.getElseBlock() instanceof EmptyStatement)) {
        	if (!(ifElse.getElseBlock() instanceof BlockStatement)
        			&& !(ifElse.getElseBlock() instanceof IfStatement)) {
        		positioningCursor();
        		insertLineFeed();
        		groovyCode.append("else");
        		lineOfPreviousNode ++;
        		columnOffset++; //format
        		ifElse.getElseBlock().visit(this);
        		columnOffset--;
        	} else {
		        /*
		         * Do not write "else" here to allow writing it later
		         * on the same line as the closing brace of the if-block
		         */
	        	inElseBlock = true;
		        ifElse.getElseBlock().visit(this);
        	}
        }
        postVisitStatement(ifElse);
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        // ignore return statement when in an empty script
        if (!shouldIgnoreReturn()) {
            preVisitStatement(statement);
            groovyCode.append("return ");
            super.visitReturnStatement(statement);
            postVisitStatement(statement);
        }
    }

    /**
     * Ignore the return statement when visiting an empty script
     */
    private boolean shouldIgnoreReturn() {
        if (root.getClasses().size() == 1) {
            ClassNode clazz = (ClassNode) root.getClasses().get(0);
            if (clazz.isScript()) {
                MethodNode runMethod = clazz.getMethod("run", new Parameter[0]);
                if (runMethod != null) {
                    Statement s = runMethod.getCode();
                    if (s instanceof BlockStatement) {
                        BlockStatement body = (BlockStatement) s;
                        if (body.getStatements().size() == 1 && body.getStatements().get(0) instanceof ReturnStatement) {
                            ReturnStatement ret = (ReturnStatement) body.getStatements().get(0);
                            return ret.getExpression() instanceof ConstantExpression &&
                                ((ConstantExpression) ret.getExpression()).getText().equals("null");
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        preVisitStatement(statement);
        groovyCode.append("switch (");
        statement.getExpression().visit(this);
        groovyCode.append(")");
        List<CaseStatement> list = statement.getCaseStatements();
        if (list != null) {
            for (CaseStatement caseStatement : list) {
                caseStatement.visit(this);
            }
        }
        statement.getDefaultStatement().visit(this);
        postVisitStatementCloseBlock(statement);
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        preVisitStatement(statement);
        groovyCode.append("synchronized (");
        statement.getExpression().visit(this);
        groovyCode.append(")");
        statement.getCode().visit(this);
        postVisitStatement(statement);
    }

    @Override
    public void visitThrowStatement(ThrowStatement statement) {
        preVisitStatement(statement);
        groovyCode.append("throw ");
        super.visitThrowStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
        preVisitStatement(statement);
        groovyCode.append("try");
        statement.getTryStatement().visit(this);
        List<CatchStatement> list = statement.getCatchStatements();
        if (list != null) {
            for (CatchStatement catchStatement : list) {
                catchStatement.visit(this);
            }
        }
        /* if a finally statement is written
         * doesn't matter if the finally statement is empty
         */
        if (!statement.getFinallyStatement().isEmpty()) {
        	groovyCode.append(" finally");
        	statement.getFinallyStatement().visit(this);
        }
        postVisitStatement(statement);
    }

    @Override
    public void visitWhileLoop(WhileStatement loop) {
        preVisitStatement(loop);
        groovyCode.append("while (");
        loop.getBooleanExpression().visit(this);
        groovyCode.append(")");
        if (loop.getLoopBlock() instanceof BlockStatement) {
        	loop.getLoopBlock().visit(this);
        } else {
            columnOffset++;
            loop.getLoopBlock().visit(this);
            columnOffset--;
        }
        postVisitStatement(loop);
    }

    /*
     * Expressions
     */
    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
    	preVisitExpression(call);
    	if (!call.getText().contains("this")) {
	    	call.getObjectExpression().visit(this);
	    	groovyCode.append(".");
    	}

        call.getMethod().visit(this);
        groovyCode.append("(");
        call.getArguments().visit(this);
        groovyCode.append(")");
        postVisitExpression(call);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
    	preVisitExpression(call);
        groovyCode.append(call.getMethod());
        groovyCode.append("(");
        call.getArguments().visit(this);
        groovyCode.append(")");
        postVisitExpression(call);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
    	preVisitExpression(call);
    	groovyCode.append("new ");
    	printType(call.getType());
    	groovyCode.append("(");
        call.getArguments().visit(this);
        groovyCode.append(")");
        postVisitExpression(call);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
    	boolean writeParanthesis = false;
    	preVisitExpression(expression);
    	Token operation = expression.getOperation();

    	if (operation.getType() == Types.LEFT_SQUARE_BRACKET) {
    		expression.getLeftExpression().visit(this);
    		groovyCode.append("[");
    		expression.getRightExpression().visit(this);
    		groovyCode.append("]");
        } else {
        	LineColumn coords = new LineColumn(expression.getLineNumber(),
        			expression.getColumnNumber());
            try {
                if (!(getParent() instanceof DeclarationExpression)
                        && FilePartReader.readForwardFromCoordinate(currentDocument, coords).startsWith("(")) {
                    groovyCode.append("(");
                    writeParanthesis = true;
                }
            } catch (BadLocationException e) {
                GroovyCore.logException("Error in refactoring", e);
            }
        	expression.getLeftExpression().visit(this);
        	if (expression.getRightExpression().getText() != "null") {
	        	groovyCode.append(" ");
	        	groovyCode.append(operation.getText());
	        	groovyCode.append(" ");
	        	expression.getRightExpression().visit(this);
        	}
        	if (writeParanthesis) {
        		groovyCode.append(")");
        	}
        }

        /* printExpression("", expression, "");
    	 * Can't print the expression like this, cause if the leftExpression is of type
    	 * VariableExpression the Line expression.getText() would ignore the type of the VariableExpression
    	 * This is because the VariableExpression's getText() method ignore the type
    	 */
    	postVisitExpression(expression);
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        expression.getBooleanExpression().visit(this);
        groovyCode.append(" ? ");
        expression.getTrueExpression().visit(this);
        groovyCode.append(" : ");
        expression.getFalseExpression().visit(this);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
    	preVisitExpression(expression);
    	super.visitPostfixExpression(expression);
        groovyCode.append(expression.getOperation().getText());
        postVisitExpression(expression);
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
    	preVisitExpression(expression);
    	groovyCode.append(expression.getOperation().getText());
    	super.visitPrefixExpression(expression);
        postVisitExpression(expression);
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
    	preVisitExpression(expression);
//    	LineColumn coords = new LineColumn(expression.getLineNumber(),
//    			expression.getColumnNumber());
//      	if (!(expression.getExpression() instanceof BinaryExpression)
//      			&& FilePartReader.readForwardFromCoordinate(currentDocument,coords).startsWith("(")) {
//    		/*
//    		 * e.g  displayName = (boolVal) ?: f   //"(" optional, that causes the read in the original
//    		 */
//    		groovyCode.append("(");
//    		super.visitBooleanExpression(expression);
//    		groovyCode.append(")");
//    	} else {
//        	/*
//        	 * if the BooleanExpression contains a BinaryExpression,
//			 * the parenthesis are written there
//        	 *
//        	 * e.g if (test == 4) {}
//        	 * or displayName = boolVal ?: f
//        	 */
//    		super.visitBooleanExpression(expression);
//    	}
    	super.visitBooleanExpression(expression);
    	postVisitExpression(expression);
	}

	@Override
    public void visitNotExpression(NotExpression expression) {
		preVisitExpression(expression);
		printExpression("!", expression, "");
		postVisitExpression(expression);
	}

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
    	preVisitExpression(expression);
    	expression.getCode().setSourcePosition(expression);
        expression.getCode().visit(this);
        postVisitExpression(expression);
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    @Override
    public void visitListExpression(ListExpression expression) {
    	preVisitExpression(expression);
    	groovyCode.append("[");
        visitListOfExpressions(expression.getExpressions(),",");
        groovyCode.append("]");
        postVisitExpression(expression);
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
    	preVisitExpression(expression);
        visitListOfExpressions(expression.getExpressions());
        groovyCode.append("new ");
        String typeName = expression.getType().getNameWithoutPackage();
        if (typeName.startsWith("[")) {
            // this is an array signature
            typeName = Signature.getElementType(typeName);
            typeName = Signature.getSignatureSimpleName(typeName);
        }
        groovyCode.append(typeName);
        visitListOfExpressions(expression.getSizeExpression(),"");
        postVisitExpression(expression);
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
    	boolean isMapList = !(expression instanceof NamedArgumentListExpression);
    	preVisitExpression(expression);
        List<MapEntryExpression> mapEntries = expression.getMapEntryExpressions();
        if (isMapList) {
            groovyCode.append("[");
        }
    	if (!mapEntries.isEmpty()) {
    		visitListOfExpressions(mapEntries,",");
    	} else {
    		groovyCode.append(":");
    	}
    	if(isMapList)groovyCode.append("]");
        postVisitExpression(expression);
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
    	preVisitExpression(expression);
        expression.getKeyExpression().visit(this);
        groovyCode.append(":");
        expression.getValueExpression().visit(this);
        postVisitExpression(expression);
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
    	preVisitExpression(expression);
        expression.getFrom().visit(this);
        groovyCode.append("..");
        if (!expression.isInclusive()) {
        	groovyCode.append("<");
        }
        expression.getTo().visit(this);
        postVisitExpression(expression);
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
    	preVisitExpression(expression);
    	groovyCode.append("*");
    	super.visitSpreadExpression(expression);
        postVisitExpression(expression);
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
    	preVisitExpression(expression);
        groovyCode.append("*");
        //super.visitSpreadMapExpression(expression);
        postVisitExpression(expression);
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
    	preVisitStatement(expression);
        expression.getExpression().visit(this);
        groovyCode.append(".&");
        expression.getMethodName().visit(this);
        postVisitStatement(expression);
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
    	preVisitExpression(expression);
    	groovyCode.append("~");
        expression.getExpression().visit(this);
        postVisitExpression(expression);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
    	preVisitExpression(expression);
    	groovyCode.append("(");
    	printType(expression.getType());
    	groovyCode.append(")");
    	super.visitCastExpression(expression);
        postVisitExpression(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
    	preVisitExpression(expression);
    	String pre = "";
    	String post = "";
    	ASTNode parent = getParent();

    	if (parent instanceof AssertStatement && expression.getText() != "null") {
			pre = " , ";
		}
    	if(parent instanceof DeclarationExpression || parent instanceof FieldNode){
    		/*
    		 * If a const expression has one of these types
    		 * the user wrote the suffix in the source.
    		 * There could be more of these situations
    		 */
    		if (expression.getType().equals(ClassHelper.Float_TYPE)){
    			post += "f";
    		}else if (expression.getType().equals(ClassHelper.Double_TYPE)){
    			post += "d";
    		} else if (expression.getType().equals(ClassHelper.BigInteger_TYPE)) {
    			post += "g";
    		} else if (expression.getType().equals(ClassHelper.Long_TYPE)) {
    			post += "l";
    		}
    	}
   		//normally write quotes if expression is of type String.
   		//exceptions: methodName in MethodCallExpression
    	//String in a GStringExpression
    	//methodName in MethodPointerExpression
    	if (constExprIsAString(expression)) {
    		LineColumn coords = new LineColumn(expression.getLineNumber(), expression.getColumnNumber());
    		String stringMarker = ASTWriterHelper.getStringMarker(currentDocument, coords);
    		pre += stringMarker;
    		printExpression(pre,expression, stringMarker);
    		if (stringMarker.length() == 3) {
    			//these strings might be longer than one line -> don't need to add manual linefeeds
		    	lineOfPreviousNode += expression.getLastLineNumber() - expression.getLineNumber();
    		}
    	}
    	else{
    		printExpression(pre, expression, post);
    	}
    	postVisitExpression(expression);
    }

	private boolean constExprIsAString(ConstantExpression expression) {
		return expression.getType().getName() == "java.lang.String"
    			&& !(getParent() instanceof MethodCallExpression)
    			&& !(getParent() instanceof GStringExpression)
    			&& !(getParent() instanceof MethodPointerExpression);
	}

    @Override
    public void visitClassExpression(ClassExpression expression) {
    	preVisitExpression(expression);
		printType(expression.getType());
    	postVisitExpression(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
    	//String pre = "";
    	//ASTNode parent = getParent();
        getParent();
    	preVisitExpression(expression);
    	LineColumn coords = new LineColumn(expression.getLineNumber(),
    			expression.getColumnNumber());
        try {
            if (FilePartReader.readForwardFromCoordinate(currentDocument, coords).startsWith("(")) {
                printExpression("(", expression, ")");
            } else {
                printExpression(expression);
            }
        } catch (BadLocationException e) {
            GroovyCore.logException("Error in refactoring", e);
        }
    	postVisitExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
    	//Write Variable type if it's the first declaration on the line
    	preVisitExpression(expression);
        VariableExpression variable = (VariableExpression) expression.getLeftExpression();
        Variable accessedVariable = variable.getAccessedVariable() == null ? variable : variable.getAccessedVariable();
    	if (previousDeclaration != null) {
    		if(previousDeclaration.getVariableExpression().getLineNumber() !=
    					expression.getVariableExpression().getLineNumber()) {
                if (!accessedVariable.isDynamicTyped()) {
		    		printType(variable.getOriginType());
		    	} else {
		    		groovyCode.append("def");
		    	}
    		} else {
    			groovyCode.append(",");
    		}
    	} else {
            if (!accessedVariable.isDynamicTyped()) {
	    		printType(variable.getOriginType());
	    	} else {
	    		groovyCode.append("def");
	    	}
    	}
    	groovyCode.append(" ");
        visitBinaryExpression(expression);
        previousDeclaration = expression;
        postVisitExpression(expression);
    }

	/**
	 * Prints the type considering array and generic
	 * @param type
	 */
	private void printType(ClassNode type) {
		if (type.isArray()) {
			printArray(type);
		} else {
			groovyCode.append(ImportResolver.getResolvedClassName(root,type,true));
			if (type.isUsingGenerics()) {
			    GenericsType[] genericTypes = type.getGenericsTypes();
			    if (genericTypes != null) {
    				groovyCode.append("<");
    				for (GenericsType generic : Arrays.asList(genericTypes)) {
    					printGenericsType(generic);
    				}
    				groovyCode.append(">");
			    }
			}
		}
	}

    public void printGenericsType(GenericsType genericType) {
    	ClassNode[] upperBounds = genericType.getUpperBounds();
    	ClassNode lowerBound = genericType.getLowerBound();
    	groovyCode.append(genericType.getName());
        if (upperBounds != null) {
            groovyCode.append(" extends ");
            for (int i = 0; i < upperBounds.length; i++) {
               printType(upperBounds[i]);
               if (i+1<upperBounds.length) groovyCode.append(" & ");
            }
        } else if (lowerBound!=null) {
            groovyCode.append(" super ");
            printType(lowerBound);
        }
    }

    /**
     * Prints an Array Declaration
     *
     * @param componentType componentType which is an Array
     */
    private void printArray(ClassNode compType) {
        ClassNode componentType = compType;
    	int dimension = 0;
    	while (componentType.isArray()) {
    		dimension++;
    		componentType = componentType.getComponentType();
    	}
    	printType(componentType);
		for (int i = 0; i < dimension; i++) {
			groovyCode.append("[]");
		}
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
    	preVisitExpression(expression);
    	String alias = ImportResolver.asAlias(root,expression.getObjectExpression().getType());
    	String fieldName = ImportResolver.asFieldName(root, expression.getObjectExpression().getType(),
    									expression.getPropertyAsString());
    	if (alias != "") {
	    	groovyCode.append(alias);
    	} else if (fieldName != ""){
    		groovyCode.append(fieldName);
    	} else {
	    	expression.getObjectExpression().visit(this);
	    	groovyCode.append(".");
	    	expression.getProperty().visit(this);
    	}
    	postVisitExpression(expression);
    }

	@Override
    public void visitAttributeExpression(AttributeExpression expression) {
    	preVisitExpression(expression);
    	expression.getObjectExpression().visit(this);
    	groovyCode.append(".@");
    	expression.getProperty().visit(this);
    	postVisitExpression(expression);
    }

	/**
	 * A FieldExpression in the AST in phase SEMANTIC_ANALYSIS can only occur
	 * in a static field access in a static method in the class containing the field
	 */
    @Override
    public void visitFieldExpression(FieldExpression expression) {
    	preVisitExpression(expression);
    	ClassNode classNode = expression.getField().getOwner();
    	groovyCode.append(ImportResolver.getResolvedClassName(root, classNode,true));
    	groovyCode.append(".");
    	groovyCode.append(expression.getFieldName());
    	postVisitExpression(expression);
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
    	preVisitExpression(expression);
        List<Expression> values = expression.getValues();
        Iterator<Expression> it = values.iterator();
		LineColumn coords = new LineColumn(expression.getLineNumber(), expression.getColumnNumber());
		String stringMarker = ASTWriterHelper.getStringMarker(currentDocument, coords);
		groovyCode.append(stringMarker);
        for (ConstantExpression stringExpression : expression.getStrings()) {
            stringExpression.visit(this);
    		if (it.hasNext()) {
    			visitValueInGString(it);
    		}
    	}
    	while (it.hasNext()) {
    		visitValueInGString(it);
    	}
    	groovyCode.append(stringMarker);
		if (stringMarker.length() == 3) {
			//these strings might be longer than one line -> don't need to add manual linefeeds
	    	lineOfPreviousNode += expression.getLastLineNumber() - expression.getLineNumber();
		}
        postVisitExpression(expression);
    }

    /*
     * Values in GStrings are allowed in the following two forms:
     * "text ${variable}" but also "text $variable"
     *
     * The AST writer needs to read the inputfile to determine
     * how to user wrote his GString
     */
    private void visitValueInGString(Iterator<Expression> it) {
        Expression valueExpression = it.next();
		LineColumn coords = new LineColumn(valueExpression.getLineNumber(), valueExpression.getColumnNumber());
        char firstChar;
        try {
            firstChar = FilePartReader.readForwardFromCoordinate(currentDocument, coords).charAt(0);
        } catch (BadLocationException e) {
            GroovyCore.logException("Error during refactoring...trying to recover", e);
            firstChar = '\0';
        }
		groovyCode.append("$");
		if(firstChar == '{'){
			groovyCode.append("{");
			(valueExpression).visit(this);
			groovyCode.append("}");
		}else{
			(valueExpression).visit(this);
		}
	}

    protected void visitListOfExpressions(List<? extends Expression> list, String separator) {
        if (list==null) return;
        for (Iterator<? extends Expression> iterator = list.iterator(); iterator.hasNext();) {
            Expression expression = iterator.next();
            preVisitExpression(expression);
            if (getParent() instanceof ArrayExpression) {
            	groovyCode.append("[");
            }
            expression.visit(this);
            if (getParent() instanceof ArrayExpression) {
            	groovyCode.append("]");
            } else if (iterator.hasNext()) {
            	groovyCode.append(separator + " ");
            }
            postVisitExpression(expression);
        }
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
    	visitListOfExpressions(ale.getExpressions(), ",");
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
    	preVisitExpression(expression);
    	expression.getBooleanExpression().visit(this);
    	groovyCode.append(" ?: ");
    	expression.getFalseExpression().visit(this);
    	postVisitExpression(expression);
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
    	preVisitExpression(expression);
    	groovyCode.append('+');
    	super.visitUnaryPlusExpression(expression);
    	postVisitExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
    	preVisitExpression(expression);
    	groovyCode.append('-');
    	super.visitUnaryMinusExpression(expression);
    	postVisitExpression(expression);
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression cle) {
    	preVisitExpression(cle);
    	groovyCode.append("(");
        visitListOfExpressions(cle.getExpressions(),";");
        groovyCode.append(")");
    	postVisitExpression(cle);
    }

	protected void preVisitStatement(ASTNode statement) {
    	ASTNode parent = getTop();
    	if (statement instanceof CaseStatement) {
    		caseCount++;
    	}
    	if (parent instanceof SwitchStatement && (caseCount == 1) ) {
    		//if we're here we are in the first case statement in a switch statement
    		//and have to print the opening curly bracket for the switch statement
    		nodeStack.pop(); //Pop SwitchStatement
    		preVisitStatementOpenBlock(parent);
    	}
    	nodeStack.push(statement);
    	if (statement.getLineNumber() != -1) {
    		lineOfCurrentNode = statement.getLineNumber()-lineOffset;
    	}
    	/*
    	 * Annotiations are included in the startline of Method/Class
    	 * -> add diffrence between annotation and class/method to the currentLine
    	 */
    	if (!(statement instanceof AnnotationNode)) {
    		lineOfCurrentNode += linesSinceFirstAnnotation;
    	}
    	positioningCursor();
	  	if (parent instanceof SwitchStatement && !(statement instanceof CaseStatement)) {
	  		groovyCode.append("default : ");
	  	} else {
    		if (inElseBlock) {
    			groovyCode.append("else ");
    			inElseBlock = false;
    		}
    	}
    }

	protected void preVisitStatementOpenBlock(ASTNode statement) {
    	nodeStack.push(statement);
    	lineOfCurrentNode = statement.getLineNumber()-lineOffset;
    	positioningCursor();
		columnOffset++;
		if (inElseBlock) {
			groovyCode.append("else");
			inElseBlock = false;
		}
		groovyCode.append(" {");
    }

	protected void postVisitStatement(ASTNode statement) {
    	nodeStack.pop();
    	lineOfCurrentNode = statement.getLastLineNumber()-lineOffset;
     	/*
    	 * Annotations lastline is to long. Workaround:
    	 * if LastLineNumber - 1 = LineNumber-> reduce annotation line count
    	 */
//    	if (statement instanceof AnnotationNode) {
//    		if (statement.getLastLineNumber() - 1 == statement.getLineNumber()) {
//    			linesSinceFirstAnnotation--;
//    		}
//    	}
    	if (statement instanceof SwitchStatement) {
    		caseCount = 0;
    	}
    }

	protected void postVisitStatementCloseBlock(ASTNode statement) {
    	nodeStack.pop();
    	lineOfCurrentNode = statement.getLastLineNumber()-lineOffset;
		columnOffset--;
    	positioningCursor();
    	groovyCode.append("}");
    }

    protected void preVisitExpression(ASTNode expression) {
    	nodeStack.push(expression);
    	lineOfCurrentNode = expression.getLineNumber()-lineOffset;
    	positioningCursor();
    }

    protected void postVisitExpression(ASTNode expression) {
    	nodeStack.pop();
    }

    protected void printExpression(ASTNode expression) {
    	String pre = "";
    	String post = "";
    	if (expression.getText() != "null") {
    		printExpression(pre,expression,post);
    	}
    }

    protected void printExpression(String pre, ASTNode expression, String post) {
    	String printExpression = expression.getText();
    	if (expression.getText() == "null") {
    		printExpression = "";
    	}
		//Escape if not RegularExpression or MultiLineString
		if (pre.length() > 0) {
			if (pre.charAt(0) != '/' && expression.getLineNumber() == expression.getLastLineNumber()) {
				printExpression = escapeJava(printExpression);
			} else {
				printExpression = printExpression.replaceAll("\r\n|\n", System.getProperty("line.separator"));
			}
		}
    	groovyCode.append(pre);
    	groovyCode.append(printExpression);
    	groovyCode.append(post);
    	if (getParent() instanceof CaseStatement) {
    		groovyCode.append(" : ");
    	}
    }

	private void positioningCursor() {
		boolean onNewLine = false;
		if ((lineOfPreviousNode == lineOfCurrentNode) && (getTop() instanceof BreakStatement)) {
			groovyCode.append(" ; ");
		} else {
			while (lineOfPreviousNode < lineOfCurrentNode) {
				groovyCode.append(lineDelimiter);
				lineOfPreviousNode++;
				onNewLine = true;
			}
		}
		if (onNewLine) {
			printColumnOffset();
		}
	}

	/**
	 *
	 */
	private void printColumnOffset() {
		for (int i = 0; i < columnOffset; i++) {
			groovyCode.append("    ");
		}
	}

	private ASTNode getTop() {
		if (nodeStack.isEmpty()) {
			return null;
		}
        return nodeStack.peek();
	}

	private ASTNode getParent() {
		if (nodeStack.size() > 1) {
			ASTNode topNode = nodeStack.pop();
			ASTNode parrentNode = nodeStack.peek();
			nodeStack.push(topNode);
			return parrentNode;
		}
        return null;
	}


    //--------------------------------------------------------------------------
    /**
     * From org.apache.commons.lang.StringEscapeUtils
     * <p>Escapes the characters in a <code>String</code> using Java String rules.</p>
     *
     * <p>Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.) </p>
     *
     * <p>So a tab becomes the characters <code>'\\'</code> and
     * <code>'t'</code>.</p>
     *
     * <p>The only difference between Java strings and JavaScript strings
     * is that in JavaScript, a single quote must be escaped.</p>
     *
     * <p>Example:
     * <pre>
     * input string: He didn't say, "Stop!"
     * output string: He didn't say, \"Stop!\"
     * </pre>
     * </p>
     *
     * @param str  String to escape values in, may be null
     * @return String with escaped values, <code>null</code> if null string input
     */
    private static String escapeJava(String str) {
        return escapeJavaStyleString(str, false);
    }


    /**
     * From org.apache.commons.lang.StringEscapeUtils
     * <p>Worker method for the {@link #escapeJavaScript(String)} method.</p>
     *
     * @param str String to escape values in, may be null
     * @param escapeSingleQuotes escapes single quotes if <code>true</code>
     * @return the escaped string
     */
    private static String escapeJavaStyleString(String str, boolean escapeSingleQuotes) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(str.length() * 2);
            escapeJavaStyleString(writer, str, escapeSingleQuotes);
            return writer.toString();
        } catch (IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * From org.apache.commons.lang.StringEscapeUtils
     *
     * <p>Worker method for the {@link #escapeJavaScript(String)} method.</p>
     *
     * @param out write to receieve the escaped string
     * @param str String to escape values in, may be null
     * @param escapeSingleQuote escapes single quotes if <code>true</code>
     * @throws IOException if an IOException occurs
     */
    private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (str == null) {
            return;
        }
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                out.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                out.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.write("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b':
                        out.write('\\');
                        out.write('b');
                        break;
                    case '\n':
                        out.write('\\');
                        out.write('n');
                        break;
                    case '\t':
                        out.write('\\');
                        out.write('t');
                        break;
                    case '\f':
                        out.write('\\');
                        out.write('f');
                        break;
                    case '\r':
                        out.write('\\');
                        out.write('r');
                        break;
                    default :
                        if (ch > 0xf) {
                            out.write("\\u00" + hex(ch));
                        } else {
                            out.write("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'':
                        if (escapeSingleQuote) {
                          out.write('\\');
                        }
                        out.write('\'');
                        break;
                    case '"':
                        out.write('\\');
                        out.write('"');
                        break;
                    case '\\':
                        out.write('\\');
                        out.write('\\');
                        break;
                    default :
                        out.write(ch);
                        break;
                }
            }
        }
    }

    /**
     * From org.apache.commons.lang.StringEscapeUtils
     *
     * <p>Returns an upper case hexadecimal <code>String</code> for the given
     * character.</p>
     *
     * @param ch The character to convert.
     * @return An upper case hexadecimal <code>String</code>
     */
    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }
}
