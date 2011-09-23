/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.align;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.formatter.Location;
import org.eclipse.jdt.internal.formatter.Scribe;

/**
 * Alignment management
 *
 * @since 2.1
 */
public class Alignment {

	// Kind of alignment
	public int kind;
	public static final int ALLOCATION = 1;
	public static final int ANNOTATION_MEMBERS_VALUE_PAIRS = 2;
	public static final int ARRAY_INITIALIZER = 3;
	public static final int ASSIGNMENT = 4;
	public static final int BINARY_EXPRESSION = 5;
	public static final int CASCADING_MESSAGE_SEND = 6;
	public static final int COMPACT_IF = 7;
	public static final int COMPOUND_ASSIGNMENT = 8;
	public static final int CONDITIONAL_EXPRESSION = 9;
	public static final int ENUM_CONSTANTS = 10;
	public static final int ENUM_CONSTANTS_ARGUMENTS = 11;
	public static final int EXPLICIT_CONSTRUCTOR_CALL = 12;
	public static final int FIELD_DECLARATION_ASSIGNMENT = 13;
	public static final int LOCAL_DECLARATION_ASSIGNMENT = 14;
	public static final int MESSAGE_ARGUMENTS = 15;
	public static final int MESSAGE_SEND = 16;
	public static final int METHOD_ARGUMENTS = 17;
	public static final int METHOD_DECLARATION = 18;
	public static final int MULTIPLE_FIELD = 19;
	public static final int SUPER_CLASS = 20;
	public static final int SUPER_INTERFACES = 21;
	public static final int THROWS = 22;
	public static final int TYPE_MEMBERS = 23;
	public static final int STRING_CONCATENATION = 24;
	public static final int TRY_RESOURCES = 25;
	public static final int MULTI_CATCH = 26;

	// name of alignment
	public String name;
	public static final String[] NAMES = {
		"", //$NON-NLS-1$
		"allocation", //$NON-NLS-1$
		"annotationMemberValuePairs", //$NON-NLS-1$
		"array_initializer", //$NON-NLS-1$
		"assignmentAlignment", //$NON-NLS-1$
		"binaryExpressionAlignment", //$NON-NLS-1$
		"cascadingMessageSendAlignment", //$NON-NLS-1$
		"compactIf", //$NON-NLS-1$
		"compoundAssignmentAlignment", //$NON-NLS-1$
		"conditionalExpression", //$NON-NLS-1$
		"enumConstants", //$NON-NLS-1$
		"enumConstantArguments", //$NON-NLS-1$
		"explicit_constructor_call", //$NON-NLS-1$
		"fieldDeclarationAssignmentAlignment", //$NON-NLS-1$
		"localDeclarationAssignmentAlignment", //$NON-NLS-1$
		"messageArguments", //$NON-NLS-1$
		"messageAlignment", //$NON-NLS-1$
		"methodArguments", //$NON-NLS-1$
		"methodDeclaration", //$NON-NLS-1$
		"multiple_field", //$NON-NLS-1$
		"superclass", //$NON-NLS-1$
		"superInterfaces", //$NON-NLS-1$
		"throws", //$NON-NLS-1$
		"typeMembers", //$NON-NLS-1$
		"stringConcatenation", //$NON-NLS-1$
		"tryResources", //$NON-NLS-1$
		"unionTypeInMulticatch", //$NON-NLS-1$
	};

	// link to enclosing alignment
	public Alignment enclosing;

	// start location of this alignment
	public Location location;

	// indentation management
	public int fragmentIndex;
	public int fragmentCount;
	public int[] fragmentIndentations;
	public boolean needRedoColumnAlignment;

	// chunk management
	public int chunkStartIndex;
	public int chunkKind;

	// break management
	public int originalIndentationLevel;
	public int breakIndentationLevel;
	public int shiftBreakIndentationLevel;
	public int[] fragmentBreaks;
	public boolean wasSplit;
	public boolean blockAlign = false;
	public boolean tooLong = false;

	public Scribe scribe;

	// reset
	private boolean reset = false;

	/*
	 * Alignment modes
	 */
	public static final int M_FORCE = 1; // if bit set, then alignment will be non-optional (default is optional)
	public static final int M_INDENT_ON_COLUMN = 2; // if bit set, broken fragments will be aligned on current location column (default is to break at current indentation level)
	public static final int	M_INDENT_BY_ONE = 4; // if bit set, broken fragments will be indented one level below current (not using continuation indentation)

