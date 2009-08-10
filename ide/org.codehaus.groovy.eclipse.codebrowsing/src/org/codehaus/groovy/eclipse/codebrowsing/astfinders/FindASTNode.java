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
package org.codehaus.groovy.eclipse.codebrowsing.astfinders;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.codebrowsing.SourceCodeFinder;
import org.codehaus.groovy.eclipse.core.util.ASTUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;

/**
 * Given an identifier and its location in the document, find the ASTNode that
 * corresponds to the editifier.
 * 
 * @author emp
 */
public class FindASTNode extends ClassCodeVisitorSupport {
	ModuleNode moduleNode;

	ClassNode classNode;

	private String identifier;

	private IRegion region;
	
	private IFile file;

	@Override
    protected SourceUnit getSourceUnit() {
		// Nothing to do.
		return null;
	}

	public void doFind(ModuleNode node, String identifier, IRegion region, IFile file)
			throws ASTNodeFoundException {
		moduleNode = node;
		this.identifier = identifier;
		this.region = region;
		this.file = file;

		List<ClassNode> classes = node.getClasses();

		for (ClassNode cNode : classes) {
			classNode = cNode;
			ClassNode superClassNode = classNode.getSuperClass();
			ClassNode[] interfaceNodes = classNode.getInterfaces();

			visitClass(superClassNode);
			for (int i = 0; i < interfaceNodes.length; ++i) {
				visitClass(interfaceNodes[i]);
			}

			visitClass(classNode);
			// classNode.visitContents(this);
		}
	}

	@Override
    public void visitFieldExpression(FieldExpression expr) {
		if (validCoords(expr) && identifier.equals(expr.getFieldName())) {
			System.out.println("Field: " + expr.getFieldName());
			testForMatch(expr);
		}
		super.visitFieldExpression(expr);
	}

	@Override
    public void visitField(FieldNode node) {
		if (validCoords(node)) {
		    ClassNode type = node.getType();
		    if (validCoords(type)) {
		        testForMatch(type);
		    }
		    testForMatch(node);
		}
		super.visitField(node);
	}

	@Override
    public void visitMethod(MethodNode node) {
        if (validCoords(node)) {
            ClassNode type = node.getReturnType();
            if (type != null && validCoords(type)) {
                testForMatch(type);
            }
            Parameter[] params = node.getParameters();
            for (Parameter param : params) {
                ClassNode pType = param.getType();
                if (pType != null && validCoords(pType)) {
                    testForMatch(pType);
                }
            }
        }
        super.visitMethod(node);
    }

	@Override
    public void visitPropertyExpression(PropertyExpression expr) {
		if (validCoords(expr)) {
			// Patch same last coord to length. This only seems to occur with
			// this.prop. Not strictly correct, as it should be the entire
			// expression to be consistent with other property expressions.
		    int propLength = expr.getPropertyAsString() == null ? 0 : expr.getPropertyAsString().length();
			if (expr.getLastLineNumber() == expr.getLineNumber()) {
				expr.setLastLineNumber(expr.getLineNumber()
						+ propLength);
			}

			if (expr.getLastColumnNumber() == expr.getColumnNumber()) {
				expr.setLastColumnNumber(expr.getColumnNumber()
						+ propLength);
			}

			if (identifier.equals(expr.getProperty())) {
				System.out.println("Property: " + expr.getProperty());
				testForMatch(expr);
			} else if (expr.getObjectExpression() instanceof ClassExpression) {
				patchClassExpressionLineColumn(expr, expr.getObjectExpression());
			}
		}
		super.visitPropertyExpression(expr);
	}

	@Override
    public void visitVariableExpression(VariableExpression expr) {
		// Patch odd zero last coords.
		if (expr.getLastLineNumber() == 0) {
			expr.setLastLineNumber(expr.getLineNumber()
					+ expr.getName().length());
		}

		if (expr.getLastColumnNumber() == 0) {
			expr.setLastColumnNumber(expr.getColumnNumber()
					+ expr.getName().length());
		}

		// First visit the parent, else a case like:
		// def insets = parent.insets
		// will match the 'insets' of 'parent.insets' to the 'def insets'.
		// TODO: how to visit rhs first?
		if (validCoords(expr)) {
			if (identifier.equals(expr.getName())
					|| identifier
							.equals(expr.getType().getNameWithoutPackage())) {
				System.out.println("Variable: " + expr.getName());
				testForMatch(expr);
			}
		}
	}
	
