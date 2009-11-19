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
package ExtractMethod;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.ExtractMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.ExtractMethodProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.objectweb.asm.Opcodes;

import tests.RefactoringTestCase;
import core.TestPrefInitializer;

/**
 * Test Case to test the ExtractMethod refactoring
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class ExtractMethodTestCase extends RefactoringTestCase {
	
	private ExtractMethodInfo info;

	public ExtractMethodTestCase(String arg0, File arg1) {
		super(arg0, arg1);
	}
	
	@Override
    public void preAction() {
		IPreferenceStore pref = TestPrefInitializer.initializePreferences(properties);
		ExtractMethodProvider provider = new ExtractMethodProvider(getDocumentProvider(),selection,pref);
		info = new ExtractMethodInfo(provider);
	}
	
	@Override
    public RefactoringStatus checkInitialCondition()
			throws OperationCanceledException, CoreException {
		return info.checkInitialConditions(new NullProgressMonitor());
	}
	
	@Override
    public void simulateUserInput() {
		// set refactoring parameters
		int modifier = 0;
		String newMethodName = "";
		try {
			newMethodName = properties.get("newMethodName");
			String mod = properties.get("modifier");
			if(mod.equals("private"))
				modifier = Opcodes.ACC_PRIVATE;
			if(mod.equals("def"))
				modifier = Opcodes.ACC_PUBLIC;
			if(mod.equals("protected"))
				modifier = Opcodes.ACC_PROTECTED;				
		} catch (Exception e) {
			e.printStackTrace();
			fail("Initialisation of testproperties failed! " + e.getMessage());
		}
		info.setModifier(modifier);
		info.setNewMethodname(newMethodName);
		setMoveParameter(info);
		setRenameParameter(info);
	}
	
	@Override
    public RefactoringStatus checkFinalCondition()
			throws OperationCanceledException, CoreException {
		return info.checkFinalConditions(new NullProgressMonitor());
	}

	@Override
    public GroovyChange createChange() throws OperationCanceledException,
			CoreException {
		return info.createGroovyChange(new NullProgressMonitor());
	}
	
	private void setRenameParameter(ExtractMethodInfo provider) {
		
		String variableToRename = properties.get("variableToRename");
		Map<String,String> variablesToRename = null;
		
		if(variableToRename != null){
			variablesToRename = new HashMap<String, String>();
			String[] renameMappings = variableToRename.split(";");
			for(int i = 0; i < renameMappings.length; i++){
				String[] singleRenames = renameMappings[i].split(":");
				if(singleRenames.length == 2){
					variablesToRename.put(singleRenames[0], singleRenames[1]);
				}
			}
			provider.setParameterRename(variablesToRename);
		}
		
	}

	private void setMoveParameter(ExtractMethodInfo provider) {
		
		String moveSettings = properties.get("moveVariable");
		
		if(moveSettings != null){
			boolean upEvent = false;
			int sortOfMoveCharPosition = moveSettings.indexOf('+');
			if(sortOfMoveCharPosition == -1){
				upEvent = true;
				sortOfMoveCharPosition = moveSettings.indexOf('-');
			}
			String varName = moveSettings.substring(0, sortOfMoveCharPosition); 
			int numberOfMoves = Integer.valueOf(moveSettings.substring(sortOfMoveCharPosition+1,moveSettings.length()));
			provider.setMoveParameter(varName, upEvent, numberOfMoves);
		}
	}

}
