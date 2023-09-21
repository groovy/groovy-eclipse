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

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.ImportDeclarationElementInfo;
import org.eclipse.jdt.internal.core.SourceRefElement;

public class GrabDeclaration extends SourceRefElement {

    private final Object elementInfo;
    private final String elementName;

    protected GrabDeclaration(GrapesContainer parent, final int sourceStart, final int sourceEnd, String group, String module, String version) {
        super(parent);

        elementInfo = new ImportDeclarationElementInfo() {{
            setSourceRangeStart(sourceStart);
            setSourceRangeEnd(sourceEnd);
            setNameSourceStart(sourceStart);
            setNameSourceEnd(sourceEnd);
        }};

        elementName = group + ':' + module + ':' + version;
    }

    @Override
    public Object getElementInfo() throws JavaModelException {
        return elementInfo;
    }

    @Override
    public String getElementName() {
        return elementName;
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
        return ReflectionUtils.executePrivateMethod(ImportDeclarationElementInfo.class, "getNameRange", getElementInfo());
    }
}
