/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.Statement;

public abstract class CommitRollbackParser implements TerminalTokens, ParserBasicInformation {
	
	// resumeOnSyntaxError codes:
	protected static final int HALT = 0;     // halt and throw up hands.
	protected static final int RESTART = 1;  // stacks adjusted, alternate goal from check point.
	protected static final int RESUME = 2;   // stacks untouched, just continue from where left off.

	public Scanner scanner;
	public int currentToken;
	
	public CommitRollbackParser snapShot;
	private static final int[] RECOVERY_TOKENS = new int [] { TokenNameSEMICOLON, TokenNameRPAREN,};
	
	protected CommitRollbackParser createSnapShotParser() {
		return new Parser();
	}
	
	protected void commit() {
		if (this.snapShot == null) {
			this.snapShot = createSnapShotParser();
		}
		this.snapShot.copyState(this);
	}
	
	public void copyState(CommitRollbackParser commitRollbackParser) {
		// Subclasses should implement.
	}

	protected int getNextToken() {
		try {
			return this.scanner.getNextToken();
		} catch (InvalidInputException e) {
			return TokenNameEOF;
		}
	}
	
	protected void shouldStackAssistNode() {
		// Not relevant here.
	}
	
	// We get here on real syntax error or syntax error triggered by fake EOF at completion site, never due to triggered recovery.
	protected int fallBackToSpringForward(Statement unused) {
		int nextToken;
		int automatonState = automatonState();
				
		// If triggered fake EOF at completion site, see if the real next token would have passed muster.
		if (this.currentToken == TokenNameEOF) {
			if (this.scanner.eofPosition < this.scanner.source.length) {
				shouldStackAssistNode();
				this.scanner.eofPosition = this.scanner.source.length;
				nextToken = getNextToken();
				if (automatonWillShift(nextToken, automatonState)) {
					this.currentToken = nextToken;
					return RESUME;
				}
				this.scanner.ungetToken(nextToken); // spit out what has been bitten more than we can chew.
			} else {
				return HALT; // don't know how to proceed.
			}
		} else {
			nextToken = this.currentToken;
			this.scanner.ungetToken(nextToken);
			if (nextToken == TokenNameRBRACE)
				ignoreNextClosingBrace(); // having ungotten it, recoveryTokenCheck will see this again. 
		}
		// OK, next token is no good to resume "in place", attempt some local repair. FIXME: need to make sure we don't get stuck keep reducing empty statements !!
		for (int i = 0, length = RECOVERY_TOKENS.length; i < length; i++) {
			if (automatonWillShift(RECOVERY_TOKENS[i], automatonState)) {
				this.currentToken = RECOVERY_TOKENS[i];
				return RESUME;
			}
		}
		// OK, no in place resumption, no local repair, fast forward to next statement.
		if (this.snapShot == null)
			return RESTART;

		this.copyState(this.snapShot);
		if (assistNodeNeedsStacking()) {
			this.currentToken = TokenNameSEMICOLON;
			return RESUME;
		}
		this.currentToken = this.scanner.fastForward(unused);
		return RESUME;
	}

	protected void ignoreNextClosingBrace() {
		return;
	}

	protected boolean assistNodeNeedsStacking() {
		return false;
	}

	public abstract int automatonState();

	public abstract boolean automatonWillShift(int nextToken, int lastAction);
}

