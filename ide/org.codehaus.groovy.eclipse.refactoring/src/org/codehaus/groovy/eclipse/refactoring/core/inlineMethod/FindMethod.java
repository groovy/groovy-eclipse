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

package org.codehaus.groovy.eclipse.refactoring.core.inlineMethod;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.StatementFinder;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTVisitorDecorator;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.eclipse.jface.text.IDocument;

public class FindMethod {

	private final UserSelection selection;
	private final IDocument document;
	private final ModuleNode rootNode;
	protected ClassNode currentClass;
	protected MethodPattern selectedMethodPattern;
	protected List<MethodPattern> methodCalls, methodDefinitions;

	public FindMethod(UserSelection selection, IDocument document,
			ModuleNode rootNode) {
		this.selection = selection;
		this.document = document;
		this.rootNode = rootNode;

		findMethodsAndCalls();
	}

	private void findMethodsAndCalls() {
		methodCalls = new ArrayList<MethodPattern>();
		methodDefinitions = new ArrayList<MethodPattern>();

		FindSelectedCall findSelectedCall = new FindSelectedCall(this);

		scanDocument(findSelectedCall);
		if (selectedMethodPattern == null)
			findSelectedMethod();
				
		FindPatternMatchingCalls findMatchingCalls = new FindPatternMatchingCalls(this);
		scanDocument(findMatchingCalls);
		scanDocument(null);
	}
	
	private void findSelectedMethod() {
		if (rootNode != null) {
			for (ClassNode cl : (List<ClassNode>) rootNode.getClasses()) {
				for (MethodNode method : (List<MethodNode>) cl
						.getMethods()) {
					if(StatementFinder.testSelection(selection, method, document, false)) {
						selectedMethodPattern = new MethodPattern(method,cl,document);
					}
				}
			}
		}
	}

	private void scanDocument(CodeVisitorSupport visitor) {

		if (rootNode != null) {
			for (ClassNode cl : (List<ClassNode>) rootNode.getClasses()) {
				for (ConstructorNode method : (List<ConstructorNode>) cl
						.getDeclaredConstructors()) {
					scanMethod(visitor, cl, method);
				}
				for (MethodNode method : (List<MethodNode>) cl.getMethods()) {
					scanMethod(visitor, cl, method);
				}
			}
		}
	}

	private void scanMethod(CodeVisitorSupport visitor, ClassNode cl,
			MethodNode method) {
		currentClass = cl;
		if (visitor != null) {
			if (method.getCode() instanceof BlockStatement) {
				visitor.visitBlockStatement(((BlockStatement) method.getCode()));
			}
		} else {
			MethodPattern mp = new MethodPattern(method,cl,document);
			if (selectedMethodPattern != null && selectedMethodPattern.equals(mp))
				methodDefinitions.add(mp);
		}
	}

	public MethodPattern getSelectedMethodPattern() {
		return selectedMethodPattern;
	}

	public List<MethodPattern> getMethodCalls() {
		return methodCalls;
	}

	public List<MethodPattern> getMethodDefinitions() {
		return methodDefinitions;
	}

	/**
	 * Scans for Method Calls which includes the current selection
	 * 
	 * @author mklenk
	 * 
	 */
	private class FindSelectedCall extends ASTVisitorDecorator<FindMethod> {

		public FindSelectedCall(FindMethod finder) {
			super(finder);
		}

		@Override
        public void visitStaticMethodCallExpression(
				StaticMethodCallExpression call) {
			if (StatementFinder.testSelection(container.selection, call, container.document, false)) {
				container.selectedMethodPattern = new MethodPattern(call, container.document);
			}
			super.visitStaticMethodCallExpression(call);
		}

		@Override
        public void visitMethodCallExpression(MethodCallExpression call) {
			if (StatementFinder.testSelection(container.selection, call.getMethod(), container.document, false)) {
				container.selectedMethodPattern = new MethodPattern(call, container.document, container.currentClass);
			}
			super.visitMethodCallExpression(call);
		}
	}

	/**
	 * Collect all method calls which are equal to the selected one
	 * 
	 * @author mklenk
	 * 
	 */
	private class FindPatternMatchingCalls extends ASTVisitorDecorator<FindMethod> {

		public FindPatternMatchingCalls(FindMethod finder) {
			super(finder);
		}
		
		@Override
        public void visitStaticMethodCallExpression(
				StaticMethodCallExpression call) {
			MethodPattern statCall = new MethodPattern(call, container.document);
			if (container.selectedMethodPattern != null && container.selectedMethodPattern.equals(statCall))
				container.methodCalls.add(statCall);
			super.visitStaticMethodCallExpression(call);
		}

		@Override
        public void visitMethodCallExpression(MethodCallExpression call) {
			MethodPattern dynCall = new MethodPattern(call, container.document, container.currentClass);
			if (container.selectedMethodPattern != null && container.selectedMethodPattern.equals(dynCall))
				container.methodCalls.add(dynCall);
			super.visitMethodCallExpression(call);
		}
	}
	
}
