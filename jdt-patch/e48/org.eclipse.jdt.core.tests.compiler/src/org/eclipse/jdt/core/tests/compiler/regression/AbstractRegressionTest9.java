/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

public class AbstractRegressionTest9 extends AbstractRegressionTest {

	public AbstractRegressionTest9(String name) {
		super(name);
	}

	protected Map<String,IModule> moduleMap = new HashMap<>(); // by name
	protected Map<String,String> file2module = new HashMap<>();

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.moduleMap.clear();
		this.file2module.clear();
		DefaultJavaRuntimeEnvironment.cleanUpDefaultJreClassLibs();
	}

	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(classPaths == null);
		for (INameEnvironment nameEnvironment : classLibs) {
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}
	
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
				compilationUnits[i].module = moduleFiles.get(fileNameString);
			} else {
				String modName = this.file2module.get(fileNameString.replace(File.separator, "/"));
				if (modName != null) {
					compilationUnits[i].module = modName.toCharArray();
				}
			}
		}
		return compilationUnits;
	}

	private IModule extractModuleDesc(String fileName, String fileContent) {
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

	private Parser createParser() {
		Map<String,String> opts = new HashMap<String, String>();
		opts.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_9);
		return new Parser(
				new ProblemReporter(getErrorHandlingPolicy(), new CompilerOptions(opts), getProblemFactory()),
				false);
	}

	// ------------------------------------------------------

	/** Use in tests to associate the CU in given files to the module of the given name. */
	public void associateToModule(String moduleName, String... fileNames) {
		for (String fileName : fileNames)			
			this.file2module.put(fileName, moduleName);
	}

}
