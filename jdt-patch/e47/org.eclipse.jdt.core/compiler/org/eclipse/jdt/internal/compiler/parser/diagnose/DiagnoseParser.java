/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser.diagnose;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.ConflictedParser;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.ParserBasicInformation;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

public class DiagnoseParser implements ParserBasicInformation, TerminalTokens, ConflictedParser {
	private static final boolean DEBUG = false;
	private boolean DEBUG_PARSECHECK = false;

	private static final int STACK_INCREMENT = 256;

//	private static final int ERROR_CODE = 1;
	private static final int BEFORE_CODE = 2;
	private static final int INSERTION_CODE = 3;
	private static final int INVALID_CODE = 4;
	private static final int SUBSTITUTION_CODE = 5;
	private static final int DELETION_CODE = 6;
	private static final int MERGE_CODE = 7;
	private static final int MISPLACED_CODE = 8;
	private static final int SCOPE_CODE = 9;
	private static final int SECONDARY_CODE = 10;
	private static final int EOF_CODE = 11;

	private static final int BUFF_UBOUND  = 31;
	private static final int BUFF_SIZE    = 32;
	private static final int MAX_DISTANCE = 30;
	private static final int MIN_DISTANCE = 3;

	private CompilerOptions options;

	private LexStream lexStream;
	private int errorToken;
	private int errorTokenStart;

	private int currentToken = 0;

	private int stackLength;
	private int stateStackTop;
	private int[] stack;

	private int[] locationStack;
	private int[] locationStartStack;

	private int tempStackTop;
	private int[] tempStack;

	private int prevStackTop;
	private int[] prevStack;
	private int nextStackTop;
	private int[] nextStack;

	private int scopeStackTop;
    private int[] scopeIndex;
    private int[] scopePosition;

	int[] list = new int[NUM_SYMBOLS + 1];
	int[] buffer = new int[BUFF_SIZE];

	private static final int NIL = -1;
	int[] stateSeen;

	int statePoolTop;
	StateInfo[] statePool;

	private Parser parser;

	private RecoveryScanner recoveryScanner;

	private boolean reportProblem;

	private static class RepairCandidate {
		public int symbol;
		public int location;

		public RepairCandidate(){
			this.symbol = 0;
			this.location = 0;
		}
	}

	private static class PrimaryRepairInfo {
		public int distance;
		public int misspellIndex;
		public int code;
		public int bufferPosition;
		public int symbol;

		public PrimaryRepairInfo(){
			this.distance = 0;
			this.misspellIndex = 0;
			this.code = 0;
			this.bufferPosition = 0;
			this.symbol = 0;
		}

		public PrimaryRepairInfo copy(){
			PrimaryRepairInfo c = new PrimaryRepairInfo();
			c.distance = this.distance;
			c.misspellIndex = this.misspellIndex;
			c.code = this.code;
			c.bufferPosition = this .bufferPosition;
			c.symbol = this.symbol;
			return c;

		}
	}

	static class SecondaryRepairInfo {
		public int code;
		public int distance;
		public int bufferPosition;
		public int stackPosition;
		public int numDeletions;
		public int symbol;

		boolean recoveryOnNextStack;
	}

	private static class StateInfo {
	    int state;
	    int next;

	    public StateInfo(int state, int next){
	    	this.state = state;
	    	this.next = next;
	    }
	}

	public DiagnoseParser(Parser parser, int firstToken, int start, int end, CompilerOptions options) {
		this(parser, firstToken, start, end, Util.EMPTY_INT_ARRAY, Util.EMPTY_INT_ARRAY, Util.EMPTY_INT_ARRAY, options);
	}

	public DiagnoseParser(Parser parser, int firstToken, int start, int end, int[] intervalStartToSkip, int[] intervalEndToSkip, int[] intervalFlagsToSkip, CompilerOptions options) {
		this.parser = parser;
		this.options = options;
		this.lexStream = new LexStream(BUFF_SIZE, parser.scanner, intervalStartToSkip, intervalEndToSkip, intervalFlagsToSkip, firstToken, start, end);
		this.recoveryScanner = parser.recoveryScanner;
	}

	private ProblemReporter problemReporter(){
		return this.parser.problemReporter();
	}

	private void reallocateStacks()	{
		int old_stack_length = this.stackLength;

		this.stackLength += STACK_INCREMENT;

		if(old_stack_length == 0){
			this.stack = new int[this.stackLength];
			this.locationStack = new int[this.stackLength];
			this.locationStartStack = new int[this.stackLength];
			this.tempStack = new int[this.stackLength];
			this.prevStack = new int[this.stackLength];
			this.nextStack = new int[this.stackLength];
			this.scopeIndex = new int[this.stackLength];
			this.scopePosition = new int[this.stackLength];
		} else {
			System.arraycopy(this.stack, 0, this.stack = new int[this.stackLength], 0, old_stack_length);
			System.arraycopy(this.locationStack, 0, this.locationStack = new int[this.stackLength], 0, old_stack_length);
			System.arraycopy(this.locationStartStack, 0, this.locationStartStack = new int[this.stackLength], 0, old_stack_length);
			System.arraycopy(this.tempStack, 0, this.tempStack = new int[this.stackLength], 0, old_stack_length);
			System.arraycopy(this.prevStack, 0, this.prevStack = new int[this.stackLength], 0, old_stack_length);
			System.arraycopy(this.nextStack, 0, this.nextStack = new int[this.stackLength], 0, old_stack_length);
			System.arraycopy(this.scopeIndex, 0, this.scopeIndex = new int[this.stackLength], 0, old_stack_length);
			System.arraycopy(this.scopePosition, 0, this.scopePosition = new int[this.stackLength], 0, old_stack_length);
		}
		return;
	}


	public void diagnoseParse(boolean record) {
		this.reportProblem = true;
		boolean oldRecord = false;
		if(this.recoveryScanner != null) {
			oldRecord = this.recoveryScanner.record;
			this.recoveryScanner.record = record;
		}
		this.parser.scanner.setActiveParser(this);
		try {
			this.lexStream.reset();

			this.currentToken = this.lexStream.getToken();

			int prev_pos;
			int pos;
			int next_pos;
			int act = START_STATE;

			reallocateStacks();

			//
			// Start parsing
			//
			this.stateStackTop = 0;
			this.stack[this.stateStackTop] = act;

			int tok = this.lexStream.kind(this.currentToken);
			this.locationStack[this.stateStackTop] = this.currentToken;
			this.locationStartStack[this.stateStackTop] = this.lexStream.start(this.currentToken);

			boolean forceRecoveryAfterLBracketMissing = false;
	//		int forceRecoveryToken = -1;

			//
			// Process a terminal
			//
			do {
				//
				// Synchronize state stacks and update the location stack
				//
				prev_pos = -1;
				this.prevStackTop = -1;

				next_pos = -1;
				this.nextStackTop = -1;

				pos = this.stateStackTop;
				this.tempStackTop = this.stateStackTop - 1;
				for (int i = 0; i <= this.stateStackTop; i++)
					this.tempStack[i] = this.stack[i];

				act = Parser.tAction(act, tok);
				//
				// When a reduce action is encountered, we compute all REDUCE
				// and associated goto actions induced by the current token.
				// Eventually, a SHIFT, SHIFT-REDUCE, ACCEPT or ERROR action is
				// computed...
				//
				while (act <= NUM_RULES) {
					do {
						this.tempStackTop -= (Parser.rhs[act]-1);
						act = Parser.ntAction(this.tempStack[this.tempStackTop], Parser.lhs[act]);
					} while(act <= NUM_RULES);
					//
					// ... Update the maximum useful position of the
					// (STATE_)STACK, push goto state into stack, and
					// compute next action on current symbol ...
					//
					if (this.tempStackTop + 1 >= this.stackLength)
						reallocateStacks();
					pos = pos < this.tempStackTop ? pos : this.tempStackTop;
					this.tempStack[this.tempStackTop + 1] = act;
					act = Parser.tAction(act, tok);
				}

				//
				// At this point, we have a shift, shift-reduce, accept or error
				// action.  STACK contains the configuration of the state stack
				// prior to executing any action on curtok. next_stack contains
				// the configuration of the state stack after executing all
				// reduce actions induced by curtok.  The variable pos indicates
				// the highest position in STACK that is still useful after the
				// reductions are executed.
				//
				while(act > ERROR_ACTION || act < ACCEPT_ACTION) { // SHIFT-REDUCE action or SHIFT action ?
					this.nextStackTop = this.tempStackTop + 1;
					for (int i = next_pos + 1; i <= this.nextStackTop; i++)
						this.nextStack[i] = this.tempStack[i];

					for (int i = pos + 1; i <= this.nextStackTop; i++) {
						this.locationStack[i] = this.locationStack[this.stateStackTop];
						this.locationStartStack[i] = this.locationStartStack[this.stateStackTop];
					}

					//
					// If we have a shift-reduce, process it as well as
					// the goto-reduce actions that follow it.
					//
					if (act > ERROR_ACTION) {
						act -= ERROR_ACTION;
						do {
							this.nextStackTop -= (Parser.rhs[act]-1);
							act = Parser.ntAction(this.nextStack[this.nextStackTop], Parser.lhs[act]);
						} while(act <= NUM_RULES);
						pos = pos < this.nextStackTop ? pos : this.nextStackTop;
					}

					if (this.nextStackTop + 1 >= this.stackLength)
						reallocateStacks();

					this.tempStackTop = this.nextStackTop;
					this.nextStack[++this.nextStackTop] = act;
					next_pos = this.nextStackTop;

					//
					// Simulate the parser through the next token without
					// destroying STACK or next_stack.
					//
					this.currentToken = this.lexStream.getToken();
					tok = this.lexStream.kind(this.currentToken);
					act = Parser.tAction(act, tok);
					while(act <= NUM_RULES) {
						//
						// ... Process all goto-reduce actions following
						// reduction, until a goto action is computed ...
						//
						do {
							int lhs_symbol = Parser.lhs[act];
							if(DEBUG) {
								System.out.println(Parser.name[Parser.non_terminal_index[lhs_symbol]]);
							}
							this.tempStackTop -= (Parser.rhs[act]-1);
							act = (this.tempStackTop > next_pos
									   ? this.tempStack[this.tempStackTop]
									   : this.nextStack[this.tempStackTop]);
							act = Parser.ntAction(act, lhs_symbol);
						}   while(act <= NUM_RULES);

						//
						// ... Update the maximum useful position of the
						// (STATE_)STACK, push GOTO state into stack, and
						// compute next action on current symbol ...
						//
						if (this.tempStackTop + 1 >= this.stackLength)
							reallocateStacks();

						next_pos = next_pos < this.tempStackTop ? next_pos : this.tempStackTop;
						this.tempStack[this.tempStackTop + 1] = act;
						act = Parser.tAction(act, tok);
					}

	//				if((tok != TokenNameRBRACE || (forceRecoveryToken != currentToken && (lexStream.flags(currentToken) & LexStream.LBRACE_MISSING) != 0))
	//					&& (lexStream.flags(currentToken) & LexStream.IS_AFTER_JUMP) !=0) {
	//					act = ERROR_ACTION;
	//					if(forceRecoveryToken != currentToken
	//						&& (lexStream.flags(currentToken) & LexStream.LBRACE_MISSING) != 0) {
	//						forceRecoveryAfterLBracketMissing = true;
	//						forceRecoveryToken = currentToken;
	//					}
	//				}

					//
					// No error was detected, Read next token into
					// PREVTOK element, advance CURTOK pointer and
					// update stacks.
					//
					if (act != ERROR_ACTION) {
						this.prevStackTop = this.stateStackTop;
						for (int i = prev_pos + 1; i <= this.prevStackTop; i++)
							this.prevStack[i] = this.stack[i];
						prev_pos = pos;

						this.stateStackTop = this.nextStackTop;
						for (int i = pos + 1; i <= this.stateStackTop; i++)
							this.stack[i] = this.nextStack[i];
						this.locationStack[this.stateStackTop] = this.currentToken;
						this.locationStartStack[this.stateStackTop] = this.lexStream.start(this.currentToken);
						pos = next_pos;
					}
				}

				//
				// At this stage, either we have an ACCEPT or an ERROR
				// action.
				//
				if (act == ERROR_ACTION) {
					//
					// An error was detected.
					//
					RepairCandidate candidate = errorRecovery(this.currentToken, forceRecoveryAfterLBracketMissing);

					forceRecoveryAfterLBracketMissing = false;

					if(this.parser.reportOnlyOneSyntaxError) {
						return;
					}

					if(this.parser.problemReporter().options.maxProblemsPerUnit < this.parser.compilationUnit.compilationResult.problemCount) {
						if(this.recoveryScanner == null || !this.recoveryScanner.record) return;
						this.reportProblem = false;
					}

					act = this.stack[this.stateStackTop];

					//
					// If the recovery was successful on a nonterminal candidate,
					// parse through that candidate and "read" the next token.
					//
					if (candidate.symbol == 0) {
						break;
					} else if (candidate.symbol > NT_OFFSET) {
						int lhs_symbol = candidate.symbol - NT_OFFSET;
						if(DEBUG) {
							System.out.println(Parser.name[Parser.non_terminal_index[lhs_symbol]]);
						}
						act = Parser.ntAction(act, lhs_symbol);
						while(act <= NUM_RULES) {
							this.stateStackTop -= (Parser.rhs[act]-1);
							act = Parser.ntAction(this.stack[this.stateStackTop], Parser.lhs[act]);
						}
						this.stack[++this.stateStackTop] = act;
						this.currentToken = this.lexStream.getToken();
						tok = this.lexStream.kind(this.currentToken);
						this.locationStack[this.stateStackTop] = this.currentToken;
						this.locationStartStack[this.stateStackTop] = this.lexStream.start(this.currentToken);
					} else {
						tok = candidate.symbol;
						this.locationStack[this.stateStackTop] = candidate.location;
						this.locationStartStack[this.stateStackTop] = this.lexStream.start(candidate.location);
					}
				}
			} while (act != ACCEPT_ACTION);
		} finally {
			if(this.recoveryScanner != null) {
				this.recoveryScanner.record = oldRecord;
			}
			this.parser.scanner.setActiveParser(null);
		}
		return;
	}

