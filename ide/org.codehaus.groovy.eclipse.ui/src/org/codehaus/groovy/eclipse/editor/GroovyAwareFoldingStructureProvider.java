/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.text.folding.DefaultJavaFoldingStructureProvider;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Replacement for {@link DefaultJavaFoldingStructureProvider} that is intended
 * to provide Groovy-specific folding behavior for the {@link GroovyEditor}.
 */
public class GroovyAwareFoldingStructureProvider extends DefaultJavaFoldingStructureProvider {

    protected GroovyEditor editor;

    @Override
    public void install(ITextEditor editor, ProjectionViewer viewer) {
        super.install(editor, viewer);
        if (editor instanceof GroovyEditor)
            this.editor = (GroovyEditor) editor;
    }

    @Override
    public void uninstall() {
        this.editor = null;
        super.uninstall();
    }

    @Override
    protected void computeFoldingStructure(IJavaElement element, FoldingStructureComputationContext context) {
        // NOTE: be sure to call super.computeFoldingStructure when editor is null to preserve Java behavior
        // TODO: implement folding for multi-line closures
        // TODO: implement folding for script run methods
        super.computeFoldingStructure(element, context);
    }
}
