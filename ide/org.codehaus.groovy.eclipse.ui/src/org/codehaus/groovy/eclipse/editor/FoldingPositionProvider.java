/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.editor;

import static org.codehaus.groovy.eclipse.core.util.SetUtil.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.jdt.internal.ui.text.JavaPartitionScanner;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;

/**
 * Provides and updates positions for folding positions.
 * 
 * @author km
 */
public class FoldingPositionProvider {
	private final IDocument document;
	private Set< Position > positions = set();
	private Set< Position > removedPositions = set();
	private Set< Position > addedPositions = set();
	private final Set< Position > oldPositions;

	private class FoldingStructureVisitor extends ClassCodeVisitorSupport {
		@Override
        public void visitClass(ClassNode node) {
			super.visitClass(node);
			addPosition(node);
		}

		@Override
        public void visitMethod(MethodNode node) {
			super.visitMethod(node);
			addPosition(node);
		}

		@Override
        protected SourceUnit getSourceUnit() {
			return null;
		}
	}

	/**
	 * @param document
	 *            document for which to provide folding positions (must
	 *            implement {@link IDocumentExtension3}).
	 * @param oldPositions
	 *            list of previous position (of type {@link Position}).
	 */
	public FoldingPositionProvider(IDocument document, Collection< Position > oldPositions) {
		super();
		assert document instanceof IDocumentExtension3;
		this.document = document;
		this.oldPositions = set( oldPositions );
	}

	public Set< Position > getAddedPositions() {
		return Collections.unmodifiableSet(addedPositions);
	}

	/**
	 * After calling {@link #updatePositions} this method returns the list of
	 * folding positions that need to be removed.
	 * 
	 * @return List of removed folding position (of type {@link Position}).
	 */
	public Set< Position > getRemovedPositions() {
		return Collections.unmodifiableSet(removedPositions);
	}

	public Set< Position > getPositions() {
		return Collections.unmodifiableSet(positions);
	}

	/**
	 * Updates the folding positions with the given list of module nodes (of
	 * type {@link ModuleNode}).
	 * 
	 * @param moduleNodes
	 *            List of module nodes for updating the folding positions (of
	 *            type {@link ModuleNode}).
	 */
	public void updatePositions(final List< ModuleNode > moduleNodes) {
		positions = set();
		final FoldingStructureVisitor foldingStructureVisitor = new FoldingStructureVisitor();
		for (Iterator< ModuleNode > moduleNodeIter = moduleNodes.iterator(); moduleNodeIter
				.hasNext();) {
			final ModuleNode moduleNode = moduleNodeIter.next();
			final List< ClassNode > classes = new ArrayList<ClassNode>();
			for( final Object object : moduleNode.getClasses() )
			    classes.add( ( ClassNode )object );
			final List< ImportNode > importNodes = new ArrayList<ImportNode>();
			for( final Object object : moduleNode.getImports() )
			    importNodes.add( ( ImportNode )object );
			addImportPositions(importNodes);
			for (Iterator< ClassNode > classNodeIter = classes.iterator(); classNodeIter
					.hasNext();) {
				final ClassNode classNode = classNodeIter.next();
				foldingStructureVisitor.visitClass(classNode);
			}
		}
		addPositionsForMultilineCommentPartitions();
		removedPositions = set( oldPositions );
		removedPositions.removeAll(positions);
		addedPositions = set( positions );
		addedPositions.removeAll(oldPositions);
	}

	/**
	 * Only import nodes with a type have a line number, so folding just works
	 * with type imports!
	 * 
	 * @param importNodes
	 *            belong to the same module node, so determining min and max
	 *            line number is sufficient
	 */
	private void addImportPositions(List< ImportNode > importNodes) {
		if (importNodes.size() > 1) {
			int startLineNo = 0;
			int endLineNo = 0;
			boolean firstPass = true;
			for (Iterator< ImportNode > importNodeIter = importNodes.iterator(); importNodeIter
					.hasNext();) {
				ImportNode importNode = importNodeIter.next();
				if (importNode.getType() != null) {
					int lineNo = convertAntlrLineNoToEclipseLineNo(importNode
							.getType().getLineNumber());
					if (firstPass) {
						startLineNo = endLineNo = lineNo;
					} else {
						boolean newImportFoldingRequired = lineNo - endLineNo > 1;
						if (newImportFoldingRequired) {
							addPosition(startLineNo, endLineNo);
							startLineNo = endLineNo = lineNo;
						} else {
							endLineNo = lineNo;
						}
					}
				}
				firstPass = false;
			}
			addPosition(startLineNo, endLineNo);
		}
	}

	private void addPositionsForMultilineCommentPartitions() {
		final IDocumentPartitioner documentPartitioner = getDocumentExtension3()
				.getDocumentPartitioner(JavaPartitionScanner.JAVA_PARTITIONING);
		final ITypedRegion[] partitions = documentPartitioner
				.computePartitioning(0, document.getLength());
		for (int i = 0; i < partitions.length; i++) {
			final ITypedRegion typedRegion = partitions[i];
			final String type = typedRegion.getType();
			if (IJavaPartitions.JAVA_MULTI_LINE_COMMENT.equals(type)) {
				try {
					int startLineNo = document.getLineOfOffset(typedRegion
							.getOffset());
					int endLineNo = document.getLineOfOffset(typedRegion
							.getOffset()
							+ typedRegion.getLength());
					addPosition(startLineNo, endLineNo);
				} catch (BadLocationException e) {
					GroovyPlugin.getDefault().logException(
							"Exception in updating folding:", e);
				}
			}
		}
	}

	private IDocumentExtension3 getDocumentExtension3() {
		return (IDocumentExtension3) document;
	}

	private void addPosition(final ASTNode node) {
		int lineNumber = convertAntlrLineNoToEclipseLineNo(node.getLineNumber());
		int lastLineNumber = convertAntlrLineNoToEclipseLineNo(node
				.getLastLineNumber());
		addPosition(lineNumber, lastLineNumber);
	}

	/**
	 * Projection supports code folding based on line numbers.
	 * 
	 * @param startLineNo
	 * @param endLineNo
	 * @return
	 */
	private void addPosition(int startLineNo, int endLineNo) {
		if (startLineNo < endLineNo) {
			try {
				int startOffset = document.getLineOffset(startLineNo);
				int endOffset = document.getLineOffset(endLineNo)
						+ document.getLineLength(endLineNo);
				int length = endOffset - startOffset;
				Position position = new Position(startOffset, length);
				positions.add(position);
			} catch (BadLocationException e) {
			}
		}
	}

	/**
	 * Antlr line numbers start with 1, eclipse line numbers with 0.
	 * 
	 * @param antlrLineNo
	 * @return
	 */
	private int convertAntlrLineNoToEclipseLineNo(int antlrLineNo) {
		return antlrLineNo - 1;
	}
}
