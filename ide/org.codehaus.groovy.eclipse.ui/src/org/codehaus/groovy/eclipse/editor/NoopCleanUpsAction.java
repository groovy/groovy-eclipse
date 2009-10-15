package org.codehaus.groovy.eclipse.editor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

/**
 * 
 * @author Andrew Eisenberg
 * @created Aug 20, 2009
 * ensure that this class is a noop
 */
class NoopCleanUpsAction extends AllCleanUpsAction {

    public NoopCleanUpsAction(IWorkbenchSite site) {
        super(site);
    }

    @Override
    public void dispose() {
    }

    @Override
    protected ICleanUp[] getCleanUps(ICompilationUnit[] units) {
        return new ICleanUp[0];
    }

    @Override
    protected void performRefactoring(ICompilationUnit[] cus,
            ICleanUp[] cleanUps) throws InvocationTargetException {
    }

    @Override
    public ICompilationUnit[] getCompilationUnits(
            IStructuredSelection selection) {
        return new ICompilationUnit[0];
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