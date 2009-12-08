/* 
 * Copyright (C) 2007, 2008 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.Checks;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.JavaModelSearch;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.RenameLocalProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.VariableProxy;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTNodeInfo;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTScanner;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.NodeNotFoundException;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.RenameSelectionInfoPredicate;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Collects Groovy and Java Candidates
 * @author Stefan Reinhard
 *
 */
public class CandidateCollector {
	
	protected IGroovyDocumentProvider docProvider;
	protected UserSelection selection;
	
	private ASTNode selectedASTNode;
	private ClassNode parentClass;
	private ClassNode renameClassNode;
	private FieldPattern renameFieldNode;
	private MethodPattern renameMethodPattern;
	private VariableProxy renameLocalNode;
	
	private IJavaProject project;
	
	public CandidateCollector(IGroovyDocumentProvider docProvider, UserSelection selection) {
		this.docProvider = docProvider;
		this.selection = selection;
		selectedASTNode = getSelectedNode();
		renameClassNode = RenameClassProvider.giveClassNodeToRename(selectedASTNode);
		renameFieldNode = RenameFieldProvider.giveFieldNodeToRename(selectedASTNode);
		renameMethodPattern = RenameMethodProvider.giveMethodNodeToRename(selectedASTNode,docProvider, parentClass);
		renameLocalNode = RenameLocalProvider.giveVariableExpressionToRename(selectedASTNode);
		if (docProvider.getFile() != null) {
			IProject sourceProject = docProvider.getFile().getProject();
			project = JavaCore.create(sourceProject);
		}
	}	
	
	/*
	 * Java dispatch from here
	 */

	public IJavaElement[] getJavaCandidates() throws CoreException {
		List<IJavaElement> result = new LinkedList<IJavaElement>();
		if(renameClassNode != null) {
			IType type = searchType();
			if (type != null && !type.isBinary()) result.add(type);
		} else if (renameFieldNode != null) {
			if (!isFieldDeclaration()) {
				result.addAll(createFieldList());
			}
		} else if (renameMethodPattern != null) {
			if (!isMethodDeclaration()) {	
				result.addAll(createMethodList());
			}
		}
		return result.toArray(new IJavaElement[result.size()]);
	}
	
	private IType searchType() throws CoreException {
//		SearchPattern pattern = SearchPattern.createPattern(
//				renameClassNode.getName(), 
//				IJavaSearchConstants.TYPE,
//				IJavaSearchConstants.DECLARATIONS,
//				SearchPattern.R_EXACT_MATCH);
//		JavaModelSearch search = new JavaModelSearch(project, pattern);
		return project.findType(renameClassNode.getName());
	}
	
	private boolean isFieldDeclaration() {
		ASTNode node = renameFieldNode.getSelectedASTNode();
		return node != null && node instanceof FieldNode;
	}

	private List<IField> createFieldList() throws CoreException {
		List<IField> result = new LinkedList<IField>();
		ClassNode declaring = renameFieldNode.getDeclaringClass();
		if (declaring == null) return result;
		boolean isDynamic = declaring.equals(new ClassNode(Object.class));
		if (!isDynamic) {
			IField field = searchJavaField();
			if (Checks.isRefactorable(field)) result.add(field);
		} else {
			List<IField> fields = searchAllJavaFields();
			List<IField> filtered = filterPossibleFields(fields);
			result.addAll(filtered);
		}
		return result;
	}
	
	private List<IField> filterPossibleFields(List<IField> list) {
		LinkedList<IField> filtered = new LinkedList<IField>();
		for(IField field : list) {
			if (Checks.isRefactorable(field))
				filtered.add(field);
		}
		return filtered;
	}
	
	private IField searchJavaField() throws CoreException {
		SearchPattern pattern = SearchPattern.createPattern(
				renameFieldNode.getFullyQualifiedName(), 
				IJavaSearchConstants.FIELD,
				IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_EXACT_MATCH);
		JavaModelSearch search = new JavaModelSearch(project, pattern);
		return search.searchFirst(IField.class);
	}
	
	private List<IField> searchAllJavaFields() throws CoreException {
		SearchPattern pattern = SearchPattern.createPattern(
				renameFieldNode.getName(), 
				IJavaSearchConstants.FIELD,
				IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_EXACT_MATCH);
		JavaModelSearch search = new JavaModelSearch(project, pattern);
		return search.searchAll(IField.class);
	}
	
	private boolean isMethodDeclaration() {
		ASTNode node = renameMethodPattern.getNode();
		return node != null && node instanceof MethodNode;
	}
	
	private List<IMethod> createMethodList() throws CoreException {
		String methodPattern = getMethodSearchPattern();
		List<IMethod> elements = searchJavaMethods(methodPattern);
		return filterMethodList(elements);
	}
	
	private List<IMethod> searchJavaMethods(String methodPattern) throws CoreException {
		SearchPattern pattern = SearchPattern.createPattern(methodPattern, 
				IJavaSearchConstants.METHOD,
				IJavaSearchConstants.ALL_OCCURRENCES, 
				SearchPattern.R_EXACT_MATCH);
		JavaModelSearch search = new JavaModelSearch(project, pattern);
		return search.searchAll(IMethod.class);
	}
	
