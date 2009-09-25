/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.core.util.RecordedParsingInformation;
import org.eclipse.jdt.internal.formatter.align.Alignment;
import org.eclipse.jdt.internal.formatter.align.AlignmentException;
import org.eclipse.jdt.internal.formatter.comment.CommentFormatterUtil;
import org.eclipse.jdt.internal.formatter.comment.HTMLEntity2JavaReader;
import org.eclipse.jdt.internal.formatter.comment.IJavaDocTagConstants;
import org.eclipse.jdt.internal.formatter.comment.Java2HTMLEntityReader;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * This class is responsible for dumping formatted source
 * @since 2.1
 */
public class Scribe implements IJavaDocTagConstants {

	private static final int INITIAL_SIZE = 100;

	private boolean checkLineWrapping;
	/** one-based column */
	public int column;
	private int[][] commentPositions;

	// Most specific alignment.
	public Alignment currentAlignment;
	public int currentToken;

	// edits management
	private OptimizedReplaceEdit[] edits;
	public int editsIndex;

	public CodeFormatterVisitor formatter;
	public int indentationLevel;
	public int lastNumberOfNewLines;
	public int line;

	private int[] lineEnds;
	private int maxLines;
	private String lineSeparator;
	public Alignment memberAlignment;
	public boolean needSpace = false;

	public int nlsTagCounter;
	public int pageWidth;
	public boolean pendingSpace = false;

	public Scanner scanner;
	public int scannerEndPosition;
	public int tabLength;
	public int indentationSize;
	private final IRegion[] regions;
	private IRegion[] adaptedRegions;
	public int tabChar;
	public int numberOfIndentations;
	private boolean useTabsOnlyForLeadingIndents;

	/** indent empty lines*/
	private final boolean indentEmptyLines;

	/* Comments formatting */
	private static final int INCLUDE_BLOCK_COMMENTS = CodeFormatter.F_INCLUDE_COMMENTS | CodeFormatter.K_MULTI_LINE_COMMENT;
	private static final int INCLUDE_JAVA_DOC = CodeFormatter.F_INCLUDE_COMMENTS | CodeFormatter.K_JAVA_DOC;
	private static final int INCLUDE_LINE_COMMENTS = CodeFormatter.F_INCLUDE_COMMENTS | CodeFormatter.K_SINGLE_LINE_COMMENT;
	private static final int SKIP_FIRST_WHITESPACE_TOKEN = -2;
	private static final int INVALID_TOKEN = 2000;
	private int formatComments = 0;
	private int headerEndPosition = -1;
	String commentIndentation; // indentation requested in comments (usually in javadoc root tags description)

	// New way to format javadoc
	private FormatterCommentParser formatterCommentParser; // specialized parser to format comments

	Scribe(CodeFormatterVisitor formatter, long sourceLevel, IRegion[] regions, CodeSnippetParsingUtil codeSnippetParsingUtil, boolean includeComments) {
		this.scanner = new Scanner(true, true, false/*nls*/, sourceLevel/*sourceLevel*/, null/*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
		this.formatter = formatter;
		this.pageWidth = formatter.preferences.page_width;
		this.tabLength = formatter.preferences.tab_size;
		this.indentationLevel= 0; // initialize properly
		this.numberOfIndentations = 0;
		this.useTabsOnlyForLeadingIndents = formatter.preferences.use_tabs_only_for_leading_indentations;
		this.indentEmptyLines = formatter.preferences.indent_empty_lines;
		this.tabChar = formatter.preferences.tab_char;
		if (this.tabChar == DefaultCodeFormatterOptions.MIXED) {
			this.indentationSize = formatter.preferences.indentation_size;
		} else {
			this.indentationSize = this.tabLength;
		}
		this.lineSeparator = formatter.preferences.line_separator;
		this.indentationLevel = formatter.preferences.initial_indentation_level * this.indentationSize;
		this.regions= regions;
		if (codeSnippetParsingUtil != null) {
			final RecordedParsingInformation information = codeSnippetParsingUtil.recordedParsingInformation;
			if (information != null) {
				this.lineEnds = information.lineEnds;
				this.commentPositions = information.commentPositions;
			}
		}
		if (formatter.preferences.comment_format_line_comment) this.formatComments |= CodeFormatter.K_SINGLE_LINE_COMMENT;
		if (formatter.preferences.comment_format_block_comment) this.formatComments |= CodeFormatter.K_MULTI_LINE_COMMENT;
		if (formatter.preferences.comment_format_javadoc_comment) this.formatComments |= CodeFormatter.K_JAVA_DOC;
		if (includeComments) this.formatComments |= CodeFormatter.F_INCLUDE_COMMENTS;
		reset();
	}

	/**
	 * This method will adapt the selected regions if needed.
	 * If a region should be adapted (see isAdaptableRegion(IRegion))
	 * retrieve correct upper and lower bounds and replace the region.
	 */
	private void adaptRegions() {
		int max = this.regions.length;
		if (max == 1) {
			// It's not necessary to adapt the single region which covers all the source
			if (this.regions[0].getOffset() == 0 && this.regions[0].getLength() == this.scannerEndPosition) {
				this.adaptedRegions = this.regions;
				return;
			}
		}
		this.adaptedRegions = new IRegion[max];
		int commentIndex = 0;
		for (int i = 0; i < max; i++) {
			IRegion aRegion = this.regions[i];
			int offset = aRegion.getOffset();
			int length = aRegion.getLength();

			// First look if the region starts or ends inside a comment
			int index = getCommentIndex(commentIndex, offset);
			int adaptedOffset = offset;
			int adaptedLength = length;
			if (index >= 0) {
				// the offset of the region is inside a comment => restart the region from the comment start
				adaptedOffset = this.commentPositions[index][0];
				if (adaptedOffset >= 0) {
					// adapt only javadoc or block commments. Since fix for bug
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=238210
					// edits in line comments only concerns whitespaces hence can be
					// treated as edits in code
					adaptedLength = length + offset - adaptedOffset;
					commentIndex = index;
					// include also the indentation edit just before the comment if any
					for (int j=0; j<this.editsIndex; j++) {
						int editOffset = this.edits[j].offset;
						int editEnd = editOffset + this.edits[j].length;
						if (editEnd == adaptedOffset) {
							if (j > 0 && this.edits[j].replacement.trim().length() == 0) {
								adaptedLength += adaptedOffset - this.edits[j].offset;
								adaptedOffset = editOffset;
								break;
							}
						} else if (editEnd > adaptedOffset) {
							break;
						}
					}
				}
			}
			index = getCommentIndex(commentIndex, offset+length-1);
			if (index >= 0 && this.commentPositions[index][0] >= 0) { // only javadoc or block comment
				// the region end is inside a comment => set the region end at the comment end
				int commentEnd = this.commentPositions[index][1];
				if (commentEnd < 0) commentEnd = -commentEnd;
				adaptedLength = commentEnd - adaptedOffset;
				commentIndex = index;
			}
			if (adaptedLength != length) {
				// adapt the region and jump to next one
				this.adaptedRegions[i] = new Region(adaptedOffset, adaptedLength);
			} else {
				this.adaptedRegions[i] = aRegion;
			}
		}
	}

	/*
	 * Adapt edits to regions.
	 * 
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=234583"
	 * 	for more details
	 */
	private void adaptEdits() {

		// See if adapting edits is really necessary
		int max = this.regions.length;
		if (max == 1) {
			if (this.regions[0].getOffset() == 0 && this.regions[0].getLength() == this.scannerEndPosition) {
				// No need to adapt as the regions covers the whole source
				return;
			}
		}

		// Sort edits
		OptimizedReplaceEdit[] sortedEdits = new OptimizedReplaceEdit[this.editsIndex];
		System.arraycopy(this.edits, 0, sortedEdits, 0, this.editsIndex);
		Arrays.sort(sortedEdits, new Comparator() {
			public int compare(Object o1, Object o2) {
		    	OptimizedReplaceEdit edit1 = (OptimizedReplaceEdit) o1;
		    	OptimizedReplaceEdit edit2 = (OptimizedReplaceEdit) o2;
				return edit1.offset - edit2.offset;
            }
		});

		// Adapt overlapping edits
		int currentEdit = -1;
		for (int i = 0; i < max; i++) {
			IRegion region = this.adaptedRegions[i];
			int offset = region.getOffset();
			int length = region.getLength();

			// modify overlapping edits on the region (if any)
			int index = adaptEdit(sortedEdits, currentEdit, offset, offset+length);
			if (index != -1) {
				currentEdit = index;
			}
		}
	}

	/*
     * Search whether a region overlap edit(s) at its start and/or at its end.
     * If so, modify the concerned edits to keep only the modifications which are
     * inside the given region.
     * 
     * The edit modification is done as follow:
     * 1) start it from the region start if it overlaps the region's start
     * 2) end it at the region end if it overlaps the region's end
     * 3) remove from the replacement string the number of lines which are outside
     * the region: before when overlapping region's start and after when overlapping
     * region's end. Note that the trailing indentation of the replacement string is not
     * kept when the region's end is overlapped because it's always outside the
     * region.
     */
    private int adaptEdit(OptimizedReplaceEdit[] sortedEdits, int start, int regionStart, int regionEnd) {
    	int bottom = start==-1?0:start, top = sortedEdits.length - 1;
    	int topEnd = top;
    	int i = 0;
    	OptimizedReplaceEdit edit = null;
    	int overlapIndex = -1;
        int linesOutside= -1;
    	
    	// Look for an edit overlapping the region start 
    	while (bottom <= top) {
    		i = bottom + (top - bottom) /2;
    		edit = sortedEdits[i];
    		int editStart = edit.offset;
   			int editEnd = editStart + edit.length;
    		if (regionStart < editStart) {  // the edit starts after the region's start => no possible overlap of region's start
    			top = i-1;
    			if (regionEnd < editStart) { // the edit starts after the region's end => no possible overlap of region's end
    				topEnd = top;
    			}
    		} else {
    			if (regionStart >= editEnd) { // the edit ends before the region's start => no possible overlap of region's start
	    			bottom = i+1;
				} else {
					// Count the lines of the edit which are outside the region
					linesOutside = 0;
					this.scanner.resetTo(editStart, editEnd-1);
					while (!this.scanner.atEnd()) {
						boolean before = this.scanner.currentPosition < regionStart;
	                    char ch = (char) this.scanner.getNextChar();
                    	if (ch == '\n' ) {
                    		if (before) linesOutside++;
                    	}
                    }
					
					// Restart the edit at the beginning of the line where the region start
					edit.offset = regionStart;
					edit.length -= edit.offset - editStart;

					// Cut replacement string if necessary
					int length = edit.replacement.length();
					if (length > 0) {

						// Count the lines in replacement string
						int linesReplaced = 0;
						for (int idx=0; idx < length; idx++) {
							if (edit.replacement.charAt(idx) == '\n') linesReplaced++;
						}

						// As the edit starts outside the region, remove first lines from edit string if any
						if (linesReplaced > 0) {
					    	int linesCount = linesOutside >= linesReplaced ? linesReplaced : linesOutside;
					    	if (linesCount > 0) {
					    		int idx=0;
					    		loop: while (idx < length) {
					    			char ch = edit.replacement.charAt(idx);
					    			switch (ch) {
					    				case '\n':
						    				linesCount--;
						    				if (linesCount == 0) {
						    					idx++;
						    					break loop;
						    				}
						    				break;
					    				case '\r':
					    				case ' ':
					    				case '\t':
					    					break;
					    				default:
					    					break loop;
					    			}
					    			idx++;
					    		}
					    		if (idx >= length) {
					    			edit.replacement = ""; //$NON-NLS-1$
					    		} else {
					    			edit.replacement = edit.replacement.substring(idx);
					    		}
					    	}
						}
					}
					overlapIndex = i;
					break;
				}
			}
    	}
    	
    	// Look for an edit overlapping the region end 
    	if (overlapIndex != -1) bottom = overlapIndex;
    	while (bottom <= topEnd) {
    		i = bottom + (topEnd - bottom) /2;
    		edit = sortedEdits[i];
    		int editStart = edit.offset;
   			int editEnd = editStart + edit.length;
    		if (regionEnd < editStart) {	// the edit starts after the region's end => no possible overlap of region's end
    			topEnd = i-1;
    		} else {
    			if (regionEnd >= editEnd) {	// the edit ends before the region's end => no possible overlap of region's end
	    			bottom = i+1;
				} else {
					// Count the lines of the edit which are outside the region
					linesOutside = 0;
					this.scanner.resetTo(editStart, editEnd-1);
					while (!this.scanner.atEnd()) {
						boolean after = this.scanner.currentPosition >= regionEnd;
	                    char ch = (char) this.scanner.getNextChar();
                    	if (ch == '\n' ) {
                    		if (after) linesOutside++;
                    	}
                    }

					// Cut replacement string if necessary
					int length = edit.replacement.length();
					if (length > 0) {

						// Count the lines in replacement string
						int linesReplaced = 0;
						for (int idx=0; idx < length; idx++) {
							if (edit.replacement.charAt(idx) == '\n') linesReplaced++;
						}

						// Set the replacement string to the number of missing new lines
						// As the end of the edit is out of the region, the possible trailing
						// indentation should not be added...
						if (linesReplaced == 0) {
			    			edit.replacement = ""; //$NON-NLS-1$
						} else {
							int linesCount = linesReplaced > linesOutside ? linesReplaced - linesOutside : 0;
							if (linesCount == 0) {
				    			edit.replacement = ""; //$NON-NLS-1$
							} else {
								StringBuffer buffer = new StringBuffer();
								for (int j=0; j<linesCount; j++) {
									buffer.append(this.lineSeparator);
								}
								edit.replacement = buffer.toString();
							}
						}
					}
					edit.length -= editEnd - regionEnd;
					return i;
				}
			}
    	}
    	return overlapIndex;
    }

	private final void addDeleteEdit(int start, int end) {
		if (this.edits.length == this.editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(start, end - start + 1, Util.EMPTY_STRING);
	}

	public final void addInsertEdit(int insertPosition, String insertedString) {
		if (this.edits.length == this.editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(insertPosition, 0, insertedString);
	}

	private final void addOptimizedReplaceEdit(int offset, int length, String replacement) {
		if (this.editsIndex > 0) {
			// try to merge last two edits
			final OptimizedReplaceEdit previous = this.edits[this.editsIndex-1];
			final int previousOffset = previous.offset;
			final int previousLength = previous.length;
			final int endOffsetOfPreviousEdit = previousOffset + previousLength;
			final int replacementLength = replacement.length();
			final String previousReplacement = previous.replacement;
			final int previousReplacementLength = previousReplacement.length();
			if (previousOffset == offset && previousLength == length && (replacementLength == 0 || previousReplacementLength == 0)) {
				if (this.currentAlignment != null) {
					final Location location = this.currentAlignment.location;
					if (location.editsIndex == this.editsIndex) {
						location.editsIndex--;
						location.textEdit = previous;
					}
				}
				this.editsIndex--;
				return;
			}
			if (endOffsetOfPreviousEdit == offset) {
				if (length != 0) {
					if (replacementLength != 0) {
						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength + length, previousReplacement + replacement);
					} else if (previousLength + length == previousReplacementLength) {
						// check the characters. If they are identical, we can get rid of the previous edit
						boolean canBeRemoved = true;
						loop: for (int i = previousOffset; i < previousOffset + previousReplacementLength; i++) {
							if (this.scanner.source[i] != previousReplacement.charAt(i - previousOffset)) {
								this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousReplacementLength, previousReplacement);
								canBeRemoved = false;
								break loop;
							}
						}
						if (canBeRemoved) {
							if (this.currentAlignment != null) {
								final Location location = this.currentAlignment.location;
								if (location.editsIndex == this.editsIndex) {
									location.editsIndex--;
									location.textEdit = previous;
								}
							}
							this.editsIndex--;
						}
					} else {
						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength + length, previousReplacement);
					}
				} else {
					if (replacementLength != 0) {
						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength, previousReplacement + replacement);
					}
				}
			} else if ((offset + length == previousOffset) && (previousLength + length == replacementLength + previousReplacementLength)) {
				// check if both edits corresponds to the orignal source code
				boolean canBeRemoved = true;
				String totalReplacement = replacement + previousReplacement;
				loop: for (int i = 0; i < previousLength + length; i++) {
					if (this.scanner.source[i + offset] != totalReplacement.charAt(i)) {
						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(offset, previousLength + length, totalReplacement);
						canBeRemoved = false;
						break loop;
					}
				}
				if (canBeRemoved) {
					if (this.currentAlignment != null) {
						final Location location = this.currentAlignment.location;
						if (location.editsIndex == this.editsIndex) {
							location.editsIndex--;
							location.textEdit = previous;
						}
					}
					this.editsIndex--;
				}
			} else {
				this.edits[this.editsIndex++] = new OptimizedReplaceEdit(offset, length, replacement);
			}
		} else {
			this.edits[this.editsIndex++] = new OptimizedReplaceEdit(offset, length, replacement);
		}
	}

