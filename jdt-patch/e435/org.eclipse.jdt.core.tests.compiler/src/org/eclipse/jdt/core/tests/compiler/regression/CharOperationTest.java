/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import org.eclipse.jdt.core.compiler.CharOperation;

@SuppressWarnings({ "rawtypes" })
public class CharOperationTest extends AbstractRegressionTest {

public CharOperationTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public static Class testClass() {
	return CharOperationTest.class;
}
public void test001() {
	 char[] array = { 'a' , 'b', 'b', 'c', 'a', 'b', 'c', 'a' };
	 char[] toBeReplaced = { 'b', 'c' };
	 char replacementChar = 'a';
	 int  start = 4;
	 int  end = 8;
	 CharOperation.replace(array, toBeReplaced, replacementChar, start, end);
	 char[] result = { 'a' , 'b', 'b', 'c', 'a', 'a', 'a', 'a' };

	 for (int i = 0, max = array.length; i < max; i++) {
		 assertEquals("Wrong value at " + i, result[i], array[i]);
	 }
}
public void test002() {
	 char[] array = { 'a' , 'b', 'b', 'c', 'a', 'b', 'c', 'a' };
	 char[] toBeReplaced = { 'b', 'c' };
	 char replacementChar = 'a';
	 int  start = 2;
	 int  end = 3;
	 CharOperation.replace(array, toBeReplaced, replacementChar, start, end);
	 char[] result = { 'a' , 'b', 'a', 'c', 'a', 'b', 'c', 'a' };

	 for (int i = 0, max = array.length; i < max; i++) {
		 assertEquals("Wrong value at " + i, result[i], array[i]);
	 }
}
public void test003() {
	 char[] second = { 'a' , 'b', 'b', 'c', 'a', 'b', 'c', 'a' };
	 char[] first = { 'b', 'c', 'a' };
	 int  start = 2;
	 int  end = 5;
	 assertTrue(CharOperation.equals(first, second, start, end, true));
}
public void test004() {
	 char[] second = { 'A' };
	 char[] first = { 'a' };
	 int  start = 0;
	 int  end = 1;
	 assertTrue(CharOperation.equals(first, second, start, end, false));
}
public void test005() {
	 char[] array = { 'a' , 'b', 'b', 'c', 'a', 'b', 'c', 'a' };
	 char[] toBeReplaced = { 'b', 'c' };
	 char replacementChar = 'a';
	 CharOperation.replace(array, toBeReplaced, replacementChar);
	 char[] result = { 'a' , 'a', 'a', 'a', 'a', 'a', 'a', 'a' };

	 for (int i = 0, max = array.length; i < max; i++) {
		 assertEquals("Wrong value at " + i, result[i], array[i]);
	 }
}
public void test006() {
	 char[] array = { 'a' , 'a', 'a', 'a', 'a', 'b', 'c', 'a' };
	 char[] toBeReplaced = { 'a', 'a' };
	 char[] replacementChar = { 'a' };
	 char[] result = CharOperation.replace(array, toBeReplaced, replacementChar);
	 char[] expectedValue = { 'a' , 'a', 'a', 'b', 'c', 'a' };
	 assertEquals("Wrong size", expectedValue.length, result.length);
	 for (int i = 0, max = expectedValue.length; i < max; i++) {
		 assertEquals("Wrong value at " + i, result[i], expectedValue[i]);
	 }
}
// test compareTo(char[], char[])
public void test007() {
	char[] array = { 'a' , 'a', 'a', 'a', 'a', 'b', 'c', 'a' };
	char[] array2 = { 'a', 'a' };
	assertTrue(CharOperation.compareTo(array, array2) > 0);

	 array2 = new char[] { 'a', 'a' };
	 array = new char[] { 'a' , 'a', 'a', 'a', 'a', 'b', 'c', 'a' };
	 assertTrue(CharOperation.compareTo(array2, array) < 0);

	array = new char[] { 'a' , 'a', 'a', 'a', 'a', 'b', 'c', 'a' };
	array2 = new char[] { 'a' , 'a', 'a', 'a', 'a', 'b', 'c', 'a' };
	assertTrue(CharOperation.compareTo(array, array2) == 0);
	assertTrue(CharOperation.compareTo(array2, array) == 0);

	array = new char[] { 'a' , 'b', 'c' };
	array2 = new char[] { 'a' , 'b', 'c', 'a', 'a'};
	assertTrue(CharOperation.compareTo(array, array2) < 0);

	array = new char[] { 'a' , 'b', 'c' };
	array2 = new char[] { 'a' , 'b', 'd'};
	assertTrue(CharOperation.compareTo(array, array2) < 0);
}
// test indexOf case sensitive
public void test008() {
	char[] array = new char[] { 'a' , 'b', 'c' };
	char[] array2 = new char[] { 'a' , 'b', 'c', 'a', 'a'};
	assertTrue(CharOperation.indexOf(array, array2, true, -1) < 0);
}
// test indexOf case insensitive
public void test009() {
	char[] array = new char[] { 'a' , 'b', 'c' };
	char[] array2 = new char[] { 'a' , 'b', 'c', 'a', 'a'};
	assertTrue(CharOperation.indexOf(array, array2, false, -1) < 0);
}
//test new API org.eclipse.jdt.core.compiler.CharOperation.prefixEquals(char[], char[], boolean, int)
public void test010() {
	char[] name = new char[] {  'a' , 'b', 'c', 'a', 'a' };
	char[] prefix = new char[] { 'c', 'a', 'a' };
	assertTrue(CharOperation.prefixEquals(prefix, name, false, 2));
	prefix = new char[] { 'c', 'a', 'a', 'a' };
	assertFalse(CharOperation.prefixEquals(prefix, name, false, 2));
	prefix = new char[] { 'c', 'a', 'A' };
	assertFalse(CharOperation.prefixEquals(prefix, name, true, 2));
	prefix = new char[] { 'b', 'c' };
	assertFalse(CharOperation.prefixEquals(prefix, name, false, 2));
	assertTrue(CharOperation.prefixEquals(prefix, name, false, 1));
}
// test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=324189
public void test011() {
	char[] pattern = new char[] { 'a' };
	char[] name = "AnotherA".toCharArray();
	assertFalse("Should not match", CharOperation.match(
			pattern,
			0,
			1,
			name,
			0,
			8,
			false));
}
// test the javadoc examples
public void test012() {
	assertTrue("Should match", CharOperation.match(
			new char[] { '?', 'b', '*' },
			1,
			3,
			new char[] { 'a', 'b', 'c' , 'd' },
			1,
			4,
			true));
	assertFalse("Should not match", CharOperation.match(
			new char[] { '?', 'b', '*' },
			1,
			2,
			new char[] { 'a', 'b', 'c' , 'd' },
			1,
			4,
			true));
}
}
