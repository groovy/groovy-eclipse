/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.selection;

import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameCandidates;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


/**
 * @author Stefan Reinhard
 */
public class SelectionElementProvider implements ITreeContentProvider {
	
	RenameCandidates candidates;

	public SelectionElementProvider(RenameCandidates candidateMap) {
		candidates = candidateMap;
	}
	
	public Object[] getChildren(Object parentElement) {
		return candidates.getCandidateList(parentElement);
	}

	public Object getParent(Object element) {
		return candidates.getListNameFor(element);
	}

	public boolean hasChildren(Object element) {
		return candidates.getCandidateList(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return candidates.getListNames();
	}

	public void dispose() { }

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }

}
