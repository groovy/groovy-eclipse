/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.util.DOMFinder;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * Abstract class for Java elements which implement ISourceReference.
 */
public abstract class SourceRefElement extends JavaElement implements ISourceReference {
	/*
	 * A count to uniquely identify this element in the case
	 * that a duplicate named element exists. For example, if
	 * there are two fields in a compilation unit with the
	 * same name, the occurrence count is used to distinguish
	 * them.  The occurrence count starts at 1 (thus the first
	 * occurrence is occurrence 1, not occurrence 0).
	 */
	public int occurrenceCount = 1;

protected SourceRefElement(JavaElement parent) {
	super(parent);
}
/**
 * This element is being closed.  Do any necessary cleanup.
 */
protected void closing(Object info) throws JavaModelException {
	// Do any necessary cleanup
}
/**
 * Returns a new element info for this element.
 */
protected Object createElementInfo() {
	return null; // not used for source ref elements
}
/**
 * @see ISourceManipulation
 */
public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Messages.operation_nullContainer);
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	IJavaElement[] siblings= null;
	if (sibling != null) {
		siblings= new IJavaElement[] {sibling};
	}
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().copy(elements, containers, siblings, renamings, force, monitor);
}
/**
 * @see ISourceManipulation
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements = new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
}
public boolean equals(Object o) {
	if (!(o instanceof SourceRefElement)) return false;
	return this.occurrenceCount == ((SourceRefElement)o).occurrenceCount &&
			super.equals(o);
}
/**
 * Returns the <code>ASTNode</code> that corresponds to this <code>JavaElement</code>
 * or <code>null</code> if there is no corresponding node.
 */
public ASTNode findNode(CompilationUnit ast) {
	DOMFinder finder = new DOMFinder(ast, this, false);
	try {
		return finder.search();
	} catch (JavaModelException e) {
		// receiver doesn't exist
		return null;
	}
}
/*
 * @see JavaElement#generateInfos
 */
protected void generateInfos(Object info, HashMap newElements, IProgressMonitor pm) throws JavaModelException {
	Openable openableParent = (Openable)getOpenableParent();
	if (openableParent == null) return;

	JavaElementInfo openableParentInfo = (JavaElementInfo) JavaModelManager.getJavaModelManager().getInfo(openableParent);
	if (openableParentInfo == null) {
		openableParent.generateInfos(openableParent.createElementInfo(), newElements, pm);
	}
}
public IAnnotation getAnnotation(String name) {
	return new Annotation(this, name);
}
public IAnnotation[] getAnnotations() throws JavaModelException {
	AnnotatableInfo info = (AnnotatableInfo) getElementInfo();
	return info.annotations;
}
/**
 * @see IMember
 */
public ICompilationUnit getCompilationUnit() {
	return (ICompilationUnit) getAncestor(COMPILATION_UNIT);
}
/**
 * Elements within compilation units and class files have no
 * corresponding resource.
 *
 * @see IJavaElement
 */
public IResource getCorrespondingResource() throws JavaModelException {
	if (!exists()) throw newNotPresentException();
	return null;
}
/*
 * @see JavaElement
 */
public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
	switch (token.charAt(0)) {
		case JEM_COUNT:
			return getHandleUpdatingCountFromMemento(memento, workingCopyOwner);
	}
	return this;
}
protected void getHandleMemento(StringBuffer buff) {
	super.getHandleMemento(buff);
	if (this.occurrenceCount > 1) {
		buff.append(JEM_COUNT);
		buff.append(this.occurrenceCount);
	}
}
/*
 * Update the occurence count of the receiver and creates a Java element handle from the given memento.
 * The given working copy owner is used only for compilation unit handles.
 */
public IJavaElement getHandleUpdatingCountFromMemento(MementoTokenizer memento, WorkingCopyOwner owner) {
	if (!memento.hasMoreTokens()) return this;
	this.occurrenceCount = Integer.parseInt(memento.nextToken());
	if (!memento.hasMoreTokens()) return this;
	String token = memento.nextToken();
	return getHandleFromMemento(token, memento, owner);
}
/*
 * @see IMember#getOccurrenceCount()
 */
public int getOccurrenceCount() {
	return this.occurrenceCount;
}
/**
 * Return the first instance of IOpenable in the hierarchy of this
 * type (going up the hierarchy from this type);
 */
public IOpenable getOpenableParent() {
	IJavaElement current = getParent();
	while (current != null){
		if (current instanceof IOpenable){
			return (IOpenable) current;
		}
		current = current.getParent();
	}
	return null;
}
/*
 * @see IJavaElement
 */
public IPath getPath() {
	return getParent().getPath();
}
/*
 * @see IJavaElement
 */
public IResource resource() {
	return this.parent.resource();
}
/**
 * @see ISourceReference
 */
public String getSource() throws JavaModelException {
	IOpenable openable = getOpenableParent();
	IBuffer buffer = openable.getBuffer();
	if (buffer == null) {
		return null;
	}
	ISourceRange range = getSourceRange();
	int offset = range.getOffset();
	int length = range.getLength();
	if (offset == -1 || length == 0 ) {
		return null;
	}
	try {
		return buffer.getText(offset, length);
	} catch(RuntimeException e) {
		return null;
	}
}
/**
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	SourceRefElementInfo info = (SourceRefElementInfo) getElementInfo();
	return info.getSourceRange();
}
/**
 * @see IJavaElement
 */
public IResource getUnderlyingResource() throws JavaModelException {
	if (!exists()) throw newNotPresentException();
	return getParent().getUnderlyingResource();
}
/**
 * @see IParent
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see IJavaElement
 */
public boolean isStructureKnown() throws JavaModelException {
	// structure is always known inside an openable
	return true;
}
/**
 * @see ISourceManipulation
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Messages.operation_nullContainer);
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	IJavaElement[] siblings= null;
	if (sibling != null) {
		siblings= new IJavaElement[] {sibling};
	}
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().move(elements, containers, siblings, renamings, force, monitor);
}
/**
 * @see ISourceManipulation
 */
public void rename(String newName, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (newName == null) {
		throw new IllegalArgumentException(Messages.element_nullName);
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] dests= new IJavaElement[] {getParent()};
	String[] renamings= new String[] {newName};
	getJavaModel().rename(elements, dests, renamings, force, monitor);
}
protected void toStringName(StringBuffer buffer) {
	super.toStringName(buffer);
	if (this.occurrenceCount > 1) {
		buffer.append("#"); //$NON-NLS-1$
		buffer.append(this.occurrenceCount);
	}
}
}
