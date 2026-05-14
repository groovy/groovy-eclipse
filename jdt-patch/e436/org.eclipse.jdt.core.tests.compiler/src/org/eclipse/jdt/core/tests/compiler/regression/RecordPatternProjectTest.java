/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class RecordPatternProjectTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 21");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testIssue2160" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return RecordPatternProjectTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}
	public RecordPatternProjectTest(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(false);
	}
	protected String[] getDefaultClassPaths() {
		String[] libs = DefaultJavaRuntimeEnvironment.getDefaultClassPaths();
		if (this.extraLibPath != null) {
			String[] l = new String[libs.length + 1];
			System.arraycopy(libs, 0, l, 0, libs.length);
			l[libs.length] = this.extraLibPath;
			return l;
		}
		return libs;
	}
	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(false, options);
		for (INameEnvironment nameEnvironment : classLibs) {
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(false));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE21Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {}, JAVAC_OPTIONS);
	}
	protected void runConformTest(
			String[] testFiles,
			String expectedOutputString,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments) {
			runTest(
		 		// test directory preparation
				shouldFlushOutputDirectory /* should flush output directory */,
				testFiles /* test files */,
				// compiler options
				classLibraries /* class libraries */,
				null /* no custom options */,
				false /* do not perform statements recovery */,
				null /* no custom requestor */,
				// compiler results
				false /* expecting no compiler errors */,
				null /* do not check compiler log */,
				// runtime options
				false /* do not force execution */,
				vmArguments /* vm arguments */,
				// runtime results
				expectedOutputString /* expected output string */,
				null /* do not check error string */,
				// javac options
				JavacTestOptions.DEFAULT /* default javac test options */);
		}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2160
	// VerifyError with code with record patterns and Optional
	public void testIssue2160() {
		runConformTest(new String[] {
				"bug/Interpreter.java",
				"""
				package bug;

				import java.util.Optional;
				import java.util.List;

				import bug.syntax.SyExpressionGetfield;
				import bug.syntax.SyExpressionValue;
				import bug.syntax.SyReferenceConstantSymbol;
				import bug.syntax.SySymbol;
				import bug.syntax.SySymbolField;
				import bug.syntax.SyValue;
				import bug.syntax.SyValueReferenceConstant;
				import bug.syntax.SyDeclClassLit;
				import bug.syntax.SyExpressionValue;
				import bug.syntax.SyIntLit;
				import bug.syntax.SyPrimitiveConstantInt;
				import bug.syntax.SyProgramLit;
				import bug.syntax.SyValuePrimitiveConstant;

				public final class Interpreter {
					public void step(Configuration J) {
						var e = J.syExpression();
						if (e instanceof SyExpressionGetfield syExpressionGetfield) {
							if (syExpressionGetfield instanceof SyExpressionGetfield(SyExpressionValue(SyValueReferenceConstant(SyReferenceConstantSymbol Y)), String f)) {
								final Optional<Objekt> maybeo = J.objectAt(Y);
								maybeo.ifPresentOrElse(o -> {
									//final Optional<SyValue> maybev = o.get(f); //works
									final Optional<SyValue> maybev = o.get(syExpressionGetfield.fieldName()); //does not work
									maybev.ifPresentOrElse(v -> {
										freshSymbol(Y.sySymbol(), f);
									}, () -> {});
								}, () -> {});
							}
						}
					}

					private SySymbolField freshSymbol(SySymbol sySymbol, String fieldName) {
						return null;
					}

					public static void main(String [] args) {
						var C1 = new SyDeclClassLit("Object", "", List.of(), List.of());
						var e = new SyExpressionValue(new SyValuePrimitiveConstant(new SyPrimitiveConstantInt(new SyIntLit(0))));
						var p = new SyProgramLit(List.of(C1), e);
						var J = new Configuration(p);
						var I = new Interpreter();
						I.step(J);
						System.out.println("Finished!");
					}
				}
				""",
				"bug/Configuration.java",
				"""
				package bug;

				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.LinkedHashMap;
				import java.util.List;
				import java.util.Objects;
				import java.util.Optional;

				import bug.syntax.SyExpression;
				import bug.syntax.SyLocLit;
				import bug.syntax.SyProgram;
				import bug.syntax.SyProgramLit;
				import bug.syntax.SyReferenceConstant;
				import bug.syntax.SyReferenceConstantLoc;
				import bug.syntax.SyReferenceConstantSymbol;
				import bug.syntax.SySymbol;
				import bug.syntax.SyValue;
				import bug.syntax.SyValueEq;
				import bug.syntax.SyValueIte;
				import bug.syntax.SyValueReferenceConstant;
				import bug.syntax.SyValueUnassumed;

				public final class Configuration {
					private final SyProgramLit syProgramLit;
					private final LinkedHashMap<SyReferenceConstant, Objekt> heap;
					private final ArrayList<SyValue> pathCondition;
					private SyExpression syExpression;
					private int nextLocId = 0;

					public Configuration(SyProgram syProgram) {
						switch (syProgram) {
							case SyProgramLit syProgramLit:
								this.syProgramLit = syProgramLit;
								break;
							default: throw new AssertionError();
						}
						this.heap = new LinkedHashMap<>();
						this.pathCondition = new ArrayList<>();
						this.syExpression = this.syProgramLit.syExpression();
					}

					public Configuration(Configuration other) {
						this.syProgramLit = other.syProgramLit;
						this.heap = new LinkedHashMap<>();
						for (var e : other.heap.sequencedEntrySet()) {
							this.heap.put(e.getKey(), new Objekt(e.getValue()));
						}
						this.pathCondition = new ArrayList<>(other.pathCondition);
						this.syExpression = other.syExpression;
					}

					public Optional<Objekt> objectAt(SyReferenceConstant syReferenceConstant) {
						Objects.requireNonNull(syReferenceConstant);

						return Optional.ofNullable(this.heap.get(syReferenceConstant));
					}

					public SyReferenceConstantLoc addObjectConcrete(Objekt semObject) {
						Objects.requireNonNull(semObject);

						var nextLoc = new SyReferenceConstantLoc(new SyLocLit(this.nextLocId++));
						this.heap.put(nextLoc, semObject);
						return nextLoc;
					}

					public void addObjectSymbolic(SySymbol sySymbol, Objekt semObject) {
						Objects.requireNonNull(sySymbol);
						Objects.requireNonNull(semObject);

						this.heap.put(new SyReferenceConstantSymbol(sySymbol), semObject);
					}

					public SyProgramLit syProgramLit() {
						return this.syProgramLit;
					}

					public SyExpression syExpression() {
						return this.syExpression;
					}


					public void setSyExpression(SyExpression syExpression) {
						Objects.requireNonNull(syExpression);

						this.syExpression = syExpression;
					}


					public List<SyValue> pathCondition() {
						return Collections.unmodifiableList(this.pathCondition);
					}

					public boolean unresolved(SySymbol sySymbol) {
						Objects.requireNonNull(sySymbol);

						return !this.heap.containsKey(new SyReferenceConstantSymbol(sySymbol));
					}

					public void addPathConditionClause(SyValue syValue) {
						Objects.requireNonNull(syValue);

						this.pathCondition.add(syValue);
					}

					public SyValue assume(SySymbol accessor, String fieldName, SyValue fresh) {
						Objects.requireNonNull(accessor);
						Objects.requireNonNull(fieldName);
						Objects.requireNonNull(fresh);

						SyValue retVal = fresh;
						for (var e : this.heap.sequencedEntrySet()) {
							if (e.getKey() instanceof SyReferenceConstantSymbol syReferenceConstantSymbol &&
							    !accessor.equals(syReferenceConstantSymbol.sySymbol())) {
								var o = e.getValue();
								var v = o.get(fieldName);
								if (v.isPresent() && !(v.get() instanceof SyValueUnassumed)) {
									retVal = new SyValueIte(new SyValueEq(new SyValueReferenceConstant(new SyReferenceConstantSymbol(accessor)), new SyValueReferenceConstant(syReferenceConstantSymbol)), v.get(), retVal);
								}
							}
						}
						return retVal;
					}
				}
				""",

				"bug/Objekt.java",
				"""
				package bug;

				import java.util.LinkedHashMap;
				import java.util.Objects;
				import java.util.Optional;

				import bug.syntax.SyDeclClass;
				import bug.syntax.SyDeclClassLit;
				import bug.syntax.SyDeclVariable;
				import bug.syntax.SyDeclVariableLit;
				import bug.syntax.SyProgram;
				import bug.syntax.SyProgramLit;
				import bug.syntax.SyValue;
				import bug.syntax.SyValueUnassumed;

				public final class Objekt {
					private final SyProgramLit syProgramLit;
					private final LinkedHashMap<String, SyValue> memory;
					private String className;

					public Objekt(SyProgram syProgram, String className, boolean symbolic) {
						Objects.requireNonNull(syProgram);
						Objects.requireNonNull(className);

						switch (syProgram) {
						case SyProgramLit syProgramLit:
							this.syProgramLit = syProgramLit;
							var syDeclClassOptional = syProgramLit.cdecl(className);
							var fields = syDeclClassOptional.map(c -> {
								switch (c) {
								case SyDeclClassLit syDeclClassLit:
									return syDeclClassLit.fields();
								default: throw new AssertionError();
								}
							}).orElseThrow(() -> { throw new RuntimeException("Class " + className + " does not exist."); });
							this.memory = new LinkedHashMap<>();
							this.className = className;
							for (SyDeclVariable syDeclVariable : fields) {
								switch (syDeclVariable) {
								case SyDeclVariableLit syDeclVariableLit:
									this.memory.put(syDeclVariableLit.variableName(), (symbolic ? new SyValueUnassumed() : syDeclVariableLit.syType().ini()));
									break;
								default: throw new AssertionError();
								}
							}
							break;
						default: throw new AssertionError();
						}
					}

					public Objekt(Objekt other) {
						this.syProgramLit = other.syProgramLit;
						this.memory = new LinkedHashMap<>(other.memory);
						this.className = other.className;
					}

					public Optional<SyValue> get(String fieldName) {
						Objects.requireNonNull(fieldName);

						return Optional.ofNullable(this.memory.get(fieldName));
					}

					public void upd(String fieldName, SyValue syValue) {
						Objects.requireNonNull(fieldName);
						Objects.requireNonNull(syValue);

						if (this.memory.containsKey(fieldName)) {
							this.memory.put(fieldName, syValue);
						} else {
							throw new RuntimeException("Tried to modify nonexistent field " + fieldName);
						}
					}

					public void refine(String subclassName) {
						Objects.requireNonNull(subclassName);

						for (SyDeclClass syDeclClass : this.syProgramLit.classes()) {
							switch (syDeclClass) {
							case SyDeclClassLit syDeclClassLit:
								if (this.syProgramLit.isSubclass(syDeclClassLit.className(), this.className) &&
								    this.syProgramLit.isSubclass(subclassName, syDeclClassLit.className()) &&
								    !this.className.equals(syDeclClassLit.className())) {
									for (SyDeclVariable syDeclVariable : syDeclClassLit.fields()) {
										switch (syDeclVariable) {
										case SyDeclVariableLit syDeclVariableLit:
											this.memory.put(syDeclVariableLit.variableName(), new SyValueUnassumed());
											break;
										default: throw new AssertionError();
										}
									}
								}
							default: throw new AssertionError();
							}
						}
					}
				}
				""",
				"bug/syntax/SyBoolFalse.java",
				"""
				package bug.syntax;

				public record SyBoolFalse() implements SyBool {

				}
				""",
				"bug/syntax/SyBool.java",
				"""
				package bug.syntax;

				public sealed interface SyBool permits SyBoolTrue, SyBoolFalse {

				}
				""",
				"bug/syntax/SyBoolTrue.java",
				"""
				package bug.syntax;

				public record SyBoolTrue() implements SyBool {

				}
				""",
				"bug/syntax/SyDeclClass.java",
				"""
				package bug.syntax;

				public sealed interface SyDeclClass permits SyDeclClassLit {

				}
				""",
				"bug/syntax/SyDeclClassLit.java",
				"""
				package bug.syntax;

				import java.util.List;
				import java.util.Objects;
				import java.util.Optional;

				public record SyDeclClassLit(String className, String superclassName, List<SyDeclVariable> fields, List<SyDeclMethod> methods) implements SyDeclClass {
					public SyDeclClassLit {
						Objects.requireNonNull(className);
						Objects.requireNonNull(superclassName);
						Objects.requireNonNull(fields);
						Objects.requireNonNull(methods);
						for (SyDeclVariable field : fields) {
							Objects.requireNonNull(field);
						}
						for (SyDeclMethod method : methods) {
							Objects.requireNonNull(method);
						}
					}

					public boolean hasField(String possibleFieldName) {
						Objects.requireNonNull(possibleFieldName);

						for (SyDeclVariable field : this.fields) {
							switch (field) {
							case SyDeclVariableLit syDeclVariableLit:
								if (possibleFieldName.equals(syDeclVariableLit.variableName())) {
									return true;
								}
								break;
							default: throw new AssertionError();
							}
						}
						return false;
					}

					public boolean hasMethod(String possibleMethodName) {
						Objects.requireNonNull(possibleMethodName);

						for (SyDeclMethod method : this.methods) {
							switch (method) {
							case SyDeclMethodLit syDeclMethodLit:
								if (possibleMethodName.equals(syDeclMethodLit.methodName())) {
									return true;
								}
								break;
							default: throw new AssertionError();
							}
						}
						return false;
					}

					public Optional<SyDeclVariable> fdecl(String possibleFieldName) {
						Objects.requireNonNull(possibleFieldName);

						for (SyDeclVariable field : this.fields) {
							switch (field) {
							case SyDeclVariableLit syDeclVariableLit:
								if (possibleFieldName.equals(syDeclVariableLit.variableName())) {
									return Optional.of(syDeclVariableLit);
								}
								break;
							default: throw new AssertionError();
							}
						}
						return Optional.empty();
					}

					public Optional<SyDeclMethod> mdecl(String possibleMethodName) {
						Objects.requireNonNull(possibleMethodName);

						for (SyDeclMethod method : this.methods) {
							switch (method) {
							case SyDeclMethodLit syDeclMethodLit:
								if (possibleMethodName.equals(syDeclMethodLit.methodName())) {
									return Optional.of(syDeclMethodLit);
								}
								break;
							default: throw new AssertionError();
							}
						}
						return Optional.empty();
					}
				}
				""",
				"bug/syntax/SyDeclMethod.java",
				"""
				package bug.syntax;

				public sealed interface SyDeclMethod permits SyDeclMethodLit {

				}
				""",
				"bug/syntax/SyDeclMethodLit.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyDeclMethodLit(SyType syType, String methodName, SyDeclVariable syDeclVariable, SyExpression syExpression) implements SyDeclMethod {
					public SyDeclMethodLit {
						Objects.requireNonNull(syType);
						Objects.requireNonNull(methodName);
						Objects.requireNonNull(syDeclVariable);
						Objects.requireNonNull(syExpression);
					}
				}
				""",
				"bug/syntax/SyDeclVariable.java",
				"""
				package bug.syntax;

				public sealed interface SyDeclVariable permits SyDeclVariableLit {

				}
				""",
				"bug/syntax/SyDeclVariableLit.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyDeclVariableLit(SyType syType, String variableName) implements SyDeclVariable {
					public SyDeclVariableLit {
						Objects.requireNonNull(syType);
						Objects.requireNonNull(variableName);
					}
				}
				""",
				"bug/syntax/SyExpressionAdd.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionAdd(SyExpression syExpressionFirst, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionAdd {
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionAdd replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
						var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
						return new SyExpressionAdd(syExpressionFirstNew, syExpressionSecondNew);
					}
				}
				""",
				"bug/syntax/SyExpressionAnd.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionAnd(SyExpression syExpressionFirst, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionAnd {
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionAnd replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
						var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
						return new SyExpressionAnd(syExpressionFirstNew, syExpressionSecondNew);
					}
				}
				""",
				"bug/syntax/SyExpressionEq.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionEq(SyExpression syExpressionFirst, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionEq {
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionEq replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
						var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
						return new SyExpressionEq(syExpressionFirstNew, syExpressionSecondNew);
					}
				}
				""",
				"bug/syntax/SyExpressionGetfield.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionGetfield(SyExpression syExpression, String fieldName) implements SyExpression {
					public SyExpressionGetfield {
						Objects.requireNonNull(syExpression);
						Objects.requireNonNull(fieldName);
					}

					@Override
					public SyExpressionGetfield replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionNew = this.syExpression.replace(variableName, syExpression);
						return new SyExpressionGetfield(syExpressionNew, this.fieldName);
					}
				}
				""",
				"bug/syntax/SyExpressionIf.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionIf(SyExpression syExpressionCond, SyExpression syExpressionThen, SyExpression syExpressionElse) implements SyExpression {
					public SyExpressionIf {
						Objects.requireNonNull(syExpressionCond);
						Objects.requireNonNull(syExpressionThen);
						Objects.requireNonNull(syExpressionElse);
					}

					@Override
					public SyExpressionIf replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionCondNew = this.syExpressionCond.replace(variableName, syExpression);
						var syExpressionThenNew = this.syExpressionThen.replace(variableName, syExpression);
						var syExpressionElseNew = this.syExpressionElse.replace(variableName, syExpression);
						return new SyExpressionIf(syExpressionCondNew, syExpressionThenNew, syExpressionElseNew);
					}
				}
				""",
				"bug/syntax/SyExpressionInstanceof.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionInstanceof(SyExpression syExpression, String className) implements SyExpression {
					public SyExpressionInstanceof {
						Objects.requireNonNull(syExpression);
						Objects.requireNonNull(className);
					}

					@Override
					public SyExpressionInstanceof replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionNew = this.syExpression.replace(variableName, syExpression);
						return new SyExpressionInstanceof(syExpressionNew, this.className);
					}
				}
				""",
				"bug/syntax/SyExpressionInvoke.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionInvoke(SyExpression syExpressionFirst, String methodName, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionInvoke {
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(methodName);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionInvoke replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
						var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
						return new SyExpressionInvoke(syExpressionFirstNew, this.methodName, syExpressionSecondNew);
					}
				}
				""",
				"bug/syntax/SyExpression.java",
				"""
				package bug.syntax;

				public sealed interface SyExpression permits SyExpressionVariable, SyExpressionValue, SyExpressionNew,
				SyExpressionGetfield, SyExpressionPutfield, SyExpressionLet, SyExpressionAdd, SyExpressionSub, SyExpressionLt,
				SyExpressionAnd, SyExpressionOr, SyExpressionNot, SyExpressionEq, SyExpressionInstanceof, SyExpressionIf,
				SyExpressionInvoke {
					public SyExpression replace(String variableName, SyExpression syExpression);
				}
				""",
				"bug/syntax/SyExpressionLet.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionLet(String variableName, SyExpression syExpressionFirst, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionLet {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionLet replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						if (this.variableName.equals(variableName)) {
							return this;
						} else {
							var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
							var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
							return new SyExpressionLet(this.variableName, syExpressionFirstNew, syExpressionSecondNew);
						}
					}
				}
				""",
				"bug/syntax/SyExpressionLt.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionLt(SyExpression syExpressionFirst, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionLt {
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionLt replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
						var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
						return new SyExpressionLt(syExpressionFirstNew, syExpressionSecondNew);
					}
				}
				""",
				"bug/syntax/SyExpressionNew.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionNew(String className) implements SyExpression {
					public SyExpressionNew {
						Objects.requireNonNull(className);
					}

					@Override
					public SyExpressionNew replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						return this;
					}
				}
				""",
				"bug/syntax/SyExpressionNot.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionNot(SyExpression syExpression) implements SyExpression {
					public SyExpressionNot {
						Objects.requireNonNull(syExpression);
					}

					@Override
					public SyExpressionNot replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionNew = this.syExpression.replace(variableName, syExpression);
						return new SyExpressionNot(syExpressionNew);
					}
				}
				""",
				"bug/syntax/SyExpressionOr.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionOr(SyExpression syExpressionFirst, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionOr {
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionOr replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
						var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
						return new SyExpressionOr(syExpressionFirstNew, syExpressionSecondNew);
					}
				}
				""",
				"bug/syntax/SyExpressionPutfield.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionPutfield(SyExpression syExpressionFirst, String fieldName, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionPutfield {
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(fieldName);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionPutfield replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
						var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
						return new SyExpressionPutfield(syExpressionFirstNew, this.fieldName, syExpressionSecondNew);
					}
				}
				""",
				"bug/syntax/SyExpressionSub.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionSub(SyExpression syExpressionFirst, SyExpression syExpressionSecond) implements SyExpression {
					public SyExpressionSub {
						Objects.requireNonNull(syExpressionFirst);
						Objects.requireNonNull(syExpressionSecond);
					}

					@Override
					public SyExpressionSub replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						var syExpressionFirstNew = this.syExpressionFirst.replace(variableName, syExpression);
						var syExpressionSecondNew = this.syExpressionSecond.replace(variableName, syExpression);
						return new SyExpressionSub(syExpressionFirstNew, syExpressionSecondNew);
					}
				}
				""",
				"bug/syntax/SyExpressionValue.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionValue(SyValue syValue) implements SyExpression {
					public SyExpressionValue {
						Objects.requireNonNull(syValue);
					}

					@Override
					public SyExpressionValue replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						return this;
					}
				}
				""",
				"bug/syntax/SyExpressionVariable.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyExpressionVariable(String variableName) implements SyExpression {
					public SyExpressionVariable {
						Objects.requireNonNull(variableName);
					}

					@Override
					public SyExpression replace(String variableName, SyExpression syExpression) {
						Objects.requireNonNull(variableName);
						Objects.requireNonNull(syExpression);

						if (this.variableName.equals(variableName)) {
							return syExpression;
						} else {
							return this;
						}
					}
				}
				""",
				"bug/syntax/SyInt.java",
				"""
				package bug.syntax;

				public sealed interface SyInt permits SyIntLit {

				}
				""",
				"bug/syntax/SyIntLit.java",
				"""
				package bug.syntax;

				public record SyIntLit(int value) implements SyInt {

				}
				""",
				"bug/syntax/SyLoc.java",
				"""
				package bug.syntax;

				public sealed interface SyLoc permits SyLocLit {

				}
				""",
				"bug/syntax/SyLocLit.java",
				"""
				package bug.syntax;

				public record SyLocLit(int position) implements SyLoc {

				}
				""",
				"bug/syntax/SyPrimitiveConstantBool.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyPrimitiveConstantBool(SyBool syBool) implements SyPrimitiveConstant {
					public SyPrimitiveConstantBool {
						Objects.requireNonNull(syBool);
					}
				}
				""",
				"bug/syntax/SyPrimitiveConstantInt.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyPrimitiveConstantInt(SyInt syInt) implements SyPrimitiveConstant {
					public SyPrimitiveConstantInt {
						Objects.requireNonNull(syInt);
					}
				}
				""",
				"bug/syntax/SyPrimitiveConstant.java",
				"""
				package bug.syntax;

				public sealed interface SyPrimitiveConstant permits SyPrimitiveConstantBool, SyPrimitiveConstantInt, SyPrimitiveConstantSymbol{

				}
				""",
				"bug/syntax/SyPrimitiveConstantSymbol.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyPrimitiveConstantSymbol(SySymbol sySymbol) implements SyPrimitiveConstant {
					public SyPrimitiveConstantSymbol {
						Objects.requireNonNull(sySymbol);
					}
				}
				""",
				"bug/syntax/SyProgram.java",
				"""
				package bug.syntax;

				public sealed interface SyProgram permits SyProgramLit {

				}
				""",
				"bug/syntax/SyProgramLit.java",
				"""
				package bug.syntax;

				import java.lang.AssertionError;
				import java.util.LinkedHashSet;
				import java.util.List;
				import java.util.Objects;
				import java.util.Optional;
				import java.util.Set;

				public record SyProgramLit(List<SyDeclClass> classes, SyExpression syExpression) implements SyProgram {
					public SyProgramLit {
						Objects.requireNonNull(classes);
						Objects.requireNonNull(syExpression);
						for (SyDeclClass syDeclClass : classes) {
							Objects.requireNonNull(syDeclClass);
						}
					}

					public boolean isSubclass(String possibleSubclassName, String possibleSuperclassName) {
						Objects.requireNonNull(possibleSubclassName);
						Objects.requireNonNull(possibleSuperclassName);

						for (SyDeclClass syDeclClass : this.classes) {
							switch (syDeclClass) {
							case SyDeclClassLit syDeclClassLit:
								if (possibleSubclassName.equals(syDeclClassLit.className())) {
									if (possibleSuperclassName.equals(syDeclClassLit.superclassName())) {
										return true;
									} else {
										return isSubclass(syDeclClassLit.superclassName(), possibleSuperclassName);
									}
								}
								break;
							default: throw new AssertionError();
							}
						}
						return false;
					}

					public boolean hasClass(String possibleClassName) {
						Objects.requireNonNull(possibleClassName);

						for (SyDeclClass syDeclClass : this.classes) {
							switch (syDeclClass) {
							case SyDeclClassLit syDeclClassLit:
								if (possibleClassName.equals(syDeclClassLit.className())) {
									return true;
								}
								break;
							default: throw new AssertionError();
							}
						}
						return false;
					}

					public Optional<SyDeclClass> cdecl(String possibleClassName) {
						Objects.requireNonNull(possibleClassName);

						for (SyDeclClass syDeclClass : this.classes) {
							switch (syDeclClass) {
							case SyDeclClassLit syDeclClassLit:
								if (possibleClassName.equals(syDeclClassLit.className())) {
									return Optional.of(syDeclClassLit);
								}
								break;
							default: throw new AssertionError();
							}
						}
						return Optional.empty();
					}

					public boolean seesMethod(String possibleMethodName, String possibleSubclassName, String possibleSuperclassName) {
						Objects.requireNonNull(possibleMethodName);
						Objects.requireNonNull(possibleSubclassName);
						Objects.requireNonNull(possibleSuperclassName);

						return isSubclass(possibleSubclassName, possibleSuperclassName) &&
						       cdecl(possibleSuperclassName).map(c -> {
									switch (c) {
									case SyDeclClassLit syDeclClassLit:
										return syDeclClassLit.hasMethod(possibleMethodName);
									default: throw new AssertionError();
									}
						       }).orElse(false) &&
						       this.classes.stream().allMatch(c -> {
									switch (c) {
									case SyDeclClassLit syDeclClassLit:
										return possibleSubclassName.equals(syDeclClassLit.className()) ||
										       possibleSuperclassName.equals(syDeclClassLit.className()) ||
										       !isSubclass(syDeclClassLit.className(), possibleSuperclassName) ||
										       !isSubclass(possibleSubclassName, syDeclClassLit.className()) ||
										       !syDeclClassLit.hasMethod(possibleMethodName);
									default: throw new AssertionError();
									}
						       });
					}

					public boolean recvMethod(String possibleMethodName, String possibleSubclassName, String possibleSuperclassName) {
						Objects.requireNonNull(possibleMethodName);
						Objects.requireNonNull(possibleSubclassName);
						Objects.requireNonNull(possibleSuperclassName);

						return (!possibleSubclassName.equals(possibleSuperclassName) &&
						       seesMethod(possibleMethodName, possibleSubclassName, possibleSuperclassName)) ||
						       (possibleSubclassName.equals(possibleSuperclassName) &&
						       cdecl(possibleSubclassName).map(c -> {
				                   switch (c) {
				                   case SyDeclClassLit syDeclClassLit:
				                       return syDeclClassLit.hasMethod(possibleMethodName);
				                   default: throw new AssertionError();
				                   }
				               }).orElse(false));
					}

					public Optional<SyDeclClass> methodProvider(String possibleMethodName, String possibleSubclassName) {
						Objects.requireNonNull(possibleMethodName);
						Objects.requireNonNull(possibleSubclassName);

						for (SyDeclClass syDeclClass : this.classes) {
							switch (syDeclClass) {
							case SyDeclClassLit syDeclClassLit:
								if (recvMethod(possibleMethodName, possibleSubclassName, syDeclClassLit.className())) {
									return Optional.of(syDeclClassLit);
								}
								break;
							default: throw new AssertionError();
							}
						}
						return Optional.empty();
					}

					public Optional<SyDeclClass> classWithField(String possibleFieldName) {
						Objects.requireNonNull(possibleFieldName);

						for (SyDeclClass syDeclClass : this.classes) {
							switch (syDeclClass) {
							case SyDeclClassLit syDeclClassLit:
								if (syDeclClassLit.hasField(possibleFieldName)) {
									return Optional.of(syDeclClassLit);
								}
								break;
							default: throw new AssertionError();
							}
						}
						return Optional.empty();
					}

					public Set<SyDeclClass> implementors(String possibleMethodName) {
						Objects.requireNonNull(possibleMethodName);

						final LinkedHashSet<SyDeclClass> retVal = new LinkedHashSet<>();
						for (SyDeclClass syDeclClass : this.classes) {
							switch (syDeclClass) {
							case SyDeclClassLit syDeclClassLit:
								if (syDeclClassLit.hasMethod(possibleMethodName)) {
									retVal.add(syDeclClassLit);
								}
								break;
							default: throw new AssertionError();
							}
						}
						return retVal;
					}

					public Set<SyDeclClass> overriders(String possibleMethodName, String possibleSuperclassName) {
						Objects.requireNonNull(possibleMethodName);
						Objects.requireNonNull(possibleSuperclassName);

						final LinkedHashSet<SyDeclClass> retVal = new LinkedHashSet<>();
						for (SyDeclClass syDeclClass : this.classes) {
							switch (syDeclClass) {
							case SyDeclClassLit syDeclClassLit:
								if (seesMethod(possibleMethodName, syDeclClassLit.className(), possibleSuperclassName) &&
								    syDeclClassLit.hasMethod(possibleMethodName) &&
								    !possibleSuperclassName.equals(syDeclClassLit.className())) {
									retVal.add(syDeclClassLit);
								}
								break;
							default: throw new AssertionError();
							}
						}
						return retVal;
					}
				}
				""",
				"bug/syntax/SyReferenceConstant.java",
				"""
				package bug.syntax;

				public sealed interface SyReferenceConstant permits SyReferenceConstantNull, SyReferenceConstantLoc, SyReferenceConstantSymbol {

				}
				""",
				"bug/syntax/SyReferenceConstantLoc.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyReferenceConstantLoc(SyLoc syLoc) implements SyReferenceConstant {
					public SyReferenceConstantLoc {
						Objects.requireNonNull(syLoc);
					}
				}
				""",
				"bug/syntax/SyReferenceConstantNull.java",
				"""
				package bug.syntax;

				public record SyReferenceConstantNull() implements SyReferenceConstant {

				}
				""",
				"bug/syntax/SyReferenceConstantSymbol.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyReferenceConstantSymbol(SySymbol sySymbol) implements SyReferenceConstant {
					public SyReferenceConstantSymbol {
						Objects.requireNonNull(sySymbol);
					}
				}
				""",
				"bug/syntax/SySymbolExpression.java",
				"""
				package bug.syntax;

				public record SySymbolExpression(int id) implements SySymbol {

				}
				""",
				"bug/syntax/SySymbolField.java",
				"""
				package bug.syntax;

				import java.util.List;
				import java.util.Objects;

				public record SySymbolField(int id, List<String> fieldNames) implements SySymbol {
					public SySymbolField {
						Objects.requireNonNull(fieldNames);
						for (String fieldName : fieldNames) {
							Objects.requireNonNull(fieldName);
						}
					}
				}
				""",
				"bug/syntax/SySymbol.java",
				"""
				package bug.syntax;

				public sealed interface SySymbol permits SySymbolExpression, SySymbolField {

				}
				""",
				"bug/syntax/SyTypeBool.java",
				"""
				package bug.syntax;

				public record SyTypeBool() implements SyType {
					@Override
					public boolean isReference() {
						return false;
					}

					@Override
					public SyValue ini() {
						return new SyValuePrimitiveConstant(new SyPrimitiveConstantBool(new SyBoolFalse()));
					}
				}
				""",
				"bug/syntax/SyTypeClass.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyTypeClass(String name) implements SyType {
					public SyTypeClass {
						Objects.requireNonNull(name);
					}

					@Override
					public boolean isReference() {
						return true;
					}

					@Override
					public SyValue ini() {
						return new SyValueReferenceConstant(new SyReferenceConstantNull());
					}
				}
				""",
				"bug/syntax/SyTypeInt.java",
				"""
				package bug.syntax;

				public record SyTypeInt() implements SyType {
					@Override
					public boolean isReference() {
						return false;
					}

					@Override
					public SyValue ini() {
						return new SyValuePrimitiveConstant(new SyPrimitiveConstantInt(new SyIntLit(0)));
					}
				}
				""",
				"bug/syntax/SyType.java",
				"""
				package bug.syntax;

				public sealed interface SyType permits SyTypeBool, SyTypeInt, SyTypeClass {
					public default boolean isPrimitive() { return !isReference(); }
					public boolean isReference();
					public SyValue ini();
				}
				""",
				"bug/syntax/SyValueAdd.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueAdd(SyValue syValueFirst, SyValue syValueSecond) implements SyValue {
					public SyValueAdd {
						Objects.requireNonNull(syValueFirst);
						Objects.requireNonNull(syValueSecond);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueAnd.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueAnd(SyValue syValueFirst, SyValue syValueSecond) implements SyValue {
					public SyValueAnd {
						Objects.requireNonNull(syValueFirst);
						Objects.requireNonNull(syValueSecond);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueEq.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueEq(SyValue syValueFirst, SyValue syValueSecond) implements SyValue {
					public SyValueEq {
						Objects.requireNonNull(syValueFirst);
						Objects.requireNonNull(syValueSecond);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueFieldRel.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueFieldRel(SySymbol sySymbolFirst, String fieldName, SySymbol sySymbolSecond) implements SyValue {
					public SyValueFieldRel {
						Objects.requireNonNull(sySymbolFirst);
						Objects.requireNonNull(fieldName);
						Objects.requireNonNull(sySymbolSecond);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueIte.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueIte(SyValue syValueCond, SyValue syValueThen, SyValue syValueElse) implements SyValue {
					public SyValueIte {
						Objects.requireNonNull(syValueCond);
						Objects.requireNonNull(syValueThen);
						Objects.requireNonNull(syValueElse);
					}

					@Override
					public boolean isReference() {
						return this.syValueThen.isReference();
					}
				}
				""",
				"bug/syntax/SyValue.java",
				"""
				package bug.syntax;

				public sealed interface SyValue permits SyValueUnassumed, SyValuePrimitiveConstant, SyValueReferenceConstant,
				SyValueAdd, SyValueSub, SyValueLt, SyValueAnd, SyValueOr, SyValueNot, SyValueEq, SyValueSubtypeRel,
				SyValueFieldRel, SyValueIte {
					public default boolean isPrimitive() { return !isReference(); }
					public boolean isReference();
				}
				""",
				"bug/syntax/SyValueLt.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueLt(SyValue syValueFirst, SyValue syValueSecond) implements SyValue {
					public SyValueLt {
						Objects.requireNonNull(syValueFirst);
						Objects.requireNonNull(syValueSecond);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueNot.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueNot(SyValue syValue) implements SyValue {
					public SyValueNot {
						Objects.requireNonNull(syValue);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueOr.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueOr(SyValue syValueFirst, SyValue syValueSecond) implements SyValue {
					public SyValueOr {
						Objects.requireNonNull(syValueFirst);
						Objects.requireNonNull(syValueSecond);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValuePrimitiveConstant.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValuePrimitiveConstant(SyPrimitiveConstant syPrimitiveConstant) implements SyValue {
					public SyValuePrimitiveConstant {
						Objects.requireNonNull(syPrimitiveConstant);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueReferenceConstant.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueReferenceConstant(SyReferenceConstant syReferenceConstant) implements SyValue {
					public SyValueReferenceConstant {
						Objects.requireNonNull(syReferenceConstant);
					}

					@Override
					public boolean isReference() {
						return true;
					}
				}
				""",
				"bug/syntax/SyValueSub.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueSub(SyValue syValueFirst, SyValue syValueSecond) implements SyValue {
					public SyValueSub {
						Objects.requireNonNull(syValueFirst);
						Objects.requireNonNull(syValueSecond);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueSubtypeRel.java",
				"""
				package bug.syntax;

				import java.util.Objects;

				public record SyValueSubtypeRel(SyValue syValue, SyType syType) implements SyValue {
					public SyValueSubtypeRel {
						Objects.requireNonNull(syValue);
						Objects.requireNonNull(syType);
					}

					@Override
					public boolean isReference() {
						return false;
					}
				}
				""",
				"bug/syntax/SyValueUnassumed.java",
				"""
				package bug.syntax;

				public record SyValueUnassumed() implements SyValue {
					@Override
					public boolean isReference() {
						return false; //imprecise, not meant to be used
					}
				}
				""",
		},
		"Finished!");
	}
}