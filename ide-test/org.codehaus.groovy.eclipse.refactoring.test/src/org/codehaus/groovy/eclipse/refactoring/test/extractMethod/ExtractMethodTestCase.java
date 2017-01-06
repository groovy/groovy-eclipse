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
package org.codehaus.groovy.eclipse.refactoring.test.extractMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
 * @author Michael Klenk mklenk@hsr.ch
 */
public class ExtractMethodTestCase extends RefactoringTestCase {

    private ExtractGroovyMethodRefactoring refactoring;
    private GroovyCompilationUnit unit;
    private TestProject testProject;

    public ExtractMethodTestCase(String name, File file) throws FileNotFoundException, IOException {
        super(name, file);
    }

    @Override
    public void preAction() throws FileNotFoundException, IOException {
        try {
            testProject = new TestProject();
            TestProject.setAutoBuilding(false);

            String fileName = "File" + new Random(System.currentTimeMillis()).nextInt(99999) + ".groovy";
            unit = (GroovyCompilationUnit) testProject.createGroovyTypeAndPackage("", fileName, getOrigin().get());

            int offset = getUserSelection().getOffset(),
                length = getUserSelection().getLength();
            RefactoringStatus status = new RefactoringStatus();
            System.out.printf("Attempting to extract new method from [%d,%d):%n %s%n", offset, offset + length,
                                        String.valueOf(unit.getContents()).substring(offset, offset + length));

            refactoring = new ExtractGroovyMethodRefactoring(unit, offset, length, status);
            refactoring.setPreferences(TestPrefInitializer.initializePreferences(getFileProperties(), testProject.getJavaProject()));

            if (status.getSeverity() != RefactoringStatus.OK) {
                fail("Bad refactoring status on init: " + status);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RefactoringStatus checkInitialCondition() throws CoreException, OperationCanceledException {
        return refactoring.checkInitialConditions(new NullProgressMonitor());
    }

    @Override
    public void simulateUserInput() {
        try {
            int modifier = 0;
            String mod = properties.get("modifier");
            if (mod.equals("def") || mod.equals("public"))
                modifier = Opcodes.ACC_PUBLIC;
            if (mod.equals("private"))
                modifier = Opcodes.ACC_PRIVATE;
            if (mod.equals("protected"))
                modifier = Opcodes.ACC_PROTECTED;

            refactoring.setModifier(modifier);
            refactoring.setNewMethodname(properties.get("newMethodName"));

            String moveSettings = properties.get("moveVariable");
            if (moveSettings != null && moveSettings.trim().length() > 0) {
                boolean upEvent = false;
                int sortOfMoveCharPosition = moveSettings.indexOf('+');
                if (sortOfMoveCharPosition == -1) {
                    upEvent = true;
                    sortOfMoveCharPosition = moveSettings.indexOf('-');
                }
                String varName = moveSettings.substring(0, sortOfMoveCharPosition);
                int numberOfMoves = Integer.valueOf(moveSettings.substring(sortOfMoveCharPosition + 1, moveSettings.length()));
                refactoring.setMoveParameter(varName, upEvent, numberOfMoves);
            }

            String variableToRename = properties.get("variableToRename");
            if (variableToRename != null && variableToRename.trim().length() > 0) {
                Map<String, String> variablesToRename = new HashMap<String, String>();
                for (String renameMapping : variableToRename.split(";")) {
                    String[] singleRenames = renameMapping.split(":");
                    if (singleRenames.length == 2) {
                        variablesToRename.put(singleRenames[0], singleRenames[1]);
                    }
                }
                refactoring.setParameterRename(variablesToRename);
            }
        } catch (Exception e) {
            fail("Initialisation of test properties failed! " + e.getMessage());
        }
    }

    @Override
    public RefactoringStatus checkFinalCondition() throws CoreException, OperationCanceledException {
        return refactoring.checkFinalConditions(new NullProgressMonitor());
    }

    @Override
    public Change createChange() throws CoreException, OperationCanceledException {
        return refactoring.createChange(new NullProgressMonitor());
    }

    @Override
    public void finalAssert() {
        getDocument().set(String.valueOf(unit.getContents()));
        super.finalAssert();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            testProject.dispose();
            testProject = null;
        } finally {
            super.tearDown();
        }
    }
}
