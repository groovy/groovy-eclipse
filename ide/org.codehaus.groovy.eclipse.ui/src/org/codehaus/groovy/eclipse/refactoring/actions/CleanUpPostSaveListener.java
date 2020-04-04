/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpPreferenceUtil;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring.CleanUpChange;
import org.eclipse.jdt.internal.corext.fix.FixMessages;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.jdt.internal.ui.fix.IMultiLineCleanUp.MultiLineCleanUpContext;
import org.eclipse.jdt.internal.ui.fix.MapCleanUpOptions;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.IPostSaveListener;
import org.eclipse.jdt.internal.ui.preferences.BulletListBlock;
import org.eclipse.jdt.internal.ui.preferences.SaveParticipantPreferencePage;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * This is the original {@link org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener} from JDT with minor edits.
 */
public class CleanUpPostSaveListener implements IPostSaveListener {

    public static final String POSTSAVELISTENER_ID = "org.eclipse.jdt.ui.postsavelistener.cleanup"; //$NON-NLS-1$
    private static final String WARNING_VALUE = "warning"; //$NON-NLS-1$
    private static final String ERROR_VALUE = "error"; //$NON-NLS-1$
    private static final String CHANGED_REGION_POSITION_CATEGORY = "changed_region_position_category"; //$NON-NLS-1$
    private static boolean FIRST_CALL = false;
    private static boolean FIRST_CALL_DONE = false;

    private static class CleanUpSaveUndo extends TextFileChange {

        private final IFile fFile;
        private final UndoEdit[] fUndos;
        private final long fDocumentStamp;
        private final long fFileStamp;

        public CleanUpSaveUndo(String name, IFile file, UndoEdit[] undos, long documentStamp, long fileStamp) {
            super(name, file);
            Assert.isNotNull(undos);

            fDocumentStamp = documentStamp;
            fFileStamp = fileStamp;
            fFile = file;
            fUndos = undos;
        }

        @Override
        public final boolean needsSaving() {
            return true;
        }

        @Override
        public Change perform(IProgressMonitor monitor) throws CoreException {
            if (isValid(monitor).hasFatalError())
                return new NullChange();

            if (monitor == null)
                monitor = new NullProgressMonitor();
            monitor = SubMonitor.convert(monitor, "", 2); //$NON-NLS-1$

            ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
            ITextFileBuffer buffer = null;
            try {
                manager.connect(fFile.getFullPath(), LocationKind.IFILE, ((SubMonitor) monitor).split(1));
                buffer = manager.getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);

                final IDocument document = buffer.getDocument();
                final long oldFileValue = fFile.getModificationStamp();
                final LinkedList<UndoEdit> undoEditCollector = new LinkedList<>();
                final long[] oldDocValue = new long[1];
                final boolean[] setContentStampSuccess = { false };

                if (!buffer.isSynchronizationContextRequested()) {
                    performEdit(document, oldFileValue, undoEditCollector, oldDocValue, setContentStampSuccess);

                } else {
                    ITextFileBufferManager fileBufferManager = FileBuffers.getTextFileBufferManager();

                    class UIRunnable implements Runnable {
                        public boolean fDone;
                        public Exception fException;

                        @Override
                        public void run() {
                            synchronized (this) {
                                try {
                                    performEdit(document, oldFileValue, undoEditCollector, oldDocValue, setContentStampSuccess);
                                } catch (CoreException | BadLocationException | MalformedTreeException e) {
                                    fException = e;
                                } finally {
                                    fDone = true;
                                    notifyAll();
                                }
                            }
                        }
                    }
                    UIRunnable runnable = new UIRunnable();
                    synchronized (runnable) {
                        fileBufferManager.execute(runnable);
                        while (!runnable.fDone) {
                            try {
                                runnable.wait(500);
                            } catch (InterruptedException x) {
                            }
                        }
                    }

                    if (runnable.fException != null) {
                        if (runnable.fException instanceof BadLocationException) {
                            throw (BadLocationException) runnable.fException;
                        } else if (runnable.fException instanceof MalformedTreeException) {
                            throw (MalformedTreeException) runnable.fException;
                        } else if (runnable.fException instanceof CoreException) {
                            throw (CoreException) runnable.fException;
                        }
                    }
                }

                buffer.commit(monitor, false);
                if (!setContentStampSuccess[0]) {
                    fFile.revertModificationStamp(fFileStamp);
                }

                return new CleanUpSaveUndo(getName(), fFile, undoEditCollector.toArray(new UndoEdit[undoEditCollector.size()]), oldDocValue[0], oldFileValue);

            } catch (BadLocationException e) {
                throw wrapBadLocationException(e);
            } finally {
                if (buffer != null)
                    manager.disconnect(fFile.getFullPath(), LocationKind.IFILE, ((SubMonitor) monitor).split(1));
                // GROOVY add
                assertDocumentGreclipse1452(buffer);
                // GROOVY end
            }
        }

