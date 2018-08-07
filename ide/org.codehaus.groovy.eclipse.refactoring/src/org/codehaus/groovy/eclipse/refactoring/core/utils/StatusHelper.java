/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

public class StatusHelper {

    public static RefactoringStatus convertStatus(IStatus status) {
        RefactoringStatus result = new RefactoringStatus();
        for (IStatus child : status.getChildren()) {
            result.addEntry(child.getSeverity(), child.getMessage(), null, child.getPlugin(), child.getCode());
        }
        return result;
    }

    public static RefactoringStatusContext createContext(IMember member) {
        Class<?> JavaStatusContext = getJavaStatusContext();
        //return org.eclipse.jdt.internal.corext.refactoring.[base|util].JavaStatusContext.create(member);
        return ReflectionUtils.executePrivateMethod(JavaStatusContext, "create", new Class[] {IMember.class}, JavaStatusContext, new Object[] {member});
    }

    public static RefactoringStatusContext createContext(ITypeRoot typeRoot, ISourceRange sourceRange) {
        Class<?> JavaStatusContext = getJavaStatusContext();
        //return org.eclipse.jdt.internal.corext.refactoring.[base|util].JavaStatusContext.create(typeRoot, sourceRange);
        return ReflectionUtils.executePrivateMethod(JavaStatusContext, "create", new Class[] {ITypeRoot.class, ISourceRange.class}, JavaStatusContext, new Object[] {typeRoot, sourceRange});
    }

    private static Class<?> getJavaStatusContext() {
        if (JAVA_STATUS_CONTEXT == null) {
            try {
                JAVA_STATUS_CONTEXT = Class.forName("org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext");
            } catch (ClassNotFoundException ignore) {
                try {
                    JAVA_STATUS_CONTEXT = Class.forName("org.eclipse.jdt.internal.corext.refactoring.util.JavaStatusContext");
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return JAVA_STATUS_CONTEXT;
    }
    private static Class<?> JAVA_STATUS_CONTEXT;
}