	private static char[] displayEscapeCharacters(char[] tokenSource, int start, int end) {
		StringBuffer tokenSourceBuffer = new StringBuffer();
		for (int i = 0; i < start; i++) {
			tokenSourceBuffer.append(tokenSource[i]);
		}
		for (int i = start; i < end; i++) {
			char c = tokenSource[i];

			Util.appendEscapedChar(tokenSourceBuffer, c, true);
		}
		for (int i = end; i < tokenSource.length; i++) {
			tokenSourceBuffer.append(tokenSource[i]);
		}
		return tokenSourceBuffer.toString().toCharArray();
	}

	//
//		This routine is invoked when an error is encountered.  It
//	   tries to diagnose the error and recover from it.  If it is
//	   successful, the state stack, the current token and the buffer
//	   are readjusted; i.e., after a successful recovery,
//	   state_stack_top points to the location in the state stack
//	   that contains the state on which to recover; curtok
//	   identifies the symbol on which to recover.
//
//	   Up to three configurations may be available when this routine
//	   is invoked. PREV_STACK may contain the sequence of states
//	   preceding any action on prevtok, STACK always contains the
//	   sequence of states preceding any action on curtok, and
//	   NEXT_STACK may contain the sequence of states preceding any
//	   action on the successor of curtok.
//
	private RepairCandidate errorRecovery(int error_token, boolean forcedError) {
		this.errorToken = error_token;
		this.errorTokenStart = this.lexStream.start(error_token);

		int prevtok = this.lexStream.previous(error_token);
		int prevtokKind = this.lexStream.kind(prevtok);

		if(forcedError) {
			int name_index = Parser.terminal_index[TokenNameLBRACE];

			reportError(INSERTION_CODE, name_index, prevtok, prevtok);

			RepairCandidate candidate = new RepairCandidate();
			candidate.symbol = TokenNameLBRACE;
			candidate.location = error_token;
			this.lexStream.reset(error_token);

			this.stateStackTop = this.nextStackTop;
			for (int j = 0; j <= this.stateStackTop; j++) {
				this.stack[j] = this.nextStack[j];
			}
			this.locationStack[this.stateStackTop] = error_token;
			this.locationStartStack[this.stateStackTop] = this.lexStream.start(error_token);

			return candidate;
		}

		//
		// Try primary phase recoveries. If not successful, try secondary
		// phase recoveries.  If not successful and we are at end of the
		// file, we issue the end-of-file error and quit. Otherwise, ...
		//
		RepairCandidate candidate = primaryPhase(error_token);
		if (candidate.symbol != 0) {
			return candidate;
		}

		candidate = secondaryPhase(error_token);
		if (candidate.symbol != 0) {
			return candidate;
		}

		if (this.lexStream.kind(error_token) == EOFT_SYMBOL) {
			reportError(EOF_CODE,
						Parser.terminal_index[EOFT_SYMBOL],
						prevtok,
						prevtok);
			candidate.symbol = 0;
			candidate.location = error_token;
			return candidate;
		}

		//
		// At this point, primary and (initial attempt at) secondary
		// recovery did not work.  We will now get into "panic mode" and
		// keep trying secondary phase recoveries until we either find
		// a successful recovery or have consumed the remaining input
		// tokens.
		//
		while(this.lexStream.kind(this.buffer[BUFF_UBOUND]) != EOFT_SYMBOL) {
			candidate = secondaryPhase(this.buffer[MAX_DISTANCE - MIN_DISTANCE + 2]);
			if (candidate.symbol != 0) {
				return candidate;
			}
		}

		//
		// We reached the end of the file while panicking. Delete all
		// remaining tokens in the input.
		//
		int i;
		for (i = BUFF_UBOUND; this.lexStream.kind(this.buffer[i]) == EOFT_SYMBOL; i--){/*empty*/}

		reportError(DELETION_CODE,
					Parser.terminal_index[prevtokKind],//Parser.terminal_index[lexStream.kind(prevtok)],
					error_token,
					this.buffer[i]);

		candidate.symbol = 0;
		candidate.location = this.buffer[i];

		return candidate;
	}

//
//	   This function tries primary and scope recovery on each
//	   available configuration.  If a successful recovery is found
//	   and no secondary phase recovery can do better, a diagnosis is
//	   issued, the configuration is updated and the function returns
//	   "true".  Otherwise, it returns "false".
//
	private RepairCandidate primaryPhase(int error_token) {
		PrimaryRepairInfo repair = new PrimaryRepairInfo();
		RepairCandidate candidate = new RepairCandidate();

		//
		// Initialize the buffer.
		//
		int i = (this.nextStackTop >= 0 ? 3 : 2);
		this.buffer[i] = error_token;

		for (int j = i; j > 0; j--)
			this.buffer[j - 1] = this.lexStream.previous(this.buffer[j]);

		for (int k = i + 1; k < BUFF_SIZE; k++)
			this.buffer[k] = this.lexStream.next(this.buffer[k - 1]);

		//
		// If NEXT_STACK_TOP > 0 then the parse was successful on CURTOK
		// and the error was detected on the successor of CURTOK. In
		// that case, first check whether or not primary recovery is
		// possible on next_stack ...
		//
		if (this.nextStackTop >= 0) {
			repair.bufferPosition = 3;
			repair = checkPrimaryDistance(this.nextStack, this.nextStackTop, repair);
		}

		//
		// ... Next, try primary recovery on the current token...
		//
		PrimaryRepairInfo new_repair = repair.copy();

		new_repair.bufferPosition = 2;
		new_repair = checkPrimaryDistance(this.stack, this.stateStackTop, new_repair);
		if (new_repair.distance > repair.distance || new_repair.misspellIndex > repair.misspellIndex) {
			repair = new_repair;
		}

		//
		// Finally, if prev_stack_top >= 0 then try primary recovery on
		// the prev_stack configuration.
		//

		if (this.prevStackTop >= 0) {
			new_repair = repair.copy();
			new_repair.bufferPosition = 1;
			new_repair = checkPrimaryDistance(this.prevStack,this.prevStackTop, new_repair);
			if (new_repair.distance > repair.distance || new_repair.misspellIndex > repair.misspellIndex) {
				repair = new_repair;
			}
		}

		//
		// Before accepting the best primary phase recovery obtained,
		// ensure that we cannot do better with a similar secondary
		// phase recovery.
		//
		if (this.nextStackTop >= 0) {// next_stack available
			if (secondaryCheck(this.nextStack,this.nextStackTop,3,repair.distance)) {
				return candidate;
			}
		}
		else if (secondaryCheck(this.stack, this.stateStackTop, 2, repair.distance)) {
			return candidate;
		}

		//
		// First, adjust distance if the recovery is on the error token;
		// it is important that the adjustment be made here and not at
		// each primary trial to prevent the distance tests from being
		// biased in favor of deferred recoveries which have access to
		// more input tokens...
		//
		repair.distance = repair.distance - repair.bufferPosition + 1;

		//
		// ...Next, adjust the distance if the recovery is a deletion or
		// (some form of) substitution...
		//
		if (repair.code == INVALID_CODE      ||
			repair.code == DELETION_CODE     ||
			repair.code == SUBSTITUTION_CODE ||
			repair.code == MERGE_CODE) {
			 repair.distance--;
		}

		//
		// ... After adjustment, check if the most successful primary
		// recovery can be applied.  If not, continue with more radical
		// recoveries...
		//
		if (repair.distance < MIN_DISTANCE) {
			return candidate;
		}

		//
		// When processing an insertion error, if the token preceeding
		// the error token is not available, we change the repair code
		// into a BEFORE_CODE to instruct the reporting routine that it
		// indicates that the repair symbol should be inserted before
		// the error token.
		//
		if (repair.code == INSERTION_CODE) {
			if (this.buffer[repair.bufferPosition - 1] == 0) {
				repair.code = BEFORE_CODE;
			}
		}

		//
		// Select the proper sequence of states on which to recover,
		// update stack accordingly and call diagnostic routine.
		//
		if (repair.bufferPosition == 1) {
			this.stateStackTop = this.prevStackTop;
			for (int j = 0; j <= this.stateStackTop; j++) {
				this.stack[j] = this.prevStack[j];
			}
		} else if (this.nextStackTop >= 0 && repair.bufferPosition >= 3) {
			this.stateStackTop = this.nextStackTop;
			for (int j = 0; j <= this.stateStackTop; j++) {
				this.stack[j] = this.nextStack[j];
			}
			this.locationStack[this.stateStackTop] = this.buffer[3];
			this.locationStartStack[this.stateStackTop] = this.lexStream.start(this.buffer[3]);
		}

		return primaryDiagnosis(repair);
	}


//
//		   This function checks whether or not a given state has a
//	   candidate, whose string representaion is a merging of the two
//	   tokens at positions buffer_position and buffer_position+1 in
//	   the buffer.  If so, it returns the candidate in question;
//	   otherwise it returns 0.
//
	private int mergeCandidate(int state, int buffer_position) {
		char[] name1 = this.lexStream.name(this.buffer[buffer_position]);
		char[] name2 = this.lexStream.name(this.buffer[buffer_position + 1]);

		int len  = name1.length + name2.length;

		char[] str = CharOperation.concat(name1, name2);

		for (int k = Parser.asi(state); Parser.asr[k] != 0; k++) {
			int l = Parser.terminal_index[Parser.asr[k]];

			if (len == Parser.name[l].length()) {
				char[] name = Parser.name[l].toCharArray();

				if (CharOperation.equals(str, name, false)) {
					return Parser.asr[k];
				}
			}
		}

		return 0;
	}


//
//	   This procedure takes as arguments a parsing configuration
//	   consisting of a state stack (stack and stack_top) and a fixed
//	   number of input tokens (starting at buffer_position) in the
//	   input BUFFER; and some reference arguments: repair_code,
//	   distance, misspell_index, candidate, and stack_position
//	   which it sets based on the best possible recovery that it
//	   finds in the given configuration.  The effectiveness of a
//	   a repair is judged based on two criteria:
//
//		 1) the number of tokens that can be parsed after the repair
//			is applied: distance.
//		 2) how close to perfection is the candidate that is chosen:
//			misspell_index.
//	   When this procedure is entered, distance, misspell_index and
//	   repair_code are assumed to be initialized.
//
	private PrimaryRepairInfo checkPrimaryDistance(int stck[], int stack_top, PrimaryRepairInfo repair) {
		int i, j, k, next_state, max_pos, act, root, symbol, tok;

		//
	    //  First, try scope and manual recovery.
	    //
	    PrimaryRepairInfo scope_repair = scopeTrial(stck, stack_top, repair.copy());
	    if (scope_repair.distance > repair.distance)
	        repair = scope_repair;

		//
		//  Next, try merging the error token with its successor.
		//
	    if(this.buffer[repair.bufferPosition] != 0 && this.buffer[repair.bufferPosition + 1] != 0) {// do not merge the first token
			symbol = mergeCandidate(stck[stack_top], repair.bufferPosition);
			if (symbol != 0) {
				j = parseCheck(stck, stack_top, symbol, repair.bufferPosition+2);
				if ((j > repair.distance) || (j == repair.distance && repair.misspellIndex < 10)) {
					repair.misspellIndex = 10;
					repair.symbol = symbol;
					repair.distance = j;
					repair.code = MERGE_CODE;
				}
			}
	    }

		//
		// Next, try deletion of the error token.
		//
		j = parseCheck(
				stck,
				stack_top,
				this.lexStream.kind(this.buffer[repair.bufferPosition + 1]),
				repair.bufferPosition + 2);
		if (this.lexStream.kind(this.buffer[repair.bufferPosition]) == EOLT_SYMBOL &&
			this.lexStream.afterEol(this.buffer[repair.bufferPosition+1])) {
			 k = 10;
		} else {
			k = 0;
		}
		if (j > repair.distance || (j == repair.distance && k > repair.misspellIndex)) {
			repair.misspellIndex = k;
			repair.code = DELETION_CODE;
			repair.distance = j;
		}

		//
		// Update the error configuration by simulating all reduce and
		// goto actions induced by the error token. Then assign the top
		// most state of the new configuration to next_state.
		//
		next_state = stck[stack_top];
		max_pos = stack_top;
		this.tempStackTop = stack_top - 1;

		tok = this.lexStream.kind(this.buffer[repair.bufferPosition]);
		this.lexStream.reset(this.buffer[repair.bufferPosition + 1]);
		act = Parser.tAction(next_state, tok);
		while(act <= NUM_RULES) {
			do {
				this.tempStackTop -= (Parser.rhs[act]-1);
				symbol = Parser.lhs[act];
				act = (this.tempStackTop > max_pos
									  ? this.tempStack[this.tempStackTop]
									  : stck[this.tempStackTop]);
				act = Parser.ntAction(act, symbol);
			} while(act <= NUM_RULES);
			max_pos = max_pos < this.tempStackTop ? max_pos : this.tempStackTop;
			this.tempStack[this.tempStackTop + 1] = act;
			next_state = act;
			act = Parser.tAction(next_state, tok);
		}

		//
		//  Next, place the list of candidates in proper order.
		//
		root = 0;
		for (i = Parser.asi(next_state); Parser.asr[i] != 0; i++) {
			symbol = Parser.asr[i];
			if (symbol != EOFT_SYMBOL && symbol != ERROR_SYMBOL) {
				if (root == 0) {
					this.list[symbol] = symbol;
				} else {
					this.list[symbol] = this.list[root];
					this.list[root] = symbol;
				}
				root = symbol;
			}
		}

		if (stck[stack_top] != next_state) {
			for (i = Parser.asi(stck[stack_top]); Parser.asr[i] != 0; i++) {
				symbol = Parser.asr[i];
				if (symbol != EOFT_SYMBOL && symbol != ERROR_SYMBOL && this.list[symbol] == 0) {
					if (root == 0) {
						this.list[symbol] = symbol;
					} else {
						this.list[symbol] = this.list[root];
						this.list[root] = symbol;
					}
					root = symbol;
				}
			}
		}

		i = this.list[root];
		this.list[root] = 0;
		root = i;

		//
		//  Next, try insertion for each possible candidate available in
		// the current state, except EOFT and ERROR_SYMBOL.
		//
		symbol = root;
		while(symbol != 0) {
			if (symbol == EOLT_SYMBOL && this.lexStream.afterEol(this.buffer[repair.bufferPosition])) {
				k = 10;
			} else {
				k = 0;
			}
			j = parseCheck(stck, stack_top, symbol, repair.bufferPosition);
			if (j > repair.distance) {
				repair.misspellIndex = k;
				repair.distance = j;
				repair.symbol = symbol;
				repair.code = INSERTION_CODE;
			} else if (j == repair.distance && k > repair.misspellIndex) {
				repair.misspellIndex = k;
				repair.distance = j;
				repair.symbol = symbol;
				repair.code = INSERTION_CODE;
			}

			symbol = this.list[symbol];
		}

		//
		//  Next, Try substitution for each possible candidate available
		// in the current state, except EOFT and ERROR_SYMBOL.
		//
		symbol = root;

		if(this.buffer[repair.bufferPosition] != 0) {// do not replace the first token
			while(symbol != 0) {
				if (symbol == EOLT_SYMBOL && this.lexStream.afterEol(this.buffer[repair.bufferPosition+1])) {
					k = 10;
				} else {
					k = misspell(symbol, this.buffer[repair.bufferPosition]);
				}
				j = parseCheck(stck, stack_top, symbol, repair.bufferPosition+1);
				if (j > repair.distance) {
					repair.misspellIndex = k;
					repair.distance = j;
					repair.symbol = symbol;
					repair.code = SUBSTITUTION_CODE;
				} else if (j == repair.distance && k > repair.misspellIndex) {
					repair.misspellIndex = k;
					repair.symbol = symbol;
					repair.code = SUBSTITUTION_CODE;
				}
				i = symbol;
				symbol = this.list[symbol];
				this.list[i] = 0;                             // reset element
			}
		}


		//
		// Next, we try to insert a nonterminal candidate in front of the
		// error token, or substituting a nonterminal candidate for the
		// error token. Precedence is given to insertion.
		//
		 for (i = Parser.nasi(stck[stack_top]); Parser.nasr[i] != 0; i++) {
			 symbol = Parser.nasr[i] + NT_OFFSET;
			 j = parseCheck(stck, stack_top, symbol, repair.bufferPosition+1);
			 if (j > repair.distance) {
				 repair.misspellIndex = 0;
				 repair.distance = j;
				 repair.symbol = symbol;
				 repair.code = INVALID_CODE;
			 }

			 j = parseCheck(stck, stack_top, symbol, repair.bufferPosition);
			 if ((j > repair.distance) || (j == repair.distance && repair.code == INVALID_CODE)) {
				 repair.misspellIndex = 0;
				 repair.distance = j;
				 repair.symbol = symbol;
				 repair.code = INSERTION_CODE;
			 }
		 }

		return repair;
	}


//
//	   This procedure is invoked to issue a diagnostic message and
//	   adjust the input buffer.  The recovery in question is either
//	   the insertion of one or more scopes, the merging of the error
//	   token with its successor, the deletion of the error token,
//	   the insertion of a single token in front of the error token
//	   or the substitution of another token for the error token.
//
	private RepairCandidate primaryDiagnosis(PrimaryRepairInfo repair) {
		int name_index;

		//
		//  Issue diagnostic.
		//
		int prevtok = this.buffer[repair.bufferPosition - 1];
		int	curtok  = this.buffer[repair.bufferPosition];

		switch(repair.code) {
			case INSERTION_CODE:
			case BEFORE_CODE: {
				if (repair.symbol > NT_OFFSET)
					 name_index = getNtermIndex(this.stack[this.stateStackTop],
												repair.symbol,
												repair.bufferPosition);
				else name_index = getTermIndex(this.stack,
											   this.stateStackTop,
											   repair.symbol,
											   repair.bufferPosition);

				int t = (repair.code == INSERTION_CODE ? prevtok : curtok);
				reportError(repair.code, name_index, t, t);
				break;
			}
			case INVALID_CODE: {
				name_index = getNtermIndex(this.stack[this.stateStackTop],
										   repair.symbol,
										   repair.bufferPosition + 1);
				reportError(repair.code, name_index, curtok, curtok);
				break;
			}
			case SUBSTITUTION_CODE: {
				if (repair.misspellIndex >= 6)
					name_index = Parser.terminal_index[repair.symbol];
				else
				{
					name_index = getTermIndex(this.stack, this.stateStackTop,
											  repair.symbol,
											  repair.bufferPosition + 1);
					if (name_index != Parser.terminal_index[repair.symbol])
						repair.code = INVALID_CODE;
				}
				reportError(repair.code, name_index, curtok, curtok);
				break;
			}
			case MERGE_CODE: {
				reportError(repair.code,
							 Parser.terminal_index[repair.symbol],
							 curtok,
							 this.lexStream.next(curtok));
				break;
			}
			case SCOPE_CODE: {
	            for (int i = 0; i < this.scopeStackTop; i++) {
	                reportError(repair.code,
	                            -this.scopeIndex[i],
	                            this.locationStack[this.scopePosition[i]],
	                            prevtok,
	                            Parser.non_terminal_index[Parser.scope_lhs[this.scopeIndex[i]]]);
	            }

	            repair.symbol = Parser.scope_lhs[this.scopeIndex[this.scopeStackTop]] + NT_OFFSET;
	            this.stateStackTop = this.scopePosition[this.scopeStackTop];
	            reportError(repair.code,
	                        -this.scopeIndex[this.scopeStackTop],
	                        this.locationStack[this.scopePosition[this.scopeStackTop]],
	                        prevtok,
	                        getNtermIndex(this.stack[this.stateStackTop],
	                                      repair.symbol,
	                                      repair.bufferPosition)
	                       );
	            break;
	        }
			default: {// deletion
				reportError(repair.code, Parser.terminal_index[ERROR_SYMBOL], curtok, curtok);
			}
		}

		//
		//  Update buffer.
		//
		RepairCandidate candidate = new RepairCandidate();
		switch (repair.code) {
			case INSERTION_CODE:
			case BEFORE_CODE:
			case SCOPE_CODE: {
				candidate.symbol = repair.symbol;
				candidate.location = this.buffer[repair.bufferPosition];
				this.lexStream.reset(this.buffer[repair.bufferPosition]);
				break;
			}
			case INVALID_CODE:
			case SUBSTITUTION_CODE: {
				candidate.symbol = repair.symbol;
				candidate.location = this.buffer[repair.bufferPosition];
				this.lexStream.reset(this.buffer[repair.bufferPosition + 1]);
				break;
			}
			case MERGE_CODE: {
				candidate.symbol = repair.symbol;
				candidate.location = this.buffer[repair.bufferPosition];
				this.lexStream.reset(this.buffer[repair.bufferPosition + 2]);
				break;
			}
			default: {// deletion
				candidate.location = this.buffer[repair.bufferPosition + 1];
				candidate.symbol =
						  this.lexStream.kind(this.buffer[repair.bufferPosition + 1]);
				this.lexStream.reset(this.buffer[repair.bufferPosition + 2]);
				break;
			}
		}

		return candidate;
	}


//
//	   This function takes as parameter an integer STACK_TOP that
//	   points to a STACK element containing the state on which a
//	   primary recovery will be made; the terminal candidate on which
//	   to recover; and an integer: buffer_position, which points to
//	   the position of the next input token in the BUFFER.  The
//	   parser is simulated until a shift (or shift-reduce) action
//	   is computed on the candidate.  Then we proceed to compute the
//	   the name index of the highest level nonterminal that can
//	   directly or indirectly produce the candidate.
//
	private int getTermIndex(int stck[], int stack_top, int tok, int buffer_position) {
		//
		// Initialize stack index of temp_stack and initialize maximum
		// position of state stack that is still useful.
		//
		int act = stck[stack_top],
			max_pos = stack_top,
			highest_symbol = tok;

		this.tempStackTop = stack_top - 1;

		//
		// Compute all reduce and associated actions induced by the
		// candidate until a SHIFT or SHIFT-REDUCE is computed. ERROR
		// and ACCEPT actions cannot be computed on the candidate in
		// this context, since we know that it is suitable for recovery.
		//
		this.lexStream.reset(this.buffer[buffer_position]);
		act = Parser.tAction(act, tok);
		while(act <= NUM_RULES) {
			//
			// Process all goto-reduce actions following reduction,
			// until a goto action is computed ...
			//
			do {
				this.tempStackTop -= (Parser.rhs[act]-1);
				int lhs_symbol = Parser.lhs[act];
				act = (this.tempStackTop > max_pos
									  ? this.tempStack[this.tempStackTop]
									  : stck[this.tempStackTop]);
				act = Parser.ntAction(act, lhs_symbol);
			} while(act <= NUM_RULES);

			//
			// Compute new maximum useful position of (STATE_)stack,
			// push goto state into the stack, and compute next
			// action on candidate ...
			//
			max_pos = max_pos < this.tempStackTop ? max_pos : this.tempStackTop;
			this.tempStack[this.tempStackTop + 1] = act;
			act = Parser.tAction(act, tok);
		}

		//
		// At this stage, we have simulated all actions induced by the
		// candidate and we are ready to shift or shift-reduce it. First,
		// set tok and next_ptr appropriately and identify the candidate
		// as the initial highest_symbol. If a shift action was computed
		// on the candidate, update the stack and compute the next
		// action. Next, simulate all actions possible on the next input
		// token until we either have to shift it or are about to reduce
		// below the initial starting point in the stack (indicated by
		// max_pos as computed in the previous loop).  At that point,
		// return the highest_symbol computed.
		//
		this.tempStackTop++; // adjust top of stack to reflect last goto
						  // next move is shift or shift-reduce.
		int threshold = this.tempStackTop;

		tok = this.lexStream.kind(this.buffer[buffer_position]);
		this.lexStream.reset(this.buffer[buffer_position + 1]);

		if (act > ERROR_ACTION) {  // shift-reduce on candidate?
			act -= ERROR_ACTION;
		} else {
			this.tempStack[this.tempStackTop + 1] = act;
			act = Parser.tAction(act, tok);
		}

		while(act <= NUM_RULES) {
			//
			// Process all goto-reduce actions following reduction,
			// until a goto action is computed ...
			//
			do {
				this.tempStackTop -= (Parser.rhs[act]-1);

				if (this.tempStackTop < threshold) {
					return (highest_symbol > NT_OFFSET
						 ? Parser.non_terminal_index[highest_symbol - NT_OFFSET]
						 : Parser.terminal_index[highest_symbol]);
				}

				int lhs_symbol = Parser.lhs[act];
				if (this.tempStackTop == threshold)
					highest_symbol = lhs_symbol + NT_OFFSET;
				act = (this.tempStackTop > max_pos
									  ? this.tempStack[this.tempStackTop]
									  : stck[this.tempStackTop]);
				act = Parser.ntAction(act, lhs_symbol);
			} while(act <= NUM_RULES);

			this.tempStack[this.tempStackTop + 1] = act;
			act = Parser.tAction(act, tok);
		}

		return (highest_symbol > NT_OFFSET
							 ? Parser.non_terminal_index[highest_symbol - NT_OFFSET]
							 : Parser.terminal_index[highest_symbol]);
	}

//
//	   This function takes as parameter a starting state number:
//	   start, a nonterminal symbol, A (candidate), and an integer,
//	   buffer_position,  which points to the position of the next
//	   input token in the BUFFER.
//	   It returns the highest level non-terminal B such that
//	   B =>*rm A.  I.e., there does not exists a nonterminal C such
//	   that C =>+rm B. (Recall that for an LALR(k) grammar if
//	   C =>+rm B, it cannot be the case that B =>+rm C)
//
	private int getNtermIndex(int start, int sym, int buffer_position) {
		int highest_symbol = sym - NT_OFFSET,
			tok = this.lexStream.kind(this.buffer[buffer_position]);
		this.lexStream.reset(this.buffer[buffer_position + 1]);

		//
		// Initialize stack index of temp_stack and initialize maximum
		// position of state stack that is still useful.
		//
		this.tempStackTop = 0;
		this.tempStack[this.tempStackTop] = start;

		int act = Parser.ntAction(start, highest_symbol);
		if (act > NUM_RULES) { // goto action?
			this.tempStack[this.tempStackTop + 1] = act;
			act = Parser.tAction(act, tok);
		}

		while(act <= NUM_RULES) {
			//
			// Process all goto-reduce actions following reduction,
			// until a goto action is computed ...
			//
			do {
				this.tempStackTop -= (Parser.rhs[act]-1);
				if (this.tempStackTop < 0)
					return Parser.non_terminal_index[highest_symbol];
				if (this.tempStackTop == 0)
					highest_symbol = Parser.lhs[act];
				act = Parser.ntAction(this.tempStack[this.tempStackTop], Parser.lhs[act]);
			} while(act <= NUM_RULES);
			this.tempStack[this.tempStackTop + 1] = act;
			act = Parser.tAction(act, tok);
		}

		return Parser.non_terminal_index[highest_symbol];
	}

//
//		   Check whether or not there is a high probability that a
//	   given string is a misspelling of another.
//	   Certain singleton symbols (such as ":" and ";") are also
//	   considered to be misspelling of each other.
//
	private int misspell(int sym, int tok) {


		//
		//
		//
		char[] name = Parser.name[Parser.terminal_index[sym]].toCharArray();
		int n = name.length;
		char[] s1 = new char[n + 1];
		for (int k = 0; k < n; k++) {
			char c = name[k];
			s1[k] = ScannerHelper.toLowerCase(c);
		}
		s1[n] = '\0';

		//
		//
		//
		char[] tokenName = this.lexStream.name(tok);
		int len = tokenName.length;
		int m = len < MAX_NAME_LENGTH ? len : MAX_NAME_LENGTH;
		char[] s2 = new char[m + 1];
		for (int k = 0; k < m; k++) {
			char c = tokenName[k];
			s2[k] = ScannerHelper.toLowerCase(c);
		}
		s2[m] = '\0';

		//
		//  Singleton mispellings:
		//
		//  ;      <---->     ,
		//
		//  ;      <---->     :
		//
		//  .      <---->     ,
		//
		//  '      <---->     "
		//
		//
		if (n == 1  &&  m == 1) {
			if ((s1[0] == ';'  &&  s2[0] == ',')  ||
				(s1[0] == ','  &&  s2[0] == ';')  ||
				(s1[0] == ';'  &&  s2[0] == ':')  ||
				(s1[0] == ':'  &&  s2[0] == ';')  ||
				(s1[0] == '.'  &&  s2[0] == ',')  ||
				(s1[0] == ','  &&  s2[0] == '.')  ||
				(s1[0] == '\'' &&  s2[0] == '\"')  ||
				(s1[0] == '\"'  &&  s2[0] == '\'')) {
					return 3;
			}
		}

		//
		// Scan the two strings. Increment "match" count for each match.
		// When a transposition is encountered, increase "match" count
		// by two but count it as an error. When a typo is found, skip
		// it and count it as an error. Otherwise we have a mismatch; if
		// one of the strings is longer, increment its index, otherwise,
		// increment both indices and continue.
		//
		// This algorithm is an adaptation of a boolean misspelling
		// algorithm proposed by Juergen Uhl.
		//
		int count = 0;
		int prefix_length = 0;
		int num_errors = 0;

		int i = 0;
		int j = 0;
		while ((i < n)  &&  (j < m)) {
			if (s1[i] == s2[j]) {
				count++;
				i++;
				j++;
				if (num_errors == 0) {
					prefix_length++;
				}
			} else if (s1[i+1] == s2[j]  &&  s1[i] == s2[j+1]) {
				count += 2;
				i += 2;
				j += 2;
				num_errors++;
			} else if (s1[i+1] == s2[j+1]) {
				i++;
				j++;
				num_errors++;
			} else {
				if ((n - i) > (m - j)) {
					 i++;
				} else if ((m - j) > (n - i)) {
					 j++;
				} else {
					i++;
					j++;
				}
				num_errors++;
			}
		}

		if (i < n  ||  j < m)
			num_errors++;

		if (num_errors > ((n < m ? n : m) / 6 + 1))
			 count = prefix_length;

		return(count * 10 / ((n < len ? len : n) + num_errors));
	}

