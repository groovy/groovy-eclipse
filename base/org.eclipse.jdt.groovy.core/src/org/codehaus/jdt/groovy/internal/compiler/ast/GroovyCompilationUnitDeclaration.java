/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement     - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import groovyjarjarasm.asm.Opcodes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Comment;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.TaskEntry;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.LocatedMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.CSTNode;
import org.codehaus.groovy.syntax.PreciseSyntaxException;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.tools.GroovyClass;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A subtype of JDT CompilationUnitDeclaration that represents a groovy source file. It overrides methods as appropriate, delegating
 * to the groovy infrastructure.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class GroovyCompilationUnitDeclaration extends CompilationUnitDeclaration {

	// The groovy compilation unit shared by all files in the same project
	private CompilationUnit groovyCompilationUnit;
	// The groovy sourceunit (a member of the groovyCompilationUnit)
	private SourceUnit groovySourceUnit;
	private CompilerOptions compilerOptions;
	private boolean checkGenerics;
	public static boolean defaultCheckGenerics = false;
	public static boolean earlyTransforms = true;

	private boolean isScript = false;

	private boolean hasAnonInners;

	/**
	 * Map to keep track of anonymous inner type outer methods. Only used is hasAnonInners is true
	 */
	private Map<MethodNode, AbstractMethodDeclaration> enclosingMethodMap = null;

	private static final boolean DEBUG_TASK_TAGS = false;

	public GroovyCompilationUnitDeclaration(ProblemReporter problemReporter, CompilationResult compilationResult, int sourceLength,
			CompilationUnit groovyCompilationUnit, SourceUnit groovySourceUnit, CompilerOptions compilerOptions) {
		super(problemReporter, compilationResult, sourceLength);
		this.groovyCompilationUnit = groovyCompilationUnit;
		this.groovySourceUnit = groovySourceUnit;
		this.compilerOptions = compilerOptions;
		this.checkGenerics = defaultCheckGenerics;
	}

	/**
	 * Take the comments information from the parse and apply it to the compilation unit
	 */
	private void setComments() {
		List<Comment> groovyComments = this.groovySourceUnit.getComments();
		if (groovyComments == null || groovyComments.size() == 0) {
			return;
		}
		this.comments = new int[groovyComments.size()][2];
		for (int c = 0, max = groovyComments.size(); c < max; c++) {
			Comment groovyComment = groovyComments.get(c);
			this.comments[c] = groovyComment.getPositions(compilationResult.lineSeparatorPositions);
			// System.out.println("Comment recorded on " + groovySourceUnit.getName() + "  " + this.comments[c][0] + ">"
			// + this.comments[c][1]);
		}
	}

	/**
	 * Drives the Groovy Compilation Unit for this project through to the specified phase. Yes on a call for one groovy file to
	 * processToPhase(), all the groovy files in the project proceed to that phase. This isn't ideal but doesn't necessarily cause a
	 * problem. But it does mean progress reporting for the compilation is incorrect as it jumps rather than smoothly going from 1
	 * to 100%.
	 * 
	 * @param phase the phase to process up to
	 * @return true if clean processing, false otherwise
	 */
	public boolean processToPhase(int phase) {
		boolean alreadyHasProblems = compilationResult.hasProblems();
		// Our replacement error collector doesn't cause an exception, instead they are checked for post 'compile'
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(groovyCompilationUnit.getTransformLoader());
				groovyCompilationUnit.compile(phase);
			} finally {
				Thread.currentThread().setContextClassLoader(cl);
			}
			if (groovySourceUnit.getErrorCollector().hasErrors()) {
				recordProblems(groovySourceUnit.getErrorCollector().getErrors());
				return false;
			} else {
				return true;
			}
		} catch (MultipleCompilationErrorsException problems) {
			AbortCompilation abort = getAbortCompilation(problems);
			if (abort != null) {
				System.out.println("Abort compilation");
				throw abort;
			} else {
				System.err.println("exception handling");
				// alternative to catching this is fleshing out the ErrorCollector
				// sub type we have and asking it if there
				// are errors at the end of a run...
				problems.printStackTrace();
				recordProblems(problems.getErrorCollector().getErrors());
			}
		} catch (GroovyBugError gbr) {
			// FIXASC (M3) really, the GBE should not be thrown in the first place
			// we shouldn't need to silently fail here.
			if (alreadyHasProblems) {
				// do not log the error because it is likely to have
				// occurred because of the existing problem
				System.err.println("Ignoring GroovyBugError since it is likely caused by earlier issues.  Ignored problem is '"
						+ gbr.getMessage() + "'");
			} else {
				boolean reportit = true;
				if (gbr.getCause() instanceof AbortCompilation) {
					// might be nothing to log - AbortCompilations can occur 'normally' during processing
					// when jobs are stopped due to any results they produce being stale.
					AbortCompilation abort = (AbortCompilation) gbr.getCause();
					if (abort.isSilent) {
						reportit = false;
					}
				}
				if (reportit) {
					System.err.println("Internal Groovy Error --- " + gbr.getBugText());
					gbr.printStackTrace();
					// The groovy compiler threw an exception
					// FIXASC (M3) Should record these errors as a problem on the project
					// should *not* throw these because of bad syntax in the file
					Util.log(gbr, "Internal groovy compiler error.");

					// Also need to record these problems as compiler errors since some users will not think to check the log
					// This is mostly a fix for problems like those in GRECLIPSE-1420, where a GBError is thrown when it is really
					// just a syntax problem.
					groovySourceUnit.getErrorCollector().addError(
							new SyntaxErrorMessage(new SyntaxException("Internal groovy compiler error.\n" + gbr.getBugText(), gbr,
									1, 0), groovySourceUnit));
					recordProblems(groovySourceUnit.getErrorCollector().getErrors());
				}
			}
		}
		return false;
	}

	private org.eclipse.jdt.internal.compiler.problem.AbortCompilation getAbortCompilation(
			MultipleCompilationErrorsException problems) {
		ErrorCollector collector = problems.getErrorCollector();
		if (collector.getErrorCount() == 1 && problems.getErrorCollector().getError(0) instanceof ExceptionMessage) {
			Exception abort = ((ExceptionMessage) problems.getErrorCollector().getError(0)).getCause();
			return abort instanceof AbortCompilation ? (AbortCompilation) abort : null;
		}
		return null;
	}

	/**
	 * @return the *groovy* compilation unit shared by all files in the same project
	 */
	public CompilationUnit getCompilationUnit() {
		return groovyCompilationUnit;
	}

	/**
	 * Populate the compilation unit based on the successful parse.
	 */
	public void populateCompilationUnitDeclaration() {
		ModuleNode moduleNode = groovySourceUnit.getAST();
		// if (moduleNode.encounteredUnrecoverableError()) {
		// String msg = "Groovy: Unrecoverable error during processing - source file contains invalid syntax";
		// int sev = ProblemSeverities.Error;
		// CategorizedProblem p = new DefaultProblemFactory().createProblem(getFileName(), 0, new String[] { msg }, 0,
		// new String[] { msg }, sev, 0, 1, 1, 0);
		// this.problemReporter.record(p, compilationResult, this);
		// }
		createPackageDeclaration(moduleNode);
		createImports(moduleNode);
		createTypeDeclarations(moduleNode);
	}

	// make protected for testing
	protected void createImports(ModuleNode moduleNode) {
		List<ImportNode> importNodes = moduleNode.getImports();
		List<ImportNode> importPackages = ImportNodeCompatibilityWrapper.getStarImports(moduleNode);
		Map<String, ImportNode> importStatics = ImportNodeCompatibilityWrapper.getStaticImports(moduleNode);
		Map<String, ImportNode> importStaticStars = ImportNodeCompatibilityWrapper.getStaticStarImports(moduleNode);
		if (importNodes.size() > 0 || importPackages.size() > 0 || importStatics.size() > 0 || importStaticStars.size() > 0) {
			List<ImportReference> importReferences = new ArrayList<ImportReference>();
			for (ImportNode importNode : importNodes) {
				char[][] splits = CharOperation.splitOn('.', importNode.getClassName().toCharArray());
				ImportReference ref = null;
				ClassNode type = importNode.getType();
				int typeStartOffset = startOffset(type);
				int typeEndOffset = endOffset(type);
				if (typeStartOffset == 0) {
					// not a real import, a fake one created during recovery
					continue;
				}
				if (importNode.getAlias() != null && importNode.getAlias().length() > 0) {
					// FIXASC will need extra positional info for the 'as' and the alias
					ref = new AliasImportReference(importNode.getAlias().toCharArray(), splits, positionsFor(splits,
							typeStartOffset, typeEndOffset), false, ClassFileConstants.AccDefault);
				} else {
					ref = new ImportReference(splits, positionsFor(splits, typeStartOffset, typeEndOffset), false,
							ClassFileConstants.AccDefault);
				}
				ref.sourceEnd = Math.max(typeEndOffset - 1, ref.sourceStart); // For error reporting, Eclipse wants -1
				int start = importNode.getStart();
				ref.declarationSourceStart = start;
				int end = importNode.getEnd();
				ref.declarationSourceEnd = end;

				ref.declarationEnd = ref.sourceEnd;
				importReferences.add(ref);
			}

			for (ImportNode importPackage : importPackages) {
				String importText = importPackage.getText();

				// when calculating these offsets, assume no extraneous whitespace
				int packageStartOffset = importPackage.getStart() + "import ".length();
				int packageEndOffset = packageStartOffset + importText.length() - "import ".length() - ".*".length();

				char[][] splits = CharOperation.splitOn('.',
						importPackage.getPackageName().substring(0, importPackage.getPackageName().length() - 1).toCharArray());
				ImportReference ref = new ImportReference(splits, positionsFor(splits, packageStartOffset, packageEndOffset), true,
						ClassFileConstants.AccDefault);
				// import * style only have slocs for the entire ImportNode and not for the embedded type
				ref.sourceEnd = packageEndOffset;
				ref.declarationSourceStart = importPackage.getStart();
				ref.declarationSourceEnd = importPackage.getEnd();
				ref.declarationEnd = ref.sourceEnd;
				importReferences.add(ref);
			}
			for (Map.Entry<String, ImportNode> importStatic : importStatics.entrySet()) {
				ImportNode importNode = importStatic.getValue();
				String importName = importNode.getClassName() + "." + importStatic.getKey();
				char[][] splits = CharOperation.splitOn('.', importName.toCharArray());

				ImportReference ref = null;
				ClassNode type = importNode.getType();
				int typeStartOffset = startOffset(type);
				int typeEndOffset = endOffset(type);
				if (importNode.getAlias() != null && importNode.getAlias().length() > 0) {
					// FIXASC will need extra positional info for the 'as' and the alias
					ref = new AliasImportReference(importNode.getAlias().toCharArray(), splits, positionsFor(splits,
							typeStartOffset, typeEndOffset), false, ClassFileConstants.AccDefault | ClassFileConstants.AccStatic);
				} else {
					ref = new ImportReference(splits, positionsFor(splits, typeStartOffset, typeEndOffset), false,
							ClassFileConstants.AccDefault | ClassFileConstants.AccStatic);
				}
				ref.sourceEnd = Math.max(typeEndOffset - 1, ref.sourceStart); // For error reporting, Eclipse wants -1
				ref.declarationSourceStart = importNode.getStart();
				ref.declarationSourceEnd = importNode.getEnd();
				ref.declarationEnd = ref.sourceEnd;
				importReferences.add(ref);
			}
			for (Map.Entry<String, ImportNode> importStaticStar : importStaticStars.entrySet()) {
				String classname = importStaticStar.getKey();
				ImportNode importNode = importStaticStar.getValue();
				ClassNode importedType = importNode.getType();
				int typeStartOffset = importedType != null ? startOffset(importedType) : 0;
				int typeEndOffset = importedType != null ? endOffset(importedType) : 0;
				char[][] splits = CharOperation.splitOn('.', classname.toCharArray());
				ImportReference ref = new ImportReference(splits, positionsFor(splits, typeStartOffset, typeEndOffset), true,
						ClassFileConstants.AccDefault | ClassFileConstants.AccStatic);
				ref.sourceEnd = Math.max(typeEndOffset - 1, ref.sourceStart); // For error reporting, Eclipse wants -1
				ref.declarationSourceStart = importNode.getStart();
				ref.declarationSourceEnd = importNode.getEnd();
				ref.declarationEnd = ref.sourceEnd;
				importReferences.add(ref);
			}

			// ensure proper lexical order
			if (importReferences.size() != 0) {
				imports = importReferences.toArray(new ImportReference[importReferences.size()]);
				Arrays.sort(imports, new Comparator<ImportReference>() {
					public int compare(ImportReference left, ImportReference right) {
						return left.sourceStart - right.sourceStart;
					}
				});
				for (ImportReference ref : imports) {
					if (ref.declarationSourceStart > 0 && (ref.declarationEnd - ref.declarationSourceStart + 1) < 0) {
						throw new IllegalStateException("Import reference alongside class " + moduleNode.getClasses().get(0)
								+ " will trigger later failure: " + ref.toString() + " declSourceStart="
								+ ref.declarationSourceStart + " declEnd=" + +ref.declarationEnd);
					}

				}
			}
		}
	}

	/**
	 * Build a JDT package declaration based on the groovy one
	 */
	private void createPackageDeclaration(ModuleNode moduleNode) {
		if (moduleNode.hasPackageName()) {
			PackageNode packageNode = moduleNode.getPackage();// Node();
			String packageName = moduleNode.getPackageName();
			if (packageName.endsWith(".")) {
				packageName = packageName.substring(0, packageName.length() - 1);
			}
			long start = startOffset(packageNode);
			long end = endOffset(packageNode);
			char[][] packageReference = CharOperation.splitOn('.', packageName.toCharArray());
			currentPackage = new ImportReference(packageReference, positionsFor(packageReference, start, end), true,
					ClassFileConstants.AccDefault);
			currentPackage.declarationSourceStart = currentPackage.sourceStart;
			currentPackage.declarationSourceEnd = currentPackage.sourceEnd;
			currentPackage.declarationEnd = currentPackage.sourceEnd;

			// FIXASC (M3) not right, there may be spaces between package keyword and decl. Just the first example of position
			// problems
			currentPackage.declarationSourceStart = currentPackage.sourceStart - "package ".length();
			currentPackage.declarationEnd = currentPackage.declarationSourceEnd = currentPackage.sourceEnd;
		}
	}

	/**
	 * Convert groovy annotations into JDT annotations
	 * 
	 * @return an array of annotations or null if there are none
	 */
	private Annotation[] transformAnnotations(List<AnnotationNode> groovyAnnotations) {
		// FIXASC positions are crap
		if (groovyAnnotations != null && groovyAnnotations.size() > 0) {
			List<Annotation> annotations = new ArrayList<Annotation>();
			for (AnnotationNode annotationNode : groovyAnnotations) {
				ClassNode annoType = annotationNode.getClassNode();
				Map<String, Expression> memberValuePairs = annotationNode.getMembers();
				// FIXASC (M3) do more than pure marker annotations and do annotation values

				if (memberValuePairs == null || memberValuePairs.size() == 0) {
					// Marker annotation:
					TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
					annotationReference.sourceStart = annotationNode.getStart();
					annotationReference.sourceEnd = annotationNode.getEnd();

					MarkerAnnotation annotation = new MarkerAnnotation(annotationReference, annotationNode.getStart());
					annotation.declarationSourceEnd = annotation.sourceEnd;
					annotations.add(annotation);
				} else {

					if (memberValuePairs.size() == 1 && memberValuePairs.containsKey("value")) {
						// Single member annotation

						// Code written to only manage a single class literal value annotation - so that @RunWith works
						Expression value = memberValuePairs.get("value");
						if (value instanceof PropertyExpression) {
							String pExpression = ((PropertyExpression) value).getPropertyAsString();
							if (pExpression.equals("class")) {
								TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
								annotationReference.sourceStart = annotationNode.getStart();
								annotationReference.sourceEnd = annotationNode.getEnd();
								SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference,
										annotationNode.getStart());
								annotation.memberValue = new ClassLiteralAccess(value.getEnd(),
										classLiteralToTypeReference((PropertyExpression) value));
								annotation.declarationSourceEnd = annotation.sourceStart
										+ annoType.getNameWithoutPackage().length();
								annotations.add(annotation);
							}
						} else if (value instanceof VariableExpression && annoType.getName().endsWith("RunWith")) {
							// FIXASC special case for 'RunWith(Foo)' where for some reason groovy doesn't mind the missing
							// '.class'
							// FIXASC test this
							TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
							annotationReference.sourceStart = annotationNode.getStart();
							annotationReference.sourceEnd = annotationNode.getEnd();
							SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference,
									annotationNode.getStart());
							String v = ((VariableExpression) value).getName();
							TypeReference ref = null;
							int start = annotationReference.sourceStart;
							int end = annotationReference.sourceEnd;
							if (v.indexOf(".") == -1) {
								ref = new SingleTypeReference(v.toCharArray(), toPos(start, end - 1));
							} else {
								char[][] splits = CharOperation.splitOn('.', v.toCharArray());
								ref = new QualifiedTypeReference(splits, positionsFor(splits, start, end - 2));
							}
							annotation.memberValue = new ClassLiteralAccess(value.getEnd(), ref);
							annotation.declarationSourceEnd = annotation.sourceStart + annoType.getNameWithoutPackage().length();
							annotations.add(annotation);
							// FIXASC underlining for SuppressWarnings doesn't seem right when included in messages
						} else if (annoType.getName().equals("SuppressWarnings")
								&& (value instanceof ConstantExpression || value instanceof ListExpression)) {
							if (value instanceof ListExpression) {
								ListExpression listExpression = (ListExpression) value;
								// FIXASC tidy up all this junk (err, i mean 'refactor') once we have confidence in test
								// coverage
								List<Expression> listOfExpressions = listExpression.getExpressions();
								TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
								annotationReference.sourceStart = annotationNode.getStart();
								annotationReference.sourceEnd = annotationNode.getEnd() - 1;
								SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference,
										annotationNode.getStart());

								ArrayInitializer arrayInitializer = new ArrayInitializer();
								arrayInitializer.expressions = new org.eclipse.jdt.internal.compiler.ast.Expression[listOfExpressions
										.size()];
								for (int c = 0; c < listOfExpressions.size(); c++) {
									ConstantExpression cExpression = (ConstantExpression) listOfExpressions.get(c);
									String v = (String) cExpression.getValue();
									TypeReference ref = null;
									int start = cExpression.getStart();
									int end = cExpression.getEnd() - 1;
									if (v.indexOf(".") == -1) {
										ref = new SingleTypeReference(v.toCharArray(), toPos(start, end - 1));
										annotation.declarationSourceEnd = annotation.sourceStart
												+ annoType.getNameWithoutPackage().length() - 1;
									} else {
										char[][] splits = CharOperation.splitOn('.', v.toCharArray());
										ref = new QualifiedTypeReference(splits, positionsFor(splits, start, end - 2));
										annotation.declarationSourceEnd = annotation.sourceStart + annoType.getName().length() - 1;
									}
									arrayInitializer.expressions[c] = new StringLiteral(v.toCharArray(), start, end, -1);
								}
								annotation.memberValue = arrayInitializer;
								annotations.add(annotation);
							} else {
								ConstantExpression constantExpression = (ConstantExpression) value;
								if (value.getType().getName().equals("java.lang.String")) {
									// single value, eg. @SuppressWarnings("unchecked")
									// FIXASC tidy up all this junk (err, i mean 'refactor') once we have confidence in test
									// coverage
									// FIXASC test positional info for conjured up anno refs
									TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
									annotationReference.sourceStart = annotationNode.getStart();
									annotationReference.sourceEnd = annotationNode.getEnd() - 1;
									SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference,
											annotationNode.getStart());
									String v = (String) constantExpression.getValue();
									TypeReference ref = null;
									int start = constantExpression.getStart();
									int end = constantExpression.getEnd() - 1;
									if (v.indexOf(".") == -1) {
										ref = new SingleTypeReference(v.toCharArray(), toPos(start, end - 1));
										annotation.declarationSourceEnd = annotation.sourceStart
												+ annoType.getNameWithoutPackage().length() - 1;
									} else {
										char[][] splits = CharOperation.splitOn('.', v.toCharArray());
										ref = new QualifiedTypeReference(splits, positionsFor(splits, start, end - 2));
										annotation.declarationSourceEnd = annotation.sourceStart + annoType.getName().length() - 1;
									}
									annotation.memberValue = new StringLiteral(v.toCharArray(), start, end, -1);
									annotations.add(annotation);
								}
							}
						}
					} else if (annoType.getNameWithoutPackage().equals("Test")) {
						// normal annotation (with at least one member value pair)
						// GRECLIPSE-569
						// treat as a marker annotation
						// this is specifically written so that annotations like @Test(expected = FooException) can be found
						TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
						annotationReference.sourceStart = annotationNode.getStart();
						annotationReference.sourceEnd = annotationNode.getEnd();

						MarkerAnnotation annotation = new MarkerAnnotation(annotationReference, annotationNode.getStart());
						annotation.declarationSourceEnd = annotation.sourceEnd;
						annotations.add(annotation);
					}
				}
			}
			if (annotations.size() > 0) {
				return annotations.toArray(new Annotation[annotations.size()]);
			}
		}
		return null;
	}

	private TypeReference classLiteralToTypeReference(PropertyExpression value) {
		// should be a class literal node
		assert value.getPropertyAsString().equals("class");

		// FIXASC ignore type parameters for now
		Expression candidate = value.getObjectExpression();
		List<char[]> nameParts = new LinkedList<char[]>();
		while (candidate instanceof PropertyExpression) {
			nameParts.add(0, ((PropertyExpression) candidate).getPropertyAsString().toCharArray());
			candidate = ((PropertyExpression) candidate).getObjectExpression();
		}
		if (candidate instanceof VariableExpression) {
			nameParts.add(0, ((VariableExpression) candidate).getName().toCharArray());
		}

		char[][] namePartsArr = nameParts.toArray(new char[nameParts.size()][]);
		long[] poss = positionsFor(namePartsArr, value.getObjectExpression().getStart(), value.getObjectExpression().getEnd());
		TypeReference ref;
		if (namePartsArr.length > 1) {
			ref = new QualifiedTypeReference(namePartsArr, poss);
		} else if (namePartsArr.length == 1) {
			ref = new SingleTypeReference(namePartsArr[0], poss[0]);
		} else {
			// should not happen
			ref = TypeReference.baseTypeReference(nameToPrimitiveTypeId.get("void"), 0);
		}

		return ref;
	}

	/**
	 * Find any javadoc that terminates on one of the two lines before the specified line, return the first bit encountered. A
	 * little crude but will cover a lot of common cases... <br>
	 */
	// FIXASC when the parser correctly records javadoc for nodes alongside them during a parse, we will not have to search
	private Javadoc findJavadoc(int line) {
		// System.out.println("Looking for javadoc for line " + line);
		for (Comment comment : groovySourceUnit.getComments()) {
			if (comment.isJavadoc()) {
				// System.out.println("Checking against comment ending on " + comment.getLastLine());
				if (comment.getLastLine() + 1 == line || (comment.getLastLine() + 2 == line && !comment.usedUp)) {
					int[] pos = comment.getPositions(compilationResult.lineSeparatorPositions);
					// System.out.println("Comment says it is from line=" + comment.sline + ",col=" + comment.scol + " to line="
					// + comment.eline + ",col=" + comment.ecol);
					// System.out.println("Returning positions " + pos[0] + ">" + pos[1]);
					comment.usedUp = true;
					return new Javadoc(pos[0], pos[1]);
				}
			}
		}
		return null;
	}

	/**
	 * Build JDT TypeDeclarations for the groovy type declarations that were parsed from the source file.
	 */
	private void createTypeDeclarations(ModuleNode moduleNode) {
		List<ClassNode> moduleClassNodes = moduleNode.getClasses();
		hasAnonInners = false;
		for (ClassNode classNode : moduleClassNodes) {
			if (isAnon(classNode)) {
				hasAnonInners = true;
				enclosingMethodMap = new HashMap<MethodNode, AbstractMethodDeclaration>();
				break;
			}
		}
		List<TypeDeclaration> typeDeclarations = new ArrayList<TypeDeclaration>();
		Map<ClassNode, TypeDeclaration> fromClassNodeToDecl = new HashMap<ClassNode, TypeDeclaration>();

		char[] mainName = toMainName(compilationResult.getFileName());
		boolean isInner = false;
		List<ClassNode> classNodes = null;
		classNodes = moduleClassNodes;
		Map<ClassNode, List<TypeDeclaration>> innersToRecord = new HashMap<ClassNode, List<TypeDeclaration>>();
		for (ClassNode classNode : classNodes) {
			if (!classNode.isPrimaryClassNode()) {
				continue;
			}

			GroovyTypeDeclaration typeDeclaration = new GroovyTypeDeclaration(compilationResult, classNode);

			typeDeclaration.annotations = transformAnnotations(classNode.getAnnotations());
			if (classNode instanceof InnerClassNode) {
				isInner = true;
			} else {
				typeDeclaration.name = classNode.getNameWithoutPackage().toCharArray();
				if (!CharOperation.equals(typeDeclaration.name, mainName)) {
					typeDeclaration.bits |= ASTNode.IsSecondaryType;
				}
				isInner = false;
			}

			boolean isInterface = classNode.isInterface();
			int mods = classNode.getModifiers();
			if ((mods & Opcodes.ACC_ENUM) != 0) {
				// remove final
				mods = mods & ~Opcodes.ACC_FINAL;
			}
			// FIXASC should this modifier be set?
			// mods |= Opcodes.ACC_PUBLIC;
			// FIXASC should not do this for inner classes, just for top level types
			// FIXASC does this make things visible that shouldn't be?
			mods = mods & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
			if (!isInner) {
				if ((mods & Opcodes.ACC_STATIC) != 0) {
					mods = mods & ~(Opcodes.ACC_STATIC);
				}
			}
			typeDeclaration.modifiers = mods & ~(isInterface ? Opcodes.ACC_ABSTRACT : 0);

			fixupSourceLocationsForTypeDeclaration(typeDeclaration, classNode);

			if (classNode.getGenericsTypes() != null) {
				GenericsType[] genericInfo = classNode.getGenericsTypes();
				// Example case here: Foo<T extends Number & I>
				// the type parameter is T, the 'type' is Number and the bounds for the type parameter are just the extra bound
				// I.
				typeDeclaration.typeParameters = new TypeParameter[genericInfo.length];
				for (int tp = 0; tp < genericInfo.length; tp++) {
					TypeParameter typeParameter = new TypeParameter();
					typeParameter.name = genericInfo[tp].getName().toCharArray();
					ClassNode[] upperBounds = genericInfo[tp].getUpperBounds();
					if (upperBounds != null) {
						// FIXASC (M3) Positional info for these references?
						typeParameter.type = createTypeReferenceForClassNode(upperBounds[0]);
						typeParameter.bounds = (upperBounds.length > 1 ? new TypeReference[upperBounds.length - 1] : null);
						for (int b = 1, max = upperBounds.length; b < max; b++) {
							typeParameter.bounds[b - 1] = createTypeReferenceForClassNode(upperBounds[b]);
							typeParameter.bounds[b - 1].bits |= ASTNode.IsSuperType;
						}
					}
					typeDeclaration.typeParameters[tp] = typeParameter;
				}
			}

			boolean isEnum = (classNode.getModifiers() & Opcodes.ACC_ENUM) != 0;
			configureSuperClass(typeDeclaration, classNode.getSuperClass(), isEnum);
			configureSuperInterfaces(typeDeclaration, classNode);
			typeDeclaration.methods = createMethodAndConstructorDeclarations(classNode, isEnum, compilationResult);
			typeDeclaration.fields = createFieldDeclarations(classNode, isEnum);
			typeDeclaration.properties = classNode.getProperties();
			if (classNode instanceof InnerClassNode) {
				InnerClassNode innerClassNode = (InnerClassNode) classNode;
				ClassNode outerClass = innerClassNode.getOuterClass();
				String outername = outerClass.getNameWithoutPackage();
				String newInner = innerClassNode.getNameWithoutPackage().substring(outername.length() + 1);
				typeDeclaration.name = newInner.toCharArray();

				// Record that we need to set the parent of this inner type later
				List<TypeDeclaration> inners = innersToRecord.get(outerClass);
				if (inners == null) {
					inners = new ArrayList<TypeDeclaration>();
					innersToRecord.put(outerClass, inners);
				}
				inners.add(typeDeclaration);

				if (isAnon(classNode)) {
					// anon inner class, actually
					typeDeclaration.bits |= (ASTNode.IsAnonymousType | ASTNode.IsLocalType);
					// fill in the AST just enough to get JDT working
					typeDeclaration.allocation = new QualifiedAllocationExpression(typeDeclaration);
					typeDeclaration.allocation.type = typeDeclaration.superclass;
					typeDeclaration.allocation.enclosingInstance = new NullLiteral(typeDeclaration.sourceStart,
							typeDeclaration.sourceEnd);
					typeDeclaration.name = CharOperation.NO_CHAR;
				}
			} else {
				typeDeclarations.add(typeDeclaration);
			}
			fromClassNodeToDecl.put(classNode, typeDeclaration);
		}

		// For inner types, now attach them to their parents. This was not done earlier as sometimes the types are processed in
		// such an order that inners are dealt with before outers
		for (Map.Entry<ClassNode, List<TypeDeclaration>> innersToRecordEntry : innersToRecord.entrySet()) {
			ClassNode outer = innersToRecordEntry.getKey();
			TypeDeclaration outerTypeDeclaration = fromClassNodeToDecl.get(outer);
			// Check if there is a problem locating the parent for the inner
			if (outerTypeDeclaration == null) {
				throw new GroovyEclipseBug("Failed to find the type declaration for " + outer.getText());
			}

			List<TypeDeclaration> newInnersList = innersToRecordEntry.getValue();
			for (Iterator<TypeDeclaration> iterator = newInnersList.iterator(); iterator.hasNext();) {
				GroovyTypeDeclaration inner = (GroovyTypeDeclaration) iterator.next();
				if ((inner.bits & ASTNode.IsAnonymousType) > 0) {
					iterator.remove();
					MethodNode enclosingMethodGroovy = inner.getClassNode().getEnclosingMethod();
					if (enclosingMethodGroovy == null) {
						// probably an anon type inside a script
						ClassNode outerClass = inner.getClassNode().getOuterClass();
						enclosingMethodGroovy = outerClass.getMethod("run", new Parameter[0]);
						if (enclosingMethodGroovy == null) {
							throw new GroovyEclipseBug("Failed to find the enclosing method for anonymous type "
									+ inner.getClassNode().getName());
						}
					}
					AbstractMethodDeclaration enclosingMethodJDT = enclosingMethodMap.get(enclosingMethodGroovy);
					enclosingMethodJDT.bits |= ASTNode.HasLocalType;
					inner.enclosingMethod = enclosingMethodJDT;

					// just a dummy scope to be filled in for real later. needed for structure requesting
					enclosingMethodJDT.scope = new MethodScope(outerTypeDeclaration.scope, enclosingMethodJDT,
							enclosingMethodJDT.isStatic());
					if (inner.enclosingMethod.statements == null || inner.enclosingMethod.statements.length == 0) {
						inner.enclosingMethod.statements = new Statement[] { inner.allocation };
					} else {
						Statement[] newStatements = new Statement[inner.enclosingMethod.statements.length + 1];
						System.arraycopy(inner.enclosingMethod.statements, 0, newStatements, 0,
								inner.enclosingMethod.statements.length);
						newStatements[inner.enclosingMethod.statements.length] = inner.allocation;
						inner.enclosingMethod.statements = newStatements;
					}
					((GroovyTypeDeclaration) outerTypeDeclaration).addAnonymousType(inner);

				}
			}
			outerTypeDeclaration.memberTypes = newInnersList.toArray(new TypeDeclaration[newInnersList.size()]);
		}

		types = typeDeclarations.toArray(new TypeDeclaration[typeDeclarations.size()]);

		// clean up
		enclosingMethodMap = null;
	}

	/**
	 * @param classNode
	 * @return
	 */
	private boolean isAnon(ClassNode classNode) {
		// FIXADE does Groovy support non-anon local types???
		return classNode.getEnclosingMethod() != null
		// check to see if anon type inside of a script
				|| (classNode.getOuterClass() != null && classNode.getOuterClass().isScript());
	}

	public char[] toMainName(char[] fileName) {
		if (fileName == null) {
			return new char[0];
		}
		int start = CharOperation.lastIndexOf('/', fileName) + 1;
		if (start == 0 || start < CharOperation.lastIndexOf('\\', fileName))
			start = CharOperation.lastIndexOf('\\', fileName) + 1;

		int end = CharOperation.lastIndexOf('.', fileName);
		if (end == -1)
			end = fileName.length;

		return CharOperation.subarray(fileName, start, end);
	}

	/**
	 * Build JDT representations of all the method/ctors on the groovy type
	 */
	private AbstractMethodDeclaration[] createMethodAndConstructorDeclarations(ClassNode classNode, boolean isEnum,
			CompilationResult compilationResult) {
		List<AbstractMethodDeclaration> accumulatedDeclarations = new ArrayList<AbstractMethodDeclaration>();
		createConstructorDeclarations(classNode, isEnum, accumulatedDeclarations);
		createMethodDeclarations(classNode, isEnum, accumulatedDeclarations);
		return accumulatedDeclarations.toArray(new AbstractMethodDeclaration[accumulatedDeclarations.size()]);
	}

	/**
	 * Hold onto the groovy initializer so we can return it later. This is much easier than translating it into a JDT initializer
	 * and back again later.
	 */
	static class FieldDeclarationWithInitializer extends FieldDeclaration {
		private Expression initializer;

		public FieldDeclarationWithInitializer(char[] name, int sourceStart, int sourceEnd) {
			super(name, sourceStart, sourceEnd);
		}

		public void setGroovyInitializer(Expression initializer) {
			this.initializer = initializer;
		}

		public Expression getGroovyInitializer() {
			return this.initializer;
		}
	}

	/**
	 * Build JDT representations of all the fields on the groovy type. <br>
	 * Enum field handling<br>
	 * Groovy handles them as follows: they have the ACC_ENUM bit set and the type is the type of the declaring enum type. When
	 * building declarations, if you want the SourceTypeBinding to correctly build an enum field binding (in
	 * SourceTypeBinding.resolveTypeFor(FieldBinding)) then you need to: (1) avoid setting modifiers, the enum fields are not
	 * expected to have any modifiers (2) leave the type as null, that is how these things are identified by JDT.
	 * 
	 * @param isEnum
	 */
	private FieldDeclaration[] createFieldDeclarations(ClassNode classNode, boolean isEnum) {
		List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();
		List<FieldNode> fieldNodes = classNode.getFields();
		if (fieldNodes != null) {
			for (FieldNode fieldNode : fieldNodes) {
				if (isEnum && (fieldNode.getName().equals("MAX_VALUE") || fieldNode.getName().equals("MIN_VALUE"))) {
					continue;
				}
				boolean isEnumField = (fieldNode.getModifiers() & Opcodes.ACC_ENUM) != 0;
				boolean isSynthetic = (fieldNode.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0;
				if (!isSynthetic) {
					// JavaStubGenerator ignores private fields but I don't
					// think we want to here
					FieldDeclarationWithInitializer fieldDeclaration = new FieldDeclarationWithInitializer(fieldNode.getName()
							.toCharArray(), 0, 0);
					fieldDeclaration.annotations = transformAnnotations(fieldNode.getAnnotations());
					if (!isEnumField) {
						fieldDeclaration.modifiers = fieldNode.getModifiers() & ~0x4000; // 4000 == AccEnum
						fieldDeclaration.type = createTypeReferenceForClassNode(fieldNode.getType());
					}
					fieldDeclaration.javadoc = new Javadoc(108, 132);
					fixupSourceLocationsForFieldDeclaration(fieldDeclaration, fieldNode, isEnumField);
					fieldDeclaration.setGroovyInitializer(fieldNode.getInitialExpression());
					fieldDeclarations.add(fieldDeclaration);
				}
			}
		}
		return fieldDeclarations.toArray(new FieldDeclaration[fieldDeclarations.size()]);
	}

	/**
	 * Build JDT representations of all the constructors on the groovy type
	 */
	private void createConstructorDeclarations(ClassNode classNode, boolean isEnum,
			List<AbstractMethodDeclaration> accumulatedMethodDeclarations) {
		List<ConstructorNode> constructorNodes = classNode.getDeclaredConstructors();

		char[] ctorName = null;
		if (classNode instanceof InnerClassNode) {
			InnerClassNode innerClassNode = (InnerClassNode) classNode;
			ClassNode outerClass = innerClassNode.getOuterClass();
			String outername = outerClass.getNameWithoutPackage();
			String newInner = innerClassNode.getNameWithoutPackage().substring(outername.length() + 1);
			ctorName = newInner.toCharArray();
		} else {
			ctorName = classNode.getNameWithoutPackage().toCharArray();
		}

		// Do we need a default constructor?
		boolean needsDefaultCtor = constructorNodes.size() == 0 && !classNode.isInterface();

		if (needsDefaultCtor) {
			ConstructorDeclaration constructor = new ConstructorDeclaration(compilationResult);
			constructor.bits |= ASTNode.IsDefaultConstructor;
			if (isEnum) {
				constructor.modifiers = ClassFileConstants.AccPrivate;
			} else {
				constructor.modifiers = ClassFileConstants.AccPublic;
			}
			constructor.selector = ctorName;
			accumulatedMethodDeclarations.add(constructor);
		}

		for (ConstructorNode constructorNode : constructorNodes) {
			ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(compilationResult);

			fixupSourceLocationsForConstructorDeclaration(constructorDeclaration, constructorNode);

			constructorDeclaration.annotations = transformAnnotations(constructorNode.getAnnotations());
			// FIXASC should we just use the constructor node modifiers or does groovy make all constructors public apart from
			// those on enums?
			constructorDeclaration.modifiers = isEnum ? ClassFileConstants.AccPrivate : ClassFileConstants.AccPublic;
			constructorDeclaration.selector = ctorName;
			constructorDeclaration.arguments = createArguments(constructorNode.getParameters(), false);
			constructorDeclaration.thrownExceptions = createTypeReferencesForClassNodes(constructorNode.getExceptions());
			if (constructorNode.hasDefaultValue()) {
				createConstructorVariants(constructorNode, constructorDeclaration, accumulatedMethodDeclarations,
						compilationResult, isEnum);
			} else {
				accumulatedMethodDeclarations.add(constructorDeclaration);
			}

			if (hasAnonInners) {
				enclosingMethodMap.put(constructorNode, constructorDeclaration);
			}
		}

		if (earlyTransforms) {
			executeEarlyTransforms_ConstructorRelated(ctorName, classNode, accumulatedMethodDeclarations);
		}

	}

	/**
	 * Augment set of constructors based on annotations. If the annotations are going to trigger additional constructors later, add
	 * them here.
	 */
	private void executeEarlyTransforms_ConstructorRelated(char[] ctorName, ClassNode classNode,
			List<AbstractMethodDeclaration> accumulatedMethodDeclarations) {
		List<AnnotationNode> annos = classNode.getAnnotations();
		boolean hasImmutableAnnotation = false;
		if (annos != null) {
			for (AnnotationNode anno : annos) {
				if (anno.getClassNode() != null) {
					String annoName = anno.getClassNode().getName();
					if (annoName.equals("groovy.transform.Immutable")) {
						hasImmutableAnnotation = true;
					} else if (annoName.equals("Immutable")) {
						// do our best to see if this is the real groovy @Immutable class node
						ModuleNode module = classNode.getModule();
						if (module != null) {
							ClassNode imp = module.getImportType("Immutable");
							if (imp == null || imp.getName().equals("groovy.transform.Immutable")) {
								hasImmutableAnnotation = true;
							}
						}
					}
				}
			}
		}
		// TODO probably ought to check if clashing import rather than assuming it is groovy-eclipse Immutable (even though that is
		// very likely)
		if (hasImmutableAnnotation) {
			// @Immutable action: new constructor

			// TODO Should check against existing ones before creating a duplicate but quite ugly, and
			// groovy will be checking anyway...
			List<FieldNode> fields = classNode.getFields();
			if (fields.size() > 0) {
				// only add constructor if one or more fields.
				// when no fields are present, fall back on the default generated constructor
				Argument[] arguments = new Argument[fields.size()];
				for (int i = 0; i < fields.size(); i++) {
					FieldNode field = fields.get(i);
					TypeReference parameterTypeReference = createTypeReferenceForClassNode(field.getType());
					// TODO should set type reference position
					arguments[i] = new Argument(fields.get(i).getName().toCharArray(), toPos(field.getStart(), field.getEnd() - 1),
							parameterTypeReference, ClassFileConstants.AccPublic);
					arguments[i].declarationSourceStart = fields.get(i).getStart();
				}
				ConstructorDeclaration constructor = new ConstructorDeclaration(compilationResult);
				constructor.selector = ctorName;
				constructor.modifiers = ClassFileConstants.AccPublic;
				constructor.arguments = arguments;
				accumulatedMethodDeclarations.add(constructor);
			}
		}
	}

	/**
	 * Create JDT Argument representations of Groovy parameters
	 */
	private Argument[] createArguments(Parameter[] ps, boolean isMain) {
		if (ps == null || ps.length == 0) {
			return null;
		}
		Argument[] arguments = new Argument[ps.length];
		for (int i = 0; i < ps.length; i++) {
			Parameter parameter = ps[i];
			TypeReference parameterTypeReference = createTypeReferenceForClassNode(parameter.getType());
			// not doing this for now:
			// if (isMain) {
			// parameterTypeReference = new ArrayTypeReference("String".toCharArray(), 1,
			// (parameterTypeReference.sourceStart << 32) | parameterTypeReference.sourceEnd);
			// }

			arguments[i] = new Argument(parameter.getName().toCharArray(), toPos(parameter.getStart(), parameter.getEnd() - 1),
					parameterTypeReference, ClassFileConstants.AccPublic);
			arguments[i].declarationSourceStart = parameter.getStart();
		}
		if (isVargs(ps) /* && !isMain */) {
			arguments[ps.length - 1].type.bits |= ASTNode.IsVarArgs;
		}
		return arguments;
	}

	/**
	 * Build JDT representations of all the methods on the groovy type
	 */
	private void createMethodDeclarations(ClassNode classNode, boolean isEnum,
			List<AbstractMethodDeclaration> accumulatedDeclarations) {
		List<MethodNode> methods = classNode.getMethods();

		for (MethodNode methodNode : methods) {
			if (isEnum && methodNode.isSynthetic()) {
				// skip synthetic methods in enums
				continue;
				// String name = methodNode.getName();
				// Parameter[] params = methodNode.getParameters();
				// if (name.equals("values") && params.length == 0) {
				// continue;
				// }
				// if (name.equals("valueOf") && params.length == 1 && params[0].getType().equals(ClassHelper.STRING_TYPE)) {
				// continue;
				// }
			}
			MethodDeclaration methodDeclaration = createMethodDeclaration(classNode, methodNode, isEnum, compilationResult);
			// methodDeclaration.javadoc = new Javadoc(0, 20);
			if (methodNode.hasDefaultValue()) {
				createMethodVariants(methodNode, methodDeclaration, accumulatedDeclarations, compilationResult);
			} else {
				accumulatedDeclarations.add(methodDeclaration);
			}
			if (hasAnonInners) {
				enclosingMethodMap.put(methodNode, methodDeclaration);
			}
		}
	}

	/**
	 * Called if a method has some 'defaulting' arguments and will compute all the variants (including the one with all parameters).
	 */
	private void createMethodVariants(MethodNode method, MethodDeclaration methodDecl,
			List<AbstractMethodDeclaration> accumulatedDeclarations, CompilationResult compilationResult) {
		List<Argument[]> variants = getVariantsAllowingForDefaulting(method.getParameters(), methodDecl.arguments);
		for (Argument[] variant : variants) {
			MethodDeclaration variantMethodDeclaration = genMethodDeclarationVariant(method, variant, methodDecl.returnType,
					compilationResult);
			addUnlessDuplicate(accumulatedDeclarations, variantMethodDeclaration);
		}
	}

	/**
	 * In the given list of groovy parameters, some are defined as defaulting to an initial value. This method computes all the
	 * variants of defaulting parameters allowed and returns a List of Argument arrays. Each argument array represents a variation.
	 */
	private List<Argument[]> getVariantsAllowingForDefaulting(Parameter[] groovyParams, Argument[] jdtArguments) {
		List<Argument[]> variants = new ArrayList<Argument[]>();

		int psCount = groovyParams.length;
		Parameter[] wipableParameters = new Parameter[psCount];
		System.arraycopy(groovyParams, 0, wipableParameters, 0, psCount);

		// Algorithm: wipableParameters is the 'full list' of parameters at the start. As the loop is repeated, all the non-null
		// values in the list indicate a parameter variation. On each repeat we null the last one in the list that
		// has an initial expression. This is repeated until there are no more left to null.

		List<Argument> oneVariation = new ArrayList<Argument>();
		int nextToLetDefault = -1;
		do {
			oneVariation.clear();
			nextToLetDefault = -1;
			// Create a variation based on the non null entries left in th elist
			for (int p = 0; p < psCount; p++) {
				if (wipableParameters[p] != null) {
					oneVariation.add(jdtArguments[p]);
					if (wipableParameters[p].hasInitialExpression()) {
						nextToLetDefault = p;
					}
				}
			}
			if (nextToLetDefault != -1) {
				wipableParameters[nextToLetDefault] = null;
			}
			Argument[] argumentsVariant = (oneVariation.size() == 0 ? null : oneVariation
					.toArray(new Argument[oneVariation.size()]));
			variants.add(argumentsVariant);
		} while (nextToLetDefault != -1);

		return variants;
	}

	/**
	 * Add the new declaration to the list of those already built unless it clashes with an existing one. This can happen where the
	 * default parameter mechanism causes creation of a variant that collides with an existing declaration. I'm not sure if Groovy
	 * should be reporting an error when this occurs, but Grails does actually do it and gets no error.
	 */
	private void addUnlessDuplicate(List<AbstractMethodDeclaration> accumulatedDeclarations,
			AbstractMethodDeclaration newDeclaration) {
		boolean isDuplicate = false;

		for (AbstractMethodDeclaration aMethodDecl : accumulatedDeclarations) {
			if (CharOperation.equals(aMethodDecl.selector, newDeclaration.selector)) {
				Argument[] mdArgs = aMethodDecl.arguments;
				Argument[] vmdArgs = newDeclaration.arguments;
				int mdArgsLen = mdArgs == null ? 0 : mdArgs.length;
				int vmdArgsLen = vmdArgs == null ? 0 : vmdArgs.length;
				if (mdArgsLen == vmdArgsLen) {
					boolean argsTheSame = true;
					for (int i = 0; i < mdArgsLen; i++) {
						// FIXASC this comparison can fail if some are fully qualified and some not - in fact it
						// suggests that default param variants should be built by augmentMethod() in a similar way to
						// the GroovyObject methods, rather than during type declaration construction - but not super urgent right
						// now
						if (!CharOperation.equals(mdArgs[i].type.getTypeName(), vmdArgs[i].type.getTypeName())) {
							argsTheSame = false;
							break;
						}
					}
					if (argsTheSame) {
						isDuplicate = true;
						break;
					}
				}
			}
		}

		if (!isDuplicate) {
			accumulatedDeclarations.add(newDeclaration);
		}
	}

	/**
	 * Called if a constructor has some 'defaulting' arguments and will compute all the variants (including the one with all
	 * parameters).
	 */
	private void createConstructorVariants(ConstructorNode constructorNode, ConstructorDeclaration constructorDecl,
			List<AbstractMethodDeclaration> accumulatedDeclarations, CompilationResult compilationResult, boolean isEnum) {

		List<Argument[]> variants = getVariantsAllowingForDefaulting(constructorNode.getParameters(), constructorDecl.arguments);

		for (Argument[] variant : variants) {
			ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(compilationResult);
			constructorDeclaration.annotations = transformAnnotations(constructorNode.getAnnotations());
			constructorDeclaration.modifiers = isEnum ? ClassFileConstants.AccPrivate : ClassFileConstants.AccPublic;
			constructorDeclaration.selector = constructorDecl.selector;
			constructorDeclaration.arguments = variant;
			fixupSourceLocationsForConstructorDeclaration(constructorDeclaration, constructorNode);
			addUnlessDuplicate(accumulatedDeclarations, constructorDeclaration);
		}
	}

	/**
	 * Create a JDT MethodDeclaration that represents a groovy MethodNode
	 */
	private MethodDeclaration createMethodDeclaration(ClassNode classNode, MethodNode methodNode, boolean isEnum,
			CompilationResult compilationResult) {
		if (classNode.isAnnotationDefinition()) {
			AnnotationMethodDeclaration methodDeclaration = new AnnotationMethodDeclaration(compilationResult);
			int modifiers = methodNode.getModifiers();
			modifiers &= ~(ClassFileConstants.AccSynthetic | ClassFileConstants.AccTransient);
			methodDeclaration.annotations = transformAnnotations(methodNode.getAnnotations());
			methodDeclaration.modifiers = modifiers;
			if (methodNode.hasAnnotationDefault()) {
				methodDeclaration.modifiers |= ClassFileConstants.AccAnnotationDefault;
			}
			methodDeclaration.selector = methodNode.getName().toCharArray();
			fixupSourceLocationsForMethodDeclaration(methodDeclaration, methodNode);
			ClassNode returnType = methodNode.getReturnType();
			methodDeclaration.returnType = createTypeReferenceForClassNode(returnType);
			return methodDeclaration;
		} else {
			MethodDeclaration methodDeclaration = new MethodDeclaration(compilationResult);
			// TODO refactor - extract method
			GenericsType[] generics = methodNode.getGenericsTypes();
			// generic method
			if (generics != null && generics.length != 0) {
				methodDeclaration.typeParameters = new TypeParameter[generics.length];
				for (int tp = 0; tp < generics.length; tp++) {
					TypeParameter typeParameter = new TypeParameter();
					typeParameter.name = generics[tp].getName().toCharArray();
					ClassNode[] upperBounds = generics[tp].getUpperBounds();
					if (upperBounds != null) {
						// FIXASC Positional info for these references?
						typeParameter.type = createTypeReferenceForClassNode(upperBounds[0]);
						typeParameter.bounds = (upperBounds.length > 1 ? new TypeReference[upperBounds.length - 1] : null);
						for (int b = 1, max = upperBounds.length; b < max; b++) {
							typeParameter.bounds[b - 1] = createTypeReferenceForClassNode(upperBounds[b]);
							typeParameter.bounds[b - 1].bits |= ASTNode.IsSuperType;
						}
					}
					methodDeclaration.typeParameters[tp] = typeParameter;
				}
			}

			boolean isMain = false;
			// Note: modifiers for the MethodBinding constructed for this declaration will be created marked with
			// AccVarArgs if the bitset for the type reference in the final argument is marked IsVarArgs
			int modifiers = methodNode.getModifiers();
			modifiers &= ~(ClassFileConstants.AccSynthetic | ClassFileConstants.AccTransient);
			methodDeclaration.annotations = transformAnnotations(methodNode.getAnnotations());
			methodDeclaration.modifiers = modifiers;
			methodDeclaration.selector = methodNode.getName().toCharArray();
			// Need to capture the rule in Verifier.adjustTypesIfStaticMainMethod(MethodNode node)
			// if (node.getName().equals("main") && node.isStatic()) {
			// Parameter[] params = node.getParameters();
			// if (params.length == 1) {
			// Parameter param = params[0];
			// if (param.getType() == null || param.getType()==ClassHelper.OBJECT_TYPE) {
			// param.setType(ClassHelper.STRING_TYPE.makeArray());
			// ClassNode returnType = node.getReturnType();
			// if(returnType == ClassHelper.OBJECT_TYPE) {
			// node.setReturnType(ClassHelper.VOID_TYPE);
			// }
			// }
			// }
			Parameter[] params = methodNode.getParameters();
			ClassNode returnType = methodNode.getReturnType();

			// source of 'static main(args)' would become 'static Object main(Object args)' - so transform here
			if ((modifiers & ClassFileConstants.AccStatic) != 0 && params != null && params.length == 1
					&& methodNode.getName().equals("main")) {
				Parameter p = params[0];
				if (p.getType() == null || p.getType().getName().equals(ClassHelper.OBJECT)) {
					String name = p.getName();
					params = new Parameter[1];
					params[0] = new Parameter(ClassHelper.STRING_TYPE.makeArray(), name);
					if (returnType.getName().equals(ClassHelper.OBJECT)) {
						returnType = ClassHelper.VOID_TYPE;
					}
				}
			}

			methodDeclaration.arguments = createArguments(params, isMain);
			methodDeclaration.returnType = createTypeReferenceForClassNode(returnType);
			methodDeclaration.thrownExceptions = createTypeReferencesForClassNodes(methodNode.getExceptions());
			fixupSourceLocationsForMethodDeclaration(methodDeclaration, methodNode);
			return methodDeclaration;
		}
	}

	/**
	 * Create a JDT representation of a groovy MethodNode - but with some parameters defaulting
	 */
	private MethodDeclaration genMethodDeclarationVariant(MethodNode methodNode, Argument[] alternativeArguments,
			TypeReference returnType, CompilationResult compilationResult) {
		MethodDeclaration methodDeclaration = new MethodDeclaration(compilationResult);
		int modifiers = methodNode.getModifiers();
		modifiers &= ~(ClassFileConstants.AccSynthetic | ClassFileConstants.AccTransient);
		methodDeclaration.annotations = transformAnnotations(methodNode.getAnnotations());
		methodDeclaration.modifiers = modifiers;
		methodDeclaration.selector = methodNode.getName().toCharArray();
		methodDeclaration.arguments = alternativeArguments;
		methodDeclaration.returnType = returnType;
		fixupSourceLocationsForMethodDeclaration(methodDeclaration, methodNode);
		return methodDeclaration;
	}

	private void configureSuperInterfaces(TypeDeclaration typeDeclaration, ClassNode classNode) {
		ClassNode[] interfaces = classNode.getInterfaces();
		if (interfaces != null && interfaces.length > 0) {
			typeDeclaration.superInterfaces = new TypeReference[interfaces.length];
			for (int i = 0; i < interfaces.length; i++) {
				typeDeclaration.superInterfaces[i] = createTypeReferenceForClassNode(interfaces[i]);
			}
		} else {
			typeDeclaration.superInterfaces = new TypeReference[0];
		}
	}

	private void configureSuperClass(TypeDeclaration typeDeclaration, ClassNode superclass, boolean isEnum) {
		if (isEnum && superclass.getName().equals("java.lang.Enum")) {
			// Don't wire it in, JDT will do it
			typeDeclaration.superclass = null;
		} else {
			// If the start position is 0 the superclass wasn't actually declared, it was added by Groovy
			if (!(superclass.getStart() == 0 && superclass.equals(ClassHelper.OBJECT_TYPE))) {
				typeDeclaration.superclass = createTypeReferenceForClassNode(superclass);
			}
		}
	}

	// --- helper code

	private final static Map<Character, Integer> charToTypeId = new HashMap<Character, Integer>();

	private final static Map<String, Integer> nameToPrimitiveTypeId = new HashMap<String, Integer>();

	static {
		charToTypeId.put('D', TypeIds.T_double);
		charToTypeId.put('I', TypeIds.T_int);
		charToTypeId.put('F', TypeIds.T_float);
		charToTypeId.put('J', TypeIds.T_long);
		charToTypeId.put('Z', TypeIds.T_boolean);
		charToTypeId.put('B', TypeIds.T_byte);
		charToTypeId.put('C', TypeIds.T_char);
		charToTypeId.put('S', TypeIds.T_short);
		nameToPrimitiveTypeId.put("double", TypeIds.T_double);
		nameToPrimitiveTypeId.put("int", TypeIds.T_int);
		nameToPrimitiveTypeId.put("float", TypeIds.T_float);
		nameToPrimitiveTypeId.put("long", TypeIds.T_long);
		nameToPrimitiveTypeId.put("boolean", TypeIds.T_boolean);
		nameToPrimitiveTypeId.put("byte", TypeIds.T_byte);
		nameToPrimitiveTypeId.put("char", TypeIds.T_char);
		nameToPrimitiveTypeId.put("short", TypeIds.T_short);
		nameToPrimitiveTypeId.put("void", TypeIds.T_void);
		try {
			String value = System.getProperty("	earlyTransforms");
			if (value != null) {
				if (value.equalsIgnoreCase("true")) {
					System.out.println("groovyeclipse.earlyTransforms = true");
					earlyTransforms = true;
				} else if (value.equalsIgnoreCase("false")) {
					System.out.println("groovyeclipse.earlyTransforms = false");
					earlyTransforms = false;
				}
			}
		} catch (Throwable t) {
			// --
		}
	}

	/**
	 * For some input array (usually representing a reference), work out the offset positions, assuming they are dotted. <br>
	 * Currently this uses the size of each component to move from start towards end. For the very last one it makes the end
	 * position 'end' because in some cases just adding 1+length of previous reference isn't enough. For example in java.util.List[]
	 * the end will be the end of [] but reference will only contain 'java' 'util' 'List'
	 * <p>
	 * Because the 'end' is quite often wrong right now (for example on a return type 'java.util.List[]' the end can be two
	 * characters off the end (set to the start of the method name...) - we are just computing the positional information from the
	 * start.
	 */
	// FIXASC seems that sometimes, especially for types that are defined as 'def', but are converted to java.lang.Object, end
	// < start. This causes no end of problems. I don't think it is so much the 'declaration' as the fact that is no reference and
	// really what is computed here is the reference for something actually specified in the source code. Coming up with fake
	// positions for something not specified is not entirely unreasonable we should check
	// if the reference in particular needed creating at all in the first place...
	private long[] positionsFor(char[][] reference, long start, long end) {
		long[] result = new long[reference.length];
		if (start < end) {
			// Do the right thing
			long pos = start;
			for (int i = 0, max = result.length; i < max; i++) {
				long s = pos;
				pos = pos + reference[i].length - 1; // jump to the last char of the name
				result[i] = ((s << 32) | pos);
				pos += 2; // jump onto the following '.' then off it
			}
		} else {
			// FIXASC this case shouldn't happen (end<start) - uncomment following if to collect diagnostics
			long pos = (start << 32) | start;
			for (int i = 0, max = result.length; i < max; i++) {
				result[i] = pos;
			}
		}
		return result;
	}

	/**
	 * Convert from an array ClassNode into a TypeReference. Name of the node is expected to be something like java.lang.String[][]
	 * - primitives should be getting handled by the other create method (and have a sig like '[[I')
	 */
	private TypeReference createTypeReferenceForArrayNameTrailingBrackets(ClassNode node, int start, int end) {
		String name = node.getName();
		int dim = 0;
		int pos = name.length() - 2;
		ClassNode componentType = node;
		// jump back counting dimensions
		while (pos > 0 && name.charAt(pos) == '[') {
			dim++;
			pos -= 2;
			componentType = componentType.getComponentType();
		}
		if (componentType.isPrimitive()) {
			Integer ii = charToTypeId.get(name.charAt(dim));
			if (ii == null) {
				throw new IllegalStateException("node " + node + " reported it had a primitive component type, but it does not...");
			} else {
				TypeReference baseTypeReference = TypeReference.baseTypeReference(ii, dim);
				baseTypeReference.sourceStart = start;
				baseTypeReference.sourceEnd = start + componentType.getName().length();
				return baseTypeReference;
			}
		}
		if (dim == 0) {
			throw new IllegalStateException("Array classnode with dimensions 0?? node:" + node.getName());
		}
		// array component is something like La.b.c; ... or sometimes just [[Z (where Z is a type, not primitive)
		String arrayComponentTypename = name.substring(0, pos + 2);
		if (arrayComponentTypename.indexOf(".") == -1) {
			return createJDTArrayTypeReference(arrayComponentTypename, dim, start, end);
		} else {
			return createJDTArrayQualifiedTypeReference(arrayComponentTypename, dim, start, end);
		}
	}

	/**
	 * Format will be [[I or [[Ljava.lang.String; - this latter form is really not right but groovy can produce it so we need to
	 * cope with it.
	 */
	private TypeReference createTypeReferenceForArrayNameLeadingBrackets(ClassNode node, int start, int end) {
		String name = node.getName();
		int dim = 0;
		ClassNode componentType = node;
		while (name.charAt(dim) == '[') {
			dim++;
			componentType = componentType.getComponentType();
		}
		if (componentType.isPrimitive()) {
			Integer ii = charToTypeId.get(name.charAt(dim));
			if (ii == null) {
				throw new IllegalStateException("node " + node + " reported it had a primitive component type, but it does not...");
			} else {
				TypeReference baseTypeReference = TypeReference.baseTypeReference(ii, dim);
				baseTypeReference.sourceStart = start;
				baseTypeReference.sourceEnd = start + componentType.getName().length();
				return baseTypeReference;
			}
		} else {
			String arrayComponentTypename = name.substring(dim);
			if (arrayComponentTypename.charAt(arrayComponentTypename.length() - 1) == ';') {
				arrayComponentTypename = name.substring(dim + 1, name.length() - 1); // chop off '['s 'L' and ';'
			}
			if (arrayComponentTypename.indexOf(".") == -1) {
				return createJDTArrayTypeReference(arrayComponentTypename, dim, start, end);
			} else {
				return createJDTArrayQualifiedTypeReference(arrayComponentTypename, dim, start, end);
			}
		}
	}

	// because 'length' is computed as 'end-start+1' and start==-1 indicates it does not exist, then
	// to have a length of 0 the end must be -2.
	private static long NON_EXISTENT_POSITION = toPos(-1, -2);

	/**
	 * Pack start and end positions into a long - no adjustments are made to the values passed in, the caller must make any required
	 * adjustments.
	 */
	private static long toPos(long start, long end) {
		if (start == 0 && end <= 0) {
			return NON_EXISTENT_POSITION;
		}
		return ((start << 32) | end);
	}

	private TypeReference createTypeReferenceForClassNode(GenericsType genericsType) {
		if (genericsType.isWildcard()) {
			ClassNode[] bounds = genericsType.getUpperBounds();
			if (bounds != null) {
				// FIXASC other bounds?
				// positions example: (29>31)Set<(33>54)? extends (43>54)Serializable>
				TypeReference boundReference = createTypeReferenceForClassNode(bounds[0]);
				Wildcard wildcard = new Wildcard(Wildcard.EXTENDS);
				wildcard.sourceStart = genericsType.getStart();
				wildcard.sourceEnd = boundReference.sourceEnd();
				wildcard.bound = boundReference;
				return wildcard;
			} else if (genericsType.getLowerBound() != null) {
				// positions example: (67>69)Set<(71>84)? super (79>84)Number>
				TypeReference boundReference = createTypeReferenceForClassNode(genericsType.getLowerBound());
				Wildcard wildcard = new Wildcard(Wildcard.SUPER);
				wildcard.sourceStart = genericsType.getStart();
				wildcard.sourceEnd = boundReference.sourceEnd();
				wildcard.bound = boundReference;
				return wildcard;
			} else {
				Wildcard w = new Wildcard(Wildcard.UNBOUND);
				w.sourceStart = genericsType.getStart();
				w.sourceEnd = genericsType.getStart();
				return w;
			}
			// FIXASC what does the check on this next really line mean?
		} else if (!genericsType.getType().isGenericsPlaceHolder()) {
			TypeReference typeReference = createTypeReferenceForClassNode(genericsType.getType());
			return typeReference;
		} else {
			// this means it is a placeholder. As an example, if the reference is to 'List'
			// then the genericsType info may include a placeholder for the type variable (as the user
			// didn't fill it in as anything) and so for this example the genericsType is 'E extends java.lang.Object'
			// I don't think we need a type reference for this as the type references we are constructed
			// here are representative of what the user did in the source, not the resolved result of that.
			// throw new GroovyEclipseBug();
			return null;
		}
	}

	private TypeReference[] createTypeReferencesForClassNodes(ClassNode[] classNodes) {
		if (classNodes == null || classNodes.length == 0) {
			return null;
		}
		TypeReference[] refs = new TypeReference[classNodes.length];
		for (int i = 0; i < classNodes.length; i++) {
			refs[i] = createTypeReferenceForClassNode(classNodes[i]);
		}
		return refs;
	}

	private TypeReference createTypeReferenceForClassNode(ClassNode classNode) {
		int start = startOffset(classNode);
		int end = endOffset(classNode);

		List<TypeReference> typeArguments = null;

		// need to distinguish between raw usage of a type 'List' and generics
		// usage 'List<T>' -
		// it basically depends upon whether the type variable reference can be
		// resolved within the current 'scope' - if it cannot then this is probably a raw
		// reference (yes?)

		if (classNode.isUsingGenerics()) {
			GenericsType[] genericsInfo = classNode.getGenericsTypes();
			if (genericsInfo != null) {
				for (int g = 0; g < genericsInfo.length; g++) {
					// ClassNode typeArgumentClassNode = genericsInfo[g].getType();
					TypeReference tr = createTypeReferenceForClassNode(genericsInfo[g]);
					if (tr != null) {
						if (typeArguments == null) {
							typeArguments = new ArrayList<TypeReference>();
						}
						typeArguments.add(tr);
					}
					// if (!typeArgumentClassNode.isGenericsPlaceHolder()) {
					// typeArguments.add(createTypeReferenceForClassNode(typeArgumentClassNode));
					// }
				}
			}
		}

		String name = classNode.getName();

		if (name.length() == 1 && name.charAt(0) == '?') {
			return new Wildcard(Wildcard.UNBOUND);
		}

		int arrayLoc = name.indexOf("[");
		if (arrayLoc == 0) {
			return createTypeReferenceForArrayNameLeadingBrackets(classNode, start, end);
		} else if (arrayLoc > 0) {
			return createTypeReferenceForArrayNameTrailingBrackets(classNode, start, end);
		}

		if (nameToPrimitiveTypeId.containsKey(name)) {
			return TypeReference.baseTypeReference(nameToPrimitiveTypeId.get(name), 0);
		}

		if (name.indexOf(".") == -1) {
			if (typeArguments == null) {
				TypeReference tr = verify(new SingleTypeReference(name.toCharArray(), toPos(start, end - 1)));

				if (!checkGenerics) {
					tr.bits |= TypeReference.IgnoreRawTypeCheck;
				}
				return tr;
			} else {
				// FIXASC determine when array dimension used in this case,
				// is it 'A<T[]> or some silliness?
				long l = toPos(start, end - 1);
				return new ParameterizedSingleTypeReference(name.toCharArray(),
						typeArguments.toArray(new TypeReference[typeArguments.size()]), 0, l);
			}
		} else {
			char[][] compoundName = CharOperation.splitOn('.', name.toCharArray());
			if (typeArguments == null) {
				TypeReference tr = new QualifiedTypeReference(compoundName, positionsFor(compoundName, start, end));
				if (!checkGenerics) {
					tr.bits |= TypeReference.IgnoreRawTypeCheck;
				}
				return tr;
			} else {
				// FIXASC support individual parameterization of component
				// references A<String>.B<Wibble>
				TypeReference[][] typeReferences = new TypeReference[compoundName.length][];
				typeReferences[compoundName.length - 1] = typeArguments.toArray(new TypeReference[typeArguments.size()]);
				return new ParameterizedQualifiedTypeReference(compoundName, typeReferences, 0, positionsFor(compoundName, start,
						end));
			}
		}
	}

	private final static boolean DEBUG = false;

	// FIXASC this is useless - use proper positions
	private long[] getPositionsFor(char[][] compoundName) {
		long[] ls = new long[compoundName.length];
		for (int i = 0; i < compoundName.length; i++) {
			ls[i] = 0;
		}
		return ls;
	}

	// FIXASC are costly regens being done for all the classes???

	@SuppressWarnings("unchecked")
	@Override
	public void generateCode() {
		boolean successful = processToPhase(Phases.ALL);
		if (successful) {

			// At the end of this method we want to make this call for each of the classes generated during processing
			//
			// compilationResult.record(classname.toCharArray(), new GroovyClassFile(classname, classbytes, foundBinding, path));
			//
			// For each generated class (in groovyCompilationUnit.getClasses()) we know:
			// String classname = groovyClass.getName(); = this is the name of the generated type (doesn't matter where the
			// declaration was)
			// byte[] classbytes = groovyClass.getBytes(); = duh
			// String path = groovyClass.getName().replace('.', '/'); = where to put it on disk

			// The only tricky piece of information is discovering the binding that gave rise to the type. This is complicated
			// in groovy because it is not policing that the package name matches the directory structure.
			// Effectively the connection between the TypeDeclaration (which points to the binding) and the
			// groovy created component is lost - if that were maintained we would not have to go hunting for it.
			// On finishing processing we have access to the generated classes but GroovyClassFile objects have no idea what
			// their originating ClassNode was.

			// Under eclipse I've extended GroovyClassFile objects to remember their sourceUnit and ClassNode - this means
			// we have to do very little hunting for the binding and don't have to mess around with strings (chopping off
			// packages, etc).

			// This returns all of them, for all source files
			List<GroovyClass> classes = groovyCompilationUnit.getClasses();

			if (DEBUG) {
				log("Processing sourceUnit " + groovySourceUnit.getName());
			}

			for (GroovyClass clazz : classes) {
				ClassNode classnode = clazz.getClassNode();
				if (DEBUG) {
					log("Looking at class " + clazz.getName());
					log("ClassNode where it came from " + classnode);
				}
				// Only care about those coming about because of this groovySourceUnit
				if (clazz.getSourceUnit() == groovySourceUnit) {
					if (DEBUG) {
						log("It is from this source unit");
					}
					// Worth continuing
					String classname = clazz.getName();
					SourceTypeBinding binding = null;
					if (types != null && types.length != 0) {
						binding = findBinding(types, clazz.getClassNode());
					}
					if (DEBUG) {
						log("Binding located?" + (binding != null));
					}
					if (binding == null) {
						// closures will be represented as InnerClassNodes
						ClassNode current = classnode;
						while (current instanceof InnerClassNode && binding == null) {
							current = ((InnerClassNode) current).getOuterClass();
							binding = findBinding(types, current);
							if (DEBUG) {
								log("Had another look because it is in an InnerClassNode, found binding? " + (binding != null));
							}
						}
					}

					boolean isScript = false;
					// Suppress class file output if it is a script
					// null binding implies synthetic type, which we assume cannot be a script
					if (binding != null && binding.scope != null && (binding.scope.parent instanceof GroovyCompilationUnitScope)) {
						GroovyCompilationUnitScope gcuScope = (GroovyCompilationUnitScope) binding.scope.parent;
						if (gcuScope.isScript()) {
							isScript = true;
						}
					}
					if (!isScript) {
						byte[] classbytes = clazz.getBytes();
						String path = clazz.getName().replace('.', '/');
						GroovyClassFile classFile = new GroovyClassFile(classname, classbytes, binding, path);
						char[] classNameChars = classname.toCharArray();
						if (binding == null) {
							// GRECLIPSE-1653 this type likely added by AST transform and is synthetic
							compilationResult.compiledTypes.put(classNameChars, classFile);
						} else {
							compilationResult.record(classNameChars, classFile);
						}
					}
				}
			}
		}
	}

	private void log(String message) {
		System.out.println(message);
	}

	private SourceTypeBinding findBinding(TypeDeclaration[] typedeclarations, ClassNode cnode) {
		for (TypeDeclaration typedeclaration : typedeclarations) {
			GroovyTypeDeclaration groovyTypeDeclaration = (GroovyTypeDeclaration) typedeclaration;
			if (groovyTypeDeclaration.getClassNode().equals(cnode)) {
				return groovyTypeDeclaration.binding;
			}
			if (typedeclaration.memberTypes != null) {
				SourceTypeBinding binding = findBinding(typedeclaration.memberTypes, cnode);
				if (binding != null) {
					return binding;
				}
			}
		}
		return null;
	}

	// ---

	private int startOffset(org.codehaus.groovy.ast.ASTNode astnode) {
		// int l = fromLineColumnToOffset(astnode.getLineNumber(),
		// astnode.getColumnNumber()) - 1;
		// return l;
		return (Math.max(astnode.getStart(), 0));
	}

	private int endOffset(org.codehaus.groovy.ast.ASTNode astnode) {
		// starts from 0 and dont want the char after it, i want the last char
		// return fromLineColumnToOffset(astnode.getLineNumber(),
		// astnode.getLastColumnNumber()) - 2;
		// return astnode.getEnd();
		return (Math.max(astnode.getEnd(), 0));
	}

	// here be dragons
	private void recordProblems(List<?> errors) {
		// FIXASC look at this error situation (described below), surely we need to do it?
		// Due to the nature of driving all groovy entities through compilation together, we can accumulate messages for other
		// compilation units whilst processing the one we wanted to. Per GRE396 this can manifest as recording the wrong thing
		// against the wrong type. That is the only case I have seen of it, so I'm not putting in the general mechanism for all
		// errors yet, I'm just dealing with RuntimeParserExceptions. The general strategy would be to compare the ModuleNode
		// for each message with the ModuleNode currently being processed - if they differ then this isn't a message for this
		// unit and so we ignore it. If we do deal with it then we remember that we did (in errorsRecorded) and remove it from
		// the list of those to process.

		List errorsRecorded = new ArrayList();
		// FIXASC poor way to get the errors attached to the files
		// FIXASC does groovy ever produce warnings? How are they treated here?
		for (Iterator<?> iterator = errors.iterator(); iterator.hasNext();) {
			SyntaxException syntaxException = null;
			Message message = (Message) iterator.next();
			StringWriter sw = new StringWriter();
			message.write(new PrintWriter(sw));
			String msg = sw.toString();
			CategorizedProblem p = null;
			int line = 0;
			int sev = 0;
			int scol = 0;
			int ecol = 0;
			// LocatedMessage instances are produced sometimes, e.g. by grails ast transforms, use the context for position
			if (message instanceof LocatedMessage) {
				CSTNode context = ((LocatedMessage) message).getContext();
				if (context instanceof Token) {
					line = context.getStartLine();
					scol = context.getStartColumn();
					String text = ((Token) context).getText();
					ecol = scol + (text == null ? 1 : (text.length() - 1));
				}
			}
			if (message instanceof SimpleMessage) {
				SimpleMessage simpleMessage = (SimpleMessage) message;
				sev |= ProblemSeverities.Error;
				String simpleText = simpleMessage.getMessage();
				if (simpleText.length() > 1 && simpleText.charAt(0) == '\n') {
					simpleText = simpleText.substring(1);
				}
				msg = "Groovy:" + simpleText;
				if (msg.indexOf("\n") != -1) {
					msg = msg.substring(0, msg.indexOf("\n"));
				}
			}
			if (message instanceof SyntaxErrorMessage) {
				SyntaxErrorMessage errorMessage = (SyntaxErrorMessage) message;
				syntaxException = errorMessage.getCause();
				sev |= ProblemSeverities.Error;
				// FIXASC in the short term, prefixed groovy to indicate
				// where it came from
				String actualMessage = syntaxException.getMessage();
				if (actualMessage.length() > 1 && actualMessage.charAt(0) == '\n') {
					actualMessage = actualMessage.substring(1);
				}
				msg = "Groovy:" + actualMessage;
				if (msg.indexOf("\n") != -1) {
					msg = msg.substring(0, msg.indexOf("\n"));
				}
				line = syntaxException.getLine();
				scol = errorMessage.getCause().getStartColumn();
				ecol = errorMessage.getCause().getEndColumn() - 1;
			}
			int soffset = -1;
			int eoffset = -1;
			if (message instanceof ExceptionMessage) {
				ExceptionMessage em = (ExceptionMessage) message;
				sev |= ProblemSeverities.Error;
				if (em.getCause() instanceof RuntimeParserException) {
					RuntimeParserException rpe = (RuntimeParserException) em.getCause();
					sev |= ProblemSeverities.Error;
					msg = "Groovy:" + rpe.getMessage();
					if (msg.indexOf("\n") != -1) {
						msg = msg.substring(0, msg.indexOf("\n"));
					}
					ModuleNode errorModuleNode = rpe.getModule();
					ModuleNode thisModuleNode = this.getModuleNode();
					if (!errorModuleNode.equals(thisModuleNode)) {
						continue;
					}
					soffset = rpe.getNode().getStart();
					eoffset = rpe.getNode().getEnd() - 1;
					// need to work out the line again as it may be wrong
					line = 0;
					while (compilationResult.lineSeparatorPositions[line] < soffset
							&& line < compilationResult.lineSeparatorPositions.length) {
						line++;
					}

					line++; // from an array index to a real 'line number'
				}
			}
			if (syntaxException instanceof PreciseSyntaxException) {
				soffset = ((PreciseSyntaxException) syntaxException).getStartOffset();
				eoffset = ((PreciseSyntaxException) syntaxException).getEndOffset();
				// need to work out the line again as it may be wrong
				line = 0;
				while (line < compilationResult.lineSeparatorPositions.length
						&& compilationResult.lineSeparatorPositions[line] < soffset) {
					line++;
				}
				;
				line++; // from an array index to a real 'line number'
			} else {
				if (soffset == -1) {
					soffset = getOffset(compilationResult.lineSeparatorPositions, line, scol);
				}
				if (eoffset == -1) {
					eoffset = getOffset(compilationResult.lineSeparatorPositions, line, ecol);
				}
			}
			if (soffset > eoffset) {
				eoffset = soffset;
			}
			if (soffset > sourceEnd) {
				soffset = sourceEnd;
				eoffset = sourceEnd;
			}

			char[] filename = getFileName();
			p = new DefaultProblemFactory().createProblem(filename, 0, new String[] { msg }, 0, new String[] { msg }, sev, soffset,
					eoffset, line, scol);
			this.problemReporter.record(p, compilationResult, this, false);
			errorsRecorded.add(message);
			System.err.println(new String(compilationResult.getFileName()) + ": " + line + " " + msg);
		}
		errors.removeAll(errorsRecorded);
	}

	private int getOffset(int[] lineSeparatorPositions, int line, int col) {
		if (lineSeparatorPositions.length > (line - 2) && line > 1) {
			return lineSeparatorPositions[line - 2] + col;
		} else {
			return col;
		}
	}

	@Override
	public CompilationUnitScope buildCompilationUnitScope(LookupEnvironment lookupEnvironment) {
		GroovyCompilationUnitScope gcus = new GroovyCompilationUnitScope(this, lookupEnvironment);
		gcus.setIsScript(isScript);
		return gcus;
	}

	public ModuleNode getModuleNode() {
		return groovySourceUnit == null ? null : groovySourceUnit.getAST();
	}

	public SourceUnit getSourceUnit() {
		return groovySourceUnit;
	}

	// TODO find a better home for this?
	@Override
	public org.eclipse.jdt.core.dom.CompilationUnit getSpecialDomCompilationUnit(org.eclipse.jdt.core.dom.AST ast) {
		return new org.codehaus.jdt.groovy.core.dom.GroovyCompilationUnit(ast);
	}

	/**
	 * Try to get the source locations for type declarations to be as correct as possible
	 */
	private void fixupSourceLocationsForTypeDeclaration(GroovyTypeDeclaration typeDeclaration, ClassNode classNode) {
		// start and end of the name of class
		if (classNode instanceof InnerClassNode) {
			// anonynymous inner classes do not have start and end set aproproately
			typeDeclaration.sourceStart = classNode.getNameStart();
			typeDeclaration.sourceEnd = classNode.getNameEnd();
		} else {
			// scripts do not have a name, so use start instead
			typeDeclaration.sourceStart = Math.max(classNode.getNameStart(), classNode.getStart());
			typeDeclaration.sourceEnd = Math.max(classNode.getNameEnd(), classNode.getStart());
		}
		// start and end of the entire declaration including Javadoc and ending at the last close bracket
		int line = classNode.getLineNumber();
		Javadoc doc = findJavadoc(line);
		if (doc != null) {
			if (imports != null && imports.length > 0) {
				if (doc.sourceStart < imports[imports.length - 1].sourceStart) {
					// ignore the doc if it should be associated with and import statement
					doc = null;
				}
			} else if (currentPackage != null) {
				if (doc.sourceStart < currentPackage.sourceStart) {
					// ignore the doc if it should be associated with the package statement
					doc = null;
				}
			}
		}

		typeDeclaration.javadoc = doc;
		typeDeclaration.declarationSourceStart = doc == null ? classNode.getStart() : doc.sourceStart;
		// Without the -1 we can hit AIOOBE in org.eclipse.jdt.internal.core.Member.getJavadocRange where it calls getText()
		// because the source range length causes us to ask for more data than is in the buffer. What does this mean?
		// For hovers, the AIOOBE is swallowed and you just see no hover box.
		typeDeclaration.declarationSourceEnd = classNode.getEnd() - 1;

		// * start at the opening brace and end at the closing brace
		// except that scripts do not have a name, use the start instead
		// FIXADE this is not exactly right since getNameEnd() comes before extends and implements clauses
		typeDeclaration.bodyStart = Math.max(classNode.getNameEnd(), classNode.getStart());

		// seems to be the same as declarationSourceEnd
		typeDeclaration.bodyEnd = classNode.getEnd() - 1;

		// start of the modifiers after the javadoc
		typeDeclaration.modifiersSourceStart = classNode.getStart();

	}

	/**
	 * Try to get the source locations for constructor declarations to be as correct as possible
	 */
	private void fixupSourceLocationsForConstructorDeclaration(ConstructorDeclaration ctorDeclaration, ConstructorNode ctorNode) {
		ctorDeclaration.sourceStart = ctorNode.getNameStart();
		ctorDeclaration.sourceEnd = ctorNode.getNameEnd();

		// start and end of method declaration including JavaDoc
		// ending with closing '}' or ';' if abstract
		int line = ctorNode.getLineNumber();
		Javadoc doc = findJavadoc(line);
		ctorDeclaration.javadoc = doc;
		ctorDeclaration.declarationSourceStart = doc == null ? ctorNode.getStart() : doc.sourceStart;
		ctorDeclaration.declarationSourceEnd = ctorNode.getEnd() - 1;

		// start of method's modifier list (after Javadoc is ended)
		ctorDeclaration.modifiersSourceStart = ctorNode.getStart();

		// opening bracket
		ctorDeclaration.bodyStart =
		// try for opening bracket
		ctorNode.getCode() != null ? ctorNode.getCode().getStart() :
		// handle abstract constructor. not sure if this can ever happen, but you never know with Groovy
				ctorNode.getNameEnd();

		// closing bracket or ';' same as declarationSourceEnd
		ctorDeclaration.bodyEnd = ctorNode.getEnd() - 1;
	}

	/**
	 * Try to get the source locations for method declarations to be as correct as possible
	 */
	private void fixupSourceLocationsForMethodDeclaration(MethodDeclaration methodDeclaration, MethodNode methodNode) {
		// run() method for scripts has no name, so use the start of the method instead
		methodDeclaration.sourceStart = Math.max(methodNode.getNameStart(), methodNode.getStart());
		methodDeclaration.sourceEnd = Math.max(methodNode.getNameEnd(), methodNode.getStart());

		// start and end of method declaration including JavaDoc
		// ending with closing '}' or ';' if abstract
		int line = methodNode.getLineNumber();
		Javadoc doc = findJavadoc(line);
		methodDeclaration.javadoc = doc;
		methodDeclaration.declarationSourceStart = doc == null ? methodNode.getStart() : doc.sourceStart;
		methodDeclaration.declarationSourceEnd = methodNode.getEnd() - 1;

		// start of method's modifier list (after Javadoc is ended)
		methodDeclaration.modifiersSourceStart = methodNode.getStart();

		// opening bracket
		methodDeclaration.bodyStart =
		// try for opening bracket
		methodNode.getCode() != null ? methodNode.getCode().getStart() :
		// run() method for script has no opening bracket
		// also need to handle abstract methods
				Math.max(methodNode.getNameEnd(), methodNode.getStart());

		// closing bracket or ';' same as declarationSourceEnd
		methodDeclaration.bodyEnd = methodNode.getEnd() - 1;
	}

	/**
	 * Try to get the source locations for field declarations to be as correct as possible
	 */
	private void fixupSourceLocationsForFieldDeclaration(FieldDeclaration fieldDeclaration, FieldNode fieldNode, boolean isEnumField) {
		// TODO (groovy) each area marked with a '*' is only approximate
		// and can be revisited to make more precise

		// Here, we distinguish between the declaration and the fragment
		// e.g.- def x = 9, y = "l"
		// 'x = 9,' and 'y = "l"' are the fragments and 'def x = 9, y = "l"' is the declaration

		// the start and end of the fragment name
		fieldDeclaration.sourceStart = fieldNode.getNameStart();
		fieldDeclaration.sourceEnd = fieldNode.getNameEnd();

		// start of the declaration (including javadoc?)
		int line = fieldNode.getLineNumber();
		Javadoc doc = findJavadoc(line);
		fieldDeclaration.javadoc = doc;

		if (isEnumField) {
			// they have no 'leading' type declaration or modifiers
			fieldDeclaration.declarationSourceStart = doc == null ? fieldNode.getNameStart() : doc.sourceStart;// fieldNode.getNameStart();
			fieldDeclaration.declarationSourceEnd = fieldNode.getNameEnd() - 1;
		} else {
			fieldDeclaration.declarationSourceStart = doc == null ? fieldNode.getStart() : doc.sourceStart;
			// the end of the fragment including initializer (and trailing ',')
			fieldDeclaration.declarationSourceEnd = fieldNode.getEnd() - 1;
		}
		// * first character of the declaration's modifier
		fieldDeclaration.modifiersSourceStart = fieldNode.getStart();

		// end of the entire Field declaration (after all fragments and including ';' if exists)
		fieldDeclaration.declarationEnd = fieldNode.getEnd();

		// * end of the type declaration part of the declaration (the same for each fragment)
		// eg- int x, y corresponds to the location after 'int'
		fieldDeclaration.endPart1Position = fieldNode.getNameStart();

		// * just before the start of the next fragment
		// (or the end of the entire declaration if it is the last one)
		// (how is this different from declarationSourceEnd?)
		fieldDeclaration.endPart2Position = fieldNode.getEnd() - 1;
	}

	/**
	 * @return true if this is varargs, using the same definition as in AsmClassGenerator.isVargs(Parameter[])
	 */
	private boolean isVargs(Parameter[] parameters) {
		if (parameters.length == 0) {
			return false;
		}
		ClassNode clazz = parameters[parameters.length - 1].getType();
		return (clazz.isArray());
	}

	// for testing
	public String print() {
		return toString();
	}

	public GroovyCompilationUnitScope getScope() {
		return (GroovyCompilationUnitScope) scope;
	}

	// -- overridden behaviour from the supertype

	@Override
	public void resolve() {
		processToPhase(Phases.SEMANTIC_ANALYSIS);
		checkForTags();
		setComments();
	}

	/**
	 * Check any comments from the source file for task tag markers.
	 */
	private void checkForTags() {
		if (this.compilerOptions == null) {
			return;
		}
		List<Comment> comments = groovySourceUnit.getComments();
		if (comments == null) {
			return;
		}
		char[][] taskTags = this.compilerOptions.taskTags;
		char[][] taskPriorities = this.compilerOptions.taskPriorities;
		boolean caseSensitiveTags = this.compilerOptions.isTaskCaseSensitive;
		try {
			if (taskTags != null) {
				// For each comment find all task tags within it and cope with
				for (Comment comment : comments) {
					List<TaskEntry> allTasksInComment = new ArrayList<TaskEntry>();
					for (int t = 0; t < taskTags.length; t++) {
						String taskTag = new String(taskTags[t]);
						String taskPriority = null;
						if (taskPriorities != null) {
							taskPriority = new String(taskPriorities[t]);
						}
						allTasksInComment.addAll(comment.getPositionsOf(taskTag, taskPriority,
								compilationResult.lineSeparatorPositions, caseSensitiveTags));
					}
					if (!allTasksInComment.isEmpty()) {
						// Need to check quickly for clashes
						for (int t1 = 0; t1 < allTasksInComment.size(); t1++) {
							for (int t2 = 0; t2 < allTasksInComment.size(); t2++) {
								if (t1 == t2)
									continue;
								TaskEntry taskOne = allTasksInComment.get(t1);
								TaskEntry taskTwo = allTasksInComment.get(t2);
								if (DEBUG_TASK_TAGS) {
									System.out.println("Comparing " + taskOne.toString() + " and " + taskTwo.toString());
								}
								if ((taskOne.start + taskOne.taskTag.length() + 1) == taskTwo.start) {
									// Adjacent tags
									taskOne.isAdjacentTo = taskTwo;
								} else {
									if ((taskOne.getEnd() > taskTwo.start) && (taskOne.start < taskTwo.start)) {
										taskOne.setEnd(taskTwo.start - 1);
										if (DEBUG_TASK_TAGS) {
											System.out.println("trim " + taskOne.toString() + " and " + taskTwo.toString());
										}
									} else if (taskTwo.getEnd() > taskOne.start && taskTwo.start < taskOne.start) {
										taskTwo.setEnd(taskOne.start - 1);
										if (DEBUG_TASK_TAGS) {
											System.out.println("trim " + taskOne.toString() + " and " + taskTwo.toString());
										}
									}
								}
							}
						}
						for (TaskEntry taskEntry : allTasksInComment) {
							this.problemReporter.referenceContext = this;
							if (DEBUG_TASK_TAGS) {
								System.out.println("Adding task " + taskEntry.toString());
							}
							problemReporter.task(taskEntry.taskTag, taskEntry.getText(), taskEntry.taskPriority, taskEntry.start,
									taskEntry.getEnd());
						}
					}
				}
			}
		} catch (AbortCompilation ac) {
			// that is ok... probably cancelled
		} catch (Throwable t) {
			Util.log(t, "Unexpected problem processing task tags in " + groovySourceUnit.getName());
			new RuntimeException("Unexpected problem processing task tags in " + groovySourceUnit.getName(), t).printStackTrace();
		}
	}

	@Override
	public void analyseCode() {
		processToPhase(Phases.CANONICALIZATION);
	}

	@Override
	public void abort(int abortLevel, CategorizedProblem problem) {
		// FIXASC look at callers of this, should we be following the abort on first problem policy?
		super.abort(abortLevel, problem);
	}

	@Override
	public void checkUnusedImports() {
		super.checkUnusedImports();
	}

	@Override
	public void cleanUp() {
		// FIXASC any tidy up for us to do?
		super.cleanUp();
	}

	@Override
	public CompilationResult compilationResult() {
		return super.compilationResult();
	}

	@Override
	public TypeDeclaration declarationOfType(char[][] typeName) {
		return super.declarationOfType(typeName);
	}

	@Override
	public void finalizeProblems() {
		super.finalizeProblems();
	}

	@Override
	public char[] getFileName() {
		return super.getFileName();
	}

	@Override
	public char[] getMainTypeName() {
		// FIXASC necessary to return something for groovy?
		return super.getMainTypeName();
	}

	@Override
	public boolean hasErrors() {
		return super.hasErrors();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public boolean isPackageInfo() {
		return super.isPackageInfo();
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		// FIXASC additional stuff to print?
		return super.print(indent, output);
	}

	@Override
	public void propagateInnerEmulationForAllLocalTypes() {
		// FIXASC anything to do here for groovy inner types?
		super.propagateInnerEmulationForAllLocalTypes();
	}

	@Override
	public void record(LocalTypeBinding localType) {
		super.record(localType);
	}

	@Override
	public void recordStringLiteral(StringLiteral literal, boolean fromRecovery) {
		// FIXASC assert not called for groovy, surely
		super.recordStringLiteral(literal, fromRecovery);
	}

	@Override
	public void recordSuppressWarnings(IrritantSet irritants, Annotation annotation, int scopeStart, int scopeEnd) {
		super.recordSuppressWarnings(irritants, annotation, scopeStart, scopeEnd);
	}

	@Override
	public void tagAsHavingErrors() {
		super.tagAsHavingErrors();
	}

	@Override
	public void traverse(ASTVisitor visitor, CompilationUnitScope unitScope) {
		// FIXASC are we well formed enough for this?
		super.traverse(visitor, unitScope);
	}

	@Override
	public ASTNode concreteStatement() {
		// FIXASC assert not called for groovy, surely
		return super.concreteStatement();
	}

	@Override
	public boolean isImplicitThis() {
		// FIXASC assert not called for groovy, surely
		return super.isImplicitThis();
	}

	@Override
	public boolean isSuper() {
		// FIXASC assert not called for groovy, surely
		return super.isSuper();
	}

	@Override
	public boolean isThis() {
		// FIXASC assert not called for groovy, surely
		return super.isThis();
	}

	@Override
	public int sourceEnd() {
		return super.sourceEnd();
	}

	@Override
	public int sourceStart() {
		return super.sourceStart();
	}

	@Override
	public String toString() {
		// FIXASC anything to add?
		return super.toString();
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		// FIXASC in a good state for traversal? what would cause this to trigger?
		super.traverse(visitor, scope);
	}

	// -- builders for JDT TypeReference subclasses

	/**
	 * Create a JDT ArrayTypeReference.<br>
	 * Positional information:
	 * <p>
	 * For a single array reference, for example 'String[]' start will be 'S' and end will be the char after ']'. When the
	 * ArrayTypeReference is built we need these positions for the result: sourceStart - the 'S'; sourceEnd - the ']';
	 * originalSourceEnd - the 'g'
	 */
	private ArrayTypeReference createJDTArrayTypeReference(String arrayComponentTypename, int dimensions, int start, int end) {
		ArrayTypeReference atr = new ArrayTypeReference(arrayComponentTypename.toCharArray(), dimensions, toPos(start, end - 1));
		atr.originalSourceEnd = atr.sourceStart + arrayComponentTypename.length() - 1;
		return atr;
	}

	/**
	 * Create a JDT ArrayQualifiedTypeReference.<br>
	 * Positional information:
	 * <p>
	 * For a qualified array reference, for example 'java.lang.Number[][]' start will be 'j' and end will be the char after ']'.
	 * When the ArrayQualifiedTypeReference is built we need these positions for the result: sourceStart - the 'j'; sourceEnd - the
	 * final ']'; the positions computed for the reference components would be j..a l..g and N..r
	 */
	private ArrayQualifiedTypeReference createJDTArrayQualifiedTypeReference(String arrayComponentTypename, int dimensions,
			int start, int end) {
		char[][] compoundName = CharOperation.splitOn('.', arrayComponentTypename.toCharArray());
		ArrayQualifiedTypeReference aqtr = new ArrayQualifiedTypeReference(compoundName, dimensions, positionsFor(compoundName,
				start, end - dimensions * 2));
		aqtr.sourceEnd = end - 1;
		return aqtr;
	}

	/**
	 * Check the supplied TypeReference. If there are problems with the construction of a TypeReference then these may not surface
	 * until it is used later, perhaps when reconciling. The easiest way to check there will not be problems later is to check it at
	 * construction time.
	 * 
	 * @param toVerify the type reference to check
	 * @param does the type reference really exist in the source or is it conjured up based on the source
	 * @return the verified type reference
	 * @throws IllegalStateException if the type reference is malformed
	 */
	private TypeReference verify(TypeReference toVerify) {
		if (GroovyCheckingControl.checkTypeReferences) {
			if (toVerify.getClass().equals(SingleTypeReference.class)) {
				SingleTypeReference str = (SingleTypeReference) toVerify;
				if (str.sourceStart == -1) {
					if (str.sourceEnd != -2) {
						throw new IllegalStateException("TypeReference '" + new String(str.token) + " should end at -2");
					}
				} else {
					if (str.sourceEnd < str.sourceStart) {
						throw new IllegalStateException("TypeReference '" + new String(str.token) + " should end at "
								+ str.sourceStart + " or later");
					}
				}
			} else {
				throw new IllegalStateException("Cannot verify type reference of this class " + toVerify.getClass());
			}
		}
		return toVerify;
	}

	public void tagAsScript() {
		this.isScript = true;
	}

}
