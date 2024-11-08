/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import java.io.File;
//import junit.framework.AssertionFailedError;
import junit.framework.Test;
//import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.compiler.CharOperation;

@SuppressWarnings({ "rawtypes" })
public class UtilTest extends AbstractRegressionTest {

StringBuilder camelCaseErrors;

public UtilTest(String name) {
	super(name);
}
static {
//	TESTS_RANGE = new int[] { 62, -1 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
/**
 * Assert that a pattern and a name matches or not.
 * If result is invalid then store warning in buffer and display it.
 */
void assertCamelCase(String pattern, String name, boolean match) {
	assertCamelCase(pattern, name, false /* name may have more parts*/, match);
}
/**
 * Assert that a pattern and a name matches or not.
 * If result is invalid then store warning in buffer and display it.
 */
void assertCamelCase(String pattern, String name, boolean prefixMatch, boolean match) {
	boolean camelCase = CharOperation.camelCaseMatch(pattern==null?null:pattern.toCharArray(), name==null?null:name.toCharArray(), prefixMatch);
	if (match != camelCase) {
		StringBuilder line = new StringBuilder("'");
		line.append(name);
		line.append("' SHOULD");
		if (!match) line.append(" NOT");
		line.append(" match pattern '");
		line.append(pattern);
		line.append("', but it DOES");
		if (!camelCase) line.append(" NOT");
		if (this.camelCaseErrors.length() == 0) {
			System.out.println("Invalid results in test "+getName()+":");
		}
		System.out.println("	- "+line);
		this.camelCaseErrors.append('\n');
		this.camelCaseErrors.append(line);
	}
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.camelCaseErrors = new StringBuilder();
}

public boolean checkPathMatch(char[] pattern, char[] path, boolean isCaseSensitive) {

	CharOperation.replace(pattern, '/', File.separatorChar);
	CharOperation.replace(pattern, '\\', File.separatorChar);
	CharOperation.replace(path, '/', File.separatorChar);
	CharOperation.replace(path, '\\', File.separatorChar);

	boolean result = CharOperation.pathMatch(pattern, path, isCaseSensitive, File.separatorChar);

//	boolean antResult = SelectorUtils.matchPath(new String(pattern), new String(path), isCaseSensitive);
//	if (antResult != result) {
//		new AssertionFailedError("WARNING : Ant expectation for patchMatch(\""+new String(pattern)+"\", \""+new String(path)+"\", ...) is: "+antResult).printStackTrace();
//	}

	return result;
}

public void test01() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("X".toCharArray(), "Xyz".toCharArray(), true));
}
public void test02() {

	assertTrue("Pattern matching failure",
		CharOperation.match("X*".toCharArray(), "Xyz".toCharArray(), true));
}
public void test03() {

	assertTrue("Pattern matching failure",
		CharOperation.match("X".toCharArray(), "X".toCharArray(), true));
}
public void test04() {

	assertTrue("Pattern matching failure",
		CharOperation.match("X*X".toCharArray(), "XYX".toCharArray(), true));
}
public void test05() {

	assertTrue("Pattern matching failure",
		CharOperation.match("XY*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test06() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*XY*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test07() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test08() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("a*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test09() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("abc".toCharArray(), "XYZ".toCharArray(), true));
}
public void test10() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("ab*c".toCharArray(), "abX".toCharArray(), true));
}
public void test11() {

	assertTrue("Pattern matching failure",
		CharOperation.match("a*b*c".toCharArray(), "aXXbYYc".toCharArray(), true));
}
public void test12() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("*a*bc".toCharArray(), "aXXbYYc".toCharArray(), true));
}
public void test13() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("*foo*bar".toCharArray(), "".toCharArray(), true));
}
public void test14() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*foo*bar".toCharArray(), "ffoobabar".toCharArray(), true));
}
public void test15() {

	assertTrue("Pattern matching failure",
		!CharOperation.match("*fol*bar".toCharArray(), "ffoobabar".toCharArray(), true));
}
public void test16() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*X*Y*".toCharArray(), "XY".toCharArray(), true));
}
public void test17() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*X*Y*".toCharArray(), "XYZ".toCharArray(), true));
}
public void test18() {

	assertTrue("Pattern matching failure",
		CharOperation.match("main(*)".toCharArray(), "main(java.lang.String[] argv)".toCharArray(), true));
}
public void test19() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*rr*".toCharArray(), "ARRAY".toCharArray(), false));
}