	private PrimaryRepairInfo scopeTrial(int stck[], int stack_top, PrimaryRepairInfo repair) {
	    this.stateSeen = new int[this.stackLength];
	    for (int i = 0; i < this.stackLength; i++)
	        this.stateSeen[i] = NIL;

	    this.statePoolTop = 0;
	    this.statePool = new StateInfo[this.stackLength];

	    scopeTrialCheck(stck, stack_top, repair, 0);

	    this.stateSeen = null;
	    this.statePoolTop = 0;

	    repair.code = SCOPE_CODE;
	    repair.misspellIndex = 10;

	    return repair;
	}

	private void scopeTrialCheck(int stck[], int stack_top, PrimaryRepairInfo repair, int indx) {
		if(indx > 20) return; // avoid too much recursive call to improve performance

		int act = stck[stack_top];

	    for (int i = this.stateSeen[stack_top]; i != NIL; i = this.statePool[i].next) {
	        if (this.statePool[i].state == act) return;
	    }

	    int old_state_pool_top = this.statePoolTop++;
	    if(this.statePoolTop >= this.statePool.length) {
	    	System.arraycopy(this.statePool, 0, this.statePool = new StateInfo[this.statePoolTop * 2], 0, this.statePoolTop);
	    }

	    this.statePool[old_state_pool_top] = new StateInfo(act, this.stateSeen[stack_top]);
	    this.stateSeen[stack_top] = old_state_pool_top;

	    next : for (int i = 0; i < SCOPE_SIZE; i++) {
	        //
	        // Use the scope lookahead symbol to force all reductions
	        // inducible by that symbol.
	        //
	        act = stck[stack_top];
	        this.tempStackTop = stack_top - 1;
	        int max_pos = stack_top;
	        int tok = Parser.scope_la[i];
	        this.lexStream.reset(this.buffer[repair.bufferPosition]);
	        act = Parser.tAction(act, tok);
	        while(act <= NUM_RULES) {
	            //
	            // ... Process all goto-reduce actions following
	            // reduction, until a goto action is computed ...
	            //
	            do  {
	                this.tempStackTop -= (Parser.rhs[act]-1);
	                int lhs_symbol = Parser.lhs[act];
	                act =  (this.tempStackTop > max_pos
	                            ?  this.tempStack[this.tempStackTop]
	                            :  stck[this.tempStackTop]);
	                act = Parser.ntAction(act, lhs_symbol);
	            }  while(act <= NUM_RULES);
	            if (this.tempStackTop + 1 >= this.stackLength)
	                return;
	            max_pos = max_pos < this.tempStackTop ? max_pos : this.tempStackTop;
	            this.tempStack[this.tempStackTop + 1] = act;
	            act = Parser.tAction(act, tok);
	        }

	        //
	        // If the lookahead symbol is parsable, then we check
	        // whether or not we have a match between the scope
	        // prefix and the transition symbols corresponding to
	        // the states on top of the stack.
	        //
	        if (act != ERROR_ACTION) {
	        	int j, k;
	            k = Parser.scope_prefix[i];
	            for (j = this.tempStackTop + 1;
	                 j >= (max_pos + 1) &&
	                 Parser.in_symbol(this.tempStack[j]) == Parser.scope_rhs[k]; j--) {
	                 k++;
	            }
	            if (j == max_pos) {
	                for (j = max_pos;
	                     j >= 1 && Parser.in_symbol(stck[j]) == Parser.scope_rhs[k];
	                     j--) {
	                    k++;
	                }
	            }
	            //
	            // If the prefix matches, check whether the state
	            // newly exposed on top of the stack, (after the
	            // corresponding prefix states are popped from the
	            // stack), is in the set of "source states" for the
	            // scope in question and that it is at a position
	            // below the threshold indicated by MARKED_POS.
	            //
	            int marked_pos = (max_pos < stack_top ? max_pos + 1 : stack_top);
	            if (Parser.scope_rhs[k] == 0 && j < marked_pos) { // match?
	                int stack_position = j;
	                for (j = Parser.scope_state_set[i];
	                     stck[stack_position] != Parser.scope_state[j] &&
	                     Parser.scope_state[j] != 0;
	                     j++){/*empty*/}
	                //
	                // If the top state is valid for scope recovery,
	                // the left-hand side of the scope is used as
	                // starting symbol and we calculate how far the
	                // parser can advance within the forward context
	                // after parsing the left-hand symbol.
	                //
	                if (Parser.scope_state[j] != 0) {     // state was found
	                    int previous_distance = repair.distance;
	                    int distance = parseCheck(stck,
	                                          stack_position,
	                                          Parser.scope_lhs[i]+NT_OFFSET,
	                                          repair.bufferPosition);
	                    //
	                    // if the recovery is not successful, we
	                    // update the stack with all actions induced
	                    // by the left-hand symbol, and recursively
	                    // call SCOPE_TRIAL_CHECK to try again.
	                    // Otherwise, the recovery is successful. If
	                    // the new distance is greater than the
	                    // initial SCOPE_DISTANCE, we update
	                    // SCOPE_DISTANCE and set scope_stack_top to INDX
	                    // to indicate the number of scopes that are
	                    // to be applied for a succesful  recovery.
	                    // NOTE that this procedure cannot get into
	                    // an infinite loop, since each prefix match
	                    // is guaranteed to take us to a lower point
	                    // within the stack.
	                    //
	                    if ((distance - repair.bufferPosition + 1) < MIN_DISTANCE) {
	                        int top = stack_position;
	                        act = Parser.ntAction(stck[top], Parser.scope_lhs[i]);
	                        while(act <= NUM_RULES) {
	                        	if(Parser.rules_compliance[act] > this.options.sourceLevel) {
								 	continue next;
								}
	                            top -= (Parser.rhs[act]-1);
	                            act = Parser.ntAction(stck[top], Parser.lhs[act]);
	                        }
	                        top++;

	                        j = act;
	                        act = stck[top];  // save
	                        stck[top] = j;    // swap
	                        scopeTrialCheck(stck, top, repair, indx+1);
	                        stck[top] = act; // restore
	                    } else if (distance > repair.distance) {
	                        this.scopeStackTop = indx;
	                        repair.distance = distance;
	                    }

	                    if (this.lexStream.kind(this.buffer[repair.bufferPosition]) == EOFT_SYMBOL &&
	                        repair.distance == previous_distance) {
	                        this.scopeStackTop = indx;
	                        repair.distance = MAX_DISTANCE;
	                    }

	                    //
	                    // If this scope recovery has beaten the
	                    // previous distance, then we have found a
	                    // better recovery (or this recovery is one
	                    // of a list of scope recoveries). Record
	                    // its information at the proper location
	                    // (INDX) in SCOPE_INDEX and SCOPE_STACK.
	                    //
	                    if (repair.distance > previous_distance) {
	                        this.scopeIndex[indx] = i;
	                        this.scopePosition[indx] = stack_position;
	                        return;
	                    }
	                }
	            }
	        }
	    }
	}
//
//	   This function computes the ParseCheck distance for the best
//	   possible secondary recovery for a given configuration that
//	   either deletes none or only one symbol in the forward context.
//	   If the recovery found is more effective than the best primary
//	   recovery previously computed, then the function returns true.
//	   Only misplacement, scope and manual recoveries are attempted;
//	   simple insertion or substitution of a nonterminal are tried
//	   in CHECK_PRIMARY_DISTANCE as part of primary recovery.
//
	private boolean secondaryCheck(int stck[], int stack_top, int buffer_position, int distance) {
		int top, j;

		for (top = stack_top - 1; top >= 0; top--) {
			j = parseCheck(stck, top,
						   this.lexStream.kind(this.buffer[buffer_position]),
						   buffer_position + 1);
			if (((j - buffer_position + 1) > MIN_DISTANCE) && (j > distance))
				return true;
		}

		PrimaryRepairInfo repair = new PrimaryRepairInfo();
	    repair.bufferPosition = buffer_position + 1;
	    repair.distance = distance;
	    repair = scopeTrial(stck, stack_top, repair);
	    if ((repair.distance - buffer_position) > MIN_DISTANCE && repair.distance > distance)
	         return true;
		return false;
	}


//
//	   Secondary_phase is a boolean function that checks whether or
//	   not some form of secondary recovery is applicable to one of
//	   the error configurations. First, if "next_stack" is available,
//	   misplacement and secondary recoveries are attempted on it.
//	   Then, in any case, these recoveries are attempted on "stack".
//	   If a successful recovery is found, a diagnosis is issued, the
//	   configuration is updated and the function returns "true".
//	   Otherwise, the function returns false.
//
	private RepairCandidate secondaryPhase(int error_token) {
		SecondaryRepairInfo repair = new SecondaryRepairInfo();
		SecondaryRepairInfo misplaced = new SecondaryRepairInfo();

		RepairCandidate candidate = new RepairCandidate();

		int i, j, k, top;
		int	next_last_index = 0;
		int	last_index;

		candidate.symbol = 0;

		repair.code = 0;
		repair.distance = 0;
		repair.recoveryOnNextStack = false;

		misplaced.distance = 0;
		misplaced.recoveryOnNextStack = false;

		//
		// If the next_stack is available, try misplaced and secondary
		// recovery on it first.
		//
		if (this.nextStackTop >= 0) {
			int  save_location;

			this.buffer[2] = error_token;
			this.buffer[1] = this.lexStream.previous(this.buffer[2]);
			this.buffer[0] = this.lexStream.previous(this.buffer[1]);

			for (k = 3; k < BUFF_UBOUND; k++)
				this.buffer[k] = this.lexStream.next(this.buffer[k - 1]);

			this.buffer[BUFF_UBOUND] = this.lexStream.badtoken();// elmt not available

			//
			// If we are at the end of the input stream, compute the
			// index position of the first EOFT symbol (last useful
			// index).
			//
			for (next_last_index = MAX_DISTANCE - 1;
				 next_last_index >= 1 &&
				 this.lexStream.kind(this.buffer[next_last_index]) == EOFT_SYMBOL;
				 next_last_index--){/*empty*/}
			next_last_index = next_last_index + 1;

			save_location = this.locationStack[this.nextStackTop];
			int save_location_start = this.locationStartStack[this.nextStackTop];
			this.locationStack[this.nextStackTop] = this.buffer[2];
			this.locationStartStack[this.nextStackTop] = this.lexStream.start(this.buffer[2]);
			misplaced.numDeletions = this.nextStackTop;
			misplaced = misplacementRecovery(this.nextStack, this.nextStackTop,
											 next_last_index,
											 misplaced, true);
			if (misplaced.recoveryOnNextStack)
				misplaced.distance++;

			repair.numDeletions = this.nextStackTop + BUFF_UBOUND;
			repair = secondaryRecovery(this.nextStack, this.nextStackTop,
									   next_last_index,
									   repair, true);
			if (repair.recoveryOnNextStack)
				repair.distance++;

			this.locationStack[this.nextStackTop] = save_location;
			this.locationStartStack[this.nextStackTop] = save_location_start;
		} else {            // next_stack not available, initialize ...
			misplaced.numDeletions = this.stateStackTop;
			repair.numDeletions = this.stateStackTop + BUFF_UBOUND;
		}

		//
		// Try secondary recovery on the "stack" configuration.
		//
		this.buffer[3] = error_token;

		this.buffer[2] = this.lexStream.previous(this.buffer[3]);
		this.buffer[1] = this.lexStream.previous(this.buffer[2]);
		this.buffer[0] = this.lexStream.previous(this.buffer[1]);

		for (k = 4; k < BUFF_SIZE; k++)
			this.buffer[k] = this.lexStream.next(this.buffer[k - 1]);

		for (last_index = MAX_DISTANCE - 1;
			 last_index >= 1 && this.lexStream.kind(this.buffer[last_index]) == EOFT_SYMBOL;
			 last_index--){/*empty*/}
		last_index++;

		misplaced = misplacementRecovery(this.stack, this.stateStackTop,
										 last_index,
										 misplaced, false);

		repair = secondaryRecovery(this.stack, this.stateStackTop,
								   last_index, repair, false);

		//
		// If a successful misplaced recovery was found, compare it with
		// the most successful secondary recovery.  If the misplaced
		// recovery either deletes fewer symbols or parse-checks further
		// then it is chosen.
		//
		if (misplaced.distance > MIN_DISTANCE) {
			if (misplaced.numDeletions <= repair.numDeletions ||
			   (misplaced.distance - misplaced.numDeletions) >=
			   (repair.distance - repair.numDeletions)) {
				repair.code = MISPLACED_CODE;
				repair.stackPosition = misplaced.stackPosition;
				repair.bufferPosition = 2;
				repair.numDeletions = misplaced.numDeletions;
				repair.distance = misplaced.distance;
				repair.recoveryOnNextStack = misplaced.recoveryOnNextStack;
			}
		}

		//
		// If the successful recovery was on next_stack, update: stack,
		// buffer, location_stack and last_index.
		//
		if (repair.recoveryOnNextStack) {
			this.stateStackTop = this.nextStackTop;
			for (i = 0; i <= this.stateStackTop; i++)
				this.stack[i] = this.nextStack[i];

			this.buffer[2] = error_token;
			this.buffer[1] = this.lexStream.previous(this.buffer[2]);
			this.buffer[0] = this.lexStream.previous(this.buffer[1]);

			for (k = 3; k < BUFF_UBOUND; k++)
				this.buffer[k] = this.lexStream.next(this.buffer[k - 1]);

			this.buffer[BUFF_UBOUND] = this.lexStream.badtoken();// elmt not available

			this.locationStack[this.nextStackTop] = this.buffer[2];
			this.locationStartStack[this.nextStackTop] = this.lexStream.start(this.buffer[2]);
			last_index = next_last_index;
		}

	    //
	    // Next, try scope recoveries after deletion of one, two, three,
	    // four ... buffer_position tokens from the input stream.
	    //
	    if (repair.code == SECONDARY_CODE || repair.code == DELETION_CODE) {
	        PrimaryRepairInfo scope_repair = new PrimaryRepairInfo();

	        scope_repair.distance = 0;
	        for (scope_repair.bufferPosition = 2;
	             scope_repair.bufferPosition <= repair.bufferPosition &&
	             repair.code != SCOPE_CODE; scope_repair.bufferPosition++) {
	            scope_repair = scopeTrial(this.stack, this.stateStackTop, scope_repair);
	            j = (scope_repair.distance == MAX_DISTANCE
	                                        ? last_index
	                                        : scope_repair.distance);
	            k = scope_repair.bufferPosition - 1;
	            if ((j - k) > MIN_DISTANCE && (j - k) > (repair.distance - repair.numDeletions)) {
	                repair.code = SCOPE_CODE;
	                i = this.scopeIndex[this.scopeStackTop];       // upper bound
	                repair.symbol = Parser.scope_lhs[i] + NT_OFFSET;
	                repair.stackPosition = this.stateStackTop;
	                repair.bufferPosition = scope_repair.bufferPosition;
	            }
	        }
	    }

	    //
	    // If no successful recovery is found and we have reached the
	    // end of the file, check whether or not scope recovery is
	    // applicable at the end of the file after discarding some
	    // states.
	    //
	    if (repair.code == 0 && this.lexStream.kind(this.buffer[last_index]) == EOFT_SYMBOL) {
	        PrimaryRepairInfo scope_repair = new PrimaryRepairInfo();

	        scope_repair.bufferPosition = last_index;
	        scope_repair.distance = 0;
	        for (top = this.stateStackTop;
	             top >= 0 && repair.code == 0; top--)
	        {
	            scope_repair = scopeTrial(this.stack, top, scope_repair);
	            if (scope_repair.distance > 0)
	            {
	                repair.code = SCOPE_CODE;
	                i = this.scopeIndex[this.scopeStackTop];    // upper bound
	                repair.symbol = Parser.scope_lhs[i] + NT_OFFSET;
	                repair.stackPosition = top;
	                repair.bufferPosition = scope_repair.bufferPosition;
	            }
	        }
	    }

		//
		// If a successful repair was not found, quit!  Otherwise, issue
		// diagnosis and adjust configuration...
		//
		if (repair.code == 0)
			return candidate;

		secondaryDiagnosis(repair);

		//
		// Update buffer based on number of elements that are deleted.
		//
		switch(repair.code) {
			case MISPLACED_CODE:
				 candidate.location = this.buffer[2];
				 candidate.symbol = this.lexStream.kind(this.buffer[2]);
				 this.lexStream.reset(this.lexStream.next(this.buffer[2]));

				 break;

			case DELETION_CODE:
				 candidate.location = this.buffer[repair.bufferPosition];
				 candidate.symbol =
						   this.lexStream.kind(this.buffer[repair.bufferPosition]);
				 this.lexStream.reset(this.lexStream.next(this.buffer[repair.bufferPosition]));

				 break;

		default: // SCOPE_CODE || SECONDARY_CODE
				 candidate.symbol = repair.symbol;
				 candidate.location = this.buffer[repair.bufferPosition];
				 this.lexStream.reset(this.buffer[repair.bufferPosition]);

				 break;
		}

		return candidate;
	}


//
//	   This boolean function checks whether or not a given
//	   configuration yields a better misplacement recovery than
//	   the best misplacement recovery computed previously.
//
	private SecondaryRepairInfo misplacementRecovery(int stck[], int stack_top, int last_index, SecondaryRepairInfo repair, boolean stack_flag) {
		int  previous_loc = this.buffer[2];
		int stack_deletions = 0;

		for (int top = stack_top - 1; top >= 0; top--) {
			if (this.locationStack[top] < previous_loc) {
				stack_deletions++;
			}
			previous_loc = this.locationStack[top];

			int j = parseCheck(stck, top, this.lexStream.kind(this.buffer[2]), 3);
			if (j == MAX_DISTANCE) {
				 j = last_index;
			}
			if ((j > MIN_DISTANCE) && (j - stack_deletions) > (repair.distance - repair.numDeletions)) {
				repair.stackPosition = top;
				repair.distance = j;
				repair.numDeletions = stack_deletions;
				repair.recoveryOnNextStack = stack_flag;
			}
		}

		return repair;
	}


//
//	   This boolean function checks whether or not a given
//	   configuration yields a better secondary recovery than the
//	   best misplacement recovery computed previously.
//
	private SecondaryRepairInfo secondaryRecovery(int stck[],int stack_top, int last_index, SecondaryRepairInfo repair, boolean stack_flag) {
		int previous_loc;
		int stack_deletions = 0;

		previous_loc = this.buffer[2];
		for (int top = stack_top; top >= 0 && repair.numDeletions >= stack_deletions; top--) {
			if (this.locationStack[top] < previous_loc) {
				stack_deletions++;
			}
			previous_loc = this.locationStack[top];

			for (int i = 2;
				 i <= (last_index - MIN_DISTANCE + 1) &&
				 (repair.numDeletions >= (stack_deletions + i - 1)); i++) {
				int j = parseCheck(stck, top, this.lexStream.kind(this.buffer[i]), i + 1);

				if (j == MAX_DISTANCE) {
					 j = last_index;
				}
				if ((j - i + 1) > MIN_DISTANCE) {
					int k = stack_deletions + i - 1;
					if ((k < repair.numDeletions) ||
						(j - k) > (repair.distance - repair.numDeletions) ||
						((repair.code == SECONDARY_CODE) && (j - k) == (repair.distance - repair.numDeletions))) {
						repair.code = DELETION_CODE;
						repair.distance = j;
						repair.stackPosition = top;
						repair.bufferPosition = i;
						repair.numDeletions = k;
						repair.recoveryOnNextStack = stack_flag;
					}
				}

				for (int l = Parser.nasi(stck[top]); l >= 0 && Parser.nasr[l] != 0; l++) {
					int symbol = Parser.nasr[l] + NT_OFFSET;
					j = parseCheck(stck, top, symbol, i);
					if (j == MAX_DISTANCE) {
						 j = last_index;
					}
					if ((j - i + 1) > MIN_DISTANCE) {
						int k = stack_deletions + i - 1;
						if (k < repair.numDeletions || (j - k) > (repair.distance - repair.numDeletions)) {
							repair.code = SECONDARY_CODE;
							repair.symbol = symbol;
							repair.distance = j;
							repair.stackPosition = top;
							repair.bufferPosition = i;
							repair.numDeletions = k;
							repair.recoveryOnNextStack = stack_flag;
						}
					}
				}
			}
		}

		return repair;
	}


//
//	   This procedure is invoked to issue a secondary diagnosis and
//	   adjust the input buffer.  The recovery in question is either
//	   an automatic scope recovery, a manual scope recovery, a
//	   secondary substitution or a secondary deletion.
//
	private void secondaryDiagnosis(SecondaryRepairInfo repair) {
		switch(repair.code) {
			case SCOPE_CODE: {
	            if (repair.stackPosition < this.stateStackTop) {
	                reportError(DELETION_CODE,
	                            Parser.terminal_index[ERROR_SYMBOL],
	                            this.locationStack[repair.stackPosition],
	                            this.buffer[1]);
	            }
	            for (int i = 0; i < this.scopeStackTop; i++) {
	                reportError(SCOPE_CODE,
	                            -this.scopeIndex[i],
	                            this.locationStack[this.scopePosition[i]],
	                            this.buffer[1],
	                            Parser.non_terminal_index[Parser.scope_lhs[this.scopeIndex[i]]]);
	            }

	            repair.symbol = Parser.scope_lhs[this.scopeIndex[this.scopeStackTop]] + NT_OFFSET;
	            this.stateStackTop = this.scopePosition[this.scopeStackTop];
	            reportError(SCOPE_CODE,
	                        -this.scopeIndex[this.scopeStackTop],
	                        this.locationStack[this.scopePosition[this.scopeStackTop]],
	                        this.buffer[1],
	                        getNtermIndex(this.stack[this.stateStackTop],
	                                      repair.symbol,
	                                      repair.bufferPosition)
	                       );
	            break;
	        }
			default: {
				reportError(repair.code,
							(repair.code == SECONDARY_CODE
										  ? getNtermIndex(this.stack[repair.stackPosition],
														  repair.symbol,
														  repair.bufferPosition)
										  : Parser.terminal_index[ERROR_SYMBOL]),
							this.locationStack[repair.stackPosition],
							this.buffer[repair.bufferPosition - 1]);
				this.stateStackTop = repair.stackPosition;
			}
		}
	}




//
//	   Try to parse until first_token and all tokens in BUFFER have
//	   been consumed, or an error is encountered. Return the number
//	   of tokens that were expended before the parse blocked.
//
	private int parseCheck(int stck[], int stack_top, int first_token, int buffer_position) {
		int max_pos;
		int indx;
		int ct;
		int act;

		//
		// Initialize pointer for temp_stack and initialize maximum
		// position of state stack that is still useful.
		//
		act = stck[stack_top];
		if (first_token > NT_OFFSET) {
			this.tempStackTop = stack_top;
			if(this.DEBUG_PARSECHECK) {
				System.out.println(this.tempStackTop);
			}
			max_pos = stack_top;
			indx = buffer_position;
			ct = this.lexStream.kind(this.buffer[indx]);
			this.lexStream.reset(this.lexStream.next(this.buffer[indx]));
			int lhs_symbol = first_token - NT_OFFSET;
			act = Parser.ntAction(act, lhs_symbol);
			if (act <= NUM_RULES) {
				// same loop as 'process_non_terminal'
				do {
					this.tempStackTop -= (Parser.rhs[act]-1);

					if(this.DEBUG_PARSECHECK) {
						System.out.print(this.tempStackTop);
						System.out.print(" ("); //$NON-NLS-1$
						System.out.print(-(Parser.rhs[act]-1));
						System.out.print(") [max:"); //$NON-NLS-1$
						System.out.print(max_pos);
						System.out.print("]\tprocess_non_terminal\t"); //$NON-NLS-1$
						System.out.print(act);
						System.out.print("\t"); //$NON-NLS-1$
						System.out.print(Parser.name[Parser.non_terminal_index[Parser.lhs[act]]]);
						System.out.println();
					}

					if(Parser.rules_compliance[act] > this.options.sourceLevel) {
					 	return 0;
					}
					lhs_symbol = Parser.lhs[act];
					act = (this.tempStackTop > max_pos
										  ? this.tempStack[this.tempStackTop]
										  : stck[this.tempStackTop]);
					act = Parser.ntAction(act, lhs_symbol);
				} while(act <= NUM_RULES);

				max_pos = max_pos < this.tempStackTop ? max_pos : this.tempStackTop;
			}
		} else {
			this.tempStackTop = stack_top - 1;

			if(this.DEBUG_PARSECHECK) {
				System.out.println(this.tempStackTop);
			}

			max_pos = this.tempStackTop;
			indx = buffer_position - 1;
			ct = first_token;
			this.lexStream.reset(this.buffer[buffer_position]);
		}

		process_terminal: for (;;) {
			if(this.DEBUG_PARSECHECK) {
				System.out.print(this.tempStackTop + 1);
				System.out.print(" (+1) [max:"); //$NON-NLS-1$
				System.out.print(max_pos);
				System.out.print("]\tprocess_terminal    \t"); //$NON-NLS-1$
				System.out.print(ct);
				System.out.print("\t"); //$NON-NLS-1$
				System.out.print(Parser.name[Parser.terminal_index[ct]]);
				System.out.println();
			}

			if (++this.tempStackTop >= this.stackLength)  // Stack overflow!!!
				return indx;
			this.tempStack[this.tempStackTop] = act;

			act = Parser.tAction(act, ct);

			if (act <= NUM_RULES) {               // reduce action
				this.tempStackTop--;

				if(this.DEBUG_PARSECHECK) {
					System.out.print(this.tempStackTop);
					System.out.print(" (-1) [max:"); //$NON-NLS-1$
					System.out.print(max_pos);
					System.out.print("]\treduce"); //$NON-NLS-1$
					System.out.println();
				}
			} else if (act < ACCEPT_ACTION ||     // shift action
					 act > ERROR_ACTION) {        // shift-reduce action
				if (indx == MAX_DISTANCE)
					return indx;
				indx++;
				ct = this.lexStream.kind(this.buffer[indx]);
				this.lexStream.reset(this.lexStream.next(this.buffer[indx]));
				if (act > ERROR_ACTION) {
					act -= ERROR_ACTION;

					if(this.DEBUG_PARSECHECK) {
						System.out.print(this.tempStackTop);
						System.out.print("\tshift reduce"); //$NON-NLS-1$
						System.out.println();
					}
				} else {
					if(this.DEBUG_PARSECHECK) {
						System.out.println("\tshift"); //$NON-NLS-1$
					}
					continue process_terminal;
				}
			} else if (act == ACCEPT_ACTION) {           // accept action
				 return MAX_DISTANCE;
			} else {
				return indx;                         // error action
			}

			// same loop as first token initialization
			// process_non_terminal:
			do {
				this.tempStackTop -= (Parser.rhs[act]-1);

				if(this.DEBUG_PARSECHECK) {
					System.out.print(this.tempStackTop);
					System.out.print(" ("); //$NON-NLS-1$
					System.out.print(-(Parser.rhs[act]-1));
					System.out.print(") [max:"); //$NON-NLS-1$
					System.out.print(max_pos);
					System.out.print("]\tprocess_non_terminal\t"); //$NON-NLS-1$
					System.out.print(act);
					System.out.print("\t"); //$NON-NLS-1$
					System.out.print(Parser.name[Parser.non_terminal_index[Parser.lhs[act]]]);
					System.out.println();
				}

				if(act <= NUM_RULES) {
					if(Parser.rules_compliance[act] > this.options.sourceLevel) {
					 	return 0;
					}
				}
				int lhs_symbol = Parser.lhs[act];
				act = (this.tempStackTop > max_pos
									  ? this.tempStack[this.tempStackTop]
									  : stck[this.tempStackTop]);
				act = Parser.ntAction(act, lhs_symbol);
			} while(act <= NUM_RULES);

			max_pos = max_pos < this.tempStackTop ? max_pos : this.tempStackTop;
		} // process_terminal;
	}
	private void reportError(int msgCode, int nameIndex, int leftToken, int rightToken) {
		reportError(msgCode, nameIndex, leftToken, rightToken, 0);
	}

