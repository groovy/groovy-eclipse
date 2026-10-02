/*******************************************************************************
 * Copyright (c) 2020, 2022 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.BasicModule;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTest_15 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocDescription = CompilerOptions.RETURN_TAG;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;
	String reportDeprecation = CompilerOptions.ERROR;
	String reportJavadocDeprecation = null;
	String processAnnotations = null;

	protected Map<String,IModule> moduleMap = new HashMap<>(); // by name
	Map<String,String> file2module = new HashMap<>();

public JavadocTest_15(String name) {
	super(name);
}

public static Class javadocTestClass() {
	return JavadocTest_15.class;
}

// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {

}

public static Test suite() {
	return buildMinimalComplianceTestSuite(javadocTestClass(), F_15);
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
	options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
	if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
	}
	if (this.reportJavadocDeprecation != null) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, this.reportJavadocDeprecation);
	}
	if (this.reportMissingJavadocComments != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.ENABLED);
		if (this.reportMissingJavadocCommentsVisibility != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, this.reportMissingJavadocCommentsVisibility);
		}
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocTags != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.ENABLED);
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocDescription != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagDescription, this.reportMissingJavadocDescription);
	}
	if (this.processAnnotations != null) {
		options.put(CompilerOptions.OPTION_Process_Annotations, this.processAnnotations);
	}
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportDeprecation, this.reportDeprecation);
	options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	return options;
}
@Override
protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
	this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
	INameEnvironment[] classLibs = getClassLibs(classPaths == null, options);
	for (INameEnvironment nameEnvironment : classLibs) {
		((FileSystem) nameEnvironment).scanForModules(createParser());
	}
	return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
}
Parser createParser() {
	Map<String,String> opts = new HashMap<>();
	opts.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_9);
	return new Parser(
			new ProblemReporter(getErrorHandlingPolicy(), new CompilerOptions(opts), getProblemFactory()),
			false);
}
IModule extractModuleDesc(String fileName, String fileContent) {
	if (fileName.toLowerCase().endsWith(IModule.MODULE_INFO_JAVA)) {
		Parser parser = createParser();

		ICompilationUnit cu = new CompilationUnit(fileContent.toCharArray(), fileName, null);
		CompilationResult compilationResult = new CompilationResult(cu, 0, 1, 10);
		CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
		if (unit.isModuleInfo() && unit.moduleDeclaration != null) {
			return new BasicModule(unit.moduleDeclaration, null);
		}
	}
	return null;
}
private void populateModuleMap(final String[] modFiles) {
	for (int i = 0; i < modFiles.length; i+=2) {
		IModule module = extractModuleDesc(modFiles[i], modFiles[i+1]);
		if (module != null) {
			this.moduleMap.put(String.valueOf(module.name()), module);
		}
	}
}
//--- same as AbstractRegressionTest9, just in a different inheritance hierarchy:

@Override
protected CompilationUnit[] getCompilationUnits(String[] testFiles) {
	Map<String,char[]> moduleFiles= new HashMap<>(); // filename -> modulename
	// scan for all module-info.java:
	for (int i = 0; i < testFiles.length; i+=2) {
		IModule module = extractModuleDesc(testFiles[i], testFiles[i+1]);
		if (module != null) {
			this.moduleMap.put(String.valueOf(module.name()), module);
			moduleFiles.put(testFiles[0], module.name());
		}
	}
	// record module information in CUs:
	CompilationUnit[] compilationUnits = Util.compilationUnits(testFiles);
	for (int i = 0; i < compilationUnits.length; i++) {
		char[] fileName = compilationUnits[i].getFileName();
		String fileNameString = String.valueOf(compilationUnits[i].getFileName());
		if (CharOperation.endsWith(fileName, TypeConstants.MODULE_INFO_FILE_NAME)) {
			compilationUnits[i].module = moduleFiles.get(fileNameString.replace(File.separator, "/"));
		} else {
			String modName = this.file2module.get(fileNameString.replace(File.separator, "/"));
			if (modName != null) {
				compilationUnits[i].module = modName.toCharArray();
			}
		}
	}
	return compilationUnits;
}
/** Use in tests to associate the CU in file 'fileName' to the module of the given name. */
void associateToModule(String moduleName, String... fileNames) {
	for (String fileName : fileNames)
		this.file2module.put(fileName, moduleName);
}
@Override
protected void tearDown() throws Exception {
	super.tearDown();
	this.moduleMap.clear();
	this.file2module.clear();
}
/* (non-Javadoc)
 * @see junit.framework.TestCase#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.docCommentSupport = CompilerOptions.ENABLED;
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	this.reportDeprecation = CompilerOptions.ERROR;
}

public void test001() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * @see mod.one/\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * @see mod.one/p.I1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";
	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runConformTest( testFiles, modFiles, "" );
}

public void test002() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@link mod.one/}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@link mod.one/p.I1}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";
	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runConformTest(testFiles , modFiles, "" );
}

public void test003() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@linkplain mod.one/}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@linkplain mod.one/p.I1}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";


	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runConformTest(testFiles , modFiles, "" );
}

public void test004() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@linkplain mod.one/}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@linkplain mod.one/p.P1#foo()}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runConformTest(testFiles , modFiles, "" );
}

public void test005() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@linkplain mod.one/}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@linkplain mod.one/p.P1#abc}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	public int abc;\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runConformTest(testFiles , modFiles, "" );
}

public void test006() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@linkplain mod.one/}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@linkplain mod.one/p.P1#abd}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	public int abc;\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";
	String errorMsg = "" +
				"----------\n" +
				"1. ERROR in p\\P1.java (at line 4)\n" +
				"	* {@linkplain mod.one/p.P1#abd}\n" +
				"	                           ^^^\n" +
				"Javadoc: abd cannot be resolved or is not a field\n" +
				"----------\n";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runNegativeTest(testFiles , modFiles, errorMsg,
			            JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test007() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * @see mod.one/ abc\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * @see mod.one/p.I1 xyz\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runConformTest(testFiles , modFiles, "" );
}

public void test008() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@link mod.one/ abc}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@link mod.one/p.I1 xyz}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runConformTest(testFiles , modFiles, "" );
}

public void test009() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@linkplain mod.one/ abc}\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@linkplain mod.one/p.I1 xyz}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runConformTest(testFiles , modFiles, "" );
}

public void test010() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@linkplain mod.on/}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@linkplain mod.one/p.P1#abc}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	public int abc;\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";
	String errorMsg = "" +
				"----------\n" +
				"1. ERROR in p\\I1.java (at line 4)\n" +
				"	* {@linkplain mod.on/}\n" +
				"	              ^^^^^^\n" +
				"Javadoc: mod.on cannot be resolved to a module\n" +
				"----------\n";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runNegativeTest(testFiles , modFiles, errorMsg,
			            JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test011() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@linkplain mod.one/}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@linkplain mod.one/p.P2#abc}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	public int abc;\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";
	String errorMsg = "" +
				"----------\n" +
				"1. ERROR in p\\P1.java (at line 4)\n" +
				"	* {@linkplain mod.one/p.P2#abc}\n" +
				"	                      ^^^^\n" +
				"Javadoc: p.P2 cannot be resolved to a type\n" +
				"----------\n";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runNegativeTest(testFiles , modFiles, errorMsg,
			            JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test012() {
	File outputDirectory = new File(OUTPUT_DIR);
	Util.flushDirectoryContent(outputDirectory);

	String moduleInfo = "" +
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						"}";
	String I1 = "" +
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" * {@linkplain mod.one/}\n" +
				" */\n" +
				"interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}";

	String P1 = "" +
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" * {@linkplain mod.one/q.P1#abc}\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	public int abc;\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}";
	String errorMsg = "" +
				"----------\n" +
				"1. ERROR in p\\P1.java (at line 4)\n" +
				"	* {@linkplain mod.one/q.P1#abc}\n" +
				"	                      ^^^^\n" +
				"Javadoc: q cannot be resolved to a type\n" +
				"----------\n";

	associateToModule("mod.one", "p/I1.java");
	associateToModule("mod.one", "p/P1.java");
	String[] testFiles = new String[] {"p/I1.java", I1 ,"p/P1.java", P1};
	String[] modFiles =  new String[] {"module-info.java", moduleInfo  };
	populateModuleMap(modFiles);
	this.runNegativeTest(testFiles , modFiles, errorMsg,
			            JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

}

