/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.context.impl;


import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.eclipse.jface.text.IRegion;

/**
 * Factory for creating source code contexts for a location within some Groovy code.
 * 
 * @author empovazan
 */
public class SourceCodeContextFactory {
	/**
	 * A little stack with which to collect the ASTNode path.
	 */
	static class Stack extends ArrayList< ASTNode > {
		private static final long serialVersionUID = -1280137281407764358L;

		public void push(ASTNode info) {
			add(info);
		}

		public ASTNode peek() {
			return get(size() - 1);
		}

		public ASTNode pop() {
			return remove(size() - 1);
		}
		
		public ASTNode[] toASTPath() {
			return toArray(new ASTNode[size()]);
		}
	}

	/**
	 * Visitor for a specific class. This visitor is used with a class that encloses some source location.
	 */
	class ClassVisitor extends ClassCodeVisitorSupport {
		private final ISourceBuffer buffer;

		private final Stack astNodeStack;

		private final List< ISourceCodeContext > contexts;

		private IRegion region;
		
		ClassVisitor(ISourceBuffer buffer, Stack astNodeStack, List<ISourceCodeContext> contexts, IRegion region) {
			this.buffer = buffer;
			this.astNodeStack = astNodeStack;
			this.contexts = contexts;
			this.region = region;
		}

		/**
		 * This visits the arguments of a method call. Perhaps there will be such a context later on.
		 */
		@Override
        public void visitArgumentlistExpression(ArgumentListExpression ale) {
			super.visitArgumentlistExpression(ale);
		}

		@Override
        public void visitBlockStatement(BlockStatement block) {
			// Some explanation: some blocks, like code block of a method don't have line/col numbers. These are
			// ignored. The ASTNode path contains only 'visible' node with real line/col information.
			if (block.getLineNumber() != -1 && isInBlock(buffer, block, region)) {
				astNodeStack.push(block);
				// TODO: there should be a block context.
			}
			// Visit nested blocks.
			super.visitBlockStatement(block);
		}

		@Override
        public void visitClosureExpression(ClosureExpression expression) {
			int[] range = findClosureRange(buffer, expression);
			int offset = region.getOffset();
			if (range[0] != -1 
					&& !isInClosureParameters(buffer, range, region) 
					&& range[0] <= offset && offset < range[1]) {
				astNodeStack.push(expression);
				ClosureScopeContext context = new ClosureScopeContext(buffer, astNodeStack.toASTPath(), region);
				contexts.add(context);
				super.visitClosureExpression(expression);
				// FUTURE: emp - what to do about this? Perhaps scopes should be just 'block scope' with a flag to identify
				// if it is a method, closure, for loop, etc. For now this works.
				// HACK: emp - If we get this far, there is no closure block above, but there may be a 'for' block loop.
				context.astPath = astNodeStack.toASTPath();
				throw new VisitCompleteException();
			} else {
				super.visitClosureExpression(expression);
			}
		}
		
		@Override
        public void visitClass(ClassNode node) {
			astNodeStack.push(node);
			contexts.add(new ClassContext(buffer, astNodeStack.toASTPath(), region));
			super.visitClass(node);
			contexts.add(new ClassScopeContext(buffer, astNodeStack.toASTPath(), region));
		}
		
		@Override
        public void visitForLoop(ForStatement forLoop) {
//			if (isInBlock(buffer, forLoop.getLoopBlock(), region)) {
				super.visitForLoop(forLoop);
				// If we are returning from the for loop, then this loop block is the one with the scope.
//				astNodeStack.push(forLoop.getLoopBlock());
//				throw new VisitCompleteException();
//			}
		}

		@Override
        public void visitConstructor(ConstructorNode node) {
			if (isEnclosingASTNode(node, region.getOffset())) {
				astNodeStack.push(node);
				if (isInClassOrMethodBlock(buffer, node, region)) {
					contexts.add(new ConstructorScopeContext(buffer, astNodeStack.toASTPath(), region));
					throw new VisitCompleteException();
				} else if (isInMethodParameters(buffer, node, region)) {
					contexts.add(new ConstructorParametersContext(buffer, astNodeStack.toASTPath(), region));
					throw new VisitCompleteException();
				} else {
					super.visitConstructor(node);
				}
			}
		}

