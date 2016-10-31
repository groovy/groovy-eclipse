/*
 * Copyright 2009-2016 the original author or authors.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

/**
 * ensure that this class is a noop
 *
 * @author Andrew Eisenberg
 * @created Aug 20, 2009
 */
class NoopCleanUpsAction extends AllCleanUpsAction {

    public NoopCleanUpsAction(IWorkbenchSite site) {
        super(site);
    }

    @Override
    protected ICleanUp[] getCleanUps(ICompilationUnit[] units) {
        return new ICleanUp[0];
    }

    @Override
    public ICompilationUnit[] getCompilationUnits(IStructuredSelection selection) {
        return new ICompilationUnit[0];
    }

    @Override
    protected void performRefactoring(ICompilationUnit[] cus, ICleanUp[] cleanUps) {
    }

    @Override
    public void run(IStructuredSelection selection) {
    }

    @Override
    public void run(ITextSelection selection) {
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
    }

    @Override
    public void selectionChanged(ITextSelection selection) {
    }
}
