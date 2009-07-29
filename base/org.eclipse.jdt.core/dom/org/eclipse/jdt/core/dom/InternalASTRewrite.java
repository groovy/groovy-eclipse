/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.TargetSourceRangeComputer;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteAnalyzer;
import org.eclipse.jdt.internal.core.dom.rewrite.LineInformation;
import org.eclipse.jdt.internal.core.dom.rewrite.ListRewriteEvent;
import org.eclipse.jdt.internal.core.dom.rewrite.NodeInfoStore;
import org.eclipse.jdt.internal.core.dom.rewrite.NodeRewriteEvent;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore.CopySourceInfo;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore.PropertyLocation;

/**
 * Internal class: not intended to be used by client.
 * When AST modifications recording is enabled, all changes are recorded by this class.
 */
class InternalASTRewrite extends NodeEventHandler {
	
	/** root node for the rewrite: Only nodes under this root are accepted */
	private CompilationUnit root;

	protected final RewriteEventStore eventStore;
	protected final NodeInfoStore nodeStore;
	protected final Hashtable clonedNodes;
	
	int cloneDepth = 0;
	
	/**
	 * Constructor
	 * @param root root node of the recorded ast.
	 */
	public InternalASTRewrite(CompilationUnit root) {
		this.root = root;
		this.eventStore = new RewriteEventStore();
		this.nodeStore = new NodeInfoStore(root.getAST());
		this.clonedNodes = new Hashtable();
	}

	/**
	 * Performs the rewrite: The rewrite events are translated to the corresponding in text changes.
	 * @param document Document which describes the code of the AST that is passed in in the
	 * constructor. This document is accessed read-only.
	 * @param options options
	 * @throws IllegalArgumentException if the rewrite fails
	 * @return Returns the edit describing the text changes.
	 */
	public TextEdit rewriteAST(IDocument document, Map options) {
		TextEdit result = new MultiTextEdit();
		
		final CompilationUnit rootNode = getRootNode();
		if (rootNode != null) {
			TargetSourceRangeComputer xsrComputer = new TargetSourceRangeComputer() {
				/** 
				 * This implementation of
				 * {@link TargetSourceRangeComputer#computeSourceRange(ASTNode)}
				 * is specialized to work in the case of internal AST rewriting, where the
				 * original AST has been modified from its original form. This means that
				 * one cannot trust that the root of the given node is the compilation unit.
				 */
				public SourceRange computeSourceRange(ASTNode node) {
					int extendedStartPosition = rootNode.getExtendedStartPosition(node);
					int extendedLength = rootNode.getExtendedLength(node);
					return new SourceRange(extendedStartPosition, extendedLength);
				}
			};
			char[] content= document.get().toCharArray();
			LineInformation lineInfo= LineInformation.create(document);
			String lineDelim= TextUtilities.getDefaultLineDelimiter(document);
			List comments= rootNode.getCommentList();
			
			ASTRewriteAnalyzer visitor = new ASTRewriteAnalyzer(content, lineInfo, lineDelim, result, this.eventStore, this.nodeStore, comments, options, xsrComputer);
			rootNode.accept(visitor);
		}
		return result;
	}
	
	private  void markAsMoveOrCopyTarget(ASTNode node, ASTNode newChild) {
		ASTNode source = (ASTNode)this.clonedNodes.get(newChild);
		if(source != null) {
			if(this.cloneDepth == 0) {
				PropertyLocation propertyLocation = this.eventStore.getPropertyLocation(source, RewriteEventStore.ORIGINAL);
				CopySourceInfo sourceInfo =
					this.eventStore.markAsCopySource(
						propertyLocation.getParent(),
						propertyLocation.getProperty(),
						source,
						false);
				this.nodeStore.markAsCopyTarget(newChild, sourceInfo);
			}
		} else if((newChild.getFlags() & ASTNode.ORIGINAL) != 0) {
			PropertyLocation propertyLocation = this.eventStore.getPropertyLocation(newChild, RewriteEventStore.ORIGINAL);
			CopySourceInfo sourceInfo =
				this.eventStore.markAsCopySource(
					propertyLocation.getParent(),
					propertyLocation.getProperty(),
					newChild,
					true);
			this.nodeStore.markAsCopyTarget(newChild, sourceInfo);
		}
	}

