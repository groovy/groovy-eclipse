/*
 * Copyright 2009-2020 the original author or authors.
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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.junit.Assert;

public final class TestRenameParticipantSingle extends RenameParticipant {

    private static List<RenameParticipant> fgInstances = new ArrayList<>();

    private Object fElement;
    private String fHandle;

    @Override
    public boolean initialize(Object element) {
        fgInstances.add(this);
        fElement = element;
        ref(fElement);
        if (fElement instanceof IJavaElement) {
            fHandle = ((IJavaElement) fElement).getHandleIdentifier();
        } else {
            fHandle = ((IResource) fElement).getFullPath().toString();
        }
        return true;
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

    public static void testNumberOfInstances(int instances) {
        Assert.assertEquals(instances, fgInstances.size());
    }

    public static void testElements(String[] handles) {
        testNumberOfInstances(handles.length);
        List<String> l1 = Arrays.asList(handles);
        for (int i = 0; i < l1.size(); i++) {
            Assert.assertTrue(l1.contains(getInstance(i).fHandle));
        }
    }

    public static void testArguments(RenameArguments[] args) {
        testNumberOfInstances(args.length);
        for (int i = 0; i < args.length; i++) {
            RenameArguments expected = args[i];
            RenameArguments actual = getInstance(i).getArguments();
            Assert.assertEquals(expected.getNewName(), actual.getNewName());
            Assert.assertEquals(expected.getUpdateReferences(), actual.getUpdateReferences());
        }
    }

    public static void reset() {
        fgInstances = new ArrayList<>();
    }

    private static TestRenameParticipantSingle getInstance(int i) {
        return ((TestRenameParticipantSingle) fgInstances.get(i));
    }

    void ref(Object element) {
    }
}