	private void reportError(int msgCode, int nameIndex, int leftToken, int rightToken, int scopeNameIndex) {
		int lToken = (leftToken > rightToken ? rightToken : leftToken);

		if (lToken < rightToken) {
			reportSecondaryError(msgCode, nameIndex, lToken, rightToken, scopeNameIndex);
		} else {
			reportPrimaryError(msgCode, nameIndex, rightToken, scopeNameIndex);
		}
	}

	private void reportPrimaryError(int msgCode, int nameIndex, int token, int scopeNameIndex) {
		String name;
		if (nameIndex >= 0) {
			name = Parser.readableName[nameIndex];
		} else {
			name = Util.EMPTY_STRING;
		}

		int errorStart = this.lexStream.start(token);
		int errorEnd = this.lexStream.end(token);
		int currentKind = this.lexStream.kind(token);
		String errorTokenName = Parser.name[Parser.terminal_index[this.lexStream.kind(token)]];
		char[] errorTokenSource = this.lexStream.name(token);
		if (currentKind == TerminalTokens.TokenNameStringLiteral) {
			errorTokenSource = displayEscapeCharacters(errorTokenSource, 1, errorTokenSource.length - 1);
		}

		int addedToken = -1;
		if(this.recoveryScanner != null) {
			if (nameIndex >= 0) {
				addedToken = Parser.reverse_index[nameIndex];
			}
		}
		switch(msgCode) {
			case BEFORE_CODE:
				if(this.recoveryScanner != null) {
					if(addedToken > -1) {
						this.recoveryScanner.insertToken(addedToken, -1, errorStart);
					} else {
						int[] template = getNTermTemplate(-addedToken);
						if(template != null) {
							this.recoveryScanner.insertTokens(template, -1, errorStart);
						}
					}
				}
				if(this.reportProblem) problemReporter().parseErrorInsertBeforeToken(
					errorStart,
					errorEnd,
					currentKind,
					errorTokenSource,
					errorTokenName,
					name);
				 break;
			case INSERTION_CODE:
				if(this.recoveryScanner != null) {
					if(addedToken > -1) {
						this.recoveryScanner.insertToken(addedToken, -1, errorEnd);
					} else {
						int[] template = getNTermTemplate(-addedToken);
						if(template != null) {
							this.recoveryScanner.insertTokens(template, -1, errorEnd);
						}
					}
				}
				if(this.reportProblem) problemReporter().parseErrorInsertAfterToken(
					errorStart,
					errorEnd,
					currentKind,
					errorTokenSource,
					errorTokenName,
					name);
				 break;
			case DELETION_CODE:
				if(this.recoveryScanner != null) {
					this.recoveryScanner.removeTokens(errorStart, errorEnd);
				}
				if(this.reportProblem) problemReporter().parseErrorDeleteToken(
					errorStart,
					errorEnd,
					currentKind,
					errorTokenSource,
					errorTokenName);
				break;
			case INVALID_CODE:
				if (name.length() == 0) {
					if(this.recoveryScanner != null) {
						this.recoveryScanner.removeTokens(errorStart, errorEnd);
					}
					if(this.reportProblem) problemReporter().parseErrorReplaceToken(
						errorStart,
						errorEnd,
						currentKind,
						errorTokenSource,
						errorTokenName,
						name);
				} else {
					if(this.recoveryScanner != null) {
						if(addedToken > -1) {
							this.recoveryScanner.replaceTokens(addedToken, errorStart, errorEnd);
						} else {
							int[] template = getNTermTemplate(-addedToken);
							if(template != null) {
								this.recoveryScanner.replaceTokens(template, errorStart, errorEnd);
							}
						}
					}
					if(this.reportProblem) problemReporter().parseErrorInvalidToken(
						errorStart,
						errorEnd,
						currentKind,
						errorTokenSource,
						errorTokenName,
						name);
				}
				break;
			case SUBSTITUTION_CODE:
				if(this.recoveryScanner != null) {
					if(addedToken > -1) {
						this.recoveryScanner.replaceTokens(addedToken, errorStart, errorEnd);
					} else {
						int[] template = getNTermTemplate(-addedToken);
						if(template != null) {
							this.recoveryScanner.replaceTokens(template, errorStart, errorEnd);
						}
					}
				}
				if(this.reportProblem) problemReporter().parseErrorReplaceToken(
					errorStart,
					errorEnd,
					currentKind,
					errorTokenSource,
					errorTokenName,
					name);
				 break;
			case SCOPE_CODE:
				StringBuffer buf = new StringBuffer();

				int[] addedTokens = null;
	            int addedTokenCount = 0;
	            if(this.recoveryScanner != null) {
	            	addedTokens = new int[Parser.scope_rhs.length - Parser.scope_suffix[- nameIndex]];
	            }

	            int insertedToken = TokenNameNotAToken;
				for (int i = Parser.scope_suffix[- nameIndex]; Parser.scope_rhs[i] != 0; i++) {
					buf.append(Parser.readableName[Parser.scope_rhs[i]]);
					if (Parser.scope_rhs[i + 1] != 0) // any more symbols to print?
						buf.append(' ');
					else
						insertedToken = Parser.reverse_index[Parser.scope_rhs[i]];

					if(addedTokens != null) {
	                	int tmpAddedToken = Parser.reverse_index[Parser.scope_rhs[i]];
		                if (tmpAddedToken > -1) {
		                	int length = addedTokens.length;
		                	if(addedTokenCount == length) {
		                		System.arraycopy(addedTokens, 0, addedTokens = new int[length * 2], 0, length);
		                	}
		                	addedTokens[addedTokenCount++] = tmpAddedToken;
		                } else {
		                	int[] template = getNTermTemplate(-tmpAddedToken);
		                	if(template != null) {
			                	for (int j = 0; j < template.length; j++) {
									int length = addedTokens.length;
		                			if(addedTokenCount == length) {
				                		System.arraycopy(addedTokens, 0, addedTokens = new int[length * 2], 0, length);
				                	}
		                			addedTokens[addedTokenCount++] = template[j];
								}
		                	} else {
			                	addedTokenCount = 0;
			                	addedTokens = null;
		                	}
		                }
	                }
				}

				if(addedTokenCount > 0) {
	            	System.arraycopy(addedTokens, 0, addedTokens = new int[addedTokenCount], 0, addedTokenCount);

	            	int completedToken = -1;
	            	if(scopeNameIndex != 0) {
	            		completedToken = -Parser.reverse_index[scopeNameIndex];
	            	}
	            	this.recoveryScanner.insertTokens(addedTokens, completedToken, errorEnd);
	            }

				if (scopeNameIndex != 0) {
					if (insertedToken == TokenNameElidedSemicolonAndRightBrace) {
						/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=383046, we should never ever report the diagnostic, "Syntax error, insert ElidedSemicolonAndRightBraceto complete LambdaBody"
						   as it is a synthetic token. Instead we should simply repair and move on. See how the regular Parser behaves at Parser.consumeElidedLeftBraceAndReturn and Parser.consumeExpression.
						   See also: point (4) in https://bugs.eclipse.org/bugs/show_bug.cgi?id=380194#c15
						*/
						break;
					}
					if(this.reportProblem) problemReporter().parseErrorInsertToComplete(
						errorStart,
						errorEnd,
						buf.toString(),
						Parser.readableName[scopeNameIndex]);
				} else {
					if(this.reportProblem) problemReporter().parseErrorInsertToCompleteScope(
						errorStart,
						errorEnd,
						buf.toString());
				}

				break;
			case EOF_CODE:
				if(this.reportProblem) problemReporter().parseErrorUnexpectedEnd(
					errorStart,
					errorEnd);
				break;
			case MERGE_CODE:
				if(this.recoveryScanner != null) {
					if(addedToken > -1) {
						this.recoveryScanner.replaceTokens(addedToken, errorStart, errorEnd);
					} else {
						int[] template = getNTermTemplate(-addedToken);
						if(template != null) {
							this.recoveryScanner.replaceTokens(template, errorStart, errorEnd);
						}
					}
				}
				if(this.reportProblem) problemReporter().parseErrorMergeTokens(
					errorStart,
					errorEnd,
					name);
				break;
			case MISPLACED_CODE:
				if(this.recoveryScanner != null) {
					this.recoveryScanner.removeTokens(errorStart, errorEnd);
				}
				if(this.reportProblem) problemReporter().parseErrorMisplacedConstruct(
					errorStart,
					errorEnd);
				break;
			default:
				if (name.length() == 0) {
					if(this.recoveryScanner != null) {
						this.recoveryScanner.removeTokens(errorStart, errorEnd);
					}
					if(this.reportProblem) problemReporter().parseErrorNoSuggestion(
						errorStart,
						errorEnd,
						currentKind,
						errorTokenSource,
						errorTokenName);
				} else {
					if(this.recoveryScanner != null) {
						if(addedToken > -1) {
							this.recoveryScanner.replaceTokens(addedToken, errorStart, errorEnd);
						} else {
							int[] template = getNTermTemplate(-addedToken);
							if(template != null) {
								this.recoveryScanner.replaceTokens(template, errorStart, errorEnd);
							}
						}
					}
					if(this.reportProblem) problemReporter().parseErrorReplaceToken(
						errorStart,
						errorEnd,
						currentKind,
						errorTokenSource,
						errorTokenName,
						name);
				}
				break;
		}
	}

