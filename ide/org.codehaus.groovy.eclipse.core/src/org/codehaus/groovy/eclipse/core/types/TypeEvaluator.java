 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.types;

import java.util.ArrayList;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetCompiler;
import org.codehaus.groovy.eclipse.core.util.UnsupportedVisitException;
import org.codehaus.groovy.syntax.Types;

/**
 * The type evaluator attempts to evaluate an expression and return the type of the result.
 * 
 * @author empovazan
 */
public class TypeEvaluator {
	public static class EvalResult {
		String name;

		boolean isClass;

		EvalResult(String name, boolean isClass) {
			this.name = name;
			this.isClass = isClass;
		}

		public String getName() {
			return name;
		}

		public boolean isClass() {
			return isClass;
		}
	}
	
	private final Visitor visitor = new Visitor();

	private final ITypeEvaluationContext context;
	
	
	/**
	 * Creates a type evaluator for the given evaluation context.
	 * @param context
	 */
	public TypeEvaluator(ITypeEvaluationContext context) {
		this.context = context;
	}

	/**
	 * Evaluate an expression for its type.
	 * 
	 * @param expression
	 * @return The evaluation result, or null if a type could not be evaluated.
	 */
	public EvalResult evaluate(String expression) {
		ModuleNode moduleNode = compileExpression(expression);
		if (moduleNode != null) {
			Statement stmt = extractFirstStatement(moduleNode);
			if (stmt != null) {
			    stmt.visit(visitor);
			    return visitor.getResult();
			}
		}
		return null;
	}
	
	/**
	 * Given an expression in 
	 * @param expression
	 * @return
	 */
	public EvalResult evaluate(Expression expression) {
		try {
			expression.visit(visitor);
		} catch (UnsupportedVisitException e) {
			GroovyCore.logException("Unsupported type evaluation: " + e.getMessage(), e);
		}
		return visitor.getResult();
	}

	/**
	 * Compile the simple expression without any type resolution.
	 * 
	 * @param expression
	 * @return
	 */
	private ModuleNode compileExpression(String expression) {
		expression = decorateExpression(expression);
		
		GroovySnippetCompiler compiler = new GroovySnippetCompiler(context.getProject());
		return compiler.compile(expression);
//		IGroovyCompiler compiler = new GroovyCompiler();
//		ClassLoader loader = context.getClassLoader();
//		if (loader == null) {
//			loader = Thread.currentThread().getContextClassLoader();
//		}
//		IGroovyCompilerConfiguration config = new GroovyCompilerConfigurationBuilder()
//				.classLoader(loader)
//				.buildAST()
//				.resolveAST()
//				.done();
//		compiler.compile("CompletionExpression", new ByteArrayInputStream(expression.getBytes()), config, reporter);
//		return reporter.moduleNode;
	}