	// split modes can be combined either with M_FORCE or M_INDENT_ON_COLUMN

	/** foobar(#fragment1, #fragment2, <ul>
	 *  <li>    #fragment3, #fragment4 </li>
	 * </ul>
	 */
	public static final int M_COMPACT_SPLIT = 16; // fill each line with all possible fragments

	/** foobar(<ul>
	 * <li>    #fragment1, #fragment2,  </li>
	 * <li>     #fragment5, #fragment4, </li>
	 * </ul>
	 */
	public static final int M_COMPACT_FIRST_BREAK_SPLIT = 32; //  compact mode, but will first try to break before first fragment

	/** foobar(<ul>
	 * <li>     #fragment1,  </li>
	 * <li>     #fragment2,  </li>
	 * <li>     #fragment3 </li>
	 * <li>     #fragment4,  </li>
	 * </ul>
	 */
	public static final int M_ONE_PER_LINE_SPLIT = 32+16; // one fragment per line

	/**
	 * foobar(<ul>
	 * <li>     #fragment1,  </li>
	 * <li>        #fragment2,  </li>
	 * <li>        #fragment3 </li>
	 * <li>        #fragment4,  </li>
	 * </ul>
	 */
	public static final int M_NEXT_SHIFTED_SPLIT = 64; // one fragment per line, subsequent are indented further

	/** foobar(#fragment1, <ul>
	 * <li>      #fragment2,  </li>
	 * <li>      #fragment3 </li>
	 * <li>      #fragment4,  </li>
	 * </ul>
	 */
	public static final int M_NEXT_PER_LINE_SPLIT = 64+16; // one per line, except first fragment (if possible)

	//64+32
	//64+32+16

	// mode controlling column alignments
	/**
	 * <table BORDER COLS=4 WIDTH="100%" >
	 * <tr><td>#fragment1A</td>            <td>#fragment2A</td>       <td>#fragment3A</td>  <td>#very-long-fragment4A</td></tr>
	 * <tr><td>#fragment1B</td>            <td>#long-fragment2B</td>  <td>#fragment3B</td>  <td>#fragment4B</td></tr>
	 * <tr><td>#very-long-fragment1C</td>  <td>#fragment2C</td>       <td>#fragment3C</td>  <td>#fragment4C</td></tr>
	 * </table>
	 */
	public static final int M_MULTICOLUMN = 256; // fragments are on same line, but multiple line of fragments will be aligned vertically

	public static final int M_NO_ALIGNMENT = 0;

	public int mode;

	public static final int SPLIT_MASK = M_ONE_PER_LINE_SPLIT | M_NEXT_SHIFTED_SPLIT | M_COMPACT_SPLIT | M_COMPACT_FIRST_BREAK_SPLIT | M_NEXT_PER_LINE_SPLIT;

	// alignment tie-break rules - when split is needed, will decide whether innermost/outermost alignment is to be chosen
	public static final int R_OUTERMOST = 1;
	public static final int R_INNERMOST = 2;
	public int tieBreakRule;
	public int startingColumn = -1;

	// alignment effects on a per fragment basis
	public static final int NONE = 0;
	public static final int BREAK = 1;

	// chunk kind
	public static final int CHUNK_FIELD = 1;
	public static final int CHUNK_METHOD = 2;
	public static final int CHUNK_TYPE = 3;
	public static final int CHUNK_ENUM = 4;

