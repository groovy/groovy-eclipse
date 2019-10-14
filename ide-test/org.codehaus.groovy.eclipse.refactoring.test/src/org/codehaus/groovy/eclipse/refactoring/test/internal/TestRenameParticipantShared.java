/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.test.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaElementMapper;
import org.eclipse.jdt.core.refactoring.RenameTypeArguments;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.junit.Assert;

public final class TestRenameParticipantShared extends RenameParticipant implements ISharableParticipant {

    static TestRenameParticipantShared fgInstance;
    List<Object> fElements = new ArrayList<>(3);
    List<String> fHandles = new ArrayList<>(3);
    List<RenameArguments> fArguments = new ArrayList<>(3);
    Map<String, String> fSimilarToHandle = new HashMap<>();
    Map<String, String> fSimilarToNewName = new HashMap<>();

    @Override
    public boolean initialize(Object element) {
        fgInstance = this;
        fElements.add(element);
        fArguments.add(getArguments());
        if (element instanceof IJavaElement) {
            fHandles.add(((IJavaElement) element).getHandleIdentifier());
        } else {
            fHandles.add(((IResource) element).getFullPath().toString());
        }
        IJavaElementMapper updating = Adapters.adapt(getProcessor(), IJavaElementMapper.class);
        if ((updating != null) && getArguments() instanceof RenameTypeArguments) {
            RenameTypeArguments arguments = (RenameTypeArguments) getArguments();
            if (arguments.getUpdateSimilarDeclarations()) {
                IJavaElement[] elements = arguments.getSimilarDeclarations();
                for (int i = 0; i < elements.length; i++) {
                    IJavaElement updated = updating.getRefactoredJavaElement(elements[i]);
                    if (updated != null) {
                        fSimilarToHandle.put(elements[i].getHandleIdentifier(), getKey(updated));
                        fSimilarToNewName.put(elements[i].getHandleIdentifier(), updated.getElementName());
                    }
                }
            }
        }

        return true;
    }

    private String getKey(IJavaElement updated) {
        if (updated instanceof IType)
            return ((IType) updated).getKey();
        else if (updated instanceof IMethod)
            return ((IMethod) updated).getKey();
        else if (updated instanceof IField)
            return ((IField) updated).getKey();
        return "";
    }

    @Override
    public void addElement(Object element, RefactoringArguments args) {
        fElements.add(element);
        fArguments.add((RenameArguments) args);
        if (element instanceof IJavaElement) {
            fHandles.add(((IJavaElement) element).getHandleIdentifier());
        } else {
            fHandles.add(((IResource) element).getFullPath().toString());
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException {
        return null;
    }

    public static void testNumberOfElements(int expected) {
        if (expected == 0) {
            Assert.assertTrue(fgInstance == null);
        } else {
            Assert.assertEquals(expected, fgInstance.fElements.size());
            Assert.assertEquals(expected, fgInstance.fArguments.size());
        }
    }

    public static void testArguments(RenameArguments[] args) {
        testNumberOfElements(args.length);
        for (int i = 0; i < args.length; i++) {
            RenameArguments expected = args[i];
            RenameArguments actual = fgInstance.fArguments.get(i);
            Assert.assertEquals(expected.getNewName(), actual.getNewName());
            Assert.assertEquals(expected.getUpdateReferences(), actual.getUpdateReferences());
        }
    }

    public static void reset() {
        fgInstance = null;
    }

    public static void testNumberOfSimilarElements(int expected) {
        if (expected == 0) {
            Assert.assertTrue(fgInstance == null);
        } else {
            Assert.assertEquals(expected, fgInstance.fSimilarToHandle.size());
        }
    }

    public static void testSimilarElements(List<String> similarList, List<String> similarNewNameList, List<String> similarNewHandleList) {
        for (int i = 0; i < similarList.size(); i++) {
            String handle = similarList.get(i);
            String newHandle = similarNewHandleList.get(i);
            String newName = similarNewNameList.get(i);
            String actualNewHandle = fgInstance.fSimilarToHandle.get(handle);
            String actualNewName = fgInstance.fSimilarToNewName.get(handle);
            Assert.assertEquals("New element handle not as expected", newHandle, actualNewHandle);
            Assert.assertEquals("New element name not as expected", newName, actualNewName);
        }
        Assert.assertEquals(similarList.size(), fgInstance.fSimilarToHandle.size());
    }
}