	private String decorateExpression(String expression) {
		String[] imports = context.getImports();
		if (imports.length != 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < imports.length; i++) {
				sb.append("import ").append(imports[i]).append("\n");
			}
			sb.append(expression);
			return sb.toString();
		}
		return expression;
	}

	/**
	 * Extract the first statement of the code block. This is the statement to visit to evaluate the type.
	 * 
	 * @param moduleNode
	 * @return
	 */
	private Statement extractFirstStatement(ModuleNode moduleNode) {
	    try {
    		ClassNode classNode = (ClassNode) moduleNode.getClasses().get(0);
    		MethodNode methodNode = (MethodNode) classNode.getMethods("run").get(0);
    		return (Statement) ((BlockStatement) methodNode.getCode()).getStatements().get(0);
	    } catch (IndexOutOfBoundsException e) {
	        // no first statement exists
	        return null;
	    }
	}

	class Visitor extends ClassCodeVisitorSupport {
		class StackItem {
			String name;
			Object value;
			boolean isClass;

			public StackItem(String name) {
				this(name, false);
			}
			
			public StackItem(String name, Object value) {
				this(name, value, false);
			}
			
			public StackItem(String name, boolean isClass) {
				this(name, "", isClass);
			}

			public StackItem(String name, Object value, boolean isClass) {
				this.name = name;
				this.value = value;
				this.isClass = isClass;
			}
			
			@Override
            public String toString() {
				return name + ":" + value + ":" + isClass;
			}
		}

		class Stack extends ArrayList {
			private static final long serialVersionUID = -1280137281407764358L;

			@SuppressWarnings("unchecked")
            public void push(StackItem item) {
				add(item);
			}

			public void push(String name) {
				push(name, null, false);
			}

			public void push(String name, Object value) {
				push(name, value, false);
			}
			
			public void push(String name, Object value, boolean isClass) {
				push(new StackItem(name, value, isClass));
			}

			public StackItem peek() {
				return (StackItem) get(size() - 1);
			}

			public StackItem pop() {
				return (StackItem) remove(size() - 1);
			}
		}

		Stack stack = new Stack();

		EvalResult getResult() {
			if (stack.size() > 0) {
				StackItem item = stack.pop();
				return new EvalResult(item.name, item.isClass);
			}
			return new EvalResult("void", false);
		}

		@Override
        protected SourceUnit getSourceUnit() {
			return null;
		}

		@Override
        public void visitArrayExpression(ArrayExpression expression) {
			throw new UnsupportedVisitException("visitArrayException");
		}
		
		@Override
        public void visitAttributeExpression(AttributeExpression expression) {
			throw new UnsupportedVisitException("visitAttributeExpression");
		}
		
		@Override
        public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
			super.visitBitwiseNegationExpression(expression);
			StackItem objectType = stack.pop();
			if (objectType.name.equals("java.lang.String")) {
				stack.push(new StackItem("java.util.regex.Pattern"));
			} else if (objectType.name.equals("java.lang.Integer")) {
				stack.push(new StackItem("java.lang.Integer"));
			} else if (objectType.name.equals("java.lang.Long")) {
				stack.push(new StackItem("java.lang.Long"));
			} else {
				throw new UnsupportedVisitException("visitBitwiseNegationExpression");
			}
		}
		
		@Override
        public void visitBinaryExpression(BinaryExpression expression) {
			// FUTURE: emp - if a list access, find the types of elements place in the list, and choose the base type.
			super.visitBinaryExpression(expression);
			String signature = TypeUtil.OBJECT_TYPE;
			StackItem right = stack.pop();
			StackItem left = stack.pop();
			if (expression.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
				signature = left.name;
				if (signature.charAt(0) == '[') {
					signature = signature.charAt(1) == 'L' ? signature.substring(2, signature.length() - 1) : signature
							.substring(1);
				}
			}
			stack.push(signature);
		}
		
		@Override
        public void visitBooleanExpression(BooleanExpression expression) {
			throw new UnsupportedVisitException("visitBooleanExpression");
		}
		
		@Override
        public void visitCastExpression(CastExpression expression) {
			super.visitCastExpression(expression);
			stack.push(new StackItem(expression.getType().getName(), true));
		}
		
		@Override
        public void visitClassExpression(ClassExpression expression) {
			super.visitClassExpression(expression);
			ClassNode type = expression.getType();
			stack.push(new StackItem(type.getName(), true));
		}

		@Override
        public void visitConstantExpression(ConstantExpression expression) {
			// Type and value here
			stack.push(new StackItem(expression.getType().getName(), expression
					.getValue()));
		}

		@Override
        public void visitConstructorCallExpression(ConstructorCallExpression call) {
			int stackSize = stack.size();
			super.visitConstructorCallExpression(call);
			int argCount = stack.size() - stackSize; // Ignore args.
			for (int i = 0; i < argCount; ++i) {
				stack.pop();
			}
			
			// C_tor is typed, nothing to do.
			stack.push(call.getType().getName());
		}
		
		@Override
        public void visitExpressionStatement(ExpressionStatement statement) {
			super.visitExpressionStatement(statement);
		}
		
		@Override
        public void visitFieldExpression(FieldExpression expression) {
			throw new UnsupportedVisitException("visitFieldExpression");
		}
		
		@Override
		public void visitGStringExpression(GStringExpression expression) {
		    super.visitGStringExpression(expression);
		    stack.push("groovy.lang.GString");
		}
		
		@Override
        public void visitListExpression(ListExpression expression) {
			super.visitListExpression(expression);
			stack.push("java.util.List");
		}
		
		@Override
        public void visitMapExpression(MapExpression expression) {
			super.visitMapExpression(expression);
			stack.push("java.util.Map");
		}
		
		@Override
        public void visitMethodCallExpression(MethodCallExpression call) {
			int stackSize = stack.size();
			super.visitMethodCallExpression(call);
			
			// Get the argument types.
			int argCount = stack.size() - stackSize - 2;
			String[] argTypes = new String[argCount];
			for (int i = 0; i < argCount; ++i) {
				StackItem arg = stack.pop();
				argTypes[i] = arg.name;
			}

			// Method name, remove it
			stack.pop();
			// Return type.
			StackItem objectType = stack.pop();

			// The method parameters for this call should be inferred at this point.
			Method method = context.lookupMethod(objectType.name, call.getMethod().getText(), argTypes, false,
					objectType.isClass);
			stack.push(new StackItem(method.getReturnType()));
		}
		
		@Override
        public void visitNotExpression(NotExpression expression) {
			throw new UnsupportedVisitException("visitNotExpression");
		}
		
		@Override
        public void visitPostfixExpression(PostfixExpression expression) {
		    super.visitPostfixExpression(expression);
		    stack.push(new StackItem("java.lang.Number"));
		}
		
		@Override
        public void visitPrefixExpression(PrefixExpression expression) {
            super.visitPrefixExpression(expression);
            stack.push(new StackItem("java.lang.Number"));
		}
		
		@Override
        public void visitPropertyExpression(PropertyExpression expression) {
			super.visitPropertyExpression(expression);
			StackItem propertyItem = stack.pop();
			StackItem objectItem = stack.pop();

			GroovyDeclaration property = context.lookupProperty(objectItem.name, (String) propertyItem.value,
					false, propertyItem.isClass);
			//fields are detected as properties
			if (property==null) {
			  property = context.lookupField(objectItem.name, (String) propertyItem.value,
	          false, propertyItem.isClass);
			}
			stack.push(new StackItem(property.getSignature()));
		}
		
		@Override
        public void visitRangeExpression(RangeExpression expression) {
			throw new UnsupportedVisitException("visitRangeExpression");
		}
		
		@Override
        public void visitRegexExpression(RegexExpression expression) {
			throw new UnsupportedVisitException("visitRegexExpression");
		}
		
		@Override
        public void visitReturnStatement(ReturnStatement statement) {
		    stack.push(new StackItem(statement.getExpression().getType().getName()));
		}
		
		@Override
        public void visitSpreadExpression(SpreadExpression expression) {
			throw new UnsupportedVisitException("visitSpreadExpression");
		}
		
		@Override
        public void visitSpreadMapExpression(SpreadMapExpression expression) {
			throw new UnsupportedVisitException("visitSpreadMapExpression");
		}
		
		@Override
        public void visitTernaryExpression(TernaryExpression expression) {
			super.visitTernaryExpression(expression);
		}
		
		@Override
        public void visitVariableExpression(VariableExpression expression) {
			GroovyDeclaration type = context.lookupSymbol(expression.getName());
			stack.push(new StackItem(type.getSignature(), expression.getName(), type.getType() == GroovyDeclaration.Kind.CLASS));
		}
	}
}