public void test20() {

	assertTrue("Pattern matching failure",
		CharOperation.match("hello*World".toCharArray(), "helloWorld".toCharArray(), true));
}

public void test21() {
	assertEquals("Trim failure", "hello", new String(CharOperation.trim("hello".toCharArray())));
}
public void test22() {
	assertEquals("Trim failure", "hello", new String(CharOperation.trim("   hello".toCharArray())));
}
public void test23() {
	assertEquals("Trim failure", "hello", new String(CharOperation.trim("   hello   ".toCharArray())));
}
public void test24() {
	assertEquals("Trim failure", "hello", new String(CharOperation.trim("hello   ".toCharArray())));
}
public void test25() {
	assertEquals("Trim failure", "", new String(CharOperation.trim("   ".toCharArray())));
}
public void test26() {
	assertEquals("Trim failure", "hello world", new String(CharOperation.trim(" hello world  ".toCharArray())));
}
public void test27() {
	char [][] tokens = CharOperation.splitAndTrimOn(','," hello,world".toCharArray());
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[hello][world]", buffer.toString());
}
public void test28() {
	char [][] tokens = CharOperation.splitAndTrimOn(','," hello , world".toCharArray());
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[hello][world]", buffer.toString());
}
public void test29() {
	char [][] tokens = CharOperation.splitAndTrimOn(','," hello, world   ".toCharArray());
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[hello][world]", buffer.toString());
}
public void test30() {
	char [][] tokens = CharOperation.splitAndTrimOn(','," hello, world   ,zork/, aaa bbb".toCharArray());
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[hello][world][zork/][aaa bbb]", buffer.toString());
}
public void test31() {
	char [][] tokens = CharOperation.splitAndTrimOn(',',"  ,  ".toCharArray());
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[][]", buffer.toString());
}
public void test32() {
	char [][] tokens = CharOperation.splitAndTrimOn(',',"   ".toCharArray());
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[]", buffer.toString());
}
public void test33() {
	char [][] tokens = CharOperation.splitAndTrimOn(',',"  , hello  ".toCharArray());
	StringBuilder buffer = new StringBuilder();
	for (int i = 0; i < tokens.length; i++){
		buffer.append('[').append(tokens[i]).append(']');
	}
	assertEquals("SplitTrim failure", "[][hello]", buffer.toString());
}

public void test34() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/*/World".toCharArray(), "hello/zzz/World".toCharArray(), true));
}

public void test35() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/**/World".toCharArray(), "hello/x/y/z/World".toCharArray(), true));
}

public void test36() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/**/World/**/*.java".toCharArray(), "hello/x/y/z/World/X.java".toCharArray(), true));
}

public void test37() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("**/World/**/*.java".toCharArray(), "hello/x/y/z/World/X.java".toCharArray(), true));
}

public void test38() {

	assertTrue("Path pattern matching failure",
		!checkPathMatch("/*.java".toCharArray(), "/hello/x/y/z/World/X.java".toCharArray(), true));
}

/*
 * From Ant pattern set examples
 */
public void test39() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("**/CVS/*".toCharArray(), "CVS/Repository".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("**/CVS/*".toCharArray(), "org/apache/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		checkPathMatch("**/CVS/*".toCharArray(), "org/apache/jakarta/tools/ant/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-4",
		!checkPathMatch("**/CVS/*".toCharArray(), "org/apache/CVS/foo/bar/Entries".toCharArray(), true));
}

/*
 * From Ant pattern set examples
 */
public void test40() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/jakarta/tools/ant/docs/index.html".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/jakarta/test.xml".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		!checkPathMatch("org/apache/jakarta/**".toCharArray(), "org/apache/xyz.java".toCharArray(), true));
}