		@Override
        public void visitMethod(MethodNode node) {
			if (isEnclosingASTNode(node, region.getOffset())) {
				astNodeStack.push(node);
				if (isInClassOrMethodBlock(buffer, node, region)) {
					MethodScopeContext context = new MethodScopeContext(buffer, astNodeStack.toASTPath(), region);
					contexts.add(context);
					super.visitMethod(node);
					// HACK: emp - If we get this far, there is no closure block above, but there may be a 'for' block loop.
					context.astPath = astNodeStack.toASTPath();
					throw new VisitCompleteException();
				} else if (isInMethodParameters(buffer, node, region)) {
					contexts.add(new MethodParametersContext(buffer, astNodeStack.toASTPath(), region));
					super.visitMethod(node);
					throw new VisitCompleteException();
				}
			}
		}
		
		@Override
        public void visitMethodCallExpression(MethodCallExpression call) {
			astNodeStack.push(call);
			super.visitMethodCallExpression(call);
			astNodeStack.pop();
		}
		
		/**
		 * Closures are problematic. The source ranges are wrong.<br>
		 * When they are initializers, the range is from the '=' up to the '{'.M<br>
		 * If they are arguments to methods, the range is from the '{' up to the first expression in the closure.
		 * 
		 * @return Range [a, b)
		 */
		private int[] findClosureRange(ISourceBuffer buffer, ClosureExpression expression) {
			int[] range = new int[2];
			int offset = buffer.toOffset(expression.getLineNumber(), expression.getColumnNumber());
			char ch = buffer.charAt(offset);
			if (ch == '=') {
				while (Character.isWhitespace(ch = buffer.charAt(offset++)))
					;
			} else if (ch == '{') {
				// It is necessary to check for '{' because a closure will be visited from a contructor, if the closure
				// is the initializer of a field. So can't just say 'else' on its own.
				++offset;
			} else {
				range[0] = range[1] = -1;
				return range;
			}
			range[0] = offset;

			int pairCount = 1;
			while ((ch = buffer.charAt(offset++)) != '}' && pairCount != 0) {
				if (ch == '{') {
					++pairCount;
				} else if (ch == '}') {
					--pairCount;
				}
			}
			range[1] = offset;

			return range;
		}

		/**
		 * Parameters are from inside '{' up to '->'.
		 * 
		 * @return True if in parameters, else false.
		 */
		private boolean isInClosureParameters(ISourceBuffer buffer, int[] range, IRegion region) {
			// TODO: test case: what about nested closures and blocks?
			int offset = range[0];
			char ch = buffer.charAt(offset++);
			while (ch != '}' && ch != '{') {
				if (ch == '-') {
					ch = buffer.charAt(offset);
					if (ch == '>') {
						int location = region.getOffset();
						if (range[0] <= location && location <= offset) {
							return true;
						}
					}
				}
				ch = buffer.charAt(offset++);
			}
			return false;
		}

		@Override
        protected SourceUnit getSourceUnit() {
			return null;
		}

	}

	public ISourceCodeContext[] createContexts(ISourceBuffer buffer, ModuleNode moduleNode, IRegion r) {
		List< ISourceCodeContext > contexts = newList();
		Stack astNodeStack = new Stack();

		astNodeStack.push(moduleNode);

		// Add the root context.
		contexts.add(new ModuleContext(buffer, astNodeStack.toASTPath(), r));
		
		// emp - If this is a script and there is no code, or if someone was typeing really fast, no classes will be
		// present in the module node, as the reconciler would have not caught up.
		if (moduleNode.getClasses().size() == 0) {
			return contexts.toArray(new ISourceCodeContext[contexts.size()]);
		}

		if (((ClassNode) moduleNode.getClasses().get(0)).isScript()) {
			findContextsForGroovyScript(contexts, buffer, moduleNode, astNodeStack, r);
		} else {
		    findContextsForGroovyClass(contexts, buffer, moduleNode, astNodeStack, r);
		}

		return contexts.toArray(new ISourceCodeContext[contexts.size()]);
	}