	// location to align and break on.
	public Alignment(int kind, int mode, int tieBreakRule, Scribe scribe, int fragmentCount, int sourceRestart, int continuationIndent){

		Assert.isTrue(kind >=ALLOCATION && kind <=MULTI_CATCH);
		this.kind = kind;
		this.name = NAMES[kind];
		this.location = new Location(scribe, sourceRestart);
		this.mode = mode;
		this.tieBreakRule = tieBreakRule;
		this.fragmentCount = fragmentCount;
		this.scribe = scribe;
		this.originalIndentationLevel = this.scribe.indentationLevel;
		this.wasSplit = false;

		// initialize the break indentation level, using modes and continuationIndentationLevel preference
		final int indentSize = this.scribe.indentationSize;
		int currentColumn = this.location.outputColumn;
		if (currentColumn == 1) {
		    currentColumn = this.location.outputIndentationLevel + 1;
		}

		if ((mode & M_INDENT_ON_COLUMN) != 0) {
			// indent broken fragments at next indentation level, based on current column
			this.breakIndentationLevel = this.scribe.getNextIndentationLevel(currentColumn);
			if (this.breakIndentationLevel == this.location.outputIndentationLevel) {
				this.breakIndentationLevel += (continuationIndent * indentSize);
			}
		} else if ((mode & M_INDENT_BY_ONE) != 0) {
			// indent broken fragments exactly one level deeper than current indentation
			this.breakIndentationLevel = this.location.outputIndentationLevel + indentSize;
		} else {
			this.breakIndentationLevel = this.location.outputIndentationLevel + continuationIndent * indentSize;
		}
		this.shiftBreakIndentationLevel = this.breakIndentationLevel + indentSize;

		this.fragmentIndentations = new int[this.fragmentCount];
		this.fragmentBreaks = new int[this.fragmentCount];

		// check for forced alignments
		if ((this.mode & M_FORCE) != 0) {
			couldBreak();
		}
	}

	public boolean checkChunkStart(int chunk, int startIndex, int sourceRestart) {
		if (this.chunkKind != chunk) {
			this.chunkKind = chunk;

			// when redoing same chunk alignment, must not reset
			if (startIndex != this.chunkStartIndex) {
				this.chunkStartIndex = startIndex;
				this.location.update(this.scribe, sourceRestart);
				reset();
			}
			return true;
		}
		return false;
	}

	public void checkColumn() {
		if ((this.mode & M_MULTICOLUMN) != 0 && this.fragmentCount > 0) {
			int currentIndentation = this.scribe.getNextIndentationLevel(this.scribe.column+(this.scribe.needSpace ? 1 : 0));
			int fragmentIndentation = this.fragmentIndentations[this.fragmentIndex];
			if (currentIndentation > fragmentIndentation) {
				this.fragmentIndentations[this.fragmentIndex] =  currentIndentation;
				if (fragmentIndentation != 0) {
					for (int i = this.fragmentIndex+1; i < this.fragmentCount; i++) {
						this.fragmentIndentations[i] = 0;
					}
					this.needRedoColumnAlignment = true;
				}
			}
			// backtrack only once all fragments got checked
			if (this.needRedoColumnAlignment && this.fragmentIndex == this.fragmentCount-1) { // alignment too small

//				if (CodeFormatterVisitor.DEBUG){
//					System.out.println("ALIGNMENT TOO SMALL");
//					System.out.println(this);
//				}
				this.needRedoColumnAlignment = false;
				int relativeDepth = 0;
				Alignment targetAlignment = this.scribe.memberAlignment;
				while (targetAlignment != null){
					if (targetAlignment == this){
						throw new AlignmentException(AlignmentException.ALIGN_TOO_SMALL, relativeDepth);
					}
					targetAlignment = targetAlignment.enclosing;
					relativeDepth++;
				}
			}
		}
	}

	public int depth() {
		int depth = 0;
		Alignment current = this.enclosing;
		while (current != null) {
			depth++;
			current = current.enclosing;
		}
		return depth;
	}
	
	/**
	 * Returns whether the alignment can be aligned or not.
	 * Only used for message send alignment, it currently blocks its alignment
	 * when it's at the first nesting level of a message send. It allow to save
	 * space on the argument broken line by reducing the number of indentations.
	 */
	public boolean canAlign() {
		if (this.tooLong) {
			return true;
		}
		boolean canAlign = true;
		Alignment enclosingAlignment = this.enclosing;
		while (enclosingAlignment != null) {
			switch (enclosingAlignment.kind) {
				case Alignment.ALLOCATION:
				case Alignment.MESSAGE_ARGUMENTS:
					// message send inside arguments, avoid to align
					if (enclosingAlignment.isWrapped() && 
							(enclosingAlignment.fragmentIndex > 0 || enclosingAlignment.fragmentCount < 2)) {
						return !this.blockAlign;
					}
					if (enclosingAlignment.tooLong) {
						return true;
					}
					canAlign = false;
					break;
				case Alignment.MESSAGE_SEND:
					// multiple depth of message send, hence allow current to align
					switch (this.kind) {
						case Alignment.ALLOCATION:
						case Alignment.MESSAGE_ARGUMENTS:
						case Alignment.MESSAGE_SEND:
							Alignment superEnclosingAlignment = enclosingAlignment.enclosing;
							while (superEnclosingAlignment != null) {
								switch (superEnclosingAlignment.kind) {
									case Alignment.ALLOCATION:
									case Alignment.MESSAGE_ARGUMENTS:
									case Alignment.MESSAGE_SEND:
										// block the alignment of the intermediate message send
										if (this.scribe.nlsTagCounter == 0) {
											enclosingAlignment.blockAlign = true;
										}
										return !this.blockAlign;
								}
								superEnclosingAlignment = superEnclosingAlignment.enclosing;
							}
							break;
					}
					return !this.blockAlign;
			}
			enclosingAlignment = enclosingAlignment.enclosing;
		}
		return canAlign && !this.blockAlign;
	}