	public final void addReplaceEdit(int start, int end, String replacement) {
		if (this.edits.length == this.editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(start,  end - start + 1, replacement);
	}

	public void alignFragment(Alignment alignment, int fragmentIndex){
		alignment.fragmentIndex = fragmentIndex;
		alignment.checkColumn();
		alignment.performFragmentEffect();
	}

	public void checkNLSTag(int sourceStart) {
		if (hasNLSTag(sourceStart)) {
			this.nlsTagCounter++;
		}
	}

	private int consumeInvalidToken(int end) {
	    this.scanner.resetTo(this.scanner.startPosition, end);
    	// In case of invalid unicode character, consume the current backslash character before continuing
    	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=233228
	    if (this.scanner.currentCharacter == '\\') {
	    	this.scanner.currentPosition = this.scanner.startPosition+1;
	    }
	    int previousPosition = this.scanner.currentPosition;
	    char ch = (char) this.scanner.getNextChar();
	    if (this.scanner.atEnd()) {
	    	// avoid infinite loop
	    	return INVALID_TOKEN;
	    }
	    while (!this.scanner.atEnd() && ch != '*' && !ScannerHelper.isWhitespace(ch)) {
	    	previousPosition = this.scanner.currentPosition;
	    	ch = (char) this.scanner.getNextChar();
	    }
	    // restore last whitespace
	    this.scanner.currentPosition = previousPosition;
	    return INVALID_TOKEN;
    }

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart){
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
	}

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart, boolean adjust){
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart, adjust);
	}

	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart){
		return createAlignment(name, mode, tieBreakRule, count, sourceRestart, this.formatter.preferences.continuation_indentation, false);
	}

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart, int continuationIndent, boolean adjust){
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart, continuationIndent, adjust);
	}

	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart, int continuationIndent, boolean adjust){
		Alignment alignment = new Alignment(name, mode, tieBreakRule, this, count, sourceRestart, continuationIndent);
		// adjust break indentation
		if (adjust && this.memberAlignment != null) {
			Alignment current = this.memberAlignment;
			while (current.enclosing != null) {
				current = current.enclosing;
			}
			if ((current.mode & Alignment.M_MULTICOLUMN) != 0) {
				final int indentSize = this.indentationSize;
				switch(current.chunkKind) {
					case Alignment.CHUNK_METHOD :
					case Alignment.CHUNK_TYPE :
						if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
							alignment.breakIndentationLevel = this.indentationLevel + indentSize;
						} else {
							alignment.breakIndentationLevel = this.indentationLevel + continuationIndent * indentSize;
						}
						alignment.update();
						break;
					case Alignment.CHUNK_FIELD :
						if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
							alignment.breakIndentationLevel = current.originalIndentationLevel + indentSize;
						} else {
							alignment.breakIndentationLevel = current.originalIndentationLevel + continuationIndent * indentSize;
						}
						alignment.update();
						break;
				}
			} else {
				switch(current.mode & Alignment.SPLIT_MASK) {
					case Alignment.M_COMPACT_SPLIT :
					case Alignment.M_COMPACT_FIRST_BREAK_SPLIT :
					case Alignment.M_NEXT_PER_LINE_SPLIT :
					case Alignment.M_NEXT_SHIFTED_SPLIT :
					case Alignment.M_ONE_PER_LINE_SPLIT :
						final int indentSize = this.indentationSize;
						switch(current.chunkKind) {
							case Alignment.CHUNK_METHOD :
							case Alignment.CHUNK_TYPE :
								if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
									alignment.breakIndentationLevel = this.indentationLevel + indentSize;
								} else {
									alignment.breakIndentationLevel = this.indentationLevel + continuationIndent * indentSize;
								}
								alignment.update();
								break;
							case Alignment.CHUNK_FIELD :
								if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
									alignment.breakIndentationLevel = current.originalIndentationLevel + indentSize;
								} else {
									alignment.breakIndentationLevel = current.originalIndentationLevel + continuationIndent * indentSize;
								}
								alignment.update();
								break;
						}
						break;
				}
			}
		}
		return alignment;
	}

	public Alignment createMemberAlignment(String name, int mode, int count, int sourceRestart) {
		Alignment mAlignment = createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
		mAlignment.breakIndentationLevel = this.indentationLevel;
		return mAlignment;
	}

	public void enterAlignment(Alignment alignment){
		alignment.enclosing = this.currentAlignment;
		alignment.location.lastLocalDeclarationSourceStart = this.formatter.lastLocalDeclarationSourceStart;
		this.currentAlignment = alignment;
	}

	public void enterMemberAlignment(Alignment alignment) {
		alignment.enclosing = this.memberAlignment;
		alignment.location.lastLocalDeclarationSourceStart = this.formatter.lastLocalDeclarationSourceStart;
		this.memberAlignment = alignment;
	}

	public void exitAlignment(Alignment alignment, boolean discardAlignment){
		Alignment current = this.currentAlignment;
		while (current != null){
			if (current == alignment) break;
			current = current.enclosing;
		}
		if (current == null) {
			throw new AbortFormatting("could not find matching alignment: "+alignment); //$NON-NLS-1$
		}
		this.indentationLevel = alignment.location.outputIndentationLevel;
		this.numberOfIndentations = alignment.location.numberOfIndentations;
		this.formatter.lastLocalDeclarationSourceStart = alignment.location.lastLocalDeclarationSourceStart;
		if (discardAlignment){
			this.currentAlignment = alignment.enclosing;
		}
	}

	public void exitMemberAlignment(Alignment alignment){
		Alignment current = this.memberAlignment;
		while (current != null){
			if (current == alignment) break;
			current = current.enclosing;
		}
		if (current == null) {
			throw new AbortFormatting("could not find matching alignment: "+alignment); //$NON-NLS-1$
		}
		this.indentationLevel = current.location.outputIndentationLevel;
		this.numberOfIndentations = current.location.numberOfIndentations;
		this.formatter.lastLocalDeclarationSourceStart = alignment.location.lastLocalDeclarationSourceStart;
		this.memberAlignment = current.enclosing;
	}

	public Alignment getAlignment(String name){
		if (this.currentAlignment != null) {
			return this.currentAlignment.getAlignment(name);
		}
		return null;
	}

	/**
	 * Answer actual indentation level based on true column position
	 * @return int
	 */
	public int getColumnIndentationLevel() {
		return this.column - 1;
	}

	public final int getCommentIndex(int position) {
		if (this.commentPositions == null)
			return -1;
		int length = this.commentPositions.length;
		if (length == 0) {
			return -1;
		}
		int g = 0, d = length - 1;
		int m = 0;
		while (g <= d) {
			m = g + (d - g) / 2;
			int bound = this.commentPositions[m][1];
			if (bound < 0) {
				bound = -bound;
			}
			if (bound < position) {
				g = m + 1;
			} else if (bound > position) {
				d = m - 1;
			} else {
				return m;
			}
		}
		return -(g + 1);
	}

	/*
     * Returns the index of the comment including the given offset position
     * starting the search from the given start index.
     *
     * @param start The start index for the research
     * @param position The position
     * @return The index of the comment if the given position is located inside it, -1 otherwise
     */
    private int getCommentIndex(int start, int position) {
    	int commentsLength = this.commentPositions == null ? 0 : this.commentPositions.length;
    	if (commentsLength == 0) return -1;
    	if (position == 0) {
    		if (commentsLength > 0 && this.commentPositions[0][0]== 0) {
    			return 0;
    		}
    		return -1;
    	}
    	int bottom = start, top = commentsLength - 1;
    	int i = 0;
    	int[] comment = null;
    	while (bottom <= top) {
    		i = bottom + (top - bottom) /2;
    		comment = this.commentPositions[i];
    		int commentStart = comment[0];
    		if (commentStart < 0) commentStart = -commentStart;
    		if (position < commentStart) {
    			top = i-1;
    		} else {
    			int commentEnd = comment[1];
    			if (commentEnd < 0) commentEnd = -commentEnd;
    			if (position >= commentEnd) {
    				bottom = i+1;
    			} else {
    				return i;
    			}
    		}
    	}
    	return -1;
    }

	private IRegion getCoveringAdaptedRegion(int offset, int end) {
		int index = getIndexOfAdaptedRegionAt(offset);

		if (index < 0) {
			index = -(index + 1);
			index--;
			if (index < 0) {
				return null;
			}
		}

		IRegion region = this.adaptedRegions[index];
		if ((region.getOffset() <= offset) && (end <= region.getOffset() + region.getLength() - 1)) {
			return region;
		}
		return null;
	}

	private int getCurrentCommentOffset(int start) {
		int linePtr = -Arrays.binarySearch(this.lineEnds, start);
		int offset = 0;
		int beginningOfLine = getLineEnd(linePtr - 1);
		if (beginningOfLine == -1) {
			beginningOfLine = 0;
		}
		int currentStartPosition = start;
		char[] source = this.scanner.source;

		// find the position of the beginning of the line containing the comment
		while (beginningOfLine > currentStartPosition) {
			if (linePtr > 0) {
				beginningOfLine = getLineEnd(--linePtr);
			} else {
				beginningOfLine = 0;
				break;
			}
		}
		for (int i = currentStartPosition - 1; i >= beginningOfLine ; i--) {
			char currentCharacter = source[i];
			switch (currentCharacter) {
				case '\t' :
					offset += this.tabLength;
					break;
				case '\r' :
				case '\n' :
					break;
				default:
					offset++;
					break;
			}
		}
		return offset;
	}

	public String getEmptyLines(int linesNumber) {
		if (this.nlsTagCounter > 0) {
			return Util.EMPTY_STRING;
		}
		StringBuffer buffer = new StringBuffer();
		if (this.lastNumberOfNewLines == 0) {
			linesNumber++; // add an extra line breaks
			for (int i = 0; i < linesNumber; i++) {
				if (this.indentEmptyLines) printIndentationIfNecessary(buffer);
				buffer.append(this.lineSeparator);
			}
			this.lastNumberOfNewLines += linesNumber;
			this.line += linesNumber;
			this.column = 1;
			this.needSpace = false;
			this.pendingSpace = false;
		} else if (this.lastNumberOfNewLines == 1) {
			for (int i = 0; i < linesNumber; i++) {
				if (this.indentEmptyLines) printIndentationIfNecessary(buffer);
				buffer.append(this.lineSeparator);
			}
			this.lastNumberOfNewLines += linesNumber;
			this.line += linesNumber;
			this.column = 1;
			this.needSpace = false;
			this.pendingSpace = false;
		} else {
			if ((this.lastNumberOfNewLines - 1) >= linesNumber) {
				// there is no need to add new lines
				return Util.EMPTY_STRING;
			}
			final int realNewLineNumber = linesNumber - this.lastNumberOfNewLines + 1;
			for (int i = 0; i < realNewLineNumber; i++) {
				if (this.indentEmptyLines) printIndentationIfNecessary(buffer);
				buffer.append(this.lineSeparator);
			}
			this.lastNumberOfNewLines += realNewLineNumber;
			this.line += realNewLineNumber;
			this.column = 1;
			this.needSpace = false;
			this.pendingSpace = false;
		}
		return String.valueOf(buffer);
	}

	private int getIndexOfAdaptedRegionAt(int offset) {
		if (this.adaptedRegions.length == 1) {
			int offset2 = this.adaptedRegions[0].getOffset();
			if (offset2 == offset) {
				return 0;
			}
			return offset2 < offset ? -2 : -1;
		}
		return Arrays.binarySearch(this.adaptedRegions, new Region(offset, 0), new Comparator() {
			public int compare(Object o1, Object o2) {
				int r1Offset = ((IRegion)o1).getOffset();
				int r2Offset = ((IRegion)o2).getOffset();

				return r1Offset - r2Offset;
			}
		});
	}

	public OptimizedReplaceEdit getLastEdit() {
		if (this.editsIndex > 0) {
			return this.edits[this.editsIndex - 1];
		}
		return null;
	}

	public final int getLineEnd(int lineNumber) {
		if (this.lineEnds == null)
			return -1;
		if (lineNumber >= this.lineEnds.length + 1)
			return this.scannerEndPosition;
		if (lineNumber <= 0)
			return -1;
		return this.lineEnds[lineNumber-1]; // next line start one character behind the lineEnd of the previous line
	}

	Alignment getMemberAlignment() {
		return this.memberAlignment;
	}

	public String getNewLine() {
		if (this.nlsTagCounter > 0) {
			return Util.EMPTY_STRING;
		}
		if (this.lastNumberOfNewLines >= 1) {
			this.column = 1; // ensure that the scribe is at the beginning of a new line
			return Util.EMPTY_STRING;
		}
		this.line++;
		this.lastNumberOfNewLines = 1;
		this.column = 1;
		this.needSpace = false;
		this.pendingSpace = false;
		return this.lineSeparator;
	}

	/**
	 * Answer next indentation level based on column estimated position
	 * (if column is not indented, then use indentationLevel)
	 */
	public int getNextIndentationLevel(int someColumn) {
		int indent = someColumn - 1;
		if (indent == 0)
			return this.indentationLevel;
		if (this.tabChar == DefaultCodeFormatterOptions.TAB) {
			if (this.useTabsOnlyForLeadingIndents) {
				return indent;
			}
			int rem = indent % this.indentationSize;
			int addition = rem == 0 ? 0 : this.indentationSize - rem; // round to superior
			return indent + addition;
		}
		return indent;
	}

	/*
	 * Preserve empty lines depending on given count and preferences.
	 */
	private String getPreserveEmptyLines(int count) {
		if (count == 0) {
			// preserve line breaks in wrapping if specified
			// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=198074
			if (this.currentAlignment != null && !this.formatter.preferences.join_wrapped_lines) {
				// insert a new line only if it has not been already done before
				// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=283476
				if (this.lastNumberOfNewLines == 0) {
					StringBuffer buffer = new StringBuffer(getNewLine());
					int savedIndentation = this.indentationLevel;
					this.indentationLevel = this.currentAlignment.breakIndentationLevel;
					printIndentationIfNecessary(buffer);
					this.indentationLevel = savedIndentation;
					return buffer.toString();
				}
			}
			return Util.EMPTY_STRING;
		}
		if (this.formatter.preferences.number_of_empty_lines_to_preserve != 0) {
			int linesToPreserve = Math.min(count, this.formatter.preferences.number_of_empty_lines_to_preserve);
			return getEmptyLines(linesToPreserve);
		}
		return getNewLine();
	}

	private IRegion getAdaptedRegionAt(int offset) {
		int index = getIndexOfAdaptedRegionAt(offset);
		if (index < 0) {
			return null;
		}

		return this.adaptedRegions[index];
	}

	public TextEdit getRootEdit() {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=208541
		adaptRegions();
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=234583
		adaptEdits();

		MultiTextEdit edit = null;
		int regionsLength = this.adaptedRegions.length;
		int textRegionStart;
		int textRegionEnd;
		if (regionsLength == 1) {
			IRegion lastRegion = this.adaptedRegions[0];
			textRegionStart = lastRegion.getOffset();
			textRegionEnd = textRegionStart + lastRegion.getLength();
		} else {
			textRegionStart = this.adaptedRegions[0].getOffset();
			IRegion lastRegion = this.adaptedRegions[regionsLength - 1];
			textRegionEnd = lastRegion.getOffset() + lastRegion.getLength();
		}

		int length = textRegionEnd - textRegionStart + 1;
		if (textRegionStart <= 0) {
			if (length <= 0) {
				edit = new MultiTextEdit(0, 0);
			} else {
				edit = new MultiTextEdit(0, textRegionEnd);
			}
		} else {
			edit = new MultiTextEdit(textRegionStart, length - 1);
		}
		for (int i= 0, max = this.editsIndex; i < max; i++) {
			OptimizedReplaceEdit currentEdit = this.edits[i];
			if (isValidEdit(currentEdit)) {
				try {
					edit.addChild(new ReplaceEdit(currentEdit.offset, currentEdit.length, currentEdit.replacement));
				}
				catch (MalformedTreeException ex) {
					// log exception in case of error
					CommentFormatterUtil.log(ex);
 					throw ex;
				}
			}
		}
		this.edits = null;
		return edit;
	}

	public void handleLineTooLong() {
		// search for closest breakable alignment, using tiebreak rules
		// look for outermost breakable one
		int relativeDepth = 0, outerMostDepth = -1;
		Alignment targetAlignment = this.currentAlignment;
		while (targetAlignment != null){
			if (targetAlignment.tieBreakRule == Alignment.R_OUTERMOST && targetAlignment.couldBreak()){
				outerMostDepth = relativeDepth;
			}
			targetAlignment = targetAlignment.enclosing;
			relativeDepth++;
		}
		if (outerMostDepth >= 0) {
			throw new AlignmentException(AlignmentException.LINE_TOO_LONG, outerMostDepth);
		}
		// look for innermost breakable one
		relativeDepth = 0;
		targetAlignment = this.currentAlignment;
		while (targetAlignment != null){
			if (targetAlignment.couldBreak()){
				throw new AlignmentException(AlignmentException.LINE_TOO_LONG, relativeDepth);
			}
			targetAlignment = targetAlignment.enclosing;
			relativeDepth++;
		}
		// did not find any breakable location - proceed
	}

	/*
	 * Check if there is a NLS tag on this line. If yes, return true, returns false otherwise.
	 */
	private boolean hasNLSTag(int sourceStart) {
		// search the last comment where commentEnd < current lineEnd
		if (this.lineEnds == null) return false;
		int index = Arrays.binarySearch(this.lineEnds, sourceStart);
		int currentLineEnd = getLineEnd(-index);
		if (currentLineEnd != -1) {
			int commentIndex = getCommentIndex(currentLineEnd);
			if (commentIndex < 0) {
				commentIndex = -commentIndex - 2;
			}
			if (commentIndex >= 0 && commentIndex < this.commentPositions.length) {
				int start = this.commentPositions[commentIndex][0];
				if (start < 0) {
					start = -start;
					// check that we are on the same line
					int lineIndexForComment = Arrays.binarySearch(this.lineEnds, start);
					if (lineIndexForComment == index) {
						return CharOperation.indexOf(Scanner.TAG_PREFIX, this.scanner.source, true, start, currentLineEnd) != -1;
					}
				}
			}
		}
		return false;
	}

	private boolean includesBlockComments() {
	    return ((this.formatComments & INCLUDE_BLOCK_COMMENTS) == INCLUDE_BLOCK_COMMENTS && this.headerEndPosition < this.scanner.currentPosition) ||
	    	(this.formatter.preferences.comment_format_header && this.headerEndPosition >= this.scanner.currentPosition);
    }

	private boolean includesJavadocComments() {
	    return ((this.formatComments & INCLUDE_JAVA_DOC) == INCLUDE_JAVA_DOC && this.headerEndPosition < this.scanner.currentPosition) ||
	    	(this.formatter.preferences.comment_format_header && this.headerEndPosition >= this.scanner.currentPosition);
    }

	private boolean includesLineComments() {
	    return ((this.formatComments & INCLUDE_LINE_COMMENTS) == INCLUDE_LINE_COMMENTS && this.headerEndPosition < this.scanner.currentPosition) ||
	    	(this.formatter.preferences.comment_format_header && this.headerEndPosition >= this.scanner.currentPosition);
    }

	boolean includesComments() {
	    return (this.formatComments & CodeFormatter.F_INCLUDE_COMMENTS) != 0;
    }

	public void indent() {
		this.indentationLevel += this.indentationSize;
		this.numberOfIndentations++;
	}

	/**
	 * @param compilationUnitSource
	 */
	public void initializeScanner(char[] compilationUnitSource) {
		this.scanner.setSource(compilationUnitSource);
		this.scannerEndPosition = compilationUnitSource.length;
		this.scanner.resetTo(0, this.scannerEndPosition - 1);
		this.edits = new OptimizedReplaceEdit[INITIAL_SIZE];
		this.maxLines = this.lineEnds == null ? -1 : this.lineEnds.length - 1;
		this.scanner.lineEnds = this.lineEnds;
		this.scanner.linePtr = this.maxLines;
		initFormatterCommentParser();
	}

	private void initFormatterCommentParser() {
		if (this.formatterCommentParser == null) {
			this.formatterCommentParser = new FormatterCommentParser(this.scanner.sourceLevel);
		}
		this.formatterCommentParser.scanner.setSource(this.scanner.source);
		this.formatterCommentParser.source = this.scanner.source;
		this.formatterCommentParser.scanner.lineEnds = this.lineEnds;
		this.formatterCommentParser.scanner.linePtr = this.maxLines;
		this.formatterCommentParser.parseHtmlTags = this.formatter.preferences.comment_format_html;
	}

	private boolean isOnFirstColumn(int start) {
		if (this.lineEnds == null) return start == 0;
		int index = Arrays.binarySearch(this.lineEnds, start);
		// we want the line end of the previous line
		int previousLineEnd = getLineEnd(-index - 1);
		return previousLineEnd != -1 && previousLineEnd == start - 1;
	}

	private boolean isValidEdit(OptimizedReplaceEdit edit) {
		final int editLength= edit.length;
		final int editReplacementLength= edit.replacement.length();
		final int editOffset= edit.offset;
		if (editLength != 0) {

			IRegion covering = getCoveringAdaptedRegion(editOffset, (editOffset + editLength - 1));
			if (covering != null) {
				if (editReplacementLength != 0 && editLength == editReplacementLength) {
					for (int i = editOffset, max = editOffset + editLength; i < max; i++) {
						if (this.scanner.source[i] != edit.replacement.charAt(i - editOffset)) {
							return true;
						}
					}
					return false;
				}
				return true;
			}

			IRegion starting = getAdaptedRegionAt(editOffset + editLength);
			if (starting != null) {
				int i = editOffset;
				for (int max = editOffset + editLength; i < max; i++) {
					int replacementStringIndex = i - editOffset;
					if (replacementStringIndex >= editReplacementLength || this.scanner.source[i] != edit.replacement.charAt(replacementStringIndex)) {
						break;
					}
				}
				if (i - editOffset != editReplacementLength && i != editOffset + editLength - 1) {
					edit.offset = starting.getOffset();
					edit.length = 0;
					edit.replacement = edit.replacement.substring(i - editOffset);
					return true;
				}
			}

			return false;
		}

		IRegion covering = getCoveringAdaptedRegion(editOffset, editOffset);
		if (covering != null) {
			return true;
		}

		if (editOffset == this.scannerEndPosition) {
			int index = Arrays.binarySearch(
				this.adaptedRegions,
				new Region(editOffset, 0),
				new Comparator() {
					public int compare(Object o1, Object o2) {
						IRegion r1 = (IRegion)o1;
						IRegion r2 = (IRegion)o2;

						int r1End = r1.getOffset() + r1.getLength();
						int r2End = r2.getOffset() + r2.getLength();

						return r1End - r2End;
					}
				});
			if (index < 0) {
				return false;
			}
			return true;
		}
		return false;
	}

	private void preserveEmptyLines(int count, int insertPosition) {
		if (count > 0) {
			if (this.formatter.preferences.number_of_empty_lines_to_preserve != 0) {
				int linesToPreserve = Math.min(count, this.formatter.preferences.number_of_empty_lines_to_preserve);
				this.printEmptyLines(linesToPreserve, insertPosition);
			} else {
				printNewLine(insertPosition);
			}
		}
	}

	private void print(int length, boolean considerSpaceIfAny) {
		if (this.checkLineWrapping && length + this.column > this.pageWidth) {
			handleLineTooLong();
		}
		this.lastNumberOfNewLines = 0;
		if (this.indentationLevel != 0) {
			printIndentationIfNecessary();
		}
		if (considerSpaceIfAny) {
			space();
		}
		if (this.pendingSpace) {
			addInsertEdit(this.scanner.getCurrentTokenStartPosition(), " "); //$NON-NLS-1$
		}
		this.pendingSpace = false;
		this.needSpace = false;
		this.column += length;
		this.needSpace = true;
	}

	private void printBlockComment(boolean isJavadoc) {
		int currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
		int currentTokenEndPosition = this.scanner.getCurrentTokenEndPosition() + 1;
		boolean includesBlockComments = !isJavadoc && includesBlockComments();

		this.scanner.resetTo(currentTokenStartPosition, currentTokenEndPosition - 1);
		int currentCharacter;
		boolean isNewLine = false;
		int start = currentTokenStartPosition;
		int nextCharacterStart = currentTokenStartPosition;
		int previousStart = currentTokenStartPosition;
		boolean onFirstColumn = isOnFirstColumn(start);

		boolean indentComment = false;
		if (this.indentationLevel != 0) {
			if (isJavadoc
					|| !this.formatter.preferences.never_indent_block_comments_on_first_column
					|| !onFirstColumn) {
				indentComment = true;
				printIndentationIfNecessary();
			}
		}
		if (this.pendingSpace) {
			addInsertEdit(currentTokenStartPosition, " "); //$NON-NLS-1$
		}
		this.needSpace = false;
		this.pendingSpace = false;

		int commentColumn = this.column;
		if (includesBlockComments) {
			if (printBlockComment(currentTokenStartPosition, currentTokenEndPosition)) {
				return;
			}
		}

		int currentIndentationLevel = this.indentationLevel;
		if ((commentColumn-1) > this.indentationLevel) {
			this.indentationLevel = commentColumn-1;
		}
		int currentCommentOffset = onFirstColumn ? 0 : getCurrentCommentOffset(start);
		boolean formatComment = (isJavadoc && (this.formatComments & CodeFormatter.K_JAVA_DOC) != 0) || (!isJavadoc && (this.formatComments & CodeFormatter.K_MULTI_LINE_COMMENT) != 0);

		try {
			while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter = this.scanner.getNextChar()) != -1) {
				nextCharacterStart = this.scanner.currentPosition;
	
				switch(currentCharacter) {
					case '\r' :
						start = previousStart;
						isNewLine = true;
						if (this.scanner.getNextChar('\n')) {
							currentCharacter = '\n';
							nextCharacterStart = this.scanner.currentPosition;
						}
						break;
					case '\n' :
						start = previousStart;
						isNewLine = true;
						nextCharacterStart = this.scanner.currentPosition;
						break;
					default:
						if (isNewLine) {
							this.column = 1;
							this.line++;
							isNewLine = false;
	
							StringBuffer buffer = new StringBuffer();
							if (onFirstColumn) {
								// simply insert indentation if necessary
								buffer.append(this.lineSeparator);
								if (indentComment) {
									printIndentationIfNecessary(buffer);
								}
								if (formatComment) {
									if (ScannerHelper.isWhitespace((char) currentCharacter)) {
										int previousStartPosition = this.scanner.currentPosition;
										while(currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n' && ScannerHelper.isWhitespace((char) currentCharacter)) {
											previousStart = nextCharacterStart;
											previousStartPosition = this.scanner.currentPosition;
											currentCharacter = this.scanner.getNextChar();
											nextCharacterStart = this.scanner.currentPosition;
										}
										if (currentCharacter == '\r' || currentCharacter == '\n') {
											nextCharacterStart = previousStartPosition;
										}
									}
									if (currentCharacter != '\r' && currentCharacter != '\n') {
										buffer.append(' ');
									}
								}
							} else {
								if (ScannerHelper.isWhitespace((char) currentCharacter)) {
									int previousStartPosition = this.scanner.currentPosition;
									int count = 0;
									loop: while(currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n' && ScannerHelper.isWhitespace((char) currentCharacter)) {
										if (count >= currentCommentOffset) {
											break loop;
										}
										previousStart = nextCharacterStart;
										previousStartPosition = this.scanner.currentPosition;
										switch(currentCharacter) {
											case '\t' :
												count += this.tabLength;
												break;
											default :
												count ++;
										}
										currentCharacter = this.scanner.getNextChar();
										nextCharacterStart = this.scanner.currentPosition;
									}
									if (currentCharacter == '\r' || currentCharacter == '\n') {
										nextCharacterStart = previousStartPosition;
									}
								}
								buffer.append(this.lineSeparator);
								if (indentComment) {
									printIndentationIfNecessary(buffer);
								}
								if (formatComment) {
									int previousStartTemp = previousStart;
									int nextCharacterStartTemp = nextCharacterStart;
									while(currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n' && ScannerHelper.isWhitespace((char) currentCharacter)) {
										previousStart = nextCharacterStart;
										currentCharacter = this.scanner.getNextChar();
										nextCharacterStart = this.scanner.currentPosition;
									}
									if (currentCharacter == '*') {
										buffer.append(' ');
									} else {
										previousStart = previousStartTemp;
										nextCharacterStart = nextCharacterStartTemp;
									}
									this.scanner.currentPosition = nextCharacterStart;
								}
							}
							addReplaceEdit(start, previousStart - 1, String.valueOf(buffer));
						} else {
							this.column += (nextCharacterStart - previousStart);
						}
				}
				previousStart = nextCharacterStart;
				this.scanner.currentPosition = nextCharacterStart;
			}
		} finally {
			this.indentationLevel = currentIndentationLevel;
		}
		this.lastNumberOfNewLines = 0;
		this.needSpace = false;
		this.scanner.resetTo(currentTokenEndPosition, this.scannerEndPosition - 1);
	}

	private boolean printBlockComment(int currentTokenStartPosition, int currentTokenEndPosition) {

		// Compute indentation
		int maxColumn = this.formatter.preferences.comment_line_length + 1;
		int indentLevel = this.indentationLevel;
		int indentations = this.numberOfIndentations;
		switch (this.tabChar) {
			case DefaultCodeFormatterOptions.TAB:
				switch (this.tabLength) {
					case 0:
						this.indentationLevel = 0;
						this.column = 1;
						this.numberOfIndentations = 0;
						break;
					case 1:
						this.indentationLevel = this.column - 1;
						this.numberOfIndentations = this.indentationLevel;
						break;
					default:
						this.indentationLevel = (this.column / this.tabLength) * this.tabLength;
						this.column = this.indentationLevel + 1;
						this.numberOfIndentations = this.indentationLevel / this.tabLength;
				}
				break;
			case DefaultCodeFormatterOptions.MIXED:
				if (this.tabLength == 0) {
					this.indentationLevel = 0;
					this.column = 1;
					this.numberOfIndentations = 0;
				} else {
					this.indentationLevel = this.column - 1;
					this.numberOfIndentations = this.indentationLevel / this.tabLength;
				}
				break;
			case DefaultCodeFormatterOptions.SPACE:
				if (this.indentationSize == 0) {
					this.indentationLevel = 0;
					this.column = 1;
					this.numberOfIndentations = 0;
				} else {
					this.indentationLevel = this.column - 1;
				}
				break;
		}

		// Consume the comment prefix
		StringBuffer buffer = new StringBuffer();
		this.scanner.getNextChar();
		this.scanner.getNextChar();
		this.column += 2;
		this.scanner.skipComments = true;
		StringBuffer tokensBuffer = new StringBuffer();
		int editStart = this.scanner.currentPosition;
		int editEnd = -1;

		// Consume text token per token
		int previousToken = -1;
		boolean newLine = false;
		boolean multiLines = false;
		boolean hasMultiLines = false;
		boolean hasTokens = false;
		boolean bufferHasTokens = false;
		boolean lineHasTokens = false;
		int hasTextOnFirstLine = 0;
		boolean firstWord = true;
		boolean clearBlankLines = this.formatter.preferences.comment_clear_blank_lines_in_block_comment;
		boolean joinLines = this.formatter.preferences.join_lines_in_comments;
		int scannerLine = Util.getLineNumber(this.scanner.currentPosition, this.lineEnds, 0, this.maxLines);
		int firstLine = scannerLine;
		int lineNumber = scannerLine;
		int lastTextLine = -1;
		while (!this.scanner.atEnd()) {

			// Consume token
			int token;
			try {
				token = this.scanner.getNextToken();
			} catch (InvalidInputException iie) {
	    		token = consumeInvalidToken(currentTokenEndPosition-1);
				newLine = false;
			}

			// Look at specific tokens
    		boolean insertSpace = (previousToken == TerminalTokens.TokenNameWHITESPACE) && (!firstWord || !hasTokens);
    		boolean isTokenStar = false;
			switch (token) {
				case TerminalTokens.TokenNameWHITESPACE:
					if (tokensBuffer.length() > 0) {
						if (hasTextOnFirstLine == 1 && multiLines) {
							printBlockCommentHeaderLine(buffer);
							hasTextOnFirstLine = -1;
						}
						buffer.append(tokensBuffer);
						this.column += tokensBuffer.length();
						tokensBuffer.setLength(0);
						bufferHasTokens = true;
					}
					if (previousToken == -1) {
						// do not remember the first whitespace
						previousToken = SKIP_FIRST_WHITESPACE_TOKEN;
					} else {
						previousToken = token;
					}
					lineNumber = Util.getLineNumber(this.scanner.currentPosition, this.lineEnds, scannerLine>1 ? scannerLine-2 : 0, this.maxLines);
					if (lineNumber > scannerLine) {
						hasMultiLines = true;
						newLine = true;
					}
					scannerLine = lineNumber;
					continue;
				case TerminalTokens.TokenNameMULTIPLY:
					isTokenStar = true;
					lineNumber = Util.getLineNumber(this.scanner.currentPosition, this.lineEnds, scannerLine>1 ? scannerLine-2 : 0, this.maxLines);
					if (lineNumber == firstLine && previousToken == SKIP_FIRST_WHITESPACE_TOKEN) {
						editStart = this.scanner.getCurrentTokenStartPosition();
					}
					previousToken = token;
					if (this.scanner.currentCharacter == '/') {
						editEnd = this.scanner.startPosition - 1;
						// Add remaining buffered tokens
						if (tokensBuffer.length() > 0) {
							buffer.append(tokensBuffer);
							this.column += tokensBuffer.length();
						}
						// end of comment
						if (multiLines || hasMultiLines) {
					    	buffer.append(this.lineSeparator);
					    	this.column = 1;
					    	printIndentationIfNecessary(buffer);
						}
						buffer.append(' ');
						this.column += BLOCK_FOOTER_LENGTH + 1;
				    	this.scanner.getNextChar(); // reach the end of scanner
				    	continue;
					}
					if (newLine) {
						scannerLine = lineNumber;
						newLine = false;
						continue;
					}
					break;
				case TerminalTokens.TokenNameMULTIPLY_EQUAL:
					if (newLine) {
						this.scanner.resetTo(this.scanner.startPosition, currentTokenEndPosition-1);
						this.scanner.getNextChar(); // consume the multiply
						previousToken = TerminalTokens.TokenNameMULTIPLY;
						scannerLine = Util.getLineNumber(this.scanner.currentPosition, this.lineEnds, scannerLine>1 ? scannerLine-2 : 0, this.maxLines);
						continue;
					}
					break;
				case TerminalTokens.TokenNameMINUS:
				case TerminalTokens.TokenNameMINUS_MINUS:
					if (previousToken == -1) {
						// Do not format comment starting with /*-
						// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=230944
						this.indentationLevel = indentLevel;
						this.numberOfIndentations = indentations;
						this.lastNumberOfNewLines = 0;
						this.needSpace = false;
						this.scanner.skipComments = false;
						this.scanner.resetTo(currentTokenStartPosition, currentTokenEndPosition - 1);
						return false;
					}
					break;
				default:
					// do nothing
					break;
			}

			// Look at gap and insert corresponding lines if necessary
			int linesGap;
			int max;
			lineNumber = Util.getLineNumber(this.scanner.currentPosition, this.lineEnds, scannerLine>1 ? scannerLine-2 : 0, this.maxLines);
			if (lastTextLine == -1) {
				linesGap = lineNumber - firstLine;
				max = 0;
			} else {
				linesGap = lineNumber - lastTextLine;
				if (token == TerminalTokens.TokenNameAT && linesGap ==1) {
					// insert one blank line before root tags
					linesGap = 2;
				}
				max = joinLines && lineHasTokens ? 1 : 0;
			}
			if (linesGap > max) {
				if (clearBlankLines) {
					// TODO (frederic) see if there's a bug for the unremoved blank line for root tags
					 if (token == TerminalTokens.TokenNameAT) {
						 linesGap = 1;
					 } else {
						linesGap = (max==0 || !joinLines) ? 1 : 0;
					 }
				}
				for (int i=0; i<linesGap; i++) {
					// Add remaining buffered tokens
					if (tokensBuffer.length() > 0) {
						if (hasTextOnFirstLine == 1) {
							printBlockCommentHeaderLine(buffer);
							hasTextOnFirstLine = -1;
						}
						buffer.append(tokensBuffer);
						tokensBuffer.setLength(0);
						bufferHasTokens = true;
					}
			    	buffer.append(this.lineSeparator);
			    	this.column = 1;
			    	printIndentationIfNecessary(buffer);
		    		buffer.append(BLOCK_LINE_PREFIX);
		    		this.column += BLOCK_LINE_PREFIX_LENGTH;
		    		firstWord = true;
					multiLines = true;
				}
				insertSpace = insertSpace && linesGap == 0;
			}
    		if (newLine) lineHasTokens = false;

			// Increment column
			int tokenStart = this.scanner.getCurrentTokenStartPosition();
    		int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - tokenStart;
    		hasTokens = true;
    		if (!isTokenStar) lineHasTokens = true;
    		if (hasTextOnFirstLine == 0 && !isTokenStar) {
    			if (firstLine == lineNumber) {
	    			hasTextOnFirstLine = 1;
	    			this.column++; // include first space
	    		} else {
	    			hasTextOnFirstLine = -1;
	    		}
    		}
    		int lastColumn = this.column + tokensBuffer.length() + tokenLength;
    		if (insertSpace) lastColumn++;

    		// Append next token inserting a new line if max line is reached
			if (lineHasTokens && !firstWord && lastColumn > maxColumn) {
		    	String tokensString = tokensBuffer.toString().trim();
				// not enough space on the line
				if (hasTextOnFirstLine == 1) {
					printBlockCommentHeaderLine(buffer);
				}
				if ((this.indentationLevel+tokensString.length()+tokenLength) > maxColumn) {
					// there won't be enough room even if we break the line before the buffered tokens
					// So add the buffered tokens now
					buffer.append(tokensString);
					this.column += tokensString.length();
					tokensBuffer.setLength(0);
				}
				if (bufferHasTokens) {
			    	buffer.append(this.lineSeparator);
			    	this.column = 1;
			    	printIndentationIfNecessary(buffer);
		    		buffer.append(BLOCK_LINE_PREFIX);
			    	this.column += BLOCK_LINE_PREFIX_LENGTH;
				}
		    	if (tokensBuffer.length() > 0) {
					buffer.append(tokensString);
					this.column += tokensString.length();
					tokensBuffer.setLength(0);
		    	}
				buffer.append(this.scanner.source, tokenStart, tokenLength);
				bufferHasTokens = true;
				this.column += tokenLength;
				multiLines = true;
				hasTextOnFirstLine = -1;
			} else {
				// append token to the line
				if (insertSpace)  {
					tokensBuffer.append(' ');
				}
				tokensBuffer.append(this.scanner.source, tokenStart, tokenLength);
			}
			previousToken = token;
			newLine = false;
    		firstWord = false;
			scannerLine = lineNumber;
			lastTextLine = lineNumber;
		}

		// Replace block comment text
		if (hasTokens || multiLines) {
			StringBuffer replacement = new StringBuffer();
			if (hasTextOnFirstLine == 1) {
				if ((hasMultiLines || multiLines)) {
					int col = this.column;
					replacement.append(this.lineSeparator);
					this.column = 1;
					printIndentationIfNecessary(replacement);
					replacement.append(BLOCK_LINE_PREFIX);
			    	this.column = col;
				} else {
					replacement.append(' ');
				}
			}
			replacement.append(buffer);
			addReplaceEdit(editStart, editEnd, replacement.toString());
		}

		// Reset
		this.indentationLevel = indentLevel;
		this.numberOfIndentations = indentations;
		this.lastNumberOfNewLines = 0;
		this.needSpace = false;
		this.scanner.resetTo(currentTokenEndPosition, this.scannerEndPosition - 1);
		this.scanner.skipComments = false;
		return true;
	}

	private void printBlockCommentHeaderLine(StringBuffer buffer) {
	    if (buffer.length() == 0) {
	    	buffer.append(this.lineSeparator);
	    	this.column = 1;
	    	printIndentationIfNecessary(buffer);
	    	buffer.append(BLOCK_LINE_PREFIX);
	    	this.column += BLOCK_LINE_PREFIX_LENGTH;
	    } else {
	    	StringBuffer insert = new StringBuffer();
	    	insert.append(this.lineSeparator);
	    	this.column = 1;
	    	printIndentationIfNecessary(insert);
	    	insert.append(BLOCK_LINE_PREFIX);
	    	this.column += BLOCK_LINE_PREFIX_LENGTH;
	    	buffer.insert(0, insert.toString());
	    }
    }

	public void printEndOfCompilationUnit() {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasComment = false;
			boolean hasLineComment = false;
			boolean hasWhitespace = false;
			int count = 0;
			while (true) {
				this.currentToken = this.scanner.getNextToken();
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						count = 0;
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (count == 0) {
							hasWhitespace = true;
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (hasLineComment) {
							preserveEmptyLines(count, this.scanner.getCurrentTokenStartPosition());
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (hasComment) {
							if (count == 1) {
								this.printNewLine(this.scanner.getCurrentTokenStartPosition());
							} else {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							}
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else {
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						}
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;
						count = 0;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						count = 0;
						break;
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.startPosition);
							} else if (count == 1) {
								printNewLine(this.scanner.startPosition);
							}
						} else if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						if (includesJavadocComments()) {
							printJavadocComment(this.scanner.startPosition, this.scanner.currentPosition);
						} else {
							printBlockComment(true);
						}
						printNewLine();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						count = 0;
						break;
					case TerminalTokens.TokenNameSEMICOLON :
						print(this.scanner.currentPosition - this.scanner.startPosition, this.formatter.preferences.insert_space_before_semicolon);
						break;
					case TerminalTokens.TokenNameEOF :
						if (count >= 1 || this.formatter.preferences.insert_new_line_at_end_of_file_if_missing) {
							this.printNewLine(this.scannerEndPosition);
						}
						return;
					default :
						// step back one token
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}
	/*
	 * prints a code snippet
	 */
	private void printCodeSnippet(int startPosition, int endPosition) {
		String snippet = new String(this.scanner.source, startPosition, endPosition - startPosition + 1);

		// 1 - strip content prefix (@see JavaDocRegion#preprocessCodeSnippet)
		ILineTracker tracker= new DefaultLineTracker();
		String contentPrefix= IJavaDocTagConstants.JAVADOC_STAR;

		StringBuffer inputBuffer= new StringBuffer();
		inputBuffer.setLength(0);
		inputBuffer.append(snippet);
		tracker.set(snippet);
		for (int lines= tracker.getNumberOfLines() - 1; lines > 0; lines--) {
			int lineOffset;
			try {
				lineOffset= tracker.getLineOffset(lines);
			} catch (BadLocationException e) {
				// should not happen
				CommentFormatterUtil.log(e);
				return;
			}
			int prefixOffset= inputBuffer.indexOf(contentPrefix, lineOffset);
			if (prefixOffset >= 0 && inputBuffer.substring(lineOffset, prefixOffset).trim().length() == 0)
				inputBuffer.delete(lineOffset, prefixOffset + 1 + 1);
		}

		// 2 - convert HTML to Java (@see JavaDocRegion#convertHtml2Java)
		HTMLEntity2JavaReader reader= new HTMLEntity2JavaReader(new StringReader(inputBuffer.toString()));
		char[] buf= new char[snippet.length()]; // html2text never gets longer, only shorter!
		String convertedSnippet;
		try {
			int read= reader.read(buf);
			convertedSnippet = new String(buf, 0, read);
		} catch (IOException e) {
			// should not happen
			CommentFormatterUtil.log(e);
			return;
		}

		// 3 - format snippet (@see JavaDocRegion#formatCodeSnippet)
		// include comments in case of line comments are present in the snippet
		String formattedSnippet = convertedSnippet;
		Map options = this.formatter.preferences.getMap();
		if (this.scanner.sourceLevel > ClassFileConstants.JDK1_3) {
			options.put(JavaCore.COMPILER_SOURCE, CompilerOptions.versionFromJdkLevel(this.scanner.sourceLevel));
		}
		TextEdit edit= CommentFormatterUtil.format2(CodeFormatter.K_UNKNOWN | CodeFormatter.F_INCLUDE_COMMENTS, convertedSnippet, 0, this.lineSeparator, options);
		if (edit == null) {
			// 3.a - not a valid code to format, keep initial buffer
			formattedSnippet = inputBuffer.toString();
		} else {
			// 3.b - valid code formatted
			// 3.b.i - get the result
			formattedSnippet = CommentFormatterUtil.evaluateFormatterEdit(convertedSnippet, edit, null);

			// 3.b.ii- convert back to HTML (@see JavaDocRegion#convertJava2Html)
			Java2HTMLEntityReader javaReader= new Java2HTMLEntityReader(new StringReader(formattedSnippet));
			buf= new char[256];
			StringBuffer conversionBuffer= new StringBuffer();
			int l;
			try {
				do {
					l= javaReader.read(buf);
					if (l != -1)
						conversionBuffer.append(buf, 0, l);
				} while (l > 0);
				formattedSnippet = conversionBuffer.toString();
			} catch (IOException e) {
				// should not happen
				CommentFormatterUtil.log(e);
				return;
			}
		}

		// 4 - add the content prefix (@see JavaDocRegion#postprocessCodeSnippet)
		StringBuffer outputBuffer = new StringBuffer();
		tracker = new DefaultLineTracker();
		this.column = 1;
		printIndentationIfNecessary(outputBuffer); // append indentation
		outputBuffer.append(BLOCK_LINE_PREFIX);
		String linePrefix = outputBuffer.toString();
		outputBuffer.setLength(0);
		outputBuffer.append(formattedSnippet);
		tracker.set(outputBuffer.toString());
		for (int lines=tracker.getNumberOfLines() - 1; lines > 0; lines--) {
			try {
				outputBuffer.insert(tracker.getLineOffset(lines), linePrefix);
			} catch (BadLocationException e) {
				// should not happen
				CommentFormatterUtil.log(e);
				return;
			}
		}

		// 5 - replace old text with the formatted snippet
		addReplaceEdit(startPosition, endPosition, outputBuffer.toString());
	}

	void printComment() {
		printComment(CodeFormatter.K_UNKNOWN);
	}

	/*
	 * Main method to print and format comments (javadoc, block and single line comments)
	 */
	void printComment(int kind) {
		final boolean rejectLineComment = kind  == CodeFormatter.K_MULTI_LINE_COMMENT || kind == CodeFormatter.K_JAVA_DOC;
		final boolean rejectBlockComment = kind  == CodeFormatter.K_SINGLE_LINE_COMMENT || kind  == CodeFormatter.K_JAVA_DOC;
		final boolean rejectJavadocComment = kind  == CodeFormatter.K_SINGLE_LINE_COMMENT || kind  == CodeFormatter.K_MULTI_LINE_COMMENT;
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasComment = false;
			boolean hasLineComment = false;
			boolean hasWhitespace = false;
			int count = 0;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						count = 0;
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (count == 0) {
							hasWhitespace = true;
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (hasLineComment) {
							preserveEmptyLines(count, this.scanner.getCurrentTokenStartPosition());
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (hasComment) {
							if (count == 1) {
								this.printNewLine(this.scanner.getCurrentTokenStartPosition());
							} else {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							}
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (count != 0 && (!this.formatter.preferences.join_wrapped_lines || this.formatter.preferences.number_of_empty_lines_to_preserve != 0)) {
							addReplaceEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition(), getPreserveEmptyLines(count-1));
						} else {
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						}
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (rejectLineComment) break;
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;
						count = 0;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (rejectBlockComment) break;
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						count = 0;
						break;
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						if (rejectJavadocComment) break;
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						if (includesJavadocComments()) {
							printJavadocComment(this.scanner.startPosition, this.scanner.currentPosition);
						} else {
							printBlockComment(true);
						}
						printNewLine();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						count = 0;
						break;
					default :
						// step back one token
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	void printComment(int kind, String source, int start, int end, int level) {

		// Set scanner
		initializeScanner(source.toCharArray());
		this.scanner.resetTo(start, end);
		// Put back 3.4RC2 code => comment following line  as it has an impact on Linux tests
		// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=234336
		// TODO (frederic) Need more investigations and a better fix in
		// isAdaptableRegion(int) and adaptRegions()
		// this.scannerEndPosition = end;

		// Set indentation level
	    this.numberOfIndentations = level;
	    this.indentationLevel = level * this.indentationSize;
	    this.column = this.indentationLevel + 1;

	    // Print corresponding comment
	    switch (kind) {
	    	case CodeFormatter.K_SINGLE_LINE_COMMENT:
			    printComment(kind);
	    		break;
	    	case CodeFormatter.K_MULTI_LINE_COMMENT:
			    printComment(kind);
	    		break;
	    	case CodeFormatter.K_JAVA_DOC:
	    		printJavadocComment(start, end);
	    		break;
	    }
    }

	private void printLineComment() {
    	int currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
    	int currentTokenEndPosition = this.scanner.getCurrentTokenEndPosition() + 1;
    	boolean includesLineComments = includesLineComments();
    	boolean isNlsTag = false;
    	if (CharOperation.indexOf(Scanner.TAG_PREFIX, this.scanner.source, true, currentTokenStartPosition, currentTokenEndPosition) != -1) {
    		this.nlsTagCounter = 0;
    		isNlsTag = true;
    	}
    	this.scanner.resetTo(currentTokenStartPosition, currentTokenEndPosition - 1);
    	int currentCharacter;
    	int start = currentTokenStartPosition;
    	int nextCharacterStart = currentTokenStartPosition;

    	if (this.indentationLevel != 0) {
    		if (!this.formatter.preferences.never_indent_line_comments_on_first_column
    				|| !isOnFirstColumn(start)) {
    			printIndentationIfNecessary();
    		}
    	}
    	if (this.pendingSpace) {
    		addInsertEdit(currentTokenStartPosition, " "); //$NON-NLS-1$
    	}
    	this.needSpace = false;
    	this.pendingSpace = false;
    	int previousStart = currentTokenStartPosition;

		if (!isNlsTag && includesLineComments) {
			printLineComment(currentTokenStartPosition, currentTokenEndPosition-1);
		} else {
			// do nothing!?
	    	loop: while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter = this.scanner.getNextChar()) != -1) {
	    		nextCharacterStart = this.scanner.currentPosition;

	    		switch(currentCharacter) {
	    			case '\r' :
	    				start = previousStart;
	    				break loop;
	    			case '\n' :
	    				start = previousStart;
	    				break loop;
	    		}
	    		previousStart = nextCharacterStart;
	    	}
	    	if (start != currentTokenStartPosition) {
	    		// this means that the line comment doesn't end the file
	    		addReplaceEdit(start, currentTokenEndPosition - 1, this.lineSeparator);
	    		this.line++;
	    		this.column = 1;
	    		this.lastNumberOfNewLines = 1;
	    	}
		}
    	this.needSpace = false;
    	this.pendingSpace = false;
    	// realign to the proper value
    	if (this.currentAlignment != null) {
    		if (this.memberAlignment != null) {
    			// select the last alignment
    			if (this.currentAlignment.location.inputOffset > this.memberAlignment.location.inputOffset) {
    				if (this.currentAlignment.couldBreak() && this.currentAlignment.wasSplit) {
    					this.currentAlignment.performFragmentEffect();
    				}
    			} else {
    				this.indentationLevel = Math.max(this.indentationLevel, this.memberAlignment.breakIndentationLevel);
    			}
    		} else if (this.currentAlignment.couldBreak() && this.currentAlignment.wasSplit) {
    			this.currentAlignment.performFragmentEffect();
    		}
    	}
    	this.scanner.resetTo(currentTokenEndPosition, this.scannerEndPosition - 1);
    }

	private void printLineComment(int commentStart, int commentEnd) {

		// Compute indentation
		int firstColumn = this.column;
		int indentLevel = this.indentationLevel;
		int indentations = this.numberOfIndentations;
		this.indentationLevel = getNextIndentationLevel(firstColumn);
		this.numberOfIndentations = this.indentationSize==0 ? 0 : this.indentationLevel / this.indentationSize;

		// Consume the comment prefix
		this.scanner.resetTo(commentStart, commentEnd);
		this.scanner.getNextChar();
		this.scanner.getNextChar();
		this.column += 2;

		// Scan the text token per token to compact it and size it the max line length
		int maxColumn = this.formatter.preferences.comment_line_length + 1;
		int previousToken = -1;
		int lastTokenEndPosition = commentStart;
		int spaceStartPosition = -1;
		int spaceEndPosition = -1;
		this.scanner.skipComments = true;
		String newLineString = null;
		this.commentIndentation = null;

		// Consume text token per token
		while (!this.scanner.atEnd()) {
			int token;
			try {
				token = this.scanner.getNextToken();
			} catch (InvalidInputException iie) {
	    		token = consumeInvalidToken(commentEnd);
			}
			switch (token) {
				case TerminalTokens.TokenNameWHITESPACE:
					if (previousToken == -1) {
						// do not remember the first whitespace
						previousToken = SKIP_FIRST_WHITESPACE_TOKEN;
					} else {
						previousToken = token;
					}
					// Remember space position
					spaceStartPosition = this.scanner.getCurrentTokenStartPosition();
					spaceEndPosition = this.scanner.getCurrentTokenEndPosition();
					continue;
				case TerminalTokens.TokenNameEOF:
					continue;
				case TerminalTokens.TokenNameIdentifier:
					if (previousToken == -1 || previousToken == SKIP_FIRST_WHITESPACE_TOKEN) {
						char[] identifier = this.scanner.getCurrentTokenSource();
						int startPosition = this.scanner.getCurrentTokenStartPosition();
						int restartPosition = this.scanner.currentPosition;
						if (CharOperation.equals(identifier, Parser.FALL_THROUGH_TAG, 0, 5/*length of string "$FALL"*/) && this.scanner.currentCharacter == '-') {
							try {
								this.scanner.getNextToken(); //  consume the '-'
								token = this.scanner.getNextToken(); // consume the "THROUGH"
								if (token == TerminalTokens.TokenNameIdentifier) {
									identifier = this.scanner.getCurrentTokenSource();
									if (CharOperation.endsWith(Parser.FALL_THROUGH_TAG, identifier)) {
										// the comment starts with a fall through
										if (previousToken == SKIP_FIRST_WHITESPACE_TOKEN) {
											addReplaceEdit(spaceStartPosition, startPosition-1, " "); //$NON-NLS-1$
										}
										this.scanner.startPosition = startPosition;
										previousToken = token;
										break;
									}
								}
							} catch (InvalidInputException iie) {
								// skip
							}
						}
						// this was not a valid fall-through tag, hence continue to process the comment normally
						this.scanner.startPosition = startPosition;
			    		this.scanner.currentPosition = restartPosition;
					}
					break;
			}
			int tokenStart = this.scanner.getCurrentTokenStartPosition();
    		int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - tokenStart;

			// insert space at the beginning if not present
			if (previousToken == -1 ) {
    			addInsertEdit(this.scanner.startPosition, " "); //$NON-NLS-1$
				this.column++;
			}
			// replace space at the beginning if present
			else if (previousToken == SKIP_FIRST_WHITESPACE_TOKEN) {
				addReplaceEdit(spaceStartPosition, this.scanner.startPosition-1, " "); //$NON-NLS-1$
				this.column++;
				spaceStartPosition = -1; // do not use this position to split the comment
			} else {
				// not on the first token
				boolean insertSpace = previousToken == TerminalTokens.TokenNameWHITESPACE;
				if (insertSpace) {
					// count inserted space if any in token length
					tokenLength++;
				}
				// insert new line if max line width is reached and a space was previously encountered
				if (spaceStartPosition > 0 && (this.column+tokenLength) > maxColumn) {
					this.lastNumberOfNewLines++;
					this.line++;
					if (newLineString == null) {
						StringBuffer newLineBuffer = new StringBuffer(this.lineSeparator);
						this.column = 1;
						printIndentationIfNecessary(newLineBuffer);
					    newLineBuffer.append(LINE_COMMENT_PREFIX);
						this.column += LINE_COMMENT_PREFIX_LENGTH;
						newLineString = newLineBuffer.toString();
				    	firstColumn = this.column;
					} else {
						this.column = firstColumn;
					}
					if (lastTokenEndPosition > spaceEndPosition) {
						this.column += lastTokenEndPosition - (spaceEndPosition + 1); // add all previous tokens lengths since last space
					}
					if (this.edits[this.editsIndex-1].offset == spaceStartPosition) {
						// previous space was already edited, so remove it
						this.editsIndex--;
					}
					addReplaceEdit(spaceStartPosition, spaceEndPosition, newLineString);
					spaceStartPosition = -1;
					if (insertSpace) {
						tokenLength--; // reduce token length as the space will be replaced by the new line
					}
				}
				// replace space if needed
				else if (insertSpace) {
					addReplaceEdit(spaceStartPosition, this.scanner.startPosition-1, " "); //$NON-NLS-1$
				}
			}
			// update column position and store info of the current token
			this.column += tokenLength;
			previousToken = token;
			lastTokenEndPosition = this.scanner.currentPosition;
		}
		this.scanner.skipComments = false;

		// Skip separator if the comment is not at the end of file
		this.indentationLevel = indentLevel;
		this.numberOfIndentations = indentations;
		this.lastNumberOfNewLines = 0;
		this.scanner.resetTo(lastTokenEndPosition, commentEnd);
		while (!this.scanner.atEnd()) {
			spaceEndPosition = this.scanner.currentPosition;
			this.scanner.getNextChar();
			if (this.scanner.currentCharacter == '\n' || this.scanner.currentCharacter == '\r') {
				// line comment is normally ended with new line
				this.column = 1;
				this.line++;
				this.lastNumberOfNewLines++;
				break;
			}
		}
		
		// Delete leading whitespaces if any
		if (previousToken != -1 && lastTokenEndPosition != commentStart && spaceEndPosition > lastTokenEndPosition) {
			addDeleteEdit(lastTokenEndPosition, spaceEndPosition-1);
		}
	}

	public void printEmptyLines(int linesNumber) {
		this.printEmptyLines(linesNumber, this.scanner.getCurrentTokenEndPosition() + 1);
	}

	private void printEmptyLines(int linesNumber, int insertPosition) {
		final String buffer = getEmptyLines(linesNumber);
		if (Util.EMPTY_STRING == buffer) return;
		addInsertEdit(insertPosition, buffer);
	}

	void printIndentationIfNecessary() {
		StringBuffer buffer = new StringBuffer();
		printIndentationIfNecessary(buffer);
		if (buffer.length() > 0) {
			addInsertEdit(this.scanner.getCurrentTokenStartPosition(), buffer.toString());
			this.pendingSpace = false;
		}
	}

	private void printIndentationIfNecessary(StringBuffer buffer) {
		switch(this.tabChar) {
			case DefaultCodeFormatterOptions.TAB :
				boolean useTabsForLeadingIndents = this.useTabsOnlyForLeadingIndents;
				int numberOfLeadingIndents = this.numberOfIndentations;
				int indentationsAsTab = 0;
				if (useTabsForLeadingIndents) {
					while (this.column <= this.indentationLevel) {
						if (indentationsAsTab < numberOfLeadingIndents) {
							if (buffer != null) buffer.append('\t');
							indentationsAsTab++;
							int complement = this.tabLength - ((this.column - 1) % this.tabLength); // amount of space
							this.column += complement;
							this.needSpace = false;
						} else {
							if (buffer != null) buffer.append(' ');
							this.column++;
							this.needSpace = false;
						}
					}
				} else {
					while (this.column <= this.indentationLevel) {
						if (buffer != null) buffer.append('\t');
						int complement = this.tabLength - ((this.column - 1) % this.tabLength); // amount of space
						this.column += complement;
						this.needSpace = false;
					}
				}
				break;
			case DefaultCodeFormatterOptions.SPACE :
				while (this.column <= this.indentationLevel) {
					if (buffer != null) buffer.append(' ');
					this.column++;
					this.needSpace = false;
				}
				break;
			case DefaultCodeFormatterOptions.MIXED :
				useTabsForLeadingIndents = this.useTabsOnlyForLeadingIndents;
				numberOfLeadingIndents = this.numberOfIndentations;
				indentationsAsTab = 0;
				if (useTabsForLeadingIndents) {
					final int columnForLeadingIndents = numberOfLeadingIndents * this.indentationSize;
					while (this.column <= this.indentationLevel) {
						if (this.column <= columnForLeadingIndents) {
							if ((this.column - 1 + this.tabLength) <= this.indentationLevel) {
								if (buffer != null) buffer.append('\t');
								this.column += this.tabLength;
							} else if ((this.column - 1 + this.indentationSize) <= this.indentationLevel) {
								// print one indentation
								for (int i = 0, max = this.indentationSize; i < max; i++) {
									if (buffer != null) buffer.append(' ');
									this.column++;
								}
							} else {
								if (buffer != null) buffer.append(' ');
								this.column++;
							}
						} else {
							for (int i = this.column, max = this.indentationLevel; i <= max; i++) {
								if (buffer != null) buffer.append(' ');
								this.column++;
							}
						}
						this.needSpace = false;
					}
				} else {
					while (this.column <= this.indentationLevel) {
						if ((this.column - 1 + this.tabLength) <= this.indentationLevel) {
							if (buffer != null) buffer.append('\t');
							this.column += this.tabLength;
						} else if ((this.column - 1 + this.indentationSize) <= this.indentationLevel) {
							// print one indentation
							for (int i = 0, max = this.indentationSize; i < max; i++) {
								if (buffer != null) buffer.append(' ');
								this.column++;
							}
						} else {
							if (buffer != null) buffer.append(' ');
							this.column++;
						}
						this.needSpace = false;
					}
				}
				break;
		}
	}

	private void printJavadocBlock(FormatJavadocBlock block) {
		if( block == null) return;

		// Init positions
		int previousEnd = block.tagEnd;
		int maxNodes = block.nodesPtr;

		// Compute indentation
		boolean headerLine = block.isHeaderLine() && this.lastNumberOfNewLines == 0;
		int maxColumn = this.formatter.preferences.comment_line_length + 1;
		if (headerLine) {
			maxColumn++;
		}

		// format tag section if necessary
		if (!block.isInlined()) {
			this.lastNumberOfNewLines = 0;
		}
		if (block.isDescription()) {
			if (!block.isInlined()) {
			    this.commentIndentation = null;
			}
		} else {
			int tagLength = previousEnd - block.sourceStart + 1;
			this.column += tagLength;
			if (!block.isInlined()) {
			    boolean indentRootTags = this.formatter.preferences.comment_indent_root_tags && !block.isInDescription();
			    int commentIndentationLevel = 0;
				if (indentRootTags) {
				    commentIndentationLevel = tagLength + 1;
					boolean indentParamTag = this.formatter.preferences.comment_indent_parameter_description && block.isInParamTag();
					if (indentParamTag) {
						commentIndentationLevel += this.indentationSize;
					}
				}
				if (commentIndentationLevel == 0) {
				    this.commentIndentation = null;
				} else {
		    		StringBuffer indentationBuffer = new StringBuffer();
		        	for (int i=0; i<commentIndentationLevel; i++) {
		    			indentationBuffer.append(' ');
		        	}
	        		this.commentIndentation = indentationBuffer.toString();
		    	}
			}
			FormatJavadocReference reference= block.reference;
			if (reference != null) {
				// format reference
				printJavadocBlockReference(block, reference);
				previousEnd = reference.sourceEnd;
			}

			// Nothing else to do if the tag has no node
			if (maxNodes < 0)  {
				if (block.isInlined()) {
					this.column++;
				}
				return;
			}
		}

		// tag section: iterate through the blocks composing this tag but the last one
		int previousLine = Util.getLineNumber(previousEnd, this.lineEnds, 0, this.maxLines);
		boolean clearBlankLines = this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment;
		boolean joinLines = this.formatter.preferences.join_lines_in_comments;
		for (int i=0; i<=maxNodes; i++) {
			FormatJavadocNode node = block.nodes[i];
			int nodeStart = node.sourceStart;

			// Print empty lines before the node
			int newLines;
			if (i == 0) {
				newLines = this.formatter.preferences.comment_insert_new_line_for_parameter && block.isParamTag() ? 1 : 0;
				if (nodeStart > (previousEnd+1)) {
					if (!clearBlankLines || !joinLines) {
						int startLine = Util.getLineNumber(nodeStart, this.lineEnds, previousLine-1, this.maxLines);
						int gapLine = previousLine;
						if (joinLines) gapLine++; // if not preserving line break then gap must be at least of one line
						if (startLine > gapLine) {
							newLines = startLine - previousLine;
						}
						if (clearBlankLines) {
							// clearing blank lines in this block means that break lines should be preserved, hence only keep one new line
							if (newLines > 0)  newLines = 1;
						}
					}
					if (newLines == 0) {
						newLines = printJavadocBlockNodesNewLines(block, node, previousEnd);
					}
					printJavadocGapLines(previousEnd+1, nodeStart-1, newLines, clearBlankLines, false, null);
				} else {
					StringBuffer buffer = new StringBuffer();
					if (newLines > 0) {
						for (int j=0; j<newLines; j++) {
							printJavadocNewLine(buffer);
						}
						addInsertEdit(nodeStart, buffer.toString());
					}
				}
			} else {
				newLines = this.column > maxColumn ? 1 : 0;
				if (!clearBlankLines && node.lineStart > (previousLine+1)) newLines = node.lineStart - previousLine;
				if (newLines < node.linesBefore) newLines = node.linesBefore;
				if (newLines == 0) {
					newLines = printJavadocBlockNodesNewLines(block, node, previousEnd);
				}
				if (newLines > 0 || nodeStart > (previousEnd+1)) {
		   			printJavadocGapLines(previousEnd+1, nodeStart-1, newLines, clearBlankLines, false, null);
				}
			}
			if (headerLine && newLines > 0) {
				headerLine = false;
				maxColumn--;
			}

			// Print node
			if (node.isText()) {
				FormatJavadocText text = (FormatJavadocText) node;
				if (text.isHtmlTag()) {
					if (text.isImmutableHtmlTag()) {
						// Indent if new line was added
						if (newLines > 0 && this.commentIndentation != null) {
					    	addInsertEdit(node.sourceStart, this.commentIndentation);
					    	this.column += this.commentIndentation.length();
						}
						printJavadocHtmlImmutableTag(text, block, newLines > 0);
						this.column += getTextLength(block, text);
					} else {
						printJavadocHtmlTag(text, block, newLines>0);
					}
				} else {
					printJavadocText(text, block, newLines>0);
				}
			} else {
				if (newLines > 0 && this.commentIndentation != null) {
			    	addInsertEdit(node.sourceStart, this.commentIndentation);
			    	this.column += this.commentIndentation.length();
				}
				printJavadocBlock((FormatJavadocBlock)node);
			}

			// Print empty lines before the node
			previousEnd = node.sourceEnd;
			previousLine = Util.getLineNumber(previousEnd, this.lineEnds, node.lineStart > 1 ? node.lineStart-2 : 0, this.maxLines);
		}
		this.lastNumberOfNewLines = 0;
	}

	private int printJavadocBlockNodesNewLines(FormatJavadocBlock block, FormatJavadocNode node, int previousEnd) {
	   	int maxColumn = this.formatter.preferences.comment_line_length+1;
    	int nodeStart = node.sourceStart;
 	    try {
			this.scanner.resetTo(nodeStart , node.sourceEnd);
	    	int length = 0;
	    	int newLines = 0;
	    	boolean newLine = false;
			boolean headerLine = block.isHeaderLine() && this.lastNumberOfNewLines == 0;
			int firstColumn = 1 + this.indentationLevel + BLOCK_LINE_PREFIX_LENGTH;
			if (this.commentIndentation != null) firstColumn += this.commentIndentation.length();
			if (headerLine) maxColumn++;
	    	if (node.isText()) {
	    		FormatJavadocText text = (FormatJavadocText)node;
    			if (text.isImmutableHtmlTag()) {
			    	if (nodeStart > (previousEnd+1)) {
			    		length++; // include space between nodes
			    	}
    				int lastColumn = this.column + length;
	    			while (!this.scanner.atEnd()) {
	    				int token = this.scanner.getNextToken();
	    				switch (token) {
	    					case TerminalTokens.TokenNameWHITESPACE:
	    						if (CharOperation.indexOf('\n', this.scanner.source, this.scanner.startPosition, this.scanner.currentPosition) >= 0) {
	    							return newLines;
	    						}
	    						length = 1;
	    						break;
	    					case TerminalTokens.TokenNameMULTIPLY:
	    						if (newLine) {
	    							newLine = false;
	    							continue;
	    						}
	    						length = 1;
	    						break;
	    					default:
				    			length = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
	    						break;
	    				}
	    				lastColumn += length;
	    				if (lastColumn > maxColumn) {
							newLines++;
				    		if (headerLine) {
								maxColumn--;
								headerLine = false;
			    			}
							lastColumn = firstColumn;
						}
	    			}
	    			return newLines;
    			}
    			if (text.isHtmlTag()) {
    				if (text.getHtmlTagID() == JAVADOC_SINGLE_BREAK_TAG_ID) {
    					// never break before single break tag
    					return 0;
    				}
	    			// read the html tag
    				this.scanner.getNextToken();
	    			if (this.scanner.getNextToken() == TerminalTokens.TokenNameDIVIDE) {
	    				length++;
	    				this.scanner.getNextToken();
	    			}
	    			length += (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
	    			this.scanner.getNextToken(); // '>'
	    			length++;
	    		} else {
	    			while (true) {
	    				int token = this.scanner.getNextToken();
	    				if (token == TerminalTokens.TokenNameWHITESPACE || token == TerminalTokens.TokenNameEOF) break;
		    			int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
		    			length += tokenLength;
		    			if ((this.column + length) >= maxColumn) {
		    				break;
		    			}
	    			}
	    		}
	    	} else {
	    		FormatJavadocBlock inlinedBlock = (FormatJavadocBlock) node;
	    		length += inlinedBlock.tagEnd - inlinedBlock.sourceStart + 1;  // tag length
	    		if (inlinedBlock.reference != null) {
		    		length++; // space between tag and reference
					this.scanner.resetTo(inlinedBlock.reference.sourceStart, inlinedBlock.reference.sourceEnd);
					int previousToken = -1;
					loop: while (!this.scanner.atEnd()) {
						int token = this.scanner.getNextToken();
			    		int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
						switch (token) {
							case TerminalTokens.TokenNameWHITESPACE:
								if (previousToken == TerminalTokens.TokenNameCOMMA) { // space between method arguments
									length++;
								}
								break;
							case TerminalTokens.TokenNameMULTIPLY:
								break;
							default:
								length += tokenLength;
								if ((this.column+length) > maxColumn) {
									break loop;
								}
					    		break;
						}
						previousToken = token;
		    		}
	    		}
	    		length++; // one more for closing brace
	    	}
	    	if (nodeStart > (previousEnd+1)) {
	    		length++; // include space between nodes
	    	}
    		if ((firstColumn + length) >= maxColumn && node == block.nodes[0]) {
    			// Do not split in this peculiar case as length would be also over the max
    			// length on next line
    			return 0;
    		}
			if ((this.column + length) > maxColumn) {
	    		return 1;
	    	}
	    } catch (InvalidInputException iie) {
	    	// Assume length is one
	    	int tokenLength = 1;
	    	if (nodeStart > (previousEnd+1)) {
	    		tokenLength++; // include space between nodes
	    	}
			if ((this.column + tokenLength) > maxColumn) {
	    		return 1;
	    	}
	    }
	    return 0;
    }

	private void printJavadocBlockReference(FormatJavadocBlock block, FormatJavadocReference reference) {
		int maxColumn = this.formatter.preferences.comment_line_length + 1;
		boolean headerLine = block.isHeaderLine();
		boolean inlined = block.isInlined();
		if (headerLine) maxColumn++;

		// First we need to know what is the indentation
		this.scanner.resetTo(block.tagEnd+1, reference.sourceEnd);
		StringBuffer buffer = new StringBuffer();
		boolean needFormat = false;
		int previousToken = -1;
		int spacePosition = -1;
		String newLineString = null;
		int firstColumn = -1;
		while (!this.scanner.atEnd()) {
			int token;
			try {
				token = this.scanner.getNextToken();
	    		int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
				switch (token) {
					case TerminalTokens.TokenNameWHITESPACE:
						if (previousToken != -1 || tokenLength > 1 || this.scanner.currentCharacter != ' ') needFormat = true;
						switch (previousToken) {
							case TerminalTokens.TokenNameMULTIPLY :
							case TerminalTokens.TokenNameLPAREN:
								break;
							default:	// space between method arguments
								spacePosition = buffer.length();
								// $FALL-THROUGH$ - fall through next case
							case -1:
								buffer.append(' ');
								this.column++;
								break;
						}
						break;
					case TerminalTokens.TokenNameMULTIPLY:
						break;
					default:
						if (!inlined && spacePosition > 0 && (this.column+tokenLength) > maxColumn) {
							// not enough space on the line
							this.lastNumberOfNewLines++;
							this.line++;
							if (newLineString == null) {
								StringBuffer newLineBuffer = new StringBuffer(this.lineSeparator);
						    	this.column = 1;
						    	printIndentationIfNecessary(newLineBuffer);
					    		newLineBuffer.append(BLOCK_LINE_PREFIX);
					    		this.column += BLOCK_LINE_PREFIX_LENGTH;
								if (this.commentIndentation != null) {
							    	newLineBuffer.append(this.commentIndentation);
							    	this.column += this.commentIndentation.length();
						    	}
						    	newLineString = newLineBuffer.substring(0, newLineBuffer.length()-1); // remove last space as buffer will be inserted before a space
						    	firstColumn = this.column;
							} else {
								this.column = firstColumn;
							}
							this.column = firstColumn + buffer.length() - spacePosition - 1;
							buffer.insert(spacePosition, newLineString);
							if (headerLine) {
								headerLine = false;
								maxColumn--;
							}
							spacePosition = -1;
						}
						buffer.append(this.scanner.source, this.scanner.startPosition, tokenLength);
			    		this.column += tokenLength;
			    		break;
				}
				previousToken = token;
			} catch (InvalidInputException iie) {
				// does not happen as syntax is correct
			}
		}
		if (needFormat) {
		    addReplaceEdit(block.tagEnd+1, reference.sourceEnd, buffer.toString());
		}
    }

	private int getTextLength(FormatJavadocBlock block, FormatJavadocText text) {

		// Special case for immutable tags
		if (text.isImmutableHtmlTag()) {
			this.scanner.resetTo(text.sourceStart , text.sourceEnd);
			int textLength = 0;
			while (!this.scanner.atEnd()) {
				try {
	                int token = this.scanner.getNextToken();
	    			if (token == TerminalTokens.TokenNameWHITESPACE) {
						if (CharOperation.indexOf('\n', this.scanner.source, this.scanner.startPosition, this.scanner.currentPosition) >= 0) {
							textLength = 0;
							this.scanner.getNextChar();
							if (this.scanner.currentCharacter == '*') {
								this.scanner.getNextChar();
								if (this.scanner.currentCharacter != ' ') {
									textLength++;
								}
							} else {
								textLength++;
							}
							continue;
						}
	    			}
	    			textLength += (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
                } catch (InvalidInputException e) {
	                return textLength;
                }
			}
			return textLength;
		}

		// Simple for one line tags
	    if (block.isOneLineTag()) {
	    	return text.sourceEnd - text.sourceStart + 1;
	    }

	    // Find last line
    	int startLine = Util.getLineNumber(text.sourceStart, this.lineEnds, 0, this.maxLines);
    	int endLine = startLine;
    	int previousEnd = -1;
    	for (int i=0; i<=text.separatorsPtr; i++) {
    		int end = (int) (text.separators[i] >>> 32);
    		endLine = Util.getLineNumber(end, this.lineEnds, endLine-1, this.maxLines);
    		if (endLine > startLine) {
    			return previousEnd - text.sourceStart + 1;
    		}
    		previousEnd = end;
    	}

    	// This was a one line text
		return text.sourceEnd - text.sourceStart + 1;
    }

	/*
	 * Print and formats a javadoc comments
	 */
	void printJavadocComment(int start, int end) {
		int lastIndentationLevel = this.indentationLevel;
		try {
			// parse the comment on the fly
			this.scanner.resetTo(start, end-1);
			if (! this.formatterCommentParser.parse(start, end-1)) {
				// problem occurred while parsing the javadoc, early abort formatting
				return;
			}

			FormatJavadoc javadoc = (FormatJavadoc) this.formatterCommentParser.docComment;

			// handle indentation
			if (this.indentationLevel != 0) {
				printIndentationIfNecessary();
			}

			// handle pending space if any
			if (this.pendingSpace) {
				addInsertEdit(start, " "); //$NON-NLS-1$
			}

			if (javadoc.blocks == null) {
				// no FormatJavadocTags in this this javadoc
				return;
			}

			// init properly
			this.needSpace = false;
			this.pendingSpace = false;
			int length = javadoc.blocks.length;

			// format empty lines between before the first block
			FormatJavadocBlock previousBlock = javadoc.blocks[0];
			this.lastNumberOfNewLines = 0;
			int currentLine = this.line;
			int firstBlockStart = previousBlock.sourceStart;
			printIndentationIfNecessary(null);
			this.column += JAVADOC_HEADER_LENGTH; // consider that the header is already scanned

			// If there are several blocks in the javadoc
			int index = 1;
			if (length > 1) {
				// format the description if any
				if (previousBlock.isDescription()) {
					printJavadocBlock(previousBlock);
					FormatJavadocBlock block = javadoc.blocks[index++];
					int newLines = this.formatter.preferences.comment_insert_empty_line_before_root_tags ? 2 : 1;
					printJavadocGapLines(previousBlock.sourceEnd+1, block.sourceStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
					previousBlock = block;
				}

				// format all tags but the last one composing this comment
				while (index < length) {
					printJavadocBlock(previousBlock);
					FormatJavadocBlock block = javadoc.blocks[index++];
					printJavadocGapLines(previousBlock.sourceEnd+1, block.sourceStart-1, 1, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
					previousBlock = block;
				}
			}

			// format the last block
			printJavadocBlock(previousBlock);

			// format the header and footer empty spaces
			int newLines = this.line > currentLine || javadoc.isMultiLine() ? 1 : 0;
			printJavadocGapLines(javadoc.textStart, firstBlockStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
			printJavadocGapLines(previousBlock.sourceEnd+1, javadoc.textEnd, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, true, null);
		}
		finally {
			// reset the scanner
			this.scanner.resetTo(end, this.scannerEndPosition - 1);
			this.needSpace = false;
			this.indentationLevel = lastIndentationLevel;
			this.lastNumberOfNewLines = 0;
		}
	}

	/*
	 * prints the empty javadoc line between the 2 given positions.
	 * May insert new '*' before each new line
	 */
	private void printJavadocGapLines(int textStartPosition, int textEndPosition, int newLines, boolean clearBlankLines, boolean footer, StringBuffer output) {
		try {
			// If no lines to set in the gap then just insert a space if there's enough room to
			if (newLines == 0) {
				if (output == null) {
					addReplaceEdit(textStartPosition, textEndPosition,  " "); //$NON-NLS-1$
				} else {
					output.append(' ');
				}
				this.column++;
				return;
			}

			// if there's no enough room to replace text, then insert the gap
			if (textStartPosition > textEndPosition) {
				if (newLines > 0) {
					StringBuffer buffer = new StringBuffer();
					for (int i=0; i<newLines; i++) {
						buffer.append(this.lineSeparator);
						this.column = 1;
						printIndentationIfNecessary(buffer);
						if (footer) {
							buffer.append(' ');
							this.column++;
						} else {
							buffer.append(BLOCK_LINE_PREFIX);
							this.column += BLOCK_LINE_PREFIX_LENGTH;
						}
					}
					if (output == null) {
						addInsertEdit(textStartPosition, buffer.toString());
					} else {
						output.append(buffer);
					}
				}
				return;
			}

			// There's enough room and some lines to set...
			// Skip the text token per token to keep existing stars when possible
			this.scanner.resetTo(textStartPosition, textEndPosition);
			this.scanner.recordLineSeparator = true;
			this.scanner.linePtr = Util.getLineNumber(textStartPosition, this.lineEnds, 0, this.maxLines) - 2;
			int linePtr = this.scanner.linePtr;
			int lineCount = 0;
			int start = textStartPosition;
			while (!this.scanner.atEnd()) {
				switch (this.scanner.getNextToken()) {
					case TerminalTokens.TokenNameMULTIPLY:
						// we just need to replace each lines between '*' with the javadoc formatted ones
						int linesGap = this.scanner.linePtr - linePtr;
						if (linesGap > 0) {
							StringBuffer buffer = new StringBuffer();
							if (lineCount > 0) {
								// TODO (eric) https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
								buffer.append( ' ');
							}
							for (int i = 0; i < linesGap ; i++) {
								if (clearBlankLines && lineCount >= newLines) {
									// leave as the required new lines have been inserted
									// so remove any remaining blanks and leave
									if (textEndPosition >= start) {
										if (output == null) {
											addReplaceEdit(start, textEndPosition, buffer.toString());
										} else {
											output.append(buffer);
										}
									}
									return;
								}
								buffer.append(this.lineSeparator);
								this.column = 1;
								printIndentationIfNecessary(buffer);
								if (i == (linesGap-1)) {
									buffer.append(' ');
									this.column++;
								} else {
									buffer.append(BLOCK_LINE_PREFIX);
									this.column += BLOCK_LINE_PREFIX_LENGTH;
								}
								lineCount++;
							}
							int currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
							int tokenLength = this.scanner.currentPosition - currentTokenStartPosition;
							if (output == null) {
								addReplaceEdit(start, currentTokenStartPosition-1, buffer.toString());
							} else {
								output.append(buffer);
								output.append(this.scanner.source, currentTokenStartPosition, tokenLength);
							}
							this.column += tokenLength;
							if (footer && clearBlankLines && lineCount == newLines) {
								if (textEndPosition >= currentTokenStartPosition) {
									if (output == null) {
										addDeleteEdit(currentTokenStartPosition, textEndPosition);
									}
								}
								return;
							}
						}
						// next start is just after the current token
						start = this.scanner.currentPosition;
						linePtr = this.scanner.linePtr;
						break;
				}
			}

			// Format the last whitespaces
			if (lineCount < newLines) {
				// Insert new lines as not enough was encountered while scanning the whitespaces
				StringBuffer buffer = new StringBuffer();
				if (lineCount > 0) {
					// TODO (eric) https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
					buffer.append( ' ');
				}
				for (int i = lineCount; i < newLines-1; i++) {
					printJavadocNewLine(buffer);
				}
				buffer.append(this.lineSeparator);
				this.column = 1;
				printIndentationIfNecessary(buffer);
				if (footer) {
					buffer.append(' ');
					this.column++;
				} else {
					buffer.append(BLOCK_LINE_PREFIX);
					this.column += BLOCK_LINE_PREFIX_LENGTH;
				}
				if (output == null) {
					if (textEndPosition >= start) {
						addReplaceEdit(start, textEndPosition, buffer.toString());
					} else {
						addInsertEdit(textEndPosition+1, buffer.toString());
					}
				} else {
					output.append(buffer);
				}
			} else {
				// Replace all remaining whitespaces by a single space
				if (textEndPosition >= start) {
					StringBuffer buffer = new StringBuffer();
					if (this.scanner.linePtr > linePtr) {
						if (lineCount > 0) {
							// TODO (eric) https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
							buffer.append( ' ');
						}
						buffer.append(this.lineSeparator);
						this.column = 1;
						printIndentationIfNecessary(buffer);
					}
					buffer.append(' ');
					if (output == null) {
						addReplaceEdit(start, textEndPosition, buffer.toString());
					} else {
						output.append(buffer);
					}
				}
				this.column++;
			}
		}
		catch (InvalidInputException iie) {
			// there's nothing to do if this exception happens
		}
		finally {
			this.scanner.recordLineSeparator = false;
			this.needSpace = false;
			this.scanner.resetTo(textEndPosition+1, this.scannerEndPosition - 1);
			this.lastNumberOfNewLines += newLines;
			this.line += newLines;
		}
	}

	private void printJavadocHtmlImmutableTag(FormatJavadocText text, FormatJavadocBlock block, boolean textOnNewLine) {

		try {
			// Iterate on text line separators
			int lineNumber = text.lineStart;
			this.scanner.tokenizeWhiteSpace = false;
			StringBuffer buffer = null;
			for (int idx=1, max=text.separatorsPtr; idx<max ; idx++) {
				int start = (int) text.separators[idx];
				int lineStart = Util.getLineNumber(start, this.lineEnds, lineNumber, this.maxLines);
				if (buffer == null) {
					buffer = new StringBuffer();
					this.column = 1;
					printIndentationIfNecessary(buffer);
					buffer.append(BLOCK_LINE_PREFIX);
					this.column += BLOCK_LINE_PREFIX_LENGTH;
				}
				while (lineNumber < lineStart) {
					int end = this.lineEnds[lineNumber-1];
					this.scanner.resetTo(end, start);
					int token = this.scanner.getNextToken();
					switch (token) {
						case TerminalTokens.TokenNameMULTIPLY:
						case TerminalTokens.TokenNameMULTIPLY_EQUAL:
							break;
						default:
							return;
					}
					if (this.scanner.currentCharacter == ' ') {
						this.scanner.getNextChar();
					}
					addReplaceEdit(end+1, this.scanner.getCurrentTokenEndPosition(), buffer.toString());
					lineNumber++;
				}
			}
		}
		catch (InvalidInputException iie) {
			// leave
		}
		finally {
			// Reset
			this.needSpace = false;
			this.scanner.tokenizeWhiteSpace = true;
			this.scanner.resetTo(text.sourceEnd+1, this.scannerEndPosition - 1);
		}
	}

	private int printJavadocHtmlTag(FormatJavadocText text, FormatJavadocBlock block, boolean textOnNewLine) {

		// Compute indentation if necessary
		boolean clearBlankLines = this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment;
		boolean headerLine = block.isHeaderLine() && this.lastNumberOfNewLines == 0;
		int firstColumn = 1 + this.indentationLevel + BLOCK_LINE_PREFIX_LENGTH;
		if (headerLine) firstColumn++;

		// Local variables init
		int textStart = text.sourceStart;
		int nextStart = textStart;
		int startLine = Util.getLineNumber(textStart, this.lineEnds, 0, this.maxLines);
	    int htmlTagID = text.getHtmlTagID();
	    StringBuffer buffer = new StringBuffer();

	    // New line will be added before next node
	    int max = text.separatorsPtr;
		int linesAfter = 0;
		int previousEnd = -1;
	    boolean isHtmlBreakTag = htmlTagID == JAVADOC_SINGLE_BREAK_TAG_ID;
		boolean isHtmlSeparatorTag = htmlTagID == JAVADOC_SEPARATOR_TAGS_ID;
		if (isHtmlBreakTag) {
			return 1;
		}

		// Iterate on text line separators
		boolean isCode = htmlTagID == JAVADOC_CODE_TAGS_ID;
		for (int idx=0, ptr=0; idx<=max || (text.htmlNodesPtr != -1 && ptr <= text.htmlNodesPtr); idx++) {

			// append text to buffer realigning with the line length
			int end = (idx > max) ? text.sourceEnd : (int) (text.separators[idx] >>> 32);
			int nodeKind = 0; // text break
			if (text.htmlNodesPtr >= 0 && ptr <= text.htmlNodesPtr && end > text.htmlNodes[ptr].sourceStart) {
				FormatJavadocNode node = text.htmlNodes[ptr];
				FormatJavadocText htmlTag = node.isText() ? (FormatJavadocText) node : null;
				int newLines = htmlTag == null ? 0 : htmlTag.linesBefore;
				if (linesAfter > newLines) {
					newLines = linesAfter;
					if (newLines > 1 && clearBlankLines) {
						if (idx < 2 || (text.htmlIndexes[idx-2] & JAVADOC_TAGS_ID_MASK) != JAVADOC_CODE_TAGS_ID) {
							newLines = 1;
						}
					}
				}
				if (textStart < previousEnd) {
					addReplaceEdit(textStart, previousEnd, buffer.toString());
				}
				boolean immutable = htmlTag == null ? false : htmlTag.isImmutableHtmlTag();
				if (newLines == 0) {
					newLines = printJavadocBlockNodesNewLines(block, node, previousEnd);
				}
				int nodeStart = node.sourceStart;
				if (newLines > 0 || (idx > 1 && nodeStart > (previousEnd+1))) {
					printJavadocGapLines(previousEnd+1, nodeStart-1, newLines, clearBlankLines, false, null);
				}
				if (newLines > 0) textOnNewLine = true;
				buffer = new StringBuffer();
				if (node.isText()) {
					if (immutable) {
						// do not change immutable tags, just increment column
						if (textOnNewLine && this.commentIndentation != null) {
					    	addInsertEdit(node.sourceStart, this.commentIndentation);
					    	this.column += this.commentIndentation.length();
						}
						printJavadocHtmlImmutableTag(htmlTag, block, textOnNewLine);
						this.column += getTextLength(block, htmlTag);
						linesAfter = 0;
					} else {
						linesAfter = printJavadocHtmlTag(htmlTag, block, textOnNewLine);
					}
					nodeKind = 1; // text
				} else {
					if (textOnNewLine && this.commentIndentation != null) {
				    	addInsertEdit(node.sourceStart, this.commentIndentation);
				    	this.column += this.commentIndentation.length();
					}
					printJavadocBlock((FormatJavadocBlock)node);
					linesAfter = 0;
					nodeKind = 2; // block
				}
				textStart = node.sourceEnd+1;
				ptr++;
				if (idx > max)  {
					return linesAfter;
				}
			} else {
				if (idx > 0 && linesAfter > 0) {
					printJavadocGapLines(previousEnd+1, nextStart, linesAfter, clearBlankLines, false, buffer);
					textOnNewLine = true;
				}
				boolean needIndentation = textOnNewLine;
				if (idx > 0) {
					if (!needIndentation && text.isTextAfterHtmlSeparatorTag(idx-1)) {
						needIndentation = true;
					}
				}
				this.needSpace = idx > 1 && (previousEnd+1) < nextStart; // There's no space between text and html tag or inline block => do not insert space a the beginning of the text
				printJavadocTextLine(buffer, nextStart, end, block, idx==0, needIndentation, idx==0/* opening html tag?*/ || text.htmlIndexes[idx-1] != -1);
				linesAfter = 0;
			    if (idx==0) {
			    	if (isHtmlSeparatorTag) {
				    	linesAfter = 1;
				    }
				} else if (text.htmlIndexes[idx-1] == JAVADOC_SINGLE_BREAK_TAG_ID) {
			    	linesAfter = 1;
			    }
			}

			// Replace with current buffer if there are several empty lines between text lines
			nextStart = (int) text.separators[idx];
			int endLine = Util.getLineNumber(end, this.lineEnds, startLine-1, this.maxLines);
			startLine = Util.getLineNumber(nextStart, this.lineEnds, endLine-1, this.maxLines);
			int linesGap = startLine - endLine;
			if (linesGap > 0) {
				if (clearBlankLines) {
					// keep previously computed lines after
				} else {
					if (idx==0 || linesGap > 1 || (idx < max && nodeKind==1 && (text.htmlIndexes[idx-1] & JAVADOC_TAGS_ID_MASK) != JAVADOC_IMMUTABLE_TAGS_ID)) {
						if (linesAfter < linesGap) {
							linesAfter = linesGap;
						}
					}
				}
			}
			textOnNewLine = linesAfter > 0;

			// print <pre> tag
			if (isCode) {
    			int codeEnd = (int) (text.separators[max] >>> 32);
    			if (codeEnd > end) {
    				if (this.formatter.preferences.comment_format_source) {
						if (textStart < end) addReplaceEdit(textStart, end, buffer.toString());
						// Count the lines until the exact start position of the code
						this.scanner.resetTo(end+1, nextStart-1);
						int newLines = 0;
						try {
							int token = this.scanner.getNextToken();
							loop: while (true) {
								switch (token) {
									case TerminalTokens.TokenNameWHITESPACE:
										if (CharOperation.indexOf('\n', this.scanner.source, this.scanner.startPosition, this.scanner.currentPosition) < 0) {
											break loop;
										}
										newLines++;
										break;
									case TerminalTokens.TokenNameMULTIPLY:
										nextStart = this.scanner.currentPosition + 1;
										break;
									default:
										break loop;
								}
								token = this.scanner.getNextToken();
							}
						}
						catch (InvalidInputException iie) {
							// skip
						}
						if (newLines == 0) newLines=1;
		    			printJavadocGapLines(end+1, nextStart-1, newLines, false/* clear first blank lines inside <pre> tag as done by old formatter */, false, null);
						printCodeSnippet(nextStart, codeEnd);
						nextStart = (int) text.separators[max];
	    				printJavadocGapLines(codeEnd+1, nextStart-1, 1, false/* clear blank lines inside <pre> tag as done by old formatter */, false, null);
	    				return 2;
    				}
    			} else {
					nextStart = (int) text.separators[max];
					if ((nextStart-1) > (end+1)) {
						int line1 = Util.getLineNumber(end+1, this.lineEnds, startLine-1, this.maxLines);
						int line2 = Util.getLineNumber(nextStart-1, this.lineEnds, line1-1, this.maxLines);
	    				int gapLines = line2-line1-1;
						printJavadocGapLines(end+1, nextStart-1, gapLines, false/* never clear blank lines inside <pre> tag*/, false, null);
						if (gapLines > 0) textOnNewLine = true;
					}
    			}
				return 1;
			}

			// store previous end
			previousEnd = end;
		}

		// Insert last gap
	    boolean closingTag = isHtmlBreakTag || (text.htmlIndexes != null && (text.htmlIndexes[max] & JAVADOC_TAGS_ID_MASK) == htmlTagID);
		if (previousEnd != -1) {
		    if (max > 0 && isHtmlSeparatorTag && closingTag) {
				if (linesAfter == 0) linesAfter = 1;
			}
			if (linesAfter > 0) {
				printJavadocGapLines(previousEnd+1, nextStart-1, linesAfter, clearBlankLines, false, buffer);
				textOnNewLine = linesAfter > 0;
			}
		}

	    // Print closing tag
		boolean needIndentation = textOnNewLine;
		if (!needIndentation && !isHtmlBreakTag && text.htmlIndexes != null && text.isTextAfterHtmlSeparatorTag(max)) {
			needIndentation = true;
		}
		this.needSpace = !closingTag && max > 0 // not a single or not closed tag (e.g. <br>)
			&& (previousEnd+1) < nextStart; // There's no space between text and html tag or inline block => do not insert space a the beginning of the text
		printJavadocTextLine(buffer, nextStart, text.sourceEnd, block, max <= 0, needIndentation, closingTag/* closing html tag*/);
		if (textStart < text.sourceEnd) {
			addReplaceEdit(textStart, text.sourceEnd, buffer.toString());
		}

		// Reset
		this.needSpace = false;
		this.scanner.resetTo(text.sourceEnd+1, this.scannerEndPosition - 1);

		// Return the new lines to insert after
	    if (max > 0 && isHtmlSeparatorTag) {
			return 1;
		}
	    return 0;
    }

	private void printJavadocNewLine(StringBuffer buffer) {
	    buffer.append(this.lineSeparator);
	    this.column = 1;
	    printIndentationIfNecessary(buffer);
	    buffer.append(BLOCK_LINE_PREFIX);
	    this.column += BLOCK_LINE_PREFIX_LENGTH;
	    this.line++;
	    this.lastNumberOfNewLines++;
    }

	private void printJavadocText(FormatJavadocText text, FormatJavadocBlock block, boolean textOnNewLine) {

		boolean clearBlankLines = this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment;
		boolean joinLines = this.formatter.preferences.join_lines_in_comments;
		StringBuffer buffer = new StringBuffer();
		int textStart = text.sourceStart;
		int nextStart = textStart;
		int startLine = Util.getLineNumber(textStart, this.lineEnds, 0, this.maxLines);

		// Iterate on text line separators
		for (int idx=0, max=text.separatorsPtr; idx<=max ; idx++) {

			// append text to buffer realigning with the line length
			int end = (int) (text.separators[idx] >>> 32);
			boolean needIndentation = textOnNewLine;
			if (idx > 0) {
				if (!needIndentation && text.isTextAfterHtmlSeparatorTag(idx-1)) {
					needIndentation = true;
				}
			}
			this.needSpace = idx > 0;
			printJavadocTextLine(buffer, nextStart, end, block, idx==0 /*first text?*/, needIndentation, false /*not an html tag*/);
			textOnNewLine = false;

			// Replace with current buffer if there are several empty lines between text lines
			nextStart = (int) text.separators[idx];
			if (!clearBlankLines || !joinLines) {
				int endLine = Util.getLineNumber(end, this.lineEnds, startLine-1, this.maxLines);
				startLine = Util.getLineNumber(nextStart, this.lineEnds, endLine-1, this.maxLines);
				int gapLine = endLine;
				if (joinLines) gapLine++; // if not preserving line break then gap must be at least of one line
				if (startLine > gapLine) {
					addReplaceEdit(textStart, end, buffer.toString());
					textStart = nextStart;
					buffer.setLength(0);
					int newLines = startLine - endLine;
					if (clearBlankLines) newLines = 1;
					printJavadocGapLines(end+1, nextStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
					textOnNewLine = true;
				}
			}
		}

		// Replace remaining line
		boolean needIndentation = textOnNewLine;
		this.needSpace = text.separatorsPtr >= 0;
		printJavadocTextLine(buffer, nextStart, text.sourceEnd, block, text.separatorsPtr==-1 /* first text?*/, needIndentation, false /*not an html tag*/);
		// TODO Bring back following optimization
		// if (lastNewLines != this.lastNumberOfNewLines || (this.column - currentColumn) != (text.sourceEnd - text.sourceStart + 1)) {
			addReplaceEdit(textStart, text.sourceEnd, buffer.toString());
		// }

		// Reset
		this.needSpace = false;
		this.scanner.resetTo(text.sourceEnd+1, this.scannerEndPosition - 1);
	}

	/*
	 * Returns whether the text has been modified or not.
	 */
	private void printJavadocTextLine(StringBuffer buffer, int textStart, int textEnd, FormatJavadocBlock block, boolean firstText, boolean needIndentation, boolean isHtmlTag) {

		boolean headerLine = block.isHeaderLine() && this.lastNumberOfNewLines == 0;

		// First we need to know what is the indentation
		StringBuffer tokensBuffer = new StringBuffer();
		int firstColumn = 1 + this.indentationLevel + BLOCK_LINE_PREFIX_LENGTH;
		int maxColumn = this.formatter.preferences.comment_line_length + 1;
		if (headerLine) {
			firstColumn++;
			maxColumn++;
		}
		if (needIndentation && this.commentIndentation != null) {
			buffer.append(this.commentIndentation);
	    	this.column += this.commentIndentation.length();
		}
		if (this.column < firstColumn) {
			this.column = firstColumn;
		}

		// Scan the text token per token to compact it and size it the max line length
		String newLineString = null;
		try {
			this.scanner.resetTo(textStart, textEnd);
			this.scanner.skipComments = true;
			int previousToken = -1;
			boolean textOnNewLine = needIndentation;

			// Consume text token per token
    		while (!this.scanner.atEnd()) {
				int token;
				try {
					token = this.scanner.getNextToken();
				} catch (InvalidInputException iie) {
					token = consumeInvalidToken(textEnd);
				}
	    		int tokensBufferLength = tokensBuffer.length();
    			int tokenStart = this.scanner.getCurrentTokenStartPosition();
	    		int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - tokenStart;
				boolean insertSpace = (previousToken == TerminalTokens.TokenNameWHITESPACE || this.needSpace) && !textOnNewLine;
				String tokensBufferString = tokensBuffer.toString().trim();
				switch (token) {
					case TerminalTokens.TokenNameWHITESPACE:
						if (tokensBufferLength > 0) {
							boolean shouldSplit = (this.column+tokensBufferLength) > maxColumn // the max length is reached
								&& !isHtmlTag
								&& (insertSpace || tokensBufferLength > 1) // allow to split at the beginning only when starting with an identifier or a token with a length > 1
								&& tokensBufferString.charAt(0) != '@'; // avoid to split just before a '@'
							if (shouldSplit) {
								this.lastNumberOfNewLines++;
								this.line++;
								if (newLineString == null) {
									StringBuffer newLineBuffer = new StringBuffer(this.lineSeparator);
							    	this.column = 1;
							    	printIndentationIfNecessary(newLineBuffer);
						    		newLineBuffer.append(BLOCK_LINE_PREFIX);
							    	this.column += BLOCK_LINE_PREFIX_LENGTH;
									if (this.commentIndentation != null) {
										newLineBuffer.append(this.commentIndentation);
								    	this.column += this.commentIndentation.length();
									}
						    		firstColumn = this.column;
						    		newLineString = newLineBuffer.toString();
								} else {
									this.column = firstColumn;
								}
								buffer.append(newLineString);
								buffer.append(tokensBufferString);
								this.column += tokensBufferString.length();
								if (headerLine) {
									firstColumn--;
									maxColumn--;
									headerLine = false;
								}
							} else {
								buffer.append(tokensBuffer);
								this.column += tokensBufferLength;
							}
							tokensBuffer.setLength(0);
						}
						textOnNewLine = false;
						previousToken = token;
						continue;
					case TerminalTokens.TokenNameCharacterLiteral:
						if (this.scanner.currentPosition > this.scanner.eofPosition) {
							this.scanner.resetTo(this.scanner.startPosition, textEnd);
							this.scanner.getNextChar();
							token = 1;
						}
						break;
				}
	    		int lastColumn = this.column + tokensBufferLength + tokenLength;
	    		if (insertSpace) lastColumn++;
				boolean shouldSplit = lastColumn > maxColumn // the max length is reached
					&& (!isHtmlTag || previousToken == -1) // not an html tag or just at the beginning of it
					&& token != TerminalTokens.TokenNameAT && (tokensBufferLength == 0 || tokensBuffer.charAt(tokensBufferLength-1) != '@'); // avoid to split just before a '@'
				if (shouldSplit) {
					// not enough space on the line
					if ((tokensBufferLength > 0 || tokenLength < maxColumn) && !isHtmlTag && tokensBufferLength > 0 && (firstColumn+tokensBufferLength+tokenLength) >= maxColumn) {
						// there won't be enough room even if we break the line before the buffered tokens
						// So add the buffered tokens now
						buffer.append(tokensBuffer);
						this.column += tokensBufferLength;
						tokensBuffer.setLength(0);
						tokensBufferLength = 0;
						textOnNewLine = false;
					}
					if ((tokensBufferLength > 0 || tokenLength < maxColumn) && (!textOnNewLine || !firstText)) {
						this.lastNumberOfNewLines++;
						this.line++;
						if (newLineString == null) {
							StringBuffer newLineBuffer = new StringBuffer(this.lineSeparator);
					    	this.column = 1;
					    	printIndentationIfNecessary(newLineBuffer);
				    		newLineBuffer.append(BLOCK_LINE_PREFIX);
					    	this.column += BLOCK_LINE_PREFIX_LENGTH;
							if (this.commentIndentation != null) {
								newLineBuffer.append(this.commentIndentation);
						    	this.column += this.commentIndentation.length();
							}
				    		firstColumn = this.column;
				    		newLineString = newLineBuffer.toString();
						} else {
							this.column = firstColumn;
						}
						buffer.append(newLineString);
					}
			    	if (tokensBufferLength > 0) {
			    		String tokensString = tokensBufferString;
						buffer.append(tokensString);
						this.column += tokensString.length();
						tokensBuffer.setLength(0);
						tokensBufferLength = 0;
			    	}
					buffer.append(this.scanner.source, tokenStart, tokenLength);
					this.column += tokenLength;
					textOnNewLine = false;
					if (headerLine) {
						firstColumn--;
						maxColumn--;
						headerLine = false;
					}
    			} else {
					// append token to the line
		    		if (insertSpace) {
		    			tokensBuffer.append(' ');
		    		}
					tokensBuffer.append(this.scanner.source, tokenStart, tokenLength);
    			}
				previousToken = token;
    			this.needSpace = false;
    			if (headerLine && lastColumn == maxColumn && this.scanner.atEnd()) {
					this.lastNumberOfNewLines++;
					this.line++;
    			}
    		}
		}
		finally {
			this.scanner.skipComments = false;
			// Add remaining buffered tokens
			if (tokensBuffer.length() > 0) {
				buffer.append(tokensBuffer);
				this.column += tokensBuffer.length();
			}
		}
    }

	public void printModifiers(Annotation[] annotations, ASTVisitor visitor) {
		printModifiers(annotations, visitor, ICodeFormatterConstants.ANNOTATION_UNSPECIFIED);
	}

	public void printModifiers(Annotation[] annotations, ASTVisitor visitor, int annotationSourceKind) {
		try {
			int annotationsLength = annotations != null ? annotations.length : 0;
			int annotationsIndex = 0;
			boolean isFirstModifier = true;
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasComment = false;
			boolean hasModifiers = false;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(this.currentToken) {
					case TerminalTokens.TokenNamepublic :
					case TerminalTokens.TokenNameprotected :
					case TerminalTokens.TokenNameprivate :
					case TerminalTokens.TokenNamestatic :
					case TerminalTokens.TokenNameabstract :
					case TerminalTokens.TokenNamefinal :
					case TerminalTokens.TokenNamenative :
					case TerminalTokens.TokenNamesynchronized :
					case TerminalTokens.TokenNametransient :
					case TerminalTokens.TokenNamevolatile :
					case TerminalTokens.TokenNamestrictfp :
						hasModifiers = true;
						print(this.scanner.currentPosition - this.scanner.startPosition, !isFirstModifier);
						isFirstModifier = false;
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameAT :
						hasModifiers = true;
						if (!isFirstModifier) {
							space();
						}
						this.scanner.resetTo(this.scanner.getCurrentTokenStartPosition(), this.scannerEndPosition - 1);
						if (annotationsIndex < annotationsLength) {
							annotations[annotationsIndex++].traverse(visitor, (BlockScope) null);
							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122247
							boolean shouldAddNewLine = false;
							switch (annotationSourceKind) {
								case ICodeFormatterConstants.ANNOTATION_ON_MEMBER :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_member) {
										shouldAddNewLine = true;
									}
									break;
								case ICodeFormatterConstants.ANNOTATION_ON_PARAMETER :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_parameter) {
										shouldAddNewLine = true;
									}
									break;
								case ICodeFormatterConstants.ANNOTATION_ON_LOCAL_VARIABLE :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_local_variable) {
										shouldAddNewLine = true;
									}
									break;
								default:
									// do nothing when no annotation formatting option specified
							}
							if (shouldAddNewLine) {
								this.printNewLine();
							}
						} else {
							return;
						}
						isFirstModifier = false;
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						printBlockComment(true);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameWHITESPACE :
						addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						int count = 0;
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (count >= 1 && hasComment) {
							printNewLine();
						}
						currentTokenStartPosition = this.scanner.currentPosition;
						hasComment = false;
						break;
					default:
						if (hasModifiers) {
							space();
						}
						// step back one token
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printNewLine() {
		this.printNewLine(this.scanner.getCurrentTokenEndPosition() + 1);
	}

	public void printNewLine(int insertPosition) {
		if (this.nlsTagCounter > 0) {
			return;
		}
		if (this.lastNumberOfNewLines >= 1) {
			this.column = 1; // ensure that the scribe is at the beginning of a new line
			return;
		}
		addInsertEdit(insertPosition, this.lineSeparator);
		this.line++;
		this.lastNumberOfNewLines = 1;
		this.column = 1;
		this.needSpace = false;
		this.pendingSpace = false;
	}

	public void printNextToken(int expectedTokenType){
		printNextToken(expectedTokenType, false);
	}

	public void printNextToken(int expectedTokenType, boolean considerSpaceIfAny){
		printComment(CodeFormatter.K_UNKNOWN);
		try {
			this.currentToken = this.scanner.getNextToken();
			if (expectedTokenType != this.currentToken) {
				throw new AbortFormatting("unexpected token type, expecting:"+expectedTokenType+", actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
			}
			print(this.scanner.currentPosition - this.scanner.startPosition, considerSpaceIfAny);
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printNextToken(int[] expectedTokenTypes) {
		printNextToken(expectedTokenTypes, false);
	}

	public void printNextToken(int[] expectedTokenTypes, boolean considerSpaceIfAny){
		printComment(CodeFormatter.K_UNKNOWN);
		try {
			this.currentToken = this.scanner.getNextToken();
			if (Arrays.binarySearch(expectedTokenTypes, this.currentToken) < 0) {
				StringBuffer expectations = new StringBuffer(5);
				for (int i = 0; i < expectedTokenTypes.length; i++){
					if (i > 0) {
						expectations.append(',');
					}
					expectations.append(expectedTokenTypes[i]);
				}
				throw new AbortFormatting("unexpected token type, expecting:["+expectations.toString()+"], actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
			}
			print(this.scanner.currentPosition - this.scanner.startPosition, considerSpaceIfAny);
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printArrayQualifiedReference(int numberOfTokens, int sourceEnd) {
		int currentTokenStartPosition = this.scanner.currentPosition;
		int numberOfIdentifiers = 0;
		try {
			do {
				printComment(CodeFormatter.K_UNKNOWN);
				switch(this.currentToken = this.scanner.getNextToken()) {
					case TerminalTokens.TokenNameEOF :
						return;
					case TerminalTokens.TokenNameWHITESPACE :
						addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameIdentifier :
						print(this.scanner.currentPosition - this.scanner.startPosition, false);
						currentTokenStartPosition = this.scanner.currentPosition;
						if (++ numberOfIdentifiers == numberOfTokens) {
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						}
						break;
					case TerminalTokens.TokenNameDOT :
						print(this.scanner.currentPosition - this.scanner.startPosition, false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameRPAREN:
						currentTokenStartPosition = this.scanner.startPosition;
						// $FALL-THROUGH$ - fall through default case...
					default:
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			} while (this.scanner.currentPosition <= sourceEnd);
		} catch(InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printQualifiedReference(int sourceEnd, boolean expectParenthesis) {
		int currentTokenStartPosition = this.scanner.currentPosition;
		try {
			do {
				printComment(CodeFormatter.K_UNKNOWN);
				switch(this.currentToken = this.scanner.getNextToken()) {
					case TerminalTokens.TokenNameEOF :
						return;
					case TerminalTokens.TokenNameWHITESPACE :
						addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameIdentifier :
					case TerminalTokens.TokenNameDOT :
						print(this.scanner.currentPosition - this.scanner.startPosition, false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameRPAREN:
						if (expectParenthesis) {
							currentTokenStartPosition = this.scanner.startPosition;
						}
						// $FALL-THROUGH$ - fall through default case...
					default:
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			} while (this.scanner.currentPosition <= sourceEnd);
		} catch(InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	private void printRule(StringBuffer stringBuffer) {
		for (int i = 0; i < this.pageWidth; i++){
			if ((i % this.tabLength) == 0) {
				stringBuffer.append('+');
			} else {
				stringBuffer.append('-');
			}
		}
		stringBuffer.append(this.lineSeparator);

		for (int i = 0; i < (this.pageWidth / this.tabLength); i++) {
			stringBuffer.append(i);
			stringBuffer.append('\t');
		}
	}

	public void printTrailingComment(int numberOfNewLinesToInsert) {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasWhitespaces = false;
			boolean hasLineComment = false;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						int count = 0;
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (hasLineComment) {
							if (count >= 1) {
								currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
								preserveEmptyLines(numberOfNewLinesToInsert, currentTokenStartPosition);
								addDeleteEdit(currentTokenStartPosition, this.scanner.getCurrentTokenEndPosition());
								this.scanner.resetTo(this.scanner.currentPosition, this.scannerEndPosition - 1);
								return;
							}
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else if (count > 1) {
							this.printEmptyLines(numberOfNewLinesToInsert, this.scanner.getCurrentTokenStartPosition());
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else {
							hasWhitespaces = true;
							currentTokenStartPosition = this.scanner.currentPosition;
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (hasWhitespaces) {
							space();
						}
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (hasWhitespaces) {
							space();
						}
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					default :
						// step back one token
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}
	public void printTrailingComment() {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasWhitespaces = false;
			boolean hasComment = false;
			boolean hasLineComment = false;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						int count = 0;
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (hasLineComment) {
							if (count >= 1) {
								currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
								preserveEmptyLines(count, currentTokenStartPosition);
								addDeleteEdit(currentTokenStartPosition, this.scanner.getCurrentTokenEndPosition());
								this.scanner.resetTo(this.scanner.currentPosition, this.scannerEndPosition - 1);
								return;
							}
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else if (count >= 1) {
							if (hasComment) {
								this.printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else {
							hasWhitespaces = true;
							currentTokenStartPosition = this.scanner.currentPosition;
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (hasWhitespaces) {
							space();
						}
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (hasWhitespaces) {
							space();
						}
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasComment = true;
						break;
					default :
						// step back one token
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	void redoAlignment(AlignmentException e){
		if (e.relativeDepth > 0) { // if exception targets a distinct context
			e.relativeDepth--; // record fact that current context got traversed
			this.currentAlignment = this.currentAlignment.enclosing; // pop currentLocation
			throw e; // rethrow
		}
		// reset scribe/scanner to restart at this given location
		resetAt(this.currentAlignment.location);
		this.scanner.resetTo(this.currentAlignment.location.inputOffset, this.scanner.eofPosition);
		// clean alignment chunkKind so it will think it is a new chunk again
		this.currentAlignment.chunkKind = 0;
	}

	void redoMemberAlignment(AlignmentException e){
		// reset scribe/scanner to restart at this given location
		resetAt(this.memberAlignment.location);
		this.scanner.resetTo(this.memberAlignment.location.inputOffset, this.scanner.eofPosition);
		// clean alignment chunkKind so it will think it is a new chunk again
		this.memberAlignment.chunkKind = 0;
	}

	public void reset() {
		this.checkLineWrapping = true;
		this.line = 0;
		this.column = 1;
		this.editsIndex = 0;
		this.nlsTagCounter = 0;
	}

	private void resetAt(Location location) {
		this.line = location.outputLine;
		this.column = location.outputColumn;
		this.indentationLevel = location.outputIndentationLevel;
		this.numberOfIndentations = location.numberOfIndentations;
		this.lastNumberOfNewLines = location.lastNumberOfNewLines;
		this.needSpace = location.needSpace;
		this.pendingSpace = location.pendingSpace;
		this.editsIndex = location.editsIndex;
		this.nlsTagCounter = location.nlsTagCounter;
		if (this.editsIndex > 0) {
			this.edits[this.editsIndex - 1] = location.textEdit;
		}
		this.formatter.lastLocalDeclarationSourceStart = location.lastLocalDeclarationSourceStart;
	}

	private void resize() {
		System.arraycopy(this.edits, 0, (this.edits = new OptimizedReplaceEdit[this.editsIndex * 2]), 0, this.editsIndex);
	}

	void setIncludeComments(boolean on) {
		if (on) {
			this.formatComments |= CodeFormatter.F_INCLUDE_COMMENTS;
		} else {
			this.formatComments &= ~CodeFormatter.F_INCLUDE_COMMENTS;
		}
	}

	void setHeaderComment(int position) {
		this.headerEndPosition = position;
	}

	public void space() {
		if (!this.needSpace) return;
		this.lastNumberOfNewLines = 0;
		this.pendingSpace = true;
		this.column++;
		this.needSpace = false;
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer
			.append("(page width = " + this.pageWidth + ") - (tabChar = ");//$NON-NLS-1$//$NON-NLS-2$
		switch(this.tabChar) {
			case DefaultCodeFormatterOptions.TAB :
				 stringBuffer.append("TAB");//$NON-NLS-1$
				 break;
			case DefaultCodeFormatterOptions.SPACE :
				 stringBuffer.append("SPACE");//$NON-NLS-1$
				 break;
			default :
				 stringBuffer.append("MIXED");//$NON-NLS-1$
		}
		stringBuffer
			.append(") - (tabSize = " + this.tabLength + ")")//$NON-NLS-1$//$NON-NLS-2$
			.append(this.lineSeparator)
			.append("(line = " + this.line + ") - (column = " + this.column + ") - (identationLevel = " + this.indentationLevel + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
			.append(this.lineSeparator)
			.append("(needSpace = " + this.needSpace + ") - (lastNumberOfNewLines = " + this.lastNumberOfNewLines + ") - (checkLineWrapping = " + this.checkLineWrapping + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
			.append(this.lineSeparator)
			.append("==================================================================================")	//$NON-NLS-1$
			.append(this.lineSeparator);
		printRule(stringBuffer);
		return stringBuffer.toString();
	}

	public void unIndent() {
		this.indentationLevel -= this.indentationSize;
		this.numberOfIndentations--;
	}
}
