/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.IOException;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ModuleDeclarationSyntaxTest extends AbstractSyntaxTreeTest {

	public ModuleDeclarationSyntaxTest(String name, String referenceCompiler,
			String referenceCompilerTestsScratchArea) {
		super(name, referenceCompiler, referenceCompilerTestsScratchArea);
	}
	public static Class<?> testClass() {
		return ModuleDeclarationSyntaxTest.class;
	}
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	static {
		//		TESTS_NAMES = new String[] { "test0009" };
		//		TESTS_NUMBERS = new int[] { 133, 134, 135 };
	}
	public ModuleDeclarationSyntaxTest(String testName){
		super(testName, null, null);
	}
	public void test0001() throws IOException {
		String source =
				"module com.greetings {\n" +
				"}\n";
		String expectedUnitToString =
				"module com.greetings {\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0002() throws IOException {
		String source =
				"module com.greetings {\n" +
				    "requires org.astro;" +
				"}\n";
		String expectedUnitToString =
				"module com.greetings {\n" +
				"  requires org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0003() throws IOException {
		String source =
				"module org.astro {\n" +
				"    exports org.astro;\n" +
				"}\n";
		String expectedUnitToString =
				"module org.astro {\n" +
				"  exports org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0004() throws IOException {
		String source =
				"module org.astro {\n" +
				"    exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		String expectedUnitToString =
				"module org.astro {\n" +
				"  exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0005() throws IOException {
		String source =
				"module com.socket {\n" +
				"    exports com.socket;\n" +
				"    exports com.socket.spi;\n" +
				"    uses com.socket.spi.NetworkSocketProvider;\n" +
				"}\n";
		String expectedUnitToString =
				"module com.socket {\n" +
				"  exports com.socket;\n" +
				"  exports com.socket.spi;\n" +
				"  uses com.socket.spi.NetworkSocketProvider;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0006() throws IOException {
		String source =
				"module org.fastsocket {\n" +
				"    requires com.socket;\n" +
				"    provides com.socket.spi.NetworkSocketProvider\n" +
				"      with org.fastsocket.FastNetworkSocketProvider;\n" +
				"}\n";
		String expectedUnitToString =
				"module org.fastsocket {\n" +
				"  requires com.socket;\n" +
				"  provides com.socket.spi.NetworkSocketProvider with org.fastsocket.FastNetworkSocketProvider;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0007() throws IOException {
		String source =
				"module org.fastsocket {\n" +
				"    requires com.socket;\n" +
				"    provides com.socket.spi.NetworkSocketProvider;\n" +
				"}\n";
		String expectedErrorString =
				"----------\n" +
				"1. ERROR in module-info (at line 3)\n" +
				"	provides com.socket.spi.NetworkSocketProvider;\n" +
				"	                       ^\n" +
				"Syntax error on token \".\", with expected\n" +
				"----------\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), expectedErrorString, "module-info", null, null, options);
	}
	public void test0008() throws IOException {
		String source =
				"module @Marker com.greetings {\n" +
				"	requires org.astro;" +
				"}\n";
		String errorMsg = """
				----------
				1. ERROR in module-info (at line 1)
					module @Marker com.greetings {
					^^^^^^
				Syntax error on token "module", delete this token
				----------
				2. ERROR in module-info (at line 1)
					module @Marker com.greetings {
					       ^^^^^^^^^^^^^^^^^^^^^
				Syntax error on tokens, ModuleHeader expected instead
				----------
				""";

		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0009() throws IOException {
		String source =
				"module com.greetings {\n" +
				"	requires @Marker org.astro;\n" +
				"}\n";
		String errorMsg = """
				----------
				1. ERROR in module-info (at line 1)
					module com.greetings {
					requires @Marker org.astro;
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				2. ERROR in module-info (at line 2)
					requires @Marker org.astro;
					                    ^
				Syntax error on token(s), misplaced construct(s)
				----------
				3. ERROR in module-info (at line 3)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0010() throws IOException {
		String source =
				"module com.greetings {\n" +
				"	requires private org.astro;\n" +
				"}\n";
		String errorMsg =
				"----------\n" +
				"1. ERROR in module-info (at line 2)\n" +
				"	requires private org.astro;\n"+
				"	         ^^^^^^^\n"+
				"Syntax error on token \"private\", delete this token\n" +
				 "----------\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0011() throws IOException {
		String source =
				"module com.greetings {\n" +
				"	exports @Marker com.greetings;\n" +
				"}\n";
		String errorMsg = """
				----------
				1. ERROR in module-info (at line 1)
					module com.greetings {
					exports @Marker com.greetings;
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				2. ERROR in module-info (at line 2)
					exports @Marker com.greetings;
					                   ^
				Syntax error on token(s), misplaced construct(s)
				----------
				3. ERROR in module-info (at line 3)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0012() throws IOException {
		String source =
				"module com.greetings {\n" +
				"	exports com.greetings to @Marker org.astro;\n" +
				"}\n";
		String errorMsg =
				"----------\n" +
				"1. ERROR in module-info (at line 2)\n" +
				"	exports com.greetings to @Marker org.astro;\n"+
				"	                         ^^^^^^^\n"+
				"Syntax error on tokens, delete these tokens\n" +
				 "----------\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0013() throws IOException {
		String source =
				"module com.greetings {\n" +
				"	uses @Marker org.astro.World;\n" +
				"}\n";
		String errorMsg =
				"----------\n" +
				"1. ERROR in module-info (at line 2)\n" +
				"	uses @Marker org.astro.World;\n" +
				"	     ^^^^^^^\n"+
				"Syntax error, type annotations are illegal here\n" +
				 "----------\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0014() throws IOException {
		String source =
				"module com.greetings {\n" +
				"	provides @Marker org.astro.World with @Marker com.greetings.Main;\n" +
				"}\n";
		String errorMsg =
				"----------\n" +
				"1. ERROR in module-info (at line 2)\n" +
				"	provides @Marker org.astro.World with @Marker com.greetings.Main;\n" +
				"	         ^^^^^^^\n"+
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in module-info (at line 2)\n" +
				"	provides @Marker org.astro.World with @Marker com.greetings.Main;\n" +
				"	                                      ^^^^^^^\n"+
				"Syntax error, type annotations are illegal here\n" +
				 "----------\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0015() throws IOException {
		String source =
				"module com.greetings {\n" +
				    "requires transitive org.astro;" +
				"}\n";
		String expectedUnitToString =
				"module com.greetings {\n" +
				"  requires transitive org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0016() throws IOException {
		String source =
				"module com.greetings {\n" +
				    "requires static org.astro;" +
				"}\n";
		String expectedUnitToString =
				"module com.greetings {\n" +
				"  requires static org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0017() throws IOException {
		String source =
				"module com.greetings {\n" +
				    "requires transitive static org.astro;" +
				"}\n";
		String expectedUnitToString =
				"module com.greetings {\n" +
				"  requires transitive static org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0018() throws IOException {
		String source =
				"import com.socket.spi.NetworkSocketProvider;\n" +
				"module org.fastsocket {\n" +
				"    requires com.socket;\n" +
				"    provides NetworkSocketProvider\n" +
				"      with org.fastsocket.FastNetworkSocketProvider;\n" +
				"}\n";
		String expectedUnitToString =
				"import com.socket.spi.NetworkSocketProvider;\n" +
				"module org.fastsocket {\n" +
				"  requires com.socket;\n" +
				"  provides NetworkSocketProvider with org.fastsocket.FastNetworkSocketProvider;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0019() throws IOException {
		String source =
				"import com.socket.spi.*;\n" +
				"module org.fastsocket {\n" +
				"    requires com.socket;\n" +
				"    provides NetworkSocketProvider\n" +
				"      with org.fastsocket.FastNetworkSocketProvider;\n" +
				"}\n";
		String expectedUnitToString =
				"import com.socket.spi.*;\n" +
				"module org.fastsocket {\n" +
				"  requires com.socket;\n" +
				"  provides NetworkSocketProvider with org.fastsocket.FastNetworkSocketProvider;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0020() throws IOException {
		String source =
				"open module com.greetings {\n" +
				    "requires transitive static org.astro;" +
				"}\n";
		String expectedUnitToString =
				"open module com.greetings {\n" +
				"  requires transitive static org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0021() throws IOException {
		String source =
				"module org.fastsocket {\n" +
				"    requires com.socket;\n" +
				"    provides com.socket.spi.NetworkSocketProvider\n" +
				"      with org.fastsocket.FastNetworkSocketProvider, org.fastSocket.SlowNetworkSocketProvider;\n" +
				"}\n";
		String expectedUnitToString =
				"module org.fastsocket {\n" +
				"  requires com.socket;\n" +
				"  provides com.socket.spi.NetworkSocketProvider with org.fastsocket.FastNetworkSocketProvider, org.fastSocket.SlowNetworkSocketProvider;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0022() throws IOException {
		String source =
				"module org.astro {\n" +
				"    opens org.astro;\n" +
				"}\n";
		String expectedUnitToString =
				"module org.astro {\n" +
				"  opens org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0023() throws IOException {
		String source =
				"module org.astro {\n" +
				"    opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		String expectedUnitToString =
				"module org.astro {\n" +
				"  opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0024() throws IOException {
		String source =
				"module org.astro {\n" +
				"    exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"    opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"    opens org.astro.galaxy to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		String expectedUnitToString =
				"module org.astro {\n" +
				"  exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"  opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"  opens org.astro.galaxy to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0025() throws IOException {
		String source =
				"@Foo\n" +
				"module org.astro {\n" +
				"    exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"    opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"    opens org.astro.galaxy to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		String expectedUnitToString =
				"@Foo\n" +
				"module org.astro {\n" +
				"  exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"  opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"  opens org.astro.galaxy to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0026() throws IOException {
		String source =
				"@Foo\n" +
				"open module org.astro {\n" +
				"    exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"    opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"    opens org.astro.galaxy to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		String expectedUnitToString =
				"@Foo\n" +
				"open module org.astro {\n" +
				"  exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"  opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"  opens org.astro.galaxy to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}

	public void test0027() throws IOException {
		String source =
				"@Foo @Bar(x = 2) @Baz(\"true\")\n" +
				"open module org.astro {\n" +
				"    exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"    opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"    opens org.astro.galaxy to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		String expectedUnitToString =
				"@Foo @Bar(x = 2) @Baz(\"true\")\n" +
				"open module org.astro {\n" +
				"  exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"  opens org.astro to com.greetings, com.example1, com.example2;\n" +
				"  opens org.astro.galaxy to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}

	public void testBug518626() throws IOException {
		String source =
				"module module.test {\n" +
				"    provides X with Y;\n" +
				"}\n";
		String expectedUnitToString =
				"module module.test {\n" +
				"  provides X with Y;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void testbug488541() throws IOException {
		String source =
				"module module {\n" +
				"   requires requires;\n" +
				"   exports to to exports;\n" +
				"   uses module;\n" +
				"   provides uses with to;\n" +
				"}\n";
		String expectedUnitToString =
				"module module {\n" +
				"  requires requires;\n" +
				"  exports to to exports;\n" +
				"  uses module;\n" +
				"  provides uses with to;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void testbug488541a() throws IOException {
		String source =
			"import module.pack1.exports.pack2;\n" +
			"import module.open.pack1.opens.pack2;\n" +
			"@open @module(true)\n" +
			"open module module.module.module {\n" +
			"   requires static transitive requires;\n" +
			"   requires transitive static transitive;\n" +
			"   exports to to exports;\n" +
			"   opens module.to.pack1 to to.exports;\n" +
			"   uses module;\n" +
			"   provides uses with to;\n" +
			"}\n";
		String expectedUnitToString =
			"import module.pack1.exports.pack2;\n" +
			"import module.open.pack1.opens.pack2;\n" +
			"@open @module(true)\n" +
			"open module module.module.module {\n" +
			"  requires transitive static requires;\n" +
			"  requires transitive static transitive;\n" +
			"  exports to to exports;\n" +
			"  opens module.to.pack1 to to.exports;\n" +
			"  uses module;\n" +
			"  provides uses with to;\n" +
			"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void testbug488541b() throws IOException {
		String source =
				"module module {\n" +
				"   requires requires;\n" +
				"   exports to to exports, module;\n" +
				"   uses module;\n" +
				"   provides uses with to, open, module;\n" +
				"}\n";
		String expectedUnitToString =
				"module module {\n" +
				"  requires requires;\n" +
				"  exports to to exports, module;\n" +
				"  uses module;\n" +
				"  provides uses with to, open, module;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}

}
