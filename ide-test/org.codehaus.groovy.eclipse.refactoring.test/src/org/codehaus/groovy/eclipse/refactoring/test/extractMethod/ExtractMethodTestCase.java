/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.extractMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyMethodRefactoring;
import org.codehaus.groovy.eclipse.refactoring.test.TestPrefInitializer;
import org.codehaus.groovy.eclipse.test.TestProject;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Test Case to test the ExtractMethod refactoring
 *
 * @author Michael Klenk mklenk@hsr.ch
 */
public class ExtractMethodTestCase extends RefactoringTestCase {

    private ExtractGroovyMethodRefactoring refactoring;
    private GroovyCompilationUnit unit;
    private TestProject testProject;

    public ExtractMethodTestCase(String arg0, File arg1) throws FileNotFoundException, IOException {
        super(arg0, arg1);
    }

    @Override
    public void preAction() throws FileNotFoundException, IOException {
        try {
            testProject = new TestProject();
            unit = (GroovyCompilationUnit) testProject.createGroovyTypeAndPackage("", "File.groovy", getOrigin().get());
            RefactoringStatus status = new RefactoringStatus();
            refactoring = new ExtractGroovyMethodRefactoring(unit, getUserSelection().getOffset(), getUserSelection().getLength(), status);
            refactoring.setPreferences(
                TestPrefInitializer.initializePreferences(getFileProperties(), testProject.getJavaProject()));
            if (status.getSeverity() != RefactoringStatus.OK) {
                fail("Bad refactoring status on init: " + status);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        unit.discardWorkingCopy();
        testProject.dispose();
    }

    @Override
    public RefactoringStatus checkInitialCondition() throws OperationCanceledException, CoreException {
        return refactoring.checkInitialConditions(new NullProgressMonitor());
    }

    @Override
    public void simulateUserInput() {
        // set refactoring parameters
        int modifier = 0;
        String newMethodName = "";
        try {
            newMethodName = properties.get("newMethodName");
            String mod = properties.get("modifier");
            if (mod.equals("private"))
                modifier = Opcodes.ACC_PRIVATE;
            if (mod.equals("def"))
                modifier = Opcodes.ACC_PUBLIC;
            if (mod.equals("protected"))
                modifier = Opcodes.ACC_PROTECTED;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Initialisation of testproperties failed! " + e.getMessage());
        }
        refactoring.setModifier(modifier);
        refactoring.setNewMethodname(newMethodName);
        setMoveParameter(refactoring);
        setRenameParameter(refactoring);
    }

    @Override
    public RefactoringStatus checkFinalCondition() throws OperationCanceledException, CoreException {
        return refactoring.checkFinalConditions(new NullProgressMonitor());
    }

    @Override
    public Change createChange() throws OperationCanceledException, CoreException {
        return refactoring.createChange(new NullProgressMonitor());
    }

    @Override
    public void finalAssert() {
        getDocument().set(String.valueOf(unit.getContents()));
        super.finalAssert();
    }

    private void setRenameParameter(ExtractGroovyMethodRefactoring provider) {
        String variableToRename = properties.get("variableToRename");
        Map<String, String> variablesToRename = null;
        if (variableToRename != null) {
            variablesToRename = new HashMap<String, String>();
            String[] renameMappings = variableToRename.split(";");
            for (int i = 0; i < renameMappings.length; i++) {
                String[] singleRenames = renameMappings[i].split(":");
                if (singleRenames.length == 2) {
                    variablesToRename.put(singleRenames[0], singleRenames[1]);
                }
            }
            provider.setParameterRename(variablesToRename);
        }
    }

    private void setMoveParameter(ExtractGroovyMethodRefactoring provider) {
        String moveSettings = properties.get("moveVariable");
        if (moveSettings != null) {
            boolean upEvent = false;
            int sortOfMoveCharPosition = moveSettings.indexOf('+');
            if (sortOfMoveCharPosition == -1) {
                upEvent = true;
                sortOfMoveCharPosition = moveSettings.indexOf('-');
            }
            String varName = moveSettings.substring(0, sortOfMoveCharPosition);
            int numberOfMoves = Integer.valueOf(moveSettings.substring(sortOfMoveCharPosition + 1, moveSettings.length()));
            provider.setMoveParameter(varName, upEvent, numberOfMoves);
        }
    }
}