	/**
	 * Create a compatible context to the given context with new line/col coordinates.
	 * 
	 * @param sourceCodeContext
	 * @param line
	 * @param column
	 * @return
	 */
	public ISourceCodeContext createContext(ISourceCodeContext sourceCodeContext, IRegion region) {
		ISourceCodeContext[] contexts = createContexts(((AbstractSourceCodeContext) sourceCodeContext).buffer,
				(ModuleNode) sourceCodeContext.getASTPath()[0], region);
		for (int i = 0; i < contexts.length; ++i) {
			if (contexts[i].getId().equals(sourceCodeContext.getId())) {
				return contexts[i];
			}
		}
		return null;
	}

	/**
	 * The source file is implemented in the style of a Java class - there are no script methods/expressions outside of
	 * the classes.
	 * 
	 * @param contexts
	 *            The resulting contexts. The list already contains the module as the top level context.
	 * @param buffer
	 * @param moduleNode
	 * @param astNodeStack
	 *            The AST node stack with the ModuleNode on top.
	 * @param line
	 * @param column
	 */
	private void findContextsForGroovyClass(List< ISourceCodeContext > contexts, ISourceBuffer buffer, ModuleNode moduleNode,
			Stack astNodeStack, IRegion region) {
		if (isInModuleBody(buffer, moduleNode, region)) {
			// Add the module body, and done.
			contexts.add(new ModuleScopeContext(buffer, astNodeStack.toASTPath(), region));
		} else {
			// It is inside a class, either in the body, or inside some methods, so visit as needed.
			List classes = moduleNode.getClasses();
			for (Iterator iter = classes.iterator(); iter.hasNext();) {
				ClassNode classNode = (ClassNode) iter.next();

				if (isEnclosingASTNode(classNode, region.getOffset())) {
					try {
						new ClassVisitor(buffer, astNodeStack, contexts, region).visitClass(classNode);
					} catch (VisitCompleteException e) {
						break;
					}
				}
			}
		}
	}
	
	/**
	 * The source file implements a Groovy script with script methods and expressions outside of classes. In a script,
	 * unless in a defined function, the contexts are always: [module, moduleScope, class, methodScope] - for the
	 * implicit 'run' method. Otherwise: [module, classScope, methodScope] - for defined functions.
	 * 
	 * @param contexts
	 *            The resulting contexts. The list already contains the module as the top level context.
	 * @param buffer
	 * @param moduleNode
	 * @param astNodeStack
	 *            The AST node stack with the ModuleNode on top.
	 * @param line
	 * @param column
	 */
	private void findContextsForGroovyScript(List< ISourceCodeContext > contexts, ISourceBuffer buffer, ModuleNode moduleNode,
			Stack astNodeStack, IRegion region) {

		// We are only interested in script methods and the single implicitly defined 'run' method.
		MethodNode methodNode = findScriptMethod(buffer, moduleNode, region);
		if (methodNode.getName().equals("run")
				&& !isInAnyMethodParameters(buffer, methodNode.getDeclaringClass(), region)) {
			// The 'run' methods contains all expressions in the script, not including script functions.
			// So we have the odd situation of a module scope as well as a method scope at the same time.
			contexts.add(new ModuleScopeContext(buffer, astNodeStack.toASTPath(), region));

			astNodeStack.push(methodNode.getDeclaringClass());
			contexts.add(new ClassContext(buffer, astNodeStack.toASTPath(), region));

			astNodeStack.push(methodNode);
			contexts.add(new MethodScopeContext(buffer, astNodeStack.toASTPath(), region));

			try {
				ClassVisitor visitor = new ClassVisitor(buffer, astNodeStack, contexts, region);
				Statement code = methodNode.getCode();
				if (code != null)
					code.visit(visitor);
			} catch (VisitCompleteException e) {
			}
			if ((contexts.get(contexts.size() - 1)).getId().equals(ISourceCodeContext.METHOD_SCOPE) == false) {
				// Likely in a closure. But module scope added above is incorrect if not in method scope here.
				contexts.remove(1);
			}

		} else {
			List classes = moduleNode.getClasses();
			for (Iterator iter = classes.iterator(); iter.hasNext();) {
				ClassNode classNode = (ClassNode) iter.next();

				// The class node does not have valid coords, need to check per method/params.
				if (isInAnyMethodParameters(buffer, classNode, region)
						|| isInAnyMethodBlock(buffer, classNode, region)) {
					try {
						new ClassVisitor(buffer, astNodeStack, contexts, region).visitClass(classNode);
					} catch (VisitCompleteException e) {
						break;
					}
				}
			}
		}
	}

//	/**
//	 * Find the class enclosing the given locations, this means inside the class { block }.
//	 * 
//	 * @param moduleNode
//	 * @param buffer
//	 * @param line
//	 * @param column
//	 * @return
//	 */
//	private ClassNode findEnclosingClass(ModuleNode moduleNode, ISourceBuffer buffer, IRegion region) {
//		List classNodes = moduleNode.getClasses();
//		if (classNodes.size() == 0) {
//			return null;
//		}
//		
//		for (Iterator iter = classNodes.iterator(); iter.hasNext(); ) {
//			ClassNode classNode = (ClassNode) iter.next();
//			if (ASTUtils.isEnclosingASTNode(classNode, region)
//					&& isInClassOrMethodBlock(buffer, classNode, region)) {
//				return classNode;
//			}
//		}
//		
//		return null;
//	}