/*
 * From Ant pattern set examples
 */
public void test41() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/jakarta/tools/ant/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		!checkPathMatch("org/apache/**/CVS/*".toCharArray(), "org/apache/CVS/foo/bar/Entries".toCharArray(), true));
}

/*
 * From Ant pattern set examples
 */
public void test42() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("**/test/**".toCharArray(), "org/apache/test/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("**/test/**".toCharArray(), "test".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		checkPathMatch("**/test/**".toCharArray(), "a/test".toCharArray(), true));
	assertTrue("Path pattern matching failure-4",
		checkPathMatch("**/test/**".toCharArray(), "test/a.java".toCharArray(), true));
	assertTrue("Path pattern matching failure-5",
		!checkPathMatch("**/test/**".toCharArray(), "org/apache/test.java".toCharArray(), true));
}
/*
 * Corner cases
 */
public void test43() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("/test/".toCharArray(), "/test/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("/test/**".toCharArray(), "/test/CVS/Entries".toCharArray(), true));
}
/*
 * Corner cases
 */
public void test44() {

	assertTrue("Path pattern matching failure-1",
		!checkPathMatch("test".toCharArray(), "test/CVS/Entries".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		!checkPathMatch("**/test".toCharArray(), "test/CVS/Entries".toCharArray(), true));
}
/*
 * Corner cases
 */
public void test45() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("/test/test1/".toCharArray(), "/test/test1/test/test1".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		!checkPathMatch("/test/test1".toCharArray(), "/test/test1/test/test1".toCharArray(), true));
}
public void test46() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/**/World".toCharArray(), "hello/World".toCharArray(), true));
}
/*
 * Regression test for 28316 Missing references to constructor
 */
public void test47() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*x".toCharArray(), "x.X".toCharArray(), false));
}
public void test48() {

	assertTrue("Pattern matching failure",
		CharOperation.match("*a*".toCharArray(), "abcd".toCharArray(), false));
}
public void test49() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("**/hello".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test50() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("**/hello/**".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test51() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("**/hello/".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test52() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("hello/".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test53() {

	assertTrue("Path pattern matching failure",
		!checkPathMatch("/".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test54() {

	assertTrue("Path pattern matching failure-1",
		!checkPathMatch("x/".toCharArray(), "hello/x".toCharArray(), true)); // 29761

	assertTrue("Path pattern matching failure-2",
		checkPathMatch("**/x/".toCharArray(), "hello/x".toCharArray(), true));

	assertTrue("Path pattern matching failure-3",
		!checkPathMatch("/x/".toCharArray(), "hello/x".toCharArray(), true));
}
public void test56() {

	assertTrue("Path pattern matching failure",
		!checkPathMatch("/**".toCharArray(), "hello/hello".toCharArray(), true));
}
public void test57() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("/".toCharArray(), "/hello/hello".toCharArray(), true));
}
public void test58() {

	assertTrue("Path pattern matching failure",
		checkPathMatch("/**".toCharArray(), "/hello/hello".toCharArray(), true));
}
public void test59() {

	assertTrue("Path pattern matching failure",
		!checkPathMatch("**".toCharArray(), "/hello/hello".toCharArray(), true));
}
public void test60() {

	assertTrue("Path pattern matching failure-1",
		!checkPathMatch("/P/src".toCharArray(), "/P/src/X".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		!checkPathMatch("/P/**/src".toCharArray(), "/P/src/X".toCharArray(), true));
	assertTrue("Path pattern matching failure-3",
		checkPathMatch("/P/src".toCharArray(), "/P/src".toCharArray(), true));
	assertTrue("Path pattern matching failure-4",
		!checkPathMatch("A.java".toCharArray(), "/P/src/A.java".toCharArray(), true));
}
public void test61() {

	assertTrue("Path pattern matching failure-1",
		checkPathMatch("/P/src/**/CVS".toCharArray(), "/P/src/CVS".toCharArray(), true));
	assertTrue("Path pattern matching failure-2",
		checkPathMatch("/P/src/**/CVS/".toCharArray(), "/P/src/CVS".toCharArray(), true));
}
public void test62() {
	assertCamelCase("NPE", "NullPointerException", true/* should match */);
	assertCamelCase("NPExc", "NullPointerException", true/* should match */);
	assertCamelCase("NPoE", "NullPointerException", true/* should match */);
	assertCamelCase("NuPExc", "NullPointerException", true/* should match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
public void test63() {
	assertCamelCase("NPEX", "NullPointerException", false/* should not match */);
	assertCamelCase("NPex", "NullPointerException", false/* should not match */);
	assertCamelCase("npe", "NullPointerException", false/* should not match */);
	assertCamelCase("npe", "NPException", false/* should not match */);
	assertCamelCase("NPointerE", "NullPointerException", true/* should match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
public void test64() {
	assertCamelCase("IAE", "IgnoreAllErrorHandler", true/* should match */);
	assertCamelCase("IAE", "IAnchorElement", true/* should match */);
	assertCamelCase("IAnchorEleme", "IAnchorElement", true/* should match */);
	assertCamelCase("", "IAnchorElement", false/* should not match */);
	assertCamelCase(null, "IAnchorElement", true/* should match */);
	assertCamelCase("", "", true/* should match */);
	assertCamelCase("IAnchor", null, false/* should not match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
public void test65() {
	assertCamelCase("iSCDCo", "invokeStringConcatenationDefaultConstructor", true/* should match */);
	assertCamelCase("inVOke", "invokeStringConcatenationDefaultConstructor", false/* should not match */);
	assertCamelCase("i", "invokeStringConcatenationDefaultConstructor", true/* should match */);
	assertCamelCase("I", "invokeStringConcatenationDefaultConstructor", false/* should not match */);
	assertCamelCase("iStringCD", "invokeStringConcatenationDefaultConstructor", true/* should match */);
	assertCamelCase("NPE", "NullPointerException/java.lang", true/* should match */);
	assertCamelCase("NPE", "NullPointer/lang.Exception", false/* should not match */);
	assertCamelCase("NPE", "Null_Pointer$Exception", true/* should match */);
	assertCamelCase("NPE", "Null1Pointer2Exception", true/* should match */);
	assertCamelCase("NPE", "Null.Pointer.Exception", false/* should not match */);
	assertCamelCase("NPE", "aNullPointerException", false/* should not match */);
	assertCamelCase("nullP", "nullPointerException", true/* should match */);
	assertCamelCase("nP", "nullPointerException", true/* should match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}

/**
 * Bug 130390: CamelCase algorithm cleanup and improvement
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=130390"
 */
public void test66() {
    String[][] MATCHES = {
            {"TZ","TimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"TiZ","TimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"TiZon","TimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"TZon","TimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"TZone","TimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"TimeZone","TimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"TimeZ","TimeZ"},  //$NON-NLS-1$//$NON-NLS-2$
            {"TZ","TimeZ"},  //$NON-NLS-1$//$NON-NLS-2$
            {"T","TimeZ"},  //$NON-NLS-1$//$NON-NLS-2$
            {"T","TimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"TZ","TZ"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aT","aTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aTi","aTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aTiZ","aTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aTZ","aTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aT","artTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aTi","artTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aTiZ","artTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aTZ","artTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
    };

    for (int i = 0; i<MATCHES.length ; i++) {
        String[] match = MATCHES[i];
        assertCamelCase(match[0], match[1], true/*should match*/);
    }

    String[][] MIS_MATCHES = {
            {"TZ","Timezone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aTZ","TimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aTZ","TZ"},  //$NON-NLS-1$//$NON-NLS-2$
            {"arT","aTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"arTi","aTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"arTiZ","aTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"arTZ","aTimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
            {"aT","atimeZone"},  //$NON-NLS-1$//$NON-NLS-2$
    };

    for (int i = 0; i<MIS_MATCHES.length ; i++) {
        String[] match = MIS_MATCHES[i];
        assertCamelCase(match[0], match[1], false/*should not match*/);
    }

	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}

/**
 * Bug 137087: Open Type - missing matches when using mixed case pattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=137087"
 */
public void test67() {
	assertCamelCase("runtimeEx", "RuntimeException", false/* should not match */);
	assertCamelCase("Runtimeex", "RuntimeException", false/* should not match */);
	assertCamelCase("runtimeexception", "RuntimeException", false/* should not match */);
	assertCamelCase("Runtimexception", "RuntimeException", false/* should not match */);
	assertCamelCase("illegalMSException", "IllegalMonitorStateException", false/* should not match */);
	assertCamelCase("illegalMsException", "IllegalMonitorStateException", false /* should not match */);
	assertCamelCase("IllegalMSException", "IllegalMonitorStateException", true/* should match */);
	assertCamelCase("IllegalMsException", "IllegalMonitorStateException", false /* should not match */);
	assertCamelCase("clonenotsupportedex", "CloneNotSupportedException", false/* should not match */);
	assertCamelCase("CloneNotSupportedEx", "CloneNotSupportedException", true/* should match */);
	assertCamelCase("cloneNotsupportedEx", "CloneNotSupportedException", false/* should not match */);
	assertCamelCase("ClonenotSupportedexc", "CloneNotSupportedException", false/* should not match */);
	assertCamelCase("cloneNotSupportedExcep", "CloneNotSupportedException", false/* should not match */);
	assertCamelCase("Clonenotsupportedexception", "CloneNotSupportedException", false/* should not match */);
	assertCamelCase("CloneNotSupportedException", "CloneNotSupportedException", true /* should match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
// lower CamelCase
public void test68() {
	assertCamelCase("aMe", "aMethod", true/* should match */);
	assertCamelCase("ame", "aMethod", false/* should not match */);
	assertCamelCase("longNOM", "longNameOfMethod", true/* should match */);
	assertCamelCase("longNOMeth", "longNameOfMethod", true/* should match */);
	assertCamelCase("longNOMethod", "longNameOfMethod", true/* should match */);
	assertCamelCase("longNoMethod", "longNameOfMethod", false/* should not match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
// search tests
public void test69() {
	assertCamelCase("aa", "AxxAyy", false /* should not match */);
	assertCamelCase("Aa", "AxxAyy", false /* should not match */);
	assertCamelCase("aA", "AxxAyy", false /* should not match */);
	assertCamelCase("AA", "AxxAyy", true /* should match */);
	assertCamelCase("aa", "AbcdAbcdefAbcAbcdefghAbAAzzzzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnAbcAbcdefghijklm", false /* should not match */);
	assertCamelCase("AA", "AbcdAbcdefAbcAbcdefghAbAAzzzzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnAbcAbcdefghijklm", true /* should match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}

// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=109695
public void test70() throws CoreException {
	assertCamelCase("IDE3", "IDocumentExtension", true /*same part count*/, false /* should not match */);
	assertCamelCase("IDE3", "IDocumentExtension2", true /*same part count*/, false /* should not match */);
	assertCamelCase("IDE3", "IDocumentExtension3", true /*same part count*/, true /* should match */);
	assertCamelCase("IDE3", "IDocumentExtension135", true /*same part count*/, true /* should match */);
	assertCamelCase("IDE3", "IDocumentExtension315", true /*same part count*/, true /* should match */);
	assertCamelCase("IDPE3", "IDocumentProviderExtension", true /*same part count*/, false /* should not match */);
	assertCamelCase("IDPE3", "IDocumentProviderExtension2", true /*same part count*/, false /* should not match */);
	assertCamelCase("IDPE3", "IDocumentProviderExtension4", true /*same part count*/, false /* should not match */);
	assertCamelCase("IDPE3", "IDocumentProviderExtension3", true /*same part count*/, true /* should match */);
	assertCamelCase("IDPE3", "IDocumentProviderExtension5", true /*same part count*/, false /* should not match */);
	assertCamelCase("IDPE3", "IDocumentProviderExtension54321", true /*same part count*/, true /* should match */);
	assertCamelCase("IDPE3", "IDocumentProviderExtension12345", true /*same part count*/, true /* should match */);
	assertCamelCase("IPL3", "IPerspectiveListener", true /*same part count*/, false /* should not match */);
	assertCamelCase("IPL3", "IPerspectiveListener2", true /*same part count*/, false /* should not match */);
	assertCamelCase("IPL3", "IPerspectiveListener3", true /*same part count*/, true /* should match */);
	assertCamelCase("IPS2", "IPropertySource", true /*same part count*/, false /* should not match */);
	assertCamelCase("IPS2", "IPropertySource2", true /*same part count*/, true /* should match */);
	assertCamelCase("IWWPD2", "IWorkbenchWindowPulldownDelegate", true /*same part count*/, false /* should not match */);
	assertCamelCase("IWWPD2", "IWorkbenchWindowPulldownDelegate2", true /*same part count*/, true /* should match */);
	assertCamelCase("UTF16DSS", "UTF16DocumentScannerSupport", true /*same part count*/, true /* should match */);
	assertCamelCase("UTF16DSS", "UTF1DocScannerSupport", true /*same part count*/, false /* should not match */);
	assertCamelCase("UTF16DSS", "UTF6DocScannerSupport", true /*same part count*/, false /* should not match */);
	assertCamelCase("UTF16DSS", "UTFDocScannerSupport", true /*same part count*/, false /* should not match */);
	assertCamelCase("UTF1DSS", "UTF16DocumentScannerSupport", true /*same part count*/, true /* should match */);
	assertCamelCase("UTF1DSS", "UTF1DocScannerSupport", true /*same part count*/, true /* should match */);
	assertCamelCase("UTF1DSS", "UTF6DocScannerSupport", true /*same part count*/, false /* should not match */);
	assertCamelCase("UTF1DSS", "UTFDocScannerSupport", true /*same part count*/, false /* should not match */);
	assertCamelCase("UTF6DSS", "UTF16DocumentScannerSupport", true /*same part count*/, true /* should match */);
	assertCamelCase("UTF6DSS", "UTF1DocScannerSupport", true /*same part count*/, false /* should not match */);
	assertCamelCase("UTF6DSS", "UTF6DocScannerSupport", true /*same part count*/, true /* should match */);
	assertCamelCase("UTF6DSS", "UTFDocScannerSupport", true /*same part count*/, false /* should not match */);
	assertCamelCase("UTFDSS", "UTF16DocumentScannerSupport", true /*same part count*/, true /* should match */);
	assertCamelCase("UTFDSS", "UTF1DocScannerSupport", true /*same part count*/, true /* should match */);
	assertCamelCase("UTFDSS", "UTF6DocScannerSupport", true /*same part count*/, true /* should match */);
	assertCamelCase("UTFDSS", "UTFDocScannerSupport", true /*same part count*/, true /* should match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=124624
public void test71() {
	assertCamelCase("HM", "HashMap", true /*same count of parts expected*/, true /*should match*/);
	assertCamelCase("HM", "HtmlMapper", true /*same count of parts expected*/, true /*should match*/);
	assertCamelCase("HM", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HaM", "HashMap", true /*same count of parts expected*/, true /* should match */);
	assertCamelCase("HaM", "HtmlMapper", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HaM", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HashM", "HashMap", true /*same count of parts expected*/, true /* should match */);
	assertCamelCase("HashM", "HtmlMapper", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HashM", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
public void test71b() { // previous test cases but with 3.3 behavior
	assertCamelCase("HM", "HashMap", true /*should match*/);
	assertCamelCase("HM", "HtmlMapper", true /*should match*/);
	assertCamelCase("HM", "HashMapEntry", true /*should match*/);
	assertCamelCase("HaM", "HashMap", true /* should match */);
	assertCamelCase("HaM", "HtmlMapper", false /*should not match*/);
	assertCamelCase("HaM", "HashMapEntry", true /*should match*/);
	assertCamelCase("HashM", "HashMap", true /* should match */);
	assertCamelCase("HashM", "HtmlMapper", false /*should not match*/);
	assertCamelCase("HashM", "HashMapEntry", true /*should match*/);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=124624
public void test72() {
	assertCamelCase("HMa", "HashMap", true /*same count of parts expected*/, true /* should match */);
	assertCamelCase("HMa", "HtmlMapper", true /*same count of parts expected*/, true /* should match */);
	assertCamelCase("HMa", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HaMa", "HashMap", true /*same count of parts expected*/, true /* should match */);
	assertCamelCase("HaMa", "HtmlMapper", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HaMa", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HashMa", "HashMap", true /*same count of parts expected*/, true /* should match */);
	assertCamelCase("HashMa", "HtmlMapper", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HashMa", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
public void test72b() { // previous test cases but with 3.3 behavior
	assertCamelCase("HMa", "HashMap", true /*should match*/);
	assertCamelCase("HMa", "HtmlMapper", true /*should match*/);
	assertCamelCase("HMa", "HashMapEntry", true /*should match*/);
	assertCamelCase("HaMa", "HashMap", true /* should match */);
	assertCamelCase("HaMa", "HtmlMapper", false /*should not match*/);
	assertCamelCase("HaMa", "HashMapEntry", true /*should match*/);
	assertCamelCase("HashMa", "HashMap", true /* should match */);
	assertCamelCase("HashMa", "HtmlMapper", false /*should not match*/);
	assertCamelCase("HashMa", "HashMapEntry", true /*should match*/);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=124624
public void test73() {
	assertCamelCase("HMap", "HashMap", true /*same count of parts expected*/, true /*should match*/);
	assertCamelCase("HMap", "HtmlMapper", true /*same count of parts expected*/, true /* should not match */);
	assertCamelCase("HMap", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HaMap", "HashMap", true /*same count of parts expected*/, true /* should match */);
	assertCamelCase("HaMap", "HtmlMapper", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HaMap", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HashMap", "HashMap", true /*same count of parts expected*/, true /* should match */);
	assertCamelCase("HashMap", "HtmlMapper", true /*same count of parts expected*/, false /* should not match */);
	assertCamelCase("HashMap", "HashMapEntry", true /*same count of parts expected*/, false /* should not match */);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
public void test73b() { // previous test cases but with 3.3 behavior
	assertCamelCase("HMap", "HashMap", true /*should match*/);
	assertCamelCase("HMap", "HtmlMapper", true /*should match*/);
	assertCamelCase("HMap", "HashMapEntry", true /*should match*/);
	assertCamelCase("HaMap", "HashMap", true /* should match */);
	assertCamelCase("HaMap", "HtmlMapper", false /*should not match*/);
	assertCamelCase("HaMap", "HashMapEntry", true /*should match*/);
	assertCamelCase("HashMap", "HashMap", true /* should match */);
	assertCamelCase("HashMap", "HtmlMapper", false /*should not match*/);
	assertCamelCase("HashMap", "HashMapEntry", true /*should match*/);
	// Verify that there were no unexpected results
    assertTrue(this.camelCaseErrors.toString(), this.camelCaseErrors.length()==0);
}
public static Class testClass() {
	return UtilTest.class;
}
}
