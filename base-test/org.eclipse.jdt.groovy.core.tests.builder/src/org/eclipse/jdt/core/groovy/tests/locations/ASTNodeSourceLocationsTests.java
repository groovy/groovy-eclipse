/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.locations;

import groovy.lang.GroovyClassLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import junit.framework.Test;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.eclipse.jdt.core.groovy.tests.builder.GroovierBuilderTests;
/**
 * Test the source locations of ASTNodes to ensure they are correct, especially 
 * look into the changes that we force into them.
 * @author andrew
 * @created Jun 9, 2010
 */
public class ASTNodeSourceLocationsTests  extends GroovierBuilderTests {
	
	public ASTNodeSourceLocationsTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(ASTNodeSourceLocationsTests.class);
	}

	public void testBinaryExpr1() throws Exception {
		String contents = 
			"def map = [:]\n" +
			"map = [:]";
		checkBinaryExprSLocs(contents, 
				new BinaryExpressionSLocTester(), 
				"def map = [:]", "map = [:]");
	}

	public void testBinaryExpr2() throws Exception {
		String contents = 
			"def foo = [1, 2] as Set\n" +
			"foo == [1, 2] as Set";
		checkBinaryExprSLocs(contents, 
				new BinaryExpressionSLocTester(), 
		"def foo = [1, 2] as Set", "foo == [1, 2] as Set");
	}

	public void testBinaryExpr3() throws Exception {
		String contents = 
			"(foo == [1, 2] as Set)";
		checkBinaryExprSLocs(contents, 
				new BinaryExpressionSLocTester(), "(foo == [1, 2] as Set)");
	}
	
	public void testBinaryExpr4() throws Exception {
		String contents = 
			"((foo == [1, 2] as Set))";
		checkBinaryExprSLocs(contents, 
		new BinaryExpressionSLocTester(), "((foo == [1, 2] as Set))");
	}
	
	public void testBinaryExpr5() throws Exception {
		String contents = 
			"[:] + [:] +  [:]";
		checkBinaryExprSLocsReverse(contents, 
		"[:] + [:] +  [:]", 
		"[:] + [:]");
	}
	
	public void testBinaryExpr6() throws Exception {
		String contents = 
			"[:] << [:] + [:]";
		checkBinaryExprSLocsReverse(contents, 
		"[:] << [:] + [:]",
		"[:] + [:]");
	}
	
	// Not right!!! ending whitespace is included, but shouldm't be.
	public void testBinaryExpr7() throws Exception {
		String contents = 
			"  a = b   ";
		checkBinaryExprSLocs(contents, 
				new BinaryExpressionSLocTester(), "a = b   ");
	}
	
	public void testMapExpression1() throws Exception {
		checkBinaryExprSLocs("[:]", 
				new MapExpressionSLocTester(), "[:]");
	}
	
	public void testMapExpression2() throws Exception {
		checkBinaryExprSLocs("[  :  ]", 
				new MapExpressionSLocTester(), "[  :  ]");
	}
	
	public void testMapExpression3() throws Exception {
		checkBinaryExprSLocs("def x = [:]", 
				new MapExpressionSLocTester(), "[:]");
	}
	
	// fails because we are not smart about how we extend
	// slocs for empty map expressions
	public void _testMapExpression4() throws Exception {
		checkBinaryExprSLocs("def x = [ : ]", 
				new MapExpressionSLocTester(), "[ : ]");
	}
	
	public void testMapEntryExpression1() throws Exception {
		checkBinaryExprSLocs("[a:b]", 
				new MapEntryExpressionSLocTester(), "a:b");
	}
	
	public void testMapEntryExpression2() throws Exception {
		checkBinaryExprSLocs("[a : b]", 
				new MapEntryExpressionSLocTester(), "a : b");
	}
	
	// has extra whitespace at end, but should not
	public void testMapEntryExpression3() throws Exception {
		checkBinaryExprSLocs("[a : b  ]", 
				new MapEntryExpressionSLocTester(), "a : b");
	}
	
	public void testMapEntryExpression4() throws Exception {
		checkBinaryExprSLocs("[a : b, c : d]", 
				new MapEntryExpressionSLocTester(), "a : b", "c : d");
	}
	
	
	public void testMapEntryExpression5() throws Exception {
		checkBinaryExprSLocs("def x = [a : b, c : d]", 
				new MapEntryExpressionSLocTester(), "a : b", "c : d");
	}
	
	public void testMapEntryExpression6() throws Exception {
		checkBinaryExprSLocs("def x = [a : b] << [ c : d]", 
				new MapEntryExpressionSLocTester(), "a : b", "c : d");
	}
	
	public void testMapEntryExpression7() throws Exception {
		checkBinaryExprSLocs("def x = [a : b, e : [ c : d]]", 
				new MapEntryExpressionSLocTester(), "a : b", "c : d", "e : [ c : d]");
	}
	
	public void testCastExpression1() throws Exception {
		checkBinaryExprSLocs("foo as Set", 
				new CastExpressionSLocTester(), "foo as Set");
	}
	
	public void testCastExpression2() throws Exception {
		checkBinaryExprSLocs("def x = foo as Set", 
				new CastExpressionSLocTester(), "foo as Set");
	}
	
	public void testCastExpression3() throws Exception {
		checkBinaryExprSLocs("def x = foo as Set ", 
				new CastExpressionSLocTester(), "foo as Set");
	}
	
	public void testImportStatement1() throws Exception {
        checkBinaryExprSLocs(
                "import javax.swing.*\n" +
                "import javax.swing.JFrame\n" +
                "import javax.applet.*;\n" +
                "import javax.applet.Applet;\n", 
                new ImportStatementSLocTester(), 
                "import javax.swing.*",
                "import javax.swing.JFrame",
                "import javax.applet.*",
                "import javax.applet.Applet");
    }
	
	
	class StartAndEnd {
		final int start;
		final int end;
		public StartAndEnd(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
		public StartAndEnd(ASTNode node) {
			this(node.getStart(), node.getEnd());
		}

		boolean isOK(ASTNode node) {
			return node.getStart() == start && node.getEnd() == end;
		}
		
		@Override
		public String toString() {
			return "[start=" + start + ", end=" + end + "]";
		}
	}
	
	
	abstract class AbstractSLocTester extends ClassCodeVisitorSupport {
		List<ASTNode> allCollectedNodes = new ArrayList<ASTNode>();

		
		void doTest(ModuleNode module, StartAndEnd...sae) {
			for (ClassNode c : (Iterable<ClassNode>) module.getClasses()) {
				this.visitClass(c);
			}
			
			assertStartAndEnds(sae);
		}

		@Override
		protected SourceUnit getSourceUnit() {
			return null;
		}
		
		void assertStartAndEnds(StartAndEnd...sae) {
			assertEquals("Wrong number expressions found", sae.length, allCollectedNodes.size());
			ASTNode[] bexprs = allCollectedNodes.toArray(new ASTNode[0]);
			List<Integer> problemIndices = new ArrayList<Integer>();
			for (int i = 0; i < bexprs.length; i++) {
				if (! sae[i].isOK(bexprs[i])) {
					problemIndices.add(i);
				}
			}
			
			if (problemIndices.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (Integer integer : problemIndices) {
					int val = integer.intValue();
					sb.append("Expected slocs at " + sae[val] + " for expression " + bexprs[val] + 
							"but instead found: " + new StartAndEnd(bexprs[val]) + "\n");
				}
				fail(sb.toString());
			}
		}
	}
	
	class ImportStatementSLocTester extends AbstractSLocTester {
//        @Override
        public void visitImports(ModuleNode module) {
            ImportNodeCompatibilityWrapper wrapper = new ImportNodeCompatibilityWrapper(module);
            SortedSet<ImportNode> nodes = wrapper.getAllImportNodes();
            for (ImportNode node : nodes) {
                allCollectedNodes.add(node);
            }
        }
    }
    
	
	class BinaryExpressionSLocTester extends AbstractSLocTester {
		@Override
		public void visitBinaryExpression(BinaryExpression expression) {
			super.visitBinaryExpression(expression);
			allCollectedNodes.add(expression);
		}
	}
	
	class MapExpressionSLocTester extends AbstractSLocTester {
		@Override
		public void visitMapExpression(MapExpression expression) {
			super.visitMapExpression(expression);
			allCollectedNodes.add(expression);
		}
	}

	class MapEntryExpressionSLocTester extends AbstractSLocTester {
		@Override
		public void visitMapEntryExpression(MapEntryExpression expression) {
			super.visitMapEntryExpression(expression);
			allCollectedNodes.add(expression);
		}
	}
	
	class CastExpressionSLocTester extends AbstractSLocTester {
		@Override
		public void visitCastExpression(CastExpression expression) {
			super.visitCastExpression(expression);
			allCollectedNodes.add(expression);
		}
	}
	
	private void checkBinaryExprSLocs(String contents, AbstractSLocTester tester, String... exprStrings) throws Exception {
		StartAndEnd[] points = convertToPoints(contents, exprStrings);
		ModuleNode module = createModuleNodeFor(contents);
		tester.doTest(module, points);
	}
	
	private void checkBinaryExprSLocsReverse(String contents, String...exprStrings) throws Exception {
		StartAndEnd[] points = convertToPoints(contents, exprStrings);
		List<StartAndEnd> list = Arrays.asList(points);
		Collections.reverse(list);
		points = list.toArray(points);
		ModuleNode module = createModuleNodeFor(contents);
		BinaryExpressionSLocTester tester = new BinaryExpressionSLocTester();
		tester.doTest(module, points);
	}

	private StartAndEnd[] convertToPoints(String contents, String[] exprStrings) {
		StartAndEnd[] points = new StartAndEnd[exprStrings.length];
		int prevEnd = 0;
		int prevStart = 0;
		for (int i = 0; i < exprStrings.length; i++) {
			int start = contents.indexOf(exprStrings[i], prevEnd);
			if (start == -1) {
				// now try prevStart
				start = contents.indexOf(exprStrings[i], prevStart);
				if (start == -1) {
					// now try from the beginning
					start = contents.indexOf(exprStrings[i]); 
					if (start == -1) {
						fail("Could not find exprString");
					}
				}
			}
			int end = start + exprStrings[i].length();
			points[i] = new StartAndEnd(start, end);
			prevStart = start;
			prevEnd = end + 1;
		}
		return points;
	}

	private ModuleNode createModuleNodeFor(String contents) throws Exception {
        SourceUnit sourceUnit = new SourceUnit("Foo", contents, new CompilerConfiguration(), new GroovyClassLoader(), new ErrorCollector(new CompilerConfiguration()));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        return sourceUnit.getAST();
	}

}
