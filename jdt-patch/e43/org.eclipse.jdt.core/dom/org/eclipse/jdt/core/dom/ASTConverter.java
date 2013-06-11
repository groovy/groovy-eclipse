/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 393719 - [compiler] inconsistent warnings on iteration variables
 *******************************************************************************/

package org.eclipse.jdt.core.dom;
// GROOVY PATCHED

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.JavadocArgumentExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.dom.SourceRangeVerifier;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Internal class for converting internal compiler ASTs into public ASTs.
 */
class ASTConverter {

	protected AST ast;
	protected Comment[] commentsTable;
	char[] compilationUnitSource;
	int compilationUnitSourceLength;
	protected DocCommentParser docParser;
	// comments
	protected boolean insideComments;
	protected IProgressMonitor monitor;
	protected Set pendingNameScopeResolution;
	protected Set pendingThisExpressionScopeResolution;
	protected boolean resolveBindings;
	Scanner scanner;
	private DefaultCommentMapper commentMapper;
	
	// GROOVY start
	private boolean scannerUsable = true;
	// GROOVY end

	public ASTConverter(Map options, boolean resolveBindings, IProgressMonitor monitor) {
		this.resolveBindings = resolveBindings;
		Object sourceModeSetting = options.get(JavaCore.COMPILER_SOURCE);
		long sourceLevel = CompilerOptions.versionToJdkLevel(sourceModeSetting);
		if (sourceLevel == 0) {
			// unknown sourceModeSetting
			sourceLevel = ClassFileConstants.JDK1_3;
		}
		this.scanner = new Scanner(
			true /*comment*/,
			false /*whitespace*/,
			false /*nls*/,
			sourceLevel /*sourceLevel*/,
			null /*taskTags*/,
			null/*taskPriorities*/,
			true/*taskCaseSensitive*/);
		this.monitor = monitor;
		this.insideComments = JavaCore.ENABLED.equals(options.get(JavaCore.COMPILER_DOC_COMMENT_SUPPORT));
	}

