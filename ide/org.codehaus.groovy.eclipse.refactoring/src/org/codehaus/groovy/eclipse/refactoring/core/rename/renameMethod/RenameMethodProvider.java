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
import java.util.Collections;
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
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.MultiFileRefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyNode;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyTreeBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.participation.GroovyParticipantManager;
import org.codehaus.groovy.eclipse.refactoring.core.participation.GroovySharableParticipants;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyConventionsBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.participants.JavaProcessors;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * 
 * Provider Class for the Refactoring "Rename Method"
 *
 */
public class RenameMethodProvider extends MultiFileRefactoringProvider implements IRenameProvider {
	
	private static final String THIS = "this.";
	private static final List<RefactoringParticipant> EMPTY_PARTICIPANTS= Collections.EMPTY_LIST;

	private final MethodPattern selectedMethodPattern;
	private String newMethodName;
	private int nrOfMethodDefinitions;
	private MethodNode relevantMethodDefintion;
	private IGroovyDocumentProvider documentOfMethodDefinition;
	private List<RefactoringParticipant> fParticipants = EMPTY_PARTICIPANTS;

	protected List<RenameTextEditProvider> textEditProviders = new ArrayList<RenameTextEditProvider>();

	private final ICompilationUnit unit;
	
	public RenameMethodProvider(IGroovyFileProvider docProvider, MethodPattern selectedNode, ICompilationUnit unit) {
		super(docProvider);
		this.selectedMethodPattern = selectedNode;
		this.unit = unit;
		this.selectedASTNode = selectedMethodPattern.getNode();
	}
	
	// FIXADE RC1 can delete this?
//	public RenameMethodProvider(IGroovyFileProvider docProvider, UserSelection selection, MethodPattern selectedNode) {
//		this(docProvider, selectedNode, null);
//		setSelection(selection);
//	}
	
	@Override
    protected void prepareCandidateLists() {
		
		// FIXADE RC1: Why do candidates need to be collected several times?
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
		checkForDuplicates(refactoringStatus);
		IJavaElement element = codeResolve(unit);
		
		if (element != null) {    
		    processRenameParticipants(refactoringStatus, element);
		} else {
		    if (unit != null && selection != null) {
		        // only add error if there was a valid selection going in.
		        refactoringStatus.addError("Cannot resolve selection to a Groovy program element");
		    }
		}
				
		return refactoringStatus;
	}
	
    private IJavaElement codeResolve(ICompilationUnit input) throws JavaModelException {
        if (input == null || selectedASTNode == null) {
            return null;
        }
        JavaModelUtil.reconcile((ICompilationUnit) input);
        IJavaElement[] elements = input.codeSelect(
                selectedASTNode.getStart(), 0);
        if (elements != null && elements.length > 0) {
            return elements[0];
        } else {
            return null;
        }
    }
	
	private void processRenameParticipants(RefactoringStatus refactoringStatus, IJavaElement element) throws CoreException{
		
		GroovySharableParticipants sharableParticipants= new GroovySharableParticipants(); 
		RenameArguments arguments = new RenameArguments(newMethodName, true);
		
		GroovyRenameMethodProcessor processor = new GroovyRenameMethodProcessor((IMethod)element);
        
		
		RefactoringParticipant[] loadedParticipants= GroovyParticipantManager.loadRenameParticipants(refactoringStatus, processor, element, arguments,
																					JavaProcessors.computeAffectedNatures(element), null, sharableParticipants);
		if (loadedParticipants == null || loadedParticipants.length == 0) {
			fParticipants= EMPTY_PARTICIPANTS;
		} else {
			fParticipants= new ArrayList<RefactoringParticipant>();
			for (int i= 0; i < loadedParticipants.length; i++) {
				fParticipants.add(loadedParticipants[i]);
			}
		}
		
		
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
		
		for (RefactoringParticipant participant : fParticipants) {
			Change participantChange= participant.createChange(new SubProgressMonitor(pm, 1));
			if (participantChange != null) {
				GroovyCore.logTraceMessage("--adding participant change " + participantChange);
				change.addChange(participantChange);
			}
		}

		return change;
	}

	public void checkUserInput(RefactoringStatus status, String text) {
		IStatus stateValidName = new GroovyConventionsBuilder(text, "method")
				.validateGroovyIdentifier().validateLowerCase(IStatus.WARNING).done();
		addStatusEntries(status, stateValidName);
	}

	@Override
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