	private CompilationUnit getRootNode() {
		return this.root;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Events:\n"); //$NON-NLS-1$
		buf.append(this.eventStore.toString());
		return buf.toString();
	}
	
	void preValueChangeEvent(ASTNode node, SimplePropertyDescriptor property) {
		// force event creation
		this.getNodeEvent(node, property);
	}
	
	void postValueChangeEvent(ASTNode node, SimplePropertyDescriptor property) {
		NodeRewriteEvent event = this.getNodeEvent(node, property);
		event.setNewValue(node.getStructuralProperty(property));
	}
	
	void preAddChildEvent(ASTNode node, ASTNode child,	StructuralPropertyDescriptor property) {
		if(property.isChildProperty()) {
			NodeRewriteEvent event = this.getNodeEvent(node, property);
			event.setNewValue(child);
			if(child != null) {
				this.markAsMoveOrCopyTarget(node, child);
			}
		} else if(property.isChildListProperty()) {
			// force event creation
			this.getListEvent(node, property);
		}
	}
	
	void postAddChildEvent(ASTNode node, ASTNode child,	StructuralPropertyDescriptor property) {
		if(property.isChildListProperty()) {

			ListRewriteEvent event = this.getListEvent(node, property);
			List list = (List)node.getStructuralProperty(property);
			int i = list.indexOf(child);
			int s = list.size();
			int index;
			if(i + 1 < s) {
				ASTNode nextNode = (ASTNode)list.get(i + 1);
				index = event.getIndex(nextNode, ListRewriteEvent.NEW);
			} else {
				index = -1;
			}
			event.insert(child, index);
			if(child != null) {
				this.markAsMoveOrCopyTarget(node, child);
			}
		}
	}
	
	void preRemoveChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
		if(property.isChildProperty()) {
			NodeRewriteEvent event = getNodeEvent(node, property);
			event.setNewValue(null);
		} else if(property.isChildListProperty()) {
			ListRewriteEvent event = this.getListEvent(node, property);
			int i = event.getIndex(child, ListRewriteEvent.NEW);
			NodeRewriteEvent nodeEvent = (NodeRewriteEvent)event.getChildren()[i];
			if(nodeEvent.getOriginalValue() == null) {
				event.revertChange(nodeEvent);
			} else {
				nodeEvent.setNewValue(null);
			}
		}
	}
	
	void preReplaceChildEvent(ASTNode node, ASTNode child, ASTNode newChild, StructuralPropertyDescriptor property) {
		if(property.isChildProperty()) {
			NodeRewriteEvent event = getNodeEvent(node, property);
			event.setNewValue(newChild);
			if(newChild != null) {
				this.markAsMoveOrCopyTarget(node, newChild);
			}
		} else if(property.isChildListProperty()) {
			ListRewriteEvent event = this.getListEvent(node, property);
			int i = event.getIndex(child, ListRewriteEvent.NEW);
			NodeRewriteEvent nodeEvent = (NodeRewriteEvent)event.getChildren()[i];
			nodeEvent.setNewValue(newChild);
			if(newChild != null) {
				this.markAsMoveOrCopyTarget(node, newChild);
			}
		}
	}
	
	
	void preCloneNodeEvent(ASTNode node) {
		this.cloneDepth++;
	}
	
	
	void postCloneNodeEvent(ASTNode node, ASTNode clone) {
		if(node.ast == root.ast && clone.ast == root.ast) {
			if((node.getFlags() & ASTNode.ORIGINAL) != 0) {
				this.clonedNodes.put(clone, node);
			} else {
				// node can be a cloned node
				Object original = this.clonedNodes.get(node);
				if(original != null) {
					this.clonedNodes.put(clone, original);
				}
			}
		}
		this.cloneDepth--;
	}
	
	private NodeRewriteEvent getNodeEvent(ASTNode node, StructuralPropertyDescriptor property) {
		return this.eventStore.getNodeEvent(node, property, true);
	}
	
	private ListRewriteEvent getListEvent(ASTNode node, StructuralPropertyDescriptor property) {
		return this.eventStore.getListEvent(node, property, true);
	}
}