	protected void adjustSourcePositionsForParent(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		int start = expression.sourceStart;
		int end = expression.sourceEnd;
		int leftParentCount = 1;
		int rightParentCount = 0;
		this.scanner.resetTo(start, end);
		try {
			int token = this.scanner.getNextToken();
			expression.sourceStart = this.scanner.currentPosition;
			boolean stop = false;
			while (!stop && ((token  = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF)) {
				switch(token) {
					case TerminalTokens.TokenNameLPAREN:
						leftParentCount++;
						break;
					case TerminalTokens.TokenNameRPAREN:
						rightParentCount++;
						if (rightParentCount == leftParentCount) {
							// we found the matching parenthesis
							stop = true;
						}
				}
			}
			expression.sourceEnd = this.scanner.startPosition - 1;
		} catch(InvalidInputException e) {
			// ignore
		}
	}

	protected void buildBodyDeclarations(
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration,
			AbstractTypeDeclaration typeDecl,
			boolean isInterface) {
		// add body declaration in the lexical order
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] members = typeDeclaration.memberTypes;
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = typeDeclaration.fields;
		org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration[] methods = typeDeclaration.methods;

		int fieldsLength = fields == null? 0 : fields.length;
		int methodsLength = methods == null? 0 : methods.length;
		int membersLength = members == null ? 0 : members.length;
		int fieldsIndex = 0;
		int methodsIndex = 0;
		int membersIndex = 0;

		while ((fieldsIndex < fieldsLength)
			|| (membersIndex < membersLength)
			|| (methodsIndex < methodsLength)) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration nextFieldDeclaration = null;
			org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration nextMethodDeclaration = null;
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration nextMemberDeclaration = null;

			int position = Integer.MAX_VALUE;
			int nextDeclarationType = -1;
			if (fieldsIndex < fieldsLength) {
				nextFieldDeclaration = fields[fieldsIndex];
				if (nextFieldDeclaration.declarationSourceStart < position) {
					position = nextFieldDeclaration.declarationSourceStart;
					nextDeclarationType = 0; // FIELD
				}
			}
			if (methodsIndex < methodsLength) {
				nextMethodDeclaration = methods[methodsIndex];
				if (nextMethodDeclaration.declarationSourceStart < position) {
					position = nextMethodDeclaration.declarationSourceStart;
					nextDeclarationType = 1; // METHOD
				}
			}
			if (membersIndex < membersLength) {
				nextMemberDeclaration = members[membersIndex];
				if (nextMemberDeclaration.declarationSourceStart < position) {
					position = nextMemberDeclaration.declarationSourceStart;
					nextDeclarationType = 2; // MEMBER
				}
			}
			switch (nextDeclarationType) {
				case 0 :
					if (nextFieldDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT) {
						typeDecl.bodyDeclarations().add(convert(nextFieldDeclaration));
					} else {
						checkAndAddMultipleFieldDeclaration(fields, fieldsIndex, typeDecl.bodyDeclarations());
					}
					fieldsIndex++;
					break;
				case 1 :
					methodsIndex++;
					if (!nextMethodDeclaration.isDefaultConstructor() && !nextMethodDeclaration.isClinit()) {
						// GROOVY start - a little ugly, but allows the conversion of the method declaration
						// to know if it is occurring within a pure java type or not
						boolean originalValue = this.scannerUsable;
						try {
							this.scannerUsable = typeDeclaration.isScannerUsableOnThisDeclaration();
							// GROOVY end
							typeDecl.bodyDeclarations().add(convert(isInterface, nextMethodDeclaration));
						// GROOVY start
						} finally {
							this.scannerUsable = originalValue;
						}
						// GROOVY end
					}
					break;
				case 2 :
					membersIndex++;
					ASTNode node = convert(nextMemberDeclaration);
					if (node == null) {
						typeDecl.setFlags(typeDecl.getFlags() | ASTNode.MALFORMED);
					} else {
						typeDecl.bodyDeclarations().add(node);
					}
			}
		}
		// Convert javadoc
		convert(typeDeclaration.javadoc, typeDecl);
	}

	protected void buildBodyDeclarations(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration enumDeclaration2, EnumDeclaration enumDeclaration) {
		// add body declaration in the lexical order
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] members = enumDeclaration2.memberTypes;
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = enumDeclaration2.fields;
		org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration[] methods = enumDeclaration2.methods;

		int fieldsLength = fields == null? 0 : fields.length;
		int methodsLength = methods == null? 0 : methods.length;
		int membersLength = members == null ? 0 : members.length;
		int fieldsIndex = 0;
		int methodsIndex = 0;
		int membersIndex = 0;

		while ((fieldsIndex < fieldsLength)
			|| (membersIndex < membersLength)
			|| (methodsIndex < methodsLength)) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration nextFieldDeclaration = null;
			org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration nextMethodDeclaration = null;
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration nextMemberDeclaration = null;

			int position = Integer.MAX_VALUE;
			int nextDeclarationType = -1;
			if (fieldsIndex < fieldsLength) {
				nextFieldDeclaration = fields[fieldsIndex];
				if (nextFieldDeclaration.declarationSourceStart < position) {
					position = nextFieldDeclaration.declarationSourceStart;
					nextDeclarationType = 0; // FIELD
				}
			}
			if (methodsIndex < methodsLength) {
				nextMethodDeclaration = methods[methodsIndex];
				if (nextMethodDeclaration.declarationSourceStart < position) {
					position = nextMethodDeclaration.declarationSourceStart;
					nextDeclarationType = 1; // METHOD
				}
			}
			if (membersIndex < membersLength) {
				nextMemberDeclaration = members[membersIndex];
				if (nextMemberDeclaration.declarationSourceStart < position) {
					position = nextMemberDeclaration.declarationSourceStart;
					nextDeclarationType = 2; // MEMBER
				}
			}
			switch (nextDeclarationType) {
				case 0 :
					if (nextFieldDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT) {
						enumDeclaration.enumConstants().add(convert(nextFieldDeclaration));
					} else {
						checkAndAddMultipleFieldDeclaration(fields, fieldsIndex, enumDeclaration.bodyDeclarations());
					}
					fieldsIndex++;
					break;
				case 1 :
					methodsIndex++;
					if (!nextMethodDeclaration.isDefaultConstructor() && !nextMethodDeclaration.isClinit()) {
						enumDeclaration.bodyDeclarations().add(convert(false, nextMethodDeclaration));
					}
					break;
				case 2 :
					membersIndex++;
					enumDeclaration.bodyDeclarations().add(convert(nextMemberDeclaration));
					break;
			}
		}
		convert(enumDeclaration2.javadoc, enumDeclaration);
	}

	protected void buildBodyDeclarations(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration expression, AnonymousClassDeclaration anonymousClassDeclaration) {
		// add body declaration in the lexical order
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] members = expression.memberTypes;
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = expression.fields;
		org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration[] methods = expression.methods;

		int fieldsLength = fields == null? 0 : fields.length;
		int methodsLength = methods == null? 0 : methods.length;
		int membersLength = members == null ? 0 : members.length;
		int fieldsIndex = 0;
		int methodsIndex = 0;
		int membersIndex = 0;

		while ((fieldsIndex < fieldsLength)
			|| (membersIndex < membersLength)
			|| (methodsIndex < methodsLength)) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration nextFieldDeclaration = null;
			org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration nextMethodDeclaration = null;
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration nextMemberDeclaration = null;

			int position = Integer.MAX_VALUE;
			int nextDeclarationType = -1;
			if (fieldsIndex < fieldsLength) {
				nextFieldDeclaration = fields[fieldsIndex];
				if (nextFieldDeclaration.declarationSourceStart < position) {
					position = nextFieldDeclaration.declarationSourceStart;
					nextDeclarationType = 0; // FIELD
				}
			}
			if (methodsIndex < methodsLength) {
				nextMethodDeclaration = methods[methodsIndex];
				if (nextMethodDeclaration.declarationSourceStart < position) {
					position = nextMethodDeclaration.declarationSourceStart;
					nextDeclarationType = 1; // METHOD
				}
			}
			if (membersIndex < membersLength) {
				nextMemberDeclaration = members[membersIndex];
				if (nextMemberDeclaration.declarationSourceStart < position) {
					position = nextMemberDeclaration.declarationSourceStart;
					nextDeclarationType = 2; // MEMBER
				}
			}
			switch (nextDeclarationType) {
				case 0 :
					if (nextFieldDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT) {
						anonymousClassDeclaration.bodyDeclarations().add(convert(nextFieldDeclaration));
					} else {
						checkAndAddMultipleFieldDeclaration(fields, fieldsIndex, anonymousClassDeclaration.bodyDeclarations());
					}
					fieldsIndex++;
					break;
				case 1 :
					methodsIndex++;
					if (!nextMethodDeclaration.isDefaultConstructor() && !nextMethodDeclaration.isClinit()) {
						anonymousClassDeclaration.bodyDeclarations().add(convert(false, nextMethodDeclaration));
					}
					break;
				case 2 :
					membersIndex++;
					ASTNode node = convert(nextMemberDeclaration);
					if (node == null) {
						anonymousClassDeclaration.setFlags(anonymousClassDeclaration.getFlags() | ASTNode.MALFORMED);
					} else {
						anonymousClassDeclaration.bodyDeclarations().add(node);
					}
			}
		}
	}

	/**
	 * @param compilationUnit
	 * @param comments
	 */
	void buildCommentsTable(CompilationUnit compilationUnit, int[][] comments) {
		// Build comment table
		this.commentsTable = new Comment[comments.length];
		int nbr = 0;
		for (int i = 0; i < comments.length; i++) {
			Comment comment = createComment(comments[i]);
			if (comment != null) {
				comment.setAlternateRoot(compilationUnit);
				this.commentsTable[nbr++] = comment;
			}
		}
		// Resize table if  necessary
		if (nbr<comments.length) {
			Comment[] newCommentsTable = new Comment[nbr];
			System.arraycopy(this.commentsTable, 0, newCommentsTable, 0, nbr);
			this.commentsTable = newCommentsTable;
		}
		compilationUnit.setCommentTable(this.commentsTable);
	}

	protected void checkAndAddMultipleFieldDeclaration(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields, int index, List bodyDeclarations) {
		if (fields[index] instanceof org.eclipse.jdt.internal.compiler.ast.Initializer) {
			org.eclipse.jdt.internal.compiler.ast.Initializer oldInitializer = (org.eclipse.jdt.internal.compiler.ast.Initializer) fields[index];
			Initializer initializer = new Initializer(this.ast);
			initializer.setBody(convert(oldInitializer.block));
			setModifiers(initializer, oldInitializer);
			initializer.setSourceRange(oldInitializer.declarationSourceStart, oldInitializer.sourceEnd - oldInitializer.declarationSourceStart + 1);
			// The javadoc comment is now got from list store in compilation unit declaration
			convert(oldInitializer.javadoc, initializer);
			bodyDeclarations.add(initializer);
			return;
		}
		if (index > 0 && fields[index - 1].declarationSourceStart == fields[index].declarationSourceStart) {
			// we have a multiple field declaration
			// We retrieve the existing fieldDeclaration to add the new VariableDeclarationFragment
			FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclarations.get(bodyDeclarations.size() - 1);
			fieldDeclaration.fragments().add(convertToVariableDeclarationFragment(fields[index]));
		} else {
			// we can create a new FieldDeclaration
			bodyDeclarations.add(convertToFieldDeclaration(fields[index]));
		}
	}

	protected void checkAndAddMultipleLocalDeclaration(org.eclipse.jdt.internal.compiler.ast.Statement[] stmts, int index, List blockStatements) {
		if (index > 0
				&& stmts[index - 1] instanceof org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.LocalDeclaration local1 = (org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) stmts[index - 1];
			org.eclipse.jdt.internal.compiler.ast.LocalDeclaration local2 = (org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) stmts[index];
			if (local1.declarationSourceStart == local2.declarationSourceStart) {
				// we have a multiple local declarations
				// We retrieve the existing VariableDeclarationStatement to add the new VariableDeclarationFragment
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) blockStatements.get(blockStatements.size() - 1);
				variableDeclarationStatement.fragments().add(convertToVariableDeclarationFragment((org.eclipse.jdt.internal.compiler.ast.LocalDeclaration)stmts[index]));
			} else {
				// we can create a new FieldDeclaration
				blockStatements.add(convertToVariableDeclarationStatement((org.eclipse.jdt.internal.compiler.ast.LocalDeclaration)stmts[index]));
			}
		} else {
			// we can create a new FieldDeclaration
			blockStatements.add(convertToVariableDeclarationStatement((org.eclipse.jdt.internal.compiler.ast.LocalDeclaration)stmts[index]));
		}
	}

	protected void checkCanceled() {
		if (this.monitor != null && this.monitor.isCanceled())
			throw new OperationCanceledException();
	}

	protected void completeRecord(ArrayType arrayType, org.eclipse.jdt.internal.compiler.ast.ASTNode astNode) {
		ArrayType array = arrayType;
		int dimensions = array.getDimensions();
		for (int i = 0; i < dimensions; i++) {
			Type componentType = array.getComponentType();
			this.recordNodes(componentType, astNode);
			if (componentType.isArrayType()) {
				array = (ArrayType) componentType;
			}
		}
	}

	public ASTNode convert(boolean isInterface, org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration methodDeclaration) {
		checkCanceled();
		if (methodDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration) methodDeclaration);
		}
		MethodDeclaration methodDecl = new MethodDeclaration(this.ast);
		setModifiers(methodDecl, methodDeclaration);
		boolean isConstructor = methodDeclaration.isConstructor();
		methodDecl.setConstructor(isConstructor);
		final SimpleName methodName = new SimpleName(this.ast);
		methodName.internalSetIdentifier(new String(methodDeclaration.selector));
		int start = methodDeclaration.sourceStart;
		// GROOVY start
		// why does this do what it does?
		/* old {
		int end = retrieveIdentifierEndPosition(start, methodDeclaration.sourceEnd);
		} new */
 		int end = (scannerAvailable(methodDeclaration.scope)?retrieveIdentifierEndPosition(start, methodDeclaration.sourceEnd):methodDeclaration.sourceEnd);
		// GROOVY end
		methodName.setSourceRange(start, end - start + 1);
		methodDecl.setName(methodName);
		org.eclipse.jdt.internal.compiler.ast.TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
		int methodHeaderEnd = methodDeclaration.sourceEnd;
		int thrownExceptionsLength = thrownExceptions == null ? 0 : thrownExceptions.length;
		if (thrownExceptionsLength > 0) {
			Name thrownException;
			int i = 0;
			do {
				thrownException = convert(thrownExceptions[i++]);
				methodDecl.thrownExceptions().add(thrownException);
			} while (i < thrownExceptionsLength);
			methodHeaderEnd = thrownException.getStartPosition() + thrownException.getLength();
		}
		org.eclipse.jdt.internal.compiler.ast.Argument[] parameters = methodDeclaration.arguments;
		int parametersLength = parameters == null ? 0 : parameters.length;
		if (parametersLength > 0) {
			SingleVariableDeclaration parameter;
			int i = 0;
			do {
			    // GROOVY start
			    // make sure the scope is available just in case it is necessary for varargs
		        // new code
			    BlockScope origScope = null;
			    if (parameters[i].binding != null) {
			        origScope = parameters[i].binding.declaringScope;
			        parameters[i].binding.declaringScope = methodDeclaration.scope;
			    }
		        // GROOVY end
			    
				parameter = convert(parameters[i++]);
                
				// GROOVY start
                // unset the scope
                // new code
				if (parameters[i-1].binding != null) {
				    parameters[i-1].binding.declaringScope = origScope;
				}
                // GROOVY end
				methodDecl.parameters().add(parameter);
			} while (i < parametersLength);
			if (thrownExceptionsLength == 0) {
				methodHeaderEnd = parameter.getStartPosition() + parameter.getLength();
			}
		}
		org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall explicitConstructorCall = null;
		if (isConstructor) {
			if (isInterface) {
				// interface cannot have a constructor
				methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
			}
			org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration constructorDeclaration = (org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration) methodDeclaration;
			explicitConstructorCall = constructorDeclaration.constructorCall;
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					// set the return type to VOID
					PrimitiveType returnType = new PrimitiveType(this.ast);
					returnType.setPrimitiveTypeCode(PrimitiveType.VOID);
					returnType.setSourceRange(methodDeclaration.sourceStart, 0);
					methodDecl.internalSetReturnType(returnType);
					break;
				default :
					methodDecl.setReturnType2(null);
			}
		} else if (methodDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.MethodDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.MethodDeclaration method = (org.eclipse.jdt.internal.compiler.ast.MethodDeclaration) methodDeclaration;
			org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference = method.returnType;
			if (typeReference != null) {
				Type returnType = convertType(typeReference);
				// get the positions of the right parenthesis
				int rightParenthesisPosition = retrieveEndOfRightParenthesisPosition(end, method.bodyEnd);
				int extraDimensions = retrieveExtraDimension(rightParenthesisPosition, method.bodyEnd);
				methodDecl.setExtraDimensions(extraDimensions);
				setTypeForMethodDeclaration(methodDecl, returnType, extraDimensions);
			} else {
				// no return type for a method that is not a constructor
				methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						break;
					default :
						methodDecl.setReturnType2(null);
				}
			}
		}
		int declarationSourceStart = methodDeclaration.declarationSourceStart;
		int bodyEnd = methodDeclaration.bodyEnd;
		methodDecl.setSourceRange(declarationSourceStart, bodyEnd - declarationSourceStart + 1);
		int declarationSourceEnd = methodDeclaration.declarationSourceEnd;
		int rightBraceOrSemiColonPositionStart = bodyEnd == declarationSourceEnd ? bodyEnd : bodyEnd + 1;
		int closingPosition = retrieveRightBraceOrSemiColonPosition(rightBraceOrSemiColonPositionStart, declarationSourceEnd);
		if (closingPosition != -1) {
			int startPosition = methodDecl.getStartPosition();
			methodDecl.setSourceRange(startPosition, closingPosition - startPosition + 1);

			org.eclipse.jdt.internal.compiler.ast.Statement[] statements = methodDeclaration.statements;

			start = retrieveStartBlockPosition(methodHeaderEnd, methodDeclaration.bodyStart);
			if (start == -1) start = methodDeclaration.bodyStart; // use recovery position for body start
			end = retrieveRightBrace(methodDeclaration.bodyEnd, declarationSourceEnd);
			Block block = null;
			if (start != -1 && end != -1) {
				/*
				 * start or end can be equal to -1 if we have an interface's method.
				 */
				block = new Block(this.ast);
				block.setSourceRange(start, closingPosition - start + 1);
				methodDecl.setBody(block);
			}
			if (block != null && (statements != null || explicitConstructorCall != null)) {
				if (explicitConstructorCall != null && explicitConstructorCall.accessMode != org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall.ImplicitSuper) {
					block.statements().add(convert(explicitConstructorCall));
				}
				int statementsLength = statements == null ? 0 : statements.length;
				for (int i = 0; i < statementsLength; i++) {
					if (statements[i] instanceof org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
						checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
					} else {
						final Statement statement = convert(statements[i]);
						if (statement != null) {
							block.statements().add(statement);
						}
					}
				}
			}
			if (block != null
					&& (Modifier.isAbstract(methodDecl.getModifiers())
							|| Modifier.isNative(methodDecl.getModifiers())
							|| isInterface)) {
				methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
			}
		} else {
			// syntax error in this method declaration
			methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
			if (!methodDeclaration.isNative() && !methodDeclaration.isAbstract()) {
				start = retrieveStartBlockPosition(methodHeaderEnd, bodyEnd);
				if (start == -1) start = methodDeclaration.bodyStart; // use recovery position for body start
				end = methodDeclaration.bodyEnd;
				// try to get the best end position
				CategorizedProblem[] problems = methodDeclaration.compilationResult().problems;
				if (problems != null) {
					for (int i = 0, max = methodDeclaration.compilationResult().problemCount; i < max; i++) {
						CategorizedProblem currentProblem = problems[i];
						if (currentProblem.getSourceStart() == start && currentProblem.getID() == IProblem.ParsingErrorInsertToComplete) {
							end = currentProblem.getSourceEnd();
							break;
						}
					}
				}
				int startPosition = methodDecl.getStartPosition();
				methodDecl.setSourceRange(startPosition, end - startPosition + 1);
				if (start != -1 && end != -1) {
					/*
					 * start or end can be equal to -1 if we have an interface's method.
					 */
					Block block = new Block(this.ast);
					block.setSourceRange(start, end - start + 1);
					methodDecl.setBody(block);
				}
			}
		}

		org.eclipse.jdt.internal.compiler.ast.TypeParameter[] typeParameters = methodDeclaration.typeParameters();
		if (typeParameters != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
					break;
				default :
					for (int i = 0, max = typeParameters.length; i < max; i++) {
						methodDecl.typeParameters().add(convert(typeParameters[i]));
					}
			}
		}

		// The javadoc comment is now got from list store in compilation unit declaration
		convert(methodDeclaration.javadoc, methodDecl);
		if (this.resolveBindings) {
			recordNodes(methodDecl, methodDeclaration);
			recordNodes(methodName, methodDeclaration);
			methodDecl.resolveBinding();
		}
		return methodDecl;
	}

	// GROOVY start
	private boolean scannerAvailable(Scope scope) {
		if (!this.scannerUsable) {
			return false;
		}
		if (scope!=null) {
			CompilationUnitScope cuScope = scope.compilationUnitScope();
			if (cuScope!=null) {
				return cuScope.scannerAvailable();	
			}
		}
		return true;
	}
	// GROOVY end

	
	public ClassInstanceCreation convert(org.eclipse.jdt.internal.compiler.ast.AllocationExpression expression) {
		ClassInstanceCreation classInstanceCreation = new ClassInstanceCreation(this.ast);
		if (this.resolveBindings) {
			recordNodes(classInstanceCreation, expression);
		}
		if (expression.typeArguments != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					classInstanceCreation.setFlags(classInstanceCreation.getFlags() | ASTNode.MALFORMED);
					break;
				default :
					for (int i = 0, max = expression.typeArguments.length; i < max; i++) {
						classInstanceCreation.typeArguments().add(convertType(expression.typeArguments[i]));
					}
			}
		}
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				classInstanceCreation.internalSetName(convert(expression.type));
				break;
			default :
				classInstanceCreation.setType(convertType(expression.type));
		}
		classInstanceCreation.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.arguments;
		if (arguments != null) {
			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				classInstanceCreation.arguments().add(convert(arguments[i]));
			}
		}
		return classInstanceCreation;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression expression) {
		InfixExpression infixExpression = new InfixExpression(this.ast);
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		if (this.resolveBindings) {
			this.recordNodes(infixExpression, expression);
		}
		final int expressionOperatorID = (expression.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT;
		if (expression.left instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression
				&& ((expression.left.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0)) {
			// create an extended string literal equivalent => use the extended operands list
			infixExpression.extendedOperands().add(convert(expression.right));
			org.eclipse.jdt.internal.compiler.ast.Expression leftOperand = expression.left;
			org.eclipse.jdt.internal.compiler.ast.Expression rightOperand = null;
			do {
				rightOperand = ((org.eclipse.jdt.internal.compiler.ast.BinaryExpression) leftOperand).right;
				if ((((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) != expressionOperatorID
							&& ((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0))
					 || ((rightOperand instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression
				 			&& ((rightOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) != expressionOperatorID)
							&& ((rightOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0))) {
				 	List extendedOperands = infixExpression.extendedOperands();
				 	InfixExpression temp = new InfixExpression(this.ast);
					if (this.resolveBindings) {
						this.recordNodes(temp, expression);
					}
				 	temp.setOperator(getOperatorFor(expressionOperatorID));
				 	Expression leftSide = convert(leftOperand);
					temp.setLeftOperand(leftSide);
					temp.setSourceRange(leftSide.getStartPosition(), leftSide.getLength());
					int size = extendedOperands.size();
				 	for (int i = 0; i < size - 1; i++) {
				 		Expression expr = temp;
				 		temp = new InfixExpression(this.ast);

						if (this.resolveBindings) {
							this.recordNodes(temp, expression);
						}
				 		temp.setLeftOperand(expr);
					 	temp.setOperator(getOperatorFor(expressionOperatorID));
						temp.setSourceRange(expr.getStartPosition(), expr.getLength());
				 	}
				 	infixExpression = temp;
				 	for (int i = 0; i < size; i++) {
				 		Expression extendedOperand = (Expression) extendedOperands.remove(size - 1 - i);
				 		temp.setRightOperand(extendedOperand);
				 		int startPosition = temp.getLeftOperand().getStartPosition();
				 		temp.setSourceRange(startPosition, extendedOperand.getStartPosition() + extendedOperand.getLength() - startPosition);
				 		if (temp.getLeftOperand().getNodeType() == ASTNode.INFIX_EXPRESSION) {
				 			temp = (InfixExpression) temp.getLeftOperand();
				 		}
				 	}
					int startPosition = infixExpression.getLeftOperand().getStartPosition();
					infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
					if (this.resolveBindings) {
						this.recordNodes(infixExpression, expression);
					}
					return infixExpression;
				}
				infixExpression.extendedOperands().add(0, convert(rightOperand));
				leftOperand = ((org.eclipse.jdt.internal.compiler.ast.BinaryExpression) leftOperand).left;
			} while (leftOperand instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression && ((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0));
			Expression leftExpression = convert(leftOperand);
			infixExpression.setLeftOperand(leftExpression);
			infixExpression.setRightOperand((Expression)infixExpression.extendedOperands().remove(0));
			int startPosition = leftExpression.getStartPosition();
			infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
			return infixExpression;
		}
		Expression leftExpression = convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(convert(expression.right));
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		int startPosition = leftExpression.getStartPosition();
		infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		return infixExpression;
	}

	private AnnotationTypeDeclaration convertToAnnotationDeclaration(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		checkCanceled();
		if (this.scanner.sourceLevel < ClassFileConstants.JDK1_5) return null;
		AnnotationTypeDeclaration typeDecl = this.ast.newAnnotationTypeDeclaration();
		setModifiers(typeDecl, typeDeclaration);
		final SimpleName typeName = new SimpleName(this.ast);
		typeName.internalSetIdentifier(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(typeName);
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.bodyEnd - typeDeclaration.declarationSourceStart + 1);

		buildBodyDeclarations(typeDeclaration, typeDecl, false);
		// The javadoc comment is now got from list store in compilation unit declaration
		if (this.resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
			recordNodes(typeName, typeDeclaration);
			typeDecl.resolveBinding();
		}
		return typeDecl;
	}

	public ASTNode convert(org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration annotationTypeMemberDeclaration) {
		checkCanceled();
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			return null;
		}
		AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration2 = new AnnotationTypeMemberDeclaration(this.ast);
		setModifiers(annotationTypeMemberDeclaration2, annotationTypeMemberDeclaration);
		final SimpleName methodName = new SimpleName(this.ast);
		methodName.internalSetIdentifier(new String(annotationTypeMemberDeclaration.selector));
		int start = annotationTypeMemberDeclaration.sourceStart;
		int end = retrieveIdentifierEndPosition(start, annotationTypeMemberDeclaration.sourceEnd);
		methodName.setSourceRange(start, end - start + 1);
		annotationTypeMemberDeclaration2.setName(methodName);
		org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference = annotationTypeMemberDeclaration.returnType;
		if (typeReference != null) {
			Type returnType = convertType(typeReference);
			setTypeForMethodDeclaration(annotationTypeMemberDeclaration2, returnType, 0);
		}
		int declarationSourceStart = annotationTypeMemberDeclaration.declarationSourceStart;
		int declarationSourceEnd = annotationTypeMemberDeclaration.bodyEnd;
		annotationTypeMemberDeclaration2.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
		// The javadoc comment is now got from list store in compilation unit declaration
		convert(annotationTypeMemberDeclaration.javadoc, annotationTypeMemberDeclaration2);
		org.eclipse.jdt.internal.compiler.ast.Expression memberValue = annotationTypeMemberDeclaration.defaultValue;
		if (memberValue != null) {
			annotationTypeMemberDeclaration2.setDefault(convert(memberValue));
		}
		if (this.resolveBindings) {
			recordNodes(annotationTypeMemberDeclaration2, annotationTypeMemberDeclaration);
			recordNodes(methodName, annotationTypeMemberDeclaration);
			annotationTypeMemberDeclaration2.resolveBinding();
		}
		return annotationTypeMemberDeclaration2;
	}

	public SingleVariableDeclaration convert(org.eclipse.jdt.internal.compiler.ast.Argument argument) {
		SingleVariableDeclaration variableDecl = new SingleVariableDeclaration(this.ast);
		setModifiers(variableDecl, argument);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(argument.name));
		int start = argument.sourceStart;
		int nameEnd = argument.sourceEnd;
		name.setSourceRange(start, nameEnd - start + 1);
		variableDecl.setName(name);
		final int typeSourceEnd = argument.type.sourceEnd;
		final int extraDimensions = retrieveExtraDimension(nameEnd + 1, typeSourceEnd);
		variableDecl.setExtraDimensions(extraDimensions);
		final boolean isVarArgs = argument.isVarArgs();
        // GROOVY start
		// Do not try to change source ends for var args.  Groovy assumes that
		// all methods that have an array as the last param are varargs
        /* old {
        if (isVarArgs && extraDimensions == 0) {
        } new */
		if (argument.binding != null && scannerAvailable(argument.binding.declaringScope) && isVarArgs && extraDimensions == 0) {
		    // GROOVY end
			// remove the ellipsis from the type source end
			argument.type.sourceEnd = retrieveEllipsisStartPosition(argument.type.sourceStart, typeSourceEnd);
		}
		Type type = convertType(argument.type);
		int typeEnd = type.getStartPosition() + type.getLength() - 1;
		int rightEnd = Math.max(typeEnd, argument.declarationSourceEnd);
		/*
		 * There is extra work to do to set the proper type positions
		 * See PR http://bugs.eclipse.org/bugs/show_bug.cgi?id=23284
		 */
		if (isVarArgs) {
			setTypeForSingleVariableDeclaration(variableDecl, type, extraDimensions + 1);
			if (extraDimensions != 0) {
				variableDecl.setFlags(variableDecl.getFlags() | ASTNode.MALFORMED);
			}
		} else {
			setTypeForSingleVariableDeclaration(variableDecl, type, extraDimensions);
		}
		variableDecl.setSourceRange(argument.declarationSourceStart, rightEnd - argument.declarationSourceStart + 1);

		if (isVarArgs) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					variableDecl.setFlags(variableDecl.getFlags() | ASTNode.MALFORMED);
					break;
				default :
					variableDecl.setVarargs(true);
			}
		}
		if (this.resolveBindings) {
			recordNodes(name, argument);
			recordNodes(variableDecl, argument);
			variableDecl.resolveBinding();
		}
		return variableDecl;
	}


	public Annotation convert(org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
		if (annotation instanceof org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation) {
			return convert((org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation) annotation);
		} else if (annotation instanceof org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) {
			return convert((org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) annotation);
		} else {
			return convert((org.eclipse.jdt.internal.compiler.ast.NormalAnnotation) annotation);
		}
	}

	public ArrayCreation convert(org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression expression) {
		ArrayCreation arrayCreation = new ArrayCreation(this.ast);
		if (this.resolveBindings) {
			recordNodes(arrayCreation, expression);
		}
		arrayCreation.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression[] dimensions = expression.dimensions;

		int dimensionsLength = dimensions.length;
		for (int i = 0; i < dimensionsLength; i++) {
			if (dimensions[i] != null) {
				Expression dimension = convert(dimensions[i]);
				if (this.resolveBindings) {
					recordNodes(dimension, dimensions[i]);
				}
				arrayCreation.dimensions().add(dimension);
			}
		}
		Type type = convertType(expression.type);
		if (this.resolveBindings) {
			recordNodes(type, expression.type);
		}
		ArrayType arrayType = null;
		if (type.isArrayType()) {
			arrayType = (ArrayType) type;
		} else {
			arrayType = this.ast.newArrayType(type, dimensionsLength);
			if (this.resolveBindings) {
				completeRecord(arrayType, expression);
			}
			int start = type.getStartPosition();
			int end = type.getStartPosition() + type.getLength();
			int previousSearchStart = end - 1;
			ArrayType componentType = (ArrayType) type.getParent();
			for (int i = 0; i < dimensionsLength; i++) {
				previousSearchStart = retrieveRightBracketPosition(previousSearchStart + 1, this.compilationUnitSourceLength);
				componentType.setSourceRange(start, previousSearchStart - start + 1);
				componentType = (ArrayType) componentType.getParent();
			}
		}
		arrayCreation.setType(arrayType);
		if (this.resolveBindings) {
			recordNodes(arrayType, expression);
		}
		if (expression.initializer != null) {
			arrayCreation.setInitializer(convert(expression.initializer));
		}
		return arrayCreation;
	}

	public ArrayInitializer convert(org.eclipse.jdt.internal.compiler.ast.ArrayInitializer expression) {
		ArrayInitializer arrayInitializer = new ArrayInitializer(this.ast);
		if (this.resolveBindings) {
			recordNodes(arrayInitializer, expression);
		}
		arrayInitializer.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression[] expressions = expression.expressions;
		if (expressions != null) {
			int length = expressions.length;
			for (int i = 0; i < length; i++) {
				Expression expr = convert(expressions[i]);
				if (this.resolveBindings) {
					recordNodes(expr, expressions[i]);
				}
				arrayInitializer.expressions().add(expr);
			}
		}
		return arrayInitializer;
	}

	public ArrayAccess convert(org.eclipse.jdt.internal.compiler.ast.ArrayReference reference) {
		ArrayAccess arrayAccess = new ArrayAccess(this.ast);
		if (this.resolveBindings) {
			recordNodes(arrayAccess, reference);
		}
		arrayAccess.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
		arrayAccess.setArray(convert(reference.receiver));
		arrayAccess.setIndex(convert(reference.position));
		return arrayAccess;
	}

	public AssertStatement convert(org.eclipse.jdt.internal.compiler.ast.AssertStatement statement) {
		AssertStatement assertStatement = new AssertStatement(this.ast);
		final Expression assertExpression = convert(statement.assertExpression);
		Expression searchingNode = assertExpression;
		assertStatement.setExpression(assertExpression);
		org.eclipse.jdt.internal.compiler.ast.Expression exceptionArgument = statement.exceptionArgument;
		if (exceptionArgument != null) {
			final Expression exceptionMessage = convert(exceptionArgument);
			assertStatement.setMessage(exceptionMessage);
			searchingNode = exceptionMessage;
		}
		int start = statement.sourceStart;
		int sourceEnd = retrieveSemiColonPosition(searchingNode);
		if (sourceEnd == -1) {
			sourceEnd = searchingNode.getStartPosition() + searchingNode.getLength() - 1;
			assertStatement.setSourceRange(start, sourceEnd - start + 1);
		} else {
			assertStatement.setSourceRange(start, sourceEnd - start + 1);
		}
		return assertStatement;
	}

	public Assignment convert(org.eclipse.jdt.internal.compiler.ast.Assignment expression) {
		Assignment assignment = new Assignment(this.ast);
		if (this.resolveBindings) {
			recordNodes(assignment, expression);
		}
		Expression lhs = convert(expression.lhs);
		assignment.setLeftHandSide(lhs);
		assignment.setOperator(Assignment.Operator.ASSIGN);
		Expression rightHandSide = convert(expression.expression);
		assignment.setRightHandSide(rightHandSide);
		int start = lhs.getStartPosition();
		int end = rightHandSide.getStartPosition() + rightHandSide.getLength() - 1;
		assignment.setSourceRange(start, end - start + 1);
		return assignment;
	}

	/*
	 * Internal use only
	 * Used to convert class body declarations
	 */
	public TypeDeclaration convert(org.eclipse.jdt.internal.compiler.ast.ASTNode[] nodes) {
		final TypeDeclaration typeDecl = new TypeDeclaration(this.ast);
		typeDecl.setInterface(false);
		int nodesLength = nodes.length;
		for (int i = 0; i < nodesLength; i++) {
			org.eclipse.jdt.internal.compiler.ast.ASTNode node = nodes[i];
			if (node instanceof org.eclipse.jdt.internal.compiler.ast.Initializer) {
				org.eclipse.jdt.internal.compiler.ast.Initializer oldInitializer = (org.eclipse.jdt.internal.compiler.ast.Initializer) node;
				Initializer initializer = new Initializer(this.ast);
				initializer.setBody(convert(oldInitializer.block));
				setModifiers(initializer, oldInitializer);
				initializer.setSourceRange(oldInitializer.declarationSourceStart, oldInitializer.sourceEnd - oldInitializer.declarationSourceStart + 1);
//				setJavaDocComment(initializer);
//				initializer.setJavadoc(convert(oldInitializer.javadoc));
				convert(oldInitializer.javadoc, initializer);
				typeDecl.bodyDeclarations().add(initializer);
			} else if (node instanceof org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) {
				org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) node;
				if (i > 0
					&& (nodes[i - 1] instanceof org.eclipse.jdt.internal.compiler.ast.FieldDeclaration)
					&& ((org.eclipse.jdt.internal.compiler.ast.FieldDeclaration)nodes[i - 1]).declarationSourceStart == fieldDeclaration.declarationSourceStart) {
					// we have a multiple field declaration
					// We retrieve the existing fieldDeclaration to add the new VariableDeclarationFragment
					FieldDeclaration currentFieldDeclaration = (FieldDeclaration) typeDecl.bodyDeclarations().get(typeDecl.bodyDeclarations().size() - 1);
					currentFieldDeclaration.fragments().add(convertToVariableDeclarationFragment(fieldDeclaration));
				} else {
					// we can create a new FieldDeclaration
					typeDecl.bodyDeclarations().add(convertToFieldDeclaration(fieldDeclaration));
				}
			} else if(node instanceof org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration) {
				AbstractMethodDeclaration nextMethodDeclaration = (AbstractMethodDeclaration) node;
				if (!nextMethodDeclaration.isDefaultConstructor() && !nextMethodDeclaration.isClinit()) {
					typeDecl.bodyDeclarations().add(convert(false, nextMethodDeclaration));
				}
			} else if(node instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
				org.eclipse.jdt.internal.compiler.ast.TypeDeclaration nextMemberDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) node;
				ASTNode nextMemberDeclarationNode = convert(nextMemberDeclaration);
				if (nextMemberDeclarationNode == null) {
					typeDecl.setFlags(typeDecl.getFlags() | ASTNode.MALFORMED);
				} else {
					typeDecl.bodyDeclarations().add(nextMemberDeclarationNode);
				}
			}
		}
		return typeDecl;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.BinaryExpression expression) {
		InfixExpression infixExpression = new InfixExpression(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(infixExpression, expression);
		}

		int expressionOperatorID = (expression.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT;
		infixExpression.setOperator(getOperatorFor(expressionOperatorID));

		if (expression.left instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression
				&& ((expression.left.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0)) {
			// create an extended string literal equivalent => use the extended operands list
			infixExpression.extendedOperands().add(convert(expression.right));
			org.eclipse.jdt.internal.compiler.ast.Expression leftOperand = expression.left;
			org.eclipse.jdt.internal.compiler.ast.Expression rightOperand = null;
			do {
				rightOperand = ((org.eclipse.jdt.internal.compiler.ast.BinaryExpression) leftOperand).right;
				if ((((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) != expressionOperatorID
							&& ((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0))
					 || ((rightOperand instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression
				 			&& ((rightOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) != expressionOperatorID)
							&& ((rightOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0))) {
				 	List extendedOperands = infixExpression.extendedOperands();
				 	InfixExpression temp = new InfixExpression(this.ast);
					if (this.resolveBindings) {
						this.recordNodes(temp, expression);
					}
				 	temp.setOperator(getOperatorFor(expressionOperatorID));
				 	Expression leftSide = convert(leftOperand);
					temp.setLeftOperand(leftSide);
					temp.setSourceRange(leftSide.getStartPosition(), leftSide.getLength());
					int size = extendedOperands.size();
				 	for (int i = 0; i < size - 1; i++) {
				 		Expression expr = temp;
				 		temp = new InfixExpression(this.ast);

						if (this.resolveBindings) {
							this.recordNodes(temp, expression);
						}
				 		temp.setLeftOperand(expr);
					 	temp.setOperator(getOperatorFor(expressionOperatorID));
						temp.setSourceRange(expr.getStartPosition(), expr.getLength());
				 	}
				 	infixExpression = temp;
				 	for (int i = 0; i < size; i++) {
				 		Expression extendedOperand = (Expression) extendedOperands.remove(size - 1 - i);
				 		temp.setRightOperand(extendedOperand);
				 		int startPosition = temp.getLeftOperand().getStartPosition();
				 		temp.setSourceRange(startPosition, extendedOperand.getStartPosition() + extendedOperand.getLength() - startPosition);
				 		if (temp.getLeftOperand().getNodeType() == ASTNode.INFIX_EXPRESSION) {
				 			temp = (InfixExpression) temp.getLeftOperand();
				 		}
				 	}
					int startPosition = infixExpression.getLeftOperand().getStartPosition();
					infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
					if (this.resolveBindings) {
						this.recordNodes(infixExpression, expression);
					}
					return infixExpression;
				}
				infixExpression.extendedOperands().add(0, convert(rightOperand));
				leftOperand = ((org.eclipse.jdt.internal.compiler.ast.BinaryExpression) leftOperand).left;
			} while (leftOperand instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression && ((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0));
			Expression leftExpression = convert(leftOperand);
			infixExpression.setLeftOperand(leftExpression);
			infixExpression.setRightOperand((Expression)infixExpression.extendedOperands().remove(0));
			int startPosition = leftExpression.getStartPosition();
			infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
			return infixExpression;
		} else if (expression.left instanceof StringLiteralConcatenation
				&& ((expression.left.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0)
				&& (OperatorIds.PLUS == expressionOperatorID)) {
			StringLiteralConcatenation literal = (StringLiteralConcatenation) expression.left;
			final org.eclipse.jdt.internal.compiler.ast.Expression[] stringLiterals = literal.literals;
			infixExpression.setLeftOperand(convert(stringLiterals[0]));
			infixExpression.setRightOperand(convert(stringLiterals[1]));
			for (int i = 2; i < literal.counter; i++) {
				infixExpression.extendedOperands().add(convert(stringLiterals[i]));
			}
			infixExpression.extendedOperands().add(convert(expression.right));
			int startPosition = literal.sourceStart;
			infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
			return infixExpression;
		}
		Expression leftExpression = convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(convert(expression.right));
		int startPosition = leftExpression.getStartPosition();
		infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		return infixExpression;
	}

	public Block convert(org.eclipse.jdt.internal.compiler.ast.Block statement) {
		Block block = new Block(this.ast);
		if (statement.sourceEnd > 0) {
			block.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		}
		org.eclipse.jdt.internal.compiler.ast.Statement[] statements = statement.statements;
		if (statements != null) {
			int statementsLength = statements.length;
			for (int i = 0; i < statementsLength; i++) {
				if (statements[i] instanceof org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
					checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
				} else {
					Statement statement2 = convert(statements[i]);
					if (statement2 != null) {
						block.statements().add(statement2);
					}
				}
			}
		}
		return block;
	}

	public BreakStatement convert(org.eclipse.jdt.internal.compiler.ast.BreakStatement statement)  {
		BreakStatement breakStatement = new BreakStatement(this.ast);
		breakStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.label != null) {
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(statement.label));
			retrieveIdentifierAndSetPositions(statement.sourceStart, statement.sourceEnd, name);
			breakStatement.setLabel(name);
		}
		return breakStatement;
	}


	public SwitchCase convert(org.eclipse.jdt.internal.compiler.ast.CaseStatement statement) {
		SwitchCase switchCase = new SwitchCase(this.ast);
		org.eclipse.jdt.internal.compiler.ast.Expression constantExpression = statement.constantExpression;
		if (constantExpression == null) {
			switchCase.setExpression(null);
		} else {
			switchCase.setExpression(convert(constantExpression));
		}
		switchCase.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		retrieveColonPosition(switchCase);
		return switchCase;
	}

	public CastExpression convert(org.eclipse.jdt.internal.compiler.ast.CastExpression expression) {
		CastExpression castExpression = new CastExpression(this.ast);
		castExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		TypeReference type = expression.type;
		trimWhiteSpacesAndComments(type);
		castExpression.setType(convertType(type));
		castExpression.setExpression(convert(expression.expression));
		if (this.resolveBindings) {
			recordNodes(castExpression, expression);
		}
		return castExpression;
	}

	public CharacterLiteral convert(org.eclipse.jdt.internal.compiler.ast.CharLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		CharacterLiteral literal = new CharacterLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.internalSetEscapedValue(new String(this.compilationUnitSource, sourceStart, length));
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}
	public Expression convert(org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess expression) {
		TypeLiteral typeLiteral = new TypeLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(typeLiteral, expression);
		}
		typeLiteral.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		typeLiteral.setType(convertType(expression.type));
		return typeLiteral;
	}

	public CompilationUnit convert(org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration unit, char[] source) {
		try {
			if(unit.compilationResult.recoveryScannerData != null) {
				RecoveryScanner recoveryScanner = new RecoveryScanner(this.scanner, unit.compilationResult.recoveryScannerData.removeUnused());
				this.scanner = recoveryScanner;
				this.docParser.scanner = this.scanner;
			}
			this.compilationUnitSource = source;
			this.compilationUnitSourceLength = source.length;
			this.scanner.setSource(source, unit.compilationResult);
			// GROOVY start
			/* old {
			CompilationUnit compilationUnit = new CompilationUnit(this.ast);
		 	} new */
			CompilationUnit compilationUnit = unit.getSpecialDomCompilationUnit(this.ast);
			if (compilationUnit==null ) {
				compilationUnit = new CompilationUnit(this.ast);
			}
			// GROOVY end
			compilationUnit.setStatementsRecoveryData(unit.compilationResult.recoveryScannerData);

			// Parse comments
			int[][] comments = unit.comments;
			if (comments != null) {
				buildCommentsTable(compilationUnit, comments);
			}
	
			// handle the package declaration immediately
			// There is no node corresponding to the package declaration
			if (this.resolveBindings) {
				recordNodes(compilationUnit, unit);
			}
			if (unit.currentPackage != null) {
				PackageDeclaration packageDeclaration = convertPackage(unit);
				compilationUnit.setPackage(packageDeclaration);
			}
			org.eclipse.jdt.internal.compiler.ast.ImportReference[] imports = unit.imports;
			if (imports != null) {
				int importLength = imports.length;
				for (int i = 0; i < importLength; i++) {
					compilationUnit.imports().add(convertImport(imports[i]));
				}
			}
	
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = unit.types;
			if (types != null) {
				int typesLength = types.length;
				for (int i = 0; i < typesLength; i++) {
					org.eclipse.jdt.internal.compiler.ast.TypeDeclaration declaration = types[i];
					if (CharOperation.equals(declaration.name, TypeConstants.PACKAGE_INFO_NAME)) {
						continue;
					}
					ASTNode type = convert(declaration);
					if (type == null) {
						compilationUnit.setFlags(compilationUnit.getFlags() | ASTNode.MALFORMED);
					} else {
						compilationUnit.types().add(type);
					}
				}
			}
			compilationUnit.setSourceRange(unit.sourceStart, unit.sourceEnd - unit.sourceStart  + 1);
	
			int problemLength = unit.compilationResult.problemCount;
			if (problemLength != 0) {
				CategorizedProblem[] resizedProblems = null;
				final CategorizedProblem[] problems = unit.compilationResult.getProblems();
				final int realProblemLength=problems.length;
				if (realProblemLength == problemLength) {
					resizedProblems = problems;
				} else {
					System.arraycopy(problems, 0, (resizedProblems = new CategorizedProblem[realProblemLength]), 0, realProblemLength);
				}
				ASTSyntaxErrorPropagator syntaxErrorPropagator = new ASTSyntaxErrorPropagator(resizedProblems);
				compilationUnit.accept(syntaxErrorPropagator);
				ASTRecoveryPropagator recoveryPropagator =
					new ASTRecoveryPropagator(resizedProblems, unit.compilationResult.recoveryScannerData);
				compilationUnit.accept(recoveryPropagator);
				compilationUnit.setProblems(resizedProblems);
			}
			if (this.resolveBindings) {
				lookupForScopes();
			}
			compilationUnit.initCommentMapper(this.scanner);
			if (SourceRangeVerifier.DEBUG) {
				String bugs = new SourceRangeVerifier().process(compilationUnit);
				if (bugs != null) {
					StringBuffer message = new StringBuffer("Bad AST node structure:");  //$NON-NLS-1$
					String lineDelimiter = Util.findLineSeparator(source);
					if (lineDelimiter == null) lineDelimiter = System.getProperty("line.separator");//$NON-NLS-1$
					message.append(lineDelimiter);
					message.append(bugs.replaceAll("\n", lineDelimiter)); //$NON-NLS-1$
					message.append(lineDelimiter);
					message.append("----------------------------------- SOURCE BEGIN -------------------------------------"); //$NON-NLS-1$
					message.append(lineDelimiter);
					message.append(source);
					message.append(lineDelimiter);
					message.append("----------------------------------- SOURCE END -------------------------------------"); //$NON-NLS-1$
					Util.log(new IllegalStateException("Bad AST node structure"), message.toString()); //$NON-NLS-1$
					if (SourceRangeVerifier.DEBUG_THROW) {
						throw new IllegalStateException(message.toString());
					}
				}
			}
			return compilationUnit;
		} catch(IllegalArgumentException e) {
			StringBuffer message = new StringBuffer("Exception occurred during compilation unit conversion:");  //$NON-NLS-1$
			String lineDelimiter = Util.findLineSeparator(source);
			if (lineDelimiter == null) lineDelimiter = System.getProperty("line.separator");//$NON-NLS-1$
			message.append(lineDelimiter);
			message.append("----------------------------------- SOURCE BEGIN -------------------------------------"); //$NON-NLS-1$
			message.append(lineDelimiter);
			message.append(source);
			message.append(lineDelimiter);
			message.append("----------------------------------- SOURCE END -------------------------------------"); //$NON-NLS-1$
			Util.log(e, message.toString());
			throw e;
		}
	}

	public Assignment convert(org.eclipse.jdt.internal.compiler.ast.CompoundAssignment expression) {
		Assignment assignment = new Assignment(this.ast);
		Expression lhs = convert(expression.lhs);
		assignment.setLeftHandSide(lhs);
		int start = lhs.getStartPosition();
		assignment.setSourceRange(start, expression.sourceEnd - start + 1);
		switch (expression.operator) {
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS :
				assignment.setOperator(Assignment.Operator.PLUS_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS :
				assignment.setOperator(Assignment.Operator.MINUS_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.MULTIPLY :
				assignment.setOperator(Assignment.Operator.TIMES_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.DIVIDE :
				assignment.setOperator(Assignment.Operator.DIVIDE_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.AND :
				assignment.setOperator(Assignment.Operator.BIT_AND_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.OR :
				assignment.setOperator(Assignment.Operator.BIT_OR_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.XOR :
				assignment.setOperator(Assignment.Operator.BIT_XOR_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.REMAINDER :
				assignment.setOperator(Assignment.Operator.REMAINDER_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.LEFT_SHIFT :
				assignment.setOperator(Assignment.Operator.LEFT_SHIFT_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.RIGHT_SHIFT :
				assignment.setOperator(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.UNSIGNED_RIGHT_SHIFT :
				assignment.setOperator(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN);
				break;
		}
		assignment.setRightHandSide(convert(expression.expression));
		if (this.resolveBindings) {
			recordNodes(assignment, expression);
		}
		return assignment;
	}

	public ConditionalExpression convert(org.eclipse.jdt.internal.compiler.ast.ConditionalExpression expression) {
		ConditionalExpression conditionalExpression = new ConditionalExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(conditionalExpression, expression);
		}
		conditionalExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		conditionalExpression.setExpression(convert(expression.condition));
		conditionalExpression.setThenExpression(convert(expression.valueIfTrue));
		conditionalExpression.setElseExpression(convert(expression.valueIfFalse));
		return conditionalExpression;
	}

	public ContinueStatement convert(org.eclipse.jdt.internal.compiler.ast.ContinueStatement statement)  {
		ContinueStatement continueStatement = new ContinueStatement(this.ast);
		continueStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.label != null) {
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(statement.label));
			retrieveIdentifierAndSetPositions(statement.sourceStart, statement.sourceEnd, name);
			continueStatement.setLabel(name);
		}
		return continueStatement;
	}

	public DoStatement convert(org.eclipse.jdt.internal.compiler.ast.DoStatement statement) {
		DoStatement doStatement = new DoStatement(this.ast);
		doStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		doStatement.setExpression(convert(statement.condition));
		final Statement action = convert(statement.action);
		if (action == null) return null;
		doStatement.setBody(action);
		return doStatement;
	}

	public NumberLiteral convert(org.eclipse.jdt.internal.compiler.ast.DoubleLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public EmptyStatement convert(org.eclipse.jdt.internal.compiler.ast.EmptyStatement statement) {
		EmptyStatement emptyStatement = new EmptyStatement(this.ast);
		emptyStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		return emptyStatement;
	}

	// field is an enum constant
	public EnumConstantDeclaration convert(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration enumConstant) {
		checkCanceled();
		EnumConstantDeclaration enumConstantDeclaration = new EnumConstantDeclaration(this.ast);
		final SimpleName typeName = new SimpleName(this.ast);
		typeName.internalSetIdentifier(new String(enumConstant.name));
		typeName.setSourceRange(enumConstant.sourceStart, enumConstant.sourceEnd - enumConstant.sourceStart + 1);
		enumConstantDeclaration.setName(typeName);
		int declarationSourceStart = enumConstant.declarationSourceStart;
		int declarationSourceEnd = enumConstant.declarationSourceEnd;
		final org.eclipse.jdt.internal.compiler.ast.Expression initialization = enumConstant.initialization;
		if (initialization != null) {
			if (initialization instanceof QualifiedAllocationExpression) {
				org.eclipse.jdt.internal.compiler.ast.TypeDeclaration anonymousType = ((QualifiedAllocationExpression) initialization).anonymousType;
				if (anonymousType != null) {
					AnonymousClassDeclaration anonymousClassDeclaration = new AnonymousClassDeclaration(this.ast);
					int start = retrieveStartBlockPosition(anonymousType.sourceEnd, anonymousType.bodyEnd);
					int end = retrieveRightBrace(anonymousType.bodyEnd, declarationSourceEnd);
					if (end == -1) end = anonymousType.bodyEnd;
					anonymousClassDeclaration.setSourceRange(start, end - start + 1);
					enumConstantDeclaration.setAnonymousClassDeclaration(anonymousClassDeclaration);
					buildBodyDeclarations(anonymousType, anonymousClassDeclaration);
					if (this.resolveBindings) {
						recordNodes(anonymousClassDeclaration, anonymousType);
						anonymousClassDeclaration.resolveBinding();
					}
					enumConstantDeclaration.setSourceRange(declarationSourceStart, end - declarationSourceStart + 1);
				}
			} else {
				enumConstantDeclaration.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
			}
			final org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = ((org.eclipse.jdt.internal.compiler.ast.AllocationExpression) initialization).arguments;
			if (arguments != null) {
				for (int i = 0, max = arguments.length; i < max; i++) {
					enumConstantDeclaration.arguments().add(convert(arguments[i]));
				}
			}
		} else {
			enumConstantDeclaration.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
		}
		setModifiers(enumConstantDeclaration, enumConstant);
		if (this.resolveBindings) {
			recordNodes(enumConstantDeclaration, enumConstant);
			recordNodes(typeName, enumConstant);
			enumConstantDeclaration.resolveVariable();
		}
		convert(enumConstant.javadoc, enumConstantDeclaration);
		return enumConstantDeclaration;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.EqualExpression expression) {
		InfixExpression infixExpression = new InfixExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(infixExpression, expression);
		}
		Expression leftExpression = convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(convert(expression.right));
		int startPosition = leftExpression.getStartPosition();
		infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		switch ((expression.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) {
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.EQUAL_EQUAL :
				infixExpression.setOperator(InfixExpression.Operator.EQUALS);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.NOT_EQUAL :
				infixExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
		}
		return infixExpression;

	}

	public Statement convert(org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall statement) {
		Statement newStatement;
		int sourceStart = statement.sourceStart;
		if (statement.isSuperAccess() || statement.isSuper()) {
			SuperConstructorInvocation superConstructorInvocation = new SuperConstructorInvocation(this.ast);
			if (statement.qualification != null) {
				superConstructorInvocation.setExpression(convert(statement.qualification));
			}
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = statement.arguments;
			if (arguments != null) {
				int length = arguments.length;
				for (int i = 0; i < length; i++) {
					superConstructorInvocation.arguments().add(convert(arguments[i]));
				}
			}
			if (statement.typeArguments != null) {
				if (sourceStart > statement.typeArgumentsSourceStart) {
					sourceStart = statement.typeArgumentsSourceStart;
				}
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						superConstructorInvocation.setFlags(superConstructorInvocation.getFlags() | ASTNode.MALFORMED);
						break;
					default :
						for (int i = 0, max = statement.typeArguments.length; i < max; i++) {
							superConstructorInvocation.typeArguments().add(convertType(statement.typeArguments[i]));
						}
						break;
				}
			}
			newStatement = superConstructorInvocation;
		} else {
			ConstructorInvocation constructorInvocation = new ConstructorInvocation(this.ast);
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = statement.arguments;
			if (arguments != null) {
				int length = arguments.length;
				for (int i = 0; i < length; i++) {
					constructorInvocation.arguments().add(convert(arguments[i]));
				}
			}
			if (statement.typeArguments != null) {
				if (sourceStart > statement.typeArgumentsSourceStart) {
					sourceStart = statement.typeArgumentsSourceStart;
				}
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						constructorInvocation.setFlags(constructorInvocation.getFlags() | ASTNode.MALFORMED);
						break;
					default :
						for (int i = 0, max = statement.typeArguments.length; i < max; i++) {
							constructorInvocation.typeArguments().add(convertType(statement.typeArguments[i]));
						}
					break;
				}
			}
			if (statement.qualification != null) {
				// this is an error
				constructorInvocation.setFlags(constructorInvocation.getFlags() | ASTNode.MALFORMED);
			}
			newStatement = constructorInvocation;
		}
		newStatement.setSourceRange(sourceStart, statement.sourceEnd - sourceStart + 1);
		if (this.resolveBindings) {
			recordNodes(newStatement, statement);
		}
		return newStatement;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		if ((expression.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) != 0) {
			return convertToParenthesizedExpression(expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.Annotation) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Annotation) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.CastExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.CastExpression) expression);
		}
		// switch between all types of expression
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.AllocationExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AllocationExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.ArrayInitializer) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ArrayInitializer) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.PrefixExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.PrefixExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.PostfixExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.PostfixExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.CompoundAssignment) {
			return convert((org.eclipse.jdt.internal.compiler.ast.CompoundAssignment) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.Assignment) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Assignment) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.FalseLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.FalseLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.TrueLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.TrueLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.NullLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.NullLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.CharLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.CharLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.DoubleLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.DoubleLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.FloatLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.FloatLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.IntLiteralMinValue) {
			return convert((org.eclipse.jdt.internal.compiler.ast.IntLiteralMinValue) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.IntLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.IntLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.LongLiteralMinValue) {
			return convert((org.eclipse.jdt.internal.compiler.ast.LongLiteralMinValue) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.LongLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.LongLiteral) expression);
		}
		if (expression instanceof StringLiteralConcatenation) {
			return convert((StringLiteralConcatenation) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.StringLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.StringLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.EqualExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.EqualExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.BinaryExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.UnaryExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.UnaryExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.ConditionalExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ConditionalExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.MessageSend) {
			return convert((org.eclipse.jdt.internal.compiler.ast.MessageSend) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.Reference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Reference) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.TypeReference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.TypeReference) expression);
		}
		return null;
	}

	public StringLiteral convert(org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral expression) {
		expression.computeConstant();
		StringLiteral literal = new StringLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setLiteralValue(expression.constant.stringValue());
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public BooleanLiteral convert(org.eclipse.jdt.internal.compiler.ast.FalseLiteral expression) {
		final BooleanLiteral literal =  new BooleanLiteral(this.ast);
		literal.setBooleanValue(false);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.FieldReference reference) {
		if (reference.receiver.isSuper()) {
			final SuperFieldAccess superFieldAccess = new SuperFieldAccess(this.ast);
			if (this.resolveBindings) {
				recordNodes(superFieldAccess, reference);
			}
			if (reference.receiver instanceof org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) {
				Name qualifier = convert((org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) reference.receiver);
				superFieldAccess.setQualifier(qualifier);
				if (this.resolveBindings) {
					recordNodes(qualifier, reference.receiver);
				}
			}
			final SimpleName simpleName = new SimpleName(this.ast);
			simpleName.internalSetIdentifier(new String(reference.token));
			int sourceStart = (int)(reference.nameSourcePosition>>>32);
			int length = (int)(reference.nameSourcePosition & 0xFFFFFFFF) - sourceStart + 1;
			simpleName.setSourceRange(sourceStart, length);
			superFieldAccess.setName(simpleName);
			if (this.resolveBindings) {
				recordNodes(simpleName, reference);
			}
			superFieldAccess.setSourceRange(reference.receiver.sourceStart, reference.sourceEnd - reference.receiver.sourceStart + 1);
			return superFieldAccess;
		} else {
			final FieldAccess fieldAccess = new FieldAccess(this.ast);
			if (this.resolveBindings) {
				recordNodes(fieldAccess, reference);
			}
			Expression receiver = convert(reference.receiver);
			fieldAccess.setExpression(receiver);
			final SimpleName simpleName = new SimpleName(this.ast);
			simpleName.internalSetIdentifier(new String(reference.token));
			int sourceStart = (int)(reference.nameSourcePosition>>>32);
			int length = (int)(reference.nameSourcePosition & 0xFFFFFFFF) - sourceStart + 1;
			simpleName.setSourceRange(sourceStart, length);
			fieldAccess.setName(simpleName);
			if (this.resolveBindings) {
				recordNodes(simpleName, reference);
			}
			fieldAccess.setSourceRange(receiver.getStartPosition(), reference.sourceEnd - receiver.getStartPosition() + 1);
			return fieldAccess;
		}
	}

	public NumberLiteral convert(org.eclipse.jdt.internal.compiler.ast.FloatLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public Statement convert(ForeachStatement statement) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				return createFakeEmptyStatement(statement);
			default :
				EnhancedForStatement enhancedForStatement = new EnhancedForStatement(this.ast);
				enhancedForStatement.setParameter(convertToSingleVariableDeclaration(statement.elementVariable));
				org.eclipse.jdt.internal.compiler.ast.Expression collection = statement.collection;
				if (collection == null) return null;
				enhancedForStatement.setExpression(convert(collection));
				final Statement action = convert(statement.action);
				if (action == null) return null;
				enhancedForStatement.setBody(action);
				int start = statement.sourceStart;
				int end = statement.sourceEnd;
				enhancedForStatement.setSourceRange(start, end - start + 1);
				return enhancedForStatement;
		}
	}

	public ForStatement convert(org.eclipse.jdt.internal.compiler.ast.ForStatement statement) {
		ForStatement forStatement = new ForStatement(this.ast);
		forStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Statement[] initializations = statement.initializations;
		if (initializations != null) {
			// we know that we have at least one initialization
			if (initializations[0] instanceof org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
				org.eclipse.jdt.internal.compiler.ast.LocalDeclaration initialization = (org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) initializations[0];
				VariableDeclarationExpression variableDeclarationExpression = convertToVariableDeclarationExpression(initialization);
				int initializationsLength = initializations.length;
				for (int i = 1; i < initializationsLength; i++) {
					initialization = (org.eclipse.jdt.internal.compiler.ast.LocalDeclaration)initializations[i];
					variableDeclarationExpression.fragments().add(convertToVariableDeclarationFragment(initialization));
				}
				if (initializationsLength != 1) {
					int start = variableDeclarationExpression.getStartPosition();
					int end = ((org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) initializations[initializationsLength - 1]).declarationSourceEnd;
					variableDeclarationExpression.setSourceRange(start, end - start + 1);
				}
				forStatement.initializers().add(variableDeclarationExpression);
			} else {
				int initializationsLength = initializations.length;
				for (int i = 0; i < initializationsLength; i++) {
					Expression initializer = convertToExpression(initializations[i]);
					if (initializer != null) {
						forStatement.initializers().add(initializer);
					} else {
						forStatement.setFlags(forStatement.getFlags() | ASTNode.MALFORMED);
					}
				}
			}
		}
		if (statement.condition != null) {
			forStatement.setExpression(convert(statement.condition));
		}
		org.eclipse.jdt.internal.compiler.ast.Statement[] increments = statement.increments;
		if (increments != null) {
			int incrementsLength = increments.length;
			for (int i = 0; i < incrementsLength; i++) {
				forStatement.updaters().add(convertToExpression(increments[i]));
			}
		}
		final Statement action = convert(statement.action);
		if (action == null) return null;
		forStatement.setBody(action);
		return forStatement;
	}

	public IfStatement convert(org.eclipse.jdt.internal.compiler.ast.IfStatement statement) {
		IfStatement ifStatement = new IfStatement(this.ast);
		ifStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		ifStatement.setExpression(convert(statement.condition));
		final Statement thenStatement = convert(statement.thenStatement);
		if (thenStatement == null) return null;
		ifStatement.setThenStatement(thenStatement);
		org.eclipse.jdt.internal.compiler.ast.Statement statement2 = statement.elseStatement;
		if (statement2 != null) {
			final Statement elseStatement = convert(statement2);
			if (elseStatement != null) {
				ifStatement.setElseStatement(elseStatement);
			}
		}
		return ifStatement;
	}

	public InstanceofExpression convert(org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression expression) {
		InstanceofExpression instanceOfExpression = new InstanceofExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(instanceOfExpression, expression);
		}
		Expression leftExpression = convert(expression.expression);
		instanceOfExpression.setLeftOperand(leftExpression);
		final Type convertType = convertType(expression.type);
		instanceOfExpression.setRightOperand(convertType);
		int startPosition = leftExpression.getStartPosition();
		int sourceEnd = convertType.getStartPosition() + convertType.getLength() - 1;
		instanceOfExpression.setSourceRange(startPosition, sourceEnd - startPosition + 1);
		return instanceOfExpression;
	}

	public NumberLiteral convert(org.eclipse.jdt.internal.compiler.ast.IntLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		final NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(org.eclipse.jdt.internal.compiler.ast.IntLiteralMinValue expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public void convert(org.eclipse.jdt.internal.compiler.ast.Javadoc javadoc, BodyDeclaration bodyDeclaration) {
		if (bodyDeclaration.getJavadoc() == null) {
			if (javadoc != null) {
				if (this.commentMapper == null || !this.commentMapper.hasSameTable(this.commentsTable)) {
					this.commentMapper = new DefaultCommentMapper(this.commentsTable);
				}
				Comment comment = this.commentMapper.getComment(javadoc.sourceStart);
				if (comment != null && comment.isDocComment() && comment.getParent() == null) {
					Javadoc docComment = (Javadoc) comment;
					if (this.resolveBindings) {
						recordNodes(docComment, javadoc);
						// resolve member and method references binding
						Iterator tags = docComment.tags().listIterator();
						while (tags.hasNext()) {
							recordNodes(javadoc, (TagElement) tags.next());
						}
					}
					bodyDeclaration.setJavadoc(docComment);
				}
			}
		}
	}

	public void convert(org.eclipse.jdt.internal.compiler.ast.Javadoc javadoc, PackageDeclaration packageDeclaration) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				return;
		}
		if (packageDeclaration.getJavadoc() == null) {
			if (javadoc != null) {
				if (this.commentMapper == null || !this.commentMapper.hasSameTable(this.commentsTable)) {
					this.commentMapper = new DefaultCommentMapper(this.commentsTable);
				}
				Comment comment = this.commentMapper.getComment(javadoc.sourceStart);
				if (comment != null && comment.isDocComment() && comment.getParent() == null) {
					Javadoc docComment = (Javadoc) comment;
					if (this.resolveBindings) {
						recordNodes(docComment, javadoc);
						// resolve member and method references binding
						Iterator tags = docComment.tags().listIterator();
						while (tags.hasNext()) {
							recordNodes(javadoc, (TagElement) tags.next());
						}
					}
					packageDeclaration.setJavadoc(docComment);
				}
			}
		}
	}

	public LabeledStatement convert(org.eclipse.jdt.internal.compiler.ast.LabeledStatement statement) {
		LabeledStatement labeledStatement = new LabeledStatement(this.ast);
		final int sourceStart = statement.sourceStart;
		labeledStatement.setSourceRange(sourceStart, statement.sourceEnd - sourceStart + 1);
		Statement body = convert(statement.statement);
		if (body == null) return null;
		labeledStatement.setBody(body);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(statement.label));
		name.setSourceRange(sourceStart, statement.labelEnd - sourceStart + 1);
		labeledStatement.setLabel(name);
		return labeledStatement;
	}

	public NumberLiteral convert(org.eclipse.jdt.internal.compiler.ast.LongLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		final NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(org.eclipse.jdt.internal.compiler.ast.LongLiteralMinValue expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		final NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public Expression convert(MessageSend expression) {
		// will return a MethodInvocation or a SuperMethodInvocation or
		Expression expr;
		int sourceStart = expression.sourceStart;
		if (expression.isSuperAccess()) {
			// returns a SuperMethodInvocation
			final SuperMethodInvocation superMethodInvocation = new SuperMethodInvocation(this.ast);
			if (this.resolveBindings) {
				recordNodes(superMethodInvocation, expression);
			}
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(expression.selector));
			int nameSourceStart =  (int) (expression.nameSourcePosition >>> 32);
			int nameSourceLength = ((int) expression.nameSourcePosition) - nameSourceStart + 1;
			name.setSourceRange(nameSourceStart, nameSourceLength);
			if (this.resolveBindings) {
				recordNodes(name, expression);
			}
			superMethodInvocation.setName(name);
			// expression.receiver is either a QualifiedSuperReference or a SuperReference
			// so the casting cannot fail
			if (expression.receiver instanceof org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) {
				Name qualifier = convert((org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) expression.receiver);
				superMethodInvocation.setQualifier(qualifier);
				if (this.resolveBindings) {
					recordNodes(qualifier, expression.receiver);
				}
				if (qualifier != null) {
					sourceStart = qualifier.getStartPosition();
				}
			}
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.arguments;
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++) {
					Expression expri = convert(arguments[i]);
					if (this.resolveBindings) {
						recordNodes(expri, arguments[i]);
					}
					superMethodInvocation.arguments().add(expri);
				}
			}
			final TypeReference[] typeArguments = expression.typeArguments;
			if (typeArguments != null) {
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						superMethodInvocation.setFlags(superMethodInvocation.getFlags() | ASTNode.MALFORMED);
						break;
					default :
						for (int i = 0, max = typeArguments.length; i < max; i++) {
							superMethodInvocation.typeArguments().add(convertType(typeArguments[i]));
						}
						break;
				}
			}
			expr = superMethodInvocation;
		} else {
			// returns a MethodInvocation
			final MethodInvocation methodInvocation = new MethodInvocation(this.ast);
			if (this.resolveBindings) {
				recordNodes(methodInvocation, expression);
			}
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(expression.selector));
			int nameSourceStart =  (int) (expression.nameSourcePosition >>> 32);
			int nameSourceLength = ((int) expression.nameSourcePosition) - nameSourceStart + 1;
			name.setSourceRange(nameSourceStart, nameSourceLength);
			methodInvocation.setName(name);
			if (this.resolveBindings) {
				recordNodes(name, expression);
			}
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.arguments;
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++) {
					Expression expri = convert(arguments[i]);
					if (this.resolveBindings) {
						recordNodes(expri, arguments[i]);
					}
					methodInvocation.arguments().add(expri);
				}
			}
			Expression qualifier = null;
			org.eclipse.jdt.internal.compiler.ast.Expression receiver = expression.receiver;
			if (receiver instanceof MessageSend) {
				if ((receiver.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) != 0) {
					qualifier = convertToParenthesizedExpression(receiver);
				} else {
					qualifier = convert((MessageSend) receiver);
				}
			} else {
				qualifier = convert(receiver);
			}
			if (qualifier instanceof Name && this.resolveBindings) {
				recordNodes(qualifier, receiver);
			}
			methodInvocation.setExpression(qualifier);
			if (qualifier != null) {
				sourceStart = qualifier.getStartPosition();
			}
			final TypeReference[] typeArguments = expression.typeArguments;
			if (typeArguments != null) {
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						methodInvocation.setFlags(methodInvocation.getFlags() | ASTNode.MALFORMED);
						break;
					default :
						for (int i = 0, max = typeArguments.length; i < max; i++) {
							methodInvocation.typeArguments().add(convertType(typeArguments[i]));
						}
						break;
				}
			}
			expr = methodInvocation;
		}
		expr.setSourceRange(sourceStart, expression.sourceEnd - sourceStart + 1);
		return expr;
	}

	public MarkerAnnotation convert(org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation annotation) {
		final MarkerAnnotation markerAnnotation = new MarkerAnnotation(this.ast);
		setTypeNameForAnnotation(annotation, markerAnnotation);
		int start = annotation.sourceStart;
		int end = annotation.declarationSourceEnd;
		markerAnnotation.setSourceRange(start, end - start + 1);
		if (this.resolveBindings) {
			recordNodes(markerAnnotation, annotation);
			markerAnnotation.resolveAnnotationBinding();
		}
		return markerAnnotation;
	}

	public MemberValuePair convert(org.eclipse.jdt.internal.compiler.ast.MemberValuePair memberValuePair) {
		final MemberValuePair pair = new MemberValuePair(this.ast);
		final SimpleName simpleName = new SimpleName(this.ast);
		simpleName.internalSetIdentifier(new String(memberValuePair.name));
		int start = memberValuePair.sourceStart;
		int end = memberValuePair.sourceEnd;
		simpleName.setSourceRange(start, end - start + 1);
		pair.setName(simpleName);
		final Expression value = convert(memberValuePair.value);
		pair.setValue(value);
		start = memberValuePair.sourceStart;
		end = value.getStartPosition() + value.getLength() - 1;
		pair.setSourceRange(start, end - start + 1);

		if (memberValuePair.value instanceof SingleNameReference &&
				((SingleNameReference)memberValuePair.value).token == RecoveryScanner.FAKE_IDENTIFIER) {
			pair.setFlags(pair.getFlags() | ASTNode.RECOVERED);
		}

		if (this.resolveBindings) {
			recordNodes(simpleName, memberValuePair);
			recordNodes(pair, memberValuePair);
		}
		return pair;
	}

	public Name convert(org.eclipse.jdt.internal.compiler.ast.NameReference reference) {
		if (reference instanceof org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference) reference);
		} else {
			return convert((org.eclipse.jdt.internal.compiler.ast.SingleNameReference) reference);
		}
	}

	public InfixExpression convert(StringLiteralConcatenation expression) {
		expression.computeConstant();
		final InfixExpression infixExpression = new InfixExpression(this.ast);
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		org.eclipse.jdt.internal.compiler.ast.Expression[] stringLiterals = expression.literals;
		infixExpression.setLeftOperand(convert(stringLiterals[0]));
		infixExpression.setRightOperand(convert(stringLiterals[1]));
		for (int i = 2; i < expression.counter; i++) {
			infixExpression.extendedOperands().add(convert(stringLiterals[i]));
		}
		if (this.resolveBindings) {
			this.recordNodes(infixExpression, expression);
		}
		infixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return infixExpression;
	}

	public NormalAnnotation convert(org.eclipse.jdt.internal.compiler.ast.NormalAnnotation annotation) {
		final NormalAnnotation normalAnnotation = new NormalAnnotation(this.ast);
		setTypeNameForAnnotation(annotation, normalAnnotation);

		int start = annotation.sourceStart;
		int end = annotation.declarationSourceEnd;

		org.eclipse.jdt.internal.compiler.ast.MemberValuePair[] memberValuePairs = annotation.memberValuePairs;
		if (memberValuePairs != null) {
			for (int i = 0, max = memberValuePairs.length; i < max; i++) {
				MemberValuePair memberValuePair = convert(memberValuePairs[i]);
				int memberValuePairEnd = memberValuePair.getStartPosition() + memberValuePair.getLength() - 1;
				if (end == memberValuePairEnd) {
					normalAnnotation.setFlags(normalAnnotation.getFlags() | ASTNode.RECOVERED);
				}
				normalAnnotation.values().add(memberValuePair);
			}
		}

		normalAnnotation.setSourceRange(start, end - start + 1);
		if (this.resolveBindings) {
			recordNodes(normalAnnotation, annotation);
			normalAnnotation.resolveAnnotationBinding();
		}
		return normalAnnotation;
	}

	public NullLiteral convert(org.eclipse.jdt.internal.compiler.ast.NullLiteral expression) {
		final NullLiteral literal = new NullLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression expression) {
		InfixExpression infixExpression = new InfixExpression(this.ast);
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		if (this.resolveBindings) {
			this.recordNodes(infixExpression, expression);
		}
		final int expressionOperatorID = (expression.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT;
		if (expression.left instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression
				&& ((expression.left.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0)) {
			// create an extended string literal equivalent => use the extended operands list
			infixExpression.extendedOperands().add(convert(expression.right));
			org.eclipse.jdt.internal.compiler.ast.Expression leftOperand = expression.left;
			org.eclipse.jdt.internal.compiler.ast.Expression rightOperand = null;
			do {
				rightOperand = ((org.eclipse.jdt.internal.compiler.ast.BinaryExpression) leftOperand).right;
				if ((((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) != expressionOperatorID
							&& ((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0))
					 || ((rightOperand instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression
				 			&& ((rightOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) != expressionOperatorID)
							&& ((rightOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0))) {
				 	List extendedOperands = infixExpression.extendedOperands();
				 	InfixExpression temp = new InfixExpression(this.ast);
					if (this.resolveBindings) {
						this.recordNodes(temp, expression);
					}
				 	temp.setOperator(getOperatorFor(expressionOperatorID));
				 	Expression leftSide = convert(leftOperand);
					temp.setLeftOperand(leftSide);
					temp.setSourceRange(leftSide.getStartPosition(), leftSide.getLength());
					int size = extendedOperands.size();
				 	for (int i = 0; i < size - 1; i++) {
				 		Expression expr = temp;
				 		temp = new InfixExpression(this.ast);

						if (this.resolveBindings) {
							this.recordNodes(temp, expression);
						}
				 		temp.setLeftOperand(expr);
					 	temp.setOperator(getOperatorFor(expressionOperatorID));
						temp.setSourceRange(expr.getStartPosition(), expr.getLength());
				 	}
				 	infixExpression = temp;
				 	for (int i = 0; i < size; i++) {
				 		Expression extendedOperand = (Expression) extendedOperands.remove(size - 1 - i);
				 		temp.setRightOperand(extendedOperand);
				 		int startPosition = temp.getLeftOperand().getStartPosition();
				 		temp.setSourceRange(startPosition, extendedOperand.getStartPosition() + extendedOperand.getLength() - startPosition);
				 		if (temp.getLeftOperand().getNodeType() == ASTNode.INFIX_EXPRESSION) {
				 			temp = (InfixExpression) temp.getLeftOperand();
				 		}
				 	}
					int startPosition = infixExpression.getLeftOperand().getStartPosition();
					infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
					if (this.resolveBindings) {
						this.recordNodes(infixExpression, expression);
					}
					return infixExpression;
				}
				infixExpression.extendedOperands().add(0, convert(rightOperand));
				leftOperand = ((org.eclipse.jdt.internal.compiler.ast.BinaryExpression) leftOperand).left;
			} while (leftOperand instanceof org.eclipse.jdt.internal.compiler.ast.BinaryExpression && ((leftOperand.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0));
			Expression leftExpression = convert(leftOperand);
			infixExpression.setLeftOperand(leftExpression);
			infixExpression.setRightOperand((Expression)infixExpression.extendedOperands().remove(0));
			int startPosition = leftExpression.getStartPosition();
			infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
			return infixExpression;
		}
		Expression leftExpression = convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(convert(expression.right));
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		int startPosition = leftExpression.getStartPosition();
		infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		return infixExpression;
	}

	public PostfixExpression convert(org.eclipse.jdt.internal.compiler.ast.PostfixExpression expression) {
		final PostfixExpression postfixExpression = new PostfixExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(postfixExpression, expression);
		}
		postfixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		postfixExpression.setOperand(convert(expression.lhs));
		switch (expression.operator) {
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS :
				postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS :
				postfixExpression.setOperator(PostfixExpression.Operator.DECREMENT);
				break;
		}
		return postfixExpression;
	}

	public PrefixExpression convert(org.eclipse.jdt.internal.compiler.ast.PrefixExpression expression) {
		final PrefixExpression prefixExpression = new PrefixExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(prefixExpression, expression);
		}
		prefixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		prefixExpression.setOperand(convert(expression.lhs));
		switch (expression.operator) {
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS :
				prefixExpression.setOperator(PrefixExpression.Operator.INCREMENT);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS :
				prefixExpression.setOperator(PrefixExpression.Operator.DECREMENT);
				break;
		}
		return prefixExpression;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression allocation) {
		final ClassInstanceCreation classInstanceCreation = new ClassInstanceCreation(this.ast);
		if (allocation.enclosingInstance != null) {
			classInstanceCreation.setExpression(convert(allocation.enclosingInstance));
		}
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				classInstanceCreation.internalSetName(convert(allocation.type));
				break;
			default :
				classInstanceCreation.setType(convertType(allocation.type));
		}
		org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = allocation.arguments;
		if (arguments != null) {
			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				Expression argument = convert(arguments[i]);
				if (this.resolveBindings) {
					recordNodes(argument, arguments[i]);
				}
				classInstanceCreation.arguments().add(argument);
			}
		}
		if (allocation.typeArguments != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					classInstanceCreation.setFlags(classInstanceCreation.getFlags() | ASTNode.MALFORMED);
					break;
				default :
					for (int i = 0, max = allocation.typeArguments.length; i < max; i++) {
						classInstanceCreation.typeArguments().add(convertType(allocation.typeArguments[i]));
					}
			}
		}
		if (allocation.anonymousType != null) {
			int declarationSourceStart = allocation.sourceStart;
			classInstanceCreation.setSourceRange(declarationSourceStart, allocation.anonymousType.bodyEnd - declarationSourceStart + 1);
			final AnonymousClassDeclaration anonymousClassDeclaration = new AnonymousClassDeclaration(this.ast);
			int start = retrieveStartBlockPosition(allocation.anonymousType.sourceEnd, allocation.anonymousType.bodyEnd);
			anonymousClassDeclaration.setSourceRange(start, allocation.anonymousType.bodyEnd - start + 1);
			classInstanceCreation.setAnonymousClassDeclaration(anonymousClassDeclaration);
			buildBodyDeclarations(allocation.anonymousType, anonymousClassDeclaration);
			if (this.resolveBindings) {
				recordNodes(classInstanceCreation, allocation.anonymousType);
				recordNodes(anonymousClassDeclaration, allocation.anonymousType);
				anonymousClassDeclaration.resolveBinding();
			}
			return classInstanceCreation;
		} else {
			final int start = allocation.sourceStart;
			classInstanceCreation.setSourceRange(start, allocation.sourceEnd - start + 1);
			if (this.resolveBindings) {
				recordNodes(classInstanceCreation, allocation);
			}
			return classInstanceCreation;
		}
	}

	public Name convert(org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference nameReference) {
		return setQualifiedNameNameAndSourceRanges(nameReference.tokens, nameReference.sourcePositions, nameReference);
	}

	public Name convert(org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference reference) {
		return convert(reference.qualification);
	}

	public ThisExpression convert(org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference reference) {
		final ThisExpression thisExpression = new ThisExpression(this.ast);
		thisExpression.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
		thisExpression.setQualifier(convert(reference.qualification));
		if (this.resolveBindings) {
			recordNodes(thisExpression, reference);
			recordPendingThisExpressionScopeResolution(thisExpression);
		}
		return thisExpression;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.Reference reference) {
		if (reference instanceof org.eclipse.jdt.internal.compiler.ast.NameReference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.NameReference) reference);
		}
		if (reference instanceof org.eclipse.jdt.internal.compiler.ast.ThisReference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ThisReference) reference);
		}
		if (reference instanceof org.eclipse.jdt.internal.compiler.ast.ArrayReference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ArrayReference) reference);
		}
		if (reference instanceof org.eclipse.jdt.internal.compiler.ast.FieldReference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.FieldReference) reference);
		}
		return null; // cannot be reached
	}

	public ReturnStatement convert(org.eclipse.jdt.internal.compiler.ast.ReturnStatement statement) {
		final ReturnStatement returnStatement = new ReturnStatement(this.ast);
		returnStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.expression != null) {
			returnStatement.setExpression(convert(statement.expression));
		}
		return returnStatement;
	}

	public SingleMemberAnnotation convert(org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation annotation) {
		final SingleMemberAnnotation singleMemberAnnotation = new SingleMemberAnnotation(this.ast);
		setTypeNameForAnnotation(annotation, singleMemberAnnotation);
		singleMemberAnnotation.setValue(convert(annotation.memberValue));
		int start = annotation.sourceStart;
		int end = annotation.declarationSourceEnd;
		singleMemberAnnotation.setSourceRange(start, end - start + 1);
		if (this.resolveBindings) {
			recordNodes(singleMemberAnnotation, annotation);
			singleMemberAnnotation.resolveAnnotationBinding();
		}
		return singleMemberAnnotation;
	}

	public SimpleName convert(org.eclipse.jdt.internal.compiler.ast.SingleNameReference nameReference) {
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(nameReference.token));
		if (this.resolveBindings) {
			recordNodes(name, nameReference);
		}
		name.setSourceRange(nameReference.sourceStart, nameReference.sourceEnd - nameReference.sourceStart + 1);
		return name;
	}

	public Statement convert(org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement instanceof ForeachStatement) {
			return convert((ForeachStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.LocalDeclaration localDeclaration = (org.eclipse.jdt.internal.compiler.ast.LocalDeclaration)statement;
			return convertToVariableDeclarationStatement(localDeclaration);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.AssertStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AssertStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.Block) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Block) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.BreakStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.BreakStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ContinueStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ContinueStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.CaseStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.CaseStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.DoStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.DoStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.EmptyStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.EmptyStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ForStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ForStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.IfStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.IfStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.LabeledStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.LabeledStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ReturnStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ReturnStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.SwitchStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.SwitchStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ThrowStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ThrowStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.TryStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.TryStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
			ASTNode result = convert((org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) statement);
			if (result == null) {
				return createFakeEmptyStatement(statement);
			}
			// annotation and enum type declarations are not returned by the parser inside method bodies
					TypeDeclaration typeDeclaration = (TypeDeclaration) result;
					TypeDeclarationStatement typeDeclarationStatement = new TypeDeclarationStatement(this.ast);
					typeDeclarationStatement.setDeclaration(typeDeclaration);
					switch(this.ast.apiLevel) {
						case AST.JLS2_INTERNAL :
							TypeDeclaration typeDecl = typeDeclarationStatement.internalGetTypeDeclaration();
							typeDeclarationStatement.setSourceRange(typeDecl.getStartPosition(), typeDecl.getLength());
							break;
				default :
							AbstractTypeDeclaration typeDeclAST3 = typeDeclarationStatement.getDeclaration();
							typeDeclarationStatement.setSourceRange(typeDeclAST3.getStartPosition(), typeDeclAST3.getLength());
							break;
					}
					return typeDeclarationStatement;
			}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.WhileStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.WhileStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.Expression) {
			org.eclipse.jdt.internal.compiler.ast.Expression statement2 = (org.eclipse.jdt.internal.compiler.ast.Expression) statement;
			final Expression expr = convert(statement2);
			final ExpressionStatement stmt = new ExpressionStatement(this.ast);
			stmt.setExpression(expr);
			int sourceStart = expr.getStartPosition();
			int sourceEnd = statement2.statementEnd;
			stmt.setSourceRange(sourceStart, sourceEnd - sourceStart + 1);
			return stmt;
		}
		return createFakeEmptyStatement(statement);
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.StringLiteral expression) {
		if (expression instanceof StringLiteralConcatenation) {
			return convert((StringLiteralConcatenation) expression);
		}
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		StringLiteral literal = new StringLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.internalSetEscapedValue(new String(this.compilationUnitSource, sourceStart, length));
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public SwitchStatement convert(org.eclipse.jdt.internal.compiler.ast.SwitchStatement statement) {
		SwitchStatement switchStatement = new SwitchStatement(this.ast);
		switchStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		switchStatement.setExpression(convert(statement.expression));
		org.eclipse.jdt.internal.compiler.ast.Statement[] statements = statement.statements;
		if (statements != null) {
			int statementsLength = statements.length;
			for (int i = 0; i < statementsLength; i++) {
				if (statements[i] instanceof org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
					checkAndAddMultipleLocalDeclaration(statements, i, switchStatement.statements());
				} else {
					final Statement currentStatement = convert(statements[i]);
					if (currentStatement != null) {
						switchStatement.statements().add(currentStatement);
					}
				}
			}
		}
		return switchStatement;
	}

	public SynchronizedStatement convert(org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement statement) {
		SynchronizedStatement synchronizedStatement = new SynchronizedStatement(this.ast);
		synchronizedStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		synchronizedStatement.setBody(convert(statement.block));
		synchronizedStatement.setExpression(convert(statement.expression));
		return synchronizedStatement;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.ThisReference reference) {
		if (reference.isImplicitThis()) {
			// There is no source associated with an implicit this
			return null;
		} else if (reference instanceof org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) reference);
		} else if (reference instanceof org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference) {
			return convert((org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference) reference);
		}  else {
			ThisExpression thisExpression = new ThisExpression(this.ast);
			thisExpression.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
			if (this.resolveBindings) {
				recordNodes(thisExpression, reference);
				recordPendingThisExpressionScopeResolution(thisExpression);
			}
			return thisExpression;
		}
	}

	public ThrowStatement convert(org.eclipse.jdt.internal.compiler.ast.ThrowStatement statement) {
		final ThrowStatement throwStatement = new ThrowStatement(this.ast);
		throwStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		throwStatement.setExpression(convert(statement.exception));
		return throwStatement;
	}

	public BooleanLiteral convert(org.eclipse.jdt.internal.compiler.ast.TrueLiteral expression) {
		final BooleanLiteral literal = new BooleanLiteral(this.ast);
		literal.setBooleanValue(true);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public TryStatement convert(org.eclipse.jdt.internal.compiler.ast.TryStatement statement) {
		final TryStatement tryStatement = new TryStatement(this.ast);
		tryStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		LocalDeclaration[] localDeclarations = statement.resources;
		int resourcesLength = localDeclarations.length;
		if (resourcesLength > 0) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
				case AST.JLS3_INTERNAL :
					// convert it to a simple try statement tagged as MALFORMED
					tryStatement.setFlags(tryStatement.getFlags() | ASTNode.MALFORMED);
					break;
				default:
					for (int i = 0; i < resourcesLength; i++) {
						LocalDeclaration localDeclaration = localDeclarations[i];
						VariableDeclarationExpression variableDeclarationExpression = convertToVariableDeclarationExpression(localDeclaration);
						int start = variableDeclarationExpression.getStartPosition();
						int end = localDeclaration.declarationEnd;
						variableDeclarationExpression.setSourceRange(start, end - start + 1);
						tryStatement.resources().add(variableDeclarationExpression);
					}
			}
		}
		tryStatement.setBody(convert(statement.tryBlock));
		org.eclipse.jdt.internal.compiler.ast.Argument[] catchArguments = statement.catchArguments;
		if (catchArguments != null) {
			int catchArgumentsLength = catchArguments.length;
			org.eclipse.jdt.internal.compiler.ast.Block[] catchBlocks = statement.catchBlocks;
			int start = statement.tryBlock.sourceEnd;
			for (int i = 0; i < catchArgumentsLength; i++) {
				CatchClause catchClause = new CatchClause(this.ast);
				int catchClauseSourceStart = retrieveStartingCatchPosition(start, catchArguments[i].sourceStart);
				catchClause.setSourceRange(catchClauseSourceStart, catchBlocks[i].sourceEnd - catchClauseSourceStart + 1);
				catchClause.setBody(convert(catchBlocks[i]));
				catchClause.setException(convert(catchArguments[i]));
				tryStatement.catchClauses().add(catchClause);
				start = catchBlocks[i].sourceEnd;
			}
		}
		if (statement.finallyBlock != null) {
			tryStatement.setFinally(convert(statement.finallyBlock));
		}
		return tryStatement;
	}

	public ASTNode convert(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		int kind = org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.kind(typeDeclaration.modifiers);
		switch (kind) {
			case org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.ENUM_DECL :
				if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
					return null;
				} else {
					return convertToEnumDeclaration(typeDeclaration);
				}
			case org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.ANNOTATION_TYPE_DECL :
				if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
					return null;
				} else {
					return convertToAnnotationDeclaration(typeDeclaration);
				}
		}

		checkCanceled();
		TypeDeclaration typeDecl = new TypeDeclaration(this.ast);
		if (typeDeclaration.modifiersSourceStart != -1) {
			setModifiers(typeDecl, typeDeclaration);
		}
		boolean isInterface = kind == org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.INTERFACE_DECL;
		typeDecl.setInterface(isInterface);
		final SimpleName typeName = new SimpleName(this.ast);
		typeName.internalSetIdentifier(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(typeName);
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.bodyEnd - typeDeclaration.declarationSourceStart + 1);

		// need to set the superclass and super interfaces here since we cannot distinguish them at
		// the type references level.
		if (typeDeclaration.superclass != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					typeDecl.internalSetSuperclass(convert(typeDeclaration.superclass));
					break;
				default :
					typeDecl.setSuperclassType(convertType(typeDeclaration.superclass));
					break;
			}
		}

		org.eclipse.jdt.internal.compiler.ast.TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					for (int index = 0, length = superInterfaces.length; index < length; index++) {
						typeDecl.internalSuperInterfaces().add(convert(superInterfaces[index]));
					}
					break;
				default :
					for (int index = 0, length = superInterfaces.length; index < length; index++) {
						typeDecl.superInterfaceTypes().add(convertType(superInterfaces[index]));
					}
			}
		}
		org.eclipse.jdt.internal.compiler.ast.TypeParameter[] typeParameters = typeDeclaration.typeParameters;
		if (typeParameters != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					typeDecl.setFlags(typeDecl.getFlags() | ASTNode.MALFORMED);
					break;
				default :
					for (int index = 0, length = typeParameters.length; index < length; index++) {
						typeDecl.typeParameters().add(convert(typeParameters[index]));
					}
			}
		}
		buildBodyDeclarations(typeDeclaration, typeDecl, isInterface);
		if (this.resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
			recordNodes(typeName, typeDeclaration);
			typeDecl.resolveBinding();
		}
		return typeDecl;
	}

	public TypeParameter convert(org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParameter) {
		final TypeParameter typeParameter2 = new TypeParameter(this.ast);
		final SimpleName simpleName = new SimpleName(this.ast);
		simpleName.internalSetIdentifier(new String(typeParameter.name));
		int start = typeParameter.sourceStart;
		int end = typeParameter.sourceEnd;
		simpleName.setSourceRange(start, end - start + 1);
		typeParameter2.setName(simpleName);
		final TypeReference superType = typeParameter.type;
		end = typeParameter.declarationSourceEnd;
		if (superType != null) {
			Type type = convertType(superType);
			typeParameter2.typeBounds().add(type);
			end = type.getStartPosition() + type.getLength() - 1;
		}
		TypeReference[] bounds = typeParameter.bounds;
		if (bounds != null) {
			Type type = null;
			for (int index = 0, length = bounds.length; index < length; index++) {
				type = convertType(bounds[index]);
				typeParameter2.typeBounds().add(type);
				end = type.getStartPosition() + type.getLength() - 1;
			}
		}
		start = typeParameter.declarationSourceStart;
		end = retrieveClosingAngleBracketPosition(end);
		typeParameter2.setSourceRange(start, end - start + 1);
		if (this.resolveBindings) {
			recordName(simpleName, typeParameter);
			recordNodes(typeParameter2, typeParameter);
			typeParameter2.resolveBinding();
		}
		return typeParameter2;
	}

	public Name convert(org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference) {
		char[][] typeName = typeReference.getTypeName();
		int length = typeName.length;
		if (length > 1) {
			// QualifiedName
			org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference qualifiedTypeReference = (org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference;
			final long[] positions = qualifiedTypeReference.sourcePositions;
			return setQualifiedNameNameAndSourceRanges(typeName, positions, typeReference);
		} else {
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(typeName[0]));
			name.setSourceRange(typeReference.sourceStart, typeReference.sourceEnd - typeReference.sourceStart + 1);
			name.index = 1;
			if (this.resolveBindings) {
				recordNodes(name, typeReference);
			}
			return name;
		}
	}

	public PrefixExpression convert(org.eclipse.jdt.internal.compiler.ast.UnaryExpression expression) {
		final PrefixExpression prefixExpression = new PrefixExpression(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(prefixExpression, expression);
		}
		prefixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		prefixExpression.setOperand(convert(expression.expression));
		switch ((expression.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) {
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS :
				prefixExpression.setOperator(PrefixExpression.Operator.PLUS);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS :
				prefixExpression.setOperator(PrefixExpression.Operator.MINUS);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.NOT :
				prefixExpression.setOperator(PrefixExpression.Operator.NOT);
				break;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.TWIDDLE :
				prefixExpression.setOperator(PrefixExpression.Operator.COMPLEMENT);
		}
		return prefixExpression;
	}

	public WhileStatement convert(org.eclipse.jdt.internal.compiler.ast.WhileStatement statement) {
		final WhileStatement whileStatement = new WhileStatement(this.ast);
		whileStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		whileStatement.setExpression(convert(statement.condition));
		final Statement action = convert(statement.action);
		if (action == null) return null;
		whileStatement.setBody(action);
		return whileStatement;
	}

	public ImportDeclaration convertImport(org.eclipse.jdt.internal.compiler.ast.ImportReference importReference) {
		final ImportDeclaration importDeclaration = new ImportDeclaration(this.ast);
		final boolean onDemand = (importReference.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.OnDemand) != 0;
		final char[][] tokens = importReference.tokens;
		int length = importReference.tokens.length;
		final long[] positions = importReference.sourcePositions;
		if (length > 1) {
			importDeclaration.setName(setQualifiedNameNameAndSourceRanges(tokens, positions, importReference));
		} else {
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(tokens[0]));
			final int start = (int)(positions[0]>>>32);
			final int end = (int)(positions[0] & 0xFFFFFFFF);
			name.setSourceRange(start, end - start + 1);
			name.index = 1;
			importDeclaration.setName(name);
			if (this.resolveBindings) {
				recordNodes(name, importReference);
			}
		}
		importDeclaration.setSourceRange(importReference.declarationSourceStart, importReference.declarationEnd - importReference.declarationSourceStart + 1);
		importDeclaration.setOnDemand(onDemand);
		int modifiers = importReference.modifiers;
		if (modifiers != ClassFileConstants.AccDefault) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					importDeclaration.setFlags(importDeclaration.getFlags() | ASTNode.MALFORMED);
					break;
				default :
					if (modifiers == ClassFileConstants.AccStatic) {
						importDeclaration.setStatic(true);
					} else {
						importDeclaration.setFlags(importDeclaration.getFlags() | ASTNode.MALFORMED);
					}
			}
		}
		if (this.resolveBindings) {
			recordNodes(importDeclaration, importReference);
		}
		return importDeclaration;
	}

	public PackageDeclaration convertPackage(org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration compilationUnitDeclaration) {
		org.eclipse.jdt.internal.compiler.ast.ImportReference importReference = compilationUnitDeclaration.currentPackage;
		final PackageDeclaration packageDeclaration = new PackageDeclaration(this.ast);
		final char[][] tokens = importReference.tokens;
		final int length = importReference.tokens.length;
		long[] positions = importReference.sourcePositions;
		if (length > 1) {
			packageDeclaration.setName(setQualifiedNameNameAndSourceRanges(tokens, positions, importReference));
		} else {
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(tokens[0]));
			int start = (int)(positions[0]>>>32);
			int end = (int)(positions[length - 1] & 0xFFFFFFFF);
			name.setSourceRange(start, end - start + 1);
			name.index = 1;
			packageDeclaration.setName(name);
			if (this.resolveBindings) {
				recordNodes(name, compilationUnitDeclaration);
			}
		}
		packageDeclaration.setSourceRange(importReference.declarationSourceStart, importReference.declarationEnd - importReference.declarationSourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = importReference.annotations;
		if (annotations != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					packageDeclaration.setFlags(packageDeclaration.getFlags() & ASTNode.MALFORMED);
					break;
				default :
					for (int i = 0, max = annotations.length; i < max; i++) {
						packageDeclaration.annotations().add(convert(annotations[i]));
					}
			}
		}
		if (this.resolveBindings) {
			recordNodes(packageDeclaration, importReference);
		}
		// Set javadoc
		convert(compilationUnitDeclaration.javadoc, packageDeclaration);
		return packageDeclaration;
	}

	private EnumDeclaration convertToEnumDeclaration(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		checkCanceled();
		// enum declaration cannot be built if the source is not >= 1.5, since enum is then seen as an identifier
		final EnumDeclaration enumDeclaration2 = new EnumDeclaration(this.ast);
		setModifiers(enumDeclaration2, typeDeclaration);
		final SimpleName typeName = new SimpleName(this.ast);
		typeName.internalSetIdentifier(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		enumDeclaration2.setName(typeName);
		enumDeclaration2.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.bodyEnd - typeDeclaration.declarationSourceStart + 1);

		org.eclipse.jdt.internal.compiler.ast.TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			for (int index = 0, length = superInterfaces.length; index < length; index++) {
				enumDeclaration2.superInterfaceTypes().add(convertType(superInterfaces[index]));
			}
		}
		buildBodyDeclarations(typeDeclaration, enumDeclaration2);
		if (this.resolveBindings) {
			recordNodes(enumDeclaration2, typeDeclaration);
			recordNodes(typeName, typeDeclaration);
			enumDeclaration2.resolveBinding();
		}
		return enumDeclaration2;
	}
	public Expression convertToExpression(org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.Expression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Expression) statement);
		} else {
			return null;
		}
	}

	protected FieldDeclaration convertToFieldDeclaration(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDecl) {
		VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(fieldDecl);
		final FieldDeclaration fieldDeclaration = new FieldDeclaration(this.ast);
		fieldDeclaration.fragments().add(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, fieldDecl);
			variableDeclarationFragment.resolveBinding();
		}
		fieldDeclaration.setSourceRange(fieldDecl.declarationSourceStart, fieldDecl.declarationEnd - fieldDecl.declarationSourceStart + 1);
		Type type = convertType(fieldDecl.type);
		setTypeForField(fieldDeclaration, type, variableDeclarationFragment.getExtraDimensions());
		setModifiers(fieldDeclaration, fieldDecl);
		convert(fieldDecl.javadoc, fieldDeclaration);
		return fieldDeclaration;
	}

	public ParenthesizedExpression convertToParenthesizedExpression(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		final ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(parenthesizedExpression, expression);
		}
		parenthesizedExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		adjustSourcePositionsForParent(expression);
		trimWhiteSpacesAndComments(expression);
		// decrement the number of parenthesis
		int numberOfParenthesis = (expression.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) >> org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedSHIFT;
		expression.bits &= ~org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK;
		expression.bits |= (numberOfParenthesis - 1) << org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedSHIFT;
		parenthesizedExpression.setExpression(convert(expression));
		return parenthesizedExpression;
	}

	protected VariableDeclarationExpression convertToVariableDeclarationExpression(org.eclipse.jdt.internal.compiler.ast.LocalDeclaration localDeclaration) {
		final VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(localDeclaration);
		final VariableDeclarationExpression variableDeclarationExpression = new VariableDeclarationExpression(this.ast);
		variableDeclarationExpression.fragments().add(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
		}
		variableDeclarationExpression.setSourceRange(localDeclaration.declarationSourceStart, localDeclaration.declarationSourceEnd - localDeclaration.declarationSourceStart + 1);
		Type type = convertType(localDeclaration.type);
		setTypeForVariableDeclarationExpression(variableDeclarationExpression, type, variableDeclarationFragment.getExtraDimensions());
		if (localDeclaration.modifiersSourceStart != -1) {
			setModifiers(variableDeclarationExpression, localDeclaration);
		}
		return variableDeclarationExpression;
	}

	protected SingleVariableDeclaration convertToSingleVariableDeclaration(LocalDeclaration localDeclaration) {
		final SingleVariableDeclaration variableDecl = new SingleVariableDeclaration(this.ast);
		setModifiers(variableDecl, localDeclaration);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(localDeclaration.name));
		int start = localDeclaration.sourceStart;
		int nameEnd = localDeclaration.sourceEnd;
		name.setSourceRange(start, nameEnd - start + 1);
		variableDecl.setName(name);
		final int extraDimensions = retrieveExtraDimension(nameEnd + 1, localDeclaration.type.sourceEnd);
		variableDecl.setExtraDimensions(extraDimensions);
		Type type = convertType(localDeclaration.type);
		int typeEnd = type.getStartPosition() + type.getLength() - 1;
		// https://bugs.eclipse.org/393719 - [compiler] inconsistent warnings on iteration variables
		// compiler considers collectionExpression as within the declarationSourceEnd, DOM AST must use the shorter range to avoid overlap
		int sourceEnd = ((localDeclaration.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.IsForeachElementVariable) != 0)  
				? localDeclaration.sourceEnd : localDeclaration.declarationSourceEnd;
		int rightEnd = Math.max(typeEnd, sourceEnd);
		/*
		 * There is extra work to do to set the proper type positions
		 * See PR http://bugs.eclipse.org/bugs/show_bug.cgi?id=23284
		 */
		setTypeForSingleVariableDeclaration(variableDecl, type, extraDimensions);
		variableDecl.setSourceRange(localDeclaration.declarationSourceStart, rightEnd - localDeclaration.declarationSourceStart + 1);
		if (this.resolveBindings) {
			recordNodes(name, localDeclaration);
			recordNodes(variableDecl, localDeclaration);
			variableDecl.resolveBinding();
		}
		return variableDecl;
	}

	protected VariableDeclarationFragment convertToVariableDeclarationFragment(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration) {
		final VariableDeclarationFragment variableDeclarationFragment = new VariableDeclarationFragment(this.ast);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(fieldDeclaration.name));
		name.setSourceRange(fieldDeclaration.sourceStart, fieldDeclaration.sourceEnd - fieldDeclaration.sourceStart + 1);
		variableDeclarationFragment.setName(name);
		int start = fieldDeclaration.sourceEnd;
		int end = start;
		int extraDimensions = retrieveExtraDimension(fieldDeclaration.sourceEnd + 1, fieldDeclaration.declarationSourceEnd );
		variableDeclarationFragment.setExtraDimensions(extraDimensions);
		if (fieldDeclaration.initialization != null) {
			final Expression expression = convert(fieldDeclaration.initialization);
			variableDeclarationFragment.setInitializer(expression);
			start = expression.getStartPosition() + expression.getLength();
			end = start - 1;
		} else {
			// we need to do it even if extendedDimension is null in case of syntax error in an array initializer
			// need the exclusive range for retrieveEndOfPotentialExtendedDimensions
			int possibleEnd = retrieveEndOfPotentialExtendedDimensions(start + 1, fieldDeclaration.sourceEnd, fieldDeclaration.declarationSourceEnd);
			if (possibleEnd == Integer.MIN_VALUE) {
				end = fieldDeclaration.declarationSourceEnd;
				variableDeclarationFragment.setFlags(variableDeclarationFragment.getFlags() | ASTNode.MALFORMED);
			} if (possibleEnd < 0) {
				end = -possibleEnd;
				variableDeclarationFragment.setFlags(variableDeclarationFragment.getFlags() | ASTNode.MALFORMED);
			} else {
				end = possibleEnd;
			}
		}
		variableDeclarationFragment.setSourceRange(fieldDeclaration.sourceStart, end - fieldDeclaration.sourceStart + 1);
		if (this.resolveBindings) {
			recordNodes(name, fieldDeclaration);
			recordNodes(variableDeclarationFragment, fieldDeclaration);
			variableDeclarationFragment.resolveBinding();
		}
		return variableDeclarationFragment;
	}

	protected VariableDeclarationFragment convertToVariableDeclarationFragment(org.eclipse.jdt.internal.compiler.ast.LocalDeclaration localDeclaration) {
		final VariableDeclarationFragment variableDeclarationFragment = new VariableDeclarationFragment(this.ast);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(localDeclaration.name));
		name.setSourceRange(localDeclaration.sourceStart, localDeclaration.sourceEnd - localDeclaration.sourceStart + 1);
		variableDeclarationFragment.setName(name);
		int start = localDeclaration.sourceEnd;
		org.eclipse.jdt.internal.compiler.ast.Expression initialization = localDeclaration.initialization;
		int extraDimension = retrieveExtraDimension(localDeclaration.sourceEnd + 1, this.compilationUnitSourceLength);
		variableDeclarationFragment.setExtraDimensions(extraDimension);
		boolean hasInitialization = initialization != null;
		int end;
		if (hasInitialization) {
			final Expression expression = convert(initialization);
			variableDeclarationFragment.setInitializer(expression);
			start = expression.getStartPosition() + expression.getLength();
			end = start - 1;
		} else {
			// we need to do it even if extendedDimension is null in case of syntax error in an array initializer
			// start + 1 because we need the exclusive range for retrieveEndOfPotentialExtendedDimensions
			int possibleEnd = retrieveEndOfPotentialExtendedDimensions(start + 1, localDeclaration.sourceEnd, localDeclaration.declarationSourceEnd);
			if (possibleEnd == Integer.MIN_VALUE) {
				end = start;
				variableDeclarationFragment.setFlags(variableDeclarationFragment.getFlags() | ASTNode.MALFORMED);
			} else if (possibleEnd < 0) {
				end = -possibleEnd;
				variableDeclarationFragment.setFlags(variableDeclarationFragment.getFlags() | ASTNode.MALFORMED);
			} else {
				end = possibleEnd;
			}
		}
		variableDeclarationFragment.setSourceRange(localDeclaration.sourceStart, end - localDeclaration.sourceStart + 1);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
			recordNodes(name, localDeclaration);
			variableDeclarationFragment.resolveBinding();
		}
		return variableDeclarationFragment;
	}

	protected VariableDeclarationStatement convertToVariableDeclarationStatement(org.eclipse.jdt.internal.compiler.ast.LocalDeclaration localDeclaration) {
		final VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(localDeclaration);
		final VariableDeclarationStatement variableDeclarationStatement = new VariableDeclarationStatement(this.ast);
		variableDeclarationStatement.fragments().add(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
		}
		variableDeclarationStatement.setSourceRange(localDeclaration.declarationSourceStart, localDeclaration.declarationSourceEnd - localDeclaration.declarationSourceStart + 1);
		Type type = convertType(localDeclaration.type);
		setTypeForVariableDeclarationStatement(variableDeclarationStatement, type, variableDeclarationFragment.getExtraDimensions());
		if (localDeclaration.modifiersSourceStart != -1) {
			setModifiers(variableDeclarationStatement, localDeclaration);
		}
		return variableDeclarationStatement;
	}

	public Type convertType(TypeReference typeReference) {
		if (typeReference instanceof Wildcard) {
			final Wildcard wildcard = (Wildcard) typeReference;
			final WildcardType wildcardType = new WildcardType(this.ast);
			if (wildcard.bound != null) {
				final Type bound = convertType(wildcard.bound);
				wildcardType.setBound(bound, wildcard.kind == Wildcard.EXTENDS);
				int start = wildcard.sourceStart;
				wildcardType.setSourceRange(start, bound.getStartPosition() + bound.getLength() - start);
			} else {
				final int start = wildcard.sourceStart;
				final int end = wildcard.sourceEnd;
				wildcardType.setSourceRange(start, end - start + 1);
			}
			if (this.resolveBindings) {
				recordNodes(wildcardType, typeReference);
			}
			return wildcardType;
		}
		Type type = null;
		int sourceStart = -1;
		int length = 0;
		int dimensions = typeReference.dimensions();
		if (typeReference instanceof org.eclipse.jdt.internal.compiler.ast.SingleTypeReference) {
			// this is either an ArrayTypeReference or a SingleTypeReference
			char[] name = ((org.eclipse.jdt.internal.compiler.ast.SingleTypeReference) typeReference).getTypeName()[0];
			sourceStart = typeReference.sourceStart;
			length = typeReference.sourceEnd - typeReference.sourceStart + 1;
			// need to find out if this is an array type of primitive types or not
			if (isPrimitiveType(name)) {
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length);
				if (end == -1) {
					end = sourceStart + length - 1;
				}
				final PrimitiveType primitiveType = new PrimitiveType(this.ast);
				primitiveType.setPrimitiveTypeCode(getPrimitiveTypeCode(name));
				primitiveType.setSourceRange(sourceStart, end - sourceStart + 1);
				type = primitiveType;
			} else if (typeReference instanceof ParameterizedSingleTypeReference) {
				ParameterizedSingleTypeReference parameterizedSingleTypeReference = (ParameterizedSingleTypeReference) typeReference;
				final SimpleName simpleName = new SimpleName(this.ast);
				simpleName.internalSetIdentifier(new String(name));
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length);
				if (end == -1) {
					end = sourceStart + length - 1;
				}
				simpleName.setSourceRange(sourceStart, end - sourceStart + 1);
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						SimpleType simpleType = new SimpleType(this.ast);
						simpleType.setName(simpleName);
						simpleType.setFlags(simpleType.getFlags() | ASTNode.MALFORMED);
						simpleType.setSourceRange(sourceStart, end - sourceStart + 1);
						type = simpleType;
						if (this.resolveBindings) {
							this.recordNodes(simpleName, typeReference);
						}
						break;
					default :
						simpleType = new SimpleType(this.ast);
						simpleType.setName(simpleName);
						simpleType.setSourceRange(simpleName.getStartPosition(), simpleName.getLength());
						final ParameterizedType parameterizedType = new ParameterizedType(this.ast);
						parameterizedType.setType(simpleType);
						type = parameterizedType;
						TypeReference[] typeArguments = parameterizedSingleTypeReference.typeArguments;
						if (typeArguments != null) {
							Type type2 = null;
							for (int i = 0, max = typeArguments.length; i < max; i++) {
								type2 = convertType(typeArguments[i]);
								((ParameterizedType) type).typeArguments().add(type2);
								end = type2.getStartPosition() + type2.getLength() - 1;
							}
							end = retrieveClosingAngleBracketPosition(end + 1);
							type.setSourceRange(sourceStart, end - sourceStart + 1);
						} else {
							type.setSourceRange(sourceStart, end - sourceStart + 1);
						}
						if (this.resolveBindings) {
							this.recordNodes(simpleName, typeReference);
							this.recordNodes(simpleType, typeReference);
						}
				}
			} else {
				final SimpleName simpleName = new SimpleName(this.ast);
				simpleName.internalSetIdentifier(new String(name));
				// we need to search for the starting position of the first brace in order to set the proper length
				// PR http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length);
				if (end == -1) {
					end = sourceStart + length - 1;
				}
				simpleName.setSourceRange(sourceStart, end - sourceStart + 1);
				final SimpleType simpleType = new SimpleType(this.ast);
				simpleType.setName(simpleName);
				type = simpleType;
				type.setSourceRange(sourceStart, end - sourceStart + 1);
				type = simpleType;
				if (this.resolveBindings) {
					this.recordNodes(simpleName, typeReference);
				}
			}
			if (dimensions != 0) {
				type = this.ast.newArrayType(type, dimensions);
				type.setSourceRange(sourceStart, length);
				ArrayType subarrayType = (ArrayType) type;
				int index = dimensions - 1;
				while (index > 0) {
					subarrayType = (ArrayType) subarrayType.getComponentType();
					int end = retrieveProperRightBracketPosition(index, sourceStart);
					subarrayType.setSourceRange(sourceStart, end - sourceStart + 1);
					index--;
				}
				if (this.resolveBindings) {
					// store keys for inner types
					completeRecord((ArrayType) type, typeReference);
				}
			}
		} else {
			if (typeReference instanceof ParameterizedQualifiedTypeReference) {
				ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference = (ParameterizedQualifiedTypeReference) typeReference;
				char[][] tokens = parameterizedQualifiedTypeReference.tokens;
				TypeReference[][] typeArguments = parameterizedQualifiedTypeReference.typeArguments;
				long[] positions = parameterizedQualifiedTypeReference.sourcePositions;
				sourceStart = (int)(positions[0]>>>32);
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL : {
							char[][] name = ((org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference).getTypeName();
							int nameLength = name.length;
							sourceStart = (int)(positions[0]>>>32);
							length = (int)(positions[nameLength - 1] & 0xFFFFFFFF) - sourceStart + 1;
							Name qualifiedName = this.setQualifiedNameNameAndSourceRanges(name, positions, typeReference);
							final SimpleType simpleType = new SimpleType(this.ast);
							simpleType.setName(qualifiedName);
							simpleType.setSourceRange(sourceStart, length);
							type = simpleType;
						}
						break;
					default :
						if (typeArguments != null) {
							int numberOfEnclosingType = 0;
                            int startingIndex = 0;
                            int endingIndex = 0;
							for (int i = 0, max = typeArguments.length; i < max; i++) {
								if (typeArguments[i] != null) {
									numberOfEnclosingType++;
								} else if (numberOfEnclosingType == 0) {
                                    endingIndex++;
                                }
							}
							Name name = null;
							if (endingIndex - startingIndex == 0) {
								final SimpleName simpleName = new SimpleName(this.ast);
								simpleName.internalSetIdentifier(new String(tokens[startingIndex]));
								recordPendingNameScopeResolution(simpleName);
								int start = (int)(positions[startingIndex]>>>32);
								int end = (int) positions[startingIndex];
								simpleName.setSourceRange(start, end - start + 1);
								simpleName.index = 1;
								name = simpleName;
								if (this.resolveBindings) {
		 							recordNodes(simpleName, typeReference);
								}
							} else {
								name = this.setQualifiedNameNameAndSourceRanges(tokens, positions, endingIndex, typeReference);
							}
							SimpleType simpleType = new SimpleType(this.ast);
							simpleType.setName(name);
							int start = (int)(positions[startingIndex]>>>32);
							int end = (int) positions[endingIndex];
							simpleType.setSourceRange(start, end - start + 1);
							ParameterizedType parameterizedType = new ParameterizedType(this.ast);
							parameterizedType.setType(simpleType);
                            if (this.resolveBindings) {
                                recordNodes(simpleType, typeReference);
                                recordNodes(parameterizedType, typeReference);
                            }
							start = simpleType.getStartPosition();
							end = start + simpleType.getLength() - 1;
							for (int i = 0, max = typeArguments[endingIndex].length; i < max; i++) {
								final Type type2 = convertType(typeArguments[endingIndex][i]);
								parameterizedType.typeArguments().add(type2);
								end = type2.getStartPosition() + type2.getLength() - 1;
							}
							int indexOfEnclosingType = 1;
							parameterizedType.index = indexOfEnclosingType;
							end = retrieveClosingAngleBracketPosition(end + 1);
							length = end + 1;
							parameterizedType.setSourceRange(start, end - start + 1);
							startingIndex = endingIndex + 1;
							Type currentType = parameterizedType;
							while(startingIndex < typeArguments.length) {
								SimpleName simpleName = new SimpleName(this.ast);
								simpleName.internalSetIdentifier(new String(tokens[startingIndex]));
								simpleName.index = startingIndex + 1;
								start = (int)(positions[startingIndex]>>>32);
								end = (int) positions[startingIndex];
								simpleName.setSourceRange(start, end - start + 1);
								recordPendingNameScopeResolution(simpleName);
								QualifiedType qualifiedType = new QualifiedType(this.ast);
								qualifiedType.setQualifier(currentType);
								qualifiedType.setName(simpleName);
                                if (this.resolveBindings) {
                                    recordNodes(simpleName, typeReference);
                                    recordNodes(qualifiedType, typeReference);
                                }
								start = currentType.getStartPosition();
								end = simpleName.getStartPosition() + simpleName.getLength() - 1;
								qualifiedType.setSourceRange(start, end - start + 1);
								indexOfEnclosingType++;
								if (typeArguments[startingIndex] != null) {
	                               	qualifiedType.index = indexOfEnclosingType;
									ParameterizedType parameterizedType2 = new ParameterizedType(this.ast);
									parameterizedType2.setType(qualifiedType);
 									parameterizedType2.index = indexOfEnclosingType;
                                   if (this.resolveBindings) {
                                        recordNodes(parameterizedType2, typeReference);
                                    }
									for (int i = 0, max = typeArguments[startingIndex].length; i < max; i++) {
										final Type type2 = convertType(typeArguments[startingIndex][i]);
										parameterizedType2.typeArguments().add(type2);
										end = type2.getStartPosition() + type2.getLength() - 1;
									}
									end = retrieveClosingAngleBracketPosition(end + 1);
									length = end + 1;
									parameterizedType2.setSourceRange(start, end - start + 1);
									currentType = parameterizedType2;
								} else {
									currentType = qualifiedType;
                               		qualifiedType.index = indexOfEnclosingType;
								}
								startingIndex++;
							}
							if (this.resolveBindings) {
								this.recordNodes(currentType, typeReference);
							}
							type = currentType;
							length -= sourceStart;
						}
				}
			} else if (typeReference instanceof org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) {
				char[][] name = ((org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference).getTypeName();
				int nameLength = name.length;
				long[] positions = ((org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference).sourcePositions;
				sourceStart = (int)(positions[0]>>>32);
				length = (int)(positions[nameLength - 1] & 0xFFFFFFFF) - sourceStart + 1;
				final Name qualifiedName = this.setQualifiedNameNameAndSourceRanges(name, positions, typeReference);
				final SimpleType simpleType = new SimpleType(this.ast);
				simpleType.setName(qualifiedName);
				type = simpleType;
				type.setSourceRange(sourceStart, length);
			} else {
				TypeReference[] typeReferences = ((org.eclipse.jdt.internal.compiler.ast.UnionTypeReference) typeReference).typeReferences;
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
					case AST.JLS3_INTERNAL :
						// recovery
						type = this.convertType(typeReferences[0]);
						int start = typeReference.sourceStart;
						int endPosition = typeReference.sourceEnd;
						length = endPosition - start + 1;
						type.setSourceRange(start, length);
						type.setFlags(type.getFlags() | ASTNode.MALFORMED);
						break;
					default:
						// union type reference
						final UnionType unionType = new UnionType(this.ast);
						for (int i = 0, max = typeReferences.length; i < max; i++) {
							unionType.types().add(this.convertType(typeReferences[i]));
						}
						type = unionType;
						List types = unionType.types();
						int size = types.size();
						start = ((Type) types.get(0)).getStartPosition();
						Type lastType = (Type) types.get(size - 1);
						endPosition = lastType.getStartPosition() + lastType.getLength();
						length = endPosition - start; /* + 1 - 1 == 0 */
						type.setSourceRange(start, length);
				}
			}

			length = typeReference.sourceEnd - sourceStart + 1;
			if (dimensions != 0) {
				type = this.ast.newArrayType(type, dimensions);
				if (this.resolveBindings) {
					completeRecord((ArrayType) type, typeReference);
				}
				int end = retrieveEndOfDimensionsPosition(sourceStart+length, this.compilationUnitSourceLength);
				if (end != -1) {
					type.setSourceRange(sourceStart, end - sourceStart + 1);
				} else {
					type.setSourceRange(sourceStart, length);
				}
				ArrayType subarrayType = (ArrayType) type;
				int index = dimensions - 1;
				while (index > 0) {
					subarrayType = (ArrayType) subarrayType.getComponentType();
					end = retrieveProperRightBracketPosition(index, sourceStart);
					subarrayType.setSourceRange(sourceStart, end - sourceStart + 1);
					index--;
				}
			}
		}
		if (this.resolveBindings) {
			this.recordNodes(type, typeReference);
		}
		boolean sawDiamond = false;
		if (typeReference instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference pstr = (ParameterizedSingleTypeReference) typeReference;
			if (pstr.typeArguments == TypeReference.NO_TYPE_ARGUMENTS) {
				sawDiamond = true;
			}
		} else if (typeReference instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference pqtr = (ParameterizedQualifiedTypeReference) typeReference;
			for (int i = 0, len = pqtr.typeArguments.length; i < len; i++) {
				if (pqtr.typeArguments[i] == TypeReference.NO_TYPE_ARGUMENTS) {
					sawDiamond = true;
					break;
				}
			}
		} 
		if (sawDiamond) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
				case AST.JLS3_INTERNAL :
					type.setFlags(type.getFlags() | ASTNode.MALFORMED);
			}
		}
		return type;
	}

	protected Comment createComment(int[] positions) {
		// Create comment node
		Comment comment = null;
		int start = positions[0];
		int end = positions[1];
		if (positions[1]>0) { // Javadoc comments have positive end position
			Javadoc docComment = this.docParser.parse(positions);
			if (docComment == null) return null;
			comment = docComment;
		} else {
			end = -end;
			if (positions[0] == 0) { // we cannot know without testing chars again
				if (this.docParser.scanner.source[1] == '/') {
					comment = new LineComment(this.ast);
				} else {
					comment = new BlockComment(this.ast);
				}
			}
			else if (positions[0]>0) { // Block comment have positive start position
				comment = new BlockComment(this.ast);
			} else { // Line comment have negative start and end position
				start = -start;
				comment = new LineComment(this.ast);
			}
			comment.setSourceRange(start, end - start);
		}
		return comment;
	}

	protected Statement createFakeEmptyStatement(org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement == null) return null;
		EmptyStatement emptyStatement = new EmptyStatement(this.ast);
		emptyStatement.setFlags(emptyStatement.getFlags() | ASTNode.MALFORMED);
		int start = statement.sourceStart;
		int end = statement.sourceEnd;
		emptyStatement.setSourceRange(start, end - start + 1);
		return emptyStatement;
	}
	/**
	 * @return a new modifier
	 */
	private Modifier createModifier(ModifierKeyword keyword) {
		final Modifier modifier = new Modifier(this.ast);
		modifier.setKeyword(keyword);
		int start = this.scanner.getCurrentTokenStartPosition();
		int end = this.scanner.getCurrentTokenEndPosition();
		modifier.setSourceRange(start, end - start + 1);
		return modifier;
	}

	protected InfixExpression.Operator getOperatorFor(int operatorID) {
		switch (operatorID) {
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.EQUAL_EQUAL :
				return InfixExpression.Operator.EQUALS;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.LESS_EQUAL :
				return InfixExpression.Operator.LESS_EQUALS;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.GREATER_EQUAL :
				return InfixExpression.Operator.GREATER_EQUALS;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.NOT_EQUAL :
				return InfixExpression.Operator.NOT_EQUALS;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.LEFT_SHIFT :
				return InfixExpression.Operator.LEFT_SHIFT;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.RIGHT_SHIFT :
				return InfixExpression.Operator.RIGHT_SHIFT_SIGNED;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.UNSIGNED_RIGHT_SHIFT :
				return InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.OR_OR :
				return InfixExpression.Operator.CONDITIONAL_OR;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.AND_AND :
				return InfixExpression.Operator.CONDITIONAL_AND;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS :
				return InfixExpression.Operator.PLUS;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS :
				return InfixExpression.Operator.MINUS;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.REMAINDER :
				return InfixExpression.Operator.REMAINDER;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.XOR :
				return InfixExpression.Operator.XOR;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.AND :
				return InfixExpression.Operator.AND;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.MULTIPLY :
				return InfixExpression.Operator.TIMES;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.OR :
				return InfixExpression.Operator.OR;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.DIVIDE :
				return InfixExpression.Operator.DIVIDE;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.GREATER :
				return InfixExpression.Operator.GREATER;
			case org.eclipse.jdt.internal.compiler.ast.OperatorIds.LESS :
				return InfixExpression.Operator.LESS;
		}
		return null;
	}

	protected PrimitiveType.Code getPrimitiveTypeCode(char[] name) {
		switch(name[0]) {
			case 'i' :
				if (name.length == 3 && name[1] == 'n' && name[2] == 't') {
					return PrimitiveType.INT;
				}
				break;
			case 'l' :
				if (name.length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g') {
					return PrimitiveType.LONG;
				}
				break;
			case 'd' :
				if (name.length == 6
					 && name[1] == 'o'
					 && name[2] == 'u'
					 && name[3] == 'b'
					 && name[4] == 'l'
					 && name[5] == 'e') {
					return PrimitiveType.DOUBLE;
				}
				break;
			case 'f' :
				if (name.length == 5
					 && name[1] == 'l'
					 && name[2] == 'o'
					 && name[3] == 'a'
					 && name[4] == 't') {
					return PrimitiveType.FLOAT;
				}
				break;
			case 'b' :
				if (name.length == 4
					 && name[1] == 'y'
					 && name[2] == 't'
					 && name[3] == 'e') {
					return PrimitiveType.BYTE;
				} else
					if (name.length == 7
						 && name[1] == 'o'
						 && name[2] == 'o'
						 && name[3] == 'l'
						 && name[4] == 'e'
						 && name[5] == 'a'
						 && name[6] == 'n') {
					return PrimitiveType.BOOLEAN;
				}
				break;
			case 'c' :
				if (name.length == 4
					 && name[1] == 'h'
					 && name[2] == 'a'
					 && name[3] == 'r') {
					return PrimitiveType.CHAR;
				}
				break;
			case 's' :
				if (name.length == 5
					 && name[1] == 'h'
					 && name[2] == 'o'
					 && name[3] == 'r'
					 && name[4] == 't') {
					return PrimitiveType.SHORT;
				}
				break;
			case 'v' :
				if (name.length == 4
					 && name[1] == 'o'
					 && name[2] == 'i'
					 && name[3] == 'd') {
					return PrimitiveType.VOID;
				}
		}
		return null; // cannot be reached
	}

	protected boolean isPrimitiveType(char[] name) {
		switch(name[0]) {
			case 'i' :
				if (name.length == 3 && name[1] == 'n' && name[2] == 't') {
					return true;
				}
				return false;
			case 'l' :
				if (name.length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g') {
					return true;
				}
				return false;
			case 'd' :
				if (name.length == 6
					 && name[1] == 'o'
					 && name[2] == 'u'
					 && name[3] == 'b'
					 && name[4] == 'l'
					 && name[5] == 'e') {
					return true;
				}
				return false;
			case 'f' :
				if (name.length == 5
					 && name[1] == 'l'
					 && name[2] == 'o'
					 && name[3] == 'a'
					 && name[4] == 't') {
					return true;
				}
				return false;
			case 'b' :
				if (name.length == 4
					 && name[1] == 'y'
					 && name[2] == 't'
					 && name[3] == 'e') {
					return true;
				} else
					if (name.length == 7
						 && name[1] == 'o'
						 && name[2] == 'o'
						 && name[3] == 'l'
						 && name[4] == 'e'
						 && name[5] == 'a'
						 && name[6] == 'n') {
					return true;
				}
				return false;
			case 'c' :
				if (name.length == 4
					 && name[1] == 'h'
					 && name[2] == 'a'
					 && name[3] == 'r') {
					return true;
				}
				return false;
			case 's' :
				if (name.length == 5
					 && name[1] == 'h'
					 && name[2] == 'o'
					 && name[3] == 'r'
					 && name[4] == 't') {
					return true;
				}
				return false;
			case 'v' :
				if (name.length == 4
					 && name[1] == 'o'
					 && name[2] == 'i'
					 && name[3] == 'd') {
					return true;
				}
				return false;
		}
		return false;
	}

	private void lookupForScopes() {
		if (this.pendingNameScopeResolution != null) {
			for (Iterator iterator = this.pendingNameScopeResolution.iterator(); iterator.hasNext(); ) {
				Name name = (Name) iterator.next();
				this.ast.getBindingResolver().recordScope(name, lookupScope(name));
			}
		}
		if (this.pendingThisExpressionScopeResolution != null) {
			for (Iterator iterator = this.pendingThisExpressionScopeResolution.iterator(); iterator.hasNext(); ) {
				ThisExpression thisExpression = (ThisExpression) iterator.next();
				this.ast.getBindingResolver().recordScope(thisExpression, lookupScope(thisExpression));
			}
		}

	}

	private BlockScope lookupScope(ASTNode node) {
		ASTNode currentNode = node;
		while(currentNode != null
			&&!(currentNode instanceof MethodDeclaration)
			&& !(currentNode instanceof Initializer)
			&& !(currentNode instanceof FieldDeclaration)
			&& !(currentNode instanceof AbstractTypeDeclaration)) {
			currentNode = currentNode.getParent();
		}
		if (currentNode == null) {
			return null;
		}
		if (currentNode instanceof Initializer) {
			Initializer initializer = (Initializer) currentNode;
			while(!(currentNode instanceof AbstractTypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			if (currentNode instanceof TypeDeclaration
				|| currentNode instanceof EnumDeclaration
				|| currentNode instanceof AnnotationTypeDeclaration) {
				org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.ast.getBindingResolver().getCorrespondingNode(currentNode);
				if ((initializer.getModifiers() & Modifier.STATIC) != 0) {
					return typeDecl.staticInitializerScope;
				} else {
					return typeDecl.initializerScope;
				}
			}
		} else if (currentNode instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) currentNode;
			while(!(currentNode instanceof AbstractTypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.ast.getBindingResolver().getCorrespondingNode(currentNode);
			if ((fieldDeclaration.getModifiers() & Modifier.STATIC) != 0) {
				return typeDecl.staticInitializerScope;
			} else {
				return typeDecl.initializerScope;
			}
		} else if (currentNode instanceof AbstractTypeDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.ast.getBindingResolver().getCorrespondingNode(currentNode);
			return typeDecl.initializerScope;
		}
		AbstractMethodDeclaration abstractMethodDeclaration = (AbstractMethodDeclaration) this.ast.getBindingResolver().getCorrespondingNode(currentNode);
		return abstractMethodDeclaration.scope;
	}

	protected void recordName(Name name, org.eclipse.jdt.internal.compiler.ast.ASTNode compilerNode) {
		if (compilerNode != null) {
			recordNodes(name, compilerNode);
			if (compilerNode instanceof org.eclipse.jdt.internal.compiler.ast.TypeReference) {
				org.eclipse.jdt.internal.compiler.ast.TypeReference typeRef = (org.eclipse.jdt.internal.compiler.ast.TypeReference) compilerNode;
				if (name.isQualifiedName()) {
					SimpleName simpleName = null;
					while (name.isQualifiedName()) {
						simpleName = ((QualifiedName) name).getName();
						recordNodes(simpleName, typeRef);
						name = ((QualifiedName) name).getQualifier();
						recordNodes(name, typeRef);
					}
				}
			}
		}
	}

	protected void recordNodes(ASTNode node, org.eclipse.jdt.internal.compiler.ast.ASTNode oldASTNode) {
		this.ast.getBindingResolver().store(node, oldASTNode);
	}

	protected void recordNodes(org.eclipse.jdt.internal.compiler.ast.Javadoc javadoc, TagElement tagElement) {
		Iterator fragments = tagElement.fragments().listIterator();
		while (fragments.hasNext()) {
			ASTNode node = (ASTNode) fragments.next();
			if (node.getNodeType() == ASTNode.MEMBER_REF) {
				MemberRef memberRef = (MemberRef) node;
				Name name = memberRef.getName();
				// get compiler node and record nodes
				int start = name.getStartPosition();
				org.eclipse.jdt.internal.compiler.ast.ASTNode compilerNode = javadoc.getNodeStartingAt(start);
				if (compilerNode!= null) {
					recordNodes(name, compilerNode);
					recordNodes(node, compilerNode);
				}
				// Replace qualifier to have all nodes recorded
				if (memberRef.getQualifier() != null) {
					org.eclipse.jdt.internal.compiler.ast.TypeReference typeRef = null;
					if (compilerNode instanceof JavadocFieldReference) {
						org.eclipse.jdt.internal.compiler.ast.Expression expression = ((JavadocFieldReference)compilerNode).receiver;
						if (expression instanceof org.eclipse.jdt.internal.compiler.ast.TypeReference) {
							typeRef = (org.eclipse.jdt.internal.compiler.ast.TypeReference) expression;
						}
					}
					else if (compilerNode instanceof JavadocMessageSend) {
						org.eclipse.jdt.internal.compiler.ast.Expression expression = ((JavadocMessageSend)compilerNode).receiver;
						if (expression instanceof org.eclipse.jdt.internal.compiler.ast.TypeReference) {
							typeRef = (org.eclipse.jdt.internal.compiler.ast.TypeReference) expression;
						}
					}
					if (typeRef != null) {
						recordName(memberRef.getQualifier(), typeRef);
					}
				}
			} else if (node.getNodeType() == ASTNode.METHOD_REF) {
				MethodRef methodRef = (MethodRef) node;
				Name name = methodRef.getName();
				// get method name start position
				int start = methodRef.getStartPosition();
				this.scanner.resetTo(start, start + name.getStartPosition()+name.getLength());
				int token;
				try {
					nextToken: while((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF && token != TerminalTokens.TokenNameLPAREN)  {
						if (token == TerminalTokens.TokenNameERROR && this.scanner.currentCharacter == '#') {
							start = this.scanner.getCurrentTokenEndPosition()+1;
							break nextToken;
						}
					}
				}
				catch(InvalidInputException e) {
					// ignore
				}
				// get compiler node and record nodes
				org.eclipse.jdt.internal.compiler.ast.ASTNode compilerNode = javadoc.getNodeStartingAt(start);
				// record nodes
				if (compilerNode != null) {
					recordNodes(methodRef, compilerNode);
					// get type ref
					org.eclipse.jdt.internal.compiler.ast.TypeReference typeRef = null;
					if (compilerNode instanceof org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression) {
						typeRef = ((org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression)compilerNode).type;
						if (typeRef != null) recordNodes(name, compilerNode);
					}
					else if (compilerNode instanceof org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend) {
						org.eclipse.jdt.internal.compiler.ast.Expression expression = ((org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend)compilerNode).receiver;
						if (expression instanceof org.eclipse.jdt.internal.compiler.ast.TypeReference) {
							typeRef = (org.eclipse.jdt.internal.compiler.ast.TypeReference) expression;
						}
						recordNodes(name, compilerNode);
					}
					// record name and qualifier
					if (typeRef != null && methodRef.getQualifier() != null) {
						recordName(methodRef.getQualifier(), typeRef);
					}
				}
				// Resolve parameters
				Iterator parameters = methodRef.parameters().listIterator();
				while (parameters.hasNext()) {
					MethodRefParameter param = (MethodRefParameter) parameters.next();
					org.eclipse.jdt.internal.compiler.ast.Expression expression = (org.eclipse.jdt.internal.compiler.ast.Expression) javadoc.getNodeStartingAt(param.getStartPosition());
					if (expression != null) {
						recordNodes(param, expression);
						if (expression instanceof JavadocArgumentExpression) {
							JavadocArgumentExpression argExpr = (JavadocArgumentExpression) expression;
							org.eclipse.jdt.internal.compiler.ast.TypeReference typeRef = argExpr.argument.type;
							if (this.ast.apiLevel >= AST.JLS3_INTERNAL) {
								param.setVarargs(argExpr.argument.isVarArgs());
							}
							recordNodes(param.getType(), typeRef);
							if (param.getType().isSimpleType()) {
								recordName(((SimpleType)param.getType()).getName(), typeRef);
							} else if (param.getType().isArrayType()) {
								Type type = ((ArrayType) param.getType()).getElementType();
								recordNodes(type, typeRef);
								if (type.isSimpleType()) {
									recordName(((SimpleType)type).getName(), typeRef);
								}
							}
						}
					}
				}
			} else if (node.getNodeType() == ASTNode.SIMPLE_NAME ||
					node.getNodeType() == ASTNode.QUALIFIED_NAME) {
				org.eclipse.jdt.internal.compiler.ast.ASTNode compilerNode = javadoc.getNodeStartingAt(node.getStartPosition());
				recordName((Name) node, compilerNode);
			} else if (node.getNodeType() == ASTNode.TAG_ELEMENT) {
				// resolve member and method references binding
				recordNodes(javadoc, (TagElement) node);
			}
		}
	}

	protected void recordPendingNameScopeResolution(Name name) {
		if (this.pendingNameScopeResolution == null) {
			this.pendingNameScopeResolution = new HashSet();
		}
		this.pendingNameScopeResolution.add(name);
	}

	protected void recordPendingThisExpressionScopeResolution(ThisExpression thisExpression) {
		if (this.pendingThisExpressionScopeResolution == null) {
			this.pendingThisExpressionScopeResolution = new HashSet();
		}
		this.pendingThisExpressionScopeResolution.add(thisExpression);
	}

	/**
	 * Remove whitespaces and comments before and after the expression.
	 */
	private void trimWhiteSpacesAndComments(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		int start = expression.sourceStart;
		int end = expression.sourceEnd;
		int token;
		int trimLeftPosition = expression.sourceStart;
		int trimRightPosition = expression.sourceEnd;
		boolean first = true;
		Scanner removeBlankScanner = this.ast.scanner;
		try {
			removeBlankScanner.setSource(this.compilationUnitSource);
			removeBlankScanner.resetTo(start, end);
			while (true) {
				token = removeBlankScanner.getNextToken();
				switch (token) {
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
					case TerminalTokens.TokenNameCOMMENT_LINE :
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (first) {
							trimLeftPosition = removeBlankScanner.currentPosition;
						}
						break;
					case TerminalTokens.TokenNameWHITESPACE :
						if (first) {
							trimLeftPosition = removeBlankScanner.currentPosition;
						}
						break;
					case TerminalTokens.TokenNameEOF :
						expression.sourceStart = trimLeftPosition;
						expression.sourceEnd = trimRightPosition;
						return;
					default :
						/*
						 * if we find something else than a whitespace or a comment,
						 * then we reset the trimRigthPosition to the expression
						 * source end.
						 */
						trimRightPosition = removeBlankScanner.currentPosition - 1;
						first = false;
				}
			}
		} catch (InvalidInputException e){
			// ignore
		}
	}

	/**
	 * Remove potential trailing comment by settings the source end on the closing parenthesis
	 */
	protected void removeLeadingAndTrailingCommentsFromLiteral(ASTNode node) {
		int start = node.getStartPosition();
		this.scanner.resetTo(start, start + node.getLength());
		int token;
		int startPosition = -1;
		try {
			while((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF)  {
				switch(token) {
					case TerminalTokens.TokenNameIntegerLiteral :
					case TerminalTokens.TokenNameFloatingPointLiteral :
					case TerminalTokens.TokenNameLongLiteral :
					case TerminalTokens.TokenNameDoubleLiteral :
					case TerminalTokens.TokenNameCharacterLiteral :
						if (startPosition == -1) {
							startPosition = this.scanner.startPosition;
						}
						int end = this.scanner.currentPosition;
						node.setSourceRange(startPosition, end - startPosition);
						return;
					case TerminalTokens.TokenNameMINUS :
						startPosition = this.scanner.startPosition;
						break;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}

	/**
	 * This method is used to retrieve the end position of the block.
	 * @return the dimension found, -1 if none
	 */
	protected int retrieveClosingAngleBracketPosition(int start) {
		this.scanner.resetTo(start, this.compilationUnitSourceLength);
		this.scanner.returnOnlyGreater = true;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameGREATER:
						return this.scanner.currentPosition - 1;
					case TerminalTokens.TokenNameLESS:
						// TokenNameLESS can only be found if the current type has a diamond, start is located before the '<'
						continue;
					default:
						return start;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		this.scanner.returnOnlyGreater = false;
		return start;
	}

	/**
	 * This method is used to set the right end position for expression
	 * statement. The actual AST nodes don't include the trailing semicolon.
	 * This method fixes the length of the corresponding node.
	 */
	protected void retrieveColonPosition(ASTNode node) {
		int start = node.getStartPosition();
		int length = node.getLength();
		int end = start + length;
		this.scanner.resetTo(end, this.compilationUnitSourceLength);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameCOLON:
						node.setSourceRange(start, this.scanner.currentPosition - start);
						return;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}
	/**
	 * This method is used to retrieve the start position of the Ellipsis
	 */
	protected int retrieveEllipsisStartPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameELLIPSIS:
						return this.scanner.startPosition - 1;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;

	}
	/**
	 * This method is used to retrieve the end position of the block.
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveEndBlockPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		int count = 0;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACE://110
						count++;
						break;
					case TerminalTokens.TokenNameRBRACE://95
						count--;
						if (count == 0) {
							return this.scanner.currentPosition - 1;
						}
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	protected int retrieveSemiColonPosition(Expression node) {
		int start = node.getStartPosition();
		int length = node.getLength();
		int end = start + length;
		this.scanner.resetTo(end, this.compilationUnitSourceLength);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameSEMICOLON:
						return this.scanner.currentPosition - 1;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the ending position for a type declaration when the dimension is right after the type
	 * name.
	 * For example:
	 *    int[] i; => return 5, but int i[] => return -1;
	 * @return int the dimension found
	 */
	protected int retrieveEndOfDimensionsPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		int foundPosition = -1;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACKET:
					case TerminalTokens.TokenNameCOMMENT_BLOCK:
					case TerminalTokens.TokenNameCOMMENT_JAVADOC:
					case TerminalTokens.TokenNameCOMMENT_LINE:
						break;
					case TerminalTokens.TokenNameRBRACKET://166
						foundPosition = this.scanner.currentPosition - 1;
						break;
					default:
						return foundPosition;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return foundPosition;
	}

	/**
	 * This method is used to retrieve the position just before the left bracket.
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveEndOfElementTypeNamePosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameIdentifier:
					case TerminalTokens.TokenNamebyte:
					case TerminalTokens.TokenNamechar:
					case TerminalTokens.TokenNamedouble:
					case TerminalTokens.TokenNamefloat:
					case TerminalTokens.TokenNameint:
					case TerminalTokens.TokenNamelong:
					case TerminalTokens.TokenNameshort:
					case TerminalTokens.TokenNameboolean:
						return this.scanner.currentPosition - 1;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the position after the right parenthesis.
	 * @return int the position found
	 */
	protected int retrieveEndOfRightParenthesisPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameRPAREN:
						return this.scanner.currentPosition;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the array dimension declared after the
	 * name of a local or a field declaration.
	 * For example:
	 *    int i, j[] = null, k[][] = {{}};
	 *    It should return 0 for i, 1 for j and 2 for k.
	 * @return int the dimension found
	 */
	protected int retrieveExtraDimension(int start, int end) {
		this.scanner.resetTo(start, end);
		int dimensions = 0;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACKET:
					case TerminalTokens.TokenNameCOMMENT_BLOCK:
					case TerminalTokens.TokenNameCOMMENT_JAVADOC:
					case TerminalTokens.TokenNameCOMMENT_LINE:
						break;
					case TerminalTokens.TokenNameRBRACKET://166
						dimensions++;
						break;
					default:
						return dimensions;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return dimensions;
	}

	protected void retrieveIdentifierAndSetPositions(int start, int end, Name name) {
		this.scanner.resetTo(start, end);
		int token;
		try {
			while((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF)  {
				if (token == TerminalTokens.TokenNameIdentifier) {
					int startName = this.scanner.startPosition;
					int endName = this.scanner.currentPosition - 1;
					name.setSourceRange(startName, endName - startName + 1);
					return;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}

	/**
	 * This method is used to retrieve the start position of the block.
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveIdentifierEndPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameIdentifier://110
						return this.scanner.getCurrentTokenEndPosition();
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve position before the next comma or semi-colon.
	 * @param initializerEnd the given initializer end exclusive
	 * @return int the position found.
	 */
	protected int retrieveEndOfPotentialExtendedDimensions(int initializerEnd, int nameEnd, int end) {
		this.scanner.resetTo(initializerEnd, end);
		boolean hasTokens = false;
		int balance = 0;
		int pos = initializerEnd > nameEnd ? initializerEnd - 1 : nameEnd;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				hasTokens = true;
				switch(token) {
					case TerminalTokens.TokenNameLBRACE :
					case TerminalTokens.TokenNameLBRACKET :
						balance++;
						break;
					case TerminalTokens.TokenNameRBRACKET :
					case TerminalTokens.TokenNameRBRACE :
						balance --;
						pos = this.scanner.currentPosition - 1;
						break;
					case TerminalTokens.TokenNameCOMMA :
						if (balance == 0) return pos;
						// case where a missing closing brace doesn't close an array initializer
						pos = this.scanner.currentPosition - 1;
						break;
					case TerminalTokens.TokenNameSEMICOLON :
						if (balance == 0) return pos;
						return -pos;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		// no token, we simply return pos as the right position
		return hasTokens ? Integer.MIN_VALUE : pos;
	}

	protected int retrieveProperRightBracketPosition(int bracketNumber, int start) {
		this.scanner.resetTo(start, this.compilationUnitSourceLength);
		try {
			int token, count = 0;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameRBRACKET:
						count++;
						if (count == bracketNumber) {
							return this.scanner.currentPosition - 1;
						}
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve position before the next right brace or semi-colon.
	 * @return int the position found.
	 */
	protected int retrieveRightBraceOrSemiColonPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameRBRACE :
						return this.scanner.currentPosition - 1;
					case TerminalTokens.TokenNameSEMICOLON :
						return this.scanner.currentPosition - 1;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve position before the next right brace or semi-colon.
	 * @return int the position found.
	 */
	protected int retrieveRightBrace(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameRBRACE :
						return this.scanner.currentPosition - 1;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the position of the right bracket.
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveRightBracketPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			int balance = 0;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACKET :
						balance++;
						break;
					case TerminalTokens.TokenNameRBRACKET :
						balance--;
						if (balance == 0) return this.scanner.currentPosition - 1;
						break;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the start position of the block.
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveStartBlockPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACE://110
						return this.scanner.startPosition;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the starting position of the catch keyword.
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveStartingCatchPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNamecatch://225
						return this.scanner.startPosition;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	public void setAST(AST ast) {
		this.ast = ast;
		this.docParser = new DocCommentParser(this.ast, this.scanner, this.insideComments);
	}

	protected void setModifiers(AnnotationTypeDeclaration typeDecl, org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		this.scanner.resetTo(typeDeclaration.declarationSourceStart, typeDeclaration.sourceStart);
		this.setModifiers(typeDecl, typeDeclaration.annotations, typeDeclaration.sourceStart);
	}

	protected void setModifiers(AnnotationTypeMemberDeclaration annotationTypeMemberDecl, org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration annotationTypeMemberDeclaration) {
		this.scanner.resetTo(annotationTypeMemberDeclaration.declarationSourceStart, annotationTypeMemberDeclaration.sourceStart);
		this.setModifiers(annotationTypeMemberDecl, annotationTypeMemberDeclaration.annotations, annotationTypeMemberDeclaration.sourceStart);
	}

	/**
	 * @param bodyDeclaration
	 */
	protected void setModifiers(BodyDeclaration bodyDeclaration, org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations, int modifiersEnd) {
		this.scanner.tokenizeWhiteSpace = false;
		try {
			int token;
			int indexInAnnotations = 0;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				IExtendedModifier modifier = null;
				switch(token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT :
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, modifiersEnd);
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
					case TerminalTokens.TokenNameCOMMENT_LINE :
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						break;
					default :
						// there is some syntax errors in source code
						break;
				}
				if (modifier != null) {
					bodyDeclaration.modifiers().add(modifier);
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}

	protected void setModifiers(EnumDeclaration enumDeclaration, org.eclipse.jdt.internal.compiler.ast.TypeDeclaration enumDeclaration2) {
		this.scanner.resetTo(enumDeclaration2.declarationSourceStart, enumDeclaration2.sourceStart);
		this.setModifiers(enumDeclaration, enumDeclaration2.annotations, enumDeclaration2.sourceStart);
	}

	protected void setModifiers(EnumConstantDeclaration enumConstantDeclaration, org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				enumConstantDeclaration.internalSetModifiers(fieldDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag);
				if (fieldDeclaration.annotations != null) {
					enumConstantDeclaration.setFlags(enumConstantDeclaration.getFlags() | ASTNode.MALFORMED);
				}
				break;
			default :
				this.scanner.resetTo(fieldDeclaration.declarationSourceStart, fieldDeclaration.sourceStart);
				this.setModifiers(enumConstantDeclaration, fieldDeclaration.annotations, fieldDeclaration.sourceStart);
		}
	}

	/**
	 * @param fieldDeclaration
	 * @param fieldDecl
	 */
	protected void setModifiers(FieldDeclaration fieldDeclaration, org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDecl) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				fieldDeclaration.internalSetModifiers(fieldDecl.modifiers & ExtraCompilerModifiers.AccJustFlag);
				if (fieldDecl.annotations != null) {
					fieldDeclaration.setFlags(fieldDeclaration.getFlags() | ASTNode.MALFORMED);
				}
				break;
			default :
				this.scanner.resetTo(fieldDecl.declarationSourceStart, fieldDecl.sourceStart);
				this.setModifiers(fieldDeclaration, fieldDecl.annotations, fieldDecl.sourceStart);
		}
	}

	/**
	 * @param initializer
	 * @param oldInitializer
	 */
	protected void setModifiers(Initializer initializer, org.eclipse.jdt.internal.compiler.ast.Initializer oldInitializer) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL:
				initializer.internalSetModifiers(oldInitializer.modifiers & ExtraCompilerModifiers.AccJustFlag);
				if (oldInitializer.annotations != null) {
					initializer.setFlags(initializer.getFlags() | ASTNode.MALFORMED);
				}
				break;
			default :
				this.scanner.resetTo(oldInitializer.declarationSourceStart, oldInitializer.bodyStart);
				this.setModifiers(initializer, oldInitializer.annotations, oldInitializer.bodyStart);
		}
	}
	/**
	 * @param methodDecl
	 * @param methodDeclaration
	 */
	protected void setModifiers(MethodDeclaration methodDecl, AbstractMethodDeclaration methodDeclaration) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				methodDecl.internalSetModifiers(methodDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag);
				if (methodDeclaration.annotations != null) {
					methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
				}
				break;
			default :
				this.scanner.resetTo(methodDeclaration.declarationSourceStart, methodDeclaration.sourceStart);
				this.setModifiers(methodDecl, methodDeclaration.annotations, methodDeclaration.sourceStart);
		}
	}

	/**
	 * @param variableDecl
	 * @param argument
	 */
	protected void setModifiers(SingleVariableDeclaration variableDecl, Argument argument) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				variableDecl.internalSetModifiers(argument.modifiers & ExtraCompilerModifiers.AccJustFlag);
				if (argument.annotations != null) {
					variableDecl.setFlags(variableDecl.getFlags() | ASTNode.MALFORMED);
				}
				break;
			default :
				this.scanner.resetTo(argument.declarationSourceStart, argument.sourceStart);
				org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = argument.annotations;
				int indexInAnnotations = 0;
				try {
					int token;
					while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
						IExtendedModifier modifier = null;
						switch(token) {
							case TerminalTokens.TokenNameabstract:
								modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
								break;
							case TerminalTokens.TokenNamepublic:
								modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
								break;
							case TerminalTokens.TokenNamestatic:
								modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
								break;
							case TerminalTokens.TokenNameprotected:
								modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
								break;
							case TerminalTokens.TokenNameprivate:
								modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
								break;
							case TerminalTokens.TokenNamefinal:
								modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
								break;
							case TerminalTokens.TokenNamenative:
								modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
								break;
							case TerminalTokens.TokenNamesynchronized:
								modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
								break;
							case TerminalTokens.TokenNametransient:
								modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
								break;
							case TerminalTokens.TokenNamevolatile:
								modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
								break;
							case TerminalTokens.TokenNamestrictfp:
								modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
								break;
							case TerminalTokens.TokenNameAT :
								// we have an annotation
								if (annotations != null && indexInAnnotations < annotations.length) {
									org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
									modifier = convert(annotation);
									this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
								}
								break;
							case TerminalTokens.TokenNameCOMMENT_BLOCK :
							case TerminalTokens.TokenNameCOMMENT_LINE :
							case TerminalTokens.TokenNameCOMMENT_JAVADOC :
								break;
							default :
								return;
						}
						if (modifier != null) {
							variableDecl.modifiers().add(modifier);
						}
					}
				} catch(InvalidInputException e) {
					// ignore
				}
		}
	}

	protected void setModifiers(SingleVariableDeclaration variableDecl, LocalDeclaration localDeclaration) {
		switch(this.ast.apiLevel) {
		case AST.JLS2_INTERNAL :
			variableDecl.internalSetModifiers(localDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag);
			if (localDeclaration.annotations != null) {
				variableDecl.setFlags(variableDecl.getFlags() | ASTNode.MALFORMED);
			}
			break;
		default :
			this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
			org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
			int indexInAnnotations = 0;
			try {
				int token;
				while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
					IExtendedModifier modifier = null;
					switch(token) {
						case TerminalTokens.TokenNameabstract:
							modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
							break;
						case TerminalTokens.TokenNamepublic:
							modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
							break;
						case TerminalTokens.TokenNamestatic:
							modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
							break;
						case TerminalTokens.TokenNameprotected:
							modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
							break;
						case TerminalTokens.TokenNameprivate:
							modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
							break;
						case TerminalTokens.TokenNamefinal:
							modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
							break;
						case TerminalTokens.TokenNamenative:
							modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
							break;
						case TerminalTokens.TokenNamesynchronized:
							modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
							break;
						case TerminalTokens.TokenNametransient:
							modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
							break;
						case TerminalTokens.TokenNamevolatile:
							modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
							break;
						case TerminalTokens.TokenNamestrictfp:
							modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
							break;
						case TerminalTokens.TokenNameAT :
							// we have an annotation
							if (annotations != null && indexInAnnotations < annotations.length) {
								org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
								modifier = convert(annotation);
								this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
							}
							break;
						case TerminalTokens.TokenNameCOMMENT_BLOCK :
						case TerminalTokens.TokenNameCOMMENT_LINE :
						case TerminalTokens.TokenNameCOMMENT_JAVADOC :
							break;
						default :
							return;
					}
					if (modifier != null) {
						variableDecl.modifiers().add(modifier);
					}
				}
			} catch(InvalidInputException e) {
				// ignore
			}
		}
	}

	/**
	 * @param typeDecl
	 * @param typeDeclaration
	 */
	protected void setModifiers(TypeDeclaration typeDecl, org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				int modifiers = typeDeclaration.modifiers;
				modifiers &= ~ClassFileConstants.AccInterface; // remove AccInterface flags
				modifiers &= ExtraCompilerModifiers.AccJustFlag;
				typeDecl.internalSetModifiers(modifiers);
				if (typeDeclaration.annotations != null) {
					typeDecl.setFlags(typeDecl.getFlags() | ASTNode.MALFORMED);
				}
				break;
			default :
				this.scanner.resetTo(typeDeclaration.declarationSourceStart, typeDeclaration.sourceStart);
				this.setModifiers(typeDecl, typeDeclaration.annotations, typeDeclaration.sourceStart);
		}
	}

	/**
	 * @param variableDeclarationExpression
	 * @param localDeclaration
	 */
	protected void setModifiers(VariableDeclarationExpression variableDeclarationExpression, LocalDeclaration localDeclaration) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				int modifiers = localDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag;
				modifiers &= ~ExtraCompilerModifiers.AccBlankFinal;
				variableDeclarationExpression.internalSetModifiers(modifiers);
				if (localDeclaration.annotations != null) {
					variableDeclarationExpression.setFlags(variableDeclarationExpression.getFlags() | ASTNode.MALFORMED);
				}
				break;
			default :
				this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
				org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
				int indexInAnnotations = 0;
				try {
					int token;
					while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
						IExtendedModifier modifier = null;
						switch(token) {
							case TerminalTokens.TokenNameabstract:
								modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
								break;
							case TerminalTokens.TokenNamepublic:
								modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
								break;
							case TerminalTokens.TokenNamestatic:
								modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
								break;
							case TerminalTokens.TokenNameprotected:
								modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
								break;
							case TerminalTokens.TokenNameprivate:
								modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
								break;
							case TerminalTokens.TokenNamefinal:
								modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
								break;
							case TerminalTokens.TokenNamenative:
								modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
								break;
							case TerminalTokens.TokenNamesynchronized:
								modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
								break;
							case TerminalTokens.TokenNametransient:
								modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
								break;
							case TerminalTokens.TokenNamevolatile:
								modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
								break;
							case TerminalTokens.TokenNamestrictfp:
								modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
								break;
							case TerminalTokens.TokenNameAT :
								// we have an annotation
								if (annotations != null && indexInAnnotations < annotations.length) {
									org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
									modifier = convert(annotation);
									this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
								}
								break;
							case TerminalTokens.TokenNameCOMMENT_BLOCK :
							case TerminalTokens.TokenNameCOMMENT_LINE :
							case TerminalTokens.TokenNameCOMMENT_JAVADOC :
								break;
							default :
								return;
						}
						if (modifier != null) {
							variableDeclarationExpression.modifiers().add(modifier);
						}
					}
				} catch(InvalidInputException e) {
					// ignore
				}
		}
	}

	/**
	 * @param variableDeclarationStatement
	 * @param localDeclaration
	 */
	protected void setModifiers(VariableDeclarationStatement variableDeclarationStatement, LocalDeclaration localDeclaration) {
		switch(this.ast.apiLevel) {
			case AST.JLS2_INTERNAL :
				int modifiers = localDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag;
				modifiers &= ~ExtraCompilerModifiers.AccBlankFinal;
				variableDeclarationStatement.internalSetModifiers(modifiers);
				if (localDeclaration.annotations != null) {
					variableDeclarationStatement.setFlags(variableDeclarationStatement.getFlags() | ASTNode.MALFORMED);
				}
				break;
			default :
				this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
				org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
				int indexInAnnotations = 0;
				try {
					int token;
					while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
						IExtendedModifier modifier = null;
						switch(token) {
							case TerminalTokens.TokenNameabstract:
								modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
								break;
							case TerminalTokens.TokenNamepublic:
								modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
								break;
							case TerminalTokens.TokenNamestatic:
								modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
								break;
							case TerminalTokens.TokenNameprotected:
								modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
								break;
							case TerminalTokens.TokenNameprivate:
								modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
								break;
							case TerminalTokens.TokenNamefinal:
								modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
								break;
							case TerminalTokens.TokenNamenative:
								modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
								break;
							case TerminalTokens.TokenNamesynchronized:
								modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
								break;
							case TerminalTokens.TokenNametransient:
								modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
								break;
							case TerminalTokens.TokenNamevolatile:
								modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
								break;
							case TerminalTokens.TokenNamestrictfp:
								modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
								break;
							case TerminalTokens.TokenNameAT :
								// we have an annotation
								if (annotations != null && indexInAnnotations < annotations.length) {
									org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
									modifier = convert(annotation);
									this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
								}
								break;
							case TerminalTokens.TokenNameCOMMENT_BLOCK :
							case TerminalTokens.TokenNameCOMMENT_LINE :
							case TerminalTokens.TokenNameCOMMENT_JAVADOC :
								break;
							default :
								return;
						}
						if (modifier != null) {
							variableDeclarationStatement.modifiers().add(modifier);
						}
					}
				} catch(InvalidInputException e) {
					// ignore
				}
		}
	}

	protected QualifiedName setQualifiedNameNameAndSourceRanges(char[][] typeName, long[] positions, org.eclipse.jdt.internal.compiler.ast.ASTNode node) {
		int length = typeName.length;
		final SimpleName firstToken = new SimpleName(this.ast);
		firstToken.internalSetIdentifier(new String(typeName[0]));
		firstToken.index = 1;
		int start0 = (int)(positions[0]>>>32);
		int start = start0;
		int end = (int)(positions[0] & 0xFFFFFFFF);
		firstToken.setSourceRange(start, end - start + 1);
		final SimpleName secondToken = new SimpleName(this.ast);
		secondToken.internalSetIdentifier(new String(typeName[1]));
		secondToken.index = 2;
		start = (int)(positions[1]>>>32);
		end = (int)(positions[1] & 0xFFFFFFFF);
		secondToken.setSourceRange(start, end - start + 1);
		QualifiedName qualifiedName = new QualifiedName(this.ast);
		qualifiedName.setQualifier(firstToken);
		qualifiedName.setName(secondToken);
		if (this.resolveBindings) {
			recordNodes(qualifiedName, node);
			recordPendingNameScopeResolution(qualifiedName);
			recordNodes(firstToken, node);
			recordNodes(secondToken, node);
			recordPendingNameScopeResolution(firstToken);
			recordPendingNameScopeResolution(secondToken);
		}
		qualifiedName.index = 2;
		qualifiedName.setSourceRange(start0, end - start0 + 1);
		SimpleName newPart = null;
		for (int i = 2; i < length; i++) {
			newPart = new SimpleName(this.ast);
			newPart.internalSetIdentifier(new String(typeName[i]));
			newPart.index = i + 1;
			start = (int)(positions[i]>>>32);
			end = (int)(positions[i] & 0xFFFFFFFF);
			newPart.setSourceRange(start,  end - start + 1);
			QualifiedName qualifiedName2 = new QualifiedName(this.ast);
			qualifiedName2.setQualifier(qualifiedName);
			qualifiedName2.setName(newPart);
			qualifiedName = qualifiedName2;
			qualifiedName.index = newPart.index;
			qualifiedName.setSourceRange(start0, end - start0 + 1);
			if (this.resolveBindings) {
				recordNodes(qualifiedName, node);
				recordNodes(newPart, node);
				recordPendingNameScopeResolution(qualifiedName);
				recordPendingNameScopeResolution(newPart);
			}
		}
		QualifiedName name = qualifiedName;
		if (this.resolveBindings) {
			recordNodes(name, node);
			recordPendingNameScopeResolution(name);
		}
		return name;
	}

	protected QualifiedName setQualifiedNameNameAndSourceRanges(char[][] typeName, long[] positions, int endingIndex, org.eclipse.jdt.internal.compiler.ast.ASTNode node) {
 		int length = endingIndex + 1;
		final SimpleName firstToken = new SimpleName(this.ast);
		firstToken.internalSetIdentifier(new String(typeName[0]));
		firstToken.index = 1;
		int start0 = (int)(positions[0]>>>32);
		int start = start0;
		int end = (int) positions[0];
		firstToken.setSourceRange(start, end - start + 1);
		final SimpleName secondToken = new SimpleName(this.ast);
		secondToken.internalSetIdentifier(new String(typeName[1]));
		secondToken.index = 2;
		start = (int)(positions[1]>>>32);
		end = (int) positions[1];
		secondToken.setSourceRange(start, end - start + 1);
		QualifiedName qualifiedName = new QualifiedName(this.ast);
		qualifiedName.setQualifier(firstToken);
		qualifiedName.setName(secondToken);
		if (this.resolveBindings) {
			recordNodes(qualifiedName, node);
			recordPendingNameScopeResolution(qualifiedName);
			recordNodes(firstToken, node);
			recordNodes(secondToken, node);
			recordPendingNameScopeResolution(firstToken);
			recordPendingNameScopeResolution(secondToken);
		}
		qualifiedName.index = 2;
		qualifiedName.setSourceRange(start0, end - start0 + 1);
		SimpleName newPart = null;
		for (int i = 2; i < length; i++) {
			newPart = new SimpleName(this.ast);
			newPart.internalSetIdentifier(new String(typeName[i]));
			newPart.index = i + 1;
			start = (int)(positions[i]>>>32);
			end = (int) positions[i];
			newPart.setSourceRange(start,  end - start + 1);
			QualifiedName qualifiedName2 = new QualifiedName(this.ast);
			qualifiedName2.setQualifier(qualifiedName);
			qualifiedName2.setName(newPart);
			qualifiedName = qualifiedName2;
			qualifiedName.index = newPart.index;
			qualifiedName.setSourceRange(start0, end - start0 + 1);
			if (this.resolveBindings) {
				recordNodes(qualifiedName, node);
				recordNodes(newPart, node);
				recordPendingNameScopeResolution(qualifiedName);
				recordPendingNameScopeResolution(newPart);
			}
		}
        if (newPart == null && this.resolveBindings) {
            recordNodes(qualifiedName, node);
            recordPendingNameScopeResolution(qualifiedName);
        }
		return qualifiedName;
	}

	protected void setTypeNameForAnnotation(org.eclipse.jdt.internal.compiler.ast.Annotation compilerAnnotation, Annotation annotation) {
		TypeReference typeReference = compilerAnnotation.type;
		if (typeReference instanceof QualifiedTypeReference) {
			QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference) typeReference;
			char[][] tokens = qualifiedTypeReference.tokens;
			long[] positions = qualifiedTypeReference.sourcePositions;
			// QualifiedName
			annotation.setTypeName(setQualifiedNameNameAndSourceRanges(tokens, positions, typeReference));
		} else {
			SingleTypeReference singleTypeReference = (SingleTypeReference) typeReference;
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(singleTypeReference.token));
			int start = singleTypeReference.sourceStart;
			int end = singleTypeReference.sourceEnd;
			name.setSourceRange(start, end - start + 1);
			name.index = 1;
			annotation.setTypeName(name);
			if (this.resolveBindings) {
				recordNodes(name, typeReference);
			}
		}
	}

	protected void setTypeForField(FieldDeclaration fieldDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					fieldDeclaration.setType(elementType);
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					fieldDeclaration.setType(subarrayType);
					updateInnerPositions(subarrayType, remainingDimensions);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				fieldDeclaration.setType(type);
			}
		} else {
			if (type.isArrayType()) {
				// update positions of the component types of the array type
				int dimensions = ((ArrayType) type).getDimensions();
				updateInnerPositions(type, dimensions);
			}
			fieldDeclaration.setType(type);
		}
	}

	protected void setTypeForMethodDeclaration(MethodDeclaration methodDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					switch(this.ast.apiLevel) {
						case AST.JLS2_INTERNAL :
							methodDeclaration.internalSetReturnType(elementType);
							break;
						default :
							methodDeclaration.setReturnType2(elementType);
						break;
					}
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					updateInnerPositions(subarrayType, remainingDimensions);
					switch(this.ast.apiLevel) {
						case AST.JLS2_INTERNAL :
							methodDeclaration.internalSetReturnType(subarrayType);
							break;
						default :
							methodDeclaration.setReturnType2(subarrayType);
						break;
					}
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						methodDeclaration.internalSetReturnType(type);
						break;
					default :
						methodDeclaration.setReturnType2(type);
					break;
				}
			}
		} else {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					methodDeclaration.internalSetReturnType(type);
					break;
				default :
					methodDeclaration.setReturnType2(type);
				break;
			}
		}
	}

	protected void setTypeForMethodDeclaration(AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration, Type type, int extraDimension) {
		annotationTypeMemberDeclaration.setType(type);
	}

	protected void setTypeForSingleVariableDeclaration(SingleVariableDeclaration singleVariableDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					singleVariableDeclaration.setType(elementType);
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					updateInnerPositions(subarrayType, remainingDimensions);
					singleVariableDeclaration.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				singleVariableDeclaration.setType(type);
			}
		} else {
			singleVariableDeclaration.setType(type);
		}
	}

	protected void setTypeForVariableDeclarationExpression(VariableDeclarationExpression variableDeclarationExpression, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					variableDeclarationExpression.setType(elementType);
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					updateInnerPositions(subarrayType, remainingDimensions);
					variableDeclarationExpression.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				variableDeclarationExpression.setType(type);
			}
		} else {
			variableDeclarationExpression.setType(type);
		}
	}

	protected void setTypeForVariableDeclarationStatement(VariableDeclarationStatement variableDeclarationStatement, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					variableDeclarationStatement.setType(elementType);
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					updateInnerPositions(subarrayType, remainingDimensions);
					variableDeclarationStatement.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				variableDeclarationStatement.setType(type);
			}
		} else {
			variableDeclarationStatement.setType(type);
		}
	}

	protected void updateInnerPositions(Type type, int dimensions) {
		if (dimensions > 1) {
			// need to set positions for intermediate array type see 42839
			int start = type.getStartPosition();
			Type currentComponentType = ((ArrayType) type).getComponentType();
			int searchedDimension = dimensions - 1;
			int rightBracketEndPosition = start;
			while (currentComponentType.isArrayType()) {
				rightBracketEndPosition = retrieveProperRightBracketPosition(searchedDimension, start);
				currentComponentType.setSourceRange(start, rightBracketEndPosition - start + 1);
				currentComponentType = ((ArrayType) currentComponentType).getComponentType();
				searchedDimension--;
			}
		}
	}
}