	private void reportSecondaryError(int msgCode,	int nameIndex,	int leftToken,	int rightToken, int scopeNameIndex) {
		String name;
		if (nameIndex >= 0) {
			name = Parser.readableName[nameIndex];
		} else {
			name = Util.EMPTY_STRING;
		}

		int errorStart = -1;
		if(this.lexStream.isInsideStream(leftToken)) {
			if(leftToken == 0) {
				errorStart = this.lexStream.start(leftToken + 1);
			} else {
				errorStart = this.lexStream.start(leftToken);
			}
		} else {
			if(leftToken == this.errorToken) {
				errorStart = this.errorTokenStart;
			} else {
				for (int i = 0; i <= this.stateStackTop; i++) {
					if(this.locationStack[i] == leftToken) {
						errorStart = this.locationStartStack[i];
					}
				}
			}
			if(errorStart == -1) {
				errorStart = this.lexStream.start(rightToken);
			}
		}
		int errorEnd = this.lexStream.end(rightToken);

		int addedToken = -1;
		if(this.recoveryScanner != null) {
			if (nameIndex >= 0) {
				addedToken = Parser.reverse_index[nameIndex];
			}
		}

		switch(msgCode) {
			case MISPLACED_CODE:
				if(this.recoveryScanner != null) {
					this.recoveryScanner.removeTokens(errorStart, errorEnd);
				}
				if(this.reportProblem) problemReporter().parseErrorMisplacedConstruct(
					errorStart,
					errorEnd);
				break;
			case SCOPE_CODE:
				// error start is on the last token start
				errorStart = this.lexStream.start(rightToken);

	            StringBuffer buf = new StringBuffer();

	            int[] addedTokens = null;
	            int addedTokenCount = 0;
	            if(this.recoveryScanner != null) {
	            	addedTokens = new int[Parser.scope_rhs.length - Parser.scope_suffix[- nameIndex]];
	            }
	            int insertedToken = TokenNameNotAToken;
	            for (int i = Parser.scope_suffix[- nameIndex]; Parser.scope_rhs[i] != 0; i++) {

	                buf.append(Parser.readableName[Parser.scope_rhs[i]]);
	                if (Parser.scope_rhs[i+1] != 0)
	                     buf.append(' ');
	                else
	                	insertedToken = Parser.reverse_index[Parser.scope_rhs[i]];

	                if(addedTokens != null) {
	                	int tmpAddedToken = Parser.reverse_index[Parser.scope_rhs[i]];
		                if (tmpAddedToken > -1) {
		                	int length = addedTokens.length;
		                	if(addedTokenCount == length) {
		                		System.arraycopy(addedTokens, 0, addedTokens = new int[length * 2], 0, length);
		                	}
		                	addedTokens[addedTokenCount++] = tmpAddedToken;
		                } else {
		                	int[] template = getNTermTemplate(-tmpAddedToken);
		                	if(template != null) {
			                	for (int j = 0; j < template.length; j++) {
									int length = addedTokens.length;
		                			if(addedTokenCount == length) {
				                		System.arraycopy(addedTokens, 0, addedTokens = new int[length * 2], 0, length);
				                	}
		                			addedTokens[addedTokenCount++] = template[j];
								}
		                	} else {
			                	addedTokenCount = 0;
			                	addedTokens = null;
		                	}
		                }
	                }
	            }
	            if(addedTokenCount > 0) {
	            	System.arraycopy(addedTokens, 0, addedTokens = new int[addedTokenCount], 0, addedTokenCount);
	            	int completedToken = -1;
	            	if(scopeNameIndex != 0) {
	            		completedToken = -Parser.reverse_index[scopeNameIndex];
	            	}
	            	this.recoveryScanner.insertTokens(addedTokens, completedToken, errorEnd);
	            }
	            if (scopeNameIndex != 0) {
	            	if (insertedToken == TokenNameElidedSemicolonAndRightBrace) {
						/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=383046, we should never ever report the diagnostic, "Syntax error, insert ElidedSemicolonAndRightBraceto complete LambdaBody"
						   as it is a synthetic token. Instead we should simply repair and move on. See how the regular Parser behaves at Parser.consumeElidedLeftBraceAndReturn and Parser.consumeExpression.
						   See also: point (4) in https://bugs.eclipse.org/bugs/show_bug.cgi?id=380194#c15
						*/
						break;
					}
	                if(this.reportProblem) problemReporter().parseErrorInsertToComplete(
						errorStart,
						errorEnd,
						buf.toString(),
						Parser.readableName[scopeNameIndex]);
	            } else {
	            	if(this.reportProblem) problemReporter().parseErrorInsertToCompletePhrase(
						errorStart,
						errorEnd,
						buf.toString());
	            }
	            break;
			case MERGE_CODE:
				if(this.recoveryScanner != null) {
					if(addedToken > -1) {
						this.recoveryScanner.replaceTokens(addedToken, errorStart, errorEnd);
					} else {
						int[] template = getNTermTemplate(-addedToken);
						if(template != null) {
							this.recoveryScanner.replaceTokens(template, errorStart, errorEnd);
						}
					}
				}
				if(this.reportProblem) problemReporter().parseErrorMergeTokens(
					errorStart,
					errorEnd,
					name);
				break;
			case DELETION_CODE:
				if(this.recoveryScanner != null) {
					this.recoveryScanner.removeTokens(errorStart, errorEnd);
				}
				if(this.reportProblem) problemReporter().parseErrorDeleteTokens(
					errorStart,
					errorEnd);
				break;
			default:
				if (name.length() == 0) {
					if(this.recoveryScanner != null) {
						this.recoveryScanner.removeTokens(errorStart, errorEnd);
					}
					if(this.reportProblem) problemReporter().parseErrorNoSuggestionForTokens(
						errorStart,
						errorEnd);
				} else {
					if(this.recoveryScanner != null) {
						if(addedToken > -1) {
							this.recoveryScanner.replaceTokens(addedToken, errorStart, errorEnd);
						} else {
							int[] template = getNTermTemplate(-addedToken);
							if(template != null) {
								this.recoveryScanner.replaceTokens(template, errorStart, errorEnd);
							}
						}
					}
					if(this.reportProblem) problemReporter().parseErrorReplaceTokens(
						errorStart,
						errorEnd,
						name);
				}
		}
		return;
	}

