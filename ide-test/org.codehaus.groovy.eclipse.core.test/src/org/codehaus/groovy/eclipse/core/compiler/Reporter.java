 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.ast.ModuleNode;

/**
 * Reporter to collect various info during testing.
 * 
 * @author empovazan
 */
class Reporter implements IGroovyCompilationReporter {
	Map mapFileNameToCST = new HashMap();

	Map mapFileNameToAST = new HashMap();

	Map mapFileNameToClassNames = new HashMap();

	Map mapFileNameToClassFilePaths = new HashMap();

	Map mapFileNameToErrorMessages = new HashMap();

	boolean reportingStarted = false;

	boolean reportingEnded = false;

	/** Contains file names that began reporting while inside an existing begin/end report. */
	Set setOfBeginReportingErrorFileNames = new HashSet();

	/** Contains file names that ended reporting while another was being ended. */
	Set setOfEndReportingErrorFileNames = new HashSet();

	Set setOfDuplicateReportingErrorFileNames = new HashSet();

	String reportingFileName = null;

	public void beginReporting() {
		reportingStarted = true;
	}

	public void endReporting() {
		reportingEnded = true;
	}

	public void beginReporting(String fileName) {
		if (reportingFileName != null) {
			System.out.println("Begin error: current = " + reportingFileName + ", begin = " + fileName);
			setOfBeginReportingErrorFileNames.add(reportingFileName);
		}
		if (setOfBeginReportingErrorFileNames.contains(fileName)) {
			System.out.println("Begin duplicate error:" + reportingFileName);
			setOfDuplicateReportingErrorFileNames.add(fileName);
		}
		reportingFileName = fileName;
	}

	public void compilationError(String fileName, int line, int startCol, int endCol, String message, String stackTrace) {
		List list = (List) mapFileNameToErrorMessages.get(fileName);
		if (list == null) {
			list = new ArrayList();
			mapFileNameToErrorMessages.put(fileName, list);
		}
		System.out.println("Error: " + message);
		list.add(message);
	}

	public void endReporting(String fileName) {
		if (!reportingFileName.equals(fileName)) {
			System.out.println("End error: current = " + reportingFileName + ", end = " + fileName);
			setOfEndReportingErrorFileNames.add(fileName);
		}
		reportingFileName = null;
	}

	public void generatedCST(String fileName, GroovySourceAST cst) {
		System.out.println("Generated CST: " + fileName);
		mapFileNameToCST.put(fileName, cst);
	}

	public void generatedAST(String fileName, ModuleNode moduleNode) {
		System.out.println("Generated AST: " + fileName);
		mapFileNameToAST.put(fileName, moduleNode);
	}

	public void generatedClasses(String fileName, String[] classNames, String[] classFilePaths) {
		System.out.println("Generated class files: " + fileName);
		for (int i = 0; i < classNames.length; i++) {
			System.out.println("\t" + classNames[i]);
		}
		for (int i = 0; i < classFilePaths.length; i++) {
			System.out.println("\t" + classFilePaths[i]);
		}

		mapFileNameToClassNames.put(fileName, classNames);
		mapFileNameToClassFilePaths.put(fileName, classFilePaths);
	}

	public boolean hasReportingErrors() {
		return setOfBeginReportingErrorFileNames.size() != 0 || setOfEndReportingErrorFileNames.size() != 0
				|| setOfDuplicateReportingErrorFileNames.size() != 0 || mapFileNameToErrorMessages.size() != 0;
	}
}