	/**
	 * Fund a method node which contains the line/column. This is either a script method, or the 'run' method containing
	 * the script expressions.
	 * 
	 * @param buffer
	 * @param moduleNode
	 * @param line
	 * @param column
	 * @return
	 */
	private MethodNode findScriptMethod(ISourceBuffer buffer, ModuleNode moduleNode, IRegion region) {
		// Check the script methods first.
		List methodNodes = moduleNode.getMethods();
		if (methodNodes.size() > 0) {
			for (Iterator iter = methodNodes.iterator(); iter.hasNext();) {
				MethodNode methodNode = (MethodNode) iter.next();
				if (isEnclosingMethod(buffer, methodNode, region)) {
					return methodNode;
				}
			}
		}

		// Check the methods in classes next.
		List classNodes = moduleNode.getClasses();
		MethodNode runMethod = null;
		
		for (Iterator iter = classNodes.iterator(); iter.hasNext();) {
			ClassNode classNode = (ClassNode) iter.next();
			MethodNode methodNode = findEnclosingMethod(buffer, classNode, region);
			if (methodNode != null) {
				return methodNode;
			}
			
			// Tag the 'run' methods containing script expressions.
			if (runMethod == null) {
				List methods = classNode.getDeclaredMethods("run");
				if (methods.size() == 1) {
					runMethod = (MethodNode) methods.get(0);
				}
			}
		}
		
		return runMethod;
	}

	private MethodNode findEnclosingMethod(ISourceBuffer buffer, ClassNode classNode, IRegion region) {
		for (Iterator iter = classNode.getMethods().iterator(); iter.hasNext(); ) {
			MethodNode methodNode = (MethodNode) iter.next();
			if (isEnclosingMethod(buffer, methodNode, region)) {
				return methodNode;
			}
		}
		return null;
	}

	private boolean isEnclosingMethod(ISourceBuffer buffer, MethodNode methodNode, IRegion region) {
		return isEnclosingASTNode(methodNode, region.getOffset())
				&& isInClassOrMethodBlock(buffer, methodNode, region);
	}

