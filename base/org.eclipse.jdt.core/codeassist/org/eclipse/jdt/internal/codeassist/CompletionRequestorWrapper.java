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
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * This CompletionRequetor wrap the old requestor ICOmpletionRequestor
 * @since 3.1
 * @deprecated
 */
public class CompletionRequestorWrapper extends CompletionRequestor {
	private static boolean DECODE_SIGNATURE = false;
	
	private org.eclipse.jdt.core.ICompletionRequestor requestor;
	public CompletionRequestorWrapper(org.eclipse.jdt.core.ICompletionRequestor requestor) {
		this.requestor = requestor;
	}
	
	public void accept(CompletionProposal proposal) {
		switch(proposal.getKind()) {
			case CompletionProposal.KEYWORD:
				this.requestor.acceptKeyword(
						proposal.getName(),
						proposal.getReplaceStart(),
						proposal.getReplaceEnd(),
						proposal.getRelevance());
				break;
			case CompletionProposal.PACKAGE_REF:
				if(DECODE_SIGNATURE) {
					this.requestor.acceptPackage(
							proposal.getDeclarationSignature(),
							proposal.getCompletion(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance());
				} else {
					this.requestor.acceptPackage(
							proposal.getPackageName(),
							proposal.getCompletion(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance());
				}
				break;
			case CompletionProposal.TYPE_REF:
				if((proposal.getFlags() & Flags.AccEnum) != 0) {
					// does not exist for old requestor
				} else if((proposal.getFlags() & Flags.AccInterface) != 0) {
					if(DECODE_SIGNATURE) {
						this.requestor.acceptInterface(
								proposal.getDeclarationSignature(),
								Signature.getSignatureSimpleName(proposal.getSignature()),
								proposal.getCompletion(),
								proposal.getFlags() & ~Flags.AccInterface,
								proposal.getReplaceStart(),
								proposal.getReplaceEnd(),
								proposal.getRelevance());
					} else {
						this.requestor.acceptInterface(
								proposal.getPackageName() == null ? CharOperation.NO_CHAR : proposal.getPackageName(),
								proposal.getTypeName(),
								proposal.getCompletion(),
								proposal.getFlags() & ~Flags.AccInterface,
								proposal.getReplaceStart(),
								proposal.getReplaceEnd(),
								proposal.getRelevance());
					}
				} else {
					if(DECODE_SIGNATURE) {
						this.requestor.acceptClass(
								proposal.getDeclarationSignature(),
								Signature.getSignatureSimpleName(proposal.getSignature()),
								proposal.getCompletion(),
								proposal.getFlags(),
								proposal.getReplaceStart(),
								proposal.getReplaceEnd(),
								proposal.getRelevance());
					} else {
						this.requestor.acceptClass(
								proposal.getPackageName() == null ? CharOperation.NO_CHAR : proposal.getPackageName(),
								proposal.getTypeName(),
								proposal.getCompletion(),
								proposal.getFlags(),
								proposal.getReplaceStart(),
								proposal.getReplaceEnd(),
								proposal.getRelevance());
					}
				}
				break;
			case CompletionProposal.FIELD_REF:
				if(DECODE_SIGNATURE) {
					this.requestor.acceptField(
							Signature.getSignatureQualifier(proposal.getDeclarationSignature()),
							Signature.getSignatureSimpleName(proposal.getDeclarationSignature()),
							proposal.getName(),
							Signature.getSignatureQualifier(proposal.getSignature()),
							Signature.getSignatureSimpleName(proposal.getSignature()), 
							proposal.getCompletion(),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
					);
				} else {
					this.requestor.acceptField(
							proposal.getDeclarationPackageName() == null ? CharOperation.NO_CHAR : proposal.getDeclarationPackageName(),
							proposal.getDeclarationTypeName() == null ? CharOperation.NO_CHAR : proposal.getDeclarationTypeName(),
							proposal.getName(),
							proposal.getPackageName() == null ? CharOperation.NO_CHAR : proposal.getPackageName(),
							proposal.getTypeName() == null ? CharOperation.NO_CHAR : proposal.getTypeName(),
							proposal.getCompletion(),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
					);
				}
				break;
			case CompletionProposal.METHOD_REF:
				if(DECODE_SIGNATURE) {
					this.requestor.acceptMethod(
							Signature.getSignatureQualifier(proposal.getDeclarationSignature()),
							Signature.getSignatureSimpleName(proposal.getDeclarationSignature()),
							proposal.getName(),
							getParameterPackages(proposal.getSignature()),
							getParameterTypes(proposal.getSignature()),
							proposal.findParameterNames(null) == null ? CharOperation.NO_CHAR_CHAR : proposal.findParameterNames(null),
							Signature.getSignatureQualifier(Signature.getReturnType(proposal.getSignature())),
							Signature.getSignatureSimpleName(Signature.getReturnType(proposal.getSignature())),
							proposal.getCompletion(),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
						);
				} else {
					this.requestor.acceptMethod(
							proposal.getDeclarationPackageName() == null ? CharOperation.NO_CHAR : proposal.getDeclarationPackageName(),
							proposal.getDeclarationTypeName() == null ? CharOperation.NO_CHAR : proposal.getDeclarationTypeName(),
							proposal.getName(),
							proposal.getParameterPackageNames() == null ? CharOperation.NO_CHAR_CHAR : proposal.getParameterPackageNames(),
							proposal.getParameterTypeNames() == null ? CharOperation.NO_CHAR_CHAR : proposal.getParameterTypeNames(),
							proposal.findParameterNames(null) == null ? CharOperation.NO_CHAR_CHAR : proposal.findParameterNames(null),
							proposal.getPackageName() == null ? CharOperation.NO_CHAR : proposal.getPackageName(),
							proposal.getTypeName() == null ? CharOperation.NO_CHAR : proposal.getTypeName(),
							proposal.getCompletion(),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
					);
				}
				break;
			case CompletionProposal.METHOD_DECLARATION:
				if(DECODE_SIGNATURE) {
					this.requestor.acceptMethodDeclaration(
							Signature.getSignatureQualifier(proposal.getDeclarationSignature()),
							Signature.getSignatureSimpleName(proposal.getDeclarationSignature()),
							proposal.getName(),
							getParameterPackages(proposal.getSignature()),
							getParameterTypes(proposal.getSignature()),
							proposal.findParameterNames(null) == null ? CharOperation.NO_CHAR_CHAR : proposal.findParameterNames(null),
							Signature.getSignatureQualifier(Signature.getReturnType(proposal.getSignature())),
							Signature.getSignatureSimpleName(Signature.getReturnType(proposal.getSignature())),
							proposal.getCompletion(),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
					);
				} else {
					this.requestor.acceptMethodDeclaration(
							proposal.getDeclarationPackageName(),
							proposal.getDeclarationTypeName(),
							proposal.getName(),
							proposal.getParameterPackageNames() == null ? CharOperation.NO_CHAR_CHAR : proposal.getParameterPackageNames(),
							proposal.getParameterTypeNames() == null ? CharOperation.NO_CHAR_CHAR : proposal.getParameterTypeNames(),
							proposal.findParameterNames(null) == null ? CharOperation.NO_CHAR_CHAR : proposal.findParameterNames(null),
							proposal.getPackageName(),
							proposal.getTypeName(),
							proposal.getCompletion(),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
					);
				}
				break;
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
				if(DECODE_SIGNATURE) {
					this.requestor.acceptAnonymousType(
							Signature.getSignatureQualifier(proposal.getDeclarationSignature()),
							Signature.getSignatureSimpleName(proposal.getDeclarationSignature()), 
							getParameterPackages(proposal.getSignature()),
							getParameterTypes(proposal.getSignature()),
							proposal.findParameterNames(null) == null ? CharOperation.NO_CHAR_CHAR : proposal.findParameterNames(null),
							proposal.getCompletion(),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
						);
				} else {
					this.requestor.acceptAnonymousType(
						proposal.getDeclarationPackageName(),
						proposal.getDeclarationTypeName(),
						proposal.getParameterPackageNames() == null ? CharOperation.NO_CHAR_CHAR : proposal.getParameterPackageNames(),
						proposal.getParameterTypeNames() == null ? CharOperation.NO_CHAR_CHAR : proposal.getParameterTypeNames(),
						proposal.findParameterNames(null) == null ? CharOperation.NO_CHAR_CHAR : proposal.findParameterNames(null),
						proposal.getCompletion(),
						proposal.getFlags(),
						proposal.getReplaceStart(),
						proposal.getReplaceEnd(),
						proposal.getRelevance()
					);
				}
				break;
			case CompletionProposal.LABEL_REF :
				this.requestor.acceptLabel(
					proposal.getCompletion(),
					proposal.getReplaceStart(),
					proposal.getReplaceEnd(),
					proposal.getRelevance()
				);
				break;
			case CompletionProposal.LOCAL_VARIABLE_REF:
				if(DECODE_SIGNATURE) {
					this.requestor.acceptLocalVariable(
							proposal.getCompletion(),
							Signature.getSignatureQualifier(proposal.getSignature()),
							Signature.getSignatureSimpleName(proposal.getSignature()),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
					);
				} else {
					this.requestor.acceptLocalVariable(
							proposal.getCompletion(),
							proposal.getPackageName() == null ? CharOperation.NO_CHAR : proposal.getPackageName(),
							proposal.getTypeName(),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
					);
				}
				break;
			case CompletionProposal.VARIABLE_DECLARATION:
				if(DECODE_SIGNATURE) {
					this.requestor.acceptLocalVariable(
							proposal.getCompletion(),
							Signature.getSignatureQualifier(proposal.getSignature()),
							Signature.getSignatureSimpleName(proposal.getSignature()),
							proposal.getFlags(),
							proposal.getReplaceStart(),
							proposal.getReplaceEnd(),
							proposal.getRelevance()
						);
				} else {
					this.requestor.acceptLocalVariable(
						proposal.getCompletion(),
						proposal.getPackageName(),
						proposal.getTypeName(),
						proposal.getFlags(),
						proposal.getReplaceStart(),
						proposal.getReplaceEnd(),
						proposal.getRelevance()
					);
				}
				break;
			case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
				if(this.requestor instanceof IExtendedCompletionRequestor) {
					IExtendedCompletionRequestor r = (IExtendedCompletionRequestor) this.requestor;
					if(DECODE_SIGNATURE) {
						r.acceptPotentialMethodDeclaration(
								Signature.getSignatureQualifier(proposal.getDeclarationSignature()),
								Signature.getSignatureSimpleName(proposal.getDeclarationSignature()),
								proposal.getName(),
								proposal.getReplaceStart(),
								proposal.getReplaceEnd(),
								proposal.getRelevance()
						);
					} else {
						r.acceptPotentialMethodDeclaration(
								proposal.getDeclarationPackageName(),
								proposal.getDeclarationTypeName(),
								proposal.getName(),
								proposal.getReplaceStart(),
								proposal.getReplaceEnd(),
								proposal.getRelevance()
						);
					}
				}
				break;
				
		}
	}
	
	public void completionFailure(IProblem problem) {
		this.requestor.acceptError(problem);
	}
	
	private char[][] getParameterPackages(char[] methodSignature) {
		char[][] parameterQualifiedTypes = Signature.getParameterTypes(methodSignature);
		int length = parameterQualifiedTypes == null ? 0 : parameterQualifiedTypes.length;
		char[][] parameterPackages = new char[length][];
		for(int i = 0; i < length; i++) {
			parameterPackages[i] = Signature.getSignatureQualifier(parameterQualifiedTypes[i]);
		}

		return parameterPackages;
	}
	
	private char[][] getParameterTypes(char[] methodSignature) {
		char[][] parameterQualifiedTypes = Signature.getParameterTypes(methodSignature);
		int length = parameterQualifiedTypes == null ? 0 : parameterQualifiedTypes.length;
		char[][] parameterPackages = new char[length][];
		for(int i = 0; i < length; i++) {
			parameterPackages[i] = Signature.getSignatureSimpleName(parameterQualifiedTypes[i]);
		}

		return parameterPackages;
	}
}
