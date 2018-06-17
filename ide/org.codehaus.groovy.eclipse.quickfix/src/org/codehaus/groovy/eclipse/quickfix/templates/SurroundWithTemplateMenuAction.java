/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.templates;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssist;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.JDTQuickMenuCreator;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class SurroundWithTemplateMenuAction implements IWorkbenchWindowPulldownDelegate2 {

    public static final String SURROUND_WITH_QUICK_MENU_ACTION_ID =
        "org.eclipse.jdt.ui.edit.text.java.surround.with.quickMenu"; //$NON-NLS-1$

    private static final String GROOVY_TEMPLATE_PREFERENCE_PAGE_ID =
        "org.codehaus.groovy.eclipse.quickfix.preferencepage.template"; //$NON-NLS-1$

    private static final String CODE_TEMPLATE_PREFERENCE_PAGE_ID =
        "org.eclipse.jdt.ui.preferences.CodeTemplatePreferencePage"; //$NON-NLS-1$

    private static final String TEMPLATE_GROUP = "templateGroup"; //$NON-NLS-1$

    private static final String CONFIG_GROUP = "configGroup"; //$NON-NLS-1$

    private static Action NONE_APPLICABLE_ACTION =
        new Action(ActionMessages.SurroundWithTemplateMenuAction_NoneApplicable) {
            @Override
            public void run() {
                //Do nothing
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };

    private Menu fMenu;
    private IPartService fPartService;
    private IPartListener fPartListener = new IPartListener() {

        @Override
        public void partActivated(IWorkbenchPart part) {
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
        }

        @Override
        public void partClosed(IWorkbenchPart part) {
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {
            disposeMenuItems();
        }

        @Override
        public void partOpened(IWorkbenchPart part) {
        }
    };

    protected void disposeMenuItems() {
        if (fMenu == null || fMenu.isDisposed()) {
            return;
        }
        MenuItem[] items = fMenu.getItems();
        for (int i = 0; i < items.length; i++) {
            MenuItem menuItem = items[i];
            if (!menuItem.isDisposed()) {
                menuItem.dispose();
            }
        }
    }

    @Override
    public Menu getMenu(Menu parent) {
        setMenu(new Menu(parent));
        fillMenu(fMenu);
        initMenu();
        return fMenu;
    }

    @Override
    public Menu getMenu(Control parent) {
        setMenu(new Menu(parent));
        fillMenu(fMenu);
        initMenu();
        return fMenu;
    }

    @Override
    public void dispose() {
        if (fPartService != null) {
            fPartService.removePartListener(fPartListener);
            fPartService = null;
        }
        setMenu(null);
    }

    @Override
    public void init(IWorkbenchWindow window) {
        if (fPartService != null) {
            fPartService.removePartListener(fPartListener);
            fPartService = null;
        }

        if (window != null) {
            IPartService partService = window.getPartService();
            if (partService != null) {
                fPartService = partService;
                partService.addPartListener(fPartListener);
            }
        }
    }

    @Override
    public void run(IAction action) {
        IWorkbenchPart activePart = JavaPlugin.getActivePage().getActivePart();
        if (!(activePart instanceof CompilationUnitEditor))
            return;

        final CompilationUnitEditor editor = (CompilationUnitEditor) activePart;

        new JDTQuickMenuCreator(editor) {
            @Override
            protected void fillMenu(IMenuManager menu) {
                SurroundWithTemplateMenuAction.fillMenu(menu, editor);
            }
        }.createMenu();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // Default do nothing
    }

    public static void fillMenu(IMenuManager menu, CompilationUnitEditor editor) {
        IAction[] actions = getTemplateActions(editor);

        if ((actions == null || actions.length == 0)) {
            menu.add(NONE_APPLICABLE_ACTION);
        } else {
            menu.add(new Separator(TEMPLATE_GROUP));
            for (int i = 0; actions != null && i < actions.length; i += 1) {
                menu.add(actions[i]);
            }
        }

        menu.add(new Separator(CONFIG_GROUP));
        menu.add(new ConfigureTemplatesAction());
    }

    /**
     * The menu to show in the workbench menu
     * @param menu the menu to fill entries into it
     */
    protected void fillMenu(Menu menu) {

        IWorkbenchPart activePart = JavaPlugin.getActivePage().getActivePart();
        if (!(activePart instanceof CompilationUnitEditor)) {
            ActionContributionItem item = new ActionContributionItem(NONE_APPLICABLE_ACTION);
            item.fill(menu, -1);
            return;
        }

        CompilationUnitEditor editor = (CompilationUnitEditor) activePart;
        if (editor.isBreadcrumbActive()) {
            ActionContributionItem item = new ActionContributionItem(NONE_APPLICABLE_ACTION);
            item.fill(menu, -1);
            return;
        }

        IAction[] actions = getTemplateActions(editor);

        boolean hasTemplateActions = actions != null && actions.length > 0;
        if (!hasTemplateActions) {
            ActionContributionItem item = new ActionContributionItem(NONE_APPLICABLE_ACTION);
            item.fill(menu, -1);
        } else if (hasTemplateActions) {
            for (int i = 0; i < actions.length; i++) {
                ActionContributionItem item = new ActionContributionItem(actions[i]);
                item.fill(menu, -1);
            }
        }

        Separator configGroup = new Separator(CONFIG_GROUP);
        configGroup.fill(menu, -1);

        ActionContributionItem configAction = new ActionContributionItem(new ConfigureTemplatesAction());
        configAction.fill(menu, -1);
    }

    protected void initMenu() {
        fMenu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                Menu m = (Menu) e.widget;
                MenuItem[] items = m.getItems();
                for (int i = 0; i < items.length; i++) {
                    items[i].dispose();
                }
                fillMenu(m);
            }
        });
    }

    private void setMenu(Menu menu) {
        if (fMenu != null) {
            fMenu.dispose();
        }
        fMenu = menu;
    }

    private static IAction[] getTemplateActions(JavaEditor editor) {
        ITextSelection textSelection = getTextSelection(editor);
        if (textSelection == null || textSelection.getLength() == 0)
            return null;

        ICompilationUnit cu = JavaUI.getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
        if (!(cu instanceof GroovyCompilationUnit))
            return null;

        GroovyQuickAssist quickTemplateProcessor = new GroovyQuickAssist();
        AssistContext context =
            new AssistContext(cu, editor.getViewer(), editor, textSelection.getOffset(), textSelection.getLength());

        List<IJavaCompletionProposal> proposals =
            quickTemplateProcessor.getTemplateAssists(context, (GroovyCompilationUnit) cu);

        if (proposals == null || proposals.isEmpty()) return null;

        return getActionsFromProposals(proposals, context.getSelectionOffset(), editor.getViewer());
    }

    private static ITextSelection getTextSelection(JavaEditor editor) {
        ISelectionProvider selectionProvider = editor.getSelectionProvider();
        if (selectionProvider == null)
            return null;

        ISelection selection = selectionProvider.getSelection();
        if (!(selection instanceof ITextSelection))
            return null;

        return (ITextSelection) selection;
    }

    @SuppressWarnings("unused")
    private static boolean isInJavadoc(JavaEditor editor) {
        ITextSelection selection = getTextSelection(editor);
        if (selection == null)
            return false;

        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        try {
            String contentType =
                TextUtilities.getContentType(document, IJavaPartitions.JAVA_PARTITIONING, selection.getOffset(), true);
            return contentType.equals(IJavaPartitions.JAVA_DOC);
        } catch (BadLocationException e) {
            return false;
        }
    }

    private static IAction[] getActionsFromProposals(List<IJavaCompletionProposal> proposals, final int offset, final ITextViewer viewer) {
        List<Action> result = new ArrayList<>();

        int j = 1;
        for (IJavaCompletionProposal cnadidate : proposals) {
            if (cnadidate instanceof ICompletionProposalExtension2) {
                final IJavaCompletionProposal proposal = cnadidate;

                StringBuffer actionName = new StringBuffer();
                if (j < 10) {
                    actionName.append('&').append(j).append(' ');
                }
                actionName.append(cnadidate.getDisplayString());

                Action action = new Action(actionName.toString()) {
                    @Override
                    public void run() {
                        applyProposal(proposal, viewer, (char) 0, 0, offset);
                    }
                };
                action.setImageDescriptor(JavaPluginImages.DESC_OBJS_TEMPLATE);

                result.add(action);
                j++;
            }
        }

        if (result.isEmpty()) return null;

        return result.toArray(new IAction[result.size()]);
    }

    private static void applyProposal(ICompletionProposal proposal, ITextViewer viewer, char trigger, int stateMask,
        final int offset) {
        Assert.isTrue(proposal instanceof ICompletionProposalExtension2);

        IRewriteTarget target = null;
        IEditingSupportRegistry registry = null;
        IEditingSupport helper = new IEditingSupport() {
            @Override
            public boolean isOriginator(DocumentEvent event, IRegion focus) {
                return focus.getOffset() <= offset && focus.getOffset() + focus.getLength() >= offset;
            }

            @Override
            public boolean ownsFocusShell() {
                return false;
            }
        };

        try {
            IDocument document = viewer.getDocument();

            if (viewer instanceof ITextViewerExtension) {
                ITextViewerExtension extension = (ITextViewerExtension) viewer;
                target = extension.getRewriteTarget();
            }

            if (target != null)
                target.beginCompoundChange();

            if (viewer instanceof IEditingSupportRegistry) {
                registry = (IEditingSupportRegistry) viewer;
                registry.register(helper);
            }

            ((ICompletionProposalExtension2) proposal).apply(viewer, trigger, stateMask, offset);

            Point selection = proposal.getSelection(document);
            if (selection != null) {
                viewer.setSelectedRange(selection.x, selection.y);
                viewer.revealRange(selection.x, selection.y);
            }
        } finally {
            if (target != null)
                target.endCompoundChange();

            if (registry != null)
                registry.unregister(helper);
        }
    }

    private static class ConfigureTemplatesAction extends Action {

        ConfigureTemplatesAction() {
            super(ActionMessages.SurroundWithTemplateMenuAction_ConfigureTemplatesActionName);
        }

        @Override
        public void run() {
            PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(
                getShell(), GROOVY_TEMPLATE_PREFERENCE_PAGE_ID, new String[] {GROOVY_TEMPLATE_PREFERENCE_PAGE_ID, CODE_TEMPLATE_PREFERENCE_PAGE_ID}, null);
            preferenceDialog.getTreeViewer().expandAll();
            preferenceDialog.open();
        }

        private Shell getShell() {
            return JavaPlugin.getActiveWorkbenchWindow().getShell();
        }
    }
}
