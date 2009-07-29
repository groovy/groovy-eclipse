/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils.patterns;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.StaticFieldImport;
import org.eclipse.jface.text.IDocument;

/**
 * 
 * Class represents a method as accurate as possible
 *
 */
public class MethodPattern {
	
	private ClassNode classType;
	private String methodName;
	private int argSize;
	
	private UserSelection position;
	private ASTNode node;
	private ArgumentListExpression arguments;
	
	public MethodPattern(StaticMethodCallExpression sc, IDocument document) {
		initStaticMethodCall(sc);
		if(ASTTools.hasValidPosition(sc)) {
			position = new UserSelection(sc,document);
		}
	}
	
	public MethodPattern(StaticMethodCallExpression sc){
		initStaticMethodCall(sc);
		position = new UserSelection(0,0);
	}

	private void initStaticMethodCall(StaticMethodCallExpression sc) {
		classType = sc.getOwnerType();
		methodName = sc.getMethod();
		Expression arguments = sc.getArguments();
		if (arguments instanceof ArgumentListExpression) {
			ArgumentListExpression args = (ArgumentListExpression) arguments;
			argSize = args.getExpressions().size();
			this.arguments = args;
		}
		node = sc;
	}
	
	public MethodPattern(MethodCallExpression mc, IDocument document, ClassNode currentClass) {
		Expression exp = mc.getObjectExpression();
		classType = ClassHelper.OBJECT_TYPE;
		determineClassTypeForMethodCalls(document, currentClass, exp);
		methodName = mc.getMethod().getText();
		Expression arguments = mc.getArguments();
		if (arguments instanceof ArgumentListExpression) {
			ArgumentListExpression args = (ArgumentListExpression) arguments;
			argSize = args.getExpressions().size();
			this.arguments = args;
		}
		
		if(ASTTools.hasValidPosition(mc)) {
			position = new UserSelection(mc,document);
		}
		node = mc;
	}

	private void determineClassTypeForMethodCalls(IDocument document,
			ClassNode currentClass, Expression exp) {
		if (exp instanceof ClassExpression) {
			ClassExpression clExp = (ClassExpression) exp;
			classType = clExp.getType();
		}
		if (exp instanceof VariableExpression) {
			classType = currentClass;
			VariableExpression varExp = (VariableExpression) exp;
			if(varExp.getAccessedVariable()!= null)
				classType = varExp.getAccessedVariable().getType();
		}
		if (exp instanceof MethodCallExpression) {
			MethodCallExpression mx = (MethodCallExpression) exp;
			classType = new MethodPattern(mx,document,currentClass).classType;
		}
	}
	
	public MethodPattern(MethodCallExpression mc) {
		initMethodCallExpression(mc);
	}

	private void initMethodCallExpression(MethodCallExpression mc) {
		classType = ClassHelper.OBJECT_TYPE;
		methodName = mc.getMethod().getText();
		Expression arguments = mc.getArguments();
		if (arguments instanceof ArgumentListExpression) {
			ArgumentListExpression args = (ArgumentListExpression) arguments;
			argSize = args.getExpressions().size();
			this.arguments = args;
		}
		
		if(ASTTools.hasValidPosition(mc)) {
			position = new UserSelection(0,0);
		}
		node = mc;
	}
	
	public MethodPattern(MethodNode method, ClassNode cl, IDocument document) {
		initMethodNodePattern(method, cl);
		if(ASTTools.hasValidPosition(method)) {
			position = new UserSelection(method,document);
		}
	}
	
	public MethodPattern(ASTNode node, IDocument document){
		determineTypeOfASTNode(node, document);
	}
	
	private void determineTypeOfASTNode(ASTNode node, IDocument document) {
		if(node instanceof MethodNode){
			MethodNode methNode = (MethodNode) node;
			initMethodNodePattern(methNode, methNode.getDeclaringClass());
		} else if(node instanceof MethodCallExpression){
			MethodCallExpression call = (MethodCallExpression) node;
			initMethodCallExpression(call);
			determineClassTypeForMethodCalls(document,ClassHelper.OBJECT_TYPE,call.getObjectExpression());
		} else if(node instanceof StaticMethodCallExpression){
			StaticMethodCallExpression staticCall = (StaticMethodCallExpression) node;
			initStaticMethodCall(staticCall);
		} else if(node instanceof StaticFieldImport){
			StaticFieldImport staticFieldImport = (StaticFieldImport) node;
			initStaticFieldImport(staticFieldImport);
		} else {
			throw new RuntimeException("Wrong Type of Node");
		}
	}

	private void initStaticFieldImport(StaticFieldImport staticFieldImport) {
		classType = staticFieldImport.getType();
		methodName = staticFieldImport.getField();
		argSize = 0;
		node = staticFieldImport;
	}
	
	public MethodPattern(MethodNode method){
		initMethodNodePattern(method, null);
		position = new UserSelection(0,0);
	}
	
	public MethodPattern(MethodNode method, ClassNode classnode){
		initMethodNodePattern(method, classnode);
		position = new UserSelection(0,0);
	}

	private void initMethodNodePattern(MethodNode method, ClassNode cl) {
		classType = cl;
		methodName = method.getName();
		argSize = method.getParameters().length;
		arguments = new ArgumentListExpression(method.getParameters());
		node = method;
	}

	@Override
    public String toString() {
		return classType.getName() + "." + methodName + "(" + argSize + ")";
	}
	
	@Override
    public boolean equals(Object obj) {
		if(obj instanceof MethodPattern) {
			MethodPattern t = (MethodPattern) obj;
			return(t.classType.equals(classType) && t.equalSignature(this));
		}
		return false;
	}
	
	public boolean equalSignature(Object obj){
		if(obj instanceof MethodPattern) {
			MethodPattern t = (MethodPattern) obj;
			return(t.methodName.equals(methodName) && t.argSize == argSize);
		}
		return false;
	}
	
	public UserSelection getPosition() {
		return position;
	}
	
	public ASTNode getNode() {
		return node;
	}
	
	public int getArgSize() {
		return argSize;
	}
	
	public ArgumentListExpression getArguments() {
		return arguments;
	}

	public String getMethodName() {
		return methodName;
	}

	public ClassNode getClassType() {
		return classType;
	}
	
}