	private int[] getNTermTemplate(int sym) {
		int templateIndex = Parser.recovery_templates_index[sym];
    	if(templateIndex > 0) {
    		int[] result = new int[Parser.recovery_templates.length];
    		int count = 0;
    		for(int j = templateIndex; Parser.recovery_templates[j] != 0; j++) {
    			result[count++] = Parser.recovery_templates[j];
    		}
    		System.arraycopy(result, 0, result = new int[count], 0, count);
    		return result;
    	} else {
        	return null;
    	}
	}

	public String toString() {
		StringBuffer res = new StringBuffer();

		res.append(this.lexStream.toString());

		return res.toString();
	}

	public boolean atConflictScenario(int token) {
		/* There is too much voodoo that goes on here in DiagnoseParser (multiple machines, lexer stream reset etc.)
		   So we take a simple minded view that we will always ask for disambiguation, except there is one scenario 
		   that needs special handling, we let the lexer stream deal with that: In X<String>.Y<Integer>:: the second
		   '<' should not be tagged for disambiguation. If a synthetic token gets injected there, there will be syntax
		   error. See that this is not a problem for the regular/normal parser.
		*/ 
		return (token == TokenNameLPAREN || token == TokenNameAT || (token == TokenNameLESS && !this.lexStream.awaitingColonColon()));
	}

	@Override
	public boolean isParsingModuleDeclaration() {
		//
		return this.parser.isParsingModuleDeclaration();
	}
}
