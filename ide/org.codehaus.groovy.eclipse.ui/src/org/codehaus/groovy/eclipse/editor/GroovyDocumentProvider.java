package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.editor.actions.GroovyCleanupPostSaveListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.IPostSaveListener;
import org.eclipse.jface.text.IRegion;


/**
 * 
 * Document provider for GroovyCompilationUnits in GroovyEditors
 * 
 * Ensures that Save listeners are properly handled and that
 * JDT operations are replaced with Groovy operations
 * 
 * @author Andrew Eisenberg
 * @created Aug 17, 2009
 *
 */
public class GroovyDocumentProvider extends CompilationUnitDocumentProvider {

    private GroovyCleanupPostSaveListener listener;
    
    public GroovyDocumentProvider() {
        super();
        listener = new GroovyCleanupPostSaveListener();
    }

    /**
     * Replace CleanUpPostSaveListener with our GroovyCleanupPostSaveListener
     */
    @Override
    protected void notifyPostSaveListeners(CompilationUnitInfo info,
            IRegion[] changedRegions, IPostSaveListener[] listeners,
            IProgressMonitor monitor) throws CoreException {
        
        if (listeners == null) {
            return;
        }
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof CleanUpPostSaveListener) {
                listeners[i] = listener;
            }
        }
        super.notifyPostSaveListeners(info, changedRegions, listeners, monitor);
    }
}
