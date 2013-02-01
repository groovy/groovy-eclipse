/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ray V. (voidstar@gmail.com) - Contribution for bug 282988
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
	private boolean preserveLineBreakIndentation = false;
	public int line;

	private int[] lineEnds;
	private int maxLines;
	public Alignment memberAlignment;
	public boolean needSpace = false;

	// Line separator infos
	final private String lineSeparator;
	final private String lineSeparatorAndSpace;
	final private char firstLS;
	final private int lsLength;

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

	/** empty lines*/
	private final boolean indentEmptyLines;
	int blank_lines_between_import_groups = -1;

	// Preserve empty lines constants
	public static final int DO_NOT_PRESERVE_EMPTY_LINES = -1;
	public static final int PRESERVE_EMPTY_LINES_KEEP_LAST_NEW_LINES_INDENTATION = 1;
	public static final int PRESERVE_EMPTY_LINES_IN_FORMAT_LEFT_CURLY_BRACE = 2;
	public static final int PRESERVE_EMPTY_LINES_IN_STRING_LITERAL_CONCATENATION = 3;
	public static final int PRESERVE_EMPTY_LINES_IN_CLOSING_ARRAY_INITIALIZER = 4;
	public static final int PRESERVE_EMPTY_LINES_IN_FORMAT_OPENING_BRACE = 5;
	public static final int PRESERVE_EMPTY_LINES_IN_BINARY_EXPRESSION = 6;
	public static final int PRESERVE_EMPTY_LINES_IN_EQUALITY_EXPRESSION = 7;
	public static final int PRESERVE_EMPTY_LINES_BEFORE_ELSE = 8;
	public static final int PRESERVE_EMPTY_LINES_IN_SWITCH_CASE = 9;
	public static final int PRESERVE_EMPTY_LINES_AT_END_OF_METHOD_DECLARATION = 10;
	public static final int PRESERVE_EMPTY_LINES_AT_END_OF_BLOCK = 11;
	final static int PRESERVE_EMPTY_LINES_DO_NOT_USE_ANY_INDENTATION = -1;
	final static int PRESERVE_EMPTY_LINES_USE_CURRENT_INDENTATION = 0;
	final static int PRESERVE_EMPTY_LINES_USE_TEMPORARY_INDENTATION = 1;

	/** disabling */
	boolean editsEnabled;
	boolean useTags;
	int tagsKind;

	/* Comments formatting */
	private static final int INCLUDE_BLOCK_COMMENTS = CodeFormatter.F_INCLUDE_COMMENTS | CodeFormatter.K_MULTI_LINE_COMMENT;
	private static final int INCLUDE_JAVA_DOC = CodeFormatter.F_INCLUDE_COMMENTS | CodeFormatter.K_JAVA_DOC;
	private static final int INCLUDE_LINE_COMMENTS = CodeFormatter.F_INCLUDE_COMMENTS | CodeFormatter.K_SINGLE_LINE_COMMENT;
	private static final int SKIP_FIRST_WHITESPACE_TOKEN = -2;
	private static final int INVALID_TOKEN = 2000;
	static final int NO_TRAILING_COMMENT = 0x0000;
	static final int BASIC_TRAILING_COMMENT = 0x0100;
	static final int COMPLEX_TRAILING_COMMENT = 0x0200;
	static final int IMPORT_TRAILING_COMMENT = COMPLEX_TRAILING_COMMENT | 0x0001;
	static final int UNMODIFIABLE_TRAILING_COMMENT = 0x0400;
	private int formatComments = 0;
	private int headerEndPosition = -1;
	String commentIndentation; // indentation requested in comments (usually in javadoc root tags description)

	// Class to store previous line comment information
	static class LineComment {
		boolean contiguous = false;
		int currentIndentation, indentation;
		int lines;
		char[] leadingSpaces;
	}
	final LineComment lastLineComment = new LineComment();

	// New way to format javadoc
	private FormatterCommentParser formatterCommentParser; // specialized parser to format comments

	// Disabling and enabling tags
	OptimizedReplaceEdit previousDisabledEdit;
	private char[] disablingTag, enablingTag;

	// Well know strings
	private String[] newEmptyLines = new String[10];
	private static String[] COMMENT_INDENTATIONS = new String[20];

	// final string buffers
	private final StringBuffer tempBuffer= new StringBuffer();
	private final StringBuffer blockCommentBuffer = new StringBuffer();
	private final StringBuffer blockCommentTokensBuffer = new StringBuffer();
	private final StringBuffer codeSnippetBuffer = new StringBuffer();
	private final StringBuffer javadocBlockRefBuffer= new StringBuffer();
	private final StringBuffer javadocGapLinesBuffer = new StringBuffer();
	private StringBuffer[] javadocHtmlTagBuffers = new StringBuffer[5];
	private final StringBuffer javadocTextBuffer = new StringBuffer();
	private final StringBuffer javadocTokensBuffer = new StringBuffer();

	Scribe(CodeFormatterVisitor formatter, long sourceLevel, IRegion[] regions, CodeSnippetParsingUtil codeSnippetParsingUtil, boolean includeComments) {
		initializeScanner(sourceLevel, formatter.preferences);
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
		this.lineSeparatorAndSpace = this.lineSeparator+' ';
		this.firstLS = this.lineSeparator.charAt(0);
		this.lsLength = this.lineSeparator.length();
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
					// adapt only javadoc or block comments. Since fix for bug
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=238210
					// edits in line comments only concerns whitespaces hence can be
					// treated as edits in code
					adaptedLength = length + offset - adaptedOffset;
					commentIndex = index;
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

    	// Set invalid all edits outside the region
		if (currentEdit != -1) {
			int length = sortedEdits.length;
	    	for (int e=currentEdit; e<length; e++) {
	    		sortedEdits[e].offset = -1;
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
    	int initialStart = start==-1 ? 0 : start;
		int bottom = initialStart, top = sortedEdits.length - 1;
    	int topEnd = top;
    	int i = 0;
    	OptimizedReplaceEdit edit = null;
    	int overlapIndex = -1;

    	// Look for an edit overlapping the region start
    	while (bottom <= top) {
    		i = bottom + (top - bottom) /2;
    		edit = sortedEdits[i];
    		int editStart = edit.offset;
   			int editEnd = editStart + edit.length;
    		if (editStart > regionStart) {  // the edit starts after the region's start => no possible overlap of region's start
    			top = i-1;
    			if (editStart > regionEnd) { // the edit starts after the region's end => no possible overlap of region's end
    				topEnd = top;
    			}
    		} else {
    			if (editEnd < regionStart) { // the edit ends before the region's start => no possible overlap of region's start
	    			bottom = i+1;
				} else {
					// Count the lines of the edit which are outside the region
					int linesOutside = 0;
					StringBuffer spacesOutside = new StringBuffer();
					this.scanner.resetTo(editStart, editEnd-1);
					while (this.scanner.currentPosition < regionStart && !this.scanner.atEnd()) {
						char ch = (char) this.scanner.getNextChar();
						switch (ch) {
							case '\n':
								linesOutside++;
								spacesOutside.setLength(0);
								break;
							case '\r':
								break;
							default:
								spacesOutside.append(ch);
								break;
						}
					}

					// Restart the edit at the beginning of the line where the region start
					edit.offset = regionStart;
					int editLength = edit.length;
					edit.length -= edit.offset - editStart;

					// Cut replacement string if necessary
					int length = edit.replacement.length();
					if (length > 0) {

						// Count the lines in replacement string
						int linesReplaced = 0;
						for (int idx=0; idx < length; idx++) {
							if (edit.replacement.charAt(idx) == '\n') linesReplaced++;
						}

						// If the edit was a replacement but become an insertion due to the length reduction
						// and if the edit finishes just before the region starts and if there's no line to replace
						// then there's no replacement to do...
						if (editLength > 0 && edit.length == 0 && editEnd == regionStart && linesReplaced == 0 && linesOutside== 0) {
							edit.offset = -1;
						} else {

							// As the edit starts outside the region, remove first lines from edit string if any
							if (linesReplaced > 0) {
								int linesCount = linesOutside >= linesReplaced ? linesReplaced : linesOutside;
								if (linesCount > 0) {
									int idx = 0;
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
									// Compare spaces outside the region and the beginning
									// of the replacement string to remove the common part
									int spacesOutsideLength = spacesOutside.length();
									int replacementStart = idx;
									for (int o=0, r=0; o < spacesOutsideLength && r<(length-idx); o++) {
										char rch = edit.replacement.charAt(idx + r);
										char och = spacesOutside.charAt(o);
										if (rch == och) {
											replacementStart++;
											r++;
										} else if (rch == '\t' && (this.tabLength > 0 && och == ' ')) {
											if ((o+1)%this.tabLength == 0) {
												replacementStart++;
												r++;
											}
										} else {
											break;
										}
									}
									// Update the replacement string
									if (replacementStart > length || (replacementStart == length && spacesOutsideLength > 0)) {
										edit.offset = -1;
									} else if (spacesOutsideLength == 0 && replacementStart == length) {
										edit.replacement = ""; //$NON-NLS-1$
									} else {
										edit.replacement = edit.replacement.substring(replacementStart);
									}
								}
							}
						}
					}
					overlapIndex = i;
					break;
				}
			}
    	}
    	int validIndex = (overlapIndex != -1) ? overlapIndex : bottom;

    	// Look for an edit overlapping the region end
    	if (overlapIndex != -1) bottom = overlapIndex;
    	while (bottom <= topEnd) {
    		i = bottom + (topEnd - bottom) /2;
    		edit = sortedEdits[i];
    		int editStart = edit.offset;
   			int editEnd = editStart + edit.length;
   			if (regionEnd < editStart) {	// the edit starts after the region's end => no possible overlap of region's end
    			topEnd = i-1;
    		} else if (regionEnd == editStart) {	// special case when the edit starts just after the region's end...
    			// ...we got the last index of the edit inside the region
				topEnd = i - 1;
    			// this last edit is valid only if it's an insertion and if it has indentation
    			if (edit.length == 0) {
    				int nrLength = 0;
    				int rLength = edit.replacement.length();
    				if (nrLength < rLength) {
	    				int ch = edit.replacement.charAt(nrLength);
	    				loop: while (nrLength < rLength) {
		    				switch (ch) {
		    					case ' ':
		    					case '\t':
		    						nrLength++;
		    						break;
		    					default:
		    						break loop;
		    				}
	    				}
    				}
    				if (nrLength > 0) {
	    				topEnd++;
	    				if (nrLength < rLength) {
	    					edit.replacement = edit.replacement.substring(0, nrLength);
	    				}
    				}
    			}
    			break;
       		} else if (editEnd <= regionEnd) {	// the edit ends before the region's end => no possible overlap of region's end
    			bottom = i+1;
			} else {
				// Count the lines of the edit which are outside the region
				int linesOutside = 0;
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
							edit.replacement = getNewLineString(linesCount);
						}
					}
				}
				edit.length = regionEnd - editStart;

		    	// We got the last edit of the regions, give up
				topEnd = i;
				break;
			}
    	}

    	// Set invalid all edits outside the region
    	for (int e=initialStart; e<validIndex; e++) {
    		sortedEdits[e].offset = -1;
    	}
    	
    	// Return the index of next edit to look at
    	return topEnd+1;
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
		if (!this.editsEnabled) {
			if (this.previousDisabledEdit != null && this.previousDisabledEdit.offset == offset) {
				replacement = this.previousDisabledEdit.replacement;
			}
			this.previousDisabledEdit = null;
			if (replacement.indexOf(this.lineSeparator) >= 0) {
				if (length == 0 || printNewLinesCharacters(offset, length)) {
					this.previousDisabledEdit = new OptimizedReplaceEdit(offset, length, replacement);
				}
			}
			return;
		}
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

	public Alignment createAlignment(int kind, int mode, int count, int sourceRestart){
		return createAlignment(kind, mode, Alignment.R_INNERMOST, count, sourceRestart);
	}

	public Alignment createAlignment(int kind, int mode, int tieBreakRule, int count, int sourceRestart){
		return createAlignment(kind, mode, tieBreakRule, count, sourceRestart, this.formatter.preferences.continuation_indentation, false);
	}

	public Alignment createAlignment(int kind, int mode, int count, int sourceRestart, int continuationIndent, boolean adjust){
		return createAlignment(kind, mode, Alignment.R_INNERMOST, count, sourceRestart, continuationIndent, adjust);
	}

	public Alignment createAlignment(int kind, int mode, int tieBreakRule, int count, int sourceRestart, int continuationIndent, boolean adjust){
		Alignment alignment = new Alignment(kind, mode, tieBreakRule, this, count, sourceRestart, continuationIndent);
		// specific break indentation for message arguments inside binary expressions
		if ((this.currentAlignment == null && this.formatter.expressionsDepth >= 0) ||
			(this.currentAlignment != null && this.currentAlignment.kind == Alignment.BINARY_EXPRESSION &&
				(this.formatter.expressionsPos & CodeFormatterVisitor.EXPRESSIONS_POS_MASK) == CodeFormatterVisitor.EXPRESSIONS_POS_BETWEEN_TWO)) {
			switch (kind) {
				case Alignment.CONDITIONAL_EXPRESSION:
				case Alignment.MESSAGE_ARGUMENTS:
				case Alignment.MESSAGE_SEND:
					if (this.formatter.lastBinaryExpressionAlignmentBreakIndentation == alignment.breakIndentationLevel) {
						alignment.breakIndentationLevel += this.indentationSize;
						alignment.shiftBreakIndentationLevel += this.indentationSize;
						this.formatter.lastBinaryExpressionAlignmentBreakIndentation = 0;
					}
					break;
			}
		}
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

	public Alignment createMemberAlignment(int kind, int mode, int count, int sourceRestart) {
		Alignment mAlignment = createAlignment(kind, mode, Alignment.R_INNERMOST, count, sourceRestart);
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
			if (this.currentAlignment == null) {
				this.formatter.lastBinaryExpressionAlignmentBreakIndentation = 0;
			}
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

	private int getCurrentCommentIndentation(int start) {
		int linePtr = -Arrays.binarySearch(this.lineEnds, start);
		int indentation = 0;
		int beginningOfLine = getLineEnd(linePtr - 1)+1;
		if (beginningOfLine == -1) {
			beginningOfLine = 0;
		}
		int currentStartPosition = start;
		char[] source = this.scanner.source;

		// find the position of the beginning of the line containing the comment
		while (beginningOfLine > currentStartPosition) {
			if (linePtr > 0) {
				beginningOfLine = getLineEnd(--linePtr)+1;
			} else {
				beginningOfLine = 0;
				break;
			}
		}
		for (int i=beginningOfLine; i < currentStartPosition ; i++) {
			char currentCharacter = source[i];
			switch (currentCharacter) {
				case '\t' :
					if (this.tabLength != 0) {
						int reminder = indentation % this.tabLength;
						if (reminder == 0) {
							indentation += this.tabLength;
						} else {
							indentation = ((indentation / this.tabLength) + 1) * this.tabLength;
						}
					}
					break;
				case '\r' :
				case '\n' :
					indentation = 0;
					break;
				default:
					indentation++;
					break;
			}
		}
		return indentation;
	}

	int getCurrentIndentation(char[] whitespaces, int offset) {
		if (whitespaces == null) return offset;
		int length = whitespaces.length;
		if (this.tabLength == 0) return length;
		int indentation = offset;
		for (int i=0; i<length; i++) {
			char ch = whitespaces[i];
			switch (ch) {
				case '\t' :
					int reminder = indentation % this.tabLength;
					if (reminder == 0) {
						indentation += this.tabLength;
					} else {
						indentation = ((indentation / this.tabLength) + 1) * this.tabLength;
					}
					break;
				case '\r' :
				case '\n' :
					indentation = 0;
					break;
				default:
					indentation++;
					break;
			}
		}
		return indentation;
	}

	int getCurrentIndentation(int start) {
		int linePtr = Arrays.binarySearch(this.lineEnds, start);
		if (linePtr < 0) {
			linePtr = -linePtr - 1;
		}
		int indentation = 0;
		int beginningOfLine = getLineEnd(linePtr)+1;
		if (beginningOfLine == -1) {
			beginningOfLine = 0;
		}
		char[] source = this.scanner.source;

		for (int i=beginningOfLine; i<start; i++) {
			char currentCharacter = source[i];
			switch (currentCharacter) {
				case '\t' :
					if (this.tabLength != 0) {
						int reminder = indentation % this.tabLength;
						if (reminder == 0) {
							indentation += this.tabLength;
						} else {
							indentation = ((indentation / this.tabLength) + 1) * this.tabLength;
						}
					}
					break;
				case '\r' :
				case '\n' :
					indentation = 0;
					break;
				case ' ':
					indentation++;
					break;
				default:
					return indentation;
			}
		}
		return indentation;
	}

	public String getEmptyLines(int linesNumber) {
		if (this.nlsTagCounter > 0) {
			return Util.EMPTY_STRING;
		}
		String emptyLines;
		if (this.lastNumberOfNewLines == 0) {
			linesNumber++; // add an extra line breaks
			if (this.indentEmptyLines) {
				this.tempBuffer.setLength(0);
				for (int i = 0; i < linesNumber; i++) {
					printIndentationIfNecessary(this.tempBuffer);
					this.tempBuffer.append(this.lineSeparator);
					this.column = 1;
				}
				emptyLines = this.tempBuffer.toString();
			} else {
				emptyLines = getNewLineString(linesNumber);
			}
			this.lastNumberOfNewLines += linesNumber;
			this.line += linesNumber;
			this.column = 1;
			this.needSpace = false;
			this.pendingSpace = false;
		} else if (this.lastNumberOfNewLines == 1) {
			if (this.indentEmptyLines) {
				this.tempBuffer.setLength(0);
				for (int i = 0; i < linesNumber; i++) {
					printIndentationIfNecessary(this.tempBuffer);
					this.tempBuffer.append(this.lineSeparator);
					this.column = 1;
				}
				emptyLines = this.tempBuffer.toString();
			} else {
				emptyLines = getNewLineString(linesNumber);
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
			if (this.indentEmptyLines) {
				this.tempBuffer.setLength(0);
				for (int i = 0; i < realNewLineNumber; i++) {
					printIndentationIfNecessary(this.tempBuffer);
					this.tempBuffer.append(this.lineSeparator);
					this.column = 1;
				}
				emptyLines = this.tempBuffer.toString();
			} else {
				emptyLines = getNewLineString(realNewLineNumber);
			}
			this.lastNumberOfNewLines += realNewLineNumber;
			this.line += realNewLineNumber;
			this.column = 1;
			this.needSpace = false;
			this.pendingSpace = false;
		}
		return emptyLines;
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

	private String getNewLineString(int linesCount) {
		int length = this.newEmptyLines.length;
		if (linesCount > length) {
			System.arraycopy(this.newEmptyLines, 0, this.newEmptyLines = new String[linesCount+10], 0, length);
		}
		String newLineString = this.newEmptyLines[linesCount-1];
		if (newLineString == null) {
			this.tempBuffer.setLength(0);
			for (int j=0; j<linesCount; j++) {
				this.tempBuffer.append(this.lineSeparator);
			}
			newLineString = this.tempBuffer.toString();
			this.newEmptyLines[linesCount-1] = newLineString;
		}
		return newLineString;
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
			if (this.indentationSize == 0) {
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
	private String getPreserveEmptyLines(int count, int emptyLinesRules) {
		if (count == 0) {
			int currentIndentationLevel = this.indentationLevel;
			int useAlignmentBreakIndentation = useAlignmentBreakIndentation(emptyLinesRules);
			switch (useAlignmentBreakIndentation) {
				case PRESERVE_EMPTY_LINES_DO_NOT_USE_ANY_INDENTATION:
					return Util.EMPTY_STRING;
				default:
					// Return the new indented line
					StringBuffer buffer = new StringBuffer(getNewLine());
					printIndentationIfNecessary(buffer);
					if (useAlignmentBreakIndentation == PRESERVE_EMPTY_LINES_USE_TEMPORARY_INDENTATION) {
						this.indentationLevel = currentIndentationLevel;
					}
					return buffer.toString();
			}
		}
		if (this.blank_lines_between_import_groups >= 0) {
			useAlignmentBreakIndentation(emptyLinesRules);
			return getEmptyLines(this.blank_lines_between_import_groups);
		}
		if (this.formatter.preferences.number_of_empty_lines_to_preserve != 0) {
			useAlignmentBreakIndentation(emptyLinesRules);
			int linesToPreserve = Math.min(count, this.formatter.preferences.number_of_empty_lines_to_preserve);
			return getEmptyLines(linesToPreserve);
		}
		return getNewLine();
	}
	private int useAlignmentBreakIndentation(int emptyLinesRules) {
		// preserve line breaks in wrapping if specified
		// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=198074
		boolean specificEmptyLinesRule = emptyLinesRules != PRESERVE_EMPTY_LINES_KEEP_LAST_NEW_LINES_INDENTATION;
		if ((this.currentAlignment != null || specificEmptyLinesRule) && !this.formatter.preferences.join_wrapped_lines) {
			// insert a new line only if it has not been already done before
			// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=283476
			if (this.lastNumberOfNewLines == 0 || specificEmptyLinesRule || this.formatter.arrayInitializersDepth >= 0) {
				
				// Do not use alignment break indentation in specific circumstances
				boolean useAlignmentBreakIndentation;
				boolean useAlignmentShiftBreakIndentation = false;
				boolean useLastBinaryExpressionAlignmentBreakIndentation = false;
				switch (emptyLinesRules) {
					case DO_NOT_PRESERVE_EMPTY_LINES:
					case PRESERVE_EMPTY_LINES_IN_SWITCH_CASE:
					case PRESERVE_EMPTY_LINES_AT_END_OF_METHOD_DECLARATION:
					case PRESERVE_EMPTY_LINES_AT_END_OF_BLOCK:
						return PRESERVE_EMPTY_LINES_DO_NOT_USE_ANY_INDENTATION;
					case PRESERVE_EMPTY_LINES_IN_BINARY_EXPRESSION:
						useAlignmentBreakIndentation = true;
						if ((this.formatter.expressionsPos & CodeFormatterVisitor.EXPRESSIONS_POS_MASK) == CodeFormatterVisitor.EXPRESSIONS_POS_BETWEEN_TWO) {
							// we're just before the left expression, try to use the last
							// binary expression break indentation if any
							useLastBinaryExpressionAlignmentBreakIndentation = true;
						}
						break;
					case PRESERVE_EMPTY_LINES_IN_EQUALITY_EXPRESSION:
						useAlignmentShiftBreakIndentation = this.currentAlignment == null || this.currentAlignment.kind == Alignment.BINARY_EXPRESSION;
						useAlignmentBreakIndentation = !useAlignmentShiftBreakIndentation;
						break;
					case PRESERVE_EMPTY_LINES_IN_FORMAT_OPENING_BRACE:
						useAlignmentBreakIndentation = this.formatter.arrayInitializersDepth <= 1
							&& this.currentAlignment != null
							&& this.currentAlignment.kind == Alignment.ARRAY_INITIALIZER;
						break;
					case PRESERVE_EMPTY_LINES_IN_FORMAT_LEFT_CURLY_BRACE:
						useAlignmentBreakIndentation = false;
						break;
					default:
						if ((emptyLinesRules & 0xFFFF) == PRESERVE_EMPTY_LINES_IN_CLOSING_ARRAY_INITIALIZER && this.scanner.currentCharacter == '}' ) {
							// last array initializer closing brace
							this.indentationLevel = emptyLinesRules >> 16;
							this.preserveLineBreakIndentation = true;
							return PRESERVE_EMPTY_LINES_USE_CURRENT_INDENTATION;
						}
						useAlignmentBreakIndentation = true;
						break;
				}

				// If there's an alignment try to align on its break indentation level
				Alignment alignment = this.currentAlignment;
				if (alignment == null) {
					if (useLastBinaryExpressionAlignmentBreakIndentation) {
						if (this.indentationLevel < this.formatter.lastBinaryExpressionAlignmentBreakIndentation) {
							this.indentationLevel = this.formatter.lastBinaryExpressionAlignmentBreakIndentation;
						}
					}
					if (useAlignmentShiftBreakIndentation && this.memberAlignment != null) {
						if (this.indentationLevel < this.memberAlignment.shiftBreakIndentationLevel) {
							this.indentationLevel = this.memberAlignment.shiftBreakIndentationLevel;
						}
					}
				} else {
					// Use the member alignment break indentation level when
					// it's closer from the wrapped line than the current alignment
					if (this.memberAlignment != null && this.memberAlignment.location.inputOffset > alignment.location.inputOffset) {
						alignment = this.memberAlignment;
					}

					// Use the break indentation level if possible...
					if (useLastBinaryExpressionAlignmentBreakIndentation) {
						if (this.indentationLevel < this.formatter.lastBinaryExpressionAlignmentBreakIndentation) {
							this.indentationLevel = this.formatter.lastBinaryExpressionAlignmentBreakIndentation;
						}
					}
					if (useAlignmentBreakIndentation) {
						if (this.indentationLevel < alignment.breakIndentationLevel) {
							this.indentationLevel = alignment.breakIndentationLevel;
						}
					} else if (useAlignmentShiftBreakIndentation) {
						if (this.indentationLevel < alignment.shiftBreakIndentationLevel) {
							this.indentationLevel = alignment.shiftBreakIndentationLevel;
						}
					}
				}
				this.preserveLineBreakIndentation = true;
				if (useLastBinaryExpressionAlignmentBreakIndentation || useAlignmentShiftBreakIndentation) {
					return PRESERVE_EMPTY_LINES_USE_TEMPORARY_INDENTATION;
				}
				return PRESERVE_EMPTY_LINES_USE_CURRENT_INDENTATION;
			}
		}
		return PRESERVE_EMPTY_LINES_DO_NOT_USE_ANY_INDENTATION;
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
			if (currentEdit.offset >= 0 && currentEdit.offset <= this.scannerEndPosition) {
				if (currentEdit.length == 0 || (currentEdit.offset != this.scannerEndPosition && isMeaningfulEdit(currentEdit))) {
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
		}
		this.edits = null;
		return edit;
	}

	public void handleLineTooLong() {
		if (this.formatter.preferences.wrap_outer_expressions_when_nested) {
			handleLineTooLongSmartly();
			return;
		}
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

	private void handleLineTooLongSmartly() {
		// search for closest breakable alignment, using tiebreak rules
		// look for outermost breakable one
		int relativeDepth = 0, outerMostDepth = -1;
		Alignment targetAlignment = this.currentAlignment;
		int previousKind = -1;
		int insideMessage = 0;
		boolean insideStringConcat = false;
		while (targetAlignment != null){
			boolean couldBreak = targetAlignment.tieBreakRule == Alignment.R_OUTERMOST ||
				(!insideStringConcat &&
						insideMessage > 0 && targetAlignment.kind == Alignment.MESSAGE_ARGUMENTS &&
						(!targetAlignment.wasReset() || previousKind != Alignment.MESSAGE_SEND));
			if (couldBreak && targetAlignment.couldBreak()){
				outerMostDepth = relativeDepth;
			}
			switch (targetAlignment.kind) {
				case Alignment.MESSAGE_ARGUMENTS:
				case Alignment.MESSAGE_SEND:
					insideMessage++;
					break;
				case Alignment.STRING_CONCATENATION:
					insideStringConcat = true;
					break;
			}
			previousKind = targetAlignment.kind;
			targetAlignment = targetAlignment.enclosing;
			relativeDepth++;
		}
		if (outerMostDepth >= 0) {
			throw new AlignmentException(AlignmentException.LINE_TOO_LONG, outerMostDepth);
		}
		// look for innermost breakable one
		relativeDepth = 0;
		targetAlignment = this.currentAlignment;
		AlignmentException alignmentException = null;
		int msgArgsDepth = -1;
		while (targetAlignment != null) {
			if (targetAlignment.kind == Alignment.MESSAGE_ARGUMENTS) {
				msgArgsDepth = relativeDepth;
			}
			if (alignmentException == null) {
				if (targetAlignment.couldBreak()) {
					// do not throw the exception immediately to have a chance to reset
					// previously broken alignments (see bug 203588)
					alignmentException = new AlignmentException(AlignmentException.LINE_TOO_LONG, relativeDepth);
					if (insideStringConcat) throw alignmentException;
				}
			} else if (targetAlignment.wasSplit) {
				// reset the nearest already broken outermost alignment.
				// Note that it's not done twice to avoid infinite loop while raising
				// the exception on an innermost alignment...
				if (!targetAlignment.wasReset()) {
					targetAlignment.reset();
					if (msgArgsDepth > alignmentException.relativeDepth) {
						alignmentException.relativeDepth = msgArgsDepth;
					}
					throw alignmentException;
				}
			}
			targetAlignment = targetAlignment.enclosing;
			relativeDepth++;
		}
		if (alignmentException != null) {
			throw alignmentException;
		}
		// did not find any breakable location - proceed
		if (this.currentAlignment != null) {
			this.currentAlignment.blockAlign = false;
			this.currentAlignment.tooLong = true;
		}
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

	void setIndentation(int level, int n) {
		this.indentationLevel = level + n * this.indentationSize;
		this.numberOfIndentations = this.indentationLevel / this.indentationSize;
	}

	private void initializeScanner(long sourceLevel, DefaultCodeFormatterOptions preferences) {
		this.useTags = preferences.use_tags;
		this.tagsKind = 0;
		char[][] taskTags = null;
		if (this.useTags) {
			this.disablingTag = preferences.disabling_tag;
			this.enablingTag = preferences.enabling_tag;
			if (this.disablingTag == null) {
				if (this.enablingTag != null) {
					taskTags = new char[][] { this.enablingTag };
				}
			} else if (this.enablingTag == null) {
				taskTags = new char[][] { this.disablingTag };
			} else {
				taskTags = new char[][] { this.disablingTag, this.enablingTag };
			}
		}
		if (taskTags != null) {
			loop: for (int i=0,length=taskTags.length; i<length; i++) {
				if (taskTags[i].length > 2 && taskTags[i][0] == '/') {
					switch (taskTags[i][1]) {
						case '/':
							this.tagsKind = TerminalTokens.TokenNameCOMMENT_LINE;
							break loop;
						case '*':
							if (taskTags[i][2] != '*') {
								this.tagsKind = TerminalTokens.TokenNameCOMMENT_BLOCK;
								break loop;
							}
							break;
					}
				}
			}
		}
		this.scanner = new Scanner(true, true, false/*nls*/, sourceLevel/*sourceLevel*/, taskTags, null/*taskPriorities*/, true/*taskCaseSensitive*/);
		this.editsEnabled = true;
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

	private boolean isMeaningfulEdit(OptimizedReplaceEdit edit) {
		final int editLength= edit.length;
		final int editReplacementLength= edit.replacement.length();
		final int editOffset= edit.offset;
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

	private void preserveEmptyLines(int count, int insertPosition) {
		if (count > 0) {
			if (this.blank_lines_between_import_groups >= 0) {
				printEmptyLines(this.blank_lines_between_import_groups, insertPosition);
			} else if (this.formatter.preferences.number_of_empty_lines_to_preserve != 0) {
				int linesToPreserve = Math.min(count, this.formatter.preferences.number_of_empty_lines_to_preserve);
				printEmptyLines(linesToPreserve, insertPosition);
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
		int currentCommentIndentation = onFirstColumn ? 0 : getCurrentCommentIndentation(start);
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

							boolean addSpace = false;
							if (onFirstColumn) {
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
										addSpace = true;
									}
								}
							} else {
								if (ScannerHelper.isWhitespace((char) currentCharacter)) {
									int previousStartPosition = this.scanner.currentPosition;
									int currentIndentation = 0;
									loop: while(currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n' && ScannerHelper.isWhitespace((char) currentCharacter)) {
										if (currentIndentation >= currentCommentIndentation) {
											break loop;
										}
										previousStart = nextCharacterStart;
										previousStartPosition = this.scanner.currentPosition;
										switch(currentCharacter) {
											case '\t' :
												if (this.tabLength != 0) {
													int reminder = currentIndentation % this.tabLength;
													if (reminder == 0) {
														currentIndentation += this.tabLength;
													} else {
														currentIndentation = ((currentIndentation / this.tabLength) + 1) * this.tabLength;
													}
												}
												break;
											default :
												currentIndentation ++;
										}
										currentCharacter = this.scanner.getNextChar();
										nextCharacterStart = this.scanner.currentPosition;
									}
									if (currentCharacter == '\r' || currentCharacter == '\n') {
										nextCharacterStart = previousStartPosition;
									}
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
										addSpace = true;
									} else {
										previousStart = previousStartTemp;
										nextCharacterStart = nextCharacterStartTemp;
									}
									this.scanner.currentPosition = nextCharacterStart;
								}
							}
							String replacement;
							if (indentComment) {
								this.tempBuffer.setLength(0);
								this.tempBuffer.append(this.lineSeparator);
								if (this.indentationLevel > 0) {
									printIndentationIfNecessary(this.tempBuffer);
								}
								if (addSpace) {
									this.tempBuffer.append(' ');
								}
								replacement = this.tempBuffer.toString();
							} else {
								replacement = addSpace ? this.lineSeparatorAndSpace : this.lineSeparator;
							}
							addReplaceEdit(start, previousStart - 1, replacement);
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
		this.blockCommentBuffer.setLength(0);
		this.scanner.getNextChar();
		this.scanner.getNextChar();
		this.column += 2;
		this.scanner.skipComments = true;
		this.blockCommentTokensBuffer.setLength(0);
		int editStart = this.scanner.currentPosition;
		int editEnd = -1;

		// Consume text token per token
		int previousToken = -1;
		boolean newLine = false;
		boolean multiLines = false;
		boolean hasMultiLines = false;
		boolean hasTokens = false;
		boolean bufferHasTokens = false;
		boolean bufferHasNewLine = false;
		boolean lineHasTokens = false;
		int hasTextOnFirstLine = 0;
		boolean firstWord = true;
		boolean clearBlankLines = this.formatter.preferences.comment_clear_blank_lines_in_block_comment;
		boolean joinLines = this.formatter.preferences.join_lines_in_comments;
		boolean newLinesAtBoundaries = this.formatter.preferences.comment_new_lines_at_block_boundaries;
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
					if (this.blockCommentTokensBuffer.length() > 0) {
						if (hasTextOnFirstLine == 1 && multiLines) {
							printBlockCommentHeaderLine(this.blockCommentBuffer);
							hasTextOnFirstLine = -1;
						}
						this.blockCommentBuffer.append(this.blockCommentTokensBuffer);
						this.column += this.blockCommentTokensBuffer.length();
						this.blockCommentTokensBuffer.setLength(0);
						bufferHasTokens = true;
						bufferHasNewLine = false;
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
						this.blockCommentBuffer.append(' ');
					}
					previousToken = token;
					if (this.scanner.currentCharacter == '/') {
						editEnd = this.scanner.startPosition - 1;
						// Add remaining buffered tokens
						if (this.blockCommentTokensBuffer.length() > 0) {
							this.blockCommentBuffer.append(this.blockCommentTokensBuffer);
							this.column += this.blockCommentTokensBuffer.length();
						}
						// end of comment
						if (newLinesAtBoundaries) {
							if (multiLines || hasMultiLines) {
						    	this.blockCommentBuffer.append(this.lineSeparator);
						    	this.column = 1;
						    	printIndentationIfNecessary(this.blockCommentBuffer);
							}
						}
						this.blockCommentBuffer.append(' ');
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
				linesGap = newLinesAtBoundaries ? lineNumber - firstLine : 0;
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
					if (this.blockCommentTokensBuffer.length() > 0) {
						if (hasTextOnFirstLine == 1) {
							printBlockCommentHeaderLine(this.blockCommentBuffer);
							hasTextOnFirstLine = -1;
						}
						this.blockCommentBuffer.append(this.blockCommentTokensBuffer);
						this.blockCommentTokensBuffer.setLength(0);
						bufferHasTokens = true;
					}
			    	this.blockCommentBuffer.append(this.lineSeparator);
			    	this.column = 1;
			    	printIndentationIfNecessary(this.blockCommentBuffer);
		    		this.blockCommentBuffer.append(BLOCK_LINE_PREFIX);
		    		this.column += BLOCK_LINE_PREFIX_LENGTH;
		    		firstWord = true;
					multiLines = true;
					bufferHasNewLine = true;
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
    		int lastColumn = this.column + this.blockCommentTokensBuffer.length() + tokenLength;
    		if (insertSpace) lastColumn++;

    		// Append next token inserting a new line if max line is reached
			if (lineHasTokens && !firstWord && lastColumn > maxColumn) {
		    	String tokensString = this.blockCommentTokensBuffer.toString().trim();
		    	int tokensStringLength = tokensString.length();
				// not enough space on the line
				if (hasTextOnFirstLine == 1) {
					printBlockCommentHeaderLine(this.blockCommentBuffer);
				}
				if ((this.indentationLevel+tokensStringLength+tokenLength) > maxColumn) {
					// there won't be enough room even if we break the line before the buffered tokens
					// So add the buffered tokens now
					this.blockCommentBuffer.append(this.blockCommentTokensBuffer);
					this.column += this.blockCommentTokensBuffer.length();
					this.blockCommentTokensBuffer.setLength(0);
					bufferHasNewLine = false;
					bufferHasTokens = true;
				}
				if (bufferHasTokens && !bufferHasNewLine) {
			    	this.blockCommentBuffer.append(this.lineSeparator);
			    	this.column = 1;
			    	printIndentationIfNecessary(this.blockCommentBuffer);
		    		this.blockCommentBuffer.append(BLOCK_LINE_PREFIX);
			    	this.column += BLOCK_LINE_PREFIX_LENGTH;
				}
		    	if (this.blockCommentTokensBuffer.length() > 0) {
					this.blockCommentBuffer.append(tokensString);
					this.column += tokensStringLength;
					this.blockCommentTokensBuffer.setLength(0);
		    	}
				this.blockCommentBuffer.append(this.scanner.source, tokenStart, tokenLength);
				bufferHasTokens = true;
				bufferHasNewLine = false;
				this.column += tokenLength;
				multiLines = true;
				hasTextOnFirstLine = -1;
			} else {
				// append token to the line
				if (insertSpace)  {
					this.blockCommentTokensBuffer.append(' ');
				}
				this.blockCommentTokensBuffer.append(this.scanner.source, tokenStart, tokenLength);
			}
			previousToken = token;
			newLine = false;
    		firstWord = false;
			scannerLine = lineNumber;
			lastTextLine = lineNumber;
		}

		// Replace block comment text
		if (this.nlsTagCounter == 0 || !multiLines) {
			if (hasTokens || multiLines) {
				StringBuffer replacement;
				if (hasTextOnFirstLine == 1) {
					this.blockCommentTokensBuffer.setLength(0);
					replacement = this.blockCommentTokensBuffer;
					if ((hasMultiLines || multiLines)) {
						int col = this.column;
						replacement.append(this.lineSeparator);
						this.column = 1;
						printIndentationIfNecessary(replacement);
						replacement.append(BLOCK_LINE_PREFIX);
				    	this.column = col;
					} else if (this.blockCommentBuffer.length()==0 || this.blockCommentBuffer.charAt(0)!=' ') {
						replacement.append(' ');
					}
					replacement.append(this.blockCommentBuffer);
				} else {
					replacement = this.blockCommentBuffer;
				}
				addReplaceEdit(editStart, editEnd, replacement.toString());
			}
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
		if (!this.formatter.preferences.comment_new_lines_at_block_boundaries) {
			buffer.insert(0, ' ');
			this.column++;
		}
	    else if (buffer.length() == 0) {
	    	buffer.append(this.lineSeparator);
	    	this.column = 1;
	    	printIndentationIfNecessary(buffer);
	    	buffer.append(BLOCK_LINE_PREFIX);
	    	this.column += BLOCK_LINE_PREFIX_LENGTH;
	    } else {
	    	this.tempBuffer.setLength(0);
	    	this.tempBuffer.append(this.lineSeparator);
	    	this.column = 1;
			printIndentationIfNecessary(this.tempBuffer);
	    	this.tempBuffer.append(BLOCK_LINE_PREFIX);
	    	this.column += BLOCK_LINE_PREFIX_LENGTH;
	    	buffer.insert(0, this.tempBuffer.toString());
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
	private void printCodeSnippet(int startPosition, int endPosition, int linesGap) {
		String snippet = new String(this.scanner.source, startPosition, endPosition - startPosition + 1);
	
		// 1 - strip content prefix (@see JavaDocRegion#preprocessCodeSnippet)
		int firstLine = Util.getLineNumber(startPosition, this.lineEnds, 0, this.maxLines) - 1;
		int lastLine = Util.getLineNumber(endPosition, this.lineEnds, firstLine>1 ? firstLine-2 : 0, this.maxLines) - 1;
		this.codeSnippetBuffer.setLength(0);
		if (firstLine == lastLine && linesGap == 0) {
			this.codeSnippetBuffer.append(snippet);
		} else {
			boolean hasCharsAfterStar = false;
			if (linesGap == 0) {
				this.codeSnippetBuffer.append(this.scanner.source, startPosition, this.lineEnds[firstLine]+1-startPosition);
				firstLine++;
			}
			int initialLength = this.codeSnippetBuffer.length();
			for (int currentLine=firstLine; currentLine<=lastLine; currentLine++) {
				this.scanner.resetTo(this.lineEnds[currentLine-1]+1, this.lineEnds[currentLine]);
				int lineStart = this.scanner.currentPosition;
				boolean hasStar = false;
				loop: while (!this.scanner.atEnd()) {
					char ch = (char) this.scanner.getNextChar();
					switch (ch) {
						case ' ':
						case '\t' :
						case '\u000c' :
							break;
						case '\r' :
						case '\n' :
							break loop;
						case '*':
							hasStar = true;
							break loop;
						default:
							if (ScannerHelper.isWhitespace(ch)) {
								break;
							}
							break loop;
					}
				}
				if (hasStar) {
					lineStart = this.scanner.currentPosition;
					if (!hasCharsAfterStar && !this.scanner.atEnd()) {
						char ch = (char) this.scanner.getNextChar();
						boolean atEnd = this.scanner.atEnd();
						switch (ch) {
							case ' ':
							case '\t' :
							case '\u000c' :
								break;
							case '\r' :
							case '\n' :
								atEnd = true;
								break;
							default:
								if (!ScannerHelper.isWhitespace(ch)) {
									if (hasStar) {
										// A non whitespace character is just after the star
										// then we need to restart from the beginning without
										// consuming the space after the star
										hasCharsAfterStar = true;
										currentLine = firstLine-1;
										this.codeSnippetBuffer.setLength(initialLength);
										continue;
									}
								}
								break;
						}
						if (!hasCharsAfterStar && !atEnd) {
							// Until then, there's always a whitespace after each star
							// of the comment, hence we need to consume it as it will
							// be rewritten while reindenting the snippet lines
							lineStart = this.scanner.currentPosition;
						}
					}
				}
				int end = currentLine == lastLine ? endPosition : this.lineEnds[currentLine];
				this.codeSnippetBuffer.append(this.scanner.source, lineStart, end+1-lineStart);
			}
		}
	
		// 2 - convert HTML to Java (@see JavaDocRegion#convertHtml2Java)
		HTMLEntity2JavaReader reader= new HTMLEntity2JavaReader(new StringReader(this.codeSnippetBuffer.toString()));
		char[] buf= new char[this.codeSnippetBuffer.length()]; // html2text never gets longer, only shorter!
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
			formattedSnippet = this.codeSnippetBuffer.toString();
		} else {
			// 3.b - valid code formatted
			// 3.b.i - get the result
			formattedSnippet = CommentFormatterUtil.evaluateFormatterEdit(convertedSnippet, edit, null);
	
			// 3.b.ii- convert back to HTML (@see JavaDocRegion#convertJava2Html)
			Java2HTMLEntityReader javaReader= new Java2HTMLEntityReader(new StringReader(formattedSnippet));
			buf= new char[256];
			this.codeSnippetBuffer.setLength(0);
			int l;
			try {
				do {
					l= javaReader.read(buf);
					if (l != -1)
						this.codeSnippetBuffer.append(buf, 0, l);
				} while (l > 0);
				formattedSnippet = this.codeSnippetBuffer.toString();
			} catch (IOException e) {
				// should not happen
				CommentFormatterUtil.log(e);
				return;
			}
		}
	
		// 4 - add the content prefix (@see JavaDocRegion#postprocessCodeSnippet)
		this.codeSnippetBuffer.setLength(0);
		ILineTracker tracker = new DefaultLineTracker();
		this.column = 1;
		printIndentationIfNecessary(this.codeSnippetBuffer); // append indentation
		this.codeSnippetBuffer.append(BLOCK_LINE_PREFIX);
		String linePrefix = this.codeSnippetBuffer.toString();
		this.codeSnippetBuffer.setLength(0);
		String replacement = formattedSnippet;
		tracker.set(formattedSnippet);
		int numberOfLines = tracker.getNumberOfLines();
		if (numberOfLines > 1) {
			int lastLineOffset = -1;
			for (int i=0; i<numberOfLines-1; i++) {
				if (i>0) this.codeSnippetBuffer.append(linePrefix);
				try {
					lastLineOffset = tracker.getLineOffset(i+1);
					this.codeSnippetBuffer.append(formattedSnippet.substring(tracker.getLineOffset(i), lastLineOffset));
				} catch (BadLocationException e) {
					// should not happen
					CommentFormatterUtil.log(e);
					return;
				}
			}
			this.codeSnippetBuffer.append(linePrefix);
			this.codeSnippetBuffer.append(formattedSnippet.substring(lastLineOffset));
			replacement = this.codeSnippetBuffer.toString();
		}
	
		// 5 - replace old text with the formatted snippet
		addReplaceEdit(startPosition, endPosition, replacement);
	}

	void printComment() {
		printComment(CodeFormatter.K_UNKNOWN, NO_TRAILING_COMMENT, PRESERVE_EMPTY_LINES_KEEP_LAST_NEW_LINES_INDENTATION);
	}

	void printComment(int emptyLinesRules) {
		printComment(CodeFormatter.K_UNKNOWN, NO_TRAILING_COMMENT, emptyLinesRules);
	}

	void printComment(int kind, int trailing) {
		printComment(kind, trailing, PRESERVE_EMPTY_LINES_KEEP_LAST_NEW_LINES_INDENTATION);
	}

	/*
	 * Main method to print and format comments (javadoc, block and single line comments)
	 */
	void printComment(int kind, int trailing, int emptyLinesRules) {
		final boolean rejectLineComment = kind  == CodeFormatter.K_MULTI_LINE_COMMENT || kind == CodeFormatter.K_JAVA_DOC;
		final boolean rejectBlockComment = kind  == CodeFormatter.K_SINGLE_LINE_COMMENT || kind  == CodeFormatter.K_JAVA_DOC;
		final boolean rejectJavadocComment = kind  == CodeFormatter.K_SINGLE_LINE_COMMENT || kind  == CodeFormatter.K_MULTI_LINE_COMMENT;
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasComment = false;
			boolean hasLineComment = false;
			boolean hasWhitespaces = false;
			int lines = 0;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				int foundTaskCount = this.scanner.foundTaskCount;
				int tokenStartPosition = this.scanner.getCurrentTokenStartPosition();
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						int whitespacesEndPosition = this.scanner.getCurrentTokenEndPosition();
						lines = 0;
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									lines++;
									break;
								case '\n' :
									lines++;
							}
						}
						// If following token is a line comment on the same line or the line just after,
						// then it might be not really formatted as a trailing comment
						boolean realTrailing = trailing > NO_TRAILING_COMMENT;
						if (realTrailing && this.scanner.currentCharacter == '/' && (lines == 0 || (lines == 1 && !hasLineComment && trailing == IMPORT_TRAILING_COMMENT))) {
							// sometimes changing the trailing may not be the best idea
							// for complex trailing comment, it's basically a good idea
							boolean canChangeTrailing = (trailing & COMPLEX_TRAILING_COMMENT) != 0;
							// for basic trailing comment preceded by a line comment, then it depends on the comments relative position
							// when following comment column (after having been rounded) is below the preceding one,
							// then it becomes not a good idea to change the trailing flag
							if (trailing == BASIC_TRAILING_COMMENT && hasLineComment) {
								int currentCommentIndentation = getCurrentIndentation(whiteSpaces, 0);
								int relativeIndentation = currentCommentIndentation - this.lastLineComment.currentIndentation;
								if (this.tabLength == 0) {
									canChangeTrailing = relativeIndentation == 0;
								} else {
									canChangeTrailing = relativeIndentation > -this.tabLength;
								}
							}
							// if the trailing can be change, then look at the following tokens
							if (canChangeTrailing) {
								int currentPosition = this.scanner.currentPosition;
								if (this.scanner.getNextToken() == TerminalTokens.TokenNameCOMMENT_LINE) {
									realTrailing = !hasLineComment;
									switch (this.scanner.getNextToken()) {
										case TerminalTokens.TokenNameCOMMENT_LINE:
											// at least two contiguous line comments
											// the formatter should not consider comments as trailing ones
											realTrailing = false;
											break;
										case TerminalTokens.TokenNameWHITESPACE:
											if (this.scanner.getNextToken() == TerminalTokens.TokenNameCOMMENT_LINE) {
												// at least two contiguous line comments
												// the formatter should not consider comments as trailing ones
												realTrailing = false;
											}
											break;
									}
								}
								this.scanner.resetTo(currentPosition, this.scanner.eofPosition - 1);
							}
						}
						// Look whether comments line may be contiguous or not
						// Note that when preceding token is a comment line, then only one line
						// is enough to have an empty line as the line end is included in the comment line...
						// If comments are contiguous, store the white spaces to be able to compute the current comment indentation
						if (lines > 1 || (lines == 1 && hasLineComment)) {
							this.lastLineComment.contiguous = false;
						}
						this.lastLineComment.leadingSpaces = whiteSpaces;
						this.lastLineComment.lines = lines;
						// Strategy to consume spaces and eventually leave at this stage
						// depends on the fact that a trailing comment is expected or not
						if (realTrailing) {
							// if a line comment is consumed, no other comment can be on the same line after
							if (hasLineComment) {
								if (lines >= 1) {
									currentTokenStartPosition = tokenStartPosition;
									preserveEmptyLines(lines, currentTokenStartPosition);
									addDeleteEdit(currentTokenStartPosition, whitespacesEndPosition);
									this.scanner.resetTo(this.scanner.currentPosition, this.scannerEndPosition - 1);
									return;
								}
								this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
								return;
							} 
							// if one or several new lines are consumed, following comments cannot be considered as trailing ones
							if (lines >= 1) {
								if (hasComment) {
									this.printNewLine(tokenStartPosition);
								}
								this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
								return;
							}
							// delete consumed white spaces
							hasWhitespaces = true;
							currentTokenStartPosition = this.scanner.currentPosition;
							addDeleteEdit(tokenStartPosition, whitespacesEndPosition);
						} else {
							if (lines == 0) {
								hasWhitespaces = true;
								if (hasLineComment && emptyLinesRules != PRESERVE_EMPTY_LINES_KEEP_LAST_NEW_LINES_INDENTATION) {
									addReplaceEdit(tokenStartPosition, whitespacesEndPosition, getPreserveEmptyLines(0, emptyLinesRules));
								} else {
									addDeleteEdit(tokenStartPosition, whitespacesEndPosition);
								}
							} else if (hasLineComment) {
								useAlignmentBreakIndentation(emptyLinesRules);
								currentTokenStartPosition = tokenStartPosition;
								preserveEmptyLines(lines, currentTokenStartPosition);
								addDeleteEdit(currentTokenStartPosition, whitespacesEndPosition);
							} else if (hasComment) {
								useAlignmentBreakIndentation(emptyLinesRules);
								if (lines == 1) {
									this.printNewLine(tokenStartPosition);
								} else {
									preserveEmptyLines(lines - 1, tokenStartPosition);
								}
								addDeleteEdit(tokenStartPosition, whitespacesEndPosition);
							} else if (lines != 0 && (!this.formatter.preferences.join_wrapped_lines || this.formatter.preferences.number_of_empty_lines_to_preserve != 0 || this.blank_lines_between_import_groups > 0)) {
								addReplaceEdit(tokenStartPosition, whitespacesEndPosition, getPreserveEmptyLines(lines-1, emptyLinesRules));
							} else {
								useAlignmentBreakIndentation(emptyLinesRules);
								addDeleteEdit(tokenStartPosition, whitespacesEndPosition);
							}
						}
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (this.useTags && this.editsEnabled) {
							boolean turnOff = false;
							if (foundTaskCount > 0) {
								setEditsEnabled(foundTaskCount);
								turnOff = true;
							} else if (this.tagsKind == this.currentToken
								&& CharOperation.fragmentEquals(this.disablingTag, this.scanner.source, tokenStartPosition, true)) {
    							this.editsEnabled = false;
								turnOff = true;
					    	}
							if (turnOff) {
								if (!this.editsEnabled && this.editsIndex > 1) {
									OptimizedReplaceEdit currentEdit = this.edits[this.editsIndex-1];
									if (this.scanner.startPosition == currentEdit.offset+currentEdit.length) {
										printNewLinesBeforeDisablingComment();
									}
								}
							}
						}
						if (rejectLineComment) break;
						if (lines >= 1) {
							if (lines > 1) {
								preserveEmptyLines(lines - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (lines == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespaces) {
							space();
						}
						hasWhitespaces = false;
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;
						lines = 0;
						if (this.useTags && !this.editsEnabled) {
							if (foundTaskCount > 0) {
								setEditsEnabled(foundTaskCount);
							} else if (this.tagsKind == this.currentToken) {
	    						this.editsEnabled = CharOperation.fragmentEquals(this.enablingTag, this.scanner.source, tokenStartPosition, true);
					    	}
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (this.useTags && this.editsEnabled) {
							boolean turnOff = false;
							if (foundTaskCount > 0) {
								setEditsEnabled(foundTaskCount);
								turnOff = true;
							} else if (this.tagsKind == this.currentToken
								&& CharOperation.fragmentEquals(this.disablingTag, this.scanner.source, tokenStartPosition, true)) {
    							this.editsEnabled = false;
								turnOff = true;
					    	}
							if (turnOff) {
								if (!this.editsEnabled && this.editsIndex > 1) {
									OptimizedReplaceEdit currentEdit = this.edits[this.editsIndex-1];
									if (this.scanner.startPosition == currentEdit.offset+currentEdit.length) {
										printNewLinesBeforeDisablingComment();
									}
								}
							}
						}
						if (trailing > NO_TRAILING_COMMENT && lines >= 1) {
							// a block comment on next line means that there's no trailing comment
							this.scanner.resetTo(this.scanner.getCurrentTokenStartPosition(), this.scannerEndPosition - 1);
							return;
						}
						this.lastLineComment.contiguous = false;
						if (rejectBlockComment) break;
						if (lines >= 1) {
							if (lines > 1) {
								preserveEmptyLines(lines - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (lines == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespaces) {
							space();
						}
						hasWhitespaces = false;
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						lines = 0;
						if (this.useTags && !this.editsEnabled) {
							if (foundTaskCount > 0) {
								setEditsEnabled(foundTaskCount);
							} else if (this.tagsKind == this.currentToken) {
	    						this.editsEnabled = CharOperation.fragmentEquals(this.enablingTag, this.scanner.source, tokenStartPosition, true);
					    	}
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						if (this.useTags && this.editsEnabled && foundTaskCount > 0) {
							setEditsEnabled(foundTaskCount);
							if (!this.editsEnabled && this.editsIndex > 1) {
								OptimizedReplaceEdit currentEdit = this.edits[this.editsIndex-1];
								if (this.scanner.startPosition == currentEdit.offset+currentEdit.length) {
									printNewLinesBeforeDisablingComment();
								}
							}
						}
						if (trailing > NO_TRAILING_COMMENT) {
							// a javadoc comment should not be considered as a trailing comment
							this.scanner.resetTo(this.scanner.getCurrentTokenStartPosition(), this.scannerEndPosition - 1);
							return;
						}
						this.lastLineComment.contiguous = false;
						if (rejectJavadocComment) break;
						if (lines >= 1) {
							if (lines > 1) {
								preserveEmptyLines(lines - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (lines == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespaces) {
							space();
						}
						hasWhitespaces = false;
						if (includesJavadocComments()) {
							printJavadocComment(this.scanner.startPosition, this.scanner.currentPosition);
						} else {
							printBlockComment(true);
						}
						if (this.useTags && !this.editsEnabled && foundTaskCount > 0) {
							setEditsEnabled(foundTaskCount);
						}
						printNewLine();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						lines = 0;
						break;
					default :
						this.lastLineComment.contiguous = false;
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
		resetScanner(source.toCharArray());
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
			    printComment(kind, NO_TRAILING_COMMENT);
	    		break;
	    	case CodeFormatter.K_MULTI_LINE_COMMENT:
			    printComment(kind, NO_TRAILING_COMMENT);
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

    	// Print comment line indentation
    	int commentIndentationLevel;
   		boolean onFirstColumn = isOnFirstColumn(start);
    	if (this.indentationLevel == 0) {
    		commentIndentationLevel = this.column - 1;
    	} else {
			if (onFirstColumn &&
					((includesLineComments && !this.formatter.preferences.comment_format_line_comment_starting_on_first_column) ||
					 this.formatter.preferences.never_indent_line_comments_on_first_column)
    			) {
	   			commentIndentationLevel = this.column - 1;
    		} else {
    			// Indentation may be specific for contiguous comment
    			// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=293300
				if (this.lastLineComment.contiguous) {
					// The leading spaces have been set while looping in the printComment(int) method
					int currentCommentIndentation = getCurrentIndentation(this.lastLineComment.leadingSpaces, 0);
					// Keep the current comment indentation when over the previous contiguous line comment
					// and the previous comment has not been reindented
					int relativeIndentation = currentCommentIndentation - this.lastLineComment.currentIndentation;
					boolean similarCommentsIndentation = false;
					if (this.tabLength == 0) {
						similarCommentsIndentation = relativeIndentation == 0;
					} else if (relativeIndentation > -this.tabLength) {
						similarCommentsIndentation = relativeIndentation == 0 || currentCommentIndentation != 0 && this.lastLineComment.currentIndentation != 0;
					}
					if (similarCommentsIndentation && this.lastLineComment.indentation != this.indentationLevel) {
						int currentIndentationLevel = this.indentationLevel;
						this.indentationLevel = this.lastLineComment.indentation ;
						printIndentationIfNecessary();
						this.indentationLevel = currentIndentationLevel;
			   			commentIndentationLevel = this.lastLineComment.indentation ;
					} else {
						printIndentationIfNecessary();
			   			commentIndentationLevel = this.column - 1;
					}
				} else {
					if (this.currentAlignment != null && this.currentAlignment.kind == Alignment.ARRAY_INITIALIZER &&
						this.currentAlignment.fragmentCount > 0 &&
						this.indentationLevel < this.currentAlignment.breakIndentationLevel &&
						this.lastLineComment.lines > 0)
					{
						int currentIndentationLevel = this.indentationLevel;
						this.indentationLevel = this.currentAlignment.breakIndentationLevel;
		    			printIndentationIfNecessary();
						this.indentationLevel = currentIndentationLevel;
			   			commentIndentationLevel = this.currentAlignment.breakIndentationLevel;
					} else {
		    			printIndentationIfNecessary();
			   			commentIndentationLevel = this.column - 1;
					}
				}
    		}
    	}
    	
    	// Store line comment information
   		this.lastLineComment.contiguous = true;
		this.lastLineComment.currentIndentation = getCurrentCommentIndentation(currentTokenStartPosition);
		this.lastLineComment.indentation = commentIndentationLevel;
		
		// Add pending space if necessary
    	if (this.pendingSpace) {
    		if (this.formatter.preferences.comment_preserve_white_space_between_code_and_line_comments) {
    			addInsertEdit(currentTokenStartPosition, new String(this.lastLineComment.leadingSpaces));
    		} else {
    			addInsertEdit(currentTokenStartPosition, " "); //$NON-NLS-1$
    		}
    	}
    	this.needSpace = false;
    	this.pendingSpace = false;
    	int previousStart = currentTokenStartPosition;

		if (!isNlsTag && includesLineComments && (!onFirstColumn || this.formatter.preferences.comment_format_line_comment_starting_on_first_column)) {
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
    		if (this.currentAlignment.kind == Alignment.BINARY_EXPRESSION &&
    			this.currentAlignment.enclosing != null &&
    			this.currentAlignment.enclosing.kind == Alignment.BINARY_EXPRESSION &&
    			this.indentationLevel < this.currentAlignment.breakIndentationLevel)
    		{
    			this.indentationLevel = this.currentAlignment.breakIndentationLevel;
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
		if (this.indentationSize != 0) {
			this.numberOfIndentations = this.indentationLevel / this.indentationSize;
		}
		else{
			this.numberOfIndentations = 0;
		}

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
						this.tempBuffer.setLength(0);
						this.tempBuffer.append(this.lineSeparator);
						this.column = 1;
						if (!this.formatter.preferences.never_indent_line_comments_on_first_column) {
							printIndentationIfNecessary(this.tempBuffer);
						}
					    this.tempBuffer.append(LINE_COMMENT_PREFIX);
						this.column += LINE_COMMENT_PREFIX_LENGTH;
						newLineString = this.tempBuffer.toString();
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

		// Replace the line separator at the end of the comment if any...
		int startReplace = previousToken == SKIP_FIRST_WHITESPACE_TOKEN ? spaceStartPosition : lastTokenEndPosition;
		if (this.column == 1 && commentEnd >= startReplace) {
			addReplaceEdit(startReplace, commentEnd, this.formatter.preferences.line_separator);
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
		this.tempBuffer.setLength(0);
		printIndentationIfNecessary(this.tempBuffer);
		if (this.tempBuffer.length() > 0) {
			addInsertEdit(this.scanner.getCurrentTokenStartPosition(), this.tempBuffer.toString());
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
						if (this.tabLength > 0 && indentationsAsTab < numberOfLeadingIndents) {
							if (buffer != null) buffer.append('\t');
							indentationsAsTab++;
							int complement = this.tabLength - ((this.column - 1) % this.tabLength); // amount of space
							this.column += complement;
						} else {
							if (buffer != null) buffer.append(' ');
							this.column++;
						}
						this.needSpace = false;
					}
				} else if (this.tabLength > 0) {
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
							if (this.tabLength > 0 && (this.column - 1 + this.tabLength) <= this.indentationLevel) {
								if (buffer != null) buffer.append('\t');
								this.column += this.tabLength;
							} else if ((this.column - 1 + this.indentationSize) <= this.indentationLevel) {
								// print one indentation
								// note that this.indentationSize > 0 when entering in the following loop
								// hence this.column will be incremented and then avoid endless loop (see bug 290905)
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
						if (this.tabLength > 0 && (this.column - 1 + this.tabLength) <= this.indentationLevel) {
							if (buffer != null) buffer.append('\t');
							this.column += this.tabLength;
						} else if (this.indentationSize > 0 && (this.column - 1 + this.indentationSize) <= this.indentationLevel) {
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
				setCommentIndentation(commentIndentationLevel);
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
					if (newLines == 0 && (!node.isImmutable() || block.reference != null)) {
						newLines = printJavadocBlockNodesNewLines(block, node, previousEnd);
					}
					if (block.isImmutable()) {
						printJavadocGapLinesForImmutableBlock(block);
					} else {
						printJavadocGapLines(previousEnd+1, nodeStart-1, newLines, clearBlankLines, false, null);
					}
				} else {
					this.tempBuffer.setLength(0);
					if (newLines > 0) {
						for (int j=0; j<newLines; j++) {
							printJavadocNewLine(this.tempBuffer);
						}
						addInsertEdit(nodeStart, this.tempBuffer.toString());
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
				if (text.isImmutable()) {
					// Indent if new line was added
					if (text.isImmutableHtmlTag() && newLines > 0 && this.commentIndentation != null) {
				    	addInsertEdit(node.sourceStart, this.commentIndentation);
				    	this.column += this.commentIndentation.length();
					}
					printJavadocImmutableText(text, block, newLines > 0);
					this.column += getTextLength(block, text);
				} else if (text.isHtmlTag()) {
					printJavadocHtmlTag(text, block, newLines>0);
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
	    	boolean newLine = false;
			boolean headerLine = block.isHeaderLine() && this.lastNumberOfNewLines == 0;
			int firstColumn = 1 + this.indentationLevel + BLOCK_LINE_PREFIX_LENGTH;
			if (this.commentIndentation != null) firstColumn += this.commentIndentation.length();
			if (headerLine) maxColumn++;
			FormatJavadocText text = null;
			boolean isImmutableNode = node.isImmutable();
			boolean nodeIsText = node.isText();
			if (nodeIsText) {
	    		text = (FormatJavadocText)node;
			} else {
				FormatJavadocBlock inlinedBlock = (FormatJavadocBlock)node;
				if (isImmutableNode) {
					text = (FormatJavadocText) inlinedBlock.getLastNode();
					if (text != null) {
			    		length += inlinedBlock.tagEnd - inlinedBlock.sourceStart + 1;  // tag length
				    	if (nodeStart > (previousEnd+1)) {
				    		length++; // include space between nodes
				    	}
						this.scanner.resetTo(text.sourceStart , node.sourceEnd);
					}
				}
			}
	    	if (text != null) {
    			if (isImmutableNode) {
			    	if (nodeStart > (previousEnd+1)) {
			    		length++; // include space between nodes
			    	}
    				int lastColumn = this.column + length;
	    			while (!this.scanner.atEnd()) {
	    				try {
		    				int token = this.scanner.getNextToken();
		    				switch (token) {
		    					case TerminalTokens.TokenNameWHITESPACE:
		    						if (CharOperation.indexOf('\n', this.scanner.source, this.scanner.startPosition, this.scanner.currentPosition) >= 0) {
		    							return 0;
		    						}
									lastColumn = getCurrentIndentation(this.scanner.getCurrentTokenSource(), lastColumn);
		    						break;
		    					case TerminalTokens.TokenNameMULTIPLY:
		    						if (newLine) {
		    							newLine = false;
		    							continue;
		    						}
		    						lastColumn++;
		    						break;
		    					default:
					    			lastColumn += (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
		    						break;
		    				}
	    				}
	    				catch (InvalidInputException iie) {
	    					// maybe an unterminated string or comment
			    			lastColumn += (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
	    				}
	    				if (lastColumn > maxColumn) {
	    					return 1;
						}
	    			}
	    			return 0;
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
		this.javadocBlockRefBuffer.setLength(0);
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
								spacePosition = this.javadocBlockRefBuffer.length();
								// $FALL-THROUGH$ - fall through next case
							case -1:
								this.javadocBlockRefBuffer.append(' ');
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
								this.tempBuffer.setLength(0);
								this.tempBuffer.append(this.lineSeparator);
						    	this.column = 1;
						    	printIndentationIfNecessary(this.tempBuffer);
					    		this.tempBuffer.append(BLOCK_LINE_PREFIX);
					    		this.column += BLOCK_LINE_PREFIX_LENGTH;
								if (this.commentIndentation != null) {
							    	this.tempBuffer.append(this.commentIndentation);
							    	this.column += this.commentIndentation.length();
						    	}
						    	newLineString = this.tempBuffer.substring(0, this.tempBuffer.length()-1); // remove last space as buffer will be inserted before a space
						    	firstColumn = this.column;
							} else {
								this.column = firstColumn;
							}
							this.column = firstColumn + this.javadocBlockRefBuffer.length() - spacePosition - 1;
							this.javadocBlockRefBuffer.insert(spacePosition, newLineString);
							if (headerLine) {
								headerLine = false;
								maxColumn--;
							}
							spacePosition = -1;
						}
						this.javadocBlockRefBuffer.append(this.scanner.source, this.scanner.startPosition, tokenLength);
			    		this.column += tokenLength;
			    		break;
				}
				previousToken = token;
			} catch (InvalidInputException iie) {
				// does not happen as syntax is correct
			}
		}
		if (needFormat) {
		    addReplaceEdit(block.tagEnd+1, reference.sourceEnd, this.javadocBlockRefBuffer.toString());
		}
    }

	private int getTextLength(FormatJavadocBlock block, FormatJavadocText text) {

		// Special case for immutable tags
		if (text.isImmutable()) {
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
   					// maybe an unterminated string or comment
	    			textLength += (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
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
			int newLines = (this.formatter.preferences.comment_new_lines_at_javadoc_boundaries && (this.line > currentLine || javadoc.isMultiLine())) ? 1 : 0;
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
					this.javadocGapLinesBuffer.setLength(0);
					for (int i=0; i<newLines; i++) {
						this.javadocGapLinesBuffer.append(this.lineSeparator);
						this.column = 1;
						printIndentationIfNecessary(this.javadocGapLinesBuffer);
						if (footer) {
							this.javadocGapLinesBuffer.append(' ');
							this.column++;
						} else {
							this.javadocGapLinesBuffer.append(BLOCK_LINE_PREFIX);
							this.column += BLOCK_LINE_PREFIX_LENGTH;
						}
					}
					if (output == null) {
						addInsertEdit(textStartPosition, this.javadocGapLinesBuffer.toString());
					} else {
						output.append(this.javadocGapLinesBuffer);
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
			boolean endsOnMultiply = false;
			while (!this.scanner.atEnd()) {
				switch (this.scanner.getNextToken()) {
					case TerminalTokens.TokenNameMULTIPLY:
						// we just need to replace each lines between '*' with the javadoc formatted ones
						int linesGap = this.scanner.linePtr - linePtr;
						if (linesGap > 0) {
							this.javadocGapLinesBuffer.setLength(0);
							if (lineCount > 0) {
								// TODO https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
								this.javadocGapLinesBuffer.append( ' ');
							}
							for (int i = 0; i < linesGap ; i++) {
								if (clearBlankLines && lineCount >= newLines) {
									// leave as the required new lines have been inserted
									// so remove any remaining blanks and leave
									if (textEndPosition >= start) {
										if (output == null) {
											addReplaceEdit(start, textEndPosition, this.javadocGapLinesBuffer.toString());
										} else {
											output.append(this.javadocGapLinesBuffer);
										}
									}
									return;
								}
								this.javadocGapLinesBuffer.append(this.lineSeparator);
								this.column = 1;
								printIndentationIfNecessary(this.javadocGapLinesBuffer);
								if (i == (linesGap-1)) {
									this.javadocGapLinesBuffer.append(' ');
									this.column++;
								} else {
									this.javadocGapLinesBuffer.append(BLOCK_LINE_PREFIX);
									this.column += BLOCK_LINE_PREFIX_LENGTH;
								}
								lineCount++;
							}
							int currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
							int tokenLength = this.scanner.currentPosition - currentTokenStartPosition;
							if (output == null) {
								addReplaceEdit(start, currentTokenStartPosition-1, this.javadocGapLinesBuffer.toString());
							} else {
								output.append(this.javadocGapLinesBuffer);
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
						endsOnMultiply = true;
						break;
					default:
						endsOnMultiply = false;
						break;
				}
			}

			// Format the last whitespaces
			if (lineCount < newLines) {
				// Insert new lines as not enough was encountered while scanning the whitespaces
				this.javadocGapLinesBuffer.setLength(0);
				if (lineCount > 0) {
					// TODO https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
					this.javadocGapLinesBuffer.append( ' ');
				}
				for (int i = lineCount; i < newLines-1; i++) {
					printJavadocNewLine(this.javadocGapLinesBuffer);
				}
				this.javadocGapLinesBuffer.append(this.lineSeparator);
				this.column = 1;
				printIndentationIfNecessary(this.javadocGapLinesBuffer);
				if (footer) {
					this.javadocGapLinesBuffer.append(' ');
					this.column++;
				} else {
					this.javadocGapLinesBuffer.append(BLOCK_LINE_PREFIX);
					this.column += BLOCK_LINE_PREFIX_LENGTH;
				}
				if (output == null) {
					if (textEndPosition >= start) {
						addReplaceEdit(start, textEndPosition, this.javadocGapLinesBuffer.toString());
					} else {
						addInsertEdit(textEndPosition+1, this.javadocGapLinesBuffer.toString());
					}
				} else {
					output.append(this.javadocGapLinesBuffer);
				}
			} else {
				// Replace all remaining whitespaces by a single space
				if (textEndPosition >= start) {
					this.javadocGapLinesBuffer.setLength(0);
					if (this.scanner.linePtr > linePtr) {
						if (lineCount > 0) {
							// TODO https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
							this.javadocGapLinesBuffer.append(' ');
						}
						this.javadocGapLinesBuffer.append(this.lineSeparator);
						this.column = 1;
						printIndentationIfNecessary(this.javadocGapLinesBuffer);
					}
					this.javadocGapLinesBuffer.append(' ');
					if (output == null) {
						addReplaceEdit(start, textEndPosition, this.javadocGapLinesBuffer.toString());
					} else {
						output.append(this.javadocGapLinesBuffer);
					}
					this.needSpace = false;
				} else if (endsOnMultiply) {
					if (output == null) {
						addInsertEdit(textEndPosition+1, " "); //$NON-NLS-1$
					} else {
						output.append(' ');
					}
					this.needSpace = false;
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

	private void printJavadocImmutableText(FormatJavadocText text, FormatJavadocBlock block, boolean textOnNewLine) {

		try {
			// Iterate on text line separators
			int textLineStart = text.lineStart;
			this.scanner.tokenizeWhiteSpace = false;
			String newLineString = null;
			for (int idx=0, max=text.separatorsPtr; idx<=max ; idx++) {
				int start = (int) text.separators[idx];
				int lineStart = Util.getLineNumber(start, this.lineEnds, textLineStart-1, this.maxLines);
				while (textLineStart < lineStart) {
					int end = this.lineEnds[textLineStart-1];
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
					if (newLineString == null) {
						this.tempBuffer.setLength(0);
						this.column = 1;
						printIndentationIfNecessary(this.tempBuffer);
						this.tempBuffer.append(BLOCK_LINE_PREFIX);
						this.column += BLOCK_LINE_PREFIX_LENGTH;
						newLineString = this.tempBuffer.toString();
					}
					addReplaceEdit(end+1, this.scanner.getCurrentTokenEndPosition(), newLineString);
					textLineStart = Util.getLineNumber(this.scanner.currentPosition-1, this.lineEnds, textLineStart, this.maxLines);
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

	/*
	 *  Print the gap lines for an immutable block.
	 *  That's needed to  be specific as the formatter needs to keep white spaces
	 *  if possible except those which are indentation ones.
	 *  Note that in the peculiar case of a two lines immutable tag (multi lines block),
	 *  the formatter will join the two lines.
	 */
	private void printJavadocGapLinesForImmutableBlock(FormatJavadocBlock block) {

		// Init
		int firstLineEnd = -1; // not initialized
		int newLineStart = -1; // not initialized
		int secondLineStart = -1; // not initialized
		int starPosition = -1; // not initialized
		int offset = 0;
		int start = block.tagEnd + 1;
		int end = block.nodes[0].sourceStart-1;
		this.scanner.resetTo(start, end);
		int lineStart = block.lineStart;
		int lineEnd = Util.getLineNumber(block.nodes[0].sourceEnd, this.lineEnds, lineStart-1, this.maxLines);
		boolean multiLinesBlock = lineEnd > (lineStart+1);
		int previousPosition = this.scanner.currentPosition;
		String newLineString = null;
		int indentationColumn = 0;
		int leadingSpaces = -1;

		// Scan the existing gap
		while (!this.scanner.atEnd()) {
			char ch = (char) this.scanner.getNextChar();
			switch (ch) {
				case '\t' :
					// increase the corresponding counter from the appropriate tab value
					if (secondLineStart > 0 || firstLineEnd < 0) {
						int reminder = this.tabLength == 0 ? 0 : offset % this.tabLength;
						if (reminder == 0) {
							offset += this.tabLength;
						} else {
							offset = ((offset / this.tabLength) + 1) * this.tabLength;
						}
					} else if (leadingSpaces >= 0) {
						int reminder = this.tabLength == 0 ? 0 : offset % this.tabLength;
						if (reminder == 0) {
							leadingSpaces += this.tabLength;
						} else {
							leadingSpaces = ((offset / this.tabLength) + 1) * this.tabLength;
						}
					}
					break;
				case '\r' :
				case '\n' :
					// new line, store the end of the first one
					if (firstLineEnd < 0) {
						firstLineEnd = previousPosition;
					}
					// print indentation if there were spaces without any star on the line
					if (leadingSpaces > 0 && multiLinesBlock) {
						if (newLineString == null) {
							this.column = 1;
							this.tempBuffer.setLength(0);
							printIndentationIfNecessary(this.tempBuffer);
							this.tempBuffer.append(BLOCK_LINE_PREFIX);
							this.column += BLOCK_LINE_PREFIX_LENGTH;
							newLineString = this.tempBuffer.toString();
							indentationColumn = this.column;
						} else {
							this.column = indentationColumn;
						}
						addReplaceEdit(newLineStart, newLineStart+indentationColumn-2, newLineString);
					}
					// store line start and reset positions
					newLineStart = this.scanner.currentPosition;
					leadingSpaces = 0;
					starPosition = -1;
					if (multiLinesBlock) {
						offset = 0;
						secondLineStart = -1;
					}
					break;
				case '*' :
					// store line start position if this is the first star of the line
					if (starPosition < 0 && firstLineEnd > 0) {
						secondLineStart = this.scanner.currentPosition;
						starPosition = this.scanner.currentPosition;
						leadingSpaces = -1;
					}
					break;
				default :
					// increment offset if line has started
					if (secondLineStart > 0) {
						// skip first white space after the first '*'
						if (secondLineStart == starPosition) {
							secondLineStart = this.scanner.currentPosition;
						} else {
							// print indentation before the following characters
							if (offset == 0 && multiLinesBlock) {
								if (newLineString == null) {
									this.tempBuffer.setLength(0);
									this.column = 1;
									printIndentationIfNecessary(this.tempBuffer);
									this.tempBuffer.append(BLOCK_LINE_PREFIX);
									this.column += BLOCK_LINE_PREFIX_LENGTH;
									indentationColumn = this.column;
									newLineString = this.tempBuffer.toString();
								} else {
									this.column = indentationColumn;
								}
								addReplaceEdit(newLineStart, secondLineStart-1, newLineString);
							}
							offset++;
						}
					} else if (firstLineEnd < 0) {
						// no new line yet, increment the offset
						offset++;
					} else if (leadingSpaces >= 0) {
						// no star yet, increment the leading spaces
						leadingSpaces++;
					}
					break;
			}
			previousPosition = this.scanner.currentPosition;
		}
		
		// Increment the columns from the numbers of characters counted on the line
		if (multiLinesBlock) {
			this.column += offset;
		} else {
			this.column++;
		}
		
		// Replace the new line with a single space when there's only one separator
		// or, if necessary, print the indentation on the last line
		if (!multiLinesBlock) {
			if (firstLineEnd > 0) {
				addReplaceEdit(firstLineEnd, end, " "); //$NON-NLS-1$
			}
		}
		else if (secondLineStart > 0) {
			if (newLineString == null) {
				this.tempBuffer.setLength(0);
				this.column = 1;
				printIndentationIfNecessary(this.tempBuffer);
				this.tempBuffer.append(BLOCK_LINE_PREFIX);
				this.column += BLOCK_LINE_PREFIX_LENGTH;
				newLineString = this.tempBuffer.toString();
				indentationColumn = this.column;
			} else {
				this.column = indentationColumn;
			}
			addReplaceEdit(newLineStart, secondLineStart-1, newLineString);
		}
		else if (leadingSpaces > 0) {
			if (newLineString == null) {
				this.tempBuffer.setLength(0);
				this.column = 1;
				printIndentationIfNecessary(this.tempBuffer);
				this.tempBuffer.append(BLOCK_LINE_PREFIX);
				this.column += BLOCK_LINE_PREFIX_LENGTH;
				newLineString = this.tempBuffer.toString();
				indentationColumn = this.column;
			} else {
				this.column = indentationColumn;
			}
			addReplaceEdit(newLineStart, newLineStart+indentationColumn-2, newLineString);
		}

		// Reset
		this.needSpace = false;
		this.scanner.resetTo(end+1, this.scannerEndPosition - 1);
	}

	private int printJavadocHtmlTag(FormatJavadocText text, FormatJavadocBlock block, boolean textOnNewLine) {

		// Compute indentation if necessary
		boolean clearBlankLines = this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment;

		// Local variables init
		int textStart = text.sourceStart;
		int nextStart = textStart;
		int startLine = Util.getLineNumber(textStart, this.lineEnds, 0, this.maxLines);
	    int htmlTagID = text.getHtmlTagID();
	    if (text.depth >= this.javadocHtmlTagBuffers.length) {
	    	int length = this.javadocHtmlTagBuffers.length;
	    	System.arraycopy(this.javadocHtmlTagBuffers, 0, this.javadocHtmlTagBuffers = new StringBuffer[text.depth+6], 0, length);
	    }
	    StringBuffer buffer = this.javadocHtmlTagBuffers[text.depth];
	    if (buffer == null) {
	    	buffer = new StringBuffer();
	    	this.javadocHtmlTagBuffers[text.depth] = buffer;
	    } else {
	    	buffer.setLength(0);
	    }

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
				boolean immutable = node.isImmutable();
				if (newLines == 0) {
					newLines = printJavadocBlockNodesNewLines(block, node, previousEnd);
				}
				int nodeStart = node.sourceStart;
				if (newLines > 0 || (idx > 1 && nodeStart > (previousEnd+1))) {
					printJavadocGapLines(previousEnd+1, nodeStart-1, newLines, clearBlankLines, false, null);
				}
				if (newLines > 0) textOnNewLine = true;
				buffer.setLength(0);
				if (node.isText()) {
					if (immutable) {
						// do not change immutable tags, just increment column
						if (textOnNewLine && this.commentIndentation != null) {
					    	addInsertEdit(node.sourceStart, this.commentIndentation);
					    	this.column += this.commentIndentation.length();
						}
						printJavadocImmutableText(htmlTag, block, textOnNewLine);
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
					printJavadocGapLines(previousEnd+1, nextStart-1, linesAfter, clearBlankLines, false, buffer);
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
						// See whether there's a space before the code
						if (linesGap > 0) {
							int lineStart = this.scanner.getLineStart(startLine);
							if (nextStart > lineStart) { // if code starts at the line, then no leading space is needed
								this.scanner.resetTo(lineStart, nextStart-1);
								try {
									int token = this.scanner.getNextToken();
									if (token == TerminalTokens.TokenNameWHITESPACE) {
										// skip indentation
										token = this.scanner.getNextToken();
									}
									if (token == TerminalTokens.TokenNameMULTIPLY) {
										nextStart = this.scanner.currentPosition;
									}
								}
								catch (InvalidInputException iie) {
									// skip
								}
							}
						}
						// Format gap lines before code
						int newLines = linesGap;
						if (newLines == 0) newLines=1;
						this.needSpace = false;
						printJavadocGapLines(end+1, nextStart-1, newLines, false/* clear first blank lines inside <pre> tag as done by old formatter */, false, null);
						// Format the code
						printCodeSnippet(nextStart, codeEnd, linesGap);
						// Format the gap lines after the code
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
		boolean isValidHtmlSeparatorTag = max > 0 && isHtmlSeparatorTag && closingTag;
		if (previousEnd != -1) {
		    if (isValidHtmlSeparatorTag) {
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
	    return isValidHtmlSeparatorTag ? 1 : 0;
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
		this.javadocTextBuffer.setLength(0);
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
			printJavadocTextLine(this.javadocTextBuffer, nextStart, end, block, idx==0 || (!joinLines && textOnNewLine)/*first text?*/, needIndentation, false /*not an html tag*/);
			textOnNewLine = false;

			// Replace with current buffer if there are several empty lines between text lines
			nextStart = (int) text.separators[idx];
			if (!clearBlankLines || !joinLines) {
				int endLine = Util.getLineNumber(end, this.lineEnds, startLine-1, this.maxLines);
				startLine = Util.getLineNumber(nextStart, this.lineEnds, endLine-1, this.maxLines);
				int gapLine = endLine;
				if (joinLines) gapLine++; // if not preserving line break then gap must be at least of one line
				if (startLine > gapLine) {
					addReplaceEdit(textStart, end, this.javadocTextBuffer.toString());
					textStart = nextStart;
					this.javadocTextBuffer.setLength(0);
					int newLines = startLine - endLine;
					if (clearBlankLines) newLines = 1;
					printJavadocGapLines(end+1, nextStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
					textOnNewLine = true;
				}
				else if (startLine > endLine) {
					textOnNewLine = !joinLines;
				}
			}
		}

		// Replace remaining line
		boolean needIndentation = textOnNewLine;
		this.needSpace = text.separatorsPtr >= 0;
		printJavadocTextLine(this.javadocTextBuffer, nextStart, text.sourceEnd, block, text.separatorsPtr==-1 /* first text?*/, needIndentation, false /*not an html tag*/);
		// TODO Bring back following optimization
		// if (lastNewLines != this.lastNumberOfNewLines || (this.column - currentColumn) != (text.sourceEnd - text.sourceStart + 1)) {
			addReplaceEdit(textStart, text.sourceEnd, this.javadocTextBuffer.toString());
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
		this.javadocTokensBuffer.setLength(0);
		int firstColumn = 1 + this.indentationLevel + BLOCK_LINE_PREFIX_LENGTH;
		int maxColumn = this.formatter.preferences.comment_line_length + 1;
		if (headerLine) {
			firstColumn++;
			maxColumn++;
		}
		if (needIndentation && this.commentIndentation != null) {
			buffer.append(this.commentIndentation);
	    	this.column += this.commentIndentation.length();
	    	firstColumn += this.commentIndentation.length();
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
	    		int tokensBufferLength = this.javadocTokensBuffer.length();
    			int tokenStart = this.scanner.getCurrentTokenStartPosition();
	    		int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - tokenStart;
				boolean insertSpace = (previousToken == TerminalTokens.TokenNameWHITESPACE || this.needSpace) && !textOnNewLine;
				String tokensBufferString = this.javadocTokensBuffer.toString().trim();
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
									this.tempBuffer.setLength(0);
									this.tempBuffer.append(this.lineSeparator);
							    	this.column = 1;
							    	printIndentationIfNecessary(this.tempBuffer);
						    		this.tempBuffer.append(BLOCK_LINE_PREFIX);
							    	this.column += BLOCK_LINE_PREFIX_LENGTH;
									if (this.commentIndentation != null) {
										this.tempBuffer.append(this.commentIndentation);
								    	this.column += this.commentIndentation.length();
									}
						    		firstColumn = this.column;
						    		newLineString = this.tempBuffer.toString();
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
								buffer.append(this.javadocTokensBuffer);
								this.column += tokensBufferLength;
							}
							this.javadocTokensBuffer.setLength(0);
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
					&& token != TerminalTokens.TokenNameAT && (tokensBufferLength == 0 || this.javadocTokensBuffer.charAt(tokensBufferLength-1) != '@'); // avoid to split just before a '@'
				if (shouldSplit) {
					// not enough space on the line
					if ((tokensBufferLength > 0 || tokenLength < maxColumn) && !isHtmlTag && tokensBufferLength > 0 && (firstColumn+tokensBufferLength+tokenLength) >= maxColumn) {
						// there won't be enough room even if we break the line before the buffered tokens
						// So add the buffered tokens now
						buffer.append(this.javadocTokensBuffer);
						this.column += tokensBufferLength;
						this.javadocTokensBuffer.setLength(0);
						tokensBufferLength = 0;
						textOnNewLine = false;
					}
					if ((tokensBufferLength > 0 || /*(firstColumn+tokenLength) < maxColumn || (insertSpace &&*/ this.column > firstColumn) && (!textOnNewLine || !firstText)) {
						this.lastNumberOfNewLines++;
						this.line++;
						if (newLineString == null) {
							this.tempBuffer.setLength(0);
							this.tempBuffer.append(this.lineSeparator);
					    	this.column = 1;
					    	printIndentationIfNecessary(this.tempBuffer);
				    		this.tempBuffer.append(BLOCK_LINE_PREFIX);
					    	this.column += BLOCK_LINE_PREFIX_LENGTH;
							if (this.commentIndentation != null) {
								this.tempBuffer.append(this.commentIndentation);
						    	this.column += this.commentIndentation.length();
							}
				    		firstColumn = this.column;
				    		newLineString = this.tempBuffer.toString();
						} else {
							this.column = firstColumn;
						}
						buffer.append(newLineString);
					}
			    	if (tokensBufferLength > 0) {
			    		String tokensString = tokensBufferString;
						buffer.append(tokensString);
						this.column += tokensString.length();
						this.javadocTokensBuffer.setLength(0);
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
		    			this.javadocTokensBuffer.append(' ');
		    		}
					this.javadocTokensBuffer.append(this.scanner.source, tokenStart, tokenLength);
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
			if (this.javadocTokensBuffer.length() > 0) {
				buffer.append(this.javadocTokensBuffer);
				this.column += this.javadocTokensBuffer.length();
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
				int foundTaskCount = this.scanner.foundTaskCount;
				int tokenStartPosition = this.scanner.getCurrentTokenStartPosition();
				int tokenEndPosition = this.scanner.getCurrentTokenEndPosition();
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
							boolean insertSpaceBeforeBrace = this.formatter.preferences.insert_space_before_opening_brace_in_array_initializer;
							this.formatter.preferences.insert_space_before_opening_brace_in_array_initializer = false;
							try {
								annotations[annotationsIndex++].traverse(visitor, (BlockScope) null);
							}
							finally {
								this.formatter.preferences.insert_space_before_opening_brace_in_array_initializer = insertSpaceBeforeBrace;
							}
							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122247
							boolean shouldAddNewLine = false;
							switch (annotationSourceKind) {
								case ICodeFormatterConstants.ANNOTATION_ON_TYPE :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_type) {
										shouldAddNewLine = true;
									}
									break;
								case ICodeFormatterConstants.ANNOTATION_ON_FIELD :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_field) {
										shouldAddNewLine = true;
									}
									break;
								case ICodeFormatterConstants.ANNOTATION_ON_METHOD :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_method) {
										shouldAddNewLine = true;
									}
									break;
								case ICodeFormatterConstants.ANNOTATION_ON_PACKAGE :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_package) {
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
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						if (this.useTags && this.editsEnabled) {
							boolean turnOff = false;
							if (foundTaskCount > 0) {
								setEditsEnabled(foundTaskCount);
								turnOff = true;
							} else if (this.tagsKind == this.currentToken
								&& CharOperation.equals(this.disablingTag, this.scanner.source, tokenStartPosition, tokenEndPosition+1)) {
    							this.editsEnabled = false;
								turnOff = true;
					    	}
							if (turnOff) {
								if (!this.editsEnabled && this.editsIndex > 1) {
									OptimizedReplaceEdit currentEdit = this.edits[this.editsIndex-1];
									if (this.scanner.startPosition == currentEdit.offset+currentEdit.length) {
										printNewLinesBeforeDisablingComment();
									}
								}
							}
						}
						printBlockComment(this.currentToken == TerminalTokens.TokenNameCOMMENT_JAVADOC);
						if (this.useTags && !this.editsEnabled) {
							if (foundTaskCount > 0) {
								setEditsEnabled(foundTaskCount);
							} else if (this.tagsKind == this.currentToken) {
	    						this.editsEnabled = CharOperation.equals(this.enablingTag, this.scanner.source, tokenStartPosition, tokenEndPosition+1);
					    	}
						}
						currentTokenStartPosition = this.scanner.currentPosition;
						hasComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						tokenEndPosition = -this.scanner.commentStops[this.scanner.commentPtr];
						if (this.useTags && this.editsEnabled) {
							boolean turnOff = false;
							if (foundTaskCount > 0) {
								setEditsEnabled(foundTaskCount);
								turnOff = true;
							} else if (this.tagsKind == this.currentToken
								&& CharOperation.equals(this.disablingTag, this.scanner.source, tokenStartPosition, tokenEndPosition)) {
    							this.editsEnabled = false;
								turnOff = true;
					    	}
							if (turnOff) {
								if (!this.editsEnabled && this.editsIndex > 1) {
									OptimizedReplaceEdit currentEdit = this.edits[this.editsIndex-1];
									if (this.scanner.startPosition == currentEdit.offset+currentEdit.length) {
										printNewLinesBeforeDisablingComment();
									}
								}
							}
						}
						printLineComment();
						if (this.useTags && !this.editsEnabled) {
							if (foundTaskCount > 0) {
								setEditsEnabled(foundTaskCount);
							} else if (this.tagsKind == this.currentToken) {
	    						this.editsEnabled = CharOperation.equals(this.enablingTag, this.scanner.source, tokenStartPosition, tokenEndPosition);
					    	}
						}
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
			// ensure that the scribe is at the beginning of a new line
			// only if no specific indentation has been previously set
			if (!this.preserveLineBreakIndentation) {
				this.column = 1; 
			}
			this.preserveLineBreakIndentation = false;
			return;
		}
		addInsertEdit(insertPosition, this.lineSeparator);
		this.line++;
		this.lastNumberOfNewLines = 1;
		this.column = 1;
		this.needSpace = false;
		this.pendingSpace = false;
		this.preserveLineBreakIndentation = false;
		this.lastLineComment.contiguous = false;
	}

	/*
	 * Print the indentation of a disabling comment
	 */
	private void printNewLinesBeforeDisablingComment() {

		// Get the beginning of comment line
		int linePtr = Arrays.binarySearch(this.lineEnds, this.scanner.startPosition);
		if (linePtr < 0) {
			linePtr = -linePtr - 1;
		}
		int indentation = 0;
		int beginningOfLine = getLineEnd(linePtr)+1;
		if (beginningOfLine == -1) {
			beginningOfLine = 0;
		}
		
		// If the comment is in the middle of the line, then there's nothing to do
		OptimizedReplaceEdit currentEdit = this.edits[this.editsIndex-1];
		int offset = currentEdit.offset;
		if (offset >= beginningOfLine) return;

		// Compute the comment indentation
		int scannerStartPosition = this.scanner.startPosition;
		int scannerEofPosition = this.scanner.eofPosition;
		int scannerCurrentPosition = this.scanner.currentPosition;
		char scannerCurrentChar = this.scanner.currentCharacter;
		int length = currentEdit.length;
		this.scanner.resetTo(beginningOfLine, offset+length-1);
		try {
			while (!this.scanner.atEnd()) {
				char ch = (char) this.scanner.getNextChar();
				switch (ch) {
					case '\t' :
						if (this.tabLength != 0) {
							int reminder = indentation % this.tabLength;
							if (reminder == 0) {
								indentation += this.tabLength;
							} else {
								indentation = ((indentation / this.tabLength) + 1) * this.tabLength;
							}
						}
						break;
					case ' ':
						indentation++;
						break;
					default:
						// Should not happen as the offset of the edit is before the beginning of line
						return;
				}
			}
		
			// Split the existing edit to keep the change before the beginning of the last line
			// but change the indentation after. Note that at this stage, the add*Edit methods
			// cannot be longer used as the edits are disabled
			String indentationString;
			int currentIndentation = getCurrentIndentation(this.scanner.currentPosition);
			if (currentIndentation > 0 && this.indentationLevel > 0) {
				int col = this.column;
				this.tempBuffer.setLength(0);
				printIndentationIfNecessary(this.tempBuffer);
				indentationString = this.tempBuffer.toString();
				this.column = col;
			} else {
				indentationString = Util.EMPTY_STRING;
			}
			String replacement = currentEdit.replacement;
			if (replacement.length() == 0) {
				// previous edit was a delete, as we're sure to have a new line before
				// the comment, then the edit needs to be either replaced entirely with
				// the expected indentation
				this.edits[this.editsIndex-1] = new OptimizedReplaceEdit(beginningOfLine, offset+length-beginningOfLine, indentationString);
			} else {
				int idx = replacement.lastIndexOf(this.lineSeparator);
				if (idx >= 0) {
					// replace current edit if it contains a line separator
					int start = idx + this.lsLength;
					this.tempBuffer.setLength(0);
					this.tempBuffer.append(replacement.substring(0, start));
					if (indentationString != Util.EMPTY_STRING) {
						this.tempBuffer.append(indentationString);
					}
					this.edits[this.editsIndex-1] = new OptimizedReplaceEdit(offset, length, this.tempBuffer.toString());
				}
			}
		}
		finally {
			this.scanner.startPosition = scannerStartPosition;
			this.scanner.eofPosition = scannerEofPosition;
			this.scanner.currentPosition = scannerCurrentPosition;
			this.scanner.currentCharacter = scannerCurrentChar;
		}
	}

	/*
	 * Print new lines characters when the edits are disabled. In this case, only
	 * the line separator is replaced if necessary, the other white spaces are untouched.
	 */
	private boolean printNewLinesCharacters(int offset, int length) {
		boolean foundNewLine = false;
		int scannerStartPosition = this.scanner.startPosition;
		int scannerEofPosition = this.scanner.eofPosition;
		int scannerCurrentPosition = this.scanner.currentPosition;
		char scannerCurrentChar = this.scanner.currentCharacter;
		this.scanner.resetTo(offset, offset+length-1);
		try {
			while (!this.scanner.atEnd()) {
				int start = this.scanner.currentPosition;
				char ch = (char) this.scanner.getNextChar();
				boolean needReplace = ch != this.firstLS;
				switch (ch) {
					case '\r':
						if (this.scanner.atEnd()) break;
						ch = (char) this.scanner.getNextChar();
						if (ch != '\n') break;
						needReplace = needReplace || this.lsLength != 2;
						//$FALL-THROUGH$
					case '\n':
						if (needReplace) {
							if (this.editsIndex == 0 || this.edits[this.editsIndex-1].offset != start) {
								this.edits[this.editsIndex++] = new OptimizedReplaceEdit(start, this.scanner.currentPosition-start, this.lineSeparator);
							}
						}
						foundNewLine = true;
						break;
				}
			}
		}
		finally {
			this.scanner.startPosition = scannerStartPosition;
			this.scanner.eofPosition = scannerEofPosition;
			this.scanner.currentPosition = scannerCurrentPosition;
			this.scanner.currentCharacter = scannerCurrentChar;
		}
		return foundNewLine;		
	}

	public void printNextToken(int expectedTokenType){
		printNextToken(expectedTokenType, false);
	}

	public void printNextToken(int expectedTokenType, boolean considerSpaceIfAny) {
		printNextToken(expectedTokenType, considerSpaceIfAny, PRESERVE_EMPTY_LINES_KEEP_LAST_NEW_LINES_INDENTATION);
	}

	public void printNextToken(int expectedTokenType, boolean considerSpaceIfAny, int emptyLineRules) {
		// Set brace flag, it's useful for the scribe while preserving line breaks
		printComment(CodeFormatter.K_UNKNOWN, NO_TRAILING_COMMENT, emptyLineRules);
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
		printComment(CodeFormatter.K_UNKNOWN, NO_TRAILING_COMMENT);
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
				printComment(CodeFormatter.K_UNKNOWN, NO_TRAILING_COMMENT);
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
				printComment(CodeFormatter.K_UNKNOWN, NO_TRAILING_COMMENT);
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
		// only called if this.tabLength > 0
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

	void redoAlignment(AlignmentException e){
		if (e.relativeDepth > 0) { // if exception targets a distinct context
			e.relativeDepth--; // record fact that current context got traversed
			this.currentAlignment = this.currentAlignment.enclosing; // pop currentLocation
			throw e; // rethrow
		}
		// reset scribe/scanner to restart at this given location
		resetAt(this.currentAlignment.location);
		this.scanner.resetTo(this.currentAlignment.location.inputOffset, this.scanner.eofPosition - 1);
		// clean alignment chunkKind so it will think it is a new chunk again
		this.currentAlignment.chunkKind = 0;
	}

	void redoMemberAlignment(AlignmentException e){
		// reset scribe/scanner to restart at this given location
		resetAt(this.memberAlignment.location);
		this.scanner.resetTo(this.memberAlignment.location.inputOffset, this.scanner.eofPosition - 1);
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

	/**
	 * @param compilationUnitSource
	 */
	public void resetScanner(char[] compilationUnitSource) {
		this.scanner.setSource(compilationUnitSource);
		this.scannerEndPosition = compilationUnitSource.length;
		this.scanner.resetTo(0, this.scannerEndPosition - 1);
		this.edits = new OptimizedReplaceEdit[INITIAL_SIZE];
		this.maxLines = this.lineEnds == null ? -1 : this.lineEnds.length - 1;
		this.scanner.lineEnds = this.lineEnds;
		this.scanner.linePtr = this.maxLines;
		initFormatterCommentParser();
	}

	private void resize() {
		System.arraycopy(this.edits, 0, (this.edits = new OptimizedReplaceEdit[this.editsIndex * 2]), 0, this.editsIndex);
	}

	private void setCommentIndentation(int commentIndentationLevel) {
		if (commentIndentationLevel == 0) {
		    this.commentIndentation = null;
		} else {
			int length = COMMENT_INDENTATIONS.length;
			if (commentIndentationLevel > length) {
				System.arraycopy(COMMENT_INDENTATIONS, 0, COMMENT_INDENTATIONS = new String[commentIndentationLevel+10], 0, length);
			}
			this.commentIndentation = COMMENT_INDENTATIONS[commentIndentationLevel-1];
			if (this.commentIndentation == null) {
				this.tempBuffer.setLength(0);
				for (int i=0; i<commentIndentationLevel; i++) {
					this.tempBuffer.append(' ');
				}
				this.commentIndentation = this.tempBuffer.toString();
				COMMENT_INDENTATIONS[commentIndentationLevel-1] = this.commentIndentation;
			}
		}
	}

	/*
	 * Look for the tags identified by the scanner to see whether some of them
	 * may change the status of the edition for the formatter.
	 * Do not return as soon as a match is found, as there may have several
	 * disabling/enabling tags in a comment, hence the last one will be the one really
	 * changing the formatter behavior...
	 */
	private void setEditsEnabled(int count) {
		for (int i=0; i<count; i++) {
			if (this.disablingTag != null && CharOperation.equals(this.scanner.foundTaskTags[i], this.disablingTag)) {
				this.editsEnabled = false;
			}
			if (this.enablingTag != null && CharOperation.equals(this.scanner.foundTaskTags[i], this.enablingTag)) {
				this.editsEnabled = true;
			}
		}
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
		if (this.tabLength > 0) {
			printRule(stringBuffer);
		}
		return stringBuffer.toString();
	}

	public void unIndent() {
		this.indentationLevel -= this.indentationSize;
		this.numberOfIndentations--;
	}
}