	@Override
	public void visitConstantExpression(ConstantExpression expr) {
	    if (validCoords(expr)) {
            if (identifier.equals(expr.getValue())
                    || identifier
                            .equals(expr.getType().getNameWithoutPackage())) {
                System.out.println("Variable: " + expr.getValue());
                testForMatch(expr);
            }
        }
	}
	
	@Override
	public void visitDeclarationExpression(DeclarationExpression expression) {
	    ClassNode type = expression.getLeftExpression().getType();
	    if (validCoords(type)) {
	        testForMatch(type);
	    }
	    
	    super.visitDeclarationExpression(expression);
	}
	

	@Override
    public void visitMethodCallExpression(MethodCallExpression call) {
		if (validCoords(call)) {
			if (identifier.equals(call.getMethod().getText())) {
				System.out.println("Method call: " + call.getMethod());
				// Strange - columns can be equal sometimes
				if (call.getLineNumber() == call.getLastLineNumber()
						&& call.getColumnNumber() == call.getLastColumnNumber()) {
					call.setColumnNumber(call.getColumnNumber()
							- identifier.length());
				}
				testForMatch(call);
			} else if (call.getObjectExpression() instanceof ClassExpression) {
				patchClassExpressionLineColumn(call, call.getObjectExpression());
			}
		}
		super.visitMethodCallExpression(call);
	}

	/**
	 * DELETEME 
	 * 
	 * @param node
	 * @param expr
	 */
	private void patchClassExpressionLineColumn(ASTNode node, Expression expr) {
	}

	@Override
    public void visitClassExpression(ClassExpression expr) {
		if (validCoords(expr)) {
			System.out.println("Class expr: " + expr.getText());
			testForMatch(expr);
		}
		super.visitClassExpression(expr);
	}

	@Override
    public void visitClass(ClassNode node) {
		// TODO: find the real "class Blah" length
		if (validCoords(node) && ASTUtils.isInsideNode(node, region.getOffset())) {
			// TODO: want qualified identifier.
			if (identifier.equals(node.getNameWithoutPackage())) {
				throw new ASTNodeFoundException(moduleNode, classNode, node,
						identifier, region);
			} else if (node.getSuperClass() != null &&
					identifier.equals(node.getSuperClass()
					.getNameWithoutPackage())) {
				throw new ASTNodeFoundException(moduleNode, classNode, node
						.getSuperClass(), identifier, region);
			} else {
				ClassNode[] interfaceNodes = node.getInterfaces();
				for (int i = 0; i < interfaceNodes.length; ++i) {
					if (identifier.equals(interfaceNodes[i]
							.getNameWithoutPackage())) {
						throw new ASTNodeFoundException(moduleNode, classNode,
								interfaceNodes[i], identifier, region);
					}
				}
				
				for (Iterator<ImportNode> iterator = moduleNode.getImports().iterator();
						iterator.hasNext();) {
					ImportNode importNode = iterator.next();
					if (identifier.equals(importNode.getAlias())) {
						throw new ASTNodeFoundException(moduleNode, classNode,
								importNode.getType(), identifier, region);
					}
				}
				
				for (Iterator<String> iterator = moduleNode.getImportPackages().iterator();
						iterator.hasNext();) {
					String packageName = iterator.next();
					ClassNode possibleClassNode = ClassHelper.makeWithoutCaching(packageName + identifier);
					if (SourceCodeFinder.find(possibleClassNode, file) != null) {
						throw new ASTNodeFoundException(moduleNode, classNode,
								possibleClassNode, identifier, region);
					}
				}
			}
		}
		super.visitClass(node);
	}

	@Override
    public void visitMethodPointerExpression(MethodPointerExpression expr) {
		if (validCoords(expr) && identifier.equals(expr.getMethodName())) {
			System.out.println("Method Pointer: " + expr.getMethodName());
			testForMatch(expr);
		}
		super.visitMethodPointerExpression(expr);
	}

	@Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
		if (validCoords(call) && identifier.equals(call.getMethod())) {
			System.out.println("Static method call: " + call.getMethod());
			testForMatch(call);
		}
		super.visitStaticMethodCallExpression(call);
	}

	private boolean validCoords(ASTNode node) {
		boolean ret = node.getLineNumber() > 0 && node.getColumnNumber() > 0;
		if (ret) {
			printNodeText(node);
		}
		return ret;
	}

	private void printNodeText(ASTNode node) {
		System.out.println("Text [" + node.getLineNumber() + ","
				+ node.getColumnNumber() + "]: " + node.getText());
	}

	private void testForMatch(ASTNode astNode) {
		if (ASTUtils.isInsideNode(astNode, region.getOffset())) {
			throw new ASTNodeFoundException(moduleNode, classNode, astNode,
					identifier, region);
		}
	}
}