	private boolean isInAnyMethodBlock(ISourceBuffer buffer, ClassNode classNode, IRegion region) {
		List declaredConstructors = classNode.getDeclaredConstructors();
		for (Iterator iter = declaredConstructors.iterator(); iter.hasNext();) {
			ASTNode node = (ASTNode) iter.next();
			if (isEnclosingASTNode(node, region.getOffset()) && isInClassOrMethodBlock(buffer, node, region)) {
				return true;
			}
		}

		List methods = classNode.getMethods();
		
		for (Iterator iter = methods.iterator(); iter.hasNext();) {
			MethodNode node = (MethodNode) iter.next();
			
			if (node.getName().equals("<clinit>")) {
				repairStaticInitializerCoords(buffer, node);
			}
			
			if (isEnclosingASTNode(node, region.getOffset()) && isInClassOrMethodBlock(buffer, node, region)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Static initializers have no line/column information, so add them into the AST.
	 * @param buffer
	 * @param node
	 * @param line
	 * @param column
	 * @return
	 */
	private void repairStaticInitializerCoords(ISourceBuffer buffer, MethodNode node) {
		ClassNode classNode = node.getDeclaringClass();
		int ixStart = buffer.toOffset(classNode.getLineNumber(), classNode.getColumnNumber());
		Pattern pattern = Pattern.compile("static\\s+\\{");
		Matcher matcher = pattern.matcher(buffer);
		boolean found = matcher.find(ixStart);
		if (found) {
    		ixStart = matcher.end() - 1;
    		int ixEnd = ixStart + 2;
    		int pairCount = 1;
    		
    		while (pairCount != 0 && ixEnd < buffer.length()) {
    			char ch = buffer.charAt(ixEnd++);
    			if (ch == '}') {
    				--pairCount;
    			} else if (ch == '{') {
    				++pairCount;
    			}
    		}
    		
    		int[] startCoord = buffer.toLineColumn(ixStart);
    		int[] endCoord = buffer.toLineColumn(ixEnd);
    		node.setLineNumber(startCoord[0]);
    		node.setColumnNumber(startCoord[1]);
    		node.setLastLineNumber(endCoord[0]);
    		node.setLastColumnNumber(endCoord[1]);
		}
	}

	private boolean isInModuleBody(ISourceBuffer buffer, ModuleNode moduleNode, IRegion region) {
		List classes = moduleNode.getClasses();
		for (Iterator iter = classes.iterator(); iter.hasNext();) {
			ClassNode node = (ClassNode) iter.next();
			if (isInClassOrMethodBlock(buffer, node, region)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * True if inside the nodes block, ie. {insidehere} but not including the braces.
	 * 
	 * @return True if in block, else false.
	 */
	private boolean isInClassOrMethodBlock(ISourceBuffer buffer, ASTNode node, IRegion region) {
		int nodeStartOffset = node.getStart();
		int nodeEndOffset = node.getEnd();
		int queryOffset = region.getOffset();
		try {
			while (buffer.charAt(nodeStartOffset++) != '{')
				;
			if (nodeStartOffset <= queryOffset && queryOffset < nodeEndOffset) {
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return false;
	}
	
	/**
	 * True if inside the nodes block, ie. {insidehere} but not including the braces. This method is for general block,
	 * as the end column is incorrect. It assumes the start is correct.
	 * 
	 * @return True if in block, else false.
	 */
	private boolean isInBlock(ISourceBuffer buffer, ASTNode node, IRegion region) {

		int nodeStartOffset = node.getStart();
		int nodeEndOffset = nodeStartOffset;
		int pairCount = 1;
		int queryOffset = region.getOffset();
		try {
			do {
				if (buffer.charAt(nodeEndOffset) == '{') {
					++pairCount;
				} else if (buffer.charAt(nodeEndOffset) == '}'){
					--pairCount;
				}
			} while (pairCount > 0 && ++nodeEndOffset < buffer.length());

			if (nodeStartOffset <= queryOffset && queryOffset < nodeEndOffset) {
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return false;
	}


	private boolean isInAnyMethodParameters(ISourceBuffer buffer, ClassNode classNode, IRegion region) {
		List declaredConstructors = classNode.getDeclaredConstructors();
		for (Iterator iter = declaredConstructors.iterator(); iter.hasNext();) {
			ASTNode node = (ASTNode) iter.next();
			if (isEnclosingASTNode(node, region.getOffset()) && isInMethodParameters(buffer, node, region)) {
				return true;
			}
		}

		List methods = classNode.getMethods();
		for (Iterator iter = methods.iterator(); iter.hasNext();) {
			ASTNode node = (ASTNode) iter.next();
			if (isEnclosingASTNode(node, region.getOffset()) && isInMethodParameters(buffer, node, region)) {
				return true;
			}
		}
		return false;
	}

	private boolean isEnclosingASTNode(ASTNode node, int offset) {
		return node.getStart() <= offset && node.getEnd() >= offset;
	}

	private boolean isInMethodParameters(ISourceBuffer buffer, ASTNode node, IRegion region) {
		int startOffset = buffer.toOffset(node.getLineNumber(), node.getColumnNumber());
		int queryOffset = region.getOffset();
		try {
			while (buffer.charAt(startOffset++) != '(')
				;
			int endOffset = startOffset;
			while (buffer.charAt(endOffset++) != ')')
				;
			if (startOffset <= queryOffset && queryOffset < endOffset) {
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return false;
	}
	
	
}
