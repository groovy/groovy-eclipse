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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
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
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.PreciseSyntaxException;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.tools.GroovyClass;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.util.Util;
import org.objectweb.asm.Opcodes;

/**
 * A subtype of JDT CompilationUnitDeclaration that represents a groovy source file. It overrides methods as appropriate, delegating
 * to the groovy infrastructure.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class GroovyCompilationUnitDeclaration extends CompilationUnitDeclaration {

	// FIXASC (M2) need to look at all callers of getTypeClass() on the ClassNode to see what they want it for !

	// The groovy compilation unit shared by all files in the same project
	private CompilationUnit groovyCompilationUnit;

	// The groovy sourceunit (a member of the groovyCompilationUnit)
	private SourceUnit groovySourceUnit;

	// FIXASC (M2) decide if we need this resolution stack
	private Stack<GroovyTypeDeclaration> typeDecls = new Stack<GroovyTypeDeclaration>();

	public GroovyCompilationUnitDeclaration(ProblemReporter problemReporter, CompilationResult compilationResult, int sourceLength,
			CompilationUnit groovyCompilationUnit, SourceUnit groovySourceUnit) {
		super(problemReporter, compilationResult, sourceLength);
		this.groovyCompilationUnit = groovyCompilationUnit;
		this.groovySourceUnit = groovySourceUnit;
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
			groovyCompilationUnit.compile(phase);
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
				System.out.println("Groovy Bug error thrown, but can ignore because "
						+ "likely to have been caused by existing syntax problems");
			} else {
				System.err.println("Groovy Bug --- " + gbr.getBugText());
				gbr.printStackTrace();
				// The groovy compiler threw an exception
				// FIXASC (M3) Should record these errors as a problem on the project
				// should *not* throw these because of bad syntax in the file
				Util.log(gbr, "Groovy bug when compiling.");
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
	 * Populate the compilation unit based on the successful parse.
	 */
	public void populateCompilationUnitDeclaration() {
		ModuleNode moduleNode = groovySourceUnit.getAST();
		createPackageDeclaration(moduleNode);
		createImports(moduleNode);
		createTypeDeclarations(moduleNode);
	}

	private void createImports(ModuleNode moduleNode) {
		List<ImportNode> importNodes = moduleNode.getImports();
		List<String> importPackages = moduleNode.getImportPackages();
		if (importNodes.size() > 0 || importPackages.size() > 0) {
			int importNum = 0;
			imports = new ImportReference[importNodes.size() + importPackages.size()];
			for (ImportNode importNode : importNodes) {
				char[][] splits = CharOperation.splitOn('.', importNode.getClassName().toCharArray());
				// FIXASC (M3) the importNode itself does not have correct start and end positions but the type inside of it does.
				// this should be changed so that the importNode itself has the locations
				ImportReference ref = null;

				// FIXASC (M2) cheating here - seeing if it is an alias by whether the alias matches the end of the typename
				// (wrong!)
				if (importNode.getAlias() != null && importNode.getAlias().length() > 0
				// && !importNode.getType().getName().endsWith(importNode.getAlias())
				) {
					// aliased import!
					// FIXASC (M2) will need extra positional info for the 'as' and the alias
					ref = new AliasImportReference(importNode.getAlias().toCharArray(), splits, positionsFor(splits,
							startOffset(importNode.getType()), endOffset(importNode.getType())), false,
							ClassFileConstants.AccDefault);
				} else {
					ref = new ImportReference(splits, positionsFor(splits, startOffset(importNode.getType()), endOffset(importNode
							.getType())), false, ClassFileConstants.AccDefault);
				}
				ref.declarationSourceStart = ref.sourceStart;
				ref.declarationSourceEnd = ref.sourceEnd;
				ref.declarationEnd = ref.sourceEnd;
				imports[importNum++] = ref;
			}
			for (String importPackage : importPackages) {
				char[][] splits = CharOperation.splitOn('.', importPackage.substring(0, importPackage.length() - 1).toCharArray());
				imports[importNum++] = new ImportReference(splits, getPositionsFor(splits), true, ClassFileConstants.AccDefault);
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
		// FIXASC (M2) positions are crap
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
				} else if (memberValuePairs.size() == 1 && memberValuePairs.containsKey("value")) {
					// Single member annotation

					// Code written to only manage a single class literal value annotation - so that @RunWith works
					Expression value = memberValuePairs.get("value");
					if (value instanceof PropertyExpression) {
						String pExpression = ((PropertyExpression) value).getPropertyAsString();
						if (pExpression.equals("class")) {
							TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
							annotationReference.sourceStart = annotationNode.getStart();
							annotationReference.sourceEnd = annotationNode.getEnd();
							SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference, annotationNode
									.getStart());
							annotation.memberValue = new ClassLiteralAccess(value.getEnd(),
									classLiteralToTypeReference((PropertyExpression) value));
							annotation.declarationSourceEnd = annotation.sourceStart + annoType.getNameWithoutPackage().length();
							annotations.add(annotation);
						}
					} else if (value instanceof VariableExpression && annoType.getName().endsWith("RunWith")) {
						// FIXASC (M2) special case for 'RunWith(Foo)' where for some reason groovy doesn't mind the missing
						// '.class'
						// FIXASC (M2) test this
						TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
						annotationReference.sourceStart = annotationNode.getStart();
						annotationReference.sourceEnd = annotationNode.getEnd();
						SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference, annotationNode
								.getStart());
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
						// FIXASC (M2) underlying for SuppressWarnings doesn't seem right when included in messages
					} else if (annoType.getName().equals("SuppressWarnings")
							&& (value instanceof ConstantExpression || value instanceof ListExpression)) {
						if (value instanceof ListExpression) {
							ListExpression listExpression = (ListExpression) value;
							// FIXASC (M2) tidy up all this junk
							List<Expression> listOfExpressions = listExpression.getExpressions();
							TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
							annotationReference.sourceStart = annotationNode.getStart();
							annotationReference.sourceEnd = annotationNode.getEnd() - 1;
							SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference, annotationNode
									.getStart());

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
								// FIXASC (M2) test positional info for conjured up anno refs
								TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
								annotationReference.sourceStart = annotationNode.getStart();
								annotationReference.sourceEnd = annotationNode.getEnd() - 1;
								SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference, annotationNode
										.getStart());
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

		// FIXASC (M2) ignore type parameters for now
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
	 * Build JDT TypeDeclarations for the groovy type declarations that were parsed from the source file.
	 */
	private void createTypeDeclarations(ModuleNode moduleNode) {
		List<ClassNode> classNodes = moduleNode.getClasses();
		List<TypeDeclaration> typeDeclarations = new ArrayList<TypeDeclaration>();
		for (ClassNode classNode : classNodes) {
			if (!classNode.isPrimaryClassNode()) {
				continue;
			}

			GroovyTypeDeclaration typeDeclaration = new GroovyTypeDeclaration(compilationResult, classNode);
			try {
				pushTypeDecl(typeDeclaration);
				typeDeclaration.annotations = transformAnnotations(classNode.getAnnotations());
				typeDeclaration.name = classNode.getNameWithoutPackage().toCharArray();

				boolean isInterface = classNode.isInterface();
				int mods = classNode.getModifiers();
				if ((mods & Opcodes.ACC_ENUM) != 0) {
					// remove final
					mods = mods & ~Opcodes.ACC_FINAL;
				}
				// FIXASC (M2) should this modifier be set?
				// mods |= Opcodes.ACC_PUBLIC;
				// FIXASC (M2) inner class support when it is in groovy 1.7
				// FIXASC (M2) should not do this for inner classes, just for top level types
				// FIXASC (M2) does this make things visible that shouldn't be?
				mods = mods & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
				typeDeclaration.modifiers = mods & ~(isInterface ? Opcodes.ACC_ABSTRACT : 0);

				// FIXADE (M2) only type declarations not named after the compilation unit should be secondary
				typeDeclaration.bits |= ASTNode.IsSecondaryType;

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
				typeDeclaration.fields = createFieldDeclarations(classNode);
				typeDeclaration.properties = classNode.getProperties();
			} finally {
				popTypeDecl();
			}
			typeDeclarations.add(typeDeclaration);
		}
		types = typeDeclarations.toArray(new TypeDeclaration[typeDeclarations.size()]);
	}

	private void pushTypeDecl(GroovyTypeDeclaration typeDeclaration) {
		typeDecls.push(typeDeclaration);
	}

	private void popTypeDecl() {
		typeDecls.pop();
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
	 * Build JDT representations of all the fields on the groovy type
	 */
	private FieldDeclaration[] createFieldDeclarations(ClassNode classNode) {
		List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();
		List<FieldNode> fieldNodes = classNode.getFields();
		if (fieldNodes != null) {
			for (FieldNode fieldNode : fieldNodes) {
				// boolean isEnumField = (fieldNode.getModifiers() & Opcodes.ACC_ENUM) != 0;
				boolean isSynthetic = (fieldNode.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0;
				// if (isEnumField) {
				// enumFields.add(fieldNode);
				// } else
				if (!isSynthetic) {
					// JavaStubGenerator ignores private fields but I don't
					// think we want to here
					FieldDeclaration fieldDeclaration = new FieldDeclaration(fieldNode.getName().toCharArray(), 0, 0);
					fieldDeclaration.annotations = transformAnnotations(fieldNode.getAnnotations());
					// 4000 == AccEnum
					fieldDeclaration.modifiers = fieldNode.getModifiers() & ~0x4000;
					fieldDeclaration.type = createTypeReferenceForClassNode(fieldNode.getType());

					fixupSourceLocationsForFieldDeclaration(fieldDeclaration, fieldNode);
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
			constructor.selector = classNode.getNameWithoutPackage().toCharArray();
			accumulatedMethodDeclarations.add(constructor);
		}

		for (ConstructorNode constructorNode : constructorNodes) {
			ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(compilationResult);

			fixupSourceLocationsForConstructorDeclaration(constructorDeclaration, constructorNode);

			constructorDeclaration.annotations = transformAnnotations(constructorNode.getAnnotations());
			// FIXASC (M2) should we just use the constructor node modifiers or does groovy make all constructors public apart from
			// those on enums?
			constructorDeclaration.modifiers = isEnum ? ClassFileConstants.AccPrivate : ClassFileConstants.AccPublic;
			constructorDeclaration.selector = classNode.getNameWithoutPackage().toCharArray();
			constructorDeclaration.arguments = createArguments(constructorNode.getParameters(), false);
			if (constructorNode.hasDefaultValue()) {
				createConstructorVariants(constructorNode, constructorDeclaration, accumulatedMethodDeclarations,
						compilationResult, isEnum);
			} else {
				accumulatedMethodDeclarations.add(constructorDeclaration);
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
				// skip values() method and valueOf(String)
				String name = methodNode.getName();
				Parameter[] params = methodNode.getParameters();
				if (name.equals("values") && params.length == 0) {
					continue;
				}
				if (name.equals("valueOf") && params.length == 1 && params[0].getType().equals(ClassHelper.STRING_TYPE)) {
					continue;
				}
			}
			MethodDeclaration methodDeclaration = createMethodDeclaration(classNode, methodNode, isEnum, compilationResult);
			if (methodNode.hasDefaultValue()) {
				createMethodVariants(methodNode, methodDeclaration, accumulatedDeclarations, compilationResult);
			} else {
				accumulatedDeclarations.add(methodDeclaration);
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
						// FIXASC (M2) this comparison can fail if some are fully qualified and some not - in fact it
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
		MethodDeclaration methodDeclaration = new MethodDeclaration(compilationResult);
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
		fixupSourceLocationsForMethodDeclaration(methodDeclaration, methodNode);
		return methodDeclaration;
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
			typeDeclaration.superclass = createTypeReferenceForClassNode(superclass);
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
	// FIXASC (M2) seems that sometimes, especially for types that are defined as 'def', but are converted to java.lang.Object, end
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
			// FIXASC (M2) this case shouldn't happen (end<start) - uncomment following if to collect diagnostics
			long pos = (start << 32) | start;
			for (int i = 0, max = result.length; i < max; i++) {
				result[i] = pos;
			}
		}
		return result;
	}

	/**
	 * Convert from a signature form to a type reference. For example "C" for character "[[Z" for array of array of boolean
	 */
	private TypeReference createTypeReferenceForArrayName(ClassNode node, int start, int end) {
		String signature = node.getName();
		int pos = 0;
		while (signature.charAt(pos) == '[') {
			pos++;
		}
		int dim = pos;

		if (signature.length() == (pos + 1)) {
			// primitive array component
			return TypeReference.baseTypeReference(charToTypeId.get(signature.charAt(pos)), dim);
		}

		// array component is something like La.b.c;
		if (signature.charAt(pos) == 'L' && dim > 0) {
			String arrayComponentTypename = signature.substring(pos + 1, signature.length() - 1); // chop off '['s 'L' and ';'
			if (arrayComponentTypename.indexOf(".") == -1) {
				return createJDTArrayTypeReference(arrayComponentTypename, dim, start, end);
			} else {
				return createJDTArrayQualifiedTypeReference(arrayComponentTypename, dim, start, end);
			}
		}
		throw new GroovyEclipseBug("Unable to convert signature to reference.  Signature was '" + signature + "'");
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
				// FIXASC (M2) other bounds?
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
			// FIXASC (M2) what does the check on this next really line mean?
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

	private TypeReference createTypeReferenceForClassNode(ClassNode classNode) {
		int start = startOffset(classNode);
		int end = endOffset(classNode);

		List<TypeReference> typeArguments = null;

		// need to distinguish between raw usage of a type 'List' and generics
		// usage 'List<T>' - it basically depends upon whether the type variable reference can be
		// resolved within the current 'scope' - if it cannot then this is probably a raw
		// reference (yes?)

		if (classNode.isUsingGenerics()) {
			GenericsType[] genericsInfo = classNode.getGenericsTypes();
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

		String name = classNode.getName();

		if (name.length() == 1 && name.charAt(0) == '?') {
			return new Wildcard(Wildcard.UNBOUND);
		}

		// array? [Ljava/lang/String;
		if (name.charAt(0) == '[') {
			return createTypeReferenceForArrayName(classNode, start, end);
		}

		if (nameToPrimitiveTypeId.containsKey(name)) {
			return TypeReference.baseTypeReference(nameToPrimitiveTypeId.get(name), 0);
		}

		if (name.indexOf(".") == -1) {
			if (typeArguments == null) {
				return verify(new SingleTypeReference(name.toCharArray(), toPos(start, end - 1)));
			} else {
				// FIXASC (M2) determine when array dimension used in this case,
				// is it 'A<T[]> or some silliness?
				long l = toPos(start, end - 1);
				return new ParameterizedSingleTypeReference(name.toCharArray(), typeArguments
						.toArray(new TypeReference[typeArguments.size()]), 0, l);
			}
		} else {
			char[][] compoundName = CharOperation.splitOn('.', name.toCharArray());
			if (typeArguments == null) {
				return new QualifiedTypeReference(compoundName, positionsFor(compoundName, start, end));
			} else {
				// FIXASC (M2) support individual parameterization of component
				// references A<String>.B<Wibble>
				TypeReference[][] typeReferences = new TypeReference[compoundName.length][];
				typeReferences[compoundName.length - 1] = typeArguments.toArray(new TypeReference[typeArguments.size()]);
				return new ParameterizedQualifiedTypeReference(compoundName, typeReferences, 0, positionsFor(compoundName, start,
						end));
			}
		}
	}

	// FIXASC (M2) this is useless - use proper positions
	private long[] getPositionsFor(char[][] compoundName) {
		long[] ls = new long[compoundName.length];
		for (int i = 0; i < compoundName.length; i++) {
			ls[i] = 0;
		}
		return ls;
	}

	// FIXASC (M2) are costly regens being done for all the classes???

	@SuppressWarnings("unchecked")
	@Override
	public void generateCode() {
		boolean successful = processToPhase(Phases.ALL);
		if (successful) {
			// FIXASC (M2:optimize) should make the CompilationUnit smarter to preserve
			// the information we are about to dig for

			// this returns all the classes and we only want those caused by
			// this sourceUnit
			List<GroovyClass> unitGeneratedClasses = groovyCompilationUnit.getClasses();
			List<String> classnamesFromThisSourceUnit = new ArrayList<String>();
			List<ClassNode> classnodesFromSourceUnit = groovySourceUnit.getAST().getClasses();
			for (ClassNode classnode : classnodesFromSourceUnit) {
				classnamesFromThisSourceUnit.add(classnode.getName());
			}
			for (GroovyClass groovyClass : unitGeneratedClasses) {
				String classname = groovyClass.getName();
				String relatedClassName = null;
				if (classnamesFromThisSourceUnit != null) {
					boolean looksRight = false;
					for (String cn : classnamesFromThisSourceUnit) {
						if (classname.equals(cn) || classname.startsWith(cn + "$")) {
							looksRight = true;
							relatedClassName = cn;
						}
					}
					if (!looksRight) {
						continue;
					}
				}
				byte[] classbytes = groovyClass.getBytes();
				String path = groovyClass.getName().replace('.', '/');// File.separatorChar);
				SourceTypeBinding foundBinding = null;
				// FIXASC (M2:optimize) poor way to discover the binding, improve this - we should already know the binding
				if (types != null && types.length != 0) {
					for (TypeDeclaration typeDecl : types) {
						// Going to say that a null binding is because of some other error that prevented it being built
						SourceTypeBinding sourceTypeBinding = typeDecl.binding;
						if (sourceTypeBinding == null) {
							Util.log(new RuntimeException(), "Broken binding (hope code had errors...) for declaration: '"
									+ new String(typeDecl.name) + "'");
						} else {
							String bindingName = CharOperation.toString(sourceTypeBinding.compoundName);
							// FIXASC (M2) it appears 'scripts' have a classname and no
							// package (is that right?) - revisit the second part of
							// this if clause. 'configtest' is an example script
							// from grails that does this
							if (bindingName.equals(relatedClassName) || bindingName.endsWith(relatedClassName)) {
								foundBinding = typeDecl.binding;
							}
						}
					}
				}
				// null foundBinding here will manifest as NPE shortly and means
				// we are looking for a class file unrelated to the current
				// sourceUnit...
				if (foundBinding == null) {
					// FIXASC (M2) surface as an error? example of this problem, see DeclarationTests.groovy in groovyc
					// may be too late to register with the CompilationUnit error collector
					// something like this :
					// this.groovyCompilationUnit.getErrorCollector().addError(
					// new SyntaxErrorMessage(new SyntaxException(
					// "Missing binding: does the package name match the directory?", 0, 0), groovySourceUnit));
					Util.log(new RuntimeException("Missing binding"),
							"Couldn't find binding (Does the package name match the directory for this type?) '" + relatedClassName
									+ "'");
					return;
				}

				compilationResult.record(classname.toCharArray(), new GroovyClassFile(classname, classbytes, foundBinding, path));
			}
		}
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
		// FIXASC (M2) look at this error situation (described below), surely we need to do it?
		// Due to the nature of driving all groovy entities through compilation together, we can accumulate messages for other
		// compilation units whilst processing the one we wanted to. Per GRE396 this can manifest as recording the wrong thing
		// against the wrong type. That is the only case I have seen of it, so I'm not putting in the general mechanism for all
		// errors yet, I'm just dealing with RuntimeParserExceptions. The general strategy would be to compare the ModuleNode
		// for each message with the ModuleNode currently being processed - if they differ then this isn't a message for this
		// unit and so we ignore it. If we do deal with it then we remember that we did (in errorsRecorded) and remove it from
		// the list of those to process.

		List errorsRecorded = new ArrayList();
		// FIXASC (M2) poor way to get the errors attached to the files
		// FIXASC (M2) does groovy ever produce warnings? How are they treated here?
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
			if (message instanceof SimpleMessage) {
				SimpleMessage simpleMessage = (SimpleMessage) message;
				sev |= ProblemSeverities.Error;
				msg = "Groovy:" + simpleMessage.getMessage();
				if (msg.indexOf("\n") != -1) {
					msg = msg.substring(0, msg.indexOf("\n"));
				}
			}
			if (message instanceof SyntaxErrorMessage) {
				SyntaxErrorMessage errorMessage = (SyntaxErrorMessage) message;
				syntaxException = errorMessage.getCause();
				sev |= ProblemSeverities.Error;
				// FIXASC (M2) in the short term, prefixed groovy to indicate
				// where it came from
				msg = "Groovy:" + errorMessage.getCause().getMessage();
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
					eoffset = rpe.getNode().getEnd();
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
				while (compilationResult.lineSeparatorPositions[line] < soffset
						&& line < compilationResult.lineSeparatorPositions.length) {
					line++;
				}
				;
				line++; // from an array index to a real 'line number'
			} else {
				soffset = getOffset(compilationResult.lineSeparatorPositions, line, scol);
				eoffset = getOffset(compilationResult.lineSeparatorPositions, line, ecol);
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
			this.problemReporter.record(p, compilationResult, this);
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
		return new GroovyCompilationUnitScope(this, lookupEnvironment);
	}

	public ModuleNode getModuleNode() {
		return groovySourceUnit == null ? null : groovySourceUnit.getAST();
	}

	public SourceUnit getSourceUnit() {
		return groovySourceUnit;
	}

	/**
	 * Try to get the source locations for type declarations to be as correct as possible
	 */
	private void fixupSourceLocationsForTypeDeclaration(GroovyTypeDeclaration typeDeclaration, ClassNode classNode) {
		// start and end of the name of class
		// scripts do not have a name, so use start instead
		typeDeclaration.sourceStart = Math.max(classNode.getNameStart(), classNode.getStart());
		typeDeclaration.sourceEnd = Math.max(classNode.getNameEnd(), classNode.getStart());

		// start and end of the entire declaration including Javadoc
		// and ending at the last close bracket
		typeDeclaration.declarationSourceStart = classNode.getStart();
		typeDeclaration.declarationSourceEnd = classNode.getEnd();

		// * start at the opening brace and end at the closing brace
		// except that scripts do not have a name, use the start instead
		typeDeclaration.bodyStart = Math.max(classNode.getNameEnd(), classNode.getStart());

		// seems to be the same as declarationSourceEnd
		typeDeclaration.bodyEnd = classNode.getEnd();

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
		ctorDeclaration.declarationSourceStart = ctorNode.getStart();
		ctorDeclaration.declarationSourceEnd = ctorNode.getEnd();

		// start of method's modifier list (after Javadoc is ended)
		ctorDeclaration.modifiersSourceStart = ctorNode.getStart();

		// opening bracket
		ctorDeclaration.bodyStart = ctorNode.getNameEnd();

		// closing bracket or ';' same as declarationSourceEnd
		ctorDeclaration.bodyEnd = ctorNode.getEnd();
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
		methodDeclaration.declarationSourceStart = methodNode.getStart();
		methodDeclaration.declarationSourceEnd = methodNode.getEnd();

		// start of method's modifier list (after Javadoc is ended)
		methodDeclaration.modifiersSourceStart = methodNode.getStart();

		// opening bracket
		// run() method for script has no opening bracket
		methodDeclaration.bodyStart = Math.max(methodNode.getNameEnd(), methodNode.getStart());

		// closing bracket or ';' same as declarationSourceEnd
		methodDeclaration.bodyEnd = methodNode.getEnd();
	}

	/**
	 * Try to get the source locations for field declarations to be as correct as possible
	 */
	private void fixupSourceLocationsForFieldDeclaration(FieldDeclaration fieldDeclaration, FieldNode fieldNode) {
		// TODO (groovy) each area marked with a '*' is only approximate
		// and can be revisited to make more precise

		// Here, we distinguish between the declaration and the fragment
		// e.g.- def x = 9, y = "l"
		// 'x = 9,' and 'y = "l"' are the fragments and 'def x = 9, y = "l"' is the declaration

		// the start and end of the fragment name
		fieldDeclaration.sourceStart = fieldNode.getNameStart();
		fieldDeclaration.sourceEnd = fieldNode.getNameEnd();

		// * start of the declaration (including javadoc?)
		fieldDeclaration.declarationSourceStart = fieldNode.getStart();

		// the end of the fragment including initializer (and trailing ',')
		fieldDeclaration.declarationSourceEnd = fieldNode.getEnd();

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
		fieldDeclaration.endPart2Position = fieldNode.getEnd();
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
	}

	@Override
	public void analyseCode() {
		processToPhase(Phases.CANONICALIZATION);
	}

	@Override
	public void abort(int abortLevel, CategorizedProblem problem) {
		// FIXASC (M2) Auto-generated method stub
		super.abort(abortLevel, problem);
	}

	@Override
	public void checkUnusedImports() {
		// FIXASC (M2) Auto-generated method stub
		super.checkUnusedImports();
	}

	@Override
	public void cleanUp() {
		// FIXASC (M2) Auto-generated method stub
		super.cleanUp();
	}

	@Override
	public CompilationResult compilationResult() {
		// FIXASC (M2) Auto-generated method stub
		return super.compilationResult();
	}

	@Override
	public TypeDeclaration declarationOfType(char[][] typeName) {
		// FIXASC (M2) Auto-generated method stub
		return super.declarationOfType(typeName);
	}

	@Override
	public void finalizeProblems() {
		// FIXASC (M2) Auto-generated method stub
		super.finalizeProblems();
	}

	@Override
	public char[] getFileName() {
		// FIXASC (M2) Auto-generated method stub
		return super.getFileName();
	}

	@Override
	public char[] getMainTypeName() {
		// FIXASC (M2) Auto-generated method stub
		return super.getMainTypeName();
	}

	@Override
	public boolean hasErrors() {
		// FIXASC (M2) Auto-generated method stub
		return super.hasErrors();
	}

	@Override
	public boolean isEmpty() {
		// FIXASC (M2) Auto-generated method stub
		return super.isEmpty();
	}

	@Override
	public boolean isPackageInfo() {
		// FIXASC (M2) Auto-generated method stub
		return super.isPackageInfo();
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		// FIXASC (M2) Auto-generated method stub
		return super.print(indent, output);
	}

	@Override
	public void propagateInnerEmulationForAllLocalTypes() {
		// FIXASC (M2) Auto-generated method stub
		super.propagateInnerEmulationForAllLocalTypes();
	}

	@Override
	public void record(LocalTypeBinding localType) {
		// FIXASC (M2) Auto-generated method stub
		super.record(localType);
	}

	@Override
	public void recordStringLiteral(StringLiteral literal, boolean fromRecovery) {
		// FIXASC (M2) Auto-generated method stub
		super.recordStringLiteral(literal, fromRecovery);
	}

	@Override
	public void recordSuppressWarnings(IrritantSet irritants, Annotation annotation, int scopeStart, int scopeEnd) {
		// FIXASC (M2) Auto-generated method stub
		super.recordSuppressWarnings(irritants, annotation, scopeStart, scopeEnd);
	}

	@Override
	public void tagAsHavingErrors() {
		// FIXASC (M2) Auto-generated method stub
		super.tagAsHavingErrors();
	}

	@Override
	public void traverse(ASTVisitor visitor, CompilationUnitScope unitScope) {
		// FIXASC (M2) Auto-generated method stub
		super.traverse(visitor, unitScope);
	}

	@Override
	public ASTNode concreteStatement() {
		// FIXASC (M2) Auto-generated method stub
		return super.concreteStatement();
	}

	@Override
	public boolean isImplicitThis() {
		// FIXASC (M2) Auto-generated method stub
		return super.isImplicitThis();
	}

	@Override
	public boolean isSuper() {
		// FIXASC (M2) Auto-generated method stub
		return super.isSuper();
	}

	@Override
	public boolean isThis() {
		// FIXASC (M2) Auto-generated method stub
		return super.isThis();
	}

	@Override
	public int sourceEnd() {
		// FIXASC (M2) Auto-generated method stub
		return super.sourceEnd();
	}

	@Override
	public int sourceStart() {
		// FIXASC (M2) Auto-generated method stub
		return super.sourceStart();
	}

	@Override
	public String toString() {
		// FIXASC (M2) Auto-generated method stub
		return super.toString();
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		// FIXASC (M2) Auto-generated method stub
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

}
