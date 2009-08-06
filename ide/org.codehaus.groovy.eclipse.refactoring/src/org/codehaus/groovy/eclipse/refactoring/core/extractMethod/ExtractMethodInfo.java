/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.core.extractMethod;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringInfo;

/**
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class ExtractMethodInfo extends RefactoringInfo {
	
	private ExtractMethodProvider extractMethProvider;

	public ExtractMethodInfo(ExtractMethodProvider provider) {
		super(provider);
		extractMethProvider = provider;
	}

	public BlockStatement getBlockStatement() {
		return extractMethProvider.getBlockStatement();
	}

	public String getMethodCall() {
		return extractMethProvider.getMethodCall();
	}

	public MethodNode getNewMethod() {
		return extractMethProvider.getNewMethod();
	}

	public String getNewMethodname() {
		return extractMethProvider.getNewMethodname();
	}

	public void setNewMethodname(String newMethodname) {
		extractMethProvider.setNewMethodname(newMethodname);
		setChanged();
		notifyObservers();
	}
	
	public void setModifier(int modifier) {
		extractMethProvider.setModifier(modifier);
		setChanged();
		notifyObservers();
	}
	public String getMethodHead() {
		return extractMethProvider.getMethodHead();
	}
	
	public List<String> getMethodNames() {
		return extractMethProvider.getMethodNames();
	}

	public String getClassName() {
		return extractMethProvider.getClassName();
	}

	public int setMoveParameter(String varName, boolean upEvent, int numberOfMoves) {
		return extractMethProvider.setMoveParameter(varName, upEvent, numberOfMoves);
	}

	public void setParameterRename(Map<String,String> variablesToRename) {
		extractMethProvider.setParameterRename(variablesToRename);
		setChanged();
		notifyObservers();
	}

	public Parameter[] getCallAndMethHeadParameters() {
		return extractMethProvider.getCallAndMethHeadParameters();
	}

	public String getOriginalParameterName(int selectionIndex) {
		return extractMethProvider.getOriginalParameterName(selectionIndex);
	}

}
