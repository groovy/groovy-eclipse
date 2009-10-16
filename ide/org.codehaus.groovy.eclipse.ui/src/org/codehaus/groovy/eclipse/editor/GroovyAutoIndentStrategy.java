 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.corext.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.FastJavaPartitionScanner;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.Symbols;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

/**
 * Auto indent strategy sensitive to brackets.
 * This is a copy of JavaAutoIndentStrategy except it holds an instance of GroovyIndenter instead
 * of JavaIndenter
 */
public class GroovyAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

	/** The line comment introducer. Value is "{@value}" */
	private static final String LINE_COMMENT= "//"; //$NON-NLS-1$

		private static class CompilationUnitInfo {

			char[] buffer;
			int delta;

			CompilationUnitInfo(char[] buffer, int delta) {
				this.buffer= buffer;
				this.delta= delta;
			}
		}


	private boolean fCloseBrace;
	private boolean fIsSmartMode;

	private String fPartitioning;
	private final IJavaProject fProject;

	/**
	 * Creates a new Java auto indent strategy for the given document partitioning.
	 *
	 * @param partitioning the document partitioning
	 * @param project the project to get formatting preferences from, or null to use default preferences
	 */
	public GroovyAutoIndentStrategy(String partitioning, IJavaProject project) {
		fPartitioning= partitioning;
		fProject= project;
 	}

	private int getBracketCount(IDocument d, int startOffset, int endOffset, boolean ignoreCloseBrackets) throws BadLocationException {

		int bracketCount= 0;
		while (startOffset < endOffset) {
			char curr= d.getChar(startOffset);
			startOffset++;
			switch (curr) {
				case '/' :
					if (startOffset < endOffset) {
						char next= d.getChar(startOffset);
						if (next == '*') {
							// a comment starts, advance to the comment end
							startOffset= getCommentEnd(d, startOffset + 1, endOffset);
						} else if (next == '/') {
							// '//'-comment: nothing to do anymore on this line
							startOffset= endOffset;
						}
					}
					break;
				case '*' :
					if (startOffset < endOffset) {
						char next= d.getChar(startOffset);
						if (next == '/') {
							// we have been in a comment: forget what we read before
							bracketCount= 0;
							startOffset++;
						}
					}
					break;
				case '{' :
					bracketCount++;
					ignoreCloseBrackets= false;
					break;
				case '}' :
					if (!ignoreCloseBrackets) {
						bracketCount--;
					}
					break;
				case '"' :
				case '\'' :
					startOffset= getStringEnd(d, startOffset, endOffset, curr);
					break;
				default :
					}
		}
		return bracketCount;
	}

	// ----------- bracket counting ------------------------------------------------------

	private int getCommentEnd(IDocument d, int offset, int endOffset) throws BadLocationException {
		while (offset < endOffset) {
			char curr= d.getChar(offset);
			offset++;
			if (curr == '*') {
				if (offset < endOffset && d.getChar(offset) == '/') {
					return offset + 1;
				}
			}
		}
		return endOffset;
	}

	private String getIndentOfLine(IDocument d, int line) throws BadLocationException {
		if (line > -1) {
			int start= d.getLineOffset(line);
			int end= start + d.getLineLength(line) - 1;
			int whiteEnd= findEndOfWhiteSpace(d, start, end);
			return d.get(start, whiteEnd - start);
		}
		
		return ""; //$NON-NLS-1$
	}

	private int getStringEnd(IDocument d, int offset, int endOffset, char ch) throws BadLocationException {
		while (offset < endOffset) {
			char curr= d.getChar(offset);
			offset++;
			if (curr == '\\') {
				// ignore escaped characters
				offset++;
			} else if (curr == ch) {
				return offset;
			}
		}
		return endOffset;
	}

	private void smartIndentAfterClosingBracket(IDocument d, DocumentCommand c) {
		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			int p= (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			int line= d.getLineOfOffset(p);
			int start= d.getLineOffset(line);
			int whiteend= findEndOfWhiteSpace(d, start, c.offset);

			JavaHeuristicScanner scanner= new JavaHeuristicScanner(d);
			GroovyIndenter indenter= new GroovyIndenter(d, scanner, fProject);

			// shift only when line does not contain any text up to the closing bracket
			if (whiteend == c.offset) {
				// evaluate the line with the opening bracket that matches out closing bracket
				int reference= indenter.findReferencePosition(c.offset, false, true, false, false);
				int indLine= d.getLineOfOffset(reference);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuffer replaceText= new StringBuffer(getIndentOfLine(d, indLine));
					// add the rest of the current line including the just added close bracket
					replaceText.append(d.get(whiteend, c.offset - whiteend));
					replaceText.append(c.text);
					// modify document command
					c.length += c.offset - start;
					c.offset= start;
					c.text= replaceText.toString();
				}
			}
		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}
	}

	private void smartIndentAfterOpeningBracket(IDocument d, DocumentCommand c) {
		if (c.offset < 1 || d.getLength() == 0)
			return;

		JavaHeuristicScanner scanner= new JavaHeuristicScanner(d);

		int p= (c.offset == d.getLength() ? c.offset - 1 : c.offset);

		try {
			// current line
			int line= d.getLineOfOffset(p);
			int lineOffset= d.getLineOffset(line);

			// make sure we don't have any leading comments etc.
			if (d.get(lineOffset, p - lineOffset).trim().length() != 0)
				return;

			// line of last javacode
			int pos= scanner.findNonWhitespaceBackward(p, JavaHeuristicScanner.UNBOUND);
			if (pos == -1)
				return;
			int lastLine= d.getLineOfOffset(pos);

			// only shift if the last java line is further up and is a braceless block candidate
			if (lastLine < line) {

				GroovyIndenter indenter= new GroovyIndenter(d, scanner, fProject);
				StringBuffer indent= indenter.computeIndentation(p, true);
				String toDelete= d.get(lineOffset, c.offset - lineOffset);
				if (indent != null && !indent.toString().equals(toDelete)) {
					c.text= indent.append(c.text).toString();
					c.length += c.offset - lineOffset;
					c.offset= lineOffset;
				}
			}

		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}

	}

	private void smartIndentAfterNewLine(IDocument d, DocumentCommand c) {
		JavaHeuristicScanner scanner= new JavaHeuristicScanner(d);
		GroovyIndenter indenter= new GroovyIndenter(d, scanner, fProject);
		StringBuffer indent= indenter.computeIndentation(c.offset);
		if (indent == null)
			indent= new StringBuffer(); 

		int docLength= d.getLength();
		if (c.offset == -1 || docLength == 0)
			return;

		try {
			int p= (c.offset == docLength ? c.offset - 1 : c.offset);
			int line= d.getLineOfOffset(p);

			StringBuffer buf= new StringBuffer(c.text + indent);


			IRegion reg= d.getLineInformation(line);
			int lineEnd= reg.getOffset() + reg.getLength();

			int contentStart= findEndOfWhiteSpace(d, c.offset, lineEnd);
			c.length=  Math.max(contentStart - c.offset, 0);

			int start= reg.getOffset();
			ITypedRegion region= TextUtilities.getPartition(d, fPartitioning, start, true);
			if (IJavaPartitions.JAVA_DOC.equals(region.getType()))
				start= d.getLineInformationOfOffset(region.getOffset()).getOffset();

			// insert closing brace on new line after an unclosed opening brace
			if (getBracketCount(d, start, c.offset, true) > 0 && closeBrace() && !isClosed(d, c.offset, c.length)) {
				c.caretOffset= c.offset + buf.length();
				c.shiftsCaret= false;

				// copy old content of line behind insertion point to new line
				// unless we think we are inserting an anonymous type definition
				if (c.offset == 0 || !(computeAnonymousPosition(d, c.offset - 1, fPartitioning, lineEnd) != -1)) {
					if (lineEnd - contentStart > 0) {
						c.length=  lineEnd - c.offset;
						buf.append(d.get(contentStart, lineEnd - contentStart).toCharArray());
					}
				}

				buf.append(TextUtilities.getDefaultLineDelimiter(d));
				StringBuffer reference= null;
				int nonWS= findEndOfWhiteSpace(d, start, lineEnd);
				if (nonWS < c.offset && d.getChar(nonWS) == '{')
					reference= new StringBuffer(d.get(start, nonWS - start));
				else
					reference= indenter.getReferenceIndentation(c.offset);
				if (reference != null)
					buf.append(reference);
				buf.append('}');
			}
			// insert extra line upon new line between two braces
			else if (c.offset > start && contentStart < lineEnd && d.getChar(contentStart) == '}') {
				int firstCharPos= scanner.findNonWhitespaceBackward(c.offset - 1, start);
				if (firstCharPos != JavaHeuristicScanner.NOT_FOUND && d.getChar(firstCharPos) == '{') {
					c.caretOffset= c.offset + buf.length();
					c.shiftsCaret= false;

					StringBuffer reference= null;
					int nonWS= findEndOfWhiteSpace(d, start, lineEnd);
					if (nonWS < c.offset && d.getChar(nonWS) == '{')
						reference= new StringBuffer(d.get(start, nonWS - start));
					else
						reference= indenter.getReferenceIndentation(c.offset);

					buf.append(TextUtilities.getDefaultLineDelimiter(d));

					if (reference != null)
						buf.append(reference);
				}
			}
			c.text= buf.toString();

		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}
	}

	/**
	 * Computes an insert position for an opening brace if <code>offset</code> maps to a position in
	 * <code>document</code> with a expression in parenthesis that will take a block after the closing parenthesis.
	 *
	 * @param document the document being modified
	 * @param offset the offset of the caret position, relative to the line start.
	 * @param partitioning the document partitioning
	 * @param max the max position
	 * @return an insert position relative to the line start if <code>line</code> contains a parenthesized expression that can be followed by a block, -1 otherwise
	 */
	private static int computeAnonymousPosition(IDocument document, int offset, String partitioning,  int max) {
		// find the opening parenthesis for every closing parenthesis on the current line after offset
		// return the position behind the closing parenthesis if it looks like a method declaration
		// or an expression for an if, while, for, catch statement

		JavaHeuristicScanner scanner= new JavaHeuristicScanner(document);
		int pos= offset;
		int length= max;
		int scanTo= scanner.scanForward(pos, length, '}');
		if (scanTo == -1)
			scanTo= length;

		int closingParen= findClosingParenToLeft(scanner, pos) - 1;

		while (true) {
			int startScan= closingParen + 1;
			closingParen= scanner.scanForward(startScan, scanTo, ')');
			if (closingParen == -1)
				break;

			int openingParen= scanner.findOpeningPeer(closingParen - 1, '(', ')');

			// no way an expression at the beginning of the document can mean anything
			if (openingParen < 1)
				break;

			// only select insert positions for parenthesis currently embracing the caret
			if (openingParen > pos)
				continue;

			if (looksLikeAnonymousClassDef(document, partitioning, scanner, openingParen - 1))
				return closingParen + 1;

		}

		return -1;
	}

	/**
	 * Finds a closing parenthesis to the left of <code>position</code> in document, where that parenthesis is only
	 * separated by whitespace from <code>position</code>. If no such parenthesis can be found, <code>position</code> is returned.
	 *
	 * @param scanner the java heuristic scanner set up on the document
	 * @param position the first character position in <code>document</code> to be considered
	 * @return the position of a closing parenthesis left to <code>position</code> separated only by whitespace, or <code>position</code> if no parenthesis can be found
	 */
	private static int findClosingParenToLeft(JavaHeuristicScanner scanner, int position) {
		if (position < 1)
			return position;

		if (scanner.previousToken(position - 1, JavaHeuristicScanner.UNBOUND) == Symbols.TokenRPAREN)
			return scanner.getPosition() + 1;
		return position;
	}

	/**
	 * Checks whether the content of <code>document</code> in the range (<code>offset</code>, <code>length</code>)
	 * contains the <code>new</code> keyword.
	 *
	 * @param document the document being modified
	 * @param offset the first character position in <code>document</code> to be considered
	 * @param length the length of the character range to be considered
	 * @param partitioning the document partitioning
	 * @return <code>true</code> if the specified character range contains a <code>new</code> keyword, <code>false</code> otherwise.
	 */
	private static boolean isNewMatch(IDocument document, int offset, int length, String partitioning) {
		Assert.isTrue(length >= 0);
		Assert.isTrue(offset >= 0);
		Assert.isTrue(offset + length < document.getLength() + 1);

		try {
			String text= document.get(offset, length);
			int pos= text.indexOf("new"); //$NON-NLS-1$

			while (pos != -1 && !isDefaultPartition(document, pos + offset, partitioning))
				pos= text.indexOf("new", pos + 2); //$NON-NLS-1$

			if (pos < 0)
				return false;

			if (pos != 0 && Character.isJavaIdentifierPart(text.charAt(pos - 1)))
				return false;

			if (pos + 3 < length && Character.isJavaIdentifierPart(text.charAt(pos + 3)))
				return false;

			return true;

		} catch (BadLocationException e) {
		}
		return false;
	}

	/**
	 * Checks whether the content of <code>document</code> at <code>position</code> looks like an
	 * anonymous class definition. <code>position</code> must be to the left of the opening
	 * parenthesis of the definition's parameter list.
	 *
	 * @param document the document being modified
	 * @param position the first character position in <code>document</code> to be considered
	 * @param partitioning the document partitioning
	 * @return <code>true</code> if the content of <code>document</code> looks like an anonymous class definition, <code>false</code> otherwise
	 */
	private static boolean looksLikeAnonymousClassDef(IDocument document, String partitioning, JavaHeuristicScanner scanner, int position) {
		int previousCommaParenEqual= scanner.scanBackward(position - 1, JavaHeuristicScanner.UNBOUND, new char[] {',', '(', '='});
		if (previousCommaParenEqual == -1 || position < previousCommaParenEqual + 5) // 2 for borders, 3 for "new"
			return false;

		if (isNewMatch(document, previousCommaParenEqual + 1, position - previousCommaParenEqual - 2, partitioning))
			return true;

		return false;
	}

	/**
	 * Checks whether <code>position</code> resides in a default (Java) partition of <code>document</code>.
	 *
	 * @param document the document being modified
	 * @param position the position to be checked
	 * @param partitioning the document partitioning
	 * @return <code>true</code> if <code>position</code> is in the default partition of <code>document</code>, <code>false</code> otherwise
	 */
	private static boolean isDefaultPartition(IDocument document, int position, String partitioning) {
		Assert.isTrue(position >= 0);
		Assert.isTrue(position <= document.getLength());

		try {
			ITypedRegion region= TextUtilities.getPartition(document, partitioning, position, false);
			return region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE);

		} catch (BadLocationException e) {
		}

		return false;
	}

	private boolean isClosed(IDocument document, int offset, int length) {

		CompilationUnitInfo info= getCompilationUnitForMethod(document, offset, fPartitioning);
		if (info == null)
			return false;

		CompilationUnit compilationUnit= null;
		try {
			ASTParser parser= ASTParser.newParser(AST.JLS3);
			parser.setSource(info.buffer);
			compilationUnit= (CompilationUnit) parser.createAST(null);
		} catch (ArrayIndexOutOfBoundsException x) {
			// work around for parser problem
			return false;
		}

		IProblem[] problems= compilationUnit.getProblems();
		for (int i= 0; i != problems.length; ++i) {
			if (problems[i].getID() == IProblem.UnmatchedBracket)
				return true;
		}

		final int relativeOffset= offset - info.delta;

		ASTNode node= NodeFinder.perform(compilationUnit, relativeOffset, length);

		if (length == 0) {
			while (node != null && (relativeOffset == node.getStartPosition() || relativeOffset == node.getStartPosition() + node.getLength()))
				node= node.getParent();
		}
		
		if (node == null)
			return false;

		switch (node.getNodeType()) {
			case ASTNode.BLOCK:
				return getBlockBalance(document, offset, fPartitioning) <= 0;

			case ASTNode.IF_STATEMENT:
			{
				IfStatement ifStatement= (IfStatement) node;
				Expression expression= ifStatement.getExpression();
				IRegion expressionRegion= createRegion(expression, info.delta);
				Statement thenStatement= ifStatement.getThenStatement();
				IRegion thenRegion= createRegion(thenStatement, info.delta);

				// between expression and then statement
				if (expressionRegion.getOffset() + expressionRegion.getLength() <= offset && offset + length <= thenRegion.getOffset())
					return thenStatement != null;

				Statement elseStatement= ifStatement.getElseStatement();
				IRegion elseRegion= createRegion(elseStatement, info.delta);

				if (elseStatement != null) {
					int sourceOffset= thenRegion.getOffset() + thenRegion.getLength();
					int sourceLength= elseRegion.getOffset() - sourceOffset;
					IRegion elseToken= getToken(document, new Region(sourceOffset, sourceLength), ITerminalSymbols.TokenNameelse);
					return elseToken != null && elseToken.getOffset() + elseToken.getLength() <= offset && offset + length < elseRegion.getOffset();
				}
			}
			break;

			case ASTNode.WHILE_STATEMENT:
			case ASTNode.FOR_STATEMENT:
			{
				Expression expression= node.getNodeType() == ASTNode.WHILE_STATEMENT ? ((WhileStatement) node).getExpression() : ((ForStatement) node).getExpression();
				IRegion expressionRegion= createRegion(expression, info.delta);
				Statement body= node.getNodeType() == ASTNode.WHILE_STATEMENT ? ((WhileStatement) node).getBody() : ((ForStatement) node).getBody();
				IRegion bodyRegion= createRegion(body, info.delta);

				// between expression and body statement
				if (expressionRegion.getOffset() + expressionRegion.getLength() <= offset && offset + length <= bodyRegion.getOffset())
					return body != null;
			}
			break;

			case ASTNode.DO_STATEMENT:
			{
				DoStatement doStatement= (DoStatement) node;
				IRegion doRegion= createRegion(doStatement, info.delta);
				Statement body= doStatement.getBody();
				IRegion bodyRegion= createRegion(body, info.delta);

				if (doRegion.getOffset() + doRegion.getLength() <= offset && offset + length <= bodyRegion.getOffset())
					return body != null;
			}
			break;
		}

		return true;
	}

	/**
	 * Installs a java partitioner with <code>document</code>.
	 *
	 * @param document the document
	 */
	private static void installJavaStuff(Document document) {
		String[] types= new String[] {
									  IJavaPartitions.JAVA_DOC,
									  IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
									  IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
									  IJavaPartitions.JAVA_STRING,
									  IJavaPartitions.JAVA_CHARACTER,
									  IDocument.DEFAULT_CONTENT_TYPE
		};
		FastPartitioner partitioner= new FastPartitioner(new FastJavaPartitionScanner(), types);
		partitioner.connect(document);
		document.setDocumentPartitioner(IJavaPartitions.JAVA_PARTITIONING, partitioner);
	}

	/**
	 * Installs a java partitioner with <code>document</code>.
	 *
	 * @param document the document
	 */
	private static void removeJavaStuff(Document document) {
		document.setDocumentPartitioner(IJavaPartitions.JAVA_PARTITIONING, null);
	}

	private void smartPaste(IDocument document, DocumentCommand command) {
		int newOffset= command.offset;
		int newLength= command.length;
		String newText= command.text;

		try {
			JavaHeuristicScanner scanner= new JavaHeuristicScanner(document);
			GroovyIndenter indenter= new GroovyIndenter(document, scanner, fProject);
			int offset= newOffset;

			// reference position to get the indent from
			int refOffset= indenter.findReferencePosition(offset);
			if (refOffset == JavaHeuristicScanner.NOT_FOUND)
				return;
			int peerOffset= getPeerPosition(document, command);
			peerOffset= indenter.findReferencePosition(peerOffset);
			refOffset= Math.min(refOffset, peerOffset);

			// eat any WS before the insertion to the beginning of the line
			int firstLine= 1; // don't format the first line per default, as it has other content before it
			IRegion line= document.getLineInformationOfOffset(offset);
			String notSelected= document.get(line.getOffset(), offset - line.getOffset());
			if (notSelected.trim().length() == 0) {
				newLength += notSelected.length();
				newOffset= line.getOffset();
				firstLine= 0;
			}

			// prefix: the part we need for formatting but won't paste
			IRegion refLine= document.getLineInformationOfOffset(refOffset);
			String prefix= document.get(refLine.getOffset(), newOffset - refLine.getOffset());

			// handle the indentation computation inside a temporary document
			Document temp= new Document(prefix + newText);
			DocumentRewriteSession session= temp.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
			
			// GROOVY Change
			// don't use Groovy indenter, use the GroovyIndentation from refactoring plugin
			ITextSelection sel = new TextSelection(prefix.length(), newText.length());
			DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(sel, temp, GroovyPlugin.getDefault()
		            .getPreferenceStore(), true);
			int indentLevel = formatter.computeIndentLevel(prefix);
			formatter.setIndentationLevel(indentLevel);
			try {
    			TextEdit edit = formatter.format();
    			edit.apply(temp);
			} catch (RuntimeException e) {
			    GroovyCore.logException(e.getMessage(), e);
			}
//			scanner= new JavaHeuristicScanner(temp);
//			indenter= new GroovyIndenter(temp, scanner, fProject);
//			installJavaStuff(temp);
//
//			// indent the first and second line
//			// compute the relative indentation difference from the second line
//			// (as the first might be partially selected) and use the value to
//			// indent all other lines.
//			boolean isIndentDetected= false;
//			StringBuffer addition= new StringBuffer();
//			int insertLength= 0;
//			int first= document.computeNumberOfLines(prefix) + firstLine; // don't format first line
//			int lines= temp.getNumberOfLines();
//			boolean changed= false;
//			for (int l= first; l < lines; l++) { // we don't change the number of lines while adding indents
//
//				IRegion r= temp.getLineInformation(l);
//				int lineOffset= r.getOffset();
//				int lineLength= r.getLength();
//
//				if (lineLength == 0) // don't modify empty lines
//					continue;
//
//				if (!isIndentDetected) {
//
//					// indent the first pasted line
//					String current= getCurrentIndent(temp, l);
//					StringBuffer correct= indenter.computeIndentation(lineOffset);
//					if (correct == null)
//						return; // bail out
//
//					insertLength= subtractIndent(correct, current, addition);
//					if (l != first && temp.get(lineOffset, lineLength).trim().length() != 0) {
//						isIndentDetected= true;
//						if (insertLength == 0) {
//							 // no adjustment needed, bail out
//							if (firstLine == 0) {
//								// but we still need to adjust the first line
//								command.offset= newOffset;
//								command.length= newLength;
//								if (changed)
//									break; // still need to get the leading indent of the first line
//							}
//							return;
//						}
//						removeJavaStuff(temp);
//					} else {
//						changed= insertLength != 0;
//					}
//				}
//
//				// relatively indent all pasted lines
//				if (insertLength > 0)
//					addIndent(temp, l, addition);
//				else if (insertLength < 0)
//					cutIndent(temp, l, -insertLength);
//
//			}
			// GROOVY change end
			
			temp.stopRewriteSession(session);
			newText= temp.get(prefix.length(), temp.getLength() - prefix.length());

			command.offset= newOffset;
			command.length= newLength;
			command.text= newText;

		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}

	}

	/**
	 * Returns the indentation of the line <code>line</code> in <code>document</code>.
	 * The returned string may contain pairs of leading slashes that are considered
	 * part of the indentation. The space before the asterix in a javadoc-like
	 * comment is not considered part of the indentation.
	 *
	 * @param document the document
	 * @param line the line
	 * @return the indentation of <code>line</code> in <code>document</code>
	 * @throws BadLocationException if the document is changed concurrently
	 */
	private static String getCurrentIndent(Document document, int line) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int from= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		// go behind line comments
		int to= from;
		while (to < endOffset - 2 && document.get(to, 2).equals(LINE_COMMENT))
			to += 2;

		while (to < endOffset) {
			char ch= document.getChar(to);
			if (!Character.isWhitespace(ch))
				break;
			to++;
		}

		// don't count the space before javadoc like, asterix-style comment lines
		if (to > from && to < endOffset - 1 && document.get(to - 1, 2).equals(" *")) { //$NON-NLS-1$
			String type= TextUtilities.getContentType(document, IJavaPartitions.JAVA_PARTITIONING, to, true);
			if (type.equals(IJavaPartitions.JAVA_DOC) || type.equals(IJavaPartitions.JAVA_MULTI_LINE_COMMENT))
				to--;
		}

		return document.get(from, to - from);
	}

	/**
	 * Computes the difference of two indentations and returns the difference in
	 * length of current and correct. If the return value is positive, <code>addition</code>
	 * is initialized with a substring of that length of <code>correct</code>.
	 *
	 * @param correct the correct indentation
	 * @param current the current indentation (migth contain non-whitespace)
	 * @param difference a string buffer - if the return value is positive, it will be cleared and set to the substring of <code>current</code> of that length
	 * @return the difference in lenght of <code>correct</code> and <code>current</code>
	 */
	private int subtractIndent(CharSequence correct, CharSequence current, StringBuffer difference) {
		int c1= computeVisualLength(correct);
		int c2= computeVisualLength(current);
		int diff= c1 - c2;
		if (diff <= 0)
			return diff;

		difference.setLength(0);
		int len= 0, i= 0;
		while (len < diff) {
			char c= correct.charAt(i++);
			difference.append(c);
			len += computeVisualLength(c);
		}


		return diff;
	}

	/**
	 * Indents line <code>line</code> in <code>document</code> with <code>indent</code>.
	 * Leaves leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param indent the indentation to insert
	 * @throws BadLocationException on concurrent document modification
	 */
	private static void addIndent(Document document, int line, CharSequence indent) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int insert= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		// go behind line comments
		while (insert < endOffset - 2 && document.get(insert, 2).equals(LINE_COMMENT))
			insert += 2;

		// insert indent
		document.replace(insert, 0, indent.toString());
	}

	/**
	 * Cuts the visual equivalent of <code>toDelete</code> characters out of the
	 * indentation of line <code>line</code> in <code>document</code>. Leaves
	 * leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param toDelete the number of space equivalents to delete.
	 * @throws BadLocationException on concurrent document modification
	 */
	private void cutIndent(Document document, int line, int toDelete) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int from= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		// go behind line comments
		while (from < endOffset - 2 && document.get(from, 2).equals(LINE_COMMENT))
			from += 2;

		int to= from;
		while (toDelete > 0 && to < endOffset) {
			char ch= document.getChar(to);
			if (!Character.isWhitespace(ch))
				break;
			toDelete -= computeVisualLength(ch);
			if (toDelete >= 0)
				to++;
			else
				break;
		}

		document.replace(from, to - from, null);
	}

	/**
	 * Returns the visual length of a given <code>CharSequence</code> taking into
	 * account the visual tabulator length.
	 *
	 * @param seq the string to measure
	 * @return the visual length of <code>seq</code>
	 */
	private int computeVisualLength(CharSequence seq) {
		int size= 0;
		int tablen= getVisualTabLengthPreference();

		for (int i= 0; i < seq.length(); i++) {
			char ch= seq.charAt(i);
			if (ch == '\t') {
				if (tablen != 0)
					size += tablen - size % tablen;
				// else: size stays the same
			} else {
				size++;
			}
		}
		return size;
	}

	/**
	 * Returns the visual length of a given character taking into
	 * account the visual tabulator length.
	 *
	 * @param ch the character to measure
	 * @return the visual length of <code>ch</code>
	 */
	private int computeVisualLength(char ch) {
		if (ch == '\t'){
			return getVisualTabLengthPreference();
		}
		return 1;
	}

	/**
	 * The preference setting for the visual tabulator display.
	 *
	 * @return the number of spaces displayed for a tabulator in the editor
	 */
	private int getVisualTabLengthPreference() {
		return CodeFormatterUtil.getTabWidth(fProject);
	}

	private int getPeerPosition(IDocument document, DocumentCommand command) {
		if (document.getLength() == 0)
			return 0;
    	/*
    	 * Search for scope closers in the pasted text and find their opening peers
    	 * in the document.
    	 */
    	Document pasted= new Document(command.text);
    	installJavaStuff(pasted);
    	int firstPeer= command.offset;

    	JavaHeuristicScanner pScanner= new JavaHeuristicScanner(pasted);
    	JavaHeuristicScanner dScanner= new JavaHeuristicScanner(document);

    	// add scope relevant after context to peer search
    	int afterToken= dScanner.nextToken(command.offset + command.length, JavaHeuristicScanner.UNBOUND);
    	try {
			switch (afterToken) {
			case Symbols.TokenRBRACE:
				pasted.replace(pasted.getLength(), 0, "}"); //$NON-NLS-1$
				break;
			case Symbols.TokenRPAREN:
				pasted.replace(pasted.getLength(), 0, ")"); //$NON-NLS-1$
				break;
			case Symbols.TokenRBRACKET:
				pasted.replace(pasted.getLength(), 0, "]"); //$NON-NLS-1$
				break;
			}
		} catch (BadLocationException e) {
			// cannot happen
			Assert.isTrue(false);
		}

    	int pPos= 0; // paste text position (increasing from 0)
    	int dPos= Math.max(0, command.offset - 1); // document position (decreasing from paste offset)
    	while (true) {
    		int token= pScanner.nextToken(pPos, JavaHeuristicScanner.UNBOUND);
   			pPos= pScanner.getPosition();
    		switch (token) {
    			case Symbols.TokenLBRACE:
    			case Symbols.TokenLBRACKET:
    			case Symbols.TokenLPAREN:
    				pPos= skipScope(pScanner, pPos, token);
    				if (pPos == JavaHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				break; // closed scope -> keep searching
    			case Symbols.TokenRBRACE:
    				int peer= dScanner.findOpeningPeer(dPos, '{', '}');
    				dPos= peer - 1;
    				if (peer == JavaHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case Symbols.TokenRBRACKET:
    				peer= dScanner.findOpeningPeer(dPos, '[', ']');
    				dPos= peer - 1;
    				if (peer == JavaHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case Symbols.TokenRPAREN:
    				peer= dScanner.findOpeningPeer(dPos, '(', ')');
    				dPos= peer - 1;
    				if (peer == JavaHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case Symbols.TokenCASE:
    			case Symbols.TokenDEFAULT:
    				GroovyIndenter indenter= new GroovyIndenter(document, dScanner, fProject);
    				peer= indenter.findReferencePosition(dPos, false, false, false, true);
    				if (peer == JavaHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching

    			case Symbols.TokenEOF:
    				return firstPeer;
    			default:
    				// keep searching
    		}
    	}
    }

    /**
     * Skips the scope opened by <code>token</code> in <code>document</code>,
     * returns either the position of the
     * @param pos
     * @param token
     * @return the position after the scope
     */
    private static int skipScope(JavaHeuristicScanner scanner, int pos, int token) {
    	int openToken= token;
    	int closeToken;
    	switch (token) {
    		case Symbols.TokenLPAREN:
    			closeToken= Symbols.TokenRPAREN;
    			break;
    		case Symbols.TokenLBRACKET:
    			closeToken= Symbols.TokenRBRACKET;
    			break;
    		case Symbols.TokenLBRACE:
    			closeToken= Symbols.TokenRBRACE;
    			break;
    		default:
    			Assert.isTrue(false);
    			return -1; // dummy
    	}

    	int depth= 1;
    	int p= pos;

    	while (true) {
    		int tok= scanner.nextToken(p, JavaHeuristicScanner.UNBOUND);
    		p= scanner.getPosition();

    		if (tok == openToken) {
    			depth++;
    		} else if (tok == closeToken) {
    			depth--;
    			if (depth == 0)
    				return p + 1;
    		} else if (tok == Symbols.TokenEOF) {
    			return JavaHeuristicScanner.NOT_FOUND;
    		}
    	}
    }

    private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters= document.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.equals(delimiters, text) > -1;
		return false;
	}

	private void smartIndentOnKeypress(IDocument document, DocumentCommand command) {
		switch (command.text.charAt(0)) {
			case '}':
				smartIndentAfterClosingBracket(document, command);
				break;
			case '{':
				smartIndentAfterOpeningBracket(document, command);
				break;
			case 'e':
				smartIndentUponE(document, command);
				break;
		}
	}

	private void smartIndentUponE(IDocument d, DocumentCommand c) {
		if (c.offset < 4 || d.getLength() == 0)
			return;

		try {
			String content= d.get(c.offset - 3, 3);
			if (content.equals("els")) { //$NON-NLS-1$
				JavaHeuristicScanner scanner= new JavaHeuristicScanner(d);
				int p= c.offset - 3;

				// current line
				int line= d.getLineOfOffset(p);
				int lineOffset= d.getLineOffset(line);

				// make sure we don't have any leading comments etc.
				if (d.get(lineOffset, p - lineOffset).trim().length() != 0)
					return;

				// line of last javacode
				int pos= scanner.findNonWhitespaceBackward(p - 1, JavaHeuristicScanner.UNBOUND);
				if (pos == -1)
					return;
				int lastLine= d.getLineOfOffset(pos);

				// only shift if the last java line is further up and is a braceless block candidate
				if (lastLine < line) {

					GroovyIndenter indenter= new GroovyIndenter(d, scanner, fProject);
					int ref= indenter.findReferencePosition(p, true, false, false, false);
					if (ref == JavaHeuristicScanner.NOT_FOUND)
						return;
					int refLine= d.getLineOfOffset(ref);
					String indent= getIndentOfLine(d, refLine);

					if (indent != null) {
						c.text= indent.toString() + "else"; //$NON-NLS-1$
						c.length += c.offset - lineOffset;
						c.offset= lineOffset;
					}
				}

				return;
			}

			if (content.equals("cas")) { //$NON-NLS-1$
				JavaHeuristicScanner scanner= new JavaHeuristicScanner(d);
				int p= c.offset - 3;

				// current line
				int line= d.getLineOfOffset(p);
				int lineOffset= d.getLineOffset(line);

				// make sure we don't have any leading comments etc.
				if (d.get(lineOffset, p - lineOffset).trim().length() != 0)
					return;

				// line of last javacode
				int pos= scanner.findNonWhitespaceBackward(p - 1, JavaHeuristicScanner.UNBOUND);
				if (pos == -1)
					return;
				int lastLine= d.getLineOfOffset(pos);

				// only shift if the last java line is further up and is a braceless block candidate
				if (lastLine < line) {

					GroovyIndenter indenter= new GroovyIndenter(d, scanner, fProject);
					int ref= indenter.findReferencePosition(p, false, false, false, true);
					if (ref == JavaHeuristicScanner.NOT_FOUND)
						return;
					int refLine= d.getLineOfOffset(ref);
					int nextToken= scanner.nextToken(ref, JavaHeuristicScanner.UNBOUND);
					String indent;
					if (nextToken == Symbols.TokenCASE || nextToken == Symbols.TokenDEFAULT)
						indent= getIndentOfLine(d, refLine);
					else // at the brace of the switch
						indent= indenter.computeIndentation(p).toString();

					if (indent != null) {
						c.text= indent.toString() + "case"; //$NON-NLS-1$
						c.length += c.offset - lineOffset;
						c.offset= lineOffset;
					}
				}

				return;
			}

		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}
	}

	/*
	 * @see org.eclipse.jface.text.IAutoIndentStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {

		if (c.doit == false)
			return;

		clearCachedValues();
		if (!isSmartMode()) {
			super.customizeDocumentCommand(d, c);
			return;
		}

		if (c.length == 0 && c.text != null && isLineDelimiter(d, c.text))
			smartIndentAfterNewLine(d, c);
		else if (c.text.length() == 1)
			smartIndentOnKeypress(d, c);
		else if (c.text.length() > 1 && getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_PASTE))
			smartPaste(d, c); // no smart backspace for paste

	}

	private static IPreferenceStore getPreferenceStore() {
		return JavaPlugin.getDefault().getCombinedPreferenceStore();
	}

	private boolean closeBrace() {
		return fCloseBrace;
	}

	private boolean isSmartMode() {
		return fIsSmartMode;
	}

	private void clearCachedValues() {
        IPreferenceStore preferenceStore= getPreferenceStore();
		fCloseBrace= preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACES);
		fIsSmartMode= computeSmartMode();
	}

	private boolean computeSmartMode() {
		IWorkbenchPage page= JavaPlugin.getActivePage();
		if (page != null)  {
			IEditorPart part= page.getActiveEditor();
			if (part instanceof ITextEditorExtension3) {
				ITextEditorExtension3 extension= (ITextEditorExtension3) part;
				return extension.getInsertMode() == ITextEditorExtension3.SMART_INSERT;
			}
		}
		return false;
	}

	private static CompilationUnitInfo getCompilationUnitForMethod(IDocument document, int offset, String partitioning) {
		try {
			JavaHeuristicScanner scanner= new JavaHeuristicScanner(document);

			IRegion sourceRange= scanner.findSurroundingBlock(offset);
			if (sourceRange == null)
				return null;
			String source= document.get(sourceRange.getOffset(), sourceRange.getLength());

			StringBuffer contents= new StringBuffer();
			contents.append("class ____C{void ____m()"); //$NON-NLS-1$
			final int methodOffset= contents.length();
			contents.append(source);
			contents.append('}');

			char[] buffer= contents.toString().toCharArray();

			return new CompilationUnitInfo(buffer, sourceRange.getOffset() - methodOffset);

		} catch (BadLocationException e) {
			JavaPlugin.log(e);
		}

		return null;
	}

	/**
	 * Returns the block balance, i.e. zero if the blocks are balanced at
	 * <code>offset</code>, a negative number if there are more closing than opening
	 * braces, and a positive number if there are more opening than closing braces.
	 *
	 * @param document
	 * @param offset
	 * @param partitioning
	 * @return the block balance
	 */
	private static int getBlockBalance(IDocument document, int offset, String partitioning) {
		if (offset < 1)
			return -1;
		if (offset >= document.getLength())
			return 1;

		int begin= offset;
		int end= offset - 1;

		JavaHeuristicScanner scanner= new JavaHeuristicScanner(document);

		while (true) {
			begin= scanner.findOpeningPeer(begin - 1, '{', '}');
			end= scanner.findClosingPeer(end + 1, '{', '}');
			if (begin == -1 && end == -1)
				return 0;
			if (begin == -1)
				return -1;
			if (end == -1)
				return 1;
		}
	}

	private static IRegion createRegion(ASTNode node, int delta) {
		return node == null ? null : new Region(node.getStartPosition() + delta, node.getLength());
	}

	private static IRegion getToken(IDocument document, IRegion scanRegion, int tokenId)  {

		try {

			final String source= document.get(scanRegion.getOffset(), scanRegion.getLength());

			IScanner scanner= ToolFactory.createScanner(false, false, false, false);
			scanner.setSource(source.toCharArray());

			int id= scanner.getNextToken();
			while (id != ITerminalSymbols.TokenNameEOF && id != tokenId)
				id= scanner.getNextToken();

			if (id == ITerminalSymbols.TokenNameEOF)
				return null;

			int tokenOffset= scanner.getCurrentTokenStartPosition();
			int tokenLength= scanner.getCurrentTokenEndPosition() + 1 - tokenOffset; // inclusive end
			return new Region(tokenOffset + scanRegion.getOffset(), tokenLength);

		} catch (InvalidInputException x) {
			return null;
		} catch (BadLocationException x) {
			return null;
		}
	}
}
