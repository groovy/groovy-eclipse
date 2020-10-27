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
package org.codehaus.groovy.eclipse.quickfix.test

import static org.eclipse.jdt.internal.core.util.Util.getProblemArgumentsFromMarker

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixProcessor
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.CoreException
import org.eclipse.jdt.core.IJavaModelMarker
import org.eclipse.jdt.internal.core.CompilationUnit
import org.eclipse.jdt.internal.ui.text.correction.AssistContext
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation
import org.eclipse.jdt.ui.text.java.IInvocationContext
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jdt.ui.text.java.IProblemLocation

@CompileStatic
abstract class QuickFixTestSuite extends GroovyEclipseTestSuite {

    @Override
    protected GroovyCompilationUnit addGroovySource(CharSequence contents, String name = "QuickFix${nextUnitName()}", String pack = '') {
        super.addGroovySource(contents, name, pack)
    }

    @Override
    protected CompilationUnit addJavaSource(CharSequence contents, String name = "QuickFix${nextUnitName()}", String pack = '') {
        super.addJavaSource(contents, name, pack)
    }

    protected IJavaCompletionProposal[] getGroovyQuickFixes(CompilationUnit unit) throws CoreException {
        IMarker[] markers = getJavaProblemMarkers(unit.resource)

        IProblemLocation[] locations = markers.collect { IMarker marker ->
            int offset = marker.getAttribute(IMarker.CHAR_START, -1)
            int length = marker.getAttribute(IMarker.CHAR_END, -1) - offset
            def isError = marker.getAttribute(IMarker.SEVERITY) == IMarker.SEVERITY_ERROR
            new ProblemLocation(offset, length, (int) marker.getAttribute(IJavaModelMarker.ID),
                getProblemArgumentsFromMarker(marker.getAttribute(IJavaModelMarker.ARGUMENTS, '')), isError, marker.type)
        }

        IInvocationContext context = new AssistContext(unit, 0, 0) // TODO: pass offset and length?

        return new GroovyQuickFixProcessor().getCorrections(context, locations)
    }

    protected IMarker[] getJavaProblemMarkers(IResource resource) {
        buildProject()
        return resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE)
    }
}
