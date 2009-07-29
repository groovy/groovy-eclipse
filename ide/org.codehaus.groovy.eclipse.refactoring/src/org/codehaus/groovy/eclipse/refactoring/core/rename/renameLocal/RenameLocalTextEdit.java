/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import java.util.List;
import java.util.Map;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEdit;
import org.codehaus.groovy.eclipse.refactoring.core.utils.EditHelper;
import org.eclipse.jface.text.IDocument;

/**
 * 
 * Scans a method or a statementblock to find the variable to rename. All variable that have their
 * accessed variable set to the oldVariable are affected. So the compare is based on the references 
 * instead of the names.
 * 
 * In case the scanner is called from Extract Method or Inline Method the rename are based on the
 * renameMap. <oldName><newName>
 * 
 * @author martin
 *
 */
public class RenameLocalTextEdit extends RenameTextEdit {
	
	private final MethodNode renameLocalMethod;
	private BlockStatement script;
	//must be the Variable of the declaration
	private VariableProxy oldVariable;
	private Map<String,String> renameMap = null;

	/*
	 * Constructor used for "Rename Local" refactoring
	 */
	public RenameLocalTextEdit(IGroovyDocumentProvider docProvider,
			VariableProxy oldVariable, MethodNode renameLocalMethod, String newName) {
		super(docProvider,oldVariable.getName(),newName);
		this.oldVariable = oldVariable;
		this.renameLocalMethod = renameLocalMethod;
		//Variable to rename is in a script
		if (renameLocalMethod == null) {
			script = getRootNode().getStatementBlock();
		}
	}
	
	/*
	 * Constructor used for rename the variable for the "Extract Method" and "Inline Method" refactoring
	 */
	public RenameLocalTextEdit(ModuleNode root, IDocument document, String methodName, Map<String,String> renameMap) {
		super(root, document);
		this.renameMap = renameMap;
		renameLocalMethod = getMethodNode(getRootNode(), methodName);
		//Variables to rename are in a script
		if (renameLocalMethod == null) {
			script = getRootNode().getStatementBlock();
		}
	} 
	
	private MethodNode getMethodNode(ModuleNode root, String name) {
		List<MethodNode> methods = root.getMethods();
		for (MethodNode method :  methods) {
			if (method.getName().equals(name)) {
				return method;
			}
		}
		return null;
	}
	
	@Override
    public void scanAST() {
		if (renameLocalMethod != null) {
			visitMethod(renameLocalMethod);
		} else {
			//the variable is defined somewhere in: script, Closure, ForLoop
			script.visit(this);
		}
	}	
		
	@Override
    public void analyzeParameter(Parameter parameter) {
		VariableProxy variable = new VariableProxy(parameter);
		variable.setSourcePosition(parameter);
		if (isNodeToRename(parameter)) {
			edits.addChild(EditHelper.getVariableProxyReplaceEdit(variable,  document, newName));
		}
	}
	
	@Override
    public void visitVariableExpression(VariableExpression expression) {
		VariableProxy variable = new VariableProxy(expression);
		variable.setSourcePosition(expression);
		if (isNodeToRename(expression)) {
			edits.addChild(EditHelper.getVariableProxyReplaceEdit(variable, document, newName));
		} 
	}
		
	private boolean isNodeToRename(Variable variable) {
		if (renameMap == null) {
			if (variable instanceof VariableExpression) {
				return oldVariable.getVariable() == ((VariableExpression)variable).getAccessedVariable();
			}
            return oldVariable.getVariable() == variable;
		}
        //is node to rename?
        String newName = renameMap.get(variable.getName());
        if (newName != null) {
        	setNewName(newName);
        	return true;
        }
        return false;
	}
	

	

}
