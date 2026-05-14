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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceRefElement;

/**
 * Container for grabbed dependencies.
 *
 * @see org.eclipse.jdt.internal.core.ImportContainer
 * @see org.codehaus.jdt.groovy.model.GrapesContainerInfo
 */
public class GrapesContainer extends SourceRefElement implements IParent {

    protected GrapesContainer(ICompilationUnit gunit) {
        super((JavaElement) gunit);
    }

    @Override
    public String getElementName() {
        return "grapes";
    }

    @Override
    public int getElementType() {
        return 0;
    }

    @Override
    protected char getHandleMementoDelimiter() {
        return 0;
    }

    @Override
    public ISourceRange getNameRange() throws JavaModelException {
        return null;
    }
}
