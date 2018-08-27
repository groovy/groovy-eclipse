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
package org.codehaus.groovy.eclipse.ui;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyProjectFacade;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Determines if a groovy file has a main method or is a script.
 */
public class GroovyResourcePropertyTester extends PropertyTester {

    /**
     * Property name to determine if a class has a main method
     */
    public static final String hasMain = "hasMain";
    public static final String isScript = "isScript";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        boolean result = false;

        if (hasMain.equals(property) || isScript.equals(property)) {
            try {
                if (receiver instanceof IFile) {
                    ICompilationUnit unit = JavaCore.createCompilationUnitFrom((IFile) receiver);
                    result = isRunnable(unit);

                } else if (receiver instanceof IAdaptable) {
                    ICompilationUnit unit = Adapters.adapt(receiver, ICompilationUnit.class);
                    result = isRunnable(unit);

                    if (unit == null) {
                        IFile file = Adapters.adapt(receiver, IFile.class);
                        if (file != null) {
                            unit = JavaCore.createCompilationUnitFrom(file);
                            result = isRunnable(unit);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // can ignore; passed in non-JavaLike file name
            } catch (JavaModelException e) {
                // can ignore situations when trying to find types that are not on the classpath
                if (e.getStatus() != null && e.getStatus().getCode() != IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH) {
                    GroovyCore.logException("Exception when testing for main methods " + receiver, e);
                }
            }
        }

        return result;
    }

    private boolean isRunnable(ICompilationUnit unit) throws JavaModelException {
        if (unit instanceof GroovyCompilationUnit) {
            for (IType type : unit.getAllTypes()) {
                if (GroovyProjectFacade.hasRunnableMain(type)) {
                    return true;
                }
            }
        }
        return false;
    }
}
