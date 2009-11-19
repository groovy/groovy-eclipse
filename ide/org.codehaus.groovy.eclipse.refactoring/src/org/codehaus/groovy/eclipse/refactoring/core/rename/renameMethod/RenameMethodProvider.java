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
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.MultiFileRefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyNode;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyTreeBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyConventionsBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * 
 * Provider Class for the Refactoring "Rename Method"
 *
 */
public class RenameMethodProvider extends MultiFileRefactoringProvider implements IRenameProvider {
	
	private static final String THIS = "this.";
	
	private final MethodPattern selectedMethodPattern;
	private String newMethodName;
	private int nrOfMethodDefinitions;
	private MethodNode relevantMethodDefintion;
	private IGroovyDocumentProvider documentOfMethodDefinition;

	protected List<RenameTextEditProvider> textEditProviders = new ArrayList<RenameTextEditProvider>();

	public RenameMethodProvider(IGroovyFileProvider docProvider, MethodPattern selectedNode) {
		super(docProvider);
		this.selectedMethodPattern = selectedNode;
		this.selectedASTNode = selectedMethodPattern.getNode();
	}
	
	public RenameMethodProvider(IGroovyFileProvider docProvider, UserSelection selection, MethodPattern selectedNode) {
		this(docProvider, selectedNode);
		setSelection(selection);
	}
	
	@Override
    protected void prepareCandidateLists() {
		
		// FIXME: Why do candidates need to be collected several times?
		// This is a small workaround to fix it, but maybe dangerous
		if (hasCandidateLists()) return;
		
		textEditProviders = new ArrayList<RenameTextEditProvider>();
		definitiveCandidates = new HashMap<IGroovyDocumentProvider, List<ASTNode>>();
		ambiguousCandidates = new HashMap<IGroovyDocumentProvider, List<ASTNode>>();
		
		for(IGroovyDocumentProvider document : getUsedDocuments()){
			getAllCandidatesInDocument(document);
		}
		
		removeCandidatesWithInvalidPositions();
		
		
		
		//We have an exact pattern that contains a class, this 
		//fact allows us to eliminate some candidates
		if(!selectedMethodPattern.getClassType().getName().equals(JAVA_LANG_OBJECT)){
			removeImpossibleCandidates();
		}
		
		//Some candidates are ambiguous and will therefore
		//be moved to the list of ambiguous candidates
		moveAmbiguousCandidates(ambiguousCandidates);
		
		//If called programmatically
		if (selectedASTNode == null && !definitiveCandidates.values().isEmpty()) {
			for(List<ASTNode> list : definitiveCandidates.values()) {
				for(ASTNode node : list) {
					selectedASTNode = node;
					continue;
				}
			}	
		}
		
		checkUniqueMethodDefinitions();
		
		moveSelectionToDefinitiveCandidates();
		
		//interface handling:
		//if selected method is defined in a interface, all
		//classes that implement the same interface should rename their methods too
		handleMethodsOfRelatedClasses();
	}
	
	private void handleMethodsOfRelatedClasses() {
		if(selectedASTNode instanceof MethodNode){
			MethodNode methodNode = (MethodNode) selectedASTNode;
			ClassNode declaringClass = methodNode.getDeclaringClass();
			HierarchyTreeBuilder treebuilder = new HierarchyTreeBuilder(fileProvider);
			Map<String, HierarchyNode> interconnectedClasses = treebuilder.getInterconnectedClasses(declaringClass);
			Set<ClassNode> relatedClassNodes = new HashSet<ClassNode>();
			for(HierarchyNode nodeInHiearachy : interconnectedClasses.values()){
				relatedClassNodes.add(nodeInHiearachy.getOriginClass());
			}
			//iterate over ambiguous candidates and put them to the definitive list
			addRelatedMethodsToDefinitveCandidates(relatedClassNodes);
		}
	}

