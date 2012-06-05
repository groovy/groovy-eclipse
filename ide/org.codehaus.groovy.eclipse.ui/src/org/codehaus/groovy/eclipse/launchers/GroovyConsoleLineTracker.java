/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.launchers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * 
 * @author Scott Hickey
 */
public class GroovyConsoleLineTracker implements IConsoleLineTracker {

    /**
     * @author Andrew Eisenberg
     * @created Sep 21, 2009
     *
     */
    public class AmbiguousFileLink extends FileLink implements IHyperlink {
        IFile[] files;
        boolean fileChosen = false;

        public AmbiguousFileLink(IFile[] files, String editorId, int fileOffset,
                int fileLength, int fileLineNumber) {
            super(null, editorId, fileOffset, fileLength, fileLineNumber);
            this.files = files;
        }

        @Override
        public void linkActivated() {
            if (!fileChosen) {
                IFile file = chooseFile(files);
                if (file != null) {
                    fileChosen = true;
                    ReflectionUtils.setPrivateField(FileLink.class, "fFile", this, file);
                }
            }
            if (fileChosen) {
                super.linkActivated();
            }
        }
    }

    /**
     * @author Andrew Eisenberg
     * @created Sep 21, 2009
     *
     */
    public class FileContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            IFile[] files = (IFile[]) inputElement;
            Object[] out = new Object[files.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = files[i];
            }
            return out;
        }

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

    }

    private IConsole console;
    private final static Pattern linePattern = Pattern.compile(".*\\((.*)\\.groovy(:(.*))?\\)");


    public void init(IConsole console) {
        this.console = console;
    }

    /**
     * Hyperlink error lines to the editor.
     */
    // not implemented yet
    public void lineAppended(IRegion line) {
        if (console == null) return;

        int lineOffset = line.getOffset();
        int lineLength = line.getLength();
        try {
            String consoleLine = console.getDocument().get(lineOffset, lineLength);
            GroovyPlugin.trace(consoleLine);
            Matcher m = linePattern.matcher(consoleLine);
            String groovyFileName = null;
            int lineNumber = -1;
            int openParenIndexAt = -1;
            int closeParenIndexAt = -1;
            // match
            if (m.matches()) {
                GroovyCore.trace("match: " + m);

                consoleLine = m.group(0);
                openParenIndexAt = consoleLine.indexOf("(");
                if (openParenIndexAt >= 0) {
                    int end = consoleLine.indexOf(".groovy");
                    if(end == -1 || (openParenIndexAt + 1) >= end) {
                        return;
                    }
                    String groovyClassName = consoleLine.substring(openParenIndexAt + 1, end);
                    int classIndex = consoleLine.indexOf(groovyClassName);
                    int start = 3;
                    if(classIndex < start || classIndex >= consoleLine.length()) {
                        return;
                    }
                    String groovyFilePath = consoleLine.substring(start, classIndex).trim().replace('.','/');
                    groovyFileName = groovyFilePath + groovyClassName + ".groovy";
                    int colonIndex = consoleLine.indexOf(":");
                    // get the line number in groovy class
                    closeParenIndexAt = consoleLine.lastIndexOf(")");
                    if (colonIndex > 0) {
                        lineNumber = Integer.parseInt(consoleLine.substring(colonIndex + 1, closeParenIndexAt));
                    }
                    GroovyPlugin.trace("groovyFile=" + groovyFileName + " lineNumber:" + lineNumber);
                }
                // hyperlink if we found something
                if (groovyFileName != null) {
                    IFile[] file = searchForFileInLaunchConfig(groovyFileName);
                    if (file.length == 1) {
                        IHyperlink link = new FileLink(file[0], GroovyEditor.EDITOR_ID, -1, -1, lineNumber);
                        console.addLink(link, lineOffset + openParenIndexAt + 1, closeParenIndexAt - openParenIndexAt -1);
                    } else if (file.length > 1) {
                        IHyperlink link = new AmbiguousFileLink(file, GroovyEditor.EDITOR_ID, -1, -1, lineNumber);
                        console.addLink(link, lineOffset + openParenIndexAt + 1, closeParenIndexAt - openParenIndexAt -1);
                    }
                }
            }
        } catch (Exception e) {
            GroovyPlugin.trace("unexpected error:" +  e.getMessage());
        }
    }

    /**
     * @param groovyFileName
     * @return
     * @throws JavaModelException
     */
    private IFile[] searchForFileInLaunchConfig(String groovyFileName) throws JavaModelException {
        List<IFile> files = new LinkedList<IFile>();
        IJavaProject[] projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
        for (IJavaProject javaProject : projects) {
            if (GroovyNature.hasGroovyNature(javaProject.getProject())) {
                for (IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
                    if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        IResource resource = root.getResource();
                        if (resource.isAccessible() && resource.getType() != IResource.FILE) {
                            IFile file = ((IContainer) resource).getFile(new Path(groovyFileName));
                            if (file.isAccessible()) {
                                files.add(file);
                            }
                        }
                    }
                }
            }
        }
        return files.toArray(new IFile[files.size()]);
    }

    /**
     * @param files
     * @return
     */
    IFile chooseFile(final IFile[] files) {
        final IFile[] result = new IFile[] { null };
        ConsolePlugin.getStandardDisplay().syncExec(new Runnable() {
            public void run() {
                final ListDialog dialog = new ListDialog(ConsolePlugin.getStandardDisplay().getActiveShell());
                dialog.setLabelProvider(new WorkbenchLabelProvider() {
                    @Override
                    protected String decorateText(String input, Object element) {
                        return ((IFile) element).getFullPath().toPortableString();
                    }
                });
                dialog.setTitle("Choose file to open");
                dialog.setInput(files);
                dialog.setContentProvider(new FileContentProvider());
                dialog.setAddCancelButton(true);
                dialog.setMessage("Select a file:");
                dialog.setBlockOnOpen(true);
                int dialogResult = dialog.open();
                if (dialogResult == Dialog.OK && dialog.getResult().length > 0) {
                    result[0] = (IFile) dialog.getResult()[0];
                }
            }
        });
        return result[0];
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
     */
    public void dispose() {
        console = null;
    }

}
