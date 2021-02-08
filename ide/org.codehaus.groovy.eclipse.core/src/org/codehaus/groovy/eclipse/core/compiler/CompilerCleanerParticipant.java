/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.compiler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

public class CompilerCleanerParticipant extends CompilationParticipant {

    private Set<IProject> building = Collections.synchronizedSet(new HashSet<>());

    @Override
    public boolean isActive(IJavaProject javaProject) {
        return GroovyNature.hasGroovyNature(javaProject.getProject()) && LanguageSupportFactory.isGroovyLanguageSupportInstalled();
    }

    @Override
    public void buildStarting(BuildContext[] files, boolean isBatch) {
        if (files == null || files.length == 0) return;
        building.add(files[0].getFile().getProject());
    }

    @Override
    public void buildFinished(IJavaProject javaProject) {
        building.remove(javaProject.getProject());
    }

    @Override
    public void cleanStarting(IJavaProject javaProject) {
        if (building.isEmpty())
            GenericsUtils.clearParameterizedTypeCache();
    }
}