	private void addRelatedMethodsToDefinitveCandidates(Set<ClassNode> relatedClassNodes) {
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : ambiguousCandidates.entrySet()){
			for(ASTNode node : entry.getValue()){
				if(node instanceof MethodNode){
					MethodNode methodCandidate = (MethodNode) node;
					ClassNode candidateClass = methodCandidate.getDeclaringClass();
					if(relatedClassNodes.contains(candidateClass)){
						addNodeToACandidateListList(definitiveCandidates, entry.getKey(), methodCandidate);
					}
				}
			}
		}
	}

	private void checkUniqueMethodDefinitions() {
		nrOfMethodDefinitions = 0;
		relevantMethodDefintion = null;
		documentOfMethodDefinition = null;
		checkMapForMethodDefinitions(definitiveCandidates);
		checkMapForMethodDefinitions(ambiguousCandidates);
		
		if(nrOfMethodDefinitions == 1){
			addNodeToACandidateListList(definitiveCandidates, documentOfMethodDefinition, relevantMethodDefintion);
			if(ambiguousCandidates.get(documentOfMethodDefinition) != null){
				ambiguousCandidates.get(documentOfMethodDefinition).remove(relevantMethodDefintion);
			}
		}
	}

	private void checkMapForMethodDefinitions (Map<IGroovyDocumentProvider, List<ASTNode>> map) {
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : map.entrySet()){
			for(ASTNode node : entry.getValue()){
				if(node instanceof MethodNode) {
					nrOfMethodDefinitions++;
					relevantMethodDefintion = (MethodNode) node;
					documentOfMethodDefinition = entry.getKey();
				}
			}
		}
	}
	
	private void setProviders(){
		for(IGroovyDocumentProvider document : getUsedDocuments()){
			List<ASTNode> list = definitiveCandidates.get(document);
			if(list != null){
				textEditProviders.add(new RenameMethodTextEditProvider(
						selectedMethodPattern, newMethodName, document, list));
			}
		}
	}
	
	private void removeImpossibleCandidates() {
		Map<IGroovyDocumentProvider, List<ASTNode>> toDelete = new HashMap<IGroovyDocumentProvider, List<ASTNode>>();
		
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : definitiveCandidates.entrySet()){
			for(ASTNode node : entry.getValue()){
				if(node instanceof MethodNode){
					MethodNode methNode = (MethodNode) node;
					String classNameOfCurrentNode = methNode.getDeclaringClass().getName();
					String classNameOfNodeSelectedByUser = selectedMethodPattern.getClassType().getName();

					boolean nodeHasDifferentClass = !classNameOfCurrentNode.equals(classNameOfNodeSelectedByUser);
					if(classNameOfCurrentNode != JAVA_LANG_OBJECT && nodeHasDifferentClass){
						if(!classIsRelatedToSelectedClass(methNode.getDeclaringClass())){
							addNodeToACandidateListList(toDelete, entry.getKey(), node);
						}
					}
				} else if(node instanceof MethodCallExpression){
					MethodCallExpression methCall = (MethodCallExpression) node;
					MethodPattern patternOfMethodCall = new MethodPattern(methCall, entry.getKey().getDocument(), ClassHelper.OBJECT_TYPE);
					verifyMethodCall(toDelete, entry, node, patternOfMethodCall);
				} else if(node instanceof StaticMethodCallExpression){
					StaticMethodCallExpression staticMethCall = (StaticMethodCallExpression)node;
					MethodPattern patternOfStaticMethCall = new MethodPattern(staticMethCall);
					verifyMethodCall(toDelete, entry, node, patternOfStaticMethCall);
				}
			}
		}
		
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> nodeToDelete : toDelete.entrySet()){
			for(ASTNode astNodeToDelete : nodeToDelete.getValue()){
				definitiveCandidates.get(nodeToDelete.getKey()).remove(astNodeToDelete);
				
				List<ASTNode> ambiguousCandids = ambiguousCandidates.get(nodeToDelete.getKey());
				if(ambiguousCandids != null){
					ambiguousCandids.remove(astNodeToDelete);
				}
			}
		}
	}

	private boolean classIsRelatedToSelectedClass(ClassNode candidate) {
		if(selectedASTNode instanceof MethodNode){
			MethodNode methodNodeSelectedByUser = (MethodNode)selectedASTNode;
			HierarchyTreeBuilder treebuilder = new HierarchyTreeBuilder(fileProvider);
			Map<String, HierarchyNode> interconnectedClasses = treebuilder.getInterconnectedClasses(methodNodeSelectedByUser.getDeclaringClass());
			Set<ClassNode> relatedClassNodes = new HashSet<ClassNode>();
			for(HierarchyNode nodeInHiearachy : interconnectedClasses.values()){
				relatedClassNodes.add(nodeInHiearachy.getOriginClass());
			}
			for(ClassNode node : relatedClassNodes){
				if(node.getName().equals(candidate.getName())){
					return true;
				} 
			}
		}
		return false;
	}

	private void verifyMethodCall(Map<IGroovyDocumentProvider, List<ASTNode>> toDelete,
			Entry<IGroovyDocumentProvider, List<ASTNode>> entry, ASTNode node, MethodPattern patternOfMethodCall) {
		String nameOfCurrentNode = patternOfMethodCall.getClassType().getName();
		String nameOfNodeSelectedByUser = selectedMethodPattern.getClassType().getName();
		boolean nodeHasDifferentClass = !nameOfCurrentNode.equals(nameOfNodeSelectedByUser);
		if(nameOfCurrentNode != JAVA_LANG_OBJECT && nodeHasDifferentClass){
			addNodeToACandidateListList(toDelete, entry.getKey(), node);
		}
	}

	private void moveAmbiguousCandidates(
			Map<IGroovyDocumentProvider, List<ASTNode>> doubtfulCandidates) {
		
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : definitiveCandidates.entrySet()){
			for(ASTNode node : entry.getValue()){
				
				//ignore the astNode selected by the user
				if(node == selectedASTNode){
					continue;
				} 
				
				//ignore the exact matches
				MethodPattern patternOfCurrentNode = new MethodPattern(node, getDocumentProvider().getDocument());
				if(		patternOfCurrentNode.equals(selectedMethodPattern) && 
						!patternOfCurrentNode.getClassType().getName().equals(JAVA_LANG_OBJECT)){
					continue;
				} 
				
				//ignore exact matches in related interfaces
				if(patternOfCurrentNode.equalSignature(selectedMethodPattern) &&
						classIsRelatedToSelectedClass(patternOfCurrentNode.getClassType())){
					continue;
				}
				
				if(node instanceof MethodCallExpression){
					if(((MethodCallExpression)node).getText().startsWith(THIS)){
						continue;
					}
				}
				
				addNodeToACandidateListList(doubtfulCandidates, entry.getKey(), node);
			}
		}
		
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : doubtfulCandidates.entrySet()){
			for(ASTNode listElement : entry.getValue()){
				definitiveCandidates.get(entry.getKey()).remove(listElement);
			}
		}
	}

	private void getAllCandidatesInDocument(IGroovyDocumentProvider currentDocument) {
		RenameMethodCandidatesCollector candiateCollector = new RenameMethodCandidatesCollector(currentDocument.getRootNode(), selectedMethodPattern, true);
		candiateCollector.scanAST();
		
		for(ASTNode currentCandid: candiateCollector.getCandidates()){
			if(!definitiveCandidates.containsKey(currentDocument)){
				definitiveCandidates.put(currentDocument, new LinkedList<ASTNode>());
			}
			definitiveCandidates.get(currentDocument).add(currentCandid);
		}
	}

	@Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus refactoringStatus = new RefactoringStatus();
		addDefaultParamWarning(refactoringStatus);
		return checkForDuplicates(refactoringStatus);
	}

	private void addDefaultParamWarning(RefactoringStatus refactoringStatus) {
		List<ASTNode> allMethodNodesWithSameName = new LinkedList<ASTNode>();
		for(IGroovyDocumentProvider document : fileProvider.getAllSourceFiles()){
			RenameMethodCandidatesCollector candiateCollector = new RenameMethodCandidatesCollector(document.getRootNode(), selectedMethodPattern, false);
			candiateCollector.scanAST();
			allMethodNodesWithSameName.addAll(candiateCollector.getCandidates());
		}
		
		for(ASTNode node : allMethodNodesWithSameName){
			if(node instanceof MethodNode){
				MethodNode methNode = (MethodNode) node;
				for(Parameter param : methNode.getParameters()){
					if(param.hasInitialExpression()){
						refactoringStatus.addWarning(GroovyRefactoringMessages.RenameMethod_DefaultParamsUsed);
						break;
					}
				}
			}
		}
	}

	private RefactoringStatus checkForDuplicates(RefactoringStatus refactoringStatus) {
		List<MethodPattern> listOfAllUsedMethods = new LinkedList<MethodPattern>();
		HierarchyTreeBuilder classTreeBuilder = new HierarchyTreeBuilder(fileProvider);
		
		for(HierarchyNode node : classTreeBuilder.getCompleteClassStructure().values()){
			for(Object methodnode : node.getOriginClass().getMethods()){
				MethodNode mnode = (MethodNode) methodnode;
				listOfAllUsedMethods.add(new MethodPattern(mnode));
			}
		}
		
		for(MethodPattern pattern : listOfAllUsedMethods){
			boolean sameNrOfArgumens = pattern.getArgSize() == selectedMethodPattern.getArgSize();
			if(pattern.getMethodName().equals(newMethodName) && sameNrOfArgumens){
				refactoringStatus.addWarning(MessageFormat.format(
						GroovyRefactoringMessages.RenameMethod_VariableAlreadyExists,newMethodName));
				break;
			}
		}
		
		return refactoringStatus;
	}
	
	@Override
    public void addInitialConditionsCheckStatus(RefactoringStatus status) {
		//check if the method can be renamed
		//no conditions yet
	}

	@Override
    public GroovyChange createGroovyChange(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		//Iteration over all documents in the workspace to look, where the method to rename is used
		GroovyChange change = new GroovyChange(GroovyRefactoringMessages.RenameMethodRefactoring);
		for (RenameTextEditProvider textEditProvider : textEditProviders) {
			MultiTextEdit multi = removeDublicatedTextedits(textEditProvider);
			change.addEdit(textEditProvider.getDocProvider(), multi);
		}

		/*
		 * Would also look for java method, that are affected. It's just a prototype.
		 */
//		change.addChange(getJavaMethodChange(pm));
		return change;
	}
