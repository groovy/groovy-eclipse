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
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.MultiFileRefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IRenameProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyConventionsBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * Contains all the information for the Rename Field refactoring
 * 
 * @author reto kleeb
 */
public class RenameFieldProvider extends MultiFileRefactoringProvider implements IRenameProvider {
	
	private final String oldFieldName;
	private final FieldPattern patternOfAccessedField;
	private String newFieldName;
	private int nrOfFieldNodes;
	private FieldNode relevantFieldNode;
	private IGroovyDocumentProvider documentOfFieldNode;
	private boolean checkUniqueFieldDefinitions = true;
	protected List<RenameTextEditProvider> textEditProviders = new ArrayList<RenameTextEditProvider>();

	public RenameFieldProvider(IGroovyFileProvider docProvider, FieldPattern accessedField) {
		super(docProvider);
		oldFieldName = accessedField.getName();
		patternOfAccessedField = accessedField;
		this.selectedASTNode = patternOfAccessedField.getSelectedASTNode();
	}
	
	public RenameFieldProvider(IGroovyFileProvider docProvider, UserSelection selection, FieldPattern accessedField) {
		this(docProvider, accessedField);
		setSelection(selection);
	}
	
	@Override
    protected void prepareCandidateLists(){
		
		// FIXADE RC1: Why do candidates need to be collected several times?
		// This is a small workaround to fix it, but maybe dangerous
		if (hasCandidateLists()) return;
		
		textEditProviders = new ArrayList<RenameTextEditProvider>();
		definitiveCandidates = new HashMap<IGroovyDocumentProvider, List<ASTNode>>();
		ambiguousCandidates = new HashMap<IGroovyDocumentProvider, List<ASTNode>>();
		
		for(IGroovyDocumentProvider document : fileProvider.getAllSourceFiles()){
			getAllCandidatesInDocument(document);
		}
		
		removeCandidatesWithInvalidPositions();
		
		removeImpossibleCandidates();
		
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
		
		moveSelectionToDefinitiveCandidates();

		if(checkUniqueFieldDefinitions) {
			checkUniqueFieldDefinitions();
		}
	}
	
	private void checkUniqueFieldDefinitions() {
		nrOfFieldNodes = 0;
		relevantFieldNode = null;
		documentOfFieldNode = null;
		checkMapForFieldNodes(definitiveCandidates);
		checkMapForFieldNodes(ambiguousCandidates);
		
		if(nrOfFieldNodes == 1 && relevantFieldNode != null){
			addNodeToACandidateListList(definitiveCandidates, documentOfFieldNode, relevantFieldNode);
			if(ambiguousCandidates.get(documentOfFieldNode) != null){
				ambiguousCandidates.get(documentOfFieldNode).remove(relevantFieldNode);
			}
		}
	}

