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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.corext.util.JavaElementResourceMapping;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.junit.Assert;

/**
 * Copied from {@link org.eclipse.jdt.ui.tests.refactoring.ParticipantTesting}.
 */
public final class ParticipantTesting {

    public static void reset() {
        TestRenameParticipantShared.reset();
        TestRenameParticipantSingle.reset();
    }

    public static String[] createHandles(Object object) {
        return createHandles(new Object[] {object});
    }

    public static String[] createHandles(Object obj1, Object obj2) {
        return createHandles(new Object[] {obj1, obj2});
    }

    public static String[] createHandles(Object obj1, Object obj2, Object obj3) {
        return createHandles(new Object[] {obj1, obj2, obj3});
    }

    public static String[] createHandles(Object obj1, Object obj2, Object obj3, Object obj4) {
        return createHandles(new Object[] {obj1, obj2, obj3, obj4});
    }

    public static String[] createHandles(Object[] elements) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < elements.length; i++) {
            Object element = elements[i];
            if (element instanceof IJavaElement) {
                result.add(((IJavaElement) element).getHandleIdentifier());
            } else if (element instanceof IResource) {
                result.add(((IResource) element).getFullPath().toString());
            } else if (element instanceof JavaElementResourceMapping) {
                result.add(((JavaElementResourceMapping) element).getJavaElement().getHandleIdentifier() + "_mapping");
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static void testRename(String[] expectedHandles, RenameArguments[] args) {
        Assert.assertEquals(expectedHandles.length, args.length);
        if (expectedHandles.length == 0) {
            TestRenameParticipantShared.testNumberOfElements(0);
            TestRenameParticipantSingle.testNumberOfInstances(0);
        } else {
            testElementsShared(expectedHandles, TestRenameParticipantShared.fgInstance.fHandles);
            TestRenameParticipantShared.testArguments(args);

            TestRenameParticipantSingle.testNumberOfInstances(expectedHandles.length);
            TestRenameParticipantSingle.testElements(expectedHandles);
            TestRenameParticipantSingle.testArguments(args);
        }
    }

    private static void testElementsShared(String[] expected, List<String> actual) {
        for (int i = 0; i < expected.length; i++) {
            String handle = expected[i];
            Assert.assertTrue("Expected handle not found: " + handle, actual.contains(handle));
        }
        testNumberOfElements(expected.length, actual);
    }

    private static void testNumberOfElements(int expected, List<?> actual) {
        if (expected == 0 && actual == null)
            return;
        Assert.assertEquals(expected, actual.size());
    }
}
