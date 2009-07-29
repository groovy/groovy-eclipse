/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.CoreTestsActivator;
import org.codehaus.groovy.eclipse.core.compiler.GroovyCompiler;
import org.codehaus.groovy.eclipse.core.compiler.GroovyCompilerConfigurationBuilder;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompilationReporter;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompiler;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompilerConfiguration;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.impl.StringSourceBuffer;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.ITypeEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.SymbolTableRegistry;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluationContextBuilder;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;
import org.codehaus.groovy.eclipse.core.types.impl.ClassLoaderMemberLookup;
import org.codehaus.groovy.eclipse.core.types.impl.CompositeLookup;
import org.codehaus.groovy.eclipse.core.types.impl.GroovyProjectMemberLookup;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class InferredTypeEvaluatorTests extends EclipseTestCase {
	private static final String TEST_DATA = "/testData/InferredTypeEvaluatorTestCode.groovy";

	public void testLocalAsString() {
		checkInferredTypes(getName());
	}
	
	public void testLocalAsInteger() {
		checkInferredTypes(getName());
	}

	public void testFieldAsJFrame() {
		checkInferredTypes(getName());
	}
	
	public void testFieldAsString() {
		checkInferredTypes(getName());
	}
	
	public void testUndefinedFieldValue() {
		checkInferredTypes(getName());
	}
	
	public void testListType() {
		checkInferredTypes(getName());
	}
	
	public void testMapType() {
		checkInferredTypes(getName());
	}
	
	public void testBitwiseNegateOperatorPattern() {
		checkInferredTypes(getName());
	}
	
	public void testBitwiseNegateOperatorInteger() {
		checkInferredTypes(getName());
	}
	
	// TODO: emp - get these working.
//	public void testExplicitReturn() {
//		checkInferredTypes(getName());
//	}
//	
//	public void testImplicitReturn() {
//		checkInferredTypes(getName());
//	}
//	
//	public void testFirstParam() {
//		checkInferredTypes(getName());
//	}
//	
//	public void testSecondParam() {
//		checkInferredTypes(getName());
//	}
	
	/**
	 * Line column container.
	 */
	static class TestInfo {
		String testMethodName;

		int line;
		
		int startCol;
		
		int endCol;
		
		IRegion region;

		String expected;

		TestInfo(String testMethodName, int line, int startCol, int endCol, IRegion region, String expected) {
			this.testMethodName = testMethodName;
			this.line = line;
			this.startCol = startCol;
			this.endCol = endCol;
			this.region = region;
			this.expected = expected;
		}

		@Override
        public String toString() {
			StringBuffer sb = new StringBuffer(testMethodName);
			sb.append(":").append(expected).append(" [").append(line).append(',').append(startCol).append("..").append(
					endCol).append("]");
			return sb.toString();
		}
	}

	/**
	 * Reporter to get the AST from.
	 */
	static class TestCompilationReporter implements IGroovyCompilationReporter {
		public ModuleNode moduleNode;

		public void beginReporting() {
		}

		public void beginReporting(String fileName) {
		}

		public void compilationError(String fileName, int line, int startCol, int endCol, String message,
				String stackTrace) {
			throw new IllegalStateException(message + "\n" + stackTrace);
		}

		public void endReporting() {
		}

		public void endReporting(String fileName) {
		}

		public void generatedAST(String fileName, ModuleNode moduleNode) {
			this.moduleNode = moduleNode;
		}

		public void generatedCST(String fileName, GroovySourceAST cst) {
		}

		public void generatedClasses(String fileName, String[] classNames, String[] classFilePaths) {
		}
	}

	// The source code with tabs replaces with spaces.
	static String sourceCode;

	// Mapping from test case method name to an array of line/column info.
	static Map<String, TestInfo> mapTestCaseNameToTestInfo;
	
	/**
	 * Find the test locations by the test name, and check against the expected types.
	 * @param testName
	 */
	void checkInferredTypes(String testName) {
		// Set up lots of stuff.
		StringSourceBuffer buffer = new StringSourceBuffer(sourceCode);
		ModuleNode moduleNode = compileTestSource();
		String[] imports = createImportsArray(moduleNode);
		TestInfo info = mapTestCaseNameToTestInfo.get(testName);
		ISourceCodeContext[] sourceCodeContexts = createSourceCodeContexts(buffer, moduleNode, info);
		ISymbolTable symbolTable = SymbolTableRegistry.createSymbolTable(sourceCodeContexts);

		GroovyProjectFacade groovyProject = new GroovyProjectFacade(testProject.getJavaProject());
		IMemberLookup projectLookup = new GroovyProjectMemberLookup(groovyProject);
		IMemberLookup classloaderLookup = new ClassLoaderMemberLookup(Thread.currentThread().getContextClassLoader()); 
		IMemberLookup memberLookup = new CompositeLookup(new IMemberLookup[] { projectLookup, classloaderLookup });
		String expression = sourceCode.substring(info.region.getOffset(), info.region.getOffset() + info.region.getLength()).toString();
		
		// Set up the evaluation context.
		ITypeEvaluationContext evalContext = new TypeEvaluationContextBuilder()
				.classLoader(Thread.currentThread().getContextClassLoader())
				.imports(imports)
				.sourceCodeContext(sourceCodeContexts[sourceCodeContexts.length - 1])
				.symbolTable(symbolTable)
				.memberLookup(memberLookup)
				.location(info.region)
				.done();
		TypeEvaluator eval = new TypeEvaluator(evalContext);
		
		// Finally the result.
		EvalResult result = eval.evaluate(expression);
		
		assertEquals(info.expected, result.getName());
	}
	
	private ISourceCodeContext[] createSourceCodeContexts(StringSourceBuffer buffer, ModuleNode moduleNode, TestInfo info) {
		SourceCodeContextFactory factory = new SourceCodeContextFactory();
		return factory.createContexts(buffer, moduleNode, info.region);
	}

	private String[] createImportsArray(ModuleNode moduleNode) {
		List<ImportNode> imports = moduleNode.getImports();
		List<String> importPackages = moduleNode.getImportPackages();
		List<String> results = new ArrayList<String>();
		
		results.add(moduleNode.getPackageName() + "*");
		
		for (Iterator<ImportNode> iter = imports.iterator(); iter.hasNext();) {
			results.add(iter.next().getClassName());
		}
		for (Iterator<String> iter = importPackages.iterator(); iter.hasNext();) {
			results.add((iter.next()) + '*');
		}
		
		return (String[]) results.toArray(new String[results.size()]);
	}

	private ModuleNode compileTestSource() {
		ByteArrayInputStream is = new ByteArrayInputStream(sourceCode.getBytes());
		TestCompilationReporter reporter = new TestCompilationReporter();
		IGroovyCompiler compiler = new GroovyCompiler();
		IGroovyCompilerConfiguration config = new GroovyCompilerConfigurationBuilder().buildAST().done();
		compiler.compile("ContextTestCode.groovy", is, config, reporter);
		return reporter.moduleNode;
	}

	@Override
    protected void setUp() throws Exception {
		super.setUp();		
		GroovyRuntime.addGroovyRuntime(testProject.getProject());
		
		if (sourceCode == null) {
			final URL url = CoreTestsActivator.bundle().getEntry(
					TEST_DATA);
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(url.openStream()));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(output);

			String line = reader.readLine();
			String spaces = getTabSpaces(line);
			line = line.replaceAll("\\t", spaces);
			writer.println(line);
			while ((line = reader.readLine()) != null) {
				line = line.replaceAll("\\t", spaces);
				writer.println(line);
			}

			reader.close();
			writer.close();

			sourceCode = output.toString();

			mapTestCaseNameToTestInfo = mapTestCaseNameToTestInfo(sourceCode);
		}

		testProject.createGroovyTypeAndPackage("org.codehaus.groovy.eclipse.core.type",
				"InferredTypeEvaluatorTestCode.groovy", sourceCode);		
	}

	private String getTabSpaces(String line) {
		String tabSpacing = line.substring(0, "//#TAB_SPACING:4".length());
		int spacing = Integer.parseInt(tabSpacing.substring(tabSpacing.indexOf(':') + 1));
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < spacing; ++i) {
			sb.append(" ");
		}
		return sb.toString();
	}

	private Map<String, TestInfo> mapTestCaseNameToTestInfo(String sourceCode) throws Exception {
		Map<String, TestInfo> map = new HashMap<String, TestInfo>();

		LineNumberReader reader = new LineNumberReader(new StringReader(sourceCode));
		String line;
		while ((line = reader.readLine()) != null) {
			TestInfo info = parseTestInfo(line, reader.getLineNumber());
			if (info != null) {
				if (map.containsKey(info.testMethodName)) {
					throw new IllegalStateException("Test method name " + info.testMethodName
							+ " already exists in test case.");
				}
				map.put(info.testMethodName, info);
			}
		}
		
		// fix the regions
		IDocument doc = new Document(sourceCode);
		for (TestInfo info : map.values()) {
			info.region = new Region(doc.getLineInformation(info.line-1).getOffset() + info.startCol - 1, 
					info.endCol - info.startCol);
		}
		
		return map;
	}

	/**
	 * Given a string like this:<br>
	 * "//#testInModuleBody,1,17,23,java.lang.String parse the TestInfo from the string.
	 * 
	 * @param line
	 *            The line text.
	 * @param lineNumber
	 *            The current line number.
	 * @return The TestInfo or null if there is none.
	 */
	private TestInfo parseTestInfo(String line, int lineNumber) {
		Pattern pattern = Pattern.compile("\\s*//#(test\\w+),(\\d+),(\\d+),([\\w.]+)");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			String testMethodName = matcher.group(1);
			int startCol = Integer.parseInt(matcher.group(2));
			int endCol = Integer.parseInt(matcher.group(3));
			String expected = matcher.group(4);
			return new TestInfo(testMethodName, lineNumber + 1, startCol, endCol, null, expected);
		}
		return null;
	}
}