	private void checkMapForFieldNodes (Map<IGroovyDocumentProvider, List<ASTNode>> map) {
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : map.entrySet()){
			for(ASTNode node : entry.getValue()){
				if(node instanceof FieldNode) {
					nrOfFieldNodes++;
					relevantFieldNode = (FieldNode) node;
					documentOfFieldNode = entry.getKey();
				}
			}
		}
	}
	
	private void removeImpossibleCandidates() {
		if(!patternOfAccessedField.getDeclaringClass().getName().equals(JAVA_LANG_OBJECT) &&
				selectedASTNode instanceof FieldNode){
			Map<IGroovyDocumentProvider, List<ASTNode>> toDelete = new HashMap<IGroovyDocumentProvider, List<ASTNode>>();
			
			for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : definitiveCandidates.entrySet()){
				for(ASTNode node : entry.getValue()){
					if(node instanceof FieldNode && !node.equals(selectedASTNode)){
						addNodeToACandidateListList(toDelete,entry.getKey(),node);
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
		
	}

	private void moveAmbiguousCandidates(
			Map<IGroovyDocumentProvider, List<ASTNode>> ambiguousCandidates) {
		
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : definitiveCandidates.entrySet()){
			for(ASTNode node : entry.getValue()){
				
				//ignore the astNode selected by the user
				if(node == selectedASTNode){
					continue;
				} 
				
				//ignore the exact matches
				FieldPattern candidatePattern = giveFieldNodeToRename(node);
				if(patternOfAccessedField.equals(candidatePattern)){
					continue;
				}
				
				addNodeToACandidateListList(ambiguousCandidates, entry.getKey(), node);
			}
		}
		
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : ambiguousCandidates.entrySet()){
			for(ASTNode listElement : entry.getValue()){
				definitiveCandidates.get(entry.getKey()).remove(listElement);
			}
		}
		
	}

	private void getAllCandidatesInDocument(IGroovyDocumentProvider currentDocument) {
		RenameFieldCandidateCollector candiateCollector = new RenameFieldCandidateCollector(currentDocument.getRootNode(),patternOfAccessedField);
		candiateCollector.scanAST();
		
		for(ASTNode currentCandid: candiateCollector.getCandidates()){
			if(!definitiveCandidates.containsKey(currentDocument)){
				definitiveCandidates.put(currentDocument, new LinkedList<ASTNode>());
			}
			if(!definitiveCandidates.get(currentDocument).contains(currentCandid)){
				definitiveCandidates.get(currentDocument).add(currentCandid);
			}
		}
	}

	@Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return checkDuplicateNames();
	}

	private RefactoringStatus checkDuplicateNames() {
		// TODO
		RefactoringStatus stat = new RefactoringStatus();
		return stat;
	}

	@Override
    public void addInitialConditionsCheckStatus(RefactoringStatus status) {
		//check if the field can be renamed
		//no conditions yet
	}

	@Override
    public GroovyChange createGroovyChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		GroovyChange change = new GroovyChange(GroovyRefactoringMessages.RenameFieldRefactoring);
		for (RenameTextEditProvider textEditProvider : textEditProviders) {
			MultiTextEdit multi = removeDublicatedTextedits(textEditProvider);
			change.addEdit(textEditProvider.getDocProvider(), multi);
		}
		return change;
	}

	public void checkUserInput(RefactoringStatus status, String input) {
		IStatus stateValidName = new GroovyConventionsBuilder(input, "field")
		.validateGroovyIdentifier().validateLowerCase(IStatus.WARNING).done();
		addStatusEntries(status, stateValidName);
	}

	@Override
    public String getOldName() {
		return oldFieldName;
	}
	
	public FieldPattern getFieldPattern() {
		return patternOfAccessedField;
	}
	
	public void setTextProviders(){
		for(IGroovyDocumentProvider document : fileProvider.getAllSourceFiles()){
			if(definitiveCandidates.get(document) != null){
				textEditProviders.add(new RenameFieldTextEditProvider(newFieldName, oldFieldName, document, patternOfAccessedField,definitiveCandidates.get(document)));
			}
		}
	}

	public void setNewName(String newName) {
		newFieldName = newName;
		prepareCandidateLists();
		setTextProviders();
	}
	
	public String getNewName() {
		return newFieldName;
	}
	
	public void checkUniqueFieldDefinitions(boolean b) {
		checkUniqueFieldDefinitions = b;
	}

	public static FieldPattern giveFieldNodeToRename(ASTNode node) {
		if (node instanceof VariableExpression) {
			return handleVariableExpression(node);
		} else if (node instanceof FieldNode) {
			return new FieldPattern((FieldNode) node, node);
		} else if(node instanceof PropertyExpression){
			return handlePropertyExpression(node);
		} else if(node instanceof FieldExpression){
			return handleFieldExpression(node);
		}
		return null;
	}

	private static FieldPattern handleFieldExpression(ASTNode node) {
		FieldExpression fExpr = (FieldExpression) node;
		return new FieldPattern(fExpr.getField(), node);
	}

	private static FieldPattern handlePropertyExpression(ASTNode node) {
		PropertyExpression prop = (PropertyExpression)node;
		FieldPattern pattern = new FieldPattern(prop.getObjectExpression().getType(),prop.getType(),prop.getPropertyAsString(),node);
		return pattern;
	}

	private static FieldPattern handleVariableExpression(ASTNode node) {
		Variable accessedVariable = ((VariableExpression) node).getAccessedVariable();
		if (accessedVariable instanceof FieldNode || accessedVariable instanceof PropertyNode) {
			return new FieldPattern((FieldNode) accessedVariable, node);
		} 
		return null;
	}

}
