/*******************************************************************************
 * Copyright (c) 2024 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.DubiousOutcome;

/**
 * <b>README:</b> this class captures the actual outcome of examples where we doubt if the outcome is correct,
 * (e.g., because javac behaves differently). Still the current outcome is expected in tests, in order
 * to alert us when their behavior changes due to some other fix. If such change occurs, we should decide:
 * <ol>
 * <li>if the new outcome is worse, try to improve the code change
 * <li>if the new outcome is equally dubious, just change the test expectation
 * <li>if the new outcome is good, thus removing the doubt, then move the test to a 'regular' suite class
 * </ol>
 */
public class DubiousOutcomeTest extends AbstractRegressionTest {

	static {
//		TESTS_NAMES = new String[] { "testGH1591" };
//		TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}

	public DubiousOutcomeTest(String name) {
		super(name);
	}
	public static Class<?> testClass() {
		return DubiousOutcomeTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public void testGH1591() {
		// javac accepts
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Outer.java",
				"""
				import java.io.Serializable;
				import java.util.List;
				import java.util.function.Supplier;

				public class Outer {
					public void test() {
						Supplier<? extends List<? extends Serializable>> supplier = () -> null;
						error(supplier.get(), "");
					}

					public <T, V extends Serializable> void error(List<V> v2, T t) {}

					}
				"""
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in Outer.java (at line 8)\n" +
			"	error(supplier.get(), \"\");\n" +
			"	^^^^^\n" +
			"The method error(List<V>, T) in the type Outer is not applicable for the arguments (capture#1-of ? extends List<? extends Serializable>, String)\n" +
			"----------\n";
		runner.javacTestOptions = DubiousOutcome.EclipseErrorsJavacNone;
		runner.runNegativeTest();
	}

	public void testHohwille_20160104() {
		// see https://github.com/m-m-m/util/issues/166#issuecomment-168652351
		/* javac:
		 	CombinedInterface.java:9: error: reference to setValue is ambiguous
					setValue(Boolean.valueOf(value));
					^
			  both method setValue(Boolean) in TypedInterface and method setValue(V) in GenericInterface match
			  where V is a type-variable:
			    V extends Object declared in interface GenericInterface
			1 error
		 */
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"CombinedInterface.java",
				"""
				interface GenericInterface<V> {
					void setValue(V value);
				}
				interface TypedInterface {
					void setValue(Boolean value);
				}
				public interface CombinedInterface extends GenericInterface<Boolean>, TypedInterface {
					default void set(boolean value) {
						setValue(Boolean.valueOf(value));
					}
				}
				"""
			};
		runner.javacTestOptions = DubiousOutcome.JavacErrorsEclipseNone;
		runner.runConformTest();
	}

	public void testHohwille_20180606() {
		/* see https://github.com/m-m-m/util/issues/166#issuecomment-395133804
		  javac:
			GenericTest.java:3: error: type argument ? super B is not within bounds of type-variable B
				private final GenericTest<? super A, ? super B> parent;
				                                     ^
			  where B,A are type-variables:
			    B extends A declared in class GenericTest
			    A extends Object declared in class GenericTest
			GenericTest.java:5: error: type argument ? super B is not within bounds of type-variable B
				public GenericTest(GenericTest<? super A, ? super B> parent) {
				                                          ^
			  where B,A are type-variables:
			    B extends A declared in class GenericTest
			    A extends Object declared in class GenericTest
			2 errors
		 */
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"GenericTest.java",
				"""
				public class GenericTest<A, B extends A> {

					private final GenericTest<? super A, ? super B> parent;

					public GenericTest(GenericTest<? super A, ? super B> parent) {
						super();
						this.parent = parent;
					}
				}
				"""
			};
		runner.javacTestOptions = DubiousOutcome.JavacErrorsEclipseNone;
		runner.runConformTest();
	}
	public void testHohwille_20231104() {
		/* see https://github.com/m-m-m/util/issues/166#issuecomment-1793234294
		   and https://bugs.openjdk.org/browse/JDK-8319461
		   My reduction
		javac:
			PropertyFactoryManager.java:15: error: incompatible types: inference variable P#1 has incompatible bounds
					MyPropertyFactory factory = getRequiredFactory(propertyType, valueClass);
					                                              ^
			    equality constraints: P#2
			    upper bounds: WritableProperty<V#1>,ReadableProperty<V#1>
			  where P#1,V#1,P#2,V#2 are type-variables:
			    P#1 extends ReadableProperty<V#1> declared in method <V#1,P#1>getRequiredFactory(Class<P#1>,Class<V#1>)
			    V#1 extends Object declared in method <V#1,P#1>getRequiredFactory(Class<P#1>,Class<V#1>)
			    P#2 extends ReadableProperty<V#2> declared in method <V#2,P#2>create(Class<P#2>,Class<V#2>)
			    V#2 extends Object declared in method <V#2,P#2>create(Class<P#2>,Class<V#2>)
			PropertyFactoryManager.java:21: error: incompatible types: inference variable P#1 has incompatible bounds
					MyPropertyFactory factory = getRequiredFactory(propertyType, typeInfo.getValueClass());
					                                              ^
			    equality constraints: P#2
			    upper bounds: WritableProperty<V#1>,ReadableProperty<V#1>
			  where P#1,V#1,P#2,V#2 are type-variables:
			    P#1 extends ReadableProperty<V#1> declared in method <V#1,P#1>getRequiredFactory(Class<P#1>,Class<V#1>)
			    V#1 extends Object declared in method <V#1,P#1>getRequiredFactory(Class<P#1>,Class<V#1>)
			    P#2 extends ReadableProperty<V#2> declared in method <V#2,P#2>create(Class<P#2>,PropertyTypeInfo<V#2>)
			    V#2 extends Object declared in method <V#2,P#2>create(Class<P#2>,PropertyTypeInfo<V#2>)
			2 errors
		 */
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"PropertyFactoryManager.java",
				"""
				interface WritableObservableValue<V> { }
				interface ReadableProperty<V> { }
				interface WritableProperty<V> extends WritableObservableValue<V>, ReadableProperty<V> { }
				interface PropertyTypeInfo<V> {
					Class<V> getValueClass();
				}
				interface MyPropertyFactory<V, P extends WritableProperty<V>> {
					<P2 extends ReadableProperty<V>> P2 create(PropertyTypeInfo<V> typeInfo);
				}

				public interface PropertyFactoryManager {
					@SuppressWarnings({ "rawtypes" })
					default <V, P extends ReadableProperty<V>> MyPropertyFactory create(Class<P> propertyType, Class<V> valueClass) {
						// https://github.com/m-m-m/util/issues/166
						MyPropertyFactory factory = getRequiredFactory(propertyType, valueClass);
						return factory;
					}
					@SuppressWarnings({ "rawtypes", "unchecked" })
					default <V, P extends ReadableProperty<V>> P create(Class<P> propertyType, PropertyTypeInfo<V> typeInfo) {
						// https://github.com/m-m-m/util/issues/166
						MyPropertyFactory factory = getRequiredFactory(propertyType, typeInfo.getValueClass());
						return (P) factory.create(typeInfo);
					}

					default <V, P extends ReadableProperty<V>> MyPropertyFactory<V, ? extends P> getRequiredFactory(
							Class<P> propertyType, Class<V> valueType) {
						return null;
					}
				}
				"""
			};
		runner.javacTestOptions = DubiousOutcome.JDK8319461;
		runner.runConformTest();
	}
	public void testJDK8319461() {
		/* Hohwille's reduction of the above
			javac (the warning is irrelevant, could be easily avoided:
			JDK8319461.java:3: warning: [rawtypes] found raw type: Factory
					Factory factory = getFactory(propertyType, valueClass);
					^
			  missing type arguments for generic class Factory<V,P>
			  where V,P are type-variables:
			    V extends Object declared in interface Factory
			    P extends WritableProperty<V> declared in interface Factory
			JDK8319461.java:3: error: incompatible types: inference variable P#1 has incompatible bounds
					Factory factory = getFactory(propertyType, valueClass);
					                            ^
			    equality constraints: P#2
			    upper bounds: WritableProperty<V#1>,ReadableProperty<V#1>
			  where P#1,V#1,P#2,V#2 are type-variables:
			    P#1 extends ReadableProperty<V#1> declared in method <V#1,P#1>getFactory(Class<P#1>,Class<V#1>)
			    V#1 extends Object declared in method <V#1,P#1>getFactory(Class<P#1>,Class<V#1>)
			    P#2 extends ReadableProperty<V#2> declared in method <V#2,P#2>create(Class<P#2>,Class<V#2>,String)
			    V#2 extends Object declared in method <V#2,P#2>create(Class<P#2>,Class<V#2>,String)
			1 error
			1 warning
		 */
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"JDK8319461.java",
				"""
				public class JDK8319461 {
					public <V, P extends ReadableProperty<V>> P create(Class<P> propertyType, Class<V> valueClass, String name) {
						Factory factory = getFactory(propertyType, valueClass);
						return null;
					}
					public <V, P extends ReadableProperty<V>> Factory<V, ? extends P> getFactory(Class<P> propertyType, Class<V> valueType) {
						Factory<V, ? extends P> factory = null;
						return factory;
					}
					public interface ReadableProperty<V> { }
					public interface WritableProperty<V> extends ReadableProperty<V> { }
					public interface Factory<V, P extends WritableProperty<V>> { }
				}
				"""
			};
		runner.javacTestOptions = DubiousOutcome.JDK8319461;
		runner.runConformTest();
	}
}
