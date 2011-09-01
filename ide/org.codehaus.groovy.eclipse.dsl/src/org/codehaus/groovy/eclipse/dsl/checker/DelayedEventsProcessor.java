package org.codehaus.groovy.eclipse.dsl.checker;

import java.util.ArrayList;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Helper class used to process delayed events.
 * Events currently supported:
 * <ul>
 * <li>SWT.OpenDocument</li>
 * </ul>
 * @since 3.3
 */
public class DelayedEventsProcessor implements Listener {

	private ArrayList filesToOpen = new ArrayList(1);

	/**
	 * Constructor.
	 * @param display display used as a source of event
	 */
	public DelayedEventsProcessor(Display display) {
		display.addListener(SWT.OpenDocument, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		final String path = event.text;
		if (path == null)
			return;
		// If we start supporting events that can arrive on a non-UI thread, the following
		// line will need to be in a "synchronized" block:
		filesToOpen.add(path);
	}
	
	/**
	 * Process delayed events.
	 * @param display display associated with the workbench 
	 */
	public void catchUp(Display display) {
		if (filesToOpen.isEmpty())
			return;
		
		// If we start supporting events that can arrive on a non-UI thread, the following
		// lines will need to be in a "synchronized" block:
		String[] filePaths = new String[filesToOpen.size()];
		filesToOpen.toArray(filePaths);
		filesToOpen.clear();

		for(int i = 0; i < filePaths.length; i++) {
			openFile(display, filePaths[i]);
		}
	}

	private void openFile(Display display, final String path) {
		display.asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return;
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
				IFileInfo fetchInfo = fileStore.fetchInfo();
				if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
					IWorkbenchPage page = window.getActivePage();
					if (page == null) {
						String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_noWindow, path);
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								IDEWorkbenchMessages.OpenDelayedFileAction_title,
								msg, SWT.SHEET);
					}
					try {
						IDE.openInternalEditorOnFileStore(page, fileStore);
						Shell shell = window.getShell();
						if (shell != null) {
							if (shell.getMinimized())
								shell.setMinimized(false);
							shell.forceActive();
						}
					} catch (PartInitException e) {
						String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_errorOnOpen,
										fileStore.getName());
						CoreException eLog = new PartInitException(e.getMessage());
						IDEWorkbenchPlugin.log(msg, new Status(IStatus.ERROR, GroovyDSLCoreActivator.PLUGIN_ID, msg, eLog));
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								IDEWorkbenchMessages.OpenDelayedFileAction_title,
								msg, SWT.SHEET);
					}
				} else {
					String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_fileNotFound, path);
					MessageDialog.open(MessageDialog.ERROR, window.getShell(),
							IDEWorkbenchMessages.OpenDelayedFileAction_title,
							msg, SWT.SHEET);
				}
			}
		});
	}

}
