/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
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
package org.codehaus.groovy.eclipse.refactoring.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public abstract class MultiFileRefactoringProvider extends RefactoringProvider {
	
	protected IGroovyFileProvider fileProvider;
	protected IGroovyDocumentProvider selectionDocument;
	
	protected Map<IGroovyDocumentProvider, List<ASTNode>> definitiveCandidates;
	protected Map<IGroovyDocumentProvider, List<ASTNode>> ambiguousCandidates;
	protected ASTNode selectedASTNode;
	protected static final String JAVA_LANG_OBJECT = "java.lang.Object";

	public MultiFileRefactoringProvider(IGroovyFileProvider fileProvider) {
		this.fileProvider = fileProvider;
		this.selectionDocument = fileProvider.getSelectionDocument();
	}
	
	public IDocument getSelectionDocument() {
		return selectionDocument.getDocument();
	}
	
	public ModuleNode getSelectionRoot() {
		return selectionDocument.getRootNode();
	}
	
	@Override
    public IGroovyDocumentProvider getDocumentProvider() {
		return selectionDocument;
	}
	
	public List<IGroovyDocumentProvider> getUsedDocuments() {
		return fileProvider.getAllSourceFiles();
	}
	
	public IGroovyFileProvider getFileProvider() {
		return fileProvider;
	}
	
	@Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) {
		RefactoringStatus result = new RefactoringStatus();
		for (IGroovyDocumentProvider docProvider : getUsedDocuments()) {
			if (!docProvider.fileExists()) {
				result.addFatalError("Sourcefile " + docProvider.getName() + " not found");
			} else if (docProvider.isReadOnly()) {
				result.addFatalError("Soucefile " + docProvider.getName() +  " is read only");
			}
		}
		addInitialConditionsCheckStatus(result);
		return result;
	}
	
	/**
	 * there are certain nodes that only exist in the AST
	 * but do not have a representation in the sourcecode. These nodes
	 * should not be considered for a rename refactoring.
	 * Example: methodnodes with default parameters
	 */
	protected void removeCandidatesWithInvalidPositions() {
		Map<IGroovyDocumentProvider, List<ASTNode>> toDelete = new HashMap<IGroovyDocumentProvider, List<ASTNode>>();
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : definitiveCandidates.entrySet()){
			for(ASTNode node : entry.getValue()){
				if(!ASTTools.hasValidPosition(node)){
					addNodeToACandidateListList(toDelete, entry.getKey(), node);
				}
			}
		}
		
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : toDelete.entrySet()){
			for(ASTNode node : entry.getValue()){
				definitiveCandidates.get(entry.getKey()).remove(node);
			}
		}
	}
	
	protected void addNodeToACandidateListList(
			Map<IGroovyDocumentProvider, List<ASTNode>> candidateList,
			IGroovyDocumentProvider docProvider, ASTNode node) {
		
		if(candidateList.containsKey(docProvider)){
			if(!candidateList.get(docProvider).contains(node)){
				candidateList.get(docProvider).add(node);
			}
		} else{
			LinkedList<ASTNode> list = new LinkedList<ASTNode>();
			list.add(node);
			candidateList.put(docProvider, list);
		}
	}
	
	public void addDefinitiveEntry(IGroovyDocumentProvider docProvider, ASTNode node) {
		addNodeToACandidateListList(definitiveCandidates, docProvider, node);
	}

	public void removeDefinitveEntry(IGroovyDocumentProvider docProvider, ASTNode node) {
		definitiveCandidates.get(docProvider).remove(node);
	}
	
	public void removeAmbiguousEntry(IGroovyDocumentProvider docProvider, ASTNode node) {
		ambiguousCandidates.get(docProvider).remove(node);
	}
	
	public void removeAllAmbiguousEntrys() {
		ambiguousCandidates.clear();
	}
	
	public Map<IGroovyDocumentProvider, List<ASTNode>> getAmbiguousCandidates() {
		return ambiguousCandidates;
	}

	public Map<IGroovyDocumentProvider, List<ASTNode>> getDefinitiveCandidates() {
		return definitiveCandidates;
	}
	
	public boolean hasCandidates() {
		prepareCandidateLists();
		int nrOfCandidates = 0;
		// FIXME: Values or keys?
		for(List<ASTNode> list : definitiveCandidates.values()) {
			nrOfCandidates += list.size();
		}
		for(List<ASTNode> list : ambiguousCandidates.values()) {
			nrOfCandidates += list.size();
		};
		return nrOfCandidates > 0;
	}
	
	public boolean refactoringIsAmbiguous(){
		prepareCandidateLists();
		int totalNrOfChanges = 0;
		for(Entry<IGroovyDocumentProvider, List<ASTNode>> entry : ambiguousCandidates.entrySet()){
			totalNrOfChanges += entry.getValue().size();
		}
		return totalNrOfChanges > 0;
	}

	protected abstract void prepareCandidateLists();
	
	public abstract String getOldName();
	
	protected boolean hasCandidateLists() {
		return definitiveCandidates != null 
				&& ambiguousCandidates != null;
	}

	private class SelectionPicker implements Runnable {
		public int lineNumber = -1;
		public void run() {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window == null) return;
			IEditorPart part = window.getActivePage().getActiveEditor();
//			if (part instanceof GroovyEditor) {
//				GroovyEditor editor = (GroovyEditor)part;
//				ITextSelection ts = (ITextSelection) editor.getSelectionProvider().getSelection();
//				if (ts.getStartLine()==ts.getEndLine()) {
//					lineNumber = ts.getStartLine();
//				}
//			}
		}
	};
	
	public void moveSelectionToDefinitiveCandidates() {
		int lineNumber = getSelectedLineNumber();
		if (lineNumber > -1) {
			IGroovyDocumentProvider doc = null;
			ASTNode selectedNode = null;
			for (Entry<IGroovyDocumentProvider, List<ASTNode>> entry : ambiguousCandidates.entrySet()) {
				for(ASTNode node : entry.getValue()) {
					if (node.getLineNumber()==lineNumber) {
						doc = entry.getKey();
						selectedNode = node;
					}
				}
			}
			
			if (doc != null && selectedNode != null) {
				addDefinitiveEntry(doc, selectedNode);
				removeAmbiguousEntry(doc, selectedNode);
			}
		}
	}

	private int getSelectedLineNumber() {
		SelectionPicker picker = new SelectionPicker();
		int lineNumber = -1;
		IWorkbench workbench = null;
		try {
			workbench = PlatformUI.getWorkbench();
		} catch (IllegalStateException e) { 
			if (selectedASTNode != null) {
				lineNumber = selectedASTNode.getLineNumber();
			}
		}
		if (workbench != null ) {
			workbench.getDisplay().syncExec(picker);
			lineNumber = picker.lineNumber;
		}
		lineNumber++;
		return lineNumber;
	}
}