        private void performEdit(IDocument document, long oldFileValue, LinkedList<UndoEdit> editCollector, long[] oldDocValue, boolean[] setContentStampSuccess)
                throws MalformedTreeException, BadLocationException, CoreException {
            if (document instanceof IDocumentExtension4) {
                oldDocValue[0] = ((IDocumentExtension4) document).getModificationStamp();
            } else {
                oldDocValue[0] = oldFileValue;
            }

            // perform the changes
            for (int index = 0; index < fUndos.length; index++) {
                UndoEdit edit = fUndos[index];
                UndoEdit redo = edit.apply(document, TextEdit.CREATE_UNDO);
                editCollector.addFirst(redo);
            }

            if (document instanceof IDocumentExtension4 &&
                fDocumentStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
                try {
                    ((IDocumentExtension4) document).replace(0, 0, "", fDocumentStamp); //$NON-NLS-1$
                    setContentStampSuccess[0] = true;
                } catch (BadLocationException e) {
                    throw wrapBadLocationException(e);
                }
            }
        }
    }

    private static final class SlowCleanUpWarningDialog extends OptionalMessageDialog {

        private static final String ID = "SaveActions.slowWarningDialog"; //$NON-NLS-1$

        private final String fCleanUpNames;

        protected SlowCleanUpWarningDialog(Shell parent, String title, String cleanUpNames) {
            super(ID, parent, title, null, null, MessageDialog.WARNING, new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
            fCleanUpNames = cleanUpNames;
        }

        @Override
        protected Control createMessageArea(Composite parent) {
            initializeDialogUnits(parent);

            Composite messageComposite = new Composite(parent, SWT.NONE);
            messageComposite.setFont(parent.getFont());
            GridLayout layout = new GridLayout();
            layout.numColumns = 1;
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
            layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
            messageComposite.setLayout(layout);
            messageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            Label explain = new Label(messageComposite, SWT.WRAP);
            explain.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            explain.setText(FixMessages.CleanUpPostSaveListener_SlowCleanUpWarningDialog_explain);

            final BulletListBlock cleanUpListBlock = new BulletListBlock(messageComposite, SWT.NONE);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            cleanUpListBlock.setLayoutData(gridData);
            cleanUpListBlock.setText(fCleanUpNames);

            TextLayout textLayout = new TextLayout(messageComposite.getDisplay());
            textLayout.setText(fCleanUpNames);
            int lineCount = textLayout.getLineCount();
            if (lineCount < 5)
                gridData.heightHint = textLayout.getLineBounds(0).height * 6;
            textLayout.dispose();

            Link link = new Link(messageComposite, SWT.NONE);
            link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            link.setText(FixMessages.CleanUpPostSaveListener_SlowCleanUpDialog_link);

            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    PreferencesUtil.createPreferenceDialogOn(getShell(), SaveParticipantPreferencePage.PREFERENCE_PAGE_ID, null, null).open();
                }
            });

            return messageComposite;
        }
    }

    @Override
    public boolean needsChangedRegions(ICompilationUnit unit) throws CoreException {
        ICleanUp[] cleanUps = getCleanUps(unit.getJavaProject().getProject());
        return requiresChangedRegions(cleanUps);
    }

    @Override
    public void saved(ICompilationUnit unit, IRegion[] changedRegions, IProgressMonitor monitor) throws CoreException {
        monitor = SubMonitor.convert(monitor, getName(), IProgressMonitor.UNKNOWN);

        if (!ActionUtil.isOnBuildPath(unit))
            return;

        // GROOVY add
        // do not perform any cleanups if not a Groovy project
        IProject proj = unit.getJavaProject().getProject();
        if (proj == null || !org.codehaus.jdt.groovy.model.GroovyNature.hasGroovyNature(proj)) {
            return;
        }
        // GROOVY end

        ICleanUp[] cleanUps = getCleanUps(unit.getJavaProject().getProject());

        long oldFileValue = unit.getResource().getModificationStamp();
        long oldDocValue = getDocumentStamp((IFile) unit.getResource(), ((SubMonitor) monitor).split(2));

        CompositeChange result = new CompositeChange(FixMessages.CleanUpPostSaveListener_SaveAction_ChangeName);
        LinkedList<UndoEdit> undoEdits = new LinkedList<>();

        if (FIRST_CALL && !FIRST_CALL_DONE) {
            FIRST_CALL = false;
            FIRST_CALL_DONE = true;
        } else {
            FIRST_CALL = true;
        }
        HashSet<ICleanUp> slowCleanUps;
        if (FIRST_CALL_DONE) {
            slowCleanUps = new HashSet<>();
        } else {
            slowCleanUps = null;
        }
        IUndoManager manager = RefactoringCore.getUndoManager();

        boolean success = false;
        try {
            manager.aboutToPerformChange(result);

            do {
                RefactoringStatus preCondition = new RefactoringStatus();
                for (int i = 0; i < cleanUps.length; i++) {
                    RefactoringStatus conditions = cleanUps[i].checkPreConditions(unit.getJavaProject(),
                        new ICompilationUnit[] { unit }, ((SubMonitor) monitor).split(5));
                    preCondition.merge(conditions);
                }
                if (showStatus(preCondition) != Window.OK)
                    return;

                Map<String, String> options = new HashMap<>();
                for (int i = 0; i < cleanUps.length; i++) {
                    Map<String, String> map = cleanUps[i].getRequirements().getCompilerOptions();
                    if (map != null) {
                        options.putAll(map);
                    }
                }

                CompilationUnit ast = null;
                if (requiresAST(cleanUps)) {
                    ast = createAst(unit, options, ((SubMonitor) monitor).split(10));
                }

                CleanUpContext context;
                if (changedRegions == null) {
                    context = new CleanUpContext(unit, ast);
                } else {
                    context = new MultiLineCleanUpContext(unit, ast, changedRegions);
                }

                ArrayList<ICleanUp> undoneCleanUps = new ArrayList<>();
                CleanUpChange change =
                    CleanUpRefactoring.calculateChange(context, cleanUps, undoneCleanUps, slowCleanUps);

                RefactoringStatus postCondition = new RefactoringStatus();
                for (int i = 0; i < cleanUps.length; i++) {
                    RefactoringStatus conditions = cleanUps[i].checkPostConditions(((SubMonitor) monitor).split(1));
                    postCondition.merge(conditions);
                }
                if (showStatus(postCondition) != Window.OK)
                    return;

                cleanUps = undoneCleanUps.toArray(new ICleanUp[undoneCleanUps.size()]);
                if (change != null) {
                    result.add(change);

                    change.setSaveMode(TextFileChange.LEAVE_DIRTY);
                    change.initializeValidationData(new NullProgressMonitor());

                    PerformChangeOperation performChangeOperation = new PerformChangeOperation(change);
                    performChangeOperation.setSchedulingRule(unit.getSchedulingRule());

                    if (changedRegions != null && changedRegions.length > 0 && requiresChangedRegions(cleanUps)) {
                        changedRegions = performWithChangedRegionUpdate(performChangeOperation, changedRegions,
                            unit, ((SubMonitor) monitor).split(5));
                    } else {
                        performChangeOperation.run(((SubMonitor) monitor).split(5));
                    }

                    performChangeOperation.getUndoChange();
                    undoEdits.addFirst(change.getUndoEdit());
                }
            } while (cleanUps.length > 0);
            success = true;
        } finally {
            manager.changePerformed(result, success);
        }

        if (undoEdits.size() > 0) {
            UndoEdit[] undoEditArray = undoEdits.toArray(new UndoEdit[undoEdits.size()]);
            CleanUpSaveUndo undo = new CleanUpSaveUndo(result.getName(), (IFile) unit.getResource(), undoEditArray,
                oldDocValue, oldFileValue);
            undo.initializeValidationData(new NullProgressMonitor());
            manager.addUndo(result.getName(), undo);
        }

        if (slowCleanUps != null && slowCleanUps.size() > 0)
            showSlowCleanUpsWarning(slowCleanUps);
    }

    // GROOVY edit
    protected /*private static*/ ICleanUp[] getCleanUps(IProject project) throws CoreException {
        ICleanUp[] cleanUps;
        Map<String, String> settings = CleanUpPreferenceUtil.loadSaveParticipantOptions(new ProjectScope(project));
        if (settings == null) {
            IEclipsePreferences contextNode = InstanceScope.INSTANCE.getNode(JavaUI.ID_PLUGIN);
            String id = contextNode.get(CleanUpConstants.CLEANUP_ON_SAVE_PROFILE, null);
            if (id == null) {
                id = DefaultScope.INSTANCE.getNode(JavaUI.ID_PLUGIN).get(CleanUpConstants.CLEANUP_ON_SAVE_PROFILE,
                    CleanUpConstants.DEFAULT_SAVE_PARTICIPANT_PROFILE);
            }
            throw new CoreException(new Status(IStatus.ERROR, JavaUI.ID_PLUGIN,
                FixMessages.bind(FixMessages.CleanUpPostSaveListener_unknown_profile_error_message, id)));
        }

        if (CleanUpOptions.TRUE.equals(settings.get(CleanUpConstants.CLEANUP_ON_SAVE_ADDITIONAL_OPTIONS))) {
            cleanUps = getCleanUps(settings, null);
        } else {
            HashMap<String, String> filteredSettins = new HashMap<>();
            filteredSettins.put(CleanUpConstants.FORMAT_SOURCE_CODE, settings.get(CleanUpConstants.FORMAT_SOURCE_CODE));
            filteredSettins.put(CleanUpConstants.FORMAT_SOURCE_CODE_CHANGES_ONLY,
                settings.get(CleanUpConstants.FORMAT_SOURCE_CODE_CHANGES_ONLY));
            filteredSettins.put(CleanUpConstants.ORGANIZE_IMPORTS, settings.get(CleanUpConstants.ORGANIZE_IMPORTS));

            Set<String> ids = new HashSet<>(2);
            ids.add("org.eclipse.jdt.ui.cleanup.format"); //$NON-NLS-1$
            ids.add("org.eclipse.jdt.ui.cleanup.imports"); //$NON-NLS-1$
            cleanUps = getCleanUps(filteredSettins, ids);
        }

        return cleanUps;
    }

    // GROOVY edit
    protected /*private static*/ ICleanUp[] getCleanUps(Map<String, String> settings, Set<String> ids) {
        ICleanUp[] result = JavaPlugin.getDefault().getCleanUpRegistry().createCleanUps(ids);

        for (int i = 0; i < result.length; i++) {
            result[i].setOptions(new MapCleanUpOptions(settings));
        }

        return result;
    }

    private int showStatus(RefactoringStatus status) {
        if (!status.hasError())
            return Window.OK;

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        Dialog dialog = RefactoringUI.createRefactoringStatusDialog(status, shell, "", false); //$NON-NLS-1$
        return dialog.open();
    }

    private long getDocumentStamp(IFile file, IProgressMonitor monitor) throws CoreException {
        final ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        final IPath path = file.getFullPath();

        monitor = SubMonitor.convert(monitor, "", 2); //$NON-NLS-1$

        ITextFileBuffer buffer = null;
        try {
            manager.connect(path, LocationKind.IFILE, ((SubMonitor) monitor).split(1));
            buffer = manager.getTextFileBuffer(path, LocationKind.IFILE);
            IDocument document = buffer.getDocument();

            if (document instanceof IDocumentExtension4) {
                return ((IDocumentExtension4) document).getModificationStamp();
            } else {
                return file.getModificationStamp();
            }
        } finally {
            if (buffer != null)
                manager.disconnect(path, LocationKind.IFILE, ((SubMonitor) monitor).split(1));
            // GROOVY add
            assertDocumentGreclipse1452(buffer);
            // GROOVY end
        }
    }

    private IRegion[] performWithChangedRegionUpdate(PerformChangeOperation performChangeOperation,
        IRegion[] changedRegions, ICompilationUnit unit, IProgressMonitor monitor) throws CoreException {
        final ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        final IPath path = unit.getResource().getFullPath();

        monitor = SubMonitor.convert(monitor, "", 7); //$NON-NLS-1$

        ITextFileBuffer buffer = null;
        try {
            manager.connect(path, LocationKind.IFILE, ((SubMonitor) monitor).split(1));
            buffer = manager.getTextFileBuffer(path, LocationKind.IFILE);
            IDocument document = buffer.getDocument();

            document.addPositionCategory(CHANGED_REGION_POSITION_CATEGORY);
            DefaultPositionUpdater updater = new DefaultPositionUpdater(CHANGED_REGION_POSITION_CATEGORY);
            try {
                document.addPositionUpdater(updater);

                Position[] positions = new Position[changedRegions.length];
                for (int i = 0; i < changedRegions.length; i++) {
                    try {
                        Position position = new Position(changedRegions[i].getOffset(), changedRegions[i].getLength());
                        document.addPosition(CHANGED_REGION_POSITION_CATEGORY, position);

                        positions[i] = position;
                    } catch (BadLocationException e) {
                        throw wrapBadLocationException(e);
                    } catch (BadPositionCategoryException e) {
                        throw wrapBadPositionCategoryException(e);
                    }
                }

                performChangeOperation.run(((SubMonitor) monitor).split(5));

                ArrayList<Region> result = new ArrayList<>();
                for (int i = 0; i < positions.length; i++) {
                    Position position = positions[i];
                    if (!position.isDeleted())
                        result.add(new Region(position.getOffset(), position.getLength()));
                }

                return result.toArray(new IRegion[result.size()]);
            } finally {
                document.removePositionUpdater(updater);
                try {
                    document.removePositionCategory(CHANGED_REGION_POSITION_CATEGORY);
                } catch (BadPositionCategoryException e) {
                    throw wrapBadPositionCategoryException(e);
                }
            }
        } finally {
            if (buffer != null)
                manager.disconnect(path, LocationKind.IFILE, ((SubMonitor) monitor).split(1));
            // GROOVY add
            assertDocumentGreclipse1452(buffer);
            // GROOVY end
        }
    }

    private boolean requiresAST(ICleanUp[] cleanUps) {
        for (int i = 0; i < cleanUps.length; i++) {
            if (cleanUps[i].getRequirements().requiresAST())
                return true;
        }

        return false;
    }

    private boolean requiresChangedRegions(ICleanUp[] cleanUps) {
        for (int i = 0; i < cleanUps.length; i++) {
            CleanUpRequirements requirements = cleanUps[i].getRequirements();
            if (requirements.requiresChangedRegions())
                return true;
        }

        return false;
    }

    private CompilationUnit createAst(ICompilationUnit unit, Map<String, String> cleanUpOptions,
        IProgressMonitor monitor) {
        IJavaProject project = unit.getJavaProject();
        if (compatibleOptions(project, cleanUpOptions)) {
            CompilationUnit ast = SharedASTProvider.getAST(unit, SharedASTProvider.WAIT_NO, monitor);
            if (ast != null)
                return ast;
        }

        ASTParser parser = CleanUpRefactoring.createCleanUpASTParser();
        parser.setSource(unit);

        Map<String, String> compilerOptions = RefactoringASTParser.getCompilerOptions(unit.getJavaProject());
        compilerOptions.putAll(cleanUpOptions);
        parser.setCompilerOptions(compilerOptions);

        return (CompilationUnit) parser.createAST(monitor);
    }

    private boolean compatibleOptions(IJavaProject project, Map<String, String> cleanUpOptions) {
        if (cleanUpOptions.size() == 0)
            return true;

        Map<String, String> projectOptions = project.getOptions(true);

        for (Iterator<String> iterator = cleanUpOptions.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next();
            String projectOption = projectOptions.get(key);
            String cleanUpOption = cleanUpOptions.get(key);
            if (!strongerEquals(projectOption, cleanUpOption))
                return false;
        }

        return true;
    }

    private boolean strongerEquals(String projectOption, String cleanUpOption) {
        if (projectOption == null)
            return false;

        if (ERROR_VALUE.equals(cleanUpOption)) {
            return ERROR_VALUE.equals(projectOption);
        } else if (WARNING_VALUE.equals(cleanUpOption)) {
            return ERROR_VALUE.equals(projectOption) || WARNING_VALUE.equals(projectOption);
        }

        return false;
    }

    @Override
    public String getName() {
        return FixMessages.CleanUpPostSaveListener_name;
    }

    @Override
    public String getId() {
        return POSTSAVELISTENER_ID;
    }

    private static CoreException wrapBadLocationException(BadLocationException e) {
        String message = e.getMessage();
        if (message == null)
            message = "BadLocationException"; //$NON-NLS-1$
        return new CoreException(
            new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IRefactoringCoreStatusCodes.BAD_LOCATION, message, e));
    }

    private CoreException wrapBadPositionCategoryException(BadPositionCategoryException e) {
        String message = e.getMessage();
        if (message == null)
            message = "BadPositionCategoryException"; //$NON-NLS-1$
        return new CoreException(new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, 0, message, e));
    }

    private void showSlowCleanUpsWarning(HashSet<ICleanUp> slowCleanUps) {
        final StringBuffer cleanUpNames = new StringBuffer();
        for (Iterator<ICleanUp> iterator = slowCleanUps.iterator(); iterator.hasNext();) {
            ICleanUp cleanUp = iterator.next();
            String[] descriptions = cleanUp.getStepDescriptions();
            if (descriptions != null) {
                for (int i = 0; i < descriptions.length; i++) {
                    if (cleanUpNames.length() > 0)
                        cleanUpNames.append('\n');

                    cleanUpNames.append(descriptions[i]);
                }
            }
        }

        if (Display.getCurrent() != null) {
            showSlowCleanUpDialog(cleanUpNames);
        } else {
            Display.getDefault().asyncExec(() -> showSlowCleanUpDialog(cleanUpNames));
        }
    }

    private void showSlowCleanUpDialog(final StringBuffer cleanUpNames) {
        if (OptionalMessageDialog.isDialogEnabled(SlowCleanUpWarningDialog.ID)) {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            new SlowCleanUpWarningDialog(shell, FixMessages.CleanUpPostSaveListener_SlowCleanUpDialog_title,
                cleanUpNames.toString()).open();
        }
    }

    // GROOVY add
    /**
     * Checks the validity of the document and log error if there's a problem.
     */
    private static void assertDocumentGreclipse1452(final ITextFileBuffer buffer) {
        if (buffer != null) {
            try {
                buffer.getDocument().getPositions(IDocument.DEFAULT_CATEGORY);
            } catch (BadPositionCategoryException e) {
                org.codehaus.groovy.eclipse.core.GroovyCore.logException("GRECLIPSE-1452: Problem found in file. " + buffer.getLocation(), e);
            }
        }
    }
    // GROOVY end
}
