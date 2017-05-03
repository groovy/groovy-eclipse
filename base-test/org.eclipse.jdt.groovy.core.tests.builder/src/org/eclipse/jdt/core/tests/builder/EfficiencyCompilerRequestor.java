/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.core.tests.builder;

import java.util.Vector;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IDebugRequestor;
import org.eclipse.jdt.internal.core.util.Util;

public class EfficiencyCompilerRequestor implements IDebugRequestor {
	private boolean isActive = false;

	private Vector<String> compiledClasses = new Vector<String>(10);

	public void acceptDebugResult(CompilationResult result){
		ClassFile[] classFiles = result.getClassFiles();
		Util.sort(classFiles, new Util.Comparer() {
			public int compare(Object a, Object b) {
				String aName = new String(((ClassFile)a).fileName());
				String bName = new String(((ClassFile)b).fileName());
				return aName.compareTo(bName);
			}
		});
		for (int i = 0; i < classFiles.length; i++) {
			String className = new String(classFiles[i].fileName());
			className = className.replace('/', '.');
			if (!compiledClasses.contains(className)) {
				compiledClasses.addElement(className);
			}
		}
	}

	public String[] getCompiledClasses(){
		return compiledClasses.toArray(new String[0]);
	}

	public void clearResult(){
		compiledClasses.clear();
	}

	public void reset() {
	    // do nothing by default
	}

	public void activate() {
		isActive = true;
	}

	public void deactivate() {
		isActive = false;
	}

	public boolean isActive() {
		return isActive;
	}
}
