/*******************************************************************************
 * Copyright (c) 2008, 2024 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.*;

public class RecoveredAnnotation extends RecoveredElement {
	public static final int MARKER = 0;
	public static final int NORMAL = 1;
	public static final int SINGLE_MEMBER = 2;

	private int kind;
	final int identifierPtr;
	private final int identifierLengthPtr;
	private final int sourceStart;
	public boolean hasPendingMemberValueName;
	public int memberValuPairEqualEnd = -1;
	public Annotation annotation;
	public int errorToken;

	public RecoveredAnnotation(int identifierPtr, int identifierLengthPtr, int sourceStart, RecoveredElement parent, int bracketBalance) {
		super(parent, bracketBalance);
		this.kind = MARKER;
		this.identifierPtr = identifierPtr;
		this.identifierLengthPtr = identifierLengthPtr;
		this.sourceStart = sourceStart;
	}

	@Override
	public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalanceValue) {
		if (this.annotation == null && (typeDeclaration.bits & ASTNode.IsAnonymousType) != 0){
			// ignore anonymous type in annotations when annotation isn't fully recovered
			return this;
		}
		return super.add(typeDeclaration, bracketBalanceValue);
	}

	@Override
	public RecoveredElement addAnnotationName(int identPtr, int identLengthPtr, int annotationStart, int bracketBalanceValue) {

		RecoveredAnnotation element = new RecoveredAnnotation(identPtr, identLengthPtr, annotationStart, this, bracketBalanceValue);

		return element;
	}

	public RecoveredElement addAnnotation(Annotation annot, int index) {
		this.annotation = annot;

		if (this.parent != null) return this.parent;
		return this;
	}

	@Override
	public void updateFromParserState() {
		Parser parser = parser();

		if (this.annotation == null && this.identifierPtr <= parser.identifierPtr) {
			Annotation annot = null;

			boolean needUpdateRParenPos = false;

			MemberValuePair pendingMemberValueName = null;
			Expression singleValue = null;
			if (this.hasPendingMemberValueName && this.identifierPtr < parser.identifierPtr) {
				char[] memberValueName = parser.identifierStack[this.identifierPtr + 1];

				long pos = parser.identifierPositionStack[this.identifierPtr + 1];
				int start = (int) (pos >>> 32);
				int end = (int)pos;

				if (this.errorToken == TerminalTokens.TokenNameDOT) {
					// do we need to consult identifierLengthStack to pull more than one name segment?
					char[][] qname = new char[][] { memberValueName, RecoveryScanner.FAKE_IDENTIFIER };
					singleValue = new QualifiedNameReference(qname, new long[] {pos,pos}, start, end);
					this.kind = SINGLE_MEMBER;
				} else {
					int valueEnd = this.memberValuPairEqualEnd > -1 ? this.memberValuPairEqualEnd : end;
					SingleNameReference fakeExpression = new SingleNameReference(RecoveryScanner.FAKE_IDENTIFIER, (((long) valueEnd + 1) << 32) + (valueEnd));
					pendingMemberValueName = new MemberValuePair(memberValueName, start, end, fakeExpression);
				}
			}
			parser.identifierPtr = this.identifierPtr;
			parser.identifierLengthPtr = this.identifierLengthPtr;
			TypeReference typeReference = parser.getAnnotationType();

			switch (this.kind) {
				case NORMAL:
					if (parser.astPtr > -1 && parser.astStack[parser.astPtr] instanceof MemberValuePair) {
						MemberValuePair[] memberValuePairs = null;

						int argLength = parser.astLengthStack[parser.astLengthPtr];
						int argStart = parser.astPtr - argLength + 1;

						if (argLength > 0) {
							int annotationEnd;
							if (pendingMemberValueName != null) {
								memberValuePairs = new MemberValuePair[argLength + 1];

								System.arraycopy(parser.astStack, argStart, memberValuePairs, 0, argLength);
								parser.astLengthPtr--;
								parser.astPtr -= argLength;

								memberValuePairs[argLength] = pendingMemberValueName;

								annotationEnd = pendingMemberValueName.sourceEnd;
							} else {
								memberValuePairs = new MemberValuePair[argLength];

								System.arraycopy(parser.astStack, argStart, memberValuePairs, 0, argLength);
								parser.astLengthPtr--;
								parser.astPtr -= argLength;

								MemberValuePair lastMemberValuePair = memberValuePairs[memberValuePairs.length - 1];

								annotationEnd =
									lastMemberValuePair.value != null
										? lastMemberValuePair.value instanceof Annotation
												? ((Annotation)lastMemberValuePair.value).declarationSourceEnd
												: lastMemberValuePair.value.sourceEnd
										: lastMemberValuePair.sourceEnd;
							}

							NormalAnnotation normalAnnotation = new NormalAnnotation(typeReference, this.sourceStart);
							normalAnnotation.memberValuePairs = memberValuePairs;
							normalAnnotation.declarationSourceEnd = annotationEnd;
							normalAnnotation.bits |= ASTNode.IsRecovered;

							annot = normalAnnotation;

							needUpdateRParenPos = true;
						}
					}


					break;
				case SINGLE_MEMBER:
					Expression memberValue = singleValue != null ? singleValue
							: parser.expressionPtr > -1 ? parser.expressionStack[parser.expressionPtr--]
							: null;
					if (memberValue != null) {
						SingleMemberAnnotation singleMemberAnnotation = new SingleMemberAnnotation(typeReference, this.sourceStart);
						singleMemberAnnotation.memberValue = memberValue;
						singleMemberAnnotation.declarationSourceEnd = memberValue.sourceEnd;
						singleMemberAnnotation.bits |= ASTNode.IsRecovered;

						annot = singleMemberAnnotation;

						needUpdateRParenPos = true;
					}
					break;
			}

			if (!needUpdateRParenPos) {
				if (pendingMemberValueName != null) {
					NormalAnnotation normalAnnotation = new NormalAnnotation(typeReference, this.sourceStart);
					normalAnnotation.memberValuePairs = new MemberValuePair[]{pendingMemberValueName};
					normalAnnotation.declarationSourceEnd = pendingMemberValueName.value.sourceEnd;
					normalAnnotation.bits |= ASTNode.IsRecovered;

					annot = normalAnnotation;
				} else {
					MarkerAnnotation markerAnnotation = new MarkerAnnotation(typeReference, this.sourceStart);
					markerAnnotation.declarationSourceEnd = markerAnnotation.sourceEnd;
					markerAnnotation.bits |= ASTNode.IsRecovered;

					annot = markerAnnotation;
				}
			}

			parser.currentElement = addAnnotation(annot, this.identifierPtr);
			parser.annotationRecoveryCheckPoint(annot.sourceStart, annot.declarationSourceEnd);
			if (this.parent != null) {

				this.parent.updateFromParserState();
			}
		}
	}

	@Override
	public ASTNode parseTree() {
		return this.annotation;
	}

	@Override
	public void resetPendingModifiers() {
		if (this.parent != null) this.parent.resetPendingModifiers();
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int sourceStart() {
		return this.sourceStart;
	}

	@Override
	public int sourceEnd() {
		if (this.annotation == null) {
			Parser parser = parser();
			if (this.identifierPtr < parser.identifierPositionStack.length) {
				return (int) parser.identifierPositionStack[this.identifierPtr];
			} else {
				return this.sourceStart;
			}
		}
		return this.annotation.declarationSourceEnd;
	}

	@Override
	public String toString(int tab) {
		if (this.annotation != null) {
			return tabString(tab) + "Recovered annotation:\n" + this.annotation.print(tab + 1, new StringBuilder(10)); //$NON-NLS-1$
		} else {
			return tabString(tab) + "Recovered annotation: identiferPtr=" + this.identifierPtr + " identiferlengthPtr=" + this.identifierLengthPtr + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public Annotation updatedAnnotationReference() {
		return this.annotation;
	}

	@Override
	public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd){
		if (this.bracketBalance > 0){ // was an member value array initializer
			this.bracketBalance--;
			return this;
		}
		if (this.parent != null){
			return this.parent.updateOnClosingBrace(braceStart, braceEnd);
		}
		return this;
	}

	@Override
	public void updateParseTree() {
		updatedAnnotationReference();
	}
}
