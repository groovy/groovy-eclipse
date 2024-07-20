/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabrice Matrat - initial contribution
 *     IBM Corporation - code review and integration
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.jdt.internal.compiler.util.ManifestAnalyzer;

@SuppressWarnings({ "rawtypes" })
public class ManifestAnalyzerTest extends AbstractRegressionTest {

	private static final String FIRST_JAR = "firstJar.jar";
	private static final String SECOND_JAR = "secondJar.jar";
	private static final String WHITESPACE = " ";

	ManifestAnalyzer manifestAnalyzer = new ManifestAnalyzer();

	public ManifestAnalyzerTest(String name) {
		super(name);
	}
	public void testWithOneJar() throws IOException {
		String testWithOneJar = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + "\nBuild-Reference: Version toto";
		analyzeManifestContents(testWithOneJar);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 1, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
	}

	private void analyzeManifestContents(String contents) throws IOException {
		try (InputStream stream = new ByteArrayInputStream(contents.getBytes())) {
			this.manifestAnalyzer.analyzeManifestContents(stream);
		}
	}
	public void testWithOneJarWithWiteSpace() throws IOException {
		String testWithOneJarWithWiteSpace = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + WHITESPACE + "\nBuild-Reference: Version toto";
		analyzeManifestContents(testWithOneJarWithWiteSpace);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 1, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
	}

	public void testWithSecondJarOnNextLine() throws IOException {
		String testWithSecondJarOnNextLine = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + "\n"+ WHITESPACE + WHITESPACE +"secondJar.jar\nBuild-Reference: Version toto";
		analyzeManifestContents(testWithSecondJarOnNextLine);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 2, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
		assertEquals(SECOND_JAR, jars.get(1));
	}

	public void testWithSecondJarOnTwoLine() throws IOException {
		String testWithSecondJarOnTwoLine = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + WHITESPACE + "second\n" + WHITESPACE + "Jar.jar\nBuild-Reference: Version toto";
		analyzeManifestContents(testWithSecondJarOnTwoLine);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 2, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
		assertEquals(SECOND_JAR, jars.get(1));
	}

	public void testWithSecondJarOnTwoLine2() throws IOException {
		String testWithSecondJarOnTwoLine = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + WHITESPACE + "second\n" + WHITESPACE + WHITESPACE + "Jar.jar\nBuild-Reference: Version toto";
		analyzeManifestContents(testWithSecondJarOnTwoLine);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 3, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
		assertEquals("second", jars.get(1));
		assertEquals("Jar.jar", jars.get(2));
	}

	public void testWithSecondJarOnTwoLine3() throws IOException {
		String testWithSecondJarOnTwoLine = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + WHITESPACE + "second\n" + "Jar.jar\nBuild-Reference: Version toto";
		analyzeManifestContents(testWithSecondJarOnTwoLine);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 2, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
		assertEquals("second", jars.get(1));
	}

	public void testWithSecondJarOnTwoLine4() throws IOException {
		String testWithSecondJarOnTwoLine = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + "\n" + "second\n" + WHITESPACE + "Jar.jar\nBuild-Reference: Version toto";
		analyzeManifestContents(testWithSecondJarOnTwoLine);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 1, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
	}

	public void testWithSecondJarOnNextLine5() throws IOException {
		String testWithSecondJarOnNextLine = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + "\n"+ WHITESPACE + "secondJar.jar\nBuild-Reference: Version toto";
		analyzeManifestContents(testWithSecondJarOnNextLine);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 1, jars.size());
		assertEquals(FIRST_JAR + SECOND_JAR, jars.get(0));
	}

	public void testWithSecondJarOnTwoLineEndedWithEOF() throws IOException {
		String testWithSecondJarOnTwoLineEndedWithEOF = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + " second\n Jar.jar";
		analyzeManifestContents(testWithSecondJarOnTwoLineEndedWithEOF);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 1, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
	}

	public void testWithSecondJarOnTwoLineEndedWithEOF2() throws IOException {
		String testWithSecondJarOnTwoLineEndedWithEOF = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + " second\n Jar.jar\n";
		analyzeManifestContents(testWithSecondJarOnTwoLineEndedWithEOF);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 2, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
		assertEquals(SECOND_JAR, jars.get(1));
	}

	public void testWithSecondJarOnTwoLineEndedWithWhiteSpaceEOF() throws IOException {
		String testWithSecondJarOnTwoLineEndedWithWhiteSpaceEOF = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + " second\n Jar.jar ";
		analyzeManifestContents(testWithSecondJarOnTwoLineEndedWithWhiteSpaceEOF);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 2, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
		assertEquals(SECOND_JAR, jars.get(1));
	}

	public void testWithSecondJarOnTwoLineEndedWithWhiteSpaceNewLineEOF() throws IOException {
		String testWithSecondJarOnTwoLineEndedWithWhiteSpaceNewLineEOF = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + " second\n Jar.jar \n";
		analyzeManifestContents(testWithSecondJarOnTwoLineEndedWithWhiteSpaceNewLineEOF);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 2, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
		assertEquals(SECOND_JAR, jars.get(1));
	}

	public void testWithSecondJarOnTwoLineEndedWithNewLineEOF() throws IOException {
		String testWithSecondJarOnTwoLineEndedWithNewLineEOF = "Manifest-Version: 1.0\nAnt-Version: Apache Ant 1.6.5\nCreated-By: 1.5.0_14-b03 (Sun Microsystems Inc.)\nClass-Path: " + FIRST_JAR + " second\n Jar.jar\n";
		analyzeManifestContents(testWithSecondJarOnTwoLineEndedWithNewLineEOF);
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 2, jars.size());
		assertEquals(FIRST_JAR, jars.get(0));
		assertEquals(SECOND_JAR, jars.get(1));
	}

	public void testWithOneJarUsingUTF8Name() throws IOException {
		try (InputStream inputStream = ManifestAnalyzerTest.class.getResourceAsStream("MANIFEST.MF")) {
			this.manifestAnalyzer.analyzeManifestContents(inputStream);
		}
		List jars = this.manifestAnalyzer.getCalledFileNames();
		assertEquals("Wrong size", 1, jars.size());
		assertEquals("called\u3042.jar", jars.get(0));
	}
}