	public boolean couldBreak(){
		if (this.fragmentCount == 0) return false;
		int i;
		switch(this.mode & SPLIT_MASK){

			/*  # aligned fragment
			 *  foo(
			 *     #AAAAA, #BBBBB,
			 *     #CCCC);
			 */
			case M_COMPACT_FIRST_BREAK_SPLIT :
				if (this.fragmentBreaks[0] == NONE) {
					this.fragmentBreaks[0] = BREAK;
					this.fragmentIndentations[0] = this.breakIndentationLevel;
					return this.wasSplit = true;
				}
				i = this.fragmentIndex;
				do {
					if (this.fragmentBreaks[i] == NONE) {
						this.fragmentBreaks[i] = BREAK;
						this.fragmentIndentations[i] = this.breakIndentationLevel;
						return this.wasSplit = true;
					}
				} while (--i >= 0);
				break;
			/*  # aligned fragment
			 *  foo(#AAAAA, #BBBBB,
			 *     #CCCC);
			 */
			case M_COMPACT_SPLIT :
				i = this.fragmentIndex;
				do {
					if (this.fragmentBreaks[i] == NONE) {
						this.fragmentBreaks[i] = BREAK;
						this.fragmentIndentations[i] = this.breakIndentationLevel;
						return this.wasSplit = true;
					}
				} while (--i >= 0);
				break;

			/*  # aligned fragment
			 *  foo(
			 *      #AAAAA,
			 *          #BBBBB,
			 *          #CCCC);
			 */
			case M_NEXT_SHIFTED_SPLIT :
				if (this.fragmentBreaks[0] == NONE) {
					this.fragmentBreaks[0] = BREAK;
					this.fragmentIndentations[0] = this.breakIndentationLevel;
					for (i = 1; i < this.fragmentCount; i++){
						this.fragmentBreaks[i] = BREAK;
						this.fragmentIndentations[i] = this.shiftBreakIndentationLevel;
					}
					return this.wasSplit = true;
				}
				break;

			/*  # aligned fragment
			 *  foo(
			 *      #AAAAA,
			 *      #BBBBB,
			 *      #CCCC);
			 */
			case M_ONE_PER_LINE_SPLIT :
				if (this.fragmentBreaks[0] == NONE) {
					for (i = 0; i < this.fragmentCount; i++){
						this.fragmentBreaks[i] = BREAK;
						this.fragmentIndentations[i] = this.breakIndentationLevel;
					}
					return this.wasSplit = true;
				}
				break;
			/*  # aligned fragment
			 *  foo(#AAAAA,
			 *      #BBBBB,
			 *      #CCCC);
			 */
			case M_NEXT_PER_LINE_SPLIT :
				if (this.fragmentBreaks[0] == NONE) {
					if (this.fragmentCount > 1
							&& this.fragmentBreaks[1] == NONE) {
						if ((this.mode & M_INDENT_ON_COLUMN) != 0) {
							this.fragmentIndentations[0] = this.breakIndentationLevel;
						}
						for (i = 1; i < this.fragmentCount; i++) {
							this.fragmentBreaks[i] = BREAK;
							this.fragmentIndentations[i] = this.breakIndentationLevel;
						}
						return this.wasSplit = true;
					}
				}
				break;
		}
		return false; // cannot split better
	}
	
	public boolean isWrapped() {
		if (this.fragmentCount == 0) return false;
		return this.fragmentBreaks[this.fragmentIndex] == BREAK;
	}

	public int wrappedIndex() {
		for (int i = 0, max = this.fragmentCount; i < max; i++) {
			if (this.fragmentBreaks[i] == BREAK) {
				return i;
			}
		}
		return -1;
	}

