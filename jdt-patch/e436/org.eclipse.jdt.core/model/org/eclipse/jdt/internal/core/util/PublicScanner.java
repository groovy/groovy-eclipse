/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalToken;

/**
 * This class acts as a facade to the internal Scanner implementation and delegates
 * all the work to the internal Scanner instance.
 *
 * <p>
 * <b>Note for maintainers</b>
 * No additional logic should be added here except adopting NON-API constants
 * returned by {@link #getNextToken()} from {@link TerminalToken} to {@link ITerminalSymbols}.
 */
public class PublicScanner implements IScanner, ITerminalSymbols {

	private final Scanner delegate;

	public PublicScanner(
			boolean tokenizeComments,
			boolean tokenizeWhiteSpace,
			boolean checkNonExternalizedStringLiterals,
			long sourceLevel,
			long complianceLevel,
			char[][] taskTags,
			char[][] taskPriorities,
			boolean isTaskCaseSensitive,
			boolean isPreviewEnabled,
			boolean recordLineSeparator) {

			this.delegate = createScanner(tokenizeComments, tokenizeWhiteSpace, checkNonExternalizedStringLiterals, sourceLevel,
					complianceLevel, taskTags, taskPriorities, isTaskCaseSensitive, isPreviewEnabled);
			this.delegate.recordLineSeparator = recordLineSeparator;
	}