//	private Change getJavaMethodChange(IProgressMonitor pm) {
//		StringBuilder stringPattern = new StringBuilder();
//		stringPattern.append(selectedMethodPattern.getClassType().getName());
//		stringPattern.append(".");
//		stringPattern.append(selectedMethodPattern.getMethodName());
//		stringPattern.append("()");
//		stringPattern.append(" void");
//		SearchPattern searchPattern = SearchPattern.createPattern(stringPattern.toString(), IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
//		SearchEngine searchEngine = new SearchEngine();
//		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
//		final List<IMethod> methodList = new ArrayList<IMethod>();
//		SearchRequestor requestor= new SearchRequestor() {
//			@Override
//            public void acceptSearchMatch(SearchMatch match) throws CoreException {
//				Object element = match.getElement();
//				if (match.getElement() instanceof IMethod) {
//					methodList.add((IMethod)element);
//				}
//			}
//		};
//		try {
//			searchEngine.search(searchPattern, new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, scope, requestor, new NullProgressMonitor());
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for(IMethod method : methodList) {
//			System.out.println(method.getElementName());
//		}
//		RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(IJavaRefactorings.RENAME_METHOD);
//		RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor) contribution.createDescriptor();
//		descriptor.setJavaElement(methodList.get(0)); //set the java element to refactor
//		descriptor.setNewName(newMethodName);  //new method name from user input
//		descriptor.setUpdateReferences(true);  //refactor also the references
//		RefactoringStatus status = new RefactoringStatus();
//		status = descriptor.validateDescriptor();
//		try {
//			Refactoring renameMethod = descriptor.createRefactoring(status);
//			status.merge(renameMethod.checkInitialConditions(pm));
//			status.merge(renameMethod.checkFinalConditions(pm));
//			return renameMethod.createChange(pm);
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//	}

	public void checkUserInput(RefactoringStatus status, String text) {
		IStatus stateValidName = new GroovyConventionsBuilder(text, "method")
				.validateGroovyIdentifier().validateLowerCase(IStatus.WARNING).done();
		addStatusEntries(status, stateValidName);
	}

	public String getOldName() {
		return selectedMethodPattern.getMethodName();
	}
	
	public MethodPattern getMethodPattern() {
		return selectedMethodPattern;
	}

	public void setNewName(String newName) {
		newMethodName = newName;
		prepareCandidateLists();
		setProviders();
		for(RenameTextEditProvider provider : textEditProviders){
			provider.setNewName(newName);
		}
	}
	
	public String getNewName() {
		return newMethodName;
	}
	
	/**
	 * If the refactoring is responsible for the given nodetype
	 * it will return a methodPattern, otherwise null
	 * @param node
	 * @param docProvider
	 * @return the MethodPattern that describes the method as accurate as possible
	 */
	public static MethodPattern giveMethodNodeToRename(ASTNode node,
			IGroovyDocumentProvider docProvider, ClassNode parentClass) {
		
		if(node instanceof MethodNode){
			return handleMethodDeclaration(node, docProvider);
		} else if(node instanceof MethodCallExpression){
			return handleMethodCall(node, docProvider, parentClass);
		} else if(node instanceof StaticMethodCallExpression){
			return handleStaticMethodCallExpr(node, docProvider);
		}
		return null;
	}

	private static MethodPattern handleStaticMethodCallExpr(ASTNode node,
			IGroovyDocumentProvider docProvider) {
		
		StaticMethodCallExpression staticCall = (StaticMethodCallExpression) node;
		MethodPattern staticCallPattern = new MethodPattern(staticCall,docProvider.getDocument());
		return staticCallPattern;
	}

	private static MethodPattern handleMethodCall(ASTNode node,
			IGroovyDocumentProvider docProvider, ClassNode parentClass) {
		
		MethodCallExpression mce = (MethodCallExpression) node;
		if(mce.getText().startsWith(THIS)){
			//Pattern contains the correct class using the parent
			return new MethodPattern(mce,docProvider.getDocument(),parentClass);
		}
        //correct class in case the user uses static typing
        if(mce.getObjectExpression() instanceof VariableExpression){
        	VariableExpression accessedVari = (VariableExpression)mce.getObjectExpression();
        	return new MethodPattern(mce,docProvider.getDocument(),accessedVari.getType());
        }
        //Pattern contains java.lang.Object
        return new MethodPattern(mce,docProvider.getDocument(),mce.getType());
	}

	private static MethodPattern handleMethodDeclaration(ASTNode node,
			IGroovyDocumentProvider docProvider) {
		//Pattern contains the correct class
		MethodNode mn = (MethodNode) node;
		return new MethodPattern(mn,mn.getDeclaringClass(),docProvider.getDocument());
	}

}