	// perform alignment effect for current fragment
	public void performFragmentEffect(){
		if (this.fragmentCount == 0) return;
		if ((this.mode & M_MULTICOLUMN) == 0) {
			switch(this.mode & SPLIT_MASK) {
				case Alignment.M_COMPACT_SPLIT :
				case Alignment.M_COMPACT_FIRST_BREAK_SPLIT :
				case Alignment.M_NEXT_PER_LINE_SPLIT :
				case Alignment.M_NEXT_SHIFTED_SPLIT :
				case Alignment.M_ONE_PER_LINE_SPLIT :
					break;
				default:
					return;
			}
		}

		int fragmentIndentation = this.fragmentIndentations[this.fragmentIndex];
		if (this.startingColumn < 0 || (fragmentIndentation+1) < this.startingColumn) {
			if (this.fragmentBreaks[this.fragmentIndex] == BREAK) {
				this.scribe.printNewLine();
			}
			if (fragmentIndentation > 0) {
				this.scribe.indentationLevel = fragmentIndentation;
			}
		}
	}

	// reset fragment indentation/break status
	public void reset() {

		this.wasSplit = false;
		if (this.fragmentCount > 0){
			this.fragmentIndentations = new int[this.fragmentCount];
			this.fragmentBreaks = new int[this.fragmentCount];
		}

		// check for forced alignments
		if ((this.mode & M_FORCE) != 0) {
			couldBreak();
		}
		this.reset = true;
	}

	public void toFragmentsString(StringBuffer buffer){
		// default implementation
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		return toString(buffer, -1);
	}

	public String toString(StringBuffer buffer, int level) {
		
		// Compute the indentation at the given level
		StringBuffer indentation = new StringBuffer();
		for (int i=0; i<level; i++) {
			indentation.append('\t');
		}
		
		// First line is for class and name
		buffer.append(indentation);
		buffer
			.append("<kind: ")	//$NON-NLS-1$
			.append(this.kind)
			.append("> ");	//$NON-NLS-1$
		buffer
			.append("<name: ")	//$NON-NLS-1$
			.append(this.name)
			.append(">\n");	//$NON-NLS-1$
		
		// Line for depth and break indentation
		buffer.append(indentation);
		buffer
			.append("<depth=")	//$NON-NLS-1$
			.append(depth())
			.append("><breakIndent=")	//$NON-NLS-1$
			.append(this.breakIndentationLevel)
			.append("><shiftBreakIndent=")	//$NON-NLS-1$
			.append(this.shiftBreakIndentationLevel)
			.append(">\n"); //$NON-NLS-1$

		// Line to display the location
		buffer.append(indentation);
		buffer
			.append("<location=")	//$NON-NLS-1$
			.append(this.location.toString())
			.append(">\n");	//$NON-NLS-1$

		// Lines for fragments
		buffer
			.append(indentation)
			.append("<fragments:\n");	//$NON-NLS-1$
		for (int i = 0; i < this.fragmentCount; i++){
			buffer
				.append(indentation)
				.append(" - ")	//$NON-NLS-1$
				.append(i)
				.append(": ")	//$NON-NLS-1$
				.append("<break: ")	//$NON-NLS-1$
				.append(this.fragmentBreaks[i] > 0 ? "YES" : "NO")	//$NON-NLS-1$	//$NON-NLS-2$
				.append(">")	//$NON-NLS-1$
				.append("<indent: ")	//$NON-NLS-1$
				.append(this.fragmentIndentations[i])
				.append(">\n");	//$NON-NLS-1$
		}
		buffer
			.append(indentation)
			.append(">\n"); //$NON-NLS-1$
		
		// Display enclosing
		if (this.enclosing != null && level >= 0) {
			buffer
				.append(indentation)
				.append("<enclosing assignement:\n");	//$NON-NLS-1$
			this.enclosing.toString(buffer, level+1);
			buffer
				.append(indentation)
				.append(">\n"); //$NON-NLS-1$
		}
		
		// Return the result
		return buffer.toString();
	}

	public void update() {
		for (int i = 1; i < this.fragmentCount; i++){
		    if (this.fragmentBreaks[i] == BREAK) {
		        this.fragmentIndentations[i] = this.breakIndentationLevel;
		    }
		}
	}

	public boolean wasReset() {
		return this.reset;
	}
}