	protected Scanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace,
			boolean checkNonExternalizedStringLiterals, long sourceLevel, long complianceLevel, char[][] taskTags,
			char[][] taskPriorities, boolean isTaskCaseSensitive, boolean isPreviewEnabled) {
		return new Scanner(tokenizeComments, tokenizeWhiteSpace, checkNonExternalizedStringLiterals,
				sourceLevel, complianceLevel, taskTags, taskPriorities, isTaskCaseSensitive, isPreviewEnabled);
	}

	@Override
	public int getCurrentTokenEndPosition(){
		return this.delegate.getCurrentTokenEndPosition();
	}

	@Override
	public char[] getCurrentTokenSource() {
		return this.delegate.getCurrentTokenSource();
	}

	@Override
	public final char[] getRawTokenSource() {
		return this.delegate.getRawTokenSource();
	}

	@Override
	public int getCurrentTokenStartPosition(){
		return this.delegate.startPosition;
	}

	/*
	 * Search the source position corresponding to the end of a given line number
	 *
	 * Line numbers are 1-based, and relative to the scanner initialPosition.
	 * Character positions are 0-based.
	 *
	 * In case the given line number is inconsistent, answers -1.
	 */
	@Override
	public final int getLineEnd(int lineNumber) {
		return this.delegate.getLineEnd(lineNumber);
	}

	@Override
	public final int[] getLineEnds() {
		return this.delegate.getLineEnds();
	}

	/**
	 * Search the source position corresponding to the beginning of a given line number
	 *
	 * Line numbers are 1-based, and relative to the scanner initialPosition.
	 * Character positions are 0-based.
	 *
	 * e.g.	getLineStart(1) --> 0	indicates that the first line starts at character 0.
	 *
	 * In case the given line number is inconsistent, answers -1.
	 *
	 * @param lineNumber int
	 * @return int
	 */
	@Override
	public final int getLineStart(int lineNumber) {
		return this.delegate.getLineStart(lineNumber);
	}

	@Override
	public int getNextToken() throws InvalidInputException {
		TerminalToken nextToken = this.delegate.getNextToken();
		int symbol  = translateTokenToTerminalSymbol(nextToken);
		return symbol;
	}

	/**
	 * Translates internal generated constants from {@link TerminalToken} to public constants defined in
	 * {@link ITerminalSymbols}.
	 * <p>
	 * There is PublicScannerTest that validates the translation and can generate switch body for this method
	 * on changes in Scanner / TerminalTokens.
	 * <p>
	 * Note for maintainers: new tokens from {@link TerminalToken} should be added to {@link ITerminalSymbols} with
	 * <b>adopted</b> numerical values (they differ in each interface).
	 */
	@Deprecated // uses deprecated TerminalTokensIdentifier
	private int translateTokenToTerminalSymbol(TerminalToken nextTokenEnum) throws InvalidInputException {
		int nextToken = nextTokenEnum.tokenNumber();
		switch (nextTokenEnum) {
			case TokenNameAND : nextToken = ITerminalSymbols.TokenNameAND; break;
			case TokenNameAND_AND : nextToken = ITerminalSymbols.TokenNameAND_AND; break;
			case TokenNameAND_EQUAL : nextToken = ITerminalSymbols.TokenNameAND_EQUAL; break;
			case TokenNameARROW : nextToken = ITerminalSymbols.TokenNameARROW; break;
			case TokenNameAT : nextToken = ITerminalSymbols.TokenNameAT; break;
			case TokenNameAT308 : nextToken = ITerminalSymbols.TokenNameAT; break;
			case TokenNameAT308DOTDOTDOT : nextToken = ITerminalSymbols.TokenNameAT; break;
			case TokenNameBeginCasePattern : nextToken = getNextToken(); break;
			case TokenNameCaseArrow : nextToken = ITerminalSymbols.TokenNameARROW; break;
			case TokenNameBeginIntersectionCast : nextToken = getNextToken(); break;
			case TokenNameBeginLambda : nextToken = getNextToken(); break;
			case TokenNameBeginTypeArguments : nextToken = getNextToken(); break;
			case TokenNameCOLON : nextToken = ITerminalSymbols.TokenNameCOLON; break;
			case TokenNameCOLON_COLON : nextToken = ITerminalSymbols.TokenNameCOLON_COLON; break;
			case TokenNameCOMMA : nextToken = ITerminalSymbols.TokenNameCOMMA; break;
			case TokenNameCOMMENT_BLOCK : nextToken = ITerminalSymbols.TokenNameCOMMENT_BLOCK; break;
			case TokenNameCOMMENT_JAVADOC : nextToken = ITerminalSymbols.TokenNameCOMMENT_JAVADOC; break;
			case TokenNameCOMMENT_MARKDOWN : nextToken = ITerminalSymbols.TokenNameCOMMENT_MARKDOWN; break;
			case TokenNameCOMMENT_LINE : nextToken = ITerminalSymbols.TokenNameCOMMENT_LINE; break;
			case TokenNameCharacterLiteral : nextToken = ITerminalSymbols.TokenNameCharacterLiteral; break;
			case TokenNameDIVIDE : nextToken = ITerminalSymbols.TokenNameDIVIDE; break;
			case TokenNameDIVIDE_EQUAL : nextToken = ITerminalSymbols.TokenNameDIVIDE_EQUAL; break;
			case TokenNameDOT : nextToken = ITerminalSymbols.TokenNameDOT; break;
			case TokenNameDoubleLiteral : nextToken = ITerminalSymbols.TokenNameDoubleLiteral; break;
			case TokenNameELLIPSIS : nextToken = ITerminalSymbols.TokenNameELLIPSIS; break;
			case TokenNameEOF : nextToken = ITerminalSymbols.TokenNameEOF; break;
			case TokenNameEQUAL : nextToken = ITerminalSymbols.TokenNameEQUAL; break;
			case TokenNameEQUAL_EQUAL : nextToken = ITerminalSymbols.TokenNameEQUAL_EQUAL; break;
			case TokenNameERROR : nextToken = ITerminalSymbols.TokenNameERROR; break;
			case TokenNameElidedSemicolonAndRightBrace : nextToken = getNextToken(); break;
			case TokenNameFloatingPointLiteral : nextToken = ITerminalSymbols.TokenNameFloatingPointLiteral; break;
			case TokenNameGREATER : nextToken = ITerminalSymbols.TokenNameGREATER; break;
			case TokenNameGREATER_EQUAL : nextToken = ITerminalSymbols.TokenNameGREATER_EQUAL; break;
			case TokenNameIdentifier : nextToken = ITerminalSymbols.TokenNameIdentifier; break;
			case TokenNameIntegerLiteral : nextToken = ITerminalSymbols.TokenNameIntegerLiteral; break;
			case TokenNameLBRACE : nextToken = ITerminalSymbols.TokenNameLBRACE; break;
			case TokenNameLBRACKET : nextToken = ITerminalSymbols.TokenNameLBRACKET; break;
			case TokenNameLEFT_SHIFT : nextToken = ITerminalSymbols.TokenNameLEFT_SHIFT; break;
			case TokenNameLEFT_SHIFT_EQUAL : nextToken = ITerminalSymbols.TokenNameLEFT_SHIFT_EQUAL; break;
			case TokenNameLESS : nextToken = ITerminalSymbols.TokenNameLESS; break;
			case TokenNameLESS_EQUAL : nextToken = ITerminalSymbols.TokenNameLESS_EQUAL; break;
			case TokenNameLPAREN : nextToken = ITerminalSymbols.TokenNameLPAREN; break;
			case TokenNameLongLiteral : nextToken = ITerminalSymbols.TokenNameLongLiteral; break;
			case TokenNameMINUS : nextToken = ITerminalSymbols.TokenNameMINUS; break;
			case TokenNameMINUS_EQUAL : nextToken = ITerminalSymbols.TokenNameMINUS_EQUAL; break;
			case TokenNameMINUS_MINUS : nextToken = ITerminalSymbols.TokenNameMINUS_MINUS; break;
			case TokenNameMULTIPLY : nextToken = ITerminalSymbols.TokenNameMULTIPLY; break;
			case TokenNameMULTIPLY_EQUAL : nextToken = ITerminalSymbols.TokenNameMULTIPLY_EQUAL; break;
			case TokenNameNOT : nextToken = ITerminalSymbols.TokenNameNOT; break;
			case TokenNameNOT_EQUAL : nextToken = ITerminalSymbols.TokenNameNOT_EQUAL; break;
			case TokenNameNotAToken : nextToken = ITerminalSymbols.TokenNameNotAToken; break;
			case TokenNameOR : nextToken = ITerminalSymbols.TokenNameOR; break;
			case TokenNameOR_EQUAL : nextToken = ITerminalSymbols.TokenNameOR_EQUAL; break;
			case TokenNameOR_OR : nextToken = ITerminalSymbols.TokenNameOR_OR; break;
			case TokenNamePLUS : nextToken = ITerminalSymbols.TokenNamePLUS; break;
			case TokenNamePLUS_EQUAL : nextToken = ITerminalSymbols.TokenNamePLUS_EQUAL; break;
			case TokenNamePLUS_PLUS : nextToken = ITerminalSymbols.TokenNamePLUS_PLUS; break;
			case TokenNameQUESTION : nextToken = ITerminalSymbols.TokenNameQUESTION; break;
			case TokenNameRBRACE : nextToken = ITerminalSymbols.TokenNameRBRACE; break;
			case TokenNameRBRACKET : nextToken = ITerminalSymbols.TokenNameRBRACKET; break;
			case TokenNameREMAINDER : nextToken = ITerminalSymbols.TokenNameREMAINDER; break;
			case TokenNameREMAINDER_EQUAL : nextToken = ITerminalSymbols.TokenNameREMAINDER_EQUAL; break;
			case TokenNameRIGHT_SHIFT : nextToken = ITerminalSymbols.TokenNameRIGHT_SHIFT; break;
			case TokenNameRIGHT_SHIFT_EQUAL : nextToken = ITerminalSymbols.TokenNameRIGHT_SHIFT_EQUAL; break;
			case TokenNameRPAREN : nextToken = ITerminalSymbols.TokenNameRPAREN; break;
			case TokenNameRestrictedIdentifierYield : nextToken = ITerminalSymbols.TokenNameRestrictedIdentifierYield; break;
			case TokenNameRestrictedIdentifierpermits : nextToken = ITerminalSymbols.TokenNameRestrictedIdentifierpermits; break;
			case TokenNameRestrictedIdentifierrecord : nextToken = ITerminalSymbols.TokenNameRestrictedIdentifierrecord; break;
			case TokenNameRestrictedIdentifiersealed : nextToken = ITerminalSymbols.TokenNameRestrictedIdentifiersealed; break;
			case TokenNameSEMICOLON : nextToken = ITerminalSymbols.TokenNameSEMICOLON; break;
			case TokenNameSingleQuoteStringLiteral : nextToken = ITerminalSymbols.TokenNameSingleQuoteStringLiteral; break;
			case TokenNameStringLiteral : nextToken = ITerminalSymbols.TokenNameStringLiteral; break;
			case TokenNameTWIDDLE : nextToken = ITerminalSymbols.TokenNameTWIDDLE; break;
			case TokenNameTextBlock : nextToken = ITerminalSymbols.TokenNameTextBlock; break;
			case TokenNameUNSIGNED_RIGHT_SHIFT : nextToken = ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT; break;
			case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL : nextToken = ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL; break;
			case TokenNameWHITESPACE : nextToken = ITerminalSymbols.TokenNameWHITESPACE; break;
			case TokenNameXOR : nextToken = ITerminalSymbols.TokenNameXOR; break;
			case TokenNameXOR_EQUAL : nextToken = ITerminalSymbols.TokenNameXOR_EQUAL; break;
			case TokenNameabstract : nextToken = ITerminalSymbols.TokenNameabstract; break;
			case TokenNameassert : nextToken = ITerminalSymbols.TokenNameassert; break;
			case TokenNameboolean : nextToken = ITerminalSymbols.TokenNameboolean; break;
			case TokenNamebreak : nextToken = ITerminalSymbols.TokenNamebreak; break;
			case TokenNamebyte : nextToken = ITerminalSymbols.TokenNamebyte; break;
			case TokenNamecase : nextToken = ITerminalSymbols.TokenNamecase; break;
			case TokenNamecatch : nextToken = ITerminalSymbols.TokenNamecatch; break;
			case TokenNamechar : nextToken = ITerminalSymbols.TokenNamechar; break;
			case TokenNameclass : nextToken = ITerminalSymbols.TokenNameclass; break;
			case TokenNameconst : nextToken = ITerminalSymbols.TokenNameconst; break;
			case TokenNamecontinue : nextToken = ITerminalSymbols.TokenNamecontinue; break;
			case TokenNamedefault : nextToken = ITerminalSymbols.TokenNamedefault; break;
			case TokenNamedo : nextToken = ITerminalSymbols.TokenNamedo; break;
			case TokenNamedouble : nextToken = ITerminalSymbols.TokenNamedouble; break;
			case TokenNameelse : nextToken = ITerminalSymbols.TokenNameelse; break;
			case TokenNameenum : nextToken = ITerminalSymbols.TokenNameenum; break;
			case TokenNameextends : nextToken = ITerminalSymbols.TokenNameextends; break;
			case TokenNamefalse : nextToken = ITerminalSymbols.TokenNamefalse; break;
			case TokenNamefinal : nextToken = ITerminalSymbols.TokenNamefinal; break;
			case TokenNamefinally : nextToken = ITerminalSymbols.TokenNamefinally; break;
			case TokenNamefloat : nextToken = ITerminalSymbols.TokenNamefloat; break;
			case TokenNamefor : nextToken = ITerminalSymbols.TokenNamefor; break;
			case TokenNamegoto : nextToken = ITerminalSymbols.TokenNamegoto; break;
			case TokenNameif : nextToken = ITerminalSymbols.TokenNameif; break;
			case TokenNameimplements : nextToken = ITerminalSymbols.TokenNameimplements; break;
			case TokenNameimport : nextToken = ITerminalSymbols.TokenNameimport; break;
			case TokenNameinstanceof : nextToken = ITerminalSymbols.TokenNameinstanceof; break;
			case TokenNameint : nextToken = ITerminalSymbols.TokenNameint; break;
			case TokenNameinterface : nextToken = ITerminalSymbols.TokenNameinterface; break;
			case TokenNamelong : nextToken = ITerminalSymbols.TokenNamelong; break;
			case TokenNamenative : nextToken = ITerminalSymbols.TokenNamenative; break;
			case TokenNamenew : nextToken = ITerminalSymbols.TokenNamenew; break;
			case TokenNamenon_sealed : nextToken = ITerminalSymbols.TokenNamenon_sealed; break;
			case TokenNamenull : nextToken = ITerminalSymbols.TokenNamenull; break;
			case TokenNamepackage : nextToken = ITerminalSymbols.TokenNamepackage; break;
			case TokenNameprivate : nextToken = ITerminalSymbols.TokenNameprivate; break;
			case TokenNameprotected : nextToken = ITerminalSymbols.TokenNameprotected; break;
			case TokenNamepublic : nextToken = ITerminalSymbols.TokenNamepublic; break;
			case TokenNamereturn : nextToken = ITerminalSymbols.TokenNamereturn; break;
			case TokenNameshort : nextToken = ITerminalSymbols.TokenNameshort; break;
			case TokenNamestatic : nextToken = ITerminalSymbols.TokenNamestatic; break;
			case TokenNamestrictfp : nextToken = ITerminalSymbols.TokenNamestrictfp; break;
			case TokenNamesuper : nextToken = ITerminalSymbols.TokenNamesuper; break;
			case TokenNameswitch : nextToken = ITerminalSymbols.TokenNameswitch; break;
			case TokenNamesynchronized : nextToken = ITerminalSymbols.TokenNamesynchronized; break;
			case TokenNamethis : nextToken = ITerminalSymbols.TokenNamethis; break;
			case TokenNamethrow : nextToken = ITerminalSymbols.TokenNamethrow; break;
			case TokenNamethrows : nextToken = ITerminalSymbols.TokenNamethrows; break;
			case TokenNametransient : nextToken = ITerminalSymbols.TokenNametransient; break;
			case TokenNametrue : nextToken = ITerminalSymbols.TokenNametrue; break;
			case TokenNametry : nextToken = ITerminalSymbols.TokenNametry; break;
			case TokenNamevoid : nextToken = ITerminalSymbols.TokenNamevoid; break;
			case TokenNamevolatile : nextToken = ITerminalSymbols.TokenNamevolatile; break;
			case TokenNamewhile : nextToken = ITerminalSymbols.TokenNamewhile; break;
			case TokenNameRestrictedIdentifierWhen : nextToken = ITerminalSymbols.TokenNameRestrictedIdentifierWhen; break;
			case TokenNameUNDERSCORE : nextToken = ITerminalSymbols.TokenNameUNDERSCORE; break;
			default:
				throw Scanner.invalidToken(nextTokenEnum);
		}
		return nextToken;
	}

	@Override
	public char[] getSource(){
		return this.delegate.getSource();
	}

	/**
	 * Reposition the scanner on some portion of the original source. The given endPosition is the last valid position.
	 * Beyond this position, the scanner will answer EOF tokens (<code>ITerminalSymbols.TokenNameEOF</code>).
	 *
	 * @param begin the given start position
	 * @param end the given end position
	 */
	@Override
	public void resetTo(int begin, int end) {
		this.delegate.resetTo(begin, end);
	}

	/**
	 * Search the line number corresponding to a specific position
	 * @param position int
	 * @return int
	 */
	@Override
	public final int getLineNumber(int position) {
		return this.delegate.getLineNumber(position);
	}

	@Override
	public final void setSource(char[] sourceString){
		this.delegate.setSource(sourceString);
	}

	@Override
	public String toString() {
		return this.delegate.toString();
	}

}