	private List<IMethod> filterMethodList(List<IMethod> elements) {
		List<IMethod> methodList = new LinkedList<IMethod>();
		for (IJavaElement element : elements) {
			if (element instanceof IMethod) {
				IMethod method = (IMethod)element;
				
				int argSize = method.getNumberOfParameters();
				if (renameMethodPattern.getMethodName().equals(method.getElementName()) &&
						renameMethodPattern.getArgSize() == argSize &&
							Checks.isRefactorable(method)) {
					methodList.add(method);
				}
			} else {
				System.out.println(element);
			}
		}
		return methodList;
	}
	
	private String getMethodSearchPattern() {
		StringBuilder b = new StringBuilder();
		b.append(renameMethodPattern.getMethodName());
		return b.toString();
	}
	
	/*
	 * Groovy dispatch from here
	 */
	public ASTNode[] getGroovyCandidates() {
		List<ASTNode> result = new LinkedList<ASTNode>();
		if (renameClassNode != null && (renameClassNode.isPrimaryClassNode() || renameClassNode.redirect() instanceof JDTClassNode)) {
			result.add(renameClassNode);
		} else if (renameLocalNode != null) {
			result.add(renameLocalNode);
		} else if (renameFieldNode != null) {
			result.addAll(searchFieldDefinitons());
		} else if (renameMethodPattern != null) {
			result.addAll(searchMethodDefinitions());
		}
		return result.toArray(new ASTNode[result.size()]);
	}

	public List<FieldNode> searchFieldDefinitons() {
		List<FieldNode> fieldList = new LinkedList<FieldNode>();
		for (IGroovyDocumentProvider d : getWSFileProvider().getAllSourceFiles()) {
			FieldDefinitionCollector collector = new FieldDefinitionCollector(d.getRootNode(), renameFieldNode);
			collector.scanAST();
			fieldList.addAll(collector.getFieldDefinitions());
		}
		return fieldList;
	}
	
	private List<MethodNode> searchMethodDefinitions() {
		List<MethodNode> methodList = new LinkedList<MethodNode>();
		for (IGroovyDocumentProvider doc : getWSFileProvider().getAllSourceFiles()) {
			MethodDefinitionCollector collector = new MethodDefinitionCollector(doc.getRootNode(), renameMethodPattern);
			collector.scanAST();
			methodList.addAll(collector.getMethodDefinitions());
		}
		return methodList;
	}

	public ASTNode getSelectedNode() {
		RenameSelectionInfoPredicate renameSelectionInfoPredicate = new RenameSelectionInfoPredicate(selection,docProvider.getDocument());
		ASTScanner infoBuilder = new ASTScanner(docProvider.getRootNode(), renameSelectionInfoPredicate,docProvider.getDocument());
		infoBuilder.startASTscan();
		
		//get the tightest selection, inner most node 
		ASTNode currentCandidate = null;
		for (Entry<ASTNode, ASTNodeInfo> entry : infoBuilder.getMatchedNodes().entrySet()){
			ASTNode key = entry.getKey();
			if(currentCandidate == null){
				currentCandidate = key;
			} else { 
				//test if node's startpoint is after selectedNode's startpoint
				SourceCodePoint nodeStartPoint = new SourceCodePoint(key,SourceCodePoint.BEGIN);
				SourceCodePoint selectedNodeStartPoint = new SourceCodePoint(currentCandidate,SourceCodePoint.BEGIN);
				if (nodeStartPoint.isAfter(selectedNodeStartPoint) || key instanceof FieldExpression) {
						currentCandidate = key;
				}
			}
		}
		//Used for rename method, to find out where the method is defined
		if (currentCandidate != null) {
		    findParentClass(infoBuilder, currentCandidate);
		}
		
		//Dispatch on a ConstantExpression is not possible -> the parent can be a PropertyExpression,
		//AttributeExpression or MethodCallExpression
		if(currentCandidate instanceof ConstantExpression){
			currentCandidate = infoBuilder.getInfo(currentCandidate).getParent();
		}
		
		//A FieldExpression has just a reference on the accessed field. To be able to dispatch we must
		//separate the expression into a ClassNode or simply leave it as FieldExpression
		if(currentCandidate instanceof FieldExpression){
			FieldExpression fieldExpr = (FieldExpression) currentCandidate;
			String className = fieldExpr.getField().getDeclaringClass().getNameWithoutPackage();
			SourceCodePoint start = new SourceCodePoint(currentCandidate,SourceCodePoint.BEGIN);
			int offsetToClass = start.getOffset(docProvider.getDocument());
			int selectionEndOffset = selection.getOffset() + selection.getLength();
			int classEndOffset = offsetToClass + className.length();
			if (selection.getOffset() >= offsetToClass && (selectionEndOffset  <= classEndOffset)) {
				currentCandidate = fieldExpr.getField().getDeclaringClass();
			}
		}
		return currentCandidate;
	}

	private void findParentClass(ASTScanner infoBuilder, ASTNode startNode) {
	    ModuleNode module = docProvider.getRootNode();
	    for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
            if (clazz.getStart() <= startNode.getStart() && clazz.getEnd() >= startNode.getEnd()) {
                parentClass = clazz;
                break;
            }
        }
	}
	
	protected IGroovyFileProvider getWSFileProvider() {
	    // FIXADE RC1 create a CompilationUnitFileProvider
		return new WorkspaceFileProvider(new WorkspaceDocumentProvider(docProvider.getFile()));
	}
	
}
