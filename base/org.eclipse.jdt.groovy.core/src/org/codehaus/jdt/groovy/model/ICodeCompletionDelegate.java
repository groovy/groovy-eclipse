/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.jdt.groovy.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * Used so that {@link GroovyCompilationUnit} can delegate to another class for its code completion
 */
public interface ICodeCompletionDelegate {

    void codeComplete(ICompilationUnit cu, ICompilationUnit unitToSkip, int position, CompletionRequestor requestor,
        WorkingCopyOwner owner, ITypeRoot typeRoot, IProgressMonitor monitor) throws JavaModelException;

    boolean shouldCodeComplete(CompletionRequestor requestor, ITypeRoot typeRoot);
}
